 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.concurrent.Callable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 public class AccessionTask implements Runnable {
     
     private final File inputFile;
     private final File outputFile;
     private final Accessioner accessioner;
     private final Corrector corrector;
     
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public AccessionTask(File inputFile, File outputFile, Accessioner accessioner, Corrector corrector){
         this.inputFile = inputFile;
         this.outputFile = outputFile;
         this.accessioner = accessioner;
         this.corrector = corrector;
     }
     
     public void run() {
         SampleData st = null;
         try {
             st = accessioner.convert(this.inputFile);
         } catch (ParseException e) {
             log.error("ParseException converting " + this.inputFile, e);
             return;
         } catch (IOException e) {
             log.error("IOException converting " + this.inputFile, e);
             return;
         } catch (SQLException e) {
             log.error("SQLException converting " + this.inputFile, e);
             return;
         }
         
         //do corrections
         if (corrector != null){
             corrector.correct(st);
         }
         
         //TODO add derived from detector here
 
         FileWriter out = null;
         try {
             out = new FileWriter(this.outputFile);
         } catch (IOException e) {
             log.error("Error opening " + this.outputFile, e);
             return;
         }
 
         SampleTabWriter sampletabwriter = new SampleTabWriter(out);
         try {
             sampletabwriter.write(st);
         } catch (IOException e) {
             log.error("Error writing " + this.outputFile, e);
             return;
         }
     }
 
 }
