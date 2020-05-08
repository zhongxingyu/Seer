 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.extension;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.update.configuration.IConfiguredSite;
 import org.eclipse.update.configuration.ILocalSite;
 import org.eclipse.update.core.IFeature;
 import org.eclipse.update.core.IFeatureReference;
 import org.eclipse.update.core.IURLEntry;
 import org.eclipse.update.core.IVerificationListener;
 import org.eclipse.update.core.IVerificationResult;
 import org.eclipse.update.core.SiteManager;
 import org.eclipse.update.core.VersionedIdentifier;
 import org.eclipse.wst.server.core.internal.IInstallableServer;
 import org.eclipse.wst.server.core.internal.IMemento;
 import org.eclipse.wst.server.core.internal.InstallableServer;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.internal.XMLMemento;
 import org.eclipse.wst.server.ui.internal.Messages;
 import org.eclipse.wst.server.ui.internal.ProgressUtil;
 import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
 import org.eclipse.wst.server.ui.internal.Trace;
 import org.eclipse.wst.server.ui.internal.wizard.ExtensionWizard;
 import org.osgi.framework.Version;
 
 public class ExtensionUtility {
 	public static boolean launchExtensionWizard(Shell shell, String title, String message) {
 		ExtensionWizard wizard2 = new ExtensionWizard(title, message);
 		WizardDialog dialog = new WizardDialog(shell, wizard2);
 		if (dialog.open() != Window.CANCEL)
 			return true;
 		return false;
 	}
 
 	public static ExtensionSite[] getExtensionItems(URL url) throws CoreException {
 		InputStream in = null;
 		try {
 			in = url.openStream();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not load URL " + url);
 		}
 		
 		if (in == null)
 			throw new CoreException(new Status(IStatus.ERROR, ServerUIPlugin.PLUGIN_ID, 0, "Could not load extensions", null));
 		
 		try {
 			IMemento memento = XMLMemento.loadMemento(in);
 			IMemento children[] = memento.getChildren("site");
 			int size = children.length;
 			List<ExtensionSite> list = new ArrayList<ExtensionSite>(size);
 			for (int i = 0; i < size; i++) {
 				ExtensionSite item = new ExtensionSite(children[i]);
 				list.add(item);
 			}
 			
 			ExtensionSite[] items = new ExtensionSite[list.size()];
 			list.toArray(items);
 			return items;
 		} catch (Exception e) {
 			throw new CoreException(new Status(IStatus.ERROR, ServerUIPlugin.PLUGIN_ID, 0, e.getMessage(), e));
 		}
 	}
 
 	/**
 	 * Returns an array of all known extension items.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @return the array of extensions items {@link ExtensionSite}
 	 */
 	public static ExtensionSite[] getExtensionItems() {
 		URL url = ServerUIPlugin.getInstance().getBundle().getEntry("serverAdapterSites.xml");
 		
 		try {
 			return getExtensionItems(url);
 		} catch (CoreException ce) {
 			Trace.trace(Trace.SEVERE, "Could not get extension items");
 			return null;
 		}
 	}
 
 	/**
 	 * Return true if the new feature is already installed, or a newer one is.
 	 * 
 	 * @param existing
 	 * @param newFeature
 	 * @return true if the new feature is already installed, or a newer one is.
 	 */
 	protected static boolean alreadyExists(List existing, IFeature newFeature) {
 		if (existing.contains(newFeature))
 			return true;
 		
 		VersionedIdentifier newVi = newFeature.getVersionedIdentifier();
 		String ver = newVi.toString();
 		int ind = ver.indexOf("_");
 		if (ind >= 0)
 			ver = ver.substring(ind+1);
 		Version newV = new Version(ver);
 		
 		Iterator iterator = existing.iterator();
 		while (iterator.hasNext()) {
 			IFeature feature = (IFeature) iterator.next();
 			VersionedIdentifier vi = feature.getVersionedIdentifier(); 
 			if (vi.getIdentifier().equals(newVi.getIdentifier())) {
 				ver = vi.toString();
 				ind = ver.indexOf("_");
 				if (ind >= 0)
 					ver = ver.substring(ind+1);
 				Version nextCand = new Version(ver);
 				if (nextCand.compareTo(newV) >= 0)
 					return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public static void addFeature(List<IFeature> list, List<IFeature> existing, IFeature newFeature, FeatureListener listener) {
 		if (alreadyExists(existing, newFeature))
 			return;
 		
 		VersionedIdentifier newVi = newFeature.getVersionedIdentifier();
 		String ver = newVi.toString();
 		int ind = ver.indexOf("_");
 		if (ind >= 0)
 			ver = ver.substring(ind+1);
 		Version newV = new Version(ver);
 		IFeature remove = null;
 		
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			IFeature feature = (IFeature) iterator.next();
 			VersionedIdentifier vi = feature.getVersionedIdentifier(); 
 			if (vi.getIdentifier().equals(newVi.getIdentifier())) {
 				ver = vi.toString();
 				ind = ver.indexOf("_");
 				if (ind >= 0)
 					ver = ver.substring(ind+1);
 				Version nextCand = new Version(ver);
 				if (nextCand.compareTo(newV) < 0) {
 					remove = feature;
 				} else // new feature is older
 					return;
 			}
 		}
 		if (remove != null) {
 			list.remove(remove);
 			listener.featureRemoved(remove);
 		}
 		
 		list.add(newFeature);
 		listener.featureFound(newFeature);
 	}
 
 	public static void addFeatures(List<IFeature> list, List<IFeature> existing, List newFeatures, FeatureListener listener) {
 		Iterator iterator = newFeatures.iterator();
 		while (iterator.hasNext()) {
 			addFeature(list, existing, (IFeature) iterator.next(), listener);
 		}
 	}
 
 	public interface FeatureListener {
 		public void featureFound(IFeature feature);
 		public void featureRemoved(IFeature feature);
 		public void siteFailure(String host);
 	}
 
 	protected static List<IFeature> getExistingFeatures(IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask(Messages.installableServerLocal, 100);
 		ILocalSite site = SiteManager.getLocalSite();
 		IConfiguredSite[] sites = site.getCurrentConfiguration().getConfiguredSites();
 		int size = sites.length;
 		List<IFeature> list = new ArrayList<IFeature>(200);
 		for (int i = 0; i < size; i++) {
 			IFeatureReference[] refs = sites[i].getFeatureReferences();
 			int size2 = refs.length;
 			for (int j = 0; j < size2; j++) {
 				IFeature f = refs[j].getFeature(ProgressUtil.getSubMonitorFor(monitor, 5));
 				if (!list.contains(f))
 					list.add(f);
 			}
 		}
 		monitor.done();
 		
 		return list;
 	}
 
 	public static IFeature[] getAllFeatures(final String id, final FeatureListener listener, IProgressMonitor monitor) throws CoreException {
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.beginTask("", 1100);
 		
 		monitor.subTask(Messages.installableServerLocal);
 		final List<IFeature> existing = getExistingFeatures(ProgressUtil.getSubMonitorFor(monitor, 100));
 		
 		final ExtensionSite[] items = ExtensionUtility.getExtensionItems();
 		IInstallableServer[] servers = ServerPlugin.getInstallableServers();
 		final int x = 1000 / (items.length + servers.length);
 		
 		monitor.worked(50);
 		final List<IFeature> list = new ArrayList<IFeature>();
 		int size = items.length;
 		
 		Thread[] threads = new Thread[size];
 		for (int i = 0; i < size; i++) {
 			try {
 				if (monitor.isCanceled())
 					return null;
 				
 				monitor.subTask(NLS.bind(Messages.installableServerSearching, items[i].getUrl()));
 				final int ii = i;
 				final IProgressMonitor monitor2 = monitor;
 				threads[i] = new Thread("Extension Checker") {
 					public void run() {
 						try {
 							List<IFeature> list2 = items[ii].getFeatures(id, ProgressUtil.getSubMonitorFor(monitor2, x));
 							addFeatures(list, existing, list2, listener);
 						} catch (CoreException ce) {
 							listener.siteFailure(ce.getLocalizedMessage());
 							Trace.trace(Trace.WARNING, "Error downloading server adapter info", ce);
 						}
 					}
 				};
 				threads[i].setDaemon(true);
 				threads[i].start();
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Error downloading server adapter info 2", e);
 			}
 		}
 		
 		for (int i = 0; i < size; i++) {
 			try {
 				if (monitor.isCanceled())
 					return null;
 				
 				if (threads[i].isAlive())
 					threads[i].join();
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Error downloading server adapter info 3", e);
 			}
 		}
 		
 		// add installable servers
 		size = servers.length;
 		for (int i = 0; i < size; i++) {
 			if (monitor.isCanceled())
 				return null;
 			
 			InstallableServer is = (InstallableServer) servers[i];
 			monitor.subTask(NLS.bind(Messages.installableServerSearching, is.getFromSite()));
 			IFeature feature = is.getFeature(ProgressUtil.getSubMonitorFor(monitor, x));
 			if (feature != null)
 				addFeature(list, existing, feature, listener);
 		}
 		
 		IFeature[] ef = new IFeature[list.size()];
 		list.toArray(ef);
 		monitor.done();
 		return ef;
 	}
 
 	public static String getLicense(IFeature feature) {
 		IURLEntry license = feature.getLicense();
 		if (license != null)
 			return license.getAnnotation();
 		
 		return null;
 	}
 
 	public static String getDescription(IFeature feature) {
 		IURLEntry license = feature.getDescription();
 		if (license != null)
 			return license.getAnnotation();
 		
 		return null;
 	}
 
 	/*
 	 * @see IInstallableServer#install(IProgressMonitor)
 	 */
 	public static void install(IFeature feature, IProgressMonitor monitor) throws CoreException {
 		try {
 			IVerificationListener verificationListener = new IVerificationListener() {
 				public int prompt(IVerificationResult result) {
 					return CHOICE_INSTALL_TRUST_ONCE;
 				}
 			};
			SiteManager.getLocalSite().getCurrentConfiguration().getConfiguredSites()[0].install(feature, verificationListener, monitor);
 		} catch (CoreException ce) {
 			Trace.trace(Trace.WARNING, "Error installing server adapter", ce);
 			throw ce;
 		}
 		
 		try {
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 }
