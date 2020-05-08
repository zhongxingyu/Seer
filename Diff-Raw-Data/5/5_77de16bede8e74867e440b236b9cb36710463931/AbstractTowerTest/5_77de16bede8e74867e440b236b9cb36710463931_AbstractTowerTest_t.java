 package se.chalmers.towerdefence.model;
 
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import org.junit.Test;
 
 public class AbstractTowerTest {
 	private ArrayList<RoadSquare> roadIterator;
 	
 	@Test
 	public void sellTowerTest() {
		Tower tower = new Tower(1.0f, 1.0f, null);
 		assertTrue(tower.exists() == true);
 		tower.sellTower();
 		assertTrue(tower.exists() == false);
 	}
 	
 	@Test
 	public void monsterInRangeTest() {
 		this.roadIterator = new ArrayList<RoadSquare>();
 		roadIterator.add(new RoadSquare(0, 0));
 		roadIterator.add(new RoadSquare(0, 4));
 		roadIterator.add(new RoadSquare(4, 4));
 		roadIterator.add(new RoadSquare(4, 0));
 		roadIterator.add(new RoadSquare(8, 0));
 		roadIterator.add(new RoadSquare(8, 4));
 		roadIterator.add(new RoadSquare(8, 8));
 		roadIterator.add(new RoadSquare(4, 8));
 		roadIterator.add(new RoadSquare(4, 12));
 		roadIterator.add(new RoadSquare(8, 12));
 		roadIterator.add(new RoadSquare(12, 12));
 		roadIterator.add(new RoadSquare(12, 8));
 		roadIterator.add(new RoadSquare(16, 8));
 		roadIterator.add(new RoadSquare(16, 12));
 		roadIterator.add(new RoadSquare(16, 16));
 		roadIterator.add(new RoadSquare(20, 16));
 		roadIterator.add(new RoadSquare(24, 16));
 		roadIterator.add(new RoadSquare(28, 16));
 		roadIterator.add(new RoadSquare(28, 12));
 		roadIterator.add(new RoadSquare(24, 12));
 		roadIterator.add(new RoadSquare(24, 8));
 		roadIterator.add(new RoadSquare(38, 8));
 	
 		
		Tower tower = new Tower(1.0f, 1.0f, null);
 		Road road = new Road(roadIterator);
 		Player player = new Player(0,0);
 		Monster monster = new Monster(road, player);
 		assertTrue(tower.monsterInRange(monster));
 		for (int i = 0; i < 379; i++) {
 			monster.move();
 		}
 		assertTrue(tower.monsterInRange(monster));
 		monster.move();
 		assertFalse(tower.monsterInRange(monster));
 		tower.upgradeTower();
 		assertTrue(tower.monsterInRange(monster));
 	}
 
 }
