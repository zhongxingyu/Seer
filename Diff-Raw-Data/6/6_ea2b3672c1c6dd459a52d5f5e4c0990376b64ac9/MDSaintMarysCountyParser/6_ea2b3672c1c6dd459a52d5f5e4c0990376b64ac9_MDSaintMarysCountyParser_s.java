 package net.anei.cadpage.parsers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 Saint Marys County, MD
 Contact: "Jason Adams" <jadams@bdvfd.org> or jadams@bdvfd.org or 2405384664@vzwpix.com
 Sender: mplus@co.saint-marys.md.us
 System: Pro QA Medical & Pro QA Fire
 
 21:10:05*Personal Injury Accident*22607 THREE NOTCH RD INTERSECTN*MACARTHUR BL*LEXINGTON PARK*CO9 CO3 SQ3 CO39 CO79 A796*N/B LANES TWO VEHICLES AT LEAST ONE INJURY*
 14:41:22*Personal Injury Accident*21050 WILLOWS RD INTERSECTN*ABBERLY CREST LN*LEXINGTON PARK*CO3 CO39 CO6R*
 15:24:31*Working House Fire*20242 POINT LOOKOUT RD*OLD GREAT MILLS RD*INDIAN BRIDGE RD*GREAT MILLS*CO3 CO9 CO6 CO1 CO5 CO39 CO6R*
 21:11:30*Commercial Building Fire*46528 VALLEY CT APT3019*SPRING VALLEY DR*DEAD END*LEXINGTON PARK*CO3 CO13 CO9 TK3 CO7 TK7 CO39*Using ProQA Fire*
 23:14:28*Heart Problems*22521 IVERSON DR UNIT3*AMBER DR*CUL DE SAC*CALIFORNIA*CO9*55YOF C/A/B; RAPID HEART RATE AND WEAK; HX DIABETES;*
  */
 
 
 public class MDSaintMarysCountyParser extends SmartAddressParser {
   
  private static final Pattern MARKER = Pattern.compile("^\\d\\d:\\d\\d:\\d\\d\\*");
   
   public MDSaintMarysCountyParser() {
     super("SAINT MARYS COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
     return "mplus@co.saint-marys.md.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     
     String[] flds = body.split("\\*");
     if (flds.length < 6) return false;
     
     int ndx = 0;
     for (String fld : flds) {
       fld = fld.trim();
       switch (ndx++) {
       
       case 0:
         // Time - skip
         break;
       
       case 1:
         // Call description
         data.strCall = fld;
         break;
         
       case 2:
         // Address line
         if (fld.endsWith(" INTERSECTN")) {
           fld = fld.substring(0, fld.length()-11).trim();
         } else {
           ndx++;
         }
         parseAddress(StartType.START_ADDR, fld, data);
         data.strApt = getLeft();
         break;
         
       case 3:
         // intersecting street
         data.strAddress = data.strAddress + " & " + fld;
         ndx += 2;
         break;
         
       case 4:
         // Cross street 1
         data.strCross = fld;
         break;
         
       case 5:
         // Cross street 2
         data.strCross = data.strCross + " / " + fld;
         break;
         
       case 6:
         // town
         data.strCity = fld;
         break;
         
       case 7:
         // Units
         data.strUnit = fld;
         break;
         
       case 8:
         // Description
         data.strSupp = fld;
         break;
         
       case 9:
         // Additional description
         data.strSupp = data.strSupp + " / " + fld;
         ndx--;
         break;
       }
     }
     
     return true;
   }
 }
