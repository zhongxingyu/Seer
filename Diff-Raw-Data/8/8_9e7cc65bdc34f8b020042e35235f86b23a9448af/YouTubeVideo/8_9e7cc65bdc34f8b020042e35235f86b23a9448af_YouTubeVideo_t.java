 package net.kuehldesign.backuptube.site.youtube.video;
 
 import com.google.gson.Gson;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.LinkedList;
 import net.kuehldesign.backuptube.BackupHelper;
 import net.kuehldesign.backuptube.exception.BadVideoException;
 import net.kuehldesign.backuptube.exception.FatalBackupException;
 import net.kuehldesign.backuptube.site.youtube.YouTubeHelper;
 import net.kuehldesign.backuptube.video.DownloadableVideo;
 import net.kuehldesign.jnetutils.JNetUtils;
 import net.kuehldesign.jnetutils.exception.UnableToGetSourceException;
 
 public class YouTubeVideo implements DownloadableVideo {
     private YouTubeVideoTitle title;
     private YouTubeVideoDescription content;
     private YouTubeVideoDate published;
     private LinkedList<YouTubeVideoLink> link;
     private LinkedList<YouTubeVideoAuthor> author;
     private LinkedList<YouTubeVideoCategory> category;
     private boolean hasError = false;
     private String source;
     private String url;
 
     private int cacheFormatValue = 0;
     private String cacheURL;
     private String cacheToken;
 
     private YouTubeVideo responseVideo;
     private boolean hasFoundResponseInfo = false;
 
     public String getTitle() {
         return title.getTitle();
     }
 
     private void findResponseInfo() {
         if (! hasFoundResponseInfo() && source.indexOf("This video is a response to <a href=\"") > (- 1))  {
             try {
                 String vid = BackupHelper.between(source, "This video is a response to <a href=\"/watch?v=", "&amp;");
                
                 URL videoURL = new URL("http://gdata.youtube.com/feeds/api/videos/" + URLEncoder.encode(vid, "UTF-8") + "?v=2&alt=json");
 
                 URLConnection connection = videoURL.openConnection();
                 YouTubeVideoWithEntry videoEntry = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), YouTubeVideoWithEntry.class);
                 responseVideo = videoEntry.getVideo();
                 responseVideo.init();
             } catch (Exception ex) {
                ex.printStackTrace();
             }
         }
 
         hasFoundResponseInfo = true;
     }
 
     private boolean hasFoundResponseInfo() {
         return hasFoundResponseInfo;
     }
 
     public YouTubeVideo getResponseVideo() {
        findResponseInfo();
        
         return responseVideo;
     }
 
     public String getTags() {
         String tags = "";
 
         for (int i = 2; i < category.size(); i ++) {
             tags += category.get(i).getTerm() + " ";
         }
 
         if (tags.length() > 1) {
             tags = tags.substring(0, tags.length() - 1);
         }
 
         return tags;
     }
 
     public String getCategory() { // as far as I can tell category is always index #1
         return category.get(1).getLabel();
     }
 
     public String getUploader() {
         return author.get(0).getAuthor();
     }
 
     public String getDescription() {
         return content.getDescription();
     }
 
     public long getPublished() {
         return published.getDate();
     }
 
     public String getURL() {
         if (url != null) {
             return url;
         }
 
         return link.get(0).getURL();
     }
 
     public String getVideoID() {
         String url = getURL();
         return BackupHelper.between(url, "?v=", "&feature");
     }
 
 //    public String getToken() {
 //        return cache_token;
 //    }
 
     private String findToken(String source) throws FatalBackupException, BadVideoException {
         try {
             String t = URLEncoder.encode(BackupHelper.between(source, "\"t\": \"", "\""), "UTF-8");
 
             if (! t.endsWith("%3D")) {
                 t = BackupHelper.betweenMore(source, "&t=", "&", 2);
             }
 
             if (t == null) {
                 // http://www.youtube.com/get_video_info?video_id=ID
                 String infoSource;
 
                 try {
                     infoSource = JNetUtils.getSource("http://www.youtube.com/get_video_info?video_id=" + getVideoID());
                 } catch (UnableToGetSourceException ex) {
                     throw new BadVideoException("Unable to get source for info");
                 }
 
                 String infoSourceUnencoded;
 
                 infoSourceUnencoded = URLDecoder.decode(infoSource, "UTF-8");
                 
                 if (infoSourceUnencoded.contains("status=fail")) {
                     hasError = true;
                 } else {
                     t = BackupHelper.between(infoSourceUnencoded, "&token=", "&");
                 }
             }
 
             return t;
         } catch (UnsupportedEncodingException ex) {
             throw new FatalBackupException("UTF-8 encoding not supported");
         }
     }
 
     public String getDownloadURL() {
         return cacheURL;
     }
 
     private void findURLs(String source, String source18) throws FatalBackupException {
         // URL map
         String urlMap = BackupHelper.between(source, "&fmt_url_map=", "&") + "%2C" + BackupHelper.between(source18, "&fmt_url_map=", "&");
         String[] mapParts = urlMap.split("%2C");
 
         for (String mapPart : mapParts) {
             String[] parts = mapPart.split("%7C");
             String qual;
             String url;
 
             try {
                 qual = URLDecoder.decode(parts[0], "UTF-8");
                 url = URLDecoder.decode(parts[1], "UTF-8");
             } catch (UnsupportedEncodingException ex) {
                 throw new FatalBackupException("UTF-8 encoding not supported");
             } catch (ArrayIndexOutOfBoundsException ex) {
                 continue;
             }
 
             // qual is an identifier based on the type of video it is (e.g. MP4 High Quality)
             int format = 0;
 
             switch (Integer.valueOf(qual)) {
                 case 13:
                     format = YouTubeHelper.FORMAT_3GP_LOW;
                 break;
 
                 case 16:
                     format = YouTubeHelper.FORMAT_3GP_MEDIUM;
                 break;
 
                 case 36:
                     format = YouTubeHelper.FORMAT_3GP_HIGH;
                 break;
 
                 case 5:
                     format = YouTubeHelper.FORMAT_FLV_LOW;
                 break;
 
                 case 34:
                     format = YouTubeHelper.FORMAT_FLV_MEDIUM;
                 break;
 
                 case 6:
                     format = YouTubeHelper.FORMAT_FLV_MEDIUM2;
                 break;
 
                 case 35:
                     format = YouTubeHelper.FORMAT_FLV_HIGH;
                 break;
 
                 case 18:
                     format = YouTubeHelper.FORMAT_MP4_HIGH;
                 break;
 
                 case 22:
                     format = YouTubeHelper.FORMAT_MP4_720P;
                 break;
 
                 case 37:
                     format = YouTubeHelper.FORMAT_MP4_1080P;
                 break;
 
                 case 38:
                     format = YouTubeHelper.FORMAT_MP4_4K;
                 break;
             }
 
             if (cacheFormatValue < format) {
                 cacheFormatValue = format;
                 cacheURL = url;
             }
         }
     }
 
     public String getExtension() {
         if (cacheFormatValue >= YouTubeHelper.FORMAT_MP4_HIGH) {
             return "mp4";
         } else if (cacheFormatValue >= YouTubeHelper.FORMAT_FLV_LOW) {
             return "flv";
         } else {
             return "3gp";
         }
     }
 
     public void setURL(String url) {
         this.url = url;
     }
 
     public void init() throws FatalBackupException, BadVideoException {
         String source18;
 
         try {
             source = JNetUtils.getSource(getURL());
             source18 = JNetUtils.getSource(getURL() + "&fmt=18");
 
             cacheToken = findToken(source);
         } catch (Exception ex) {
             ex.printStackTrace();
             throw new BadVideoException(ex.getMessage());
         }
 
         findURLs(source, source18);
         
         if (getURL() == null || getURL().length() <= 0) {
             throw new BadVideoException("Couldn't get a URL");
         }
     }
 
     public String getSiteID() {
         return BackupHelper.SITE_YOUTUBE;
     }
 
     public YouTubeVideo() {
     }
 }
