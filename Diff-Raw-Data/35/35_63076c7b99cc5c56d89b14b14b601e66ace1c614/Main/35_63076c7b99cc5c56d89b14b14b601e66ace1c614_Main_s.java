 package program;
 
 import java.io.IOException;
 import java.util.Random;
 
 import model.NBayes;
 
 import corpus.Corpus;
 import corpus.Vocabulary;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	
 	public static void main(String[] args) throws IOException {
 		int numIter = 100;
 		long seed = 17;
 		//String inFile = "/home/anjan/workspace/SRL-anjan/myconll2005/final/nbayes/combined.final.propprocessed.span";
		String inFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.txt";
		int numClass = 2;
		boolean containsLabel = true;
 		Corpus c = new Corpus();
 		c.read(inFile, containsLabel); //also reads the vocabulary
 		Random r = new Random(seed);
 		NBayes model;
 		if(containsLabel) {
			model = new NBayes(c, c.labelMap.size(), Vocabulary.vocabSize);
 			model.initializeSupervised();
 			//model.train(1);
 		} else {
			model = new NBayes(c, numClass, Vocabulary.vocabSize);
 			model.initializeRandom(r);
 			model.train(numIter);
 		}
 		model.save();
 		model.decode(inFile + ".decoded");
 	}
 }
