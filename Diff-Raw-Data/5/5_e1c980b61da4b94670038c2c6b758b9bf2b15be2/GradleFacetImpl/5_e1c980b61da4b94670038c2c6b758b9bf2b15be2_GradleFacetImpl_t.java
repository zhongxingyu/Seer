 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects;
 
 import java.io.File;
 
 import javax.inject.Inject;
 
 import org.jboss.forge.addon.facets.AbstractFacet;
 import org.jboss.forge.addon.gradle.parser.GradleSourceUtil;
 import org.jboss.forge.addon.gradle.projects.model.GradleModel;
 import org.jboss.forge.addon.gradle.projects.model.GradleModelImpl;
 import org.jboss.forge.addon.gradle.projects.model.GradleModelLoader;
 import org.jboss.forge.addon.gradle.projects.model.GradleProfile;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.resource.FileResource;
 import org.jboss.forge.addon.resource.Resource;
 import org.jboss.forge.addon.resource.ResourceFactory;
 import org.jboss.forge.addon.resource.ResourceFilter;
 import org.jboss.forge.furnace.util.Streams;
 
 /**
  * @author Adam Wyłuda
  */
 public class GradleFacetImpl extends AbstractFacet<Project> implements GradleFacet
 {
    @Inject
    private GradleManager manager;
    @Inject
    private GradleModelLoader modelLoader;
    @Inject
    private ResourceFactory resourceFactory;
 
    private GradleModel model;
 
    @Override
    public boolean install()
    {
       if (!this.isInstalled())
       {
          if (!getBuildScriptResource().exists())
          {
             getBuildScriptResource().createNewFile();
            getBuildScriptResource().setContents("apply plugin: 'java'\n");
          }
          if (!getSettingsScriptResource().exists())
          {
             getSettingsScriptResource().createNewFile();
          }
       }
       return isInstalled();
    }
 
    @Override
    public boolean isInstalled()
    {
       return getBuildScriptResource().exists();
    }
 
    @Override
    public boolean executeTask(String task)
    {
       return executeTask(task, "");
    }
 
    @Override
    public boolean executeTask(String task, String profile, String... arguments)
    {
       return manager.runGradleBuild(getFaceted().getProjectRoot().getFullyQualifiedName(), task, profile, arguments);
    }
 
    @Override
    public GradleModel getModel()
    {
       if (this.model != null)
       {
          // Returns a copy of model
          return new GradleModelImpl(this.model);
       }
       loadModel();
       return new GradleModelImpl(this.model);
    }
 
    @Override
    public void setModel(GradleModel newModel)
    {
       getBuildScriptResource().setContents(newModel.getScript());
 
       // If we need to change model name then it must be done in settings.gradle
       if (!this.model.getName().equals(newModel.getName()))
       {
          String settingsScript = getSettingsScriptResource().getContents();
          // Because setting project name in model also changes the project path
          // we must take project path from old model
          settingsScript = GradleSourceUtil.setProjectName(settingsScript, this.model.getProjectPath(),
                   newModel.getName());
          getSettingsScriptResource().setContents(settingsScript);
       }
 
       // Update profiles
       for (GradleProfile profile : newModel.getProfiles())
       {
          // If profile doesn't exist we must create a file for it
          if (!profile.getProfileScriptResource().exists())
          {
             profile.getProfileScriptResource().createNewFile();
          }
 
          // If there is a change in profile
          if (!profile.getProfileScriptResource().getContents().equals(profile.getModel().getScript()))
          {
             profile.getProfileScriptResource().setContents(profile.getModel().getScript());
          }
       }
 
       // Remove profile scripts if they are not apparent on the list
       for (Resource<?> resource : getFaceted().getProjectRoot().listResources(new ResourceFilter()
       {
          @Override
          public boolean accept(Resource<?> resource)
          {
             return resource.getName().endsWith(GradleSourceUtil.PROFILE_SUFFIX);
          }
       }))
       {
          boolean hasProfile = false;
          String profileName = resource.getName().substring(0, resource.getName().lastIndexOf("-"));
          for (GradleProfile profile : newModel.getProfiles())
          {
             if (profile.getName().equals(profileName))
             {
                hasProfile = true;
                break;
             }
          }
          if (!hasProfile)
          {
             resource.delete();
          }
       }
 
       this.model = newModel;
    }
 
    @Override
    public FileResource<?> getBuildScriptResource()
    {
       return (FileResource<?>) getFaceted().getProjectRoot().getChild("build.gradle");
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public FileResource<?> getSettingsScriptResource()
    {
       return (FileResource<?>) resourceFactory.create(FileResource.class, new File(
                getModel().getRootProjectDirectory(), "settings.gradle"));
    }
 
    private void loadModel()
    {
       checkIfIsForgeLibraryInstalled();
 
       manager.runGradleBuild(getFaceted().getProjectRoot().getFullyQualifiedName(),
                GradleSourceUtil.FORGE_OUTPUT_TASK, "");
 
       FileResource<?> forgeOutputfile = (FileResource<?>) getFaceted().getProjectRoot().getChild(
                GradleSourceUtil.FORGE_OUTPUT_XML);
       String forgeOutput = Streams.toString(forgeOutputfile.getResourceInputStream());
 
       forgeOutputfile.delete();
 
       GradleModel loadedModel = modelLoader.loadFromXML(forgeOutput);
       loadedModel.setScript(getBuildScriptResource().getContents());
 
       // Set resources for profiles
       for (GradleProfile profile : loadedModel.getProfiles())
       {
          profile.setProfileScriptResource(
                   (FileResource<?>) getBuildScriptResource().getParent()
                            .getChild(profile.getName() + GradleSourceUtil.PROFILE_SUFFIX));
          profile.getModel().setScript(profile.getProfileScriptResource().getContents());
       }
 
       this.model = loadedModel;
    }
 
    private void checkIfIsForgeLibraryInstalled()
    {
       String script = getBuildScriptResource().getContents();
       String newScript = GradleSourceUtil.checkForIncludeForgeLibraryAndInsert(script);
 
       // If Forge library is not included
       if (!script.equals(newScript))
       {
          getBuildScriptResource().setContents(newScript);
 
         FileResource<?> forgeLib = (FileResource<?>)
                  getFaceted().getProjectRoot().getChild(GradleSourceUtil.FORGE_LIBRARY);
          forgeLib.setContents(getClass().getResourceAsStream(GradleSourceUtil.FORGE_LIBRARY_RESOURCE));
       }
    }
 }
