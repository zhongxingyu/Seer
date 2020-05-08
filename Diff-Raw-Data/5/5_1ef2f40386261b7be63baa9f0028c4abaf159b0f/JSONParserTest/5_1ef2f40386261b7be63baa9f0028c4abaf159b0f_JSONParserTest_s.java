 package ch.eonum.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.eonum.JSONParser;
 import ch.eonum.Logger;
 import ch.eonum.MedicalLocation;
 import ch.eonum.Mode;
 
 /* Unit Test */
 public class JSONParserTest
 {
 	private static Mode previousMode;
 
 	@BeforeClass
 	public static void setUpBeforeClass()
 	{
 		previousMode = Logger.mode;
 		Logger.mode = Mode.TEST;
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass()
 	{
 		Logger.mode = previousMode;
 	}
 
 	@Test
 	public final void testDeserializeHansMuster()
 	{
 		String content = "{ \"status\": \"OK\",\"results\": [ { \"name\": \"Hans Muster\","
			+ "\"address\": \"Bahnhofstrasse, 3000 Bern\", \"email\": \"\", "
 			+ "\"types\": [ \"allgemeinaerzte\" ], \"location\": { \"lat\": 46.12345, \"lng\": 7.54321 } } ] } ";
 
 		JSONParser parser = new JSONParser();
 		MedicalLocation[] locations = parser.deserializeLocations(content);
 
 		assertNotNull(locations);
 		MedicalLocation testLocation = locations[0];
 
 		assertNotNull(testLocation);
 		assertEquals("Hans Muster", testLocation.getName());
 		assertEquals("Bahnhofstrasse, 3000 Bern", testLocation.getAddress());
 		assertEquals("", testLocation.getEmail());
 	}
 
 	@Test
 	public final void testDeserializeErnstHasli()
 	{
 		String content = "{ \"status\": \"OK\",\"results\": [ { \"name\": \"Ernst Hasli\","
			+ "\"address\": \"Kleines Gässli 5, 1234 Beispielsdorf\", \"email\": \"ernst_58@hotmail.com\", "
 			+ "\"types\": [ \"allergologen\" ], \"location\": { \"lat\": 45.1, \"lng\": 7.61 } } ] } ";
 
 		JSONParser parser = new JSONParser();
 		MedicalLocation[] locations = parser.deserializeLocations(content);
 
 		assertNotNull(locations);
 		MedicalLocation testLocation = locations[0];
 
 		assertNotNull(testLocation);
 		assertEquals("Ernst Hasli", testLocation.getName());
 		assertEquals("Kleines Gässli 5, 1234 Beispielsdorf", testLocation.getAddress());
 		assertEquals("ernst_58@hotmail.com", testLocation.getEmail());
 	}
 }
