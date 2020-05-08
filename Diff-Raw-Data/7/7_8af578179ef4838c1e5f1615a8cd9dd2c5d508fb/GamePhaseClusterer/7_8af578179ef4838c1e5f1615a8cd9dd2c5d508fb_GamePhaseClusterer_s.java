 package game_phase;
 
 import java.util.ArrayList;
 
 import utilities.GameData;
 import utilities.GameParser;
 import utilities.helper_classes.GameInfo;
 import utilities.helper_classes.Utilities;
 
 public class GamePhaseClusterer {
 	
	public static final int NUM_GAMES = 5;
 	public static final int CLUSTERS = 3;
 	public static final int ITERATIONS = 100;
 
 	public static void main(String args[]){
 		GameData myGameData = new GameData(NUM_GAMES, 0.99);
 		Utilities.printInfo("Finished fetching game data");
 		ArrayList<double[]> trainMatrix = new ArrayList<double[]>();
 		
 		int count = 0;
 		while (myGameData.hasNextGame()){
 			final long startTime = System.currentTimeMillis();
 			
 			Utilities.printInfoInline("Training on game # " + ++count + "..."); //time will be appended in-line
 			GameInfo trainGameInfo = myGameData.getNextGame();
 			GameParser myParser = new GameParser(trainGameInfo);
 			
 			while (myParser.hasNextGameState()){
 				trainMatrix.add( FeatureExtractor.extractFeatures(myParser.getNextGameState().getCurr()) );
 			}
 			
 			final long endTime = System.currentTimeMillis();
 			Utilities.printInfo("training took " + Utilities.msToString(endTime - startTime));
 		}
 		
 		double[][] matrix = trainMatrix.toArray(new double[1][1]);
 		KMeansWrapper kmeans = new KMeansWrapper(CLUSTERS, ITERATIONS, matrix);
 		kmeans.cluster();
 		double[][] centers = kmeans.centroids();
 		
 		for(int j = 0; j < centers[0].length; j++){
 			System.out.print(j + "\t");
 			for(int i = 0; i < centers.length; i++){
				System.out.print(i + " " );
 			}
 			System.out.println("");
 		}
 	}
 
 }
