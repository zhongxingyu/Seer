 package nu.mrpi.wordfeudapi.domain;
 
 import com.google.gson.Gson;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Pierre Ingmansson
  */
 public class GameTest {
     @Test
    public void testSerialization() {
         Gson gson = new Gson();
 
         try {
             String json = getContents(new File(Thread.currentThread().getContextClassLoader().getResource("onegame.json").toURI()));
 
            Game game = gson.fromJson(json, Game.class);
             assertEquals(170684861, game.getId());
             assertEquals(76, game.getBagCount());
             assertEquals(14, game.getTiles().length);
             assertEquals(11, game.getTiles()[0].getX());
             assertEquals(2, game.getTiles()[0].getY());
             assertEquals('A', game.getTiles()[0].getCharacter());
             assertEquals(false, game.getTiles()[0].isWildcard());
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
 
     }
 
     static public String getContents(File aFile) {
         //...checks on aFile are elided
         StringBuilder contents = new StringBuilder();
 
         try {
             //use buffering, reading one line at a time
             //FileReader always assumes default encoding is OK!
             BufferedReader input = new BufferedReader(new FileReader(aFile));
             try {
                 String line = null; //not declared within while loop
                 /*
                 * readLine is a bit quirky :
                 * it returns the content of a line MINUS the newline.
                 * it returns null only for the END of the stream.
                 * it returns an empty String if two newlines appear in a row.
                 */
                 while ((line = input.readLine()) != null) {
                     contents.append(line);
                     contents.append(System.getProperty("line.separator"));
                 }
             } finally {
                 input.close();
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         return contents.toString();
     }
 }
