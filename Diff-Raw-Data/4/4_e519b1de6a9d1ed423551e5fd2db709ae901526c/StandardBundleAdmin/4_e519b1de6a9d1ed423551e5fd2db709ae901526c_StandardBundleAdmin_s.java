 /*******************************************************************************
  * Copyright (c) 2007, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SpringSource, a divison of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.management.remote;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.osgi.framework.console.CommandProvider;
 import org.eclipse.osgi.framework.internal.core.Util;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.ExportPackageDescription;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.virgo.ide.management.remote.ServiceReference.Type;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 /**
  * @author Christian Dupuis
  */
 @ManagedResource(objectName = "org.eclipse.virgo.kernel:type=BundleAdmin", description = "Virgo IDE Connector Bean")
 public class StandardBundleAdmin implements BundleAdmin {
 
 	private final PlatformAdmin platformAdmin;
 
 	@SuppressWarnings("unused")
 	private final PackageAdmin packageAdmin;
 
 	private final BundleContext bundleContext;
 
	private ServiceTracker consoleProviderTracker;
 
 	public StandardBundleAdmin(PlatformAdmin platformAdmin, PackageAdmin packageAdmin, BundleContext bundleContext) {
 		this.platformAdmin = platformAdmin;
 		this.packageAdmin = packageAdmin;
 		this.bundleContext = bundleContext;
 		this.consoleProviderTracker = new ServiceTracker(bundleContext, CommandProvider.class.getName(), null);
 		this.consoleProviderTracker.open();
 	}
 
 	@ManagedOperation(description = "Stop the given bundle")
 	public void stop(long bundleId) {
 		execute("stop " + bundleId);
 	}
 
 	@ManagedOperation(description = "Start the given bundle")
 	public void start(long bundleId) {
 		execute("start " + bundleId);
 	}
 
 	@ManagedOperation(description = "Refresh the given bundle")
 	public void refresh(long bundleId) {
 		execute("refresh " + bundleId);
 	}
 
 	@ManagedOperation(description = "Refresh the given bundle")
 	public void update(long bundleId) {
 		execute("update " + bundleId);
 	}
 
 	@ManagedOperation(description = "Executes the given command")
 	public String execute(String cmdLine) {
 		StringWriter writer = new StringWriter();
 		PrintWriter printWriter = new PrintWriter(writer);
 		try {
 			ServerCommandInterpreter interpreter = new ServerCommandInterpreter(cmdLine, getServices(), printWriter);
 			interpreter.execute(interpreter.nextArgument());
 			try {
 				writer.close();
 				printWriter.close();
 			} catch (IOException e) {
 			}
 			return writer.toString();
 		} finally {
 			try {
 				writer.close();
 				printWriter.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	@ManagedOperation(description = "Returns the current OSGi Bundles")
 	public Map<Long, Bundle> retrieveBundles() {
 		Map<Long, Bundle> bundles = new HashMap<Long, Bundle>();
 		for (org.osgi.framework.Bundle b : bundleContext.getBundles()) {
 
 			Object version = b.getHeaders().get("Bundle-Version");
 			Bundle bundle = new Bundle(Long.toString(b.getBundleId()), b.getSymbolicName(), (version != null
 					? version.toString()
 					: "0"), getState(b), b.getLocation());
 
 			Dictionary<?, ?> headers = b.getHeaders();
 			Enumeration<?> keys = headers.keys();
 			while (keys.hasMoreElements()) {
 				Object key = keys.nextElement();
 				Object value = headers.get(key);
 				bundle.addHeader(key.toString(), value.toString());
 			}
 
 			BundleDescription bundleDescription = platformAdmin.getState(false).getBundle(b.getBundleId());
 
 			ExportPackageDescription[] exportedPackages = bundleDescription.getExportPackages();
 
 			if (exportedPackages != null) {
 				for (ExportPackageDescription exportedPackage : exportedPackages) {
 					PackageExport packageExport = new PackageExport(exportedPackage.getName(),
 							(exportedPackage.getVersion() != null ? exportedPackage.getVersion().toString() : "0"));
 					bundle.addPackageExport(packageExport);
 				}
 			}
 
 			ExportPackageDescription[] visiblePackages = platformAdmin.getStateHelper().getVisiblePackages(
 					bundleDescription);
 
 			if (visiblePackages != null) {
 				for (ExportPackageDescription visiblePackage : visiblePackages) {
 					PackageImport packageImport = new PackageImport(visiblePackage.getName(),
 							(visiblePackage.getVersion() != null ? visiblePackage.getVersion().toString() : "0"),
 							Long.toString(visiblePackage.getSupplier().getBundleId()));
 					bundle.addPackageImport(packageImport);
 				}
 			}
 
 			if (b.getRegisteredServices() != null) {
 				for (ServiceReference ref : b.getRegisteredServices()) {
 					org.eclipse.virgo.ide.management.remote.ServiceReference reference = new org.eclipse.virgo.ide.management.remote.ServiceReference(
 							Type.REGISTERED, ref.getBundle().getBundleId(),
 							OsgiServiceReferenceUtils.getServiceObjectClasses(ref));
 					Map<?, ?> props = OsgiServiceReferenceUtils.getServicePropertiesAsMap(ref);
 					for (Object key : props.keySet()) {
 						String value = props.get(key).toString();
 						if (props.get(key).getClass().isArray()) {
 							value = Arrays.deepToString((Object[]) props.get(key));
 						}
 						reference.addProperty(key.toString(), value);
 					}
 
 					if (ref.getUsingBundles() != null) {
 						for (org.osgi.framework.Bundle usingBundle : ref.getUsingBundles()) {
 							reference.addUsingBundle(usingBundle.getBundleId());
 						}
 					}
 					bundle.addRegisteredService(reference);
 				}
 			}
 
 			if (b.getServicesInUse() != null) {
 				for (ServiceReference ref : b.getServicesInUse()) {
 					org.eclipse.virgo.ide.management.remote.ServiceReference reference = new org.eclipse.virgo.ide.management.remote.ServiceReference(
 							Type.IN_USE, ref.getBundle().getBundleId(),
 							OsgiServiceReferenceUtils.getServiceObjectClasses(ref));
 					Map<?, ?> props = OsgiServiceReferenceUtils.getServicePropertiesAsMap(ref);
 					for (Object key : props.keySet()) {
 						String value = props.get(key).toString();
 						if (props.get(key).getClass().isArray()) {
 							value = Arrays.deepToString((Object[]) props.get(key));
 						}
 						reference.addProperty(key.toString(), value);
 					}
 
 					if (ref.getUsingBundles() != null) {
 						for (org.osgi.framework.Bundle usingBundle : ref.getUsingBundles()) {
 							reference.addUsingBundle(usingBundle.getBundleId());
 						}
 					}
 					bundle.addUsingService(reference);
 				}
 			}
 
 			bundles.put(Long.valueOf(bundle.getId()), bundle);
 		}
 		return bundles;
 	}
 
 	private Object[] getServices() {
 		ServiceReference[] serviceRefs = consoleProviderTracker.getServiceReferences();
 		if (serviceRefs == null) {
 			return new Object[0];
 		}
 		Util.dsort(serviceRefs, 0, serviceRefs.length);
 
 		Object[] serviceObjects = new Object[serviceRefs.length];
 		for (int i = 0; i < serviceRefs.length; i++) {
 			serviceObjects[i] = bundleContext.getService(serviceRefs[i]);
 		}
 		return serviceObjects;
 	}
 
 	private String getState(org.osgi.framework.Bundle b) {
 		switch (b.getState()) {
 		case org.osgi.framework.Bundle.ACTIVE:
 			return "ACTIVE";
 		case org.osgi.framework.Bundle.INSTALLED:
 			return "INSTALLED";
 		case org.osgi.framework.Bundle.RESOLVED:
 			return "RESOLVED";
 		case org.osgi.framework.Bundle.STARTING:
 			return "STARTING";
 		case org.osgi.framework.Bundle.STOPPING:
 			return "";
 		case org.osgi.framework.Bundle.UNINSTALLED:
 			return "UNINSTALLED";
 		default:
 			return "UNKNOWN";
 		}
 	}
 
 }
