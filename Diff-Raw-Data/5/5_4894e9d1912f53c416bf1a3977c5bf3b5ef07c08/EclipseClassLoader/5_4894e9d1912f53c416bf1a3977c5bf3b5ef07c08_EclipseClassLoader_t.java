 /*******************************************************************************
  * Copyright (c) 2003,2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.runtime.adaptor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 import org.eclipse.osgi.framework.adaptor.*;
 import org.eclipse.osgi.framework.adaptor.core.*;
 import org.eclipse.osgi.framework.internal.core.AbstractBundle;
 import org.eclipse.osgi.framework.internal.core.Msg;
 import org.eclipse.osgi.framework.internal.defaultadaptor.DefaultClassLoader;
 import org.eclipse.osgi.framework.internal.defaultadaptor.DevClassPathHelper;
 import org.eclipse.osgi.framework.log.FrameworkLogEntry;
 import org.eclipse.osgi.framework.stats.*;
 import org.osgi.framework.*;
 
 /**
  * Internal class.
  */
 public class EclipseClassLoader extends DefaultClassLoader {
 	private static String[] NL_JAR_VARIANTS = buildNLJarVariants(EnvironmentInfo.getDefault().getNL());
 	private static boolean DEFINE_PACKAGES;
 	static {
 		try {
 			Class.forName("java.lang.Package"); //$NON-NLS-1$
 			DEFINE_PACKAGES = true;
 		} catch (ClassNotFoundException e) {
 			DEFINE_PACKAGES = false;
 		}
 	}
 
 	public EclipseClassLoader(ClassLoaderDelegate delegate, ProtectionDomain domain, String[] classpath, ClassLoader parent, BundleData bundleData) {
 		super(delegate, domain, classpath, parent, (org.eclipse.osgi.framework.internal.defaultadaptor.DefaultBundleData) bundleData);
 	}
 
 	public Class findLocalClass(String className) throws ClassNotFoundException {
 		if (StatsManager.MONITOR_CLASSES) //Suport for performance analysis
 			ClassloaderStats.startLoadingClass(getClassloaderId(), className);
 		boolean found = true;
 
 		try {
 			AbstractBundle bundle = (AbstractBundle) hostdata.getBundle();
 			// If the bundle is active, uninstalled or stopping then the bundle has already
 			// been initialized (though it may have been destroyed) so just return the class.
			if ((bundle.getState() & (Bundle.ACTIVE | Bundle.UNINSTALLED | Bundle.STOPPING)) != 0)
 				return basicFindLocalClass(className);
 
 			// The bundle is not active and does not require activation, just return the class
 			if (!shouldActivateFor(className))
 				return basicFindLocalClass(className);
 
 			// The bundle is starting.  Note that if the state changed between the tests 
 			// above and this test (e.g., it was not ACTIVE but now is), that's ok, we will 
 			// just try to start it again (else case).
 			// TODO need an explanation here of why we duplicated the mechanism 
 			// from the framework rather than just calling start() and letting it sort it out.
			if (bundle.getState() == Bundle.STARTING) {
 				// If the thread trying to load the class is the one trying to activate the bundle, then return the class 
 				if (bundle.testStateChanging(Thread.currentThread()) || bundle.testStateChanging(null))
 					return basicFindLocalClass(className);
 
 				// If it's another thread, we wait and try again. In any case the class is returned. 
 				// The difference is that an exception can be logged.
 				// TODO do we really need this test?  We just did it on the previous line?
 				if (!bundle.testStateChanging(Thread.currentThread())) {
 					Thread threadChangingState = bundle.getStateChanging();
 					if (StatsManager.TRACE_BUNDLES && threadChangingState != null) {
 						System.out.println("Concurrent startup of bundle " + bundle.getSymbolicName() + " by " + Thread.currentThread() + " and " + threadChangingState.getName() + ". Waiting up to 5000ms for " + threadChangingState + " to finish the initialization."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 					}
 					Object lock = bundle.getStateChangeLock();
 					long start = System.currentTimeMillis();
 					long delay = 5000;
 					long timeLeft = delay;
 					while (true) {
 						if (bundle.testStateChanging(null) || timeLeft <= 0)
 							break;
 						try {
 							synchronized (lock) {
 								lock.wait(timeLeft);
 							}
 						} catch (InterruptedException e) {
 							//Ignore and keep waiting
 						}
 						timeLeft = start + delay - System.currentTimeMillis();
 					}
 					if (timeLeft <= 0 || bundle.getState() != Bundle.ACTIVE) {
 						String bundleName = bundle.getSymbolicName() == null ? Long.toString(bundle.getBundleId()) : bundle.getSymbolicName();
 						String message = EclipseAdaptorMsg.formatter.getString("ECLIPSE_CLASSLOADER_CONCURRENT_STARTUP", new Object[] {Thread.currentThread().getName(), className, threadChangingState.getName(), bundleName, Long.toString(delay)}); //$NON-NLS-1$ 
 						EclipseAdaptor.getDefault().getFrameworkLog().log(new FrameworkLogEntry(FrameworkAdaptor.FRAMEWORK_SYMBOLICNAME, message, 0, new Exception(EclipseAdaptorMsg.formatter.getString("ECLIPSE_CLASSLOADER_GENERATED_EXCEPTION")), null)); //$NON-NLS-1$
 					}
 					return basicFindLocalClass(className);
 				}
 			}
 
 			//The bundle must be started.
 			try {
 				hostdata.getBundle().start();
 			} catch (BundleException e) {
 				String message = EclipseAdaptorMsg.formatter.getString("ECLIPSE_CLASSLOADER_ACTIVATION", bundle.getSymbolicName(), Long.toString(bundle.getBundleId())); //$NON-NLS-1$
 				EclipseAdaptor.getDefault().getFrameworkLog().log(new FrameworkLogEntry(FrameworkAdaptor.FRAMEWORK_SYMBOLICNAME, message, 0, e, null));
 				throw new ClassNotFoundException(className, e);
 			}
 			return basicFindLocalClass(className);
 		} catch (ClassNotFoundException e) {
 			found = false;
 			throw e;
 		} finally {
 			if (StatsManager.MONITOR_CLASSES)
 				ClassloaderStats.endLoadingClass(getClassloaderId(), className, found);
 		}
 	}
 
 	/**
 	 * Do the basic work for finding a class. This avoids the activation detection etc
 	 * and can be used by subclasses to override the default (from the superclass) 
 	 * way of finding classes.
 	 * @param name the class to look for
 	 * @return the found class
 	 * @throws ClassNotFoundException if the requested class cannot be found
 	 */
 	protected Class basicFindLocalClass(String name) throws ClassNotFoundException {
 		return super.findLocalClass(name);
 	}
 
 	/**
 	 * Determines if for loading the given class we should activate the bundle. 
 	 */
 	private boolean shouldActivateFor(String className) throws ClassNotFoundException {
 		//Don't reactivate on shut down
 		if (hostdata.getAdaptor().isStopping()) {
 			BundleStopper stopper = EclipseAdaptor.getDefault().getBundleStopper();
 			if (stopper != null && stopper.isStopped(hostdata.getBundle())) {
 				String message = EclipseAdaptorMsg.formatter.getString("ECLIPSE_CLASSLOADER_ALREADY_STOPPED", className, hostdata.getSymbolicName()); //$NON-NLS-1$
 				ClassNotFoundException exception = new ClassNotFoundException(message);
 				EclipseAdaptor.getDefault().getFrameworkLog().log(new FrameworkLogEntry(FrameworkAdaptor.FRAMEWORK_SYMBOLICNAME, message, 0, exception, null)); //$NON-NLS-1$
 				throw exception;
 			}
 		}
 		boolean autoStart = ((EclipseBundleData) hostdata).isAutoStart();
 		String[] autoStartExceptions = ((EclipseBundleData) hostdata).getAutoStartExceptions();
 		// no exceptions, it is easy to figure it out
 		if (autoStartExceptions == null)
 			return autoStart;
 		// otherwise, we need to check if the package is in the exceptions list
 		int dotPosition = className.lastIndexOf('.');
 		// the class has no package name... no exceptions apply
 		if (dotPosition == -1)
 			return autoStart;
 		String packageName = className.substring(0, dotPosition);
 		// should activate if autoStart and package is not an exception, or if !autoStart and package is exception
 		return autoStart ^ contains(autoStartExceptions, packageName);
 	}
 
 	private boolean contains(String[] array, String element) {
 		for (int i = 0; i < array.length; i++)
 			if (array[i].equals(element))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Override defineClass to allow for package defining.
 	 */
 	protected Class defineClass(String name, byte[] classbytes, int off, int len, ClasspathEntry classpathEntry) throws ClassFormatError {
 		if (!DEFINE_PACKAGES)
 			return super.defineClass(name, classbytes, off, len, classpathEntry);
 
 		// Define the package if it is not the default package.
 		int lastIndex = name.lastIndexOf('.');
 		if (lastIndex != -1) {
 			String packageName = name.substring(0, lastIndex);
 			Package pkg = getPackage(packageName);
 			if (pkg == null) {
 				// get info about the package from the classpath entry's manifest.
 				String specTitle = null, specVersion = null, specVendor = null, implTitle = null, implVersion = null, implVendor = null;
 				Manifest mf = ((EclipseClasspathEntry) classpathEntry).getManifest();
 				if (mf != null) {
 					Attributes mainAttributes = mf.getMainAttributes();
 					String dirName = packageName.replace('.', '/') + '/';
 					Attributes packageAttributes = mf.getAttributes(dirName);
 					boolean noEntry = false;
 					if (packageAttributes == null) {
 						noEntry = true;
 						packageAttributes = mainAttributes;
 					}
 					specTitle = packageAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
 					if (specTitle == null && !noEntry)
 						specTitle = mainAttributes.getValue(Attributes.Name.SPECIFICATION_TITLE);
 					specVersion = packageAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
 					if (specVersion == null && !noEntry)
 						specVersion = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
 					specVendor = packageAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
 					if (specVendor == null && !noEntry)
 						specVendor = mainAttributes.getValue(Attributes.Name.SPECIFICATION_VENDOR);
 					implTitle = packageAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
 					if (implTitle == null && !noEntry)
 						implTitle = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
 					implVersion = packageAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
 					if (implVersion == null && !noEntry)
 						implVersion = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
 					implVendor = packageAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
 					if (implVendor == null && !noEntry)
 						implVendor = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
 				}
 				// The package is not defined yet define it before we define the class.
 				// TODO still need to seal packages.
 				definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, null);
 			}
 		}
 		return super.defineClass(name, classbytes, off, len, classpathEntry);
 	}
 
 	private String getClassloaderId() {
 		return hostdata.getBundle().getSymbolicName();
 	}
 
 	public URL getResource(String name) {
 		URL result = super.getResource(name);
 		if (StatsManager.MONITOR_RESOURCES) {
 			if (result != null && name.endsWith(".properties")) { //$NON-NLS-1$
 				ClassloaderStats.loadedBundle(getClassloaderId(), new ResourceBundleStats(getClassloaderId(), name, result));
 			}
 		}
 		return result;
 	}
 
 	protected void findClassPathEntry(ArrayList result, String entry, AbstractBundleData bundledata, ProtectionDomain domain) {
 		String var = hasPrefix(entry);
 		if (var == null) {
 			super.findClassPathEntry(result, entry, bundledata, domain);
 			return;
 		}
 		if (var.equals("ws")) { //$NON-NLS-1$
 			super.findClassPathEntry(result, "ws/" + EnvironmentInfo.getDefault().getWS() + entry.substring(4), bundledata, domain); //$NON-NLS-1$
 			return;
 		}
 		if (var.equals("os")) { //$NON-NLS-1$
 			super.findClassPathEntry(result, "os/" + EnvironmentInfo.getDefault().getOS() + entry.substring(4), bundledata, domain); //$NON-NLS-1$ 
 			return;
 		}
 		if (var.equals("nl")) { //$NON-NLS-1$
 			entry = entry.substring(4);
 			for (int i = 0; i < NL_JAR_VARIANTS.length; i++) {
 				if (addClassPathEntry(result, "nl/" + NL_JAR_VARIANTS[i] + entry, bundledata, domain)) //$NON-NLS-1$ //$NON-NLS-2$
 					return;
 			}
 			// is we are not in development mode, post some framework errors.
 			if (!DevClassPathHelper.inDevelopmentMode()) {
 				BundleException be = new BundleException(Msg.formatter.getString("BUNDLE_CLASSPATH_ENTRY_NOT_FOUND_EXCEPTION", entry, hostdata.getLocation())); //$NON-NLS-1$
 				bundledata.getAdaptor().getEventPublisher().publishFrameworkEvent(FrameworkEvent.ERROR, bundledata.getBundle(), be);
 			}
 		}
 	}
 
 	private static String[] buildNLJarVariants(String nl) {
 		ArrayList result = new ArrayList();
 		nl = nl.replace('_', '/');
 		while (nl.length() > 0) {
 			result.add("nl/" + nl + "/"); //$NON-NLS-1$ //$NON-NLS-2$
 			int i = nl.lastIndexOf('/'); //$NON-NLS-1$
 			nl = (i < 0) ? "" : nl.substring(0, i); //$NON-NLS-1$
 		}
 		result.add(""); //$NON-NLS-1$
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	//return a String representing the string found between the $s
 	private String hasPrefix(String libPath) {
 		if (libPath.startsWith("$ws$")) //$NON-NLS-1$
 			return "ws"; //$NON-NLS-1$
 		if (libPath.startsWith("$os$")) //$NON-NLS-1$
 			return "os"; //$NON-NLS-1$
 		if (libPath.startsWith("$nl$")) //$NON-NLS-1$
 			return "nl"; //$NON-NLS-1$
 		return null;
 	}
 
 	/**
 	 * Override to create EclipseClasspathEntry objects.  EclipseClasspathEntry
 	 * allows access to the manifest file for the classpath entry.
 	 */
 	protected ClasspathEntry createClassPathEntry(BundleFile bundlefile, ProtectionDomain domain) {
 		return new EclipseClasspathEntry(bundlefile, domain);
 	}
 
 	/**
 	 * A ClasspathEntry that has a manifest associated with it.
 	 */
 	protected class EclipseClasspathEntry extends ClasspathEntry {
 		Manifest mf;
 		boolean initMF = false;
 
 		protected EclipseClasspathEntry(BundleFile bundlefile, ProtectionDomain domain) {
 			super(bundlefile, domain);
 		}
 
 		public Manifest getManifest() {
 			if (initMF)
 				return mf;
 
 			BundleEntry mfEntry = getBundleFile().getEntry(org.eclipse.osgi.framework.internal.core.Constants.OSGI_BUNDLE_MANIFEST);
 			if (mfEntry != null)
 				try {
 					InputStream manIn = mfEntry.getInputStream();
 					mf = new Manifest(manIn);
 					manIn.close();
 				} catch (IOException e) {
 					// do nothing
 				}
 			initMF = true;
 			return mf;
 		}
 	}
 }
