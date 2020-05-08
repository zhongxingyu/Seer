 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.plugins.trace;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import silvertrout.commons.EscapeUtils;
 import silvertrout.commons.ConnectHelper;
 import silvertrout.Channel;
 import silvertrout.User;
 
 import silvertrout.commons.Base64Coder;
 
 import java.util.HashMap;
 
 /**
  * JBT-plugin to fetch a web page's title and print it to the channel
  * 
  * Beta version with URL and URLConnection support. Tries to do some charset
  * conversion based on HTTP headers. TODO: Check meta tag for additional
  * information about charset.
  *
  * @author reggna
  * @author tigge
  */
 public class Trace extends silvertrout.Plugin {
     public static String substring(String s, String start, String end){
         if(s.contains(start))
             s = s.substring(s.indexOf(start)+start.length());
         else return "";
         
         if(s.contains(end))
             return EscapeUtils.unescapeHtml(s.substring(0,s.indexOf(end)));
         else return "";
     }
 
     @Override
     public void onPrivmsg(User user, Channel channel, String message) {
         if (channel == null || !message.startsWith("!trace")) return;
         /* fix a hashmap to store information about the person */
         HashMap<String, String> personInfo = new HashMap<String, String>();
         personInfo.put("search", message.substring(7));
         fetchFromEniro(personInfo);
         /* did something go wrong? */
         if(!personInfo.containsKey("firstname")){
             /* then we try hitta */
             fetchFromHitta(personInfo);
         }
         /* still no match, then return */
         if(!personInfo.containsKey("firstname")){
             channel.sendPrivmsg(user.getNickname() + ": N.N.");
             return;
         }
 
         /* fetch ssn from upplysning */
         getSSN(personInfo);
         String send = user.getNickname() + ": "
                 + personInfo.get("firstname") + " "
                 + personInfo.get("lastname") + ", "
                 + personInfo.get("address") + ", "
                 + personInfo.get("zipCode") + " "
                 + personInfo.get("locality") + " "
                 + personInfo.get("ssn");
         if(send.contains("\\")){
             channel.sendPrivmsg("ERROR!");
             return;
         }
         channel.sendPrivmsg(send);
 
         // Fetch operator for a number
         String operator = OperatorFinder.getOperator(personInfo.get("search"));
         if(operator != null) {
             channel.sendPrivmsg(user.getNickname() + ": "
                     + personInfo.get("search") + ": " + operator);
         }
 
         // Fetch ratsit information
         if(personInfo.containsKey("ssn") && !personInfo.get("ssn").equals("")) {
             String info = RatsitFinder.getInformation(personInfo.get("ssn"));
             if(!info.equals(""))
                 channel.sendPrivmsg(user.getNickname() + ": " + info);
         }
     }
 
    public static String getTokenFromUpplysning(){
        String site = ConnectHelper.Connect("http", "upplysning.se", "/",
                80, 16384, null, null);
        return substring(site, "name=\"x\" value=\"", "\"");
    }

     public static void getSSN(HashMap<String,String> personInfo){
         String ssn = "", url = "", upplysning = "";
        String x = getTokenFromUpplysning();
         if(personInfo.containsKey("ssn")){
         try{
             url = "/search.aspx?searchkey=26722183&bsa=S%F6k&tab=person&f="
                 + java.net.URLEncoder.encode(personInfo.get("firstname"), "iso-8859-1")
                 + "&l=" + java.net.URLEncoder.encode(personInfo.get("lastname"), "iso-8859-1")
                 + "&s=" + personInfo.get("ssn");
         } catch(UnsupportedEncodingException e){ /* not possible  */}
         System.out.println(url);
         upplysning = ConnectHelper.Connect("http",
                 "www.upplysning.se", url, 80, 16384, null, null);
         }else{
             try{
                 url = "/search.aspx?bs=S%F6k&what="
                     + java.net.URLEncoder.encode(personInfo.get("firstname") + " "
                     + personInfo.get("lastname"), "iso-8859-1") + "&where="
                     + java.net.URLEncoder.encode(personInfo.get("address")
                     + ", " + personInfo.get("zipCode") + " "
                     + personInfo.get("locality"), "iso-8859-1");
             } catch(UnsupportedEncodingException e){ /* not possible  */}
             upplysning = ConnectHelper.Connect("http",
                     "www.upplysning.se", url, 80, 16384, null, null);
             if(!upplysning.contains("<a href=\"show.aspx?id=")){
                 try{
                     url = "/search.aspx?bs=S%F6k&what="
                         + java.net.URLEncoder.encode(personInfo.get("firstname")
                         + " " + personInfo.get("lastname"), "iso-8859-1")
                         + "&where="+ java.net.URLEncoder.encode(
                         personInfo.get("zipCode") + " "
                         + personInfo.get("locality"), "iso-8859-1");
                 }catch(UnsupportedEncodingException e){ /* not possible  */}
                 upplysning = ConnectHelper.Connect("http", "www.upplysning.se",
                         url, 80, 16384, null, null);
             }
         }
         System.out.println(url);
         if(upplysning.contains("<a href=\"show.aspx?id=")){
             try{
                 ssn = Base64Coder.decodeString(substring(upplysning,
                     "<a href=\"show.aspx?id=","\"")).replaceAll(
                     "\\D", "");
             personInfo.put("ssn", ssn.substring(2, 8) + "-" + ssn.substring(8));
             System.out.println("out: "+ ssn);
             }catch(Exception e){
                 personInfo.put("ssn", "");
             }
         }
     }
 
     public static void fetchFromEniro(HashMap<String, String> personInfo){
         String message = personInfo.get("search");
         if(message == null || message.equals("")) return;
         String url = "";
         try{
             url = "/resultat/" + java.net.URLEncoder.encode(message,
                     "iso-8859-1");
         }catch(UnsupportedEncodingException e){ return;  /* not possible  */}
         /* fetch information from eniro */
         String eniro = ConnectHelper.Connect("http", "personer.eniro.se", url,
                 80, 16384, null, null);
         /* make sure we did get a hit: */
         if(eniro.contains("ingen träff")) return;
         // person or organization?
         if(eniro.contains("given-name")){
             personInfo.put("firstname", substring(eniro,
                     "<span class=\"given-name\">","<"));
             personInfo.put("lastname", substring(eniro,
                     "<span class=\"family-name\">","<"));
         } else {
             personInfo.put("firstname", substring(eniro,
                     "org\">","<"));
             personInfo.put("lastname", "");
         }
         personInfo.put("address", substring(eniro,
                 "<span class=\"street-address\">", "<"));
         personInfo.put("zipCode", substring(eniro,
                 "<span class=\"postal-code\">", "<"));
         personInfo.put("locality", substring(eniro,
                 "<span class=\"locality\">", "<"));
         /*System.out.println("firstname:" + personInfo.get("firstname"));
         System.out.println("lastname:" + personInfo.get("lastname"));
         System.out.println("address:" + personInfo.get("address"));
         System.out.println("zipCode:" + personInfo.get("zipCode"));
         System.out.println("locality:" + personInfo.get("locality"));
          */
     }
 
     public static void fetchFromHitta(HashMap<String, String> personInfo){
         String message = personInfo.get("search");
         if(message == null || message.equals("")) return;
         String url = "";
         try{
             url = "/SearchMixed.aspx?vad="
                     + java.net.URLEncoder.encode(message,
                     "iso-8859-1");
         }catch(UnsupportedEncodingException e){ /* not possible  */}
         String hitta = ConnectHelper.Connect("http", "hitta.se", url, 80,
                 524288, null, null);
         if(hitta.contains("inga träffar")) return;
         System.out.println(url);
         personInfo.put("firstname", substring(hitta,"var tooltipText = '<strong>",
                 " "));
         personInfo.put("lastname", substring(hitta, "var tooltipText = '<strong>"
                 + personInfo.get("firstname") +" ","<"));
         personInfo.put("address", substring(hitta, "<strong>Adress:</strong><br>",
                 "<"));
         personInfo.put("zipCode", substring(hitta, "var zipCode = '", "'"));
         personInfo.put("locality", substring(hitta, "var locality = \"", "\""));
     }
 }
