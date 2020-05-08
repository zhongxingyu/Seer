 package com.surevine.profileserver.db.jdbc.JDBCDataStoreTest;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.xmpp.packet.JID;
 
 import com.surevine.profileserver.db.jdbc.DatabaseTester;
 import com.surevine.profileserver.db.jdbc.JDBCDataStore;
 import com.surevine.profileserver.db.jdbc.dialect.Sql92DataStoreDialect;
 import com.surevine.profileserver.helpers.IQTestHandler;
 
 public class RosterTest {
 
 	DatabaseTester dbTester;
 	Connection conn;
 
 	JDBCDataStore store;
 	private JID ownerJid = new JID("owner@example.com");
 	private JID userJid  = new JID("user@server.co.uk/desktop");
 	private String group = "friends";
 
 	public RosterTest() throws SQLException, IOException,
 			ClassNotFoundException {
 		dbTester = new DatabaseTester();
 		IQTestHandler.readConf();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		dbTester.initialise();
 
 		store = new JDBCDataStore(dbTester.getConnection(),
 				new Sql92DataStoreDialect());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		dbTester.close();
 	}
 
 	@Test
 	public void testCanAddRosterItem() throws Exception {
 		dbTester.loadData("basic-data");
 		store.addRosterEntry(ownerJid, userJid, group);
 		Assert.assertEquals(group, store.getRosterGroupsForUser(ownerJid, userJid).get(0));
 	}
 
 	@Test
 	public void testCanGetRosterGroup() throws Exception {
 		dbTester.loadData("basic-data");
 		Assert.assertEquals("advisor",
 				store.getRosterGroupsForUser(ownerJid, new JID("mum@example.com/home")).get(0));
 		Assert.assertEquals("family",
 				store.getRosterGroupsForUser(ownerJid, new JID("mum@example.com/home")).get(1));
 	}
 	
 	@Test
 	public void testNoEntryReturnsEmptyResults() throws Exception {
 		dbTester.loadData("basic-data");
 		Assert.assertEquals(0, store.getRosterGroupsForUser(ownerJid, userJid).size());
 	}
 	
 	@Test
 	public void testMultipleRosterGroupsAreReflectedInResults() throws Exception {
 		dbTester.loadData("basic-data");
 		
 		ArrayList<String> groups = store.getRosterGroupsForUser(ownerJid, new JID("boss@company.org"));
 		Assert.assertEquals(2, groups.size());
 		Assert.assertEquals("colleagues", groups.get(0));
 		Assert.assertEquals("people-i-dont-like", groups.get(1));
 	}
 	
 	@Test
 	public void testCanGetRosterGroups() throws Exception {
 		dbTester.loadData("basic-data");
 		ArrayList<String> groups = store.getOwnerRosterGroupList(ownerJid);
 		Assert.assertEquals(5, groups.size());
 		Assert.assertEquals("advisor", groups.get(0));
 		Assert.assertEquals("colleagues", groups.get(1));
 		Assert.assertEquals("family", groups.get(2));
 		Assert.assertEquals("friends", groups.get(3));
 		Assert.assertEquals("people-i-dont-like", groups.get(4));
 	}
 
 	@Test
 	public void testCanClearRoster() throws Exception {
 		dbTester.loadData("basic-data");
 		store.clearRoster(ownerJid);
 		Assert.assertEquals(0, store.getOwnerRosterGroupList(ownerJid).size());
 	}
 
 	@Test
 	public void testCanGetRosterGroupsForVCard() throws Exception {
 		dbTester.loadData("basic-data");
		Assert.assertEquals("advisor",
				store.getRosterGroupsForVCard(ownerJid, "family").get(0));
 		Assert.assertEquals("family",
 				store.getRosterGroupsForVCard(ownerJid, "family").get(1));
 	}
 	
 	@Test
 	public void testNoEntryReturnsEmptyResultsForVCard() throws Exception {
 		dbTester.loadData("basic-data");
 		Assert.assertEquals(0, store.getRosterGroupsForVCard(ownerJid, "cats").size());
 	}
 	
 	@Test
 	public void testMultipleRosterGroupsForVCardAreReflectedInResults() throws Exception {
 		dbTester.loadData("basic-data");
 		
 		ArrayList<String> groups = store.getRosterGroupsForVCard(ownerJid, "family");
 		Assert.assertEquals(2, groups.size());
 		Assert.assertEquals("colleagues", groups.get(0));
 		Assert.assertEquals("people-i-dont-like", groups.get(1));
 	}
 
 }
