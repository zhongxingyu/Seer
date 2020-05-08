 package org.codehaus.xfire.xmlbeans.client;
 
 import java.util.ArrayList;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.codehaus.xfire.client.AbstractClientHandler;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.util.STAXUtils;
import org.codehaus.xfire.xmlbeans.XmlBeansFault;
 
 
 /**
  * Handles XmlBeans requests and response for SOAP/REST clients.
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 27, 2004
  */
 public class XMLBeansClientHandler
     extends AbstractClientHandler
 {
     private XmlObject[] request;
     private XmlObject[] response;
     private XmlOptions options;
     
     public XMLBeansClientHandler()
     {
         options = new XmlOptions();
         options.setCharacterEncoding("UTF-8");
     }
     
     public XMLBeansClientHandler(XmlOptions options)
     {
         this.options = options;
     }
     
     /**
      * @return Returns the request.
      */
     public XmlObject[] getRequest()
     {
         return request;
     }
     
     /**
      * @param request The request to set.
      */
     public void setRequest(XmlObject[] request)
     {
         this.request = request;
     }
     
     /**
      * @return Returns the response.
      */
     public XmlObject[] getResponse()
     {
         return response;
     }
     
     /**
      * @param response The response to set.
      */
     public void setResponse(XmlObject[] response)
     {
         this.response = response;
     }
 
     public void writeRequest( XMLStreamWriter writer ) 
     	throws XMLStreamException
     {
         if ( request != null )
         {
 	        for ( int i = 0; i < request.length; i++ )
 	        {
 	            STAXUtils.copy(request[i].newXMLStreamReader(options), writer);
 	        }
         }
     }
 
     
     public void handleResponse(XMLStreamReader reader) 
     	throws XMLStreamException, XFireFault
     {
         try
         {
             ArrayList responseElements = new ArrayList();
             
             boolean more = true;
             int event = reader.getEventType();
             while ( more )
             {
                 switch( event )
                 {
                     case XMLStreamReader.END_DOCUMENT:
                         more = false;
                         break;
                     case XMLStreamReader.START_ELEMENT:
                         responseElements.add(XmlObject.Factory.parse( reader ));
                     case XMLStreamReader.END_ELEMENT:
                         // TODO: there should be a more generic way to do this
                         // so that it works with SOAP and REST at any point
                         // in any stream.
                         if ( reader.getLocalName().equals("Envelope") )
                             more = false;
                         else if ( reader.getLocalName().equals("Header") )
                             more = false;
                         break;
                     default:
                         break;
                 }
                 
                 if ( more )
                     event = reader.next();
             }
             
             response = (XmlObject[]) responseElements.toArray(new XmlObject[responseElements.size()]);
             
             XmlCursor cursor = response[0].newCursor();
             cursor.toFirstChild();
 
             if ( response.length == 1 
                  && 
                  cursor.getName().getLocalPart().equals("Fault") )
             {
                throw new XmlBeansFault(response[0]);
             }
         }
         catch (XmlException e)
         {
             throw new XMLStreamException("Couldn't parse response.", e);
         }
     }
 }
