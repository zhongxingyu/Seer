 package net.anei.cadpage.parsers.ID;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Gooding County, ID
 Contact: Christopher Bailey <wvfd9240@yahoo.com>
 Sender: PagingService@sircomm.com
 
  SIRCOMM / 1:34 PM\nNEW EVENT WND092\nFIRE\nFIRE ALL OTHER OR UN\n2000 E 3100 SOUTH\nWND AREA\n
  SIRCOMM / 1:55 AM\nNEW EVENT WND092\nACCU\nACCIDENT W/UNKNOWN I\n2285 E 3300 SOUTH\nWND AREA\n
  SIRCOMM / 9:59 PM\nNEW EVENT WND092\nFIREI\nFIRE INVESTIGATION\n1511 E 3600 SOUTH\nWND AREA\n
  SIRCOMM / 12:39 PM\nNEW EVENT WND092\n911\n911 DROP OFF/OPEN LI\n2175 E 3600 SOUTH\nWND AREA\n
  SIRCOMM / 7:39 AM\nNEW EVENT WND092\nFIREV\nVEHICLE FIRE\n3232 S 1800 EAST\nWND AREA\n
  SIRCOMM / 8:58 AM\nNEW EVENT WND092\nUT\nUTILITY PROBLEMS\n336 3RD AVE E\nWENDELL\n
  SIRCOMM / 7:33 AM\nNEW EVENT WND092\nFIREB\nBRUSH/GRASS FIRE\n1425 E 2700 SOUTH\nWND AREA\n
  SIRCOMM / 9:14 PM\nNEW EVENT WND092\nFIREI\nFIRE INVESTIGATION\n1800 E 2800 SOUTH\nWND AREA\n
  SIRCOMM / 8:28 PM\nNEW EVENT WND092\nFIREB\nBRUSH/GRASS FIRE\n2132 NIAGRA SPRINGS GR\nWND AREA\n
  SIRCOMM / 8:52 AM\nNEW EVENT WND092\nUT\nUTILITY PROBLEMS\n145 N HAGERMAN ST\nWENDELL\n
 SIRCOMM / 3:15 PM\nNEW EVENT WND092\nFIREB\nBRUSH/GRASS FIRE\n314 QUAIL RUN LN\n
  
 Contact: dwayne woodbridge <wendellfire9219@gmail.com>
 Sender: PagingService@sircomm.com
 (SIRCOMM) 7:45 PM\r\nNEW EVENT GDG099\r\nMED\r\nMEDICAL CALL\r\n521 UTAH ST\r\nGOODING [Attachment(s) removed]
 (SIRCOMM) 7:49 AM\r\nNEW EVENT GDG099\r\nACCI\r\nACCIDENT W/INJURIES\r\n2200 E 3000 SOUTH\r\nWND AREA [Attachment(s) removed]
 
 */
 
 
 public class IDGoodingCountyParser extends FieldProgramParser {
   
   public IDGoodingCountyParser() {
     super(CITY_CODES, "GOODING COUNTY", "ID",
          "TIME SRC SKIP CALL ADDR! CITY");
   }
   
   @Override
   public String getFilter() {
     return "PagingService@sircomm.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     do {
       if (subject.equals("SIRCOMM")) break;
 
       if (body.startsWith("SIRCOMM / ")) {
         body = body.substring(10).trim();
         break;
       }
       return false;
     } while (false);
     
    return parseFields(body.split("\n"), 5, data);
   }
 
   private class MySourceField extends SourceField {
     @Override
     public void parse(String field, Data data) {
       if (!field.startsWith("NEW EVENT ")) abort();
       super.parse(field.substring(10).trim(), data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("TIME")) return new TimeField(TIME_FMT);
     if (name.equals("SRC")) return new MySourceField();
     return super.getField(name);
   }
   
   private static final DateFormat TIME_FMT = new SimpleDateFormat("mm:ss aa");
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "WND AREA", "WENDELL"
   });
 }
