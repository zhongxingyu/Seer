 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Davie County, NC
 Contact: Michael Wilson <fireman1700@gmail.com>
 
 911:Call #110619-2932* Address:866 ANGELL RD* * * City:MOCKSVILLE* * Type:HC* HAZARDOUS CONDITION* HILL JOEL* PH#:336-940-2303* Units:17* IRA:* Medical: No* Haza.
 911:Call #110622-3347* Address:2388 LIBERTY CHURCH RD* * * City:MOCKSVILLE* * Type:19D4* HEART PROBLEMS* HALL GENE* PH#:336-492-5114* Units:17,34,ST4,YC24* IRA:*
 911:Call #110622-3355* Address:389 LIBERTY CHURCH RD* * * City:MOCKSVILLE* * Type:MED* MEDICAL CALL* CULLER C O* PH#:336-492-7249* Units:17* IRA:* Medical: No* H
 911:Call #110623-3420* Address:261 COOPER CREEK DR* * * City:MOCKSVILLE* Geo Comment: BUSINESSES IN CITY LIMITS NBH: OFF 1734 US HWY 601 N Landmark Comment: KNOX
 911:Call #110624-3510* Address:3868 US HWY 601 N* * * City:MOCKSVILLE* * Type:YEL* CARDIAC PROBLEM* HOHFF, EARL V* PH#:336-492-3017* Units:17* IRA:* Medical: No*
 
 911:Call #110627-3974* Address:NC HWY 801 N // BONKIN LAKE RD* * * City:MOCKSVILLE* * Type:GWB* GRASS/WOODS/BRUSH FIRE* PAUL TONY* PH#:336-575-4524* Units:17,23*
 911:Call #110630-4406* Address:284 BRANGUS WAY* * * City:MOCKSVILLE* NBH: OFF 2386 CANA RD* Type:VF* VEHICLE FIRE* MEADER CORTLAND J* PH#:336-940-2666* Units:17
 911:Call #110628-4171* Address:700 RICHIE RD* * * City:MOCKSVILLE* NBH: FROM 444 EATONS CHURCH RD TO 3558 US HWY 601 N NBH: I40 TO FARMSTEAD LN* Type:HC* HAZARDO
 
Contact: Brent Crotts <jvfd2110@gmail.com>
911@[70.60.255.70] 911:Call #110713-6266* Address:154 CRESTVIEW DR* * * City:MOCKSVILLE* NBH: OFF 499 EAST LAKE DR* Type:26A2-11* SICK PERSON (SPECIFIC DIAGNOSIS)* linda packett* P

  */
 
 
 public class NCDavieCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile("\\*(?: \\*)*"); 
   
   public NCDavieCountyParser() {
     super("DAVIE COUNTY", "NC",
            "ID Address:ADDR! City:CITY! NBH:INFO3 Type:CALL CALL NAME PH:PHONE Units:UNIT IRA:SKIP INFO+ Geo_Comment:INFO2");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
    int ipt = body.indexOf("911:Call #");
    if (ipt < 0) return false;
    body = body.substring(ipt+10).trim();
     body = body.replace("PH#:", "PH:");
     return parseFields(DELIM.split(body), data);
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   private class MyInfo2Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
       data.strSupp = "Geo Comment: " + field;
     }
   }
   
   private class MyInfo3Field extends InfoField {
     @Override
     public void parse(String field, Data data) {
       data.strSupp = "NBH: " + field;
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("INFO2")) return new MyInfo2Field();
     if (name.equals("INFO3")) return new MyInfo3Field();
     return super.getField(name);
   }
 }
