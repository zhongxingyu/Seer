 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.Parameter;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 import org.testtoolinterfaces.testsuite.ParameterVariable;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 
 
 public class SetList extends GenericCommandExecutor
 {
 	private static final String COMMAND = "setList";
 	private static final String PAR_VALUE = "value";
 
 	private static final SpecifiedParameter PARSPEC_NAME = new SpecifiedParameter (
	        "name", String.class, false, true, false, false );
 	private static final SpecifiedParameter PARSPEC_VALUE = new SpecifiedParameter (
			PAR_VALUE, String.class, false, true, false, false );
 
 	public SetList(SutInterface anInterface)
 	{
 		super(COMMAND, anInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec(PARSPEC_NAME);
 		this.addParamSpec(PARSPEC_VALUE);
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception
 	{
 		String listName = (String) this.obtainValue(aVariables, parameters, PARSPEC_NAME);
 
 		ArrayList<Object> list = new ArrayList<Object>();
 		Iterator<Parameter> paramItr = parameters.iterator();
 		
 		while ( paramItr.hasNext() ) {
 			Parameter par = paramItr.next();
 			if ( par.getName().equals(PAR_VALUE) && par instanceof ParameterImpl ) {
 				list.add( ((ParameterImpl) par).getValue() );
 			} else if ( par instanceof ParameterVariable ) {
 				String valName = ((ParameterVariable) par).getVariableName();
 				Object value = aVariables.getValue(valName);
 
 				// TODO what if value is null
 				list.add(value);
 			}
 		}
 
 		RunTimeVariable rtVariable = new RunTimeVariable( listName, list );
 		aVariables.add(rtVariable);
 	}
 }
