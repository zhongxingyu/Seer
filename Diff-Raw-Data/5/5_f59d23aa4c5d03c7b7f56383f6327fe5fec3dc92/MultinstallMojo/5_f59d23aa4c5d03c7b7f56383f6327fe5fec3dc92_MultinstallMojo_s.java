 package org.mule.tools.maven;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.installer.ArtifactInstallationException;
 import org.apache.maven.artifact.metadata.ArtifactMetadata;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.install.AbstractInstallMojo;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.artifact.ProjectArtifactMetadata;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.WriterFactory;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.mule.tooling.maven.plugin.utils.MavenDependencyUtils;
 
 /**
  * 
  * @author aabdala
  */
 @Mojo(name = "multinstall", defaultPhase = LifecyclePhase.INSTALL, requiresDirectInvocation = false, executionStrategy = "always")
 public class MultinstallMojo extends AbstractInstallMojo {
 
 	@Parameter(required = true)
 	private List<InstallableArtifact> installableArtifacts;
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (installableArtifacts == null) {
 			throw new IllegalArgumentException("list of artifacts is null");
 		}
 		int size = installableArtifacts.size();
 		getLog().debug("Executing multinstall for " + size + " artifacts");
 		for (InstallableArtifact artifact : installableArtifacts) {
 			install(artifact);
 		}
 	}
 
 	private void install(InstallableArtifact installableArtifact) throws MojoExecutionException, MojoFailureException {
 		String groupId = installableArtifact.getGroupId();
 		String artifactId = installableArtifact.getArtifactId();
 		String version = installableArtifact.getVersion();
 		String packaging = installableArtifact.getType();
 		String classifier = installableArtifact.getClassifier();
 		String pomFile = installableArtifact.getPomFile();
 
 		File file = new File(installableArtifact.getFile());
 
 		if (!file.exists()) {
 			throw new MojoExecutionException("File " + file.getAbsolutePath() + " does not exist");
 		}
 
 		if (pomFile != null) {
 			processModel(readModel(new File(pomFile)), installableArtifact);
 		}
 		Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, packaging, classifier);
 
 		if (file.equals(getLocalRepoFile(artifact))) {
 			throw new MojoFailureException("Cannot install artifact. " + "Artifact is already in the local repository.\n\nFile in question is: " + file + "\n");
 		}
 
 		File generatedPomFile = null;
 
 		if (!"pom".equals(packaging)) {
 			if (pomFile != null) {
 				ArtifactMetadata pomMetadata = new ProjectArtifactMetadata(artifact, new File(pomFile));
 				artifact.addMetadata(pomMetadata);
 			} else {
 				generatedPomFile = generatePomFile(installableArtifact);
 				ArtifactMetadata pomMetadata = new ProjectArtifactMetadata(artifact, generatedPomFile);
 				getLog().debug("Installing generated POM");
 				artifact.addMetadata(pomMetadata);
 			}
 		}
 
 		if (updateReleaseInfo) {
 			artifact.setRelease(true);
 		}
 
 		Collection<File> metadataFiles = new LinkedHashSet<File>();
 		// TODO: maybe not strictly correct, while we should enforce that
 		// packaging has a type handler of the same id,
 		// we don't
 		try {
 			installer.install(file, artifact, localRepository);
 			installChecksums(artifact, metadataFiles);
 		} catch (ArtifactInstallationException e) {
 			throw new MojoExecutionException("Error installing artifact '" + artifact.getDependencyConflictId() + "': " + e.getMessage(), e);
 		} finally {
 			if (generatedPomFile != null) {
 				generatedPomFile.delete();
 			}
 		}
 
 		installChecksums(metadataFiles);
 	}
 
 	/**
 	 * Generates a minimal model from the user-supplied artifact information.
 	 * 
 	 * @return The generated model, never <code>null</code>.
 	 */
 	private Model generateModel(InstallableArtifact artifact) {
 		Model model = new Model();
 
 		model.setModelVersion("4.0.0");
 
 		model.setGroupId(artifact.getGroupId());
 		model.setArtifactId(artifact.getArtifactId());
 		model.setVersion(artifact.getVersion());
 		model.setPackaging(artifact.getType());
 
 		model.setDescription("POM was created from org.mule.tools:multinstall-maven-plugin");
 
 		return model;
 	}
 
 	/**
 	 * Parses a POM.
 	 * 
 	 * @param pomFile
 	 *            The path of the POM file to parse, must not be
 	 *            <code>null</code>.
 	 * @return The model from the POM file, never <code>null</code>.
 	 * @throws MojoExecutionException
 	 *             If the POM could not be parsed.
 	 */
 	private Model readModel(File pomFile) throws MojoExecutionException {
 		Reader reader = null;
 		try {
 			reader = ReaderFactory.newXmlReader(pomFile);
 			return new MavenXpp3Reader().read(reader);
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException("File not found " + pomFile, e);
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error reading POM " + pomFile, e);
 		} catch (XmlPullParserException e) {
 			throw new MojoExecutionException("Error parsing POM " + pomFile, e);
 		} finally {
 			IOUtil.close(reader);
 		}
 	}
 
 	/**
 	 * Populates missing artifact parameters from the specified POM.
 	 * 
 	 * @param model
 	 *            The POM to extract missing artifact coordinates from, must not
 	 *            be <code>null</code>.
 	 */
 	private void processModel(Model model, InstallableArtifact artifact) {
 		Parent parent = model.getParent();
 
 		if (artifact.getGroupId() == null) {
 			artifact.setGroupId(model.getGroupId());
 			if (artifact.getGroupId() == null && parent != null) {
 				artifact.setGroupId(parent.getGroupId());
 			}
 		}
 		if (artifact.getArtifactId() == null) {
 			artifact.setArtifactId(model.getArtifactId());
 		}
 		if (artifact.getVersion() == null) {
 			artifact.setVersion(model.getVersion());
 			if (artifact.getVersion() == null && parent != null) {
 				artifact.setVersion(parent.getVersion());
 			}
 		}
 		if (artifact.getType() == null) {
 			artifact.setType(model.getPackaging());
 		}
 	}
 
 	/**
 	 * Generates a (temporary) POM file from the plugin configuration. It's the
 	 * responsibility of the caller to delete the generated file when no longer
 	 * needed.
 	 * 
 	 * @return The path to the generated POM file, never <code>null</code>.
 	 * @throws MojoExecutionException
 	 *             If the POM file could not be generated.
 	 */
 	private File generatePomFile(InstallableArtifact artifact) throws MojoExecutionException {
 		Model model = generateModel(artifact);
 
 		Writer writer = null;
 		try {
			File pomFile = File.createTempFile("multinstall-" + MavenDependencyUtils.toString(artifact), ".pom");
 
 			writer = WriterFactory.newXmlWriter(pomFile);
 			new MavenXpp3Writer().write(writer, model);
 
 			return pomFile;
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error writing temporary POM file: " + e.getMessage(), e);
 		} finally {
 			IOUtil.close(writer);
 		}
 	}
 
 }
