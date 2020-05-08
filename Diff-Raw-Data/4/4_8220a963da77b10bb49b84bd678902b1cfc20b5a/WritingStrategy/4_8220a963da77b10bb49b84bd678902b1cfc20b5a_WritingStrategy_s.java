 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
   EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 package org.easetech.easytest.codegen;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.easetech.easytest.loader.Loader;
 import org.easetech.easytest.loader.LoaderFactory;
 import org.easetech.easytest.loader.LoaderType;
 
 /**
 * An implementation of IWritingStrategy
 * responsible for persisting the  generated test cases, test data, converters to files 
 *
 * @author Ravi Polampelli
 *
 */
 
 public class WritingStrategy extends ConfigurableStrategy implements IWritingStrategy, JUnitDocletProperties {
 
     public static final String TESTSOURCE_INDENT_WIDTH         = "testsource.indent.width";
     public static final String TESTSOURCE_INDENT_WIDTH_DEFAULT = "2";
     
     private Map<String,StringBuffer> javaSourceFileMap = new HashMap<String,StringBuffer>();
 
     public void indent(StringBuffer sourceCode) {
         int indentLevel = 0;
         int indentWidth = 0;
         int beginIndex  = 0;
         int endIndex    = 0;
         int opening     = 0;
         int closing     = 0;
         int inserted    = 0;
         boolean openingFirst = false;
         boolean closingFirst = false;
 
         if (sourceCode != null) {
             indentWidth = getIndentWidth();
 
             while (endIndex<sourceCode.length()) {
                 switch (sourceCode.charAt(endIndex)) {
                     case '{':
                         opening++;
                         if (!closingFirst) {
                             openingFirst = true;
                         }
                         break;
                     case '}':
                         closing++;
                         if (!openingFirst) {
                             closingFirst = true;
                         }
                         break;
                     case '\n':
                         if (closing>opening) {
                             indentLevel = indentLevel-(closing-opening);
                         }
                         if (closing == opening) {
                             if (closingFirst) indentLevel--;
                         }
 
                         inserted = 0;
                         for (int i=0; (i<(indentLevel* indentWidth)); i++) {
                             if (sourceCode.charAt(beginIndex+i) != ' ') {
                                 sourceCode.insert(beginIndex+i, " ");
                                 inserted++;
                             }
                         }
                         endIndex += inserted;
 
                         if (closing == opening) {
                             if (closingFirst) indentLevel++;
                         }
                         if (opening>closing) {
                             indentLevel = indentLevel+(opening-closing);
                         }
                         beginIndex = endIndex+1;
                         opening = 0;
                         closing = 0;
                         openingFirst = false;
                         closingFirst = false;
                         break;
                     // no default
                 }
                 endIndex++;
             }
         }
     }
 
     /**
      * Merges generated source code with class file for given class name.
      * @return true if successfully merged or target file does not exist, false if class file contains no JUnitDoclet markers.
      */
     public StringBuffer loadClassSource(String root, String fullClassName) {
 
         StringBuffer   returnValue = null;
         File           file;
         BufferedReader bufferedReader;
         String         name;
         String         line;
 
         name = translateClassNameToFileName(fullClassName);
 
         try {
             file = new File(root, name);
 
             if (file.exists()) {
 
                 bufferedReader = new BufferedReader(new FileReader(file));
                 returnValue = new StringBuffer();
 
                 while (bufferedReader.ready()) {
                     line = bufferedReader.readLine();
                     returnValue.append(line);
                     returnValue.append("\n");
                 }
 
                 bufferedReader.close();
 
                 //noinspection UnusedAssignment
                 bufferedReader = null;
 
             } // no else
 
             //noinspection UnusedAssignment
             file = null;
         } catch (IOException ioe) {
             ioe.printStackTrace();
 
             throw new RuntimeException(ioe.toString());
         }
 
         return returnValue;
     }
     
     /**
      * Load actual source code from the path for given class name.
      * It first checks if file is available in cache(i.e. javaSourceFileMap)
      * @param String fullClassName
      * @param String path file path
      * @return StringBuffer if successfully loaded or null if file does not exist
      */
     public StringBuffer loadSourceClassSource(String fullClassName, String path) {
     	StringBuffer javaSourceFile = null;
     	
     	if(javaSourceFileMap.get(fullClassName)!=null){
     		javaSourceFile =  javaSourceFileMap.get(fullClassName);
     	} else {    	
 	    	
 	       
 	        File           file;
 	        BufferedReader bufferedReader;
 	        String         name;
 	        String         line;
 	
 	        name = translateClassNameToFileName(fullClassName);
 	
 	        try {
 	            file = new File(path,name);
 	
 	            if (file.exists()) {
 	
 	                bufferedReader = new BufferedReader(new FileReader(file));
 	                javaSourceFile = new StringBuffer();
 	
 	                while (bufferedReader.ready()) {
 	                    line = bufferedReader.readLine();
 	                    javaSourceFile.append(line);
 	                    javaSourceFile.append("\n");
 	                }
 	
 	                bufferedReader.close();
 	
 	                //noinspection UnusedAssignment
 	                bufferedReader = null;
 	                javaSourceFileMap.put(fullClassName, javaSourceFile);
 	
 	            } // no else
 	
 	            //noinspection UnusedAssignment
 	            file = null;
 	        } catch (IOException ioe) {
 	            ioe.printStackTrace();
 	
 	            throw new RuntimeException(ioe.toString());
 	        }
     	}
 
         return javaSourceFile;
     }
     
 
 
 
     private String translatePathToFilePath(String path) {
 
         String returnValue;
 
         returnValue = path.replace('.', '/');
         returnValue = returnValue.replace('\\', '/');
 
         return returnValue;
 	}
 
 	public void writeClassSource(String root, String fullClassName, StringBuffer sourceCode) {
 
         File           file;
         FileWriter     fileWriter;
         BufferedWriter bufferedWriter;
         String         name;
 
         name = translateClassNameToFileName(fullClassName);
 
         try {
             file = new File(root, name);
 
             file.getParentFile().mkdirs();
 
             fileWriter     = new FileWriter(file);
             bufferedWriter = new BufferedWriter(fileWriter);
 
             bufferedWriter.write(sourceCode.toString());
             bufferedWriter.flush();
             bufferedWriter.close();
 
             bufferedWriter = null;
             fileWriter     = null;
             file           = null;
         } catch (IOException ioe) {
             ioe.printStackTrace();
 
             throw new RuntimeException(ioe.toString());
         }
 
         if (fileWriter != null) {    // Any error while working with BufferedWriter ?
             try {
                 fileWriter.close();
 
                 fileWriter = null;
                 file       = null;
             } catch (IOException ioe) {
                 ioe.printStackTrace();
 
                 throw new RuntimeException(ioe.toString());
             }
         }
     }
 
     public String translateClassNameToFileName(String className) {
 
         String returnValue;
 
         returnValue = className.replace('.', '/') + ".java";
 
         return returnValue;
     }
 
     public boolean isExistingAndNewer(String dirInQuestion, String fullClassNameInQuestion,
                                       String dirReference, String fullClassNameReference) {
         boolean returnValue = false;
         File inQuestion;
         File reference;
 
         if ((dirInQuestion != null) && (fullClassNameInQuestion != null) &&
             (dirReference != null)  && (fullClassNameReference != null)) {
 
             inQuestion  = new File(dirInQuestion, translateClassNameToFileName(fullClassNameInQuestion));
             reference   = new File(dirReference,  translateClassNameToFileName(fullClassNameReference));
 
             returnValue = inQuestion.exists() && (inQuestion.lastModified()>reference.lastModified());
         }
         return returnValue;
     }
 
     public int getIndentWidth() {
         int        returnValue;
         Properties properties;
         String     stringValue;
 
         properties  = getProperties();
 
         if (properties != null) {
             stringValue = properties.getProperty(TESTSOURCE_INDENT_WIDTH, TESTSOURCE_INDENT_WIDTH_DEFAULT);
             returnValue = Integer.parseInt(stringValue);
         } else {
             returnValue = Integer.parseInt(TESTSOURCE_INDENT_WIDTH_DEFAULT);
         }
         return returnValue; 
     }
 
 	public void writeTestDataFile(String root, String fullClassName,
 			TestCaseVO testCaseVO,Properties seedData) {
 		File           file;
         FileOutputStream     fos; 
         String         name;
         Map<String, List<Map<String, Object>>> testData = testCaseVO.getTestData();
         
         //LoaderType loaderType;        
         Loader dataLoader = LoaderFactory.getLoader(LoaderType.EXCEL);
         name = translateClassNameToTestDataFileName(fullClassName,LoaderType.EXCEL);
         LOG.debug("WriteTestDataFile root:"+root+",name:"+name);
         root = root.replaceFirst("test/java", "test/resources");
         LOG.debug("seedData:"+seedData);
         
         Map<String,List<String>> seedDataValuesMap = null;
         
         if(seedData!=null){
         	seedDataValuesMap = loadSeedDataValues(seedData);
         }
         if(seedDataValuesMap!=null){
         	mergeSeedDataWithTestData(testData,seedDataValuesMap);
         }
         
         try {
             file = new File(root, name);
             
             if(file.exists()){
             	LOG.debug("file exists so loading old data:");
             	String overwrite = testCaseVO.getProperties().getProperty(OVERWRITE_EXISTING_TEST_DATA);
             	//merge the existing test data if value doesn't exist or value is 'NO'
             	if(overwrite == null || "NO".equalsIgnoreCase(overwrite)) {
             		mergeToExistingDataFile(testData,file,dataLoader);
             	}
             }
 
             file.getParentFile().mkdirs();
             
             fos     = new FileOutputStream(file);
             dataLoader.writeFullData(fos, testData);
             fos     = null;
             file    = null;
         } catch (IOException ioe) {
             ioe.printStackTrace();
 
             throw new RuntimeException(ioe.toString());
         }
 
         if (fos != null) {    // Any error while working with BufferedWriter ?
             try {
             	fos.close();
 
             	fos = null;
                 file       = null;
             } catch (IOException ioe) {
                 ioe.printStackTrace();
 
                 throw new RuntimeException(ioe.toString());
             }
         }
 
         checkMandatoryFields(testCaseVO,seedDataValuesMap);
         if(testCaseVO.getTestDataMissingFields().size()>0){
         	String logFileName = fullClassName.replace('.', File.separatorChar)+".log";
         	FileWriter     fileWriter;
             BufferedWriter bufferedWriter;
         	
         	try {
                 file = new File(root, logFileName);                
 
 
                 file.getParentFile().mkdirs();
                 
                 fileWriter     = new FileWriter(file);
                 bufferedWriter = new BufferedWriter(fileWriter);
                 StringBuffer writeLog = new StringBuffer();
                 writeLog.append("Test data is missing for below methods <method : [parameter]>\n");
                 writeLog.append("-------------------------------------------------------------\n");
                 for(String methodName:testCaseVO.getTestDataMissingFields().keySet()){
                 	writeLog.append(methodName + " : ");
                 	writeLog.append(testCaseVO.getTestDataMissingFields().get(methodName)+ "\n");
                 }
 
                 bufferedWriter.write(writeLog.toString());
                 bufferedWriter.flush();
                 bufferedWriter.close();
 
                 bufferedWriter = null;
                 fileWriter     = null;
                 file           = null;
             } catch (IOException ioe) {
                 ioe.printStackTrace();
 
                 throw new RuntimeException(ioe.toString());
             }
 
             if (fileWriter != null) {    // Any error while working with BufferedWriter ?
                 try {
                     fileWriter.close();
 
                     fileWriter = null;
                     file       = null;
                 } catch (IOException ioe) {
                     ioe.printStackTrace();
 
                     throw new RuntimeException(ioe.toString());
                 }
             }
         }
 
         
 		
 	}
 
 	private void checkMandatoryFields(TestCaseVO testCaseVO,Map<String,List<String>> seedDataValuesMap) {
 		//Map<String, List<Map<String, Object>>> testData = testCaseVO.getTestData();
 		Map<String,List<String>> mandatoryFields = testCaseVO.getTestDataMandatoryFields();
 		LOG.debug("checkMandatoryFields started: mandatoryFields:"+mandatoryFields);
 		LOG.debug("seedDataValuesMap:"+seedDataValuesMap);
 		Map<String,List<String>> missingFields = new HashMap<String,List<String>>();
         if(seedDataValuesMap != null && seedDataValuesMap.size() > 0){
 			for(String methodName:mandatoryFields.keySet()){
 				 
 				List<String> testMethodFields = mandatoryFields.get(methodName);
 				for(String paramName:testMethodFields){
 					boolean isParamExists = false;
 					for(String seedKey:seedDataValuesMap.keySet()){
 						if(paramName.equalsIgnoreCase(seedKey)){
 							isParamExists = true;						
 							break;
 						}
 					}
 					if(!isParamExists) {
 						if(!missingFields.containsKey(methodName)){
 							missingFields.put(methodName, new ArrayList<String>());
 						}
 						missingFields.get(methodName).add(paramName);
 					}
 	
 				}
 				
 			}
         } else {
         	missingFields.putAll(mandatoryFields);
         }
 		testCaseVO.setTestDataMissingFields(missingFields);
 		LOG.debug("checkMandatoryFields finished: count:"+mandatoryFields.size());
 		
 	}
 
 	private void mergeToExistingDataFile(
 			Map<String, List<Map<String, Object>>> testData, File file, Loader dataLoader) {
 		LOG.debug("mergeToExistingData() started:"+file.getAbsolutePath());
 		
 		Map<String, List<Map<String, Object>>> oldData = null;
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			oldData = dataLoader.loadFromInputStream(fis);
 			if(fis != null){
 				try {
 					fis.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				fis = null;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		LOG.debug("oldData loaded:"+oldData);
 		if(oldData!=null) mergeTestDataWithOldData(testData,oldData);
 		LOG.debug("mergeToExistingData() finished:"+testData);
 		
 	}
 
 	private void mergeTestDataWithOldData(Map<String, List<Map<String, Object>>> testData,
 			Map<String, List<Map<String, Object>>> oldData) {
 		LOG.debug("mergeTestDataWithOldData() started:");
 		
 		for(String oldTestMethod:oldData.keySet()){
 			LOG.debug("oldTestMethod() :"+oldTestMethod);
 			List<Map<String, Object>> oldTestMethodData =  oldData.get(oldTestMethod);
 			for(String newTestMethod:testData.keySet()){
 				LOG.debug("newTestMethod() :"+newTestMethod);
 				List<Map<String, Object>> newTestMethodData =  testData.get(newTestMethod);
 				if(newTestMethod.equals(oldTestMethod)){					
 					for(Map<String, Object> oldTestDataRow : oldTestMethodData){
 						boolean isSameTestDataRow = false;
 						LOG.debug("oldTestDataRow:"+oldTestDataRow);
 						for(Map<String, Object> newTestDataRow : newTestMethodData){
 							LOG.debug("newTestDataRow:"+newTestDataRow);
 							//LOG.debug("oldTestDataRow.equals(newTestDataRow):"+oldTestDataRow.equals(newTestDataRow));
 							//LOG.debug("oldTestDataRow.toString().equals(newTestDataRow.toString()):"+oldTestDataRow.toString().equals(newTestDataRow.toString()));
 							if(oldTestDataRow.equals(newTestDataRow) ||
 									oldTestDataRow.toString().equals(newTestDataRow.toString())){
 								LOG.debug("oldTestDataRow and newTestDataRow are same:");
 								isSameTestDataRow = true;
 								break;
 							}
 						}
 						if(!isSameTestDataRow){
 							LOG.debug("oldTestDataRow and newTestDataRow are not same:");
 							for(String oldParam:oldTestDataRow.keySet()){
 								boolean paramNameExists = false;
 								for(String newParam:newTestMethodData.get(0).keySet()){
 									if(oldParam.equals(newParam)){
 										paramNameExists = true;
 										break;
 									}
 								}
 								if(!paramNameExists){
 									oldTestDataRow.remove(oldParam);
 								}
 							}
 							LOG.debug("Adding oldTestDataRow to newTestMethodData:"+oldTestDataRow);
 							newTestMethodData.add(oldTestDataRow);
 						}
 					}
 					break;
 				}
 			}
 			
 		}
 		LOG.debug("mergeTestDataWithOldData() finsihed:");
 		
 	}
 
 	private void mergeSeedDataWithTestData(
 			Map<String, List<Map<String, Object>>> testData,
 			Map<String, List<String>> seedDataValuesMap) {
 		LOG.debug("mergeSeedDataWithTestData() started:");
 		for(List<Map<String, Object>> methodData:testData.values()){
 			int origMethodDataSize = methodData.size();
 			LOG.debug("origMethodDataSize:"+origMethodDataSize);
 			// Get the list of all parameters from first row
 			Map<String, Object> data = methodData.get(0); 
 			for(String paramName:data.keySet()){
 				for(String seedDataParamName:seedDataValuesMap.keySet()){
 					LOG.debug("paramName"+paramName);
 					LOG.debug("seedDataParamName"+seedDataParamName);
 					if(paramName.equalsIgnoreCase(seedDataParamName)){
 						//initializing value to original data index, since 
 						// we need start adding seed data from there.
 						int sdIndex = origMethodDataSize;
 						for(String seedDataParamValue:seedDataValuesMap.get(seedDataParamName)){
 							Map<String, Object> dataRow = null;
 							LOG.debug("seeddataIndex:"+sdIndex);
 							// if seed data size for a parameter is more than existing data list size
 							// then need to create new row of data.
 							if(sdIndex >= methodData.size()){
 								dataRow = new LinkedHashMap<String, Object>();								
 								methodData.add(dataRow);
 							} else { //take the existing row and add param value there
 								dataRow = methodData.get(sdIndex);								 
 							}
 							dataRow.put(paramName, seedDataParamValue);
 							sdIndex++;
 						}
 					}
 				}
 			}
 			
 		}
 		
 	}
 
 	private Map<String, List<String>> loadSeedDataValues(Properties seedData) {
 		LOG.debug("loadSeedDataValues() started:"+seedData);
 		Map<String,List<String>> seedDataValuesMap = new HashMap<String,List<String>>();
 		for(Object keyObj:seedData.keySet()){
 			String keyStr = (String)keyObj;
 			String[] keys = null;
 			if(keyStr.contains(",")){
 				keys = keyStr.split(",");
 			} else {
 				keys = new String[]{keyStr};
 			}
 			String valueStr = (String)seedData.get(keyObj);
 			String[] values = null;
 			if(valueStr.contains(",")){
 				values = valueStr.split(",");
 			} else {
 				values = new String[]{valueStr};
 			}
 			List<String> valueList = new ArrayList<String>();
 			for(String value:values) {
 				valueList.add(value);
 			}
 			for(String key:keys){
 				seedDataValuesMap.put(key, valueList);
 			}
 		}
 		LOG.debug("loadSeedDataValues() finished:"+seedDataValuesMap);
 		return seedDataValuesMap;
 	}
 
 	private String translateClassNameToTestDataFileName(String fullClassName, LoaderType loaderType) {
         String returnValue;
         String extension = ".xls";
         if(LoaderType.CSV.equals(loaderType)){
         	extension = ".csv";
         }else if(LoaderType.EXCEL.equals(loaderType)){
         	extension = ".xls";
         }else if(LoaderType.XML.equals(loaderType)){
         	extension = ".xml";
         }
         returnValue = fullClassName.replace('.', File.separatorChar) + extension;
 
         return returnValue;
 	}
 
 	public void writeConverterSources(String outputRoot, String packageName,
 			Map<String, StringBuffer> convertersMap, String overwriteConverters) {
 		
 		for(String fullClassName:convertersMap.keySet()) {
 			File           file;
 	        FileWriter     fileWriter;
 	        BufferedWriter bufferedWriter;
 	        String         name;
 	        LOG.debug("packageName+fullClassName:"+packageName+"."+fullClassName);
 	        name = translateClassNameToFileName(packageName+"."+fullClassName);
 	
 	        try {
 	            file = new File(outputRoot, name);
 	            
 	            if(file.exists() && "NO".equalsIgnoreCase(overwriteConverters)){
 	            	continue;
 	            }
 	
 	            file.getParentFile().mkdirs();
 	
 	            fileWriter     = new FileWriter(file);
 	            bufferedWriter = new BufferedWriter(fileWriter);
 	            StringBuffer sourceCode = convertersMap.get(fullClassName);
 	            indent(sourceCode);
 	
 	            bufferedWriter.write(sourceCode.toString());
 	            bufferedWriter.flush();
 	            bufferedWriter.close();
 	
 	            bufferedWriter = null;
 	            fileWriter     = null;
 	            file           = null;
 	        } catch (IOException ioe) {
 	            ioe.printStackTrace();
 	
 	            throw new RuntimeException(ioe.toString());
 	        }
 	
 	        if (fileWriter != null) {    // Any error while working with BufferedWriter ?
 	            try {
 	                fileWriter.close();
 	
 	                fileWriter = null;
 	                file       = null;
 	            } catch (IOException ioe) {
 	                ioe.printStackTrace();
 	
 	                throw new RuntimeException(ioe.toString());
 	            }
 	        }
 		}
 		
 	}
 }

