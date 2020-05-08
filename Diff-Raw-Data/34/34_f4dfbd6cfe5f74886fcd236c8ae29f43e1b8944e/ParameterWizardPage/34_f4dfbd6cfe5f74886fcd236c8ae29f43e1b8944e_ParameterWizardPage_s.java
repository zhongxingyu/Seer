 /*******************************************************************************
  * Copyright (c) Philipps University of Marburg. All rights reserved. 
  * This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Philipps University of Marburg - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.refactor.refactoring.henshin.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.henshin.model.Module;
 import org.eclipse.emf.henshin.model.Parameter;
 import org.eclipse.emf.henshin.model.Unit;
 import org.eclipse.emf.refactor.refactoring.generator.core.ParameterInfo;
 import org.eclipse.emf.refactor.refactoring.henshin.generator.HenshinRefactoringInfo;
 import org.eclipse.emf.refactor.refactoring.henshin.managers.HenshinFileManager;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * WizardPage for selecting refactoring parameters out of those from the 
  * main transformation unit of the specified Henshin transformation file. 
  * @generated NOT
  * @author Thorsten Arendt
  */
 public class ParameterWizardPage extends WizardPage implements Listener {
 	
 	private Table parameterTable;	
 	
 	private static final String MAINUNIT = "mainUnit";
 	private static final String SELECTEDEOBJECT = "selectedEObject";
 	
 	/**
 	 * Name of the project the model refactoring code has to be 
 	 * generated to.
 	 */
 	private String projectName; 
 	
 	/**
 	 * Name of the Henshin file used for executing the EMF model
 	 * refactoring.
 	 */
 	private String transformationFileName;
 	
 	/**
 	 * Henshin Module loaded from the specified file name.
 	 */
 	private Module module;
 	
 	/**
 	 * List of all parameters existing in the main transformation unit 
 	 * of the specified Henshin transformation file. 
 	 */
 	private List<ParameterInfo> mainUnitParameters;
 	
 	/**
 	 * List of selected refactoring parameters out of those from the main 
 	 * transformation unit of the specified Henshin transformation file. 
 	 */
 	private List<ParameterInfo> selectedParameters;
 	
 	/**
 	 * Default constructor.
 	 * @param pageName Name of the wizard page.
 	 */
 	protected ParameterWizardPage(String pageName) {
 		super(pageName);
 		mainUnitParameters = new ArrayList<ParameterInfo>();
 		selectedParameters = new ArrayList<ParameterInfo>();
 	}
 	
 	/**
 	 * Gets the list of selected refactoring parameters out of those 
 	 * from the main transformation unit of the specified Henshin 
 	 * transformation file. 
 	 * @return List of selected refactoring parameters out of those from 
 	 * the main transformation unit of the specified Henshin 
 	 * transformation file. 
 	 */
 	public List<ParameterInfo> getSelectedParameters() {
 		return selectedParameters;
 	}
 	
 	/**
 	 * Sets the name of the Henshin file used for executing the EMF model
 	 * refactoring.
 	 * @param transformationFileName Name of the Henshin file used for 
 	 * executing the EMF model refactoring.
 	 */
 	public void setTransformationFileName(String transformationFileName) {
 		this.transformationFileName = transformationFileName;
 		this.mainUnitParameters.clear();
 		this.parameterTable.removeAll();
 		this.selectedParameters.clear();
 		setMainUnitParameters();
 		fillParameterTable();
 		checkIsPageComplete();
 	}
 	
 	/**
 	 * Gets the name of the Henshin file used for executing the EMF model
 	 * refactoring.
 	 * @return Name of the Henshin file used for executing the EMF model
 	 * refactoring.
 	 */
 	public String getTransformationFileName() {
 		return transformationFileName;
 	}
 
 	/**
 	 * Sets the name of the project the model refactoring code has to be 
 	 * generated to.
 	 * @param projectName Name of the project the model refactoring code 
 	 * has to be generated to.
 	 */
 	public void setProjectName(String projectName) {
 		this.projectName = projectName;
 	}
 
 	/**
 	 * Gets the name of the project the model refactoring code has to be 
 	 * generated to.
 	 * @return Name of the project the model refactoring code has to be 
 	 * generated to.
 	 */
 	public String getProjectName() {
 		return projectName;
 	}
 	
 	/**
 	 * Sets the list of all parameters existing in the main transformation 
 	 * unit of the specified Henshin transformation file. 
 	 */
 	private void setMainUnitParameters() {
 		setTransformationSystem();
 		if (null != this.module){
 			Unit mainUnit = 
 					this.module.getUnit(MAINUNIT);
 			if (mainUnit != null){
 				for (Parameter parameter : mainUnit.getParameters()){
 					if (parameter.getName().equals(SELECTEDEOBJECT)){
 						continue;
 					}
 					ParameterInfo pi = 
 								new ParameterInfo(parameter.getName());
 					pi.setDescription(parameter.getDescription());
 					this.mainUnitParameters.add(pi);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sets the Henshin TransformationSystem loaded from the specified 
 	 * file name.
 	 */
 	private void setTransformationSystem() {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot()
 										.getProject(this.projectName);
 		String path = project.getLocationURI().getPath() 
 						+ HenshinRefactoringInfo.TRAFODIR 
 						+ this.transformationFileName;
 		this.module = (Module) HenshinFileManager.loadFile(path);
 	}
 	
 	/**
 	 * Fills the table with parameter data from the main transformation 
 	 * unit of the specified Henshin transformation file.
 	 */
 	private void fillParameterTable() {
 		for (ParameterInfo pi : this.mainUnitParameters){
 			TableItem item = new TableItem (this.parameterTable, SWT.NONE);
 			item.setText (1, pi.getName());
 			item.setText (2, pi.getDescription());
 			item.setChecked(true);
 			selectedParameters.add(pi);
 		}
 	}
 	
 
 	/**
 	 * @see org.eclipse.jface.dialogs.IDialogPage#
 	 * createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		FillLayout fillLayout = new FillLayout();
 		fillLayout.spacing = 5;
 		fillLayout.type = SWT.VERTICAL;
 		composite.setLayout(fillLayout);
 		setMessage("Please select the refactoring parameters " +
 				"out of those from the main unit of the " +
 				"selected henshin transformation (execute).");
 		parameterTable = new Table(composite, 
 				SWT.CHECK  | SWT.SINGLE | SWT.FULL_SELECTION 
 				| SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		parameterTable.setHeaderVisible(true);
 		parameterTable.addListener(SWT.Selection, this);
 		TableColumn col;
 		col = new TableColumn(parameterTable, SWT.LEFT | SWT.READ_ONLY);
 		col.setText("sel");
 		col.setWidth(30);
 		col = new TableColumn(parameterTable, SWT.LEFT | SWT.READ_ONLY);
 		col.setText("Parameter name");
 		col.setWidth(200);
 		col = new TableColumn(parameterTable, SWT.LEFT | SWT.READ_ONLY);
 		col.setText("Parameter description");
 		col.setWidth(200);	
 		setControl(composite);
 	}
 
 	/**
 	 * @see org.eclipse.swt.widgets.Listener#
 	 * handleEvent(org.eclipse.swt.widgets.Event)
 	 */
 	@Override
 	public void handleEvent(Event event) {
 		if (event.widget == parameterTable){
 			this.selectedParameters.clear();
 			for (TableItem tableItem : parameterTable.getItems()){
 				if (tableItem.getChecked()){
 					ParameterInfo pC = 
 						new ParameterInfo(tableItem.getText(1));
 					pC.setDescription(tableItem.getText(2));
 					selectedParameters.add(pC);
 				}
 			}
 			checkIsPageComplete();
 		}
 	}
 	
 	/**
 	 * Checks whether the edited data are complete and sets
 	 * the corresponding message to the top of the wizard page.
 	 */
 	private void checkIsPageComplete() {
 		setTransformationSystem();
 		if (this.module == null){
			setMessage("Cannot load henshin transformation system.", 
															SWT.ERROR);
 			setPageComplete(false);
 			return;
 		}
 		Unit mainUnit = this.module.getUnit(MAINUNIT);
 		if (mainUnit == null){
 			setMessage("There is no transformation unit in '" + 
 					this.transformationFileName + "' named '" + 
 					MAINUNIT + "'", SWT.ERROR);
 			setPageComplete(false);
 			return;
 		}
 		boolean selectedEObjectParameterExists = false;
 		for (Parameter parameter : mainUnit.getParameters()){
 			if (parameter.getName().equals(SELECTEDEOBJECT)){
 				selectedEObjectParameterExists = true;
 				break;
 			}
 		}
 		if (!selectedEObjectParameterExists){
 			setMessage("There is no parameter in unit '" + 
 					MAINUNIT + "' named '" + SELECTEDEOBJECT + "'", 
 															SWT.ERROR);
 			setPageComplete(false);
 			return;
 		}
 		setMessage("Please select the refactoring parameters " +
 				"out of those from the main unit of the " +
 				"selected henshin transformation (execute).");
 	}
 
 }
