 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.authorization.manager;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.octahedron.cotopaxi.eventbus.EventBus;
 import br.octahedron.figgo.modules.DataAlreadyExistsException;
 import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.authorization.data.DomainUser;
 import br.octahedron.figgo.modules.authorization.data.DomainUserDAO;
 import br.octahedron.figgo.modules.authorization.data.Role;
 import br.octahedron.figgo.modules.authorization.data.RoleDAO;
 
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
 
 /**
  * @author Danilo Queiroz
  */
 public class AuthorizationManagerTest {
 
 	private EventBus eventBus;
 	private RoleDAO roleDAO;
 	private DomainUserDAO domainUserDAO;
 	private AuthorizationManager authManager;
 	private GoogleAuthorizer gAuth;
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvIsLoggedIn(true);
 
 	@Before
 	public void setUp() {
 		this.helper.setUp();
 		this.gAuth = createMock(GoogleAuthorizer.class);
 		this.eventBus = createMock(EventBus.class);
 		this.roleDAO = createMock(RoleDAO.class);
 		this.domainUserDAO = createMock(DomainUserDAO.class);
 		this.authManager = new AuthorizationManager();
 		this.authManager.setRoleDAO(this.roleDAO);
 		this.authManager.setDomainUserDAO(this.domainUserDAO);
 		this.authManager.setEventBus(this.eventBus);
 		this.authManager.setGoogleAuthorizer(gAuth);
 	}
 
 	@Test(expected = DataAlreadyExistsException.class)
 	public void createRoleTest() {
 		// setup mock
 		expect(this.roleDAO.exists("role")).andReturn(false).times(2);
 		this.roleDAO.save(new Role("role"));
 		expect(this.roleDAO.exists("role")).andReturn(true).times(2);
 
 		replay(this.roleDAO);
 		try {
 			// test
 			assertFalse(this.authManager.existsRole("role"));
 			this.authManager.createRole("role");
 			assertTrue(this.authManager.existsRole("role"));
 			this.authManager.createRole("role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void deleteRoleTest() {
 		// setup mock
 		expect(this.roleDAO.exists("role")).andReturn(true).times(1);
 		this.roleDAO.delete("role");
 		expect(this.roleDAO.exists("role")).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			this.authManager.removeRole("role");
 			this.authManager.removeRole("role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void getTest() {
 		// setup mock
 		expect(this.roleDAO.exists("role")).andReturn(true).times(1);
 		expect(this.roleDAO.get("role")).andReturn(new Role("role")).times(1);
 		expect(this.roleDAO.exists("role")).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			Role role = this.authManager.getRole("role");
 			assertNotNull(role);
 			assertEquals(new Role("role"), role);
 			this.authManager.getRole("role");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void addUserTest() {
 		// setup mock
 		Role role = new Role("role");
 		expect(this.roleDAO.exists("role")).andReturn(true).times(1);
 		expect(this.roleDAO.get("role")).andReturn(role).times(1);
 		expect(this.roleDAO.exists("role")).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertEquals(0, role.getUsers().size());
 			this.authManager.addUsersToRole("role", "user1", "user2");
 			assertEquals(2, role.getUsers().size());
 			this.authManager.addUsersToRole("role", "user1");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void addActivitiesTest() {
 		// setup mock
 		Role role = new Role("role");
 		expect(this.roleDAO.exists("role")).andReturn(true).times(1);
 		expect(this.roleDAO.get("role")).andReturn(role).times(1);
 		expect(this.roleDAO.exists("role")).andReturn(false).times(1);
 		replay(this.roleDAO);
 		try {
 			// test
 			assertEquals(0, role.getActivities().size());
 			this.authManager.addActivitiesToRole("role", "activity1", "activity2");
 			assertEquals(2, role.getActivities().size());
 			this.authManager.addActivitiesToRole("role", "activity1");
 		} finally {
 			// verify
 			verify(this.roleDAO);
 		}
 	}
 
 	@Test(expected = DataDoesNotExistsException.class)
 	public void removeUserFromRole() {
 		// setup mock
 		Role role = new Role("role");
 		role.addUsers("user1", "user2");
 		expect(this.roleDAO.exists("role")).andReturn(true).times(1);
 		expect(this.roleDAO.get("role")).andReturn(role).times(1);
 		this.eventBus.publish(eq(new UserRemovedFromRoleEvent("user1", "domain")));
 		expect(this.roleDAO.exists("role")).andReturn(false).times(1);
 		replay(this.roleDAO, this.eventBus);
 		try {
 			// test
 			assertEquals(2, role.getUsers().size());
 			this.authManager.removeUserFromRole("role", "user1", "domain");
 			assertEquals(1, role.getUsers().size());
 			this.authManager.removeUserFromRole("role", "user1", "domain");
 		} finally {
 			// verify
 			verify(this.roleDAO, this.eventBus);
 		}
 	}
 
 	@Test
 	public void removeUserFromRoles() {
 		// setup mock
 		Role role = new Role("role");
 		role.addUsers("user1", "user2");
 		Role role2 = new Role("role2");
 		role2.addUsers("user1", "user2");
 		List<Role> roles = new LinkedList<Role>();
 		roles.add(role);
 		roles.add(role2);
 		expect(this.roleDAO.getUserRoles("user1")).andReturn(roles);
 		this.eventBus.publish(eq(new UserRemovedFromRoleEvent("user1", "domain")));
 		replay(this.domainUserDAO, this.roleDAO, eventBus);
 		try {
 			// test
 			this.authManager.removeUserFromRoles("user1", "domain");
 			assertEquals(1, role.getUsers().size());
 			assertEquals(1, role2.getUsers().size());
 		} finally {
 			// verify
 			verify(this.roleDAO, this.domainUserDAO, eventBus);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void getUserDomains() {
 		// setup mock
 		List<DomainUser> domains = new LinkedList<DomainUser>();
 		domains.add(new DomainUser("tester","domain1", true));
 		domains.add(new DomainUser("tester","domain2", true));
 		expect(this.domainUserDAO.getDomains("tester")).andReturn(domains);
 		expect(this.domainUserDAO.getDomains("newuser")).andReturn(Collections.EMPTY_LIST);
 		replay(this.domainUserDAO);
 		// test0
		Collection<String> domains2 = this.authManager.getUserDomains("tester");
 		assertEquals(2, domains2.size());
		domains2 = this.authManager.getUserDomains("newuser");
 		assertTrue(domains2.isEmpty());
 		// verify
 		verify(this.domainUserDAO);
 	}
 
 	@Test
 	public void authorizeTest() {
 		// setup mock
 		expect(this.gAuth.isApplicationAdmin()).andReturn(false);
 		expect(this.roleDAO.existsRoleFor("reviewer", "commit_code")).andReturn(false);
 		expect(this.gAuth.isApplicationAdmin()).andReturn(false);
 		expect(this.roleDAO.existsRoleFor("reviewer", "pull_code")).andReturn(true);
 		expect(this.gAuth.isApplicationAdmin()).andReturn(false);
 		expect(this.roleDAO.existsRoleFor("tester", "commit_code")).andReturn(true);
 		expect(this.gAuth.isApplicationAdmin()).andReturn(false);
 		expect(this.roleDAO.existsRoleFor("developer", "commit_code")).andReturn(true);
 		expect(this.gAuth.isApplicationAdmin()).andReturn(true);
 		
 		replay(this.roleDAO, this.gAuth);
 		// test
 		assertFalse(this.authManager.isAuthorized("reviewer", "commit_code"));
 		assertTrue(this.authManager.isAuthorized("reviewer", "pull_code"));
 		assertTrue(this.authManager.isAuthorized("tester", "commit_code"));
 		assertTrue(this.authManager.isAuthorized("developer", "commit_code"));
 		assertTrue(this.authManager.isAuthorized("root", "rule_everything"));
 
 		// verify
 		verify(this.roleDAO, this.gAuth);
 	}
 }
