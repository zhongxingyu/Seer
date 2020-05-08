 package com.wedlum.styleprofile.domain.photo;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class PhotoTest {
 
     @Test
     public void invalidMetadata(){
         try {
            Photo subject = new Photo("myPhoto.png", "garbage");
             Assert.fail();
         } catch (IllegalStateException ex){
            Assert.assertEquals("Invalid: invalid metadata: Root tag 'Photo:' not found.", ex.getMessage());
         }
 
     }
 }
