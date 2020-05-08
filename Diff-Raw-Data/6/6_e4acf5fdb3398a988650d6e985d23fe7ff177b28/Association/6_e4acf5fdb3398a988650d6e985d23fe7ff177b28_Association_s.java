 
 package edu.common.dynamicextensions.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import edu.common.dynamicextensions.domain.databaseproperties.ConstraintProperties;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 
 /**
  * An entity can have multiple associations, where each association is linked to another entity.
  * This Class represents the Association between the Entities.
  * @version 1.0
  * @created 28-Sep-2006 12:20:06 PM
  * @hibernate.joined-subclass table="DYEXTN_ASSOCIATION"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  * @hibernate.cache  usage="read-write"
  */
 public class Association extends AbstractAttribute implements AssociationInterface,AssociationMetadataInterface
 {
 
 	/**
 	 * Serial Version Unique Identifief
 	 */
 	private static final long serialVersionUID = 1234567890L;
 
 	/**
 	 * Direction of the association.
 	 */
 	protected String direction;
 
 	/**
 	 * Source role of association.This specifies how the source entity is related to target entity.
 	 */
 	protected RoleInterface sourceRole;
 
 	/**
 	 * Target role of association.This specifies how the target entity is related to source entity.
 	 */
 	protected RoleInterface targetRole;
 
 	/**
 	 * The target entity of this association.
 	 */
 	protected EntityInterface targetEntity;
 
 	/**
 	 * Constraint properties related to this association.
 	 */
 	public Collection<ConstraintPropertiesInterface> constraintPropertiesCollection = new HashSet<ConstraintPropertiesInterface>();
 
 	/**
 	 * Specifies whether this abstarct attribute is a collection or not.
 	 */
 	protected Boolean isCollection = false;
 	/**
 	 *
 	 */
 	protected Boolean isSystemGenerated = false;
 
 	/**
 	 * Empty Constructor.
 	 */
 	public Association()
 	{
 	}
 	/**
 	 * This method returns whether the Attribute is a Collection or not.
 	 * @hibernate.property name="isCollection" type="boolean" column="IS_COLLECTION"
 	 * @return whether the Attribute is a Collection or not.
 	 */
 	public Boolean getIsCollection()
 	{
 		return isCollection;
 	}
 
 	/**
 	 * This method sets whether the Attribute is a Collection or not.
 	 * @param isCollection the Boolean value to be set.
 	 */
 	public void setIsCollection(Boolean isCollection)
 	{
 		this.isCollection = isCollection;
 	}
 	/**
 	 * This method returns the direction of the Association.
 	 * @hibernate.property name="direction" type="string" column="DIRECTION"
 	 * @return the direction of the Association
 	 */
 	private String getDirection()
 	{
 		return direction;
 	}
 
 	/**
 	 * This method sets the direction of the Association.
 	 * @param direction the direction of the Association to be set.
 	 */
 	private void setDirection(String direction)
 	{
 
 		this.direction = direction;
 	}
 
 	/**
 	 * @return
 	 * @hibernate.many-to-one cascade="save-update" column="TARGET_ENTITY_ID" class="edu.common.dynamicextensions.domain.Entity" constrained="true"
 	 */
 	public EntityInterface getTargetEntity()
 	{
 
 		return targetEntity;
 	}
 
 	/**
 	 * This method sets the target Entity of the Association to the given Entity.
 	 * @param targetEntity the Entity to be set as target Entity of the Association.
 	 */
 	public void setTargetEntity(EntityInterface targetEntity)
 	{
 
 		this.targetEntity = targetEntity;
 	}
 
 	/**
 	 * This method returns the source Role of the Association.
 	 * @return the source Role of the Association.
 	 * @hibernate.many-to-one  cascade="save-update" column="SOURCE_ROLE_ID" class="edu.common.dynamicextensions.domain.Role" constrained="true"
 	 */
 	public RoleInterface getSourceRole()
 	{
 		return sourceRole;
 	}
 
 	/**
 	 * This method sets the source Role of the Association.
 	 * @param sourceRole the Role to be set as source Role.
 	 */
 	public void setSourceRole(RoleInterface sourceRole)
 	{
 		this.sourceRole = sourceRole;
 	}
 
 	/**
 	 * This method returns the targetRole of the Association.
 	 * @return the targetRole of the Association.
 	 * @hibernate.many-to-one cascade="save-update" column="TARGET_ROLE_ID" class="edu.common.dynamicextensions.domain.Role" constrained="true"
 	 */
 
 	public RoleInterface getTargetRole()
 	{
 		return targetRole;
 	}
 
 	/**
 	 * This method sets the target Role of the Association.
 	 * @param targetRole the Role to be set as targetRole of the Association.
 	 */
 	public void setTargetRole(RoleInterface targetRole)
 	{
 
 		this.targetRole = targetRole;
 	}
 
 	/**
 	 * This method returns the Collection of the ConstraintProperties of the Association.
 	 *
 	 * @hibernate.set name="constraintPropertiesCollection" table="DYEXTN_CONSTRAINT_PROPERTIES"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="ASSOCIATION_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.databaseproperties.ConstraintProperties"
 	 *
 	 * @return the Collection of the ConstraintProperties of the Association.
 	 */
 	private Collection<ConstraintPropertiesInterface> getConstraintPropertiesCollection()
 	{
 		return constraintPropertiesCollection;
 	}
 
 	/**
 	 * This method sets constraintPropertiesCollection to the given Collection of the ConstraintProperties.
 	 * @param constraintPropertiesCollection The constraintPropertiesCollection to set.
 	 */
 	private void setConstraintPropertiesCollection(
 			Collection<ConstraintPropertiesInterface> constraintPropertiesCollection)
 	{
 		this.constraintPropertiesCollection = constraintPropertiesCollection;
 	}
 
 	/**
 	 * This method returns the ConstraintProperties of the Association.
 	 * @return the ConstraintProperties of the Association.
 	 */
 	public ConstraintPropertiesInterface getConstraintProperties()
 	{
 		ConstraintPropertiesInterface contraintProperties = null;
 		if (constraintPropertiesCollection != null && !constraintPropertiesCollection.isEmpty())
 		{
 			Iterator constraintPropertiesIterator = constraintPropertiesCollection.iterator();
 			contraintProperties = (ConstraintPropertiesInterface) constraintPropertiesIterator
 					.next();
 		}
 		return contraintProperties;
 	}
 
 	/**
 	 * This method sets the constraintProperties to the given ContraintProperties.
 	 * @param constraintProperties the constraintProperties to be set.
 	 */
 	public void setConstraintProperties(ConstraintPropertiesInterface constraintProperties)
 	{
 		if (constraintPropertiesCollection == null)
 		{
 			constraintPropertiesCollection = new HashSet<ConstraintPropertiesInterface>();
 		}
 		else
 		{
 			constraintPropertiesCollection.clear();
 		}
 		this.constraintPropertiesCollection.add(constraintProperties);
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.domaininterface.AssociationInterface#getAssociationDirection()
 	 */
 	public AssociationDirection getAssociationDirection()
 	{
 		return AssociationDirection.get(getDirection());
 	}
 
 	/**
 	 * @throws DynamicExtensionsApplicationException
 	 * @see edu.common.dynamicextensions.domaininterface.AssociationInterface#setAssociationDirection(edu.common.dynamicextensions.util.global.Constants.AssociationDirection)
 	 */
 	public void setAssociationDirection(AssociationDirection direction)
 	{
 		setDirection(direction.getValue());
 		populateSystemGeneratedAssociation(this);
 	}
 
 	/**
 	 * This method returns whether the Attribute is a Collection or not.
 	 * @hibernate.property name="isSystemGenerated" type="boolean" column="IS_SYSTEM_GENERATED"
 	 * @return Returns the isSystemGenerated.
 	 */
 	public Boolean getIsSystemGenerated()
 	{
 		return isSystemGenerated;
 	}
 
 	/**
 	 * @param isSystemGenerated The isSystemGenerated to set.
 	 */
 	public void setIsSystemGenerated(Boolean isSystemGenerated)
 	{
 		this.isSystemGenerated = isSystemGenerated;
 	}
     /**This method is used for following purposes.
      * 1. The method creates a system generated association in case when the association is bidirectional.
      * Bi directional association is supposed to be a part of the target entity's attributes.
      * So we create a replica of the original association (which we call as system generated association)
      * and this association is added to the target entity's attribute collection.
      * 2. The method also removes the system generated association from the target entity
      * when the association direction of the assciation is changed from "bi-directional" to "SRC-Destination".
      * In this case we no longer need the system generated association.
      * So if the sys. generated association is present , it is removed.
      * @param association
      */
     private void populateSystemGeneratedAssociation(Association association)
     {
         //Getting the sys.generated association for the given original association.
         if (association.getIsSystemGenerated())
         {
             return;
         }
         else
         {
             Association systemGeneratedAssociation = getSystemGeneratedAssociation(association);
             if (association.getAssociationDirection() == AssociationDirection.BI_DIRECTIONAL)
             {
                 ConstraintPropertiesInterface constraintPropertiesSysGen = new ConstraintProperties();
                 if (systemGeneratedAssociation == null)
                 {
                     systemGeneratedAssociation = new Association();
                 }
                 else
                 {
                     constraintPropertiesSysGen = systemGeneratedAssociation
                             .getConstraintProperties();
                 }
                 constraintPropertiesSysGen.setName(association.getConstraintProperties().getName());
                 //Swapping the source and target keys.
                 constraintPropertiesSysGen.setSourceEntityKey(association.getConstraintProperties()
                         .getTargetEntityKey());
                 constraintPropertiesSysGen.setTargetEntityKey(association.getConstraintProperties()
                         .getSourceEntityKey());
                 //Populating the sys. generated association.
                 //systemGeneratedAssociation.setName(association.getName());
 
                 //For XMI import, we can get self referencing bi directional associations. Hence creating unique name for the sys generated association.
                 systemGeneratedAssociation.setName(association.getName()
                         + association.getEntity().getId());
 
                 systemGeneratedAssociation.setDescription(association.getDescription());
                 systemGeneratedAssociation.setTargetEntity(association.getEntity());
                 systemGeneratedAssociation.setEntity(association.getTargetEntity());
                 //Swapping the source and target roles.
                 systemGeneratedAssociation.setSourceRole(association.getTargetRole());
                 systemGeneratedAssociation.setTargetRole(association.getSourceRole());
                 systemGeneratedAssociation.setIsSystemGenerated(true);
                 systemGeneratedAssociation
                         .setAssociationDirection(AssociationDirection.BI_DIRECTIONAL);
 
                 systemGeneratedAssociation.setConstraintProperties(constraintPropertiesSysGen);
 
                 for (TaggedValueInterface taggedValue : association.getTaggedValueCollection())
                 {
                     systemGeneratedAssociation.addTaggedValue(((TaggedValue) taggedValue).clone());
                 }
 
                 //Adding the sys.generated association to the target entity.
                 association.getTargetEntity().addAbstractAttribute(systemGeneratedAssociation);
             }
             else
             {
                 //Removing the not required sys. generated association because the direction has been changed
                 //from "bi directional" to "src-destination".
                 if (systemGeneratedAssociation != null)
                 {
                     association.getTargetEntity().removeAbstractAttribute(
                             systemGeneratedAssociation);
                 }
             }
         }
     }
     /**This method is used to get the system generated association given the original association.
      * System generated association is searched based on the following criteria
      * 1. The flag "isSystemGenerated" should be set.
      * 2. The source and target roles are swapped. So original association's source role should be the target role
      * of the sys.generated association and vice versa.
      * @param association
      * @return
      */
     private Association getSystemGeneratedAssociation(Association association)
     {
     	Association associationInterface = null;
         EntityInterface targetEnetity = association.getTargetEntity();
         Collection associationCollection = targetEnetity.getAssociationCollection();
         if (associationCollection != null && !associationCollection.isEmpty())
         {
             Iterator associationIterator = associationCollection.iterator();
             while (associationIterator.hasNext())
             {
                 associationInterface = (Association) associationIterator.next();
                 if (associationInterface.getIsSystemGenerated()
                         && associationInterface.getSourceRole().equals(association.getTargetRole())
                         && associationInterface.getTargetRole().equals(association.getSourceRole()))
                 {
                     break;
                 }
             }
         }
        return associationInterface;
     }
 
 	public AssociationType getAssociationType()
 	{
 		RoleInterface roleInterface = this.getTargetRole();
 		return roleInterface.getAssociationsType();
 	}
 
 
 }
