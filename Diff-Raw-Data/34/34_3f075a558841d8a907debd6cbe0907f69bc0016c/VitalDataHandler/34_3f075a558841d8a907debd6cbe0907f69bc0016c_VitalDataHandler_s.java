 package eu.neq.mais.request;
 
 import eu.neq.mais.domain.VitalData;
 import eu.neq.mais.domain.gnuhealth.VitalDataGnu;
 import eu.neq.mais.technicalservice.DTOWrapper;
 import eu.neq.mais.technicalservice.Settings;
 import eu.neq.mais.technicalservice.storage.DbHandler;
import org.eclipse.jetty.server.Response;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 29.04.12
  * Time: 11:20
  * To change this template use File | Settings | File Templates.
  */
 
 @Path("/vitaldata")
 public class VitalDataHandler {
 
 
     protected static Logger logger = Logger.getLogger("eu.neq.mais.request");
 
 
     @OPTIONS
     @Path("/")
     public Response getVitalDataOptionsForPatient(@Context HttpServletResponse servlerResponse) {
 
         servlerResponse.addHeader("Allow-Control-Allow-Methods", "POST,GET,OPTIONS");
         servlerResponse.addHeader("Access-Control-Allow-Credentials", "true");
         servlerResponse.addHeader("Access-Control-Allow-Origin", Settings.ALLOW_ORIGIN_ADRESS);
         servlerResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
         servlerResponse.addHeader("Access-Control-Max-Age", "60");
 
 
         return null;
     }
 
 
     @GET
     @Path("/")
     @Produces(MediaType.APPLICATION_JSON)
    public String getVitalDataForPatient(@QueryParam("session") String sessionString, @QueryParam("patientId") String patient_id,
                                          @QueryParam("start_Date") String startDate, @QueryParam("end_Date") String endDate) {
 
 
         String response;
        //Parse Dates
 
 
         //Check for right assignmrnt of values
         //Date startDate_sql = new Date(startDate.getMonth(), startDate.getDay(), startDate.getYear());
         //Date endDate_sql = new Date(endDate.getMonth(), endDate.getDay(), endDate.getYear());
         Calendar startDate_sql = null;
         Calendar endDate_sql = null;
 
 
         SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");
         try {
             startDate_sql = Calendar.getInstance();
 
             startDate_sql.setTime(sdfToDate.parse(startDate));
 
             endDate_sql = Calendar.getInstance();
             endDate_sql.setTime(sdfToDate.parse(endDate));
 
             System.out.print(endDate_sql.getTime());
             System.out.print(startDate_sql.getTime());
 
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         try {
 
             DbHandler dbh = new DbHandler();
 
 
             List<eu.neq.mais.technicalservice.storage.VitalData> vitalDataEntries;
 
 
             if (dbh.getUserItemsCount(patient_id) == 0) {
                 System.out.println(dbh.getUserItemsCount(patient_id));
                 dbh.insertVitalData(patient_id);
 
             }
             vitalDataEntries = dbh.getVitalData(String
                     .valueOf(patient_id), startDate_sql, endDate_sql);
 
 
             List<VitalData> vitalDataListGnu = new ArrayList<VitalData>();
 
 
             for (eu.neq.mais.technicalservice.storage.VitalData e : vitalDataEntries) {
                 vitalDataListGnu.add(new VitalDataGnu(
                         e.getUser_id(),
                         e.getBmi(),
                         e.getTemprature(),
                         e.getPulse(),
                         e.getFluid_balance(),
                         Long.valueOf(e.getDate())
                 ));
 
             }
 
 
             dbh.close();
 
             response = new DTOWrapper().wrap(vitalDataListGnu);
         } catch (Exception e) {
             e.printStackTrace();
 
             response = new DTOWrapper().wrapError("Error while retrieving vital data information: " + e.toString());
         }
         return response;
         //return "test";
     }
 
 }
