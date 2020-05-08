 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.simpleservices.authorizationservice.store;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.security.Permission;
 import java.security.Permissions;
 import java.security.Principal;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.riena.security.authorizationservice.IPermissionStore;
 import org.eclipse.riena.security.common.authorization.PermissionClassFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * 
  */
 public class FilePermissionStore implements IPermissionStore {
 
 	private Document permissionTree;
 
	public FilePermissionStore(File permissionFile) throws SAXException, IOException, ParserConfigurationException {
 		this(new FileInputStream(permissionFile));
 	}
 
 	public FilePermissionStore(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
 		super();
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		permissionTree = db.parse(new InputSource(inputStream));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.security.simpleservices.authorizationservice.IPermissionStore#loadPermissions(javax.security.auth.Subject)
 	 */
 	public Permissions loadPermissions(Principal principal) {
 		Permissions allPerms = new Permissions();
 		NodeList nl = permissionTree.getDocumentElement().getElementsByTagName("principal"); //$NON-NLS-1$
 		for (int i = 0; i < nl.getLength(); i++) {
 			Element el = (Element) nl.item(i);
 			String principalClazz = el.getAttribute("class"); //$NON-NLS-1$
 			String principalName = el.getAttribute("name"); //$NON-NLS-1$
 			if (!principalClazz.equals(principal.getClass().getName()) || !principalName.equals(principal.getName())) {
 				continue;
 			}
 			if (principal.getClass().getName().equals(el.getAttribute("class"))) { //$NON-NLS-1$
 				NodeList nlPerms = el.getElementsByTagName("permission"); //$NON-NLS-1$
 				for (int x = 0; x < nlPerms.getLength(); x++) {
 					Element ePerm = (Element) nlPerms.item(x);
 					String clazz = ePerm.getAttribute("class"); //$NON-NLS-1$
 					String name = ePerm.getAttribute("name"); //$NON-NLS-1$
 					String action = ePerm.getAttribute("action"); //$NON-NLS-1$
 					try {
 						// its not good to use Class.forName
 						// so we use a specific factory
 						Class<?> permClass = PermissionClassFactory.retrieveClass(clazz);
 						Constructor<?> constr;
 						Permission perm;
 						if (action != null && action.length() > 0) {
 							try {
 								int actInt = Integer.parseInt(action);
 								constr = permClass.getConstructor(String.class, int.class);
 								perm = (Permission) constr.newInstance(name, actInt);
 							} catch (NumberFormatException e) {
 								constr = permClass.getConstructor(String.class, String.class);
 								perm = (Permission) constr.newInstance(name, action);
 							}
 						} else {
 							constr = permClass.getConstructor(String.class);
 							perm = (Permission) constr.newInstance(name);
 						}
 						if (perm != null) {
 							allPerms.add(perm);
 						}
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					} catch (SecurityException e) {
 						e.printStackTrace();
 					} catch (NoSuchMethodException e) {
 						e.printStackTrace();
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					} catch (InvocationTargetException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return allPerms;
 	}
 }
