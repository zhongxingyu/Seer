 /**
  * Copyright 2011 Matthias Nuessler <m.nuessler@web.de>
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
 package org.nuessler.maven.plugin.cakupan;
 
 import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  * Run transformation tests using Surefire.
  * 
  * @author Matthias Nuessler
  * @phase test
  * @goal test
  * @execute phase="test" lifecycle="cakupan"
  * @requiresDependencyResolution test
  */
 public class CakupanTestMojo extends AbstractCakupanMojo {
 
     /**
      * The Maven Session Object
      * 
      * @parameter expression="${session}"
      * @required
      * @readonly
      */
     private MavenSession session;
 
     /**
      * The Maven PluginManager Object
      * 
      * @component
      * @required
      */
    private BuildPluginManager pluginManager;
 
     /**
      * <i>Maven Internal</i>: List of artifacts for the plugin.
      * 
      * @parameter default-value="${plugin.artifacts}"
      * @required
      * @readonly
      */
     protected List<Artifact> pluginArtifacs;
 
     /**
      * A list of &lt;include> elements specifying the tests (by pattern) that
      * should be included in testing. When not specified, the default
      * testIncludes will be <code><br/>
      * &lt;testIncludes><br/>
      * &nbsp;&lt;include>**&#47;*TransformationTest.java&lt;/include><br/>
      * &nbsp;&lt;include>**&#47;*XsltTest.java&lt;/include><br/>
      * &lt;/testIncludes><br/>
      * </code>
      * 
      * @parameter
      */
     private List<String> testIncludes;
 
     /**
      * A list of &lt;exclude> elements specifying the tests (by pattern) that
      * should be excluded in testing. When not specified and when the
      * <code>test</code> parameter is not specified, the default testExcludes
      * will be <code><br/>
      * &lt;testExcludes><br/>
      * &nbsp;&lt;exclude>**&#47;*$*&lt;/exclude><br/>
      * &lt;/testExcludes><br/>
      * </code> (which excludes all inner classes).<br>
      * 
      * @parameter
      */
     private List<String> testExcludes;
 
     /**
      * Set this to "true" to ignore a failure during testing. Its use is NOT
      * RECOMMENDED, but quite convenient on occasion.
      * 
      * @parameter default-value="false"
      *            expression="${maven.test.failure.ignore}"
      */
     private boolean testFailureIgnore;
 
     /**
      * Specify this parameter to run individual tests by file name, overriding
      * the <code>includes/excludes</code> parameters. Each pattern you specify
      * here will be used to create an include pattern formatted like
      * <code>**&#47;${test}.java</code>, so you can just type
      * "-Dtest=MyXsltTest" to run a single test called "foo/MyXsltTest.java".<br/>
      * This parameter overrides the <code>includes/excludes</code> parameter.
      * <p/>
      * 
      * @parameter expression="${test}"
      */
     private String test;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         handleDefaults();
         getLog().info("testIncludes: " + testIncludes);
 
         executeMojo(
                 plugin(groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin"), //
                         version("2.9")), //
                 goal("test"), //
                 configuration(createConfigurationElements()),
                 executionEnvironment(getProject(), session, pluginManager));
     }
 
     private Element createSystemPropertyVarElement(String destDir) {
         return element(
                 name("systemPropertyVariables"),
                 element(name("javax.xml.transform.TransformerFactory"),
                         "com.cakupan.xslt.transform.SaxonCakupanTransformerInstrumentFactoryImpl"),
                 element(name("cakupan.dir"), destDir));
     }
 
     private Element[] createConfigurationElements() throws MojoFailureException {
         List<String> classpathElements = Arrays
                 .asList(getAdditionalClasspathElements());
         getLog().info(
                 "Augmenting test classpath with cakupan and its dependencies: "
                         + classpathElements);
         List<Element> config = new ArrayList<Element>(6);
         config.add(createElementWithChildren("includes", "include",
                 testIncludes));
         config.add(createElementWithChildren("excludes", "exclude",
                 testExcludes));
         config.add(createElementWithChildren("additionalClasspathElements",
                 "additionalClasspathElement", classpathElements));
         String destDir = null;
         try {
             destDir = getInstrumentDestDir().getCanonicalPath();
         } catch (IOException e) {
             throw new MojoFailureException("dest dir not found");
         }
         config.add(createSystemPropertyVarElement(destDir));
         config.add(new Element("testFailureIgnore", String
                 .valueOf(testFailureIgnore)));
         if (StringUtils.isNotBlank(test)) {
             config.add(new Element("test", test));
         }
         return config.toArray(new Element[config.size()]);
     }
 
     private String[] getAdditionalClasspathElements() {
         final List<String> cakupanDepArtifactIds = Arrays.asList(
                 "cakupan-xslt", "xstream", "saxon", "xalan", "xpp3");
         Collection<Artifact> cakupanArtifacts = Collections2.filter(
                 pluginArtifacs, new Predicate<Artifact>() {
                     public boolean apply(Artifact input) {
                         return cakupanDepArtifactIds.contains(input
                                 .getArtifactId());
                     }
                 });
 
         Collection<String> classpathElements = Collections2.transform(
                 cakupanArtifacts, new Function<Artifact, String>() {
                     public String apply(final Artifact input) {
                         try {
                             return input.getFile().getCanonicalPath();
                         } catch (final IOException e) {
                             getLog().error(e);
                             return "";
                         }
                     }
                 });
         return classpathElements.toArray(new String[classpathElements.size()]);
     }
 
     private Element createElementWithChildren(final String parentName,
             final String childName, final List<String> children) {
         Element[] includeElements = Collections2.transform(children,
                 new Function<String, Element>() {
                     public Element apply(String input) {
                         return new Element(childName, input);
                     }
                 }).toArray(new Element[children.size()]);
         return new Element(parentName, includeElements);
     }
 
     private void handleDefaults() {
         if (CollectionUtils.isEmpty(testIncludes)) {
             testIncludes = Arrays.asList("**/*TransformationTest.java",
                     "**/*XsltTest.java");
         }
         if (CollectionUtils.isEmpty(testExcludes)) {
             testExcludes = Arrays.asList("**/*$*");
         }
     }
 }
