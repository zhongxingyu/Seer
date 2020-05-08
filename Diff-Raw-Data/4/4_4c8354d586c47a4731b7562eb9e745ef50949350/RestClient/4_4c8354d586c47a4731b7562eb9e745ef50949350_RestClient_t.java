 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.spt.rms.rep.rest;
 
 import com.spt.rms.rep.domain.Report;
 import com.spt.rms.rep.domain.ReportEnvelop;
 import com.spt.rms.rep.domain.ReportParameter;
 import com.spt.rms.rep.domain.ReportParameterEnvelop;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBElement;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * Panupong Chantaklang
  * @since 04/02/2011
  */
 public class RestClient {
 
     private static Logger logger = LoggerFactory.getLogger(RestClient.class);
     private String URL = "http://localhost:8080/RMSReportGenerator/";
 
     GenericType<JAXBElement<ReportEnvelop>> reportEnvelopType = new GenericType<JAXBElement<ReportEnvelop>>() {};
     GenericType<JAXBElement<Report>> reportType = new GenericType<JAXBElement<Report>>() {};
     GenericType<JAXBElement<ReportParameterEnvelop>> reportParametrEnvelopType = new GenericType<JAXBElement<ReportParameterEnvelop>>(){};
     GenericType<JAXBElement<ReportParameter>> reportParameterType = new GenericType<JAXBElement<ReportParameter>>() {};
 
     public RestClient() {
     }
 
     public RestClient(String ServerURL) {
         URL = ServerURL;
     }
 
     public String getURL() {
         return URL;
     }
 
     public void setURL(String URL) {
         this.URL = URL;
     }
 
 
     //Report REST
     /**
      * list all report
      * @return List of Report
      */
     public List<Report> listAllReport() {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         ReportEnvelop reportEnvelop = r.path("/reports/all").accept(MediaType.APPLICATION_XML_TYPE).get(reportEnvelopType).getValue();
         return reportEnvelop.getReportList()==null?Collections.EMPTY_LIST:reportEnvelop.getReportList();
     }
 
     /**
      * list Report by criteria and automatic assign %criteria%
      * @param reportCode
      * @param reportDescription
      * @return List of Report
      */
     public List<Report> listReportByCriteria(String reportCode, String reportDescription) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
        String code = (reportCode!=null&&reportCode.length()!=0)?reportCode.trim():"%";
        String desc = (reportDescription!=null&&reportDescription.length()!=0)?reportDescription.trim():"%";
 
         ReportEnvelop reportEnvelop = r.path(java.text.MessageFormat.format("/reports/criteria/{0}/{1}", new Object[]{code,desc})).accept(MediaType.APPLICATION_XML_TYPE).get(reportEnvelopType).getValue();
         return reportEnvelop.getReportList() == null ? Collections.EMPTY_LIST : reportEnvelop.getReportList();
     }
 
     /**
      * Get report by ID
      * @param id
      * @return Report
      */
     public Report getReportById(Long id) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         Report report;
         try{
             logger.info("ID : {}",id);
             report = r.path(java.text.MessageFormat.format("/reports/repid={0}", new Object[]{""+id})).accept(MediaType.APPLICATION_XML_TYPE).get(reportType).getValue();
             //report = r.path("/reports/repid="+id).accept(MediaType.APPLICATION_XML_TYPE).get(reportType).getValue();
         }catch(Exception e){
             return null;
         }
         return report;
     }
 
     /**
      * Get report by code  "Column RPRPROGID"
      * @param code
      * @return Report
      */
     public Report getReportByCode(String code) {
         Report report;
         Client c = Client.create();
         WebResource r = c.resource(URL);
 
         try{
             report = r.path(java.text.MessageFormat.format("/reports/repcode={0}", new Object[]{code})).accept(MediaType.APPLICATION_XML_TYPE).get(reportType).getValue();
         } catch (Exception e) {
             return null;
         }
         return report;
     }
 
     /**
      * Add report
      * @param report
      * @return true if success false if not success
      */
     public boolean addReport(Report report) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try {
             r.path("reports/addEntity").type(MediaType.APPLICATION_XML_TYPE).post(reportType,report);
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
     }
 
     /**
      * Update report data
      * @param report
      * @return true if success false if not success
      */
     public boolean updateReport(Report report) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try{
             r.path("reports/updateEntity").accept(MediaType.APPLICATION_XML_TYPE).post(reportType,report);
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
     }
 
     /**
      * Delete report
      * @param report
      * @return true if success false if not success
      */
     public boolean deleteReport(Report report) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try{
             r.path(java.text.MessageFormat.format("/reports/{0}", new Object[]{""+report.getId()})).delete();
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
     }
     //End of Report REST
 
     //Report Parameter REST
     /**
      * List parameter of report order by ReportParameter ID
      * @param report
      * @return List of ReportParameter
      */
     public List<ReportParameter> listReportParameterByReport(Report report) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         ReportParameterEnvelop reportParameterEnvelop = r.path(java.text.MessageFormat.format("reportparameters/report={0}", new Object[]{""+report.getId()})).accept(MediaType.APPLICATION_XML_TYPE).get(reportParametrEnvelopType).getValue();
         List<ReportParameter> resList = Collections.emptyList();
         resList = reportParameterEnvelop.getReportParameterList();
         if(resList!=null&&resList.size()>0){
             Collections.sort(resList,ReportParameter.SequenceComparator);
         }
         return resList;
     }
 
     /**
      * Get report parameter by ID
      * @param id
      * @return ReportParameter
      */
     public ReportParameter getReportParameterById(Long id) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         ReportParameter param;
         try{
             param =  r.path(java.text.MessageFormat.format("reportparameters/paramid={0}", new Object[]{""+id})).accept(MediaType.APPLICATION_XML_TYPE).get(reportParameterType).getValue();
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return null;
         }
         return param;
     }
 
     /**
      * Get report parameter by code
      * @param code
      * @return ReportParameter
      */
     public ReportParameter getReportParameterByCode(String code) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         ReportParameter param;
         try{
             param = r.path(java.text.MessageFormat.format("reportparameters/paramcode={0}", new Object[]{code})).accept(MediaType.APPLICATION_XML_TYPE).get(reportParameterType).getValue();
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return null;
         }
         return param;
     }
 
     /**
      * Add report parameter
      * @param reportParameter
      * @return true if success false if not success
      */
     public boolean addReportParameter(ReportParameter reportParameter) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try {
             r.path("reportparameters/addEntity").accept(MediaType.APPLICATION_XML_TYPE).post(reportParameterType,reportParameter);
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
 
     }
 
     /**
      * Update report parameter
      * @param reportParameter
      * @return true if success false if not success
      */
     public boolean updateReportParameter(ReportParameter reportParameter) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try {
             r.path("reportparameters/updateEntity").accept(MediaType.APPLICATION_XML_TYPE).post(reportParameterType,reportParameter);
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
     }
 
     /**
      * Delete report parameter
      * @param reportParameter
      * @return true if success false if not success
      */
     public boolean deleteReportParameter(ReportParameter reportParameter) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         try {
             r.path(java.text.MessageFormat.format("/reportparameters/{0}", new Object[]{""+reportParameter.getId()})).delete();
             return true;
         } catch (Exception e) {
             logger.error("{}", e.getMessage());
             return false;
         }
 
     }
     //End of Report Parameter REST
 
 
     //Utilities Method
     /**
      * check SQL Statement
      * @param sql
      * @return List of ReportParameter object but assign only 2 value in code and description for build LOV data
      */
     public List<ReportParameter> checkSQLStatement(String sql) {
         Client c = Client.create();
         WebResource r = c.resource(URL);
         ReportParameterEnvelop reportParameterEnvelop = r.path(java.text.MessageFormat.format("reportparameters/check={0}", new Object[]{sql})).accept(MediaType.APPLICATION_XML_TYPE).get(reportParametrEnvelopType).getValue();
         return reportParameterEnvelop.getReportParameterList()==null?Collections.EMPTY_LIST:reportParameterEnvelop.getReportParameterList();
 
     }
 
     /**
      * Generate Report URL for post to Report Server
      * @param reportServerURL
      * @return string of url
      */
     public String generateReportURL(String reportServerURL) {
         return "";
     }
 }
