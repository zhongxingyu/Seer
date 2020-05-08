 package com.alexrnl.commons.database;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import com.alexrnl.commons.database.Dummy.DummyColumn;
 
 /**
  * Test suite for the {@link QueryGenerator} class.
  * @author Alex
  */
 public class QueryGeneratorTest {
 
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#getIDColumn(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testGetIDColumn () {
 		assertEquals(new Dummy().getEntityColumns().get(DummyColumn.ID), QueryGenerator.getIDColumn(new Dummy()));
 	}
 	
 	/**
	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#getIDColumn(com.alexrnl.commons.database.Entity)}.<br />
	 * This expect either an {@link java.lang.AssertionError} or a {@link com.alexrnl.commons.database.NoIdError}.
 	 */
	@Test(expected=Error.class)
 	public void testGetIDColumnNoIdError () {
 		QueryGenerator.getIDColumn(new Fake());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#escapeSpecialChars(java.lang.String)}.
 	 */
 	@Test
 	public void testEscapeSpecialChars () {
 		assertEquals("test", QueryGenerator.escapeSpecialChars("test"));
 		assertEquals("te''st", QueryGenerator.escapeSpecialChars("te'st"));
 		assertEquals("te\\\"st", QueryGenerator.escapeSpecialChars("te\"st"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#insert(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testInsertEntity () {
 		assertEquals("INSERT INTO Dummy(`id`,`name`) VALUES ", QueryGenerator.insert(new Dummy()));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#insert(com.alexrnl.commons.database.Entity, boolean)}.
 	 */
 	@Test
 	public void testInsertEntityBoolean () {
 		assertEquals("INSERT INTO Dummy(`id`,`name`) VALUES ", QueryGenerator.insert(new Dummy(), true));
 		assertEquals("INSERT INTO Dummy(`name`) VALUES ", QueryGenerator.insert(new Dummy(), false));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#delete(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testDeleteEntity () {
 		assertEquals("DELETE FROM Dummy WHERE id = '0'", QueryGenerator.delete(new Dummy(0)));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#delete(com.alexrnl.commons.database.Entity, boolean)}.
 	 */
 	@Test
 	public void testDeleteEntityBoolean () {
 		assertEquals("DELETE FROM Dummy WHERE id = '0'", QueryGenerator.delete(new Dummy(0), false));
 		assertEquals("DELETE FROM Dummy WHERE id = ?", QueryGenerator.delete(new Dummy(0), true));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#update(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testUpdate () {
 		assertEquals("UPDATE Dummy SET ", QueryGenerator.update(new Dummy()));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#whereID(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testWhereIDEntity () {
 		assertEquals(" WHERE id = '0'", QueryGenerator.whereID(new Dummy(0)));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#whereID(com.alexrnl.commons.database.Entity, java.lang.Object)}.
 	 */
 	@Test
 	public void testWhereIDEntityObject () {
 		assertEquals(" WHERE id = 'test'", QueryGenerator.whereID(new Dummy(), "test"));
 		assertEquals(" WHERE id = '0'", QueryGenerator.whereID(new Dummy(), 0));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#where(com.alexrnl.commons.database.Column, java.lang.Object)}.
 	 */
 	@Test
 	public void testWhereColumnObject () {
 		assertEquals(" WHERE id = ?", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), null));
 		assertEquals(" WHERE id = 'test'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), "test"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#where(com.alexrnl.commons.database.Column, java.lang.Object, boolean)}.
 	 */
 	@Test
 	public void testWhereColumnObjectBoolean () {
 		assertEquals(" WHERE id LIKE ?", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), null, true));
 		assertEquals(" WHERE id LIKE 'test'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), "test", true));
 		assertEquals(" WHERE id LIKE '0'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), 0, true));
 		assertEquals(" WHERE id = ?", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), null, false));
 		assertEquals(" WHERE id = 'test'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), "test", false));
 		assertEquals(" WHERE id = '0'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.ID), 0, false));
 		assertEquals(" WHERE name = 'test'", QueryGenerator.where(new Dummy().getEntityColumns().get(DummyColumn.NAME), "test", false));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#whereLike(com.alexrnl.commons.database.Column, java.lang.Object)}.
 	 */
 	@Test
 	public void testWhereLike () {
 		assertEquals(" WHERE id LIKE ?", QueryGenerator.whereLike(new Dummy().getEntityColumns().get(DummyColumn.ID), null));
 		assertEquals(" WHERE id LIKE 'test'", QueryGenerator.whereLike(new Dummy().getEntityColumns().get(DummyColumn.ID), "test"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#searchAll(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testSearchAll () {
 		assertEquals("SELECT * FROM Dummy", QueryGenerator.searchAll(new Dummy()));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#insertPrepared(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testInsertPrepared () {
 		assertEquals("INSERT INTO Dummy(`name`) VALUES (?)", QueryGenerator.insertPrepared(new Dummy()));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.database.QueryGenerator#updatePrepared(com.alexrnl.commons.database.Entity)}.
 	 */
 	@Test
 	public void testUpdatePrepared () {
 		assertEquals("UPDATE Dummy SET name = ? WHERE id = ?", QueryGenerator.updatePrepared(new Dummy()));
 	}
 }
