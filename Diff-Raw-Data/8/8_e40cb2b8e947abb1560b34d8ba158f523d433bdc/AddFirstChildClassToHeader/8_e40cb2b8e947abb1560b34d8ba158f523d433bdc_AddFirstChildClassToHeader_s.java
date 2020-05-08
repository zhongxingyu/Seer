 package org.codehaus.xsite.extractors.sitemesh.rules;
 
 import com.opensymphony.module.sitemesh.html.BasicRule;
 import com.opensymphony.module.sitemesh.html.CustomTag;
 import com.opensymphony.module.sitemesh.html.Tag;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Rule for HTMLProcessor that adds class=""FirstChild" to the first header of the body if it is the first element.
  */
 public class AddFirstChildClassToHeader extends BasicRule {
     private boolean firstChildIsHeader = true;
     private final Pattern pattern;
 
     public AddFirstChildClassToHeader() {
         pattern = Pattern.compile("^H[1-9]$", Pattern.CASE_INSENSITIVE);
     }
 
     public boolean shouldProcess(String tag) {
         final Matcher matcher = pattern.matcher(tag);
         return tag.equalsIgnoreCase("p") || matcher.matches();
     }
 
     public void process(Tag tag) {
         if (firstChildIsHeader) {
             if (!tag.getName().equalsIgnoreCase("p")) {
                final CustomTag customTag;
                // http://jira.opensymphony.com/browse/SIM-202
                if (tag.getAttributeCount() == 0) {
                    customTag = new CustomTag(tag.getName(), tag.getType());
                } else {
                    customTag = new CustomTag(tag);
                }
                 customTag.addAttribute("class", "FirstChild");
                 tag = customTag;
             }
             firstChildIsHeader = false;
         }
         tag.writeTo(currentBuffer());
     }
 }
