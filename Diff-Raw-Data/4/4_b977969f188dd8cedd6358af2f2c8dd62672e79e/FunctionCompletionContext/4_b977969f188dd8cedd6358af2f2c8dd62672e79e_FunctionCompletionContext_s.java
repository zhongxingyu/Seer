 package org.velzno.codeassist;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.php.internal.core.codeassist.CodeAssistUtils;
 import org.eclipse.php.internal.core.codeassist.contexts.ClassMemberContext;
 
 /**
  * R/C 関数のコード補完を行うかどうか判定する
  * 
  * FunctionGoalEvaluatorFactory::createEvaluator が実行される前に，
  * 必ず実行されるので，ここでクラス名を確保しておく
  * @author yabeken
  */
 public class FunctionCompletionContext extends ClassMemberContext{
 	private static String className = "";
 	private static String functionName = "";
 	public boolean isValid(ISourceModule sourceModule, int offset, CompletionRequestor requestor) {
 		clearNames();
 		try {
 			String source = sourceModule.getSource().substring(0, offset);
 			source = source.substring(0, source.lastIndexOf("->"));
 			//R(Hoge) R("org.rhaco.Hoge")
 			Matcher m1 = Pattern.compile("(R|C)\\s*\\(\\s*((?:[\"\'])?)(?:[a-zA-Z][a-zA-Z0-9-_]*\\.)*([a-zA-Z][a-zA-Z0-9_]*?)\\s*\\2\\)\\s*$").matcher(source);
 			//R(new Hoge($abc)) R(new Hoge("abc"))
 			Matcher m2 = Pattern.compile("(R)\\s*\\(\\s*new\\s+([a-zA-Z][a-zA-Z0-9_]*?)(?:\\s*\\(.*?\\))?\\s*\\)\\s*$").matcher(source);
 			//R($hoge)
 			Matcher m3 = Pattern.compile("(R|C)\\s*\\(\\s*(\\$[a-zA-Z][a-zA-Z0-9_]*?)\\s*\\)\\s*$").matcher(source);
 			if(m1.find()){
 				className = m1.group(3);
 				functionName = m1.group(1);
 			}else if(m2.find()){
 				className = m2.group(2);
 				functionName = m2.group(1);
 			}else if(m3.find()){
 				//TODO
 				functionName = m3.group(1);
				IType type = CodeAssistUtils.getVariableType(sourceModule, "$a", getOffset())[0];
 				className = type.getElementName().toString();
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		if(super.isValid(sourceModule, offset, requestor) && getTriggerType() == Trigger.OBJECT){
 			return true;
 		}
 		return false;
 	}
 	public static String getClassName(){
 		return className;
 	}
 	public static String getFunctionName(){
 		return functionName;
 	}
 	public static void clearNames(){
 		className = "";
 		functionName = "";
 	}
 }
