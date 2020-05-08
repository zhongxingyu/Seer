 package org.codehaus.xfire.service.binding;
 
 import java.util.Iterator;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.xfire.util.stax.DepthXMLStreamReader;
 
 public class WrappedBinding
     extends AbstractBinding
 {
     public void readMessage(InMessage inMessage, MessageContext context)
         throws XFireFault
     {
         Service endpoint = context.getService();
         
         DepthXMLStreamReader dr = new DepthXMLStreamReader(context.getInMessage().getXMLStreamReader());
 
         if ( !STAXUtils.toNextElement(dr) )
             throw new XFireFault("There must be a method name element.", XFireFault.SENDER);
         
         OperationInfo op = context.getExchange().getOperation();
 
         if (!isClientModeOn(context) && op == null)
         {
             op = endpoint.getServiceInfo().getOperation( dr.getLocalName() );
             
             if (op == null)
             {
                 throw new XFireFault("Invalid operation: " + dr.getName(), XFireFault.SENDER);
             }
 
             setOperation(op, context);
         }
         
         // Move from Operation element to whitespace or start element
         nextEvent(dr);
 
         read(inMessage, context, null);
     }
 
     public void writeMessage(OutMessage message, XMLStreamWriter writer, MessageContext context)
         throws XFireFault
     {
         try
         {
             Service endpoint = context.getService();
             Object[] values = (Object[]) message.getBody();
             
             OperationInfo op = context.getExchange().getOperation();
             String name;
 
             MessageInfo msgInfo;
             boolean client = isClientModeOn(context);
             if (client)
             {
                 name = op.getName();
                 msgInfo = op.getInputMessage();
             }
             else
             {
                 name = op.getName() + "Response";
                 msgInfo = op.getOutputMessage();
             }
 
             writeStartElement(writer, name, msgInfo.getName().getNamespaceURI());
             
             for(Iterator itr = msgInfo.getMessageParts().iterator(); itr.hasNext();)
             {
                 MessagePartInfo outParam = (MessagePartInfo) itr.next();
                 
                 Object value;
                 if (client) 
                     value = getClientParam(values, outParam, context);
                 else 
                     value = getParam(values, outParam, context);
                 
                 writeParameter(writer, context, value, outParam, getBoundNamespace(context, outParam));
             }
     
             writer.writeEndElement();
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't write start element.", e);
         }
     }
 
     public void writeStartElement(XMLStreamWriter writer, String name, String namespace) 
         throws XMLStreamException
     {
         String prefix = "";
       	prefix = NamespaceHelper.getUniquePrefix(writer);
       	
         if (namespace.length() > 0)
         {
             writer.setPrefix(prefix, namespace);
             writer.writeStartElement(prefix, name, namespace);
             writer.writeNamespace(prefix, namespace);
         }
         else
         {
             writer.setDefaultNamespace("");
             writer.writeStartElement(name);
             writer.writeDefaultNamespace("");
         }
     }
 }
