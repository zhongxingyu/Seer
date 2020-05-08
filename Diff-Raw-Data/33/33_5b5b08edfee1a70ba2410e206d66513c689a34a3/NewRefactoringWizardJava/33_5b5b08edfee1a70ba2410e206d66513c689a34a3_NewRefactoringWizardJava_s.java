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
 package org.eclipse.emf.refactor.refactoring.generator.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.refactor.refactoring.generator.core.RefactoringInfo;
 import org.eclipse.emf.refactor.refactoring.generator.interfaces.INewRefactoringWizard;
 import org.eclipse.emf.refactor.refactoring.generator.managers.GenerationManager;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 /**
  * Wizard for editing data needed for model refactoring code generation.
  * @generated NOT
  * @author Thorsten Arendt
  */
 public class NewRefactoringWizardJava extends Wizard implements INewWizard, INewRefactoringWizard {
 	
 	/**
 	 *  Wizard page for configurating model refactoring data (basics).
 	 */
 	private BasicDataWizardPage basicWizardPage;	
 	
 	/**
 	 * Wizard page for configurating model refactoring parameters.
 	 */
 	private ParameterWizardPage parameterWizardPage;
 	
 	/**
 	  * Wizard page for configurating model test parameters.
 	  */
 	 private TestWizardPage testWizardPage;
 	 
 	 private LinkedList<IProject> projects;
 
 	private String projectName;
 
 	private String menuLabel;
 
 	private String id;
 
 	private String namespaceUri;
 
 	private String className;
 
 	private String jar;
 
 	private String importPackage;
 	 
 	 /**
 	  * Default constructor that initializes its wizard pages.
 		* @param selectedEObject The selected EObject when starting the 
 		* generation process of the model refactoring.
 		*/
 	public NewRefactoringWizardJava(){
		super();
		setWindowTitle("EMF Refactor - Specify EMF Model Refactoring");
		this.addPages();
 	}
 	
 //	/**
 //	 * Default constructor that initializes its wizard pages.
 //	 * @param selectedEObject The selected EObject when starting the 
 //	 * generation process of the model refactoring.
 //	 */
 //	public NewRefactoringWizardJava(EObject selectedEObject){
 //		super();
 //		setWindowTitle("EMF Refactor - Specify EMF Model Refactoring");
 //		this.basicWizardPage = new BasicDataWizardPage("EMF Refactor", selectedEObject);
 //		this.parameterWizardPage = new ParameterWizardPage("EMF Refactor");
 //		this.testWizardPage = new TestWizardPage("EMF Refactor");
 //		this.addPage(basicWizardPage);
 //		this.addPage(parameterWizardPage);
 //		this.addPage(testWizardPage);
 //	}
 
 	public NewRefactoringWizardJava(String metamodel, String contextType) {
		super();
		setWindowTitle("EMF Refactor - Specify EMF Model Refactoring");
 		this.namespaceUri = metamodel;
 		this.className = contextType;
		this.addPages();
 	}
 	
 	@Override
 	public void addPages() {
 		this.basicWizardPage = new BasicDataWizardPage();
 		this.parameterWizardPage = new ParameterWizardPage();
 		this.testWizardPage = new TestWizardPage();
 		if (namespaceUri != null && ! namespaceUri.isEmpty() 
 				&& className != null && ! className.isEmpty()) {
 			setMetamodelAndContext();
 		}
 		this.addPage(basicWizardPage);
 		this.addPage(parameterWizardPage);
 		this.addPage(testWizardPage);
 	}
 	
 	public void setMetamodelAndContext() {
 		basicWizardPage.setMetamodel(namespaceUri);
 		basicWizardPage.setContextType(className);
 	}
 
 	/**
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	@Override
 	public boolean performFinish() {
 		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
 			@Override
 			public void execute(IProgressMonitor monitor)
 					throws InvocationTargetException {
 				try {				
 					createRefactoring(monitor);
 				} catch (Exception e){
 					e.printStackTrace();
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 		try {
 			getContainer().run(false, false, op);
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	/**
 	 * Triggers the model refactoring generation activity. Collects data
 	 * from its wizard pages and starts the refactoring generator.
 	 * @param monitor Object monitoring the model refactoring 
 	 * generation activity.
 	 */
 	protected void createRefactoring(IProgressMonitor monitor) {
 //		String projectName = basicWizardPage.getProjectName();
 //		String id = basicWizardPage.getRefactoringId();
 //		String menuLabel = basicWizardPage.getMenuLabel();
 //		String namespaceUri = basicWizardPage.getNsUri();
 //		String namespacePrefix = basicWizardPage.getNsPrefix();
 //		String metaModelName = basicWizardPage.getMetaModelName();
 //		String className = basicWizardPage.getClassName();
 //		String jar = basicWizardPage.getJar();
 		int numberOfTests = testWizardPage.getNumberOfTests();
 		String nsPrefix = getNsPrefix();
 		String metaModelName = getMetaModelName();
 		RefactoringInfo refactoringConfig = 
 		   new RefactoringInfo(projectName, id, menuLabel, namespaceUri,
 		         nsPrefix, numberOfTests);
 		refactoringConfig.setSelectedEObjectJar(jar);
 		refactoringConfig.setSelectedEObjectClass(importPackage + "." + className);
 		refactoringConfig.setMetaModelName(metaModelName);
 		refactoringConfig.setParameters(parameterWizardPage.getParameters());
 		System.out.println(refactoringConfig);
 		GenerationManager rg = new GenerationManager(refactoringConfig); 
 		rg.run(monitor);
 	}
 
 	private String getMetaModelName() {
 		EPackage mm = 
 				EPackage.Registry.INSTANCE.getEPackage(namespaceUri);
 		return mm.getName();
 	}
 
 	private String getNsPrefix() {
 		EPackage mm = 
 				EPackage.Registry.INSTANCE.getEPackage(namespaceUri);
 		return mm.getNsPrefix();
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		initProjects();
 	}
 	
 	private void initProjects(){
 		this.projects = new LinkedList<IProject>();
 		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (IProject project : allProjects) {
 			if (project.isOpen()) {
 				IProjectNature nature = null;
 				try {
 					nature = project.getNature("org.eclipse.pde.PluginNature");
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 				if (null != nature) 
 					this.projects.add(project);
 			}
 		}
 	}
 	
 	public LinkedList<IProject> getProjects() {
 		return projects;
 	}
 
 	@Override
 	public int getPageNumbers() {
 		return 3;
 	}
 
 	@Override
 	public void updateSecondPage() { }
 
 	@Override
 	public WizardPage getSecondPage() {
 		return parameterWizardPage;
 	}
 
 	@Override
 	public void setTargetProject(String projectName) {
 		this.projectName = projectName;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.menuLabel = name;
 	}
 
 	@Override
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	@Override
 	public void setMetamodel(String metamodel) {
 		this.namespaceUri = metamodel;
 	}
 
 	@Override
 	public void setContext(String context) {
 		this.className = context;
 	}
 
 	@Override
 	public void setJar(String jar) {
 		this.jar = jar;
 	}
 
 	@Override
 	public void setImportPackage(String importPackage) {
 		this.importPackage = importPackage;
 	}
 	
 }
