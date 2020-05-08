 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.tpmp.resolver.tycho;
 
 import static org.sourcepit.common.utils.io.IOResources.osgiIn;
 import static org.sourcepit.common.utils.lang.Exceptions.pipe;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.repository.RepositorySystem;
 import org.codehaus.plexus.logging.Logger;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.sisu.equinox.EquinoxServiceFactory;
 import org.eclipse.tycho.ArtifactDescriptor;
 import org.eclipse.tycho.ArtifactKey;
 import org.eclipse.tycho.ReactorProject;
 import org.eclipse.tycho.artifacts.DependencyArtifacts;
 import org.eclipse.tycho.artifacts.TargetPlatform;
 import org.eclipse.tycho.core.DependencyResolverConfiguration;
 import org.eclipse.tycho.core.TargetEnvironment;
 import org.eclipse.tycho.core.TargetPlatformConfiguration;
 import org.eclipse.tycho.core.TargetPlatformResolver;
 import org.eclipse.tycho.core.TychoConstants;
 import org.eclipse.tycho.core.TychoProject;
 import org.eclipse.tycho.core.osgitools.AbstractTychoProject;
 import org.eclipse.tycho.core.osgitools.BundleReader;
 import org.eclipse.tycho.core.osgitools.DefaultReactorProject;
 import org.eclipse.tycho.core.osgitools.OsgiManifest;
 import org.eclipse.tycho.core.resolver.DefaultTargetPlatformResolverFactory;
 import org.eclipse.tycho.core.resolver.shared.OptionalResolutionAction;
 import org.eclipse.tycho.core.utils.TychoProjectUtils;
 import org.eclipse.tycho.osgi.adapters.MavenLoggerAdapter;
 import org.eclipse.tycho.p2.metadata.IDependencyMetadata;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolutionResult;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolutionResult.Entry;
 import org.eclipse.tycho.p2.resolver.facade.P2Resolver;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolverFactory;
 import org.eclipse.tycho.resolver.TychoDependencyResolver;
 import org.sourcepit.common.manifest.osgi.BundleManifest;
 import org.sourcepit.common.manifest.osgi.BundleSymbolicName;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.common.manifest.osgi.resource.BundleManifestResourceImpl;
 import org.sourcepit.common.utils.io.IOOperation;
 import org.sourcepit.common.utils.xml.XmlUtils;
 import org.sourcepit.tpmp.ee.ExecutionEnvironmentSelector;
 import org.sourcepit.tpmp.resolver.TargetPlatformConfigurationHandler;
 import org.sourcepit.tpmp.resolver.TargetPlatformResolutionHandler;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 @Named
 public class TychoSessionTargetPlatformResolver
 {
    @Inject
    private Map<String, TychoProject> projectTypes;
 
    @Inject
    private DefaultTargetPlatformResolverFactory targetPlatformResolverLocator;
 
    @Inject
    private RepositorySystem repositorySystem;
 
    @Inject
    private ResolutionErrorHandler resolutionErrorHandler;
 
    @Inject
    private BundleReader bundleReader;
 
    @Inject
    private TychoDependencyResolver resolver;
 
    @Inject
    private EquinoxServiceFactory equinox;
 
    @Inject
    private Logger logger;
 
    @Inject
    private ExecutionEnvironmentSelector eeSelector;
 
    public void resolve(MavenSession session, boolean includeSource, TargetPlatformConfigurationHandler configHandler,
       TargetPlatformResolutionHandler resolutionHandler)
    {
       final TargetPlatformConfiguration aggregatedConfiguration = new TargetPlatformConfiguration();
       final LinkedHashSet<String> explodedBundles = new LinkedHashSet<String>();
       final LinkedHashSet<Dependency> frameworkExtensions = new LinkedHashSet<Dependency>();
       aggregateTargetPlatformConfigurations(session, aggregatedConfiguration, frameworkExtensions, explodedBundles);
       handleConfiguration(aggregatedConfiguration, configHandler);
 
       final List<ReactorProject> reactorProjects = DefaultReactorProject.adapt(session);
 
       final MavenProject project = setupAggregatedProject(session, reactorProjects, aggregatedConfiguration);
 
       final ContentCollector contentCollector = new ContentCollector(resolutionHandler);
 
       final TargetPlatform targetPlatform = resolve(session, project, contentCollector, explodedBundles,
          frameworkExtensions);
 
       if (includeSource)
       {
          resolveSources(session, targetPlatform, contentCollector, resolutionHandler);
       }
    }
 
    private void resolveSources(MavenSession session, final TargetPlatform targetPlatform,
       final ContentCollector contentCollector, TargetPlatformResolutionHandler handler)
    {
       final Map<File, MavenProject> projectsMap = new HashMap<File, MavenProject>();
       for (MavenProject mavenProject : session.getProjects())
       {
          projectsMap.put(mavenProject.getBasedir(), mavenProject);
       }
 
       final P2ResolverFactory factory = equinox.getService(P2ResolverFactory.class);
       final P2Resolver resolver = factory.createResolver(new MavenLoggerAdapter(logger, false));
 
       final Class<? extends TargetPlatform> clazz = targetPlatform.getClass();
       final Method method = getMethod(clazz, "getInstallableUnits");
       final Collection<?> units = invoke(method, targetPlatform);
 
       for (final Object unit : units)
       {
          if (hasSourceCapability(unit))
          {
             final String symbolicName = getSymbolicName(unit);
             final String version = getVersion(unit);
 
             final BundleManifest manifest = getManifest(unit);
 
             String[] targetIdAndVersion = getTargetIdAndVersion(manifest);
             if (targetIdAndVersion == null)
             {
                targetIdAndVersion = getTargetIdAndVersion(symbolicName, version);
             }
 
             if (targetIdAndVersion != null)
             {
                final String targetKey = targetIdAndVersion[0] + "_" + targetIdAndVersion[1];
                if (contentCollector.getPlugins().contains(targetKey))
                {
                   final P2ResolutionResult result = resolver.resolveInstallableUnit(targetPlatform, symbolicName,
                      version);
 
                   for (Entry entry : result.getArtifacts())
                   {
                      final File location = entry.getLocation();
                      if (location != null && location.exists())
                      {
                         handler.handlePlugin(entry.getId(), entry.getVersion(), location, false,
                            projectsMap.get(location));
                      }
                   }
                }
             }
          }
       }
    }
 
    private String[] getTargetIdAndVersion(String symbolicName, String version)
    {
       String targetId = null;
       if (symbolicName.endsWith(".source"))
       {
          targetId = symbolicName.substring(0, symbolicName.length() - ".source".length());
       }
       if (targetId == null || version == null)
       {
          return null;
       }
       return new String[] { targetId, version };
    }
 
    private String[] getTargetIdAndVersion(BundleManifest manifest)
    {
       String targetId = null;
       String version = null;
 
       final BundleSymbolicName bundleSymbolicName = manifest.getBundleSymbolicName();
       final Version bundleVersion = manifest.getBundleVersion();
       if (bundleSymbolicName != null && bundleVersion != null)
       {
          String symbolicName = bundleSymbolicName.getSymbolicName();
          String value = manifest.getHeaderValue("Eclipse-SourceBundle");
          if (value == null)
          {
             if (symbolicName.endsWith(".source"))
             {
                targetId = symbolicName.substring(0, symbolicName.length() - ".source".length());
             }
             version = bundleVersion.toFullString();
          }
          else
          {
             String[] segments = value.split(";");
             targetId = segments[0];
             for (int i = 1; i < segments.length; i++)
             {
                String segment = segments[i];
                if (segment.startsWith("version="))
                {
                   segment = segment.substring("version=".length());
                   if (segment.startsWith("\""))
                   {
                      segment = segment.substring(1);
                   }
                   if (segment.endsWith("\""))
                   {
                      segment = segment.substring(0, segment.length() - 1);
                   }
                   version = segment;
                   break;
                }
             }
          }
       }
 
       if (targetId == null || version == null)
       {
          return null;
       }
       return new String[] { targetId, version };
    }
 
    private static String getSymbolicName(Object unit)
    {
       final Method method = getMethod(unit.getClass(), "getId");
       return invoke(method, unit);
    }
 
    private static String getVersion(Object unit)
    {
       final Method method = getMethod(unit.getClass(), "getVersion");
       final Object version = invoke(method, unit);
       return version.toString();
    }
 
    private static BundleManifest getManifest(Object unit)
    {
       Method method = getMethod(unit.getClass(), "getTouchpointData");
       final Collection<?> points = invoke(method, unit);
       for (Object point : points)
       {
          // getInstruction(String instructionKey)
          method = getMethod(point.getClass(), "getInstruction", String.class);
          Object instruction = invoke(method, point, "manifest");
          if (instruction != null)
          {
             method = getMethod(instruction.getClass(), "getBody");
 
             String manifest = invoke(method, instruction);
 
             Resource resource = new BundleManifestResourceImpl();
             try
             {
                resource.load(new ByteArrayInputStream(manifest.getBytes("UTF-8")), null);
             }
             catch (IOException e)
             {
                throw pipe(e);
             }
             return (BundleManifest) resource.getContents().get(0);
          }
       }
       return null;
    }
 
    private static boolean hasSourceCapability(Object unit)
    {
       final Method method = getMethod(unit.getClass(), "getProvidedCapabilities");
       final Collection<?> capabilities = invoke(method, unit);
       for (Object capabilty : capabilities)
       {
          if (capabilty.toString().startsWith("org.eclipse.equinox.p2.eclipse.type/source/"))
          {
             return true;
          }
       }
       return false;
    }
 
    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... argTypes)
    {
       try
       {
          return clazz.getDeclaredMethod(methodName, argTypes);
       }
       catch (NoSuchMethodException e)
       {
          for (Class<?> interfaze : clazz.getInterfaces())
          {
             final Method method = getMethod(interfaze, methodName, argTypes);
             if (method != null)
             {
                return method;
             }
          }
          final Class<?> superclass = clazz.getSuperclass();
          if (superclass != null)
          {
             final Method method = getMethod(superclass, methodName, argTypes);
             if (method != null)
             {
                return method;
             }
          }
          return null;
       }
    }
 
    @SuppressWarnings("unchecked")
    private static <T> T invoke(Method method, Object target, Object... args)
    {
       try
       {
          return (T) method.invoke(target, args);
       }
       catch (IllegalAccessException e)
       {
          throw pipe(e);
       }
       catch (InvocationTargetException e)
       {
          final Throwable t = e.getTargetException();
          if (t instanceof RuntimeException)
          {
             throw (RuntimeException) t;
          }
          if (t instanceof Error)
          {
             throw (Error) t;
          }
          if (t instanceof Exception)
          {
             throw pipe((Exception) t);
          }
          throw new IllegalStateException(t);
       }
    }
 
    private static class ContentCollector implements TargetPlatformResolutionHandler
    {
       private final TargetPlatformResolutionHandler delegate;
 
       private final Set<String> plugins = new LinkedHashSet<String>();
 
       public ContentCollector(TargetPlatformResolutionHandler delegate)
       {
          this.delegate = delegate;
       }
 
       public void handleFeature(String id, String version, File location, MavenProject mavenProject)
       {
          delegate.handleFeature(id, version, location, mavenProject);
       }
 
       public void handlePlugin(String id, String version, File location, boolean unpack, MavenProject mavenProject)
       {
          plugins.add(id + "_" + version);
          delegate.handlePlugin(id, version, location, unpack, mavenProject);
       }
 
       public Set<String> getPlugins()
       {
          return plugins;
       }
    }
 
    private TargetPlatform resolve(MavenSession session, final MavenProject project,
       TargetPlatformResolutionHandler resolutionHandler, final LinkedHashSet<String> explodedBundles,
       final LinkedHashSet<Dependency> frameworkExtensions)
    {
      final List<ReactorProject> reactorProjects = new ArrayList<ReactorProject>(DefaultReactorProject.adapt(session));
      reactorProjects.remove(DefaultReactorProject.adapt(session.getCurrentProject()));
 
       final TargetPlatformResolver platformResolver = targetPlatformResolverLocator.lookupPlatformResolver(project);
 
       final TargetPlatform targetPlatform = platformResolver.computeTargetPlatform(session, project, reactorProjects);
 
       final DependencyResolverConfiguration resolverConfiguration = new DependencyResolverConfiguration()
       {
          public OptionalResolutionAction getOptionalResolutionAction()
          {
             return OptionalResolutionAction.OPTIONAL;
          }
 
          public List<Dependency> getExtraRequirements()
          {
             return TychoProjectUtils.getTargetPlatformConfiguration(project).getExtraRequirements();
          }
       };
 
       final DependencyArtifacts platformArtifacts = platformResolver.resolveDependencies(session, project,
          targetPlatform, reactorProjects, resolverConfiguration);
 
       if (platformArtifacts == null)
       {
          throw new IllegalStateException("Cannot determinate build target platform location -- not executing tests");
       }
 
       final Map<ReactorProject, MavenProject> projectsMap = new HashMap<ReactorProject, MavenProject>();
       for (MavenProject mavenProject : session.getProjects())
       {
          final ReactorProject reactorProject = DefaultReactorProject.adapt(mavenProject);
          projectsMap.put(reactorProject, mavenProject);
       }
 
       for (ArtifactDescriptor artifact : platformArtifacts.getArtifacts(ArtifactKey.TYPE_ECLIPSE_FEATURE))
       {
          final MavenProject mavenProject = getMavenProject(projectsMap, artifact);
 
          final File location = artifact.getLocation();
 
          // due to the feature model of Tycho violates the specified behaviour of the "unpack" attribute, we have to
          // parse the feature.xml on our own. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=386851.
          final Document feature = loadFeature(location);
          for (Node pluginNode : XmlUtils.queryNodes(feature, "/feature/plugin"))
          {
             final Element plugin = (Element) pluginNode;
             if (isUnpack(plugin))
             {
 
                explodedBundles.add(getId(plugin));
             }
          }
 
          final ArtifactKey key = artifact.getKey();
          resolutionHandler.handleFeature(key.getId(), key.getVersion(), location, mavenProject);
       }
 
       for (ArtifactDescriptor artifact : platformArtifacts.getArtifacts())
       {
          final ArtifactKey key = artifact.getKey();
          final String type = key.getType();
          if (ArtifactKey.TYPE_ECLIPSE_PLUGIN.equals(type) || ArtifactKey.TYPE_ECLIPSE_TEST_PLUGIN.equals(type))
          {
             final MavenProject mavenProject = getMavenProject(projectsMap, artifact);
             final boolean explodedBundle = isExplodedBundle(key.getId(), artifact.getLocation(), explodedBundles);
             resolutionHandler.handlePlugin(key.getId(), key.getVersion(), artifact.getLocation(), explodedBundle,
                mavenProject);
          }
       }
 
       processFrameworkExtensions(explodedBundles, getFrameworkExtensions(session, project, frameworkExtensions),
          resolutionHandler);
 
       return targetPlatform;
    }
 
    private void handleConfiguration(final TargetPlatformConfiguration aggregatedConfiguration,
       TargetPlatformConfigurationHandler configHandler)
    {
       for (TargetEnvironment te : aggregatedConfiguration.getEnvironments())
       {
          configHandler.handleTargetEnvironment(te.getOs(), te.getWs(), te.getArch(), te.getNl());
       }
 
       final String ee = aggregatedConfiguration.getExecutionEnvironment();
       if (ee != null)
       {
          configHandler.handleExecutionEnvironment(ee);
       }
    }
 
    private void processFrameworkExtensions(Set<String> explodedBundles, Collection<File> frameworkExtensions,
       TargetPlatformResolutionHandler handler)
    {
       for (File bundleFile : frameworkExtensions)
       {
          final OsgiManifest mf = bundleReader.loadManifest(bundleFile);
 
          final String symbolicName = mf.getBundleSymbolicName();
          final String version = mf.getBundleVersion();
 
          handler.handlePlugin(symbolicName, version, bundleFile,
             isExplodedBundle(symbolicName, bundleFile, explodedBundles), null);
       }
    }
 
    private boolean isExplodedBundle(String id, File location, Set<String> explodedBundles)
    {
       if (explodedBundles.contains(id))
       {
          return true;
       }
       return bundleReader.loadManifest(location).isDirectoryShape();
    }
 
    private List<File> getFrameworkExtensions(MavenSession session, MavenProject project,
       Collection<Dependency> frameworkExtensions)
    {
       final List<File> files = new ArrayList<File>();
 
       if (frameworkExtensions != null)
       {
          for (Dependency frameworkExtension : frameworkExtensions)
          {
             Artifact artifact = repositorySystem.createDependencyArtifact(frameworkExtension);
             ArtifactResolutionRequest request = new ArtifactResolutionRequest();
             request.setArtifact(artifact);
             request.setResolveRoot(true).setResolveTransitively(false);
             request.setLocalRepository(session.getLocalRepository());
             request.setRemoteRepositories(project.getPluginArtifactRepositories());
             request.setOffline(session.isOffline());
             request.setForceUpdate(session.getRequest().isUpdateSnapshots());
             ArtifactResolutionResult result = repositorySystem.resolve(request);
             try
             {
                resolutionErrorHandler.throwErrors(request, result);
             }
             catch (ArtifactResolutionException e)
             {
                throw new IllegalStateException("Failed to resolve framework extension "
                   + frameworkExtension.getManagementKey(), e);
             }
             files.add(artifact.getFile());
          }
       }
 
       return files;
    }
 
    private MavenProject getMavenProject(final Map<ReactorProject, MavenProject> projectsMap, ArtifactDescriptor artifact)
    {
       ReactorProject otherProject = artifact.getMavenProject();
       MavenProject mavenProject = null;
       if (otherProject != null)
       {
          mavenProject = projectsMap.get(otherProject);
          if (mavenProject == null)
          {
             throw new IllegalStateException();
          }
       }
       return mavenProject;
    }
 
    private boolean isUnpack(final Element plugin)
    {
       if (!plugin.hasAttribute("unpack"))
       {
          return true;
       }
       return Boolean.parseBoolean(plugin.getAttribute("unpack"));
    }
 
    private Document loadFeature(File location)
    {
       final Document[] result = new Document[1];
       new IOOperation<InputStream>(osgiIn(location, "feature.xml"))
       {
          @Override
          protected void run(InputStream openResource) throws IOException
          {
             result[0] = XmlUtils.readXml(openResource);
          }
       }.run();
       return result[0];
    }
 
    private String getId(Element plugin)
    {
       if (plugin.hasAttribute("id"))
       {
          return plugin.getAttribute("id");
       }
       return null;
    }
 
    private MavenProject setupAggregatedProject(MavenSession session, List<ReactorProject> reactorProjects,
       TargetPlatformConfiguration aggregatedConfiguration)
    {
       MavenProject origin = session.getCurrentProject();
       MavenProject fake = new MavenProject(origin.getModel());
       fake.setClassRealm(origin.getClassRealm());
       fake.setFile(origin.getFile());
       fake.setRemoteArtifactRepositories(origin.getRemoteArtifactRepositories());
       fake.setPluginArtifactRepositories(origin.getPluginArtifactRepositories());
       fake.setManagedVersionMap(origin.getManagedVersionMap());
 
       if (getTychoProject(fake) == null)
       {
          fake.setPackaging("eclipse-repository");
       }
 
       AbstractTychoProject tychoProject = (AbstractTychoProject) getTychoProject(fake);
       tychoProject.setupProject(session, fake);
 
       Properties properties = new Properties();
       properties.putAll(fake.getProperties());
       properties.putAll(session.getSystemProperties()); // session wins
       properties.putAll(session.getUserProperties());
       fake.setContextValue(TychoConstants.CTX_MERGED_PROPERTIES, properties);
 
       fake.setContextValue(TychoConstants.CTX_TARGET_PLATFORM_CONFIGURATION, aggregatedConfiguration);
 
       Map<String, DependencyMetadata> metadata = new LinkedHashMap<String, DependencyMetadata>();
 
       for (ReactorProject reactorProject : reactorProjects)
       {
          final Map<String, Set<Object>> dependencyMetadata = reactorProject.getDependencyMetadata();
          if (dependencyMetadata != null)
          {
             for (String classifier : dependencyMetadata.keySet())
             {
                DependencyMetadata dm = metadata.get(classifier);
                if (dm == null)
                {
                   dm = new DependencyMetadata();
                   metadata.put(classifier, dm);
                }
 
                mergeMetadata(dm, reactorProject, classifier, true);
                mergeMetadata(dm, reactorProject, classifier, false);
             }
          }
       }
      
       fake.setContextValue("tpmp.aggregatedMetadata", metadata);
 
       return fake;
    }
 
    private static class DependencyMetadata implements IDependencyMetadata
    {
 
       private Set<Object> metadata;
       private Set<Object> secondaryMetadata;
 
       public Set<Object /* IInstallableUnit */> getMetadata(boolean primary)
       {
          return primary ? metadata : secondaryMetadata;
       }
 
       public Set<Object /* IInstallableUnit */> getMetadata()
       {
          LinkedHashSet<Object> result = new LinkedHashSet<Object>();
          result.addAll(metadata);
          result.addAll(secondaryMetadata);
          return result;
       }
 
       public void setMetadata(boolean primary, Collection<?> units)
       {
          if (primary)
          {
             metadata = new LinkedHashSet<Object>(units);
          }
          else
          {
             secondaryMetadata = new LinkedHashSet<Object>(units);
          }
       }
 
    }
 
    private void mergeMetadata(DependencyMetadata dm, ReactorProject reactorProject, String classifier, boolean primary)
    {
       Set<Object> dependencyMetadata = reactorProject.getDependencyMetadata(classifier, primary);
       if (dependencyMetadata != null && !dependencyMetadata.isEmpty())
       {
          Set<Object> merged = dm.getMetadata(primary);
          if (merged == null)
          {
             merged = new LinkedHashSet<Object>();
          }
          merged.addAll(dependencyMetadata);
          dm.setMetadata(primary, merged);
       }
    }
 
    private void aggregateTargetPlatformConfigurations(MavenSession session,
       final TargetPlatformConfiguration aggregatedPlatform, final LinkedHashSet<Dependency> frameworkExtensions,
       LinkedHashSet<String> explodedBundles)
    {
       final LinkedHashSet<TargetEnvironment> environments = new LinkedHashSet<TargetEnvironment>();
       final LinkedHashSet<String> executionEnvironments = new LinkedHashSet<String>();
       final LinkedHashSet<ArtifactKey> requirements = new LinkedHashSet<ArtifactKey>();
       final LinkedHashSet<Dependency> dependencies = new LinkedHashSet<Dependency>();
       for (MavenProject project : session.getProjects())
       {
          final TychoProject tychoProject = getTychoProject(project);
          if (tychoProject != null)
          {
             final TargetPlatformConfiguration configuration = getTargetPlatformConfiguration(session, project);
             environments.addAll(configuration.getEnvironments());
             executionEnvironments.add(configuration.getExecutionEnvironment());
 
             final Boolean allow = aggregatedPlatform.getAllowConflictingDependencies();
             if (allow == null || allow.booleanValue() == false)
             {
                aggregatedPlatform.setAllowConflictingDependencies(configuration.getAllowConflictingDependencies());
             }
 
             final boolean disableP2Mirrors = aggregatedPlatform.isDisableP2Mirrors();
             if (!disableP2Mirrors)
             {
                aggregatedPlatform.setDisableP2Mirrors(configuration.isDisableP2Mirrors());
             }
 
             final boolean implicitTargetEnvironment = aggregatedPlatform.isImplicitTargetEnvironment();
             if (!implicitTargetEnvironment)
             {
                aggregatedPlatform.setImplicitTargetEnvironment(configuration.isDisableP2Mirrors());
             }
 
             final boolean includePackedArtifacts = aggregatedPlatform.isIncludePackedArtifacts();
             if (!includePackedArtifacts)
             {
                aggregatedPlatform.setIncludePackedArtifacts(configuration.isIncludePackedArtifacts());
             }
 
             final String pomDependencies = aggregatedPlatform.getPomDependencies();
             if (pomDependencies == null)
             {
                aggregatedPlatform.setPomDependencies(configuration.getPomDependencies());
             }
 
             final String targetPlatformResolver = aggregatedPlatform.getTargetPlatformResolver();
             if (targetPlatformResolver == null)
             {
                aggregatedPlatform.setResolver(configuration.getTargetPlatformResolver());
             }
 
             aggregatedPlatform.getFilters().addAll(configuration.getFilters());
 
             aggregatedPlatform.getExtraRequirements().addAll(configuration.getExtraRequirements());
 
             final TychoSurefirePluginConfiguration surefirePluginConfiguration = new TychoSurefirePluginConfigurationReader()
                .read(project);
             if (surefirePluginConfiguration != null)
             {
                dependencies.addAll(surefirePluginConfiguration.getDependencies());
                explodedBundles.addAll(surefirePluginConfiguration.getExplodedBundles());
                frameworkExtensions.addAll(surefirePluginConfiguration.getFrameworkExtensions());
             }
          }
       }
 
       aggregatedPlatform.getEnvironments().addAll(environments);
       aggregatedPlatform.setExecutionEnvironment(eeSelector.select(executionEnvironments));
 
       for (ArtifactKey requirement : requirements)
       {
          Dependency dependency = new Dependency();
          dependency.setArtifactId(requirement.getId());
          dependency.setVersion(requirement.getVersion());
          dependency.setType(requirement.getType());
 
          aggregatedPlatform.getExtraRequirements().add(dependency);
       }
 
       aggregatedPlatform.getExtraRequirements().addAll(dependencies);
    }
 
    private TychoProject getTychoProject(MavenProject project)
    {
       return projectTypes.get(project.getPackaging());
    }
 
    private TargetPlatformConfiguration getTargetPlatformConfiguration(MavenSession session, MavenProject project)
    {
       setupSessionLazy(session);
       return TychoProjectUtils.getTargetPlatformConfiguration(project);
    }
 
    private void setupSessionLazy(MavenSession session)
    {
       List<MavenProject> projects = session.getProjects();
       for (MavenProject project : projects)
       {
          setupProjectLazy(session, project);
       }
    }
 
    private void setupProjectLazy(MavenSession session, MavenProject project)
    {
       final TargetPlatformConfiguration targetPlatformConfiguration = (TargetPlatformConfiguration) project
          .getContextValue(TychoConstants.CTX_TARGET_PLATFORM_CONFIGURATION);
       if (targetPlatformConfiguration == null)
       {
          // project was not set up by Tycho. Maybe running in -Dtycho.mode=maven
          resolver.setupProject(session, project, DefaultReactorProject.adapt(project));
       }
    }
 }
