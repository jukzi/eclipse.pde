/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.neweditor.plugin;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.IModelChangeProvider;
import org.eclipse.pde.core.osgi.bundle.IBundlePluginBase;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.manifest.JavaAttributeValue;
import org.eclipse.pde.internal.ui.neweditor.FormEntryAdapter;
import org.eclipse.pde.internal.ui.neweditor.PDEFormPage;
import org.eclipse.pde.internal.ui.neweditor.PDESection;
import org.eclipse.pde.internal.ui.neweditor.manifest.JavaAttributeWizard;
import org.eclipse.pde.internal.ui.newparts.ComboPart;
import org.eclipse.pde.internal.ui.newparts.FormEntry;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;

public class GeneralInfoSection extends PDESection {
	public static final String KEY_MATCH = "ManifestEditor.PluginSpecSection.versionMatch";
	public static final String KEY_MATCH_PERFECT = "ManifestEditor.MatchSection.perfect";
	public static final String KEY_MATCH_EQUIVALENT = "ManifestEditor.MatchSection.equivalent";
	public static final String KEY_MATCH_COMPATIBLE = "ManifestEditor.MatchSection.compatible";
	public static final String KEY_MATCH_GREATER = "ManifestEditor.MatchSection.greater";
	private FormEntry fIdEntry;
	private FormEntry fVersionEntry;
	private FormEntry fNameEntry;
	private FormEntry fProviderEntry;
	private FormEntry fClassEntry;
	private FormEntry fPluginIdEntry;
	private FormEntry fPluginVersionEntry;
	private ComboPart fMatchCombo;
	/**
	 * @param page
	 * @param parent
	 * @param style
	 */
	public GeneralInfoSection(PDEFormPage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION);
		createClient(getSection(), page.getEditor().getToolkit());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.neweditor.PDESection#createClient(org.eclipse.ui.forms.widgets.Section,
	 *      org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	protected void createClient(Section section, FormToolkit toolkit) {
		section.setText(PDEPlugin
				.getResourceString("ManifestEditor.PluginSpecSection.title"));
		section.setDescription(PDEPlugin
				.getResourceString("ManifestEditor.PluginSpecSection.desc"));
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = toolkit.getBorderStyle() != SWT.NULL ? 0 : 2;
		layout.numColumns = 3;
		client.setLayout(layout);
		section.setClient(client);
		IActionBars actionBars = getPage().getPDEEditor().getEditorSite()
				.getActionBars();
		createIDEntry(client, toolkit, actionBars);
		createVersionEntry(client, toolkit, actionBars);
		createNameEntry(client, toolkit, actionBars);
		createProviderEntry(client, toolkit, actionBars);
		if (isFragment()) {
			createPluginIDEntry(client, toolkit, actionBars);
			createPluginVersionEntry(client, toolkit, actionBars);
			createMatchCombo(client, toolkit, actionBars);
		} else {
			createClassEntry(client, toolkit, actionBars);
		}
		toolkit.paintBordersFor(client);
		IBaseModel model = getPage().getModel();
		if (model instanceof IModelChangeProvider)
			((IModelChangeProvider) model).addModelChangedListener(this);
	}
	public String getContextId() {
		if (getPluginBase() instanceof IBundlePluginBase)
			return BundleInputContext.CONTEXT_ID;
		return PluginInputContext.CONTEXT_ID;
	}
	private IPluginBase getPluginBase() {
		IBaseModel model = getPage().getPDEEditor().getAggregateModel();
		return ((IPluginModelBase) model).getPluginBase();
	}
	private void createIDEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fIdEntry = new FormEntry(client, toolkit, "ID:", null, false);
		fIdEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					getPluginBase().setId(entry.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fIdEntry.setEditable(isEditable());
	}
	private void createVersionEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fVersionEntry = new FormEntry(client, toolkit, "Version:", null, false);
		fVersionEntry.setFormEntryListener(new FormEntryAdapter(this,
				actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					getPluginBase().setVersion(entry.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fVersionEntry.setEditable(isEditable());
	}
	private void createNameEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fNameEntry = new FormEntry(client, toolkit, "Name:", null, false);
		fNameEntry.setFormEntryListener(new FormEntryAdapter(this, actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					getPluginBase().setName(entry.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fNameEntry.setEditable(isEditable());
	}
	private void createProviderEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fProviderEntry = new FormEntry(client, toolkit, "Provider:", null,
				false);
		fProviderEntry.setFormEntryListener(new FormEntryAdapter(this,
				actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					getPluginBase().setProviderName(entry.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fProviderEntry.setEditable(isEditable());
	}
	private void createClassEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fClassEntry = new FormEntry(client, toolkit, "Class:", "Browse...",
				true);
		fClassEntry
				.setFormEntryListener(new FormEntryAdapter(this, actionBars) {
					public void textValueChanged(FormEntry entry) {
						try {
							((IPlugin) getPluginBase()).setClassName(entry
									.getValue());
						} catch (CoreException e) {
							PDEPlugin.logException(e);
						}
					}
					public void linkActivated(HyperlinkEvent e) {
						String value = fClassEntry.getValue();
						value = trimNonAlphaChars(value);
						if (value.length() > 0 && doesClassExist(value))
							doOpenClass();
						else {
							JavaAttributeValue javaAttVal = createJavaAttributeValue();
							JavaAttributeWizard wizard = new JavaAttributeWizard(
									javaAttVal);
							WizardDialog dialog = new WizardDialog(PDEPlugin
									.getActiveWorkbenchShell(), wizard);
							dialog.create();
							SWTUtil.setDialogSize(dialog, 400, 500);
							int result = dialog.open();
							if (result == WizardDialog.OK) {
								String newValue = wizard.getClassNameWithArgs();
								fClassEntry.setValue(newValue);
							}
						}
					}
					public void browseButtonSelected(FormEntry entry) {
						doOpenSelectionDialog();
					}
				});
		fClassEntry.setEditable(isEditable());
	}
	private String trimNonAlphaChars(String value) {
		value = value.trim();
		while (value.length() > 0 && !Character.isLetter(value.charAt(0)))
			value = value.substring(1, value.length());
		int loc = value.indexOf(":");
		if (loc!=-1 && loc >0)
			value = value.substring(0,loc);
		else if (loc == 0)
			value = "";
		return value;
	}
	private boolean doesClassExist(String className) {
		IProject project = getPage().getPDEEditor().getCommonProject();
		String path = className.replace('.', '/') + ".java";
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject javaProject = JavaCore.create(project);

				IJavaElement result = javaProject.findElement(new Path(path));
				return result != null;
			} else {
				IResource resource = project.findMember(new Path(path));
				return resource != null;
			}
		} catch (JavaModelException e) {
			return false;
		} catch (CoreException e) {
			return false;
		}
	}
	private JavaAttributeValue createJavaAttributeValue() {
		IProject project = getPage().getPDEEditor().getCommonProject();
		IPluginModelBase model = (IPluginModelBase) getPage().getModel();
		String value = fClassEntry.getValue();
		return new JavaAttributeValue(project, model, null, value);
	}
	private void createPluginIDEntry(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		fPluginIdEntry = new FormEntry(client, toolkit, "Plug-in Id:", null,
				true);
		fPluginIdEntry.setFormEntryListener(new FormEntryAdapter(this,
				actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					((IFragment) getPluginBase()).setPluginId(entry.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
			public void linkActivated(HyperlinkEvent e) {
			}
		});
		fPluginIdEntry.setEditable(isEditable());
	}
	private void createPluginVersionEntry(Composite client,
			FormToolkit toolkit, IActionBars actionBars) {
		fPluginVersionEntry = new FormEntry(client, toolkit,
				"Plug-in Version:", null, false);
		fPluginVersionEntry.setFormEntryListener(new FormEntryAdapter(this,
				actionBars) {
			public void textValueChanged(FormEntry entry) {
				try {
					((IFragment) getPluginBase()).setPluginVersion(entry
							.getValue());
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fPluginVersionEntry.setEditable(isEditable());
	}
	private void createMatchCombo(Composite client, FormToolkit toolkit,
			IActionBars actionBars) {
		toolkit.createLabel(client, PDEPlugin.getResourceString(KEY_MATCH));
		fMatchCombo = new ComboPart();
		fMatchCombo.createControl(client, toolkit, SWT.READ_ONLY);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = 20;
		fMatchCombo.getControl().setLayoutData(gd);
		String[] items = new String[]{"",
				PDEPlugin.getResourceString(KEY_MATCH_EQUIVALENT),
				PDEPlugin.getResourceString(KEY_MATCH_COMPATIBLE),
				PDEPlugin.getResourceString(KEY_MATCH_PERFECT),
				PDEPlugin.getResourceString(KEY_MATCH_GREATER)};
		fMatchCombo.setItems(items);
		fMatchCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int match = fMatchCombo.getSelectionIndex();
				try {
					((IFragment) getPluginBase()).setRule(match);
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		});
		fMatchCombo.getControl().setEnabled(isEditable());
	}
	private boolean isFragment() {
		IPluginModelBase model = (IPluginModelBase) getPage().getPDEEditor()
				.getContextManager().getAggregateModel();
		return model.isFragmentModel();
	}
	public void commit(boolean onSave) {
		fIdEntry.commit();
		fNameEntry.commit();
		fProviderEntry.commit();
		if (isFragment()) {
			fPluginIdEntry.commit();
			fPluginVersionEntry.commit();
		} else {
			fClassEntry.commit();
		}
		super.commit(onSave);
	}
	public void refresh() {
		IPluginModelBase model = (IPluginModelBase) getPage().getPDEEditor()
				.getContextManager().getAggregateModel();
		IPluginBase pluginBase = model.getPluginBase();
		fIdEntry.setValue(pluginBase.getId(), true);
		fNameEntry.setValue(pluginBase.getName(), true);
		fVersionEntry.setValue(pluginBase.getVersion(), true);
		fProviderEntry.setValue(pluginBase.getProviderName(), true);
		if (isFragment()) {
			IFragment fragment = (IFragment) pluginBase;
			fPluginIdEntry.setValue(fragment.getPluginId(), true);
			fPluginVersionEntry.setValue(fragment.getPluginVersion(), true);
			fMatchCombo.select(fragment.getRule());
		} else {
			IPlugin plugin = (IPlugin) pluginBase;
			fClassEntry.setValue(plugin.getClassName(), true);
		}
		super.refresh();
	}
	public void dispose() {
		IBaseModel model = getPage().getModel();
		if (model instanceof IModelChangeProvider)
			((IModelChangeProvider) model).removeModelChangedListener(this);
		super.dispose();
	}
	private void doOpenClass() {
		String name = fClassEntry.getText().getText();
		name = trimNonAlphaChars(name);
		IProject project = getPage().getPDEEditor().getCommonProject();
		String path = name.replace('.', '/') + ".java";
		try {
			if (project.hasNature(JavaCore.NATURE_ID)){
				IJavaProject javaProject = JavaCore.create(project);
				IJavaElement result = javaProject.findElement(new Path(path));
				JavaUI.openInEditor(result);
			} else {
				IResource resource = project.findMember(new Path(path));
				if (resource!=null && resource instanceof IFile){
					IWorkbenchPage page = PDEPlugin.getActivePage();
					IDE.openEditor(page, (IFile)resource, true);
				}
			}
		} catch (PartInitException e) {
			PDEPlugin.logException(e);
		} catch (JavaModelException e) {
			// nothing
			Display.getCurrent().beep();
		} catch (CoreException e){
			PDEPlugin.logException(e);
		}
	}
	private void doOpenSelectionDialog() {
		try {
			Shell shell = PDEPlugin.getActiveWorkbenchShell();
			IResource resource = getPluginBase().getModel()
					.getUnderlyingResource();
			IProject project = (resource == null) ? null : resource
					.getProject();
			if (project != null){
				SelectionDialog dialog = JavaUI.createTypeDialog(shell,
						new ProgressMonitorDialog(shell),
						getSearchScope(project),
						IJavaElementSearchConstants.CONSIDER_CLASSES, false,
						"*");
				dialog.setTitle("Select Type");
				if (dialog.open() == SelectionDialog.OK) {
					IType type = (IType) dialog.getResult()[0];
					fClassEntry.setValue(type.getFullyQualifiedName());
				}
			}
		} catch (CoreException e) {
		}
	}
	private IJavaSearchScope getSearchScope(IProject project) {
		IJavaProject jProject = JavaCore.create(project);
		return SearchEngine.createJavaSearchScope(getDirectRoots(jProject));
	}
	private IPackageFragmentRoot[] getDirectRoots(IJavaProject project) {
		ArrayList result = new ArrayList();
		try {
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE
						|| (roots[i].isArchive() && !roots[i].isExternal())) {
					result.add(roots[i]);
				}
			}
		} catch (JavaModelException e) {
		}
		return (IPackageFragmentRoot[]) result
				.toArray(new IPackageFragmentRoot[result.size()]);
	}
}