 package net.anei.cadpage.parsers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 Saint Marys County, MD
 Contact: "Jason Adams" <jadams@bdvfd.org> or jadams@bdvfd.org or 2405384664@vzwpix.com
 Contact: Brian Brown <brian.brown86@gmail.com>
 Contact: patrick woodburn <patlvfd@gmail.com>
 Contact: Latoya Beaumont <beaumontlt.lb@gmail.com>
 Contact: "scherry3626@aol.com" <scherry3626@aol.com>
 Contact: Patrick Stanley <work3750@gmail.com>
 Contact: john douglass <jdouglassx@gmail.com>
 Contact: Crystal Jackson <crazyredheadmd@gmail.com>
 Sender: mplus@co.saint-marys.md.us
 System: Pro QA Medical & Pro QA Fire
 
 21:10:05*Personal Injury Accident*22607 THREE NOTCH RD INTERSECTN*MACARTHUR BL*LEXINGTON PARK*CO9 CO3 SQ3 CO39 CO79 A796*N/B LANES TWO VEHICLES AT LEAST ONE INJURY*
 14:41:22*Personal Injury Accident*21050 WILLOWS RD INTERSECTN*ABBERLY CREST LN*LEXINGTON PARK*CO3 CO39 CO6R*
 15:24:31*Working House Fire*20242 POINT LOOKOUT RD*OLD GREAT MILLS RD*INDIAN BRIDGE RD*GREAT MILLS*CO3 CO9 CO6 CO1 CO5 CO39 CO6R*
 21:11:30*Commercial Building Fire*46528 VALLEY CT APT3019*SPRING VALLEY DR*DEAD END*LEXINGTON PARK*CO3 CO13 CO9 TK3 CO7 TK7 CO39*Using ProQA Fire*
 23:14:28*Heart Problems*22521 IVERSON DR UNIT3*AMBER DR*CUL DE SAC*CALIFORNIA*CO9*55YOF C/A/B; RAPID HEART RATE AND WEAK; HX DIABETES;*
 ((37593) CAD ) 22:12:45*CO Detector With Symptons*21353 FOXGLOVE CT*DEAD END*BAYWOODS RD*HERMANVILLE*CO3 CO39*Using ProQA Fire*
 ((44333) CAD ) 00:35:39*CHIMNEY FIRE*25120 DOVE POINT LN*PARSONS MILL RD*DEAD END*LOVEVILLE*CO1 TK1 CO7*Using ProQA Fire*
 ((60350) CAD ) 18:34:06*Breathing Difficulties*22030 OXFORD CT APT*GLOUCESTER CT*DEAD END*LEXINGTON PARK*ALS CO39 CO6R*66YOF HIGH BLOOD
 
 ((46589) CAD) 14:02:26*Stroke**APT A2**22027 OXFORD CT APTA2*GLOUCESTER CT*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical*
 ((46677) CAD) 13:59:56*Chest Pain*40452 MEDLEYS LN*LAUREL GROVE RD*LOVEVILLE RD*OAKVILLE*CO79 ALS*subject has pacemaker*
 ((46589) CAD) 13:29:15*Breathing Difficulties**ST MARYS NURSING CENTER**21585 PEABODY ST RM441A*HOLLYWOOD RD*DEAD END*LEONARDTOWN*CO19 CO79 ALS*hx copd*
 ((46589) CAD) 12:47:33*Chest Pain*17498 GRAYSON RD*BEACHVILLE RD*VILLA RD*ST INIGOES*CO49R ALS*Using ProQA Medical*
 ((46589) CAD) 12:14:40*Sick Person**NEWTOWNE VILLAGE APTS**22810 DORSEY ST APT309*CONNELY CT*DEAD END*LEONARDTOWN*CO19 CO79 A799*Using ProQA Medical*
 ((46589) CAD) 12:08:40*Sick Person*46104 SALTMARSH CT*WEST WESTBURY BL*DEAD END*LEXINGTON PARK*CO39 A397*Using ProQA Medical*
 ((47017) CAD ) 21:24:45*Seizures/Convulsions*THREE OAK CENTER*46905 LEI DR*THREE NOTCH RD*SOUTH CORAL DR*LEXINGTON PARK*ALS CO39*Using ProQA
 ((14072) CAD ) 08:27:10*Sick Person*BANK OF AMERICA*21800 NORTH SHANGRI LA DR*THREE NOTCH RD*GREAT MILLS RD*LEXINGTON PARK*CO39 A397*Using ProQA
 
 ((23645) CAD ) 07:33:05*Mutual Aid EMS*401 EP WORTH CT*CO9*302; unresponsive*
