 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Montgomery County, MD
 Contact: Alain Pankopf <hockeylaxboy11@gmail.com>
 Sender: rc.355@c-msg.net,@mcen.montgomerycountymd.gov 
 
 MCo / [mCAD] * D * 2301 * BUILDING FIRE * 1620 E JEFFERSON ST ,RO * E723 E703 E721 E726 E705B AT723 AT703 RS703 A723 BC703 BC704 D3 D5 DFRS INV CEALRM CODE BCNOT
 MCo / [mCAD] * D * 0302 * BUILDING FIRE * 733 MONROE ST ,RO * E723 E733 E726 E725 E721 AT703 AT723 RS741B M723 BC703 BC704 D3 D5 DFRS INV CEALRM CODE BCNOT
 MCo / [mCAD] * D * 0301 * HOUSE FIRE * 1006 DE BECK DR ,RO * E703 E723 E721 E725 E733 AT703 T731 RS703 A703 BC703 BC704 D3 D5 DFRS INV CEALRM CODE BCNOT
 MCo / [mCAD] * D * 2312 * UNCONFIRMED POWDER * 5700 BOU AVE ,MCG (TARGET STORE) * E723 AT723 A723 M703 HM707 E720 D3 BC703 DFRS BCNOT HIRT ECC
 MCo / [mCAD] * D * 0801 * BOX ALARM * 17211 KING JAMES WAY ,MCG * E728 E731 E703 T731 RS703 A708C BC705 BC703 D8 D3 DFRS INV CEALRM CODE BCNOT
 MCo / [mCAD] * D * 0503 * HOUSE FIRE * 4805 FLANDERS AVE ,MCG * E705B E723 E720 E750 E726 AT723 AT751 RS742 A705B BC704 BC703 D5 D3 DFRS INV CEALRM CODE BCNOT
 MCo / [mCAD] * D * 0318 * STABBING - ALS1 * 9300 KEY WEST AVE / 15200 SHADY GROVE RD ,RO * M703 BCNOT EMSNOT E703 D3 ECC25 DFRS
 MCo / [mCAD] * D * 0301 * PIC w/ ENTRAPMENT -ALS1 * 1180 EDMONSTON DR / 1100 WADE AVE ,RO * M703 RS703 E703 D3 EMS EMSNOT BCNOT DFRS
 
 Contact: Keith Stakes <kstakes2@aol.com>
 Sender: MC Emergency Network
 CAD MSG: * D * 3132 * BOX ALARM * 209 FOUNTAIN GREEN LA ,GA  * E708 E722 E729 E734 E728 T731 AT708 RS703 M73M
 CAD MSG: * D * 2424 * PIC w/ ENTRAPMENT -ALS1 * 13400 NEW HAMPSHIRE AVE / 2 RANDOLPH RD ,MCG  * A724 E724
 
 Contact: "Ebow Holdbrook-Smith" <ebowhold4u@gmail.com>
 CAD MSG: * D * 3525 * PIC w/VEH OVERTURNED-ALS1 * S270C 121-27 @ 17000 S270 X18 TO X16 HWY ,MCG  * M735 T7
 
 Contact: Morgan Kee <morgan.kee@gmail.com>
 (MCo) [mCAD] * D * 4009 * PIC w/ ENTRAPMENT -ALS1 * 4700 BROOM DR / 18400 WICKHAM RD ,MCG * A740 E740 M704 RS704 D4 EMS EMSNOT BCNOT DFRS
 
 Contact: Robert Murphy <robertmurphy30@gmail.com>
 FRM:General Alerts\nMSG:CAD MSG: * D *  * MAF FULL * 14615 PHILIP DR * AT715 BCNOT DFRS=
 
 Contact: Christopher Finelli <chris.finelli@gmail.com>
 CAD MSG: * D * 0609 * RSC - RESET ALARM * 4908 AUBURN AVE ,MCG  * RS741B ECC1
 
Contact: Tim Whirley <t.whirley76@gmail.com>
Sender: alrt_7H8G@mcen.montgomerycountymd.gov
CAD MSG: * D * 1704 * ABDOMINAL PAIN * 7500 MATTINGLY LA ,MCG  * A717 ECC30

 */
 
 public class MDMontgomeryCountyParser extends MsgParser {
   
   public MDMontgomeryCountyParser() {
     super("MONTGOMERY", "MD");
   }
   
 	public String getFilter() {
 		return "rc.355@c-msg.net,MC Emergency Network,@mcen.montgomerycountymd.gov";
 	}
 	
 	@Override
 	protected boolean parseMsg(String body, Data data) {
   
 	  int pt = body.indexOf("* D * ");
 	  if (pt < 0) return false;
 	  body = body.substring(pt+6).trim();
     
     Properties props = parseMessage(body, "\\*", new String[]{"BOX","Call","Addr","Units"});
     data.strBox = props.getProperty("BOX", "");
     data.strCall = props.getProperty("Call", "");
     Parser p = new Parser(props.getProperty("Addr", ""));
     String strAddress = p.get('(');
     data.strPlace = p.get(')');
     p = new Parser(strAddress);
     p.getLastOptional(',');
    strAddress = p.get();
    strAddress = strAddress.replaceAll("\\bLA\\b", "LN");
    parseAddress(strAddress, data);
     for (String unit : props.getProperty("Units", "").split(" +")) {
       if (validUnit(unit)) {
         if (data.strUnit.length() > 0) data.strUnit += ' ';
         data.strUnit += unit;
       }
     }
     return true;
   }
 
 	// Determine if unit code is valid reportable unit
   
   private static final Pattern UNIT = Pattern.compile("[A-Z]+\\d{3}[A-Z]?");
   private boolean validUnit(String unit) {
     
     // We could do the actual type lookup, but just checking for three
     // trailing digits should be enough
     return UNIT.matcher(unit).matches();
   }
   
 /***
 valid units consist of specific prefix followed by 3 digits.
 
 A – Ambulance (BLS EMS Transport Unit)
 AB – Air Boat
 ALS – ALS Chase Car
 AR – Air Unit
 AT – Tower (Aerial Tower)
 BC – Battalion Chief
 DC – Duty Chief
 E – Engine
 ECC CAD – Emergency Communication Center CAD dispatch console
 EMS – EMS Supervisor
 EW – Engine Tanker
 HM – Haz Mat
 HSU – Haz Mat Support Unit
 M – Medic (ALS EMS Transport Unit)
 MAB – Medical Ambulance Bus (20 patient EMS transport unit)
 MSU – Medical Support Unit (Mass casualty EMS supplies)
 NCR – National Capital Region Communications Unit
 RAT – Reserve Aerial Tower
 RE – Reserve Engine
 RECON – Urban Search & Rescue Recon Unit
 RS – Rescue Squad (Heavy Rescue)
 SOP – Special Operations Chief
 T – Truck
 U – Unit
 W – Tanker (Water Tender)
 XTP – Urban Search & Rescue Roll-off Transport Unit
 ***/  
 }
