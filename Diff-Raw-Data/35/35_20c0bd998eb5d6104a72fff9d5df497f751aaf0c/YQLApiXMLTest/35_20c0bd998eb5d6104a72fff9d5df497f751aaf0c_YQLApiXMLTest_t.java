 package de.hpi.fgis.yql;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 
import org.junit.After;
import org.junit.Before;
 import org.junit.Test;
 
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 public class YQLApiXMLTest {
	YQLApiXML api;
	
	@Before
	public void setUp() throws Exception {
		api = new YQLApiXML();
	}
	@After
	public void shutDown() throws Exception {
		api.close();
		api = null;
	}
	
 	@Test
 	public void testParse() {
 		String xml = "<guid isPermaLink=\"false\">USCA1116_2013_06_17_7_00_PDT</guid>";
 		String expectedJson = "{\"guid\":{\"isPermaLink\":\"false\",\"content\":\"USCA1116_2013_06_17_7_00_PDT\"}}";
 		
 		assertEquals(JSON.parse(expectedJson), api.parse(new ByteArrayInputStream(xml.getBytes())));
 		
 
 		xml = "<add job=\"351\">\n" +
 		      "    <tag>foobar</tag>\n" +
 		      "    <tag>foobar2</tag>\n" +
 		      "</add>";
 		expectedJson = "{\"add\":{\"job\":\"351\",\"tag\":[\"foobar\",\"foobar2\"]}}";
 		
 		assertEquals(JSON.parse(expectedJson), api.parse(new ByteArrayInputStream(xml.getBytes())));
 	}
 
 	@Test
 	public void testFormat() {
 		assertEquals("xml", api.format());
 	}
 	
 
 	@Test
 	public void testQuery() throws IOException {
 		String expectedJson = "{\"place\":{\"xmlns\":\"http://where.yahooapis.com/v1/schema.rng\",\"name\":\"Berlin\"}}";
 		DBObject results = api.query("select name from geo.places where woeid = '638242'");
 		assertEquals(JSON.parse(expectedJson), results);
 	}
 	@Test
 	public void testQueryMeta() throws IOException {
 		DBObject meta = api.queryMeta("select name from geo.places where woeid = '638242'", new HashMap<String, String>());
 		assertEquals("en-US", ((DBObject)meta.get("query")).get("yahoo:lang"));
 	}
 }
