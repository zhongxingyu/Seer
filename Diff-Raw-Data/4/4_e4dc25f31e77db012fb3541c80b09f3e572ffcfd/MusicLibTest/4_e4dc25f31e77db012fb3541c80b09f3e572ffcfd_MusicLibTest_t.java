 package musiikkisoitin;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import musiikkisoitin.MusicLib;
 import musiikkisoitin.Song;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author tuurekau
  */
 public class MusicLibTest {
     MusicLib mLib;
     
     @Before
     public void setUp() {
         mLib = new MusicLib(new User("test123"));
     }
     
     @After
     public void tearDown() {
        mLib.removeUser();
     }
 
     @Test
     public void initialized() {
         
         assertEquals(0, mLib.getSongs().size());
     }
     
     @Test
     public void dataFileCreated(){
         assertEquals(mLib.getDataFile().exists(), true);
     }
     
     @Test
     public void addingNullSong(){
         mLib.addSong(null);
         assertEquals(0, mLib.getSongs().size());
     }
     
     @Test
     public void addingSongs(){
         mLib.addSong(new Song("Foobar"));
         assertEquals(1, mLib.getSongs().size());
         mLib.addSong(new Song("Foobar2"));
         assertEquals(2, mLib.getSongs().size());
     }
     
     @Test
     public void getRandom(){
         MusicLib mLib2 = new MusicLib(new User("test123"), new PseudoRand());
         mLib2.addSong(new Song("Foobar 1"));
         mLib2.addSong(new Song("Foobar 2"));
         mLib2.addSong(new Song("Foobar 3"));
         
         Song s = mLib2.getRandom();
         String str = s.getFileName();
         assertEquals("Foobar 2", str);
        mLib2.removeUser();
     }
 }
