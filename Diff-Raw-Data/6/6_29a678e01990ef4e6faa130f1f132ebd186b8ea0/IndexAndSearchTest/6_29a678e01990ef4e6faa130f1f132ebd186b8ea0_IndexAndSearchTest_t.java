 package fulltextsearch.hibsearch;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Explanation;
 import org.apache.lucene.util.Version;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.hibernate.Session;
 import org.hibernate.ejb.Ejb3Configuration;
 import org.hibernate.search.FullTextSession;
 import org.hibernate.search.jpa.FullTextEntityManager;
 import org.hibernate.search.jpa.FullTextQuery;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Example testcase for Hibernate Search
  */
 public class IndexAndSearchTest {
 
 	private EntityManagerFactory emf;
 
 	private EntityManager em;
 
 	private static Logger log = LoggerFactory.getLogger( IndexAndSearchTest.class );
 
 	@Before
 	public void setUp() {
 		initHibernate();
 	}
 
 	@After
 	public void tearDown() {
 		purge();
 	}
 
 	@Test
 	public void testIndexAndSearch() throws Exception {
 		List<Book> books = search( "hibernate" );
 		assertEquals( "Should get empty list since nothing is indexed yet", 0, books.size() );
 
 		index();
 
 		// search by title
 		books = search( "hibernate" );
 		assertEquals( "Should find one book", 1, books.size() );
 		assertEquals( "Wrong title", "Java Persistence with Hibernate", books.get( 0 ).getTitle() );
 
 		// search author
 		books = search( "\"Gavin King\"" );
 		assertEquals( "Should find one book", 1, books.size() );
 		assertEquals( "Wrong title", "Java Persistence with Hibernate", books.get( 0 ).getTitle() );
 	}
 
 	@Test
 	public void testStemming() throws Exception {
 
 		index();
 
 		List<Book> books = search( "refactor" );
 		assertEquals( "Wrong title", "Refactoring: Improving the Design of Existing Code", books.get( 0 ).getTitle() );
 
 		books = search( "refactors" );
 		assertEquals( "Wrong title", "Refactoring: Improving the Design of Existing Code", books.get( 0 ).getTitle() );
 
 		books = search( "refactored" );
 		assertEquals( "Wrong title", "Refactoring: Improving the Design of Existing Code", books.get( 0 ).getTitle() );
 
 		books = search( "refactoring" );
 		assertEquals( "Wrong title", "Refactoring: Improving the Design of Existing Code", books.get( 0 ).getTitle() );
 	}
 	
 	@Test
 	public void testCaseSensitiveSearch() throws Exception {
 	    index();
 	    
 	    List<Book> books = search( "Refactoring", "customanalyzer2" );
 	    assertEquals( "Should find one book", 1, books.size() );
 	    assertEquals( "Wrong title", "Refactoring: Improving the Design of Existing Code", books.get( 0 ).getTitle() );
 	    
         books = search( "refactoring", "customanalyzer2" );
 	    assertEquals( "Should find no books", 0, books.size() );
 	    
        books = search( "King" );
        assertEquals( "Should find four books", 4, books.size() );
        
 	    books = search( "King", "customanalyzer2" );
 	    assertEquals( "Should find no books since author is not indexed for customanalyzer2", 0, books.size() );
 	    
         books = search( "\"Refactoring: Improving the Design of Existing Code\"", "customanalyzer2" );
         assertEquals( "Should find one book with exact match", 1, books.size() );	 
         
         books = search( "\"Refactoring: Improving the design of Existing Code\"", "customanalyzer2" );
         assertEquals( "Should find no books (design is lowercased)", 0, books.size() );              
 	}
 	
 	@Test
 	public void testWildcardSearch() throws Exception {
 	    index();
 	    List<Book> books = search( "Cl*r", "customanalyzer2" );
 	    assertEquals( "Should find one book", 
 	                  "Clear Your Clutter with Feng Shui: Space Clearing Can Change Your Life", books.get( 0 ).getTitle() );
 	    
 	    books = search( "s*sky" );
 	    assertEquals( "Should find Spolsky's book", 
 	                  "Smart and Gets Things Done: Joel Spolsky's Concise Guide to Finding the Best Technical Talent, " +
 	                          "Hardback: Joel Spolsky's Concise Guide to Finding the Best Technical Talent", 
 	                  books.get( 0 ).getTitle() );
 	}
 	
 	@Test
 	public void testPhraseSearch() throws Exception {
 	    index();
 	    List<Book> books = search("\"in Action\"");
 	    assertEquals( "Should find two books", 2, books.size() );
 	    
	    books = search("\"Action in\"");
         assertEquals( "Should find no books", 0, books.size() );
 	}
 	
 	@Test
 	public void testBooleanSearch() throws Exception {
 	    index();
 	    
 	    List<Book> books = search( "+and -\"java\"", "customanalyzer2" );	    
 	    int withoutJava = books.size();   
 	    
 	    books = search( "+and +java", "customanalyzer2" );
 	    int withJava = books.size();   
 	    
         books = search( "+and java", "customanalyzer2" );        
         int total = books.size();  	    
         
         assertEquals( "subtotals should sum to total", withoutJava + withJava, total );
 	}
 
 	private void initHibernate() {
 		Ejb3Configuration config = new Ejb3Configuration();
 		config.configure( "hibernate-search-example", new HashMap<Object, Object>() );
 		emf = config.buildEntityManagerFactory();
 		em = emf.createEntityManager();
 	}
 
 	private void index() {
 		FullTextSession ftSession = org.hibernate.search.Search.getFullTextSession( ( Session ) em.getDelegate() );
 		try {
 			ftSession.createIndexer().startAndWait();
 		}
 		catch ( InterruptedException e ) {
 			log.error( "Was interrupted during indexing", e );
 		}
 	}
 
 	private void purge() {
 		FullTextSession ftSession = org.hibernate.search.Search.getFullTextSession( ( Session ) em.getDelegate() );
 		ftSession.purgeAll( Book.class );
 		ftSession.flushToIndexes();
 	}
 
 	private List<Book> search(String searchQuery) throws ParseException {
 	    return search(searchQuery, "customanalyzer");
 	}
 	
 	private List<Book> search(String searchQuery, String analyzer) throws ParseException {
 		Query query = searchQuery( searchQuery, analyzer );
 
 		List<Object []> results = query.getResultList();
 		List<Book> books = new ArrayList<Book>( results.size() );
 
 		log.info( "Found: " + results.size() );
 
 		for ( Object[] r : results ) {
 		    Book b = (Book) r[0];		    
 			log.info( "Title: " + b.getTitle() + " Score: " + r[1] );			
 			books.add( b );
 		}
 		
 		return books;
 	}
 
 	private Query searchQuery(String searchQuery, String analyzer) throws ParseException {
 
 		String[] bookFields = { "title", "subtitle", "authors.name", "publicationDate", "title_sort" };
 
 		//lucene part
 		Map<String, Float> boostPerField = new HashMap<String, Float>( 4 );
 		boostPerField.put( bookFields[0], ( float ) 4 );
 		boostPerField.put( bookFields[1], ( float ) 3 );
 		boostPerField.put( bookFields[2], ( float ) 4 );
 		boostPerField.put( bookFields[3], ( float ) .5 );
 
 		FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager( ( EntityManager ) em );
 		Analyzer customAnalyzer = ftEm.getSearchFactory().getAnalyzer( analyzer );
 		QueryParser parser = new MultiFieldQueryParser(
 				Version.LUCENE_31, bookFields,
 				customAnalyzer, boostPerField
 		);
 
 		org.apache.lucene.search.Query luceneQuery;
 		luceneQuery = parser.parse( searchQuery );
 
 		final FullTextQuery query = ftEm.createFullTextQuery( luceneQuery, Book.class )
 		        .setProjection(FullTextQuery.THIS, FullTextQuery.SCORE);
 		
 		return query;
 	}
 
 }
