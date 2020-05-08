 package net.anei.cadpage.parsers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
import java.util.Locale;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * This class is responsible for parsing useful information from an SMS page message
  */
 public abstract class MsgParser {
   
   private static final int GPS_POS_LAT =  0x01;  // Lattitude should be positive
   private static final int GPS_NEG_LAT =   0x02;  // Lattitude should be negative
   private static final int GPS_POS_LONG = 0x04;  // Longitidue should be positive
   private static final int GPS_NEG_LONG =  0x08;  // Longitude should be negative
   private static final int GPS_LARGE_LAT = 0x10;  // Abs value of latitude should exceed abs value of longitude
   private static final int GPS_LARGE_LONG = 0x20;  // Abs value of longitude should exceed abs value of latitidue
 
   /**
    * Country code passed to constructor to set up tables for specific countries
    * The country code is also responsible for converting GPS coordinates to a normal
    * Google standard form.  Because there is no standard as to which coordinate comes
    * first and whether or not they are signed, this conversion has to make various
    * assumptions about what legitimate GPS coordinates look like that can vary from
    * country to country
    */
   public static enum CountryCode {
     
     US(GPS_POS_LAT | GPS_NEG_LONG | GPS_LARGE_LONG), 
     UK(GPS_POS_LAT | GPS_LARGE_LAT), 
     AU(GPS_NEG_LAT | GPS_POS_LONG | GPS_LARGE_LONG), 
     NZ(GPS_NEG_LAT | GPS_POS_LONG | GPS_LARGE_LONG),
     SE(GPS_POS_LAT | GPS_POS_LONG | GPS_LARGE_LAT);
     
     private int gpsFlags;
     
    CountryCode(int gpsFlags) {
       this.gpsFlags = gpsFlags;
     }
 
    /**
     * Look for GPS coordinates in address line.  If found, parse them into a
     * set of coordinates that Google Maps will recognize
     */
    public String parseGPSCoords(String address) {
      Matcher match = GPS_PATTERN.matcher(address);
      if (!match.find()) return null;
 
      // Calculate the coordinate values
      double c1 = cvtGpsCoord(match.group(2));
      double c2 = cvtGpsCoord(match.group(4));
      
      // If they used the X: ... Y: ... labels, swap the parameters
      // because we and google expect the latitude to come first
      if (match.group(1) != null && match.group(3) != null) {
        double tmp = c1;
        c1 = c2;
        c2 = tmp;
      }
      
      // There isn't a consistent standard as to which is latitude and
      // which is longitude, so we will have to make some guesses.
      double latitude, longitude;
      
      // First step is to identify which is latitude and which is longitude
      // If one must be larger hat the other, we can use their relative sizes
      // to determine that
      double ac1 = Math.abs(c1);
      double ac2 = Math.abs(c2);
      if (ac1 == 1.0 && ac2 == 1.0) return null;
      if ((gpsFlags & GPS_LARGE_LAT) != 0) {
        if (ac1 > ac2) {
          latitude = c1;
          longitude = c2;
        } else {
          latitude = c2;
          longitude = c1;
        }
      } else if ((gpsFlags & GPS_LARGE_LONG) != 0) {
        if (ac2 > ac1) {
          latitude = c1;
          longitude = c2;
        } else {
          latitude = c2;
          longitude = c1;
        }
      } 
      
      // No luck there  there are some other tricks we can try if one
      // coordinate is negative but the other is not.  Then check to
      // see which coordinate is supposed to be negative
      else if (c1 < 0 ^ c2 < 0) {
        if ((gpsFlags & GPS_NEG_LAT|GPS_NEG_LONG) == GPS_NEG_LAT) {
          if (c1 < 0) {
            latitude = c1;
            longitude = c2;
          } else {
            latitude = c2;
            longitude = c1;
          }
        } else if ((gpsFlags & (GPS_NEG_LAT|GPS_NEG_LONG)) == GPS_NEG_LONG) {
          if (c2 < 0) {
            latitude = c1;
            longitude = c2;
          } else {
            latitude = c2;
            longitude = c1;
          }
        } else {
          latitude = c1;
          longitude = c2;
        }
      } 
      
      // Lacking any other information, assume first is lattitude and second is longitude
      else {
        latitude = c1;
        longitude = c2;
      }
      
      // Rest is easy, make sure anything that should be negative is negative
      if ((gpsFlags & GPS_NEG_LAT) != 0) {
        if (latitude > 0) latitude = - latitude;
      }
      if ((gpsFlags & GPS_NEG_LONG) != 0) {
        if (longitude > 0) longitude = - longitude;
      }
      
      // And convert the result to a GPS string
     return String.format(Locale.US, "%+8.6f,%+8.6f", latitude, longitude);
    }
 
    /**
     * Convert GPS coordinate in degree:min:sec form  or degree min form to strait degrees
     */
    private static double cvtGpsCoord(String coord) {
      Matcher match = GPS_COORD_PTN.matcher(coord);
      if (!match.matches()) throw new RuntimeException("This cannot possibly fail");
      String tmp = match.group(1);
      if (tmp != null) return Double.parseDouble(tmp);
      
      String sign;
      double degrees;
      double minutes;
      sign = match.group(2);
      if (sign != null) {
        degrees = Double.parseDouble(match.group(3));
        minutes = Double.parseDouble(match.group(4));
      } else {
        sign = match.group(5);
        degrees = Double.parseDouble(match.group(6));
        minutes = Double.parseDouble(match.group(7));
        double seconds = Double.parseDouble(match.group(8));
        minutes += seconds/60.0;
      }
      
      degrees += minutes/60;
      if (sign.equals("-")) degrees = - degrees;
      return degrees;
    }
   };
   
   /**
    * Pattern that identifies GPS coordinates in an arbitrary field
    */
   private static final String GPS_COORD_PTN_STR = "([-+]?[0-9]+\\.[0-9]{4,})|([-+]?)([0-9]+) +([0-9]+\\.[0-9]{2,})|([-+]?)([0-9]+):([0-9]+):([0-9]+\\.[0-9]{1,})";
   private static final Pattern GPS_COORD_PTN = Pattern.compile(GPS_COORD_PTN_STR);
   private static final String GPS_COORD_PTN_STR2 = GPS_COORD_PTN_STR.replace("(", "").replace(")","");
   public static final Pattern GPS_PATTERN = 
       Pattern.compile("\\b(X: *)?(" + GPS_COORD_PTN_STR2 + ")[,\\W]\\W*(Y: *)?(" + GPS_COORD_PTN_STR2 + ")\\b");
 
   /**
    * Parse flag indicates that sender address filtering should not be checked
    */
   public static final int PARSE_FLG_SKIP_FILTER = 0x01;
   
   /**
    * Parse flag indicates that message has been positively ID's as coming from
    * this dispatch center
    */
   public static final int PARSE_FLG_POSITIVE_ID = 0x02;
   
   /**
    * Parse flag indicates that messages from dispatch that are not CAD pages
    * should be treated as general alerts.  This include messages identified as
    * run reports 
    */
   public static final int PARSE_FLG_GEN_ALERT = 0x04;
   
   /**
    * Parse flag indicates that messages that have been identified as run reports
    * should be returned.  But messages identified as  general reports should not
    */
   
   public static final int PARSE_FLG_RUN_REPORT = 0x08;
   
   /**
    * Parser flag indicating that we are being called from a parser test class.
    * This suppresses the usual logic that reports general alerts identified
    * by individual parsers as parsing failures if the general alert processing
    * flag was not passed.
    */
   public static final int PARSE_FLG_TEST_MODE = 0x10;
 
   /**
    * Force flag forces processing of message
    */
   public static final int PARSE_FLG_FORCE = 0x7;
   
   // parser code
   private String parserCode;
   
   // Default city and state passed in constructor
   private String defCity;
   private String defState;
   
   // Parser country code
   private CountryCode countryCode;
   
   // Save parse flags so we can check message status from methods that
   // were not passed the parse flags
   private int parseFlags;
   
   // Pattern matching a terminated string of digits
   public static final Pattern NUMERIC = Pattern.compile("\\b\\d+\\b");
   
   public MsgParser(String defCity, String defState) {
     this(defCity, defState, CountryCode.US);
   }
   
   public MsgParser(String defCity, String defState, CountryCode countryCode) {
     this.defCity = defCity;
     this.defState = defState;
     this.countryCode = countryCode;
     
     String clsName = this.getClass().getName();
     int ipt = clsName.lastIndexOf('.');
     parserCode = clsName.substring(ipt+1, clsName.length()-6);
 
   }
 
   /**
    * @return parsers default city
    */
   public String getDefaultCity() {
     return defCity;
   }
 
   /**
    * @return parsers default state
    */
   public String getDefaultState() {
     return defState;
   }
   
   public void setDefaultCity(String defCity) {
     this.defCity = defCity;
   }
   
   public void setDefaultState(String defState) {
     this.defState = defState;
   }
   
   /**
    * @return parser country code
    */
   public CountryCode getCountryCode() {
     return countryCode;
   }
   
   /**
    * Determine if message is a valid CAD message for this parser, and parse
    * all information from the message if it is
    * @param msg message to be parsed
    * @overrideFilter true if parser filters should be overridden
    * @genAlert true if general alerts should be accepted
    * @return true if this message contain the text phrases that indicate it is 
    * a valid page message
    */
   public synchronized boolean isPageMsg(Message msg, int parseFlags) {
     
     // Has to be synchronized because this is called from the MMS service thread
     // And we only have one global copy of parsing flags for each parser
     // Save parse flags for future reference
     this.parseFlags = parseFlags;
     
     // See what the parseMsg method thinks of this
     Data data = parseMsg(msg, parseFlags);
     
     // If all parsers failed, return false
     if (data == null) return false;
     
     // Save parser code and information object in message so we won't have to
     // go through all of this again
     msg.setInfo(new MsgInfo(data));
     return true;
   }
 
   /**
    * build information object from information parsed from message
    * @param msg message to be parsed
    * @param parseFlags 
    * @return new message information object if successful, false otherwise
    */
   protected Data parseMsg(Message msg, int parseFlags) {
 
     // If parser filter is not being overridden, and the message address does not
     // match the parser filter, message should be rejected
     String filter = getFilter();
     boolean overrideFilter = (parseFlags & PARSE_FLG_SKIP_FILTER) != 0;
     if (! overrideFilter && ! matchFilter(msg.getAddress(), filter)) return null;
     
     // Save parse flags for future reference (again)
     // We have to do this again because the GroupBestParser will call 
     // this method is sub parsers without calling the initial inPageMsg() method
     this.parseFlags = parseFlags;
     boolean parseGenAlert = (parseFlags & PARSE_FLG_GEN_ALERT) != 0;
     boolean parseRunReport = parseGenAlert || (parseFlags & PARSE_FLG_RUN_REPORT) != 0;
     
     // Decode the call page and place the data in the database
     String strSubject = msg.getSubject();
     String strMessage = htmlFilter(msg.getMessageBody()).trim();
     Data data = new Data(this);
     if (strMessage == null) return data;
     if (parseMsg(strSubject, strMessage, data)) {
       
       // Generally, if the parse was happy with the call, we are happy.  One exception
       // is when individual parser determine a message should be a general alert because
       // it isn't a real CAD page, but it has enough identifying markers to positively
       // identify it as coming from Dispatch.  If the user didn't want to process
       // general alerts, and we aren't running in a test class, change this to
       // an outright failure
       if ((parseFlags & PARSE_FLG_TEST_MODE) == 0) {
         if (!parseGenAlert && data.strCall.equals("GENERAL ALERT")) return null;
         if (!parseRunReport && data.strCall.equals("RUN REPORT")) return null;
       }
       return data;
     }
     
     // If this isn't a valid CAD page, see if we should treat it as a general alert
     // If not then return failure
     if (parseGenAlert && isPositiveId()) {
       return ManageParsers.getInstance().getAlertParser().parseMsg(msg, parseFlags);
     }
     
     // Otherwise return null
     return null;
   }
   
   /**
    * Determine if this message has been identified as coming from the dispatch
    * center we are parsing messages for
    * @return true if it is, false otherwise
    */
   public boolean isPositiveId() {
     
     // If the caller flagged this as positively ID's, so be it
     if ((parseFlags & PARSE_FLG_POSITIVE_ID) != 0) return true;
     
     // Otherwise consider this a positive ID if the message had to pass
     // a non-empty filter
     if ((parseFlags & PARSE_FLG_SKIP_FILTER) != 0) return false;
     if (getFilter().length() <= 1) return false;
     
     return true;
   }
 
   /**
    * Parse information object from message
    * @param strSubject message subject to be parsed
    * @param strMessage message text to be parsed
    * @param data data object to be constructed
    * @return true if successful, false otherwise
    */
   protected boolean parseMsg(String strSubject, String strMessage, Data data) {
     return parseMsg(strMessage, data);
   }
 
   /**
    * Parse information object from message
    * @param strMessage message text to be parsed
    * @param data data object to be constructed
    * @return true if successful, false otherwise
    */
   protected boolean parseMsg(String strMessage, Data data) {
     throw new RuntimeException("parseMsg method was not overridden");
   }
 
   /**
    * If parser subclass provides an HTML filter pattern, and if messages starts with an HTML tag
    * use the HTML filter to extract the useful message data from the HTML message
    * @param body message body
    * @return adjusted message body
    */
   private String htmlFilter(String body) {
     Pattern ptn = getHtmlFilter();
     if (ptn == null) return body;
     if (!body.startsWith("<html>") && !body.startsWith("<HTML>")) return body;
     String result = "";
     for (String line : body.split("\n")) {
       Matcher match = ptn.matcher(line);
       if (match.matches()) {
         if (match.groupCount() > 0) {
           for (int ndx = 0; ndx < match.groupCount(); ndx++) {
             String tmp = match.group(ndx);
             if (tmp != null) {
               line = tmp;
               break;
             }
           }
         }
         result = append(result, "\n", line.trim());
       }
     }
     return result;
   }
   
   /**
    * @return Pattern used to extract data message from an HTML message
    */
   protected Pattern getHtmlFilter() {
     return null;
   }
   
   /**
    * @return sponsor of this user.  A non-null value means someone else is
    * paying and we aren't going to bug users for donations.
    */
   public String getSponsor() {
     
     // UK locations are free for now
     if (countryCode == CountryCode.UK) return "UK";
     if (countryCode == CountryCode.AU) return "AU";
     if (countryCode == CountryCode.NZ) return "NZ"; 
     return null;
   }
   
   /**
    * @return returns sponsor purchase date if it should be used to compute
    * a sponsor expiration date or null if the sponsorship should not expire
    * there is no sponsor expiration date.
    */
   public Date getSponsorDate () {
     String dateStr = getSponsorDateString();
     if (dateStr == null) return null;
     try {
       return DATE_FORMAT.parse(dateStr);
     } catch (ParseException ex) {
       throw new RuntimeException(ex);
     }
   }
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMddyyyy");
  
   /**
    * @return sponsor purchase date in MMDDYYYY form, or null if not defined
    */
   protected String getSponsorDateString() {
     return null;
   }
   
   /**
    * @return Filter associated with this parser
    */
   public String getFilter() {
     return "";
   }
 
   /**
    * @return map address adjustment flags<br>
    * MAP_FLG_SUPPR_LA suppresses LA -> LN adjustment<br>
    * MAP_FLG_SUPPR_EXT suppresses EXT removal
    * MAP_FLG_ADD_DEFAULT_CNTY always add default county
    * MAP_FLG_SUPPR_DIRO suppresses [NEWS]O -> & adjustment
    * MAP_FLG_SUPPR_ADD_PLACE suppress logic to add place name to naked streets
    * MAP_FLG_PREFER_GPS recomend GPS coordinates of map address for mapping purposes 
    */
   public int getMapFlags() {
     return 0;
   }
   public static final int MAP_FLG_SUPPR_LA = MsgInfo.MAP_FLG_SUPPR_LA;
   public static final int MAP_FLG_SUPPR_EXT = MsgInfo.MAP_FLG_SUPPR_EXT;
   public static final int MAP_FLG_ADD_DEFAULT_CNTY = MsgInfo.MAP_FLG_ADD_DEFAULT_CNTY;
   public static final int MAP_FLG_SUPPR_DIRO = MsgInfo.MAP_FLG_SUPPR_DIRO;
   public static final int MAP_FLG_SUPPR_ADD_PLACE = MsgInfo.MAP_FLG_SUPPR_ADD_PLACE;
   public static final int MAP_FLG_PREFER_GPS = MsgInfo.MAP_FLG_PREFER_GPS;
   public static final int MAP_FLG_SUPPR_SR = MsgInfo.MAP_FLG_SUPPR_SR;
 
 
   /**
    * Convenience method to identify a page message by checking to see if
    * it contains a sequence of key phrases in the proper order
    * @param body body of message to be checked
    * @param keywords array of key phrases used to identify paging message
    * @return true if all key phrases were found.
    */
   protected boolean isPageMsg(String body, String[] keywords) {
     int ipt = 0;
     for (String keyword : keywords) {
       keyword = keyword + ":";
       ipt = body.indexOf(keyword, ipt);
       if (ipt < 0) return false;
       ipt += keyword.length();
     }
     return true;
   }
   
   /**
    * @return Alias code.  This code will be non-null for parsers that are different aliases of another parser
    * and all of the parsers that are aliases of one another will return the same code.
    */
   public String getAliasCode() {
     return null;
   }
   
   /**
    * @return parser code identifying the parser that actually was used
    */
   public String getParserCode() {
     return parserCode;
   }
 
   /**
    * @return Human readable name of location parser
    */
   public String getLocName() {
     
     // Overridden in special cased, but general default is to build a name
     // from the default city and state
     if (defCity.length() == 0) return "";
 
     char[] carry = defCity.toCharArray();
     boolean upshift = true;
     for (int j = 0; j<carry.length; j++) {
       char chr = carry[j];
       if (chr == ' ') {
         upshift = true;
       } else if (upshift) {
         chr = Character.toUpperCase(chr);
         upshift = false;
       } else {
         chr = Character.toLowerCase(chr);
       }
       carry[j] = chr;
     }
     
     String locName = new String(carry);
     
     if (defState.length() > 0) {
       locName = locName + ", " + defState;
     }
     
     return locName;
   }
 
  
  /** 
   * General purpose parser for formats where there is not a clear delimiter
   * between key: value item pairs.
   * @param body message body to be parsed
   * @param keyWords list of expected keywords
   * @return Properties object containing the parsed key: value pairs
   */
  protected static Properties parseMessage(String body, String[] keyWords) {
    
    Properties props = new Properties();
    String[] flds = parseMessageFields(body, keyWords);
    for (String fld : flds) {
      int pt = fld.indexOf(':');
      if (pt > 0) {
        String key = fld.substring(0,pt).trim();
        String value = fld.substring(pt+1).trim();
        props.setProperty(key, value);
      }
    }
    return props;
  }
  
  
  /** 
   * General purpose parser for formats where there is not a clear delimiter
   * between key: value item pairs.
   * @param body message body to be parsed
   * @param keyWords list of expected keywords
   * @return Array of data fields broken up by defined keywords
   */
  protected static String[] parseMessageFields(String body, String[] keyWords) {
    return parseMessageFields(body, keyWords, ':', false, false);
  }
  
  /** 
   * General purpose parser for formats where there is not a clear delimiter
   * between key: value item pairs.
   * @param body message body to be parsed
   * @param keyWords list of expected keywords
   * @param breakChar character that marks the end of all keywords
   * @param anyOrder true if if keywords can occur in any order
   * @param ignoreCase true if case should be ignored when comparing keywords
   * @return Array of data fields broken up by defined keywords
   */
  protected static String[] parseMessageFields(String body, String[] keyWords, 
                                                 char breakChar, boolean anyOrder, boolean ignoreCase) {
    
    List<String> fields = new ArrayList<String>();
    int iKey = -1;  // Current key table pointer
    int iStartPt = 0;   // current data field start index
    int iColonPt = iStartPt;
    int iNxtKey;
    
    // Loop processing each keyword found
    do {
      
      // Start searching for the next keyword starting at the current data field
      int iEndPt = -1;
      iNxtKey = -1;
      
      // This loop checks each break characters looking for one that
      // matches an available keyword
      while (true) {
        
        // Find the next colon character, if there isn't one, bail out
        int iDataPt = iColonPt;
        iColonPt = body.indexOf(breakChar, iColonPt+1);
        if (iColonPt < 0) break;
        
        int ipt = iColonPt;
        while (ipt>iDataPt && body.charAt(ipt-1)==' ') ipt--;
        
        // Next search the available keywords to see if this colon
        // defines one of them
        for (int ndx = iKey+1; ndx < keyWords.length; ndx++) {
          String key = keyWords[ndx];
          int len = key.length();
          int iTempPt = ipt - len;
          if (iTempPt < iDataPt) continue;
          if (iTempPt > 0) {
            char chr = body.charAt(iTempPt-1);
            if (!Character.isWhitespace(chr)) continue;
          }
          String keyword = body.substring(iTempPt, ipt);
          if (ignoreCase) keyword = keyword.toUpperCase();
          if (!keyword.equals(key)) continue;
          iNxtKey = ndx;
          iEndPt = iTempPt;
          break;
        }
        
        // If we found a keyword for this colon, bail out of loop
        // Otherwise try looking for another colon
        if (iEndPt >= 0) break;
      }
      
      // Coming up of the preceding loop, we either found a valid keyword
      // with index iNextPt, starting at iEndPT and ending at iColonPt.
      // Or we didn't.  If we didn't we need to go through some more
      // cleverness to find the end of the current field.
      if (iNxtKey < 0) {
        
        // Normally this would be the end of the message body
        // But we are going to be clever and see if the last token in 
        // the message body looks like a truncated available keyword.
        // if it is, we will trim that part off.
        iEndPt = body.length();
        int iTempPt = iEndPt;
        for (int cnt = 1; cnt <= 3; cnt++) {
          iTempPt = body.lastIndexOf(' ', iTempPt-1);
          if (iTempPt < 0) break;
          String tail = body.substring(iTempPt+1);
          for (int ndx = iKey+1; ndx < keyWords.length; ndx++) {
            if (keyWords[ndx].startsWith(tail)) {
              iEndPt = iTempPt;
              iTempPt = 0;
              break;
            }
          }
          if (iTempPt == 0) break;
        }
      }
      
      // By this point we have identified 
      //   iKey     current keyword index (or -1 if none)
      //   iNxtKey  Next keyword index (or -1 if none)
      //   iStartPt Start of data field associated with iKey
      //   iEndPt   End of data field associated with iKey
      //   iColonPn Colon terminating next keyword
      
      // Save current field and get ready to start looking for the
      // end of the next keyword
      if (iEndPt > 0) fields.add(body.substring(iStartPt, iEndPt).trim());
      if (!anyOrder) iKey = iNxtKey;
      iStartPt = iEndPt;
    } while (iNxtKey >= 0);
    
    return fields.toArray(new String[fields.size()]);
  }
 
  /**
   * General purpose message parser for formats where there is a clear delimiter
   * between key: value data pairs 
   * @param body
   * @param delim line delimiter
   * @return
   */
  protected static Properties parseMessage(String body, String delim) {
    return parseMessage(body, delim, null);
  }
  
  /**
   * General purpose parser
   * @param body of message to be parsed
   * @param delim expression to be used to separate lines in message
   * @param missedKeys if not null, an array of keywords to be assigned to
   * lines that are missing a keyword
   * @return Properties object containing the parsed line tokens
   */
  protected static Properties parseMessage(String body, String delim, String[] missedKeys) {
    
      Properties props = new Properties();
      String[] lines = body.split(delim);
      int missed = 0;
      for (String line : lines) {
          line = line.trim();
          int ndx = line.indexOf(':');
          if (ndx < 0) {
            if (missedKeys != null && missed < missedKeys.length) {
              props.put(missedKeys[missed++], line);
            }
            continue;
          }
          String key = line.substring(0, ndx).trim();
          String value = line.substring(ndx+1).trim();
          props.put(key, value);
      }
      return props;
  }
 
  /**
   * Parse address line into address and city fields treating anything behind a
   * dash as a city
   * @param addressLine address line to be parsed
   * @param data message info object to be filled
   */
  protected static void parseAddressCity(String addressLine, MsgInfo.Data data) {
    parseAddress(addressLine, data, true); 
  }
 
  /**
   * Parse address line into address fields
   * @param addressLine address line to be parsed
   * @param data message info object to be filled
   */
  protected void parseAddress(String addressLine, MsgInfo.Data data) {
    parseAddress(addressLine, data, false); 
  }
  
  /**
   * Perform any parser specific customizations involved in calcualting a
   * map address
   * @param sAddress original map address
   * @return customized map address
   */
  public String adjustMapAddress(String sAddress) {
    return sAddress;
  }
 
  /**
   * Perform an final parser specific custom adjustments to the calcualted
   * map search address 
   * @param sAddress calculated map search address
   * @return adjusted map search address
   */
  public String postAdjustMapAddress(String sAddress) {
    return sAddress;
  }
 
  /**
   * Parse address line into address and city fields
   * @param addressLine address line to be parsed
   * @param data message info object to be filled
   * @param parseCity true if cities should be parsed with dashes
   */
  private static final Pattern INTERSECT = Pattern.compile("/|&");
  private static final Pattern APT = Pattern.compile("((?:\\bAPTS?|\\bUNIT|\\bSUITE|\\bROOM|\\bSTE|\\bRM|\\bFLOOR|\\bFLR|\\bLOT)(?![A-Z])|\\bAPT|#APT|#)[ #\\.]*(.+)$",Pattern.CASE_INSENSITIVE);
  private static final Pattern DOT = Pattern.compile("\\.(?!\\d)");
  private static void parseAddress(String addressLine, MsgInfo.Data data, 
                                      boolean parseCity) {
    addressLine = addressLine.trim();
    
    // Periods used with abbreviations also cause trouble.  Just get rid of all periods
    // except those followed by a digit which are presumed to be decimal points
    addressLine = DOT.matcher(addressLine).replaceAll("");
    
    // Pick off trailing apartment
    Matcher match = APT.matcher(addressLine);
    if (match.find()) {
      String sPrefix = match.group(1);
      if (!sPrefix.startsWith("FL")) sPrefix = "";
      data.strApt = append(sPrefix, " ", match.group(2));
      addressLine = addressLine.substring(0,match.start()).trim();
    }
 
    addressLine = addressLine.replace("1/2", "1%2");
    for (String addr : INTERSECT.split(addressLine)) {
      addr = addr.trim();
      if (parseCity) {
        int pt = addr.lastIndexOf('-');
        if (pt > 0) {
          if (data.strCity.length() == 0) data.strCity = addr.substring(pt+1).trim();
          addr = addr.substring(0, pt).trim();
        }
      }
      data.strAddress = append(data.strAddress, " & ", addr);
    }
    data.strAddress = data.strAddress.replace("1%2", "1/2");
  }
   
   /**
    * Set formated date/time field
    * @param dateFmt Date format to be used to parse date/time string
    * @param field date/time string to be parsed
    * @param data Data object where information should be saved
    * @param returns true if field was parsed successfully
    */
   public static boolean setDateTime(DateFormat dateFmt, String field, Data data) {
     try {
        Date date = dateFmt.parse(field);
        DateFormat fmt = dateFmt.getCalendar().get(Calendar.YEAR) == 1970 ? DATE_FMT2 : DATE_FMT;
        data.strDate = fmt.format(date);
        data.strTime = TIME_FMT.format(date);
        return true;
     } catch (ParseException ex) {
       return false;
     }
   }
   
   /**
    * Set formated date field
    * @param dateFmt Date format to be used to parse date string
    * @param field date string to be parsed
    * @param data Data object where information should be saved
    * @param returns true if field was parsed successfully
    */
   public static boolean setDate(DateFormat dateFmt, String field, Data data) {
     try {
        Date date = dateFmt.parse(field);
        DateFormat fmt = dateFmt.getCalendar().get(Calendar.YEAR) == 1970 ? DATE_FMT2 : DATE_FMT;
        data.strDate = fmt.format(date);
        return true;
     } catch (ParseException ex) {
       return false;
     }
   }
   
   /**
    * Set formated time field
    * @param dateFmt Date format to be used to parse date/time string
    * @param field time string to be parsed
    * @param data Data object where information should be saved
    * @param returns true if field was parsed successfully
    */
   public static boolean setTime(DateFormat dateFmt, String field, Data data) {
     try {
        Date date = dateFmt.parse(field);
        data.strTime = TIME_FMT.format(date);
        return true;
     } catch (ParseException ex) {
       return false;
     }
   }
   
   private static final DateFormat DATE_FMT = new SimpleDateFormat("MM/dd/yyyy");
   private static final DateFormat DATE_FMT2 = new SimpleDateFormat("MM/dd");
   private static final DateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");
  
   
   /**
    * Set GPS Coordinates in standard Google search format
    * handling special case where trailing zeros have been dropped
    * from the coordinates
    * @param location GPS coordinates to be saved
    * @param data data information object
    */
   public void setTrimmedGPSLoc(String location, Data data) {
     location = END_NUMBER.matcher(location).replaceAll("000");
     setGPSLoc(location, data);
   }
   private static final Pattern END_NUMBER = Pattern.compile("(?<=\\d)(?= |$)");
   
   
   /**
    * Set GPS Coordinates in standard Google search format
    * @param location GPS coordinates to be saved
    * @param data data information object
    */
   public void setGPSLoc(String location, Data data) {
     String gpsLoc = countryCode.parseGPSCoords(location);
     data.strGPSLoc = (gpsLoc != null ? gpsLoc : "");
   }
 
  /**
   * Build a code table for use by convertCodeTable
   * @param table array containing pairs of strings, each pair will be considered
   * as a key/value pair to be inserted into the code table
   * @return resulting code table
   */
  protected static Properties buildCodeTable(String[] table) {
    Properties props = new Properties();
    for (int ndx = 0; ndx < table.length-1; ndx+=2) {
      props.put(table[ndx], table[ndx+1]);
    }
    return props;
  }
  
  /**
   * Determine if this is a personal name or a place name
   * @param field field to be checked
   * @return true if should be place name, false if a personal name
   */
  protected boolean checkPlace(String field) {
    if (field.contains(",")) return false;
    if (field.toUpperCase().startsWith("MR")) return false;
    int cnt = 0;
    char last = 'X';
    for (char chr : field.toCharArray()) {
      if (chr == ' ' && last != ' ') cnt++;
      last = chr;
    }
    return cnt >= 2;
  }
  
  /**
   * Look for an abbreviated form of a full street name.  If found, expand it to the full
   * street name 
   * @param fullName Full street name
   * @param field address field containing possible abbreviated street name
   * @return address field with street name expanded to full name
   */
  protected String expandStreet(String fullName, String field) {
    int trigLen  = fullName.lastIndexOf(' ');
    if (trigLen < 0) return field;
    trigLen += 2;
    if (trigLen > fullName.length()) return field;
    String trigger = fullName.substring(0,trigLen);
    int pt1 = 0;
    while (true) {
      int pt2 = field.indexOf(trigger, pt1);
      if (pt2 < 0) break;
      int pt3 = field.indexOf(' ', pt2+trigLen);
      if (pt3 < 0) pt3 = field.length();
      if (fullName.startsWith(field.substring(pt2,pt3))) {
        field = field.substring(0,pt2) + fullName + field.substring(pt3);
        pt3 += fullName.length()-(pt3-pt2);
      }
      pt1 = pt3;
    }
    return field;
  }
 
  /**
   * Convert any special codes in an item
   * @param item String item to be converted
   * @param codeTable table of codes with corresponding values
   * @return if item is a key to a codeTable entry, returns the associated value,
   * otherwise returns item
   */
  protected static String convertCodes(String item, Properties codeTable) {
    String value = codeTable.getProperty(item);
    return (value != null ? value : item);
  }
  
  /**
   * Utility method to cut a selected subfield out of a string field and 
   * return everything that is left
   * @param field field are working on
   * @param start start index of subfield to be removed
   * @param end end index of subfield to be removed
   * @return Remainder of field once subfield has been removed.
   */
  protected static String cutOut(String field, int start, int end) {
    String pfx = field.substring(0,start).trim();
    String sfx = field.substring(end).trim();
    if (pfx.length() == 0) return sfx;
    if (sfx.length() == 0) return pfx;
    return pfx + " " + sfx;
  }
  
  /**
   * Class containing a list of strings that tokens will need to be checked against
   */
  protected static class MatchList {
    
    private Set<String> set;
 
    /**
     * Constructor
     * @param list list of strings that tokens will be checked against
     */
    public MatchList(String[] list) {
      set = new HashSet<String>(list.length);
      for (String entry : list) set.add(entry.toUpperCase());
    }
    
    /**
     * Determine if token is in list
     * @param token token to be checked
     * @return true if token is in list
     */
    public boolean contains(String token) {
      return set.contains(token.toUpperCase());
    }
  }
  
  /**
   * Convenience method to append two strings with a connector
   * @param str1 first string
   * @param connector connector string
   * @param str2 second string
   * @return appended string
   */
  protected static String append(String str1, String connector, String str2) {
    if (str1.length() == 0) return str2;
    if (str2.length() == 0) return str1;
    return str1 + connector + str2;
  }
  
  /**
   * Covenience method to get results of a (possibly null) match group
   * @param input resuts of the Matcher.group() call
   * @return unnullified and trimmed result.
   */
  protected String getOptGroup(String input) {
    return (input == null ? "" : input.trim());
  }
 
  /**
   * Convenience method to remove any extended charset characters from input data
   * @param line input data string
   * @return input data string purged of any extended charset characters
   */
  protected String cleanExtendedChars(String line) {
    StringBuilder sb = null;
    for (int ndx = 0; ndx < line.length(); ndx++) {
      char ch = line.charAt(ndx);
      if (ch > 127) {
        if (sb == null) sb = new StringBuilder(line.substring(0,ndx));
      } else {
        if (sb != null) sb.append(ch);
      }
    }
    return (sb == null ? line : sb.toString());
  }
  
  /**
   * Convience method to extract substring from string which might not
   * be long enough to contain the full substring
   * @param body original string
   * @param st start position
   * @return substring
   */
  public String substring(String body, int st) {
    return substring(body, st, Integer.MAX_VALUE);
  }
  
  
  /**
   * Convience method to extract substring from string which might not
   * be long enough to contain the full substring
   * @param body original string
   * @param st start position
   * @param end end position
   * @return substring
   */
  public String substring(String body, int st, int end) {
    int len = body.length();
    if (st >= len) return "";
    if (end > len) end = len;
    return body.substring(st, end).trim();
  }
  
  /**
   * Worker class that will parse a into consecutive substrings up to the
   * next occurrence of a particular parser
   */
  public static class Parser {
    
    private String line;
    private int spt;
    private int ept;
 
    /**
     * Constructor 
     * @param line string to be parsed out
     */
    public Parser(String line) {
      init(line);
    }
 
    /**
     * Reset parser with new string
     * @param line new string to be parsed
     */
    public void init(String line) {
      this.line = line;
      this.spt = 0;
      this.ept = line.length();
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String get(char delim) {
      return get(delim, false, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getOptional(char delim) {
      return get(delim, true, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String getRequired(char delim) {
      return get(delim, false, true);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String getLast(char delim) {
      return getLast(delim, false, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getLastOptional(char delim) {
      return getLast(delim, true, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getLastRequired(char delim) {
      return getLast(delim, false, true);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String get(String delim) {
      return get(delim, false, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getOptional(String delim) {
      return get(delim, true, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getRequired(String delim) {
      return get(delim, false, true);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found
     */
    public String getLast(String delim) {
      return getLast(delim, false, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getLastOptional(String delim) {
      return getLast(delim, true, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getLastRequired(String delim) {
      return getLast(delim, false, true);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String get(Pattern delim) {
      return get(delim, false, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getOptional(Pattern delim) {
      return get(delim, true, false);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter if found, empty string otherwise
     */
    public String getRequired(Pattern delim) {
      return get(delim, false, true);
    }
    
    /**
     * @param delim delimiter
     * @return everything up to next occurrence of delimiter
     */
    public String get() {
      return get(-1, 0, false, false);
    }
    
    /**
     * @param delim delimiter
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return everything up to next occurrence of delimiter
     */
    private String get(char delim, boolean optional, boolean required) {
      return get(line.indexOf(delim, spt), 1, optional, required);
    }
    
    /**
     * @param delim delimiter
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return everything up to next occurrence of delimiter
     */
    private String get(String delim, boolean optional, boolean required) {
      return get(line.indexOf(delim, spt), delim.length(), optional, required);
    }
    
    /**
     * @param delim delimiter
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return everything up to next occurrence of delimiter
     */
    private String getLast(char delim, boolean optional, boolean required) {
      return getLast(line.lastIndexOf(delim, ept-1), 1, optional, required);
    }
    
    /**
     * @param delim delimiter
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return everything up to next occurrence of delimiter
     */
    private String getLast(String delim, boolean optional, boolean required) {
      int len = delim.length();
      return getLast(line.lastIndexOf(delim, ept-len), len, optional, required);
    }
    
    /**
     * @param ptn delimiter pattern
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return everything up to next occurrence of delimiter
     */
    private String get(Pattern ptn, boolean optional, boolean required) {
      Matcher match = ptn.matcher(line);
      int ndx = -1;
      int len = 0;
      if (match.find(spt)) {
        ndx = match.start();
        len = match.end()-ndx;
      }
      return get(ndx, len, optional, required);
    }
    
    /**
     * @param npt index returned by indexof search
     * @param len length of delimiter searched for
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return whatever was found
     */
    private String get(int npt, int len, boolean optional, boolean required) {
      if (npt < 0 || npt+len > ept) {
        if (optional) return "";
        if (required) return null;
        npt = ept;
        len = 0;
      }
      String result = line.substring(spt, npt).trim();
      spt = npt + len;
      while (spt < ept && line.charAt(spt)==' ') spt++;
      return result;
    }
    
    /**
     * @param npt index returned by indexof search
     * @param len length of delimiter searched for
     * @param optional true if empty string should be returned if deliminter not found
     * @param required true if null should be returned if delimiter not found
     * @return whatever was found
     */
    private String getLast(int npt, int len, boolean optional, boolean required) {
      if (npt < 0 || npt<spt) {
        if (optional) return "";
        if (required) return null;
        npt = spt;
        len = 0;
      }
      String result = line.substring(npt+len, ept).trim();
      ept = npt;
      while (ept > spt && line.charAt(ept-1)==' ') ept--;
      return result;
    }
    
    /**
     * Search for the next occurrance of a specific pattern and return the match result
     * @param ptn Pattern for which we are searching
     * @return a Match object matching the specified pattern if a match was found. Null 
     * if no match was found
     */
    public Matcher getMatcher(Pattern ptn) {
      Matcher match = ptn.matcher(line);
      if (!match.find(spt)) return null;
      
      spt = match.end();
      return match;
    }
    
    /**
     * Convenience method to return first group of a pattern match
     * @param ptn Pattern to be searched for
     * @return First group value if match found, empty string otherwise
     */
    public String getMatcherValue(Pattern ptn) {
      Matcher match = getMatcher(ptn);
      return match == null ? "" : match.group(1);
    }
  }
 
  /**
   * Determine if message address matches address filter
   * @param sAddress message address
   * @param sFilter address filter
   * @return true if message address satisfies filter
   */
  public static boolean matchFilter(String sAddress, String sFilter) {
    
    if (sFilter == null) return true;
    
    // A filter with length of 0 or 1 is invalid and is always passed
    sFilter = sFilter.trim();
    if (sFilter.length() <= 1) return true;
    
    // Filter can consist of multiple address filters separated by comas
    for (String tFilter : sFilter.split(",")) {
      tFilter = tFilter.trim();
      
      // A subfilter with length of 0 or 1 is invalid and is ignored
      // Doing otherwise makes it too difficult to determine whether or not
      // an active filter is in place
      if (tFilter.length() <= 1) continue;
      
      // If filter consists only of numeric digits, it needs to match the
      // beginning of what is presumably a phone number. Taking some pains
      // to eliminate the spurious +1 that sometimes gets added to the the
      // sender phonen number
      if (DIGITS.matcher(tFilter).matches()) {
        String tmp = sAddress;
        if (tmp.startsWith("+")) tmp = tmp.substring(1);
        if (tmp.startsWith("1") && ! tFilter.startsWith("1")) tmp = tmp.substring(1);
        if (tmp.startsWith(tFilter)) return true;
      } 
      
      // Otherwise it can
      // match any substring of the sender address.  This last
      // check should be case insensitive, which we accomplish by downshifting
      // both the address and the filter
      else {
        if (sAddress.toLowerCase().contains(tFilter.toLowerCase())) return true;
      }
    }
    return false;
   }
   private static final Pattern DIGITS = Pattern.compile("\\d+"); 
 }
