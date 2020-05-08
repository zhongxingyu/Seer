 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.facets;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.forge.addon.gradle.projects.GradleTestProjectProvider;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.projects.facets.PackagingFacet;
 import org.jboss.forge.addon.resource.FileResource;
 import org.jboss.forge.addon.resource.Resource;
 import org.jboss.forge.arquillian.AddonDependency;
 import org.jboss.forge.arquillian.Dependencies;
 import org.jboss.forge.arquillian.archive.ForgeArchive;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author Adam Wyłuda
  */
 @RunWith(Arquillian.class)
 public class GradlePackagingFacetTest
 {
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.forge.addon:resources"),
             @AddonDependency(name = "org.jboss.forge.addon:projects"),
             @AddonDependency(name = "org.jboss.forge.addon:parser-java"),
             @AddonDependency(name = "org.jboss.forge.addon:gradle"),
             @AddonDependency(name = "org.jboss.forge.addon:maven"),
             @AddonDependency(name = "org.jboss.forge.addon:configuration")
    })
    public static ForgeArchive getDeployment()
    {
       return GradleTestProjectProvider.getDeployment(
                GradleTestProjectProvider.SIMPLE_RESOURCES_PATH,
                GradleTestProjectProvider.SIMPLE_RESOURCES);
    }
 
    private static GradleTestProjectProvider projectProvider;
 
    @Inject
    private GradleTestProjectProvider injectedProjectProvider;
    private Project project;
    private PackagingFacet facet;
 
    @Before
    public void setUp()
    {
       if (projectProvider == null)
       {
          projectProvider = injectedProjectProvider;
       }
       project = projectProvider.create("",
                GradleTestProjectProvider.SIMPLE_RESOURCES_PATH,
                GradleTestProjectProvider.SIMPLE_RESOURCES);
       facet = project.getFacet(PackagingFacet.class);
    }
 
    @Test
    public void testGetPackagingType()
    {
       String packagingType = facet.getPackagingType();
       assertEquals("war", packagingType);
    }
 
    @Test
    public void testSetPackagingType()
    {
       facet.setPackagingType("ear");
 
       Project sameProject = projectProvider.findProject();
       PackagingFacet sameFacet = sameProject.getFacet(PackagingFacet.class);
       String packagingType = sameFacet.getPackagingType();
       assertEquals("ear", packagingType);
    }
 
    @Test
    public void testGetFinalArtifact()
    {
       facet.executeBuild();
       Resource<?> res = facet.getFinalArtifact();
       String name = res.getName();
       assertEquals("archiveX.war", name);
    }
 
    @Test
    public void testCreateBuilder()
    {
       FileResource<?> archive = (FileResource<?>) facet.createBuilder().runTests(true).build();
       assertEquals("archiveX.war", archive.getName());
       assertTrue(archive.exists());
       assertTrue(project.getRootDirectory().getChild("test.log").exists());
    }
 
    @Test
    public void testCreateBuilderSkipTests()
    {
       FileResource<?> archive = (FileResource<?>) facet.createBuilder().runTests(false).build();
       assertEquals("archiveX.war", archive.getName());
       assertTrue(archive.exists());
       assertFalse(project.getRootDirectory().getChild("test.log").exists());
    }
 
    @Test
    public void testGetFinalName()
    {
       String finalName = facet.getFinalName();
       assertEquals("archiveX", finalName);
    }
 
    public void testSetFinalName()
    {
       facet.setFinalName("NEW_ARCHIVE_NAME");
 
       Project sameProject = projectProvider.findProject();
       PackagingFacet sameFacet = sameProject.getFacet(PackagingFacet.class);
 
       String finalName = sameFacet.getFinalName();
       assertEquals("NEW_ARCHIVE_NAME", finalName);
    }
 }
