 package eu.piotrbuda.twittory.core;
 
import org.apache.commons.lang3.Validate;
 import twitter4j.Status;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * .
  */
 public class LinkExtractor {
     /**
      * Pattern that matches http and https urls
      */
     private static final Pattern SCHEMA_PATTERN = Pattern.compile("https?://[-\\w+&@#/%=~()|?!:,.;]*[-\\w+&@#/%=~()|]", Pattern.CASE_INSENSITIVE);
     /**
      * Pattern that matches urls starting with www (missing schema)
      */
     private static final Pattern SCHEMALESS_PATTERN = Pattern.compile("www\\.[-\\w+&@#/%=~()|?!:,.;]*[-\\w+&@#/%=~()|]", Pattern.CASE_INSENSITIVE);
 
     /**
      * Extracts link from a Twitter status.
      *
      * @param status status to be inspected for link existence
      * @return empty string if there is no link in status or string representing an url if one is present
      */
     public String extractLinkFrom(Status status) {
        Validate.notNull(status, "Status cannot be null");
         String text = status.getText();
         String url = matchPattern(SCHEMA_PATTERN, text);
         if (url.equals("")) {
             url = matchPattern(SCHEMALESS_PATTERN, text);
             if (!url.equals("")) {
                 url = "http://" + url;
             }
             return url;
         }
         return url;
     }
 
     private String matchPattern(Pattern pattern, String text) {
         Matcher matcher = pattern.matcher(text);
         if (matcher.find()) {
             return text.substring(matcher.start(), matcher.end());
         }
         return "";
     }
 }
