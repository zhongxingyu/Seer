 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2010 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * Chul Kim
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.eclipse.visualization.containmenttree.viewer;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.s23m.cell.serialization.serializer.SerializationType;
 import org.s23m.cell.serialization.serializer.SerializerHolder;
import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ContainmentTreeViewerPlugin extends AbstractUIPlugin {
 
 	// The plug-in ID
	public static final String PLUGIN_ID = "org.s23m.cell.eclipse.visualization.containmenttree.viewer";
 
 	// The shared instance
 	private static ContainmentTreeViewerPlugin plugin;
 
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		SerializerHolder.getGmodelInstanceSerializer(SerializationType.XML); //to initialize the serializer
 		updateUIConfiguration();
 		addPreferencesChangeListener();
 	}
 
 	private void updateUIConfiguration() {
 	}
 
 	private void addPreferencesChangeListener() {
 	}
 
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static ContainmentTreeViewerPlugin getDefault() {
 		return plugin;
 	}
 
 }
