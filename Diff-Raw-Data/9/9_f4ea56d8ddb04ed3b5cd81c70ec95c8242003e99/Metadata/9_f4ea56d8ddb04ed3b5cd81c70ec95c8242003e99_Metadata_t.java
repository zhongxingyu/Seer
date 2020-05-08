 package com.vhly.epubmaker.epub;
 
 /**
  * Created by IntelliJ IDEA.
  * User: vhly[FR]
  * Date: 11-9-6
  * Email: vhly@163.com
  */
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Metadata for opf file
  */
 public class Metadata {
     private String identifier_BookID;
     private String title;
     private String rights;
     private String publisher;
     private String subject;
     private String date;
     private String description;
     private String creator;
     private String language;
     private String cover_image;
 
 
     public String getIdentifier() {
         return identifier_BookID;
     }
 
     public void setIdentifier(String identifier_BookID) {
         this.identifier_BookID = identifier_BookID;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getRights() {
         return rights;
     }
 
     public void setRights(String rights) {
         this.rights = rights;
     }
 
     public String getPublisher() {
         return publisher;
     }
 
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getCreator() {
         return creator;
     }
 
     public void setCreator(String creator) {
         this.creator = creator;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public String getCover_image() {
         return cover_image;
     }
 
     public void setCover_image(String cover_image) {
         this.cover_image = cover_image;
     }
 
     public String toXML() {
         String ret;
         StringBuffer sb = new StringBuffer();
         sb.append("<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:opf=\"http://www.idpf.org/2007/opf\"\nxmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:calibre=\"http://calibre.kovidgoyal.net/2009/metadata\"\nxmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
 
         sb.append("<dc:title>").append(title).append("</dc:title>");
 
         sb.append("<dc:creator opf:role=\"aut\">").append(creator).append("</dc:creator>");
 
         sb.append("<dc:description>").append(description).append("</dc:description>");
 
         Date currentDate = new Date();
         SimpleDateFormat format = new SimpleDateFormat();
         format.applyPattern("yyyy-MM-dd");
         String ff = format.format(currentDate);
         sb.append("<dc:date opf:event=\"creation\">").append(ff).append("</dc:date>");
 
        sb.append("<dc:identifier id=\"uuid_id\" opf:scheme=\"uuid\">").append(identifier_BookID).append("</dc:identifier>");
 
         sb.append("<dc:subject>").append(subject).append("</dc:subject>");
 
         sb.append("<dc:contributor opf:role=\"bkp\">").append(creator).append("</dc:contributor>");
 
         sb.append("<dc:publisher>").append(publisher).append("</dc:publisher>");
 
         sb.append("<dc:format>epub</dc:format>");
 
         sb.append("</metadata>");
         ret = sb.toString();
         return ret;
     }
 }
