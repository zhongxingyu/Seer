 package com.ouchadam.fang.persistance.database;
 
 public class Tables {
 
     private Tables() {
         throw new RuntimeException("This class should not be instantiated");
     }
 
     public enum Channel {
         _id,
         CHANNEL_TITLE,
         CATEGORY,
         SUMMARY,
         URL;
     }
 
     public enum ChannelImage {
         CHANNEL,
        IMAGE_URL,
         LINK,
         TITLE,
         WIDTH,
         HEIGHT;
     }
 
     public enum Item {
         _id,
         CHANNEL,
         TITLE,
         PUBDATE,
         DURATION,
         AUDIO_URL,
         AUDIO_TYPE,
         LINK,
         HERO_IMAGE,
         SUBTITLE,
         SUMMARY;
     }
 
     public enum Playlist {
         ITEM_ID,
         DOWNLOADED,
         DOWNLOAD_ID,
         PLAY_POSITION,
         MAX_DURATION;
     }
 
 }
