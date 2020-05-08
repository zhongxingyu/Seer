 package net.caprazzi.giddone;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import net.caprazzi.giddone.cloning.CloneService;
 import net.caprazzi.giddone.deploy.DeployService;
 import net.caprazzi.giddone.hook.HookQueueClient;
 import net.caprazzi.giddone.parsing.*;
 import net.caprazzi.giddone.worker.HookQueueExecutor;
 import net.caprazzi.giddone.worker.RepositoryParser;
 import net.caprazzi.giddone.worker.SourceCodeParser;
 import net.caprazzi.giddone.worker.SourceFileScanner;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 public class GiddoneWorkerService {
 
     private static final Logger Log = LoggerFactory.getLogger(GiddoneWorkerService.class);
 
     public static void main(String[] args) throws IOException {
         HttpClient httpClient = new DefaultHttpClient();
         // TODO: read from config
         HookQueueClient queueClient = new HookQueueClient(httpClient, "http://gitdone-receive-hook.herokuapp.com");
 
         // TODO: read from config
         Path tempDir = FileSystems.getDefault().getPath("./workers");
         try {
             Files.createDirectory(tempDir);
             Log.debug("Created workers dir: {}", tempDir.toAbsolutePath());
         }
         catch(FileAlreadyExistsException ex) {
             Log.debug("Using existing workers dir: {}", tempDir.toAbsolutePath());
         }
 
         CloneService cloneService = new CloneService(tempDir);
 
         // TODO: load languages from config
         Languages languages = new Languages(new Language("Java", "java", CommentStrategy.DoubleSlash));
         SourceFileScanner sourceFileScanner = new SourceFileScanner(languages);
         CommentParser commentParser = new CommentParser();
         TodoParser todoParser = new TodoParser();
 
         SourceCodeParser parser = new SourceCodeParser(sourceFileScanner, commentParser, todoParser);
         RepositoryParser repositoryParser = new RepositoryParser(cloneService, parser, tempDir);
 
         // TODO: from config
         String myAccessKeyID = "1P1SGYDKGB3TJP35Z602";
         String mySecretKey =  "jJs0pddIZrqJjXnOyv479MatKr38HlmsEMb4C6LF";
         AWSCredentials myCredentials = new BasicAWSCredentials(myAccessKeyID, mySecretKey);
         AmazonS3 s3client = new AmazonS3Client(myCredentials);
         DeployService database = new DeployService(s3client);
 
        HookQueueExecutor queueExecutor = new HookQueueExecutor(queueClient, 5000, repositoryParser, database);
         queueExecutor.start();
     }
 }
