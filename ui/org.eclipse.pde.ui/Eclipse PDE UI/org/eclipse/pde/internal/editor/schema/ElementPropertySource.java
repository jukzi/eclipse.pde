package org.eclipse.pde.internal.editor.schema;

import org.eclipse.pde.internal.schema.*;
import java.util.*;
import org.eclipse.pde.internal.base.schema.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.internal.*;

public class ElementPropertySource extends SchemaObjectPropertySource {
	public static final String P_LABEL_ATTRIBUTE = "labelAttribute";
	public static final String P_ICON = "icon";
	public static final String P_NAME = "name";
	public static final String KEY_NAME = "SchemaEditor.ElementPR.name";
	public static final String KEY_ICON = "SchemaEditor.ElementPR.icon";
	public static final String KEY_LABEL_ATTRIBUTE = "SchemaEditor.ElementPR.labelAttribute";
	public static final String KEY_INVALID = "SchemaEditor.ElementPR.invalid";
	private Vector descriptors;

	class LabelAttributeValidator implements ICellEditorValidator {
		public String isValid(Object value) {
			String svalue = value.toString();
			if (isValidAttribute(svalue) == false) {
				return PDEPlugin.getFormattedMessage(KEY_INVALID, svalue);
			}
			return null;
		}
	}

public ElementPropertySource(ISchemaElement extension) {
	super(extension);
}
private void fixReferences(SchemaElement element) {
	((Schema)element.getSchema()).updateReferencesFor(element);
}
public Object getEditableValue() {
	return null;
}
public IPropertyDescriptor[] getPropertyDescriptors() {
	if (descriptors==null) {
		descriptors = new Vector();
		PropertyDescriptor desc = createTextPropertyDescriptor(P_LABEL_ATTRIBUTE, PDEPlugin.getResourceString(KEY_LABEL_ATTRIBUTE));
		desc.setValidator(new LabelAttributeValidator());
		descriptors.addElement(desc);
		desc = createTextPropertyDescriptor(P_ICON, PDEPlugin.getResourceString(KEY_ICON));
		descriptors.addElement(desc);
		desc = createTextPropertyDescriptor(P_NAME, PDEPlugin.getResourceString(KEY_NAME));
		descriptors.addElement(desc);
	}
	return toDescriptorArray(descriptors);
}
public Object getPropertyValue(Object name) {
	ISchemaElement element = (ISchemaElement) getSourceObject();
	if (name.equals(P_LABEL_ATTRIBUTE))
		return getNonzeroValue(element.getLabelProperty());
	if (name.equals(P_ICON))
		return getNonzeroValue(element.getIconName());
	if (name.equals(P_NAME))
		return getNonzeroValue(element.getName());
	return "";
}
public boolean isPropertySet(Object property) {
	return false;
}
private boolean isValidAttribute(String name) {
	if (name==null || name.length()==0) return true;
	ISchemaElement element = (ISchemaElement)getSourceObject();
	return element.getAttribute(name)!=null;
}
public void resetPropertyValue(Object property) {}
public void setPropertyValue(Object name, Object value) {
	SchemaElement element = (SchemaElement) getSourceObject();
	String svalue = (String) value;
	if (name.equals(P_LABEL_ATTRIBUTE))
		element.setLabelProperty(svalue);
	else
		if (name.equals(P_ICON))
			element.setIconName(svalue);
	else
		if (name.equals(P_NAME)) {
			element.setName(svalue);
			fixReferences(element);
		}
}
}
