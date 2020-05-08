 package org.kalibro.core.persistence;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.persistence.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.tests.UnitTest;
 import org.mockito.InOrder;
 import org.mockito.Mockito;
 import org.powermock.reflect.Whitebox;
 
 public class RecordManagerTest extends UnitTest {
 
 	private static final Long ID = new Random().nextLong();
 	private static final String QUERY = "RecordManagerTest query";
 	private static final String MERGED = "RecordManagerTest merged";
 	private static final String UNMERGED = "RecordManagerTest unmerged";
 
 	private EntityManager entityManager;
 	private EntityTransaction transaction;
 
 	private RecordManager recordManager;
 
 	@Before
 	public void setUp() {
 		entityManager = mock(EntityManager.class);
 		transaction = mock(EntityTransaction.class);
 		recordManager = new RecordManager(entityManager);
 		when(entityManager.getTransaction()).thenReturn(transaction);
 		when(entityManager.merge(UNMERGED)).thenReturn(MERGED);
 	}
 
 	@Test
 	public void shouldCreateQuery() {
 		Query query = mock(Query.class);
 		when(entityManager.createQuery(QUERY)).thenReturn(query);
 		assertSame(query, recordManager.createQuery(QUERY));
 		verify(entityManager).clear();
 	}
 
 	@Test
 	public void shouldCreateTypedQuery() {
 		TypedQuery<String> query = mock(TypedQuery.class);
 		when(entityManager.createQuery(QUERY, String.class)).thenReturn(query);
 		assertSame(query, recordManager.createQuery(QUERY, String.class));
 		verify(entityManager).clear();
 	}
 
 	@Test
 	public void shouldCreateProcedureQuery() {
 		StoredProcedureQuery query = mock(StoredProcedureQuery.class);
		when(entityManager.createNamedStoredProcedureQuery(QUERY)).thenReturn(query);
 		assertSame(query, recordManager.createProcedureQuery(QUERY));
 		verify(entityManager).clear();
 	}
 
 	@Test
 	public void shouldGetById() {
 		when(entityManager.find(String.class, ID)).thenReturn(MERGED);
 		assertSame(MERGED, recordManager.getById(ID, String.class));
 		verify(entityManager).clear();
 	}
 
 	@Test
 	public void shouldMergeAndSave() throws Exception {
 		assertEquals(MERGED, recordManager.save(UNMERGED));
 		verify(entityManager).clear();
 		verifyWithinTransaction("persist");
 	}
 
 	@Test
 	public void shouldMergeAndSaveCollection() throws Exception {
 		recordManager.saveAll(list(UNMERGED));
 		verify(entityManager).clear();
 		verifyWithinTransaction("persist");
 	}
 
 	@Test
 	public void shouldDoNothinWhenSavingEmptyCollection() {
 		recordManager.saveAll(new ArrayList<String>());
 		verifyZeroInteractions(entityManager);
 	}
 
 	@Test
 	public void shouldRemoveById() throws Exception {
 		when(entityManager.find(String.class, ID)).thenReturn(MERGED);
 		recordManager.removeById(ID, String.class);
 		verify(entityManager).clear();
 		verifyWithinTransaction("remove");
 	}
 
 	private void verifyWithinTransaction(String method) throws Exception {
 		InOrder order = Mockito.inOrder(transaction, entityManager, transaction);
 		order.verify(transaction).begin();
 		Whitebox.invokeMethod(order.verify(entityManager), method, MERGED);
 		order.verify(transaction).commit();
 	}
 
 	@Test
 	public void shouldCloseEntityManagerOnFinalize() throws Throwable {
 		when(entityManager.isOpen()).thenReturn(true);
 		recordManager.finalize();
 		verify(entityManager).close();
 	}
 }
