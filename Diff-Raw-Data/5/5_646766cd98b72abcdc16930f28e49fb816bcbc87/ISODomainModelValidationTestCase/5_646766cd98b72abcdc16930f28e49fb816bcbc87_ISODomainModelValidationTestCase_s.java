 package org.cagrid.iso21090.model.test;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.data.MalformedQueryException;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import gov.nih.nci.ncicb.xmiinout.handler.HandlerEnum;
 
 import java.io.File;
 import java.io.FileReader;
 
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.cagrid.iso21090.model.tools.ISOSupportDomainModelGenerator;
 import org.cagrid.iso21090.model.validator.ISODomainModelValidator;
 
 public class ISODomainModelValidationTestCase extends TestCase {
     
     public static final String XMI_MODEL_FILENAME = "test/resources/sdk.xmi";
     public static final String TEST_QUERIES_DIR = "test/resources/testQueries";
     
     private static ISODomainModelValidator validator = null;
     private static DomainModel model = null;
     
     public ISODomainModelValidationTestCase(String name) {
         super(name);
     }
     
     
     public void setUp() {
         if (validator == null) {
             validator = new ISODomainModelValidator();
         }
         if (model == null) {
             File modelFile = new File(XMI_MODEL_FILENAME);
             assertTrue("XMI model file " + modelFile.getAbsolutePath() + " not found", modelFile.exists());
             try {
                 ISOSupportDomainModelGenerator generator = new ISOSupportDomainModelGenerator(HandlerEnum.EADefault);
                 model = generator.generateDomainModel(modelFile.getAbsolutePath());
             } catch (Exception ex) {
                 ex.printStackTrace();
                 fail("Error reading domain model: " + ex.getMessage());
             }
         }
     }
     
     
     public void testValidateAllPaymentsQuery() {
         validateQuery("allPayments.xml");
     }
     
     
     public void testValidateAllSuperclassQuery() {
         validateQuery("allSuperclass.xml");
     }
     
     
     public void testAssociationNotNullQuery() {
         validateQuery("associationNotNull.xml");
     }
     
     
     public void testAssociationWithAttributeEqualQuery() {
         validateQuery("associationWithAttributeEqual.xml");
     }
     
     
     public void testAssociationWithGroupQuery() {
         validateQuery("associationWithGroup.xml");
     }
     
     
     public void testCountAssociationNotNullQuery() {
         validateQuery("countAssociationNotNull.xml");
     }
     
     
     public void testDistinctAttributeFromCashQuery() {
         validateQuery("distinctAttributeFromCash.xml");
     }
     
     
     public void testGroupOfAssociationsUsingAndQuery() {
         validateQuery("groupOfAssociationsUsingAnd.xml");
     }
     
     
     public void testGroupOfAssociationsUsingOrQuery() {
         validateQuery("groupOfAssociationsUsingOr.xml");
     }
     
     
     public void testGroupOfAttributesUsingAndQuery() {
         validateQuery("groupOfAttributesUsingAnd.xml");
     }
     
     
     public void testGroupOfAttributesUsingOrQuery() {
         validateQuery("groupOfAttributesUsingOr.xml");
     }
     
     
     public void testNestedAssociationsQuery() {
         validateQuery("nestedAssociations.xml");
     }
     
     
     public void testNestedAssociationsNoRoleNamesQuery() {
         validateQuery("nestedAssociationsNoRoleNames.xml");
     }
     
     
     public void testSingleAttributeFromCashQuery() {
         validateQuery("singleAttributeFromCash.xml");
     }
     
     
     public void testUngergraduateStudentWithNameQuery() {
         validateQuery("undergraduateStudentWithName.xml");
     }
     
     
     public void testDsetCdCodeAttributeEqualQuery() {
         validateQuery("dsetCdCodeAttributeEqual.xml");
     }
     
     
     public void testDsetAdPartValueAttributeEqualQuery() {
         validateQuery("dsetAdPartValueAttributeEqual.xml");
     }
     
     
     public void testDsetCdNullFlavorQuery() {
         validateQuery("dsetCdNullFlavor.xml");
     }
     
     
     public void testIvlIntAttributeQuery() {
         validateQuery("ivlIntAttribute.xml");
     }
     
     
     public void testDsetTelAttributeValueQuery() {
         validateQuery("dsetTelAttributeValue.xml");
     }
     
     
     public void testBlNonNullQuery() {
         validateQuery("blNonNullValue.xml");
     }
     
     
     public void testCdDatatypeCodeSystemAttributeQuery() {
         validateQuery("cdDataTypeCodeSystemAttribute.xml");
     }
     
     
     public void testScDataTypeCdCodeSystemAttributeEqualQuery() {
         validateQuery("scDataTypeCdCodeSystemAttributeEqual.xml");
     }
     
     
     public void testIiRootValueQuery() {
         validateQuery("iiRootValue.xml");
     }
     
     
     public void testIvlTsWidthNullFlavorQuery() {
         validateQuery("ivlTsWidthNullFlavor.xml");
     }
     
     
     public void testPqUnitQuery() {
         validateQuery("pqUnitAttribute.xml");
     }
     
     
     public void testRealValueQuery() {
         validateQuery("realDataTypeValue.xml");
     }
     
     
     public void testIvlPqWidthQuery() {
         validateQuery("ivlPqWidth.xml");
     }
     
     
     public void testCdEdTextQuery() {
         validateQuery("dsetCdEdText.xml");
     }
     
     
     public void testCdEdTextValueQuery() {
         validateQuery("dsetCdEdTextValue.xml");
     }
     
     
     private void validateQuery(String queryFilename) {
         CQLQuery query = loadQuery(queryFilename);
         try {
             validator.validateDomainModel(query, model);
         } catch (MalformedQueryException ex) {
             ex.printStackTrace();
             fail("Error validating query: " + ex.getMessage());
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Unknown error: " + ex.getMessage());
         }
     }
     
     
     private CQLQuery loadQuery(String queryFilename) {
         CQLQuery query = null;
         File queryFile = new File(TEST_QUERIES_DIR, queryFilename);
         try {
             FileReader reader = new FileReader(queryFile);
             query = (CQLQuery) Utils.deserializeObject(reader, CQLQuery.class);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error loading query " + queryFilename + ": " + ex.getMessage());
         }
         return query;
     }
     
 
     public static void main(String[] args) {
         TestRunner runner = new TestRunner();
         TestResult result = runner.doRun(new TestSuite(ISODomainModelValidationTestCase.class));
         System.exit(result.errorCount() + result.failureCount());
     }
 }
