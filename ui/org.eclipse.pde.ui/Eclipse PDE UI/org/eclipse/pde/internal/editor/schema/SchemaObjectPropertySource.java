package org.eclipse.pde.internal.editor.schema;

import java.util.*;
import org.eclipse.pde.internal.editor.*;
import org.eclipse.pde.internal.base.schema.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.jface.viewers.*;

public abstract class SchemaObjectPropertySource implements IPropertySource {
	private Object sourceObject;

	class ComboProvider extends LabelProvider {
		private String property;
		private String [] table;
		public ComboProvider(String property, String [] table) {
			this.property = property;
			this.table = table;
		}
		public String getText(Object obj) {
			Integer index = (Integer)getPropertyValue(property);
			return table[index.intValue()];
		}
	}

public SchemaObjectPropertySource(Object object) {
	this.sourceObject = object;
}
protected PropertyDescriptor createComboBoxPropertyDescriptor(
	String id,
	String name,
	String[] choices) {
	if (isEditable())
		return new ComboBoxPropertyDescriptor(id, name, choices);
	else
		return new PropertyDescriptor(id, name);

}
protected PropertyDescriptor createTextPropertyDescriptor(
	String id,
	String name) {
	if (isEditable())
		return new ModifiedTextPropertyDescriptor(id, name);
	else
		return new PropertyDescriptor(id, name);

}
protected Object getNonzeroValue(Object value) {
	if (value!=null) return value;
	return "";
}
public java.lang.Object getSourceObject() {
	return sourceObject;
}
public boolean isEditable() {
	ISchemaObject schemaObject = (ISchemaObject)getSourceObject();
	return schemaObject.getSchema().isEditable();
}
public void setSourceObject(java.lang.Object newSourceObject) {
	sourceObject = newSourceObject;
}
protected IPropertyDescriptor[] toDescriptorArray(Vector result) {
	IPropertyDescriptor [] array = new IPropertyDescriptor[result.size()];
	result.copyInto(array);
	return array;
}
}
