 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import models.Source;
 import models.User;
 
 import org.apache.commons.io.IOUtils;
 
 import play.Logger;
 import play.Play;
 import play.modules.facebook.FbGraphException;
 import play.mvc.Http;
 import play.mvc.Router;
 import play.mvc.Router.ActionDefinition;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 public class Sources extends FacebookLoggedInController{
 	  
 	public static final String AWS_ACCESS_KEY = "AKIAJLGV564C5LYG5WHQ";
 	public static final String AWS_SECRET_KEY = "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";
     public static final String BUCKET_NAME = "media.9dials.com";
   
     public static String getFileUrl(String fileName){
     	return "http://"+BUCKET_NAME+"/"+fileName;
     }
     
 	public static void index() {
 		render();
 	}
 
 	public static void audio() {
 		render();
 	}
 	
 	public static void upload(File file, String name) throws FileNotFoundException, FbGraphException {
 		User fbUser = FacebookSecurity.getCurrentFbUser();	
 	    AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
 	    AmazonS3 s3Client = new AmazonS3Client(awsCredentials);
 	    String s3key = UUID.randomUUID().toString();
 	    s3Client.putObject(BUCKET_NAME, s3key, file);
 	    String url = getLocalUrl(s3key);
 	    Source source = new Source(fbUser,file.getName(),s3key,name, url);
 	    source.save();
 	    index();
 	}
 	
 	public static List<Source> findMine() throws FbGraphException{
 		User fbUser = FacebookSecurity.getCurrentFbUser();		
 		return Source.find("byCreator", fbUser).fetch(10);
 	}
 	
 	private static String getLocalUrl(String s3key) {
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("sourceId", s3key);	
 		String url = getFull9DialsUrl("MyClips.index", map);
 		return url;
 	}
 
 	private static String getPlayableUrl(String s3key) {
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("sourceId", s3key);	
 		String url = getFull9DialsUrl("Share.index", map);
 		return url;
 	}
 	
 	public static void uploadLocally(File file) {
 	    
 	    FileOutputStream moveTo = null;
 	    try {
 	        moveTo = new FileOutputStream(new File("/tmp/test.ogg"));
 	        FileInputStream fileInputStream = new FileInputStream(file);
             IOUtils.copy(fileInputStream, moveTo);
         } catch (IOException e) {
             Logger.error(e, "local upload error");
         }
 	}
 	
 	public static String getFull9DialsUrl(String action, Map<String, Object> args) {
         ActionDefinition actionDefinition = Router.reverse(action, args);
         String base = getBase9DialsUrl();
         if (actionDefinition.method.equals("WS")) {
             return base.replaceFirst("https?", "ws") + actionDefinition;
         }
         return base + actionDefinition;
     }
 	
 	public static String getFullUrl(String action, Map<String, Object> args) {
         ActionDefinition actionDefinition = Router.reverse(action, args);
         String base = getBaseUrl();
         if (actionDefinition.method.equals("WS")) {
             return base.replaceFirst("https?", "ws") + actionDefinition;
         }
         return base + actionDefinition;
     }
 
     // Gets baseUrl from current request or application.baseUrl in application.conf
     protected static String getBase9DialsUrl() {
             // No current request is present - must get baseUrl from config
             String appBaseUrl = Play.configuration.getProperty("application.baseUrl", "application.baseUrl");
             if (appBaseUrl.endsWith("/")) {
                 // remove the trailing slash
                 appBaseUrl = appBaseUrl.substring(0, appBaseUrl.length()-1);
             }
             return appBaseUrl;
     }
 
     protected static String getBaseUrl() {
         // No current request is present - must get baseUrl from config
         String appBaseUrl = Play.configuration.getProperty("facebook.baseUrl", "facebook.baseUrl");
         if (appBaseUrl.endsWith("/")) {
             // remove the trailing slash
             appBaseUrl = appBaseUrl.substring(0, appBaseUrl.length()-1);
         }
         return appBaseUrl;
     }
     
 	public static String getIndexUrl(){
		Map<String,Object> map = new HashMap<String,Object>();
		return getFullUrl("sources.index", map);
 	}
 }
