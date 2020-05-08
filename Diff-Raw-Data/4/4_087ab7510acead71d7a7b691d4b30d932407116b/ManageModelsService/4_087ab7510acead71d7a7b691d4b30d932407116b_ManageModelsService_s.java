 package it.unitn.disi.unagi.application.internal.services;
 
 import it.unitn.disi.unagi.application.Activator;
 import it.unitn.disi.unagi.application.exceptions.CouldNotCompileConstraintsFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotCreateFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotDeleteFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotGenerateRequirementsClassesException;
 import it.unitn.disi.unagi.application.nls.Messages;
 import it.unitn.disi.unagi.application.services.IManageModelsService;
 import it.unitn.disi.unagi.application.services.IManageProjectsService;
 import it.unitn.disi.util.io.FileIOUtil;
 import it.unitn.disi.util.logging.LogUtil;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.ecore.generator.Generator;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eeat.ocl.compiler.OCLCompiler;
 import org.eeat.ocl.compiler.OCLCompilerException;
 import org.eeat.ocl.compiler.OCLParser;
 import org.eeat.ocl.compiler.OCLParserException;
 
 /**
  * Implementation of the service class for model management.
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  * @version 1.0
  */
 public class ManageModelsService extends ManageFilesService implements IManageModelsService {
 	/** File extension for model generation files. */
 	private static final String GENMODEL_FILE_EXTENSION = "genmodel"; //$NON-NLS-1$
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageModelsService#createNewRequirementsModel(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IProject, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public IFile createNewRequirementsModel(IProgressMonitor progressMonitor, IProject project, String name, String basePackage) throws CouldNotCreateFileException {
 		String projectName = project.getName();
 		LogUtil.log.info("Creating new requirements model {0} (base package: {1}) in project {2}.", name, basePackage, projectName); //$NON-NLS-1$
 
 		// Obtains the models folder.
 		IFolder modelsFolder = project.getFolder(IManageProjectsService.MODELS_PROJECT_SUBDIR);
 
 		// Generates a file object in the folder, checking that the file can be created later.
 		String fileName = name + '.' + REQUIREMENTS_MODEL_EXTENSION;
 		IFile modelFile = generateCreatableFileDescriptor(modelsFolder, fileName);
 
 		// Creates the new model file in the project.
 		createNewFile(progressMonitor, modelFile);
 
 		// Generates initial contents for the requirements file.
 		try {
 			createRequirementsContents(modelFile, name, basePackage);
 		}
 		catch (IOException e) {
 			LogUtil.log.error("Could not create initial contents for requirements model {0}.", e, modelFile.getFullPath()); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(modelFile);
 		}
 
 		// Returns the newly created file.
 		return modelFile;
 	}
 
 	/**
 	 * Internal method that creates the basic EMF contents of a new requirements model file that is being created.
 	 * 
 	 * @param modelFile
 	 *          The workspace file in which to put the contents.
 	 * @throws IOException
 	 *           In case any I/O errors occur during this process.
 	 */
 	private void createRequirementsContents(IFile modelFile, String name, String basePackage) throws IOException {
 		String modelFilePath = modelFile.getLocationURI().toString();
 		LogUtil.log.info("Creating basic EMF contents for requirements file: {0}.", modelFilePath); //$NON-NLS-1$
 
 		// Initializes the standalone factory implementation for ecore files and a new resource set.
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(REQUIREMENTS_MODEL_EXTENSION, new EcoreResourceFactoryImpl());
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// Loads the goal model EMF file.
 		URL url = FileLocator.find(Activator.getBundle(), GORE_EMF_FILE_PATH, Collections.EMPTY_MAP);
 		Resource ddlResource = resourceSet.createResource(URI.createURI(url.toString()));
 		ddlResource.load(Collections.EMPTY_MAP);
 
 		// Load the package from the goal model EMF file and create a new package for the requirements file.
 		final EPackage ddlPackage = (EPackage) ddlResource.getContents().get(0);
 		final EPackage newPackage = EcoreFactory.eINSTANCE.createEPackage();
 		newPackage.setName(name);
 		newPackage.setNsPrefix(basePackage);
 		newPackage.setNsURI(modelFile.getName());
 
 		// Create the main goal for the requirements file and adds it to its package.
 		final EClass mainGoal = EcoreFactory.eINSTANCE.createEClass();
 		mainGoal.setName(MAIN_GOAL_BASE_NAME);
 		newPackage.getEClassifiers().add(mainGoal);
 
 		// Add the class Goal from the Goal Model EMF file as superclass of the main goal in the requirements model.
 		final EClass superClass = (EClass) ddlPackage.getEClassifier("Goal"); //$NON-NLS-1$
 		mainGoal.getESuperTypes().add(superClass);
 
 		// Finally, create a new resource to save the requirements package into a new model file.
 		Resource outputRes = resourceSet.createResource(URI.createURI(modelFilePath));
 		outputRes.getContents().add(newPackage);
 		outputRes.save(Collections.EMPTY_MAP);
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageModelsService#deleteRequirementsModel(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public void deleteRequirementsModel(IProgressMonitor progressMonitor, IFile modelFile) throws CouldNotDeleteFileException {
 		// Deletes the file from the workspace.
 		deleteFile(progressMonitor, modelFile);
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageModelsService#generateRequirementsClasses(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public IFolder generateRequirementsClasses(IProgressMonitor progressMonitor, IFile modelFile) throws CouldNotGenerateRequirementsClassesException {
 		try {
 			IProject project = modelFile.getProject();
 			IFolder sourcesFolder = project.getFolder(IManageProjectsService.SOURCES_PROJECT_SUBDIR);
 
 			// Extracts model name and base package from the model file.
 			EPackage ePackage = extractEMFPackage(modelFile);
 			String modelName = modelFile.getLocation().removeFileExtension().lastSegment();
 			String basePackage = ePackage.getNsPrefix();
 
 			// Creates the genmodel file that is used to generate the Java classes.
 			Resource genModelResource = createGenModelFile(progressMonitor, modelFile, modelName, basePackage, sourcesFolder);
 
 			// Generates the sources for the classes declared in the model.
 			generateClasses(progressMonitor, genModelResource);
 
 			return sourcesFolder;
 		}
 		catch (IOException | CoreException e) {
 			LogUtil.log.error("Unagi caught an exception while trying to generate sources from a requirements model: {0}.", e, modelFile.getName()); //$NON-NLS-1$
 			throw new CouldNotGenerateRequirementsClassesException(modelFile, e);
 		}
 	}
 
 	/**
 	 * Internal method for retrieving the EPackage element from an EMF ECore file.
 	 * 
 	 * @param modelFile
 	 *          The ECore file from which to retrieve the ECore package element.
 	 * @return The EPackage element representing an ECore package.
 	 */
 	private EPackage extractEMFPackage(IFile modelFile) {
 		// Initializes the ECore model.
 		EcorePackage.eINSTANCE.eClass();
 
 		// Loads the EMF file as a resource.
 		ResourceSet resSet = new ResourceSetImpl();
 		Resource resource = resSet.getResource(URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true), true);
 
 		// Retrieves the package object.
 		EPackage ePackage = (EPackage) resource.getContents().get(0);
 		return ePackage;
 	}
 
 	/**
 	 * Internal method for creating the model generation file from a given EMF ECore file.
 	 * 
 	 * This method's implementation is based on code taken from the run() method of class
 	 * org.eclipse.emf.codegen.ecore.Generator.
 	 * 
 	 * @param progressMonitor
 	 *          The workbench's progress monitor, in case the operation takes a long time.
 	 * @param modelFile
 	 *          The ECore file that defines the classes that guide the generation of source code.
 	 * @param modelName
 	 *          The name of the model that is defined in the model file.
 	 * @param basePackage
 	 *          The base package under which the classes should be generated.
 	 * @param sourcesFolder
 	 *          The folder in which source code files should be generated.
 	 * @return A resource file that represents a model generator that can be used for actually generating the source code
 	 *         in a later step.
 	 * @throws IOException
 	 *           If there are any problems in the creation of the model generation file.
 	 * @see org.eclipse.emf.codegen.ecore.Generator
 	 */
 	private Resource createGenModelFile(IProgressMonitor progressMonitor, IFile modelFile, String modelName, String basePackage, IFolder sourcesFolder) throws IOException {
 		// Create paths and URI objects for the model, the sources directory and the genmodel file.
 		IPath ecorePath = modelFile.getLocation();
 		IPath sourcesDirPath = sourcesFolder.getFullPath();
 		IPath genModelPath = ecorePath.removeFileExtension().addFileExtension(GENMODEL_FILE_EXTENSION);
 		URI ecoreURI = URI.createFileURI(ecorePath.toString());
 		URI genModelURI = URI.createFileURI(genModelPath.toString());
 
 		// Obtains the ECore package from the model file.
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap());
 		Resource resource = resourceSet.getResource(ecoreURI, true);
 		EPackage ePackage = (EPackage) resource.getContents().get(0);
 
 		// Updates the progress monitor with the task that is about to happen.
 		progressMonitor.beginTask("", 2); //$NON-NLS-1$
 		progressMonitor.subTask(Messages.getFormattedString("task.generateRequirementsClasses.createGenModelFile.description", modelName, genModelPath)); //$NON-NLS-1$
 
 		// Creates the genmodel file as a resource in the workspace and configures its parameters.
 		// setModelDirectory() indicates where the class generation should take place.
 		Resource genModelResource = Resource.Factory.Registry.INSTANCE.getFactory(genModelURI).createResource(genModelURI);
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		genModelResource.getContents().add(genModel);
 		resourceSet.getResources().add(genModelResource);
 		genModel.setModelDirectory(sourcesDirPath.toString());
 		genModel.getForeignModel().add(ecorePath.lastSegment());
 		genModel.initialize(Collections.singleton(ePackage));
 		GenPackage genPackage = genModel.getGenPackages().get(0);
 		genModel.setModelName(genModelURI.trimFileExtension().lastSegment());
 		genPackage.setBasePackage(basePackage);
 
 		//
 		URL baseGenModelURL = FileLocator.find(Activator.getBundle(), BASE_GENMODEL_FILE_PATH, Collections.EMPTY_MAP);
 		Resource baseGenModelResource = resourceSet.createResource(URI.createURI(baseGenModelURL.toString()));
 		baseGenModelResource.load(Collections.EMPTY_MAP);
 		GenModel baseGenModel = (GenModel) baseGenModelResource.getContents().get(0);
 		for (GenPackage pkg : baseGenModel.getGenPackages())
 			genModel.getUsedGenPackages().add(pkg);
 
 		// Generates the genmodel file and updates the progress monitor.
 		progressMonitor.worked(1);
 		genModelResource.save(Collections.EMPTY_MAP);
 
 		// Returns the genmodel object.
 		return genModelResource;
 	}
 
 	/**
 	 * Internal method for generating source code files given the instructions contained in the model generator file that
 	 * was created earlier from the EMF Ecore file that defines the model.
 	 * 
 	 * This method's implementation is based on code taken from the run() method of class
 	 * org.eclipse.emf.codegen.ecore.Generator.
 	 * 
 	 * @param progressMonitor
 	 *          The workbench's progress monitor, in case the operation takes a long time.
 	 * @param genModelResource
 	 *          The model generator file that contains the instructions for source code generation.
 	 * @throws CoreException
 	 *           If there are any problems in the generation of source files.
 	 */
 	private void generateClasses(IProgressMonitor progressMonitor, Resource genModelResource) throws CoreException {
 		// Retrieves the genmodel object from the genmodel resource and checks that it's valid.
 		GenModel genModel = (GenModel) genModelResource.getContents().get(0);
 		IStatus status = genModel.validate();
 		if (!status.isOK()) {
 			Diagnostic diagnostic = genModel.diagnose();
 			throw new IllegalStateException("Genmodel file is not valid. Diagnostic message is: " + diagnostic.getMessage()); //$NON-NLS-1$
 		}
 
 		// Creates a generator from the genmodel file and configures the generation.
 		Generator generator = new Generator();
 		generator.setInput(genModel);
 		genModel.setForceOverwrite(true);
 		genModel.reconcile();
 		genModel.setCanGenerate(true);
 		genModel.setValidateModel(true);
 		genModel.setUpdateClasspath(false);
 		genModel.setCodeFormatting(true);
 
 		// Generates the source code following the instructions contained in the genmodel file.
 		generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, CodeGenUtil.EclipseUtil.createMonitor(progressMonitor, 1));
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageConstraintsService#createNewConstraintsFile(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IProject, java.lang.String)
 	 */
 	@Override
 	public IFile createNewConstraintsFile(IProgressMonitor progressMonitor, IProject project, String name) throws CouldNotCreateFileException {
 		String projectName = project.getName();
 		LogUtil.log.info("Creating a new constraints file {0} in project {1}.", name, projectName); //$NON-NLS-1$
 
 		// Obtains the models folder.
 		IFolder modelsFolder = project.getFolder(IManageProjectsService.MODELS_PROJECT_SUBDIR);
 
 		// Generates a file object in the folder, checking that the file can be created later.
 		String fileName = name + '.' + CONSTRAINTS_FILE_EXTENSION;
 		IFile constraintsFile = generateCreatableFileDescriptor(modelsFolder, fileName);
 
 		// Creates the new constraints file in the project.
 		createNewFile(progressMonitor, constraintsFile);
 
 		// Generates initial contents for the constraints file.
 		try {
 			createConstraintsContents(progressMonitor, constraintsFile, name);
 		}
 		catch (IOException | CoreException e) {
 			LogUtil.log.error("Could not create initial contents for constraints file: {0}.", e, constraintsFile.getFullPath()); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(constraintsFile);
 		}
 
 		// Returns the newly created file.
 		return constraintsFile;
 	}
 
 	/**
 	 * Internal method that creates the basic contents of a new constraints file that is being created.
 	 * 
 	 * @param progressMonitor
 	 *          The workbench's progress monitor, in case the operation takes a long time.
 	 * @param constraintsFile
 	 *          The workspace file in which to put the contents.
 	 * @param packageName
 	 *          The name of the package that is being defined in this constraints file.
 	 * @throws CoreException
 	 *           In case Eclipse cannot set the contents of the file.
 	 * @throws IOException
 	 *           In case any I/O errors occur during the processing of the template for constraints files.
 	 */
 	private void createConstraintsContents(IProgressMonitor progressMonitor, IFile constraintsFile, String packageName) throws CoreException, IOException {
 		// Loads the template for constraints files from the plug-in bundle.
 		URL templateFileURL = FileLocator.find(Activator.getBundle(), CONSTRAINTS_TEMPLATE_FILE_PATH, Collections.EMPTY_MAP);
 
 		// Process the template, replacing the package name with the name of the file (without extension).
 		Map<String, Object> map = new HashMap<>();
 		map.put(CONSTRAINTS_VARIABLE_PACKAGE_NAME, packageName);
 		String contents = FileIOUtil.processTemplate(templateFileURL, map);
 
 		// Changes the contents of the constraints file.
 		InputStream source = new ByteArrayInputStream(contents.getBytes());
 		constraintsFile.setContents(source, true, false, progressMonitor);
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageConstraintsService#deleteConstraintsFile(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public void deleteConstraintsFile(IProgressMonitor progressMonitor, IFile constraintsFile) throws CouldNotDeleteFileException {
 		// Deletes the file from the workspace.
 		deleteFile(progressMonitor, constraintsFile);
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageModelsService#compileConstraintsFile(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public IFile compileConstraintsFile(IProgressMonitor progressMonitor, IFile constraintsFile) throws CouldNotCompileConstraintsFileException {
 		IProject project = constraintsFile.getProject();
 
 		try {
 			// Obtains a list with all existing requirements models in the project (including the base models).
 			List<URL> requirementsModels = listRequirementsModels(project);
 
 			// Parses the constraints file.
 			OCLParser parser = new OCLParser(requirementsModels);
 			parser.parse(constraintsFile.getLocationURI().toURL());
 
 			// Loads the compiler's template file from the plug-in.
 			IPath templatePath = new Path(Activator.getProperty(Activator.PROPERTY_COMPILER_TEMPLATE_FILE));
 			URL templateURL = FileLocator.find(Activator.getBundle(), templatePath, Collections.EMPTY_MAP);
 
 			// Compiles the constraints file that was parsed.
 			OCLCompiler compiler = new OCLCompiler(parser, templateURL);
 			String result = compiler.compile();
 
 			// Creates a new file in the same folder as the constraints file, but with rules file extension.
 			String rulesFileName = constraintsFile.getFullPath().removeFileExtension().lastSegment() + '.' + RULES_FILE_EXTENSION;
 			IFolder modelsFolder = (IFolder) constraintsFile.getParent();
 			IFile rulesFile = modelsFolder.getFile(rulesFileName);
 
 			// Writes the result of the compilation to the rules file and returns it.
 			FileIOUtil.saveFile(rulesFile.getLocation().toString(), result);
 			return rulesFile;
 		}
		catch (CoreException | IOException | OCLParserException | OCLCompilerException e) {
 			LogUtil.log.error("Could not compile constraints file: {0}.", e, constraintsFile.getFullPath()); //$NON-NLS-1$
 			throw new CouldNotCompileConstraintsFileException(constraintsFile);
 		}
 	}
 
 	/**
 	 * Scans the models folder and produces a list with the URLs of all requirements model files.
 	 * 
 	 * @param project
 	 *          The project whose model folder should be scanned.
 	 * @return A list of URL objects, pointing to the location of the project's requirements files.
 	 * @throws CoreException
 	 *           If an Eclipse error occur while scanning the project.
 	 * @throws MalformedURLException
 	 *           If the URI returned from the elements of the Eclipse project are malformed.
 	 */
 	private List<URL> listRequirementsModels(IProject project) throws CoreException, MalformedURLException {
 		List<URL> models = new ArrayList<>();
 
 		// Retrieves the model folder from the project.
 		IFolder modelsFolder = project.getFolder(IManageProjectsService.MODELS_PROJECT_SUBDIR);
 
 		// Checks its members, those who have the extension indicating they are requirements files.
 		for (IResource resource : modelsFolder.members())
 			if (resource.getType() == IResource.FILE)
 				if (REQUIREMENTS_MODEL_EXTENSION.equals(resource.getFileExtension()))
 					models.add(resource.getLocationURI().toURL());
 
 		// Returns the list of requirements models.
 		return models;
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageModelsService#deleteRulesFile(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public void deleteRulesFile(IProgressMonitor progressMonitor, IFile rulesFile) throws CouldNotDeleteFileException {
 		// Deletes the file from the workspace.
 		deleteFile(progressMonitor, rulesFile);
 	}
 }
