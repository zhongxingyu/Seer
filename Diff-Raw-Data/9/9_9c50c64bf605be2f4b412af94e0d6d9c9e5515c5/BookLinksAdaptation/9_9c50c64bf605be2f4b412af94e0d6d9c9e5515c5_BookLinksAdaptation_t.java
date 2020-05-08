 package au.com.miskinhill.rdf;
 
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.XMLEvent;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import au.id.djc.rdftemplate.XMLStream;
 import au.id.djc.rdftemplate.selector.AbstractAdaptation;
 import au.id.djc.rdftemplate.selector.SelectorFactory;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class BookLinksAdaptation extends AbstractAdaptation<XMLStream, RDFNode> {
     
     private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
     private static final QName A_QNAME = new QName(XHTML_NS_URI, "a");
     private static final QName LANG_QNAME = new QName("lang");
     private static final QName HREF_QNAME = new QName("href");
     
     private final XMLInputFactory inputFactory;
     private final XMLEventFactory eventFactory;
     private final SelectorFactory selectorFactory;
     
     @Autowired
     public BookLinksAdaptation(XMLInputFactory inputFactory, XMLEventFactory eventFactory, SelectorFactory selectorFactory) {
         super(XMLStream.class, new Class<?>[] { }, RDFNode.class);
         this.inputFactory = inputFactory;
         this.eventFactory = eventFactory;
         this.selectorFactory = selectorFactory;
     }
     
     @Override
     protected XMLStream doAdapt(RDFNode node) {
         List<Link> links = new ArrayList<Link>();
         String title = selectorFactory.get("dc:title#string-lv").withResultType(String.class).singleResult(node);
         String responsibility = StringUtils.defaultString(oneOrNull(selectorFactory.get("mhs:responsibility#string-lv")
                 .withResultType(String.class).result(node)));
         String asin = oneOrNull(selectorFactory.get("dc:identifier[uri-prefix='urn:asin:']#uri-slice(9)")
                 .withResultType(String.class).result(node));
         String isbn = oneOrNull(selectorFactory.get("dc:identifier[uri-prefix='urn:isbn:']#uri-slice(9)")
                 .withResultType(String.class).result(node));
         String gbooksId = oneOrNull(selectorFactory.get("dc:identifier[uri-prefix='http://books.google.com/books?id=']#uri-slice(33)")
                 .withResultType(String.class).result(node));
         String oclcnum = oneOrNull(selectorFactory.get("dc:identifier[uri-prefix='info:oclcnum/']#uri-slice(13)")
                 .withResultType(String.class).result(node));
        String openLibraryId = oneOrNull(selectorFactory.get("owl:sameAs[uri-prefix='http://openlibrary.org/books/']#uri-slice(29)")
                .withResultType(String.class).result(node));
         
         if (asin != null) {
             String domain = "com";
             if (asin.startsWith("2")) domain = "fr";
             else if (asin.startsWith("3")) domain = "de";
             links.add(new Link(
                     String.format("http://www.amazon.%s/dp/%s/?tag=mishil-20", domain, query(asin, "UTF-8")),
                     "Amazon." + domain));
         }
         
         Literal titleLiteral = selectorFactory.get("dc:title").withResultType(RDFNode.class).singleResult(node).as(Literal.class);
         if (literalLang(titleLiteral).equals("ru")) {
             links.add(new Link("http://www.ozon.ru/?context=search&text=" + query(title + " " + responsibility, "CP1251"), "Ozon.ru"));
         }
         
        if (openLibraryId != null) {
            links.add(new Link("http://openlibrary.org/books/" + query(openLibraryId, "UTF-8"), "Open Library"));
        } else if (isbn != null) {
            links.add(new Link("http://openlibrary.org/search?q=isbn:" + query(isbn, "UTF-8"), "Open Library"));
         }
         
         if (gbooksId != null) {
             links.add(new Link("http://books.google.com/books?id=" + query(gbooksId, "UTF-8"), "Google Book Search"));
         } else if (isbn != null) {
             links.add(new Link("http://books.google.com/books?vid=ISBN" + query(isbn, "UTF-8"), "Google Book Search"));
         } else {
             links.add(new Link("http://books.google.com/books?q=" + query(title, "UTF-8"), "Google Book Search"));
         }
         
         if (oclcnum != null) {
             links.add(new Link(uri("www.worldcat.org", "/oclc/" + oclcnum), "WorldCat"));
         } else if (isbn != null) {
             links.add(new Link("http://www.worldcat.org/search?q=isbn:" + query(isbn, "UTF-8"), "WorldCat"));
         } else {
             links.add(new Link("http://www.worldcat.org/search?q=" + query(title, "UTF-8"), "WorldCat"));
         }
         
         if (isbn != null) {
             links.add(new Link(uri("www.librarything.com", "/isbn/" + isbn), "LibraryThing"));
         } else {
             links.add(new Link(uri("www.librarything.com", "/title/" + title), "LibraryThing"));
         }
         
         List<XMLEvent> events = new ArrayList<XMLEvent>();
         for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
             Link link = it.next();
             link.write(events);
             if (it.hasNext())
                 events.add(eventFactory.createCharacters(",\n"));
         }
         return new XMLStream(events);
     }
 
     private final class Link {
         private final String href;
         private final String anchor;
         public Link(String href, String anchor) {
             this.href = href;
             this.anchor = anchor;
         }
         public void write(List<XMLEvent> destination) {
             Attribute hrefAttr = eventFactory.createAttribute(HREF_QNAME, href);
             destination.add(eventFactory.createStartElement(A_QNAME, Collections.singleton(hrefAttr).iterator(), null));
             destination.add(eventFactory.createCharacters(anchor));
             destination.add(eventFactory.createEndElement(A_QNAME, null));
         }
     }
     
     private String uri(String hostname, String rawPath) {
         try {
             return new URI("http", hostname, rawPath, null).toString();
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
     
     private String query(String raw, String encoding) {
         try {
             return URLEncoder.encode(raw, encoding);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
     
     private String literalLang(Literal literal) {
         if (literal.isWellFormedXML()) {
             try {
                 XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(literal.getLexicalForm()));
                 XMLEvent event;
                 do {
                     event = reader.nextEvent();
                 } while (!event.isStartElement());
                 return event.asStartElement().getAttributeByName(LANG_QNAME).getValue();
             } catch (XMLStreamException e) {
                 throw new RuntimeException(e);
             }
         } else {
             return literal.getLanguage();
         }
     }
     
     private <T> T oneOrNull(List<T> ts) {
         switch (ts.size()) {
             case 0: return null;
             case 1: return ts.get(0);
             default: throw new RuntimeException("Got more than one thing: " + ts);
         }
     }
 
 }
