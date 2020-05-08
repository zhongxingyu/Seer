 package counter.saveresults;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import counter.filestatistics.*;
 
 public class SaveResults_c extends SaveResults{
 	public int numStruct = 0;
 	public int numGoto = 0;
 	public int numLabel = 0;
 
 	@Override
 	public String getDiffTotalStatisticsInfo() {
 		StringBuilder total = new StringBuilder();
 		total.append("Struct: " + numStruct);
 		total.append("\n");
 		total.append("Do: " + numDo);
 		total.append("\n");
 		total.append("Goto: " + numGoto);
 		total.append("\n");
 		total.append("Label: " + numLabel);
 		total.append("\n");
 		total.append("\n");
 		total.append("-------------------------");
 		total.append("\n");
 		total.append("\n");
 		total.append("\n");
 		return total.toString();
 	}
 
 	@Override
 	public String getDiffFileStatisticsInfo(
 			FileStatistics fileStatistics) {
 
 		FileStatistics_c fs = (FileStatistics_c) fileStatistics;
 		StringBuilder sBuilder = new StringBuilder();
 		sBuilder.append("Struct: " + fs.numStruct);
 		sBuilder.append("\n");
 		sBuilder.append("Goto: " + fs.numGoto);
 		sBuilder.append("\n");
 		sBuilder.append("Label: " + fs.numLabel);
 		sBuilder.append("\n");
 		sBuilder.append("\n");
 		return sBuilder.toString();
 	}
 
 	public void getDiffTotalStatistics(FileStatistics fileStatistics){
 		FileStatistics_c fs = (FileStatistics_c) fileStatistics;
 		numStruct += fs.numStruct;
 		numGoto += fs.numGoto;
 		numLabel += fs.numLabel;
 	}
 
 	public int getLocalFunctionCallNumber(List<String> functionList,
 			List<String> functionCallList, List<String> classList, 
 			List<String> callerList) {
 		int numLocalCall = 0;
 		boolean isLocal = false;
 		Set<String> methodSet = new HashSet<String>();
 		Iterator<String> it_method = functionList.iterator();
 		while(it_method.hasNext()){
 			methodSet.add(it_method.next());
 		}
 		Iterator<String> it_call = functionCallList.iterator();
 		String callname;
 		while(it_call.hasNext()){
 			callname = it_call.next();
 			if (methodSet.contains(callname)){
 				numLocalCall++;
 				isLocal = true;
 			}
 			if(callname.startsWith("get")||callname.startsWith("set")
 					||callname.startsWith("_get")||callname.startsWith("_set")
 					||callname.startsWith("Get")||callname.startsWith("Set")
 					||callname.startsWith("_Get")||callname.startsWith("_Set")){
 				if(isLocal){
 					numLocalGetterSetterCall++;
 				}
 				else{
 					numLibGetterSetterCall++;
 				}
 			}
 		}
 		return numLocalCall;
 	}
 }
