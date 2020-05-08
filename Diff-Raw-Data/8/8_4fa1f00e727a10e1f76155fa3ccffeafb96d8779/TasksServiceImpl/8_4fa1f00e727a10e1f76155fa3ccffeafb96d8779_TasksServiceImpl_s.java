 package org.imirsel.nema.webapp.webflow;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.Map.Entry;
 
 import javax.jcr.SimpleCredentials;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.annotations.parser.beans.DataTypeBean;
 import org.imirsel.nema.contentrepository.client.ArtifactService;
 import org.imirsel.nema.contentrepository.client.ContentRepositoryServiceException;
 import org.imirsel.nema.dao.ExternalMirexSubmissionDao;
 import org.imirsel.nema.flowservice.FlowService;
 import org.imirsel.nema.flowservice.MeandreServerException;
 import org.imirsel.nema.model.Component;
 import org.imirsel.nema.model.ExecutableBundle;
 import org.imirsel.nema.model.ExecutableMetadata;
 import org.imirsel.nema.model.Flow;
 import org.imirsel.nema.model.InvalidResourcePathException;
 import org.imirsel.nema.model.Job;
 import org.imirsel.nema.model.MirexSubmission;
 import org.imirsel.nema.model.Property;
 import org.imirsel.nema.model.RepositoryResourcePath;
 import org.imirsel.nema.model.ResourcePath;
 import org.imirsel.nema.model.Role;
 import org.imirsel.nema.model.User;
 import org.imirsel.nema.service.UserManager;
 import org.imirsel.nema.webapp.model.JobForm;
 import org.imirsel.nema.webapp.model.UploadedExecutableBundle;
 import org.imirsel.nema.webapp.service.JcrService;
 import org.imirsel.nema.webapp.service.JiniService;
 import org.springframework.binding.message.MessageBuilder;
 import org.springframework.binding.message.MessageContext;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.webflow.context.ExternalContext;
 import org.springframework.webflow.core.collection.ParameterMap;
 
 /**
  * Action class for the task template flow generation webflow.
  * 
  * @author gzhu1
  * @since 0.6.0
  * 
  */
 public class TasksServiceImpl {
 
 	static private Log logger = LogFactory.getLog(TasksServiceImpl.class);
 
 	private FlowService flowService;
 	private UserManager userManager;
 	private ArtifactService artifactService;
 	private JcrService jcrService;
 	private JiniService jiniService;
 	
 
 	private String uploadDirectory;
 	private String physicalDir;
 	private String webDir;
 	private ExternalMirexSubmissionDao mirexSubmissionDao;
 
 	// Component properties that should be hidden.
 	final static String REMOTE_COMPONENT = "_remoteDynamicComponent";
 	final static String CREDENTIALS = "_credentials";
 	final static String EXECUTABLE_URL = "profileName";
 	final static String OS = "_os";
 	//final static String GROUP = "_group";
 	 final static String MIREX_SUBMISSION_CODE="_submissionCode";
 	 final static String MIREX_SUBMISSION_NAME="_submissionName";
 	 final static String LOOKUP_HOST="_lookupHost";
 	 final static String CONTENT_REPOSITORY_URI="_contentRepositoryUri";
 	 final static String NEMA_USER="_user";
 
 	final static Set<String> HIDDEN_PROPERTIES = new HashSet<String>();
 	{
 		HIDDEN_PROPERTIES.add(REMOTE_COMPONENT);
 		HIDDEN_PROPERTIES.add(EXECUTABLE_URL);
 		HIDDEN_PROPERTIES.add(OS);
 		HIDDEN_PROPERTIES.add(CREDENTIALS);
 	}
 
 	public String getPhysicalDir() {
 		return physicalDir;
 	}
 
 	public String getWebDir() {
 		return webDir;
 	}
 
 	/**
 	 * Send executable bundle to content repository, replace/add the new
 	 * ResourcePath of executable bundle in the the executableMap.
 	 * 
 	 * @param component
 	 *            Remote dynamic component.
 	 * @param properties
 	 *            Properties of the component. Values are modified in the
 	 *            method.
 	 * @param bundle
 	 *            Bundle to send over to content repository
 	 * @param uuid
 	 *            Unique ID for the webflow
 	 * @param executableMap
 	 *            Note that this map is going to be modified, old path is
 	 *            replaced by new path.
 	 * @throws ContentRepositoryServiceException
 	 */
 	public void addExecutable(final Component component,
 			final List<Property> properties,
 			final UploadedExecutableBundle bundle, final UUID uuid,
 			Map<Component, ResourcePath> executableMap,
 			MessageContext messageContext)
 			throws ContentRepositoryServiceException {
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		Property os=findProperty(properties, OS);
 		if (os!=null) { os.setValue(bundle.getPreferredOs());}
 		deleteExecutableFromRepository(executableMap.get(component), credential);
 		ResourcePath path = artifactService.saveExecutableBundle(credential,
 				uuid.toString(), bundle);
 		executableMap.put(component, path);
 		String uri=path.getURIAsString();
 		findProperty(properties, EXECUTABLE_URL).setValue(uri);
 		if (path != null) {
 			// MessageContext messageContext=requestContext.getMessageContext();
 			messageContext.addMessage(new MessageBuilder().info().defaultText(
 					"Executable profile was successfully saved.").build());
 		} else {
 			throw new ContentRepositoryServiceException(
 
 			"An error occurred while saving the executable profile: "
 					+ bundle.getFileName());
 		}
 
 	}
 
 	/**
 	 * Remove the executable bundle from the content repository.
 	 * 
 	 * @param component
 	 *            Remote dynamic component
 	 * @param executableMap
 	 *            Maps with all existing executableBundle's {@link ResourcePath}
 	 *            s. Values are modified in the method.
 	 * @param properties
 	 *            Properties of remote dynamic component. Values are modified in
 	 *            the method.
 	 */
 	public void removeExecutable(Component component,
 			Map<Component, ResourcePath> executableMap,
 			List<Property> properties) throws ContentRepositoryServiceException {
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		if (executableMap.containsKey(component)) {
 			ResourcePath oldPath = executableMap.get(component);
 			deleteExecutableFromRepository(oldPath, credential);
 			executableMap.remove(component);
 			findProperty(properties, EXECUTABLE_URL).setValue("");
 		}
 	}
 
 	/**
 	 * clear all executable bundles sent over to content repository
 	 * 
 	 * @param executableMap
 	 *            Maps with all existing executableBundle's {@link ResourcePath}
 	 */
 	public void clearBundles(Map<Component, ResourcePath> executableMap)
 			throws ContentRepositoryServiceException {
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		for (Component component : executableMap.keySet()) {
 			ResourcePath oldPath = executableMap.get(component);
 			deleteExecutableFromRepository(oldPath, credential);
 		}
 	}
 
 	private void deleteExecutableFromRepository(ResourcePath path,
 			SimpleCredentials credential)
 			throws ContentRepositoryServiceException {
 		logger.info("Calling deleteExecutableFromRepository -commmenting it out. This will impact clear bundle function");
 		try {
 			if ((path != null) && (artifactService.exists(credential, path))) {
 				logger.info("remove from content repository executable bundle:"
 						+ path);
 				artifactService.removeExecutableBundle(credential, path);
 			}
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 			throw (e);
 		}
 		
 
 	}
 
 	/**
 	 * Retrieve the Executable bundle with resource path {@link ResourcePath},
 	 * and populated the extra fields for UploadedExecutableBundle
 	 * 
 	 * @param path
 	 *            {@link ResourcePath} to the executable bundle
 	 * @param properties
 	 *            Properties of the remote dynamic component, necessary to build
 	 *            {@link UploadedExecutableBundle} object because it has extra
 	 *            info from {@linkplain properties}
 	 * @return
 	 */
 	public UploadedExecutableBundle findBundle(ResourcePath path,
 			List<Property> properties) {
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		UploadedExecutableBundle bundle = null;
 		try {
 			if ((path != null) && (artifactService.exists(credential, path))) {
 				ExecutableBundle oldBundle = artifactService
 						.getExecutableBundle(credential, path);
 				bundle = new UploadedExecutableBundle(oldBundle);
 				if (bundle == null)
 					bundle = new UploadedExecutableBundle();
 				Property os=findProperty(properties, OS);
 				
 				if (os==null) {bundle.setPreferredOs("Unix");}
 				else {bundle.setPreferredOs(os.getValue());
 				}
 			}
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 		}
 		return bundle;
 	}
 
 	/**
 	 * Return the list of flow templates that are of a certain type. All
 	 * templates are returned if type is unknown, invalid, or null.
 	 * 
 	 * @param flowTypeStr
 	 *            String representation of the flow type. Types are defined in
 	 *            {@link Flow.FlowType}.
 	 * 
 	 * @return List of {@link Flow}s that match the specified type, or all of
 	 *         the flows if the type is unrecognized or null.
 	 */
 	public List<Flow> getFlowTemplates(String flowTypeStr) {
 		Flow.FlowType flowType = (flowTypeStr == null ? null : Flow.FlowType
 				.toFlowType(flowTypeStr));
 
 		Set<Flow> flowSet = this.flowService.getFlowTemplates();
 		List<Flow> list = new ArrayList<Flow>();
 		if ((flowType != null)&&(flowType!=Flow.FlowType.UNKNOWN)) {
 			for (Flow flow : flowSet) {
 				if ((flow.getType().equals(flowType)))
 					list.add(flow);
 			}
 		} else {
 			list.addAll(flowSet);
 		}
 		return list;
 	}
 
 	private String getFullyQualifiedPropertyName(String component,
 			String propertyName) {
 		if (component == null) {
 			return propertyName;
 		}
 		int index = component.lastIndexOf("/");
 		if (index == -1) {
 			return component + "_" + propertyName;
 		}
 		int second = component.substring(0, index).lastIndexOf("/");
 		String cname = component.substring(second + 1, index);
 		String count = component.substring(index + 1);
 		return cname + "_" + count + "_" + propertyName;
 	}
 
 	/**
 	 * Get all mirex submissions from Mirex 2010 records and append an extra for
 	 * non-submission
 	 * 
 	 * @return
 	 */
 	public List<MirexSubmission> getAllMirexSubmissions() {
 		MirexSubmission nonSubmission = new MirexSubmission(-999999,
 				JobForm.IMPOSSIBLE, "not a mirex submission");
 		List<MirexSubmission> submissions = mirexSubmissionDao
 				.getAllSubmissions();
 		Collections.sort(submissions,new Comparator<MirexSubmission>(){
 
 			@Override
 			public int compare(MirexSubmission arg0, MirexSubmission arg1) {
 				return arg0.getHashcode().compareTo(arg1.getHashcode());
 			}});
 		submissions.add(nonSubmission);
 		return submissions;
 	}
 
 	/**
 	 * This method is necessary for render tag
 	 * 
 	 * @return roles from the default user manager
 	 */
 	public String[] getRoles() {
 		Set<Role> roleList = this.userManager.getCurrentUser().getRoles();
 		int size = roleList.size();
 
 		String[] roles = new String[size];
 		int i = 0;
 		for (Role role : roleList) {
 			roles[i] = role.getName();
 			i++;
 		}
 		return roles;
 	}
 
 	/**
 	 * Tests whether or not the supplied properties are from a remote component.
 	 * 
 	 * @param properties
 	 *            Properties of one component
 	 * @return True if the properties are from a remote component.
 	 */
 	public boolean areFromRemoteComponent(List<Property> properties) {
 		Property remote = findProperty(properties, REMOTE_COMPONENT);
 		return (remote != null)
 				&& (remote.getDefaultValue().toString()
 						.equalsIgnoreCase("true"));
 	}
 
 	/**
 	 * Go through all the properties from all components, change the value of
 	 * the specific name.
 	 * 
 	 * @param componentMap
 	 *            properties from all component
 	 * @param name
 	 *            Property name
 	 * @param value
 	 *            Property value
 	 */
 	public void replacePropertyValue(
 			Map<Component, List<Property>> componentMap, String name,
 			String value) {
 		for (List<Property> list : componentMap.values()) {
 			for (Property property : list) {
 				if (name.equals(property.getName())) {
 					property.setValue(value);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @see TasksServiceImpl.replacePropertyValue();
 	 * @param componentMap
 	 * @param name
 	 * @param value
 	 */
 	public void replacePropertyValue(
 			Map<Component, List<Property>> componentMap, String name,
 			int value) {
 		replacePropertyValue(componentMap, name, String.valueOf(value));
 	}
 	
 	/**
 	 * update properties (_submissionCode, _submissionName) from componentMap with the submissions code
 	 * @param componentMap  Properties of all components, Note: corresponding properties are modified
 	 * @param submissionCode 
 	 * @param submissions	list of all Mirex submissions, to find the name for submission code
 	 */
 	public void updateMirexSubmission(
 			Map<Component, List<Property>> componentMap,
 			String submissionCode,List<MirexSubmission> submissions){
 	 String submissionName=findMirexSubmissionName(submissions,submissionCode);
 	 replacePropertyValue(componentMap,MIREX_SUBMISSION_CODE,submissionCode);
 	 replacePropertyValue(componentMap,MIREX_SUBMISSION_NAME,submissionName);
 	 //jobForm.setName(appendSuffix(jobForm.getName(),submissionCode));
 	 //jobForm.setDescription(appendSuffix(jobForm.getDescription(),submissionCode));
 	}
 	
 	
 	private String findMirexSubmissionName(List<MirexSubmission> list,String submissionCode){
 		if (submissionCode!=null){
 		for (MirexSubmission submission:list){
 			if (submissionCode.equals(submission.getHashcode())) {
 				return submission.getName();  
 			}
 		}
 		}
 		return null;
 	}
 	/**
 	 * Create a job with all the properties in datatypeMaps.
 	 * 
 	 * @param flow
 	 *            Flow that the job is based on.
 	 * @param componentMap
 	 *            All parameters.
 	 * @param name
 	 *            Job name
 	 * @param description
 	 *            Job description
 	 * @return the job object created with the parameters
 	 * @throws MeandreServerException
 	 */
 	public Job run(Flow flow, Map<Component, List<Property>> componentMap,
 			String name, String description, String mirexSubmissionCode,int taskId)
 			throws MeandreServerException {
 		HashMap<String, String> paramMap = new HashMap<String, String>();
 
 		Component component;
 		for (Entry<Component, List<Property>> mapsEntry : componentMap
 				.entrySet()) {
 			component = mapsEntry.getKey();
 			for (Property property : mapsEntry.getValue()) {
 				paramMap.put(getFullyQualifiedPropertyName(component
 						.getInstanceUri(), property.getName()), property
 						.getValue());
 			}
 
 		}
 		String templateFlowId = flow.getId().toString();
 		String templateFlowUri = flow.getUri();
 
 		logger.debug("start to run");
 		if (templateFlowId == null || templateFlowUri == null) {
 			logger
 					.error("flowId or flowUri is null -some severe error happened...");
 			throw new MeandreServerException("flowId or flowUri is null");
 		}
 		User user = userManager.getCurrentUser();
 		if (user == null) {
 			logger.error("user is null");
 			throw new MeandreServerException("Could not get the user");
 		}
 
 		if (name == null) {
 			name = paramMap.get("name");
 			if (name == null) {
 				name = flow.getName();
 			}
 		}
 		if (description == null) {
 			description = paramMap.get("description");
 			if (description == null) {
 				description = flow.getDescription();
 			}
 		}
 		long userId = user.getId();
 		Flow instance = new Flow();
 		instance.setCreatorId(userId);
 		instance.setDateCreated(new Date());
 		instance.setInstanceOf(findTemplate(flow));
 		instance.setKeyWords(flow.getKeyWords());
 		instance.setName(name);
 		instance.setTemplate(false);
 		instance.setDescription(description);
 		instance.setType(flow.getType());
 		instance.setTypeName(flow.getTypeName());
 		instance.setSubmissionCode(mirexSubmissionCode);
 		logger.info("Getting current user's credentials to send them to the flowservice");
                 instance.setTaskId((long)taskId);
 		
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
                 logger.info("Getting current user's credentials to send them to the flowservice:"+credential.getUserID());
 		instance = this.flowService.createNewFlow(credential, instance,
 				paramMap, templateFlowUri, user.getId());
 
 		String[] splits = instance.getUri().split("/");
 		String token = "token";
 
 		if (splits.length > 0) {
 			token = splits[splits.length - 1];
 		}
 
 		long instanceId = instance.getId();
 		logger.info("created new flow: " + instance.getUri()
 				+ " Now ready to run.");
 		Job job = this.flowService.executeJob(credential, token, name,
 				description, instanceId, user.getId(), user.getEmail());
 
 		logger.info("Finish calling execute Job");
 		return job;
 
 	}
 	
 	private Flow findTemplate(Flow flow){
 		if (flow.isTemplate()||(flow.getInstanceOf()==null)||(flow.getInstanceOf()==flow)){
 			return flow;
 		}else {
 			return findTemplate(flow.getInstanceOf());
 		}
 	}
 
 	/**
 	 * Only for testing. Not called in the production code.
 	 */
 	public void test() {
 		throw new org.springframework.remoting.RemoteAccessException(
 				"custom exception");
 	}
 
 	public void setArtifactService(ArtifactService artifactService) {
 		this.artifactService = artifactService;
 	}
 
 	/**
 	 * Extract a {@code List} of {@link Component}s from the given {@code Map}
 	 * of {@link Components}. The order of the list should not be changed.
 	 * 
 	 * @param componentMap
 	 *            Map of {@link Component}s to component properties.
 	 * @return List of just {@link Component}s extracted from the supplied map.
 	 */
 	public List<Component> extractComponentList(
 			Map<Component, List<Property>> componentMap) {
 		List<Component> list = new ArrayList<Component>(componentMap.keySet());
 		Collections.sort(list);
 		return list;
 	}
 
 	/**
 	 * Load the {@link Components} for the given {@link Flow}. Set the
 	 * _credential property to current user.
 	 * 
 	 * @param flow
 	 *            The {@link Flow} for which the properties should be loaded.
 	 * @return Map containing {@link Component}s to {@link Properties}.
 	 */
 	public Map<Component, List<Property>> loadFlowComponents(Flow flow) {
 		
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		Map<Component, List<Property>> componentsToPropertyLists = flowService
 				.getAllComponentsAndPropertyDataTypes(credential,flow.getUri());
 
 		for (List<Property> properties : componentsToPropertyLists.values()) {
 			Collections.sort(properties);
 		}
 		
 		//prepopulate the credential field
 		
 		String credentialString = credential.getUserID() + ":"
 				+ new String(credential.getPassword());
 		replacePropertyValue(componentsToPropertyLists, CREDENTIALS, credentialString);
 		replacePropertyValue(componentsToPropertyLists,NEMA_USER, userManager.getCurrentUser().getUsername());
 		replacePropertyValue(componentsToPropertyLists,LOOKUP_HOST,jiniService.getLookupServiceHost());
 		replacePropertyValue(componentsToPropertyLists,CONTENT_REPOSITORY_URI,jcrService.getContentRepositoryUri());
 		
 		return componentsToPropertyLists;
 	}
 	
 	
 
 	public void setFlowService(FlowService flowService) {
 		this.flowService = flowService;
 	}
 
 	/**
 	 * By default this field is set by method {@link setUploadingPaths}, and
 	 * this field <B>must</B> match field webDir {@link setWebDir}. It is the
 	 * physical directory used to store the uploading field of file type. This
 	 * one should end with proper "/" or "\" TODO this is a bad implementation
 	 * of file upload, it needs more robust implementation
 	 * 
 	 * @param physicalDir
 	 */
 	public void setPhysicalDir(String physicalDir) {
 		this.physicalDir = physicalDir;
 	}
 
 	/**
 	 * Set the upload directory, this one should start with "/" and end with no
 	 * "/", an empty one would be "". The method should check on it and correct
 	 * it if possible.
 	 * 
 	 * TODO this is a bad implementation of file upload, it needs more robust
 	 * implementation
 	 * 
 	 * @param uploadDirectory
 	 */
 	public void setUploadDirectory(String uploadDirectory) {
 		if ((!uploadDirectory.isEmpty()) && (!uploadDirectory.startsWith("/"))) {
 			uploadDirectory = "/" + uploadDirectory;
 		}
 		if ((!uploadDirectory.isEmpty()) && (uploadDirectory.endsWith("/"))) {
 			uploadDirectory = uploadDirectory.substring(0, uploadDirectory
 					.length() - 1);
 		}
 		this.uploadDirectory = uploadDirectory;
 	}
 
 	/**
 	 * set the real physical/web path from the servlet context/request for
 	 * uploading, default behavior, when webDir has value (set by outside), skip
 	 * this step. The path is unique for one webflow. TODO this is a bad
 	 * implementation of file upload, it needs more robust implementation
 	 * 
 	 * @param externalContext
 	 *            {@link ExternalContext} with all the server info.
 	 * @param uuid
 	 *            UUID of the flow to build the upload path.
 	 * 
 	 */
 	public void buildUploadPath(ExternalContext externalContext, UUID uuid) {
 		if ((webDir == null) || (webDir.isEmpty())) {
 			ServletContext context = (ServletContext) externalContext
 					.getNativeContext();
 			HttpServletRequest req = (HttpServletRequest) externalContext
 					.getNativeRequest();
 			String subDir = uploadDirectory + "/" + req.getRemoteUser() + "/"
 					+ uuid + "/";
 			physicalDir = context.getRealPath(subDir);
 			// Create the directory if it doesn't exist
 			if (!physicalDir.endsWith(File.separator)) {
 				physicalDir = physicalDir + File.separator;
 			}
 
 			webDir = "http://" + req.getServerName() + ":"
 					+ req.getServerPort() + req.getContextPath() + subDir;
 
 			logger.info("Built the upload path: " + physicalDir + "," + webDir);
 		}
 	}
 
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 
 	/**
 	 * By default this field is set by method {@link setUploadingPaths}, and
 	 * this field <B>must</B> match field physicalDir {@link setPhysicalDir}. It
 	 * is the web directory used to store the uploading field of file type. This
 	 * one should end with "/"
 	 * 
 	 * @param webDir
 	 */
 	public void setWebDir(String webDir) {
 		this.webDir = webDir;
 	}
 
 	/**
 	 * Update the property values using the user-submitted parameters.
 	 * 
 	 * @param parameters
 	 *            HTTP request parameters.
 	 * @param properties
 	 *            Properties to be updated. Values are modified in the method.
 	 */
 	public void updateProperties(ParameterMap parameters,
 			List<Property> properties) {
 		for (Property property : properties) {
 			if (CREDENTIALS.equals(property.getName())) {
 				SimpleCredentials credential = userManager
 						.getCurrentUserCredentials();
 				String credentialString = credential.getUserID() + ":"
 						+ new String(credential.getPassword());
 				property.setValue(credentialString);
 			} else {
 				if (parameters.contains(property.getName())) {
 					List<DataTypeBean> ltb = property.getDataTypeBeanList();
 					if ((ltb != null)
 							&& (!ltb.isEmpty())
 							&& (ltb.get(0).getRenderer() != null)
 							&& (ltb.get(0).getRenderer()
 									.endsWith("FileRenderer"))) {
 						MultipartFile file = parameters
 								.getMultipartFile(property.getName());
 						if ((file != null) && (!file.isEmpty())) {
 							File dirPath = new File(physicalDir);
 
 							if (!dirPath.exists()) {
 								dirPath.mkdirs();
 							}
 							String filename = file.getOriginalFilename();
 							File uploadedFile = new File(physicalDir + filename);
 							try {
 								file.transferTo(uploadedFile);
 							} catch (IllegalStateException e) {
 								logger.error(e, e);
 							} catch (IOException e) {
 								logger.error(e, e);
 							}
 							property.setValue(webDir + filename);
 						}
 					} else {
 						property.setValue(parameters.get(property.getName()));
 					}
 				}
 			}
 		}// for loop
 	}
 
 	private Property findProperty(Collection<Property> properties, String name) {
 		if (name != null) {
 			for (Property property : properties) {
 				if (name.equals(property.getName()))
 					return property;
 			}
 		}
 		return null;
 	}
 
 	public List<Property> removeHiddenPropertiesForRemoteDynamicComponent(
 			List<Property> properties) {
 		List<Property> list = new ArrayList<Property>();
 		for (Property property : properties) {
 			if (!HIDDEN_PROPERTIES.contains(property.getName())) {
 				list.add(property);
 			}
 		}
 
 		return list;
 	}
 
 	/**
 	 * Find the Executable Bundle from the content repository using the properties's profile as the path
 	 * @param properties
 	 * @return
 	 * @throws ContentRepositoryServiceException
 	 * @throws InvalidResourcePathException
 	 */
 	public ExecutableMetadata getExecutableMetadata(List<Property> properties) throws ContentRepositoryServiceException, InvalidResourcePathException{
 		Property executable=findProperty(properties, EXECUTABLE_URL);
 		ResourcePath path=getRepositoryResourcePath(executable.getValue());
 		SimpleCredentials credential = userManager.getCurrentUserCredentials();
 		ExecutableMetadata meta=artifactService.getBundleMetadata(credential, path); 
 		
 		return meta;
 	}
 	
 	/**
 	 * If the commandlineflag ("commandLine" in request) is changed, duplicate the executable bundle , 
 	 * send it over to content repository, update the property with the new resource path, and change the newExecutable flag with true. 
 	 * If it is already the new executable, the previous one in content repository is removed.     
 	 * @param parameters
 	 * @param meta Original executable metadata.  
 	 * @param properties Modified.  The Executable_URL is replaced with the new one if command line is changed
 	 * @param uuid   
 	 * @param newExecutable   The map of flags, with component as key.  {@see generateIsNewExecutableMap}
 	 * @param component
 	 * @throws InvalidResourcePathException
 	 * @throws ContentRepositoryServiceException
 	 */
 	public void cloneExecutableBundle(ParameterMap parameters, ExecutableMetadata meta,List<Property> properties,UUID uuid,Map<Component,Boolean> newExecutable, Component component)
 		throws InvalidResourcePathException, ContentRepositoryServiceException{
 		String commandLine=parameters.get("commandLine");
 		if (!meta.getCommandLineFlags().equals(commandLine)){
 			Property executableProp=findProperty(properties, EXECUTABLE_URL);
 			SimpleCredentials credential = userManager.getCurrentUserCredentials();
 			ResourcePath path=getRepositoryResourcePath(executableProp.getValue());
 			ExecutableBundle executable=artifactService.getExecutableBundle(credential, path);
 			
 			if (newExecutable.get(component)){
 				deleteExecutableFromRepository(path, credential);
 			}else {
 				newExecutable.put(component,true);
 			}
 			
 			executable.setCommandLineFlags(commandLine);
 			ResourcePath newPath=artifactService.saveExecutableBundle(credential, uuid.toString(), executable);
 			logger.info("Saved executable bundle at "+newPath.getURIAsString());
 			executableProp.setValue(newPath.getURIAsString());
 			
 		}
 	}
 	
 	
 	
 	private RepositoryResourcePath getRepositoryResourcePath(final String jcrUri)
 			                        throws InvalidResourcePathException {
 			                String split[] = jcrUri.split(":");
 			                if (split.length != 3) {
 			                        throw new InvalidResourcePathException(
 			                                        "The resource path is of the format protocol:workspace:nodepath");
 			                }
 			                String protocol = split[0];
 			                String workspace = split[1];
 			                String path = split[2];
 			                // get rid of the first two slashes
 			                path = path.substring(2);
 			                RepositoryResourcePath rrp = new RepositoryResourcePath(protocol,workspace, path);
 			                return rrp;
     }
 
 	
 	/**
 	 * Generate the map of flags, with component as key. 
 	 * 
 	 * 	 	True if  it is already the new executable bundle that should be removed from content repository.  
 	 * 		False if the executable bundle is the old one that should be kept in the content repository. 
 
 	 * @param components
 	 * @return
 	 */
 	public Map<Component,Boolean> generateIsNewExecutableMap(Collection<Component> components){
 		Map<Component,Boolean> map=new HashMap<Component,Boolean>();
 		for (Component component:components){
 			map.put(component, false);
 		}
 		return map;
 	}
 	
 	
 	public void prepareModel(JobForm jobForm,Map<Component,List<Property>> componentMap,Flow flow,Boolean cloned){
 		if (cloned==null) {cloned=false;}
 		
 		String des=flow.getDescription();
 		if (des==null) des="";
 		if (cloned) {des="Clone of \""+flow.getName()+"\"\n"+des;}
 		jobForm.setDescription(des);
 		Property taskId=searchProperty(componentMap,"taskID");
 		if (taskId!=null){
 		try{
 			jobForm.setTaskId(Integer.parseInt(taskId.getValue()));
 		}catch(NumberFormatException e){
 			jobForm.setTaskId(0);
 		}
 		}
 		Property mirexSub=searchProperty(componentMap,MIREX_SUBMISSION_CODE);
 		if (mirexSub!=null) {jobForm.setMirexSubmissionCode(mirexSub.getValue());}
 		
 	}
 	
 	private Property searchProperty(Map<Component,List<Property>> componentMap,String name){
 		for (List<Property> list:componentMap.values()){
 			Property prop=findProperty(list, name);
 			if (prop!=null) return prop;
 		}
 		return null;
 	}
 	
 	public void setMirexSubmissionDao(ExternalMirexSubmissionDao mirexSubmissionDao) {
 		this.mirexSubmissionDao = mirexSubmissionDao;
 	}
 	public void setJcrService(JcrService jcrService) {
 		this.jcrService = jcrService;
 	}
 
 	public void setJiniService(JiniService jiniService) {
 		this.jiniService = jiniService;
 	}
 }
