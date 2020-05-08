 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.debug.tests.core;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IBuiltinModuleProvider;
 import org.eclipse.dltk.debug.tests.AbstractDebugTest;
 import org.eclipse.dltk.internal.launching.InterpreterContainer;
 import org.eclipse.dltk.internal.launching.InterpreterContainerInitializer;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterStandin;
 import org.eclipse.dltk.launching.LibraryLocation;
 import org.eclipse.dltk.launching.ScriptRuntime;
 
 
 /**
  * Tests interpreter buildpath container
  */
 public class BuildpathContainerTests extends AbstractDebugTest {
 	
 	class FakeContainer implements IBuildpathContainer {
 		
 		IBuildpathEntry[] entries = new IBuildpathEntry[0];
 		
 		public IBuildpathEntry[] getBuildpathEntries() {
 			return entries;
 		}
 		
 		public String getDescription() {
 			return "Fake";
 		}
 		
 		public int getKind() {
 			return IBuildpathContainer.K_DEFAULT_SYSTEM;
 		}
 
 		public IPath getPath() {
 			return new Path(ScriptRuntime.INTERPRETER_CONTAINER);
 		}
 		
 		public void setEntries(IBuildpathEntry[] cpe) {
 			entries = cpe;
 		}

		public IBuiltinModuleProvider getBuiltinProvider() {
			// TODO Auto-generated method stub
			return null;
		}
 	}
 	
 	public BuildpathContainerTests(String name) {
 		super(name);
 	}
 
 	/**
 	 * Tests that the container will accept an update
 	 */
 	public void testCanUpdate() throws CoreException {
 		// Create a new Interpreter install that mirros the current install
 		IInterpreterInstall def = ScriptRuntime.getDefaultInterpreterInstall("testnature");
 		String InterpreterId = def.getId() + System.currentTimeMillis();
 		InterpreterStandin standin = new InterpreterStandin(def.getInterpreterInstallType(), InterpreterId);
 		String InterpreterName = "Alternate Interpreter";
 		IPath containerPath = new Path(ScriptRuntime.INTERPRETER_CONTAINER);
 		containerPath = containerPath.append(new Path(def.getInterpreterInstallType().getId()));
 		containerPath = containerPath.append(new Path(InterpreterName));
 		standin.setName(InterpreterName);
 		standin.setInstallLocation(def.getInstallLocation());
 		standin.setLibraryLocations(ScriptRuntime.getLibraryLocations(def));
 		standin.convertToRealInterpreter();
 		
 		// ensure the new Interpreter exists
 		IInterpreterInstall newInterpreter = def.getInterpreterInstallType().findInterpreterInstall(InterpreterId);
 		assertNotNull("Failed to create new Interpreter", newInterpreter);
 		
 		InterpreterContainer container = new InterpreterContainer(newInterpreter, containerPath);
 		InterpreterContainerInitializer initializer = new InterpreterContainerInitializer();
 		// store the current library settings
 		LibraryLocation[] originalLibs = ScriptRuntime.getLibraryLocations(newInterpreter);
 		assertTrue("Libraries should not be empty", originalLibs.length > 0);
 		IBuildpathEntry[] originalEntries = container.getBuildpathEntries();
 		assertEquals("Libraries should be same size as buildpath entries", originalLibs.length, originalEntries.length);
 		
 		// ensure we can update
 		assertTrue("Initializer will not accept update", initializer.canUpdateBuildpathContainer(containerPath, getDLTKProject()));
 		
 		// update to an empty set of libs
 		FakeContainer fakeContainer = new FakeContainer();
 		initializer.requestBuildpathContainerUpdate(containerPath, getDLTKProject(), fakeContainer);
 		
 		// ensure the library locations are now empty on the new Interpreter
 		LibraryLocation[] newLibs = ScriptRuntime.getLibraryLocations(newInterpreter);
 		assertEquals("Libraries should be empty", 0, newLibs.length);
 		
 		// re-set to original libs
 		fakeContainer.setEntries(originalEntries);
 		initializer.requestBuildpathContainerUpdate(containerPath, getDLTKProject(), fakeContainer);
 		
 		// ensure libs are restored
 		newLibs = ScriptRuntime.getLibraryLocations(newInterpreter);
 		assertEquals("Libraries should be restored", originalLibs.length, newLibs.length);
 		for (int i = 0; i < newLibs.length; i++) {
 			LibraryLocation location = newLibs[i];
 			LibraryLocation origi = originalLibs[i];
 			assertEquals("Library should be the eqaual", origi.getSystemLibraryPath().toFile(), location.getSystemLibraryPath().toFile());
 		} 
 	}
 }
