package org.eclipse.pde.internal.base.model;

/**
 * @see IModelProviderEvent
 */
public class ModelProviderEvent implements IModelProviderEvent {
	private int type;
	private IModel model;
/**
 * The constructor.
 * @param the event type
 * @param the changed model
 */
public ModelProviderEvent(int type, IModel model) {
	this.type = type;
	this.model = model;
}
/**
 * @see IModelProviderEvent#getAffectedModel
 */
public IModel getAffectedModel() {
	return model;
}
/**
 * @see IModelProviderEvent#getEventType
 */
public int getEventType() {
	return type;
}
}
