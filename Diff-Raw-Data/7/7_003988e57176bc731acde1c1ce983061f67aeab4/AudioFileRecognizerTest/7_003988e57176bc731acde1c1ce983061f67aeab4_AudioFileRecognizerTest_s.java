 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmaker.audio;
 
 import java.io.File;
 import junit.framework.TestCase;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public class AudioFileRecognizerTest extends TestCase {
 
     public AudioFileRecognizerTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of recognize method, of class AudioFileRecognizer.
      */
     public void testRecognize_String() {
         System.out.println("recognize");
         String filePath = "";
         AudioFileRecognizer instance = new AudioFileRecognizer();
         RecognizedTextOfSingleAudiofile expResult = null;
         RecognizedTextOfSingleAudiofile result = instance.recognize(filePath);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 
     /**
      * Test of recognize method, of class AudioFileRecognizer.
      */
     public void testRecognize_File() {
         System.out.println("recognize");
         File file = null;
         AudioFileRecognizer instance = new AudioFileRecognizer();
         RecognizedTextOfSingleAudiofile expResult = null;
         RecognizedTextOfSingleAudiofile result = instance.recognize(file);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 }
