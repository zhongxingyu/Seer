 package soot.jimple.infoflow.test.junit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import soot.jimple.infoflow.Infoflow;
 import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
 
 public class MultiTest extends JUnitTests {
 
 	private static final String SOURCE_STRING_PWD = "<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>";
 
     @Test
     public void multiTest1(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void multiSourceCode()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
     }
 
     @Test
     public void multiTest2(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void multiSourceCode2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
     }
 
     @Test
     public void ifPathTest1(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void ifPathTestCode1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void ifPathTest2(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void ifPathTestCode2()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void ifPathTest3(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void ifPathTestCode3()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void ifPathTest4(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void ifPathTestCode4()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void loopPathTest1(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void loopPathTestCode1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sink, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void hashTest1(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void hashTestCode1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sinkInt, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
     }
 
     @Test
     public void shiftTest1(){
     	Infoflow infoflow = initInfoflow();
     	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.MultiTestCode: void shiftTestCode1()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 
 		Assert.assertTrue(infoflow.getResults().isPathBetweenMethods(sinkInt, SOURCE_STRING_PWD));
 		Assert.assertEquals(1, infoflow.getResults().size());
 
 
     }
 
 }
