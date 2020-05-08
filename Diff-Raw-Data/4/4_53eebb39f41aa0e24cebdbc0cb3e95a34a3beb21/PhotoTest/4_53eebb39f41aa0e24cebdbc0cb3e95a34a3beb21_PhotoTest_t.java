 import org.junit.Test;
 
 import java.io.File;
 
 import static org.junit.Assert.assertEquals;
 
 public class PhotoTest {
     @Test(expected = IllegalArgumentException.class)
     public void shouldRejectInvalidBaseUrl() {
         new Photo(new File(""), "hi!");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void shouldRejectMissingTrailingSlash() {
         new Photo(new File(""), "http://missing-trailing.com");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void shouldRejectNonJpg() {
         new Photo(new File("/some/album/photo.tiff"), "http://domain.com/");
     }
 
     @Test
     public void shouldKnowItsPaths() {
         Photo photo = new Photo(new File("/some/where/albumDirectory/photo.jpg"), "https://mon.bucket.com/");
 
        assertEquals(photo.getKey(), "albumDirectory/photo.jpg");
        assertEquals(photo.getUrl(), "https://mon.bucket.com/albumDirectory/photo.jpg");
     }
 }
