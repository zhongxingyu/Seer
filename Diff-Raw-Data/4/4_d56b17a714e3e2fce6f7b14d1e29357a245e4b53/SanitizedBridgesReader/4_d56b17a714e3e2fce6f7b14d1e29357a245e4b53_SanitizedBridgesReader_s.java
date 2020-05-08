 import java.io.*;
 import java.text.*;
 import java.util.*;
 import org.apache.commons.codec.digest.*;
 
public class BridgeReader {
  public BridgeReader(ConsensusStatsFileHandler csfh,
       BridgeStatsFileHandler bsfh, String bridgesDir,
       SortedSet<String> countries) throws IOException, ParseException {
     System.out.print("Parsing all files in directory " + bridgesDir
         + "/...");
     SimpleDateFormat timeFormat = new SimpleDateFormat(
         "yyyy-MM-dd HH:mm:ss");
     timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     Stack<File> filesInInputDir = new Stack<File>();
     filesInInputDir.add(new File(bridgesDir));
     while (!filesInInputDir.isEmpty()) {
       File pop = filesInInputDir.pop();
       if (pop.isDirectory()) {
         for (File f : pop.listFiles()) {
           filesInInputDir.add(f);
         }
         continue;
       }
       BufferedReader br = new BufferedReader(new FileReader(pop));
 // TODO parsing of original bridge descriptors should go away!
       boolean needToHash = pop.getName().startsWith("cached-extrainfo");
       String hashedIdentity = null, publishedLine = null,
           geoipStartTimeLine = null;
       boolean skip = false;
       String line = null;
       while ((line = br.readLine()) != null) {
         if (line.startsWith("r ")) {
           // parse bridge status; TODO possibly move to own class
           int runningBridges = 0;
           while ((line = br.readLine()) != null) {
             if (line.startsWith("s ") && line.contains(" Running")) {
               runningBridges++;
             }
           }
           String fn = pop.getName();
           String date = fn.substring(0, 4) + "-" + fn.substring(4, 6)
               + "-" + fn.substring(6, 8) + " " + fn.substring(9, 11)
               + ":" + fn.substring(11, 13) + ":" + fn.substring(13, 15);
           csfh.addBridgeConsensusResults(date, runningBridges);
         } else if (line.startsWith("extra-info ")) {
           hashedIdentity = needToHash ? DigestUtils.shaHex(
               line.split(" ")[2]).toUpperCase() : line.split(" ")[2];
           skip = bsfh.isKnownRelay(hashedIdentity);
         } else if (!skip && line.startsWith("published ")) {
           publishedLine = line;
         } else if (!skip && line.startsWith("geoip-start-time ")) {
           geoipStartTimeLine = line;
         } else if (!skip && line.startsWith("geoip-client-origins")
             && line.split(" ").length > 1) {
           if (publishedLine == null ||
               geoipStartTimeLine == null) {
             System.out.println("Either published line or "
                 + "geoip-start-time line is not present in file "
                 + pop.getAbsolutePath() + ".");
             break;
           }
           long published = timeFormat.parse(publishedLine.
               substring("published ".length())).getTime();
           long started = timeFormat.parse(geoipStartTimeLine.
               substring("geoip-start-time ".length())).getTime();
           long seconds = (published - started) / 1000L;
           Map<String, Double> obs = new HashMap<String, Double>();
           String[] parts = line.split(" ")[1].split(",");
           for (String p : parts) {
             for (String c : countries) {
               if (p.startsWith(c)) {
                 obs.put(c, ((double) Long.parseLong(p.substring(3)) - 4L)
                     * 86400.0D / ((double) seconds));
               }
             }
           }
           String date = publishedLine.split(" ")[1];
           String time = publishedLine.split(" ")[2];
           bsfh.addStats(date, time, hashedIdentity, obs);
         }
       }
       br.close();
     }
     System.out.println("done");
   }
 }
