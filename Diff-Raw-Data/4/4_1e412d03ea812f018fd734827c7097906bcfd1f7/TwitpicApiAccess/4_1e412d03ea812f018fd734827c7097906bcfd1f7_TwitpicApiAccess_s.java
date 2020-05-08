 package de.geotweeter.apiconn;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DecimalFormat;
 
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.scribe.exceptions.OAuthException;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 
 import android.util.Log;
 import android.util.Pair;
 
 import com.alibaba.fastjson.JSON;
 
 import de.geotweeter.Constants;
 import de.geotweeter.Utils;
 import de.geotweeter.exceptions.PermanentTweetSendException;
 import de.geotweeter.exceptions.TemporaryTweetSendException;
 import de.geotweeter.exceptions.TweetSendException;
 import de.geotweeter.timelineelements.Url;
 
 public class TwitpicApiAccess {
 	
 	protected final String LOG = "TwitpicApiAccess";
 	
 	private transient TwitterApiAccess twitter_api;
 	
 	public enum ImageSize {
 		MINI, THUMB, LARGE, FULL
 	}
 	
 	public TwitpicApiAccess(Token twitterToken) {
 		twitter_api = new TwitterApiAccess(twitterToken);
 	}
 
 	public String uploadImage(File image, String text, long imageSize) throws IOException, TweetSendException {
 		OAuthRequest request = new OAuthRequest(Verb.POST, Constants.TWITPIC_URI);
 		MultipartEntity entity = new MultipartEntity();
 		entity.addPart("key", new StringBody(Utils.getProperty("twitpic.key")));
		entity.addPart("message", new StringBody(text));
 		
 		ContentBody picture = null;
 		if (imageSize < 0 || image.length() <= imageSize) {
 			picture = new FileBody(image);
 		} else {
 			picture = new ByteArrayBody(Utils.reduceImageSize(image, imageSize), image.getName());
 		}
 		entity.addPart("media", picture);
 		
 		Log.d(LOG, "Start output Stream, Twitpic " + image.getName());
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		entity.writeTo(out);
 		Log.d(LOG, "Finish output Stream, Twitpic " + image.getName());
 
 		request.addPayload(out.toByteArray());
 		request.addHeader(entity.getContentType().getName(), entity.getContentType().getValue());
 		out.close();
 
 		OAuthRequest verified_credentials = twitter_api.getVerifiedCredentials();
 		
 		request.addHeader("X-Auth-Service-Provider", Constants.URI_VERIFY_CREDENTIALS_1);
 		request.addHeader("X-Verify-Credentials-Authorization", verified_credentials.getHeaders().get("Authorization"));
 
 		Log.d(LOG, "Send Twitpic " + image.getName());
 		Response response;
 		try {
 			response = request.send();
 		} catch(OAuthException e) {
 			// TODO In the next scribe version will be more differentiated Exception classes for
 			// connection problems and so on. We really should use that.
 			throw new TemporaryTweetSendException();
 		}
 		Log.d(LOG, "Finished Send Twitpic " + image.getName());
 		
 		if (response.isSuccessful()) {
 			return JSON.parseObject(response.getBody()).getString("url");
 		} else {
 			if (response.getCode() >= 500) {
 				throw new TemporaryTweetSendException();
 			} else {
 				throw new PermanentTweetSendException();
 			}
 		}
 		
 	}
 	
 	public static Pair<URL, URL> getUrlPair(Url url) throws MalformedURLException {
 		URL screen_url = new URL(url.expanded_url);
 		return new Pair<URL, URL>(new URL("http://twitpic.com/show/mini/" + screen_url.getPath()), 
 				new URL("http://twitpic.com/show/full/" + screen_url.getPath()));
 	}
 	
 	public static String getPlaceholder(int index) {
 		DecimalFormat df = new DecimalFormat("000");
 		return Constants.TWITPIC + "pic" + df.format(index);
 	}
 	
 	// TODO Whitespaces berücksichtigen
 	public static String replacePlaceholder(String text, String url, int index) {
 		return text.replace(getPlaceholder(index) , url);
 	}
 	
 }
