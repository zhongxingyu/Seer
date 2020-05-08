 package org.codehaus.xfire.fault;
 
import java.util.Iterator;
 import java.util.List;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.SOAPConstants;
 import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.util.STAXUtils;
import org.w3c.dom.Element;
 
 /**
  * Creates a fault message based on an exception for SOAP 1.2 messages.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class SOAP11FaultHandler
 	implements FaultHandler
 {
     public static final String NAME = "1.1";
     
     /**
      * @see org.codehaus.xfire.fault.FaultHandler#handleFault(java.lang.Exception, org.codehaus.xfire.MessageContext)
      */
     public void handleFault( Exception e, 
                              MessageContext context )
     {
         XFireFault fault = createFault(e);
 
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
         XMLStreamWriter writer;
         try
         {
             writer = factory.createXMLStreamWriter( context.getResponseStream() );
             writer.writeStartDocument();
             writer.writeStartElement("soap:Envelope");
             writer.writeAttribute("xmlns:soap", SOAPConstants.SOAP11_ENVELOPE_NS);
             
             writer.writeStartElement("soap:Body");
             writer.writeStartElement("soap:Fault");
 
             writer.writeStartElement("faultcode");
             
             String codeString = fault.getCode();
             if ( codeString.equals( XFireFault.RECEIVER ) )
             {
                 codeString = "Server";
             }
             if ( codeString.equals( XFireFault.SENDER ) )
             {
                 codeString = "Server";
             }
             else if ( codeString.equals( XFireFault.DATA_ENCODING_UNKNOWN ) )
             {
                 codeString = "Client";
             }
             
             writer.writeCharacters( codeString );
             writer.writeEndElement();
             
             writer.writeStartElement("faultstring");
             writer.writeCharacters( fault.getMessage() );
             writer.writeEndElement();
 
             writer.writeStartElement("detail");
             if ( fault.getDetail() != null )
             {
                 List detail = fault.getDetail();
                 
             }
             writer.writeEndElement();
             
             writer.writeEndElement(); // Fault
             writer.writeEndElement(); // Body
             writer.writeEndElement(); // Envelope
             writer.writeEndDocument(); 
         }
         catch (XMLStreamException xe)
         {
             throw new XFireRuntimeException("Couldn't create fault.", xe);
         }
     }
 
 	/**
 	 * @param e
 	 * @return
 	 */
 	private XFireFault createFault(Exception e)
 	{
 		XFireFault fault = XFireFault.createFault(e);
         
 		return fault;
 	}
 
 }
