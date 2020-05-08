 package net.dovemq.gen;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 class AMQPXMLReader
 {
     private static final String AMQP_TYPES_XML_FILE_NAME = "types.xml";
     private static final String AMQP_TRANSPORT_XML_FILE_NAME = "transport.xml";
     private static final String AMQP_MESSAGING_XML_FILE_NAME = "messaging.xml"; 
     private static final String AMQP_SECURITY_XML_FILE_NAME = "security.xml";  
    private static final String AMQP_XML_FILES_LOCATION = System.getenv("HOME") + "/camqp/spec";
     
    protected static final String PACKAGE_NAME = "com.cisco.camqp2.transport.protocol.data";
 
     protected static AMQPGenericTypeContainer gPrimitiveTypes = null;
     protected static AMQPGenericTypeContainer gRestrictedTypes = null;
     protected static AMQPGenericTypeContainer gDefinitionsCompositeTypes = null;
     protected static AMQPGenericTypeContainer gCompositeTypes = null;    
     protected static String gDirectory = null;
     
     /**
      * @param args
      * @throws IOException 
      * @throws JDOMException 
      */
     public static void main(String[] args) throws JDOMException, IOException
     {
         AMQPPrimitiveTypeMappings.initialize();
         
         gPrimitiveTypes = new AMQPGenericTypeContainer();
         gRestrictedTypes = new AMQPGenericTypeContainer();
         gDefinitionsCompositeTypes = new AMQPGenericTypeContainer();
         gCompositeTypes = new AMQPGenericTypeContainer();
         
         String baseDir = args[0];
         gDirectory = Utils.convertPackageNameToDir(baseDir, PACKAGE_NAME);
         System.out.println(gDirectory);
 
         /*
          * parse types.xml
          */
         String typesXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_TYPES_XML_FILE_NAME);
         Element typesRootElement = getRootElement(typesXMLFileName);
         processTopLevelElement(typesRootElement, "encodings");
 
         /*
          * parse transport.xml
          */
         String transportXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_TRANSPORT_XML_FILE_NAME);
         Element transportRootElement = getRootElement(transportXMLFileName);
         processTopLevelElement(transportRootElement, "definitions");
         processTopLevelElement(transportRootElement, "frame-bodies");
         
         /*
          * parse messaging.xml
          */        
         String messagingXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_MESSAGING_XML_FILE_NAME);
         Element messagingRootElement = getRootElement(messagingXMLFileName);
         processTopLevelElement(messagingRootElement, "message-format");
         processTopLevelElement(messagingRootElement, "delivery-state");
         processTopLevelElement(messagingRootElement, "addressing");
         
         /*
          * parse security.xml
          */
         String securityXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_SECURITY_XML_FILE_NAME);
         Element securityRootElement = getRootElement(securityXMLFileName);
         processTopLevelElement(securityRootElement, "sasl");
 
         /*
          * Generate code
          */
         gRestrictedTypes.generateConstants(gDirectory);
         gCompositeTypes.generateClassFile(gDirectory); 
         
         gPrimitiveTypes.generateFormatCodeConstants(gDirectory);
         gPrimitiveTypes.generateAmqpTypeEnums(gDirectory);
     }    
     
     static Element
     getRootElement(String xmlFileName) throws JDOMException, IOException
     {
         SAXBuilder builder = new SAXBuilder();
         Document doc = builder.build(xmlFileName);
         return doc.getRootElement();
     }
     
     static Element
     getTopLevelElement(Element parentElement, String elementName) throws JDOMException, IOException
     {
         @SuppressWarnings("unchecked")
         List<Element> children = (List<Element>) parentElement.getChildren();
         for (Element child : children)
         {
             if (child.getAttributeValue("name").equalsIgnoreCase(elementName))
             {
                 return child;
             }
         }
         String errorMsg = String.format("Element %s not found under %s", elementName, parentElement.getName());
         System.out.println(errorMsg);
         throw new AMQPElementNotFoundExcepiton(errorMsg);
     }
 
     static void
     processTopLevelElement(Element rootElement, String topElementName) throws JDOMException, IOException
     {
         Element topLevelElement = getTopLevelElement(rootElement, topElementName);
         @SuppressWarnings("unchecked")
         List<Element> children = (List<Element>) topLevelElement.getChildren();
         for (Element c : children)
         {
             if (c.getName().equalsIgnoreCase("type"))
             {
                 if (c.getAttributeValue("class").equalsIgnoreCase("composite"))
                 {
                     AMQPCompositeType.CompoundMetaType metaType = AMQPCompositeType.getMetaType(topElementName);
                     AMQPCompositeType compoundType = null;
                     if (AMQPCompositeType.isCompositeTypeList(c))
                     {
                         compoundType = new AMQPListCompositeType(c, metaType);    
                     }
                     else
                     {
                         compoundType = new AMQPMapCompositeType(c, metaType);
                     }
                     
                     if (metaType == AMQPCompositeType.CompoundMetaType.DEFINITION)
                     {
                         gDefinitionsCompositeTypes.putType(compoundType.getName(), compoundType);
                     }
                     gCompositeTypes.putType(compoundType.getName(), compoundType);                        
                 }
                 else if (c.getAttributeValue("class").equalsIgnoreCase("restricted"))
                 {
                     AMQPRestrictedType restrictedType = new AMQPRestrictedType(c);
                     gRestrictedTypes.putType(restrictedType.getName(), restrictedType);
                 }
                 else
                 {
                     AMQPPrimitiveType primitiveType = new AMQPPrimitiveType(c);
                     gPrimitiveTypes.putType(primitiveType.getName(), primitiveType);                    
                 }
             }
         }        
     }
 }
