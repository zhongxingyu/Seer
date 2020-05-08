 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.corext.refactoring.structure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.SourceRange;
 import org.eclipse.dltk.core.manipulation.RefactoringChecks;
 import org.eclipse.dltk.core.manipulation.SourceModuleChange;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.internal.corext.refactoring.CollectingSearchRequestor;
 import org.eclipse.dltk.internal.corext.refactoring.RefactoringScopeFactory;
 import org.eclipse.dltk.internal.corext.refactoring.RefactoringSearchEngine;
 import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptor;
 import org.eclipse.dltk.internal.corext.refactoring.SearchResultGroup;
 import org.eclipse.dltk.internal.corext.refactoring.base.ScriptStatusContext;
 import org.eclipse.dltk.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
 import org.eclipse.dltk.internal.corext.refactoring.util.ResourceUtil;
 import org.eclipse.dltk.internal.corext.refactoring.util.TextChangeManager;
 import org.eclipse.dltk.internal.javascript.core.manipulation.JavascriptManipulationPlugin;
 import org.eclipse.dltk.internal.javascript.core.manipulation.Messages;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.Checks;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.ParameterInfo;
 import org.eclipse.dltk.internal.javascript.corext.refactoring.RefactoringCoreMessages;
 import org.eclipse.dltk.javascript.core.dom.CallExpression;
 import org.eclipse.dltk.javascript.core.dom.DomFactory;
 import org.eclipse.dltk.javascript.core.dom.DomPackage;
 import org.eclipse.dltk.javascript.core.dom.Expression;
 import org.eclipse.dltk.javascript.core.dom.FunctionExpression;
 import org.eclipse.dltk.javascript.core.dom.Identifier;
 import org.eclipse.dltk.javascript.core.dom.Node;
 import org.eclipse.dltk.javascript.core.dom.Parameter;
 import org.eclipse.dltk.javascript.core.dom.SimplePropertyAssignment;
 import org.eclipse.dltk.javascript.core.dom.Source;
