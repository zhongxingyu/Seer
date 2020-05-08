 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.melati.poem.AppBugPoemException;
 import org.melati.poem.CachedSelection;
 import org.melati.poem.Column;
 import org.melati.poem.DefinitionSource;
 import org.melati.poem.DisplayLevel;
 import org.melati.poem.Field;
 import org.melati.poem.NullTypeMismatchPoemException;
 import org.melati.poem.Persistent;
 import org.melati.poem.SQLPoemType;
 import org.melati.poem.Searchability;
 import org.melati.poem.StandardIntegrityFix;
 import org.melati.poem.Table;
 import org.melati.poem.TroidPoemType;
 import org.melati.poem.User;
 import org.melati.poem.Column.SettingException;
 import org.melati.poem.util.EnumUtils;
 
 /**
  * @author timp
  *
  */
 public class ColumnTest extends PoemTestCase {
 
   /**
    * Constructor for ColumnTest.
    * @param name
    */
   public ColumnTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#setUp()
    */
   protected void setUp()
       throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#tearDown()
    */
   protected void tearDown()
       throws Exception {
     super.tearDown();
   }
 
   /**
    * @see org.melati.poem.Column#
    *      Column(Table, String, SQLPoemType, DefinitionSource)
    */
   public void testColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getDatabase()
    */
   public void testGetDatabase() {
     assertEquals(getDb(), 
         getDb().getUserTable().troidColumn().getDatabase());
   }
 
   /**
    * @see org.melati.poem.Column#getTable()
    */
   public void testGetTable() {
     assertEquals(getDb().getUserTable(), 
         getDb().getUserTable().troidColumn().getTable());
   }
 
   /**
    * @see org.melati.poem.Column#getName()
    */
   public void testGetName() {
     assertEquals("id", 
         getDb().getUserTable().troidColumn().getName());
   }
 
   /**
    * @see org.melati.poem.Column#quotedName()
    */
   public void testQuotedName() {
     assertEquals(getDb().quotedName("id"), 
         getDb().getUserTable().troidColumn().quotedName());
   }
 
   /**
    * @see org.melati.poem.Column#fullQuotedName()
    */
   public void testFullQuotedName() {
     assertEquals(getDb().quotedName("user")+ "." + getDb().quotedName("id"), 
         getDb().getUserTable().troidColumn().fullQuotedName());
 
   }
 
   /**
    * @see org.melati.poem.Column#getDisplayName()
    */
   public void testGetDisplayName() {
     assertEquals("Id", 
         getDb().getUserTable().troidColumn().getDisplayName());
 
   }
 
   /**
    * @see org.melati.poem.Column#getDescription()
    */
   public void testGetDescription() {
     assertEquals("The Table Row Object ID", 
         getDb().getUserTable().troidColumn().getDescription());
   }
 
   /**
    * @see org.melati.poem.Column#getColumnInfo()
    */
   public void testGetColumnInfo() {
     assertEquals("columninfo/0", 
         getDb().getUserTable().troidColumn().getColumnInfo().toString());
   }
 
   /**
    * @see org.melati.poem.Column#getDisplayLevel()
    */
   public void testGetDisplayLevel() {
     assertEquals(DisplayLevel.detail, 
         getDb().getUserTable().troidColumn().getDisplayLevel());
     assertEquals(DisplayLevel.primary, 
         getDb().getUserTable().getColumn("name").getDisplayLevel());
     assertEquals(DisplayLevel.summary, 
         getDb().getUserTable().getColumn("login").getDisplayLevel());
   }
 
   /**
    * @see org.melati.poem.Column#setDisplayLevel(DisplayLevel)
    */
   public void testSetDisplayLevel() {
     assertEquals(DisplayLevel.summary, 
         getDb().getUserTable().getColumn("login").getDisplayLevel());
     getDb().getUserTable().getColumn("login").setDisplayLevel(DisplayLevel.detail);
     assertEquals(DisplayLevel.detail, 
         getDb().getUserTable().getColumn("login").getDisplayLevel());
     getDb().getUserTable().getColumn("login").setDisplayLevel(DisplayLevel.summary);
     assertEquals(DisplayLevel.summary, 
         getDb().getUserTable().getColumn("login").getDisplayLevel());
   }
 
   /**
    * @see org.melati.poem.Column#getSearchability()
    */
   public void testGetSearchability() {
     assertEquals(Searchability.yes, 
         getDb().getUserTable().troidColumn().getSearchability());
     assertEquals(Searchability.primary, 
         getDb().getUserTable().getColumn("name").getSearchability());
     assertEquals(Searchability.yes, 
         getDb().getUserTable().getColumn("login").getSearchability());
     assertEquals(Searchability.no, 
         getDb().getUserTable().getColumn("password").getSearchability());
 
   }
 
   /**
    * @see org.melati.poem.Column#setSearchability(Searchability)
    */
   public void testSetSearchability() {
     assertEquals(Searchability.yes, 
         getDb().getUserTable().getColumn("login").getSearchability());
     getDb().getUserTable().getColumn("login").setSearchability(Searchability.no);
     assertEquals(Searchability.no, 
         getDb().getUserTable().getColumn("login").getSearchability());
     getDb().getUserTable().getColumn("login").setSearchability(Searchability.yes);
     assertEquals(Searchability.yes, 
         getDb().getUserTable().getColumn("login").getSearchability());
 
   }
 
   /**
    * @see org.melati.poem.Column#getUserEditable()
    */
   public void testGetUserEditable() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getUserCreateable()
    */
   public void testGetUserCreateable() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getSQLType()
    */
   public void testGetSQLType() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getType()
    */
   public void testGetType() {
 
   }
 
   /**
    * @see org.melati.poem.Column#isTroidColumn()
    */
   public void testIsTroidColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Column#isDeletedColumn()
    */
   public void testIsDeletedColumn() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getIndexed()
    */
   public void testGetIndexed() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getUnique()
    */
   public void testGetUnique() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getIntegrityFix()
    */
   public void testGetIntegrityFix() {
     assertEquals(StandardIntegrityFix.prevent,
         getDb().getUserTable().troidColumn().getIntegrityFix());
   }
 
   /**
    * Set the integrityFix and set it back again.
    * @see org.melati.poem.Column#setIntegrityFix(StandardIntegrityFix)
    */
   public void testSetIntegrityFix() {
     assertEquals(StandardIntegrityFix.prevent,
         getDb().getUserTable().troidColumn().getIntegrityFix());
     getDb().getUserTable().troidColumn().setIntegrityFix(StandardIntegrityFix.delete);
     assertEquals(StandardIntegrityFix.delete,
         getDb().getUserTable().troidColumn().getIntegrityFix());
     getDb().getUserTable().troidColumn().setIntegrityFix(StandardIntegrityFix.prevent);
     assertEquals(StandardIntegrityFix.prevent,
         getDb().getUserTable().troidColumn().getIntegrityFix());
 
   }
 
   /**
    * @see org.melati.poem.Column#getRenderInfo()
    */
   public void testGetRenderInfo() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getWidth()
    */
   public void testGetWidth() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getHeight()
    */
   public void testGetHeight() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getDisplayOrderPriority()
    */
   public void testGetDisplayOrderPriority() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getSortDescending()
    */
   public void testGetSortDescending() {
 
   }
 
   /**
    * @see org.melati.poem.Column#toString()
    */
   public void testToString() {
 
   }
 
   /**
    * @see org.melati.poem.Column#dump()
    */
   public void testDump() {
     getDb().getUserTable().troidColumn().dump();
   }
 
   /**
    * @see org.melati.poem.Column#eqClause(Object)
    */
   public void testEqClause() {
     assertEquals(getDb().getDbms().getQuotedName("user") + 
         "." + getDb().getDbms().getQuotedName("id") + " IS NULL", 
             getDb().getUserTable().troidColumn().eqClause(null));
     assertEquals(getDb().getDbms().getQuotedName("user") + "." + 
         getDb().getDbms().getQuotedName("id") + " = 1", 
                  getDb().getUserTable().troidColumn().eqClause(new Integer(1)));
   }
 
   /**
    * @see org.melati.poem.Column#selectionWhereEq(Object)
    */
   public void testSelectionWhereEq() {
     assertEquals(new Integer(69), 
         new Integer(EnumUtils.vectorOf(
             getDb().getColumnInfoTable().getHeightColumn().
             selectionWhereEq(new Integer(1))).size()));
     try {
       getDb().getColumnInfoTable().getHeightColumn().
           selectionWhereEq(null);
       fail("Should have bombed");
     } catch (NullTypeMismatchPoemException e) {
       e = null;
     }
   }
 
   /**
    * @see org.melati.poem.Column#firstWhereEq(Object)
    */
   public void testFirstWhereEq() {
     assertNull(getDb().getColumnInfoTable().getHeightColumn().
         firstWhereEq(new Integer(0)));
     assertNull(getDb().getColumnInfoTable().getHeightColumn().
         firstWhereEq(new Integer(2)));
     assertEquals("columninfo/0", 
         getDb().getColumnInfoTable().getHeightColumn().
             firstWhereEq(new Integer(1)).toString());
   }
 
   /**
    * @see org.melati.poem.Column#cachedSelectionWhereEq(Object)
    */
   public void testCachedSelectionWhereEq() {
    int queries = getDb().getQueryCount();
     CachedSelection cs = getDb().getColumnInfoTable().getHeightColumn().
         cachedSelectionWhereEq(new Integer(1));
     int queries2 = getDb().getQueryCount();
    assertEquals(queries, queries2);
     assertEquals(69,cs.count());
     int queries3 = getDb().getQueryCount();
     assertEquals(queries2 + 1, queries3);
   }
 
   /**
    * @see org.melati.poem.Column#getRaw(Persistent)
    */
   public void testGetRaw() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getRaw_unsafe(Persistent)
    */
   public void testGetRaw_unsafe() {
 
   }
 
   /**
    * @see org.melati.poem.Column#setRaw(Persistent, Object)
    */
   public void testSetRaw() {
 
   }
 
   /**
    * @see org.melati.poem.Column#setRaw_unsafe(Persistent, Object)
    */
   public void testSetRaw_unsafe() {
 
   }
 
   /**
    * @see org.melati.poem.Column#getCooked(Persistent)
    */
   public void testGetCooked() {
 
   }
 
   /**
    * @see org.melati.poem.Column#setCooked(Persistent, Object)
    */
   public void testSetCooked() {
 
   }
 
   /**
    * @see org.melati.poem.Column#load_unsafe(ResultSet, int, Persistent)
    */
   public void testLoad_unsafe() {
 
   }
 
   /**
    * @see org.melati.poem.Column#save_unsafe(Persistent, PreparedStatement, int)
    */
   public void testSave_unsafe() {
 
   }
 
   /**
    * @see org.melati.poem.Column#asEmptyField()
    */
   public void testAsEmptyField() {
     Field f = getDb().getUserTable().troidColumn().asEmptyField();
     assertEquals("id",f.getName());
     assertEquals("Id",f.getDisplayName());
     assertEquals("The Table Row Object ID",f.getDescription());
     assertEquals(1,f.getHeight());
     assertEquals(20,f.getWidth());
     assertNull(f.getRaw());
     try { 
       f.getCooked();
       fail("Should have bombed");
     } catch (NullTypeMismatchPoemException e) {
       e = null;
     }
     assertTrue(f.getIndexed());
     assertFalse(f.getUserEditable());
     assertFalse(f.getUserCreateable());
     assertNull(f.getRenderInfo());
     assertTrue(f.getType() instanceof TroidPoemType);
   }
 
   /**
    * @see org.melati.poem.Column#setRawString(Persistent, String)
    */
   public void testSetRawString() {
     User admin = getDb().getUserTable().administratorUser();
     try { 
       getDb().getUserTable().troidColumn().setRawString(admin, "one");
       fail("Should have bombed");
     } catch (SettingException e) {
       e = null;
     }
     getDb().getUserTable().getNameColumn().setRawString(admin, "Admin");
     assertEquals("Admin", admin.getName());
     getDb().getUserTable().getNameColumn().setRawString(admin, "Melati database administrator");
   }
 
   /**
    * @see org.melati.poem.Column#referencesTo(Persistent)
    */
   public void testReferencesTo() {
     Column userTroidColumn = getDb().getUserTable().troidColumn();
     User admin = getDb().getUserTable().administratorUser();
     assertEquals("", EnumUtils.concatenated("|", userTroidColumn.referencesTo(admin)));
     Column userColumn = getDb().getGroupMembershipTable().getUserColumn();
     assertEquals("groupmembership/0", 
                  EnumUtils.concatenated("|", userColumn.referencesTo(admin)));    
   }
 
   /**
    * @see org.melati.poem.Column#ensure(Persistent)
    */
   public void testEnsure() {
     User fred = (User)getDb().getUserTable().newPersistent();
     fred.setName("Fred");
     fred.setLogin("fred");
     fred.setPassword("fred");
     Column userNameColumn = getDb().getUserTable().getColumn("name");
     User ensured = (User)userNameColumn.ensure(fred);
     assertEquals("Fred", ensured.getName());
     fred.delete();
   }
 
   /**
    * @see org.melati.poem.Column#firstFree(String)
    */
   public void testFirstFree() {
     Column userTroidColumn = getDb().getUserTable().troidColumn();
     assertEquals(2, userTroidColumn.firstFree(null));    
 
     try {
       Column userNameColumn = getDb().getUserTable().getColumn("name");
       userNameColumn.firstFree(null);
       fail("Should have bombed");
     } catch (AppBugPoemException e) {
       e = null;
     }
     // Think that these two show what could go wrong
     assertEquals(0, userTroidColumn.firstFree("ID > 1"));    
     assertEquals(1, userTroidColumn.firstFree("ID < 1"));    
 
   }
 
 }
