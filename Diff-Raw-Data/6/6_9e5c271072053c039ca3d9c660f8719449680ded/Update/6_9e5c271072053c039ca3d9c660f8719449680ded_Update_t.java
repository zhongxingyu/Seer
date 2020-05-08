 package gutenberg.collect;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Locale;
 
 public class Update extends Task {
 
     public Update(Path bankroot, String filter) {
         super(bankroot, filter);
         this.workingDir = bankroot.resolve(STAGING);
         this.locker = bankroot.resolve(LOCKER);
     }
     
     @Override
     protected void init() throws Exception {
         this.detectedPath = locker.resolve(todaysFolder());
         if (!Files.exists(detectedPath)) 
             Files.createDirectory(detectedPath);
         this.undetectedPath = locker.resolve("unresolved");
     }
 
     @Override
     protected void execute(Path file) throws Exception {
         
         String base36ScanId = file.getFileName().toString();
         boolean detected = !base36ScanId.endsWith(UNDETECTED);
 
         Path target = null;
         if (detected) {
             target = detectedPath.resolve(base36ScanId);
             if (Files.exists(target))
                 Files.delete(file);
             else if (updateScanId(locker.relativize(target).toString()))
                 Files.move(file, target);
             else
                 Files.delete(file);
         } else {
             target = undetectedPath.resolve(base36ScanId);
             if (Files.exists(target))
                 Files.delete(file);
             else
                 Files.move(file, target);                
         }        
     }
         
     
     @Override
     protected void cleanup() throws Exception {
         conn.disconnect();
     }
 
     private boolean updateScanId(String scanId) throws Exception {        
         String charset = Charset.defaultCharset().name();
         
         String hostport = System.getProperty("user.name").equals("gutenberg")?
             "www.gradians.com": "localhost:3000";        
        URL updateScan = new URL(String.format("http://%s/update_scan_id", hostport));
        String params = String.format("id=%s",
             URLEncoder.encode(scanId, charset));
         conn = (HttpURLConnection)updateScan.openConnection();
         conn.setDoOutput(true); // Triggers HTTP POST
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
         conn.setRequestProperty("Cache-Control", "no-cache");
         conn.getOutputStream().write(params.getBytes(charset));
         conn.getOutputStream().close();
 
         byte[] goodReturn = "{\"status\":\"ok\"}".getBytes();
         byte[] actualReturn = new byte[goodReturn.length];
         while (conn.getInputStream().read(actualReturn) != -1) { }
         conn.getInputStream().close();
         return 200 == conn.getResponseCode() && 
             Arrays.equals(goodReturn, actualReturn);
     }    
 
     private String todaysFolder() {
         Calendar rightNow = Calendar.getInstance();
         return String.format("%s.%s.%s", rightNow.get(Calendar.DAY_OF_MONTH),
             rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH),
             rightNow.get(Calendar.YEAR));
     }
     
     private HttpURLConnection conn;
     private Path locker, undetectedPath, detectedPath;
 
 }
