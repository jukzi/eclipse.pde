package org.eclipse.pde.internal.model.component;

import org.w3c.dom.Node;
import java.io.*;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.internal.base.model.component.*;

public class ComponentPlugin extends ComponentReference implements IComponentPlugin {

public void write(String indent, PrintWriter writer) {
	writer.print(indent + "<plugin");
	String indent2 = indent + Component.INDENT + Component.INDENT;
	if (getId() != null) {
		writer.println();
		writer.print(indent2 + "id=\"" + getId() + "\"");
	}
	if (getLabel() != null) {
		writer.println();
		writer.print(indent2 + "label=\"" + getLabel() + "\"");
	}
	if (getVersion() != null) {
		writer.println();
		writer.print(indent2 + "version=\"" + getVersion() + "\"");
	}
	writer.println(">");
	writer.println(indent + "</plugin>");
}
}
