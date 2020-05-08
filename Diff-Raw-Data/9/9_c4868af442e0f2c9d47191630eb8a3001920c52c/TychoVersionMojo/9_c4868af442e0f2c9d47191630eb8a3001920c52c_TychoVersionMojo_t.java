 package mvn.p2.vt.mojo;
 
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Calls the Version Checker with a p2/Tycho generated version.
  * @goal add-tycho-version
  */
 public class TychoVersionMojo extends AbstractVersionMojo
 {
 	/**
 	 * The p2 version.
 	 *
	 * @parameter default-value="${unqualifiedVersion}.${buildQualifier}"
 	 */
 	private String p2Version;
 	/**
 	 * The Maven version.
 	 *
 	 * @parameter expression="${project.version}"
 	 * @readonly
 	 */
 	private String mvnVersion;
 
 
 	@Override
 	protected VersionManifest createManifest() throws MojoFailureException {
		//TODO: once we figure out why this is happening we can throw an error and fail the build instead
		if (p2Version == null || p2Version.contains("${unqualifiedVersion}")) {
			getLog().error("Cannot find p2Version. Inserting invalid value to avoid build failure.");
		}
		
 		VersionManifest manifest = super.createManifest();
 		manifest.setP2Version(p2Version);
 		manifest.setMavenVersion(mvnVersion);
 		
 		if (mvnVersion == null) {
 			getLog().warn("Maven version could not be detected");
 		}
 		
 		return manifest;
 	}
 }
