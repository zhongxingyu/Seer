 
 package edu.wustl.common.querysuite.metadata.category;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 
 /**
  * @author deepak_shingan
  *
  * @hibernate.joined-subclass table="CATEGORIAL_ATTRIBUTE"
  * @hibernate.joined-subclass-key column="ID" 
  */
 public class CategorialAttribute extends AbstractCategorialAttribute implements Serializable
 {
 
	/**
	 * added default serial version
	 */
	private static final long serialVersionUID = 1L;
	
 	private Long deCategoryAttributeId;
 
 	private AttributeInterface categoryAttribute;
 
 	/**
 	 * @return the categoryAttribute
 	 */
 	public AttributeInterface getCategoryAttribute()
 	{
 		return categoryAttribute;
 	}
 
 	/**
 	 * @param categoryAttribute the categoryAttribute to set
 	 */
 	public void setCategoryAttribute(AttributeInterface categoryAttribute)
 	{
 		this.categoryAttribute = categoryAttribute;
 	}
 
 	/**
 	 * @return the deCategoryAttributeId
 	 * 
 	 * @hibernate.property name="deCategoryAttributeId" type="long" length="30" column="DE_CATEGORY_ATTRIBUTE_ID"
 	 */
 	public Long getDeCategoryAttributeId()
 	{
 		return deCategoryAttributeId;
 	}
 
 	/**
 	 * @param deCategoryAttributeId the deCategoryAttributeId to set
 	 */
 	public void setDeCategoryAttributeId(Long deCategoryAttributeId)
 	{
 		this.deCategoryAttributeId = deCategoryAttributeId;
 	}
 
 }
