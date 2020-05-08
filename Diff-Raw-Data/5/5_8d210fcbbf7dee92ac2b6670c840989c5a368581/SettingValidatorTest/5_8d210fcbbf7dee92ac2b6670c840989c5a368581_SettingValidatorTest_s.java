 package app;
 
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.hasItems;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import config.DefaultAppSettings;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.CoreMatchers.*;
 
 @RunWith(Parameterized.class)
 public class SettingValidatorTest {
 	Properties appSettings;
 	
 	String pageThreads, imageThreads, writeBlocked, BaseUrl, subpages;
 	boolean expectedResponse;
 	
 	@Parameters 
 	public static Collection<Object[]> params() {
 		
 	return Arrays.asList(new Object[][] {
 			 {true,	"1", 	"1",	"false",	"http://foo.bar",	"a;15,b;14"}, // 0
 			 {false,"1", 	"1",	"false",	"http://foo.bar",	"5;15,6;14"}, // 1
 			 {false,"0", 	"1",	"false",	"http://foo.bar",	"a;15,b;14"}, // 2
 			 {false,"-1", 	"1",	"false",	"http://foo.bar",	"a;15,b;14"}, // 3
 			 {false,"1", 	"0",	"false",	"http://foo.bar",	"a;15,b;14"}, // 4
 			 {false,"1", 	"-1",	"false",	"http://foo.bar",	"a;15,b;14"}, // 5
 			 {false,"1", 	"1",	"true",		"http://foo.bar",	"a;15,b;14"}, // 6
 			 {false,"1", 	"1",	"fdfg",		"http://foo.bar",	"a;15,b;14"}, // 7
 			 {false,"1", 	"1",	"3455",		"http://foo.bar",	"a;15,b;14"}, // 8
 			 {false,"1", 	"1",	"false",	"http:/foo.bar",	"a;15,b;14"}, // 9
 			 {false,"1", 	"1",	"false",	"foo.bar",			"a;15,b;14"}, // 10
 			 {false,"1", 	"1",	"false",	"http://foobar",	"a;15,b;14"}, // 11
 			 {false,"1", 	"1",	"false",	"http://foo.bar",	"a;15b;14"},  // 12
			 {false,"1", 	"1",	"false",	"http://foo.bar",	"a;15"},	  // 13
 			 {false,"1", 	"1",	"false",	"http://foo.bar",	"a,15,b,14"}, // 14
 			 {false,"1", 	"1",	"false",	"http://foo.bar",	"a;15;b;14"}, // 15
 			 {false,"-4", 	"&",	"**",		"http//foo.bar//",	"5;1gfdh14"}, // 16
			 {false,"2", 	"2",	"false",	"http://foo.bar",	"a;15,b;14"}, // 17
 		});
 	}
 	
 	public SettingValidatorTest(boolean expectedResponse, String pageThreads, String imageThreads, String writeBlocked, String baseUrl, String subpages) {
 		this.expectedResponse = expectedResponse;
 		this.pageThreads = pageThreads;
 		this.imageThreads = imageThreads;
 		this.writeBlocked = writeBlocked;
 		BaseUrl = baseUrl;
 		this.subpages = subpages;
 	}
 
 
 
 	@Before
 	public void setup() throws Exception{
 		appSettings = makeAppSettings(pageThreads, imageThreads, writeBlocked, BaseUrl, subpages);
 	}
 	
 	@Test
 	public void testValidateAppSettings() {
 		assertThat(SettingValidator.validateAppSettings(appSettings), is(expectedResponse));
 	}
 	
 	/**
 	 * Construct a new property Object from parameters
 	 * @param pageThreads
 	 * @param imageThreads
 	 * @param writeBlocked
 	 * @param BaseUrl
 	 * @param subpages
 	 * @return
 	 */
 	private Properties makeAppSettings(String pageThreads, String imageThreads, String writeBlocked, String BaseUrl, String subpages){
 		Properties setting = new Properties();
 		
 		setting.setProperty("page_threads",pageThreads);
 		setting.setProperty("image_threads",imageThreads);
 		setting.setProperty("write_Blocked",writeBlocked);
 		setting.setProperty("base_url",BaseUrl);
 		setting.setProperty("sub_pages",subpages);
 		
 		return setting;
 	}
 }
