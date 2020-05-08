 /*******************************************************************************
  * Copyright (c) 2013 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.buildpath;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.tests.ProblemTestUtil;
 import org.eclipse.dltk.core.tests.ProjectSetup;
 import org.eclipse.dltk.core.tests.model.ModelTestsPlugin;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.junit.rules.TestName;
 
 public class SetContainerEventsTest extends Assert {
 
 	@Rule
 	public final ProjectSetup project = new ProjectSetup(
 			ModelTestsPlugin.WORKSPACE, "SetContainerEvents",
 			ProjectSetup.Option.INDEXER_DISABLED);
 
 	@Rule
 	public final TemporaryFolder temp = new TemporaryFolder();
 
 	@Rule
 	public final TestName testname = new TestName();
 
 	static final boolean VERBOSE = false;
 
 	@Test
 	public void assertNoProblems() throws CoreException {
 		ProblemTestUtil.assertNoProblems(project.get());
 	}
 
 	final List<IModelElementDelta> added = new ArrayList<IModelElementDelta>();
 	final List<IModelElementDelta> changed = new ArrayList<IModelElementDelta>();
 	final List<IModelElementDelta> removed = new ArrayList<IModelElementDelta>();
 
 	final IElementChangedListener listener = new IElementChangedListener() {
 		public void elementChanged(ElementChangedEvent event) {
 			if (VERBOSE) {
 				System.out.println(testname.getMethodName() + " " + event);
 			}
 			visit(event.getDelta());
 			if (VERBOSE) {
 				System.out.println(added);
 				System.out.println(changed);
 				System.out.println(removed);
 				System.out.println("");
 			}
 		}
 
 		private void visit(IModelElementDelta delta) {
 			if (!(delta.getElement() instanceof IScriptModel)) {
 				switch (delta.getKind()) {
 				case IModelElementDelta.ADDED:
 					added.add(delta);
 					break;
 				case IModelElementDelta.CHANGED:
 					changed.add(delta);
 					break;
 				case IModelElementDelta.REMOVED:
 					removed.add(delta);
 					break;
 				}
 			}
 			for (IModelElementDelta child : delta.getAffectedChildren()) {
 				visit(child);
 			}
 		}
 	};
 
	@Test
 	public void setContainer() throws ModelException,IOException {
 		final File folder1 = temp.newFolder("folder1");
 		DLTKCore.addElementChangedListener(listener,
 				IResourceChangeEvent.POST_CHANGE);
 		try {
 			DLTKCore.setBuildpathContainer(TestContainer2.CONTAINER_ID,
 					new IScriptProject[] { project.getScriptProject() },
 					new IBuildpathContainer[] { new TestContainer2(folder1) },
 					null);
 		} finally {
 			DLTKCore.removeElementChangedListener(listener);
 		}
 		assertEquals(1, added.size());
 		assertThat(added.get(0).getElement(),
 				instanceOf(IProjectFragment.class));
 		assertEquals("folder1", ((IProjectFragment) added.get(0).getElement())
 				.getPath().lastSegment());
 	}
 
 	@Test
 	public void changeContainer() throws ModelException,IOException {
 		final File folder1 = temp.newFolder("folder1");
 		final File folder2 = temp.newFolder("folder2");
 		DLTKCore.setBuildpathContainer(TestContainer2.CONTAINER_ID,
 				new IScriptProject[] { project.getScriptProject() },
 				new IBuildpathContainer[] { new TestContainer2(folder1) }, null);
 		DLTKCore.addElementChangedListener(listener,
 				IResourceChangeEvent.POST_CHANGE);
 		try {
 			DLTKCore.setBuildpathContainer(TestContainer2.CONTAINER_ID,
 					new IScriptProject[] { project.getScriptProject() },
 					new IBuildpathContainer[] { new TestContainer2(folder2) },
 					null);
 		} finally {
 			DLTKCore.removeElementChangedListener(listener);
 		}
 
 		// XXX (alex) results are not fully correct here, as
 		// BuildpathChange.generateDelta() calls
 		// ScriptProject.computeProjectFragments() with checkExistence=false and
 		// ScriptProjectFragment.getProjectFragment(IPath) can't return fragment
 		// for the external path.
 		assertEquals(0, added.size());
 		assertEquals(0, removed.size());
 		assertEquals(2, changed.size());
 		assertEquals(project.getScriptProject(), changed.get(0).getElement());
 		assertEquals("folder2",
 				((IProjectFragment) changed.get(1).getElement()).getPath()
 						.lastSegment());
 		assertTrue((changed.get(1).getFlags() & IModelElementDelta.F_ADDED_TO_BUILDPATH) != 0);
 	}
 
 }
