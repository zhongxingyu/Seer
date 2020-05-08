 package projectrts.model.abilities;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import projectrts.model.GameModel;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.Player;
 import projectrts.model.entities.Warrior;
 import projectrts.model.world.Position;
 
 public class AbilityManagerTest {
 	Warrior warrior;
 	AbilityManager am;
 	@Before
 	public void setUp() throws Exception {
 		new GameModel();
 		am = new AbilityManager();
 		EntityManager.INSTANCE.addNewPCE(Warrior.class.getSimpleName(), new Player(null), new Position(5,5));
 		EntityManager.INSTANCE.update(1);
 		warrior  = (Warrior) EntityManager.INSTANCE.getPCEAtPosition(new Position(5,5));
 		
 	}
 	
 	@Test
 	public void testAddAbilitiesToEntity(){		
 		List<IAbility> abl = am.getAbilities(warrior);
 		assertTrue("Attack".equals(abl.get(0).getName()));
 		assertTrue("Move".equals(abl.get(1).getName()));		
 	}
 	
 	@Test
 	public void testDoAbility(){
 		am.doAbility(MoveAbility.class.getSimpleName(), new Position (3.5,4.5), warrior);
 		int counter = 0;
 		while(!warrior.getPosition().equals(new Position(3.5,4.5))){
 			am.update(1);
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			counter ++;
 			assertTrue(counter<100);
 		}
 	}
 	
 	@Test
 	public void testUseAbilitySelected(){
 		EntityManager.INSTANCE.select(warrior.getPosition(), warrior.getOwner());
 		am.useAbilitySelected(MoveAbility.class.getSimpleName(), new Position(2.5,3.5), warrior.getOwner());
 		int counter = 0;
 		while(!warrior.getPosition().equals(new Position(3.5,4.5))){
 			am.update(1);
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			counter ++;
 			assertTrue(counter<100);
 		}
 	}
 	
 	@Test
 	public void testAbortAbility(){
 		am.doAbility(MoveAbility.class.getSimpleName(), new Position (60.5,50.5), warrior);
 		for(int i=0;i<50;i++){	
 			am.update(1);
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		am.abortAbility(MoveAbility.class.getSimpleName(), warrior);
 		Position oldPos= warrior.getPosition();
 		for(int i=0;i<50;i++){	
 			am.update(1);
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		assertFalse(oldPos.equals(warrior.getPosition()));
 	}
 	
 	
 }
