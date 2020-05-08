 package org.codehaus.xsite.extractors;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.codehaus.xsite.FileSystem;
 import org.codehaus.xsite.PageExtractor;
 import org.codehaus.xsite.io.CommonsFileSystem;
 import org.codehaus.xsite.model.Link;
 import org.codehaus.xsite.model.Page;
 
 import com.opensymphony.module.sitemesh.html.BasicRule;
 import com.opensymphony.module.sitemesh.html.HTMLProcessor;
 import com.opensymphony.module.sitemesh.html.Tag;
 import com.opensymphony.module.sitemesh.html.TagRule;
 import com.opensymphony.module.sitemesh.html.TextFilter;
 import com.opensymphony.module.sitemesh.html.rules.BodyTagRule;
 import com.opensymphony.module.sitemesh.html.rules.HeadExtractingRule;
 import com.opensymphony.module.sitemesh.html.rules.MetaTagRule;
 import com.opensymphony.module.sitemesh.html.rules.TitleExtractingRule;
 import com.opensymphony.module.sitemesh.html.util.CharArray;
 
 /**
  * PageExtractor which extract page information from an HTML file using the
  * SiteMesh library.
  * 
  * @author Joe Walnes
  * @author J&ouml;rg Schaible
  */
 public class SiteMeshPageExtractor implements PageExtractor {
 
     private String filename;
     private String head;
     private String body;
     private Collection<Link> links;
     private final TagRule[] rules;
     private final TextFilter[] filter;
     private final FileSystem fileSystem;
     private final CharacterEscaper characterEscaper;
 	private final AttributedPageBuilder pageBuilder;
 
     public SiteMeshPageExtractor() {
         this(new TagRule[0], new TextFilter[0], new CommonsFileSystem());
     }
 
     public SiteMeshPageExtractor(TagRule[] rules, FileSystem fileSystem) {
         this(rules, new TextFilter[0], fileSystem);
     }
 
     public SiteMeshPageExtractor(TextFilter[] filter, FileSystem fileSystem) {
         this(new TagRule[0], filter, fileSystem);
     }
 
     public SiteMeshPageExtractor(TagRule[] rules, TextFilter[] filter, FileSystem fileSystem) {
         this(rules, filter, fileSystem, new CharacterEscaper());
     }
 
     public SiteMeshPageExtractor(TagRule[] rules, TextFilter[] filter, FileSystem fileSystem, CharacterEscaper characterEscaper) {
     	this(rules, filter, fileSystem, characterEscaper, new AttributedPageBuilder(characterEscaper));
     }
 
    public SiteMeshPageExtractor(TagRule[] rules, TextFilter[] filter, FileSystem fileSystem,  CharacterEscaper characterEscaper, AttributedPageBuilder pageBuilder) {
         this.rules = rules;
         this.filter = filter;
         this.fileSystem = fileSystem;
         this.characterEscaper = characterEscaper;
 		this.pageBuilder = pageBuilder;
     }
 
     public Page extractPage(File htmlFile) {
         links = new HashSet<Link>();
         try {
             filename = htmlFile.getName();
             extractContentFromHTML(fileSystem.readFile(htmlFile).toCharArray());
             return new Page(filename, head, body, links, new HashMap<String, String>(pageBuilder.getProperties()));
         } catch (IOException e) {
             throw new CannotParsePageException(e);
         }
     }
 
     public Page extractPage(String filename, String htmlContent) {
         links = new HashSet<Link>();
         try {
             this.filename = filename;
             extractContentFromHTML(htmlContent.toCharArray());
             return new Page(filename, head, body, links, new HashMap<String, String>(pageBuilder.getProperties()));
         } catch (IOException e) {
             throw new CannotParsePageException(e);
         }
     }
 
     private void extractContentFromHTML(char[] rawHTML) throws IOException {
     	pageBuilder.getProperties().clear();
     	
         // buffers to hold head and body content
         CharArray headBuffer = new CharArray(64);
         CharArray bodyBuffer = new CharArray(4096);
 
         // setup rules for html processor
         HTMLProcessor htmlProcessor = new HTMLProcessor(rawHTML, bodyBuffer);
         htmlProcessor.addRule(new BodyTagRule(pageBuilder, bodyBuffer));
         htmlProcessor.addRule(new HeadExtractingRule(headBuffer));
         htmlProcessor.addRule(new TitleExtractingRule(pageBuilder));
         htmlProcessor.addRule(new MetaTagRule(pageBuilder));
         htmlProcessor.addRule(new LinkExtractingRule());
 
         for (int i = 0; i < rules.length; i++) {
             htmlProcessor.addRule(rules[i]);
         }
         for (int i = 0; i < filter.length; i++) {
             htmlProcessor.addTextFilter(filter[i]);
         }
 
         // go!
         htmlProcessor.process();
         this.head = characterEscaper.escape(headBuffer.toString());
         this.body = characterEscaper.escape(bodyBuffer.toString());
     }
 
     @SuppressWarnings("serial")
     public static class CannotParsePageException extends RuntimeException {
         public CannotParsePageException(Throwable cause) {
             super(cause);
         }
     }
 
     /**
      * Rule for HTMLProcessor that records all <a href=""> links.
      */
     private class LinkExtractingRule extends BasicRule {
 
         public boolean shouldProcess(String tag) {
             return tag.equals("a");
         }
 
         public void process(Tag tag) {
             if (tag.hasAttribute("href", false)) {
                 links.add(new Link(tag.getAttributeValue("title", false), tag.getAttributeValue("href", false)));
             }
             tag.writeTo(currentBuffer());
         }
     }
 
 }
