 package org.codehaus.xfire.aegis.type.xml;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.stax.ElementWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.util.STAXUtils;
 import org.codehaus.xfire.util.stax.W3CDOMStreamReader;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Reads and writes <code>javax.xml.transform.Source</code> types.
  * <p>
 * The XML stream is converted DOMSource and sent off.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @see javanet.staxutils.StAXSource
  * @see javax.xml.stream.XMLInputFactory
  * @see org.codehaus.xfire.util.STAXUtils
  */
 public class SourceType
     extends Type
 {
     public SourceType()
     {
        setTypeClass(Source.class);
        setWriteOuter(false);
     }
     
     public Object readObject(MessageReader mreader, MessageContext context)
         throws XFireFault
     {
         DocumentType dt = (DocumentType) getTypeMapping().getType(Document.class);
         
         return new DOMSource((Document) dt.readObject(mreader, context));
     }
 /*
     public Source createSource(XMLStreamReader reader)
     {
         return new StAXSource(reader);
     }
     */
     public void writeObject(Object object, MessageWriter writer, MessageContext context)
         throws XFireFault
     {
         try
         {
             if (object == null) return;
             
             XMLStreamReader reader = createXMLStreamReader((Source) object);
 
             STAXUtils.copy(reader, ((ElementWriter) writer).getXMLStreamWriter());
         }
         catch (XMLStreamException e)
         {
             throw new XFireFault("Could not write xml.", e, XFireFault.SENDER);
         }
     }
 
     protected XMLStreamReader createXMLStreamReader(Source object)
         throws FactoryConfigurationError, XMLStreamException, XFireFault
     {
         if (object == null) return null;
 
         if (object instanceof DOMSource)
         {
             DOMSource ds = (DOMSource) object;
             
             Element element = null;
             if (ds.getNode() instanceof Element)
             {
                 element = (Element) ds.getNode();
             }
             else if (ds.getNode() instanceof Document)
             {
                 element = ((Document) ds.getNode()).getDocumentElement();
             }
             else
             {
                 throw new XFireFault("Node type " + ds.getNode().getClass() + 
                                      " was not understood.", XFireFault.RECEIVER);
             }
             
             return new W3CDOMStreamReader(element);
         }
         
         XMLInputFactory xif = XMLInputFactory.newInstance();
         XMLStreamReader reader = xif.createXMLStreamReader(object);
         return reader;
     }
 }
