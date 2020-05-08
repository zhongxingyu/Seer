 package net.anzix.fbfeed.output;
 
 import com.sun.syndication.feed.synd.*;
 import com.sun.syndication.io.SyndFeedOutput;
 import net.anzix.fbfeed.data.Feed;
 import net.anzix.fbfeed.data.Item;
 import net.anzix.fbfeed.data.Link;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Creates atom feed.
  */
 public class RssOutput {
 
     private File outputDir;
 
     private static final Logger LOG = LoggerFactory.getLogger(RssOutput.class);
 
     public RssOutput(File outputDir) {
         this.outputDir = outputDir;
     }
 
     public void output(Feed f) throws Exception {
         SyndFeed feed = new SyndFeedImpl();
         feed.setFeedType("atom_1.0");
         feed.setTitle(f.getName());
         feed.setLink(f.getLink());
         feed.setDescription("Generated from the facebook flow.");
 
         List entries = new ArrayList();
         SyndEntry entry;
         SyndContent description;
 
 
         feed.setEntries(entries);
 
         SyndFeedOutput output = new SyndFeedOutput();
 
         Date updated = null;
         for (Item i : f.getItems()) {
             entry = new SyndEntryImpl();
             entry.setTitle(i.getTitle());
             entry.setUpdatedDate(i.getDate());
             if (updated == null || (i.getDate() != null && updated.compareTo(i.getDate()) < 0)) {
                 updated = i.getDate();
             }
             entry.setLink(i.getHtmlLink());
             description = new SyndContentImpl();
             description.setType("text/html");
             description.setValue(i.getHtmlBody());
             entry.setDescription(description);
 
             entries.add(entry);
         }
         feed.setPublishedDate(updated);
 
        File outputFile = new File(outputDir, f.getId() + ".xml");
         FileWriter writer = new FileWriter(outputFile);
         LOG.debug("Writing html file to " + outputFile.getAbsolutePath());
         output.output(feed, writer);
         writer.close();
 
 
     }
 }
