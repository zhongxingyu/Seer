 package unibz.it.edu;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import unibz.it.edu.rdfElements.RDFGraph;
 import unibz.it.edu.rdfElements.RDFTriplet;
 import unibz.it.edu.rdfElements.RDFUri;
 
 public class TestRDFExpand {
 	
 	private static RDFGraphData test_data;
 	private static final String rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 	
 	
 	@BeforeClass
 	public static void setup() {
 		test_data = new RDFGraphData();	
 	}
 	
 	@Test
 	public void test_implicitProperties() {
 		
 		RDFGraph right_data = new RDFGraph(test_data.basic1);
 		RDFExpander exp = new RDFExpander();
 		exp.expand(right_data);
 		RDFTriplet _prop = new RDFTriplet(
 				new RDFUri("http://www.example.org/publishedBy"),
				new RDFUri("type", rdfns),
				new RDFUri("Property", rdfns));
 		
		assertEquals(true, right_data.getTriplets().contains(_prop));
 		
 	}
 
 }
