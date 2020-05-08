 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.extractsuperclass;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInnerToTopRefactoring;
 import org.eclipse.jdt.internal.ui.JavaPluginImages;
 import org.eclipse.jdt.ui.text.java.IInvocationContext;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
 import org.eclipse.jdt.ui.text.java.IProblemLocation;
 import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
 import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
 import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
 import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.TextFileChange;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.text.edits.InsertEdit;
 
 
 @SuppressWarnings("restriction")
 public class CompositeRefactoringsQuickAssistProcessor implements IQuickAssistProcessor {
 
 	@Override
 	public boolean hasAssists(IInvocationContext context) throws CoreException {
 		ASTNode coveringNode= context.getCoveringNode();
 		if (coveringNode != null) {
 			return getCreateNewSuperclassProposal(context, coveringNode, false, null) ||
 					getMoveToImmediateSuperclassProposal(context, coveringNode, false, null) ||
 					getMoveTypeToNewFileProposal(context, coveringNode, false, null);
 		}
 		return false;
 	}
 
 	@Override
 	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
 		ASTNode coveringNode= context.getCoveringNode();
 		if (coveringNode != null) {
 			ArrayList<ICommandAccess> resultingCollections= new ArrayList<ICommandAccess>();
 			getCreateNewSuperclassProposal(context, coveringNode, false, resultingCollections);
 			getMoveToImmediateSuperclassProposal(context, coveringNode, false, resultingCollections);
 			getMoveTypeToNewFileProposal(context, coveringNode, false, resultingCollections);
 			return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
 		}
 		return null;
 	}
 
 	private static boolean getCreateNewSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals) throws CoreException {
 		if (!(coveringNode instanceof TypeDeclaration)) {
 			return false;
 		}
 
 		if (proposals == null) {
 			return true;
 		}
 
 		final ICompilationUnit cu= context.getCompilationUnit();
 		ASTNode coveringTypeDeclarationASTNode= context.getCoveringNode();
 		ASTNode node= coveringTypeDeclarationASTNode.getParent();
 		AST ast= node.getAST();
 		ASTRewrite rewrite= ASTRewrite.create(ast);
 		String label= "Create new superclass in file";
 		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
 		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
 		TypeDeclaration newSuperclass= ast.newTypeDeclaration();
 		newSuperclass.setName(ast.newSimpleName("NewSuperclass"));
 		ListRewrite listRewrite= rewrite.getListRewrite(node, CompilationUnit.TYPES_PROPERTY);
 		listRewrite.insertLast(newSuperclass, null);
 		rewrite.set(newSuperclass, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, coveringTypeDeclarationASTNode.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY), null);
 		rewrite.set(coveringTypeDeclarationASTNode, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass.getName(), null);
 		proposals.add(proposal);
 		return true;
 	}
 
 	private static boolean getMoveToImmediateSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals)
 			throws CoreException {
 		if (!(coveringNode instanceof SimpleName) || !(coveringNode.getParent() instanceof MethodDeclaration)) {
 			return false;
 		}
 		MethodDeclaration methodDeclaration= (MethodDeclaration)coveringNode.getParent();
 		if (methodDeclaration.getName() != coveringNode || methodDeclaration.resolveBinding() == null) {
 			return false;
 		}
 		if (proposals == null) {
 			return true;
 		}
 
 		final ICompilationUnit cu= context.getCompilationUnit();
 		CompilationUnit cuASTNode= (CompilationUnit)methodDeclaration.getRoot();
 		ASTNode typeDeclarationASTNode= methodDeclaration.getParent();
 		ASTRewrite rewrite= ASTRewrite.create(typeDeclarationASTNode.getAST());
 		String label= "Move to immediate superclass";
 		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
 		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
 		ASTNode placeHolderForMethodDeclaration= rewrite.createMoveTarget(methodDeclaration);
 		SimpleType superTypeASTNode= (SimpleType)typeDeclarationASTNode.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
 		ITypeBinding superTypeBinding= superTypeASTNode.resolveBinding();
 		// See http://wiki.eclipse.org/JDT/FAQ#From_an_IBinding_to_its_declaring_ASTNode
 		ASTNode SuperTypeDeclarationASTNode= cuASTNode.findDeclaringNode(superTypeBinding);
 		ListRewrite superTypeMembersListRewrite= rewrite.getListRewrite(SuperTypeDeclarationASTNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
 		superTypeMembersListRewrite.insertFirst(placeHolderForMethodDeclaration, null);
 		rewrite.remove(methodDeclaration, null);
 		proposals.add(proposal);
 		return true;
 	}
 
 
 	private static boolean getMoveTypeToNewFileProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals) throws CoreException {
 		if (!(coveringNode instanceof TypeDeclaration)) {
 			return false;
 		}
 
 		if (proposals == null) {
 			return true;
 		}
 
 		final ICompilationUnit cu= context.getCompilationUnit();
 		ITypeBinding typeBinding= ((TypeDeclaration)coveringNode).resolveBinding();
 		IType type= (IType)typeBinding.getJavaElement();
 		final MoveInnerToTopRefactoring moveInnerToTopRefactoring= new MoveInnerToTopRefactoring(type, null);
 
 		if (moveInnerToTopRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			String label= "Move selected type to new file";
 
 			Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
 			int relevance= problemsAtLocation ? 1 : 4;
 			RefactoringStatus status= moveInnerToTopRefactoring.checkFinalConditions(new NullProgressMonitor());
 			Change change= null;
 			if (status.hasFatalError()) {
 				change= new TextFileChange("fatal error", (IFile)cu.getResource()); //$NON-NLS-1$
 				((TextFileChange)change).setEdit(new InsertEdit(0, "")); //$NON-NLS-1$
 			} else {
 				change= moveInnerToTopRefactoring.createChange(new NullProgressMonitor());
 			}
 			ChangeCorrectionProposal proposal= new ChangeCorrectionProposal(label, change, relevance, image);
 
 			proposals.add(proposal);
 		}
 		return true;
 	}
 
 }
