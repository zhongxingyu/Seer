 package com.feup.contribution.aida.builder;
 
 import java.util.LinkedList; 
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.ConstructorInvocation;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 public class AidaASTVisitor extends ASTVisitor{
 	private LinkedList<String> unitNames = new LinkedList<String>();
 
 	@Override
	public void endVisit(ConstructorInvocation node) {
 		addBinding(node.resolveConstructorBinding().getDeclaringClass());
		super.endVisit(node);
 	}
 
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		for (ITypeBinding binding : node.resolveBinding().getInterfaces()) {
 			addBinding(binding);
 		}
 		addBinding(node.resolveBinding().getSuperclass());
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(MethodInvocation node) {
 		addBinding(node.resolveMethodBinding().getDeclaringClass());
 		return super.visit(node);
 	}
 
 	private void addBinding(ITypeBinding binding) {
 		if (binding == null) return;
 		String cn = binding.getName();
 		String pn = binding.getPackage().getName();
 		unitNames.add(pn+"."+cn);
 	}
 	
 	public LinkedList<String> getUnitNames() {
 		return unitNames;
 	}
 }
