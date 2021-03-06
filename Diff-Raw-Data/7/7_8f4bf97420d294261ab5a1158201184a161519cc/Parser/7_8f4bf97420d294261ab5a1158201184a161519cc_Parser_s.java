 /*
  * (c) Copyright 2009 University of Bristol
  * All rights reserved.
  * [See end of file]
  */
 package net.rootdev.javardfa;
 
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 /**
  * @author Damian Steer <pldms@mac.com>
  */
 public class Parser implements ContentHandler {
     
     protected final XMLOutputFactory outputFactory;
     protected final XMLEventFactory eventFactory;
     protected final StatementSink sink;
     private final Set<Setting> settings;
     protected final Constants consts;
     private final Resolver resolver;
     private final LiteralCollector2 literalCollector;
 
     public Parser(StatementSink sink) {
         this(   sink,
                 XMLOutputFactory.newInstance(),
                 XMLEventFactory.newInstance(),
                 new IRIResolver());
     }
 
     public Parser(StatementSink sink,
             XMLOutputFactory outputFactory,
             XMLEventFactory eventFactory,
             Resolver resolver) {
         this.sink = sink;
         this.outputFactory = outputFactory;
         this.eventFactory = eventFactory;
         this.settings = EnumSet.noneOf(Setting.class);
         this.consts = new Constants();
         this.resolver = resolver;
         this.literalCollector = new LiteralCollector2(this);
 
         // Important, although I guess the caller doesn't get total control
         outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
     }
 
     public void enable(Setting setting) {
         settings.add(setting);
     }
 
     public void disable(Setting setting) {
         settings.remove(setting);
     }
 
     public void setBase(String base) {
         this.context = new EvalContext(base);
     }
 
     EvalContext parse(EvalContext context, StartElement element)
             throws XMLStreamException {
         boolean skipElement = false;
         String newSubject = null;
         String currentObject = null;
         List<String> forwardProperties = new LinkedList();
         List<String> backwardProperties = new LinkedList();
         String currentLanguage = context.language;
 
         // The xml / html namespace matching is a bit ropey. I wonder if the html 5
         // parser has a setting for this?
         if (settings.contains(Setting.ManualNamespaces)) {
             if (element.getAttributeByName(consts.xmllang) != null) {
                 currentLanguage = element.getAttributeByName(consts.xmllang).getValue();
             } else if (element.getAttributeByName(consts.lang) != null) {
                 currentLanguage = element.getAttributeByName(consts.lang).getValue();
             }
         } else if (element.getAttributeByName(consts.xmllangNS) != null) {
             currentLanguage = element.getAttributeByName(consts.xmllangNS).getValue();
         }
 
         if (consts.base.equals(element.getName()) &&
                 element.getAttributeByName(consts.href) != null) {
             context.setBase(element.getAttributeByName(consts.href).getValue());
         }
 
         if (element.getAttributeByName(consts.rev) == null &&
                 element.getAttributeByName(consts.rel) == null) {
             Attribute nSubj = findAttribute(element, consts.about, consts.src, consts.resource, consts.href);
             if (nSubj != null) {
                 newSubject = getURI(context.base, element, nSubj);
            } else {
                 if (consts.body.equals(element.getName()) ||
                             consts.head.equals(element.getName())) {
                     newSubject = context.base;
                 }
                 else if (element.getAttributeByName(consts.typeof) != null) {
                     newSubject = createBNode();
                 } else {
                     if (context.parentObject != null) {
                         newSubject = context.parentObject;
                     }
                     if (element.getAttributeByName(consts.property) == null) {
                         skipElement = true;
                     }
                 }
             }
         } else {
             Attribute nSubj = findAttribute(element, consts.about, consts.src);
             if (nSubj != null) {
                 newSubject = getURI(context.base, element, nSubj);
            } else {
                 // if element is head or body assume about=""
                 if (consts.head.equals(element.getName()) ||
                         consts.body.equals(element.getName())) {
                     newSubject = context.base;
                 } else if (element.getAttributeByName(consts.typeof) != null) {
                     newSubject = createBNode();
                 } else if (context.parentObject != null) {
                     newSubject = context.parentObject;
                 }
             }
             Attribute cObj = findAttribute(element, consts.resource, consts.href);
             if (cObj != null) {
                 currentObject = getURI(context.base, element, cObj);
             }
         }
 
         if (newSubject != null && element.getAttributeByName(consts.typeof) != null) {
             List<String> types = getURIs(context.base, element, element.getAttributeByName(consts.typeof));
             for (String type : types) {
                 emitTriples(newSubject,
                         consts.rdfType,
                         type);
             }
         }
 
         // Dodgy extension
         if (settings.contains(Setting.FormMode)) {
             if (consts.form.equals(element.getName())) {
                 emitTriples(newSubject, consts.rdfType, "http://www.w3.org/1999/xhtml/vocab/#form"); // Signal entering form
             }
             if (consts.input.equals(element.getName()) &&
                     element.getAttributeByName(consts.name) != null) {
                 currentObject = "?" + element.getAttributeByName(consts.name).getValue();
             }
 
         }
 
         if (currentObject != null) {
             if (element.getAttributeByName(consts.rel) != null) {
                 emitTriples(newSubject,
                         getURIs(context.base, element, element.getAttributeByName(consts.rel)),
                         currentObject);
             }
             if (element.getAttributeByName(consts.rev) != null) {
                 emitTriples(currentObject,
                         getURIs(context.base, element, element.getAttributeByName(consts.rev)),
                         newSubject);
             }
         } else {
             if (element.getAttributeByName(consts.rel) != null) {
                 forwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(consts.rel)));
             }
             if (element.getAttributeByName(consts.rev) != null) {
                 backwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(consts.rev)));
             }
             if (!forwardProperties.isEmpty() || !backwardProperties.isEmpty()) {
                 // if predicate present
                 currentObject = createBNode();
             }
         }
 
         // Getting literal values. Complicated!
         if (element.getAttributeByName(consts.property) != null) {
             List<String> props = getURIs(context.base, element, element.getAttributeByName(consts.property));
             String dt = getDatatype(element);
             if (element.getAttributeByName(consts.content) != null) { // The easy bit
                 String lex = element.getAttributeByName(consts.content).getValue();
                 if (dt == null || dt.length() == 0) {
                     emitTriplesPlainLiteral(newSubject, props, lex, currentLanguage);
                 } else {
                     emitTriplesDatatypeLiteral(newSubject, props, lex, dt);
                 }
             } else {
                 // Begin to gather a literal
                 System.err.println(context);
                 System.err.println("..." + newSubject);
                 literalCollector.collect(newSubject, props, dt, currentLanguage);
             }
         }
 
         if (!skipElement && newSubject != null) {
             emitTriples(context.parentSubject,
                     context.forwardProperties,
                     newSubject);
 
             emitTriples(newSubject,
                     context.backwardProperties,
                     context.parentSubject);
         }
 
         EvalContext ec = new EvalContext(context);
         if (skipElement) {
             ec.language = currentLanguage;
         } else {
             if (newSubject != null) {
                 ec.parentSubject = newSubject;
             } else {
                 ec.parentSubject = context.parentSubject;
             }
 
             if (currentObject != null) {
                 ec.parentObject = currentObject;
             } else if (newSubject != null) {
                 ec.parentObject = newSubject;
             } else {
                 ec.parentObject = context.parentSubject;
             }
 
             ec.language = currentLanguage;
             ec.forwardProperties = forwardProperties;
             ec.backwardProperties = backwardProperties;
         }
         return ec;
     }
 
     private Attribute findAttribute(StartElement element, QName... names) {
         for (QName aName : names) {
             Attribute a = element.getAttributeByName(aName);
             if (a != null) {
                 return a;
             }
         }
         return null;
     }
 
     private void emitTriples(String subj, Collection<String> props, String obj) {
         for (String prop : props) {
             sink.addObject(subj, prop, obj);
         }
     }
 
     protected void emitTriplesPlainLiteral(String subj, Collection<String> props, String lex, String language) {
         for (String prop : props) {
             System.err.printf("s: <%s> p: <%s> lex: '%s' lang: '%s'\n",
                     subj, prop, lex, language);
             sink.addLiteral(subj, prop, lex, language, null);
         }
     }
 
     protected void emitTriplesDatatypeLiteral(String subj, Collection<String> props, String lex, String datatype) {
         for (String prop : props) {
             sink.addLiteral(subj, prop, lex, null, datatype);
         }
     }
 
     int bnodeId = 0;
     
     private String createBNode() // TODO probably broken? Can you write bnodes in rdfa directly?
     {
         return "_:node" + (bnodeId++);
     }
 
     private String getDatatype(StartElement element) {
         Attribute de = element.getAttributeByName(consts.datatype);
         if (de == null) {
             return null;
         }
         String dt = de.getValue();
         if (dt.length() == 0) {
             return dt;
         }
         return expandCURIE(element, dt);
     }
 
     private void getNamespaces(Attributes attrs) {
         for (int i = 0; i < attrs.getLength(); i++) {
             String qname = attrs.getQName(i);
             String prefix = getPrefix(qname);
             if ("xmlns".equals(prefix)) {
                 String pre = getLocal(prefix, qname);
                 String uri = attrs.getValue(i);
                 if (!settings.contains(Setting.ManualNamespaces) && pre.contains("_"))
                     continue; // not permitted
                 context.setNamespaceURI(pre, uri);
                 sink.addPrefix(pre, uri);
             }
         }
     }
 
     private String getPrefix(String qname) {
         if (!qname.contains(":")) {
             return "";
         }
         return qname.substring(0, qname.indexOf(":"));
     }
 
     private String getLocal(String prefix, String qname) {
         if (prefix.length() == 0) {
             return qname;
         }
         return qname.substring(prefix.length() + 1);
     }
     /**
      * SAX methods
      */
     private Locator locator;
     private EvalContext context;
 
     public void setDocumentLocator(Locator arg0) {
         this.locator = arg0;
         if (locator.getSystemId() != null)
             this.setBase(arg0.getSystemId());
     }
 
     public void startDocument() throws SAXException {
         sink.start();
     }
 
     public void endDocument() throws SAXException {
         sink.end();
     }
 
     public void startPrefixMapping(String arg0, String arg1)
             throws SAXException {
         context.setNamespaceURI(arg0, arg1);
         sink.addPrefix(arg0, arg1);
     }
 
     public void endPrefixMapping(String arg0) throws SAXException {
     }
 
     public void startElement(String arg0, String localname, String qname, Attributes arg3) throws SAXException {
         try {
             //System.err.println("Start element: " + arg0 + " " + arg1 + " " + arg2);
 
             // This is set very late in some html5 cases (not even ready by document start)
             if (context == null) {
                 this.setBase(locator.getSystemId());
             }
 
             // Dammit, not quite the same as XMLEventFactory
             String prefix = /*(localname.equals(qname))*/
                     (qname.indexOf(':') == -1 ) ? ""
                     : qname.substring(0, qname.indexOf(':'));
             if (settings.contains(Setting.ManualNamespaces)) {
                 getNamespaces(arg3);
                 if (prefix.length() != 0) {
                     arg0 = context.getNamespaceURI(prefix);
                     localname = localname.substring(prefix.length() + 1);
                 }
             }
             StartElement e = eventFactory.createStartElement(
                     prefix, arg0, localname,
                     fromAttributes(arg3), null, context);
 
             if (literalCollector.isCollecting()) literalCollector.handleEvent(e);
 
             // If we are gathering XML we stop parsing
             if (!literalCollector.isCollectingXML()) context = parse(context, e);
         } catch (XMLStreamException ex) {
             throw new RuntimeException("Streaming issue", ex);
         }
 
     }
 
     public void endElement(String arg0, String localname, String qname) throws SAXException {
         //System.err.println("End element: " + arg0 + " " + arg1 + " " + arg2);
         if (literalCollector.isCollecting()) {
             String prefix = (localname.equals(qname)) ? ""
                     : qname.substring(0, qname.indexOf(':'));
             XMLEvent e = eventFactory.createEndElement(prefix, arg0, localname);
             literalCollector.handleEvent(e);
         }
         // If we aren't collecting an XML literal keep parsing
         if (!literalCollector.isCollectingXML()) context = context.parent;
     }
 
     public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
         if (literalCollector.isCollecting()) {
             XMLEvent e = eventFactory.createCharacters(String.valueOf(arg0, arg1, arg2));
             literalCollector.handleEvent(e);
         }
     }
 
     public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
         //System.err.println("Whitespace...");
         if (literalCollector.isCollecting()) {
             XMLEvent e = eventFactory.createIgnorableSpace(String.valueOf(arg0, arg1, arg2));
             literalCollector.handleEvent(e);
         }
     }
 
     public void processingInstruction(String arg0, String arg1) throws SAXException {
     }
 
     public void skippedEntity(String arg0) throws SAXException {
     }
 
     private Iterator fromAttributes(Attributes attributes) {
         List toReturn = new LinkedList();
         boolean haveLang = false;
         for (int i = 0; i < attributes.getLength(); i++) {
             String qname = attributes.getQName(i);
             String prefix = qname.contains(":") ? qname.substring(0, qname.indexOf(":")) : "";
             Attribute attr = eventFactory.createAttribute(
                     prefix, attributes.getURI(i),
                     attributes.getLocalName(i), attributes.getValue(i));
             //if (consts.xmllang.getLocalPart().equals(attributes.getLocalName(i)) &&
             //        consts.xmllang.getNamespaceURI().equals(attributes.getURI(i))) {
             if (qname.equals("xml:lang")) {
                 haveLang = true;
             }
 
             if (!qname.equals("xmlns") && !qname.startsWith("xmlns:"))
                 toReturn.add(attr);
         }
         
         return toReturn.iterator();
     }
     
     public String getURI(String base, StartElement element, Attribute attr) {
         QName attrName = attr.getName();
         if (attrName.equals(consts.href) || attrName.equals(consts.src)) // A URI
         {
             if (attr.getValue().length() == 0) return base;
             else return resolver.resolve(base, attr.getValue());
         }
         if (attrName.equals(consts.about) || attrName.equals(consts.resource)) // Safe CURIE or URI
         {
             return expandSafeCURIE(base, element, attr.getValue());
         }
         if (attrName.equals(consts.datatype)) // A CURIE
         {
             return expandCURIE(element, attr.getValue());
         }
         throw new RuntimeException("Unexpected attribute: " + attr);
     }
 
     public List<String> getURIs(String base, StartElement element, Attribute attr) {
         List<String> uris = new LinkedList<String>();
         String[] curies = attr.getValue().split("\\s+");
         boolean permitReserved = consts.rel.equals(attr.getName()) ||
                 consts.rev.equals(attr.getName());
         for (String curie : curies) {
             if (consts.SpecialRels.contains(curie.toLowerCase())) {
                 if (permitReserved)
                     uris.add("http://www.w3.org/1999/xhtml/vocab#" + curie.toLowerCase());
             } else {
                 String uri = expandCURIE(element, curie);
                 if (uri != null) {
                     uris.add(uri);
                 }
             }
         }
         return uris;
     }
 
     public String expandCURIE(StartElement element, String value) {
         if (value.startsWith("_:")) {
             if (!settings.contains(Setting.ManualNamespaces)) return value;
             if (element.getNamespaceURI("_") == null) return value;
         }
         if (settings.contains(Setting.FormMode) && // variable
                 value.startsWith("?")) {
             return value;
         }
         int offset = value.indexOf(":") + 1;
         if (offset == 0) {
             //throw new RuntimeException("Is this a curie? \"" + value + "\"");
             return null;
         }
         String prefix = value.substring(0, offset - 1);
 
         // Apparently these are not allowed to expand
         if ("xml".equals(prefix) || "xmlns".equals(prefix)) return null;
 
         String namespaceURI = prefix.length() == 0 ? "http://www.w3.org/1999/xhtml/vocab#" : element.getNamespaceURI(prefix);
         if (namespaceURI == null) {
             return null;
             //throw new RuntimeException("Unknown prefix: " + prefix);
         }
         if (offset != value.length() && value.charAt(offset) == '#') {
             offset += 1; // ex:#bar
         }
         if (namespaceURI.endsWith("/") || namespaceURI.endsWith("#")) {
             return namespaceURI + value.substring(offset);
         } else {
             return namespaceURI + "#" + value.substring(offset);
         }
     }
 
     public String expandSafeCURIE(String base, StartElement element, String value) {
         if (value.startsWith("[") && value.endsWith("]")) {
             return expandCURIE(element, value.substring(1, value.length() - 1));
         } else {
             if (value.length() == 0) {
                 return base;
             }
 
             if (settings.contains(Setting.FormMode) &&
                     value.startsWith("?")) {
                 return value;
             }
 
             return resolver.resolve(base, value);
         }
     }
 }
 
 /*
  * (c) Copyright 2009 University of Bristol
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
