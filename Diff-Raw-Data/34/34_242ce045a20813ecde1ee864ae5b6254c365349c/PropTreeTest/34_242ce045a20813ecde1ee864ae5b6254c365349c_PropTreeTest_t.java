 package de.zib.gndms.stuff.propertytree;
 
 
 
 
 
import static org.testng.Assert.assertEquals;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
import java.util.Properties;
import java.util.logging.Logger;

 
 public class PropTreeTest {
 
 	static Logger logger = Logger.getLogger( PropTreeTest.class.getName( ) );
 	
 
 	@DataProvider(name="properties")
 	public Object[][] loadProperties(){
 		
 		Properties tmp =new Properties();
 		tmp.setProperty("A.B.D.E", "a");
 		tmp.setProperty("A.B", "x");
 		tmp.setProperty("B.A.C", "r");
 		tmp.setProperty("B.A", "q");
 		tmp.setProperty("B.D", "u");
 		tmp.setProperty("G", "xa");
 		tmp.setProperty("E.D.B.A", "z");
 
 		return new Object[][]{new Object []{tmp}};
 	}
 	
 
 	
 	
 	@Test( dataProvider="properties")
 	public void runcreatePropertyTest(Properties props){
 		logger.info("Generating PropertyTree from properties and comparing");
 		PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
 		assertEquals(pt.asProperties(false), props);
 	}
 	
 
 	@Test(dataProvider="properties")
 	public void runLabelTest(Properties props){
 		logger.info( "\n>>> Performing Labeltests" );
         PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
         logger.info("Checking setLabel() and getLabel()");
         assertEquals(pt.asProperties(true), props, "expected properties ?");
         assertEquals(pt.getLabel(), null, "label should be null for new tree.. ");
         pt.setLabel("lbl");
         assertEquals(pt.getLabel(), "lbl","setLabel() correct ?");
 	}
 	
 	@Test(dataProvider="properties")
 	public void runValueTest(Properties props){
 		logger.info( "\n>>> Performing Valuetests" );
         PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
         logger.info("Checking setValue() and getValue()");
         assertEquals(pt.asProperties(true), props, "expected properties ?");
         assertEquals(pt.getValue(),null);
         pt.setValue("XX");
         assertEquals(pt.getValue(), "XX");
 	}
 	
 	@Test(dataProvider="properties")
 	public void runsubTreeTest(Properties props){
 		logger.info( "\n>>> Performing SubTreetests" );
         logger.info("Generating PropertyTree from properties");
         PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
         PropertyTree subpt=pt.subTree("B.A");
         logger.info("Checking subTree()");
         Properties result=new Properties();
         result.setProperty("A.C", "r");
         result.setProperty("A", "q");
         assertEquals(subpt.asProperties(false), result, "expected properties ?");
         assertEquals(subpt.getValue(), "q", "expected Rootvalue?");
 	}
 	
 	
 	@Test(dataProvider="properties")
 	public void runPathTest(Properties props){
 		logger.info( "\n>>> Performing Path-tests" );
 		PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
 		logger.info("Checking addPath()");
 		pt.addPath("A.B");
         assertEquals(pt.asProperties(false), props, "Did nothing change as path already existed ?");
         pt.addPath("A.B.J.K");
         pt.setProperty("A.B.J", "g");
         Properties comp=(Properties) props.clone();
         comp.setProperty("A.B.J", "g");
         assertEquals(pt.asProperties(false), comp, "Properly added new Path ?");
 	}
 
 	@Test(dataProvider="properties")
 	public void addTreeTest(Properties props){
 		logger.info( "\n>>> Performing addTree-tests" );
 		PropertyTree pt=PropertyTreeFactory.createPropertyTree(props);
 		 logger.info("Checking addTree()");
 		Properties addition=new Properties();
 		addition.setProperty("Y", "bla");
 		addition.setProperty("Y.Z", "blahoch2");
 		pt.addTree("B.A", PropertyTreeFactory.createPropertyTree(addition));
 		Properties expected=new Properties();
 		expected.setProperty("B.D", "u");
 		expected.setProperty("G", "xa");
 		expected.setProperty("E.D.B.A", "z");
 		expected.setProperty("A.B", "x");
 		expected.setProperty("B.A.Y", "bla");
 		expected.setProperty("B.A.Y.Z", "blahoch2");
 		expected.setProperty("A.B.D.E", "a");
         assertEquals(pt.asProperties(true), expected, "Successfully deleted old subtree and added new one ?");
         
         pt.addTree("Q",PropertyTreeFactory.createPropertyTree(addition));
         expected.setProperty("Q.Y", "bla");
         expected.setProperty("Q.Y.Z", "blahoch2");
         assertEquals(pt.asProperties(true), expected, "Successfully added new subtree ?");
 	}
 }
