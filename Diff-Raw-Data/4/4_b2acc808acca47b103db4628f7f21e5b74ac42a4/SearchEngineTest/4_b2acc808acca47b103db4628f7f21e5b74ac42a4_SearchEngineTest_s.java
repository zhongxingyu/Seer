 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
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
 import com.flexive.shared.FxContext;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.search.*;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.search.query.QueryOperatorNode;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.value.*;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import static org.testng.Assert.assertEquals;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.testng.annotations.DataProvider;
 import org.testng.Assert;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.collections.CollectionUtils;
 
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
 
     private int testInstanceCount;  // number of instances for the SearchTest type
     private final List<Long> generatedNodeIds = new ArrayList<Long>();
 
     @BeforeClass
     public void setup() throws Exception {
         login(TestUsers.REGULAR);
         final List<FxPK> testPks = new SqlQueryBuilder().select("@pk").type(TEST_TYPE).getResult().collectColumn(1);
         testInstanceCount = testPks.size();
         assert testInstanceCount > 0 : "No instances of test type " + TEST_TYPE + " found.";
         // link test instances in tree
         for (FxPK pk: testPks) {
             generatedNodeIds.add(
                     EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("test" + pk)
                             .setReference(pk).setName(RandomStringUtils.random(new Random().nextInt(1024), true, true)))
             );
         }
     }
 
     @AfterClass
     public void shutdown() throws Exception {
         for (long nodeId: generatedNodeIds) {
             EJBLookup.getTreeEngine().remove(
                     FxTreeNodeEdit.createNew("").setId(nodeId).setMode(FxTreeMode.Edit),
                     false, true);
         }
         logout();
     }
 
     @Test
     public void simpleSelectTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption", "comment").enterSub(QueryOperatorNode.Operator.AND)
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment%")
                 .closeSub().getResult();
         assert result.getRowCount() == 25 : "Expected to fetch 25 rows, got: " + result.getRowCount();
         assert result.getColumnIndex("co.caption") == 1 : "Unexpected column index for co.caption: " + result.getColumnIndex("co.caption");
         assert result.getColumnIndex("co.comment") == 2 : "Unexpected column index for co.comment: " + result.getColumnIndex("co.comment");
         for (int i = 1; i <= result.getRowCount(); i++) {
             assert result.getString(i, 1).startsWith("test caption") : "Unexpected column value: " + result.getString(i, 1);
             assert result.getString(i, 2).startsWith("folder comment") : "Unexpected column value: " + result.getString(i, 2);
         }
     }
 
     @Test
     public void simpleNestedQueryTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("caption").andSub()
                 .condition("caption", PropertyValueComparator.LIKE, "test caption%")
                 .orSub().condition("comment", PropertyValueComparator.LIKE, "folder comment 1")
                 .condition("comment", PropertyValueComparator.LIKE, "folder comment 2").closeSub().getResult();
         assert result.getRowCount() == 2 : "Expected to fetch 2 rows, got: " + result.getRowCount();
         for (int i = 1; i <= 2; i++) {
             assert result.getString(i, 1).matches("test caption [12]") : "Unexpected column value: " + result.getString(i, 1);
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
         assert result.getRowCount() > 0;
         final FxType folderType = CacheAdmin.getEnvironment().getType("FOLDER");
         for (FxResultRow row : result.getResultRows()) {
             assert folderType.getId() == ((FxLargeNumber) row.getValue(1)).getBestTranslation()
                     : "Unexpected result type: " + row.getValue(1) + ", expected: " + folderType.getId();
         }
     }
 
     @Test
     public void briefcaseQueryTest() throws FxApplicationException {
         // create briefcase
         final String selectFolders = new SqlQueryBuilder().filterType("FOLDER").getQuery();
         final FxSQLSearchParams params = new FxSQLSearchParams().saveResultInBriefcase("test briefcase", "description", (Long) null);
         final FxResultSet result = EJBLookup.getSearchEngine().search(selectFolders, 0, Integer.MAX_VALUE, params);
         long bcId = result.getCreatedBriefcaseId();
         try {
             assert result.getRowCount() > 0;
             assert result.getCreatedBriefcaseId() != -1 : "Briefcase should have been created, but no ID returned.";
 
             // select briefcase
             final FxResultSet briefcase = new SqlQueryBuilder().filterBriefcase(result.getCreatedBriefcaseId()).getResult();
             assert briefcase.getRowCount() > 0 : "Empty briefcase returned, but getResult returned " + result.getRowCount() + " rows.";
         } finally {
             EJBLookup.getBriefcaseEngine().remove(bcId);
         }
     }
 
     @Test
     public void typeConditionTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("typedef").type(FxType.CONTACTDATA).getResult();
         final FxType cdType = CacheAdmin.getEnvironment().getType(FxType.CONTACTDATA);
         assert result.getRowCount() > 0;
         for (FxResultRow row : result.getResultRows()) {
             assert ((FxLargeNumber) row.getFxValue(1)).getDefaultTranslation() == cdType.getId()
                     : "Unexpected type in result, expected " + cdType.getId() + ", was: " + row.getFxValue(1);
         }
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
         // also select virtual properties to make sure they don't mess up the result
         final FxResultSet result = new SqlQueryBuilder().select(
                 getTestPropertyName(name)).type(TEST_TYPE).getResult();
         assert result.getRowCount() == testInstanceCount : "Expected all test instances to be returned, got "
                 + result.getRowCount() + " instead of " + testInstanceCount;
         final int idx = 1;
         for (FxResultRow row : result.getResultRows()) {
             assert dataType.getValueClass().isAssignableFrom(row.getFxValue(idx).getClass())
                     : "Invalid class returned for datatype " + dataType + ": " + row.getFxValue(idx).getClass() + " instead of " + dataType.getValueClass();
             assert row.getFxValue(idx).getXPathName() != null : "XPath was null";
             assert row.getFxValue(idx).getXPathName().equalsIgnoreCase(getTestPropertyName(name)) : "Invalid property name: " + row.getFxValue(idx).getXPathName() + ", expected: " + getTestPropertyName(name);
         }
     }
 
     @Test
     public void selectVirtualPropertiesTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@path", "@node_position", "@permissions",
                 getTestPropertyName("string")).type(TEST_TYPE).getResult();
         final int idx = 5;
         for (FxResultRow row : result.getResultRows()) {
             assert getTestPropertyName("string").equalsIgnoreCase(row.getFxValue(idx).getXPathName())
                     : "Invalid property name from XPath: " + row.getFxValue(idx).getXPathName()
                     + ", expected: " + getTestPropertyName("string");
         }
     }
 
     @Test(dataProvider = "testProperties")
     public void orderByTest(String name, FxDataType dataType) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName(name)).type(TEST_TYPE)
                 .orderBy(2, SortDirection.ASCENDING).getResult();
         assert result.getRowCount() > 0;
         assertAscendingOrder(result, 2);
     }
 
     @Test
     public void selectPermissionsTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(TEST_TYPE).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row : result.getResultRows()) {
             final FxPK pk = row.getPk(1);
             final PermissionSet permissions = row.getPermissions(2);
             assert permissions.isMayRead();
             final PermissionSet contentPerms = EJBLookup.getContentEngine().load(pk).getPermissions();
             assert contentPerms.equals(permissions) : "Permissions from search: " + permissions + ", content: " + contentPerms;
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
 
     private void setResultPreferences(SortDirection sortDirection) throws FxApplicationException {
        final ResultPreferencesEdit prefs = new ResultPreferencesEdit(new ArrayList<ResultColumnInfo>(0), Arrays.asList(
                 new ResultOrderByInfo(Table.CONTENT, getTestPropertyName("string"), "", sortDirection)),
                 25, 100);
         EJBLookup.getResultPreferencesEngine().save(prefs, CacheAdmin.getEnvironment().getType(TEST_TYPE).getId(), ResultViewType.LIST, AdminResultLocations.DEFAULT);
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
         final List<FxPK> folderPks = new SqlQueryBuilder().select("@pk").type("folder").getResult().collectColumn(1);
 
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
                     assert false : "Failed to submit for property " + dataType + " with comparator " + comparator
                             + ":\n" + e.getMessage() + ", thrown at:\n"
                             + StringUtils.join(e.getStackTrace(), '\n');
                 }
             }
         }
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
         final String propertyName = getTestPropertyName(name);
 
         for (PropertyValueComparator comparator : PropertyValueComparator.getAvailable(dataType)) {
             if (!(comparator.equals(PropertyValueComparator.EQ) || comparator.equals(PropertyValueComparator.GE)
                || comparator.equals(PropertyValueComparator.GT) || comparator.equals(PropertyValueComparator.LE)
                || comparator.equals(PropertyValueComparator.LT))) {
                 continue;
             }
             final FxValue value = getTestValue(name, comparator);
             final SqlQueryBuilder builder = new SqlQueryBuilder().select("@pk", propertyName).condition(propertyName, comparator, value);
             final FxResultSet result = builder.getResult();
             assert result.getRowCount() > 0 : "Cannot test on empty result sets, query=\n" + builder.getQuery();
             for (FxResultRow row: result.getResultRows()) {
                 final FxValue rowValue = row.getFxValue(2);
                 switch(comparator) {
                     case EQ:
                         assert rowValue.compareTo(value) == 0
                                 : "Result value " + rowValue + " is not equal to " + value + " (compareTo = "
                                 + rowValue.compareTo(value) + ")";
                         assert rowValue.getBestTranslation().equals(value.getBestTranslation())
                                 : "Result value " + rowValue + " is not equal to " + value;
                         break;
                     case LT:
                         assert rowValue.compareTo(value) < 0 : "Result value " + rowValue + " is not less than " + value;
                         break;
                     case LE:
                         assert rowValue.compareTo(value) <= 0 : "Result value " + rowValue + " is not less or equal to " + value;
                         break;
                     case GT:
                         assert rowValue.compareTo(value) > 0 : "Result value " + rowValue + " is not greater than " + value;
                         break;
                     case GE:
                         assert rowValue.compareTo(value) >= 0 : "Result value " + rowValue + " is not greater or equal to " + value;
                         break;
                     default:
                         assert false : "Invalid comparator: " + comparator;
                 }
             }
         }
     }
 
     /**
      * Finds a FxValue of the test instances that matches some of the result rows
      * for the given comparator, but not all or none.
      *
      * @param name  the test property name
      * @param comparator    the comparator
      * @return  a value that matches some rows
      * @throws FxApplicationException   on search engine errors
      */
     private FxValue getTestValue(String name, PropertyValueComparator comparator) throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select(getTestPropertyName(name)).type(TEST_TYPE).getResult();
         final List<FxValue> values = result.collectColumn(1);
         assert values.size() == testInstanceCount : "Expected " + testInstanceCount + " rows, got: " + values.size();
         for (FxValue value: values) {
             if (value == null || value.isEmpty()) {
                 continue;
             }
             int match = 0;  // number of matched values for the given comparator
             int count = 0;  // number of values checked so far
             for (FxValue value2: values) {
                 if (value2 == null || value2.isEmpty()) {
                     continue;
                 }
                 count++;
                 switch(comparator) {
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
                         assert false : "Cannot check relative ordering for comparator " + comparator;
                 }
                 if (match > 0 && count > match) {
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
         }
         throw new IllegalArgumentException("Failed to find a suitable test value for property " + getTestPropertyName(name)
                 + " and comparator " + comparator);
     }
 
     @Test
     public void aclSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "acl", "acl.label", "acl.name",
                 "acl.mandator", "acl.description", "acl.cat_type", "acl.color", "acl.created_by", "acl.created_at",
                 "acl.modified_by", "acl.modified_at").type(TEST_TYPE).getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final ACL acl = CacheAdmin.getEnvironment().getACL(row.getLong("acl"));
             final FxContent content = EJBLookup.getContentEngine().load(row.getPk(1));
             assert content.getAclId() == acl.getId()
                     : "Invalid ACL for instance " + row.getPk(1) + ": " + acl.getId()
                     + ", content engine returned " + content.getAclId();
 
             // check label
             assert acl.getLabel().getBestTranslation().equals(row.getFxValue("acl.label").getBestTranslation())
                     : "Invalid ACL label '" + row.getValue(3) + "', expected: '" + acl.getLabel() + "'";
 
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
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final Step step = CacheAdmin.getEnvironment().getStep(row.getLong("step"));
             final StepDefinition definition = CacheAdmin.getEnvironment().getStepDefinition(step.getStepDefinitionId());
             assert definition.getLabel().getBestTranslation().equals(row.getFxValue("step.label").getBestTranslation())
                     : "Invalid step label '" + row.getValue(3) + "', expected: '" + definition.getLabel() + "'";
             try {
                 final FxContent content = EJBLookup.getContentEngine().load(row.getPk(1));
                 assert content.getStepId() == step.getId()
                         : "Invalid step for instance " + row.getPk(1) + ": " + step.getId()
                         + ", content engine returned " + content.getStepId();
             } catch (FxNoAccessException e) {
                 assert false : "Content engine denied read access to instance " + row.getPk(1) + " that was returned by search."; 
             }
 
             // check fields selected from the ACL table
             assertEquals(row.getLong("step.id"), step.getId(), "Invalid value for field: id");
             assertEquals(row.getLong("step.stepdef"), step.getStepDefinitionId(), "Invalid value for field: stepdef");
             assertEquals(row.getLong("step.workflow"), step.getWorkflowId(), "Invalid value for field: workflow");
             assertEquals(row.getLong("step.acl"), step.getAclId(), "Invalid value for field: acl");
         }
     }
 
     @Test
     public void contactDataSelectTest() throws FxApplicationException {
         // contact data is an example of a content with extended private permissions and no permissions for other users
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "@permissions").type(FxType.CONTACTDATA).getResult();
         for (FxResultRow row: result.getResultRows()) {
             try {
                 final FxContent content = EJBLookup.getContentEngine().load(row.getPk(1));
                 assert content.getPermissions().equals(row.getPermissions("@permissions"))
                         : "Content perm: " + content.getPermissions() + ", search perm: " + row.getPermissions(2);
             } catch (FxNoAccessException e) {
                 assert false : "Search returned contact data #" + row.getPk(1)
                         + ", but content engine disallows access: " + e.getMessage();
             }
         }
     }
 
     /**
      * Executes a query without conditions and checks if it returns only instances
      * the user can actually read (a select without conditions is an optimized case of the
      * search that must implement the same security constraints as a regular query).
      *
      * @throws FxApplicationException   on errors
      */
     @Test
     public void selectAllPermissionsTest() throws FxApplicationException {
         final FxResultSet result = EJBLookup.getSearchEngine().search("SELECT co.@pk FROM content co", 0, 999999, null);
         for (FxResultRow row: result.getResultRows()) {
             try {
                 EJBLookup.getContentEngine().load(row.getPk(1));
             } catch (FxNoAccessException e) {
                 assert false : "Content engine denied read access to instance " + row.getPk(1) + " that was returned by search."; 
             }
         }
     }
 
     @Test
     public void mandatorSelectorTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", "mandator", "mandator.id",
                 "mandator.metadata", "mandator.is_active", "mandator.created_by", "mandator.created_at",
                 "mandator.modified_by", "mandator.modified_at").getResult();
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final Mandator mandator = CacheAdmin.getEnvironment().getMandator(row.getLong("mandator"));
             final FxContent content = EJBLookup.getContentEngine().load(row.getPk(1));
 
             assertEquals(mandator.getId(), content.getMandatorId(), "Search returned different mandator than content engine");
             assertEquals(row.getLong("mandator.id"), mandator.getId(), "Invalid value for field: id");
             if( row.getValue("mandator.metadata") != null ) {
                 long mand = row.getLong("mandator.metadata");
                 if (!(mand == 0 || mand == -1))
                     Assert.fail("Invalid mandator: " + mand + "! Expected 0 or -1 (System default or test division)");
             }
             assertEquals(row.getLong("mandator.is_active"), mandator.isActive() ? 1 : 0, "Invalid value for field: is_active");
             checkLifecycleInfo(row, "mandator", mandator.getLifeCycleInfo());
         }
     }
 
     @Test
     public void accountSelectorTest() throws FxApplicationException {
         for (String name : new String[] { "created_by", "modified_by" }) {
             final FxResultSet result = new SqlQueryBuilder().select("@pk", name, name + ".mandator",
                     name + ".username", name + ".password", name + ".email", name + ".contact_id",
                     name + ".valid_from", name + ".valid_to", name + ".description", name + ".created_by",
                     name + ".created_at", name + ".modified_by", name + ".modified_at",
                     name + ".is_active", name + ".is_validated", name + ".lang", name + ".login_name",
                     name + ".allow_multilogin", name + ".default_node").maxRows(10).getResult();
             assert result.getRowCount() == 10 : "Expected 10 result rows";
             for (FxResultRow row: result.getResultRows()) {
                 final Account account = EJBLookup.getAccountEngine().load(row.getLong(name));
                 assertEquals(row.getString(name + ".username"), account.getName(), "Invalid value for field: username");
                 assertEquals(row.getString(name + ".login_name"), account.getLoginName(), "Invalid value for field: login_name");
                 assertEquals(row.getString(name + ".email"), account.getEmail(), "Invalid value for field: email");
                 assertEquals(row.getLong(name + ".contact_id"), account.getContactDataId(), "Invalid value for field: contact_id");
                 assertEquals(row.getLong(name + ".is_active"), account.isActive() ? 1 : 0, "Invalid value for field: is_active");
                 assertEquals(row.getLong(name + ".is_validated"), account.isValidated() ? 1 : 0, "Invalid value for field: is_validated");
                 assertEquals(row.getLong(name + ".allow_multilogin"), account.isAllowMultiLogin() ? 1 : 0, "Invalid value for field: allow_multilogin");
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
         assert result.getRowCount() > 0;
         for (FxResultRow row: result.getResultRows()) {
             final List<FxPaths.Path> paths = row.getPaths(2);
             assert paths.size() > 0 : "Returned no path information for content " + row.getPk(1);
             for (FxPaths.Path path: paths) {
                 assert path.getItems().size() > 0 : "Empty path returned";
                 final FxPaths.Item leaf = path.getItems().get(path.getItems().size() - 1);
                 assert leaf.getReferenceId() == row.getPk(1).getId() : "Expected reference ID " + row.getPk(1)
                         + ", got: " + leaf.getReferenceId() + " (nodeId=" + leaf.getNodeId() + ")";
 
                 final String treePath = StringUtils.join(EJBLookup.getTreeEngine().getLabels(FxTreeMode.Edit, leaf.getNodeId()), '/');
                 assert treePath.equals(path.getCaption()) : "Unexpected tree path '" + path.getCaption()
                         + "', expected: '" + treePath + "'";
 
             }
         }
         // test selection via node path
         final FxResultSet pathResult = new SqlQueryBuilder().select("@pk", "@path").isChild("/").getResult();
         assert pathResult.getRowCount() == result.getRowCount() : "Path select returned " + pathResult.getRowCount()
                 + " rows, select by ID returned " + result.getRowCount() + " rows.";
         // query a leaf node
         new SqlQueryBuilder().select("@pk").isChild(EJBLookup.getTreeEngine().getPathById(FxTreeMode.Edit, pathResult.getResultRow(0).getPk(1).getId()));
     }
 
     @Test
     public void fulltextSearchTest() throws FxApplicationException {
         final FxResultSet result = new SqlQueryBuilder().select("@pk", getTestPropertyName("string")).type(TEST_TYPE).maxRows(1).getResult();
         assert result.getRowCount() == 1 : "Expected only one result, got: " + result.getRowCount();
 
         // perform a fulltext query against the first word
         final FxPK pk = result.getResultRow(0).getPk(1);
         final String[] words = StringUtils.split(((FxString) result.getResultRow(0).getFxValue(2)).getBestTranslation(), ' ');
         assert words.length > 0;
         assert words[0].length() > 0 : "Null length word: " + words[0];
         final FxResultSet ftresult = new SqlQueryBuilder().select("@pk").fulltext(words[0]).getResult();
         assert ftresult.getRowCount() > 0 : "Expected at least one result for fulltext query '" + words[0] + "'";
         assert ftresult.collectColumn(1).contains(pk) : "Didn't find pk " + pk + " in result, got: " + ftresult.collectColumn(1);
     }
 
     @Test
     public void versionFilterTest() throws FxApplicationException {
         final List<FxPK> allVersions = getPksForVersion(VersionFilter.ALL);
         final List<FxPK> liveVersions = getPksForVersion(VersionFilter.LIVE);
         final List<FxPK> maxVersions = getPksForVersion(VersionFilter.MAX);
         assert allVersions.size() > 0 : "All versions result must not be empty";
         assert liveVersions.size() > 0 : "Live versions result must not be empty";
         assert maxVersions.size() > 0 : "Max versions result must not be empty";
         assert allVersions.size() > liveVersions.size() : "Expected more than only live versions";
         assert allVersions.size() > maxVersions.size() : "Expected more than only max versions";
         assert !CollectionUtils.isEqualCollection(liveVersions, maxVersions) : "Expected different results for max and live version filter";
         for (FxPK pk: liveVersions) {
             final FxContent content = EJBLookup.getContentEngine().load(pk);
             assert content.isLiveVersion() : "Expected live version for " + pk;
         }
         for (FxPK pk: maxVersions) {
             final FxContent content = EJBLookup.getContentEngine().load(pk);
             assert content.isMaxVersion() : "Expected max version for " + pk;
             assert content.getVersion() == 1 || !content.isLiveVersion();
         }
     }
 
     @Test
     public void lastContentChangeTest() throws FxApplicationException {
         final long lastContentChange = EJBLookup.getSearchEngine().getLastContentChange(false);
         assert lastContentChange > 0;
         final FxContent content = EJBLookup.getContentEngine().initialize(TEST_TYPE);
         content.setAclId(TestUsers.getInstanceAcl().getId());
         content.setValue("/" + getTestPropertyName("string"), new FxString(false, "lastContentChangeTest"));
         FxPK pk = null;
         try {
             assert EJBLookup.getSearchEngine().getLastContentChange(false) == lastContentChange
                     : "Didn't touch contents, but lastContentChange timestamp was increased";
             pk = EJBLookup.getContentEngine().save(content);
             assert EJBLookup.getSearchEngine().getLastContentChange(false) > lastContentChange
                     : "Saved content, but lastContentChange timestamp was not increased: "
                     + EJBLookup.getSearchEngine().getLastContentChange(false);
         } finally {
             if (pk != null) {
                 EJBLookup.getContentEngine().remove(pk);
             }
         }
     }
 
     @Test
     public void lastContentChangeTreeTest() throws FxApplicationException {
         final long lastContentChange = EJBLookup.getSearchEngine().getLastContentChange(false);
         assert lastContentChange > 0;
         final long nodeId = EJBLookup.getTreeEngine().save(FxTreeNodeEdit.createNew("lastContentChangeTreeTest"));
         try {
             final long editContentChange = EJBLookup.getSearchEngine().getLastContentChange(false);
             assert editContentChange > lastContentChange
                     : "Saved content, but lastContentChange timestamp was not increased: " + editContentChange;
             EJBLookup.getTreeEngine().activate(FxTreeMode.Edit, nodeId, false);
             assert EJBLookup.getSearchEngine().getLastContentChange(true) > editContentChange
                     : "Activated content, but live mode lastContentChange timestamp was not increased: "
                         + EJBLookup.getSearchEngine().getLastContentChange(true);
             assert EJBLookup.getSearchEngine().getLastContentChange(false) == editContentChange
                     : "Edit tree didn't change, but lastContentChange timestamp was updated";
         } finally {
             FxContext.get().runAsSystem();
             try {
                 try {
                     EJBLookup.getTreeEngine().remove(EJBLookup.getTreeEngine().getNode(FxTreeMode.Edit, nodeId), true, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
                 try {
                     EJBLookup.getTreeEngine().remove(EJBLookup.getTreeEngine().getNode(FxTreeMode.Live, nodeId), true, false);
                 } catch (FxApplicationException e) {
                     // pass
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
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
             assert oldValue == null || (ascending
                     ? row.getFxValue(column).compareTo(oldValue) >= 0
                     : row.getFxValue(column).compareTo(oldValue) <= 0)
                     : row.getFxValue(column) + " is not "
                     + (ascending ? "greater" : "less") + " than " + oldValue;
             oldValue = row.getFxValue(column);
         }
     }
 
 }
