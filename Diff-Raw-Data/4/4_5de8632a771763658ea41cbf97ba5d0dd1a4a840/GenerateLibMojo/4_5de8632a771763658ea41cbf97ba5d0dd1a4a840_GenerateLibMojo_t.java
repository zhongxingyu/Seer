 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin.distgen;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactCollector;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.shared.dependency.tree.DependencyNode;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
 import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
 
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 import org.twdata.maven.mojoexecutor.MojoExecutor;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.security.KeyStore;
 import java.security.PublicKey;
 import java.security.cert.Certificate;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 
 import de.cismet.cids.jnlp.AllPermissions;
 import de.cismet.cids.jnlp.ApplicationDesc;
 import de.cismet.cids.jnlp.Argument;
 import de.cismet.cids.jnlp.ComponentDesc;
 import de.cismet.cids.jnlp.Extension;
 import de.cismet.cids.jnlp.Homepage;
 import de.cismet.cids.jnlp.Information;
 import de.cismet.cids.jnlp.J2Se;
 import de.cismet.cids.jnlp.Jar;
 import de.cismet.cids.jnlp.Jnlp;
 import de.cismet.cids.jnlp.ObjectFactory;
 import de.cismet.cids.jnlp.Property;
 import de.cismet.cids.jnlp.Resources;
 import de.cismet.cids.jnlp.Security;
 
 import de.cismet.cids.mavenplugin.AbstractCidsMojo;
 
 /**
  * Goal which generates a the lib folder of a cids distribution.
  *
  * @version                       $Revision$, $Date$
  * @goal                          generate-lib
  * @phase                         prepare-package
  * @requiresDependencyResolution  runtime
  */
 // TODO: this class should be totally refactored as the design is awkward
 public class GenerateLibMojo extends AbstractCidsMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String STARTER_DIR = "starter"; // NOI18N
 
     public static final String CLIENT_DIR = "client"; // NOI18N
 
     public static final String CLASSPATH_DIR = "classpath"; // NOI18N
 
     public static final String CLASSIFIER_CLASSPATH = "classpath"; // NOI18N
     public static final String CLASSIFIER_STARTER = "starter";     // NOI18N
     public static final String FILE_EXT_JAR = "jar";               // NOI18N
     public static final String FILE_EXT_JNLP = "jnlp";             // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${cids.generate-lib.skip}" default-value="false"
      * @required   false
      */
     private transient Boolean skip;
 
     /**
      * The directory where the lib directory shall be created in. It is most likely the cids Distribution directory and
      * most likely the directory that is hosted via the <code>codebase</code> parameter, too.<br/>
      * <br/>
      * E.g. outputDirectory = /home/cismet/cidsDistribution, codebase = http://www.cismet.de/cidsDistribution
      *
      * @parameter  expression="${cids.generate-lib.outputDirectory}" default-value="target/generate-lib-out"
      * @required   true
      */
     private transient File outputDirectory;
 
     /**
      * The vendor generating the lib structure.
      *
      * @parameter  expression="${cids.generate-lib.vendor}
      */
     private transient String vendor;
 
     /**
      * The homepage of the vendor generating the lib structure.
      *
      * @parameter  expression="${cids.generate-lib.homepage}
      */
     private transient String homepage;
 
     /**
      * The <code>codebase</code> URL is the pendant to the outputDirectory. It serves as a pointer to the publicly
      * hosted distribution and will be used in <code>jnlp</code> file generation. If the parameter is not provided,
      * <code>classpath-jnlp</code> files won't be generated.
      *
      * @parameter  expression="${cids.generate-lib.codebase}"
      * @required   false
      */
     private transient URL codebase;
 
     /**
      * The <code>m2codebase</code> points to the directory where the m2 artifacts are hosted. If the parameter is
      * parseable as an URL then it is assumed to be an absolute ref, otherwise it is interpreted relative to <code>
      * codebase</code>. If the parameter is not provided at all it is assumed that the <code>m2codebase</code> will be
      * hosted at <code>${codebase}/lib/m2</code>. If neither <code>codebase</code> nor <code>m2codebase</code> is
      * provided or none of them are absolute <code>classpath-jnlps</code> will not be generated. If the generation shall
      * be done in <code>legacy</code> mode the value of this parameter will be ignored
      *
      * @parameter  expression="${cids.generate-lib.m2codebase}" default-value="lib/m2"
      * @required   false
      */
     private transient String m2codebase;
 
     /**
      * DOCUMENT ME!
      *
      * @parameter  expression="${cids.generate-lib.accountExtension}"
      * @required   true
      */
     private transient String accountExtension;
 
     /**
      * Allows for more fine grained generation options.
      *
      * @parameter
      */
     private transient DependencyEx[] dependencyConfiguration;
 
     /**
      * The artifact repository to use.
      *
      * @parameter  expression="${localRepository}"
      * @required
      * @readonly
      */
     private transient ArtifactRepository localRepository;
 
     /**
      * The artifact collector to use.
      *
      * @component
      * @required
      * @readonly
      */
     private transient ArtifactCollector artifactCollector;
 
     /**
      * The dependency tree builder to use.
      *
      * @component
      * @required
      * @readonly
      */
     private DependencyTreeBuilder dependencyTreeBuilder;
 
     /**
      * The Maven Session Object.
      *
      * @parameter  expression="${session}"
      * @required
      * @readonly
      */
     private transient MavenSession session;
 
     /**
      * The Maven PluginManager Object.
      *
      * @component
      * @required
      * @readonly
      */
     private transient PluginManager pluginManager;
 
     /** Cache for the files whose signature has already been verified. */
     private final transient Set<File> verified = new HashSet<File>(100);
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Generates a lib directory from the projects dependencies.
      *
      * @throws  MojoExecutionException  if any error occurs during execution
      * @throws  MojoFailureException    if the mojo cannot be executed due to wrong plugin configuration
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                 getLog().info("generate lib skipped"); // NOI18N
             }
 
             return;
         }
 
         // get all direct artifacts of the project and scan through the dependency configuration if there is some
         // additional requirement to the dependency artifacts
         final Set<Artifact> dependencies = project.getDependencyArtifacts();
         final Set<ArtifactEx> accepted = new LinkedHashSet<ArtifactEx>(dependencies.size());
         for (final Artifact artifact : dependencies) {
             // only accept artifacts neccessary for runtime
             if (Artifact.SCOPE_COMPILE.equals(artifact.getScope())
                         || Artifact.SCOPE_RUNTIME.equals(artifact.getScope())) {
                 accepted.add(getExtendedArtifact(artifact));
             }
         }
 
         final List<ArtifactEx> ordered = determineProcessingOrder(accepted);
 
         if (getLog().isDebugEnabled()) {
             getLog().debug("order: " + ordered); // NOI18N
         }
 
         final List<ArtifactEx> processed = new ArrayList<ArtifactEx>(ordered.size());
 
         for (final ArtifactEx toProcess : ordered) {
             processArtifact(toProcess, processed);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifactEx  DOCUMENT ME!
      * @param   processed   DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      * @throws  MojoFailureException    DOCUMENT ME!
      */
     private void processArtifact(final ArtifactEx artifactEx, final List<ArtifactEx> processed)
             throws MojoExecutionException, MojoFailureException {
         if (getLog().isDebugEnabled()) {
             getLog().debug("processing artifact: " + artifactEx); // NOI18N
         }
 
         final ArtifactEx virtualParent = createVirtualArtifact(artifactEx);
         if (artifactEx.getDependencyEx().isGenerateJar()) {
             // search for already processed child artifacts
             ArtifactEx child = null;
             for (int i = processed.size() - 1; i >= 0; --i) {
                 final ArtifactEx current = processed.get(i);
                 if (current.getDependencyEx().isGenerateJar()
                             && isChildOf(artifactEx.getDependencyTreeRoot(), current.getArtifact())) {
                     child = processed.get(i);
 
                     if (getLog().isDebugEnabled()) {
                         getLog().debug("found jar child: " + child + " for artifact: " + artifactEx); // NOI18N
                     }
 
                     break;
                 }
             }
 
             artifactEx.setClassPathJar(generateJar(artifactEx, child));
 
             if (virtualParent != null) {
                 artifactEx.setExtendedClassPathJar(generateJar(virtualParent, artifactEx));
             }
 
             if (artifactEx.getDependencyEx().getStarterConfiguration() != null) {
                 artifactEx.setStarterJar(generateStarterJar(artifactEx));
             }
         }
 
         if (artifactEx.getDependencyEx().isGenerateJnlp()) {
             if (codebase == null) {
                 throw new MojoExecutionException(
                     "if jnlp classpath generation is activated, you must provide a codebase"); // NOI18N
             }
 
             // search for already processed child artifacts and generate if found
             ArtifactEx child = null;
             for (int i = processed.size() - 1; i >= 0; --i) {
                 final ArtifactEx current = processed.get(i);
                 if (current.getDependencyEx().isGenerateJnlp()
                             && isChildOf(artifactEx.getDependencyTreeRoot(), current.getArtifact())) {
                     child = processed.get(i);
 
                     if (getLog().isDebugEnabled()) {
                         getLog().debug("found jnlp child: " + child + " for artifact: " + artifactEx); // NOI18N
                     }
 
                     break;
                 }
             }
 
             artifactEx.setClassPathJnlp(generateJnlp(artifactEx, child));
 
             if (virtualParent != null) {
                 artifactEx.setExtendedClassPathJnlp(generateJnlp(virtualParent, artifactEx));
             }
 
             if (artifactEx.getDependencyEx().getStarterConfiguration() != null) {
                 artifactEx.setStarterJnlp(generateStarterJnlp(artifactEx));
             }
         }
 
         processed.add(artifactEx);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifactEx  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoFailureException    DOCUMENT ME!
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private File generateStarterJar(final ArtifactEx artifactEx) throws MojoFailureException, MojoExecutionException {
         final StarterConfiguration starter = artifactEx.getDependencyEx().getStarterConfiguration();
 
         if (starter == null) {
             throw new MojoFailureException("starter configuration not present"); // NOI18N
         }
 
         final String mainClass = starter.getMainClass();
 
         if (mainClass == null) {
             throw new MojoFailureException("starter configuration needs main class definition"); // NOI18N
         }
 
         final StringBuilder classpath = new StringBuilder();
 
         final LocalConfiguration localConfiguration = starter.getLocalConfiguration();
         final File localDir;
         try {
             localDir = new File(generateStructure() + File.separator + getLocalDirectory(localConfiguration));
 
             if (getLog().isDebugEnabled()) {
                 getLog().debug("starter jar: using local dir: " + localDir); // NOI18N
             }
 
             if (localDir.exists()) {
                 if (!localDir.canRead()) {
                     throw new IOException("cannot read local dir: " + localDir);                                     // NOI18N
                 }
             } else {
                 if (getLog().isWarnEnabled()) {
                     getLog().warn(
                         "starter jar: the local dir is not present and will be created, thus jars cannot be added: " // NOI18N
                                 + localDir);
                 }
 
                 if (!localDir.mkdirs()) {
                     throw new IOException("cannot create local dir: " + localDir); // NOI18N
                 }
             }
         } catch (final IOException e) {
             final String message = "illegal local dir: " + getLocalDirectory(localConfiguration); // NOI18N
             getLog().error(message, e);
 
             throw new MojoExecutionException(message, e);
         }
 
         final List<String> localFileNames;
         if (localConfiguration.getJarNames() == null) {
             localFileNames = null;
         } else {
             localFileNames = new ArrayList(Arrays.asList(localConfiguration.getJarNames()));
         }
 
         final File[] localJars = localDir.listFiles(new FileFilter() {
 
                     @Override
                     public boolean accept(final File file) {
                         if (!file.isFile()) {
                             return false;
                         } else if (localFileNames == null) {
                             return file.getName().toLowerCase().endsWith(".jar"); // NOI18N
                         } else {
                             return localFileNames.remove(file.getName());
                         }
                     }
                 });
 
         if ((localFileNames != null) && !localFileNames.isEmpty()) {
             final StringBuilder sb = new StringBuilder();
             for (final String s : localFileNames) {
                 sb.append(s).append(", ");
             }
             sb.delete(sb.length() - 2, sb.length());
 
             getLog().warn(
                 "The following jars are not included in the starter classpath, because they are not present: " // NOI18N
                         + sb.toString());
         }
 
         for (final File localJar : localJars) {
             if (!isSigned(localJar)) {
                 signJar(localJar);
             }
 
             classpath.append(localJar.getAbsolutePath()).append(' ');
         }
 
         if (artifactEx.getExtendedClassPathJar() == null) {
             classpath.append(artifactEx.getClassPathJar().getAbsolutePath());
         } else {
             classpath.append(artifactEx.getExtendedClassPathJar().getAbsolutePath());
         }
 
         // Generate Manifest and jar File
         final Manifest manifest = new Manifest();
         manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); // NOI18N
         manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath.toString());
         manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
 
         JarOutputStream target = null;
         try {
             final String jarName = artifactEx.getArtifact().getArtifactId() + "-"                                    // NOI18N
                         + artifactEx.getArtifact().getBaseVersion() + "-" + CLASSIFIER_STARTER + "." + FILE_EXT_JAR; // NOI18N;
 
             // write the jar file
             final File jar = getOutputFile(jarName, starter.getStarterAlias());
             target = new JarOutputStream(new FileOutputStream(jar), manifest);
 
             // close the stream to be able to sign the jar
             target.close();
             if (!isSigned(jar)) {
                 signJar(jar);
             }
 
             if (getLog().isInfoEnabled()) {
                 getLog().info("generated starter jar: " + jar); // NOI18N
             }
 
             return jar;
         } catch (final Exception ex) {
             final String message = "cannot generate starter jar for artifact: " + artifactEx; // NOI18N
             getLog().error(message, ex);
 
             throw new MojoExecutionException(message, ex);
         } finally {
             if (target != null) {
                 try {
                     target.close();
                 } catch (final IOException e) {
                     if (getLog().isWarnEnabled()) {
                         getLog().warn("cannot close jar output stream", e); // NOI18N
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifactEx  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoFailureException    DOCUMENT ME!
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private Jnlp generateStarterJnlp(final ArtifactEx artifactEx) throws MojoFailureException, MojoExecutionException {
         final StarterConfiguration starter = artifactEx.getDependencyEx().getStarterConfiguration();
 
         if (starter == null) {
             throw new MojoFailureException("starter configuration not present"); // NOI18N
         }
 
         final String mainClass = starter.getMainClass();
 
         if (mainClass == null) {
             throw new MojoFailureException("starter configuration needs main class definition"); // NOI18N
         }
 
         final ObjectFactory objectFactory = new ObjectFactory();
         final Jnlp jnlp = objectFactory.createJnlp();
         jnlp.setSpec("1.0+"); // NOI18N
         final Information info = objectFactory.createInformation();
 
         final MavenProject artifactProject;
         try {
             artifactProject = resolveProject(artifactEx.getArtifact());
         } catch (final ProjectBuildingException ex) {
             final String message = "cannot build artifact project from artifact: " + artifactEx; // NOI18N
             getLog().error(message, ex);
 
             throw new MojoExecutionException(message, ex);
         }
 
         // set jnlp info
         if ((starter.getTitle() != null) && !starter.getTitle().isEmpty()) {
             info.setTitle(starter.getTitle());
         } else {
             info.setTitle(artifactProject.getName() + " Starter"); // NOI18N
         }
         if (vendor != null) {
             info.setVendor(vendor);
         }
         if (homepage != null) {
             final Homepage hp = objectFactory.createHomepage();
             hp.setHref(homepage);
             info.setHomepage(hp);
         }
         jnlp.getInformation().add(info);
 
         final Resources resources = objectFactory.createResources();
         final List resourceList = resources.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage();
 
         // set java properties
         final Java java = starter.getJava();
         final J2Se j2se = objectFactory.createJ2Se();
         j2se.setVersion(java.getVersion());
         j2se.setInitialHeapSize(java.getInitialHeapSize().toLowerCase());
         j2se.setMaxHeapSize(java.getMaximalHeapSize().toLowerCase());
         if ((java.getJvmArgs() != null) && !java.getJvmArgs().isEmpty()) {
             j2se.setJavaVmArgs(java.getJvmArgs());
         }
         resourceList.add(j2se);
 
         // add properties
         if (starter.getProperties() != null) {
             for (final Entry entry : starter.getProperties().entrySet()) {
                 final Property property = objectFactory.createProperty();
                 property.setName((String)entry.getKey());
                 property.setValue((String)entry.getValue());
 
                 resourceList.add(property);
             }
         }
 
         // add all local jars
         final LocalConfiguration localConfiguration = starter.getLocalConfiguration();
         if (localConfiguration.getJarNames() == null) {
             getLog().warn("no local jar names provided, not adding local jars");                                // NOI18N
         } else {
             final String localBase = generateHRef(LIB_DIR + "/" + getLocalDirectory(localConfiguration)) + "/"; // NOI18N
 
             if (getLog().isDebugEnabled()) {
                 getLog().debug("starter jnlp: using local base: " + localBase); // NOI18N
             }
 
             for (final String name : localConfiguration.getJarNames()) {
                 final Jar jar = objectFactory.createJar();
                 jar.setHref(localBase + name);
 
                 resourceList.add(jar);
             }
         }
 
         // add the extension to the main classpath jar
         final Extension extension = objectFactory.createExtension();
         if (artifactEx.getExtendedClassPathJnlp() == null) {
             extension.setHref(artifactEx.getClassPathJnlp().getHref());
         } else {
             extension.setHref(artifactEx.getExtendedClassPathJnlp().getHref());
         }
         resourceList.add(extension);
 
         // resources are finished
         jnlp.getResources().add(resources);
 
         // security parameters
         final Security security = objectFactory.createSecurity();
         final AllPermissions allPermissions = objectFactory.createAllPermissions();
         security.setAllPermissions(allPermissions);
 
         // security is finished
         jnlp.setSecurity(security);
 
         // application section
         final ApplicationDesc applicationDesc = objectFactory.createApplicationDesc();
         applicationDesc.setMainClass(mainClass);
 
         if (starter.getArguments() != null) {
             for (final String arg : starter.getArguments()) {
                 final Argument argument = objectFactory.createArgument();
                 argument.setvalue(generateHRef(arg));
 
                 applicationDesc.getArgument().add(argument);
             }
         }
 
         // application section is finished
         jnlp.getApplicationDescOrAppletDescOrComponentDescOrInstallerDesc().add(applicationDesc);
 
         final String jnlpName = artifactEx.getArtifact().getArtifactId() + "-" // NOI18N
                     + artifactEx.getArtifact().getBaseVersion()
                     + "-" + CLASSIFIER_STARTER + "." + FILE_EXT_JNLP;          // NOI18N
 
         return writeJnlp(jnlp, jnlpName, starter.getStarterAlias());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifactEx  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private ArtifactEx createVirtualArtifact(final ArtifactEx artifactEx) throws MojoExecutionException {
         // prepare virtual project for additional dependencies if present
         final Dependency[] additionalDeps = artifactEx.getDependencyEx().getAdditionalDependencies();
 
         final ArtifactEx extParent;
         if ((additionalDeps == null) || (additionalDeps.length == 0)) {
             if (getLog().isDebugEnabled()) {
                 getLog().debug(
                     "no additional dependencies present, skip virtual artifact generation for artifact: " // NOI18N
                             + artifactEx);
             }
 
             extParent = null;
         } else {
             if (getLog().isDebugEnabled()) {
                 getLog().debug("generating virtual artifact for artifact: " + artifactEx); // NOI18N
             }
 
             final Model model = createModel(artifactEx);
             final MavenProject extProject = new MavenProject(model);
 
             extProject.setArtifact(factory.createBuildArtifact(
                     extProject.getGroupId(),
                     extProject.getArtifactId(),
                     extProject.getVersion(),
                     extProject.getPackaging()));
 
             extParent = new ArtifactEx(extProject.getArtifact());
             extParent.setVirtualProject(extProject);
         }
 
         return extParent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   config  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getLocalDirectory(final LocalConfiguration config) {
         if (config.getDirectory() == null) {
             return LocalConfiguration.DEFAULT_LOCAL_DIR + accountExtension;
         } else {
             return config.getDirectory();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifactEx  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private Model createModel(final ArtifactEx artifactEx) throws MojoExecutionException {
         final Model model = new Model();
         final Artifact artifact = artifactEx.getArtifact();
 
         final MavenProject artifactProject;
         try {
             artifactProject = resolveProject(artifact);
         } catch (ProjectBuildingException ex) {
             final String message = "cannot create maven project from artifact: " + artifact; // NOI18N
             getLog().error(message, ex);
 
             throw new MojoExecutionException(message, ex);
         }
 
         model.setParent(artifactProject.getModel().getParent());
 
         model.setGroupId(artifactProject.getGroupId());
         model.setArtifactId(artifactProject.getArtifactId() + "-ext"); // NOI18N
         model.setVersion(artifactProject.getVersion());
         model.setName(artifactProject.getName() + " Extended");        // NOI18N
 
         final Dependency[] additionalDeps = artifactEx.getDependencyEx().getAdditionalDependencies();
         if (additionalDeps != null) {
             for (final Dependency dep : additionalDeps) {
                 model.addDependency(dep);
             }
         }
 
         return model;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   parent  DOCUMENT ME!
      * @param   child   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      * @throws  IllegalStateException   DOCUMENT ME!
      */
     private File generateJar(final ArtifactEx parent, final ArtifactEx child) throws MojoExecutionException {
         final Artifact parentArtifact = parent.getArtifact();
         final boolean virtual = parent.isVirtual();
 
         if (virtual && (parent.getVirtualProject() == null)) {
             throw new IllegalStateException(
                 "if we deal with a virtual artifact, there must be a virtual project attached"); // NOI18N
         }
 
         final StringBuilder classpath;
         // we don't append the parent artifact's file path if the artifact is virtual
         if (virtual) {
             classpath = new StringBuilder();
         } else {
             classpath = new StringBuilder(parentArtifact.getFile().getAbsolutePath());
             classpath.append(' ');
         }
 
         final ArtifactFilter filter;
         if ((child == null) || (child.getClassPathJar() == null)) {
             filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
         } else {
             filter = new ChildDependencyFilter(child);
             classpath.append(child.getClassPathJar().getAbsolutePath()).append(' ');
         }
 
         JarOutputStream target = null;
         try {
             final Set<Artifact> resolved;
             if (virtual) {
                 resolved = resolveArtifacts(parent.getVirtualProject(), Artifact.SCOPE_RUNTIME, filter);
             } else {
                 resolved = resolveArtifacts(parentArtifact, Artifact.SCOPE_RUNTIME, filter);
             }
 
             for (final Artifact dep : resolved) {
                 if (!isSigned(dep.getFile())) {
                     signJar(dep.getFile());
                 }
                 classpath.append(dep.getFile().getAbsolutePath()).append(' ');
             }
 
             // Generate Manifest and jar File
             final Manifest manifest = new Manifest();
             manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); // NOI18N
             manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath.toString());
 
             final String jarname = parentArtifact.getArtifactId() + "-" + parentArtifact.getBaseVersion() // NOI18N
                         + "-" + CLASSIFIER_CLASSPATH + "." + FILE_EXT_JAR;                                // NOI18N
 
             // write the jar file
             final File jar = getOutputFile(jarname, null);
             target = new JarOutputStream(new FileOutputStream(jar), manifest);
 
             // close the stream to be able to sign the jar
             target.close();
 
             if (!isSigned(jar)) {
                 signJar(jar);
             }
 
             if (getLog().isInfoEnabled()) {
                 getLog().info("generated jar: " + jar); // NOI18N
             }
 
             return jar;
         } catch (final Exception ex) {
             final String message = "cannot generate jar for artifact: " + parent + " || child: " + child; // NOI18N
             getLog().error(message, ex);
 
             throw new MojoExecutionException(message, ex);
         } finally {
             if (target != null) {
                 try {
                     target.close();
                 } catch (final IOException e) {
                     if (getLog().isWarnEnabled()) {
                         getLog().warn("cannot close jar output stream", e); // NOI18N
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  toSign  DOCUMENT ME!
      */
     private void signJar(final File toSign) {
         final String groupId = MojoExecutor.groupId("org.apache.maven.plugins");     // NOI18N
         final String artifactId = MojoExecutor.artifactId("maven-jarsigner-plugin"); // NOI18N
         final String version = MojoExecutor.version("1.2");                          // NOI18N
         final Plugin plugin = MojoExecutor.plugin(groupId, artifactId, version);
 
         final String goal = MojoExecutor.goal("sign"); // NOI18N
 
         final String keystorePath = project.getProperties().getProperty("de.cismet.keystore.path"); // NOI18N
         final String keystorePass = project.getProperties().getProperty("de.cismet.keystore.pass"); // NOI18N
 
         if ((keystorePass == null) || (keystorePath == null)) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn(
                     "Cannot sign jar because either de.cismet.keystore.path or de.cismet.keystore.pass is not set"); // NOI18N
             }
 
             return;
         }
 
         final MojoExecutor.Element archive = MojoExecutor.element("archive", toSign.getAbsolutePath()); // NOI18N
         final MojoExecutor.Element keystore = MojoExecutor.element("keystore", keystorePath);           // NOI18N
         final MojoExecutor.Element storepass = MojoExecutor.element("storepass", keystorePass);         // NOI18N
         final MojoExecutor.Element alias = MojoExecutor.element("alias", "cismet");                     // NOI18N
         final Xpp3Dom configuration = MojoExecutor.configuration(archive, keystore, storepass, alias);
 
         final MojoExecutor.ExecutionEnvironment environment = MojoExecutor.executionEnvironment(
                 project,
                 session,
                 pluginManager);
 
         if (getLog().isInfoEnabled()) {
             getLog().info("Signing jar: " + toSign);
         }
 
         try {
             MojoExecutor.executeMojo(plugin, goal, configuration, environment);
         } catch (final MojoExecutionException ex) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("Cannot sign jar", ex); // NOI18N
             }
         }
     }
 
     /**
      * We have to use the (deprecated) maven jar plugin to verify the signature because the jarsigner plugin does not
      * support the errorWhenNotSigned option. Maybe some later version... http://jira.codehaus.org/browse/MJARSIGNER-18
      *
      * @param   toSign  the file to verify
      *
      * @return  true if the given file is signed, false in any other case
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private boolean isSigned(final File toSign) {
         if (toSign == null) {
             throw new IllegalArgumentException("toSign file must not be null"); // NOI18N
         }
 
         if (getLog().isInfoEnabled()) {
             getLog().info("verifying signature for: " + toSign); // NOI18N
         }
 
         // the fastest way out, avoids multiple checks on the same file
         if (verified.contains(toSign)) {
             if (getLog().isInfoEnabled()) {
                 getLog().info("signature verified: " + toSign); // NOI18N
             }
 
             return true;
         }
 
         final String keystorePath = project.getProperties().getProperty("de.cismet.keystore.path"); // NOI18N
         final String keystorePass = project.getProperties().getProperty("de.cismet.keystore.pass"); // NOI18N
 
         if ((keystorePass == null) || (keystorePath == null)) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn(
                     "Cannot verify signature because either de.cismet.keystore.path or de.cismet.keystore.pass is not set"); // NOI18N
             }
 
             return false;
         }
 
         try {
             final JarInputStream jis = new JarInputStream(new BufferedInputStream(new FileInputStream(toSign)), true);
             final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
             keystore.load(new BufferedInputStream(new FileInputStream(keystorePath)), keystorePass.toCharArray());
             final Certificate cismet = keystore.getCertificate("cismet"); // NOI18N
             final PublicKey key = cismet.getPublicKey();
 
             JarEntry entry;
             while ((entry = jis.getNextJarEntry()) != null) {
                 // read from the stream to ensure the presence of the certs if any
                 final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 int byteRead;
                 while ((byteRead = jis.read()) != -1) {
                     baos.write(byteRead);
                 }
 
                 final Certificate[] certs = entry.getCertificates();
                 if (certs == null) {
                     if (entry.getName().endsWith(".class")) {
                         if (getLog().isWarnEnabled()) {
                             getLog().warn("class file not signed: " + entry + " | " + toSign); // NOI18N
                         }
 
                         // bail out, signature check failed
                         return false;
                     } else {
                         if (getLog().isDebugEnabled()) {
                             getLog().debug("no certs for non-class entry, skipping: " + entry); // NOI18N
                         }
                     }
                 } else {
                     boolean isVerified = false;
                     for (final Certificate cert : certs) {
                         if (cert.equals(cismet)) {
                             try {
                                 cert.verify(key);
                                 isVerified = true;
 
                                 // we can get outta here
                                 break;
                             } catch (final Exception e) {
                                 if (getLog().isDebugEnabled()) {
                                     getLog().debug("certificate of entry cannot be verified: " // NOI18N
                                                 + cert + " | entry: " + entry + " | toSign: " + toSign, // NOI18N
                                         e);
                                 }
                             }
                         } else {
                             if (getLog().isDebugEnabled()) {
                                 getLog().debug("skipping non-cismet cert: " + cert + " | entry: " + entry // NOI18N
                                             + " | toSign: " + toSign);              // NOI18N
                             }
                         }
                     }
 
                     if (!isVerified) {
                         if (getLog().isWarnEnabled()) {
                             getLog().warn("cannot verify entry: " + entry + " | toSign: " + toSign); // NOI18N
                         }
 
                         return false;
                     }
                 }
             }
         } catch (final Exception e) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("cannot verify signature: " + toSign, e); // NOI18N
             }
 
             return false;
         }
 
         if (getLog().isInfoEnabled()) {
             getLog().info("signature verified: " + toSign); // NOI18N
         }
 
         verified.add(toSign);
 
         return true;
 
 //        final String groupId = MojoExecutor.groupId("org.apache.maven.plugins"); // NOI18N
 //        final String artifactId = MojoExecutor.artifactId("maven-jar-plugin");   // NOI18N
 //        final String version = MojoExecutor.version("2.3.2");                    // NOI18N
 //        final Plugin plugin = MojoExecutor.plugin(groupId, artifactId, version);
 //
 //        final String goal = MojoExecutor.goal("sign-verify"); // NOI18N
 //
 //        final MojoExecutor.Element archive = MojoExecutor.element("jarPath", toSign.getAbsolutePath()); // NOI18N
 //        // curiously the certs option of the jarsigner is less verbose than an execution without it
 //        final MojoExecutor.Element certs = MojoExecutor.element("checkCerts", String.valueOf(true)); // NOI18N
 //
 //        final Xpp3Dom configuration = MojoExecutor.configuration(archive, certs);
 //
 //        final MojoExecutor.ExecutionEnvironment environment = MojoExecutor.executionEnvironment(
 //                project,
 //                session,
 //                pluginManager);
 //
 //        try {
 //            MojoExecutor.executeMojo(plugin, goal, configuration, environment);
 //
 //            return true;
 //        } catch (final Exception e) {
 //            // most likely the execution failed because the signature is not present
 //            if (getLog().isDebugEnabled()) {
 //                getLog().debug("cannot check jar signature: " + toSign, e); // NOI18N
 //            }
 //
 //            return false;
 //        }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   parent  DOCUMENT ME!
      * @param   child   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      * @throws  IllegalStateException   DOCUMENT ME!
      */
     private Jnlp generateJnlp(final ArtifactEx parent, final ArtifactEx child) throws MojoExecutionException {
         final Artifact parentArtifact = parent.getArtifact();
         final boolean virtual = parent.isVirtual();
 
         if (virtual && (parent.getVirtualProject() == null)) {
             throw new IllegalStateException(
                 "if we deal with a virtual artifact, there must be a virtual project attached"); // NOI18N
         }
 
         final ObjectFactory objectFactory = new ObjectFactory();
         final Jnlp jnlp = objectFactory.createJnlp();
         jnlp.setSpec("1.0+"); // NOI18N
         final Information info = objectFactory.createInformation();
 
         final Resources resources = objectFactory.createResources();
         final List jars = resources.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage();
 
         final MavenProject artifactProject;
         if (virtual) {
             artifactProject = parent.getVirtualProject();
         } else {
             try {
                 artifactProject = resolveProject(parentArtifact);
             } catch (final ProjectBuildingException ex) {
                 final String message = "cannot build artifact project from artifact: " + parent; // NOI18N
                 getLog().error(message, ex);
 
                 throw new MojoExecutionException(message, ex);
             }
 
             final Jar self = objectFactory.createJar();
 
             self.setHref(generateJarHRef(parentArtifact));
 
             jars.add(self);
         }
 
         assert artifactProject != null : "artifact project must not be null"; // NOI18N
 
         // set jnlp info
         info.setTitle(artifactProject.getName());
         if (vendor != null) {
             info.setVendor(vendor);
         }
         if (homepage != null) {
             final Homepage hp = objectFactory.createHomepage();
             hp.setHref(homepage);
             info.setHomepage(hp);
         }
         jnlp.getInformation().add(info);
 
         final ArtifactFilter filter;
         if ((child == null) || (child.getClassPathJnlp() == null)) {
             filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
         } else {
             filter = new ChildDependencyFilter(child);
 
             // add the child jnlp extension
             final Extension extension = objectFactory.createExtension();
             extension.setHref(child.getClassPathJnlp().getHref());
 
             jars.add(extension);
         }
 
         final Set<Artifact> resolved;
         try {
             resolved = resolveArtifacts(artifactProject, Artifact.SCOPE_RUNTIME, filter);
         } catch (final Exception ex) {
             final String message = "cannot resolve artifacts for artifact: " + parent; // NOI18N
             getLog().error(message, ex);
 
             throw new MojoExecutionException(message, ex);
         }
 
         for (final Artifact dep : resolved) {
             if (!isSigned(dep.getFile())) {
                 signJar(dep.getFile());
             }
 
             final Jar jar = objectFactory.createJar();
             jar.setHref(generateJarHRef(dep));
 
             jars.add(jar);
         }
 
         jnlp.getResources().add(resources);
 
         // security parameters are needed even for classpath jnlps
         final Security security = objectFactory.createSecurity();
         final AllPermissions allPermissions = objectFactory.createAllPermissions();
         security.setAllPermissions(allPermissions);
 
         // security is finished
         jnlp.setSecurity(security);
 
         // add the necessary component-desc
         final ComponentDesc componentdesc = objectFactory.createComponentDesc();
         jnlp.getApplicationDescOrAppletDescOrComponentDescOrInstallerDesc().add(componentdesc);
 
         final String jnlpName = parentArtifact.getArtifactId() + "-" + parentArtifact.getBaseVersion() // NOI18N
                     + "-" + CLASSIFIER_CLASSPATH + "." + FILE_EXT_JNLP;                                // NOI18N
 
         return writeJnlp(jnlp, jnlpName, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   jnlp      DOCUMENT ME!
      * @param   jnlpName  DOCUMENT ME!
      * @param   alias     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private Jnlp writeJnlp(final Jnlp jnlp, final String jnlpName, final String alias) throws MojoExecutionException {
         try {
             final String trimmedCodebase = trimSlash(codebase.toString());
             jnlp.setCodebase(trimmedCodebase);
             jnlp.setHref(generateSelfHRef(codebase, jnlpName, alias));
 
             final File outFile = getOutputFile(jnlpName, alias);
             final JAXBContext jaxbContext = JAXBContext.newInstance("de.cismet.cids.jnlp"); // NOI18N
             final Marshaller marshaller = jaxbContext.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");                      // NOI18N
             marshaller.marshal(jnlp, outFile);
 
             if (getLog().isInfoEnabled()) {
                 getLog().info("generated jnlp: " + outFile); // NOI18N
             }
 
             return jnlp;
         } catch (final Exception e) {
             final String message = "cannot create classpath jnlp: " + jnlpName; // NOI18N
             getLog().error(message, e);                                         // NOI18N
 
             throw new MojoExecutionException(message, e);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifacts  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private List<ArtifactEx> determineProcessingOrder(final Set<ArtifactEx> artifacts) throws MojoExecutionException {
         final LinkedList<ArtifactEx> list = new LinkedList<ArtifactEx>();
 
         for (final ArtifactEx artifactEx : artifacts) {
             try {
                 final MavenProject artifactProject = resolveProject(artifactEx.getArtifact());
                 final DependencyNode root = dependencyTreeBuilder.buildDependencyTree(
                         artifactProject,
                         localRepository,
                         factory,
                         artifactMetadataSource,
                         new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME),
                         artifactCollector);
                 artifactEx.setDependencyTreeRoot(root);
 
                 int insertionIndex = 0;
                 for (int i = 0; i < list.size(); ++i) {
                     if (isChildOf(root, list.get(i).getArtifact())) {
                         insertionIndex = i + 1;
                     }
                 }
 
                 if (getLog().isDebugEnabled()) {
                     getLog().debug(insertionIndex + " is insertion index for artifact: " + artifactEx);
                 }
 
                 list.add(insertionIndex, artifactEx);
             } catch (final ProjectBuildingException ex) {
                 final String message = "cannot resolve maven project for artifact: " + artifactEx.getArtifact(); // NOI18N
                 getLog().error(message, ex);
 
                 throw new MojoExecutionException(message, ex);
             } catch (final DependencyTreeBuilderException ex) {
                 final String message = "cannot build dependency tree for artifact: " + artifactEx.getArtifact(); // NOI18N
                 getLog().error(message, ex);
 
                 throw new MojoExecutionException(message, ex);
             }
         }
 
         return list;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   current  DOCUMENT ME!
      * @param   toCheck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean isChildOf(final DependencyNode current, final Artifact toCheck) {
         // DFS
         for (final Object o : current.getChildren()) {
             final DependencyNode child = (DependencyNode)o;
 
             if (child.getArtifact().equals(toCheck)) {
                 return true;
             } else if (isChildOf(child, toCheck)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifact  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ArtifactEx getExtendedArtifact(final Artifact artifact) {
         if ((artifact != null) && (dependencyConfiguration != null)) {
             for (final DependencyEx dep : dependencyConfiguration) {
                 if (dep.getGroupId().equals(artifact.getGroupId())
                             && dep.getArtifactId().equals(artifact.getArtifactId())) {
                     return new ArtifactEx(artifact, dep);
                 }
             }
         }
        
        if(getLog().isWarnEnabled()){
            getLog().warn("extended dependency configuration not found, using defaults: " + artifact); // NOI18N
        }
 
         return new ArtifactEx(artifact);
     }
 
     /**
      * Generates the basic lib structure consisting of a lib folder. The lib folder is created within the
      * outputDirectory.
      *
      * @return  the generated lib folder <code>File</code>
      *
      * @throws  IOException  if any of the folders cannot be created
      */
     private File generateStructure() throws IOException {
         final File libDir = new File(outputDirectory, LIB_DIR);
         if (!libDir.exists() && !libDir.isDirectory() && !libDir.mkdirs()) {
             throw new IOException("could not create lib folder: " + libDir); // NOI18N
         }
 
         return libDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private File generateStarterDir() throws IOException {
         final File starterDir = new File(generateStructure(), STARTER_DIR + accountExtension);
         if (!starterDir.exists() && !starterDir.isDirectory() && !starterDir.mkdir()) {
             throw new IOException("could not create starter folder: " + starterDir); // NOI18N
         }
 
         return starterDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private File generateClasspathDir() throws IOException {
         final File classpath = new File(generateStructure(), CLASSPATH_DIR + accountExtension);
         if (!classpath.exists() && !classpath.isDirectory() && !classpath.mkdir()) {
             throw new IOException("could not create starter folder: " + classpath); // NOI18N
         }
 
         return classpath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private File generateClientDir() throws IOException {
         final File clientDir = new File(outputDirectory, CLIENT_DIR + File.separator + accountExtension.toLowerCase());
         if (!clientDir.exists() && !clientDir.isDirectory() && !clientDir.mkdirs()) {
             throw new IOException("could not create client folder: " + clientDir); // NOI18N
         }
 
         return clientDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   filename  DOCUMENT ME!
      * @param   alias     starterAlias DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException               DOCUMENT ME!
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private File getOutputFile(final String filename, final String alias) throws IOException {
         final String name = filename.substring(0, filename.lastIndexOf('.'));
         final String ext = filename.substring(filename.lastIndexOf('.') + 1);
 
         if (name.endsWith(CLASSIFIER_CLASSPATH)) {
             return new File(generateClasspathDir(), filename);
         } else if (name.endsWith(CLASSIFIER_STARTER)) {
             if (FILE_EXT_JAR.equals(ext)) {
                 if (alias == null) {
                     return new File(generateStarterDir(), filename);
                 } else {
                     return new File(generateStarterDir(), alias + "." + FILE_EXT_JAR);    // NOI18N
                 }
             } else if (FILE_EXT_JNLP.equals(ext)) {
                 if (alias == null) {
                     return new File(generateClientDir(), filename);
                 } else {
                     return new File(generateClientDir(), alias + "." + FILE_EXT_JNLP);    // NOI18N
                 }
             } else {
                 throw new IllegalArgumentException("unsupported file extension: " + ext); // NOI18N
             }
         } else {
             throw new IllegalArgumentException("unsupported classifier, filename: " + filename); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   codebase  DOCUMENT ME!
      * @param   jnlpName  DOCUMENT ME!
      * @param   alias     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException               DOCUMENT ME!
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private String generateSelfHRef(final URL codebase, final String jnlpName, final String alias) throws IOException {
         if (codebase == null) {
             throw new IllegalArgumentException("codebase must not be null"); // NOI18N
         }
 
         final StringBuilder sb = new StringBuilder(trimSlash(codebase.toString()));
 
         if ('/' != sb.charAt(sb.length() - 1)) {
             sb.append('/');
         }
 
         final String outFile = getOutputFile(jnlpName, alias).getAbsolutePath()
                     .replace(outputDirectory.getAbsolutePath(), "") // NOI18N
             .replace(File.separator, "/");                          // NOI18N
 
         sb.append(trimSlash(outFile));
 
         return sb.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     private String getM2BaseURL() {
         String ret = null;
 
         try {
             ret = new URL(m2codebase).toString();
         } catch (final MalformedURLException e) {
             if (codebase == null) {
                 if (getLog().isDebugEnabled()) {
                     getLog().debug("codebase is not provided and m2codebase is not absolute", e); // NOI18N
                 }
             } else {
                 final StringBuilder sb = new StringBuilder(trimSlash(codebase.toString()));
 
                 sb.append('/');
                 sb.append(trimSlash(m2codebase));
 
                 ret = sb.toString();
             }
         }
 
         if (ret == null) {
             throw new IllegalStateException("cannot create m2 base url"); // NOI18N
         } else {
             return trimSlash(ret);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toTrim  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String trimSlash(final String toTrim) {
         final StringBuilder sb = new StringBuilder(toTrim);
 
         while ('/' == sb.charAt(sb.length() - 1)) {
             sb.deleteCharAt(sb.length() - 1);
         }
 
         while ('/' == sb.charAt(0)) {
             sb.deleteCharAt(0);
         }
 
         return sb.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   artifact  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String generateJarHRef(final Artifact artifact) {
         final String m2baseurl = getM2BaseURL();
 
         final StringBuilder sb = new StringBuilder(m2baseurl);
 
         sb.append('/');
         sb.append(artifact.getGroupId().replace(".", "/")); // NOI18N
         sb.append('/');
         sb.append(artifact.getArtifactId());
         sb.append('/');
         sb.append(artifact.getBaseVersion());
         sb.append('/');
         sb.append(artifact.getFile().getName());
 
         return sb.toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   url  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     private String generateHRef(final String url) throws MojoExecutionException {
         try {
             return new URL(url).toString();
         } catch (final MalformedURLException e) {
             if (getLog().isDebugEnabled()) {
                 getLog().debug("given url is considered tobe relative: " + url, e); // NOI18N
             }
 
             final String trimmedCodebase = trimSlash(codebase.toString());
             final String trimmedPath = trimSlash(url);
 
             return trimmedCodebase + "/" + trimmedPath; // NOI18N
         }
     }
 }
