 package com.wedlum.styleprofile.domain.photo;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class PhotoTest {
 
     @Test
     public void invalidMetadata(){
        Photo subject = new Photo("myPhoto.png", "garbage");
         try {
            subject.getColors();
             Assert.fail();
         } catch (IllegalStateException ex){
            Assert.assertEquals("myPhoto.png: invalid metadata: Root tag 'Photo:' not found.", ex.getMessage());
         }
 
     }
 }
