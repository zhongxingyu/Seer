 package edu.wustl.cab2b.server.path;
 
 import static edu.wustl.cab2b.common.util.Constants.ORIGINAL_ASSOCIATION_POINTER;
 import static edu.wustl.cab2b.common.util.Constants.PROJECT_VERSION;
 import static edu.wustl.cab2b.common.util.Constants.TYPE_DERIVED;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 import edu.wustl.cab2b.server.util.InheritanceUtil;
 import gov.nih.nci.cagrid.metadata.common.SemanticMetadata;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.UMLClass;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociationEdge;
 
 /**
  * This class stores the UML model to database using dynamic extension APIs and
  * generates necessary information for all paths generation<br>
  * Using <b> {@link DomainModelParser}</b> to get the domain model of
  * application and transforms it to dynamic extension's objects.<br>
  * An instance of this class refers to one domain model and one entity group.
  * This class decides whether to create a storage table for entity or not based
  * on {@link edu.wustl.cab2b.server.path.PathConstants#CREATE_TABLE_FOR_ENTITY}
  * To create a table for entity set this to TRUE before calling this code else
  * set it to false.
  * 
  * @author Chandrakant Talele
  * @version 1.0
  */
 
 public class DomainModelProcessor {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(DomainModelProcessor.class);
     /**
      * Map with KEY : EntityInterface VALUE: its index in adjacency matrix.
      */
     private Map<EntityInterface, Integer> entityVsIndex;
 
     /**
      * Instance of Domain object factory, which will be used to create dynamic
      * extension's objects.
      */
     private static DomainObjectFactory deFactory = DomainObjectFactory.getInstance();
 
     /**
      * Map with KEY : UML id of a class(coming from domain model) VALUE :
      * dynamic extension Entity created for this UML class.
      */
     private Map<String, EntityInterface> umlClassIdVsEntity;
 
     /**
      * Saved entity group created by this class
      */
     private EntityGroupInterface entityGroup;
 
     /**
      * This constructor creates a entity group with the name as Project's Long
      * name from domain model, and short name as given "applicationName". It
      * stores classes and associations as part of a single entity group. Then
      * creates adjacencyMatrix and other needed information for path building.
      * 
      * @param parser The DomainModelParser from which this class will get
      *            Classes,Associations etc
      * @param applicationName Name of the application. It will be the Short name
      *            of the newly created entity group.
      * @throws DynamicExtensionsApplicationException 
      * @throws DynamicExtensionsSystemException 
      */
     public DomainModelProcessor(DomainModelParser parser, String applicationName) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException {
         logger.info("Creating entity group for application : " + applicationName);
 
         entityGroup = DynamicExtensionUtility.createEntityGroup();
         entityGroup.setShortName(applicationName);
         entityGroup.setName(parser.getDomainModel().getProjectLongName());
         entityGroup.setLongName(parser.getDomainModel().getProjectLongName());
         entityGroup.setDescription(parser.getDomainModel().getProjectDescription());
         DynamicExtensionUtility.addTaggedValue(entityGroup, PROJECT_VERSION,
                                                parser.getDomainModel().getProjectVersion());
 
         UMLClass[] umlClasses = parser.getUmlClasses();
         int noOfClasses = umlClasses.length;
         umlClassIdVsEntity = new HashMap<String, EntityInterface>(noOfClasses);
         for (UMLClass umlClass : umlClasses) {
             EntityInterface entity = createEntity(umlClass);
             entity.addEntityGroupInterface(entityGroup);
             entityGroup.addEntity(entity);
             umlClassIdVsEntity.put(umlClass.getId(), entity);
         }
         Map<String, List<String>> parentIdVsChildrenIds = parser.getParentVsChildrenMap();
         for (UMLAssociation umlAssociation : parser.getUmlAssociations()) {
             addAssociation(umlAssociation, parentIdVsChildrenIds);
         }
 
         processInheritance(parentIdVsChildrenIds);
         markInheritedAttributes(entityGroup);
 
         logger.info("Storing entity group....");
         DynamicExtensionUtility.markMetadataEntityGroup(entityGroup);
         entityGroup = saveEntityGroup(entityGroup);
 
         entityVsIndex = new HashMap<EntityInterface, Integer>(noOfClasses);
         int index = 0;
         for (EntityInterface entity : entityGroup.getEntityCollection()) {
             entityVsIndex.put(entity, index);
             index++;
         }
     }
 
     /**
      * Creates a Dynamic Exension Entity from given UMLClass.<br>
      * It also assigns all the attributes of the UMLClass to the Entity as the
      * Dynamic Extension Primitive Attributes.Then stores the input UML class,
      * adds the Dynamic Extension's PrimitiveAttributes to the Collection.
      * Properties which are copied from UMLAttribute to DE Attribute are
      * name,description,semanticMetadata,permissible values
      * 
      * @param umlClass The UMLClass from which to form the Dynamic Extension
      *            Entity
      * @return the unsaved entity for given UML class
      */
     protected EntityInterface createEntity(UMLClass umlClass) {
         logger.debug("Creating entity for class : " + umlClass.getClassName());
         String name = new StringBuilder().append(umlClass.getPackageName()).append(".").append(umlClass.getClassName()).toString();
         EntityInterface entity = deFactory.createEntity();
         entity.setName(name);
         entity.setDescription(umlClass.getDescription());
         setSemanticMetadata(entity, umlClass.getSemanticMetadata());
         UMLAttribute[] attributes = umlClass.getUmlAttributeCollection().getUMLAttribute();
 
         if (attributes == null) {
             return entity;
         }
         for (UMLAttribute umlAttribute : attributes) {
             DataType dataType = DataType.get(umlAttribute.getDataTypeName());
             AttributeInterface attribute = dataType.createAttribute(umlAttribute);
             if (attribute != null) { // to bypass attributes of invalid data-types
                 attribute.setName(umlAttribute.getName());
                 attribute.setDescription(umlAttribute.getDescription());
                 setSemanticMetadata(attribute, umlAttribute.getSemanticMetadata());
                 entity.addAttribute(attribute);
             }
         }
         return entity;
     }
 
     /**
      * Converts the UML association to dynamic Extension Association.Adds it to
      * the entity group. It replicates this association in all children of
      * source and all children of target class. It taggs replicated association
      * to identify them later on and mark them inherited. Also a back pointer is
      * added to replicated association go get original association.
      * 
      * @param umlAssociation umlAssociation to process
      * @param parentIdVsChildrenIds Map with key as UML-id of parent class and
      *            value as list of UML-id of all children classes.
      */
     void addAssociation(UMLAssociation umlAssociation, Map<String, List<String>> parentIdVsChildrenIds) {
         UMLAssociationEdge srcEdge = umlAssociation.getSourceUMLAssociationEdge().getUMLAssociationEdge();
         String srcId = srcEdge.getUMLClassReference().getRefid();
         EntityInterface srcEntity = umlClassIdVsEntity.get(srcId);
 
         UMLAssociationEdge tgtEdge = umlAssociation.getTargetUMLAssociationEdge().getUMLAssociationEdge();
         String tgtId = tgtEdge.getUMLClassReference().getRefid();
         EntityInterface tgtEntity = umlClassIdVsEntity.get(tgtId);
 
         AssociationInterface association = getAssociation(srcEntity);
         association.setSourceRole(getRole(srcEdge));
         association.setTargetEntity(tgtEntity);
         association.setTargetRole(getRole(tgtEdge));
         if (umlAssociation.isBidirectional()) {
             association.setAssociationDirection(Constants.AssociationDirection.BI_DIRECTIONAL);
         } else {
             association.setAssociationDirection(Constants.AssociationDirection.SRC_DESTINATION);
         }
         String originalAssociationIdentifier = InheritanceUtil.generateUniqueId(association);
         List<EntityInterface> childrenOfSrc = new ArrayList<EntityInterface>();
         childrenOfSrc.add(srcEntity);
         childrenOfSrc.addAll(getAllChildren(srcId, parentIdVsChildrenIds));
 
         List<EntityInterface> childrenOfTgt = new ArrayList<EntityInterface>();
         childrenOfTgt.add(tgtEntity);
         childrenOfTgt.addAll(getAllChildren(tgtId, parentIdVsChildrenIds));
 
         for (int i = 0; i < childrenOfSrc.size(); i++) {
             for (int j = 0; j < childrenOfTgt.size(); j++) {
                 if (i == 0 && j == 0) {
                     continue; // skipping association between roots as it is
                     // already added
                 }
                 AssociationInterface replica = getAssociation(childrenOfSrc.get(i));
                 RoleInterface srcRole = DynamicExtensionUtility.cloneRole(association.getSourceRole());
                 RoleInterface tgtRole = DynamicExtensionUtility.cloneRole(association.getTargetRole());
                 replica.setSourceRole(srcRole);
                 replica.setTargetEntity(childrenOfTgt.get(j));
                 replica.setTargetRole(tgtRole);
                 replica.setAssociationDirection(association.getAssociationDirection());
                 markInherited(replica);
                 DynamicExtensionUtility.addTaggedValue(replica, ORIGINAL_ASSOCIATION_POINTER,
                                                        originalAssociationIdentifier);
             }
         }
     }
 
     /**
      * Creates Role for the input UMLAssociationEdge
      * 
      * @param edge UML Association Edge to process
      * @return the Role for given UML Association Edge
      */
     private RoleInterface getRole(UMLAssociationEdge edge) {
         int maxCardinality = edge.getMaxCardinality();
         int minCardinality = edge.getMinCardinality();
         RoleInterface role = deFactory.createRole();
         role.setAssociationsType(Constants.AssociationType.ASSOCIATION);
         role.setName(edge.getRoleName());
        //Bug# 10295: Workaround to fix the max cardinality = 0. Assuming these cases are MANY
        if(maxCardinality == 0) {
            role.setMaximumCardinality(Constants.Cardinality.MANY);
        } else {
            role.setMaximumCardinality(getCardinality(maxCardinality));
        }
         role.setMinimumCardinality(getCardinality(minCardinality));
         return role;
     }
 
     /**
      * Gets dynamic extension's Cardinality enumration for passed integer value.
      * 
      * @param cardinality integer value of cardinality.
      * @return Dynamic Extension's Cardinality enumeration
      */
     private Constants.Cardinality getCardinality(int cardinality) {
         if (cardinality == 0) {
             return Constants.Cardinality.ZERO;
         }
         if (cardinality == 1) {
             return Constants.Cardinality.ONE;
         }
         return Constants.Cardinality.MANY;
     }
 
     /**
      * Stores the SemanticMetadata to the owner which can be class or attribute
      * 
      * @param owner any class extending AbstractMetadataInterface
      * @param semanticMetadataArr Semantic Metadata array to set.
      */
     private void setSemanticMetadata(AbstractMetadataInterface owner, SemanticMetadata[] semanticMetadataArr) {
         DynamicExtensionUtility.setSemanticMetadata(owner, semanticMetadataArr);
     }
 
     /**
      * Returns the List of entities Ids. This list is ordered in sync with
      * Adjacency Matrix returned by
      * {@link DomainModelProcessor#getAdjacencyMatrix()}
      * 
      * @return the List of entities Ids
      */
     public List<Long> getEntityIds() {
         ArrayList<Long> entityIds = new ArrayList<Long>(entityVsIndex.size());
         for (int i = 0; i < entityVsIndex.size(); i++) {
             entityIds.add(i, null);
         }
         for (Entry<EntityInterface, Integer> entry : entityVsIndex.entrySet()) {
             entityIds.set(entry.getValue(), entry.getKey().getId());
         }
         return entityIds;
     }
 
     /**
      * This matrix will be used to generate linear paths. AdjacencyMatrix[i][j]
      * will be true only if there is a association present in iTH and jTH class
      * otherwise it will be false. Entities referring to each index of adjacency
      * matrix can be taken from iTH element of list taken from
      * {@link DomainModelProcessor#getEntityIds()}
      * 
      * @return the adjacencyMatrix
      */
     public boolean[][] getAdjacencyMatrix() {
         int noOfClasses = entityVsIndex.size();
         boolean[][] adjacencyMatrix = new boolean[noOfClasses][noOfClasses];
 
         List<Integer> srcIndexes = new ArrayList<Integer>();
         List<Integer> destIndexes = new ArrayList<Integer>();
 
         for (EntityInterface entity : entityVsIndex.keySet()) {
             for (AssociationInterface association : entity.getAssociationCollection()) {
                 int srcIndex = entityVsIndex.get(entity);
                 int tgtIndex = entityVsIndex.get(association.getTargetEntity());
 
                 if (InheritanceUtil.isInherited(association)) {
                     srcIndexes.add(srcIndex);
                     destIndexes.add(tgtIndex);
                     continue;
                 }
 
                 adjacencyMatrix[srcIndex][tgtIndex] = true;
 
                 if (association.getAssociationDirection().equals(AssociationDirection.BI_DIRECTIONAL)) {
                     adjacencyMatrix[tgtIndex][srcIndex] = true;
                 }
             }
         }
         for (int i = 0; i < srcIndexes.size(); i++) {
             adjacencyMatrix[srcIndexes.get(i)][destIndexes.get(i)] = false;
             adjacencyMatrix[destIndexes.get(i)][srcIndexes.get(i)] = false;
         }
 
         return adjacencyMatrix;
     }
 
     /**
      * Processes inheritance relation ship present in domain model
      * 
      * @param parentIdVsChildrenIds Map with key as UML-id of parent class and
      *            value as list of UML-id of all children classes.
      */
     void processInheritance(Map<String, List<String>> parentIdVsChildrenIds) {
         for (Entry<String, List<String>> entry : parentIdVsChildrenIds.entrySet()) {
             EntityInterface parent = umlClassIdVsEntity.get(entry.getKey());
             for (String childId : entry.getValue()) {
                 EntityInterface child = umlClassIdVsEntity.get(childId);
                 child.setParentEntity(parent);
             }
         }
     }
 
     // default scope for testing purpose
     /**
      * Taggs inherited attributes present in given entity group. The processing
      * is based on name. For a attribute, if attribute with same name present in
      * parent hirarchy then it is considered as inherited.
      * 
      * @param eg Entity Group top process
      */
     void markInheritedAttributes(EntityGroupInterface eg) {
         for (EntityInterface entity : eg.getEntityCollection()) {
             if (entity.getParentEntity() != null) {
                 Collection<AttributeInterface> parentAttributeCollection = entity.getParentEntity().getAttributeCollection();
                 for (AttributeInterface attributeFromChild : entity.getAttributeCollection()) {
                     boolean isInherited = false;
                     for (AttributeInterface attributeFromParent : parentAttributeCollection) {
                         if (attributeFromChild.getName().equals(attributeFromParent.getName())) {
                             isInherited = true;
                             break;
                         }
                     }
                     if (isInherited) {
                         markInherited(attributeFromChild);
                     }
                 }
             }
         }
     }
 
     /**
      * Marks given abstract attribute (attribute/association) as a derived one.
      * 
      * @param abstractAttribute abstract attribute to tagg.
      */
     private void markInherited(AbstractAttributeInterface abstractAttribute) {
         DynamicExtensionUtility.addTaggedValue(abstractAttribute, TYPE_DERIVED, TYPE_DERIVED);
     }
 
     /**
      * @param umlIdOfParent Parent UML class
      * @param parentIdVsChildrenIds Map with key as UML-id of parent class and
      *            value as list of UML-id of all children classes.
      * @return All the childrens of given class
      */
     List<EntityInterface> getAllChildren(String umlIdOfParent, Map<String, List<String>> parentIdVsChildrenIds) {
         List<String> childrenIds = parentIdVsChildrenIds.get(umlIdOfParent);
         if (childrenIds == null || childrenIds.isEmpty()) {
             return new ArrayList<EntityInterface>(0);
         }
         List<EntityInterface> children = new ArrayList<EntityInterface>();
         for (String childId : childrenIds) {
             children.add(umlClassIdVsEntity.get(childId));
             children.addAll(getAllChildren(childId, parentIdVsChildrenIds));
         }
         return children;
     }
 
     /**
      * @param sourceEntity Entity to which a association is to be attached
      * @return A association attached to given entity.
      */
     AssociationInterface getAssociation(EntityInterface sourceEntity) {
         AssociationInterface association = deFactory.createAssociation();
         // TODO remove it after getting DE fix,association name should not be
         // compulsory
         association.setName("AssociationName_" + (sourceEntity.getAssociationCollection().size() + 1));
         association.setEntity(sourceEntity);
         sourceEntity.addAssociation(association);
         return association;
     }
 
     /**
      * Gives information about replication nodes KEY : Any entity which has at
      * least one non-replicated association. VALUE : All entities that are
      * direct/indirect subclasses of the KEY entity
      * 
      * @return All the replication nodes. Needed for path generation algorithm
      */
     public Map<Integer, Set<Integer>> getReplicationNodes() {
         Map<Integer, Set<Integer>> parentVsAllChildren = new HashMap<Integer, Set<Integer>>();
         for (EntityInterface child : entityVsIndex.keySet()) {
             EntityInterface parent = child.getParentEntity();
             if (parent != null) {
                 Integer parentId = entityVsIndex.get(parent);
                 Set<Integer> children = parentVsAllChildren.get(parentId);
                 if (children == null) {
                     children = new HashSet<Integer>();
                     parentVsAllChildren.put(parentId, children);
                 }
                 children.add(entityVsIndex.get(child));
             }
         }
         return parentVsAllChildren;
     }
 
     /**
      * @return Returns the saved entity group created by this class
      */
     public EntityGroupInterface getEntityGroup() {
         return entityGroup;
     }
 
     /**
      * @param entityVsIndex The entityVsIndex to set.
      */
     void setEntityVsIndex(Map<EntityInterface, Integer> entityVsIndex) {
         this.entityVsIndex = entityVsIndex;
     }
 
     /**
      * For testing purpose. Default scope constructor
      */
     DomainModelProcessor() {
 
     }
     EntityGroupInterface saveEntityGroup(EntityGroupInterface eg) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException {
         return DynamicExtensionUtility.persistEGroup(eg);
     }
 
 }
