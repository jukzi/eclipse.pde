package org.eclipse.pde.internal.editor.manifest;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.w3c.dom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.*;
import org.w3c.dom.Document;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.*;
import org.eclipse.pde.internal.*;

public class ExtensionPointForm extends ScrollableSectionForm {
	public static final String FORM_TITLE = "ManifestEditor.ExtensionPointForm.title";
	private ManifestExtensionPointPage page;
	private DetailExtensionPointSection extensionPointSection;
	private PointUsageSection usageSection;

public ExtensionPointForm(ManifestExtensionPointPage page) {
	this.page = page;
	setVerticalFit(true);
	setScrollable(true);
}
protected void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	parent.setLayout(layout);
	layout.makeColumnsEqualWidth=true;
	layout.numColumns = 2;
	layout.marginWidth = 10;
	layout.horizontalSpacing=15;
	extensionPointSection = new DetailExtensionPointSection(page);
	Control control = extensionPointSection.createControl(parent, getFactory());
	GridData gd = new GridData(GridData.FILL_BOTH);
	control.setLayoutData(gd);

	usageSection = new PointUsageSection(page);
	control = usageSection.createControl(parent, getFactory());
	gd = new GridData(GridData.FILL_BOTH);
	control.setLayoutData(gd);

	// Link
	SectionChangeManager manager = new SectionChangeManager();
	manager.linkSections(extensionPointSection, usageSection);

	registerSection(extensionPointSection);
	registerSection(usageSection);
}
public void expandTo(Object object) {
   extensionPointSection.expandTo(object);
}
public void initialize(Object model) {
	setHeadingText(PDEPlugin.getResourceString(FORM_TITLE));
	super.initialize(model);
	((Composite)getControl()).layout(true);
}
}
