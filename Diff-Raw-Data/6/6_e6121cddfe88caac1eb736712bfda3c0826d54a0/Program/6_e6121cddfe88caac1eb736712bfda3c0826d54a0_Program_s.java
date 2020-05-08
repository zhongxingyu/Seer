 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import simpleformat.*;
 import simpleformat2clutograph.SimpleFormat2ClutoGraph;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author saricas
  */
 public class Program {
 
     public static void main(String[] args) 
             throws TransformerConfigurationException, TransformerException, IOException, FileNotFoundException, FileFormatNotSupportedException {      
         
         if (args.length != 3) {
             System.err.println("Usage:");
             System.err.println("  java -jar Xmi2Graphml.jar xmiFileName"
                     + " authorityThreshold(float) hubThreshold(float)");
             System.exit(1);
         }
         
         File xmiFile = new File("./" + args[0]);
         InputStream xslGraphml = 
                 Program.class.getResourceAsStream("/xmi2graphml/xmi2graphml.xsl");
         InputStream xslSimpleFormat = 
                 Program.class.getResourceAsStream("/xmi2simpleformat/xmi2simpleformat.xsl");
 
         Source xmiSource = new StreamSource(xmiFile);
         Source xsltGraphml = new StreamSource(xslGraphml);
         Source xsltSimpleFormat = new StreamSource(xslSimpleFormat);
         
         String fileName = args[0].substring(0, args[0].indexOf(".xmi"));
         File graphml = new File("./" + fileName + ".graphml");
         File simpleFormat = new File("./" + fileName + ".smpl");
 
         Result resultGraphml = new StreamResult(graphml);
         Result resultSimpleFormat = new StreamResult(simpleFormat);
         
         TransformerFactory transFact = TransformerFactory.newInstance();
         Transformer transGraphml = transFact.newTransformer(xsltGraphml);
         Transformer transSimpleFormat = transFact.newTransformer(xsltSimpleFormat);
 
         transGraphml.setOutputProperty(OutputKeys.INDENT, "yes");
         transGraphml.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
         
         transSimpleFormat.setOutputProperty(OutputKeys.INDENT, "yes");
         transSimpleFormat.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
         
         if (createFile(graphml)) {
             System.out.println("Creating graphml file...");
             transGraphml.transform(xmiSource, resultGraphml);
             System.out.println("Transformation completed. Output file: " + graphml.getCanonicalPath());
         }
         
         if (createFile(simpleFormat)) {
             System.out.println("Creating simpleformat file...");
             transSimpleFormat.transform(xmiSource, resultSimpleFormat);
             System.out.println("Transformation completed. Output file: " + simpleFormat.getCanonicalPath());
         }
         
         // create sparse graph matrix file for cluto clustering tool
         File graphFile = new File("./" + fileName + ".graph");
         if (createFile(graphFile)) {
             System.out.println("Creating graph file...");
             SimpleFormat2ClutoGraph sf2cg = new SimpleFormat2ClutoGraph(simpleFormat);
             sf2cg.write(graphFile);
             System.out.println("Transformation completed. Output file: " + graphFile.getCanonicalPath());
         }        
         
         // calculate authority, hub and cycle classes from simpleformat
         System.out.println("Calculating authority, hub and cycle classes.");
         Tokenizer tokenizer = new Tokenizer(simpleFormat);
         List<simpleformat.Class> klasses = tokenizer.tokenize();
         
         File resultsFile = new File("./" + fileName + ".txt");
         createFile(resultsFile);
         FileOutputStream ostream = new FileOutputStream(resultsFile);
         DataOutputStream out = new DataOutputStream(ostream);
         BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out));
         wr.write("Authority parameter: " + String.valueOf(Float.parseFloat(args[1])));
         wr.newLine();
         wr.write("Hub parameter: " + String.valueOf(Float.parseFloat(args[2])));
         wr.newLine();
         wr.newLine();
 
         AuthorityClassFinder authorityClassFinder =
                 new AuthorityClassFinder(klasses, tokenizer.getEdgeCount(), Float.parseFloat(args[1]));
         List<simpleformat.Class> authorityList = authorityClassFinder.find();
 
         HubClassFinder hubClassFinder =
                 new HubClassFinder(klasses, tokenizer.getEdgeCount(), Float.parseFloat(args[2]));
         List<simpleformat.Class> hubList = hubClassFinder.find();
         
         // filter out god classes
         List<simpleformat.Class> godList = new ArrayList<simpleformat.Class>();
        for (simpleformat.Class c : authorityList) {
             if (hubList.contains(c)) {
                 c.setType(simpleformat.Class.GOD);
                 authorityList.remove(c);
                 hubList.remove(c);
                 godList.add(c);
             }
         }
         
         printResults("God", godList, wr);
         printResults("Authority", authorityList, wr);        
         printResults("Hub", hubList, wr);
 
         CycleClassFinder cycleClassFinder = new CycleClassFinder(klasses);
         List<simpleformat.Class> cycleList = cycleClassFinder.find();
         printResults("Cycle", cycleList, wr);
 
         wr.close();
     }
     
     private static void printResults(String title, List<simpleformat.Class> list, BufferedWriter wr) 
             throws IOException {
         System.out.println(title + " count: " + String.valueOf(list.size()));
         wr.write(title + " count: " + String.valueOf(list.size()));
         wr.newLine();
         for (simpleformat.Class c : list) {
             System.out.println("- " + c.getName());
             wr.write("- " + c.getName());
             wr.newLine();
         }
         System.out.println();
         wr.newLine();
     }
     
     private static boolean createFile(File f) throws IOException {
         if (!f.getParentFile().exists())
             f.getParentFile().mkdirs();
         if (!f.exists())
             return f.createNewFile();
         return false;
     }
     
 }
