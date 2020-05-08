 package controllers;
 
import com.google.common.collect.Maps;
import models.ImageInfo;
 import models.domain.Embed;
 import models.domain.Speaker;
 import models.domain.Talk;
 import models.domain.Thumbnail;
 import org.junit.Test;
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
import java.util.HashMap;

 public class ApplicationTest extends FunctionalTest {
 
     @Test
     public void testThatIndexPageReturns200() {
         Response response = GET("/");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
     public void talkPageResponds200ForValidVideo() {
         Talk talk = new Talk();
         talk.id(123);
         talk.thumbnail(new Thumbnail("", 0, 0));
         talk.embed(new Embed(""));
         talk.save();
 
         Response response = GET("/talk/123");
         assertIsOk(response);
         assertContentType("text/html", response);
 
         talk.delete();
     }
 
     @Test
     public void talkPageResponds404ForInvalidVideo() {
         Response response = GET("/talk/0");
         assertIsNotFound(response);
     }
 
     @Test
     public void talkPageResponds200ForValidSpeaker() {
         Speaker speaker = new Speaker("test", "bio", "url");
        HashMap images = Maps.newHashMap();
        images.put("small", new ImageInfo("url", 200, 100, 2011));
        speaker.images(images);
         speaker.save();
 
         Response response = GET("/speaker/test");
         assertIsOk(response);
         assertContentType("text/html", response);
 
         speaker.delete();
     }
 
     @Test
     public void talkPageResponds404ForInvalidSpeaker() {
         Response response = GET("/speaker/foo");
         assertIsNotFound(response);
     }
 
     @Test
    public void talkFilterPageResponds200ForValidYear() {
         Response response = GET("/talks/2011");
         assertIsOk(response);
         assertContentType("text/html", response);
     }
                                                                      
    @Test
    public void talkFilterPageResponds404ForInvalidYear() {
        Response response = GET("/talks/2007");
        assertEquals(new Integer(404), response.status);
    }


     
 }
