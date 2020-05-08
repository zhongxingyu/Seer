 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.util.Enumeration;
 
 import org.melati.poem.AccessPoemException;
 import org.melati.poem.Capability;
 import org.melati.poem.Column;
 import org.melati.poem.FieldSet;
 import org.melati.poem.PoemTask;
 import org.melati.poem.PoemThread;
 import org.melati.poem.PreparedTailoredQuery;
 import org.melati.poem.Table;
 import org.melati.poem.User;
 import org.melati.poem.util.EnumUtils;
 
 /**
  * @author timp
  * @since 22 Jan 2007
  * 
  */
 public class PreparedTailoredQueryTest extends EverythingTestCase {
 
   /**
    * Constructor.
    * 
    * @param name
    */
   public PreparedTailoredQueryTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * 
    * @see org.melati.poem.test.PoemTestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * 
    * @see org.melati.poem.test.PoemTestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.poem.PreparedTailoredQuery#selection()}.
    */
   public void testSelection() {
     EverythingDatabase db = (EverythingDatabase)getDb();
     Capability spyMaster = db.getCapabilityTable().ensure("spyMaster");
     final Capability moneyPenny = db.getCapabilityTable().ensure("moneyPenny");
 
     User spy = (User)db.getUserTable().newPersistent();
     spy.setLogin("spy");
     spy.setName("Spy");
     spy.setPassword("spy");
     spy.makePersistent();
 
     Protected spyMission = (Protected)db.getProtectedTable().newPersistent();
     spyMission.setCanRead(moneyPenny);
     spyMission.setCanSelect(moneyPenny);
     spyMission.setCanWrite(moneyPenny);
     spyMission.setCanDelete(spyMaster);
     spyMission.setSpy(spy);
     spyMission.setMission("impossible");
     spyMission.setDeleted(false);
     spyMission.makePersistent();
 
     final Column canReadColumn = db.getProtectedTable().getCanReadColumn();
     final PreparedTailoredQuery ptq = new PreparedTailoredQuery(
             new Column[] { canReadColumn }, new Table[0], canReadColumn
                     .fullQuotedName()
                     + "=" + moneyPenny.troid(), null);
     assertEquals(new Integer(1), new Integer(EnumUtils
             .vectorOf(ptq.selection()).size()));
     Enumeration en = ptq.selection();
     while (en.hasMoreElements()) {
       Object ne = en.nextElement();
       System.err.println("FieldSet:" + ne);
     }
 
     PoemTask readAsGuest = new PoemTask() {
       public void run() {
         try {
           Enumeration en = ptq.selection();
           assertEquals(new Integer(1), new Integer(EnumUtils.vectorOf(en)
                   .size()));
           en = ptq.selection();
           while (en.hasMoreElements()) {
             System.err.println(en.nextElement());
           }
           fail("Should have blown up");
         } catch (AccessPoemException e) {
           e = null;
         }
 
       }
     };
 
     PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest);
 
     final Column missionColumn = db.getProtectedTable().getMissionColumn();
     assertEquals("moneyPenny", spyMission.getCanRead().getName());
     final PreparedTailoredQuery ptq2 = new PreparedTailoredQuery(new Column[] {
         missionColumn, db.getUserTable().getPasswordColumn() },
             new Table[] { db.getUserTable() }, missionColumn.fullQuotedName()
                     + " = 'impossible' AND "
                     + db.getProtectedTable().getSpyColumn().fullQuotedName()
                     + " = " + db.getUserTable().troidColumn().fullQuotedName(),
             null);
     PoemTask readAsGuest2 = new PoemTask() {
       public void run() {
         Enumeration en = ptq2.selection();
         try {
          assertEquals(new Integer(1), new Integer(EnumUtils.vectorOf(
                  ptq2.selection()).size()));
           en = ptq2.selection();
           while (en.hasMoreElements()) {
             FieldSet tuple = (FieldSet)en.nextElement();
             System.err.println(tuple);
           }
           fail("Should have blown up");
         } catch (AccessPoemException e) {
           e = null;
         }
 
       }
     };
 
     PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest2);
 
     // Now remove row's capability and set Users capability;
     // (as Protected does not get checked in {@link
     // TailoredResultSetEnumeration}).
     spyMission.setCanRead(null);
     db.getUserTable().getTableInfo().setDefaultcanread(moneyPenny);
 
     try {
       PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest2);
     } catch (AccessPoemException e) {
       e = null;
     }
 
     // Check that table level protection is used if row level is missing
     db.getProtectedTable().getTableInfo().setDefaultcanread(moneyPenny);
     try {
       PoemThread.withAccessToken(db.guestAccessToken(), readAsGuest2);
     } catch (AccessPoemException e) {
       e = null;
     }
 
     // cleanup
     db.getProtectedTable().getTableInfo().setDefaultcanread(null);
     db.getUserTable().getTableInfo().setDefaultcanread(null);
     spyMission.delete();
     spy.delete();
     spyMaster.delete();
     moneyPenny.delete();
 
   }
 
   /**
    * Test method for
    * {@link org.melati.poem.PreparedTailoredQuery#selection_firstRaw()}.
    */
   public void testSelection_firstRaw() {
 
   }
 
   /**
    * Test method for
    * {@link org.melati.poem.PreparedTailoredQuery#PreparedTailoredQuery(java.lang.String, org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testPreparedTailoredQueryStringColumnArrayTableArrayStringString() {
 
   }
 
   /**
    * Test method for
    * {@link org.melati.poem.PreparedTailoredQuery#PreparedTailoredQuery(org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testPreparedTailoredQueryColumnArrayTableArrayStringString() {
 
   }
 
   /**
    * Test method for
    * {@link org.melati.poem.TailoredQuery#TailoredQuery(org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testTailoredQueryColumnArrayTableArrayStringString() {
 
   }
 
   /**
    * Test method for
    * {@link org.melati.poem.TailoredQuery#TailoredQuery(java.lang.String, org.melati.poem.Column[], org.melati.poem.Table[], java.lang.String, java.lang.String)}.
    */
   public void testTailoredQueryStringColumnArrayTableArrayStringString() {
 
   }
 
   /**
    * Test method for {@link org.melati.poem.TailoredQuery#toString()}.
    */
   public void testToString() {
 
   }
 
 }
