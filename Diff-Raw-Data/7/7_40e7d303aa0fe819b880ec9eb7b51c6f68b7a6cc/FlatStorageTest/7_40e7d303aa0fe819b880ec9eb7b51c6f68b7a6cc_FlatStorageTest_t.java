 package com.flexive.tests.embedded.persistence;
 
 import static com.flexive.core.flatstorage.FxFlatStorage.FxFlatColumnType;
 import com.flexive.core.flatstorage.FxFlatStorageInfo;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.flexive.tests.embedded.TestUsers;
 import org.apache.commons.lang.RandomStringUtils;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Tests for the Flat Storage
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "content", "flatstorage"})
 public class FlatStorageTest {
     private boolean testsEnabled = false;
 
     private ContentEngine co;
     private ACLEngine acl;
     private TypeEngine type;
     private AssignmentEngine ass;
     private DivisionConfigurationEngine dce;
 
     final static String TEST_STORAGE = "FX_CONTENT_FLAT_TEST";
     final static String TEST_STORAGE_DESCRIPTION = "Test storage";
 
     public static final String TEST_TYPE = "TEST_TYPE_" + RandomStringUtils.random(16, true, true);
 
     /**
      * setup...
      *
      * @throws Exception on errors
      */
     @BeforeClass
     public void beforeClass() throws Exception {
         co = EJBLookup.getContentEngine();
         acl = EJBLookup.getAclEngine();
         type = EJBLookup.getTypeEngine();
         ass = EJBLookup.getAssignmentEngine();
         dce = EJBLookup.getDivisionConfigurationEngine();
         login(TestUsers.SUPERVISOR);
         testsEnabled = dce.isFlatStorageEnabled();
         if (testsEnabled)
             setupStorage();
     }
 
     /**
      * Setup testing structure.
      * <p/>
      * Hierarchy looks like this:
      * * TestProperty1 (String 1024)[0..1] *
      * * TestProperty2 (Text) [0..1] *
      * * TestProperty3 (String 1024) [0..5]
      * * TestProperty4 (String 1024) [0..N]
      * * TestProperty5 (Double) [0..1] *
      * * TestProperty6 (Number) [0..1] *
      * * TestProperty7 (Text) [1..1] *
      * * TestGroup1[0..2]
      * * * TestProperty1_1 (String 1024) [0..1]
      * * TestGroup2[0..1]
      * * * TestProperty2_1 (Boolean) [0..1] *
      * * * TestProperty2_2 (String 1024) [0..2]
      *
      * @throws Exception on errors
      */
     @BeforeClass(dependsOnMethods = {"beforeClass"})
     private void setupStructures() throws Exception {
         try {
             if (CacheAdmin.getEnvironment().getType(TEST_TYPE) != null)
                 return;
         } catch (FxRuntimeException e) {
             //ignore and create
         }
         long typeId = type.save(FxTypeEdit.createNew(TEST_TYPE, new FxString("Test data"), CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()), null));
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxString hint = new FxString(true, "hint");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty1", new FxString(true, "TestProperty1"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty2", new FxString(true, "TestProperty2"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.Text), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty3", new FxString(true, "TestProperty3"), hint,
                 new FxMultiplicity(0, 5), CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty4", new FxString(true, "TestProperty4"), hint,
                 FxMultiplicity.MULT_0_N, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty5", new FxString(true, "TestProperty5"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.Double), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty6", new FxString(true, "TestProperty6"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.Number), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty7", new FxString(true, "TestProperty7"), hint,
                 FxMultiplicity.MULT_1_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.Text), "/");
         ass.createGroup(typeId, FxGroupEdit.createNew("TestGroup1", new FxString(true, "TestGroup1"), hint, false,
                 new FxMultiplicity(0, 2)), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty1_1", new FxString(true, "TestProperty1_1"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024), "/TestGroup1");
         ass.createGroup(typeId, FxGroupEdit.createNew("TestGroup2", new FxString(true, "TestGroup2"), hint, false,
                 FxMultiplicity.MULT_0_1), "/");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty2_1", new FxString(true, "TestProperty2_1"), hint,
                 FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.Boolean), "/TestGroup2");
         ass.createProperty(typeId, FxPropertyEdit.createNew("TestProperty2_2", new FxString(true, "TestProperty2_2"), hint,
                 new FxMultiplicity(0, 2), CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()),
                 FxDataType.String1024), "/TestGroup2");
 
     }
 
     @AfterClass
    public void afterClass() throws FxLogoutFailedException, FxApplicationException {
         long typeId = CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
         co.removeForType(typeId);
         type.remove(typeId);
         if (testsEnabled)
             dce.removeFlatStorage(TEST_STORAGE);
         logout();
     }
 
     private void setupStorage() throws FxApplicationException {
         if (!testsEnabled) return;
         dce.createFlatStorage(TEST_STORAGE, TEST_STORAGE_DESCRIPTION, 20, 20, 10, 10, 10);
     }
 
     @Test
     public void storageCreateRemove() throws Exception {
         if (!testsEnabled) return;
         dce.createFlatStorage(TEST_STORAGE + "_CR", TEST_STORAGE_DESCRIPTION, 10, 20, 30, 40, 50);
         boolean found = false;
         for (FxFlatStorageInfo info : dce.getFlatStorageInfos()) {
             if (info.getName().equals(TEST_STORAGE + "_CR")) {
                 found = true;
                 Assert.assertEquals(info.getColumnsString(), 10);
                 Assert.assertEquals(info.getColumnsText(), 20);
                 Assert.assertEquals(info.getColumnsBigInt(), 30);
                 Assert.assertEquals(info.getColumnsDouble(), 40);
                 Assert.assertEquals(info.getColumnsSelect(), 50);
             }
         }
         Assert.assertTrue(found, "Test storage not found!");
         dce.removeFlatStorage(TEST_STORAGE + "_CR");
         found = false;
         for (FxFlatStorageInfo info : dce.getFlatStorageInfos()) {
             if (info.getName().equals(TEST_STORAGE + "_CR"))
                 found = true;
         }
         Assert.assertFalse(found, "Test storage should no longer exist!");
     }
 
     @Test
     public void flatAnalyze() throws Exception {
         if (!testsEnabled) return;
 
         Map<String, List<FxPropertyAssignment>> pot = ass.getPotentialFlatAssignments(CacheAdmin.getEnvironment().getType(TEST_TYPE));
         Assert.assertEquals(pot.size(), 5, "Expected 5 flat mappings");
         Assert.assertEquals(pot.get(FxFlatColumnType.STRING.name()).size(), 1);
         Assert.assertEquals(pot.get(FxFlatColumnType.TEXT.name()).size(), 2);
         Assert.assertEquals(pot.get(FxFlatColumnType.BIGINT.name()).size(), 1);
         Assert.assertEquals(pot.get(FxFlatColumnType.DOUBLE.name()).size(), 1);
         Assert.assertEquals(pot.get(FxFlatColumnType.SELECT.name()).size(), 1);
         //2 should be boosted since its required
         Assert.assertEquals(pot.get(FxFlatColumnType.TEXT.name()).get(0), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTPROPERTY7"));
         Assert.assertEquals(pot.get(FxFlatColumnType.TEXT.name()).get(1), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTPROPERTY2"));
         Assert.assertEquals(pot.get(FxFlatColumnType.STRING.name()).get(0), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTPROPERTY1"));
         Assert.assertEquals(pot.get(FxFlatColumnType.DOUBLE.name()).get(0), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTPROPERTY5"));
         Assert.assertEquals(pot.get(FxFlatColumnType.BIGINT.name()).get(0), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTPROPERTY6"));
         Assert.assertEquals(pot.get(FxFlatColumnType.SELECT.name()).get(0), CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/TESTGROUP2/TESTPROPERTY2_1"));
     }
 
     @Test(dependsOnMethods = {"flatAnalyze"})
     public void flattenAssignments() throws Exception {
         if (!testsEnabled) return;
 
 
         //create a test instance which will be migrated by the flat storage
         final FxContent test = co.initialize(TEST_TYPE);
         test.randomize();
         co.save(test);
 
         Map<String, List<FxPropertyAssignment>> pot = ass.getPotentialFlatAssignments(CacheAdmin.getEnvironment().getType(TEST_TYPE));
         for (String col : pot.keySet()) {
             for (FxPropertyAssignment pa : pot.get(col)) {
                 Assert.assertFalse(pa.isFlatstoreEntry(), "Assignment " + pa.getXPath() + " is not expected to be a flatstore entry!");
                 ass.flattenAssignment(TEST_STORAGE, pa);
                 Assert.assertTrue(CacheAdmin.getEnvironment().getPropertyAssignment(pa.getXPath()).isFlatstoreEntry(),
                         "Assignment " + pa.getXPath() + " is expected to be a flatstore entry!");
             }
         }
     }
 
 }
