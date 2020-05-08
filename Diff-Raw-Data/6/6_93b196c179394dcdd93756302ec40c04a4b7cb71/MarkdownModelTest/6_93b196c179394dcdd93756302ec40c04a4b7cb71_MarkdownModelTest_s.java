 /*
  * The MIT License
  *
  * Copyright (c) 2013, CloudBees, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.cloudbees.literate.api.v1;
 
 import org.cloudbees.literate.api.v1.vfs.FilesystemRepository;
 import org.cloudbees.literate.api.v1.vfs.PathNotFoundException;
 import org.cloudbees.literate.api.v1.vfs.ProjectRepository;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestName;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.hasItem;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import static org.junit.Assume.assumeThat;
 
 public class MarkdownModelTest {
     @Rule
     public TestName name = new TestName();
 
     public File projectRootDir;
 
     public ProjectRepository repository;
 
     @Before
     public void findProjectRoot() throws Exception {
         URL url = getClass().getResource(getClass().getSimpleName() + "/" + name.getMethodName());
         assumeThat("The test resource for " + name.getMethodName() + " exist", url, Matchers.notNullValue());
         try {
             projectRootDir = new File(url.toURI());
         } catch (URISyntaxException e) {
             projectRootDir = new File(url.getPath());
         }
         assumeThat("The test resource for " + name.getMethodName() + " exist", projectRootDir.isDirectory(), Matchers
                 .is(true));
         repository = new FilesystemRepository(projectRootDir);
     }
 
     @Test(expected = PathNotFoundException.class)
     public void empty() throws Exception {
         new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
     }
 
     @Test
     public void smokes() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("java"), contains(Matchers.containsString("mvn verify")));
         assertThat(model.getBuildFor("ruby"), contains(Matchers.containsString("rake build")));
         assertThat(model.getEnvironments().size(), Matchers.is(5));
 
         assertThat(model.getEnvironments(), Matchers
                 .hasItem(new ExecutionEnvironment("java", "oraclejdk7", "linux", "x86")));
     }
 
     @Test
     public void markerFile() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("java"), contains(Matchers.containsString("mvn verify")));
         assertThat(model.getBuildFor("ruby"), contains(Matchers.containsString("rake build")));
     }
 
     @Test
     public void multipleSections() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("ruby"), contains(Matchers.containsString("rake build"),
                 Matchers.containsString("rake clean")));
        assertThat(model.getBuildFor("ruby"), Matchers.not(hasItem(Matchers.containsString("note in the middle"))));
        assertThat(model.getBuildFor("ruby"), Matchers.not(hasItem(Matchers.containsString("Smell"))));
     }
 
 
     @Test
     public void backTickCommands() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("java"), contains(Matchers.containsString("mvn verify")));
     }
 
 
     @Test
     public void badBuildSection() throws Exception {
         try {
             new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
             fail("This is not a valid model - try again.");
         } catch (IllegalStateException e) {
             assertThat(e.getMessage(),
                     Matchers.containsString("Cannot have a global command and environment specific commands"));
         }
     }
 
 
     @Test
     public void defaultSimplest() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor(ExecutionEnvironment.any()), contains(Matchers.containsString("rake build")));
         assertThat(model.getBuildFor(ExecutionEnvironment.any()), contains(Matchers.containsString("rake clean")));
     }
 
     @Test
     public void defaultBuild() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("ruby"), contains(Matchers.containsString("rake build")));
     }
 
     @Test
     public void deploySection() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(
                 ProjectModelRequest.builder(repository).addTaskId("deploy").addTaskId("promote").build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("ruby"), contains(Matchers.containsString("rake build")));
         assertThat(model.getTask("deploy").getCommand(), contains(Matchers.containsString("bees app:deploy")));
         assertThat(model.getTask("promote").getCommand(), contains(Matchers.containsString("super")));
 
     }
 
 
     @Test
     public void showcase() throws Exception {
         ProjectModel model = new ProjectModelSource().submit(ProjectModelRequest.builder(repository).build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor("java"), contains(Matchers.containsString("mvn package")));
         assertThat(model.getBuildFor("java"), contains(Matchers.containsString("./bin/ci-cleanup")));
         assertThat(model.getEnvironments(), Matchers
                 .hasItem(new ExecutionEnvironment("java", "oraclejdk7", "linux", "x86")));
     }
 
     @Test
     public void customRequest() throws Exception {
         ProjectModel model = new ProjectModelSource()
                 .submit(ProjectModelRequest.builder(repository).withBaseName("foobar").withEnvironmentsId("target")
                         .withBuildId("how to").addTaskIds("install", "uninstall").build());
         assertThat(model, Matchers.notNullValue());
         assertThat(model.getBuildFor(), Matchers
                 .allOf(contains(Matchers.containsString("./configure")), contains(
                         Matchers.containsString("./make clean all"))));
         assertThat(model.getEnvironments(), Matchers.hasItems(
                 new ExecutionEnvironment("linux", "x86"),
                 new ExecutionEnvironment("linux", "x64"),
                 new ExecutionEnvironment("linux", "arm"),
                 new ExecutionEnvironment("osx", "x86"),
                 new ExecutionEnvironment("osx", "x64")
         ));
         assertThat(model.getTask("install"),
                 Matchers.allOf(Matchers.notNullValue(),
                         Matchers.hasProperty("command", contains(Matchers.is("./make install\n")))));
         assertThat(model.getTask("uninstall"),
                 Matchers.allOf(Matchers.notNullValue(),
                         Matchers.hasProperty("command", contains(Matchers.is("./make uninstall\n")))));
     }
 
 
 }
