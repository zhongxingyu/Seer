 package com.desdegdl;
 
 import com.desdegdl.util.URLReader;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  *
  * @author joel
  */
 public class ObtenerActasPREP {
 
     public final static int NUMBER_OF_CONCURRENT_THREADS = 3;
     public static final String PREPURL = "http://prep2012.ife.org.mx/prep/DetalleCasillas";
     public static final String ACTASURL = "https://prep2012.ife.org.mx/actas/";
     public static final String ACTASEXTENSION = ".jpg";
     public static final String PURLSFILENAME = "processedurls.txt";
     public static final String STOPCRAWLERFILENAME = "/tmp/stopactas";
     public static final String WGETPATH = "/usr/local/bin/wget";
     private HashMap<String, String> sections = new HashMap<String, String>();
     private FileWriter pURLsWriter = null;
     private final String lineseparator = System.getProperty("line.separator");
 
     public ObtenerActasPREP() {
         InputStream in = null;
         InputStreamReader isr = null;
         BufferedReader br = null;
 
         try {
             in = ObtenerActasPREP.class.getResourceAsStream("/secciones.txt");
             isr = new InputStreamReader(in);
             br = new BufferedReader(isr);
             String strLine;
 
             while ((strLine = br.readLine()) != null) {
                 String[] split = StringUtils.split(strLine, "|");//strLine.split("|");
                 //System.out.println(split.length);
                 //System.out.println(Arrays.toString(split));
                 String edoId = split[0].trim();
                 String secId = split[2].trim();
                 //System.out.println(edoId + " : " + secId);
                 if (!this.sections.containsKey(edoId + "+" + secId)) {
                     this.sections.put(edoId + "+" + secId, "?idEdo=" + edoId + "&votoExt=true&seccion=" + secId);
                     //System.out.println("?idEdo=" + edoId + "&votoExt=true&seccion=" + secId);
                 }
             }
             //System.out.println("urls size: " + sections.size()); 
         } catch (Exception exc) {
             exc.printStackTrace(System.err);
             System.exit(-1);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ex) {
                 }
             }
             if (isr != null) {
                 try {
                     isr.close();
                 } catch (IOException ex) {
                 }
             }
             if (br != null) {
                 try {
                     br.close();
                 } catch (IOException ex) {
                 }
             }
         }
 
         File purlsFile = new File(PURLSFILENAME);
 
         if (purlsFile.exists()) {
             FileInputStream fstream = null;
 
             try {
                 fstream = new FileInputStream(PURLSFILENAME);
                 in = new DataInputStream(fstream);
                 isr = new InputStreamReader(in);
                 br = new BufferedReader(isr);
                 String strLine;
 
                 while ((strLine = br.readLine()) != null) {
                     strLine = StringUtils.substringAfter(strLine, PREPURL + "?idEdo=");
                     String edoId = StringUtils.substringBefore(strLine, "&");
                     String secId = StringUtils.substringAfter(strLine, "&seccion=");
                     this.sections.remove(edoId + "+" + secId);
                 }
             } catch (Exception exc) {
                 exc.printStackTrace(System.err);
                 System.exit(-1);
             } finally {
                 if (fstream != null) {
                     try {
                         fstream.close();
                     } catch (IOException ex) {
                     }
                 }
                 if (in != null) {
                     try {
                         in.close();
                     } catch (IOException ex) {
                     }
                 }
                 if (isr != null) {
                     try {
                         isr.close();
                     } catch (IOException ex) {
                     }
                 }
                 if (br != null) {
                     try {
                         br.close();
                     } catch (IOException ex) {
                     }
                 }
             }
         } else {
             try {
                 purlsFile.createNewFile();
             } catch (IOException exc) {
                 exc.printStackTrace(System.err);
                 System.exit(-1);
             }
         }
         
         try {
             pURLsWriter = new FileWriter(PURLSFILENAME, true);
         } catch (IOException exc) {
             exc.printStackTrace(System.err);
             System.exit(-1);
         }
     }
 
     private void wget(String url) {
        if (url == null) {
            return;
        }
        
         try {
             Process process = new ProcessBuilder(new String[]{WGETPATH, "-q", "-U MSIE 6.0", url}).start();
             process.waitFor();
         } catch (InterruptedException ex) {
             Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
         } catch (IOException ex) {
             Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
         }
     }
 
     private void shutdown() {
         try {
             this.pURLsWriter.close();
         } catch (IOException ex) {
             Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
         }
     }
 
     private void addProcessedURL(String url) {
         if (url == null) {
             return;
         }
 
         try {
             this.pURLsWriter.write(url + this.lineseparator);
             this.pURLsWriter.flush();
         } catch (IOException ex) {
             Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
         }
     }
 
     public void getAllActas() {
         ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
 
         List<Future<CrawlerResult>> actasCrawlersFutures = new ArrayList<Future<CrawlerResult>>();
 
         for (Map.Entry<String, String> entry : this.sections.entrySet()) {
             try {
                 Future<CrawlerResult> f = executorService.submit(new ActasCrawler(PREPURL + entry.getValue()));
                 actasCrawlersFutures.add(f);
             } catch (MalformedURLException ex) {
                 Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
             }
         }
 
         for (Future<CrawlerResult> f : actasCrawlersFutures) {
             try {
                 File file = new File(STOPCRAWLERFILENAME);
 
                 if (file.exists()) {
                     file.delete();
                     f.cancel(true);
                     break;
                 }
 
                 CrawlerResult res = f.get();
                 List<String> imageUrls = res.getImageURLs();
 
                 for (String u : imageUrls) {
                     String url = ACTASURL + u + ACTASEXTENSION;
                     System.out.println(url);
                     wget(url);
                 }
 
                 addProcessedURL(res.getFromURL());
             } catch (InterruptedException ex) {
                 Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
             } catch (ExecutionException ex) {
                 if (!(ex.getCause() instanceof IOException)) {
                     Logger.getLogger(ObtenerActasPREP.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                 }
             }
         }
 
         executorService.shutdown();
         shutdown();
     }
 
     class CrawlerResult {
 
         private List<String> imageURLs;
         private String fromURL;
 
         public CrawlerResult(List<String> imageURLs, String fromURL) {
             this.imageURLs = imageURLs;
             this.fromURL = fromURL;
         }
 
         public List<String> getImageURLs() {
             return Collections.unmodifiableList(imageURLs);
         }
 
         public String getFromURL() {
             return fromURL;
         }
     }
 
     class ActasCrawler implements Callable<CrawlerResult> {
 
         private String url;
         private URLReader reader;
 
         public ActasCrawler(String url) throws MalformedURLException {
             this.url = url;
             this.reader = new URLReader(url, "UTF-8");
         }
 
         @Override
         public CrawlerResult call() throws Exception {
             StringBuilder html = reader.read();
             List<String> imageURLs = new ArrayList<String>();
 
             String[] urls = StringUtils.substringsBetween(html.toString(), ACTASURL, ACTASEXTENSION);
             Collections.addAll(imageURLs, urls);
 
             CrawlerResult ret = new CrawlerResult(imageURLs, this.url);
 
             return ret;
         }
     }
 
     public static void main(String[] args) {
         new ObtenerActasPREP().getAllActas();
         System.exit(0);
     }
 }
