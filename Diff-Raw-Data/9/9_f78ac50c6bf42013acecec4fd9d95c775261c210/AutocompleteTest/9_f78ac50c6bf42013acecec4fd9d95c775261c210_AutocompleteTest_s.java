 package com.gentics.cr.lucene.autocomplete;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.TermVector;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.gentics.cr.CRConfigStreamLoader;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.configuration.EnvironmentConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 
 public class AutocompleteTest {
	private static CRConfigUtil config = null;
 	private static CRConfigUtil autoConfig = null;
 	private static RequestProcessor rp=null;
 	private static LuceneIndexLocation location=null;
 	
 	@BeforeClass
 	public static void setUp() throws CRException, URISyntaxException, IOException {
 		EnvironmentConfiguration.loadEnvironmentProperties();
		config = new CRConfigStreamLoader("search", AutocompleteTest.class.getResourceAsStream("search.properties"));
 		autoConfig = new CRConfigStreamLoader("autocomplete", AutocompleteTest.class.getResourceAsStream("autocomplete.properties"));
 		rp = autoConfig.getNewRequestProcessorInstance(1);
 		CRConfigUtil rpConfig = autoConfig.getRequestProcessorConfig(1);
 		location = LuceneIndexLocation.getIndexLocation(rpConfig);
 		IndexAccessor accessor = location.getAccessor();
 		
 		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.1");
 		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.2");
 		addDoc(accessor, "content:saab", "category:cars", "contentid:10007.3");
 		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.4");
 		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.5");
 		addDoc(accessor, "content:pagani", "category:cars", "contentid:10007.6");
 		addDoc(accessor, "content:potatoe", "category:plants", "contentid:10007.7");
 		addDoc(accessor, "content:flower", "category:plants", "contentid:10007.8");
 		addDoc(accessor, "content:tree", "category:plants", "contentid:10007.9");
 		
 		AutocompleteIndexExtension autoExtension = new AutocompleteIndexExtension(rpConfig, location);
 		AutocompleteIndexJob aIJ = new AutocompleteIndexJob(autoConfig, location, autoExtension);
 		aIJ.run();
 	}
 	
 	@Test
 	public void testConfig() {
 		Assert.assertNotNull(rp);
 	}
 	
 	@Test
 	public void testSingleAutocomplete() throws CRException {
 		CRRequest request = new CRRequest();
 		request.setRequestFilter("a");
 		Collection<CRResolvableBean> objects = rp.getObjects(request);
 		Assert.assertEquals("The Search did not find all items.", 1, objects.size());
 		CRResolvableBean bean = objects.iterator().next();
 		Assert.assertEquals("Autocomplete result did not return the expected word", "audi", bean.get("word"));
 		Assert.assertEquals("Autocomplete result did not return the expected word count", "1", bean.get("count"));
 	}
 	
 	@Test
 	public void testMultiAutocomplete() throws CRException {
 		CRRequest request = new CRRequest();
 		request.setRequestFilter("p");
 		Collection<CRResolvableBean> objects = rp.getObjects(request);
 		Assert.assertEquals("The Search did not find all items.", 2, objects.size());
 		CRResolvableBean bean = objects.iterator().next();
 		Assert.assertEquals("Autocomplete result did not return the expected word", true, "potatoe".equals(bean.get("word")) || "pagani".equals(bean.get("word")));
 		Assert.assertEquals("Autocomplete result did not return the expected word count", "1", bean.get("count"));
 		bean = objects.iterator().next();
 		Assert.assertEquals("Autocomplete result did not return the expected word", true, "potatoe".equals(bean.get("word")) || "pagani".equals(bean.get("word")));
 		Assert.assertEquals("Autocomplete result did not return the expected word count", "1", bean.get("count"));
 	}
 	
 	/**
 	 * Adds a Document to the index.
 	 * @param ia
 	 * @param fields
 	 * @return
 	 * @throws CorruptIndexException
 	 * @throws IOException
 	 */
 	private static Document addDoc(IndexAccessor ia, String... fields) throws CorruptIndexException, IOException {
 		Document document = new Document();
 		for (String field : fields) {
 			String name = field.replaceAll(":.*", "");
 			String value = field.substring(name.length() + 1);
 			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED,
 					TermVector.WITH_POSITIONS_OFFSETS));
 		}
 		IndexWriter writer = ia.getWriter();
 		writer.addDocument(document);
 		ia.release(writer);
 		return document;
 	}
 }
