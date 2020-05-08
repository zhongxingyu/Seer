 package plusone.clustering;
 
 import plusone.utils.Indexer;
 import plusone.utils.PaperAbstract;
 import plusone.utils.PlusoneFileWriter;
 import plusone.utils.Term;
 import plusone.utils.Utils;
 import plusone.utils.WordAndScore;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 
 import org.ejml.simple.SimpleMatrix;
 
 public class Lda extends ClusteringTest {
 
     private List<PaperAbstract> trainingSet;
     private List<PaperAbstract> testingSet;
     private List<PaperAbstract> documents;
     private Indexer<String> wordIndexer;
     private Term[] terms;
     private static final int CLUSTERS = 30;
     private SimpleMatrix beta;
     private SimpleMatrix gammas;
 
     public Lda(List<PaperAbstract> documents, 
 	       List<PaperAbstract> trainingSet, 
 	       List<PaperAbstract> testingSet, 
 	       Indexer<String> wordIndexer,
 	       Term[] terms) {
 	super("Lda");
 	this.documents=documents;
 	this.trainingSet=trainingSet;
 	this.testingSet=testingSet;
 	this.wordIndexer = wordIndexer;
 	this.terms = terms;
     }	       
 
     private void train(int k, boolean outputUsedWords) {
 	try {
 	    new File("lda").mkdir();
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
 
 	//String trainingData = "lda/train.ldain";
 	String trainingData = "/tmp/train.ldain";
 
 	createLdaInput(trainingData, documents);
 	Utils.runCommand("lib/lda-c-dist/lda est 1 " + CLUSTERS + 
 			 " lib/lda-c-dist/settings.txt " + trainingData + 
 			 " random lda", false);
 
 	// new code...
 	double[][] betaMatrix = 
 	    readLdaResultFile("lda/final.beta", 0, true);
 	double[][] gammasMatrix = 
 	    readLdaResultFile("lda/final.gamma", 
 			      documents.size() - testingSet.size(), false);
 
 	System.out.println("gammasMatrix size: " + gammasMatrix.length);
 	System.out.println("other size: " + gammasMatrix[0].length);
 
 	// matrix multiplication using the EJML package
 	this.beta = new SimpleMatrix(betaMatrix);
 	this.gammas = new SimpleMatrix(gammasMatrix);
     }
 
     private double[][] readLdaResultFile(String filename, int start, 
 					 boolean exp) {
 	List<String[]> gammas = new ArrayList<String[]>();
 	double[][] results = null;
 
 	try {
 	    FileInputStream fstream = new FileInputStream(filename);
 	    DataInputStream in = new DataInputStream(fstream);
 	    BufferedReader br = 
 		new BufferedReader(new InputStreamReader(in));
 	    String strLine;
 
 	    int c = 0;
 	    while ((strLine = br.readLine()) != null) {
 		if (c >= start) {
 		    gammas.add(strLine.trim().split(" "));
 		}
 		c ++;
 	    }
 	    //System.out.println("C got to " + c);
 
 	    results = new double[gammas.size()][];
 	    for (int i = 0; i < gammas.size(); i ++) {
 		results[i] = new double[gammas.get(i).length];
 		for (int j = 0; j < gammas.get(i).length; j ++) {
 		    results[i][j] = new Double(gammas.get(i)[j]);
 		    if (exp)
 			results[i][j] = Math.exp(results[i][j]);	    
 		}
 	    }
 	    
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
 
 	return results;
     }
 
     public Integer[][] predict(int k, boolean outputUsedWords) {
 	this.train(k, outputUsedWords);
 	SimpleMatrix matrix = gammas.mult(beta);
 	Integer[][] results = new Integer[testingSet.size()][];
 	for (int row = 0; row < matrix.numRows(); row ++) {
 	    PriorityQueue<WordAndScore> queue = 
 		new PriorityQueue<WordAndScore>(k+1);
 	    for (int col = 0; col < matrix.numCols(); col ++) {
 		if (!outputUsedWords && testingSet.get(row).getTf0(col) > 0)
 	    		continue;
 	    	if (queue.size() < k || 
		    matrix.get(row,col) > queue.peek().score){
 		    if (queue.size()>=k)
 			queue.poll();
 		    queue.add(new WordAndScore(col, 
 					       matrix.get(row,col), false));
 	    	}
 	    }
 
 	    results[row] = new Integer[Math.min(k, queue.size())];
 	    for (int i = 0; i < k && !queue.isEmpty(); i ++) {
 		results[row][i] = queue.poll().wordID;
 	    }
 	}
 	return results;
     }
 
     // helper functions
     private void createLdaInput(String filename, 
 				List<PaperAbstract> papers) {
 	PlusoneFileWriter fileWriter = new PlusoneFileWriter(filename);
 
 	for (PaperAbstract paper : papers) {
 
 	    fileWriter.write(paper.uniqueWords + " ");
 	    for(int entry : paper.wordSet)
 		fileWriter.write(entry + ":" + paper.getTf0(entry) + " ");
 
 	    fileWriter.write("\n");
 	}
 
 	fileWriter.close();
     }
 }
