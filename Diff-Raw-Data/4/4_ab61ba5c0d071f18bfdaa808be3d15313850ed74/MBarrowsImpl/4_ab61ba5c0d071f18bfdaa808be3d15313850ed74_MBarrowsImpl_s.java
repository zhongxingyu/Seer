 package models.mbarrows;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 
 import models.AbstractModel;
 import models.usermodel.TacTexAbstractUserModel.UserState;
 import edu.umich.eecs.tac.props.Ad;
 import edu.umich.eecs.tac.props.Product;
 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 import edu.umich.eecs.tac.props.QueryType;
 import edu.umich.eecs.tac.props.SalesReport;
 
 public class MBarrowsImpl extends AbstractMaxBarrows {
 	
 	private ArrayList<Query> m_queries;
 	private HashMap<Query, Double> m_continuationProbs;
 	private HashMap<Query, Double[]> m_advertiserEffects;
 	private String m_ourname;
 	
 	public MBarrowsImpl(String us){
 		 m_queries.add(new Query(null, null));
 		 m_queries.add(new Query("lioneer", null));
 		 m_queries.add(new Query(null, "tv"));
 		 m_queries.add(new Query("lioneer", "tv"));
 		 m_queries.add(new Query(null, "audio"));
 		 m_queries.add(new Query("lioneer", "audio"));
 		 m_queries.add(new Query(null, "dvd"));
 		 m_queries.add(new Query("lioneer", "dvd"));
 		 m_queries.add(new Query("pg", null));
 		 m_queries.add(new Query("pg", "tv"));
 		 m_queries.add(new Query("pg", "audio"));
 		 m_queries.add(new Query("pg", "dvd"));
 		 m_queries.add(new Query("flat", null));
 		 m_queries.add(new Query("flat", "tv"));
 		 m_queries.add(new Query("flat", "audio"));
 		 m_queries.add(new Query("flat", "dvd"));
 		 
 		 //This string should be equivalent to the strings in advertisersAbovePerSlot, whatever those turn out to be.
 		 //In other words, it should be a valid key for ads in the updateModel method
 		 m_ourname = us;
 		 
 		 for (Query q : m_queries){
 				QueryType qt = q.getType();
 				Double [] advertiserEffects = new Double[8];
 				if(qt==QueryType.FOCUS_LEVEL_TWO){
 					 //For each query, store the average continuation probabilities
 					m_continuationProbs.put(q, 0.55);
 					 //For each query, store the average advertiser effects
 					for(int i = 0; i < advertiserEffects.length; i++){
 						advertiserEffects[i] = 0.45;
 					}
 					m_advertiserEffects.put(q, advertiserEffects);
 				}
 				if(qt==QueryType.FOCUS_LEVEL_ONE){
 					 //For each query, store the average continuation probabilities
 					m_continuationProbs.put(q, 0.45);
 					 //For each query, store the average advertiser effects
 					for(int i = 0; i < advertiserEffects.length; i++){
 						advertiserEffects[i] = 0.35;
 					}
 					m_advertiserEffects.put(q, advertiserEffects);
 				}
 				if(qt==QueryType.FOCUS_LEVEL_ZERO){
 					 //For each query, store the average continuation probabilities
 					m_continuationProbs.put(q, 0.35);
 					 //For each query, store the average advertiser effects
 					for(int i = 0; i < advertiserEffects.length; i++){
 						advertiserEffects[i] = 0.25;
 					}
 					m_advertiserEffects.put(q, advertiserEffects);
 				}
 		 }
 	}
 
 	@Override
 	public double[] getPrediction(Query q) {
 		Double[] toconvert = m_advertiserEffects.get(q);
 		double[] toreturn = new double[toconvert.length];
 		for (int index = 0; index < toconvert.length; index++){
 			toreturn[index]=toconvert[index];
 		}
 		return toreturn;
 	}
 
 	@Override
 	public boolean updateModel(QueryReport queryReport,
 			SalesReport salesReport, LinkedList<Integer> impressionsPerSlot,
 			LinkedList<LinkedList<String>> advertisersAbovePerSlot,
 			HashMap<String, Ad> ads,
 			HashMap<Product, HashMap<UserState, Double>> userStates) {
 		
 		assert (impressionsPerSlot.size() == advertisersAbovePerSlot.size());
 		
 		//For each query
 		for (Query q: m_queries){
 			//For each slot
 			for (int slot = 0; slot < impressionsPerSlot.size(); slot++){
 				LinkedList<String> aboveme = advertisersAbovePerSlot.get(slot);
 				double impressions = impressionsPerSlot.get(slot);
 				//If in top slot, estimate advertiser effect
 				if(aboveme.size()==0){
 					HashMap<Product, Double> userDist = estimateUserDist(userStates);
 					HashMap<Product, Double> impDist = estimateImpressionDist(userDist, impressions);
 					//This is a hack. Ideally we want click distributions over slots.
 					HashMap<Product, Double> clickDist = estimateImpressionDist(userDist, queryReport.getClicks(q));
 					//Click probabilities should be uniform for now due to above hack.
 					//Impressions = views in this case
 					HashMap<Product, Double> clickProb = estimateProbClick(impDist,clickDist);
 					//Note:  Need to check all divisions of ints for rounding errors
 					double averageProbClick = 1.0*(queryReport.getClicks(q)/impressions);
 					Ad ourad = ads.get(m_ourname);
 					//I don't know if this is a valid way of determining whether an ad was promoted.
 					boolean promoted = false;
 					if(queryReport.getPromotedImpressions(q)>queryReport.getRegularImpressions(q)){
 						promoted = true;
 					}
 					double eq_us = calculateAdvertiserEffect(userDist,clickProb,averageProbClick,(!ourad.isGeneric()),promoted,ourad.getProduct(),q);
 					//I'm unsure about how to order the advertisers in an array, so here I assume that we are first
 					Double [] ad_effects = m_advertiserEffects.get(q);
 					ad_effects[0]=eq_us;
 					m_advertiserEffects.put(q, ad_effects);
 				}else{
 					//If not, estimate conversion probability
 					//Hacky, hard coded values
 					double convProb = 0.11;
 					if(q.getType()==QueryType.FOCUS_LEVEL_TWO){
 						convProb = 0.36;
 					}
 					if(q.getType()==QueryType.FOCUS_LEVEL_ONE){
 						convProb = 0.23;
 					}
 					//Pick a good value for continuation prob based on that
 				
 				}
 			}
 		}
 		
 		
 		return false;
 	}
 	
 	public static double calculateAdvertiserEffect(HashMap<Product, Double> userDist, HashMap<Product, Double> probClick, double averageProbClick, boolean targeted, boolean promoted, Product target, Query q){
 		double fpro = 1.0;
 		if(promoted){
 			fpro = 1.5;
 		}
 		double TE = 0.5;
 		//If targeted
 		if(targeted){
 			//iterate over the user preferences
 			double eq = 0.0;
 			for(Product userpref:probClick.keySet()){
 				//calculate the component of the sum for users of this preference
 				if(target.equals(userpref)){
 					double prClick = probClick.get(userpref);
 					eq+=userDist.get(userpref)*(prClick/(prClick+(1.0+TE)*fpro-(1.0+TE)*prClick*fpro));
 				}else{
 					double prClick = probClick.get(userpref);
 					eq+=userDist.get(userpref)*(prClick/(prClick+(1/(1.0+TE))*fpro-(1/(1.0+TE))*prClick*fpro));
 				}
 				//sum to get estimate for advertiser effect
 			}
 			return eq;
 		}else{
 			//using probability of click for the entire user population,
 			//calculate estimate for advertiser effect
 			double eq = averageProbClick/(averageProbClick+fpro+averageProbClick*fpro);
 			return eq;
 		}
 	}
 	
 	public static double calculateProbClick(double views, double clicks){
 		return clicks/views;
 	}
 	
 	public static HashMap<Product, Double> estimateImpressionDist(HashMap<Product, Double> userDist, double impressions){
 		HashMap<Product,Double> toreturn = new HashMap<Product,Double>();
 		double sum = 0;
 		for(Product userpref:userDist.keySet()){
 			sum += userDist.get(userpref);
 		}
 		for(Product prod:userDist.keySet()){
 			double percentage = userDist.get(prod)/sum;
 			toreturn.put(prod, percentage*impressions);
 		}
 		return toreturn;
 	}
 	
 	public static HashMap<Product, Double> estimateUserDist(HashMap<Product, HashMap<UserState, Double>> userStates){
 		HashMap<Product,Double> toreturn = new HashMap<Product,Double>();
		double sum = 0;
 		for(Product userpref:userStates.keySet()){
 			for(UserState userstate:userStates.get(userpref).keySet()){
 				sum += userStates.get(userpref).get(userstate);
 			}
 			toreturn.put(userpref, sum);
 		}
 		return toreturn;
 	}
 	
 	public static HashMap<Product, Double> estimateProbClick(HashMap<Product, Double> viewDist, HashMap<Product, Double> clickDist){
 		HashMap<Product,Double> toreturn = new HashMap<Product,Double>();
 		for(Product userpref:viewDist.keySet()){
 			double views = viewDist.get(userpref);
 			if(views!=0){
 				toreturn.put(userpref, clickDist.get(userpref)/views);
 			}else{
 				toreturn.put(userpref, 0.0);
 			}
 		}
 		return toreturn;
 	}
 
 	@Override
 	public AbstractModel getCopy() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
