 package org.codehaus.xfire.fault;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.MessageSerializer;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.util.jdom.StaxBuilder;
 import org.codehaus.xfire.util.jdom.StaxSerializer;
 import org.codehaus.xfire.util.stax.FragmentStreamReader;
 import org.jdom.Element;
 
 public class Soap11FaultSerializer
     implements MessageSerializer
 {
  //  private StaxBuilder builder = new StaxBuilder();
     
     public void readMessage(InMessage message, MessageContext context)
         throws XFireFault
     {
         XFireFault fault = new XFireFault();
 
         XMLStreamReader reader = message.getXMLStreamReader();
 
         try
         {
             boolean end = false;
             while (!end && reader.hasNext())
             {
                 int event = reader.next();
                 switch (event)
                 {
                     case XMLStreamReader.START_DOCUMENT:
                         String encoding = reader.getCharacterEncodingScheme();
                         message.setEncoding(encoding);
                         break;
                     case XMLStreamReader.END_DOCUMENT:
                         end = true;
                         break;
                     case XMLStreamReader.END_ELEMENT:
                         break;
                     case XMLStreamReader.START_ELEMENT:
                         if (reader.getLocalName().equals("faultcode"))
                         {
                             fault.setFaultCode(NamespaceHelper.readQName(reader));
                         }
                         else if (reader.getLocalName().equals("faultstring"))
                         {
                             fault.setMessage(reader.getElementText());
                         }
                         else if (reader.getLocalName().equals("faultactor"))
                         {
                             fault.setRole(reader.getElementText());
                         }
                         else if (reader.getLocalName().equals("detail"))
                         {
                            StaxBuilder builder = new StaxBuilder();
                             fault.setDetail(builder.build(new FragmentStreamReader(reader)).getRootElement());
                         }
                         break;
                     default:
                         break;
                 }
             }
         }
         catch (XMLStreamException e)
         {
             throw new XFireFault("Could not parse message.", e, XFireFault.SENDER);
         }
         message.setBody(fault);
     }
 
 
     public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context)
         throws XFireFault
     {
         XFireFault fault = (XFireFault) message.getBody();
 
         try
         {
             Map namespaces = fault.getNamespaces();
             for (Iterator itr = namespaces.keySet().iterator(); itr.hasNext();)
             {
                 String prefix = (String) itr.next();
                 writer.writeAttribute("xmlns:" + prefix, (String) namespaces.get(prefix));
             }
 
             writer.writeStartElement("soap:Fault");
 
             writer.writeStartElement("faultcode");
 
             QName faultCode = fault.getFaultCode();
             String codeString;
             if (faultCode.equals(XFireFault.RECEIVER))
             {
                 codeString = "soap:Server";
             }
             else if (faultCode.equals(XFireFault.SENDER))
             {
                codeString = "soap:Client";
             }
             else if (faultCode.equals(XFireFault.VERSION_MISMATCH))
             {
                 codeString = "soap:VersionMismatch";
             }
             else if (faultCode.equals(XFireFault.MUST_UNDERSTAND))
             {
                 codeString = "soap:MustUnderstand";
             }
             else if (faultCode.equals(XFireFault.DATA_ENCODING_UNKNOWN))
             {
                 codeString = "soap:Client";
             }
             else
             {
                 String ns = faultCode.getNamespaceURI();
                 String prefix = "";
                 if (ns.length() > 0)
                 {
                     prefix = NamespaceHelper.getUniquePrefix(writer, ns, true) + ":";
                 }
                 
                 codeString = prefix + faultCode.getLocalPart();
             }
 
             writer.writeCharacters(codeString);
             writer.writeEndElement();
 
             writer.writeStartElement("faultstring");
             writer.writeCharacters(fault.getMessage());
             writer.writeEndElement();
 
             
             if (fault.hasDetails())
             {
                 Element detail = fault.getDetail();
 
                 writer.writeStartElement("detail");
                 
                 StaxSerializer serializer = new StaxSerializer();
                 List details = detail.getContent();
                 for (int i = 0; i < details.size(); i++)
                 {
                     serializer.writeElement((Element) details.get(i), writer);
                 }
 
                 writer.writeEndElement(); // Details
             }
 
             if (fault.getRole() != null)
             {
                 writer.writeStartElement("faultactor");
                 writer.writeCharacters(fault.getRole());
                 writer.writeEndElement();
             }
 
             writer.writeEndElement(); // Fault
         }
         catch (XMLStreamException xe)
         {
             throw new XFireRuntimeException("Couldn't create fault.", xe);
         }
     }
 }
