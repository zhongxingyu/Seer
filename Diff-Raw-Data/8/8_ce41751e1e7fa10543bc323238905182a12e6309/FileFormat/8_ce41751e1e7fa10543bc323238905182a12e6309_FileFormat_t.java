 package com.mediaplatform.util;
 
 import org.jboss.solder.core.Veto;
 
 /**
  * User: Sergey Demin
  * Date: 10.11.2009
  * Time: 13:44:03
  */
 @Veto
 public enum FileFormat {
     BMP("bmp", "image/bmp"),
     JPG("jpg", "image/jpeg"),
     PNG("png", "image/png"),
     PDF("pdf", "application/pdf"),
     AVI("avi", "video/msvideo"),
     FLV("flv", "video/x-flv"),
    MOV("mov", "video/quicktime"),
    MKV("mov", "video/x-matroska"),
     MP4("mp4", "video/mp4"),
     MP3("mp3", "audio/mp3"),
     WEBM("webm", "video/webm"),
     HTML("html", "text/html"),
     ZIP("zip", "application/zip"),
     SWF("swf", "application/x-shockwave-flash"),
     CSS("css", "text/css");
 
 
     private final String ext;
     private final String contentType;
 
     FileFormat(String ext, String contentType) {
         this.ext = ext;
         this.contentType = contentType;
     }
 
     public String getExt() {
         return ext;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public static FileFormat getFormat(String value){
         if (value == null) return null;
         value = value.toLowerCase();
         for (FileFormat format : values()) {
             if (value.endsWith("."+format.getExt())) return format;
         }
         return null;
     }
 
     public static FileFormat getFormatByMime(String value){
         if (value == null) return null;
         value = value.toLowerCase();
         for (FileFormat format : values()) {
             if (value.startsWith(format.getContentType())) return format;
         }
         return null;
     }
 }
