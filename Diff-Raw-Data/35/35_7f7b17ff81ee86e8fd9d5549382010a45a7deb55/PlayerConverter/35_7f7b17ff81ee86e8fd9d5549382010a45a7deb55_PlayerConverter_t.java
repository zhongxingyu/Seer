 // %1329240092:de.hattrickorganizer.gui.transferscout%
 package de.hattrickorganizer.gui.transferscout;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
import de.hattrickorganizer.model.HOVerwaltung;

 
 /**
  * Parses a player out of a text copied from HT. Tries also to give error informations but this may
  * be wrong!
  *
  * @author Marco Senn
  */
 public class PlayerConverter {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** List of all 21 ratings for the active language */
     private List skills;
     private List skillvalues;
     private List specialities;
     private List specialitiesvalues;
     private int error;
    final HOVerwaltung homodel = HOVerwaltung.instance();
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * We prepare our skill and specialities and sort them
      */
     public PlayerConverter() {
         // Get all skills for active language
         // This should be the same language as in Hattrick
         skills = new ArrayList();
         skills.add(homodel.getResource().getProperty("nonexisting").toLowerCase());
         skills.add(homodel.getResource().getProperty("katastrophal").toLowerCase());
         skills.add(homodel.getResource().getProperty("erbaermlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("armselig").toLowerCase());
         skills.add(homodel.getResource().getProperty("schwach").toLowerCase());
         skills.add(homodel.getResource().getProperty("durchschnittlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("passabel").toLowerCase());
         skills.add(homodel.getResource().getProperty("gut").toLowerCase());
         skills.add(homodel.getResource().getProperty("sehr_gut").toLowerCase());
         skills.add(homodel.getResource().getProperty("hervorragend").toLowerCase());
         skills.add(homodel.getResource().getProperty("grossartig").toLowerCase());
         skills.add(homodel.getResource().getProperty("brilliant").toLowerCase());
         skills.add(homodel.getResource().getProperty("fantastisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("Weltklasse").toLowerCase());
         skills.add(homodel.getResource().getProperty("uebernatuerlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("gigantisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("ausserirdisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("mythisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("maerchenhaft").toLowerCase());
         skills.add(homodel.getResource().getProperty("galaktisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("goettlich").toLowerCase());
 
         skillvalues = new ArrayList();
 
         for (int k = 0; k < skills.size(); k++) {
             skillvalues.add(new Integer(k));
         }
 
         // Sort skills by length (shortest first)
         int p = skills.size() - 1;
 
         while (p > 0) {
             int k = p;
 
             while ((k < skills.size())
                    && (skills.get(k - 1).toString().length() > skills.get(k).toString().length())) {
                 final String t = skills.get(k - 1).toString();
                 skills.set(k - 1, skills.get(k).toString());
                 skills.set(k, t);
 
                 final Integer i = (Integer) skillvalues.get(k - 1);
                 skillvalues.set(k - 1, (Integer) skillvalues.get(k));
                 skillvalues.set(k, i);
                 k++;
             }
 
             p--;
         }
 
         // Get all specialities for active language
         // This should be the same language as in Hattrick
         specialities = new ArrayList();
         specialities.add(homodel.getResource().getProperty("sp_Technical").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Quick").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Powerful").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Unpredictable").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Head").toLowerCase());
        specialities.add(homodel.getResource().getProperty("sp_Regainer").toLowerCase());
 
         specialitiesvalues = new ArrayList();
 
        for (int k = 0; k < 6; k++) {
             specialitiesvalues.add(new Integer(k));
         }
 
         // Sort specialities by length (shortest first)
         p = specialities.size() - 1;
 
         while (p > 0) {
             int k = p;
 
             while ((k < specialities.size())
                    && (specialities.get(k - 1).toString().length() > specialities.get(k).toString()
                                                                                  .length())) {
                 final String t = specialities.get(k - 1).toString();
                 specialities.set(k - 1, specialities.get(k).toString());
                 specialities.set(k, t);
 
                 final Integer i = (Integer) specialitiesvalues.get(k - 1);
                 specialitiesvalues.set(k - 1, (Integer) specialitiesvalues.get(k));
                 specialitiesvalues.set(k, i);
                 k++;
             }
 
             p--;
         }
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Returns possible error. If error is nonzero, there was a problem.
      *
      * @return Returns possible error
      */
     public final int getError() {
         return error;
     }
 
     /**
      * Parses the copied text and returns a Player Object
      *
      * @param text the copied text from HT site
      *
      * @return Player a Player object
      *
      * @throws Exception Throws exception on some parse errors
      */
     public final Player build(String text) throws Exception {
         error = 0;
 
         final Player player = new Player();
 		String teamname = "";
 
         // Init some helper variables
         String mytext = text;
         final List lines = new ArrayList();
         int p = -1;
         String tmp = "";
 
         // Detect linefeed
         //  \n will do for linux and windows
         //  \r is for mac
         String feed = "";
 
         if (text.indexOf("\n") >= 0) {
             feed = "\n";
         } else {
             if (text.indexOf("\r") >= 0) {
                 feed = "\r";
             }
         }
 
         // If we detected some possible player
         if (!feed.equals("")) {
             //
             // We start reformating given input here and extracting
             // only needed lines for player detection
             //
             // Delete empty lines from input
             String txt = text;
 
             while ((p = txt.indexOf(feed)) >= 0) {
                 tmp = txt.substring(0, p).trim();
 
                 if (!tmp.equals("")) {
                     lines.add(tmp);
                 }
 
                 txt = txt.substring(p + 1);
             }
 
             // Extract age-line and name-line
             p = 0;
 
             while (p < lines.size()) {
                 // Search for line containing a skill value and a number
                 tmp = lines.get(p).toString();
 
                 boolean broken = false;
                 int k = 0;
 
                 while (k < tmp.length()) {
                     if ((tmp.charAt(k) >= '0') && (tmp.charAt(k) <= '9')) {
                         broken = true;
                         break;
                     }
 
                     k++;
                 }
 
                 // We found a number. Check for a skill now
                 if (broken) {
                     broken = false;
                     k = skills.size() - 1;
 
                     while (k >= 0) {
                         if (tmp.indexOf(skills.get(k).toString()) >= 0) {
                             p--;
                             tmp = lines.get(p).toString();
 
                             while (p > 0) {
                                 p--;
                                 lines.remove(p);
                             }
 
                             broken = true;
                             break;
                         }
 
                         k--;
                     }
 
                     if (broken) {
                         if (tmp.indexOf("(")>-1 && tmp.indexOf(")")>-1) {
                             break;
                         } else {
                             p++;
                         }
                     }
                 }
 
                 p++;
             }
 
             // Extract TSI-line
             p = 2;
 
             while (p < lines.size()) {
                 //Search for TSI-line (ending in numbers)
                 tmp = lines.get(p).toString();
 
                 if ((tmp.charAt(tmp.length() - 1) >= '0') && (tmp.charAt(tmp.length() - 1) <= '9') ) {
                 	if (tmp.length()>9 && tmp.substring(tmp.length()-9, tmp.length()).indexOf(".")>-1) {
                 		p++;
                 		continue;
                 	}
 					String teamline = lines.get(p + 2).toString();
 					teamname = teamline.split(":")[1].trim();
                     while (p > 2) {
                         p--;
                         lines.remove(p);
                     }
 
                     break;
                 }
 
                 p++;
             }
 
             // Search for actual year (expires) and also next year
             // (end of year problem)
             p = 8;
 
             final Date d = new Date();
             SimpleDateFormat f = new SimpleDateFormat("yyyy");
             final String year = f.format(d);
             final int y = Integer.valueOf(year).intValue() + 1;
             final String year2 = String.valueOf(y);
 
             while (p < lines.size()) {
                 // Delete all rows not containing our year
                 tmp = lines.get(p).toString();
 
                 if ((tmp.indexOf(year) >= 0) || (tmp.indexOf(year2) >= 0)) {
                     while (p > 3) {
                         p--;
                         lines.remove(p);
                     }
 
                     break;
                 }
 
                 p++;
             }
 
             p = 5;
             lines.remove(p);
             p = 6;
             while (p < lines.size()) {
                 lines.remove(p);
             }
 
             // Extract minimal bid
             tmp = lines.get(4).toString();
             int n = 0;
             int k = 0;
             String bid = "";
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) < '0') || (tmp.charAt(k) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 k++;
             }
             k = 0;
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) >= '0') && (tmp.charAt(k) <= '9')) {
                     bid += tmp.charAt(k);
                 }
 
                 k++;
             }
 
             // Extract current bid if any
             tmp = lines.get(lines.size() - 1).toString();
             n = 0;
             k = 0;
             String curbid = "";
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) < '0') || (tmp.charAt(k) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 k++;
             }
             k = 0;
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) >= '0') && (tmp.charAt(k) <= '9')) {
                     curbid += tmp.charAt(k);
                 }
 
                 k++;
             }
 
             if (lines.size() >= 6) {
                 if (!curbid.equals("")) {
                     if (Integer.valueOf(curbid).intValue() >= Integer.valueOf(bid).intValue()) {
                         lines.remove(4);
                     } else {
                         lines.remove(5);
                     }
                 } else {
                         lines.remove(5);
                 }
             }
 
             //
             // We have now prepared our input to our needs
             // Let's extract the values now
             //
             if (lines.size() != 5) {
                 error = 2;
             }
 
             // Get name and ID from line 1
             tmp = lines.get(0).toString();
             player.setPlayerName(tmp.substring(0, tmp.indexOf("(")).trim());
             player.setPlayerID(Integer.valueOf(tmp.substring(tmp.indexOf("(") + 1, tmp.indexOf(")"))
                                                   .trim()).intValue());
             tmp = tmp.substring(tmp.indexOf(")")).trim();
 
            // Get injury
             String injury = "";
             for (int j = 0; j < tmp.length(); j++) {
            	if ((tmp.charAt(j) >= '0') && (tmp.charAt(j) <= '9') && (tmp.charAt(j-1) != '[')) {
            		injury = String.valueOf(tmp.charAt(j));
            		break;
            	}
             }
 
             if (!injury.equals("")) {
                 player.setInjury(Integer.valueOf(injury).intValue());
             }
 
            // check bookings
            try {
	            int b1 = tmp.indexOf("[");
	            int b2 = tmp.indexOf("]");
	            if (b1 > 1 && b2 > 1 && tmp.indexOf("[", b1+1) > -1) {
	            	player.setBooked(tmp.substring(b1+1, b2));
	            }
            } catch (Exception e) { /* ignore */ }

             // Get age from line 2
             tmp = lines.get(1).toString();
 
             String age = "";
             p = 0;
             n = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     age = age + tmp.charAt(p);
                 } else {
                     break;
                 }
 
                 p++;
             }
 
             if (!age.equals("")) {
                 player.setAge(Integer.valueOf(age).intValue());
             } else {
                 error = 2;
             }
 
             // Get ageDays from line 2
             tmp = lines.get(1).toString();
             int ageIndex = tmp.indexOf(age) + age.length();
             tmp = tmp.substring(ageIndex);
 
             String ageDays = "";
             p = 0;
             n = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     ageDays = ageDays + tmp.charAt(p);
                 } else {
                     break;
                 }
 
                 p++;
             }
 
             if (!ageDays.equals("")) {
                 player.setAgeDays(Integer.valueOf(ageDays).intValue());
             } else {
                 error = 2;
             }
 
             // Get tsi from line 3
             tmp = lines.get(2).toString();
 
             String tsi = "";
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     tsi = tsi + tmp.charAt(p);
                 }
 
                 p++;
             }
 
             if (!tsi.equals("")) {
                 player.setTSI(Integer.valueOf(tsi).intValue());
             } else {
                 error = 2;
             }
 
             // Get expire time from line 4
             tmp = lines.get(3).toString();
 
             String exp = "";
             p = 0;
             k = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                     k++;
                 } else {
                     tmp = tmp.substring(k);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
             k = 0;
 
             String part1 = "";
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     k++;
                 } else {
                     part1 = tmp.substring(0, k);
 
                     if (part1.length() < 2) {
                         part1 = "0" + part1;
                     }
 
                     tmp = tmp.substring(k + 1);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
             k = 0;
 
             String part2 = "";
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     k++;
                 } else {
                     part2 = tmp.substring(0, k);
 
                     if (part2.length() < 2) {
                         part2 = "0" + part2;
                     }
 
                     tmp = tmp.substring(k + 1);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
             k = 0;
 
             String part3 = "";
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     k++;
                 } else {
                     part3 = tmp.substring(0, k);
 
                     if (part3.length() < 2) {
                         part3 = "0" + part3;
                     }
 
                     tmp = tmp.substring(k + 1);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
 
             String part4 = "";
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     part4 = part4 + tmp.charAt(p);
                 }
 
                 p++;
             }
 
             final Calendar c = Calendar.getInstance();
             f = new SimpleDateFormat("ddMMyyyy");
 
             final Date d1 = c.getTime();
             final String date1 = f.format(d1);
             c.add(Calendar.DATE, 1);
 
             final Date d2 = c.getTime();
             final String date2 = f.format(d2);
             c.add(Calendar.DATE, 1);
 
             final Date d3 = c.getTime();
             final String date3 = f.format(d3);
 
             String date = part1 + part2 + part3;
 
             if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                 exp = date + part4;
             } else {
                 date = part1 + part3 + part2;
 
                 if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                     exp = date + part4;
                 } else {
                     date = part2 + part1 + part3;
 
                     if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                         exp = date + part4;
                     } else {
                         date = part2 + part3 + part1;
 
                         if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                             exp = date + part4;
                         } else {
                             date = part3 + part1 + part2;
 
                             if ((date1.equals(date))
                                 || (date2.equals(date))
                                 || (date3.equals(date))) {
                                 exp = date + part4;
                             } else {
                                 date = part3 + part2 + part1;
 
                                 if ((date1.equals(date))
                                     || (date2.equals(date))
                                     || (date3.equals(date))) {
                                     exp = date + part4;
                                 } else {
                                     exp = part1 + part2 + part3 + part4;
 
                                     if (error == 0) {
                                         error = 1;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
 
             // exp is of format: ddmmyyyyhhmm
             player.setExpiryDate(exp.substring(0, 2) + "." + exp.substring(2, 4) + "."
                                  + exp.substring(6, 8));
             player.setExpiryTime(exp.substring(8, 10) + ":" + exp.substring(10, 12));
 
             // Get price from line 5
             tmp = lines.get(4).toString();
 
             String price = "";
 
             for (int j = 0; j < tmp.length(); j++) {
                 if ((tmp.charAt(j) >= '0') && (tmp.charAt(j) <= '9')) {
                     price = price + tmp.charAt(j);
                 }
             }
 
             if (!price.equals("")) {
                 player.setPrice(Integer.valueOf(price).intValue());
             } else {
                 error = 2;
             }
 
             // truncate text from player name to date (year)
             final String name = player.getPlayerName();
             if ((p = mytext.indexOf(name)) >= 0) {
                 mytext = mytext.substring(p + name.length());
             }
             if ((p = mytext.indexOf(name)) >= 0) {
                 mytext = mytext.substring(p);
             }
 
             tmp = "";
             while (price.length() > 3) {
                 tmp = (price.substring(price.length() - 3) + " " + tmp).trim();
                 price = price.substring(0, price.length() - 3);
             }
             if (tmp.length() > 0) {
                 price = price + " " + tmp;
             }
             if (mytext.indexOf(price) >= 0) {
                 mytext = mytext.substring(0, mytext.lastIndexOf(price));
             }
 
             char[] cs = new char[teamname.length()];
 
             for (int cl = 0; cl < cs.length; cl++) {
                 cs[cl] = '*';
             }
 
             mytext = mytext.replaceAll(teamname, new String(cs)).toLowerCase();
 
             cs = new char[name.length()];
 
             for (int cl = 0; cl < cs.length; cl++) {
                 cs[cl] = '*';
             }
 
             mytext = mytext.replaceAll(name.toLowerCase(), new String(cs)).toLowerCase();
 
             // We can search all the skills in text now
             p = skills.size() - 1;
 
             final List foundskills = new ArrayList();
 
             while (p >= 0) {
                 final String singleskill = skills.get(p).toString();
                 k = mytext.indexOf(singleskill);
 
                 if (k >= 0) {
                     final List pair = new ArrayList();
                     pair.add(new Integer(k));
                     pair.add(singleskill);
                     pair.add(new Integer(p));
                     foundskills.add(pair);
 
                     final char[] ct = new char[singleskill.length()];
 
                     for (int cl = 0; cl < ct.length; cl++) {
                         ct[cl] = '*';
                     }
 
                     mytext = mytext.replaceFirst(singleskill, new String(ct));
                 } else {
                     p--;
                 }
             }
 
             if ((foundskills.size() != 11) && (error == 0)) {
                 error = 1;
             }
 
             // Sort skills by location
             p = foundskills.size() - 1;
 
             while (p > 0) {
                 k = p;
 
                 while (k < foundskills.size()) {
                     final List ts1 = (ArrayList) foundskills.get(k - 1);
                     final List ts2 = (ArrayList) foundskills.get(k);
 
                     if (((Integer) ts1.get(0)).intValue() > ((Integer) ts2.get(0)).intValue()) {
                         foundskills.set(k - 1, ts2);
                         foundskills.set(k, ts1);
                         k++;
                     } else {
                         break;
                     }
                 }
 
                 p--;
             }
 
             player.setForm(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(0))
                                                        .get(2)).intValue())).intValue());
             player.setExperience(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(1))
                                                              .get(2)).intValue())).intValue());
             player.setLeadership(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(2))
                                                              .get(2)).intValue())).intValue());
             player.setStamina(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                  .size()
                                                                                                  - 8))
                                                           .get(2)).intValue())).intValue());
             player.setGoalKeeping(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills
                                                                          .get(foundskills.size()
                                                                               - 7)).get(2))
                                                              .intValue())).intValue());
             player.setPlayMaking(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                     .size()
                                                                                                     - 6))
                                                              .get(2)).intValue())).intValue());
             player.setPassing(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                  .size()
                                                                                                  - 5))
                                                           .get(2)).intValue())).intValue());
             player.setWing(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                               .size()
                                                                                               - 4))
                                                        .get(2)).intValue())).intValue());
             player.setDefense(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                  .size()
                                                                                                  - 3))
                                                           .get(2)).intValue())).intValue());
             player.setAttack(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                 .size()
                                                                                                 - 2))
                                                          .get(2)).intValue())).intValue());
             player.setSetPieces(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(foundskills
                                                                                                    .size()
                                                                                                    - 1))
                                                             .get(2)).intValue())).intValue());
 
             // We can search the speciality in text now
             p = specialities.size() - 1;
 
             final List foundspecialities = new ArrayList();
 
             while (p >= 0) {
                 final String singlespeciality = specialities.get(p).toString();
                 k = mytext.indexOf(singlespeciality);
 
                 if (k >= 0) {
                     final List pair = new ArrayList();
                     pair.add(new Integer(k));
                     pair.add(singlespeciality);
                     pair.add(new Integer(p));
                     foundspecialities.add(pair);
 
                     final char[] ct = new char[singlespeciality.length()];
 
                     for (int cl = 0; cl < ct.length; cl++) {
                         ct[cl] = '*';
                     }
 
                     mytext = mytext.replaceFirst(singlespeciality, new String(ct));
                 } else {
                     p--;
                 }
             }
 
             if ((foundspecialities.size() > 1) && (error == 0)) {
                 error = 1;
             }
 
             // Sort specialities by location
             p = foundspecialities.size() - 1;
 
             while (p > 0) {
                 k = p;
 
                 while (k < foundspecialities.size()) {
                     final List ts1 = (ArrayList) foundspecialities.get(k - 1);
                     final List ts2 = (ArrayList) foundspecialities.get(k);
 
                     if (((Integer) ts1.get(0)).intValue() > ((Integer) ts2.get(0)).intValue()) {
                         foundspecialities.set(k - 1, ts2);
                         foundspecialities.set(k, ts1);
                         k++;
                     } else {
                         break;
                     }
                 }
 
                 p--;
             }
 
             if (foundspecialities.size() > 0) {
                 player.setSpeciality(((Integer) specialitiesvalues.get(((Integer) ((ArrayList) foundspecialities
                                                                                    .get(0)).get(2))
                                                                        .intValue())).intValue() + 1);
             } else {
                 player.setSpeciality(0);
             }
         }
 
         return player;
     }
 }
