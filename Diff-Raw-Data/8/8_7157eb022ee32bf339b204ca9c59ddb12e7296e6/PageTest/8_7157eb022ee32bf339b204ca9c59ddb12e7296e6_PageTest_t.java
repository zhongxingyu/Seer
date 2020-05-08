 package cz.tsl.velocity;
 
 import cz.tsl.XmlDiff;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import static junit.framework.Assert.assertTrue;
 
 
 /**
  * User: richard.sery
  * Date: 31.3.13
  * Time: 13:42
  */
 @RunWith(JUnit4.class)
 public class PageTest extends AbstractVmTest {
 
     @Test
     public void pageTest() throws Exception {
		XmlDiff diff = runTest("pageTest/page");
 		assertTrue(diff.result() + " VM output: " + diff.getTestedXml(), diff.passed());
     }
 
 	@Test
 	public void pageWithHeadAndBodyTest() throws Exception {
		XmlDiff diff = runTest("pageTest/headAndBody");
 		assertTrue(diff.result() + " VM output: " + diff.getTestedXml(), diff.passed());
 	}
 
 	@Test
 	public void pageMultipartActionTest() throws Exception {
		XmlDiff diff = runTest("pageTest/pageMultipartAction");
 		assertTrue(diff.result() + " VM output: " + diff.getTestedXml(), diff.passed());
 	}
 }
