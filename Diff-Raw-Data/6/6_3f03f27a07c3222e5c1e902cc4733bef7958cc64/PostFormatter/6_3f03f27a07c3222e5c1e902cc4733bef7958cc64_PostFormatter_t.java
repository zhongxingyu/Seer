 package cc.hughes.droidchatty;
 
 import android.text.Spanned;
 import android.view.View;
 
 public class PostFormatter {
 
     public static Spanned formatContent(Post post, boolean multiLine)
     {
         return formatContent(post, null, multiLine);
     }
     
     public static Spanned formatContent(Post post, View view, boolean multiLine)
     {
         return formatContent(post.getUserName(), post.getContent(), view, multiLine);
     }
 
     public static Spanned formatContent(Thread thread, boolean multiLine)
     {
         return formatContent(thread.getUserName(), thread.getContent(), null, multiLine);
     }
 
     public static Spanned formatContent(String userName, String content, final View view, boolean multiLine)
     {
         if (userName.equalsIgnoreCase("shacknews"))
         {
            // fix escaped html
             content = content.replaceAll("&lt;(/?)a(.*?)&gt;", "<$1a$2>");
            content = content.replaceAll("&lt;br /&gt;", "<br />");
            content = content.replaceAll("&lt;(/?)span(.*?)&gt;", "<$1span$2>");
             
             // make relative link absolute
             content = content.replaceAll("href=\"/", "href=\"http://www.shacknews.com/");
         }
         
         return ShackTags.fromHtml(content, view, !multiLine);
     }
 }
