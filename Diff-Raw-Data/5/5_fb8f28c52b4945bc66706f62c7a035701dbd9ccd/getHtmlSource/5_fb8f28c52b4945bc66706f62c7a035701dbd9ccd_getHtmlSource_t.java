 package com.oboenikui.rsssearch;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 //import javax.swing.JTextArea;
 
 public class getHtmlSource{
     //private JTextArea htmlArea;
     private String rssUrl;
     private String atomUrl;
     public getHtmlSource(String url) throws ClientProtocolException, IOException, URISyntaxException {
         URL u;
         try{
             u = new URL(url);
         } catch (MalformedURLException e) {
             u = new URL("http",url,"");
         }
         HttpClient client = new DefaultHttpClient();
         String source;
         HttpGet hg=new HttpGet(u.toString());
         try{
             HttpResponse response = client.execute(hg);
             final int statusCode = response.getStatusLine().getStatusCode();
             // Bad status
             if (statusCode != HttpStatus.SC_OK) {
                 source = null;
             }
             // Good status
             else {
                 source = EntityUtils.toString(response.getEntity());
             }
             
             Document document = Jsoup.parse(source);
             Elements el1 = document.getElementsByAttributeValue("type","application/rss+xml");
             Elements el2 = document.getElementsByAttributeValue("type","application/atom+xml");
             
             if(el1.size()>0){
                 String url2 = el1.get(0).attributes().get("href");
                 URL u2 = null;
                 try{
                     u2 = new URL(url2);
                 } catch (MalformedURLException e) {
                     u2 = new URL(u.getProtocol(),u.getHost(),u.getFile()+"/"+url2);
                 } finally {
                     rssUrl = u2.toString();
                 }
             }
 
             if(el2.size()>0){
                 String url2 = el2.get(0).attributes().get("href");
                 URL u2 = null;
                 try{
                     u2 = new URL(url2);
                 } catch (MalformedURLException e) {
                    u2 = new URL(u.getProtocol(),u.getHost(),u.getFile()+"/"+url2);
                 } finally {
                    atomUrl = u2.toString();
                 }
             }
         }catch (Exception e) {
             e.printStackTrace();
         }finally{
             client.getConnectionManager().shutdown();
         }
     }
     public String getRssUrl(){
         return rssUrl;
     }
     public String getAtomUrl(){
         return atomUrl;
     }
 }
