 package soot.jimple.infoflow.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import soot.RefType;
 import soot.SootMethod;
 import soot.Type;
 import soot.VoidType;
 import soot.jimple.infoflow.data.SootMethodAndClass;
 
 public class SootMethodRepresentationParser {
 
 	/**
 	 * parses a string in soot representation, for example:
 	 * <soot.jimple.infoflow.test.TestNoMain: java.lang.String function1()>
 	 * <soot.jimple.infoflow.test.TestNoMain: void functionCallOnObject()>
 	 * <soot.jimple.infoflow.test.TestNoMain: java.lang.String function2(java.lang.String,java.lang.String)>
 	 *TODO: was ist mit Static?
 	 * @param parseString
 	 */
 	public SootMethodAndClass parseSootMethodString(String parseString){
 		if(!parseString.startsWith("<") || !parseString.endsWith(">")){
 			throw new IllegalArgumentException("Illegal format of " +parseString +" (should use soot method representation)");
 		}
 		String name = "";
 		String className = "";
 		Type returnType = VoidType.v();
 		Pattern pattern = Pattern.compile("<(.*?):");
         Matcher matcher = pattern.matcher(parseString);
         if(matcher.find()){
         	className = matcher.group(1);
         }
         pattern = Pattern.compile(": (.*?) ");
         matcher = pattern.matcher(parseString);
         if(matcher.find()){
         	String retType =  matcher.group(1);
         	if(retType.equals("void"))
         		returnType = VoidType.v();
         	else
         		returnType = RefType.v(retType);
         	//remove the string contents that are already found so easier regex is possible
         	parseString = parseString.substring(matcher.end(1));
         	
         }
         pattern = Pattern.compile(" (.*?)\\(");
         matcher = pattern.matcher(parseString);
         if(matcher.find()){
         	name = matcher.group(1);
         }
         List<Type> paramList = new ArrayList<Type>();
         pattern = Pattern.compile("\\((.*?)\\)");
         matcher = pattern.matcher(parseString);
         if(matcher.find()){
         	String params = matcher.group(1);
        	
         	while(params.contains(",")){
        		int index = params.indexOf(',');
        		paramList.add(RefType.v(params.substring(0, index)));
        		params = params.substring(index + 1);
        		
         	}
         	if(!params.equals("")){
         		paramList.add(RefType.v(params));
         	}
         }
         SootMethod method = new SootMethod(name, paramList, returnType);
        return new SootMethodAndClass(method, className);
        
 	}
 	
 	public HashMap<String, List<SootMethod>> parseMethodList(List<String> methods){
 		HashMap<String, List<SootMethod>> result = new HashMap<String, List<SootMethod>>();
 		for(String method : methods){
 			SootMethodAndClass smc = parseSootMethodString(method);
 			if(result.containsKey(smc.getClassString())){
 				result.get(smc.getClassString()).add(smc.getSootMethod());
 			} else{
 				List<SootMethod> methodList = new ArrayList<SootMethod>();
 				methodList.add(smc.getSootMethod());
 				result.put(smc.getClassString(), methodList);
 			}
 		}
 		
 		return result;
 	}
 	
 }
