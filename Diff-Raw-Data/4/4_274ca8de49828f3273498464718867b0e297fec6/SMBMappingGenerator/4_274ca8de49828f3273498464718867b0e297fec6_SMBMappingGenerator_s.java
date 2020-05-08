 package soapproxy.application.mapping;
 
 import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
 import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
 import org.apache.xmlbeans.XmlObject;
 import org.dom4j.dom.DOMElement;
 import org.dom4j.io.DOMWriter;
 import org.dom4j.io.SAXReader;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.*;
 import soapproxy.util.SoapMessageBuilder;
 
 import javax.wsdl.BindingOperation;
 import java.io.StringReader;
 
 @Component
 public class SMBMappingGenerator extends DefaultMappingGenerator {
 
   private SoapMessageBuilder smb;
 
   @Autowired
   private MappingDefaultValuesRepository mappingDefaultValuesRepository;
 
   @Override
  public String getMapping(String wsdlUri, String operation, String jsonSchemaUrl) throws Exception {
     setWsdlUri(wsdlUri);
     setOperation(operation);
     setWsdlContext(new WsdlContext(wsdlUri));
     setJsonSchemaUrl(jsonSchemaUrl);
     setSmb(new SoapMessageBuilder(getWsdlContext()));
     return generateMapping();
   }
 
   @Override
   protected Element getOutputFrame(BindingOperation bindingOperation) throws Exception {
     return generateFrame(getSmb().buildSoapMessageFromOutput(bindingOperation, true), true, getOutputTopic(), null, MessageType.OUTPUT);
   }
 
   @Override
   protected Element getInputFrame(BindingOperation bindingOperation) throws Exception {
     return generateFrame(getSmb().buildSoapMessageFromInput(bindingOperation, true), false, getInputTopic(), getJsonSchemaUrl(), MessageType.INPUT);
   }
 
   public Element generateFrame(String soapMessageTemplate, boolean outgoingOnly, String topic, String schemaLocation, MessageType messageType) throws Exception {
     DOMElement frame = new DOMElement("frame");
     addTopic(frame, outgoingOnly, topic);
     // TODO format might not only be json, but also simple string
     addFormat(frame);
     addSchema(frame, schemaLocation);
     Element mappings = new DOMElement("mappings");
     frame.appendChild(mappings);
 
     addMappings(mappings, getEnvelopeElement(soapMessageTemplate), "", messageType);
 
     return frame;
   }
 
   private void addMappings(Element mappings, Element messageElement, String currentPath, MessageType messageType) {
 
     boolean repeatingElementFlag = false;
     if (!messageElement.hasChildNodes()) {
       return;
     }
 
     NodeList nodeList = messageElement.getChildNodes();
     for (int i = 0; i < nodeList.getLength(); i++) {
       Node node = nodeList.item(i);
       if (node instanceof Comment) {
         if (node.getNodeValue() != null && node.getNodeValue().contains("or more repetitions:")) {
           repeatingElementFlag = true;
         }
       }
       if (node instanceof Element) {
         Element element = (Element) node;
         String path = currentPath + "/" + element.getLocalName();
         if (repeatingElementFlag) {
           // add repeating element
           Element repeatingElement = addRepeatingElementGroup(mappings, path);
           // is it a leaf node?
           if (hasChildElements(element)) {
             // no
             addMappings(repeatingElement, element, path, messageType);
           } else {
             // repeating element that is of a simple type
             addMapping(repeatingElement, null, getGlobalReference(element), messageType);
           }
           // unset flag
           repeatingElementFlag = false;
         } else {
           if (hasChildElements(element)) {
             addMappings(mappings, element, path, messageType);
           } else {
             String defaultValue = getDefaultValue(messageType, path);
             addMapping(mappings, path, getGlobalReference(element), messageType, defaultValue);
           }
         }
       }
     }
   }
 
   private String getDefaultValue(MessageType messageType, String path) {
     if (mappingDefaultValuesRepository != null) {
       return mappingDefaultValuesRepository.getDefaultValue(getWsdlUri(), getOperation(), messageType, path);
     }
     return null;
   }
 
   private boolean hasChildElements(Element element) {
     if (element.hasChildNodes()) {
       NodeList nodeList = element.getChildNodes();
       for (int i = 0; i < nodeList.getLength(); i++) {
         if (nodeList.item(i) instanceof Element) {
           return true;
         }
       }
     }
     return false;  //To change body of created methods use File | Settings | File Templates.
   }
 
   private String getGlobalReference(Element element) {
     if (element.hasAttributeNS(MODEL_REFERENCE_ATTRIBUTE.getNamespaceURI(), MODEL_REFERENCE_ATTRIBUTE.getLocalPart())) {
       return element.getAttributeNodeNS(MODEL_REFERENCE_ATTRIBUTE.getNamespaceURI(), MODEL_REFERENCE_ATTRIBUTE.getLocalPart()).getNodeValue();
     }
     return null;
   }
 
   private Element getBodyElement(String soapMessage) throws Exception {
     XmlObject soapMessageObject = XmlObject.Factory.parse(soapMessage);
     XmlObject bodyObject = SoapUtils.getBodyElement(soapMessageObject, SoapVersion.Soap11);
     return (Element) bodyObject.getDomNode();
   }
 
   private Element getEnvelopeElement(String soapMessage) throws Exception {
     SAXReader reader = new SAXReader();
     org.dom4j.Document soapDocument = reader.read(new StringReader(soapMessage));
 
     // Convert dom4j document to w3c document
     DOMWriter writer = new DOMWriter();
     Document doc2 = writer.write(soapDocument);
 
     return doc2.getDocumentElement();
   }
 
   public SoapMessageBuilder getSmb() {
     return smb;
   }
 
   public void setSmb(SoapMessageBuilder smb) {
     this.smb = smb;
   }
 
   public MappingDefaultValuesRepository getMappingDefaultValuesRepository() {
     return mappingDefaultValuesRepository;
   }
 
   public void setMappingDefaultValuesRepository(MappingDefaultValuesRepository mappingDefaultValuesRepository) {
     this.mappingDefaultValuesRepository = mappingDefaultValuesRepository;
   }
 }
