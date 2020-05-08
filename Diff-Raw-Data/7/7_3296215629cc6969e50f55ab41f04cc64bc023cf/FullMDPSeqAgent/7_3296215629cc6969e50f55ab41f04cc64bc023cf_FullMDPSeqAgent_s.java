 package speed;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import legacy.CartesianProduct;
 import legacy.P_X_t;
 import legacy.PowerSet;
 
 public class FullMDPSeqAgent extends SeqAgent {
 
 	// auction variables
 	SeqAuction auction;
 	Value valuation;
 	JointDistributionEmpirical jde;
 	int agent_idx;
 	
 	// computational variables
 	int t, max_idx, price_length;
 	double temp, temp2, bid, optimal_bid, max_value;
 	P_X_t pxt,PXT;
 	Set<Integer> X, X_more, goods_won;
 	Set<double[]> genPrices;
 	HashMap<P_X_t,Double> V, pi;
 	double[] b, Q, Reward, condDist, prices, realized, realized_plus;	
 
 	public FullMDPSeqAgent(JointDistributionEmpirical jde, Value valuation, int agent_idx) {
 		super(agent_idx, valuation);
 		
 		this.jde = jde;
 		this.valuation = valuation;
 		this.agent_idx = agent_idx;
 		
 		// initiate (to be used when calling "getBid()"; to be cleared when "reset()")
 		goods_won = new HashSet<Integer>();
 		price_length = (int) (jde.max_price/jde.precision+1);	// (used in computeFullMDP)
 
 		// Compute MDP
 		computeFullMDP(jde);
 	}
 	
 	
 	// Ask the agent to computes optimal bidding policy \pi((X,t)) using MDP. The two steps correspond to the two steps in write-up	
 	public void computeFullMDP(JointDistribution pd){	
 
 		Q = new double[price_length];
 		Reward = new double[price_length];
 		condDist = new double[price_length];
 		
 		V = new HashMap<P_X_t,Double>();			// Value function V((X,t))
 		pi = new HashMap<P_X_t,Double>();			// optimal bidding function \pi((X,t))
 		
 		
 		// 1) ******************************** Initialize V values for t = no_slots; corresponding to after all rounds are closed. 
 		t = jde.no_goods;
 		
 		// Start from the whole set, and assign V values to all its power sets
 		Set<Integer> remaining_set = new HashSet<Integer>();
 		for (int i = 0; i< jde.no_goods; i++){
 			remaining_set.add(i);
 		}
 		Set<Set<Integer>> genSet = PowerSet.generate(remaining_set);			// enumerate over all X
 		double[] prices = new double[price_length];	// enumerate over all realized prices
 		for (int i = 0; i < price_length; i++)
 			prices[i] = i*jde.precision;
 		
 		genPrices = CartesianProduct.generate(prices, t);
 
 		// Assign values to states
 		for (Set<Integer> X : genSet) {
 			for (double[] realized : genPrices)
 				V.put(new P_X_t(realized,X,t),valuation.getValue(X.size())); 
 		}
 
 		// list of possible bids (to maximize over)
 		b = new double[price_length];	// enumerate over all realized prices
 
 		
 		// 2) ******************************** Recursively assign values for t = no_slots-1,...,1
 		
 		// > Loop over auction t
 		for (t = jde.no_goods-1; t>-1; t--){ 
 			 
 			for (int i = 0; i < prices.length; i++)
 				b[i]=jde.precision*((double) (i+(i+1))/2-0.1);		// bid = (p_{i}+p_{i+1})/2 - 0.1*precision
 
 			// ----- loops start here
 			genPrices = CartesianProduct.generate(prices, t);	// possible realized historical prices			
 			remaining_set.remove(t);
     		genSet = PowerSet.generate(remaining_set);			// possible subset of goods			
 
     		// > Loop over possible realized historical prices
 			for (double[] realized : genPrices){			
 				// get conditional Distribution
 				condDist = jde.getPMF(realized);
 				realized_plus = new double[realized.length+1];
 				
 				// Precompute Reward = R(b,(realized,X,t)) for each potential bid in b
 				temp = 0;
 	    		for (int j = 0; j < b.length; j++){
 	    			temp += -(j*jde.precision)*condDist[j];	// add -condDist*f(p)
 	    			Reward[j]=temp;
 	    		}
 	    		
 	    		// > Loop over subsets of goods X = {0,...,t-1}
 	    		for (Set<Integer> X : genSet) {
 	    			pxt = new P_X_t(realized,X,t);
 	    			X_more = new HashSet<Integer>();
 		    		X_more.addAll(X);
 		    		X_more.add(t);		    		
 		    		
 		    		// copy realized prices (to append later)
 		    		for (int k = 0; k < realized.length; k++)
     					realized_plus[k] = realized[k];
 		    		    				
 	    			// Compute Q(b,(realized,X,t)) for each bid b
 	    			for (int i = 0; i < b.length; i++) {
 		    			temp2 = Reward[i];
 
 		    			// if agent wins round t
 		    			for (int j = 0; j <= i; j++){
 		    				realized_plus[realized.length] = j*jde.precision;
		    				PXT = new P_X_t(realized_plus,X_more,t+1);
 		    				temp2 += condDist[j]*V.get(PXT);
 		    			}
 		    			// if agent doesn't win round t
 		    			for (int j = i+1; j < condDist.length; j++) {
 		    				realized_plus[realized.length] = j*jde.precision;
		    				PXT = new P_X_t(realized_plus,X,t+1);
 		    				temp2 += condDist[j]*V.get(PXT);
 		    			}
 		    			Q[i]=temp2;
 		    		}
 
 //// print Q function (to comment out) 
 //			    		System.out.print("Q(b,"+pxt.toString()+")=");
 //			    		for (int i = 0; i < Q.length; i++)
 //			    			System.out.print(Q[i]+",");
 //			    		System.out.println();
 		    		
 		    		// Find \pi_((realized,X,t)) = argmax_b Q(b,(realized,X,t))
 		    		max_value = Q[0];		// Value of largest Q((X,t),b)
 			    	max_idx = 0;				// Index of largest Q((X,t),b)
 			    	for (int i = 1; i < Q.length; i++) {
 			    		if (Q[i] > max_value) {	// Compare
 			    			max_value = Q[i];
 			    			max_idx = i;
 			    		}
 			    	}
 		    		// Now we found the optimal bid for state (X,t). Assign values to \pi((X,t)) and V((X,t))
 			    	V.put(pxt,Q[max_idx]);
 		    		pi.put(pxt,b[max_idx]);
 	    		}
     		}		    	
 		}
 	}
 
 	
 	@Override
 	public void reset(SeqAuction auction) {
 		this.auction = auction;
 		goods_won.clear();
 	}
 
 	@Override
 	public double getBid(int good_id) {
 		
 		// Do MDP calculation the first time
 		if (good_id == 0) {
 			price_length = (int) (jde.max_price/jde.precision+1);	// (used in computeFullMDP)
 //			computeFullMDP(jde);
 		} else {			// Figure out what we have won in the past
 			if (auction.winner[good_id-1] == agent_idx)
 				goods_won.add(good_id-1);
 		}
 
 		// figure out realized HOBs
 		realized = new double[good_id];
 		for (int i = 0; i < good_id;i ++) {
 			if (goods_won.contains(i)) {
 				realized[i] = (int) (auction.sp[i]);	// TODO: currently, forced prices to be integers in an ad hoc way. To be fixed. 
 			} else {
 				realized[i] = (int) (auction.fp[i]);
 			}
 		}
 		
 		P_X_t state = new P_X_t(realized,goods_won,good_id);			// Current state (realized,X,t)
 		System.out.println("good id = "+good_id+",current state = " + state.toString());
 
 		return pi.get(state);
 	}
 	
 }
