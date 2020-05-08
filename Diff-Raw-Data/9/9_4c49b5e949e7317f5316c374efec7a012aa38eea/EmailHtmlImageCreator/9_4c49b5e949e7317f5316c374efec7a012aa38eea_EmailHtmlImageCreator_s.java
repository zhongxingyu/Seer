 package com.abudko.reseller.huuto.notification.email;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class EmailHtmlImageCreator {
 
     static final String IMAGE_CONTENT_FORMAT = "<img src=\"%s\">";
 
    static final String IMAGE_EXTENSION_ORIG = "-medium.jpg";
 
     public String generateHtmlForResponse(String imgBaseSrc) {
         StringBuilder sb = new StringBuilder("<html>");
         sb.append("\n");
 
         String imageContent = String.format(IMAGE_CONTENT_FORMAT, getImgUrl(imgBaseSrc));
         sb.append(imageContent);
 
         sb.append("\n");
         sb.append("</html>");
 
         return sb.toString();
     }
 
     private String getImgUrl(String imgBaseSrc) {
         if (imgBaseSrc == null) {
             return "";
         }
         StringBuilder imgeSrcBuilder = new StringBuilder(imgBaseSrc);
         imgeSrcBuilder.append(IMAGE_EXTENSION_ORIG);
         return imgeSrcBuilder.toString();
     }
 }
