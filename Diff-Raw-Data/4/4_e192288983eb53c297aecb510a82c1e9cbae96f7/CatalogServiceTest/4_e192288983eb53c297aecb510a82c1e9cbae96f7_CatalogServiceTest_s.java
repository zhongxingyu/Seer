 package de.hswt.hrm.catalog.service;
 
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 import de.hswt.hrm.catalog.dao.core.IActivityDao;
 import de.hswt.hrm.catalog.dao.core.ICatalogDao;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 import de.hswt.hrm.catalog.dao.core.ITargetDao;
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 
public class CatalogServiceTest {
 
 	private IActivityDao activityDao = mock(IActivityDao.class);
 	private ICurrentDao currentDao = mock(ICurrentDao.class);
 	private ITargetDao targetDao = mock(ITargetDao.class);
 	private ICatalogDao catalogDao = mock(ICatalogDao.class);
 
 	private CatalogService getMockedService() {
 		CatalogService service = new CatalogService(activityDao, currentDao,
 				targetDao, catalogDao);
 		return service;
 	}
 
 	@Test
 	public void testFindAllCatalogItem() throws DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findAllCatalogItem();
 
 		verify(activityDao, times(1)).findAll();
 		verify(currentDao, times(1)).findAll();
 		verify(targetDao, times(1)).findAll();
 	}
 
 	@Test
 	public void testFindAllActivity() throws DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findAllActivity();
 		verify(activityDao, times(1)).findAll();
 	}
 
 	@Test
 	public void testFindAllCurrent() throws DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findAllCurrent();
 		verify(currentDao, times(1)).findAll();
 	}
 
 	@Test
 	public void testFindAllTarget() throws DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findAllTarget();
 		verify(targetDao, times(1)).findAll();
 	}
 
 	@Test
 	public void testFindActivityById() throws ElementNotFoundException,
 			DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findActivityById(5);
 		verify(activityDao, times(1)).findById(5);
 	}
 
 	@Test
 	public void testFindCurrentById() throws ElementNotFoundException,
 			DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findCurrentById(5);
 		verify(currentDao, times(1)).findById(5);
 	}
 
 	@Test
 	public void testFindTargetById() throws ElementNotFoundException,
 			DatabaseException {
 		CatalogService service = getMockedService();
 
 		service.findTargetById(5);
 		verify(targetDao, times(1)).findById(5);
 	}
 
 	@Test
 	public void testInsertActivity() throws SaveException {
 		CatalogService service = getMockedService();
 		Activity activity = new Activity("Some Activity", "With a little text.");
 
 		service.insertActivity(activity);
 		verify(activityDao, times(1)).insert(activity);
 	}
 
 	@Test
 	public void testInsertCurrent() throws SaveException {
 		CatalogService service = getMockedService();
 		Current current = new Current("Some Current", "With a little text.");
 
 		service.insertCurrent(current);
 		verify(currentDao, times(1)).insert(current);
 	}
 
 	@Test
 	public void testInsertTarget() throws SaveException {
 		CatalogService service = getMockedService();
 		Target target = new Target("Some Target", "With a little text.");
 
 		service.insertTarget(target);
 		verify(targetDao, times(1)).insert(target);
 	}
 
 	public void testUpdateActivity() throws SaveException,
 			ElementNotFoundException {
 		CatalogService service = getMockedService();
 		Activity activity = new Activity(5, "Some Activity",
 				"With a little text.");
 
 		service.updateActivity(activity);
 		verify(activityDao, times(1)).update(activity);
 	}
 
 	@Test
 	public void testUpdateCurrent() throws SaveException,
 			ElementNotFoundException {
 		CatalogService service = getMockedService();
 		Current current = new Current(5, "Some Current", "With a little text.");
 
 		service.updateCurrent(current);
 		verify(currentDao, times(1)).update(current);
 	}
 
 	@Test
 	public void testUpdateTarget() throws SaveException,
 			ElementNotFoundException {
 		CatalogService service = getMockedService();
 		Target target = new Target(5, "Some Target", "With a little text.");
 
 		service.updateTarget(target);
 		verify(targetDao, times(1)).update(target);
 	}
 
 	@Test
 	public void testRefresh() throws ElementNotFoundException,
 			DatabaseException {
 		CatalogService service = getMockedService();
 
 		Activity activity = new Activity(5, "Some Activity",
 				"With a little text.");
 		when(activityDao.findById(anyInt())).thenReturn(activity);
 		service.refresh(activity);
 		verify(activityDao, times(1)).findById(5);
 
 		Current current = new Current(6, "Some Current", "With a little text.");
 		when(currentDao.findById(anyInt())).thenReturn(current);
 		service.refresh(current);
 		verify(currentDao, times(1)).findById(6);
 
 		Target target = new Target(7, "Some Target", "With a little text.");
 		when(targetDao.findById(anyInt())).thenReturn(target);
 		service.refresh(target);
 		verify(targetDao, times(1)).findById(7);
 	}
 }
