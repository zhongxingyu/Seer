 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import static com.testdroid.api.APIEntity.createUri;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APIQueryBuilder;
 import com.testdroid.api.APISort;
 import com.testdroid.api.model.APIFiles.APIFile;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Łukasz Kajda <lukasz.kajda@bitbar.com>
  */
 @XmlRootElement
 public class APIProject extends APIEntity {
 
     @XmlType(namespace = "APIProject")
     public static enum Type { 
         ANDROID, CTS, IOS, UIAUTOMATOR, REMOTECONTROL, RECORDERONLINE, CALABASH;
         public Class<? extends APIFiles> getFilesClass() {
             switch(this) {
                 case ANDROID: return AndroidFiles.class;
                 case CTS: return null;
                 case IOS: return IOSFiles.class;
                 case UIAUTOMATOR: return UIAutomatorFiles.class;
                 case REMOTECONTROL: return RemoteControlFiles.class;
                 case RECORDERONLINE: return RecorderOnlineFiles.class;
                 case CALABASH: return CalabashFiles.class;
                 default: return null;
             }
         }
     }
     
     @XmlType(namespace = "APIProject")
     public static enum APIArchivingStrategy {
         NEVER, DAYS, RUNS;
     }
 
     private String name;
     private String description;
     private Type type;
     private boolean common;
     private Long sharedById;
     private String sharedByEmail;
    private APIArchivingStrategy archivingStrategy = APIArchivingStrategy.NEVER;
     private Integer archivingItemCount;
 
     public APIProject() {
     }
     
     public APIProject(Long id, String name, String description, Type type, Long sharedById, String sharedByEmail, boolean common, 
             APIArchivingStrategy archivingStrategy, Integer archivingItemCount) {
         super(id);
         this.name = name;
         this.description = description;
         this.type = type;
         this.sharedById = sharedById;
         this.sharedByEmail = sharedByEmail;
         this.common = common;
         this.archivingStrategy = archivingStrategy;
         this.archivingItemCount = archivingItemCount;
         jobConfig = new HashMap<APIProjectJobConfig.Type, APIProjectJobConfig>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public boolean isCommon() {
         return common;
     }
 
     public void setCommon(boolean common) {
         this.common = common;
     }
 
     /**
      * Returns user ID sharing this project or null if project is owned or common.
      */
     public Long getSharedById() {
         return sharedById;
     }
 
     public void setSharedById(Long sharedById) {
         this.sharedById = sharedById;
     }
 
     public String getSharedByEmail() {
         return sharedByEmail;
     }
 
     public void setSharedByEmail(String sharedByEmail) {
         this.sharedByEmail = sharedByEmail;
     }
 
     public APIArchivingStrategy getArchivingStrategy() {
         return archivingStrategy;
     }
 
     public void setArchivingStrategy(APIArchivingStrategy archivingStrategy) {
         this.archivingStrategy = archivingStrategy;
     }
 
     public Integer getArchivingItemCount() {
         return archivingItemCount;
     }
 
     public void setArchivingItemCount(Integer archivingItemCount) {
         this.archivingItemCount = archivingItemCount;
     }
     
     public String getArchivingStrategyDisplayValue() {
         switch(archivingStrategy) {
             case NEVER: return "never";
             case RUNS: return String.format("%s run%s", archivingItemCount, archivingItemCount != 1 ? "s" : "");
             case DAYS: return String.format("%s day%s", archivingItemCount, archivingItemCount != 1 ? "s" : "");
             default: return "";
         }
     }
 
     private APITestRunConfig testRunConfig;
     private Map<APIProjectJobConfig.Type, APIProjectJobConfig> jobConfig;
     private APIFiles files;
     private byte[] icon;
 
     private String getConfigURI() { return createUri(selfURI, "/config"); };
     private String getJobConfigURI(APIProjectJobConfig.Type type) { return createUri(selfURI, "/job-configs/" + type.toString()); };
     private String getFilesURI() { return createUri(selfURI, "/files"); };
     private String getIconURI() { return createUri(selfURI, "/icon"); };
     private String getSharingsURI() { return createUri(selfURI, "/sharings"); };
     private String getTrendsURI() { return createUri(selfURI, "/trends"); };
     private String getReportsURI() { return createUri(selfURI, "/reports/%s"); };
     private String getRunsURI() { return createUri(selfURI, "/runs"); };
     private String getRunURI(Long id) { return createUri(selfURI, "/runs/" + id); }
     private String getPublicDeviceGroupsURI() { return createUri(selfURI, "/public-device-groups"); }
     private String getUploadApplicationURI() { return createUri(selfURI, "/files/application"); }
     private String getUploadTestURI() { return createUri(selfURI, "/files/test"); }
     private String getUploadDataURI() { return createUri(selfURI, "/files/data"); }
     private String getNotificationEmailsURI() { return createUri(selfURI, "/notifications"); }
     private String getParametersURI() { return createUri(selfURI, "/config/parameters"); }
 
     private String getCreateRunParameters(String testRunName) {
         return String.format("name=%s", testRunName);
     }
     private String getCreateNotificationParameters(String email, APINotificationEmail.Type type) {
         return String.format("email=%s&type=%s", email, type);
     }
     private String getCreateParameterParameters(String key, String value) {
         return String.format("key=%s&value=%s", key, value);
     }
     
     @JsonIgnore
     public APITestRunConfig getTestRunConfig() throws APIException {
         if(testRunConfig == null) {
             testRunConfig = getResource(getConfigURI(), APITestRunConfig.class).getEntity();
         }
         return testRunConfig;
     }
     
     @JsonIgnore
     public APIProjectJobConfig getJobConfig(APIProjectJobConfig.Type type) throws APIException {
         if(jobConfig == null ) {
             jobConfig = new HashMap<APIProjectJobConfig.Type, APIProjectJobConfig>();
         }
         if(jobConfig.get(type) == null) {
             jobConfig.put(type, getResource(getJobConfigURI(type), APIProjectJobConfig.class).getEntity());
         }
         return jobConfig.get(type);
     }
     
     /**
      * Returns APIFiles entity about files uploaded to this project.
      * Depending on <code>type</code> it may be any subclass of <code>APIFiles</code> returned.
      */
     @JsonIgnore
     public <T extends APIFiles> T getFiles(Class<T> clazz) throws APIException {
         if(clazz == null || !clazz.isAssignableFrom(type.getFilesClass())) {
             throw new APIException("This project type does not have requested file types");
         }
         if(files == null) {
             files = getResource(getFilesURI(), clazz).getEntity();
         }
         return (T) files;
     }
     
     @JsonIgnore
     public byte[] getIcon() throws APIException, IOException {
         if(icon == null) {
             icon = IOUtils.toByteArray(getResource(getIconURI(), null).getStream());
         }
         return icon;
     }
     
     @JsonIgnore
     public APITestRun run() throws APIException {
         return postResource(getRunsURI(), null, APITestRun.class);
     }
     
     @JsonIgnore
     public APITestRun run(String testRunName) throws APIException {
         return postResource(getRunsURI(), getCreateRunParameters(testRunName), APITestRun.class);
     }
     
     public void update() throws APIException {
         Map<String, Object> body = new HashMap<String, Object>() {{
             put("name", name);
             put("description", description);
             put("common", common);
             put("archivingStrategy", archivingStrategy);
             put("archivingItemCount", archivingItemCount);
         }};
         APIProject project = postResource(selfURI, body, APIProject.class);
         clone(project);
     }
     
     public void delete() throws APIException {
         deleteResource(selfURI);
     }
 
     @JsonIgnore
     public APIListResource<APITestRun> getTestRunsResource() throws APIException {
         return getListResource(getRunsURI(), APITestRun.class);
     }
 
     /**
      * @since 1.3.34
      * @param queryBuilder
      * @return
      * @throws APIException 
      */
     @JsonIgnore
     public APIListResource<APITestRun> getTestRunsResource(APIQueryBuilder queryBuilder) throws APIException {
         return getListResource(getRunsURI(), queryBuilder, APITestRun.class);
     }
     
     public APITestRun getTestRun(Long id) throws APIException {
         return getResource(getRunURI(id), APITestRun.class).getEntity();
     }
     
     /**
      * @deprecated 
      * @param offset
      * @param limit
      * @param search
      * @param sort
      * @return
      * @throws APIException 
      */
     @JsonIgnore
     public APIListResource<APITestRun> getTestRunsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getRunsURI(), offset, limit, search, sort, APITestRun.class);
     }
     
     @JsonIgnore
     public APIProjectSharing share(String email) throws APIException {
         return postResource(getSharingsURI(), String.format("email=%s", email), APIProjectSharing.class);
     }
     
     @JsonIgnore
     public APIListResource<APIProjectSharing> getProjectSharings() throws APIException {
         return getListResource(getSharingsURI(), APIProjectSharing.class);
     }
     
     /**
      * @since 1.3.34
      * @param queryBuilder 
      * @return
      * @throws APIException 
      */
     @JsonIgnore
     public APIListResource<APIProjectSharing> getProjectSharings(APIQueryBuilder queryBuilder) throws APIException {
         return getListResource(getSharingsURI(), queryBuilder, APIProjectSharing.class);
     }
     
     /**
      * @deprecated 
      * @param offset
      * @param limit
      * @param search
      * @param sort
      * @return
      * @throws APIException 
      */
     @JsonIgnore
     public APIListResource<APIProjectSharing> getProjectSharings(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getSharingsURI(), offset, limit, search, sort, APIProjectSharing.class);
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceGroup> getPublicDeviceGroups() throws APIException {
         return getListResource(getPublicDeviceGroupsURI(), APIDeviceGroup.class);
     }
     
     @JsonIgnore
     public APINotificationEmail createNotificationEmail(String email, APINotificationEmail.Type type) throws APIException {
         return postResource(getNotificationEmailsURI(), getCreateNotificationParameters(email, type), APINotificationEmail.class);
     }
     
     @JsonIgnore
     public APIListResource<APINotificationEmail> getNotificationEmails() throws APIException {
         return getListResource(getNotificationEmailsURI(), APINotificationEmail.class);
     }
     
     /**
      * Returns list of notification emails for project.
      * @since 1.3.34
      * @param queryBuilder
      * @return
      * @throws APIException 
      */
     @JsonIgnore
     public APIListResource<APINotificationEmail> getNotificationEmails(APIQueryBuilder queryBuilder) throws APIException {
         return getListResource(getNotificationEmailsURI(), queryBuilder, APINotificationEmail.class);
     }
     
     @JsonIgnore
     public APIFiles.APIFile uploadApplication(File file, String contentType) throws APIException {
         return postFile(getUploadApplicationURI(), file, contentType, APIFile.class);
     }
 
     @JsonIgnore
     public APIFiles.APIFile uploadTest(File file, String contentType) throws APIException {
         return postFile(getUploadTestURI(), file, contentType, APIFile.class);
     }
     
     @JsonIgnore
     public APIFiles.APIFile uploadData(File file, String contentType) throws APIException {
         return postFile(getUploadDataURI(), file, contentType, APIFile.class);
     }
     
     @JsonIgnore
     public APITestRunParameter createParameter(String key, String value) throws APIException {
         return postResource(getParametersURI(), getCreateParameterParameters(key, value), APITestRunParameter.class);
     }
     
     @JsonIgnore
     public APIListResource<APITestRunParameter> getParameters() throws APIException {
         return getListResource(getParametersURI(), APITestRunParameter.class);
     }
     
     @JsonIgnore
     public APIListResource<APITestRunParameter> getParameters(APIQueryBuilder queryBuilder) throws APIException {
         return getListResource(getParametersURI(), queryBuilder, APITestRunParameter.class);
     }
     
     @JsonIgnore
     public void deleteParameter(long parameterId) throws APIException {
         deleteResource(String.format("%s/%s", getParametersURI(), parameterId));
     }
     
     @Override
     @JsonIgnore
     protected <T extends APIEntity> void clone(T from) {
         APIProject apiProject = (APIProject) from;
         cloneBase(from);
         this.common = apiProject.common;
         this.description = apiProject.description;
         this.files = apiProject.files;
         this.icon = apiProject.icon;
         this.jobConfig = apiProject.jobConfig;
         this.name = apiProject.name;
         this.sharedById = apiProject.sharedById;
         this.testRunConfig = apiProject.testRunConfig;
         this.type = apiProject.type;
         this.archivingStrategy = apiProject.archivingStrategy;
         this.archivingItemCount = apiProject.archivingItemCount;
     }
 }
