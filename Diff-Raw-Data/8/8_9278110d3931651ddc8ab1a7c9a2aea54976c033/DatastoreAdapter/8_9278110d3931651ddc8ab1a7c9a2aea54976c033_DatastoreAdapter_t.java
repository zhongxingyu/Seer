 package com.blumenthal.ListeyTest;
 
 import static com.blumenthal.listey.TimeStampedNode.Status.ACTIVE;
 import static com.blumenthal.listey.TimeStampedNode.Status.DELETED;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.blumenthal.listey.CategoryInfo;
 import com.blumenthal.listey.DataStoreUniqueId;
 import com.blumenthal.listey.ItemCategoryInfo;
 import com.blumenthal.listey.ItemInfo;
 import com.blumenthal.listey.ListInfo;
 import com.blumenthal.listey.ListeyDataMultipleUsers;
 import com.blumenthal.listey.ListeyDataOneUser;
 import com.blumenthal.listey.OtherUserPrivOnList;
 import com.blumenthal.listey.TimeStampedNode;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 
 /**
  * 
  */
 
 /**
  * @author David
  *
  */
 public class DatastoreAdapter {
 	final String FOO_EMAIL = "foo@test.com";
 	final String BAR_EMAIL = "bar@test.com";
 	final String BAZ_EMAIL = "baz@test.com";
 	
 	private String fooList1Id = null;
 	private String fooListToDeleteId = null;
 	private String fooList1Item1Id = null;
 	private String fooListToDeleteItem1Id = null;
 	private String fooList1CatToDeleteId = null;
 	private long uniqueTime = 10000L;
 	private int tempUniqueId = 1;
 	
 	static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	
     private final LocalServiceTestHelper helper =
     		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
     @Before
     public void setUp() {
     	helper.setUp();
     }
 
     @After
     public void tearDown() {
     	helper.tearDown();
     }
  
     
     public ListeyDataMultipleUsers createAndSaveUser() {
 		DataStoreUniqueId uniqCreator = new DataStoreUniqueId();
 		DataStoreUniqueId.numShards=1;
 		
 		//Create and flesh out a multi-user data structure
 		ListeyDataMultipleUsers multiUser = new ListeyDataMultipleUsers();
 		ListeyDataOneUser fooUser = new ListeyDataOneUser();
 		fooUser.setUniqueId(FOO_EMAIL);
 		multiUser.userData.put(FOO_EMAIL, fooUser);
 		
 		//Create Foo list1
 		fooList1Id = uniqCreator.getUniqueId();
 		ListInfo fooList1 = new ListInfo(ACTIVE, fooList1Id, "Foo List 1", uniqueTime++);
 		fooUser.lists.put(fooList1.getUniqueId(), fooList1);
 		
 		OtherUserPrivOnList barPrivOnFoo1 = new OtherUserPrivOnList();
 		barPrivOnFoo1.userId = BAR_EMAIL;
 		barPrivOnFoo1.lastUpdate = uniqueTime++;
 		barPrivOnFoo1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
 		fooList1.getOtherUserPrivs().put(BAR_EMAIL, barPrivOnFoo1);
 		
 		//TODO - why isn't this working?
 		//OtherUserPrivOnList bazPrivOnFoo1 = new OtherUserPrivOnList();
 		//barPrivOnFoo1.userId = BAZ_EMAIL;
 		//barPrivOnFoo1.lastUpdate = uniqueTime++;
 		//barPrivOnFoo1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
 		//fooList1.getOtherUserPrivs().put(BAZ_EMAIL, bazPrivOnFoo1);
 		
 		CategoryInfo fooList1Cat1 = new CategoryInfo();
 		fooList1Cat1.setLastUpdate(uniqueTime++);
 		fooList1Cat1.setName("Foo List 1 Category 1");
 		fooList1Cat1.setStatus(TimeStampedNode.Status.ACTIVE);
 		fooList1Cat1.setUniqueId(uniqCreator.getUniqueId());
 		fooList1.getCategories().add(fooList1Cat1);
 		
 		CategoryInfo fooList1CatToDelete = new CategoryInfo();
 		fooList1CatToDelete.setLastUpdate(uniqueTime++);
 		fooList1CatToDelete.setName("Foo List 1 Category to delete");
 		fooList1CatToDelete.setStatus(ACTIVE);
 		fooList1CatToDeleteId = uniqCreator.getUniqueId();
 		fooList1CatToDelete.setUniqueId(fooList1CatToDeleteId);
 		fooList1.getCategories().add(fooList1CatToDelete);
 		
 		ItemInfo fooList1Item1 = new ItemInfo();
 		fooList1Item1Id = uniqCreator.getUniqueId();
 		fooList1Item1.setUniqueId(fooList1Item1Id);
 		fooList1.getItems().put(fooList1Item1.getUniqueId(), fooList1Item1);
 		fooList1Item1.setLastUpdate(uniqueTime++);
 		fooList1Item1.setName("Foo List 1 Item 1");
 		fooList1Item1.setStatus(ACTIVE);
 		fooList1Item1.setCount(2L);
 		ItemCategoryInfo fooList1Item1Cat1 = new ItemCategoryInfo();
 		fooList1Item1Cat1.setUniqueId(uniqCreator.getUniqueId());
 		fooList1Item1.getCategories().put(fooList1Item1Cat1.getUniqueId(), fooList1Item1Cat1);
 		fooList1Item1Cat1.setLastUpdate(uniqueTime++);
 		fooList1Item1Cat1.setStatus(ACTIVE);
 		
 		//Create Foo listToDelete
 		fooListToDeleteId = uniqCreator.getUniqueId();
		ListInfo fooListToDelete = new ListInfo(ACTIVE, fooListToDeleteId, "Foo List To Delete", uniqueTime++);
 		fooUser.lists.put(fooListToDelete.getUniqueId(), fooListToDelete);
 		
 		OtherUserPrivOnList barPrivOnFoo2 = new OtherUserPrivOnList();
 		barPrivOnFoo2.userId = BAR_EMAIL;
 		barPrivOnFoo2.lastUpdate = uniqueTime++;
 		barPrivOnFoo2.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
 		fooListToDelete.getOtherUserPrivs().put(BAR_EMAIL, barPrivOnFoo2);
 		
 		CategoryInfo fooListToDeleteCat1 = new CategoryInfo();
 		fooListToDeleteCat1.setLastUpdate(uniqueTime++);
		fooListToDeleteCat1.setName("Foo List To Delete Category 1");
 		fooListToDeleteCat1.setStatus(ACTIVE);
 		fooListToDeleteCat1.setUniqueId(uniqCreator.getUniqueId());
 		fooListToDelete.getCategories().add(fooListToDeleteCat1);
 		
 		ItemInfo fooListToDeleteItem1 = new ItemInfo();
 		fooListToDeleteItem1Id = uniqCreator.getUniqueId();
 		fooListToDeleteItem1.setUniqueId(fooListToDeleteItem1Id);
 		fooListToDelete.getItems().put(fooListToDeleteItem1.getUniqueId(), fooListToDeleteItem1);
 		fooListToDeleteItem1.setLastUpdate(uniqueTime++);
		fooListToDeleteItem1.setName("Foo List To Delete Item 1");
 		fooListToDeleteItem1.setStatus(ACTIVE);
 		fooListToDeleteItem1.setCount(2L);
 		ItemCategoryInfo fooListToDeleteItem1Cat1 = new ItemCategoryInfo();
 		fooListToDeleteItem1Cat1.setUniqueId(uniqCreator.getUniqueId());
 		fooListToDeleteItem1.getCategories().put(fooListToDeleteItem1Cat1.getUniqueId(), fooListToDeleteItem1Cat1);
 		fooListToDeleteItem1Cat1.setLastUpdate(uniqueTime++);
 		fooListToDeleteItem1Cat1.setStatus(ACTIVE);
 		
 		//Add list for bar user
 		//Create Bar list1
 		ListeyDataOneUser barUser = new ListeyDataOneUser();
 		multiUser.userData.put(BAR_EMAIL, barUser);
 		barUser.setUniqueId(BAR_EMAIL);
 		ListInfo barList1 = new ListInfo(ACTIVE, uniqCreator.getUniqueId(), "Bar List 1", uniqueTime++);
 		barUser.lists.put(barList1.getUniqueId(), barList1);
 				
 		OtherUserPrivOnList fooPrivOnBar1 = new OtherUserPrivOnList();
 		fooPrivOnBar1.userId = FOO_EMAIL;
 		fooPrivOnBar1.lastUpdate = uniqueTime++;
 		fooPrivOnBar1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
 		barList1.getOtherUserPrivs().put(FOO_EMAIL, fooPrivOnBar1);
 				
 		CategoryInfo barList1Cat1 = new CategoryInfo();
 		barList1Cat1.setLastUpdate(uniqueTime++);
 		barList1Cat1.setName("Bar List 1 Category 1");
 		barList1Cat1.setStatus(ACTIVE);
 		barList1Cat1.setUniqueId(uniqCreator.getUniqueId());
 		barList1.getCategories().add(barList1Cat1);
 
 		ItemInfo barList1Item1 = new ItemInfo();
 		barList1Item1.setUniqueId(uniqCreator.getUniqueId());
 		barList1.getItems().put(barList1Item1.getUniqueId(), barList1Item1);
 		barList1Item1.setLastUpdate(uniqueTime++);
 		barList1Item1.setName("Bar List 1 Item 1");
 		barList1Item1.setStatus(ACTIVE);
 		barList1Item1.setCount(2L);
 		ItemCategoryInfo barList1Item1Cat1 = new ItemCategoryInfo();
 		barList1Item1Cat1.setUniqueId(uniqCreator.getUniqueId());
 		barList1Item1.getCategories().put(barList1Item1Cat1.getUniqueId(), barList1Item1Cat1);
 		barList1Item1Cat1.setLastUpdate(uniqueTime++);
 		barList1Item1Cat1.setStatus(ACTIVE);
 		
 		//Convert multiUser to entities and write all the entities to the datastore at once
 		List<Entity> entities = multiUser.toEntities(uniqCreator);
 		//Also convert bar's list to entities, since foo has privs on bar's list
 		Key barKey = KeyFactory.createKey(ListeyDataOneUser.KIND, BAR_EMAIL);
 		entities.addAll(barList1.toEntities(uniqCreator, barKey));
 		datastore.put(entities);
 		
 		
 		//Now wipe the other user privs map from bar, since those won't be reloaded
 		//when we load data for foo
 		for (Map.Entry<String, ListInfo> entry : multiUser.userData.get(BAR_EMAIL).lists.entrySet()) {
 			entry.getValue().getOtherUserPrivs().clear();
 		}
 		
 		return multiUser;
     }//createAndSaveUser
     
     
 	
     public ListeyDataMultipleUsers[] createAndReloadUser() {
 		ListeyDataMultipleUsers clientMultiUser = createAndSaveUser();
 
 		//Now, reload (a new copy) back from the datastore
 		ListeyDataMultipleUsers serverMultiUser = new ListeyDataMultipleUsers(datastore, FOO_EMAIL);
 		
 		return new ListeyDataMultipleUsers[] {clientMultiUser, serverMultiUser};
     }//createAndReloadUser
     
     
 	@Test
 	public void testSaveAndLoadUser(){
 		ListeyDataMultipleUsers[] users = createAndReloadUser();
 		ListeyDataMultipleUsers clientMultiUser = users[0];
 		ListeyDataMultipleUsers serverMultiUser = users[1];
 		
 		//Verify loaded version matches saved version
 		assertEquals("before/after multi-user jsons don't match", clientMultiUser.toJson(), serverMultiUser.toJson());
 		assertEquals("loadedMultiUser doesn't match original", true, clientMultiUser.deepEquals(serverMultiUser));
 	}//testLoadAndSaveUser
 	
 	
 	@Test
 	public void testClientChange() {
 		ListeyDataMultipleUsers[] users = createAndReloadUser();
 		ListeyDataMultipleUsers clientMultiUser = users[0];
 		ListeyDataMultipleUsers serverMultiUser = users[1];
 		
 		//Now change the client and test compareAndUpdate
 		DataStoreUniqueId uniqueIdCreator = new DataStoreUniqueId();
 		
 		//*******************************************
 		//Change list name
 		ListInfo clientList1 = clientMultiUser.userData.get(FOO_EMAIL).lists.get(fooList1Id);
 		clientList1.setName("Foo List 1 new name");
 		clientList1.setLastUpdate(uniqueTime++);
 		
 		//Delete list
 		ListInfo clientListToDelete = clientMultiUser.userData.get(FOO_EMAIL).lists.get(fooListToDeleteId);
 		clientListToDelete.setStatus(DELETED);
 		clientListToDelete.setLastUpdate(uniqueTime++);
 		
 		//Add new list
 		ListInfo clientList2 = new ListInfo();
 		clientList2.setLastUpdate(uniqueTime++);
 		clientList2.setName("Foo List 2");
 		clientList2.setUniqueId(":" + tempUniqueId++);
 		clientMultiUser.userData.get(FOO_EMAIL).lists.put(clientList2.getUniqueId(), clientList2);
 		
 		//Change category name
 		Iterator<CategoryInfo> catIter = clientList1.getCategories().iterator();
 		CategoryInfo clientList1Cat1 = catIter.next();
 		clientList1Cat1.setName("Foo List 1 Category 1 new name");
 		clientList1Cat1.setLastUpdate(uniqueTime++);
 		
 		//Delete category
 		CategoryInfo clientList1CatToDelete = catIter.next();
 		clientList1CatToDelete.setStatus(DELETED);
 		clientList1CatToDelete.setLastUpdate(uniqueTime++);
 		
 		//Add new category
 		CategoryInfo clientList1Cat2 = new CategoryInfo();
 		clientList1Cat2.setLastUpdate(uniqueTime++);
 		clientList1Cat2.setName("Foo List 1 Category 2");
 		clientList1Cat2.setUniqueId(":" + tempUniqueId++);
 		clientList1.getCategories().add(clientList1Cat2);
 		
 		//Change item name
 		ItemInfo clientItem1 = clientList1.getItems().get(fooList1Item1Id);
 		clientItem1.setName("Foo List 1 Item 1 new name");
 		clientItem1.setLastUpdate(uniqueTime++);
 		
 		//Add new item
 		ItemInfo clientItem2 = new ItemInfo();
 		clientItem2.setUniqueId(":" + tempUniqueId++);
 		clientItem2.setCount(2L);
 		clientItem2.setLastUpdate(uniqueTime++);
 		clientItem2.setName("Foo List 1 Item 2");
 		clientList1.getItems().put(clientItem2.getUniqueId(), clientItem2);
 		
 		//Add new ItemCategoryInfo
 		ItemCategoryInfo clientList1Item2Cat2 = new ItemCategoryInfo();
 		clientList1Item2Cat2.setLastUpdate(uniqueTime++);
 		clientList1Item2Cat2.setUniqueId(clientList1Cat2.getUniqueId());
 		clientItem2.getCategories().put(clientList1Item2Cat2.getUniqueId(), clientList1Item2Cat2);
 		
 		//************************************************************************
 		//Test full compare
 		List<Entity> updateEntities = new ArrayList<Entity>();
 		List<Key> deleteKeys = new ArrayList<Key>();
 		ListeyDataMultipleUsers updatedMultiUser = ListeyDataMultipleUsers.compareAndUpdate(uniqueIdCreator, serverMultiUser, clientMultiUser, updateEntities, deleteKeys);
 		assertNotNull(updatedMultiUser);
 		assertEquals(4, deleteKeys.size());
 		assertEquals(9, updateEntities.size());
 		
 		//Verify list change was noticed
 		ListInfo updatedList1 = updatedMultiUser.userData.get(FOO_EMAIL).lists.get(fooList1Id);
 		assertTrue(updatedList1.shallowEquals(clientList1));
 		ListInfo listFromEntity = new ListInfo(updateEntities.remove(0));
 		assertTrue(listFromEntity.shallowEquals(clientList1));
 		
 		//Verify new item was noticed
 		//Iterate through the item ids until you find the one that is NOT the one we already knew
 		String updatedItem2UniqueId = null;
 		for (String oneId : updatedList1.getItems().keySet()) {
 			if (!oneId.equals(clientItem1.getUniqueId())) {
 				updatedItem2UniqueId = oneId;
 				break;
 			}
 		}//foreach oneId
 		ItemInfo updatedItem2 = updatedList1.getItems().get(updatedItem2UniqueId);
 		assertEquals(clientItem2.getCount(), updatedItem2.getCount());
 		assertEquals(clientItem2.getLastUpdate(), updatedItem2.getLastUpdate());
 		assertEquals(clientItem2.getName(), updatedItem2.getName());
 		assertEquals(clientItem2.getStatus(), updatedItem2.getStatus());
 		assertFalse("Expected " + clientItem2.getUniqueId() + " to be different from " + updatedItem2.getUniqueId(),
 				clientItem2.getUniqueId().equals(updatedItem2.getUniqueId()));
 		assertTrue(DataStoreUniqueId.isTemporaryId(clientItem2.getUniqueId()));
 		assertFalse(DataStoreUniqueId.isTemporaryId(updatedItem2.getUniqueId()));
 		ItemInfo itemFromEntity = new ItemInfo(updateEntities.remove(0));
 		assertTrue(itemFromEntity.shallowEquals(updatedItem2));
 		
 		//Verify new ItemCategoryInfo was noticed
 		ItemCategoryInfo updatedItem2CatInfo = updatedItem2.getCategories().values().iterator().next();
 		assertEquals(clientList1Item2Cat2.getLastUpdate(), updatedItem2CatInfo.getLastUpdate());
 		assertFalse("Expected " + clientList1Item2Cat2.getUniqueId() + " to be different from " + updatedItem2CatInfo.getUniqueId(),
 				clientList1Item2Cat2.getUniqueId().equals(updatedItem2CatInfo.getUniqueId()));
 		assertTrue(DataStoreUniqueId.isTemporaryId(clientList1Item2Cat2.getUniqueId()));
 		assertFalse(DataStoreUniqueId.isTemporaryId(updatedItem2CatInfo.getUniqueId()));
 		ItemCategoryInfo itemCatFromEntity = new ItemCategoryInfo(updateEntities.remove(0));
 		assertTrue(itemCatFromEntity.shallowEquals(updatedItem2CatInfo));
 		
 		
 		//Verify item change was noticed
 		ItemInfo updatedItem1 = updatedList1.getItems().get(fooList1Item1Id);
 		assertTrue(updatedItem1.shallowEquals(clientItem1));
 		itemFromEntity = new ItemInfo(updateEntities.remove(0));
 		assertTrue(itemFromEntity.shallowEquals(clientItem1));
 
 		
 		CategoryInfo updatedCat1=null, updatedCat2=null, deletedCat=null;
 		for (CategoryInfo oneCat : updatedList1.getCategories()) {
 			if (oneCat.getUniqueId().equals(clientList1Cat1.getUniqueId())){
 				updatedCat1 = oneCat;
 			}
 			else if (oneCat.getUniqueId().equals(fooList1CatToDeleteId)){
 				deletedCat = oneCat;
 			} else {
 				updatedCat2 = oneCat;
 			}
 		}
 
 
 		//Verify category change was noticed
 		assertTrue(updatedCat1.shallowEquals(clientList1Cat1));
 		CategoryInfo categoryFromEntity = new CategoryInfo(updateEntities.remove(0));
 		assertTrue(categoryFromEntity.shallowEquals(clientList1Cat1));
 		
 		
 		//Verify category deletion was noticed
 		assertTrue(deletedCat.shallowEquals(clientList1CatToDelete));
 		categoryFromEntity = new CategoryInfo(updateEntities.remove(0));
 		assertTrue(categoryFromEntity.shallowEquals(clientList1CatToDelete));
 		
 		
 		//Verify new category was noticed
 		assertEquals(clientList1Cat2.getName(), updatedCat2.getName());
 		assertEquals(clientList1Cat2.getLastUpdate(), updatedCat2.getLastUpdate());
 		assertEquals(clientList1Cat2.getStatus(), updatedCat2.getStatus());
 		assertTrue(DataStoreUniqueId.isTemporaryId(clientList1Cat2.getUniqueId()));
 		assertFalse(DataStoreUniqueId.isTemporaryId(updatedCat2.getUniqueId()));
 		assertFalse("Expected " + clientList1Cat2.getUniqueId() + " to be different from " + updatedCat2.getUniqueId(),
 				clientList1Cat2.getUniqueId().equals(updatedCat2.getUniqueId()));
 		categoryFromEntity = new CategoryInfo(updateEntities.remove(0));
 		assertTrue(categoryFromEntity.shallowEquals(updatedCat2));
 		
 		//Verify deleted list was noticed
 		ListInfo updatedListToDelete = updatedMultiUser.userData.get(FOO_EMAIL).lists.get(fooListToDeleteId);
 		assertTrue(updatedListToDelete.shallowEquals(clientListToDelete));
 		listFromEntity = new ListInfo(updateEntities.remove(0));
 		assertTrue(listFromEntity.shallowEquals(clientListToDelete));
 		Key deletedKey = deleteKeys.remove(0);
 		deletedKey.getKind().equals(ItemInfo.KIND);
 		deletedKey = deleteKeys.remove(0);
 		deletedKey.getKind().equals(ItemCategoryInfo.KIND);
 		
 		//Verify new list was noticed
 		//Iterate through the list ids until you find the one that is NOT the one we already knew
 		String updatedList2UniqueId = null;
 		for (String oneId : updatedMultiUser.userData.get(FOO_EMAIL).lists.keySet()) {
 			if (!oneId.equals(updatedList1.getUniqueId())) {
 				updatedList2UniqueId = oneId;
 				break;
 			}
 		}//foreach oneId
 		ListInfo updatedList2 = updatedMultiUser.userData.get(FOO_EMAIL).lists.get(updatedList2UniqueId);
 		assertEquals(clientList2.getLastUpdate(), updatedList2.getLastUpdate());
 		assertEquals(clientList2.getName(), updatedList2.getName());
 		assertEquals(clientList2.getStatus(), updatedList2.getStatus());
 		assertTrue(DataStoreUniqueId.isTemporaryId(clientItem2.getUniqueId()));
 		assertFalse(DataStoreUniqueId.isTemporaryId(updatedList2.getUniqueId()));
 		assertFalse("Expected " + clientList2.getUniqueId() + " to be different from " + updatedList2.getUniqueId(),
 				clientList2.getUniqueId().equals(updatedList2.getUniqueId()));
 		listFromEntity = new ListInfo(updateEntities.remove(0));
 		assertTrue(listFromEntity.shallowEquals(updatedList2));
 
 	}//testClientChange
 
 	
 	@Test
 	public void testServerChange() {
 		ListeyDataMultipleUsers[] users = createAndReloadUser();
 		ListeyDataMultipleUsers clientMultiUser = users[0];
 		ListeyDataMultipleUsers serverMultiUser = users[1];
 		
 		//Now change the client and test compareAndUpdate
 		DataStoreUniqueId uniqueIdCreator = new DataStoreUniqueId();
 		
 		//*******************************************
 		//Change list name
 		ListeyDataOneUser fooUser = serverMultiUser.userData.get(FOO_EMAIL);
 		fooUser.setChangedOnServer(true);
 		ListInfo serverList1 = fooUser.lists.get(fooList1Id);
 		serverList1.setName("Foo List 1 new name");
 		serverList1.setLastUpdate(uniqueTime++);
 		serverList1.setChangedOnServer(true);
 		
 		//Delete list
 		ListInfo serverListToDelete = serverMultiUser.userData.get(FOO_EMAIL).lists.get(fooListToDeleteId);
 		serverListToDelete.setStatus(DELETED);
 		serverListToDelete.setLastUpdate(uniqueTime++);
 		serverListToDelete.setChangedOnServer(true);
 		
 		//Add new list
 		ListInfo serverList2 = new ListInfo();
 		serverList2.setLastUpdate(uniqueTime++);
 		serverList2.setChangedOnServer(true);
 		serverList2.setName("Foo List 2");
 		serverList2.setUniqueId(uniqueIdCreator.getUniqueId());
 		serverMultiUser.userData.get(FOO_EMAIL).lists.put(serverList2.getUniqueId(), serverList2);
 		
 		//Change category name
 		CategoryInfo serverList1Cat1 = serverList1.getCategories().first();
 		serverList1Cat1.setName("Foo List 1 Category 1 new name");
 		serverList1Cat1.setLastUpdate(uniqueTime++);
 		serverList1Cat1.setChangedOnServer(true);
 		
 		//Add new category
 		CategoryInfo serverList1Cat2 = new CategoryInfo();
 		serverList1Cat2.setLastUpdate(uniqueTime++);
 		serverList1Cat2.setChangedOnServer(true);
 		serverList1Cat2.setName("Foo List 1 Category 2");
 		serverList1Cat2.setUniqueId(uniqueIdCreator.getUniqueId());
 		serverList1.getCategories().add(serverList1Cat2);
 		
 		//Change item name
 		ItemInfo serverItem1 = serverList1.getItems().get(fooList1Item1Id);
 		serverItem1.setName("Foo List 1 Item 1 new name");
 		serverItem1.setLastUpdate(uniqueTime++);
 		serverItem1.setChangedOnServer(true);
 		
 		//Add new item
 		ItemInfo serverItem2 = new ItemInfo();
 		serverItem2.setUniqueId(uniqueIdCreator.getUniqueId());
 		serverItem2.setCount(2L);
 		serverItem2.setLastUpdate(uniqueTime++);
 		serverItem2.setChangedOnServer(true);
 		serverItem2.setName("Foo List 1 Item 2");
 		serverList1.getItems().put(serverItem2.getUniqueId(), serverItem2);
 		
 		//Add new ItemCategoryInfo
 		ItemCategoryInfo serverList1Item2Cat2 = new ItemCategoryInfo();
 		serverList1Item2Cat2.setLastUpdate(uniqueTime++);
 		//Note, sub-items of new things don't get flagged as new from the server, because it's difficult and not needed
 		//serverList1Item2Cat2.setChangedOnServer(true);
 		serverList1Item2Cat2.setUniqueId(serverList1Cat2.getUniqueId());
 		serverItem2.getCategories().put(serverList1Item2Cat2.getUniqueId(), serverList1Item2Cat2);
 		
 		//************************************************************************
 		//Test full compare
 		List<Entity> updateEntities = new ArrayList<Entity>();
 		List<Key> deleteKeys = new ArrayList<Key>();
 		ListeyDataMultipleUsers updatedMultiUser = ListeyDataMultipleUsers.compareAndUpdate(uniqueIdCreator, serverMultiUser, clientMultiUser, updateEntities, deleteKeys);
 		assertNotNull(updatedMultiUser);
 		
 		//Since all changes were on the server, the updated version should match the server version
 		String updatedJson = updatedMultiUser.toJson();
 		String serverJson = serverMultiUser.toJson();
 		assertEquals(serverJson, updatedJson);
 		
 		assertEquals(0, deleteKeys.size());
 		//All changes were on the server, so it shouldn't think any entities need updating
 		assertEquals(0, updateEntities.size());
 	}//testServerChange
 
 }//DatastoreAdapter
