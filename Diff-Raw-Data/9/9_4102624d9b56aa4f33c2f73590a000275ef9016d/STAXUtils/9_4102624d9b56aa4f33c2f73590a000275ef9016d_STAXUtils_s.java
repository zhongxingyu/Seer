 package org.codehaus.xfire.util;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Common StAX utilities.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public class STAXUtils
 {
     /**
      * Returns true if currently at the start of an element, otherwise move forwards to the next
      * element start and return true, otherwise false is returned if the end of the stream is reached.
      */
     public static boolean skipToStartOfElement(XMLStreamReader in)
         throws XMLStreamException
     {
         for (int code = in.getEventType(); code != XMLStreamReader.END_DOCUMENT; code = in.next())
         {
             if (code == XMLStreamReader.START_ELEMENT)
             {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean toNextElement(DepthXMLStreamReader dr)
     {
         if (dr.getEventType() == XMLStreamReader.START_ELEMENT)
             return true;
         
         if (dr.getEventType() == XMLStreamReader.END_ELEMENT)
             return false;
         
         try
         {
             int depth = dr.getDepth();
             
             for (int event = dr.getEventType(); dr.getDepth() >= depth; event = dr.next())
             {
                 if (event == XMLStreamReader.START_ELEMENT && dr.getDepth() == depth + 1)
                 {
                     return true;
                 }
                 else if (event == XMLStreamReader.END_ELEMENT)
                 {
                     depth--;
                 }
             }
             
             return false;
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
    
     public static boolean skipToStartOfElement(DepthXMLStreamReader in)
         throws XMLStreamException
     {
         int depth = in.getDepth();
         for (int code = in.getEventType(); code != XMLStreamReader.END_DOCUMENT; code = in.next())
         {
             if (code == XMLStreamReader.START_ELEMENT)
             {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Copies the reader to the writer.  The start and end document
      * methods must be handled on the writer manually.
      * 
      * TODO: if the namespace on the reader has been declared previously
      * to where we are in the stream, this probably won't work.
      * 
      * @param reader
      * @param writer
      * @throws XMLStreamException
      */
     public static void copy( XMLStreamReader reader, XMLStreamWriter writer ) 
         throws XMLStreamException
     {
         int read = 0; // number of elements read in
         int event = reader.getEventType();
         
         while ( true )
         {
             switch( event )
             {
                 case XMLStreamConstants.START_ELEMENT:
                     read++;
                     writeStartElement( reader, writer );
                     break;
                 case XMLStreamConstants.END_ELEMENT:
                     writer.writeEndElement();
                     read--;
                     if ( read <= 0 )
                         return;
                     break;
                 case XMLStreamConstants.CHARACTERS:
                     writer.writeCharacters( reader.getText() );  
                     break;
                 case XMLStreamConstants.START_DOCUMENT:
                 case XMLStreamConstants.END_DOCUMENT:
                 case XMLStreamConstants.ATTRIBUTE:
                 case XMLStreamConstants.NAMESPACE:
                     break;
                 default:
                     break;
             }
             event = reader.next();
         }
     }
 
     private static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer) 
         throws XMLStreamException
     {
         String local = reader.getLocalName();
         String uri = reader.getNamespaceURI();
         String prefix = reader.getPrefix();
         if (prefix == null)
         {
             prefix = "";
         }
         
         String boundPrefix = writer.getPrefix(uri);
         boolean writeElementNS = false;
         if ( boundPrefix == null || !prefix.equals(boundPrefix) )
         {   
             writeElementNS = true;
         }
         
         // Write out the element name and possible the default namespace
         if (uri != null)
         {
             if (prefix.length() == 0)
             {
                 writer.setDefaultNamespace(uri);
                writer.writeStartElement(uri, local);
             }
             else
             {
                writer.setPrefix(prefix, uri);
                 writer.writeStartElement(prefix, local, uri);
            } 
         }
         else
         {
             writer.writeStartElement( reader.getLocalName() );
         }
 
         // Write out the namespaces
         for ( int i = 0; i < reader.getNamespaceCount(); i++ )
         {
             String nsURI = reader.getNamespaceURI(i);
             String nsPrefix = reader.getNamespacePrefix(i);
             if (nsPrefix == null) nsPrefix = "";
             
             if ( nsPrefix.length() ==  0 )
             {
                 writer.writeDefaultNamespace(nsURI);
             }
             else
             {
                 writer.writeNamespace(nsPrefix, nsURI);
             }
 
             if (nsURI.equals(uri) && nsPrefix.equals(prefix))
             {
                 writeElementNS = false;
             }
         }
         
         // Check if the namespace still needs to be written.
         // We need this check because namespace writing works 
         // different on Woodstox and the RI.
         if (writeElementNS)
         {
             if ( prefix == null || prefix.length() ==  0 )
             {
                 writer.writeDefaultNamespace(uri);
             }
             else
             {
                 writer.writeNamespace(prefix, uri);
             }
         }
 
         // Write out attributes
         for ( int i = 0; i < reader.getAttributeCount(); i++ )
         {
             String ns = reader.getAttributeNamespace(i);
             if ( ns == null || ns.length() == 0 )
             {
                 writer.writeAttribute(
                         reader.getAttributeLocalName(i),
                         reader.getAttributeValue(i));
             }
             else
             {
                 writer.writeAttribute(
                     reader.getAttributeNamespace(i),
                     reader.getAttributeLocalName(i),
                     reader.getAttributeValue(i));
             }
         }
     }
 
     /**
      * Writes an Element to an XMLStreamWriter.  The writer must already
      * have started the doucment (via writeStartDocument()). Also, this probably
      * won't work with just a fragment of a document. The Element should be
      * the root element of the document.
      * 
      * @param e
      * @param writer
      * @throws XMLStreamException
      */
     public static void writeElement( Element e, XMLStreamWriter writer ) 
         throws XMLStreamException
     {
         String prefix = e.getPrefix();
         String ns = e.getNamespaceURI();
         String localName = e.getLocalName();
         
         if ( prefix == null )
         {
             if ( ns == null )
             {
                 writer.writeStartElement( localName );
             }
             else
             {
                 prefix = "";
                 writer.setDefaultNamespace(ns);
                 writer.writeStartElement( ns, localName );
                 
                 String curUri = writer.getNamespaceContext().getNamespaceURI(prefix);
                 if ( curUri == null || curUri.length() != ns.length() )
                 {
                     writer.writeDefaultNamespace(ns);
                 }
             }
         }
         else
         {
             writer.writeStartElement(prefix, localName, ns);
             
             String curUri = writer.getNamespaceContext().getNamespaceURI(prefix);
             if ( curUri == null || curUri.length() != ns.length() || !curUri.equals(ns) )
             {
                 System.out.println("writing namespace: " + ns);
                 writer.writeNamespace(prefix, ns);
             }
         }
 
         NamedNodeMap attrs = e.getAttributes();
         for ( int i = 0; i < attrs.getLength(); i++ )
         {
             Node attr = attrs.item(i);
             
             String attrPrefix = writer.getNamespaceContext().getPrefix(attr.getNamespaceURI());
             if ( attrPrefix == null )
             {
                 writer.writeAttribute(attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
             }
             else
             {
                 writer.writeAttribute(attrPrefix, attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
             }
         }
     
         String value = DOMUtils.getContent(e);
         
         if ( value != null && value.length() > 0)
             writer.writeCharacters( value );
         
         NodeList nodes = e.getChildNodes();
         for ( int i = 0; i < nodes.getLength(); i++ )
         {
             Node n = nodes.item(i);
             if ( n instanceof Element )
             {
                 writeElement((Element)n, writer);
             }
         }
 
         writer.writeEndElement();
     }
     
     /**
      * @param e
      * @return
      */
     private static Element getNamespaceDeclarer(Element e)
     {
         while( true )
         {
             Node n = e.getParentNode();
             if ( n.equals(e) )
                 return null;
             if ( n.getNamespaceURI() != null )
                 return (Element) n;
         }
     }
 
     public static void readElements(Element root, XMLStreamReader reader)
     	throws XMLStreamException
     {
         readElements(root.getOwnerDocument(), root, reader);
     }
     
     public static void readElements(Document doc, Element root, XMLStreamReader reader)
         throws XMLStreamException
     {
         int read = 0; // number of elements read in
         
         Element e;
         
         StringBuffer text = new StringBuffer();
         
         while ( true )
         {
             int event = reader.next();
             switch( event )
             {
                 case XMLStreamConstants.START_ELEMENT:
                     read++;
                     e = doc.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
                     root.appendChild(e);
 
                     for ( int i = 0; i < reader.getAttributeCount(); i++ )
                     {
                         Attr attr = doc.createAttributeNS(reader.getAttributeNamespace(i),
                                                           reader.getAttributeLocalName(i));
                         attr.setValue(reader.getAttributeValue(i));
                         e.setAttributeNode(attr);
                     }
                     
                     readElements(e, reader);
                     break;
                 case XMLStreamConstants.END_ELEMENT:
                     if (text.length() > 0)
                         DOMUtils.setText(root, text.toString());  
                     return;
                 case XMLStreamConstants.CHARACTERS:
                     if (root != null)
                         text.append(reader.getText());
                     break;
                 case XMLStreamConstants.END_DOCUMENT:
                     return;
                 case XMLStreamConstants.CDATA:
                 case XMLStreamConstants.START_DOCUMENT:
                 case XMLStreamConstants.ATTRIBUTE:
                 case XMLStreamConstants.NAMESPACE:
                 default:
                     break;
             }
         }
     }
     
     public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding)
     {
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
 
         if (encoding == null) encoding = "UTF-8";
         
         try
         {
             XMLStreamWriter writer = factory.createXMLStreamWriter(out, encoding);
 
             return writer;
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 
     public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding)
     {
         XMLInputFactory factory = XMLInputFactory.newInstance();
 
         if (encoding == null) encoding = "UTF-8";
         
         try
         {
             return factory.createXMLStreamReader(in, encoding);
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 
     public static XMLStreamReader createXMLStreamReader(Reader reader)
     {
         XMLInputFactory factory = XMLInputFactory.newInstance();
  
         try
         {
             return factory.createXMLStreamReader(reader);
         }
         catch (XMLStreamException e)
         {
             throw new XFireRuntimeException("Couldn't parse stream.", e);
         }
     }
 }
