 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import static com.testdroid.api.APIEntity.encodeURL;
 import com.testdroid.api.APIException;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APITestRunConfig extends APIEntity {
     @XmlType
     public static enum Scheduler {
         PARALLEL, SERIAL;
     }
     @XmlType
     public static enum LimitationType {
         PACKAGE, CLASS;
     }
     @XmlType
     public static enum Mode {
 
         FULL_RUN("Full run"), 
         APP_CRAWLER("App crawler"), 
         CTS("CTS"), 
         IOS("iOS"), 
         UIAUTOMATOR("UIAutomator"), 
         REMOTECONTROL("Remote control"),
         RECORDERONLINE("Recorder online"),
         CALABASH("Calabash");
         
         private String friendlyName;
         
         private Mode(String friendlyName){
             this.friendlyName = friendlyName;
         }
 
         public String getFriendlyName() {
             return friendlyName;
         }
     }
     
     private Long projectId;
     private Scheduler scheduler;
     private Mode mode;
     private boolean autoScreenshots;
     private boolean runAvailable;
     private String screenshotDir;
     private LimitationType limitationType;
     private String limitationValue;
     private String withAnnotation;
     private String withoutAnnotation;
     private String applicationUsername;
     private String applicationPassword;
     private Long usedDeviceGroupId;
     private Integer creditsPrice;
     private String deviceLanguageCode;
     private String hookURL;
     private String uiAutomatorTestClasses;
     private boolean launchApp;
     private String instrumentationRunner;
     private boolean checkApp;
 
     public APITestRunConfig() {}
 
     public APITestRunConfig(Long id, Long projectId, Scheduler scheduler, Mode mode, boolean autoScreenshots, boolean runAvailable, 
             String screenshotDir, LimitationType limitationType, String limitationValue, String withAnnotation, String withoutAnnotation, 
             String applicationUsername, String applicationPassword, Long usedClusterId, Integer creditsPrice, String deviceLanguageCode, String hookURL,
             String uiAutomatorTestClasses, Boolean launchApp, String instrumentationRunner, Boolean checkApp) {
         super(id);
         this.projectId = projectId;
         this.scheduler = scheduler;
         this.mode = mode;
         this.autoScreenshots = autoScreenshots;
         this.runAvailable = runAvailable;
         this.screenshotDir = screenshotDir;
         this.limitationType = limitationType;
         this.limitationValue = limitationValue;
         this.withAnnotation = withAnnotation;
         this.withoutAnnotation = withoutAnnotation;
         this.applicationUsername = applicationUsername;
         this.applicationPassword = applicationPassword;
         this.usedDeviceGroupId = usedClusterId;
         this.creditsPrice = creditsPrice;
         this.deviceLanguageCode = deviceLanguageCode;
         this.hookURL = hookURL;
         this.uiAutomatorTestClasses = uiAutomatorTestClasses;
         this.launchApp = launchApp;
         this.instrumentationRunner = instrumentationRunner;
         this.checkApp = checkApp;
     }
 
     public Long getProjectId() {
         return projectId;
     }
 
     public void setProjectId(Long projectId) {
         this.projectId = projectId;
     }
 
     public Scheduler getScheduler() {
         return scheduler;
     }
 
     public void setScheduler(Scheduler scheduler) {
         this.scheduler = scheduler;
     }
 
     public Mode getMode() {
         return mode;
     }
 
     public void setMode(Mode mode) {
         this.mode = mode;
     }
 
     public LimitationType getLimitationType() {
         return limitationType;
     }
 
     public void setLimitationType(LimitationType limitationType) {
         this.limitationType = limitationType;
     }
 
     public boolean isAutoScreenshots() {
         return autoScreenshots;
     }
 
     public void setAutoScreenshots(boolean autoScreenshots) {
         this.autoScreenshots = autoScreenshots;
     }
 
     public boolean isRunAvailable() {
         return runAvailable;
     }
 
     public void setRunAvailable(boolean runAvailable) {
         this.runAvailable = runAvailable;
     }
 
     public String getScreenshotDir() {
         return screenshotDir;
     }
 
     public void setScreenshotDir(String screenshotDir) {
         this.screenshotDir = screenshotDir;
     }
 
     public String getLimitationValue() {
         return limitationValue;
     }
 
     public void setLimitationValue(String limitationValue) {
         this.limitationValue = limitationValue;
     }
 
     public String getWithAnnotation() {
         return withAnnotation;
     }
 
     public void setWithAnnotation(String withAnnotation) {
         this.withAnnotation = withAnnotation;
     }
 
     public String getWithoutAnnotation() {
         return withoutAnnotation;
     }
 
     public void setWithoutAnnotation(String withoutAnnotation) {
         this.withoutAnnotation = withoutAnnotation;
     }
 
     public String getApplicationUsername() {
         return applicationUsername;
     }
 
     public void setApplicationUsername(String applicationUsername) {
         this.applicationUsername = applicationUsername;
     }
 
     public String getApplicationPassword() {
         return applicationPassword;
     }
 
     public void setApplicationPassword(String applicationPassword) {
         this.applicationPassword = applicationPassword;
     }
 
     public Long getUsedDeviceGroupId() {
         return usedDeviceGroupId;
     }
 
     public void setUsedDeviceGroupId(Long usedDeviceGroupId) {
         this.usedDeviceGroupId = usedDeviceGroupId;
     }
 
     public Integer getCreditsPrice() {
         return creditsPrice;
     }
 
     public void setCreditsPrice(Integer creditsPrice) {
         this.creditsPrice = creditsPrice;
     }
 
     public String getDeviceLanguageCode() {
         return deviceLanguageCode;
     }
 
     public void setDeviceLanguageCode(String deviceLanguageCode) {
         this.deviceLanguageCode = deviceLanguageCode;
     }
 
     public String getHookURL() {
         return hookURL;
     }
 
     public void setHookURL(String hookURL) {
         this.hookURL = hookURL;
     }
 
     public String getUiAutomatorTestClasses() {
         return uiAutomatorTestClasses;
     }
 
     public void setUiAutomatorTestClasses(String uiAutomatorTestClasses) {
         this.uiAutomatorTestClasses = uiAutomatorTestClasses;
     }
 
     public boolean isLaunchApp() {
         return launchApp;
     }
 
     public void setLaunchApp(boolean launchApp) {
         this.launchApp = launchApp;
     }
 
     public String getInstrumentationRunner() {
         return instrumentationRunner;
     }
 
     public void setInstrumentationRunner(String instrumentationRunner) {
         this.instrumentationRunner = instrumentationRunner;
     }
 
     public boolean isCheckApp() {
         return checkApp;
     }
 
     public void setCheckApp(boolean checkApp) {
         this.checkApp = checkApp;
     }
     
     public void update() throws APIException {
        String body = String.format("?projectId=%s&scheduler=%s&mode=%s&autoScreenshots=%s&screenshotDir=%s&limitationType=%s&limitationValue=%s&" +
                "withAnnotation=%s&withoutAnnotation=%s&applicationUsername=%s&applicationPassword=%s&usedClusterId=%s&deviceLanguageCode=%s&"+
                "hookURL=%s&uiAutomatorTestClasses=%s&launchApp=%s&instrumentationRunner=%s&checkApp=%s", projectId, scheduler != null? encodeURL(scheduler.name()) : "", 
                 mode != null ? encodeURL(mode.name()) : "", encodeURL(autoScreenshots), encodeURL(screenshotDir), limitationType != null ? encodeURL(limitationType.name()) : "", 
                 encodeURL(limitationValue), encodeURL(withAnnotation), encodeURL(withoutAnnotation), encodeURL(applicationUsername), encodeURL(applicationPassword), 
                 usedDeviceGroupId, encodeURL(deviceLanguageCode), encodeURL(hookURL), encodeURL(uiAutomatorTestClasses), encodeURL(launchApp), encodeURL(instrumentationRunner),
                 encodeURL(checkApp));
         APITestRunConfig config = postResource(selfURI, body, APITestRunConfig.class);
         clone(config);
     }
 
     @Override
     @JsonIgnore
     protected <T extends APIEntity> void clone(T from) {
         APITestRunConfig apiTestRunConfig = (APITestRunConfig) from;
         cloneBase(from);
         this.applicationPassword = apiTestRunConfig.applicationPassword;
         this.applicationUsername = apiTestRunConfig.applicationUsername;
         this.autoScreenshots = apiTestRunConfig.autoScreenshots;
         this.checkApp = apiTestRunConfig.checkApp;
         this.creditsPrice = apiTestRunConfig.creditsPrice;
         this.deviceLanguageCode = apiTestRunConfig.deviceLanguageCode;
         this.hookURL = apiTestRunConfig.hookURL;
         this.instrumentationRunner = apiTestRunConfig.instrumentationRunner;
         this.launchApp = apiTestRunConfig.launchApp;
         this.limitationType = apiTestRunConfig.limitationType;
         this.limitationValue = apiTestRunConfig.limitationValue;
         this.mode = apiTestRunConfig.mode;
         this.projectId = apiTestRunConfig.projectId;
         this.runAvailable = apiTestRunConfig.runAvailable;
         this.scheduler = apiTestRunConfig.scheduler;
         this.screenshotDir = apiTestRunConfig.screenshotDir;
         this.uiAutomatorTestClasses = apiTestRunConfig.uiAutomatorTestClasses;
         this.usedDeviceGroupId = apiTestRunConfig.usedDeviceGroupId;
         this.withAnnotation = apiTestRunConfig.withAnnotation;
         this.withoutAnnotation = apiTestRunConfig.withoutAnnotation;
     }
     
 }
