 /*
  * //
  * // Copyright (C) 2009 Boutros-Labs(German cancer research center) b110-it@dkfz.de
  * //
  * //
  * //    This program is free software: you can redistribute it and/or modify
  * //    it under the terms of the GNU General Public License as published by
  * //    the Free Software Foundation, either version 3 of the License, or
  * //    (at your option) any later version.
  * //
  * //    This program is distributed in the hope that it will be useful,
  * //    but WITHOUT ANY WARRANTY; without even the implied warranty of
  * //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * //
  * //    You should have received a copy of the GNU General Public License
  * //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package cellHTS.classes;
 
 
 import org.apache.tapestry5.ioc.internal.util.TapestryException;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.text.SimpleDateFormat;
 
 import data.DataFile;
 import data.DataFileParameter;
 import data.Plate;
 import data.Experiment;
 
 
 /**
  *
  * This is our class which does all the parsing of cellHTS files
  * 
  * Created by IntelliJ IDEA.
  * User: oliverpelz
  * Date: 10.11.2008
  * Time: 11:09:37
  *
  */
 public class FileParser {
     
     /**
      *  this will be the operating system dependendant linefeed symol
      */
     public static String lineFeed="\n";
 
 
     /**
      *
      *  check a whole file with a regular expression pattern
      * returns true if it matches the whole file and false if not
      * the value of the returned hasmap is the errorcode returning, when match=true the errorcode will be ""
      * the header Pattern is optional because not all files we want to scan must have a mandantory header
      *
      * @param pattern the header pattern to check a file with, all without the headline
      * @param file the file you want to apply the regexp with
      * @param headerPattern the pattern to check a file with for the headline
      * @return  returns an array of two elements, first will say true or false (true if everything went fine), second element is an possible error message if the first element is false
      * 
      */
     public static String[] checkFileRegExp(Pattern pattern, File file, Pattern headerPattern) {
         //if the file contains only valid content, it returns true and
         //empty error code
         String[] returnArr = {"true", ""};
         
         Matcher m;
         int lineCnt;
         if (pattern == null) {
             String exceptionText = "method checkFileRegExp needs a Pattern as a parameter";
             TapestryException exception = new TapestryException(exceptionText, null);
             throw exception;
         }
 
         try {
             FileReader reader = new FileReader(file);
             BufferedReader buffer = new BufferedReader(reader);
 
             String line;
             lineCnt = 0;
             boolean headerFound = false;
             while ((line = buffer.readLine()) != null) {
                 lineCnt++;
                 //check the header line...aka first line
                 if (headerPattern != null && lineCnt == 1) {
                     //header pattern is optional...but if we have one it has to match!
                     m = headerPattern.matcher(line);
                     if (!m.find()) {
                         returnArr[0] = "false";
                         returnArr[1] = "cannot parse header line: " + line;
                         return returnArr;
                     }
 
                 } else {
                     m = pattern.matcher(line);
                     if (!m.find()) {
                         returnArr[0] = "false";
                         returnArr[1] = "cannot parse line  " + lineCnt + " " + line;
                         return returnArr;
 
                     }
                 }
 
             }
             if (lineCnt == 0) {
                 returnArr[0] = "false";
                 returnArr[1] = "cannot use empty file";
                 return returnArr;
             }
 
         } catch (IOException e) {
             returnArr[0] = "false";
             returnArr[1] = "IO Error";
             return returnArr;
         }
         //only if true
         returnArr[1] = "" + lineCnt;
         return returnArr;
     }
     public static String[] checkFileRegExpIgnoreCase(Pattern pattern, File file, Pattern headerPattern) {
         //if the file contains only valid content, it returns true and
         //empty error code
         String[] returnArr = {"true", ""};
 
         headerPattern = Pattern.compile(headerPattern.pattern(),Pattern.CASE_INSENSITIVE);
         pattern = Pattern.compile(pattern.pattern(),Pattern.CASE_INSENSITIVE);
 
         Matcher m;
         int lineCnt;
         if (pattern == null) {
             String exceptionText = "method checkFileRegExp needs a Pattern as a parameter";
             TapestryException exception = new TapestryException(exceptionText, null);
             throw exception;
         }
 
         try {
             FileReader reader = new FileReader(file);
             BufferedReader buffer = new BufferedReader(reader);
 
             String line;
             lineCnt = 0;
             boolean headerFound = false;
             while ((line = buffer.readLine()) != null) {
                 lineCnt++;
                 //check the header line...aka first line
                 if (headerPattern != null && lineCnt == 1) {
                     //header pattern is optional...but if we have one it has to match!
                     m = headerPattern.matcher(line);
                     if (!m.find()) {
                         returnArr[0] = "false";
                         returnArr[1] = "cannot parse header line: " + line;
                         return returnArr;
                     }
 
                 } else {
                     m = pattern.matcher(line);
                     if (!m.find()) {
                         returnArr[0] = "false";
                         returnArr[1] = "cannot parse line  " + lineCnt + " " + line;
                         return returnArr;
 
                     }
                 }
 
             }
             if (lineCnt == 0) {
                 returnArr[0] = "false";
                 returnArr[1] = "cannot use empty file";
                 return returnArr;
             }
 
         } catch (IOException e) {
             returnArr[0] = "false";
             returnArr[1] = "IO Error";
             return returnArr;
         }
         //only if true
         returnArr[1] = "" + lineCnt;
         return returnArr;
     }
 
     /**
      *
      *  this method needs an empty plateList call by reference for the results of the parsing processs
      * another call by refernce param is wells which returns the well number format of the plates
      * we use an array for the wells to simulate call by reference ;-)
      * it also returns a string arr with possible error messages
      *
      *
      * @param pattern
      * @param file
      * @param headerPattern
      * @param plateList
      * @param wells
      * @return returns an array of two elements, first will say true or false (true if everything went fine), second element is an possible error message if the first element is false
      *
      */
     public static String[] parsePlateConfigFile(Pattern pattern, File file, Pattern headerPattern, HashMap<Integer, HashMap<String, String>> plateList, Integer[] wells) {
         String[] returnArr = {"true", ""};
         Matcher m;
         int plates;
 
         HashMap<String, String> wellMap;
         try {
             FileReader reader = new FileReader(file);
             BufferedReader buffer = new BufferedReader(reader);
 
             String line;
             String headerLine = "";
             int lineCnt = 0;
 
             int validwellTypeCount = 0;
 
             while ((line = buffer.readLine()) != null) {
 
                 if (lineCnt <= 2) {
                     //collect the first three lines
                     headerLine += line + lineFeed;
 
                     if (lineCnt == 2) {
                         //we want to parse the header of the file which is 3 lines long
                         m = headerPattern.matcher(headerLine);
                         boolean matchFound = m.find();
                         if (matchFound) {
                             // Get all groups for this match
                             wells[0] = Integer.parseInt(m.group(1));
                             plates = Integer.parseInt(m.group(2));
                         } else {
                             returnArr[0] = "false";
                             returnArr[1] = "header line isnt in valid format";
                             return returnArr;
                         }
                     }
                 } else {
                     //normal data lines
                     m = pattern.matcher(line);
                     boolean matchFound = m.find();
                     if (matchFound) {
                         // Get all groups for this match
                         String plate = m.group(1);
                         String well = m.group(2);
                         String content = m.group(3);
 
                         
 
                         //check if the first data line is valid
                         if (lineCnt == 3) {
                             if (!plate.equals("*") && !well.equals("*") && !content.equals("sample")) {
                                 returnArr[0] = "false";
                                 returnArr[1] = "first data line isnt in correct format";
                                 return returnArr;
                             }
                         } else {
                             int plateNum;
                             if (plate.equals("*")) {
                                 plateNum = 0;
                             } else {
                                 plateNum = Integer.parseInt(plate);
                             }
 
                             //we should be strict here and only allow stuff which we can display. I know that one can define own sample names e.g
                             //geneA etc. but we should restrict this for the user here.!
                             if(content.equals("pos")||content.equals("neg")||content.equals("other")||content.equals("empty")||content.equals("sample")) {
                                 //all the other normal datalines
                                 Integer plateNumObj = new Integer(plateNum);
                                 if (!plateList.containsKey(plateNumObj)) {
                                     plateList.put(plateNumObj, new HashMap<String, String>());
                                 }
                                 plateList.get(plateNumObj).put(well, content);
                             }
 
                         }
                     } else {
                         returnArr[0] = "false";
                         returnArr[1] = "error in dataline: " + lineCnt + " " + line;
                         return returnArr;
                     }
 
 
                 }
                 lineCnt++;
             }
             reader.close();
             buffer.close();
         } catch (IOException e) {
             returnArr[0] = "false";
             returnArr[1] = "IO Error";
             return returnArr;
         }
 
         
 
         return returnArr;
     }
 
     /**
      *
      * parse a Platelist file into a special datastructure
      *
      * @param headerPattern parse the platelist file header with this pattern
      * @param bodyPattern  parse all the datalines with this pattern
      * @param file   the file to be parsed
      * @param resultMap the output/result data structure
      * @return returns an array of two elements, first will say true or false (true if everything went fine), second element is an possible error message if the first element is false
      * 
      */
     public static String[] parsePlatelistFile(Pattern headerPattern, Pattern bodyPattern,File file,HashMap<String,Integer[]> resultMap) {
         String[] returnArr = {"true", ""};
 
          try {
                     FileReader reader = new FileReader(file);
                     BufferedReader buffer = new BufferedReader(reader);
 
                     String line;
                     int lineCnt = 0;
                     Matcher m;
                    while ((line = buffer.readLine()) != null) {
                        if(lineCnt++==0) {
                              m=headerPattern.matcher(line);
                              if(!m.find()) {
                                returnArr[0] = "false";
                                returnArr[1] = "header line isnt in valid format";
                                 return returnArr;
                             }
 
                        }
                        else {
                            m=bodyPattern.matcher(line);
                             if(!m.find()) {
                                 returnArr[0] = "false";
                                 returnArr[1] = "error in dataline: " + lineCnt + " " + line;
                                 return returnArr;
                             }
                             else {
                                 try {
                                 String filename = m.group(1);
                                 Integer plate = Integer.parseInt(m.group(2));
                                 Integer replicate = Integer.parseInt(m.group(3));
 
 
                                 Integer channel=null;
                                 if(m.groupCount()==4) {
                                    String channelString = m.group(4);
                                    
                                    if(channelString!=null&&!channelString.equals("")) {
                                        channel = Integer.parseInt(channelString);
                                    }
                                 }                                        
                                 Integer tmpArr[]={plate,replicate,channel};
                                 resultMap.put(filename,tmpArr);
 
                                 }catch(Exception e) {
                                     e.printStackTrace();
                                    returnArr[0] = "false";
                                    returnArr[1] = "error in parsing plate, replicate or channel in line: " + lineCnt + " " + line;
                                    return returnArr;
                                 }
                             }
                        }
 
                    }
 
              reader.close();
             buffer.close();
 
 
 
         } catch (IOException e) {
             returnArr[0] = "false";
             returnArr[1] = "IO Error";
         }
         return returnArr;
     }
 
 
     /**
      *
      * this parses a screenlog file and returns the parsed data and success status
      *
      * @param headerPattern  the header pattern the file will be checked with
      * @param file the file to be parsed
      * @param contData the output datastructure with all the parsed elements
      * @return returns an array of two elements, first will say true or false (true if everything went fine), second element is an possible error message if the first element is false
      */
     public static String[] parseScreenlogFile(Pattern headerPattern,File file,HashMap<Integer,ArrayList<String[]>> contData) {
         String[] returnArr = {"true", ""};          
 
         //loop the file...for every line
         try {
                     FileReader reader = new FileReader(file);
                     BufferedReader buffer = new BufferedReader(reader);
 
                     String line;
 
                     int lineCnt = 0;
                     Matcher m;
                      boolean multiChannel=false;
                     while ((line = buffer.readLine()) != null) {
                         //line zero is the headerline
                         if(lineCnt++==0) {
                              m=headerPattern.matcher(line);
                             if(!m.find()) {
                                returnArr[0] = "false";
                                returnArr[1] = "header line isnt in valid format";
                                 return returnArr;
                             }
                             if(line.split("\t").length==6) {    //singlechannel have 5, multichannel 6 columns (one additionally for channel)
                                 multiChannel=true;
                             }
                         }
                         else {
                                 String tmpArr[]=line.split("\t");
 
 
                                 try {
                                 Integer plateNum = Integer.parseInt(tmpArr[0]);
                                 Integer repNum = Integer.parseInt(tmpArr[1]);
                                 //if double channel
                                 Integer channel=null;
                                 String wellID;
                                 String comment;
 
                                 if(multiChannel) {
                                     channel=Integer.parseInt(tmpArr[2]);
                                     wellID =tmpArr[3];
                                     comment = tmpArr[5];
                                 }
                                 else {
                                     //if single channel we dont have channel information in there
                                     wellID = tmpArr[2];
                                     comment = tmpArr[4];
                                 }
 
 
                                 //init array for plate,sample,channel,well
                                 String tempArr[] = {null,null,null,null};
                                 tempArr[0]=""+repNum;
                                 tempArr[1]=""+channel;
                                 tempArr[2]=wellID;
                                 tempArr[3]=comment;
 
 
                                 //init stuff if not defined
                                 if(!contData.containsKey(plateNum)) {
                                     //create plate if not existend
                                     contData.put(plateNum,new ArrayList<String[]>());
                                 }
                                 contData.get(plateNum).add(tempArr);
 
                                 }catch(NumberFormatException e) {
                                    returnArr[0] = "false";
                                    returnArr[1] = "error in parsing plate or replicate or channel in line: " + lineCnt + " " + line;
                                    return returnArr;
                                 }
                             }
 
 
 
 
                     }
             reader.close();
             buffer.close();
         } catch (IOException e) {
             returnArr[0] = "false";
             returnArr[1] = "IO Error";
             return returnArr;
         }
 
 
          return returnArr;
     }
 
 
     /**
      *
      *  this takes the filename Hashmap and parses the names for every empty Datafile value there will be created a new one
      *  and returns a map from filename to ==> extracted Parameters plateNumber,
      *  repetition,channel
      *  the parameter excludeFilesFromParsing is optional to exclude files from being parsed
      *
      * @param dataFileMap the datastructure with all the filenames, here there will also be the results putted in. You should provide an Hashmap with keys which are the filenames and initialized datafiles. The result will be written into the datafiles values.
      * @param excludeFilesFromParsing which of the filenames (keys of the dataFileMap) should be excluded
      * @param fixRegExp the regular expression you want to parse the filenames with.If none provided some standard regexps will be taken out of the Configuration class
      * 
      */
     public static void parseDataFilenameParams(HashMap<String, DataFile> dataFileMap, HashSet<String> excludeFilesFromParsing, String fixRegExp) {
 
         //iterate over all dataFiles
 
         Iterator dataFileIterator = dataFileMap.keySet().iterator();
         //remember excludeFilesFromParsing is optional
         if (excludeFilesFromParsing == null) {
             excludeFilesFromParsing = new HashSet<String>();
         }
         while (dataFileIterator.hasNext()) {
 
 
             String filename = (String) dataFileIterator.next();
             DataFile dataFile = dataFileMap.get(filename);
             //if we got hits put them into the DataFile obj
             Integer plateNumber = null;
             Integer replicNumber = null;
             Integer channel = null;
 
             //create a new datafile for that file if not edited yet
             if (!excludeFilesFromParsing.contains(filename)) {
                 dataFile.setPlateNumber(null);
                 dataFile.setReplicate(null);
                 dataFile.setChannel(null);                    
             } else {
                 //dont do anything now if we edited this value by our own
                 //because we already edited this
                 continue;
             }
             //if we have set up a fixed regexp to parse our filenames with!!!
             if (!fixRegExp.equals("")) {
                 //i defined a new regexp language for filename parameters ..see manual
                 HashMap<String,String> regExpElements=extractRegExpElements(fixRegExp);
                 if(regExpElements==null) {
                     continue;
                 }
                 if(!regExpElements.containsKey("regExp")) {
                   continue;
                 }
                 String regExp = regExpElements.get("regExp");
                 Pattern regExpPatt = Pattern.compile(regExp);
                 Matcher m = regExpPatt.matcher(filename);
                 boolean matchFound = m.find();
                 boolean errorOccured=false;
                 if (matchFound) {
                     Integer amountHits = new Integer(m.groupCount());
                     for (int i = 1; i <= amountHits; i++) {
                          Integer intObj=null;
                          String stringObj=null;
                          try {
                                 intObj = Integer.parseInt(m.group(i));
 
                             } catch (NumberFormatException e) {
                                //if a exception is thrown we have a string!
                               stringObj = m.group(i);                                
                             }
                          if(stringObj!=null) {
                              if(regExpElements.containsKey(stringObj)) {
                                  intObj = Integer.parseInt(regExpElements.get(stringObj));
                              }
                          }
                         //get the matched element type
                         if(!regExpElements.containsKey(""+i)) {
                             continue;
                         }
                         String element =  regExpElements.get(""+i);
 
                         if(element.equals("p")) {
                            plateNumber = intObj;
                             dataFile.setPlateNumber(plateNumber);
                         }
                         else if (element.equals("r")) {
                             replicNumber = intObj;
                             dataFile.setReplicate(replicNumber);
                         }
                         else if(element.equals("c")) {
                             channel= intObj;
                             dataFile.setChannel(channel);
                         }
                         else {
                             //this is unused right now
                             errorOccured=true;
                             continue;
                         }
                     }
                     
 
                 }
                 else {
                     continue;
                 }
 
             } else {
 
                 //collection to store the pattern with the most found parameters
                 //this is the bruteforce approach
                 //we make a score to find out the pattern which finds the most occurences of single parameters
                 TreeMap<Integer, ArrayList<Pattern>> scorePatternMap = new TreeMap<Integer, ArrayList<Pattern>>();
                 //temp collection to store the results of the single regex application
                 HashMap<Pattern, DataFile> patternDataFileMap = new HashMap<Pattern, DataFile>();
                 //the return container is the input collection (dataFileMap) with the best regex parsed parameters
 
                 //iterate over all valid regex patterns and check each file with it
 
                 HashMap<Pattern, DataFileParameter[]> dataFilePatterns = Configuration.DATAFILE_PATTERNS;
 
                 Iterator dataFileParameterIterator = dataFilePatterns.keySet().iterator();
 
                 while (dataFileParameterIterator.hasNext()) {
                     Pattern pattern = (Pattern) dataFileParameterIterator.next();
                     DataFileParameter[] parameterEnums = dataFilePatterns.get(pattern);
 
                     Matcher m = pattern.matcher(filename);
 
                     boolean matchFound = m.find();
                     if (matchFound) {
 
                         // Get all groups for this match
                         Integer amountHits = new Integer(m.groupCount());
                         if (scorePatternMap.get(amountHits) == null) {
                             scorePatternMap.put(amountHits, new ArrayList<Pattern>());
                         }
                         scorePatternMap.get(amountHits).add(pattern);
 
                         //we start at one because hit 0 is the complete text you scanned
                         for (int i = 1; i <= amountHits; i++) {
                             Integer groupStr = null;
                             try {
                                 groupStr = Integer.parseInt(m.group(i));
                                 //sometimes we have single characters such as a,b,c which will be converted to numbers
                             } catch (NumberFormatException e) {
 
                                 char singleChar = m.group(i).toCharArray()[0];
                                 int asciiNum = (int) singleChar;
                                 //make it case insensitive "a" based
                                 if (asciiNum >= 96) {
                                     groupStr = asciiNum - 96;
                                 }
                                 //else we  are case "A" based
                                 else if (asciiNum >= 64) {
                                     groupStr = asciiNum - 64;
                                 }
                             }
                             //we have a maximum of three parameter
                             DataFileParameter param = parameterEnums[i - 1];
 
                             if (param.equals(DataFileParameter.PLATE)) {
                                 plateNumber = groupStr;
                                 dataFile.setPlateNumber(plateNumber);
                             } else if (param.equals(DataFileParameter.REPLIC)) {
                                 replicNumber = groupStr;
                                 dataFile.setReplicate(replicNumber);
                             } else if (param.equals(DataFileParameter.CHANNEL)) {
                                 channel = groupStr;
                                 dataFile.setChannel(channel);
 
                                 //this is a quick hack for RL,FL ending
                                 if (channel == 6) { //this is char for F
                                     channel = 1;
                                 } else if (channel == 18) {
                                     channel = 2;
                                 }
 
                             }
                         }
                         DataFile tempFile = new DataFile(filename, plateNumber, replicNumber, channel);
                         patternDataFileMap.put(pattern, tempFile);
                     }
 
                     //now get the pattern with the highest score...this should be the biggest element in a TreeSet  (asc order)
                     if (scorePatternMap.size() > 0) {
                         Integer bestScore = scorePatternMap.lastKey();
                         ArrayList<Pattern> bestPatternList = scorePatternMap.get(bestScore);
                         //when all of them are equal score we pick up element zero ..this is a hack because it is unlikely that
                         //we got more than one pattern resulting in the same score/hits
 
                         if (bestScore > 0) {    //if we found anything at all
                             dataFile = patternDataFileMap.get(bestPatternList.get(0));
                             dataFileMap.put(filename, dataFile);
                         }
                     }
 
                 }
             }
         }
 
         //now try to normalize things
         filterZeroBased(dataFileMap, excludeFilesFromParsing);
     }
 
 
     /**
      *
      *  This is mainly a normalization method.
      *  this gives e.g. Replication numbers such as 28 new 1-based numbers, so if we have 28,28,28 on plate 1 they will be 1,2,3
      *  according to  the lowest replication number
      *  this also works for plateNumber >1 and channels
      * 
      * @param dataFileMap
      * @param excludeFilesFromParsing
      */
     private static void filterZeroBased(HashMap<String, DataFile> dataFileMap, HashSet<String> excludeFilesFromParsing) {
         //map the plate Number to the filename keys SORTED
         TreeMap<Integer, ArrayList<String>> plateNumToFilenameMap = new TreeMap<Integer, ArrayList<String>>();
         //map the replication number to the filename SORTED
         TreeMap<Integer, ArrayList<String>> repNumToFilenameMap = new TreeMap<Integer, ArrayList<String>>();
         //map the channel numbers to filename SORTED
         TreeMap<Integer, ArrayList<String>> channelToFilenameMap = new TreeMap<Integer, ArrayList<String>>();
 
         Iterator dataFileMapIterator = dataFileMap.keySet().iterator();
         //get lowest plateNumber...which should be element zero in a treemap ...and make all 1-based with it
 
         while (dataFileMapIterator.hasNext()) {
             String filename = (String) dataFileMapIterator.next();
             DataFile dataFile = dataFileMap.get(filename);
             if (excludeFilesFromParsing.contains(filename)) {
                 //dont do anything if we have already edited this file
                 //by our own
                 continue;
             }
             //store plate numbers
             Integer plateNum = dataFile.getPlateNumber();
             Integer repNum = dataFile.getReplicate();
             Integer channelNum = dataFile.getChannel();
             if (plateNum != null) {
                 if (plateNumToFilenameMap.get(plateNum) == null) {
                     plateNumToFilenameMap.put(plateNum, new ArrayList<String>());
                 }
                 plateNumToFilenameMap.get(plateNum).add(filename);
             }
             if (repNum != null) {
                 if (repNumToFilenameMap.get(repNum) == null) {
                     repNumToFilenameMap.put(repNum, new ArrayList<String>());
                 }
                 repNumToFilenameMap.get(repNum).add(filename);
             }
             if (channelNum != null) {
                 if (channelToFilenameMap.get(channelNum) == null) {
                     channelToFilenameMap.put(channelNum, new ArrayList<String>());
                 }
                 channelToFilenameMap.get(channelNum).add(filename);
             }
 
         }
 
         //for every filename out of every plate
         Iterator plateNumToFilenameIterator = plateNumToFilenameMap.keySet().iterator();
         //if we edited all files firstKey would throw otherwise
         if (!plateNumToFilenameMap.isEmpty()) {
             Integer lowestPlateNumber = plateNumToFilenameMap.firstKey();
 
             while (plateNumToFilenameIterator.hasNext()) {
                 Integer plateNum = (Integer) plateNumToFilenameIterator.next();
                 ArrayList<String> filenames = plateNumToFilenameMap.get(plateNum);
                 for (String filename : filenames) {
                     //make plateNumbers 1-based
                     dataFileMap.get(filename).setPlateNumber(plateNum - lowestPlateNumber + 1);
                     //for every sorted rep Number
                     Iterator repNumToFilenameIterator = repNumToFilenameMap.keySet().iterator();
                     //get the lowest replication number which is element 0 in a treeset
                     if (repNumToFilenameMap.size() > 0) {
                         Integer lowestRepNum = repNumToFilenameMap.firstKey();
                         Integer replicate = dataFileMap.get(filename).getReplicate();
                         if (replicate != null) {
                             dataFileMap.get(filename).setReplicate(replicate - lowestRepNum + 1);
                         }
                     }
                     //get the lowest channel number
                     if (channelToFilenameMap.size() > 0) {
                         Integer lowestChannelNum = channelToFilenameMap.firstKey();
                         Integer channel = dataFileMap.get(filename).getChannel();
                         if (channel != null) {
                             dataFileMap.get(filename).setChannel(channel - lowestChannelNum + 1);
                         }
                     }
 
 
                 }
 
             }
         }
     }
 
     /**
      *
      * this method reads a whole textfile (except the newlines) and returns the content as a String object
      *
      * @param file The inputfile to read from
      * @return the string containing the content of the file
      */
     public static String readFileAsString(File file) {
         String content = "";
         try {
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 
 
             StringBuffer contentOfFile = new StringBuffer();
             String line;
             while ((line = br.readLine()) != null) {
                 contentOfFile.append(line);
             }
             content = contentOfFile.toString() + lineFeed;
 
         }
         catch (IOException e) {
             content = "Error reading the file " + file.getName() + " msg:" + e.getMessage();
         }
         return content;
     }
 
     /**
      *
      * same as readFileAsString() method but inserts newlines at each original line end
      *
      * @param file The inputfile to read from
      * @return the string containing the content of the file
      */
     public static String readFileAsStringWithNewline(File file) {
         String content = "";
         try {
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 
 
             StringBuffer contentOfFile = new StringBuffer();
             String line;
             while ((line = br.readLine()) != null) {
                 content += line + lineFeed;
             }
 
         }
         catch (IOException e) {
             content = "Error reading the file " + file.getName() + " msg:" + e.getMessage();
         }
         return content;
     }
 
     /**
      *
      * this sub parses a description file and stores important data (not all only the most important ones)
      * into an experiment object
      *
      * @param filename the input description file as a File object
      * @param experiment this is the output structure which the input file will be written to
      * @return true if successfully read and stored into the experiment object, false otherwise
      */
     public static boolean parseDescriptionFile(File filename,Experiment experiment) {
         DescriptionParser descParser = new DescriptionParser();
         StringFunctions str = new StringFunctions();
         SimpleDateFormat df = new SimpleDateFormat("mm/dd/yy");
         Map<String, String> descriptionMap;
             try {
                 descriptionMap = descParser.parse(new FileReader(filename));
             } catch (IOException e) {
                 e.getMessage();
                 return false;
             };
 
 
             for(String key : descriptionMap.keySet()) {
                 String value = descriptionMap.get(key);
 
                 Pattern p = Pattern.compile("^experimenter",Pattern.CASE_INSENSITIVE);
                 Matcher m = p.matcher(key);
                 if(m.find()) {
                     experiment.setExperimenter(value);
                     continue;
                 }
                 p = Pattern.compile("^screen$",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                     experiment.setScreen(value);
                     continue;
                 }
                 p = Pattern.compile("^title$",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                    experiment.setTitle(value);
                    continue;
                 }
                 p = Pattern.compile("^date",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                     experiment.setDate(str.parseDate(value, df));
                     continue;
                 }
                 p = Pattern.compile("^celltype",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                     experiment.setCelltype(value);
                     continue;
                 }
 
                 p = Pattern.compile("^assaydesc",Pattern.CASE_INSENSITIVE);
                 Matcher m1 = p.matcher(key);
                 p = Pattern.compile("^assaytype",Pattern.CASE_INSENSITIVE);
                 Matcher m2 = p.matcher(key);
                 p = Pattern.compile("^assay",Pattern.CASE_INSENSITIVE);
                 Matcher m3 = p.matcher(key);
 
                 if(m1.find()) {
                     experiment.setAssaydesc(value);
                     continue;
                 }
                 else if(m2.find()) {
                      experiment.setAssaytype(value);
                     continue;
                 }
                 else if(m3.find()) {
                      experiment.setAssay(value);
                     continue;
                 }
                 p = Pattern.compile("^version",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                     experiment.setVersion(value);
                     continue;
                 }
                 p = Pattern.compile("^channel1",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                     experiment.setChannel1(value);
                     continue;
                 }
                 p = Pattern.compile("^channel2",Pattern.CASE_INSENSITIVE);
                 m = p.matcher(key);
                 if(m.find()) {
                      experiment.setChannel2(value);
                     continue;
                 }
         }
         return true;
     }
     //extract all the neccessary information from a web cellHTS regexp string e.g. /A(\w)-B(\d+)-C(\d+)/c:R=2,L=1;r;p
     /**
      *
      * this method parses
      *
      * @param fullRegExp the regexp as a string representation
      * @return a Hashmap data structure with the extracted plate,channel and replicate information
      */
     public static HashMap<String,String> extractRegExpElements(String fullRegExp) {
          HashMap<String,String> returnMap=new  HashMap<String,String>();
          //a valid string looks like /(\\d+)_(\\w+)_(RL | FL)/p;c;r:RL=1,FL=2  see manual
          int startPos = fullRegExp.indexOf("/");
          if(startPos==-1) {
              return null;
          }
          //find the end
          int endPos = fullRegExp.indexOf("/",startPos+1);
          if(endPos==-1) {
              return null;
          }
          String regEx = fullRegExp.substring(startPos+1,endPos);  //extract the string without the /'s
          String definitions = fullRegExp.substring(endPos+1,fullRegExp.length());
         //put in the return map the regEx
         returnMap.put("regExp",regEx);
 
 
        //now process the definitions
          String [] tokens = definitions.split(";");
          Pattern p1 = Pattern.compile("[pPcCrR]");
          Pattern p2 = Pattern.compile(":");
          int counter=0;
          for(String token : tokens) {
              counter++;
              Matcher m1  =p1.matcher(token);
              //only p,c,r are allowed
              if(!m1.find()) {
                 return null;
              }
              //kill whitespaces
              token=token.replace(" ","");
              
 
                           
              //some have advanced options which map alphanumeric to numeric RL=1,FL=2
              Matcher m2 = p2.matcher(token);
              if(m2.find()) {
                  String []splitTokens =  token.split(":");
                  token = splitTokens[0];
                  String mapString = splitTokens[1];
                  if(mapString==null) {
                      return null;
                  }
                  String []mapTokens = mapString.split(",");
                  if(mapTokens==null) {
                      return null;
                  }
                  //e.g. RA=1,RB=2....
                  for(String singleMap : mapTokens) {
                      String [] keyMap = singleMap.split("=");
                      if(keyMap==null) {
                          return null;
                      }
                      if(keyMap.length!=2) {
                          return null;
                      }
                      //TODO:put this in the same return Hashmap ...I known this is not the best data seperated way of doing it
                      try {
                          //test if we could convert the value to an int
                          int test = Integer.parseInt(keyMap[1]);
                      } catch (NumberFormatException e) {
                          return null;
                      }
 
                      returnMap.put(keyMap[0],keyMap[1]);
                  }
 
              }
              //put in position of p,r,c
              returnMap.put(""+counter,token);
 
          }
         return returnMap;
     }
 
     /**
      * simple setter for setting OS dependent newline symbol
      * @param lineFeeder  newline symbol e.g. \n or \r\n  etc.
      */
     public static void setLineFeed(String lineFeeder) {
         lineFeed = lineFeeder;
     }
     
     /**
      *
      * reads a properties file and extracts the property job ID
      * this method is deprecated, we will use Java standard api's properties class instead
      *
      *
      * @param file the properties input file
      * @param jobID the jobID from which you want to get the value, eg. in the properties file file there is a line JOBID12345 = blablabla  so you will provide "JOBUD12345" and you would receive blablabla
      * @return returns the value of the key jobID in the properties file file
      */
     public static Integer readAmountFromDownloadPropertiesFile(File file,String jobID) {
         Integer returnVal=null;
         try {
                     FileReader reader = new FileReader(file);
                     BufferedReader buffer = new BufferedReader(reader);
 
                     String line;
                     
 
                     while ((line = buffer.readLine()) != null) {
                         line = line.replaceAll(" ","");
                         String []splitLine = line.split("=");
                         if(splitLine[0].equals(jobID)) {
                             returnVal=Integer.parseInt(splitLine[1]);
                             break;
                         }
                     }
 
 
             buffer.close();
             reader.close();
         } catch(IOException e) {
               e.printStackTrace();
               
         }
 
         return returnVal;                    
     }
     public static int countNumberOfLinesForFile(File inputfile) {
        try {
        if(!inputfile.exists())   {
            return 0;
        }
 //       BufferedReader bf = new BufferedReader(new FileReader(inputfile));
        FileReader fr = new FileReader(inputfile);
 
        LineNumberReader ln = new LineNumberReader(fr);
 
       int count = 0;
 
         while (ln.readLine() != null){
 
           count++;
 
 
         }
 
 
 
         ln.close();
 
         return count;
        } catch(IOException e) {
                      e.printStackTrace();
            return 0;
 
        }
 
     }
 
     }
