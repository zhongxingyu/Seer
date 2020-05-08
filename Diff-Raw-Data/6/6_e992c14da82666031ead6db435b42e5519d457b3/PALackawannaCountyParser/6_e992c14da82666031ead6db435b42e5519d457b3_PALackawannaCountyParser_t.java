 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Lackawanna County, PA
 Contact: greg rhodes <grhodes908@gmail.com>
 Contact: Mark Krzan <mkrzan1@gmail.com>
Contact: Randy Kochanowski <rkochdcfd@gmail.com>     
 Sender: aegispage@lackawannacounty.org
     
 Unit  Location: 206 MILLER RD STEVENSON RD / CARBONDALE RD 1 Common Name: Call Type: ALS Call Time: 09/26/11 17:02 Narrative  pd notified  POSS CONFUSED ALSO  Nature Of Call: 79YOF FEELING WEEK. SHE DID FALL EARLIER
 Unit  Location: 711 MAY ST MAPLE ST / DEAD END 59 Common Name: Call Type: ALS Call Time: 09/26/11 21:33 Narrative  PD ADVISED  REF EMD  HX OF HEART PROBLEMS  Nature Of Call: 56 YOM RAPID HEART RATE
 Unit  Location: 108 TERRACE DR STURGES RD / SCRANTON CARBONDALE HWY 20 Common Name: LACKAWANNA HEALTH AND REHABCall Type: ALS Call Time: 09/26/11 21:02 Narrative  22-9/LA4 TO MOSES TAYLOR  22-9 ENRT MOMENTARILY  Nature Of Call: in c-2 -- 90 yo f diff breathing low o2 sats
 Unit  Location: 601 6TH ST DEAD END / MOOSIC HEIGHTS 94 Common Name: Call Type: ALS Call Time: 09/26/11 22:27 Narrative  94PD ADVISED  UNKNOWN DRUGS  MALE TOOK DRUGS  IS BREATHING - IS SEMI-RESPONSIVE  Nature Of Call: 24 YOM COLLAPSED
 Unit  Location: 116 FULTON ST PROSPECT ST / PROSPECT ST 61 Common Name: Call Type: BLS Call Time: 09/27/11 13:56 Narrative  CALLER REFUSED EMD  REQ NO SIRENS  Nature Of Call: 85 YOF PAIN IN THE SHOULDER
 Unit  Location: 2500 N  WASHINGTON AVE DEAD END / HIGHNETT PL 50 Common Name: MU WOODLAND APARTMENTSCall Type: AFA Call Time: 09/27/11 14:53 Narrative  simplex panel PHN 5703486242// ATT KH  Nature Of Call: bldg 2,4,6
 Unit  Location: 2500 N  WASHINGTON AVE DEAD END / HIGHNETT PL 50 Common Name: MU WOODLAND APARTMENTSCall Type: AFA Call Time: 09/27/11 14:53 Narrative  simplex panel PHN 5703486242// ATT KH  Nature Of Call: bldg 2,4,6
 Unit  Location: 415 VENARD RD TEDRICK ST / ELAN GARDENS 2 Common Name: HERITAGE BAPTIST CHURCHCall Type: AFA Call Time: 09/27/11 15:07 Narrative  2pd advised  Nature Of Call: GEN FIRE ALRM ZONE 1
 Unit  Location: 1196 CHAPMAN LAKE RD 28 Common Name: Call Type: AFA Call Time: 09/27/11 16:07 Narrative  Nature Of Call: res fire/ cutler res/ gen fire
 Unit  Location: 40 GORDON AVE UN-NAMED / GORDAN AVE 51 Common Name: Call Type: STRT Call Time: 09/27/11 19:16 Narrative  tree that is in fire is 10 ft from residence  Nature Of Call: tree on fire
 Unit 21-9  Location: 40 VALLEY VIEW ESTS EYNON JERMYN RD / EYNON JERMYN RD 33 Common Name: Call Type: BLS Call Time: 09/27/11 19:43 Narrative  21 pd notified  POSS CONSTIPATED  Nature Of Call: 10 MOS OLD. RIGID ABDOMEN.
 Unit 21-9 R25  Location: 215 ERIE ST UN-NAMED / CHURCH ST 25 Common Name: Call Type: BLS Call Time: 09/28/11 10:00 Narrative  25PD ADVISED  UNABLE TO GET UP  Nature Of Call: 79 YOF FALL VICTIM, FELL ONTO FLOOR FROM COUCH
Unit E22 E20 L20 R23 22-9 SQ22  Location: 8 SCOTT RD W MOUNTAIN RD, E MOUNTAIN RD / GREEN GROVE RD 22 Common Name: MUSTANG MOTORSCall Type: STRT Call Time: 11/22/11 13:11 Narrative  BLACK SMOKE  Nature Of Call: SMOKE COMING FROM BUILDING
 
  */
 
 
 public class PALackawannaCountyParser extends FieldProgramParser {
   
   public PALackawannaCountyParser() {
     super("LACKAWANNA COUNTY", "PA",
           "Location:ADDR/SXx! Common_Name:PLACE? Call_Type:CALL! Call_Time:DATETIME Narrative:INFO Quadrant:MAP District:SKIP Nature_Of_Call:INFO");
   }
   
   @Override
   public String getFilter() {
     return "aegispage@lackawannacounty.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = body.replace("Call Type:", " Call Type:").replace(" Narrative ", " Narrative: ");
     return super.parseMsg(body, data);
   }
   
   private class MyAddressCityField extends AddressCityField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("0 ")) field = field.substring(2).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDRCITY")) return new MyAddressCityField();
     return super.getField(name);
   }
 }
