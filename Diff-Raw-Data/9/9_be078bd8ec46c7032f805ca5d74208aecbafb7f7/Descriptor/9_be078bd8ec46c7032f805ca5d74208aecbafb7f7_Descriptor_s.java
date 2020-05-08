 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease.core;
 
 import net.oneandone.maven.plugins.prerelease.util.Subversion;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.util.Strings;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.DeploymentRepository;
 import org.apache.maven.model.Scm;
 import org.apache.maven.project.MavenProject;
 import org.sonatype.aether.repository.RemoteRepository;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /** Basically the prerelease.properties. Metadata about a prerelease. */
 public class Descriptor {
     private static final String SVN_ORIG = "svn.orig";
     private static final String SVN_TAG = "svn.tag";
     private static final String PROJECT_NAME = "project.name";
     private static final String PROJECT_URL = "project.url";
     private static final String PROJECT_GROUP_ID = "project.groupId";
     private static final String PROJECT_ARTIFACT_ID = "project.artifactId";
     private static final String PROJECT_VERSION = "project.version"; // of the release
     private static final String DEPLOY_REPOSITORY = "deployRepository";
     private static final String DEPLOY_PLUGIN_METADATA = "deployPluginMetadata";
     private static final String PREVIOUS = "previous"; // previous snapshot version
     private static final String NEXT = "next"; // next snapshot version
     private static final String DEPLOY_PROPERTIES = "deployProperties.";
 
     public static Descriptor load(Target target) throws IOException {
         Properties properties;
         InputStream src;
 
         properties = new Properties();
         src = file(target).createInputStream();
         properties.load(src);
         return new Descriptor(target.getRevision(), get(properties, SVN_ORIG), get(properties, SVN_TAG),
                 new Project(get(properties, PROJECT_NAME), get(properties, PROJECT_URL),
                         get(properties, PROJECT_GROUP_ID), get(properties, PROJECT_ARTIFACT_ID), get(properties, PROJECT_VERSION)),
                 repo(get(properties, DEPLOY_REPOSITORY)), "true".equals(get(properties, DEPLOY_PLUGIN_METADATA)),
                 get(properties, PREVIOUS), get(properties, NEXT),
                 getProperties(properties, DEPLOY_PROPERTIES));
     }
 
     public static Descriptor create(MavenProject mavenProject, long revision)
             throws MissingScmTag, MissingDeveloperConnection, CannotBumpVersion, CannotDeterminTagBase {
         Project project;
         String svnOrig;
         String svnTag;
         DeploymentRepository repo;
 
         svnOrig = getSvnUrl(mavenProject);
         svnTag = tagurl(svnOrig, mavenProject);
         project = Project.forMavenProject(mavenProject, releaseVersion(mavenProject));
         repo = mavenProject.getDistributionManagement().getRepository();
         return new Descriptor(revision, svnOrig, svnTag, project, new RemoteRepository(repo.getId(),
                 repo.getLayout(), Strings.removeLeftOpt(repo.getUrl(), "dav:")),
                 "maven-plugin".equals(mavenProject.getPackaging()), mavenProject.getVersion(), next(project.version),
                 new HashMap<String, String>());
     }
 
     public static Descriptor checkedCreate(World world, MavenProject mavenProject, long revision)
             throws CannotDeterminTagBase,
             MissingScmTag, CannotBumpVersion, MissingDeveloperConnection, TagAlreadyExists, VersioningProblem {
         return create(mavenProject, revision).check(world, mavenProject);
     }
 
     //--
 
     /** not actually stored in properties, but convenient to have it here */
     public final long revision;
     public final String svnOrig;
     public final String svnTag;
     public final Project project;
     public final RemoteRepository deployRepository;
     public final boolean deployPluginMetadata;
     public final String previous;
     public final String next;
     public final Map<String, String> deployProperties;
 
     public Descriptor(long revision, String svnOrig, String svnTag, Project project, RemoteRepository deployRepository,
                       boolean deployPluginMetadata, String previous, String next, Map<String, String> deployProperties) {
         if (svnOrig.endsWith("/")) {
             throw new IllegalArgumentException(svnOrig);
         }
         if (svnTag.endsWith("/")) {
             throw new IllegalArgumentException(svnTag);
         }
         this.revision = revision;
         this.svnOrig = svnOrig;
         this.svnTag = svnTag;
         this.project = project;
         this.deployRepository = deployRepository;
         this.deployPluginMetadata = deployPluginMetadata;
         this.previous = previous;
         this.next = next;
         this.deployProperties = deployProperties;
     }
 
     /** @return this */
     public Descriptor check(World world, MavenProject mavenProject)
            throws TagAlreadyExists, VersioningProblem, CannotDeterminTagBase, MissingScmTag, CannotBumpVersion, MissingDeveloperConnection {
         List<String> problems;
         MavenProject parent;
 
         problems = new ArrayList<>();
         checkSnapshot("project", mavenProject.getVersion(), problems);
         parent = mavenProject.getParent();
         if (parent != null) {
             checkRelease("project parent", parent.getVersion(), problems);
         }
         for (Dependency dependency : mavenProject.getDependencies()) {
             checkRelease(dependency.getGroupId() + ":" + dependency.getArtifactId(), dependency.getVersion(), problems);
         }
         for (Artifact artifact : mavenProject.getPluginArtifacts()) {
             if ("net.oneandone.maven.plugins".equals(artifact.getGroupId()) && "prerelease".equals(artifact.getArtifactId())) {
                 // ignores -- that what we use in integration tests */
             } else {
                 checkRelease(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact.getVersion(), problems);
             }
         }
         if (problems.size() > 0) {
             throw new VersioningProblem(problems);
         }
         if (Subversion.exists(world.getTemp(), svnTag)) {
             throw new TagAlreadyExists(svnTag);
         }
         return this;
     }
 
     public String getName() {
         return Long.toString(revision);
     }
 
     public String getTagName() {
         int idx;
 
         idx = svnTag.lastIndexOf('/');
         return svnTag.substring(idx + 1);
     }
 
     public void save(Target target) throws IOException {
         Properties properties;
         OutputStream dest;
 
         properties = new Properties();
         properties.setProperty(SVN_ORIG, svnOrig);
         properties.setProperty(SVN_TAG, svnTag);
         properties.setProperty(DEPLOY_REPOSITORY, deployRepository.getId()
                 + "::" + deployRepository.getContentType() + "::" + deployRepository.getUrl());
         properties.setProperty(DEPLOY_PLUGIN_METADATA, Boolean.toString(deployPluginMetadata));
         properties.setProperty(PREVIOUS, previous);
        properties.setProperty(PROJECT_NAME, project.name);
        properties.setProperty(PROJECT_URL, project.url);
         properties.setProperty(PROJECT_GROUP_ID, project.groupId);
         properties.setProperty(PROJECT_ARTIFACT_ID, project.artifactId);
         properties.setProperty(PROJECT_VERSION, project.version);
         properties.setProperty(NEXT, next);
         for (Map.Entry<String, String> entry : deployProperties.entrySet()) {
             properties.setProperty(DEPLOY_PROPERTIES + entry.getKey(), entry.getValue());
         }
         dest = file(target).createOutputStream(false);
         properties.store(dest, "");
         dest.close();
     }
 
     //-- utility code
 
     private static void checkRelease(String where, String version, List<String> problems) {
         if (version.endsWith("-SNAPSHOT")) {
             problems.add(where + ": expected release version, got " + version);
         }
     }
 
     private static void checkSnapshot(String where, String version, List<String> problems) {
         if (!version.endsWith("-SNAPSHOT")) {
             problems.add(where + ": expected snapshot version, got " + version);
         }
     }
 
     public static String getSvnUrl(MavenProject project) throws MissingScmTag, MissingDeveloperConnection {
         Scm scm;
         String result;
 
         scm = project.getScm();
         if (scm == null) {
             throw new MissingScmTag();
         }
         result = scm.getDeveloperConnection();
         if (result == null) {
             throw new MissingDeveloperConnection();
         }
         return Strings.removeRightOpt(Strings.removeLeft(result, "scm:svn:"), "/");
     }
 
     public static String releaseVersion(MavenProject project) {
         return Strings.removeRight(project.getVersion(), "-SNAPSHOT");
     }
 
     public static String tagurl(String svnurl, MavenProject project) throws CannotDeterminTagBase {
         int idx;
 
         svnurl = svnurl + "/";
         idx = svnurl.indexOf("/trunk/");
         if (idx == -1) {
             idx = svnurl.indexOf("/branches/");
             if (idx == -1) {
                 throw new CannotDeterminTagBase(svnurl);
             }
         }
         return svnurl.substring(0, idx) + "/tags" + "/" + project.getArtifactId() + "-" + releaseVersion(project);
     }
 
     public static String next(String version) throws CannotBumpVersion {
         int idx;
         int num;
 
         idx = version.lastIndexOf('.');
         if (idx == -1) {
             throw new CannotBumpVersion(version);
         }
         try {
             num = Integer.parseInt(version.substring(idx + 1));
         } catch (NumberFormatException e) {
             throw new CannotBumpVersion(version, e);
         }
         num++;
         return version.substring(0, idx + 1) + num + "-SNAPSHOT";
     }
 
 
     private static RemoteRepository repo(String str) throws IOException {
         String[] parts;
 
         parts = str.split("::");
         if (parts.length != 3) {
             throw new IllegalStateException(str);
         }
         return new RemoteRepository(parts[0], parts[1], parts[2]);
     }
 
     private static String get(Properties properties, String key) {
         String value;
 
         value = properties.getProperty(key);
         if (value == null) {
             throw new IllegalStateException("missing key: " + key);
         }
         return value;
     }
 
     private static Map<String, String> getProperties(Properties properties, String prefix) {
         String key;
         Map<String, String> result;
 
         result = new HashMap<>();
         for (Map.Entry<Object, Object> entry : properties.entrySet()) {
             key = (String) entry.getKey();
             if (key.startsWith(prefix)) {
                 result.put(key.substring(prefix.length()), (String) entry.getValue());
             }
         }
         return result;
     }
 
 
     public static FileNode file(Target target) {
         return target.join("prerelease.properties");
     }
 }
