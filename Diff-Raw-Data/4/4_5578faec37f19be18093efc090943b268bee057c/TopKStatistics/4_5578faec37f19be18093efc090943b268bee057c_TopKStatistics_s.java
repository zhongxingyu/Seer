 package prefwork.rating.test;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import prefwork.core.Utils;
 import weka.core.Summarizable;
 
 public class TopKStatistics extends TestInterpreter {
 
 	TestResults testResults;
 	int run;
 	int count, positionsSum;
 	double ndcg;
 	int topk[];
 	int actualTopK;
 	@Override
 	synchronized public void writeTestResults(TestResults testResults) {
 		this.testResults = testResults;
 		//writeRawRatings(testResults,filePrefix+"ratings",headerPrefix,rowPrefix);
 		try {
 			testResults.processResults();
 			File f = new File(filePrefix + ".csv");
 			BufferedWriter out;
 			if (!f.exists()) {
 				out = new BufferedWriter(new FileWriter(filePrefix + ".csv",
 						true));
 				out
 						.write(headerPrefix
 								+ "userId;topk;run;topkCount;topkRatio;ndcg;mae;buildTime;testTime;countTrain;countTest;countUnableToPredict;\n");
 			} else
 				out = new BufferedWriter(new FileWriter(filePrefix + ".csv",
 						true));
 			for (Integer userId : testResults.getUsers()) {
 				List<Stats> l = testResults.getListStats(userId);
 				for (int i = 0; i < l.size(); i++) {
 					//Iterate over different top-k sizes
 					for (int j = 0; j < topk.length; j++) {
 						actualTopK = topk[j];
 						run = i;
 						Stats stat = testResults.getStatNoAdd(userId, run);
 						if (stat == null)
 							continue;
 						computeTopK(stat);
 						//getConcordantDiscordant(stat);
 						// TODO upravit
 						
 						out
 								.write((rowPrefix + userId + ";" + actualTopK  + ";" + run + ";" 
 										+ count + ";"
 										+ positionsSum + ";"
 										+ ndcg + ";"
 										+ computeMae(stat) + ";"
 										+ stat.buildTime + ";" + stat.testTime
 										+ ";" + stat.countTrain + ";"
 										+ stat.countTest + ";"
 										+ stat.countUnableToPredict + ";"+ "\n")
 										.replace('.', ','));
 					}
 				}
 			}
 			out.flush();
 			out.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Counts the number of times an ordered object is in topk list.
 	 * @param stat
 	 * @return
 	 */
 	private void computeTopK(Stats stat) {
 		Set<Entry<Integer, Double[]>> set = stat.getSet();
 		if (set == null || set.size() <= 0)
 			return;
 
 		CompareRatings cd = new CompareRatings();
 		Rating[] array2;
 		/** Array of users ratings.*/
 		array2 = DataMiningStatistics.getArray(set, 0, stat.unableToPredict);
 		
 		Rating[] array1;
 		/** Array of methods ratings.*/
 		array1= DataMiningStatistics.getArray(set, 1, null);
 		java.util.Arrays.sort(array1, cd);
 		
 		/*Compute nCDG*/
 		Double[] arr = new Double[array1.length];
 		for (int i = 0; i < array1.length; i++) {
 			if(array1[i]==null)
 				continue;
 			int j = DataMiningStatistics.findObject(array1[i].objectId, array2);
 			arr[i] = array2[j].rating;
 		}
 		
 		ndcg = ndcg(arr, actualTopK);
 		
 		
 		array1 = java.util.Arrays.copyOf(array1, Math.min(actualTopK, array1.length));
 		//array1 = setWeights(array1);
 		java.util.Arrays.sort(array2, cd);
 		//array2 = setWeights(array2);
 		//array2 = java.util.Arrays.copyOf(array2, Math.min(20, array2.length));
 		
 		count = 0;
 		positionsSum = 0;
 		for (int i = 0; i < array1.length; i++) {
 			if(array1[i]==null)
 				break;
 			int j = DataMiningStatistics.findObject(array1[i].objectId, array2);
 			if(j != -1 && array2[j].rating == 1.0){
 				count++;
 				positionsSum+=i+1;
 			}
 		}
 		if(positionsSum == 0)
 			positionsSum = array1.length+1;
 		if(count != 0)
 			positionsSum /= count;
 
 	}
 
 	@Override
 	public void configTestInterpreter(XMLConfiguration config, String section) {
 		// TODO Auto-generated method stub
 		Configuration testConf = config.configurationAt(section);
 		topk = Utils.stringListToIntArray(testConf.getList("topk"));
 		
 	}
 	
 
     // the max grade
     private static double G_MAX = 5;
 
     public static double lex(Double[] orgRanking) {
             Double[] ranking = new Double[orgRanking.length];
             for(int i = 0 ; i < orgRanking.length ; i++){
                     ranking[i] = orgRanking[i];
             }
             double feedbackScore = 0;
             for (int i = 9; i >= 0; i--) {
                     int idx = 9 - i;
                     double currScore = Math.pow(2, i) * ranking[idx];
                     feedbackScore += currScore;
                     if (idx == ranking.length - 1) {
                             return feedbackScore;
                     }
             }
             return feedbackScore;
     }
    
     /**
      * Calculates the DCG (Discounted cumulative gain) score for the specified ranking evaluation.
      *
      * @param ranking
      *            the ranking evaluation
      * @return the DCG score for the ranking
      */
     public static double dcg(Double[] ranking) {
             double score = ranking[0];
             for (int i = 1; i < ranking.length; i++) {
                     score += calcDcgForPos(ranking[i], i + 1);
             }
             return ranking[0] + score;
     }
 
     /**
      * Calculates the DCG (Discounted cumulative gain) score for the specified evaluation score at the specified rank
      * position.
      *
      * @param score
      *            the evaluation score
      * @param rank
      *            the rank position
      * @return the DCG score
      */
     private static double calcDcgForPos(double score, int rank) {
             double log2Pos = Math.log(rank) / Math.log(2);
             return score / log2Pos;
     }
 
     /**
      * Calculates the NDCG (Normalized discounted cumulative gain) score for the specified ranking evaluation.
      *
      * @param ranking
      *            the ranking evaluation
      * @return the NDCG score for the ranking
      */
     public static double ndcg(Double[] orgRanking, int topk) {
             Double[] ranking = new Double[Math.min(topk,orgRanking.length)];
             for(int i = 0 ; i < orgRanking.length && i < topk ; i++){
                     ranking[i] = orgRanking[i];
             }
             double dcg = dcg(ranking);
             double idcg = dcg(reverseSortDesc(ranking));
             if(idcg == 0.0)
             	return 0;
             return  dcg/idcg ;
     }
 
     /**
      * Reverse sorts the specified array in descending order based on the evaluation value.
      *
      * @param ranking
      *            the ranking evaluation
      * @return the sorted array
      */
     public static Double[] reverseSortDesc(Double[] ranking) {
             Double[] reverseSorted = new Double[ranking.length];
             Arrays.sort(ranking);
             int arrLen = ranking.length;
             for (int i = arrLen - 1; i >= 0; i--) {
                     int pos = arrLen - 1 - i;
                     reverseSorted[pos] = ranking[i];
             }
             return reverseSorted;
     }
    
     /**
      * Calculates the ERR (Expected Reciprocal Rank).
      *
      * @param ranking
      *            the ranking evaluation
      * @return the ERR score
      */
     public static double err(Double[] orgRanking) {
             Double[] ranking = new Double[orgRanking.length];
             for(int i = 0 ; i < orgRanking.length ; i++){
                     ranking[i] = orgRanking[i];
             }
             double p = 1;
             double errScore = 0;
             int n = ranking.length;
             for (int r = 1; r <= n; r++) {
                     double g = ranking[r - 1];
                     double rg = (Math.pow(2, g) - 1) / Math.pow(2, G_MAX);
                     errScore += p * (rg / r);
                     p = p * (1 - rg);
             }
             return errScore;
     }
     private String computeMae(Stats stat) {
 		Set<Entry<Integer, Double[]>> set = stat.getSet();
 		if (set == null || set.size() <= 0)
 			return "0";
 		double mae = 0;
 		for (Entry<Integer, Double[]> entry : set) {
 			mae += Math.abs(entry.getValue()[1] - entry.getValue()[0]);
 		}
 		mae /= set.size();
 		double stdDevmae = 0;
 		for (Entry<Integer, Double[]> entry : set) {
 			stdDevmae += Math.abs(mae- Math.abs(entry.getValue()[1] - entry.getValue()[0]));
 		}
 
 		if(mae > 1000)
 			return  ""+ 1000 ;
 		
 		return  ""+ mae ;
 	}
 
 }
 
