 package com.hexcoder.icarrus.dao;
 
 import com.hexcoder.icarrus.dto.MessageHandler;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: 67726e
  * Date: 8/11/11
  * Time: 8:56 PM
  */
 public class HistoryDAO {
     private final File historyFile = new File("data/UploadHistory.idat");
 
 
     public HistoryDAO() {}
 
     public List<List<String[]>> loadHistoryFromFile() {
         List<List<String[]>> historyRows = new LinkedList<List<String[]>>();
         List<String[]> tmpList = new LinkedList<String[]>();                                                            // Temp list to hold the contents of a file block while it is being read
 
         if (!historyFile.exists()) return historyRows;                                                                  // We can stop right here if there is no history file to read
 
         BufferedReader in = null;
         boolean insideBlock = false;
         String line = "";
 
         try {
             in = new BufferedReader(new FileReader(historyFile));
 
             try {
                 while ((line = in.readLine()) != null) {
                     line = line.trim();
                     if (line.length() == 0) continue;                                                                   // If this line is empty, skip over it
 
                     if (insideBlock) {
                         if (line.equals("}")) {                                                                         // End of the current file block, signify this and add the file block data to the List
                             insideBlock = false;
                             historyRows.add(tmpList);
                             tmpList = new LinkedList<String[]>();
                         } else {                                                                                        // Otherwise we have a regular line, perform parameter checks.
                             if (line.charAt(0) != '~') continue;                                                        // Doesn't start with the parameter designator, skip over this line
                             String[] parts = line.split(":");
                             if (parts.length != 2) continue;                                                            // If the line contains more than one colon, it is invalid and skipped over
                             if (parts[0].length() == 1) continue;                                                       // Invalid parameter if the key value is blank
                             if (parts[0].contains(" ") || parts[0].contains("\t")) continue;                            // Skip the line if it contains whitespace
 
                             parts[0] = parts[0].substring(1, parts[0].length());                                        // Strip the tilde from the key String
                            parts[1] = parts[1].replaceAll("~", ":");                                                   // Replace all tildes '~' with colons ':' as per spec
 
                             tmpList.add(parts);                                                                         // Add the valid key/value pair to the temporary file block List
                         }
                     } else {
                         if (line.equals("-File {")) insideBlock = true;                                                 // Check to see if we have the start of a new block
                     }
                 }
             } finally {
                 in.close();
             }
         } catch (IOException e) {
             MessageHandler.postMessage("History Read Error", "The upload history could not be read.", LoggingDAO.ERROR);
         }
 
         return historyRows;
     }
 
     /**
      * Function is called when a new entry is added to the upload history. The history description
      * is appended to the end of the current history file.
      *
      * @param historyEntry is an array containing the key/value pairs for the history data
      */
     public void writeHistoryToFile(String[][] historyEntry) {
         boolean generalError = false;
         StringBuilder writeString = new StringBuilder();
         writeString.append("\n-File {\n");
 
         for (int i = 0; i < historyEntry.length; i++) {
             if (historyEntry[i][1].contains("\n") || historyEntry[i][1].contains("~")) generalError = true;             // The value section cannot contain newline '\n' or tilde '~' characters
             if (historyEntry[i][0].contains(" ")) generalError = true;
 
             if (generalError) break;                                                                                    // Skip any additional processing if there were errors
 
             historyEntry[i][1] = historyEntry[i][1].replace(":", "~");                                                  // Replace all colons with tildes as per the specification
             writeString.append("\t~").append(historyEntry[i][0]).append(":").append(historyEntry[i][1]).append("\n");   // Append this line of the File block to
         }
 
         writeString.append("}\n");
 
         if (generalError) {
             MessageHandler.postMessage("History Content Error", "The history data was invalid and could not be written to file.", LoggingDAO.ERROR);
         }
 
         try { if (!historyFile.exists()) generalError = historyFile.createNewFile(); }                                  // Create a new history file if one does not exist
         catch (IOException e) { generalError = true; }
         if (generalError) {
             MessageHandler.postMessage("History File Error",
                     "A new history file could not be created. The history will not be stored.", LoggingDAO.ERROR);      // Inform the user that the history data file could not be created.
             return;
         }
 
         FileWriter out = null;
         try {
             out = new FileWriter(historyFile, true);                                                                    // Open a writer to the history file and append the data
             try { out.write(writeString.toString()); }                                                                  // Write out the final contents of the StringBuilder if all is well
             finally {
                 out.flush();
                 out.close();
             }
         } catch (IOException e) {
             MessageHandler.postMessage("History Write Error", "The upload history could not be written to file.", LoggingDAO.ERROR);
         }
     }
 }
