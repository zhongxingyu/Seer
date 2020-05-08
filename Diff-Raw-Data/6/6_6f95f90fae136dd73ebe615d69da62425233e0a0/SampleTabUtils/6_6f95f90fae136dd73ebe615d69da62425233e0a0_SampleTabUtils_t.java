 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 public class SampleTabUtils {
     private static Logger log = LoggerFactory.getLogger("uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils");
 
     //make sure this is kept in sync with uk.ac.ebi.fgpt.conan.process.biosd.AbstractBioSDProcess.getPathPrefix
     public static String getSubmissionDirPath(String submissionID) {
        return getSubmissionDirFile(submissionID).getPath();
     }
     
     public static File getSubmissionDirFile(String submissionID) {
         if (submissionID.startsWith("GMS-")) { 
             return new File("imsr", submissionID);
         } else if (submissionID.startsWith("GAE-")) {
             //split by pipeline
             String pipe = submissionID.split("-")[1];
             String ident = submissionID.split("-")[2];
             File targetfile = new File("ae", "GAE-"+pipe);
             int i = 7;
             int groupsize = 3;
             while (i < ident.length()){
                 targetfile = new File(targetfile, submissionID);
                 i += groupsize;   
             }
             //return targetfile.getPath();
             return new File("ae", submissionID);
         }
         else if (submissionID.startsWith("GPR-")) {
             return new File("pride", submissionID);
         } else if (submissionID.startsWith("GVA-")) { 
             return new File("dgva", submissionID);
         } else if (submissionID.startsWith("GCR-")) { 
             return new File("coriell", submissionID);
         } else if (submissionID.startsWith("GEN-")) { 
             return new File("sra", submissionID);
         } else if (submissionID.startsWith("GEM-")) {
             File targetfile = new File("GEM");
             int i = 7;
             int groupsize = 3;
             while (i < submissionID.length()){
                 targetfile = new File(targetfile, submissionID.substring(0,i));
                 i += groupsize;   
             }
             return new File(targetfile, submissionID);
         } else if (submissionID.startsWith("GSB-")) {
             return new File("GSB", submissionID);
         } else if (submissionID.equals("GEN")) { 
             return new File("encode", submissionID);
         } else if (submissionID.equals("G1K")) { 
             return new File("g1k", submissionID);
         } else if (submissionID.startsWith("GHM")) { 
             return new File("hapmap", submissionID);
         } else {
             throw new IllegalArgumentException("Unable to get path prefix for "+submissionID);
         }
     }
     
     public static boolean releaseInACentury(File sampletabFile) throws IOException, ParseException{
         SampleTabSaferParser parser = new SampleTabSaferParser();
         SampleData sd = parser.parse(sampletabFile);
         if (sd == null){
             log.error("Failed to parse "+sampletabFile);
             return false;
         } else if(sd.msi.submissionReleaseDate.before(new Date())) {
             //if its already public, then release it in 100 years
             Writer writer = null;
             try {
                 writer = new BufferedWriter(new FileWriter(sampletabFile));
                 SampleTabWriter stwriter = new SampleTabWriter(writer);
                 stwriter.write(sd);
             } catch (IOException e){
                 throw e;
             } finally {
                 if (writer != null){
                     try {
                         writer.close();
                     } catch (IOException e) {
                         //do nothing
                     }
                 }
             }
 
             return true;
         } else {
             return false;
         }
     }
     
     public static boolean releaseInACentury(SampleData sd) {
         if (sd == null){
             throw new IllegalArgumentException("Must provide non-null SampleData");
         }
         if(sd.msi.submissionReleaseDate.before(new Date())) {
             //release it in 100 years
             Calendar cal = GregorianCalendar.getInstance();
             cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)+100);
             sd.msi.submissionReleaseDate = cal.getTime();
             return true;
         }
         return false;
     }
     
     public static String generateSubmissionTitle(SampleData sd){
         /*
 NUMBER SPECIES samples (from DATABASE)
 
 NUMBER
     number of samples in group
 SPECIES
     if all the same species latin (common) species name
     if multiple species then "mixed species"
     if no species then omit
          */
         
         String title = "";
         
         boolean singular = true;
         
         if (sd.scd.getNodes(SampleNode.class).size() > 0){
            singular = false;
            Integer count = new Integer(sd.scd.getNodes(SampleNode.class).size());
            title += count.toString()+" ";
         }
         
         Set<String> species = new HashSet<String>();
         for (SampleNode sample : sd.scd.getNodes(SampleNode.class)){
             for(SCDNodeAttribute attr : sample.getAttributes()){
                 if (OrganismAttribute.class.isInstance(attr)){
                     OrganismAttribute org = (OrganismAttribute) attr;
                     species.add(org.getAttributeValue());
                 }
             }
         }
         if (species.size() == 0){
             //no known species, do nothing
         } else if (species.size() == 1){
             //add species name to title
             List<String> speciesArray = new ArrayList<String>(species);
             title += speciesArray.get(0)+" ";   
         } else if (species.size() > 1){
             if (singular){
                 title += "mixed species ";
             } else {
                 title += "Mixed species ";
             }
         }
         
         title += "samples ";
         
 
         Set<String> dbnames = new HashSet<String>();
         for(Database db : sd.msi.databases){
             dbnames.add(db.getName());
         }
         for (SampleNode sample : sd.scd.getNodes(SampleNode.class)){
             for(SCDNodeAttribute attr : sample.getAttributes()){
                 if (DatabaseAttribute.class.isInstance(attr)){
                     DatabaseAttribute db = (DatabaseAttribute) attr;
                     dbnames.add(db.getNodeName());
                 }
             }
         }        
         
         if (dbnames.size() > 0){
             title += "from ";
             for (String name : dbnames){
                 title += name+" and ";
             }
             //trim extra "and "
             title = title.substring(0, title.length()-4);
         } 
         
         return title.trim();
     }
     
 }
