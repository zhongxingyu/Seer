 package com.github.dreamrec;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class ControllerTest {
 
     File file;
 
     @Before
     public void setup(){
          file = new File("tralivali");
     }
 
     @After
     public void tearDown(){
          file.delete();
     }
 
     @Test
     public void saveToFileTest(){
         long startTime = System.currentTimeMillis();
         Model model = new Model(13.3,startTime);
         Controller controller = new Controller(model);
         for (int i = 0; i < 10000; i++) {
               model.addEyeData(i);
         }
         controller.saveToFile(file);
         controller.readFromFile(file);
         Model restoredModel = controller.getModel();
         assertTrue(model.getFrequency() == restoredModel.getFrequency());
         assertTrue(model.getStartTime() == restoredModel.getStartTime());
        assertTrue(model.getEyeDataList().size() == restoredModel.getEyeDataList().size());
         for (int i = 0; i < model.getEyeDataList().size(); i++) {
              assertTrue(model.getEyeDataList().get(i).equals(restoredModel.getEyeDataList().get(i)));
         }
     }
 }
