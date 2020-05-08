 package edu.mwdb.project;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import matlabcontrol.MatlabConnectionException;
 import matlabcontrol.MatlabInvocationException;
 import matlabcontrol.MatlabProxy;
 
 import matlabcontrol.extensions.MatlabNumericArray;
 import matlabcontrol.extensions.MatlabTypeConverter;
 
 import org.apache.lucene.analysis.CharArraySet;
 import org.apache.lucene.analysis.StopAnalyzer;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.TermFreqVector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 public class UserCompLDA1 {
 	String[] authorsStaticVocabulary = null;
 	public double[][]  rebuildAuthorMatrix(String personId) throws IOException {
 
 		Utility util = new Utility();
 		Connection con = util.getDBConnection();
 		List<String> rowData = new ArrayList<String>();
 
 		Statement stmt;
 		try {
 			stmt = con.createStatement();
 		
 
 		
 		// To get the list of papers written by the given author.
 		String query_authorid = 
 				"select p.abstract from papers p join " +  
 						"(select distinct w.paperid from " + 
 						"authors a join writtenby w where a.personid = w.personid and a.personid = " + personId  + 
 						" order by paperid) T1 on p.paperid = T1.paperid where p.abstract != \"\"";
 
 		ResultSet rs = stmt.executeQuery(query_authorid);
 		while (rs.next())
 		{
 			rowData.add(rs.getString("abstract"));
 		}
 
 		// To know the count of abstracts in the DB and store it in noOfDocs.
 		Statement stmt1 = con.createStatement();
 		int noOfDocs = 0;
 		String query_alldocs_count = "SELECT count(*) AS count FROM papers WHERE abstract != \"\"";
 		ResultSet rs1 = stmt1.executeQuery(query_alldocs_count);
 		while (rs1.next()) 
 		{
 			noOfDocs = rs1.getInt("count");
 		}
 
 		//Extract the abstracts from the DB
 		Statement stmt2 = con.createStatement();
 		String query_alldocs = "SELECT abstract FROM papers WHERE abstract != \"\"";
 		ResultSet rs2 = stmt2.executeQuery(query_alldocs);
 
 		// Creation of a Index Directory.
 		StopAnalyzer docAnalyzer = new StopAnalyzer(Version.LUCENE_36,Utility.getStopWordsFile());
 		IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_36,docAnalyzer);
 		Directory indexDirectory = new RAMDirectory();
 		IndexWriter indexWr = new IndexWriter(indexDirectory, indexConfig);
 
 		String doc="";
 
 		// Adding a field 'doc' from the abstract to create an indexed document.
 		while (rs2.next()) 
 		{
 			doc = rs2.getString("abstract");
 			Document document = new Document();
 			document.add(new Field("doc", doc, Field.Store.YES,Field.Index.ANALYZED));
 			indexWr.addDocument(document);
 			indexWr.commit();
 		}
 
 		CharArraySet stopWordsCharArrSet;
 		TokenStream docStream;
 		TokenStream keywords;
 		int noOfWords = 0;
 
 		Map<String,Float> termFreq = new HashMap<String, Float>();
 		KeywordConfig config;			
 		List<KeywordConfig> configList = new ArrayList<KeywordConfig>();
 
 		// List of Lists - where each list stores the keywords of the respective documents.
 		List<List<String>> docKeywords = new ArrayList<List<String>>();
 
 		// List of Maps - where each list stores the IDF map of the respective documents.
 		List<Map<String,Float>> docIdfMapList = new ArrayList<Map<String,Float>>();
 		stopWordsCharArrSet = new CharArraySet(Version.LUCENE_36, util.createStopWordsSet(), true);
 
 		for (int i=0;i<rowData.size();i++)
 		{
 			String[] rowDataArr = rowData.get(i).split("[ ]+");
 			noOfWords += rowDataArr.length;
 
 			//Creating the Character Array Set from the list of stop words
 
 			//Creating a token stream from the abstract got from the DB for the given paperId
 			docStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(rowData.get(i)));
 
 			//Creating the Keywords of a given abstract
 			keywords = new StopFilter(Version.LUCENE_36, docStream ,stopWordsCharArrSet );
 
 			termFreq = util.createauthorTF(keywords, rowData.get(i));
 
 			List<String> keywordsList = new ArrayList<String>();
 			for(Map.Entry<String, Float> k : termFreq.entrySet())
 			{
 				keywordsList.add(k.getKey());
 			}
 			docKeywords.add(keywordsList);
 
 			//					Map<String,Float> idfMap = utilityObj.createTFIDF(noOfDocs,indexDirectory, termFreq,"TF");
 			Map<String,Float> idfMap = new HashMap<String,Float>(termFreq);
 			docIdfMapList.add(idfMap);
 
 			for(Map.Entry<String, Float> keys : termFreq.entrySet())
 			{
 				config = new KeywordConfig();
 				config.setKeyword(keys.getKey());
 				config.setWeightedFreq(keys.getValue());
 				configList.add(config);
 			}
 		}
 
 		Map<String,Float> termFinalFreq = new HashMap<String, Float>();
 		for (KeywordConfig itr: configList){
 			Float val = termFinalFreq.get(itr.getKeyword());
 			termFinalFreq.put(itr.getKeyword(), (val == null) ? itr.getWeightedFreq() : (val + itr.getWeightedFreq()));
 		}	
 		/* do not normalize for lda */
 		/*
 			for(Map.Entry<String, Float> k: termFinalFreq.entrySet())
 			{
 				termFinalFreq.put(k.getKey(), k.getValue()/noOfWords);
 			}
 		 */
 		int rowSize = rowData.size();
 		int columnSize = termFinalFreq.size();
 
 //		task1aLDA lda = new task1aLDA();
 	
 
 		authorsStaticVocabulary = makeStaticKeywordList(termFinalFreq );
 
 		return  makeDocumentMatrix(rowSize, columnSize,  docKeywords,  termFinalFreq, docIdfMapList);
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 		
 		
 	}
 
 	public List<HashMap<String,Float>> doLDA(double[][] docKeywordCorpusMatrix,String[] staticVocabulary) throws Exception{
 
 		MatlabProxy proxy = MatLab.getProxy();
 		LDAPrep ldaInputs = new LDAPrep();
 
 		ldaInputs.doLDAPrepFullMatrix(docKeywordCorpusMatrix);
 		//ldaInputs.makeDictionaryFile(staticVocabulary);
 
 		double[][] WS = new double[1][ldaInputs.WS.length];
 		WS[0] = ldaInputs.WS;
 		double[][] DS = new double[1][ldaInputs.DS.length];
 		DS[0] = ldaInputs.DS;
 
 		double[] WS3 = ldaInputs.WS;
 		/* lose precision this way */    
 		//	proxy.setVariable("WS3",WS3);
 		// proxy.setVariable("DS",DS);
 		String currentPath = Utility.getCurrentFolder();
 		proxy.eval("cd "+currentPath);
 
 
 		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
 
 		processor.setNumericArray("WS", new MatlabNumericArray(WS, null));
 		processor.setNumericArray("DS", new MatlabNumericArray(DS, null));
 
 		/* set the number of latent semantics to retrieve */
 		double T = 5.0;
 		proxy.setVariable("T", 5);
 		proxy.setVariable("WO", staticVocabulary);
 		proxy.eval("[WPALL,DPALL,ZALL] = LDA1(WS,DS,T,WO)");
 
 		double[][] WP = processor.getNumericArray("WPALL").getRealArray2D();
 		double[][] DP = processor.getNumericArray("DPALL").getRealArray2D();
 		double[][] Z = processor.getNumericArray("ZALL").getRealArray2D();
 
 		//Do processing of Topics probability Matrix generated by matlab in text file to display top k topics
 
 		/* numImportantTopics = T,  numRelevantWords = 7 + 1 for header */
 		String filename = currentPath + "/" + "topics.txt";
 		//				 List<KeywordConfig>[] topicsconfigList = ldaInputs.readLDATopics( filename,  5, 8);
 		List<KeywordConfig>[] topicsconfigList = ldaInputs.readPrintTopics(filename, 5,8);
 		
 		/* for consistency convert to list of HashMap */
 		List<HashMap<String,Float>> topicsList = new ArrayList<HashMap<String,Float>>();
 		for(List<KeywordConfig> aTopic : topicsconfigList){
 			HashMap<String,Float> topicFreq = new HashMap<String,Float>(aTopic.size());
 				for(KeywordConfig term : aTopic){
 					String item = term.getKeyword();
 					Float  freq = term.getWeightedFreq();
 					topicFreq.put(item,freq);
 			}
 			topicsList.add(topicFreq);
 		}
 
 		return topicsList;
 
 	}
 	
 
 	public double[][] getDocumentMatrix(Directory luceneIndexDir, List<String> completeKeywordList) throws Exception{
 		Utility util = new Utility();
 		DblpData db = new DblpData();
 		
 
 		IndexReader reader = IndexReader.open(luceneIndexDir);
 		double[][] documentMatrix = new double[reader.maxDoc()][completeKeywordList.size()];
 		
 		int termIdx = 0;
 		Map<String, Integer> allKeywordsPosMap = new HashMap<String, Integer>();
 		for(String kw:completeKeywordList){
 			allKeywordsPosMap.put(kw, termIdx++);
 		}
 
 			try {
 					
 			for (int i = 0; i < reader.maxDoc(); i++) {
 				TermFreqVector tfv = reader.getTermFreqVector(i, "doc");
 				double[] doc1 = Utility.getAlignedTermFreqVector(tfv,allKeywordsPosMap);
 				documentMatrix[i] = doc1;
 				
 			}
 			reader.close();
 			 
 		} catch (CorruptIndexException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return documentMatrix;
 	}
 	
 	
 	public double[][] buildTopicsMatrix(List<HashMap<String, Float>> topicsList, List<String> completeKeywordList) {
 		Utility util = new Utility();
 		DblpData db = new DblpData();
 		
 
 	
 		double[][] topicsDocumentMatrix = new double[topicsList.size()][completeKeywordList.size()];
 		
 		int termIdx = 0;
 		Map<String, Integer> allKeywordsPosMap = new HashMap<String, Integer>();
 		for(String kw:completeKeywordList){
 			allKeywordsPosMap.put(kw, termIdx++);
 		}
 
 			
 			int topicCounter = 0;	
 			for (HashMap<String, Float> singleTopicSignificantWords  : topicsList) {
 				String[] wordVector =  new String[singleTopicSignificantWords.size()];	
 				float[]  wordFreqVector = new float[singleTopicSignificantWords.size()];
 			
 				/* extract topic word and frequency arrays */
 				int i =0;
 				for(Map.Entry<String,Float> wordFreqentry : singleTopicSignificantWords.entrySet()){
 					wordVector[i] = wordFreqentry.getKey();
 					wordFreqVector[i] = wordFreqentry.getValue();
 					i++;
 				}
 				
 				double[] doc1 = getAlignedWordVector(wordVector, wordFreqVector, allKeywordsPosMap);
 				topicsDocumentMatrix[topicCounter] = doc1;
 				topicCounter++;
 			}
 				
 		return topicsDocumentMatrix;
 	}
 	
 	
 	public double[][] makeDocumentMatrix(int rowSize, int columnSize, List<List<String>> docKeywords, Map<String,Float> termFinalFreq, List<Map<String,Float>> docMapList){
 		
 		double docKeywordCorpusMatrix[][] = new double[rowSize][columnSize];
 		// Build the input Corpus matrix.
 		for(int row=0;row<rowSize;row++)
 		{
 			List<String> tempList = docKeywords.get(row);
 			for(int column=0;column<columnSize;column++)
 			{
 				for(Map.Entry<String, Float> k: termFinalFreq.entrySet())
 				{
 					for(int i=0;i<tempList.size();i++)
 					{
 						if(k.getKey().equals(tempList.get(i)))
 						{
 							docKeywordCorpusMatrix[row][column] = docMapList.get(row).get(k.getKey());
 							break;
 						}
 						docKeywordCorpusMatrix[row][column] = 0;
 					}
 					if (column<columnSize-1)
 						column++;
 				}
 			}
 		}
 		return docKeywordCorpusMatrix;
 	}
 	
 	/*
 	 * Similar to getAlignedTermFreqVector function in Utility but with
 	 * different input parameters
 	 */
 	public static double[] getAlignedWordVector(String[] termTexts,
 			float[] termFreqs, Map<String, Integer> allKeywordsPosMap) {
 
 		double[] alignedVector = new double[allKeywordsPosMap.keySet().size()];
 
 		for (int i = 0; i < termTexts.length; i++) {
 			if (!allKeywordsPosMap.containsKey(termTexts[i])) {
 				// System.out.println(termTexts[i]);
 			} else {
 				int j = allKeywordsPosMap.get(termTexts[i]);
 				if (j != -1) {
 					alignedVector[j] = termFreqs[i];
 				}
 			}
 		}
 		return alignedVector;
 	}
 	
 	/*
 	*  Helper method that creates a static word list that will reflect the indices in
 	*  the data matrix.  HashMap does not guarantee iterative order.
 	*/
 		public String[] makeStaticKeywordList(Map<String,Float> termFinalFreq ){
 			String[] staticVocabulary = new String[(termFinalFreq.size())];
 			int i = 0;
 			for(Map.Entry<String, Float> k: termFinalFreq.entrySet()){
 				staticVocabulary[i] = new String(k.getKey());
 				i++;
 			}
 			/* */
 			List<String> allterms = new ArrayList(termFinalFreq.keySet());
 			staticVocabulary = allterms.toArray(new String[allterms.size()]);
 		return staticVocabulary;
 		}
 		
 
 /*
 *  Helper method that creates a static word list that will reflect the indices in
 *  the data matrix.  HashMap does not guarantee iterative order.
 */
 	public String[] makeStaticKeywordList2(Map<String,Integer> termPositions ){
 		String[] staticVocabulary = new String[(termPositions.size())];
 		int i = 0;
 		for(Map.Entry<String, Integer> k: termPositions.entrySet()){
 			staticVocabulary[k.getValue()] = new String(k.getKey());
 			
 		}
 		
 	return staticVocabulary;
 	}
 	
 		
 	public Object[] doLatentCompare(double[][] topicsMatrix, double[][] allUsersKeyWordMatrix, double[][] authorDataMatrix, double[] authKeywordMatrix) throws MatlabInvocationException, MatlabConnectionException, Exception{
 		//LDA - Start
 		DblpData db = new DblpData();
 		
 			System.out.println("Starting to compute similarity using top 5 semantics LDA");
 			
 			double[][] inputCorpusMatrix = allUsersKeyWordMatrix;
 			double[][] UserMatrix = authorDataMatrix;
 			double[][] latentsMatrix = topicsMatrix;
 			double[][] givenauthKeywordMatrix = new double[1][authKeywordMatrix.length];
 			
 			givenauthKeywordMatrix[0] = authKeywordMatrix;
 			
 		
 			//Create a proxy, which we will use to control MATLAB
 			MatlabProxy proxy = MatLab.getProxy();
 			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
 
 			String currentPath = Utility.getCurrentFolder();
 			proxy.eval("cd "+currentPath);
 			
 			processor.setNumericArray("givenAuthKWArray", new MatlabNumericArray(givenauthKeywordMatrix, null));
 			processor.setNumericArray("inputCorpusMatrix", new MatlabNumericArray(inputCorpusMatrix, null));
 			processor.setNumericArray("userMatrix", new MatlabNumericArray(UserMatrix, null));
 			processor.setNumericArray("latentsMatrix", new MatlabNumericArray(latentsMatrix,null));
 
             	
 			proxy.eval("[ALLUSERSLDAMatrix] = inputCorpusMatrix * transpose(latentsMatrix)");
 			proxy.eval("[AUTHORLDAMatrix] = givenAuthKWArray * transpose(latentsMatrix)");
 
 			
 		
 			Object[] ldaObj = proxy.returningEval("AUTHORLDAMatrix(1,:)", 1);
 				
 			double[][]ldaSemUserMatrix = new double[1][authKeywordMatrix.length];
 			
 			ldaSemUserMatrix[0] = (double[]) ldaObj[0];
 			
 		
 			processor.setNumericArray("userMatrixLDA", new MatlabNumericArray(ldaSemUserMatrix, null));
 			
 			Object[] objLDA = new Object[2];
 
			objLDA = proxy.returningEval("knnsearch( ALLUSERSLDAMatrix, userMatrixLDA,'k', 11,'Distance','cosine')",2);
 			
 			return  objLDA;
 	
 		
 	}
 
 public TermFreqVector getAuthorKeywordVector(String authorId,Directory luceneIndexDir) throws CorruptIndexException, IOException
 {	DblpData db = new DblpData();
  Map<String, TermFreqVector>  allAuthors = db.getAuthorTermFrequencies(luceneIndexDir);
  TermFreqVector tfv = allAuthors.get(authorId);
  return tfv;
 }
 	
 public void doLatentSemantics(String authorid) throws Exception{
 		
 		DblpData db = new DblpData();
 		Directory dirIndex = db.createAuthorDocumentIndex();
 		Directory dirAllPapersIndex = db.createAllDocumentIndex();
 		
 		
 		List<String> allterms =	db.getAllTermsInIndex(dirAllPapersIndex, "doc");
 		
 		Map<String, Integer> wordPosMap = findMap(allterms);
 	
 		
 		String[] staticVocabulary = allterms.toArray(new String[allterms.size()]);
 		String[] staticVocabularyAlternate = makeStaticKeywordList2(wordPosMap);
 		
 		
 		double[][] authorsKWDocumentMatrix = getDocumentMatrix(dirIndex,  allterms);	
 		
 		
 		// place holder
 		
 		double[][] authorDataMatrix = rebuildAuthorMatrix(authorid);
 		
 		double[] authorKeywordVector  = Utility.getAlignedTermFreqVector(getAuthorKeywordVector(authorid, dirIndex), wordPosMap);
 	
 		List<HashMap<String,Float>> topics = doLDA(authorDataMatrix, authorsStaticVocabulary );
 		
 //
 		
 		double[][] completeTopicsMatrix = buildTopicsMatrix(topics, allterms);
 
 			/* Project LDA onto other data */
 		Object[] objLDA = doLatentCompare(completeTopicsMatrix, authorsKWDocumentMatrix, authorDataMatrix, authorKeywordVector);	
 
 		double[] indexLDA = (double[]) objLDA[0]; 
 		double[] distLDA = (double[]) objLDA[1];
 		display(indexLDA, distLDA, authorid, dirIndex);
 	}
 	
 
 	public void display(double[] indexLDA, double[] distLDA, String authorid,Directory AuthorIndex) {
 
 		DblpData db = new DblpData();
 
 		HashMap<String, String> authNamePersonIdList = db.getAuthNamePersonIdList();
 		List<String> authorIds = db.getAllActiveAuthors(AuthorIndex);
 		String[] authors = authorIds.toArray(new String[authorIds.size()]);
 		System.out.println("For Author: " + authNamePersonIdList.get(authorid));
 		if (authNamePersonIdList.get(authors[(int) (indexLDA[0] - 1)]) == null) {
 			for (int i = 0; i < 10; i++) {
 				System.out.println("Author Name: "
 						+ authors[(int) (indexLDA[0] - 1)] + " "
 						+ "Distance from given author: " + distLDA[i]);
 			}
 		} else {
 			for (int j = 0; j < indexLDA.length; j++) {
 				if (!authorid.equals(authors[(int) (indexLDA[j] - 1)])) {
 					System.out.printf("Author Name: %-25s Distance from given author: %10.9f",
 									authNamePersonIdList.get(authors[(int) (indexLDA[j] - 1)]),	distLDA[j]);
 					System.out.println();
 				}
 			}
 		}
 
 		// LDA - End
 	}
 	
 	public Map<String, Integer> findMap(List<String> completeKeywordList){
 		int termIdx = 0;
 		Map<String, Integer> allKeywordsPosMap = new HashMap<String, Integer>();
 		for(String kw:completeKeywordList){
 			allKeywordsPosMap.put(kw, termIdx++);
 		}
 		return allKeywordsPosMap;
 	}
 
 }
 	
