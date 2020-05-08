 package com.github.fhd.gwtetris.client;
 
 import static org.junit.Assert.*;
 
 import org.junit.*;
 
 import com.github.fhd.gwtetris.client.gamelogic.*;
 
 public class JavaRNGTest {
     private RNG rng = new JavaRNG();
     
     @Test
     public void randomPiecePosition() {
         int r = rng.randomPiecePosition(5);
        assertTrue(r > 0 && r < 6);
     }
     
     @Test
     public void randomPieceTypeNo() {
         int r = rng.randomPieceTypeNo(5);
        assertTrue(r > 0 && r < 5);
     }
 }
