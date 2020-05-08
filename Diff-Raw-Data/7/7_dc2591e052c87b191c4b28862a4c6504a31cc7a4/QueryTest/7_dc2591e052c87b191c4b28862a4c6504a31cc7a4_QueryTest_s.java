 package test.engine;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import static internal.Helper.write;
 
 import internal.parser.ParseException;
 import internal.parser.Parser;
 import internal.parser.containers.query.IQuery;
 import internal.parser.resolve.ResolutionEngine;
 import internal.parser.resolve.Result;
 import internal.piece.PieceFactory;
 import internal.tree.IWorldTree.IMap;
 import internal.tree.WorldTreeFactory;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class QueryTest {
 	private IMap map;
 	private WorldTreeFactory factory = null;
 	private Parser parser			 = new Parser(new StringReader(""));
 	private String queryDirPath		 = "src/test/engine/query tests/";
 	private static String[] pieceStrings = {
 		"LR",
 		"UD",
 		"UL",
 		"L",
 		"U",
 		"D",
 		"R",
 		"UDLR",
 		"UDL",
 		"UDR",
 		"ULR",
 		"DLR",
 		"UR",
 		"DR",
 		"DL",
 		""
 	};
 	
 	@BeforeClass
 	public static void setUp() {
 		try {
 			PieceFactory.initialize(pieceStrings);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	@Test
 	public void inbuiltPropertiesTest() {
 //		Direction tests
 		factory = new WorldTreeFactory("src/test/engine/query tests/inbuilt properties/init.properties");
 		map = factory.newMap("InbuiltPropertiesTestMap", null, null);
 		
 		String string = "A toeast B";
 		parser.ReInit(new StringReader(string));
 		try {
 			IQuery query = parser.query();
 			System.out.println(ResolutionEngine.evaluate(map, query));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void createNewQueryTest() {
 		factory = new WorldTreeFactory();
 		Properties properties = factory.properties();
 		System.out.println("Query :");
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 			StringBuffer cmd = new StringBuffer();
 			while(!cmd.toString().contains(";")) {
 				cmd.append(in.readLine());
 				cmd.append("\n");
 			}
 			System.out.println("\nComments (End with ';') :");
 			
 //			Comments for this test
 			StringBuffer comments = new StringBuffer("#");
 			while(!comments.toString().contains(";")) {
 				comments.append(in.readLine());
 				comments.append("\n#");
 			}
 			
 //			Get new test directory
 			String testDir 	= getNewTest();
 			File testFile	= new File(testDir + "/test");
 
 //			Store the properties for this test
 			File propertiesFile = new File(testDir + "/init.properties");
 			properties.store(new FileOutputStream(propertiesFile), null);
 			
 //			Get the results to test for
 			map = factory.newMap(testFile.getParentFile().getName(), null, null);
 			map.fullInit();
 			write(map);
 			
 			parser.ReInit(new StringReader(cmd.toString()));
 			IQuery query 	= parser.query();
 			Result result 	= ResolutionEngine.evaluate(map, query);
 			
 //			Now store this test
 			BufferedWriter out = new BufferedWriter(new FileWriter(testFile));
 			out.write(comments.toString());
 			out.newLine();
 			out.write(query.toString());
 			out.newLine();
 			out.write(result.toString());
 			out.newLine();
 			
 			out.close();
 			
 			System.out.println("Test " + testFile.getParentFile().getName() + " created!");
 		} catch(IOException e) {
 			System.err.println(e.getMessage());
 		} catch (ParseException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 
 	private String getNewTest() {
 		File returnTestDir = null;
 		
 		File dir = new File(queryDirPath);
 		
 		String testPrefix 	= "test";
 		int validTestID 		= 0;
 
 //		Find a testID to use
 		List<String> tests	= Arrays.asList(dir.list());
		while(validTestID > 0) {
 			if(!tests.contains(testPrefix + String.format("%03d", validTestID)))
 				break;
 			validTestID++;
 		}
 		
 		returnTestDir = new File(queryDirPath + testPrefix + String.format("%03d", validTestID));
 		if(returnTestDir.exists())
 			throw new IllegalStateException("Error in getNewFile code! Should have picked a directory that does not exist!");
 		returnTestDir.mkdir();
 		return returnTestDir.getAbsolutePath();
 	}
 }
