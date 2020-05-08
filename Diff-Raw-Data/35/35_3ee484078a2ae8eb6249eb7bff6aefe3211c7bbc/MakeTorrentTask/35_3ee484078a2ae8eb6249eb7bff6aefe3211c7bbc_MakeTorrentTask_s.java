 package uk.ac.bristol.dundry.tasks;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.turn.ttorrent.common.Torrent;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionException;
 import uk.ac.bristol.dundry.dao.Repository;
 
 /**
  *
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 public class MakeTorrentTask extends JobBase {
     
     @Override
     public void execute(Repository repo, Resource item, Resource prov, String id,
         Path root, JobDataMap jobData) throws JobExecutionException {
         
         Path publishedPath = repo.getPublishPathForId(id);
         String tracker = jobData.getString("torrent.tracker");
         try {
             Torrent torrent = Torrent.create(
                     publishedPath.toFile(), listContent(publishedPath), 
                     new URI(tracker), "dundry");
            File torrentFile = root.resolve(id + ".torrent").toFile();
             torrent.save(torrentFile);
         } catch (NoSuchAlgorithmException | InterruptedException | IOException | URISyntaxException ex) {
             throw new JobExecutionException("Creating torrent failed", ex);
         }
     }
     
     private List<File> listContent(final Path parent) throws IOException {
         
         final List<File> files = new ArrayList<>();
         
         Files.walkFileTree(parent, new SimpleFileVisitor<Path>() {
             @Override
             public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                 files.add(file.toFile());
                 return FileVisitResult.CONTINUE;
             }
         });
         return files;
     }
 }
