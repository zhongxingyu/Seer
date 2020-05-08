 package client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 
 class ServiceUpdateManager {
    private final String URL;
    private HttpClient Client = new DefaultHttpClient(); 
 
    public ServiceUpdateManager(String address) {
       URL = address;
    }
 
    protected double getRefreshRate() {
       double rate = 0d;
      HttpGet httpGet = new HttpGet(URL);
      setRequestHeaders(httpGet);
       return rate;
    }
 
    protected void postRequest() throws IOException {
       HttpPost httpPost = new HttpPost(URL);
       setRequestHeaders(httpPost);
       
       HttpResponse response = Client.execute(httpPost);
       handleResponse(response);
    }
 
    private void setRequestHeaders(HttpRequestBase base) {
       base.setHeader("accept", "application/json");
       base.setHeader("Content-Type", "application/json");
    }
    
    protected void handleResponse(HttpResponse response) throws IOException {
       if (response.getStatusLine().getStatusCode() != 200) {
          throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatusLine().getStatusCode());
       }
 
       BufferedReader br = new BufferedReader(
                           new InputStreamReader((response.getEntity().getContent())));
 
       String output;
       System.out.println("Output from Server .... \n");
       while ((output = br.readLine()) != null) {
          System.out.println(output);
       }
    }
 
    protected void shutdown() {
       Client.getConnectionManager().shutdown();
    }
 }
