 package net.anei.cadpage.parsers.VA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.dispatch.DispatchDAPROParser;
 
 /*
 Rockingham County, VA (class II)
 Contact: Spencer Gibson <stgibs@gmail.com>
 Sender: messaging@iamresponding.com or mailbox@hrecc.org
 
 Some departments have a leading blank and some do not.  Other departments start
 with a different code, but it will always be Rnn or Cnn.
 
 R40 EMS-CARDIAC CONDITION 1751 MAIN AVE HAR CFS# 2010-082726 CROSS: GARBERS CHURCH RD/S HIGH ST
 R40 EMS-MENTAL PROBLEM 445 N MAIN ST 44 HAR CFS# 2010-082451 CROSS: WOLFE ST/ROCK ST
 R40 EMS-ABDOMINAL PAIN 1737 MORELAND DR HAR CFS# 2010-083119 CROSS: PHEASANT RUN CIR/ASHFORD CT
 R40 EMS-CHEST PAIN 235 LAYMAN ST 101 HAR CFS# 2010-083046 CROSS: N MAIN ST/LONGVIEW DR
 R40 TRAFFIC CRASH 300 BOYERS RD BLK HAR CFS# 2010-082984 CROSS: MYSTIC WOODS LN/CULLISON CT
 R40 TRAFFIC CRASH RESERVOIR ST & CANTRELL AV HAR CFS# 2010-082327
 (Rescue 40) R40 ODOR INVESTIGATION IN STRUCTUR 290 WARREN SERVICE DR HAR CFS# 2010-091415 CROSS: BLUESTONE DR/DEAD END
 (Rescue 40) 100 BLK OF QUALITY STREET IN BWATER IS CLOSED REVIEW THE TRANSMITTED FAX FOR THE ROAD CLOSURE-ECC GROUP PAGED C90
  
 Contact: sean taylor <firefightin30@gmail.com>, 5408304219@vzwpix.com
 C30 GRASS FIRE SPOTSWOOD TRL & ROCKINGHAM PIKE ELK CFS# 2010-092361
 C30 EMS-CARDIAC CONDITION 1533 RABBIT DR ELK CFS# 2010-092834 CROSS: N EAST SIDE HWY/DEADEND
 C30 EMS-DIFFICULTY BREATHING 105 ELKMONT DR 2 ELK CFS# 2010-092623 CROSS: S EASTSIDE HWY/DEAD END
 C30 POSSIBLE STRUCTURE FIRE 244 QUAIL CT MCG CFS# 2010-092692 CROSS: ASHBY RD/BETHEL LN
 C30 EMS-TRAUMA INJURIES E SPOTSWOOD AVE & MORGAN AVE ELK CFS# 2010-094660
 C30 EMS-DIFFICULTY BREATHING 3240 THOROUGHFARE RD ELK CFS# 2010-094548 CROSS: WHISPERING WINDS TRL/EPPARD LN
 C30 EMS-DIFFICULTY BREATHING 320 E ROCKINGHAM ST ELK CFS# 2010-094840 CROSS: JACKSON AVE/PAGE ST
 
 Contact: Laurie Hensley <lhensley0217@gmail.com>
 R35 EMS-ILLNESS 516 W SPOTSWOOD TRL ELK CFS# 2011-018309 CROSS: SHENANDOAH AVE/2ND ST
 
 Contact: jeff hammer <jh2785@gmail.com>
 MAILBOX@hrecc.org Msg: C80 EMS-FALLS 9718 VALLEY VIEW RD MCG CFS# 2011-034525 CROSS: TREE SIDE LN/LONGLEY RD
 
 Contact: "wnlwaff@comcast.net" <wnlwaff@comcast.net>
 (Rescue 40) R40 EMS-DIFFICULTY BREATHING 1825 S MAIN ST HAR CFS# 2011-051783 CROSS: PLEASANT HILL RD/DUKES PLAZA
 
 Contact: "ecashff@aol.com" <ecashff@aim.com>
 Sender: MAILBOX@hrecc.org
 C60 EMS-FALLS 273 CENTER ST TIM CFS# 2012-022275 CROSS: SHENANDOAH AVE/NEW MARKET RD
 
 
 *** NOT IMPLEMENTED ***
 Contact: Michael Roper <ropermw2@gmail.com>
 (Rescue 40) DIFFICULTY BREATHING 563 NEFF AVE A   2012-001403 CROSS: RESERVOIR ST/CHASE CT
 
 */
 
 public class VARockinghamCountyParser extends DispatchDAPROParser {
   
   private static final String DEF_STATE = "VA";
   private static final String DEF_CITY = "ROCKINGHAM COUNTY";
   
   
   private static final Properties CITY_CODE_TABLE = 
     buildCodeTable(new String[]{
         "BRO", "BROADWAY",
         "BRI", "BRIDGEWATER",
         "HAR", "HARRISONBURG",
         "SIN", "SINGERS GLEN",
         "CLO", "CLOVER HILL",
         "HIN", "HINTON",
         "MOU", "MOUNT CRAWFORD",
         "TIM", "TIMBERVILLE",
         "DAY", "DAYTON",
         "GRO", "GROTTOES",
         "MCG", "MCGAHEYSVILLE",
         "PEN", "PENN LAIRD",
         "ELK", "ELKTON",
         "WEY", "WEYERS CAVE",
         "NEW", "NEW MARKET",
         "STA", "STANLEY",
         "LUR", "LURAY",
         "SHE", "SHENANDOAH",
         "KEE", "KEEZLETOWN"
     });
   
 	  
 	  public VARockinghamCountyParser() {
 			 super(CITY_CODE_TABLE, DEF_CITY, DEF_STATE);
 	  }
 	  
 	  @Override
 	  public String getFilter() {
 	    return "messaging@iamresponding.com,mailbox@hrecc.org";
 	  }
 }
