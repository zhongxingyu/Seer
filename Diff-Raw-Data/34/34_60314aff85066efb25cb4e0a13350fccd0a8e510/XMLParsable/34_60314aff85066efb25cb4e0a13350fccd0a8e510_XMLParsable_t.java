 package requests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class XMLParsable {
 
 	private List<Tuple> variables;
 	
 	public void addVariable(String variableName, Object variable) {
 		if(variables == null) {
 			this.variables = new ArrayList<Tuple>();
 		}
 		variables.add(new Tuple(variableName, variable));
 	}
 	
 	public abstract String getClassName();
 	
 	public int getNumberOfVariables() {
 		return variables.size();
 	}
 	
 	public String getVariableName(int i) {
 		return variables.get(i).getVariableName();
 	}
 	
 	public Object getVariable(int i) {
 		return variables.get(i).getVariable();
 	}
 	
	public void removeVariable(int i) {
		variables.remove(i);
	}
	
 	public String toXml() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<xml><" + getClassName() + ">");
 		for(int i = 0; i < getNumberOfVariables(); i++) {
 			if(getVariableName(i).equals("accepted")) {
 				System.out.println("accepted = " + getVariable(i));
 			}
 			sb.append("<" + getVariableName(i) + ">" + getVariable(i) + "</" + getVariableName(i) + ">");
 		}
 		sb.append("</" + getClassName() + "></xml>");
 		return sb.toString();
 	}
 	
 	public static Object toObject(String xml) {
 		// Remove surrounding xml-tags
 		xml = splitOuter(xml).s2;
 		StringPair temp = splitOuter(xml);
 		String className = temp.s1;
 		xml = temp.s2;
 		List<Tuple> variables = splitInner(xml);
 		Map<String, String> vars = new HashMap<String, String>();
 		for (Tuple t : variables) {
 			vars.put(t.getVariableName(), (String)t.getVariable());
 		}
 		return assembleObject(className, vars);
 		
 //		System.out.println("Class name: " + className);
 //		System.out.println("Variables:");
 //		for (Tuple t : variables) {
 //			System.out.println(t.getVariableName() + " : " + t.getVariable());
 //		}
 //		return null;
 	}
 	
 	private static Object assembleObject(String className, Map<String, String> vars) {
 		Object o = null;
 		switch(className) {
 		case "LogInRequest":
 			o = new LogInRequest(vars.get("username"), vars.get("password"), vars.get("accepted").equals("true") ? true : false);
 			break;
 		}
 		return o;
 	}
 	
 	private static StringPair splitOuter(String xml) {
 		int temp = xml.indexOf('>');
 		String attName = xml.substring(1, temp);
 		int temp2 = xml.indexOf("</" + attName + ">");
 		String inner = xml.substring(temp + 1, temp2);
 		return new StringPair(attName, inner);
 	}
 	
 	private static List<Tuple> splitInner(String xml) {
 		List<Tuple> variables = new ArrayList<Tuple>();
 		int temp;
 		while((temp = xml.indexOf('>')) != -1) {
 			String attName = xml.substring(1, temp);
 			int temp2 = xml.indexOf("</" + attName + ">");
 			String inner = xml.substring(temp + 1, temp2);
 			xml = xml.substring(temp2 + attName.length() + 3);
 			variables.add(new Tuple(attName, inner));
 		}
 		return variables;
 	}
 	
 	static class StringPair {
 		public String s1, s2;
 		
 		public StringPair(String s1, String s2) {
 			this.s1 = s1;
 			this.s2 = s2;
 		}
 	}
 }
