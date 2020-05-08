 package net.anzix.fbfeed.output;
 
 /**
  * RSS Embedable box with an optional title and thumbnail.
  */
 public class HtmlTableContainer {
     private String title;
     private String link;
     private String message;
     private String thumbnail;
     private String image;
 
     public HtmlTableContainer(String title, String link, String message) {
         this.title = title;
         this.link = link;
         this.message = message;
     }
 
     public String getHtml() {
         StringBuilder b = new StringBuilder();
         b.append("<table style=\"margin-left:15px;border:2px;background-color:#EFEFEF;\">");
         if (title != null) {
             String t = title;
             if (link != null && link.length() > 0) {
                 t = "<a href=\"" + link + "\">" + t + "</a>";
             }
             b.append("<tr><td " + (thumbnail != null ? "colspan=\"2\"" : "") + " >" + t + "</td></tr>");
         }
         b.append("<tr>");
         if (thumbnail != null) {
            b.append("<td style=\"width: 30px;\"><img src=\"" + thumbnail + "\"/></td>");
         }
        b.append("<td style=\"vertical-align:top;background-color:#FEFEFE;padding:3px;\">" + (message != null ? message : "") + "</td>");
         b.append("</tr>");
         b.append("</table>");
         return b.toString();
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public void setThumbnail(String thumbnail) {
         this.thumbnail = thumbnail;
     }
 
     public void setImage(String image) {
         this.image = image;
     }
 }
