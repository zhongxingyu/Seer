 package org.otherobjects.cms.tools;
 
 import java.io.StringWriter;
 import java.text.NumberFormat;
 import java.util.Locale;
 
 import javax.annotation.Resource;
 
 import net.java.textilej.parser.MarkupParser;
 import net.java.textilej.parser.builder.HtmlDocumentBuilder;
 import net.java.textilej.parser.markup.textile.TextileDialect;
 
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.views.Tool;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Component;
 
 /**
  * Tool to be used from templates to aid in generating formatted text.
  * @author joerg
  *
  */
 @Component
 @Tool
 public class FormatTool
 {
     @Resource
     private MessageSource messageSource;
 
     @Resource
     private OtherObjectsConfigurator otherObjectsConfigurator;
 
     @Resource
     private InlineFormatter inlineFormatter;
 
     public FormatTool()
     {
     }
 
     public String getProperty(String name)
     {
         return otherObjectsConfigurator.getProperty(name);
     }
 
     /**
      * Formats textile string into HTML. HTML special chars in the textileSource will get escaped (notably the less than and greater than signs)
      * @param textileSource
      * @return
      */
     public String formatTextile(String textileSource)
     {
 
         // Remove double line breaks
         String text = textileSource;//textileSource.replaceAll("/\n\n/", "\n");
 
         // Recode headings
        text = text.replaceAll("(?m)^h1. ", "h2. ");
        text = text.replaceAll("(?m)^h2. ", "h3. ");
         text = text.replaceAll("(?m)^h3. ", "h4. ");
 
         // Recode code blocks
         //        text = text.replaceAll("\\[code\\]", "<pre>\n<code>\n");
         //        text = text.replaceAll("\\[/code\\]", "\n</code>\n</pre>");
 
         // Recode links
         //        text = text.replaceAll("\\[LINK:([^|]*)\\|CAPTION:([^\\]]*)\\]", "\"$2\":$1");
         //        text = text.replaceAll("(?m)\\[LINK:([^]]*)\\]", "\"$1\":$1");
 
         // TODO This needs to be optimesd
         // TODO Add support for additional markups
         MarkupParser parser = new MarkupParser(new TextileDialect());
         StringWriter out = new StringWriter();
         HtmlDocumentBuilder builder = new HtmlDocumentBuilder(out);
         builder.setEmitAsDocument(false);
         parser.setBuilder(builder);
         parser.parse(text);
         parser.setBuilder(null);
         String html = out.toString();
 
         if (inlineFormatter != null)
             return inlineFormatter.format(html);
         else
             return html;
     }
 
     /**
      * Formats file size into huma readable form.
      * 
      * @param size in bytes
      * @return formatted string
      */
     public static String formatFileSize(Long size)
     {
         NumberFormat f = NumberFormat.getInstance();
         f.setMaximumFractionDigits(1);
         f.setMinimumFractionDigits(1);
 
         double s = (double) size / (double) 1024;
         if (s < 1028 * 0.8)
         {
             return f.format(s) + " KB";
         }
 
         s /= 1024;
         if (s < 1024 * 0.8)
         {
             return f.format(s) + " MB";
         }
 
         s /= 1024;
         return f.format(s) + " GB";
     }
 
     /**
      * Parses a string and looks up messages if appropriate. Useful for form labels that may or may note use
      * message codes. If the string appears to be a message code then this is looked up otherwise the string
      * is returned unaltered.
      * 
      * TODO Change format.
      * 
      * @param textileSource
      * @return
      */
     public String getMessage(String message)
     {
         if (message.startsWith("$"))
         {
             // Message
             // FIXME Get locale from somewhere better
             return messageSource.getMessage(message.substring(2, message.length() - 1), null, Locale.ENGLISH);
         }
         if (message.contains(".") && !message.endsWith(".") && message.matches("[a-z0-9\\.]*"))
         {
             // Message
             // FIXME proper regexp here
             return messageSource.getMessage(message, null, Locale.ENGLISH);
         }
         else
             return message;
     }
 
     protected void setMessageSource(MessageSource messageSource)
     {
         this.messageSource = messageSource;
     }
 
     protected void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 
     protected void setInlineFormatter(InlineFormatter inlineFormatter)
     {
         this.inlineFormatter = inlineFormatter;
     }
 }
