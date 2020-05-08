 package com.allplayers.objects;
 
 import java.util.Date;
 import java.util.TimeZone;
 
 public class AlbumData extends DataObject {
     private String uuid = "";
     private String title = "";
     private String description = "";
     private int photo_count = 0;
     private String modified_date = "";
     private String rep_photo = ""; // Cover Photo.
 
     public AlbumData() {
 
     }
 
     private Date parseTimestamp(String timestamp) {
         Date date = new Date(Long.parseLong(timestamp));
 
         TimeZone timezone = TimeZone.getDefault();
         int offset = timezone.getOffset(date.getTime());
         date = new Date(date.getTime() + offset);
         return date;
     }
 
     public String getUUID() {
         return uuid;
     }
 
     public String getId() {
         return uuid;
     }
 
     public String getTitle() {
         return title;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getPhotoCount() {
         return photo_count;
     }
 
     public Date getModifedDate() {
         return parseTimestamp(modified_date + "000"); // "+ "000"" Converts from seconds to milliseconds.
     }
 
     public String getModifiedDateString() {
         return parseTimestamp(modified_date + "000").toString(); // "+ "000"" Converts from seconds to milliseconds.
     }
 
     public String getCoverPhoto() {
         return rep_photo;
     }
 }
