 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.SAXParseException;
 import org.xml.sax.SAXException;
 import org.apache.log4j.Logger;
 import uk.org.catnip.eddie.Entry;
 import uk.org.catnip.eddie.Generator;
 import uk.org.catnip.eddie.Author;
 import uk.org.catnip.eddie.Link;
 import uk.org.catnip.eddie.Date;
 import uk.org.catnip.eddie.Category;
 import uk.org.catnip.eddie.Image;
 import uk.org.catnip.eddie.TextInput;
 import uk.org.catnip.eddie.Enclosure;
 import uk.org.catnip.eddie.Source;
 
 public class FeedSAXParser extends BaseSAXParser {
     static Logger log = Logger.getLogger(FeedSAXParser.class);
     private Author author;
     private Category category;
     private Generator generator;
     private Image image;
     private Link link;
     private TextInput textinput;
 
     public void endElement_author() throws SAXException {
         in_author = false;
         String content = pop("author");
         
 
         sync_author(content, "author");
         getCurrentContext().setAuthor(author);
         author = null;
     }
     public void endElement_category() throws SAXException {
         String content = pop("category");
         getCurrentContext().set("category", content);
         
         if (category != null) {
             if (category.getTerm() == null) {
                 category.setTerm(content);
             }
             getCurrentContext().addCategory(category);
         }
         category = null;
     }  
     public void endElement_channel() throws SAXException {
         in_feed = false;
     }
     
     public void endElement_comments() throws SAXException {
         State state = getCurrentState();
         current_entry.set("comments", state.resolveUri(pop("comments")));
     }
     
     public void endElement_content() throws SAXException {
         String content = pop("content");
         in_content--;
         current_entry.addContent(detail);
         current_entry.set("description", content);
     }
     public void endElement_content_encoded() throws SAXException {
         String content = pop("content_encoded");
         in_content--;
         current_entry.addContent(detail);
         current_entry.set("description", content);
     }
 
     public void endElement_contributor() throws SAXException {
         in_author = false;
         sync_author(pop("contributor"), "contributor");
         getCurrentContext().addContributor(author);
         author = null;
     }
     public void endElement_copyright() throws SAXException {
         String content = pop("copyright");
         getCurrentContext().set("copyright", content);
         getCurrentContext().set("rights", content);
         getCurrentContext().setCopyright(detail);
         in_content--;
     }
     
     public void endElement_created() throws SAXException {
         String content = pop("created");
         getCurrentContext().set("created", content);
         getCurrentContext().setCreated(new Date(content,detail));
     }
     
     public void endElement_description() throws SAXException {
         String content = pop("description");
         in_content--;
         if (in_image) {
             image.setDescription(content);
         } else if (textinput != null) {
             textinput.setDescription(content);
         } else {
             if (getCurrentContext().get("summary") == null) {
                 getCurrentContext().set("description", content);
             }
             //getCurrentContext().set("summary", content);
             getCurrentContext().set("tagline", content);
         }
     }
     
     public void endElement_docs() throws SAXException {
         State state = getCurrentState();
         String content = pop("docs");
         log.debug(state);
         content = state.resolveUri(content);
         feed.set("docs",content);
     }
     
     public void endElement_email() throws SAXException {
         String value = pop("email");
         if (in_author) {
             author.setEmail(value);
         } else if (in_contributor) {
             save_contributor("email", value);
         }
     }
 
     public void endElement_entry() throws SAXException {
         pop("entry");
         in_entry = false;
         if (current_entry.get("summary") == null && current_entry.get("description") != null){
             current_entry.set("summary", current_entry.get("description"));
         } else if (current_entry.get("summary") != null && current_entry.get("description") == null){
             current_entry.set("description", current_entry.get("summary"));
         }
         feed.addEntry(current_entry);
         current_entry = null;
     }
 
     public void endElement_generator() throws SAXException {
         String content = pop("generator");
         generator.setDetails(detail);
         if (generator.getName() == null && !content.equals("")) {
             generator.setName(content);
         }
         if (in_source) {
             current_entry.getSource().setGenerator(generator);
             current_entry.getSource().set("generator", content);
         } else {
             feed.setGenerator(generator);
             feed.set("generator", content);
         }
         generator = null;
     }
     public void endElement_guid() throws SAXException {
         String content = pop("guid");
         current_entry.set("guid", content);
         if (current_entry.isGuidIsLink()) {
             current_entry.set("link", content);  
         }
     }
     public void endElement_height() throws SAXException {
         image.setHeight(pop("height"));
     }
     public void endElement_icon() throws SAXException {
         String content = pop("icon");
         if (in_source) {
             current_entry.getSource().set("icon", content);
         } else {
             feed.set("icon", content);
         }
     }
     public void endElement_id() throws SAXException {
         State state = getCurrentState();
         String content = pop("id");
         content = state.resolveUri(content);
         getCurrentContext().set("guid", content);
         getCurrentContext().set("id", content);
     }
 
     public void endElement_image() throws SAXException {
         in_image = false;
         pop("image");
         feed.setImage(image);
         image = null;
     }
     
     public void endElement_info() throws SAXException {
         String content = pop("info");
         getCurrentContext().set("info", content);
         feed.setInfo(detail);
         in_content--;
     }
 
     public void endElement_issued() throws SAXException {
         String content = pop("issued");
         getCurrentContext().set("issued", content);
         getCurrentContext().setIssued(new Date(content,detail));
     }
     
     public void endElement_language() throws SAXException {
         getCurrentContext().set("language", pop("language"));
     }
 
     public void endElement_link() throws SAXException {
         State state = getCurrentState();
         String content = pop("link");
         content = state.resolveUri(content);
         if (in_image) {
             image.setLink(content);
         } else if (textinput != null) {
             textinput.setLink(content);
         } else if (link != null){ // TODO cleanup
             if (in_entry) {
                 current_entry.setGuidIsLink(false);
             }
             link.setDetails(detail);
             getCurrentContext().addLink(link);
             if (link.getHref() != null) {
 
                 if ((link.getType().equals("text/html") || link.getType()
                         .equals("application/xhtml+xml"))
                         && (link.getRel() != null && link.getRel().equals(
                                 "alternate"))) {
 
                     getCurrentContext().set("link", link.getHref());
                 }
                 if (link.getRel() != null && link.getRel().equals("alternate")) {
                     getCurrentContext().set("link", link.getHref());
                     
                 }
             } else {
                 getCurrentContext().set("link", content);
             }
             link = null;
         }
         
 
     }
     
     public void endElement_logo() throws SAXException {
         String content = pop("logo");
         if (in_source) {
             current_entry.getSource().set("logo", content);
         } else {
             feed.set("logo", content);
         }
     }
     
     public void endElement_modified() throws SAXException {
         String content = pop("modified");
         getCurrentContext().set("modified", content);
         getCurrentContext().set("date", content);
         getCurrentContext().setModified(new Date(content,detail));
     }
     public void endElement_name() throws SAXException {
         String content = pop("name");
         if (in_author) {
             author.setName(content);
         } else if (in_contributor) {
             save_contributor("name", content);
         } else if (textinput != null) {
             textinput.setName(content);
         }
 
     }
     public void endElement_publisher() throws SAXException {
         in_author = false;
         sync_author(pop("publisher"), "publisher");
         getCurrentContext().setPublisher(author);
         author = null;
     }
     public void endElement_source() throws SAXException {
         pop("source");
         in_source = false;
     }
     public void endElement_subtitle() throws SAXException {
         String content = pop("subtitle");
         if (in_source) {
         current_entry.getSource().set("subtitle", content);
         current_entry.getSource().setSubtitle(detail);
         } else {
             feed.set("subtitle", content);
             feed.set("tagline", content);
             feed.setSubtitle(detail);
         }
         in_content--;
     }
     public void endElement_summary() throws SAXException {
         String content = pop("summary");
         if (getCurrentContext().getSummary() == null && getCurrentContext().get("description") == null){
             getCurrentContext().set("summary", content);
             getCurrentContext().setSummary(detail);
         } else {
             if (current_entry != null) {
                 current_entry.addContent(detail);
             }
         }
         in_content--;
     }
 
     public void endElement_textinput() throws SAXException {
         String content = pop("textinput");
         feed.setTextinput(textinput);
         textinput = null;
     }
     public void endElement_title() throws SAXException {
         String content = pop("title");
         if (in_image) {
             image.setTitle(content);
         } else if (textinput != null) {
             textinput.setTitle(content);
         } else {
             getCurrentContext().set("title", content);
             getCurrentContext().setTitle(detail);
             in_content--;
         }
     }
     
     public void endElement_ttl() throws SAXException {
         feed.set("ttl", pop("ttl"));
     }
     
     public void endElement_url() throws SAXException {
         State state = getCurrentState();
         String content = pop("url");
         content = state.resolveUri(content);
         if (in_author) {
             author.setHref(content);
         } else if (in_contributor) {
             save_contributor("url", content);
         } else if (in_image) {
             image.setUrl(content);
         }
     }
     public void endElement_wfw_comment() throws SAXException {
         State state = getCurrentState();
         getCurrentContext().set("wfw_comment", state.resolveUri(pop("wfw_comment")));
     }
     public void endElement_wfw_commentrss() throws SAXException {
         State state = getCurrentState();
         getCurrentContext().set("wfw_commentrss", state.resolveUri(pop("wfw_commentrss")));
     }
     public void endElement_width() throws SAXException {
         image.setWidth(pop("width"));
     }
     
     public void save_contributor(String key, String value) {
         log.debug("save_contributors: not implemented");
     }
     
     public void startElement_author(State state) throws SAXException {
         in_author = true;
         state.expectingText = true;
         author = new Author();
         push(state);
     }
     public void startElement_category(State state) throws SAXException {
         category = new Category();
         category.setSchedule(state.getAttr("domain", state.getAttr("scheme")));
         category.setLabel(state.getAttr("label"));
         log.debug(state.getAttr("term"));
         category.setTerm(state.getAttr("term"));
     }
     public void startElement_channel(State state) throws SAXException {
         in_feed = true;
     }
 
     public void startElement_content(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "xml");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = true;
         push(state);
 
     }
     public void startElement_content_encoded(State state) throws SAXException {
         in_content++;
         state.mode = "escaped";
         state.setType("text/html");
         state.expectingText = true;
         push(state);
 
     }
     public void startElement_contributor(State state) throws SAXException {
         in_author = true;
         state.expectingText = true;
         author = new Author();
         push(state);
     }
     
     public void startElement_copyright(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = true;
         push(state);
 
     }
 
     public void startElement_created(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_description(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType("text/html");
         state.expectingText = true;       
         push(state);
     }
 
     public void startElement_enclosure(State state) throws SAXException {
         Enclosure enclosure = new Enclosure();
         enclosure.setUrl(state.getAttr("url"));
         enclosure.setLength(state.getAttr("length"));
         enclosure.setType(state.getAttr("type"));
         current_entry.addEnclosure(enclosure);
     }
     
     public void startElement_entry(State state) throws SAXException {
         in_entry = true;
         current_entry = new Entry();
         state.expectingText = false;
         push(state);
     }
 
     public void startElement_feed(State state) throws SAXException {
 
         this.in_feed = true;
         String attr_version = state.getAttr("version");
         
         if (!this.feed.has("format"))
             if (attr_version == null || attr_version.equals("")) {
                 if (state.getUri().equals("http://www.w3.org/2005/Atom")) {
                     this.feed.set("format", "atom10");
                 } else {
                     this.feed.set("format", "atom");
                 }
             } else {
                 if (attr_version.equals("0.1")) {
                     this.feed.set("format", "atom01");
                 } else if (attr_version.equals("0.2")) {
                     this.feed.set("format", "atom02");
                 } else if (attr_version.equals("0.3")) {
                     this.feed.set("format", "atom03");
                 } else {
                     this.feed.set("format", "atom");
                 }
             }
         // Do some sanity checking
         if (!this.feed.has("format")) {
             throw new SAXParseException("Failed to detect Atom format",
                     this.locator);
         }
     }
 
     public void startElement_generator(State state) throws SAXException {
         generator = new Generator();
         generator.setName(state.getAttr("name"));
         generator.setUrl(state.resolveUri(state.getAttr("url", state.getAttr("uri"))));
         generator.setVersion(state.getAttr("version"));
         state.expectingText = true;
         push(state);
     }
     public void startElement_guid(State state) throws SAXException {
         state.expectingText = true;
         current_entry.setGuidIsLink(state.getAttr("isPermaLink", "true").equals("true"));
         push(state);
     }
     public void startElement_icon(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_image(State state) throws SAXException {
         in_image = true;
         image = new Image();
         push(state);
 
     }
     
     public void startElement_info(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = true;
         push(state);
 
     }
 
     public void startElement_issued(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_link(State state) throws SAXException {
         if (state.getAttr("rel") != null && state.getAttr("rel").equals("enclosure")) {
             Enclosure enclosure = new Enclosure();
             enclosure.setUrl(state.resolveUri(state.getAttr("href")));
             enclosure.setLength(state.getAttr("length"));
             enclosure.setType(state.getAttr("type"));
             if (current_entry != null) {
                 current_entry.addEnclosure(enclosure);
             }
         } 
             link = new Link();
             link.setHref(state.resolveUri(state.getAttr("href")));
             link.setTitle(state.getAttr("title"));
             link.setRel(state.getAttr("rel", "alternate"));
             link.setHreflang(state.getAttr("hreflang"));
             link.setLength(state.getAttr("length"));
             state.expectingText = false;
         
         push(state);
     }
     public void startElement_logo(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_modified(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_publisher(State state) throws SAXException {
         in_author = true;
         state.expectingText = true;
         author = new Author();
         push(state);
     }
     public void startElement_rss(State state) throws SAXException {
         in_feed = true;
         String version = state.getAttr("version");
         if (version != null) {
             if (version.startsWith("2.")) {
                 feed.set("format", "rss20");  
             }else if (version.equals("0.94")) {
                 feed.set("format", "rss094");  
             }else if (version.equals("0.93")) {
                 feed.set("format", "rss093");  
             }else if (version.equals("0.92")) {
                 feed.set("format", "rss092");  
             }else if (version.equals("0.91")) { 
                 // TODO need to check for netscape doctype
                 feed.set("format", "rss091u");  
             }
         } else {
             feed.set("format", "rss");
         }
     }
     public void startElement_source(State state) throws SAXException {
         in_source = true;
         current_entry.setSource(new Source());
         push(state);
 
     }
     public void startElement_subtitle(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = true;
         push(state);
 
     }
     public void startElement_summary(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = true;
         push(state);
 
     }
     
     public void startElement_textinput(State state) throws SAXException {
         textinput = new TextInput();
         push(state);
     }
 
     public void startElement_title(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.setType(state.getAttr("type", "text/plain"));
         state.expectingText = (this.in_feed || this.in_entry);
         push(state);
 
     }
     public void startElement_url(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_wfw_comment(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_wfw_commentrss(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     private void sync_author(String content, String option) {
         log.debug(author);
         String name = author.getName();
         String email = author.getEmail();
         String url = author.getHref();
         if (name != null && email != null && !email.equals("")) {
             getCurrentContext().set(option,  name + " (" + email + ")");
         } else if (name != null && !name.equals("")) {
             getCurrentContext().set(option, name);
         } else {
             getCurrentContext().set(option, content);
             log.debug("sync_author: " + content);
             if (content.contains("(")) {
                 String part1 = content.substring(0, content.indexOf("("))
                         .trim();
                 String part2 = content.substring(content.lastIndexOf("(") + 1,
                         content.lastIndexOf(")")).trim();
                 if (part1.contains("@")) {
                     author.setName(part2);
                     author.setEmail(part1);
                 } else {
                     author.setName(part1);
                     author.setEmail(part2);
                 }
             } else {
                 author.setName(content);
             }
             log.debug(author);
         }
     }
     
 }
