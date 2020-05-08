 package org.easetech.easytest.codegen;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.PackageDoc;
 
 /**
 * ValueObject to encapsulate TestCase information
 *
 * @author Ravi Polampelli
 *
 */
 
 public class TestCaseVO {
 
  /** a Map with converter class names and its code */
 	private Map<String, StringBuffer> convertersMap;
 	
 	/** testData for all the test case methods, testcase method name as 
 	 * key and List of its parameters and values as value*/
 	private Map<String, List<Map<String, Object>>> testData;
 	
 	/** Original java class source code*/
 	private StringBuffer sourceCode;
 	
 	/** instance of ClassDoc, contains meta-information 
 	 * about the class like fields, methods*/
 	private ClassDoc classDoc;
 	
 	/** instance of PackageDoc, contains meta-information 
 	 * about the package like interfaces,class, enumrations */
 	private PackageDoc packageDoc;
 	
 	/** template file loaded as Properties*/
 	private Properties properties;
 	
 	/** newCode taht is generated test case code*/
 	private StringBuffer newCode;
 	
 	/** instance of  INamingStrategy*/
 	private INamingStrategy naming;
 	
 	/** Mandatory fields data, method name as key and the list of mandatory parameters as value.
 	 * this data is obtained from the method source code, 
 	 * if a particular field of a user-defined class(which is a parameter to the method) 
 	 * is used inside the method then it becomes the mandatory field */
 	private Map<String, List<String>> testDataMandatoryFields;
 	
 	/** Test Data Missing fields, method name as key and the list of missing parameter test data as value.
 	 * if the parameter is mandatory but data is not provided as the seed data then the field is marked as missing data
 	 * this information is useful for user to check the missing fields data at one place and provide the missing data in seed data file
 	 * */
 	private Map<String, List<String>> testDataMissingFields;
 
 
 	public TestCaseVO() {
 		super();
 	}
 	
 	public TestCaseVO(PackageDoc packageDoc,ClassDoc classDoc,INamingStrategy naming,Properties properties,
 			Map<String, StringBuffer> convertersMap,
 			Map<String, List<Map<String, Object>>> testData,
 			StringBuffer sourceCode,StringBuffer newCode,Map<String, List<String>> testDataMandatoryFields) {
 		super();
 		this.packageDoc = packageDoc;
 		this.classDoc = classDoc;
 		this.naming = naming;
 		this.properties = properties;
 		this.convertersMap = convertersMap;
 		this.testData = testData;
 		this.sourceCode = sourceCode;
 		this.newCode = newCode;
 		this.testDataMandatoryFields = testDataMandatoryFields;
 	}
 	
 
 
 	public Map<String, StringBuffer> getConvertersMap() {
 		return convertersMap;
 	}
 	public void setConvertersMap(Map<String, StringBuffer> convertersMap) {
 		this.convertersMap = convertersMap;
 	}
 	public Map<String, List<Map<String, Object>>> getTestData() {
 		return testData;
 	}
 	public void setTestData(Map<String, List<Map<String, Object>>> testData) {
 		this.testData = testData;
 	}
 	public StringBuffer getSourceCode() {
 		return sourceCode;
 	}
 	public void setSourceCode(StringBuffer sourceCode) {
 		this.sourceCode = sourceCode;
 	}
 
 	public ClassDoc getClassDoc() {
 		return classDoc;
 	}
 
 	public void setClassDoc(ClassDoc classDoc) {
 		this.classDoc = classDoc;
 	}
 
 	public PackageDoc getPackageDoc() {
 		return packageDoc;
 	}
 
 	public void setPackageDoc(PackageDoc packageDoc) {
 		this.packageDoc = packageDoc;
 	}
 
 	public Properties getProperties() {
 		return properties;
 	}
 
 	public void setProperties(Properties properties) {
 		this.properties = properties;
 	}
 	
 	
 	public StringBuffer getNewCode() {
 		return newCode;
 	}
 
 	public void setNewCode(StringBuffer newCode) {
 		this.newCode = newCode;
 	}
 	
 
 	public INamingStrategy getNaming() {
 		return naming;
 	}
 
 	public void setNaming(INamingStrategy naming) {
 		this.naming = naming;
 	}
 
 	public Map<String, List<String>> getTestDataMandatoryFields() {
 		return testDataMandatoryFields;
 	}
 
 	public void setTestDataMandatoryFields(Map<String, List<String>> testDataMandatoryFields) {
 		this.testDataMandatoryFields = testDataMandatoryFields;
 	}
 	
 	public Map<String, List<String>> getTestDataMissingFields() {
 		return testDataMissingFields;
 	}
 
 	public void setTestDataMissingFields(
 			Map<String, List<String>> testDataMissingFields) {
 		this.testDataMissingFields = testDataMissingFields;
 	}
 	
 	@Override
 	public String toString() {
 		return "TestCaseVO [convertersMap=" + convertersMap + ", testData="
 				+ testData + ", sourceCode=" + sourceCode + ", classDoc="
 				+ classDoc + ", packageDoc=" + packageDoc + ", properties="
 				+ properties + ", newCode=" + newCode + ", naming=" + naming
 				+ ", testDataMandatoryFields=" + testDataMandatoryFields
 				+ ", testDataMissingFields=" + testDataMissingFields + "]";
 	}
 	
 }

