 package net.dovemq.gen;
 
 import java.io.IOException;
 
 import org.jdom.Element;
 import org.jdom.JDOMException;
 
 class AMQPExampleReader
 {
     private static final String AMQP_TYPES_XML_FILE_NAME = "types.xml";
     private static final String AMQP_TRANSPORT_XML_FILE_NAME = "transport.xml";
     private static final String AMQP_MESSAGING_XML_FILE_NAME = "messaging.xml";
     private static final String AMQP_EXAMPLE_XML_FILE_NAME = "example.xml";
     private static final String AMQP_XML_FILES_LOCATION = "/Users/tejeswardas/camqp/spec";
     
    protected static final String PACKAGE_NAME = "net.dovemq.transport.protocol.data";  
     protected static String gDirectory = null;
     
     /**
      * @param args
      * @throws IOException 
      * @throws JDOMException 
      */
     public static void main(String[] args) throws JDOMException, IOException
     {
         AMQPPrimitiveTypeMappings.initialize();
         
         AMQPXMLReader.gPrimitiveTypes = new AMQPGenericTypeContainer();
         AMQPXMLReader.gRestrictedTypes = new AMQPGenericTypeContainer();
         AMQPXMLReader.gDefinitionsCompositeTypes = new AMQPGenericTypeContainer();
         AMQPXMLReader.gCompositeTypes = new AMQPGenericTypeContainer();
         
         String baseDir = args[0];
         gDirectory = Utils.convertPackageNameToDir(baseDir, PACKAGE_NAME);
         System.out.println(gDirectory);
 
         String typesXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_TYPES_XML_FILE_NAME);
         Element typesRootElement = AMQPXMLReader.getRootElement(typesXMLFileName);
         AMQPXMLReader.processTopLevelElement(typesRootElement, "encodings");
         
         /*
          * parse transport.xml
          */
         String transportXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_TRANSPORT_XML_FILE_NAME);
         Element transportRootElement = AMQPXMLReader.getRootElement(transportXMLFileName);
         AMQPXMLReader.processTopLevelElement(transportRootElement, "definitions");
         AMQPXMLReader.processTopLevelElement(transportRootElement, "frame-bodies");
         
         String messagingXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_MESSAGING_XML_FILE_NAME);
         Element messagingRootElement = AMQPXMLReader.getRootElement(messagingXMLFileName);
         AMQPXMLReader.processTopLevelElement(messagingRootElement, "message-format");
         AMQPXMLReader.processTopLevelElement(messagingRootElement, "transfer-state");
         AMQPXMLReader.processTopLevelElement(messagingRootElement, "addressing");
         
         
         String exampleXMLFileName = String.format("%s/%s", AMQP_XML_FILES_LOCATION, AMQP_EXAMPLE_XML_FILE_NAME);
         Element exampleRootElement = AMQPXMLReader.getRootElement(exampleXMLFileName);
         AMQPXMLReader.processTopLevelElement(exampleRootElement, "composite-types");
 
         /*
          * Generate code
          */
         AMQPXMLReader.gCompositeTypes.generateClassFile(gDirectory, "book"); 
     }    
 }
