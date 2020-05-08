 package com.cegeka.nocturne.godgame;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class SpreadingOfLifeTest {
 
     private Grass grass;
     private World world;
 
     @Before
     public void setup() {
         world = new World(5);
         grass = new Grass();
         grass.setAge(6);
         world.setCell(grass, 2, 2);
     }
 	
 	@Test
 	public void spawnNewGrassTestLessThan7Days() {
 		assertThat(grass.spawnNewGrass()).isEqualTo(false); // < 7
 	}
 	
	@Test
	public void spawnNewGrassTest7Days() {
		grass.setAge(7); // == 7 -> should spawn
		assertThat(grass.spawnNewGrass()).isEqualTo(true);
	}
 }
