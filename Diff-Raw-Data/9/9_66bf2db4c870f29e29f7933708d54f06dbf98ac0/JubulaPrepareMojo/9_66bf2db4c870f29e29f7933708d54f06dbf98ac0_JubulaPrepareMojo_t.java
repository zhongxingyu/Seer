 package org.mule.tooling.jubula;
 
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.JUBULA_BOOTSTRAP_VERSION;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.RCPWORKSPACE_DIRECTORY_NAME;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.RESULTS_DIRECTORY_NAME;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.initializeContext;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.pathToJubulaInstallationDirectory;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.pathToJubulaPluginsDirectory;
 import static org.mule.tooling.jubula.JubulaMavenPluginContext.pathToServerPluginsDirectory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.repository.MavenArtifactRepository;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
 import org.codehaus.plexus.util.FileUtils;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResolutionException;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 
 /**
  * Goal that runs Jubula Functional Tests.
  * 
  * @goal prepare
  * @phase pre-integration-test
  */
 public class JubulaPrepareMojo extends AbstractMojo {
 
 	private static final String ORG_ECLIPSE_JUBULA_RC_RCP_1_2_1_201206131127_JAR = "org.eclipse.jubula.rc.rcp_1.2.1.201206131127.jar";
 
 	/**
 	 * 
 	 * Project being built.
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 */
 	private MavenProject project;
 
 	/**
 	 * The entry point to Aether, i.e. the component doing all the work.
 	 * 
 	 * @component
 	 */
 	private RepositorySystem repoSystem;
 
 	/**
 	 * The current repository/network configuration of Maven.
 	 * 
 	 * @parameter default-value="${repositorySystemSession}"
 	 * @readonly
 	 */
 	private RepositorySystemSession repoSession;
 
 	/**
 	 * The project's remote repositories to use for the resolution of plugins
 	 * and their dependencies.
 	 * 
 	 * @parameter default-value="${project.remoteArtifactRepositories}"
 	 * @readonly
 	 */
 	private List<MavenArtifactRepository> remoteRepos;
 
 	/**
 	 * Where the .js files will be located for running.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 * @readonly
 	 * @required
 	 */
 	private File buildDirectory;
 
 	/**
 	 * RCP target directory.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String rcpTargetDirectory;
 
 	/**
 	 * @parameter
 	 */
 	private List<Dependency> jubulaPlugins;
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		getLog().info("Prepare Jubula Mojo starting");
 
 		initializeContext(buildDirectory);
 
 		createTestWorkingDirectories();
 		prepareJubulaInstallation();
 		copyUserDefinedPluginsToRcp();
 	}
 
 	private void prepareJubulaInstallation() throws MojoExecutionException {
 		final File jubula = fetchArtifact(getJubulaBootsrapDependency());
		extract(jubula, buildDirectory);
 
 		copyServerJubulaPluginsToJubulaPlugins();
 	}
 
 	private void createTestWorkingDirectories() {
 		createFile(RCPWORKSPACE_DIRECTORY_NAME);
 		createFile(RESULTS_DIRECTORY_NAME);
 	}
 
 	private void copyServerJubulaPluginsToJubulaPlugins()
 			throws MojoExecutionException {
 		try {
 			FileUtils.copyDirectory( //
 					new File(pathToServerPluginsDirectory()), //
 					new File(pathToJubulaPluginsDirectory()));
 
 		} catch (final IOException e) {
 			throw new MojoExecutionException(
 					"Error copying plugins. Verify that the RCP target directory is correct.",
 					e);
 		}
 	}
 
 	private void copyUserDefinedPluginsToRcp() throws MojoExecutionException {
 		try {
 			final File pluginsDirectory = new File(rcpTargetDirectory,
 					"plugins");
 
 			// TODO - Until the RC is uploaded to Maven or shipped with Jubula
 			// bootstrap, we are incluiding it here - miguel
 			final File out = new File(pluginsDirectory,
 					ORG_ECLIPSE_JUBULA_RC_RCP_1_2_1_201206131127_JAR);
 			IOUtils.copy(getRemoteControlArtifactStream(),
 					new FileOutputStream(out));
 
 			for (final File artifact : fetchArtifacts()) {
 				FileUtils.copyFileToDirectory(artifact, pluginsDirectory);
 				getLog().info(
 						"Wrote artifact " + artifact.getAbsolutePath()
 								+ " to plugins directory");
 			}
 
 		} catch (final IOException e) {
 			throw new MojoExecutionException(
 					"Error copying plugins. Verify that the RCP target directory is correct.",
 					e);
 		}
 	}
 
 	protected InputStream getRemoteControlArtifactStream() {
 		return getClass().getClassLoader().getResourceAsStream(
 				ORG_ECLIPSE_JUBULA_RC_RCP_1_2_1_201206131127_JAR);
 	}
 
 	private List<File> fetchArtifacts() throws MojoExecutionException {
 		final List<Dependency> allPlugins = getAllPlugins();
 
 		final List<File> artifacts = new ArrayList<File>(allPlugins.size());
 		for (final Dependency dependency : allPlugins) {
 			final File file = fetchArtifact(dependency);
 			artifacts.add(file);
 		}
 
 		return artifacts;
 	}
 
 	protected List<Dependency> getAllPlugins() {
 		final ArrayList<Dependency> allPlugins = new ArrayList<Dependency>(
 				jubulaPlugins);
 		// TODO - Someone should upload this dependency to Maven or at least
 		// copy it to bootstrap distribution. - miguel
 		// allPlugins.add(remoteControlDependency());
 		return allPlugins;
 	}
 
 	@SuppressWarnings("unused")
 	private Dependency getRemoteControlDependency() {
 		// TODO initialize dependency bean
 		return createDependency("", "", "", "");
 	}
 
 	private Dependency getJubulaBootsrapDependency() {
 		return createDependency( //
 				"org.mule.tooling", //
 				"jubula-bootstrap", //
 				JUBULA_BOOTSTRAP_VERSION, //
 				"zip");
 	}
 
 	protected Dependency createDependency(final String groupId,
 			final String artifactId, final String version, final String type) {
 		final Dependency dependency = new Dependency();
 		dependency.setGroupId(groupId);
 		dependency.setArtifactId(artifactId);
 		dependency.setVersion(version);
 		dependency.setType(type);
 		return dependency;
 	}
 
 	private File fetchArtifact(final Dependency dependency)
 			throws MojoExecutionException {
 
 		final ArtifactRequest request = new ArtifactRequest();
 		request.setArtifact(dependencyToArtifact(dependency));
 
 		final List<RemoteRepository> aetherRepos = mavenArtifactRepositories2aetherRepositories();
 
 		request.setRepositories(aetherRepos);
 
 		getLog().info(
 				"Resolving artifact " + dependency + " from " + aetherRepos);
 
 		ArtifactResult result;
 		try {
 			result = repoSystem.resolveArtifact(repoSession, request);
 		} catch (final ArtifactResolutionException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 
 		final Artifact artifact = result.getArtifact();
 
 		getLog().info(
 				"Resolved artifact " + artifact + " to "
 						+ result.getArtifact().getFile() + " from "
 						+ result.getRepository());
 
 		return artifact.getFile();
 	}
 
 	protected List<RemoteRepository> mavenArtifactRepositories2aetherRepositories() {
 		final List<RemoteRepository> aetherRepos = new ArrayList<RemoteRepository>();
 		for (final MavenArtifactRepository repo : remoteRepos) {
 			final RemoteRepository remoteRepository = new RemoteRepository(
 					repo.getId(), repo.getLayout().getId(), repo.getUrl());
 			aetherRepos.add(remoteRepository);
 		}
 		return aetherRepos;
 	}
 
 	protected DefaultArtifact dependencyToArtifact(final Dependency dependency) {
 		return new DefaultArtifact( //
 				dependency.getGroupId(), //
 				dependency.getArtifactId(), //
 				dependency.getType(), //
 				dependency.getVersion());
 	}
 
 	public void extract(final File file, final File extractDirectory) {
 		createFile(extractDirectory);
 
 		final ZipUnArchiver zipUnArchiver = new ZipUnArchiver(file);
 		// need to set a logger, because if not, a NPE will be raised by
 		// extract() (shame on Plexus)
 		zipUnArchiver
 				.enableLogging(new org.codehaus.plexus.logging.console.ConsoleLogger(
 						0, "Unzipping"));
 
 		zipUnArchiver.setDestDirectory(extractDirectory);
 		zipUnArchiver.extract();
 	}
 
 	protected void createFile(final String fileToBeCreatedName) {
 		final File fileToBeCreated = new File(buildDirectory,
 				fileToBeCreatedName);
 
 		createFile(fileToBeCreated);
 	}
 
 	protected void createFile(final File fileToBeCreated) {
 		if (!fileToBeCreated.exists())
 			fileToBeCreated.mkdirs();
 	}
 }
