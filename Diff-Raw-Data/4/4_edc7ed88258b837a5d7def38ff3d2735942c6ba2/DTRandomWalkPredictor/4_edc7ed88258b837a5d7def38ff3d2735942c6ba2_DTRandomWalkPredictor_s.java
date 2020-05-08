 package plusone.clustering;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import plusone.utils.Indexer;
 import plusone.utils.PaperAbstract;
 import plusone.utils.PlusoneFileWriter;
 import plusone.utils.SparseVec;
 import plusone.utils.Term;
 
 /** Does a random walk on the document-topic graph to find words.
  */
 public class DTRandomWalkPredictor extends ClusteringTest {
     protected List<PaperAbstract> documents;
     protected List<PaperAbstract> trainingSet;
     protected List<PaperAbstract> testingSet;
     protected Indexer<String> wordIndexer;
     protected Term[] terms;
     protected int walkLength;
     protected boolean stochastic;
     protected int nSampleWalks;
     protected static Random rand = new Random();
     protected List<Integer[]> predictions;
 
     public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                  List<PaperAbstract> trainingSet,
                                  List<PaperAbstract> testingSet,
                                  Indexer<String> wordIndexer,
                                  Term[] terms,
                                  int walkLength,
 				 boolean stochastic,
                                  int nSampleWalks) {
         super("DTRandomWalkPredictor");
         this.documents = documents;
         this.trainingSet = trainingSet;
         this.testingSet = testingSet;
         this.wordIndexer = wordIndexer;
         this.terms = terms;
         this.walkLength = walkLength;
         this.stochastic = stochastic;
         this.nSampleWalks = nSampleWalks;
         train();
     }
 
     public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                  List<PaperAbstract> trainingSet,
                                  List<PaperAbstract> testingSet,
                                  Indexer<String> wordIndexer,
                                  Term[] terms,
 				 int walkLength) {
 	this(documents, trainingSet, testingSet, wordIndexer, terms, walkLength, false, -1);
     }
 
     public DTRandomWalkPredictor(List<PaperAbstract> documents,
                                  List<PaperAbstract> trainingSet,
                                  List<PaperAbstract> testingSet,
                                  Indexer<String> wordIndexer,
                                  Term[] terms,
 				 int walkLength,
 				 int nSampleWalks) {
 	this(documents, trainingSet, testingSet, wordIndexer, terms, walkLength, true, nSampleWalks);
     }
     
     protected void train() {
 	predictions = new ArrayList<Integer[]>();
 	for (int document = 0; document < testingSet.size(); document ++) {
 	    PaperAbstract a = testingSet.get(document);
 
 	    SparseVec words;
 	    if (stochastic) {
 		/* Add together words at the end of nSampleWalks random walks. */
 		words = new SparseVec();
 		for (int i = 0; i < nSampleWalks; ++ i) {
 		    PaperAbstract endOfWalk = stochWalk(a);
 		    if (null == endOfWalk) continue;
 		    words.plusEquals(new SparseVec(endOfWalk.trainingTf));
 		}
 	    } else {
 		words = detWalk(a);
 	    }
 	    predictions.add(words.descending());
 	}
     }
 
     public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory) {
 	PlusoneFileWriter writer =
 	    makePredictionWriter(k, outputUsedWord, outputDirectory,
 				 stochastic ? Integer.toString(walkLength) + "-" + Integer.toString(nSampleWalks)
 				            : "det");
         Integer[][] ret = new Integer[testingSet.size()][];
 	for (int document = 0; document < testingSet.size(); 
 	     document ++) {
 	    PaperAbstract a = testingSet.get(document);
 
 	    Integer[] words = predictions.get(document);
 
             ret[document] = firstKExcluding(words, k, outputUsedWord ? null : a);
 	    for (int i = 0; i < ret[document].length; ++ i) {
                writer.write(wordIndexer.get(ret[document][i]) + " " );
 	    }
             writer.write("\n");
 	}
 
         writer.close();
 	return ret;
     }
 
     Integer[] firstKExcluding(Integer[] l, Integer k, PaperAbstract excl) {
 	List<Integer> ret = new ArrayList<Integer>();
 	for (int i = 0; i < l.length && ret.size() < k; ++i) {
 	    Integer word = l[i];
 	    if (excl == null || excl.getModelTf(word) == 0) {
 		ret.add(word);
 	    }
 	}
 	return ret.toArray(new Integer[1]);
     }
     
     protected PaperAbstract stochWalk(PaperAbstract start) {
         PaperAbstract abs = start;
         for (int i = 0; i < walkLength; ++i) {
             List<Integer> words = abs.modelWords;
             if (words.size() == 0) {
                 if (i != 0) throw new Error("assertion failed");
                 return null;
             }
             Integer word = words.get(rand.nextInt(words.size()));
             List<PaperAbstract> wordDocs = terms[word].getDocTrain();
             if (wordDocs.size() == 0) {
                 if (i != 0) throw new Error("assertion failed");
                 return null;
             }
             abs = wordDocs.get(rand.nextInt(wordDocs.size()));
         }
         return abs;
     }
 
     protected SparseVec detWalk(PaperAbstract start) {
 	SparseVec words = new SparseVec(start.trainingTf);
         int nDocs = trainingSet.size() + testingSet.size();
 	for (int i = 0; i < walkLength; ++i) {
 	    /* Walk from words to docs. */
 	    SparseVec docs = new SparseVec();
 	    for (Map.Entry<Integer, Double> pair : words.pairs()) {
                 Term term = terms[pair.getKey()];
 		SparseVec docsForThisWord = term.makeTrainingDocVec(true);
                 docsForThisWord.dotEquals(pair.getValue() * term.trainingIdf(nDocs));
 		docs.plusEquals(docsForThisWord);
 	    }
 	    /* Walk from docs to words. */
 	    words = new SparseVec();
 	    for (Map.Entry<Integer, Double> pair : docs.pairs()) {
 		SparseVec wordsForThisDoc = trainingSet.get(pair.getKey()).makeTrainingWordVec(true, nDocs, terms);
 		wordsForThisDoc.dotEquals(pair.getValue());
 		words.plusEquals(wordsForThisDoc);
 	    }
 	}
 	return words;
     }
 }
