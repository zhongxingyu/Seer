 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.refactorings.usesupertypeininstanceof;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
 import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;
 import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
 import org.eclipse.ltk.core.refactoring.RefactoringContribution;
 import org.eclipse.ltk.core.refactoring.RefactoringCore;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 
 /**
  * Refactoring descriptor for the use supertype in instanceof expressions refactoring.
  * <p>
  * An instance of this refactoring descriptor may be obtained by calling
  * {@link RefactoringContribution#createDescriptor()} on a refactoring contribution requested by
  * invoking {@link RefactoringCore#getRefactoringContribution(String)} with the appropriate
  * refactoring id.
  * </p>
  * <p>
  * Note: this class is not intended to be instantiated by clients.
  * </p>
  * 
 * @since 1.1
 * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  */
 public final class UseSupertypeInInstanceOfDescriptor extends JavaRefactoringDescriptor {
 
 	/**
 	 * This ID should match the of the refactoring contribution used in the corresponding extension.
 	 */
 	public static final String ID= "edu.illinois.compositerefactorings.usesupertypeininstanceof";
 
 	/** The subtype attribute */
 	private IType fSubType= null;
 
 	/** The supertype attribute */
 	private IType fSupertype= null;
 
 	/**
 	 * Creates a new refactoring descriptor.
 	 */
 	public UseSupertypeInInstanceOfDescriptor() {
 		super(ID);
 	}
 
 	/**
 	 * Creates a new refactoring descriptor.
 	 * 
 	 * @param project the non-empty name of the project associated with this refactoring, or
 	 *            <code>null</code> for a workspace refactoring
 	 * @param description a non-empty human-readable description of the particular refactoring
 	 *            instance
 	 * @param comment the human-readable comment of the particular refactoring instance, or
 	 *            <code>null</code> for no comment
 	 * @param arguments a map of arguments that will be persisted and describes all settings for
 	 *            this refactoring
 	 * @param flags the flags of the refactoring descriptor
 	 * 
 	 * @throws IllegalArgumentException if the argument map contains invalid keys/values
 	 * 
	 * @since 1.2
 	 */
	public UseSupertypeInInstanceOfDescriptor(String project, String description, String comment, Map arguments, int flags) {
 		super(ID, project, description, comment, arguments, flags);
 		fSubType= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
 		fSupertype= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, JavaRefactoringDescriptorUtil.getAttributeName(ATTRIBUTE_ELEMENT, 1), project);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void populateArgumentMap() {
 		super.populateArgumentMap();
 		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, getProject(), fSubType);
 		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, JavaRefactoringDescriptorUtil.getAttributeName(ATTRIBUTE_ELEMENT, 1), getProject(), fSupertype);
 	}
 
 	/**
 	 * Sets the subtype of the refactoring.
 	 * <p>
 	 * Occurrences of the subtype are replaced by the supertype set by {@link #setSupertype(IType)}
 	 * where possible.
 	 * </p>
 	 * 
 	 * @param type the subtype to set
 	 */
 	public void setSubtype(final IType type) {
 		Assert.isNotNull(type);
 		fSubType= type;
 	}
 
 	/**
 	 * Sets the supertype of the refactoring.
 	 * <p>
 	 * Occurrences of the subtype set by {@link #setSubtype(IType)} are replaced by the supertype
 	 * where possible.
 	 * </p>
 	 * 
 	 * @param type the supertype to set
 	 */
 	public void setSupertype(final IType type) {
 		Assert.isNotNull(type);
 		fSupertype= type;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public RefactoringStatus validateDescriptor() {
 		RefactoringStatus status= super.validateDescriptor();
 		if (fSubType == null)
 			status.merge(RefactoringStatus.createFatalErrorStatus(DescriptorMessages.UseSupertypeDescriptor_no_subtype));
 		if (fSupertype == null)
 			status.merge(RefactoringStatus.createFatalErrorStatus(DescriptorMessages.UseSupertypeDescriptor_no_supertype));
 		return status;
 	}
 }
