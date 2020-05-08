 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.refactorings.usesupertypeininstanceof;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.NodeFinder;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
 import org.eclipse.jdt.core.refactoring.descriptors.UseSupertypeDescriptor;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.eclipse.jdt.core.search.SearchMatch;
 import org.eclipse.jdt.core.search.SearchPattern;
 import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
 import org.eclipse.jdt.internal.corext.refactoring.Checks;
 import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
 import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
 import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
 import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
 import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
 import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
 import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
 import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
 import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
 import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
 import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
 import org.eclipse.jdt.internal.corext.util.Messages;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
 import org.eclipse.jdt.ui.JavaElementLabels;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.Refactoring;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
 
 @SuppressWarnings("restriction")
 public class UseSuperTypeInInstanceOfRefactoring extends Refactoring {
 
 	/**
 	 * CompilationUnitRewrites for all affected cus
 	 */
 	private Map<ICompilationUnit, CompilationUnitRewrite> fRewrites;
 
 	/**
 	 * Text change manager (actually a CompilationUnitChange manager) which manages all changes.
 	 */
 	private TextChangeManager fChangeManager;
 
	/** The number of files affected by the last change generation */
	private int fChanges= 0;

 	/** The subtype to replace */
 	private IType fSubType;
 
 	/** The supertype as replacement */
 	private IType fSuperType;
 
 	public UseSuperTypeInInstanceOfRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
 		RefactoringStatus initializeStatus= initialize(arguments);
 		status.merge(initializeStatus);
 	}
 
 	private final RefactoringStatus initialize(JavaRefactoringArguments extended) {
 		String handle= extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
 		if (handle != null) {
 			final IJavaElement element= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
 			if (element == null || !element.exists() || element.getElementType() != IJavaElement.TYPE)
 				return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), UseSupertypeInInstanceOfDescriptor.ID);
 			else
 				fSubType= (IType)element;
 		} else
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
 		handle= extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1);
 		if (handle != null) {
 			final IJavaElement element= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
 			if (element == null || !element.exists() || element.getElementType() != IJavaElement.TYPE)
 				return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getName(), UseSupertypeInInstanceOfDescriptor.ID);
 			else
 				fSuperType= (IType)element;
 		} else
 			return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1));
 		return new RefactoringStatus();
 	}
 
 	/**
 	 * Returns the subtype to be replaced.
 	 * 
 	 * @return The subtype to be replaced
 	 */
 	public final IType getSubType() {
 		return fSubType;
 	}
 
 	/**
 	 * Returns the supertype as replacement.
 	 * 
 	 * @return The supertype as replacement
 	 */
 	public final IType getSuperType() {
 		return fSuperType;
 	}
 
 	private CompilationUnitRewrite getCachedCURewrite(ICompilationUnit unit) {
 		CompilationUnitRewrite rewrite= fRewrites.get(unit);
 		if (rewrite == null) {
 			rewrite= new CompilationUnitRewrite(unit);
 			fRewrites.put(unit, rewrite);
 		}
 		return rewrite;
 	}
 
 	private void createChangeAndDiscardRewrite(ICompilationUnit compilationUnit) throws CoreException {
 		CompilationUnitRewrite rewrite= fRewrites.get(compilationUnit);
 		if (rewrite != null) {
 			fChangeManager.manage(compilationUnit, rewrite.createChange(true));
 			fRewrites.remove(compilationUnit);
 		}
 	}
 
 	private IFile[] getAllFilesToModify() {
 		List<ICompilationUnit> cus= new ArrayList<ICompilationUnit>();
 		cus.addAll(Arrays.asList(fChangeManager.getAllCompilationUnits()));
 		return ResourceUtil.getFiles(cus.toArray(new ICompilationUnit[cus.size()]));
 	}
 
 	private static ASTNode getSelectedNode(ICompilationUnit unit, CompilationUnit root, int offset, int length) {
 		ASTNode node= null;
 		try {
 			if (unit != null)
 				node= checkNode(NodeFinder.perform(root, offset, length, unit));
 			else
 				node= checkNode(NodeFinder.perform(root, offset, length));
 		} catch (JavaModelException e) {
 			// Do nothing
 		}
 		if (node != null)
 			return node;
 		return checkNode(NodeFinder.perform(root, offset, length));
 	}
 
 	private static ASTNode checkNode(ASTNode node) {
 		if (node == null || node.getParent() == null || node.getParent().getParent() == null) {
 			return null;
 		} else if (node.getNodeType() == ASTNode.SIMPLE_NAME && node.getParent().getNodeType() == ASTNode.SIMPLE_TYPE && node.getParent().getParent().getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
 			return node.getParent();
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "Use super type in instanceof expressions";
 	}
 
 	@Override
 	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		fRewrites= new HashMap<ICompilationUnit, CompilationUnitRewrite>();
 		return new RefactoringStatus();
 	}
 
 	@Override
 	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
 		RefactoringStatus result= new RefactoringStatus();
 		fChangeManager= new TextChangeManager();
 		for (Iterator<CompilationUnitRewrite> iter= fRewrites.values().iterator(); iter.hasNext();) {
 			iter.next().clearASTAndImportRewrites();
 		}
 		SearchPattern pattern= RefactoringSearchEngine.createOrPattern(new IJavaElement[] { fSubType }, IJavaSearchConstants.REFERENCES);
 		IJavaSearchScope scope= RefactoringScopeFactory.create(fSubType, false);
 		SearchResultGroup[] allReferences= RefactoringSearchEngine.search(pattern, scope, pm, result);
 		SearchResultGroup[] references= Checks.excludeCompilationUnits(allReferences, result);
 		if (result.hasFatalError()) {
 			return result;
 		}
 		result.merge(Checks.checkCompileErrorsInAffectedFiles(references));
 		if (result.hasFatalError()) {
 			return result;
 		}
		int ticksPerCU= references.length == 0 ? 0 : 70 / references.length;
 		for (int i= 0; i < references.length; ++i) {
 			SearchResultGroup group= references[i];
 			SearchMatch[] searchResults= group.getSearchResults();
 			CompilationUnitRewrite currentCURewrite= getCachedCURewrite(group.getCompilationUnit());
 
 			for (int j= 0; j < searchResults.length; j++) {
 				SearchMatch match= searchResults[j];
 				if (match.isInsideDocComment())
 					continue;
 
 				ASTNode target= getSelectedNode(group.getCompilationUnit(), currentCURewrite.getRoot(), match.getOffset(), match.getLength());
 
 				if (target != null) {
 					currentCURewrite.getImportRewrite().addImport(getSuperType().getFullyQualifiedName());
 					SimpleName superTypeName= currentCURewrite.getASTRewrite().getAST().newSimpleName(getSuperType().getElementName());
 					SimpleType superTypeNode= currentCURewrite.getASTRewrite().getAST().newSimpleType(superTypeName);
 					currentCURewrite.getASTRewrite().replace(target, superTypeNode, null);
 				}
 
 			}
 
 			createChangeAndDiscardRewrite(group.getCompilationUnit());
 		}
 
 		result.merge(Checks.validateModifiesFiles(getAllFilesToModify(), getValidationContext()));
 		return result;
 	}
 
 	@Override
 	public final Change createChange(final IProgressMonitor monitor) throws CoreException, OperationCanceledException {
 		Assert.isNotNull(monitor);
 		try {
			fChanges= 0;
 			monitor.beginTask("", 1); //$NON-NLS-1$
 			monitor.setTaskName(RefactoringCoreMessages.ExtractInterfaceProcessor_creating);
 			final TextEditBasedChange[] changes= fChangeManager.getAllChanges();
 			if (changes != null && changes.length != 0) {
				fChanges= changes.length;
 				IJavaProject project= null;
 				if (!fSubType.isBinary())
 					project= fSubType.getJavaProject();
 				int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
 				try {
 					if (fSubType.isLocal() || fSubType.isAnonymous())
 						flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
 				} catch (JavaModelException exception) {
 					JavaPlugin.log(exception);
 				}
 				final String name= project != null ? project.getElementName() : null;
 				final String description= Messages.format(RefactoringCoreMessages.UseSuperTypeProcessor_descriptor_description_short,
 						BasicElementLabels.getJavaElementName(fSuperType.getElementName()));
 				final String header= Messages.format(
 						RefactoringCoreMessages.UseSuperTypeProcessor_descriptor_description,
 						new String[] { JavaElementLabels.getElementLabel(fSuperType, JavaElementLabels.ALL_FULLY_QUALIFIED),
 								JavaElementLabels.getElementLabel(fSubType, JavaElementLabels.ALL_FULLY_QUALIFIED) });
 				final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(name, this, header);
 				comment.addSetting(Messages.format(RefactoringCoreMessages.UseSuperTypeProcessor_refactored_element_pattern,
 						JavaElementLabels.getElementLabel(fSuperType, JavaElementLabels.ALL_FULLY_QUALIFIED)));
 				final UseSupertypeDescriptor descriptor= RefactoringSignatureDescriptorFactory.createUseSupertypeDescriptor();
 				descriptor.setProject(name);
 				descriptor.setDescription(description);
 				descriptor.setComment(comment.asString());
 				descriptor.setFlags(flags);
 				descriptor.setSubtype(getSubType());
 				descriptor.setSupertype(getSuperType());
 				return new DynamicValidationRefactoringChange(descriptor, RefactoringCoreMessages.UseSupertypeWherePossibleRefactoring_name, fChangeManager.getAllChanges());
 			}
 			monitor.worked(1);
 		} finally {
 			monitor.done();
 		}
 		return null;
 	}
 
 }
