 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.steps;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
 import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
 import org.eclipse.ltk.core.refactoring.Refactoring;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
 import edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass.CreateNewTopLevelSuperClassDescriptor;
 
 @SuppressWarnings("restriction")
 public class CreateNewSuperclassCommandHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		// See http://wiki.bioclipse.net/index.php?title=How_to_add_menus_and_actions&oldid=4857
 		ISelection selection= HandlerUtil.getCurrentSelection(event);
 		try {
 			List<IType> selectedTypes= getSelectedTypes((IStructuredSelection)selection);
 			IProgressMonitor monitor= new NullProgressMonitor();
 			CreateNewTopLevelSuperClassDescriptor descriptor= createRefactoringDescriptor(selectedTypes);
 			Refactoring refactoring= descriptor.createRefactoringContext(new RefactoringStatus()).getRefactoring();
 			RefactoringStatus status= new RefactoringStatus();
 			status.merge(refactoring.checkInitialConditions(monitor));
 			if (status.hasFatalError()) {
 				return null;
 			}
 			status.merge(refactoring.checkFinalConditions(monitor));
 			if (status.hasFatalError()) {
 				return null;
 			}
			PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(monitor);
 		} catch (JavaModelException e) {
 			throw new ExecutionException("Unexpected selection", e);
 		} catch (CoreException e) {
 			throw new ExecutionException("Refactoring object creation failure", e);
 		}
 		return null;
 	}
 
 	public static List<IType> getSelectedTypes(IStructuredSelection selection) throws JavaModelException {
 		List<IType> selectedTypes= new ArrayList<IType>();
 		for (Object selectionElement : selection.toList()) {
 			selectedTypes.add(extractTypeFromSelection(selectionElement));
 		}
 		return selectedTypes;
 	}
 
 	// See org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester.getSingleSelectedType(IStructuredSelection)
 	private static IType extractTypeFromSelection(Object selectionElement) throws JavaModelException {
 		if (selectionElement instanceof IType)
 			return (IType)selectionElement;
 		if (selectionElement instanceof ICompilationUnit) {
 			final ICompilationUnit unit= (ICompilationUnit)selectionElement;
 			if (unit.exists()) {
 				return JavaElementUtil.getMainType(unit);
 			}
 		}
 		return null;
 	}
 
 	private String typeNames(List<IType> types) {
 		StringBuilder sb= new StringBuilder();
 		for (Iterator<IType> iterator= types.iterator(); iterator.hasNext();) {
 			IType type= (IType)iterator.next();
 			sb.append(type.getElementName());
 			if (iterator.hasNext()) {
 				sb.append(",");
 			}
 		}
 		return sb.toString();
 	}
 
 	private CreateNewTopLevelSuperClassDescriptor createRefactoringDescriptor(List<IType> types) {
 		IJavaProject javaProject= types.get(0).getJavaProject();
 		String description= MessageFormat.format(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_description, typeNames(types));
 		Map<String, String> arguments= new HashMap<String, String>();
 		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(javaProject.getElementName(), types.get(0)));
 		org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil.setJavaElementArray(arguments, CreateNewTopLevelSuperClassDescriptor.ATTRIBUTE_TYPES,
 				JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT, javaProject.getElementName(), types.toArray(new IType[] {}), 1);
 		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, "Super" + types.get(0).getElementName());
 		CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor(javaProject.getElementName(), description, null, arguments,
 				RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
 		return descriptor;
 	}
 }
