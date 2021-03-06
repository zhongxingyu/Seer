 /**
  * 
  */
 package com.redhat.qe.auto.testopia;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 import java.util.HashMap;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.ISuite;
 import org.testng.ISuiteListener;
 import org.testng.ITestContext;
 import org.testng.ITestResult;
 import org.testng.internal.IResultListener;
 
 import testopia.API.Build;
 import testopia.API.Environment;
 import testopia.API.Product;
 import testopia.API.Session;
 import testopia.API.TestCase;
 import testopia.API.TestCaseRun;
 import testopia.API.TestPlan;
 import testopia.API.TestRun;
 import testopia.API.TestopiaException;
 
 import com.redhat.qe.auto.selenium.LogFormatter;
 
 /**
  * @author jweiss
  *
  */
 public class TestopiaTestNGListener implements IResultListener, ISuiteListener {
 
 	private static String TESTOPIA_PW = "";
 	private static String TESTOPIA_USER = "";
 	private static String TESTOPIA_URL = "";
 	private static String TESTOPIA_TESTRUN_TESTPLAN = "";
 	private static String TESTOPIA_TESTRUN_PRODUCT = "";
 	
 	protected TestProcedureHandler tph = null;
 	protected static Logger log = Logger.getLogger(TestopiaTestNGListener.class.getName());
 	protected TestRun testrun;
 	protected Product product;
 	protected Build build;
 	protected Environment environment;
 	protected TestPlan testplan;
 	protected TestCase testcase;
 	protected Session session;
 	protected TestCaseRun testcaserun = null;
 	static {
 		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
 		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
 		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
 	}
 	
 	@Override
 	public void onFinish(ISuite suite) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onStart(ISuite suite) {
 	
 		
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onFinish(org.testng.ITestContext)
 	 */
 	@Override
 	public void onFinish(ITestContext context) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onStart(org.testng.ITestContext)
 	 */
 	@Override
 	public void onStart(ITestContext context) {
 		//create new test run
 		String testname = context.getName();
 		try {
 			loginTestopia();
 			retrieveContext();
 			testrun = new TestRun(session, 
 					testplan.getId(),
 					environment.getId(), 
 					build.getId(), 
 					session.getUserid(), 
 					testname);
 			testrun.create();
 			
 
 		} catch(Exception e){
 			log.severe("Could not log in to testopia!  Aborting!");
 			TestopiaException te=new TestopiaException("Failed to log in to testopia");
 			te.initCause(e);
 			throw te;
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
 	 */
 	@Override
 	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onTestFailure(org.testng.ITestResult)
 	 */
 	@Override
 	public void onTestFailure(ITestResult result) {
 		//also update the test run
 		markTestRunComplete(result);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onTestSkipped(org.testng.ITestResult)
 	 */
 	@Override
 	public void onTestSkipped(ITestResult result) {
 		markTestRunComplete(result);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onTestStart(org.testng.ITestResult)
 	 */
 	@Override
 	public void onTestStart(ITestResult result) {
 		//create new testcaserun
		int iteration = result.getMethod().getCurrentInvocationCount();
		log.fine("Got getCurrentInvocationCount()=" + iteration  + ", total=" + result.getMethod().getInvocationCount());
		String count = "";
		if (iteration > 0) count = new Integer(iteration+1).toString();
		String alias = result.getTestClass().getName() + "." + result.getMethod().getMethodName() + count;
		String summary = result.getMethod().getMethodName() + count;
 		try {
 			testcase = new TestCase(session, alias);
 			
 		}catch(Exception e){
 			log.log(Level.FINE, "Testcase retrieval failed on '" + summary + "', probably doesn't exist yet.", e);
 			try {
 				log.info("Creating new testcase.");
 				testcase = new TestCase(session, "CONFIRMED", "--default--", "P1",
 						summary, TESTOPIA_TESTRUN_TESTPLAN, TESTOPIA_TESTRUN_PRODUCT);
 				testcase.setAlias(alias);
 				testcase.setIsAutomated(true);
 				testcase.create();
 			}
 			catch(Exception e2){
 				throw new TestopiaException(e2);
 			}
 		}
 		log.fine("Testrun is " + testrun.getId());
 		
 			
 		testcaserun = new TestCaseRun(session,
 							  testrun.getId(),
 							  testcase.getId(),
 							  build.getId(),
 							  environment.getId());
 		
 		testcaserun.setStatus(TestCaseRun.Statuses.RUNNING);
 		try {
 			testcaserun.create();
 			testrun.addCases(testcaserun.getId());
 		}catch(Exception e) {
 			throw new TestopiaException(e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.ITestListener#onTestSuccess(org.testng.ITestResult)
 	 */
 	@Override
 	public void onTestSuccess(ITestResult result) {
 		//get the procedure log from the handler
 		String action = "no procedure found!";
 		Handler[] handlers = Logger.getLogger("").getHandlers();
 		
 		if (tph == null) {
 			//find the right handler (and save for later)
 			for (Handler handler: handlers){
 				if (handler instanceof TestProcedureHandler)
 					tph = ((TestProcedureHandler)handler);
 			}
 		}
 		action = tph.getLog();
 		log.fine("Updating testcase " + testcase.getAlias() + " with successful action log: \n" + action);
 		//put it in testopia
 		testcase.setAction(action);
 		
 		try {
 			testcase.storeText();
 			//FIXME remove the following lines later when all records are updated
 			testcase.setIsAutomated(true);
 			testcase.update();
 		}catch(Exception e){
 			throw new TestopiaException(e);
 		}
 		
 		
 		
 		
 		//also update the test run
 		markTestRunComplete(result);
 	}
 
 	protected void markTestRunComplete(ITestResult result){
 		//reset the handler
 		((TestProcedureHandler)tph).reset();
 		
 		if (result.getStatus() == ITestResult.SKIP) testcaserun.setStatus(TestCaseRun.Statuses.BLOCKED);
 		else {
 			if (!result.isSuccess() && result.getThrowable() != null){				
 				testcaserun.setNotes(throwableToString(result.getThrowable()));				
 			}
 			testcaserun.setStatus(result.isSuccess() ? TestCaseRun.Statuses.PASSED : TestCaseRun.Statuses.FAILED);
 		}
 		try {
 			testcaserun.update();
 		}catch(Exception e){
 			throw new TestopiaException(e);
 		}
 	}
 	
 	protected String throwableToString(Throwable t){
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		t.printStackTrace(pw);
 		return sw.toString();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
 	 */
 	@Override
 	public void onConfigurationFailure(ITestResult result) {
 		//markTestRunComplete(result);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
 	 */
 	@Override
 	public void onConfigurationSkip(ITestResult result) {
 		//markTestRunComplete(result);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
 	 */
 	@Override
 	public void onConfigurationSuccess(ITestResult result) {
 		//markTestRunComplete(result);
 
 	}
 	
 	//FIXME this is just temporary for testing
 	private static void setLogConfig(){
 		Logger.getLogger("").setLevel(Level.ALL);
 		Logger.getLogger("").getHandlers()[0].setFormatter(new LogFormatter());
 		Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
 		log.info("Hello");
 	}
 	
 	protected void loginTestopia() throws XmlRpcException, GeneralSecurityException, IOException{
 		TESTOPIA_URL = System.getProperty("testopia.url");
 		TESTOPIA_USER = System.getProperty("testopia.login");
 		TESTOPIA_PW = System.getProperty("testopia.password");
 		TESTOPIA_TESTRUN_PRODUCT = System.getProperty("testopia.testrun.product");
 		TESTOPIA_TESTRUN_TESTPLAN = System.getProperty("testopia.testrun.testplan");
 		log.fine("Logging in to testopia as " + TESTOPIA_USER);
 		session = new Session(TESTOPIA_USER, TESTOPIA_PW, new URL(TESTOPIA_URL));
 		session.login();
 	}
 	
 	protected void retrieveContext() throws XmlRpcException{
 		product = new Product(session, System.getProperty("testopia.testrun.product"));
 		testplan = new TestPlan(session, System.getProperty("testopia.testrun.testplan"));
 		build = new Build(session, product.getId());
 		Integer buildID = build.getBuildIDByName("2.2 CR1");
 		environment = new Environment(session, product.getId(), null);
 		Integer envId = environment.getEnvironemntIDByName("Windows+Postgres");
 		/*HashMap<String,Object> trinst= (HashMap<String, Object>) tr.create();
 		TestCaseRun tcr = new TestCaseRun(session,
 										  (Integer)trinst.get("run_id"),
 										  2948,
 										  buildID,
 										  envId);
 		tcr.create();*/
 
 	}
 	public static void main(String args[]) throws Exception{
 		setLogConfig();
 		log.finer("Testing log setting.");
 		Session session = new Session(TESTOPIA_USER, TESTOPIA_PW, new URL(TESTOPIA_URL));
 		session.login();
 		/*//tc.makeTestCase(id, 0, 0, true, 271, "This is a test of the testy test", 0);
 		Map<String, Object> values = new HashMap<String, Object>();
 		values.put("summary", "dfdfg");
 		Object[] result = new TestopiaTestCase(session, 0).getList(values);
 		for (Object res: result){
 			System.out.println(res.toString());
 		}
 		TestCaseRun tcr = new TestCaseRun(session, 2935, 1, 1, 1, 1);
 		tcr.makeTestCaseRun(1, 1);
 		tcr.setNotes("RICK ASTLEY");
 		tcr.setStatus(2);
 		tcr.update();*/
 		
 		/*TestCase tc2 = new TestCase(session, "PROPOSED", "--default--", "P1", "what up dude", "Acceptance", "JBoss ON");
 		tc2.setIsAutomated(true);
 		tc2.create();
 		tc2.setPriorityID("P2");
 		tc2.update();
 		tc2.update();*/
 		
 		
 		//TestRun tcr = new TestRun(session, 2948, "2.2 CR1", "Windows + Postgres" );
 		
 		//tcr.create();
 		
 		//TestCaseRun tcr = new TestCaseRun(session, 2935, )
 		Product prod = new Product(session);
 		Integer prodId = prod.getProductIDByName("JBoss ON");
 		TestPlan tp = new TestPlan(session, "Acceptance");
 		Integer plan = tp.getId();
 		Build bu = new Build(session, prodId);
 		Integer build = bu.getBuildIDByName("2.2 CR1");
 		Environment env = new Environment(session, prodId, null);
 		Integer envId = env.getEnvironemntIDByName("Windows+Postgres");
 		TestRun tr = new TestRun(session, plan, envId, build, session.getUserid(), "Test" + System.currentTimeMillis());
 		HashMap<String,Object> trinst= (HashMap<String, Object>) tr.create();
 		TestCaseRun tcr = new TestCaseRun(session,
 										  (Integer)trinst.get("run_id"),
 										  2948,
 										  build,
 										  envId);
 		tcr.create();
 	}
 
 }
