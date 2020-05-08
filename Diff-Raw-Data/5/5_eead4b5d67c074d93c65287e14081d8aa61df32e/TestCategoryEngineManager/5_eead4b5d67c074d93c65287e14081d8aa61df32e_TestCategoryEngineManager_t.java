 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.testing;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.dltk.internal.testing.launcher.NullTestRunnerUI;
 import org.eclipse.dltk.internal.testing.util.NumberUtils;
 import org.eclipse.dltk.testing.DLTKTestingPlugin;
 import org.eclipse.dltk.testing.ITestCategoryEngine;
 import org.eclipse.dltk.testing.ITestRunnerUI;
 import org.eclipse.dltk.utils.NatureExtensionManager;
 
 public class TestCategoryEngineManager extends NatureExtensionManager {
 
 	private static final String EXTENSION_POINT = DLTKTestingPlugin.PLUGIN_ID
 			+ ".categoryEngine"; //$NON-NLS-1$
 
 	protected String getCategoryAttributeName() {
 		return "testingEngineId"; //$NON-NLS-1$
 	}
 
 	private static class Descriptor {
 
 		final IConfigurationElement element;
 		final int priority;
 
 		/**
 		 * @param confElement
 		 * @param priority
 		 */
 		public Descriptor(IConfigurationElement confElement, int priority) {
 			this.element = confElement;
 			this.priority = priority;
 		}
 
 	}
 
 	private TestCategoryEngineManager() {
 		super(EXTENSION_POINT, Descriptor.class);
 	}
 
 	private static final String PRIORITY_ATTR = "priority"; //$NON-NLS-1$
 
 	protected Object createDescriptor(IConfigurationElement confElement) {
 		final String strPriority = confElement.getAttribute(PRIORITY_ATTR);
 		int priority = NumberUtils.toInt(strPriority);
 		return new Descriptor(confElement, priority);
 	}
 
 	private final Comparator descriptorComparator = new Comparator() {
 
 		public int compare(Object o1, Object o2) {
 			Descriptor descriptor1 = (Descriptor) o1;
 			Descriptor descriptor2 = (Descriptor) o2;
 			return descriptor1.priority - descriptor2.priority;
 		}
 
 	};
 
 	protected void initializeDescriptors(List descriptors) {
 		Collections.sort(descriptors, descriptorComparator);
 	}
 
 	protected Object createInstanceByDescriptor(Object descriptor)
 			throws CoreException {
 		return descriptor;
 	}
 
 	protected Object[] createEmptyResult() {
 		return new Descriptor[0];
 	}
 
 	private static TestCategoryEngineManager instance = null;
 
 	private static synchronized TestCategoryEngineManager getInstance() {
 		if (instance == null) {
 			instance = new TestCategoryEngineManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Returns the category engines registered for the specified testing engine
 	 * or <code>null</code>.
 	 * 
 	 * @param runnerUI
 	 * @return
 	 */
 	public static ITestCategoryEngine[] getCategoryEngines(
 			ITestRunnerUI runnerUI) {
		if (runnerUI instanceof NullTestRunnerUI) {
			return null;
		}
 		final Descriptor[] descriptors = (Descriptor[]) getInstance()
 				.getInstances(runnerUI.getTestingEngine().getId());
 		if (descriptors == null) {
 			return null;
 		}
 		final List result = new ArrayList(descriptors.length);
 		for (int i = 0; i < descriptors.length; ++i) {
 			final Descriptor descriptor = descriptors[i];
 			ITestCategoryEngine categoryEngine;
 			try {
 				categoryEngine = (ITestCategoryEngine) descriptor.element
 						.createExecutableExtension("class"); //$NON-NLS-1$
 				if (categoryEngine.initialize(runnerUI)) {
 					result.add(categoryEngine);
 				}
 			} catch (CoreException e) {
 				DLTKTestingPlugin.log("Error creating category engine", e); //$NON-NLS-1$
 			} catch (ClassCastException e) {
 				DLTKTestingPlugin.log("Error creating category engine", e); //$NON-NLS-1$
 			}
 		}
 		if (result.isEmpty()) {
 			return null;
 		} else {
 			return (ITestCategoryEngine[]) result
 					.toArray(new ITestCategoryEngine[result.size()]);
 		}
 	}
 }
