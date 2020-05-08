 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ACLEngine;
 import com.flexive.shared.interfaces.AssignmentEngine;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.TypeEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxFloat;
 import com.flexive.shared.value.FxNoAccess;
 import com.flexive.shared.value.FxNumber;
 import com.flexive.shared.value.FxString;
 import static com.flexive.tests.embedded.FxTestUtils.*;
 import org.apache.commons.lang.RandomStringUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 
 /**
  * Tests for the ContentEngine
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "content"})
 public class ContentEngineTest {
 
     private ContentEngine co;
     private ACLEngine acl;
     private TypeEngine type;
     private AssignmentEngine ass;
     public static final String TEST_EN = "Test value in english";
    public static final String TEST_DE = "Test datensatz in deutsch mit \u00E4ml\u00E5ut te\u00DFt";
    public static final String TEST_FR = "My french is bad but testing special characters: ?`?$\u010E1\u00C6~\u0119\\/";
     public static final String TEST_IT = "If i knew italian this would be a test value in italian ;)";
 
     public static final String TEST_TYPE = "TEST_TYPE_" + RandomStringUtils.random(16, true, true);
     public static final String TEST_GROUP = "TEST_GROUP_" + RandomStringUtils.random(16, true, true);
     private static final String TYPE_ARTICLE = "__ArticleTest";
 
     /**
      * setup...
      *
      * @throws Exception on errors
      */
     @BeforeClass
     public void beforeClass() throws Exception {
         co = EJBLookup.getContentEngine();
         acl = EJBLookup.getACLEngine();
         type = EJBLookup.getTypeEngine();
         ass = EJBLookup.getAssignmentEngine();
         login(TestUsers.SUPERVISOR);
     }
 
 
     @AfterClass(dependsOnMethods = {"tearDownStructures"})
     public void afterClass() throws FxLogoutFailedException {
         logout();
     }
 
     @AfterClass
     public void tearDownStructures() throws Exception {
         long typeId = CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
         co.removeForType(typeId);
         type.remove(typeId);
         typeId = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE).getId();
         co.removeForType(typeId);
         type.remove(typeId);
         //remove the test group
         ass.removeAssignment(CacheAdmin.getEnvironment().getAssignment("ROOT/" + TEST_GROUP).getId(), true, false);
     }
 
     /**
      * Setup testing structure.
      * <p/>
      * Hierarchy looks like this:
      * * TestProperty1 (String 255)[0..1]
      * * TestProperty2 (String 1024) [1..1]
      * * TestProperty3 (String 255) [0..5]
      * * TestProperty4 (String 255) [1..N]
      * $ TestGroup1 [0..2]
      * $ * TestProperty1_1 (String 255) [0..1]
      * $ * TestProperty1_2 (String 255) [1..1]
      * $ * TestProperty1_3 (String 255) [1..N]
      * $ $ TestGroup1_1 [1..1]
      * $ $ * TestProperty1_1_1 (String 255) [0..1]
      * $ $ TestGroup1_2 [0..N]
      * $ $ * TestProperty1_2_1 (String 255) [1..1]
      * $ $ * TestProperty1_2_2 (String 255) [0..5]
      * $ $ $ TestGroup1_2_1 [1..2]
      * $ $ $ * TestProperty1_2_1_1 (String 255) [0..2]
      * $ $ TestGroup1_3 [1..2]
      * $ $ * TestProperty1_3_1 (String 255) [0..1]
      * * TestNumber (FxNumber) [0..2]
      * * TestNumberSL (FxNumber) [0..2] (Single language only)
      * * TestFloat (FxFloat) [0..2]
      *
      * @throws Exception on errors
      */
     @BeforeClass(dependsOnMethods = {"setupACL"})
     public void setupStructures() throws Exception {
         try {
             if (CacheAdmin.getEnvironment().getType(TEST_TYPE) != null)
                 return;
         } catch (FxRuntimeException e) {
             //ignore and create
         }
         ACL structACL = CacheAdmin.getEnvironment().getACL("Test ACL Structure 1");
         boolean createRootStuff = true;
         try {
             createRootStuff = CacheAdmin.getEnvironment().getGroup(TEST_TYPE) == null;
         } catch (FxRuntimeException e) {
             //ignore and create
         }
         if (createRootStuff) {
             FxString desc = new FxString("Test data structure");
             desc.setTranslation(FxLanguage.GERMAN, "Testdaten Strukturen");
             FxString hint = new FxString("Hint text ...");
             FxGroupEdit ge = FxGroupEdit.createNew(TEST_GROUP, desc, hint, true, new FxMultiplicity(0, 1));
             ass.createGroup(ge, "/");
             FxPropertyEdit pe = FxPropertyEdit.createNew("TestProperty1", desc, hint, true, new FxMultiplicity(0, 1),
                     true, structACL, FxDataType.String1024, new FxString(FxString.EMPTY),
                     true, null, null, null).setMultiLang(true).setOverrideMultiLang(true);
             pe.setAutoUniquePropertyName(true);
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty2");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty3");
             pe.setDataType(FxDataType.String1024);
             pe.setMultiplicity(new FxMultiplicity(0, 5));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestProperty4");
             pe.setMultiplicity(new FxMultiplicity(1, FxMultiplicity.N));
             ass.createProperty(pe, "/" + TEST_GROUP);
             ge.setName("TestGroup1");
             ge.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createGroup(ge, "/" + TEST_GROUP);
             pe.setName("TestProperty1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_2");
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_3");
             pe.setMultiplicity(new FxMultiplicity(1, FxMultiplicity.N));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1");
             ge.setName("TestGroup1_1");
             ge.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_1");
             ge.setName("TestGroup1_2");
             ge.setMultiplicity(new FxMultiplicity(0, FxMultiplicity.N));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_2_1");
             pe.setMultiplicity(new FxMultiplicity(1, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             pe.setName("TestProperty1_2_2");
             pe.setMultiplicity(new FxMultiplicity(0, 5));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             ge.setName("TestGroup1_2_1");
             ge.setMultiplicity(new FxMultiplicity(1, 2));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2");
             pe.setName("TestProperty1_2_1_1");
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_2/TestGroup1_2_1");
             ge.setName("TestGroup1_3");
             ge.setMultiplicity(new FxMultiplicity(1, 2));
             ass.createGroup(ge, "/" + TEST_GROUP + "/TestGroup1");
             pe.setName("TestProperty1_3_1");
             pe.setMultiplicity(new FxMultiplicity(0, 1));
             ass.createProperty(pe, "/" + TEST_GROUP + "/TestGroup1/TestGroup1_3");
             pe.setName("TestNumber");
             pe.setDataType(FxDataType.Number);
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setName("TestNumberSL");
             pe.setDataType(FxDataType.Number);
             pe.setMultiLang(false);
             ass.createProperty(pe, "/" + TEST_GROUP);
             pe.setMultiLang(true);
             pe.setName("TestFloat");
             pe.setDataType(FxDataType.Float);
             pe.setMultiplicity(new FxMultiplicity(0, 2));
             ass.createProperty(pe, "/" + TEST_GROUP);
         }
         //create article type
         FxPropertyEdit pe = FxPropertyEdit.createNew("MyTitle", new FxString("Description"), new FxString("Hint"),
                 true, new FxMultiplicity(0, 1),
                 true, structACL, FxDataType.String1024, new FxString(FxString.EMPTY),
                 true, null, null, null).setAutoUniquePropertyName(true).setMultiLang(true).setOverrideMultiLang(true);
         long articleId = type.save(FxTypeEdit.createNew(TYPE_ARTICLE, new FxString("Article test type"), CacheAdmin.getEnvironment().getACLs(ACL.Category.STRUCTURE).get(0), null));
         ass.createProperty(articleId, pe, "/");
         pe.setName("Text");
         pe.setDataType(FxDataType.Text);
         pe.setMultiplicity(new FxMultiplicity(0, 2));
         ass.createProperty(articleId, pe, "/");
 
         long testDataId = type.save(FxTypeEdit.createNew(TEST_TYPE, new FxString("Test data"), CacheAdmin.getEnvironment().getACLs(ACL.Category.STRUCTURE).get(0), null));
         FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment("ROOT/" + TEST_GROUP);
         FxGroupAssignmentEdit gae = FxGroupAssignmentEdit.createNew(ga, CacheAdmin.getEnvironment().getType(TEST_TYPE), null, "/");
         ass.save(gae, true);
 
         try {
             if (ScriptingTest.loadScript != null) {
                 //only install scripts if scripting is being tested as well
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.loadScript.getId(), 0, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.removeScript.getId(), 0, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.loadScript.getId(), testDataId, true, true);
                 EJBLookup.getScriptingEngine().createTypeScriptMapping(ScriptingTest.removeScript.getId(), testDataId, true, true);
             }
         } catch (Exception ex) {
             //ignore since scripting might not be enabled for this test run
             ex.printStackTrace();
         }
     }
 
     /**
      * Setup ACL's needed for testing
      *
      * @throws Exception on errors
      */
     @BeforeClass(dependsOnMethods = {"beforeClass"})
     public void setupACL() throws Exception {
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Content 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Content 1", new FxString("Test ACL Content 1"), Mandator.MANDATOR_FLEXIVE, "#00CCDD", "", ACL.Category.INSTANCE);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Structure 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Structure 1", new FxString("Test ACL Structure 1"), Mandator.MANDATOR_FLEXIVE, "#BBCCDD", "", ACL.Category.STRUCTURE);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Test ACL Workflow 1");
         } catch (FxRuntimeException e) {
             acl.create("Test ACL Workflow 1", new FxString("Test ACL Workflow 1"), Mandator.MANDATOR_FLEXIVE, "#BB00DD", "", ACL.Category.WORKFLOW);
         }
         try {
             CacheAdmin.getEnvironment().getACL("Article ACL");
         } catch (FxRuntimeException e) {
             acl.create("Article ACL", new FxString("ACL for articles"), Mandator.MANDATOR_FLEXIVE, "#00CC00", "", ACL.Category.INSTANCE);
         }
     }
 
     @Test
     public void removeAddData() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assert testType != null;
         FxContent test = co.initialize(testType.getId());
         assert test != null;
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
 
         assert test.getRootGroup().getCreateableChildren(true).size() == 6;
         assert !test.getRootGroup().isRemoveable();
         assert test.getGroupData("/TestGroup1").isRemoveable();
         assert !test.getGroupData("/TestGroup1/TestGroup1_1").isRemoveable();
         assert test.getPropertyData("/TestProperty1").isRemoveable();
         assert !test.getPropertyData("/TestProperty2").isRemoveable();
         assert test.getPropertyData("/TestProperty3").isRemoveable();
         assert !test.getPropertyData("/TestProperty4").isRemoveable();
         test.remove("/TestGroup1/TestGroup1_2");
         try {
             test.getGroupData("/TestGroup1/TestGroup1_2");
             assert false : "/TestGroup1/TestGroup1_2 should no longer exist!";
         } catch (FxNotFoundException e) {
             //expected
         }
         List<String> cr = test.getGroupData("/TestGroup1").getCreateableChildren(true);
         assert cr.size() == 3;
         assert cr.get(0).equals("/TESTGROUP1[1]/TESTPROPERTY1_3[2]");
         assert cr.get(1).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]");
         assert cr.get(2).equals("/TESTGROUP1[1]/TESTGROUP1_3[2]");
         cr = test.getGroupData("/TestGroup1").getCreateableChildren(false);
 //        for(String xp: cr)
 //            System.out.println("==cr=> "+xp);
         assert cr.size() == 1;
         assert cr.get(0).equals("/TESTGROUP1[1]/TESTGROUP1_2[1]");
 
         test.getGroupData("/TestGroup1").explode(false);
         assert test.getGroupData("/TestGroup1").getChildren().size() == 6;
         assert test.getGroupData("/TESTGROUP1[1]/TESTGROUP1_2[1]").getChildren().size() == 3;
         test.remove("/TESTGROUP1[1]/TESTGROUP1_2[1]");
         assert test.getGroupData("/TestGroup1").getCreateableChildren(false).size() == 1;
 
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[1]", FxData.POSITION_BOTTOM);
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[2]", FxData.POSITION_BOTTOM);
         test.getGroupData("/TestGroup1").addEmptyChild("/TESTGROUP1[1]/TESTGROUP1_2[4]", FxData.POSITION_BOTTOM);
         try {
             test.getRootGroup().addEmptyChild("/TESTPROPERTY1[2]", FxData.POSITION_BOTTOM);
             assert false : "FxCreateException expected! max. multiplicity reached";
         } catch (FxInvalidParameterException e) {
             //expected
         }
         test.remove("/TestGroup1");
         try {
             test.getGroupData("/TestGroup1");
             assert false : "/TestGroup1 should no longer exist!";
         } catch (FxNotFoundException e) {
             //expected
         }
         test.remove("/TestNumber");
         try {
             test.getPropertyData("/TestNumber");
             assert false : "/TestNumber should no longer exist!";
         } catch (FxNotFoundException e) {
             //expected
         }
     }
 
     @Test
     public void contentComplex() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assert testType != null;
         //initialize tests start
         FxContent test = co.initialize(testType.getId());
         test.setAclId(CacheAdmin.getEnvironment().getACL("Test ACL Content 1").getId());
         assert test != null;
         int rootSize = 8 + CacheAdmin.getEnvironment().getSystemInternalRootPropertyAssignments().size();
         assert rootSize == test.getData("/").size() : "Root size expected " + rootSize + ", was " + test.getData("/").size();
 //        FxGroupData groot = test.getData("/").get(0).getParent();
         //basic sanity checks
 //        assert groot.isEmpty(); TODO: isEmpty means no sys internal properties!
         assert test.getData("/TestProperty1").get(0).getAssignmentMultiplicity().toString().equals("0..1");
         assert 1 == test.getData("/TestProperty1").get(0).getIndex();
         assert !(test.getData("/TestProperty1").get(0).mayCreateMore()); //should be at multiplicity 1
         assert !(test.getPropertyData("/TestProperty2").mayCreateMore()); //should be at multiplicity 1
         assert test.getPropertyData("/TestProperty3").mayCreateMore(); //max of 5
         assert test.getGroupData("/TestGroup1").mayCreateMore(); //max of 2
         assert 1 == test.getGroupData("/TestGroup1").getCreateableElements(); //1 left to create
         assert 1 == test.getGroupData("/TestGroup1").getRemoveableElements(); //1 left to remove
         assert 6 == test.getData("/TestGroup1").size();
         assert 0 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getCreateableElements(); //0 left
         assert 1 == test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").getRemoveableElements(); //0 left
         assert !(test.getPropertyData("/TestGroup1/TestGroup1_1/TestProperty1_1_1").mayCreateMore()); //0 left
         assert 1 == test.getGroupData("/TestGroup1/TestGroup1_2").getIndex();
         FxPropertyData p = test.getPropertyData("/TestGroup1/TestGroup1_2/TestGroup1_2_1/TestProperty1_2_1_1");
         assert 1 == p.getIndex();
         assert p.isEmpty();
         assert p.isProperty();
         assert "TESTPROPERTY1_2_1_1".equals(p.getAlias());
         assert "TESTGROUP1_2_1".equals(p.getParent().getAlias());
         assert p.getAssignmentMultiplicity().equals(new FxMultiplicity(0, 2));
         assert 4 == p.getIndices().length;
         //check required with empty values
         try {
             test.checkValidity();
             assert false : "checkValidity() succeeded when it should not!";
         } catch (FxInvalidParameterException e) {
             //ok
             assert "ex.content.required.missing".equals(e.getExceptionMessage().getKey());
         }
         //fill all required properties
         FxString testValue = new FxString(FxLanguage.ENGLISH, TEST_EN);
         testValue.setTranslation(FxLanguage.GERMAN, TEST_DE);
         testValue.setTranslation(FxLanguage.FRENCH, TEST_FR);
         testValue.setTranslation(FxLanguage.ITALIAN, TEST_IT);
         test.setValue("/TestProperty2", testValue);
         test.setValue("/TestProperty4", testValue);
         //check required with empty groups
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             assert false : "checkValidity() did not succeed when it should!";
         }
         test.setValue("/TestGroup1[1]/TestProperty1_2", testValue);
         try {
             test.checkValidity();
             assert false : "checkValidity() succeeded but /TestGroup1/TestProperty1_3 is missing!";
         } catch (FxInvalidParameterException e) {
             //ok
             assert "ex.content.required.missing".equals(e.getExceptionMessage().getKey());
         }
         test.setValue("/TestGroup1[1]/TestProperty1_3", testValue);
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             assert false : "checkValidity() did not succeed when it should!";
         }
         test.setValue("/TestGroup1/TestProperty1_2", testValue);
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             assert false : "checkValidity() did not succeed when it should!";
         }
         FxPK pk = co.save(test);
         co.remove(pk);
         //test /TestGroup1[2]...
         FxGroupData gd = test.getGroupData("/TestGroup1");
         assert gd.mayCreateMore();
         assert 1 == gd.getCreateableElements(); //1 more should be createable
         gd.createNew(FxData.POSITION_BOTTOM);
         assert test.getGroupData("/TestGroup1[2]").isEmpty();
         //should still be valid since [2] is empty
         try {
             test.checkValidity();
         } catch (FxInvalidParameterException e) {
             assert false : "checkValidity() did not succeed when it should!";
         }
         test.setValue("/TestGroup1[2]/TestProperty1_2", testValue);
         test.setValue("/TestGroup1[2]/TestProperty1_3", testValue);
         pk = co.save(test);
         FxContent testLoad = co.load(pk);
         assert TEST_IT.equals(((FxString) testLoad.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN));
         co.remove(pk);
         pk = co.save(test);
         FxContent testLoad2 = co.load(pk);
         FxNumber number = new FxNumber(true, FxLanguage.GERMAN, 42);
         number.setTranslation(FxLanguage.ENGLISH, 43);
         testLoad2.setValue("/TestNumber", number);
         FxNumber numberSL = new FxNumber(false, FxLanguage.GERMAN, 12);
         assert numberSL.getDefaultLanguage() == FxLanguage.SYSTEM_ID;
         assert 12 == numberSL.getTranslation(FxLanguage.FRENCH);
         numberSL.setTranslation(FxLanguage.ITALIAN, 13);
         assert 13 == numberSL.getTranslation(FxLanguage.FRENCH);
         testLoad2.setValue("/TestNumberSL", numberSL);
         FxFloat fxFloat = new FxFloat(true, FxLanguage.GERMAN, 42.42f);
         fxFloat.setTranslation(FxLanguage.ENGLISH, 43.43f);
         testLoad2.setValue("/TestFloat", fxFloat);
         assert 42 == ((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getDefaultTranslation() : "Default translation invalid (should be 42 for german, before save)";
         assert 43 == ((FxNumber) testLoad2.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH) : "English translation invalid (should be 43, before save)";
         assert testLoad2.getPropertyData("/TestNumber").getValue().hasDefaultLanguage();
         assert 42.42f == ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getDefaultTranslation() : "Default translation invalid (should be 42.42f for german, before save)";
         assert 43.43f == ((FxFloat) testLoad2.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH) : "English translation invalid (should be 43.43f, before save)";
         assert testLoad2.getPropertyData("/TestFloat").getValue().hasDefaultLanguage();
         FxPK saved = co.save(testLoad2);
         FxContent testLoad3 = co.load(saved);
         assert 42 == ((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getDefaultTranslation() : "Default translation invalid (should be 42 for german, after load)";
         assert 43 == ((FxNumber) testLoad3.getPropertyData("/TestNumber").getValue()).getTranslation(FxLanguage.ENGLISH) : "English translation invalid (should be 43, after load)";
         assert 13 == ((FxNumber) testLoad3.getPropertyData("/TestNumberSL").getValue()).getTranslation(FxLanguage.ENGLISH) : "English translation invalid (should be 13, after load)";
         assert !testLoad3.getPropertyData("/TestNumberSL").getValue().isMultiLanguage() : "Single language value expected";
         assert testLoad3.getPropertyData("/TestNumber").getValue().hasDefaultLanguage() : "Missing default language after load";
         assert 42.42f == ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getDefaultTranslation() : "Default translation invalid (should be 42.42f for german, before save)";
         assert 43.43f == ((FxFloat) testLoad3.getPropertyData("/TestFloat").getValue()).getTranslation(FxLanguage.ENGLISH) : "English translation invalid (should be 43.43f, before save)";
         assert testLoad3.getPropertyData("/TestFloat").getValue().hasDefaultLanguage() : "Missing default language after load";
         assert TEST_IT.equals(((FxString) testLoad3.getPropertyData("/TestGroup1[2]/TestProperty1_3").getValue()).getTranslation(FxLanguage.ITALIAN));
         assert 1 == co.removeForType(testType.getId()) : "Only one instance should be removed!";
         assert 0 == co.removeForType(testType.getId()) : "No instance should be left to remove!";
 
         co.initialize(testType.getId()).randomize();
     }
 
     @Test
     public void contentInitialize() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
             FxContent test = co.initialize(article.getId());
             test.setAclId(CacheAdmin.getEnvironment().getACL("Article ACL").getId());
             test.getData("/");
             test.getData("/MYTITLE");
             test.getData("/TEXT");
             //test if shared message loading works
             FxNoAccess noAccess = new FxNoAccess(getUserTicket(), null);
             if (getUserTicket().getLanguage().getId() == FxLanguage.ENGLISH)
                 assert "Access denied!".equals(noAccess.getDefaultTranslation()) : "Shared message loading failed! Expected [Access denied!] got: [" + noAccess.getDefaultTranslation() + "]";
             else if (getUserTicket().getLanguage().getId() == FxLanguage.GERMAN)
                 assert "Zugriff verweigert!".equals(noAccess.getDefaultTranslation()) : "Shared message loading failed!";
         } catch (FxApplicationException e) {
             assert false : e.getMessage();
         }
     }
 
     @Test
     public void defaultMultiplicity() throws Exception {
         try {
             FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
 
             FxPropertyAssignmentEdit pe = new FxPropertyAssignmentEdit((FxPropertyAssignment) article.getAssignment("/TEXT"));
             pe.setDefaultMultiplicity(2);
             assert 2 == pe.getDefaultMultiplicity() : "Wrong default multiplicity";
             pe.setDefaultMultiplicity(-3);
             assert 0 == pe.getDefaultMultiplicity() : "Wrong default multiplicity";
             pe.setDefaultMultiplicity(3);
             assert 2 == pe.getDefaultMultiplicity() : "Wrong default multiplicity";
             ass.save(pe, false);
 
             FxContent test = co.initialize(article.getId());
             test.getData("/TEXT[1]");
             test.getData("/TEXT[2]");
             pe.setDefaultMultiplicity(1);
             ass.save(pe, false);
             test = co.initialize(article.getId());
             test.getData("/TEXT[1]");
             try {
                 test.getData("/TEXT[2]");
                 assert false : "No /TEXT[2] should exist!";
             } catch (Exception e) {
                 //ok
             }
         } catch (FxApplicationException e) {
             assert false : e.getMessage();
         }
     }
 
 
     @Test
     public void contentCreate() throws Exception {
         FxType article = CacheAdmin.getEnvironment().getType(TYPE_ARTICLE);
         FxContent test = co.initialize(article.getId());
         test.setAclId(CacheAdmin.getEnvironment().getACL("Article ACL").getId());
         FxString title = new FxString(FxLanguage.ENGLISH, "Title english");
         title.setTranslation(FxLanguage.GERMAN, "Titel deutsch");
         FxString text = new FxString(FxLanguage.ENGLISH, "Text english1");
         text.setTranslation(FxLanguage.GERMAN, "Text deutsch1");
         test.setValue("/MYTITLE", title);
         test.setValue("/TEXT", text);
         int titlePos = test.getPropertyData("/MYTITLE").getPos();
         test.move("/MYTITLE", 1); //move title 1 position down
         FxPropertyData pText = test.getPropertyData("/TEXT");
         assert "Text english1".equals(((FxString) test.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH));
         if (pText.mayCreateMore()) {
             FxPropertyData pText2 = (FxPropertyData) pText.createNew(FxData.POSITION_BOTTOM);
             pText2.setValue(new FxString(FxLanguage.ENGLISH, "Text english2"));
             assert "Text english2".equals(((FxString) test.getPropertyData("/TEXT[2]").getValue()).getTranslation(FxLanguage.ENGLISH));
         }
         //            for( int i=0; i<100;i++)
         FxPK pk = co.save(test);
         FxContent comp = co.load(pk);
         assert comp != null;
         assert comp.getPk().getId() == pk.getId() : "Id failed";
         assert comp.getPk().getId() == comp.getId() : "Id of content not equal the Id of contents pk";
         assert comp.matchesPk(pk) : "matchesPk failed";
         assert comp.matchesPk(new FxPK(pk.getId(), FxPK.MAX)) : "matchesPk for max version failed";
         assert 1 == comp.getPk().getVersion() : "Version is not 1";
         assert comp.getStepId() == test.getStepId() : "Step failed";
         assert comp.getAclId() == test.getAclId() : "ACL failed";
         assert comp.isMaxVersion() : "MaxVersion failed";
         assert comp.isLiveVersion() == article.getWorkflow().getSteps().get(0).isLiveStep() : "LiveVersion failed. Expected:" + article.getWorkflow().getSteps().get(0).isLiveStep() + " Got:" + comp.isLiveVersion();
         assert comp.getMainLanguage() == FxLanguage.ENGLISH : "MainLang failed";
         assert comp.getLifeCycleInfo().getCreatorId() == getUserTicket().getUserId() : "CreatedBy failed";
         assert "Text english1".equals(((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH)) : "Expected 'Text english1', got '" + ((FxString) comp.getPropertyData("/TEXT[1]").getValue()).getTranslation(FxLanguage.ENGLISH) + "'";
         //test result of move
         assert titlePos == comp.getPropertyData("/TEXT").getPos() : "Text[1] position should be " + (titlePos) + " but is " + comp.getPropertyData("/TEXT").getPos();
         assert titlePos + 1 == comp.getPropertyData("/MYTITLE[1]").getPos();
         assert titlePos + 2 == comp.getPropertyData("/TEXT[2]").getPos();
         FxPK pk2 = co.createNewVersion(comp);
         assert 2 == pk2.getVersion();
         FxPK pk3 = co.createNewVersion(comp);
         assert 3 == pk3.getVersion();
         FxContentVersionInfo cvi = co.getContentVersionInfo(pk3);
         assert 3 == cvi.getLastModifiedVersion();
         assert 3 == cvi.getLiveVersion();
         assert 1 == cvi.getMinVersion();
         co.removeVersion(new FxPK(pk.getId(), 1));
         cvi = co.getContentVersionInfo(pk3);
         System.out.println("After rm1: " + cvi);
         assert 2 == cvi.getMinVersion();
         assert 3 == cvi.getMaxVersion();
         co.removeVersion(new FxPK(pk.getId(), 3));
         cvi = co.getContentVersionInfo(pk3);
         System.out.println("After rm3: " + cvi);
         assert 2 == cvi.getMinVersion();
         assert 2 == cvi.getMaxVersion();
         assert !cvi.hasLiveVersion();
         co.removeVersion(new FxPK(pk.getId(), 2));
         try {
             co.getContentVersionInfo(new FxPK(pk.getId()));
             assert false : "VersionInfo available for a removed instance!";
         } catch (FxApplicationException e) {
             //ok
         }
     }
 
     @Test
     public void binaryUploadTest() throws Exception {
         //        File testFile = new File("/home/mplesser/install/java/testng-5.1.zip");
         File testFile = new File("test.file");
         if (!testFile.exists())
             testFile = new File("build/ui/flexive.war");
         if (!testFile.exists())
             return;
         FileInputStream fis = new FileInputStream(testFile);
         String handle = FxStreamUtils.uploadBinary(testFile.length(), fis).getHandle();
         System.out.println("==Client done== Handle received: " + handle);
         fis.close();
     }
 
     @Test
     public void typeValidityTest() throws Exception {
         FxType t = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         assert t.isXPathValid("/", false) : "Root group should be valid for groups";
         assert !t.isXPathValid("/", true) : "Root group should be invalid for properties";
         assert t.isXPathValid("/TestProperty1", true);
         assert !t.isXPathValid("/TestProperty1", false);
         assert t.isXPathValid(TEST_TYPE + "/TestProperty1", true);
         assert !t.isXPathValid(TEST_TYPE + "123/TestProperty1", true);
         assert !t.isXPathValid("WrongType/TestProperty1", true);
         assert !t.isXPathValid(TEST_TYPE + "/TestProperty1/Dummy", true);
         assert t.isXPathValid("/TestProperty1[1]", true);
         assert !t.isXPathValid("/TestProperty1[2]", true);
         assert t.isXPathValid("/TestGroup1[2]", false);
         assert t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", true);
         assert !t.isXPathValid("/TestGroup1[1]/TestProperty1_3[4711]", false);
         assert t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", true);
         assert !t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[5]", false);
         assert !t.isXPathValid("/TestGroup1[1]/TestGroup1_2[42]/TestProperty1_2_2[6]", true);
     }
 
     @Test
     public void setValueTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent test = co.initialize(testType.getId());
         FxString testValue = new FxString("Hello world");
         test.setValue("/TestGroup1[2]/TestGroup1_2[3]/TestProperty1_2_2[4]", testValue);
         assert test.getValue("/TestGroup1[2]/TestGroup1_2[3]/TestProperty1_2_2[4]").equals(testValue);
     }
 
     @Test
     public void deltaTest() throws Exception {
         FxType testType = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         FxContent org = co.initialize(testType.getId());
         FxString testValue1 = new FxString("Hello world1");
         FxString testValue2 = new FxString("Hello world2");
         //set required properties to allow saving ..
         org.setValue("/TestProperty2[1]", testValue1);
         org.setValue("/TestProperty4[1]", testValue1);
 
         //test properties
         org.setValue("/TestProperty3[1]", testValue1);
         org.setValue("/TestProperty3[2]", testValue2);
         org.setValue("/TestProperty3[3]", testValue1);
 
         FxPK pk = co.save(org);
         try {
             org = co.load(pk);
             FxContent test = co.load(pk);
             FxDelta d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assert d.getAdds().size() == 0 : "Expected no adds, but got " + d.getAdds().size();
             assert d.getRemoves().size() == 0 : "Expected no deletes, but got " + d.getRemoves().size();
             assert d.getUpdates().size() == 0 : "Expected no updates, but got " + d.getUpdates().size();
 
             test.remove("/TestProperty3[2]");
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assert d.getAdds().size() == 0 : "Expected no adds, but got " + d.getAdds().size();
             assert d.getRemoves().size() == 1 : "Expected 1 deletes, but got " + d.getRemoves().size();
             assert d.getUpdates().size() == 1 : "Expected 1 updates, but got " + d.getUpdates().size();
             assert d.getRemoves().get(0).getXPath().equals("/TESTPROPERTY3[3]") : "Expected /TESTPROPERTY3[3] but got: " + d.getRemoves().get(0).getXPath();
             assert d.getUpdates().get(0).getXPath().equals("/TESTPROPERTY3[2]") : "Expected /TESTPROPERTY3[2] but got: " + d.getUpdates().get(0).getXPath();
 
             test = co.load(pk);
             test.setValue("/TestGroup1/TestProperty1_2", testValue1);
             test.setValue("/TestGroup1/TestProperty1_3", testValue1);
             test.getGroupData("/TestGroup1").removeEmptyEntries();
             test.getGroupData("/TestGroup1").compactPositions(true);
             d = FxDelta.processDelta(org, test);
             System.out.println(d.dump());
             assert d.changes() : "Expected some changes";
             assert d.getAdds().size() == 3 : "Expected 3 (group + 2 properties) adds but got " + d.getAdds().size();
             assert d.getRemoves().size() == 0 : "Expected 0 deletes but got " + d.getRemoves().size();
             assert d.getUpdates().size() == 0 : "Expected 0 updates but got " + d.getUpdates().size();
         } finally {
             co.remove(pk);
         }
 
     }
 }
