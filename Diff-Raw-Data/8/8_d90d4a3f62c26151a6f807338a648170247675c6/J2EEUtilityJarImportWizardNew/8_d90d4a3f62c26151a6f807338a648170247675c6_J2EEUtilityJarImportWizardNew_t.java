 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import org.eclipse.jst.j2ee.application.internal.operations.J2EEUtilityJarListImportDataModelProvider;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
 import org.eclipse.wst.project.facet.IProductConstants;
 import org.eclipse.wst.project.facet.ProductManager;
 
 
 /**
  * <p>
  * Used to import Utility Jars into several Eclipse workbench configurations. These can be extracted
  * as editable projects, binary projects, linked resources in the EAR module or actual resources in
  * the EAR module.
  * </p>
  */
 public final class J2EEUtilityJarImportWizardNew extends J2EEArtifactImportWizard implements IImportWizard {
 
 	/**
 	 * <p>
 	 * Constant used to identify the key of the page of the Wizard which allows users to define the
 	 * type of import they would like to carry out.
 	 * </p>
 	 */
 	private static final String IMPORT_TYPE = "IMPORT_TYPE"; //$NON-NLS-1$
 
 	/**
 	 * <p>
 	 * Constant used to identify the key of the page of the Wizard that allows users to select jar
 	 * files for import
 	 * </p>
 	 */
 	private static final String SELECT_JARS = "SELECT_JARS"; //$NON-NLS-1$
 
 
 	/**
 	 * <p>
 	 * The default constructor. Creates a wizard with no selection, no model instance, and no
 	 * operation instance. The model and operation will be created as needed.
 	 * </p>
 	 */
 	public J2EEUtilityJarImportWizardNew() {
 		super();
 	}
 
 	/**
 	 * <p>
 	 * The model is used to prepopulate the wizard controls and interface with the operation.
 	 * </p>
 	 * 
 	 * @param model
 	 *            The model parameter is used to pre-populate wizard controls and interface with the
 	 *            operation
 	 */
 	public J2EEUtilityJarImportWizardNew(IDataModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Adds the following pages:
 	 * <ul>
 	 * <li>{@link J2EEUtilityJarImportTypePageNew}as the main wizard page ({@link #IMPORT_TYPE})
 	 * <li>{@link J2EEUtilityJarImportPageNew}as the main wizard page ({@link #SELECT_JARS})
 	 * </ul>
 	 * </p>
 	 */
 	public void doAddPages() {
		this.addPage(new J2EEUtilityJarImportTypePageNew(getDataModel(), IMPORT_TYPE, getSelection()));
 		this.addPage(new J2EEUtilityJarImportPageNew(getDataModel(), SELECT_JARS));
 	}
 
 	protected IDataModelProvider getDefaultProvider() {
 		return new J2EEUtilityJarListImportDataModelProvider();
 	}
 	
 	protected String getFinalPerspectiveID() {
         return ProductManager.getProperty(IProductConstants.FINAL_PERSPECTIVE_UTILITY);
 	}
 
 }
