package org.eclipse.pde.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.plugin.ExtensionHyperLink;
import org.eclipse.pde.internal.ui.editor.text.BundleHyperlink;
import org.eclipse.pde.internal.ui.editor.text.JavaHyperlink;
import org.eclipse.pde.internal.ui.editor.text.PackageHyperlink;
import org.eclipse.pde.internal.ui.editor.text.ResourceHyperlink;
import org.eclipse.pde.internal.ui.editor.text.SchemaHyperlink;
import org.eclipse.pde.internal.ui.editor.text.TranslationHyperlink;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.texteditor.ITextEditor;

public class HyperlinkAction extends Action implements MouseListener, KeyListener {

	protected IHyperlinkDetector fDetector;
	protected StyledText fStyledText;
	protected IHyperlink fLink;
		
	public HyperlinkAction() {
		setImageDescriptor(PDEPluginImages.DESC_LINK_OBJ);
		setEnabled(false);
	}
	
	public void run() {
		if (fLink != null)
			fLink.open();
	}
	
	protected void removeListeners() {
		if (!hasDetector() || isTextDisposed())
			return;
		fStyledText.removeMouseListener(this);
		fStyledText.removeKeyListener(this);
	}
	
	protected void addListeners() {
		if (!hasDetector() || isTextDisposed())
			return;
		fStyledText.addMouseListener(this);
		fStyledText.addKeyListener(this);
	}
	
	public boolean detectHyperlink() {
		fLink = null;
		if (!hasDetector() || isTextDisposed())
			return false;
		
		int offset = fStyledText.getCaretOffset();
		IHyperlink[] links = fDetector.detectHyperlinks(null, new Region(offset, 0), false);
		
		if (links == null || links.length == 0)
			return false;

		fLink = links[0];
		return true;
	}

	public void setTextEditor(ITextEditor editor) {
		StyledText newText = editor instanceof PDESourcePage ? 
				((PDESourcePage)editor).getViewer().getTextWidget() : null;
		if (fStyledText != null && fStyledText.equals(newText))
			return;
		
		// remove the previous listeners if there were any
		removeListeners();
		fStyledText = newText;
		fDetector = editor instanceof PDESourcePage ? 
				(IHyperlinkDetector)((PDESourcePage)editor).getAdapter(IHyperlinkDetector.class) : null;
		// Add new listeners, if hyperlinks are present
		addListeners();
		
		setEnabled(detectHyperlink());
		generateActionText();
	}

	protected boolean hasDetector() {
		return fDetector != null;
	}
	
	private boolean isTextDisposed() {
		return fStyledText == null || !fStyledText.isDisposed();
	}
	
	public void generateActionText() {
		String text = null;
		if (fLink instanceof JavaHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenType;
		else if (fLink instanceof ExtensionHyperLink)
			text = PDEUIMessages.HyperlinkActionOpenDescription;
		else if (fLink instanceof BundleHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenBundle;
		else if (fLink instanceof PackageHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenPackage;
		else if (fLink instanceof ResourceHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenResource;
		else if (fLink instanceof SchemaHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenSchema;
		else if (fLink instanceof TranslationHyperlink)
			text = PDEUIMessages.HyperlinkActionOpenTranslation;
		setText(text);
		setToolTipText(text);
	}	
	
	public void mouseDoubleClick(MouseEvent e) {
		// Ignore
	}

	public void mouseDown(MouseEvent e) {
		setEnabled(detectHyperlink());
		generateActionText();
	}

	public void mouseUp(MouseEvent e) {
		// Ignore
	}

	public void keyPressed(KeyEvent e) {
		setEnabled(detectHyperlink());
		generateActionText();
	}

	public void keyReleased(KeyEvent e) {
		// Ignore
	}

}
