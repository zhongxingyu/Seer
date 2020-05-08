 package tests;
 
 import java.util.ArrayList;
 
 import tests.circle.Circle;
 import tests.circle.CollectionOfCircles;
 import tests.circle.Point;
 import tests.configuration.Configuration;
 import tests.graph.ClassA;
 import tests.graph.ClassB;
 import tests.graph.collections.Container;
 import tests.graph.diamond.ClassD;
import tests.maps.TestingMapsWithinMaps;
 import tests.person.Faculty;
 import tests.person.Person;
 import tests.person.PersonDirectory;
 import tests.person.Student;
 import tests.person.StudentDirectory;
 import tests.rss.Rss;
 import tests.scalar.ScalarCollection;
 
 public class RunTests
 {
 	private ArrayList<TestCase>	testCases	= new ArrayList<TestCase>();
 
 	public RunTests()
 	{
 		// composite
 		testCases.add(new Point());
 		testCases.add(new Circle());
 
 		// collection of composite
 		testCases.add(new CollectionOfCircles());
 
 		// composite inheritence
 		testCases.add(new Person());
 		testCases.add(new Faculty());
 		testCases.add(new Student());
 		testCases.add(new Rss());
 
 		// mono-morphic collection
 		testCases.add(new StudentDirectory());
 
 		// polymorphic collection
 		testCases.add(new PersonDirectory());
 		testCases.add(new Configuration());
 
 		// graph
 		testCases.add(new ClassA());
 		testCases.add(new ClassB());
 		testCases.add(new ClassD());
 		testCases.add(new Container());
 
 		// scalar collection
 		testCases.add(new ScalarCollection());
		
		testCases.add(new TestingMapsWithinMaps());
 	}
 
 	public void runTestCases()
 	{
 		System.out.println("***** Executing " + testCases.size() + " Test Cases ******** ");
 		System.out.println();
 
 		int i = 0;
 		int fail = 0;
 
 		for (TestCase testCase : testCases)
 		{
 			try
 			{
 				System.out
 						.println("--------------------------------------------------------------------------------------------------------------------------------------------------");
 				System.out.println("Test Case " + ++i + " : " + testCase.getClass().getCanonicalName());
 				System.out
 						.println("--------------------------------------------------------------------------------------------------------------------------------------------------");
 				testCase.runTest();
 			}
 			catch (Exception ex)
 			{
 				System.out.println();
 				System.out.println();
 				ex.printStackTrace();
 				System.out.println();
 				fail++;
 			}
 		}
 
 		System.out.println();
 		System.out.println("***** End: " + fail + " of " + i + " tests failed ********");
 	}
 
 	public static void main(String[] args)
 	{
 		RunTests runTests = new RunTests();
 		runTests.runTestCases();
 	}
 }
