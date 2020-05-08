 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Nederland
  *
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *
  *  Contributors:
  *     Wim Jongman - initial API and implementation
  *
  *
  *******************************************************************************/
 package org.eclipse.ecf.protocol.nntp.store.derby.internal;
 
 import static org.junit.Assert.assertTrue;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import org.eclipse.ecf.protocol.nntp.core.NewsgroupFactory;
 import org.eclipse.ecf.protocol.nntp.core.ServerFactory;
 import org.eclipse.ecf.protocol.nntp.model.ICredentials;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.ISecureStore;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.IStore;
import org.eclipse.ecf.protocol.nntp.model.IStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 import org.eclipse.ecf.protocol.nntp.store.derby.StoreFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class NewsgroupDAOTest {
 
 	private static final String[] groups = new String[] { "eclipse.newcomer",
 			"eclipse.platform.pde", "eclipse.platform.rcp",
 			"eclipse.technology.ecf", "eclipse.test" };
 	private static IStore store;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
		final IStoreFactory sf = new StoreFactory();
		store = sf.createStore(SALVO.SALVO_HOME + SALVO.SEPARATOR
 				+ "StoreTestDerby");
 		store.setSecureStore(new ISecureStore() {
 			HashMap<String, String> mappie = new HashMap<String, String>();
 
 			public void remove(String key) {
 				mappie.remove(key);
 			}
 
 			public void put(String key, String value, boolean encrypt) {
 				mappie.put(key, value);
 			}
 
 			public String get(String key, String def) {
 				return mappie.get(key).equals(null) ? def : mappie.get(key);
 			}
 
 			public void clear() {
 				mappie.clear();
 			}
 		});
 
 		DatabaseTest.setUpBeforeClass();
 		ICredentials iCredentials = new ICredentials() {
 
 			public String getUser() {
 				return "Wim Jongman";
 			}
 
 			public String getPassword() {
 				return "flinder1f7";
 			}
 
 			public String getOrganization() {
 				return "organization";
 			}
 
 			public String getLogin() {
 				return "exquisitus";
 			}
 
 			public String getEmail() {
 				return "wim.jongman@gmail.com";
 			}
 		};
 
 		server = ServerFactory.getCreateServer("news.eclipse.org", 119,
 				iCredentials, true);
 		new ServerDAO(DatabaseTest.db.getConnection(), store)
 				.insertServer(server);
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		DatabaseTest.tearDownAfterClass();
 	}
 
 	private static IServer server;
 	private static int lastnumber;
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testNewsgroupDAO() throws StoreException {
 		new NewsgroupDAO(DatabaseTest.db.getConnection());
 	}
 
 	@Test
 	public void testInsertNewsgroup() throws NNTPException, SQLException {
 
 		NewsgroupDAO DAO = new NewsgroupDAO(DatabaseTest.db.getConnection());
 		for (String group : groups) {
 			INewsgroup newsgroup = NewsgroupFactory.createNewsGroup(server,
 					group, group);
 			DAO.insertNewsgroup(newsgroup);
 			lastnumber = Integer.parseInt(newsgroup.getProperty("DB_ID"));
 		}
 
 		INewsgroup[] newsgroups = DAO.getSubscribedNewsgroups(server, false);
 
 		assertTrue(newsgroups.length == groups.length);
 
 	}
 
 	@Test
 	public void testGetNewsgroupIServerString() throws StoreException,
 			SQLException {
 		for (String group : groups) {
 			assertTrue(group + " not found.", new NewsgroupDAO(DatabaseTest.db
 					.getConnection()).getNewsgroup(server, group).length == 1);
 		}
 	}
 
 	@Test
 	public void testGetNewsgroupIServerInt() throws StoreException,
 			SQLException {
 		assertTrue(new NewsgroupDAO(DatabaseTest.db.getConnection())
 				.getNewsgroup(server, lastnumber).length == 1);
 	}
 
 	@Test
 	public void testGetSubscribedNewsgroups() throws StoreException,
 			SQLException {
 		assertTrue(new NewsgroupDAO(DatabaseTest.db.getConnection())
 				.getSubscribedNewsgroups(server, false).length == groups.length);
 		assertTrue(new NewsgroupDAO(DatabaseTest.db.getConnection())
 				.getSubscribedNewsgroups(server, true).length == 0);
 
 	}
 
 	@Test
 	public void testUpdateNewsgroup() throws StoreException {
 
 		NewsgroupDAO DAO = new NewsgroupDAO(DatabaseTest.db.getConnection());
 		INewsgroup[] fetched = DAO.getNewsgroup(server, "%");
 		for (INewsgroup grp : fetched) {
 			grp.setSubscribed(true);
 			DAO.updateNewsgroup(grp);
 		}
 		assertTrue(DAO.getSubscribedNewsgroups(server, true).length == groups.length);
 		assertTrue(DAO.getSubscribedNewsgroups(server, false).length == 0);
 
 		fetched = DAO.getNewsgroup(server, "%");
 		for (INewsgroup grp : fetched) {
 			grp.setSubscribed(false);
 			DAO.updateNewsgroup(grp);
 		}
 		assertTrue(DAO.getSubscribedNewsgroups(server, true).length + "", DAO
 				.getSubscribedNewsgroups(server, true).length == 0);
 		assertTrue(DAO.getSubscribedNewsgroups(server, false).length == groups.length);
 
 	}
 
 	@Test
 	public void testDeleteNewsgroup() throws SQLException, StoreException {
 		NewsgroupDAO DAO = new NewsgroupDAO(DatabaseTest.db.getConnection());
 		for (String group : groups) {
 			INewsgroup newsgroup = NewsgroupFactory.createNewsGroup(server,
 					group, group);
 			DAO.deleteNewsgroup(newsgroup);
 		}
 
 		INewsgroup[] newsgroups = DAO.getSubscribedNewsgroups(server, true);
 
 		assertTrue(newsgroups.length == 0);
 	}
 
 }
