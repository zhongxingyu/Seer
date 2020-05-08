 /**
  * 
  */
 package com.alertscape.pump.onramp.file;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.alertscape.common.logging.ASLogger;
 import com.alertscape.common.model.Alert;
 import com.alertscape.pump.onramp.AbstractPollingAlertOnramp;
 
 /**
  * @author josh
  * 
  */
 public class FileOnramp extends AbstractPollingAlertOnramp {
   private static final String LINE_HASH = "LINE_HASH";
   private static final String LINE_POINTER = "LINE_POINTER";
   private static final ASLogger LOG = ASLogger.getLogger(FileOnramp.class);
   private static final long MAX_FILE_CHECK_TIME = 30000l;
   private String filename;
   private long lastFileCheck;
   private long lastLinePointer;
   private String lastLineHash;
   private String lastLine;
   private String regex;
   private Pattern pattern;
   private MessageDigest m;
   private RandomAccessFile file;
   private AlertLineProcessor lineProcessor;
 
   public FileOnramp() {
     try {
       m = MessageDigest.getInstance("MD5");
     } catch (NoSuchAlgorithmException e) {
       LOG.error("Couldn't find MD5 digester", e);
     }
   }
 
   /**
    * @return the filename
    */
   public String getFilename() {
     return filename;
   }
 
   /**
    * 
    * @param filename
    *          the filename to set
    */
   public void setFilename(String filename) {
     this.filename = filename;
   }
 
   /**
    * @return the regex
    */
   public String getRegex() {
     return regex;
   }
 
   /**
    * @param regex
    *          the regex to set
    */
   public void setRegex(String regex) {
     if (regex == null) {
       this.regex = null;
       pattern = null;
     } else {
       this.regex = regex.trim();
       pattern = Pattern.compile(this.regex);
     }
   }
 
   @Override
   protected int readUntilEnd() throws Exception {
     int linesProcessed = 0;
     long startTime = System.currentTimeMillis();
 
     while (true) {
       if (!validateFile()) {
         LOG.info("New file, starting over");
         reopenFile();
         lastLinePointer = -1;
       }
       long nextLinePointer = file.getFilePointer();
       String line = file.readLine();
       while (line != null && line.trim().length() == 0) {
         line = file.readLine();
       }
       if (line == null || line.trim().length() == 0) {
         if (linesProcessed > 0) {
           long endTime = System.currentTimeMillis();
           long elapsed = endTime - startTime;
           if (elapsed < 1) {
             elapsed = 1;
           }
           long perSecond = (linesProcessed * 1000 / elapsed);
           LOG.info(getSourceName() + ", nothing left to process, done with " + linesProcessed + " lines in "
               + (endTime - startTime) + "ms at " + perSecond + "/s");
         }
         saveState();
         return linesProcessed;
       }
       String nextLineHash = hashLine(line);
       lastLinePointer = nextLinePointer;
       lastLineHash = nextLineHash;
       lastLine = line;
       state.put(LINE_POINTER, lastLinePointer);
       state.put(LINE_HASH, lastLineHash);
       Matcher matcher = pattern.matcher(line);
       if (!matcher.matches()) {
         continue;
       }
       Alert a = getLineProcessor().createAlert(matcher);
       a.setSeverity(determineSeverity(a));
       if (a != null) {
         // LOG.debug(a.getShortDescription());
         sendAlert(a);
       }
       linesProcessed++;
       if (linesProcessed % 1000 == 0) {
         long endTime = System.currentTimeMillis();
         long elapsed = endTime - startTime;
         long perSecond = (linesProcessed * 1000 / elapsed);
         LOG.info(getSourceName() + " processed " + linesProcessed + " lines in " + (endTime - startTime) + "ms at "
             + perSecond + "/s");
         linesProcessed = 0;
         startTime = System.currentTimeMillis();
         saveState();
       }
     }
   }
 
   private void reopenFile() throws FileNotFoundException {
     if (file != null) {
       try {
         file.close();
       } catch (IOException e) {
         LOG.error("Couldn't close file", e);
       }
     }
     file = new RandomAccessFile(filename, "r");
     lastFileCheck = System.currentTimeMillis();
   }
 
   private boolean validateFile() throws IOException {
     long fileTime = System.currentTimeMillis() - lastFileCheck;
 
     // Reopen the file if it's been open for more than the max time
     if (fileTime > MAX_FILE_CHECK_TIME) {
       reopenFile();
     }
 
     if (file == null) {
       LOG.info("File was null, opening");
       try {
         reopenFile();
       } catch (FileNotFoundException e) {
         LOG.error("Couldn't find the file to read in", e);
         return false;
       }
     }
 
     if (lastLinePointer == -1) {
       return true;
     }
 
     try {
       if (file.length() < lastLinePointer) {
         LOG.info("File not valid: size was less than the current pointer");
         return false;
       }
 
       file.seek(lastLinePointer);
 
       String line = file.readLine();
       if (line == null) {
         LOG.info("File not valid: read a null line");
         return false;
       }
 
       if (line.length() == 0) {
         LOG.info("The line read is zero length, can't hash so returning valid");
         return true;
       }
 
       String lineHash = hashLine(line);
 
       boolean equals = lineHash.equals(lastLineHash);
       if (!equals) {
         LOG.info("File not valid: line hash for >" + line + "< (" + lineHash + ") does not equal current hash of >"
             + lastLine + "< (" + lastLineHash + ")");
       }
 
       return equals;
     } catch (Exception e) {
       return false;
     }
   }
 
   private String hashLine(String line) {
     m.reset();
     m.update(line.getBytes(), 0, line.length());
     return new BigInteger(1, m.digest()).toString(16);
   }
 
   @Override
   protected void initState(Map<String, Object> state) {
    if (state != null) {
      try {
        lastLinePointer = (Long) state.get(LINE_POINTER);
      } catch (Exception e) {
        lastLinePointer=0;
        LOG.error("Couldn't intialize line pointer for file, starting at 0");
      }
      lastLineHash = (String) state.get(LINE_HASH);
    }
     LOG.info("Initialized file reading state to: pointer: " + lastLinePointer + ", hash: " + lastLineHash);
   }
 
   /**
    * @return the lineProcessor
    */
   public AlertLineProcessor getLineProcessor() {
     return lineProcessor;
   }
 
   /**
    * @param lineProcessor
    *          the lineProcessor to set
    */
   public void setLineProcessor(AlertLineProcessor lineProcessor) {
     this.lineProcessor = lineProcessor;
   }
 
 }
