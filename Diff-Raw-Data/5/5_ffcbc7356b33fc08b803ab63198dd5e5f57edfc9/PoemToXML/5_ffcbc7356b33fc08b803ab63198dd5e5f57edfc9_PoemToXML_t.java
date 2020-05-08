 package uk.ac.kcl.informatics.opmbuild.tools;
 
 import java.io.File;
 import java.io.IOException;
 import uk.ac.kcl.informatics.opmbuild.Graph;
 import uk.ac.kcl.informatics.opmbuild.format.poem.PoemReadException;
 import uk.ac.kcl.informatics.opmbuild.format.poem.PoemReader;
import uk.ac.kcl.informatics.opmbuild.format.xml.v11.OPM11Serialiser;
 
 public class PoemToXML {
 
     public static void main (String[] arguments) throws IOException, PoemReadException {
         if (arguments.length < 2) {
             System.out.println ("Run with command-line options: <poem-file-path> <output-xml-file>");
             System.exit (0);
         }
 
         run (arguments[0], arguments[1]);
         System.exit (0);
     }
 
     public static void run (String poemFile, String outputFile) throws IOException, PoemReadException {
         PoemReader in = new PoemReader ();
        OPM11Serialiser writer = new OPM11Serialiser ();
         Graph graph;
         File out;
 
         graph = in.read (new File (poemFile));
         out = new File (outputFile);
         if (out.getParentFile () != null) {
             out.getParentFile ().mkdirs ();
         }
 
         writer.write (graph, out, true);
     }
 }
