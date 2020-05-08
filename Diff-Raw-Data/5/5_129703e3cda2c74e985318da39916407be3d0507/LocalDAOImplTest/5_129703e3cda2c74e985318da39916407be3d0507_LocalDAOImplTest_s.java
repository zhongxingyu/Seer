 package com.szas.server.gwt.client;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class LocalDAOImplTest extends UniversalDAOImplTest {
 	private static final long EXAMPLE_TIMESTAMP = 5;
 	private static final long NEW_EXAMPLE_TIMESTAMP = EXAMPLE_TIMESTAMP + 4;
 	private LocalDAO<MockElement> localDAO;
 	@Before
 	public void setUp() {
 		LocalDAOImpl<MockElement> localDAOImpl =
 			new LocalDAOImpl<MockElement>();
 		localDAO = localDAOImpl;
 		universalDAO = localDAOImpl;
 		contentObserverProviderImpl = localDAOImpl;
 	}
 	@Test
 	public void testSyncInfoInsered() {		
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		localDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.INSERTING,elementToSync.getStatus());
 	}
 	
 	@Test
 	public void testSyncInfoUpdated() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		
 		
 		elementToSync.setStatus(LocalTuple.Status.SYNCED);
 		// TODO inform LocalDAO about changes state to SYNCED		
 		
 		element.data = NEW_EXAMPLE_DATA;
 		universalDAO.update(element);
 		
 		elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.UPDATING,elementToSync.getStatus());
 	}
 	
 	@Test
 	public void testSyncInfoDeleted() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		ArrayList<LocalTuple<MockElement>> elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 
 		LocalTuple<MockElement> elementToSync = elementsToSync.get(0);
 		
 		
 		elementToSync.setStatus(LocalTuple.Status.SYNCED);
 		// TODO inform LocalDAO about changes state to SYNCED
 		
 		
 		universalDAO.delete(element);
 		
 		elementsToSync = localDAO.getElementsToSync();
 		assertEquals(1, elementsToSync.size());
 		
 		elementToSync = elementsToSync.get(0);
 		element.testEuality(elementToSync.getElement());
 		assertEquals(LocalTuple.Status.DELETING,elementToSync.getStatus());
 	}
 	private class WrongObject {
 		
 	};
 	@Test(expected=WrongObjectThrowable.class)
 	public void testWrongUnkonwonElements() throws WrongObjectThrowable {
 		
 		ArrayList<Object> syncedElements =
 			new ArrayList<Object>();
 		syncedElements.add(new WrongObject());
 		localDAO.setSyncedUnknownElements(syncedElements);
 	}
 	
 	@Test
 	public void testGetSyncedData() {
 		MyContentObserver myContentObserver = 
 			new MyContentObserver();
 		localDAO.addContentObserver(myContentObserver);
 		
 		ArrayList<RemoteTuple<MockElement>> syncedElements =
 			new ArrayList<RemoteTuple<MockElement>>();
 		RemoteTuple<MockElement> remoteTuple = 
 			new RemoteTuple<MockElement>();
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		remoteTuple.setElement(element);
 		remoteTuple.setTimestamp(EXAMPLE_TIMESTAMP);
 		remoteTuple.setDeleted(false);
 		syncedElements.add(remoteTuple );
 		
 		localDAO.setSyncedElements(syncedElements );
 		localDAO.setLastTimestamp(EXAMPLE_TIMESTAMP);
 		
 		assertTrue("content obserer schould be notiffied", myContentObserver.notiffied);
 		
 		ArrayList<MockElement> mockElements = 
 			localDAO.getAll();
 		
 		assertEquals(1, mockElements.size());
 		assertEquals(EXAMPLE_DATA, mockElements.get(0).data);
 	}
 	
 	@Test
 	public void testInsertUpdateSyncedData() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		
 		ArrayList<LocalTuple<MockElement>> elementsToSync =
 			localDAO.getElementsToSync();
 		ArrayList<RemoteTuple<MockElement>> syncedElements = 
 			new ArrayList<RemoteTuple<MockElement>>();
 		for (LocalTuple<MockElement> elementToSync : elementsToSync) {
 			RemoteTuple<MockElement> remoteTuple = 
 				new RemoteTuple<UniversalDAOImplTest.MockElement>();
 			remoteTuple.setDeleted(false);
 			remoteTuple.setTimestamp(EXAMPLE_TIMESTAMP);
 			remoteTuple.setElement(elementToSync.getElement());
 			syncedElements.add(remoteTuple);
 		}
 		localDAO.setSyncedElements(syncedElements);
 		
 		ArrayList<MockElement> elements = 
 			localDAO.getAll();
 		assertEquals(1, elements.size());
 		assertEquals(element.getId(), elements.get(0).getId());
 		assertEquals(element.data, elements.get(0).data);
 		
 		elementsToSync =
 			localDAO.getElementsToSync();
 		syncedElements = 
 			new ArrayList<RemoteTuple<MockElement>>();
		for (LocalTuple<MockElement> elementToSync : elementsToSync) {
 			RemoteTuple<MockElement> remoteTuple = 
 				new RemoteTuple<UniversalDAOImplTest.MockElement>();
 			remoteTuple.setDeleted(true);
 			remoteTuple.setTimestamp(NEW_EXAMPLE_TIMESTAMP);
			remoteTuple.setElement(elementToSync.getElement());
 			syncedElements.add(remoteTuple);
 		}
 		localDAO.setSyncedElements(syncedElements);
 		
 		elements = 
 			localDAO.getAll();
 		assertEquals("Fail to update infrmation", 0, elements.size());
 	}
 	
 	@Test
 	public void testFailInsertData() {
 		MockElement element = new MockElement();
 		element.data = EXAMPLE_DATA;
 		universalDAO.insert(element);
 		
 		ArrayList<RemoteTuple<MockElement>> syncedElements = 
 			new ArrayList<RemoteTuple<MockElement>>();
 		localDAO.setSyncedElements(syncedElements);
 		
 		ArrayList<MockElement> elements = 
 			localDAO.getAll();
 		assertEquals(0, elements.size());
 	}
 }
