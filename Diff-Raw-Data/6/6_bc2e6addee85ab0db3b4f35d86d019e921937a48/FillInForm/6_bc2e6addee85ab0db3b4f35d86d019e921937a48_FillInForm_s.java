 
 import DataDAO.*;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import javax.servlet.RequestDispatcher;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  *
  * @author Michelle
  */
 public class FillInForm extends HttpServlet {
 
     private Connection con;
     private String dbuser = null;
     private String dbpw = null;
 
     @Override
     public void init() throws ServletException {
 
         dbuser = getInitParameter("dbUser");
         dbpw = getInitParameter("dbPassword");
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         java.util.Date utilDate = new java.util.Date();
         java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
         int id = -1;
         String volunteerIdStr = null;
         String volunteerName = null;
         //Date startTime;
         //Date endTime;
         String streetName = null;
         String houseNum = null;
         String apt = null;
         String city = null;
         String state = null;
         String zip = null;
         String address = null;
         String firstName = null;
         String lastName = null;
         String dwellingType = null;
         String ownerInfo = null;
         String landlordName = null;
         String landlordPhone = null;
         String insuranceInfo_f = null;
         String insuranceInfo_s = null;
         String insuranceInfo_c = null;
         String floorNum = null;
         String isBasement = null;
         String isBasementOccupied = null;
         String basementComment = null;
         String waterLiving = null;
         String waterBasement = null;
         String isElectricOn = null;
         String isGasOn = null;
         String Electrical_service_box = null;
         String Furnace = null;
         String Heat_Water_Heater = null;
         String Washer = null;
         String Dryer = null;
         String Stove = null;
         String Regfrigerator = null;
         String classification = null;
         String reason = null;
         String comments = null;
         int num_of_floor = -1;
         int waterLivingInt = -1;
         int waterBasementInt = -1;
 
 
         String jsonArray = (String) request.getParameter("recordJSON");
 
         try {
             JSONArray x = new JSONArray(jsonArray);
             for (int i = 0; i < x.length(); i++) {
                 JSONObject j = x.getJSONObject(0);
 
                 id = (Integer) j.get("id");
                 System.out.println("id = " + id);
                 volunteerIdStr = (String) j.get("volunteer_id");
                 int volunteerId = Integer.parseInt(volunteerIdStr);
                 volunteerName = (String) j.get("volunteer_name");
                 //Date startTime= (Date)j.get("start_time");
                 //Date endTime= (Date)j.get("end_time");
                 streetName = (String) j.get("street_name");
                 houseNum = (String) j.get("tel_house_num");
                 apt = (String) j.get("tel_apt");
                 city = (String) j.get("txt_city");
                 state = (String) j.get("txt_state");
                 zip = (String) j.get("tel_zip");
                 firstName = (String) j.get("txt_first_name");
                 lastName = (String) j.get("txt_last_name");
                 landlordName = (String) j.get("landlord_name");
                 landlordPhone = (String) j.get("landlord_tel");
                 dwellingType = (String) j.get("dwelling_type");
                 insuranceInfo_f = (String) j.get("insurance_information_f");
                 insuranceInfo_s = (String) j.get("insurance_information_s");
                 insuranceInfo_c = (String) j.get("insurance_information_c");
                 ownerInfo = (String) j.get("owner_information");
                 floorNum = (String) j.get("number_floors");
                 if (floorNum != null && !floorNum.equals("")) {
                     num_of_floor = Integer.parseInt(floorNum);
                 }
                 isBasement = (String) j.get("is_basement");
                 isBasementOccupied = (String) j.get("is_basement_occupied");
                 basementComment = (String) j.get("txtarea_basement_comment");
                 waterLiving = (String) j.get("water_in_living_area");
                 if (waterLiving != null && !waterLiving.equals("")) {
                     waterLivingInt = Integer.parseInt(waterLiving);
                 }
                 if (waterBasement != null && !waterBasement.equals("")) {
                     waterBasementInt = Integer.parseInt(waterBasement);
                 }
                waterBasement = (String) j.get("water_in_basement");
                 isElectricOn = (String) j.get("electric");
                 isGasOn = (String) j.get("gas");
                 Electrical_service_box = (String) j.get("d_electrical");
                 Furnace = (String) j.get("d_furnace");
                 Heat_Water_Heater = (String) j.get("d_water");
                 Washer = (String) j.get("d_washer");
                 Dryer = (String) j.get("d_dryer");
                 Stove = (String) j.get("d_stove");
                 Regfrigerator = (String) j.get("d_fridge");
                 classification = (String) j.get("select_classification");
                 reason = (String) j.get("txtArea_classification_reason");
                 comments = (String) j.get("txtArea_comment");
 
 //            }
 //        } catch (JSONException e) {
 //            request.setAttribute("FormSubmitMessage", "You failed to submit the form!");
 //            //System.out.println(e);
 //        }
 //
 //        try {
                 Class.forName("com.mysql.jdbc.Driver");
                 String connectionStr = "jdbc:mysql://localhost/DisasterAssessment";
                 con = DriverManager.getConnection(connectionStr, dbuser, dbpw);
                 Client client = new Client(address, apt, city, state, zip, "", "", lastName, firstName);
                 client.Insert(con);
                 int clientId = client.getId(con);
                 System.out.println("clientId:" + clientId);
 
                 Building building = new Building(landlordName, landlordPhone, dwellingType, insuranceInfo_f, insuranceInfo_s, insuranceInfo_c, ownerInfo);
                 building.Insert(con);
 
                 int buildId = building.getId(con);
                 System.out.println("BuildingId:" + buildId);
 
                 DamageAssessment ds = new DamageAssessment(classification, "", "", num_of_floor, isBasement, waterLivingInt, waterBasementInt, isGasOn, isElectricOn, isBasementOccupied, basementComment, reason,
                         Electrical_service_box, Furnace, Heat_Water_Heater, Washer, Dryer, Stove, Regfrigerator, sqlDate, sqlDate);
                 ds.insert(con);
                 int damageAssessmentId = ds.getId(con);
 
                 Cases caseInstance = new Cases(comments, clientId, damageAssessmentId, buildId, -1);
                 caseInstance.Insert(con);
             }
             request.setAttribute("FormSubmitMessage", "Your Assessment Form has been submitted successfully!");
 
         } catch (Exception e) {
             request.setAttribute("FormSubmitMessage", "You failed to submit the form!");
         } finally {
             String destination = "/welcome.jsp";
             //request.setAttribute("FormSubmitMessage", "Your Assessment Form has been submitted successfully!");
             RequestDispatcher red = getServletContext().getRequestDispatcher(destination);
             red.forward(request, response);
             try {
                 if (con != null) {
                     con.close();
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(FillInForm.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
 
 
 
 
     }
 }
