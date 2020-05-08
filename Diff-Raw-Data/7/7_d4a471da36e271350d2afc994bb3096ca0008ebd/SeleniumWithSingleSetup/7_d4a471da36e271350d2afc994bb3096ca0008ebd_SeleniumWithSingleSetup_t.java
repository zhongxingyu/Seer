 package nl.surfnet.bod.support;
 
 import nl.surfnet.bod.service.DatabaseTestHelper;
 
 import org.junit.Before;
import org.junit.BeforeClass;
 
 public abstract class SeleniumWithSingleSetup extends TestExternalSupport {
 
   private static boolean setupDone;
 
  @BeforeClass
  public static void clearSetupDone() {
    setupDone = false;
  }

   @Before
   public void setup() {
     if (setupDone) {
       return;
     }
 
     DatabaseTestHelper.clearSeleniumDatabaseSkipBaseData();
 
     setupInitialData();
 
     setupDone = true;
   }
 
   public abstract void setupInitialData();
 
   @Override
   public void clearDatabase() {
     // don't clear the database between tests..
   }
 }
