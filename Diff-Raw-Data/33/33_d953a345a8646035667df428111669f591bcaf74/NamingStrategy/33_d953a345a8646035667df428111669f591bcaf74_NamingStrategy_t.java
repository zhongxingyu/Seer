 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
 	EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 /**
 * An implementation of INamingStrategy
 *
 * @author Ravi Polampelli
 *
 */
 
 public class NamingStrategy extends ConfigurableStrategy implements INamingStrategy, JUnitDocletProperties {
 
     //
    private String TEST_CASE_EXT         = "Test";  

	private String TEST_SUITE_EXT        = "Suite";
   

	private static final String TEST_METHOD_PREFIX    = "test";
 
     private String cachedDefaultTestSuiteName = null;
     private String subPackage = null;
    
    public String getTEST_CASE_EXT() {
		return TEST_CASE_EXT;
	}

	public void setTEST_CASE_EXT(String tEST_CASE_EXT) {
		TEST_CASE_EXT = tEST_CASE_EXT;
	}
    
	public String getTEST_SUITE_EXT() {
		return TEST_SUITE_EXT;
	}

	public void setTEST_SUITE_EXT(String tEST_SUITE_EXT) {
		TEST_SUITE_EXT = tEST_SUITE_EXT;
	}
	
     public boolean isTestInTest() {
         return isTestInTest;
     }
 
     public void setTestInTest(boolean testInTest) {
         isTestInTest = testInTest;
     }
 
     private boolean isTestInTest = false;
 
     public void init() {
         super.init();
         cachedDefaultTestSuiteName = null;
         setSubPackage(null);
         setTestInTest(false);
     }
 
     public String getSubPackage() {
         return subPackage;
     }
 
     public void setSubPackage(String subPackage) {
         this.subPackage = subPackage;
     }
 
     public boolean isTestPackageName(String packageName) {
         boolean returnValue = false;
         String  subName;
 
         subName = getSubPackage();
         if (subName != null) {
             returnValue = (packageName != null);
             returnValue = returnValue && (packageName.endsWith(subName));
         }
         return returnValue;
     }
 
     public boolean isTestClassName(String fullClassName) {
         boolean returnValue = true;
         String  subName;
 
         subName = getSubPackage();
 
         if (fullClassName != null) {
             // check option -subpackage
             if (subName != null) {
                 // in general all classes in subpackage are considered to be tests
                 returnValue &= isTestClassNameInSubpackage(fullClassName, subName);
                 // is not considered a test with option -testintest and name does not sound like one
                 returnValue &= !isTestInTest() || endsLikeTestClassName(fullClassName);
             } else {
                 // check class name ending
                 returnValue &= endsLikeTestClassName(fullClassName);
             }
         } else {
             returnValue = false;
         }
 
         return returnValue;
     }
 
     private boolean isTestClassNameInSubpackage(String fullClassName, String subName) {
         return ((fullClassName.indexOf("." + subName + ".") != -1) || fullClassName.startsWith(subName + "."));
     }
 
     private boolean endsLikeTestClassName(String fullClassName) {
         return ((fullClassName.endsWith(TEST_SUITE_EXT)) || (fullClassName.endsWith(TEST_CASE_EXT)));
     }
 
     public String stripParentPackage(String className) {
 
         String returnValue = null;
         int    index;
         char   ch;
 
         index = className.lastIndexOf(".");
 
         if (index > 0) {
             returnValue = className.substring(index + 1);
         } else {
             returnValue = className;
         }
 
         return returnValue;
     }
 
     public String getTestCaseName(String fullClassName) {
 
         String returnValue     = null;
 
         returnValue = stripParentPackage(fullClassName);
         returnValue = returnValue + TEST_CASE_EXT;
 
         return returnValue;
     }
 
     public String getTestSuiteName(String packageName) {
 
         String returnValue     = null;
 
         if (cachedDefaultTestSuiteName == null) {
             cachedDefaultTestSuiteName = (String) getProperties().getProperty(TESTSUITE_CLASS_NAME, "");
         }
 
         if (cachedDefaultTestSuiteName.length()>0) {
             returnValue = cachedDefaultTestSuiteName;
         } else {
             returnValue = stripParentPackage(packageName);
             returnValue = StringHelper.firstToUpper(returnValue);
             returnValue = returnValue + TEST_SUITE_EXT;
         }
         return returnValue;
     }
 
     public String getPackageName(String fullClassName) {
 
         String returnValue = null;
         int    index;
 
         index = fullClassName.lastIndexOf(".");
 
         if (index > 0) {
             returnValue = fullClassName.substring(0, index);
         } else {
             returnValue = "";
         }
 
         return returnValue;
     }
 
     public String getTestPackageName(String packageName) {
 
         String returnValue = null;
 
         if (getSubPackage() != null) {
             returnValue = packageName+"."+getSubPackage();
         } else {
             returnValue = packageName;
         }
 
         return returnValue;
     }
 
     public String getFullTestCaseName(String fullClassName) {
         String testPackageName;
         String testCaseName;
 
         testPackageName = getTestPackageName(getPackageName(fullClassName));
         testCaseName    = getTestCaseName(stripParentPackage(fullClassName));
         return combinePackageClass(testPackageName, testCaseName);
     }
 
     public String getFullTestSuiteName(String packageName) {
         String testPackageName;
         String testSuiteName;
 
         testPackageName = getTestPackageName(packageName);
         testSuiteName   = getTestSuiteName(packageName);
         return combinePackageClass(testPackageName, testSuiteName);
     }
 
     public String combinePackageClass(String packageName, String className) {
         String returnValue;
 
         if ((packageName != null) && (packageName.length()>0)) {
             returnValue= packageName+"."+className;
         } else {
             returnValue = className;
         }
 
         return returnValue;
 
     }
 
     public String getTestMethodName(String methodName) {
 
         StringBuffer sb;
         String       first;
 
         sb    = new StringBuffer(methodName);
         first = sb.substring(0, 1);
 
         sb.replace(0, 1, first.toUpperCase());
         sb.insert(0, TEST_METHOD_PREFIX);
 
         return sb.toString();
     }
 
     public String getTestAccessorName(String prefixSet, String prefixGet, String accessorName) {
         StringBuffer sb;
         String       first;
 
         sb    = new StringBuffer(accessorName);
         first = sb.substring(0, 1);
         sb.replace(0, 1, first.toUpperCase());
 
         // insert "get" or "is" as "Get" or "Is"
         sb.insert(0, prefixGet);
         first = sb.substring(0, 1);
         sb.replace(0, 1, first.toUpperCase());
 
         // insert "set" as "Set"
         sb.insert(0, prefixSet);
         first = sb.substring(0, 1);
         sb.replace(0, 1, first.toUpperCase());
 
         sb.insert(0, TEST_METHOD_PREFIX);
 
         return sb.toString();
     }
 
 }
 
