 /*******************************************************************************
  * Copyright (c) 2010 Broadcom Corporation and others. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Broadcom Corporation - initial API and implementation
  ******************************************************************************/
 package org.eclipse.core.tests.internal.builders;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.internal.resources.ProjectVariantReference;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * These tests exercise the project variants functionality which allows a different
  * builder to be run for different project variants.
  */
 public class BuildVariantsTest extends AbstractBuilderTest {
 	public static Test suite() {
 		return new TestSuite(BuildVariantsTest.class);
 	}
 
 	private IProject project0;
 	private IProject project1;
 	private IFile file0;
 	private IFile file1;
 	private final String variant0 = "Variant0";
 	private final String variant1 = "Variant1";
 	private final String variant2 = "Variant2";
 
 	public BuildVariantsTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		// Create resources
 		IWorkspaceRoot root = getWorkspace().getRoot();
 		project0 = root.getProject("Project0");
 		project1 = root.getProject("Project1");
 		file0 = project0.getFile("File0");
 		file1 = project1.getFile("File1");
 		IResource[] resources = {project0, project1, file0, file1};
 		ensureExistsInWorkspace(resources, true);
 		setAutoBuilding(false);
 		setupProject(project0);
 		setupProject(project1);
 	}
 
 	/**
 	 * Helper method to configure a project with a build command and several variants.
 	 */
 	private void setupProject(IProject project) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 
 		// Add build command
 		ICommand command = createCommand(desc, VariantBuilder.BUILDER_NAME, "Build0");
 		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
 		desc.setBuildSpec(new ICommand[] {command});
 
 		// Create variants
 		desc.setVariants(new IProjectVariant[] {desc.newVariant(variant0), desc.newVariant(variant1), desc.newVariant(variant2)});
 
 		project.setDescription(desc, getMonitor());
 	}
 
 	/**
 	 * Tests that an incremental builder is run/not run correctly, depending on deltas,
 	 * and is given the correct deltas depending on which project variant is being built
 	 */
 	public void testDeltas() throws CoreException {
 		VariantBuilder.clearStats();
 		// Run some incremental builds while varying the active variant and whether the project was modified
 		// and check that the builder is run/not run with the correct trigger
 		file0.setContents(getRandomContents(), true, true, getMonitor());
 		incrementalBuild(1, project0, variant1, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		incrementalBuild(2, project0, variant1, false, 1, 0);
 		incrementalBuild(3, project0, variant2, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		incrementalBuild(4, project0, variant1, false, 1, 0);
 		file0.setContents(getRandomContents(), true, true, getMonitor());
 		incrementalBuild(5, project0, variant1, true, 2, IncrementalProjectBuilder.INCREMENTAL_BUILD);
 		incrementalBuild(6, project0, variant2, true, 2, IncrementalProjectBuilder.INCREMENTAL_BUILD);
 		incrementalBuild(7, project0, variant0, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 	}
 
 	/**
 	 * Tests that deltas are preserved per variant when a project is closed then opened.
 	 */
 	public void testCloseAndOpenProject() throws CoreException {
 		VariantBuilder.clearStats();
 		file0.setContents(getRandomContents(), true, true, getMonitor());
 		incrementalBuild(1, project0, variant0, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		incrementalBuild(2, project0, variant1, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		incrementalBuild(3, project0, variant2, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 
 		project0.close(getMonitor());
 		VariantBuilder.clearStats();
 		project0.open(getMonitor());
 
 		incrementalBuild(4, project0, variant0, false, 0, 0);
 		incrementalBuild(5, project0, variant1, false, 0, 0);
 		incrementalBuild(6, project0, variant2, false, 0, 0);
 	}
 
 	/**
 	 * Run a workspace build with project references
 	 * 
 	 * References are:
 	 *     p0,v0 depends on p0,v1
 	 *     p0,v0 depends on p1,v0
 	 *     p0,v0 depends on p1,v2
 	 * Active variants are:
 	 *     p0,v0 and p1,v0
 	 * Build order should be:
 	 *     p0,v1  p1,v0  p1,v2  p0,v0
 	 */
 	public void testBuildReferences() throws CoreException {
 		VariantBuilder.clearStats();
 		VariantBuilder.clearBuildOrder();
 		IProjectDescription desc = project0.getDescription();
 		desc.setActiveVariant(variant0);
 		project0.setDescription(desc, getMonitor());
 		desc = project1.getDescription();
 		desc.setActiveVariant(variant0);
 		project1.setDescription(desc, getMonitor());
 
 		// Note: references are not alphabetically ordered to check that references are sorted into a stable order
 		setReferences(project0, variant0, new IProjectVariant[] {project0.getVariant(variant1), project1.getVariant(variant2), project1.getVariant(variant0)});
 		getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
 
 		assertEquals("1.0", 4, VariantBuilder.buildOrder.size());
 		assertEquals("1.1", project0.getVariant(variant1), VariantBuilder.buildOrder.get(0));
 		assertEquals("1.2", project1.getVariant(variant0), VariantBuilder.buildOrder.get(1));
 		assertEquals("1.3", project1.getVariant(variant2), VariantBuilder.buildOrder.get(2));
 		assertEquals("1.4", project0.getVariant(variant0), VariantBuilder.buildOrder.get(3));
 		checkBuild(2, project0, variant0, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		checkBuild(3, project0, variant1, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		checkBuild(4, project0, variant2, false, 0, 0);
 		checkBuild(5, project1, variant0, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		checkBuild(6, project1, variant1, false, 0, 0);
 		checkBuild(7, project1, variant2, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 
 		// Modify project1, all project1 builders should do an incremental build
 		file1.setContents(getRandomContents(), true, true, getMonitor());
 
 		VariantBuilder.clearBuildOrder();
 		getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
 
 		assertEquals("8.0", 2, VariantBuilder.buildOrder.size());
 		assertEquals("8.1", project1.getVariant(variant0), VariantBuilder.buildOrder.get(0));
 		assertEquals("8.2", project1.getVariant(variant2), VariantBuilder.buildOrder.get(1));
 		checkBuild(9, project0, variant0, false, 1, 0);
 		checkBuild(10, project0, variant1, false, 1, 0);
 		checkBuild(11, project0, variant2, false, 0, 0);
 		checkBuild(12, project1, variant0, true, 2, IncrementalProjectBuilder.INCREMENTAL_BUILD);
 		checkBuild(13, project1, variant1, false, 0, 0);
 		checkBuild(14, project1, variant2, true, 2, IncrementalProjectBuilder.INCREMENTAL_BUILD);
 	}
 
 	/**
 	 * Tests that cleaning a project variant does not affect other variants in the same project
 	 */
 	public void testClean() throws CoreException {
 		VariantBuilder.clearStats();
 		incrementalBuild(1, project0, variant0, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		incrementalBuild(2, project0, variant1, true, 1, IncrementalProjectBuilder.FULL_BUILD);
 		clean(3, project0, variant0, 2);
 		incrementalBuild(4, project0, variant1, false, 1, 0);
 	}
 
 	/**
 	 * Helper method to set the references for a project.
 	 */
 	private void setReferences(IProject project, String variant, IProjectVariant[] variants) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 		IProjectVariantReference[] refs = new IProjectVariantReference[variants.length];
 		for (int i = 0; i < variants.length; i++)
 			refs[i] = new ProjectVariantReference(variants[i]);
 		desc.setReferencedProjectVariants(variant, refs);
 		project.setDescription(desc, getMonitor());
 	}
 
 	/**
 	 * Run an incremental build for the given project variant, and check the behaviour of the build.
 	 */
 	private void incrementalBuild(int testId, IProject project, String variant, boolean shouldBuild, int expectedCount, int expectedTrigger) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setActiveVariant(variant);
		project.setDescription(desc, getMonitor());
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
 		checkBuild(testId, project, variant, shouldBuild, expectedCount, expectedTrigger);
 	}
 
 	/**
 	 * Clean the specified project variant.
 	 */
 	private void clean(int testId, IProject project, String variant, int expectedCount) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 		desc.setActiveVariant(variant);
 		project.setDescription(desc, getMonitor());
 		project.build(IncrementalProjectBuilder.CLEAN_BUILD, getMonitor());
 		VariantBuilder builder = VariantBuilder.getBuilder(project.getVariant(variant));
 		assertNotNull(testId + ".0", builder);
 		assertEquals(testId + ".1", expectedCount, builder.buildCount);
 		assertEquals(testId + ".2", IncrementalProjectBuilder.CLEAN_BUILD, builder.triggerForLastBuild);
 	}
 
 	/**
 	 * Check the behaviour of a build
 	 */
 	private void checkBuild(int testId, IProject project, String variant, boolean shouldBuild, int expectedCount, int expectedTrigger) throws CoreException {
 		try {
 			project.getVariant(variant);
 		} catch (CoreException e) {
 			fail(testId + ".0");
 		}
 		VariantBuilder builder = VariantBuilder.getBuilder(project.getVariant(variant));
 		if (builder == null) {
 			assertFalse(testId + ".1", shouldBuild);
 			assertEquals(testId + ".2", 0, expectedCount);
 		} else {
 			assertEquals(testId + ".3", expectedCount, builder.buildCount);
 			if (shouldBuild)
 				assertEquals(testId + ".4", expectedTrigger, builder.triggerForLastBuild);
 		}
 	}
 }
