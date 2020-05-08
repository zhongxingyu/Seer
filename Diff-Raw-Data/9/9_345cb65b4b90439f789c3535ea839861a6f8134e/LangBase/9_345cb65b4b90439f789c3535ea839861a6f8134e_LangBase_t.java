 // LangBase is a language-dependent code used in GreenTea.java
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public abstract class LangBase {
 
 	public static void println(String msg) {
 		System.out.println(msg);		
 	}
 
 	public static void DebugP(String msg) {
 		LangBase.println("DEBUG" + LangBase.GetStackInfo(2) + ": " + msg);
 	}
 
 	public static String GetStackInfo(int depth){
 		String LineNumber = " ";
 		try{
 			throw new Exception();
 		}
 		catch(Exception e){
			StackTraceElement[] Elements = e.getStackTrace();
			if(depth < Elements.length){
				StackTraceElement elem = Elements[depth];
 				LineNumber += elem;
 			}
 		}
 		return LineNumber;
 	}
 	
 	public final static boolean IsWhitespace(char ch) {
 		return Character.isWhitespace(ch);
 	}
 	
 	public final static boolean IsLetter(char ch) {
 		return Character.isLetter(ch);
 	}
 	
 	public final static boolean IsDigit(char ch) {
 		return Character.isDigit(ch);
 	}
 
 	public final static Method LookupMethod(Object Callee, String MethodName) {
 		if(MethodName != null) {
 			// DebugP("looking up method : " + Callee.getClass().getSimpleName() + "." + MethodName);
 			Method[] methods = Callee.getClass().getMethods();
 			for(int i = 0; i < methods.length; i++) {
 				if(MethodName.equals(methods[i].getName())) {
 					return methods[i];
 				}
 			}
 			DebugP("method not found: " + Callee.getClass().getSimpleName() + "." + MethodName);
 		}
 		return null; /*throw new GtParserException("method not found: " + callee.getClass().getName() + "." + methodName);*/
 	}
 
 	public final static int ApplyTokenFunc(Object Self, Method Method, Object TokenContext, String Text, int pos) {
 		try {
 			Integer n = (Integer)Method.invoke(Self, TokenContext, Text, pos);
 			return n.intValue();
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public final static Object ApplyMatchFunc(Object Self, Method Method, Object Pattern, Object LeftTree, Object TokenContext) {
 		try {
			return Method.invoke(Self, Pattern, LeftTree, TokenContext);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public final static Object ApplyTypeFunc(Object Self, Method Method, Object Gamma, Object ParsedTree, Object TypeInfo) {
 		try {
 			return Method.invoke(Self, Gamma, ParsedTree, TypeInfo);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
 
