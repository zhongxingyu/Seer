 /*******************************************************************************
  * Copyright (c) 2011 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.compiler.problem;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IScriptModelMarker;
 import org.eclipse.dltk.internal.core.InternalDLTKLanguageManager;
 import org.eclipse.dltk.utils.NatureExtensionManager;
 
 public enum DefaultProblemIdentifier implements IProblemIdentifier {
 	TASK;
 
 	public String contributor() {
 		return DLTKCore.PLUGIN_ID;
 	}
 
 	private static class Manager extends
 			NatureExtensionManager<IProblemIdentifierFactory> {
 
 		public Manager() {
 			super(InternalDLTKLanguageManager.PROBLEM_FACTORY_EXTPOINT,
 					IProblemIdentifierFactory.class);
 		}
 
 		@Override
 		protected boolean isValidElement(IConfigurationElement element) {
 			return "problemIdentifierFactory".equals(element.getName());
 		}
 
		@Override
		protected String getCategoryAttributeName() {
			return "namespace";
		}

 	}
 
 	private static synchronized Manager getManager() {
 		if (manager == null) {
 			manager = new Manager();
 		}
 		return manager;
 	}
 
 	public static IProblemIdentifier decode(int id) {
 		if (id == 0 || id == -1) {
 			return null;
 		}
 		return new ProblemIdentifierInt(id);
 	}
 
 	public static IProblemIdentifier decode(String id) {
 		if (id != null && id.length() != 0) {
 			final int pos = id.indexOf(SEPARATOR);
 			if (pos >= 0) {
 				IProblemIdentifierFactory[] factories = getManager()
 						.getInstances(id.substring(0, pos));
 				if (factories != null) {
 					final String localName = id.substring(pos + 1);
 					for (IProblemIdentifierFactory factory : factories) {
 						try {
 							final IProblemIdentifier value = factory
 									.valueOf(localName);
 							if (value != null) {
 								return value;
 							}
 						} catch (IllegalArgumentException e) {
 							// ignore
 						}
 					}
 				}
 			}
 			synchronized (reportedProblemIds) {
 				if (reportedProblemIds.size() < 100
 						&& reportedProblemIds.add(id)) {
 					DLTKCore.warn("Error decoding problem idenfier \"" + id
 							+ "\"");
 				}
 			}
 			try {
 				return new ProblemIdentifierInt(Integer.parseInt(id));
 			} catch (NumberFormatException e) {
 				// ignore
 			}
 			return new ProblemIdentifierString(id);
 		} else {
 			return null;
 		}
 	}
 
 	private static final Set<String> reportedProblemIds = new HashSet<String>();
 
 	public static IProblemIdentifier getProblemId(IMarker marker) {
 		return decode(marker.getAttribute(IScriptModelMarker.ID, null));
 	}
 
 	private static final char SEPARATOR = '#';
 
 	private static Manager manager = null;
 
 	public static String encode(IProblemIdentifier identifier) {
 		if (identifier == null) {
 			return Util.EMPTY_STRING;
 		} else if (identifier instanceof Enum<?>) {
 			return identifier.getClass().getName() + SEPARATOR
 					+ identifier.name();
 		} else {
 			return identifier.name();
 		}
 	}
 
 }
