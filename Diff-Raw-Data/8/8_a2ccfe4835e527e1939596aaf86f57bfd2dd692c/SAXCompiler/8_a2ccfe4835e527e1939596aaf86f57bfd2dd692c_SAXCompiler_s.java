 /**
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.compiler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.el.ELException;
 import javax.faces.FacesException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.ext.LexicalHandler;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.FaceletHandler;
 import com.sun.facelets.tag.Location;
 import com.sun.facelets.tag.Tag;
 import com.sun.facelets.tag.TagAttribute;
 import com.sun.facelets.tag.TagAttributes;
 
 /**
  * Compiler implementation that uses SAX
  * 
  * @see com.sun.facelets.compiler.Compiler
  * 
  * @author Jacob Hookom
 * @version $Id: SAXCompiler.java,v 1.8 2005-12-21 05:51:03 jhook Exp $
  */
 public final class SAXCompiler extends Compiler {
 
     private static class CompilationHandler extends DefaultHandler implements
             LexicalHandler {
 
         private final String alias;
 
         private boolean inDocument = false;
 
         private Locator locator;
 
         private final CompilationManager unit;
 
         public CompilationHandler(CompilationManager unit, String alias) {
             this.unit = unit;
             this.alias = alias;
         }
 
         public void characters(char[] ch, int start, int length)
                 throws SAXException {
             if (this.inDocument) {
                 this.unit.writeText(new String(ch, start, length));
             }
         }
 
         public void comment(char[] ch, int start, int length)
                 throws SAXException {
             if (this.inDocument) {
                 this.unit.writeComment(new String(ch, start, length));
             }
         }
 
         protected TagAttributes createAttributes(Attributes attrs) {
             int len = attrs.getLength();
             TagAttribute[] ta = new TagAttribute[len];
             for (int i = 0; i < len; i++) {
                 ta[i] = new TagAttribute(this.createLocation(),
                         attrs.getURI(i), attrs.getLocalName(i), attrs
                                 .getQName(i), attrs.getValue(i));
             }
             return new TagAttributes(ta);
         }
 
         protected Location createLocation() {
             return new Location(this.alias, this.locator.getLineNumber(),
                     this.locator.getColumnNumber());
         }
 
         public void endCDATA() throws SAXException {
             if (this.inDocument) {
                 this.unit.writeText("]]>");
             }
         }
 
         public void endDocument() throws SAXException {
             super.endDocument();
         }
 
         public void endDTD() throws SAXException {
             this.inDocument = true;
         }
 
         public void endElement(String uri, String localName, String qName)
                 throws SAXException {
             this.unit.popTag();
         }
 
         public void endEntity(String name) throws SAXException {
         }
 
         public void endPrefixMapping(String prefix) throws SAXException {
             this.unit.popNamespace(prefix);
         }
 
         public void fatalError(SAXParseException e) throws SAXException {
             throw new SAXException("Error Traced[line: "
                     + this.locator.getLineNumber() + "] " + e.getMessage());
         }
 
         public void ignorableWhitespace(char[] ch, int start, int length)
                 throws SAXException {
             if (this.inDocument) {
                 this.unit.writeWhitespace(new String(ch, start, length));
             }
         }
 
         public InputSource resolveEntity(String publicId, String systemId)
                 throws SAXException {
             String dtd = "default.dtd";
             if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicId)) {
                 dtd = "xhtml1-transitional.dtd";
             }
             URL url = Thread.currentThread().getContextClassLoader()
                     .getResource(dtd);
            return new InputSource(url.toExternalForm());
         }
 
         public void setDocumentLocator(Locator locator) {
             this.locator = locator;
         }
 
         public void startCDATA() throws SAXException {
             if (this.inDocument) {
                 this.unit.writeText("<![CDATA[");
             }
         }
 
         public void startDocument() throws SAXException {
             this.inDocument = true;
         }
 
         public void startDTD(String name, String publicId, String systemId)
                 throws SAXException {
             if (this.inDocument) {
                 StringBuffer sb = new StringBuffer(64);
                 sb.append("<!DOCTYPE ").append(name);
                 if (publicId != null) {
                     sb.append(" PUBLIC \"").append(publicId).append("\"");
                     if (systemId != null) {
                         sb.append(" \"").append(systemId).append("\"");
                     }
                 } else if (systemId != null) {
                     sb.append(" SYSTEM \"").append(systemId).append("\"");
                 }
                 sb.append(" >\n");
                 this.unit.writeText(sb.toString());
             }
             this.inDocument = false;
         }
 
         public void startElement(String uri, String localName, String qName,
                 Attributes attributes) throws SAXException {
             this.unit.pushTag(new Tag(this.createLocation(), uri, localName,
                     qName, this.createAttributes(attributes)));
         }
 
         public void startEntity(String name) throws SAXException {
         }
 
         public void startPrefixMapping(String prefix, String uri)
                 throws SAXException {
             this.unit.pushNamespace(prefix, uri);
         }
 
         public void processingInstruction(String target, String data)
                 throws SAXException {
             if (this.inDocument) {
                 StringBuffer sb = new StringBuffer(64);
                 sb.append("<?").append(target).append(' ').append(data).append(
                         "?>\n");
                 this.unit.writeText(sb.toString());
             }
         }
     }
 
     public SAXCompiler() {
         super();
     }
 
     public FaceletHandler doCompile(URL src, String alias) throws IOException,
             FaceletException, ELException, FacesException {
         CompilationManager mngr = null;
         InputStream is = null;
         try {
             is = src.openStream();
             mngr = new CompilationManager(alias, this);
             writeXmlDecl(src, mngr);
             CompilationHandler handler = new CompilationHandler(mngr, alias);
             SAXParser parser = this.createSAXParser(handler);
             parser.parse(is, handler);
         } catch (SAXException e) {
             throw new FaceletException("Error Parsing " + alias + ": "
                     + e.getMessage(), e.getCause());
         } catch (ParserConfigurationException e) {
             throw new FaceletException("Error Configuring Parser " + alias
                     + ": " + e.getMessage(), e.getCause());
         } finally {
             if (is != null) {
                 is.close();
             }
         }
         return mngr.createFaceletHandler();
     }
 
     protected static final void writeXmlDecl(URL src, CompilationManager mngr)
             throws IOException {
         InputStream is = null;
         try {
             is = src.openStream();
             byte[] b = new byte[128];
             if (is.read(b) > 0) {
                 String r = new String(b);
                 int s = r.indexOf("<?xml");
                 if (s >= 0) {
                     int e = r.indexOf("?>", s);
                     if (e > 0) {
                         mngr.writeText(r.substring(s, e + 2) + '\n');
                     }
                 }
             }
 
         } finally {
             if (is != null) {
                 is.close();
             }
         }
     }
 
     private final SAXParser createSAXParser(CompilationHandler handler)
             throws SAXException, ParserConfigurationException {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setFeature("http://xml.org/sax/features/namespace-prefixes",
                 true);
         factory.setFeature("http://xml.org/sax/features/validation", this
                 .isValidating());
         factory.setValidating(this.isValidating());
         SAXParser parser = factory.newSAXParser();
         XMLReader reader = parser.getXMLReader();
         reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                 handler);
         reader.setErrorHandler(handler);
         reader.setEntityResolver(handler);
         return parser;
     }
 
 }
