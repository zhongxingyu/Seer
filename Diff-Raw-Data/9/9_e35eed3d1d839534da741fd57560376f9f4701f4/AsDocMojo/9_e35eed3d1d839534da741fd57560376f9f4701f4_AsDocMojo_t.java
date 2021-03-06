 package info.rvin.flexmojos.asdoc;
 
 import flex2.compiler.util.ThreadLocalToolkit;
 import flex2.tools.ASDoc;
 import info.rvin.flexmojos.utilities.MavenUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.apache.maven.project.MavenProjectHelper;
 
 /**
  * Goal which generates documentation from the ActionScript sources.
  *
  * @goal asdoc
  * @requiresDependencyResolution
  */
 public class AsDocMojo extends AbstractMojo {
 
 	/**
 	 * The maven project.
 	 *
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter expression="${project.build}"
 	 * @required
 	 * @readonly
 	 */
 	protected Build build;
 
 	/**
 	 * @component
 	 */
 	protected MavenProjectHelper projectHelper;
 
 	/**
 	 * @component
 	 */
 	protected ArtifactFactory artifactFactory;
 
 	/**
 	 * @component
 	 */
 	protected ArtifactResolver resolver;
 
 	/**
 	 * @component
 	 */
 	protected ArtifactMetadataSource artifactMetadataSource;
 
 	/**
 	 * @component
 	 */
 	protected MavenProjectBuilder mavenProjectBuilder;
 
 	/**
 	 * @parameter expression="${localRepository}"
 	 */
 	protected ArtifactRepository localRepository;
 
 	/**
 	 * @parameter expression="${project.remoteArtifactRepositories}"
 	 */
 	@SuppressWarnings("unchecked")
 	protected List remoteRepositories;
 
 	/**
 	 * @parameter expression="${plugin.artifacts}"
 	 */
 	private List<Artifact> pluginArtifacts;
 
 	/**
 	 * A list of classes to document. These classes must be in the source path.
 	 * This is the default option.
 	 *
 	 * This option works the same way as does the -include-classes option for
 	 * the compc component compiler. For more information, see Using compc, the
 	 * component compiler.
 	 *
 	 * @parameter
 	 */
 	private String[] docClasses;
 
 	/**
 	 * A list of URIs whose classes should be documented. The classes must be in
 	 * the source path.
 	 *
 	 * You must include a URI and the location of the manifest file that defines
 	 * the contents of this namespace.
 	 *
 	 * This option works the same way as does the -include-namespaces option for
 	 * the compc component compiler. For more information, see Using compc, the
 	 * component compiler.
 	 *
 	 * @parameter
 	 */
 	private Namespace[] docNamespaces;
 
 	/**
 	 * A list of files that should be documented. If a directory name is in the
 	 * list, it is recursively searched.
 	 *
 	 * This option works the same way as does the -include-sources option for
 	 * the compc component compiler. For more information, see Using compc, the
 	 * component compiler.
 	 *
 	 * @parameter
 	 *
 	 */
 	private File[] docSources;
 
 	/**
 	 * A list of classes that should not be documented. You must specify
 	 * individual class names. Alternatively, if the ASDoc comment for the class
 	 * contains the
 	 *
 	 * @private tag, is not documented.
 	 *
 	 * @parameter
 	 */
 	private String[] excludeClasses;
 
 	/**
 	 * Whether all dependencies found by the compiler are documented. If true,
 	 * the dependencies of the input classes are not documented.
 	 *
 	 * The default value is false.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean excludeDependencies;
 
 	/**
 	 * The text that appears at the bottom of the HTML pages in the output
 	 * documentation.
 	 *
 	 * @parameter
 	 */
 	private String footer;
 
 	/**
 	 * An integer that changes the width of the left frameset of the
 	 * documentation. You can change this size to accommodate the length of your
 	 * package names.
 	 *
 	 * The default value is 210 pixels.
 	 *
 	 * @parameter default-value="120"
 	 *
 	 */
 	private int leftFramesetWidth;
 
 	/**
 	 * The text that appears at the top of the HTML pages in the output
 	 * documentation.
 	 *
 	 * The default value is "API Documentation".
 	 *
 	 * @parameter default-value="API Documentation"
 	 */
 	private String mainTitle;
 
 	/**
 	 * The output directory for the generated documentation. The default value
 	 * is "asdoc-output".
 	 *
 	 * @parameter
 	 */
 	protected File output;
 
 	/**
 	 * The descriptions to use when describing a package in the documentation.
 	 * You can specify more than one package option.
 	 *
 	 * @parameter
 	 */
 	private Map<String, String> packageDescriptions;
 
 	/**
 	 * The path to the ASDoc template directory. The default is the
 	 * asdoc/templates directory in the ASDoc installation directory. This
 	 * directory contains all the HTML, CSS, XSL, and image files used for
 	 * generating the output.
 	 *
 	 * @parameter
 	 */
 	private File templatesPath;
 
 	/**
 	 * The text that appears in the browser window in the output documentation.
 	 *
 	 * The default value is "API Documentation".
 	 *
 	 * @parameter default-value="API Documentation"
 	 */
 	private String windowTitle;
 
 	/**
 	 *
 	 */
 	private List<File> libraries;
 
 	/**
 	 * Load a file containing configuration options
 	 *
 	 * @parameter
 	 */
 	private File configFile;
 
 	private File fontsSnapshot;
 
 	/**
 	 * specifies a compatibility version. e.g. compatibility 2.0.1
 	 *
 	 * @parameter
 	 */
 	private String compatibilityVersion;
 
 	/**
 	 * List of path elements that form the roots of ActionScript class
 	 * hierarchies.
 	 *
 	 * @parameter
 	 */
 	protected File[] sourcePaths;
 
 	/**
 	 * Sets the compiler when it runs on a server without a display. This is
 	 * equivalent to using the <code>compiler.headless-server</code> option of
 	 * the mxmlc or compc compilers.
 	 *
 	 * that value determines if the compiler is running on a server without a
 	 * display.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean headlessServer;
 
 	@SuppressWarnings("unchecked")
 	protected void setUp() throws MojoExecutionException, MojoFailureException {
 		if (sourcePaths == null) {
 			sourcePaths = MavenUtils.getSourcePaths(build);
 		}
 		if (docSources == null && docClasses == null && docNamespaces == null) {
 			getLog().warn(
 					"Nothing expecified to include.  Assuming source folders.");
 			docSources = MavenUtils.getSourcePaths(build);
 		}
 
 		libraries = new ArrayList<File>();
 		for (Artifact artifact : MavenUtils.getDependencyArtifacts(project,
 				resolver, localRepository, remoteRepositories,
 				artifactMetadataSource)) {
 			if ("swc".equals(artifact.getType())) {
 				libraries.add(artifact.getFile());
 			}
 		}
 
 		if (output == null) {
 			output = new File(build.getDirectory(), "asdoc");
 			if (!output.exists()) {
 				output.mkdirs();
 			}
 		}
 
 		if (configFile == null) {
 			List<Resource> resources = build.getResources();
 			for (Resource resource : resources) {
 				File cfg = new File(resource.getDirectory(),
 						getConfigFileName());
 				if (cfg.exists()) {
 					configFile = cfg;
 					break;
 				}
 			}
 		}
 		if (configFile == null) {
 			URL url = getClass().getResource("/configs/" + getConfigFileName());
 			configFile = new File(build.getDirectory(), getConfigFileName());
 			try {
 				FileUtils.copyURLToFile(url, configFile);
 			} catch (IOException e) {
 				throw new MojoExecutionException("Error creating config.xml");
 			}
 		}
 		if (!configFile.exists()) {
 			throw new MojoExecutionException("Unable to find " + configFile);
 		}
 
 		if (fontsSnapshot == null) {
 			URL url;
 			if (MavenUtils.isMac()) {
 				url = getClass().getResource("/fonts/macFonts.ser");
 			} else {
 				// And linux?!
 				// if(os.contains("windows")) {
 				url = getClass().getResource("/fonts/winFonts.ser");
 			}
 			File fontsSer = new File(build.getDirectory(), "fonts.ser");
 			try {
 				FileUtils.copyURLToFile(url, fontsSer);
 			} catch (IOException e) {
 				throw new MojoExecutionException("Error creating fonts.ser");
 			}
 			fontsSnapshot = fontsSer;
 		}
 
 		if (templatesPath == null) {
 			templatesPath = generateDefaultTemplate();
 		}
 	}
 
 	private File generateDefaultTemplate() throws MojoExecutionException {
 		File templates = new File(build.getDirectory(), "templates");
 		templates.mkdirs();
 		try {
 			for (Artifact artifact : pluginArtifacts) {
 				if ("template".equals(artifact.getClassifier())) {
 					UnzipUtils.unzip(new FileInputStream(artifact.getFile()),
 							templates);
 					return templates;
 				}
 			}
 
 			throw new MojoExecutionException(
 					"Unable to generate default template.");
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			makeHelperExecutable(templates);
 		}
 	}
 
 	private void makeHelperExecutable(File templates)
 			throws MojoExecutionException {
 		if (!MavenUtils.isWindows()) {
 			Runtime runtime = Runtime.getRuntime();
 			String pathname = String.format("%s/%s", templates
 					.getAbsolutePath(), "asDocHelper"
 					+ (MavenUtils.isLinux() ? ".linux" : ""));
 			String[] statements = new String[] { "chmod", "u+x", pathname };
 			try {
 				Process p = runtime.exec(statements);
 				int result = p.waitFor();
 				if (0 != result) {
 					throw new MojoExecutionException(String.format(
 							"Unable to execute %s. Return value = %d", Arrays
 									.asList(statements), result));
 				}
 			} catch (Exception e) {
 				throw new MojoExecutionException(String.format(
 						"Unable to execute %s", Arrays.asList(statements)));
 			}
 		}
 	}
 
 	private String getConfigFileName() {
 		return "config.xml";
 	}
 
 	protected void run() throws MojoExecutionException, MojoFailureException {
 
 		if (docSources == null || docSources.length == 0) {
 			getLog().warn("No source folder found!");
 			return;
 		}
 
 		List<String> args = new ArrayList<String>();
 
 		addDocSources(args);
 		addDocClasses(args);
 		addDocNamespaces(args);
 		addSourcePath(args);
 		addLibraries(args);
 		addCompatibility(args);
 		addPackageDescriptions(args);
 		addExcludeClasses(args);
 		addFooter(args);
 		args.add("-templates-path=" + templatesPath.getAbsolutePath());
 		args.add("-window-title=" + windowTitle);
 		args.add("-main-title=" + mainTitle);
 		args.add("-left-frameset-width=" + leftFramesetWidth);
 		args.add("-exclude-dependencies=" + excludeDependencies);
 		args.add("-compiler.fonts.local-fonts-snapshot="
 				+ fontsSnapshot.getAbsolutePath());
		if (headlessServer) {
 			args.add("-compiler.headless-server=true");
 		}
 		args.add("-load-config=" + configFile.getAbsolutePath());
 		args.add("-output=" + output.getAbsolutePath());
 
 		getLog().info(args.toString());
 
 		// I hate this, waiting for asdoc-oem
 		// https://bugs.adobe.com/jira/browse/SDK-15405
 		ASDoc.asdoc(args.toArray(new String[args.size()]));
 
 		int errorCount = ThreadLocalToolkit.errorCount();
 		if (errorCount > 0) {
 			throw new MojoExecutionException("Error compiling!");
 		}
 	}
 
 	private void addDocNamespaces(List<String> args) {
 		if (docNamespaces == null || docNamespaces.length == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (Namespace namespace : docNamespaces) {
 			if (sb.length() == 0) {
 				sb.append("-doc-namespaces=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(namespace.getUri());
 		}
 		args.add(sb.toString());
 	}
 
 	private void addDocClasses(List<String> args) {
 		if (docClasses == null || docClasses.length == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (String docClass : docClasses) {
 			if (sb.length() == 0) {
 				sb.append("-doc-classes=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(docClass);
 		}
 		args.add(sb.toString());
 	}
 
 	private void addExcludeClasses(List<String> args) {
 		if (excludeClasses == null || excludeClasses.length == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (String excludeClass : excludeClasses) {
 			if (sb.length() == 0) {
 				sb.append("-exclude-classes=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(excludeClass);
 		}
 
 		args.add(sb.toString());
 	}
 
 	private void addFooter(List<String> args) {
 		if (footer != null) {
 			args.add("-footer=" + footer);
 		}
 	}
 
 	private void addPackageDescriptions(List<String> args) {
 		if (packageDescriptions == null) {
 			return;
 		}
 
 		for (String pack : packageDescriptions.keySet()) {
			args.add("-packages.package");
			args.add(pack);
			args.add(packageDescriptions.get(pack));
 		}
 	}
 
 	private void addCompatibility(List<String> args) {
 		if (compatibilityVersion != null) {
 			args.add("-compiler.mxml.compatibility-version="
 					+ compatibilityVersion);
 		}
 	}
 
 	private void addLibraries(List<String> args) {
 		if (libraries == null || libraries.size() == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (File lib : libraries) {
 			if (sb.length() == 0) {
 				sb.append("-library-path=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(lib.getAbsolutePath());
 		}
 
 		args.add(sb.toString());
 	}
 
 	private void addSourcePath(List<String> args) {
 		if (sourcePaths == null || sourcePaths.length == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (File path : sourcePaths) {
 			if (sb.length() == 0) {
 				sb.append("-source-path=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(path.getAbsolutePath());
 		}
 		args.add(sb.toString());
 	}
 
 	private void addDocSources(List<String> args) {
 		if (docSources == null || docSources.length == 0) {
 			return;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (File docs : docSources) {
 			if (sb.length() == 0) {
 				sb.append("-doc-sources=");
 			} else {
 				sb.append(',');
 			}
 
 			sb.append(docs.getAbsolutePath());
 		}
 		args.add(sb.toString());
 	}
 
 	protected void tearDown() throws MojoExecutionException,
 			MojoFailureException {
 
 	}
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		setUp();
 		run();
 		tearDown();
 	}
 
 }
