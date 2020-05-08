 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.context;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import opennlp.tools.tokenize.TokenizerModel;
 import org.springframework.context.annotation.Bean;
 import ru.urbancamper.audiobookmarker.text.BookText;
 import ru.urbancamper.audiobookmarker.text.LongestSubsequenceFinder;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 import ru.urbancamper.audiobookmarker.text.LanguageModelBasedTextTokenizez;
 import ru.urbancamper.audiobookmarker.text.WordsToNumsMap;
 
 /**
  *
  * @author pozpl
  */
 public class BeansAnnotationsForTests {
 
    private String modelPath = "resources/tokenizer_models/en-token.bin";
 
     @Bean
     public TokenizerModel tokenizerModel(){
         InputStream modelPathInputStream = null;
         TokenizerModel tokenizerModel = null;
         try {
            File modelFile = new File(modelPath);
             modelPathInputStream = new FileInputStream(modelFile.getAbsolutePath());
             tokenizerModel = new TokenizerModel(modelPathInputStream);
         } catch (IOException ex) {
             Logger.getLogger(LanguageModelBasedTextTokenizez.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 modelPathInputStream.close();
             } catch (IOException ex) {
                 Logger.getLogger(LanguageModelBasedTextTokenizez.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return tokenizerModel;
     }
 
     @Bean
     public LanguageModelBasedTextTokenizez textTokenizer(){
        return new LanguageModelBasedTextTokenizez(tokenizerModel());
     }
 
     @Bean
     public WordsToNumsMap wordsToNumMap(){
         return new WordsToNumsMap();
     }
 
     @Bean
     public LongestSubsequenceFinder longestSubsequenceFinder(){
         return new LongestSubsequenceFinder();
     }
 
     @Bean
     public BookText bookText(){
         return new BookText(this.textTokenizer(), this.wordsToNumMap(), this.longestSubsequenceFinder());
     }
 
 //    @Bean
 //    public RecognizedTextOfSingleAudiofile recognizedTextOfSingleAudiofile(){
 //        modelPath
 //        RecognizedTextOfSingleAudiofile recText = new RecognizedTextOfSingleAudiofile(modelPath, modelPath)
 //        return null;
 //    }
 }
