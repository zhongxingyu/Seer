 package fr.cg95.cvq.generator.plugins.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.generator.ApplicationDocumentation;
 import fr.cg95.cvq.generator.ElementProperties;
 import fr.cg95.cvq.generator.IPluginGenerator;
 import fr.cg95.cvq.generator.plugins.tool.VelocityManager;
 
 /**
  * An in-memory representation of the to-be-generated request.
  *
  * An instance of this class is filled by the model plugin as it receives
  * events from the generator. Upon completion of the parsing, the model plugin
  * asks this class to generate the effective code.
  *
  * @author bor@zenexity.fr
  */
 public class ModelRequestObject {
 
     private static Logger logger = Logger.getLogger(ModelRequestObject.class);
 
     private static final String BUSINESS_OBJECTS_PREFIX_DIR = "/business/request/";
     private static final String VELOCITY_BASE_DIR = 
         "Generator/src/java/fr/cg95/cvq/generator/plugins/model/";
     
     /**
      * The base directory in which we generate business objects.
      * Configurable in Model plugin configuration file (src/xml/plugins/model_plugin.xml)
      */
     private String outputDir;
 
     /**
      * The base directory in which we generate services test classes
      * Configurable in Model plugin configuration file (src/xml/plugins/model_plugin.xml)
      */
     private String outputTestDir;
 
     /** the name of the request being generated */
     private String requestName;
 
     /** the last particle of the namespace in which business object is generated
      * (eg school) */
     private String requestNamespaceLastParticle;
 
     /** the buffer in which we are generating code */
     private StringBuffer currentSb;
 
     /** a map of first-level elements with their properties */
     private HashMap<String, ElementModelProperties> elementsPropertiesMap;
 
     /** a map of first-level elements with their model annotations */
     private HashMap<String, ApplicationDocumentation> elementsModelInfoMap;
 
     /** the complex types defined locally (ie in the current request) */
     private HashMap<String, ComplexType> complexTypesMap;
 
     public ModelRequestObject() {
         elementsPropertiesMap = new HashMap<String, ElementModelProperties>();
         elementsModelInfoMap = new HashMap<String, ApplicationDocumentation>();
         complexTypesMap = new HashMap<String, ComplexType>();
     }
 
     /**
      * Add a first-level element (ie an element of the request itself)
      *
      * @param elementName name of the field to be added
      * @param eltProperties properties associated to this element
      * @param complexContainerElementTypeName for elements defined within a local complex type,
      *        the type name of this complex type
      * @param complexContainerElementName for elements defined within a local complex type,
      *        the name of this complex type
      */
     public void addField(String elementName, ElementProperties eltProperties,
             String complexContainerElementTypeName, String complexContainerElementName) {
 
         ElementModelProperties eltModelProp = new ElementModelProperties(eltProperties);
         eltModelProp.setElementName(elementName);
 
         if (complexContainerElementTypeName != null) {
             eltModelProp.setComplexContainerElementTypeName(complexContainerElementTypeName);
             eltModelProp.setComplexContainerElementName(complexContainerElementName);
         }
         eltModelProp.setNameAsParam(StringUtils.uncapitalize(elementName));
         elementsPropertiesMap.put(elementName, eltModelProp);
 
         processElementApplicationDocumentation(eltModelProp, elementName);
     }
 
     /**
      * Remove a first-level field/element from the current request
      */
     public void removeField(String elementName) {
         elementsPropertiesMap.remove(elementName);
     }
 
     /**
      * Get the properties of the given field
      *
      * @return the {@link ElementModelProperties} object associated to this field
      */
     public ElementModelProperties getField(String elementName) {
         return elementsPropertiesMap.get(elementName);
     }
 
     /**
      * Add documentation information for the given first-level element.
      *
      * @param elementName name of the field to be added
      * @param appDocumentation documentation information associated with the field
      */
     public void addFieldInfo(String elementName, ApplicationDocumentation appDocumentation) {
         elementsModelInfoMap.put(elementName, appDocumentation);
     }
 
     /**
      * Add the definition of a complex type local to the request (ie it is defined
      * in the same XSD file)
      *
      * @param typeName name of the "local" complex type
      * @param eltProperties properties associated to this complex type
      */
     public void addComplexType(String typeName, ElementProperties eltProperties) {
         ComplexType complexType = new ComplexType();
         complexType.setTypeName(StringUtils.removeEnd(typeName,"Type"));
         ElementModelProperties eltModelProperties = new ElementModelProperties(eltProperties);
         complexType.setProperties(eltModelProperties);
 
         complexTypesMap.put(typeName, complexType);
     }
 
     private void processElementApplicationDocumentation(ElementModelProperties eltModelProperties,
             final String elementName) {
         ApplicationDocumentation appDocumentation = elementsModelInfoMap.get(elementName);
         if (appDocumentation == null) {
             // no namespace definition override in application documentation
             // so it defaults to the current request namespace
             eltModelProperties.setNamespaceLastParticle(requestNamespaceLastParticle);
             eltModelProperties.setJavaPackageName(IPluginGenerator.MODEL_BASE_TARGET_NS + ".request."
                                             + requestNamespaceLastParticle + ".");
         } else {
             String namespaceLastParticle =
                 ApplicationDocumentation.getNodeAttributeValue(appDocumentation.getXmlNode(), "namespace");
             logger.debug("processElementApplicationDocumentation() namespace last particle " 
                     + namespaceLastParticle);
 
             eltModelProperties.setNamespaceLastParticle(namespaceLastParticle);
 
             // if it comes from the referential, namespace is provided within the XML schema file
             // else it belongs to the current request's namespace
             if (namespaceLastParticle.equals(requestNamespaceLastParticle ))
                 eltModelProperties.setJavaPackageName(IPluginGenerator.MODEL_BASE_TARGET_NS + ".request."
                                                 + namespaceLastParticle + ".");
             else if (namespaceLastParticle != null)
                 eltModelProperties.setJavaPackageName(IPluginGenerator.MODEL_BASE_TARGET_NS + "."
                                                 + namespaceLastParticle + ".");
             else if (eltModelProperties.isRequestType())
                 eltModelProperties.setJavaPackageName(IPluginGenerator.MODEL_BASE_TARGET_NS + "."
                                                 + requestNamespaceLastParticle + ".");
             else
                 eltModelProperties.setJavaPackageName("");
             
             String isTiedToRequest =
                 ApplicationDocumentation.getNodeAttributeValue(appDocumentation.getXmlNode(), "isTiedToRequest");
             if (isTiedToRequest == null || !isTiedToRequest.equals("true"))
                 eltModelProperties.setTiedToRequest(false);
             else
                 eltModelProperties.setTiedToRequest(true);
         }
     }
     
     /**
      * Add the definition of an element to a local complex type.
      *
      * @param complexTypeName name of the local complex type for which we want to add an
      *                 element's definition
      * @param elementName name of the element to be added
      * @param eltProperties properties of the element to be added
      */
     public void addElementToComplexType(String complexTypeName, String elementName,
             ElementProperties eltProperties) {
 
         ComplexType complexType = complexTypesMap.get(complexTypeName);
         ElementModelProperties eltModelProperties = new ElementModelProperties(eltProperties);
         eltModelProperties.setNameAsParam(StringUtils.uncapitalize(elementName));
         
         processElementApplicationDocumentation(eltModelProperties, elementName);
  
         complexType.addElement(elementName, eltModelProperties);
     }
 
     private String transformNamespaceLastParticle(final String namespaceLastParticle) {
         return namespaceLastParticle.replace('.','/');
     }
 
     /**
      * Utility method that creates and opens a file aimed at receiving the contents
      * of a business object
      */
     private void writeBusinessObjectFile(String baseFileName, byte[] data) {
         String directory = outputDir + BUSINESS_OBJECTS_PREFIX_DIR
             + transformNamespaceLastParticle(requestNamespaceLastParticle);
         String filename = directory + "/" + baseFileName + ".java";
 
         // create directories on the path if they do not exist
         File dirPath = new File(directory);
         dirPath.mkdirs();
 
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(filename);
             fos.write(data);
         } catch (FileNotFoundException fnfe) {
             logger.error("writeBusinessObjectFile() unable to create file : " + filename);
         } catch (IOException ioe) {
             logger.error("writeBusinessObjectFile() error while writing business object " + ioe.getMessage());
         }
 
         logger.debug("writeBusinessObjectFile() successfully wrote file : " + filename);
     }
 
     /**
      * Entry point for the generation of files associated to a request.
      * Call respectively the generation of business objects and a sample service test class.
      */
     public void generateRequest() {
         generateBusinessObjects();
         generateSampleServiceTestClass();
 
         // reset local maps for the next request
         elementsPropertiesMap.clear();
         elementsModelInfoMap.clear();
         complexTypesMap.clear();
     }
 
     /**
      * Generate a base service test class and an empty class where to feed request
      * with specific data.
      */
     private void generateSampleServiceTestClass() {
 
         HashMap contextsObjects = new HashMap();
         contextsObjects.put("request", this);
 
         String testFileName = requestName + "ServiceTest.java";
         String testFileBaseDir = outputTestDir + "/"
             + transformNamespaceLastParticle(requestNamespaceLastParticle);
         String velocityFileName = VELOCITY_BASE_DIR + "ServiceTest.vm";
         VelocityManager.launchGeneration(velocityFileName, testFileName, 
                 testFileBaseDir, null, contextsObjects);
         
         testFileName = requestName + "Feeder.java";
         testFileBaseDir = outputTestDir + "/"
             + transformNamespaceLastParticle(requestNamespaceLastParticle);
         File javaFile = new File(testFileBaseDir, testFileName);
         if (javaFile.exists()) {
             logger.debug("generateSampleServiceTestClass() Request feeder class already exists, ignoring");
         } else {
             velocityFileName = VELOCITY_BASE_DIR + "RequestFeeder.vm";
             VelocityManager.launchGeneration(velocityFileName, 
                     testFileName, testFileBaseDir, null, contextsObjects);
         }
     }
 
     /**
      * Generate business objects associated to a request : the business object for
      * the request itself but also the business objects for the local complext types.
      */
     private void generateBusinessObjects() {
 
         logger.debug("generateBusinessObjects() Starting request generation ...");
 
         currentSb = new StringBuffer();
 
         generateRequestHeader();
         generateConstructor(requestName, elementsPropertiesMap);
         generateConversionMethod(requestName, elementsPropertiesMap,
                 elementsModelInfoMap, true, true);
 
         // print fields declaration and accessors
         for (String elementName : elementsPropertiesMap.keySet()) {
             ElementModelProperties eltModelProperties = elementsPropertiesMap.get(elementName);
             generateElementField(elementName, eltModelProperties);
         }
 
         generateRequestFooter();
 
         // write generated business object to the disk
         writeBusinessObjectFile(requestName, currentSb.toString().getBytes());
 
         logger.debug("generateBusinessObjects() Finished generating request, dealing with local complex types ...");
 
         // now, generate independently classes associated to local complex types
         for (String typeName : complexTypesMap.keySet()) {
             logger.debug("generateBusinessObjects() Generating local complex type : " + typeName);
             ComplexType complexType = complexTypesMap.get(typeName);
             currentSb = new StringBuffer();
 
             generateLocalComplexTypeHeader(complexType);
             generateConstructor(complexType.getTypeName(), 
                     complexType.getElementsModelPropertiesMap());
             generateConversionMethod(complexType.getTypeName(),
                     complexType.getElementsModelPropertiesMap(), null, false, false);
             generateId();
             Set subElements = complexType.getElementsNames();
             Iterator subElementsIt = subElements.iterator();
             while (subElementsIt.hasNext()) {
                 String elementName = (String) subElementsIt.next();
                 ElementModelProperties eltModelProperties =
                     complexType.getElementModelProperties(elementName);
 
                 generateElementField(elementName, eltModelProperties);
             }
 
             generateLocalComplexTypeFooter();
 
             writeBusinessObjectFile(complexType.getTypeName(), currentSb.toString().getBytes());
         }
     }
 
     /**
      * Generate the header part of a request's associated business object
      */
     private void generateRequestHeader() {
 
         // print general information (package, imports and class declaration)
         currentSb.append("package " + IPluginGenerator.MODEL_BASE_TARGET_NS + ".request." + requestNamespaceLastParticle + ";\n\n");
         currentSb.append("import fr.cg95.cvq.business.request.*;\n");
         currentSb.append("import fr.cg95.cvq.business.users.*;\n");
         currentSb.append("import fr.cg95.cvq.business.authority.*;\n");
         currentSb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".common.*;\n");
         currentSb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".request." + requestNamespaceLastParticle + ".*;\n\n");
         currentSb.append("import org.apache.xmlbeans.XmlOptions;\n");
         currentSb.append("import org.apache.xmlbeans.XmlObject;\n\n");
         currentSb.append("import fr.cg95.cvq.xml.common.RequestType;\n\n");
         currentSb.append("import java.io.Serializable;\n");
         currentSb.append("import java.math.BigInteger;\n");
         currentSb.append("import java.util.*;\n\n");
         currentSb.append("/**\n");
         currentSb.append(" * Generated class file, do not edit !\n");
         currentSb.append(" *\n");
         currentSb.append(" * @hibernate.joined-subclass\n");
         currentSb.append(" *  table=\"" + getSQLName(requestName) + "\"\n");
         currentSb.append(" *  lazy=\"false\"\n");
         currentSb.append(" * @hibernate.joined-subclass-key\n");
         currentSb.append(" *  column=\"id\"\n");
         currentSb.append(" */\n");
         currentSb.append("public class " + requestName + " extends Request implements Serializable { \n\n");
         currentSb.append("    private static final long serialVersionUID = 1L;\n\n");
     }
 
     /**
      * Generate the footer part of a request's associated business object
      */
     private void generateRequestFooter() {
         currentSb.append("}\n");
     }
 
     /**
      * Generate the constructor method for requests and local complex types.
      * Fields that have default values defined in XML Schema file are also
      * initialized here
      *
      * @param className name of the class being generated
      * @param elementsPropertiesMap properties associated to the class (request or
      *        or local complex type) being generated
      */
    private void generateConstructor(String className, HashMap elementsPropertiesMap) {
 
         currentSb.append("\n\n");
         currentSb.append("    public " + className + "() {\n");
         currentSb.append("        super();\n");
 
        Set elementsSet = elementsPropertiesMap.keySet();
        Iterator elementsIt = elementsSet.iterator();
        while (elementsIt.hasNext()) {
            String elementName = (String) elementsIt.next();
            ElementModelProperties eltProperties =
                (ElementModelProperties) elementsPropertiesMap.get(elementName);
             if (eltProperties.getDefaultValue() != null) {
                 // currently only identified two cases where default values have
                 // a sense :
                 //     -> a string with enumerations
                 //     -> a boolean
                 String xmlBeanType = eltProperties.getXmlBeanType();
                 if (xmlBeanType.indexOf("XmlString") != -1
                     && eltProperties.getEnumValues() != null) {
                     String type = eltProperties.getJavaPackageName();
                     if (eltProperties.getXmlSchemaType() != null)
                         type += eltProperties.getXmlSchemaType();
                     else
                         type += elementName;
                    currentSb.append("        " + eltProperties.getNameAsParam() + " = " + type + "." + eltProperties.getDefaultValue().toUpperCase() + ";\n");
                 } else if (xmlBeanType.indexOf("XmlBoolean") != -1) {
                    currentSb.append("        " + eltProperties.getNameAsParam() + " = Boolean.valueOf(" + eltProperties.getDefaultValue() + ");\n");
                 }
             }
         }
 
         currentSb.append("    }\n");
         currentSb.append("\n\n");
     }
 
     public void generateConversionMethod(String typeName, HashMap elementsPropertiesMap,
             HashMap elementsModelInfoMap, boolean inherits, boolean isDocument) {
 
         String xmlBeansReturnType = null;
         if (isDocument)
             xmlBeansReturnType = typeName + "Document";
         else
             xmlBeansReturnType = typeName + "Type";
         String xmlBeansDocInstance = StringUtils.uncapitalize(typeName) + "Doc";
         String xmlBeansReturnInstance = StringUtils.uncapitalize(typeName);
 
         currentSb.append("    public final String modelToXmlString() {\n\n");
         currentSb.append("        " + xmlBeansReturnType + " object = (" + xmlBeansReturnType + ") this.modelToXml();\n");
         currentSb.append("        XmlOptions opts = new XmlOptions();\n");
         currentSb.append("        opts.setSavePrettyPrint();\n");
         currentSb.append("        opts.setSavePrettyPrintIndent(4);\n");
         currentSb.append("        opts.setUseDefaultNamespace();\n");
         currentSb.append("        opts.setCharacterEncoding(\"UTF-8\");\n");
         currentSb.append("        return object.xmlText(opts);\n");
         currentSb.append("    }\n\n");
 
         //////////////////////////////////////////////////////////
         // start with Model -> XMLBeans conversion              //
         //////////////////////////////////////////////////////////
 
         currentSb.append("    public final XmlObject modelToXml() {\n\n");
         currentSb.append("        Calendar calendar = Calendar.getInstance();\n");
         currentSb.append("        Date date = null;\n");
         if (isDocument) {
             currentSb.append("        " + xmlBeansReturnType + " "
                              + xmlBeansDocInstance
                              + " = " + xmlBeansReturnType + ".Factory.newInstance();\n");
             currentSb.append("        " + xmlBeansReturnType + "." + typeName + " " + xmlBeansReturnInstance + " = " + xmlBeansDocInstance + ".addNew" + typeName + "();\n");
         } else {
             currentSb.append("        " + xmlBeansReturnType + " "
                              + xmlBeansReturnInstance
                              + " = " + xmlBeansReturnType + ".Factory.newInstance();\n");
         }
         if (inherits) {
             currentSb.append("        super.fillCommonXmlInfo(" + xmlBeansReturnInstance + ");\n");
         }
 
         Set elementsSet = elementsPropertiesMap.keySet();
         Iterator elementsIt = elementsSet.iterator();
         // keep a list of already declared local complex types variables
         // to ensure unique instanciation
         Set localComplexTypesSet = new HashSet();
         boolean alreadyDefinedCpt = false;
         while (elementsIt.hasNext()) {
             String elementName = (String) elementsIt.next();
             ElementModelProperties eltProperties =
                 (ElementModelProperties) elementsPropertiesMap.get(elementName);
 
             String objectToSetProperty = null;
             // a type (complex or simple) defined inside a local "exploded" complex type
             // as required by XML Beans API, create an instance of the local complex type
             if (eltProperties.getComplexContainerElementName() != null) {
                 String complexTypeName = eltProperties.getComplexContainerElementTypeName();
                 String complexTypeInstanceName = null;
                 // append element name to the end of the instance name
                 // to handle the case where a complex type is used more than
                 // once inside a request
                 if (eltProperties.isChoiceElement())
                     complexTypeInstanceName = StringUtils.uncapitalize(complexTypeName) + elementName;
                 else
                     complexTypeInstanceName = StringUtils.uncapitalize(complexTypeName) + eltProperties.getComplexContainerElementName();
 
                 // declare the complex type instance only once
                 if (!localComplexTypesSet.contains(complexTypeInstanceName)) {
                     localComplexTypesSet.add(complexTypeInstanceName);
 
                     if (eltProperties.isChoiceElement()) {
                         currentSb.append("        " + complexTypeName  + " " + complexTypeInstanceName + " = " + xmlBeansReturnInstance + ".addNew" + elementName + "();\n");
                     } else {
                         currentSb.append("        " + complexTypeName + " " + complexTypeInstanceName + " = " + xmlBeansReturnInstance + ".addNew" + eltProperties.getComplexContainerElementName() + "();\n");
                     }
                 }
 
                 if (eltProperties.isChoiceElement()) {
                     ApplicationDocumentation appDoc =
                         (ApplicationDocumentation) elementsModelInfoMap.get(elementName);
                     Node[] choiceNodes = appDoc.getChildrenNodes("choice");
                     Node choiceNode = choiceNodes[0];
                     elementName = ApplicationDocumentation.getNodeAttributeValue(choiceNode, "key");
                 }
 
                 objectToSetProperty = complexTypeInstanceName;
             } else {
                 objectToSetProperty = xmlBeansReturnInstance;
             }
 
             if (eltProperties.isSimpleType()) {
 
                 String xmlBeanType = eltProperties.getXmlBeanType();
                 if (xmlBeanType.indexOf("XmlLong") != -1) {
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(this." + eltProperties.getNameAsParam() + ".longValue());\n");
                 } else if (xmlBeanType.indexOf("XmlDouble") != -1) {
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(this." + eltProperties.getNameAsParam() + ".doubleValue());\n");
                 } else if (xmlBeanType.indexOf("XmlString") != -1
                            || xmlBeanType.indexOf("XmlToken") != -1) {
                     if (eltProperties.getEnumValues() == null) {
                         currentSb.append("        " + objectToSetProperty + ".set" + elementName + "(this." + eltProperties.getNameAsParam() + ");\n");
                     } else {
                         currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                         String packageName = eltProperties.getXmlBeansPackageName() + "."
                         + eltProperties.getXmlSchemaType();
                         currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(" + packageName + ".Enum.forString(this." + eltProperties.getNameAsParam() + ".toString()));\n");
                     }
                 } else if (xmlBeanType.indexOf("XmlDate") != -1) {
                     currentSb.append("        date = this." + eltProperties.getNameAsParam() + ";\n");
                     currentSb.append("        if (date != null) {\n");
                     currentSb.append("            calendar.setTime(date);\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(calendar);\n");
                     currentSb.append("        }\n");
                 } else if (xmlBeanType.indexOf("XmlBoolean") != -1) {
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(this." + eltProperties.getNameAsParam() + ".booleanValue());\n");
                 } else if (eltProperties.isListType()) {
                     if (!alreadyDefinedCpt) {
                         currentSb.append("        int i = 0;\n");
                         alreadyDefinedCpt = true;
                     }
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null) {\n");
                     currentSb.append("            String[] splittedValues = this." + eltProperties.getNameAsParam() + ".split(\" \");\n");
                     currentSb.append("            List " + eltProperties.getNameAsParam() + "List = new ArrayList();\n");
                     currentSb.append("            for (i = 0; i < splittedValues.length; i++)\n");
                     currentSb.append("                " + eltProperties.getNameAsParam() + "List.add(splittedValues[i]);\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(" + eltProperties.getNameAsParam() + "List);\n");
                     currentSb.append("        }\n");
                 } else if (xmlBeanType.indexOf("XmlPositiveInteger") != -1) {
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(new BigInteger(this." + eltProperties.getNameAsParam() + ".toString()));\n");
                 }
             } else {
                 // case of a complex type : ask it to generate its elements
                 if (eltProperties.getMaxOccurs() != null
                     && eltProperties.getMaxOccurs().intValue() == 1) {
                     currentSb.append("        if (this." + eltProperties.getNameAsParam() + " != null)\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "(" + eltProperties.getModelClassName() + ".modelToXml(this." + eltProperties.getNameAsParam() + "));\n");
                 } else {
                     String iteratorName = eltProperties.getNameAsParam() + "It";
                     String tabName = eltProperties.getNameAsParam() + "TypeTab";
                     if (!alreadyDefinedCpt) {
                         currentSb.append("        int i = 0;\n");
                         alreadyDefinedCpt = true;
                     } else {
                         currentSb.append("        i = 0;\n");
                     }
                     currentSb.append("        if (" + eltProperties.getNameAsParam() + " != null) {\n");
                     currentSb.append("            " + eltProperties.getXmlBeansPackageName() + "." + eltProperties.getXmlSchemaType() + "[] " + tabName + " = new " + eltProperties.getXmlBeansPackageName() + "." + eltProperties.getXmlSchemaType() + "[" + eltProperties.getNameAsParam() + ".size()];\n");
                     currentSb.append("            Iterator " + iteratorName + " = " + eltProperties.getNameAsParam() + ".iterator();\n");
                     currentSb.append("            while (" + iteratorName + ".hasNext()) {\n");
                     currentSb.append("                " + eltProperties.getModelClassName() + " object = (" + eltProperties.getModelClassName() + ") " + iteratorName + ".next();\n");
 
                     if (!eltProperties.isReferentialType())
                         currentSb.append("                " + tabName + "[i] = (" + eltProperties.getXmlSchemaType() + ") object.modelToXml();\n");
                     else
                         currentSb.append("                " + tabName + "[i] = " + eltProperties.getModelClassName() + ".modelToXml(object);\n");
 
                     currentSb.append("                i = i + 1;\n" );
                     currentSb.append("            }\n");
                     currentSb.append("            " + objectToSetProperty + ".set" + elementName + "Array(" + tabName + ");\n");
                     currentSb.append("        }\n");
                 }
             }
         }
 
         if (isDocument)
             currentSb.append("        return " + xmlBeansDocInstance + ";\n");
         else
             currentSb.append("        return " + xmlBeansReturnInstance + ";\n");
 
         currentSb.append("    }\n\n");
 
         if (isDocument) {
             currentSb.append("    @Override\n");
             currentSb.append("    public RequestType modelToXmlRequest() {\n");
             currentSb.append("        " + xmlBeansReturnType + " " + xmlBeansDocInstance + " =\n"); 
             currentSb.append("            (" + xmlBeansReturnType + ") modelToXml();\n");
             currentSb.append("        return " + xmlBeansDocInstance + ".get" + typeName + "();\n");
             currentSb.append("    }\n\n");
         }
         
         //////////////////////////////////////////////////////////
         // finish with XMLBeans -> Model conversion             //
         //////////////////////////////////////////////////////////
 
         // method declaration
         String modelReturnInstance = StringUtils.uncapitalize(typeName);
         currentSb.append("    public static " + typeName + " xmlToModel(" + xmlBeansReturnType + " " + xmlBeansDocInstance + ") {\n\n");
         String xmlBeansLocalVariable = null;
         if (isDocument) {
             xmlBeansLocalVariable = modelReturnInstance + "Xml";
             currentSb.append("        " + xmlBeansReturnType + "." + typeName + " " + xmlBeansLocalVariable + " = " + xmlBeansDocInstance + ".get" + typeName + "();\n");
         } else {
             xmlBeansLocalVariable = modelReturnInstance + "Doc";
         }
         // set some frequently used variables
         currentSb.append("        Calendar calendar = Calendar.getInstance();\n");
         currentSb.append("        List list = new ArrayList();\n");
 
         currentSb.append("        " + typeName + " " + modelReturnInstance + " = new " + typeName + "();\n");
         if (inherits)
             currentSb.append("        " + modelReturnInstance + ".fillCommonModelInfo(" + modelReturnInstance + "," + xmlBeansLocalVariable + ");\n");
         elementsIt = elementsSet.iterator();
         while (elementsIt.hasNext()) {
             String elementName = (String) elementsIt.next();
             ElementModelProperties eltProperties =
                 (ElementModelProperties) elementsPropertiesMap.get(elementName);
             String xmlBeansVariableAccessor = xmlBeansLocalVariable;
             if (eltProperties.getComplexContainerElementName() != null)
                 xmlBeansVariableAccessor = xmlBeansLocalVariable + ".get" + eltProperties.getComplexContainerElementName() + "()";
 
             if (eltProperties.isSimpleType()) {
 
                 String xmlBeanType = eltProperties.getXmlBeanType();
                 if (xmlBeanType.indexOf("XmlLong") != -1) {
                     // primitive types "long" are initialized with a value of 0, avoid carrying this value since it has
                     // side effects when persisting objects with Hibernate
                     currentSb.append("        if (" + xmlBeansVariableAccessor + ".get" + elementName + "() != 0)\n");
                     currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(new Long(" + xmlBeansVariableAccessor + ".get" + elementName + "()));\n");
                 } else if (xmlBeanType.indexOf("XmlDouble") != -1) {
                     currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(new Double(" + xmlBeansVariableAccessor + ".get" + elementName + "()));\n");
                 } else if (xmlBeanType.indexOf("XmlString") != -1
                            || xmlBeanType.indexOf("XmlToken") != -1) {
                     if (eltProperties.getEnumValues() == null) {
                         // no enum values, a simple string so ...
                         currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(" + xmlBeansVariableAccessor + ".get" + elementName + "());\n");
                     } else {
                         // an enum string
                         currentSb.append("        if (" + xmlBeansVariableAccessor + ".get" + elementName + "() != null)\n");
                         if (eltProperties.getXmlSchemaType() != null)
                             currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getJavaPackageName() + eltProperties.getXmlSchemaType() + ".forString(" + xmlBeansVariableAccessor + ".get" + elementName + "().toString()));\n");
                         else
                             currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getJavaPackageName() + elementName + ".forString(" + xmlBeansVariableAccessor + ".get" + elementName + "().toString()));\n");
                         // Enum value is not set, set its default value instead
                         currentSb.append("        else\n");
                         if (eltProperties.getXmlSchemaType() != null)
                             currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getJavaPackageName() + eltProperties.getXmlSchemaType() + ".getDefault" + eltProperties.getXmlSchemaType() + "());\n");
                         else
                             currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getJavaPackageName() + elementName + ".getDefault" + elementName + "());\n");
                     }
                 } else if (xmlBeanType.indexOf("XmlDate") != -1) {
                     currentSb.append("        calendar = " + xmlBeansVariableAccessor + ".get" + elementName + "();\n");
                     currentSb.append("        if (calendar != null) {\n");
                     currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(calendar.getTime());\n");
                     currentSb.append("        }\n");
                 } else if (xmlBeanType.indexOf("XmlBoolean") != -1) {
                     if (eltProperties.isChoiceElement()) {
                         ApplicationDocumentation appDoc = 
                             (ApplicationDocumentation) elementsModelInfoMap.get(elementName);
                         Node[] choiceNodes = appDoc.getChildrenNodes("choice");
                         Node choiceNode = choiceNodes[0];
                         currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(Boolean.valueOf(" + xmlBeansVariableAccessor + ".get" + elementName + "().get" + ApplicationDocumentation.getNodeAttributeValue(choiceNode, "key") + "()));\n");
                     } else {
                         currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(Boolean.valueOf(" + xmlBeansVariableAccessor + ".get" + elementName + "()));\n");
                     }
                 } else if (eltProperties.isListType()) {
                     currentSb.append("        list = " + xmlBeansVariableAccessor + ".get" + elementName + "();\n");
                     currentSb.append("        if (list != null) {\n");
                     currentSb.append("            String tempString = \"\";\n");
                     currentSb.append("            for (int i=0; i < list.size(); i++) {\n");
                     currentSb.append("                tempString = list.get(i) + \" \" + tempString;\n");
                     currentSb.append("            }\n");
                     currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(tempString);\n");
                     currentSb.append("        }\n");
                 } else if (xmlBeanType.indexOf("XmlPositiveInteger") != -1) {
                     currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(" + xmlBeansVariableAccessor + ".get" + elementName + "());\n");
                 }
             } else {
                 // case of a complex type : ask it to generate its elements
 
                 if (eltProperties.getMaxOccurs() != null
                     && eltProperties.getMaxOccurs().intValue() == 1) {
                     // if max occurs is only one, direct call to complex type
                     currentSb.append("        if (" + xmlBeansVariableAccessor + ".get" + elementName + "() != null)\n");
                     currentSb.append("            " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getModelClassName() + ".xmlToModel(" + xmlBeansVariableAccessor + ".get" + elementName + "()));\n");
                 } else {
                     currentSb.append("        List<" + eltProperties.getJavaPackageName() + eltProperties.getModelClassName() + "> " + eltProperties.getNameAsParam() + "List = new ArrayList<" + eltProperties.getJavaPackageName() + eltProperties.getModelClassName() + "> ();\n");
                     currentSb.append("        if ( " + xmlBeansVariableAccessor + ".sizeOf" + elementName + "Array() > 0) {\n");
                     currentSb.append("            for (int i = 0; i < " + xmlBeansVariableAccessor + ".get" + elementName + "Array().length; i++) {\n");
                     currentSb.append("                " + eltProperties.getNameAsParam() + "List.add(" + eltProperties.getModelClassName() + ".xmlToModel(" + xmlBeansVariableAccessor + ".get" + elementName + "Array(i)));\n");
                     currentSb.append("            }\n");
                     currentSb.append("        }\n");
                     currentSb.append("        " + modelReturnInstance + ".set" + elementName + "(" + eltProperties.getNameAsParam() + "List);\n");
                 }
             }
         }
         currentSb.append("        return " + StringUtils.uncapitalize(typeName) + ";\n");
         currentSb.append("    }\n\n");
 
     }
 
     /**
      * Id-related methods generation for classes that are not descendent of the
      * request class (typically association classes)
      */
     public void generateId() {
 
         // generate field declaration
         currentSb.append("    private Long id;\n");
         currentSb.append("\n\n");
 
         // generate setter
         currentSb.append("    public final void setId(final Long id) {\n");
         currentSb.append("        this.id = id;\n");
         currentSb.append("    }\n");
         currentSb.append("\n\n");
 
         // generate getter
         currentSb.append("    /**\n");
         currentSb.append("     * @hibernate.id\n");
         currentSb.append("     *  column=\"id\"\n");
         currentSb.append("     *  generator-class=\"sequence\"\n");
         currentSb.append("     */\n");
         currentSb.append("    public final Long getId() {\n");
         currentSb.append("        return this.id;\n");
         currentSb.append("    }\n");
         currentSb.append("\n");
     }
 
     public void generateElementField(final String elementName,
             final ElementModelProperties eltModelProperties) {
 
         String type = null;
 
         boolean appendJavaPackageName = true;
 
         if (eltModelProperties.isSimpleType()) {
             // enumerated types are mapped into their own class
             // whatever their primitive type is (although we probably are only
             // able to handle string-like primitive types)
             if (eltModelProperties.getEnumValues() != null) {
 
                 type = eltModelProperties.getXmlSchemaType();
 
                 // even more, if they are locally defined types, WE have to
                 // generate their enum class, yeah !
                 // FIXME : locally defined anonymous types should also be flagged as request type
                 if (eltModelProperties.isRequestType()) {
                     generateEnumClass(type, eltModelProperties);
                     appendJavaPackageName = true;
                 }
             } else if (eltModelProperties.isListType()) {
                 type = "String";
                 appendJavaPackageName = false;
             } else {
                 type = eltModelProperties.getJavaType();
                 appendJavaPackageName = false;
             }
         } else if (eltModelProperties.isComplexType()) {
             // if multiplicity is greater than one, use a Set
             // instead of the real type
             if (eltModelProperties.getMaxOccurs() == null
                 || eltModelProperties.getMaxOccurs().intValue() > 1) {
                 type = "List<" + eltModelProperties.getJavaPackageName() + eltModelProperties.getModelClassName() + ">";
                 appendJavaPackageName = false;
             } else {
                 type = StringUtils.removeEnd(eltModelProperties.getXmlSchemaType(), "Type");
             }
         }
 
         String javaFieldName = StringUtils.uncapitalize(elementName);
 
         // generate field declaration
         currentSb.append("    private ");
         if (appendJavaPackageName)
             currentSb.append(eltModelProperties.getJavaPackageName());
         currentSb.append(type + " " + javaFieldName + ";");
         currentSb.append("\n\n");
 
         // generate setter
         currentSb.append("    public final void set" + elementName + "(final ");
         if (appendJavaPackageName)
             currentSb.append(eltModelProperties.getJavaPackageName());
         currentSb.append(type + " " + javaFieldName + ") {\n");
         currentSb.append("        this." + javaFieldName + " = " + javaFieldName + ";\n");
         currentSb.append("    }\n");
         currentSb.append("\n\n");
 
         // generate XDoclet tags
         currentSb.append("    /**\n");
         if (eltModelProperties.isSimpleType()) {
             currentSb.append("     * @hibernate.property\n");
             currentSb.append("     *  column=\"" + getSQLName(elementName) + "\"\n");
             if (eltModelProperties.getXmlBeanType().indexOf("XmlPositiveInteger") != -1)
                 currentSb.append("     *  type=\"serializable\"\n");
             if (eltModelProperties.getMaxLength() > 0)
                 currentSb.append("     *  length=\"" + eltModelProperties.getMaxLength() + "\"\n");
             else if (eltModelProperties.getLength() > 0)
                 currentSb.append("     *  length=\"" + eltModelProperties.getLength() + "\"\n");
         } else if (eltModelProperties.isComplexType()) {
             if (eltModelProperties.isReferentialType()) {
                 if (eltModelProperties.getMaxOccurs() != null
                     && eltModelProperties.getMaxOccurs().intValue() == 1) {
                     // a many-to-one
                     currentSb.append("     * @hibernate.many-to-one\n");
                     if (eltModelProperties.isTiedToRequest())
                         currentSb.append("     *  cascade=\"all\"\n");
                     currentSb.append("     *  column=\"" + getSQLName(elementName) + "_id\"\n");
                     currentSb.append("     *  class=\"" + eltModelProperties.getJavaPackageName() + eltModelProperties.getModelClassName() + "\"\n");
                 } else {
                     // a one-to-many
                     currentSb.append("     * @hibernate.list\n");
                     currentSb.append("     *  inverse=\"false\"\n");
                     currentSb.append("     *  lazy=\"false\"\n");
                     if (eltModelProperties.isTiedToRequest())
                         currentSb.append("     *  cascade=\"all\"\n");
                     currentSb.append("     *  table=\"" + getSQLName(requestName) + "_" + getSQLName(elementName) + "\"\n");
                     currentSb.append("     * @hibernate.key\n");
                     currentSb.append("     *  column=\"" + getSQLName(requestName) + "_id\"\n");
                     currentSb.append("     * @hibernate.list-index\n");
                     currentSb.append("     *  column=\"" + getSQLName(elementName) + "_index\"\n");
                     currentSb.append("     * @hibernate.many-to-many\n");
                     currentSb.append("     *  column=\"" + getSQLName(elementName) + "_id\"\n");
                     currentSb.append("     *  class=\"" + eltModelProperties.getJavaPackageName() + eltModelProperties.getModelClassName() + "\"\n");
                 }
             } else if (eltModelProperties.getMaxOccurs() == null
                        || eltModelProperties.getMaxOccurs().intValue() > 1) {
                 // a one-to-many
                 currentSb.append("     * @hibernate.list\n");
                 currentSb.append("     *  inverse=\"false\"\n");
                 currentSb.append("     *  lazy=\"false\"\n");
                 currentSb.append("     *  cascade=\"all\"\n");
                 currentSb.append("     * @hibernate.key\n");
                 currentSb.append("     *  column=\"" + getSQLName(requestName) + "_id\"\n");
                 currentSb.append("     * @hibernate.list-index\n");
                 currentSb.append("     *  column=\"" + getSQLName(elementName) + "_index\"\n");
                 currentSb.append("     * @hibernate.one-to-many\n");
                 currentSb.append("     *  class=\"" + eltModelProperties.getJavaPackageName() + eltModelProperties.getModelClassName() + "\"\n");
             }
         }
         currentSb.append("     */\n");
 
 
         // generate getter
         currentSb.append("    public final ");
         if (appendJavaPackageName)
             currentSb.append(eltModelProperties.getJavaPackageName());
         currentSb.append(type + " get" + elementName + "() {\n");
         currentSb.append("        return this." + javaFieldName + ";\n");
         currentSb.append("    }\n");
         currentSb.append("\n");
     }
 
     public void generateEnumClass(String className, ElementProperties eltProperties) {
 
         StringBuffer sb = new StringBuffer();
 
         // print general information (package, imports and class declaration)
         sb.append("package fr.cg95.cvq.business.request." + requestNamespaceLastParticle + ";");
         sb.append("\n\n");
         sb.append("import fr.cg95.cvq.dao.hibernate.PersistentStringEnum;\n");
         sb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".common.*;\n");
         sb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".request." + requestNamespaceLastParticle + ".*;\n");
         sb.append("\n");
         sb.append("/**\n");
         sb.append(" *\n");
         sb.append(" * Generated class file, do not edit !\n");
         sb.append(" */\n");
         sb.append("public final class " + className + " extends PersistentStringEnum { \n");
         sb.append("\n");
         sb.append("    private static final long serialVersionUID = 1L;\n\n");
 
         // generate the constant enumerations
         String[] enumValues = eltProperties.getEnumValues();
         for (int i = 0; i < enumValues.length; i++) {
             String enumValue = enumValues[i];
             sb.append("    public static final " + className + " " +
                       getEnumStaticName(enumValue) +
                       " = new " + className + "(\"" + enumValue + "\");\n");
         }
 
         // generate constructors
         sb.append("\n\n");
         sb.append("    /**\n");
         sb.append("     * Prevent instantiation and subclassing with a private constructor.\n");
         sb.append("     */\n");
         sb.append("    private " + className + "(String value) {\n");
         sb.append("       super(value);\n");
         sb.append("    }\n");
         sb.append("\n\n");
         sb.append("    public " + className + "() {}\n");
         sb.append("\n");
 
         // generate the all enum value array
         sb.append("\n\n");
         sb.append("    public static " + className +  "[] all" + className + "s = {\n");
         for (int i = 0; i < enumValues.length; i++) {
             String enumValue = enumValues[i];
             sb.append("        " + getEnumStaticName(enumValue));
             if (i < enumValues.length - 1)
                 sb.append(",");
             sb.append("\n");
         }
         sb.append("    };\n");
         
         // generate the getDefault method
         sb.append("\n\n");
         sb.append("    public static " + className + " getDefault" + className + "() {\n");
         if (eltProperties.getDefaultValue() != null)
             sb.append("        return " + getEnumStaticName(eltProperties.getDefaultValue()) + ";\n");
         else
             sb.append("        return null;\n");
         sb.append("    }\n");
 
         // generate the forString accessor method
         sb.append("\n\n");
         sb.append("    public static " + className + " forString(final String enumAsString) {\n");
         sb.append("        if (enumAsString == null || enumAsString.equals(\"\"))\n");
         sb.append("            return getDefault" + className + "();\n");
         sb.append("\n");
         for (int i = 0; i < enumValues.length; i++) {
             String enumValue = enumValues[i];
             if (i == 0)
                 sb.append("        if (enumAsString.equals(" + getEnumStaticName(enumValue) + ".toString()))\n");
             else
                 sb.append("        else if (enumAsString.equals(" + getEnumStaticName(enumValue) + ".toString()))\n");
             sb.append("            return " + getEnumStaticName(enumValue) + ";\n");
         }
         sb.append("\n");
         sb.append("        return getDefault" + className + "();\n");
         sb.append("    }\n");
 
         sb.append("}\n");
 
         writeBusinessObjectFile(className, sb.toString().getBytes());
     }
 
     private void generateLocalComplexTypeHeader(ComplexType complexType) {
 
         // print general information (package, imports and class declaration)
         currentSb.append("package fr.cg95.cvq.business.request." + requestNamespaceLastParticle + ";");
         currentSb.append("\n\n");
         currentSb.append("import fr.cg95.cvq.business.users.*;\n");
         currentSb.append("import fr.cg95.cvq.business.authority.*;\n");
         currentSb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".common.*;\n");
         currentSb.append("import " + IPluginGenerator.XMLBEANS_BASE_TARGET_NS + ".request." + requestNamespaceLastParticle + ".*;\n");
         currentSb.append("import org.apache.xmlbeans.XmlOptions;\n");
         currentSb.append("import org.apache.xmlbeans.XmlObject;\n\n");
         currentSb.append("import java.io.Serializable;\n");
         currentSb.append("import java.util.*;\n\n");
         currentSb.append("import java.math.BigInteger;\n");
         currentSb.append("\n");
         currentSb.append("/**\n");
         currentSb.append(" * @hibernate.class\n");
         currentSb.append(" *  table=\"" + getSQLName(complexType.getTypeName()) + "\"\n");
         currentSb.append(" *  lazy=\"false\"\n");
         currentSb.append(" *\n");
         currentSb.append(" * Generated class file, do not edit!\n");
         currentSb.append(" */\n");
         currentSb.append("public class " + complexType.getTypeName() + " implements Serializable {\n\n");
         currentSb.append("    private static final long serialVersionUID = 1L;\n\n");
     }
 
     public void generateLocalComplexTypeFooter() {
         currentSb.append("}\n");
     }
 
     /**
      * Transform a given element name (eg StartDate) into a SQL name conform to our
      * convention (eg start_date)
      *
      * @todo : externalize this in an utils class that will handle all of this stuff
      *         of transforming names in other "standard" forms
      */
     public String getSQLName(String elementName) {
         String uncapitalizedName = StringUtils.uncapitalize(elementName);
         StringBuffer result = new StringBuffer();
         for (int i = 0; i < uncapitalizedName.length(); i++) {
             char currentChar = uncapitalizedName.charAt(i);
             if (Character.isUpperCase(currentChar)) {
                 result.append("_").append(Character.toLowerCase(currentChar));
             } else {
                 result.append(currentChar);
             }
         }
 
         return result.toString();
     }
 
     /**
      * Transform a given enum value name (eg FullCopy) into a name conform to our
      * naming convention (eg FULL_COPY)
      *
      * @todo : externalize this in an utils class that will handle all of this stuff
      *         of transforming names in other "standard" forms ?
      */
     public String getEnumStaticName(String enumValue) {
 
         StringBuffer result = new StringBuffer();
         for (int i = 0; i < enumValue.length(); i++) {
             char currentChar = enumValue.charAt(i);
             if (Character.isUpperCase(currentChar) && i > 0) {
                 result.append("_").append(currentChar);
             } else {
                 result.append(Character.toUpperCase(currentChar));
             }
         }
 
         return result.toString();
     }
 
     /**
      * Utility method called from the Velocity template that is in charge of generating
      * unit tests skeletons.
      */
     public String getRandomEnumFromTab(String[] enumValues) {
         return enumValues[0];
     }
     
     private class ComplexType {
 
         private String typeName;
         private ElementModelProperties eltModelProperties;
         private HashMap elementsModelPropertiesMap;
         private HashMap elementsModelInfoMap;
 
         public ComplexType() {
             elementsModelPropertiesMap = new HashMap();
             elementsModelInfoMap = new HashMap();
         }
 
         public void setTypeName(String typeName) {
             this.typeName = typeName;
         }
 
         public String getTypeName() {
             return this.typeName;
         }
 
         public void setProperties(ElementModelProperties eltModelProperties) {
             this.eltModelProperties = eltModelProperties;
         }
 
         public ElementModelProperties getProperties() {
             return this.eltModelProperties;
         }
 
         public void addElement(String elementName, ElementModelProperties eltModelProperties) {
             elementsModelPropertiesMap.put(elementName, eltModelProperties);
         }
 
         public Set getElementsNames() {
             return elementsModelPropertiesMap.keySet();
         }
 
         public ElementModelProperties getElementModelProperties(String elementName) {
             return (ElementModelProperties) elementsModelPropertiesMap.get(elementName);
         }
 
         public HashMap getElementsModelPropertiesMap() {
             return elementsModelPropertiesMap;
         }
 
         public void addElementModelInfo(String elementName,
                                         ApplicationDocumentation appDocumentation) {
             elementsModelInfoMap.put(elementName, appDocumentation);
         }
 
         public ApplicationDocumentation getElementModelInfo(String elementName) {
             return (ApplicationDocumentation) elementsModelInfoMap.get(elementName);
         }
 
         public HashMap getElementsModelInfoMap() {
             return elementsModelInfoMap;
         }
     }
 
     // getters and setters go below
 
     public void setOutputDir(String outputDir) {
         this.outputDir = outputDir;
     }
 
     public void setOutputTestDir(String outputTestDir) {
         this.outputTestDir = outputTestDir;
     }
 
     public void setRequestName(String requestName) {
         this.requestName = requestName;
     }
 
     public String getRequestName() {
         return this.requestName;
     }
 
     public void setRequestNamespaceLastParticle(String requestNamespaceLastParticle) {
         this.requestNamespaceLastParticle = requestNamespaceLastParticle;
     }
 
     public String getRequestNamespaceLastParticle() {
         return this.requestNamespaceLastParticle;
     }
 
     public Map getElementsPropertiesMap() {
         return this.elementsPropertiesMap;
     }
 }
