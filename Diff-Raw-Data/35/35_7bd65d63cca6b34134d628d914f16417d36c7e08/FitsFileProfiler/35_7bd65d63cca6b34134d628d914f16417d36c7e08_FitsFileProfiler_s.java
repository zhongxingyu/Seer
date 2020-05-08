 /**
  * 
  */
 package net.lovelycode.dp.contentprofiler.profilers;
 
 import static org.junit.Assert.fail;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 
 import net.lovelycode.dp.contentprofiler.RuntimeExecutor;
 import net.lovelycode.dp.contentprofiler.metadata.FileSummary;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 
 import edu.harvard.hul.ois.fits.Fits;
 import edu.harvard.hul.ois.fits.FitsOutput;
 import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
 import edu.harvard.hul.ois.fits.exceptions.FitsException;
 
 /**
  * @author AnJackson
  *
  */
 public class FitsFileProfiler {
     static private Logger log = Logger.getLogger(FitsFileProfiler.class);
     
    static String FITS_HOME = "/Users/anj/Projects/digitalpreservation/dp-toolkit/tools-profilers/fits/src/main/resources";
     static {
         try {
             FITS_HOME = new File(FITS_HOME).getCanonicalPath();
         } catch (IOException e) {
             e.printStackTrace();
         }
         log.debug("FITS_HOME: "+FITS_HOME);
     }
     
     public static FileSummary profile( File in ) {
         /*
         FileSummary fs;
         try {
             fs = new FileSummary(in);
         } catch (IOException e1) {
             e1.printStackTrace();
             return null;
         }
         */
         
         File out = null;
         try {
             out = File.createTempFile("contentprofiler", ".tmp.xml");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
 /*
  * skye boat song, 
         // This
 		FileSummary fo3 = null;
         try {
             FitsOutput fo = FitsFileProfiler.invokeFits(in, out);
             fo3 = new FileSummary(in);
 			fo3.setFormat( fo.getIdentities().get(0).getFormat() );
         } catch (FitsException e1) {
             e1.printStackTrace();
         } catch (IOException e1) {
             e1.printStackTrace();
         }
         
         return fo3;
 */
 
         FileSummary fo2 = null;
         try {
             fo2 = FitsFileProfiler.invokeFitsCli(in, out);
         } catch ( Exception e) {
             e.printStackTrace();
             return null;
         }
        
         return fo2;
 
       }
     
     private static FitsOutput invokeFits(File in, File out) throws FitsException, IOException {
         System.out.println(FITS_HOME);
         Fits fits = new Fits(FITS_HOME);
         System.out.println("xml: "+fits.FITS_XML);
         FitsOutput fitsOutput = fits.examine(in);
         if(fitsOutput.getCaughtExceptions().size() > 0) {
             for(Exception e: fitsOutput.getCaughtExceptions()) {
                 System.err.println("Warning: " + e.getMessage());
             }
         }
         System.out.println("Got: "+fitsOutput.getFileInfoElements());
         fitsOutput.saveToDisk("fitsout.xml");
         return fitsOutput;
     }
     
     
     private static final int TIMEOUT = 20;
     
     /**
      * Should not return NULL, as otherwise filed profile events will be missed.
      */
     private static FileSummary invokeFitsCli(File in, File out) throws IOException, InterruptedException, JDOMException {
         FileSummary fsum = new FileSummary(in);
 
         ProcessBuilder pb = new ProcessBuilder();
         File fits_home = new File(FITS_HOME);
         
         pb.directory(fits_home);
        pb.command(FITS_HOME+"/fits.sh","-i", in.getCanonicalPath(), "-o", out.getCanonicalPath());
 
         RuntimeExecutor r = new RuntimeExecutor(TIMEOUT*1000);
         try {
             String response = r.execute(pb);
         } catch (IOException e) {
            log.error("Process exception!");
             return fsum;
         } catch (TimeoutException e) {
             log.error("Process did not complete in "+TIMEOUT+"s");
             return fsum;
         }
         
         FileInputStream fresult = new FileInputStream(out);
         InputStreamReader isr = new InputStreamReader(fresult);
         BufferedReader br = new BufferedReader(isr);
         SAXBuilder saxBuilder = 
             new SAXBuilder("org.apache.xerces.parsers.SAXParser");
         Document dom = saxBuilder.build(br);
         
         // FIXME Write out to temp file? Or Log?
        //XMLOutputter xo = new XMLOutputter();
        //xo.output(dom.cloneContent(), System.out);
         
         // Do
         Long size = Long.parseLong(extractElement(dom,"size", null));
         if( fsum.getSize() != null && fsum.getSize() != size ) {
            log.error("Size of object from java.io.File does not match that determined by FITS!");
         }
         fsum.setSize(size);
         fsum.setMd5sum(extractElement(dom,"md5checksum", null));
         String version = extractElement(dom,"version", null);
         if( version != null && !"".equals(version) )
             log.warn("Got version: "+version);
         fsum.setFormat(extractElement(dom,"identity", "format"));
         fsum.setFormatUri(extractElement(dom,"externalIdentifier", null));
         String valid = extractElement(dom,"valid", null);
         if( valid != null && !"".equals(valid) ) 
             fsum.setValid( Boolean.parseBoolean( valid ) );
         
         String wellformed = extractElement(dom,"well-formed", null);
         if( wellformed != null && !"".equals(wellformed) ) 
             fsum.setWellFormed( Boolean.parseBoolean( wellformed ) );
         
         return fsum;
     }
     
     @SuppressWarnings("unchecked")
     private static String extractElement( Document dom, String field, String attr ) throws JDOMException {
         XPath xp = XPath.newInstance("//fits:"+field);
         xp.addNamespace("fits", "http://hul.harvard.edu/ois/xml/ns/fits/fits_output");
 /*        
         Element e = (Element)xp.selectSingleNode(dom);
         if(e != null) {
             return e.getTextTrim();
         } else {
             throw new JDOMException("Element "+field+" not found!");
         }
         */
        List<String> rl = new ArrayList<String>();
         for( Element e : (List<Element>) xp.selectNodes(dom) ) {
             if( attr == null ) {
                 rl.add( e.getTextTrim() );
             } else {
                 rl.add( e.getAttribute(attr).getValue().trim() );
             }
         }
         String result = "";
         Iterator<String> is = rl.iterator();
         while( is.hasNext() ) {
             result += is.next();
             if( is.hasNext() ) result += "|";
         }
         return result;
     }
 
     public void temp() {
         // TODO Adding -ant- as a dependency gets you these...
         org.apache.tools.bzip2.CBZip2InputStream in = null;
         org.apache.tools.tar.TarInputStream tin = null;
         org.apache.tika.parser.AutoDetectParser tika = null;
         // See:
         // http://svn.apache.org/viewvc/lucene/tika/trunk/tika-app/src/main/java/org/apache/tika/cli/TikaCLI.java?view=markup
         
     }
 }
