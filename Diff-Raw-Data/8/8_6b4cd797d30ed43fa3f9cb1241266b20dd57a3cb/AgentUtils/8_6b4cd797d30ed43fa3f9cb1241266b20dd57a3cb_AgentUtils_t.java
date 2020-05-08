 package assignment1;
 
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * This class contains code to build an agent from a custom mapping from states to lists of actions that can be then taken.
  * @author josago
  */
 public class AgentUtils
 {
 	/**
 	 * Builds a custom predator.
 	 * @param pi A mapping from states to lists of actions that can be then taken.
 	 * @return The equivalent agent to the "pi" function. Available actions for each state will be given the same probability.
 	 */
 	public static Agent buildPredator(HashMap<State, List<Integer>> pi)
 	{
 		class PredatorCustom implements Agent
 		{
 			private final HashMap<State, List<Integer>> pi;
 			
 			private PredatorCustom(HashMap<State, List<Integer>> pi)
 			{
 				this.pi = pi;
 			}
 
 			@Override
 			public int getType()
 			{
 				return Agent.TYPE_PREDATOR;
 			}
 
 			@Override
 			public String getSymbol()
 			{
				return "●";
 			}
 
 			@Override
 			public float pi(State env, int action)
 			{
 				List<Integer> actions = pi.get(env);
 
 				if (actions.contains(action))
 				{
 					return 1f / actions.size();
 					
 				}
 				else
 				{
 					return 0;
 				}
 			}
 		}
 		
 		return new PredatorCustom(pi);
 	}
 }
