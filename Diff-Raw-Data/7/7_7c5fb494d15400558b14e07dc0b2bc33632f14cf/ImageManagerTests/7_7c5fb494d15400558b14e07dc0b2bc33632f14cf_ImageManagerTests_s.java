 package com.ece.superkids.testing;
 
 import com.ece.superkids.FileManager;
 import com.ece.superkids.FileManagerImpl;
 import com.ece.superkids.images.ImageManager;
 import com.ece.superkids.questions.QuestionDatabaseFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.*;
 import java.util.Properties;
 
 import static org.junit.Assert.assertTrue;
 
 public class ImageManagerTests {
 
     private static final String IMAGE_NAME = "testelephant";
 
     private File customImage;
     private ImageManager imageManager;
     private FileManager fileManager;
     private String key;
 
     @Before
     public void setup() throws IOException {
         customImage = new File(getClass().getResource("/elephant.png").getFile());
         imageManager = QuestionDatabaseFactory.anImageManager();
         fileManager = FileManagerImpl.getInstance();
     }
 
     @After
     public void clean() throws IOException {
         deleteImageFile();
     }
 
     private void deleteImageFile() throws IOException {
         Properties props = new Properties();
         props.load(new FileInputStream(fileManager.getImagePathsFile()));
         String imagePath = props.getProperty(key);
        File image = new File(imagePath);
        image.delete();
 
         props.remove(key);
         props.store(new FileWriter(fileManager.getImagePathsFile()), "Custom Images");
     }
 
     @Test
     public void canUploadCustomImage() throws IOException {
         key = imageManager.saveImage(customImage.getPath(), IMAGE_NAME);
 
         Properties props = new Properties();
         props.load(new FileInputStream(fileManager.getImagePathsFile()));
         String imagePath = props.getProperty(key);
         File image = new File(imagePath);
         assertTrue(image.exists());
     }
 }
