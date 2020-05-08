 /*
  * ParseFile.java
  *
  * Created on February 24, 2006, 9:11 AM
  *
  * @author Douglas Slotta
  */
 
 package gov.nih.nimh.mass_sieve.io;
 
 import gov.nih.nimh.mass_sieve.PeptideHit;
 import java.awt.Component;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.ProgressMonitorInputStream;
 import org.biojavax.bio.seq.RichSequence;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 public class ParseFile {
     
     private AnalysisProgramType filetype;
     private String filename;
     private File file;
     private XMLReader xmlReader;
     private AnalysisHandler handler;
     private Component parent;
     
     //public ParseFile(String f, Component p) {
     public ParseFile(File f, Component p) {
         filename = f.getPath();
         file = f;
         parent = p;
         
         if (filename.endsWith(".dat")) {
             System.err.println("Parsing " + filename + " as a Mascot dat file");
             mascotDatHandler mdh = new mascotDatHandler(filename, parent);
             mdh.mascotDatParse();
             handler = mdh;
         } else {  // Maybe it is an XML file?
             
             CheckXMLFiletype();
             
             switch (filetype) {
                 case MASCOT:
                     System.err.println("Parsing " + filename + " as a Mascot XML file");
                     handler = new mascotXMLHandler(filename);
                     XMLParse();
                     break;
                 case OMSSA:
                     System.err.println("Parsing " + filename + " as an OMSSA file");
                     handler = new omssaHandler(filename);
                     XMLParse();
                     ((omssaHandler)handler).scaleMasses();
                     break;
                 case XTANDEM:
                     System.err.println("Parsing " + filename + " as a X!Tandem file");
                     handler = new xtandemHandler(filename);
                     XMLParse();
                     break;
                 case UNKNOWN:
                     System.err.println("Unable to determine filetype for: " + filename);
                     break;
             }
         }
     }
     
     public ArrayList<PeptideHit> getPeptideHits() {
         return handler.getPeptideHits();
     }
     
     public HashMap<String, RichSequence> getProteinDB() {
         return handler.getProteinDB();
     }
     
     public FileInformation getFileInformation() {
         return handler.getFileInformation();
     }
     
     protected int ScanFilenameToScanNumber(String fn) {
         StringBuffer sb = new StringBuffer(fn);
         int val, start, stop;
         start = sb.indexOf(".")+1;
         stop = sb.substring(start).indexOf(".")+start;
         val = Integer.parseInt(sb.substring(start,stop));
         return val;
     }
     
     
     
     void XMLParse() {
         try {
             // Parse the input
             xmlReader = XMLReaderFactory.createXMLReader();
             xmlReader.setContentHandler(handler);
             ProgressMonitorInputStream pin = new ProgressMonitorInputStream(parent, "Loading " + filename, new FileInputStream(file));
             pin.getProgressMonitor().setMillisToDecideToPopup(0);
             pin.getProgressMonitor().setMillisToPopup(0);
             BufferedInputStream bin = new BufferedInputStream(pin);
             xmlReader.parse(new InputSource(bin));
             //xmlReader.parse(filename);
         } catch (SAXException t) {
             filetype = AnalysisProgramType.UNKNOWN;
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
     
     void CheckXMLFiletype(){
         CheckTypeHandler check_handler = new CheckTypeHandler();
         
         // Use the default (non-validating) parser
         try {
             // Parse the input
             xmlReader = XMLReaderFactory.createXMLReader();
             xmlReader.setContentHandler(check_handler);
             xmlReader.parse(new InputSource(new FileInputStream(file)));
         } catch (TypeFoundException t) {
             filetype = t.getFileType();
             //System.err.println(filename + " must be a " + filetype.toString() + " file");
         } catch (SAXException t) {
             filetype = AnalysisProgramType.UNKNOWN;
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
     
 }
 
 class CheckTypeHandler extends DefaultHandler {
     public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws TypeFoundException {
        if (sName == "MSSearch") {
             throw new TypeFoundException(AnalysisProgramType.OMSSA);
         }
         if (sName == "bioml") {
             throw new TypeFoundException(AnalysisProgramType.XTANDEM);
         }
         if (sName == "mascot_search_results") {
             throw new TypeFoundException(AnalysisProgramType.MASCOT);
         }
     }
     public void endDocument() throws TypeFoundException {
         throw new TypeFoundException(AnalysisProgramType.UNKNOWN);
     }
 }
 
 class TypeFoundException extends SAXException {
     public AnalysisProgramType type;
     TypeFoundException(AnalysisProgramType ftype) {
         type = ftype;
     }
     public AnalysisProgramType getFileType() {
         return type;
     }
 }
