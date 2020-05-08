 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.test.framework;
 
 import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.virgo.osgi.extensions.equinox.hooks.BundleFileClosingBundleFileWrapperFactoryHook;
 import org.eclipse.virgo.osgi.launcher.FrameworkBuilder;
 import org.eclipse.virgo.osgi.launcher.FrameworkBuilder.FrameworkCustomizer;
 import org.eclipse.virgo.test.framework.plugin.PluginManager;
 import org.eclipse.virgo.util.common.PropertyPlaceholderResolver;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.FrameworkListener;
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.startlevel.FrameworkStartLevel;
 
 import javax.management.JMException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 /**
  * JUnit TestRunner for running OSGi integration tests on the Equinox framework.
  * <p/>
  *
  * <strong>Concurrent Semantics</strong><br />
  * TODO Document concurrent semantics of OsgiTestRunner
  */
 public class OsgiTestRunner extends BlockJUnit4ClassRunner {
     
     private static final int DEFAULT_BUNDLE_START_LEVEL = 4;
 
     private final ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();
 
     private final PluginManager pluginManager;
 
     private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
     private final ObjectName searchObjectName;
 
     public OsgiTestRunner(Class<?> klass) throws InitializationError, MalformedObjectNameException {
         super(klass);
         this.pluginManager = new PluginManager(klass);
         this.searchObjectName = new ObjectName("org.eclipse.virgo.*:*");
     }
 
     private void stupidEquinoxHack() {
         try {
             Field field = FrameworkProperties.class.getDeclaredField("properties");
             synchronized (FrameworkProperties.class) {
                 field.setAccessible(true);
                 field.set(null, null);
             }
         } catch (Exception e) {
             throw new RuntimeException("Unable to hack Equinox", e);
         }
     }
 
     @Override
     public final void run(RunNotifier notifier) {
 
         Framework framework = null;
 
         try {
             stupidEquinoxHack();
 
             // Preserve and re-instate the context classloader since tests can sometimes leave it in a strange state.
             ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
             try {
                 final Properties configurationProperties = createConfigurationProperties();
                 framework = launchOsgi(configurationProperties);
                 BundleContext targetBundleContext = getTargetBundleContext(framework.getBundleContext());
                 postProcessTargetBundleContext(targetBundleContext, configurationProperties);
                 Bundle testBundle = installAndStartTestBundle(targetBundleContext);
                 Class<?> osgiTestClass = createOsgiTestClass(testBundle);
                 // create the real runner, dispatch it against the class loaded from OSGi
                 BlockJUnit4ClassRunner realRunner = new BlockJUnit4ClassRunner(osgiTestClass);
                 realRunner.run(notifier);
             } finally {
                 Thread.currentThread().setContextClassLoader(classLoader);
             }
         } catch (Throwable e) {
             notifier.fireTestFailure(new Failure(getDescription(), e));
         } finally {
             if (framework != null) {
                 try {
                     framework.stop();
                     framework.waitForStop(30000);
                 } catch (Throwable e) {
                     e.printStackTrace();
                 }
             }
             unregisterVirgoMBeans(notifier);
            BundleFileClosingBundleFileWrapperFactoryHook.getInstance().cleanup();
         }
     }
 
     private void unregisterVirgoMBeans(RunNotifier notifier) {
         Set<ObjectName> objectNames = this.server.queryNames(this.searchObjectName, null);
 
         if (objectNames.size() == 0) {
             return;
         }
 
         for (ObjectName objectName : objectNames) {
             try {
                 this.server.unregisterMBean(objectName);
             } catch (JMException jme) {
                 jme.printStackTrace();
             }
         }
 
         notifier.fireTestFailure(new Failure(getDescription(),
                                              new IllegalStateException("The mBeans " + objectNames +
                                                                        " were not unregistered.")));
     }
 
     private Bundle installAndStartTestBundle(BundleContext targetBundleContext) throws BundleException {
         Bundle testBundle = targetBundleContext.installBundle(getTestBundleLocation());
         testBundle.start();
         return testBundle;
     }
 
     /**
      * Returns the {@link BundleContext} that should be used to install the test bundle
      *
      * @return the target <code>BundleContext</code>.
      */
     protected BundleContext getTargetBundleContext(BundleContext bundleContext) {
         return bundleContext;
     }
 
     protected void postProcessTargetBundleContext(BundleContext bundleContext, Properties frameworkProperties) throws Exception {
         // nothing for this implementation...
     }
 
     // load the test class from within OSGi
     private Class<?> createOsgiTestClass(Bundle testBundle) throws ClassNotFoundException {
         Class<?> osgiJavaTestClass = testBundle.loadClass(getTestClass().getName());
         Class<?> osgiTestClass = osgiJavaTestClass;
         return osgiTestClass;
     }
 
     // launch the OSGi framework. will also install the test bundle
     private Framework launchOsgi(Properties frameworkProperties) throws Exception {
         final Properties configurationProperties = new Properties(frameworkProperties);
         FrameworkBuilder builder = new FrameworkBuilder(configurationProperties, new FrameworkCustomizer() {
 
             public void beforeInstallBundles(Framework framework) {
 
                 /*
                  * Use the same default start level as the user region bundles. Program the framework start level
                  * instance defensively to allow for stubs which don't understand adapt.
                  */
                 FrameworkStartLevel frameworkStartLevel = (FrameworkStartLevel) framework.getBundleContext().getBundle(0).adapt(
                     FrameworkStartLevel.class);
                 if (frameworkStartLevel != null) {
                     final CountDownLatch latch = new CountDownLatch(1);
                     frameworkStartLevel.setStartLevel(DEFAULT_BUNDLE_START_LEVEL, new FrameworkListener() {
 
                         @Override
                         public void frameworkEvent(FrameworkEvent event) {
                             if (FrameworkEvent.STARTLEVEL_CHANGED == event.getType()) {
                                 latch.countDown();
                             }
                             
                         }});
                     try {
                         latch.await(30000, TimeUnit.MILLISECONDS);
                     } catch (InterruptedException e) {
                         throw new RuntimeException("Start level latch interrupted", e);
                     }
                     frameworkStartLevel.setInitialBundleStartLevel(DEFAULT_BUNDLE_START_LEVEL);
                 }
 
                 OsgiTestRunner.this.pluginManager.getPluginDelegate().beforeInstallBundles(framework, configurationProperties);
             }
 
             public void afterInstallBundles(Framework framework) {
 
             }
         });
         addUserConfiguredBundles(builder, configurationProperties);
         return builder.start();
     }
 
     private void addUserConfiguredBundles(FrameworkBuilder builder, Properties configurationProperties) throws Exception {
         Class<BundleDependencies> annotationType = BundleDependencies.class;
         Class<?> annotationDeclaringClazz = TestFrameworkUtils.findAnnotationDeclaringClass(annotationType, getTestClass().getJavaClass());
 
         if (annotationDeclaringClazz == null) {
             // could not find an 'annotation declaring class' for annotation + annotationType + and targetType +
             // startFromClazz
             return;
         }
 
         List<BundleEntry> bundleEntries = new ArrayList<BundleEntry>();
 
         while (annotationDeclaringClazz != null) {
             BundleDependencies dependencies = annotationDeclaringClazz.getAnnotation(annotationType);
             BundleEntry[] entries = dependencies.entries();
 
             bundleEntries.addAll(0, Arrays.<BundleEntry> asList(entries));
             annotationDeclaringClazz = dependencies.inheritDependencies() ? TestFrameworkUtils.findAnnotationDeclaringClass(annotationType,
                 annotationDeclaringClazz.getSuperclass()) : null;
         }
         PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();
         for (BundleEntry entry : bundleEntries) {
             final String formattedPath = resolver.resolve(entry.value(), configurationProperties);
             builder.addBundle(new URI(formattedPath), entry.autoStart());
         }
 
     }
 
     private String getTestBundleLocation() {
         return BundleLocationLocator.determineBundleLocation(getTestClass().getJavaClass());
     }
 
     private Properties createConfigurationProperties() throws Exception {
         return this.loader.loadConfigurationProperties(getTestClass().getJavaClass());
     }
 }
