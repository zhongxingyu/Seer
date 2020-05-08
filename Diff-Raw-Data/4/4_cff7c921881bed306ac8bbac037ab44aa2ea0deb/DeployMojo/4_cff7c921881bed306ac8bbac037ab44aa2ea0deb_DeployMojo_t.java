 package org.apache.maven.plugin.deploy;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DefaultArtifact;
 import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.lifecycle.LifecycleExecutionException;
 import org.apache.maven.lifecycle.internal.LifecycleDependencyResolver;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.building.ModelSource;
 import org.apache.maven.model.io.DefaultModelReader;
 import org.apache.maven.model.io.DefaultModelWriter;
 import org.apache.maven.model.io.ModelWriter;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.apache.maven.model.merge.ModelMerger;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.WriterFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Deploys an artifact to remote repository.
  *
  * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
  * @author <a href="mailto:jdcasey@apache.org">John Casey (refactoring only)</a>
  * @version $Id: DeployMojo.java 1160164 2011-08-22 09:38:32Z stephenc $
  * @goal deploy
  * @phase deploy
  * @threadSafe
  * @requiresDependencyResolution
  */
 public class DeployMojo
         extends AbstractDeployMojo {
 
     private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.+)::(.+)");
 
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
 
     /**
      * @parameter default-value="${session}"
      * @required
      * @readonly
      */
     protected MavenSession session;
 
     /**
      * @parameter default-value="${project.packaging}"
      * @required
      * @readonly
      */
     private String packaging;
 
     /**
      * @parameter default-value="${project.file}"
      * @required
      * @readonly
      */
     private File pomFile;
 
     /**
      * @parameter default-value=false expression="${deployDependencies}"
      * @required
      */
     protected boolean deployDependencies;
 
     /**
      * @parameter default-value=false expression="${filterPom}"
      * @required
      */
     private boolean filterPom;
 
     /**
      * @parameter default-value=false expression="${failureIsAnOption}"
      * @required
      */
     private boolean failureIsAnOption;
 
     /**
      * Specifies an alternative repository to which the project artifacts should be deployed ( other
      * than those specified in &lt;distributionManagement&gt; ).
      * <br/>
      * Format: id::layout::url
      *
      * @parameter expression="${altDeploymentRepository}"
      */
     private String altDeploymentRepository;
 
     /**
      * @parameter default-value="${project.attachedArtifacts}
      * @required
      * @readonly
      */
     private List attachedArtifacts;
 
     /**
      * Set this to 'true' to bypass artifact deploy
      *
      * @parameter expression="${maven.deploy.skip}" default-value="false"
      * @since 2.4
      */
     private boolean skip;
 
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @parameter expression=
      * "${component.org.apache.maven.lifecycle.internal.LifecycleDependencyResolver}"
      * @required
      * @readonly
      */
     protected LifecycleDependencyResolver lcdResolver;
 
     /**
      * List of Remote Repositories used by the resolver
      *
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @readonly
      * @required
      */
     protected java.util.List remoteRepos;
 
     /**
      * Location of the local repository.
      *
      * @parameter expression="${localRepository}"
      * @readonly
      * @required
      */
     protected org.apache.maven.artifact.repository.ArtifactRepository local;
 
     /**
      * artifact handling.
      *
      * @parameter expression="${component.org.apache.maven.artifact.handler.manager.ArtifactHandlerManager}"
      * @readonly
      * @required
      */
     protected ArtifactHandlerManager handlerManager;
 
     /**
      * Used to create a model
      *
      * @parameter expression=
      * "${component.org.apache.maven.project.ProjectBuilder}"
      * @required
      * @readonly
      */
     protected ProjectBuilder mavenProjectBuilder;
 
     /**
      * @parameter
      */
     private List<String> blackListPatterns;
 
     private List<Pattern> theBlackListPatterns = new LinkedList<Pattern>();
 
     /**
      * @parameter
      */
     private List<String> whiteListPatterns;
 
     private List<Pattern> theWhiteListPatterns = new LinkedList<Pattern>();
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         Set<Artifact> toBeDeployedArtifacts = new HashSet<Artifact>();
         toBeDeployedArtifacts.add(project.getArtifact());
         getLog().debug("Deploying project: " + project.getArtifactId());
         try {
             executeWithArtifacts(toBeDeployedArtifacts);
         } catch (Exception e) {
             throw new MojoExecutionException("Error while resolving artifacts", e);
         }
     }
 
     public void executeWithArtifacts(Set<Artifact> toBeDeployedArtifacts)
             throws MojoExecutionException, MojoFailureException, LifecycleExecutionException {
         if (skip) {
             getLog().info("Skipping artifact deployment");
             return;
         }
 
         failIfOffline();
 
         populatePatterns();
 
         ArtifactRepository repo = getDeploymentRepository();
 
         String protocol = repo.getProtocol();
 
         if (protocol.equalsIgnoreCase("scp")) {
             File sshFile = new File(System.getProperty("user.home"), ".ssh");
 
             if (!sshFile.exists()) {
                 sshFile.mkdirs();
             }
         }
 
         // create a selection of artifacts that need to be deployed
         if (deployDependencies) {
             toBeDeployedArtifacts.clear();
             toBeDeployedArtifacts.add(project.getArtifact());
             toBeDeployedArtifacts.addAll(project.getArtifacts());
         }
 
         int swallowed = 0;
 
         for (Object iter : toBeDeployedArtifacts) {
             try {
 
                 Artifact artifactTBD = (Artifact) iter;
 
                 if (!isAuthorized(artifactTBD)) {
                     getLog().debug("Skipping artifact: " + artifactTBD.getId());
                     continue;
                 }
 
                 getLog().debug("Deploying artifact: " + artifactTBD.getId());
 
                 if (artifactTBD.getFile() == null) {
                     getLog().debug("Skipping deployment of " + artifactTBD.getId());
                     continue;
                 }
 
                 Artifact thePomArtifact;
 
                 if (artifactTBD.getType().equals("pom")) {
                     thePomArtifact = artifactTBD;
                 } else {
                     thePomArtifact = new DefaultArtifact(artifactTBD.getGroupId(), artifactTBD.getArtifactId(),
                             artifactTBD.getVersion(), "", "pom", "", new PomArtifactHandler());
 
                     // we resolve the pom file first
                     HashSet<Artifact> deps = new HashSet<Artifact>();
                     deps.addAll(project.getDependencyArtifacts());
                     deps.add(thePomArtifact);
                     project.setDependencyArtifacts(deps);
                     Set<String> scopes = Collections.singleton(Artifact.SCOPE_RUNTIME);
                     lcdResolver.resolveProjectDependencies(project, scopes, scopes, session, false, Collections.<Artifact>emptySet());
                 }
 
                 // sometimes maven doesn't resolve a pom artifact
                 // don't know why, but it only happens on release artifacts
                 // we'll try it ourselves here
                 if (thePomArtifact.getFile() == null) {
                     String path = artifactTBD.getFile().getAbsolutePath();
                     int extensionStart;
                     if (artifactTBD.getClassifier() == null || artifactTBD.getClassifier().isEmpty()) {
                         extensionStart = path.lastIndexOf(".");
                     }
                     else {
                         extensionStart = path.lastIndexOf("-");
                     }
                     String pathMinusExtension = path.substring(0,extensionStart);
                     String pomFilePath = pathMinusExtension + ".pom";
                     thePomArtifact.setFile(new File(pomFilePath));
                     thePomArtifact.setResolved(true);
                 }
                 
                 if (filterPom) {                    
                     filterPom(thePomArtifact);
                 }
                 pomFile = thePomArtifact.getFile();
 
                 boolean isPomArtifact = "pom".equals(artifactTBD.getType());
 
                 try {
                     if (isPomArtifact) {
                         deploy(pomFile, artifactTBD, repo, getLocalRepository());
                     } else {
                         File file = artifactTBD.getFile();
 
                         if (file != null && file.isFile()) {
                             deploy(file, artifactTBD, repo, getLocalRepository());
                         } else if (!attachedArtifacts.isEmpty()) {
                             getLog().info("No primary artifact to deploy, deploying attached artifacts instead.");
 
                             if (updateReleaseInfo) {
                                 thePomArtifact.setRelease(true);
                             }
 
                             deploy(pomFile, thePomArtifact, repo, getLocalRepository());
 
                             // propagate the timestamped version to the main artifact for the attached artifacts
                             // to pick it up
                             artifactTBD.setResolvedVersion(thePomArtifact.getVersion());
                         } else {
                             String message = "The packaging for this project did not " +
                                     "assign a file to the build artifact";
                             System.err.println("The artifact on which we crash: " + artifactTBD.getId());
                             throw new MojoExecutionException(message);
                         }
                     }
 
                     if (filterPom) {
                         deploy(pomFile, thePomArtifact, repo, getLocalRepository());
                     }
                 } catch (ArtifactDeploymentException e) {
                     throw new MojoExecutionException("Failed to deploy artifact", e);
                 }
             } catch (MojoExecutionException e) {
                 if (!failureIsAnOption) throw e;
                 swallowed++;
                 getLog().warn("failed to deploy " + ((Artifact) iter).getId() + " but continuing anyway " +
                         "(failureIsAnOption)");
             }
         }
         for (Iterator i = attachedArtifacts.iterator(); i.hasNext(); ) {
             Artifact attached = (Artifact) i.next();
             try {
                 deploy(attached.getFile(), attached, repo, getLocalRepository());
             } catch (ArtifactDeploymentException e) {
                 if (!failureIsAnOption) throw new MojoExecutionException("Failed to deploy artifact", e);
                 swallowed++;
                 getLog().warn("failed to deploy " + attached.getId() + " but continuing anyway " +
                         "(failureIsAnOption)");
             }
         }
         if (swallowed > 0) {
             getLog().warn("I swallowed " + swallowed + " deployment exceptions. If you want me to fail on this please" +
                     " unset failureIsAnOption");
         }
     }
 
     private ArtifactRepository getDeploymentRepository()
             throws MojoExecutionException, MojoFailureException {
         ArtifactRepository repo = null;
 
         if (altDeploymentRepository != null) {
             getLog().info("Using alternate deployment repository " + altDeploymentRepository);
 
             Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(altDeploymentRepository);
 
             if (!matcher.matches()) {
                 throw new MojoFailureException(altDeploymentRepository, "Invalid syntax for repository.",
                         "Invalid syntax for alternative repository. Use \"id::layout::url\".");
             } else {
                 String id = matcher.group(1).trim();
                 String layout = matcher.group(2).trim();
                 String url = matcher.group(3).trim();
 
                 ArtifactRepositoryLayout repoLayout = getLayout(layout);
 
                 repo = repositoryFactory.createDeploymentArtifactRepository(id, url, repoLayout, true);
             }
         }
 
         if (repo == null) {
             repo = project.getDistributionManagementArtifactRepository();
         }
 
         if (repo == null) {
             String msg = "Deployment failed: repository element was not specified in the POM inside"
                     + " distributionManagement element or in -DaltDeploymentRepository=id::layout::url parameter";
 
             throw new MojoExecutionException(msg);
         }
 
         return repo;
     }
 
     private void filterPom(Artifact thePomArtifact) throws MojoExecutionException {
 
         getLog().debug("Filtering pom file: " + thePomArtifact.getId());
 
         if (!thePomArtifact.getType().equals("pom")) {
             getLog().debug("Ignoring filtering of a non-pom file");
             throw new MojoExecutionException("Don't ask me to filter a non pom file");
         }
 
         try {
             // Try to remove the broken distributionmanagement element from downloaded poms
             // otherwise maven might refuse to parse those poms to projects
             Model brokenModel = (new DefaultModelReader()).read(thePomArtifact.getFile(), null);
             brokenModel.setDistributionManagement(null);
             // set aside the packaging. We will restore it later to prevent maven from choking on
             // exotic packaging types
             String theRealPackaging = brokenModel.getPackaging();
             brokenModel.setPackaging(null);

            // also remove the module section so we don't fail on aggregator projects
            brokenModel.setModules(null);

             ModelWriter modelWriter = new DefaultModelWriter();
             getLog().debug("Overwriting pom file to remove distributionmanagement: " +
                     thePomArtifact.getFile().getAbsolutePath());
             //we write the new pom file to a temp file as to not interfere with 'official' pom files in the repo
             File tempFile = File.createTempFile("deploy-plugin", "pom");
             modelWriter.write(tempFile, null, brokenModel);
             thePomArtifact.setFile(tempFile);
 
             // first build a project from the pom artifact
             MavenProject bareProject = mavenProjectBuilder.build(thePomArtifact.getFile(),
                     project.getProjectBuildingRequest()).getProject();
 
             // get the model and start filtering useless stuff
             Model currentModel = bareProject.getModel();
 
             currentModel.setPackaging(theRealPackaging);
             currentModel.setParent(null);
             currentModel.setBuild(null);
             currentModel.setCiManagement(null);
             currentModel.setContributors(null);
             currentModel.setCiManagement(null);
             currentModel.setDevelopers(null);
             currentModel.setIssueManagement(null);
             currentModel.setMailingLists(null);
             currentModel.setProfiles(null);
             currentModel.setModules(null);
             currentModel.setDistributionManagement(null);
             currentModel.setPluginRepositories(null);
             currentModel.setReporting(null);
             currentModel.setReports(null);
             currentModel.setRepositories(null);
             currentModel.setScm(null);
             currentModel.setUrl(null);
 
             List<Dependency> goodDeps = new ArrayList<Dependency>();
             for (Object obj : bareProject.getDependencies()) {
                 Dependency dep = (Dependency) obj;
 
                 String scope = dep.getScope();
 
                 if (null == scope || !scope.equals(Artifact.SCOPE_TEST)) {
                     goodDeps.add(dep);
                 }
             }
             currentModel.setDependencies(goodDeps);
 
             currentModel.setDependencyManagement(null);
             currentModel.setProperties(null);
 
             // spit the merged model to the output file.
             getLog().debug("Overwriting pom file with filtered pom: " + thePomArtifact.getFile().getAbsolutePath());
             modelWriter.write(thePomArtifact.getFile(), null, currentModel);
         } catch (Exception e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
 
 
     }
 
     private void populatePatterns() {
         if (blackListPatterns != null) {
             for (String blackList : blackListPatterns) {
                 getLog().debug("Adding black list pattern: " + blackList);
                 theBlackListPatterns.add(Pattern.compile(blackList));
             }
         }
         if (whiteListPatterns != null) {
             for (String whiteList : whiteListPatterns) {
                 getLog().debug("Adding white list pattern: " + whiteList);
                 theWhiteListPatterns.add(Pattern.compile(whiteList));
             }
         }
     }
 
     private boolean isAuthorized(Artifact artifact) {
         String target = artifact.getId();
         for (Pattern black: theBlackListPatterns) {
             if (black.matcher(target).matches()) {
                 getLog().debug(target + " matches blacklist pattern " + black.toString());
                 return false;
             }
         }
         for (Pattern white: theWhiteListPatterns) {
             if (!white.matcher(target).matches()) {
                 getLog().debug(target + " not matches whitelist pattern " + white.toString());
                 return false;
             }
         }
         return true;
     }
 
     static class PomArtifactHandler
             implements ArtifactHandler {
         public String getClassifier() {
             return null;
         }
 
         public String getDirectory() {
             return null;
         }
 
         public String getExtension() {
             return "pom";
         }
 
         public String getLanguage() {
             return "none";
         }
 
         public String getPackaging() {
             return "pom";
         }
 
         public boolean isAddedToClasspath() {
             return false;
         }
 
         public boolean isIncludesDependencies() {
             return false;
         }
     }
 
 }
