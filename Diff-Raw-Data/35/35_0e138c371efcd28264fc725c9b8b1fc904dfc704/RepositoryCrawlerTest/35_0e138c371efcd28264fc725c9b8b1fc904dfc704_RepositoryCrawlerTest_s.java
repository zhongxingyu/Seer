 package edu.illinois.gitsvn.infra;
 
import java.util.List;
 
import org.gitective.core.filter.commit.CommitCountFilter;
 import org.gitective.tests.GitTestCase;
 import org.junit.Before;
 import org.junit.Test;
 
import edu.illinois.gitsvn.infra.filters.AnalysisFilter;

 public class RepositoryCrawlerTest extends GitTestCase{
 	
 	private RepositoryCrawler crawler;
 
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
		add("file1.txt", "file1");
		add("file2.txt", "file2");
		
 		crawler = new RepositoryCrawler();
 	}
 	
 	@Test
	public void testOneFilter() throws Exception {
		
	}
	
	@Test
	public void testTwoFilters() throws Exception {
 		
	}
	
	@Test
	public void testNoFilters() throws Exception {
 		
 	}

 }
