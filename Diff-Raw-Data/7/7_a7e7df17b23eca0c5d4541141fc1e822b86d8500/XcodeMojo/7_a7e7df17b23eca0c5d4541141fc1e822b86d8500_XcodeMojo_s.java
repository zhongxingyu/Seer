 package org.apache.maven.plugin.xcode;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Goal for generating an Xcode project from a POM.
  * This plug-in provides the ability to generate projects for Xcode from Apple, Inc.
  *
  * @goal xcode
  * @execute phase="generate-sources"
  */
 public class XcodeMojo
         extends AbstractXcodeMojo {
 
 
     public void execute()
             throws MojoExecutionException {
         try {
             doDependencyResolution(executedProject, localRepo);
         }
         catch (Exception e) {
             throw new MojoExecutionException("Unable to build project dependencies.", e);
         }
 
         File projectDir = new File(executedProject.getBasedir(),
                 executedProject.getArtifactId() + ".xcodeproj");
         if (projectDir.exists()) {
             if (!projectDir.isDirectory()) {
                 throw new MojoExecutionException("Target " +
                         projectDir.getPath() +
                         " exists, but is not a directory.");
             }
         } else {
             if (!projectDir.mkdirs()) {
                 throw new MojoExecutionException("Unable to create " +
                         projectDir.getPath() +
                         " directory.");
             }
         }
 
         Map propertyList = new HashMap();
         propertyList.put("archiveVersion", "1");
         propertyList.put("classes", Collections.EMPTY_MAP);
         propertyList.put("objectVersion", "42");
 
         Map objects = new HashMap();
         propertyList.put("objects", objects);
 
         final String sourceTree = "<source>";
 
         //
         //    create the PBXProject
         //
         List targets = new ArrayList();
         PBXObjectRef project = createPBXProject("", targets);
         objects.put(project.getID(), project.getProperties());
         propertyList.put("rootObject", project);
 
         //
         //     create the Main group
         //
         List mainGroupChildren = new ArrayList();
         PBXObjectRef mainGroup = createPBXGroup(executedProject.getArtifactId(),
                 sourceTree, mainGroupChildren);
         objects.put(mainGroup.getID(), mainGroup.getProperties());
         project.getProperties().put("mainGroup", mainGroup.getID());
 
         //
         //     create the Products Group
         //
         List productGroupChildren = new ArrayList();
         PBXObjectRef products = createPBXGroup("Products", sourceTree, productGroupChildren);
         objects.put(products.getID(), products.getProperties());
         mainGroupChildren.add(products.getID());
         project.getProperties().put("productRefGroup", products.getID());
 
         //
         //      create main XCConfigurationList
         //
         List configurations = new ArrayList();
         PBXObjectRef configurationList = createXCConfigurationList(configurations);
         configurationList.getProperties().put("defaultConfigurationIsVisible", "0");
         configurationList.getProperties().put("defaultConfigurationName", "Debug");
         objects.put(configurationList.getID(), configurationList.getProperties());
 
         //
         //     create Debug build configuration
         //
         //
         Map toolDebugSettings = new HashMap();
 
         toolDebugSettings.put("COPY_PHASE_STRIP", "NO");
         toolDebugSettings.put("GCC_DYNAMIC_NO_PIC", "NO");
         toolDebugSettings.put("GCC_ENABLE_FIX_AND_CONTINUE", "YES");
         toolDebugSettings.put("GCC_GENERATE_DEBUGGING_SYMBOLS", "YES");
         toolDebugSettings.put("GCC_OPTIMIZATION_LEVEL", "0");
         toolDebugSettings.put("JAVA_ARCHIVE_CLASSES", "YES");
         toolDebugSettings.put("JAVA_ARCHIVE_COMPRESSION", "NO");
         toolDebugSettings.put("JAVA_ARCHIVE_TYPE", "JAR");
         toolDebugSettings.put("JAVA_COMPILER", "/usr/bin/javac");
         toolDebugSettings.put("JAVA_COMPILER_SOURCE_VERSION", "1.3");
         toolDebugSettings.put("JAVA_COMPILER_TARGET_VM_VERSION", "1.3");
         //testDebugSettings.put("JAVA_MANIFEST_FILE", "Manifest");
         toolDebugSettings.put("PRODUCT_NAME", executedProject.getArtifactId());
         toolDebugSettings.put("PURE_JAVA", "YES");
         toolDebugSettings.put("REZ_EXECUTABLE", "YES");
         toolDebugSettings.put("ZERO_LINK", "YES");
 
         PBXObjectRef debugConfig = createXCBuildConfiguration("Debug", toolDebugSettings);
         objects.put(debugConfig.getID(), debugConfig.getProperties());
         configurations.add(debugConfig.getID());
 
         //
         //     create Release build configuration
         //
         //
         Map toolReleaseSettings = new HashMap();
         toolReleaseSettings.put("COPY_PHASE_STRIP", "YES");
         toolReleaseSettings.put("GCC_ENABLE_FIX_AND_CONTINUE", "NO");
         toolReleaseSettings.put("JAVA_ARCHIVE_CLASSES", "YES");
         toolReleaseSettings.put("JAVA_ARCHIVE_COMPRESSION", "NO");
         toolReleaseSettings.put("JAVA_ARCHIVE_TYPE", "JAR");
         toolReleaseSettings.put("JAVA_COMPILER", "/usr/bin/javac");
         //testReleaseSettings.put("JAVA_MANIFEST_FILE", "Manifest");
         toolReleaseSettings.put("PRODUCT_NAME", executedProject.getArtifactId());
         toolReleaseSettings.put("PURE_JAVA", "YES");
         toolReleaseSettings.put("REZ_EXECUTABLE", "YES");
         toolReleaseSettings.put("ZERO_LINK", "NO");
 
         PBXObjectRef releaseConfig = createXCBuildConfiguration("Release", toolReleaseSettings);
         objects.put(releaseConfig.getID(), releaseConfig.getProperties());
         configurations.add(releaseConfig.getID());
 
         //
         //      create other XCConfigurationList
         //
         List otherConfigurations = new ArrayList();
         PBXObjectRef otherConfigurationList = createXCConfigurationList(otherConfigurations);
         otherConfigurationList.getProperties().put("defaultConfigurationIsVisible", "0");
         otherConfigurationList.getProperties().put("defaultConfigurationName", "Debug");
         objects.put(otherConfigurationList.getID(), otherConfigurationList.getProperties());
         project.getProperties().put("buildConfigurationList", otherConfigurationList.getID());
         
         //
         //       create project Debug configuration
         //
         PBXObjectRef otherDebugConfig = createXCBuildConfiguration("Debug", Collections.EMPTY_MAP);
         objects.put(otherDebugConfig.getID(), otherDebugConfig.getProperties());
         otherConfigurations.add(otherDebugConfig.getID());
 
         //
         //       create project Release configuration
         //
         PBXObjectRef otherReleaseConfig = createXCBuildConfiguration("Release", Collections.EMPTY_MAP);
         objects.put(otherReleaseConfig.getID(), otherReleaseConfig.getProperties());
         otherConfigurations.add(otherReleaseConfig);
 
         //
         //
         //      create main Java target
         //
         List buildPhases = new ArrayList();
         List dependencies = Collections.EMPTY_LIST;
 
         PBXObjectRef toolTarget = createPBXToolTarget(
                 configurationList, buildPhases, dependencies,
                 executedProject.getArtifactId(),
                 "/usr/local/bin",
                 executedProject.getArtifactId());
         targets.add(toolTarget);
         objects.put(toolTarget.getID(), toolTarget.getProperties());
 
         //
         //    create main sources build phase
         //                                
         int buildActionMask = 2147483647;
         List sourcesBuildPhaseFiles = new ArrayList();
         PBXObjectRef sourcesBuildPhase = createPBXSourcesBuildPhase(buildActionMask,
                 sourcesBuildPhaseFiles, false);
         objects.put(sourcesBuildPhase.getID(), sourcesBuildPhase.getProperties());
         buildPhases.add(sourcesBuildPhase);
 
         //
         //     create main jar build phase
         //
         List javaArchiveBuildPhaseFiles = new ArrayList();
         PBXObjectRef javaArchiveBuildPhase = createPBXJavaArchiveBuildPhase(
                 buildActionMask, javaArchiveBuildPhaseFiles, false);
         objects.put(javaArchiveBuildPhase.getID(),
                 javaArchiveBuildPhase.getProperties());
         buildPhases.add(javaArchiveBuildPhase);
 
         //
         //     create main framework build phase
         //
         List mainFrameworksBuildFiles = new ArrayList();
         PBXObjectRef frameworksBuildPhase =
                 createPBXFrameworksBuildPhase(
                         buildActionMask, mainFrameworksBuildFiles, false);
         objects.put(frameworksBuildPhase.getID(),
                 frameworksBuildPhase.getProperties());
         buildPhases.add(frameworksBuildPhase);
 
         //
         //   Create "Main" Group
         //
         //
         List mainSourceGroupChildren = new ArrayList();
         PBXObjectRef mainSourceGroup = createPBXGroup("Main", sourceTree, mainSourceGroupChildren);
         objects.put(mainSourceGroup.getID(), mainSourceGroup.getProperties());
         mainGroupChildren.add(mainSourceGroup);
 
         //
         //   Create Test Group
         //
         List testSourceGroupChildren = new ArrayList();
         PBXObjectRef testSourceGroup = createPBXGroup("Test", sourceTree, testSourceGroupChildren);
         objects.put(testSourceGroup.getID(), testSourceGroup.getProperties());
         mainGroupChildren.add(testSourceGroup);
 
         //
         //   Create Main/Java Group
         //
         List mainJavaGroupChildren = new ArrayList();
         PBXObjectRef mainJavaGroup = createPBXGroup("Java", sourceTree, mainJavaGroupChildren);
         objects.put(mainJavaGroup.getID(), mainJavaGroup.getProperties());
         mainSourceGroupChildren.add(mainJavaGroup.getID());
 
         //
         //   Create Main/Resources Group
         //
         List mainResourcesGroupChildren = new ArrayList();
         PBXObjectRef mainResourcesGroup = createPBXGroup("Resources", sourceTree, mainResourcesGroupChildren);
         objects.put(mainResourcesGroup.getID(), mainResourcesGroup.getProperties());
         mainSourceGroupChildren.add(mainResourcesGroup.getID());
 
         //
         //    Create Main/Dependencies
         //
         List mainDependenciesGroupChildren = new ArrayList();
         PBXObjectRef mainDependenciesGroup = createPBXGroup("Dependencies", sourceTree, mainDependenciesGroupChildren);
         objects.put(mainDependenciesGroup.getID(), mainDependenciesGroup.getProperties());
         mainSourceGroupChildren.add(mainDependenciesGroup.getID());
 
         //
         //   Create Test/Java Group
         //
         List testJavaGroupChildren = new ArrayList();
         PBXObjectRef testJavaGroup = createPBXGroup("Java", sourceTree, testJavaGroupChildren);
         objects.put(testJavaGroup.getID(), testJavaGroup.getProperties());
         testSourceGroupChildren.add(testJavaGroup.getID());
 
         //
         //   Create Test/Resources
         //
         List testResourcesGroupChildren = new ArrayList();
         PBXObjectRef testResourcesGroup = createPBXGroup("Resources", sourceTree, testResourcesGroupChildren);
         objects.put(testResourcesGroup.getID(), testResourcesGroup.getProperties());
         testSourceGroupChildren.add(testResourcesGroup.getID());
 
         //
         //    Create Test/Dependencies
         //
         List testDependenciesGroupChildren = new ArrayList();
         PBXObjectRef testDependenciesGroup = createPBXGroup("Dependencies", sourceTree, testDependenciesGroupChildren);
         objects.put(testDependenciesGroup.getID(), testDependenciesGroup.getProperties());
         testSourceGroupChildren.add(testDependenciesGroup.getID());
 
         File baseDir = executedProject.getBasedir();
         for (Iterator i = executedProject.getCompileSourceRoots().iterator(); i.hasNext();) {
             String directory = (String) i.next();
             toolDebugSettings.put("JAVA_SOURCE_SUBDIR", ".");
             toolReleaseSettings.put("JAVA_SOURCE_SUBDIR", ".");
             addSourceFolder(objects, mainJavaGroup,
                     sourcesBuildPhaseFiles, new File(directory));
         }
 
         //
         //     create test Debug build configuration
         //
         //
         Map testDebugSettings = new HashMap();
         Map testReleaseSettings = new HashMap();
         List testSourcesBuildPhaseFiles = new ArrayList();
 
         for (Iterator i = executedProject.getTestCompileSourceRoots().iterator(); i.hasNext();) {
             String directory = (String) i.next();
             testDebugSettings.put("JAVA_SOURCE_SUBDIR", ".");
             testReleaseSettings.put("JAVA_SOURCE_SUBDIR", ".");
             addSourceFolder(objects, testJavaGroup,
                     testSourcesBuildPhaseFiles, new File(directory));
         }
 
         //
         //    add resources from main target
         //
         addResources(objects, executedProject.getBuild().getResources(),
                 mainResourcesGroup, baseDir, javaArchiveBuildPhaseFiles);
 
         //
         //    add resources from test target
         //
         List testJavaArchiveBuildPhaseFiles = new ArrayList();
         addResources(objects, executedProject.getBuild().getTestResources(),
                 testResourcesGroup, baseDir, testJavaArchiveBuildPhaseFiles);
 
 
         //
         //   iterate over dependencies
         //      add all dependencies to test target,
         //      add all but test scope dependencies to main target
         //
         List testFrameworksFiles = new ArrayList();
         List completeDependencies = executedProject.getDependencies();
         if (completeDependencies != null) {
             for (Iterator i = completeDependencies.iterator(); i.hasNext();) {
                 Dependency dependency = (Dependency) i.next();
                 if(!("test".equals(dependency.getScope()))) {
                     addDependency(objects, mainDependenciesGroup,
                         mainFrameworksBuildFiles, dependency);
                 }
                 addDependency(objects, testDependenciesGroup,
                     testFrameworksFiles, dependency);
             }
         }
 
         List testBuildPhases = new ArrayList();
 
         //
         //     create test java compile phase
         //
         PBXObjectRef testSourcesBuildPhase = createPBXSourcesBuildPhase(
                 buildActionMask, testSourcesBuildPhaseFiles, false);
         objects.put(testSourcesBuildPhase.getID(),
                 testSourcesBuildPhase.getProperties());
         testBuildPhases.add(testSourcesBuildPhase);
 
 
         //
         //     create test jar build phase
         //
         PBXObjectRef testJavaArchiveBuildPhase = createPBXJavaArchiveBuildPhase(
                 buildActionMask, testJavaArchiveBuildPhaseFiles, false);
         objects.put(testJavaArchiveBuildPhase.getID(),
                 testJavaArchiveBuildPhase.getProperties());
         testBuildPhases.add(testJavaArchiveBuildPhase);
 
 
         //
         //     create test frameworks build phase
         //
         PBXObjectRef testFrameworksBuildPhase =
                 createPBXFrameworksBuildPhase(
                         buildActionMask, testFrameworksFiles, false);
         objects.put(testFrameworksBuildPhase.getID(),
                 testFrameworksBuildPhase.getProperties());
         testBuildPhases.add(testFrameworksBuildPhase);
 
         //
         //      create test XCConfigurationList
         //
         List testConfigurations = new ArrayList();
         PBXObjectRef testConfigurationList = createXCConfigurationList(testConfigurations);
         testConfigurationList.getProperties().put("defaultConfigurationIsVisible", "0");
         testConfigurationList.getProperties().put("defaultConfigurationName", "Debug");
         objects.put(testConfigurationList.getID(), testConfigurationList.getProperties());
 
 
         testDebugSettings.put("COPY_PHASE_STRIP", "NO");
         testDebugSettings.put("GCC_DYNAMIC_NO_PIC", "NO");
         testDebugSettings.put("GCC_ENABLE_FIX_AND_CONTINUE", "YES");
         testDebugSettings.put("GCC_GENERATE_DEBUGGING_SYMBOLS", "YES");
         testDebugSettings.put("GCC_OPTIMIZATION_LEVEL", "0");
         testDebugSettings.put("JAVA_ARCHIVE_CLASSES", "NO");
         testDebugSettings.put("JAVA_ARCHIVE_COMPRESSION", "NO");
         testDebugSettings.put("JAVA_ARCHIVE_TYPE", "JAR");
         testDebugSettings.put("JAVA_COMPILER", "/usr/bin/javac");
         testDebugSettings.put("JAVA_COMPILER_SOURCE_VERSION", "1.3");
         testDebugSettings.put("JAVA_COMPILER_TARGET_VM_VERSION", "1.3");
         //testDebugSettings.put("JAVA_MANIFEST_FILE", "Manifest");
         testDebugSettings.put("PRODUCT_NAME", executedProject.getArtifactId() + "-test");
         testDebugSettings.put("PURE_JAVA", "YES");
         testDebugSettings.put("REZ_EXECUTABLE", "YES");
         testDebugSettings.put("ZERO_LINK", "YES");
 
         PBXObjectRef testDebugConfig = createXCBuildConfiguration("Debug", testDebugSettings);
         objects.put(testDebugConfig.getID(), testDebugConfig.getProperties());
         testConfigurations.add(testDebugConfig.getID());
 
         //
         //     create test Release build configuration
         //
         //
         testReleaseSettings.put("COPY_PHASE_STRIP", "YES");
         testReleaseSettings.put("GCC_ENABLE_FIX_AND_CONTINUE", "NO");
         testReleaseSettings.put("JAVA_ARCHIVE_CLASSES", "NO");
         testReleaseSettings.put("JAVA_ARCHIVE_COMPRESSION", "NO");
         testReleaseSettings.put("JAVA_ARCHIVE_TYPE", "JAR");
         testReleaseSettings.put("JAVA_COMPILER", "/usr/bin/javac");
         //testReleaseSettings.put("JAVA_MANIFEST_FILE", "Manifest");
         testReleaseSettings.put("PRODUCT_NAME", executedProject.getArtifactId() + "-test");
         testReleaseSettings.put("PURE_JAVA", "YES");
         testReleaseSettings.put("REZ_EXECUTABLE", "YES");
         testReleaseSettings.put("ZERO_LINK", "NO");
 
         PBXObjectRef testReleaseConfig = createXCBuildConfiguration("Release", testReleaseSettings);
         objects.put(testReleaseConfig.getID(), testReleaseConfig.getProperties());
         testConfigurations.add(testReleaseConfig.getID());
         //
         //     Create test tool target
         //
         List testDependencies = new ArrayList();
         PBXObjectRef testToolTarget = createPBXToolTarget(
                 testConfigurationList, testBuildPhases,
                 testDependencies,
                 executedProject.getArtifactId() + "-test",
                 "/usr/local/bin",
                 executedProject.getArtifactId() + "-test");
         objects.put(testToolTarget.getID(), testToolTarget.getProperties());
         targets.add(testToolTarget);
 
         //
         //    create file reference for test jar
         //
         PBXObjectRef testJarFileRef = createPBXFileReference(
                 "BUILT_PRODUCTS_DIR", new File(executedProject.getArtifactId() + "-test"));
         objects.put(testJarFileRef.getID(), testJarFileRef.getProperties());
         testJarFileRef.getProperties().put("explicitFileType", "folder");
         testJarFileRef.getProperties().put("includeInIndex", "0");
         testToolTarget.getProperties().put("productReference", testJarFileRef.getID());
 
         //
         //    create build file for test jar file
         //
         PBXObjectRef testJarBuildFile = createPBXBuildFile(testJarFileRef, null);
         objects.put(testJarBuildFile.getID(), testJarBuildFile.getProperties());
         testFrameworksFiles.add(testJarBuildFile);
         
 
         //
         //    Create proxy for main jar for test phase
         //
         //
         PBXObjectRef proxy = createPBXContainerItemProxy(
                 project, "1", toolTarget,
                 (String) toolTarget.getProperties().get("name"));
         objects.put(proxy.getID(), proxy.getProperties());
 
         //
         //    Create dependency of test phase on proxy for main phase
         //
         PBXObjectRef mainDependency = createPBXTargetDependency(
                 toolTarget, proxy);
         objects.put(mainDependency.getID(), mainDependency.getProperties());
         testDependencies.add(mainDependency);
 
 
         PBXObjectRef jarFileRef = createPBXFileReference("BUILT_PRODUCTS_DIR",
                 new File(executedProject.getArtifactId() + ".jar"));
         jarFileRef.getProperties().put("explicitFileType", "archive.jar");
         jarFileRef.getProperties().put("includeInIndex", "0");
         objects.put(jarFileRef.getID(), jarFileRef.getProperties());
 
         productGroupChildren.add(jarFileRef);
         productGroupChildren.add(testJarFileRef);
         toolTarget.getProperties().put("productReference", jarFileRef);
         //
         //    create build file for main jar file
         //
         PBXObjectRef jarBuildFile = createPBXBuildFile(jarFileRef, null);
         objects.put(jarBuildFile.getID(), jarBuildFile.getProperties());
         testFrameworksFiles.add(jarBuildFile);
 
 
         PBXObjectRef mainCopyFilesBuildPhase = createPBXCopyFilesBuildPhase(8,
                 "/usr/share/man/man1", 0, Collections.EMPTY_LIST, true);
         objects.put(mainCopyFilesBuildPhase.getID(), mainCopyFilesBuildPhase.getProperties());
         buildPhases.add(mainCopyFilesBuildPhase);
 
 
         File projectFile = new File(projectDir, "project.pbxproj");
         try {
             PropertyListSerialization.serialize(propertyList, projectFile);
         } catch (Exception ex) {
             throw new MojoExecutionException("Unable to create " +
                     projectFile.getPath(), ex);
         }
 
         //
         //   create Map for Default User Properties
         //
         //
         Map defaultUserProperties = new HashMap();
 
         //
         //   create executable reference for /usr/bin/java
         //
         PBXObjectRef javaRef = createPBXExecutableFileReference(
                 new File("/usr/bin/java"), "0");
         defaultUserProperties.put(javaRef.getID(), javaRef.getProperties());
 
         //
         //    create executable and arguments
         //
         List argumentStrings = new ArrayList();
         argumentStrings.add("-cp");
         //
         //   add all entries in compile and test dependencies
         //      to classpath
         StringBuffer classPath = new StringBuffer(executedProject.getArtifactId());
         classPath.append("-test:");
         classPath.append(executedProject.getArtifactId());
         classPath.append(".jar");
         for(Iterator iter = mainDependenciesGroupChildren.iterator();
             iter.hasNext();) {
             PBXObjectRef dependency = (PBXObjectRef) iter.next();
             classPath.append(':');
             classPath.append(dependency.getProperties().get("path"));
         }
         for(Iterator iter = testDependenciesGroupChildren.iterator();
             iter.hasNext();) {
             PBXObjectRef dependency = (PBXObjectRef) iter.next();
             classPath.append(':');
             classPath.append(dependency.getProperties().get("path"));
         }
         argumentStrings.add(classPath.toString());
         argumentStrings.add("junit.swingui.TestRunner");
 
         List executables = new ArrayList();
         PBXObjectRef executable = createPBXExecutable(argumentStrings,
                 javaRef, Collections.EMPTY_LIST);
         executables.add(executable);
         defaultUserProperties.put(executable.getID(), executable.getProperties());
 
         //
         //   this properties are default user additions to
         //      the PBXProject
         //
         Map projectUserProps = new HashMap();
         projectUserProps.put("activeExecutable", executable);
         projectUserProps.put("activeTarget", testToolTarget.getID());
         projectUserProps.put("addToTargets", Collections.EMPTY_LIST);
         projectUserProps.put("executables", executables);
         defaultUserProperties.put(project.getID(), projectUserProps);
 
         //
         //    and additions to the ToolTargets
         //
         Map toolUserProps = new HashMap();
         toolUserProps.put("activeExec", "0");
         defaultUserProperties.put(toolTarget.getID(), toolUserProps);
         defaultUserProperties.put(testToolTarget.getID(), toolUserProps);
         
 
         File defaultUserFile = new File(projectDir, "default.pbxuser");
         try {
             PropertyListSerialization.serialize(defaultUserProperties, defaultUserFile);
         } catch (Exception ex) {
             throw new MojoExecutionException("Unable to create " +
                     defaultUserFile.getPath(), ex);
         }
     }
 
 
     private void addResources(final Map objects,
                               final List projectResources,
                               final PBXObjectRef resourcesGroup,
                               final File baseDir,
                               final List javaArchiveBuildPhaseFiles) {
         for (Iterator i = projectResources.iterator(); i.hasNext();) {
             Resource resource = (Resource) i.next();
             String directory = resource.getDirectory();
             if (resource.getTargetPath() == null && !resource.isFiltering()) {
                 List localBuildFiles = new ArrayList();
                 File directoryFile = new File(directory);
                 addSourceFolder(objects, resourcesGroup,
                         localBuildFiles, directoryFile);
                 //
                 //   Add appropriate JAVA_ARCHIVE_SUBDIR setting
                 //
                 for(Iterator iter = localBuildFiles.iterator(); iter.hasNext(); ) {
                     PBXObjectRef buildFile = (PBXObjectRef) iter.next();
                     String fileRefId = buildFile.getProperties().get("fileRef").toString();
                     Map fileRef = (Map) objects.get(fileRefId);
                     String buildPath = fileRef.get("path").toString();
                     String relPath = toRelative(directoryFile,
                             new File(baseDir, buildPath).getAbsolutePath());
                     Map settings = new HashMap();
                     settings.put("JAVA_ARCHIVE_SUBDIR", new File(relPath).getParent());
                     buildFile.getProperties().put("settings", settings);
                     javaArchiveBuildPhaseFiles.add(buildFile);
                 }
             } else {
                 getLog().info(
                         "Not adding resource directory as it has an incompatible target path or filtering: "
                                 + directory);
             }
         }
 
     }
 
     /**
      * Create PBXTool target (aka product jar or test classes).
      * @param buildConfigurationList configurations.
      * @param buildPhases build phases.
      * @param dependencies dependencies.
      * @param name name.
      * @param productInstallPath product install path.
      * @param productName product name.
      * @return tool target.
      */
     private static PBXObjectRef createPBXToolTarget(
             final PBXObjectRef buildConfigurationList,
             final List buildPhases,
             final List dependencies,
             final String name,
             final String productInstallPath,
             final String productName) {
 
         Map map = new HashMap();
         map.put("isa", "PBXToolTarget");
         map.put("buildConfigurationList", buildConfigurationList.getID());
         map.put("buildPhases", buildPhases);
         map.put("dependencies", dependencies);
         map.put("name", name);
         if (productInstallPath != null) {
             map.put("productInstallPath", productInstallPath);
         }
         if (productName != null) {
             map.put("productName", productName);
         }
         return new PBXObjectRef(map);
     }
 
     /**
      * Create PBXContainerItemProxy.  Appears to let
      * the test target refer to the output of the main target.
      * @param project project.
      * @param proxyType proxy type.
      * @param target target.
      * @param targetName target name.
      * @return proxy.
      */
     private static PBXObjectRef createPBXContainerItemProxy(
             final PBXObjectRef project,
             final String proxyType,
             final PBXObjectRef target,
             final String targetName) {
 
         Map map = new HashMap();
         map.put("containerPortal", project.getID());
         map.put("isa", "PBXContainerItemProxy");
         map.put("proxyType", proxyType);
         map.put("remoteGlobalIDString", target.getID());
         map.put("remoteInfo", targetName);
         return new PBXObjectRef(map);
     }
 
     /**
      * Creates a PBXTargetDependency.
      * @param target target.
      * @param targetProxy proxy for target.
      * @return target dependency.
      */
     private static PBXObjectRef createPBXTargetDependency(
             final PBXObjectRef target,
             final PBXObjectRef targetProxy) {
 
         Map map = new HashMap();
         map.put("target", target.getID());
         map.put("isa", "PBXTargetDependency");
         map.put("targetProxy", targetProxy.getID());
         return new PBXObjectRef(map);
     }
 
     /**
      * Create PBXSourcesBuildPhase.
      *
      * @param buildActionMask build action mask.
      * @param files           source files.
      * @param runOnly         if true, phase should only be run on deployment.
      * @return PBXSourcesBuildPhase.
      */
     private static PBXObjectRef createPBXSourcesBuildPhase(int buildActionMask,
                                                            List files,
                                                            boolean runOnly) {
         Map map = new HashMap();
         map.put("buildActionMask",
                 String.valueOf(buildActionMask));
         map.put("files", files);
         map.put("isa", "PBXSourcesBuildPhase");
         map.put("runOnlyForDeploymentPostprocessing", runOnly ? "1" : "0");
         return new PBXObjectRef(map);
     }
 
     /**
      * Create PBXCopyFilesBuildPhase.
      *
      * @param buildActionMask build action mask.
      * @param files           source files.
      * @param runOnly         if true, phase should only be run on deployment.
      * @return PBXSourcesBuildPhase.
      */
     private static PBXObjectRef createPBXCopyFilesBuildPhase(int buildActionMask,
                                                              final String dstPath,
                                                              final int dstSubfolderSpec,
                                                              List files,
                                                              boolean runOnly) {
         Map map = new HashMap();
         map.put("buildActionMask",
                 String.valueOf(buildActionMask));
         map.put("files", files);
         map.put("dstPath", dstPath);
         map.put("dstSubfolderSpec", String.valueOf(dstSubfolderSpec));
         map.put("isa", "PBXCopyFilesBuildPhase");
         map.put("runOnlyForDeploymentPostprocessing", runOnly ? "1" : "0");
         return new PBXObjectRef(map);
     }
 
     /**
      * Create PBXFrameworksBuildPhase.
      *
      * @param buildActionMask build action mask.
      * @param files           source files.
      * @param runOnly         if true, phase should only be run on deployment.
      * @return PBXSourcesBuildPhase.
      */
     private static PBXObjectRef createPBXFrameworksBuildPhase(int buildActionMask,
                                                               List files,
                                                               boolean runOnly) {
         Map map = new HashMap();
         map.put("buildActionMask",
                 String.valueOf(buildActionMask));
         map.put("files", files);
         map.put("isa", "PBXFrameworksBuildPhase");
         map.put("runOnlyForDeploymentPostprocessing", runOnly ? "1" : "0");
         return new PBXObjectRef(map);
     }
 
     /**
      * Create PBXJavaArchiveBuildPhase.
      *
      * @param buildActionMask build action mask.
      * @param files           source files.
      * @param runOnly         if true, phase should only be run on deployment.
      * @return PBXSourcesBuildPhase.
      */
     private static PBXObjectRef createPBXJavaArchiveBuildPhase(int buildActionMask,
                                                                List files,
                                                                boolean runOnly) {
         Map map = new HashMap();
         map.put("buildActionMask",
                 String.valueOf(buildActionMask));
         map.put("files", files);
         map.put("isa", "PBXJavaArchiveBuildPhase");
         map.put("runOnlyForDeploymentPostprocessing", runOnly ? "1" : "0");
         return new PBXObjectRef(map);
     }
 
     /**
      * Create XCBuildConfiguration.
      *
      * @param name          name.
      * @param buildSettings build settings.
      * @return build configuration.
      */
     private static PBXObjectRef createXCBuildConfiguration(final String name,
                                                            final Map buildSettings) {
         Map map = new HashMap();
         map.put("isa", "XCBuildConfiguration");
         map.put("buildSettings", buildSettings);
         map.put("name", name);
         return new PBXObjectRef(map);
     }
 
     /**
      * Create XCConfigurationList.
      *
      * @param buildConfigurations build configurations.
      * @return configuration list.
      */
     private static PBXObjectRef createXCConfigurationList(final List buildConfigurations) {
         Map map = new HashMap();
         map.put("isa", "XCConfigurationList");
         map.put("buildConfigurations", buildConfigurations);
         return new PBXObjectRef(map);
     }
 
 
     /**
      * Create PBXProject.
      *
      * @param projectDirPath project directory path.
      * @param targets        targets.
      * @return project.
      */
     private static PBXObjectRef createPBXProject(
             final String projectDirPath,
             final List targets) {
         Map map = new HashMap();
         map.put("isa", "PBXProject");
         map.put("hasScannedForEncodings", "0");
         map.put("projectDirPath", projectDirPath);
         map.put("targets", targets);
         return new PBXObjectRef(map);
     }
 
 
     /**
      * Create PBXGroup.
      *
      * @param name       group name.
      * @param sourceTree source tree.
      * @param children   list of PBXFileReferences.
      * @return group.
      */
     private static PBXObjectRef createPBXGroup(final String name,
                                                final String sourceTree,
                                                final List children) {
         Map map = new HashMap();
         map.put("isa", "PBXGroup");
         map.put("name", name);
         map.put("sourceTree", sourceTree);
         map.put("children", children);
         return new PBXObjectRef(map);
     }
 
 
     /**
      * Create PBXFileReference.
      *
      * @param sourceTree source tree.
      * @param file       file.
      * @return PBXFileReference object.
      */
     private static PBXObjectRef createPBXFileReference(final String sourceTree,
                                                        final File file) {
         Map map = new HashMap();
         map.put("isa", "PBXFileReference");
         map.put("name", file.getName());
         map.put("path", file.getPath());
         map.put("sourceTree", sourceTree);
         return new PBXObjectRef(map);
     }
 
     private static final class SourceFileFilter implements FileFilter {
         public static final SourceFileFilter INSTANCE =
                 new SourceFileFilter();
 
         private SourceFileFilter() {
         }
 
         public boolean accept(final File file) {
             if (!file.isDirectory()) {
                 final String name = file.getName();
                 if (name.startsWith(".")) {
                     return false;
                 }
                 return true;
             }
             return false;
         }
     }
 
     private static final class SourceDirectoryFilter implements FileFilter {
         public static final SourceDirectoryFilter INSTANCE =
                 new SourceDirectoryFilter();
 
         private SourceDirectoryFilter() {
         }
 
         public boolean accept(final File file) {
             if (file.isDirectory()) {
                 final String name = file.getName();
                 if (name.startsWith(".") || name.equals("CVS")) {
                     return false;
                 }
                 return true;
             }
             return false;
         }
     }
 
 
     /**
      * Adds a sourceFolder element to Xcode project file.
      *
      * @param objects Xcode objects
      * @TODO
      */
     private void addSourceFolder(final Map objects,
                                  final PBXObjectRef parentGroup,
                                  final List buildFiles,
                                  final File sourceDir) {
         if (sourceDir.isDirectory()) {
             File[] files = sourceDir.listFiles(SourceFileFilter.INSTANCE);
             File[] directories = sourceDir.listFiles(SourceDirectoryFilter.INSTANCE);
             File baseDir = executedProject.getBasedir();
             File relDir = new File(toRelative(baseDir, sourceDir.getPath()));
 
 
             List children = (List) parentGroup.getProperties().get("children");
             for (int i = 0; i < directories.length; i++) {
                 //
                 //   collapse up to 10
                 //     single entry directories
                 File groupDir = directories[i];
                 String groupName = groupDir.getName();
                 for (int j = 0; j < 10; j++) {
                     File[] groupFiles = groupDir.listFiles(SourceFileFilter.INSTANCE);
                     if (groupFiles.length != 0) break;
                     File[] groupDirs = groupDir.listFiles(SourceDirectoryFilter.INSTANCE);
                     if (groupDirs.length != 1) break;
                     groupDir = groupDirs[0];
                     groupName = groupName + "." + groupDir.getName();
                 }
                 PBXObjectRef group = createPBXGroup(
                         groupName,
                         "SOURCE_ROOT",
                         new ArrayList());
                 group.getProperties().put("path", toRelative(baseDir, groupDir.getPath()));
                 children.add(group.getID());
                 objects.put(group.getID(), group.getProperties());
                 addSourceFolder(objects, group,
                         buildFiles,
                         groupDir);
             }
             for (int i = 0; i < files.length; i++) {
                 String fileName = files[i].getName();
                 PBXObjectRef fileRef =
                         createPBXFileReference("SOURCE_ROOT",
                                 new File(relDir, fileName));
                 if (fileName.length() > 5 &&
                         fileName.lastIndexOf(".java") == fileName.length() - 5) {
                     fileRef.getProperties().put("lastKnownFileType", "sourcecode.java");
                 }
                 objects.put(fileRef.getID(), fileRef.getProperties());
                 children.add(fileRef.getID());
 
                 PBXObjectRef buildFile =
                         createPBXBuildFile(fileRef, Collections.EMPTY_MAP);
                 objects.put(buildFile.getID(), buildFile.getProperties());
                 buildFiles.add(buildFile);
 
             }
         }
     }
 
     /**
      * Adds a dependency to a group.
      *
      * @param objects           map of all objects by ID.
      * @param dependenciesGroup group to add dependency.
      * @param buildFiles        files of build phase.
      * @param dependency        dependency.
      */
     private void addDependency(final Map objects,
                                final PBXObjectRef dependenciesGroup,
                                final List buildFiles,
                                final Dependency dependency) {
         String systemPath = dependency.getSystemPath();
         if (systemPath == null) {
             //
             //  TODO: find right way to do this
             //
             systemPath = System.getProperty("user.home") +
                     "/.m2/repository/" +
                    dependency.getGroupId() + "/" +
                     dependency.getArtifactId() + "/" +
                     dependency.getVersion() + "/" +
                     dependency.getArtifactId() +
                     "-" + dependency.getVersion() + ".jar";
         }
         File jarFile = new File(systemPath);
         PBXObjectRef fileRef = createPBXFileReference("<absolute>",
                 jarFile);
         objects.put(fileRef.getID(), fileRef.getProperties());
         fileRef.getProperties().put("lastKnownFileType", "archive.jar");
         List groupChildren = (List) dependenciesGroup.getProperties().get("children");
         groupChildren.add(fileRef);
 
         PBXObjectRef buildFile = createPBXBuildFile(fileRef, null);
         buildFile.getProperties().put("settings", Collections.EMPTY_MAP);
         objects.put(buildFile.getID(), buildFile.getProperties());
         buildFiles.add(buildFile);
     }
 
 
     /**
      * Create PBXBuildFile.
      *
      * @param fileRef  source file.
      * @param settings build settings.
      * @return PBXBuildFile.
      */
     private static PBXObjectRef createPBXBuildFile(PBXObjectRef fileRef,
                                                    Map settings) {
         Map map = new HashMap();
         map.put("fileRef", fileRef);
         map.put("isa", "PBXBuildFile");
         if (settings != null) {
             map.put("settings", settings);
         }
         return new PBXObjectRef(map);
     }
 
 
     /**
      * Create new PBXExecutable.
      * @param argumentStrings argument strings
      * @param launchableReference launchable reference
      * @param sourceDirectories source directories.
      * @return PBXExecutable.
      */
     private static PBXObjectRef createPBXExecutable(final List argumentStrings,
                                                     final PBXObjectRef launchableReference,
                                                     final List sourceDirectories) {
         Map map = new HashMap();
         map.put("activeArgIndex", "0");
         List indices = new ArrayList();
         for(int i = 0; i < argumentStrings.size(); i++) {
             indices.add("YES");
         }
         map.put("activeArgIndices", indices);
         map.put("argumentStrings", argumentStrings);
         map.put("debuggerPlugin", "JavaDebugging");
         map.put("enableDebugStr", "1");
         map.put("environmentalEntries", Collections.EMPTY_LIST);
         map.put("isa", "PBXExecutable");
         map.put("launchableReference", launchableReference);
         map.put("sourceDirectories", sourceDirectories);
         return new PBXObjectRef(map);
     }
 
 
     /**
      * Create new PBXExecutableFileReference.
      * @param executable executable, may not be null.
      * @param refType ref type, likely "0".
      * @return PBXExecutableFileReference.
      */
     private static PBXObjectRef createPBXExecutableFileReference(
             final File executable,
             final String refType) {
         Map map = new HashMap();
         map.put("isa", "PBXExecutableFileReference");
         map.put("name", executable.getName());
         map.put("path", executable.getPath());
         map.put("refType", refType);
         return new PBXObjectRef(map);
 
     }
 
     /**
      * Represents a property map with an 96 bit identity.
      * When placed in a property list, this object will
      * output the string representation of the identity
      * which XCode uses to find the corresponding property
      * bag in the "objects" property of the top-level property list.
      */
     private static final class PBXObjectRef {
         /**
          * Identifier.
          */
         private final String id;
         /**
          * Properties.
          */
         private final Map properties;
         /**
          * Next available identifier.
          */
         private static int nextID = 0;
 
 
         /**
          * Create reference.
          *
          * @param props properties.
          */
         public PBXObjectRef(final Map props) {
             if (props == null) {
                 throw new NullPointerException("props");
             }
             StringBuffer buf = new StringBuffer("000000000000000000000000");
             String idStr = Integer.toHexString(nextID++);
             buf.replace(buf.length() - idStr.length(), buf.length(), idStr);
             id = buf.toString();
             properties = props;
         }
 
         /**
          * Get object identifier.
          *
          * @return identifier.
          */
         public String toString() {
             return id;
         }
 
         /**
          * Get object identifier.
          *
          * @return object identifier.
          */
         public String getID() {
             return id;
         }
 
         /**
          * Get properties.
          *
          * @return properties.
          */
         public Map getProperties() {
             return properties;
         }
     }
 
 
     public void setProject(MavenProject project) {
         this.executedProject = project;
     }
 }
