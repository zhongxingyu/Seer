 package edu.ntnu.idi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
 import org.apache.mahout.cf.taste.example.grouplens.GroupLensDataModel;
 import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
 import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
 import org.apache.mahout.cf.taste.recommender.RecommendedItem;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.cf.taste.similarity.UserSimilarity;
 import org.apache.mahout.common.RandomUtils;
 
 
 public class Main {
 	
 	public static List<RecommendedItem> getRecommendations(int numberOfRatings, int userId, Recommender recommender) throws TasteException{
 		Recommender cachingRecommender = new CachingRecommender(recommender);
 		
 		List<RecommendedItem> recommendations = cachingRecommender.recommend(userId, numberOfRatings);
 	
 		return recommendations;
 	}
 	
 	public static GenericUserBasedRecommender createRecommender(String ratings, int neighborhoodSize) 
 			throws TasteException, IOException {
 		FileDataModel movielens = new GroupLensDataModel(new File(ratings));
 		
 		UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(movielens);
 		
 		UserNeighborhood neighborhood = new NearestNUserNeighborhood(neighborhoodSize, userSimilarity, movielens);
 		
 		return new GenericUserBasedRecommender(movielens, neighborhood, userSimilarity);
 	}
 	
 	public static void printRecommendations(List<RecommendedItem> recommendations) {
 		System.out.println("");
 		
 		for (RecommendedItem recommendedItem : recommendations) {
 			System.out.format("Item recommended : %d with value : %f \n", recommendedItem.getItemID(), 
 					recommendedItem.getValue());
 		}
 		
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws TasteException 
 	 */
 	public static void main(String[] args) throws TasteException, IOException  {
 		// creates a generic recommender with the ratings set from data and a neighborhood size of 50
		GenericUserBasedRecommender recommender = createRecommender("data/movielens-1m/ratings.dat.gz", 50);
 		
 		// uses the above recommender to get 20 recommendations (top-20) for user with id 100
 		List<RecommendedItem> recommendations = getRecommendations(20, 33, recommender);
 		
 		// prints the recommendations to console
 		printRecommendations(recommendations);
 		
 		}
 	
 	public static void evaluateRecommender() throws IOException, TasteException {
 		RandomUtils.useTestSeed();
		DataModel model = new GroupLensDataModel(new File("data/movielens-1m/ratings.dat.gz"));
 		
 		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
 		
 		RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
 			
 			public Recommender buildRecommender(DataModel model) throws TasteException {
 				UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
 				UserNeighborhood neighborhood = new NearestNUserNeighborhood(100, similarity, model);
 				return new GenericUserBasedRecommender(model, neighborhood, similarity);
 				}
 			};
 		
 		double score = evaluator.evaluate(recommenderBuilder, null, model, 0.8, 0.2);
 		System.out.println(score);
 	}
 }
