 //=============================================================================
 // Torrganizer
 //
 //             Author:  James Mack
 // Project Start Date:  2013-02-04
 //
 //=============================================================================
 
 //===============================
 // Imports
 //===============================
 
 // Library
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Vector;
 import java.util.Scanner;
 
 // Classes
 //import TFile;
 //import MovieFile;
 //import ShowFile;
 
 
 public class Torrganizer
 {
     //===============================================================
     //      Constants
     //===============================================================
 
    private static boolean DEBUG = true;
    
     // Directories
     private static String DOWNLOAD_PATH = "/home/james/Torrganizer/files";
     private static String SORT_PATH     = "/home/james/Torrganizer/Sort";
 
     // Regex
    private static final String REGEX_EXTENSION = "(?i).*\\.([a-zA-Z0-9]{3,4})$";
    private static final String REGEX_SHOW1     = "(?i)([a-zA-Z0-9\\.\\ ]+)S(\\d\\d)E(\\d\\d).*\\.(mp4|mpeg4|avi|mkv|wmv|mov)";
    private static final String REGEX_SHOW2     = "(?i)([a-zA-Z0-9\\.\\ ]+)(\\d\\d\\d).*\\.(mp4|mpeg4|avi|mkv|wmv|mov)";
    private static final String REGEX_MOVIE     = "(?i)([a-zA-Z0-9\\.\\ ]+).*[\\(|\\]]?(19|20\\d\\d)[\\(|\\]]?.*\\.(mp4|mpeg4|avi|mkv|wmv|mov)";

    /*
        ([a-zA-Z0-9\.\ ]+)S(\d\d)E(\d\d).*\.(mp4|mpeg4|avi|mkv|wmv|mov)
        ([a-zA-Z0-9\.\ ]+)(\d\d\d).*\.(mp4|mpeg4|avi|mkv|wmv|mov)
    */
 
     //===============================================================
     //      Globals
     //===============================================================
     private static CLInterface userInterface;
 
     //===============================================================
     //      main
     //===============================================================
 
     public static void main(String [] args)
     {
         userInterface = new CLInterface();
         String inputStr;
 
         inputStr = userInterface.getDownloadDirectory();
         if(!inputStr.equals(""))
             DOWNLOAD_PATH = inputStr;
         userInterface.outputDownloadDirectory(DOWNLOAD_PATH);
 
         inputStr  = userInterface.getDestinationDirectory();
         if(!inputStr.equals(""))
             SORT_PATH = inputStr;
         userInterface.outputDestinationDirectory(SORT_PATH);
 
         File homeDir = new File(DOWNLOAD_PATH);
 
         Vector<TFile> allFiles = new Vector<TFile>();
 
         allFiles = processDirectory(homeDir);
     }
 
     public static Vector<TFile> processDirectory(File thisDir)
     {
         userInterface.outputProcessingDirectory(thisDir.getName()); 
 
         File [] files = thisDir.listFiles();
 
         if(files.length <= 0) {
             userInterface.outputDirectoryEmpty(thisDir.getName());
             return new Vector<TFile>();
         }
         else {
             boolean proceed = userInterface.getDirectoryProceed();
 
             if(!proceed)
                 return new Vector<TFile>();
         }
 
         Vector<TFile> allFiles = new Vector<TFile>();
 
         for(File thisFile: files) {
             allFiles.addAll(processFile(thisFile));
         }
 
         return allFiles;
     }
 
 
     //===============================================================
     //      processFile
     //===============================================================
 
     private static Vector<TFile> processFile(File thisFile)
     {
         // if directory, enter directory
         if(thisFile.isDirectory()) {
             return processDirectory(thisFile);
         }
 
         userInterface.outputProcessingFile(thisFile.getName());
 
         // match and create TFile representation   
         TFile thisTFile = matchFile(thisFile);
         if(thisTFile == null)
             return new Vector<TFile>();
 
         // rename and move file
         String newPath = SORT_PATH;
         String newAbsoluteName = "";
 
         if(thisTFile.isShow()) {
             newPath = newPath + "/TV/" + ((ShowFile) thisTFile).getShowTitle() + "/" + ((ShowFile) thisTFile).getShowTitle() + " - Season " + ((ShowFile) thisTFile).getSeasonNum();
             newAbsoluteName = newPath + "/" + thisTFile.getFileName();
         }
 
         else if(thisTFile.isMovie()) {
             newPath = newPath + "/Movies";
             newAbsoluteName = newPath + "/" + thisTFile.getFileName();
         }
 
         else {
            String inputString = userInterface.getMoveToPath(SORT_PATH, thisTFile.getFileName());
 
             newPath = newPath + "/" + inputString;
             newAbsoluteName = newPath + "/" + thisTFile.getFileName();
         }
 
         File dir = new File(newPath);
         if(!dir.exists()) {
             userInterface.outputDirectoryNotExist(dir.getName());
 
             boolean dirResult = dir.mkdirs();
 
             if(dirResult) 
                 userInterface.outputDirectoryCreateSuccess(dir.getName());
             else
                 userInterface.outputDirectoryCreateError(dir.getName());
 
         }
 
         File newFile = new File(newAbsoluteName);
         userInterface.outputFileAttemptRename(newFile.getPath());
         boolean result = thisTFile.getFile().renameTo(newFile);
 
         if(result) {
             userInterface.outputFileRenameSuccess();
             thisTFile.setFile(newFile);
         }
         else {
             // TODO: Should probably do something about error here
             userInterface.outputFileRenameError();
         }
     
         Vector<TFile> returnVector = new Vector<TFile>();
         returnVector.addElement(thisTFile);
 
         return returnVector;
     }    
 
     //===============================================================
     //      matchFile
     //===============================================================
 
     private static TFile matchFile(File thisFile)
     {
        Pattern extensionPattern = Pattern.compile(REGEX_EXTENSION);
         Matcher extensionMatcher = extensionPattern.matcher(thisFile.getName());
 
        Pattern show1Pattern = Pattern.compile(REGEX_SHOW1);
         Matcher show1Matcher = show1Pattern.matcher(thisFile.getName());
 
        Pattern show2Pattern = Pattern.compile(REGEX_SHOW2);
         Matcher show2Matcher = show2Pattern.matcher(thisFile.getName());
 
        Pattern moviePattern = Pattern.compile(REGEX_MOVIE);
         Matcher movieMatcher = moviePattern.matcher(thisFile.getName());
 
         TFile newFile = null;
     
         // Show1
         if(show1Matcher.matches()) {
             String title = show1Matcher.group(1).replace(".", " ");
             title = title.replace("  ", " ");
             title = title.trim();
 
             int season = Integer.parseInt(show1Matcher.group(2));
             int episode = Integer.parseInt(show1Matcher.group(3));
 
             String extension = show1Matcher.group(4);
             String name = title + " - S" + String.format("%02d", season) + "E" + String.format("%02d", episode) + "." + extension;
 
             newFile = new ShowFile(thisFile, name, extension, season, episode, title);
         }
 
         // Show1
         else if(show2Matcher.matches()) {
             String title = show2Matcher.group(1).replace(".", " ");
             title = title.replace("  ", " ");
             title = title.trim();
 
             int episodeNumber = Integer.parseInt(show2Matcher.group(2));
             int episode = episodeNumber % 100;
             int season = (episodeNumber / 100) % 10;
 
             String extension = show2Matcher.group(3);
             String name = title + " - S" + String.format("%02d", season) + "E" + String.format("%02d", episode) + "." + extension;
 
             newFile = new ShowFile(thisFile, name, extension, season, episode, title);
         }
 
         // Movie
         else if(movieMatcher.matches()) {
             String title = movieMatcher.group(1).replace(".", " ");
             title = title.replace("  ", " ");
             title = title.trim();
 
             int year = Integer.parseInt(movieMatcher.group(2));
 
             String extension = movieMatcher.group(3);
             String name = title + " (" + String.format("%4d", year) + ")." + extension;
 
             newFile = new MovieFile(thisFile, name, extension, year);
         }
 
         else if(extensionMatcher.matches()) {
             String extension = extensionMatcher.group(1);
             userInterface.outputNoMatch(extension);
 
             int choice = userInterface.getProcessOption();
 
             String title, name;
 
             switch(choice) {
                 case 1:
                     title = userInterface.getShowTitle();
 
                     int season = userInterface.getShowSeason();
 
                     int episode = userInterface.getShowEpisode();
 
                     name = title + " - S" + String.format("%02d", season) + "E" + String.format("%02d", episode) + "." + extension;
 
                     newFile = new ShowFile(thisFile, name, extension, season, episode, title);
                     break;
 
                 case 2:
                     title = userInterface.getMovieTitle();
 
                     int year = userInterface.getMovieYear();
 
                     name = title + " (" + String.format("%4d", year) + ")." + extension;
 
                     newFile = new MovieFile(thisFile, name, extension, year);
                     break;
 
                 case 3:
                     name = userInterface.getFileName();
                     name = name + "." + extension;
 
                     newFile = new TFile(thisFile, name, extension);
                     break;
 
                 case 4:
 
                 default:
                     userInterface.outputSkipFile();
                     return null;
             }
         }
 
         else {
             userInterface.outputProcessError();
             return null;
         }
 
         return newFile;
 
     }    
 }
