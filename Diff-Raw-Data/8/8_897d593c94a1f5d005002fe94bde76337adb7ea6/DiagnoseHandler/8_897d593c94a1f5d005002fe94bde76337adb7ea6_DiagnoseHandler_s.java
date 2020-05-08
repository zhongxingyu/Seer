 package eu.neq.mais.request;
 
 import eu.neq.mais.NeqServer;
 import eu.neq.mais.connector.Connector;
 import eu.neq.mais.connector.ConnectorFactory;
 import eu.neq.mais.domain.Diagnose;
 import eu.neq.mais.technicalservice.DTOWrapper;
 import eu.neq.mais.technicalservice.SessionStore.NoSessionInSessionStoreException;
 import eu.neq.mais.technicalservice.Settings;
 import org.eclipse.jetty.server.Response;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * @author Denny, Jan
  */
 @Path("/diagnose/")
 public class DiagnoseHandler {
 
     protected static Logger logger = Logger.getLogger("eu.neq.mais.request");
 
     private Connector connector;
 
 
     @OPTIONS
     @Path("/one")
     public Response optionsOne(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
 
     }
 
     @GET
     @Path("/one")
     @Produces(MediaType.APPLICATION_JSON)
     public String returnDiagnose(@Context HttpServletResponse servlerResponse,
                                  @QueryParam("session") String session, @QueryParam("id") String id) {
 
         String response = new DTOWrapper().wrapError("Error while retrieving Diagnose");
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
         try {
             connector = ConnectorFactory.getConnector(NeqServer.getSessionStore().getBackendSid(session));
             Diagnose diagnose = connector.returnDiagnose(id);
             response = new DTOWrapper().wrap(diagnose);
         } catch (Exception e) {
             e.printStackTrace();
 
         } catch (NoSessionInSessionStoreException e) {
             response = new DTOWrapper().wrapError(e.toString());
         }
         logger.info("return diagnose method returned json object: " + response);
 
 
         return response;
 
     }
 
     @OPTIONS
     @Path("/all")
     public Response optionsAll(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
 
     }
 
     @GET
     @Path("/all")
     @Produces(MediaType.APPLICATION_JSON)
     public String returnDashboardData(@Context HttpServletResponse servlerResponse, @QueryParam("session") String session,
                                       @QueryParam("id") String patientId) {
 
         String response = new DTOWrapper().wrapError("Error while retrieving DashboardData");
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
         try {
             connector = ConnectorFactory.getConnector(NeqServer.getSessionStore().getBackendSid(session));
             List<?> dashboard = connector.returnDiagnosesForPatient(session, patientId);
             response = new DTOWrapper().wrap(dashboard);
         } catch (Exception e) {
             e.printStackTrace();
         } catch (NoSessionInSessionStoreException es) {
             response = new DTOWrapper().wrapError(es.toString());
         }
         logger.info("return diagnose method returned json object: " + response);
 
 
         return response;
 
 
     }
 
 
     @OPTIONS
     @Path("/procedures")
     public Response proceduresOptions(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
 
     }
 
     @GET
     @Path("/procedures")
     @Produces(MediaType.APPLICATION_JSON)
     public String returnProcedures(@Context HttpServletResponse servlerResponse, @QueryParam("session") String session) {
 
         String response = new DTOWrapper().wrapError("Error while retrieving procedures");
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
         try {
             connector = ConnectorFactory.getConnector(NeqServer.getSessionStore().getBackendSid(session));
             List<?> resultList = connector.returnProcedures();
             response = new DTOWrapper().wrap(resultList);
         } catch (Exception e) {
             e.printStackTrace();
         } catch (NoSessionInSessionStoreException es) {
             response = new DTOWrapper().wrapError(es.toString());
         }
         logger.info("return procedures method returned json object: " + response);
 
 
         return response;
 
 
     }
 
 
     @OPTIONS
     @Path("/diseases")
     public Response diseasesOptions(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
 
     }
 
     @GET
     @Path("/diseases")
     @Produces(MediaType.APPLICATION_JSON)
     public String returnDiseases(@Context HttpServletResponse servlerResponse, @QueryParam("session") String session) {
 
         String response = new DTOWrapper().wrapError("Error while retrieving procedures");
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
         try {
             connector = ConnectorFactory.getConnector(NeqServer.getSessionStore().getBackendSid(session));
             List<?> resultList = connector.returnDiseases();
             response = new DTOWrapper().wrap(resultList);
         } catch (Exception e) {
             e.printStackTrace();
         } catch (NoSessionInSessionStoreException es) {
             response = new DTOWrapper().wrapError(es.toString());
         }
         logger.info("return diseases method returned json object: " + response);
 
 
         return response;
 
 
     }
 
     @OPTIONS
     @Path("/create")
     public Response createDiagnoseOptions(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
     }
 
 
     // http://localhost:8080/diagnose/create?session=SESSION&status=STATUS&is_allergy...
 
     @POST
     @Path("/create")
     @Produces(MediaType.APPLICATION_JSON)
     public String createDiagnose(
             @Context HttpServletResponse servlerResponse,
             @QueryParam("session") String session,
             @QueryParam("status") String status,
             @QueryParam("is_allergy") boolean is_allergy,
             @QueryParam("doctor") Integer doctor,
             @QueryParam("pregnancy_warning") boolean pregnancy_warning,
             @QueryParam("age") Integer age,
             @QueryParam("weeks_of_pregnancy") Integer weeks_of_pregnancy,
             @QueryParam("date_start_treatment") String date_start_treatment,
             @QueryParam("short_comment") String short_comment,
             @QueryParam("is_on_treatment") boolean is_on_treatment,
             @QueryParam("is_active") boolean is_active,
             @QueryParam("diagnosed_date") String diagnosed_date,
             @QueryParam("treatment_description") String treatment_description,
             @QueryParam("healed_date") String healed_date,
             @QueryParam("date_stop_treatment") String date_stop_treatment,
             @QueryParam("pcs_code") Integer pcs_code,
             @QueryParam("pathology") Integer pathology,
             @QueryParam("allergy_type") String allergy_type,
             @QueryParam("disease_severity") String disease_severity,
             @QueryParam("is_infectious") boolean is_infectious,
             @QueryParam("extra_info") String extra_info,
            @QueryParam("patient_id") Integer patient_id,
            @QueryParam("disease_id") Integer disease_id) {
 
         String response = new DTOWrapper()
                 .wrapError("Error while creating lab test requests");
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods",
                 "POST,GET,PUT,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", "*");
         servlerResponse.addHeader("Access-Control-Allow-Headers",
                 "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
         try {
 
             connector = ConnectorFactory.getConnector(NeqServer
                     .getSessionStore().getBackendSid(session));
 
             Map<Object, Object> params = new HashMap<Object, Object>();
 
             params.put("status", status); //e.g. c
             params.put("is_allergy", is_allergy); //e.g. true
             params.put("doctor", doctor); //e.g. 1
             params.put("pregnancy_warning", pregnancy_warning); //e.g. true
             params.put("age", age); //e.g. 15
             params.put("weeks_of_pregnancy", weeks_of_pregnancy); //e.g. 10
             params.put("date_start_treatment", date_start_treatment); //e.g. 489534758098
             params.put("short_comment", short_comment); //e.g. text
             params.put("is_on_treatment", is_on_treatment); //e.g. true
             params.put("is_active", is_active); //e.g. true
             params.put("diagnosed_date", diagnosed_date); //e.g. 489534758098
             params.put("treatment_description", treatment_description); //e.g. text
             params.put("healed_date", healed_date); //e.g. 489534758098
             params.put("date_stop_treatment", date_stop_treatment); //e.g. 489534758098
             params.put("pcs_code", pcs_code); //e.g. 5
             params.put("pathology", pathology); //e.g. 11
             params.put("allergy_type", allergy_type); //e.g. fa
             params.put("disease_severity", disease_severity); //e.g. 3_sv
             params.put("is_infectious", is_infectious); // e.g. true
             params.put("extra_info", extra_info); // e.g. extra info text


             List<?> labTestCreationSuccessMessage = connector
                     .createDiagnose(params);
 
             response = new DTOWrapper().wrap(labTestCreationSuccessMessage);
         } catch (Exception e) {
             e.printStackTrace();
 
         } catch (NoSessionInSessionStoreException e) {
             response = new DTOWrapper().wrapError(e.toString());
         }
         logger.info("return message of create medication method: " + response);
         return response;
 
     }
 
 }
