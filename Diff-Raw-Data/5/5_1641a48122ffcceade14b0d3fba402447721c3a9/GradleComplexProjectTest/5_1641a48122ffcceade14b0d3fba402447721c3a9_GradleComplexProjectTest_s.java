 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.forge.addon.dependencies.Coordinate;
 import org.jboss.forge.addon.dependencies.Dependency;
 import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.projects.facets.DependencyFacet;
 import org.jboss.forge.addon.projects.facets.MetadataFacet;
 import org.jboss.forge.arquillian.AddonDependency;
 import org.jboss.forge.arquillian.Dependencies;
 import org.jboss.forge.arquillian.archive.ForgeArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author Adam Wyłuda
  */
 @RunWith(Arquillian.class)
 public class GradleComplexProjectTest
 {
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.forge.addon:resources", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:projects", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:parser-java", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:gradle", version = "2.0.0-SNAPSHOT"),
             @AddonDependency(name = "org.jboss.forge.addon:maven", version = "2.0.0-SNAPSHOT")
    })
    public static ForgeArchive getDeployment()
    {
       return GradleTestProjectProvider.getDeployment(
                GradleTestProjectProvider.COMPLEX_RESOURCES_PATH,
                GradleTestProjectProvider.COMPLEX_RESOURCES);
    }
 
    @Inject
    private GradleTestProjectProvider projectProvider;
 
    private Project subproject;
 
    @Before
    public void setUp()
    {
       subproject = projectProvider.create("subproject",
                         GradleTestProjectProvider.COMPLEX_RESOURCES_PATH,
                         GradleTestProjectProvider.COMPLEX_RESOURCES);
    }
 
    @After
    public void cleanUp()
    {
       projectProvider.clean();
    }
    
    @Test
    public void testChangeName()
    {
       MetadataFacet facet = subproject.getFacet(MetadataFacet.class);
       
       assertEquals("x", facet.getProjectName());
       facet.setProjectName("newname");
 
      Project sameProject = projectProvider.create("subproject",
                        GradleTestProjectProvider.COMPLEX_RESOURCES_PATH,
                        GradleTestProjectProvider.COMPLEX_RESOURCES);
       MetadataFacet sameFacet = sameProject.getFacet(MetadataFacet.class);
       
       assertEquals("newname", sameFacet.getProjectName());
    }
    
    @Test
    public void testReadInheritedProperties()
    {
       MetadataFacet metadataFacet = subproject.getFacet(MetadataFacet.class);
       assertEquals("org.complexproject", metadataFacet.getTopLevelPackage());
       assertEquals("org.x", metadataFacet.getEffectiveProperties().get("someProperty"));
       
       DependencyFacet dependencyFacet = subproject.getFacet(DependencyFacet.class);
       assertTrue(dependencyFacet.hasEffectiveDependency(
                DependencyBuilder.create("org.x:xyz:SNAPSHOT")));
       
       Dependency dep = dependencyFacet.getEffectiveDependency(
                DependencyBuilder.create("org.x:xyz:SNAPSHOT"));
       assertEquals("compile", dep.getScopeType());
       assertEquals(1, dep.getExcludedCoordinates().size());
       
       Coordinate exclusion = dep.getExcludedCoordinates().get(0);
       assertEquals("org.x", exclusion.getGroupId());
       assertEquals("abc", exclusion.getArtifactId());
    }
 }
