 package net.anei.cadpage;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.donation.DonationManager;
 import net.anei.cadpage.parsers.SmsMsgParser;
 import net.anei.cadpage.vendors.VendorManager;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.SmsMessage;
 import android.telephony.SmsMessage.MessageClass;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 import static net.anei.cadpage.BroadcastBindings.*;
 
 public class SmsMmsMessage implements Serializable {
 
   // Serialized object version code
   private static final long serialVersionUID = 1L;
 
   // Message types
   public static final int MESSAGE_TYPE_SMS = 0;
   public static final int MESSAGE_TYPE_MMS = 1;
   public static final int MESSAGE_TYPE_C2DM = 2;
 
   // Main message object private vars
   private String fromAddress = null;
   private String messageBody = null;
   
   // List of messages appended to original message
   // Yes it would have made more sense to put the original message in the list
   // as well, but then we would loose backward compatibility with messages
   // saved before the extra message list was added.
   private List<String> extraMsgBody = null;
   
   private long timestamp = 0;
   private int messageType = 0;
   private MessageClass messageClass = null;
   private boolean read = false;             
   private boolean locked = false;
   private transient SmsMsgInfo info = null;
   private transient Date incidentDate = null;
   private int msgId = 0;
   private String location = null;
   
   // Content location for MMS messages
   private String contentLoc = null;
   private String mmsMsgId = null;
   private String subject = null;
   
   // Location code and sponsor C2DM messages
   private String reqLocation = null;
   private String sponsor = null;
   private String ackReq = null;
   private String ackURL = null;
   private boolean ackNeeded = false;
   
   private transient String parseAddress = null;
   private transient String parseSubject = null;
   private transient String parseMessageBody = null;
   private transient int msgIndex = -1;
   private transient int msgCount = -1;
   
   // Temporary fields being monitored to see if they will be of any
   // use in identifying multi-part messages
   private long sentTime = 0L;
   private int iccIndex = 0;
 
   public boolean isRead() {
     return read;
   }
   
   public void setRead(boolean read) {
     if (read == this.read) return;
     this.read = read;
     reportDataChange();
     SmsMessageQueue.getInstance().calcNewCallCount();
   }
   
   public boolean isLocked() {
     return locked;
   }
   
   public void setLocked(boolean locked) {
     if (locked == this.locked) return;
     this.locked = locked;
     reportDataChange();
   }
   
   public void toggleLocked() {
     setLocked(! isLocked());
   }
   
   /**
    * @return true if this message can be deleted
    */
   public boolean canDelete() {
     
     // Can only be deleted if read and not locked
     // Config setting allows unread messages to be deleted
     return (read || ManagePreferences.deleteUnopen()) && ! locked;
   }
   
   public int getMsgId() {
     return msgId;
   }
   
   public void setMsgId(int msgId) {
     this.msgId = msgId;
   }
   
   private void reportDataChange() {
     if (msgId > 0) SmsMessageQueue.getInstance().notifyDataChange();
   }
 
   /**
    * Construct SMS SmsMmsMessage given a raw message (created from pdu), used for when
    * a message is initially received via the network.
    */
   public SmsMmsMessage(SmsMessage[] messages, long _timestamp) {
     SmsMessage sms = messages[0];
 
     timestamp = _timestamp;
     messageType = MESSAGE_TYPE_SMS;
 
     /*
      * Fetch data from raw SMS
      */
     fromAddress = sms.getDisplayOriginatingAddress();
     messageClass = sms.getMessageClass();
     sentTime = sms.getTimestampMillis();
     iccIndex = sms.getIndexOnIcc();
 
     String body;
     if (messages.length == 1 || sms.isReplace()) {
       body = sms.getDisplayMessageBody();
     } else {
       StringBuilder bodyText = new StringBuilder();
       for (int i = 0; i < messages.length; i++) {
         // getMessageBody has been throwing exceptions on occasion :(
         try {
           bodyText.append(messages[i].getMessageBody());
         } catch (NullPointerException ex) {Log.e(ex);}
       }
       body = bodyText.toString();
     }
     messageBody = body;
     
     // Calculate the from address and body to be used for parsing purposes
     getParseInfo();
   }
   
 
   /**
    * Construct MMS SmsMmsMessage given a raw message (created from pdu), used for when
    * a message is initially received via the network.
    */
   public SmsMmsMessage(MessageClass messageClass, String fromAddress, 
                         String subject, String contentLoc, String mmsMsgId,
                         long timestamp) {
     this.timestamp = timestamp;
     messageType = MESSAGE_TYPE_MMS;
     this.messageClass = messageClass;
     this.fromAddress = this.parseAddress = fromAddress;
     this.subject = (subject == null ? "" : subject);
     this.contentLoc = contentLoc;
     this.mmsMsgId = mmsMsgId;
     this.messageBody = this.parseMessageBody = null;
   }
   
   /**
    * Set the message body for an MMS type message when it is finally retrieved
    * @param messageBody new message body text
    */
   public void setMessageBody(String messageBody) {
     this.messageBody = messageBody;
     
     // Calculate the from address and body to be used for parsing purposes
     getParseInfo();
   }
 
   /**
    * Create a timeout marker message.  This servers no purpose other than
    * to mark an MMS message timeout in the log buffer
    * @return timeout marker message to be logged
    */
   public SmsMmsMessage timeoutMarker() {
     SmsMmsMessage result = 
           new SmsMmsMessage(messageClass, fromAddress, subject, contentLoc, 
                             mmsMsgId, System.currentTimeMillis());
     result.messageBody = "*** Content Timeout ***";
     return result;
   }
   
   /**
    * Construct SmsMmsMessage from information received in a C2DM message 
    * @param from message sender
    * @param subject message subject
    * @param messageBody message body
    * @param timestamp received times tamp
    * @param reqLocation requested location code
    * @param sponsor sponsor code
    * @param ackReq acknowledge request code
    * @param ackURL acknowledge URL
    */
   public SmsMmsMessage(String from, String subject, String messageBody,
                         long timestamp, String reqLocation, String sponsor, 
                         String ackReq, String ackURL) {
     this.messageType = MESSAGE_TYPE_C2DM;
     this.fromAddress = from;
     this.subject = subject;
     this.messageBody = messageBody;
     this.timestamp = timestamp;
     this.reqLocation = reqLocation;
     this.sponsor = sponsor;
     this.ackReq = ackReq;
     this.ackURL = ackURL;
     this.ackNeeded = ackReq.contains("A");
   }
 
   /**
    * Construct dummy SmsMmsMessage test notifications 
    */
   public SmsMmsMessage(String fromAddress, String subject, String messageBody,
                         long timestamp, int messageType) {
 
     this.fromAddress = fromAddress;
     this.messageBody = messageBody;
     this.timestamp = timestamp;
     this.messageType = messageType;
     this.location = "GeneralAlert";
     
     this.parseAddress = fromAddress;
     this.parseSubject = subject;
     this.parseMessageBody = messageBody;
   }
 
   /**
    * Construct test SmsMmsMessage message to check getParseInfo() logic 
    */
   public SmsMmsMessage(String fromAddress, String messageBody,
                         long timestamp, int messageType) {
 
     this.fromAddress = fromAddress;
     this.messageBody = messageBody;
     this.timestamp = timestamp;
     this.messageType = messageType;
     this.location = "GeneralAlert";
     
     getParseInfo();
   }
 
   /**
    * Construct combined message from a list of individual text messages
    * @param firstMsg the initially received message
    * @param list list of text messages
    */
   public SmsMmsMessage(SmsMmsMessage firstMsg, List<SmsMmsMessage> list) {
     this.fromAddress = firstMsg.fromAddress;
     this.timestamp = firstMsg.timestamp;
     this.messageType = firstMsg.messageType;
     this.messageClass = firstMsg.messageClass;
     this.parseAddress = firstMsg.parseAddress;
     this.parseSubject = firstMsg.parseSubject;
     this.sentTime = firstMsg.sentTime;
     this.iccIndex = firstMsg.iccIndex;
 
     boolean addBlank = ManagePreferences.splitBlankIns();
     StringBuffer sb = new StringBuffer();
     for (SmsMmsMessage msg : list) {
       if (msg != null) {
         if (this.messageBody == null) {
           this.messageBody = msg.messageBody;
           sb.append(msg.parseMessageBody);
         } else {
           if (extraMsgBody == null) extraMsgBody = new ArrayList<String>();
           extraMsgBody.add(msg.messageBody);
           if (addBlank) sb.append(' ');
           sb.append(msg.parseMessageBody);
         }
       }
     }
     parseMessageBody = sb.toString().trim();
   }
   
   /**
    * Check if message is a vendor discovery query.  This should be called as 
    * soon as the SmsMmsMessage object is constructed form an SMS or MMS message
    * @param context current context
    * @return true if this was a vendor discovery message that should not
    * be processed further by anyone
    */
   public boolean isDiscoveryQuery(Context context) {
     messageBody = VendorManager.instance().discoverQuery(context, messageBody);
     return (messageBody == null);
   }
 
   /**
    * Read serialized SmsMmsMessage object from Object input stream
    */
   private void readObject(java.io.ObjectInputStream stream)
   throws IOException, ClassNotFoundException {
     stream.defaultReadObject();
     rebuildParseInfo();
   }
   
   /**
    * Rebuild parse information (if necessary) after the insert blanks between
    * spit message option has changed
    * @return true if message parse information may have changed
    */
   public boolean splitDelimChange() {
     
     // If this isn't a split CAD page, then nothing needs to be done
     if (extraMsgBody == null) return false;
     
     // Otherwise rebuild the parse message body
     info = null;
     rebuildParseInfo();
     return true;
   }
 
   private void rebuildParseInfo() {
     getParseInfo();
     if (extraMsgBody != null) {
       String delim = (ManagePreferences.splitBlankIns() ? " " : "");
       for (String msgBody : extraMsgBody) {
         String saveAddress = parseAddress;
         String saveSubject = parseSubject;
         String saveBody = parseMessageBody;
         msgCount = msgIndex = -1;
         getParseInfo(msgBody);
         parseAddress = saveAddress;
         parseSubject = saveSubject;
         parseMessageBody = saveBody + delim + parseMessageBody;
       }
     }
   }
   
   // Patterns used to perform front end descrambling
   private static final Pattern[] MSG_HEADER_PTNS = new Pattern[]{
     Pattern.compile("^(000\\d)/(000\\d)\\b"),
     Pattern.compile("^(\\d)of(\\d):"),
     Pattern.compile("^\\((\\d)/(\\d)\\)"),
     Pattern.compile("^ *(\\d)/(\\d) / "),
     Pattern.compile("^\\( *(\\d) +of +(\\d) *\\)"),
     Pattern.compile("^Subject:(\\d)/(\\d)\\n"),
     Pattern.compile("\\[(\\d) of (\\d)\\]$"),
     Pattern.compile(":(\\d)of(\\d)$")
   };
   private static final Pattern OPT_OUT_PTN = Pattern.compile("TXT STOP.*$");
   private static final Pattern PAGECOPY_PATTERN = Pattern.compile("Pagecopy-Fr:(\\S*)\\s");
   private static final Pattern[] EMAIL_PATTERNS = new Pattern[]{ 
     Pattern.compile("^(?:\\*.*\\*)?([\\w\\.]+@[\\w\\.]+)( +/ +/ +)"),
     Pattern.compile(" - Sender: *([\\w\\.]+@[\\w\\.]+) *\n"),
     Pattern.compile("^(?:[-=.+_a-z0-9]*[0-9a-f]{8,}[-=.+_a-z0-9]*=)?([\\w.!\\-]+@[\\w.]+)[\\s:]")
   };
   private static final Pattern EMAIL_PFX_PATTERN = Pattern.compile("^([\\w\\.]+@[\\w\\.]+)\\n");
   private static final Pattern E_S_M_PATTERN = Pattern.compile("^(?:([^ ,;/]+) +)?S: *(.*?)(?: +M:|\n)");
   
   
   /**
    * Perform any front end unscrambling required to recover the original text
    * message sent by dispatch for parsing purposes.
    */
   private void getParseInfo() {
     getParseInfo(messageBody);
   }
   
   private void getParseInfo(String body) {
     
     // Set message body to empty string if we don't have one
     if (body == null) body = "";
     
     // Start by decoding common HTML sequences
     body = body.replaceAll("&nbsp;",  " ").replaceAll("&amp;",  "&")
                .replaceAll("<br>", "\n").replaceAll("&gt;", ">").replaceAll("&lt;", "<").trim();
     
     // default address and subject to obvious values
     parseAddress = fromAddress;
     parseSubject = (subject == null ? "" : subject);
     
     // Dummy loop we can break out of
     do {
       
       // Check the message header pattern,these contain a msg number and total
       // counts.  If the values don't match, set the flag indicating more data
       // is expected
       Matcher match = null;
       boolean found = false;
       for (Pattern ptn : MSG_HEADER_PTNS) {
         match = ptn.matcher(body);
         found = match.find();
         if (found) break;
       }
       if (found) {
         msgIndex = Integer.parseInt(match.group(1));
         msgCount = Integer.parseInt(match.group(2));
         if (match.start() == 0) body = body.substring(match.end()).trim();
         else body = body.substring(0,match.start()).trim();
       } else {
         if (body.startsWith("/ ")) body = body.substring(2).trim();
       }
       
       // Get rid of leading quoted blanks
       if (body.startsWith("\" \"")) body = body.substring(3).trim();
       
       // And trailing opt out message
       match = OPT_OUT_PTN.matcher(body);
       if (match.find()) body = body.substring(0,match.start()).trim();
       
       /* Decode patterns that look like this.....
       1 of 3
       FRM:CAD@livingstoncounty.livco
       SUBJ:DO NOT REPLY
       MSG:CAD:FYI: ;CITAF;5579 E GRAND RIVER;WILDWOOD DR;Event spawned from CITIZEN ASSIST LAW. [12/10/10
       (Con't) 2 of 3
       20:08:59 SPHILLIPS] CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAINBREAK [12/10/10 20:04:40 HROSSNER] CALLER ADV OF A WATER MAIN
       (Con 3 of 3
       BREAK(End)
       
       Or This
       
       FRM:e@fireblitz.com <Body%3AFRM%3Ae@fireblitz.com>
       MSG:48: TOWNHOUSE FIRE
       E818 BO802
       9903 BREEZY KNOLL CT [DEAD END & GREEN HAVEN RD]
       12/23 23:32
       http://fireblitz.com/18/8.shtm
       */
       int pt1 = -1;
       int pt2 = -1;
       if (body.startsWith("FRM:")) {
         pt1 = 0;
         pt2 = 4;
       } else if (EMAIL_PFX_PATTERN.matcher(body).find()) {
         pt1 = pt2 = 0;
       } else {
         pt1 = body.indexOf("\nFRM:");
         pt2 = pt1 + 5;
       }
       if (pt1 >= 0) {
         int pt3 = body.indexOf('\n', pt2);
         if (pt3 >= 0) {
           parseAddress = body.substring(pt2, pt3).trim();
           pt1 = pt3;
           pt3 = body.indexOf("\nSUBJ:", pt1);
           if (pt3 >= 0) {
             pt1 = pt3;
             pt2 = pt3 + 6;
             pt3 = body.indexOf('\n', pt2);
             if (pt3 >= 0) {
               addSubject(body.substring(pt2, pt3));
               pt1 = pt3;
             }
           }
           pt3 = body.indexOf("\nMSG:", pt1);
           if (pt3 >= 0) {
             pt1 = pt3;
             pt2 = pt1 + 5;
             if (body.length() > pt2 && Character.isWhitespace(body.charAt(pt2))) pt2++;
             StringBuilder sb = new StringBuilder();
             boolean skipBreak = false;
             for (String line : body.substring(pt2).split("\n")) {
               if (line.startsWith("(Con")) {
                 skipBreak = true;
               } else {
                 if (sb.length() > 0) {
                   sb.append(skipBreak ? ' ' : '\n');
                 }
                 sb.append(line);
                 skipBreak = false;
               }
             }
             trimLast(sb, "(End)");
             trimLast(sb, "\nMore?");
             body = sb.toString().trim();
             break;
           }
         }
       }
       
       /* Decode patterns that look like this 
       CommCenter@ccems.com <Body%3ACommCenter@ccems.com> [] TAP OUT (SAL)
        */
       int ipt = body.indexOf(" [] ");
       if (ipt >= 0) {
         parseAddress = body.substring(0, ipt).trim();
         if (parseAddress.contains("@")) {
           body = body.substring(ipt+4).trim();
           break;
         }
       }
       
       /* Decode patterns that look like this
       Subject:HCCAD\nEOC:F03 WIRES >WIRES/POLE SHAWNEE DR&WALTERS MILL RD XS: WALTERS MILL RD FOREST HILL NOT ENTERED Cad: 2010-000019169
       */
       if (body.startsWith("Subject:")) {
         ipt = body.indexOf('\n');
         if (ipt >= 0) {
           addSubject(body.substring(8,ipt).trim());
           body = body.substring(ipt+1).trim();
           break;
         }
       }
       
       /* Decode patterns that look like this
         Dispatch@ci.waynesboro.va.us <Body%3ADispatch@ci.waynesboro.va.us> Msg: Dispatch:2ND CALL 1001 HOPEMAN PKWY, ZAP12 INJURIES FROM PREVIOUS MVA
       */
       ipt = body.indexOf(" Msg:");
       if (ipt >= 0) {
         String addr = body.substring(0,ipt).trim();
         if (addr.contains("@") && ! addr.contains(":")) {
           parseAddress = addr;
           body = body.substring(ipt+5).trim();
           break;
         }
       }
       
       /* Decode patterns that look like this
        * Pagecopy-Fr:CAD@livingstoncounty.livco\nCAD:FYI: ;OVDOSE;4676 KENMORE DR;[Medical Priority Info] RESPONSE: P1 STA 1
        */
       match = PAGECOPY_PATTERN.matcher(body);
       if (match.find()) {
         parseAddress = match.group(1);
         body = body.substring(match.end()).trim();
         break;
       }
       
       /* Decode patterns that look like
        * S:subject M:msg
        */
       match = E_S_M_PATTERN.matcher(body);
       if (match.find()) {
         String from = match.group(1);
         if (from != null) parseAddress = from;
         addSubject(match.group(2));
         body = body.substring(match.end()).trim();
         break;
       }
       
       /* Decode patterns that match EMAIL_PATTERN, which is basically an email address
        * followed by one of a set of known delimiters
        */
       found = false;
       for (Pattern ptn : EMAIL_PATTERNS) {
         match = ptn.matcher(body);
         found = match.find();
         if (found) break;
       }
       if (found) {
         parseAddress = match.group(1);
         body = body.substring(match.end()).trim();
         break;
       }
 
     } while (false);
     
     // Finally, leading values in square or round brackets are turned into
     // message subjects.  There may be more than one of these, in which case
     // only the last is retained
     int pt1 = 0;
     
     // First skip leading dots and spaces
     while (pt1 < body.length() && " .".indexOf(body.charAt(pt1))>=0) pt1++;
     while (pt1 < body.length()) {
       while (pt1 < body.length() && body.charAt(pt1) == ' ') pt1++;
       if (pt1 >= body.length()) break;
       
       char d1 = body.charAt(pt1);
       if (d1 != '(' && d1 != '[') break;
       
       char d2 = (d1 == '(' ? ')' : ']');
       int level = 0;
       int pt2;
       for (pt2 = pt1; pt2 < body.length(); pt2++) {
         char c = body.charAt(pt2);
         if (c == d1) level++;
         if (c == d2) level--;
         if (level == 0) {
           addSubject(body.substring(pt1+1, pt2).trim());
           pt1 = pt2+1;
           break;
         }
       }
       if (pt2 >= body.length()) break;
     }
     
     body = body.substring(pt1);
     if (body.startsWith("MSG:")) body = body.substring(4).trim();
     
     // Last check, if we ended up with no message, use the last subject as the message
     if (body.length() == 0) {
       int pt = parseSubject.lastIndexOf('|');
       if (pt >= 0) {
         body = parseSubject.substring(pt+1);
         parseSubject = parseSubject.substring(0,pt);
       } else {
         body = parseSubject;
         parseSubject = "";
       }
     }
     parseMessageBody = body;
   }
   
   private void trimLast(StringBuilder sb, String endCode) {
     int len = sb.length()-endCode.length();
     if (len < 0) return;
     if (sb.substring(len).equals(endCode)) sb.setLength(len);
   }
   
   public void addSubject(String subject) {
     if (subject.length() == 0) return;
     if (parseSubject.length() == 0) parseSubject = subject;
     else parseSubject = parseSubject + '|' + subject;
   }
   
 
   public void setParserInfo(String location, SmsMsgInfo info) {
     this.location = location;
     this.info = info;
   }
   
   public void setReqLocation(String reqLocation) {
     this.reqLocation = reqLocation;
   }
   
   public void setSponsor(String sponsor) {
     this.sponsor = sponsor;
   }
 
   public long getTimestamp() {
     return timestamp;
   }
   
   public long getSentTime() {
     return sentTime;
   }
   
   public Date getIncidentDate() {
     if (incidentDate == null) incidentDate = calcIncidentDate();
     return incidentDate;
   }
   
   private Date calcIncidentDate() {
     
     // Parse the info object and retrieve the parsed date & time
     // If there is no parsed time just return the received timestamp
     if (getInfo() == null) return new Date(timestamp);
     int[] dateArry = splitDateTime(info.getDate());
     int[] timeArry = splitDateTime(info.getTime());
     if (timeArry == null) return new Date(timestamp);
     
     // Set result fields from parsed time field
     Calendar cal = new GregorianCalendar();
     cal.setTime(new Date(timestamp));
     Calendar origCal = new GregorianCalendar();
     origCal.setTime(cal.getTime());
     cal.set(Calendar.HOUR_OF_DAY, timeArry[0]);
     cal.set(Calendar.MINUTE, timeArry[1]);
     cal.set(Calendar.SECOND, (timeArry[2]>=0 ? timeArry[2] : 0));
     cal.set(Calendar.MILLISECOND, 0);
     
     // If there is no date field, retain the date from the time received
     // unless the parsed time is greater than the received time, in which 
     // case we assume the incident date must have been yesterday.  Except
     // we will allow a 5 minute discrepancy between system clocks
     if (dateArry == null) {
       long delta = origCal.getTimeInMillis() - cal.getTimeInMillis();
       if (delta < -300000L) cal.add(Calendar.DAY_OF_YEAR, -1);
     }
     
     // If there was a date field, use information from it
     else {
       cal.set(Calendar.MONTH, dateArry[0]-1);
       cal.set(Calendar.DAY_OF_MONTH, dateArry[1]);
       int year = dateArry[2];
       
       // If year was specified, use it.  If it was a 2 digit year
       // turn it to a 4 digit year
       if (year > 0) {
         if (year < 100) year += 2000;
         cal.set(Calendar.YEAR, year);
       } 
       
       // If no year was specified, retain received timestamp year
       // Unless this leads to result greater than the received time
       // in which case roll the year back
       else {
         long delta = origCal.getTimeInMillis() - cal.getTimeInMillis();
         if (delta < 0) cal.add(Calendar.YEAR, -1);
       }
     }
     return cal.getTime();
   }
   
   /**
    * Split up an entered date or time field into component integer parts
    * @param field date/time field to be parsed
    * @return 3 element field containing component parts.  First two will have
    * values, last one will be -1 if it was not present.  Returns null if
    * field is not a valid date/time string
    */
   private static final Pattern DATE_TIME_DELIM = Pattern.compile("[/:-]");
   private static int[] splitDateTime(String field) {
     if (field.length() == 0) return null;
     String[] parts = DATE_TIME_DELIM.split(field);
     if (parts.length != 2 && parts.length != 3) return null;
     int[] result = new int[3];
     result[2] = -1;
     for (int ndx = 0; ndx < parts.length; ndx++) {
       try {
         result[ndx] = Integer.parseInt(parts[ndx]);
       } catch (NumberFormatException ex) {
         return null;
       }
     }
     return result;
   }
 
   public MessageClass getMessageClass() {
     return messageClass;
   }
 
   public CharSequence getFormattedTimestamp(Context context) {
     long time = getIncidentDate().getTime();
     return DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
   }
   
   public String getSubject() {
     return parseSubject;
   }
 
   public String getMessageBody() {
     return (parseMessageBody == null ? null : parseMessageBody.trim());
   }
   
   public int getMessageType() {
     return messageType;
   }
   
   public String getFromAddress() {
     return fromAddress;
   }
 
   public String getAddress() {
     return parseAddress;
   }
   
   public String getLocation() {
     return location;
   }
   
   public String getContentLoc() {
     return contentLoc;
   }
   
   public String getMmsMsgId() {
     return mmsMsgId;
   }
   
   public int getMsgIndex() {
     return msgIndex;
   }
   
   public int getMsgCount() {
     return msgCount;
   }
   
   public String getReqLocation() {
     return reqLocation;
   }
   
   public String getSponsor() {
     return sponsor;
   }
   
   public SmsMsgInfo getInfo() {
     
     // Some special logic if the previous location was General
     // And the current location code preference is not general
     // And this message text is a valid message for this location code
     // then use the results of the new location code.
     // NOTE: Our location and info members will be set as a side effect of
     // a successful isPageMsg call
     if (location != null && location.startsWith("General")) {
       String curLocCode = ManagePreferences.location();
       if (! curLocCode.startsWith("General")) {
         if (ManageParsers.getInstance().getParser(curLocCode).isPageMsg(this)) {
           return info;
         }
       }
     }
     
     // If we don't have an info object, get a new one
     if (info == null) {
       if (! isPageMsg()) {
         
         // It is almost impossible for the the parser call to fail for a
         // message where it previously succeeded.  But if it happens
         // try again with the general alert parser, which will never fail
         SmsMsgParser parser = ManageParsers.getInstance().getAlertParser();
         parser.isPageMsg(this);
       }
     }
     return info;
   }
   
   /**
    * Determine if this message is considered to be a CAD page message
    * as determined by the parser associated with the message.  Has the
    * side effect of setting up the parser message information object and
    * setting the message location parser if it is not already set
    * @return true if this is a valid CAD page
    */
   public boolean isPageMsg() {
     return getParser().isPageMsg(this);
   }
   
   /**
    * Determine if message is a valid CAD message for this parser, and parse
    * all information from the message if it is
    * @overrideFilter true if parser filters should be overridden
    * @genAlert true if general alerts should be accepted
    * @return true if this message is a valid CAD page
    */
   public boolean isPageMsg(boolean overrideFilter, boolean genAlert) {
     return getParser().isPageMsg(this, overrideFilter, genAlert);
   }
   
   /**
    * @return parser associated with this message
    */
   public SmsMsgParser getParser() {
     
     // If we don't have a historical location code, but a location code was
     // included in the message, use it to get a parser.  If the code happens
     // to be invalid, just catch the exception and continue
     if (location == null && reqLocation != null) {
       try {
         return ManageParsers.getInstance().getParser(reqLocation);
       } catch (RuntimeException ex) {}
     }
     
     // If we have a historical location code, use it
     // Otherwise use the current configured location code.
     // Again, location and info members are set as a side effect of a successful
     // isPageMsg method call
     return ManageParsers.getInstance().getParser(location);
     
   }
   
   /**
    * Create option or context menu for message
    * @param context current context
    * @param menu menu to be constructed
    * @param display true if called from popup menu display
    */
   public static void createMenu(Activity context, Menu menu, boolean display) {
     MenuInflater inflater = context.getMenuInflater();
     inflater.inflate(R.menu.message_menu, menu);
     if (display) {
       menu.removeItem(R.id.open_item);
     } else {
       menu.removeItem(R.id.close_item);
     }
   }
   
   public void prepareMenu(Activity context, Menu menu) {
     for (int ndx = 0; ndx < menu.size(); ndx++) {
       final MenuItem item = menu.getItem(ndx);
       prepareItem(new ItemObject() {
         
         @Override
         public int getId() {
           return item.getItemId();
         }
 
         @Override
         public void setEnabled(boolean enabled) {
           item.setEnabled(enabled);
         }
 
         @Override
         public void setTitle(int resId) {
           item.setTitle(resId);
         }
 
         @Override
         public void setVisible(boolean visible) {
           item.setVisible(visible);
         }
         
       });
     }
   }
   
   public void prepareButton(int itemId, Button button) {
     final int fItemId = itemId;
     final Button fBtn = button;
     prepareItem(new ItemObject(){
       
       @Override
       public int getId() {
         return fItemId;
       }
 
       @Override
       public void setEnabled(boolean enabled) {
         fBtn.setEnabled(enabled);
       }
 
       @Override
       public void setTitle(int resId) {
         fBtn.setText(resId);
       }
 
       @Override
       public void setVisible(boolean visible) {
         fBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
       }
     });
   }
   
   private interface ItemObject {
     public int getId();
     public void setTitle(int resId);
     public void setEnabled(boolean enabled);
     public void setVisible(boolean visible);
   }
   
   private void prepareItem(ItemObject item) {
     
     switch (item.getId()) {
     
     // Disabled button is never visible
     case 0:
       item.setVisible(false);
       break;
     
     // Disable map item if we have no address
     case R.id.map_item:
       item.setEnabled(getInfo().getAddress().length() > 0);
       break;
     
     // Change label on toggle lock item depending on current lock state
     case  R.id.toggle_lock_item:
       item.setTitle(isLocked() ? R.string.unlock_item_text : R.string.lock_item_text);
       break;
       
     // Delete is only enabled if message has been read and is not locked
     case R.id.delete_item:
       item.setEnabled(canDelete());
       break;
     
     // Email is disabled for the free version
     case R.id.email_item:
       item.setEnabled(! DonationManager.instance().isFreeVersion());
       break;
     }
    
   }
 
   /**
    * Handle a menu selection concerning this message
    * @param context current context
    * @param itemId Selected Menu ID
    * @param display true if called from message display dialog
    * @return true if menu item processed, false otherwise
    */
   public boolean menuItemSelected(Activity context, int itemId, boolean display) {
     
     // Any button clears the notice
     ManageNotification.clear(context);
     if (ackNeeded) {
       C2DMReceiver.sendResponseMsg(context, ackURL, "ACK");
       ackNeeded = false;
       reportDataChange();
     }
 
     switch (itemId) {
     case R.id.open_item:
       SmsPopupActivity.launchActivity(context, this);
       return true;
       
     case R.id.ack_item:
       // Doesn't do anything yet (except clear notification)
       return true;
       
     case R.id.map_item:
       SmsPopupUtils.mapMessage(context, getInfo(), false);
       return true;
       
     case R.id.toggle_lock_item:
       toggleLocked();
       return true;
       
     case R.id.delete_item:
       SmsMessageQueue.getInstance().deleteMessage(this);
       if (display) context.finish();
       return true;
       
     case R.id.close_item:
       context.finish();
       return true;
       
     case R.id.email_item:
       EmailDeveloperActivity.sendMessageEmail(context,  msgId);
       return true;
       
     case R.id.publish_item:
       broadcastIntent(context, true);
     
     default:
       return false;
     }
   }
   
   /**
    * Broadcast message information for 3rd party apps
    * @param context broadcast context
    * @param republish true if this is a republish event
    */
   public void broadcastIntent(Context context, boolean republish) {
     Intent intent = new Intent(ACTION);
     intent.putExtra(EXTRA_MSG_ID, msgId);
     intent.putExtra(EXTRA_REPUBLISH, republish);
     putExtraString(intent, EXTRA_FROM, parseAddress);
     putExtraString(intent, EXTRA_SUBJECT, parseSubject);
     putExtraString(intent, EXTRA_MESSAGE, parseMessageBody.trim());
     intent.putExtra(EXTRA_TIME, timestamp);
     putExtraString(intent, EXTRA_LOC_CODE, location);
     
     intent.putExtra(EXTRA_QUIET_MODE, 
          !(ManagePreferences.notifyEnabled() || ManagePreferences.popupEnabled()));
     
     if (info != null) {
       putExtraString(intent, EXTRA_LOC_CITY, info.getDefCity());
       putExtraString(intent, EXTRA_LOC_STATE, info.getDefState());
       putExtraString(intent, EXTRA_PARSE_CALL_CODE, info.getCode());
       putExtraString(intent, EXTRA_PARSE_CALL, info.getCall());
       putExtraString(intent, EXTRA_PARSE_INFO, info.getSupp());
       putExtraString(intent, EXTRA_PARSE_PLACE, info.getPlace());
       putExtraString(intent, EXTRA_PARSE_ADDRESS, info.getAddress());
       putExtraString(intent, EXTRA_PARSE_CITY, info.getCity());
       putExtraString(intent, EXTRA_PARSE_STATE, info.getState());
       putExtraString(intent, EXTRA_PARSE_MAP_ADDRESS, info.getMapAddress());
       putExtraString(intent, EXTRA_PARSE_APT, info.getApt());
       putExtraString(intent, EXTRA_PARSE_MAP, info.getMap());
       putExtraString(intent, EXTRA_PARSE_BOX, info.getBox());
       putExtraString(intent, EXTRA_PARSE_AGENCY, info.getSource());
       putExtraString(intent, EXTRA_PARSE_UNIT, info.getUnit());
       putExtraString(intent, EXTRA_PARSE_CALL_ID, info.getCallId());
       putExtraString(intent, EXTRA_PARSE_NAME, info.getName());
       putExtraString(intent, EXTRA_PARSE_PHONE, info.getPhone());
       putExtraString(intent, EXTRA_PARSE_PRIORITY, info.getPriority());
       putExtraString(intent, EXTRA_PARSE_CHANNEL, info.getChannel());
     }
     
     
     context.sendBroadcast(intent, PERMISSION);
   }
   
   private static void putExtraString(Intent intent, String key, String value) {
     if (value.length() > 0) intent.putExtra(key, value);
   }
 
 
   /**
    * Append message information to support message under construction
    * @param sb String builder holding message being constructed
    */
   public void addMessageInfo(Context context, StringBuilder sb) {
     sb.append("\n\nMessage Contents\n");
     
     sb.append("Time:");
     sb.append(DateFormat.getLongDateFormat(context).format(timestamp));
     sb.append(" ");
     sb.append(DateFormat.getTimeFormat(context).format(timestamp));
 
     sb.append("\nFrom:");
     sb.append(fromAddress);
 
     sb.append("\nEff From:");
     sb.append(parseAddress);
     
     sb.append("\nType:");
     sb.append(messageType);
     
     sb.append("\nClass:");
     sb.append(messageClass);
     
     sb.append("\nSubject:");
     sb.append(subject);
     sb.append("\nEff Subject:");
     sb.append(parseSubject);
     
     sb.append("\nBody:");
     sb.append(escape(messageBody));
     if (extraMsgBody != null) {
       for (String body : extraMsgBody) {
         sb.append("\nBody:");
         sb.append(escape(body));
       }
     }
     
     sb.append("\nEff Body:");
     sb.append(parseMessageBody);
     
     sb.append("\nLocation:");
     sb.append(location);
     
     sb.append("\nContentLoc:");
     sb.append(contentLoc);
     sb.append("\nmmsMsgId:");
     sb.append(mmsMsgId);
     
     sb.append("\nC2DM Loc:");
     sb.append(reqLocation);
     
     sb.append("\nSponsor:");
     sb.append(sponsor);
     
     sb.append("ackReq:");
     sb.append(ackReq);
     sb.append("ackURL");
     sb.append(ackURL);
     
     if (msgCount >= 0) {
       sb.append("\nMsgIndex:");
       sb.append(msgIndex);
       sb.append(" of ");
       sb.append(msgCount);
     }
     
     sb.append("\nSend time:");
     sb.append(sentTime);
     sb.append("\nRec time: ");
     sb.append(timestamp);
     sb.append("\nICC Index:");
     sb.append(iccIndex);
     
     
     sb.append('\n');
   }
 
   /**
    * Escape a string containing tabs, newlines, or multiple spaces so it
    * will better survive transmission through an email message
    * @param message message to be escaped
    * @return escaped message
    */
   private static final Pattern MULT_SPACES = Pattern.compile(" {2,}");
   static String escape(String message) {
     if (message == null) return message;
     message = message.replace("\t", "\\t");
     message = message.replace("\n", "\\n\n");
     Matcher match = MULT_SPACES.matcher(message);
     if (match.find()) {
       StringBuffer sb = new StringBuffer();
       do {
         match.appendReplacement(sb, "\\\\" + (match.end()-match.start()) + "s");
       } while (match.find());
       match.appendTail(sb);
       message = sb.toString();
     }
     return message;
   }
 
   /**
    * Fix any obsolete location codes
    * @param context current context
    */
   public void fixOldLocation(Context context) {
     if (location != null) location = ManagePreferences.convertOldLocationCode(context, location);
     
     // That *SHOULD* fix things, but if we still can't retrieve a parser with this
     // location, force it to "General".
     try {
       ManageParsers.getInstance().getParser(location);
     } catch (RuntimeException ex) {
       location = "General";
     }
   }
 
   
   @Override
   public boolean equals(Object obj) {
     if (!(obj instanceof SmsMmsMessage)) return false;
     SmsMmsMessage msg = (SmsMmsMessage)obj;
     if (messageType != msg.messageType) return false;
     if (!match(fromAddress, msg.fromAddress)) return false;
     if (messageType == MESSAGE_TYPE_SMS) {
       return match(messageBody, msg.messageBody); 
     } else {
       return match(contentLoc, msg.contentLoc) &&
               match(mmsMsgId, msg.mmsMsgId);
     }
   }
   
   private boolean match(String s1, String s2) {
     if (s1 == null && s2 == null) return true;
     if (s1 == null || s2 == null) return false;
     return s1.equals(s2);
   }
 
   public void deleteFromContent(Context context) {
     // TODO Auto-generated method stub
     
   }
 }
