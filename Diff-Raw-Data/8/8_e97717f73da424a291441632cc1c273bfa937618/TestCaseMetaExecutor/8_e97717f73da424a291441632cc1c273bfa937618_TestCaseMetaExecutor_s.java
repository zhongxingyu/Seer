 package net.sf.testium.executor;
 
 import java.io.File;
 import java.util.Hashtable;
 
 import org.testtoolinterfaces.testresult.TestCaseResultLink;
 import org.testtoolinterfaces.testresult.TestResult.VERDICT;
 import org.testtoolinterfaces.testresult.impl.TestCaseResultLinkImpl;
 import org.testtoolinterfaces.testsuite.TestCaseLink;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.Warning;
 
 
 public class TestCaseMetaExecutor
 {
 	private Hashtable<String, TestCaseExecutor> myExecutors;
 
 	public TestCaseMetaExecutor()
 	{
 		Trace.println( Trace.CONSTRUCTOR );
 
 		myExecutors = new Hashtable<String, TestCaseExecutor>();
 	}
 
 	public TestCaseResultLink execute(TestCaseLink aTestCaseLink, File aLogDir, RunTimeData aRTData)
 	{
 		Trace.println(Trace.EXEC, "execute( " 
 						+ aTestCaseLink.getId() + ", "
 			            + aLogDir.getPath() + ", "
 			            + aRTData.size() + " Variables )", true );
 
 		TestCaseResultLink result;
 		if ( myExecutors.containsKey( aTestCaseLink.getLinkType() ) )
 		{
 			TestCaseExecutor executor = myExecutors.get( aTestCaseLink.getLinkType() );
 			try
 			{
 				result = executor.execute(aTestCaseLink, aLogDir, aRTData);
 			}
			catch (TestCaseLinkExecutionException e)
 			{
				Trace.print(Trace.EXEC_PLUS, e);
 				result = new TestCaseResultLinkImpl( aTestCaseLink,
 				                                 VERDICT.ERROR,
 				                                 null );
				result.addComment(e.getMessage());
 			}
 		}
 		else
 		{
 			result = new TestCaseResultLinkImpl( aTestCaseLink,
 			                                 VERDICT.ERROR,
 			                                 null );
 
 			String message = "Cannot execute test case scripts of type " + aTestCaseLink.getLinkType() + "\n";
 			result.addComment(message);
 			Warning.println(message);
 			Trace.print(Trace.EXEC_PLUS, "Cannot execute " + aTestCaseLink.toString());
 		}
 		
 		return result;
 	}
 
 	public void put(String type, TestCaseExecutor aTestCaseExecutor)
 	{
 		myExecutors.put(type, aTestCaseExecutor);
 	}
 }
