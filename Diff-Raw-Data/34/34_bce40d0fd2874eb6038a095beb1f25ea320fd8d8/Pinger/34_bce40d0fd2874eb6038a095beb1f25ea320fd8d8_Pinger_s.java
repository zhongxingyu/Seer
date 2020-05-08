 package sibli;
 
 import java.util.*;
 import java.util.regex.*;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import java.io.FileInputStream;
 import java.io.DataInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 
 //import org.apache.log4j.Logger;
 import java.util.logging.Logger;
 
 import java.text.DecimalFormat;
 
 
 /**
  * Pinger class.
  *
  */
 public class Pinger {
   //public static final Logger LOG = Logger.getLogger(Pinger.class);
   private static final Logger LOG = Logger.getLogger(Pinger.class.getName());
 
   protected List<HashMap<String,Object>> hosts = null;
   protected List<HashMap<String,Object>> hostsQueue = null;
 
 
   public Pinger() {
     this( getSources() );
   } // constructor
   
   public Pinger(List<HashMap<String,Object>> hosts) {
     this.hosts = hosts;
     this.hostsQueue = new ArrayList<HashMap<String,Object>>(this.maxConcurrentRequests);
   } // constructor
 
   public static void main(String[] args) {
     // Just in case.
     List<HashMap<String,Object>> hosts = getSources();
 
     Iterator<HashMap<String,Object>> it = hosts.iterator();
     while( it.hasNext() ) {
       System.out.println( "Host: " + it.next().toString() );
     }
   } // main
 
 
 
   /**
    * Pings provided url and returns status code from response.
    *
    */
   public static String ping(String url)
   {
     HttpURLConnection connection = null;
 
     long startTime = System.nanoTime();
     LOG.info("Pinging " + url + "...");
 
     try {
       URL u = new URL(url);
       connection = (HttpURLConnection) u.openConnection();
       connection.setRequestMethod("HEAD");
       connection.setRequestProperty("HTTP_USER_AGENT", "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.10.229 Version/11.61");
 
       connection.setConnectTimeout(5000);
       connection.setReadTimeout(5000);
 
       int code = connection.getResponseCode();
       //connection.getOutputStream().close();
 
       double time = ( System.nanoTime() - startTime ) / 1000000000.0;
       LOG.info("\t\t done in " + (new DecimalFormat("#.#####").format(time)) + " s");
 
       return Integer.toString(code);
 
     } catch (MalformedURLException e) {
       return "Malformed URL";
     } catch (IllegalArgumentException e) {
       return "URL is null";
     } catch (SocketTimeoutException e) {
       LOG.info("\t failed after " + (new DecimalFormat("#.#####").format( ( System.nanoTime() - startTime ) / 1000000000.0 )) + " s");
       return "Timeout";
     } catch (IOException e) {
       return "Host unreachable";
     } finally {
       if (connection != null) {
         try {
           connection.getInputStream().close();
         } catch (IOException e) {
           // do nothing.
         }
       }
     }
   } // ping
 
 
   /**
    * Reads hosts from config file app/config/monitoring.cfg
    * and returns the as list.
    *
    * @return List
    */
   public static final List<HashMap<String,Object>> getSources()
   {
     String config = "app/config/monitoring.cfg";
 
     FileInputStream fstream = null;
     DataInputStream input   = null;
     BufferedReader bufferReader = null;
 
     //List<String> hosts = new ArrayList<String>();
 
     // Oh, Java. This means [{k: v}, {k: v}, {k: v}, ...]
     List<HashMap<String,Object>> hosts = new ArrayList<HashMap<String,Object>>();
     HashMap<String,Object> host = null;
 
     String readLine = null;
     String[] parsedLine = null;
     String href = null;
     String html = null;
 
     Pattern urlRe = Pattern.compile("^(?:(https?|ftp)://)?([a-z0-9-]+(?:\\.[a-z0-9-]+)+)?(.*?)?(?:(\\w+\\.\\w+)([^.]*))?$");
     Matcher urlMatcher = null;
 
     try {
       fstream = new FileInputStream(config);
       input   = new DataInputStream(fstream);
       bufferReader = new BufferedReader( new InputStreamReader(input) );
 
       while ( ( readLine = bufferReader.readLine() ) != null ) {
         if (readLine.trim().length() == 0)
           continue;
 
         parsedLine = readLine.split("\t");
         href = parsedLine[0].trim();
         if (href.length() == 0)
           continue;
 
         urlMatcher = urlRe.matcher(href);
 
         if ( !urlMatcher.matches() || urlMatcher.group(2) == null )
           continue;
 
         html = (parsedLine.length < 2) ? "" : parsedLine[1].trim();
 
         host = new HashMap<String,Object>();
         host.put( "host", urlMatcher.group(2).intern() ); // host
         host.put( "href", href );
         host.put( "html", html );
 
         hosts.add( host );
       } // while
 
     } catch (FileNotFoundException e) {
       //System.out.println( "File not found: " + e.getMessage() );
     } catch (IOException e) {
       //System.out.println( "Error: " + e.getMessage() );
     } finally {
       try {
         if (fstream != null)
           fstream.close();
         if (input != null)
           input.close();
         if (bufferReader != null)
             bufferReader.close();
       } catch (IOException e) {
         // do nothing
       }
     } // finally
 
     return hosts;
   } //getSources
 
 } // Pinger class
