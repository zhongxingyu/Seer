 package net.sf.testium.executor;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestResult;
 import org.testtoolinterfaces.testresult.TestResult.VERDICT;
 import org.testtoolinterfaces.testresult.TestStepIterationResult;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testresult.TestStepResultBase;
 import org.testtoolinterfaces.testresult.TestStepResultList;
 import org.testtoolinterfaces.testresult.TestStepSelectionResult;
 import org.testtoolinterfaces.testresult.impl.TestStepIterationResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestStepResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestStepSelectionResultImpl;
 import org.testtoolinterfaces.testsuite.TestStep;
 import org.testtoolinterfaces.testsuite.TestStepCommand;
 import org.testtoolinterfaces.testsuite.TestStepIteration;
 import org.testtoolinterfaces.testsuite.TestStepScript;
 import org.testtoolinterfaces.testsuite.TestStepSelection;
 import org.testtoolinterfaces.testsuite.TestStepSequence;
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.Warning;
 
 
 public class TestStepMetaExecutor
 {
 	private SupportedInterfaceList mySutInterfaces;
 	private Hashtable<String, TestStepScriptExecutor> myScriptExecutors;
 	private TestStepSetExecutor mySetExecutor;
 
 	public TestStepMetaExecutor()
 	{
 		Trace.println( Trace.CONSTRUCTOR );
 
 		mySutInterfaces = new SupportedInterfaceList();
 		myScriptExecutors = new Hashtable<String, TestStepScriptExecutor>();
 		mySetExecutor = new TestStepSetExecutor(this);
 	}
 
 	public TestStepResultBase execute(TestStep aStep, File aScriptDir, File aLogDir, RunTimeData aRTData)
 	{
 		if ( aStep instanceof TestStepScript )
 		{
 			return executeScript( (TestStepScript) aStep, aScriptDir, aLogDir);
 		}//else
 
 		if ( aStep instanceof TestStepCommand )
 		{
 			return executeCommand( (TestStepCommand) aStep, aRTData, aLogDir);
 		}//else
 
 		if ( aStep instanceof TestStepSelection ) {
 			return executeSelection( (TestStepSelection) aStep, aScriptDir, aLogDir, aRTData);
 		}//else
 
 		if ( aStep instanceof TestStepIteration ) {
 			return executeIteration( (TestStepIteration) aStep, aScriptDir, aLogDir, aRTData);
 		}//else
 		
 		throw new Error( "Don't know how to execute " + aStep.getClass().getSimpleName() );
 	}
 
 	/**
 	 * @param aStepScript
 	 * @param aScriptDir
 	 * @param aLogDir
 	 * @return
 	 */
 	private TestStepResult executeScript( TestStepScript aStepScript,
 	                                      File aScriptDir,
 	                                      File aLogDir )
 	{
 		TestStepResult result;
 
 		if ( myScriptExecutors.containsKey( aStepScript.getScriptType() ) )
 		{
 			TestStepScriptExecutor executor = myScriptExecutors.get( aStepScript.getScriptType() );
 			result = executor.execute(aStepScript, aScriptDir, aLogDir);
 		}
 		else
 		{
 			String message = "Cannot execute step scripts of type '" + aStepScript.getScriptType() + "'\n"
 			+ "Trying to continue, but this may affect further execution...";
 
 			result = reportError(aStepScript, message);
 		}
 		return result;
 	}
 
 	/**
 	 * @param aStep
 	 * @param aLogDir
 	 * @return
 	 */
 	private TestStepResult executeCommand( TestStepCommand aStepCommand,
 	                                       RunTimeData aRtData,
 	                                       File aLogDir )
 	{
 		TestStepResult result;
 		String command = aStepCommand.getCommand();
 
 		String errorMsg = "Cannot execute steps with command '" + aStepCommand.getCommand() + "'\n";
 
 		SutInterface iface = (SutInterface) aStepCommand.getInterface();
 		if ( iface == null || ! iface.hasCommand(command) )
 		{
 			result = reportError(aStepCommand, errorMsg);
 		}
 		else
 		{
 			TestStepCommandExecutor executor = iface.getCommandExecutor(command);
 			try
 			{
 				result = executor.execute(aStepCommand, aRtData, aLogDir);
 			}
 			catch (TestSuiteException tse)
 			{
 				String message = errorMsg + tse.getMessage();
 				result = reportError(aStepCommand, message);
 			}
 		}
 
 		return result;
 	}
 	
 	private TestStepResult executeSelection( TestStepSelection selectionStep, File aScriptDir, File aLogDir, RunTimeData aRTData ) {
 		TestStepSelectionResult result = new TestStepSelectionResultImpl(selectionStep);
 
 		TestStep ifStep = selectionStep.getIfStep();
 		boolean negator = selectionStep.getNegator();
 		TestStepResult ifResult = (TestStepResult) this.execute(ifStep, aScriptDir, aLogDir, aRTData);
 
 		result.setIfStepResult(ifResult);
 		
 		TestStepResultList subStepResults = new TestStepResultList();
 		if ( ifResult.getResult().equals(VERDICT.ERROR)
 			 || ifResult.getResult().equals(VERDICT.UNKNOWN) ) {
 			 // NOP, we don't execute the then or else!
 			
 		} else {
 			String comment = "If-step evaluated to " + ifResult.getResult();
 			if ( ifResult.getResult().equals( negator ? VERDICT.FAILED : VERDICT.PASSED) ) {
 				comment += ". Then-steps executed.";
 				TestStepSequence thenSteps = selectionStep.getThenSteps();
 				this.mySetExecutor.execute(thenSteps, subStepResults, aScriptDir, aLogDir, aRTData);
 
 			} else {
 				TestStepSequence elseSteps = selectionStep.getElseSteps();
 				if ( elseSteps.isEmpty() ) {
 					comment += ". Nothing executed.";
 				} else {					
 					comment += ". Else-steps executed.";
 				}
 				comment += ". Else-steps executed.";
 				this.mySetExecutor.execute(elseSteps, subStepResults, aScriptDir, aLogDir, aRTData);
 			}
 
 			result.setComment(comment);
 		}
 
 //		result.addSubStep( ifResult );
 
 		Iterator<TestStepResultBase> subResultItr = subStepResults.iterator();
 		while ( subResultItr.hasNext() ) {
 			result.addSubStep( subResultItr.next() );
 		}
 		
 		return result;
 	}
 
 	private TestStepIterationResult executeIteration(TestStepIteration aStep,
 			File aScriptDir, File aLogDir, RunTimeData aRTData) {
 		String listName = aStep.getListName();
 		String listElement = aStep.getItemName();
 		@SuppressWarnings("unchecked")
 		ArrayList<Object> list = aRTData.getValueAs(ArrayList.class, listName);
 
 		TestStepSequence doSteps = new TestStepSequence( aStep.getSequence() );
 		TestStep untilStep = aStep.getUntilStep();
 
 		aStep.setDisplayName("Foreach " + listElement + " in " + listName);
 //		TestStepResult stepResult = TestStepResultImpl.createResult(aStep);
 		TestStepIterationResult stepIterationResult = new TestStepIterationResultImpl(aStep);
 
		if ( list == null ) {
			stepIterationResult.addComment("List " + listName + " is not set." );
			return stepIterationResult;
		}
 
 		Iterator<Object> listItr = list.iterator();
 		while (listItr.hasNext() ) {
 			Object element = listItr.next();
 			stepIterationResult.addIterationValue(element);
 
 			RunTimeVariable rtVariable = new RunTimeVariable( listElement, element );
 
 			RunTimeData subRtData = new RunTimeData( aRTData );
 			subRtData.add(rtVariable);
 
 			TestStepResultList stepResultSet = new TestStepResultList();
 //			TestStepCommand iterationStep = new TestStepCommand( foreachSeqNr,
 //					"do " + listElement + " = " + element.toString(),
 //					"do " + listElement, this.mySutInterfaces.getInterface( DefaultInterface.NAME ), 
 //					new ParameterArrayList() );
 //			iterationStep.setDisplayName("do");
 //			TestStepResult iterationStepResult = TestStepResultImpl.createResult(iterationStep);
 
 // TODO result, (scriptDir,) & logDir
 			this.mySetExecutor.execute(doSteps, stepResultSet , aScriptDir, aLogDir, subRtData);
 			stepIterationResult.addExecResult(stepResultSet);
 			
 //			Iterator<TestStepResult> resultSetItr = stepResultSet.iterator();
 //			while (resultSetItr.hasNext() ) {
 //				iterationStepResult.addSubStep(resultSetItr.next());
 //			}
 //			
 //			stepIterationResult.addSubStep(iterationStepResult);
 			
 			if ( untilStep != null ) {
 				TestStepResult myUntilResult = (TestStepResult) this.execute(untilStep, aScriptDir, aLogDir, subRtData);
 				if (myUntilResult.getResult() == VERDICT.PASSED) {
 					stepIterationResult.addUntilResult(myUntilResult);
 					break;
 				}
 			}
 		}
 
 		return stepIterationResult;
 	}
 
 	public void addSutInterface(SutInterface aSutInterface)
 	{
 		mySutInterfaces.add(aSutInterface);
 	}
 
 	public void addScriptExecutor(TestStepScriptExecutor aTestStepExecutor)
 	{
 		myScriptExecutors.put(aTestStepExecutor.getType(), aTestStepExecutor);		
 	}
 	
 	public SupportedInterfaceList getInterfaces()
 	{
 		return mySutInterfaces;
 	}
 
 	/**
 	 * @param step
 	 * @param message
 	 * @return
 	 */
 	private TestStepResult reportError(TestStep aStep, String message)
 	{
 		TestStepResult result = TestStepResultImpl.createResult( aStep );
 		result.setResult(TestResult.ERROR);
 		result.addComment(message);
 
 		Warning.println(message);
 		Trace.println(Trace.ALL, "Cannot execute " + aStep.toString());
 		return result;
 	}
 }
