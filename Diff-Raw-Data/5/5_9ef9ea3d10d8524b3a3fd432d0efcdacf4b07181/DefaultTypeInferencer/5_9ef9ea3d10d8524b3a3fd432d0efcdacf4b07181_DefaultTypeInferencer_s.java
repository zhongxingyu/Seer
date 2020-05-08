 package org.eclipse.dltk.ti;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ti.goals.AbstractTypeGoal;
 import org.eclipse.dltk.ti.goals.GoalEvaluator;
 import org.eclipse.dltk.ti.goals.IGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class DefaultTypeInferencer implements ITypeInferencer {
 
 	private Map evaluators = new HashMap();
 
 	private class MapBasedEvaluatorFactory implements IGoalEvaluatorFactory {
 
 		public GoalEvaluator createEvaluator(IGoal goal) {
 			Class goalClass = goal.getClass();
 			Object evaluator = evaluators.get(goalClass);
 			if (evaluator == null || (!(evaluator instanceof Class))) {
 				 
 				if (userFactory != null) {
 					evaluator = userFactory.createEvaluator(goal);
 				}
 				if (evaluator == null)
 					throw new RuntimeException("No evaluator registered for "
 							+ goalClass.getName() + " : " + goal);
 				return (GoalEvaluator) evaluator;
 			}
 			Class evalClass = (Class) evaluator;
 			GoalEvaluator newInstance;
 			try {
 				newInstance = (GoalEvaluator) evalClass.newInstance();
 				return newInstance;
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 	}
 
 	private final GoalEngine engine;
 	private final IGoalEvaluatorFactory userFactory;
 
 	private void initStdGoals () {
 		//registerEvaluator(FieldReferencesGoal.class, FieldReferencesGoalEvaluator.class);
 		//registerEvaluator(MethodCallsGoal.class, MethodCallsGoalEvaluator.class);
 	}
 
 	public DefaultTypeInferencer(IGoalEvaluatorFactory userFactory) {
 		engine = new GoalEngine(new MapBasedEvaluatorFactory());
 		this.userFactory = userFactory;
 		initStdGoals();
 	}
 
 	public void registerEvaluator(Class goalClass, Class evaluatorClass) {
 		Assert.isLegal(goalClass.isAssignableFrom(IGoal.class));
 		Assert.isLegal(evaluatorClass.isAssignableFrom(GoalEvaluator.class));
 		evaluators.put(goalClass, evaluatorClass);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.dltk.ti.ITypeInferencer#evaluateType(org.eclipse.dltk.ti.AbstractTypeGoal,
 	 *      long)
 	 */
 	public IEvaluatedType evaluateType(AbstractTypeGoal goal, int timeLimit) {
		Object result = engine.evaluateGoal(goal, null); //timelimit prunner
 		return (IEvaluatedType) result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.dltk.ti.ITypeInferencer#evaluateType(org.eclipse.dltk.ti.AbstractTypeGoal)
 	 */
 	public IEvaluatedType evaluateType(AbstractTypeGoal goal, IPruner pruner) {
		return evaluateType(goal, pruner);
 	}
 	
 	public IEvaluatedType evaluateType(AbstractTypeGoal goal) {
 		return evaluateType(goal, null);
 	}
 
 }
