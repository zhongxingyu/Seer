 package de.tarent.maven.plugins.pkg.testingstubs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
 import org.codehaus.plexus.util.ReaderFactory;
 
 /**
  * Defines a stub project for testing purposes
  * 
  * @author plafue
  * 
  */
 public class PkgProjectStub extends MavenProjectStub {
 	/**
 	 * Default constructor
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public PkgProjectStub(File pom) {
 		MavenXpp3Reader pomReader = new MavenXpp3Reader();
 		Model model;
 		try {
 			model = pomReader.read(ReaderFactory.newXmlReader(pom));
 			setModel(model);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		setGroupId(model.getGroupId());
 		setArtifactId(model.getArtifactId());
 		setVersion(model.getVersion());
 		setName(model.getName());
 		setUrl(model.getUrl());
 		setPackaging(model.getPackaging());
 
		setRemoteArtifactRepositories(Collections.emptyList());
 
 		Build build = new Build();
 		build.setFinalName(model.getArtifactId());
 		build.setDirectory(getBasedir() + "/target");
 		build.setSourceDirectory(getBasedir() + "/src/main/java");
 		build.setOutputDirectory(getBasedir() + "/target/classes");
 		build.setTestSourceDirectory(getBasedir() + "/src/test/java");
 		build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
 		setBuild(build);
 
 		setDependencies(model.getDependencies());
 		this.setDependencyArtifacts(new HashSet());
 
 		List compileSourceRoots = new ArrayList();
 		compileSourceRoots.add(getBasedir() + "/src/main/java");
 		setCompileSourceRoots(compileSourceRoots);
 
 		List testCompileSourceRoots = new ArrayList();
 		testCompileSourceRoots.add(getBasedir() + "/src/test/java");
 		setTestCompileSourceRoots(testCompileSourceRoots);
 	}
 
 	/** {@inheritDoc} */
 	public File getBasedir() {
 		return new File(super.getBasedir() + "/src/test/resources/dummyproject");
 	}
 
 }
