 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Harris County ESD #1, TX
 Contact: Nathan Mathews <nbmathews807@gmail.com>
 Sender: cadnoreply@proxy.hcec.com
 Sender: 930010nn
 ID#:11-06-20685 - - TRASH FIRE - 111 Bayou Dr - Apt:C - Bldg: - Key Map: 498D - Cross Streets:EAST FWY/MARKET - Box #:2002
 ID#:11-06-20710 - - FIRE - PreAlert - 16217 Ridlon - Apt: - Bldg: intra SERVICES - Key Map: 498C - Cross Streets:SHEILA LN/WOODLAND D
 ID#:11-06-20658 - - 29A1 MOTOR VEHICLE A - East Fwy / Delldale - Apt: - Bldg: - Key Map: 498E - Cross Streets: - Box #:2004
 ID#:11-06-20627 - - 29A1 MOTOR VEHICLE A - 16410 Ave D - Apt:1 - Bldg: AVE D APTS - Key Map: 498D - Cross Streets:WOODLAND DR/CEDAR L
 
 ID#:11-07-25331 - - ALARMS - 1518 Great Dover Cir - Apt: - Bldg: - Key Map: 458W
 ID#:11-07-25341 - 06D02 - 6D1 RESPIRATORY - 11870 Greenloch Ln - Apt: - Bldg: - Key Map: 416K - Cross Streets:GREENCANYON DR/GREENROC 
 ID#:11-07-25342 - 25A01 - PSYCHIATRIC - 15215 S Brentwood Dr - Apt: - Bldg: - Key Map: 498E - Cross Streets:Dead End/DELLDALE - Box #:2004
 ID#:11-07-25378 - - Stroke (CVA) - 11715 Greenglen Dr - Apt: - Bldg: - Key Map: 416K - Cross Streets:JOHN RALSTON RD/GREENROCK LN - B 
 ID#:11-07-25416 - - HEMORRHAGE/LACERATIO - 12370 S San Circle Dr - Apt: - Bldg: UNK TRAILER PARK - Key Map: 456C - Cross Streets:BARK 
 ID#:11-08-25733 - 55B04 - ELECTRICAL HAZARD - 16102 E Ih 10 - Apt: - Bldg: OLD RIVER CHURCH DAY - Key Map: 498G - Cross Streets:IH 10 EAST BAYOU RAMP/IH 10 SH -
 ID#:11-08-25969 - 29D02m - 29D5 MOTOR VEHICLE A - 539 S Sheldon Rd - Apt: - Bldg: - Key Map: 498K - Cross Streets:JACINTO PORT BLVD/MARKET - Box #:2001
 ID#:11-08-25936 - 29B04 - Traffic/Trans Incide - E BW 8 N / WOODFOREST BW 8 NB - Apt: - Bldg: - Key Map: 457Y - Cross Streets: - Box #:2005
 ID#:11-08-26006 - - Citizen Assist - 5500 E Bw 8 N - Apt: - Bldg: big tex storage - Key Map: 457Y - Cross Streets:BW 8 NB WALLISVILLE EXIT RAMP/ - Box #:2005
 
 ID#:11-08-26039 - - FUEL SPILL - 15880 Wallisville Rd - Apt: - Bldg: LEGEND OAKS
 - EAST - Key Map: 457V - Cross Streets:HIDDEN PARK DR/HYACINTH PATH W - Box #:2
 - EAST - Key Map: 457V - Cross Streets:HIDDEN PARK DR/HYACINTH PATH W - Box #:2
 005
 
 ID#:11-08-26067 - Address changed to:620 SHELDON RD - Cross Streets:AVE C/RIDLO
 N - Key Map:498B
 
 ID#:11-08-26105 - 21B01 - HEMORRHAGE/LACERATIO - 11803 Greenmesa Dr - Apt: - Bld
 
 - 67B03U - GRASS/WOODS FIRE - AVE C / DELLDALE - Apt: - Bldg: - Key Map:498E -
 Cross Streets: - Box #:
 
 : SYSCO - Key Map: 456F - Cross Streets:LEY RD/SCENIC RIVER DR - Box #:3304
 ID#:11-08-26080 - 13D01 - DIABETIC PROBLEMS - 10710 Green River Dr - Apt: - Bldg
 
 ID#:11-08-26080 - Address changed to:10710 Greens Crossing Blvd - Cross Streets
 
 Body:ID#:11-08-26159 - - Stab / GSW (B) - 919 Holbech - Apt: - Bldg: - Key Map: 497D
 
 Body:ID#:11-08-26160 - - HEART PROBLEMS - 9402 Abbotshall Ln - Apt: - Bldg: - Key Map
 Body:: 456D - Cross Streets:ARBOR FIELD LN/STONEFIELD MANO - Box #:3304
 






 */
 
 
 public class TXHarrisCountyESD1BParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" *(?: -)+ *| *(?:- )+ *");
 
   public TXHarrisCountyESD1BParser() {
     super("HARRIS COUNTY", "TX",
           "ID#:ID! CODE? CALL! PREALERT? ADDR! Apt:APT Bldg:APT Key_Map:MAP% Cross_Streets:X Box_#:BOX");
   }
   
   @Override
   public String getFilter() {
     return "cadnoreply@proxy.hcec.com,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     return parseFields(DELIM.split(body), data);
   }
   
   // Code field has to fit specific pattern
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(Pattern.compile("\\d\\d[A-Z]\\d\\d[A-Za-z]?"));
     }
   }
   
   // PreAlert is a special call extender field
   private class PreAlertField extends CallField {
     
     public PreAlertField() {
       setPattern(Pattern.compile("PreAlert"));
     }
     
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   // There are two fields, APT and BLD mapped to the apartment field.  But they
   // occur in reverse order, if both are specified, we want to to put bldg first.
   private class MyAptField extends AptField {
     @Override
     public void parse(String field, Data data) {
       data.strApt = append(field, "-", data.strApt);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("PREALERT")) return new PreAlertField();
     if (name.equals("APT")) return new MyAptField();
     return super.getField(name);
   }
   
 }
