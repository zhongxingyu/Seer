 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.commons;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.MapAssert.entry;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 
 import org.junit.Test;
 
 import com.openpojo.reflection.PojoClass;
 import com.openpojo.reflection.impl.PojoClassFactory;
 import com.openpojo.validation.PojoValidator;
 
 /**
  * Tests for {@link SrcGen4JConfig}.
  */
 public class SrcGen4JConfigTest extends AbstractTest {
 
     // CHECKSTYLE:OFF
 
     @Test
     public final void testPojoStructureAndBehavior() {
 
         final PojoClass pc = PojoClassFactory.getPojoClass(SrcGen4JConfig.class);
         final PojoValidator pv = createPojoValidator();
 
         pv.runValidation(pc);
 
     }
 
     @Test
     public final void testGetVarMap() {
 
         // PREPARE
         final List<Variable> vars = new ArrayList<Variable>();
         vars.add(new Variable("a", "1"));
         vars.add(new Variable("B", "b"));
         vars.add(new Variable("x", "2"));
         final SrcGen4JConfig testee = new SrcGen4JConfig();
         testee.setVariables(vars);
         testee.init(new File("."));
 
         // TEST
         final Map<String, String> varMap = testee.getVarMap();
 
         // VERIFY
         assertThat(varMap).isNotNull();
         assertThat(varMap).includes(entry("rootDir", "."), entry("a", "1"), entry("B", "b"),
                 entry("x", "2"));
        assertThat(varMap).hasSize(vars.size() + 1);
 
     }
 
     @Test
     public final void testUnmarshalEmptyConfig() throws Exception {
 
         // PREPARE
         final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class);
 
         // TEST
         final SrcGen4JConfig testee = new JaxbHelper().create(
                 "<srcgen4j-config xmlns=\"http://www.fuin.org/srcgen4j/commons\"/>", jaxbContext);
 
         // VERIFY
         assertThat(testee).isNotNull();
 
     }
 
     @Test
     public final void testUnmarshal() throws Exception {
 
         // PREPARE
         final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class);
         final Reader reader = new InputStreamReader(getClass().getClassLoader()
                 .getResourceAsStream("test-config.xml"));
         try {
 
             // TEST
             final SrcGen4JConfig testee = new JaxbHelper().create(reader, jaxbContext);
 
             // VERIFY
             assertThat(testee).isNotNull();
 
             assertThat(testee.getVariables()).isNotNull();
             assertThat(testee.getVariables()).hasSize(2);
             final Variable var0 = testee.getVariables().get(0);
             assertThat(var0.getName()).isEqualTo("root");
             assertThat(var0.getValue()).isEqualTo("/var/tmp");
             final Variable var1 = testee.getVariables().get(1);
             assertThat(var1.getName()).isEqualTo("project_example_path");
             assertThat(var1.getValue()).isEqualTo("${root}/example");
 
             assertThat(testee.getClasspath()).isNotNull();
             assertThat(testee.getClasspath().getBinList()).hasSize(1);
             final BinClasspathEntry entry = testee.getClasspath().getBinList().get(0);
             assertThat(entry.getPath()).isEqualTo("${project_example_path}/target/classes");
 
             assertThat(testee.getProjects()).isNotNull();
             assertThat(testee.getProjects()).hasSize(1);
             final Project prj = testee.getProjects().get(0);
             assertThat(prj.getName()).isEqualTo("example");
             assertThat(prj.getPath()).isEqualTo("${root}/example");
             assertThat(prj.getFolders()).hasSize(8);
             assertThat(prj.getFolders()).contains(new Folder("mainJava"), new Folder("mainRes"),
                     new Folder("genMainJava"), new Folder("genMainRes"), new Folder("testJava"),
                     new Folder("testRes"), new Folder("genTestJava"), new Folder("genTestRes"));
 
             assertThat(testee.getGenerators()).isNotNull();
             assertThat(testee.getGenerators().getList()).hasSize(1);
             final GeneratorConfig gen = testee.getGenerators().getList().get(0);
             assertThat(gen.getName()).isEqualTo("gen1");
             assertThat(gen.getProject()).isEqualTo("example");
             assertThat(gen.getFolder()).isEqualTo("genMainJava");
             assertThat(gen.getArtifacts()).hasSize(3);
             assertThat(gen.getArtifacts()).contains(new Artifact("one"), new Artifact("abstract"),
                     new Artifact("manual"));
             int idx = gen.getArtifacts().indexOf(new Artifact("one"));
             assertThat(idx).isGreaterThan(-1);
             final Artifact one = gen.getArtifacts().get(idx);
             assertThat(one.getTargets()).isNotNull();
             assertThat(one.getTargets()).hasSize(1);
             final Target target = one.getTargets().get(0);
             assertThat(target.getPattern()).isEqualTo(".*//abc//def//ghi//.*//.java");
             assertThat(target.getProject()).isEqualTo("example");
             assertThat(target.getFolder()).isEqualTo("genMainJava");
 
         } finally {
             reader.close();
         }
     }
 
     @Test
     public final void testInit() {
 
         // PREPARE
         final SrcGen4JConfig testee = new SrcGen4JConfig();
 
         final List<Variable> vars = new ArrayList<Variable>();
         vars.add(new Variable("project.name", "1"));
         vars.add(new Variable("project.path", "2"));
         vars.add(new Variable("generator.name", "3"));
         vars.add(new Variable("generator.project", "4"));
         vars.add(new Variable("generator.folder", "5"));
         vars.add(new Variable("folder.name", "6"));
         vars.add(new Variable("folder.path", "7"));
         vars.add(new Variable("artifact.name", "8"));
         vars.add(new Variable("artifact.project", "9"));
         vars.add(new Variable("artifact.folder", "10"));
         vars.add(new Variable("target.pattern", "11"));
         vars.add(new Variable("target.project", "12"));
         vars.add(new Variable("target.folder", "13"));
         vars.add(new Variable("generators.project", "14"));
         vars.add(new Variable("generators.folder", "15"));
 
         final List<Project> projects = new ArrayList<Project>();
         final Project project = new Project("${project.name}", "${project.path}");
         projects.add(project);
         project.addFolder(new Folder("${folder.name}", "${folder.path}"));
 
         final List<GeneratorConfig> genList = new ArrayList<GeneratorConfig>();
         final GeneratorConfig generator = new GeneratorConfig("${generator.name}",
                 "${generator.project}", "${generator.folder}");
         genList.add(generator);
         final Artifact artifact = new Artifact("${artifact.name}", "${artifact.project}",
                 "${artifact.folder}");
         generator.addArtifact(artifact);
         artifact.addTarget(new Target("${target.pattern}", "${target.project}", "${target.folder}"));
 
         final Generators generators = new Generators("${generators.project}",
                 "${generators.folder}");
         generators.setList(genList);
 
         testee.setVariables(vars);
         testee.setProjects(projects);
         testee.setGenerators(generators);
 
         // TEST
         testee.init(new File("."));
 
         // VERIFY
 
         final Project resultProject = testee.getProjects().get(0);
         assertThat(resultProject.getName()).isEqualTo("1");
         assertThat(resultProject.getPath()).isEqualTo("2");
         final Folder resultFolder = resultProject.getFolders().get(0);
         assertThat(resultFolder.getName()).isEqualTo("6");
         assertThat(resultFolder.getPath()).isEqualTo("7");
 
         assertThat(testee.getGenerators()).isNotNull();
         assertThat(testee.getGenerators().getProject()).isEqualTo("14");
         assertThat(testee.getGenerators().getFolder()).isEqualTo("15");
         assertThat(testee.getGenerators().getList()).isNotNull();
         assertThat(testee.getGenerators().getList()).hasSize(1);
 
         final GeneratorConfig resultGenerator = testee.getGenerators().getList().get(0);
         assertThat(resultGenerator.getName()).isEqualTo("3");
         assertThat(resultGenerator.getProject()).isEqualTo("4");
         assertThat(resultGenerator.getFolder()).isEqualTo("5");
         final Artifact resultArtifact = resultGenerator.getArtifacts().get(0);
         assertThat(resultArtifact.getName()).isEqualTo("8");
         assertThat(resultArtifact.getProject()).isEqualTo("9");
         assertThat(resultArtifact.getFolder()).isEqualTo("10");
         final Target resultTarget = resultArtifact.getTargets().get(0);
         assertThat(resultTarget.getPattern()).isEqualTo("11");
         assertThat(resultTarget.getProject()).isEqualTo("12");
         assertThat(resultTarget.getFolder()).isEqualTo("13");
 
     }
 
     @Test
     public final void testInitNullContent() {
 
         // PREPARE
         final SrcGen4JConfig testee = new SrcGen4JConfig();
 
         // TEST
         testee.init(new File("."));
 
         // VERIFY
         // Test makes sure the "init(File)" does not throw NullPointerException
         // if nothing is set
 
     }
 
     @Test
     public void testFindTargetPositive() throws Exception {
 
         // PREPARE
         final SrcGen4JConfig testee = load("findTarget.xml");
         testee.init(new File("."));
 
         // TEST
         final Folder folder = testee.findTargetFolder("gen1", "arti1", "a/b/c/MyClass.java");
 
         // VERIFY
         assertThat(folder.getPath()).isEqualTo("src/main/java");
         assertThat(folder.getParent().getPath()).isEqualTo("/var/tmp/myproject");
 
     }
 
     @Test
     public void testFindTargetFolderNotFoundException() throws Exception {
 
         // PREPARE
         final SrcGen4JConfig testee = load("findTarget.xml");
         testee.init(new File("."));
 
         // TEST
         try {
             testee.findTargetFolder("gen1", "arti1", "Unknown.java");
             fail("The file should lead to arti1's folder 'folder3' that is not defined");
         } catch (final FolderNotFoundException ex) {
             // VERIFIED
         }
 
     }
 
     @Test
     public void testCreateMavenStyleSingleProject() {
 
         // PREPARE
         final String projectName = "NAME";
 
         // TEST
         SrcGen4JConfig config = SrcGen4JConfig.createMavenStyleSingleProject(projectName, new File(
                 "."));
 
         // VERIFY
         assertThat(config.getProjects()).hasSize(1);
         final Project project = config.getProjects().get(0);
         assertThat(project.getName()).isEqualTo(projectName);
         assertThat(project.getPath()).isEqualTo(".");
         assertThat(project.isMaven()).isTrue();
         assertThat(project.getFolders()).hasSize(8);
         assertThat(project.getFolders()).contains(new Folder("mainJava"), new Folder("mainRes"),
                 new Folder("genMainJava"), new Folder("genMainRes"), new Folder("testJava"),
                 new Folder("testRes"), new Folder("genTestJava"), new Folder("genTestRes"));
 
     }
 
     private SrcGen4JConfig load(final String resourceName) throws Exception {
         final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class);
         final Reader reader = new InputStreamReader(getClass().getClassLoader()
                 .getResourceAsStream(resourceName));
         try {
             return new JaxbHelper().create(reader, jaxbContext);
         } finally {
             reader.close();
         }
     }
 
     // CHECKSTYLE:ON
 
 }
