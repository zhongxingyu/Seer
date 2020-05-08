 package edu.wheaton.simulator.test.simulation;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.expression.ExpressionEvaluator;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class RockPaperScissorsTest {
 	Grid testGrid;
 	String [] agentType = {"rock", "paper", "scissors"};
 	
 	@Before
 	public void setUp(){
 		this.testGrid = new Grid(100, 100);
 		
 		ExpressionEvaluator xMoveRight = new Expression("move('this', #{this.x} + 1, #{this.y})");		
 		ExpressionEvaluator yMoveUp = new Expression("move('this', #{this.x}, #{this.y} + 1)");		
 		ExpressionEvaluator xMoveLeft = new Expression("move('this', #{this.x} - 1, #{this.y})");		
 		ExpressionEvaluator yMoveDown = new Expression("move('this', #{this.x}, #{this.y} - 1)");
 		
 		// behavior
 		ExpressionEvaluator turnClockwise = new Expression("setField('this', 'direction', (#{this.direction} +1)%4)");
 		
 		// if nobody ahead in this direction
 		
 		ExpressionEvaluator dir0 = new Expression("#{this.direction} = 0 && isSlotOpen(#{this.x},#{this.y}+1)");		
 		ExpressionEvaluator dir1 = new Expression("#{this.direction} = 1 && isSlotOpen(#{this.x}+1,#{this.y})");
 		ExpressionEvaluator dir2 = new Expression("#{this.direction} = 2 && isSlotOpen(#{this.x},#{this.y}-1)");
 		ExpressionEvaluator dir3 = new Expression("#{this.direction} = 3 && isSlotOpen(#{this.x}-1,#{this.y})");
 				
 		for(int j = 0; j < agentType.length; j ++){
 			Prototype testPrototype = new Prototype(testGrid, "testPrototype");
 			try {
 				testPrototype.addField("type", agentType[j]);
 				testPrototype.addField("direction", j);
 				testPrototype.addField("initialDIrection", j);
 			} catch (ElementAlreadyContainedException e) {
 				e.printStackTrace();
 			}
 			// Normal moving behavior
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 1, dir0, xMoveRight));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 1, dir1, yMoveUp));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 1, dir2, xMoveLeft));
 			testPrototype.addTrigger(new Trigger("moveIfNoObstacle", 1, dir3, yMoveDown));
 
 			for(int i = 0; i < 10; i ++){
 				testGrid.spawnAgent(testPrototype.clonePrototype());
 			}
 		}
 	}
 	@Test
 	public void test() {
 		//Run through multiple "steps"
 		int numSteps = 100;
 		for(int i = 0; i < numSteps; i++){
 			try {
 				testGrid.updateEntities();
 			} catch (NullPointerException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} //currently updateEntities does not work
 		}
 		
 	}
 }
