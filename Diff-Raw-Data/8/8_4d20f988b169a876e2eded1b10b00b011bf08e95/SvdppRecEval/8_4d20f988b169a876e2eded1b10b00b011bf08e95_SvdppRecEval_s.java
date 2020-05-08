 package rec.collaborative.eval;
 
 import java.util.ArrayList;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
 import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
 import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
 import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 
 /*
  * 
  * Evaluationsklasse für SVD++
  * 
  */
 
public class SvdppRecEval {
 
 	DataModel dataModel;
 	
 	public SvdppRecEval(){
 		dataModel = rec.database.MySQLConnection
 				.getDatamodellFromDatabase(); //Datenmodell initialisieren
 	}
 	
 	/*
 	 * Methode zur evaluation der optimalen Anzahl an Iterationen
 	 * @param maxiter maximale Anzahl an iterationen die geprüft werden sollen
 	 * @param dim Anzahl an dimensionen, fest für jeden Testdurchlauf
 	 * @param Anzahl an Wiederholungen für jeden Iterationswert
 	 */
 	public void evalIterations(int maxiter, int dim,  int wdh) {
 		RMSRecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
 		evaluator.setMaxPreference(5);
 		evaluator.setMinPreference(1);
 		ArrayList<Double> ergebnisse = new ArrayList<Double>();
 		try {
 			double rmse = 0;
 			for (int j = 1; j <= maxiter; j++) {
 				for (int i = 0; i < wdh; i++) {
 					rmse = rmse
 							+ evaluator.evaluate(createRecBuilder(dim, j), null,
 									dataModel, 0.7, 1.0);
 				}
 				ergebnisse.add(rmse / wdh); //Durchschnittlichen RMSE für aktuellen Iterationswert berechnen und Ergebnisliste hinzufügen
 				rmse = 0;
 			}
 			for(int i=0; i < ergebnisse.size(); i++){
				System.out.println("Iterationen: " + i+1 + " RMSE: " + ergebnisse.get(i));
 			}
 		} catch (TasteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Methode zur evaluation der optimalen Anzahl an Iterationen
 	 * @param maxdim maximale Anzahl an Dimensionen die geprüft werden sollen
 	 * @param iter Anzahl an Iterationen, fest für jeden Testdurchlauf
 	 * @param Anzahl an Wiederholungen für jeden Dimsensionswert
 	 */
 	public void evalDimensions(int maxdim, int iter, int wdh) {
 		RMSRecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
 		evaluator.setMaxPreference(5);
 		evaluator.setMinPreference(1);
 		ArrayList<Double> ergebnisse = new ArrayList<Double>();
 		try {
 			double rmse = 0;
 			for (int j = 1; j <= maxdim; j++) {
 				for (int i = 0; i < wdh; i++) {
 					rmse = rmse
 							+ evaluator.evaluate(createRecBuilder(j, iter), null,
 									dataModel, 0.7, 1.0);
 				}
 				ergebnisse.add(rmse / wdh); //Durchschnittlichen RMSE für aktuellen Dimsensionswert berechnen und Ergebnisliste hinzufügen
 				rmse = 0;
 			}
 			for(int i=0; i < ergebnisse.size(); i++){
				System.out.println("Dimensionen: " + i+1 + " RMSE: " + ergebnisse.get(i));
 			}
 		} catch (TasteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public RecommenderBuilder createRecBuilder(final int dim,
 			final int iter) { // Konstanten wegen Zugriff aus der anonymen
 									// Klasse
 		return new RecommenderBuilder() {
 			@Override
 			public Recommender buildRecommender(DataModel dataModel)
 					throws TasteException {
 				return new SVDRecommender(dataModel, new SVDPlusPlusFactorizer(
 						dataModel, dim, iter));
 			}
 		};
 	}
 
 }
