 /**
  * JBoss by Red Hat
  * Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.tools.ws.creation.core.utils;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 
 /**
  * @author bfitzpat
  *
  */
 public class RestEasyLibUtils {
 
 	private static final String REST_EASY = "RestEasy"; //$NON-NLS-1$
	private static final String JAXRS_API_PREFIX = "jaxrs-api"; //$NON-NLS-1$
	private static final String JAXRS_API_POSTFIX = ".jar"; //$NON-NLS-1$
 	private static final String LIB = "lib"; //$NON-NLS-1$
 	
 	/**
 	 * Simple check to see if the JBoss WS runtime associated with a project
 	 * actually includes the RESTEasy jars. If so, returns Status.OK_STATUS.
 	 * If not, returns null.
 	 * 
 	 * @param project
 	 * @return
 	 */
 	public static IStatus doesRuntimeSupportRestEasy ( IProject project ) {
 		try {
 			String path =
 				JBossWSCreationUtils.getJBossWSRuntimeLocation(project);
 			File runtime = new File(path);
 			if (runtime.exists()) {
 				File findJar = findLibDir(runtime);
 				if (findJar == null) {
 					File parent = runtime.getParentFile();
 					if (parent.exists() && parent.isDirectory()) {
 						File[] restEasyDir = parent.listFiles(new FilenameFilter() {
 							public boolean accept(File dir, String name) {
 								if (name.equalsIgnoreCase(REST_EASY)) {
 									return true;
 								}
 								return false;
 							}
 						});
 						if (restEasyDir != null && restEasyDir.length > 0) {
 							findJar = findLibDir(restEasyDir[0]);
 						}
 					}
 				}
 				if (findJar == null) { 
 					// if it's still null, resteasy's not installed
 					return StatusUtils.errorStatus(JBossWSCreationCoreMessages.AddRestEasyJarsCommand_RestEasy_JARS_Not_Found);
 				}
 			}
 		} catch (CoreException ce) {
 			return StatusUtils.errorStatus(JBossWSCreationCoreMessages.RestEasyLibUtils_Error_UnableToFindRuntimeForProject);
 		}
 		return Status.OK_STATUS;
 	}
 	
 	/*
 	 * Finds the RESTEasy lib in the runtime path
 	 * @param in
 	 * @return
 	 */
 	private static File findLibDir ( File in ) {
 		File[] children = 
 			in.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				if (dir.isDirectory() && name.equals(LIB)) {
 					return true;
 				}
 				return false;
 			}
 		});
 		if (children != null ) {
 			for (int i = 0; i < children.length; i++) {
 				File libDir = (File) children[i];
 				if (libDir.exists() && libDir.isDirectory()) {
 					File[] jars = libDir.listFiles(new FilenameFilter() {
 						public boolean accept(File dir, String name) {
							if (name.startsWith(JAXRS_API_PREFIX) && name.endsWith(JAXRS_API_POSTFIX)) {
 								return true;
 							}
 							return false;
 						}
 					});
 					if (jars != null && jars.length > 0) {
 						return libDir;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 }
