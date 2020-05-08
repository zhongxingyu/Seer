 package mdettlaff.mobilemachine.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import mdettlaff.mobilemachine.domain.SimplifiedWebpage;
 import mdettlaff.mobilemachine.repository.WebpageRepository;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PageSimplifierServiceTest {
 
 	private static final String URL = "http://tvtropes.org/pmwiki/pmwiki.php/VideoGame/DungeonKeeper";
 	private static final int MAX_PAGE_SIZE_IN_BYTES = 20000;
 
 	private PageSimplifierService service;
 
 	private WebpageRepository repository;
 	private HttpService httpService;
 
 	@Before
 	public void setUp() throws IOException {
 		repository = mock(WebpageRepository.class);
 		httpService = mock(HttpService.class);
 		service = new PageSimplifierService(repository, httpService, MAX_PAGE_SIZE_IN_BYTES);
 	}
 
 	@Test
 	public void testCreateSimplifiedWebpage() throws Exception {
 		// Prepare
 		String webpageContent = IOUtils.toString(getClass().getResourceAsStream("DungeonKeeper.html"));
 		// Mock
 		when(httpService.download(URL)).thenReturn(webpageContent);
 		// Run
 		SimplifiedWebpage result = service.createSimplifiedWebpage(URL);
 		// Verify
 		assertEquals("Dungeon Keeper - Television Tropes &amp; Idioms  ", result.getTitle());
 		assertEquals(3, result.getPageCount());
		String expected1 = IOUtils.toString(getClass().getResourceAsStream("expected1.html"), "UTF-8");
 		assertEquals(expected1, result.getPage(1));
		String expected2 = IOUtils.toString(getClass().getResourceAsStream("expected2.html"), "UTF-8");
 		assertEquals(expected2, result.getPage(2));
		String expected3 = IOUtils.toString(getClass().getResourceAsStream("expected3.html"), "UTF-8");
 		assertEquals(expected3, result.getPage(3));
 	}
 }
