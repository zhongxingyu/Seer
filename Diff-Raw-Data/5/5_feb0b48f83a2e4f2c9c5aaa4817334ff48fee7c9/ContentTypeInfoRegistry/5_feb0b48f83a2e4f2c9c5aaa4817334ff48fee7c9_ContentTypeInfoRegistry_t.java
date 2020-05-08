 /*
  * Created on May 6, 2006
  */
 package uk.org.ponder.rsf.content;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /** Holds a registry of core ContentTypeInfo records.
  * To implement a user-defined set, simply provide your own definition
  * for the RSF application-scope bean named "contentTypeInfoMap".
  * **/
 
 public class ContentTypeInfoRegistry {
   public static final String HTML = "HTML";
   public static final String HTML_FRAGMENT = "HTML-FRAGMENT";
   public static final String AJAX = "AJAX";
   public static final String XUL = "XUL";
   public static final String SVG = "SVG";
   public static final String RSS_0_91 = "RSS-0.91";
   public static final String RSS_2 = "RSS-2.0";
   public static final String REDIRECT = "REDIRECT";
   /** A default ContentTypeInfo entry for HTML content */
   
   public static final ContentTypeInfo HTML_CONTENTINFO = 
     new ContentTypeInfo(HTML, "html",  "<!DOCTYPE html      "
         + "PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
         + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
         "text/html; charset=UTF-8",
         ContentTypeInfo.ID_FULL, false
         );
   
   public static final ContentTypeInfo HTML_FRAGMENT_CONTENTINFO = 
     new ContentTypeInfo(HTML_FRAGMENT, "html",  "",
         "text/html; charset=UTF-8",
        ContentTypeInfo.ID_FULL, true
         );
   
   public static final ContentTypeInfo AJAX_CONTENTINFO = 
     new ContentTypeInfo(AJAX, "xml",  "",
         "application/xml; charset=UTF-8"
         );
 
   public static final ContentTypeInfo XUL_CONTENTINFO = 
     new ContentTypeInfo(XUL, "xul",  "",
         "application/vnd.mozilla.xul+xml; charset=UTF-8"
         );
   
   public static final ContentTypeInfo RSS_0_91_CONTENTINFO = 
     new ContentTypeInfo(RSS_0_91, "rss",  
         "<!DOCTYPE rss PUBLIC \"-//Netscape Communications//DTD RSS 0.91//EN\"" +
        "\"http://my.netscape.com/publish/formats/rss-0.91.dtd\">",
         "application/rss+xml; charset=UTF-8"
         );
   
   public static final ContentTypeInfo RSS_2_0_CONTENTINFO = 
     new ContentTypeInfo(RSS_2, "rss", "",
         "application/rss+xml; charset=UTF-8"
         );
   
   public static final ContentTypeInfo SVG_CONTENTINFO = 
     new ContentTypeInfo(SVG, "svg", "",
         "image/svg+xml; charset=UTF-8",
         ContentTypeInfo.ID_FULL , true
         );
   
   private static Map contentmap = new HashMap();
   
   public static void addContentTypeInfo(Map map, ContentTypeInfo toadd) {
     map.put(toadd.typename, toadd);
   }
   
   static {
     addContentTypeInfo(contentmap, HTML_CONTENTINFO);
     addContentTypeInfo(contentmap, HTML_FRAGMENT_CONTENTINFO);
     addContentTypeInfo(contentmap, AJAX_CONTENTINFO);
     addContentTypeInfo(contentmap, XUL_CONTENTINFO);
     addContentTypeInfo(contentmap, RSS_0_91_CONTENTINFO);
     addContentTypeInfo(contentmap, RSS_2_0_CONTENTINFO);
     addContentTypeInfo(contentmap, SVG_CONTENTINFO);
   }
   
   public static Map getContentTypeInfoMap() {
     return contentmap;
   }
 }
