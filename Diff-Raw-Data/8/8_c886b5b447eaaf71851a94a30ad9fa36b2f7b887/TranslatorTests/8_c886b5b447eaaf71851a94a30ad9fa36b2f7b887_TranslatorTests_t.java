 /**
  * Copyright (c) 2011. Physion Consulting LLC
  * All rights reserved.
  */
 package com.physion.ebuilder.translator.test;
 
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 import org.approvaltests.Approvals;
 import org.approvaltests.UseReporter;
 import org.approvaltests.reporters.JunitReporter;
 
 import com.physion.ebuilder.datamodel.DataModel;
 import com.physion.ebuilder.datamodel.RowData;
 import com.physion.ebuilder.datatypes.Attribute;
 import com.physion.ebuilder.datatypes.ClassDescription;
 import com.physion.ebuilder.datatypes.CollectionOperator;
 import com.physion.ebuilder.datatypes.Operator;
 import com.physion.ebuilder.datatypes.Type;
 import com.physion.ebuilder.expression.ExpressionTree;
 import com.physion.ebuilder.translator.ExpressionTreeToRowData;
 import com.physion.ebuilder.translator.RowDataToExpressionTree;
 
 
 /**
  * Tests for the Translator subclasses and for serialization.
  *
  * Each method, (e.g. test1(), test2(), test3()...), tests
  * a particular functionality.  I used numbers as opposed to
  * coming up with uniqe "user friendly" names for each method.
  *
  * Each test method that currently exists, (November 6, 2011),
  * is generally of the form:
  *
  *      <Code that creates a RowData object called "rootRow">
  *      <Call String s = getResultsString(rootRow)>
  *      <Call Approvals.approve(s)>
  *
  * That's it.  The Approvals toolkit compares the result to
  * a human readable text file that contains the "approved"
  * version of the result string.  The text file is named
  * according to the test method.  For example:
  *
  *      TranslatorTests.test123.approved.txt
  *
  * More information about how this works can be found at:
  *
  *      http://approvaltests.sourceforge.net/
  *
  * I would assume that at some point in the future, Physion
  * will add tests that show a bug has been fixed.  For example,
  * if an engineer fixed issue #123 in Physion's bug tracking system,
  * I would expect a method called test123() to be created that
  * demonstrates that the bug is fixed.
  */
 public class TranslatorTests
         extends TestCase {
 
     private static ClassDescription epochCD =
             DataModel.getClassDescription("Epoch");
     private static ClassDescription epochGroupCD =
             DataModel.getClassDescription("EpochGroup");
     private static ClassDescription sourceCD =
             DataModel.getClassDescription("Source");
     private static ClassDescription responseCD =
             DataModel.getClassDescription("Response");
     private static ClassDescription resourceCD =
             DataModel.getClassDescription("Resource");
     private static ClassDescription derivedResponseCD =
             DataModel.getClassDescription("DerivedResponse");
     private static ClassDescription externalDeviceCD =
             DataModel.getClassDescription("ExternalDevice");
     private static ClassDescription noteCD =
             DataModel.getClassDescription("Note");
     private static ClassDescription timelineAnnotationCD =
             DataModel.getClassDescription("TimelineAnnotation");
     private static ClassDescription experimentCD = 
             DataModel.getClassDescription("Experiment");
 
 
     /**
      * This is a test of the translation of a simple RowData
      * object to an ExpressionTree, and the translation back
      * to a RowData.  It also tests serialization of the RowData
      * and ExpressionTree objects.
      *
      * As of November 6, 2011, the only difference between all
      * these test methods is the structure of the RowData
      * object that is created.  Otherwise, each method pretty
      * much does the same thing.
      *
      * Note that the @UseReporter annotation can be set to
      * whichever org.approvaltests.reporters.*Reporter class
      * works on your system or IDE.
      */
     //@UseReporter(QuietReporter.class)
     @UseReporter(JunitReporter.class)
     public void test1()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test the Any collection operator, (which becomes "or"),
          * and the String type and a couple attribute operators.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolID"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("abc");
         rootRow.addChildRow(rowData);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolID"));
         rowData.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData.setAttributeValue("xyz");
         rootRow.addChildRow(rowData);
 
         String s = getResultsString(
                 "CollectionOperator.ANY With Two Operands", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test2()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test the All collection operator, (which becomes "and"),
          * and the Boolean type.
          *
          *      incomplete is true
          *
          * Note, the GUI, (i.e. the RowData object), does not accept:
          *
          *      incomplete == true
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("incomplete"));
         /**
          * This is the wrong way to handle booleans in the RowData object.
          */
         //rowData.setAttributeOperator(Operator.EQUALS);
         //rowData.setAttributeValue(new Boolean(true));
         /**
          * This is the correct way.
          */
         rowData.setAttributeOperator(Operator.IS_TRUE);
 
         rootRow.addChildRow(rowData);
 
         String s = getResultsString(
                 "Attribute Boolean Operator.IS_TRUE", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test3()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test an attribute path with two levels.
          * Also test using None collection operator which
          * gets turned into two operators:  "not" with
          * "or" as its only operand.
          *
          *      Epoch None
          *          epochGroup.label == "Test 27"
          *
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("epochGroup"));
         rowData.addAttribute(epochGroupCD.getAttribute("label"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("Test 27");
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("Attribute Path Nested Twice", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test4()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test an attribute path with three levels:
          *
          *      epochGroup.source.label == "Test 27"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("epochGroup"));
         rowData.addAttribute(epochGroupCD.getAttribute("source"));
         rowData.addAttribute(sourceCD.getAttribute("label"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("Test 27");
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("Attribute Path Nested Thrice", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test5()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a reference value for null.
          *
          *      Epoch | All
          *        Epoch | owner is null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("owner"));
         rowData.addAttribute(Attribute.IS_NULL);  // optional call
         rowData.setAttributeOperator(Operator.IS_NULL);
         rootRow.addChildRow(rowData);
 
         String s = getResultsString(
                 "Reference Value Operator.IS_NULL", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test6()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a reference value for not null.
          *
          *      Epoch | All
          *        Epoch | owner is not null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("owner"));
         rowData.addAttribute(Attribute.IS_NOT_NULL);  // optional call
         rowData.setAttributeOperator(Operator.IS_NOT_NULL);
         rootRow.addChildRow(rowData);
 
         String s = getResultsString(
                 "Reference Value Operator.IS_NOT_NULL", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test7()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a compound row:
          *
          *      Epoch | All
          *        Epoch | responses All have Any
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rowData.setCollectionOperator2(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("Compound Row", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test8()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a compound row with lots of None collection operators:
          *
          *      Epoch | None
          *        Epoch | responses None have None
          *          Response | uuid == "xyz"
          *          Response | samplingRate != 1.23
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rowData.setCollectionOperator2(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
         rowData2.setAttributeOperator(Operator.NOT_EQUALS);
         rowData2.setAttributeValue(new Double(1.23));
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "Compound Row With Lots Of None Collection Operators", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test9()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
         * Test a nested compound row with lots of None collection operators:
          *
          *      Epoch | None
         *        Epoch | nextEpoch.nextEpoch.previousEpoch.responses None have None
          *          Response | uuid == "xyz"
          *          Response | samplingRate != 1.23
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("previousEpoch"));
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rowData.setCollectionOperator2(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("samplingRate"));
         rowData2.setAttributeOperator(Operator.NOT_EQUALS);
         rowData2.setAttributeValue(new Double(1.23));
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "Nested Compound Row With Lots Of None Collection Operators",
                 rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test10()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
 
         /**
          * Test a compound row:
          *
          *      Epoch | All
          *        Epoch | responses All have Any
          *          Response | resources Any have Any
         *            Resource | uuid != "ID 27"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("resources"));
         rowData2.setCollectionOperator(CollectionOperator.ANY);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.addAttribute(resourceCD.getAttribute("uuid"));
         rowData3.setAttributeOperator(Operator.NOT_EQUALS);
         rowData3.setAttributeValue("ID 27");
         rowData2.addChildRow(rowData3);
 
         String s = getResultsString("Compound Row Nested Classes", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test11()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a compound row that uses the Count collection operator:
          *
          *      Epoch | All
          *        Epoch | responses All have Any
          *          Response | resources Count <= 5
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("resources"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeOperator(Operator.LESS_THAN_EQUALS);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("Compound Row ALL COUNT", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test12()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a compound row:
          *
          *      Epoch | Any
          *        Epoch | responses Any have Any
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("Compound Row ANY ANY", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test13()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a compound row:
          *
          *      Epoch | None
          *        Epoch | responses None have Any
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("Compound Row NONE NONE", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test14()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute type.
          *
          *      Epoch | All
          *        Epoch | My keywords None have Any
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mykeywords"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("PER_USER_OR_CUSTOM_REFERENCE_OPERATOR", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test15()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute type with Count.
          *
          *      Epoch | All
          *        Epoch | My keywords Count == 5
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mykeywords"));
         rowData.setCollectionOperator(CollectionOperator.COUNT);
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue(new Integer(5));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString(
                 "PER_USER_OR_CUSTOM_REFERENCE_OPERATOR CollectionOperator.COUNT", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test16()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a nested PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute type.
          *
          *      Epoch | Any
          *        Epoch | nextEpoch.All keywords Any have Any
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rowData.setCollectionOperator2(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Nested Once CollectionOperator.ANY", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test17()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a nested PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute type.
          *
          *      Epoch | None
          *        Epoch | nextEpoch.nextEpoch.All keywords None have All
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rowData.setCollectionOperator2(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Nested Twice CollectionOperator.NONE", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test18()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test a nested PER_USER_OR_CUSTOM_REFERENCE_OPERATOR attribute type.
          *
          *     Epoch | All
          *       Epoch | nextEpoch.nextEpoch.previousEpoch.All keywords All have Any
          *         KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("previousEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rowData.setCollectionOperator2(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Nested Thrice CollectionOperator.ALL", rootRow);
         Approvals.approve(s);
     }
 
 
     /**
      * TODO:  Change the way the time is displayed by getResultsString()
      * so that the "approved" test output is the same regardless of the
      * timezone in which this test is run.  For example, as of January 2011
      * the TranslatorTests.test19.approved.txt file contains the string:
      *
      *      Thu Dec 31 19:00:00 EST 2009
      * 
      * which means the test must be run in the eastern US timezone to match.
      * One solution would be to have getResultsString always display the time
      * as UTC time.
      */
     @UseReporter(JunitReporter.class)
     public void test19()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a PARAMETERS_MAP row of type time.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
         rowData.setPropName("someTimeKey");
         rowData.setPropType(Type.DATE_TIME);
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue(new Date(1262304000000L));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("PARAMETERS_MAP Type Date", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test20()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a PARAMETERS_MAP row of type float, that
          * has an attributePath that is more than one level deep.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("previousEpoch"));
         rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.FLOAT_64);
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue(new Double(12.3));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("PARAMETERS_MAP Nested", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test21()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a PER_USER_PARAMETERS_MAP row.
          *
          *      Any properties.someKey(int) != "34"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("properties"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.INT_32);
         rowData.setAttributeOperator(Operator.NOT_EQUALS);
         rowData.setAttributeValue(new Integer(34));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("PER_USER_PARAMETERS_MAP", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test22()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test a PER_USER_PARAMETERS_MAP row.
          *
          * nextEpoch.nextEpoch.previousEpoch.Any properties.someKey(int) != "34"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("previousEpoch"));
         rowData.addAttribute(epochCD.getAttribute("properties"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.INT_32);
         rowData.setAttributeOperator(Operator.NOT_EQUALS);
         rowData.setAttributeValue(new Integer(34));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("PER_USER_PARAMETERS_MAP Nested", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test23()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         /**
          * Test handling a reference.
          *
          *  nextEpoch.nextEpoch is null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.setAttributeOperator(Operator.IS_NULL);
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("Reference isnull()", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test24()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test compound row.
          *
          *      All of the following
          *        None of the following
          *          protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("protocolID"));
         rowData2.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         String s = getResultsString("Compound Operators Nested", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test25()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
         RowData rowData4;
 
         /**
          * Test compound row.
          *
          *      Any of the following
          *        All of the following
          *          None of the following
          *            protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         String s = getResultsString(
                 "Compound Operators In Different Positions", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test26()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
         RowData rowData4;
 
         /**
          * Test compound row.
          *
          *      All of the following
          *        All of the following
          *          Any of the following
          *            None of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ANY);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         String s = getResultsString(
                 "Compound Operators In Different Positions", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test27()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
         RowData rowData4;
 
         /**
          * Test compound row.
          *
          *      Any of the following
          *        Any of the following
          *          All of the following
          *            None of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         String s = getResultsString(
                 "Compound Operators In Different Positions", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test28()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
         RowData rowData4;
 
         /**
          * Test compound row.
          *
          *      None of the following
          *        None of the following
          *          All of the following
          *            Any of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.ANY);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         String s = getResultsString(
                 "Compound Operators In Different Positions", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test29()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test compound row with PARAMETERS_MAP, PER_USER_OR_CUSTOM_REFERENCE_OPERATOR children.
          *
          *  Modified rootRow:
          *  Epoch | Any
          *    Epoch | nextEpoch.My DerivedResponses None
          *      DerivedResponse | derivationParameters.somekey(boolean) is true
          *      DerivedResponse | My Keywords Count == "5"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute(
                 "derivationParameters"));
         rowData2.setPropName("someKey");
         rowData2.setPropType(Type.BOOLEAN);
         rowData2.setAttributeOperator(Operator.IS_TRUE);
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "Nested PER_USER_OR_CUSTOM_REFERENCE_OPERATOR With PARAMETERS_MAP & PER_USER_OR_CUSTOM_REFERENCE_OPERATOR Children", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test30()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
 
         /**
          * Test compound row with PARAMETERS_MAP, PER_USER_OR_CUSTOM_REFERENCE_OPERATOR, and
          * PER_USER_PARAMETERS_MAP child.
          *
          *  Epoch | Any
          *    Epoch | nextEpoch.My DerivedResponses None
          *      DerivedResponse | derivationParameters.someKey(boolean) is true
          *      DerivedResponse | My Keywords Count == "5"
          *      DerivedResponse | externalDevice.My Property.someKey2(float) ==
          *                                                               "34.5"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute(
                 "derivationParameters"));
         rowData2.setPropName("someKey");
         rowData2.setPropType(Type.BOOLEAN);
         rowData2.setAttributeOperator(Operator.IS_TRUE);
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("externalDevice"));
         rowData2.addAttribute(externalDeviceCD.getAttribute("myproperties"));
         rowData2.setPropName("someKey2");
         rowData2.setPropType(Type.FLOAT_64);
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue(new Double(34.5));
         rowData.addChildRow(rowData2);
 
         String s = getResultsString(
                 "Nested PER_USER_OR_CUSTOM_REFERENCE_OPERATOR With PM, PU, and PUPM Children", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void test31()
             throws Exception {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
 
         /**
          * Test compound row where class changes between parent and child.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.NONE);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.addAttribute(responseCD.getAttribute("samplingRate"));
         rowData3.setAttributeOperator(Operator.GREATER_THAN_EQUALS);
         rowData3.setAttributeValue(new Double(55.2));
         rowData2.addChildRow(rowData3);
 
         String s = getResultsString(
                 "Compound Row, Class Change Between Parent And Child", rootRow);
         Approvals.approve(s);
     }
 
 
     @UseReporter(JunitReporter.class)
     public void testNoteAnnotation() throws Exception
     {
         /**
          * Test Note annotations operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("notes"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(noteCD.getAttribute("text"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("foo");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("Note annotation on Epoch", rootRow);
         Approvals.approve(s);
 
     }
 
     @UseReporter(JunitReporter.class)
     public void testMyNoteAnnotation() throws Exception
     {
         /**
          * Test Note annotations operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mynotes"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(noteCD.getAttribute("text"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("foo");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("Note annotation on Epoch", rootRow);
         Approvals.approve(s);
 
     }
 
     @UseReporter(JunitReporter.class)
     public void testMyTimelineAnnotation() throws Exception
     {
         /**
          * Test Note annotations operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mytimelineannotations"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(timelineAnnotationCD.getAttribute("startTimeZone"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("America/Chicago");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("Note annotation on Epoch", rootRow);
         Approvals.approve(s);
 
     }
 
     @UseReporter(JunitReporter.class)
     public void testTimelineAnnotation() throws Exception
     {
         /**
          * Test Note annotations operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("timelineannotations"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(timelineAnnotationCD.getAttribute("startTimeZone"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("America/Chicago");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("Note annotation on Epoch", rootRow);
         Approvals.approve(s);
 
     }
 
     @UseReporter(JunitReporter.class)
     public void testSourceContainingExperiments() throws Exception
     {
         /**
          * Test Source.CONTAINING_EXPERIMENTS operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(sourceCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(sourceCD.getAttribute("containing_experiments"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(experimentCD.getAttribute("notes"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("foo");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("containing_experiments on Source", rootRow);
         Approvals.approve(s);
     }
 
     @UseReporter(JunitReporter.class)
     public void testEpochGroupContainingExperiments() throws Exception
     {
         /**
          * Test EpochGroup.EG_CONTAINING_EXPERIMENTS operator
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochGroupCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochGroupCD.getAttribute("eg_containing_experiments"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         RowData rowData1 = new RowData();
         rowData1.addAttribute(experimentCD.getAttribute("notes"));
         rowData1.setAttributeOperator(Operator.EQUALS);
         rowData1.setAttributeValue("foo");
         rowData.addChildRow(rowData1);
 
         String s = getResultsString("eg_containing_experiments on EpochGroup", rootRow);
         Approvals.approve(s);
     }
 
     /**
      * This test was added by Steve Ford on Jan 9, 2012.
      */
     @UseReporter(JunitReporter.class)
     public void testPathToContainingExperiments() throws Exception
     {
         /**
          * EpochGroup | Any
          *   EpochGroup | source.parent.containing experiments Count == "4"
          */
 
         RowData rootRow = new RowData();
         rootRow.setClassUnderQualification(epochGroupCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         RowData rowData = new RowData();
         rowData.addAttribute(epochGroupCD.getAttribute("source"));
         rowData.addAttribute(epochGroupCD.getAttribute("parent"));
         rowData.addAttribute(sourceCD.getAttribute("containing_experiments"));
         rowData.setCollectionOperator(CollectionOperator.COUNT);
         rowData.setAttributeValue(new Integer(4));
         rootRow.addChildRow(rowData);
 
         String s = getResultsString("Path To containing_experiments", rootRow);
         Approvals.approve(s);
     }
 
 
     /*
     @UseReporter(JunitReporter.class)
     public void test1234()
         throws Exception {
 
         RowData rootRow;
         RowData rowData;
 
         <Write code that creates rootRow>
 
         String s = getResultsString("Information About The Test", rootRow);
         Approvals.approve(s);
     }
     */
 
 
     /**
      * Test the translation of a RowData expression tree to
      * an ExpressionTree, AND test the translation of an
      * ExpressionTree to a RowData expression tree.
      *
      * You pass either a RowData root row object or
      * pass an ExpressionTree object.  The code will translate
      * from one class to the other and then back again, checking
      * that the results match.  If they don't match, an error
      * message is returned.
      *
      * Please note, this method is testing that the translation
      * is "internally" consistent.  E.g. If "abc" translates to
      * "123", then "123" better translate back to "abc".
      * This method does NOT test that the behavior of the
      * translation is consistent between versions of the software.
      * The Approvals toolkit does that.  Please see the comments
      * at the top of this file for more information about that.
      *
      * @param someKindOfTree Pass either a RowData root row object
      * or pass an ExpressionTree object.
      */
     private static String testTranslation(Object someKindOfTree) {
 
         String s = "";
 
         RowData rootRow = null;
         ExpressionTree eTree = null;
 
         if (someKindOfTree instanceof RowData)
             rootRow = (RowData)someKindOfTree;
         else if (someKindOfTree instanceof ExpressionTree)
             eTree = (ExpressionTree)someKindOfTree;
         else {
             String error = "You must pass an Object of type RowData or "+
                     "ExpressionTree.  You passed: "+someKindOfTree;
             throw(new IllegalArgumentException(error));
         }
 
         boolean same;
         if (rootRow != null) {
             s += "\nStarting With RowData:\n"+rootRow;
             eTree = RowDataToExpressionTree.translate(rootRow);
             s += "\nRowData Translated To Expression:\n"+eTree;
             RowData newRowData = ExpressionTreeToRowData.translate(eTree);
             s += "\nExpressionTree Translated Back To RowData:\n"+newRowData;
 
             same = rootRow.toString(true, "").equals(
                     newRowData.toString(true, ""));
         }
         else {
             s += "\nStarting With ExpressionTree:\n"+eTree;
             rootRow = ExpressionTreeToRowData.translate(eTree);
             s += "\nExpressionTree Translated To RowData:\n"+rootRow;
             ExpressionTree newETree = RowDataToExpressionTree.
                     translate(rootRow);
             s += "\nRowData Translated Back To Expression:\n"+newETree;
 
             same = eTree.toString().equals(newETree.toString());
         }
 
         if (same)
             s += "\nOriginal and translated versions are the same.";
         else
             s += "\nERROR:  Original and translated versions are different.";
 
         return(s);
     }
 
 
     /**
      * This method makes the method calls that generate string versions
      * of the objects that were created/translated/serialized.
      * The string version is formatted in a way that makes it
      * easy for a human to look at the output and see what is going on.
      *
      * The returned string will be compared to the "approved" version
      * of the string by the Approvals toolkit.  For more information
      * about that, see the comments at the top of this file.
      *
      * @param label A string that will be displayed at the start of the
      * results string.  This string should be something that tells
      * the engineer what the purpose/significance of this test is.
      */
     private static String getResultsString(String label, RowData rootRow) {
 
         String s = "";
 
         s += "===== "+label+" =====";
 
         s += "\nOriginal RowData:\n"+rootRow;
 
         s += "\nTest Translation: ";
         s += testTranslation(rootRow);
 
         s += "\nTest RowData Serialization: ";
         String origRowData = rootRow.toString(true, "");
         rootRow.writeRowData("temp.rowData");
         rootRow = RowData.readRowData("temp.rowData");
         boolean same = origRowData.equals(rootRow.toString(true, ""));
         if (same)
             s += "RowData de/serialization succeed.";
         else
             s += "RowData de/serialization failed.";
 
         s += "\nTest ExpressionTree Serialization: ";
         ExpressionTree eTree = RowDataToExpressionTree.translate(rootRow);
         String origETree = eTree.toString();
         eTree.writeExpressionTree("temp.expTree");
         eTree = ExpressionTree.readExpressionTree("temp.expTree");
         same = origETree.equals(eTree.toString());
         if (same)
             s += "ExpressionTree de/serialization succeed.";
         else
             s += "ExpressionTree de/serialization failed.";
 
         return(s);
     }
 
 
     /**
      * This starts the testing.
      */
     public static void main(String[] args) {
         junit.textui.TestRunner.run(TranslatorTests.class);
     }
 }
