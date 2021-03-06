 package org.freeeed.main;
 
 import com.google.common.io.Files;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import org.apache.hadoop.io.MD5Hash;
 import org.apache.hadoop.io.MapWritable;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.freeeed.services.Project;
 import org.freeeed.services.Stats;
 
 /**
  *
  * @author Mark Kerzner
  */
 public class WindowsReduce extends Reduce {    
     private String metadataOutputFileName = null;
     private static WindowsReduce instance = null;
 
     private WindowsReduce() {
     }
 
     public static synchronized WindowsReduce getInstance() {
         if (instance == null) {
             instance = new WindowsReduce();
             try {
                 instance.setup(null);
             } catch (Exception e) {
                 e.printStackTrace(System.out);
             }
         }
         return instance;
     }
 
     @Override
     protected void setup(Reducer.Context context)
             throws IOException, InterruptedException {
         Project project = Project.getProject();
        metadataOutputFileName = project.getOutputDir()
             + "/metadata" + ParameterProcessing.METADATA_FILE_EXT;
 
         if (project.isEnvHadoop()) {
             String metadataFileContents = context.getConfiguration().get(ParameterProcessing.METADATA_FILE);
             Files.write(metadataFileContents.getBytes(), new File(ColumnMetadata.metadataNamesFile));
         }
         columnMetadata = new ColumnMetadata();
         columnMetadata.setFieldSeparator(project.getProperty(ParameterProcessing.FIELD_SEPARATOR));
         columnMetadata.setAllMetadata(project.getProperty(ParameterProcessing.METADATA_OPTION));
         // write standard metadata fields
         new File(project.getOutputDir()).mkdirs();
         Files.append(columnMetadata.delimiterSeparatedHeaders(),
                 new File(metadataOutputFileName), Charset.defaultCharset());
         zipFileWriter.setup();
         zipFileWriter.openZipForWriting();
     }
 
     @Override
     protected void cleanup(Reducer.Context context)
             throws IOException, InterruptedException {
         // write summary headers with all metadata
         Files.append("\n" + columnMetadata.delimiterSeparatedHeaders(),
                 new File(metadataOutputFileName), Charset.defaultCharset());
         zipFileWriter.closeZip();
         Stats.getInstance().setJobFinished();
         String outputSuccess = Project.getProject().getResultsDir() + "/_SUCCESS";
         Files.write("", new File(outputSuccess), Charset.defaultCharset());
     }
 
     @Override
     public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
             throws IOException, InterruptedException {        
         for (MapWritable value : values) {
             columnMetadata.reinit();
             ++outputFileCount;
             processMap(value);            
             Files.append("\n" + columnMetadata.delimiterSeparatedValues(),
                     new File(metadataOutputFileName), Charset.defaultCharset());
         }
     }
 }
