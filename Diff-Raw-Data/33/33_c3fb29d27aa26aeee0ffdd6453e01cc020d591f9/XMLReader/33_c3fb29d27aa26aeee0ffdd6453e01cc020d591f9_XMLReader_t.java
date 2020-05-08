 package net.dovemq.gen;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 class XMLReader
 {
     private static final String AMQP_TYPES_XML_FILE_NAME = "types.xml";
     private static final String AMQP_XML_FILES_LOCATION = "/Users/tejeswardas/camqp/spec";
     
    protected static final String PACKAGE_NAME = "net.dovemq.transport.protocol.data";
 
     protected static AMQPGenericTypeContainer gPrimitiveTypes = null;
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
 
         String baseDir = args[0];
         gDirectory = Utils.convertPackageNameToDir(baseDir, PACKAGE_NAME);
 
         /*
          * parse types.xml
          */
         String typesXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_TYPES_XML_FILE_NAME);
         Element typesRootElement = getRootElement(typesXMLFileName);
         processTopLevelElement(typesRootElement, "encodings");
     }    
     
     private static Element
     getRootElement(String xmlFileName) throws JDOMException, IOException
     {
         SAXBuilder builder = new SAXBuilder();
         Document doc = builder.build(xmlFileName);
         return doc.getRootElement();
     }
     
     private static Element
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
         throw new AMQPElementNotFoundExcepiton(String.format("Element %s not found under %s", elementName, parentElement.getName()));
     }
 
     private static void
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
              
                 }
                 else if (c.getAttributeValue("class").equalsIgnoreCase("restricted"))
                 {
  
                 }
                 else
                 {
                 	//System.out.println(c.ge)
                     AMQPPrimitiveType primitiveType = new AMQPPrimitiveType(c);
                     primitiveType.display();
                     gPrimitiveTypes.putType(primitiveType.getName(), primitiveType);                    
                 }
             }
         }        
     }
 }
