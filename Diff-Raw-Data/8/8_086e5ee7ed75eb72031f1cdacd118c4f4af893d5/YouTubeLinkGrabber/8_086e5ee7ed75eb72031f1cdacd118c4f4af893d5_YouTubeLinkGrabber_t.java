 package polly.linkexpander.core.grabbers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 
 public class YouTubeLinkGrabber extends AbstractHttpRequestGrabber {
     
     
     private final static Pattern LINK_PATTERN = Pattern.compile(
            "(https?://www\\.youtube\\.com/watch\\?v=\\S+)");
     
     private final static Pattern META_PATTERN = Pattern.compile(
         "<meta\\s+name=\"title\"\\s+content=\"([^\"]+)\">");
     
     private static final int REQUEST_URL_GROUP = 1;
     private static final int TITLE_GROUP = 1;
     
     
     
     @Override
     public Pattern getLinkPattern() {
         return LINK_PATTERN;
     }
 
     
     
     @Override
     public String processResponseLine(String line) {
         Matcher m = META_PATTERN.matcher(line);
         if (m.find()) {
             return line.substring(m.start(TITLE_GROUP), m.end(TITLE_GROUP));
         }
         return null;
     }
 
     
     
     @Override
     public String getLink(String input, Matcher matcher) {
         String url = input.substring(matcher.start(REQUEST_URL_GROUP), 
             matcher.end(REQUEST_URL_GROUP));
         
         return url;
     }
 
 }
