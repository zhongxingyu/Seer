 
 package edu.wustl.common.querysuite.metadata.category;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 
 /**
  * @version 1.0
  * @created 28-Dec-2006 2:01:13 AM
  * @hibernate.class table="ABSTRACT_CATEGORIAL_ATTRIBUTE"
  */
 public abstract class AbstractCategorialAttribute<C extends AbstractCategorialClass>
 		implements
 			Serializable
 {
 
 	private static final long serialVersionUID = 3284285745433302355L;
 
 	protected Long id;
 
 	protected Long deSourceClassAttributeId;
 
 	protected C categorialClass;
 
 	protected AttributeInterface sourceClassAttribute;
 
 	/**
 	 * @return Returns the categorialClass.
 	 * @hibernate.many-to-one column="CATEGORIAL_CLASS_ID" class="edu.wustl.common.querysuite.metadata.category.AbstractCategorialClass" constrained="true"
 	 */
 	public C getCategorialClass()
 	{
 		return categorialClass;
 	}
 
 	/**
 	 * @param categorialClass The categorialClass to set.
 	 */
 	public void setCategorialClass(C categorialEntity)
 	{
 		this.categorialClass = categorialEntity;
 	}
 
 	/**
 	 * @return Returns the deSourceClassAttribute.
 	 * @hibernate.property name="deSourceClassAttributeId" type="long" length="30" column="DE_SOURCE_CLASS_ATTRIBUTE_ID"
 	 */
 	public Long getDeSourceClassAttributeId()
 	{
 		return deSourceClassAttributeId;
 	}
 
 	/**
 	 * @param deSourceClassAttributeId The deSourceClassAttribute to set.
 	 */
 	public void setDeSourceClassAttributeId(Long deSourceClassAttributeId)
 	{
 		this.deSourceClassAttributeId = deSourceClassAttributeId;
 	}
 
 	/**
 	 * @return Returns the id.
 	 * 
 	 * @hibernate.id name="id" column="ID" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="ABSTRACT_CATEGORIAL_ATTRIBUTE_SEQ"
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * @param id The id to set.
 	 */
 	public void setId(Long id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * @return Returns the sourceClassAttribute.
 	 */
 	public AttributeInterface getSourceClassAttribute()
 	{
 		return sourceClassAttribute;
 	}
 
 	/**
 	 * @param sourceClassAttribute The sourceClassAttribute to set.
 	 */
 	public void setSourceClassAttribute(AttributeInterface sourceClassAttribute)
 	{
 		this.sourceClassAttribute = sourceClassAttribute;
 	}
 
 }
