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
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilePermission;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.AccessControlContext;
 import java.security.AccessController;
 import java.security.CodeSource;
 import java.security.Permission;
 import java.security.PermissionCollection;
 import java.security.Permissions;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.ServicePermission;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 
 import ch.ethz.iks.concierge.framework.Framework.BundleContextImpl;
 
 /**
  * The Bundle implementation.
  * 
  * @author Jan S. Rellermeyer, IKS, ETH Zurich
  */
 final class BundleImpl implements Bundle {
 
 	/**
 	 * the bundle id.
 	 */
 	final long bundleID;
 
 	/**
 	 * the bundle location.
 	 */
 	final String location;
 
 	/**
 	 * the bundle state.
 	 */
 	int state;
 
 	/**
 	 * the headers from the manifest.
 	 */
 	Hashtable headers;
 
 	/**
 	 * the bundle classloader.
 	 */
 	BundleClassLoader classloader;
 
 	/**
 	 * the bundle context.
 	 */
 	private final BundleContextImpl context;
 
 	/**
 	 * the current start level.
 	 */
 	int currentStartlevel;
 
 	/**
 	 * is bundle marked to be started persistently.
 	 */
 	boolean persistently = false;
 
 	/**
 	 * the protection domain of this bundle.
 	 */
 	ProtectionDomain domain = null;
 
 	/**
 	 * List of services registered by this bundle. Is initialized in a lazy way.
 	 */
 	List registeredServices = null;
 
 	/**
 	 * List of framework listeners registered by this bundle. Is initialized in
 	 * a lazy way.
 	 */
 	List registeredFrameworkListeners = null;
 
 	/**
 	 * List of bundle listeners registered by this bundle. Is initialized in a
 	 * lazy way.
 	 */
 	List registeredBundleListeners = null;
 
 	/**
 	 * List of service listeners registered by this bundle. Is initialized in a
 	 * lazy way.
 	 */
 	List registeredServiceListeners = null;
 
 	/**
 	 * create a new bundle object from InputStream. This is used when a new
 	 * bundle is installed.
 	 * 
 	 * @param location
 	 *            the bundle location.
 	 * @param bundleID
 	 *            the bundle id.
 	 * @param stream
 	 *            the input stream.
 	 * @throws BundleException
 	 *             if something goes wrong.
 	 */
 	BundleImpl(final String location, final long bundleID,
 			final BundleContextImpl context, final InputStream stream)
 			throws BundleException {
 		this.bundleID = bundleID;
 		this.location = location;
 		context.bundle = this;
 		this.context = context;
 		this.currentStartlevel = Framework.initStartlevel;
 
 		if (Framework.SECURITY_ENABLED) {
 			try {
 				PermissionCollection permissions = new Permissions();
 				permissions.add(new FilePermission(Framework.STORAGE_LOCATION
 						+ bundleID, "read,write,execute,delete"));
 				domain = new ProtectionDomain(new CodeSource(new URL("file:"
 						+ Framework.STORAGE_LOCATION + bundleID),
 						(java.security.cert.Certificate[]) null), permissions);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			// create the bundle classloader
 			this.classloader = new BundleClassLoader(this, stream);
 		} catch (BundleException be) {
 			throw be;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BundleException("Could not install bundle " + location, e);
 		}
 		this.state = INSTALLED;
 
 		// notify the listeners
 		Framework.notifyBundleListeners(BundleEvent.INSTALLED, this);
 
 		// if we are not during startup or shutdown, update the metadata
 		if (!Framework.frameworkStartupShutdown) {
 			updateMetadata();
 		}
 
 		// we are just installing the bundle, if it is
 		// possible, resolve it, if not, wait until the
 		// exports are really needed (i.e., they become critical)
 		if (classloader.resolveBundle(false, null)) {
 			this.state = RESOLVED;
 		}
 
 	}
 
 	/**
 	 * Create a new bundle object from a storage location. Used after framework
 	 * restarts.
 	 * 
 	 * @param file
 	 *            the bundle's metadata file on the storage.
 	 * @param bcontext
 	 *            the bundle context.
 	 * @throws Exception
 	 *             if something goes wrong.
 	 */
 	BundleImpl(final File file, final BundleContextImpl bcontext)
 			throws Exception {
 		DataInputStream in = new DataInputStream(new FileInputStream(file));
 		this.bundleID = in.readLong();
 		this.location = in.readUTF();
 		this.currentStartlevel = in.readInt();
 		this.state = Bundle.INSTALLED;
 		this.persistently = in.readBoolean();
 		in.close();
 		bcontext.bundle = this;
 		this.context = bcontext;
 
 		if (Framework.SECURITY_ENABLED) {
 			// TODO: implement
 			domain = new ProtectionDomain(null, null);
 		}
 
 		// create the bundle classloader
 		classloader = new BundleClassLoader(this, Framework.STORAGE_LOCATION
 				+ File.separatorChar + bundleID + File.separatorChar);
 
 		// we are just installing the bundle, if it is
 		// possible, resolve it, if not, wait until the
 		// exports are really needed
 		if (classloader.resolveBundle(false, null)) {
 			this.state = RESOLVED;
 		} else {
 			this.state = INSTALLED;
 		}
 	}
 
 	/**
 	 * get the bundle id.
 	 * 
 	 * @return the bundle id.
 	 * @see org.osgi.framework.Bundle#getBundleId()
 	 * @category Bundle
 	 */
 	public long getBundleId() {
 		return bundleID;
 	}
 
 	/**
 	 * get the manifest headers.
 	 * 
 	 * @return the Dictionary of the headers.
 	 * @see org.osgi.framework.Bundle#getHeaders()
 	 * @category Bundle
 	 */
 	public Dictionary getHeaders() {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
 		return headers;
 	}
 
 	/**
 	 * get the bundle location.
 	 * 
 	 * @return the bundle location.
 	 * @see org.osgi.framework.Bundle#getLocation()
 	 * @category Bundle
 	 */
 	public String getLocation() {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
 		return location;
 	}
 
 	/**
 	 * get the registered services of the bundle.
 	 * 
 	 * @return the service reference array.
 	 * @see org.osgi.framework.Bundle#getRegisteredServices()
 	 * @category Bundle
 	 */
 	public ServiceReference[] getRegisteredServices() {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Bundle " + toString()
 					+ "has been unregistered.");
 		}
 		if (registeredServices == null) {
 			return null;
 		}
 
 		/*
 		 * <specs page="91">If the Java runtime supports permissions, a
 		 * ServiceReference object to a service is included in the returned list
 		 * only if the caller has the ServicePermission to get the service using
 		 * at least one of the names classes the service was registered under.</specs>
 		 */
 		if (Framework.SECURITY_ENABLED) {
 			return checkPermissions((ServiceReferenceImpl[]) registeredServices
 					.toArray(new ServiceReferenceImpl[registeredServices.size()]));
 		} else {
 			return (ServiceReference[]) registeredServices
 					.toArray(new ServiceReference[registeredServices.size()]);
 		}
 	}
 
 	/**
 	 * get a resource.
 	 * 
 	 * @param name
 	 *            the name of the resource.
 	 * @return the URL.
 	 * @see org.osgi.framework.Bundle#getResource(java.lang.String)
 	 * @category Bundle
 	 */
 	public URL getResource(final String name) {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Bundle " + toString()
 					+ " has been uninstalled");
 		}
 		return classloader.getResource(name);
 	}
 
 	/**
 	 * get the services of the bundle that are in use.
 	 * 
 	 * @return the service reference array.
 	 * @see org.osgi.framework.Bundle#getServicesInUse()
 	 * @category Bundle
 	 */
 	public ServiceReference[] getServicesInUse() {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Bundle " + toString()
 					+ "has been unregistered.");
 		}
 
 		final ArrayList result = new ArrayList();
 		final ServiceReferenceImpl[] srefs = (ServiceReferenceImpl[]) Framework.services
 				.toArray(new ServiceReferenceImpl[Framework.services.size()]);
 		for (int i = 0; i < srefs.length; i++) {
 			if (srefs[i].useCounters.get(this) != null) {
 				result.add(srefs[i]);
 			}
 		}
 
 		if (Framework.SECURITY_ENABLED) {
 			// permissions for the interfaces have to be checked
 			return checkPermissions((ServiceReferenceImpl[]) result
 					.toArray(new ServiceReferenceImpl[result.size()]));
 		} else {
 			return (ServiceReference[]) result
 					.toArray(new ServiceReference[result.size()]);
 		}
 	}
 
 	/**
 	 * get the state of the bundle.
 	 * 
 	 * @return the state.
 	 * @see org.osgi.framework.Bundle#getState()
 	 * @category Bundle
 	 */
 	public int getState() {
 		return state;
 	}
 
 	/**
 	 * check if the bundle has a certain permission.
 	 * 
 	 * @param permission
 	 *            the permission object
 	 * @return true if the bundle has the permission.
 	 * @see org.osgi.framework.Bundle#hasPermission(java.lang.Object)
 	 * @category Bundle
 	 */
 	public boolean hasPermission(final Object permission) {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Bundle " + toString()
 					+ "has been unregistered.");
 		}
 
 		if (Framework.SECURITY_ENABLED) {
 			return permission instanceof Permission ? domain.getPermissions()
 					.implies((Permission) permission) : false;
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * start the bundle.
 	 * 
 	 * @throws BundleException
 	 *             if the bundle cannot be resolved or the Activator throws an
 	 *             exception.
 	 * @see org.osgi.framework.Bundle#start()
 	 * @category Bundle
 	 */
 	public synchronized void start() throws BundleException {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
		if (currentStartlevel <= Framework.startlevel) {
			startBundle();
		}
 		persistently = true;
 		updateMetadata();
 	}
 
 	/**
 	 * the actual starting happens here. This method does not modify the
 	 * persistent metadata.
 	 * 
 	 * @throws BundleException
 	 *             if the bundle cannot be resolved or the Activator throws an
 	 *             exception.
 	 */
 	synchronized void startBundle() throws BundleException {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Cannot start uninstalled bundle "
 					+ toString());
 		}
 		if (state == ACTIVE) {
 			return;
 		}
 		if (state == INSTALLED) {
 			// this time, it is critical to get the bundle resolved
 			// so if we need exports from other unresolved bundles,
 			// we will try to resolve them (recursively) to get the bundle
 			// started
 			classloader.resolveBundle(true, new HashSet(0));
 			state = RESOLVED;
 		}
 
 		state = STARTING;
 		try {
 			context.isValid = true;
 			if (classloader.activatorClassName != null) {
 				final Class activatorClass = classloader
 						.loadClass(classloader.activatorClassName);
 				if (activatorClass == null) {
 					throw new ClassNotFoundException(
 							classloader.activatorClassName);
 				}
 				classloader.activator = (BundleActivator) activatorClass
 						.newInstance();
 				classloader.activator.start(context);
 				state = ACTIVE;
 				Framework.notifyBundleListeners(BundleEvent.STARTED, this);
 				if (Framework.DEBUG_BUNDLES) {
 					Framework.logger.log(LogService.LOG_INFO,
 							"Framework: Bundle " + toString() + " started.");
 				}
 			} else {
 				state = RESOLVED;
 			}
 		} catch (Throwable t) {
 			Framework.clearBundleTrace(this);
 			state = RESOLVED;
 			Framework.notifyBundleListeners(BundleEvent.INSTALLED, this);
 			throw new BundleException("Error starting bundle " + toString(), t);
 		}
 
 	}
 
 	/**
 	 * stop the bundle.
 	 * 
 	 * @throws BundleException
 	 *             if the bundle has been uninstalled before.
 	 * 
 	 * @see org.osgi.framework.Bundle#stop()
 	 * @category Bundle
 	 */
 	public synchronized void stop() throws BundleException {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
 		stopBundle();
 		persistently = false;
 		updateMetadata();
 	}
 
 	/**
 	 * the actual starting happens here. This method does not modify the
 	 * persistent metadata.
 	 * 
 	 * @throws BundleException
 	 *             if the bundle has been uninstalled before.
 	 */
 	synchronized void stopBundle() throws BundleException {
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Cannot stop uninstalled bundle "
 					+ toString());
 		}
 		if (state != ACTIVE) {
 			return;
 		}
 
 		state = STOPPING;
 		try {
 			if (classloader.activator != null) {
 				classloader.activator.stop(context);
 			}
 			if (Framework.DEBUG_BUNDLES) {
 				Framework.logger.log(LogService.LOG_INFO, "Framework: Bundle "
 						+ toString() + " stopped.");
 			}
 		} catch (Throwable t) {
 			throw new BundleException("Error stopping bundle " + toString(), t);
 		} finally {
 			classloader.activator = null;
 			Framework.clearBundleTrace(this);
 			state = RESOLVED;
 			Framework.notifyBundleListeners(BundleEvent.STOPPED, this);
 			context.isValid = false;
 		}
 
 	}
 
 	/**
 	 * uninstall the bundle.
 	 * 
 	 * @throws BundleException
 	 *             if bundle is already uninstalled
 	 * @see org.osgi.framework.Bundle#uninstall()
 	 * @category Bundle
 	 */
 	public synchronized void uninstall() throws BundleException {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
 
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Bundle " + toString()
 					+ " is already uninstalled.");
 		}
 		if (state == ACTIVE) {
 			try {
 				stopBundle();
 			} catch (Throwable t) {
 				Framework.notifyFrameworkListeners(FrameworkEvent.ERROR, this,
 						t);
 			}
 		}
 
 		state = UNINSTALLED;
 
 		new File(classloader.storageLocation, "meta").delete();
 
 		if (classloader.originalExporter != null) {
 			classloader.originalExporter.cleanup(true);
 			classloader.originalExporter = null;
 		}
 		classloader.cleanup(true);
 		classloader = null;
 
 		Framework.bundleID_bundles.remove(new Long(bundleID));
 		Framework.location_bundles.remove(location);
 		Framework.bundles.remove(this);
 		Framework.notifyBundleListeners(BundleEvent.UNINSTALLED, this);
 
 		context.isValid = false;
 		context.bundle = null;
 	}
 
 	/**
 	 * update the bundle from its update location or the location from where it
 	 * was originally installed.
 	 * 
 	 * @throws BundleException
 	 *             if something goes wrong.
 	 * @see org.osgi.framework.Bundle#update()
 	 * @category Bundle
 	 */
 	public synchronized void update() throws BundleException {
 		final String updateLocation = (String) headers
 				.get(Constants.BUNDLE_UPDATELOCATION);
 		try {
 			update(new URL(updateLocation == null ? location : updateLocation)
 					.openConnection().getInputStream());
 		} catch (IOException ioe) {
 			throw new BundleException("Could not update " + toString()
 					+ " from " + updateLocation, ioe);
 		}
 	}
 
 	/**
 	 * update the bundle from an input stream.
 	 * 
 	 * @param stream
 	 *            the stream.
 	 * @throws BundleException
 	 *             if something goes wrong.
 	 * @see org.osgi.framework.Bundle#update(java.io.InputStream)
 	 * @category Bundle
 	 */
 	public synchronized void update(final InputStream stream)
 			throws BundleException {
 		if (Framework.SECURITY_ENABLED) {
 			Framework.checkAdminPermission();
 		}
 
 		if (state == UNINSTALLED) {
 			throw new IllegalStateException("Cannot update uninstalled bundle "
 					+ toString());
 		}
 		boolean wasActive = false;
 		if (state == ACTIVE) {
 			// so we have to restart it after update
 			wasActive = true;
 			stop();
 		}
 
 		try {
 			BundleClassLoader updated = new BundleClassLoader(this, stream);
 
 			// did the original bundle export packages ?
 			final String[] exports = classloader.exports;
 
 			if (exports.length > 0) {
 				// are some of them in use ?
 				boolean inUse = false;
 				for (int i = 0; i < exports.length; i++) {
 					final Package p = (Package) Framework.exportedPackages
 							.get(new Package(exports[i], null, false));
 					if (p.importingBundles != null) {
 						if (p.classloader == classloader) {
 							// set removal pending for this package, since the
 							// exporting bundle is going to be updates
 							p.removalPending = true;
 							inUse = true;
 						}
 					}
 				}
 
 				if (inUse) {
 					// did the last version already have an older version
 					// exporting the packages ?
 					if (classloader.originalExporter != null) {
 						updated.originalExporter = classloader.originalExporter;
 					} else {
 						// so the last version exported
 						updated.originalExporter = classloader;
 					}
 				}
 			}
 
 			classloader.cleanup(true);
 			// exchange the classloaders
 			classloader = updated;
 
 			if (classloader.resolveBundle(false, null)) {
 				state = RESOLVED;
 			} else {
 				state = INSTALLED;
 			}
 			Framework.notifyBundleListeners(BundleEvent.UPDATED, this);
 			if (wasActive) {
 				// restart it
 				start();
 			}
 			if (!Framework.frameworkStartupShutdown) {
 				updateMetadata();
 			}
 		} catch (BundleException be) {
 			throw be;
 		} catch (Exception e) {
 			throw new BundleException("Could not update bundle " + toString(),
 					e);
 		}
 	}
 
 	/**
 	 * update the bundle's metadata on the storage.
 	 */
 	void updateMetadata() {
 		try {
 			DataOutputStream out = new DataOutputStream(new FileOutputStream(
 					new File(classloader.storageLocation, "meta")));
 			out.writeLong(bundleID);
 			out.writeUTF(location);
 			out.writeInt(currentStartlevel);
 			out.writeBoolean(persistently);
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * get a string representation of the bundle.
 	 * 
 	 * @return the string.
 	 * @category Object
 	 */
 	public String toString() {
 		return "Bundle [" + bundleID + "]: " + location;
 	}
 
 	/**
 	 * remove all ServiceReferences for which the requesting bundle does not
 	 * have appropriate permissions
 	 * 
 	 * @param refs
 	 *            the references.
 	 * @return the permitted references.
 	 */
 	private ServiceReference[] checkPermissions(ServiceReferenceImpl[] refs) {
 		List results = new ArrayList(refs.length);
 		final AccessControlContext controller = AccessController.getContext();
 		for (int i = 0; i < refs.length; i++) {
 			String[] interfaces = (String[]) refs[i].properties
 					.get(Constants.OBJECTCLASS);
 			for (int j = 0; j < interfaces.length; j++) {
 				try {
 					controller.checkPermission(new ServicePermission(
 							interfaces[j], ServicePermission.GET));
 					results.add(refs);
 					break;
 				} catch (SecurityException se) {
 					// does not have the permission, try with the next interface
 				}
 			}
 		}
 		return (ServiceReference[]) results
 				.toArray(new ServiceReference[results.size()]);
 	}
 
 }
