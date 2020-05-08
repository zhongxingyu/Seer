 package prefwork.rating.test;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
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
 	int topk;
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
 								+ "userId;run;topkCount;topkRatio;buildTime;testTime;countTrain;countTest;countUnableToPredict;\n");
 			} else
 				out = new BufferedWriter(new FileWriter(filePrefix + ".csv",
 						true));
 			for (Integer userId : testResults.getUsers()) {
 				List<Stats> l = testResults.getListStats(userId);
 				for (int i = 0; i < l.size(); i++) {
 					run = i;
 					Stats stat = testResults.getStatNoAdd(userId, run);
 					if (stat == null)
 						continue;
 					computeTopK(stat);
 					//getConcordantDiscordant(stat);
 					// TODO upravit
 					out
 							.write((rowPrefix + userId + ";" + run + ";" 
 									+ count + ";"
 									+ positionsSum + ";"
 									+ stat.buildTime + ";" + stat.testTime
 									+ ";" + stat.countTrain + ";"
 									+ stat.countTest + ";"
 									+ stat.countUnableToPredict + ";"+ "\n")
 									.replace('.', ','));
 
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
 	private Double computeTopK(Stats stat) {
 		Set<Entry<Integer, Double[]>> set = stat.getSet();
 		if (set == null || set.size() <= 0)
 			return 0.0D;
 
 		CompareRatings cd = new CompareRatings();
 		Rating[] array2;
 		/** Array of users ratings.*/
 		array2 = DataMiningStatistics.getArray(set, 0, stat.unableToPredict);
 		
 		Rating[] array1;
 		//Exclude unableToPredict
 		/** Array of methods ratings.*/
 		array1= DataMiningStatistics.getArray(set, 1, null);
 
 		java.util.Arrays.sort(array1, cd);
 		array1 = java.util.Arrays.copyOf(array1, Math.min(topk, array1.length));
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
 			if(j != -1 && array2[j].rating == 5.0){
 				count++;
				positionsSum+=i;
 			}
 		}
 		return 1.0*count;
 	}
 
 	@Override
 	public void configTestInterpreter(XMLConfiguration config, String section) {
 		// TODO Auto-generated method stub
 		Configuration testConf = config.configurationAt(section);
 		topk = Utils.getIntFromConfIfNotNull(testConf, "topk", topk);
 		
 	}
 }
 
