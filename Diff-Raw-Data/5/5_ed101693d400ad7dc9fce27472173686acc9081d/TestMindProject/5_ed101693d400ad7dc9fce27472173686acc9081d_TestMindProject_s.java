 package org.ow2.mindEd.ide.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.cdt.core.CProjectNature;
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
 import org.eclipse.cdt.core.settings.model.ICOutputEntry;
 import org.eclipse.cdt.core.settings.model.ICProjectDescription;
 import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
 import org.eclipse.cdt.core.settings.model.ICSourceEntry;
 import org.eclipse.cdt.core.settings.model.extension.CBuildData;
 import org.eclipse.cdt.managedbuilder.core.IConfiguration;
 import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
 import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.ow2.mindEd.ide.core.FamilyJobCST;
 import org.ow2.mindEd.ide.core.MindIdeBuilder;
 import org.ow2.mindEd.ide.core.MindIdeCore;
 import org.ow2.mindEd.ide.core.MindModelManager;
 import org.ow2.mindEd.ide.core.MindNature;
 import org.ow2.mindEd.ide.core.impl.CDTUtil;
 import org.ow2.mindEd.ide.core.impl.MindMakefile;
 import org.ow2.mindEd.ide.core.impl.MindPathEntryCustomImpl;
 import org.ow2.mindEd.ide.core.impl.MindProjectImpl;
 import org.ow2.mindEd.ide.core.impl.UtilMindIde;
 import org.ow2.mindEd.ide.model.MindAdl;
 import org.ow2.mindEd.ide.model.MindFile;
 import org.ow2.mindEd.ide.model.MindObject;
 import org.ow2.mindEd.ide.model.MindPackage;
 import org.ow2.mindEd.ide.model.MindPathEntry;
 import org.ow2.mindEd.ide.model.MindProject;
 import org.ow2.mindEd.ide.model.MindRootSrc;
 import org.ow2.mindEd.ide.model.MindidePackage;
 
 public class TestMindProject {
 	protected static final int DEFAULT_TIME_OUT_WAIT_JOB = 10000;
 	static HashSet<IProject> createdProject = new HashSet<IProject>();
 
 	static class TestListener extends EContentAdapter {
 		@Override
 		public void notifyChanged(Notification notification) {
 			if (notification.getFeature() == MindidePackage.Literals.MIND_REPO__MINDPROJECTS) {
 				switch (notification.getEventType()) {
 				case Notification.ADD:
 					resolve((MindProject) notification.getNewValue());
 					break;
 				case Notification.ADD_MANY:
 					for (MindProject p : ((Collection<MindProject>) notification
 							.getNewValue())) {
 						resolve(p);
 					}
 					break;
 				case Notification.REMOVE:
 					unresolve((MindProject) notification.getOldValue());
 					break;
 				case Notification.REMOVE_MANY:
 					for (MindProject p : ((Collection<MindProject>) notification
 							.getOldValue())) {
 						unresolve(p);
 					}
 					break;
 
 				}
 				return;
 			}
 		}
 
 		private void unresolve(MindProject p) {
 		}
 
 		private void resolve(MindProject newValue) {
 			if (newValue.getProject() != null)
 				createdProject.add(newValue.getProject());
 		}
 	}
 
 	@BeforeClass
 	static public void fqoi() {
 		MindIdeCore.getModel().getWSRepo().eAdapters().add(new TestListener());
 	}
 
 	@After
 	public void deleteProjectAfter() {
 		for (IProject p : createdProject) {
 			if (p.exists())
 				try {
 					p.delete(true, null);
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 		}
 		createdProject.clear();
 	}
 
 	static public void assertEqualsMPE(MindPathEntry expected,
 			MindPathEntry actual) {
 		assertEqualsMPE(null, expected, actual);
 	}
 
 	static public void assertEqualsMPE(String message, MindPathEntry expected,
 			MindPathEntry actual) {
 		if (expected == null && actual == null)
 			return;
 		if (expected != null
 				&& MindPathEntryCustomImpl.equals(expected, actual))
 			return;
 
 		String formatted = "";
 		if (message != null && !message.equals(""))
 			formatted = message + " ";
 		String expectedString = MindPathEntryCustomImpl.toString(expected);
 		String actualString = MindPathEntryCustomImpl.toString(actual);
 		fail(formatted + "expected:<" + expectedString + "> but was:<"
 				+ actualString + ">");
 	}
 	
 	
 
 	@Test
 	public void testAPI() throws UnsupportedEncodingException, CoreException {
 		assertEquals(MindModelManager.getMindModelManager()
 				.getMindProject(null), null);
 		String name = "P1_" + System.currentTimeMillis();
 		MindProject mp = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp);
 
 		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		assertEquals(MindModelManager.getMindModelManager().getMindProject(p),
 				mp);
 
 		assertEquals(MindIdeCore.get(p), mp);
 		assertEquals(MindIdeCore.get(null), null);
 
 		assertEquals(MindIdeCore.getResource(mp), p);
 		assertEquals(MindIdeCore.getResource((MindObject) mp), p);
 
 		assertEquals(mp.getResolvedMindpath(true), mp.getMindpathentries());
 		assertEquals(mp.getResolvedMindpath(false), mp.getMindpathentries());
 		assertEquals(1, mp.getResolvedMindpath(false).size());
 		assertEqualsMPE(MindIdeCore.newMPESource(p.getFolder("src")), mp
 				.getMindpathentries().get(0));
 
 	}
 	@Test
 	public void testCreateProject() throws UnsupportedEncodingException, CoreException {
 		String name = "P1_" + System.currentTimeMillis();
 		MindProject mp = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp);
 		assertMindProject(name);
 	}
 	
 	public static void assertMindProject(String name) throws CoreException {
 		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		
 		assertTrue(p.exists());
 		assertEquals(name, p.getName());
 		assertTrue(p.getFolder("src").exists());
 		assertTrue(p.getFolder("build").exists());
 		assertTrue(p.getFile(".cproject").exists());
 		assertTrue(p.getFile(".project").exists());
 		assertTrue(p.getFile("Makefile").exists());
 		
 		IProjectDescription desc = p.getDescription();
 		String[] natures = desc.getNatureIds();
 		assertTrue(	Arrays.asList(natures).contains(CProjectNature.C_NATURE_ID));
 		assertTrue(	Arrays.asList(natures).contains(MindNature.NATURE_ID));
 		
 		ICommand[] commands = desc.getBuildSpec();
 		assertEquals(3, commands.length);
 		// 1 : org.eclipse.cdt.managedbuilder.core.genmakebuilder
		// 2 : org.ow2.fractal.mind.cadse.builder
 		// 3 : org.eclipse.cdt.managedbuilder.core.ScannerConfigBuilder
 		assertEquals("org.eclipse.cdt.managedbuilder.core.genmakebuilder", commands[0].getBuilderName());
 		assertEquals(MindIdeBuilder.BUILDER_ID, commands[1].getBuilderName());
 		assertEquals("org.eclipse.cdt.managedbuilder.core.ScannerConfigBuilder", commands[2].getBuilderName());
 		
 		
 		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(p);
 		
 		assertEquals("make", info.getBuildCommand());
 		
 		IConfiguration configuration = info.getDefaultConfiguration();
 		assertEquals("Default", configuration.getName());
 		
		assertTrue(configuration.getId().startsWith("org.ow2.fractal.mind.build"));
 		assertEquals("make", configuration.getBuildCommand());
 		
 		CBuildData buildData = configuration.getBuildData();
 		ICOutputEntry[] outDir = buildData.getOutputDirectories();
 		
 		assertEquals(1,outDir.length);
 		assertEquals(p.getFolder("build").getFullPath(), outDir[0].getFullPath());
 		
 		assertEquals("${workspace_loc:/"+p.getName() + "}", configuration.getBuilder().getBuildPath());
 		assertEquals(p.getLocation(), configuration.getBuilder().getBuildLocation());
 		
 		ICSourceEntry[] srcEntries = configuration.getSourceEntries();
 		assertEquals(1,srcEntries.length);
 		assertEquals(p.getFolder("src").getFullPath(), srcEntries[0].getFullPath());
 	}
 	
 	@Test
 	public void testWriteFile() throws CoreException, IOException {
 		// create mindProject
 		String name = "P1_" + System.currentTimeMillis();
 		MindProject mp = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp);
 
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		MindPathEntry p2_import_p1 = MindIdeCore.newMPEImport("p1");
 		mp2.getMindpathentries().add(p2_import_p1);
 		assertNull(p2_import_p1.getResolvedBy());
 		assertNotNull(p2_import_p1.getOwnerProject());
 		assertEquals(mp2, p2_import_p1.getOwnerProject());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail save mpe",
 				FamilyJobCST.FAMILY_SAVE_MPE);
 
 		assertTrue(MindProjectImpl.areMindpathsEqual(mp2.getRawMinpath(),
 				MindProjectImpl.readFileEntriesWithException(mp2.getProject())));
 
 		MindPathEntry p2_use_p1 = MindIdeCore.newMPEProject(mp.getProject());
 		mp2.getMindpathentries().add(p2_use_p1);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail save mpe",
 				FamilyJobCST.FAMILY_SAVE_MPE);
 		assertNotNull(p2_use_p1.getResolvedBy());
 		assertNotNull(p2_use_p1.getOwnerProject());
 		assertEquals(mp2, p2_use_p1.getOwnerProject());
 		assertTrue(MindProjectImpl.areMindpathsEqual(mp2.getRawMinpath(),
 				MindProjectImpl.readFileEntriesWithException(mp2.getProject())));
 
 		IFolder src2 = mp2.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail save mpe",
 				FamilyJobCST.FAMILY_SAVE_MPE);
 		assertNotNull(p2_src2.getResolvedBy());
 		assertNotNull(p2_src2.getOwnerProject());
 		assertEquals(mp2, p2_src2.getOwnerProject());
 		assertTrue(MindProjectImpl.areMindpathsEqual(mp2.getRawMinpath(),
 				MindProjectImpl.readFileEntriesWithException(mp2.getProject())));
 
 		assertEquals(4, mp2.getMindpathentries().size());
 		assertEqualsMPE(p2_import_p1, mp2.getMindpathentries().get(1));
 		assertEqualsMPE(p2_use_p1, mp2.getMindpathentries().get(2));
 		assertEqualsMPE(p2_src2, mp2.getMindpathentries().get(3));
 
 		EList<MindPathEntry> mindpath = MindProjectImpl
 				.readFileEntriesWithException(mp2.getProject());
 		assertEquals(4, mindpath.size());
 		assertEqualsMPE(p2_import_p1, mindpath.get(1));
 		assertEqualsMPE(p2_use_p1, mindpath.get(2));
 		assertEqualsMPE(p2_src2, mindpath.get(3));
 	}
 
 	@Test
 	public void testSetMindpath() throws CoreException, IOException {
 		// create mindProject
 		String name;
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp);
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		// create import non resolu
 
 		MindPathEntry p2_import_p1 = MindIdeCore.newMPEImport("p1");
 		mp2.getMindpathentries().add(p2_import_p1);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail import",
 				FamilyJobCST.FAMILY_ALL);
 		assertNull(p2_import_p1.getResolvedBy());
 		assertNotNull(p2_import_p1.getOwnerProject());
 		assertEquals(mp2, p2_import_p1.getOwnerProject());
 
 		assertTrue(MindProjectImpl.areMindpathsEqual(mp2.getRawMinpath(),
 				MindProjectImpl.readFileEntriesWithException(mp2.getProject())));
 
 		assertEquals(2, mp2.getMindpathentries().size());
 		assertEquals(p2_import_p1, mp2.getMindpathentries().get(1));
 
 		// create package p1 and adl t
 		IFolder r_mind_p1 = mp.getProject().getFolder("src/p1");
 		r_mind_p1.create(false, true, new NullProgressMonitor());
 		IFile r_mind_adl = mp.getProject().getFile("src/p1/t.adl");
 		r_mind_adl.create(
 				new ByteArrayInputStream("primitive p1.t".getBytes()), true,
 				new NullProgressMonitor());
 
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		MindRootSrc mindRootSrc = mp.getRootsrcs().get(0);
 		assertEquals(1, mindRootSrc.getPackages().size());
 		MindPackage p1_package = mindRootSrc.getPackages().get(0);
 		assertNotNull(p1_package);
 		assertEquals("p1", p1_package.getName());
 		assertEquals(MindIdeCore.getResource((MindObject) p1_package),
 				r_mind_p1);
 		assertEquals(MindIdeCore.getResource((MindObject) mindRootSrc), mp
 				.getProject().getFolder("src"));
 
 		// create adl object
 		MindFile mindFile_ADL = p1_package.getFiles().get(0);
 		assertEquals("t", mindFile_ADL.getName());
 		assertEquals(MindidePackage.Literals.MIND_ADL, mindFile_ADL.eClass());
 		assertEquals(UtilMindIde.findFile(p1_package.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL), mindFile_ADL);
 		assertEquals(MindIdeCore.getResource((MindObject) mindFile_ADL),
 				r_mind_adl);
 
 		// create itf object
 		IFile r_mind_itf = mp.getProject().getFile("src/p1/zitf.itf");
 		r_mind_itf.create(new ByteArrayInputStream("interface p1.zitf"
 				.getBytes()), true, new NullProgressMonitor());
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		MindFile mindFile_ITF = p1_package.getFiles().get(1);
 		assertEquals("zitf", mindFile_ITF.getName());
 		assertEquals(MindidePackage.Literals.MIND_ITF, mindFile_ITF.eClass());
 		assertEquals(UtilMindIde.findFile(p1_package.getFiles(), "zitf",
 				MindidePackage.Literals.MIND_ITF), mindFile_ITF);
 		assertEquals(MindIdeCore.getResource((MindObject) mindFile_ITF),
 				r_mind_itf);
 
 		// create idf object
 		IFile r_mind_idf = mp.getProject().getFile("src/p1/zidf.idf");
 		r_mind_idf.create(new ByteArrayInputStream("interface p1.zitf"
 				.getBytes()), true, new NullProgressMonitor());
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		MindFile mindFile_IDF = p1_package.getFiles().get(2);
 		assertEquals("zidf", mindFile_IDF.getName());
 		assertEquals(MindidePackage.Literals.MIND_IDF, mindFile_IDF.eClass());
 		assertEquals(UtilMindIde.findFile(p1_package.getFiles(), "zidf",
 				MindidePackage.Literals.MIND_IDF), mindFile_IDF);
 		assertEquals(MindIdeCore.getResource((MindObject) mindFile_IDF),
 				r_mind_idf);
 
 		// create c object
 		IFile r_mind_c = mp.getProject().getFile("src/p1/zidf.c");
 		r_mind_c.create(
 				new ByteArrayInputStream("interface p1.zitf".getBytes()), true,
 				new NullProgressMonitor());
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		MindFile mindFile_C = p1_package.getFiles().get(3);
 		assertEquals("zidf", mindFile_C.getName());
 		assertEquals(MindidePackage.Literals.MIND_C, mindFile_C.eClass());
 		assertEquals(UtilMindIde.findFile(p1_package.getFiles(), "zidf",
 				MindidePackage.Literals.MIND_C), mindFile_C);
 		assertEquals(MindIdeCore.getResource((MindObject) mindFile_C), r_mind_c);
 
 		// create h object
 		IFile r_mind_h = mp.getProject().getFile("src/p1/zidf.h");
 		r_mind_h.create(
 				new ByteArrayInputStream("interface p1.zitf".getBytes()), true,
 				new NullProgressMonitor());
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		MindFile mindFile_H = p1_package.getFiles().get(4);
 		assertEquals("zidf", mindFile_H.getName());
 		assertEquals(MindidePackage.Literals.MIND_H, mindFile_H.eClass());
 		assertEquals(UtilMindIde.findFile(p1_package.getFiles(), "zidf",
 				MindidePackage.Literals.MIND_H), mindFile_H);
 		assertEquals(MindIdeCore.getResource((MindObject) mindFile_H), r_mind_h);
 
 		// import package must be resolved
 		assertNotNull(p2_import_p1.getResolvedBy());
 
 		// resolve adl and idl
 		assertEquals(mp.resolveAdl("t", "p1", new BasicEList<String>()),
 				mindFile_ADL);
 		assertEquals(mp.resolveAdl("t", "other_p1", new BasicEList<String>()),
 				null);
 		assertEquals(mp.resolveAdl("t", "other_p1", importsArray("p1.t")),
 				mindFile_ADL);
 		assertEquals(mp.resolveAdl("t", "other_p1", importsArray("p1.*")),
 				mindFile_ADL);
 
 		assertEquals(mp2.resolveAdl("t", "p1", new BasicEList<String>()),
 				mindFile_ADL);
 		assertEquals(mp2.resolveAdl("t", "other_p1", new BasicEList<String>()),
 				null);
 		assertEquals(mp.resolveAdl("t", "other_p1", importsArray("p1.t")),
 				mindFile_ADL);
 		assertEquals(mp.resolveAdl("t", "other_p1", importsArray("p1.*")),
 				mindFile_ADL);
 
 		// delete package import not resolved
 		r_mind_p1.delete(false, new NullProgressMonitor());
 		mp.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		//
 		assertEquals(0, mindRootSrc.getPackages().size());
 		assertNull(p2_import_p1.getResolvedBy());
 
 	}
 
 	private EList<String> importsArray(String... i) {
 		BasicEList<String> ret = new BasicEList<String>();
 		ret.addAll(Arrays.asList(i));
 		return ret;
 	}
 
 	@Test(timeout = 10000)
 	public void testCloseProject() throws CoreException, IOException {
 		System.out.println("BEGIN TEST testCloseProject");
 		String name;
 
 		int sizeRootSrc = MindIdeCore.getModel().getWSRepo().getRootsrcs()
 				.size();
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		IProject project = mp2.getProject();
 		IFolder src2 = project.getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail create",
 				FamilyJobCST.FAMILY_ALL);
 
 		project.close(new NullProgressMonitor());
 
 		while (MindIdeCore.get(project) != null) {
 			// wait listener
 		}
 		// root src delete ?
 
 		assertEquals(sizeRootSrc, MindIdeCore.getModel().getWSRepo()
 				.getRootsrcs().size());
 		System.out.println("END TEST testCloseProject");
 
 	}
 
 	@Test
 	public void testDeleteProject() throws CoreException, IOException {
 		System.out.println("BEGIN TEST testDeleteProject");
 		String name;
 
 		int sizeRootSrc = MindIdeCore.getModel().getWSRepo().getRootsrcs()
 				.size();
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		IProject project = mp2.getProject();
 		IFolder src2 = project.getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail create",
 				FamilyJobCST.FAMILY_ALL);
 
 		project.delete(false, new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail delete",
 				FamilyJobCST.FAMILY_ALL);
 
 		assertNull(MindIdeCore.get(project));
 		// root src delete ?
 
 		assertEquals(sizeRootSrc, MindIdeCore.getModel().getWSRepo()
 				.getRootsrcs().size());
 		System.out.println("END TEST testDeleteProject");
 	}
 
 	@Test
 	public void testDelete() throws CoreException, IOException {
 		String name;
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		IFolder src2 = mp2.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 
 		// delete rootsrc
 		assertEquals(2, mp2.getRootsrcs().size());
 		MindRootSrc mindRootSrc2 = mp2.getRootsrcs().get(1);
 		assertEquals(src2.getFullPath().toPortableString(), mindRootSrc2
 				.getFullpath());
 		assertEquals(src2.getFullPath().toPortableString(), mindRootSrc2
 				.getName());
 
 		src2.delete(true, new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		assertNull(mindRootSrc2.getRepo());
 		assertNull(mindRootSrc2.getProject());
 		assertNull(p2_src2.getResolvedBy());
 
 		assertEquals(1, mp2.getRootsrcs().size());
 		IFolder src = mp2.getProject().getFolder("src");
 		MindRootSrc mindRootSrc = mp2.getRootsrcs().get(0);
 		assertEquals(src.getFullPath().toPortableString(), mindRootSrc
 				.getFullpath());
 		assertEquals(src.getFullPath().toPortableString(), mindRootSrc
 				.getName());
 
 		assertEquals(2, mp2.getMindpathentries().size());
 
 		// create two packages p1 and p2
 		// create two component in each package
 		mp2.getProject().getFolder("src/p1").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p1/t.adl").create(
 				new ByteArrayInputStream("primitive p1.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p1/z.adl").create(
 				new ByteArrayInputStream("primitive p1.z".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFolder("src/p2").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p2/t.adl").create(
 				new ByteArrayInputStream("primitive p2.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p2/z.adl").create(
 				new ByteArrayInputStream("primitive p2.z".getBytes()), true,
 				new NullProgressMonitor());
 
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		assertEquals(2, mindRootSrc.getPackages().size());
 		MindPackage p1 = UtilMindIde.find(mindRootSrc.getPackages(), "p1");
 		assertNotNull(p1);
 		assertEquals(2, p1.getFiles().size());
 
 		MindFile p1_t = UtilMindIde.findFile(p1.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p1_t);
 		MindFile p1_z = UtilMindIde.findFile(p1.getFiles(), "z",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p1_z);
 
 		MindPackage p2 = UtilMindIde.find(mindRootSrc.getPackages(), "p2");
 		assertNotNull(p2);
 		assertEquals(2, p2.getFiles().size());
 
 		MindFile p2_t = UtilMindIde.findFile(p2.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_t);
 		MindFile p2_z = UtilMindIde.findFile(p2.getFiles(), "z",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_z);
 
 		// delete component p1.z
 		mp2.getProject().getFile("src/p1/z.adl").delete(true,
 				new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		assertEquals(2, mindRootSrc.getPackages().size());
 		p1 = UtilMindIde.find(mindRootSrc.getPackages(), "p1");
 		assertNotNull(p1);
 		assertEquals(1, p1.getFiles().size());
 
 		p1_t = UtilMindIde.findFile(p1.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p1_t);
 		p1_z = UtilMindIde.findFile(p1.getFiles(), "z",
 				MindidePackage.Literals.MIND_ADL);
 		assertNull(p1_z);
 
 		p2 = UtilMindIde.find(mindRootSrc.getPackages(), "p2");
 		assertNotNull(p2);
 		assertEquals(2, p2.getFiles().size());
 
 		p2_t = UtilMindIde.findFile(p2.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_t);
 		p2_z = UtilMindIde.findFile(p2.getFiles(), "z",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_z);
 
 		// delete component p1.t
 		mp2.getProject().getFile("src/p1/t.adl").delete(true,
 				new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		assertEquals(2, mindRootSrc.getPackages().size());
 		p1 = UtilMindIde.find(mindRootSrc.getPackages(), "p1");
 		assertNotNull(p1);
 
 		p2 = UtilMindIde.find(mindRootSrc.getPackages(), "p2");
 		assertNotNull(p2);
 		assertEquals(2, p2.getFiles().size());
 
 		p2_t = UtilMindIde.findFile(p2.getFiles(), "t",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_t);
 		p2_z = UtilMindIde.findFile(p2.getFiles(), "z",
 				MindidePackage.Literals.MIND_ADL);
 		assertNotNull(p2_z);
 
 		// delete package p2
 		mp2.getProject().getFolder("src/p2").delete(true,
 				new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		assertEquals(1, mindRootSrc.getPackages().size());
 		p1 = UtilMindIde.find(mindRootSrc.getPackages(), "p1");
 		assertNotNull(p1);
 
 		p2 = UtilMindIde.find(mindRootSrc.getPackages(), "p2");
 		assertNull(p2);
 
 	}
 
 	@Test
 	public void testResolve() throws CoreException, IOException {
 		String name;
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		name = "P3_" + System.currentTimeMillis();
 		MindProject mp3 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp3);
 
 		IFolder src2 = mp2.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 
 		// create two packages p1 and p2
 		// create two component in each package
 		mp2.getProject().getFolder("src/p1").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p1/t.adl").create(
 				new ByteArrayInputStream("primitive p1.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p1/z.adl").create(
 				new ByteArrayInputStream("primitive p1.z".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFolder("src/p2").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p2/t.adl").create(
 				new ByteArrayInputStream("primitive p2.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/p2/z.adl").create(
 				new ByteArrayInputStream("primitive p2.z".getBytes()), true,
 				new NullProgressMonitor());
 
 		// create two packages p3 and p4
 		// create two component in each package
 		mp1.getProject().getFolder("src/p3").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p3/t.adl").create(
 				new ByteArrayInputStream("primitive p3.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p3/i.adl").create(
 				new ByteArrayInputStream("primitive p3.i".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFolder("src/p4").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p4/z.adl").create(
 				new ByteArrayInputStream("primitive p4.z".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p4/u.adl").create(
 				new ByteArrayInputStream("primitive p4.u".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p4/r.adl").create(
 				new ByteArrayInputStream("primitive p4.r".getBytes()), true,
 				new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		mp1.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		MindPackage p2_p1 = UtilMindIde.find(mp2.getRootsrcs().get(0)
 				.getPackages(), "p1");
 		MindAdl p2_p1_t = UtilMindIde.findAdl(p2_p1.getFiles(), "t");
 		MindAdl p2_p1_z = UtilMindIde.findAdl(p2_p1.getFiles(), "z");
 
 		MindPackage p2_p2 = UtilMindIde.find(mp2.getRootsrcs().get(0)
 				.getPackages(), "p2");
 		MindAdl p2_p2_t = UtilMindIde.findAdl(p2_p2.getFiles(), "t");
 		MindAdl p2_p2_z = UtilMindIde.findAdl(p2_p2.getFiles(), "z");
 
 		MindPackage p1_p3 = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "p3");
 		MindAdl p1_p3_t = UtilMindIde.findAdl(p1_p3.getFiles(), "t");
 		MindAdl p1_p3_i = UtilMindIde.findAdl(p1_p3.getFiles(), "i");
 
 		MindPackage p1_p4 = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "p4");
 		MindAdl p1_p4_z = UtilMindIde.findAdl(p1_p4.getFiles(), "z");
 
 		EList<MindAdl> mp1_t_in_ws = mp1.resolvePossibleAdlInWorkspace("t");
 		EList<MindAdl> mp2_t_in_ws = mp2.resolvePossibleAdlInWorkspace("t");
 		assertEquals(mp1_t_in_ws, mp2_t_in_ws);
 
 		assertList(3, mp1_t_in_ws, l(p2_p1_z, p2_p2_z, p1_p3_i, p1_p4_z), l(
 				p2_p1_t, p2_p2_t, p1_p3_t));
 
 		EList<MindAdl> mp1_a_in_ws = mp1.resolvePossibleAdlInWorkspace("a");
 		EList<MindAdl> mp2_a_in_ws = mp2.resolvePossibleAdlInWorkspace("a");
 		assertEquals(mp1_a_in_ws, mp2_a_in_ws);
 
 		assertEquals(0, mp1_a_in_ws.size());
 
 		EList<MindAdl> mp1_z_in_ws = mp1.resolvePossibleAdlInWorkspace("z");
 		EList<MindAdl> mp2_z_in_ws = mp2.resolvePossibleAdlInWorkspace("z");
 		assertEquals(mp1_z_in_ws, mp2_z_in_ws);
 
 		assertList(3, mp1_z_in_ws, l(p2_p1_t, p2_p2_t, p1_p3_i, p1_p3_t), l(
 				p2_p1_z, p2_p2_z, p1_p4_z));
 
 		EList<MindAdl> mp1_i_in_ws = mp1.resolvePossibleAdlInWorkspace("i");
 		EList<MindAdl> mp2_i_in_ws = mp2.resolvePossibleAdlInWorkspace("i");
 		assertEquals(mp1_i_in_ws, mp2_i_in_ws);
 		assertList(1, mp1_i_in_ws, l(p2_p1_t, p2_p1_z, p2_p2_t, p2_p2_z,
 				p1_p3_t, p1_p4_z), l(p1_p3_i));
 
 		// -----
 
 		EList<MindAdl> mp1_t_in_path = mp1.resolvePossibleAdlInMindPath("t");
 		EList<MindAdl> mp2_t_in_path = mp2.resolvePossibleAdlInMindPath("t");
 		assertNotSame(mp1_t_in_path, mp2_t_in_path);
 		assertList(2, mp2_t_in_path, l(p2_p1_z, p2_p2_z, p1_p3_t, p1_p4_z,
 				p1_p3_i), l(p2_p1_t, p2_p2_t));
 		assertList(1, mp1_t_in_path, l(p2_p1_z, p2_p1_t, p2_p2_t, p2_p2_z,
 				p1_p4_z, p1_p3_i), l(p1_p3_t));
 
 		EList<MindAdl> mp1_a_in_path = mp1.resolvePossibleAdlInMindPath("a");
 		EList<MindAdl> mp2_a_in_path = mp2.resolvePossibleAdlInMindPath("a");
 		assertEquals(mp1_a_in_path, mp2_a_in_path);
 		assertEquals(0, mp1_a_in_path.size());
 
 		EList<MindAdl> mp1_z_in_path = mp1.resolvePossibleAdlInMindPath("z");
 		EList<MindAdl> mp2_z_in_path = mp2.resolvePossibleAdlInMindPath("z");
 		assertNotSame(mp1_z_in_path, mp2_z_in_path);
 		assertList(2, mp2_z_in_path, l(p2_p1_t, p2_p2_t, p1_p3_t, p1_p4_z,
 				p1_p3_i), l(p2_p1_z, p2_p2_z));
 		assertList(1, mp1_z_in_path, l(p2_p1_t, p2_p2_z, p2_p2_t, p2_p2_z,
 				p1_p3_t, p1_p3_i), l(p1_p4_z));
 
 		EList<MindAdl> mp1_i_in_path = mp1.resolvePossibleAdlInMindPath("i");
 		EList<MindAdl> mp2_i_in_path = mp2.resolvePossibleAdlInMindPath("i");
 		assertNotSame(mp1_i_in_path, mp2_i_in_path);
 		assertList(0, mp2_i_in_path, null, null);
 		assertList(1, mp1_i_in_path, l(p2_p1_t, p2_p2_z, p2_p2_t, p2_p2_z,
 				p1_p3_t, p1_p4_z), l(p1_p3_i));
 
 		// resolve qualified name
 		assertEquals(p2_p1_t, mp2.resolveAdl("p1.t", "qf", importsArray()));
 		assertEquals(p2_p1_t, mp2.resolveAdl("p1.t", "p1", importsArray()));
 		assertEquals(p1_p3_i, mp1.resolveAdl("p3.i", "p1", importsArray()));
 		assertEquals(p1_p3_i, mp1.resolveAdl("p3.i", "p3", importsArray()));
 		assertEquals(null, mp1.resolveAdl("p3.ez", "p3", importsArray()));
 		assertEquals(null, mp1.resolveAdl("p3e.i", "p3", importsArray()));
 		assertEquals(null, mp1.resolveAdl("p3.ez", "pea3", importsArray()));
 		assertEquals(null, mp1.resolveAdl("p3e.i", "pea3", importsArray()));
 
 		// resolve in default package
 		mp1.getProject().getFile("src/r.adl").create(
 				new ByteArrayInputStream("primitive r".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/i.adl").create(
 				new ByteArrayInputStream("primitive i".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 
 		MindPackage p1_p_default = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "");
 		MindAdl p1_p_default_r = UtilMindIde.findAdl(p1_p_default.getFiles(),
 				"r");
 		MindAdl p1_p_default_i = UtilMindIde.findAdl(p1_p_default.getFiles(),
 				"i");
 		assertEquals(p1_p_default_r, mp1.resolveAdl("r", "qf", importsArray()));
 		assertEquals(p1_p3_i, mp1.resolveAdl("i", "p1", importsArray("p3.i")));
 		assertEquals(p1_p_default_i, mp1.resolveAdl("i", "p1", importsArray()));
 
 		// resovle package
 		EList<MindAdl> mp1_p1 = mp2.resolvePossibleAdlInPackage("p1");
 		assertList(2, mp1_p1, null, l(p2_p1_t, p2_p1_z));
 		EList<MindAdl> mp1_p8 = mp1.resolvePossibleAdlInPackage("p8");
 		assertList(0, mp1_p8, null, null);
 	}
 	
 	@Test
 	public void testResolveXXP() throws CoreException, IOException {
 		String name;
 		
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		// create two packages p1 and p2
 		// create two component in each package
 		mp1.getProject().getFolder("src/p1").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/p1/t.adl").create(
 				new ByteArrayInputStream("composite p1.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/t.adl").create(
 				new ByteArrayInputStream("composite t".getBytes()), true,
 				new NullProgressMonitor());
 		
 		sleep(80);
 		MindPackage p1_p1 = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "p1");
 		assertNotNull(p1_p1);
 		MindAdl p1_p1_t = UtilMindIde.findAdl(p1_p1.getFiles(), "t");
 		assertNotNull(p1_p1_t);
 		
 		MindPackage p1_p_default = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "");
 		assertNotNull(p1_p_default);
 		MindAdl p1_pdefault_t = UtilMindIde.findAdl(p1_p_default.getFiles(), "t");
 		assertNotNull(p1_pdefault_t);
 		
 
 		EList<MindAdl> mp1_t_in_ws = mp1.resolvePossibleAdlInWorkspace("t");
 		assertList(2, mp1_t_in_ws, null, l(p1_p1_t, p1_pdefault_t));
 
 		assertEquals(p1_p1_t, mp1.resolveAdl("t", "p1", importsArray()));
 		assertEquals(p1_pdefault_t, mp1.resolveAdl("t", "", importsArray()));
 		
 		assertEquals("p1.t", p1_p1_t.getQualifiedName());
 		assertEquals("t", p1_pdefault_t.getQualifiedName());
 		
 	}
 
 	/**
 	 * P1 src testrd_p3 t.adl z.adl src2 testrd_p4 z.adl u.adl r.adl
 	 * 
 	 * P2 import du package testrd_p4 test import resolved delete (src2)
 	 * 
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	@Test
 	public void testResolveAndDelete() throws CoreException, IOException {
 		String name;
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		IFolder src2 = mp1.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p1_src2 = MindIdeCore.newMPESource(src2);
 		mp1.getMindpathentries().add(p1_src2);
 
 		assertEquals(2, mp1.getRootsrcs().size());
 		// create two packages p3 and p4
 		// create two component in each package
 		mp1.getProject().getFolder("src/testrd_p3").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/t.adl").create(
 				new ByteArrayInputStream("primitive p3.t".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/i.adl").create(
 				new ByteArrayInputStream("primitive p3.i".getBytes()), true,
 				new NullProgressMonitor());
 
 		mp1.getProject().getFolder("src2/testrd_p4").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src2/testrd_p4/z.adl").create(
 				new ByteArrayInputStream("primitive p4.z".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src2/testrd_p4/u.adl").create(
 				new ByteArrayInputStream("primitive p4.u".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src2/testrd_p4/r.adl").create(
 				new ByteArrayInputStream("primitive p4.r".getBytes()), true,
 				new NullProgressMonitor());
 
 		MindPathEntry newMPEImport = MindIdeCore.newMPEImport("testrd_p4");
 		mp2.getMindpathentries().add(newMPEImport);
 		sleep(30);
 		assertNotNull(newMPEImport.getResolvedBy());
 		assertNotNull(newMPEImport.getOwnerProject());
 		MindAdl p4_r = mp2.findQualifiedComponent("testrd_p4.r");
 		assertNotNull(p4_r);
 
 		src2.delete(false, new NullProgressMonitor());
 
 		assertEquals(1, mp1.getRootsrcs().size());
 		assertNull(newMPEImport.getResolvedBy());
 		p4_r = mp2.findQualifiedComponent("testrd_p4.r");
 		assertNull(p4_r);
 
 		mp1.getProject().delete(true, null);
 		mp2.getProject().delete(true, null);
 	}
 
 	@Test
 	public void testResolveDependenciesImport() throws CoreException,
 			IOException {
 		String name;
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		name = "P3_" + System.currentTimeMillis();
 		MindProject mp3 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp3);
 
 		IFolder src2 = mp2.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p2_src2 = MindIdeCore.newMPESource(src2);
 		mp2.getMindpathentries().add(p2_src2);
 
 		// create two packages p1 and p2
 		// create two component in each package
 		mp2.getProject().getFolder("src/testrd_p1").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/testrd_p1/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p1.t".getBytes()),
 				true, new NullProgressMonitor());
 		mp2.getProject().getFile("src/testrd_p1/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p1.z".getBytes()),
 				true, new NullProgressMonitor());
 		mp2.getProject().getFolder("src/testrd_p2").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src/testrd_p2/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p2.t".getBytes()),
 				true, new NullProgressMonitor());
 		mp2.getProject().getFile("src/testrd_p2/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p2.z".getBytes()),
 				true, new NullProgressMonitor());
 
 		mp2.getProject().getFolder("src2/testrd_p5").create(false, true,
 				new NullProgressMonitor());
 		mp2.getProject().getFile("src2/testrd_p5/i.adl").create(
 				new ByteArrayInputStream("primitive testrd_p5.i".getBytes()),
 				true, new NullProgressMonitor());
 		mp2.getProject().getFile("src2/testrd_p5/r.adl").create(
 				new ByteArrayInputStream("primitive testrd_p5.r".getBytes()),
 				true, new NullProgressMonitor());
 
 		// create two packages p3 and p4
 		// create two component in each package
 		mp1.getProject().getFolder("src/testrd_p3").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p3.t".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/i.adl").create(
 				new ByteArrayInputStream("primitive testrd_p3.i".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFolder("src/testrd_p4").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.z".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/u.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.u".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/r.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.r".getBytes()),
 				true, new NullProgressMonitor());
 		mp2.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		mp1.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
 				new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail wait",
 				FamilyJobCST.FAMILY_ALL);
 
 		MindPackage p2_p1 = UtilMindIde.find(mp2.getRootsrcs().get(0)
 				.getPackages(), "testrd_p1");
 		MindAdl p2_p1_t = UtilMindIde.findAdl(p2_p1.getFiles(), "t");
 		MindAdl p2_p1_z = UtilMindIde.findAdl(p2_p1.getFiles(), "z");
 
 		MindPackage p2_p2 = UtilMindIde.find(mp2.getRootsrcs().get(0)
 				.getPackages(), "testrd_p2");
 		MindAdl p2_p2_t = UtilMindIde.findAdl(p2_p2.getFiles(), "t");
 		MindAdl p2_p2_z = UtilMindIde.findAdl(p2_p2.getFiles(), "z");
 
 		MindPackage p2_p5_src2 = UtilMindIde.find(mp2.getRootsrcs().get(1)
 				.getPackages(), "testrd_p5");
 		MindAdl p2_p5_src2_i = UtilMindIde.findAdl(p2_p5_src2.getFiles(), "i");
 		MindAdl p2_p5_src2_r = UtilMindIde.findAdl(p2_p5_src2.getFiles(), "r");
 
 		MindPackage p1_p3 = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "testrd_p3");
 		MindAdl p1_p3_t = UtilMindIde.findAdl(p1_p3.getFiles(), "t");
 		MindAdl p1_p3_i = UtilMindIde.findAdl(p1_p3.getFiles(), "i");
 
 		MindPackage p1_p4 = UtilMindIde.find(mp1.getRootsrcs().get(0)
 				.getPackages(), "testrd_p4");
 		MindAdl p1_p4_z = UtilMindIde.findAdl(p1_p4.getFiles(), "z");
 
 		mp3.getMindpathentries().add(MindIdeCore.newMPEImport("testrd_p2"));
 		mp3.getMindpathentries().add(MindIdeCore.newMPEImport("testrd_p3"));
 		EList<MindAdl> mp3_i_in_path = mp3.resolvePossibleAdlInMindPath("i");
 
 		assertList(1, mp3_i_in_path, null, l(p1_p3_i));
 		mp3.getMindpathentries().add(MindIdeCore.newMPEImport("testrd_p5"));
 		mp3_i_in_path = mp3.resolvePossibleAdlInMindPath("i");
 
 		assertList(2, mp3_i_in_path, null, l(p1_p3_i, p2_p5_src2_i));
 		EList<MindPathEntry> dd = mp3.getRawMinpath();
 		dd.remove(MindIdeCore.newMPEImport("testrd_p3"));
 		assertEquals(3, dd.size());
 		mp3.setMindpath(dd);
 
 		sleep(30);
 		
 		assertEquals(3, mp3.getMindpathentries().size());
 		mp3_i_in_path = mp3.resolvePossibleAdlInMindPath("i");
 		assertList(1, mp3_i_in_path, null, l(p2_p5_src2_i));
 
 	}
 
 	private void sleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	/**
 	 * 
 	 * <ol>
 	 * <li>create mpe source ==> create root src and mpe is resolved</li>
 	 * <li>delete mpe source ==> delete root src and mpe not reolved</li>
 	 * <li>delete folder     ==> delete root src and mpe not reolved ?</li>
 	 * <li>recreate folder deleted folder (pointed by a valid mpe) => mpe resolved & create root src</li>
 	 * </ol>
 	 * 
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	@Test
 	public void testMPESource() throws CoreException, IOException {
 		System.out.println("BEGIN TEST testSourceC");
 		String name;
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		// 1 create mpe source ==> create root src and mpe is resolved
 		IFolder src = mp1.getProject().getFolder("src");
 
 		IFolder src2 = mp1.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p1_src2 = MindIdeCore.newMPESource(src2);
 		mp1.getMindpathentries().add(p1_src2);
 		
 		MindRootSrc rootSrc2 = UtilMindIde.findRootSrc(mp1.getRepo(), src2.getFullPath());
 		assertNotNull(rootSrc2);
 		assertNotNull(p1_src2.getResolvedBy());
 		assertEquals(rootSrc2, p1_src2.getResolvedBy());
 
 		// 2 delete mpe source ==> delete root src and mpe not reolved
 		mp1.getMindpathentries().remove(p1_src2);
 		rootSrc2 = UtilMindIde.findRootSrc(mp1.getRepo(), src2.getFullPath());
 		assertNull(rootSrc2);
 		assertNull(p1_src2.getResolvedBy());
 
 		// 3 delete folder     ==> delete root src and mpe not reolved
 		IFolder src3 = mp1.getProject().getFolder("src3");
 		src3.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p1_src3 = MindIdeCore.newMPESource(src3);
 		mp1.getMindpathentries().add(p1_src3);
 		
 		sleep(30);
 		MindRootSrc rootSrc3 = UtilMindIde.findRootSrc(mp1.getRepo(), src3.getFullPath());
 		assertNotNull(rootSrc3);
 		assertNotNull(p1_src3.getResolvedBy());
 		assertEquals(rootSrc3, p1_src3.getResolvedBy());
 		
 		src3.delete(true,  new NullProgressMonitor());
 		sleep(30);
 		rootSrc3 = UtilMindIde.findRootSrc(mp1.getRepo(), src3.getFullPath());
 		assertNull(rootSrc3);
 		assertNull(p1_src3.getResolvedBy());
 		assertNotNull(p1_src3.getOwnerProject());
 
 		//4 recreate folder src3
 		src3.create(false, true, new NullProgressMonitor());
 		sleep(30);
 		rootSrc3 = UtilMindIde.findRootSrc(mp1.getRepo(), src3.getFullPath());
 		assertNotNull(rootSrc3);
 		assertNotNull(p1_src3.getResolvedBy());
 		assertEquals(rootSrc3, p1_src3.getResolvedBy());
 	}
 
 
 	/**
 	 * 
 	 * <ol>
 	 * <li>create resolved mpe source ==> create c source folder</li>
 	 * <li>create c source folder ==> create mpe source (resolved)</li>
 	 * <li>delete mpe source ==> delete csource, folder exit ?</li>
 	 * <li>delete src source ==> delete csourc folder, not delete mpe</li>
 	 * <li>create src source ==> resolved mpe, create c source folder (if mpe
 	 * corresponding)</li>
 	 * </ol>
 	 * 
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	@Test()
 	public void testSourceC() throws CoreException, IOException {
 		System.out.println("BEGIN TEST testSourceC");
 		String name;
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		// 1 create resolved mpe source ==> create c source folder
 		IFolder src = mp1.getProject().getFolder("src");
 
 		IFolder src2 = mp1.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p1_src2 = MindIdeCore.newMPESource(src2);
 		mp1.getMindpathentries().add(p1_src2);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail createCSource",
 				FamilyJobCST.FAMILY_CREATE_CSOURCE_FOLDER);
 		assertTrue(src.exists());
 		assertTrue(src2.exists());
 		assertCSource(src);
 		assertCSource(src2);
 
 		// 2 create c source folder ==> create mpe source (resolved)
 		IFolder src3 = mp1.getProject().getFolder("src3");
 		src3.create(false, true, new NullProgressMonitor());
 
 		CDTUtil.createCSourceFolder(src3);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail createCSource",
 				FamilyJobCST.FAMILY_CREATE_CSOURCE_FOLDER);
 
 		MindPathEntry p1_src3 = MindIdeCore.newMPESource(src3);
 		MindPathEntry p1_src3_resolved = getContainsMPE(mp1, p1_src3); // time
 		// out
 		assertNotNull(p1_src3_resolved.getResolvedBy());
 		assertTrue(p1_src3_resolved.getResolvedBy() instanceof MindRootSrc);
 		MindRootSrc rs = (MindRootSrc) p1_src3_resolved.getResolvedBy();
 		assertEquals(p1_src3.getName(), rs.getFullpath());
 		assertEquals(p1_src3_resolved.getOwnerProject(), rs.getProject());
 
 		// 3 delete mpe source ==> delete csource, folder exit ?
 		assertTrue(src2.exists());
 		mp1.getMindpathentries().remove(p1_src2);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail createCSource",
 				FamilyJobCST.FAMILY_REMOVE_CSOURCE_FOLDER);
 		assertNotCSource(src2);
 		assertTrue(!mp1.getMindpathentries().contains(p1_src2));
 		assertTrue(!src2.exists());
 		
 		// 4 delete src source ==> delete csourc folder, not delete mpe
 		src3.delete(true, new NullProgressMonitor());
 		p1_src3_resolved = getContainsMPE(mp1, p1_src3); // time out
 		assertNull(p1_src3_resolved.getResolvedBy());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail createCSource",
 				FamilyJobCST.FAMILY_REMOVE_CSOURCE_FOLDER);
 		assertNotCSource(src3);
 
 		// 5 create src source ==> resolved mpe, create c source folder (if mpe
 		// corresponding)
 		src3.create(false, true, new NullProgressMonitor());
 		p1_src3_resolved = getContainsMPE(mp1, p1_src3); // time out
 		assertNotNull(p1_src3_resolved.getResolvedBy());
 		assertTrue(p1_src3_resolved.getResolvedBy() instanceof MindRootSrc);
 		rs = (MindRootSrc) p1_src3_resolved.getResolvedBy();
 		assertEquals(p1_src3.getName(), rs.getFullpath());
 		assertEquals(p1_src3_resolved.getOwnerProject(), rs.getProject());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail createCSource",
 				FamilyJobCST.FAMILY_CREATE_CSOURCE_FOLDER);
 		assertCSource(src3);
 
 	}
 
 	private void waitJob(long timeout, long sleep, String message, Object family) {
 		long end = System.currentTimeMillis() + timeout;
 		while (System.currentTimeMillis() < end) {
 			try {
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 				fail(message);
 			}
 			if (Job.getJobManager().find(family).length == 0)
 				return;
 		}
 		fail(message);
 	}
 
 	private MindPathEntry getContainsMPE(MindProject mp1, MindPathEntry findMpe) {
 		for (MindPathEntry mpe : mp1.getMindpathentries()) {
 			if (mpe.equals(findMpe)) {
 				return mpe;
 			}
 		}
 		fail("Not found " + findMpe);
 		return null;
 	}
 
 	private void assertCSource(IFolder src) {
 		IProject project = src.getProject();
 		ICProjectDescriptionManager mgr = CoreModel.getDefault()
 				.getProjectDescriptionManager();
 		ICProjectDescription des = mgr.getProjectDescription(project, true);
 		ICConfigurationDescription config = des
 				.getConfigurationByName("Default");
 		for (ICSourceEntry icSourceEntry : config.getSourceEntries()) {
 			IFolder icSrc = ResourcesPlugin.getWorkspace().getRoot().getFolder(
 					icSourceEntry.getFullPath());
 			if (icSrc.equals(src))
 				return;
 		}
 		fail("Not found c source " + src);
 	}
 
 	private void assertNotCSource(IFolder src) {
 		IProject project = src.getProject();
 		ICProjectDescriptionManager mgr = CoreModel.getDefault()
 				.getProjectDescriptionManager();
 		ICProjectDescription des = mgr.getProjectDescription(project, true);
 		ICConfigurationDescription config = des
 				.getConfigurationByName("Default");
 		for (ICSourceEntry icSourceEntry : config.getSourceEntries()) {
 			IFolder icSrc = ResourcesPlugin.getWorkspace().getRoot().getFolder(
 					icSourceEntry.getFullPath());
 			if (icSrc.equals(src))
 				fail("Found c source " + src);
 		}
 	}
 
 	@Test
 	public void testAdaptable() throws CoreException, IOException {
 		String name;
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 
 		IFolder src = mp1.getProject().getFolder("src");
 
 		IFolder src2 = mp1.getProject().getFolder("src2");
 		src2.create(false, true, new NullProgressMonitor());
 
 		MindPathEntry p1_src2 = MindIdeCore.newMPESource(src2);
 		mp1.getMindpathentries().add(p1_src2);
 
 		assertEquals(2, mp1.getRootsrcs().size());
 		// create two packages p3 and p4
 		// create two component in each package
 		IFolder package_p3 = mp1.getProject().getFolder("src/p3");
 		package_p3.create(false, true, new NullProgressMonitor());
 		IFile p3_t = mp1.getProject().getFile("src/p3/t.adl");
 		p3_t.create(new ByteArrayInputStream("primitive p3.t".getBytes()),
 				true, new NullProgressMonitor());
 		IFile p3_i = mp1.getProject().getFile("src/p3/i.adl");
 		p3_i.create(new ByteArrayInputStream("primitive p3.i".getBytes()),
 				true, new NullProgressMonitor());
 
 		IFolder package_p4 = mp1.getProject().getFolder("src2/p4");
 		package_p4.create(false, true, new NullProgressMonitor());
 		mp1.getProject().getFile("src2/p4/z.adl").create(
 				new ByteArrayInputStream("primitive p4.z".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src2/p4/u.adl").create(
 				new ByteArrayInputStream("primitive p4.u".getBytes()), true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src2/p4/r.adl").create(
 				new ByteArrayInputStream("primitive p4.r".getBytes()), true,
 				new NullProgressMonitor());
 
 		assertAdaptable(mp1, IProject.class, mp1.getProject());
 		assertAdaptable(mp1.getProject(), MindProject.class, mp1);
 		assertAdaptable(p1_src2.getResolvedBy(), IFolder.class, src2);
 		assertAdaptable(src2, MindRootSrc.class, (MindRootSrc) p1_src2
 				.getResolvedBy());
 		MindAdl mind_p3_t = mp1.findQualifiedComponent("p3.t");
 		assertNotNull(mind_p3_t);
 		assertAdaptable(mind_p3_t, IFile.class, p3_t);
 		assertAdaptable(p3_t, MindAdl.class, mind_p3_t);
 
 		assertAdaptable(mind_p3_t.getPackage(), IFolder.class, package_p3);
 		assertAdaptable(package_p3, MindPackage.class, mind_p3_t.getPackage());
 
 	}
 
 	static <T> void assertAdaptable(Object src, Class<T> adapter, T result) {
 		T r;
 		if (src instanceof IAdaptable) {
 			r = (T) ((IAdaptable) src).getAdapter(adapter);
 		} else {
 			r = (T) Platform.getAdapterManager().getAdapter(src, adapter);
 		}
 		assertNotNull(r);
 		assertEquals(result, r);
 	}
 
 	static <T> void assertList(int size, List<T> actual, T[] notContains,
 			T[] contains) {
 		assertEquals(size, actual.size());
 		if (contains != null)
 			for (int i = 0; i < contains.length; i++) {
 				assertTrue("not containts " + contains[i], actual
 						.contains(contains[i]));
 			}
 		if (notContains != null)
 			for (int i = 0; i < notContains.length; i++) {
 				assertTrue("containts " + notContains[i], !actual
 						.contains(notContains[i]));
 			}
 	}
 
 	static <T> T[] l(T... l) {
 		return l;
 	}
 
 	@Test
 	public void testMakeFileVarCOMP() throws CoreException, IOException {
 		String name;
 
 		name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_ALL);
 
 		assertEquals("", getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		// create two packages p1 and p2
 		// create two component in each package
 		mp1.getProject().getFolder("src/testrd_p1").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p1/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p1.t".getBytes()),
 				true, new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 50, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals("", getMakeFileVar(mp1,
 				MindMakefile.MIND_COMPONENTS));
 
 		mp1.getProject().getFile("src/testrd_p1/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p1.z".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFolder("src/testrd_p2").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p2/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p2.t".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p2/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p2.z".getBytes()),
 				true, new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals("",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		mp1.getProject().getFolder("src/testrd_p5").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p5/i.adl").create(
 				new ByteArrayInputStream("primitive testrd_p5.i".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p5/r.adl").create(
 				new ByteArrayInputStream("primitive testrd_p5.r".getBytes()),
 				true, new NullProgressMonitor());
 
 		// create two packages p3 and p4
 		// create two component in each package
 		mp1.getProject().getFolder("src/testrd_p3").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/t.adl").create(
 				new ByteArrayInputStream("primitive testrd_p3.t".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p3/i.adl").create(
 				new ByteArrayInputStream("primitive testrd_p3.i".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFolder("src/testrd_p4").create(false, true,
 				new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/z.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.z".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/u.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.u".getBytes()),
 				true, new NullProgressMonitor());
 		mp1.getProject().getFile("src/testrd_p4/r.adl").create(
 				new ByteArrayInputStream("primitive testrd_p4.r".getBytes()),
 				true, new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals(
 				"",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		mp1.getProject().getFile("src/testrd_p4/r.adl").delete(true,
 				new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals(
 				"",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		mp1.getProject().getFolder("src/testrd_p3").delete(true,
 				new NullProgressMonitor());
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals("",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		mp1.getMindpathentries().add(
 				MindIdeCore.newMPEAppli("testrd_p1.t", "appli1"));
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals(
 				"testrd_p1.t:appli1",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		mp1.getMindpathentries().add(
 				MindIdeCore.newMPEAppli("testrd_p4.u", "appli2"));
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_CHANGE_MAKEFILE_VAR_MIND_COMPONENT);
 		assertEquals(
 				"testrd_p1.t:appli1 testrd_p4.u:appli2",
 				getMakeFileVar(mp1, MindMakefile.MIND_COMPONENTS));
 
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "fail wait",
 				FamilyJobCST.FAMILY_ALL);
 	}
 	
 	
 	@Test
 	public void testMakeFileVarSRC() throws CoreException, IOException {
 		
 		String p1name = "P1_" + System.currentTimeMillis();
 		MindProject mp1 = MindIdeCore.createMINDProject(p1name,
 				new NullProgressMonitor());
 		assertNotNull(mp1);
 
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_ALL);
 
 		String p1FullPath = mp1.getProject().getLocation().toOSString();
 		assertEquals(p1FullPath+"/src", getMakeFileVar(mp1, MindMakefile.MIND_SRC));
 
 		String p2name = "P2_" + System.currentTimeMillis();
 		MindProject mp2 = MindIdeCore.createMINDProject(p2name,
 				new NullProgressMonitor());
 		assertNotNull(mp2);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_ALL);
 
 		
 		MindIdeCore.createMindPackage(p2name+"/src", "p1", null);
 		
 		MindPathEntry mpe_import_p1 = MindIdeCore.newMPEImport("p1");
 		mp1.getMindpathentries().add(mpe_import_p1);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_ALL);
 
 		assertEquals(p1FullPath+"/src:"+mp2.getProject().getLocation().toOSString()+"/src", getMakeFileVar(mp1, MindMakefile.MIND_SRC));
 		
 		mp1.getMindpathentries().remove(mpe_import_p1);
 		waitJob(DEFAULT_TIME_OUT_WAIT_JOB, 10, "failsetcomp",
 				FamilyJobCST.FAMILY_ALL);
 
 		assertEquals(p1FullPath+"/src", getMakeFileVar(mp1, MindMakefile.MIND_SRC));
 
 
 		
 	}
 
 	private String getMakeFileVar(MindProject mp, String varname)
 			throws CoreException, IOException {
 		return new MindMakefile(mp.getProject()).getMakefileVariable(varname);
 	}
 }
