 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 
 import com.flexive.shared.*;
 import static com.flexive.shared.EJBLookup.*;
 import static com.flexive.shared.EJBLookup.getBriefcaseEngine;
 import static com.flexive.shared.EJBLookup.getContentEngine;
 import static com.flexive.shared.FxLanguage.ENGLISH;
 import static com.flexive.shared.FxLanguage.GERMAN;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.search.*;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import static com.flexive.shared.search.query.PropertyValueComparator.EQ;
 import com.flexive.shared.search.query.QueryOperatorNode;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringUtils;
 import org.testng.Assert;
 import static org.testng.Assert.*;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * FxSQL search query engine tests.
  * <p/>
  * Test data is created in init1201_testcontent.gy !
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = {"ejb", "search"})
 public class SearchEngineTest {
     private static final String TEST_SUFFIX = "SearchProp";
     private static final String TEST_TYPE = "SearchTest";
     private static final Map<String, FxDataType> TEST_PROPS = new HashMap<String, FxDataType>();
 
     static {
         TEST_PROPS.put("string", FxDataType.String1024);
         TEST_PROPS.put("text", FxDataType.Text);
         TEST_PROPS.put("html", FxDataType.HTML);
         TEST_PROPS.put("number", FxDataType.Number);
         TEST_PROPS.put("largeNumber", FxDataType.LargeNumber);
         TEST_PROPS.put("float", FxDataType.Float);
         TEST_PROPS.put("double", FxDataType.Double);
         TEST_PROPS.put("date", FxDataType.Date);
         TEST_PROPS.put("dateTime", FxDataType.DateTime);
         TEST_PROPS.put("boolean", FxDataType.Boolean);
         TEST_PROPS.put("binary", FxDataType.Binary);
         TEST_PROPS.put("reference", FxDataType.Reference);
         TEST_PROPS.put("selectOne", FxDataType.SelectOne);
         TEST_PROPS.put("selectMany", FxDataType.SelectMany);
         TEST_PROPS.put("dateRange", FxDataType.DateRange);
         TEST_PROPS.put("dateTimeRange", FxDataType.DateTimeRange);
     }
 
     private final List<Long> generatedNodeIds = new ArrayList<Long>();
 
     @BeforeClass
     public void setup() throws Exception {
         login(TestUsers.REGULAR);
         final List<FxPK> testPks = new SqlQueryBuilder().select("@pk").type(TEST_TYPE).getResult().collectColumn(1);
         // link test instances in tree
         for (FxPK pk : testPks) {
             generatedNodeIds.add(
                     getTreeEngine().save(FxTreeNodeEdit.createNew("test" + pk)
                             .setReference(pk).setName(RandomStringUtils.random(new Random().nextInt(1024), true, true)))
             );
         }
     }
 
     @AfterClass
     public void shutdown() throws Exception {
         for (long nodeId : generatedNodeIds) {
             getTreeEngine().remove(
                     FxTreeNodeEdit.createNew("").setId(nodeId).setMode(FxTreeMode.Edit),
                     FxTreeRemoveOp.Unfile, true);
         }
         logout();
     }
 
     @Test
     public void simpleSelectTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption", "comment").enterSub(QueryOperatorNode.Operator.AND)
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment%")
                 .closeSub().getResult();
         assertTrue(result.getRowCount() == 25, "Expected to fetch 25 rows, got: " + result.getRowCount());
         assertTrue(result.getColumnIndex("caption") == 1, "Unexpected column index for caption: " + result.getColumnIndex("caption"));
         assertTrue(result.getColumnIndex("comment") == 2, "Unexpected column index for comment: " + result.getColumnIndex("comment"));
         for (int i = 1; i <= result.getRowCount(); i++) {
             assertTrue(result.getString(i, 1).startsWith("test caption"), "Unexpected column value: " + result.getString(i, 1));
             assertTrue(result.getString(i, 2).startsWith("folder comment"), "Unexpected column value: " + result.getString(i, 2));
         }
     }
 
     @Test
     public void simpleNestedQueryTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption").andSub()
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .orSub().condition("comment", PropertyValueComparator.LIKE, "folder comment 1")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment 2").closeSub().getResult();
         assertTrue(result.getRowCount() == 2, "Expected to fetch 2 rows, got: " + result.getRowCount());
         for (int i = 1; i <= 2; i++) {
             assertTrue(result.getString(i, 1).matches("test caption [12]"), "Unexpected column value: " + result.getString(i, 1));
         }
     }
 
     @Test
     public void combinedFlatAssignment() throws FxApplicationException {
         final FxType type = CacheAdmin.getEnvironment().getType(TEST_TYPE);
         if (!type.isContainsFlatStorageAssignments()) {
             return;     // test affects only flat storage queries
         }
         // use two assignments, if possible on two different flat storage levels
         FxPropertyAssignment first = null;
         FxPropertyAssignment second = null;
         for (FxPropertyAssignment pa : type.getAssignmentsForDataType(FxDataType.String1024)) {
             if (pa.isFlatStorageEntry()) {
                 if (first == null) {
                     first = pa;
                 } else if (second == null) {
                     second = pa;
                 } else if (second.getFlatStorageMapping().getLevel() == first.getFlatStorageMapping().getLevel()) {
                     // overwrite until second flat storage mapping is on a different level
                     // (depending on the DB, it is also possible that all assignments fit in one row)
                     second = pa;
                 }
             }
         }
         if (first == null || second == null) {
             fail("No suitable assignments found");
         }
 
         // get test values
         //noinspection ConstantConditions
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "#" + first.getId(), "#" + second.getId()).type(TEST_TYPE).getResult();
         assertTrue(result.getRowCount() > 0);
         final FxPK pk = result.getResultRow(0).getPk(1);
         final Object firstValue = result.getResultRow(0).getValue(2);
         final Object secondValue = result.getResultRow(0).getValue(3);
         assertNotNull(firstValue);
         assertNotNull(secondValue);
 
         // issue a query on these conditions with AND and OR
         for (SqlQueryBuilder builder : Arrays.asList(
                 new SqlQueryBuilder().select("@pk").andSub(),
                 new SqlQueryBuilder().select("@pk").orSub()
         )) {
             final FxResultSet filterResult = builder
                     .condition("#" + first.getId(), PropertyValueComparator.EQ, firstValue)
                     .condition("#" + second.getId(), PropertyValueComparator.EQ, secondValue)
                     .getResult();
             assertTrue(filterResult.getRowCount() > 0);
             assertTrue(filterResult.collectColumn(1).contains(pk), "Expected PK " + pk + " not returned in result.");
         }
     }
 
 
     /**
      * Check if the SQL search for empty string properties works.
      *
      * @throws FxApplicationException if the search failed
      */
     @Test
     public void stringEmptyQuery() throws FxApplicationException {
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.EMPTY, null).getResult();
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.NOT_EMPTY, null).getResult();
         new SqlQueryBuilder().condition("caption", PropertyValueComparator.EMPTY, null)
                 .orSub().condition("caption", PropertyValueComparator.EMPTY, null).condition("caption", PropertyValueComparator.NOT_EMPTY, null)
                 .closeSub().getResult();
     }
 
     @Test
     public void selectUserTest() throws FxApplicationException {
         for (FxResultRow row : new SqlQueryBuilder().select("created_by", "created_by.username").getResult().getResultRows()) {
             final Account account = EJBLookup.getAccountEngine().load(((FxLargeNumber) row.getFxValue(1)).getDefaultTranslation());
             assertEquals(row.getFxValue(2).getDefaultTranslation(), account.getName());
         }
     }
 
     @Test
     public void filterByTypeTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("typedef").filterType("FOLDER").getResult();
         assertTrue(result.getRowCount() > 0);
         final FxType folderType = CacheAdmin.getEnvironment().getType("FOLDER");
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(folderType.getId() == ((FxLargeNumber) row.getValue(1)).getBestTranslation(),
                     "Unexpected result type: " + row.getValue(1) + ", expected: " + folderType.getId());
         }
     }
 
     @Test
     public void briefcaseQueryTest() throws FxApplicationException {
         // create briefcase
         final String selectFolders = new SqlQueryBuilder().select("@pk").type("FOLDER").getQuery();
         final FxSQLSearchParams params = new FxSQLSearchParams().saveResultInBriefcase("test briefcase", "description", (Long) null);
         final FxResultSet result = getSearchEngine().search(selectFolders, 0, Integer.MAX_VALUE, params);
         long bcId = result.getCreatedBriefcaseId();
         try {
             assertTrue(result.getRowCount() > 1);
             assertTrue(result.getCreatedBriefcaseId() != -1, "Briefcase should have been created, but no ID returned.");
 
             // store some metadata
             final FxReferenceMetaData<FxPK> meta1 = FxReferenceMetaData.createNew(result.getResultRow(0).getPk(1));
             meta1.put("firstkey", "somevalue");
             final FxReferenceMetaData<FxPK> meta2 = FxReferenceMetaData.createNew(result.getResultRow(1).getPk(1));
             meta2.put("secondkey", "anothervalue");
             getBriefcaseEngine().mergeMetaData(bcId, Arrays.asList(meta1, meta2));
 
             final List<FxReferenceMetaData<FxPK>> dbMeta = getBriefcaseEngine().getMetaData(bcId);
             assertEquals(FxReferenceMetaData.findByContent(dbMeta, meta1.getReference()).get("firstkey"), "somevalue");
             assertEquals(FxReferenceMetaData.findByContent(dbMeta, meta2.getReference()).get("secondkey"), "anothervalue");
 
             // select briefcase
             final FxResultSet briefcase = new SqlQueryBuilder()
                     .select("@pk", "caption", "@metadata")
                     .filterBriefcase(result.getCreatedBriefcaseId())
                     .getResult();
             Assert.assertEquals(briefcase.getTotalRowCount(), result.getTotalRowCount(),
                     "Briefcase returned " + briefcase.getTotalRowCount() + " rows, but getResult returned " + result.getTotalRowCount() + " rows.");
 
             // check metadata
             final List<FxReferenceMetaData<FxPK>> meta = briefcase.collectColumn(3);
             assertEquals(FxReferenceMetaData.findByContent(meta, meta1.getReference()).get("firstkey"), "somevalue");
             assertEquals(FxReferenceMetaData.findByContent(meta, meta2.getReference()).get("secondkey"), "anothervalue");
         } finally {
             getBriefcaseEngine().remove(bcId);
         }
     }
 
 
     @Test
     public void queryBuilderBriefcaseTest() throws FxApplicationException {
         long briefcaseId = -1;
         try {
             final FxResultSet result = new SqlQueryBuilder().saveInBriefcase("SqlQueryBuilderTest").getResult();
             assertTrue(result.getRowCount() > 0);
             briefcaseId = result.getCreatedBriefcaseId();
             final Briefcase briefcase = getBriefcaseEngine().load(briefcaseId);
             Assert.assertEquals(briefcase.getSize(), result.getTotalRowCount(),
                     "Invalid briefcase size: " + briefcase.getSize() + ", expected: " + result.getTotalRowCount());
         } finally {
             if (briefcaseId != -1) {
                 getBriefcaseEngine().remove(briefcaseId);
             }
         }
     }
 
     @Test
     public void typeConditionTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("typedef").type(FxType.CONTACTDATA).getResult();
         final FxType cdType = CacheAdmin.getEnvironment().getType(FxType.CONTACTDATA);
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(((FxLargeNumber) row.getFxValue(1)).getDefaultTranslation() == cdType.getId(),
                     "Unexpected type in result, expected " + cdType.getId() + ", was: " + row.getFxValue(1));
         }
     }
 
     @Test
     public void typeRangeTest() throws FxApplicationException {
         final FxResultSet rs1 = getSearchEngine().search("SELECT @pk WHERE typedef >= \'ROOT\'");
         assertTrue(rs1.getContentTypes().size() > 1, "Expected more than one type: " + rs1.getContentTypes());
 
         final FxResultSet rs2 = getSearchEngine().search("SELECT @pk WHERE typedef='cmis_property_perm'");
         assertTrue(rs2.getContentTypes().size() == 1, "Expected one type: " + rs2.getContentTypes());
 
         final FxResultSet rs3 = getSearchEngine().search("SELECT @pk WHERE typedef>='cmis_property_perm'");
         assertTrue(rs3.getContentTypes().size() == 2, "Expected two types: " + rs3.getContentTypes());
         assertTrue(rs3.getContentTypes().contains(rs2.getContentTypes().get(0)));
 
         final FxResultSet rs4 = getSearchEngine().search("SELECT @pk WHERE typedef < \'cmis_property_perm_secured\'");
         assertTrue(rs4.getContentTypes().size() == 1, "Expected one type: " + rs4.getContentTypes());
         assertTrue(rs4.getContentTypes().equals(rs2.getContentTypes()));
     }
 
     /**
      * Generic tests on the SearchTest type.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericSelectTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(
                 getTestPropertyName(name)).type(TEST_TYPE).getResult();
         final int testInstanceCount = getTestInstanceCount();
         Assert.assertEquals(result.getRowCount(), testInstanceCount, "Expected all test instances to be returned, got "
                 + result.getRowCount() + " instead of " + testInstanceCount);
         final int idx = 1;
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(dataType.getValueClass().isAssignableFrom(row.getFxValue(idx).getClass()),
                     "Invalid class returned for datatype " + dataType + ": " + row.getFxValue(idx).getClass() + " instead of " + dataType.getValueClass());
             assertTrue(row.getFxValue(idx).getXPathName() != null, "XPath was null");
             assertTrue(row.getFxValue(idx).getXPathName().equalsIgnoreCase(getTestPropertyName(name)), "Invalid property name: " + row.getFxValue(idx).getXPathName() + ", expected: " + getTestPropertyName(name));
         }
     }
 
     /**
      * Tests the selection of null values.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericSelectNullTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(TEST_TYPE + "/" + getTestPropertyName(name))
                 .condition("typedef", PropertyValueComparator.NE, CacheAdmin.getEnvironment().getType(TEST_TYPE).getName())
                 .getResult();
         assertTrue(result.getRowCount() > 0);
         final int idx = 1;
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(dataType.getValueClass().isAssignableFrom(row.getFxValue(idx).getClass()),
                     "Invalid class returned for datatype " + dataType + ": " + row.getFxValue(idx).getClass() + " instead of " + dataType.getValueClass());
             assertTrue(row.getFxValue(idx).isEmpty(), "Value should be empty: " + row.getFxValue(idx));
         }
     }
 
     @Test
     public void selectVirtualPropertiesTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@path", "@node_position", "@permissions",
                 getTestPropertyName("string")).type(TEST_TYPE).getResult();
         final int idx = 5;
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(getTestPropertyName("string").equalsIgnoreCase(row.getFxValue(idx).getXPathName()),
                     "Invalid property name from XPath: " + row.getFxValue(idx).getXPathName()
                             + ", expected: " + getTestPropertyName("string"));
         }
     }
 
     @Test(dataProvider = "testProperties")
     public void orderByTest(String name, FxDataType dataType) throws FxApplicationException {
         if (dataType == FxDataType.SelectMany) {
             return; // ordering for selectmany is not defined
         }
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName(name)).type(TEST_TYPE)
                 .orderBy(2, SortDirection.ASCENDING).getResult();
         assertTrue(result.getRowCount() > 0);
         assertAscendingOrder(result, 2);
     }
 
     @Test
     public void multipleOrderByTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string"), getTestPropertyName("number"))
                 .type(TEST_TYPE)
                 .orderByIndices(
                         Pair.newPair(3, SortDirection.ASCENDING),
                         Pair.newPair(2, SortDirection.DESCENDING)
                 ).getResult();
         assertAscendingOrder(result, 3);
     }
 
     @Test
     public void orderByWithLimitTest() throws FxApplicationException {
         final SqlQueryBuilder sqb = new SqlQueryBuilder()
                 .select("@pk", getTestPropertyName("string"))
                 .type(TEST_TYPE)
                 .orderBy(2, SortDirection.ASCENDING);
 
         final int rows = 5;
         final int windowSize = 3;
 
         // load all rows
         final FxResultSet refResult = sqb.getResult();
         assertTrue(refResult.getRowCount() > rows+windowSize, "Not enough rows: " + refResult.getRowCount());
 
         final List<FxPK> refColumn2 = refResult.collectColumn(2);
         // load subset with a sliding window
         for (int i = 0; i < windowSize; i++) {
             final FxResultSet result = sqb.startRow(i).fetchRows(rows).getResult();
             assertEquals(result.getRowCount(), rows);
             assertEquals(result.collectColumn(2), refColumn2.subList(i, i + rows));
         }
     }
 
     @Test
     public void selectSystemPropertiesTest() throws FxApplicationException {
         final String[] props = {"id", "version", "typedef", "mandator", "acl", "created_by",
                 "created_at", "modified_by", "modified_at"};
         final FxResultSet result = new SqlQueryBuilder().select(props).maxRows(50).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             for (String property : props) {
                 final Object value = row.getValue(property);
                 assertTrue(value != null);
                 if (value instanceof FxValue) {
                     final FxValue fxValue = (FxValue) value;
                     assertTrue(StringUtils.isNotBlank(fxValue.getXPath()) && !"/".equals(fxValue.getXPath()),
                             "XPath not set in search result for system property " + property);
                 }
             }
         }
     }
 
     @Test
     public void selectPermissionsTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(TEST_TYPE).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final FxPK pk = row.getPk(1);
             final PermissionSet permissions = row.getPermissions(2);
             assertTrue(permissions.isMayRead());
             final PermissionSet contentPerms = getContentEngine().load(pk).getPermissions();
             assertTrue(contentPerms.equals(permissions), "Permissions from search: " + permissions + ", content: " + contentPerms);
         }
     }
 
     @Test
     public void selectLockTest() throws FxApplicationException {
         // lock some instances
         final List<FxPK> locked = Lists.newArrayList();
         try {
             for (FxPK pk : new SqlQueryBuilder().select("@pk").type(TEST_TYPE).getResult().<FxPK>collectColumn(1)) {
                 if (pk.getId() % 3 == 0) {
                     getContentEngine().lock(FxLockType.Permanent, pk);
                     locked.add(pk);
                 }
             }
             assertTrue(locked.size() > 0);
 
             final FxResultSet result = new SqlQueryBuilder().select("@pk", "@lock").type(TEST_TYPE).getResult();
             assertTrue(result.getRowCount() > 0);
             for (FxResultRow row : result.getResultRows()) {
                 final FxPK pk = row.getPk(1);
                 final FxLock lock = row.getLock(2);
                 if (lock == null) {
                     assertFalse(locked.contains(pk));
                     continue;
                 }
                 assertEquals(lock.getLockedPK(), pk);
                 assertEquals(lock.getLockType(), locked.contains(pk) ? FxLockType.Permanent : FxLockType.None);
                 assertEquals(lock.getUserId(), FxContext.getUserTicket().getUserId());
                 assertTrue(lock.getCreatedDate().getTime() > 0);
                 assertTrue(lock.getExpiresDate().getTime() > 0);
             }
         } finally {
             for (FxPK pk : locked) {
                 getContentEngine().unlock(pk);
             }
         }
     }
 
 
     @Test
     public void selectBinaryTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().type("image").select("@pk", "imageBinary").getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final BinaryDescriptor binary = ((FxBinary) row.getFxValue("imageBinary")).getBestTranslation();
             assertFalse(binary.isNewBinary());
             assertNotNull(binary.getName());
             assertNotNull(binary.getMimeType());
             assertTrue(binary.getSize() > 0, "Expected size to be greater than 0");
         }
     }
 
     @Test
     public void orderByResultPreferencesTest() throws FxApplicationException {
         setResultPreferences(SortDirection.ASCENDING);
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).filterType(TEST_TYPE).getResult();
         assertAscendingOrder(result, 2);
 
         setResultPreferences(SortDirection.DESCENDING);
         final FxResultSet descendingResult = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).filterType(TEST_TYPE).getResult();
         assertDescendingOrder(descendingResult, 2);
     }
 
     @Test
     public void explicitOrderByResultPreferencesTest() throws FxApplicationException {
         setResultPreferences(SortDirection.ASCENDING);
         // sort by the second column taken from the result preferences - needs "virtual" columns
         final FxResultSet result = getSearchEngine().search(
                 "SELECT @pk, id, @* FROM content CO FILTER type='" + TEST_TYPE + "' ORDER BY 4 DESC", 0, 9999, null);
         assertTrue(result.getRowCount() > 0);
         assertDescendingOrder(result, 4);
 
         try {
             getSearchEngine().search(
                     "SELECT co.@pk, co.id, co.@* FROM content CO FILTER co.type='" + TEST_TYPE + "' ORDER BY 5 DESC", 0, 9999, null);
             fail("Selected result preference column should not be present in result set");
         } catch (FxApplicationException e) {
             // pass
         }
 
         // also test using SqlQueryBuilder
         assertDescendingOrder(new SqlQueryBuilder().select("@pk", "id", "*")
                 .orderBy(4, SortDirection.DESCENDING)
                 .filterType(TEST_TYPE).getResult(), 4);
         try {
             final int columnIndex = 9;  // number of default result preferences in ResultPreferencesEngineBean + 2 selected column + 1
             new SqlQueryBuilder().select("@pk", "id", "@*").orderBy(columnIndex, SortDirection.ASCENDING).getResult();
             fail("Selected result preference column should not be present in result set");
         } catch (FxApplicationException e) {
             // pass - the exception was thrown by the search engine, so it's a FxApplicationException
             // instead of a FxRuntimeException
         }
     }
 
     private void setResultPreferences(SortDirection sortDirection) throws FxApplicationException {
         final ResultPreferencesEdit prefs = new ResultPreferencesEdit(Arrays.asList(
                 new ResultColumnInfo(Table.CONTENT, getTestPropertyName("string"), ""),
                 new ResultColumnInfo(Table.CONTENT, getTestPropertyName("number"), "")
         ), Arrays.asList(
                 new ResultOrderByInfo(Table.CONTENT, getTestPropertyName("string"), "", sortDirection)),
                 25, 100);
         EJBLookup.getResultPreferencesEngine().save(prefs, CacheAdmin.getEnvironment().getType(TEST_TYPE).getId(), ResultViewType.LIST, AdminResultLocations.DEFAULT);
     }
 
     private static List<FxPK> folderPks;
 
     private static synchronized List<FxPK> getFolderPks() throws FxApplicationException {
         if (folderPks == null) {
             folderPks = new SqlQueryBuilder().select("@pk").type("folder").getResult().collectColumn(1);
         }
         return folderPks;
     }
 
     /**
      * Tests all available value comparators for the given datatype. Note that no semantic
      * tests are performed, each comparator is executed with a random value.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericConditionTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment(name);
         final Random random = new Random(0);
 
         // need to get some folder IDs for the reference property
         final List<FxPK> folderPks = getFolderPks();
 
         for (PropertyValueComparator comparator : PropertyValueComparator.getAvailable(dataType)) {
             for (String prefix : new String[]{
                     TEST_TYPE + "/",
                     TEST_TYPE + "/groupTop/",
                     TEST_TYPE + "/groupTop/groupNested/"
             }) {
                 final String assignmentName = prefix + getTestPropertyName(name);
                 try {
                     // submit a query with the given property/comparator combination
                     final FxValue value;
                     switch (dataType) {
                         case Reference:
                             value = new FxReference(new ReferencedContent(folderPks.get(random.nextInt(folderPks.size()))));
                             break;
                         case DateRange:
                             // a query is always performed against a particular date, but not a date range
                             value = new FxDate(new Date());
                             break;
                         case DateTimeRange:
                             value = new FxDateTime(new Date());
                             break;
                         default:
                             value = dataType.getRandomValue(random, assignment);
                     }
                     new SqlQueryBuilder().condition(assignmentName, comparator, value).getResult();
                     // no exception thrown, consider it a success
                 } catch (Exception e) {
                     fail("Failed to submit for property " + dataType + " with comparator " + comparator
                             + ":\n" + e.getMessage() + ", thrown at:\n"
                             + StringUtils.join(e.getStackTrace(), '\n'));
                 }
             }
         }
     }
 
     /**
      * Tests date and datetime queries with functions, e.g. YEAR(dateprop)=2008
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void dateConditionFunctionsTest() throws FxApplicationException {
         for (String name : new String[]{"date", "datetime"}) {
             testDateFunctionLT(name, 2020, DateFunction.YEAR, Calendar.YEAR);
             testDateFunctionLT(name, 8, DateFunction.MONTH, Calendar.MONTH);
             testDateFunctionLT(name, 20, DateFunction.DAY, Calendar.DAY_OF_MONTH);
         }
         testDateFunctionLT("datetime", 12, DateFunction.HOUR, Calendar.HOUR);
         testDateFunctionLT("datetime", 30, DateFunction.MINUTE, Calendar.MINUTE);
         testDateFunctionLT("datetime", 30, DateFunction.SECOND, Calendar.SECOND);
     }
 
     private void testDateFunctionLT(String name, int value, DateFunction dateFunction, int calendarField) throws FxApplicationException {
         final String assignmentName = TEST_TYPE + "/" + getTestPropertyName(name);
         final SqlQueryBuilder builder = getDateQuery(assignmentName, PropertyValueComparator.LT, value, dateFunction);
         final FxResultSet result = builder.getResult();
         assertTrue(result.getRowCount() > 0, "Query returned no results:\n\n" + builder.getQuery());
         final Calendar cal = Calendar.getInstance();
         for (FxResultRow row : result.getResultRows()) {
             cal.setTime(row.getDate(1));
             assertTrue(cal.get(calendarField) < value, row.getDate(1) + " should be before " + value + ", data type = " + name);
         }
     }
 
     private SqlQueryBuilder getDateQuery(String datePropertyName, PropertyValueComparator comparator, int year, DateFunction dateFunction) throws FxApplicationException {
         return new SqlQueryBuilder().select(datePropertyName)
                 .condition(dateFunction.getSqlName() + "(" + datePropertyName + ")", comparator, year);
     }
 
     @Test
     public void dateSelectFunctionsTest() throws FxApplicationException {
         final int year = Calendar.getInstance().get(Calendar.YEAR);
         // get objects created this year
         FxResultSet result = new SqlQueryBuilder().select("year(created_at)").condition("year(created_at)", PropertyValueComparator.EQ, year).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(row.getInt(1) == year, "Expected " + year + ", got: " + row.getInt(1));
         }
 
         // select a date from the details table
         final String dateAssignmentName = TEST_TYPE + "/" + getTestPropertyName("date");
         result = new SqlQueryBuilder().select(dateAssignmentName, "year(" + dateAssignmentName + ")")
                 .condition(dateAssignmentName, PropertyValueComparator.NOT_EMPTY, null).getResult();
         assertTrue(result.getRowCount() > 0);
         final Calendar cal = Calendar.getInstance();
         for (FxResultRow row : result.getResultRows()) {
             cal.setTime(row.getDate(1));
             assertTrue(cal.get(Calendar.YEAR) == row.getInt(2), "Expected year " + cal.get(Calendar.YEAR) + ", got: " + row.getInt(2));
         }
 
         // check if it is possible to select the same column twice, but with different functions
         result = new SqlQueryBuilder().select("year(created_at)", "month(created_at)").condition("year(created_at)", PropertyValueComparator.EQ, year).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             assertTrue(row.getInt(1) == year, "Expected " + year + ", got: " + row.getInt(1));
             assertTrue(row.getInt(2) != year && row.getInt(2) <= 12, "Invalid month value: " + row.getInt(2));
         }
     }
 
     @Test
     public void selectManyInTest() throws FxApplicationException {
         final String assignmentName = TEST_TYPE + "/" + getTestPropertyName("selectMany");
         final FxResultSet result = new SqlQueryBuilder().select("@pk", assignmentName).type(TEST_TYPE).getResult();
         assertTrue(result.getRowCount() > 0);
 
         // find an object with more than 2 selected options
         List<FxSelectListItem> queryItems = new ArrayList<FxSelectListItem>();
         for (FxResultRow row : result.getResultRows()) {
             final List<FxSelectListItem> selected = ((FxSelectMany) row.getFxValue(2)).getBestTranslation().getSelected();
             if (selected.size() > 2) {
                 // select first two
                 queryItems.add(selected.get(0));
                 queryItems.add(selected.get(1));
                 if (getExpectedMatchRows(result, queryItems) == result.getRowCount()
                         || getExpectedPartialMatches(result, queryItems) == result.getRowCount()) {
                     // exact or partial match for this selection would match all rows - not suitable for a query test
                     queryItems.clear();
                 } else {
                     break;
                 }
             }
         }
         assertFalse(queryItems.isEmpty(), "Failed to find a suitable selectMany property");
         assertEquals(queryItems.size(), 2);
 
         final int matchingRows = getExpectedMatchRows(result, queryItems);
         final int partialMatchingRows = getExpectedPartialMatches(result, queryItems);
 
         // select using IN
         final FxResultSet conditionResult = new SqlQueryBuilder().select("@pk", assignmentName)
                 .type(TEST_TYPE)
                 .condition(assignmentName, PropertyValueComparator.IN, FxSharedUtils.getSelectableObjectIdList(queryItems))
                 .getResult();
         assertEquals(conditionResult.getRowCount(), matchingRows, "Wrong result row count for IN query.");
 
         // test inverse match using NOT IN
         final FxResultSet invResult = new SqlQueryBuilder().select("@pk", assignmentName)
                 .type(TEST_TYPE)
                 .condition(assignmentName, PropertyValueComparator.NOT_IN, queryItems)
                 .getResult();
         assertEquals(invResult.getRowCount(), result.getRowCount() - partialMatchingRows, "Wrong result row count for NOT IN query.");
 
         for (FxResultRow invRow : invResult.getResultRows()) {
             for (FxResultRow row : conditionResult.getResultRows()) {
                 assertFalse(row.getPk(1).equals(invRow.getPk(1)), "Row returned by IN and NOT IN: " + row.getPk(1));
             }
         }
     }
 
     @Test
     public void selectScalarInTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk").orSub()
                 .condition("typedef", PropertyValueComparator.IN,
                         FxSharedUtils.getSelectableObjectIdList(CacheAdmin.getEnvironment().getTypes())
                         .subList(0, 3)
                 )
                 .condition("caption", PropertyValueComparator.IN, Arrays.asList("test1", "test2"))
                 .maxRows(10)
                 .getResult();
         assertTrue(result.getRowCount() > 0);
     }
 
     private int getExpectedPartialMatches(FxResultSet result, final List<FxSelectListItem> queryItems) {
         return Iterables.size(Iterables.filter(result.getResultRows(), new Predicate<FxResultRow>() {
             public boolean apply(FxResultRow row) {
                 final SelectMany selectMany = ((FxSelectMany) row.getValue(2)).getBestTranslation();
                 return !Collections.disjoint(queryItems, selectMany.getSelected());
             }
         }));
     }
 
     private int getExpectedMatchRows(FxResultSet result, final List<FxSelectListItem> queryItems) {
         return Iterables.size(Iterables.filter(result.getResultRows(), new Predicate<FxResultRow>() {
             public boolean apply(FxResultRow row) {
                 final SelectMany selectMany = ((FxSelectMany) row.getValue(2)).getBestTranslation();
                 return selectMany.getSelected().containsAll(queryItems);
             }
         }));
     }
 
     /**
      * Tests relative comparators like &lt; and == for all datatypes that support them.
      *
      * @param name     the base property name
      * @param dataType the datatype of the property
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on search engine errors
      */
     @Test(dataProvider = "testProperties")
     public void genericRelativeComparatorsTest(String name, FxDataType dataType) throws FxApplicationException {
         final String assignmentName = TEST_TYPE + "/" + getTestPropertyName(name);
 
         for (PropertyValueComparator comparator : PropertyValueComparator.getAvailable(dataType)) {
             if (!(comparator.equals(EQ) || comparator.equals(PropertyValueComparator.GE)
                     || comparator.equals(PropertyValueComparator.GT) || comparator.equals(PropertyValueComparator.LE)
                     || comparator.equals(PropertyValueComparator.LT))) {
                 continue;
             }
             final FxValue value = getTestValue(name, comparator);
             final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk", assignmentName).condition(assignmentName, comparator, value);
             final FxResultSet result = builder.getResult();
             assertTrue(result.getRowCount() > 0, "Cannot test on empty result sets, query=\n" + builder.getQuery());
             for (FxResultRow row : result.getResultRows()) {
                 final FxValue rowValue = row.getFxValue(2);
                 switch (comparator) {
                     case EQ:
                         assertTrue(rowValue.compareTo(value) == 0,
                                 "Result value " + rowValue + " is not equal to " + value + " (compareTo = "
                                         + rowValue.compareTo(value) + ")\n\nQuery:" + builder.getQuery());
                         Assert.assertEquals(rowValue.getBestTranslation(), value.getBestTranslation(),
                                 "Result value " + rowValue + " is not equal to " + value + "\n\nQuery:" + builder.getQuery());
                         break;
                     case LT:
                         assertTrue(rowValue.compareTo(value) < 0, "Result value " + rowValue + " is not less than " + value + "\n\nQuery:" + builder.getQuery());
                         break;
                     case LE:
                         assertTrue(rowValue.compareTo(value) <= 0, "Result value " + rowValue + " is not less or equal to " + value + "\n\nQuery:" + builder.getQuery());
                         break;
                     case GT:
                         assertTrue(rowValue.compareTo(value) > 0, "Result value " + rowValue + " is not greater than " + value + "\n\nQuery:" + builder.getQuery());
                         break;
                     case GE:
                         assertTrue(rowValue.compareTo(value) >= 0, "Result value " + rowValue + " is not greater or equal to " + value + "\n\nQuery:" + builder.getQuery());
                         break;
                     default:
                         fail("Invalid comparator: " + comparator + "\n\nQuery:" + builder.getQuery());
                 }
             }
         }
     }
 
     /**
      * Finds a FxValue of the test instances that matches some of the result rows
      * for the given comparator, but not all or none.
      *
      * @param name       the test property name
      * @param comparator the comparator
      * @return a value that matches some rows
      * @throws FxApplicationException on search engine errors
      */
     private FxValue getTestValue(String name, PropertyValueComparator comparator) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(TEST_TYPE + "/" + getTestPropertyName(name)).type(TEST_TYPE).getResult();
         final List<FxValue> values = result.collectColumn(1);
         final int testInstanceCount = getTestInstanceCount();
         assertTrue(values.size() == testInstanceCount, "Expected " + testInstanceCount + " rows, got: " + values.size());
         for (FxValue value : values) {
             if (value == null || value.isEmpty()) {
                 continue;
             }
             int match = 0;  // number of matched values for the given comparator
             int count = 0;  // number of values checked so far
             for (FxValue value2 : values) {
                 count++;
                 if (value2 == null || value2.isEmpty()) {
                     continue;
                 }
                 switch (comparator) {
                     case EQ:
                         if (value.getBestTranslation().equals(value2.getBestTranslation())) {
                             match++;
                         }
                         break;
                     case LT:
                         if (value2.compareTo(value) < 0) {
                             match++;
                         }
                         break;
                     case LE:
                         if (value2.compareTo(value) <= 0) {
                             match++;
                         }
                         break;
                     case GT:
                         if (value2.compareTo(value) > 0) {
                             match++;
                         }
                         break;
                     case GE:
                         if (value2.compareTo(value) >= 0) {
                             match++;
                         }
                         break;
                     default:
                         fail("Cannot check relative ordering for comparator " + comparator);
                 }
             }
             if (match > 0 && count > match && match < values.size()) {
                 // this value is matched by _some_ other row values, so it's suitable as test input
                 if (value instanceof FxDateRange) {
                     // daterange checks are performed against an actual date, not another range
                     return new FxDate(((FxDateRange) value).getBestTranslation().getLower());
                 } else if (value instanceof FxDateTimeRange) {
                     // see above
                     return new FxDateTime(((FxDateTimeRange) value).getBestTranslation().getLower());
                 }
                 return value;
             }
         }
         throw new IllegalArgumentException("Failed to find a suitable test value for property " + getTestPropertyName(name)
                 + " and comparator " + comparator);
     }
 
     @Test
     public void aclSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "acl", "acl.name",
                 "acl.mandator", "acl.description", "acl.cat_type", "acl.color", "acl.created_by", "acl.created_at",
                 "acl.modified_by", "acl.modified_at").type(TEST_TYPE).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final ACL acl = CacheAdmin.getEnvironment().getACL(row.getLong("acl"));
             final FxContent content = getContentEngine().load(row.getPk(1));
             Assert.assertTrue(content.getAclIds().contains(acl.getId()),
                     "Invalid ACL for instance " + row.getPk(1) + ": " + acl.getId()
                             + ", content engine returned " + content.getAclIds().get(0));
 
             // check fields selected directly from the ACL table
             assertEquals(row.getString("acl.name"), (Object) acl.getName(), "Invalid value for field: name");
             assertEquals(row.getLong("acl.mandator"), (Object) acl.getMandatorId(), "Invalid value for field: mandator");
             assertEquals(row.getString("acl.description"), (Object) acl.getDescription(), "Invalid value for field: description");
             assertEquals(row.getInt("acl.cat_type"), (Object) acl.getCategory().getId(), "Invalid value for field: category");
             assertEquals(row.getString("acl.color"), (Object) acl.getColor(), "Invalid value for field: color");
             checkLifecycleInfo(row, "acl", acl.getLifeCycleInfo());
         }
     }
 
     @Test
     public void stepSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "step", "step.label", "step.id", "step.stepdef",
                 "step.workflow", "step.acl").getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final Step step = CacheAdmin.getEnvironment().getStep(row.getLong("step"));
             final StepDefinition definition = CacheAdmin.getEnvironment().getStepDefinition(step.getStepDefinitionId());
             Assert.assertEquals(definition.getLabel().getBestTranslation(), row.getFxValue("step.label").getBestTranslation(),
                     "Invalid step label '" + row.getValue(3) + "', expected: '" + definition.getLabel() + "'");
             try {
                 final FxContent content = getContentEngine().load(row.getPk(1));
                 Assert.assertEquals(content.getStepId(), step.getId(),
                         "Invalid step for instance " + row.getPk(1) + ": " + step.getId()
                                 + ", content engine returned " + content.getStepId());
             } catch (FxNoAccessException e) {
                 fail("Content engine denied read access to instance " + row.getPk(1) + " that was returned by search.");
             }
 
             // check fields selected from the ACL table
             assertEquals(row.getLong("step.id"), step.getId(), "Invalid value for field: id");
             assertEquals(row.getLong("step.stepdef"), step.getStepDefinitionId(), "Invalid value for field: stepdef");
             assertEquals(row.getLong("step.workflow"), step.getWorkflowId(), "Invalid value for field: workflow");
             assertEquals(row.getLong("step.acl"), step.getAclId(), "Invalid value for field: acl");
         }
     }
 
     @Test
     public void selectOneFieldSelectorTest() throws FxApplicationException {
         final String propertyName = getTestPropertyName("selectOne");
         for (String field : new String[] { "id", "name", "data", "color", "pos" }) {
             final FxResultSet result = new SqlQueryBuilder().select(
                     "@pk", propertyName, propertyName + "." + field).getResult();
             assertTrue(result.getRowCount() > 0);
             // TODO: check return values
         }
     }
 
 
     @Test
     public void contactDataSelectTest() throws FxApplicationException {
         // contact data is an example of a content with extended private permissions and no permissions for other users
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(FxType.CONTACTDATA).getResult();
         for (FxResultRow row : result.getResultRows()) {
             try {
                 final FxContent content = getContentEngine().load(row.getPk(1));
                 Assert.assertEquals(content.getPermissions(), row.getPermissions("@permissions"),
                         "Content perm: " + content.getPermissions() + ", search perm: " + row.getPermissions(2));
             } catch (FxNoAccessException e) {
                 fail("Search returned contact data #" + row.getPk(1)
                         + ", but content engine disallows access: " + e.getMessage());
             }
         }
     }
     /**
      * Executes a query without conditions and checks if it returns only instances
      * the user can actually read (a select without conditions is an optimized case of the
      * search that must implement the same security constraints as a regular query).
      *
      * @throws FxApplicationException on errors
      */
     @Test
     public void selectAllPermissionsTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT @pk", 0, 999999, null);
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             try {
                 getContentEngine().load(row.getPk(1));
             } catch (FxNoAccessException e) {
                 fail("Content engine denied read access to instance " + row.getPk(1) + " that was returned by search.");
             }
         }
     }
 
     @Test
     public void mandatorSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "mandator", "mandator.id",
                 "mandator.metadata", "mandator.is_active", "mandator.created_by", "mandator.created_at",
                 "mandator.modified_by", "mandator.modified_at").getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final Mandator mandator = CacheAdmin.getEnvironment().getMandator(row.getLong("mandator"));
             final FxContent content = getContentEngine().load(row.getPk(1));
 
             assertEquals(mandator.getId(), content.getMandatorId(), "Search returned different mandator than content engine");
             assertEquals(row.getLong("mandator.id"), mandator.getId(), "Invalid value for field: id");
             if (row.getValue("mandator.metadata") != null) {
                 long mand = row.getLong("mandator.metadata");
                 if (!(mand == 0 || mand == -1))
                     fail("Invalid mandator: " + mand + "! Expected 0 or -1 (System default or test division)");
             }
             assertEquals(row.getLong("mandator.is_active"), mandator.isActive() ? 1 : 0, "Invalid value for field: is_active");
             checkLifecycleInfo(row, "mandator", mandator.getLifeCycleInfo());
         }
     }
 
     @Test
     public void accountSelectorTest() throws FxApplicationException {
         for (String name : new String[]{"created_by", "modified_by"}) {
             final FxResultSet result = new SqlQueryBuilder().select("@pk", name, name + ".mandator",
                     name + ".username", name + ".password", name + ".email", name + ".contact_id",
                     name + ".valid_from", name + ".valid_to", name + ".description", name + ".created_by",
                     name + ".created_at", name + ".modified_by", name + ".modified_at",
                     name + ".is_active", name + ".is_validated", name + ".lang", name + ".login_name",
                     name + ".allow_multilogin", name + ".default_node").maxRows(10).getResult();
             assertTrue(result.getRowCount() == 10, "Expected 10 result rows");
             for (FxResultRow row : result.getResultRows()) {
                 final Account account = EJBLookup.getAccountEngine().load(row.getLong(name));
                 assertEquals(row.getString(name + ".username"), account.getName(), "Invalid value for field: username");
                 assertEquals(row.getString(name + ".login_name"), account.getLoginName(), "Invalid value for field: login_name");
                 assertEquals(row.getString(name + ".email"), account.getEmail(), "Invalid value for field: email");
                 assertEquals(row.getLong(name + ".contact_id"), account.getContactDataId(), "Invalid value for field: contact_id");
                 assertEquals(row.getBoolean(name + ".is_active"), account.isActive(), "Invalid value for field: is_active");
                 assertEquals(row.getBoolean(name + ".is_validated"), account.isValidated(), "Invalid value for field: is_validated");
                 assertEquals(row.getBoolean(name + ".allow_multilogin"), account.isAllowMultiLogin(), "Invalid value for field: allow_multilogin");
                 assertEquals(row.getLong(name + ".lang"), account.getLanguage().getId(), "Invalid value for field: lang");
                 // default_node is not supported yet
                 //assertEquals(row.getLong(name + ".default_node"), account.getDefaultNode(), "Invalid value for field: default_node");
                 checkLifecycleInfo(row, name, account.getLifeCycleInfo());
             }
         }
     }
 
     private void checkLifecycleInfo(FxResultRow row, String baseName, LifeCycleInfo lifeCycleInfo) {
         assertEquals(row.getLong(baseName + ".created_by"), lifeCycleInfo.getCreatorId(), "Invalid value for field: created_by");
         assertEquals(row.getDate(baseName + ".created_at").getTime(), lifeCycleInfo.getCreationTime(), "Invalid value for field: id");
         assertEquals(row.getLong(baseName + ".modified_by"), lifeCycleInfo.getModificatorId(), "Invalid value for field: modified_by");
         assertEquals(row.getDate(baseName + ".modified_at").getTime(), lifeCycleInfo.getModificationTime(), "Invalid value for field: modified_at");
     }
 
     @Test
     public void treeSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@path").isChild(FxTreeNode.ROOT_NODE).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             final List<FxPaths.Path> paths = row.getPaths(2);
             assertTrue(paths.size() > 0, "Returned no path information for content " + row.getPk(1));
             for (FxPaths.Path path : paths) {
                 assertTrue(path.getItems().size() > 0, "Empty path returned");
                 final FxPaths.Item leaf = path.getItems().get(path.getItems().size() - 1);
                 Assert.assertEquals(leaf.getReferenceId(), row.getPk(1).getId(), "Expected reference ID " + row.getPk(1)
                         + ", got: " + leaf.getReferenceId() + " (nodeId=" + leaf.getNodeId() + ")");
 
                 final String treePath = StringUtils.join(getTreeEngine().getLabels(FxTreeMode.Edit, leaf.getNodeId()), '/');
                 Assert.assertEquals(treePath, path.getCaption(), "Unexpected tree path '" + path.getCaption()
                         + "', expected: '" + treePath + "'");
 
             }
         }
         // test selection via node path
         final FxResultSet pathResult = new SqlQueryBuilder().select("@pk", "@path").isChild("/").getResult();
         Assert.assertEquals(pathResult.getRowCount(), result.getRowCount(), "Path select returned " + pathResult.getRowCount()
                 + " rows, select by ID returned " + result.getRowCount() + " rows.");
         // query a leaf node
         new SqlQueryBuilder().select("@pk").isChild(getTreeEngine().getPathById(FxTreeMode.Edit, pathResult.getResultRow(0).getPk(1).getId()));
     }
 
     @Test
     public void directChildConditions() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk").isDirectChild(FxTreeNode.ROOT_NODE).getResult();
         assertTrue(result.getRowCount() > 0);
         for (FxResultRow row : result.getResultRows()) {
             assertNotNull(
                     getTreeEngine().findChild(FxTreeMode.Edit, FxTreeNode.ROOT_NODE, row.getPk(1)),
                     "No direct child with PK " + row.getPk(1) + " found under the root node."
             );
         }
     }
 
     @Test
     public void treeConditionTest_FX263() throws FxApplicationException {
         // Bug FX-263: all versions of a content are returned in the edit(=max version) tree
 
         // find a suitable test content instance
         FxResultSet result = new SqlQueryBuilder().select("@pk").condition("version", PropertyValueComparator.GT, 1).getResult();
         assertTrue(result.getRowCount() > 0);
         final FxPK pk = result.<FxPK>collectColumn(1).get(0);
         assertTrue(pk.getVersion() > 1);
 
         // create a test folder
         long folderNodeId = -1;
         try {
             folderNodeId = getTreeEngine().save(FxTreeNodeEdit.createNew("treeConditionTest_FX263"));
             // attach child content
             getTreeEngine().save(FxTreeNodeEdit.createNew("").setReference(pk).setParentNodeId(folderNodeId));
 
             // find children in default (=maximum) version filter mode, should return 1 row in maximum version
             result = new SqlQueryBuilder().select("@pk").isChild(folderNodeId).getResult();
             Assert.assertEquals(result.getRowCount(), 1, "Expected one child, got: " + result.getRowCount()
                     + " (" + result.collectColumn(1) + ")");
             // should return maximum version
             assertTrue(result.getResultRow(0).getPk(1).getVersion() == pk.getVersion());
         } finally {
             if (folderNodeId != -1) {
                 try {
                     FxContext.get().runAsSystem();
                     getTreeEngine().remove(FxTreeMode.Edit, folderNodeId, FxTreeRemoveOp.RemoveSingleFiled, true);
                 } finally {
                     FxContext.get().stopRunAsSystem();
                 }
 
             }
         }
     }
 
     @Test
     public void fulltextSearchTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).type(TEST_TYPE).maxRows(1).getResult();
         assertTrue(result.getRowCount() == 1, "Expected only one result, got: " + result.getRowCount());
 
         // perform a fulltext query against the first word
         final FxPK pk = result.getResultRow(0).getPk(1);
         final String[] words = StringUtils.split(((FxString) result.getResultRow(0).getFxValue(2)).getBestTranslation(), ' ');
         assertTrue(words.length > 0);
         assertTrue(words[0].length() > 0, "Null length word: " + words[0]);
         final FxResultSet ftresult = new SqlQueryBuilder().select("@pk").fulltext(words[0]).getResult();
         assertTrue(ftresult.getRowCount() > 0, "Expected at least one result for fulltext query '" + words[0] + "'");
         assertTrue(ftresult.collectColumn(1).contains(pk), "Didn't find pk " + pk + " in result, got: " + ftresult.collectColumn(1));
     }
 
     @Test
     public void versionFilterTest() throws FxApplicationException {
         final List<FxPK> allVersions = getPksForVersion(VersionFilter.ALL);
         final List<FxPK> liveVersions = getPksForVersion(VersionFilter.LIVE);
         final List<FxPK> maxVersions = getPksForVersion(VersionFilter.MAX);
         Collections.sort(allVersions);
         Collections.sort(liveVersions);
         Collections.sort(maxVersions);
         assertTrue(allVersions.size() > 0, "All versions result must not be empty");
         assertTrue(liveVersions.size() > 0, "Live versions result must not be empty");
         assertTrue(maxVersions.size() > 0, "Max versions result must not be empty");
         assertTrue(allVersions.size() > liveVersions.size(), "Expected more than only live versions");
         assertTrue(allVersions.size() > maxVersions.size(), "Expected more than only max versions");
         assertTrue(!liveVersions.equals(maxVersions), "Expected different results for max and live version filter");
         for (FxPK pk : liveVersions) {
             final FxContent content = getContentEngine().load(pk);
             assertTrue(content.isLiveVersion(), "Expected live version for " + pk);
         }
         for (FxPK pk : maxVersions) {
             final FxContent content = getContentEngine().load(pk);
             assertTrue(content.isMaxVersion(), "Expected max version for " + pk);
             assertTrue(content.getVersion() == 1 || !content.isLiveVersion());
         }
     }
 
     @Test
     public void lastContentChangeTest() throws FxApplicationException {
         final long lastContentChange = getSearchEngine().getLastContentChange(false);
         assertTrue(lastContentChange > 0);
         final FxContent content = getContentEngine().initialize(TEST_TYPE);
         content.setAclIds(Arrays.asList(TestUsers.getInstanceAcl().getId()));
         content.setValue("/" + getTestPropertyName("string"), new FxString(false, "lastContentChangeTest"));
         FxPK pk = null;
         try {
             Assert.assertEquals(getSearchEngine().getLastContentChange(false), lastContentChange,
                     "Didn't touch contents, but lastContentChange timestamp was increased");
             pk = getContentEngine().save(content);
             assertTrue(getSearchEngine().getLastContentChange(false) > lastContentChange,
                     "Saved content, but lastContentChange timestamp was not increased: "
                             + getSearchEngine().getLastContentChange(false));
         } finally {
             removePk(pk);
         }
     }
 
     @Test
     public void lastContentChangeTreeTest() throws FxApplicationException {
         final long lastContentChange = getSearchEngine().getLastContentChange(false);
         assertTrue(lastContentChange > 0);
         final long nodeId = getTreeEngine().save(FxTreeNodeEdit.createNew("lastContentChangeTreeTest"));
         try {
             final long editContentChange = getSearchEngine().getLastContentChange(false);
             assertTrue(editContentChange > lastContentChange,
                     "Saved content, but lastContentChange timestamp was not increased: " + editContentChange);
             getTreeEngine().activate(FxTreeMode.Edit, nodeId, false, true);
             assertTrue(getSearchEngine().getLastContentChange(true) >= editContentChange,
                     "Activated content, but live mode lastContentChange timestamp was not increased: "
                             + getSearchEngine().getLastContentChange(true));
             Assert.assertEquals(getSearchEngine().getLastContentChange(false), editContentChange,
                     "Edit tree didn't change, but lastContentChange timestamp was updated");
         } finally {
             FxContext.get().runAsSystem();
             try {
                 try {
                     getTreeEngine().remove(getTreeEngine().getNode(FxTreeMode.Edit, nodeId), FxTreeRemoveOp.Remove, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
                 try {
                     getTreeEngine().remove(getTreeEngine().getNode(FxTreeMode.Live, nodeId), FxTreeRemoveOp.Remove, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void inactiveMandatorResultTest() throws FxApplicationException {
         long mandatorId = -1;
         FxPK pk = null;
         try {
             FxContext.get().runAsSystem();
             mandatorId = getMandatorEngine().create("inactiveMandatorResultTest", true);
             final FxContent content = getContentEngine().initialize(getTestTypeId(), mandatorId, -1, -1, -1);
             content.setValue("/" + getTestPropertyName("string"), new FxString(false, "test value"));
             pk = getContentEngine().save(content);
 
             // content should be retrievable
             assertTrue(new SqlQueryBuilder().condition("id", EQ, pk.getId()).getResult().getRowCount() > 0,
                     "Test value from active mandator not found");
 
             getMandatorEngine().deactivate(mandatorId);
             // content should be removed from result
             assertTrue(new SqlQueryBuilder().condition("id", EQ, pk.getId()).getResult().getRowCount() == 0,
                     "Content from deactivated mandators should not be retrievable");
             try {
                 getContentEngine().load(pk);
                 fail("ContentEngine returned content from deactivated mandator.");
             } catch (FxApplicationException e) {
                 // pass
             }
         } finally {
             try {
                 if (mandatorId != -1) {
                     getMandatorEngine().activate(mandatorId);   // active before removing content
                 }
                 removePk(pk);
                 if (mandatorId != -1) {
                     getMandatorEngine().remove(mandatorId);
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void stringEscapeTest() throws FxApplicationException {
         FxPK pk = null;
         try {
             final FxContent content = getContentEngine().initialize(TEST_TYPE);
             content.setValue("/" + getTestPropertyName("string"), new FxString(false, "te'st"));
             pk = getContentEngine().save(content);
             final FxResultSet result = getSearchEngine().search("SELECT id WHERE "
                     + "#searchtest/" + getTestPropertyName("string") + " = 'te''st'", 0, 10, null);
             assertTrue(result.getRowCount() == 1, "Escaped string property not returned");
             assertTrue(result.getResultRow(0).getLong(1) == pk.getId());
         } finally {
             removePk(pk);
         }
     }
 
     private void removePk(FxPK pk) throws FxApplicationException {
         if (pk != null) {
             getContentEngine().remove(pk);
         }
     }
 
     @Test
     public void dateSearchTest() throws FxApplicationException, ParseException {
         FxPK pk = null;
         try {
             final String dateProperty = getTestPropertyName("date");
             final String dateTimeProperty = getTestPropertyName("dateTime");
 
             final SimpleDateFormat dateFormat = FxFormatUtils.getDateFormat();
             final SimpleDateFormat dateTimeFormat = FxFormatUtils.getDateTimeFormat();
 
             final FxContent content = getContentEngine().initialize(TEST_TYPE);
             content.setValue("/" + dateProperty, new FxDate(false,
                     dateFormat.parse("2008-03-18")));
             content.setValue("/" + dateTimeProperty, new FxDateTime(false,
                     dateTimeFormat.parse("2008-03-18 15:43:25.123")));
             pk = getContentEngine().save(content);
 
             // date query
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, EQ, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.LE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.GE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
 
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.LT, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.GT, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateProperty, PropertyValueComparator.NE, "2008-03-18").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
 
             // datetime query
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, EQ, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.LE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             assertExactPkMatch(pk, new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.GE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().<FxPK>collectColumn(1));
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.LT, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.GT, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
             Assert.assertEquals(new SqlQueryBuilder().select("@pk").condition(dateTimeProperty, PropertyValueComparator.NE, "2008-03-18 15:43:25.123").condition("id", EQ, pk.getId()).getResult().getRowCount(), 0,
                     "No rows should be returned because date condition doesn't match.");
         } finally {
             removePk(pk);
         }
     }
 
     @Test
     public void createdAtTest() throws FxApplicationException {
         final FxContent content = getContentEngine().load(getFolderPks().get(0));
         final Date createdAt = new Date(content.getLifeCycleInfo().getCreationTime());
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", EQ, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.LE, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assertExactPkMatch(content.getPk(), new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.GE, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().<FxPK>collectColumn(1));
         assertTrue(new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.LT, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().getRowCount() == 0);
         assertTrue(new SqlQueryBuilder().select("@pk").condition("created_at", PropertyValueComparator.GT, new FxDateTime(false, createdAt)).condition("id", EQ, content.getPk().getId()).getResult().getRowCount() == 0);
     }
 
     @Test
     public void assignmentQueryTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final FxResultSet result = getSearchEngine().search("SELECT #" + assignment.getId() + " WHERE #" + assignment.getId() + " IS NOT NULL",
                 0, 10, null);
         assertTrue(result.getRowCount() > 0);
     }
 
     @Test
     public void assignmentBuilderQueryTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final String dateProperty = getTestPropertyName("date");
         final FxResultSet result = new SqlQueryBuilder().select("@pk")
                 .condition(assignment, PropertyValueComparator.NOT_EMPTY, null)
                 .condition("YEAR(" + dateProperty + ")", PropertyValueComparator.GT, 0)
                 .condition(TEST_TYPE + "/grouptop/" + dateProperty, PropertyValueComparator.NOT_EMPTY, null)
                 .condition("YEAR(" + TEST_TYPE + "/grouptop/" + dateProperty + ")", PropertyValueComparator.GT, 0)
                 .getResult();
         assertTrue(result.getRowCount() > 0);
     }
 
 
     @Test
     public void commentTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("string");
         final FxResultSet result = getSearchEngine().search("SELECT /* field1 */ id, * \n" +
                 "WHERE -- line comment\n" +
                 "/* comment */ (/* comment*/ id != 0) or (/*comment*/#" + assignment.getId() + " IS NULL)", 0, 10, null);
         assertTrue(result.getRowCount() > 0);
 
     }
 
     @Test
     public void tableAliasTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT tbl.@pk, tbl.caption\n" +
                 "FROM content tbl\n" +
                 "WHERE tbl.id > 0", 0, 10, null);
         assertTrue(result.getRowCount() > 0);
     }
 
     @Test
     public void columnAliasTest() throws FxApplicationException {
         for (String bind: new String[] { "", " AS " }) {
             final FxResultSet result = getSearchEngine().search(
                     "SELECT @pk " + bind + " objectId, caption " + bind + " title\n",
                     0, 10, null);
             assertTrue(result.getRowCount() > 0);
             assertNotNull(result.getResultRow(0).getPk("objectId"));
             assertNotNull(result.getResultRow(0).getString("title"));
         }
     }
 
 
     @Test
     public void selectEmptyPathTest() throws FxApplicationException {
         // select path for all items, will include empty paths like contact data
         final FxResultSet result = getSearchEngine().search("SELECT @path", 0, 99999, null);
         assertTrue(result.getRowCount() > 0);
     }
 
     @Test
     public void orderByAssignmentTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("number");
         final FxResultSet result = new SqlQueryBuilder().select("id", assignment.getXPath())
                 .orderBy(assignment.getXPath(), SortDirection.ASCENDING).getResult();
         assertTrue(result.getRowCount() > 0);
     }
 
     @Test
     public void orderByNestedAssignmentTest() throws FxApplicationException {
         final FxPropertyAssignment assignment = getTestPropertyAssignment("groupTop/number");
         final FxResultSet result = new SqlQueryBuilder().select("id", assignment.getXPath())
                 .orderBy(assignment.getXPath(), SortDirection.ASCENDING).getResult();
         assertTrue(result.getRowCount() > 0);
     }
 
     @Test
     public void searchForStepTest() throws FxApplicationException {
         final FxResultSet result = getSearchEngine().search("SELECT step WHERE step=" + StepDefinition.EDIT_STEP_ID, 0, 10, null);
         assertTrue(result.getRowCount() > 0, "At least one instance expected to be in the 'edit' step");
     }
 
     @Test
     public void searchPropertyPermissionDeleteTest() throws FxApplicationException {
         // create a type with a property that cannot be deleted by other users
         FxContext.get().runAsSystem();
         final long typeId;
         final long aclId;
         try {
             typeId = getTypeEngine().save(FxTypeEdit.createNew("prop_delete_test")
                     .setUseInstancePermissions(false)
                     .setUsePropertyPermissions(true)
                     .setUseStepPermissions(false)
                     .setUseTypePermissions(false));
             aclId = getAclEngine().create("prop_no_delete", new FxString(""),
                     TestUsers.getTestMandator(), "#000000", "property description", ACLCategory.STRUCTURE);
             getAclEngine().assign(aclId, TestUsers.REGULAR.getUserGroupId(),
                     ACLPermission.CREATE, ACLPermission.EDIT, ACLPermission.READ);
             getAssignmentEngine().createProperty(typeId,
                     FxPropertyEdit.createNew(
                             "prop_no_delete", new FxString(""), new FxString(""),
                             FxMultiplicity.MULT_0_1,
                             CacheAdmin.getEnvironment().getACL(aclId),
                             FxDataType.String1024
                     ),
                     "/"
             );
         } finally {
             FxContext.get().stopRunAsSystem();
         }
         FxPK pk = null;
         try {
             // create a test content instance
             FxContent content = getContentEngine().initialize(typeId);
             // don't set the value, removal should be ok
             pk = getContentEngine().save(content);
             // select permissions, delete perm should be set
             assertTrue(new SqlQueryBuilder().select("@permissions")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<PermissionSet>collectColumn(1)
                     .get(0)
                     .isMayDelete(),
                     "Expected delete permission");
             getContentEngine().remove(pk);
 
             content = getContentEngine().initialize(typeId);
             content.setValue("/prop_no_delete", new FxString(false, "test"));
             pk = getContentEngine().save(content);
 
             // select this content instance and see if delete perm is set
 
             // For performance reasons, property delete permissions are not used for the
             // instance permission set. Thus this assert is expected to fail.
 
             /*assert !*/
             new SqlQueryBuilder().select("@permissions")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<PermissionSet>collectColumn(1)
                     .get(0)
                     .isMayDelete();/*
                     : "Delete permission should not be set because of property permissions";*/
 
             // removal should work now
             try {
                 getContentEngine().remove(pk);
             } catch (FxApplicationException e) {
                 fail("Content could not be removed although delete property permission not set");
             }
         } finally {
             FxContext.get().runAsSystem();
             try {
                 if (pk != null) {
                     getContentEngine().remove(pk);
                 }
                 getTypeEngine().remove(typeId);
                 getAclEngine().remove(aclId);
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     @Test
     public void searchLanguageFallbackTest_FX260() throws FxApplicationException {
         final int maxRows = 5;
         final UserTicket ticket = FxContext.getUserTicket();
         final FxLanguage oldLanguage = ticket.getLanguage();
 
         try {
             ticket.setLanguage(getLanguageEngine().load(ENGLISH));
             final FxResultSet result = new SqlQueryBuilder()
                     .select("@pk", TEST_TYPE + "/stringSearchPropML")
                     .type(TEST_TYPE)
                     .condition(TEST_TYPE + "/stringSearchPropML", PropertyValueComparator.NOT_EMPTY, null)
                     .maxRows(maxRows)
                     .getResult();
             assertTrue(result.getRowCount() == maxRows, "Expected " + maxRows + " rows but got " + result.getRowCount());
             final FxPK pk = result.getResultRow(0).getPk(1);
 
             // get reference value from the content engine
             final FxString reference = (FxString) getContentEngine().load(pk).getValue("/stringSearchPropML");
             assertTrue(StringUtils.isNotBlank(reference.getTranslation(ENGLISH)));
             assertTrue(StringUtils.isNotBlank(reference.getTranslation(GERMAN)));
             assertTrue(!reference.getTranslation(ENGLISH).equals(reference.getTranslation(GERMAN)));
 
             // compare translated values in the search result
             checkResultTranslation((FxString) result.getResultRow(0).getFxValue(2), reference, ENGLISH);
 
             ticket.setLanguage(getLanguageEngine().load(GERMAN));
             checkResultTranslation(new SqlQueryBuilder()
                     .select(TEST_TYPE + "/stringSearchPropML")
                     .condition("id", PropertyValueComparator.EQ, pk.getId())
                     .getResult()
                     .<FxString>collectColumn(1)
                     .get(0),
                     reference,
                     GERMAN);
         } finally {
             ticket.setLanguage(oldLanguage);
         }
     }
 
     @Test
     public void searchInOtherLanguagesTest_FX297() throws FxApplicationException {
         final String name = "FX297";
         final FxTreeNodeEdit node = FxTreeNodeEdit.createNew(name);
         node.setLabel(new FxString(FxLanguage.ENGLISH, name));
         final long nodeId = EJBLookup.getTreeEngine().save(node);
         final FxLanguage oldUserLanguage = FxContext.getUserTicket().getLanguage();
         try {
             FxContext.getUserTicket().setLanguage(EJBLookup.getLanguageEngine().load(FxLanguage.ENGLISH));
             queryForCaption(name);
 
             FxContext.getUserTicket().setLanguage(EJBLookup.getLanguageEngine().load(FxLanguage.GERMAN));
             queryForCaption(name);
         } finally {
             FxContext.getUserTicket().setLanguage(oldUserLanguage);
             EJBLookup.getTreeEngine().remove(FxTreeMode.Edit, nodeId, FxTreeRemoveOp.Unfile, false);
         }
     }
 
     @Test
     public void isEmptyQueryTest_FX381() throws FxApplicationException {
         final SqlQueryBuilder builder = new SqlQueryBuilder()
                 .condition(getTestPropertyAssignment("string"), PropertyValueComparator.EMPTY, null);
         assertTrue(builder.getResult().getRows().isEmpty(), "Did not expect to find empty instances");
         FxPK pk = null;
         try {
             pk = getContentEngine().save(getContentEngine().initialize(TEST_TYPE));
             assertFalse(getContentEngine().load(pk).containsValue("/" + getTestPropertyName("string")),
                     "Instance should not contain a value for property " + getTestPropertyName("string"));
             final FxResultSet result = builder.getResult();
             assertTrue(!result.getRows().isEmpty(), "Should have returned instance " + pk);
             assertTrue(result.getRowCount() == 1, "Expected 1 row, got " + result.getRowCount());
         } finally {
             if (pk != null) {
                 getContentEngine().remove(pk);
             }
         }
     }
 
     @Test
     public void virtualPropertyCondition() {
         final SqlQueryBuilder builder = new SqlQueryBuilder().condition("@pk", PropertyValueComparator.EQ, "21.1");
         try {
             builder.getResult();
             fail("Virtual properties like @pk cannot be queried");
         } catch (FxApplicationException e) {
             final StringWriter trace = new StringWriter();
             e.printStackTrace(new PrintWriter(trace));
             assertFalse(e.getCause() instanceof NullPointerException,
                     "Error should not be handled by generic NPE, exception was:\n" + trace
             );
         }
     }
 
     @Test
     public void testNodePosition_FX746() throws FxApplicationException {
         final long parentId = getTreeEngine().save(FxTreeNodeEdit.createNew("FX746").setParentNodeId(FxTreeNode.ROOT_NODE));
         try {
             final int numChildren = 5;
             for (int i = 0; i < numChildren; i++) {
                 getTreeEngine().save(
                         FxTreeNodeEdit.createNew("child_" + i)
                         .setParentNodeId(parentId)
                         .setPosition(i)
                 );
             }
             final FxTreeNode tree = getTreeEngine().getTree(FxTreeMode.Edit, parentId, 1);
             assertEquals(tree.getDirectChildCount(), numChildren);
             assertEquals(tree.getChildren().size(), numChildren);
             for (int i = 0; i < numChildren; i++) {
                 assertEquals(tree.getChildren().get(i).getName(), "child_" + i);
             }
 
             // check with FxSQL
             FxContext.get().setNodeId(parentId);    // see FX-718
             final FxResultSet result = EJBLookup.getSearchEngine().search("SELECT @pk, @node_position WHERE IS CHILD OF '/FX746' ORDER BY @node_position");
             assertEquals(result.getRowCount(), numChildren);
             int lastPosition = 0;
             final String positions = result.collectColumn(2).toString();
             for (FxResultRow row : result.getResultRows()) {
                 assertTrue(row.getInt(2) > lastPosition, "Order by @node_position not ascending: " + positions);
                 lastPosition = row.getInt(2);
             }
         } finally {
             getTreeEngine().remove(FxTreeMode.Edit, parentId, FxTreeRemoveOp.Remove, true);
         }
     }
 
     @Test
     public void testSelectAllTypesWithQueryBuilder() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().type("ROOT", true).getResult();
         assertTrue(result.getRowCount() > 0);
     }
 
    @Test
    public void testTruncatedResult() throws FxApplicationException {
        final FxResultSet result = new SqlQueryBuilder().maxRows(1).getResult();
        assertTrue(result.isTruncated(), "Result must be truncated");
        assertEquals(result.getRowCount(), 1, "Truncated result returned incorrect rowcount");
        assertEquals(result.getTotalRowCount(), 1, "Wrong totalRowCount for truncated result");
    }

     private void queryForCaption(String name) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption").condition("caption", PropertyValueComparator.EQ, name).getResult();
         assertTrue(result.getRowCount() == 1, "Expected one result row, got: " + result.getRowCount());
         assertTrue(name.equals(result.getResultRow(0).getString(1)), "Expected " + name + ", got: " + result.getResultRow(0).getString(1));
     }
 
     private void checkResultTranslation(FxString resultValue, FxString reference, long language) {
         Assert.assertEquals(reference.getTranslation(language), resultValue.getBestTranslation(),
                 "bestTranslation of result value not equal to user translation, expected: "
                         + reference.getTranslation(language) + ", got: " + resultValue.getBestTranslation());
         // The following is known to fail, because the SQL result does not know
         // whether a specific translation or the default language was used in a column (FX-265)
         /*final String translationEn = resultValue.getTranslation(language);
         Assert.assertTrue(StringUtils.isNotBlank(translationEn), "Ticket language translation not returned");*/
     }
 
     private void assertExactPkMatch(FxPK pk, List<FxPK> pks) {
         assertTrue(pks.size() == 1, "No rows returned for exact match");
         assertTrue(pks.get(0).equals(pk), "Exact match did not return expected column - expected " + pk + ", got: " + pks.get(0));
     }
 
     private List<FxPK> getPksForVersion(VersionFilter versionFilter) throws FxApplicationException {
         return new SqlQueryBuilder().select("@pk").type(TEST_TYPE).filterVersion(versionFilter).getResult().collectColumn(1);
     }
 
 
     @DataProvider(name = "testProperties")
     public Object[][] getTestProperties() {
         final Object[][] result = new Object[TEST_PROPS.size()][];
         int ctr = 0;
         for (Map.Entry<String, FxDataType> entry : TEST_PROPS.entrySet()) {
             result[ctr++] = new Object[]{entry.getKey(), entry.getValue()};
         }
         return result;
     }
 
     private String getTestPropertyName(String baseName) {
         return baseName + TEST_SUFFIX;
     }
 
     private FxPropertyAssignment getTestPropertyAssignment(String baseName) {
         return (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(TEST_TYPE + "/" + getTestPropertyName(baseName));
     }
 
     private void assertAscendingOrder(FxResultSet result, int column) {
         assertOrder(result, column, true);
     }
 
     private void assertDescendingOrder(FxResultSet result, int column) {
         assertOrder(result, column, false);
     }
 
     private void assertOrder(FxResultSet result, int column, boolean ascending) {
         FxValue oldValue = null;
         for (FxResultRow row : result.getResultRows()) {
             // check order
             assertTrue(oldValue == null || (ascending
                     ? row.getFxValue(column).compareTo(oldValue) >= 0
                     : row.getFxValue(column).compareTo(oldValue) <= 0),
                     row.getFxValue(column) + " is not "
                             + (ascending ? "greater" : "less") + " than " + oldValue);
             oldValue = row.getFxValue(column);
         }
     }
 
     private long getTestTypeId() {
         return CacheAdmin.getEnvironment().getType(TEST_TYPE).getId();
     }
 
     private int getTestInstanceCount() throws FxApplicationException {
         final int instances = new SqlQueryBuilder().select("@pk").type(TEST_TYPE).getResult().collectColumn(1).size();
         assertTrue(instances > 0, "No instances for test type " + TEST_TYPE + " found.");
         return instances;
     }
 }
