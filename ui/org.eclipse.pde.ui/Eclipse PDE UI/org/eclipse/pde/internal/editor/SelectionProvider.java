package org.eclipse.pde.internal.editor;

import org.eclipse.jface.viewers.*;
import java.util.*;

public class SelectionProvider implements org.eclipse.jface.viewers.ISelectionProvider {
	private Vector listeners = new Vector();
	private ISelection selection;

public SelectionProvider() {
}
public void addSelectionChangedListener(ISelectionChangedListener listener) {
	listeners.addElement(listener);
}
public ISelection getSelection() {
	return selection;
}
public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	listeners.removeElement(listener);
	//listener.selectionChanged(new SelectionChangedEvent(this, new StructuredSelection()));
}
public synchronized void setSelection(ISelection selection) {
	this.selection = selection;
	SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
	for (Iterator iter = listeners.iterator(); iter.hasNext();) {
		ISelectionChangedListener listener = (ISelectionChangedListener)iter.next();
		listener.selectionChanged(event);
	}
}
}
