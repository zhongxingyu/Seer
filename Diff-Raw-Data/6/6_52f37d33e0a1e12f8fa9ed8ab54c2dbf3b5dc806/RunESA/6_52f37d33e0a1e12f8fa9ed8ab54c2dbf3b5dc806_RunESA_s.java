 package jhn.esa;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import cc.mallet.types.InstanceList;
 
 import jhn.eda.topiccounts.TopicCounts;
 import jhn.eda.typetopiccounts.TypeTopicCounts;
 import jhn.idx.IntIndex;
 import jhn.util.Util;
 
 public final class RunESA {
 	private RunESA() {}
 	
 	private static int nextLogNum(String logDir) {
 		int max = -1;
 		for(File f : new File(logDir).listFiles()) {
 			if(f.isDirectory()) {
 				int value = Integer.parseInt(f.getName());
 				if(value > max) {
 					max = value;
 				}
 			}
 		}
 		return max + 1;
 	}
 	
 	private static String logFilename() {
 		final String logDir = Paths.runsDir();
 		String filename = logDir + "/" + String.valueOf(nextLogNum(logDir));
 		System.out.println("Writing logs to directory: " + filename);
 		return filename;
 	}
 	
 	public static ESA loadESA(String topicWordIdxName, String datasetName, int minCount) throws FileNotFoundException, ClassNotFoundException, IOException {
 		System.out.print("Loading type-topic counts...");
 		String ttCountsFilename = jhn.eda.Paths.typeTopicCountsFilename(topicWordIdxName, datasetName, minCount);
 		TypeTopicCounts ttcs = (TypeTopicCounts) Util.deserialize(ttCountsFilename);
 		System.out.println("done.");
 		
 		System.out.print("Loading topic counts...");
 		final String topicCountsFilename = jhn.eda.Paths.filteredTopicCountsFilename(topicWordIdxName, datasetName, minCount);
 		TopicCounts tcs = (TopicCounts) Util.deserialize(topicCountsFilename);
 		System.out.println("done.");
 		
 		ESA esa = new ESA(tcs, ttcs, logFilename());
 		
 		esa.conf.putBool(Options.PRINT_REDUCED_DOCS, true);
 //		esa.conf.putInt(Options.REDUCED_DOCS_TOP_N, 10);
 		
        System.out.print("Loading target corpus...");
        InstanceList targetData = InstanceList.load(new File(jhn.Paths.malletDatasetFilename(datasetName)));
        System.out.println("done.");
 		
 		System.out.print("Processing target corpus...");
 		esa.setTrainingData(targetData);
 		System.out.println("done.");
 		
 		return esa;
 	}
 	
 	
 	public static void main (String[] args) throws Exception {
 		final String topicWordIdxName = "wp_lucene4";
 		final String datasetName = "reuters21578";// toy_dataset2, debates2012, sacred_texts, state_of_the_union reuters21578
 		final int minCount = 2;
 		
 		ESA esa = loadESA(topicWordIdxName, datasetName, minCount);
 		
 		int topN = 1;
 		String featselFilename = Paths.featselFilename(topicWordIdxName, datasetName, topN);
 		
 //		esa.selectFeatures(featselFilename, topN);
 		
 		System.out.print("Deserializing selected features...");
 		IntIndex features = (IntIndex) Util.deserialize(featselFilename);
 		System.out.println("done.");
 //		esa.printReducedDocs(features);
 		esa.printReducedDocsLibSvm(features);
 	}//end main
 }
