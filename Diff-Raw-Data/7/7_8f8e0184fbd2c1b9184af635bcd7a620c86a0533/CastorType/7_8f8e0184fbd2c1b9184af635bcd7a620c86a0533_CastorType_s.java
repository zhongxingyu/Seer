 package org.codehaus.xfire.castor;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javanet.staxutils.ContentHandlerToXMLStreamWriter;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.AegisBindingProvider;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.stax.ElementReader;
 import org.codehaus.xfire.aegis.stax.ElementWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.dom4j.Node;
 import org.dom4j.dom.DOMDocumentFactory;
 import org.dom4j.io.STAXEventReader;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.xml.Introspector;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.exolab.castor.xml.util.XMLClassDescriptorImpl;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 /**
  * XFire Type class for marshalling beans and demarshalling XML using Castor.
  * 
  * @author Adam Kramer
  * @author Paul Saxman
  */
 public class CastorType
     extends Type
 {
     private static final Log log = LogFactory.getLog(CastorType.class);
 
     private Mapping mapping;
 
     /**
      * Constructor that sets the Castor mapping to use for de/marshalling and
      * sets the schema type for the castor type.
      * 
      * @param class
      *            The Class of the castor type.
      * @param mapping
      *            The Castor mapping file used for de/marshalling which
      *            presently needs to be in the classpath.
      */
     public CastorType(Class clazz, Mapping mapping)
     {
         this.mapping = mapping;
         setTypeClass(clazz);
 
         initType();
     }
 
     /**
      * @see org.codehaus.xfire.wsdl.SchemaType#getSchemaType()
      */
     public QName getSchemaType()
     {
         return super.getSchemaType();
     }
 
     /**
      * @see org.codehaus.xfire.aegis.type.Type#readObject(org.codehaus.xfire.aegis.MessageReader,
      *      org.codehaus.xfire.MessageContext)
      */
     public Object readObject(MessageReader reader, MessageContext context)
         throws XFireFault
     {
         Unmarshaller unmarshaller;
         XMLStreamReader sReader = ((ElementReader) reader).getXMLStreamReader();
 
         Node node = null;
 
         try
         {
             // Create a StAX XMLEventReader from the StAX XMLStreamReader.
             XMLEventReader xer = XMLInputFactory.newInstance().createXMLEventReader(sReader);
             node = (new STAXEventReader(new DOMDocumentFactory())).readNode(xer);
         }
         catch (XMLStreamException e)
         {
             log.error("Error creating a dom4j Document from a StAX XMLEventReader.", e);
             throw new XFireFault(e);
         }
 
         try
         {
             unmarshaller = new Unmarshaller(getTypeClass());
             if (this.mapping != null)
             {
                 unmarshaller.setMapping(this.mapping);
             }
             if (log.isDebugEnabled())
             {
                 unmarshaller.setDebug(true);
                 unmarshaller.setLogWriter(new LogPrintWriter());
             }
         }
         catch (MappingException e)
         {
             String error = "Could not use specified mapping for unmarshalling.";
             log.error(error, e);
             throw new XFireRuntimeException(error, e);
         }
 
         try
         {
             // Use Castor to unmarshall the DOM Node.
             return unmarshaller.unmarshal((org.w3c.dom.Node) node);
         }
         catch (MarshalException e)
         {
             log.error("Error during unmarshalling process.", e);
             throw new XFireFault(e);
         }
         catch (ValidationException e)
         {
             log.error("Error validating DOM node for unmarshalling.", e);
             throw new XFireFault(e);
         }
     }
 
     /**
      * @see org.codehaus.xfire.aegis.type.Type#writeObject(java.lang.Object,
      *      org.codehaus.xfire.aegis.MessageWriter,
      *      org.codehaus.xfire.MessageContext)
      */
     public void writeObject(Object object, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         XMLStreamWriter sWriter = ((ElementWriter) writer).getXMLStreamWriter();
         Marshaller marshaller;
         MessagePartInfo part = (MessagePartInfo) context
                 .getProperty(AegisBindingProvider.CURRENT_MESSAGE_PART);
 
         try
         {
             marshaller = new Marshaller(new SafeContentHandler(new ContentHandlerToXMLStreamWriter(
                     sWriter)));
             marshaller.setRootElement(part.getName().getLocalPart());
             if (mapping != null)
             {
                 marshaller.setMapping(mapping);
             }
         }
         catch (IOException e)
         {
             log.error("Could not marshall type.", e);
             throw new XFireFault(e);
         }
         catch (MappingException e)
         {
             log.error("Could not use specified Castor mapping for marshalling.", e);
             throw new XFireFault(e);
         }
 
         try
         {
             marshaller.marshal(object);
         }
         catch (MarshalException e)
         {
             log.error("Error during marshalling process.", e);
             throw new XFireFault(e);
         }
         catch (ValidationException e)
         {
             log.error("Could not marshall object due to validation error.", e);
             throw new XFireFault(e);
         }
     }
     
     public boolean isComplex()
     {
         return true;
     }
 
     public boolean isWriteOuter()
     {
         return false;
     }
 
     public void initType()
     {
         Class clazz = getTypeClass();
         Class xdClass = null;
         XMLClassDescriptorImpl xd = null;
         String nsUri;
         String nsPrefix;
         String localTypeName;
 
         // If mapping, get schema type info from there
         if (this.mapping != null)
         {
             try
             {
                 xd = (XMLClassDescriptorImpl) mapping.getResolver(Mapping.XML).getDescriptor(clazz);
                 nsUri = (xd.getNameSpaceURI() == null ? "" : xd.getNameSpaceURI());
                 // Use xml name as schema type name, unless it has been introspected
                if (Introspector.introspected(xd))
                    localTypeName = clazz.getSimpleName();
                else
                     localTypeName = xd.getXMLName();
                 nsPrefix = xd.getNameSpacePrefix();
                 if (nsPrefix == null || nsPrefix.length() == 0)
                     setSchemaType(new QName(nsUri, localTypeName));
                 else
                     setSchemaType(new QName(nsUri, localTypeName, nsPrefix));
                 setAbstract(!xd.isElementDefinition());
             }
             catch (MappingException e)
             {
                 String error = "Error getting resolver from mapping.";
                 log.error(error, e);
                 throw new XFireRuntimeException(error, e);
             }
         }
         else
         // No mapping, check for accompanying xml descriptor class
         {
             try
             {
                 xdClass = ClassLoaderUtils.loadClass(clazz.getName() + "Descriptor", this
                         .getClass());
                 if (xdClass != null && (XMLClassDescriptorImpl.class.isAssignableFrom(xdClass)))
                 {
                     xd = (XMLClassDescriptorImpl) xdClass.newInstance();
                     nsUri = (xd.getNameSpaceURI() == null ? "" : xd.getNameSpaceURI());
                     nsPrefix = (xd.getNameSpacePrefix() == null ? "" : xd.getNameSpacePrefix());
                     localTypeName = xd.getXMLName();
                     setSchemaType(new QName(nsUri, localTypeName, nsPrefix));
                     setAbstract(!xd.isElementDefinition());
                 }
             }
             catch (ClassNotFoundException e)
             {
             }
             catch (InstantiationException e1)
             {
             }
             catch (IllegalAccessException e2) 
             {
             }
         }
     }
 
     /**
      * A PrintWriter class that writes to a Commons Logging log.
      */
     private class LogPrintWriter
         extends PrintWriter
     {
         public LogPrintWriter()
         {
             super(System.err);
         }
 
         public void println(String string)
         {
             log.debug(string);
         }
     }
 
     /**
      * ContentHandler wrapper class which ignores calls to startElement() and
      * endElement().
      */
     private class SafeContentHandler
         implements ContentHandler
     {
         private ContentHandler handler;
 
         public SafeContentHandler(ContentHandler handler)
         {
             this.handler = handler;
         }
 
         public void setDocumentLocator(Locator arg0)
         {
             handler.setDocumentLocator(arg0);
         }
 
         public void startDocument()
             throws SAXException
         {
             log.debug("Ignoring startDocument() call to ContentHandler.");
         }
 
         public void endDocument()
             throws SAXException
         {
             log.debug("Ignoring endDocument() call to ContentHandler.");
         }
 
         public void startPrefixMapping(String arg0, String arg1)
             throws SAXException
         {
             handler.startPrefixMapping(arg0, arg1);
         }
 
         public void endPrefixMapping(String arg0)
             throws SAXException
         {
             handler.endPrefixMapping(arg0);
         }
 
         public void startElement(String arg0, String arg1, String arg2, Attributes arg3)
             throws SAXException
         {
             handler.startElement(arg0, arg1, arg2, arg3);
         }
 
         public void endElement(String arg0, String arg1, String arg2)
             throws SAXException
         {
             handler.endElement(arg0, arg1, arg2);
         }
 
         public void characters(char[] arg0, int arg1, int arg2)
             throws SAXException
         {
             handler.characters(arg0, arg1, arg2);
         }
 
         public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
             throws SAXException
         {
             handler.ignorableWhitespace(arg0, arg1, arg2);
         }
 
         public void processingInstruction(String arg0, String arg1)
             throws SAXException
         {
             handler.processingInstruction(arg0, arg1);
         }
 
         public void skippedEntity(String arg0)
             throws SAXException
         {
             handler.skippedEntity(arg0);
         }
     }
 
 }
