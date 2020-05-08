 package net.anei.cadpage.parsers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.ManageParsers;
 import net.anei.cadpage.SmsMmsMessage;
 import net.anei.cadpage.SmsMsgInfo;
 import net.anei.cadpage.SmsPopupUtils;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 
 /**
  * This class is responsible for parsing useful information from an SMS page message
  */
 public abstract class SmsMsgParser {
   
   private String defCity;
   private String defState;
   
   // Pattern matching a terminated string of digits
   public static final Pattern NUMERIC = Pattern.compile("\\b\\d+\\b");
   
   public SmsMsgParser(String defCity, String defState) {
     this.defCity = defCity;
     this.defState = defState;
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
   
   /**
    * build information object
    * @param strMessage SMS message text to be parsed
    * @return new message information object if successful, false otherwise
    */
   protected Data parseMsg(SmsMmsMessage msg, boolean overrideFilter, boolean genAlert) {
 
     // If parser filter is not being overridden, and the message address does not
     // match the parser filter, message should be rejected
     String filter = getFilter();
     if (! overrideFilter && ! SmsPopupUtils.matchFilter(msg.getAddress(), filter)) return null;
     
     // Decode the call page and place the data in the database
     String strSubject = msg.getSubject();
     String strMessage = msg.getMessageBody();
     Data data = new Data();
     data.defCity = defCity;
     data.defState = defState;
    if (strMessage == null) return data;
     if (parseMsg(strSubject, strMessage, data)) return data;
     
     // If this isn't a valid CAD page, see if we should treat it as a general alert
     // If not then return failure
     if (!overrideFilter && genAlert && filter.length() > 1) {
       return ManageParsers.getInstance().getAlertParser().parseMsg(msg, overrideFilter, genAlert);
     }
     return null;
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
    * @return Filter associated with this parser
    */
   public String getFilter() {
     return "";
   }
   
   
   /**
    * Determine if message is a valid CAD message for this parser, and parse
    * all information from the message if it is
    * @param msg message to be parsed
    * @return true if this message contain the text phrases that indicate it is 
    * a valid page message
    */
   public boolean isPageMsg(SmsMmsMessage msg) {
     return isPageMsg(msg, true, false);
   }
   
   
   /**
    * Determine if message is a valid CAD message for this parser, and parse
    * all information from the message if it is
    * @overrideFilter true if parser filters should be overridden
    * @genAlert true if general alerts should be accepted
    * @param msg message to be parsed
    * @return true if this message contain the text phrases that indicate it is 
    * a valid page message
    */
   public boolean isPageMsg(SmsMmsMessage msg, boolean overrideFilter, boolean genAlert) {
     
     // See what the parseMsg method thinks of this
     Data data = parseMsg(msg, overrideFilter, genAlert);
     if (data == null) return false;
     
     // Save parser code and information object in message so we won't have to
     // go through all of this again
     SmsMsgInfo info = new SmsMsgInfo(data);
     msg.setParserInfo(getParserCode(), info);
     return true;
   }
 
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
       ipt = body.indexOf(keyword, ipt);
       if (ipt < 0) return false;
       ipt += keyword.length();
     }
     return true;
   }
   
   /**
    * @return parser code identifying the parser that actually was used
    */
   public String getParserCode() {
     // This will be overridden in the parser group classes, but here we
     // just extract the parser code from our own class name
     
     String clsName = this.getClass().getName();
     int ipt = clsName.lastIndexOf('.');
     return clsName.substring(ipt+1, clsName.length()-6);
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
    
    List<String> fields = new ArrayList<String>();
    int iKey = -1;  // Current key table pointer
    int iStartPt = 0;   // current data field start index
    int iColonPt = iStartPt;
    
    // Loop processing each keyword found
    do {
      
      // Start searching for the next keyword starting at the current data field
      int iEndPt = -1;
      int iNxtKey = -1;
      
      // This loop checks each ':' characters looking for one that
      // matches an available keyword
      while (true) {
        
        // Find the next colon character, if there isn't one, bail out
        int iDataPt = iColonPt;
        iColonPt = body.indexOf(':', iColonPt+1);
        if (iColonPt < 0) break;
        
        // Next search the available keywords to see if this colon
        // defines one of them
        for (int ndx = iKey+1; ndx < keyWords.length; ndx++) {
          String key = keyWords[ndx];
          int len = key.length();
          int iTempPt = iColonPt - len;
          if (iTempPt < iDataPt) continue;
          if (iTempPt > 0 && body.charAt(iTempPt-1)!=' ') continue;
          if (!body.substring(iTempPt,iColonPt).equals(key)) continue;
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
        for (int cnt = 1; cnt <= 2; cnt++) {
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
      fields.add(body.substring(iStartPt, iEndPt).trim());
      iKey = iNxtKey;
      iStartPt = iEndPt;
    } while (iKey >= 0);
    
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
  protected static void parseAddressCity(String addressLine, SmsMsgInfo.Data data) {
    parseAddress(addressLine, data, true); 
  }
 
  /**
   * Parse address line into address fields
   * @param addressLine address line to be parsed
   * @param data message info object to be filled
   */
  protected void parseAddress(String addressLine, SmsMsgInfo.Data data) {
    parseAddress(addressLine, data, false); 
  }
 
  /**
   * Parse address line into address and city fields
   * @param addressLine address line to be parsed
   * @param data message info object to be filled
   * @param parseCity true if cities should be parsed with dashes
   */
  private static final Pattern INTERSECT = Pattern.compile("/|&");
  private static final Pattern APT = Pattern.compile("(?:\\bAPT\\b|#APT\\b|#) *([^ ]+)$");
  private static void parseAddress(String addressLine, SmsMsgInfo.Data data, 
                                      boolean parseCity) {
    addressLine = addressLine.trim();
    
    // Periods used with abbreviations also cause trouble.  Just get rid of all periods
    // Unless this address has GPS coordinates, in which case the periods are critical
    if (!SmsPopupUtils.GPSPattern.matcher(addressLine).find()) {
      addressLine = addressLine.replace(".", "");
    }
    
    // Pick off trailing address
    Matcher match = APT.matcher(addressLine);
    if (match.find()) {
      data.strApt = match.group(1);
      addressLine = addressLine.substring(0,match.start()).trim();
    }
 
    for (String addr : INTERSECT.split(addressLine)) {
      addr = addr.trim();
      if (parseCity) {
        int pt = addr.lastIndexOf('-');
        if (pt > 0) {
          data.strCity = addr.substring(pt+1).trim();
          addr = addr.substring(0, pt).trim();
        }
      }
      if (data.strAddress.length() > 0) data.strAddress += " & ";
      data.strAddress += addr;
    }
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
  protected String append(String str1, String connector, String str2) {
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
  }
 }
