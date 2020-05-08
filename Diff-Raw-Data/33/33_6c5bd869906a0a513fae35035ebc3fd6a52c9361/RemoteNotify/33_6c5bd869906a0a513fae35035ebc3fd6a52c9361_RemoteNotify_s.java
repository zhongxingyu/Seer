 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.io.OutputStreamWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Properties;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import com.google.gson.Gson;
 public class RemoteNotify{
     public static NotificationResponse sendNotification(String service, String to, String body){
         try{
             service = service.toLowerCase();
             String data = URLEncoder.encode("to", "UTF-8") + "=" + URLEncoder.encode(to, "UTF-8");
             data += "&" + URLEncoder.encode("body", "UTF-8") + "=" + URLEncoder.encode(body, "UTF-8");
 	    HttpURLConnection urlConnection = configureConnection(service, data);
             OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
             osw.write(data);
             osw.flush();
             osw.close();
             System.out.println(urlConnection.getResponseCode());
             System.out.println(urlConnection.getResponseMessage());
 	    String line;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    line = reader.readLine();
            System.out.println(line);
 	    return new NotificationResponse(urlConnection.getResponseCode(), urlConnection.getResponseMessage(), line);
         } catch (IOException e){
            e.printStackTrace();
         }
 	return null;
     }
 
     public static HttpURLConnection configureConnection(String urlEnd, String data) throws IOException{
 	    Properties prop = new Properties();
 	    prop.load(new FileInputStream("/etc/homework/notification.conf"));
 	    String router = prop.getProperty("router_id");
            String urlString = "http://10.2.0.1:8080/notify/1/" + router + "/" + urlEnd;
 	    URL url = new URL(urlString);
             HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
             urlConnection.setRequestMethod("POST");
             urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             urlConnection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
             urlConnection.setUseCaches(false);
             urlConnection.setDoInput(true);
             urlConnection.setDoOutput(true);
 	    return urlConnection;
     }
 }
