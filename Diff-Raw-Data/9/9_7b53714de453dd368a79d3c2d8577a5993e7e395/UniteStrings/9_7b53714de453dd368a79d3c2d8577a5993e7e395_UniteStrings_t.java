 package examples;
 
 import types.Executer;
 import types.FlowData;
 import types.MultipleExecuter;
 
 public class UniteStrings implements MultipleExecuter{
 
 	@Override
 	public String getLabel() {
 		return "Unite strings";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
 		return new Class[]{String.class, String.class};
 	}
 
 	@Override
 	public FlowData execute(FlowData[] data) {
		StringBuilder bld = new StringBuilder();
		for (FlowData curr : data){
			if (curr != null){
				bld.append((String)curr.getData());
			}
		}
		return new FlowData(bld.toString());
 	}
 
 	@Override
 	public Class getOutputType() {
 		return String.class;
 	}
 }
