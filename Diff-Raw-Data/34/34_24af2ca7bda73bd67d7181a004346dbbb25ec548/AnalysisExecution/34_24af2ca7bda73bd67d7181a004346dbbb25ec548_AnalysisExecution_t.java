 package org.iplantc.de.client.models;
 
 import java.util.Date;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.util.DateParser;
 
 import com.google.gwt.json.client.JSONObject;
 
 /**
  * Models an execution of an Analysis
  * 
  * @author sriram
  * 
  */
 public class AnalysisExecution extends DEBaseModelData {
 
     /**
 	 * 
 	 */
     private static final long serialVersionUID = 1L;
 
     /**
      * create an new Analysis execution
      */
     public AnalysisExecution(JsAnalysisExecution exec) {
         super(exec.getId(), exec.getName());
 
         setStartDate(DateParser.parseDate(exec.getStartDate()));
         setEndDate(DateParser.parseDate(exec.getEndDate()));
         setAnalysisId(exec.getAnalysisId());
         setAnalysisName(exec.getAnalysisName());
         setAnalysisDetails(exec.getAnalysisDetails());
         setStatus(exec.getStatus());
         setDescription(JsonUtil.formatString(exec.getDescription()));
         setResultFolderId(exec.getResultFolderId());
         setWikiUrl(exec.getWikiUrl());
     }
 
     /**
      * create an new Analysis execution
      */
     public AnalysisExecution() {
 
     }
 
     public AnalysisExecution(JSONObject payload) {
         setId(JsonUtil.getString(payload, "id")); //$NON-NLS-1$
         setName(JsonUtil.getString(payload, "name")); //$NON-NLS-1$
         setAnalysisId(JsonUtil.getString(payload, "analysis_id")); //$NON-NLS-1$
         setAnalysisName(JsonUtil.getString(payload, "analysis_name")); //$NON-NLS-1$
         setAnalysisDetails(JsonUtil.getString(payload, "analysis_details")); //$NON-NLS-1$
         setStartDate(DateParser.parseDate(JsonUtil.getString(payload, "startdate"))); //$NON-NLS-1$
         setEndDate(DateParser.parseDate(JsonUtil.getString(payload, "enddate"))); //$NON-NLS-1$
         setStatus(JsonUtil.getString(payload, "status")); //$NON-NLS-1$
         setResultFolderId(JsonUtil.getString(payload, "resultfolderid")); //$NON-NLS-1$
         setWikiUrl(JsonUtil.getString(payload, "wiki_url")); //$NON-NLS-1$
     }
 
     public void setWikiUrl(String url) {
         set("wiki_url", url);//$NON-NLS-1$
     }
 
     public String getResultFolderId() {
         return get("resultfolderid").toString(); //$NON-NLS-1$
     }
 
     public Date getStartDate() {
         return get("startdate"); //$NON-NLS-1$
     }
 
     public Date getEndDate() {
         return get("enddate"); //$NON-NLS-1$
     }
 
     public String getAnalysisId() {
         return get("analysis_id").toString(); //$NON-NLS-1$
     }
 
     public String getAnalysisName() {
         return get("analysis_name").toString(); //$NON-NLS-1$
     }
 
     public String getAnalysisDetails() {
         return get("analysis_details").toString(); //$NON-NLS-1$
     }
 
     public String getStatus() {
         return get("status").toString(); //$NON-NLS-1$
     }
 
     public String getDescription() {
         return get("description").toString(); //$NON-NLS-1$
     }
 
     public void setStartDate(Date startdate) {
         set("startdate", startdate); //$NON-NLS-1$
     }
 
     public void setEndDate(Date enddate) {
         set("enddate", enddate); //$NON-NLS-1$
     }
 
     public void setAnalysisId(String analysis_id) {
         set("analysis_id", analysis_id); //$NON-NLS-1$
     }
 
     public void setAnalysisName(String analysisname) {
         set("analysis_name", analysisname); //$NON-NLS-1$
     }
 
     public void setAnalysisDetails(String analysis_details) {
         set("analysis_details", analysis_details); //$NON-NLS-1$
     }
 
     public void setStatus(String status) {
         set("status", status); //$NON-NLS-1$
     }
 
     public void setDescription(String description) {
         set("description", description); //$NON-NLS-1$
     }
 
     public void setId(String id) {
         set("id", id); //$NON-NLS-1$
     }
 
     public void setName(String name) {
         set("name", name); //$NON-NLS-1$
     }
 
     public void setResultFolderId(String resultfolderid) {
        if (resultfolderid != null) {
            // Trim the path and remove any trailing slashes
            resultfolderid = resultfolderid.trim().replaceAll("/+$", ""); //$NON-NLS-1$//$NON-NLS-2$
        }

         set("resultfolderid", resultfolderid); //$NON-NLS-1$
     }
 
     public String getWikiUrl() {
         return get("wiki_url"); //$NON-NLS-1$
     }
 
 }
