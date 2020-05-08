 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerInterface;
import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerSphinxCached;
 import ru.urbancamper.audiobookmarker.document.MarkedDocument;
 import ru.urbancamper.audiobookmarker.text.BookText;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public class AudioBookMarkerUtil {
 
     private BookText bookTextAggregator;
 
     private AudioFileRecognizerInterface audioRecognizer;
 
     /**
      *
      * @param bookText
      * @param audioRecognizer
      */
     public AudioBookMarkerUtil(BookText bookText, AudioFileRecognizerInterface audioRecognizer){
         this.bookTextAggregator = bookText;
         this.audioRecognizer = audioRecognizer;
     }
 
     protected final Log logger = LogFactory.getLog(getClass());
 
 
     private String[] getAudioFilesPaths(String directoryPath){
         File directory = new File(directoryPath);
         File[] filesInDir = directory.listFiles();
         ArrayList<String> filePathsList = new ArrayList<String>();
         for(File file: filesInDir){
             filePathsList.add(file.getAbsolutePath());
         }
         String[] filePathsArray = filePathsList.toArray(new String[filePathsList.size()]);
         return filePathsArray;
     }
 
     private String getBookFullText(String txtFilePath){
         StringBuilder strBuffer = new StringBuilder();
         int BLOC_SIZE = 512;
         char[] b = new char[BLOC_SIZE];
         Reader fileReader;
         try {
             File txtFile = new File(txtFilePath);
             if(txtFile.exists()){
                 fileReader = new FileReader(txtFile);
                 int n;
                 while((n = fileReader.read(b))>0){
                     strBuffer.append(b, 0, n);
                 }
             }else{
                 this.logger.error("File is not exists" + txtFilePath);
             }
         } catch (IOException ex) {
             this.logger.error("Excsption during file read " + txtFilePath + ": " + ex);
         }
         String retText = strBuffer.toString();
         return retText;
     }
 
     public MarkedDocument makeMarkers(String audioBookDir, String bookFilePath){
 
         String[] audioFilePaths = this.getAudioFilesPaths(audioBookDir);
         String bookFullText = this.getBookFullText(bookFilePath);
 
         MarkedDocument markedDocument = this.makeMarkers(audioFilePaths, bookFullText);
 
         return markedDocument;
     }
 
 
     /**
      * This function gets an array of file paths and returns market dokument
      *
      * @param audioBookFilesPaths
      * @param fullText
      * @return
      */
     public MarkedDocument makeMarkers(String[] audioBookFilesPaths, String fullText){
         this.bookTextAggregator.setFullText(fullText);
         HashMap<String, String> audioFilesIdentificatorMap = new HashMap<String, String>();
         String audioFilePath;
         String fileName;
         for(Integer fileCounter = 0; fileCounter < audioBookFilesPaths.length; fileCounter++){
             audioFilePath = audioBookFilesPaths[fileCounter];
             fileName = this.getAudioFileName(audioFilePath);
             audioFilesIdentificatorMap.put(fileName, fileCounter.toString());
             RecognizedTextOfSingleAudiofile recognizedFile = this.audioRecognizer.recognize(audioFilePath, fileCounter.toString());
             this.bookTextAggregator.registerRecognizedTextPiece(recognizedFile);
         }
         String markedText = this.bookTextAggregator.buildTextWithAudioMarks();
         MarkedDocument markedDokument = new MarkedDocument(markedText, audioFilesIdentificatorMap);
         return markedDokument;
     }
 
     private String getAudioFileName(String audioFilePath){
         String fileName;
         fileName = new File(audioFilePath).getName();
         return fileName;
     }
 
     private Boolean isMarkedTextFileExists(String filePath){
         File cachedFile = new File(filePath);
         if(cachedFile.exists()){
             return Boolean.TRUE;
         }else{
             return Boolean.FALSE;
         }
     }
 
     public Boolean writeMarkedTextToFile(String filePath, String textToWrite){
         BufferedWriter bWriter = null;
         try {
             File cacheFile = new File(filePath);
             bWriter = new BufferedWriter(new FileWriter(cacheFile));
             bWriter.write(textToWrite);
         } catch (IOException ex) {
             this.logger.error("Error during writing process to file " + filePath);
         } finally {
             try {
                 bWriter.close();
             } catch (IOException ex) {
                 this.logger.error("Can not close file " + filePath);
             }
         }
         return Boolean.TRUE;
     }
 
 }
