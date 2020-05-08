 /* Copyright (c) 2006 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ch.ethz.iks.concierge.framework;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLStreamHandler;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.service.log.LogService;
 
 /**
  * The Classloader for an OSGi Bundle. Manages classes and resources from the
  * own bundle and delegations to other bundles by package imports.
  * 
  * @author Jan S. Rellermeyer, IKS, ETH Zurich
  */
 final class BundleClassLoader extends ClassLoader {
 
 	/**
 	 * the default name of stored bundles.
 	 */
 	private static final String BUNDLE_FILE_NAME = "bundle";
 
 	/**
 	 * the exports of this bundle.
 	 */
 	String[] exports = new String[0];
 
 	/**
 	 * the imports of this bundle.
 	 */
 	String[] imports = new String[0];
 
 	/**
 	 * the bundle activator's class name.
 	 */
 	String activatorClassName = null;
 
 	/**
 	 * the bundle activator instance.
 	 */
 	BundleActivator activator = null;
 
 	/**
 	 * reference to the bundle object.
 	 */
 	BundleImpl bundle;
 
 	/**
 	 * the delegations for imports.
 	 */
 	Map importDelegations;
 
 	/**
 	 * the jar file.
 	 */
 	private final JarFile jarFile;
 
 	/**
 	 * the storage location.
 	 */
 	final String storageLocation;
 
 	/**
 	 * 
 	 */
 	private String[] classpathStrings;
 
 	/**
 	 * the classpath.
 	 */
 	private String[] classpath;
 
 	/**
 	 * the native libraries, if any.
 	 */
 	private Map nativeLibraries;
 
 	/**
 	 * dynamic imports, if any.
 	 */
 	private String[] dynamicImports = null;
 
 	/**
 	 * the originally exporting BundleClassLoader.
 	 */
 	BundleClassLoader originalExporter = null;
 
 	/**
 	 * the packages provided by the framework.
 	 */
 	static final HashSet FRAMEWORK_PACKAGES = new HashSet(4);
 	static {
 		FRAMEWORK_PACKAGES.add("org.osgi.framework");
 		FRAMEWORK_PACKAGES.add("org.osgi.service.log");
 		FRAMEWORK_PACKAGES.add("org.osgi.service.packageadmin");
 		FRAMEWORK_PACKAGES.add("org.osgi.service.startlevel");
 	}
 
 	/**
 	 * an empty vector instance.
 	 */
 	private static final Vector EMPTY_VECTOR = new Vector(0);
 
 	/**
 	 * create a new bundle classloader.
 	 * 
 	 * @param bundle
 	 *            the bundle object.
 	 * @param stream
 	 *            the input stream.
 	 * @throws BundleException
 	 *             in case of IO errors.
 	 */
 	BundleClassLoader(final BundleImpl bundle, final InputStream stream)
 			throws BundleException {
 		this.bundle = bundle;
 		this.storageLocation = Framework.STORAGE_LOCATION + bundle.bundleID
 				+ File.separatorChar + File.separatorChar;
 
 		try {
 			// write the JAR file to the storage
 			File file = new File(storageLocation, BUNDLE_FILE_NAME);
 			storeFile(file, stream);
 
 			// and open a JarFile
 			final JarFile jar = new JarFile(file);
 
 			// process the manifest
 			processManifest(jar.getManifest());
 
 			if (Framework.DECOMPRESS_EMBEDDED && classpathStrings.length > 1) {
 				final String contentDir = storageLocation + "content";
 
 				// we have embedded jars, decompress the bundle
 				for (Enumeration entries = jar.entries(); entries
 						.hasMoreElements();) {
 					ZipEntry entry = (ZipEntry) entries.nextElement();
 					if (entry.isDirectory()) {
 						continue;
 					}
 					final File content = new File(contentDir, entry.getName());
 					content.getParentFile().mkdirs();
 					storeFile(content, new BufferedInputStream(jar
 							.getInputStream(entry)));
 				}
 				// delete the bundle jar
 				jar.close();
 				new File(jar.getName()).delete();
 				jarFile = null;
 			} else {
 				jarFile = jar;
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			Framework.deleteDirectory(new File(storageLocation));
 			throw new BundleException("Not a valid bundle: " + bundle.location);
 		}
 	}
 
 	/**
 	 * recreate the bundle class loader from storage.
 	 * 
 	 * @param bundle
 	 *            a BundleImpl for the new bundle.
 	 * @param storageLocation
 	 *            the storage location.
 	 * @throws BundleException
 	 *             if something goes wrong.
 	 */
 	BundleClassLoader(final BundleImpl bundle, final String storageLocation)
 			throws BundleException {
 		this.storageLocation = storageLocation;
 		this.bundle = bundle;
 
 		File file = new File(storageLocation, BUNDLE_FILE_NAME);
 
 		try {
 			if (file.exists()) {
 				jarFile = new JarFile(file);
 				processManifest(jarFile.getManifest());
 			} else {
 				file = new File(storageLocation, "content");
 				jarFile = null;
 				final Manifest mf = new Manifest();
 				mf.read(new FileInputStream(new File(file, "META-INF"
 						+ File.separatorChar + "MANIFEST.MF")));
 				processManifest(mf);
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			throw new BundleException("Could not restore bundle: "
 					+ bundle.location);
 		}
 	}
 
 	/**
 	 * process the bundle manifest.
 	 * 
 	 * @param manifest
 	 *            the Manifest.
 	 * @throws BundleException
 	 *             in case of parse errors.
 	 */
 	private void processManifest(final Manifest manifest)
 			throws BundleException {
 		final Attributes attrs = manifest.getMainAttributes();
 
 		checkEE(readProperty(attrs,
 				Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT),
 				splitString(System
 						.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT)));
 
 		// get the exports
 		exports = readProperty(attrs, Constants.EXPORT_PACKAGE);
 
 		// get the imports
 		imports = readProperty(attrs, Constants.IMPORT_PACKAGE);
 
 		// get the dynamicImports
 		dynamicImports = readProperty(attrs, Constants.DYNAMICIMPORT_PACKAGE);
 
 		// get the classpath
 		final String[] classpathString = readProperty(attrs,
 				Constants.BUNDLE_CLASSPATH);
 		classpathStrings = classpathString.length > 0 ? classpathString
 				: new String[] { "." };
 
 		// get the native libraries
 		final String[] nativeStrings = readProperty(attrs,
 				Constants.BUNDLE_NATIVECODE);
 		if (nativeStrings.length > 0) {
 			nativeLibraries = new HashMap(nativeStrings.length);
 			processNativeLibraries(nativeStrings, nativeLibraries);
 		}
 
 		// get the activator
 		activatorClassName = attrs.getValue(Constants.BUNDLE_ACTIVATOR);
 
 		// get dynamic imports
 		dynamicImports = readProperty(attrs, Constants.DYNAMICIMPORT_PACKAGE);
 
 		// set the bundle headers
 		final Hashtable headers = new Hashtable(attrs.size());
 		final Object[] entries = attrs.keySet().toArray(
 				new Object[attrs.keySet().size()]);
 		for (int i = 0; i < entries.length; i++) {
 			headers
 					.put(entries[i].toString(), attrs.get(entries[i])
 							.toString());
 		}
 		bundle.headers = headers;
 	}
 
 	private void checkEE(final String[] req, final String[] having)
 			throws BundleException {
 		if (req.length == 0) {
 			return;
 		}
 		final Set havingEEs = new HashSet(Arrays.asList(having));
 		for (int i = 0; i < req.length; i++) {
 			if (havingEEs.contains(req[i])) {
 				return;
 			}
 		}
 		throw new BundleException("Platform does not provide EEs "
 				+ Arrays.asList(req));
 	}
 
 	/**
 	 * try to resolve the bundle.
 	 * 
 	 * @param critical
 	 *            if true, recursion depth for resolving dependencies is
 	 *            unlimited, otherwise, only unresolved dependencies are not
 	 *            recursively resolved. Typically, non critical resolving is
 	 *            called after installation, critical if the bundle is about to
 	 *            start.
 	 * @param pending
 	 *            a <code>HashSet</code> of pending bundles to suppress cycles
 	 *            in the dependency graph.
 	 * @return true, if the bundle could be resolved.
 	 * @throws BundleException
 	 *             in case of unresolvable dependencies.
 	 */
 	boolean resolveBundle(final boolean critical, HashSet pending)
 			throws BundleException {
 
 		if (Framework.DEBUG_CLASSLOADING) {
 			Framework.logger.log(LogService.LOG_INFO,
 					"BundleClassLoader: Resolving " + bundle
 							+ (critical ? " (critical)" : " (not critical)"));
 		}
 
 		/*
 		 * resolve the bundle's internal classpath. <specs page="52">The
 		 * framework must ignore missing files in the Bundle-Classpath headers.
 		 * However, a Framework should publish a Framework Event of type ERROR
 		 * for each file that is not found in the bundle's JAR with an
 		 * appropriate message</specs>
 		 */
 		if (classpath == null) {
 			for (int i = 0; i < classpathStrings.length; i++) {
 				if (classpathStrings[i].equals(".")) {
 					// '.' is always fine
 					continue;
 				}
 				if (jarFile == null) {
 					if (!new File(storageLocation + "content",
 							classpathStrings[i]).exists()) {
 						Framework.notifyFrameworkListeners(
 								FrameworkEvent.ERROR, bundle,
 								new BundleException(
 										"Missing file in bundle classpath "
 												+ classpathStrings[i]));
 					}
 				} else {
 					if (null == jarFile.getEntry(classpathStrings[i])) {
 						Framework.notifyFrameworkListeners(
 								FrameworkEvent.ERROR, bundle,
 								new BundleException(
 										"Missing file in bundle classpath "
 												+ classpathStrings[i]));
 					}
 				}
 			}
 			classpath = classpathStrings;
 			classpathStrings = null;
 		}
 
 		HashSet exportSet = null;
 		if (exports.length > 0) {
 			exportSet = new HashSet(exports.length);
 			for (int i = 0; i < exports.length; i++) {
 				exportSet.add(Package.parsePackageString(exports[i])[0]);
 			}
 		}
 
 		// get delegations for the imports
 		if (imports.length > 0) {
 			if (importDelegations == null) {
 				importDelegations = new HashMap(imports.length);
 			}
 			for (int i = 0; i < imports.length; i++) {
 				final String packageName = Package
 						.parsePackageString(imports[i])[0];
 				if (FRAMEWORK_PACKAGES.contains(packageName)
 						|| importDelegations.get(packageName) != null
 						|| (exportSet != null && exportSet
 								.contains(packageName))) {
 					continue;
 				}
 
 				// get the classloader for the input
 				final BundleClassLoader cl = Framework.getImport(bundle,
 						imports[i], critical, pending);
 				if (cl != null) {
 					if (cl != this) {
 						// and remember this
 						importDelegations.put(packageName, cl);
 					}
 					continue;
 				} else {
 					if (critical) {
 						throw new BundleException("Unsatisfied import "
 								+ imports[i] + " for bundle "
 								+ bundle.toString(),
 								new ClassNotFoundException(
 										"Unsatisfied import " + imports[i]));
 					} else {
 						// lazy resolving: if we require a bundle for input
 						// that could not been resolved when it was installed,
 						// try to resolve it now. So we notify the framework
 						// that we are providing this exports but we have not
 						// been resolved yet. Whenever the framework really
 						// needs our exports, it will try to resolve us again.
 						if (exports.length > 0) {
 							Framework.export(this, exports, false);
 						}
 						if (Framework.DEBUG_CLASSLOADING) {
 							Framework.logger
 									.log(
 											LogService.LOG_INFO,
 											"BundleClassLoader: Missing import "
 													+ imports[i]
 													+ ". Resolving attempt terminated unsuccessfully.");
 						}
 						return false;
 					}
 				}
 			}
 		}
 
 		// add implicit imports for all exported packages
 		if (exports.length > 0) {
 
 			if (importDelegations == null) {
 				importDelegations = new HashMap(imports.length);
 			}
 
 			for (int i = 0; i < exports.length; i++) {
 				final BundleClassLoader cl = Framework.getImport(bundle,
 						Package.parsePackageString(exports[i])[0], false, null);
 				if (cl != null && cl != this) {
 					// and remember this
 					importDelegations.put(Package
 							.parsePackageString(exports[i])[0], cl);
 				}
 			}
 		}
 
 		// now that everything is resolved, we may export
 		if (exports.length > 0) {
 			Framework.export(this, exports, true);
 		}
 
 		return true;
 	}
 
 	/**
 	 * perform a cleanup. All exported packages that are removed from the
 	 * framework's package registry. All imported packages are returned.
 	 * 
 	 * @param full
 	 *            if false, the bundle is only prepared for an update. If true,
 	 *            it is prepared for the uninstalled state.
 	 */
 	void cleanup(final boolean full) {
 		// remove all exported packages
 		final ArrayList stalePackages = new ArrayList();
 		for (int i = 0; i < exports.length; i++) {
 			final Package p = (Package) Framework.exportedPackages
 					.get(new Package(exports[i], null, false));
 			if (p != null) {
 				if (p.importingBundles == null) {
 					Framework.exportedPackages.remove(p);
 					p.importingBundles = null;				
 				} else {
 					p.removalPending = true;
 					stalePackages.add(p);
 				}
 			}
 		}
 		if (bundle != null) {
 			if (full) {
 				bundle.staleExportedPackages = (Package[]) stalePackages
 						.toArray(new Package[stalePackages.size()]);
 			} else {
 				bundle.staleExportedPackages = null;
 			}
 		}
 
 		if (importDelegations != null) {
 			String[] allImports = (String[]) importDelegations.keySet()
 					.toArray(new String[importDelegations.size()]);
 			for (int i = 0; i < allImports.length; i++) {
 				final Package p = (Package) Framework.exportedPackages
 						.get(new Package(allImports[i], null, false));
 				if (p != null && p.importingBundles != null) {
 					p.importingBundles.remove(bundle);
 					if (p.importingBundles.isEmpty()) {
 						p.importingBundles = null;
 						if (p.removalPending) {
 							Framework.exportedPackages.remove(p);
 						}
 					}
 				}
 			}
 		}
 
 		importDelegations = null;
 		activator = null;
 		originalExporter = null;
 
 		if (full) {
 			if (stalePackages.size() == 0) {
 				bundle = null;
 			}
 			activatorClassName = null;
 			imports = null;
 			dynamicImports = null;
 		}
 	}
 
 	/**
 	 * find a class. The following order has to be used for loading classes:
 	 * <ol>
 	 * <li>The system class loader (already done by the loadClass method prior
 	 * to this method call)</li>
 	 * <li>Possibly imported packages</li>
 	 * <li>The own bundle</li>
 	 * </ol>
 	 * 
 	 * @param classname
 	 *            the name of the class.
 	 * @return the <code>Class</code> object, if the class could be found.
 	 * @throws ClassNotFoundException
 	 *             if the class could not be found.
 	 * @see java.lang.ClassLoader#findClass(java.lang.String)
 	 * @category ClassLoader
 	 */
 	protected Class findClass(final String classname)
 			throws ClassNotFoundException {
 
 		Class clazz;
 
 		// check dynamic imports, if they are declared
 		if (dynamicImports.length > 0) {
 			for (int i = 0; i < dynamicImports.length; i++) {
 				if (dynamicImports[i].indexOf("*") > -1) {
 					final Package[] pkgs = (Package[]) Framework.exportedPackages
 							.keySet().toArray(
 									new Package[Framework.exportedPackages
 											.size()]);
 					for (int j = 0; j < pkgs.length; j++) {
 						if (pkgs[j].matches(dynamicImports[i])
 								&& (clazz = findDelegatedClass(
 										pkgs[j].classloader, classname)) != null) {
 							return clazz;
 						}
 					}
 				} else {
 					final Package p = (Package) Framework.exportedPackages
 							.get(new Package(packageOf(classname), null, false));
 					if (p != null) {
 						if ((clazz = findDelegatedClass(p.classloader,
 								classname)) != null) {
 							return clazz;
 						}
 					}
 				}
 			}
 		}
 
 		// if delegations exist, check if the class is imported
 		if (importDelegations != null) {
 			BundleClassLoader delegation = (BundleClassLoader) importDelegations
 					.get(packageOf(classname));
 			if (delegation != null) {
 				clazz = findDelegatedClass(delegation, classname);
 				if (clazz != null) {
 					return clazz;
 				}
 			}
 		}
 
 		// okay, check if it is in the scope of this classloader
 		clazz = findOwnClass(classname);
 		if (clazz != null) {
 			return clazz;
 		}
 
 		throw new ClassNotFoundException(classname);
 	}
 
 	/**
 	 * Find a class in the scope of this classloader.
 	 * 
 	 * @param classname
 	 *            the name of the class.
 	 * @return the <code>Class</code> object if the class could be found.
 	 *         <code>null</code> otherwise.
 	 */
 	private Class findOwnClass(final String classname) {
 		try {
 			final String filename = classToFile(classname);
 			for (int i = 0; i < classpath.length; i++) {
 				final InputStream input = retrieveFile(jarFile, classpath[i],
 						storageLocation, filename);
 				if (input == null) {
 					continue;
 				}
 				try {
 					int len;
 					final ByteArrayOutputStream out = new ByteArrayOutputStream();
 					final BufferedInputStream bis = new BufferedInputStream(
 							input);
 					byte[] chunk = new byte[Framework.CLASSLOADER_BUFFER_SIZE];
 					while ((len = bis.read(chunk, 0,
 							Framework.CLASSLOADER_BUFFER_SIZE)) > 0) {
 						out.write(chunk, 0, len);
 					}
 					return defineClass(classname, out.toByteArray(), 0, out
 							.size(), bundle.domain);
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 					return null;
 				} catch (LinkageError le) {
 					System.err.println("ERROR in " + toString() + ":");
 					throw le;
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Find a class that is delegated from a different classloader by package
 	 * import.
 	 * 
 	 * @param classname
 	 *            the name of the class.
 	 * @return the <code>Class</code> object, if the class could be found.
 	 *         <code>null</code> otherwise.
 	 */
 
 	private static Class findDelegatedClass(final BundleClassLoader delegation,
 			final String classname) {
 		final Class clazz;
		synchronized(delegation) {
			return ((clazz = delegation.findLoadedClass(classname)) == null) ? delegation
 				.findOwnClass(classname)
 				: clazz;
		}
 	}
 
 	/**
 	 * find a single resource.
 	 * 
 	 * @param filename
 	 *            the name of the resource.
 	 * @return the URL to the resource.
 	 * @see java.lang.ClassLoader#findResource(java.lang.String)
 	 * @category ClassLoader
 	 */
 	protected URL findResource(final String filename) {
 		final String name = stripTrailing(filename);
 		Vector results = findOwnResources(name, false);
 		if (results.size() > 0) {
 			return (URL) results.elementAt(0);
 		}
 		results = findImportedResources(name, false);
 		return results.size() > 0 ? (URL) results.elementAt(0) : null;
 	}
 
 	/**
 	 * find multiple resources.
 	 * 
 	 * @param filename
 	 *            the name of the resource.
 	 * @return an <code>Enumeration</code> over <code>URL</code> objects.
 	 * @see java.lang.ClassLoader#findResources(java.lang.String)
 	 * @category ClassLoader
 	 */
 	protected Enumeration findResources(final String filename) {
 		final String name = stripTrailing(filename);
 		final Vector results = findOwnResources(name, true);
 		results.addAll(findImportedResources(name, true));
 		return results.elements();
 	}
 
 	/**
 	 * find one or more resources in the scope of the own classloader.
 	 * 
 	 * @param name
 	 *            the name of the resource
 	 * @param multiple
 	 *            if false, the search terminates if the first result has been
 	 *            found.
 	 * @return a <code>Vector</code> of <code>URL</code> elements.
 	 */
 	private Vector findOwnResources(final String name, final boolean multiple) {
 		final Vector results = new Vector(0);
 		try {
 			for (int i = 0; i < classpath.length; i++) {
 				final InputStream inputStream = retrieveFile(jarFile,
 						classpath[i], storageLocation, name);
 				if (inputStream != null) {
 					// results.add(new URL("bundle", name, 0, "",
 					// new BundleURLHandler(inputStream)));
 					results.add(new URL(null, "bundle://" + name,
 							new BundleURLHandler(inputStream)));
 					if (!multiple) {
 						return results;
 					}
 				}
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		return results;
 	}
 
 	/**
 	 * find one or more resources imported from other bundles.
 	 * 
 	 * @param name
 	 *            the name of the resource.
 	 * @param multiple
 	 *            if false, the searching terminates if the first resource has
 	 *            been found.
 	 * @return a <code>Vector</code> of <code>URL</code> elements.
 	 */
 	private Vector findImportedResources(final String name,
 			final boolean multiple) {
 		if (bundle.state == Bundle.INSTALLED || importDelegations == null) {
 			return EMPTY_VECTOR;
 		}
 		final BundleClassLoader delegation = (BundleClassLoader) importDelegations
 				.get(packageOf(pseudoClassname(name)));
 
 		if (delegation == null) {
 			return EMPTY_VECTOR;
 		} else {
 			return delegation.originalExporter == null ? delegation
 					.findOwnResources(name, multiple)
 					: delegation.originalExporter.findOwnResources(name,
 							multiple);
 		}
 	}
 
 	/**
 	 * find a native code library.
 	 * 
 	 * @param libname
 	 *            the name of the library.
 	 * @return the String of a path name to the library or <code>null</code>.
 	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
 	 * @category ClassLoader
 	 */
 	protected String findLibrary(final String libname) {
 		if (nativeLibraries == null) {
 			return null;
 		}
 
 		final String lib = (String) nativeLibraries.get(System
 				.mapLibraryName(libname));
 
 		if (Framework.DEBUG_CLASSLOADING) {
 			Framework.logger.log(LogService.LOG_DEBUG, "Requested " + libname);
 			Framework.logger.log(LogService.LOG_INFO, "Native libraries "
 					+ nativeLibraries);
 		}
 
 		if (lib == null) {
 			return null;
 		}
 		try {
 			File libfile = new File(storageLocation + "lib", lib);
 			if (!libfile.exists()) {
 				URL url = (URL) findOwnResources(lib, false).elementAt(0);
 				storeFile(libfile, url.openStream());
 			}
 			return libfile.getAbsolutePath();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * get a string representation of the object.
 	 * 
 	 * @return a string.
 	 * @see java.lang.Object#toString()
 	 * @category Object
 	 */
 	public String toString() {
 		return "BundleClassLoader[Bundle" + bundle + "]";
 	}
 
 	/*
 	 * static methods
 	 */
 
 	/**
 	 * read a property from the manifest attributes.
 	 * 
 	 * @param attrs
 	 *            the attributes.
 	 * @param property
 	 *            the name of the property to read.
 	 * @return the values.
 	 * @throws BundleException
 	 *             if the manifest has an empty value for the key.
 	 */
 	private static String[] readProperty(final Attributes attrs,
 			final String property) throws BundleException {
 		final String values = attrs.getValue(property);
 		if (values != null && values.equals("")) {
 			throw new BundleException("Broken manifest, " + property
 					+ " is empty.");
 		}
 
 		return splitString(values);
 	}
 
 	private static String[] splitString(final String values) {
 		if (values == null) {
 			return new String[0];
 		}
 		final StringTokenizer tokenizer = new StringTokenizer(values, ",");
 		if (tokenizer.countTokens() == 0) {
 			return new String[] { values };
 		}
 		final String[] result = new String[tokenizer.countTokens()];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = tokenizer.nextToken().trim();
 		}
 		return result;
 	}
 
 	/**
 	 * process the native libraries declarations from the manifest. Only
 	 * register natives that comply with stated OS/version/languages
 	 * constraints.
 	 * 
 	 * @param nativeStrings
 	 *            the native library declarations and constraints.
 	 * @param nativeLibraries
 	 *            the map.
 	 */
 	private static void processNativeLibraries(final String[] nativeStrings,
 			final Map nativeLibraries) {
 		int pos = -1;
 
 		final String osname = (String) Framework.properties
 				.get(Constants.FRAMEWORK_OS_NAME);
 		final String osversion = ";"
 				+ (String) Framework.properties
 						.get(Constants.FRAMEWORK_OS_VERSION);
 		final Locale language = new Locale((String) Framework.properties
 				.get(Constants.FRAMEWORK_LANGUAGE), "");
 		final String cpu = ((String) Framework.properties
 				.get(Constants.FRAMEWORK_PROCESSOR)).intern();
 		final String processor = ((cpu == "pentium" || cpu == "i386"
 				|| cpu == "i486" || cpu == "i586" || cpu == "i686") ? "x86"
 				: cpu).intern();
 
 		boolean n = false;
 		boolean no_n = true;
 		boolean l = false;
 		boolean no_l = true;
 		boolean v = false;
 		boolean no_v = true;
 		boolean p = false;
 		boolean no_p = false;
 		final List libs = new ArrayList();
 
 		for (int i = 0; i < nativeStrings.length; i++) {
 			if (nativeStrings[i].indexOf(";") == -1) {
 				nativeLibraries
 						.put(
 								(pos = nativeStrings[i].lastIndexOf("/")) > -1 ? nativeStrings[i]
 										.substring(pos + 1)
 										: nativeStrings[i],
 								stripTrailing(nativeStrings[i]));
 			} else {
 				StringTokenizer tokenizer = new StringTokenizer(
 						nativeStrings[i], ";");
 
 				while (tokenizer.hasMoreTokens()) {
 					final String token = tokenizer.nextToken();
 					final int a = token.indexOf("=");
 					if (a > -1) {
 						final String criterium = token.substring(0, a).trim()
 								.intern();
 						final String value = token.substring(a + 1).trim()
 								.intern();
 						if (criterium == Constants.BUNDLE_NATIVECODE_OSNAME) {
 							n |= value.equalsIgnoreCase(osname);
 							no_n = false;
 						} else if (criterium == Constants.BUNDLE_NATIVECODE_OSVERSION) {
 							v |= Package.matches(";" + value, osversion);
 							no_v = false;
 						} else if (criterium == Constants.BUNDLE_NATIVECODE_LANGUAGE) {
 							l |= new Locale(value, "").getLanguage().equals(
 									language);
 							no_l = false;
 						} else if (criterium == Constants.BUNDLE_NATIVECODE_PROCESSOR) {
 							if (processor == "x86") {
 								p |= (value == "x86" || value == "pentium"
 										|| value == "i386" || value == "i486"
 										|| value == "i586" || value == "i686");
 							} else {
 								p |= value.equalsIgnoreCase(processor);
 							}
 							no_p = false;
 						}
 					} else {
 						libs.add(token.trim());
 					}
 				}
 				if (!libs.isEmpty() && (no_p || p) && (no_n || n)
 						&& (no_v || v) && (no_l || l)) {
 					final String[] libraries = (String[]) libs
 							.toArray(new String[libs.size()]);
 					for (int c = 0; c < libraries.length; c++) {
 						nativeLibraries.put((pos = libraries[c]
 								.lastIndexOf("/")) > -1 ? libraries[c]
 								.substring(pos + 1) : libraries[c],
 								stripTrailing(libraries[c]));
 					}
 				}
 				p = n = v = l = false;
 				no_p = no_n = no_v = no_l = true;
 				libs.clear();
 			}
 		}
 	}
 
 	/**
 	 * store a file on the storage.
 	 * 
 	 * @param file
 	 *            the file.
 	 * @param input
 	 *            the input stream.
 	 */
 	private static void storeFile(final File file, final InputStream input) {
 		try {
 			file.getParentFile().mkdirs();
 			final FileOutputStream fos = new FileOutputStream(file);
 
 			byte[] buffer = new byte[Framework.CLASSLOADER_BUFFER_SIZE];
 			int len;
 			while ((len = input.read(buffer, 0,
 					Framework.CLASSLOADER_BUFFER_SIZE)) > -1) {
 				fos.write(buffer, 0, len);
 			}
 			fos.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/**
 	 * retrieve a file from the storage.
 	 * 
 	 * @param jarFile
 	 *            the jarFile.
 	 * @param classpath
 	 *            the classpath.
 	 * @param storageLocation
 	 *            the storage location.
 	 * @param filename
 	 *            the name of the file to retrieve.
 	 * @return the InputStream of the file or null.
 	 * @throws IOException
 	 *             if something goes wrong.
 	 */
 	private static InputStream retrieveFile(final JarFile jarFile,
 			final String classpath, final String storageLocation,
 			final String filename) throws IOException {
 		if (jarFile != null) {
 			if (classpath.equals(".")) {
 				final ZipEntry entry = jarFile.getEntry(filename);
 				if (entry == null) {
 					return null;
 				}
 				return jarFile.getInputStream(entry);
 			} else {
 				final ZipEntry entry = jarFile.getEntry(classpath);
 				if (entry == null) {
 					throw new IOException(classpath + " not found");
 				}
 				final JarInputStream embeddedJar = new JarInputStream(jarFile
 						.getInputStream(entry));
 				ZipEntry embeddedEntry;
 				while ((embeddedEntry = embeddedJar.getNextEntry()) != null) {
 					if (embeddedEntry.getName().equals(filename)) {
 						return embeddedJar;
 					}
 				}
 			}
 		} else {
 			if (classpath.equals(".")) {
 				final File file = new File(storageLocation + File.separatorChar
 						+ "content", filename);
 				if (file.exists()) {
 					return new FileInputStream(file);
 				}
 				return null;
 			} else {
 				final File file = new File(storageLocation + File.separatorChar
 						+ "content", classpath);
 				if (file.exists()) {
 					final JarFile jar = new JarFile(file);
 					final ZipEntry entry = jar.getEntry(filename);
 					if (entry == null) {
 						return null;
 					}
 					return jar.getInputStream(entry);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * get a file from a class name.
 	 * 
 	 * @param fqc
 	 *            the fully qualified class name.
 	 * @return the file name.
 	 */
 	private static String classToFile(final String fqc) {
 		return fqc.replace('.', '/') + ".class";
 	}
 
 	/**
 	 * strip trailing spaces.
 	 * 
 	 * @param filename
 	 *            the file name.
 	 * @return the stripped file name.
 	 */
 	private static String stripTrailing(final String filename) {
 		return (filename.startsWith("/") || filename.startsWith("\\")) ? filename
 				.substring(1)
 				: filename;
 	}
 
 	/**
 	 * get the package of a class.
 	 * 
 	 * @param classname
 	 * @return the package.
 	 */
 	private static String packageOf(final String classname) {
 		final int pos = classname.lastIndexOf('.');
 		return pos > -1 ? classname.substring(0, pos) : "";
 	}
 
 	/**
 	 * create a pseudo classname from a file.
 	 * 
 	 * @param filename
 	 *            the filename.
 	 * @return the pseudo classname.
 	 */
 	private static String pseudoClassname(final String filename) {
 		return stripTrailing(filename).replace('.', '-').replace('/', '.')
 				.replace('\\', '.');
 	}
 
 	/**
 	 * the "degenerated" URL handler that already contains the InputStream of
 	 * the URL and returns this InputStream on demand.
 	 * 
 	 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 	 */
 	private static final class BundleURLHandler extends URLStreamHandler {
 		/**
 		 * the embedded input stream.
 		 */
 		private final InputStream input;
 
 		/**
 		 * create a new BundleURLHandler from an input stream.
 		 * 
 		 * @param stream
 		 *            the input stream.
 		 */
 		private BundleURLHandler(final InputStream stream) {
 			input = new InputStream() {
 
 				public int read() throws IOException {
 					return stream.read();
 				}
 
 				public int read(final byte b[]) throws IOException {
 					return stream.read(b);
 				}
 			};
 		}
 
 		/**
 		 * get the connection from the url.
 		 * 
 		 * @param url
 		 *            the URL.
 		 * @return the URLConnection.
 		 * @throws IOException
 		 *             not thrown.
 		 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
 		 * @category URLStreamHandler
 		 */
 		protected URLConnection openConnection(final URL url)
 				throws IOException {
 			return new URLConnection(url) {
 
 				/**
 				 * this method is called when the URL is opened.
 				 * 
 				 * @see java.net.URLConnection#getInputStream()
 				 * @category URLConnection
 				 */
 				public InputStream getInputStream() throws IOException {
 					return input;
 				}
 
 				/**
 				 * this method has no effect, the InputStream is always
 				 * connected.
 				 * 
 				 * @see java.net.URLConnection#connect()
 				 * @category URLConnection
 				 */
 				public void connect() throws IOException {
 
 				}
 			};
 		}
 
 		/**
 		 * overridden if the URL is stored in a hash map. Some icon loading
 		 * strategies use this.
 		 * 
 		 * @param u
 		 *            the URL.
 		 * @return the hash code.
 		 * @see java.net.URLStreamHandler#hashCode(java.net.URL)
 		 * @category URLStreamHandler
 		 */
 		protected int hashCode(final URL u) {
 			return input.hashCode();
 		}
 	}
 }
