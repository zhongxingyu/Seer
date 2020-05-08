 /****************************************************
  * 
  * Universidad Francisco de Paula Santander UFPS
  * Ccuta, Colombia
  * (c) 2014 by UFPS. All rights reserved.
  * 
  ****************************************************/
 
 package classmodeler.web.converters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.uml2.uml.CallConcurrencyKind;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.Enumeration;
 import org.eclipse.uml2.uml.EnumerationLiteral;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.Interface;
 import org.eclipse.uml2.uml.InterfaceRealization;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Operation;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Realization;
 import org.eclipse.uml2.uml.Type;
 import org.eclipse.uml2.uml.UMLFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.VisibilityKind;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import classmodeler.domain.uml.types.OperationCustom;
 import classmodeler.domain.uml.types.java.JavaTypes;
 import classmodeler.service.util.CollectionUtils;
 import classmodeler.service.util.GenericUtils;
 import classmodeler.web.beans.SharedDiagram;
 
 import com.mxgraph.model.mxCell;
 
 /**
  * Converter class that allows to parse a class diagram from XML representation
  * to UML Metamodel.
  * 
  * @author Gabriel Leonardo Diaz, 23.03.2014.
  */
 public final class UMLConverter {
   
   private Map<String, Package> packages         = new HashMap<String, Package>();
   private Map<String, Classifier> classifiers   = new HashMap<String, Classifier>();
   
   private SharedDiagram diagram;
   
   /**
    * Constructs the UML metamodel converter by using the given XML model
    * representation.
    * 
    * @param mxModel
    * @author Gabriel Leonardo Diaz, 25.03.2014.
    */
   public UMLConverter (SharedDiagram diagram) {
     super();
     this.diagram = diagram;
   }
   
   /**
    * Gets the diagram which is being transformed to UML metamodel.
    * 
    * @return
    * @author Gabriel Leonardo Diaz, 26.03.2014.
    */
   public SharedDiagram getDiagram() {
     return diagram;
   }
   
   /**
    * Clears the converter and prepares it for a new execution.
    * 
    * @author Gabriel Leonardo Diaz, 25.03.2014.
    */
   private void initialize () {
     this.packages.clear();
     this.classifiers.clear();
   }
   
   /**
    * Generates the UML model from the diagram created.
    * 
    * @param diagram
    * @return
    * @author Gabriel Leonardo Diaz, 26.03.2014.
    */
   public List<NamedElement> execute () {
     initialize();
     
     List<NamedElement> result = new ArrayList<NamedElement>();
     mxCell mxCell;
     
     // 1. Generate empty types
     for (Object cell : diagram.getModel().getCells().values()) {
       mxCell  = (mxCell) cell;
       if (mxCell.equals(diagram.getModel().getRoot())) {
         continue;
       }
       
       if (!mxCell.isVertex()) {
         continue;
       }
       
       if (isPackage(mxCell)) {
         generatePackage(mxCell);
       }
       else if (isClass(mxCell)) {
         result.add(generateClass(mxCell));
       }
       else if (isInterface(mxCell)) {
         result.add(generateInteface(mxCell));
       }
       else if (isEnumeration(mxCell)) {
         result.add(generateEnumeration(mxCell));
       }
     }
     
     
     mxCell edge;
     Classifier classifier;
     Classifier source;
     Classifier target;
     
     // 2. Generate other objects
     for (Entry<String, Classifier> entry : this.classifiers.entrySet()) {
       mxCell = (mxCell) diagram.getModel().getCell(entry.getKey());
       classifier = entry.getValue();
       
       // 2.1 Properties
       for (mxCell property : getProperties(mxCell)) {
         if (classifier instanceof Enumeration) {
           generateLiteral((Enumeration) classifier, property);
         }
         else {
           generateProperty(classifier, property);
         }
       }
       
       // 2.2 Operations
       for (mxCell operation : getOperations(mxCell)) {
         generateOperation(classifier, operation);
       }
       
       // 2.3 Relationships
       if (mxCell.getEdgeCount() > 0) {
         for (int i = 0; i < mxCell.getEdgeCount(); i++) {
           edge = (com.mxgraph.model.mxCell) mxCell.getEdgeAt(i);
           
           if (isGeneralization(edge)) {
             source = this.classifiers.get(edge.getSource().getId());
             target = this.classifiers.get(edge.getTarget().getId());
             generateGeneralization(source, target);
           }
           else if (isRealization(edge)) {
             source = this.classifiers.get(edge.getSource().getId());
             target = this.classifiers.get(edge.getTarget().getId());
             
             if (source instanceof Class && target instanceof Interface) {
               generateRealization((Class) source, (Interface) target, edge);
             }
           }
         }
       }
     }
     
     return result;
   }
   
   /**
    * Generates the UML Class Metamodel from the given XML class representation.
    * 
    * @param classCell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   private Class generateClass (mxCell classCell) {
     String name               = classCell.getAttribute("name");
     Package aPackage          = getPackage(classCell.getAttribute("package"));
     VisibilityKind visibility = parseVisibility(classCell.getAttribute("visibility"));
     boolean isStatic          = parseBoolean(classCell.getAttribute("static"));
     boolean isAbstract        = parseBoolean(classCell.getAttribute("abstract"));
     boolean isFinal           = parseBoolean(classCell.getAttribute("final"));
     
     Class aClass = UMLFactory.eINSTANCE.createClass();
     aClass.setName(name);
     aClass.setIsAbstract(isAbstract);
     aClass.setVisibility(visibility);
     aClass.setIsLeaf(isStatic);
     aClass.setIsFinalSpecialization(isFinal);
     aClass.setPackage(aPackage);
     
     this.classifiers.put(classCell.getId(), aClass);
     
     return aClass;
   }
   
   /**
    * Generates the UML Inteface metamodel from the given XML interface
    * representation.
    * 
    * @param interfaceCell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   private Interface generateInteface (mxCell interfaceCell) {
     String name               = interfaceCell.getAttribute("name");
     Package aPackage          = getPackage(interfaceCell.getAttribute("package"));
     VisibilityKind visibility = parseVisibility(interfaceCell.getAttribute("visibility"));
     
     Interface aInterface = UMLFactory.eINSTANCE.createInterface();
     aInterface.setName(name);
     aInterface.setVisibility(visibility);
     aInterface.setPackage(aPackage);
     
     this.classifiers.put(interfaceCell.getId(), aInterface);
     
     return aInterface;
   }
   
   /**
    * Generates the UML Enumeration metamodel from the given XML enumeration.
    * 
    * @param enumerationCell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   private Enumeration generateEnumeration(mxCell enumerationCell) {
     String name               = enumerationCell.getAttribute("name");
     Package aPackage          = getPackage(enumerationCell.getAttribute("package"));
     VisibilityKind visibility = parseVisibility(enumerationCell.getAttribute("visibility"));
     
     Enumeration aEnumeration = UMLFactory.eINSTANCE.createEnumeration();
     aEnumeration.setName(name);
     aEnumeration.setVisibility(visibility);
     aEnumeration.setPackage(aPackage);
     
     this.classifiers.put(enumerationCell.getId(), aEnumeration);
     
     return aEnumeration;
   }
   
   /**
    * Generates a UML package element with the given cell
    * 
    * @param packageCell
    *          The cell representing a package.
    * @return The new package element.
    * @author Gabriel Leonardo Diaz, 25.03.2014.
    */
   private Package generatePackage (mxCell packageCell) {
     Package aPackage = UMLFactory.eINSTANCE.createPackage();
     aPackage.setName(packageCell.getAttribute("name"));
     
     this.packages.put(packageCell.getId(), aPackage);
     
     return aPackage;
   }
   
   /**
    * Generates a UML Enumeration Literal element with the given cell.
    * 
    * @param enumeration
    * @param literalCell
    * @return The new enumeration literal, this was already added as owned
    *         element of the given enumeration parameter.
    * @author Gabriel Leonardo Diaz, 26.03.2014.
    */
   private EnumerationLiteral generateLiteral (Enumeration enumeration, mxCell literalCell) {
     return enumeration.createOwnedLiteral(literalCell.getAttribute("name"));
   }
   
   /**
    * Generates a UML Property element with the information contained by the XML
    * representation.
    * 
    * @param classifier
    * @param propertyCell
    * @return
    * @author Gabriel Leonardo Diaz, 27.03.2014.
    */
   private Property generateProperty (Classifier classifier, mxCell propertyCell) {
     Property property = null;
     
     String name               = propertyCell.getAttribute("name");
     String defaultValue       = propertyCell.getAttribute("initialValue");
     Type type                 = getType(propertyCell.getAttribute("type"), propertyCell.getAttribute("collection"));
     VisibilityKind visibility = parseVisibility(propertyCell.getAttribute("visibility"));
     boolean isStatic          = parseBoolean(propertyCell.getAttribute("static"));
     boolean isFinal           = parseBoolean(propertyCell.getAttribute("final"));
     
     if (classifier instanceof Class) {
       property = ((Class) classifier).createOwnedAttribute(name, type);
     }
     else if (classifier instanceof Interface) {
       property = ((Interface) classifier).createOwnedAttribute(name, type);
     }
     
     if (property != null) {
       property.setVisibility(visibility);
       property.setIsStatic(isStatic);
       property.setIsLeaf(isFinal);
       property.setDefault(parseString(defaultValue));
       property.setUpper(UMLPackage.LITERAL_UNLIMITED_NATURAL___UNLIMITED_VALUE);
     }
     
     return property;
   }
   
   /**
    * Generates a UML Operation element with the information contained by the XML
    * representation.
    * 
    * @param classifier
    * @param operationCell
    * @return
    * @author Gabriel Leonardo Diaz, 27.03.2014.
    */
   private Operation generateOperation (Classifier classifier, mxCell operationCell) {
     OperationCustom operation = null;
     
     String name               = operationCell.getAttribute("name");
     Type type                 = getType(operationCell.getAttribute("type"), operationCell.getAttribute("collection"));
     VisibilityKind visibility = parseVisibility(operationCell.getAttribute("visibility"));
     boolean isStatic          = parseBoolean(operationCell.getAttribute("static"));
     boolean isFinal           = parseBoolean(operationCell.getAttribute("final"));
     boolean isSynchronized    = parseBoolean(operationCell.getAttribute("synchronized"));
    boolean isAbstract        = parseBoolean(operationCell.getAttribute("abstract"));
     
     if (classifier instanceof Class) {
       operation = new OperationCustom();
       ((Class) classifier).getOwnedOperations().add(operation);
     }
     else if (classifier instanceof Interface) {
       operation = new OperationCustom();
       ((Interface) classifier).getOwnedOperations().add(operation);
     }
     
     if (operation != null) {
       operation.setName(name);
       operation.setReturnType(type);
       operation.setVisibility(visibility);
       operation.setIsStatic(isStatic);
       operation.setIsLeaf(isFinal);
       operation.setIsAbstract(isAbstract);
       
       if (isSynchronized) {
         operation.setConcurrency(CallConcurrencyKind.GUARDED_LITERAL);
       }
       
       Element operationNode = (Element) operationCell.getValue();
       Element paramNode;
       NodeList nodeList = operationNode.getChildNodes();
       for (int i = 0; i < nodeList.getLength(); i++) {
         paramNode = (Element) nodeList.item(i);
         operation.createOwnedParameter(paramNode.getAttribute("name"), getType(paramNode.getAttribute("type"), paramNode.getAttribute("collection")));
       }
     }
     
     return operation;
   }
   
   /**
    * Generates a UML Generalization element by using the given classifier.
    * 
    * @param source
    *          The specific classifier.
    * @param target
    *          The general classifier.
    * @return
    * @author Gabriel Leonardo Diaz, 27.03.2014.
    */
   private Generalization generateGeneralization (Classifier source, Classifier target) {
     Generalization generalization = null;
     
     if (!CollectionUtils.isEmptyCollection(source.getGeneralizations())) {
       for (Generalization aGen : source.getGeneralizations()) {
         if (GenericUtils.equals(aGen.getGeneral(), target)) {
           generalization = aGen;
           break;
         }
       }
     }
     
     if (generalization == null) {
       generalization = source.createGeneralization(target);
     }
     
     return generalization;
   }
   
   /**
    * Generates a UML Generalization element by using the given classifier.
    * 
    * @param source
    *          The specific classifier.
    * @param target
    *          The general classifier.
    * @return
    * @author Gabriel Leonardo Diaz, 27.03.2014.
    */
   private Realization generateRealization (Class source, Interface target, mxCell realizationCell) {
     Realization realization = null;
     
     if (!CollectionUtils.isEmptyCollection(source.getInterfaceRealizations())) {
       for (InterfaceRealization aRealization : source.getInterfaceRealizations()) {
         if (GenericUtils.equals(aRealization.getContract(), target)) {
           realization = aRealization;
           break;
         }
       }
     }
     
     if (realization == null) {
       realization = source.createInterfaceRealization(realizationCell.getAttribute("name"), target);
     }
     
     return realization;
   }
   
   /**
    * Convenience method that creates the package if it does not exist.
    * 
    * @param packageId
    * @return
    * @author Gabriel Leonardo Diaz, 25.03.2014.
    */
   private Package getPackage (String packageId) {
     Package aPackage = this.packages.get(packageId);
     if (aPackage == null && !GenericUtils.isEmptyString(packageId)) {
       mxCell packageCell = (mxCell) diagram.getModel().getCell(packageId);
       if (packageCell != null) {
         aPackage = generatePackage(packageCell);
       }
     }
     return aPackage;
   }
   
   /**
    * Generates the type for the given id. This can be a primitive type, a
    * classifier, a generic collection or an anonymous type.
    * 
    * @param typeId
    * @param collectionType
    * @return
    * @author Gabriel Leonardo Diaz, 26.03.2014.
    */
   private Type getType (String typeId, String collectionType) {
     Type type = this.classifiers.get(typeId);
     
     if (type == null) {
       type = JavaTypes.getPrimitiveType(typeId);
       
       if (type == null && !GenericUtils.isEmptyString(typeId)) {
         type = UMLFactory.eINSTANCE.createClass(); // Anonymous Type
         type.setName(typeId);
       }
       
       // The type can be null (for constructors)
     }
     
     if (!GenericUtils.isEmptyString(collectionType)) {
       type = JavaTypes.getCollectionType(collectionType, type);
     }
     
     return type;
   }
   
   /**
    * Gets the cells containing the properties of the given classifier.
    * 
    * @param classifier
    * @return
    * @author Gabriel Leonardo Diaz, 24.03.2014.
    */
   public static List<mxCell> getProperties (mxCell classifier) {
     List<mxCell> properties = new ArrayList<mxCell>();
     
     for (int i = 0; i < classifier.getChildCount(); i++) {
       mxCell child = (mxCell) classifier.getChildAt(i);
       if (parseBoolean(child.getAttribute("attribute"))) {
         for (int j = 0; j < child.getChildCount(); j++) {
           properties.add((mxCell) child.getChildAt(j));
         }
         break;
       }
     }
     
     if (isClass(classifier)) {
       mxCell edge;
       mxCell property;
       
       for (int i = 0; i < classifier.getEdgeCount(); i++) {
         edge = (mxCell) classifier.getEdgeAt(i);
         
         if (!isAssociation(edge)) {
           continue;
         }
         
         for (int j = 0; j < edge.getChildCount(); j++) {
           property = (mxCell) edge.getChildAt(j);
           if (!GenericUtils.equals(property.getAttribute("type"), classifier.getId()) && !GenericUtils.isEmptyString(property.getAttribute("name"))) {
             properties.add(property);
           }
         }
       }
     }
     
     return properties;
   }
   
   /**
    * Gets the literals of the given enumeration.
    * 
    * @param classifier
    * @return
    * @author Gabriel Leonardo Diaz, 24.03.2014.
    */
   public static List<mxCell> getLiterals (mxCell enumerationCell) {
     List<mxCell> literals = new ArrayList<mxCell>();
     
     for (int i = 0; i < enumerationCell.getChildCount(); i++) {
       mxCell child = (mxCell) enumerationCell.getChildAt(i);
       if (parseBoolean(child.getAttribute("attribute"))) {
         for (int j = 0; j < child.getChildCount(); j++) {
           literals.add((mxCell) child.getChildAt(j));
         }
         break;
       }
     }
     
     return literals;
   }
   
   /**
    * Gets the operations of the given classifier.
    * 
    * @param classifier
    * @return
    * @author Gabriel Leonardo Diaz, 24.03.2014.
    */
   public static List<mxCell> getOperations (mxCell classifier) {
     List<mxCell> operations = new ArrayList<mxCell>();
     
     for (int i = 0; i < classifier.getChildCount(); i++) {
       mxCell child = (mxCell) classifier.getChildAt(i);
       if (!parseBoolean(child.getAttribute("attribute"))) {
         for (int j = 0; j < child.getChildCount(); j++) {
           operations.add((mxCell) child.getChildAt(j));
         }
         break;
       }
     }
     
     return operations;
   }
   
   /**
    * Parses the visibility kind from string.
    * 
    * @param visibility
    * @return
    */
   public static VisibilityKind parseVisibility (String visibility) {
     VisibilityKind kind = VisibilityKind.get(visibility);
     if (kind == null) {
       kind = VisibilityKind.PUBLIC_LITERAL;
     }
     return kind;
   }
   
   /**
    * Parses the string to a boolean value.
    * @param booleanValue
    * @return
    */
   public static boolean parseBoolean (String booleanValue) {
     return "1".equals(booleanValue);
   }
   
   /**
    * Parses the string by removing empty strings.
    * @param stringValue
    * @return
    */
   public static String parseString (String stringValue) {
     if (GenericUtils.isEmptyString(stringValue)) {
       return null;
     }
     return stringValue;
   }
   
   /**
    * Checks if the given cell contains a UML Package.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 24.03.2014.
    */
   public static boolean isPackage (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("package");
   }
   
   /**
    * Checks if the given cell contains a UML Class.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   public static boolean isClass (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("class");
   }
   
   /**
    * Checks if the given cell contains a UML Enumeration.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   public static boolean isEnumeration (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("enumeration");
   }
   
   /**
    * Checks if the given cell contains a UML Interface.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 05.03.2014.
    */
   public static boolean isInterface (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("interface");
   }
   
   /**
    * Checks if the given cell contains a Comment.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 25.03.2014.
    */
   public static boolean isComment (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("comment");
   }
   
   /**
    * Checks if the given cell contains a Link Association.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 17.03.2014.
    */
   public static boolean isLink (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equals("link");
   }
   
   /**
    * Checks if the given cell contains a UML Association.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 17.03.2014.
    */
   public static boolean isAssociation (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equals("association");
   }
   
   /**
    * Checks if the given cell contains a UML Generalization.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 19.03.2014.
    */
   public static boolean isGeneralization (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("generalization");
   }
   
   /**
    * Checks if the given cell contains a UML Realization.
    * 
    * @param cell
    * @return
    * @author Gabriel Leonardo Diaz, 19.03.2014.
    */
   public static boolean isRealization (mxCell cell) {
     if (cell == null || !(cell.getValue() instanceof Element)) {
       return false;
     }
     return ((Element) cell.getValue()).getTagName().toLowerCase().equalsIgnoreCase("realization");
   }
   
 }
