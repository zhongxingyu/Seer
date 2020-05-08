 package dk.statsbiblioteket.doms.transformers.common;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Run through list of UUIDs, call object handler, and report status in logging and files.
  */
 public class FileRecordingObjectListHandler implements ObjectListHandler {
     private static final Logger log = LoggerFactory.getLogger("FileRecordingObjectListHandler");
     private static BufferedWriter successFileWriter;
     private static BufferedWriter failureFileWriter;
     private ObjectHandler objectHandler;
 
     /**
      * Initialise handler. Will open files for reporting successes and failures.
      *
      * @param config Configuration. Will read file names from here.
      * @param objectHandler The object handler that should handle each uuid.
      * @throws IOException On trouble opening the files.
      */
     public FileRecordingObjectListHandler(Config config, ObjectHandler objectHandler) throws IOException {
         this.objectHandler = objectHandler;
         successFileWriter = new BufferedWriter(new FileWriter(new File(config.getSuccessFile())));
         failureFileWriter = new BufferedWriter(new FileWriter(new File(config.getFailureFile())));
     }
 
     @Override
     public void transform(List<String> uuids) {
         for (String uuid : uuids) {
             try {
                 System.out.println("trying to transform... " + uuid);
                 objectHandler.transform(uuid);
                 System.out.println("transformed... " + uuid);
                 recordSuccess(uuid);
             } catch (Exception e) {
                 System.out.println(e.getMessage());
                 log.error("Error processing uuid '{}'", uuid, e);
                 recordFailure(uuid, e);
             }
         }
     }
 
     public static void recordSuccess(String uuid) {
         try {
             successFileWriter.write(uuid);
             successFileWriter.newLine();
             successFileWriter.flush();
         } catch (IOException e) {
             log.error("Unable to record success of '{}'", uuid, e);
         }
         log.info("Successfully processed '{}'", uuid);
     }
 
     public static void recordFailure(String uuid, Throwable t) {
         try {
             failureFileWriter.write(uuid);
             failureFileWriter.newLine();
             failureFileWriter.flush();
         } catch (IOException e) {
             log.error("Unable to record failure of '{}'", uuid, e);
         }
         log.warn("Failure processing '{}'", uuid, t);
     }
 
     public static void recordIgnored(String uuid) {
         try {
             failureFileWriter.write(uuid);
             failureFileWriter.newLine();
             failureFileWriter.flush();
         } catch (IOException e) {
            log.error("Purposely didn't enrich '{}'", uuid, e);
         }
        log.info("Purposely didn't enrich '{}'", uuid);
     }
 }
