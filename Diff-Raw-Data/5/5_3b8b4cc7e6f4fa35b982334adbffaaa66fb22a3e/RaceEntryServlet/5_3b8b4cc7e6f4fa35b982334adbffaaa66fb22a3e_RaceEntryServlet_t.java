 /*
  * Copyright (C) 2012 Helsingfors Segelklubb ry
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fi.hoski.web.forms;
 
 import com.google.appengine.api.datastore.*;
 import fi.hoski.datastore.*;
 import fi.hoski.datastore.repository.Messages;
 import fi.hoski.datastore.repository.RaceFleet;
 import fi.hoski.datastore.repository.RaceEntry;
 import fi.hoski.datastore.repository.RaceSeries;
 import fi.hoski.mail.MailService;
 import fi.hoski.mail.MailServiceImpl;
 import fi.hoski.util.*;
 import fi.hoski.web.ServletLog;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.*;
 import java.util.Map.Entry;
 import javax.mail.internet.InternetAddress;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.codec.binary.Base64;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Timo Vesalainen
  */
 public class RaceEntryServlet extends HttpServlet {
 
   private static final String CLASSOPTIONS = "ClassOptions";
   private static final String COOKIENAME = "fi.hoski.RaceEntry";
   private ResourceBundle repositoryBundle;
   private DatastoreService datastore;
   private DSUtils entities;
   private Races races;
   private MailService mailService;
   private BoatInfoFactory boatInfoFactory;
   private boolean useCookies;
 
   @Override
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
 
     repositoryBundle = ResourceBundle.getBundle("fi/hoski/datastore/repository/fields");
     datastore = DatastoreServiceFactory.getDatastoreService();
     entities = new DSUtilsImpl(datastore);
     mailService = new MailServiceImpl();
     LogWrapper log = new ServletLog(this);
     races = new RacesImpl(log, datastore, entities, mailService);
     boatInfoFactory = new BoatInfoFactory(log, new DatastorePersistence(datastore));
     boatInfoFactory.setURL("LYS", msg(Messages.LYSINFOURL), msg(Messages.LYSCLASSINFOURL));
     boatInfoFactory.setURL("IRC", msg(Messages.IRCINFOURL), null);
     boatInfoFactory.setURL("ORC", msg(Messages.ORCINFOURL), null);
    String uc = config.getInitParameter("use-cookies");
    useCookies = Boolean.parseBoolean(uc);
    log("useCookies="+useCookies+" // "+uc);
   }
 
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
     try {
       response.setHeader("Cache-Control", "private");
       JSONObject json = null;
       String ratingSystem = request.getParameter(RaceFleet.RatingSystem);
       if (ratingSystem != null) {
         String classOptions = request.getParameter(CLASSOPTIONS);
         if (classOptions != null) {
           printClassOptions(response, boatInfoFactory.getBoatInfoService(ratingSystem));
           return;
         }
         json = new JSONObject();
         Map<String, Object> biMap = boatInfoFactory.getMap(request.getParameterMap());
         log(biMap.toString());
         if (biMap != null && !biMap.isEmpty()) {
           for (Entry<String, Object> e : biMap.entrySet()) {
             json.put(e.getKey(), e.getValue());
           }
         }
       } else {
         json = fromCookie(request);
         Entity rcEntity = getAncestor(request);
         if (rcEntity != null) {
           json.put(RaceEntry.FLEET, rcEntity.getProperty(RaceFleet.Fleet));
           String boatClass = (String) rcEntity.getProperty(RaceFleet.Class);
           if (boatClass != null) {
             json.put(RaceEntry.CLASS, boatClass);
           }
         }
         ratingSystem = json.optString(BoatInfo.FLEET, null);
         String nat = json.optString(BoatInfo.NAT, null);
         String sailNo = json.optString(BoatInfo.SAILNO, null);
         String boatType = json.optString(BoatInfo.CLASS, null);
         Map<String, Object> biMap = boatInfoFactory.getMap(ratingSystem, nat, sailNo, boatType);
         if (biMap != null && !biMap.isEmpty()) {
           for (Entry<String, Object> e : biMap.entrySet()) {
             json.put(e.getKey(), e.getValue());
           }
         }
       }
       response.setContentType("application/json");
       PrintWriter out = response.getWriter();
       json.write(out);
       out.close();
       log(json.toString());
       String refresh = request.getParameter("refresh");
       if (refresh != null) {
         boatInfoFactory.refresh(refresh);
       }
     } catch (JSONException ex) {
       log(ex.getMessage(), ex);
       throw new ServletException(ex);
     } catch (EntityNotFoundException ex) {
       log(ex.getMessage(), ex);
       throw new ServletException(ex);
     }
   }
 
   private Entity getAncestor(HttpServletRequest request) throws EntityNotFoundException {
     String referer = request.getHeader("referer");
     if (referer != null) {
       int i1 = referer.indexOf("ancestor=");
       if (i1 != -1) {
         String ancestor = null;
         int i2 = referer.indexOf("&", i1);
         if (i2 != -1) {
           ancestor = referer.substring(i1 + 9, i2);
         } else {
           ancestor = referer.substring(i1 + 9);
         }
         Key parent = KeyFactory.stringToKey(ancestor);
         return datastore.get(parent);
       }
     }
     return null;
   }
 
   private String msg(String key) {
     return entities.getMessage(key);
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
     try {
       String raceFleetKeyStr = request.getParameter("RaceFleetKey");
       if (raceFleetKeyStr == null) {
         throw new ServletException("no RaceFleetKey");
       }
       Key raceFleetKey = KeyFactory.stringToKey(raceFleetKeyStr);
       Entity raceFleetEntity = datastore.get(raceFleetKey);
       Key raceSeriesKey = raceFleetKey.getParent();
       Entity raceseriesEntity = datastore.get(raceSeriesKey);
       RaceSeries raceSeries = (RaceSeries) entities.newInstance(raceseriesEntity);
       RaceFleet raceFleet = (RaceFleet) entities.newInstance(raceFleetEntity);
       RaceEntry raceEntry = new RaceEntry(raceFleet);
       raceEntry.populate(request.getParameterMap());
 
       String fn = request.getParameter(RaceEntry.FIRSTNAME);
       String ln = request.getParameter(RaceEntry.LASTNAME);
       raceEntry.set(RaceEntry.HELMNAME, fn + " " + ln);
 
       String sa = request.getParameter(RaceEntry.STREETADDRESS);
       String zc = request.getParameter(RaceEntry.ZIPCODE);
       String ct = request.getParameter(RaceEntry.CITY);
       String cn = request.getParameter(RaceEntry.COUNTRY);
       if (cn == null || cn.isEmpty()) {
         raceEntry.set(RaceEntry.HELMADDRESS, sa + ", " + zc + " " + ct);
       } else {
         raceEntry.set(RaceEntry.HELMADDRESS, sa + ", " + zc + " " + ct + ", " + cn);
       }
 
 
       Day closingDay = (Day) raceSeries.get(RaceSeries.ClosingDate);
       Number fee = 0.0;
       if (closingDay != null) {
         Day now = new Day();
         if (closingDay.before(now)) {
           fee = (Number) raceFleet.get(RaceFleet.Fee2);
         } else {
           fee = (Number) raceFleet.get(RaceFleet.Fee);
         }
       }
       Boolean clubDiscount = (Boolean) raceSeries.get(RaceSeries.CLUBDISCOUNT);
       String clubname = repositoryBundle.getString("Clubname");
       if (clubDiscount != null && clubDiscount && clubname.equalsIgnoreCase("" + raceEntry.get(RaceEntry.CLUB))) {
         fee = new Double(0);
       }
       raceEntry.set(RaceEntry.FEE, fee);
       raceEntry.set(RaceEntry.TIMESTAMP, new Date());
 
       entities.put(raceEntry);
 
       String payingInstructions = "";
       String payingInstructionsHtml = "";
       BankingBarcode bb = races.getBarcode(raceEntry);
       if (bb != null) {
         Day dueDay = new Day(bb.getDueDate());
         String payingFormat = EntityReferences.encode(msg(Messages.RACEENTRYPAYING), "UTF-8");
         String bic = EntityReferences.encode(msg(Messages.RACEBIC), "UTF-8");
         payingInstructions = String.format(payingFormat,
           bb.toString(), // 1 = barcode
           bb.getAccount().getIBAN(), // 2 = account
           bb.getReference().toFormattedRFString(), // 3 = ref
           dueDay, // 4 = due date
           String.format("%.2f", bb.getTotal()), // 5 = total
           bic // 6 = bic
           );
         payingInstructionsHtml = String.format(payingFormat.replace("\n", "<br>"),
           "<span id='barcode'>" + bb.toString() + "</span>", // 1 = barcode
           "<span id='iban'>" + bb.getAccount().getIBAN() + "</span>", // 2 = account
           "<span id='rf'>" + bb.getReference().toFormattedRFString() + "</span>", // 3 = ref
           "<span id='due'>" + dueDay + "</span>", // 4 = due date
           "<span id='fee'>" + String.format("%.2f", bb.getTotal()) + "</span>", // 5 = total
           "<span id='bic'>" + bic + "</span>" // 6 = bic
           );
       }
       URL base = new URL(request.getRequestURL().toString());
       URL barcodeUrl = new URL(base, "/races/code128.html?ancestor=" + raceEntry.createKeyString());
       String name = (String) raceEntry.get(RaceEntry.HELMNAME);
       String email = (String) raceEntry.get(RaceEntry.HELMEMAIL);
       String confirmation = msg(Messages.RACEENTRYCONFIRMATION);
       String plainMessage = "";
       String htmlMessage =
         "<html><head></head><body>"
         + EntityReferences.encode(confirmation)
         + payingInstructionsHtml
         + raceEntry.getFieldsAsHtmlTable()
         + "<iframe src=" + barcodeUrl.toString() + "/>"
         + "</body></html>";
       if (email != null) {
         InternetAddress recipient = new InternetAddress(email, name);
         String senderStr = msg(Messages.RACEENTRYFROMADDRESS);
         InternetAddress sender;
         try {
           sender = new InternetAddress(senderStr);
           plainMessage = confirmation + "\n" + payingInstructions + "\n" + raceEntry.getFields();
 
           String subject = msg(Messages.RACEENTRYSUBJECT);
           mailService.sendMail(sender, subject, plainMessage, htmlMessage, recipient);
         } catch (Exception ex) {
           log(senderStr, ex);
         }
       }
       Cookie cookie = null;
       Cookie[] cookies = null;
       if (useCookies) {
         cookies = request.getCookies();
       }
       if (cookies != null) {
         for (Cookie ck : cookies) {
           if (COOKIENAME.equals(ck.getName())) {
             cookie = ck;
           }
         }
       }
       JSONObject json = null;
       if (useCookies && cookie != null) {
         Base64 decoder = new Base64();
         String str = new String(decoder.decode(cookie.getValue()));
         json = new JSONObject(str);
       } else {
         json = new JSONObject();
       }
       for (Map.Entry<String, String[]> entry : ((Map<String, String[]>) request.getParameterMap()).entrySet()) {
         String property = entry.getKey();
         String[] values = entry.getValue();
         if (values.length == 1) {
           json.put(property, values[0]);
         }
       }
       Base64 encoder = new Base64();
       String base64 = encoder.encodeAsString(json.toString().getBytes("UTF-8"));
       if (useCookies) {
         if (cookie == null) {
           cookie = new Cookie(COOKIENAME, base64);
           cookie.setPath("/");
           cookie.setMaxAge(400 * 24 * 60 * 60);
         } else {
           cookie.setValue(base64);
         }
         response.addCookie(cookie);
       }
       sendError(response, HttpServletResponse.SC_OK,
         "<div id=\"" + raceEntry.createKeyString() + "\">Ok</div>");
     } catch (JSONException ex) {
       log(ex.getMessage(), ex);
       sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
         "<div id=\"eJSON\">Internal error.</div>");
     } catch (EntityNotFoundException ex) {
       log(ex.getMessage(), ex);
       sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
         "<div id=\"eEntityNotFound\">Internal error.</div>");
     } catch (NumberFormatException ex) {
       log(ex.getMessage(), ex);
       sendError(response, HttpServletResponse.SC_CONFLICT,
         "<div id=\"eNumberFormat\">Number error.</div>");
     }
   }
 
   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
     return "Short description";
   }
 
   private String normalizeNumber(String str, String format, Locale locale) {
     if (str.trim().isEmpty()) {
       return "";
     }
     float f = Float.parseFloat(str.replace(',', '.'));
     return String.format(locale, format, f);
   }
 
   private JSONObject fromCookie(HttpServletRequest request) throws JSONException {
     if (useCookies) {
       Cookie[] cookies = request.getCookies();
       if (cookies != null) {
         for (Cookie cookie : cookies) {
           if (COOKIENAME.equals(cookie.getName())) {
             Base64 decoder = new Base64();
             try {
               return new JSONObject(new String(decoder.decode(cookie.getValue()), "UTF-8"));
             } catch (UnsupportedEncodingException ex) {
               log(ex.getMessage(), ex);
               return new JSONObject();
             }
           }
         }
       }
     }
     return new JSONObject();
 
   }
 
   private void printClassOptions(HttpServletResponse response, BoatInfo boatInfo) throws IOException {
     response.setContentType("application/xhtml+xml");
     response.setCharacterEncoding("UTF-8");
     PrintWriter writer = response.getWriter();
     writer.println("<select>");
     for (String boatClass : boatInfo.getBoatTypes()) {
       writer.println("<option>" + EntityReferences.encode(boatClass, "UTF-8") + "</option>");
     }
     writer.println("</select>");
     writer.close();
   }
 
   private void sendError(HttpServletResponse response,
     int statusCode,
     String htmlMessage) throws IOException {
     response.setStatus(statusCode);
     response.setContentType("text/html");
     response.setCharacterEncoding("utf-8");
     response.getWriter().write(htmlMessage);
   }
 }
