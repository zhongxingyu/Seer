 package ru.urbancamper.audiobookmarker.text.markerplacer;
 
 import ru.urbancamper.audiobookmarker.text.formats.BareTextToDocumentMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pozpl
  * Date: 24.05.13
  * Time: 7:51
  * This class places markers for text file format
  */
 public class Text implements IMarkersPlacer{
 
     /**
      *
      * @param fullTextAudioMark  Data for audio markers
      * @param bareText  tokenized text of an document
      * @param document  Document as is in text form
      * @return Document copy with audio marks
      */
     @Override
     public String produceDocumentWithMarkers(FullTextAudioMark fullTextAudioMark, String[] bareText, String document){
         Integer documentOffset = 0;
         Integer docLastCharacterIndex = document.length() - 1;
 
 
         String markedDocument = "";
         Integer previousMarkerIndex = 0;
         for(Integer bareTextWordIndex = 0; bareTextWordIndex < bareText.length; bareTextWordIndex++){
             String bareTextWord = bareText[bareTextWordIndex];
             Integer bareWordIndexInDoc = document.indexOf(bareTextWord, documentOffset);
 
             String marker = "";
             if(fullTextAudioMark.isMarkerDefinedForFullTextIndex(bareTextWordIndex)){
                 Double beginTime = fullTextAudioMark.getBeginTime(bareTextWordIndex);
                 Double endTime = fullTextAudioMark.getEndTime(bareTextWordIndex);
                 Integer audioFileIndex = fullTextAudioMark.getFileIndex(bareTextWordIndex);
                 marker = this.constructMarker(beginTime, endTime, audioFileIndex);
             }
 
             String markedTextBit = document.substring(previousMarkerIndex, bareWordIndexInDoc);
             markedDocument += markedTextBit + marker;
             previousMarkerIndex = bareWordIndexInDoc;
             documentOffset = bareWordIndexInDoc + bareTextWord.length() - 1;
 
             if(documentOffset >= docLastCharacterIndex){
                 String lastTextBit = document.substring(previousMarkerIndex, docLastCharacterIndex + 1);
                 markedDocument += lastTextBit;
                 break;
             }
         }
         return markedDocument;
     }
 
     /**
      * Function to construct marker
      * @param beginTime  Time of word begin
      * @param endTime   Time of word end
      * @param fileIndex Index of file
      * @return  Marker string
      */
     private String constructMarker(Double beginTime, Double endTime, Integer fileIndex){
         return "<" + fileIndex + ":" + beginTime + ">";
     }
 }
