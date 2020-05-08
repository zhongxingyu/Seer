 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.core;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.debug.core.model.DbgpService;
 import org.eclipse.dltk.internal.debug.core.model.HotCodeReplaceManager;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.osgi.framework.BundleContext;
 
 public class DLTKDebugPlugin extends Plugin {
 
 	private static final String LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
 
 	public static final String PLUGIN_ID = "org.eclipse.dltk.debug"; //$NON-NLS-1$
 
 	public static final int INTERNAL_ERROR = 120;
 
 	private static DLTKDebugPlugin fgPlugin;
 
 	public static DLTKDebugPlugin getDefault() {
 		return fgPlugin;
 	}
 
 	public DLTKDebugPlugin() {
 		super();
 		fgPlugin = this;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		HotCodeReplaceManager.getDefault().startup();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		HotCodeReplaceManager.getDefault().shutdown();
 
 		super.stop(context);
 
 		if (dbgpService != null) {
 			dbgpService.shutdown();
 		}
 
 		ILaunchManager launchManager = DebugPlugin.getDefault()
 				.getLaunchManager();
 		IDebugTarget[] targets = launchManager.getDebugTargets();
 		for (int i = 0; i < targets.length; i++) {
 			IDebugTarget target = targets[i];
 			if (target instanceof ScriptDebugTarget) {
 				((ScriptDebugTarget) target).shutdown();
 			}
 		}
 	}
 
 	private DbgpService dbgpService;
 
 	public synchronized IDbgpService getDbgpService() {
 		if (dbgpService == null) {
 			dbgpService = new DbgpService();
 		}
 
 		return dbgpService;
 	}
 
 	// Logging
 	public static void log(Throwable t) {
 		Throwable top = t;
 		if (t instanceof DebugException) {
 			Throwable throwable = ((DebugException) t).getStatus()
 					.getException();
 			if (throwable != null) {
 				top = throwable;
 			}
 		}
 
 		log(new Status(
 				IStatus.ERROR,
 				PLUGIN_ID,
 				INTERNAL_ERROR,
 				Messages.DLTKDebugPlugin_internalErrorLoggedFromDltkDebugPlugin,
 				top));
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public String getBindAddress() {
 		String address = getPluginPreferences().getString(
 				DLTKDebugPreferenceConstants.PREF_DBGP_BIND_ADDRESS);
 		if (DLTKDebugPreferenceConstants.DBGP_AUTODETECT_BIND_ADDRESS
				.equals(address)) {
 			String[] ipAddresses = DLTKDebugPlugin.getLocalAddresses();
 			if (ipAddresses.length > 0) {
 				address = ipAddresses[0];
 			} else {
 				address = LOCALHOST;
 			}
 		}
 		return address;
 	}
 
 	public static String[] getLocalAddresses() {
 		Set addresses = new HashSet();
 		try {
 			InetAddress ip = null;
 			Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
 			while (netInterfaces.hasMoreElements()) {
 				NetworkInterface ni = (NetworkInterface) netInterfaces
 						.nextElement();
			    // ignore virtual interfaces for vmware, etc
 				if (ni.getName().startsWith("vmnet")) {
 					continue;
				}				
 				Enumeration inetAddresses = ni.getInetAddresses();
 				while (inetAddresses.hasMoreElements()) {
 					ip = (InetAddress) inetAddresses.nextElement();
 					if (!ip.getHostAddress().equals("127.0.0.1")
 							&& !ip.isLoopbackAddress()
 							&& ip.getHostAddress().indexOf(":") == -1) {
 						break;
 					} else {
 						ip = null;
 					}
 				}
 				if (ip != null) {
 					addresses.add(ip.getHostAddress());
 				}
 			}
 
 			if (addresses.isEmpty()) {
 				addresses.add(InetAddress.getLocalHost().getHostAddress());
 			}
 		} catch (SocketException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} catch (UnknownHostException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		String[] result = new String[addresses.size()];
 		addresses.toArray(result);
 		return result;
 	}
 
 }
