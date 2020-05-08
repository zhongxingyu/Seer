 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.model;
 
 import static org.junit.Assert.*;
 
 import java.util.Set;
 
 import org.gradle.jarjar.com.google.common.collect.Sets;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Adam Wyłuda
  */
 public class GradleModelMergeUtilTest
 {
    private String source;
    private GradleModel model;
 
    @Before
    public void prepareModel()
    {
       source = ""
                + "dependencies {\n"
                + "    compile 'x:y:z'\n"
                + "}\n"
                + "allprojects {\n"
                + "    dependencies {\n"
                + "        managed group: 'a', name: 'b', version: 'c', configuration: 'compile'\n"
                + "    }\n"
                + "}\n"
                + "ext.property = 'value'\n"
                + "apply plugin: 'java'\n"
                + "repositories {\n"
                + "    maven {\n"
                + "        url 'http://url.com/'\n"
                + "    }\n"
                + "}\n";
 
       model = GradleModelLoadUtil.load(source);
    }
 
    @Test
    public void testAddDependency()
    {
       Set<GradleDependency> deps = Sets.<GradleDependency> newHashSet(
               GradleDependencyBuilder.create("test", "j:u:nit"),
                GradleDependencyBuilder.create("testRuntime", "d:t:z"),
                GradleDependencyBuilder.create("runtime", "x:x:x")
                );
 
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       for (GradleDependency dep : deps)
       {
          builder.addDependency(dep);
       }
 
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
 
       assertTrue(result.hasDependency(GradleDependencyBuilder.create("compile", "x:y:z")));
       for (GradleDependency dep : deps)
       {
          assertTrue(result.hasDependency(dep));
       }
    }
 
    @Test
    public void testRemoveDependency()
    {
       GradleModelBuilder builder = GradleModelBuilder.create();
       builder.removeDependency(GradleDependencyBuilder.create("compile", "x:y:z"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(0, result.getDependencies().size());
    }
 
    @Test
    public void testAddManagedDependency()
    {
       Set<GradleDependency> deps = Sets.<GradleDependency> newHashSet(
               GradleDependencyBuilder.create("test", "j:u:nit"),
                GradleDependencyBuilder.create("testRuntime", "d:t:z"),
                GradleDependencyBuilder.create("runtime", "x:x:x")
                );
 
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       for (GradleDependency dep : deps)
       {
          builder.addManagedDependency(dep);
       }
 
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
 
       assertTrue(result.hasDependency(GradleDependencyBuilder.create("compile", "x:y:z")));
       for (GradleDependency dep : deps)
       {
          assertTrue(result.hasManagedDependency(dep));
       }
    }
 
    @Test
    public void testRemoveManagedDependency()
    {
       GradleModelBuilder builder = GradleModelBuilder.create();
       builder.removeDependency(GradleDependencyBuilder.create("compile", "a:b:c"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(0, result.getDependencies().size());
    }
 
    @Test
    public void testAddProperty()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.setProperty("x", "y");
       builder.setProperty("a", "b");
 
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
 
       assertEquals(3, result.getProperties().size());
       assertEquals("value", result.getProperties().get("property"));
       assertEquals("y", result.getProperties().get("x"));
       assertEquals("b", result.getProperties().get("a"));
    }
 
    @Test
    public void testRemoveProperty()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.removeProperty("property");
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(0, result.getProperties().size());
    }
 
    @Test
    public void testSetProperty()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.setProperty("property", "newVal");
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(1, result.getProperties().size());
       assertEquals("newVal", result.getProperties().get("property"));
    }
 
    @Test
    public void testAddPlugin()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.addPlugin(GradlePluginBuilder.create().setClazz("myplugin"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(2, result.getPlugins().size());
       assertTrue(result.hasPlugin(GradlePluginBuilder.create().setClazz("java")));
       assertTrue(result.hasPlugin(GradlePluginBuilder.create().setClazz("myplugin")));
    }
 
    @Test
    public void testRemovePlugin()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.removePlugin(GradlePluginBuilder.create().setClazz("java"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(0, result.getPlugins().size());
    }
 
    @Test
    public void testAddRepository()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.addRepository(GradleRepositoryBuilder.create().setUrl("http://newrepo.org/"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(2, result.getRepositories().size());
       assertTrue(result.hasRepository(GradleRepositoryBuilder.create().setUrl("http://url.com/")));
       assertTrue(result.hasRepository(GradleRepositoryBuilder.create().setUrl("http://newrepo.org/")));
    }
 
    @Test
    public void testRemoveRepository()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.removeRepository(GradleRepositoryBuilder.create().setUrl("http://url.com/"));
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertEquals(0, result.getRepositories().size());
    }
    
    @Test
    public void testSetPackaging()
    {
       GradleModelBuilder builder = GradleModelBuilder.create(model);
       builder.setPackaging("ear");
       
       source = GradleModelMergeUtil.merge(source, model, builder);
       GradleModel result = GradleModelLoadUtil.load(source);
       
       assertTrue(result.hasPlugin(GradlePluginBuilder.create().setType(GradlePluginType.EAR)));
    }
 }
