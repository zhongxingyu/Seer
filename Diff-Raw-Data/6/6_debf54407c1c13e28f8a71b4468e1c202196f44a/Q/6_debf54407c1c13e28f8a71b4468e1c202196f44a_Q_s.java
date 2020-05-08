 package environment;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import state.State;
 import action.StateAction;
 import environment.Environment.action;
 
 public class Q {
 	HashMap<StateAction,Double> sa_d = new HashMap<StateAction,Double>();
 	HashMap<State, ArrayList<StateAction>> s_sa = new HashMap<State,ArrayList<StateAction>>();
 	
 	public void set(State s, action a, double d) {
 		StateAction sa = new StateAction(s,a);
 		this.sa_d.put(sa, d);
 		
 		// structure for cheap state-action retrieval
 		ArrayList<StateAction> sa_list = this.s_sa.get(s);
 		if(sa_list == null){
 			sa_list = new ArrayList<StateAction>();
			sa_list.add(sa);
 		}
 		this.s_sa.put(s, sa_list);
 	}
 	
 	public ArrayList<StateAction> getStateActions(State s) {
 		ArrayList<StateAction> sa_list = this.s_sa.get(s);
 		return sa_list;
 	}
 	
 	public double get(State s, action a) {
 		ArrayList<StateAction> sa_list = this.getStateActions(s);
 		for(StateAction sa : sa_list) {
			if(sa.getA().equals(a)){
 				return this.sa_d.get(sa);
 			}
 		}
 		/// BUG BUG BUG
 		new Exception("unable to find required state-action "+s+" - "+a).printStackTrace();
 		System.exit(0);
 		return -1;
 	}
 	
 	public action getArgmaxA(State s) {
 		ArrayList<StateAction> sa_list = this.getStateActions(s);
 		double max = Double.NEGATIVE_INFINITY;
 		action argmax_a = null;
 		for(StateAction sa : sa_list) {
 			Double val = this.sa_d.get(sa);
 			if(val > max) {
 				max = val;
 				argmax_a = sa.getA();
 			}
 		}
 		return argmax_a;
 	}
 	
 	public double getMax(State s) {
 		ArrayList<StateAction> sa_list = this.getStateActions(s);
 		double max = Double.NEGATIVE_INFINITY;
 		for(StateAction sa : sa_list) {
 			Double val = this.sa_d.get(sa);
 			if(val > max) {
 				max = val;
 			}
 		}
 		return max;
 	}
 }
