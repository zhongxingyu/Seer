 package de.hswt.hrm.component.dao.jdbc;
 
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.*;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.junit.Test;
 
 import de.hswt.hrm.catalog.dao.core.ICatalogDao;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.component.dao.core.ICategoryDao;
 import de.hswt.hrm.component.dao.core.IComponentDao;
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class ComponentDaoTest extends AbstractDatabaseTest {
 
 	private ICategoryDao getCategoryDao() {
 		ICatalogDao catalogDao = mock(ICatalogDao.class);
 		return new CategoryDao(catalogDao);
 	}
 	
 	private IComponentDao getInjectedDao() {
 		ICategoryDao categoryDao = getCategoryDao();
 		ComponentDao componentDao = new ComponentDao(categoryDao);
 		
 		return componentDao;
 	}
 	
 	private Component getComponent() throws SaveException {
 		ICategoryDao categoryDao = getCategoryDao();
 		Category cat = new Category("Some Category", 2, 2, 1, true);
 		cat = categoryDao.insert(cat);
 		
 		Component comp = new Component("Testcomp", new byte[5], new byte[5], new byte[5], 
 				new byte[5], 1, true);
 		
 		comp.setCategory(cat);
 		return comp;
 	}
 	
 	private boolean equals(final byte[] a, final byte[] b) {
 		if (a.length != b.length) {
 			return false;
 		}
 		
 		for (int i = 0; i < a.length; i++) {
 			if (a[i] != b[i]) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private void compareComponents(final Component expected, final Component actual) {
 		assertEquals("BoolRating not set correctly.", expected.getBoolRating(), actual.getBoolRating());
 		assertEquals("Category not set correctly.", expected.getCategory().orNull(), actual.getCategory().orNull());
 		assertTrue("DownUpImage not set correctly.", equals(expected.getDownUpImage(), actual.getDownUpImage()));
 		assertTrue("LeftRightImage not set correctly.", equals(expected.getLeftRightImage(), actual.getLeftRightImage()));
 		assertEquals("Name not set correctly.", expected.getName(), actual.getName());
 		assertEquals("Quantifier not set correctly.", expected.getQuantifier(), actual.getQuantifier());
 		assertTrue("RightLeftImage not set correctly.", equals(expected.getRightLeftImage(), actual.getRightLeftImage()));
 		assertTrue("UpDownImage not set correctly.", equals(expected.getUpDownImage(), actual.getUpDownImage()));
 	}
 	
 	@Test
 	public void testInsert() throws ElementNotFoundException, DatabaseException {
 		IComponentDao componentDao = getInjectedDao();
 		Component component = getComponent();
 		
 		Component parsed = componentDao.insert(component);
 		assertTrue("ID not set correctly.", parsed.getId() >= 0);
 		compareComponents(component, parsed);
 		
 		Component fromDb = componentDao.findById(parsed.getId());
 		assertEquals("ID not set correctly.", parsed.getId(), fromDb.getId());
 		compareComponents(component, fromDb);
 	}
 	
 	@Test
 	public void testAddAttribute() throws DatabaseException {
 		IComponentDao componentDao = getInjectedDao();
 		Component component = getComponent();
 		Component parsed = componentDao.insert(component);
 		final String attribute1 = "Some Attribute";
 		final String attribute2 = "Some Other Attribute";
 		
 		componentDao.addAttribute(parsed, attribute1);
 		componentDao.addAttribute(parsed, attribute2);
 		
		Collection<Attribute> attributes = componentDao.findAttributesByComponent(parsed);
 		assertEquals("Wrong count of attributes retrieved.", 2, attributes.size());
 		
 		// Check if retrieved attributes equal to inserted ones
		
 		Attribute[] attrs = attributes.toArray(new Attribute[0]);
		if (!attrs[0].getName().equals(attribute1) && !attrs[0].getName().equals(attribute2)
				|| !attrs[1].getName().equals(attribute1) && !attrs[1].getName().equals(attribute2)) {
 			fail("Wrong attribute retrieved");
 		}
 		
 		// Ensure that we don't have retrieved the same attribute twice
 		assertFalse("Same attribute retrieved twice.", attrs[0].equals(attrs[1]));
 	}
 }
