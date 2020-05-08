 package org.wyona.commons.xml;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.resolver.tools.CatalogResolver;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * XML utility class (also see org.apache.commons.lang.StringEscapeUtils)
  */
 public class XMLHelper {
     private static final Logger log = Logger.getLogger(XMLHelper.class);
     private static final String YES = "yes";
     private static final String NO = "no";
     private static final String DEFAULT_ENCODING = "iso-8859-1";
     private static final int INDENT_AMOUNT = 2;
 
     /**
      * Convert a Document into an input stream
      * 
      * @param document The source document
      * @param isFragment
      * @param indent If true, then the XML will be indented
      * @param charset Encoding, e.g. utf-8
      * @return XML document as InputStream
      */
    public static final java.io.InputStream getInputStream(Document document, boolean isFragment, boolean indent, String charset) throws Exception {
         // TODO: Is this the most efficient way?!
        //return new java.io.ByteArrayInputStream(documentToString(document, isFragment, indent, charset).getBytes()); // WARN: This does not seem to work with special characters!
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream(); // INFO: Also see http://ostermiller.org/convert_java_outputstream_inputstream.html
        writeDocument(document, bout); // WARN: This can create memory issues, but at least works with special characters!
        return new java.io.ByteArrayInputStream(bout.toByteArray());
     }
 
     /**
      * Convert a Document into something human readable.
      * 
      * @param document The document to be displayed
      * @param isFragment
      * @param indent If true, then the XML will be indented
      * @param charset Encoding, e.g. utf-8
      * @return
      */
     public static final String documentToString(Document document, boolean isFragment, boolean indent, String charset) {
         if( document == null ){
             log.error("Document is null!");
             return null;
         }
 
 // NOTE/TODO: The code snippet below seems to have problems with empty tags, for example <my-tag/>
         StringWriter strWtr = new StringWriter();
         StreamResult strResult = new StreamResult(strWtr);
         TransformerFactory tfac = TransformerFactory.newInstance();
         try {
             Transformer t = tfac.newTransformer();
             if( isFragment )
                 t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
             
             if( charset == null) {
                 log.warn("No encoding set, hence use default encoding: " + DEFAULT_ENCODING);
                 t.setOutputProperty(OutputKeys.ENCODING, DEFAULT_ENCODING);
 	    } else {
                 t.setOutputProperty(OutputKeys.ENCODING, charset);
             }
 
             t.setOutputProperty(OutputKeys.INDENT, (indent) ? YES : NO);
             t.setOutputProperty(OutputKeys.METHOD, "xml"); //xml, html, text
             t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + INDENT_AMOUNT);
             t.transform(new DOMSource(document.getDocumentElement()), strResult);
         } catch (Exception e) {
             log.error(e, e);
             //throw e;
         }
         return strResult.getWriter().toString();
 
 
 
 // The following code snippets has NO problems with empty tags, but depends explicitely on the Apache XMLSerializer!
 /*
         try {
             java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
             org.apache.xml.serialize.OutputFormat outputformat = new org.apache.xml.serialize.OutputFormat();
             outputformat.setIndent(INDENT_AMOUNT);
             outputformat.setIndenting(indent);
             outputformat.setPreserveSpace(false);
             org.apache.xml.serialize.XMLSerializer serializer = new org.apache.xml.serialize.XMLSerializer();
             serializer.setOutputFormat(outputformat);
             serializer.setOutputByteStream(stream);
             serializer.asDOMSerializer();
             serializer.serialize(document.getDocumentElement());
             return new StringBuilder(stream.toString()).toString();
         } catch (Exception e) {
             log.error(e, e);
             //throw e;
             return null;
         }
 */
     }
 
     /* Seems like we do not always have a DOM3 Save compliant parser in the classpath...
     private static LSSerializer getDOMserializer() throws Exception {
         DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
         DOMImplementation impl = registry.getDOMImplementation("Core 2.0 LS-Save 3.0");
         if (impl == null) {
             throw new UnsupportedOperationException("Could not find a DOM3 Save compliant parser!");
         }
         DOMImplementationLS implls = (DOMImplementationLS) impl;
         LSSerializer writer = implls.createLSSerializer();
         return writer;
         writer.writeToString(...)
     }
     */
 
     private static Transformer getXMLidentityTransformer(int indentation) throws Exception {
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer t = factory.newTransformer(); // identity transform
         t.setOutputProperty(OutputKeys.INDENT, (indentation != 0) ? YES : NO);
         t.setOutputProperty(OutputKeys.METHOD, "xml"); //xml, html, text
         t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + indentation);
         return t;
     }
 
     public static String stringFromNodeList(NodeList nodeList, int indentation) {
         StringBuilder sb = new StringBuilder();
         Writer w = new StringWriter();
         int n = nodeList.getLength();
         try {
             Transformer transformer = getXMLidentityTransformer(indentation);
             for (int i = 0; i < n; i++) {
                 Node node = nodeList.item(i);
                 DOMSource source = new DOMSource(node);
                 StreamResult result = new StreamResult(w);
                 transformer.transform(source, result);
                 sb.append(w.toString());
             }
             if (log.isDebugEnabled()) log.debug("sb: " + sb);
             return sb.toString();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Create DOM document from input stream
      * @param in Input stream
      */
     public static Document readDocument(java.io.InputStream in) throws Exception {
         return createBuilder().parse(in);
     }
 
     /**
      * Append XML string fragment to a DOM element
      * @param element DOM element to which fragment will be appended
      * @param xmlString XML as string, e.g. <div><p>Hello <strong>World</strong></p></div>
      */
     public static void appendFragment(Element element, String xmlString) throws Exception {
         Document tmpDoc = createBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlString)));
         Node importedNode = element.getOwnerDocument().importNode(tmpDoc.getDocumentElement(), true);
         element.appendChild(importedNode);
     }
            
     /**
      * Creates a Document instance ( DOM )
      * 
      * @param namespace Namespace of root element
      * @param localname Local name of the root element
      * @return Document object representing the rootname
      * @throws RuntimeException
      */
     public static final Document createDocument(String namespace, String localname) throws RuntimeException {
         try {
             DocumentBuilder docBuilder = createBuilder();
             org.w3c.dom.DOMImplementation domImpl = docBuilder.getDOMImplementation();
             org.w3c.dom.DocumentType doctype = null;
             Document doc = domImpl.createDocument(namespace, localname, doctype);
             if (namespace != null) {
                 doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", namespace);
             }
             return doc;
         } catch (ParserConfigurationException e) {
             throw new RuntimeException( "Unable to create a DOM document, check your xml configuration" );
         }
     }
     
     /**
      * Utility method to create a text element. This method handles escaping if xml special characters.
      * 
      * @param doc the document the owns the element
      * @param name element name
      * @param value The text contained by the element 
      * @param attribs Hashmap of element attributes (optional)
      * @return Fully formatted xml element 
      */
     public static final Element createTextElement(Document doc, String name, String value, HashMap<String, String> attribs) {
         Element child = doc.createElement(name);
         if( attribs !=null ){
             Iterator<Entry<String,String>> iter = attribs.entrySet().iterator();
             while( iter.hasNext() ){
                 Entry<String, String> attr = iter.next();
                 child.setAttribute( attr.getKey(), attr.getValue() );    
             }
         }
         Text text = doc.createTextNode( value );
         child.appendChild(text);
         return child;
     }
 
     /**
      * Creates a non-validating and namespace-aware DocumentBuilder.
      * @return A new DocumentBuilder object.
      * @throws ParserConfigurationException if an error occurs
      */
     public static DocumentBuilder createBuilder() throws ParserConfigurationException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
 
         CatalogResolver cr = new CatalogResolver();
         builder.setEntityResolver(cr);
         return builder;
     }
 
     /**
      * Write DOM document into output stream
      *
      * @param doc DOM document which will be written into OutputStream
      * @param out OutputStream into which the XML document is written
      */
     public static void writeDocument(Document doc, java.io.OutputStream out) throws Exception {
         if (doc == null) {
             throw new Exception("Document is null");
         } else if (out == null) {
             throw new Exception("OutputStream is null");
         } else {
             javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(out));
             out.close();
         }
     }
 
     /**
      * Get child elements with a specific local name and a matching attribute name/value
      */
     public static org.w3c.dom.Element[] getElements(org.w3c.dom.Element element, String localName, String attributeName, String attributeValue) throws Exception {
         org.w3c.dom.Element[] elements = getChildElements(element, localName, null);
         java.util.Vector children = new java.util.Vector();
         for (int i = 0; i < elements.length; i++) {
             org.w3c.dom.Element child = elements[i];
             if (child.getAttribute(attributeName).equals(attributeValue)) {
                 children.addElement(child);
             }
         }
         org.w3c.dom.Element[] childElements = new org.w3c.dom.Element[children.size()];
         for (int i = 0; i < childElements.length; i++) {
             childElements[i] = (org.w3c.dom.Element) children.elementAt(i);
         }
         return childElements;
     }
 
     /**
      * Get child elements with a specific local name and namespace
      * @param node Node
      * @param localName Local name of child nodes
      * @param namespaceURI Namespace of child nodes, whereas if null is specified, then it will not be taken into account
      */
     public static org.w3c.dom.Element[] getChildElements(org.w3c.dom.Node node, String localName, String namespaceURI) throws Exception {
         org.w3c.dom.NodeList nl = node.getChildNodes();
         java.util.Vector children = new java.util.Vector();
         for (int i = 0; i < nl.getLength(); i++) {
             org.w3c.dom.Node child = nl.item(i);
             if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                 String childLocalName = child.getLocalName();
                 if (childLocalName == null) {
                     log.warn("Local name is null (node name: " + child.getNodeName() + ")! Try using to strip off tagname ...");
                     childLocalName = getLocalPart(((Element) child).getTagName());
                     log.warn("Local name based on tagname: " + childLocalName);
                 }
                 if (childLocalName.equals(localName)) {
                     if (namespaceURI != null) {
                         if (child.getNamespaceURI().equals(namespaceURI)) {
                             children.addElement(child);
                         }
                     } else {
                         children.addElement(child);
                     }
                 }
             }
         }
         org.w3c.dom.Element[] childElements = new org.w3c.dom.Element[children.size()];
         for (int i = 0; i < childElements.length; i++) {
             childElements[i] = (org.w3c.dom.Element) children.elementAt(i);
         }
         return childElements;
     }
 
     /**
      * Strip off namespace prefix
      */
     private static String getLocalPart(String tagName) {
         if (tagName.indexOf(":") > 0) {
             return tagName.split(":")[1];
         } else {
             return tagName;
         }
     }
 
     /**
      * Replace special characters by their correspoding XML entities
      * This method escapes those characters which must not occur in an xml text node.
      * @param str String containing not-escaped characters
      * @return Escaped string
      */
     public static String replaceEntities(String str) {
         if (str == null) {
             return null;
         }
 
         // NOTE: There may be some &amp; and some & mixed in the input, so first transform all &amp; to & and then transform all & back to &amp; this way we don't get double escaped &amp;amp;
         str = str.replaceAll("&amp;", "&");
         str = str.replaceAll("&", "&amp;");
         str = str.replaceAll("<", "&lt;");
         //str = str.replaceAll(">", "&gt;");
         str = str.replaceAll("'", "&apos;");
         str = str.replaceAll("\"", "&quot;");
         return str;
     }
 
     /**
      * Check well-formedness of XML
      * @param in XML as InputStream
      * @return XML as InputStream
      */
     public static java.io.InputStream isWellFormed(java.io.InputStream in) throws Exception {
         log.info("Check well-formedness ...");
         javax.xml.parsers.DocumentBuilderFactory dbf= javax.xml.parsers.DocumentBuilderFactory.newInstance();
         try {
             // Buffer within memory (TODO: Maybe replace with File-buffering ...)
             // http://www-128.ibm.com/developerworks/java/library/j-io1/
             java.io.ByteArrayOutputStream baos  = new java.io.ByteArrayOutputStream();
             byte[] buf = new byte[8192];
             int bytesR;
             while ((bytesR = in.read(buf)) != -1) {
                 baos.write(buf, 0, bytesR);
             }
             byte[] memBuffer = baos.toByteArray();
 
 
             // Setup parser
             javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();
 
             // TODO: Get log messages into log4j ...
             //parser.setErrorHandler(...);
 
             // NOTE: DOCTYPE is being resolved/retrieved (e.g. xhtml schema from w3.org) also
             //       if isValidating is set to false.
             //       Hence, for performance and network reasons we use a local catalog ...
             //       Also see http://www.xml.com/pub/a/2004/03/03/catalogs.html
             //       resp. http://xml.apache.org/commons/components/resolver/
             // TODO: What about a resolver factory?
             parser.setEntityResolver(new org.apache.xml.resolver.tools.CatalogResolver());
 
             parser.parse(new java.io.ByteArrayInputStream(memBuffer));
             //org.w3c.dom.Document document = parser.parse(new ByteArrayInputStream(memBuffer));
             log.info("Data seems to be well-formed :-)");
 
             // Re-create input stream from memory buffer
             in = new java.io.ByteArrayInputStream(memBuffer);
             return in;
         } catch (org.xml.sax.SAXException e) {
             throw new Exception("The received data is not well-formed: " + e.getMessage());
         } catch (Exception e) {
             throw new Exception("The received data is either not well-formed or some other exception occured: " + e.getMessage());
         }
     }
 
     /**
      * Check if XML is valid
      * @param xmlIn XML as InputStream
      * @param schemaLanguage Schema language (xsd, dtd, rng), e.g. javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI or rather http://www.w3.org/2001/XMLSchema
      * @param schemaIn XML schema/dtd/relax-ng as InputStream
      * @return XML as InputStream
      */
     public static java.io.InputStream isValid(java.io.InputStream xmlIn, String schemaLanguage, java.io.InputStream schemaIn) throws Exception {
         log.debug("Check if XML is valid: " + schemaLanguage);
 
         // Buffer within memory (TODO: Maybe replace with File-buffering ...)
         // http://www-128.ibm.com/developerworks/java/library/j-io1/
         java.io.ByteArrayOutputStream baos  = new java.io.ByteArrayOutputStream();
         byte[] buf = new byte[8192];
         int bytesR;
         while ((bytesR = xmlIn.read(buf)) != -1) {
             baos.write(buf, 0, bytesR);
         }
         byte[] memBuffer = baos.toByteArray();
 
 
         javax.xml.validation.SchemaFactory sf = javax.xml.validation.SchemaFactory.newInstance(schemaLanguage);
         javax.xml.validation.Schema schema = sf.newSchema(new javax.xml.transform.stream.StreamSource(schemaIn));
         schema.newValidator().validate(new javax.xml.transform.stream.StreamSource(new java.io.ByteArrayInputStream(memBuffer)));
 
         xmlIn = new java.io.ByteArrayInputStream(memBuffer);
         return xmlIn;
     }
 
     /**
      * Get values re a specific Xpath
      * @param doc XML DOM Document
      * @param xpathString XPath string, e.g. "/scheduler/jobs/job/custom-config/*[local-name()='email' and namespace-uri()='http://www.foo.bar/yanel/scheduler/1.0']/@to"
      */
     public static String[] valueOf(Document doc, String xpathString) throws Exception {
         XPath xpath = XPathFactory.newInstance().newXPath();
         XPathExpression expr = xpath.compile(xpathString);
         NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
         if (nl.getLength() <= 0) {
             log.warn("No nodes found for xpath: " + xpathString);
         }
         String[] values = new String[nl.getLength()];
         for (int i = 0; i < nl.getLength(); i++) {
             Node node = nl.item(i);
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 log.debug("Element found: " + xpathString);
                 NodeList children = node.getChildNodes();
                 for (int k = 0; k < children.getLength(); k++) {
                     if (children.item(k).getNodeType() == Node.TEXT_NODE || children.item(k).getNodeType() == Node.CDATA_SECTION_NODE) {
                         if (values[i] == null) {
                             values[i] = children.item(k).getNodeValue();
                         } else {
                             values[i] = values[i] + children.item(k).getNodeValue();
                         }
                     }
                 }
                 log.debug("Value of: " + values[i]);
             } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                 log.debug("Attribute found: " + xpathString);
                 values[i] = ((org.w3c.dom.Attr) node).getValue();
             } else {
                 log.warn("Neither Attribute nor Element found (" + xpathString + "), but node type: " + node.getNodeType());
                 values[i] = node.getNodeValue();
             }
         }
         return values;
     }
 }
