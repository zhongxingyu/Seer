 package edu.mwdb.project;
 
 
 
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import matlabcontrol.MatlabProxy;
 import matlabcontrol.MatlabProxyFactory;
 import matlabcontrol.extensions.MatlabNumericArray;
 import matlabcontrol.extensions.MatlabTypeConverter;
 
 import org.apache.lucene.analysis.CharArraySet;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 public class task1aLDA {
 
 
 	public double[][] makeDocumentMatrix(int rowSize, int columnSize, List<List<String>> docKeywords, Map<String,Float> termFinalFreq, List<Map<String,Float>> docIdfMapList){
 
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
 							docKeywordCorpusMatrix[row][column] = docIdfMapList.get(row).get(k.getKey());
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
 
 	/**
 	 * @param args
 	 */
 	public void doTask(String personId) {
 		// TODO Auto-generated method stub
 		Utility utilityObj = new Utility();
 		try 
 		{
 			Connection con = utilityObj.getDBConnection();
 			// List to store the author abstracts
 			List<String> rowData = new ArrayList<String>();
 
 			Statement stmt = con.createStatement();
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
 			StandardAnalyzer docAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
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
 			stopWordsCharArrSet = new CharArraySet(Version.LUCENE_36, utilityObj.createStopWordsSet(), true);
 
 			for (int i=0;i<rowData.size();i++)
 			{
 				String[] rowDataArr = rowData.get(i).split("[ ]+");
 				noOfWords += rowDataArr.length;
 
 				//Creating the Character Array Set from the list of stop words
 
 				//Creating a token stream from the abstract got from the DB for the given paperId
 				docStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(rowData.get(i)));
 
 				//Creating the Keywords of a given abstract
 				keywords = new StopFilter(Version.LUCENE_36, docStream ,stopWordsCharArrSet);
 
 				termFreq = utilityObj.createauthorTF(keywords, rowData.get(i));
 
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
 
 			task1aLDA lda = new task1aLDA();
 
 
 			String[] staticVocabulary = lda.makeStaticKeywordList(termFinalFreq );
 
 			double docKeywordCorpusMatrix[][] = lda.makeDocumentMatrix(rowSize, columnSize,  docKeywords,  termFinalFreq, docIdfMapList);
 
 			/*
 
 
 				// Print the i/p Corpus matrix.
 				for (int row=0;row<rowSize;row++)
 				{
 					for(int column=0;column<columnSize;column++)
 					{
 						System.out.print(docKeywordCorpusMatrix[row][column] + "\t");
 					}
 					System.out.println();
 				}
 
 			 */			
 
 
 			//Create a proxy, which we will use to control MATLAB
 			MatlabProxy proxy = MatLab.getProxy();
 
 
 			//set matlab path
			String currentPath = task1aLDA.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString().substring(6);
			System.out.println(currentPath);
 			proxy.eval("cd "+currentPath);
 
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
 
 
 			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
 
 			processor.setNumericArray("WS", new MatlabNumericArray(WS, null));
 			processor.setNumericArray("DS", new MatlabNumericArray(DS, null));
 			/* set the number of latent semantics to retrieve */
 			proxy.setVariable("T", 5);
 			proxy.setVariable("WO", staticVocabulary);
 			proxy.eval("[WPALL,DPALL,ZALL] = LDA1(WS,DS,T,WO)");
 
 			double[][] WP = processor.getNumericArray("WPALL").getRealArray2D();
 			double[][] DP = processor.getNumericArray("DPALL").getRealArray2D();
 			double[][] Z = processor.getNumericArray("ZALL").getRealArray2D();
 
 			//Do processing of Topics probability Matrix generated by matlab in text file to display top k topics
 
 			/* numImportantTopics = T,  numRelevantWords = 7 + 1 for header */
 			String filename = "topics.txt";
 			//				 List<KeywordConfig>[] topicsconfigList = ldaInputs.readLDATopics( filename,  5, 8);
 			ldaInputs.readPrintTopics(filename, 5,8);
 
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 
 }
 
 
 
