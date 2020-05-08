 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.audio;
 
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public interface AudioFileRecognizerInterface {
 
     public RecognizedTextOfSingleAudiofile recognize(String filePath, String unicFileIdentifier);
     /**
      *
      * @param audioFilePath
      * @return
      */
    public String getTextFromAudioFile(String audioFilePath);
 }
