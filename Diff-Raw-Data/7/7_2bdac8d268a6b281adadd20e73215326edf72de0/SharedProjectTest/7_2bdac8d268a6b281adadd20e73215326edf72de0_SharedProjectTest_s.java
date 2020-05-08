 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.api;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.bowling.BowlingFactory;
 import org.eclipse.emf.emfstore.bowling.League;
 import org.eclipse.emf.emfstore.bowling.Player;
 import org.eclipse.emf.emfstore.bowling.Tournament;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.test.CommitCallbackAdapter;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.exceptions.ESUpdateRequiredException;
 import org.eclipse.emf.emfstore.server.model.ESBranchInfo;
 import org.eclipse.emf.emfstore.server.model.ESHistoryInfo;
 import org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery;
 import org.eclipse.emf.emfstore.server.model.query.ESRangeQuery;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESBranchVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 import org.junit.Test;
 
 public class SharedProjectTest extends BaseSharedProjectTest {
 
 	// TODO: OTS
 	private ESCommitCallback callback;
 	private ESUpdateCallback updateCallback;
 	private ESPrimaryVersionSpec target;
 
 	private boolean conflictOccurred;
 	private boolean noLocalChangesOccurred;
 
 	@Test
 	public void testShare() {
 		try {
 			final ESRemoteProject remoteProject = localProject.getRemoteProject();
 			assertNotNull(remoteProject);
 			assertTrue(localProject.isShared());
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCommit() {
 		try {
 			final ESPrimaryVersionSpec base = localProject.getBaseVersion();
 			addPlayerToProject();
 			final ESPrimaryVersionSpec head = localProject.commit(new NullProgressMonitor());
 			assertNotSame(base, head);
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	public void testCommitWithoutChange() throws ESException {
 		localProject.commit(null, new CommitCallbackAdapter() {
 			@Override
 			public void noLocalChanges(ESLocalProject projectSpace) {
 				noLocalChangesOccurred = true;
 			};
 		}, new NullProgressMonitor());
 		assertTrue(noLocalChangesOccurred);
 	}
 
 	@Test
 	public void testCommitLog() {
 
 		try {
 			final ESPrimaryVersionSpec base = localProject.getBaseVersion();
 			addPlayerToProject();
 			final ESPrimaryVersionSpec head = localProject.commit("SomeCommitMessage", callback,
 				new NullProgressMonitor());
 			assertNotSame(base, head);
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCommitBranch() {
 
 		try {
 			final ESPrimaryVersionSpec base = localProject.getBaseVersion();
 			addPlayerToProject();
 			final ESBranchVersionSpec branch = ESVersionSpec.FACTORY.createBRANCH("newBranch");
 			final ESPrimaryVersionSpec head = localProject.commitToBranch(branch, "SomeCommitMessage", callback,
 				new NullProgressMonitor());
 			assertNotSame(base, head);
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	public void testCommitBranchWithoutChange() throws ESException {
 		final ESBranchVersionSpec branch = ESVersionSpec.FACTORY.createBRANCH("newBranch");
 		localProject.commitToBranch(branch, "SomeCommitMessage", new CommitCallbackAdapter() {
 			@Override
 			public void noLocalChanges(ESLocalProject projectSpace) {
 				noLocalChangesOccurred = true;
 			}
 		}, new NullProgressMonitor());
 		assertTrue(noLocalChangesOccurred);
 	}
 
 	@Test
 	public void testBranchesOnlyTrunk() {
 		try {
 			final List<? extends ESBranchInfo> branches = localProject.getBranches(new NullProgressMonitor());
 			assertEquals(1, branches.size());
 			// TODO assert branch name
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testBranches() {
 
 		try {
 			List<? extends ESBranchInfo> branches = localProject.getBranches(new NullProgressMonitor());
 			assertEquals(1, branches.size());
 
 			addPlayerToProject();
 			final ESBranchVersionSpec branch = ESVersionSpec.FACTORY.createBRANCH("newBranch");
 			final ESPrimaryVersionSpec head = localProject.commitToBranch(branch, "SomeCommitMessage", callback,
 				new NullProgressMonitor());
 			branches = localProject.getBranches(new NullProgressMonitor());
 			assertEquals(2, branches.size());
 			// TODO assert branch names
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testHistoryInfoOnlyTrunk() {
 		try {
 			final ESRangeQuery query = ESHistoryQuery.FACTORY.rangeQuery(localProject.getBaseVersion(), 1, 1, true,
 				true,
 				true,
 				true);
 			final NullProgressMonitor monitor = new NullProgressMonitor();
 			List<ESHistoryInfo> infos = localProject.getHistoryInfos(query, monitor);
 			assertEquals(1, infos.size());
 
 			addPlayerToProject();
 
 			assertEquals(0, localProject.getBaseVersion().getIdentifier());
 			final ESPrimaryVersionSpec head = localProject.commit("SomeCommitMessage", callback,
 				new NullProgressMonitor());
 			assertEquals(1, localProject.getBaseVersion().getIdentifier());
 			infos = localProject.getHistoryInfos(ESHistoryQuery.FACTORY.rangeQuery(head, 1, 1, true, true, true, true),
 				monitor);
 			assertEquals(2, infos.size());
 			// TODO assert infos
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testHistoryInfoBranch() {
 		try {
 			final ESRangeQuery query = ESHistoryQuery.FACTORY.rangeQuery(localProject.getBaseVersion(), 1, 1, true,
 				true,
 				true,
 				true);
 			final NullProgressMonitor monitor = new NullProgressMonitor();
 
 			List<ESHistoryInfo> infos = localProject.getHistoryInfos(query, monitor);
 			assertEquals(1, infos.size());
 
 			addPlayerToProject();
 			final ESBranchVersionSpec branch = ESVersionSpec.FACTORY.createBRANCH("newBranch");
 			final ESPrimaryVersionSpec head = localProject.commitToBranch(branch, "SomeCommitMessage", callback,
 				new NullProgressMonitor());
 			infos = localProject.getHistoryInfos(query, monitor);
 			assertEquals(2, infos.size());
 			// TODO assert infos
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	private void addPlayerToProject() {
 		ProjectChangeUtil.addPlayerToProject(localProject);
 	}
 
 	@Test
 	public void testHasUncommitedChanges() {
 		assertFalse(localProject.hasUncommitedChanges());
 		addPlayerToProject();
 		assertTrue(localProject.hasUncommitedChanges());
 		try {
 			localProject.commit("SomeCommitMessage", callback, new NullProgressMonitor());
 		} catch (final ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 		assertFalse(localProject.hasUncommitedChanges());
 
 	}
 
 	@Test(expected = ESUpdateRequiredException.class)
 	public void testMergeAndExpectBaseVersionOutOfDateException() throws ESException {
 		final NullProgressMonitor monitor = new NullProgressMonitor();
 		final Player player = ProjectChangeUtil.addPlayerToProject(localProject);
 		localProject.commit(monitor);
 		final ESLocalProject checkedoutCopy = localProject.getRemoteProject().checkout("testCheckout", monitor);
 		final Player checkedoutPlayer = (Player) checkedoutCopy.getModelElements().get(0);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				player.setName("A");
 				return null;
 			}
 		});
 
 		localProject.commit(monitor);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				checkedoutPlayer.setName("B");
 				return null;
 			}
 		});
 		checkedoutCopy.commit(monitor);
 	}
 
 	@Test
 	public void testMoveElementViaReference() throws ESException {
 
 		final League league = ProjectChangeUtil.createLeague("Canadian bowling league");
 		final Player player = ProjectChangeUtil.createPlayer("Joe");
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				league.getPlayers().add(player);
 				localProject.getModelElements().add(league);
 				return null;
 			}
 		});
 		assertTrue(localProject.contains(league));
 
 		final ESLocalProject secondProject = workspace.createLocalProject("SecondTestProject");
 		secondProject.shareProject(new NullProgressMonitor());
 
 		// tournament does not contain players
 		final Tournament tournament = ProjectChangeUtil.createTournament(false);
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				secondProject.getModelElements().add(tournament);
 				tournament.getPlayers().add(player);
 				return null;
 			}
 		});
 
 		localProject.save();
 		secondProject.save();
 
 		assertTrue(secondProject.contains(player));
 		stopEMFStore();
 		startEMFStore();
 
 		for (final ESLocalProject localProject : ESWorkspaceProvider.INSTANCE.getWorkspace().getLocalProjects()) {
 			if (localProject.getProjectName().equals("SecondTestProject")) {
 				final Tournament t = (Tournament) localProject.getModelElements().get(0);
 				assertEquals(t.getPlayers().size(), 1);
 			}
 		}
 	}
 
 	@Test
 	public void testShareWithModelInExistingResource() throws IOException, ESException {
 		final Resource resource = createResource();
 		final League league = createLeague();
 		resource.getContents().add(league);
 		resource.save(ModelUtil.getResourceSaveOptions());
 
 		final ESLocalProject localProject = ESWorkspaceProvider.INSTANCE.getWorkspace().createLocalProject("test");
		localProject.getModelElements().add(league);
 		localProject.shareProject(usersession, new NullProgressMonitor());
 
 		assertTrue(localProject.isShared());
 	}
 
 	private static Resource createResource() throws IOException {
 		final ResourceSetImpl resourceSet = new ResourceSetImpl();
 		final Resource resource = resourceSet.createResource(
 			URI.createFileURI(File.createTempFile("league", ".xmi").getAbsolutePath()));
 		return resource;
 	}
 
 	private static League createLeague() {
 		final League league = BowlingFactory.eINSTANCE.createLeague();
 		final Player player = BowlingFactory.eINSTANCE.createPlayer();
 		league.getPlayers().add(player);
 		return league;
 	}
 
 	// TODO: API does not support merging currently
 	// @Test
 	// public void testMerge() throws ESException {
 	//
 	// final NullProgressMonitor monitor = new NullProgressMonitor();
 	// final Player player = ProjectChangeUtil.addPlayerToProject(localProject);
 	// localProject.commit(monitor);
 	// ESLocalProject checkedoutCopy = localProject.getRemoteProject().checkout(monitor);
 	// final Player checkedoutPlayer = (Player) checkedoutCopy.getModelElements().get(0);
 	//
 	// RunESCommand.run(new Callable<Void>() {
 	// public Void call() throws Exception {
 	// player.setName("A");
 	// return null;
 	// }
 	// });
 	//
 	// RunESCommand.run(new Callable<Void>() {
 	// public Void call() throws Exception {
 	// localProject.commit(monitor);
 	// return null;
 	// }
 	// });
 	//
 	// RunESCommand.run(new Callable<Void>() {
 	// public Void call() throws Exception {
 	// checkedoutPlayer.setName("B");
 	// return null;
 	// }
 	// });
 	//
 	// checkedoutCopy.commit(null, new CommitCallbackAdapter() {
 	// @Override
 	// public boolean baseVersionOutOfDate(final ESLocalProject localProject, IProgressMonitor progressMonitor) {
 	// ESPrimaryVersionSpec baseVersion = localProject.getBaseVersion();
 	// try {
 	// final ESPrimaryVersionSpec version = localProject.resolveVersionSpec(ESVersionSpec.FACTORY
 	// .createHEAD(baseVersion), monitor);
 	// localProject.update(version, new UpdateCallbackAdapter() {
 	// @Override
 	// public boolean conflictOccurred(
 	// org.eclipse.emf.emfstore.client.ESChangeConflict changeConflict,
 	// IProgressMonitor progressMonitor) {
 	// try {
 	// return localProject.merge(version, changeConflict,
 	// new TestConflictResolver(
 	// false, 1), null, new NullProgressMonitor());
 	// } catch (ESException e) {
 	// fail("Merge failed.");
 	// }
 	// return false;
 	// };
 	// }, new NullProgressMonitor());
 	// } catch (ESException e) {
 	// fail("Expected ChangeConflictException");
 	// }
 	// return true;
 	// }
 	// }, new NullProgressMonitor());
 	// assertEquals("B", checkedoutPlayer.getName());
 	// localProject.update(monitor);
 	// assertEquals("B", player.getName());
 	// assertTrue(EMFStoreClientUtil.areEqual(localProject, checkedoutCopy));
 	// }
 }
