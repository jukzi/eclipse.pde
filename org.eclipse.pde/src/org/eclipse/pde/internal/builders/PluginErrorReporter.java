package org.eclipse.pde.internal.builders;

import org.eclipse.core.resources.*;
import org.w3c.dom.*;


public class PluginErrorReporter extends PluginBaseErrorReporter {

	public PluginErrorReporter(IFile file) {
		super(file);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.builders.PluginBaseErrorReporter#validateTopLevelAttributes(org.w3c.dom.Element)
	 */
	protected void validateTopLevelAttributes(Element element) {
		super.validateTopLevelAttributes(element);
		Attr attr = element.getAttributeNode("class");
		if (attr != null)
			validateJavaAttribute(element, attr);
	}
	
	protected String getRootElementName() {
		return "plugin";
	}

}
