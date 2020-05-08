 package edu.wheaton.simulator.behavior;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.expression.Expression;
 
 public class MoveBehavior extends AbstractBehavior {
 
 	/**
 	 * Main constructor
 	 * 
 	 */
 //	public MoveBehavior(ExpressionEvaluator eval) {
 //		super(eval);
 //	}
 
 	@Override
 	public String getName() {
 		return "move";
 	}
 
 	/**
 	 * Attempts to move the target Agent to the (x, y) position found by
 	 * evaluating the expressions. If the slot it attempts to move to is
 	 * already full, it throws an exception. It would be good if we could find
 	 * a way to allow the user to say what happens when a FullSlotException is
 	 * thrown.
 	 */
 	@Override
 	public String execute(String[] args) throws EvaluationException {
 		Agent target = resolveAgent(args[0]);
		int oldX = target.getPosX();
		int oldY = target.getPosY();
		
 		Integer x = Double.valueOf(args[1]).intValue();
 		Integer y = Double.valueOf(args[2]).intValue();
 		
 		Grid grid = target.getGrid();
		if(grid.addAgent(target, x, y)) {
			grid.removeAgent(oldX, oldY);
 			return Expression.TRUE;
		}
 		return Expression.FALSE;
 	}
 
 }
