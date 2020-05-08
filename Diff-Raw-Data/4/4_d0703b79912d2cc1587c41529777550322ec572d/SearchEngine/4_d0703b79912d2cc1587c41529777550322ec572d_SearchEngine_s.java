 package retrieval;
 
 import indexing.IIndexer;
 import indexing.ZipTokenStream;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipException;
 
 
 import dao.*;
 
 public class SearchEngine
 {
 	private ArrayList<Map.Entry<Integer, Float>> fastAccessQueryVector;
 	private CollectionStatistics collectionStatistics;
 	private Vocabulary vocabulary;
 	private String indexName;
 	private String indexDirectory;
 	
 	private boolean useStemming;
 	
 	private Scanner indexScanner;
 	
 	private IScoringMethod scoringMethod;
 	
 	private ArrayList<RetrievalResult> searchResults;
 	private SizedPriorityQueue<RetrievalResult> tmpSearchResults;
 	
 	public SearchEngine(String _indexName, boolean _useStemming) throws IOException
 	{
 		this.indexName = _indexName;
 		this.useStemming = _useStemming;
 		
 		indexDirectory = "output"+java.io.File.separator;
 		
 		if (useStemming) {
 			indexDirectory += "stemming"+File.separator;
 		}
 		else {
 			indexDirectory += "no_stemming"+File.separator;
 		}
 	}
 	
 	public void setScoringMethod(IScoringMethod method) {
 		this.scoringMethod = method;
 	}
 	
 	private void loadVocabularyFromIndex() {
 		System.out.println("Loading vocabulary... ");
 		
 		vocabulary = new Vocabulary(1);
 		
 		String nextLine;
 		int attributeCounter = 0;
 		int termCounter = 0;
 		
 		while (indexScanner.hasNext()) {
 			nextLine = indexScanner.nextLine().toLowerCase();
 			if (nextLine.length() < 1) {
 				continue;
 			}
 			if (nextLine.charAt(0) == '%') { // ignore comments
 				continue;
 			}
 			if (nextLine.length() < 2) {
 				continue;
 			}
 			if (nextLine.length() == 5 && nextLine.contains("@data"))  {
 				break;
 			}
 			if (nextLine.startsWith("@attribute ")) {
 				if (attributeCounter >= IIndexer.META_FIELD_COUNT) { // ignore meta fields
 					vocabulary.addSilent(nextLine.substring(11, nextLine.length() - 8));// - length of " NUMERIC" == 8
 					termCounter++;
 				}
 				attributeCounter++;
 			}
 				
 		}
 		
 		System.out.println("Vocabulary contains "+termCounter+" terms. ");
 	}
 	
 	private void queryIndex() {
 		while (indexScanner.hasNext()) {
 			String nextLine = indexScanner.nextLine();
 			
 			if (nextLine.length() == 0 || nextLine.charAt(0) != '{') {
 				System.err.println("@DATA section contains invalid line:");
 				System.err.println(nextLine);
 				break;
 			}
 			
 			ArrayList<Map.Entry<Integer, Float>> documentVector = new ArrayList<Map.Entry<Integer, Float>>();
 			
 			int documentLength = 0; // number of terms in the document
 			
 			int attributeBeginning = 1;
 			int attributeMiddle = nextLine.indexOf(' ', attributeBeginning);
 			int attributeEnd = nextLine.indexOf(',', attributeMiddle);
 			
 			if (attributeMiddle == -1 || attributeEnd == -1 || attributeMiddle > attributeEnd) {
 				System.err.println("@DATA section contains invalid line:");
 				System.err.println(nextLine);
 				break;
 			}
 			
 			RetrievalResult currentRetrievalResult = new RetrievalResult();
 			//currentRetrievalResult.setTopic(queryDocument.substring(queryDocument.indexOf('/')+1));
 			
 			while(attributeMiddle != -1 && attributeEnd != -1 && attributeMiddle < attributeEnd) {
 				int attributeId = Integer.parseInt(nextLine.substring(attributeBeginning, attributeMiddle));
 				
 				switch(attributeId) {
 					case 0: // docID, not needed
 						//docID = Integer.parseInt((nextLine.substring(attributeMiddle+1, attributeEnd)));
 						break;
 					case 1:	// docClass
 						currentRetrievalResult.setDocumentClass(nextLine.substring(attributeMiddle+1, attributeEnd));
 						break;
 					case 2: // docName
 						currentRetrievalResult.setDocumentName(nextLine.substring(attributeMiddle+1, attributeEnd));
 						break;
 					default:
 						float value = Float.parseFloat(nextLine.substring(attributeMiddle, attributeEnd));
 						documentLength += (scoringMethod.requiresPlainTf()?value:1);
 						documentVector.add(new AbstractMap.SimpleEntry<Integer, Float>(attributeId-IIndexer.META_FIELD_COUNT, value));
 				}
 				
 				attributeBeginning = attributeEnd + 1;
 				attributeMiddle = nextLine.indexOf(' ', attributeBeginning);
 				attributeEnd = nextLine.indexOf(',', attributeMiddle);
 				if (attributeEnd == -1) {
 					attributeEnd = nextLine.indexOf('}');
 				}
 			}
 			
 			double similarity = 0.0;
 			double documentVectorLength = 0.0;
 			double queryVectorLength = 0.0;
 			
 			Iterator<Map.Entry<Integer, Float>> queryIterator = fastAccessQueryVector.iterator();
 			Iterator<Map.Entry<Integer, Float>> documentIterator = documentVector.iterator();
 			
 			Map.Entry<Integer, Float> nextQueryEntry = null;
 			Map.Entry<Integer, Float> nextDocumentEntry = null;
 			
 			if(queryIterator.hasNext() && documentIterator.hasNext()) {
 				nextDocumentEntry = documentIterator.next();
 				nextQueryEntry = queryIterator.next();
 			}
 			
 			while(queryIterator.hasNext() && documentIterator.hasNext()) {
 				if (nextQueryEntry.getKey() == nextDocumentEntry.getKey()) {
 					if (nextDocumentEntry.getValue() == 0.0f || nextQueryEntry.getValue() == 0.0f) {
 						nextQueryEntry = queryIterator.next();
 						nextDocumentEntry = documentIterator.next();
 						continue;
 					}
 					
 					similarity += scoringMethod.score(nextQueryEntry.getValue(), nextDocumentEntry.getValue(), 0, collectionStatistics.getNumberOfTokens(), documentLength);
 					
 					if(scoringMethod.requiresVectorLengths()) {
 						documentVectorLength += Math.pow(nextDocumentEntry.getValue(), 2.0);
 						queryVectorLength += Math.pow(nextQueryEntry.getValue(), 2.0);
 					}
 					
 					nextDocumentEntry = documentIterator.next();
 					nextQueryEntry = queryIterator.next();	
 				}
 				else if (nextQueryEntry.getKey() < nextDocumentEntry.getKey()) { // the current document does not contain this term, try next term
 					if(scoringMethod.requiresVectorLengths()) {
 						queryVectorLength += Math.pow(nextQueryEntry.getValue(), 2.0);
 					}
 					
 					nextQueryEntry = queryIterator.next();
 				}
 				else { // have to step up document vector
 					if(scoringMethod.requiresVectorLengths()) {
 						documentVectorLength += Math.pow(nextDocumentEntry.getValue(), 2.0);
 					}
 					
 					nextDocumentEntry = documentIterator.next();
 				}
 			}
 			
 			if(scoringMethod.requiresVectorLengths()) {
 				while (queryIterator.hasNext()) {
 					queryVectorLength += Math.pow(queryIterator.next().getValue(), 2.0);
 				}
 				while (documentIterator.hasNext()) {
 					documentVectorLength += Math.pow(documentIterator.next().getValue(), 2.0);
 				}
 				
 				queryVectorLength = Math.sqrt(queryVectorLength);
 				documentVectorLength = Math.sqrt(documentVectorLength);
 				
 				similarity = scoringMethod.useVectorLenghts(similarity, queryVectorLength, documentVectorLength);
 			}
 			
 			currentRetrievalResult.setSimilarity((float)(similarity));
 			currentRetrievalResult.setSize(indexName);
 			tmpSearchResults.add(currentRetrievalResult, similarity);
 		}
 		
 		indexScanner.close();
 		Iterator<RetrievalResult> resultIterator = tmpSearchResults.getAllScores().iterator();
 		int counter = 1;
 		while (resultIterator.hasNext()) {
 			RetrievalResult res = resultIterator.next();
 			res.setPlacement(counter++);
 			searchResults.add(res);
 		}
 	}
 	
 	private void parseQuery(File collectionFile, String queryDocument) throws ZipException, IOException {
 		ZipTokenStream zipTokenStream = new ZipTokenStream(collectionFile, useStemming);
 		zipTokenStream.restrictToFile(queryDocument);
 		zipTokenStream.initialize();
 		
 		DocumentTermList queryTermList = new DocumentTermList();
 		
 		while(zipTokenStream.hasNext()) { // we assume that the query fits comfortably into the main memory.
 			Token nextToken = zipTokenStream.next();
 			int termID = vocabulary.add(nextToken.getTerm(), nextToken.getDoc().getId()).getTermID(); // we add the term to the overall vocabulary (needed if our query document contains new terms).
 			queryTermList.add(termID);
 		}
 		vocabulary.setTotalNumberOfDocuments(1); // we have only one query document
 		vocabulary.finalize();
 		queryTermList.sortTermsByID();
 		
 		fastAccessQueryVector = new ArrayList<Map.Entry<Integer, Float>>(queryTermList.getDocTermEntries().entrySet());
 		
 		if(!scoringMethod.requiresPlainTf()) {
 			Iterator<Map.Entry<Integer, Float>> queryIterator = fastAccessQueryVector.iterator();
 			while(queryIterator.hasNext()) {
 				Map.Entry<Integer, Float> nextQueryEntry = queryIterator.next();
 				nextQueryEntry.setValue((float)Math.log10(1 + nextQueryEntry.getValue()));
 			}
 		}
 	}
 	
 	private void loadCollectionStatistics() {
 		File statisticsFile = new File(indexDirectory + indexName + ".stat");
 		collectionStatistics = new CollectionStatistics();
 		
 		try {
 			collectionStatistics.readFromFile(statisticsFile);
 		}
 		catch (IOException ioe) {
 			System.err.println("Error reading from statisticsFile "+statisticsFile.getPath()+"! -- Stack Trace follows.");
 			ioe.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Convenience method to process a bunch of queries and write them to their default output files.
 	 * @param K
 	 * @param collectionFile
 	 * @param queryFile
 	 * @throws IOException 
 	 */
 	public void retrieveAndWriteQueries(int K, File collectionFile, File queryFile) throws IOException {				
 		try {
 			Scanner queriesScanner = new Scanner(new FileInputStream(queryFile));
 			int queryCounter = 1;
 			System.out.println("Working... ");
 			String collectionFileName = collectionFile.getName().substring(0, collectionFile.getName().lastIndexOf('.'));
 			while (queriesScanner.hasNext()) {
 				String queryDocument = collectionFileName+"/"+queriesScanner.nextLine(); // 20_newsgroups_subset/
 				retrieveTop(K, collectionFile, queryDocument);
 				writeResultsToFile(queryCounter);
 				System.out.println(queryCounter+" queries processed.");
 				queryCounter++;
 			}
 			System.out.println("Done!");
 		}
 		catch (IOException ioe) {
 			System.err.println("IOException while reading queryFile!");
 			ioe.printStackTrace();
 		}
 	}
 	
 	public ArrayList<RetrievalResult> retrieveTop(int K, File collectionFile, String queryDocument) throws IOException {
 		GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(new File(indexDirectory+indexName+scoringMethod.getRequiredIndexSuffix()+".arff.gz")));
 		indexScanner = new Scanner(gzis);
 		
 		searchResults = new ArrayList<RetrievalResult>(K);
 		tmpSearchResults = new SizedPriorityQueue<RetrievalResult>(K, true);
 		
 		loadVocabularyFromIndex();
 		
 		loadCollectionStatistics();
 		
 		parseQuery(collectionFile, queryDocument);
 		
 		queryIndex();
 		
 		return searchResults;
 	}
 
 	public void writeResultsToFile(int numberOfQuery) throws IOException {
 		if (searchResults.size() == 0) {
 			System.out.println("No results to write to file!");
 			return;
 		}
 
 		searchResults.get(0).setTopicNumber(numberOfQuery);
 		String resultFilePath = "output"+java.io.File.separator+searchResults.get(0).getDesiredFilename();
 		
 		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(resultFilePath))));//, "UTF-8"));
 		
 		Iterator<RetrievalResult> resultsIterator = searchResults.iterator();
 		while (resultsIterator.hasNext()) {
			out.write(resultsIterator.next().toString()+(resultsIterator.hasNext()?"\n":""));
 		}
 		
 		out.close();	
 	}
 }
