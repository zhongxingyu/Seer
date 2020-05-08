 package org.mule.tooling.devkit.wizards;
 
 import static org.mule.tooling.devkit.common.DevkitUtils.DEMO_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.DOCS_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.ICONS_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.MAIN_FLOWS_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.MAIN_JAVA_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.MAIN_MULE_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.MAIN_RESOURCES_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.POM_FILENAME;
 import static org.mule.tooling.devkit.common.DevkitUtils.POM_TEMPLATE_PATH;
 import static org.mule.tooling.devkit.common.DevkitUtils.TEST_JAVA_FOLDER;
 import static org.mule.tooling.devkit.common.DevkitUtils.TEST_RESOURCES_FOLDER;
 
 import java.io.ByteArrayInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.apt.core.util.AptConfig;
 import org.eclipse.jdt.apt.core.util.IFactoryPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.mule.tooling.devkit.DevkitImages;
 import org.mule.tooling.devkit.builder.DevkitBuilder;
 import org.mule.tooling.devkit.builder.DevkitNature;
 import org.mule.tooling.devkit.common.DevkitUtils;
 import org.mule.tooling.devkit.common.ConnectorMavenModel;
 import org.mule.tooling.devkit.template.ImageWriter;
 import org.mule.tooling.devkit.template.TemplateFileWriter;
 import org.mule.tooling.devkit.template.replacer.ClassReplacer;
 import org.mule.tooling.devkit.template.replacer.ConnectorClassReplacer;
 import org.mule.tooling.devkit.template.replacer.MavenParameterReplacer;
 import org.mule.tooling.devkit.template.replacer.NullReplacer;
 
 public class NewDevkitProjectWizard extends Wizard implements INewWizard {
 
 	private static final String MAIN_TEMPLATE_PATH = "/templates/connector_main.tmpl";
     private static final String TEST_TEMPLATE_PATH = "/templates/connector_test.tmpl";
     private static final String TEST_RESOURCE_PATH = "/templates/connector-test-resource.tmpl";
 
     private NewDevkitProjectWizardPage page;
     private NewDevkitProjectWizardPageAdvance advancePage;
     private ISelection selection;
     private ConnectorMavenModel connectorModel;
     public NewDevkitProjectWizard() {
         super();
         setNeedsProgressMonitor(true);
         this.setDefaultPageImageDescriptor(DevkitImages.getManaged("", "mulesoft-logo.png"));
         connectorModel = new ConnectorMavenModel();
     }
 
     @Override
     public void addPages() {
         page = new NewDevkitProjectWizardPage(selection,connectorModel);
         advancePage = new NewDevkitProjectWizardPageAdvance(connectorModel);
         addPage(page);
         addPage(advancePage);
     }
 
     @Override
     public IWizardPage getNextPage(IWizardPage currentPage) {
         if (currentPage instanceof NewDevkitProjectWizardPage) {
         	advancePage.setDevkitVersion(page.getDevkitVersion());
         	advancePage.refresh();
            return advancePage;
         }
         return null;
     } 
     @Override
     public boolean performFinish() {
         final ConnectorMavenModel mavenModel = new ConnectorMavenModel(page.getVersion(), page.getGroupId(), page.getArtifactId(),page.getCategory());
         mavenModel.setAddGitInformation(advancePage.getAddGitHubInfo());
         mavenModel.setGitConnection(advancePage.getConnection());
         mavenModel.setGitDevConnection(advancePage.getDevConnection());
         mavenModel.setGitUrl(advancePage.getUrl());
         
         final String runtimeId = page.getDevkitVersion();
         final String packageName = page.getPackage();
         final String connectorName = page.getName();
         final boolean isMetaDataEnabled = advancePage.isMetadaEnabled();
         final boolean isOAuth = advancePage.isOAuth();
         final boolean hasQuery = advancePage.hasQuery();
         final String minMuleVersion = getMinMuleVersion();
         IRunnableWithProgress op = new IRunnableWithProgress() {
 
             @Override
             public void run(IProgressMonitor monitor) throws InvocationTargetException {
                 try {
                     doFinish(mavenModel,runtimeId,packageName,connectorName, monitor,isMetaDataEnabled,isOAuth,hasQuery,minMuleVersion);
                 } catch (CoreException e) {
                     throw new InvocationTargetException(e);
                 } finally {
                     monitor.done();
                 }
             }
         };
         try {
             getContainer().run(true, false, op);
         } catch (InterruptedException e) {
             return false;
         } catch (InvocationTargetException e) {
             Throwable realException = e.getTargetException();
             MessageDialog.openError(getShell(), "Error", realException.getMessage());
             return false;
         }
         return true;
     }
 
     private String getMinMuleVersion() {
     	String minMuleVersion = "3.3";
    	if(advancePage.hasQuery() || page.getDevkitVersion().equals(DevkitUtils.DEVKIT_3_5_0)){
     		minMuleVersion = "3.5";
     	}else{
    		if(advancePage.isMetadaEnabled() || page.getDevkitVersion().startsWith("3.4")){
     			minMuleVersion = "3.4";
     		}
     	}
     	return minMuleVersion;
 	}
 
 	/**
      * The worker method. It will find the container, create the file if missing or just replace its contents, and open the editor on the newly created file.
      * @param hasQuery 
      * @param isOauth 
      * @param isMetaDataEnabled 
 	 * @param minMuleVersion 
      */
 
     private void doFinish(ConnectorMavenModel mavenModel,String runtimeId,String connectorPackage,String connectorName, IProgressMonitor monitor, boolean isMetaDataEnabled, boolean isOAuth, boolean hasQuery, String minMuleVersion) throws CoreException {
         String artifactId = mavenModel.getArtifactId();
         String groupId = mavenModel.getGroupId();
         monitor.beginTask("Creating project" + artifactId, 2);
         IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 
         IProject project = createProject(artifactId, monitor, root);
         IJavaProject javaProject = JavaCore.create(root.getProject(artifactId));
 
         List<IClasspathEntry> entries = generateProjectEntries(monitor, project);
 
         create(project.getFolder(DOCS_FOLDER), monitor);
         create(project.getFolder(ICONS_FOLDER), monitor);
         create(project.getFolder(DEMO_FOLDER), monitor);
         create(project.getFolder(MAIN_JAVA_FOLDER + "/" + connectorPackage.replaceAll("\\.", "/")), monitor);
         create(project.getFolder(TEST_JAVA_FOLDER + "/" + connectorPackage.replaceAll("\\.", "/")), monitor);
 
         javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[] {}), monitor);
 
         ClassReplacer classReplacer = new ConnectorClassReplacer(connectorPackage, connectorName, DevkitUtils.createConnectorNameFrom(connectorName),runtimeId, isMetaDataEnabled,isOAuth,minMuleVersion,hasQuery,mavenModel.getCategory(),mavenModel.getGitUrl());
 
         TemplateFileWriter templateFileWriter = new TemplateFileWriter(project, monitor);
         templateFileWriter.apply(POM_TEMPLATE_PATH, POM_FILENAME, new MavenParameterReplacer(mavenModel,runtimeId,connectorName));
         templateFileWriter.apply("/templates/README.tmpl", "README.md", classReplacer);
         templateFileWriter.apply("/templates/CHANGELOG.tmpl", "CHANGELOG.md", classReplacer);
         templateFileWriter.apply("/templates/LICENSE_HEADER.txt.tmpl", "LICENSE_HEADER.txt", classReplacer);
         templateFileWriter.apply("/templates/LICENSE.tmpl", "LICENSE.md", new NullReplacer());
 
 
         create(connectorName, monitor, getMainTemplatePath(), getTestResourcePath(), DevkitUtils.createConnectorNameFrom(connectorName), connectorPackage, project, classReplacer);
         
         AptConfig.setEnabled(javaProject, true);
         IFactoryPath path=AptConfig.getFactoryPath(javaProject);
         path.enablePlugin(org.mule.devkit.apt.Activator.PLUGIN_ID);
         AptConfig.setFactoryPath(javaProject, path);
         AptConfig.addProcessorOption(javaProject, "enableJavaDocValidation", "true");
         
         monitor.worked(1);
     }
 
     protected void create(String moduleName, IProgressMonitor monitor, String mainTemplatePath, String testResourceTemplatePath, String className, String packageName,
             IProject project, ClassReplacer classReplacer) throws CoreException {
         
         TemplateFileWriter fileWriter = new TemplateFileWriter(project, monitor);
         ImageWriter imageWriter = new ImageWriter(project, monitor);
         fileWriter.apply(mainTemplatePath, buildMainTargetFilePath(packageName, className), classReplacer);
         fileWriter.apply(testResourceTemplatePath, getResourceExampleFileName(moduleName), classReplacer);
         fileWriter.apply(TEST_TEMPLATE_PATH, buildTestTargetFilePath(packageName, className), classReplacer);
         fileWriter.apply("/templates/example.tmpl", getExampleFileName(moduleName), classReplacer);
 
         imageWriter.apply("/templates/extension-icon-24x16.png", getIcon24FileName(moduleName));
         imageWriter.apply("/templates/extension-icon-48x32.png", getIcon48FileName(moduleName));
 
     }
     
     private List<IClasspathEntry> generateProjectEntries(IProgressMonitor monitor, IProject project) throws CoreException {
         List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
         entries.add(createEntry(project.getFolder(MAIN_JAVA_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(MAIN_RESOURCES_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(TEST_RESOURCES_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(TEST_JAVA_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(MAIN_MULE_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(MAIN_FLOWS_FOLDER), monitor));
         entries.add(createEntry(project.getFolder(DevkitUtils.GENERATED_SOURCES_FOLDER), monitor));
         entries.add(JavaRuntime.getDefaultJREContainerEntry());
         return entries;
     }
 
     private IProject createProject(String artifactId, IProgressMonitor monitor, IWorkspaceRoot root) throws CoreException {
 
         IProjectDescription projectDescription = getProjectDescription(root, artifactId);
 
         IProject project = root.getProject(artifactId);
         if (!project.exists()) {
             project.create(projectDescription, monitor);
             project.open(monitor);
         }
         return project;
     }
 
     private IProjectDescription getProjectDescription(IWorkspaceRoot root, String artifactId) throws CoreException {
         IProjectDescription desc = root.getWorkspace().newProjectDescription(artifactId);
         desc.setNatureIds(new String[] { JavaCore.NATURE_ID, DevkitNature.NATURE_ID });
         ICommand[] commands = desc.getBuildSpec();
 
         ICommand[] newCommands = new ICommand[commands.length + 1];
         System.arraycopy(commands, 0, newCommands, 0, commands.length);
 
         ICommand command = desc.newCommand();
         command.setBuilderName(DevkitBuilder.BUILDER_ID);
         newCommands[newCommands.length - 1] = command;
 
         desc.setBuildSpec(newCommands);
         return desc;
     }
 
     protected IClasspathEntry createEntry(final IResource resource, IProgressMonitor monitor) throws CoreException {
         create(resource, monitor);
         return JavaCore.newSourceEntry(resource.getFullPath());
     }
 
     protected void create(final IResource resource, IProgressMonitor monitor) throws CoreException {
         if (resource == null || resource.exists())
             return;
         if (!resource.getParent().exists())
             create(resource.getParent(), monitor);
         switch (resource.getType()) {
         case IResource.FILE:
             ((IFile) resource).create(new ByteArrayInputStream(new byte[0]), true, monitor);
             break;
         case IResource.FOLDER:
             ((IFolder) resource).create(IResource.NONE, true, monitor);
             break;
         case IResource.PROJECT:
             break;
         }
     }
 
     /**
      * We will accept the selection in the workbench to see if we can initialize from it.
      * 
      * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
      */
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         this.selection = selection;
     }
     
     protected String getTestResourcePath() {
         return TEST_RESOURCE_PATH;
     }
 
     protected String getMainTemplatePath() {
         return MAIN_TEMPLATE_PATH;
     }
 
     private String getResourceExampleFileName(String connectorName) {
         return TEST_RESOURCES_FOLDER + "/" + connectorName.toLowerCase() + "-config.xml";
     }
 
     private String getIcon48FileName(String connectorName) {
         return "icons/" + connectorName.toLowerCase() + "-connector-48x32.png";
     }
 
     private String getIcon24FileName(String connectorName) {
         return "icons/" + connectorName.toLowerCase() + "-connector-24x16.png";
     }
 
     private String getExampleFileName(String connectorName) {
         return "doc" + "/" + connectorName.toLowerCase() + "-connector.xml.sample";
     }
 
     protected String buildMainTargetFilePath(String packageName, String className) {
         return MAIN_JAVA_FOLDER + "/" + packageName.replaceAll("\\.", "/") + "/" + className + ".java";
     }
 
     private String buildTestTargetFilePath(String packageName, String className) {
         return TEST_JAVA_FOLDER + "/" + packageName.replaceAll("\\.", "/") + "/" + className + "Test.java";
     }
 }
