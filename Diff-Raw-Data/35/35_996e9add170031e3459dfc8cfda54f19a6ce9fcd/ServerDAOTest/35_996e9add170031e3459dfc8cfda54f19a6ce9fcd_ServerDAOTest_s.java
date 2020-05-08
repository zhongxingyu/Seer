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
 
 import org.eclipse.ecf.protocol.nntp.core.ServerFactory;
 import org.eclipse.ecf.protocol.nntp.model.ICredentials;
 import org.eclipse.ecf.protocol.nntp.model.ISecureStore;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.IStore;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 import org.eclipse.ecf.protocol.nntp.store.derby.StoreFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ServerDAOTest {
 
 	private static IServer server;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		DatabaseTest.setUpBeforeClass();
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		DatabaseTest.tearDownAfterClass();
 
 	}
 
 	private IStore store;
 
 	@Before
 	public void setUp() throws Exception {
		store = StoreFactory.createStore(SALVO.SALVO_HOME + SALVO.SEPARATOR
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
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testServerDAO() throws StoreException {
 		new ServerDAO(DatabaseTest.db.getConnection(),store);
 	}
 
 	@Test
 	public void testInsertServer() throws SQLException, NNTPException {
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
 		server.setSubscribed(false);
 
 		new ServerDAO(DatabaseTest.db.getConnection(),store).insertServer(server);
 		IServer[] getServer = new ServerDAO(DatabaseTest.db.getConnection(),store)
 				.getServer(server.getURL());
 		assertTrue(getServer[0] == server);
 
 	}
 
 	@Test
 	public void testUpdateServer() throws SQLException, NNTPException {
 		testDeleteServer();
 		testInsertServer();
 		IServer[] servers = new ServerDAO(DatabaseTest.db.getConnection(),store)
 				.getServer(server.getURL());
 
 		assertTrue(servers[0].isSubscribed() == false);
 		servers[0].setSubscribed(true);
 		new ServerDAO(DatabaseTest.db.getConnection(),store).updateServer(servers[0]);
 
 	}
 
 	@Test
 	public void testGetServer() throws SQLException, NNTPException {
 
 		testInsertServer();
 		IServer[] getServer = new ServerDAO(DatabaseTest.db.getConnection(),store)
 				.getServer(server.getURL());
 		assertTrue(getServer.length == 1);
 	}
 
 	@Test
 	public void testDeleteServer() throws StoreException, SQLException {
 		new ServerDAO(DatabaseTest.db.getConnection(),store).deleteServer(server);
 		IServer[] getServer = new ServerDAO(DatabaseTest.db.getConnection(),store)
 				.getServer(server.getURL());
 
 		assertTrue(getServer.length == 0);
 	}
 }
