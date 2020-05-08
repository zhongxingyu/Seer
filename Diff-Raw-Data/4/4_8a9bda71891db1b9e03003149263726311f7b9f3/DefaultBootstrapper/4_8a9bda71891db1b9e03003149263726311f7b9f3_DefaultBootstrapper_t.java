 package org.rhegium.internal.bootstrap;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Handler;
 import java.util.logging.LogManager;
 
 import org.rhegium.api.bootstrap.Bootstrapper;
 import org.rhegium.api.lifecycle.LifecycleManager;
 import org.rhegium.api.modules.FrameworkPlugin;
 import org.rhegium.api.modules.PluginDependency;
 import org.rhegium.api.modules.PluginManager;
 import org.rhegium.internal.injector.ProvisionInterceptorFactory;
 import org.rhegium.internal.modules.FrameworkFirewallingClassLoader;
 import org.rhegium.internal.modules.ResolvablePluginDependency;
 import org.rhegium.internal.modules.PluginClassLoader;
 import org.rhegium.internal.modules.PluginContextHelper;
 import org.rhegium.internal.modules.PluginDescriptor;
 import org.rhegium.internal.modules.PluginThreadContext;
 import org.rhegium.internal.utils.ReflectionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 public class DefaultBootstrapper implements Bootstrapper {
 
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapper.class);
 
 	private static final String STANDARD_CONFIGURATION_FOLDER = "conf";
 
 	private static final String PROPERTIES_BOOTSTRAP_PRIVILEGED_PACKAGES = "bootstrap.framework.privileged.packages";
 	private static final String PROPERTIES_BOOTSTRAP_FRAMEWORK_MODULES = "bootstrap.framework.modules";
 	private static final String PROPERTIES_BOOTSTRAP_PLUGIN_FOLDER = "bootstrap.plugin.folder";
 	private static final String PROPERTIES_BOOTSTRAP_WORK_FOLDER = "bootstrap.work.folder";
 
 	private static final String STANDARD_PLUGIN_LIBRARY_FOLDER = "lib/plugins";
 	private static final String STANDARD_WORK_FOLDER = "work";
 
 	private FrameworkFirewallingClassLoader firewallingClassLoader;
 
 	@Override
 	public void start(ClassLoader classLoader) throws Exception {
 		// Deactivate java.util.logging
 		deactivateJULI();
 
 		// Read properties
 		final Properties properties = loadProperties();
 
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Adding ClassLoader to prevent access to base classes...");
 		}
 		firewallingClassLoader = new FrameworkFirewallingClassLoader(classLoader, loadMultiStringProperty(properties,
 				PROPERTIES_BOOTSTRAP_PRIVILEGED_PACKAGES));
 
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Actual ClassLoader Hierarchy: "
 					+ ReflectionUtils.buildClassLoaderHierachy(firewallingClassLoader));
 		}
 
 		final File pluginPath = findPluginsPath(properties);
 		final File workPath = findWorkPath(properties);
 
 		// Find all plugins
 		final Collection<ResolvablePluginDependency> pluginDescriptors = PluginContextHelper
 				.precheckAndReorderPluginDescriptors(buildPluginDescriptors(firewallingClassLoader, pluginPath,
 						workPath));
 
 		// Add buddy classloaders
 		addBuddyClassLoaders(pluginDescriptors);
 
 		final Collection<Module> modules = loadAndBuildFrameworkModules(properties, classLoader);
 
 		final Collection<FrameworkPlugin> plugins = new ArrayList<FrameworkPlugin>();
 		for (final ResolvablePluginDependency pluginDescriptor : pluginDescriptors) {
 			LOG.info("Creating bundle "
 					+ pluginDescriptor.getName()
 					+ (!pluginDescriptor.isApiBundle() ? " by using Class '"
 							+ pluginDescriptor.getPluginClass().getCanonicalName() : " (API-Bundle)") + "...");
 
 			if (!pluginDescriptor.isApiBundle()) {
 				final FrameworkPlugin plugin = pluginDescriptor.getPluginClass().newInstance();
 
 				// Add plugin
 				plugins.add(plugin);
 
 				LOG.info("Configuring Injector for Plugin '" + plugin.getName() + "'...");
 
 				final Module module = new PluginThreadContext<Module>(pluginDescriptor.getPluginClassLoader()) {
 
 					@Override
 					public Module run() {
 						return plugin.configure();
 					}
 				}.execute();
 
 				if (module != null) {
 					modules.add(module);
 				}
 			}
 		}
 
 		// Building Guice injector and retrieve the implementation instance of
 		// ILifecycleManager to start it up
 		final Injector injector = Guice.createInjector(new ProvisionInterceptorFactory().install(modules).build());
 
 		// Get PluginManager
 		final PluginManager pluginManager = injector.getInstance(PluginManager.class);
 
 		// Get LifecycleManager
 		final LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);
 
 		if (pluginManager instanceof PluginManager) {
 			final PluginManager pm = (PluginManager) pluginManager;
 
 			for (final FrameworkPlugin plugin : plugins) {
 				LOG.info("Registering Plugin '" + plugin.getName() + "'...");
 				pm.registerPlugin(plugin);
 			}
 		}
 
 		// Startup framework
 		LOG.info("Initialize plugins...");
 		lifecycleManager.initialize();
 
 		LOG.info("Startup plugins...");
 		lifecycleManager.startup();
 	}
 
 	private void deactivateJULI() {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Redirect java.util.logging to SLF4J just before start...");
 			final java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
 
 			for (final Handler handler : rootLogger.getHandlers()) {
 				rootLogger.removeHandler(handler);
 			}
 
 			SLF4JBridgeHandler.install();
 		}
 	}
 
 	private Properties loadProperties() throws IOException {
 		final File configDirectory = new File(STANDARD_CONFIGURATION_FOLDER);
 		final Properties properties = new Properties();
 		properties.load(new FileReader(new File(configDirectory, "framework.properties")));
 
 		return properties;
 	}
 
 	private File findPluginsPath(final Properties properties) {
 		final String pluginsFolder = getProperty(properties, PROPERTIES_BOOTSTRAP_PLUGIN_FOLDER,
 				STANDARD_PLUGIN_LIBRARY_FOLDER);
 
 		final File pluginPath = new File(pluginsFolder);
 		if (!pluginPath.exists() && !pluginPath.isDirectory()) {
 			throw new IllegalArgumentException(PROPERTIES_BOOTSTRAP_PLUGIN_FOLDER + " must exists and be a directory");
 		}
 		return pluginPath;
 	}
 
 	private List<ResolvablePluginDependency> buildPluginDescriptors(final ClassLoader classLoader,
 			final File pluginPath, final File workPath) {
 
 		return AccessController.doPrivileged(new PrivilegedAction<List<ResolvablePluginDependency>>() {
 
 			@Override
 			public List<ResolvablePluginDependency> run() {
 				LOG.info("Searching framework plugins in " + pluginPath.getAbsolutePath() + "...");
 
 				final List<ResolvablePluginDependency> pluginDescriptors = new ArrayList<ResolvablePluginDependency>();
 				for (final File child : pluginPath.listFiles()) {
 					final PluginDescriptor pluginDescriptor = PluginContextHelper.buildPluginDescriptor(child,
 							workPath, classLoader);
 
 					if (pluginDescriptor != null) {
 						LOG.info("Found plugin '" + pluginDescriptor.getName() + "' in path " + child.getAbsolutePath());
 
 						pluginDescriptors.add(pluginDescriptor);
 					}
 				}
 				return pluginDescriptors;
 			}
 		});
 	}
 
 	private void addBuddyClassLoaders(final Collection<ResolvablePluginDependency> pluginDescriptors) {
 		for (final ResolvablePluginDependency pluginDescriptor : pluginDescriptors) {
 			final PluginClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
 
 			// Find buddy in all resolved dependencies
 			for (final PluginDependency<PluginClassLoader> dependency : pluginDescriptor.getDependencies()) {
 
 				pluginClassLoader.addBuddyClassLoader(dependency.getPluginClassLoader());
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<Module> loadAndBuildFrameworkModules(final Properties properties, final ClassLoader classLoader)
 			throws ReflectiveOperationException {
 
 		final String[] values = loadMultiStringProperty(properties, PROPERTIES_BOOTSTRAP_FRAMEWORK_MODULES);
 
 		final Collection<Module> modules = new ArrayList<Module>();
 
 		for (final String moduleName : values) {
 			final Class<? extends Module> moduleClass;
 			moduleClass = (Class<? extends Module>) classLoader.loadClass(moduleName);
 
 			modules.add(moduleClass.newInstance());
 		}
 
 		return modules;
 	}
 
 	private File findWorkPath(final Properties properties) throws IOException {
 		final String workFolder = getProperty(properties, PROPERTIES_BOOTSTRAP_WORK_FOLDER, STANDARD_WORK_FOLDER);
 
 		final File workPath = new File(workFolder);
 		if (workPath.exists()) {
 			if (!workPath.isDirectory()) {
 				throw new IllegalArgumentException(PROPERTIES_BOOTSTRAP_WORK_FOLDER + " must be a directory");
 			}
 
 			// Delete old work directory
 			deleteFullPath(workPath);
 		}
 
 		// (Re-) Create the work directory
 		workPath.mkdirs();
 		return workPath;
 	}
 
 	private void deleteFullPath(final File workPath) {
 		try {
 			Files.walkFileTree(workPath.toPath(), new SimpleFileVisitor<Path>() {
 
 				@Override
 				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
 
 					Files.delete(file);
 					return FileVisitResult.CONTINUE;
 				}
 
 				@Override
 				public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
 
 					if (exc == null) {
 						Files.delete(dir);
 					}
 
 					return FileVisitResult.CONTINUE;
 				}
 			});
 		}
 		catch (final IOException e) {
 			throw new RuntimeException("Work directory could not be deleted", e);
 		}
 	}
 
 	private String[] loadMultiStringProperty(final Properties properties, final String propertyName) {
 		final String value = properties.getProperty(propertyName);
 		if (value == null || value.isEmpty()) {
 			throw new IllegalArgumentException(propertyName + " cannot be null");
 		}
 
 		final String[] values = trimStrings(value.split(","));
 		return values;
 	}
 
 	private static String[] trimStrings(final String[] packages) {
 		final String[] temp = new String[packages.length];
 
 		for (int i = 0; i < packages.length; i++) {
 			temp[i] = packages[i].trim();
 		}
 
 		return temp;
 	}
 
 	private String getProperty(final Properties properties, final String propertyName, final String defaultValue) {
 		final String value = System.getProperty(propertyName);
 		if (value != null && !value.isEmpty()) {
 			return value;
 		}
 
 		return properties.getProperty(propertyName, defaultValue);
 	}
 }
