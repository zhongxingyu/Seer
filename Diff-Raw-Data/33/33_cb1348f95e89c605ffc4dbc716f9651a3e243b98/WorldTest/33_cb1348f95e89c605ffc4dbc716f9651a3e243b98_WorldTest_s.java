 package de.proglabor.aufgabe1.test;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import de.proglabor.aufgabe1.World;
 
 @RunWith(Parameterized.class)
 public class WorldTest {
 
 	World dieWelt;
 	int widthJungle;
 	int heightJungle;
 
	public WorldTest(int width, int height, int widthJungle, int heightJungle) {
 		this.widthJungle = widthJungle;
 		this.heightJungle = heightJungle;
 		dieWelt = new World(width, height, widthJungle, heightJungle);
 	}
 
 	@Parameters
 	public static Collection<Object[]> createTestParams() {
		return Arrays.asList(new Object[][] { { 40, 30, 10, 10 },
				{ 39, 30, 10, 10 }, { 40, 29, 10, 10 }, { 30, 40, 11, 10 },
				{ 30, 40, 10, 11 } });
 	}
 
 	@Test
 	public void testSetJungleLimits() {
 		dieWelt.setJungleLimits();
		assertEquals("getJungleLimitY1", 10, dieWelt.getJungleLimitY1());
		assertEquals("getJungleLimitY2", 10 + heightJungle,
 				dieWelt.getJungleLimitY2());
		assertEquals("getJungleLimitX1", 15, dieWelt.getJungleLimitX1());
		assertEquals("getJungleLimitX2", 15 + widthJungle,
 				dieWelt.getJungleLimitX2());
 	}
 
 	@Test
 	public void testAddPlant() {
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(20, 15);
 		dieWelt.addPlant(20, 15);
 		int[][] container = dieWelt.getPlantContainer();
 		assertEquals(0, container[0][0]);
 		assertEquals(1, container[15][20]);
 		assertEquals(2, container[20][15]);
 	}
 
 	@Test
 	public void testTotalPlants() {
		int width = 40;
		int height = 30;
		int widthJungle = 10;
		int heightJungle = 10;
		World dieWelt = new World(width, height, widthJungle, heightJungle);
 		// Add 5 Plants
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(20, 15);
 		dieWelt.addPlant(20, 15);
 		assertEquals(5, dieWelt.totalPlants());
 	}
 
 }
