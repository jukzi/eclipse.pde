package org.eclipse.pde.internal.base.schema;

/**
 * A base type interface. Schema type is associated
 * with schema elements and attributes to define
 * their grammar and/or valid value space.
 * For simple types, 'getName()' method
 * returns name of the type that defines
 * initial value space (for example, "string", "boolean" etc.).
 */
public interface ISchemaType {
/**
 * Returns the logical name of this type.
 * @return name of the type
 */ 
public String getName();
/**
 * Returns the schema object in which this type is defined.
 * @return the top-level schema object
 */
public ISchema getSchema();
}
