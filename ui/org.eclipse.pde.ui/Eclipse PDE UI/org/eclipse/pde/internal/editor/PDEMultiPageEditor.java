package org.eclipse.pde.internal.editor;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.pde.internal.editor.text.*;
import java.lang.reflect.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.actions.*;
import org.eclipse.pde.internal.base.model.*;
import java.io.*;
import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.jface.action.*;
import java.util.*;
import org.eclipse.swt.layout.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.editors.text.*;
import org.eclipse.jface.text.*;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.internal.*;
import org.eclipse.pde.internal.forms.*;
import org.eclipse.pde.internal.base.model.IModel;
import org.eclipse.jface.viewers.*;
import java.util.*;
import org.eclipse.ui.texteditor.*;


public abstract class PDEMultiPageEditor extends EditorPart implements ISelectionProvider {
	public static final String WRONG_EDITOR = "MultiPageEditor.wrongEditor";
	public static final String TAG_TYPE = "input_type";
	public static final String TYPE_WORKBENCH = "workbench_file";
	public static final String TYPE_SYSTEM = "system_file";
	public static final String TAG_PATH = "input_path";

	protected IFormWorkbook formWorkbook;
	private SelectionProvider selectionProvider = new SelectionProvider();
	protected Object model;
	protected IModelChangedListener modelListener;
	private Vector pages;
	protected String firstPageId;
	private PDEMultiPageContentOutline contentOutline = new PDEMultiPageContentOutline(this);
	private PDEMultiPagePropertySheet propertySheet=new PDEMultiPagePropertySheet();
	private Hashtable table = new Hashtable();
	private Menu contextMenu;
	private IDocumentProvider documentProvider;
	private boolean disposed;

public PDEMultiPageEditor() {
	formWorkbook = new CustomWorkbook();
	pages = new Vector();
	createPages();
}
public void addPage(String id, IPDEEditorPage page) {
	table.put(id, page);
	pages.addElement(page);
}
public void addSelectionChangedListener(ISelectionChangedListener listener) {
	selectionProvider.addSelectionChangedListener(listener);
}
public void commitFormPages(boolean onSave) {
	for (Iterator iter = getPages(); iter.hasNext();) {
		IPDEEditorPage page = (IPDEEditorPage) iter.next();
		if (page instanceof PDEFormPage) {
			PDEFormPage formPage = (PDEFormPage) page;
			formPage.getForm().commitChanges(onSave);
		}
	}
}
protected IDocumentPartitioner createDocumentPartitioner() {
	return null;
}
private IDocumentProvider createDocumentProvider(Object input) {
	IDocumentProvider documentProvider = null;
	if (input instanceof IFile)
		documentProvider = new FileDocumentProvider() {
		public IDocument createDocument(Object element) throws CoreException {
			IDocument document = super.createDocument(element);
			if (document != null) {
				IDocumentPartitioner partitioner = createDocumentPartitioner();
				if (partitioner != null) {
					partitioner.connect(document);
					document.setDocumentPartitioner(partitioner);
				}
			}
			return document;
		}
	};
	else
		if (input instanceof File) {
			documentProvider = new SystemFileDocumentProvider(createDocumentPartitioner());
		}
	return documentProvider;
}
protected abstract Object createModel(Object input);
protected abstract void createPages();
public void createPartControl(Composite parent) {
	formWorkbook.createControl(parent);
	formWorkbook.addFormSelectionListener(new IFormSelectionListener() {
		public void formSelected(IFormPage page) {
			updateSynchronizedViews((IPDEEditorPage) page);
			getContributor().setActivePage((IPDEEditorPage) page);
			if (page instanceof PDEFormPage) {
				PDEFormPage formPage = (PDEFormPage) page;
				if (formPage.getSelection() != null)
					setSelection(formPage.getSelection());
			}
		}
	});
	MenuManager manager = new MenuManager();
	IMenuListener listener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			editorContextMenuAboutToShow(manager);
		}
	};
	manager.setRemoveAllWhenShown(true);
	manager.addMenuListener(listener);
	contextMenu = manager.createContextMenu(formWorkbook.getControl());

	for (Iterator iter = pages.iterator(); iter.hasNext();) {
		IFormPage page = (IFormPage) iter.next();
		formWorkbook.addPage(page);
	}
	if (firstPageId != null)
		showPage(firstPageId);
}
public void dispose() {
	setSelection(new StructuredSelection());
	for (int i = 0; i < pages.size(); i++) {
		IWorkbenchPart part = (IWorkbenchPart) pages.elementAt(i);
		part.dispose();
	}
	IEditorInput input = getEditorInput();
	IAnnotationModel amodel = documentProvider.getAnnotationModel(input);
	if (amodel != null)
		amodel.disconnect(documentProvider.getDocument(input));
	documentProvider.disconnect(input);
	if (modelListener != null && model instanceof IModelChangeProvider) {
		((IModelChangeProvider)model).removeModelChangedListener(modelListener);
	}
	disposed=true;
}
public void doSave(IProgressMonitor monitor) {
	final IEditorInput input = getEditorInput();
	commitFormPages(true);
	updateDocument();
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		public void execute(final IProgressMonitor monitor) throws CoreException {
			documentProvider.saveDocument(
				monitor,
				input,
				documentProvider.getDocument(input));
		}
	};

	Shell shell = getSite().getWorkbenchWindow().getShell();

	try {
		documentProvider.aboutToChange(input);
		op.run(monitor);
		documentProvider.changed(input);
		fireSaveNeeded();
	} catch (InterruptedException x) {
	} catch (InvocationTargetException x) {
		PDEPlugin.logException(x);
	}
}
public void doSaveAs() {
	getCurrentPage().doSaveAs();
}
public void editorContextMenuAboutToShow(IMenuManager menu) {
	PDEEditorContributor contributor = getContributor();
	getCurrentPage().contextMenuAboutToShow(menu);
	if (contributor!=null) contributor.contextMenuAboutToShow(menu);

}
public void fireSaveNeeded() {
	firePropertyChange(PROP_DIRTY);
	PDEEditorContributor contributor = getContributor();
	if (contributor!=null) contributor.updateActions();
}
public IAction getAction(String id) {
	return getContributor().getGlobalAction(id);
}
public Object getAdapter(Class key) {
	if (key.equals(IContentOutlinePage.class)) {
		return getContentOutline();
	}
	if (key.equals(IPropertySheetPage.class)) {
		return getPropertySheet();
	}
	return super.getAdapter(key);
}
public PDEMultiPageContentOutline getContentOutline() {
	if (contentOutline == null
		|| (contentOutline.getControl() != null
			&& contentOutline.getControl().isDisposed())) {
		contentOutline = new PDEMultiPageContentOutline(this);
	}
	return contentOutline;
}
public org.eclipse.swt.widgets.Menu getContextMenu() {
	return contextMenu;
}
public PDEEditorContributor getContributor() {
	return (PDEEditorContributor)getEditorSite().getActionBarContributor();
}
public IPDEEditorPage getCurrentPage() {
	return (IPDEEditorPage)formWorkbook.getCurrentPage();
}
public IDocumentProvider getDocumentProvider() {
	return documentProvider;
}
public abstract IPDEEditorPage getHomePage();
public Object getModel() {
	return model;
}
public IPDEEditorPage getPage(String pageId) {
	return (IPDEEditorPage)table.get(pageId);
}
public Iterator getPages() {
	return pages.iterator();
}
public PDEMultiPagePropertySheet getPropertySheet() {
	if (propertySheet == null
		|| (propertySheet.getControl() != null
			&& propertySheet.getControl().isDisposed())) {
		propertySheet = new PDEMultiPagePropertySheet();
	}
	return propertySheet;
}
public ISelection getSelection() {
	return selectionProvider.getSelection();
}
protected abstract String getSourcePageId();
public IStatusLineManager getStatusLineManager() {
	PDEEditorContributor contributor = getContributor();
	if (contributor != null)
		return contributor.getStatusLineManager();
	return null;
}
public void gotoMarker(IMarker marker) {
	showPage(getPage(getSourcePageId())).gotoMarker(marker);
}
public void init(IEditorSite site, IEditorInput input)
	throws PartInitException {

	if (isValidContentType(input) == false) {
		String message = PDEPlugin.getFormattedMessage(WRONG_EDITOR, input.getName());
		IStatus s =
			new Status(IStatus.ERROR, PDEPlugin.getPluginId(), IStatus.OK, message, null);
		throw new PartInitException(s);
	}

	setSite(site);
	setInput(input);

	Object inputObject = null;
	if (input instanceof SystemFileEditorInput) {
		inputObject = input.getAdapter(File.class);
	} else
		if (input instanceof FileEditorInput) {
			inputObject = input.getAdapter(IFile.class);
		}
	site.setSelectionProvider(this);
	initializeModels(inputObject);
	for (Iterator iter = pages.iterator(); iter.hasNext();) {
		IEditorPart part = (IEditorPart) iter.next();
		part.init(site, input);
	}
	if (inputObject instanceof IFile)
		setTitle(((IFile) inputObject).getName());
	else
		if (inputObject instanceof java.io.File)
			setTitle("system:" + ((java.io.File) inputObject).getName());
		else
			setTitle(input.toString());
}
protected void initializeModels(Object input) {
	documentProvider = createDocumentProvider(input);
	if (documentProvider == null)
		return;
	// create document provider
	model = createModel(input);
	if (model instanceof IModelChangeProvider) {
		modelListener = new IModelChangedListener() {
			public void modelChanged(IModelChangedEvent e) {
				if (e.getChangeType() != IModelChangedEvent.WORLD_CHANGED)
					fireSaveNeeded();
			}
		};
		((IModelChangeProvider) model).addModelChangedListener(modelListener);
	}

	try {
		IEditorInput editorInput = getEditorInput();
		documentProvider.connect(editorInput);
		IAnnotationModel amodel = documentProvider.getAnnotationModel(editorInput);
		if (amodel != null)
			amodel.connect(documentProvider.getDocument(editorInput));
	} catch (CoreException e) {
		PDEPlugin.logException(e);
	}
	if (isModelCorrect(model) == false) {
		firstPageId = getSourcePageId();
	}
}
public boolean isDirty() {
	if (isModelDirty(model))
		return true;
	if (documentProvider != null)
		return documentProvider.canSaveDocument(getEditorInput());
	return false;
}
public boolean isDisposed() {
	return disposed;
}
public boolean isEditable() {
	if (model instanceof IModel) {
		return ((IModel)model).isEditable();
	}
	return true;
}
protected boolean isModelCorrect(Object model) {
	return true;
}
protected abstract boolean isModelDirty(Object model);
public boolean isSaveAsAllowed() {
	return false;
}
protected boolean isValidContentType(IEditorInput input) {
	return true;
}
protected void performGlobalAction(String id) {
	getCurrentPage().performGlobalAction(id);
}
public void registerContentOutline(IPDEEditorPage page) {
	IContentOutlinePage outlinePage = page.getContentOutlinePage();
	outlinePage.createControl(contentOutline.getPagebook());
}
public void removePage(IPDEEditorPage page) {
	formWorkbook.removePage(page);
	pages.removeElement(page);
}
public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	selectionProvider.removeSelectionChangedListener(listener);
}
public void setFocus() {
	//getCurrentPage().setFocus();
}
public void setSelection(ISelection selection) {
	selectionProvider.setSelection(selection);
}
public IPDEEditorPage showPage(String id) {
	return showPage(getPage(id));
}
public void showPage(String id, Object openToObject) {
	IPDEEditorPage page = showPage(getPage(id));
	if (page != null)
		page.openTo(openToObject);
}
public IPDEEditorPage showPage(final IPDEEditorPage page) {
	IPDEEditorPage oldPage = getCurrentPage();
	formWorkbook.selectPage(page);
	return page;
}
void updateDocument() {
	// if model is dirty, flush its content into
	// the document so that the source editor will
	// pick up the changes.
	if (!(model instanceof IEditable))
		return;
	IEditable editableModel = (IEditable) model;
	if (editableModel.isDirty() == false)
		return;
	try {
		// need to update the document
		IDocument document = documentProvider.getDocument(getEditorInput());
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bstream);
		editableModel.save(writer);
		writer.flush();
		bstream.close();
		document.set(bstream.toString());
	} catch (IOException e) {
		PDEPlugin.logException(e);
	}
}
protected abstract boolean updateModel();
public void updateSynchronizedViews(IPDEEditorPage page) {
	IContentOutlinePage outlinePage = page.getContentOutlinePage();
	if (outlinePage != null) {
		contentOutline.setPageActive(outlinePage);
	}
	IPropertySheetPage propertySheetPage = page.getPropertySheetPage();
	if (propertySheetPage != null) {
		propertySheet.setPageActive(propertySheetPage);
	}
	else {
		propertySheet.setDefaultPageActive();
	}
}
}
