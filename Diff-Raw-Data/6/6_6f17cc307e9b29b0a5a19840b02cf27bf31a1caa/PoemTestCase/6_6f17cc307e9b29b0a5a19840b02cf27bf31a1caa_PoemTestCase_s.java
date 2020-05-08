 package org.melati.poem.test;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import org.melati.LogicalDatabase;
 import org.melati.poem.AccessToken;
 import org.melati.poem.PoemDatabase;
 import org.melati.poem.PoemTask;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 
 /**
  * A TestCase that runs in a Database session.
  * 
  * @author timp
  * @since 19-May-2006
  */
 public abstract class PoemTestCase extends TestCase implements Test {
 
   /**
    * the name of the test case
    */
   private String fName;
 
   protected PoemDatabase db = null;
 
   private String dbName = "melatijunit";
 
   /**
    * Constructor.
    */
   public PoemTestCase() {
     super();
     fName= null;
     db = null;
   }
 
   /**
    * Constructor.
    * @param name
    */
   public PoemTestCase(String name) {
     super(name);
     fName= name;
     db = null;
   }
 
   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     db = (PoemDatabase)LogicalDatabase.getDatabase(dbName);
   }
 
   /**
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
 /*    if (db != null && db.getCommittedConnection() != null)
       db.inSession(AccessToken.root, // HACK
               new PoemTask() {
                 public void run() {
                   try {
                     db.shutdown();
                    } catch (Throwable  e) {
                     e.fillInStackTrace();
                     throw new RuntimeException(e);
                   }
         }});
         */
     db = null;
   }
   
   /**
    * Run the test in a session.
    * 
    * @see junit.framework.TestCase#runTest()
    */
   protected void runTest() throws Throwable {
     assertNotNull(fName);
     try {
       // use getMethod to get all public inherited
       // methods. getDeclaredMethods returns all
       // methods of this class but excludes the
       // inherited ones.
       final Method runMethod = getClass().getMethod(fName, null);
       if (!Modifier.isPublic(runMethod.getModifiers())) {
         fail("Method \""+fName+"\" should be public");
       }
       // Ensures that we are invoking on 
       // the object that method belongs to.
       final Object _this = this;
       db.inSession(AccessToken.root, // HACK
               new PoemTask() {
                 public void run() {
                   try {
         runMethod.invoke(_this, new Class[0]);
                   } catch (Throwable  e) {
                     e.fillInStackTrace();
                     throw new RuntimeException(e);
                   }
         }});
     } catch (NoSuchMethodException e) {
       fail("Method \""+fName+"\" not found");
     }
   }
 
   /**
    * Gets the name of a TestCase.
    * @return returns a String
    */
   public String getName() {
     return fName;
   }
   /**
    * Sets the name of a TestCase.
    * @param name The name to set
    */
   public void setName(String name) {
     fName = name;
   }
   
   /**
    * @return Returns the dbName.
    */
   protected String getDbName() {
     return dbName;
   }
   
   /**
    * @param dbName The dbName to set.
    */
   protected void setDbName(String dbName) {
     this.dbName = dbName;
   }
 
   
 }
