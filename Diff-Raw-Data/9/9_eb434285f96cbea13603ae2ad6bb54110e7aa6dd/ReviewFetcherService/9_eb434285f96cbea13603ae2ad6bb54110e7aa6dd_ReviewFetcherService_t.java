 package com.reviewQueue.service;
 
 
 import com.reviewQueue.model.ReviewTypes;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class ReviewFetcherService {
 
     public static String getReviewJson(ReviewTypes reviewType) throws IOException
     {
        String url = String.format("http://reviewqueue.stage.guardianprofessional.co.uk/review/%s/json", reviewType.Name);
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
         String content = "";
         if (entity != null) {
             content = EntityUtils.toString(entity);
         }
         return content;
     }
 
 
 }
