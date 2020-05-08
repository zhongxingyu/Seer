 package org.kalibro.core.persistence.database;
 
 import static org.junit.Assert.*;
import static org.kalibro.core.model.RangeFixtures.*;
import static org.kalibro.core.model.RangeLabel.*;
 import static org.powermock.api.mockito.PowerMockito.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.TestCase;
 import org.kalibro.core.model.Range;
 import org.kalibro.core.persistence.database.entities.RangeRecord;
 import org.mockito.Mockito;
 
 public class DatabaseDaoTest extends TestCase {
 
 	private DatabaseManager databaseManager;
 
 	private RangeDatabaseDao dao;
 
 	@Before
 	public void setUp() {
 		databaseManager = mock(DatabaseManager.class);
 		dao = new RangeDatabaseDao(databaseManager);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void testGetAllNames() {
		String queryText = "SELECT x.name FROM \"Range\" x ORDER BY x.name";
 		Query<String> query = mock(Query.class);
 		List<String> names = Arrays.asList("4", "2");
 		when(databaseManager.createQuery(queryText, String.class)).thenReturn(query);
 		when(query.getResultList()).thenReturn(names);
 
 		assertSame(names, dao.getAllNames());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void testConfirmEntity() {
 		String queryText = "SELECT 1 FROM \"Range\" x WHERE x.name = :name";
 		Query<String> query = mock(Query.class);
 		when(databaseManager.createQuery(queryText, String.class)).thenReturn(query);
 
 		when(query.getResultList()).thenReturn(Arrays.asList("1"));
 		assertTrue(dao.hasEntity("42"));
 		Mockito.verify(query).setParameter("name", "42");
 
 		when(query.getResultList()).thenReturn(new ArrayList<String>());
 		assertFalse(dao.hasEntity("x"));
 		Mockito.verify(query).setParameter("name", "x");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void testGetByName() {
 		String queryText = "SELECT x FROM \"Range\" x WHERE x.name = :name";
 		Query<RangeRecord> query = mock(Query.class);
 		Range range = newRange("amloc", BAD);
 		String noResultMessage = "There is no range named '42'";
 		when(databaseManager.createQuery(queryText, RangeRecord.class)).thenReturn(query);
 		when(query.getSingleResult(noResultMessage)).thenReturn(new RangeRecord(range, null));
 
 		assertDeepEquals(range, dao.getByName("42"));
 		Mockito.verify(query).setParameter("name", "42");
 	}
 
 	private class RangeDatabaseDao extends DatabaseDao<Range, RangeRecord> {
 
 		public RangeDatabaseDao(DatabaseManager databaseManager) {
 			super(databaseManager, RangeRecord.class);
 		}
 	}
 }
