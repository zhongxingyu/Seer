 package agents;
 
 
 import java.util.*;
 
 import se.sics.tasim.props.SimulationStatus;
 
 import agents.rules.Constants;
 import edu.umich.eecs.tac.props.*;
 
 public class KBWAgentMkII extends AbstractAgent {
 	
 	Random _R = new Random();
 
 	protected ArrayList<HashMap<Query,Double>> _allweights;
 	protected ArrayList<HashMap<Query, Double>> _allconversions;
 	protected ArrayList<HashMap<Query, Double>> _percenterror;
 	protected HashMap<Query,Double> _bids;
 	protected HashMap<Query,Double> _oldprices;
 	protected HashMap<Query,Double> _goalpositions;
 	
 	protected ArrayList<Double> _quotas;
 	
 	protected int[] _totalConversions;
 	
 	protected int _capacity;
 	protected int _window;
 	
 	protected double QUOTA = .2;
 	private double CSB;
 	private double MSB;
 	protected int _currentday;
 
 	private String manufacturerSpecialty;
 	private Set<Query> ourspecialty;
 
 	private int recentConvHist;
 	
 	// Initialized to our actual cost, we never want to bid above this
	private final double _maxF0Bid;
	private final double _maxF1Bid;
	private final double _maxF2Bid;
 
 	protected final static double INITIAL_CHEAPNESS = 1.0; //currently obsolete because of the entire cheapness factor
 	protected final static double CHEAPNESS = .5;
 	protected final static double BUDGETCHEAPNESS = .8;
 	protected final static double LEARNING_RATE = .05;
 	
 	protected final static double INC_RATE = 1.1;
 	protected final static double DEC_RATE = .9;
 	
 	protected final static double lambda = .995;
 	
 	@Override
 	protected BidBundle buildBidBudle() {
 		
 		BidBundle bidBundle = new BidBundle();
 		
 		// To start out, here are the assumptions for the current 
 		// version of the bidder
 		// 1. We will use a fixed amount of our capacity every day 
 		// with the intent of overselling slightly
 		// 2. We will bid for the third slot and try to maintain that position
 		// this is from our observations in class about how people drop out.
 		// This simplifies the problem for the time being.
 		// 3. Capacity is taken into consideration
 		
 		//sum up conversions from last 5 days
 		int conv = 0;
 		for(int i = 0; i < 5; i++) {
 			conv += _totalConversions[i];
 		}
 		
 		int capdiff = conv - _capacity;
 		
 		_quotas.add(QUOTA);
 		
 		debug("\n\n\n CAPDIFF: " + capdiff);
 		
 		//This is the amount that our capacity will be reduced due too being over capacity
 		double capratio = Math.pow(lambda,Math.max(capdiff, 0));
 		
 //		debug("\n\n\n"+"Recenconv pre:" + recentConvHist);
 //		
 //		if(capdiff > 70) {
 //			if(recentConvHist >= 2) {
 //				debug("CHANGED QUOTA BY -.01");
 //				QUOTA -= .01;
 //				recentConvHist = 1;
 //			}
 //			else if(recentConvHist < 0) {
 //				recentConvHist = 1;
 //			}
 //			else {
 //				recentConvHist++;
 //			}
 //		}
 //		else if(capdiff < -10) {
 //			if(recentConvHist <= -2) {
 //				debug("CHANGED QUOTA BY +.01");
 //				QUOTA += .01;
 //				recentConvHist = -1;
 //			}
 //			else if(recentConvHist > 0) {
 //				recentConvHist = -1;
 //			}
 //			else {
 //				recentConvHist--;
 //			}
 //		}
 //		else {
 //			recentConvHist = 0;
 //		}
 //		debug("Recenconv post:" + recentConvHist);
 //		debug("Cap difference: " + capdiff);
 //		debug("Quota: " + QUOTA + "\n\n\n");
 		
 		// For each query, figure out how much to devote to that specific query
 		for(Query query:_querySpace) {
 			//get the most recent weights
 			HashMap<Query,Double> weights = _allweights.get(_allweights.size()-1);
 			//Determine how many conversions we are looking for in this query
 			int quota = (int) Math.ceil(weights.get(query) * _capacity * QUOTA );
 		
 			// How many clicks does it take to get to that point?
 			int clicks = 0;
 			
 			if(ourspecialty.contains(query)) {
 				if(query.getType() == QueryType.FOCUS_LEVEL_ZERO)
 					clicks = (int) (quota/eta(Constants.CONVERSION_F0*capratio,1+CSB));
 				else if(query.getType() == QueryType.FOCUS_LEVEL_ONE)
 					clicks = (int) (quota/eta(Constants.CONVERSION_F1*capratio,1+CSB));
 				else if(query.getType() == QueryType.FOCUS_LEVEL_TWO)
 					clicks = (int) (quota/eta(Constants.CONVERSION_F2*capratio,1+CSB));
 				else
 					System.exit(0); // Death condition, just to be sure
 			}
 			else {
 				if(query.getType() == QueryType.FOCUS_LEVEL_ZERO)
 					clicks = (int) (quota/(Constants.CONVERSION_F0*capratio));
 				else if(query.getType() == QueryType.FOCUS_LEVEL_ONE)
 					clicks = (int) (quota/(Constants.CONVERSION_F1*capratio));
 				else if(query.getType() == QueryType.FOCUS_LEVEL_TWO)
 					clicks = (int) (quota/(Constants.CONVERSION_F2*capratio));
 				else
 					System.exit(0); // Death condition, just to be sure
 			}
 			
 			// How much does it cost to get to that point?
 			//We multiply by cheapness to reflect the fact that we are actually going to multiply our bid by that
 			//we add a cheapness into our budget to reflect the fact that we are actually going to pay less per click than our bid
 			double budget = clicks*_bids.get(query)*CHEAPNESS*BUDGETCHEAPNESS;
 			
 			Ad ad;
 			ad = new Ad();  // Default generic
 			
 			// Set the limit
 			
 			boolean queryComponent = (query.getComponent() != null);
 			boolean queryManu = (query.getManufacturer() != null);
 			
 			//Determine whether or not to target an add
 			if(queryComponent || queryManu) {
 				if(queryComponent && queryManu) {
 					// If query is F2 and both manu and component match our specialty, target
 					if(query.getComponent().equals(_advertiserInfo.getComponentSpecialty()) && query.getManufacturer().equals(_advertiserInfo.getManufacturerSpecialty())) {
 						Product product = new Product(_advertiserInfo.getManufacturerSpecialty(),_advertiserInfo.getComponentSpecialty());
 						ad = new Ad(product);
 					}
 				} else {
 					if( (queryComponent && query.getComponent().equals(_advertiserInfo.getComponentSpecialty())) ||
 							(queryManu && query.getManufacturer().equals(_advertiserInfo.getManufacturerSpecialty())) ) {
 						// Otherwise, if either component or manu matches our speciality, then target with some probability we pulled out of nowhere
 						if(randDouble(0, 1) > 0.5 ) {
 							Product product = new Product(_advertiserInfo.getManufacturerSpecialty(),_advertiserInfo.getComponentSpecialty());
 							ad = new Ad(product);							
 						}
 					}
 				}
 			}
 			
 			bidBundle.addQuery(query, _bids.get(query)*CHEAPNESS, ad, budget);	
 		}
 		
 		// There is no whole limit, as we set limits on the individual parts
 		return bidBundle;
 	}
 
 	@Override
 	protected void initBidder() {
 
 		debug("===================================");
 		debug("Initializing bidder");
 		debug("===================================");
 		
 		CSB = _advertiserInfo.getComponentBonus();
 		MSB = _advertiserInfo.getManufacturerBonus();
 		manufacturerSpecialty = _advertiserInfo.getManufacturerSpecialty();
 		ourspecialty = _queryManufacturer.get(manufacturerSpecialty);
 		_capacity = _advertiserInfo.getDistributionCapacity();
 		_window = _advertiserInfo.getDistributionWindow();
 		
 		_allweights = new ArrayList<HashMap<Query, Double>>();
 		_allconversions = new ArrayList<HashMap<Query, Double>>();
 		_percenterror = new ArrayList<HashMap<Query, Double>>();
 		_bids = new HashMap<Query, Double>();
 		_oldprices = new HashMap<Query, Double>();
 		_goalpositions = new HashMap<Query, Double>();
 		_totalConversions = new int[5];
 		
 		_quotas = new ArrayList<Double>();
 		
 		recentConvHist = 0;
 		
 		_totalConversions[0] = _capacity/5;
 		_totalConversions[1] = _capacity/5;
 		_totalConversions[2] = _capacity/5;
 		_totalConversions[3] = 0;
 		_totalConversions[4] = 0;
 		
 		// Initialize all of the queries to one
 		// Then normalize
 		
 		HashMap<Query,Double> weights = new HashMap<Query, Double>();
 		
 		for(Query query: _querySpace) {
 			_oldprices.put(query, 1.0);
 			//our goal position for our specialty is slightly higher
 			if (ourspecialty.contains(query)) {
 				//We weight our specialty slightly higher (.5 extra)
 				weights.put(query, 1.0 + MSB);
 				_goalpositions.put(query,3.0);
 			}
 			else {
 				weights.put(query, 1.0);
 				_goalpositions.put(query,4.0);
 			}
 		}
 		
 		normalizeWeights(weights);
 		//We add it in twice because we don't update our bidding strategy on the first or second day
 		_allweights.add(weights);
 		_allweights.add(weights);
 		  
 		// Initialize the bids to slightly below the honest value
 		// Using the "cheapness" value
 		// From observations, we've seen that people tend to sell out and drop out
 		// even on the first day. We will use the cheapness value until we get 
 		// more information from reports (when we can use different heuristics)
 
 	  _maxF0Bid = Constants.CONVERSION_F0*Constants.SALE_VALUE;
 	  _maxF1Bid = Constants.CONVERSION_F1*Constants.SALE_VALUE;
 	  _maxF2Bid = Constants.CONVERSION_F2*Constants.SALE_VALUE;
 
 		for(Query query: _querySpace) {
 			if(query.getType() == QueryType.FOCUS_LEVEL_ZERO) 
 				_bids.put(query, _maxF0Bid * INITIAL_CHEAPNESS);
 			else if(query.getType() == QueryType.FOCUS_LEVEL_ONE) 
 				_bids.put(query, _maxF1Bid * INITIAL_CHEAPNESS);
 			else if(query.getType() == QueryType.FOCUS_LEVEL_TWO) 
 				_bids.put(query, _maxF2Bid * INITIAL_CHEAPNESS);
 			
 			debug("Initial bid: " + query + " = " +_bids.get(query));
 		}
 		
 		
 	}
 
 	@Override
 	protected void updateBidStrategy() {
 
 		debug("===================================");
 		debug("Updating bidding strategy");
 		debug("===================================");
 
 		QueryReport queryReport = _queryReports.poll();
 		SalesReport salesReport = _salesReports.poll();
 
 
 		if(!(queryReport == null || salesReport == null)) {
 			if(_currentday > 1) {
 				HashMap<Query,Double> relatives = new HashMap<Query, Double>();
 				HashMap<Query,Double> c = new HashMap<Query,Double>();
 				HashMap<Query,Double> percenterror = new HashMap<Query,Double>();
 				int conversions = 0;
 
 				for(Query query:_querySpace) {
 					double conv = salesReport.getConversions(query);
 					c.put(query, conv);
 					conversions += conv;
 				}
 
 				_allconversions.add(c);
 
 				_totalConversions[0] = _totalConversions[1];
 				_totalConversions[1] = _totalConversions[2];
 				_totalConversions[2] = _totalConversions[3];
 				_totalConversions[3] = _totalConversions[4];
 				_totalConversions[4] = conversions;
 
 				relatives = getRelatives(queryReport, salesReport);
 
 
 				HashMap<Query,Double> weights = EGUpdate(_allweights.get(_allweights.size()-1),relatives);
 				_allweights.add(weights);
 
 
 				// Debug messages
 				for(Query query:_querySpace) {
 					debug("Original weight: " + query + " = " + _allweights.get(_allweights.size()-2).get(query));
 					debug("New weight: " + query + " = " + weights.get(query));
 				}
 
 				// Update bids based on how close we were to getting the weights right 2 days ago
 				for(Query query:_querySpace) {
 
 					debug("Original bid: " + query + " = " + _bids.get(query));
 
 					//If we are close to the slot that we want to be in we just slowly move down
 					//If we are in a higher slot we bid more
 					//If we are in a lower slot we bid less
 					HashMap<Query,Double> oldconvs = _allconversions.get(_currentday-2);
 					HashMap<Query,Double> oldweights = _allweights.get(_currentday-2);
 					
 					double oldconv = oldconvs.get(query);
 					double oldweight = oldweights.get(query);
 					double oldgoalconv = (int) Math.ceil(oldweight * _capacity * _quotas.get(_currentday-2));
 					
 					double percerror = ((oldconv-oldgoalconv)/oldgoalconv);
 					
 					percenterror.put(query, percerror);
 					
 					
 					double leniency = .15;
 
 					if(oldconv == 0)
 						_bids.put(query, _bids.get(query) * 1.3);
 					else if(percerror < leniency && percerror > -1*leniency)
 						_bids.put(query, _bids.get(query) * randDouble(.95,1.0));
 					else if(percerror < 0)
 						_bids.put(query, _bids.get(query) * INC_RATE);
 					else 
 						_bids.put(query, _bids.get(query) * DEC_RATE);
 						
     			if(query.getType() == QueryType.FOCUS_LEVEL_ZERO)
     			  _bids.put(query, Math.min(_maxF0Bid, _bids.get(query)));
     			else if(query.getType() == QueryType.FOCUS_LEVEL_ONE)
     			  _bids.put(query, Math.min(_maxF1Bid, _bids.get(query)));
     			else if(query.getType() == QueryType.FOCUS_LEVEL_TWO)
     			  _bids.put(query, Math.min(_maxF2Bid, _bids.get(query)));
 					
 					debug("New bid: " + query + " = " + _bids.get(query));
 				}
 				
 				_percenterror.add(percenterror);
 				for(Query query: _querySpace) {
 					debug("Percent error for " + query.toString() + ":  " + percenterror.get(query));
 				}
 				
 			}
 		}
 		else {
 			System.out.println("\n\n\n\n\n QUERY REPORT NULL!!!!! \n\n\n");
 			throw new RuntimeException("Query or Sales Report Null");
 		}
 	}
 
 	/*
 	 * Given a vector of Query-Weights
 	 * Or rather, weights in the portfolio,
 	 * this function will normalize the sum of the
 	 * weights.
 	 */
 	protected void normalizeWeights(HashMap<Query, Double> weights) {
 		double total = 0;
 		for(Query query:weights.keySet())
 			total += weights.get(query);
 		
 		for(Query query:weights.keySet()) {
 			double value = weights.get(query)/total;
 			weights.put(query, value);
 		}
 	}
 	
 	protected static void debug(Object o) {
 		if(Constants.DEBUG)
 			System.out.println(o.toString());
 	}
 	
 	//Returns a random double rand such that a <= r < b
 	protected double randDouble(double a, double b) {
 		double rand = _R.nextDouble();
 		return rand * (b - a) + a;
 	}
 	
 	protected double eta(double p, double x) {
 		return (p*x) / (p*x + (1-p));
 	}
 	
 	private HashMap<Query, Double> getRelatives(QueryReport queryReport, SalesReport salesReport) {
 		HashMap<Query,Double> newprices = new HashMap<Query,Double>();
 		HashMap<Query,Double> relatives = new HashMap<Query, Double>();
 		for(Query query:_querySpace) {
 			// Zero case, not in the query at all
 			// We don't want to not explore this at all, so for the time being
 			// we will set it to 1.
 			if( Math.abs( queryReport.getCost(query) ) <= .001 || 
 					salesReport.getRevenue(query) <= .001) {
 				newprices.put(query, 1.0);
 			}
 			// Otherwise we put the actual relative in there
 			else {
 				newprices.put(query,salesReport.getRevenue(query) / queryReport.getCost(query));
 			}
 			relatives.put(query, newprices.get(query));
 //			_oldprices.put(query,newprices.get(query));
 			System.out.println("\n\n"+"*************"+"\n"+relatives.get(query));
 		}
 		return relatives;
 	}
 	
 	protected HashMap<Query,Double> EGUpdate(HashMap<Query,Double> weights, HashMap<Query,Double> relatives) {
 		// Update weights using the EG algorithm
 		// First get the dot product
 		double dotProduct = 0;
 		for(Query query:_querySpace)  {
 			dotProduct += weights.get(query)*relatives.get(query);
 		}
 		
 		// Now get the denominator
 		double totalWeights = 0;
 		for(Query query:_querySpace) {
 			totalWeights += weights.get(query)*Math.exp((LEARNING_RATE)*relatives.get(query)/dotProduct);
 		}
 		
 		// Now update the weights
 		for(Query query:_querySpace) {
 			weights.put(query, (weights.get(query)*Math.exp((LEARNING_RATE)*relatives.get(query)/dotProduct))/totalWeights);
 		}
 		return weights;
 	}
 	
 	protected void handleSimulationStatus(SimulationStatus simulationStatus) {
 		_currentday++;
 		if(_firstDay){
 			_firstDay = false;
 			_currentday = 0;
 			initBidder();
 		}
 		sendBidAndAds();
 	}
 	
 }
