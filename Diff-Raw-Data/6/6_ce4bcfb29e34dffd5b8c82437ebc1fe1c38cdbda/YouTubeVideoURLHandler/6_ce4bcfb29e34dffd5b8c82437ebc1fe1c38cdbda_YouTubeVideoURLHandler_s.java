 package org.pircbotx.listeners.urlhandlers;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.joda.time.Duration;
 import org.pircbotx.util.URLUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 import com.google.common.net.InternetDomainName;
 import com.google.gdata.client.youtube.YouTubeService;
 import com.google.gdata.data.Category;
 import com.google.gdata.data.youtube.VideoEntry;
 import com.google.gdata.util.ServiceException;
 
 /**
  * A {@link VideoURLHandler} specifically made to retrieve video information from YouTube.
  *
  * @author Emmanuel Cron
  */
 public class YouTubeVideoURLHandler extends VideoURLHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeVideoURLHandler.class);
   private static final String GDATA_URL = "http://gdata.youtube.com/feeds/api/videos/%s?hl=%s";
   private static final String YOUTUBE_SHORT_URL = "http://youtu.be/%s";
   private static final String YOUTUBE_COM = "youtube.com";
   private static final String YOUTU_BE = "youtu.be";
 
   private String applicationName;
   private String hl;
   private int connectTimeoutMillis;
   private int readTimeoutMillis;
 
   /**
    * Creates a new handler.
    *
    * @param applicationName the name under which this handler will identify itself on YouTube
    * @param hl the language in which retrieve categories in {@code ISO-639-1} format
    * @param connectTimeoutMillis millis after which the connection attempt is abandoned
    * @param readTimeoutMillis millis after which the read attempt is abandoned
    */
   public YouTubeVideoURLHandler(String applicationName, String hl, int connectTimeoutMillis,
       int readTimeoutMillis, VideoInfoFormat format) {
     super(format);
 
     checkArgument(!Strings.isNullOrEmpty(applicationName),
         "Application name cannot be null or empty");
     checkArgument(!Strings.isNullOrEmpty(hl), "Language must be provided");
     checkArgument(connectTimeoutMillis > 0, "Connect timeout must be > 0");
     checkArgument(readTimeoutMillis > 0, "Read timeout must be > 0");
 
     this.applicationName = applicationName;
     this.hl = hl;
     this.connectTimeoutMillis = connectTimeoutMillis;
     this.readTimeoutMillis = readTimeoutMillis;
   }
 
   @Override
   public int getUrlRequiredMinLength() {
     // youtu.be/<id>
     return 10;
   }
 
   @Override
   public boolean matches(URL url) {
     if (YOUTU_BE.equalsIgnoreCase(url.getHost())) {
       return true;
     }
 
     String host = InternetDomainName.from(url.getHost()).topPrivateDomain().toString();
     if (host.equalsIgnoreCase(YOUTUBE_COM)) {
       return true;
     }
     return false;
   }
 
   @Override
   public VideoInfo retrieveVideoInfo(URL url) {
     String videoId = parseVideoId(url);
     if (Strings.isNullOrEmpty(videoId)) {
       return null;
     }
 
     YouTubeService service = new YouTubeService(applicationName);
     service.setConnectTimeout(connectTimeoutMillis);
     service.setReadTimeout(readTimeoutMillis);
 
     VideoEntry videoEntry;
     try {
       URL gDataUrl = new URL(String.format(GDATA_URL, videoId, hl));
       videoEntry = service.getEntry(gDataUrl, VideoEntry.class);
     } catch (ServiceException | IOException e) {
       LOGGER.warn(String.format("Could not retrieve video information, ignoring: %s", videoId), e);
       return null;
     }
 
     URL videoUrl;
     try {
       videoUrl = new URL(String.format(YOUTUBE_SHORT_URL, videoId));
     } catch (MalformedURLException murle) {
       LOGGER.error("Could not format short YouTube URL with video id, ignoring", murle);
       return null;
     }
     VideoInfo videoInfo = new VideoInfo(videoUrl, videoEntry.getTitle().getPlainText());
     if (videoEntry.getAuthors().size() > 0) {
       videoInfo.setUser(videoEntry.getAuthors().get(0).getName());
     }
     if (videoEntry.getCategories().size() > 0) {
       for (Category category : videoEntry.getCategories()) {
         if (!Strings.isNullOrEmpty(category.getLabel())) {
           videoInfo.setCategory(category.getLabel());
           break;
         }
       }
     }
     if (videoEntry.getMediaGroup() != null) {
       videoInfo.setDuration(new Duration(videoEntry.getMediaGroup().getDuration() * 1000));
     }
    videoInfo.setLikes(videoEntry.getYtRating().getNumLikes());
    videoInfo.setDislikes(videoEntry.getYtRating().getNumDislikes());
     videoInfo.setViews(videoEntry.getStatistics().getViewCount());
     return videoInfo;
   }
 
   // internal helpers
 
   private String parseVideoId(URL url) {
     String host = InternetDomainName.from(url.getHost()).topPrivateDomain().toString();
 
     if (host.equalsIgnoreCase(YOUTU_BE)) {
       if (url.getPath().length() < 1) {
         // No video id provided
         return null;
       }
       return url.getPath().substring(1);
     }
 
     if (host.equalsIgnoreCase(YOUTUBE_COM)) {
       if (url.getPath().length() > 3 && url.getPath().toLowerCase().startsWith("/v/")) {
         return url.getPath().substring(3);
       }
       if (url.getPath().toLowerCase().equals("/watch")) {
         return URLUtils.getQueryParamFirstValue(url, "v");
       }
       // Not recognized
     }
     return null;
   }
 }
