 package com.photon.phresco.framework.actions.applications;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.api.DynamicPageParameter;
 import com.photon.phresco.api.DynamicParameter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.ParameterModel;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.Utility;
 
 public class DynamicParameterAction extends FrameworkBaseAction implements Constants {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Quality.class);
     private static Boolean s_debugEnabled  =S_LOGGER.isDebugEnabled();
     
     //Dynamic parameter
     private List<Value> dependentValues = null; //Value for dependancy parameters
     private String dynamicPageParameterDesign = "";
     private String currentParamKey = ""; 
     private String goal = "";
     private String selectedOption = "";
     private String dependency = "";
     private String fileType = ""; //for file browse 
     private String fileOrFolder = ""; //for file browse
     	
 	private boolean paramaterAvailable;
 	private boolean errorFound;
 	private String phase = "";
 	private String errorMsg = "";
     
     private String availableParams = "";
     private final String PHASE_FUNCTIONAL_TEST_WEBDRIVER = "functional-test-webdriver";
     private final String PHASE_FUNCTIONAL_TEST_GRID = "functional-test-grid";
     private final String PHASE_FUNCTIONAL_UIAUTOMATION_TEST = "functional-test-UIAutomation";
     private final String PHASE_FUNCTIONAL_TEST = "functional-test";
     private final String PHASE_RUNAGAINST_SOURCE = "run-against-source";
     
     
     private static Map<String, PhrescoDynamicLoader> pdlMap = new HashMap<String, PhrescoDynamicLoader>();
     
    
 	/**
 	 * To the phresco plugin info file path based on the goal
 	 * @param goal
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	public String getPhrescoPluginInfoFilePath(String goal) throws PhrescoException {
 		StringBuilder sb = new StringBuilder(getApplicationHome());
 		sb.append(File.separator);
 		sb.append(FOLDER_DOT_PHRESCO);
 		sb.append(File.separator);
 		sb.append(PHRESCO_HYPEN);
 		if (PHASE_FUNCTIONAL_TEST_WEBDRIVER.equals(goal) || PHASE_FUNCTIONAL_TEST_GRID.equals(goal) || PHASE_FUNCTIONAL_UIAUTOMATION_TEST.equals(goal)) {
 			sb.append(PHASE_FUNCTIONAL_TEST);
 		} else if (PHASE_RUNGAINST_SRC_START.equals(goal)|| PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
 			sb.append(PHASE_RUNAGAINST_SOURCE);
 		} else {
 			sb.append(goal);
 		}
 		sb.append(INFO_XML);
 		return sb.toString();
 	}
     
 	public String getPhrescoPluginInfoXmlFilePath(String goal, ApplicationInfo appInfo) throws PhrescoException {
 		StringBuilder sb = new StringBuilder(Utility.getProjectHome());
 		sb.append(appInfo.getAppDirName());
 		sb.append(File.separator);
 		sb.append(FOLDER_DOT_PHRESCO);
 		sb.append(File.separator);
 		sb.append(PHRESCO_HYPEN);
 		if (PHASE_FUNCTIONAL_TEST_WEBDRIVER.equals(goal) || PHASE_FUNCTIONAL_TEST_GRID.equals(goal) || PHASE_FUNCTIONAL_UIAUTOMATION_TEST.equals(goal)) {
 			sb.append(PHASE_FUNCTIONAL_TEST);
 		}else if (PHASE_RUNGAINST_SRC_START.equals(goal)|| PHASE_RUNGAINST_SRC_STOP.equals(goal) ) {
 			sb.append(PHASE_RUNAGAINST_SOURCE);
 		} else {
 			sb.append(goal);
 		}
 		sb.append(INFO_XML);
 		return sb.toString();
 	}
 	
 	/**
      * To get list of parameters from phresco-plugin-info.xml
      * @param applicationInfo
      * @param goal
      * @return List<Parameter>
      * @throws PhrescoException
      */
     protected List<Parameter> getMojoParameters(MojoProcessor mojo, String goal) throws PhrescoException {
 		com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
 		if (mojoConfiguration != null) {
 		    return mojoConfiguration.getParameters().getParameter();
 		}
 		
 		return null;
 	}
     
     protected List<Parameter> getDynamicParameters(ApplicationInfo appInfo, String goal) throws PhrescoException {
         MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(goal)));
         com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
         List<Parameter> parameters = null;
         if (mojoConfiguration != null) {
             parameters = mojoConfiguration.getParameters().getParameter();
         }
         
         return parameters;
     }
     
     protected List<Parameter> getDynamicParameters(String goal) throws PhrescoException {
         MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(goal)));
         com.photon.phresco.plugins.model.Mojos.Mojo.Configuration mojoConfiguration = mojo.getConfiguration(goal);
         List<Parameter> parameters = null;
         if (mojoConfiguration != null) {
             parameters = mojoConfiguration.getParameters().getParameter();
         }
         
         return parameters;
     }
     
     /**
      * To set List of parameters in request
      * @param mojo TODO
      * @param appInfo
      * @param goal
      * @throws PhrescoException
      */
     protected void setPossibleValuesInReq(MojoProcessor mojo, ApplicationInfo appInfo, List<Parameter> parameters, 
     		Map<String, DependantParameters> watcherMap, String goal) throws PhrescoException {
         try {
             if (CollectionUtils.isNotEmpty(parameters)) {
                 StringBuilder paramBuilder = new StringBuilder();
                 for (Parameter parameter : parameters) {
                     String parameterKey = parameter.getKey();
                     if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && parameter.getDynamicParameter() != null) { 
                     	//Dynamic parameter
                         Map<String, Object> constructMapForDynVals = constructMapForDynVals(appInfo, watcherMap, parameterKey);
                         constructMapForDynVals.put(REQ_MOJO, mojo);
                         constructMapForDynVals.put(REQ_GOAL, goal);
                         // Get the values from the dynamic parameter class
                         List<Value> dynParamPossibleValues = getDynamicPossibleValues(constructMapForDynVals, parameter);
                         addValueDependToWatcher(watcherMap, parameterKey, dynParamPossibleValues, parameter.getValue());
                         if (watcherMap.containsKey(parameterKey)) {
                             DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
                             if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                             	if (StringUtils.isNotEmpty(parameter.getValue())) {
                                 	dependantParameters.setValue(parameter.getValue());
                                 } else {
                                 	dependantParameters.setValue(dynParamPossibleValues.get(0).getValue());
                                 }
                             }
                         }
                         
                         setReqAttribute(REQ_DYNAMIC_POSSIBLE_VALUES + parameter.getKey(), dynParamPossibleValues);
                         if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                         	if (StringUtils.isNotEmpty(parameter.getValue())) {
                         		addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                         	} else {
                         		addWatcher(watcherMap, parameter.getDependency(), parameterKey, dynParamPossibleValues.get(0).getValue());
                         	}
                         }
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         if (CollectionUtils.isNotEmpty(dynParamPossibleValues)) {
                             paramBuilder.append(dynParamPossibleValues.get(0).getValue());
                         }
                     } else if (parameter.getPossibleValues() != null) { //Possible values
                         List<Value> values = parameter.getPossibleValues().getValue();
                         
                         if (watcherMap.containsKey(parameterKey)) {
                             DependantParameters dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
                             if (StringUtils.isNotEmpty(parameter.getValue())) {
                             	dependantParameters.setValue(parameter.getValue());
                             } else {
                             	dependantParameters.setValue(values.get(0).getValue());
                             }
                         }
                         
                         addValueDependToWatcher(watcherMap, parameterKey, values, parameter.getValue());
                         if (CollectionUtils.isNotEmpty(values)) {
                         	if (StringUtils.isNotEmpty(parameter.getValue())) {
                             	addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                             } else {
                             	addWatcher(watcherMap, parameter.getDependency(), parameterKey, values.get(0).getKey());
                             }
                         }
                         
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         paramBuilder.append("");
                     } else if (parameter.getType().equalsIgnoreCase(TYPE_BOOLEAN) && StringUtils.isNotEmpty(parameter.getDependency())) {
                     	//Checkbox
                         addWatcher(watcherMap, parameter.getDependency(), parameterKey, parameter.getValue());
                         if (StringUtils.isNotEmpty(paramBuilder.toString())) {
                             paramBuilder.append("&");
                         }
                         paramBuilder.append(parameterKey);
                         paramBuilder.append("=");
                         paramBuilder.append("");
                     } else if(TYPE_DYNAMIC_PAGE_PARAMETER.equalsIgnoreCase(parameter.getType())) {
             			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
             			Map<String, Object> dynamicPageParameterMap = getDynamicPageParameter(appInfo, watcherMap, parameter);
             			List<? extends Object> dynamicPageParameter = (List<? extends Object>) dynamicPageParameterMap.get(REQ_VALUES_FROM_JSON);
             			String className = (String) dynamicPageParameterMap.get(REQ_CLASS_NAME);
             			setReqAttribute(REQ_CLASS_NAME, className);
             			setReqAttribute(REQ_DYNAMIC_PAGE_PARAMETER + parameter.getKey(), dynamicPageParameter);
             		}
                 }
                 setAvailableParams(paramBuilder.toString());
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
     }
 
 	private void addValueDependToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, List<Value> values, String previousValue) {
 		for (Value value : values) {
 		    if (StringUtils.isNotEmpty(value.getDependency())) {
 		    	if (StringUtils.isNotEmpty(previousValue)) {
 		    		addWatcher(watcherMap, value.getDependency(), parameterKey, previousValue);
 		    	} else {
 		    		addWatcher(watcherMap, value.getDependency(), parameterKey, value.getKey());
 		    	}
 		    }
 		}
 	}
 
     private void addWatcher(Map<String, DependantParameters> watcherMap, String dependency, String parameterKey, String parameterValue) {
         if (StringUtils.isNotEmpty(dependency)) {
             List<String> dependencyKeys = Arrays.asList(dependency.split(CSV_PATTERN));
             for (String dependentKey : dependencyKeys) {
             	DependantParameters dependantParameters;
                 if (watcherMap.containsKey(dependentKey)) {
                     dependantParameters = (DependantParameters) watcherMap.get(dependentKey);
                 } else {
                     dependantParameters = new DependantParameters();
                 }
                 dependantParameters.getParentMap().put(parameterKey, parameterValue);
                 watcherMap.put(dependentKey, dependantParameters);
             }
         }
        
         addParentToWatcher(watcherMap, parameterKey, parameterValue);
     }
     
     private void addParentToWatcher(Map<String, DependantParameters> watcherMap, String parameterKey, String parameterValue) {
     	
     	DependantParameters dependantParameters;
         if (watcherMap.containsKey(parameterKey)) {
             dependantParameters = (DependantParameters) watcherMap.get(parameterKey);
         } else {
             dependantParameters = new DependantParameters();
         }
     	dependantParameters.setValue(parameterValue);
     	watcherMap.put(parameterKey, dependantParameters);
     }
     
     protected Map<String, Object> constructMapForDynVals(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, String parameterKey) {
         Map<String, Object> paramMap = new HashMap<String, Object>(8);
         DependantParameters dependantParameters = watcherMap.get(parameterKey);
         if (dependantParameters != null) {
             paramMap.putAll(getDependantParameters(dependantParameters.getParentMap(), watcherMap));
         }
         paramMap.put(DynamicParameter.KEY_APP_INFO, appInfo);
         paramMap.put(REQ_CUSTOMER_ID, getCustomerId());
         if (StringUtils.isNotEmpty(getReqParameter(BUILD_NUMBER))) {
         	paramMap.put(DynamicParameter.KEY_BUILD_NO, getReqParameter(BUILD_NUMBER));
         }
         
         return paramMap;
     }
     
     private Map<String, Object> getDependantParameters(Map<String, String> parentMap, Map<String, DependantParameters> watcherMap) {
         Map<String, Object> paramMap = new HashMap<String, Object>(8);
         Set<String> keySet = parentMap.keySet();
         for (String key : keySet) {
             if (watcherMap.get(key) != null) {
                 String value = ((DependantParameters) watcherMap.get(key)).getValue();
                 paramMap.put(key, value);
             }
         }
         return paramMap;
     }
 
    /* protected void setDependencyToWatcher(Map<String, DependantParameters> watcherMap, List<String> dependencyKeys,  
             String parentParamKey, String parentParamValue) {
         for (String dependentKey : dependencyKeys) {
             DependantParameters dependantParameters;
             if (watcherMap.containsKey(dependentKey)) {
                 dependantParameters = (DependantParameters) watcherMap.get(dependentKey);
             } else {
                 dependantParameters = new DependantParameters();
             }
             dependantParameters.getParentMap().put(parentParamKey, parentParamValue);
             System.out.println("adding watcher " + dependentKey);
             watcherMap.put(dependentKey, dependantParameters);
         }
     }*/
 
     /**
      * To set List of Possible values as Dynamic parameter in request
      * @param watcherMap
      * @param parameter
      * @return
      * @throws PhrescoException
      */
     protected List<Value> getDynamicPossibleValues(Map<String, Object> watcherMap, Parameter parameter) throws PhrescoException {
         PossibleValues possibleValue = getDynamicValues(watcherMap, parameter);
         List<Value> possibleValues = (List<Value>) possibleValue.getValue();
         return possibleValues;
     }
 	
 	private PossibleValues getDynamicValues(Map<String, Object> watcherMap, Parameter parameter) throws PhrescoException {
 		try {
 			String className = parameter.getDynamicParameter().getClazz();
 			DynamicParameter dynamicParameter;
 			PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(getCustomerId());
 			if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
 				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
 			} else {
 				//To get repo info from Customer object
 				Customer customer = getServiceManager().getCustomer(getCustomerId());
 				RepoInfo repoInfo = customer.getRepoInfo();
 				//To set groupid,artfid,type infos to List<ArtifactGroup>
 				List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 				ArtifactGroup artifactGroup = new ArtifactGroup();
 				artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
 				artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
 				artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
 				//to set version
 				List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		        ArtifactInfo artifactInfo = new ArtifactInfo();
 		        artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
 				artifactInfos.add(artifactInfo);
 		        artifactGroup.setVersions(artifactInfos);
 				artifactGroups.add(artifactGroup);
 				
 				//dynamically loads specified Class
 				phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 				dynamicParameter = phrescoDynamicLoader.getDynamicParameter(className);
 				pdlMap.put(getCustomerId(), phrescoDynamicLoader);
 			}
 			
 			return dynamicParameter.getValues(watcherMap);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	protected Map<String, Object> getDynamicPageParameter(ApplicationInfo appInfo, Map<String, DependantParameters> watcherMap, Parameter parameter) throws PhrescoException {
 		String parameterKey = parameter.getKey();
 		Map<String, Object> paramsMap = constructMapForDynVals(appInfo, watcherMap, parameterKey);
 		String className = parameter.getDynamicParameter().getClazz();
 		DynamicPageParameter dynamicPageParameter;
 		PhrescoDynamicLoader phrescoDynamicLoader = pdlMap.get(getCustomerId());
 		if (MapUtils.isNotEmpty(pdlMap) && phrescoDynamicLoader != null) {
 			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
 		} else {
 			//To get repo info from Customer object
 			Customer customer = getServiceManager().getCustomer(getCustomerId());
 			RepoInfo repoInfo = customer.getRepoInfo();
 			//To set groupid,artfid,type infos to List<ArtifactGroup>
 			List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 			ArtifactGroup artifactGroup = new ArtifactGroup();
 			artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
 			artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
 			artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
 			//to set version
 			List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 	        ArtifactInfo artifactInfo = new ArtifactInfo();
 	        artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
 			artifactInfos.add(artifactInfo);
 	        artifactGroup.setVersions(artifactInfos);
 			artifactGroups.add(artifactGroup);
 			
 			//dynamically loads specified Class
 			phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 			dynamicPageParameter = phrescoDynamicLoader.getDynamicPageParameter(className);
 			pdlMap.put(getCustomerId(), phrescoDynamicLoader);
 		}
 		
 		return dynamicPageParameter.getObjects(paramsMap);
 	}
 	
 	/**
 	 * To persist entered values into phresco-plugin-info.xml
 	 * @throws IOException 
 	 */
 	protected void persistValuesToXml(MojoProcessor mojo, String goal) throws PhrescoException {
 	    try {
 	        List<Parameter> parameters = getMojoParameters(mojo, goal);
 	        if (CollectionUtils.isNotEmpty(parameters)) {
 	            for (Parameter parameter : parameters) {
 	                StringBuilder csParamVal = new StringBuilder();
 	                if (Boolean.parseBoolean(parameter.getMultiple())) {
 	                	if (getReqParameterValues(parameter.getKey()) == null) {
 	                		parameter.setValue("");
 	                	} else {
 	                		String[] parameterValues = getReqParameterValues(parameter.getKey());
 		                    for (String parameterValue : parameterValues) {
 		                        csParamVal.append(parameterValue);
 		                        csParamVal.append(",");
 		                    }
 		                    String csvVal = csParamVal.toString();
 		                    parameter.setValue(csvVal.toString().substring(0, csvVal.lastIndexOf(",")));
 	                	}
 	                } else if (TYPE_BOOLEAN.equalsIgnoreCase(parameter.getType())) {
 	                    if (getReqParameter(parameter.getKey()) != null) {
 	                        parameter.setValue(getReqParameter(parameter.getKey()));
 	                    } else {
 	                        parameter.setValue(Boolean.FALSE.toString());
 	                    }
 	                } else if (parameter.getType().equalsIgnoreCase(TYPE_MAP)) {
 	                    List<Child> childs = parameter.getChilds().getChild();
 	                    String[] keys = getReqParameterValues(childs.get(0).getKey());
 	                    String[] values = getReqParameterValues(childs.get(1).getKey());
 	                    Properties properties = new Properties();
 	                    for (int i = 0; i < keys.length; i++) {
 	                        properties.put(keys[i], values[i]);
 	                    }
 	                    StringWriter writer = new StringWriter();
 	                    properties.store(writer, "");
 	                    String value = writer.getBuffer().toString();
 	                    parameter.setValue(value);
 	                } else {
 	                    parameter.setValue(StringUtils.isNotEmpty(getReqParameter(parameter.getKey())) ? (String)getReqParameter(parameter.getKey()) : "");
 	                }
 	            }
 	        }
 	        mojo.save();
 	    } catch (IOException e) {
 	        throw new PhrescoException(e);
 	    }
 	}
 
 	public String mandatoryValidation() {
 		try {
 			File infoFile = new File(getPhrescoPluginInfoFilePath(getPhase()));
 			MojoProcessor mojo = new MojoProcessor(infoFile);
 			List<Parameter> parameters = getMojoParameters(mojo, getGoal());
 			List<String> eventDependencies = new ArrayList<String>();
 			List<String> dropDownDependencies = null;
 			Map<String, List<String>> validateMap = new HashMap<String, List<String>>();
 			if (CollectionUtils.isNotEmpty(parameters)) {
 				for (Parameter parameter : parameters) {
 					if (TYPE_BOOLEAN.equalsIgnoreCase(parameter.getType()) && StringUtils.isNotEmpty(parameter.getDependency())) {
 						//To validate check box dependency controls
 						eventDependencies = Arrays.asList(parameter.getDependency().split(CSV_PATTERN));
 						validateMap.put(parameter.getKey(), eventDependencies);//add checkbox dependency keys to map
 						if (getReqParameter(parameter.getKey()) != null && dependentParamMandatoryChk(mojo, eventDependencies)) {
 							break;//break from loop if error exists
 						}
 					} else if (TYPE_LIST.equalsIgnoreCase(parameter.getType()) &&  !Boolean.parseBoolean(parameter.getMultiple())
 							&& parameter.getPossibleValues() != null) {
 						//To validate (Parameter type - LIST) single select list box dependency controls
 						if (StringUtils.isNotEmpty(getReqParameter(parameter.getKey()))) {
 							List<Value> values = parameter.getPossibleValues().getValue();
 							String allPossibleValueDependencies = fetchAllPossibleValueDependencies(values);
 							eventDependencies = Arrays.asList(allPossibleValueDependencies.toString().split(CSV_PATTERN));
 							validateMap.put(parameter.getKey(), eventDependencies);//add psbl value dependency keys to map
 							for (Value value : values) {
 								dropDownDependencies = new ArrayList<String>();
 								if (value.getKey().equalsIgnoreCase(getReqParameter(parameter.getKey())) 
 										&& StringUtils.isNotEmpty(value.getDependency())) {
 									//get currently selected option's dependency keys to validate and break from loop
 									dropDownDependencies = Arrays.asList(value.getDependency().split(CSV_PATTERN));
 									break;
 								}
 							}
 							if (dependentParamMandatoryChk(mojo, dropDownDependencies)) {
 								//break from loop if error exists
 								break;
 							}
 						}
 					} else if (Boolean.parseBoolean(parameter.getRequired())) {
 						//comes here for other controls
 						boolean alreadyValidated = fetchAlreadyValidatedKeys(validateMap, parameter);
 						if ((parameter.isShow() || !alreadyValidated) && paramsMandatoryCheck(parameter)) {
 							break;
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		return SUCCESS;
 	}
 
 	/**
 	 * To get all dependency keys from map
 	 * @param validateMap
 	 * @param parameter
 	 * @param alreadyValidated
 	 * @return
 	 */
 	private boolean fetchAlreadyValidatedKeys(Map<String, List<String>> validateMap, Parameter parameter) {
 		boolean alreadyValidated = false;
 		Set<String> keySet = validateMap.keySet();
 		for (String key : keySet) {
 			List<String> valueList = validateMap.get(key);
 			if (valueList.contains(parameter.getKey())) {
 				alreadyValidated = true;
 			}
 		}
 		return alreadyValidated;
 	}
 
 	/**
 	 * To get all each possible value's dependency keys 
 	 * @param values
 	 * @param sb
 	 * @param sep
 	 */
 	private String fetchAllPossibleValueDependencies(List<Value> values) {
 		StringBuilder sb = new StringBuilder();
 		String sep = "";
 		for (Value value : values) {
 			if (StringUtils.isNotEmpty(value.getDependency())) {
 				sb.append(sep);
 				sb.append(value.getDependency());
 				sep = COMMA;
 			}
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * To validate currently selected option's dependency keys
 	 * @param mojo
 	 * @param eventDependencies
 	 */
 	private boolean dependentParamMandatoryChk(MojoProcessor mojo, List<String> eventDependencies) {
 		boolean flag = false;
 		if (CollectionUtils.isNotEmpty(eventDependencies)) {
 			for (String eventDependency : eventDependencies) {
 				Parameter dependencyParameter = mojo.getParameter(getGoal(), eventDependency);
 				if (Boolean.parseBoolean(dependencyParameter.getRequired()) && paramsMandatoryCheck(dependencyParameter)) {
 					flag = true;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	
 	private boolean paramsMandatoryCheck (Parameter parameter) {
 		boolean returnFlag = false; 
 		String lableTxt =  getParameterLabel(parameter);
 		if (TYPE_STRING.equalsIgnoreCase(parameter.getType()) || TYPE_NUMBER.equalsIgnoreCase(parameter.getType())
 				|| TYPE_PASSWORD.equalsIgnoreCase(parameter.getType())
 				|| TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple())
 				|| (TYPE_LIST.equalsIgnoreCase(parameter.getType()) && !Boolean.parseBoolean(parameter.getMultiple()))
 				|| (TYPE_FILE_BROWSE.equalsIgnoreCase(parameter.getType()))) {
 			if (FROM_PAGE_EDIT.equalsIgnoreCase(parameter.getEditable())) {//For editable combo box
 				returnFlag = editableComboValidate(parameter, returnFlag,lableTxt);
 			} else {//for text box,non editable single select list box,file browse
 				returnFlag = textSingleSelectValidate(parameter, returnFlag,lableTxt);
 			}
 		} else if (TYPE_DYNAMIC_PARAMETER.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()) || 
 				(TYPE_LIST.equalsIgnoreCase(parameter.getType()) && Boolean.parseBoolean(parameter.getMultiple()))) {
 			
 			returnFlag = multiSelectValidate(parameter, returnFlag, lableTxt);//for multi select list box
 		} else if (parameter.getType().equalsIgnoreCase(TYPE_MAP)) {
 			returnFlag = mapControlValidate(parameter, returnFlag);//for type map
 		}
 		
 		return returnFlag;
 	}
 
 	/**
 	 * To validate key value pair control
 	 * @param parameter
 	 * @param returnFlag
 	 * @return
 	 */
 	private boolean mapControlValidate(Parameter parameter, boolean returnFlag) {
 		List<Child> childs = parameter.getChilds().getChild();
 		String[] keys = getReqParameterValues(childs.get(0).getKey());
 		String[] values = getReqParameterValues(childs.get(1).getKey());
 		String childLabel = "";
 		for (int i = 0; i < keys.length; i++) {
 			if (StringUtils.isEmpty(keys[i]) && Boolean.parseBoolean(childs.get(0).getRequired())) {
 				childLabel = childs.get(0).getName().getValue().getValue();
 				setErrorFound(true);
 				setErrorMsg(childLabel + " " +getText(EXCEPTION_MANDAOTRY_MSG));
 				returnFlag = true;
 				break;
 			} else if (StringUtils.isEmpty(values[i]) && Boolean.parseBoolean(childs.get(1).getRequired())) {
 				childLabel = childs.get(1).getName().getValue().getValue();
 				setErrorFound(true);
 				setErrorMsg(childLabel + " " +getText(EXCEPTION_MANDAOTRY_MSG));
 				returnFlag = true;
 				break;
 			}
 		}
 		return returnFlag;
 	}
 
 	/**
 	 * To validate multi select list box
 	 * @param parameter
 	 * @param returnFlag
 	 * @param lableTxt
 	 * @return
 	 */
 	private boolean multiSelectValidate(Parameter parameter,
 			boolean returnFlag, String lableTxt) {
 		if (getReqParameterValues(parameter.getKey()) == null) {//for multi select list box
 			setErrorFound(true);
 			setErrorMsg(lableTxt + " " +getText(EXCEPTION_MANDAOTRY_MSG));
 			returnFlag = true;
 		}
 		
 		return returnFlag;
 	}
 
 	/**
 	 * To validate text box and single select list box
 	 * @param parameter
 	 * @param returnFlag
 	 * @param lableTxt
 	 * @return
 	 */
 	private boolean textSingleSelectValidate(Parameter parameter,
 			boolean returnFlag, String lableTxt) {
 		if (StringUtils.isEmpty(getReqParameter(parameter.getKey()))) {
 			setErrorFound(true);
 			setErrorMsg(lableTxt + " " +getText(EXCEPTION_MANDAOTRY_MSG));
 			returnFlag = true;
 		}
 		
 		return returnFlag;
 	}
 	
 	private boolean editableComboValidate(Parameter parameter,
 			boolean returnFlag, String lableTxt) {
 		if (StringUtils.isEmpty(getReqParameter(parameter.getKey())) || 
 				"Type or select from the list".equalsIgnoreCase(getReqParameter(parameter.getKey()))) {
 			setErrorFound(true);
 			setErrorMsg(lableTxt + " " +getText(EXCEPTION_MANDAOTRY_MSG));
 			returnFlag = true;
 		}
 		
 		return returnFlag;
 	}
 	
 	/**
 	 * To get parameter's label
 	 * @param parameter
 	 * @param lableTxt
 	 * @return
 	 */
 	private String getParameterLabel(Parameter parameter) {
 		String lableTxt = "";
 		List<com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Name.Value> labels = parameter.getName().getValue();
 		for (com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Name.Value label : labels) {
 			if (label.getLang().equals("en")) {	//to get label of parameter
 				lableTxt = label.getValue();
 			    break;
 			}
 		}
 		return lableTxt;
 	}
 	
 	/**
 	 * To get list of maven command arguments
 	 * @param parameters
 	 * @return
 	 */
 	protected List<String> getMavenArgCommands(List<Parameter> parameters) {
		List<String> buildArgCmds = new ArrayList<String>();			
 		for (Parameter parameter : parameters) {
 			if (parameter.getPluginParameter()!= null && PLUGIN_PARAMETER_FRAMEWORK.equalsIgnoreCase(parameter.getPluginParameter())) {
 				List<MavenCommand> mavenCommand = parameter.getMavenCommands().getMavenCommand();
 				for (MavenCommand mavenCmd : mavenCommand) {
 					if (StringUtils.isNotEmpty(parameter.getValue()) && parameter.getValue().equalsIgnoreCase(mavenCmd.getKey())) {
 						buildArgCmds.add(mavenCmd.getValue());
 					}
 				}
 			}
 		}
 		return buildArgCmds;
 	}
 	
 	public String changeEveDependancyListener() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.changeEveDependancyListener()");
         }
         
         Map<String, DependantParameters> watcherMap = (Map<String, DependantParameters>) 
                 getSessionAttribute(getAppId() + getGoal() + SESSION_WATCHER_MAP);
         DependantParameters currentParameters = watcherMap.get(getCurrentParamKey());
         if (currentParameters == null) {
             currentParameters = new DependantParameters();
         }
         currentParameters.setValue(getSelectedOption());
         watcherMap.put(getCurrentParamKey(), currentParameters);
 
         return SUCCESS;
     }
     
     public String updateDependancy() throws IOException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Quality.updateDependancy()");
         }
         
         try {
             ApplicationInfo applicationInfo = getApplicationInfo();
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(getGoal())));
             Map<String, DependantParameters> watcherMap = (Map<String, DependantParameters>) getSessionAttribute(getAppId() + getGoal() + SESSION_WATCHER_MAP);
             if (StringUtils.isNotEmpty(getDependency())) {
                 // Get the values from the dynamic parameter class
                 Parameter dependentParameter = mojo.getParameter(getGoal(), getDependency());
                 if (dependentParameter.getDynamicParameter() != null) {
                 	ApplicationInfo applicationInfo2 = getApplicationInfo();
                     Map<String, Object> constructMapForDynVals = constructMapForDynVals(applicationInfo, watcherMap, getDependency());
                     if (TYPE_DYNAMIC_PAGE_PARAMETER.equalsIgnoreCase(dependentParameter.getType())) {
             			Map<String, Object> dynamicPageParameterMap = getDynamicPageParameter(applicationInfo2, watcherMap, dependentParameter);
             			List<? extends Object> dynamicPageParameter = (List<? extends Object>) dynamicPageParameterMap.get(REQ_VALUES_FROM_JSON);
             			String className = (String) dynamicPageParameterMap.get(REQ_CLASS_NAME);
                     	FrameworkUtil frameworkUtil = new FrameworkUtil();
                     	ParameterModel parameterModel = new ParameterModel();
                     	parameterModel.setName(dependentParameter.getKey());
                     	parameterModel.setShow(true);
                     	StringTemplate constructDynamicTemplate = frameworkUtil.constructDynamicTemplate(getCustomerId(), dependentParameter, 
                     			parameterModel, dynamicPageParameter, className);
                     	setDynamicPageParameterDesign(constructDynamicTemplate.toString());
                     } else {
                     	constructMapForDynVals.put(REQ_MOJO, mojo);
                         constructMapForDynVals.put(REQ_GOAL, getGoal());
 	                    List<Value> dependentPossibleValues = getDynamicPossibleValues(constructMapForDynVals, dependentParameter);
 	                    setDependentValues(dependentPossibleValues);
                     	if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMap.containsKey(getDependency())) {
 	                        DependantParameters dependantParameters = (DependantParameters) watcherMap.get(getDependency());
 	                        dependantParameters.setValue(dependentPossibleValues.get(0).getValue());
 	                    } else {
 	                    	 DependantParameters dependantParameters = (DependantParameters) watcherMap.get(getDependency());
 		                     dependantParameters.setValue("");
 	                    }
 	                    if (CollectionUtils.isNotEmpty(dependentPossibleValues) && watcherMap.containsKey(dependentPossibleValues.get(0).getDependency())) {
 	                        addValueDependToWatcher(watcherMap, dependentParameter.getKey(), dependentPossibleValues, "");
 	                        if (CollectionUtils.isNotEmpty(dependentPossibleValues)) {
 	                        	addWatcher(watcherMap, dependentParameter.getDependency(), 
 	                        			dependentParameter.getKey(), dependentPossibleValues.get(0).getValue());
 	                        }
 	                    }
                     }   
                 }
             }
             return SUCCESS;
         } catch (PhrescoException e) {
             S_LOGGER.error("Entered into catch block of Quality.updateDependancy()"+ e);
             return showErrorPopup(e, getText(EXCEPTION_QUALITY_FUNCTIONAL_RUN));
         }
     }
     
     public String validateDynamicParam() {
         try {
         	File infoFile = new File(getPhrescoPluginInfoFilePath(getGoal()));
         	if (!infoFile.exists()) {
         		setParamaterAvailable(false);
                 return SUCCESS;
         	} else if (getGoal().equals(PHASE_UNIT_TEST)) {
         	    List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
         	    if (CollectionUtils.isNotEmpty(projectModules)) {
         	        setParamaterAvailable(true);
         	        return SUCCESS;
         	    }
         	}
         	
             MojoProcessor mojo = new MojoProcessor(infoFile);
             List<Parameter> parameters = getMojoParameters(mojo, getGoal());
             if (CollectionUtils.isEmpty(parameters)) {
                 setParamaterAvailable(false);
                 return SUCCESS;
             }
             
             boolean hasShow = false;
             for (Parameter parameter : parameters) {
                 if (parameter.isShow()) {
                     hasShow = true;
                     setParamaterAvailable(true);
                     return SUCCESS;
                 }
             }
             if (!hasShow) {
                 Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
                 setPossibleValuesInReq(mojo, getApplicationInfo(), parameters, watcherMap, getGoal());
                 if (watcherMap != null && !watcherMap.isEmpty()) {
                     setParamaterAvailable(true);
                 }
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return SUCCESS;
     }
     
     public String openBrowseFileTree() throws PhrescoException {
 		setReqAttribute(FILE_TYPES, getFileType());
 		setReqAttribute(FILE_BROWSE, getFileOrFolder());
 		ApplicationInfo applicationInfo = getApplicationInfo();
 		if (REQ_JAR.equalsIgnoreCase(getFileType())) {
 			setReqAttribute(REQ_PROJECT_LOCATION, "");
 			setReqAttribute(REQ_FROM, "funcTestAgaistJar");
 		} else {
 			setReqAttribute(REQ_PROJECT_LOCATION, getAppDirectoryPath(applicationInfo).replace(File.separator, FORWARD_SLASH));
 		}
 		
 		return SUCCESS;
 	}
     
     public List<Value> getDependentValues() {
         return dependentValues;
     }
 
     public void setDependentValues(List<Value> dependentValues) {
         this.dependentValues = dependentValues;
     }
     
     public String getCurrentParamKey() {
         return currentParamKey;
     }
 
     public void setCurrentParamKey(String currentParamKey) {
         this.currentParamKey = currentParamKey;
     }
 
     public String getGoal() {
         return goal;
     }
 
     public void setGoal(String goal) {
         this.goal = goal;
     }
     
     public String getSelectedOption() {
         return selectedOption;
     }
 
     public void setSelectedOption(String selectedOption) {
         this.selectedOption = selectedOption;
     }
 
     public String getDependency() {
         return dependency;
     }
 
     public void setDependency(String dependency) {
         this.dependency = dependency;
     }
     
     public boolean isParamaterAvailable() {
         return paramaterAvailable;
     }
 
     public void setParamaterAvailable(boolean paramaterAvailable) {
         this.paramaterAvailable = paramaterAvailable;
     }
 
     public String getAvailableParams() {
         return availableParams;
     }
 
     public void setAvailableParams(String availableParams) {
         this.availableParams = availableParams;
     }
     
     public String getFileType() {
 		return fileType;
 	}
 
 	public void setFileType(String fileType) {
 		this.fileType = fileType;
 	}
 
 	public String getFileOrFolder() {
 		return fileOrFolder;
 	}
 
 	public void setFileOrFolder(String fileOrFolder) {
 		this.fileOrFolder = fileOrFolder;
 	}
 
 	public void setDynamicPageParameterDesign(String dynamicPageParameterDesign) {
 		this.dynamicPageParameterDesign = dynamicPageParameterDesign;
 	}
 
 	public String getDynamicPageParameterDesign() {
 		return dynamicPageParameterDesign;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public void setErrorMsg(String errorMsg) {
 		this.errorMsg = errorMsg;
 	}
 
 	public String getErrorMsg() {
 		return errorMsg;
 	}
 
 	public void setPhase(String phase) {
 		this.phase = phase;
 	}
 
 	public String getPhase() {
 		return phase;
 	}
 }
