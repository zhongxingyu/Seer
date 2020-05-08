 package edu.uiuc.immutability;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.Assignment;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldAccess;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.internal.corext.dom.Bindings;
 
 public class ClassMutatorAnalysis extends ASTVisitor {
 	private final IType targetClass;
 	
 	Map<MethodDeclaration, MethodSummary> mutators;
 	
 	public ClassMutatorAnalysis(IType targetClass) {
 		this.targetClass = targetClass;
		
		mutators = new HashMap<MethodDeclaration, MethodSummary>();
 	}
 	
 	public boolean hasMutators() {
 		return !mutators.isEmpty();
 	}
 
 	@Override
 	public boolean visit(MethodDeclaration methodDecl) {
 		
 		final MethodSummary methodSummary = new MethodSummary(targetClass);
 		methodDecl.accept(methodSummary);
 		
 		if (methodSummary.hasFieldAssignments()) {
 			mutators.put(methodDecl, methodSummary);
 		}
 		
 		return false;
 	}
 }
 
 
 /* AST Visitors */
 
 /**
  * Summarizes the field writes in a method 
  */
 class MethodSummary extends ASTVisitor {
 	private final IType targetClass;
 	private List<SimpleName> fieldsAssignedTo;
 
 	public MethodSummary(IType targetClass) {
 		this.targetClass = targetClass;
 		fieldsAssignedTo = new ArrayList<SimpleName>();
 	}
 
 	public boolean hasFieldAssignments() {
 		return !fieldsAssignedTo.isEmpty();
 	}
 
 	public boolean visit(Assignment assignment) {
 		SimpleName fieldName = null;
 
 		Expression leftHandSide = assignment.getLeftHandSide();
 		if (leftHandSide instanceof SimpleName ) {
 			SimpleName possibleField = (SimpleName) leftHandSide;
 			IJavaElement possibleFieldParent = possibleField.resolveBinding().getJavaElement().getParent();
 			
 			if ( possibleFieldParent.getHandleIdentifier().equals(targetClass.getHandleIdentifier()) ) {
 				fieldName = possibleField;
 			}
 		} else if (leftHandSide instanceof FieldAccess) {
 			FieldAccess fieldAccess = (FieldAccess) leftHandSide;
 			fieldName = fieldAccess.getName();
 		}
 		
 		if (fieldName != null) {
 			fieldsAssignedTo.add(fieldName);
 		}
 		
 		// True because there may be a chain of assignments: i = j = 0;
 		return true;
 	}
 }
