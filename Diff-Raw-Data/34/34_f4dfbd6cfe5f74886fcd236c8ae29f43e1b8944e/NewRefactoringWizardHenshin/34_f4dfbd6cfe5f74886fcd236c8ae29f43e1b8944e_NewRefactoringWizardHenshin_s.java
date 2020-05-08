 package org.eclipse.emf.refactor.refactoring.henshin.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.refactor.refactoring.generator.interfaces.INewRefactoringWizard;
 import org.eclipse.emf.refactor.refactoring.generator.ui.BasicDataWizardPage;
 import org.eclipse.emf.refactor.refactoring.generator.ui.TestWizardPage;
 import org.eclipse.emf.refactor.refactoring.henshin.generator.HenshinRefactoringInfo;
 import org.eclipse.emf.refactor.refactoring.henshin.managers.HenshinGenerationManager;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 public class NewRefactoringWizardHenshin extends Wizard implements INewWizard, INewRefactoringWizard {
 	
 	private String menuLabel;
 	private String id;
 	private String namespaceUri;
 	private String className;
 	private String jar;
 	private String importPackage;
 	private LinkedList<IProject> projects;
 	private final String WINDOW_TITLE = "New Refactoring";
 	
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
 	 *  Wizard page for configurating model refactoring data (basics).
 	 */
 	private BasicDataWizardPage basicWizardPage;
 	
 	/**
 	 * Wizard page for specifying Henshin files used for initial checking,
 	 * final checking, and executing the EMF model refactoring.
 	 */
 	private HenshinTransformationWizardPage transformationWizardPage;
 	
 	/**
 	 * Wizard page for configurating model refactoring parameters.
 	 */
 	private ParameterWizardPage parameterWizardPage;
 	
 	/**
 	 * Wizard page for configurating model test parameters.
 	 */
 	private TestWizardPage testWizardPage;
 	
 	public NewRefactoringWizardHenshin() { }
 
 	public NewRefactoringWizardHenshin(String metamodel, String contextType) {
 		this.namespaceUri = metamodel;
 		this.className = contextType;
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		initProjects();
 	}
 	
 	@Override
 	public void addPages() {
 		setWindowTitle(WINDOW_TITLE);
 		
 		this.basicWizardPage = new BasicDataWizardPage();
 		this.transformationWizardPage = 
 				new HenshinTransformationWizardPage("EMF Refactor", this);
 		this.parameterWizardPage = 
 				new ParameterWizardPage("EMF Refactor");
 		this.testWizardPage = new TestWizardPage();
 		if (namespaceUri != null && ! namespaceUri.isEmpty() 
 				&& className != null && ! className.isEmpty()) {
 			setMetamodelAndContext();
 		}
 		this.addPage(basicWizardPage);
 		this.addPage(transformationWizardPage);
 		this.addPage(parameterWizardPage);
 		this.addPage(testWizardPage);
 	}
 	
 	public void setMetamodelAndContext() {
 		basicWizardPage.setMetamodel(namespaceUri);
 		basicWizardPage.setContextType(className);
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
 	
 	@Override
 	public boolean canFinish() {
 		return (basicWizardPage.isPageComplete() 
					&& transformationWizardPage.isPageComplete());
 	}
 
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
 		// from ...Java
 		int numberOfTests = testWizardPage.getNumberOfTests();
 		String nsPrefix = getNsPrefix();
 		String metaModelName = getMetaModelName();
 		String createChangeFileName = 
 				transformationWizardPage.getTransformationFileName();
 		HenshinRefactoringInfo refactoringConfig = 
 		   new HenshinRefactoringInfo(projectName, id, menuLabel, namespaceUri,
 				   createChangeFileName, nsPrefix, numberOfTests);
 		refactoringConfig.setSelectedEObjectJar(jar);
 		refactoringConfig.setSelectedEObjectClass(importPackage + "." + className);
 		refactoringConfig.setMetaModelName(metaModelName);
 		refactoringConfig.setParameters(parameterWizardPage.getSelectedParameters());
 		refactoringConfig.setCheckInitialFileName
 						(transformationWizardPage.getCheckInitialFileName());
 		refactoringConfig.setCheckFinalFileName
 						(transformationWizardPage.getCheckFinalFileName());
 		System.out.println(refactoringConfig);
 		HenshinGenerationManager hgm = 
 					new HenshinGenerationManager(refactoringConfig);
 		hgm.run(monitor);
 	}
 	
 	private String getNsPrefix() {
 		EPackage mm = 
 				EPackage.Registry.INSTANCE.getEPackage(namespaceUri);
 		return mm.getNsPrefix();
 	}
 	
 	private String getMetaModelName() {
 		EPackage mm = 
 				EPackage.Registry.INSTANCE.getEPackage(namespaceUri);
 		return mm.getName();
 	}
 
 	@Override
 	public LinkedList<IProject> getProjects() {
 		return projects;
 	}
 
 	@Override
 	public int getPageNumbers() {
 		return 4;
 	}
 
 	@Override
 	public void updateSecondPage() { 
 		this.transformationWizardPage.setProjectName(projectName); 
 	}
 
 	@Override
 	public WizardPage getSecondPage() {
 		return transformationWizardPage;
 	}
 
 	@Override
 	public void setTargetProject(String projectName) {
 		this.projectName = projectName;
 		this.transformationWizardPage.setProjectName(projectName); 
 		this.parameterWizardPage.setProjectName(projectName); 
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
 
 	public void setTransformationFileName(String transformationFileName) {
 		this.transformationFileName = transformationFileName;
 		this.parameterWizardPage.setTransformationFileName(transformationFileName); 
 	}
 
 	public String getTransformationFileName() {
 		return transformationFileName;
 	}
 	
 	/**
 	 * Gets the name of the project the model refactoring code has to be 
 	 * generated to.
 	 * @return Name of the project the model refactoring code has to be 
 	 * generated to.
 	 */
 	public String getProjectName(){
 		return this.projectName;
 	}
 }
