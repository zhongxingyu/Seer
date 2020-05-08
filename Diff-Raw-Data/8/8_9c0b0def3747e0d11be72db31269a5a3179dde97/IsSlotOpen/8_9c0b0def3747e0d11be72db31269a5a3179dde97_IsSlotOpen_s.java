 package edu.wheaton.simulator.expression;
 
import edu.wheaton.simulator.entity.Agent;
 import net.sourceforge.jeval.EvaluationException;
 
 public class IsSlotOpen extends AbstractExpressionFunction {
 
 	@Override
 	public String getName() {
 		return "isSlotOpen";
 	}
 
 	@Override
 	public int getResultType() {
 		return AbstractExpressionFunction.RESULT_TYPE_BOOL;
 	}
 
 	@Override
 	public String execute(String[] args) throws EvaluationException {
 		Double x = Double.valueOf(args[0]);
 		Double y = Double.valueOf(args[1]);
 		Boolean isOpen = resolveAgent("this").getGrid().emptySlot(x.intValue(),y.intValue());
 		return isOpen.toString();
 	}
 
 }
