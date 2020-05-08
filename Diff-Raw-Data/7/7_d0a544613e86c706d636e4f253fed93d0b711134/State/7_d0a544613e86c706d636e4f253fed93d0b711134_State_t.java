 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.AttributesImpl;
 import java.lang.StringBuilder;
 import java.util.Map;
 import java.util.Hashtable;
 import org.apache.log4j.Logger;
 
 public class State {
     static Logger log = Logger.getLogger(State.class);
 
     private static Map element_aliases = createElementAliases();
 
     private static Map namespace_aliases = createNamespaceAliases();
 
     private static Map createNamespaceAliases() {
         Map aliases = new Hashtable();
         // aliases.put("http://backend.userland.com/rss", "");
         // aliases.put("http://blogs.law.harvard.edu/tech/rss", "");
         // aliases.put("http://purl.org/rss/1.0/", "");
         // aliases.put("http://my.netscape.com/rdf/simple/0.9/", "");
         // aliases.put("http://example.com/newformat#", "");
         // aliases.put("http://example.com/necho", "");
         // aliases.put("http://purl.org/echo/", "");
         // aliases.put("uri/of/echo/namespace#", "");
         // aliases.put("http://purl.org/pie/", "");
         // aliases.put("http://purl.org/atom/ns#", "");
         // aliases.put("http://purl.org/rss/1.0/modules/rss091#", "");
 
         aliases.put("http://webns.net/mvcb/", "admin");
         aliases.put("http://purl.org/rss/1.0/modules/aggregation/", "ag");
         aliases.put("http://purl.org/rss/1.0/modules/annotate/", "annotate");
         aliases.put("http://media.tangent.org/rss/1.0/", "audio");
         aliases.put("http://backend.userland.com/blogChannelModule",
                 "blogChannel");
         aliases.put("http://web.resource.org/cc/", "cc");
         aliases.put("http://backend.userland.com/creativeCommonsRssModule",
                 "creativeCommons");
         aliases.put("http://purl.org/rss/1.0/modules/company", "co");
         aliases.put("http://purl.org/rss/1.0/modules/content/", "content");
         aliases.put("http://my.theinfo.org/changed/1.0/rss/", "cp");
         aliases.put("http://purl.org/dc/elements/1.1/", "dc");
         aliases.put("http://purl.org/dc/terms/", "dcterms");
         aliases.put("http://purl.org/rss/1.0/modules/email/", "email");
         aliases.put("http://purl.org/rss/1.0/modules/event/", "ev");
         aliases.put("http://postneo.com/icbm/", "icbm");
         aliases.put("http://purl.org/rss/1.0/modules/image/", "image");
         aliases.put("http://xmlns.com/foaf/0.1/", "foaf");
         aliases.put("http://freshmeat.net/rss/fm/", "fm");
         aliases.put("http://purl.org/rss/1.0/modules/link/", "l");
         aliases.put("http://madskills.com/public/xml/rss/module/pingback/",
                 "pingback");
         aliases.put("http://prismstandard.org/namespaces/1.2/basic/", "prism");
         aliases.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
         aliases.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
         aliases.put("http://purl.org/rss/1.0/modules/reference/", "ref");
         aliases.put("http://purl.org/rss/1.0/modules/richequiv/", "reqv");
         aliases.put("http://purl.org/rss/1.0/modules/search/", "search");
         aliases.put("http://purl.org/rss/1.0/modules/slash/", "slash");
         aliases.put("http://purl.org/rss/1.0/modules/servicestatus/", "ss");
         aliases.put("http://hacks.benhammersley.com/rss/streaming/", "str");
         aliases.put("http://purl.org/rss/1.0/modules/subscription/", "sub");
         aliases.put("http://purl.org/rss/1.0/modules/syndication/", "sy");
         aliases.put("http://purl.org/rss/1.0/modules/taxonomy/", "taxo");
         aliases.put("http://purl.org/rss/1.0/modules/threading/", "thr");
         aliases.put("http://purl.org/rss/1.0/modules/textinput/", "ti");
         aliases.put("http://madskills.com/public/xml/rss/module/trackback/",
                 "trackback");
         aliases.put("http://wellformedweb.org/CommentAPI/", "wfw");
         aliases.put("http://purl.org/rss/1.0/modules/wiki/", "wiki");
         aliases.put("http://schemas.xmlsoap.org/soap/envelope/", "soap");
         aliases.put("http://www.w3.org/1999/xhtml", "xhtml");
         aliases.put("http://www.w3.org/XML/1998/namespace", "xml");
         return aliases;
     }
 
     private static Map createElementAliases() {
         Map aliases = new Hashtable();
         aliases.put("abstract", "description");
         aliases.put("body", "content");
         aliases.put("content:encoded", "content_encoded");
         aliases.put("dcterms:created", "created");
         aliases.put("dc:author", "author");
         aliases.put("dc:creator", "author");
         aliases.put("dc:contributor", "contributor");
         aliases.put("dc:date", "modified");
         aliases.put("dc:language", "language");
         aliases.put("dc:publisher", "publisher");
         aliases.put("dc:rights", "copyright");
         aliases.put("dc:subject", "category");
         aliases.put("dc:title", "title");
         aliases.put("dcterms:modified", "modified");
         aliases.put("item", "entry");
         aliases.put("feedinfo", "channel");
         aliases.put("fullitem", "content_encoded");
         aliases.put("homepage", "url");
         aliases.put("keywords", "category");
         aliases.put("dcterms:issued", "issued");
         aliases.put("managingeditor", "author");
         aliases.put("product", "item");
         aliases.put("producturl", "link");
         aliases.put("pubdate", "modified");
         aliases.put("published", "dcterms_created");
        aliases.put("rights", "copyright");
         aliases.put("uri", "url");
         aliases.put("webmaster", "publisher");
         aliases.put("xhtml_body", "body");
         aliases.put("updated", "modified");
         return aliases;
     }
 
     public State() {
     }
 
     public State(String uri, String localName, String qName) {
         this.uri = uri;
         this.localName = localName.toLowerCase();
         if (namespace_aliases.containsKey(this.uri)) {
             this.namespace = (String) namespace_aliases.get(this.uri);
         }
         this.element = aliasElement(this.namespace, this.localName);
         this.qName = qName;
     }
 
     public State(String uri, String localName, String qName, Attributes atts,
             State prev) {
         this.uri = uri;
         this.localName = localName.toLowerCase();
         this.qName = qName;
         this.atts = atts;
         if (namespace_aliases.containsKey(this.uri)) {
             this.namespace = (String) namespace_aliases.get(this.uri);
         }
 
         this.element = aliasElement(this.namespace, this.localName);
 
         this.type = this.getAttr("type", prev.type);
         this.mode = this.getAttr("mode", prev.mode);
         if (this.type == null || this.type.equals("")) {
             this.type = "text/plain";
         }
         this.language = this.getAttr("xml:lang", prev.getLanguage());
         this.base = this.getAttr("xml:base", prev.getBase());
         //log.debug(this);
 
     }
 
     private String uri;
 
     public boolean content = false;
 
     private String localName;
 
     private String namespace;
 
     private String element;
 
     public String qName;
 
     public Attributes atts = new AttributesImpl();
 
     private String language;
 
     public String base;
 
     public String mode;
 
     private String type;
 
     public boolean expectingText = false;
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{");
         sb.append("element = '" + element + "', ");
         sb.append("mode = '" + mode + "', ");
         sb.append("type = '" + type + "', ");
         sb.append("base = '" + base + "', ");
         sb.append("language = '" + language + "', ");
         sb.append("namespace = '" + namespace + "', ");
         sb.append("uri = '" + uri + "', ");
         sb.append("qname = '" + qName + "', ");
         sb.append("localname = '" + localName + "', ");
         sb.append("text = '" + text + "'");
         sb.append("}");
         return sb.toString();
     }
 
     public StringBuilder text = new StringBuilder();
 
     public void addText(String str) {
         text.append(str);
     }
 
     public String getAttr(String key) {
         return this.getAttr(key, null);
     }
 
     public String getAttr(String key, String default_value) {
         // TODO: remove this hack
         if (key.equals("type") && namespace != null && namespace.equals("xhtml")) {
             return "application/xhtml+xml";
         }
         String ret = atts.getValue(key);
         if (ret == null) {
             ret = default_value;
         }
         log.trace("getAttr: " + key + " = '" + ret + "'");
         return ret;
     }
 
     public String getElement() {
         return element;
     }
 
     public void setElement(String element) {
         this.element = element;
     }
 
     private String aliasElement(String namespace, String element) {
         if (namespace != null && !namespace.equals("xhtml")) {
             element = namespace + ":" + element;
         }
         if (element_aliases.containsKey(element)) {
             return (String) element_aliases.get(element);
         }
         return element;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public String getBase() {
         return base;
     }
 
     public void setBase(String base) {
         this.base = base;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         if (type.equals("text")) {
             this.type = "text/plain";
         } else if (type.equals("html")) {
             this.type = "text/html";
         } else if (type.equals("xhtml")) {
             this.type = "application/xhtml+xml";
         } else {
             this.type = type;
         }
     }
 }
