 package org.eclipse.dltk.core.tests.ddp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.NumericLiteral;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.ti.GoalState;
 import org.eclipse.dltk.ti.IGoalEvaluatorFactory;
 import org.eclipse.dltk.ti.ITypeInferencer;
 import org.eclipse.dltk.ti.DefaultTypeInferencer;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.goals.GoalEvaluator;
 import org.eclipse.dltk.ti.goals.IGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class DDPTests extends TestCase {
 
 	private static final class FixedAnswerGoalEvaluator extends GoalEvaluator {
 		private final IEvaluatedType answer;
 
 		private FixedAnswerGoalEvaluator(IGoal goal, IEvaluatedType answer) {
 			super(goal);
 			this.answer = answer;
 		}		
 
 		public Object produceResult() {
 			return answer;
 		}
 
 		public IGoal[] init() {
 			return IGoal.NO_GOALS;
 		}
 
 		public IGoal[] subGoalDone(IGoal goal2, Object result, GoalState state) {
 			return IGoal.NO_GOALS;
 		}
 				
 		
 	}
 
 	private static final class SingleDependentGoalEvaluator extends GoalEvaluator {
 		private final IEvaluatedType answer;
 
 		private final IGoal[] dependents;
 
 //		private int state = 0;
 		
 		private int produceCalls = 0;
 		
 		private int produceTypeCalls = 0;
 
 		private SingleDependentGoalEvaluator(IGoal goal, IGoal dependent, IEvaluatedType answer) {
 			super(goal);
 			this.dependents = new IGoal[] { dependent };
 			this.answer = answer;
 		}
 
 		private SingleDependentGoalEvaluator(IGoal goal, IGoal[] dependents, Object answer) {
 			super(goal);
 			this.dependents = dependents;
 			this.answer = (IEvaluatedType) answer;
 		}
 	
 		public IGoal[] init() {
 			++produceCalls;
 			return dependents;
 		}
 
 		public IGoal[] subGoalDone(IGoal goal2, Object result, GoalState _state) {
 			++produceCalls;
 			assertTrue(result instanceof MyNum || _state == GoalState.RECURSIVE);			
 			return IGoal.NO_GOALS;
 		}
 
 		public Object produceResult() {
 			++produceTypeCalls;
 			return answer;
 		}
 		
 		public void assertState() {
 			assertEquals(1, produceTypeCalls);
 			assertEquals(1 + dependents.length, produceCalls);
 		}
 
 		public int getProduceCalls() {
 			return produceCalls;
 		}
 		
 	}
 
 	class MyNum implements IEvaluatedType {
 
 		public String toString() {
 			return "MyNum";
 		}
 
 		public String getTypeName() {
 			return "MyNum";
 		}
 
 		public boolean subtypeOf(IEvaluatedType type) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 	}
 
 	public void testSimple() throws Exception {
 		// y = 2; x = y; x?
 		final Expression x = new SimpleReference(0, 0, "x");
 		final Expression y = new SimpleReference(0, 0, "y");
 		final Expression num = new NumericLiteral(new DLTKToken());
 
 		IGoalEvaluatorFactory factory = new IGoalEvaluatorFactory() {
 
 			public GoalEvaluator createEvaluator(IGoal goal) {
 				if (goal instanceof ExpressionTypeGoal) {
 					ExpressionTypeGoal egoal = (ExpressionTypeGoal) goal;
 					Statement expr = egoal.getExpression();
 					if (expr == x)
 						return new SingleDependentGoalEvaluator(goal, new ExpressionTypeGoal(null, y),
 								new MyNum());
 					if (expr == y)
 						return new SingleDependentGoalEvaluator(goal, new ExpressionTypeGoal(null, num),
 								new MyNum());
 					if (expr == num)
 						return new FixedAnswerGoalEvaluator(goal, new MyNum());
 				}
 				return null;
 			}
 
 		};
 
 		final ITypeInferencer man = new DefaultTypeInferencer(factory);
 
 		ExpressionTypeGoal rootGoal = new ExpressionTypeGoal(null, x);
		IEvaluatedType answer = man.evaluateType(rootGoal, null);
 
 		assertTrue(answer instanceof MyNum);
 	}
 
 	public void testCycles() throws Exception {
 		final Expression x = new SimpleReference(0, 0, "x");
 		final Expression y = new SimpleReference(0, 0, "y");
 		final Expression z = new SimpleReference(0, 0, "z");
 		final Expression num = new NumericLiteral(new DLTKToken());
 		
 		final Collection evaluators = new ArrayList();
 		IGoalEvaluatorFactory factory = new IGoalEvaluatorFactory() {
 
 			public GoalEvaluator createEvaluator2(IGoal goal) {
 				if (goal instanceof ExpressionTypeGoal) {
 					ExpressionTypeGoal egoal = (ExpressionTypeGoal) goal;
 					Statement expr = egoal.getExpression();
 					if (expr == x)
 						return new SingleDependentGoalEvaluator(goal, new IGoal[] {
 								new ExpressionTypeGoal(null, y), new ExpressionTypeGoal(null, z) }, new MyNum());
 					if (expr == y)
 						return new SingleDependentGoalEvaluator(goal,
 								new IGoal[] { new ExpressionTypeGoal(null, z) }, new MyNum());
 					if (expr == z)
 						return new SingleDependentGoalEvaluator(goal, new IGoal[] {
 								new ExpressionTypeGoal(null, num), new ExpressionTypeGoal(null, y) }, new MyNum());
 					if (expr == num)
 						return new FixedAnswerGoalEvaluator(goal, new MyNum());
 				}
 				return null;
 			}
 			
 			public GoalEvaluator createEvaluator(IGoal goal) {
 				GoalEvaluator result = createEvaluator2(goal);
 				if (result != null) 
 					evaluators.add(result);
 				return result;
 			}
 			
 		};
 
 		final ITypeInferencer man = new DefaultTypeInferencer(factory);
 
 		ExpressionTypeGoal rootGoal = new ExpressionTypeGoal(null, x);
		IEvaluatedType answer = man.evaluateType(rootGoal, null);
 
 		assertTrue(answer instanceof MyNum);
 		for (Iterator iter = evaluators.iterator(); iter.hasNext();) {
 			GoalEvaluator ev = (GoalEvaluator) iter.next();
 			if (ev instanceof SingleDependentGoalEvaluator) {
 				SingleDependentGoalEvaluator sdge = (SingleDependentGoalEvaluator) ev;
 				sdge.assertState();
 			}
 		}
 	}
 	
 
 
 }
