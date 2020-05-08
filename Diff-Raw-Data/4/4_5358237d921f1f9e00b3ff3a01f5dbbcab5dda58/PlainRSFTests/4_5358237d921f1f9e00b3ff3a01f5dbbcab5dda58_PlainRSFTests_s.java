 /*
  * Created on 22 Jun 2007
  */
 package uk.org.ponder.rsf.bare.junit;
 
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsac.test.AbstractRSACTests;
 import uk.org.ponder.rsf.bare.ActionResponse;
 import uk.org.ponder.rsf.bare.RenderResponse;
 import uk.org.ponder.rsf.bare.RequestLauncher;
 import uk.org.ponder.rsf.bare.RequestResponse;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /** A base class for a "full-cycle" testing environment for an RSF application. It exposes
  * the standard JUnit environment, as well as various utility methods for launching
  * RSF (RSAC) requests and decoding and asserting the state of the results.
  * 
  * </br>
  * Applications would typically derive test cases from either this or {@link MultipleRSFTests},
  * in the constructor make calls to {@link #contributeConfigLocation(String)} 
  * and {@link #contributeRequestConfigLocation(String)}, and then write test fixture methods
  * as per normal JUnit semantics.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class PlainRSFTests extends AbstractRSACTests {
 
   public PlainRSFTests() {
     contributeConfigLocations(new String[] {
         "classpath:conf/rsf-config.xml", 
         "classpath:conf/blank-applicationContext.xml",
         "classpath:conf/test-applicationContext.xml"
     });
     
     contributeRequestConfigLocations(
         new String[] {
             "classpath:conf/rsf-requestscope-config.xml",
             "classpath:conf/blank-requestContext.xml",
             "classpath:conf/test-requestContext.xml"} 
     );
   }
   
   protected RequestLauncher requestLauncher;
   
   private RequestLauncher allocateRequestLauncher() {
    getRSACBeanLocator().startRequest();
     WriteableBeanLocator wbl = getRSACBeanLocator().getDeadBeanLocator();
     RequestLauncher togo = new RequestLauncher(applicationContext, getRSACBeanLocator(), isSingleShot());
     wbl.set("earlyRequestParser", togo);
     return togo;
   }
   
   public RequestLauncher getRequestLauncher() {
     if (isSingleShot()) {
       return requestLauncher;
     }
     else {
       return allocateRequestLauncher();
     }
   }
 
   /** Assert whether the action cycle in question has completed without error
    * @param response The action cycle to be tested
    * @param error <code>true</code> if it is to be asserted there was an error,
    * or <code>false</code> if it is to be asserted there was none.
    */
   protected void assertActionError(ActionResponse response, boolean error) {
     assertTrue("Request expected " + (error? "with":"without") + " error",
         (((ViewParameters) response.ARIResult.resultingView).errortoken == null) ^ error);
   }
 
   /** Assert that the markup rendered from the render cycle in question contains the
    * supplied text.
    * @param response The render cycle which has concluded
    * @param expected The string which is expected to be found in the markup.
    */
   protected void assertContains(RenderResponse response, String expected) {
     int index = response.markup.indexOf(expected);
     assertTrue("Expected text " + expected + " not found", index != -1);
   }
   
   
   protected TargettedMessageList fetchMessages(RequestResponse response) {
     return (TargettedMessageList) response.requestContext.locateBean("targettedMessageList");
   }
   
   protected void onSetUp() throws Exception {
     super.onSetUp();
     if (isSingleShot()) {
       requestLauncher = allocateRequestLauncher();
     }
   }
   
 }
