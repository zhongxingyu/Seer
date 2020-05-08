 import com.soundcloud.api.*;
 import java.io.IOException;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 
 public class Content 
 {
     public static final int MAXTRACKS = 100;
     private Stream[] stream;
     private String[] track;
     private String[] stream_url;
     private String[] url;
      
     public Content() throws IOException
     {    
         stream = new Stream[MAXTRACKS];
         track = new String[MAXTRACKS];
         stream_url = new String[MAXTRACKS];
         url = new String[MAXTRACKS];
     }
     
     public HttpResponse getContent(ApiWrapper wrapper) throws IOException
     {
         HttpResponse response;
         String response_string;
         
         response = wrapper.get(Request.to("/me/tracks/"));
         response_string = Http.formatJSON(Http.getString(response));
 
         if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
         {
             System.out.println("\n" + response_string);
             getTracks(response_string);
             
            response = wrapper.get(Request.to(url[0]));
            response_string = Http.formatJSON(Http.getString(response));
            System.out.println("\n----------------\n" + response_string);
             
         } 
         else 
         {
             System.err.println("Invalid status received: " + response.getStatusLine());
         }  
 
         return response;
     }
     
     public void getTracks(String data)
     {
         //Hard-code values for now
         track[0] = "1";
         stream_url[0] = "https://api.soundcloud.com/tracks/75596002/stream";
         url[0] = "https://api.soundcloud.com/tracks/75596002/download";
     }
 }
