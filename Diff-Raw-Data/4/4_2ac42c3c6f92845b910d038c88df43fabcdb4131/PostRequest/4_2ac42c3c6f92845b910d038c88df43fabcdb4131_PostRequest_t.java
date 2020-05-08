 package org.vamdc.portal.session.consumers;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.concurrent.Callable;
 
 import org.vamdc.portal.Settings;
 
 
 
 public class PostRequest implements Callable<URL>{
 
 	private URL consumer;
 	private Collection<URL> nodes;
 	public PostRequest(URL consumer, Collection<URL> nodes){
 		this.consumer = consumer;
 		this.nodes = nodes;
 	}
 	
 	public URL call() throws Exception {
 		
 		HttpURLConnection connection = setupConnection();
 		
 		return processResponse(connection);
 	}
 
 	private URL processResponse(HttpURLConnection connection)
 			throws IOException, MalformedURLException {
 		URL result=null;
 		int resultCode = connection.getResponseCode();
 		
 		if (resultCode== HttpURLConnection.HTTP_MOVED_TEMP || resultCode== HttpURLConnection.HTTP_SEE_OTHER || resultCode==HttpURLConnection.HTTP_MOVED_PERM){
 			result = new URL(connection.getHeaderField("Location"));
 		}
 		return result;
 	}
 
 	private HttpURLConnection setupConnection() throws IOException,
 			ProtocolException {
 		HttpURLConnection connection = (HttpURLConnection) consumer.openConnection();
 		connection.setInstanceFollowRedirects(false);
 		connection.setRequestMethod("POST");
 		connection.setReadTimeout(Settings.HTTP_DATA_TIMEOUT.getInt());
 		connection.setDoOutput(true);
 		
 		String data="";
 		for (URL node:nodes){
			if (data.length()>0)
				data+="&";
 			data+=URLEncoder.encode("url", "UTF-8") + "=" + URLEncoder.encode(node.toString(), "UTF-8");
			
 		};
 		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
 		wr.write(data);
 		wr.flush();
 		wr.close();
 		
 		return connection;
 	}
 	
 }
