 package CSV;
 
 import static org.junit.Assert.*;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class StatisticsTest {
 
 	@Before
 	public void createDummyUser(){
 		try {
 			CSVWriter writer = new CSVWriter(new FileWriter("resources/CSV/users.csv"));
 			writer.writeAll(new ArrayList());
 			writer.close();
 			LoginMenu.addUser("Alice");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testUpdateStats() {
 		User Alice = new User("Alice", 0,0,0,0);
 		
 		try {
 			
 			Statistics.updateStats(Alice, 200, 1000);
 			CSVReader reader = new CSVReader(new FileReader("resources/CSV/users.csv"));
 			// Test Alice's statistics
 	        String[] nextLine = reader.readNext();
 	        assertEquals("Alice", nextLine[0]);	// Username
	        assertEquals("300", nextLine[1]);	// Best score is 200
	        assertEquals("2", nextLine[2]);		// Number of Games played is 1
	        assertEquals("250", nextLine[3]);	// Average score is 200
	        assertEquals("2000", nextLine[4]); 	// Playtime is 1000
 			
 			Statistics.updateStats(Alice, 300, 1000);
 			// Test Alice's statistics
 			reader = new CSVReader(new FileReader("resources/CSV/users.csv"));
 	        nextLine = reader.readNext();
 			assertEquals("Alice", nextLine[0]);	// Username
 	        assertEquals("300", nextLine[1]);	// Best score is now 300
 	        assertEquals("2", nextLine[2]);		// Number of Games played is 2
 	        assertEquals("250", nextLine[3]);	// Average score is (300+200)/2=250
 	        assertEquals("2000", nextLine[4]); 	// Playtime is 1000+1000=2000
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 }
