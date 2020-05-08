 
 package edu.wustl.common.querysuite.metadata.category;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 
 /**
  * @version 1.0
  * @created 28-Dec-2006 2:01:13 AM
  * 
  * @hibernate.joined-subclass table="CATEGORY"
  * @hibernate.joined-subclass-key column="ID" 
  */
 public class Category extends AbstractCategory<CategorialClass, Category> implements Serializable
 {
 
 	private static final long serialVersionUID = -4899537928159969695L;
 
 	private Long deEntityId;
 
 	private EntityInterface categoryEntity;
 
 	/**
 	 * @return the categoryEntity
 	 */
 	public EntityInterface getCategoryEntity()
 	{
 		return categoryEntity;
 	}
 
 	/**
 	 * @param categoryEntity
 	 *            the categoryEntity to set
 	 */
 	public void setCategoryEntity(EntityInterface categoryEntity)
 	{
 		this.categoryEntity = categoryEntity;
 	}
 
 	/**
 	 * @return the deEntityId
 	 * 
 	 * @hibernate.property name="deEntityId" type="long" length="30" column="DE_ENTITY_ID"
 	 */
 	public Long getDeEntityId()
 	{
 		return deEntityId;
 	}
 
 	/**
 	 * @param deEntityId
 	 *            the deEntityId to set
 	 */
 	public void setDeEntityId(Long deEntityId)
 	{
 		this.deEntityId = deEntityId;
 	}
 	
 	/**
 	 * @return the rootClass
 	 * 
 	 * @hibernate.many-to-one column="ROOT_CLASS_ID" class="edu.wustl.common.querysuite.metadata.category.CategorialClass" unique="true" cascade="all" lazy="false"
 	 */
 	public CategorialClass getRootClass()
 	{
 		return rootClass;
 		
 	}
 
 }
