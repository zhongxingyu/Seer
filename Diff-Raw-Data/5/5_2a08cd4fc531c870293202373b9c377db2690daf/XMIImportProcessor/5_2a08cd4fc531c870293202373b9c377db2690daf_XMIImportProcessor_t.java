 
 package edu.common.dynamicextensions.xmi.importer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.omg.uml.UmlPackage;
 import org.omg.uml.foundation.core.AssociationEnd;
 import org.omg.uml.foundation.core.Attribute;
 import org.omg.uml.foundation.core.Generalization;
 import org.omg.uml.foundation.core.TaggedValue;
 import org.omg.uml.foundation.core.UmlAssociation;
 import org.omg.uml.foundation.core.UmlClass;
 import org.omg.uml.foundation.datatypes.Multiplicity;
 import org.omg.uml.foundation.datatypes.MultiplicityRange;
 import org.omg.uml.modelmanagement.Model;
 import org.omg.uml.modelmanagement.ModelClass;
 import org.omg.uml.modelmanagement.ModelManagementPackage;
 
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ByteArrayAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domain.userinterface.SelectControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ApplyFormControlsProcessor;
 import edu.common.dynamicextensions.processor.ContainerProcessor;
 import edu.common.dynamicextensions.processor.EntityProcessor;
 import edu.common.dynamicextensions.processor.LoadFormControlsProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlConfigurationsFactory;
 import edu.common.dynamicextensions.ui.util.RuleConfigurationObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.xmi.XMIConstants;
 import edu.common.dynamicextensions.xmi.XMIUtilities;
 import edu.common.dynamicextensions.xmi.exporter.DatatypeMappings;
 import edu.common.dynamicextensions.xmi.model.ContainerModel;
 import edu.common.dynamicextensions.xmi.model.ControlsModel;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.util.dbManager.DAOException;
 
 /**
  * 
  * @author sujay_narkar
  * @author ashish_gupta
  *
  */
 public class XMIImportProcessor
 {
 	public boolean isEditedXmi = false;
 	/**
 	 * Instance of Domain object factory, which will be used to create  dynamic extension's objects.
 	 */
 	protected static DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 
 	/**
 	 * Map with KEY : UML id of a class(coming from domain model) VALUE : dynamic extension Entity created for this UML class.  
 	 */
 	protected Map<String, EntityInterface> umlClassIdVsEntity;
 
 	/**
 	 * Saved entity group created by this class
 	 */
 	private EntityGroupInterface entityGroup;
 
 	/**
 	 * Map for storing containers corresponding to entities
 	 */
 	protected Map<String, List<ContainerInterface>> entityNameVsContainers = new HashMap<String, List<ContainerInterface>>();
 	/**
 	 * List for retrieved containers corresponding to entity group.
 	 */
 	private Collection<ContainerInterface> retrievedContainerList = new ArrayList<ContainerInterface>();
 	
 	private List<ContainerInterface> mainContainerList = new ArrayList<ContainerInterface>();
 
 	/**
 	 * Default constructor
 	 *
 	 */
 	public XMIImportProcessor()
 	{
 		super();
 	}
 	public List<ContainerInterface> processXmi(UmlPackage umlPackage, String entityGroupName, String packageName, List<String> containerNames) throws Exception
 	{
 		List<UmlClass> umlClassColl = new ArrayList<UmlClass>();
 		List<UmlAssociation> umlAssociationColl = new ArrayList<UmlAssociation>();
 		List<Generalization> umlGeneralisationColl = new ArrayList<Generalization>();
 	
 		processModel(umlPackage, umlClassColl, umlAssociationColl, umlGeneralisationColl,packageName);
 		
 		List<EntityGroupInterface> entityGroupColl = retrieveEntityGroup(entityGroupName,packageName);
 		
 		if (entityGroupColl == null || entityGroupColl.size() == 0)
 		{//Add
 			entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 			if(packageName.equals(""))
 			{
 				setEntityGroupName(entityGroupName);
 			}
 			else
 			{
 				setEntityGroupName(packageName);
 			}
 			entityGroup.setIsSystemGenerated(false);
 		}
 		else
 		{//Edit
 			isEditedXmi = true;
 			entityGroup = entityGroupColl.get(0);
 		}
 	
 		int noOfClasses = umlClassColl.size();
 		umlClassIdVsEntity = new HashMap<String, EntityInterface>(noOfClasses);
 	
 		//Creating entities and entity group.
 		for (UmlClass umlClass : umlClassColl)
 		{
 			EntityInterface entity = entityGroup.getEntityByName(umlClass.getName());
 			if (entity == null)
 			{//Add
 				entity = createEntity(umlClass);
 				entity.addEntityGroupInterface(entityGroup);
 				entityGroup.addEntity(entity);
 			}
 			else
 			{//Edit
 				Collection<Attribute> attrColl = XMIUtilities.getAttributes(umlClass, false);
 				createAttributes(attrColl, entity);
 			}
 			umlClassIdVsEntity.put(umlClass.refMofId(), entity);
 		}
 	
 		Map<String, List<String>> parentIdVsChildrenIds = new HashMap<String, List<String>>();
 	
 		if (umlGeneralisationColl.size() > 0)
 		{
 			parentIdVsChildrenIds = getParentVsChildrenMap(umlGeneralisationColl);
 		}
 	
 		if (umlAssociationColl != null)
 		{
 			for (UmlAssociation umlAssociation : umlAssociationColl)
 			{
 				addAssociation(umlAssociation, parentIdVsChildrenIds);
 			}
 		}
 		if (umlGeneralisationColl.size() > 0)
 		{
 			processInheritance(parentIdVsChildrenIds);
 			//			markInheritedAttributes(entityGroup);
 		}
 	
 		//Retriving all containers corresponding to the given entity group.
 		if (entityGroup.getId() != null)
 		{
 			retrievedContainerList = EntityManager.getInstance().getAllContainersByEntityGroupId(
 					entityGroup.getId());
 		}
 	
 		for (UmlClass umlClass : umlClassColl)
 		{
 			EntityInterface entity = umlClassIdVsEntity.get(umlClass.refMofId());
 			//In memory operation
 			createContainer(entity);
 		}
 		if (umlGeneralisationColl.size() > 0)
 		{//setting base container in child container.
 			postProcessInheritence(parentIdVsChildrenIds);
 		}
 		if (umlAssociationColl.size() > 0)
 		{//Adding container for containment control
 			postProcessAssociation();
 		}
 		//Persist container in DB
 		processPersistence(containerNames);
 		
 		return mainContainerList;
 	}
 
 	/**
 	 * @param entityGroupName
 	 * @param packageName
 	 * @return
 	 * @throws DAOException
 	 */
 	private List<EntityGroupInterface> retrieveEntityGroup(String entityGroupName, String packageName) throws DAOException
 	{
 		List<EntityGroupInterface> entityGroupColl = null;
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		if(packageName.equals(""))
 		{
 			entityGroupColl = defaultBizLogic.retrieve(EntityGroup.class
 					.getName(), edu.common.dynamicextensions.ui.util.Constants.NAME, entityGroupName);
 		}
 		else
 		{
 			entityGroupColl = defaultBizLogic.retrieve(EntityGroup.class
 					.getName(), edu.common.dynamicextensions.ui.util.Constants.NAME, packageName);
 		}
 		return entityGroupColl;
 	}
 	/**
 	 * @param entityGroupName
 	 */
 	private void setEntityGroupName(String entityGroupName)
 	{
 		entityGroup.setShortName(entityGroupName);
 		entityGroup.setName(entityGroupName);
 		entityGroup.setLongName(entityGroupName);
 		entityGroup.setDescription(entityGroupName);
 	}
 	/**
 	 * @param entityGroupName
 	 * @return
 	 * @throws DAOException
 	 * @throws ClassNotFoundException
 	 */
 	//	private List<ContainerInterface> retrieveAllContainers(String entityGroupName) throws DAOException, ClassNotFoundException
 	//	{
 	//		HibernateDAO dao = (HibernateDAO) DAOFactory.getInstance().getDAO(Constants.HIBERNATE_DAO);
 	//		dao.openSession(null);
 	//
 	//		String hql ="Select c from " + Container.class.getName() + " c join c.entity.entityGroupCollection as eg where eg.name = '"+ entityGroupName +"'";
 	//		List<ContainerInterface> list = dao.executeQuery(hql, null, false, null);
 	//		
 	//		dao.closeSession();	
 	//		return list;
 	//	}
 	/**
 	 * This method checks the entity name if it matches with the data type names like Integer, String etc.
 	 * @param umlClassName
 	 * @return
 	 */
 	private boolean checkEntityWithDataTypeEntities(String umlClassName)
 	{
 		DatatypeMappings dataType = DatatypeMappings.get(umlClassName);
 		if (dataType == null)
 		{
 			return false;
 		}
 		if(umlClassName.equalsIgnoreCase(edu.common.dynamicextensions.ui.util.Constants.COLLECTION) ||
 			umlClassName.equalsIgnoreCase(edu.common.dynamicextensions.ui.util.Constants.DATE) ||
 			umlClassName.equalsIgnoreCase(edu.common.dynamicextensions.ui.util.Constants.TIME))
 		{
 			return true;
 		}
 		return true;
 	}
 
 	/**
 	 * @param umlPackage
 	 * @param umlClassColl
 	 * @param umlAssociationColl
 	 * @param umlGeneralisationColl
 	 */
 	private void processModel(UmlPackage umlPackage, List<UmlClass> umlClassColl,
 			List<UmlAssociation> umlAssociationColl, List<Generalization> umlGeneralisationColl, String packageName)
 	{
 		ModelManagementPackage modelManagementPackage = umlPackage.getModelManagement();
 		ModelClass modelClass = modelManagementPackage.getModel();
 		Collection<Model> modelColl = modelClass.refAllOfClass();
 
 		for (Model model : modelColl)
 		{
 			Collection ownedElementColl = model.getOwnedElement();
 			System.out.println("MODEL OWNED ELEMENT SIZE: " + ownedElementColl.size());
 			Iterator iter = ownedElementColl.iterator();
 			while (iter.hasNext())
 			{
 				Object obj = iter.next();
 				if (obj instanceof org.omg.uml.modelmanagement.UmlPackage)
 				{
 					org.omg.uml.modelmanagement.UmlPackage umlPackageObj = (org.omg.uml.modelmanagement.UmlPackage) obj;
 					processPackage(umlPackageObj, umlClassColl, umlAssociationColl,
 							umlGeneralisationColl , packageName);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param parentPkg
 	 * @param pkgName
 	 * @return
 	 */
 	private void processPackage(org.omg.uml.modelmanagement.UmlPackage parentPkg,
 			List<UmlClass> umlClasses, List<UmlAssociation> associations,
 			List<Generalization> generalizations ,String packageName)
 	{//TODO if package name is present, import only that package.
 		for (Iterator i = parentPkg.getOwnedElement().iterator(); i.hasNext();)
 		{
 			Object o = i.next();
 			if (o instanceof org.omg.uml.modelmanagement.UmlPackage && !(packageName.equals(parentPkg.getName())))
 			{
 				org.omg.uml.modelmanagement.UmlPackage subPkg = (org.omg.uml.modelmanagement.UmlPackage) o;
 				processPackage(subPkg, umlClasses, associations, generalizations,packageName);
 			}
 			else if (o instanceof UmlAssociation)
 			{
 				associations.add((UmlAssociation) o);
 			}
 			else if (o instanceof Generalization)
 			{
 				generalizations.add((Generalization) o);
 			}
 			else if (o instanceof UmlClass)
 			{
 				UmlClass umlClass = (UmlClass) o;
 				boolean isEntityADatatype = checkEntityWithDataTypeEntities(umlClass.getName());
 				if (isEntityADatatype)
 				{//Skipping classes having datatype names eg Integer,String etc.
 					continue;
 				}
 
 				Collection<Generalization> generalizationColl = umlClass.getGeneralization();
 				if (generalizationColl != null && generalizationColl.size() > 0)
 				{
 					generalizations.addAll(generalizationColl);
 				}
 				umlClasses.add(umlClass);
 			}
 		}
 	}
 
 	/**
 	 * Creates a Dynamic Exension Entity from given UMLClass.<br>
 	 * It also assigns all the attributes of the UMLClass to the Entity as the
 	 * Dynamic Extension Primitive Attributes.Then stores the input UML class,
 	 * adds the Dynamic Extension's PrimitiveAttributes to the Collection.
 	 * Properties which are copied from UMLAttribute to DE Attribute are
 	 * name,description,semanticMetadata,permissible values
 	 * @param umlClass
 	 *            The UMLClass from which to form the Dynamic Extension Entity
 	 * @return the unsaved entity for given UML class
 	 */
 	private EntityInterface createEntity(UmlClass umlClass)
 	{
 		String name = (umlClass.getName());
 		EntityInterface entity = deFactory.createEntity();
 		entity.setName(name);
 		entity.setDescription(umlClass.getName());
 		entity.setAbstract(umlClass.isAbstract());
 		Collection<Attribute> attrColl = XMIUtilities.getAttributes(umlClass, false);
 
 		createAttributes(attrColl, entity);
 
 		//		setSemanticMetadata(entity, umlClass.getSemanticMetadata());
 		return entity;
 	}
 
 	/**
 	 * @param attrColl
 	 * @param entity
 	 */
 	private void createAttributes(Collection<Attribute> attrColl, EntityInterface entity)
 	{
 		if (attrColl != null)
 		{
 			for (Attribute umlAttribute : attrColl)
 			{//Not showing id attribute on UI
 				if (!(umlAttribute.getName().equalsIgnoreCase(Constants.ID) || umlAttribute
 						.getName().equalsIgnoreCase(Constants.IDENTIFIER)))
 				{
 					DataType dataType = DataType.get(umlAttribute.getType().getName());
 					if (dataType != null)
 					{//Temporary solution for unsupported datatypes. Not adding attributes having unsupported datatypes.
 
 						Collection<AttributeInterface> originalAttrColl = entity
 								.getAttributeCollection();
 
 						AttributeInterface originalAttribute = getAttributeByName(umlAttribute
 								.getName(), originalAttrColl);
 						if (originalAttribute == null)
 						{
 							AttributeInterface attribute = dataType.createAttribute(umlAttribute);
 							if (attribute != null)
 							{ // to bypass attributes of invalid datatypes
 								attribute.setName(umlAttribute.getName());
 								//					attribute.setDescription(umlAttribute.getTaggedValue().getDescription());
 								//					setSemanticMetadata(attribute, umlAttribute.getSemanticMetadata());
 								entity.addAttribute(attribute);
 							}
 						}
 					}
 					//				else
 					//				{//Temporary solution for unsupported datatypes. Not adding attributes having unsupported datatypes.
 					//					throw new DynamicExtensionsApplicationException("File contains Unsupported DataType");
 					//				}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param attrName
 	 * @param originalAttrColl
 	 * @return
 	 */
 	private AttributeInterface getAttributeByName(String attrName,
 			Collection<AttributeInterface> originalAttrColl)
 	{
 		if (originalAttrColl != null && originalAttrColl.size() > 0)
 		{
 			for (AttributeInterface originalAttr : originalAttrColl)
 			{
 				if (originalAttr.getName().equals(attrName))
 				{
 					return originalAttr;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gives a map having parent child information.
 	 * @return Map with key as UML-id of parent class and value as list of UML-id of all children classes.
 	 */
 	private Map<String, List<String>> getParentVsChildrenMap(
 			List<Generalization> umlGeneralisationColl)
 	{
 		if (umlGeneralisationColl != null)
 		{
 			HashMap<String, List<String>> parentIdVsChildrenIds = new HashMap<String, List<String>>(
 					umlGeneralisationColl.size());
 			for (Generalization umlGeneralization : umlGeneralisationColl)
 			{
 				String childClass = umlGeneralization.getChild().refMofId();
 				String parentClass = umlGeneralization.getParent().refMofId();
 				List<String> children = parentIdVsChildrenIds.get(parentClass);
 				if (children == null)
 				{
 					children = new ArrayList<String>();
 					parentIdVsChildrenIds.put(parentClass, children);
 				}
 				children.add(childClass);
 			}
 			return parentIdVsChildrenIds;
 		}
 
 		return new HashMap<String, List<String>>(0);
 	}
 
 	/**
 	 * Converts the UML association to dynamic Extension Association.Adds it to the entity group.
 	 * It replicates this association in all children of source and all children of target class.
 	 * It taggs replicated association to identify them later on and mark them inherited. 
 	 * Also a back pointer is added to replicated association go get original association.
 	 * @param umlAssociation umlAssociation to process
 	 * @param parentIdVsChildrenIds Map with key as UML-id of parent class and value as list of UML-id of all children classes.
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void addAssociation(UmlAssociation umlAssociation,
 			Map<String, List<String>> parentIdVsChildrenIds) throws DynamicExtensionsSystemException
 	{
 		List<AssociationEnd> associationEnds = umlAssociation.getConnection();
 
 		AssociationEnd sourceAssociationEnd = null;
 		AssociationEnd targetAssociationEnd = null;
 
 		Collection<TaggedValue> taggedValueColl = umlAssociation.getTaggedValue();
 		String direction = getTaggedValue(taggedValueColl, XMIConstants.TAGGED_NAME_ASSOC_DIRECTION);
 
 		int nullAssoEnd = 0;
 		for (AssociationEnd assoEnd : associationEnds)
 		{
 			if (assoEnd.getName() == null)
 			{
 				nullAssoEnd++;
 			}
 		}
 
 		if (nullAssoEnd > 0 && nullAssoEnd < 2)
 		{//Only 1 name is present hence unidirectional
 			for (AssociationEnd assoEnd : associationEnds)
 			{
 				if (assoEnd.getName() == null)
 				{
 					sourceAssociationEnd = assoEnd;
 				}
 				else
 				{
 					targetAssociationEnd = assoEnd;
 				}
 			}
 			if (direction.equals("")
 					|| direction.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_ASSOC_DEST_SRC)
 					|| direction.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_ASSOC_SRC_DEST)
 					|| direction
 							.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_CONTAINMENT_UNSPECIFIED))
 			{
 				direction = Constants.AssociationDirection.SRC_DESTINATION.toString();
 			}
 		}
 		else
 		{//bidirectional
 			sourceAssociationEnd = associationEnds.get(0);
 			targetAssociationEnd = associationEnds.get(1);
 			if (direction.equals("")
 					|| direction.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_ASSOC_BIDIRECTIONAL))
 			{
 				direction = Constants.AssociationDirection.BI_DIRECTIONAL.toString();
 			}
 			else
 			{
 				direction = Constants.AssociationDirection.SRC_DESTINATION.toString();
 			}
 		}
 		//		getAssociationEnds(sourceAssociationEnd,targetAssociationEnd,associationEnds,direction);
 
 		String sourceAssoTypeTV = getTaggedValue(sourceAssociationEnd.getTaggedValue(),
 				XMIConstants.TAGGED_VALUE_CONTAINMENT);
 		String destinationAssoTypeTV = getTaggedValue(targetAssociationEnd.getTaggedValue(),
 				XMIConstants.TAGGED_VALUE_CONTAINMENT);
 
 		String srcId = sourceAssociationEnd.getParticipant().refMofId();
 		EntityInterface srcEntity = umlClassIdVsEntity.get(srcId);
 
 		Multiplicity srcMultiplicity = sourceAssociationEnd.getMultiplicity();
 		String sourceRoleName = sourceAssociationEnd.getName();
 		RoleInterface sourceRole = getRole(srcMultiplicity, sourceRoleName, sourceAssoTypeTV);
 
 		String tgtId = targetAssociationEnd.getParticipant().refMofId();
 		EntityInterface tgtEntity = umlClassIdVsEntity.get(tgtId);
 
 		Multiplicity tgtMultiplicity = targetAssociationEnd.getMultiplicity();
 		String tgtRoleName = targetAssociationEnd.getName();
 		RoleInterface targetRole = getRole(tgtMultiplicity, tgtRoleName, destinationAssoTypeTV);
 
 		AssociationInterface association = null;
 		Collection<AssociationInterface> existingAssociationColl = srcEntity
 				.getAssociationCollection();
 		if (existingAssociationColl != null && existingAssociationColl.size() > 0)
 		{//EDIT Case
 			association = isAssociationPresent(umlAssociation.getName(), existingAssociationColl,
 					srcEntity.getName(), tgtEntity.getName(), direction, sourceRole, targetRole);
 		}
 
 		//Adding association to entity
 		if (association == null)
 		{//Make new Association
 			association = getAssociation(srcEntity, umlAssociation.getName());
 		}
 
 		association.setSourceRole(sourceRole);
 		association.setTargetEntity(tgtEntity);
 		association.setTargetRole(targetRole);
 
 		if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL.toString()))
 		{
 			association.setAssociationDirection(Constants.AssociationDirection.BI_DIRECTIONAL);
 		}
 		else
 		{
 			association.setAssociationDirection(Constants.AssociationDirection.SRC_DESTINATION);
 		}
 	}
 
 	/**
 	 * Logic: 
 	 * 1. If association name is present and matches with an existing association name, edit that association
 	 * 2. If association name is not present, check for other matching parameters like  source entity name, target entity name,
 	 * 	  direction, source role and target role. 
 	 * 		a. If any one parameter does not match, make a new association
 	 * 		b. If all parameters match, association has not been edited.
 	 * 
 	 * @param umlAssociationName
 	 * @param existingAssociationColl
 	 * @param srcEntityName
 	 * @param tgtEntityName
 	 * @param direction
 	 * @param sourceRole
 	 * @param targetRole
 	 * @return
 	 */
 	private AssociationInterface isAssociationPresent(String umlAssociationName,
 			Collection<AssociationInterface> existingAssociationColl, String srcEntityName,
 			String tgtEntityName, String direction, RoleInterface sourceRole,
 			RoleInterface targetRole)
 	{
 		for (AssociationInterface existingAsso : existingAssociationColl)
 		{
 			if (umlAssociationName != null
 					&& umlAssociationName.equalsIgnoreCase(existingAsso.getName()))
 			{//Since name is present, edit this association
 				return existingAsso;
 			}
 			//If even 1 condition does not match, goto next association
 			if (!existingAsso.getEntity().getName().equalsIgnoreCase(srcEntityName))
 			{
 				continue;
 			}
 			if (!existingAsso.getTargetEntity().getName().equalsIgnoreCase(tgtEntityName))
 			{
 				continue;
 			}
 			if (!existingAsso.getAssociationDirection().toString().equalsIgnoreCase(direction))
 			{
 				continue;
 			}
 			//SourecRole
 			if (!existingAsso.getSourceRole().getAssociationsType().equals(
 					sourceRole.getAssociationsType()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (!existingAsso.getSourceRole().getAssociationsType().equals(
 							targetRole.getAssociationsType()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (!existingAsso.getSourceRole().getMaximumCardinality().equals(
 					sourceRole.getMaximumCardinality()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (!existingAsso.getSourceRole().getMaximumCardinality().equals(
 							targetRole.getMaximumCardinality()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (!existingAsso.getSourceRole().getMinimumCardinality().equals(
 					sourceRole.getMinimumCardinality()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (!existingAsso.getSourceRole().getMinimumCardinality().equals(
 							targetRole.getMinimumCardinality()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (existingAsso.getSourceRole().getName() != null
 					&& sourceRole.getName() != null
 					&& !existingAsso.getSourceRole().getName().equalsIgnoreCase(
 							sourceRole.getName()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (existingAsso.getSourceRole().getName() != null
 							&& targetRole.getName() != null
 							&& !existingAsso.getSourceRole().getName().equalsIgnoreCase(
 									targetRole.getName()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			//			TargetRole
 			if (!existingAsso.getTargetRole().getAssociationsType().equals(
 					targetRole.getAssociationsType()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (!existingAsso.getTargetRole().getAssociationsType().equals(
 							sourceRole.getAssociationsType()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (!existingAsso.getTargetRole().getMaximumCardinality().equals(
 					targetRole.getMaximumCardinality()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (!existingAsso.getTargetRole().getMaximumCardinality().equals(
 							sourceRole.getMaximumCardinality()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (!existingAsso.getTargetRole().getMinimumCardinality().equals(
 					targetRole.getMinimumCardinality()))
 			{//For bi directional association, reversing the association ends and comparing
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{
 					if (!existingAsso.getTargetRole().getMinimumCardinality().equals(
 							sourceRole.getMinimumCardinality()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			if (existingAsso.getTargetRole().getName() != null
 					&& targetRole.getName() != null
 					&& !existingAsso.getTargetRole().getName().equalsIgnoreCase(
 							targetRole.getName()))
 			{
 				if (direction.equalsIgnoreCase(Constants.AssociationDirection.BI_DIRECTIONAL
 						.toString()))
 				{//For bi directional association, reversing the association ends and comparing
 					if (existingAsso.getTargetRole().getName() != null
 							&& sourceRole.getName() != null
 							&& !existingAsso.getTargetRole().getName().equalsIgnoreCase(
 									sourceRole.getName()))
 					{
 						continue;
 					}
 				}
 				else
 				{
 					continue;
 				}
 			}
 			//All parameters match. Hence this Association has not been edited.
 			return existingAsso;
 		}
 		return null;
 	}
 
 	/**
 	 * @param taggedValueColl
 	 * @param tagName
 	 * @return
 	 */
 	private String getTaggedValue(Collection<TaggedValue> taggedValueColl, String tagName)
 	{
 		for (TaggedValue taggedValue : taggedValueColl)
 		{
 			if (taggedValue.getType() != null)
 			{
 				if (taggedValue.getType().getName().equalsIgnoreCase(tagName))
 				{
 					Collection<String> dataValueColl = taggedValue.getDataValue();
 					for (String value : dataValueColl)
 					{
 						return value;
 					}
 				}
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Processes inheritance relation ship present in domain model 
 	 * @param parentIdVsChildrenIds Map with key as UML-id of parent class and value as list of UML-id of all children classes.
 	 */
 	private void processInheritance(Map<String, List<String>> parentIdVsChildrenIds)
 	{
 		for (Entry<String, List<String>> entry : parentIdVsChildrenIds.entrySet())
 		{
 			EntityInterface parent = umlClassIdVsEntity.get(entry.getKey());
 			for (String childId : entry.getValue())
 			{
 				EntityInterface child = umlClassIdVsEntity.get(childId);
 				child.setParentEntity(parent);
 			}
 		}
 	}
 
 	/**
 	 * Taggs inherited attributes present in given entity group. The processing is based on name.
 	 * For a attribute, if attribute with same name present in parent hirarchy then it is considered as inherited. 
 	 * @param eg Entity Group top process
 	 */
 	private void markInheritedAttributes(EntityGroupInterface eg)
 	{
 		for (EntityInterface entity : eg.getEntityCollection())
 		{
 			if (entity.getParentEntity() != null)
 			{
 				List<AbstractAttributeInterface> duplicateAttrColl = new ArrayList<AbstractAttributeInterface>();
 				Collection<AttributeInterface> parentAttributeCollection = entity.getParentEntity()
 						.getAttributeCollection();
 				for (AttributeInterface attributeFromChild : entity.getAttributeCollection())
 				{
 					boolean isInherited = false;
 					for (AttributeInterface attributeFromParent : parentAttributeCollection)
 					{
 						if (attributeFromChild.getName().equals(attributeFromParent.getName()))
 						{
 							isInherited = true;
 							duplicateAttrColl.add(attributeFromChild);
 							break;
 						}
 					}
 				}
 				//removeInheritedAttributes(entity,duplicateAttrColl,true);
 				removeInheritedAttributes(entity, duplicateAttrColl);
 			}
 		}
 	}
 
 	/**
 	 * @param sourceEntity Entity to which a association is to be attached
 	 * @return A assocition attached to given entity.
 	 */
 	private AssociationInterface getAssociation(EntityInterface sourceEntity, String associationName)
 	{
 		AssociationInterface association = deFactory.createAssociation();
 		//remove it after getting DE fix,association name should not be compulsory
 		if (associationName == null || associationName.equals(""))
 		{
 			association.setName("AssociationName_"
 					+ (sourceEntity.getAssociationCollection().size() + 1));
 		}
 		else
 		{
 			association.setName(associationName);
 		}
 		association.setEntity(sourceEntity);
 		sourceEntity.addAssociation(association);
 		return association;
 	}
 
 	/**
 	 * Creates Role for the input UMLAssociationEdge
 	 * @param edge UML Association Edge to process
 	 * @return the Role for given UML Association Edge
 	 */
 	private RoleInterface getRole(Multiplicity srcMultiplicity, String sourceRoleName,
 			String associationType)
 	{
 		Collection<MultiplicityRange> rangeColl = srcMultiplicity.getRange();
 		int minCardinality = 0;
 		int maxCardinality = 0;
 		for (MultiplicityRange range : rangeColl)
 		{
 			minCardinality = range.getLower();
 			maxCardinality = range.getUpper();
 		}
 
 		RoleInterface role = deFactory.createRole();
 		if (associationType != null
 				&& (associationType
 						.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_CONTAINMENT_UNSPECIFIED) || associationType
 						.equalsIgnoreCase(XMIConstants.TAGGED_VALUE_CONTAINMENT_NOTSPECIFIED)))
 		{
 			role.setAssociationsType(Constants.AssociationType.ASSOCIATION);
 		}
 		else
 		{		
 			role.setAssociationsType(Constants.AssociationType.CONTAINTMENT);
 		}
 		role.setName(sourceRoleName);
 		role.setMaximumCardinality(getCardinality(maxCardinality));
 		role.setMinimumCardinality(getCardinality(minCardinality));
 		return role;
 	}
 
 	/**
 	 * Gets dynamic extension's Cardinality enumration for passed integer value.
 	 * @param cardinality intger value of cardinality.
 	 * @return Dynamic Extension's Cardinality enumration
 	 */
 	private Constants.Cardinality getCardinality(int cardinality)
 	{
 		if (cardinality == 0)
 		{
 			return Constants.Cardinality.ZERO;
 		}
 		if (cardinality == 1)
 		{
 			return Constants.Cardinality.ONE;
 		}
 		return Constants.Cardinality.MANY;
 	}
 
 	/**
 	 * @param entityInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private ContainerInterface createNewContainer(EntityInterface entityInterface)
 			throws DynamicExtensionsSystemException
 	{
 		ContainerInterface containerInterface = deFactory.createContainer();
 		containerInterface.setCaption(entityInterface.getName());
 		containerInterface.setEntity(entityInterface);
 
 		//Adding Required field indicator
 		containerInterface.setRequiredFieldIndicatior(" ");
 		containerInterface.setRequiredFieldWarningMessage(" ");
 
 		Collection<AbstractAttributeInterface> abstractAttributeCollection = entityInterface
 				.getAbstractAttributeCollection();
 		Integer sequenceNumber = new Integer(0);
 		ControlInterface controlInterface;
 		for (AbstractAttributeInterface abstractAttributeInterface : abstractAttributeCollection)
 		{
 			controlInterface = getControlForAttribute(abstractAttributeInterface);
 			sequenceNumber++;
 			controlInterface.setSequenceNumber(sequenceNumber);
 			containerInterface.addControl(controlInterface);
 			controlInterface.setParentContainer((Container) containerInterface);
 		}
 		return containerInterface;
 	}
 
 	/**
 	 * @param entityName
 	 * @return
 	 */
 	private ContainerInterface getContainer(String entityName)
 	{
 		if (retrievedContainerList != null && retrievedContainerList.size() > 0)
 		{
 			for (ContainerInterface container : retrievedContainerList)
 			{
 				if (container.getCaption().equals(entityName))
 				{
 					return container;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param entityName
 	 * @return
 	 */
 	private EntityInterface getEntity(String entityName)
 	{
 		if (retrievedContainerList != null && retrievedContainerList.size() > 0)
 		{
 			for (ContainerInterface container : retrievedContainerList)
 			{
 				if (container.getEntity().getName().equals(entityName))
 				{
 					return container.getEntity();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method creates a container object.
 	 * @param entityInterface
 	 * @return
 	 */
 	protected void createContainer(EntityInterface entityInterface) throws Exception
 	{
 		ContainerInterface containerInterface = getContainer(entityInterface.getName());
 		/*DynamicExtensionsUtility.getContainerByCaption(entityInterface.getName()); */
 		if (containerInterface == null)//Add
 		{
 			containerInterface = createNewContainer(entityInterface);
 		}
 		else
 		{//Edit			
 			editEntityAndContainer(containerInterface, entityInterface);
 
 			//Populating Attributes and Controls
 			Collection<AbstractAttributeInterface> editedAttributeColl = entityInterface
 					.getAbstractAttributeCollection();
 			Collection<AbstractAttributeInterface> originalAttributeColl = containerInterface
 					.getEntity().getAbstractAttributeCollection();
 
 			Collection<AbstractAttributeInterface> attributesToRemove = new HashSet<AbstractAttributeInterface>();
 			for (AbstractAttributeInterface editedAttribute : editedAttributeColl)
 			{
 				if(editedAttribute.getName().equalsIgnoreCase(Constants.SYSTEM_IDENTIFIER))
 				{// We dont edit "id" attribute as it is the system identifier.
 					continue;
 				}
 				
 				ControlsModel controlModel = new ControlsModel();
 				if (editedAttribute instanceof AssociationInterface)
 				{
 					//When association direction is changed from bi-directional to src-destination, this method removes
 					//the redundant association.
 					removeRedundantAssociation(editedAttribute, attributesToRemove);
 				}
 
 				boolean isAttrPresent = getAttrToEdit(originalAttributeColl, editedAttribute);
 
 				if (isAttrPresent)
 				{//Edit					
 					editAttributeAndControl(controlModel, editedAttribute, containerInterface);
 				}
 				else
 				{//Add Attribute
 					addAttributeAndControl(controlModel, editedAttribute, containerInterface);
 					//					Duplicate attributes have been created since we have created attribute in the method createattribues also
 					//Do not create attributes above but create them here.
 					if (!(editedAttribute instanceof AssociationInterface))
 					{
 						attributesToRemove.add(editedAttribute);
 					}
 				}
 				controlModel.setCaption(editedAttribute.getName());
 				controlModel.setName(editedAttribute.getName());
 				//Not for Containment Association Control
 				if (!(editedAttribute instanceof AssociationInterface))
 				{
 					ApplyFormControlsProcessor applyFormControlsProcessor = ApplyFormControlsProcessor
 							.getInstance();
 					applyFormControlsProcessor.addControlToForm(containerInterface, controlModel,
 							controlModel);
 				}
 			}
 			//Since we are creating attributes in createAttributes method and also in applyFormControlsProcessor.addControlToForm method
 			//duplicate attributes have been created. Hence removing the duplicate attributes.
 			editedAttributeColl.removeAll(attributesToRemove);
 		}
 		List<ContainerInterface> containerList = new ArrayList<ContainerInterface>();
 		containerList.add(containerInterface);
 		entityNameVsContainers.put(entityInterface.getName(), containerList);
 	}
 
 	/**
 	 * @param editedAttribute
 	 * @param attributesToRemove
 	 */
 	private void removeRedundantAssociation(AbstractAttributeInterface editedAttribute,
 			Collection<AbstractAttributeInterface> attributesToRemove)
 	{
 		AssociationInterface association = (AssociationInterface) editedAttribute;
 		Collection<AssociationInterface> targetEntityAssociationColl = association
 				.getTargetEntity().getAssociationCollection();
 
 		EntityInterface originalTargetEntity = getEntity(association.getTargetEntity().getName());
 		if(originalTargetEntity != null)
 		{
 			//Removing redundant association
 			for (AssociationInterface targetAsso : targetEntityAssociationColl)
 			{
 				if (targetAsso.getTargetEntity().getName().equalsIgnoreCase(
 						association.getEntity().getName()))
 				{
 					AssociationInterface originalTargetAssociation = null;
 					for (AssociationInterface originalTgtAsso : originalTargetEntity
 							.getAssociationCollection())
 					{
 						if (targetAsso.getName().equalsIgnoreCase(originalTgtAsso.getName()))
 						{
 							originalTargetAssociation = originalTgtAsso;
 							break;
 						}
 					}
 	
 					if (targetAsso.getAssociationDirection().equals(
 							Constants.AssociationDirection.SRC_DESTINATION)
 							&& originalTargetAssociation.getAssociationDirection().equals(
 									Constants.AssociationDirection.BI_DIRECTIONAL))
 					{//We need to remove system generated association if direction has been changed from bi directional to source destination
 						attributesToRemove.add(editedAttribute);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param controlModel
 	 * @param editedAttribute
 	 * @param containerInterface
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void addAttributeAndControl(ControlsModel controlModel,
 			AbstractAttributeInterface editedAttribute, ContainerInterface containerInterface)
 			throws DynamicExtensionsSystemException
 	{
 		controlModel.setControlOperation(ProcessorConstants.OPERATION_ADD);
 		ControlInterface newcontrol = getControlForAttribute(editedAttribute);
 		int sequenceNumber = containerInterface.getControlCollection().size() + 1;
 		newcontrol.setSequenceNumber(sequenceNumber);
 		//containerInterface.addControl(newcontrol);
 		newcontrol.setParentContainer((Container) containerInterface);
 
 		String userSelectedTool = DynamicExtensionsUtility.getControlName(newcontrol);
 		controlModel.setUserSelectedTool(userSelectedTool);
 		//For Text Control
 		if (newcontrol instanceof TextFieldInterface)
 		{
 			controlModel.setColumns(new Integer(0));
 		}
 		//For creating Association or Attribute
 		populateControlModel(controlModel, editedAttribute);
 
 		//if(controlModel.getUserSelectedTool().equalsIgnoreCase(ProcessorConstants.ADD_SUBFORM_CONTROL))
 		if (editedAttribute instanceof AssociationInterface)
 		{
 			containerInterface.addControl(newcontrol);
 		}
 	}
 
 	/**
 	 * @param originalAttributeColl
 	 * @param controlModel
 	 * @param editedAttribute
 	 * @param containerInterface
 	 * @param loadFormControlsProcessor
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void editAttributeAndControl(ControlsModel controlModel,
 			AbstractAttributeInterface editedAttribute, ContainerInterface containerInterface)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		LoadFormControlsProcessor loadFormControlsProcessor = LoadFormControlsProcessor
 				.getInstance();
 		controlModel.setControlOperation(ProcessorConstants.OPERATION_EDIT);
 
 		Collection<ControlInterface> originalControlColl = containerInterface
 				.getControlCollection();
 		ControlInterface originalControlObj = null;
 		for (ControlInterface originalcontrol : originalControlColl)
 		{
 			if (originalcontrol.getAbstractAttribute().getName().equalsIgnoreCase(
 					editedAttribute.getName()))
 			{
 				originalControlObj = originalcontrol;
 				break;
 			}
 		}
 
 		if (originalControlObj != null)
 		{
 			originalControlObj.setAbstractAttribute(editedAttribute);
 			//This method wil give us populated ControlUIBean and AttributeUIBean with original control object corresponding to edited attribute
 			if (!(editedAttribute instanceof AssociationInterface))
 			{
 				loadFormControlsProcessor.editControl(originalControlObj, controlModel,
 						controlModel);
 
 				controlModel
 						.setSelectedControlId(originalControlObj.getSequenceNumber().toString());
 			}
 			//controlModel.setCaption(originalControlObj.getCaption());
 		}
 	}
 
 	/**
 	 * @param containerInterface
 	 * @param entityInterface
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void editEntityAndContainer(ContainerInterface containerInterface,
 			EntityInterface entityInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 		EntityProcessor entityProcessor = EntityProcessor.getInstance();
 		ContainerModel containerModel = new ContainerModel();
 
 		//Setting Edited entity name as caption for container. 
 		//Also not setting parentform to avoid unncessary DB call as base container is already present in the container object
 		containerModel.setFormName(entityInterface.getName());
 		//Container Object is now populated
 		containerProcessor.populateContainerInterface(containerInterface, containerModel);
 
 		containerModel.setFormDescription(entityInterface.getDescription());
 		//Entity Object is now populated
 		entityProcessor.populateEntity(containerModel, containerInterface.getEntity());
 	}
 
 	/**
 	 * @param originalAttributeColl
 	 * @param editedAttribute
 	 * @return
 	 */
 	private boolean getAttrToEdit(Collection<AbstractAttributeInterface> originalAttributeColl,
 			AbstractAttributeInterface editedAttribute)
 	{
 		for (AbstractAttributeInterface originalAttribute : originalAttributeColl)
 		{
 			if (editedAttribute.getName().equalsIgnoreCase(originalAttribute.getName()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param controlModel
 	 * @param editedAttribute
 	 */
 	private void populateControlModel(ControlsModel controlModel,
 			AbstractAttributeInterface editedAttribute)
 	{
 		if (editedAttribute instanceof AssociationInterface)
 		{
 			controlModel.setDisplayChoice(ProcessorConstants.DISPLAY_CHOICE_LOOKUP);
 		}
 		else
 		{
 			AttributeInterface attribute = (AttributeInterface) editedAttribute;
 
 			AttributeTypeInformationInterface attributeTypeInfo = attribute
 					.getAttributeTypeInformation();
 			if (attributeTypeInfo instanceof DateAttributeTypeInformation)
 			{
 				controlModel.setDataType(ProcessorConstants.DATATYPE_DATE);
 			}
 			else if (attributeTypeInfo instanceof StringAttributeTypeInformation)
 			{
 				controlModel.setDataType(ProcessorConstants.DATATYPE_STRING);
 			}
 			else if (attributeTypeInfo instanceof ByteArrayAttributeTypeInformation)
 			{
 				controlModel.setDataType(ProcessorConstants.DATATYPE_BYTEARRAY);
 			}
 			else if (attributeTypeInfo instanceof BooleanAttributeTypeInformation)
 			{
 				controlModel.setDataType(ProcessorConstants.DATATYPE_BOOLEAN);
 			}
 			else if (attributeTypeInfo instanceof ShortAttributeTypeInformation
 					|| attributeTypeInfo instanceof IntegerAttributeTypeInformation
 					|| attributeTypeInfo instanceof LongAttributeTypeInformation
 					|| attributeTypeInfo instanceof FloatAttributeTypeInformation
 					|| attributeTypeInfo instanceof DoubleAttributeTypeInformation)
 			{
 				controlModel.setDataType(ProcessorConstants.DATATYPE_NUMBER);
 			}
 		}
 	}
 
 	/**
 	 * @param parentIdVsChildrenIds
 	 * This method add the parent container to the child container for Generalisation.
 	 */
 	protected void postProcessInheritence(Map<String, List<String>> parentIdVsChildrenIds)
 			throws Exception
 	{
 		for (Entry<String, List<String>> entry : parentIdVsChildrenIds.entrySet())
 		{
 			EntityInterface parent = umlClassIdVsEntity.get(entry.getKey());
 
 			List parentContainerList = (ArrayList) entityNameVsContainers.get(parent.getName());
 			ContainerInterface parentContainer = null;
 			if (parentContainerList == null || parentContainerList.size() == 0)
 			{
 				parentContainer = getContainer(parent.getName());
 			}
 			else
 			{
 				parentContainer = (ContainerInterface) parentContainerList.get(0);
 			}
 			for (String childId : entry.getValue())
 			{
 				EntityInterface child = umlClassIdVsEntity.get(childId);
 
 				List childContainerList = (ArrayList) entityNameVsContainers.get(child.getName());
 				ContainerInterface childContainer = null;
 				if (childContainerList == null || childContainerList.size() == 0)
 				{
 					childContainer = getContainer(child.getName());
 				}
 				else
 				{
 					childContainer = (ContainerInterface) childContainerList.get(0);
 				}
 
 				childContainer.setBaseContainer(parentContainer);
 			}
 		}
 	}
 
 	/**
 	 * This method adds the target container to the containment association control
 	 */
 	protected void addControlsForAssociation() throws Exception
 	{
 		Set<String> entityIdKeySet = entityNameVsContainers.keySet();
 		for (String entityId : entityIdKeySet)
 		{
 			List containerList = (ArrayList) entityNameVsContainers.get(entityId);
 			ContainerInterface containerInterface = (ContainerInterface) containerList.get(0);
 			Collection<ControlInterface> controlCollection = containerInterface
 					.getControlCollection();
 
 			for (ControlInterface controlInterface : controlCollection)
 			{
 				if (controlInterface instanceof ContainmentAssociationControl)
 				{
 					ContainmentAssociationControl containmentAssociationControl = (ContainmentAssociationControl) controlInterface;
 					AssociationInterface associationInterface = (AssociationInterface) controlInterface
 							.getAbstractAttribute();
 
 					String targetEntityId = associationInterface.getTargetEntity().getName();
 
 					List targetContainerInterfaceList = (ArrayList) entityNameVsContainers
 							.get(targetEntityId.toString());
 
 					//					TODO remove this condition to delete association with deleted or renamed entities.
 					//getting container corresponding to renamed or deleted entity which is associated with some association from the retrieved entity group
 					ContainerInterface targetContainerInterface = null;
 					if (targetContainerInterfaceList == null
 							|| targetContainerInterfaceList.size() == 0)
 					{
 						targetContainerInterface = getContainer(targetEntityId);
 					}
 					else
 					{
 						targetContainerInterface = (ContainerInterface) targetContainerInterfaceList
 								.get(0);
 					}
 					containmentAssociationControl.setContainer(targetContainerInterface);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param abstractAttributeInterface
 	 * @return
 	 * This method creates a control for the attribute.
 	 */
 	private ControlInterface getControlForAttribute(
 			AbstractAttributeInterface abstractAttributeInterface)
 			throws DynamicExtensionsSystemException
 	{
 		ControlInterface controlInterface = null;
 		ControlConfigurationsFactory configurationsFactory = ControlConfigurationsFactory
 				.getInstance();
 		// Collect all the applicable Rule names 
 		List<String> implicitRuleList = null;
 		if (abstractAttributeInterface instanceof AssociationInterface)
 		{
 			AssociationInterface associationInterface = (AssociationInterface) abstractAttributeInterface;
 			if (associationInterface.getSourceRole().getAssociationsType().compareTo(
 					AssociationType.CONTAINTMENT) == 0)
 			{//This line is for containment association.
 				controlInterface = deFactory.createContainmentAssociationControl();
 				associationInterface.getSourceRole().setAssociationsType(
 						AssociationType.CONTAINTMENT);
 				associationInterface.getTargetRole().setAssociationsType(
 						AssociationType.CONTAINTMENT);
 			}
 			else
 			{//	this is for Linking Association
 				//if source maxcardinality or target  maxcardinality or both == -1, then control is listbox.
 				//int  sourceMaxCardinality = associationInterface.getSourceRole().getMaximumCardinality().getValue().intValue();
 
 				int targetMaxCardinality = associationInterface.getTargetRole()
 						.getMaximumCardinality().getValue().intValue();
 				if (targetMaxCardinality == -1)
 				{//List box for 1 to many or many to many relationship
 					controlInterface = deFactory.createListBox();
 					((ListBoxInterface) controlInterface).setIsMultiSelect(true);
 				}
 				else
 				{//Combo box for the rest
 					controlInterface = deFactory.createComboBox();
 				}
 
 				((SelectControl) controlInterface).setSeparator(",");
 				addAssociationDisplayAttributes(associationInterface, controlInterface);
 				implicitRuleList = configurationsFactory.getAllImplicitRules(
 						ProcessorConstants.COMBOBOX_CONTROL, "Text");
 			}
 		}
 		else
 		{
 			AttributeInterface attributeInterface = (AttributeInterface) abstractAttributeInterface;
 			AttributeTypeInformationInterface attributeTypeInformation = attributeInterface
 					.getAttributeTypeInformation();
 			UserDefinedDEInterface userDefinedDEInterface = (UserDefinedDEInterface) attributeTypeInformation
 					.getDataElement();
 
 			if (userDefinedDEInterface != null
 					&& userDefinedDEInterface.getPermissibleValueCollection() != null
 					&& userDefinedDEInterface.getPermissibleValueCollection().size() > 0)
 			{
 				controlInterface = deFactory.createListBox();
 
 				// multiselect for permisible values 
 				((ListBoxInterface) controlInterface).setIsMultiSelect(true);
 				attributeInterface.setIsCollection(new Boolean(true));
 				implicitRuleList = configurationsFactory.getAllImplicitRules(
 						ProcessorConstants.LISTBOX_CONTROL, attributeInterface.getDataType());
 
 			}
 			else if (attributeTypeInformation instanceof DateAttributeTypeInformation)
 			{
 				((DateAttributeTypeInformation) attributeTypeInformation)
 						.setFormat(Constants.DATE_PATTERN_MM_DD_YYYY);
 				controlInterface = deFactory.createDatePicker();
 				implicitRuleList = configurationsFactory.getAllImplicitRules(
 						ProcessorConstants.DATEPICKER_CONTROL, attributeInterface.getDataType());
 			}
 			//Creating check box for boolean attributes
 			else if (attributeTypeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				controlInterface = deFactory.createCheckBox();
 				implicitRuleList = configurationsFactory.getAllImplicitRules(
 						ProcessorConstants.CHECKBOX_CONTROL, attributeInterface.getDataType());
 			}
 			else
 			{
 				controlInterface = deFactory.createTextField();
				((TextFieldInterface) controlInterface).setColumns(0);
 				if (attributeTypeInformation instanceof StringAttributeTypeInformation)
 				{
 					((StringAttributeTypeInformation) attributeTypeInformation)
							.setSize(new Integer(0));
 					implicitRuleList = configurationsFactory.getAllImplicitRules(
 							ProcessorConstants.TEXT_CONTROL, ProcessorConstants.DATATYPE_STRING);
 				}
 				else
 				{
 					implicitRuleList = configurationsFactory.getAllImplicitRules(
 							ProcessorConstants.TEXT_CONTROL, ProcessorConstants.DATATYPE_NUMBER);
 				}
 			}
 		}
 		controlInterface.setName(abstractAttributeInterface.getName());
 		controlInterface.setCaption(abstractAttributeInterface.getName());
 		controlInterface.setAbstractAttribute(abstractAttributeInterface);
 
 		if (implicitRuleList != null && implicitRuleList.size() > 0)
 		{
 			for (String validationRule : implicitRuleList)
 			{
 				RuleInterface rule = instantiateRule(validationRule);
 				abstractAttributeInterface.addRule(rule);
 			}
 		}
 		return controlInterface;
 	}
 
 	/**
 	 * @param validationRule
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private RuleInterface instantiateRule(String validationRule)
 
 	throws DynamicExtensionsSystemException
 	{
 		RuleConfigurationObject ruleConfigurationObject = null;
 		RuleInterface rule = null;
 
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		ControlConfigurationsFactory configurationsFactory = ControlConfigurationsFactory
 				.getInstance();
 		Collection<RuleParameterInterface> ruleParameterCollection = new HashSet<RuleParameterInterface>();
 
 		ruleConfigurationObject = configurationsFactory.getRuleObject(validationRule);
 		//		ruleParameterCollection = getRuleParameterCollection(ruleConfigurationObject,
 		//				attributeUIBeanInformationIntf);
 
 		rule = domainObjectFactory.createRule();
 		rule.setName(ruleConfigurationObject.getRuleName());
 
 		if (ruleParameterCollection != null && !(ruleParameterCollection.isEmpty()))
 		{
 			rule.setRuleParameterCollection(ruleParameterCollection);
 		}
 
 		return rule;
 	}
 
 	/**
 	 * @param associationInterface
 	 * @param controlInterface
 	 * In case of linking association, this method adds the association display attributes.
 	 */
 	private void addAssociationDisplayAttributes(AssociationInterface associationInterface,
 			ControlInterface controlInterface)
 	{
 		EntityInterface targetEntity = associationInterface.getTargetEntity();
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		//		This method returns all attributes and not associations
 		Collection<AttributeInterface> targetEntityAttrColl = targetEntity.getAttributeCollection();
 		int seqNo = 1;
 		for (AttributeInterface attr : targetEntityAttrColl)
 		{
 			AssociationDisplayAttributeInterface associationDisplayAttribute = domainObjectFactory
 					.createAssociationDisplayAttribute();
 			associationDisplayAttribute.setSequenceNumber(seqNo);
 			associationDisplayAttribute.setAttribute(attr);
 			//This method adds to the associationDisplayAttributeCollection
 			((SelectControl) controlInterface)
 					.addAssociationDisplayAttribute(associationDisplayAttribute);
 			seqNo++;
 		}
 	}
 
 	/**
 	 * @param entity
 	 * This method removes inherited attributes.
 	 */
 
 	protected void removeInheritedAttributes(EntityInterface entity,
 			List duplicateAttributeCollection)
 	{
 		if (duplicateAttributeCollection != null)
 		{
 			entity.getAbstractAttributeCollection().removeAll(duplicateAttributeCollection);
 		}
 	}
 
 	/**
 	 * @param umlClasses
 	 * This method creates all containers.
 	 */
 	protected void processPersistence(List<String> containerNames) throws Exception
 	{
 		//Collection<ContainerInterface> containerColl = new HashSet<ContainerInterface>();
 
 //		Set<String> entityIdKeySet = entityNameVsContainers.keySet();
 		
 		for(String containerName : containerNames)
 		{			
 			List containerList = (ArrayList) entityNameVsContainers.get(containerName);	
 			if(containerList == null || containerList.size() < 1)
 			{
 				throw new DynamicExtensionsApplicationException("The container name " + containerName + " does " +
 						"not match with the container name in the Model.");
 			}
 			ContainerInterface containerInterface = (ContainerInterface) containerList.get(0);
 			mainContainerList.add(containerInterface);
 		}
 		
 		
 		
 		
 //		for (String entityId : entityIdKeySet)
 //		{
 //			List containerList = (ArrayList) entityNameVsContainers.get(entityId);
 //			ContainerInterface containerInterface = (ContainerInterface) containerList.get(0);
 //			containerColl.add(containerInterface);
 //		}
 		EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			entityManagerInterface.persistEntityGroupWithAllContainers(entityGroup, mainContainerList);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @throws Exception
 	 */
 	protected void postProcessAssociation() throws Exception
 	{
 		addControlsForAssociation();
 	}
 
 }
