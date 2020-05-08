 package api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.SignatureType;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 // FIXME: credit to original author!
 
 /**
  * @author	Jan Van Haaren
  * @date	27 June 2013
  */
 public class HattrickConnector {
 
 	private static HattrickConnector HATTRICK_CONNECTOR;
 
 	private final String serviceURL = "http://chpp.hattrick.org/chppxml.ashx";
 
 	private final String applicationName = "Match Scanner for Academic Research";
 
 	private final String consumerKey = "";
 
 	private final String consumerSecret = "";
 
 	private final String token = "";
 
 	private final String tokenSecret = "";
 
 	private final OAuthService oauthService;
 
 	private final Token accessToken;
 
 	private HattrickConnector() {
 		this.oauthService = new ServiceBuilder().provider(HattrickAPI.class)
 				.apiKey(this.getConsumerKey())
 				.apiSecret(this.getConsumerSecret())
 				.signatureType(SignatureType.Header).build();
 		this.accessToken = new Token(this.getToken(),this.getTokenSecret());
 	}
 
 	protected static HattrickConnector getInstance() {
 		if (HATTRICK_CONNECTOR == null) {
 			HATTRICK_CONNECTOR = new HattrickConnector();
 		}
 
 		return HATTRICK_CONNECTOR;
 	}
 
 	protected String getWebContent(String path) {
 		return this.getServiceFile(this.getServiceURL() + path);
 	}
 
 	private String getServiceFile(String sURL) {
 		String returnString = "";
 		Response response = null;
 		int responseCode = 200;
 		boolean tryAgain = true;
 
 		try {
 			while (tryAgain == true) {
 				OAuthRequest request = new OAuthRequest(Verb.GET, sURL);
 				request.addHeader("accept-language", "en");
 				request.setConnectionKeepAlive(true);
 				request.setConnectTimeout(60, TimeUnit.SECONDS);
 				request.addHeader("accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
 				request.addHeader("accept-encoding", "gzip, deflate");
 				request.addHeader("user-agent", this.getApplicationName());
 
 				if (this.getAccessToken() == null || this.getAccessToken().getToken().length() == 0) {
 					responseCode = 401;
 				}
 				else {
 					this.getOAuthService().signRequest(this.getAccessToken(), request);
 					response = request.send();
 					responseCode = response.getCode();
 				}
 
 				switch (responseCode) {
 				case 200:
 				case 201:
 					returnString = this.readStream(this.getResultStream(response));
 					tryAgain = false;
 					break;
 				case 407:
 					throw new RuntimeException("HTTP Response Code 407: Proxy authentication required.");
 				default:
 					throw new RuntimeException("HTTP Response Code: " + responseCode);
 				}
 			}
 		}
 		catch (Exception exception) {
 			exception.printStackTrace();
 		}
 
 		return returnString;
 	}
 
 	private InputStream getResultStream(Response response) throws IOException {
 		InputStream resultingInputStream = null;
 		if (response != null) {
 			String encoding = response.getHeader("Content-Encoding");
 			if ((encoding != null) && encoding.equalsIgnoreCase("gzip")) {
 				resultingInputStream = new GZIPInputStream(response.getStream());
 			}
 			else if ((encoding != null) && encoding.equalsIgnoreCase("deflate")) {
 				resultingInputStream = new InflaterInputStream(response.getStream(), new Inflater(true));
 			}
 			else {
 				resultingInputStream = response.getStream();
 			}
 		}
 
 		return resultingInputStream;
 	}
 
 	private String readStream(InputStream inputStream) throws IOException {
 		StringBuilder builder = new StringBuilder();
 		if (inputStream != null) {
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
 			String line = bufferedReader.readLine();
 			if (line != null) {
 				builder.append(line);
 				while ((line = bufferedReader.readLine()) != null) {
 					builder.append("\n");
 					builder.append(line);
 				}
 			}
 			bufferedReader.close();
 		}
 
 		return builder.toString();
 	}
 
 	public String getServiceURL() {
 		return this.serviceURL;
 	}
 
 	private String getApplicationName() {
 		return this.applicationName;
 	}
 
 	private String getConsumerKey() {
 		return this.consumerKey;
 	}
 
 	private String getConsumerSecret() {
 		return this.consumerSecret;
 	}
 
 	private String getToken() {
 		return this.token;
 	}
 
 	private String getTokenSecret() {
 		return this.tokenSecret;
 	}
 
 	private OAuthService getOAuthService() {
 		return this.oauthService;
 	}
 
 	private Token getAccessToken() {
 		return this.accessToken;
 	}
 
 }
