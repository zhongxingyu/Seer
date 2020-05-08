 package projectrts.model.entities;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import projectrts.model.GameModel;
 import projectrts.model.world.Position;
 
 public class EntityManagerTest {
 
 	private EntityManager entityManager = EntityManager.INSTANCE;
 	
 	@Test
 	public void testGetEntitiesOfPlayer() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.update(1);
 		List<IPlayerControlledEntity> entities = entityManager
 				.getEntitiesOfPlayer(player);
 
 		assertTrue(entities.size() == 1);
 		for (IPlayerControlledEntity e : entities) {
 			assertTrue(e.getOwner().equals(player));
 		}
 
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(10, 13));
 		entityManager.update(1);
 		entities = entityManager.getEntitiesOfPlayer(player);
 
 		assertTrue(entities.size() == 4);
 		for (IPlayerControlledEntity e : entities) {
 			assertTrue(e.getOwner().equals(player));
 		}
 	}
 
 	@Test
 	public void testGetAllEntities() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.update(1);
 		List<IEntity> entities = entityManager.getAllEntities();
 
 		assertTrue(entities.size() == 1);
 
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(10, 13));
 		entityManager.update(1);
 		entities = entityManager.getAllEntities();
 
 		assertTrue(entities.size() == 5);
 	}
 
 	@Test
 	public void testRequestNewEntityID() {
 		List<Integer> numbers = new ArrayList<Integer>();
 		int testAmount = 1000;
 
 		for (int i = 0; i < testAmount; i++) {
 			int nr = entityManager.requestNewEntityID();
 			assertTrue(!numbers.contains(nr));
 			numbers.add(nr);
 		}
 	}
 	
 	@Test
 	public void testRemoveEntity() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.addNewNPCE(Resource.class.getSimpleName(), new Position(20, 20));
 		entityManager.update(1);
 		
 		IEntity entity = entityManager.getEntityAtPosition(new Position(10, 10));
 		AbstractPlayerControlledEntity pce = null;
 		AbstractNonPlayerControlledEntity npce = null;
 		if (entity instanceof AbstractPlayerControlledEntity) {
 			pce = (AbstractPlayerControlledEntity) entity;
 		}
 		entity = entityManager.getEntityAtPosition(new Position(20, 20));
 		if (entity instanceof AbstractNonPlayerControlledEntity) {
 			npce = (AbstractNonPlayerControlledEntity) entity;
 		}
 		assertTrue(pce != null);
 		assertTrue(npce != null);
 		assertTrue(entityManager.getAllEntities().size() == 2);
 		
 		entityManager.removeEntity(pce);
 		entityManager.update(1);
 		assertTrue(entityManager.getAllEntities().size() == 1);
 		assertTrue(entityManager.getEntityAtPosition(new Position(10, 10)) == null);
 		
 		entityManager.removeEntity(npce);
 		entityManager.update(1);
 		assertTrue(entityManager.getAllEntities().size() == 0);
 		assertTrue(entityManager.getEntityAtPosition(new Position(20, 20)) == null);
 	}
 	
 	@Test
 	public void testGetEntityAtPosition() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.addNewNPCE(Resource.class.getSimpleName(), new Position(20, 20));
 		entityManager.update(1);
 		
 		IEntity entity = entityManager.getEntityAtPosition(new Position(7, 7));
 		assertTrue(entity == null);
 		entity = entityManager.getEntityAtPosition(new Position(10, 10));
 		assertTrue(entity instanceof AbstractPlayerControlledEntity);
 		entity = entityManager.getEntityAtPosition(new Position(20, 20));
 		assertTrue(entity instanceof AbstractNonPlayerControlledEntity);
 	}
 	
 	@Test
 	public void testGetPCEAtPositionPosition() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.update(1);
 		
 		AbstractPlayerControlledEntity pce =
 				entityManager.getPCEAtPosition(new Position(7, 7));
 		assertTrue(pce == null);
 		pce = entityManager.getPCEAtPosition(new Position(10, 10));
 		assertTrue(pce != null);
 	}
 	
 	@Test
 	public void testGetPCEAtPositionPositionPlayer() {
 		new GameModel();
 		Player player1 = new Player(null);
 		Player player2 = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player1,
 				new Position(10, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player2,
 				new Position(20, 20));
 		entityManager.update(1);
 		
 		AbstractPlayerControlledEntity pce =
 				entityManager.getPCEAtPosition(new Position(7, 7), player1);
 		assertTrue(pce == null);
 		pce = entityManager.getPCEAtPosition(new Position(10, 10), player1);
 		assertTrue(pce != null);
 		
 		pce = entityManager.getPCEAtPosition(new Position(10, 10), player2);
 		assertTrue(pce == null);
 		pce = entityManager.getPCEAtPosition(new Position(20, 20), player2);
 		assertTrue(pce != null);
 		pce = entityManager.getPCEAtPosition(new Position(20, 20), player1);
 		assertTrue(pce == null);
 	}
 	
 	@Test
 	public void testGetNPCEAtPosition() {
 		new GameModel();
 		entityManager.resetData();
 		entityManager.addNewNPCE(Resource.class.getSimpleName(), new Position(10, 10));
 		entityManager.update(1);
 		
 		AbstractNonPlayerControlledEntity npce =
 				entityManager.getNPCEAtPosition(new Position(7, 7));
 		assertTrue(npce == null);
 		npce = entityManager.getNPCEAtPosition(new Position(10, 10));
 		assertTrue(npce != null);
 	}
 	
 	@Test
 	public void testSelect() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		Position onUnit = new Position(10, 10);
 		Position closeToUnit = new Position(9.5f, 9.5f);
 		Position farFromUnit = new Position(5, 5);
 		entityManager.update(1);
 		AbstractPlayerControlledEntity pce = entityManager.getPCEAtPosition(onUnit);
 		
 		assertTrue(pce != null);
 
 		assertTrue(entityManager.getSelectedEntities().size() == 0);
 		assertTrue(!entityManager.isSelected(pce));
 		assertTrue(entityManager.getSelectedEntitiesOfPlayer(player).size()==0);
 
 		entityManager.select(onUnit, player);
 		assertTrue(entityManager.getSelectedEntities().size() != 0);
 		assertTrue(entityManager.isSelected(pce));
 		assertTrue(entityManager.getSelectedEntitiesOfPlayer(player).size()==1);
 		assertTrue(entityManager.getSelectedEntitiesOfPlayer(player).get(0).equals(pce));
 
 		entityManager.select(closeToUnit, player);
 		assertTrue(entityManager.getSelectedEntities().size() != 0);
 
 		entityManager.select(farFromUnit, player);
 		assertTrue(entityManager.getSelectedEntities().size() == 0);
 	}
 
 	@Test
 	public void testGetNearbyEntities() {
 		new GameModel();
 		Player player = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		entityManager.update(1);
 		List<AbstractEntity> entities = entityManager
 				.getNearbyEntities(new Position(15, 10), 2);
 		assertTrue(entities.isEmpty());
 
 		entities = entityManager.getNearbyEntities(
 				new Position(15, 10), 6);
 		assertTrue(entities.size() == 1);
 
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(14, 10));
 		entityManager.update(1);
 		entities = entityManager.getNearbyEntities(
 				new Position(15, 10), 3);
 		assertTrue(entities.size() == 3);
 	}
 	
 	@Test
 	public void testGetClosestEnemy() {
 		new GameModel();
 		Player player1 = new Player(null);
 		Player player2 = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player1,
 				new Position(10, 10));
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player2,
 				new Position(13, 10));
 		entityManager.update(1);
 		AbstractPlayerControlledEntity pce1 = entityManager.getPCEAtPosition(new Position(10, 10));
 		AbstractPlayerControlledEntity pce2 = entityManager.getPCEAtPosition(new Position(13, 10));
 		
 		AbstractPlayerControlledEntity foundPCE = entityManager.getClosestEnemy(pce1);
 		assertTrue(foundPCE != null);
 		assertTrue(foundPCE.equals(pce2));
 		
 		foundPCE = entityManager.getClosestEnemy(pce2);
 		assertTrue(foundPCE != null);
 		assertTrue(foundPCE.equals(pce1));
 	}
 	
 	@Test
 	public void testGetClosestEnemyStructure() {
 		new GameModel();
 		Player player1 = new Player(null);
 		Player player2 = new Player(null);
 		entityManager.resetData();
 		entityManager.addNewPCE(Worker.class.getSimpleName(), player1,
 				new Position(10, 10));
 		entityManager.addNewPCE(Headquarter.class.getSimpleName(), player2,
 				new Position(13, 10));
 		entityManager.update(1);
 		AbstractPlayerControlledEntity unit = entityManager.getPCEAtPosition(new Position(10, 10));
 		AbstractPlayerControlledEntity building = entityManager.getPCEAtPosition(new Position(13, 10));
 		
		AbstractPlayerControlledEntity foundPCE = entityManager.getClosestEnemyStructure(unit);
 		assertTrue(foundPCE != null);
 		assertTrue(foundPCE.equals(building));
 	}
 }
