 package nz.co.searchwellington.flickr;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import com.aetrion.flickr.Flickr;
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.REST;
 import com.aetrion.flickr.photos.PhotoList;
 
 public class FlickrApi {
     
     private static Logger log = Logger.getLogger(FlickrApi.class);
     
     private String apiKey;
 	private String apiSecret;
     
     public FlickrApi() {      
     }
         
     public void setApiKey(String apiKey) {
         this.apiKey = apiKey;
     }
     
     public void setApiSecret(String apiSecret) {
 		this.apiSecret = apiSecret;
 	}
 
 	public int getPoolPhotoCountForTag(String tagName, String poolGroupId) {
         boolean groupIdSet = poolGroupId != null && !poolGroupId.trim().equals("");
         if (groupIdSet) {
             log.info("Checking count for tag " + tagName + " in pool group id " + poolGroupId);
             try {                
                 Flickr flickr = new Flickr(apiKey, apiSecret, new REST());
                 String[] tags = new String[1];
                tags[0] = tagName;                
                 PhotoList photos = flickr.getPoolsInterface().getPhotos(poolGroupId, tags, 0, 0);   
                 return photos.getTotal();
                 
             } catch (IOException e) {
                log.error(e);
             } catch (SAXException e) {
                 log.error(e);
             } catch (FlickrException e) {
                 log.error(e);
             } catch (ParserConfigurationException e) {
                 log.error(e);
             }
         } else {
             log.warn("Not checking for Flickr counts; pool group id is not set.");
         }
         return 0;
     }
     
 }
