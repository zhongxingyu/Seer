 package com.isb.jVoice.dsl.builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.graphiti.mm.algorithms.AlgorithmsPackage;
 import org.eclipse.graphiti.mm.algorithms.styles.StylesPackage;
 import org.eclipse.graphiti.mm.pictograms.PictogramsPackage;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 
 import com.google.inject.Injector;
 import com.isb.bks.ivr.voice.dsl.VoiceDslStandaloneSetup;
 import com.isb.bks.ivr.voice.dsl.validation.VoiceDslJavaValidator;
 import com.vectorsf.jvoice.core.validation.operations.OperationsValidator;
 import com.vectorsf.jvoice.model.base.JVModule;
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.model.operations.OperationsPackage;
 import com.vectorsf.jvoice.prompt.model.voiceDsl.VoiceDslPackage;
 
 /**
  * Goal which touches a timestamp file.
  * 
  * @goal generateFlow
  * 
  * @phase generate-sources
  * 
  * @configurator include-project-dependencies
  * 
  * @requiresDependencyResolution compile+runtime
  */
 public class GenerateFlowMojo extends AbstractMojo {
 
 	private static final CharSequence SEPARATOR2 = "\\";
 
 	private static final CharSequence SEPARATOR = "/";
 
 	/**
 	 * Location of the target directory.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 */
 	private File outputDirectory;
 
 	/**
 	 * Location of the source directory.
 	 * 
 	 * @parameter expression="${basedir}/src/main/resources/jv"
 	 */
 	private File sourceDirectory;
 
 	/**
 	 * A set of patterns matching files from the sourceDirectory that should be processed as grammars.
 	 * 
 	 * @parameter
 	 */
 	protected Set<String> includes = new HashSet<>();
 
 	/**
 	 * A set of exclude patterns.
 	 * 
 	 * @parameter
 	 */
 	protected Set<String> excludes = new HashSet<>();
 
 	/**
 	 * The Maven Project Object
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 */
 	protected MavenProject project;
 
 	/**
 	 * The maven project's helper.
 	 * 
 	 * @component role="org.apache.maven.project.MavenProjectHelper"
 	 * @required
 	 * @readonly
 	 */
 	private MavenProjectHelper projectHelper;
 
 	/**
 	 * @parameter expression="${project.runtimeClasspathElements}"
 	 */
 	private List<String> runtimeClasspathElements;
 
 	private String nameProject;
 
 	private URL[] buildURLs() {
 		List<URL> urls = new ArrayList<URL>(runtimeClasspathElements.size());
 		for (String element : runtimeClasspathElements) {
 
 			try {
 				urls.add(new File(element).toURI().toURL());
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		return urls.toArray(new URL[urls.size()]);
 	}
 
 	private ClassLoader createClassLoaderForProjectDependencies() {
 		URL[] urls = buildURLs();
 		return new URLClassLoader(urls, this.getClass().getClassLoader());
 	}
 
 	@Override
 	public void execute() throws MojoExecutionException {
 		
 		File f = new File(outputDirectory, "jVoice");
 		if (!f.exists()) {
 			f.mkdirs();
 		}
 		try {
 
 			Injector injector = new VoiceDslStandaloneSetup().createInjectorAndDoEMFRegistration();
 
 			EPackage.Registry.INSTANCE.put(OperationsPackage.eNS_URI, OperationsPackage.eINSTANCE);
 			EPackage.Registry.INSTANCE.put(PictogramsPackage.eNS_URI, PictogramsPackage.eINSTANCE);
 			EPackage.Registry.INSTANCE.put(AlgorithmsPackage.eNS_URI, AlgorithmsPackage.eINSTANCE);
 			EPackage.Registry.INSTANCE.put(StylesPackage.eNS_URI, StylesPackage.eINSTANCE);
 
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("jvflow", new XMIResourceFactoryImpl());
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("projectInformation",
 					new XMIResourceFactoryImpl());
 
 			ResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
 			resourceSet.getLoadOptions().put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 
 			// Se registran las validaciones para los DSLs
 			VoiceDslJavaValidator validator = injector.getInstance(VoiceDslJavaValidator.class);
 	        EValidator.Registry.INSTANCE.put(VoiceDslPackage.eINSTANCE, validator);
 	        // Se registran las validaciones para el modelo de operaciones
 	        EValidator.Registry.INSTANCE.put(OperationsPackage.eINSTANCE,
 	                new OperationsValidator());
 			
 			VegaXMLURIHandlerMavenImpl vegaURIHandler = new VegaXMLURIHandlerMavenImpl(project,
 					createClassLoaderForProjectDependencies());
 			resourceSet.getLoadOptions().put(XMLResource.OPTION_URI_HANDLER, vegaURIHandler);
 
 			// Obtenemos el nombre del modulo del projectInformation
 			JVModule modulo = getProjectInformation();
 			nameProject = modulo.getName();
 
 			generateMainModule(modulo, f);
			
			//Si el módulo no tiene flujos, sourceDirectory puede no existir.
			//Por lo que lo comprobamos y si no existe ni intentamos procesar los flujos.
			if (sourceDirectory.exists()){
				processFlowFiles(resourceSet, f);				
			}
 			if (project != null) {
 				projectHelper.addResource(project, outputDirectory.getAbsolutePath(),
 						Collections.singletonList("jVoice/**/*.xml"), Collections.emptyList());
 				projectHelper.addResource(project, project.getBasedir().getAbsolutePath(),
 						Collections.singletonList(".projectInformation"), Collections.emptyList());
 			}
 
 		} catch (Exception e) {
 			throw new MojoExecutionException("", e);
 		}
 
 	}
 
 	private void generateMainModule(JVModule modulo, File mainFolder) {
 		File folder = new File(mainFolder, modulo.getName());
 		folder.mkdirs();
 		MainFlowGenerator.compile(new File(folder, "errorHandler.xml"), modulo);
 	}
 
 	/**
 	 * Metodo que accede al projectInformation para obtener informacion de el.
 	 */
 	private JVModule getProjectInformation() {
 		String ruta = "file:///" + project.getBasedir().getAbsolutePath() + SEPARATOR + ".projectInformation";
 		ResourceSet resSet = new ResourceSetImpl();
 		URI uri = URI.createURI(ruta.replace(SEPARATOR2, SEPARATOR));
 		Resource res = resSet.getResource(uri, true);
 		JVModule module = (JVModule) res.getContents().get(0);
 		return module;
 	}
 
 	private String getTargetFlowName(String name) {
 		String basename = name.substring(0, name.lastIndexOf('.'));
 
 		return basename + ".xml";
 	}
 
 	private Set<String> getFlowIncludesPatterns() {
 		if (includes == null || includes.isEmpty()) {
 			Set<String> ret = new HashSet<>();
 			ret.add("**/*.jvflow");
 			ret.add("**/*.voiceDsl");
 
 			return ret;
 		}
 		return includes;
 	}
 
 	/**
 	 * Checks to see if the list of outputFiles all exist, and have last-modified timestamps which are later than the
 	 * last-modified timestamp of the grammar file. If these conditions hold, the method returns false, otherwise, it
 	 * returns true.
 	 * 
 	 * @param sourceFile
 	 * @param outputFiles
 	 * @return
 	 */
 	private static boolean buildRequired(File sourceFile, List<File> outputFiles) {
 
 		long sourceLastModified = getLastModified(sourceFile);
 
 		for (File outputFile : outputFiles) {
 			if (!outputFile.exists() || sourceLastModified > outputFile.lastModified()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static long getLastModified(File sourceFile) {
 		long lastModified = sourceFile.lastModified();
 		String flowName = sourceFile.getName();
 		flowName = flowName.substring(0, flowName.lastIndexOf('.'));
 
 		File folder = new File(sourceFile.getParentFile(), flowName + ".resources");
 		for (File locution : folder.listFiles()) {
 			long locutionModified = locution.lastModified();
 			if (locutionModified > lastModified) {
 				lastModified = locutionModified;
 			}
 		}
 		return lastModified;
 	}
 
 	private void processFlowFiles(ResourceSet resourceSet, File folder) throws InclusionScanException, IOException, MojoExecutionException {
 		SourceMapping mapping = new SuffixMapping("jvflow", Collections.<String> emptySet());
 		Set<String> includes = getFlowIncludesPatterns();
 		SourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
 
 		scan.addSourceMapping(mapping);
 		Set<File> modifiedFiles = scan.getIncludedSources(sourceDirectory, null);
 		if (modifiedFiles.isEmpty()) {
 			if (getLog().isInfoEnabled()) {
 				getLog().info("No Flows to process");
 			}
 		} else {
 			boolean built = false;
 			Set<File> flowFiles = getParentFlows(modifiedFiles);
 			for (File flow : flowFiles) {
 				built |= processFlowFile(resourceSet, flow, folder);
 			}
 			if (!built && getLog().isInfoEnabled()) {
 				getLog().info("No DSL processed; generated files are up to date");
 			}
 		}
 	}
 
 	private Set<File> getParentFlows(Set<File> files) {
 		Set<File> onlyFlows = new HashSet<>();
 		for (File file : files) {
 			String name = file.getName();
 			String extension = name.substring(name.lastIndexOf('.') + 1);
 			if (extension.equals("jvflow")) {
 				onlyFlows.add(file);
 			} else if (extension.equals("voiceDsl")) {
 				onlyFlows.add(findFlowFor(file));
 			}
 		}
 
 		return onlyFlows;
 	}
 
 	private File findFlowFor(File file) {
 		File parent = file.getParentFile();
 		String parentName = parent.getName();
 		String flowName = parentName.substring(0, parentName.lastIndexOf('.')) + ".jvflow";
 		File grandpa = parent.getParentFile();
 		File flow = new File(grandpa, flowName);
 
 		return flow;
 	}
 
 	private boolean processFlowFile(ResourceSet resourceSet, File origFile, File targetFolder) throws IOException, MojoExecutionException {
 		// Obtenemos los paquetes en los que se encuentra el archivo.
 		String rutafile = origFile.getAbsolutePath().toString().replace(sourceDirectory.toString(), "").trim();
 		// Copiamos la estrucutra de paquetes.
 		targetFolder = new File(targetFolder, rutafile.replace(origFile.getName(), "").trim());
 		if (!targetFolder.exists()) {
 			targetFolder.mkdirs();
 		}
 
 		File targetFile = new File(targetFolder, getTargetFlowName(origFile.getName()));
 
 		if (buildRequired(origFile, Collections.singletonList(targetFile))) {
 			targetFile.createNewFile();
 
 			// resolve test
 			URI uri = URI.createFileURI(origFile.getCanonicalPath().toString());
 			Resource res = resourceSet.getResource(uri, true);
         
 	        EObject eObject = res.getContents().get(1);
 			if (eObject instanceof Flow)
 	        {
 		        // Se valida el flow, sus padres y sus estados hijos
 		        Diagnostic diagn = Diagnostician.INSTANCE.validate(eObject);
 		        List<Diagnostic> lD = diagn.getChildren();
 		        String sError=null;
 		        for (Diagnostic diagnostic : lD) 
 		        {
 		        	if (diagnostic.getSeverity()==Diagnostic.ERROR)
 		        	{
 		        		sError+=" -- "+diagnostic.getMessage();
 		        		System.out.print("ERROR: ");
 		        	}
 		        	else if (diagnostic.getSeverity()==Diagnostic.WARNING)
 		        	{
 		        		System.out.print("WARNING: ");
 		        	}
 		        	System.out.println(diagnostic.getMessage());
 				}
 				if (sError != null)
 				{
 					throw new MojoExecutionException(sError);
 				}
 	        }
 	        Resource diagrama =  res.getContents().get(0).eResource();
 			SpringWebFlowGenerator.compile(targetFile, diagrama, nameProject);
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
