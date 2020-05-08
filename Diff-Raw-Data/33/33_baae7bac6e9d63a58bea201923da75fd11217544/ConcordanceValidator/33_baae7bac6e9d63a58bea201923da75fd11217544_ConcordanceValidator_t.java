 package org.objectrepository.validation;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Author: Christian Roosendaal
  */
 
 public class ConcordanceValidator {
 
     // mandatory columns:
     private static final String OBJECT_COLUMN_NAME = "objnr";
     private static final String INV_COLUMN_NAME = "ID";
     private static final String VOLGNR_COLUMN_NAME = "volgnr";
     private static final String TIF_COLUMN_NAME = "master";
 
     // optional columns:
     private static final String PID_COLUMN_NAME = "PID";
     private static final String JPEG_COLUMN_NAME = "jpeg";
     private static final String JPEG2_COLUMN_NAME = "jpeg2";
     private static final String OCR_COLUMN_NAME = "OCR";
 
     private static String pattern = "^[a-zA-Z0-9-:" + escapeMetacharacters("._()[]{@$}=\\") + "]{1,240}$";
 
     private static final String CSV_SEPARATOR = ",";
 
     private static final String ERROR_FILE_EXISTENCE = " file entry in concordance table does not exist in directory";
     private static final String ERROR_CONCORDANCE_FILE_MISSING = " file in directory is not listed in the concordance table";
     private static final String ERROR_CONCORDANCE_FILE_DUPLICATE = "Duplicate entry in concordance table.";
 
     private static final String REPORT_FILE = "report.txt";
 
 
     private static final int MINIMAL_FILE_SIZE = 1000;
 
     private static final byte[] MAGIC_NUMBER_TIFF_BIG_ENDIAN = new byte[]{(byte) 0x4D, (byte) 0x4D, (byte) 0x00, (byte) 0x2A};
     private static final byte[] MAGIC_NUMBER_TIFF_LITTLE_ENDIAN = new byte[]{(byte) 0x49, (byte) 0x49, (byte) 0x2A, (byte) 0x00};
     private static final byte[] MAGIC_NUMBER_JPEG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
 
 
     int objectColumnNr;
     int invColumnNr;
     int masterColumnNr;
     int volgNrColumnNr;
     int pidColumnNr;
     int jpegColumnNr;
     int jpeg2ColumnNr;
     int jpeg3ColumnNr;
     int ocrColumnNr;
 
     boolean objectColumnPresent;
     boolean masterColumnPresent;
     boolean invColumnPresent;
     boolean volgNrColumnPresent;
     boolean ocrPresent;
     boolean jpegPresent;
     boolean jpeg2Present;
     boolean pidColumnPresent;
 
     // for testing purposes:
     public static boolean exitCalled;
     public static boolean warning;
     public static boolean relationshipError;
     public static boolean fileOrHeaderError;
     public static boolean volgnummerError;
     public static boolean headerOrFilesizeError;
 
 
     boolean isUnitTesting = false;
 
     ArrayList<File> subDirList = new ArrayList<File>();
 
     private String fileSet;
     private String archivalID;
     private String na;
     private String baseFolder; // parent folder of the fileSet
 
     File concordanceFile;
 
 
     public ConcordanceValidator(File file) {
 
         this.fileSet = file.getAbsolutePath();
         this.archivalID = file.getName();
         File parentFile = file.getParentFile();
         this.na = parentFile.getName();
         this.baseFolder = parentFile.getAbsolutePath();
         this.pidColumnPresent = false;
         this.exitCalled = false;
 
         String concordanceFileLocation = this.fileSet + "/" + archivalID + ".csv";
 
         this.concordanceFile = new File(concordanceFileLocation);
 
         // setup the file to log all output:
         new File(fileSet + "/" + REPORT_FILE);
     }
 
     public void start() {
 
         parseColumns();
 
 
         try {
 
             testVolgnummers();
 
             testRelationShips();
 
             testFileExistenceAndTestHeaders(masterColumnNr);
 
             testDoubles();
 
             testCharacters();
 
            testSubdirectories();
 
             if (jpegPresent) {
                 testFileExistenceAndTestHeaders(jpegColumnNr);
 //                testSubdirectories();
             }
 
             if (jpeg2Present) {
                 testFileExistenceAndTestHeaders(jpeg2ColumnNr);
 //                testSubdirectories();
             }
 
             if (ocrPresent) {
                 testFileExistenceAndTestHeaders(ocrColumnNr);
 //                testSubdirectories();
             }
 
 
             if (!pidColumnPresent) createPidColumn();
 
             writeLog("----  All tests passed.  ----");
             writeLog("");
 
         } catch (IOException e) {
             writeLog(e.getMessage());
         }
 
 
     }
 
     private void testCharacters() throws IOException {
         final BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
         final Pattern p = Pattern.compile(pattern);
 
         String line;
         int lineNr = 0;
         while ((line = input.readLine()) != null) {
             if (line.trim().isEmpty()) continue;
 
             lineNr++;
             String[] columnNames = line.split(CSV_SEPARATOR);
             String filename = new File(columnNames[masterColumnNr]).getName();
             Matcher m = p.matcher(escapeMetacharacters(filename));
             if (!m.find(0)) {
                 writeErrorLog("The filename '" + filename + "' contains an invalid character.", lineNr, line);
             } else {
                 m = p.matcher(escapeMetacharacters(escapeMetacharacters(columnNames[invColumnNr])));
                 if (!m.find(0)) {
                     writeErrorLog("The inventory number '" + columnNames[invColumnNr] + "' contains an invalid character.", lineNr, line);
                 }
             }
         }
     }
 
     /**
      * testDoubles
      * <p/>
      * See if a filename is mentioned elsewhere in the list
      */
     private void testDoubles() throws IOException {
 
         final BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
 
         final List list = new ArrayList<String>();
         String line;
         int lineNr = 0;
         while ((line = input.readLine()) != null) {
             if (line.trim().isEmpty()) continue;
 
             lineNr++;
             String[] columnNames = line.split(CSV_SEPARATOR);
             String masterColumn = columnNames[masterColumnNr];
             if (list.contains(masterColumn)) {
                 writeErrorLog("Duplicate file entry '" + masterColumn + "'", lineNr, line);
             } else
                 list.add(masterColumn);
         }
     }
 
 
     /*
     * Creates an extra column in the concordance table to contain PID numbers, if it is not already there.
     *
     * */
     public void createPidColumn() {
 
         File tempConcordanceFile = new File(fileSet + "/concordanceValidWithPID.csv");
 
         try {
             BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
             BufferedWriter output = new BufferedWriter(new FileWriter(tempConcordanceFile));
 
 
             String inputLine;
             String outputLine;
 
             inputLine = input.readLine();
             if (inputLine.charAt(inputLine.length() - 1) == CSV_SEPARATOR.charAt(0)) {
                 outputLine = inputLine + "PID";
             } else {
                 outputLine = inputLine + CSV_SEPARATOR + "PID";
             }
 
             output.write(outputLine + "\n");
 
             while ((inputLine = input.readLine()) != null) {
                 if (inputLine.trim().isEmpty()) continue;
 
                 final String pid = na + "/" + UUID.randomUUID().toString().toUpperCase();
                 if (inputLine.charAt(inputLine.length() - 1) == CSV_SEPARATOR.charAt(0)) {
                     outputLine = inputLine + pid;
                 } else {
                     outputLine = inputLine + CSV_SEPARATOR + pid;
                 }
 
                 output.write(outputLine + "\n");
             }
 
             output.flush();
             input.close();
             output.close();
 
             concordanceFile = tempConcordanceFile;
 
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         writeLog("Creation of PID column containing UUIDs succeeded.");
 
     }
 
 
     public void parseColumns() {
         try {
 
             BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
 
             String line = input.readLine();
 
             String[] columnNames = line.split(CSV_SEPARATOR);
 
             if (columnNames.length < 3) {
                 writeErrorLog(" incorrect CSV separator. Expected \";\".", 1, line);
                 exit();
             }
 
             for (int i = 0; i < columnNames.length; i++) {
 
                 if (columnNames[i].equalsIgnoreCase(OBJECT_COLUMN_NAME)) {
                     objectColumnNr = i;
                     objectColumnPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(INV_COLUMN_NAME)) {
                     invColumnNr = i;
                     invColumnPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(TIF_COLUMN_NAME)) {
                     masterColumnNr = i;
                     masterColumnPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(VOLGNR_COLUMN_NAME)) {
                     volgNrColumnNr = i;
                     volgNrColumnPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(PID_COLUMN_NAME)) {
                     pidColumnNr = i;
                     pidColumnPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(JPEG_COLUMN_NAME)) {
                     jpegColumnNr = i;
                     jpegPresent = true;
                 } else if (columnNames[i].equalsIgnoreCase(JPEG2_COLUMN_NAME)) {
                     jpeg2ColumnNr = i;
                     jpeg2Present = true;
                 } else if (columnNames[i].equalsIgnoreCase(OCR_COLUMN_NAME)) {
                     ocrColumnNr = i;
                     ocrPresent = true;
                 }
 
             }
 
             if (!objectColumnPresent || !masterColumnPresent || !volgNrColumnPresent) {
 
                 writeErrorLog("One of the mandatory colunms " + OBJECT_COLUMN_NAME + ", "
                         + TIF_COLUMN_NAME + ", " + VOLGNR_COLUMN_NAME + " is missing in the concordance table");
                 exit();
 
             }
 
 
         } catch (IOException e) {
 
             writeErrorLog(" Concordance table file not found or cannot read file: " + concordanceFile);
             exit();
 
         }
 
         writeLog("Parsing columns complete. No errors detected.");
 
 
     }
 
 
     // check if every file in all subdirectories have the right archivalID.
     public void testSubdirectories() {
 
         for (File dir : subDirList) {
 
             File[] fileList = dir.listFiles();
 
             for (File objectDir : fileList) {
 
                 String[] objects = objectDir.list();
 
                 for (String object : objects) {
 
                     String prefixOfFile = object.split("_")[0];
                     if (!prefixOfFile.equals(archivalID)) {
 
                         writeErrorLog("Error : file " + object + " has incorrect archivalID. Expected \"" + archivalID + "\". Encountered: \"" + prefixOfFile + "\"");
                         exit();
 
                     }
                 }
 
 
             }
 
         }
 
 
         writeLog("Subdirectory archivalID test passed. All data files have the right archivalID: \"" + archivalID + "\"");
 
 
     }
 
 
     public void testRelationShips() {
 
         BufferedReader input = null;
         int lineNr = 2;
         String line;
 
         try {
             input = new BufferedReader(new FileReader(concordanceFile));
 
             // skip the first line containing column names:
             input.readLine();
 
             while ((line = input.readLine()) != null) {
                 if (line.trim().isEmpty()) continue;
 
                 String[] columns = line.split(CSV_SEPARATOR);
 
                 String tifImage = columns[masterColumnNr].split("\\.")[0];
                 String jpegImage = columns[jpegColumnNr].split("\\.")[0];
 
                 String[] tifImageArray = tifImage.split("/");
 
                 if (tifImageArray.length < 2) {
                     writeErrorLog("Incorrect path name for master image at column " + masterColumnNr, lineNr, line);
                     relationshipError = true;
                 }
 
                 tifImage = tifImageArray[tifImageArray.length - 1];
 
                 String[] jpegImageArray = jpegImage.split("/");
 
                 if (jpegImageArray.length < 2) {
                     writeErrorLog("Incorrect path name for jpeg image at column " + jpegColumnNr, lineNr, line);
                     relationshipError = true;
                 }
 
                 jpegImage = jpegImageArray[jpegImageArray.length - 1];
 
 
                 if (!tifImage.equals(jpegImage)) {
                     writeErrorLog("Warning: Difference in filenames between " + tifImage + " and " + jpegImage, lineNr, line);
                     relationshipError = true;
                 }
 
                 if (jpeg2Present) {
                     String jpeg2Image = columns[jpeg2ColumnNr].split("\\.")[0];
 
                     String[] jpeg2ImageArray = jpeg2Image.split("/");
 
                     if (jpegImageArray.length < 2) {
                         writeErrorLog("Incorrect path name for jpeg image at column " + jpeg2ColumnNr, lineNr, line);
                         relationshipError = true;
                     }
 
                     jpeg2Image = jpeg2ImageArray[jpeg2ImageArray.length - 1];
 
                     if (!jpeg2Image.equalsIgnoreCase(jpegImage)) {
                         writeErrorLog("Warning: Difference in filenames between " + jpeg2Image + " and " + jpegImage, lineNr, line);
                         relationshipError = true;
                     }
                 }
 
 
                 if (ocrPresent) {
                     String ocr = columns[ocrColumnNr].split("\\.")[0];
                     String[] ocrArray = ocr.split("/");
 
                     if (ocrArray.length < 2) {
                         writeErrorLog("Incorrect path name at column " + ocrColumnNr, lineNr, line);
                         relationshipError = true;
                     }
 
                     ocr = ocrArray[ocrArray.length - 1];
 
                     if (!ocr.equalsIgnoreCase(jpegImage)) {
                         writeErrorLog("Warning: Difference in filenames between " + ocr + " and " + jpegImage, lineNr, line);
                         relationshipError = true;
                     }
                 }
 
 
                 lineNr++;
 
             }
 
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (!relationshipError) {
             writeLog("Relationship test between columns test passed. All image files denoted in the table correspond to each other.");
         }
     }
 
     // checks if the volgnummers and objectnummers are in correct order.
     // i.e.: (objectnummer:volgnummer) 1:1, 1:2, 1:3, 2:1, 2:2, 2:3, 3:1, ...,
     public void testVolgnummers() {
         BufferedReader input = null;
         String line;
         int lineNr = 2;
         int expectedVolgNr = 1;
         int expectedObjNr = 1;
         ArrayList<ObjectNumber> numberList = new ArrayList<ObjectNumber>();
 
 
         try {
             input = new BufferedReader(new FileReader(concordanceFile));
 
             // skip the first line containing column names:
             input.readLine();
 
             while ((line = input.readLine()) != null) {
 
                 if (line.trim().isEmpty()) continue;
 
                 String[] columns = line.split(CSV_SEPARATOR);
                 String volgNr = columns[volgNrColumnNr];
                 String objNr = columns[objectColumnNr];
                 int volgNrParsed = -1;
                 int objNrParsed = -1;
 
                 // try parsing the volgnummer, throw error if not a number:
                 try {
                     volgNrParsed = Integer.parseInt(volgNr);
                 } catch (NumberFormatException e) {
                     writeErrorLog(" incorrect entry '" + volgNr + "' in volgnummer column", lineNr, line);
                     volgnummerError = true;
                 }
                 // try parsing the objectnummer, throw error if not a number:
               /* try {
                     objNrParsed = Integer.parseInt(objNr);
                 } catch (NumberFormatException e) {
                     writeErrorLog(" incorrect entry '" + objNr + "' in object nummer column", lineNr, line);
                     volgnummerError = true;
                }*/
 
                 ObjectNumber combinedNumber = new ObjectNumber(objNrParsed, volgNrParsed, lineNr);
                 numberList.add(combinedNumber);
                 lineNr++;
             }
 
             for (ObjectNumber combinedNumber : numberList) {
                 if (combinedNumber.getObjectNumber() != expectedObjNr) {
                     expectedObjNr++;

                     if (combinedNumber.getObjectNumber() != expectedObjNr) {
                         if (!sortedVolgnummerCorrect(numberList)) {
                            //writeErrorLog(" objectnummer '" + expectedObjNr + "' incorrect.", combinedNumber.getLineNumber(), line);
                            //volgnummerError = true;
                         } else {
                            //writeErrorLog("Warning: objectnummer '" + expectedObjNr + "' incorrect", combinedNumber.getLineNumber(), line);
                            //writeErrorLog("After sorting no errors were found. This probably means two or more lines in the table have been switched");
                            //volgnummerError = true;
                         }
                         expectedObjNr = combinedNumber.getObjectNumber();
                     }

                     expectedVolgNr = 1;
                 }
 
                 if (combinedNumber.getVolgNumber() != expectedVolgNr) {
 
                     if (!sortedVolgnummerCorrect(numberList)) {
 
                         writeErrorLog(" volgnummer '" + combinedNumber.getVolgNumber() + "' incorrect. Expected: " + expectedVolgNr, combinedNumber.getLineNumber(), line);
                         volgnummerError = true;
 
                     } else {
 
                         writeErrorLog("Warning: volgnummer '" + combinedNumber.getVolgNumber() + "' incorrect. Expected: " + expectedVolgNr, combinedNumber.getLineNumber(), line);
                         writeErrorLog("After sorting no errors were found. This means two or more lines in the table have been switched.");
                         volgnummerError = true;
 
                     }
                 }
                 expectedVolgNr++;
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (!volgnummerError) {
             writeLog("Volgnummer test passed.");
         }
     }
 
 
     private boolean sortedVolgnummerCorrect(ArrayList<ObjectNumber> numberList) {
         int expectedObjNr = 1;
         int expectedVolgNr = 1;
 
         Collections.sort(numberList, new
                 Comparator<ObjectNumber>() {
                     public int compare(ObjectNumber lhs, ObjectNumber rhs) {
 
                         if (lhs.getObjectNumber() > rhs.getObjectNumber()) return 1;
                         else if (lhs.getObjectNumber() < rhs.getObjectNumber()) return -1;
 
                         if (lhs.getVolgNumber() > rhs.getVolgNumber()) return 1;
                         else if (lhs.getVolgNumber() < rhs.getVolgNumber()) return -1;
 
                         return 0;
 
                     }
 
                 });
 
         for (ObjectNumber combinedNumber : numberList) {
             if (combinedNumber.getObjectNumber() != expectedObjNr) {
                 expectedObjNr++;
                 if (combinedNumber.getObjectNumber() == expectedObjNr) {
                     expectedVolgNr = 1;
                 } else {
                     return false;
                 }
             }
 
             if (combinedNumber.getVolgNumber() != expectedVolgNr) {
 
                 return false;
 
             }
             expectedVolgNr++;
         }
 
         return true;
     }
 
 
     public static String getExtension(File f) {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 
         if (i > 0 && i < s.length() - 1)
             ext = s.substring(i + 1).toLowerCase();
 
         if (ext == null)
             return "";
         return ext;
     }
 
     public void testHeaderAndFilesize(File inputFile, int columnNr) {
         byte[] magicNumber = {};
         String extension = getExtension(inputFile);
 
         if (extension.equalsIgnoreCase("tif") || extension.equalsIgnoreCase("tiff")) {
             magicNumber = MAGIC_NUMBER_TIFF_LITTLE_ENDIAN;
         } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
             magicNumber = MAGIC_NUMBER_JPEG;
         } else {
             writeLog(" cannot check header of file " + inputFile + ". File does not have a recognizable extension: " + extension);
             headerOrFilesizeError = true;
         }
 
 
         try {
 
             byte[] b = new byte[4];
 
             FileInputStream fis = new FileInputStream(inputFile);
             if (inputFile.length() < MINIMAL_FILE_SIZE) {
 
                 writeErrorLog(" file " + inputFile + " has size smaller than limit of " + MINIMAL_FILE_SIZE + " bytes");
                 headerOrFilesizeError = true;
             }
 
             if (fis.read(b) < 4) {
                 writeErrorLog("Error reading first 4 bytes of file " + inputFile);
                 headerOrFilesizeError = true;
             }
 
             if (!Arrays.equals(b, magicNumber)) {
 
                 // if TIF file and magic number incorrect, check Big Endian magic number too:
                 if (extension.equalsIgnoreCase("tif") || extension.equalsIgnoreCase("tiff") &&
                         !Arrays.equals(b, MAGIC_NUMBER_TIFF_BIG_ENDIAN)) {
                     writeErrorLog(" The file " + inputFile + " has extension " + extension + " but does not have the correct header.");
                     headerOrFilesizeError = true;
                 }
             }
 
             fis.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
 
     private void writeErrorLog(String logString, int lineNr, String line) {
 
         System.out.println("Error: " + logString);
         System.out.println("line " + lineNr + ": " + line);
         System.out.println("");
     }
 
     private void writeErrorLog(String logString) {
 
         System.out.println("Error: " + logString);
         System.out.println("");
     }
 
     private void writeLog(String logString) {
         System.out.println(logString);
     }
 
     void exit() {
 //        try {
 //            reportOutput.close();
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //        }
 
 
         System.getProperties();
         exitCalled = true;
 
         if (!isUnitTesting) {
             System.exit(0);
         }
     }
 
 
     public int countLines(File concordanceFile) throws IOException {
         BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
         int nrOfLines = 0;
 
         String line;
         while ((line = input.readLine()) != null) {
             if (line.trim().isEmpty()) continue;
             nrOfLines++;
         }
 
         return nrOfLines;
 
     }
 
 
     public void testFileExistenceAndTestHeaders(int columnNumber) throws IOException {
         BufferedReader input = new BufferedReader(new FileReader(concordanceFile));
         String line;
         String subDir = "";
         String errorString = "";
 
         // line 1 contains column names so start with line 2:
         int lineNr = 2;
 
         ArrayList<String> concordanceFileList = new ArrayList<String>();
         ArrayList<String> objectList = new ArrayList<String>();
 
         // skip the first line containing the column names:
         input.readLine();
 
         writeLog("Counting number of lines in concordance file...");
         int countedLines = countLines(concordanceFile);
         writeLog("Number of lines in concordance file: " + countedLines);
 
         // check if file in concordance table exists in directory:
         writeLog("Checking if file in concordance table exists in directory..");
         while ((line = input.readLine()) != null) {
 
             if (line.trim().isEmpty()) continue;
             String[] columns = line.split(CSV_SEPARATOR);
             String fileWithSubdir = columns[columnNumber];
             String[] fileWithSubdirArray = fileWithSubdir.split("/");
 
             if (lineNr == 2) {
                 subDir = "";
                 for (int i = 1; i < (fileWithSubdirArray.length - 2); i++) {
                     subDir += fileWithSubdirArray[i] + "/";
                 }
             }
             concordanceFileList.add(fileWithSubdirArray[fileWithSubdirArray.length - 1]);
             String objectNr = columns[objectColumnNr];
             objectList.add(objectNr);
 
             File file = new File(baseFolder + "/" + fileWithSubdir);
             if (!file.exists()) {
 
                 errorString += ERROR_FILE_EXISTENCE + ": " + file + "\n";
                 errorString += "Concordance file " + file + ", line " + lineNr + " column " + columnNumber + "\n";
                 fileOrHeaderError = true;
 
             } else {
 
                 // test header of image files
                 testHeaderAndFilesize(file, columnNumber);
 
             }
 
             String invColumn = columns[objectColumnNr];
             String correspondingSubdir = baseFolder + "/" + subDir + "/" + invColumn;
             file = new File(correspondingSubdir);
             if (!file.exists()) {
 
                 writeErrorLog(" found objectnummer " + objectNr + " in concordance table without corresponding subdirectory "
                         + correspondingSubdir, lineNr, line);
                 fileOrHeaderError = true;
 
             }
 
             lineNr++;
         }
 
 
         // remove duplicates from object list:
         HashSet h = new HashSet(objectList);
         objectList.clear();
         objectList.addAll(h);
 
         // save the directory for further tests:
         File subDirFile = new File(baseFolder + "/" + subDir);
         subDirList.add(subDirFile);
 
         // check if the amount of subdirectories (objectnumbers) is the same as in concordance table:
         String[] subdirsCheck = subDirFile.list(new FilenameFilter() {
 
             public boolean accept(File dir, String name) {
                 return dir.isDirectory();
             }
         });
 
         if (subdirsCheck == null) {
             writeErrorLog("\nError during header and filesize test: Trying to list subdirectories of " + subDirFile);
             exit();
 
         }
 
         if (subdirsCheck.length != objectList.size()) {
             errorString += "Amount of directories found in " + subDirFile + "(" + subdirsCheck.length + ") is not the same as the amount of objects found in concordance file (" + objectList.size() + ")\n";
             errorString += "baseFolder: " + baseFolder + ", subDir: " + subDir + "\n";

            for (String d : subdirsCheck) {
                File folder = new File(d);
                if (!objectList.contains(folder.getName())) {
                    errorString += d + "\n";
                }
            }

             fileOrHeaderError = true;
         }
 
 
         //  check if all files in the data folders exist in the concordance file:
         writeLog("Checking if all files in the data folders exist in the concordance file..");
 
 
         File file = new File(baseFolder + "/" + subDir);
         File[] objectSubdirs = file.listFiles();
 
         Collection<String> listOfAllFiles = new HashSet<String>();
         for (File objectSubdir : objectSubdirs) {
             listOfAllFiles.addAll(Arrays.asList(objectSubdir.list()));
         }
 
         if (listOfAllFiles.removeAll(concordanceFileList)) {// retainAll returns true if there is a difference
             errorString += "The following files are found on disk but are not listed in the concordance table: \n";
             for (String fileAllFile : listOfAllFiles) {
                 errorString += fileAllFile + "\n";
             }
             fileOrHeaderError = true;
         }
 
         if (!fileOrHeaderError) {
             writeLog("Concordance table " + subDir + " <-> Directory " + subDir + " test passed. All " + subDir + " files in concordance" +
                     "table are present in " + subDir + " directory, and all " + subDir + " files in " + subDir + " directory are present in concordance table.");
             writeLog("Header test passed. All files of type " + subDir + " denoted in the concordance table have the right headers.");
             writeLog("File size test passed. All files of type " + subDir + " denoted in the concordance table have a size bigger than " + MINIMAL_FILE_SIZE + " bytes.");
         } else {
             writeErrorLog(errorString);
         }
     }
 
     /**
      * Escapes the regular expression's special characters.
      *
      * @param text
      * @return
      */
     private static String escapeMetacharacters(String text) {
 
         final Character[] metaCharacters = {'[', ']', '{', '}', '^', '$', '.', '|', '?', '*', '+', '(', ')', '\\'};
         ArrayList<Character> list = new ArrayList(Arrays.asList(metaCharacters));
 
         final StringBuilder sb = new StringBuilder();
         for (char c : text.toCharArray()) {
             if (list.contains(c))
                 sb.append("\\" + Character.toString(c));
             else
                 sb.append(c);
         }
         return sb.toString();
     }
 
 }
