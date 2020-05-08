 package de.htwg.se.battleship;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.Test;
 
 import de.htwg.se.battleship.controller.commands.CloseGame;
 
 public class BattleshipTest {
 
     @Test
     public void test() {
 
         Battleship object1 = Battleship.getInstance();
         Battleship object2 = Battleship.getInstance();
 
         assertEquals(object1, object2);
 
         CloseGame close = new CloseGame();
         InputStream oldIn = System.in;
        String s = close.getCommand() + System.getProperty("line.separator");
 
         try {
             System.setIn(new ByteArrayInputStream(s.getBytes("UTF-8")));
         } catch (UnsupportedEncodingException e) {
             System.setIn(oldIn);
             fail(e.getMessage());
         }
 
         Battleship.main(new String[0]);
 
         System.setIn(oldIn);
     }
 
 }
