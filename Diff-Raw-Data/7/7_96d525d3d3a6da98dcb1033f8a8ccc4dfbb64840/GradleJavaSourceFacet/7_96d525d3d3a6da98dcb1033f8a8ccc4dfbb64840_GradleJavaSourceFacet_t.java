 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.facets;
 
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import org.gradle.jarjar.com.google.common.collect.Lists;
 import org.jboss.forge.addon.facets.AbstractFacet;
 import org.jboss.forge.addon.facets.constraints.FacetConstraint;
 import org.jboss.forge.addon.facets.constraints.FacetConstraints;
 import org.jboss.forge.addon.gradle.projects.GradleFacet;
 import org.jboss.forge.addon.gradle.projects.model.GradleModel;
 import org.jboss.forge.addon.gradle.projects.model.GradleSourceDirectory;
 import org.jboss.forge.addon.gradle.projects.model.GradleSourceSet;
 import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
 import org.jboss.forge.addon.parser.java.resources.JavaResource;
 import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
 import org.jboss.forge.addon.projects.Project;
 import org.jboss.forge.addon.resource.DirectoryResource;
 import org.jboss.forge.addon.resource.Resource;
 import org.jboss.forge.addon.resource.ResourceFilter;
 import org.jboss.forge.furnace.util.Strings;
 import org.jboss.forge.parser.java.JavaSource;
 
 /**
  * @author Adam Wyłuda
  * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
  */
 @FacetConstraints({
          @FacetConstraint({ GradleFacet.class })
 })
 public class GradleJavaSourceFacet extends AbstractFacet<Project> implements JavaSourceFacet
 {
    @Override
    public boolean install()
    {
       if (!this.isInstalled())
       {
          for (DirectoryResource folder : this.getSourceFolders())
          {
             folder.mkdirs();
          }
       }
       return isInstalled();
    }
 
    @Override
    public boolean isInstalled()
    {
       return getSourceFolder().exists();
    }
 
    @Override
    public String calculateName(JavaResource resource)
    {
       String fullPath = Packages.fromFileSyntax(resource.getFullyQualifiedName());
       String pkg = calculatePackage(resource);
       String name = fullPath.substring(fullPath.lastIndexOf(pkg) + pkg.length() + 1);
       name = name.substring(0, name.lastIndexOf(".java"));
       return name;
    }
 
    @Override
    public String calculatePackage(JavaResource resource)
    {
       List<DirectoryResource> folders = getSourceFolders();
       String pkg = null;
       for (DirectoryResource folder : folders)
       {
          String sourcePrefix = folder.getFullyQualifiedName();
          pkg = resource.getParent().getFullyQualifiedName();
          if (pkg.startsWith(sourcePrefix))
          {
             pkg = pkg.substring(sourcePrefix.length() + 1);
             break;
          }
       }
       pkg = Packages.fromFileSyntax(pkg);
 
       return pkg;
    }
 
    @Override
    public String getBasePackage()
    {
       return Packages.toValidPackageName(getFaceted().getFacet(GradleFacet.class).getModel().getGroup());
    }
 
    @Override
    public DirectoryResource getBasePackageResource()
    {
       return getSourceFolder().getChildDirectory(Packages.toFileSyntax(getBasePackage()));
    }
 
    @Override
    public List<DirectoryResource> getSourceFolders()
    {
       List<DirectoryResource> resources = Lists.newArrayList();
       GradleModel model = getFaceted().getFacet(GradleFacet.class).getModel();
 
       for (GradleSourceSet sourceSet : model.getEffectiveSourceSets())
       {
          for (GradleSourceDirectory sourceDir : sourceSet.getJavaDirectories())
          {
             resources.add(directoryResourceFromRelativePath(sourceDir.getPath()));
          }
       }
 
       return resources;
    }
 
    @Override
    public DirectoryResource getSourceFolder()
    {
       GradleModel model = getFaceted().getFacet(GradleFacet.class).getModel();
       GradleSourceDirectory dir = GradleResourceUtil.findSourceSetNamed(model.getEffectiveSourceSets(), "main")
                .getJavaDirectories().get(0);
       return directoryResourceFromRelativePath(dir.getPath());
    }
 
    @Override
    public DirectoryResource getTestSourceFolder()
    {
       GradleModel model = getFaceted().getFacet(GradleFacet.class).getModel();
       GradleSourceDirectory dir = GradleResourceUtil.findSourceSetNamed(model.getEffectiveSourceSets(), "test")
                .getJavaDirectories().get(0);
       return directoryResourceFromRelativePath(dir.getPath());
    }
 
    @Override
    public JavaResource saveJavaSource(JavaSource<?> source) throws FileNotFoundException
    {
       return getJavaResource(Packages.toFileSyntax(source.getQualifiedName()) + ".java").setContents(source);
    }
 
    @Override
    public JavaResource saveTestJavaSource(JavaSource<?> source) throws FileNotFoundException
    {
       return getTestJavaResource(Packages.toFileSyntax(source.getQualifiedName()) + ".java").setContents(source);
    }
 
    @Override
    public JavaResource getJavaResource(String relativePath) throws FileNotFoundException
    {
       return GradleResourceUtil.findFileResource(getMainJavaSources(), relativePath).reify(JavaResource.class);
    }
 
    @Override
    public JavaResource getJavaResource(JavaSource<?> javaClass) throws FileNotFoundException
    {
       String pkg = Strings.isNullOrEmpty(javaClass.getPackage()) ? "" : javaClass.getPackage() + ".";
       return getJavaResource(pkg + javaClass.getName());
    }
 
    @Override
    public JavaResource getTestJavaResource(String relativePath) throws FileNotFoundException
    {
       return GradleResourceUtil.findFileResource(getTestJavaSources(), relativePath).reify(JavaResource.class);
    }
 
    @Override
    public JavaResource getTestJavaResource(JavaSource<?> javaClass) throws FileNotFoundException
    {
       String pkg = Strings.isNullOrEmpty(javaClass.getPackage()) ? "" : javaClass.getPackage() + ".";
       return getTestJavaResource(pkg + javaClass.getName());
    }
 
    @Override
    public void visitJavaSources(final JavaResourceVisitor visitor)
    {
       for (DirectoryResource sourceFolder : getMainJavaSources())
       {
          visitSources(sourceFolder, visitor);
       }
    }
 
    @Override
    public void visitJavaTestSources(final JavaResourceVisitor visitor)
    {
       for (DirectoryResource testSourceFolder : getTestJavaSources())
       {
          visitSources(testSourceFolder, visitor);
       }
    }
 
    private void visitSources(final Resource<?> searchFolder, final JavaResourceVisitor visitor)
    {
       if (searchFolder instanceof DirectoryResource)
       {
 
          searchFolder.listResources(new ResourceFilter()
          {
             @Override
             public boolean accept(Resource<?> resource)
             {
                if (resource instanceof DirectoryResource)
                {
                   visitSources(resource, visitor);
                }
                else if (resource instanceof JavaResource)
                {
                   visitor.visit((JavaResource) resource);
                }
 
                return false;
             }
          });
       }
    }
 
    private List<DirectoryResource> getMainJavaSources()
    {
       return getJavaSourcesFromSourceSet("main");
    }
 
    private List<DirectoryResource> getTestJavaSources()
    {
       return getJavaSourcesFromSourceSet("test");
    }
 
    private List<DirectoryResource> getJavaSourcesFromSourceSet(String sourceSetName)
    {
       List<DirectoryResource> resources = Lists.newArrayList();
       GradleFacet gradleFacet = getFaceted().getFacet(GradleFacet.class);
 
       for (GradleSourceDirectory sourceDir : GradleResourceUtil
                .findSourceSetNamed(gradleFacet.getModel().getEffectiveSourceSets(), sourceSetName)
                .getJavaDirectories())
       {
          resources.add(directoryResourceFromRelativePath(sourceDir.getPath()));
       }
 
       return resources;
    }
 
    private DirectoryResource directoryResourceFromRelativePath(String path)
    {
       return getFaceted().getFacet(GradleFacet.class).getBuildScriptResource().getParent()
                .getChildDirectory(path);
    }
 }
