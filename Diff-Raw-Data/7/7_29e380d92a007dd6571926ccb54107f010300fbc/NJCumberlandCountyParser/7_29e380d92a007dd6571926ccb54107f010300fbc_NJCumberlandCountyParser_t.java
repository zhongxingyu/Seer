 package net.anei.cadpage.parsers.NJ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 
 /*
 Cumberland County, NJ
 Contact: Ryan Feaster <feaster.ryan@gmail.com>
 Sender: E911@co.cumberland.nj.us
 
 E911:E22_INVEST_(VINELAND CITY)1763 CHEROKEE LA_2011-04-24 19:02:46_
 E911:E2902_STRUCTURE_(GREENWICH TWP)176 BAYSIDE RD_2011-04-24 17:27:45_AREA OF THE BAY
 E911:E22_COMM ALARM_(VINELAND CITY)CARNIVAL DISCOUNT LIQUORS / 4039 S MAIN RD_2011-04-24 12:27:19_
 E911:29R-FA_BLEEDING_(DEERFIELD TWP)246 KENYON AV/SHINDLE RES_2011-05-02 10:25:55_
 E911:29R-FA_BLEEDING_(DEERFIELD TWP)CUMBERLAND COUNTY TEC / 601 BRIDGETON AV_2011-05-02 09:27:47_NURSING OFFICE
 E911:E21_VEHICLE_(VINELAND CITY)S DELSEA DR & W ELMER RD_2011-05-01 18:46:26_
 E911:L2_COMM ALARM_(VINELAND CITY)COURTYARD SCHOOL INC / 1270 S EAST AVE_2011-05-01 17:03:36_
 E911:B2_UNKNOWNF_(VINELAND CITY)1775 ROOSEVELT BLVD / BEHIND_2011-05-01 00:10:13_
 E911:E2901_MVC_(DEERFIELD TWP)BIG OAK RD & MORTON AV_2011-04-30 00:06:19_
 E911:E2901_COMM ALARM_(DEERFIELD TWP)MAJOR OIL / 733 LANDIS AV_2011-05-12 09:29:52_
 
 Contact: Will Mosley <willmosleysr@gmail.com>
 NJFFS-FD_WDS/BRSH_(MILLVILLE CITY)MILL VILLAGE APTS / 301 N WADE BLVD_2011-06-02 21:50:47_BEHIND/WOODS
 
Contact: Chuck Nardelli <chuckie3238@gmail.com>
(E3202_MVC_(UPPER DEERFIELD)DUBOIS RD & FINLEY RD_2011-08-16 01) 19:26_
 
 */
 
 public class NJCumberlandCountyParser extends FieldProgramParser {
   
   
   public NJCumberlandCountyParser() {
     super("CUMBERLAND COUNTY", "NJ",
            "UNIT CALL ADDR TIME! PLACE");
   }
   
   @Override
   public String getFilter() {
     return "E911@co.cumberland.nj.us";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
    if (subject.length() > 0) body = subject + " " + body;
     if (body.startsWith("E911:")) body = body.substring(5).trim();
     return parseFields(body.split("_"), data);
   }
   
   private static final Pattern ADDR_PTN = Pattern.compile("^\\((.*)\\)");
   private static final Pattern LA_PTN = Pattern.compile(" LA\\b");
   private class MyAddressField extends AddressField {
 
     @Override
     public void parse(String field, Data data) {
       
       // City is in front in parenthesis
       Matcher match = ADDR_PTN.matcher(field);
       if (match.find()) {
         data.strCity = match.group(1);
         field = field.substring(match.end()).trim();
       }
       
       // Replace LA abbrv with LN
       field = LA_PTN.matcher(field).replaceAll(" LN");
       
       // slash divides address into two parts, either which can be a place name
       // or they can both be streets
       
       int pt = field.indexOf('/');
       if (pt >= 0) {
         String fld1 = field.substring(0,pt).trim();
         String fld2 = field.substring(pt+1).trim();
         
         if (checkAddress(fld1) == 0) {
           data.strPlace = fld1;
           field = fld2;
         } else if (checkAddress(fld2) == 0) {
           data.strPlace = fld2;
           field = fld1;
         }
       }
       
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "CITY ADDR"; 
     }
   }
   
   private class TimeStampField extends SkipField {
     public TimeStampField() {
      setPattern(Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d[: ]\\d\\d:\\d\\d"), true);
     }
   }
   
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       data.strPlace = append(data.strPlace, " / ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("TIME")) return new TimeStampField();
     if (name.equals("PLACE")) return new MyPlaceField();
     return super.getField(name);
   }
 }
