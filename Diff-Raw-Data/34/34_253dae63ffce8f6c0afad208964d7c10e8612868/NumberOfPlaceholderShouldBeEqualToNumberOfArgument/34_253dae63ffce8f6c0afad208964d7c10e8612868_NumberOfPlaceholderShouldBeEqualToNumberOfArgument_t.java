 package jp.skypencil.pmd.slf4j;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sourceforge.pmd.AbstractJavaRule;
 import net.sourceforge.pmd.ast.ASTAdditiveExpression;
 import net.sourceforge.pmd.ast.ASTArgumentList;
import net.sourceforge.pmd.ast.ASTArrayDimsAndInits;
 import net.sourceforge.pmd.ast.ASTArrayInitializer;
 import net.sourceforge.pmd.ast.ASTClassOrInterfaceType;
 import net.sourceforge.pmd.ast.ASTExpression;
 import net.sourceforge.pmd.ast.ASTFieldDeclaration;
 import net.sourceforge.pmd.ast.ASTLiteral;
 import net.sourceforge.pmd.ast.ASTName;
 import net.sourceforge.pmd.ast.ASTPrimaryExpression;
 import net.sourceforge.pmd.ast.ASTPrimaryPrefix;
 import net.sourceforge.pmd.ast.ASTPrimarySuffix;
 import net.sourceforge.pmd.ast.ASTType;
 import net.sourceforge.pmd.ast.ASTVariableDeclaratorId;
 import net.sourceforge.pmd.ast.ASTVariableInitializer;
 
 import org.slf4j.Logger;
 
 public class NumberOfPlaceholderShouldBeEqualToNumberOfArgument extends AbstractJavaRule {
 	private Set<String> loggerFields = new HashSet<String>();
 
 	@Override
 	public Object visit(ASTFieldDeclaration node, Object data) {
 		ASTType fieldType = node.getFirstChildOfType(ASTType.class);
 		if (fieldType == null) {
 			return super.visit(node, data);
 		}
 		ASTClassOrInterfaceType fieldClass = fieldType.getFirstChildOfType(ASTClassOrInterfaceType.class);
 		if (fieldClass != null && Logger.class.equals(fieldClass.getType())) {
 			String fieldName = node.getFirstChildOfType(ASTVariableDeclaratorId.class).getNameDeclaration().getImage();
 			loggerFields.add(fieldName);
 		}
 		return super.visit(node, data);
 	}
 
 	@Override
 	public Object visit(ASTPrimaryExpression node, Object data) {
 		ASTPrimaryPrefix prefix = node.getFirstChildOfType(ASTPrimaryPrefix.class);
 		ASTPrimarySuffix suffix = node.getFirstChildOfType(ASTPrimarySuffix.class);
 		if (prefix == null || suffix == null) {
 			return super.visit(node, data);
 		}
 		String expressionName = prefix.getFirstChildOfType(ASTName.class).getImage();
 		ASTArgumentList argumentList = suffix.getFirstChildOfType(ASTArgumentList.class);
 		if (!isLogging(expressionName) || argumentList == null) {
 			return super.visit(node, data);
 		}
 
 		List<ASTExpression> arguments = findExpressions(argumentList);
 		if (arguments.size() > 0) {
 			List<ASTLiteral> literals = arguments.get(0).findChildrenOfType(ASTLiteral.class);
 			if (literals.size() != 1 || arguments.get(0).containsChildOfType(ASTAdditiveExpression.class)) {
 				addViolationWithMessage(data, node, "Sorry but currently this rule handles only string literal as 1st argument.");
 			} else {
 				ASTLiteral literal = literals.get(0);
 				String format = literal.getImage();
 				int expectedArguments = countDelimiter(format);
 				int givenArguments = arguments.size() - 1;	// removing count of message
 				ASTExpression lastArgument = arguments.get(arguments.size() - 1);
				if (!isArray(lastArgument) && isThrowable(lastArgument.getType())) {
 					--givenArguments;
 					lastArgument = arguments.get(arguments.size() - 2);
 				}
 				if (hasArrayInitializer(lastArgument)) {
 					givenArguments = sizeOf(lastArgument);
 				}
 				if (expectedArguments != givenArguments) {
 					addViolation(data, node);
 				}
 			}
 		}
 		return super.visit(node, data);
 	}
 
 	/**
 	 * Iterate all children in ASTArgumentList to get children which is ASTExpression
 	 */
 	private List<ASTExpression> findExpressions(ASTArgumentList argumentList) {
 		// #findChildrenOfType lists all children which is instance of ASTExpression
 		List<ASTExpression> arguments = argumentList.findChildrenOfType(ASTExpression.class);
 
 		// remove non-direct children
 		for (Iterator<ASTExpression> iter = arguments.iterator(); iter.hasNext();) {
 			if (!iter.next().getNthParent(1).equals(argumentList)) {
 				iter.remove();
 			}
 		}
 
 		return arguments;
 	}
 
 	private boolean hasArrayInitializer(ASTExpression lastArgument) {
 		return lastArgument.containsChildOfType(ASTArrayInitializer.class);
 	}
 
 	int sizeOf(ASTExpression array) {
 		ASTArrayInitializer initializer = array.getFirstChildOfType(ASTArrayInitializer.class);
 		return initializer.findChildrenOfType(ASTVariableInitializer.class).size();
 	}
 
	private boolean isArray(ASTExpression lastArgument) {
		return lastArgument.containsChildOfType(ASTArrayDimsAndInits.class);
 	}
 
 	boolean isThrowable(Class<?> clazz) {
 		if (clazz == null) {
			// Given argument is an instance of class which is out of CLASSPATH.
			// We guess it as an instance of original throwable.
			return true;
 		}
 		Class<?> superClass = clazz.getSuperclass();
 		if (superClass == null || superClass == Object.class) {
 			return clazz.equals(Throwable.class);
 		} else {
 			return isThrowable(superClass);
 		}
 	}
 
 	private boolean isLogging(String expressionName) {
 		for (String fieldName : loggerFields) {
 			for (String methodName : new String[]{"trace", "debug", "info", "warn", "error"}) {
 				String loggingMethodName = fieldName + "." + methodName;
 				if (expressionName.equals(loggingMethodName)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	int countDelimiter(String format) {
 		Matcher matcher = Pattern.compile("(.?)(\\\\\\\\)*\\{\\}").matcher(format);
 		int count = 0;
 		while (matcher.find()) {
 			if (!"\\".equals(matcher.group(1))) {
 				++count;
 			}
 		}
 		return count;
 	}
 }
