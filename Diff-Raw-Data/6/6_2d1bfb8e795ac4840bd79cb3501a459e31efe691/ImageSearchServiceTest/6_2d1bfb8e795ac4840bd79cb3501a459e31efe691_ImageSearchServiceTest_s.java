 /**
  * 
  */
 package com.dart.archive.image.search.site;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.dart.archive.image.search.Candidate;
 import com.dart.archive.image.search.color.NaiveColorImageSearcher;
 import com.dart.archive.image.search.surf.InterestPointsSearcher;
 
 /**
  * @author Massimiliano.Gerardi
  *
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ImageSearchServiceTest {
 	
 	@InjectMocks
 	private ImageSearchService imageSearchService;
 	
 	@Mock
 	private NaiveColorImageSearcher colorImageSearcher;
 	
 	@Mock 
 	private InterestPointsSearcher interestPointsSearcher;
 
 	@Mock
 	private Candidate candidate1;
 	
 	@Mock
 	private Candidate candidate2;
 	
 	@Mock
 	private Candidate candidate3;
 	
 	Collection<Candidate> ipcandidates = new ArrayList<Candidate>();
 	Collection<Candidate> naivecandidates = new ArrayList<Candidate>();
 	
 	@Before
 	public void setUp() {
 		imageSearchService.images = "/src/test/resources/images/service";
 		given(interestPointsSearcher.search(any(File.class))).willReturn(ipcandidates);
 		given(colorImageSearcher.search(any(File.class))).willReturn(naivecandidates);
 		given(candidate1.getImage()).willReturn(new File("/src/test/resources/images/service/image1.jpg"));
 		given(candidate2.getImage()).willReturn(new File("/src/test/resources/images/service/image2.jpg"));
 		given(candidate3.getImage()).willReturn(new File("/src/test/resources/images/service/image3.jpg"));
 	}
 	
 	/**
 	 * test method for {@link ImageSearchService#search(java.io.File)}
 	 */
 	@Test
 	public void testSearchInterestPoints() {
 		ipcandidates.add(candidate1);
 		ImageSearchResults results = imageSearchService.search(new File("/src/test/resources/image.jpg"));
 		assertNotNull(results);
 		assertThat(results.getContent().size(), is(1));
 		String url = results.getContent().get(0);
		assertThat(url, is("/src/test/resources/images/service/image1.jpg"));
 	}
 
 	/**
 	 * test method for {@link ImageSearchService#search(java.io.File)}
 	 */
 	@Test
 	public void testSearchColor() {
 		ipcandidates.clear();
 		naivecandidates.add(candidate1);
 		naivecandidates.add(candidate2);
 		naivecandidates.add(candidate3);
 		ImageSearchResults results = imageSearchService.search(new File("/src/test/resources/image.jpg"));
 		assertNotNull(results);
 		assertThat(results.getContent().size(), is(3));
 		String url = results.getContent().get(0);
		String expectedPath = new File("/src/test/resources/images/service/image1.jpg").getAbsolutePath();
 		assertThat(url, is(expectedPath));
 	}
 
 }
