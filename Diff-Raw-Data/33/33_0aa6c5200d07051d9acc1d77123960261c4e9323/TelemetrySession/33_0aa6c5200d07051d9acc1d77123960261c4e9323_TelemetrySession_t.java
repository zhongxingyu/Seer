 package org.hackystat.projectbrowser.page.telemetry;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.page.ProjectBrowserBasePage;
 import org.hackystat.projectbrowser.page.telemetry.datapanel.TelemetryChartDataModel;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.telemetry.service.client.TelemetryClient;
 import org.hackystat.telemetry.service.client.TelemetryClientException;
 import org.hackystat.telemetry.service.resource.chart.jaxb.ParameterDefinition;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryChartDefinition;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryChartRef;
 import org.hackystat.telemetry.service.resource.chart.jaxb.Type;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Session to hold state for telemetry.
  * @author Shaoxuan Zhang
  */
 public class TelemetrySession implements Serializable {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   /** The parameter key of telemetry. */
   public static final String TELEMETRY_KEY = "0";
   /** The parameter key of granularity. */
   public static final String GRANULARITY_KEY = "1";
   /** The parameter key of start date. */
   public static final String START_DATE_KEY = "2";
   /** The parameter key of end date. */
   public static final String END_DATE_KEY = "3";
   /** The parameter key of selectedProjects. */
   public static final String SELECTED_PROJECTS_KEY = "4";
   /** The parameter key of telemetry parameters. */
   public static final String TELEMETRY_PARAMERTERS_KEY = "5";
   /** 
    * The last parameter key that is required.
    * The telemetry parameters key is optional because not all telemetries have parameter.
    */
   private static final String LAST_REQUIRED_KEY = "4";
   /** The last parameter key. */
   public static final String PARAMETER_ORDER_MESSAGE = "Correct parameter order is : " + 
                         "/telemetryName/granularity/startDate/endDate/projects/param";
   
   /** The separator for parameter values. */
   public static final String PARAMETER_VALUE_SEPARATOR = ",";
   /** The separator between project name and its onwer. */
  public static final String PROJECT_NAME_OWNER_SEPARATR = "::";
 
   /** The analysis this user has selected. */
   private String telemetryName = null;
   /** The granularity of the chart. Either Day, Week, or Month. */
   private String granularity = "Day";
   /** The start date this user has selected. */
   private long startDate = ProjectBrowserBasePage.getDateBefore(7).getTime();
   /** The end date this user has selected. */
   private long endDate = ProjectBrowserBasePage.getDateBefore(1).getTime();
   /** The projects this user has selected. */
   private List<Project> selectedProjects = new ArrayList<Project>();
   
   /** The descriptions for all telemetries. */
   private final Map<String, TelemetryChartDefinition> telemetryDefs = 
                             new HashMap<String, TelemetryChartDefinition>();
   /** The available granularities. */
   private final List<String> granularityList = new ArrayList<String>();
   /** The parameters for telemetry chart. */
   private final List<IModel> parameters = new ArrayList<IModel>();
   /** The data model to hold state for data panel */
   private final TelemetryChartDataModel dataModel = new TelemetryChartDataModel();
   
   /** the feedback string. */
   private String feedback = "";
   /** Error message when parsing page paramters. */
   private String paramErrorMessage = "";
   
   /**
    * Create the instance.
    */
   public TelemetrySession() {
     granularityList.add("Day");
     granularityList.add("Week");
     granularityList.add("Month");
   }
   /**
    * @return the parameters
    */
   public List<IModel> getParameters() {
     return this.parameters;
   }
 
   /**
    * Returns the list of the parameters in a single String, separated by comma.
    * @return a String.
    */
   public String getParametersAsString() {
     StringBuffer stringBuffer = new StringBuffer();
     for (IModel model : this.parameters) {
       if (model != null) {
         stringBuffer.append(model.getObject());
         stringBuffer.append(PARAMETER_VALUE_SEPARATOR);
       }
     }
     String param = stringBuffer.toString();
     if (param.endsWith(PARAMETER_VALUE_SEPARATOR)) {
       param = param.substring(0, param.length() - 1);
     }
     return param;
   }
   
   /**
    * @param feedback the feedback to set
    */
   public void setFeedback(String feedback) {
     this.feedback = feedback;
   }
 
   /**
    * @return the feedback
    */
   public String getFeedback() {
     String returnString = this.feedback;
     this.feedback = "";
     return returnString;
   }
 
   /**
    * @param telemetryName the telemetry to set
    */
   public void setTelemetryName(String telemetryName) {
     this.telemetryName = telemetryName;
   }
 
   /**
    * @return the telemetry
    */
   public String getTelemetryName() {
     return this.telemetryName;
   }
 
   /**
    * @return the logger that associated to this web application.
    */
   private Logger getLogger() {
     return ((ProjectBrowserApplication)ProjectBrowserApplication.get()).getLogger();
   }
   
   /**
    * Return the TelemetryList. Initialize it if it is null.
    * @return the telemetryList
    */
   public List<String> getTelemetryList() {
     List<String> telemetryList = new ArrayList<String>();
     if (this.getTelemetryDefs().isEmpty()) {
       this.initializeTelemetryList();
     }
     telemetryList.addAll(this.getTelemetryDefs().keySet());
     Collections.sort(telemetryList);
     return telemetryList;
   }
   
   /**
    * Initialize the telemetry definition list.
    */
   private void initializeTelemetryList() {
     Logger logger = getLogger();
     TelemetryClient client  = ProjectBrowserSession.get().getTelemetryClient();
     try {
       logger.info("Retrieving data for Telemetry chart definitions.");
       for (TelemetryChartRef chartRef : client.getChartIndex().getTelemetryChartRef()) {
         getTelemetryDefs().put(chartRef.getName(), client.getChartDefinition(chartRef.getName()));
       }
       logger.info("Finished retrieving data for Telemetry chart definitions.");
     }
     catch (TelemetryClientException e) {
       this.feedback = "Exception when retrieving Telemetry chart definition: " + e.getMessage();
       logger.warning("Error when retrieving Telemetry chart definition: " 
                                     + e.getMessage());
     }
   }
 
   /**
    * Return the list of ParameterDefinition under telemetry type in this session.
    * @return list of ParameterDefinition.
    */
   public List<ParameterDefinition> getParameterList() {
     return this.getParameterList(this.telemetryName);
   }
   
   /**
    * Return the list of ParameterDefinition under the given telemetry.
    * @param telemetryName the telemetry
    * @return list of ParameterDefinition.
    */
   public List<ParameterDefinition> getParameterList(String telemetryName) {
     Logger logger = getLogger();
     if (this.telemetryDefs.isEmpty()) {
       this.initializeTelemetryList();
     }
     if (telemetryName != null) {
       TelemetryChartDefinition teleDef = 
         this.getTelemetryDefs().get(telemetryName);
       if (teleDef == null) {
         TelemetryClient client  = ProjectBrowserSession.get().getTelemetryClient();
         try {
           logger.info("Retrieving telemetry chart definition: " + telemetryName);
           teleDef = client.getChartDefinition(telemetryName);
           this.getTelemetryDefs().put(telemetryName, teleDef);
           return teleDef.getParameterDefinition();
         }
         catch (TelemetryClientException e) {
           this.feedback = "Error when retrieving chart definition of telemetry: " + 
             this.telemetryName + ">>" + e.getMessage();
           logger.warning("Error when retrieving chart definition of telemetry: " + 
               this.telemetryName + ">>" + e.getMessage());
         }
       }
       else {
         return teleDef.getParameterDefinition();
       }
     }
     return new ArrayList<ParameterDefinition>();
   }
 
   /**
    * @param startDate the startDate to set
    */
   public void setStartDate(Date startDate) {
     this.startDate = startDate.getTime();
   }
 
   /**
    * @return the startDate
    */
   public Date getStartDate() {
     return new Date(startDate);
   }
 
   /**
    * @param endDate the endDate to set
    */
   public void setEndDate(Date endDate) {
     this.endDate = endDate.getTime();
   }
 
   /**
    * @return the endDate
    */
   public Date getEndDate() {
     return new Date(endDate);
   }
 
   /**
    * Returns the string that represents startDate in standard formatted.
    * e.g. 2008-08-08T08:08:08+08:00, 
    * the +08:00 in the end means the time zone of this time stamp is +08:00
    * @return a String
    */
   public String getFormattedStartDateString() {
     return Tstamp.makeTimestamp(this.startDate).toString();
   }
 
   /**
    * Returns the string that represents endDate in standard formatted.
    * e.g. 2008-08-08T08:08:08+08:00, 
    * the +08:00 in the end means the time zone of this time stamp is +08:00
    * @return a String
    */
   public String getFormattedEndDateString() {
     return Tstamp.makeTimestamp(this.endDate).toString();
   }
   
   /**
    * Returns the start date in yyyy-MM-dd format.  
    * @return The date as a simple string. 
    */
   /*
   public String getStartDateString() {
     SimpleDateFormat format = 
       new SimpleDateFormat(ProjectBrowserBasePage.DATA_FORMAT, Locale.ENGLISH);
     return format.format(new Date(this.startDate));
   }
   */
   
   /**
    * Returns the end date in yyyy-MM-dd format.  
    * @return The date as a simple string. 
    */
   /*
   public String getEndDateString() {
     SimpleDateFormat format = 
       new SimpleDateFormat(ProjectBrowserBasePage.DATA_FORMAT, Locale.ENGLISH);
     return format.format(new Date(this.endDate));
   }
   */
 
   /**
    * @param granularity the granularity to set
    */
   public void setGranularity(String granularity) {
     this.granularity = granularity;
   }
 
   /**
    * @return the granularity
    */
   public String getGranularity() {
     return granularity;
   }
 
   /**
    * @return the granularityList
    */
   public List<String> getGranularityList() {
     return granularityList;
   }
 
   /**
    * Update the data model.
    * If execute in background process is determined in ProjectBrowserProperties.
    */
   public void updateDataModel() {
     boolean backgroundProcessEnable = ((ProjectBrowserApplication)ProjectBrowserApplication.get()).
       isBackgroundProcessEnable("telemetry");
     dataModel.setModel(getStartDate(), getEndDate(), 
         selectedProjects, telemetryName, granularity, parameters,
         ((ProjectBrowserApplication)ProjectBrowserApplication.get()).getTelemetryHost(),
         ProjectBrowserSession.get().getEmail(),
         ProjectBrowserSession.get().getPassword());
     
     if (backgroundProcessEnable) {
       Thread thread = new Thread() {
         @Override
         public void run() {
           dataModel.loadData();
         }
       };
       thread.start();
     }
     else {
       dataModel.loadData();
     }
     
   }
 
   
   /**
    * Cancel data model's update.
    */
   public void cancelDataUpdate() {
     dataModel.cancelDataLoading();
   }
   
 
   /**
    * @return the dataModel
    */
   public TelemetryChartDataModel getDataModel() {
     return dataModel;
   }
 
   /**
    * @param selectedProjects the selectedProjects to set
    */
   public void setSelectedProjects(List<Project> selectedProjects) {
     this.selectedProjects = selectedProjects;
   }
 
   /**
    * @return the selectedProjects
    */
   public List<Project> getSelectedProjects() {
     return selectedProjects;
   }
   
   /**
    * Returns the list of the selected projects in a single String, separated by comma.
    * @return a String.
    */
   public String getSelectedProjectsAsString() {
     StringBuffer projectList = new StringBuffer();
     for (int i = 0; i < this.getSelectedProjects().size(); ++i) {
       Project project = this.getSelectedProjects().get(i);
       if (project == null) {
         continue;
       }
       projectList.append(project.getName());
       projectList.append(PROJECT_NAME_OWNER_SEPARATR);
       projectList.append(project.getOwner());
       if (i < this.getSelectedProjects().size() - 1) {
         projectList.append(PARAMETER_VALUE_SEPARATOR);
       }
     }
     return projectList.toString();
   }
 
   /**
    * @return the telemetrys
    */
   public Map<String, TelemetryChartDefinition> getTelemetryDefs() {
     return telemetryDefs;
   }
   
   /**
    * @return the list of TelemetryChartDefinition.
    */
   public List<TelemetryChartDefinition> getChartDescriptions() {
     List<TelemetryChartDefinition> chartDef = new ArrayList<TelemetryChartDefinition>();
     for (String telemetryName : this.getTelemetryList()) {
       chartDef.add(this.telemetryDefs.get(telemetryName));
     }
     return chartDef;
   }
 
   /**
    * Returns a Project instance that available to current user and 
    * is matched to the given project name and project owner.
    * @param projectName the given project name.
    * @param projectOwner the given project owner.
    * @return the Project instance. null if no matching project is found,
    * which may means either the project name or project owner is null or there is no Project for
    * this user with the same project name and owner as the given ones.
    */
   public Project getProject(String projectName, String projectOwner) {
     if (projectName == null || projectOwner == null) {
       return null;
     }
     for (Project project : ProjectBrowserSession.get().getProjectList()) {
       if (projectName.equals(project.getName()) && projectOwner.equals(project.getOwner())) {
         return project;
       }
     }
     return null;
   }
   
   /**
    * Load data from URL parameters into this session.
    * @param parameters the URL parameters
    * @return true if all parameters are loaded correctly
    */
   public boolean loadPageParameters(PageParameters parameters) {
     boolean isLoadSucceed = true;
     boolean isTelemetryLoaded = false;
     Logger logger = this.getLogger();
     if (!parameters.containsKey(LAST_REQUIRED_KEY)) {
       isLoadSucceed = false;
       String error = "Some parameters are missing, should be " + LAST_REQUIRED_KEY + "\n" +
       		PARAMETER_ORDER_MESSAGE;
       logger.warning(error);
       this.paramErrorMessage = error + "\n";
       return false;
     }
     StringBuffer errorMessage = new StringBuffer(1000);
     //load telemetry name
     if (parameters.containsKey(TELEMETRY_KEY)) {
       String telemetryString = parameters.getString(TELEMETRY_KEY);
       if (this.getTelemetryList().contains(telemetryString)) {
         this.setTelemetryName(telemetryString);
         isTelemetryLoaded = true;
       }
       else {
         isLoadSucceed = false;
         String error = "Telemetry from URL parameter is unknown: " + telemetryString;
         logger.warning(error);
         errorMessage.append(error);
         errorMessage.append('\n');
       }
     }
     else {
       isLoadSucceed = false;
       errorMessage.append("Telemetry key is missing in URL parameters.\n");
     }
     //load granularity
     if (parameters.containsKey(GRANULARITY_KEY)) {
       String granularityString = parameters.getString(GRANULARITY_KEY);
       if (this.granularityList.contains(granularityString)) {
         this.setGranularity(granularityString);
       }
       else {
         isLoadSucceed = false;
         String error = "Granularity is not supported: " + granularityString;
         logger.warning(error);
         errorMessage.append(error);
         errorMessage.append('\n');
       }
     }
     else {
       isLoadSucceed = false;
       errorMessage.append("granularity key is missing in URL parameters.\n");
     }
     //load start date
     if (parameters.containsKey(START_DATE_KEY)) {
       String startDateString = parameters.getString(START_DATE_KEY);
       try {
         this.startDate = 
           Tstamp.makeTimestamp(startDateString).toGregorianCalendar().getTimeInMillis();
       }
       catch (Exception e) {
         isLoadSucceed = false;
         String error = 
           "Errors when parsing start date from URL parameter: " + startDateString;
         logger.warning(error + " > " + e.getMessage());
         errorMessage.append(error);
         errorMessage.append('\n');
       }
     }
     else {
       isLoadSucceed = false;
       errorMessage.append("startDate key is missing in URL parameters.\n");
     }
     //load end date
     if (parameters.containsKey(END_DATE_KEY)) {
       String endDateString = parameters.getString(END_DATE_KEY);
       try {
         this.endDate = 
           Tstamp.makeTimestamp(endDateString).toGregorianCalendar().getTimeInMillis();
       }
       catch (Exception e) {
         isLoadSucceed = false;
         String error = "Errors when parsing end date from URL parameter: " + endDateString;
         logger.warning(error + " > " + e.getMessage());
         errorMessage.append(error);
         errorMessage.append('\n');
       }
     }
     else {
       isLoadSucceed = false;
       errorMessage.append("endDate key is missing in URL parameters.\n");
     }
     //load seletecd project
     if (parameters.containsKey(SELECTED_PROJECTS_KEY)) {
      String[] projectsStringArray = 
        parameters.getString(SELECTED_PROJECTS_KEY).split(PARAMETER_VALUE_SEPARATOR);
       List<Project> projectsList = new ArrayList<Project>();
      for (String projectString : projectsStringArray) {
        int index = projectString.lastIndexOf(PROJECT_NAME_OWNER_SEPARATR);
        if (index < 0 || index > projectString.length()) {
           isLoadSucceed = false;
          String error = "Error URL parameter: project: " + projectString + 
               " >> project name and owner are missing or not formatted correctly.";
           logger.warning(error);
           errorMessage.append(error);
           errorMessage.append('\n');
           continue;
         }
        String projectName = projectString.substring(0, index);
        String projectOwner = projectString.substring(index + PROJECT_NAME_OWNER_SEPARATR.length());
        System.out.println(projectName + " " + projectOwner);
         Project project = this.getProject(projectName, projectOwner);
         if (project == null) {
           isLoadSucceed = false;
          String error = "Error URL parameter: project: " + projectString +
               " >> matching project not found under user: " + 
               ProjectBrowserSession.get().getEmail();
           logger.warning(error);
           errorMessage.append(error);
           errorMessage.append('\n');
         }
         else {
           projectsList.add(project);
         }
       }
       if (!projectsList.isEmpty()) {
         this.setSelectedProjects(projectsList);
       }
     }
     else {
       isLoadSucceed = false;
       errorMessage.append("projects key is missing in URL parameters.\n");
     }
     //load telemetry parameters
     if (parameters.containsKey(TELEMETRY_PARAMERTERS_KEY)) {
       String paramString = parameters.getString(TELEMETRY_PARAMERTERS_KEY);
       String[] paramStringArray = paramString.split(PARAMETER_VALUE_SEPARATOR);
       this.parameters.clear();
       if (isTelemetryLoaded) {
         List<ParameterDefinition> paramDefList = 
           this.getTelemetryDefs().get(this.telemetryName).getParameterDefinition();
         if (paramStringArray.length == paramDefList.size()) {
           for (int i = 0; i < paramStringArray.length; ++i) {
             if (isValueMatchType(paramStringArray[i], paramDefList.get(i).getType())) {
               this.parameters.add(new Model(paramStringArray[i]));
             }
             else {
               isLoadSucceed = false;
               String error = "Telemetry parameter: " + paramStringArray[i] + " is not matched to" +
               		" type: " + paramDefList.get(i).getType().getName();
               logger.warning(error);
               errorMessage.append(error);
               errorMessage.append('\n');
             }
           }
         }
         else {
           isLoadSucceed = false;
           String error = "Error in URL parameters: telemetry parameters: " + 
           parameters.getString(TELEMETRY_PARAMERTERS_KEY) + "(" + paramStringArray.length +
           ") >> number of parameters not matched to definition within telemetry: " + 
           this.telemetryName + "(" + paramDefList.size() + ")";
           logger.warning(error);
           errorMessage.append(error);
           errorMessage.append('\n');
         }
       }
     }
     if (errorMessage.length() > 0) {
       this.paramErrorMessage = errorMessage.toString() + PARAMETER_ORDER_MESSAGE;
     }
     return isLoadSucceed;
   }
 
   /**
    * Checks if the given value is of the given type.
    * @param value the given value.
    * @param type the given type.
    * @return true if the value and type are matched.
    */
   private boolean isValueMatchType(String value, Type type) {
     if ("Enumerated".equals(type.getName())) {
       return type.getValue().contains(value);
     }
     else if ("Boolean".equals(type.getName())) {
       return "true".equals(value) || "false".equals(value);
     }
     else if ("Integer".equals(type.getName())) {
       try {
         Integer.valueOf(value);
         return true;
       }
       catch (NumberFormatException e) {
         return false;
       }
     }
     else if ("Text".equals(type.getName())) {
       return true;
     }
     return false;
   }
   /**
    * Returns a PageParameters instance that represents the content of the input form.
    * @return a PageParameters instance.
    */
   public PageParameters getPageParameters() {
     PageParameters parameters = new PageParameters();
 
     parameters.put(TELEMETRY_KEY, this.getTelemetryName());
     parameters.put(GRANULARITY_KEY, this.getGranularity());
     parameters.put(START_DATE_KEY, this.getFormattedStartDateString());
     parameters.put(END_DATE_KEY, this.getFormattedEndDateString());
     parameters.put(SELECTED_PROJECTS_KEY, this.getSelectedProjectsAsString());
     parameters.put(TELEMETRY_PARAMERTERS_KEY, this.getParametersAsString());
     
     return parameters;
   }
 
   /**
    * @return the paramErrorMessage
    */
   public String getParamErrorMessage() {
     String temp = this.paramErrorMessage;
     this.clearParamErrorMessage();
     return temp;
   }
   /**
    * Clears the paramErrorMessage.
    */
   public void clearParamErrorMessage() {
     this.paramErrorMessage = "";
   }
 
 }