import org.eclipse.dltk.javascript.core.dom.VariableDeclaration;
 import org.eclipse.dltk.javascript.core.dom.VariableReference;
 import org.eclipse.dltk.javascript.core.dom.rewrite.ASTConverter;
 import org.eclipse.dltk.javascript.core.dom.rewrite.NodeFinder;
 import org.eclipse.dltk.javascript.core.dom.rewrite.RewriteAnalyzer;
 import org.eclipse.dltk.javascript.core.dom.rewrite.VariableLookup;
 import org.eclipse.dltk.javascript.core.refactoring.descriptors.ChangeMethodSignatureDescriptor;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.change.ChangeDescription;
 import org.eclipse.emf.ecore.change.util.ChangeRecorder;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
 import org.eclipse.ltk.core.refactoring.TextChange;
 import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
 import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
 import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
 import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
 
 
 public class ChangeSignatureProcessor extends RefactoringProcessor {
 
 	//private static final String ATTRIBUTE_RETURN= "return"; //$NON-NLS-1$
 	//private static final String ATTRIBUTE_VISIBILITY= "visibility"; //$NON-NLS-1$
 	private static final String ATTRIBUTE_PARAMETER= "parameter"; //$NON-NLS-1$
 	private static final String ATTRIBUTE_DEFAULT= "default"; //$NON-NLS-1$
 	//private static final String ATTRIBUTE_KIND= "kind"; //$NON-NLS-1$
 	//private static final String ATTRIBUTE_DELEGATE= "delegate"; //$NON-NLS-1$
 	//private static final String ATTRIBUTE_DEPRECATE= "deprecate"; //$NON-NLS-1$
 
 	private List<ParameterInfo> fParameterInfos;
 
 	//private SourceModuleRewrite fBaseCuRewrite;
 	//private List fExceptionInfos;
 	private TextChangeManager fChangeManager;
 
 	private IMethod fMethod;
 	/*private IMethod fTopMethod;
 	private IMethod[] fRippleMethods;*/
 	private SearchResultGroup[] fReferences;
 	/*private ReturnTypeInfo fReturnTypeInfo;*/
 	private String fMethodName;
 	/*private int fVisibility;
 
 	private StubTypeContext fContextCuStartEnd;
 	private int fOldVarargIndex; // initialized in checkVarargs()
 
 	private BodyUpdater fBodyUpdater;
 	private IDefaultValueAdvisor fDefaultValueAdvisor;
 
 	private ITypeHierarchy fCachedTypeHierarchy= null;
 	private boolean fDelegateUpdating;
 	private boolean fDelegateDeprecation;
 
 	public ChangeSignatureProcessor(JavaRefactoringArguments arguments, RefactoringStatus status) throws JavaModelException {
 		this((IMethod) null);
 		status.merge(initialize(arguments));
 	}*/
 
 	/**
 	 * Creates a new change signature refactoring.
 	 * @param method the method, or <code>null</code> if invoked by scripting framework
 	 * @throws JavaModelException if something's wrong with the given method
 	 */
 	public ChangeSignatureProcessor(IMethod method) {
 		fMethod= method;
 		//fOldVarargIndex= -1;
 		//fDelegateUpdating= false;
 		//fDelegateDeprecation= true;
 		if (fMethod != null) {
 			fParameterInfos= createParameterInfoList(method);
 			// fExceptionInfos is created in checkInitialConditions
 			//fReturnTypeInfo= new ReturnTypeInfo(Signature.toString(Signature.getReturnType(fMethod.getSignature())));
 			fMethodName= fMethod.getElementName();
 			//fVisibility= JdtFlags.getVisibilityCode(fMethod);
 		}
 	}
 
 	private static List<ParameterInfo> createParameterInfoList(IMethod method) {
 		try {
 			//String[] typeNames= method.getParameterTypes();
 			String[] oldNames= method.getParameterNames();
 			List<ParameterInfo> result= new ArrayList<ParameterInfo>(oldNames.length);
 			for (int i= 0; i < oldNames.length; i++){
 				ParameterInfo parameterInfo;
 				/*if (i == oldNames.length - 1 && Flags.isVarargs(method.getFlags())) {
 					String varargSignature= typeNames[i];
 					int arrayCount= Signature.getArrayCount(varargSignature);
 					String baseSignature= Signature.getElementType(varargSignature);
 					if (arrayCount > 1)
 						baseSignature= Signature.createArraySignature(baseSignature, arrayCount - 1);
 					parameterInfo= new ParameterInfo(Signature.toString(baseSignature) + ParameterInfo.ELLIPSIS, oldNames[i], i);
 				} else {
 					parameterInfo= new ParameterInfo(Signature.toString(typeNames[i]), oldNames[i], i);
 				}*/
 				// TODO retrieve types
 				parameterInfo = new ParameterInfo("", oldNames[i], i);
 				result.add(parameterInfo);
 			}
 			return result;
 		} catch(ModelException e) {
 			JavascriptManipulationPlugin.log(e);
 			return new ArrayList<ParameterInfo>(0);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#getProcessorName()
 	 */
 	public String getProcessorName() {
 		return RefactoringCoreMessages.ChangeSignatureRefactoring_modify_Parameters;
 	}
 
 	/*public IMethod getMethod() {
 		return fMethod;
 	}*/
 
 	public String getMethodName() {
 		return fMethodName;
 	}
 
 	/*public String getReturnTypeString() {
 		return fReturnTypeInfo.getNewTypeName();
 	}*/
 
 	public void setNewMethodName(String newMethodName){
 		Assert.isNotNull(newMethodName);
 		fMethodName= newMethodName;
 	}
 
 	/*public void setNewReturnTypeName(String newReturnTypeName){
 		Assert.isNotNull(newReturnTypeName);
 		fReturnTypeInfo.setNewTypeName(newReturnTypeName);
 	}*/
 
 	public boolean canChangeNameAndReturnType(){
 		try {
 			return ! fMethod.isConstructor();
 		} catch (ModelException e) {
 			JavascriptManipulationPlugin.log(e);
 			return false;
 		}
 	}
 
 	/**
 	 * @return visibility
 	 * @see org.eclipse.jdt.core.dom.Modifier
 	 */
 	/*public int getVisibility(){
 		return fVisibility;
 	}*/
 
 	/**
 	 * @param visibility new visibility
 	 * @see org.eclipse.jdt.core.dom.Modifier
 	 */
 	/*public void setVisibility(int visibility){
 		Assert.isTrue(	visibility == Modifier.PUBLIC ||
 		            	visibility == Modifier.PROTECTED ||
 		            	visibility == Modifier.NONE ||
 		            	visibility == Modifier.PRIVATE);
 		fVisibility= visibility;
 	}*/
 
 	/*
 	 * @see JdtFlags
 	 */
 	/*public int[] getAvailableVisibilities() throws JavaModelException{
 		if (fTopMethod.getDeclaringType().isInterface())
 			return new int[]{Modifier.PUBLIC};
 		else if (fTopMethod.getDeclaringType().isEnum() && fTopMethod.isConstructor())
 			return new int[]{	Modifier.NONE,
 								Modifier.PRIVATE};
 		else
 			return new int[]{	Modifier.PUBLIC,
 								Modifier.PROTECTED,
 								Modifier.NONE,
 								Modifier.PRIVATE};
 	}*/
 
 	/**
 	 *
 	 * @return List of <code>ParameterInfo</code> objects.
 	 */
 	public List<ParameterInfo> getParameterInfos(){
 		return fParameterInfos;
 	}
 
 	/**
 	 * @return List of <code>ExceptionInfo</code> objects.
 	 */
 	/*public List getExceptionInfos(){
 		return fExceptionInfos;
 	}
 
 	public void setBodyUpdater(BodyUpdater bodyUpdater) {
 		fBodyUpdater= bodyUpdater;
 	}
 
 	public CompilationUnitRewrite getBaseCuRewrite() {
 		return fBaseCuRewrite;
 	}
 
 	//------------------- IDelegateUpdating ----------------------
 
 	public boolean canEnableDelegateUpdating() {
 		return true;
 	}
 
 	public boolean getDelegateUpdating() {
 		return fDelegateUpdating;
 	}
 
 	public void setDelegateUpdating(boolean updating) {
 		fDelegateUpdating= updating;
 	}
 
 	public void setDeprecateDelegates(boolean deprecate) {
 		fDelegateDeprecation= deprecate;
 	}
 
 	public boolean getDeprecateDelegates() {
 		return fDelegateDeprecation;
 	}
 
 	public String getDelegateUpdatingTitle(boolean plural) {
 		if (plural)
 			return RefactoringCoreMessages.DelegateCreator_keep_original_changed_plural;
 		else
 			return RefactoringCoreMessages.DelegateCreator_keep_original_changed_singular;
 	}*/
 
 	//------------------- /IDelegateUpdating ---------------------
 
 	public RefactoringStatus checkSignature() {
 		return checkSignature(false);
 	}
 
 	private RefactoringStatus checkSignature(boolean resolveBindings) {
 		RefactoringStatus result= new RefactoringStatus();
 		checkMethodName(result);
 		if (result.hasFatalError())
 			return result;
 
 		checkParameterNamesAndValues(result);
 		if (result.hasFatalError())
 			return result;
 
 		checkForDuplicateParameterNames(result);
 		if (result.hasFatalError())
 			return result;
 
 		/*try {
 			RefactoringStatus[] typeStati;
 			if (resolveBindings)
 				typeStati= TypeContextChecker.checkAndResolveMethodTypes(fMethod, getStubTypeContext(), getNotDeletedInfos(), fReturnTypeInfo);
 			else
 				typeStati= TypeContextChecker.checkMethodTypesSyntax(fMethod, getNotDeletedInfos(), fReturnTypeInfo);
 			for (int i= 0; i < typeStati.length; i++)
 				result.merge(typeStati[i]);
 
 			result.merge(checkVarargs());
 		} catch (CoreException e) {
 			//cannot do anything here
 			throw new RuntimeException(e);
 		}*/
 
 		//checkExceptions() unnecessary (IType always ok)
 		return result;
 	}
 
 	public boolean isSignatureSameAsInitial() throws ModelException {
 		//if (! isVisibilitySameAsInitial())
 		//	return false;
 		if (! isMethodNameSameAsInitial())
 			return false;
 		//if (! isReturnTypeSameAsInitial())
 		//	return false;
 		//if (! areExceptionsSameAsInitial())
 		//	return false;
 
 		if (fMethod.getParameters().length == 0 && fParameterInfos.isEmpty())
 			return true;
 
 		if (areNamesSameAsInitial() && isOrderSameAsInitial() && areParameterTypesSameAsInitial())
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * @return true if the new method cannot coexist with the old method since
 	 *         the signatures are too much alike
 	 */
 	/*public boolean isSignatureClashWithInitial() {
 
 		if (!isMethodNameSameAsInitial())
 			return false; // name has changed.
 
 		if (fMethod.getNumberOfParameters() == 0 && fParameterInfos.isEmpty())
 			return true; // name is equal and both parameter lists are empty
 
 		// name is equal and there are some parameters.
 		// check if there are more or less parameters than before
 
 		int no= getNotDeletedInfos().size();
 
 		if (fMethod.getNumberOfParameters() != no)
 			return false;
 
 		// name is equal and parameter count is equal.
 		// check whether types remained the same
 
 		if (isOrderSameAsInitial())
 			return areParameterTypesSameAsInitial();
 		else
 			return false; // could be more specific here
 	}*/
 
 	private boolean areParameterTypesSameAsInitial() {
 		for (ParameterInfo info : fParameterInfos) {
 			if (! info.isAdded() && ! info.isDeleted() && info.isTypeNameChanged())
 				return false;
 		}
 		return true;
 	}
 
 	/*private boolean isReturnTypeSameAsInitial() {
 		return ! fReturnTypeInfo.isTypeNameChanged();
 	}*/
 
 	private boolean isMethodNameSameAsInitial() {
 		return fMethodName.equals(fMethod.getElementName());
 	}
 
 	/*private boolean areExceptionsSameAsInitial() {
 		for (Iterator iter= fExceptionInfos.iterator(); iter.hasNext();) {
 			ExceptionInfo info= (ExceptionInfo) iter.next();
 			if (! info.isOld())
 				return false;
 		}
 		return true;
 	}*/
 
 	private void checkParameterNamesAndValues(RefactoringStatus result) {
 		int i= 1;
 		for (ParameterInfo info : fParameterInfos) {
 			if (info.isDeleted())
 				continue;
 			checkParameterName(result, info, i);
 			if (result.hasFatalError())
 				return;
 			if (info.isAdded())	{
 				checkParameterDefaultValue(result, info);
 				if (result.hasFatalError())
 					return;
 			}
 		}
 	}
 
 	private void checkParameterName(RefactoringStatus result, ParameterInfo info, int position) {
 		if (info.getNewName().trim().length() == 0) {
 			result.addFatalError(Messages.format(
 					RefactoringCoreMessages.ChangeSignatureRefactoring_param_name_not_empty, Integer.toString(position)));
 		} else {
 			result.merge(Checks.validateIdentifier(info.getNewName()));
 			// TODO checkTempName is actually more complicated
 			//result.merge(Checks.checkTempName(info.getNewName(), fMethod));
 		}
 	}
 
 	private void checkMethodName(RefactoringStatus result) {
 		if (isMethodNameSameAsInitial() || ! canChangeNameAndReturnType())
 			return;
 		if ("".equals(fMethodName.trim())) { //$NON-NLS-1$
 			String msg= RefactoringCoreMessages.ChangeSignatureRefactoring_method_name_not_empty;
 			result.addFatalError(msg);
 			return;
 		}
 		result.merge(Checks.validateIdentifier(fMethodName));
 		//result.merge(Checks.checkMethodName(fMethodName, fMethod));
 	}
 
 	private void checkParameterDefaultValue(RefactoringStatus result, ParameterInfo info) {
 		/*if (fDefaultValueAdvisor != null)
 			return;
 		if (info.isNewVarargs()) {
 			if (! isValidVarargsExpression(info.getDefaultValue())){
 				String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_invalid_expression, new String[]{info.getDefaultValue()});
 				result.addFatalError(msg);
 			}
 			return;
 		}*/
 
 		if (info.getDefaultValue().trim().equals("")){ //$NON-NLS-1$
 			String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_default_value,
 					info.getNewName()/*BasicElementLabels.getJavaElementName(info.getNewName())*/);
 			result.addFatalError(msg);
 			return;
 		}
 		// TODO check default value for validity
 		/*if (! isValidExpression(info.getDefaultValue())) {
 			String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_invalid_expression, new String[]{info.getDefaultValue()});
 			result.addFatalError(msg);
 		}*/
 	}
 
 	/*private RefactoringStatus checkVarargs() throws JavaModelException {
 		RefactoringStatus result= checkOriginalVarargs();
 		if (result != null)
 			return result;
 
 		if (fRippleMethods != null) {
 			for (int iRipple= 0; iRipple < fRippleMethods.length; iRipple++) {
 				IMethod rippleMethod= fRippleMethods[iRipple];
 				if (! JdtFlags.isVarargs(rippleMethod))
 					continue;
 
 				// Vararg method can override method that takes an array as last argument
 				fOldVarargIndex= rippleMethod.getNumberOfParameters() - 1;
 				List notDeletedInfos= getNotDeletedInfos();
 				for (int i= 0; i < notDeletedInfos.size(); i++) {
 					ParameterInfo info= (ParameterInfo) notDeletedInfos.get(i);
 					if (fOldVarargIndex != -1 && info.getOldIndex() == fOldVarargIndex && ! info.isNewVarargs()) {
 						String rippleMethodType= rippleMethod.getDeclaringType().getFullyQualifiedName('.');
 						String message= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_ripple_cannot_convert_vararg, new Object[] { BasicElementLabels.getJavaElementName(info.getNewName()), BasicElementLabels.getJavaElementName(rippleMethodType)});
 						return RefactoringStatus.createFatalErrorStatus(message, JavaStatusContext.create(rippleMethod));
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private RefactoringStatus checkOriginalVarargs() throws JavaModelException {
 		if (JdtFlags.isVarargs(fMethod))
 			fOldVarargIndex= fMethod.getNumberOfParameters() - 1;
 		List notDeletedInfos= getNotDeletedInfos();
 		for (int i= 0; i < notDeletedInfos.size(); i++) {
 			ParameterInfo info= (ParameterInfo) notDeletedInfos.get(i);
 			if (info.isOldVarargs() && ! info.isNewVarargs())
 				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_cannot_convert_vararg, BasicElementLabels.getJavaElementName(info.getNewName())));
 			if (i != notDeletedInfos.size() - 1) {
 				// not the last parameter
 				if (info.isNewVarargs())
 					return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_vararg_must_be_last, BasicElementLabels.getJavaElementName(info.getNewName())));
 			}
 		}
 		return null;
 	}
 
 	private RefactoringStatus checkTypeVariables() {
 		if (fRippleMethods.length == 1)
 			return null;
 
 		RefactoringStatus result= new RefactoringStatus();
 		if (fReturnTypeInfo.isTypeNameChanged() && fReturnTypeInfo.getNewTypeBinding() != null) {
 			HashSet typeVariablesCollector= new HashSet();
 			collectTypeVariables(fReturnTypeInfo.getNewTypeBinding(), typeVariablesCollector);
 			if (typeVariablesCollector.size() != 0) {
 				ITypeBinding first= (ITypeBinding) typeVariablesCollector.iterator().next();
 				String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_return_type_contains_type_variable, new String[] {BasicElementLabels.getJavaElementName(fReturnTypeInfo.getNewTypeName()), BasicElementLabels.getJavaElementName(first.getName())});
 				result.addError(msg);
 			}
 		}
 
 		for (Iterator iter= getNotDeletedInfos().iterator(); iter.hasNext();) {
 			ParameterInfo info= (ParameterInfo) iter.next();
 			if (info.isTypeNameChanged() && info.getNewTypeBinding() != null) {
 				HashSet typeVariablesCollector= new HashSet();
 				collectTypeVariables(info.getNewTypeBinding(), typeVariablesCollector);
 				if (typeVariablesCollector.size() != 0) {
 					ITypeBinding first= (ITypeBinding) typeVariablesCollector.iterator().next();
 					String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_parameter_type_contains_type_variable, new String[] {BasicElementLabels.getJavaElementName(info.getNewTypeName()), BasicElementLabels.getJavaElementName(info.getNewName()), BasicElementLabels.getJavaElementName(first.getName())});
 					result.addError(msg);
 				}
 			}
 		}
 		return result;
 	}
 
 	private void collectTypeVariables(ITypeBinding typeBinding, Set typeVariablesCollector) {
 		if (typeBinding.isTypeVariable()) {
 			typeVariablesCollector.add(typeBinding);
 			ITypeBinding[] typeBounds= typeBinding.getTypeBounds();
 			for (int i= 0; i < typeBounds.length; i++)
 				collectTypeVariables(typeBounds[i], typeVariablesCollector);
 
 		} else if (typeBinding.isArray()) {
 			collectTypeVariables(typeBinding.getElementType(), typeVariablesCollector);
 
 		} else if (typeBinding.isParameterizedType()) {
 			ITypeBinding[] typeArguments= typeBinding.getTypeArguments();
 			for (int i= 0; i < typeArguments.length; i++)
 				collectTypeVariables(typeArguments[i], typeVariablesCollector);
 
 		} else if (typeBinding.isWildcardType()) {
 			ITypeBinding bound= typeBinding.getBound();
 			if (bound != null) {
 				collectTypeVariables(bound, typeVariablesCollector);
 			}
 		}
 	}
 
 	public static boolean isValidExpression(String string){
 		String trimmed= string.trim();
 		if ("".equals(trimmed)) //speed up for a common case //$NON-NLS-1$
 			return false;
 		StringBuffer cuBuff= new StringBuffer();
 		cuBuff.append(CONST_CLASS_DECL)
 			  .append("Object") //$NON-NLS-1$
 			  .append(CONST_ASSIGN);
 		int offset= cuBuff.length();
 		cuBuff.append(trimmed)
 			  .append(CONST_CLOSE);
 		ASTParser p= ASTParser.newParser(AST.JLS3);
 		p.setSource(cuBuff.toString().toCharArray());
 		CompilationUnit cu= (CompilationUnit) p.createAST(null);
 		Selection selection= Selection.createFromStartLength(offset, trimmed.length());
 		SelectionAnalyzer analyzer= new SelectionAnalyzer(selection, false);
 		cu.accept(analyzer);
 		ASTNode selected= analyzer.getFirstSelectedNode();
 		return (selected instanceof Expression) &&
 				trimmed.equals(cuBuff.substring(cu.getExtendedStartPosition(selected), cu.getExtendedStartPosition(selected) + cu.getExtendedLength(selected)));
 	}
 
 	public static boolean isValidVarargsExpression(String string) {
 		String trimmed= string.trim();
 		if ("".equals(trimmed)) //speed up for a common case //$NON-NLS-1$
 			return true;
 		StringBuffer cuBuff= new StringBuffer();
 		cuBuff.append("class A{ {m("); //$NON-NLS-1$
 		int offset= cuBuff.length();
 		cuBuff.append(trimmed)
 			  .append(");}}"); //$NON-NLS-1$
 		ASTParser p= ASTParser.newParser(AST.JLS3);
 		p.setSource(cuBuff.toString().toCharArray());
 		CompilationUnit cu= (CompilationUnit) p.createAST(null);
 		Selection selection= Selection.createFromStartLength(offset, trimmed.length());
 		SelectionAnalyzer analyzer= new SelectionAnalyzer(selection, false);
 		cu.accept(analyzer);
 		ASTNode[] selectedNodes= analyzer.getSelectedNodes();
 		if (selectedNodes.length == 0)
 			return false;
 		for (int i= 0; i < selectedNodes.length; i++) {
 			if (! (selectedNodes[i] instanceof Expression))
 				return false;
 		}
 		return true;
 	}
 
 	public StubTypeContext getStubTypeContext() {
 		try {
 			if (fContextCuStartEnd == null)
 				fContextCuStartEnd= TypeContextChecker.createStubTypeContext(getCu(), fBaseCuRewrite.getRoot(), fMethod.getSourceRange().getOffset());
 		} catch (CoreException e) {
 			//cannot do anything here
 			throw new RuntimeException(e);
 		}
 		return fContextCuStartEnd;
 	}
 
 	private ITypeHierarchy getCachedTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
 		if (fCachedTypeHierarchy == null)
 			fCachedTypeHierarchy= fMethod.getDeclaringType().newTypeHierarchy(new SubProgressMonitor(monitor, 1));
 		return fCachedTypeHierarchy;
 	}*/
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor.beginTask("", 1); //$NON-NLS-1$
 			RefactoringStatus result= RefactoringChecks.checkIfCuBroken(fMethod);
 			if (result.hasFatalError())
 				return result;
 			if (fMethod == null || !fMethod.exists()) {
 				String message= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_method_deleted,
 						getCu().getResource().getName()/*BasicElementLabels.getFileName(getCu())*/);
 				return RefactoringStatus.createFatalErrorStatus(message);
 			}
 			// TODO most of checks are irrelevant, not necessarily all of them
 			/*if (fMethod.getDeclaringType().isInterface()) {
 				fTopMethod= MethodChecks.overridesAnotherMethod(fMethod, fMethod.getDeclaringType().newSupertypeHierarchy(new SubProgressMonitor(monitor, 1)));
 				monitor.worked(1);
 			} else if (MethodChecks.isVirtual(fMethod)) {
 				ITypeHierarchy hierarchy= getCachedTypeHierarchy(new SubProgressMonitor(monitor, 1));
 				fTopMethod= MethodChecks.isDeclaredInInterface(fMethod, hierarchy, new SubProgressMonitor(monitor, 1));
 				if (fTopMethod == null)
 					fTopMethod= MethodChecks.overridesAnotherMethod(fMethod, hierarchy);
 			}
 			if (fTopMethod == null)
 				fTopMethod= fMethod;
 			if (! fTopMethod.equals(fMethod)) {
 				if (fTopMethod.getDeclaringType().isInterface()) {
 					RefactoringStatusContext context= JavaStatusContext.create(fTopMethod);
 					String message= Messages.format(RefactoringCoreMessages.MethodChecks_implements,
 							new String[]{JavaElementUtil.createMethodSignature(fTopMethod), BasicElementLabels.getJavaElementName(fTopMethod.getDeclaringType().getFullyQualifiedName('.'))});
 					return RefactoringStatus.createStatus(RefactoringStatus.FATAL, message, context, Corext.getPluginId(), RefactoringStatusCodes.METHOD_DECLARED_IN_INTERFACE, fTopMethod);
 				} else {
 					RefactoringStatusContext context= JavaStatusContext.create(fTopMethod);
 					String message= Messages.format(RefactoringCoreMessages.MethodChecks_overrides,
 							new String[]{JavaElementUtil.createMethodSignature(fTopMethod), BasicElementLabels.getJavaElementName(fTopMethod.getDeclaringType().getFullyQualifiedName('.'))});
 					return RefactoringStatus.createStatus(RefactoringStatus.FATAL, message, context, Corext.getPluginId(), RefactoringStatusCodes.OVERRIDES_ANOTHER_METHOD, fTopMethod);
 				}
 			}
 
 			if (monitor.isCanceled())
 				throw new OperationCanceledException();
 			
 			if (fBaseCuRewrite == null || !fBaseCuRewrite.getCu().equals(getCu())) {
 				fBaseCuRewrite= new CompilationUnitRewrite(getCu());
 				fBaseCuRewrite.getASTRewrite().setTargetSourceRangeComputer(new TightSourceRangeComputer());
 			}
 			RefactoringStatus[] status= TypeContextChecker.checkMethodTypesSyntax(fMethod, getParameterInfos(), fReturnTypeInfo);
 			for (int i= 0; i < status.length; i++) {
 				result.merge(status[i]);
 			}
 			monitor.worked(1);
 			result.merge(createExceptionInfoList());*/
 			monitor.worked(1);
 			return result;
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/*private RefactoringStatus createExceptionInfoList() {
 		if (fExceptionInfos == null || fExceptionInfos.isEmpty()) {
 			fExceptionInfos= new ArrayList(0);
 			try {
 				ASTNode nameNode= NodeFinder.perform(fBaseCuRewrite.getRoot(), fMethod.getNameRange());
 				if (nameNode == null || !(nameNode instanceof Name) || !(nameNode.getParent() instanceof MethodDeclaration))
 					return null;
 				MethodDeclaration methodDeclaration= (MethodDeclaration) nameNode.getParent();
 				List exceptions= methodDeclaration.thrownExceptions();
 				List result= new ArrayList(exceptions.size());
 				for (int i= 0; i < exceptions.size(); i++) {
 					Name name= (Name) exceptions.get(i);
 					ITypeBinding typeBinding= name.resolveTypeBinding();
 					if (typeBinding == null)
 						return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ChangeSignatureRefactoring_no_exception_binding);
 					IJavaElement element= typeBinding.getJavaElement();
 					result.add(ExceptionInfo.createInfoForOldException(element, typeBinding));
 				}
 				fExceptionInfos= result;
 			} catch (JavaModelException e) {
 				JavaPlugin.log(e);
 			}
 		}
 		return null;
 	}*/
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
 	 */
 	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
 		try {
 			pm.beginTask(RefactoringCoreMessages.ChangeSignatureRefactoring_checking_preconditions, 8);
 			RefactoringStatus result= new RefactoringStatus();
 			//fBaseCuRewrite.clearASTAndImportRewrites();
 			//fBaseCuRewrite.getASTRewrite().setTargetSourceRangeComputer(new TightSourceRangeComputer());
 
 			if (isSignatureSameAsInitial())
 				return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.ChangeSignatureRefactoring_unchanged);
 			result.merge(checkSignature(true));
 			if (result.hasFatalError())
 				return result;
 
 			//if (fDelegateUpdating && isSignatureClashWithInitial())
 			//	result.merge(RefactoringStatus.createErrorStatus(RefactoringCoreMessages.ChangeSignatureRefactoring_old_and_new_signatures_not_sufficiently_different ));
 
 			//String binaryRefsDescription= Messages.format(RefactoringCoreMessages.ReferencesInBinaryContext_ref_in_binaries_description , BasicElementLabels.getJavaElementName(getMethodName()));
 			//ReferencesInBinaryContext binaryRefs= new ReferencesInBinaryContext(binaryRefsDescription);
 			//fRippleMethods= RippleMethodFinder2.getRelatedMethods(fMethod, binaryRefs, new SubProgressMonitor(pm, 1), null);
 			//result.merge(checkVarargs());
 			if (result.hasFatalError())
 				return result;
 
 			fReferences= findReferences(new SubProgressMonitor(pm, 1), result);
 			/*binaryRefs.addErrorIfNecessary(result);
 
 			result.merge(checkVisibilityChanges());
 			result.merge(checkTypeVariables());
 
 			//TODO:
 			// We need a common way of dealing with possible compilation errors for all occurrences,
 			// including visibility problems, shadowing and missing throws declarations.
 
 			if (! isOrderSameAsInitial())
 				result.merge(checkReorderings(new SubProgressMonitor(pm, 1)));
 			else
 				pm.worked(1);
 
 			//TODO (bug 58616): check whether changed signature already exists somewhere in the ripple,
 			// - error if exists
 			// - warn if exists with different parameter types (may cause overloading)
 
 			if (! areNamesSameAsInitial())
 				result.merge(checkRenamings(new SubProgressMonitor(pm, 1)));
 			else
 				pm.worked(1);
 			if (result.hasFatalError())
 				return result;*/
 
 //			resolveTypesWithoutBindings(new SubProgressMonitor(pm, 1)); // already done in checkSignature(true)
 
 			createChangeManager(new SubProgressMonitor(pm, 1), result);
 			//fCachedTypeHierarchy= null;
 
 			/*if (mustAnalyzeAstOfDeclaringCu())
 				result.merge(checkCompilationofDeclaringCu()); //TODO: should also check in ripple methods (move into createChangeManager)
 			if (result.hasFatalError())
 				return result;*/
 
 			RefactoringChecks.addModifiedFilesToChecker(getAllFilesToModify(), context);
 			return result;
 		} finally {
 			pm.done();
 		}
 	}
 
 	protected void clearManagers() {
 		fChangeManager= null;
 	}
 
 	/*private RefactoringStatus checkVisibilityChanges() throws JavaModelException {
 		if (isVisibilitySameAsInitial())
 			return null;
 	    if (fRippleMethods.length == 1)
 	    	return null;
 	    Assert.isTrue(JdtFlags.getVisibilityCode(fMethod) != Modifier.PRIVATE);
 	    if (fVisibility == Modifier.PRIVATE)
 	    	return RefactoringStatus.createWarningStatus(RefactoringCoreMessages.ChangeSignatureRefactoring_non_virtual);
 		return null;
 	}*/
 
 	public String getOldMethodSignature() throws ModelException {
 		StringBuffer buff= new StringBuffer();
 		//buff.append(ScriptElementLabels.getElementLabel(fMethod.getParent(), JavaElementLabels.ALL_FULLY_QUALIFIED));
 		//buff.append('.');
 		buff.append(fMethod.getElementName())
 			.append("(") //$NON-NLS-1$
 			.append(getOldMethodParameters())
 			.append(")"); //$NON-NLS-1$
 
 		//return BasicElementLabels.getJavaCodeString(buff.toString());
 		return buff.toString();
 	}
 
 	public String getNewMethodSignature() throws ModelException {
 		StringBuffer buff= new StringBuffer();
 
 		buff.append("function ") //$NON-NLS-1$
 			.append(getMethodName())
 			.append("(") //$NON-NLS-1$
 			.append(getMethodParameters())
 			.append(")"); //$NON-NLS-1$
 
 		//return BasicElementLabels.getJavaCodeString(buff.toString());
 		return buff.toString();
 	}
 
 	/*private String getVisibilityString(int visibility) {
 		String visibilityString= JdtFlags.getVisibilityString(visibility);
 		if ("".equals(visibilityString)) //$NON-NLS-1$
 			return visibilityString;
 		return visibilityString + ' ';
 	}
 
 	private String getMethodThrows() {
 		final String throwsString= " throws "; //$NON-NLS-1$
 		StringBuffer buff= new StringBuffer(throwsString);
 		for (Iterator iter= fExceptionInfos.iterator(); iter.hasNext(); ) {
 			ExceptionInfo info= (ExceptionInfo) iter.next();
 			if (! info.isDeleted()) {
 				buff.append(info.getElement().getElementName());
 				buff.append(", "); //$NON-NLS-1$
 			}
 		}
 		if (buff.length() == throwsString.length())
 			return ""; //$NON-NLS-1$
 		buff.delete(buff.length() - 2, buff.length());
 		return buff.toString();
 	}
 
 	private String getOldMethodThrows() {
 		final String throwsString= " throws "; //$NON-NLS-1$
 		StringBuffer buff= new StringBuffer(throwsString);
 		for (Iterator iter= fExceptionInfos.iterator(); iter.hasNext(); ) {
 			ExceptionInfo info= (ExceptionInfo) iter.next();
 			if (! info.isAdded()) {
 				buff.append(info.getElement().getElementName());
 				buff.append(", "); //$NON-NLS-1$
 			}
 		}
 		if (buff.length() == throwsString.length())
 			return ""; //$NON-NLS-1$
 		buff.delete(buff.length() - 2, buff.length());
 		return buff.toString();
 	}*/
 
 	private void checkForDuplicateParameterNames(RefactoringStatus result){
 		Set<String> found= new HashSet<String>();
 		Set<String> doubled= new HashSet<String>();
 		for (ParameterInfo info : getNotDeletedInfos()) {
 			String newName= info.getNewName();
 			if (found.contains(newName) && !doubled.contains(newName)){
 				result.addFatalError(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_duplicate_name,
 						newName/*BasicElementLabels.getJavaElementName(newName)*/));
 				doubled.add(newName);
 			} else {
 				found.add(newName);
 			}
 		}
 	}
 
 	private ISourceModule getCu() {
 		return fMethod.getSourceModule();
 	}
 
 	/*private boolean mustAnalyzeAstOfDeclaringCu() throws JavaModelException{
 		if (JdtFlags.isAbstract(getMethod()))
 			return false;
 		else if (JdtFlags.isNative(getMethod()))
 			return false;
 		else if (getMethod().getDeclaringType().isInterface())
 			return false;
 		else
 			return true;
 	}
 
 	private RefactoringStatus checkCompilationofDeclaringCu() throws CoreException {
 		ICompilationUnit cu= getCu();
 		TextChange change= fChangeManager.get(cu);
 		String newCuSource= change.getPreviewContent(new NullProgressMonitor());
 		CompilationUnit newCUNode= new RefactoringASTParser(AST.JLS3).parse(newCuSource, cu, true, false, null);
 		IProblem[] problems= RefactoringAnalyzeUtil.getIntroducedCompileProblems(newCUNode, fBaseCuRewrite.getRoot());
 		RefactoringStatus result= new RefactoringStatus();
 		for (int i= 0; i < problems.length; i++) {
 			IProblem problem= problems[i];
 			if (shouldReport(problem, newCUNode))
 				result.addEntry(new RefactoringStatusEntry((problem.isError() ? RefactoringStatus.ERROR : RefactoringStatus.WARNING), problem.getMessage(), new JavaStringStatusContext(newCuSource, SourceRangeFactory.create(problem))));
 		}
 		return result;
 	}*/
 
 	/**
 	 * Evaluates if a problem needs to be reported.
 	 * @param problem the problem
 	 * @param cu the AST containing the new source
 	 * @return return <code>true</code> if the problem needs to be reported
 	 */
 	/*protected boolean shouldReport(IProblem problem, CompilationUnit cu) {
 		if (! problem.isError())
 			return false;
 		if (problem.getID() == IProblem.UndefinedType) //reported when trying to import
 			return false;
 		return true;
 	}*/
 
 	private String getOldMethodParameters() {
 		StringBuffer buff= new StringBuffer();
 		boolean first = true;
 		for (Object element : getNotAddedInfos()) {
 			ParameterInfo info= (ParameterInfo) element;
 			if (!first)
 				buff.append(", ");  //$NON-NLS-1$
 			buff.append(createDeclarationString(info));
 			first = false;
 		}
 		return buff.toString();
 	}
 
 	private String getMethodParameters() {
 		StringBuffer buff= new StringBuffer();
 		boolean first = true;
 		for (ParameterInfo info : getNotDeletedInfos()) {
 			if (!first)
 				buff.append(", ");  //$NON-NLS-1$
 			buff.append(createDeclarationString(info));
 			first = false;
 		}
 		return buff.toString();
 	}
 
 	private List<ParameterInfo> getAddedInfos(){
 		List<ParameterInfo> result= new ArrayList<ParameterInfo>(1);
 		for (ParameterInfo info : fParameterInfos) {
 			if (info.isAdded())
 				result.add(info);
 		}
 		return result;
 	}
 
 	private List<ParameterInfo> getDeletedInfos(){
 		List<ParameterInfo> result= new ArrayList<ParameterInfo>(1);
 		for (ParameterInfo info : fParameterInfos) {
 			if (info.isDeleted())
 				result.add(info);
 		}
 		return result;
 	}
 
 	private List<ParameterInfo> getNotAddedInfos(){
 		List<ParameterInfo> all= new ArrayList<ParameterInfo>(fParameterInfos);
 		all.removeAll(getAddedInfos());
 		return all;
 	}
 
 	private List<ParameterInfo> getNotDeletedInfos(){
 		List<ParameterInfo> all= new ArrayList<ParameterInfo>(fParameterInfos);
 		all.removeAll(getDeletedInfos());
 		return all;
 	}
 
 	private boolean areNamesSameAsInitial() {
 		for (ParameterInfo info : fParameterInfos) {
 			if (info.isRenamed())
 				return false;
 		}
 		return true;
 	}
 
 	private boolean isOrderSameAsInitial(){
 		int i= 0;
 		for (ParameterInfo info : fParameterInfos) {
 			if (info.isDeleted() || info.getOldIndex() != i)
 				return false;
 			i++;
 		}
 		return true;
 	}
 
 	/*private RefactoringStatus checkReorderings(IProgressMonitor pm) throws JavaModelException {
 		try{
 			pm.beginTask(RefactoringCoreMessages.ChangeSignatureRefactoring_checking_preconditions, 1);
 			return checkNativeMethods();
 		} finally{
 			pm.done();
 		}
 	}*/
 
 	/*private RefactoringStatus checkRenamings(IProgressMonitor pm) throws ModelException {
 		try{
 			pm.beginTask(RefactoringCoreMessages.ChangeSignatureRefactoring_checking_preconditions, 1);
 			return checkParameterNamesInRippleMethods();
 		} finally{
 			pm.done();
 		}
 	}
 
 	private RefactoringStatus checkParameterNamesInRippleMethods() throws ModelException {
 		RefactoringStatus result= new RefactoringStatus();
 		Set newParameterNames= getNewParameterNamesList();
 		for (int i= 0; i < fRippleMethods.length; i++) {
 			String[] paramNames= fRippleMethods[i].getParameterNames();
 			for (int j= 0; j < paramNames.length; j++) {
 				if (newParameterNames.contains(paramNames[j])){
 					String[] args= new String[]{ JavaElementUtil.createMethodSignature(fRippleMethods[i]), BasicElementLabels.getJavaElementName(paramNames[j])};
 					String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_already_has, args);
 					RefactoringStatusContext context= JavaStatusContext.create(fRippleMethods[i].getCompilationUnit(), fRippleMethods[i].getNameRange());
 					result.addError(msg, context);
 				}
 			}
 		}
 		return result;
 	}
 
 	private Set getNewParameterNamesList() {
 		Set oldNames= getOriginalParameterNames();
 		Set currentNames= getNamesOfNotDeletedParameters();
 		currentNames.removeAll(oldNames);
 		return currentNames;
 	}
 
 	private Set getNamesOfNotDeletedParameters() {
 		Set result= new HashSet();
 		for (Iterator iter= getNotDeletedInfos().iterator(); iter.hasNext();) {
 			ParameterInfo info= (ParameterInfo) iter.next();
 			result.add(info.getNewName());
 		}
 		return result;
 	}
 
 	private Set getOriginalParameterNames() {
 		Set result= new HashSet();
 		for (Iterator iter= fParameterInfos.iterator(); iter.hasNext();) {
 			ParameterInfo info= (ParameterInfo) iter.next();
 			if (! info.isAdded())
 				result.add(info.getOldName());
 		}
 		return result;
 	}
 
 	private RefactoringStatus checkNativeMethods() throws JavaModelException{
 		RefactoringStatus result= new RefactoringStatus();
 		for (int i= 0; i < fRippleMethods.length; i++) {
 			if (JdtFlags.isNative(fRippleMethods[i])){
 				String message= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_native,
 					new String[]{JavaElementUtil.createMethodSignature(fRippleMethods[i]), BasicElementLabels.getJavaElementName(fRippleMethods[i].getDeclaringType().getFullyQualifiedName('.'))});
 				result.addError(message, JavaStatusContext.create(fRippleMethods[i]));
 			}
 		}
 		return result;
 	}*/
 
 	private IFile[] getAllFilesToModify(){
 		return ResourceUtil.getFiles(fChangeManager.getAllSourceModules());
 	}
 
 	public Change[] getAllChanges() {
 		return fChangeManager.getAllChanges();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ltk.core.refactoring.Refactoring#createChange(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public Change createChange(IProgressMonitor pm) {
 		pm.beginTask("", 1); //$NON-NLS-1$
 		try {
 			return new DynamicValidationRefactoringChange(createDescriptor(), doGetRefactoringChangeName(), getAllChanges());
 		} finally {
 			clearManagers();
 			pm.done();
 		}
 	}
 
 	/*private ChangeMethodSignatureArguments getParticipantArguments() {
 		ArrayList parameterList= new ArrayList();
 		List pis= getParameterInfos();
 		String[] originalParameterTypeSigs= fMethod.getParameterTypes();
 
 		for (Iterator iter= pis.iterator(); iter.hasNext();) {
 			ParameterInfo pi= (ParameterInfo) iter.next();
 			if (!pi.isDeleted()) {
 				int oldIndex= pi.isAdded() ? -1 : pi.getOldIndex();
 				String newName= pi.getNewName();
 				String typeSig;
 				if (pi.isTypeNameChanged()) {
 					String newType= pi.getNewTypeName();
 					if (pi.isNewVarargs()) {
 						newType= ParameterInfo.stripEllipsis(newType) + "[]"; //$NON-NLS-1$
 					}
 					typeSig= Signature.createTypeSignature(newType, false);
 				} else {
 					typeSig= originalParameterTypeSigs[pi.getOldIndex()];
 				}
 				String defaultValue= pi.getDefaultValue();
 				parameterList.add(new Parameter(oldIndex, newName, typeSig, defaultValue));
 			}
 		}
 		Parameter[] parameters= (Parameter[]) parameterList.toArray(new Parameter[parameterList.size()]);
 
 		ArrayList exceptionList= new ArrayList();
 		List exceptionInfos= getExceptionInfos();
 		for (int i= 0; i < exceptionInfos.size(); i++) {
 			ExceptionInfo ei= (ExceptionInfo) exceptionInfos.get(i);
 			if (!ei.isDeleted()) {
 				int oldIndex= ei.isAdded() ? -1 : i;
 				String qualifiedTypeName= ei.getFullyQualifiedName();
 				String newTypeSig= Signature.createTypeSignature(qualifiedTypeName, true);
 				exceptionList.add(new ThrownException(oldIndex, newTypeSig));
 			}
 		}
 		ThrownException[] exceptions= (ThrownException[]) exceptionList.toArray(new ThrownException[exceptionList.size()]);
 		String returnTypeSig;
 		if (fReturnTypeInfo.isTypeNameChanged()) {
 			returnTypeSig= Signature.createTypeSignature(fReturnTypeInfo.getNewTypeName(), false);
 		} else {
 			try {
 				returnTypeSig= fMethod.getReturnType();
 			} catch (JavaModelException e) {
 				returnTypeSig= Signature.createTypeSignature(fReturnTypeInfo.getNewTypeName(), false);
 			}
 		}
 		return new ChangeMethodSignatureArguments(fMethodName, returnTypeSig, fVisibility, parameters, exceptions, fDelegateUpdating);
 	}*/
 
 
 	public ScriptRefactoringDescriptor createDescriptor() {
 		final Map arguments= new HashMap();
 		String project= null;
 		IScriptProject scriptProject= fMethod.getScriptProject();
 		if (scriptProject != null)
 			project= scriptProject.getElementName();
 		ChangeMethodSignatureDescriptor descriptor= null;
 		//try {
 			final String description= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_descriptor_description_short,
 					fMethod.getElementName()/*BasicElementLabels.getJavaElementName(fMethod.getElementName())*/);
 			//final String header= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_descriptor_description, new String[] { getOldMethodSignature(), getNewMethodSignature()});
 			//final JDTRefactoringDescriptorComment comment= createComment(project, header);
 			descriptor= new ChangeMethodSignatureDescriptor(project, description, ""/*comment.asString()*/, arguments, getDescriptorFlags());
 			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT, ScriptRefactoringDescriptor.elementToHandle(project,fMethod));
 			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, fMethodName);
 			/*arguments.put(ATTRIBUTE_DELEGATE, Boolean.valueOf(fDelegateUpdating).toString());
 			arguments.put(ATTRIBUTE_DEPRECATE, Boolean.valueOf(fDelegateDeprecation).toString());
 			if (fReturnTypeInfo.isTypeNameChanged())
 				arguments.put(ATTRIBUTE_RETURN, fReturnTypeInfo.getNewTypeName());
 			try {
 				if (!isVisibilitySameAsInitial())
 					arguments.put(ATTRIBUTE_VISIBILITY, new Integer(fVisibility).toString());
 			} catch (JavaModelException exception) {
 				JavaPlugin.log(exception);
 			}*/
 			int count= 1;
 			for (ParameterInfo info : fParameterInfos) {
 				final StringBuffer buffer= new StringBuffer(64);
 				if (info.isAdded())
 					buffer.append("{added}"); //$NON-NLS-1$
 				else
 					buffer.append(info.getOldTypeName());
 				buffer.append(" "); //$NON-NLS-1$
 				if (info.isAdded())
 					buffer.append("{added}"); //$NON-NLS-1$
 				else
 					buffer.append(info.getOldName());
 				buffer.append(" "); //$NON-NLS-1$
 				buffer.append(info.getOldIndex());
 				buffer.append(" "); //$NON-NLS-1$
 				if (info.isDeleted())
 					buffer.append("{deleted}"); //$NON-NLS-1$
 				else
 					buffer.append(info.getNewTypeName().replaceAll(" ", ""));  //$NON-NLS-1$//$NON-NLS-2$
 				buffer.append(" "); //$NON-NLS-1$
 				if (info.isDeleted())
 					buffer.append("{deleted}"); //$NON-NLS-1$
 				else
 					buffer.append(info.getNewName());
 				buffer.append(" "); //$NON-NLS-1$
 				buffer.append(info.isDeleted());
 				arguments.put(ATTRIBUTE_PARAMETER + count, buffer.toString());
 				final String value= info.getDefaultValue();
 				if (value != null && !"".equals(value)) //$NON-NLS-1$
 					arguments.put(ATTRIBUTE_DEFAULT + count, value);
 				count++;
 			}
 			/*count= 1;
 			for (final Iterator iterator= fExceptionInfos.iterator(); iterator.hasNext();) {
 				final ExceptionInfo info= (ExceptionInfo) iterator.next();
 				arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + count, JavaRefactoringDescriptorUtil.elementToHandle(project,info.getElement()));
 				arguments.put(ATTRIBUTE_KIND + count, new Integer(info.getKind()).toString());
 				count++;
 			}
 		} catch (ModelException exception) {
 			JavascriptManipulationPlugin.log(exception);
 			return null;
 		}*/
 		return descriptor;
 	}
 
 	protected int getDescriptorFlags() {
 		/*int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE;
 		try {
 			if (!Flags.isPrivate(fMethod.getFlags()))
 				flags|= RefactoringDescriptor.MULTI_CHANGE;
 			final IType declaring= fMethod.getDeclaringType();
 			if (declaring.isAnonymous() || declaring.isLocal())
 				flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
 		} catch (JavaModelException exception) {
 			JavaPlugin.log(exception);
 		}
 		return flags;*/
 		return ScriptRefactoringDescriptor.ARCHIVE_IMPORTABLE | ScriptRefactoringDescriptor.ARCHIVE_REFACTORABLE
 			| RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
 	}
 
 	/*private JDTRefactoringDescriptorComment createComment(String project, final String header) throws JavaModelException {
 		final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(project, this, header);
 		if (!fMethod.getElementName().equals(fMethodName))
 			comment.addSetting(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_new_name_pattern, BasicElementLabels.getJavaElementName(fMethodName)));
 		if (!isVisibilitySameAsInitial()) {
 			String visibility= JdtFlags.getVisibilityString(fVisibility);
 			if ("".equals(visibility)) //$NON-NLS-1$
 				visibility= RefactoringCoreMessages.ChangeSignatureRefactoring_default_visibility;
 			comment.addSetting(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_new_visibility_pattern, visibility));
 		}
 		if (fReturnTypeInfo.isTypeNameChanged())
 			comment.addSetting(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_new_return_type_pattern, BasicElementLabels.getJavaElementName(fReturnTypeInfo.getNewTypeName())));
 		List deleted= new ArrayList();
 		List added= new ArrayList();
 		List changed= new ArrayList();
 		for (final Iterator iterator= fParameterInfos.iterator(); iterator.hasNext();) {
 			final ParameterInfo info= (ParameterInfo) iterator.next();
 			if (info.isDeleted())
 				deleted.add(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_deleted_parameter_pattern, new String[] { BasicElementLabels.getJavaElementName(info.getOldTypeName()), BasicElementLabels.getJavaElementName(info.getOldName())}));
 			else if (info.isAdded())
 				added.add(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_added_parameter_pattern, new String[] { BasicElementLabels.getJavaElementName(info.getNewTypeName()), BasicElementLabels.getJavaElementName(info.getNewName())}));
 			else if (info.isRenamed() || info.isTypeNameChanged() || info.isVarargChanged())
 				changed.add(Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_changed_parameter_pattern, new String[] { BasicElementLabels.getJavaElementName(info.getOldTypeName()), BasicElementLabels.getJavaElementName(info.getOldName())}));
 		}
 		if (!added.isEmpty())
 			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.ChangeSignatureRefactoring_added_parameters, (String[]) added.toArray(new String[added.size()])));
 		if (!deleted.isEmpty())
 			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.ChangeSignatureRefactoring_removed_parameters, (String[]) deleted.toArray(new String[deleted.size()])));
 		if (!changed.isEmpty())
 			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.ChangeSignatureRefactoring_changed_parameters, (String[]) changed.toArray(new String[changed.size()])));
 		added.clear();
 		deleted.clear();
 		changed.clear();
 		for (final Iterator iterator= fExceptionInfos.iterator(); iterator.hasNext();) {
 			final ExceptionInfo info= (ExceptionInfo) iterator.next();
 			if (info.isAdded())
 				added.add(info.getElement().getElementName());
 			else if (info.isDeleted())
 				deleted.add(info.getElement().getElementName());
 		}
 		if (!added.isEmpty())
 			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.ChangeSignatureRefactoring_added_exceptions, (String[]) added.toArray(new String[added.size()])));
 		if (!deleted.isEmpty())
 			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.ChangeSignatureRefactoring_removed_exceptions, (String[]) deleted.toArray(new String[deleted.size()])));
 		return comment;
 	}*/
 
 	protected String doGetRefactoringChangeName() {
 		return RefactoringCoreMessages.ChangeSignatureRefactoring_restructure_parameters;
 	}
 
 	private TextChangeManager createChangeManager(IProgressMonitor pm, RefactoringStatus result) throws CoreException {
 		pm.beginTask(RefactoringCoreMessages.ChangeSignatureRefactoring_preview, 1);
 		fChangeManager= new TextChangeManager();
 		/*boolean isNoArgConstructor= isNoArgConstructor();
 		Map namedSubclassMapping= null;
 		if (isNoArgConstructor){
 			//create only when needed;
 			namedSubclassMapping= createNamedSubclassMapping(new SubProgressMonitor(pm, 1));
 		}else{
 			pm.worked(1);
 		}*/
 		boolean decl = false;
 		for (int i= 0; i < fReferences.length; i++) {
 			SearchResultGroup group= fReferences[i];
 			ISourceModule cu= group.getSourceModule();
 			if (pm.isCanceled())
 				throw new OperationCanceledException();
 			processCu(cu,group.getSearchResults(),result);
 			if (cu.equals(fMethod.getSourceModule()))
 				decl = true;
 		}
 		if (!decl) {
 			if (pm.isCanceled())
 				throw new OperationCanceledException();
 			processCu(fMethod.getSourceModule(),new SearchMatch[0],result);
 		}
 		pm.done();
 		return fChangeManager;
 	}
 	private void processCu(ISourceModule cu, SearchMatch[] searchResults, RefactoringStatus result)
 			throws ModelException {
 		if (cu == null)
 			return;
 		/*SourceModuleRewrite cuRewrite;
 		if (cu.equals(getCu())) {
 			cuRewrite= fBaseCuRewrite;
 		} else {
 			cuRewrite= new SourceModuleRewrite(cu);
 			cuRewrite.getASTRewrite().setTargetSourceRangeComputer(new TightSourceRangeComputer());
 		}*/
 		Source root = (Source)ASTConverter.convert(JavaScriptParserUtil.parse(cu));
 		Node[] nodes= NodeFinder.findNodes(root, searchResults);
 		ChangeRecorder cr = new ChangeRecorder(root);
 		for(Node node : nodes) {
 			CallExpression call = getFunctionReference(node);
 			if (call != null) {
 				Identifier id = (node instanceof Identifier) ? (Identifier)node : ((VariableReference)node).getVariable();
 				id.setName(fMethodName);
 				if (call.getArguments().size() != fMethod.getParameters().length)
 					result.addWarning(RefactoringCoreMessages.ChangeSignatureRefactoring_different_num_of_args,
 							ScriptStatusContext.create(cu, new SourceRange(node.getBegin(), node.getEnd()-node.getBegin())));
 				reshuffleElements(call.getArguments(),new NewElementsProvider<Expression>(){
 					@Override
 					Expression createElement(ParameterInfo info) {
 						// XXX Dirty hack indeed
 						VariableReference ref = DomFactory.eINSTANCE.createVariableReference(); 
 						Identifier id = DomFactory.eINSTANCE.createIdentifier();
 						if (info.getDefaultValue() == null) {
 							id.setName(ParameterInfo.DEFAULT_VALUE);
 						} else
 							id.setName(info.getDefaultValue());
 						ref.setVariable(id);
 						return ref;
 					}
 				});
 			} else {
 				result.addError(RefactoringCoreMessages.ChangeSignatureRefactoring_unknown_reference,
 						ScriptStatusContext.create(cu, new SourceRange(node.getBegin(), node.getEnd()-node.getBegin())));
 				tryRename(node);
 			}
 		}
 		if (cu.equals(fMethod.getSourceModule())) {
 			Node node = NodeFinder.findNode(root, fMethod.getNameRange());
 			FunctionExpression expr = node == null ? null : getFunctionDeclaration(node);
 			if (expr != null) {
 				((Identifier)node).setName(fMethodName);
 				reshuffleElements(expr.getParameters(),new NewElementsProvider<Parameter>(){
 					@Override
 					Parameter createElement(ParameterInfo info) {
 						Parameter param = DomFactory.eINSTANCE.createParameter();
 						Identifier id = DomFactory.eINSTANCE.createIdentifier();
 						id.setName(info.getNewName());
 						param.setName(id);
 						return param;
 					}
 				});
 				modifyElements(expr.getParameters());
 				result.merge(checkIfDeletedParametersUsed(expr.getBody(),cu));
 				updateBody(expr.getBody());
 			} else {
 				result.addError(RefactoringCoreMessages.ChangeSignatureRefactoring_unknown_reference,
 						ScriptStatusContext.create(cu, new SourceRange(node.getBegin(), node.getEnd()-node.getBegin())));
 				tryRename(node);
 			}
 		}
 		ChangeDescription cd = cr.endRecording();
 		RewriteAnalyzer rewrite = new RewriteAnalyzer(cd, cu.getSource());
 		rewrite.rewrite(root);
 		cd.apply();
 		TextChange change = new SourceModuleChange(cu.getElementName(), cu);
 		change.setEdit(rewrite.getEdit());
 		fChangeManager.manage(cu, change);
 	}
 	private RefactoringStatus checkIfDeletedParametersUsed(Node node, ISourceModule cu) {
 		Set<String> deleted = new HashSet<String>();
 		for (ParameterInfo info : getDeletedInfos()) {
 			if (info.isDeleted())
 				deleted.add(info.getOldName());
 		}
 		List<Identifier> refs = VariableLookup.findReferences(node,deleted,true);
 		RefactoringStatus status = new RefactoringStatus();
 		for(Identifier ref : refs) {
 			SourceRange range = new SourceRange(ref.getBegin(), ref.getEnd()-ref.getBegin());
 			RefactoringStatusContext context= ScriptStatusContext.create(cu, range);
 			String[] keys= new String[]{ ref.getName(), fMethodName };
 			String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_parameter_used, keys);
 			status.addWarning(msg, context);
 		}
 		return status;
 	}
 	
 	private void updateBody(Node node) {
 		Map<String,String> renaming = new HashMap<String,String>();
 		for (ParameterInfo info : getNotDeletedInfos()) {
 			if (!info.isDeleted())
 				renaming.put(info.getOldName(),info.getNewName());
 		}
 		List<Identifier> refs = VariableLookup.findReferences(node,renaming.keySet());
 		for(Identifier ref : refs) {
 			ref.setName(renaming.get(ref.getName()));
 		}
 	}
 	
 	private static abstract class NewElementsProvider<T> {
 		abstract T createElement(ParameterInfo info);
 	}
 	
 	private final <T extends Node> void reshuffleElements(EList<T> original, NewElementsProvider<T> provider) {
 		List<T> dst = new ArrayList<T>();
 		Set<T> deleted = new HashSet<T>();
 		{
 			for(ParameterInfo info : fParameterInfos) {
 				if (info.isDeleted())
 					deleted.add(original.get(info.getOldIndex()));
 				else {
 					if (info.isAdded())
 						dst.add(provider.createElement(info));
 					else
 						dst.add(original.get(info.getOldIndex()));
 				}
 			}
 		}
 		for(int i=0;i<dst.size()+1;i++) {
 			while (i < original.size() && deleted.contains(original.get(i)))
 				original.remove(i);
 			if (i == dst.size())
 				break;
 			T node = dst.get(i);
 			if (i < original.size() && original.get(i) == node)
 				continue;
 			if (original.contains(node))
 				original.move(i, node);
 			else
 				original.add(i, dst.get(i));
 		}
 	}
 	
 	private final <T extends Node> void modifyElements(EList<Parameter> original) {
 		int i=0;
 		for(ParameterInfo info : fParameterInfos) {
 			if (info.isDeleted()) break;
 			if (!info.isAdded() && info.isRenamed())
 				original.get(i).getName().setName(info.getNewName());
 			// TODO check for types
 			i++;
 		}
 	}
 	
 	private IDLTKSearchScope createRefactoringScope()  throws ModelException {
 		return RefactoringScopeFactory.create(fMethod, true, false);
 	}
 
 	private SearchResultGroup[] findReferences(IProgressMonitor pm, RefactoringStatus status) throws CoreException {
 		//final boolean isConstructor= fMethod.isConstructor();
 		/*CuCollectingSearchRequestor requestor= new CuCollectingSearchRequestor(binaryRefs) {
 			protected void acceptSearchMatch(ISourceModule unit, SearchMatch match) throws CoreException {
 				// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=27236 :
 				if (isConstructor && match instanceof MethodReferenceMatch) {
 					MethodReferenceMatch mrm= (MethodReferenceMatch) match;
 					if (mrm.isSynthetic()) {
 						return;
 					}
 				}
 				collectMatch(match);
 			}
 		};*/
 		CollectingSearchRequestor requestor = new CollectingSearchRequestor(true);
 
 		/*SearchPattern pattern;
 		if (isConstructor) {
 
 //			// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=226151 : don't find binary refs for constructors for now
 //			return ConstructorReferenceFinder.getConstructorOccurrences(fMethod, pm, status);
 
 //			SearchPattern occPattern= SearchPattern.createPattern(fMethod, IJavaSearchConstants.ALL_OCCURRENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
 			SearchPattern declPattern= SearchPattern.createPattern(fMethod, IJavaSearchConstants.DECLARATIONS, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
 			SearchPattern refPattern= SearchPattern.createPattern(fMethod, IJavaSearchConstants.REFERENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
 //			pattern= SearchPattern.createOrPattern(declPattern, refPattern);
 //			pattern= occPattern;
 
 			// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=226151 : do two searches
 			try {
 				SearchEngine engine= new SearchEngine();
 				engine.search(declPattern, SearchUtils.getDefaultSearchParticipants(), createRefactoringScope(), requestor, new NullProgressMonitor());
 				engine.search(refPattern, SearchUtils.getDefaultSearchParticipants(), createRefactoringScope(), requestor, pm);
 			} catch (CoreException e) {
 				throw new JavaModelException(e);
 			}
 			return RefactoringSearchEngine.groupByCu(requestor.getResults(), status);
 
 		} else {
 			pattern= RefactoringSearchEngine.createOrPattern(fRippleMethods, IJavaSearchConstants.ALL_OCCURRENCES);
 		}*/
 		SearchPattern pattern = SearchPattern.createPattern(fMethod, IDLTKSearchConstants.REFERENCES);
 		return RefactoringSearchEngine.search(pattern, createRefactoringScope(), requestor, pm, status);
 	}
 
 	private static String createDeclarationString(ParameterInfo info) {
 		return info.getNewName() + ("".equals(info.getNewTypeName()) ? "" : ":" + info.getNewTypeName()); //$NON-NL0S-1$
 	}
 
 	private static CallExpression getFunctionReference(Node node){
 		if (node.eContainingFeature() == DomPackage.eINSTANCE.getCallExpression_Applicant())
 			return (CallExpression)node.eContainer();
 		if (node.eContainingFeature() == DomPackage.eINSTANCE.getPropertyAccessExpression_Property())
 			return getFunctionReference((Node)node.eContainer());
 		return null;
 	}
 	
 	private static FunctionExpression getFunctionDeclaration(Node node) {
 		if (node.eContainingFeature() == DomPackage.eINSTANCE.getFunctionExpression_Identifier())
 			return (FunctionExpression)node.eContainer();
 		if (node.eContainingFeature() == DomPackage.eINSTANCE.getPropertyAssignment_Name()) {
 			Node parent = (Node)node.eContainer();
 			if (parent instanceof SimplePropertyAssignment) {
 				Node func = ((SimplePropertyAssignment)parent).getInitializer();
 				if (func instanceof FunctionExpression)
 					return (FunctionExpression)func;
 			}
 		}
		if (node.eContainingFeature() == DomPackage.eINSTANCE.getVariableDeclaration_Identifier()) {
			Expression initializer = ((VariableDeclaration)node.eContainer()).getInitializer();
			if (initializer instanceof FunctionExpression)
				return (FunctionExpression)initializer;
		}
 		return null;
 	}
 	
 	private void tryRename(Node node) {
 		if (node instanceof Identifier)
 			((Identifier) node).setName(fMethodName);
 		if (node instanceof VariableReference)
 			((VariableReference) node).getVariable().setName(fMethodName);
 	}
 
 	public Object[] getElements() {
 		return new Object[] { fMethod };
 	}
 
 	public String getIdentifier() {
 		return "org.eclipse.dltk.javascript.ui.changeMethodSignatureRefactoring"; //$NON-NLS-1$;
 	}
 
 	public boolean isApplicable() throws CoreException {
 		return Checks.isAvailable(fMethod);
 		//return RefactoringAvailabilityTester.isChangeSignatureAvailable(fMethod);
 	}
 
 	/*public final RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants shared) throws CoreException {
 		return getRenameModifications().loadParticipants(status, this, getAffectedProjectNatures(), shared);
 	}*/
 
 	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
 		//String[] affectedNatures= ScriptProcessors.computeAffectedNatures(fMethod);
 		return new RefactoringParticipant[0];
 		//return JavaParticipantManager.loadChangeMethodSignatureParticipants(status, this, fMethod, getParticipantArguments(), null, affectedNatures, sharedParticipants);
 	}
 }
