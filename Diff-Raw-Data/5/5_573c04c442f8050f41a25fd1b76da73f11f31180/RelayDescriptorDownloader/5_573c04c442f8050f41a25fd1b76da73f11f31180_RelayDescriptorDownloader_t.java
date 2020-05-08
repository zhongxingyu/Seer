 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * Download the current consensus and relevant extra-info descriptors and
  * hand them to the relay descriptor parser.
  */
 public class RelayDescriptorDownloader {
   public RelayDescriptorDownloader(RelayDescriptorParser rdp,
       String authority, SortedMap<String, String> directories) {
     try {
      rdp.initialize();
    } catch (IOException e) {
      return;
    }
    try {
       System.out.print("Downloading current consensus from " + authority
           + "... ");
       URL u = new URL("http://" + authority
           + "/tor/status-vote/current/consensus");
       HttpURLConnection huc = (HttpURLConnection) u.openConnection();
       huc.setRequestMethod("GET");
       huc.connect();
       int response = huc.getResponseCode();
       if (response == 200) {
         BufferedInputStream in = new BufferedInputStream(
             huc.getInputStream());
         StringBuilder sb = new StringBuilder();
         int len;
         byte[] data = new byte[1024];
         while ((len = in.read(data, 0, 1024)) >= 0) {
           sb.append(new String(data, 0, len));
         }
         in.close();
         String consensus = sb.toString();
         rdp.parse(new BufferedReader(new StringReader(consensus)));
       }
       System.out.print("done\nDownloading extra-info descriptors from "
           + authority + "... ");
       Stack<String> extraInfos = new Stack<String>();
       for (String fingerprint : directories.keySet()) {
         u = new URL("http://" + authority + "/tor/extra/fp/"
             + fingerprint);
         huc = (HttpURLConnection) u.openConnection();
         huc.setRequestMethod("GET");
         huc.connect();
         response = huc.getResponseCode();
         if (response == 200) {
           BufferedInputStream in = new BufferedInputStream(
               huc.getInputStream());
           StringBuilder sb = new StringBuilder();
           int len;
           byte[] data = new byte[1024];
           while ((len = in.read(data, 0, 1024)) >= 0) {
             sb.append(new String(data, 0, len));
           }
           in.close();
           String extraInfo = sb.toString();
           if (extraInfo.length() > 0) {
             BufferedReader br = new BufferedReader(
                 new StringReader(extraInfo));
             rdp.parse(br);
           }
         }
       }
       System.out.println("done");
     } catch (IOException e) {
       System.out.println("failed");
     }
   }
 }
 
