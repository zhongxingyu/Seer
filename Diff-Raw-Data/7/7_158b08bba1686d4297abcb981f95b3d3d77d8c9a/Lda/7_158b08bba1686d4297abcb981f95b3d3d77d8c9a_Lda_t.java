 package plusone.clustering;
 
 import plusone.utils.Indexer;
 import plusone.utils.PaperAbstract;
 import plusone.utils.PlusoneFileWriter;
 import plusone.utils.Terms;
 import plusone.utils.Utils;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.ejml.simple.SimpleMatrix;
 
 public class Lda extends ClusteringTest {
 
 	private List<PaperAbstract> trainingSet;
 	private Indexer<String> wordIndexer;
 	private Terms terms;
 	private static final int CLUSTERS = 30;
 	private SimpleMatrix beta;
 	private SimpleMatrix gammas;
 
 	public Lda(List<PaperAbstract> trainingSet, Indexer<String> wordIndexer,
 			Terms terms) {
 		super("Lda");
 		this.trainingSet = trainingSet;
		for (PaperAbstract paper : trainingSet) {
			paper.generateTf(0, null, false);
		}
		
 		this.wordIndexer = wordIndexer;
 		this.terms = terms;
 		train();
 	}
 
 	/**
 	 * deprecated
 	 */
 	public void analysis() {
 		// double trainPercent, double testWordPercent) {
 
 		// super.analysis(0,0);
 
 		/*
 		 * List<PaperAbstract> trainingSet = this.documents.subList(0,
 		 * ((int)(documents.size() * trainPercent))); List<PaperAbstract>
 		 * testingSet = this.documents.subList((int)(documents.size() *
 		 * trainPercent) + 1, documents.size());
 		 * 
 		 * for (PaperAbstract a : testingSet) {
 		 * a.generateTestset(testWordPercent, this.wordIndexer);
 		 * //trainingSet.add(a); }
 		 */
 
 		// this.train(documents, testingSet);
 		// this.test(testingSet, testWordPercent);
 	}
 
 	/**
 	 * Runs lda-c-dist on the training set to learn the beta matrix and alpha
 	 * parameter (in this case, all alphas to the dirichlet are equal)
 	 */
 	private void train() {
 		try {
 			new File("lda").mkdir();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		String trainingData = "lda/train.dat";
 
 		createLdaInput(trainingData, trainingSet);
 		Utils.runCommand("lib/lda-c-dist/lda est 1 " + CLUSTERS
 				+ " lib/lda-c-dist/settings.txt " + trainingData
 				+ " random lda", false);
 
 		double[][] betaMatrix = readLdaResultFile("lda/final.beta", 0, true);
 		beta = new SimpleMatrix(betaMatrix);
 		// SimpleMatrix results = gammas.mult(beta);
 
 		/*
 		 * Integer[][] predictedWords = this.predictTopKWords(beta, gammas,
 		 * testingAbstracts, k, outputUsedWords);
 		 * 
 		 * int predicted = 0, total = 0; double tfidfScore = 0.0, idfScore = 0;
 		 * for (int document = 0; document < predictedWords.length; document ++)
 		 * { //System.out.println("document: " + document +
 		 * " number of predicted words: " + predictedWords[document].length);
 		 * for (int predict = 0; predict < predictedWords[document].length;
 		 * predict ++) { Integer wordID = predictedWords[document][predict]; if
 		 * (testingAbstracts.get(document).predictionWords.isEmpty())
 		 * System.out.println("no prediction words in testing set?");
 		 * 
 		 * if (testingAbstracts.get(document).predictionWords .contains(wordID))
 		 * { predicted ++; tfidfScore += this.tfidf.tfidf(abstracts.size() -
 		 * testingAbstracts.size() + document, wordID); idfScore +=
 		 * this.tfidf.idf(wordID); }
 		 * 
 		 * total ++; } } System.out.println("Predicted " +
 		 * ((double)predicted/total)*100 + " percent of the words");
 		 * System.out.println("total attempts: " + total);
 		 * System.out.println("TFIDF score: " + tfidfScore);
 		 * System.out.println("IDF score: " + idfScore);
 		 */
 	}
 
 	
 	/**
 	 * Given a set of test documents, runs lda-c-dist inference to learn the
 	 * final gammas. Then, subtracts alpha from each gamma to find the expected
 	 * number of times each word appears per topic. Finally, multiplies each
 	 * gamma by beta to find the expected number of times a word appears for
 	 * each document.
 	 * 
 	 * @param testDocs	the list of documents to run prediction on
 	 * @return	the expected number of times each word appears per document
 	 */
 	public double[][] predict(List<PaperAbstract> testDocs){
 	
 		String testData = "lda/test.dat";
 
 		createLdaInput(testData, testDocs);
 		Utils.runCommand("lib/lda-c-dist/lda inf " + 
 				" lib/lda-c-dist/settings.txt " + "lda/final" + 
 				testData + " lda/output", false);
 		
 		double[][] gammasMatrix = readLdaResultFile("lda/output-gamma.dat",
 				testDocs.size(), false);
 		double alpha = readAlpha("lda/final.other");
 		for (int i=0; i<gammasMatrix.length; i++) {
 			for (int j=0; j<gammasMatrix[i].length; j++) {
 				gammasMatrix[i][j] -= alpha;
 			}
 		}
 		gammas = new SimpleMatrix(gammasMatrix);
 		SimpleMatrix results = gammas.mult(beta);
 		
 		double[][] result = new double[results.numRows()][results.numCols()];
 		for (int row=0; row<results.numRows(); row++) {
 			for (int col=0; col<results.numCols(); col++) {
 				result[row][col] = results.get(row, col);
 			}
 		}
 		
 		return result;
 	}
 	                
 	/*public Integer[][] predictTopKWords(int k, boolean outputUsedWords) {
 		train();
 		SimpleMatrix matrix = gammas.mult(beta);
 		Integer[][] results = new Integer[testingSet.size()][];
 		for (int row = 0; row < matrix.numRows(); row++) {
 			PriorityQueue<ItemAndScore> queue = 
 				new PriorityQueue<ItemAndScore>(k + 1);
 			
 			for (int col = 0; col < matrix.numCols(); col++) {
 				if (!outputUsedWords && testingSet.get(row).tf[col][0] > 0)
 					continue;
 				if (queue.size() < k
 						|| matrix.get(row, col) > queue.peek().score) {
 					if (queue.size() >= k)
 						queue.poll();
 					queue.add(new ItemAndScore(col, matrix.get(row, col), false));
 				}
 			}
 
 			// if (outputUsedWords) {
 			results[row] = new Integer[Math.min(k, queue.size())];
 			for (int i = 0; i < k && !queue.isEmpty(); i++) {
 				results[row][i] = queue.poll().wordID;
 			}
 			
 			 * } else { //System.out.println("Predicting results for row: " +
 			 * row); List<WordAndScore> lst = new ArrayList<WordAndScore>(); for
 			 * (int i = 0; i < k && !queue.isEmpty(); i ++) { WordAndScore cur =
 			 * queue.poll(); //System.out.println("predicted word: " +
 			 * wordIndexer.get(cur.wordID) + " score: " + cur.score); if
 			 * (!abstracts.get(row).outputWords.contains(cur)) lst.add(cur);
 			 * else i --; }
 			 * 
 			 * results[row] = new Integer[lst.size()]; for (int i = 0; i <
 			 * lst.size(); i ++) { results[row][i] = lst.get(i).wordID; } }
 			 
 		}
 		return results;
 	}*/
 
 	/**
 	 * Takes a list of PaperAbstract documents and writes them to file according
 	 * to the format specified by lda-c-dist
 	 * 
 	 * @param filename	name of the file to be created (will be overwritten
 	 * 					if it already exists)
 	 * @param papers	list of papers to be written to file 
 	 */
 	private void createLdaInput(String filename, List<PaperAbstract> papers) {
 
		System.out.print("creating lda input in file: " + filename + " ... ");
 
 		PlusoneFileWriter fileWriter = new PlusoneFileWriter(filename);
 
 		for (PaperAbstract paper : papers) {
 			fileWriter.write(paper.getTrainingWords().size() + " ");
 			
 			for (int word : paper.getTrainingWords()) {
 				fileWriter.write(word + ":" + paper.getTrainingTf(word) + " ");
 			}
 			fileWriter.write("\n");
 		}
 
 		fileWriter.close();
 		
 		System.out.println("done.");
 	}
 	
 	/**
 	 * Takes a file output by lda-c-dist and stores it in a matrix.
 	 * 
 	 * @param filename	file to be read
 	 * @param start		TODO use for start (typically 0)
 	 * @param exp		whether to exponentiate the read entries
 	 * @return	a double[][] (matrix) with the contents of filename 
 	 */
 	private double[][] readLdaResultFile(String filename, int start, 
 			boolean exp) {
 		List<String[]> gammas = new ArrayList<String[]>();
 		double[][] results = null;
 		// System.out.println("reading lda results file starting at : " +
 		// start);
 		try {
 			FileInputStream fstream = new FileInputStream(filename);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 
 			int c = 0;
 			while ((strLine = br.readLine()) != null) {
 				if (c >= start) {
 					gammas.add(strLine.trim().split(" "));
 				}
 				c++;
 			}
 			// System.out.println("C got to " + c);
 
 			results = new double[gammas.size()][];
 			for (int i = 0; i < gammas.size(); i++) {
 				results[i] = new double[gammas.get(i).length];
 				for (int j = 0; j < gammas.get(i).length; j++) {
 					results[i][j] = new Double(gammas.get(i)[j]);
 					if (exp)
 						results[i][j] = Math.exp(results[i][j]);
 
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return results;
 	}
 	
 	private double readAlpha(String filename) {
 		FileInputStream filecontents = null;
 		try {
 			filecontents = new FileInputStream(filename);
 		} catch (FileNotFoundException e) {
 			System.out.println("Check your filepath");
 			System.exit(1);
 		}
 		Scanner lines = new Scanner(filecontents);
 		String alphaLine = lines.nextLine();
 		alphaLine = lines.nextLine();
 		alphaLine = lines.nextLine();
 		String[] splitLine = alphaLine.split(" ");
 		return Double.parseDouble(splitLine[1]);
 	}
 }