((19239) CAD ) 20:51:10*Chest Pain*19673 NORTH SNOW HILL MANOR RD*SOUTH SNOW HILL MANOR RD*LYARD RD*ST MARYS CITY*CO39 A398 ALS*PT. DOES HAVE A PACEMAKER
 
 Contact: Doug Walmsley <dcwalmsley@md.metrocast.net>
 ((57079) CAD ) 16:18:26*Breathing Difficulties*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39 A397 ALS M3*RM102B-FRONT
 ((61165) CAD ) 07:09:07*Sick Person*46369 HATTONS REST LN*DEAD END*POINT LOOKOUT RD*PARK HALL*CO39*Using ProQA Medical*
 ((60056) CAD ) 18:14:19*Traumatic Injuries*21121 ACE LN*WINDING WY*SHORT WY*LORD CALVERT TRLPK*CO39*Using ProQA Medical*
 ((59734) CAD ) 16:59:32*Eye Problems/Injuries*LA QUINTA INN*22769 THREE NOTCH RD*CHANCELLORS RUN RD*GUNSTON DR*CALIFORNIA*CO39*at the front desk*
 ((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b
 ((3871) CAD ) 15:18:55*Seizures/Convulsions*PARK HALL ELEMENTARY*20343 HERMANVILLE RD*PIPER CT*DIXON CT*PARK HALL*A397 CO39 ALS M3*COME IN OFF OF ROUTE5*
 ((3306) CAD ) 13:33:38*Allergic Reaction/Bee Stings*GREAT MILLS HIGH SCHOOL*21130 GREAT MILLS RD*CYPRESS WY*TRI COMMUNITY WY*GREAT MILLS*ALS CO39*Using P
 ((3130) CAD ) 12:57:41*Falls/Traumatic*ESPERANZA MIDDLE SCHOOL*22790 MAPLE RD*THREE NOTCH RD*ELM CT*TOWN CREEK*A389 CO39*USE THE MAIN ENTRANCE*
 ((2455) CAD ) 10:10:53*Falls/Traumatic*BRETON MEDICAL GROUP BLDG #3*22576 MACARTHUR BL SUITE 354*HALSEY CT*THREE NOTCH RD*SAN SOUCI*CO39 A389*56yof C/A/B
 ((1741) CAD ) 05:36:56*Falls/Traumatic*CALLAWAY SHELL*20943 POINT LOOKOUT RD APT3*PINEY POINT RD*HUNTING QUARTER DR*CALLAWAY*CO39*44YOM C/A/B; CUT TO N
 ((64130) CAD ) 18:24:21*Sick Person*SOUTH RIDGE*13425 POINT LOOKOUT RD APT2*CURLEYS RD*EVERGREEN ESTATES LN*RIDGE*CO39*29 yom side pains*
 ((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b
 ((12416) CAD ) 18:36:28*Chest Pain*45848 KRYSTAL LN*TRI COMMUNITY WY*BRIDGETT LN*LORD CALVERT TRLPK*ALS CO39*Using ProQA Medical*
 
 Contact: tim vallandingham <blayre0190@gmail.com>
 ((62650) CAD ) 17:06:15*Personal Injury Accident*GOV THOMAS JOHNSON BRIDGE*46100 PATUXENT BEACH RD*NORTH PATUXENT BEACH RD*DEAD
 
 Contact: Richard Werring <rbwerring@gmail.com>
 (557) CAD ) 23:33:54*Breathing Difficulties*CEDAR LANE APARTMENTS*22680 CEDAR LANE CT APT2204*POINT LOOKOUT RD*CEDAR LANE RD*LEONARDTOWN*ALS CO19*74YOM
  */
 
 
 public class MDSaintMarysCountyParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("\\b\\d\\d:\\d\\d:\\d\\d\\*");
   private static final Pattern PLACE = Pattern.compile("\\*\\*([^*]+)\\*\\*");
   
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
     body = body.substring(match.start());
     
     // Special case, field delimited by double starts is a place name
     // that should be removed from the message string
     match = PLACE.matcher(body);
     if (match.find()) {
       data.strPlace = body.substring(match.start(1), match.end(1));
       body = body.substring(0, match.start()+1) + body.substring(match.end());
     }
     
     String[] flds = body.split("\\*+");
     if (flds.length < 6) return false;
     
     Result lastResult = null;
     String lastFld = null;
     boolean intersection = false;
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
         
         // If line ends with intersection, it is positively the
         // address field.  Any previously found field goes into the place
         // field, and we process the next intersecting address field.
         intersection = fld.endsWith(" INTERSECTN");
         if (intersection) {
           if (lastFld != null) data.strPlace = lastFld;
           parseAddress(StartType.START_ADDR, fld.substring(0, fld.length()-11), data);
           data.strApt = getLeft();
           break;
         }
         
         // Otherwise parse the address.  We always parse the first two
         // fields to see which one has the best address
         Result result = parseAddress(StartType.START_ADDR, fld);
         if (lastResult == null) {
           lastFld = fld;
           lastResult = result;
           ndx--;
           break;
         }
         
         // If this field looks better than the previous one
         // treat the prev field as a place and and parse this an address;
         if (lastResult.getStatus() < result.getStatus()) {
           data.strPlace = lastFld;
           result.getData(data);
           data.strApt = result.getLeft();
           break;
         }
         
         // If the previous field looks like the better than this one
         // parse the previous address and drop through to treat this
         // one as the first cross street
         lastResult.getData(data);
         data.strApt = lastResult.getLeft();
         ndx++;
         
       case 3:
         // Cross street 1
         // If the address field marked an intersection, there will only
         // be one cross street.  The mapping logic will merge it into the
         // mapping address if needed so we don't have to
         data.strCross = fld;
         if (intersection) ndx++;
         break;
         
       case 4:
         // Cross street 2
         data.strCross = data.strCross + " / " + fld;
         break;
         
       case 5:
         // town
         data.strCity = fld;
         break;
         
       case 6:
         // Units
         data.strUnit = fld;
         break;
         
       case 7:
         // Description
         data.strSupp = fld;
         break;
         
       case 8:
         // Additional description
         data.strSupp = data.strSupp + " / " + fld;
         ndx--;
         break;
       }
     }
     
     return true;
   }
 }
