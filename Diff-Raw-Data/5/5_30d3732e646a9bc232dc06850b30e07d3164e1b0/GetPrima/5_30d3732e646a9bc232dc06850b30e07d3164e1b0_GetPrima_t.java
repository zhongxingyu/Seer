 /*
  =============
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package abelymiguel.miralaprima;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.sql.*;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author refusta
  */
 public class GetPrima extends HttpServlet {
 
     private Connection _con;
     private Statement _stmt;
     private ResultSet _rs;
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request,
             HttpServletResponse response)
             throws ServletException, IOException {
         try {
             _con = Utils.getConnection();
             _stmt = _con.createStatement();
 
         } catch (URISyntaxException ex) {
             Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
         }
         PrintWriter out = response.getWriter();
         response.setContentType("text/javascript;charset=UTF-8");
 
         String country_code;
         country_code = request.getParameter("country_code");
 
         JSONObject jsonObject;
         JSONArray jsonArray;
         String json_str;
         if (country_code != null) {
             jsonObject = new JSONObject(this.getCountry(country_code));
             json_str = jsonObject.toString();
         } else {
             jsonArray = new JSONArray(this.getAllCountries());
             json_str = jsonArray.toString();
         }
 
         String jsonpCallback = request.getParameter("callback");
         if (jsonpCallback != null) {
             out.write(jsonpCallback + "(" + json_str + ")");
         } else {
             out.println(json_str);
         }
         try {
             _con.close();
             _stmt.close();
             _rs.close();
         } catch (SQLException ex) {
             Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
         }
         out.close();
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "This servlet returns the risk premium of a gived country";
     }// </editor-fold>
 
     private HashMap<String, Object> getCountry(String country_code) {
         HashMap<String, Object> respuestaJson;
         if (country_code.equals("ES") || country_code.equals("IT") || country_code.equals("PT") || country_code.equals("GR")) {
             respuestaJson = getCountryBloomberg(country_code);
         } else {
             respuestaJson = getCountryDMacro(country_code);
         }
 
         return respuestaJson;
     }
 
     private HashMap<String, Object> getCountryDMacro(String country_code) {
         HashMap<String, Object> respuestaJson = new HashMap<String, Object>();
 
         String country_prime;
         String name;
 
         String result;
         if (country_code.equals("HU")) {
             country_prime = "hungria";
             name = "Hungria";
         } else if (country_code.equals("IN")) {
             country_prime = "india";
             name = "India";
        } else if (country_code.equals("GB")) {
             country_prime = "uk";
             name = "Reino Unido";
         } else {
             return respuestaJson;
         }
 
         Float prima_value;
         Float prima_delta;
         Float prima_percent;
 
         if (isUpdated(country_code)) {
             respuestaJson = getLatestPrimaFromDB(country_code);
             respuestaJson.put("action", "fromDatabase");
 
         } else {
 
             Document doc;
             try {
                 doc = Jsoup.connect("http://www.datosmacro.com/prima-riesgo/" + country_prime).get();
             } catch (IOException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
                 return getLatestPrimaFromDB(country_code);
             }
 
             try {
                 Element riskPremium = doc.select(".numero").first();
 //                System.out.println("Prima: " + riskPremium.text());
                 prima_value = Float.valueOf(riskPremium.text()).floatValue();
 
                 Element riskDelta = doc.select(".odd").first();
                 String deltaStr = riskDelta.text().substring(riskDelta.text().lastIndexOf(" "));
                 prima_delta = Float.valueOf(deltaStr).floatValue();
 //                System.out.println("Trending delta: " + prima_delta);
 
                 String percentStr;
                 prima_percent = 100 * prima_delta / (prima_value - prima_delta);
                 DecimalFormat df = new DecimalFormat("0.00");
                 percentStr = df.format(prima_percent);
                 prima_percent = Float.valueOf(percentStr).floatValue();
                 //                System.out.println("Trending prima_percent: " + prima_percent);
 
                 respuestaJson.put("name", name);
                 respuestaJson.put("country_code", country_code);
                 respuestaJson.put("prima_value", prima_value);
                 respuestaJson.put("prima_delta", prima_delta);
                 respuestaJson.put("prima_percent", prima_percent);
 
                 if (isSameDay(country_code)) {
                     result = this.updatePrimaInDB(prima_value, prima_delta, prima_percent, this.getLatestPrimaIdFromDB(country_code));
                     respuestaJson.put("action", "update");
                     respuestaJson.put("result", result);
                 } else {
                     result = this.storePrimaInDB(prima_value, prima_delta, prima_percent, country_code);
                     respuestaJson.put("action", "store");
                     respuestaJson.put("result", result);
                 }
             } catch (Exception ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
                 return getLatestPrimaFromDB(country_code);
             }
         }
         return respuestaJson;
     }
 
     private HashMap<String, Object> getCountryBloomberg(String country_code) {
         HashMap<String, Object> respuestaJson = new HashMap<String, Object>();
 
         String country_prime;
         String name;
 
         String result;
         if (country_code.equals("ES")) {
             country_prime = "!SPN:IND";
             name = "España";
         } else if (country_code.equals("IT")) {
             country_prime = "!IT10:IND";
             name = "Italia";
         } else if (country_code.equals("GR")) {
             country_prime = "!GRK:IND";
             name = "Grecia";
         } else if (country_code.equals("PT")) {
             country_prime = "!PORT10:IND";
             name = "Portugal";
         } else {
             return respuestaJson;
         }
 
         Float prima_value;
         Float prima_delta;
         Float prima_percent;
 
         if (isUpdated(country_code)) {
             respuestaJson = getLatestPrimaFromDB(country_code);
             respuestaJson.put("action", "fromDatabase");
         } else {
 
             Document doc;
             try {
                 doc = Jsoup.connect("http://www.bloomberg.com/quote/" + country_prime).get();
             } catch (IOException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
                 return getLatestPrimaFromDB(country_code);
             }
 
             try {
                 Element riskPremium = doc.select(".price").last();
 //              System.out.println("Prima: " + riskPremium.text());
                 prima_value = Float.valueOf(riskPremium.text().replace(",", "")).floatValue();
 
                 Elements riskPremiumsUp = doc.select(".trending_up");
                 Elements riskPremiumsDown = doc.select(".trending_down");
 //              System.out.println("Trending: " + riskPremiumsUp.text());
 //              System.out.println("Trending: " + riskPremiumsDown.text());
 
                 if (!riskPremiumsUp.text().equals("")) {
                     String delta = riskPremiumsUp.text();
                     prima_delta = Float.valueOf(delta.substring(0, delta.indexOf(" ")).replace(",", "")).floatValue();
 //                  System.out.println("Delta: " + prima_delta);
 
                     String percent = riskPremiumsUp.text();
                     prima_percent = Float.valueOf(percent.substring(percent.indexOf(" ") + 1, percent.length() - 1)).floatValue();
 //                  System.out.println("Percent: " + prima_percent);
                 } else if (!riskPremiumsDown.text().equals("")) {
                     String delta = riskPremiumsDown.text();
                     prima_delta = Float.valueOf(delta.substring(0, delta.indexOf(" ")).replace(",", "")).floatValue();
                     prima_delta = prima_delta * -1;
 //                  System.out.println("Delta: " + prima_delta);
 
                     String percent = riskPremiumsDown.text();
                     prima_percent = Float.valueOf(percent.substring(percent.indexOf(" ") + 1, percent.length() - 1)).floatValue();
                     prima_percent = prima_percent * -1;
 //                  System.out.println("Percent: " + prima_percent);
                 } else {
                     prima_delta = 0f;
                     prima_percent = 0f;
                 }
                 respuestaJson.put("name", name);
                 respuestaJson.put("country_code", country_code);
                 respuestaJson.put("prima_value", prima_value);
                 respuestaJson.put("prima_delta", prima_delta);
                 respuestaJson.put("prima_percent", prima_percent);
 
                 if (isSameDay(country_code)) {
                     result = this.updatePrimaInDB(prima_value, prima_delta, prima_percent, this.getLatestPrimaIdFromDB(country_code));
                     respuestaJson.put("action", "update");
                     respuestaJson.put("result", result);
                 } else {
                     result = this.storePrimaInDB(prima_value, prima_delta, prima_percent, country_code);
                     respuestaJson.put("action", "store");
                     respuestaJson.put("result", result);
                 }
             } catch (Exception ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
                 return getLatestPrimaFromDB(country_code);
             }
         }
         return respuestaJson;
     }
 
     private ArrayList<HashMap<String, Object>> getAllCountries() {
         ArrayList<HashMap<String, Object>> respuestaJson = new ArrayList<HashMap<String, Object>>();
         ArrayList<String> country_codes = new ArrayList<String>();
         country_codes.add("ES");
         country_codes.add("PT");
         country_codes.add("IT");
         country_codes.add("GR");
         country_codes.add("HU");
         country_codes.add("IN");
        country_codes.add("GB");
 
         for (String country : country_codes) {
             respuestaJson.add(this.getCountry(country));
         }
         return respuestaJson;
     }
 
     private HashMap<String, Object> getLatestPrimaFromDB(String country) {
 
         HashMap<String, Object> respuestaJson = new HashMap<String, Object>();
 
         if (country != null) {
             try {
                 _rs = _stmt.executeQuery("SELECT * FROM `country_values` where `country_code` = '" + country + "' order by id DESC LIMIT 1;");
                 while (_rs.next()) {
                     Float prima_value = _rs.getFloat("prima_value");
                     Float prima_delta = _rs.getFloat("prima_delta");
                     Float prima_percent = _rs.getFloat("prima_percent");
                     String name = this.getNameFromCountryCode(country);
                     respuestaJson.put("prima_value", prima_value);
                     respuestaJson.put("prima_delta", prima_delta);
                     respuestaJson.put("prima_percent", prima_percent);
                     respuestaJson.put("country_code", country);
                     respuestaJson.put("name", name);
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return respuestaJson;
     }
 
     private int getLatestPrimaIdFromDB(String country) {
 
         int id = 0;
 
         if (country != null) {
             try {
 //                Connection con = Utils.getConnection();
 //                Statement stmt = _con.createStatement();
                 _rs = _stmt.executeQuery("SELECT id FROM `country_values` where `country_code` = '" + country + "' order by id DESC LIMIT 1;");
                 while (_rs.next()) {
                     id = _rs.getInt("id");
                 }
 
             } catch (SQLException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return id;
     }
 
     private String getNameFromCountryCode(String country) {
 
         String name = null;
 
         if (country != null) {
             try {
                 _rs = _stmt.executeQuery("SELECT name FROM `countries` WHERE `country_code` = '" + country + "'");
                 while (_rs.next()) {
                     name = _rs.getString("name");
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return name;
     }
 
     private String storePrimaInDB(Float prima, Float delta, Float percent, String country) {
 
         String result;
 
         try {
 
             _stmt.execute("INSERT INTO `heroku_e3c20314821e7d1`.`country_values` (`id`, `country_code`, `prima_value`, `prima_delta`, `prima_percent`, `last_update`) VALUES (null, '" + country + "', " + prima + ", " + delta + ", " + percent + ", '" + this.getTimestamp() + "');");
             result = "OK";
 
         } catch (SQLException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
             result = "ERROR";
         }
         return result;
     }
 
     private String updatePrimaInDB(Float prima, Float delta, Float percent, int id) {
 
         String result;
         Timestamp ts = this.getTimestamp();
         try {
             _stmt.execute("UPDATE `country_values` SET `prima_value`=" + prima + ", `prima_delta`=" + delta + ", `prima_percent`=" + percent + ", `last_update`='" + ts + "' WHERE `id`='" + id + "';");
             result = "OK";
         } catch (SQLException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
             result = "ERROR";
         }
         return result;
     }
 
     private Timestamp getDateOfLastStored(String country) {
 
         Timestamp dateLastStored = null;
 
         if (country != null) {
             try {
                 _rs = _stmt.executeQuery("SELECT last_update FROM `country_values` where `country_code` = '" + country + "' order by id DESC LIMIT 1;");
                 while (_rs.next()) {
                     dateLastStored = _rs.getTimestamp("last_update");
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(GetPrima.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return dateLastStored;
     }
 
     private Boolean isUpdated(String country) {
 
         Boolean isUpdated = false;
 
         Timestamp date_today = this.getTimestamp();
 
         Timestamp dateLastUpdate = this.getDateOfLastStored(country);
         if (dateLastUpdate != null) {
             if (dateLastUpdate.getTime() > date_today.getTime() - 300000) {
                 isUpdated = true;
             }
         }
         return isUpdated;
     }
 
     private Boolean isSameDay(String country) {
 
         Boolean isSameDay = true;
         try {
 
             Timestamp dateToday = this.getTimestamp();
 
             Timestamp dateLastUpdate = this.getDateOfLastStored(country);
 
 //            System.out.println("dateLastUpdate "+ dateLastUpdate);
 
             Calendar calToday = Calendar.getInstance();
             calToday.setTime(dateToday);
 
             Calendar calLastUpdate = Calendar.getInstance();
             calLastUpdate.setTime(dateLastUpdate);
 
             if (calToday.get(java.util.Calendar.DAY_OF_MONTH) == calLastUpdate.get(java.util.Calendar.DAY_OF_MONTH)
                     && calToday.get(java.util.Calendar.MONTH) == calLastUpdate.get(java.util.Calendar.MONTH)
                     && calToday.get(java.util.Calendar.YEAR) == calLastUpdate.get(java.util.Calendar.YEAR)) {
                 isSameDay = true;
             } else {
                 isSameDay = false;
             }
 //            System.out.println("isSameDay "+ isSameDay);
         } catch (Exception e) {
             e.getLocalizedMessage();
         }
 
 
         return isSameDay;
     }
 
     private Timestamp getTimestamp() {
 
         Timestamp date_added;
         java.util.Date date = new java.util.Date();
         date_added = new Timestamp(date.getTime());
         return date_added;
     }
 
     public static void main(String[] args) throws Exception {
         Server server = new Server(Integer.valueOf(System.getenv("PORT")));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new GetPrima()), "/GetPrima");
         context.addServlet(new ServletHolder(new GetMoza()), "/GetMoza");
         context.addServlet(new ServletHolder(new SetMoza()), "/SetMoza");
         server.start();
         server.join();
     }
 }
