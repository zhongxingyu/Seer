 package controllers.analysis;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.apache.commons.math3.linear.ArrayRealVector;
 import org.apache.commons.math3.linear.RealVector;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.core.StopFilter;
 import org.apache.lucene.analysis.de.GermanAnalyzer;
 import org.apache.lucene.analysis.en.PorterStemFilter;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.FieldType;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import controllers.Application;
 import controllers.preparation.Search;
 
 /**
  * 
  * @author Christian Ochsenk&uuml;hn
  * @author Sebastian Mandel
  * @version 1.0
  */
 public class Algorithm {
 
 	// Prozentuales Vorkommen in allen Dokumenten, ab der ein Wort noch als selten gilt:
	private final static float RARE_APPEARANCE = (float) 0.05; // = 5%
 	// Anzahl der seltenen Wörter, ab der ein Dokument als selbes Thema angesehen wird:
	private final static int RARE_WORDS_UPPER_BOUND = 15;
 	// Anzahl der seltenen Wörter, ab der ein Dokument eventuell dem selben Thema zugehören könnte:
 	private final static int RARE_WORDS_LOWER_BOUND = 5;
 	// Schwellwert, ab dem der Cosinus-Abstand zweier Vektoren dasselbe Thema annimmt:
 	private final static float COSINUS_BOUND = (float) 0.75;
 	
 //	private static ArrayList<String> rareWords;
 	// Integer docId, int sameRareWordsCount
 	private static Map<Integer, Integer> maybeSimilarDocs;
 	private static Map<String, Integer> currentTokens;
 	
 	/**
 	 * Zun&auml;chst werden Artikel gesucht, in deren Titel mindestens ein Wort aus
 	 * dem Titel des zu vergleichenden Artikels auftauchen. Das Dokument mit dem
 	 * besten per Vector Space Model festgestellten Scoring wird weiter
 	 * untersucht. Es wird gepr&uuml;ft, wie viel W&ouml;rter aus dem Titel des zu
 	 * vergleichenden Artikels auch im Titel des vermeintlich &auml;hnlichen Artikels
 	 * enthalten sind. Ergibt diese Pr&uuml;fung mehr als 2 identische W&ouml;rter, wird kein neues
 	 * Thema angenommen und der TopicHash des &auml;hnlichen Artikels extrahiert.
 	 * <br><br>
 	 * Fall 1: Gleiche Themen werden als Unterschiedliche erkannt<br>
 	 * H&auml;ufigkeit: Wahrscheinlich<br>
 	 * Folge: Kein Problem, da sp&auml;tere Funktionen dies noch genauer &uuml;berprüfen
 	 * <br><br>
 	 * Fall 2: Unterschiedliche Themen als Gleiche erkannt<br>
 	 * H&auml;ufigkeit: gering<br>
 	 * Folge: Neues Thema geht unter<br>
 	 * @author Sebastian Mandel
 	 * @param queryTitle
 	 *            Titel des Nachrichtenartikels, der auf ein neues Thema hin
 	 *            untersucht werden soll
 	 * @return TopicHash eines Artikels mit &auml;hnlichem Sachverhalt, sofern ein
 	 *         solcher detektiert wird. Ansonsten Leerstring.
 	 */
 	public static String hasSimilarTitle(String queryTitle) {
 		String topicHash = "";
 		ScoreDoc[] hits = null;
 		IndexSearcher searcher = Application.createSearcher();
 		Query query1;
 		Query query2;
 		String docTitle;
 		StringTokenizer queryTokenizer;
 		StringTokenizer docTokenizer;
 		String currentQueryToken;
 		int i = 0;
 
 		try {
 			//Erzeugung und Durchführung der Suchanfrage 
 			BooleanQuery booleanQuery = new BooleanQuery();
 			query1 = new QueryParser(Version.LUCENE_46, "title", Application.getAnalyzer()).parse(queryTitle);
 			query2 = NumericRangeQuery.newLongRange("date", Search.getLowerBound(), Search.getUpperBound(), true, true);
 			booleanQuery.add(query1, BooleanClause.Occur.MUST);
 			booleanQuery.add(query2, BooleanClause.Occur.MUST);
 			hits = searcher.search(booleanQuery, 1).scoreDocs;
 			
 			//Wird ähnlicher Titel gefunden, Prüfung wieviel Wörter in beiden Titeln vorhanden sind
 			if (hits.length == 1) {
 				docTitle = searcher.doc(hits[0].doc).get("title");
 				queryTokenizer = new StringTokenizer(queryTitle);
 
 				while (queryTokenizer.hasMoreTokens()) {
 					currentQueryToken = queryTokenizer.nextToken();
 					docTokenizer = new StringTokenizer(docTitle);
 					while (docTokenizer.hasMoreTokens()) {
 						if (currentQueryToken.equals(docTokenizer.nextToken())) {
 							i++;
 							break;
 						}
 					}
 				}
 				if (i >= 3) {
 					topicHash = searcher.doc(hits[0].doc).get("topichash");
 					System.out.println("   Aehnlicher Titel gefunden: " + topicHash);
 				}	
 			}
 			searcher.getIndexReader().close();
 		} catch (IOException | ParseException | NullPointerException e) {
 			System.out.println("   hasSimilarTitle fehlgeschlagen da Index nicht vorhanden.");
 		}
 		return topicHash;
 	}
 
 	/**
 	 * Diese Funktion sucht die seltenen Worte eines gegebenen Texts im Index und analysiert,
 	 * wieviele Dokumente des Index diese seltenen Worte enthalten. Sobald der Schwellwert RARE_WORDS_UPPER_BOUND
 	 * an seltenen Worten in einem Dokument &uuml;berschritten wird, so wird dieses Dokument als &auml;hnlich zum
 	 * &uuml;bergebenen Text angesehen. Der TopicHash des &auml;hnlichen Dokuments wird zur&uuml;ckgegeben.
 	 * Sollte keines der Dokumente den Schwellwert überschreiten, so wird ein leerer String zur&uuml;ckgegeben. 
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param text = Zu analysierender Text
 	 * @return TopicHash eines &auml;hnlichen Artikels, ansonsten leerer String
 	 */
 	public static String hasSimilarRareWords(String text) {
 		int rareRangeMax = (int) (Application.getNumberOfAllDocuments()*RARE_APPEARANCE);
 		if(rareRangeMax<1)
 			rareRangeMax = 1;
 //		System.out.println("rareRangeMax: "+rareRangeMax);
 		
 		currentTokens = getTFMap(text);
 		
 		IndexSearcher searcher = Application.createSearcher();
 		Query query2 = NumericRangeQuery.newLongRange("date",Search.getLowerBound(), Search.getUpperBound(), true, true);
 		
 		maybeSimilarDocs = new HashMap<Integer, Integer>(); // < Integer docId, Integer sameRareWordsCount >
 		
 		for(String token : currentTokens.keySet()) {
 			// Für jeden Begriff: Durchsuchen des Index:
 			BooleanQuery booleanQuery = new BooleanQuery();
 			try {
 				Query query1 = new QueryParser(Version.LUCENE_46, "text",Application.getAnalyzer()).parse(token);
 				booleanQuery.add(query1, BooleanClause.Occur.MUST);
 				booleanQuery.add(query2, BooleanClause.Occur.MUST);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			try {
 				TopDocs topDocs = searcher.search(booleanQuery, 1);
 				if(topDocs.totalHits>0) {
 					topDocs = searcher.search(booleanQuery, topDocs.totalHits);
 				
 					if(topDocs.totalHits <= rareRangeMax) { // Wenn es ein seltener Begriff ist, dann...				
 						ScoreDoc[] hits = topDocs.scoreDocs;
 						for(int j=0; j<hits.length; j++) {
 							int temp = 0;
 							
 							if(maybeSimilarDocs.get(hits[j].doc)!=null)
 								temp = maybeSimilarDocs.get(hits[j].doc);
 								
 							if(++temp >= RARE_WORDS_UPPER_BOUND) {
 	//							System.out.println("More than "+RARE_WORDS_UPPER_BOUND+" rare words in document >>> Old Topic");
 								String hash = searcher.doc(hits[j].doc).get("topichash");
	//							System.out.println("Similar with :"+searcher.doc(hits[j].doc).get("title"));
								searcher.getIndexReader().close();
 								System.out.println("   Aehnlich zu Thema " + hash);
 								return hash;
 							}
 	//						System.out.println("Doc #"+hits[j].doc+" contains "+temp+" rare words.");
 							maybeSimilarDocs.put(hits[j].doc, temp);
 						}
 					} else {
 //						System.out.println("Nicht selten, weil "+topDocs.totalHits+" totalHits > Range "+rareRangeMax);
 					}
 				} else {
 //					System.out.println("In keinem Dokument gefunden...");
 				}
 				
 			} catch (IOException | NullPointerException e) {
 				System.out.println("hasSimilarRareWords fehlgeschlagen da Index nicht vorhanden.");
 			} 
 		}
 		try {
 			searcher.getIndexReader().close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	/**
 	 * Vergleicht einen übergebenen Text mit allen Dokumenten, die mehr als RARE_WORDS_LOWER_BOUND gleiche seltene
 	 * Begriffe enthalten. Dazu nutzt er die Anzahl des Vorkommens aller Begriff, legt diese für jeden Text in einem
 	 * Vektor ab und vergleicht den Abstand der Vektoren. Ab einem Abstand von COSINUS_BOUND wird ein ähnliches Thema
 	 * angenommen und der TopicHash des &auml;hnlichen Dokuments wird zur&uuml;ckgegeben.
 	 * Sollte keines der Dokumente den Schwellwert überschreiten, so wird ein leerer String zur&uuml;ckgegeben. 
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param text = Zu analysierender Text
 	 * @return TopicHash eines &auml;hnlichen Artikels, ansonsten leerer String
 	 */
 	public static String hasSimilarBody(String text) {
 		String topicHash = "";
		if(maybeSimilarDocs!=null) {
 			// Iteriert jedes Dokument, welches seltene Begriffe enthält:
 			for(int docId : maybeSimilarDocs.keySet())
 		    {
 				if(maybeSimilarDocs.get(docId) > RARE_WORDS_LOWER_BOUND) {
 					Map<String, Integer> similarDocTF = Application.getTermFrequencies(docId);
 //					for(String temp : similarDocTF.keySet()) {
 //						System.out.println("\""+temp+"\": "+similarDocTF.get(temp));
 //					}
 					RealVector simVector = createRealVector(similarDocTF,similarDocTF);
 					RealVector newDocVector = createRealVector(similarDocTF, currentTokens);
 					double cosSim = getCosineSimilarity(simVector, newDocVector);
 					if(cosSim > COSINUS_BOUND) {
 						try {
 							IndexSearcher searcher = Application.createSearcher();
 							topicHash = searcher.doc(docId).get("topichash");
 							searcher.getIndexReader().close();
 							System.out.println("   Cosinus-Abstand "+((int)(cosSim*100))/100f + " ueber Schwellwert. Aehnlich zu Thema: "+topicHash);
 							return topicHash;
 						} catch (IOException e) {
 							System.out.println("Algorithm: Error getting topicHash.");
 						}
 					} else {
 						System.out.println("   Cosinus-Abstand "+((int)(cosSim*100))/100f + " zu wenig.");
 					}
 				}
 		    }
 		} else {
 		//	System.out.println("Keine seltenen Begriffe im Dokument...");
 		}
 		
 		return topicHash;
 	}
 	
 	/**
 	 * Berechnet den Cosinus-Abstand zwischen zwei Vektoren und gibt ihn zur&uuml;ck
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param vec1 = Vector 1
 	 * @param vec2 = Vector 2
 	 * @return Abstand zwischen den Vektoren (1 entspricht einem Winkel von 0°)
 	 */
 	private static double getCosineSimilarity(RealVector vec1, RealVector vec2) {
         return (vec1.dotProduct(vec2)) / (vec1.getNorm() * vec2.getNorm());
     }
 	
 	/**
 	 * Erzeugt aus einer Termfrequenz-Map einen Vector.
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param rootMap = Ursprungsset an Begriffen, an welchem sich der neue Vector ausrichten soll
 	 * @param mapToVector = Termfrequenz-Map, die in einen Vector umgewandelt werden soll
 	 * @return Neuer TF-Vector
 	 */
 	private static RealVector createRealVector(Map<String, Integer> rootMap, Map<String, Integer> mapToVector) {
         RealVector vector = new ArrayRealVector(rootMap.size());
         int i = 0;
         for (String term : rootMap.keySet()) {        	
         	int value = 0;
         	if(mapToVector.containsKey(term))
         		value = mapToVector.get(term);
         	
             vector.setEntry(i++, value);
         }
         return (RealVector) vector.mapDivide(vector.getL1Norm());
     }
 	
 	/**
 	 * Speichert einen &uuml;bergebenen Text temporär in einem eigenen Index, um davon die Termfrequenz abzuleiten.
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param text = Text dessen Termfrequenz bekommen werden soll
 	 * @return Terme und jeweilige Anzahl des Vorkommens
 	 */
 	public static Map<String, Integer> getTFMap(String text) {
 		String tempIndex = "tempIndex";
 		try {
 			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,
 					new GermanAnalyzer(Version.LUCENE_46)).setSimilarity(new DefaultSimilarity());
 			iwc.setOpenMode(OpenMode.CREATE); // Index wird jedes mal überschrieben
 			IndexWriter writer = new IndexWriter(FSDirectory.open(new File(tempIndex)), iwc);
 			
 			Document doc = new Document();
 			FieldType type = new FieldType();
 			type.setIndexed(true);
 			type.setStored(false);
 			type.setStoreTermVectors(true);
 			doc.add(new Field("text", text, type));
 			writer.addDocument(doc);
 			writer.close();
 			
 			return Application.getTermFrequencies(0, new File(tempIndex));
 		} catch (IOException e) {
 			System.out.println("Probleme beim Erstellen des temporären Index.");
 			return null;
 		}
 	}
 	
 	/**
 	 * [Funktion wird in aktueller Version nicht genutzt. Nutze stattdessen Algorithm.getTFMap(String text)]
 	 * Teilt gegebenen Text in Tokens und entfernt die Stopwords. Zählt das Vorkommen jedes Terms im Text.
 	 * @author Christian Ochsenk&uuml;hn
 	 * @param text = Text der geteilt werden soll
 	 * @return Tokens und jeweilige Anzahl des Vorkommens
 	 */
 	public static Map<String, Integer> tokenizeAndRemoveStopWords(String text) {  
 	    TokenStream tokenStream = new StandardTokenizer(
 	            Version.LUCENE_46, new StringReader(text));
 	    tokenStream = new StopFilter(Version.LUCENE_46, tokenStream, ((GermanAnalyzer)Application.getAnalyzer()).getStopwordSet());
 	    tokenStream = new PorterStemFilter(tokenStream);
 	    
 	    Map<String, Integer> termFreq = new HashMap<String, Integer>();
 	    try {
 	    	tokenStream.reset();
 			while (tokenStream.incrementToken()) {
 			    String tok = tokenStream.getAttribute(CharTermAttribute.class).toString();
 /*			    try {  
 			      Double.parseDouble(tok);
 			      continue;	// do not add numbers!
 			    } catch(NumberFormatException nfe)  {  
 			        // everything ok!
 			    } */
 			    
 			    if(termFreq.containsKey(tok))
 			    	termFreq.put(tok, (termFreq.get(tok)+1));
 			    else
 			    	termFreq.put(tok, 1);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	    
 	    return termFreq;
 	}
 }
