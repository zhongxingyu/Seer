 /**
  *
  */
 package imageGetters;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import static org.junit.Assert.*;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import static org.hamcrest.CoreMatchers.*;
 import model.Picture;
 
 import org.junit.Test;
 
 public class TwitterParserTests {
 
     @Test
     public void Parse_GivenJsonWith2Pictures_ReturnsListWith2Pictures() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         ArrayList<Picture> pictureList = new ArrayList<>();
         TwitterParser parser = new TwitterParser();
         JsonArray jsonPictures = parser.parse(ir);
 
         for (JsonElement j : jsonPictures) {
             Picture picture = parser.addToList(j);
             pictureList.add(picture);
         }
 
         assertThat(pictureList.size(), is(2));
     }
 
     @Test
     public void Parse_GivenValidJson_FirstPictureHasThumbUrl() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         ArrayList<Picture> pictureList = new ArrayList<>();
         TwitterParser parser = new TwitterParser();
         JsonArray jsonPictures = parser.parse(ir);
 
         for (JsonElement j : jsonPictures) {
             Picture picture = parser.addToList(j);
             pictureList.add(picture);
         }
 
         assertThat(pictureList.get(0).getThumbUrl(), is(not(nullValue())));
     }
 
     @Test
     public void Parse_GivenValidJson_FirstPictureHasLargeUrl() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         ArrayList<Picture> pictureList = new ArrayList<>();
         TwitterParser parser = new TwitterParser();
         JsonArray jsonPictures = parser.parse(ir);
 
         for (JsonElement j : jsonPictures) {
             Picture picture = parser.addToList(j);
             pictureList.add(picture);
         }
 
         assertThat(pictureList.get(0).getLargeUrl(), is(not(nullValue())));
     }
 
     @Test
     public void Parse_GivenValidJson_FirstPictureHasUnixDate() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         ArrayList<Picture> pictureList = new ArrayList<>();
         TwitterParser parser = new TwitterParser();
         JsonArray jsonPictures = parser.parse(ir);
 
         for (JsonElement j : jsonPictures) {
             Picture picture = parser.addToList(j);
             pictureList.add(picture);
         }
 
         assertThat(pictureList.get(0).getUnixDate(), is(not(nullValue())));
     }
 
     @Test
     public void Parse_GivenValidJson_FirstPictureHasID() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         ArrayList<Picture> pictureList = new ArrayList<>();
         TwitterParser parser = new TwitterParser();
         JsonArray jsonPictures = parser.parse(ir);
 
         for (JsonElement j : jsonPictures) {
             Picture picture = parser.addToList(j);
             pictureList.add(picture);
         }
 
         assertThat(pictureList.get(0).getId(), is(not(nullValue())));
     }
 
     @Test
     public void Parse_GivenValidJson_FindsNextURL() throws FileNotFoundException, UnsupportedEncodingException {
         InputStream fis = getClass().getResourceAsStream("./TwitterJsonSampleData.json");
         InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
 
         TwitterParser parser = new TwitterParser();
         parser.parse(ir);
         String next_url = parser.getNextUrl();
 
         assertThat(next_url, is(not(nullValue())));
     }
 }
