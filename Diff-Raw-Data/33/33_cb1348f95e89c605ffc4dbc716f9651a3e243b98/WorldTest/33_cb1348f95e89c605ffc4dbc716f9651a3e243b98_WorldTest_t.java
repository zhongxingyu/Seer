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
	int expectedX1;
	int expectedY1;

	public WorldTest(int width, int height, int widthJungle, int heightJungle,
			int expectedX1, int expectedY1) {
 
 		this.widthJungle = widthJungle;
 		this.heightJungle = heightJungle;
		this.expectedX1 = expectedX1;
		this.expectedY1 = expectedY1;
 		dieWelt = new World(width, height, widthJungle, heightJungle);
 	}
 
 	@Parameters
 	public static Collection<Object[]> createTestParams() {
		return Arrays.asList(new Object[][] { { 40, 30, 10, 10, 15, 10 },
				{ 40, 27, 10, 10, 15, 8 }, { 40, 30, 11, 10, 14, 10 },
				{ 40, 30, 10, 11, 15, 9 }, { 40, 30, 11, 11, 14, 9 } });
 	}
 
 	@Test
 	public void testSetJungleLimits() {
 		dieWelt.setJungleLimits();
		assertEquals("getJungleLimitY1", expectedY1, dieWelt.getJungleLimitY1());
		assertEquals("getJungleLimitY2", expectedY1 + heightJungle,
 				dieWelt.getJungleLimitY2());
		assertEquals("getJungleLimitX1", expectedX1, dieWelt.getJungleLimitX1());
		assertEquals("getJungleLimitX2", expectedX1 + widthJungle,
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
 		// Add 5 Plants
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(15, 20);
 		dieWelt.addPlant(20, 15);
 		dieWelt.addPlant(20, 15);
 		assertEquals(5, dieWelt.totalPlants());
 	}
 
 }
