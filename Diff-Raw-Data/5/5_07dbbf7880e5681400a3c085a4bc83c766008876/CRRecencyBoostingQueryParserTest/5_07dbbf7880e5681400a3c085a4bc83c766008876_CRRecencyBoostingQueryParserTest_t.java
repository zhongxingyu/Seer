 package com.gentics.cr.lucene.search.query;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.Query;
 
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.AbstractLuceneTest;
 import com.gentics.cr.lucene.LuceneVersion;
 import com.gentics.cr.lucene.search.query.mocks.ComparableDocument;
 import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;
 
 public class CRRecencyBoostingQueryParserTest extends AbstractLuceneTest {
 	
 	private static final StandardAnalyzer STANDARD_ANALYZER = new StandardAnalyzer(LuceneVersion.getVersion());
 	private static final String[] SEARCHED_ATTRIBUTES = new String[] { SimpleLucene.CONTENT_ATTRIBUTE, "binarycontent" };
 	private CRRecencyBoostingQueryParser parser;
 	private CRRequest crRequest;
 	private SimpleLucene lucene;
 	private ArrayList<ComparableDocument> documents;
 	
 	public CRRecencyBoostingQueryParserTest(String name) throws CorruptIndexException, IOException {
 		super(name);
 	}
 
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		GenericConfiguration config = new GenericConfiguration();
 		Properties p = new Properties();
 		p.setProperty("MULTIPLICATORBOOST", "4.0");
 		p.setProperty("BOOSTATTRIBUTE", "updatetimestamp");
 		p.setProperty("TIMERANGE", "1296000"); // 15 days
 
 		config.setProperties(p);
 
 		crRequest = new CRRequest();
 		lucene = new SimpleLucene();
 		parser = new CRRecencyBoostingQueryParser(config,
 					LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
 
 
 		documents = new ArrayList<ComparableDocument>();
 		/* 0 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word1 1.10.",
 			"updatetimestamp:1349042400",
 			"node_id:2"))); // 01.10.2012 00:00:00
 		/* 1 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word2 word9",
 			"updatetimestamp:1349042400",
 			"node_id:8"))); // 01.10.2012 00:00:00
 		/* 2 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word3",
 			"updatetimestamp:1347228000",
 			"binarycontent:word9",
 			"node_id:8"))); // 10.09.2012 00:00:00
 		/* 3 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word1 1.9.",
 			"updatetimestamp:1346450400",
 			"node_id:7"))); // 01.09.2012 00:00:00
 		/* 4 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word5",
 			"updatetimestamp:1346450400",
 			"node_id:8"))); // 01.09.2012 00:00:00
 		/* 5 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word1 word1 1.10.",
 			"updatetimestamp:1349042400",
 			"node_id:1"))); // 01.10.2012 00:00:00
 		/* 6 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word1 20.9.",
 			"updatetimestamp:1348092000",
 			"node_id:6"))); // 20.09.2012 00:00:00
 		/* 7 */documents.add(new ComparableDocument(lucene.add(
 			SimpleLucene.CONTENT_ATTRIBUTE + ":word1 28.9.",
 			"updatetimestamp:1348783200",
 			"node_id:4"))); // 28.09.2012 00:00:00
 		/* 8 */documents.add(new ComparableDocument(lucene.add(
 				SimpleLucene.CONTENT_ATTRIBUTE + ":word1 word1 word1 word1 word1 word1 word1 1.9.",
 				"updatetimestamp:1346450400",
 				"node_id:5"))); // 01.09.2012 00:00:00
 	}
 
 	public void testBoostingWithCRRecencyBoostingQueryParser() throws ParseException, CorruptIndexException, IOException {
 		Query orginalQuery = parser.parse("word1");
 
 		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(orginalQuery));
 		assertEquals(6, matchedDocuments.size());
 		Iterator<ComparableDocument> i = matchedDocuments.iterator();
 
 		assertEquals("Ordering of the matching collection not expected! First entry must be the 6th document.",
 					i.next(), documents.get(5));
 		assertEquals("Ordering of the matching collection not expected! Second entry must be the 1th document.",
 					i.next(), documents.get(0));
 		assertEquals("Ordering of the matching collection not expected! Third entry must be the 8th document.",
 					i.next(), documents.get(7));
 		assertEquals("Ordering of the matching collection not expected! Fourth entry must be the 9th document.",
 					i.next(), documents.get(8));
		assertEquals("Ordering of the matching collection not expected! Fifth entry must be the 4th document.",
 					i.next(), documents.get(3));
		assertEquals("Ordering of the matching collection not expected! Sixth entry must be the 7th document.",
 					i.next(), documents.get(6));
 	}
 	
 	public void testCRRecencyBoostingQueryCalculation() throws ParseException, CorruptIndexException, IOException {
 		CRRecencyBoostingQuery query = new CRRecencyBoostingQuery(parser.parse("word1"), 4, 1296000, "updatetimestamp");
 		
 		float result = query.getCustomScoreProvider(lucene.getReader()).customScore(0, 2, 1);
 		long currentTime = System.currentTimeMillis() / 1000;
 
 		float testingResult = 0;
 		long publishedTime = 1349042400;
 
 		long timeAgo = currentTime - publishedTime;
 		float boost = (float) (4) * (1296000 - timeAgo) / 1296000;
 		testingResult = (float) (2 * (1 + boost));
 
 		assertEquals("Comparison between the boost-calculating of the first document issn't correct!",
 				result, testingResult);
 	}
 }
