 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.mail.imap;
 
 import net.rim.device.api.i18n.MessageFormat;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.DataBuffer;
 import net.rim.device.api.util.IntIntHashtable;
 import net.rim.device.api.util.MathUtilities;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.MailProgressHandler;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.ConnectionResponseTester;
 import org.logicprobe.LogicMail.util.StringArrays;
 import org.logicprobe.LogicMail.util.StringParser;
 import org.logicprobe.LogicMail.util.Watchdog;
 
 import java.io.IOException;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * This class implements the commands for the IMAP protocol
  */
 public class ImapProtocol {
     private static final ConnectionResponseTester executeResponseTester = new ImapResponseLineTester();    
     private Connection connection;
     private Watchdog watchdog;
     private IdleThread idleThread;
     private UntaggedResponseListener untaggedResponseListener;
     private String selectedMailbox;
 
     // Number of octets to fetch at a time, when fetching body content.
     private static final int FETCH_INCREMENT_INITIAL_WIFI = 8192;
     private static final int FETCH_INCREMENT_INITIAL_MOBILE = 4096;
     private static final int FETCH_INCREMENT_MIN = 1024;
     private static final int FETCH_INCREMENT_MAX = 32768;
     
     /**
      * Counts the commands executed so far in this session. Every command of an
      * IMAP session needs a unique ID that is prepended to the command line.
      */
     private int commandCount = 0;
 
     /** Creates a new instance of ImapProtocol */
     public ImapProtocol() {
     }
 
     /**
      * Sets the connection instance used by this class.
      * This must be set after opening the connection, and prior to calling any
      * command methods.
      *
      * @param connection the new connection instance
      */
     public void setConnection(Connection connection) {
         this.connection = connection;
     }
 
     /**
      * Sets the watchdog instance used by this class to detect stalled
      * connections. This should be set after opening the connection, and
      * prior to calling any command methods, to enable watchdog functionality.
      *
      * @param watchdog the new watchdog instance
      */
     public void setWatchdog(Watchdog watchdog) {
         this.watchdog = watchdog;
     }
     
     /**
      * Sets the listener that receives untagged response notifications.
      *
      * @param untaggedResponseListener the new untagged response listener
      */
     public void setUntaggedResponseListener(UntaggedResponseListener untaggedResponseListener) {
         this.untaggedResponseListener = untaggedResponseListener;
     }
     
     /**
      * Execute the "STARTTLS" command.
      * The underlying connection mode must be switched after the
      * successful execution of this command.
      */
     public void executeStartTLS() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeStartTLS()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         execute(STARTTLS, null, null);
     }
 
     /**
      * Execute the "LOGIN" command
      * @param username The username to login with
      * @param password The password to login with
      * @return True on success, false on authentication failures
      */
     public boolean executeLogin(String username, String password)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeLogin(\"" + username + "\", \"" +
                 password + "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         this.selectedMailbox = "";
         
         // Authenticate with the server
         try {
             execute(LOGIN,
                 CHAR_QUOTE + StringParser.addEscapedChars(username) + "\" \"" +
                 StringParser.addEscapedChars(password) + CHAR_QUOTE, null);
         } catch (MailException exp) {
             // Invalid users are caught by execute()
             // and a MailException is thrown
             return false;
         }
 
         return true;
     }
 
     /**
      * Execute the "LOGOUT" command
      */
     public void executeLogout() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeLogout()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         this.selectedMailbox = "";
         
         execute(LOGOUT, null, null);
     }
 
     /**
      * Execute the "CLOSE" command
      */
     public void executeClose() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeClose()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         execute(CLOSE, null, null);
     }
 
     /**
      * Execute the "CAPABILITY" command
      * @return Hashtable containing a mapping from String to the boolean
      * value "true" for every capability that exists in the reply.
      */
     public Hashtable executeCapability() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeCapability()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute(CAPABILITY, null, null);
 
         if ((replyText == null) || (replyText.length < 1)) {
             throw new MailException("Unable to query server capabilities");
         }
 
         Hashtable table = new Hashtable();
 
         String[] tokens = StringParser.parseTokenString(replyText[0], CHAR_SP);
 
         if ((tokens.length > 2) && tokens[1].equals(CAPABILITY)) {
             for (int i = 2; i < tokens.length; i++)
                 table.put(tokens[i], Boolean.TRUE);
         }
 
         return table;
     }
 
     /**
      * Execute the "COMPRESS DEFLATE" command
      * @return true, if compression was successfully enabled
      */
     public boolean executeCompressDeflate() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("ImapProtocol.executeCompress()").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         try {
             execute(COMPRESS, DEFLATE, null);
         } catch (MailException e) {
             return false;
         }
         return true;
     }
     
     /**
      * Execute the "NAMESPACE" command
      * @return A fully populated Namespace object
      */
     public NamespaceResponse executeNamespace()
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeNamespace()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         byte[][] replyText = executeResponse(NAMESPACE, null, null);
 
         if ((replyText == null) || (replyText.length < 1)) {
             throw new MailException("Unable to query server namespaces");
         }
 
         // Assume a single-line reply, and modify that reply so its enclosed
         // within a set of outer parenthesis so that the parser is happy.
         int offset = Arrays.getIndex(replyText[0], (byte)'(');
         byte[] data = new byte[(replyText[0].length - offset) + 2];
         data[0] = (byte)'(';
         int j = 1;
         for(int i=offset; i<replyText[0].length; i++) {
             data[j++] = replyText[0][i];
         }
         data[j] = (byte)')';
         
         Vector tokens = ImapParser.parenListParser(data);
 
         // Sanity check on results
         if ((tokens == null) || (tokens.size() < 3)) {
             return new NamespaceResponse();
         }
 
         NamespaceResponse response = new NamespaceResponse();
         Vector nsTokens;
         Vector temp;
         int size;
         int i;
 
         // Parse personal namespace(s)
         if (tokens.elementAt(0) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(0);
             size = nsTokens.size();
             response.personal = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.personal[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.personal[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.personal[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         // Parse other users' namespace(s)
         if (tokens.elementAt(1) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(1);
             size = nsTokens.size();
             response.other = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.other[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.other[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.other[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         // Parse shared namespace(s)
         if (tokens.elementAt(2) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(2);
             size = nsTokens.size();
             response.shared = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.shared[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.shared[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.shared[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         return response;
     }
 
     /**
      * Execute the "SELECT" command
      * @param mboxpath The mailbox path to select
      * @return Parsed response object
      */
     public SelectResponse executeSelect(String mboxpath)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeSelect(\"" + mboxpath + "\")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute(SELECT,
                 CHAR_QUOTE + StringParser.addEscapedChars(mboxpath) +
                 CHAR_QUOTE, null);
         SelectResponse response = new SelectResponse();
 
         int p;
         int q;
 
         for (int i = 0; i < replyText.length; i++) {
             String rowText = replyText[i];
 
             if ((p = rowText.indexOf(_EXISTS)) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.exists = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.exists = 0;
                     }
                 }
             } else if ((p = rowText.indexOf(_RECENT)) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.recent = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.recent = 0;
                     }
                 }
             } else if ((p = rowText.indexOf(UNSEEN_)) != -1) {
                 p += 6;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.unseen = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.unseen = 0;
                     }
                 }
             } else if ((p = rowText.indexOf(UIDVALIDITY_)) != -1) {
                 p += 11;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.uidValidity = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.uidValidity = 0;
                     }
                 }
             } else if ((p = rowText.indexOf(UIDNEXT_)) != -1) {
                 p += 7;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.uidNext = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.uidNext = -1;
                     }
                 }
             }
         }
 
         // Keep track of the selected mailbox for the few commands that
         // can operate on any mailbox.
         this.selectedMailbox = mboxpath;
         
         return response;
     }
 
     /**
      * Execute the "EXPUNGE" command.
      */
     public void executeExpunge() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeExpunge()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
         
         byte[][] responses = executeResponse(EXPUNGE, null, null);
 
         for (int i = 0; i < responses.length; i++) {
             checkForUntaggedValue(responses[i]);
         }
     }
 
     public StatusResponse[] executeStatus(String[] mboxpaths,
         MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
             buf.append("ImapProtocol.executeStatus({\r\n");
 
             if (mboxpaths == null) {
                 buf.append("  null\r\n");
             } else {
                 for (int i = 0; i < mboxpaths.length; i++) {
                     buf.append("  \"" + mboxpaths[i] + "\"\r\n");
                 }
             }
 
             buf.append("})");
             EventLogger.logEvent(AppInfo.GUID, buf.toString().getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         StatusResponse[] response = new StatusResponse[mboxpaths.length];
        String[] result = new String[mboxpaths.length];
        
         int i;
        for (i = 0; i < mboxpaths.length; i+=10) {
            int increment = Math.min(10, mboxpaths.length - i);
            String[] arguments = new String[increment];
            for(int j=0; j<increment; j++) {
                arguments[j] = CHAR_QUOTE
                    + StringParser.addEscapedChars(mboxpaths[i + j])
                    + "\" (MESSAGES RECENT UNSEEN)";
            }
            
            String[] tempResult = executeBatch(STATUS, arguments, progressHandler);
            if ((tempResult == null) || (tempResult.length != arguments.length)) {
                throw new MailException("Unable to query folder status");
            }
            for(int j=0; j<increment; j++) {
                result[i + j] = tempResult[j];
            }
         }
 
        for (i = 0; i < result.length; i++) {
             response[i] = new StatusResponse();
 
             if (result[i] == null) {
                 continue;
             }
 
             int p = result[i].indexOf('(');
             int q = result[i].indexOf(')');
 
             if ((p == -1) || (q == -1) || (p >= q)) {
                 continue;
             }
 
             String[] fields = StringParser.parseTokenString(result[i].substring(p +
                         1, q), CHAR_SP);
 
             if (fields.length != 6) {
                 continue;
             }
 
             for (int j = 0; j < fields.length; j += 2) {
                 if (fields[j].equalsIgnoreCase(MESSAGES)) {
                     try {
                         response[i].exists = Integer.parseInt(fields[j + 1]);
                     } catch (NumberFormatException e) {
                     }
                 } else if (fields[j].equalsIgnoreCase(RECENT)) {
                     try {
                         response[i].recent = Integer.parseInt(fields[j + 1]);
                     } catch (NumberFormatException e) {
                     }
                 } else if (fields[j].equalsIgnoreCase(UNSEEN)) {
                     try {
                         response[i].unseen = Integer.parseInt(fields[j + 1]);
                     } catch (NumberFormatException e) {
                     }
                 }
             }
         }
 
         return response;
     }
 
     /**
      * Execute the "FETCH (FLAGS UID)" command
      * @param firstIndex Index of the first message
      * @param lastIndex Index of the last message
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlags(int firstIndex,
         int lastIndex, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlags(" + firstIndex + ", " +
                 lastIndex + ")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector(lastIndex - firstIndex);
         
         executeResponse(FETCH,
                 Integer.toString(firstIndex) + CHAR_COLON +
                 Integer.toString(lastIndex) + " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
                 else {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID)" command
      * @param uidNext Unique ID of the next message
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlagsUid(int uidNext,
         MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlagsUid(" + uidNext + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector();
         executeResponse(UID_FETCH,
                 Integer.toString(uidNext) + CHAR_COLON_ASTERISK +
                 " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
                 else {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID)" command
      * @param uids the set of UIDs to fetch flags for
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlagsUid(int[] uids,
         MailProgressHandler progressHandler) throws IOException, MailException {
         
         if (uids.length == 0) {
             return new FetchFlagsResponse[0];
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (uids.length - 1); i++) {
             buf.append(uids[i]);
             buf.append(',');
         }
 
         buf.append(uids[uids.length - 1]);
 
         String uidList = buf.toString();
         
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlagsUid(" + uidList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector();
         executeResponse(UID_FETCH,
                 uidList + " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
                 else {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     private FetchFlagsResponse prepareFetchFlagsResponse(byte[] rawText) {
         if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
             return null;
         }
 
         FetchFlagsResponse flagRespItem = new FetchFlagsResponse();
         
         try {
             Vector parsedText = null;
 
             try {
                 int offset = Arrays.getIndex(rawText, (byte)'(');
                 parsedText = ImapParser.parenListParser(rawText, offset, rawText.length - offset);
             } catch (Exception exp) {
                 parsedText = null;
                 return null;
             }
 
             
             flagRespItem.flags = null;
 
             // Iterate through results, locating and parsing the
             // FLAGS and ENVELOPE sections in an order-independent way.
             int parsedSize = parsedText.size();
 
             for (int j = 0; j < parsedSize; j++) {
                 if (parsedText.elementAt(j) instanceof String) {
                     if (((String) parsedText.elementAt(j)).equals(FLAGS) &&
                             (parsedSize > (j + 1)) &&
                             parsedText.elementAt(j + 1) instanceof Vector) {
                         flagRespItem.flags = ImapParser.parseMessageFlags((Vector) parsedText.elementAt(j +
                                     1));
                     } else if (((String) parsedText.elementAt(j)).equals(
                                 UID) && (parsedSize > (j + 1)) &&
                             parsedText.elementAt(j + 1) instanceof String) {
                         try {
                             flagRespItem.uid = Integer.parseInt((String) parsedText.elementAt(j +
                                         1));
                         } catch (NumberFormatException e) {
                             flagRespItem.uid = -1;
                         }
                     }
                 }
             }
 
             if (flagRespItem.flags == null) {
                 flagRespItem.flags = new MessageFlags();
             }
 
             // Find the message index in the reply
             int spaceIndex = Arrays.getIndex(rawText, (byte)' ');
             int fetchIndex = StringArrays.indexOf(rawText, FETCH.getBytes(), spaceIndex);
             int midx = StringArrays.parseInt(rawText, spaceIndex + 1, fetchIndex - spaceIndex - 2);
 
             flagRespItem.index = midx;
         } catch (Throwable exp) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             flagRespItem = null;
         }
 
         return flagRespItem;
     }
 
     /**
      * Execute the "FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param firstIndex Index of the first message
      * @param lastIndex Index of the last message
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelope(int firstIndex, int lastIndex,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelope(" + firstIndex + ", " +
                 lastIndex + ")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         executeResponse(FETCH,
                 Integer.toString(firstIndex) + CHAR_COLON +
                 Integer.toString(lastIndex) +
                 " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 if(!prepareFetchEnvelopeResponse(rawLine, callback)) {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param uidNext Unique ID of the next message
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelopeUid(int uidNext,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + uidNext + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(UID_FETCH,
                 Integer.toString(uidNext) + CHAR_COLON_ASTERISK +
                 " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 if(!prepareFetchEnvelopeResponse(rawLine, callback)) {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
     
     /**
      * Execute the "FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param indices Set of index values for the messages to fetch
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelope(int[] indices,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (indices.length == 0) {
             callback.responseAvailable(null);
 
             return;
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (indices.length - 1); i++) {
             buf.append(indices[i]);
             buf.append(',');
         }
 
         buf.append(indices[indices.length - 1]);
 
         String indexList = buf.toString();
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + indexList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(FETCH,
                 indexList + " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 if(!prepareFetchEnvelopeResponse(rawLine, callback)) {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param uids Set of unique IDs for the messages to fetch
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelopeUid(int[] uids,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (uids.length == 0) {
             callback.responseAvailable(null);
 
             return;
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (uids.length - 1); i++) {
             buf.append(uids[i]);
             buf.append(',');
         }
 
         buf.append(uids[uids.length - 1]);
 
         String uidList = buf.toString();
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + uidList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(UID_FETCH,
                 uidList + " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 if(!prepareFetchEnvelopeResponse(rawLine, callback)) {
                     checkForUntaggedValue(rawLine);
                 }
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     private boolean prepareFetchEnvelopeResponse(byte[] rawText, FetchEnvelopeCallback callback) {
         if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
             return false;
         }
         
         boolean hasEnvelopeOrStructure = false;
         
         FetchEnvelopeResponse envRespItem = null;
         try {
             MessageEnvelope env = null;
             ImapParser.MessageSection structure = null;
             Vector parsedText = null;
 
             try {
                 int offset = Arrays.getIndex(rawText, (byte)'(');
                 parsedText = ImapParser.parenListParser(rawText, offset, rawText.length - offset);
             } catch (Exception exp) {
                 return false;
             }
 
             envRespItem = new FetchEnvelopeResponse();
             envRespItem.flags = null;
 
             // Iterate through results, locating and parsing the
             // FLAGS and ENVELOPE sections in an order-independent way.
             int parsedSize = parsedText.size();
 
             for (int j = 0; j < parsedSize; j++) {
                 if (FLAGS.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     envRespItem.flags = ImapParser.parseMessageFlags((Vector) parsedText.elementAt(j + 1));
                 }
                 else if (UID.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof String) {
                     try {
                         envRespItem.uid = Integer.parseInt((String) parsedText.elementAt(j + 1));
                     } catch (NumberFormatException e) {
                         envRespItem.uid = -1;
                     }
                 }
                 else if (ENVELOPE.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     env = ImapParser.parseMessageEnvelope((Vector) parsedText.elementAt(j + 1));
                     hasEnvelopeOrStructure = true;
                 }
                 else if (BODYSTRUCTURE.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     structure = ImapParser.parseMessageStructureParameter((Vector) parsedText.elementAt(j + 1));
                     hasEnvelopeOrStructure = true;
                 }
             }
 
             if(!hasEnvelopeOrStructure) { return false; }
             
             // If either of the above sections were not found, then populate
             // the reply with the relevant dummy data.
             if (env == null) {
                 env = ImapParser.generateDummyEnvelope();
             }
 
             if (envRespItem.flags == null) {
                 envRespItem.flags = new MessageFlags();
             }
 
             // Find the message index in the reply
             int spaceIndex = Arrays.getIndex(rawText, (byte)' ');
             int fetchIndex = StringArrays.indexOf(rawText, FETCH.getBytes(), spaceIndex);
             int midx = StringArrays.parseInt(rawText, spaceIndex + 1, fetchIndex - spaceIndex - 2);
 
             envRespItem.index = midx;
             envRespItem.envelope = env;
             envRespItem.structure = structure;
         } catch (Exception exp) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             envRespItem = null;
         }
 
         if (envRespItem != null) {
             callback.responseAvailable(envRespItem);
         }
         
         return true;
     }
 
     /**
      * Execute the "FETCH (BODYSTRUCTURE)" command
      * @param uid Unique ID of the message
      * @return Body structure tree
      */
     public ImapParser.MessageSection executeFetchBodystructure(int uid)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchBodyStructure(" + uid + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         byte[][] rawList = executeResponse(UID_FETCH, uid + " (BODYSTRUCTURE)", null);
 
         ImapParser.MessageSection msgStructure = null;
 
         for (int i = 0; i < rawList.length; i++) {
             byte[] rawText = rawList[i];
             if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
                 continue;
             }
             
             try {
                 msgStructure = ImapParser.parseMessageStructure(rawText);
             } catch (Exception exp) {
                 EventLogger.logEvent(AppInfo.GUID,
                     ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             }
             if(msgStructure == null) {
                 checkForUntaggedValue(rawText);
             }
         }
 
         return msgStructure;
     }
     
     /**
      * Execute the "FETCH (BODY)" command
      * @param uid Unique ID of the message
      * @param address Address of the body section (i.e. "1", "1.2")
      * @param progressHandler the progress handler
      * @return Body text as a string
      */
     public byte[] executeFetchBody(int uid, String address, MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchBody(" + uid + ", \"" + address +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         DataBuffer buf = new DataBuffer();
         int fetchOffset = 0;
         int previousIncrement = -1;
         long previousTime = -1L;
         while(true) {
             int fetchIncrement = getFetchIncrement(previousIncrement, previousTime);
             long time1 = System.currentTimeMillis();
             int fetched = fetchBodyIncrement(buf, uid, address, fetchOffset, fetchIncrement, progressHandler);
             long time2 = System.currentTimeMillis();
             previousTime = Math.abs(time2 - time1);
             previousIncrement = fetchIncrement;
             
             if(fetched < fetchIncrement) {
                 break;
             }
             else {
                 fetchOffset += fetched;
             }
         }
         
         return buf.toArray();
     }
 
     /**
      * Gets the fetch increment for the next message body fetch operation.
      * The initial value is based on the network transport type.  With each
      * successful fetch, the increment is updated using a form of additive
      * increase / multiplicative decrease algorithm.
      *
      * @param previousIncrement the previous increment
      * @param previousTime the previous increment's receive time
      * @return the next fetch increment
      */
     protected int getFetchIncrement(int previousIncrement, long previousTime) {
         int fetchIncrement;
         if(previousIncrement < 0 || previousTime < 0) {
             if(connection.getConnectionType() == ConnectionConfig.TRANSPORT_WIFI_ONLY) {
                 fetchIncrement = FETCH_INCREMENT_INITIAL_WIFI;
             }
             else {
                 fetchIncrement = FETCH_INCREMENT_INITIAL_MOBILE;
             }
         }
         else {
             if(previousTime < 900) {
                 // If we fetched the previous increment too quickly, then
                 // increase the increment size by 1024 bytes.
                 fetchIncrement = previousIncrement + 1024;
             }
             else if(previousTime > 1100) {
                 // If we fetched the previous increment too slowly, then
                 // halve the increment size.
                 fetchIncrement = previousIncrement >>> 1;
             }
             else {
                 // If the previous increment was fetched in 1000 +/- 100 sec,
                 // consider ourselves stable and change nothing.
                 fetchIncrement = previousIncrement;
             }
             
             // Make sure our chosen increment is within the allowable range.
             fetchIncrement = MathUtilities.clamp(FETCH_INCREMENT_MIN, fetchIncrement, FETCH_INCREMENT_MAX);
         }
         return fetchIncrement;
     }
 
     private int fetchBodyIncrement(DataBuffer buf, int uid, String address, int fetchOffset, int fetchIncrement, MailProgressHandler progressHandler) throws IOException, MailException {
         String args = MessageFormat.format(
                 "{0} (BODY[{1}]<{2}.{3}>)",
                 new Object[] {
                         Integer.toString(uid),
                         address,
                         Integer.toString(fetchOffset),
                         Integer.toString(fetchIncrement)
                 });
         
         byte[][] rawList = executeResponse(UID_FETCH, args, progressHandler);
 
         if (rawList.length < 1) {
             return -1;
         }
 
         byte[] rawMessage = null;
         
         for(int i=0; i<rawList.length; i++) {
             if(rawMessage == null) {
                 // If we don't yet have the raw message body, try to find it in
                 // the current response line
                 int offset = Arrays.getIndex(rawList[i], (byte)'(');
                 Vector parsedList = ImapParser.parenListParser(rawList[i], offset, rawList[i].length - offset);
                 int size = parsedList.size();
 
                 for(int j=0; j<(size - 1); j++) {
                     Object element = parsedList.elementAt(j);
                     if(element instanceof String
                             && ((String)element).startsWith(BODY)
                             && parsedList.elementAt(j + 1) instanceof byte[]) {
                         rawMessage = (byte[])parsedList.elementAt(j + 1);
                     }
                 }
 
                 // If it wasn't in the current response line, then check for
                 // an untagged response instead
                 if(rawMessage == null) {
                     checkForUntaggedValue(rawList[i]);
                 }
             }
             else {
                 // If we already have the expected raw message body, then just
                 // check for an untagged response
                 checkForUntaggedValue(rawList[i]);
             }
         }
 
         if(rawMessage != null && rawMessage.length > 0) {
             buf.write(rawMessage, 0, rawMessage.length);
             return rawMessage.length;
         }
         else {
             return 0;
         }
     }
     
     /**
      * Execute the "STORE" command to update message flags.
      * Updated flags will be returned through the untagged response listener.
      * 
      * @param uid The message unique ID to modify.
      * @param addOrRemove True to add flags, false to remove them.
      * @param flags Array of flags to change.  (i.e. "\Seen", "\Answered")
      */
     public void executeStore(int uid, boolean addOrRemove, String[] flags) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
             buf.append("ImapProtocol.executeStore(");
             buf.append(uid);
             buf.append(", ");
             buf.append(addOrRemove ? "add" : "remove");
             buf.append(", {");
             for (int i = 0; i < flags.length; i++) {
                 buf.append('\"');
                 buf.append(flags[i]);
                 buf.append('\"');
 
                 if (i < (flags.length - 1)) {
                     buf.append(", ");
                 }
             }
             buf.append("})");
             EventLogger.logEvent(AppInfo.GUID, buf.toString().getBytes(), EventLogger.DEBUG_INFO);
         }
 
         StringBuffer buf = new StringBuffer();
         buf.append(uid);
         buf.append(' ');
         buf.append(addOrRemove ? '+' : '-');
         buf.append("FLAGS (");
 
         for (int i = 0; i < flags.length; i++) {
             buf.append(flags[i]);
 
             if (i < (flags.length - 1)) {
                 buf.append(' ');
             }
         }
 
         buf.append(')');
         
         byte[][] rawList = executeResponse(UID_STORE, buf.toString(), null);
 
         if (rawList.length < 1) {
             return;
         }
         
         for(int i=0; i<rawList.length; i++) {
             checkForUntaggedValue(rawList[i]);
         }
     }
 
     /**
      * Execute the "APPEND" command to add a message to an existing mailbox.
      * @param mboxName Mailbox name.
      * @param rawMessage The raw message text, in RFC2822-complaint format.
      * @param flags Flags to store the message with.
      */
     public void executeAppend(String mboxName, String rawMessage,
         MessageFlags flags) throws IOException, MailException {
         String flagsString = ImapParser.createMessageFlagsString(flags);
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeAppend(rawMessage, \"" + flagsString +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         
         byte[] rawData = rawMessage.getBytes();
         byte[][] rawList = executeContinue(APPEND,
             CHAR_QUOTE + StringParser.addEscapedChars(mboxName) + "\" (" +
             flagsString + ") {" + rawData.length + "}", rawData,
             "Unable to append message to " + mboxName);
         
         if(selectedMailbox != null && selectedMailbox.equals(mboxName)) {
             for(int i=0; i<rawList.length; i++) {
                 checkForUntaggedValue(rawList[i]);
             }
         }
     }
 
     /**
      * Execute the "COPY" command to copy a message from the current mailbox
      * to a different mailbox.
      *
      * @param uid The IMAP unique ID of the message to copy.
      * @param mboxPath The path of the destination mailbox
      */
     public void executeCopy(int uid, String mboxPath)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeCopy(\"" + uid + "\", \"" + mboxPath +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         execute(UID_COPY,
             uid + " \"" + StringParser.addEscapedChars(mboxPath) + CHAR_QUOTE,
             null);
     }
 
     /**
      * Execute the "LIST" command, and return a fully parsed response.
      *
      * @param refName Reference name
      * @param mboxName Mailbox name or wildcards (i.e. "%")
      * @param progressHandler the progress handler
      * @return Vector of ListResponse objects
      */
     public Vector executeList(String refName, String mboxName,
         MailProgressHandler progressHandler) throws IOException, MailException {
         //
         // The default behavior should be to use executeListSubscribed.
         // I've left this in place so that if a future version wants to
         // offer a screen for Subscribe/Unsubscribe to folders, the
         // framework will already be in place.
         //
         return executeListImpl(LIST, refName, mboxName, progressHandler);
     }
 
     /**
      * Execute the "LSUB" command, and return a fully parsed response.
      *
      * @param refName Reference name
      * @param mboxName Mailbox name or wildcards (i.e. "%")
      * @param progressHandler the progress handler
      * @return Vector of ListResponse objects
      */
     public Vector executeLsub(String refName, String mboxName,
         MailProgressHandler progressHandler) throws IOException, MailException {
         return executeListImpl(LSUB, refName, mboxName, progressHandler);
     }
 
     private Vector executeListImpl(String ListVerb, String refName,
         String mboxName, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeList(\"" + refName + "\", \"" + mboxName +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         String[] results;
         results = execute(ListVerb,
                 CHAR_QUOTE + StringParser.addEscapedChars(refName) + "\" \"" +
                 StringParser.addEscapedChars(mboxName) + CHAR_QUOTE,
                 progressHandler);
 
         Vector retVec = new Vector(results.length);
         ListResponse response;
         String temp;
         String flagStr;
         String argStr;
         int p;
         int q;
 
         // Preprocess the results for specified-length continuation lines
         Vector resultsVec = new Vector();
         int line = 0;
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 0, -1);
         }
 
         StringBuffer buf = new StringBuffer();
 
         while (line < results.length) {
             p = results[line].indexOf('{');
             q = results[line].indexOf('}');
 
             if ((line < (results.length - 1)) && (p != -1) && (q != -1) &&
                     (p < q) && (q == (results[line].length() - 1))) {
                 int len = Integer.parseInt(results[line].substring(p + 1, q));
 
                 buf.append(results[line].substring(0, p));
                 buf.append('"');
                 buf.append(results[line + 1].substring(0, len));
                 buf.append('"');
                 resultsVec.addElement(buf.toString());
                 buf.setLength(0);
                 line += 2;
             } else {
                 resultsVec.addElement(results[line]);
                 line++;
             }
         }
 
         int resultsSize = resultsVec.size();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 0, resultsSize);
         }
 
         for (int i = 0; i < resultsSize; i++) {
             // Separate out the flag and argument strings
             flagStr = null;
             argStr = null;
             temp = (String) resultsVec.elementAt(i);
             p = temp.indexOf('(');
             q = temp.indexOf(')', p + 1);
 
             if ((p != -1) && (q > p)) {
                 flagStr = temp.substring(p + 1, q);
             }
 
             if (temp.length() > (q + 2)) {
                 argStr = temp.substring(q + 2);
             }
 
             // List response is invalid if both parts are not available
             if ((flagStr == null) || (argStr == null)) {
                 continue;
             }
 
             response = new ListResponse();
             response.delim = "";
             response.name = "";
 
             // Look for flags if present.
             response.canSelect = !(flagStr.indexOf(FLAG_NOSELECT) != -1);
             response.hasChildren = (flagStr.indexOf(FLAG_HAS_CHILDREN) != -1);
             response.noInferiors = (flagStr.indexOf(FLAG_NOINFERIORS) != -1);
             response.marked = (flagStr.indexOf(FLAG_MARKED) != -1);
 
             try {
                 p = 0;
                 q = 0;
 
                 int size = argStr.length();
 
                 if (argStr.charAt(p) == '"') {
                     p++;
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != '"')) {
                         q++;
                     }
 
                     response.delim = argStr.substring(p, q);
                     p = q + 2;
                 } else {
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != ' ')) {
                         q++;
                     }
 
                     response.delim = argStr.substring(p, q);
                     p = q + 1;
                 }
 
                 if (response.delim.equalsIgnoreCase(NIL)) {
                     response.delim = "";
                 }
 
                 if (argStr.charAt(p) == '"') {
                     p++;
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != '"')) {
                         q++;
                     }
 
                     response.name = argStr.substring(p, q);
                 } else {
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != ' ')) {
                         q++;
                     }
 
                     response.name = argStr.substring(p, q);
                 }
 
                 // Strip any escaped characters
                 response.name = StringParser.removeEscapedChars(response.name);
                 response.delim = StringParser.removeEscapedChars(response.delim);
 
                 // Only add if the response is not a duplicate of the request
                 if (!(refName.equals(response.name + response.delim) ||
                         refName.equals(response.name))) {
                     retVec.addElement(response);
                 }
             } catch (Exception e) {
                 // Prevent parse errors from being fatal
             }
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                     i, resultsSize);
             }
         }
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 resultsSize, resultsSize);
         }
 
         return retVec;
     }
 
     /**
      * Execute the "NOOP" command.
      * This command does not return anything directly, but the untagged
      * response listener will be notified of any untagged responses that are
      * received during its execution.
      */
     public void executeNoop() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeNoop()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         byte[][] responses = executeResponse(NOOP, null, null);
 
         for (int i = 0; i < responses.length; i++) {
             checkForUntaggedValue(responses[i]);
         }
     }
     
     /**
      * Execute the "IDLE" command.
      * @param idleListener the listener to receive notifications during idle
      * @throws IllegalStateException if the idle thread is still active
      */
     public void executeIdle(IdleListener idleListener) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeIdle()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         if(idleThread != null && idleThread.isAlive()) {
             throw new IllegalStateException();
         }
         
         // Send the IDLE command
         String idleCommandTag = executeNoReply(IDLE, null);
         byte[] tagBytes = idleCommandTag.getBytes();
         
         // Start the idle thread
         idleThread = new IdleThread(tagBytes, idleListener);
         idleThread.start();
     }
 
     private class IdleThread extends Thread {
         private final byte[] idleCommandTag;
         private final IdleListener idleListener;
         private IOException pendingIOException;
         private MailException pendingMailException;
         
         public IdleThread(byte[] idleCommandTag, IdleListener idleListener) {
             this.idleCommandTag = idleCommandTag;
             this.idleListener = idleListener;
         }
         
         public void run() {
             try {
                 byte[] response = blockingReadIgnoringTimeout();
     
                 // Keep looping until we receive a tagged response
                 while (!StringArrays.startsWith(response, idleCommandTag)) {
                     // Check the current response for a supported untagged value
                     checkForUntaggedValue(response);
                     
                     response = blockingReadIgnoringTimeout();
                 }
     
                 if(Arrays.equals(response, idleCommandTag.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                         || Arrays.equals(response, idleCommandTag.length, NO_PREFIX, 0, NO_PREFIX.length)) {
                     throw new MailException(new String(response));
                 }
             } catch (IOException e) {
                 this.pendingIOException = e;
                 idleListener.pendingException();
             } catch (MailException e) {
                 this.pendingMailException = e;
                 idleListener.pendingException();
             }
         }
         
         private byte[] blockingReadIgnoringTimeout() throws IOException {
             byte[] response;
             while(true) {
                 try {
                     response = connection.receive();
                     break;
                 } catch (IOException e) {
                     String message = e.getMessage();
                     if(message == null || message.toLowerCase().indexOf(TIME) == -1) {
                         throw e;
                     }
                 }
             }
             return response;
         }
         
         public void throwPendingExceptions() throws IOException, MailException {
             if(pendingIOException != null) {
                 throw pendingIOException;
             }
             else if(pendingMailException != null) {
                 throw pendingMailException;
             }
         }
     }
     
     /**
      * Checks the provided server response for an untagged value, and calls the
      * appropriate listener method.
      *
      * @param response the response to parse
      * @param listener the listener to notify
      * @return true, if a value was handled
      */
     private boolean checkForUntaggedValue(byte[] response) {
         boolean result = false;
         if(response.length > 2 && response[0] == CHAR_ASTERISK && response[1] == (byte)' ') {
             int p = StringArrays.indexOf(response, (byte)' ', 2);
             
             if(p > 2 && Character.isDigit((char)response[2])) {
                 int value;
                 try {
                     value = StringArrays.parseInt(response, 2, p - 2);
                 } catch (NumberFormatException e) {
                     // Ignore values that cannot be parsed as numbers
                     return false;
                 }
                 
                 if(Arrays.equals(response, p + 1, RECENT_SUFFIX, 0, RECENT_SUFFIX.length)) {
                     if(untaggedResponseListener != null) {
                         untaggedResponseListener.recentResponse(value);
                     }
                     result = true;
                 }
                 else if(Arrays.equals(response, p + 1, EXISTS_SUFFIX, 0, EXISTS_SUFFIX.length)) {
                     if(untaggedResponseListener != null) {
                         untaggedResponseListener.existsResponse(value);
                     }
                     result = true;
                 }
                 else if(Arrays.equals(response, p + 1, EXPUNGE_SUFFIX, 0, EXPUNGE_SUFFIX.length)) {
                     if(untaggedResponseListener != null) {
                         untaggedResponseListener.expungeResponse(value);
                     }
                     result = true;
                 }
                 else if(Arrays.equals(response, p + 1, FETCH_B, 0, FETCH_B.length)) {
                     FetchFlagsResponse fetchResponse = new FetchFlagsResponse();
                     fetchResponse.index = value;
                     fetchResponse.uid = -1;
                     try {
                         int offset = Arrays.getIndex(response, (byte)'(');
                         Vector parsedText = ImapParser.parenListParser(response, offset, response.length - offset);
                         int parsedSize = parsedText.size();
                         for (int i = 0; i < parsedSize; i++) {
                             if(!(parsedText.elementAt(i) instanceof String)) { continue; }
                             String element = (String)parsedText.elementAt(i);
                             if(element.equals(FLAGS)
                                     && (parsedSize > (i + 1))
                                     && parsedText.elementAt(i + 1) instanceof Vector) {
                                 fetchResponse.flags =
                                     ImapParser.parseMessageFlags((Vector)parsedText.elementAt(i + 1));
                             }
                             else if(element.equals(UID)
                                     && (parsedSize > (i + 1))
                                     && parsedText.elementAt(i + 1) instanceof String) {
                                 try {
                                     fetchResponse.uid = Integer.parseInt((String)parsedText.elementAt(i + 1));
                                 } catch (NumberFormatException e) { }
                             }
                         }
                     } catch (Exception exp) {
                         EventLogger.logEvent(AppInfo.GUID,
                                 ("Error parsing untagged FETCH: " + exp.toString()).getBytes(),
                                 EventLogger.ERROR);
                     }
                     if(fetchResponse.flags != null) {
                         if(untaggedResponseListener != null) {
                             untaggedResponseListener.fetchResponse(fetchResponse);
                         }
                     }
                     result = true;
                 }
                 
                 if(result && EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
                     EventLogger.logEvent(AppInfo.GUID,
                             ("Untagged: " + (new String(response, 2, response.length - 2))).getBytes(),
                             EventLogger.DEBUG_INFO);
                 }
             }
         }
         return result;
     }
     
     /**
      * Execute the "DONE" command.
      */
     public void executeIdleDone() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeIdleDone()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         if(idleThread == null) {
             throw new IllegalStateException();
         }
         
         // Send the command that ends the idle state.
         // If the idle thread is not currently alive, it is because some
         // exception caused it to terminate prematurely.  In that case, there
         // is no point in trying to further communicate with the server here.
         if(watchdog != null) { watchdog.start(); }
         if(idleThread.isAlive()) {
             connection.sendCommand(DONE);
         }
         
         // Wait for the idle thread to terminate
         try {
             idleThread.join();
         } catch (InterruptedException e) {
             EventLogger.logEvent(AppInfo.GUID,
                     e.toString().getBytes(), EventLogger.ERROR);
         }
         if(watchdog != null && watchdog.isStarted()) { watchdog.cancel(); }
         
         IdleThread temp = idleThread;
         idleThread = null;
         temp.throwPendingExceptions();
     }
 
     /**
      * Executes an IMAP command several times, with different arguments,
      * and return the replies as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the commands
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected String[] executeBatch(String command, String[] arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         String[] result = new String[arguments.length];
         int count = 0;
 
         IntIntHashtable commandMap = new IntIntHashtable();
         StringBuffer commandBuf = new StringBuffer();
 
         for (int i = 0; i < arguments.length; i++) {
             String tag = TAG_PREFIX + (commandCount++);
             commandMap.put(StringArrays.hashCode(tag.getBytes()), i);
             commandBuf.append(tag);
             commandBuf.append(' ');
             commandBuf.append(command);
             commandBuf.append(
                     ((arguments[i] == null) ? "" : (CHAR_SP + arguments[i])));
             commandBuf.append(CRLF);
         }
 
         int preCount = connection.getBytesReceived();
         
         if(watchdog != null) { watchdog.start(); }
         byte[] data = commandBuf.toString().getBytes();
         connection.sendRaw(data, 0, data.length);
         if(watchdog != null) { watchdog.kick(); }
 
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         byte[] temp;
         String tempResult = "";
         int p;
 
         while (count < arguments.length) {
             preCount = postCount;
 
             temp = connection.receive();
             if(watchdog != null) { watchdog.kick(); }
             
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
 
             p = Arrays.getIndex(temp, (byte) ' ');
 
             if(Arrays.equals(temp, p + 1, BAD_PREFIX, 0, BAD_PREFIX.length)
                     || Arrays.equals(temp, p + 1, NO_PREFIX, 0, NO_PREFIX.length)) {
                 if(watchdog != null) { watchdog.cancel(); }
                 throw new MailException(new String(temp));
             }
 
             int keyHashCode = StringArrays.hashCode(temp, 0, p);
 
             if ((p != -1) && commandMap.containsKey(keyHashCode)) {
                 result[commandMap.get(keyHashCode)] = tempResult;
                 tempResult = "";
                 count++;
             } else {
                 tempResult = new String(temp);
             }
         }
 
         if(watchdog != null) { watchdog.cancel(); }
         
         return result;
     }
 
     /**
      * Executes an IMAP command, and returns the reply as an
      * array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected byte[][] executeResponse(String command, String arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         byte[][] result = new byte[0][];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.kick(); }
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive(executeResponseTester);
         if(watchdog != null) { watchdog.kick(); }
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, temp);
             preCount = postCount;
             temp = connection.receive(executeResponseTester);
             if(watchdog != null) { watchdog.kick(); }
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
 
         if(watchdog != null) { watchdog.cancel(); }
         
         if(Arrays.equals(temp, tagBytes.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                 || Arrays.equals(temp, tagBytes.length, NO_PREFIX, 0, NO_PREFIX.length)) {
             throw new MailException(new String(temp));
         }
         
         return result;
     }
 
     /**
      * Executes an IMAP command, invoking the supplied callback on each line
      * of the response.
      * 
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param callback the callback to invoke on each line of the response
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected void executeResponse(String command, String arguments,
             ExecuteCallback callback, MailProgressHandler progressHandler)
     throws IOException, MailException {
         
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.kick(); }
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive(executeResponseTester);
         if(watchdog != null) { watchdog.kick(); }
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             try {
                 callback.processResponse(temp);
             } catch (Throwable t) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Unable to parse response: " + t.getMessage()).getBytes(),
                         EventLogger.ERROR);
             }
             
             preCount = postCount;
             temp = connection.receive(executeResponseTester);
             if(watchdog != null) { watchdog.kick(); }
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
 
         if(watchdog != null) { watchdog.cancel(); }
         
         try {
             callback.executeComplete();
         } catch (Throwable t) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("Unable to complete processing the response: " + t.getMessage()).getBytes(),
                     EventLogger.ERROR);
         }
         
         if(Arrays.equals(temp, tagBytes.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                 || Arrays.equals(temp, tagBytes.length, NO_PREFIX, 0, NO_PREFIX.length)) {
             throw new MailException(new String(temp));
         }
     }
     
     /**
      * Callback interface for <code>executeResponse()</code>.
      */
     protected interface ExecuteCallback {
         void processResponse(byte[] rawLine);
         void executeComplete();
     }
 
     /**
      * Executes an IMAP command, and returns the reply as an
      * array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected String[] execute(String command, String arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         String[] result = new String[0];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.kick(); }
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive();
         if(watchdog != null) { watchdog.kick(); }
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, new String(temp));
             preCount = postCount;
             temp = connection.receive();
             if(watchdog != null) { watchdog.kick(); }
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
         
         if(watchdog != null) { watchdog.cancel(); }
         
         if(Arrays.equals(temp, tagBytes.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                 || Arrays.equals(temp, tagBytes.length, NO_PREFIX, 0, NO_PREFIX.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Executes an IMAP command, waits for a reply starting with a "+",
      * then sends more text, and ultimately returns the reply as an
      * array of strings.
      * This method is designed specifically for executeAppend().
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param errorMsg Error message if we get back something other than a continue
      * @return Command responses
      */
     protected byte[][] executeContinue(String command, String arguments,
         byte[] textData, String errorMsg) throws IOException, MailException {
         byte[][] result = new byte[0][];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.kick(); }
 
         byte[] tagBytes = tag.getBytes();
 
         byte[] temp = connection.receive();
         if(watchdog != null) { watchdog.kick(); }
 
         if (Arrays.getIndex(temp, CHAR_PLUS) == -1) {
             throw new MailException(errorMsg);
         }
 
         for(int i=0; i<textData.length; i+=1024) {
             connection.sendRaw(textData, i, Math.min(1024, textData.length - i));
             if(watchdog != null) { watchdog.kick(); }
         }
         connection.sendRaw(CRLF_B, 0, CRLF_B.length);
         if(watchdog != null) { watchdog.kick(); }
 
         temp = connection.receive();
         if(watchdog != null) { watchdog.kick(); }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, temp);
             temp = connection.receive();
             if(watchdog != null) { watchdog.kick(); }
         }
 
         if(watchdog != null) { watchdog.cancel(); }
         
         if(Arrays.equals(temp, tagBytes.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                 || Arrays.equals(temp, tagBytes.length, NO_PREFIX, 0, NO_PREFIX.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Attempts to read a line of text from the server, without sending
      * anything first.
      * @return Returned string, or null if nothing is available
      */
     protected String receive() throws IOException, MailException {
         String result;
         
         if(watchdog != null) { watchdog.start(); }
         
         if (connection.available() > 0) {
             result = new String(connection.receive());
         } else {
             result = null;
         }
 
         if(watchdog != null) { watchdog.cancel(); }
         
         return result;
     }
 
     /**
      * Executes an IMAP command directly and expects no reply.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @return Tag used to send the command
      */
     protected String executeNoReply(String command, String arguments)
         throws IOException, MailException {
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.cancel(); }
 
         return tag;
     }
 
     /**
      * Executes an IMAP command without a tag prefix,
      * and returns the reply as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param endTag Known tag that indicates the end of the result
      * @return List of returned strings
      */
     protected String[] executeUntagged(String command, String arguments,
         String endTag) throws IOException, MailException {
         String[] result = new String[0];
 
         if(watchdog != null) { watchdog.start(); }
         connection.sendCommand(command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
         if(watchdog != null) { watchdog.kick(); }
 
         byte[] tagBytes = endTag.getBytes();
         byte[] temp = connection.receive();
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, new String(temp));
             temp = connection.receive();
             if(watchdog != null) { watchdog.kick(); }
         }
         
         if(watchdog != null) { watchdog.cancel(); }
 
         if(Arrays.equals(temp, tagBytes.length, BAD_PREFIX, 0, BAD_PREFIX.length)
                 || Arrays.equals(temp, tagBytes.length, NO_PREFIX, 0, NO_PREFIX.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Callback for fetching envelopes.
      */
     public static interface FetchEnvelopeCallback {
         void responseAvailable(FetchEnvelopeResponse response);
     }
 
     /**
      * Flags that can be associated with a message.
      * Used inside reply objects for multiple commands.
      */
     public static class MessageFlags {
         /** Message has been read */
         public boolean seen;
 
         /** Message has been answered */
         public boolean answered;
 
         /** Message is flagged for special attention */
         public boolean flagged;
 
         /** Message is deleted */
         public boolean deleted;
 
         /** Message has not completed composition */
         public boolean draft;
 
         /** Message has recently arrived */
         public boolean recent;
 
         /** Message has been forwarded */
         public boolean forwarded;
 
         /** Message has been flagged as junk */
         public boolean junk;
     }
 
     /**
      * Container for a single namespace entry
      */
     public static class Namespace {
         /** Namespace prefix */
         public String prefix;
 
         /** Hierarchy delimiter */
         public String delimiter;
 
         // Extensions not currently parsed
     }
 
     /**
      * Container for a NAMESPACE response
      */
     public static class NamespaceResponse {
         /** Personal Namespace(s) */
         public Namespace[] personal;
 
         /** Other Users' Namespace(s) */
         public Namespace[] other;
 
         /** Shared Namespace(s) */
         public Namespace[] shared;
     }
 
     /**
      * Container for a SELECT response
      */
     public static class SelectResponse {
         //public MessageFlags availableFlags; // not used
         //public MessageFlags permanentFlags; // not used
         public int exists;
         public int recent;
         public int unseen;
         public int uidNext = -1;
         public int uidValidity;
     }
 
     /**
      * Container for a STATUS response
      */
     public static class StatusResponse {
         public int exists;
         public int unseen;
         public int recent;
     }
 
     /**
      * Container for a FETCH (FLAGS) response
      */
     public static class FetchFlagsResponse {
         public int index;
         public int uid;
         public MessageFlags flags;
     }
 
     /**
      * Container for a FETCH (ENVELOPE) response
      */
     public static class FetchEnvelopeResponse extends FetchFlagsResponse {
         public MessageEnvelope envelope;
         public ImapParser.MessageSection structure;
     }
 
     /**
      * Container for a LIST response
      */
     public static class ListResponse {
         public boolean hasChildren;
         public boolean noInferiors;
         public boolean canSelect;
         public boolean marked;
         public String delim;
         public String name;
     }
 
     /**
      * Listener for untagged responses sent during other commands.
      */
     public interface UntaggedResponseListener {
         void existsResponse(int value);
         void recentResponse(int value);
         void expungeResponse(int value);
         void fetchResponse(FetchFlagsResponse value);
     }
     
     /**
      * Listener for any special cases during IDLE mode.
      */
     public interface IdleListener {
         void pendingException();
     }
     
     // String constants
     private static String UID_COPY = "UID COPY";
     private static String UID_STORE = "UID STORE";
     private static String UID_FETCH = "UID FETCH";
     private static String DONE = "DONE";
     private static String IDLE = "IDLE";
     private static String NOOP = "NOOP";
     private static String NIL = "NIL";
     private static String LSUB = "LSUB";
     private static String LIST = "LIST";
     private static String FETCH = "FETCH";
     private static String BODYSTRUCTURE = "BODYSTRUCTURE";
     private static String BODY = "BODY";
     private static String ENVELOPE = "ENVELOPE";
     private static String UID = "UID";
     private static String FLAGS = "FLAGS";
     private static String UNSEEN = "UNSEEN";
     private static String RECENT = "RECENT";
     private static String MESSAGES = "MESSAGES";
     private static String NAMESPACE = "NAMESPACE";
     private static String CAPABILITY = "CAPABILITY";
     private static String EXPUNGE = "EXPUNGE";
     private static String APPEND = "APPEND";
     private static String STATUS = "STATUS";
     private static String SELECT = "SELECT";
     private static String CLOSE = "CLOSE";
     private static String LOGOUT = "LOGOUT";
     private static String LOGIN = "LOGIN";
     private static String STARTTLS = "STARTTLS";
     private static String COMPRESS = "COMPRESS";
     private static String DEFLATE = "DEFLATE";
     private static String FLAG_MARKED = "\\Marked";
     private static String FLAG_NOINFERIORS = "\\Noinferiors";
     private static String FLAG_HAS_CHILDREN = "\\HasChildren";
     private static String FLAG_NOSELECT = "\\Noselect";
     private static String TAG_PREFIX = "A";
     private static final byte[] NO_PREFIX = "NO ".getBytes();
     private static final byte[] BAD_PREFIX = "BAD ".getBytes();
     private static final byte[] RECENT_SUFFIX = "RECENT".getBytes();
     private static final byte[] EXISTS_SUFFIX = "EXISTS".getBytes();
     private static final byte[] EXPUNGE_SUFFIX = "EXPUNGE".getBytes();
     private static final byte[] FETCH_B = "FETCH".getBytes();
     private static String CHAR_SP = " ";
     private static final byte CHAR_PLUS = (byte)'+';
     private static String CHAR_COLON = ":";
     private static final byte CHAR_ASTERISK = (byte)'*';
     private static String CHAR_QUOTE = "\"";
     private static String CHAR_COLON_ASTERISK = ":*";
     private static String CRLF = "\r\n";
     private static final byte[] CRLF_B = CRLF.getBytes();
     private static String TIME = "time";
     private static String UIDNEXT_ = "UIDNEXT ";
     private static String UIDVALIDITY_ = "UIDVALIDITY ";
     private static String UNSEEN_ = "UNSEEN ";
     private static String _RECENT = " RECENT";
     private static String _EXISTS = " EXISTS";
 }
