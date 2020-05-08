 package torrent;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import torrent.interfaces.IWebRequest;
 import external.http.multipart.FilePart;
 import external.http.multipart.MultipartEntity;
 import external.http.multipart.Part;
 
 public class WebRequest implements IWebRequest
 {
 	@Override
 	public void postRequest(URL url)
 	{
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url.toExternalForm());
 		
 		try
 		{
 			client.execute(request);
 		} 
 		catch (ClientProtocolException e) {}
 		catch (IOException e) {}
 	}
 
 	@Override
 	public String getRequest(URL url)
 	{
 		StringBuilder sb = new StringBuilder();
 		
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url.toExternalForm());
 		
 		try
 		{
 			HttpResponse response = client.execute(request);
 
 			BufferedReader rd = 
 				new BufferedReader(
 						new InputStreamReader(
 								response.getEntity().getContent()));
 			
 			String line = "";
 			
 			while ((line = rd.readLine()) != null) 
 			{
 				sb.append(line);
 			}
 		} 
		catch (ClientProtocolException e) {}
		catch (IllegalStateException e) {}
		catch (IOException e) {}
 		
 		return sb.toString();
 	}
 	
 	@Override
 	public Boolean postRequestWithFile(URL url, File upload)
 	{
 		HttpClient client = new DefaultHttpClient();
 		Boolean success = false; 
 		
 		try {
 			HttpPost httppost = new HttpPost(url.toExternalForm());
 			Part[] parts = { new FilePart("fileEl", upload) };
 			httppost.setEntity(new MultipartEntity(parts, httppost.getParams()));
 
			// Make the request
 			HttpResponse response = client.execute(httppost);
			
 			success =  response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;

		} catch (FileNotFoundException e) {	} 
		catch (Exception e) {	}
 		
 		return success;
 	}
 
 }
