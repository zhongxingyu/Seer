 package org.inigma.maven;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.eclipse.jgit.storage.file.FileRepository;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 
 /**
  * @goal gitVersion
  * 
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  */
 public class GitVersionBranchMojo extends AbstractMojo {
     /**
      * @parameter default-value="${project.artifact}"
      */
     private Artifact artifact;
 
     /**
      * The maven project.
      * 
      * @parameter expression="${project}"
      * @readonly
      */
     private MavenProject project;
 
     /**
      * Contains the full list of projects in the reactor.
      * 
      * @parameter expression="${reactorProjects}"
      * @readonly
      */
     private List<MavenProject> reactorProjects;
 
     /**
      * Define the desired pattern to use for snapshots of branched code. Valid substitution variables are
      * <ul>
     * <li>project.version - The original version number without the -SNAPSHOT component.</li>
      * <li>scmVersion.date - The current date in yyyy.MM.dd.hh.mm.ss format.</li>
      * <li>scmVersion.branch - The name of the current branch.</li>
      * </ul>
      * 
      * @parameter expression="${versionPattern}" default-value="${scmVersion.number}.${scmVersion.branch}-SNAPSHOT"
      * @readonly
      */
     private String versionPattern;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         getLog().info("Executing GitVersionBranchMojo with pattern " + versionPattern);
         VersionInformation version = new VersionInformation(versionPattern);
         String versionString = artifact.getVersion();
         version.setSnapshot(versionString.endsWith("-SNAPSHOT"));
         if (version.isSnapshot()) {
             version.setVersion(versionString.substring(0, versionString.indexOf("-SNAPSHOT")));
         } else {
             version.setVersion(versionString);
         }
 
         if (version.isSnapshot()) {
             FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir();
             if (repositoryBuilder == null) {
                 throw new MojoExecutionException("Git Repository could not be found.");
             }
             String branch = null;
             try {
                 FileRepository repository = repositoryBuilder.build();
                 branch = repository.getBranch();
                 getLog().info("Branch name is " + branch);
             } catch (IOException e) {
                 throw new MojoExecutionException("Unable to understand the git repository", e);
             }
 
             if (!"master".equals(branch)) {
                 version.setBranchName(branch);
             }
         }
 
         String finalVersion = version.getFinalVersion();
         project.getProperties().put("scmVersion", finalVersion); // version-branch-SNAPSHOT
         for (MavenProject subproj : reactorProjects) {
             subproj.getProperties().put("scmVersion", finalVersion);
         }
     }
 }
