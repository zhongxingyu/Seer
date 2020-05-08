 package storage;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import model.Issue;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ServerStorage extends AsyncTask<String, Void, String> {
 
     private Issue issue;

     @Override
     protected String doInBackground(String... params) {
         return reportIssue();
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
         System.out.println("********CALL STARTED***********");
     }
 
     public String reportIssue() {
         HttpClient httpClient = new DefaultHttpClient();
         String url = "http://kranti-api.herokuapp.com/issues";
         HttpPost httpPost = new HttpPost(url);
         String result = null;
         List<NameValuePair> nameValuePairs = generatePostParams();
         try {
             httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
 
         try {
             HttpResponse httpResponse = httpClient.execute(httpPost);
             StatusLine statusLine = httpResponse.getStatusLine();
             int statusCode = statusLine.getStatusCode();
            System.out.println("******STATUS CODE ********" + statusCode);
             if (statusCode >= 200 && statusCode <= 210) {
                 System.out.println("********SUCCESS*************");
                 HttpEntity httpEntity = httpResponse.getEntity();
                 InputStream content = httpEntity.getContent();
                 result = toString(content);
             }
         } catch (IOException httpResponseError) {
             Log.e("HTTP Response", "IO error");
             return "404 error";
         }
         return result;
     }
 
     private List<NameValuePair> generatePostParams() {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("issue[title]", issue.getTitle()));
         nameValuePairs.add(new BasicNameValuePair("issue[description]", issue.getDescription()));
         nameValuePairs.add(new BasicNameValuePair("issue[location]", issue.getLocation()));
         return nameValuePairs;
     }
 
     @Override
     protected void onPostExecute(String resultString) {
         super.onPostExecute(resultString);
         System.out.println("******CALL COMPLETE***********");
     }
 
     private String toString(InputStream content) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(content));
         StringBuilder result = new StringBuilder();
         String line;
         try {
             while ((line = reader.readLine()) != null) {
                 result.append(line);
             }
         } catch (IOException readerException) {
             readerException.printStackTrace();
         }
         return result.toString();
     }
 
     public void store(Issue issue) {
         this.issue = issue;
         execute();
     }
 }
