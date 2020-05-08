 package net.anzix.fbfeed;
 
 import ch.qos.logback.classic.Level;
 import com.google.common.io.Files;
 import com.google.common.io.LineProcessor;
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import net.anzix.fbfeed.data.*;
 import net.anzix.fbfeed.output.HtmlOutput;
 import net.anzix.fbfeed.output.RssOutput;
 import net.anzix.fbfeed.output.SysOutput;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class Start {
 
     private static final Logger LOG = LoggerFactory.getLogger(Start.class);
 
     private File cacheLocation = new File(System.getProperty("java.io.tmpdir"), "fbcache");
 
     @Option(name = "--id", required = true, usage = "Id of the facebook object, or a file with one id per line.")
     private String id;
 
     @Option(name = "--key", required = true, usage = "Access token")
     private String access_key;
 
     @Option(name = "-v", usage = "Use if you need debug level logging.")
     private boolean verbose;
 
     @Option(name = "--type", usage = "Output type (rss,html,sysout). Multiple format can be used with separating with ,")
     private String type = "rss";
 
     @Option(name = "--output", usage = "Destination directory")
     private File outputDir = new File(".");
 
     private boolean forceDownload = false;
 
     public static void main(String args[]) throws Exception {
         Start prog = new Start();
         CmdLineParser parser = new CmdLineParser(prog);
         try {
             parser.parseArgument(args);
             prog.run();
         } catch (CmdLineException ex) {
             System.err.println("Error in the arguments: " + ex.getMessage());
             parser.printUsage(System.err);
         } catch (Exception ex) {
             LOG.error("Error during the generation", ex);
         }
 
     }
 
     public void run() throws Exception {
         if (verbose) {
             ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
         } else {
             ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
         }
         for (File feedFile : retrieveFeeds()) {
             Feed feed = parse(feedFile);
             for (String t : type.split(",")) {
                 String oneType = t.trim();
                 LOG.info("Generating " + oneType + " output");
                 if (oneType.equals("rss")) {
                     new RssOutput(outputDir).output(feed);
                 } else if (oneType.equals("sysout")) {
                     new SysOutput().output(feed);
                 } else if (oneType.equals("html")) {
                     new HtmlOutput(outputDir).output(feed);
                 } else {
                     LOG.error("Unknown output type: " + type);
                     System.exit(-1);
                 }
             }
         }
 
 
     }
 
     private File[] retrieveFeeds() throws Exception {
         LOG.info("Updating feeds...");
         if (id.trim().matches("[0-9]+")) {
             return new File[]{retrieveFeed(id)};
         } else {
             File idFile = new File(id);
             if (idFile.exists()) {
                 final List<File> results = new ArrayList<File>();
                 Files.readLines(idFile, Charset.defaultCharset(), new LineProcessor<Object>() {
                     @Override
                     public boolean processLine(String line) throws IOException {
                         if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
                             try {
                                 results.add(retrieveFeed(line.trim()));
                             } catch (Exception ex) {
                                 LOG.error("Can't process line " + line);
                             }
                         }
                         return true;
                     }
 
                     @Override
                     public Object getResult() {
                         return null;
                     }
                 });
                 return results.toArray(new File[results.size()]);
             } else {
                 LOG.error("ID file doesn't exist: " + id);
                 return new File[0];
             }
         }
     }
 
     private File retrieveFeed(String fbId) throws Exception {
         if (!cacheLocation.exists()) {
             cacheLocation.mkdirs();
         }
         File cacheFile = new File(cacheLocation, fbId + ".json");
         boolean refresh = false;
         if (cacheFile.exists()) {
             long updated = (new Date().getTime() - cacheFile.lastModified()) / (1000l * 60);
             if (updated > 60) {
                 LOG.debug("Cached file has been  modified " + updated + " minutes ago. Should be updated.");
                 refresh = true;
             } else {
                 LOG.debug("Using cache file " + cacheFile.getAbsolutePath() + "(" + updated + " minutes old)");
             }
 
         }
         if (!cacheFile.exists() || refresh || forceDownload) {
             URL website = new URL("https://graph.facebook.com/" + fbId + "?fields=name,id,posts&access_token=" + access_key);
             LOG.debug("(Re)downloading " + website);
             ReadableByteChannel rbc = Channels.newChannel(website.openStream());
             FileOutputStream fos = new FileOutputStream(cacheFile);
             fos.getChannel().transferFrom(rbc, 0, 1 << 24);
         }
         return cacheFile;
 
     }
 
     public Feed parse(File f) throws Exception {
         Feed feed = new Feed();
         Gson gson = new Gson();
         JsonObject e = gson.fromJson(new FileReader(f), JsonObject.class);
         String id = e.get("id").getAsString();
         feed.setLink("http://facebook.com/" + id);
         feed.setName(e.get("name").getAsString());
         feed.setId(id);
 
         for (JsonElement o : e.get("posts").getAsJsonObject().get("data").getAsJsonArray()) {
             JsonObject obj = (JsonObject) o;
             String type = "";
             if (obj.get("type") != null) {
                 type = obj.get("type").getAsString();
             }
             if (type.equals("photo")) {
                 Photo p = new Photo();
                 p.readFrom((JsonObject) obj);
                 feed.addItem(p);
             } else if (type.equals("link")) {
                 Link l = new Link();
                 l.readFrom((JsonObject) obj);
                 feed.addItem(l);
             } else if (type.equals("video")) {
                 Video v = new Video();
                 v.readFrom((JsonObject) obj);
                 feed.addItem(v);
             } else if (type.equals("status")) {
                 if (obj.get("message") != null) {
                     Status s = new Status();
                     s.readFrom((JsonObject) obj);
                     feed.addItem(s);
                 }
             } else {
                 LOG.warn("Unhandled object: " + obj);
             }
 
         }
 
 
         return feed;
     }
 
 }
