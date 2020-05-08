 package org.eclipse.emf.emfstore.client.test.api;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.emfstore.bowling.League;
 import org.eclipse.emf.emfstore.bowling.Player;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.ESWorkspace;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.test.server.api.util.TestConflictResolver;
 import org.eclipse.emf.emfstore.common.model.ESModelElementId;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESLogMessage;
 import org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UnsharedLocalProjectTest extends BaseEmptyEmfstoreTest {
 
 	private final ESWorkspace workspace = ESWorkspaceProvider.INSTANCE.getWorkspace();
 	private ESLocalProject localProject;
 
 	@Override
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		localProject = workspace.createLocalProject("TestProject", "My Test Project");
 	}
 
 	@Override
 	@After
 	public void tearDown() throws Exception {
 		List<ESLocalProject> projects = new ArrayList<ESLocalProject>(workspace.getLocalProjects());
 		for (ESLocalProject lp : projects) {
 			lp.delete(new NullProgressMonitor());
 		}
 		super.tearDown();
 	}
 
 	@Test
 	public void testProjectName() {
 		assertEquals("TestProject", localProject.getProjectName());
 	}
 
 	@Test
 	public void testProjectID() {
 		// unshared projects have no project ID
 		assertNull(localProject.getGlobalProjectId());
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testAddTag() throws ESException {
 		// a tag can not be created for an unshared project
 		localProject.addTag(localProject.getBaseVersion(), ESVersionSpec.FACTORY.createTAG("test", "test"),
 			new NullProgressMonitor());
 		fail("Cannot add a tag!");
 	}
 
 	@Test
 	public void testIsShared() {
 		assertFalse(localProject.isShared());
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCommit() throws ESException {
 		// can not commit an unshared project
 		localProject.commit();
 		fail("Should not be able to commit an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCommit2() throws ESException {
 		// can not commit an unshared project
 		localProject.commit(ESLogMessage.FACTORY.createLogMessage("test", "super"), null, new NullProgressMonitor());
 		fail("Should not be able to commit an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testCommitToBranch() throws ESException {
 		localProject.commitToBranch(ESVersionSpec.FACTORY.createBRANCH(localProject.getBaseVersion()),
 			ESLogMessage.FACTORY.createLogMessage("test", "super"), null, new NullProgressMonitor());
 		fail("Should not be able to commit an unshared Project!");
 	}
 
 	@Test
 	public void testGetBaseVersion() {
 		// unshared project has no base version
 		ESPrimaryVersionSpec version = localProject.getBaseVersion();
 		assertNull(version);
 	}
 
 	@Test(expected = ESException.class)
 	public void testGetBranches() throws ESException {
 		localProject.getBranches(new NullProgressMonitor());
 		fail("Should not be able to getBranches from an unshared Project!");
 	}
 
 	@Test(expected = ESException.class)
 	public void testGetHistoryInfos() throws ESException {
 		Player player = ProjectChangeUtil.addPlayerToProject(localProject);
 		ESModelElementId id = localProject.getModelElementId(player);
 		ESHistoryQuery query = ESHistoryQuery.FACTORY.modelelementQuery(localProject.getBaseVersion(), id, 1, 0, true,
 			true);
 		localProject.getHistoryInfos(query, new NullProgressMonitor());
 		fail("Should not be able to getHistoryInfos from an unshared Project!");
 	}
 
 	@Test
 	public void testGetLastUpdated() {
 		// unshared project has no last updated date
 		Date date = localProject.getLastUpdated();
 		assertNull(date);
 	}
 
 	@Test(expected = ESException.class)
 	public void testGetRemoteProject() throws ESException {
 		ESRemoteProject remoteProject = localProject.getRemoteProject();
 		assertNull(remoteProject);
 		fail("Should not be able to getRemoteProject from an unshared Project!");
 	}
 
 	@Test
 	public void testGetUsersession() {
 		ESUsersession session = localProject.getUsersession();
 		assertNull(session);
 	}
 
 	@Test
 	public void testHasUncommitedChanges() {
 		assertFalse(localProject.hasUncommitedChanges());
 		ProjectChangeUtil.addPlayerToProject(localProject);
 		assertTrue(localProject.hasUncommitedChanges());
 	}
 
 	@Test
 	public void testHasUnsavedChanges() {
 		assertFalse(localProject.hasUnsavedChanges());
 		ProjectChangeUtil.addPlayerToProject(localProject);
 		assertTrue(localProject.hasUnsavedChanges());
 		localProject.save();
 		assertFalse(localProject.hasUnsavedChanges());
 	}
 
 	@Test
 	public void testImportLocalChanges() throws ESException {
 		// TODO: localProject.importLocalChanges(fileName);
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testIsUpdated() throws ESException {
 		localProject.isUpdated();
 		fail("Should not be able to check update state of an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testMerge() throws ESException {
 		localProject.mergeBranch(localProject.getBaseVersion(), new TestConflictResolver(false, 0),
 			new NullProgressMonitor());
 		fail("Should not be able to merge with head on an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testMergeBranch() throws ESException {
 		localProject.mergeBranch(localProject.getBaseVersion(),
 			new TestConflictResolver(false, 0), new NullProgressMonitor());
 
 		fail("Should not be able to merge with head on an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testRemoveTag() throws ESException {
 		localProject.removeTag(localProject.getBaseVersion(), ESVersionSpec.FACTORY.createTAG("tag", "branch"),
 			new NullProgressMonitor());
 		fail("Should not remove a tag from an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testResolveSpec() throws ESException {
 		localProject.resolveVersionSpec(ESVersionSpec.FACTORY.createHEAD(), new NullProgressMonitor());
 		fail("Should not be able to resolve a version spec from an unshared Project!");
 	}
 
 	@Test
 	public void testRevert() {
 		Player player1 = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player2 = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player3 = ProjectChangeUtil.addPlayerToProject(localProject);
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertTrue(localProject.getAllModelElements().contains(player2));
 		assertTrue(localProject.getAllModelElements().contains(player3));
 
 		localProject.revert();
 
 		assertEquals(localProject.getAllModelElements().size(), 0);
 	}
 
 	@Test
 	public void testSave() {
 
 		ProjectChangeUtil.addPlayerToProject(localProject);
 
 		localProject.save();
 		localProject = null;
 		localProject = workspace.getLocalProjects().get(0);
 	}
 
 	@Test
 	public void testUndoLastOperation() {
 		Player player1 = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player2 = ProjectChangeUtil.addPlayerToProject(localProject);
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertTrue(localProject.getAllModelElements().contains(player2));
 
 		localProject.undoLastOperation();
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertFalse(localProject.getAllModelElements().contains(player2));
 	}
 
 	@Test
 	public void testUndoLastOperationsMore() {
 		Player player = ProjectChangeUtil.addPlayerToProject(localProject);
 		assertTrue(localProject.getAllModelElements().contains(player));
 		localProject.undoLastOperations(0);
 		assertTrue(localProject.getAllModelElements().contains(player));
 		localProject.undoLastOperations(1);
 		assertFalse(localProject.getAllModelElements().contains(player));
 		localProject.undoLastOperations(2);
 	}
 
 	@Test
 	public void testUndoLastOperations() {
 		Player player1 = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player2 = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player3 = ProjectChangeUtil.addPlayerToProject(localProject);
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertTrue(localProject.getAllModelElements().contains(player2));
 		assertTrue(localProject.getAllModelElements().contains(player3));
 
 		localProject.undoLastOperations(0);
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertTrue(localProject.getAllModelElements().contains(player2));
 		assertTrue(localProject.getAllModelElements().contains(player3));
 
 		localProject.undoLastOperations(1);
 
 		assertTrue(localProject.getAllModelElements().contains(player1));
 		assertTrue(localProject.getAllModelElements().contains(player2));
 		assertFalse(localProject.getAllModelElements().contains(player3));
 
 		localProject.undoLastOperations(2);
 
 		assertFalse(localProject.getAllModelElements().contains(player1));
 		assertFalse(localProject.getAllModelElements().contains(player2));
 		assertFalse(localProject.getAllModelElements().contains(player3));
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testUpdate() throws ESException {
 		localProject.update();
 		fail("Should not be able to update an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testUpdateVersion() throws ESException {
 		localProject.update(localProject.getBaseVersion());
 		fail("Should not be able to update an unshared Project!");
 	}
 
 	@Test(expected = RuntimeException.class)
 	public void testUpdateVersionCallback() throws ESException {
 		localProject.update(localProject.getBaseVersion(), null, new NullProgressMonitor());
 		fail("Should not be able to update an unshared Project!");
 	}
 
 	@Test
 	public void testShare() {
 		try {
 			assertNull(localProject.getUsersession());
 			localProject.shareProject(new NullProgressMonitor());
 		} catch (ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testShareSession() {
 		try {
 			ESServer server = ESServer.FACTORY.getServer("localhost", 8080, KeyStoreManager.DEFAULT_CERTIFICATE);
 			ESUsersession usersession = server.login("super", "super");
 			localProject.shareProject(usersession, new NullProgressMonitor());
 			ESRemoteProject remote = localProject.getRemoteProject();
 			assertNotNull(remote);
 		} catch (ESException e) {
 			log(e);
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testGetterMethods() throws ESException {
 
 		League league = ProjectChangeUtil.createLeague("League");
 		Player player = ProjectChangeUtil.addPlayerToProject(localProject);
 		Player player2 = ProjectChangeUtil.addPlayerToProject(localProject);
 
 		league.getPlayers().add(player);
 		league.getPlayers().add(player2);
 
 		localProject.getModelElements().add(league);
 		assertEquals(3, localProject.getAllModelElements().size());
 		assertEquals(1, localProject.getModelElements().size());
 		assertEquals(2, localProject.getAllModelElementsByClass(Player.class).size());
		assertEquals(1, localProject.getAllModelElementsByClass(League.class, true).size());
 	}
 }
