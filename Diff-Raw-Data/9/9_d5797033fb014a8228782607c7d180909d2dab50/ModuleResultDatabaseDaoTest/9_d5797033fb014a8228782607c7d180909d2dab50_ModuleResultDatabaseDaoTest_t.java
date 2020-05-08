 package org.kalibro.core.persistence;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.junit.Test;
 import org.kalibro.Granularity;
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.ModuleResultRecord;
 import org.mockito.InOrder;
 import org.mockito.Mockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 
 @PrepareForTest({ModuleResult.class, ModuleResultDatabaseDao.class})
 public class ModuleResultDatabaseDaoTest extends
 	DatabaseDaoTestCase<ModuleResult, ModuleResultRecord, ModuleResultDatabaseDao> {
 
 	private static final Long ID = new Random().nextLong();
 	private static final Long TIME = new Random().nextLong();
 	private static final Date DATE = new Date(TIME);
 
 	@Test
 	public void shouldGetChildren() {
 		assertDeepEquals(set(entity), dao.childrenOf(ID));
 
 		verify(dao).createRecordQuery("moduleResult.parent = :parentId");
 		verify(query).setParameter("parentId", ID);
 	}
 
 	@Test
 	public void shouldGetHistory() {
 		Module module = mockModule();
 
 		TypedQuery<Object[]> historyQuery = mock(TypedQuery.class);
 		doReturn(historyQuery).when(dao).createQuery(anyString(), eq(Object[].class));
 
 		List<Object[]> results = new ArrayList<Object[]>();
 		results.add(new Object[]{TIME, record});
 		when(historyQuery.getResultList()).thenReturn(results);
 
 		SortedMap<Date, ModuleResult> history = dao.historyOf(ID);
 		assertEquals(1, history.size());
 		assertDeepEquals(entity, history.get(DATE));
 		verify(historyQuery).setParameter("moduleName", module.getName()[0]);
		verify(historyQuery).setParameter("moduleResultId", ID);
 	}
 
 	private Module mockModule() {
 		dao = mock(ModuleResultDatabaseDao.class, Mockito.CALLS_REAL_METHODS);
 		Module module = new Module(Granularity.CLASS, "ModuleResultDatabaseDaoTest");
 		doReturn(entity).when(dao).get(ID);
 		when(entity.getModule()).thenReturn(module);
 		return module;
 	}
 
 	@Test
 	public void shouldSave() throws Exception {
 		assertSame(entity, dao.save(entity, ID));
 		verifyNew(ModuleResultRecord.class).withArguments(entity, ID);
 		verify(dao).save(record);
 	}
 
 	@Test
 	public void shouldPrepareResultForModule() throws Exception {
 		Module module = new Module(Granularity.PACKAGE, "org");
 		String where = "moduleResult.processing = :processingId AND moduleResult.moduleName = :module";
 		String where2 = "moduleResult.processing = :processingId AND moduleResult.moduleGranularity = :module";
 		doReturn(false).when(dao).exists("WHERE " + where, "processingId", ID, "module", "org");
 		doReturn(false).when(dao).exists("WHERE " + where2, "processingId", ID, "module", "SOFTWARE");
 		prepareQuery(where);
 		prepareQuery(where2);
 
 		Module preparedModule = mock(Module.class);
 		whenNew(ModuleResultRecord.class)
 			.withParameterTypes(Module.class, Long.class, Long.class)
 			.withArguments(any(Module.class), any(Long.class), eq(ID)).thenReturn(record);
 		doReturn(null).when(dao).save(record);
 		when(entity.getModule()).thenReturn(preparedModule);
 
 		assertSame(entity, dao.prepareResultFor(module, ID));
 		verifyCallsInOrder(preparedModule);
 	}
 
 	private void prepareQuery(String where) {
 		doReturn(query).when(dao).createRecordQuery(where);
 	}
 
 	private void verifyCallsInOrder(Module preparedModule) {
 		InOrder order = Mockito.inOrder(dao, query, dao, query, preparedModule);
 		order.verify(dao).save(record);
 		order.verify(query).setParameter("processingId", ID);
 		order.verify(query).setParameter("module", "SOFTWARE");
 		order.verify(query).getSingleResult();
 		order.verify(dao).save(record);
 		order.verify(query).setParameter("processingId", ID);
 		order.verify(query).setParameter("module", "org");
 		order.verify(query).getSingleResult();
 		order.verify(preparedModule).setGranularity(Granularity.PACKAGE);
 	}
 }
