 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 import org.apache.commons.codec.binary.*;
 
 
 public class ArchiveWriter {
   private Logger logger;
   private String outputDirectory;
   private int storedConsensuses = 0, storedVotes = 0,
       storedServerDescriptors = 0, storedExtraInfoDescriptors = 0;
 
   public ArchiveWriter(String outputDirectory) {
     this.logger = Logger.getLogger(ArchiveWriter.class.getName());
     this.outputDirectory = outputDirectory;
   }
 
   private boolean store(byte[] data, String filename) {
     try {
       File file = new File(filename);
       if (!file.exists()) {
         this.logger.finer("Storing " + filename);
         file.getParentFile().mkdirs();
         BufferedOutputStream bos = new BufferedOutputStream(
             new FileOutputStream(file));
         bos.write(data, 0, data.length);
         bos.close();
         return true;
       }
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not store relay descriptor "
           + filename, e);
     }
     return false;
   }
 
   public void storeConsensus(byte[] data, long validAfter) {
     SimpleDateFormat printFormat = new SimpleDateFormat(
         "yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
     printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String filename = outputDirectory + "/consensus/"
         + printFormat.format(new Date(validAfter)) + "-consensus";
     if (this.store(data, filename)) {
       this.storedConsensuses++;
     }
   }
 
   public void storeVote(byte[] data, long validAfter,
       String fingerprint, String digest) {
     SimpleDateFormat printFormat = new SimpleDateFormat(
         "yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
     printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String filename = outputDirectory + "/vote/"
         + printFormat.format(new Date(validAfter)) + "-vote-"
         + fingerprint + "-" + digest;
     if (this.store(data, filename)) {
       this.storedVotes++;
     }
   }
 
   public void storeServerDescriptor(byte[] data, String digest,
       long published) {
     SimpleDateFormat printFormat = new SimpleDateFormat("yyyy/MM/");
     printFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String filename = outputDirectory + "/server-descriptor/"
         + printFormat.format(new Date(published))
         + digest.substring(0, 1) + "/" + digest.substring(1, 2) + "/"
         + digest;
     if (this.store(data, filename)) {
       this.storedServerDescriptors++;
     }
   }
 
   public void storeExtraInfoDescriptor(byte[] data,
       String extraInfoDigest, long published) {
     SimpleDateFormat descriptorFormat = new SimpleDateFormat("yyyy/MM/");
     descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String filename = outputDirectory + "/extra-info/"
         + descriptorFormat.format(new Date(published))
         + extraInfoDigest.substring(0, 1) + "/"
         + extraInfoDigest.substring(1, 2) + "/"
         + extraInfoDigest;
     if (this.store(data, filename)) {
       this.storedExtraInfoDescriptors++;
     }
   }
 
   private StringBuilder intermediateStats = new StringBuilder();
   public void intermediateStats(String event) {
     intermediateStats.append("While " + event + ", we stored "
         + this.storedConsensuses + " consensus(es), " + this.storedVotes
         + " vote(s), " + this.storedServerDescriptors
         + " server descriptor(s), and " + this.storedExtraInfoDescriptors
        + " extra-info descriptor(s) to disk.\n");
     this.storedConsensuses = 0;
     this.storedVotes = 0;
     this.storedServerDescriptors = 0;
     this.storedExtraInfoDescriptors = 0;
   }
   /**
    * Dump some statistics on the completeness of descriptors to the logs
    * on level INFO.
    */
   public void dumpStats() {
    StringBuilder sb = new StringBuilder("Finished writing relay "
        + "descriptors to disk.\n");
    sb.append(intermediateStats.toString());
     sb.append("Statistics on the completeness of written relay "
         + "descriptors of the past 12 consensuses (Consensus/Vote, "
         + "valid-after, votes, server descriptors, extra-infos):");
     try {
       SimpleDateFormat validAfterFormat =
           new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       validAfterFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
       SimpleDateFormat consensusVoteFormat =
           new SimpleDateFormat("yyyy/MM/dd/yyyy-MM-dd-HH-mm-ss");
       consensusVoteFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
       SimpleDateFormat descriptorFormat =
           new SimpleDateFormat("yyyy/MM/");
       descriptorFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 
       SortedSet<File> consensuses = new TreeSet<File>();
       Stack<File> leftToParse = new Stack<File>();
       leftToParse.add(new File(outputDirectory + "/consensus"));
       while (!leftToParse.isEmpty()) {
         File pop = leftToParse.pop();
         if (pop.isDirectory()) {
           for (File f : pop.listFiles()) {
             leftToParse.add(f);
           }
         } else if (pop.length() > 0) {
           consensuses.add(pop);
         }
         while (consensuses.size() > 12) {
           consensuses.remove(consensuses.first());
         }
       }
       for (File f : consensuses) {
         BufferedReader br = new BufferedReader(new FileReader(f));
         String line = null, validAfterTime = null,
             voteFilenamePrefix = null, dirSource = null;
         int allVotes = 0, foundVotes = 0,
             allServerDescs = 0, foundServerDescs = 0,
             allExtraInfos = 0, foundExtraInfos = 0;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("valid-after ")) {
             validAfterTime = line.substring("valid-after ".length());
             long validAfter = validAfterFormat.parse(
                 validAfterTime).getTime();
             voteFilenamePrefix = outputDirectory + "/vote/"
                 + consensusVoteFormat.format(new Date(validAfter))
                 + "-vote-";
           } else if (line.startsWith("dir-source ")) {
             dirSource = line.split(" ")[2];
           } else if (line.startsWith("vote-digest ")) {
             allVotes++;
             File voteFile = new File(voteFilenamePrefix + dirSource + "-"
                 + line.split(" ")[1]);
             if (voteFile.exists()) {
               foundVotes++;
               BufferedReader vbr = new BufferedReader(new FileReader(
                   voteFile));
               String line3 = null;
               int voteAllServerDescs = 0, voteFoundServerDescs = 0,
                   voteAllExtraInfos = 0, voteFoundExtraInfos = 0;
               while ((line3 = vbr.readLine()) != null) {
                 if (line3.startsWith("r ")) {
                   voteAllServerDescs++;
                   String digest = Hex.encodeHexString(Base64.decodeBase64(
                       line3.split(" ")[3] + "=")).toLowerCase();
                   long published = validAfterFormat.parse(
                       line3.split(" ")[4] + " "
                       + line3.split(" ")[5]).getTime();
                   String filename = outputDirectory
                       + "/server-descriptor/"
                       + descriptorFormat.format(new Date(published))
                       + digest.substring(0, 1) + "/"
                       + digest.substring(1, 2) + "/" + digest;
                   if (new File(filename).exists()) {
                     BufferedReader sbr = new BufferedReader(new FileReader(
                         new File(filename)));
                     String line2 = null;
                     while ((line2 = sbr.readLine()) != null) {
                       if (line2.startsWith("opt extra-info-digest ") ||
                           line2.startsWith("extra-info-digest ")) {
                         voteAllExtraInfos++;
                         String extraInfoDigest = line2.startsWith("opt ") ?
                             line2.split(" ")[2].toLowerCase() :
                             line2.split(" ")[1].toLowerCase();
                         String filename2 = outputDirectory
                             + "/extra-info/"
                             + descriptorFormat.format(new Date(published))
                             + extraInfoDigest.substring(0, 1) + "/"
                             + extraInfoDigest.substring(1, 2) + "/"
                             + extraInfoDigest;
                         if (new File(filename2).exists()) {
                           voteFoundExtraInfos++;
                         }
                       }
                     }
                     sbr.close();
                     voteFoundServerDescs++;
                   }
                 }
               }
               vbr.close();
               sb.append(String.format("%nV, %s, NA, %d/%d (%.1f%%), "
                   + "%d/%d (%.1f%%)", validAfterTime,
                   voteFoundServerDescs, voteAllServerDescs,
                   100.0D * (double) voteFoundServerDescs /
                     (double) voteAllServerDescs,
                   voteFoundExtraInfos, voteAllExtraInfos,
                   100.0D * (double) voteFoundExtraInfos /
                     (double) voteAllExtraInfos));
             }
           } else if (line.startsWith("r ")) {
             allServerDescs++;
             String digest = Hex.encodeHexString(Base64.decodeBase64(
                 line.split(" ")[3] + "=")).toLowerCase();
             long published = validAfterFormat.parse(
                 line.split(" ")[4] + " " + line.split(" ")[5]).getTime();
             String filename = outputDirectory + "/server-descriptor/"
                 + descriptorFormat.format(new Date(published))
                 + digest.substring(0, 1) + "/"
                 + digest.substring(1, 2) + "/" + digest;
             if (new File (filename).exists()) {
               BufferedReader sbr = new BufferedReader(new FileReader(
                   new File(filename)));
               String line2 = null;
               while ((line2 = sbr.readLine()) != null) {
                 if (line2.startsWith("opt extra-info-digest ") ||
                     line2.startsWith("extra-info-digest ")) {
                   allExtraInfos++;
                   String extraInfoDigest = line2.startsWith("opt ") ?
                       line2.split(" ")[2].toLowerCase() :
                       line2.split(" ")[1].toLowerCase();
                   String filename2 = outputDirectory + "/extra-info/"
                       + descriptorFormat.format(new Date(published))
                       + extraInfoDigest.substring(0, 1) + "/"
                       + extraInfoDigest.substring(1, 2) + "/"
                       + extraInfoDigest;
                   if (new File (filename2).exists()) {
                     foundExtraInfos++;
                   }
                 }
               }
               sbr.close();
               foundServerDescs++;
             }
           }
         }
         sb.append(String.format("%nC, %s, %d/%d (%.1f%%), "
             + "%d/%d (%.1f%%), %d/%d (%.1f%%)",
             validAfterTime, foundVotes, allVotes,
             100.0D * (double) foundVotes / (double) allVotes,
             foundServerDescs, allServerDescs,
             100.0D * (double) foundServerDescs / (double) allServerDescs,
             foundExtraInfos, allExtraInfos,
             100.0D * (double) foundExtraInfos / (double) allExtraInfos));
       }
       this.logger.info(sb.toString());
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not dump statistics to disk.",
           e);
     } catch (ParseException e) {
       this.logger.log(Level.WARNING, "Could not dump statistics to disk.",
           e);
     }
   }
 }
