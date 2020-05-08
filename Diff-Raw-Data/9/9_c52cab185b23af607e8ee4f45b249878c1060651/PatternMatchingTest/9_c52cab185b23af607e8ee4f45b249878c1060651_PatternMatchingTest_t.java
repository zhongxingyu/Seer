 package layr.engine.expressions;
 
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class PatternMatchingTest {
 
 	URLPattern urlPattern = new URLPattern();
 
 	@Test
 	public void assertThatReplacesTheUrl() {
 		String pattern = "users/{id}/edit";
 		String actual = urlPattern.parseMethodUrlPatternToRegExp( pattern );
		Assert.assertEquals( "users/([^/]+)/edit", actual );
 	}
 
 	@Test
 	public void assertThatNotReplacesTheUrl() {
 		String pattern = "users/987654/edit";
 		String actual = urlPattern.parseMethodUrlPatternToRegExp( pattern );
 		Assert.assertEquals( pattern, actual );
 	}
 
 	@Test
 	public void assertExtractCorrectPlaceHoldersFromUrlAgainsAPattern() {
 		String url = "users/123/edit/Miere";
 		String pattern = "users/{id}/edit/{name}";
 		Map<String, String> values = urlPattern.extractMethodPlaceHoldersValueFromURL( pattern, url );
 		Assert.assertEquals( 2, values.size() );
 		Assert.assertEquals( "123", values.get( "id" ) );
 		Assert.assertEquals( "Miere", values.get( "name" ) );
 	}
 }
