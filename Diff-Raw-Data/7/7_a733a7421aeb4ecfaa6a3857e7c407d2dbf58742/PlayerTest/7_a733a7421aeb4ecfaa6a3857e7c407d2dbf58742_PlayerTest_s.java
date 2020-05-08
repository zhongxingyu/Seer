 package de.htwg.wzzrd.test;
 
import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.wzzrd.entities.Card;
 import de.htwg.wzzrd.entities.Player;
 import de.htwg.wzzrd.entities.ServerPlayer;
 import de.htwg.wzzrd.model.Settings;
 
 public class PlayerTest {
     Player p;
     ServerPlayer sp;
 
     @Before
     public void setUp() {
         p = new Player("Chris");
         sp = new ServerPlayer("Micha", null);
     }
 
     @Test
     public void test() {
         assertTrue(p.getName().equals("Chris"));
         p.setName("Michi");
         assertTrue(p.toString().equals("Michi"));
     }
 
     @Test
     public void testEquals() {
         Player p1 = new Player("1");
         Player p2 = new Player("2");
         Player p3 = new Player("1");
 
         assertTrue(p1.equals(p3));
         assertFalse(p1.equals(p2));
         assertFalse(p1.equals(new Object()));
     }
 
     @Test
     public void testServerPlayer1() {
         assertFalse(sp.hasCard(new Card(0, 1)));
         assertFalse(sp.hasNation(1));
         assertFalse(sp.hasNation(0));
         sp.addCard(new Card(0, 1));
         assertTrue(sp.hasCard(new Card(0, 1)));
         assertFalse(sp.hasCard(new Card(1, 1)));
         assertFalse(sp.hasNation(1));
         assertTrue(sp.hasNation(0));
         sp.removeCard(new Card(0, 1));
         assertFalse(sp.hasCard(new Card(0, 1)));
 
         assertNull(sp.getNet());
     }
 
     @Test
     public void testServerPlayer2() {
         assertFalse(sp.hasCard(new Card(0, 14)));
         sp.addCard(new Card(0, 14));
         assertTrue(sp.hasCard(new Card(0, 14)));
         assertFalse(sp.hasCard(new Card(1, 14)));
         sp.removeCard(new Card(0, 14));
         assertFalse(sp.hasCard(new Card(0, 14)));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testServerPlayer3() {
        sp.hasNation(Settings.NATIONCOUNT);
     }
 
 }
