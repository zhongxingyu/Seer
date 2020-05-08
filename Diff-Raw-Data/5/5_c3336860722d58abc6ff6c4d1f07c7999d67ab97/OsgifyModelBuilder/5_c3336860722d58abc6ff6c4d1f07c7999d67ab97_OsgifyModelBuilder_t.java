 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.maven.context;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.LegacySupport;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.repository.RepositorySystem;
 import org.sourcepit.common.manifest.osgi.BundleManifest;
 import org.sourcepit.common.manifest.osgi.BundleManifestFactory;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.osgify.core.bundle.BundleManifestAppender;
 import org.sourcepit.osgify.core.model.context.BundleCandidate;
 import org.sourcepit.osgify.core.model.context.BundleReference;
 import org.sourcepit.osgify.core.model.context.ContextModelFactory;
 import org.sourcepit.osgify.core.model.context.OsgifyContext;
 import org.sourcepit.osgify.core.resolve.BundleContentAppender;
 import org.sourcepit.osgify.core.resolve.VersionRangeResolver;
 import org.sourcepit.osgify.core.resolve.BundleContentAppender.BundleProjectClassDirectoryResolver;
 
 /*
  * - Artifact as root
  * - Project as root
  * - Resolve against "workspace" (know projects)
  * - resolve against remote repositories
  * - resolve against local repository
  * - support fat bundle and single bundles
  */
 
 @Named
 public class OsgifyModelBuilder
 {
    public static class Request
    {
       private Artifact artifact;
 
       private boolean fatBundle = false;
 
       private boolean includeSource;
 
       private boolean resolveDependenciesOfNativeBundles = false;
 
       private boolean virtualArtifact = false;
 
       private final List<Dependency> dependencies = new ArrayList<Dependency>();
 
       private String scope;
 
       private ArtifactRepository localRepository;
 
       private final List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
 
       private boolean scanBundleContents = false;
 
       private BundleProjectClassDirectoryResolver bundleProjectClassDirectoryResolver;
 
       private NativeBundleStrategy nativeBundleStrategy;
 
       private boolean skipManifestDerivation;
 
       public Artifact getArtifact()
       {
          return artifact;
       }
 
       public void setArtifact(Artifact artifact)
       {
          this.artifact = artifact;
       }
 
       public boolean isFatBundle()
       {
          return fatBundle;
       }
 
       public void setFatBundle(boolean fatBundle)
       {
          this.fatBundle = fatBundle;
       }
 
       public boolean isVirtualArtifact()
       {
          return virtualArtifact;
       }
 
       public void setVirtualArtifact(boolean virtualArtifact)
       {
          this.virtualArtifact = virtualArtifact;
       }
 
       public void setSkipManifestDerivation(boolean skipManifestDerivation)
       {
          this.skipManifestDerivation = skipManifestDerivation;
       }
 
       public boolean isSkipManifestDerivation()
       {
          return skipManifestDerivation;
       }
 
       public void setIncludeSource(boolean resolveSource)
       {
          this.includeSource = resolveSource;
       }
 
       public boolean isIncludeSource()
       {
          return includeSource;
       }
 
       public List<Dependency> getDependencies()
       {
          return dependencies;
       }
 
       public String getScope()
       {
          if (scope == null)
          {
             return Artifact.SCOPE_COMPILE;
          }
          return scope;
       }
 
       public void setScope(String scope)
       {
          this.scope = scope;
       }
 
       public ArtifactRepository getLocalRepository()
       {
          return localRepository;
       }
 
       public void setLocalRepository(ArtifactRepository localRepository)
       {
          this.localRepository = localRepository;
       }
 
       public List<ArtifactRepository> getRemoteRepositories()
       {
          return remoteRepositories;
       }
 
       public void setResolveDependenciesOfNativeBundles(boolean resolveDependenciesOfNativeBundles)
       {
          this.resolveDependenciesOfNativeBundles = resolveDependenciesOfNativeBundles;
       }
 
       public boolean isResolveDependenciesOfNativeBundles()
       {
          return resolveDependenciesOfNativeBundles;
       }
 
       public boolean isScanBundleContents()
       {
          return scanBundleContents;
       }
 
       public void setScanBundleContents(boolean scanBundleContents)
       {
          this.scanBundleContents = scanBundleContents;
       }
 
       public void setBundleProjectClassDirectoryResolver(
          BundleProjectClassDirectoryResolver bundleProjectClassDirectoryResolver)
       {
          this.bundleProjectClassDirectoryResolver = bundleProjectClassDirectoryResolver;
       }
 
       public BundleProjectClassDirectoryResolver getBundleProjectClassDirectoryResolver()
       {
          return bundleProjectClassDirectoryResolver;
       }
 
       public void setNativeBundleStrategy(NativeBundleStrategy nativeBundleStrategy)
       {
          this.nativeBundleStrategy = nativeBundleStrategy;
       }
 
       public NativeBundleStrategy getNativeBundleStrategy()
       {
          return nativeBundleStrategy;
       }
    }
 
    public interface NativeBundleStrategy
    {
       NativeBundleStrategy DEFAULT = new NativeBundleStrategy()
       {
          public boolean isNativeBundle(Artifact artifact, MavenProject project, BundleCandidate bundleCandidate)
          {
             return bundleCandidate.getManifest() != null;
          }
       };
 
       boolean isNativeBundle(Artifact artifact, MavenProject project, BundleCandidate bundleCandidate);
    }
 
    @Inject
    private LegacySupport legacySupport;
 
    @Inject
    private RepositorySystem repositorySystem;
 
    @Inject
    private MavenDependencyWalker dependencyWalker;
 
    @Inject
    private BundleContentAppender bundleContentAppender;
 
    @Inject
    private VersionRangeResolver versionRangeResolver;
 
    @Inject
    private BundleManifestAppender manifestAppender;
 
    public Request createVirtualBundleRequest(String groupId, String artifactId, String version, String classifier,
       Collection<Dependency> dependencies, String scope, boolean isFatBundle,
       List<ArtifactRepository> remoteArtifactRepositories, ArtifactRepository localRepository)
    {
       final Artifact artifact = repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, "jar",
          classifier);
 
       final Request request = createDependenciesRequest(dependencies, scope, remoteArtifactRepositories,
          localRepository);
       request.setArtifact(artifact);
       request.setVirtualArtifact(true);
       request.setFatBundle(isFatBundle);
       return request;
    }
 
    public Request createBundleRequest(String groupId, String artifactId, String version, String classifier,
       String scope, boolean isFatBundle, List<ArtifactRepository> remoteArtifactRepositories,
       ArtifactRepository localRepository)
    {
       final Artifact artifact = repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, "jar",
          classifier);
 
       return createBundleRequest(artifact, scope, isFatBundle, remoteArtifactRepositories, localRepository);
    }
 
    public Request createBundleRequest(final Artifact artifact, String scope, boolean isFatBundle,
       List<ArtifactRepository> remoteArtifactRepositories, ArtifactRepository localRepository)
    {
       final Request request = createDefaultRequest(scope, remoteArtifactRepositories, localRepository);
       request.setArtifact(artifact);
       request.setFatBundle(isFatBundle);
       return request;
    }
 
    public Request createDependenciesRequest(Collection<Dependency> dependencies, String scope,
       List<ArtifactRepository> remoteArtifactRepositories, ArtifactRepository localRepository)
    {
       final Request request = createDefaultRequest(scope, remoteArtifactRepositories, localRepository);
       request.setArtifact(null);
       if (dependencies != null)
       {
          request.getDependencies().addAll(dependencies);
       }
       return request;
    }
 
    private Request createDefaultRequest(String scope, List<ArtifactRepository> remoteArtifactRepositories,
       ArtifactRepository localRepository)
    {
       final Request request = new Request();
       request.setVirtualArtifact(false);
       request.setFatBundle(false);
       request.setResolveDependenciesOfNativeBundles(true);
       request.setScope(scope);
       request.setBundleProjectClassDirectoryResolver(new MavenBundleProjectClassDirectoryResolver(scope));
       request.setScanBundleContents(true);
       request.setLocalRepository(localRepository);
       if (remoteArtifactRepositories != null)
       {
          request.getRemoteRepositories().addAll(remoteArtifactRepositories);
       }
       return request;
    }
 
    public OsgifyContext build(final Request request)
    {
       final MavenDependencyWalker.Request walkerRequest = new MavenDependencyWalker.Request();
       final MavenSession currentSession = legacySupport.getSession();
       if (currentSession != null)
       {
          walkerRequest.setReactorProjects(currentSession.getProjects());
       }
       walkerRequest.setArtifactFilter(newResolutionFilter(request.getScope()));
       walkerRequest.setResolveSource(request.isIncludeSource());
 
       walkerRequest.setArtifact(request.getArtifact());
       walkerRequest.setResolveRoot(!request.isVirtualArtifact());
       walkerRequest.setDependencies(request.getDependencies());
       walkerRequest.setRemoteRepositories(request.getRemoteRepositories());
       walkerRequest.setLocalRepository(request.getLocalRepository());
 
       final NativeBundleStrategy nativeBundleStrategy = request.getNativeBundleStrategy() == null
          ? NativeBundleStrategy.DEFAULT
          : request.getNativeBundleStrategy();
 
       final BundleCandidatesCollector bundleCollector = new BundleCandidatesCollector(request.isFatBundle()
          || request.isResolveDependenciesOfNativeBundles())
       {
          private int calls = 0;
 
          public boolean visitNode(Artifact artifact, MavenProject project)
          {
             calls++;
             boolean visit = super.visitNode(artifact, project);
             if (visit)
             {
                visit = !request.isFatBundle() || calls < 2;
             }
             return visit;
          };
 
          @Override
          protected BundleCandidate newBundleCandidate(Artifact artifact, MavenProject project)
          {
             final BundleCandidate bundle = super.newBundleCandidate(artifact, project);
             if (request.isVirtualArtifact() && artifact.equals(request.getArtifact()))
             {
                bundle.setLocation(null); // erase falsely resolved local file path
             }
             return bundle;
          }
 
          @Override
          protected boolean isNativeBundle(Artifact artifact, MavenProject project, BundleCandidate bundleCandidate)
          {
             return nativeBundleStrategy.isNativeBundle(artifact, project, bundleCandidate);
          }
       };
       walkerRequest.setHandler(bundleCollector);
 
       dependencyWalker.walk(walkerRequest);
 
       final OsgifyContext context = ContextModelFactory.eINSTANCE.createOsgifyContext();
       context.getBundles().addAll(bundleCollector.getBundleCandidates());
 
       if (request.isScanBundleContents())
       {
          bundleContentAppender.appendContents(context, request.getBundleProjectClassDirectoryResolver());
       }
 
       for (BundleCandidate bundleCandidate : context.getBundles())
       {
          for (BundleReference bundleReference : bundleCandidate.getDependencies())
          {
             bundleReference.setVersionRange(versionRangeResolver.resolveVersionRange(bundleReference));
          }
       }
 
       if (!request.isSkipManifestDerivation())
       {
          manifestAppender.append(context);
       }
 
       if (request.isIncludeSource())
       {
          final List<BundleCandidate> sourceCandidates = new ArrayList<BundleCandidate>();
          for (BundleCandidate bundleCandidate : context.getBundles())
          {
             final File sourceJar = bundleCollector.getBundleNodeToSourceJarMap().get(bundleCandidate);
            if (sourceJar == null || !sourceJar.exists())
            {
               continue;
            }

             final Version version = bundleCandidate.getVersion();
 
             final String symbolicName = bundleCandidate.getSymbolicName() + ".source";
 
             BundleCandidate sourceCandidate = ContextModelFactory.eINSTANCE.createBundleCandidate();
             sourceCandidate.setLocation(sourceJar);
             sourceCandidate.setSymbolicName(symbolicName);
             sourceCandidate.setVersion(version);
 
             if (!request.isSkipManifestDerivation())
             {
                BundleManifest manifest = BundleManifestFactory.eINSTANCE.createBundleManifest();
                manifest.setBundleSymbolicName(symbolicName);
                manifest.setBundleVersion(version);
 
                // Eclipse-SourceBundle: com.ibm.icu;version="4.4.2.v20110823";roots:="."
                manifest.setHeader("Eclipse-SourceBundle", bundleCandidate.getSymbolicName() + ";version=\"" + version
                   + "\";roots:=\".\"");
                sourceCandidate.setManifest(manifest);
             }
 
             sourceCandidates.add(sourceCandidate);
          }
 
          context.getBundles().addAll(sourceCandidates);
       }
 
       return context;
    }
 
    private ArtifactFilter newResolutionFilter(String scope)
    {
       final List<ArtifactFilter> artifactFilters = new ArrayList<ArtifactFilter>(2);
       artifactFilters.add(new ScopeArtifactFilter(scope));
       artifactFilters.add(new TypeArtifactFilter("jar"));
       return new AndArtifactFilter(artifactFilters);
    }
 }
