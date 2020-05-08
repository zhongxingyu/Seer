 package jp.skypencil.pmd.slf4j;
 
 import java.util.Deque;
 import java.util.LinkedList;
 
 import net.sourceforge.pmd.AbstractJavaRule;
 import net.sourceforge.pmd.ast.ASTClassOrInterfaceDeclaration;
 import net.sourceforge.pmd.ast.ASTClassOrInterfaceType;
 import net.sourceforge.pmd.ast.ASTFieldDeclaration;
 import net.sourceforge.pmd.ast.ASTVariableInitializer;
 
 public final class DontPassOtherClassToLoggerFactory extends AbstractJavaRule {
	private Deque<Class<?>> stack = new LinkedList<Class<?>>();
 
 	@Override
 	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		stack.push(node.getType());
 
 		try {
 			return super.visit(node, data);
 		} finally {
 			stack.pop();
 		}
 	}
 
 	@Override
 	public Object visit(ASTFieldDeclaration node, Object data) {
 		ASTClassOrInterfaceType field = node.getFirstChildOfType(ASTClassOrInterfaceType.class);
 		if (field != null && field.getType().equals(org.slf4j.Logger.class)) {
 			ASTVariableInitializer initializer = node.getFirstChildOfType(ASTVariableInitializer.class);
 			ASTClassOrInterfaceType givenClassToFactory = initializer.getFirstChildOfType(ASTClassOrInterfaceType.class);
			if (givenClassToFactory != null && !givenClassToFactory.getType().equals(stack.peek())) {
 				addViolation(data, node);
 			}
 		}
 
 		return super.visit(node, data);
 	}
 }
