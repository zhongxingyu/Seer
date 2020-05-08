 package com.chaschev.install;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.chaschev.chutils.util.OpenBean2;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.SystemUtils;
 import org.apache.maven.DefaultMaven;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.eclipse.aether.artifact.Artifact;
 import org.eclipse.aether.resolution.ArtifactResult;
 import org.eclipse.aether.artifact.DefaultArtifact;
 import org.apache.maven.plugin.descriptor.PluginDescriptor;
 import org.eclipse.aether.resolution.DependencyResult;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.text.MessageFormat;
 import java.util.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.transform;
 import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;
 
 @Mojo(name = "install", requiresProject = false, threadSafe = true)
 public class InstallMojo extends AbstractExecMojo {
     public static final Function<String, File> PATH_TO_FILE = new Function<String, File>() {
         public File apply(String s) {
             return new File(s);
         }
     };
 
     public static final class MatchingPath implements Comparable<MatchingPath> {
         int priority;
 
         String path;
 
         public MatchingPath(int priority, String path) {
             this.priority = priority;
             this.path = path;
         }
 
         @Override
         public int compareTo(MatchingPath o) {
             return priority - o.priority;
         }
     }
 
     @Parameter(property = "installTo")
     private String installTo;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
 //            FindAvailableVersions.main(null);
             initialize();
 
             Artifact artifact = new DefaultArtifact(artifactName);
             DependencyResult dependencyResult = resolveArtifact(artifact);
 
             artifact = dependencyResult.getRoot().getArtifact();
 
             List<ArtifactResult> dependencies = dependencyResult.getArtifactResults();
 
             if (className != null) {
                 new ExecObject(getLog(),
                     artifact, dependencies, className,
                     parseArgs(),
                     systemProperties
                 ).execute();
             }
 
             Class<?> installation = new URLClassLoader(new URL[]{artifact.getFile().toURI().toURL()}).loadClass("Installation");
 
             List<Object[]> entries = (List<Object[]>) OpenBean2.getStaticFieldValue(installation, "shortcuts");
 
             if (installTo == null) {
                 installTo = findPath();
             }
 
             File installToDir = new File(installTo);
 
             File classPathFile = writeClasspath(artifact, dependencies, installToDir);
 
             for (Object[] entry : entries) {
                 String shortCut = (String) entry[0];
                 String className = entry[1] instanceof String? entry[1].toString() : ((Class) entry[1]).getName();
 
                 File file;
                 if (SystemUtils.IS_OS_WINDOWS) {
                     file = new File(installToDir, shortCut + ".bat");
                     FileUtils.writeStringToFile(
                         file,
                         createLaunchScript(className, classPathFile));
                 } else {
                     file = new File(installToDir, shortCut);
                     FileUtils.writeStringToFile(
                         file,
                         createLaunchScript(className, classPathFile));
                     try {
                         file.setExecutable(true, false);
                     } catch (Exception e) {
                         getLog().warn("could not make '" + file.getAbsolutePath() + "' executable: " + e.toString());
                     }
                 }
 
                 getLog().info("created a shortcut: " + file.getAbsolutePath() + " -> " + className);
             }
         } catch (Exception e) {
             if (e instanceof RuntimeException) {
                 throw (RuntimeException) e;
             } else {
                 getLog().error(e.toString(), e);
                 throw new MojoExecutionException(e.toString());
             }
         }
     }
 
     private String createLaunchScript(String className, File classPathFile) {
         String jarPath = getJarByClass(Runner.class).getAbsolutePath();
 
         if (IS_OS_UNIX) {
             String installerUserHome = getInstallerHomeDir(jarPath);
 
             jarPath = jarPath.replace(installerUserHome, "$HOME");
         }
 
         String appLaunchingString = MessageFormat.format("{0} -cp \"{1}\" {2} {3} {4}",
             javaExePath(), jarPath, Runner.class.getName(), classPathFile.getAbsolutePath(), className);
 
         return sudoInstallationSupportingScript(jarPath, appLaunchingString);
     }
 
     // Solution for sudo installation problem: if you first install the app by sudo mvn installation:install ...
     // Then plugin which contains the Runner is not on the path of all other users because it has been installed to the root's repo!
     // To solve this, we first check if the Runner is on the classpath (by running it with a control string).
     // If it's not, then the plugin dependency is being fetched.
 
     // A simpler and may be better option would be to supply a bootstrap (containing the Runner) dependency for the app.
     // With this solution however there is no need in the dependency and no coupling.
     private String sudoInstallationSupportingScript(String jarPath, String appLaunchingString) {
         String script;
 
         boolean unix = IS_OS_UNIX;
         PluginDescriptor desc = (PluginDescriptor)getPluginContext().get("pluginDescriptor");
 
         if (unix) {
             appLaunchingString = appLaunchingString + " $*";
 
             script = MessageFormat.format("" +
                 "{0} -cp \"{1}\" {2} SMOKE_TEST_HUH 2> /dev/null\n" +
                 "\n" +
                 "rc=$?\n" +
                 "\n" +
                 "if [[ $rc != 0 ]] ; then\n" +
                 "    mvn -U {4}:{5}:{6}:fetch\n" +
                 "    {3}\n" +
                 "    exit $rc\n" +
                 "fi\n" +
                 "\n" +
                 "{3}\n",
                 javaExePath(), jarPath, Runner.class.getName(),
                 appLaunchingString,
                 desc.getGroupId(), desc.getArtifactId(), desc.getVersion());
         } else {
             appLaunchingString = "@" + appLaunchingString + " %*";
 
             script =
                 MessageFormat.format("" +
                     "@{0} -cp \"{1}\" {2} SMOKE_TEST_HUH 2> nul\n" +
                     "@IF ERRORLEVEL 1 GOTO nok\n" +
                     "{3}\n" +
                     "@goto leave\n\n" +
                     "" +
                     ":nok\n" +
                     "mvn -U {4}:{5}:{6}:fetch\n" +
                     "{3}\n" +
                     "@goto leave\n\n" +
                     "" +
                     ":leave\n",
                     javaExePath(), jarPath, Runner.class.getName(),
                     appLaunchingString,
                     desc.getGroupId(), desc.getArtifactId(), desc.getVersion());
 
         }
         return script;
     }
 
     private static String getInstallerHomeDir(String jarPath) {
        return new File(StringUtils.substringBefore(jarPath, "/com/chaschev")).getParentFile().getParentFile().getAbsolutePath();
     }
 
     private static File javaExePath() {
         return new File(SystemUtils.getJavaHome(), "bin/" + (IS_OS_UNIX ? "java" : "java.exe"));
     }
 
     private static File writeClasspath(Artifact artifact, List<ArtifactResult> dependencies, File installToDir) throws IOException {
         final String jarPath = getJarByClass(Runner.class).getAbsolutePath();
 
         final String installerUserHome = getInstallerHomeDir(jarPath);
 
         ArrayList<File> classPathFiles = newArrayList(transform(dependencies, new Function<ArtifactResult, File>() {
             @Override
             public File apply(ArtifactResult artifactResult) {
                 return artifactResult.getArtifact().getFile();
             }
         }));
 
         classPathFiles.add(getJarByClass(Runner.class));
 
         File file = new File(installToDir, artifact.getGroupId() + "." + artifact.getArtifactId());
         FileUtils.writeLines(file, transform(classPathFiles, new Function<File, String>() {
             @Override
             public String apply(File file) {
                 if (IS_OS_UNIX) {
                     return file.getAbsolutePath().replace(installerUserHome, "$HOME");
                 } else {
                     return file.getAbsolutePath();
                 }
             }
         }));
 
         return file;
     }
 
     private String findPath() throws MojoFailureException {
         String path = Optional.fromNullable(System.getenv("path")).or(System.getenv("PATH"));
 
         ArrayList<String> pathEntries = newArrayList(path == null ? new String[0] : path.split(File.pathSeparator));
 
         String javaHomeAbsPath = SystemUtils.getJavaHome().getParentFile().getAbsolutePath();
 
         String mavenHomeAbsPath = getMavenHomeByClass(DefaultMaven.class).getAbsolutePath();
 
         List<MatchingPath> matchingPaths = new ArrayList<MatchingPath>();
 
         final LinkedHashSet<File> knownBinFolders = Sets.newLinkedHashSet(
             Lists.transform(Arrays.asList("/usr/local/bin", "/usr/local/sbin"), PATH_TO_FILE)
         );
 
         for (String pathEntry : pathEntries) {
             File entryFile = new File(pathEntry);
             String absPath = entryFile.getAbsolutePath();
 
             boolean writable = isWritable(entryFile);
 
             getLog().debug("testing " + entryFile.getAbsolutePath() + ": " + (writable ? "writable" : "not writable"));
 
             if (absPath.startsWith(javaHomeAbsPath)) {
                 addMatching(matchingPaths, absPath, writable, 1);
             } else if (absPath.startsWith(mavenHomeAbsPath)) {
                 addMatching(matchingPaths, absPath, writable, 2);
             }
         }
 
         if (IS_OS_UNIX && matchingPaths.isEmpty()) {
             getLog().warn("didn't find maven/jdk writable roots available on path, trying common unix paths: " + knownBinFolders);
 
             final LinkedHashSet<File> pathEntriesSet = Sets.newLinkedHashSet(
                 Lists.transform(pathEntries, PATH_TO_FILE)
             );
 
             for (File knownBinFolder : knownBinFolders) {
                 if (pathEntriesSet.contains(knownBinFolder)) {
                     addMatching(matchingPaths, knownBinFolder.getAbsolutePath(), isWritable(knownBinFolder), 3);
                 }
             }
         }
 
         Collections.sort(matchingPaths);
 
         if (matchingPaths.isEmpty()) {
             throw new MojoFailureException("Could not find a bin folder to write to. Tried: \n" + Joiner.on("\n")
                 .join(mavenHomeAbsPath, javaHomeAbsPath) + "\n" +
                 (IS_OS_UNIX ? knownBinFolders + "\n" : "") +
                 " but they don't appear on the path or are not writable. You may try running as administrator or specifying -DinstallTo=your-bin-dir-path parameter");
         }
 
         return matchingPaths.get(0).path;
     }
 
     private void addMatching(List<MatchingPath> matchingPaths, String matchingPath, boolean writable, int type) {
         if (writable) {
             getLog().info(matchingPath + " matches");
 
             matchingPaths.add(new MatchingPath(type, matchingPath));
         } else {
             getLog().warn(matchingPath + " matches, but is not writable");
 
         }
     }
 
     private static File getMavenHomeByClass(Class<?> aClass) {
         return getJarByClass(aClass).getParentFile().getParentFile();
     }
 
     public static File getJarByClass(Class<?> aClass) {
         return new File(aClass.getProtectionDomain().getCodeSource().getLocation().getFile());
     }
 
     private static boolean isWritable(File dir) {
         try {
             File tempFile = File.createTempFile("testWrite", "txt", dir);
             if (tempFile.exists()) {
                 tempFile.delete();
                 return true;
             }
         } catch (IOException e) {
             return false;
         }
 
         return dir.canWrite();
     }
 
 }
