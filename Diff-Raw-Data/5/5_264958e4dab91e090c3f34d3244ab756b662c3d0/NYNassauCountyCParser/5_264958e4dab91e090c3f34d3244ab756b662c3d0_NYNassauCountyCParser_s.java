 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Nassau County, NY (version C)
 Contact: Besnik Gjonlekaj <tigerfdny@gmail.com>
 Sender: backup@westburyfd.xohost.com
 System: Fire Rescue Systems
 
 SIG 14- SIGNAL 8 - FOR AFA  WESTBURY MIDDLE SCHOOL 455 ROCKLAND ST CS: LINDEN PLACE / SCHOOL ST TOA: 13:36 03/27
 SIG 16- AFA  THE SOURCE MALL 1504 OLD COUNTRY ROAD CS: MERCHANTS CONCOURSE / ZECKENDORF BOULEVARD TOA: 06:03 03/27/11
 SIG 4- SIGNAL 9   CABRERA, TULIO 362 WINTHROP STREET CS: POST AVENUE  / LINDEN AVENUE TOA: 11:33 03/26/11
 SIG 3- AUTO ACCIDENT W/AIDED  BOND STREET & OLD COUNTRY ROAD  BOND STREET CS: OLD COUNTRY ROAD TOA: 11:49 03/25/11
 SIG 1- STRUCTURE FIRE   RITE AID 210 POST AVENUE CS: MAPLE AVENUE  / MADISON AVENUE TOA: 10:30 03/24/11
 SIG 3- AUTO ACCIDENT/ HURST TOOL   BENIHANA RESAURANT 920 MERCHANTS CONCOURSE CS: TAYLOR AVENUE  / PRIVADO ROAD TOA: 12:56 03/24/11
 SIG 14- SIGNAL 8 - FOR AFA  WESTBURY MIDDLE SCHOOL 455 ROCKLAND STREET CS: LINDEN PLACE  / SCHOOL STREET TOA: 13:36 03/27/11
 SIG 4- SIGNAL 9   MARK,GERTRUDE 701 THE PLAIN ROAD CS: LADENBURG DRIVE  / VALENTINE ROAD TOA: 09:14 04/02/11
 SIG 4- SIGNAL 9  CODE 96  OUIDIO CASTRO 50 WATERBURY LANE CS: BARD ROAD  / PEPPERIDGE ROAD TOA: 13:23 04/02/11
 
 */
 
 
 public class NYNassauCountyCParser extends FieldProgramParser {
   
   private static final Pattern MARKER = Pattern.compile("^SIG \\d{1,2}- ");
 
   public NYNassauCountyCParser() {
     super("NASSAU COUNTY", "NY",
           "CALL! LOC:ADDR/SP! CS:X! TOA:SKIP");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.startsWith("***")) return false;
     if (!MARKER.matcher(body).find()) return false;
     body = body.replaceFirst("  ", " LOC: ");
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt1 = field.indexOf("  ");
       if (pt1 >= 0) {
         int pt2 = field.lastIndexOf("  ");
         if (pt1 == pt2) {
           if (field.startsWith("CODE ")) {
             pt2 = field.length();
           } else {
             pt1 = -2;
           }
         }
         if (pt1 >= 0) data.strCall = append(data.strCall, " ", field.substring(0,pt1).trim());
         if (pt2 < field.length()) data.strPlace = field.substring(pt2+2).trim();
         field = field.substring(pt1+2,pt2).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
 }
 
 
