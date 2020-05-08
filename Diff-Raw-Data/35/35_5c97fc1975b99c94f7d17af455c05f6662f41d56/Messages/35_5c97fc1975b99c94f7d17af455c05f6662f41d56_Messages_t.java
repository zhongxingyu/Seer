 /*******************************************************************************
  * Copyright (c) 2011-2014 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.views.emfstorebrowser.dialogs.admin;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * UI Accesscontrol related messages.
  * 
  * @author emueller
  * 
  */
 public final class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.emf.emfstore.internal.client.ui.views.emfstorebrowser.dialogs.admin.messages"; //$NON-NLS-1$
	public static String ProjectComposite_Participants;
	public static String ProjectComposite_ProjectAdmin;
	public static String ProjectComposite_Projects;
	public static String ProjectComposite_Reader;
	public static String ProjectComposite_Role;
	public static String ProjectComposite_Select_Participant;
	public static String ProjectComposite_ServerAdmin;
	public static String ProjectComposite_Version;
	public static String ProjectComposite_Writer;
 	public static String PropertiesComposite_Add;
 	public static String PropertiesComposite_Could_Not_Fetch_Group_Members;
 	public static String PropertiesComposite_Could_Not_Fetch_Groups;
 	public static String PropertiesComposite_Could_Not_Fetch_Participants;
 	public static String PropertiesComposite_Description;
 	public static String PropertiesComposite_Name;
 	public static String PropertiesComposite_Properties;
 	public static String PropertiesComposite_Remove;
 	public static String PropertiesForm_Group;
 	public static String PropertiesForm_Project;
 	public static String PropertiesForm_User;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
