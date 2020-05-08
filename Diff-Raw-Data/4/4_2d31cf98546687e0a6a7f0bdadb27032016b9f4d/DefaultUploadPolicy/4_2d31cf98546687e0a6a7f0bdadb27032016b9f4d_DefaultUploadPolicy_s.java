 //
// $Id: DefaultUploadPolicy.java 95 2007-05-02 03:27:05Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-05-04
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.policies;
 
 import java.awt.GridLayout;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 // import sun.plugin.javascript.JSObject;
 
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 
 import wjhk.jupload2.JUploadApplet;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
 import wjhk.jupload2.filedata.DefaultFileData;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.gui.JUploadFileFilter;
 import wjhk.jupload2.gui.JUploadTextArea;
 
 /**
  * This class implements all {@link wjhk.jupload2.policies.UploadPolicy}
  * methods. Its way of working is he same as the JUpload version 1. <BR>
  * The simplest way to use this policy is given in the presentation of
  * {@link UploadPolicy}. The DefaultUploadPolicy is used when no
  * <I>uploadPolicy</I> parameter is given to the applet, or this parameter has
  * 'DefaultUploadPolicy' as a value. <BR>
  * <P>
  * The <U>default behavior</U> is representated below. It can be overrided by
  * adding parameters to the applet. All available parameters are shown in the
  * presentation of {@link UploadPolicy}.
  * </P>
  * <UL>
  * <LI>Default implementation for all
  * {@link wjhk.jupload2.policies.UploadPolicy} methods.
  * <LI>Files are uploaded all in one HTTP request.
  * <LI>No handling for particular kind of files: files are transmitted without
  * any transformation.
  * <LI>The file are transmitted to the server with the navigator cookies,
  * userAgent and Protocol. This make upload occurs within the current user
  * session on the server. So, it allows right management and context during the
  * management of uploaded files, on the server.
  * </UL>
  * 
  * @author Etienne Gauthier
  * @version $Revision$
  */
 
 public class DefaultUploadPolicy implements UploadPolicy {
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// APPLET PARAMETERS
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * applet contains the reference of the Applet. It's useful to interact with
      * it. <BR>
      * It also allows acccess to the navigator properties, if the html tag
      * MAYSCRIPT is put in the APPLET tag. This allows this class to get the
      * cookie, userAgent and protocol, to upload files in the current user
      * session on the server. <BR>
      * Default : no default value
      */
     private JUploadApplet applet = null;
 
     /**
      * Contains the applet parameter of the same name. If a valid URL is given
      * here, the navigator will get redirected to this page, after a successful
      * upload.
      */
     private String afterUploadURL = UploadPolicy.DEFAULT_AFTER_UPLOAD_URL;
 
     /**
      * Contains the allowedFileExtensions applet paramater.
      */
     private String allowedFileExtensions = UploadPolicy.DEFAULT_ALLOWED_FILE_EXTENSIONS;
 
     /**
      * Indicate whether the status bar is shown or not to the user. In all cases
      * it remains in memory, and stores all debug information. This allows a log
      * information, in case of an error occurs.
      * 
      * @see #urlToSendErrorTo
      */
     private boolean showStatusBar = UploadPolicy.DEFAULT_SHOW_STATUSBAR;
 
     /**
      * The current debug level.
      */
     private int debugLevel = UploadPolicy.DEFAULT_DEBUG_LEVEL;
 
     /**
      * This String contains the filenameEncoding parameter. All details about
      * the available applet parameters are displayed in the <a
      * href="UploadPolicy.html@parameters">Upload Policy javadoc page</a>.
      */
     private String filenameEncoding = UploadPolicy.DEFAULT_FILENAME_ENCODING;
 
     /**
      * The lang parameter, given to the applet.
      */
     private String lang = UploadPolicy.DEFAULT_LANG;
 
     /**
      * The look and feel is used as a parameter of the
      * UIManager.setLookAndFeel(String) method. See the parameters list on the
      * {@link UploadPolicy} page.
      */
     private String lookAndFeel = UploadPolicy.DEFAULT_LOOK_AND_FEEL;
 
     /**
      * The applet will do as may HTTP requests to upload all files, with the
      * number as a maximum number of files for each HTTP request. <BR>
      * Default : -1
      */
     private int nbFilesPerRequest = UploadPolicy.DEFAULT_NB_FILES_PER_REQUEST;
 
     /**
      * Current value (or default value) of the maxChunkSize applet parameter.
      * <BR>
      * Default : Long.MAX_VALUE
      */
     private long maxChunkSize = UploadPolicy.DEFAULT_MAX_CHUNK_SIZE;
 
     /**
      * Current value (or default value) of the maxFileSize applet parameter.
      * <BR>
      * Default : Long.MAX_VALUE
      */
     private long maxFileSize = UploadPolicy.DEFAULT_MAX_FILE_SIZE;
 
     /**
      * The URL where files should be posted. <BR>
      * Default : no default value. (mandatory)
      */
     private String postURL = UploadPolicy.DEFAULT_POST_URL;
 
     /**
      * @see UploadPolicy#getServerProtocol()
      */
     private String serverProtocol = UploadPolicy.DEFAULT_SERVER_PROTOCOL;
 
     /**
      * @see UploadPolicy#getStringUploadSuccess()
      */
     private String stringUploadSuccess = UploadPolicy.DEFAULT_STRING_UPLOAD_SUCCESS;
 
     /**
      * If an error occurs during upload, and this attribute is not null, the
      * applet asks the user if wants to send the debug ouput to the
      * administrator. If yes, the full debug information is POSTed to this URL.
      * It's a little development on the server side to send a mail to the
      * webmaster, or just log this error into a log file.
      * 
      * @see UploadPolicy#sendDebugInformation(String)
      */
     private String urlToSendErrorTo = UploadPolicy.DEFAULT_URL_TO_SEND_ERROR_TO;
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// INTERNAL ATTRIBUTE
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This Vector contains headers that will be added for each upload. It may
      * contains specific cookies, for instance.
      * 
      * @see #onAppendHeader(StringBuffer)
      */
     private Vector<String> headers = new Vector<String>();
 
     /**
      * The text area, where message are to be displayed.
      * 
      * @see #displayMsg(String)
      */
     private JUploadTextArea statusArea = null;
 
     /**
      * The resourceBundle contains all localized String (and others ??)
      */
     private ResourceBundle resourceBundle = null;
 
     /**
      * This StringBuffer is used to store all information that could be useful,
      * in case a problem occurs. Is content can then be sent to the webmaster.
      */
     private StringBuffer debugBufferString = new StringBuffer();
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// CONSTRUCTORS
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The main constructor : use default values, and the given postURL.
      * 
      * @param theApplet The current applet. As the reference to the current
      *            upload policy exists almost everywhere, this parameter allows
      *            any access to anyone on the applet... including reading the
      *            applet parameters.
      */
     public DefaultUploadPolicy(JUploadApplet theApplet) {
         // Call default constructor for all default initialization;.
         this.applet = theApplet;
         this.statusArea = theApplet.getStatusArea();
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the afterUploadURL applet parameter.
         setAfterUploadURL(UploadPolicyFactory.getParameter(theApplet,
                 PROP_AFTER_UPLOAD_URL, DEFAULT_AFTER_UPLOAD_URL, this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the allowedFileExtensions applet parameter
         setAllowedFileExtensions(UploadPolicyFactory.getParameter(theApplet,
                 PROP_ALLOWED_FILE_EXTENSIONS, DEFAULT_ALLOWED_FILE_EXTENSIONS,
                 this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the showStatusBar applet parameter.
         setShowStatusBar(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SHOW_STATUSBAR, DEFAULT_SHOW_STATUSBAR, this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the debug level. This control the level of debug messages that
         // are written
         // in the status area (see displayDebugMessage). In all cases, the full
         // output
         // is written in the debugBufferString (see also urlToSendErrorTo)
         setDebugLevel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL, this), false);
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the filenameEncoding. If not null, it should be a valid argument
         // for
         // the URLEncoder.encode method.
         setFilenameEncoding(UploadPolicyFactory.getParameter(theApplet,
                 PROP_FILENAME_ENCODING, DEFAULT_FILENAME_ENCODING, this));
 
         // Force the look and feel of the current system.
         setLookAndFeel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum number of files to upload in one HTTP request.
         setNbFilesPerRequest(UploadPolicyFactory.getParameter(theApplet,
                 PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum size of a file on one HTTP request (indicate if the
         // file
         // must be splitted before upload, see UploadPolicy comment).
         setMaxChunkSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_CHUNK_SIZE, DEFAULT_MAX_CHUNK_SIZE, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum size of an uploaded file.
         setMaxFileSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_FILE_SIZE, DEFAULT_MAX_FILE_SIZE, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the URL where files must be posted.
         setPostURL(UploadPolicyFactory.getParameter(theApplet, PROP_POST_URL,
                 DEFAULT_POST_URL, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the server protocol.
         // It is used by Coppermine Picture Gallery (nice tool) to control that
         // the user
         // sending the cookie uses the same http protocol that the original
         // connexion.
         // Please have a look tp the UploadPolicy.serverProtocol attribute.
         setServerProtocol(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the upload String Success. See Uploadolicy#getStringUploadSuccess
         // It is used by Coppermine Picture Gallery (nice tool) to control that
         // the user
         // sending the cookie uses the same http protocol that the original
         // connexion.
         // Please have a look tp the UploadPolicy.serverProtocol attribute.
         setStringUploadSuccess(UploadPolicyFactory
                 .getParameter(theApplet, PROP_STRING_UPLOAD_SUCCESS,
                         DEFAULT_STRING_UPLOAD_SUCCESS, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the URL where the full debug output can be sent when an error
         // occurs.
         setUrlToSendErrorTo(UploadPolicyFactory.getParameter(theApplet,
                 PROP_URL_TO_SEND_ERROR_TO, DEFAULT_URL_TO_SEND_ERROR_TO, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // Get resource file.
         setLang(UploadPolicyFactory.getParameter(theApplet, PROP_LANG,
                 DEFAULT_LANG, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // Load session data read from the navigator:
         // - cookies.
         // - User-Agent : re
         //
         // cookie is the value of the javascript <I>document.cookie</I>
         // property.
         String cookie = null;
         // userAgent is the value of the javascript <I>navigator.userAgent</I>
         // property.
         String userAgent = null;
 
         try {
             // Test, to avoid a crash under linux
             JSObject applet = JSObject.getWindow(getApplet());
             JSObject doc = (JSObject) applet.getMember("document");
             cookie = (String) doc.getMember("cookie");
 
             JSObject nav = (JSObject) applet.getMember("navigator");
             userAgent = (String) nav.getMember("userAgent");
 
             displayDebug("cookie: " + cookie, 10);
             displayDebug("userAgent: " + userAgent, 10);
         } catch (JSException e) {
             displayWarn("JSException (" + e.getClass() + ": " + e.getMessage()
                     + ") in DefaultUploadPolicy, trying default values.");
 
             // If we can't have access to the JS objects, we're in development :
             // Let's put some 'hard value', to test the applet from the
             // development tool (mine is eclipse).
 
             // felfert: I need different values so let's make that
             // configurable...
             cookie = System.getProperty("debug_cookie");
             userAgent = System.getProperty("debug_agent");
             // cookie =
             // "cpg146_data=YTo1OntzOjI6IklEIjtzOjMyOiI5MWEyMzdiNmYwYmM0MTJjMjRiMTZlNzdiNzlmYzBjMyI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO3M6MzoibGFwIjtpOjI7czozOiJsaXYiO2E6NDp7aTowO3M6MzoiNDMzIjtpOjE7czozOiI0NDUiO2k6MjtzOjM6IjQ0NiI7aTozO3M6MzoiNDY2Ijt9fQ%3D%3D;
             // 66f77117891c8a8654024874bc0a5d24=56357ed95576afa0d8beb558bdb6c73d";
             // userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; fr;
             // rv:1.8.1) Gecko/20061010 Firefox/2.0";
         }
         // The cookies and user-agent will be added to the header sent by the
         // applet:
         if (cookie != null)
             addHeader("Cookie: " + cookie);
         if (userAgent != null)
             addHeader("User-Agent: " + userAgent);
 
         // We let the UploadPolicyFactory call the displayParameterStatus
         // method, so that the
         // initialization is finished, including for classes which inherit from
         // DefaultUploadPolicy.
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// UploadPolicy methods
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // getters and setters are sorted below
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#addHeader(java.lang.String)
      */
     public void addHeader(String header) {
         this.headers.add(header);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#beforeUpload()
      */
     public void beforeUpload() {
         // Default: no special action.
     }
 
     /**
      * The default behaviour (see {@link DefaultUploadPolicy}) is to check that
      * the stringUploadSuccess applet parameter is present in the response from
      * the server. The return is tested, in the order below: <DIR>
      * <LI>True, if the stringUploadSuccess was not given as an applet
      * parameter (no test at all).
      * <LI>True, if the stringUploadSuccess string is present in the
      * serverOutputBody.
      * <LI>True, If previous condition is not filled, but the HTTP header
      * "HTTP(.*)200OK$" is present: the test is currently non blocking, because
      * I can not test all possible HTTP configurations.<BR>
      * Note: If "Transfer-Encoding: chunked" is present, the body may be cut by
      * 'strange' characters, which prevent to find the success string. Then, a
      * warning is displayed.
      * <LI>False if the previous conditions are not fullfilled. </DIR>
      * 
      * @param serverOutput The full HTTP answer, including the http headers.
      * @param serverOutputBody The body of the HTTP answer.
      * @return True or False, indicating if the upload is a success or not.
      * @see UploadPolicy#checkUploadSuccess(String, String)
      */
     public boolean checkUploadSuccess(String serverOutput,
             String serverOutputBody) throws JUploadException {
         final Pattern patternSuccess = Pattern
                 .compile(this.stringUploadSuccess);
         final Pattern patternTransferEncodingChunked = Pattern.compile(
                 "^Transfer-Encoding: chunked", Pattern.CASE_INSENSITIVE);
         // La premi�re ligne est de la forme "HTTP/1.1 NNN Texte", o� NNN et le
         // code HTTP de retour (200, 404, 500...)
         final Pattern patternHttpStatus = Pattern.compile(
                 "HTTP[^ ]* ([^ ]*) .*", Pattern.DOTALL);
 
         if (this.stringUploadSuccess.equals(""))
             return true;
 
         // The success string should be in the http body
         boolean uploadSuccess = patternSuccess.matcher(serverOutputBody).find();
         // The transfert encoding may be present in the serverOutput (that
         // contains the http headers)
         boolean uploadTransferEncodingChunked = patternTransferEncodingChunked
                 .matcher(serverOutput).find();
 
         // And have a match, to search for the http return code (200 for Ok)
         Matcher matcherUploadHttpStatus = patternHttpStatus
                 .matcher(serverOutput);
         if (!matcherUploadHttpStatus.matches())
             throw new JUploadException(
                     "Can't find the HTTP status in serverOutput!");
 
         int httpStatus = Integer.parseInt(matcherUploadHttpStatus.group(1));
         boolean upload_200_OK = (httpStatus == 200);
 
         displayDebug("HTTP return code: " + httpStatus, 40);
 
         // Let's find what we should answer:
         if (uploadSuccess) {
             return true;
         } else if (uploadTransferEncodingChunked && upload_200_OK) {
             // Hum, as the transfert encoding is chuncked, the success
             // string may be splitted. We display
             // an info message, and expect everything is Ok.
             // FIXME The chunked encoding should be correctly handled,
             // instead of the current 'expectations' below.
             displayInfo("The transertEncoding is chunked, and http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
             return true;
         } else if (upload_200_OK) {
             // This method is currently non blocking.
             displayWarn("The http upload is technically Ok, but the success string was not found. Suspicion is that upload was Ok...let's go on");
             // We raise no exception (= success)
             return true;
         } else {
             // The upload is not successful: here, we know it!
             throw new JUploadExceptionUploadFailed(getClass().getName()
                     + ".checkUploadSuccess(): The http return code is : "
                     + httpStatus + " (should be 200)");
         }
     }// isUploadSuccessful
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
      */
     public void afterUpload(Exception e, @SuppressWarnings("unused")
     String serverOutput) {
         // If there was no error, and afterUploadURL is defined, let's try to go
         // to this URL.
         if (e == null && getAfterUploadURL() != null) {
             try {
                 if (getDebugLevel() >= 100) {
                     alertStr("No switch to getAfterUploadURL, because debug level is "
                             + getDebugLevel() + " (>=100)");
                 } else {
                     if (true) {
                         // Let's change the current URL to edit names and
                         // comments,
                         // for the selected album.
                         // Ok, let's go and add names and comments to the newly
                         // updated pictures.
                         JSObject applet = JSObject.getWindow(getApplet());
                         JSObject doc = (JSObject) applet.getMember("document");
                         JSObject loc = (JSObject) doc.getMember("location");
                         Object[] argsReplace = {
                             getAfterUploadURL()
                         };
                         loc.call("replace", argsReplace);
                     } else {
                         getApplet().getAppletContext().showDocument(
                                 new URL(getAfterUploadURL()), "_self");
                     }
                 }
             } catch (Exception ee) {
                 // Oops, no navigator. We are probably in debug mode, within
                 // eclipse for instance.
                 displayErr(ee);
             }
         }
     }
 
     /** @see UploadPolicy#alertStr(String) */
     public void alertStr(String str) {
         JOptionPane.showMessageDialog(null, str, "Alert",
                 JOptionPane.WARNING_MESSAGE);
     }
 
     /** @see UploadPolicy#alert(String) */
     public void alert(String key) {
         alertStr(getString(key));
     }
 
     /** @see UploadPolicy#alert(String,String) */
     public void alert(String key, String arg1) {
         alertStr(getString(key, arg1));
     }
 
     /**
      * The DefaultUpload accepts all file types: we just return an instance of
      * FileData, without any test.
      * 
      * @see UploadPolicy#createFileData(File)
      */
     public FileData createFileData(File file) {
         return new DefaultFileData(file, this);
     }
 
     /**
      * Default implementation of
      * {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
      * IT creates a JPanel, containing the three given JButton. It creates the
      * same panel as the original JUpload.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton,
      *      JButton, JPanel)
      */
     public JPanel createTopPanel(JButton browse, JButton remove,
             JButton removeAll, @SuppressWarnings("unused")
             JPanel mainPanel) {
         JPanel jPanel = new JPanel();
         jPanel.setLayout(new GridLayout(1, 3));
         jPanel.add(browse);
         jPanel.add(removeAll);
         jPanel.add(remove);
         return jPanel;
     }
 
     /**
      * @see UploadPolicy#displayErr(Exception)
      */
     public void displayErr(Exception e) {
         displayErr(e.getClass().getName() + ": " + e.getLocalizedMessage());
     }
 
     /**
      * @see UploadPolicy#displayErr(String)
      */
     public void displayErr(String err) {
         // If debug is off, the status bar may not be visible. We switch the
         // debug to on, to be sure that some
         // information will be displayed to the user.
         if (getDebugLevel() <= 0) {
             setDebugLevel(1);
         }
         displayMsg("[ERROR] ", err);
     }
 
     /**
      * @see UploadPolicy#displayInfo(String)
      */
     public void displayInfo(String info) {
         displayMsg("[INFO] ", info);
     }
 
     /**
      * @see UploadPolicy#displayWarn(String)
      */
     public void displayWarn(String warn) {
         displayMsg("[WARN] ", warn);
     }
 
     /**
      * @see UploadPolicy#displayDebug(String, int)
      */
     public void displayDebug(String debug, int minDebugLevel) {
         final String tag = "[DEBUG] ";
         if (this.debugLevel >= minDebugLevel) {
             // displayMsg will add the message to the debugStrignBuffer.
             displayMsg(tag, debug);
         } else {
             // Let's store all text in the debug BufferString
             addMsgToDebugBufferString(timestamp(tag, debug));
         }
     }
 
     /** @see UploadPolicy#getString(String) */
     public String getString(String key) {
         String ret = this.resourceBundle.getString(key);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String) */
     public String getString(String key, String value1) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String,String) */
     public String getString(String key, String value1, String value2) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1).replaceAll("\\{2\\}", value2);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String,String,String) */
     public String getString(String key, String value1, String value2,
             String value3) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1).replaceAll("\\{2\\}", value2).replaceAll("\\{3\\}",
                 value3);
         return ret;
     }
 
     /**
      * @see UploadPolicy#getString(String,int)
      */
     public String getString(String key, int value1) {
         return getString(key, Integer.toString(value1));
     }
 
     /**
      * @see UploadPolicy#getUploadFilename(FileData, int)
      */
     public String getUploadFilename(FileData fileData,
             @SuppressWarnings("unused")
             int index) throws JUploadException {
         if (this.filenameEncoding == null || this.filenameEncoding.equals(""))
             return fileData.getFileName();
         try {
             return URLEncoder.encode(fileData.getFileName(),
                     this.filenameEncoding);
         } catch (UnsupportedEncodingException e) {
             throw new JUploadException(e);
         }
     }
 
     /** @see UploadPolicy#getUploadName(FileData, int) */
     public String getUploadName(@SuppressWarnings("unused")
     FileData fileData, int index) {
         // This is the original way of working of JUpload.
         // It can easily be modified, by using another UploadPolicy.
         return "File" + index;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#isUploadReady() */
     public boolean isUploadReady() {
         // Default : nothing to do before upload, so we're ready.
         return true;
     }
 
     /** @see UploadPolicy#onAppendHeader(StringBuffer) */
     public StringBuffer onAppendHeader(StringBuffer sb) {
         Iterator it = this.headers.iterator();
         String header;
         while (it.hasNext()) {
             header = (String) it.next();
             displayDebug(header, 90);
             sb.append(header);
             sb.append("\r\n");
         }
         return sb;
     }// appendHeader
 
     /**
      * Default implementation of the
      * {@link wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)}.
      * Nothing's done.
      */
     public void onSelectFile(@SuppressWarnings("unused")
     FileData fileData) {
         // Default implementation : no action
     }
 
     /** @see UploadPolicy#sendDebugInformation(String) */
     public void sendDebugInformation(String description) {
 
         if (this.urlToSendErrorTo.length() > 0) {
             if (JOptionPane.showConfirmDialog(null,
                     getString("questionSendMailOnError"), "Confirm",
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                 displayDebug("Within response == true", 60);
 
                 // The message is written in english, as it is not sure that the
                 // webmaster speaks the same language as the current user.
                 String query = null;
                 String action = null;
                 Socket sock = null;
                 DataOutputStream dataout = null;
                 BufferedReader datain = null;
                 StringBuffer sbHttpResponseBody = null;
                 StringBuffer request = null;
 
                 try {
                     query = "description="
                             + URLEncoder.encode(description, "UTF-8")
                             + "&log="
                             + URLEncoder
                                     .encode(
                                             "\n\nAn error occured during upload, in JUpload\n"
                                                     + "All debug information is available below\n\n\n\n"
                                                     + this.debugBufferString
                                                             .toString(),
                                             "UTF-8");
                     request = new StringBuffer();
                     URL url = new URL(this.urlToSendErrorTo);
                     request
                             .append("POST ")
                             .append(url)
                             .append(" ")
                             .append(getServerProtocol())
                             .append("\r\n")
                             .append("Host: ")
                             .append(url.getHost())
                             .append("\r\n")
                             .append("Accept: */*\r\n")
                             .append(
                                     "Content-type: application/x-www-form-urlencoded\r\n")
                             .append("Connection: close\r\n").append(
                                     "Content-length: ").append(query.length())
                             .append("\r\n");
                     // Get specific headers for this upload.
                     onAppendHeader(request);
                     // Blank line (end of header)
                     request.append("\r\n").append(query);
 
                     // If port not specified then use default http port 80.
                     sock = new Socket(url.getHost(), (-1 == url.getPort()) ? 80
                             : url.getPort());
                     dataout = new DataOutputStream(new BufferedOutputStream(
                             sock.getOutputStream()));
                     datain = new BufferedReader(new InputStreamReader(sock
                             .getInputStream()));
                     // DataInputStream datain = new DataInputStream(new
                     // BufferedInputStream(sock.getInputStream()));
 
                     // Send http request to server
                     action = "send bytes (1)";
                     dataout.writeBytes(request.toString());
                     dataout.writeBytes(query);
                     action = "flush";
                     dataout.flush();
                     action = "wait for server answer";
                     String strUploadSuccess = getStringUploadSuccess();
                     boolean uploadSuccess = false;
                     boolean readingHttpBody = false;
                     sbHttpResponseBody = new StringBuffer();
                     String line;
                     // Now, we wait for the full answer (which should mean that
                     // the uploaded files
                     // has been treated on the server)
                     while ((line = datain.readLine()) != null) {
 
                         // Is this upload a success ?
                         action = "test success";
                         if (line.matches(strUploadSuccess)) {
                             uploadSuccess = true;
                         }
 
                         // Store the http body
                         if (readingHttpBody) {
                             action = "sbHttpResponseBody";
                             sbHttpResponseBody.append(line).append("\n");
                         }
                         if (line.length() == 0) {
                             // Next lines will be the http body (or perhaps we
                             // already are in the body, but it's Ok anyway)
                             action = "readingHttpBody";
                             readingHttpBody = true;
                         }
                     }
                     // Is our upload a success ?
                     if (!uploadSuccess) {
                         throw new JUploadExceptionUploadFailed(
                                 getString("errHttpResponse"));
                     }
 
                 } catch (Exception e) {
                     displayErr(getString("errDuringLogManagement") + " ("
                             + action + ") (" + e.getClass() + ") : "
                             + e.getMessage());
                 } finally {
                     try {
                         dataout.close();
                     } catch (Exception e) {
                         displayErr(getString("errDuringLogManagement")
                                 + " (dataout.close) (" + e.getClass() + ") : "
                                 + e.getMessage());
                     }
                     dataout = null;
                     try {
                         // Throws java.io.IOException
                         datain.close();
                     } catch (Exception e) {
                         // Nothing to do.
                     }
                     datain = null;
                     try {
                         // Throws java.io.IOException
                         sock.close();
                     } catch (Exception e) {
                         displayErr(getString("errDuringLogManagement")
                                 + " (sock.close)(" + e.getClass() + ") : "
                                 + e.getMessage());
                     }
                     sock = null;
                     displayDebug("Sent to server: " + request.toString(), 100);
                     displayDebug("Body received: "
                             + sbHttpResponseBody.toString(), 100);
 
                 }
             }
         }
     }// sendDebugInformation
 
     /**
      * This method manages all applet parameters. It allows javascript to update
      * their value, for instance after the user chooses a value in a list ...
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
      *      java.lang.String)
      */
     public void setProperty(String prop, String value) {
         if (prop.equals(PROP_AFTER_UPLOAD_URL)) {
             setAfterUploadURL(value);
         } else if (prop.equals(PROP_ALLOWED_FILE_EXTENSIONS)) {
             setAllowedFileExtensions(value);
         } else if (prop.equals(PROP_DEBUG_LEVEL)) {
             setDebugLevel(UploadPolicyFactory.parseInt(value, this.debugLevel,
                     this));
         } else if (prop.equals(PROP_LANG)) {
             setAfterUploadURL(value);
         } else if (prop.equals(PROP_FILENAME_ENCODING)) {
             setFilenameEncoding(value);
         } else if (prop.equals(PROP_LOOK_AND_FEEL)) {
             setLookAndFeel(value);
         } else if (prop.equals(PROP_MAX_CHUNK_SIZE)) {
             setMaxChunkSize(UploadPolicyFactory.parseLong(value,
                     this.maxChunkSize, this));
         } else if (prop.equals(PROP_MAX_FILE_SIZE)) {
             setMaxFileSize(UploadPolicyFactory.parseLong(value,
                     this.maxFileSize, this));
         } else if (prop.equals(PROP_NB_FILES_PER_REQUEST)) {
             setNbFilesPerRequest(UploadPolicyFactory.parseInt(value,
                     this.nbFilesPerRequest, this));
         } else if (prop.equals(PROP_POST_URL)) {
             setPostURL(value);
         } else if (prop.equals(PROP_SERVER_PROTOCOL)) {
             setServerProtocol(value);
         } else if (prop.equals(PROP_STRING_UPLOAD_SUCCESS)) {
             setStringUploadSuccess(value);
         } else if (prop.equals(PROP_URL_TO_SEND_ERROR_TO)) {
             setUrlToSendErrorTo(value);
         } else {
             displayWarn("Unknown applet parameter: " + prop
                     + " (in DefaultUploadPolicy.setProperty)");
         }
 
     }
 
     /**
      * This method displays the applet parameter list, according to the current
      * debugLevel. It is called by the {@link #setDebugLevel(int)} method. It
      * should be override by any subclasses, that should display its own
      * parameters, then call <I>super.displayParameterStatus()</I>.
      * 
      * @see UploadPolicy#displayParameterStatus()
      */
     public void displayParameterStatus() {
         // Let's handle the language:
         if (getDebugLevel() >= 20) {
             displayDebug(
                     "=======================================================================",
                     20);
             // /////////////////////////////////////////////////////////////////////////////
             // Let's display some information to the user, about the received
             // parameters.
             displayInfo("JUpload applet, version " + JUploadApplet.VERSION
                     + " (" + JUploadApplet.LAST_MODIFIED
                     + "), available at http://jupload.sourceforge.net/");
             displayDebug("Java version  : "
                     + System.getProperty("java.version"), 20);
 
             displayDebug("List of all applet parameters:", 20);
             displayDebug("  language: "
                     + this.resourceBundle.getLocale().getLanguage(), 20);
             displayDebug("  country: "
                     + this.resourceBundle.getLocale().getCountry(), 20);
 
             displayDebug("afterUploadURL: " + getAfterUploadURL(), 20);
             displayDebug("  allowedFileExtensions: "
                     + getAllowedFileExtensions(), 20);
             displayDebug("debug: " + this.debugLevel, 1);
             displayDebug("filenameEncoding: " + this.filenameEncoding, 20);
             displayDebug("lang: " + this.lang, 20);
             displayDebug("maxChunkSize: " + this.maxChunkSize, 20);
             if (this.maxFileSize == Long.MAX_VALUE) {
                 // If the maxFileSize was not given, we display its value only
                 // in debug mode.
                 displayDebug("maxFileSize  : " + this.maxFileSize, 20);
             } else {
                 // If the maxFileSize was given, we always inform the user.
                 displayInfo("maxFileSize  : " + this.maxFileSize);
             }
             displayDebug("nbFilesPerRequest: " + this.nbFilesPerRequest, 20);
             displayDebug("postURL: " + this.postURL, 20);
             displayDebug("serverProtocol: " + this.serverProtocol, 20);
             displayDebug("showStatusBar: " + getShowStatusBar(), 20);
             displayDebug("stringUploadSuccess: " + this.stringUploadSuccess, 20);
             displayDebug("urlToSendErrorTo: " + this.urlToSendErrorTo, 20);
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// getters / setters
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /** @see UploadPolicy#getAfterUploadURL() */
     public String getAfterUploadURL() {
         return this.afterUploadURL;
     }
 
     /** @param afterUploadURL the afterUploadURL to set */
     protected void setAfterUploadURL(String afterUploadURL) {
         this.afterUploadURL = afterUploadURL;
     }
 
     /** @see UploadPolicy#getAllowedFileExtensions() */
     public String getAllowedFileExtensions() {
         return this.allowedFileExtensions;
     }
 
     /** @param allowedFileExtensions the allowedFileExtensions to set */
     protected void setAllowedFileExtensions(String allowedFileExtensions) {
         if (allowedFileExtensions == null || allowedFileExtensions.equals("")) {
             this.allowedFileExtensions = null;
         } else {
             this.allowedFileExtensions = (allowedFileExtensions.startsWith("/") ? ""
                     : "/")
                     + allowedFileExtensions.toLowerCase()
                     + (allowedFileExtensions.endsWith("/") ? "" : "/");
         }
     }
 
     /** @see UploadPolicy#getApplet() */
     public JUploadApplet getApplet() {
         return this.applet;
     }
 
     /** @see UploadPolicy#getDebugLevel() */
     public int getDebugLevel() {
         return this.debugLevel;
     }
 
     /** @see UploadPolicy#setDebugLevel(int) */
     public void setDebugLevel(int debugLevel) {
         setDebugLevel(debugLevel, true);
     }
 
     /**
      * Set the debug level.
      * 
      * @param debugLevel The new debuglevel.
      * @param displayAppletParameterList Flag. If set to true, the applet's
      *            parameters are shown.
      */
     public void setDebugLevel(int debugLevel, boolean displayAppletParameterList) {
         // If the debugLevel was previously set, we inform the user of this
         // change.
         if (this.debugLevel >= 0) {
             displayInfo("Debug level set to " + debugLevel);
         }
         this.debugLevel = debugLevel;
 
         // The status bar may become visible or hidden, depending on the current
         // debug level.
         if (getApplet().getUploadPanel() != null) {
             getApplet().getUploadPanel().showOrHideStatusBar();
 
             // Let's display the current applet parameters.
             if (displayAppletParameterList) {
                 displayParameterStatus();
             }
         }
     }
 
     /**
      * This method sets the current language to take into account. It loads the
      * {@link #resourceBundle}, which will allow the applet to display the
      * texts in the correct language.
      * 
      * @param lang The new language to take into account. See the
      *            java.util.Locale(String) constructor for a list of valid
      *            values.
      */
     protected void setLang(String lang) {
         Locale locale;
         this.lang = lang;
         if (lang == null) {
             displayInfo("lang = null, taking default language");
             locale = Locale.getDefault();
         } else {
             // If we have a 5 characters lang string, then it should look like
             // ll_CC, where ll is the language code
             // and CC is the Contry code.
             if (lang.length() == 5
                     && (lang.substring(2, 3).equals("_") || lang
                             .substring(2, 3).equals("-"))) {
                 String language = lang.substring(0, 2);
                 String country = lang.substring(3, 5);
                 displayDebug("setLang - language read: " + language, 50);
                 displayDebug("setLang - country read: " + country, 50);
                 locale = new Locale(language, country.toUpperCase());
             } else {
                 locale = new Locale(lang);
             }
         }
         this.resourceBundle = ResourceBundle.getBundle(
                 "wjhk.jupload2.lang.lang", locale);
     }
 
     protected String getLookAndFeel() {
         return this.lookAndFeel;
     }
 
     /** @param lookAndFeel the lookAndFeel to set */
     protected void setLookAndFeel(String lookAndFeel) {
         this.lookAndFeel = lookAndFeel;
         if (lookAndFeel != null && !lookAndFeel.equals("")
                 && !lookAndFeel.equals("java")) {
             // We try to call the UIManager.setLookAndFeel() method. We catch
             // all possible exceptions, to prevent
             // that the applet is blocked.
             try {
                 if (!lookAndFeel.equals("system")) {
                     // Correction given by Fritz. Thanks to him.
                     UIManager.setLookAndFeel(lookAndFeel);
                 } else {
                     UIManager.setLookAndFeel(UIManager
                             .getSystemLookAndFeelClassName());
                 }
             } catch (Exception e) {
                 displayErr(e);
             }
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getMaxChunkSize() */
     public long getMaxChunkSize() {
         return this.maxChunkSize;
     }
 
     /** @param maxChunkSize the maxChunkSize to set */
     protected void setMaxChunkSize(long maxChunkSize) {
         if (maxChunkSize < 0) {
             displayDebug(
                     "maxChunkSize<0 which is invalid. Switched to the default value (Long.MAX_VALUE)",
                     1);
             maxChunkSize = Long.MAX_VALUE;
         }
         this.maxChunkSize = maxChunkSize;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getMaxFileSize() */
     public long getMaxFileSize() {
         return this.maxFileSize;
     }
 
     /** @param maxFileSize the maxFileSize to set */
     protected void setMaxFileSize(long maxFileSize) {
         if (maxFileSize < 0) {
             displayDebug(
                     "maxFileSize<0 which is invalid. Switched to the default value (Long.MAX_VALUE)",
                     1);
             maxFileSize = Long.MAX_VALUE;
         }
         this.maxFileSize = maxFileSize;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getNbFilesPerRequest() */
     public int getNbFilesPerRequest() {
         return this.nbFilesPerRequest;
     }
 
     /** @param nbFilesPerRequest the nbFilesPerRequest to set */
     protected void setNbFilesPerRequest(int nbFilesPerRequest) {
         if (nbFilesPerRequest < 0) {
             displayDebug(
                     "nbFilesPerRequest<0 which is invalid. Switched to the default value (Integer.MAX_VALUE)",
                     1);
             nbFilesPerRequest = Integer.MAX_VALUE;
         }
         this.nbFilesPerRequest = nbFilesPerRequest;
     }
 
     /** @see UploadPolicy#getFilenameEncoding() */
     public String getFilenameEncoding() {
         return this.filenameEncoding;
     }
 
     /** @param filenameEncoding the filenameEncoding to set */
     protected void setFilenameEncoding(String filenameEncoding) {
         this.filenameEncoding = filenameEncoding;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getPostURL() */
     public String getPostURL() {
         return this.postURL;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#setPostURL(String) */
     public void setPostURL(String postURL) {
         this.postURL = postURL;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
     public String getServerProtocol() {
         return this.serverProtocol;
     }
 
     /** @param serverProtocol the serverProtocol to set */
     protected void setServerProtocol(String serverProtocol) {
         this.serverProtocol = serverProtocol;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
     public boolean getShowStatusBar() {
         return this.showStatusBar;
     }
 
     /** @param showStatusBar the new showStatusBar value */
     protected void setShowStatusBar(boolean showStatusBar) {
         this.showStatusBar = showStatusBar;
         // The status bar may become visible or hidden, depending on this
         // parameter.
         if (getApplet().getUploadPanel() != null) {
             getApplet().getUploadPanel().showOrHideStatusBar();
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadSuccess() */
     public String getStringUploadSuccess() {
         return this.stringUploadSuccess;
     }
 
     /** @param stringUploadSuccess the stringUploadSuccess to set */
     protected void setStringUploadSuccess(String stringUploadSuccess) {
         this.stringUploadSuccess = stringUploadSuccess;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getUrlToSendErrorTo() */
     public String getUrlToSendErrorTo() {
         return this.urlToSendErrorTo;
     }
 
     /** @param urlToSendErrorTo the urlToSendErrorTo to set */
     protected void setUrlToSendErrorTo(String urlToSendErrorTo) {
         this.urlToSendErrorTo = urlToSendErrorTo;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// Internal methods
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This methods allows the applet to store all messages (debug, warning,
      * info, errors...) into a StringBuffer. If any problem occurs, the whole
      * output (displayed or not by the displayDebug, for instance) can be stored
      * in a file, or sent to the webmaster. This can help to identify and
      * correct problems that can occurs on the various computer configurations.
      * 
      * @param msg
      */
     private void addMsgToDebugBufferString(String msg) {
         this.debugBufferString.append(msg);
     }
 
     private final String timestamp(String tag, String s) {
         final String stamp = new SimpleDateFormat("HH:mm:ss.SSS ")
                 .format(new Date())
                 + tag;
         final boolean endsLF = s.endsWith("\n");
         if (endsLF)
             s = s.substring(0, s.length() - 1);
         return (stamp + s.replaceAll("\n", "\n" + stamp) + (endsLF ? "\n" : ""));
     }
 
     /**
      * Displays a message. If the statusArea panel is set, the message is
      * displayed on it. If not, the System.out.println function is used.
      * 
      * @param msg The message to display.
      */
     private final void displayMsg(String tag, String msg) {
         msg = timestamp(tag, msg);
         if (this.statusArea == null) {
             System.out.println(msg);
         } else {
             this.statusArea.append(msg);
             if (!msg.endsWith("\n"))
                 this.statusArea.append("\n");
         }
         // Let's store all text in the debug BufferString
         addMsgToDebugBufferString(msg + "\n");
         if (!msg.endsWith("\n"))
             addMsgToDebugBufferString("\n");
     }
 
     /**
      * This method returns the response for the
      * {@link JUploadFileFilter#accept(File)} which just calls this method. This
      * method checks that the file extension corresponds to the
      * allowedFileExtensions applet parameter.
      * 
      * @see UploadPolicy#fileFilterAccept(File)
      */
     public boolean fileFilterAccept(File file) {
         if (file.isDirectory()) {
             return true;
         } else if (this.allowedFileExtensions == null
                 || this.allowedFileExtensions.equals("")) {
             return true;
         } else {
             // Get the file extension
             String filename = file.getName();
             int point = filename.lastIndexOf('.');
             // Do our filename ends with a point ?
             if (point == filename.length() - 1) {
                 return false;
             }
             String extension = filename.substring(point + 1).toLowerCase();
             // allowedFileExtensions is :
             // - a list of file extensions,
             // - in lower case,
             // - separated by slash
             // - A slash has been added at the beginning in
             // setAllowedFileExtensions
             // - A slash has been added at the end in setAllowedFileExtensions
             // So, we just look for the /ext/ string in the stored
             // allowedFileExtensions.
             return (this.allowedFileExtensions.indexOf("/" + extension + "/")) >= 0;
         }
     }
 
     /** @see UploadPolicy#fileFilterGetDescription() */
     public String fileFilterGetDescription() {
         if (this.allowedFileExtensions == null
                 || this.allowedFileExtensions.equals(""))
             return null;
 
         return "JUpload file filter (" + this.allowedFileExtensions + ")";
     }
 
     /**
      * Returns null: the default icon is used.
      * 
      * @see UploadPolicy#fileViewGetIcon(File)
      */
     public Icon fileViewGetIcon(@SuppressWarnings("unused")
     File file) {
         return null;
     }
 
 }
