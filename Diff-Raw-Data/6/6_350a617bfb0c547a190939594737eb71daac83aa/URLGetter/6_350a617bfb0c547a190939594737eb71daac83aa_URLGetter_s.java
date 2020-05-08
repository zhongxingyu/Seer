 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.UUID;
 
 public class URLGetter {
 
     public static void main(String[] args) {
         if (args.length < 2) {
             System.out.println("Usage: java URLGetter <method> <url> [ <post body filename> ]");
             System.out.println("    Note that you can specify an HTTP proxy using the following JVM args");
             System.out.println("     -Dhttp.proxyHost=proxy.example.com");
             System.out.println("     -Dhttp.proxyPort=1234");
             System.exit(-1);
         }
 
         String method = args[0].toUpperCase();
         String urlIn = args[1];
         String bodyFile = null;
         byte[] body = null;
         if (args.length >= 3) {
             bodyFile = args[2];
             body = Files.readFileAsBytes(bodyFile);
         }
 
         System.out.println(String.format("Sending %s request to %s %s", method, urlIn, bodyFile == null ? "" : "with post body contents from " + bodyFile));
 
         URL url;
         HttpURLConnection connection = null;
         try {
             url = new URL(urlIn);
             connection = (HttpURLConnection)url.openConnection();
             connection.setRequestMethod(method);
 
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
 
             if (body != null && body.length > 0) {
                 connection.setRequestProperty("Content-Length", String.valueOf(body.length));
                 DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                 wr.write(body);
                 wr.flush();
                 wr.close();
             }
 
             InputStream is = connection.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is));
             String line;
             StringBuffer response = new StringBuffer();
             while((line = rd.readLine()) != null) {
                     response.append(line);
                     response.append('\n');
             }
             rd.close();
             System.out.println("Response:");
             System.out.println(response.toString());
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if(connection != null) {
                 connection.disconnect();
             }
         }
     }
   
 }
