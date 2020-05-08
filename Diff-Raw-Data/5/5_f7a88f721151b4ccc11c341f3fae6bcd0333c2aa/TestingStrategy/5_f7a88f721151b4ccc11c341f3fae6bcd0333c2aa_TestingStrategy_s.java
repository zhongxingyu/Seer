 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
  	EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.FieldDoc;
 import com.sun.javadoc.MethodDoc;
 import com.sun.javadoc.PackageDoc;
 import com.sun.javadoc.Parameter;
 import com.sun.javadoc.Type;
 
 /**
 * An implementation of ITestingStrategy
 * It contains the logic for test case generation for suite, class, method etc..
 * Fetches the required variables for template from ClassDoc, 
 * Populate those variables and apply on template to generate the target test cases
 *
 * @author Ravi Polampelli
 *
 */
 
 public class TestingStrategy extends ConfigurableStrategy implements ITestingStrategy, JUnitDocletProperties {
 
     /**
      * An instance of logger associated.
      */
     public static final Logger LOG = LoggerFactory.getLogger(TestingStrategy.class);
 	
 	// TODO Accessor Tests fuer Enumerations
 
     protected static final String TESTSUITE_SUITE_METHOD_NAME = "suite";
     protected static final String JUNIT_TEST_CLASS_NAME       = "junit.framework.Test";
     protected static final String ACCESSOR_STARTS_WITH[][] = {{"set", "get"},{"set", "is"}};
     protected static int INDEX_SET = 0;
     protected static int INDEX_GET = 1;
 
     private static String[] requiredStrings = null;
     public static final String[] MINIMUM_MARKER_SET = {
         VALUE_MARKER_IMPORT_BEGIN,
         VALUE_MARKER_IMPORT_END,
         VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN,
         VALUE_MARKER_EXTENDS_IMPLEMENTS_END,
         VALUE_MARKER_CLASS_BEGIN,
         VALUE_MARKER_CLASS_END
     };
 
 
 
     public void init() {
         super.init();
         setProperties(null);
     }
     
     /**
      * 
      * Checks if package that is encapsulated in package doc is testable 
      * It mainly checks if PackageDoc name is test package name.
      * 
      * @param doc the package doc
      * @param naming naming strategy object, used to find out test package name
      */
     
     public boolean isTestablePackage(PackageDoc doc, INamingStrategy naming) {
 
         boolean returnValue;
 
         returnValue = (doc != null);
         returnValue = returnValue && (naming != null) && !naming.isTestPackageName(doc.name());
 
         return returnValue;
     }
 
     /**
      * 
      * Checks if class that is encapsulated in class doc is testable 
      * It mainly checks if class is not abstract, interface, protected, private, annotated, enum or itself is test etc...
      * not a suite method
      * and it is a public class
      * 
      * @param doc the class doc
      * @param naming naming strategy object, used to find out test class name
      */
     public boolean isTestableClass(ClassDoc doc, INamingStrategy naming) {
 
         boolean  returnValue;
 
         returnValue = (doc != null);
         returnValue = returnValue && !doc.isAbstract();
         returnValue = returnValue && !doc.isInterface();
         returnValue = returnValue && !doc.isProtected();
         returnValue = returnValue && !doc.isPrivate();
         returnValue = returnValue && !doc.isAnnotationType();
         returnValue = returnValue && !doc.isEnum();
         returnValue = returnValue && !isInnerClass(doc);
         returnValue = returnValue && doc.isPublic();
         returnValue = returnValue && !isATest(doc);
         returnValue = returnValue && (naming != null) && !naming.isTestClassName(doc.qualifiedName());
         returnValue = returnValue && !hasSuiteMethod(doc);
 
         return returnValue;
     }
 
     /**
      * 
      * Checks if method that is encapsulated in method doc is testable 
      * It mainly checks if method is not abstract,  protected, private, annotated, enum const
      * It is a public method
      * 
      * @param doc the class doc
      * @param naming naming strategy object, used to find out test class name
      */
     public boolean isTestableMethod(MethodDoc doc) {
 
         boolean returnValue;
 
         returnValue = (doc != null);
         returnValue = returnValue && !doc.isAbstract();
         returnValue = returnValue && !doc.isProtected();
         returnValue = returnValue && !doc.isPrivate();
         returnValue = returnValue && !doc.isAnnotationTypeElement();
         returnValue = returnValue && !doc.isEnumConstant();
         returnValue = returnValue && doc.isPublic();
 
         return returnValue;
     }
     
   
     /**
      * 
      * Generates test suite code  
      * It gets the test suite properties (sub package test suites, test classes, suite name etc..)
      * and loads the required template part and apply the test suite properties on this template
      * to get the actual test suite code 
      * 
      * @param testSuiteVO the test suite value object 
      * @param indexPackage this is index of PackageDoc of PackageDoc array contained 
      * in testSuiteVO for which test suit to be generated.
      */
     
     public boolean codeTestSuite(TestSuiteVO testSuiteVO,int indexPackage) {
 
         LOG.info("codeTestSuite started");
     	boolean    returnValue;
         Properties addProps;
         String     template;
         PackageDoc[] packageDocs = testSuiteVO.getPackageDocs();
 
         returnValue = (packageDocs != null);
         returnValue = returnValue && (indexPackage >= 0);
         returnValue = returnValue && (indexPackage < packageDocs.length);
         returnValue = returnValue && (testSuiteVO.getNaming() != null);
         returnValue = returnValue && (testSuiteVO.getNewCode() != null);
         returnValue = returnValue && (testSuiteVO.getProperties() != null);
 
         if (returnValue) {
             returnValue = isTestablePackage(packageDocs[indexPackage], testSuiteVO.getNaming());
 
             if (returnValue) {    // test this package
                 addProps = getTestSuiteProperties(testSuiteVO,indexPackage);
                 template = getTemplate(addProps, "testsuite", addProps.getProperty(TEMPLATE_NAME));
                 
                 if(isTestSuiteExist(addProps) || isTestCaseExist(addProps)) {
                 	testSuiteVO.getNewCode().append(StringHelper.replaceVariables(template, addProps));                
                 } else {
                 	LOG.info("There are no test classes for your preference " +
                 			"in this suite hence no test suite will be generated:"+packageDocs[indexPackage].name());  
                 }
             } // no else
         } else {
             printError("TestingStrategy.codeTestSuite() parameter error");
         }
         LOG.info("codeTestSuite finished,returnValue:"+returnValue);
         return returnValue;
     }
 
     private boolean isTestCaseExist(Properties addProps) {
     	boolean isTestCaseExist = false;
     	
     	if(addProps.getProperty(TESTSUITE_ADD_TESTCASES)!=null && 
         		!"".equals(addProps.getProperty(TESTSUITE_ADD_TESTCASES))){
     		isTestCaseExist = true;
     	}
 		return isTestCaseExist;
 	}
 
 	private boolean isTestSuiteExist(Properties addProps) {
     	boolean isTestSuiteExist = false;
     	
     	if(addProps.getProperty(TESTSUITE_ADD_TESTSUITES)!=null && 
         		!"".equals(addProps.getProperty(TESTSUITE_ADD_TESTSUITES))){
     		isTestSuiteExist = true;
     	}
 		return isTestSuiteExist;
 	}
 	
     /**
      * 
      * Generates test class/case code  
      * It gets the test case properties (test methods, parameters, names etc..)
      * and loads the required template part then apply the test case properties on this template
      * to get the actual test class/case code 
      * 
      * @param testCaseVO the test case value object
      */
 	public boolean codeTestCase(TestCaseVO testCaseVO) {
 
     	LOG.info("codeTestCase started");
     	boolean    returnValue;
         Properties addProps;
         String     template;
         ClassDoc classDoc = testCaseVO.getClassDoc();
 
         // check if all parameters are non-null
         returnValue = (testCaseVO.getClassDoc() != null);
         returnValue = returnValue && (testCaseVO.getPackageDoc() != null);
         returnValue = returnValue && (testCaseVO.getNaming() != null);
         returnValue = returnValue && (testCaseVO.getNewCode() != null);
         returnValue = returnValue && (testCaseVO.getProperties() != null);
 
         if (returnValue) {
             returnValue = isTestableClass(classDoc, testCaseVO.getNaming());
 
             if (returnValue) {    // test this class
             	                
                 addProps = getTestCaseProperties(testCaseVO);
                 template = getTemplate(addProps, "testcase", addProps.getProperty(TEMPLATE_NAME));
                 LOG.debug("getTestCaseProperties:"+addProps);
                 LOG.debug("template:"+template); 
                 if(addProps.getProperty(TESTCASE_TESTMETHODS)!=null && 
                 		!"".equals(addProps.getProperty(TESTCASE_TESTMETHODS))) {
                 	testCaseVO.getNewCode().append(StringHelper.replaceVariables(template, addProps));                
                 } else {
                 	LOG.info("There are no test methods for your preference " +
                 			"in this class hence no test case will be generated:"+testCaseVO.getClassDoc().name()); 
                 }
                 
             } // no else
         } else {
             printError("TestingStrategy.codeTestCase() parameter error");
         }
         LOG.info("codeTestCase finished,returnValue:"+returnValue);
         return returnValue;
     }
 	
     /**
      * 
      * Generates test method code in a test class  
      * It gets the test method properties (parameters, types, return types, names etc..)     * 
      * and loads the required template part then apply the test method properties on this template
      * to get the actual test method code 
      * There are two templates used
      * 1) void return types
      * 2) non-void return types - this is introduced to capture the result/output data from test method
      * 
      * @param testCaseVO the test case value object
      * @param testMethodVO the test case value object
      */
     public boolean codeTest(TestCaseVO testCaseVO, TestMethodVO testMethodVO,int index) {
     	MethodDoc[] methodDocs = testMethodVO.getMethodDocs();
     	LOG.info("codeTest started,index:"+index+methodDocs[index].name());
     	boolean    returnValue;
         Properties addProps;
         String     template;
 
         // check if all parameters are non-null and index is in range
         returnValue = (methodDocs != null);
         returnValue = returnValue && (index >= 0);
         returnValue = returnValue && (index < methodDocs.length);
         returnValue = returnValue && (testCaseVO.getClassDoc() != null);
         returnValue = returnValue && (testCaseVO.getPackageDoc() != null);
         returnValue = returnValue && (testCaseVO.getNaming() != null);
         returnValue = returnValue && (testMethodVO.getNewCode() != null);
         returnValue = returnValue && (testMethodVO.getProperties() != null);
         
         LOG.debug("all parameters are non-null and index is in range?:"+returnValue);
         
         if (returnValue) {
             returnValue = isTestableMethod(methodDocs[index]);
             LOG.debug("isTestableMethod:"+returnValue);
 
             if (returnValue) {    // test this method
                 addProps = getTestProperties(testCaseVO,testMethodVO,index);
                 //List<Map<String, Object>> methodData = getTestMethodMap(methodDocs, index, classDoc, packageDoc, naming, properties);
                 LOG.debug("getTestProperties:"+addProps);
                 if (addProps != null) {
                     // test if not tested already
                 	String returnType = addProps.getProperty(METHOD_RETURNTYPE);
                 	if(returnType != null && !returnType.equalsIgnoreCase("void")) {
                 		template = getTemplate(addProps, "testmethod", addProps.getProperty(TEMPLATE_NAME));
                 	} else {
                 		template = getTemplate(addProps, "testmethodvoid", addProps.getProperty(TEMPLATE_NAME));
                 	}
                     LOG.debug("template:"+template);
                     testMethodVO.getNewCode().append(StringHelper.replaceVariables(template, addProps));
 
                 } // no else
             } // no else
         } else {
             printError("TestingStrategy.codeTestCase() parameter error");
         }
         LOG.info("codeTest finished,returnValue:"+returnValue);
         return returnValue;
     }
     
     /**
      * Gets test suite properties, sub pcakge test suites, test classes of this package:<br>
      * \@pre fields of test case vo (classDoc != null) && (packageDoc != null) && (naming != null) && (properties != null) <br>
      * \@post return != null <br>
      * @param sourceCode 
      * @param filterProperties 
      *
      * @return new Properties instance with all properties for parameter 'properties'
      *         and test case specific properties
      */
 	public Properties getTestSuiteProperties(TestSuiteVO testSuiteVO, int indexPackage) {
 
         Properties returnValue = new Properties(testSuiteVO.getProperties());
 
         returnValue.setProperty(TESTSUITE_PACKAGE_NAME, testSuiteVO.getNaming().getTestPackageName(testSuiteVO.getPackageDocs()[indexPackage].name()));
         returnValue.setProperty(TESTSUITE_CLASS_NAME, testSuiteVO.getNaming().getTestSuiteName(testSuiteVO.getPackageDocs()[indexPackage].name()));
         returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
         returnValue.setProperty(TESTSUITE_ADD_TESTSUITES, getTestSuiteAddTestSuites(testSuiteVO,indexPackage));
         returnValue.setProperty(TESTSUITE_ADD_TESTCASES, getTestSuiteAddTestCases(testSuiteVO,indexPackage));
         returnValue.setProperty(TESTSUITE_IMPORTS, getTestSuiteImports(testSuiteVO));
         returnValue.setProperty(PACKAGE_NAME, testSuiteVO.getPackageDocs()[indexPackage].name());
         return returnValue;
     }
 
     /**
      * Gets test case properties, test methods, registers for converters and editors:<br>
      * \@pre fields of test case vo (classDoc != null) && (packageDoc != null) && (naming != null) && (properties != null) <br>
      * \@post return != null <br>
      * @param testCaseVO
      *
      * @return new Properties instance with all properties for parameter 'properties'
      *         and test case specific properties
      */
     public Properties getTestCaseProperties(TestCaseVO testCaseVO) {
 
     	LOG.info("getTestCaseProperties started,");
     	Properties returnValue = new Properties(testCaseVO.getProperties());
 
         returnValue.setProperty(TESTCASE_PACKAGE_NAME, testCaseVO.getNaming().getTestPackageName(testCaseVO.getPackageDoc().name()));
         returnValue.setProperty(TESTCASE_CLASS_NAME, testCaseVO.getNaming().getTestCaseName(testCaseVO.getClassDoc().name()));
         returnValue.setProperty(TESTCASE_INSTANCE_NAME, testCaseVO.getClassDoc().name().substring(0,1).toLowerCase()+testCaseVO.getClassDoc().name().substring(1));
         returnValue.setProperty(TESTCASE_INSTANCE_TYPE, testCaseVO.getClassDoc().qualifiedName());
         //List of imports to be added to test case
         Set<String> importsSet = new HashSet<String>();
         TestMethodVO testMethodVO = new TestMethodVO(null,returnValue,importsSet,null,null);
         // Gets all the test methods of the test case
         returnValue.setProperty(TESTCASE_TESTMETHODS, getTestMethods(testCaseVO, testMethodVO));
         returnValue = testMethodVO.getProperties();
         returnValue.setProperty(TESTCASE_METHOD_UNMATCHED, VALUE_METHOD_UNMATCHED_NAME);
         returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
         returnValue.setProperty(PACKAGE_NAME, testCaseVO.getPackageDoc().name());
         returnValue.setProperty(CLASS_NAME, testCaseVO.getClassDoc().name());
         //test data file path, the path will be same as test class, the actual location (root folder) may be different
         String testDataFilePath = StringHelper.getFilePath(returnValue.getProperty(TESTCASE_PACKAGE_NAME),
        													returnValue.getProperty(TESTCASE_CLASS_NAME));        
         returnValue.setProperty(TESTCASE_DATA_FILE_PATH, testDataFilePath);
         //gets the converter registers to be added in test class @BeforeClass method
         returnValue.setProperty(TESTCASE_REGISTER_CONVERTERS, getConverterRegisters(returnValue,testCaseVO.getConvertersMap()));
     	
       //gets the editor registers to be added in test class @BeforeClass method
         returnValue.setProperty(TESTCASE_REGISTER_EDITORS, getEditorRegisters(returnValue,testCaseVO.getConvertersMap()));
 
         returnValue.setProperty(TESTCASE_IMPORTS, codeImports(returnValue,testMethodVO.getImportsSet()));
         
         LOG.info("getTestCaseProperties finished,returnValue:"+returnValue);
         LOG.debug("testData map at testclass level:"+testCaseVO.getTestData());
         return returnValue;
     }
 
     private String codeImports(Properties returnValue,
 			Set<String> importsSet) {
     	StringBuffer importsListValue = new StringBuffer();
     	if(importsSet.size()>0){
         	
         	String template = getTemplate(returnValue, "class", "import");
         	for(String importClassName :importsSet ){           
 	            returnValue.setProperty(PARAM_CLASS_TYPE, importClassName);
 	            importsListValue.append(StringHelper.replaceVariables(template, returnValue));
         	}
         }
     	LOG.debug("getTestCaseImports:"+importsListValue.toString());
 		return importsListValue.toString();
 	}
 
 
 
 	private String getTestMethods(TestCaseVO testCaseVo,TestMethodVO testMethodVO) {
     	LOG.debug("getTestMethods started,");
     	StringBuffer sb;
         MethodDoc[] methodDocs;
 
         methodDocs = testCaseVo.getClassDoc().methods(false);
         sb         = new StringBuffer();
         testMethodVO.setNewCode(sb);
         for (int i=0; i< methodDocs.length; i++) {
             if (isTestableMethod(methodDocs[i])) {
             	int startPosition = methodDocs[i].position().line();
             	int endPosition = 0;
             	if(i+1<methodDocs.length){
             		endPosition = methodDocs[i+1].position().line()-1; 
             	}
             	StringBuffer methodSourceCode = getMethodSourceCode(testCaseVo.getSourceCode(),startPosition,endPosition);
             	testMethodVO.setMethodSourceCode(methodSourceCode);
             	//check the filters then generate test code
             	if(isFiltered(methodSourceCode,testMethodVO.getProperties())) {
             		testMethodVO.setMethodDocs(methodDocs);
             		//actual method to generate test code
             		codeTest(testCaseVo,testMethodVO, i);
             	}
             }
         }
         LOG.debug("getTestMethods finished,"+testMethodVO.getNewCode().toString());
         return testMethodVO.getNewCode().toString();
     }
 
     private boolean isFiltered(StringBuffer sourceCode, Properties filterProperties) {
     	boolean isFiltered = false;
     	LOG.debug("isFiltered started");
     	String includeFilter = filterProperties.getProperty(FILTER_INCLUDE);
     	String excludeFilter = filterProperties.getProperty(FILTER_EXCLUDE);
     	if(includeFilter != null || excludeFilter != null) {
     		String[] includeFilters = null;
     		String[] excludeFilters = null;
     		if(includeFilter != null) {
     			includeFilters = includeFilter.split(",");
     		}
     		if(excludeFilter != null) {
     			excludeFilters = excludeFilter.split(",");
     		}
     		isFiltered = checkFilter(sourceCode,includeFilters,excludeFilters);
     	} else {
     		isFiltered = true;
     	}
 
 
 		LOG.debug("isFiltered finished with value:"+isFiltered);
 		return isFiltered;
 	}
     
     /**
      * Gets the method source code for given class between the lines start and end position.
      * It loads the source code into String Buffer and gets the substring between the start and end position
      * @param excludeFilters 
      * @param String fullClassName
      * @param String path file path
      * @param int startPosition
      * @param int endPostion
      * @return String if successfully loaded or null if either  file does not exist or line does not exist.
      */
     
     private boolean checkFilter(StringBuffer javaClassSource,
     		String[] includeFilters, String[] excludeFilters){
     	
     	LOG.debug("checkFilter started: startPosition:");
 
     	boolean isFiltered = false;
     	
     	String[] sourceCodeLines = javaClassSource.toString().split("\n");
  
 		for(int i=0;i<sourceCodeLines.length;i++){
 			LOG.debug("sourceCodeLines[i]:"+i+", "+sourceCodeLines[i]);
 			
 			//priority to exclude filters
 			if(excludeFilters != null){    			
     			for(int j=0;j<excludeFilters.length;j++){
     				LOG.debug("excludeFilters[j]"+j+excludeFilters[j]);
     				if(sourceCodeLines[i].contains(excludeFilters[j])){
     					return false;
     				}
     			}
 			}
 			//then check include filters
 			if(includeFilters != null){ 
     			for(int j=0;j<includeFilters.length;j++){
     				LOG.debug("includeFilters[j]"+j+includeFilters[j]);
     				if(sourceCodeLines[i].contains(includeFilters[j])){
     					return true;
     				}
     			}
 			}
 		}
 		//after checking include and exclude filters on method source code
 		// if includeFilters are not there then return true
 		if(includeFilters == null) 
 			return true;
 	
 
     	LOG.debug("checkFilter finished with value"+isFiltered);
     	return isFiltered;
     }
     
     private StringBuffer getMethodSourceCode(StringBuffer javaClassSource,
 			int startPosition, int endPosition) {
     	
     	String[] sourceCodeLines = javaClassSource.toString().split("\n");
     	StringBuffer methodSourceCode = new StringBuffer();
     	
     	if(startPosition <0) startPosition = 0;
     	if(endPosition <= 0) endPosition = sourceCodeLines.length;
     	if(startPosition > 0){
     		for(int i=startPosition;i<endPosition;i++){    			
     			i = skipCommentLines(sourceCodeLines,i);
     			methodSourceCode.append(sourceCodeLines[i]+"\n");
     		}
     	}
 		return methodSourceCode;
 	}
 
 	// Method to count no.of lines in comment and add commented line index to array
  	private int skipCommentLines(String[] sourceCodeLines, int textIndex){
 
  		boolean isSkipped = false;
  		String sourceCodeLine = sourceCodeLines[textIndex];
  		// check if single line comment
  		if(sourceCodeLine.trim().startsWith("//")) {
  			//System.out.println("skipCommentLines starts ");
  			//System.out.println("comments text"+sourceCodeLines);
  			textIndex++;
  			isSkipped = true;
  			//System.out.println("skipCommentLines finished ");
  		}
  		
  		//check for multiple line count, i.e. this line is start of block comment and then count upto close of block comment.		
  		if(sourceCodeLine.trim().startsWith("/*")) {
  			//System.out.println("skipCommentLines starts ");
  			//System.out.println(" comments start text"+sourceCodeLines);			
 
  			//increment count
  			textIndex++;
  			//check if line has ending comment "*/" otherwise loop through the lines till ending comment appears
  			while(!sourceCodeLine.contains("*/")) {				
  				sourceCodeLine = sourceCodeLines[textIndex];
  				//increment count
  				textIndex++;				
  			}
  			isSkipped = true;			
  		}
  		sourceCodeLine = sourceCodeLines[textIndex];
  		if(isSkipped && sourceCodeLine!= null) {			
  			if(sourceCodeLine.trim().startsWith("//") || sourceCodeLine.trim().startsWith("/*") ) {	
  				//System.out.println("skipCommentLines recursive started"+sourceCodeLines);
  				textIndex = skipCommentLines(sourceCodeLines,textIndex);
  				//System.out.println("skipCommentLines recursive finished"+recursiveIsSkipped);
  			}
  			isSkipped = true;
  		}
  		
  		return textIndex;		
  	}
 
 	/**
      * Comment on DBC:<br>
      * \@pre (methodDoc != null) && (classDoc != null) && (packageDoc != null) && (naming != null) && (properties != null) <br>
      * @param testData 
      * @param convertersMap 
      * @param sourceCode 
      * @param importsList 
      *
      * @return if the method specified by 'index' needs a test, new Properties instance with all properties for parameter 'properties'
      *           and test method specific properties;
      *           null if the method specified by 'index' needs no test
      */
     public Properties getTestProperties(TestCaseVO testCaseVO, TestMethodVO testMethodVO, int index) {
         Properties returnValue = null;
         StringBuffer signature = null;
         StringBuffer paramValues = null;
         Parameter[] parameters = null;
         MethodDoc[] methodDocs = testMethodVO.getMethodDocs();
         returnValue = getTestAccessorProperties(testCaseVO,testMethodVO,index);
         // returnValue == null means, no test for this accessor
 
         if ((returnValue == testMethodVO.getProperties()) && (returnValue != null)) {
             // not an accessor
 
             if (isFirstTestableMethodWithName(methodDocs, index)) {
                 returnValue = new Properties(testMethodVO.getProperties());
                 returnValue.setProperty(TESTMETHOD_NAME, testCaseVO.getNaming().getTestMethodName(methodDocs[index].name()));
                 //returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_DEFAULT);
                 returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_EASYTEST);
                 returnValue.setProperty(METHOD_NAME, methodDocs[index].name());
                 signature = new StringBuffer("");
                 paramValues = new StringBuffer("");
                
                 testMethodVO.setMethodData(new ArrayList<Map<String,Object>>());
                 parameters = methodDocs[index].parameters();
                 LOG.debug("methodDocs[index].position()"+methodDocs[index].position().toString());
                 LOG.debug("methodDocs[index].position() line"+methodDocs[index].position().line());
                 LOG.debug("methodDocs[index].position() column"+methodDocs[index].position().column());
                 //building method data map
                 Map<String, Object> data = new LinkedHashMap<String,Object>();
                 List<String> mandatoryFields = new ArrayList<String>();
                 
                 for (int i=0; i<getNumberOfParameters(methodDocs[index]); i++) {             
                     
                 	if (i>0) {
                         signature.append(", ");
                         paramValues.append(", ");
                     } // no else
                     signature.append("@Param(name=\""+parameters[i].name()+"\")");
                     signature.append(parameters[i].typeName()+" ");                    
                     signature.append(parameters[i].name());
                     paramValues.append(parameters[i].name());                    
                     
                     //checking if parameter is of complex type
                     Type paramType = parameters[i].type();
                     //if parameter is collection or any other type in java.util than Date,Time..
                     // then we can not create converter hence we can not include that in EasyTest framework
                 	String paramTypeName = paramType.qualifiedTypeName();
                 	LOG.debug("Param Type qualifiedTypeName is :"+paramTypeName);
                 	if(!isJavaDateType(paramType) && paramTypeName.contains("java.util.")){
                 		return null;
                 	}
                     LOG.debug("Param Type is :"+paramType.typeName());
                     if(paramType != null){
                     	LOG.debug("ParamType is primitive:"+paramType.isPrimitive());
                     	Properties converterProperties = new Properties(returnValue);
                     	codeConvertersAndEditors(converterProperties,paramType,
                     			testCaseVO,data,parameters[i].name(),testMethodVO,mandatoryFields);
                     	if(!isSimpleType(paramType)){
                     		testMethodVO.getImportsSet().add(paramType.qualifiedTypeName());
                     	}                    	
                     }                                        
                 }
                 testMethodVO.getMethodData().add(data);
                 testCaseVO.getTestData().put(returnValue.getProperty(TESTMETHOD_NAME), testMethodVO.getMethodData());
                 testCaseVO.getTestDataMandatoryFields().put(returnValue.getProperty(TESTMETHOD_NAME), mandatoryFields);
                 
                 returnValue.setProperty(METHOD_SIGNATURE, signature.toString());
                 returnValue.setProperty(METHOD_PARAMETER_VALUES, paramValues.toString());
                 
                 Type returnType = methodDocs[index].returnType();
                 if(returnType != null ){
                 	returnValue.setProperty(METHOD_RETURNTYPE, getReturnTypeName(returnType));
                 	//if return type is not simple type, then add that in test case imports list
                 	if(!isSimpleType(returnType)) {
                 		testMethodVO.getImportsSet().add(returnType.qualifiedTypeName());
                 	} 
                 }
             } else {
                 // not the first overloaded method (multiple methods sharing one name and one test)
                 returnValue = null;
             }
         } // no else
         LOG.debug("testData map:"+testCaseVO.getTestData());
         return returnValue;
     }
     
     
     
     private String getReturnTypeName(Type returnType) {
 		String returnTypeName = returnType.simpleTypeName();
 		if(returnType.dimension() != null && 
 				returnType.dimension().equals("[]")){
 			returnTypeName = returnTypeName.concat("[]");
 			
 		}
 		return returnTypeName;
 	}
 
 	private void codeConvertersAndEditors(Properties returnValue, Type type,
 			TestCaseVO testCaseVO,
 			Map<String, Object> data, String parameterName, 
 			TestMethodVO testMethodVO,List<String> mandatoryFields) {
     	LOG.debug("codeConvertersAndEditors started :"+type.qualifiedTypeName());
     	LOG.debug("isSimpleType:"+isSimpleType(type));
     	LOG.debug("isTypeEditor:"+isTypeEditor(type));
     	if(isSimpleType(type) || isJavaDateType(type) || type.asClassDoc().isEnum()){
     		Object defaultObject = getDefaultObjValue(type);    		
             data.put(parameterName, defaultObject);
             mandatoryFields.add(parameterName);
     	} 
     	//commenting editors
     	/*else if(isTypeEditor(type) && 
     			!testCaseVO.getConvertersMap().containsKey(type.typeName()+EDITOR_CLASS_NAME_SUFFIX)){
     		LOG.debug("Type requires Editor:"+type.qualifiedTypeName());
     		codeEditors(returnValue,type,testCaseVO,testMethodVO);
     		// TODO check parameter type and add default values of that type
     		data.put(parameterName, "defaultString");
     		mandatoryFields.add(parameterName);
     	} */ 
     	else if(!testCaseVO.getConvertersMap().containsKey(type.typeName()+CONVERTER_CLASS_NAME_SUFFIX)){
     		LOG.debug("Type is not primitive, String:"+type.qualifiedTypeName());
     		codeConverterClasses(returnValue,type,parameterName,testCaseVO,data,testMethodVO,mandatoryFields);
     		//setting complex parameter type in method data
     		//following method gets all fields in side that complex type and add it to method data map
     		//setTypeFieldsInMethodData(returnValue,type,data);
     	} else {
     		LOG.debug("Type is not matched with any condition:"+type.qualifiedTypeName());
     	}
     	LOG.debug("codeConvertersAndEditors setter data:"+data);
     	LOG.debug("codeConvertersAndEditors finished :"+type.qualifiedTypeName());
 	}
 
 	private Object getDefaultObjValue(Type type) {
 		Object objValue = null;
 		String typeName = type.qualifiedTypeName();
 		long now = System.currentTimeMillis();
 		if(typeName.equals("int") || 
 				typeName.equals("short") || 
 				typeName.equals("long") ||
 				typeName.equals("float") ||
 				typeName.equals("double") ||
 				typeName.equals("java.lang.Integer") ||
 				typeName.equals("java.lang.Short") ||
 				typeName.equals("java.lang.Long") ||
 				typeName.equals("java.lang.Float") ||
 				typeName.equals("java.lang.Double")){
 			objValue = 0;
 		} else if(typeName.equals("java.lang.String")){
 			objValue = "defaultString";
 		} else if(typeName.equals("java.lang.Character")){
 			objValue = null;
 		} else if(typeName.equals("char")){
 			objValue = null;
 		} else if(typeName.equals("java.lang.Boolean") || 
 				typeName.equals("boolean")){
 			objValue = false;
 		} else if(typeName.equals("java.lang.Byte")){
 			objValue = new Byte((byte) 0);
 		} else if(typeName.equals("byte")){
 			objValue = (byte) 0;
 		} else if(typeName.equals("java.util.Date")){
 			objValue = new Date(now);
 		} else if(typeName.equals("java.sql.Date")){
 			objValue = new java.sql.Date(now);
 		} else if(typeName.equals("java.sql.Time")){
 			objValue = new java.sql.Time(now);
 		} else if(typeName.equals("java.sql.Date")){
 			objValue = new java.sql.Timestamp(now);
 		}
 		return objValue;
 	}
 
 	private void codeEditors(Properties returnValue, Type type,
 			TestCaseVO testCaseVO, TestMethodVO testMethodVO) {
 		LOG.debug("codeEditors started :"+type.qualifiedTypeName());
 
     	returnValue.setProperty(EDITOR_CLASS_NAME, type.simpleTypeName()+EDITOR_CLASS_NAME_SUFFIX);
     	returnValue.setProperty(EDITOR_INSTANCE_TYPE_FULLNAME, type.qualifiedTypeName());
     	returnValue.setProperty(EDITOR_INSTANCE_TYPE, type.simpleTypeName());
     	String editorSetValue = "";
     	if(isTypeJodaDateTime(type)){
     		String template = getTemplate(returnValue, "editor", "setvaluejodadatetime");
     		editorSetValue = StringHelper.replaceVariables(template, returnValue);
     	} 
     	returnValue.setProperty(EDITOR_SETVALUE, editorSetValue);
 
         String template = getTemplate(returnValue, "editor", "default");
         LOG.debug("template:"+template);
         StringBuffer editorCode = new StringBuffer();        
         editorCode.append(StringHelper.replaceVariables(template, returnValue));
         testCaseVO.getConvertersMap().put(returnValue.getProperty(EDITOR_CLASS_NAME),editorCode);
         testMethodVO.getImportsSet().add(type.qualifiedTypeName());
         LOG.debug("codeEditors finsihed :"+type.qualifiedTypeName());		
 	}
 
 
 
 
 	private void codeConverterClasses(Properties returnValue, Type type,String parameterName, 
 			TestCaseVO testCaseVO, 
 			Map<String, Object> data, 
 			 TestMethodVO testMethodVO,List<String> mandatoryFields) {
 		LOG.debug("codeConverterClasses started :"+type.qualifiedTypeName());
 
     	returnValue.setProperty(CONVERTER_CLASS_NAME, type.typeName()+CONVERTER_CLASS_NAME_SUFFIX);
     	returnValue.setProperty(CONVERTER_INSTANCE_TYPE, type.typeName());
     	returnValue.setProperty(CONVERTER_INSTANCE_NAME, getInstanceNameForTypeName(type.typeName()));
     	Set<String> converterImportsSet = new HashSet<String>();
     	returnValue.setProperty(CONVERTER_SETTERS, codeConverterSetters(type,parameterName,returnValue,data,testCaseVO,
     													testMethodVO,converterImportsSet,mandatoryFields));
     	returnValue.setProperty(CONVERTER_IMPORTS, codeImports(returnValue,converterImportsSet));
     	
     	StringBuffer converterCode = new StringBuffer();
         String template = getTemplate(returnValue, "converter", "default");
         LOG.debug("template:"+template);
         converterCode.append(StringHelper.replaceVariables(template, returnValue));
         LOG.debug("converterCode:"+converterCode);
         testCaseVO.getConvertersMap().put(returnValue.getProperty(CONVERTER_CLASS_NAME), converterCode);
         LOG.debug("codeConverterClasses finished :"+type.qualifiedTypeName());
 	}
 
 	private String getInstanceNameForTypeName(String typeName) {
 		return typeName.substring(0,1).toLowerCase()+typeName.substring(1);
 	}
 
 	private String codeConverterSetters(Type type,String parameterName,
 			Properties returnValue,Map<String, Object> data, 
 			TestCaseVO testCaseVO, TestMethodVO testMethodVO, Set<String> converterImportsSet,List<String> mandatoryFields) {
 		LOG.debug("codeConverterSetters started :"+type.qualifiedTypeName());
 		StringBuffer setterCode = new StringBuffer();
 		ClassDoc typeClassDoc = type.asClassDoc();
     	FieldDoc[] typeFields = typeClassDoc.fields(false);
 
 
         String templatePrimitiveConverter = getTemplate(returnValue, "converter", "setmethodprim");
         String templateNonPrimConverter = getTemplate(returnValue, "converter", "setmethodnonprim");
         String templateEditorConverter = getTemplate(returnValue, "converter", "setmethodeditor");
         String templateConverterSetter = getTemplate(returnValue, "converter", "setmethodconverter");
         String templateNoConverterSetter = getTemplate(returnValue, "converter", "setmethodnoconverter");
         //LOG.debug("template:"+templateStringConverter);
         LOG.debug("typeFields length"+typeFields.length);
         converterImportsSet.add(type.qualifiedTypeName());
         for(int i=0;i<typeFields.length;i++){
         	Type fieldType = typeFields[i].type();
         	String typeName = fieldType.simpleTypeName();
         	String fieldSetterName = getSetterName(typeClassDoc,typeFields[i].name());
         	LOG.debug("typeName"+typeName);
         	//if this is static final public field then no need to set this value
         	// hence skip
         	if(!isSettableField(typeFields[i])){
         		continue;
         	}
         	LOG.debug("fieldType.qualifiedTypeName():"+fieldType.qualifiedTypeName());
         	LOG.debug("typeFields[i].name():"+typeFields[i].name());
         	LOG.debug("typeFields[i].name():"+typeFields[i].qualifiedName());
         	LOG.debug("typeFields[i].isOrdinaryClass():"+typeFields[i].isOrdinaryClass());
         	LOG.debug("fieldType.dimension():"+fieldType.dimension());
         	
         	
         	if(isSimpleType(fieldType) || isJavaDateType(fieldType) || isTypeEnum(fieldType)) {
 	        	LOG.debug("typeFields[i].name():"+typeFields[i].name());
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_SETTER_NAME,fieldSetterName);
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_TYPE, fieldType.simpleTypeName());
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_NAME, typeFields[i].name());
 
 	        	//typeFields[i].
 	        	if(fieldType.isPrimitive() || isTypeEnum(fieldType)){
 	        		returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_TYPE_WRAPPER, getTypeBoxName(fieldType.simpleTypeName()));
 	        		setterCode.append(StringHelper.replaceVariables(templatePrimitiveConverter, returnValue));
 	        	} else {
 	        		
 	        		setterCode.append(StringHelper.replaceVariables(templateNonPrimConverter, returnValue));
 	        	}
 	        	Object defaultObject = getDefaultObjValue(fieldType);
 	        	data.put(typeFields[i].name(), defaultObject);
 	        	if(isMandatory(testMethodVO.getMethodSourceCode(),parameterName,typeFields[i].name())){
 	        		mandatoryFields.add(typeFields[i].name());
 	        	}
 	        	if(!isSimpleType(fieldType)){
 	        		converterImportsSet.add(fieldType.qualifiedTypeName());	
 	        	}
         	} 
         	//commenting editor code now
         	/*else if(isTypeEditor(fieldType)){
         		if( !testCaseVO.getConvertersMap().containsKey(fieldType.typeName()+EDITOR_CLASS_NAME_SUFFIX)){
             		LOG.debug("Type requires Editor:"+fieldType.qualifiedTypeName());
             		codeEditors(returnValue,fieldType,testCaseVO,testMethodVO);
         		}
         		LOG.debug("typeFields[i].name() editor setter:"+typeFields[i].name());
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_SETTER_NAME, fieldSetterName);
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_TYPE, fieldType.simpleTypeName());
 	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_NAME, typeFields[i].name());
 	        	returnValue.setProperty(EDITOR_CLASS_NAME, typeName+EDITOR_CLASS_NAME_SUFFIX);	        	
 	        		        	
 	        	setterCode.append(StringHelper.replaceVariables(templateEditorConverter, returnValue));
 	        	converterImportsSet.add(fieldType.qualifiedTypeName());	        	
 	        	// TODO check parameter type and add default values of that type
         		data.put(typeFields[i].name(), "defaultString");
         		
         		
 	        	if(isMandatory(testMethodVO.getMethodSourceCode(),parameterName,typeFields[i].name())){
 	        		mandatoryFields.add(typeFields[i].name());
 	        	}
 	        	
         	}*/
         	
         	else {
         		//this field is of complex type, hence need to create a converter and set the value
         		//check if converter exist for this complex type, otherwise create it.
         		if(testCaseVO.getConvertersMap().containsKey(fieldType.typeName()+CONVERTER_CLASS_NAME_SUFFIX)){
         			LOG.debug("set the converter of:"+typeFields[i].name());
             		returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_SETTER_NAME, fieldSetterName);
     	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_CONVERTER, typeName+CONVERTER_CLASS_NAME_SUFFIX);
     	        		        	
     	        	setterCode.append(StringHelper.replaceVariables(templateConverterSetter, returnValue));
     	        	converterImportsSet.add(fieldType.qualifiedTypeName());
     	        	
             		
         		} else {
         			LOG.debug("set the converter of:"+typeFields[i].name());
             		returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_SETTER_NAME, fieldSetterName);
     	        	returnValue.setProperty(CONVERTER_INSTANCE_ATTRIBUTE_CONVERTER, typeName+CONVERTER_CLASS_NAME_SUFFIX);
     	        		        	
     	        	setterCode.append(StringHelper.replaceVariables(templateNoConverterSetter, returnValue));
     	        	
         			// TODO : need to create converters.
         			/*
         			LOG.debug("Type requires converter:"+fieldType.qualifiedTypeName());
             		Properties properties = new Properties(returnValue);
             		//using recursion if field type is complex
             		codeConvertersAndEditors(properties,fieldType,convertersMap,data,typeFields[i].name(),importsSet);
             		*/
         			
         		}       		
         		
         	}
         }
         LOG.debug("setterCode:"+setterCode);
         LOG.debug("codeConverterSetters started :"+type.qualifiedTypeName());
         LOG.debug("converter setter data:"+data);
 		return setterCode.toString();
 	}
 
 
 
 	private boolean isTypeEnum(Type fieldType) {
 		boolean isEnum = false;
 
 		if(isSimpleType(fieldType)){
 			isEnum = false;
 		} else if(fieldType.asClassDoc().isEnum() || 
 				fieldType.asClassDoc().isFinal() || 
 				fieldType.asClassDoc().isEnumConstant()) {
 			isEnum = true;
 		}
 		return isEnum;
 	}
 
 	private boolean isMandatory(StringBuffer methodSourceCode, String objectName,String name) {
 		LOG.debug("isMandatory started: objectName"+objectName+", fieldName:"+name);
 		LOG.debug("methodSourceCode:"+methodSourceCode.toString());
 		String[] methodSourceLines = methodSourceCode.toString().split("\n");
 		String searchString = objectName+"."+"get"+name;
 		for(String line:methodSourceLines){
 			if(line!=null && line.toUpperCase().contains(searchString.toUpperCase())){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private String getSetterName(ClassDoc typeClassDoc, String fieldName) {
 		LOG.debug("getSetterName started:"+typeClassDoc.name()+fieldName);
 		String setterName = null;
 		MethodDoc[] methods = typeClassDoc.methods();
 		for(MethodDoc method:methods){
 			String methodName = method.name();			
 			if(methodName.contains("set") && methodName.toUpperCase().contains(fieldName.toUpperCase())){
 				setterName = methodName;
 				break;
 			}
 		}
 		if(setterName == null){
 			setterName = StringHelper.getSetterName(fieldName);
 		}
 		LOG.debug("getSetterName finished:"+setterName);
 		return setterName;
 	}
 
 	private String getEditorRegisters(Properties returnValue,
 			Map<String, StringBuffer> convertersMap) {
     	
 		String editorsRegisterStr = "";
 		if(convertersMap.size() > 0){
 			StringBuffer editorsRegister = new StringBuffer();
 	        String template = getTemplate(returnValue, "editor", "register");
 	
 	        for(String editorClass:convertersMap.keySet()){
 	        	if(editorClass.endsWith(EDITOR_CLASS_NAME_SUFFIX)) {
 	        		returnValue.setProperty(EDITOR_CLASS_NAME, editorClass);
 	        		String editorInstanceName = editorClass.replace(EDITOR_CLASS_NAME_SUFFIX, "");
 	        		returnValue.setProperty(EDITOR_INSTANCE_TYPE, editorInstanceName);        		
 	        		editorsRegister.append(StringHelper.replaceVariables(template, returnValue));
 	        	}
 	        }
 	        editorsRegisterStr = editorsRegister.toString();
 	    	
 		}
 		LOG.debug("editorsRegisterStr:"+editorsRegisterStr);
 		return editorsRegisterStr;
 	}
 
 	private String getConverterRegisters(Properties returnValue, Map<String, StringBuffer> convertersMap) {
 		String convertersRegisterStr = "";
 		if(convertersMap.size() > 0){
 			StringBuffer convertersRegister = new StringBuffer();
 			
 	        String template = getTemplate(returnValue, "converter", "register");
 	
 	        for(String converterClass:convertersMap.keySet()){
 	        	if(converterClass.endsWith(CONVERTER_CLASS_NAME_SUFFIX)) {
 	        		returnValue.setProperty(CONVERTER_CLASS_NAME, converterClass);
 	        		convertersRegister.append(StringHelper.replaceVariables(template, returnValue));
 	        	}
 	        }
 	        convertersRegisterStr = convertersRegister.toString();
 		}
     	LOG.debug("convertersRegister.toString():"+convertersRegisterStr);
 		return convertersRegisterStr;
 	}
 	
 	private boolean isSimpleType(Type type) {
 		//check if it primitive type or wrapper simple types in java.lang package 
 		// i.e. int, Integer,String etc..
 		return type.isPrimitive() || type.qualifiedTypeName().contains("java.lang.");
 	}
     
     //checks if this type is avaialble in java.lang or java.util or java.sql
     private boolean isJavaDateType(Type type) {
 
     	boolean isJavaDateType = false;
     	String typeName = type.qualifiedTypeName();
     	if(isSimpleType(type)) {
     		isJavaDateType = true;
     	} else if (typeName.equals("java.sql.Timestamp")){
     		isJavaDateType = true;
     	} else if(typeName.equals("java.sql.Date")) {
     		isJavaDateType = true;
     	} else if(typeName.equals("java.sql.Time")) {
     		isJavaDateType = true;
     	} else if(typeName.equals("java.util.Date")) {
     		isJavaDateType = true;
     	} else {
     		isJavaDateType = false;
     	}
 		return isJavaDateType;
 	}
 	
 	private boolean isTypeEditor(Type fieldType) {
 		boolean isTypeEditor = false;
     	if(isJavaDateType(fieldType)) {
     		isTypeEditor = false;
     	} else {
     		isTypeEditor = isTypeJodaDateTime(fieldType);
     	}
 		return isTypeEditor;
 	}
 	
 	private boolean isSettableField(FieldDoc typeFields) {
 		boolean isSettableField = true;
 		///ClassDoc fieldTypeClassDoc = typeFields.asClassDoc();
 		if(typeFields != null && 
 				(typeFields.isStatic()  || 
 				typeFields.isFinal())){
 			isSettableField = false;
 		}
 		if(typeFields!=null && 
 				(typeFields.type().dimension() != null && 
 				  typeFields.type().dimension().equals("[]"))){
 			isSettableField = false;
 		}
 		return isSettableField;
 	}	
 
 	
 	private boolean isTypeJodaDateTime(Type type) {		
 		//checking enum or constant
 		String typeName = type.qualifiedTypeName();
 		return "org.joda.time.DateTime".equals(typeName);
 	}
 	
 	private String getTypeBoxName(String typeName) {
 		String typeBoxName = null;
 		if("int".equals(typeName)){
 			typeBoxName = "Integer";
 		} else if("short".equals(typeName)){
 			typeBoxName = "Short";
 		} else if("float".equals(typeName)){
 			typeBoxName = "Float";
 		} else if("double".equals(typeName)){
 			typeBoxName = "Double";
 		} else if("long".equals(typeName)){
 			typeBoxName = "Long";
 		}else if("boolean".equals(typeName)){
 			typeBoxName = "Boolean";	
 		}else if("byte".equals(typeName)){
 			typeBoxName = "Byte";	
 		}else if("char".equals(typeName)){
 			typeBoxName = "Character";	
 		}else {
 			typeBoxName = typeName;	
 		}
 		return typeBoxName;
 	}
 	
 	private String getConverterUtilMethodName(String typeName){
 		typeName = getTypeBoxName(typeName);
 		String utilMethodName = null;
 		
 		if(typeName.endsWith("Integer")){
 			utilMethodName = "convertToInteger";
 		} else if(typeName.endsWith("Short")){
 			utilMethodName = "convertToShort";
 		} else if(typeName.endsWith("Long")){
 			utilMethodName = "convertToLong";
 		} else if(typeName.endsWith("Float")){
 			utilMethodName = "convertToFloat";
 		} else if(typeName.endsWith("Double")){
 			utilMethodName = "convertToDouble";
 		} else if(typeName.equals("java.util.Date") || typeName.equals("Date") ){
 			utilMethodName = "convertToDate";
 		} else if(typeName.equals("java.sql.Date")){
 			utilMethodName = "convertToSQLDate";
 		} else if(typeName.equals("java.sql.Time")){
 			utilMethodName = "convertToSQLTime";
 		} else if(typeName.equals("java.sql.Timestamp")){
 			utilMethodName = "convertToSQLTimestamp";
 		} else if(typeName.endsWith("Boolean")){
 			utilMethodName = "convertToBoolean";
 		} else if(typeName.endsWith("Byte")){
 			utilMethodName = "convertToByte";
 		} else if(typeName.endsWith("Character")){
 			utilMethodName = "convertToCharacter";
 		} 
 		return utilMethodName;
 	}	
 	
 
 	public String getTestSuiteAddTestSuites(TestSuiteVO testSuiteVO, int indexPackage) {
         StringBuffer sb;
         String template;
         String templateForNormalItem;
         String templateForLastItem;
         Properties addProps;
         PackageDoc[] subPackages;
 
         sb                    = new StringBuffer();
         addProps              = new Properties(testSuiteVO.getProperties());
         templateForNormalItem = getTemplate(testSuiteVO.getProperties(), ADD_TESTSUITE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);
         templateForLastItem   = getTemplate(testSuiteVO.getProperties(), ADD_TESTSUITE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT_LAST);
 
         subPackages = getDirectSubPackages(testSuiteVO.getPackageDocs(), indexPackage);
         List<String> testSuiteList = testSuiteVO.getTestSuiteClasses();
         for (int i=0;i<testSuiteList.size();i++) {
             if( i==testSuiteList.size()-1 && isNotEmpty(templateForLastItem) ) {
                 template = templateForLastItem;
             } else {
                 template = templateForNormalItem;
             }
 
             if (isTestSuiteDirectSubPackage(testSuiteList.get(i),subPackages)) {
                 addProps.setProperty(ADD_TESTSUITE_NAME,testSuiteList.get(i));
                 //addProps.setProperty(TESTSUITE_PACKAGE_NAME, testSuiteVO.getNaming().getTestPackageName(subPackages[i].name()));
                 sb.append(StringHelper.replaceVariables(template, addProps));
             }
         }
 
         return sb.toString();
     }
 
     private boolean isTestSuiteDirectSubPackage(String testSuiteName,
 			PackageDoc[] subPackages) {    	
     	for(PackageDoc subPackageDoc:subPackages){
     		if(testSuiteName.contains(subPackageDoc.name())) {
     			return true;
     		}    	
     	}
 
 		return false;
 	}
 
 	public String getTestSuiteAddTestCases(TestSuiteVO testSuiteVO, int indexPackage) {
         StringBuffer sb;
         String template;
         String templateForNormalItem;
         String templateForLastItem;
         Properties addProps;
         //ClassDoc[] classes;
 
         sb                    = new StringBuffer();
         addProps              = new Properties(testSuiteVO.getProperties());
         templateForNormalItem = getTemplate(testSuiteVO.getProperties(), ADD_TESTCASE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);
         templateForLastItem   = getTemplate(testSuiteVO.getProperties(), ADD_TESTCASE_TO_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT_LAST);
         //classes               = testSuiteVO.getPackageDocs()[indexPackage].ordinaryClasses();
 
         // If there are any testsuites, they are placed behind the testcases.
         // In such case, we never use the template for last testcase (the one without trailing ',').
         if( isTestSuiteExist(testSuiteVO,indexPackage)) {
         	
             templateForLastItem = null;
         }
         List<String> testClassNames = testSuiteVO.getTestClasses();
 
         for (int i=0; i<testClassNames.size(); i++) {
             //if (isTestableClass(classes[i], testSuiteVO.getNaming())) {
                 addProps.setProperty(ADD_TESTCASE_NAME, testClassNames.get(i));
                 //addProps.setProperty(TESTSUITE_PACKAGE_NAME, testSuiteVO.getNaming().getTestPackageName(testSuiteVO.getPackageDocs()[indexPackage].name()));
                 //sb.append(StringHelper.replaceVariables(template, addProps));
                 if(i<testClassNames.size()-1){
                 	sb.append(StringHelper.replaceVariables(templateForNormalItem, addProps));
                 } else {
                 	if(templateForLastItem!= null) {
                 		sb.append(StringHelper.replaceVariables(templateForLastItem, addProps));
                 	} else {
                 		sb.append(StringHelper.replaceVariables(templateForNormalItem, addProps));
                 	}
                 }
             //}
         }
 
         return sb.toString();
     }
 
 
 	private boolean isTestSuiteExist(TestSuiteVO testSuiteVO, int indexPackage) {
 		String testSuites = getTestSuiteAddTestSuites(testSuiteVO, indexPackage);
 		
 		if(testSuites != null && !"".equals(testSuites)){
 			return true;
 		}
 		return false;
 	}
 
 	public String getTestSuiteImports(TestSuiteVO testSuiteVO) {
         StringBuffer sb;
         String template;
         Properties addProps;
 
         sb = new StringBuffer();
         addProps = new Properties(testSuiteVO.getProperties());
         template = getTemplate(testSuiteVO.getProperties(), ADD_IMPORT_TESTSUITE, TEMPLATE_ATTRIBUTE_DEFAULT);
         
         for (String testSuiteName:testSuiteVO.getTestSuiteClasses()) {
              addProps.setProperty(ADD_TESTSUITE_NAME, testSuiteName);
              //addProps.setProperty(TESTSUITE_PACKAGE_NAME, testSuiteVO.getNaming().getTestPackageName(subPackages[i].name()));
              sb.append(StringHelper.replaceVariables(template, addProps));
         }
 
         return sb.toString();
     }
 
     public boolean isFirstTestableMethodWithName(MethodDoc[] methodDocs, int index) {
         boolean returnValue = true;
         String reference;
 
         reference = methodDocs[index].name();
 
         for (int i=0; (i<index) && returnValue; i++) {
             if (reference.equals(methodDocs[i].name()) && isTestableMethod(methodDocs[i])) {
                 returnValue = false;
             }
         }
 
         return returnValue;
     }
 
     public int countTestableMethodsWithName(MethodDoc[] methodDocs, String methodName) {
         int returnValue =0;
 
         for (int i=0; (i<methodDocs.length); i++) {
             if (methodName.equals(methodDocs[i].name()) && isTestableMethod(methodDocs[i])) {
                 returnValue++;
             }
         }
 
         return returnValue;
 
     }
 
     /**
      * Builds accessor specific properties if the method specified by 'index' is an accessor method.
      *
      * @return if specfied method is an set accessor, returns properties with all properties from
      *         parameter 'properties' and accessor specific properties;
      *         if specfied method is an get accessor, return null;
      *         if  specfied method is not an accessor, returns parameter 'properties' unchanged
      */
     public Properties getTestAccessorProperties(TestCaseVO testCaseVO, TestMethodVO testMethodVO,int index) {
         Properties returnValue = null;
         String testMethodName;
         String methodName;
         int    indexAccessorPair;
         int    indexArray;
         String accessedPropertyName;
         String setAccessorName;
         String getAccessorName;
         String testsByType;
         String accessorTypeName;
         Parameter[] parameters;
         MethodDoc[] methodDocs = testMethodVO.getMethodDocs();
 
 
         methodName = methodDocs[index].name();
         indexAccessorPair = getAccessorPairIndex(methodDocs, index);
 
         if (indexAccessorPair >= 0) {
 
             if ((methodName.startsWith(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET])) &&
                 (isFirstTestableMethodWithName(methodDocs, index))) {
                 // testSetGet
 
                 accessedPropertyName = getAccessedPropertyName(methodName, indexAccessorPair);
 
                 if ((accessedPropertyName != null) && (accessedPropertyName.length() > 0))
                 {
                     testMethodName = testCaseVO.getNaming().getTestAccessorName(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET],
                                                                 ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET],
                                                                 accessedPropertyName);
                     setAccessorName = ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_SET]+accessedPropertyName;
                     getAccessorName = ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET]+accessedPropertyName;
                     parameters = methodDocs[index].parameters();
                     if ((parameters != null) && (parameters.length == 1)) {
                         accessorTypeName = parameters[0].typeName();
                         indexArray = accessorTypeName.indexOf("[]");
                         if (indexArray == -1) {
                             testsByType = getAccessorTestsByType(testMethodVO.getProperties(), TEMPLATE_ATTRIBUTE_DEFAULT, accessorTypeName);
                         } else{
                             testsByType = getAccessorTestsByType(testMethodVO.getProperties(), TEMPLATE_ATTRIBUTE_ARRAY, accessorTypeName.substring(0, indexArray));
                         }
                         returnValue = new Properties(testMethodVO.getProperties());
                         returnValue.setProperty(ACCESSOR_TESTS, testsByType);
                         returnValue.setProperty(ACCESSOR_NAME, testMethodName);
                         returnValue.setProperty(ACCESSOR_SET_NAME, setAccessorName);
                         returnValue.setProperty(ACCESSOR_GET_NAME, getAccessorName);
                         returnValue.setProperty(ACCESSOR_TYPE_NAME, accessorTypeName);
                         returnValue.setProperty(TESTMETHOD_NAME, testMethodName);
                         returnValue.setProperty(TEMPLATE_NAME, TEMPLATE_ATTRIBUTE_ACCESSOR);
                         returnValue.setProperty(METHOD_NAME, methodDocs[index].name());
                     }
                 } else {
                     // method is not an accessor
                     returnValue = testMethodVO.getProperties();
                 }
             }
 
             if (methodName.startsWith(ACCESSOR_STARTS_WITH[indexAccessorPair][INDEX_GET])) {
                 // if method is a get-accessor and there is a set accessor  -> nothing to do here
                 returnValue = null;
             }
 
         } else {
             returnValue = testMethodVO.getProperties();
         }
 
         return returnValue;
     }
 
 
     /**
      * A method is considered an accessor if (i) method name starts with certain prefixes,
      * (ii) prefix is followed by a property name (that is longer than the empyt string ""),
      * (iii) there are methods with this property name for both 'get' and 'set' prefixes,
      * (iv) number of parameters for the get method is 0 and number of parameter for the set method is 1.
      *
      * @return -1 = not both accessors found or not an accessor method,
      *         0 or above = index of prefix in ACCESSOR_STARTS_WITH method of the method specified by 'index'
      */
     public int getAccessorPairIndex(MethodDoc[] methodDocs, int index) {
         int returnValue = -1;
         String accessedPropertyName;
         String setAccessorName;
         String getAccessorName;
         boolean foundSet = false;
         boolean foundGet = false;
         boolean exactlyOneParamSet = true;
         boolean exactlyZeroParamGet = true;
 
         if (isTestableMethod(methodDocs[index])) {
             for (int i = 0; (returnValue == -1) && (i < ACCESSOR_STARTS_WITH.length); i++) {
                 accessedPropertyName = getAccessedPropertyName(methodDocs[index].name(), i);
 
                 if ((accessedPropertyName != null) && (accessedPropertyName.length() > 0 )) {
                     setAccessorName = ACCESSOR_STARTS_WITH[i][INDEX_SET]+accessedPropertyName;
                     getAccessorName = ACCESSOR_STARTS_WITH[i][INDEX_GET]+accessedPropertyName;
 
                     for (int j=0; (returnValue == -1) && (j<methodDocs.length); j++) {
                         if (isTestableMethod(methodDocs[j])) {
                             if (getAccessorName.equals(methodDocs[j].name())) {
                                 foundGet            |= true;
                                 exactlyZeroParamGet &= (getNumberOfParameters(methodDocs[j]) == 0);
                             } else if (setAccessorName.equals(methodDocs[j].name())) {
                                 foundSet            |= true;
                                 exactlyOneParamSet   &= (getNumberOfParameters(methodDocs[j]) == 1);
                             }
 
                         } // no else
                     }
                     if (foundGet && foundSet && exactlyOneParamSet && exactlyZeroParamGet)
                     {
                         returnValue = i;
                     } // no else
                 } // no else, is not an accessor method
             }
         } // no else
 
         return returnValue;
     }
 
     /**
      * Comment on DBC:<br>
      * \@pre methodDoc != null <br>
      */
     private static int getNumberOfParameters(MethodDoc methodDoc)
     {
         if (methodDoc.parameters() != null) {
             return methodDoc.parameters().length;
         } else {
             return 0;
         }
     }
 
     /**
      * @return name of accessed property if 'accessorMethodName' starts with an accessor prefix
      *     specified by 'indexAccessorPair' (see field ACCESSOR_STARTS_WITH),
      *     null in all other cases.
      */
     public String getAccessedPropertyName(String accessorMethodName, int indexAccessorPair) {
         String returnValue = null;
         String prefix;
         if ((accessorMethodName != null) && (accessorMethodName.length()>0)) {
             for (int setOrGet =0; ((returnValue == null) && (setOrGet<ACCESSOR_STARTS_WITH[indexAccessorPair].length)); setOrGet++) {
                 prefix = ACCESSOR_STARTS_WITH[indexAccessorPair][setOrGet];
                 if (accessorMethodName.startsWith(prefix)) {
                     returnValue = accessorMethodName.substring(prefix.length());
                 }
             }
         }
         return returnValue;
     }
 
     public String getAccessorTestsByType(Properties properties, String templateAttribute, String type) {
 
         String returnValue = null;
         String template;
         Properties addProps;
 
         if (TEMPLATE_ATTRIBUTE_DEFAULT.equals(templateAttribute)) {
             returnValue = properties.getProperty(ACCESSOR_TESTS + "." + type);
         }
 
         if (returnValue == null) {
             template = getTemplate(properties, ACCESSOR_TESTS, templateAttribute);
             addProps = new Properties(properties);
             addProps.put(ACCESSOR_TYPE_NAME, type);
             returnValue = StringHelper.replaceVariables(template, addProps);
         }
 
         if (returnValue != null) {
             returnValue = returnValue.trim();
         }
         return returnValue;
     }
 
     public boolean isInnerClass(ClassDoc doc) {
         boolean returnValue = false;
 
         if (doc != null) {
             returnValue = (-1 < doc.name().indexOf("."));
         }
 
         return returnValue;
     }
 
     public boolean isATest(ClassDoc doc) {
         boolean returnValue = false;
 
         ClassDoc temp;
         String   tempName;
         ClassDoc interfaces[];
 
         temp = doc;
         // iterate over this class and all super classes
         while (!returnValue && (temp != null)) {
             tempName = temp.qualifiedName();
             if (tempName.equals(JUNIT_TEST_CLASS_NAME)) {
                 returnValue = true;  // Is junit.framework.Test a super class? (true for very old versions of JUnit)
             } else {
                 interfaces = temp.interfaces();
                 // iterate over all interfaces
                 for (int i=0; ((interfaces != null) && (i<interfaces.length)); i++) {
                     tempName = interfaces[i].qualifiedName();
                     if (tempName.equals(JUNIT_TEST_CLASS_NAME)) {
                         returnValue = true; // Is this class or any super class implementing junit.framework.Test?
                     }
                 }
             }
             temp = temp.superclass();
         }
         return returnValue;
     }
 
     public boolean hasSuiteMethod(ClassDoc doc) {
 
         boolean     returnValue = false;
         MethodDoc[] methods     = doc.methods();
 
         for (int i = 0; !returnValue && (i < methods.length); i++) {
             MethodDoc method = methods[i];
 
             returnValue |= TESTSUITE_SUITE_METHOD_NAME.equals(method.name()) && method.isStatic();
         }
 
         return returnValue;
     }
 
     public PackageDoc[] getDirectSubPackages(PackageDoc[] packageDocs, int indexCurrentPackage) {
         List<PackageDoc> list;
         String subStart;
         String tempPackageName;
 
         list = new LinkedList<PackageDoc>();
         subStart = packageDocs[indexCurrentPackage].name()+ ".";
 
         for (int i=0; i< packageDocs.length; i++) {
             tempPackageName = packageDocs[i].name();
             if ((i != indexCurrentPackage) &&                              // is not current
                 tempPackageName.startsWith(subStart) &&                    // is sub package (may be indirect)
                 (-1 == tempPackageName.indexOf(".", subStart.length()))) { // is direct sub package (no further ".")
                 list.add(packageDocs[i]);
             }
         }
         return (PackageDoc[]) list.toArray( new PackageDoc[0]);
     }
 
     public boolean isValid(String code) {
         return hasAllRequiredStrings(code) && isValidStructure(code);
     }
 
     public boolean hasAllRequiredStrings(String code) {
         boolean returnValue = true;
 
         // create array w/ required strings
         if (requiredStrings == null) {
             requiredStrings = new String[MINIMUM_MARKER_SET.length];
 
             for (int i = 0; i < MINIMUM_MARKER_SET.length; i++) {
                 requiredStrings[i] = MINIMUM_MARKER_SET[i].trim();
             }
         }
 
         // check if code contains all required markers
         for (int i = 0; i < requiredStrings.length; i++) {
             if (code.indexOf(requiredStrings[i]) == -1) {
                 returnValue = false;
             }
         }
         return returnValue;
     }
 
     public boolean isValidStructure(String code) {
 
         boolean returnValue = true;
         int     indexBegin;
         int     indexEnd;
         int     indexContentBegin;
         int     indexContentEnd;
         String  markDescription;
 
         if (code != null) {
 
             indexBegin = code.indexOf(VALUE_MARKER_BEGIN);
             indexEnd = code.indexOf(VALUE_MARKER_END);
 
             while (returnValue && (indexBegin < indexEnd) && (indexBegin > -1)) {
                 markDescription = code.substring(indexBegin + VALUE_MARKER_BEGIN.length(),
                                                  code.indexOf("\n", indexBegin));
 
                 indexEnd = indexBegin+VALUE_MARKER_BEGIN.length();
                 do {
                     indexEnd = code.indexOf(VALUE_MARKER_END + markDescription, indexEnd);
                 } while ((indexEnd>0) && (Character.isWhitespace(code.charAt(indexEnd))));
 
                 if (indexEnd > -1) {
                     indexContentBegin = code.indexOf("\n", indexBegin+VALUE_MARKER_BEGIN.length());
                     indexContentEnd   = code.lastIndexOf("\n", indexEnd);
                     if (indexContentBegin < indexContentEnd) {
                         returnValue = isValidStructure(code.substring(indexContentBegin, indexContentEnd));
                     }
                 } else {
                     returnValue = false;
                 }
                 indexBegin = code.indexOf(VALUE_MARKER_BEGIN, indexEnd+1);
                 indexEnd = code.indexOf(VALUE_MARKER_END, indexEnd+1);
             }
 
             returnValue = returnValue && (indexBegin * indexEnd > 0); // existing pairwise, if existing at all
             returnValue = returnValue && ((indexBegin<0) || (indexBegin<indexEnd));
         } else {
             printError("TestingStrategy.isValidStructure() code == null");
 
             returnValue = false;
         }
 
         return returnValue;
     }
 
     /**
      * Merges all markers from inCode into inOutCode. In the end all markers from oldCode
      * will be in newCode as well. If nessesary some new generated default content in
      * newCode gets overwritten. If some markers are not in newCode any more, they will
      * be moved to testVault, a special test method.
      *
      * @param inOutCode points to the in-out StringBuffer with the new code
      * @param inCode holds all markers to be merged into to newCode
      * @param fullClassName is used only for the error message, if anything goes wrong.
      * @return true if successfully merged, false if old code contains no JUnitDoclet markers.
      */
 
     public boolean merge(StringBuffer inOutCode, StringBuffer inCode, String fullClassName) {
 
         boolean      returnValue = true;
         String       newContent;
         String       oldContent;
         String       markDescription;
         String       markContent;
         int          oldIndexLeft;
         int          oldIndexRight;
         int          insertFromIndex;
         int          insertToIndex;
         StringBuffer unmatched;
 
         if (inOutCode != null) {
             if (inCode != null) {
                 oldContent       = inCode.toString();
                 unmatched        = new StringBuffer();
                 oldIndexLeft     = oldContent.indexOf(VALUE_MARKER_BEGIN, 0);
                 oldIndexRight    = oldContent.indexOf("\n", oldIndexLeft) + "\n".length();
 
                 if (isValid(oldContent)) {
                     while ((oldIndexRight > -1) && (oldIndexLeft > -1)) {
                         markDescription = oldContent.substring(oldIndexLeft + VALUE_MARKER_BEGIN.length(), oldIndexRight).trim();
                         oldIndexLeft    = oldIndexRight;
                         oldIndexRight   = oldContent.indexOf(VALUE_MARKER_END + markDescription, oldIndexLeft);
                         oldIndexRight   = oldContent.lastIndexOf("\n", oldIndexRight) + "\n".length();
                         markContent     = oldContent.substring(oldIndexLeft, oldIndexRight);
                         newContent      = inOutCode.toString();
 
                         insertFromIndex = 0;
                         do {
                             insertFromIndex = newContent.indexOf(VALUE_MARKER_BEGIN + markDescription, insertFromIndex);
                             if (insertFromIndex > -1) {
                                 insertFromIndex = insertFromIndex + VALUE_MARKER_BEGIN.length() + markDescription.length();
                             }
                         } while ((insertFromIndex > -1) && (!Character.isWhitespace(newContent.charAt(insertFromIndex))));
 
                         if (insertFromIndex > -1) {
                             // go to end of line
                             while ((insertFromIndex - 1 < newContent.length())
                                     && (newContent.charAt(insertFromIndex - 1) != '\n')) {
                                 insertFromIndex++;
                             }
                             insertToIndex = newContent.indexOf(VALUE_MARKER_END + markDescription, insertFromIndex);
                         } else {
                             insertToIndex = -1;
                         }
 
                         // go back to begin of line
                         while ((insertToIndex > 0) && (newContent.charAt(insertToIndex - 1) == ' ')) {
                             insertToIndex--;
                         }
 
                         if ((insertFromIndex != -1) && (insertToIndex != -1)) {
                             if (containsCodeOrComment(markContent)) {
                                 // replace only, if old marker was empty
                                 inOutCode.replace(insertFromIndex, insertToIndex, markContent);
                             } // no else
                         } else {
 
                             // no match found -> append special method if there is some content
                             if (containsCodeOrComment(markContent)) {
                                 unmatched.append(VALUE_MARKER_BEGIN + markDescription);
                                 unmatched.append("\n");
                                 unmatched.append(markContent);
                                 unmatched.append(VALUE_MARKER_END + markDescription);
                                 unmatched.append("\n");
                             } // no else
                         }
 
                         oldIndexLeft  = oldContent.indexOf(VALUE_MARKER_BEGIN, oldIndexRight);
                         oldIndexRight = oldContent.indexOf("\n", oldIndexLeft) + "\n".length();
                     }
 
                     if (unmatched.length() > 0) {
 
                         // there have been unmatched blocks
                         newContent    = inOutCode.toString();
                         insertToIndex = newContent.lastIndexOf(VALUE_METHOD_UNMATCHED_NAME);
 
                         // go back to begin of line
                         while ((insertToIndex > 0) && (newContent.charAt(insertToIndex - 1) != '\n')) {
                             insertToIndex--;
                         }
 
                         if (insertToIndex != -1) {
                             inOutCode.insert(insertToIndex, unmatched.toString());
                         } // no else
                     }
 
                     if (hasUnmatchedMarkers(inOutCode.toString())) {
                         printWarning("Class " + fullClassName + " contains unmatched tests.");
                     }
 
                 } else {
                     printWarning("Class " + fullClassName + " was not generated by JUnitDoclet. It's not overwritten.\n"+
                                  "Please rename and start JUnitDoclet again.");
                     returnValue = false;
                 }
             } // no else
         } else {
             printError("TestingStrategy.merge() inOutCode == null");
         }
         return returnValue;
     }
 
     public boolean containsCodeOrComment(String markContent) {
         boolean returnValue = false;
         char    ch;
 
         if ((markContent != null) && (markContent.length() > 0)) {
             for (int i=0; (!returnValue && (i<markContent.length())); i++) {
                 ch = markContent.charAt(i);
                 returnValue = !Character.isWhitespace(ch);
             }
         } // no else
 
         return returnValue;
     }
 
     public boolean hasUnmatchedMarkers(String code) {
         boolean returnValue = false;
         int     beginUnmatched;
         int     endUnmatched;
         int     tempUnmatched;
 
         beginUnmatched = StringHelper.indexOfTwoPartString(code, VALUE_MARKER_METHOD_BEGIN, VALUE_METHOD_UNMATCHED_NAME_MARKER, 0);
         endUnmatched   = StringHelper.indexOfTwoPartString(code, VALUE_MARKER_METHOD_END, VALUE_METHOD_UNMATCHED_NAME_MARKER, beginUnmatched);
         // TODO better search algorithm for beginUnmatched and endUnmatched
         if ((beginUnmatched != -1) && (endUnmatched != -1) && (endUnmatched > beginUnmatched)) {
             tempUnmatched = beginUnmatched + VALUE_MARKER_METHOD_BEGIN.length()
                             + VALUE_METHOD_UNMATCHED_NAME.length();
 
             while ('\n' != code.charAt(tempUnmatched)) {
                 tempUnmatched++;
             }
 
             while ((tempUnmatched < endUnmatched)
                     && (Character.isWhitespace(code.charAt(tempUnmatched)))) {
                 tempUnmatched++;
             }
 
             if (tempUnmatched < endUnmatched) {
                 returnValue = true;
             } // no else
         }
         return returnValue;
     }
 }
