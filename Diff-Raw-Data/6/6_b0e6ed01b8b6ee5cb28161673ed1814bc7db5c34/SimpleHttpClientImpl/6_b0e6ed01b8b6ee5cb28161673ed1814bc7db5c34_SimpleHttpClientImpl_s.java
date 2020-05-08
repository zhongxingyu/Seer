 package filmeUtils.http;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 
 public class SimpleHttpClientImpl implements SimpleHttpClient {
 
 	public static final int TIMEOUT = 60;
 	private final DefaultHttpClient httpclient;
 	private final File cookieFile;
 	
 	public SimpleHttpClientImpl(){
 		this(null);
 	}
 	
 	public SimpleHttpClientImpl(final File cookieFile) {
 		this.cookieFile = cookieFile;
 		httpclient = new DefaultHttpClient(); 
 		loadCookies();
 		final HttpParams httpParameters = httpclient.getParams();
 		final int connectionTimeOutSec= TIMEOUT;
 		final int socketTimeoutSec = TIMEOUT;
 		HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeOutSec * 1000);
 		HttpConnectionParams.setSoTimeout        (httpParameters, socketTimeoutSec * 1000);
 	}
 	
 	public void close() { 
 		httpclient.getConnectionManager().shutdown();
 	}
 
 	public String get(final String get) throws ClientProtocolException, IOException {
 		final HttpGet httpGet = new HttpGet(get); 
 		final String result = executeAndGetResponseContents(httpGet);
 		return result;
 	}
 
 	public String post(final String postUrl, final Map<String, String> params) throws ClientProtocolException, IOException {
 		final HttpPost httpost = new HttpPost(postUrl);
 		final List <NameValuePair> nvps = new ArrayList <NameValuePair>();
 		final Set<Entry<String, String>> entrySet = params.entrySet();
 		for (final Entry<String, String> entry : entrySet) {
 			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
 		}
 		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
 		final String result = executeAndGetResponseContents(httpost);
 		return result;
 	}
 
 	public String getOrNull(final String url) {
 		try {
 			final String getResult = get(url);
 			return getResult;
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	public String getToFile(final String link, final File destFile) throws ClientProtocolException, IOException {
 		final HttpGet httpGet = new HttpGet(link);
 		return executeSaveResponseToFileReturnContentType(httpGet, destFile);
 	} 
 
 	private HttpResponse execute(final HttpUriRequest httpost) throws ClientProtocolException, IOException {
 		return httpclient.execute(httpost);
 	}
 
 	private String executeAndGetResponseContents(final HttpUriRequest httpost)throws IOException, ClientProtocolException {
 		final HttpResponse response = execute(httpost);
 	    final HttpEntity entity = response.getEntity();
 		final InputStream contentIS = entity.getContent();
		final String content = IOUtils.toString(contentIS);
 		contentIS.close();
 		storeCookies();
 		return content;
 	}
 	
 	private String executeSaveResponseToFileReturnContentType(final HttpUriRequest httpost,final File dest)throws IOException, ClientProtocolException {
 		final HttpResponse response = execute(httpost);
 	    final HttpEntity entity = response.getEntity();
 	    final String contentType = entity.getContentType().getValue();
 		final InputStream in = entity.getContent();
 		final OutputStream out = new FileOutputStream(dest);
 		IOUtils.copy(in, out);
 		out.flush();
 		out.close();
 		in.close();
 		storeCookies();
 		return contentType;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadCookies(){
 		if(cookieFile == null)return;
 		if(!cookieFile.exists())return;
 		
 		List<Serializable> serializedCookies = null; 
 		FileInputStream fis = null;
 		ObjectInputStream in = null;
 		try{
 			fis = new FileInputStream(cookieFile);
 			in = new ObjectInputStream(fis);
 			serializedCookies = (List<Serializable>)in.readObject();
 			in.close();
 		}catch(final IOException ex){
 			ex.printStackTrace();
 		}catch(final ClassNotFoundException ex){
 			ex.printStackTrace();
 		}
 		final CookieStore cookieStore = httpclient.getCookieStore();
 		for (final Serializable serializable : serializedCookies) {
 			final Cookie cookie = (Cookie) serializable;
 			cookieStore.addCookie(cookie);
 		}
 	}
 	
 	private void storeCookies() throws IOException{
 		if(cookieFile == null)return;
 		
 		if(!cookieFile.exists()){
 			cookieFile.createNewFile(); 
 		}
 		
 		final CookieStore cookieStore = httpclient.getCookieStore();
 		
 		final List<Cookie> cookies = cookieStore.getCookies();
 		final List<Serializable> serializableCookies = new ArrayList<Serializable>();
 		for (final Cookie cookie : cookies) {
 			try{
 				final Serializable serializableCookie = (Serializable) cookie;
 				serializableCookies.add(serializableCookie);
 			}catch (final ClassCastException e) {
 				serializableCookies.add(new SerializableCookie(cookie));
 			}
 		}
 		
 		FileOutputStream fos = null;
 		ObjectOutputStream out = null;
 		try{
 			fos = new FileOutputStream(cookieFile);
 			out = new ObjectOutputStream(fos);
 			out.writeObject(serializableCookies);
 			out.close();
 		}catch(final IOException ex){
 			ex.printStackTrace();
 		}
 		
 	}
 
 }
