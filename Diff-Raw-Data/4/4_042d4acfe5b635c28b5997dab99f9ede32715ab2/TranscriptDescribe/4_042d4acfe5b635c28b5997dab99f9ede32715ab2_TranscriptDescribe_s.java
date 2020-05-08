 package net.qldarch.ingest.transcript;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.*;
 import org.apache.commons.io.input.AutoCloseInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.qldarch.ingest.Configuration;
 import net.qldarch.ingest.IngestStage;
 import net.qldarch.service.rdf.MetadataRepositoryException;
 import net.qldarch.service.rdf.RdfDataStoreDao;
 import net.qldarch.service.rdf.RdfDescription;
 import net.qldarch.service.rdf.SesameDataStoreDao;
 import net.qldarch.service.rdf.User;
 
 import static net.qldarch.service.rdf.KnownURIs.*;
 
 public class TranscriptDescribe implements IngestStage {
     public static Logger logger = LoggerFactory.getLogger(TranscriptDescribe.class);
 
     private Configuration configuration;
 
     public TranscriptDescribe(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public void ingest() {
         String urlPrefix = configuration.getJsonURLPrefix().toString();
         String endpoint = configuration.getEndpoint();
         String repository = configuration.getRepository();
         File outputDir = configuration.getOutputDir();
 
         Collection<File> summaryFiles =
             FileUtils.listFiles(outputDir, new String[] {"summary"}, true);
 
         logger.debug("Loading Transcript Descriptions: {}", summaryFiles);
 
         for (File summaryFile : summaryFiles) {
             if (!summaryFile.exists()) {
                 System.out.println("Summary file " + summaryFile + " went missing");
                 continue;
             }
 
             try {
                 RdfDataStoreDao rdfDao = new SesameDataStoreDao(endpoint, repository);
                 performTranscriptDescribe(urlPrefix, rdfDao, summaryFile);
             } catch (IOException ei) {
                 System.out.printf("Error while performing description load for %s : %s",
                         summaryFile, ei.getMessage());
                 logger.warn("Error while performing description load for {}", summaryFile, ei);
             } catch (MetadataRepositoryException em) {
                 System.out.printf("Error while performing description load for %s : %s",
                         summaryFile, em.getMessage());
                 logger.warn("Error while performing description load for {}", summaryFile, em);
             }
         }
     }
 
     private void performTranscriptDescribe(String urlPrefix, RdfDataStoreDao dao, File summaryFile)
             throws IOException, MetadataRepositoryException {
         Properties summary = new Properties();
         summary.load(new AutoCloseInputStream(FileUtils.openInputStream(summaryFile)));
 
         if (!summary.containsKey("deploy.file")) {
             System.out.println("No deploy.file entry in summary file: " + summaryFile);
             return;
         }
         if (!summary.containsKey("json.path")) {
             System.out.println("No json.path entry in summary file: " + summaryFile);
             return;
         }
 
         String jsonPath = summary.getProperty("json.path");
         URI jsonURI;
         try {
             jsonURI = new URL(urlPrefix + jsonPath).toURI();
         } catch (MalformedURLException em) {
             System.out.printf("urlPrefix: %s + jsonPath %s invalid URL for %s (%s)\n",
                     urlPrefix, jsonPath, summary.getProperty("deploy.file"), em.getMessage());
             return;
         } catch (URISyntaxException eu) {
             System.out.printf("urlPrefix: %s + jsonPath %s invalid URI for %s (%s)\n",
                     urlPrefix, jsonPath, summary.getProperty("deploy.file"), eu.getMessage());
             return;
         }
 
         if (!summary.containsKey("interview")) {
             System.out.println("Error No interview entry in summary file: " + summaryFile);
             return;
         }
         String interview = summary.getProperty("interview");
         URI interviewURI;
         try {
             interviewURI = new URI(interview);
         } catch (URISyntaxException eu) {
             System.out.printf("interview is invalid URI: %s for %s (%s)\n",
                     interview, summary.getProperty("deploy.file"), eu.getMessage());
             return;
         }
 
         RdfDescription rdf = new RdfDescription();
         rdf.setUri(interview);
         rdf.addProperty("qldarch:transcriptLocation", jsonURI);
        rdf.addProperty("rdf:type", "qldarch:Interview");
 
         User td = new User("transcript.describe");
 
         try {
             replaceTranscriptLocation(dao, td, rdf, interviewURI);
         } catch (MetadataRepositoryException em) {
             System.out.println("Failed to describe interview: " + interview +
                     ": " +  em.getMessage());
             // FIXME: Setup logging and replace this with a dump to the logfile.
             em.printStackTrace();
             return;
         }
     }
 
     private void replaceTranscriptLocation(RdfDataStoreDao dao, User td, RdfDescription rdf,
             URI interviewURI) throws MetadataRepositoryException {
         dao.performInsert(rdf, td, QAC_HAS_EXPRESSION_GRAPH, td.getExpressionGraph());
     }
 }
 
