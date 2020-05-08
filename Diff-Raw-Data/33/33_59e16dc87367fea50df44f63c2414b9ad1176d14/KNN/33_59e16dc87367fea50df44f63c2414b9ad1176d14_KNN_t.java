 package plusone.clustering;
 
 import plusone.utils.Document;
 import plusone.utils.Indexer;
 import plusone.utils.KBestList;
 import plusone.utils.PaperAbstract;
 import plusone.utils.Term;
 import plusone.utils.TFIDFCounter;
 import plusone.utils.WordAndScore;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class KNN extends ClusteringTest {
 
     private List<PaperAbstract> documents;
     private List<PaperAbstract> trainingSet;
     private List<PaperAbstract> testingSet;
     private Indexer<String> wordIndexer;
     private List<Document> model;
     private Term[] terms;
    private int K_CLOSEST = 10;
 
     public KNN(List<PaperAbstract> documents,
 	       List<PaperAbstract> trainingSet,
 	       List<PaperAbstract> testingSet,
 	       Indexer<String> wordIndexer,
 	       Term[] terms) {
 	super("KNN");
 	this.documents = documents;
 	this.trainingSet = trainingSet;
 	this.testingSet = testingSet;
 	this.wordIndexer = wordIndexer;
 	this.terms = terms;	
 
 	this.train(trainingSet);
     }
 
     private void train(List<PaperAbstract> training) {
 	try {
 	    new File("lda").mkdir();
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
 
 	this.model = new ArrayList<Document>();
 	for (PaperAbstract a : training) {
 	    Document t = Document.abstractToDocument(a);
 	    this.model.add(t);
 	}
 	
     }
 
     private KBestList<Document> getKClosest(Document testD) {
 	KBestList<Document> kList = new KBestList(K_CLOSEST);
 	
 	for (Document d : this.model) {
 	    double dist = testD.distance(d);
 	    kList.insert(d, dist);
 	}
 	return kList;
     }
 
     public Integer[][] predict(int k, boolean outputUsedWord) {
 	Integer[][] array = new Integer[this.testingSet.size()][];
 	for (int document = 0; document < this.testingSet.size(); 
 	     document ++) {
 	    PaperAbstract a = testingSet.get(document);
 	    Document t = Document.abstractToDocument(a);
 	    KBestList<Document> kList = getKClosest(t);
 	    
	    List<Integer> lst = predictTopKWordsWithKList(kList, a, k, 
							  outputUsedWord);
 	    array[document] = (Integer[])lst.toArray(new Integer[lst.size()]);
 	}
 	return array;
     }
 	
    private List<Integer> predictTopKWordsWithKList(KBestList<Document> kList,
						    PaperAbstract testDoc, 
						    int k, 
						    boolean outputUsedWord) {
 	List<Integer> predictedWords = new ArrayList<Integer>();
 	Document d = new Document();
 	Iterator<Document> iter = kList.iterator();
 
 	while (iter.hasNext()) {
 	    Document t = iter.next();
 	    Iterator<Integer> words = t.getWordIterator();
 	    while (words.hasNext()) {
 		Integer word = words.next();
 		d.addWord(word, t.getWordCount(word));
 	    }
 	}
 
 	List<WordAndScore> wordsList = new ArrayList<WordAndScore>();
 	Iterator<Integer> wordsIter = d.getWordIterator();
 	while (wordsIter.hasNext()) {
 	    Integer word = wordsIter.next();
 	    wordsList.add(new WordAndScore(word, d.getWordCount(word), 
 					   false));
 	}
 	Collections.sort(wordsList);
 
 	for (WordAndScore pair : wordsList) {
	    if (outputUsedWord ||
		testDoc.getTf0(pair.wordID) == 0)
 		predictedWords.add(pair.wordID);
	    
 	    if (predictedWords.size() == k)
 		return predictedWords;
 	}
 	return predictedWords;
     }
 }
