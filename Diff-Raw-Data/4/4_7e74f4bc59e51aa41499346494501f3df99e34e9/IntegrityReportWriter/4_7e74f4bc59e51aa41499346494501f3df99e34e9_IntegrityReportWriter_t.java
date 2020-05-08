 package org.bitrepository.integrityservice.reports;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to handle writing (streaming) report parts, and generation (writing) of the final report 
  */
 public class IntegrityReportWriter {
     private static final Logger log = LoggerFactory.getLogger(IntegrityReportWriter.class);
 
     private static final String DELETED_FILE = "deletedFile";
     private static final String CHECKSUM_ISSUE_PREFIX = "checksumIssue-";
     private static final String MISSING_CHECKSUM_PREFIX = "missingChecksum-";
     private static final String OBSOLETE_CHECKSUM_PREFIX = "obsoleteChecksum-";
     private static final String MISSING_FILE_PREFIX = "missingFile-";
     private static final String REPORT_FILE = "report";
     
     private static final String SECTION_HEADER_START_STOP = "========";
     private static final String PILLAR_HEADER_START_STOP = "--------";
     private static final String NOISSUE_HEADER_START_STOP = "++++++++";
     
     private final Map<String, BufferedWriter> missingFiles = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> checksumIssues = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> missingChecksums = new TreeMap<String, BufferedWriter>();
     private final Map<String, BufferedWriter> obsoleteChecksums = new TreeMap<String, BufferedWriter>();
     private BufferedWriter deletedFilesWriter;
     private BufferedWriter reportFileWriter;
     private final File reportDir;
     
     public IntegrityReportWriter(File reportDir) {
         this.reportDir = reportDir;
     }
     
     /**
      * Method to retrieve the path to the report file 
      */
     public String getReportFilePath() {
         File reportFile = new File(reportDir, REPORT_FILE);
         log.debug("getReportFilePath: Report file located at: " + reportFile.getAbsolutePath());
         return reportFile.getAbsolutePath();
     }
         
     /**
      * Method to handle writing of a deleted file entry to on-disk storage 
      * @param fileID The ID of the file to be added to the report as deleted from system.
      */
     public void writeDeletedFile(String fileID) throws IOException {
         if(deletedFilesWriter == null) {
             File deletedFilesFile = makeEmptyFile(reportDir, DELETED_FILE);
             deletedFilesWriter = new BufferedWriter(new FileWriter(deletedFilesFile, true));
         }
         addLine(deletedFilesWriter, fileID);
     }
     
     /**
      * Method to handle writing of a checksum issue for a file on a given pillar
      * @param pillarID The ID of the pillar with a checksum issue
      * @param fileID The ID of the file which have a checksum issue 
      */
     public void writeChecksumIssue(String pillarID, String fileID) throws IOException {
         BufferedWriter checksumIssueWriter;
         String key = CHECKSUM_ISSUE_PREFIX + pillarID;
         if(!checksumIssues.containsKey(key)) {
             File checksumIssueFile = makeEmptyFile(reportDir, key);
             checksumIssueWriter = new BufferedWriter(new FileWriter(checksumIssueFile, true));
             checksumIssues.put(key, checksumIssueWriter);
         } else {
             checksumIssueWriter = checksumIssues.get(key); 
         }
         addLine(checksumIssueWriter, fileID);        
     }
     
     /**
      * Method to handle writing of a missing file entry on a given pillar
      * @param pillarID The ID of the pillar where the file is missing
      * @param fileID The ID of the missing file
      */
     public void writeMissingFile(String pillarID, String fileID) throws IOException {
         BufferedWriter missingFileWriter;
         String key = MISSING_FILE_PREFIX + pillarID;
         if(!missingFiles.containsKey(key)) {
             File missingFileFile = makeEmptyFile(reportDir, key);
             missingFileWriter = new BufferedWriter(new FileWriter(missingFileFile, true));
             missingFiles.put(key, missingFileWriter);
         } else {
             missingFileWriter = missingFiles.get(key);
         }
         addLine(missingFileWriter, fileID);
     }
     
     /**
      * Method to handle writing of a obsolete checksum entry for a given pillar
      * @param pillarID The ID of the pillar with the obsolete checksum
      * @param fileID The ID of the file which have an obsolete checksum 
      */
     public void writeObsoleteChecksum(String pillarID, String fileID) throws IOException {
         BufferedWriter obsoleteChecksumWriter;
         String key = OBSOLETE_CHECKSUM_PREFIX + pillarID;
         if(!obsoleteChecksums.containsKey(key)) {
             File obsoleteChecksumFile = makeEmptyFile(reportDir, key);
             obsoleteChecksumWriter = new BufferedWriter(new FileWriter(obsoleteChecksumFile, true));
             obsoleteChecksums.put(key, obsoleteChecksumWriter);
         } else {
             obsoleteChecksumWriter = obsoleteChecksums.get(key);
         }
         addLine(obsoleteChecksumWriter, fileID);
     }
     
     /**
      * Method to handle writing of a missing checksum entry for a given pillar
      * @param pillarID The ID of the pillar with the missing checksum
      * @param fileID The ID of the file missing a checksum 
      */
     public void writeMissingChecksum(String pillarID, String fileID) throws IOException {
         BufferedWriter missingChecksumWriter;
         String key = MISSING_CHECKSUM_PREFIX + pillarID;
         if(!missingChecksums.containsKey(key)) {
             File missingChecksumFile = makeEmptyFile(reportDir, key);
             missingChecksumWriter = new BufferedWriter(new FileWriter(missingChecksumFile, true));
             missingChecksums.put(key, missingChecksumWriter);
         } else {
             missingChecksumWriter = missingChecksums.get(key);
         }
         addLine(missingChecksumWriter, fileID);
     }
     
     /**
      * Method to write the full report. If a report already exists, the old file will be deleted 
      * and a fresh one generated.   
      */
     public void writeReport(String reportHeader) throws IOException {
         flushAll();
         if(reportFileWriter == null) {
             File reportFile = new File(reportDir, REPORT_FILE);
             if(reportFile.exists()) {
                 reportFile.delete();
             }
             reportFileWriter = new BufferedWriter(new FileWriter(reportFile, true));
         }
         reportFileWriter.append(reportHeader);
         reportFileWriter.newLine();
        
         writeSectionHeader(reportFileWriter, "Deleted files");
         if(deletedFilesWriter != null) {
             writeSectionPart(reportFileWriter, new File(reportDir, DELETED_FILE));
         } else {
             writeNoIssueHeader(reportFileWriter, "No deleted files detected");
         }
         writeSectionHeader(reportFileWriter, "Missing files");
         if(!missingFiles.isEmpty()) {
             writeReportSection(reportFileWriter, missingFiles.keySet());
         } else {
             writeNoIssueHeader(reportFileWriter, "No missing files detected");
         }
         
         writeSectionHeader(reportFileWriter, "Checksum issues");
         if(!checksumIssues.isEmpty()) {
             writeReportSection(reportFileWriter, checksumIssues.keySet());
         } else {
             writeNoIssueHeader(reportFileWriter, "No checksum issues detected");
         }
         
         writeSectionHeader(reportFileWriter, "Missing checksums");
         if(!missingChecksums.isEmpty()) {
             writeReportSection(reportFileWriter, missingChecksums.keySet());
         } else {
             writeNoIssueHeader(reportFileWriter, "No missing checksums detected");
         }
         
         writeSectionHeader(reportFileWriter, "Obsolete checksums");
         if(!obsoleteChecksums.isEmpty()) {
             writeReportSection(reportFileWriter, obsoleteChecksums.keySet());
         } else {
             writeNoIssueHeader(reportFileWriter, "No obsolete checksums detected");
         }
         
         reportFileWriter.flush();
         
     }
     
     /**
      * Flushes all open files 
      */
     private void flushAll() throws IOException {
         if(deletedFilesWriter != null) {
             deletedFilesWriter.flush();
         }
         
         for(BufferedWriter writer : missingFiles.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : checksumIssues.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : missingChecksums.values()) {
             writer.flush();
         }
         
         for(BufferedWriter writer : obsoleteChecksums.values()) {
             writer.flush();
         }
     }
     
     /**
      * Method to close all open writers/streams
      * Only call close after finished using object.  
      */
     public void close() throws IOException {
         if(reportFileWriter != null) {
             reportFileWriter.close();
         }
         
         if(deletedFilesWriter != null) {
             deletedFilesWriter.close();
         }
         
         for(BufferedWriter writer : missingFiles.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : checksumIssues.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : missingChecksums.values()) {
             writer.close();
         }
         
         for(BufferedWriter writer : obsoleteChecksums.values()) {
             writer.close();
         }
     }
     
     /**
      * Helper method to write the header of a report section 
      */
     private void writeSectionHeader(BufferedWriter report, String sectionName) throws IOException {
         report.append(SECTION_HEADER_START_STOP + " " + sectionName + " " + SECTION_HEADER_START_STOP);
         report.newLine();
     }
     
     /**
      * Helper method to write the header of a pillar part of a section 
      */
     private void writePillarHeader(BufferedWriter report, String pillarName) throws IOException {
         report.append(PILLAR_HEADER_START_STOP + " " + pillarName + " " + PILLAR_HEADER_START_STOP);
         report.newLine();
     }
     
     /**
      * Helper method to write the no-issue header of a section 
      */
     private void writeNoIssueHeader(BufferedWriter report, String issueMessage) throws IOException {
         report.append(NOISSUE_HEADER_START_STOP + " " + issueMessage + " " + NOISSUE_HEADER_START_STOP);
         report.newLine();
 
     }
     
     /**
      * Helper method to write the content of a report section 
      */
     private void writeReportSection(BufferedWriter report, Set<String> section) throws IOException {
         for(String part : section) {
             String pillarName = part.split("-")[1];
             File partFile = new File(reportDir, part);
             log.debug("Writing part for pillar: " + pillarName);
             writePillarHeader(report, pillarName);
             writeSectionPart(report, partFile);
         }
     }
     
     /**
      * Helper method to read the actual content and write it back to the combined report file. 
      */
     private void writeSectionPart(BufferedWriter report, File partData) throws FileNotFoundException {
         BufferedReader br = new BufferedReader(new FileReader(partData));
         String fileID;
         try {
             while ((fileID = br.readLine()) != null) {
                 report.append(fileID);
                 report.newLine();
             }
         } catch (IOException e) {
             log.error(e.getMessage(), e);
         } finally {
             try {
                 br.close();
             } catch (IOException e) {
                 log.error(e.getMessage(), e);
             }
         }
     }
     
     /**
      * Creates a File object, and makes sure that it's empty. I.e. deletes the old file if present on disk. 
      */
     private File makeEmptyFile(File dir, String fileName) {
        File file = new File(reportDir, fileName);
         if(file.exists()) {
             file.delete();
         }
         return file;
     }
     
     /**
      * Helper method to add an entry to a partial report file. 
      * Writes the line, adds a new line, and flushes to disk. 
      */
     private void addLine(BufferedWriter writer, String line) throws IOException {
         writer.append(line);
         writer.newLine();
         writer.flush(); 
     }
 }
