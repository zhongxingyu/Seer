 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class CodeGenUIPlugin extends AbstractUIPlugin {
 	private static CodeGenUIPlugin plugin;
 
 	public CodeGenUIPlugin() {
 		plugin = this;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		if (null == EPackage.Registry.INSTANCE.getEPackage(GMFGenPackage.eNS_URI)) {
 			EPackage.Registry.INSTANCE.put(GMFGenPackage.eNS_URI, new EPackage.Descriptor() {
 				public EPackage getEPackage() {
 					return GMFGenPackage.eINSTANCE;
 				}
 			});
 		}
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	public static CodeGenUIPlugin getDefault() {
 		return plugin;
 	}
 
 	public static String getBundleString(String key) {
 		return Platform.getResourceBundle(getDefault().getBundle()).getString(key);
 	}
 
 	public static String getBundleString(String key, Object[] args) {
 		String val = getBundleString(key);
 		if (val == null) {
 			return key;
 		}
 		return MessageFormat.format(val, args);
 	}
 
 	public static IStatus createStatus(int statusCode, String message, Exception ex) {
 		return new Status(statusCode, getPluginID(), 0, message, ex);
 	}
 	public static IStatus createError(String message, Exception ex) {
 		return createStatus(IStatus.ERROR, message, ex);
 	}
 	public static IStatus createWarning(String message) {
 		return createStatus(IStatus.WARNING, message, null);
 	}
 	public static IStatus createInfo(String message) {
 		return createStatus(IStatus.INFO, message, null);
 	}
 
 	public static String getPluginID() {
 		return getDefault().getBundle().getSymbolicName();
 	}
 
 	public static String formatMessage(String bundleStringKey, IStatus status) {
 		if (status.isMultiStatus()) {
 			IStatus[] children = status.getChildren();
 			StringBuffer sb = new StringBuffer();
 			// don't care about too nested statuses just because will switch to
 			// jobs soon, with
 			// required support already in place
			for (int i = 0; i < children.length; i++) {
 				sb.append(children[i].getMessage());
 				sb.append('\n');
 			}
 			return CodeGenUIPlugin.getBundleString(bundleStringKey, new Object[] { sb.toString() });
 		} else {
 			return CodeGenUIPlugin.getBundleString(bundleStringKey, new Object[] { status.getMessage() });
 		}
 	}
 }
