 package br.ufmg.dcc.vod.spiderpig.jobs.youtube;
 
 import java.io.IOException;
 
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.api.services.youtube.YouTube;
 
 public class YTConstants {
 
 	public static final String BKOFF_TIME = "worker.job.youtube.backofftime";
 	public static final String SLEEP_TIME = "worker.job.youtube.sleeptime";
 	
 	public static final String DEV_KEY_V2 = "worker.job.youtube.v2.devkey";
	public static final String APP_NAME_V2 = "worker.job.youtube.v2.appname";
 	
 	public static final String API_KEY = "worker.job.youtube.v3.apikey";
 	
 	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
 	public static final NullInitializer INITIALIZER = new NullInitializer();
 	
 	private static class NullInitializer implements HttpRequestInitializer {
 		@Override
 		public void initialize(HttpRequest arg0) throws IOException {
 		}
 	}
 	
 	public static YouTube buildYoutubeService() {
 		return new YouTube.Builder(YTConstants.HTTP_TRANSPORT, 
 				YTConstants.JSON_FACTORY, 
 				YTConstants.INITIALIZER).
 				setApplicationName("Simple API Access").build();
 	}
 }
