 //
 // $Id: DefaultUploadPolicy.java 289 2007-06-19 10:04:46 +0000 (mar., 19 juin
 // 2007) etienne_sf $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: 2006-05-04
 // Creator: etienne_sf
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
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.GridLayout;
 import java.awt.SystemColor;
 import java.awt.dnd.DropTargetDropEvent;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
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
 import java.util.regex.PatternSyntaxException;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.text.BadLocationException;
 
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 import wjhk.jupload2.JUploadApplet;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionStopAddingFiles;
 import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.filedata.DefaultFileData;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.gui.JUploadFileChooser;
 import wjhk.jupload2.gui.JUploadFileFilter;
 import wjhk.jupload2.gui.JUploadPanel;
 import wjhk.jupload2.gui.JUploadTextArea;
 import wjhk.jupload2.upload.helper.ByteArrayEncoder;
 import wjhk.jupload2.upload.helper.ByteArrayEncoderHTTP;
 import wjhk.jupload2.upload.helper.HTTPConnectionHelper;
 import wjhk.jupload2.upload.helper.HttpConnect;
 import wjhk.jupload2.upload.helper.InteractiveTrustManager;
 
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
  * @author etienne_sf
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
      * It also allows access to the navigator properties, if the html tag
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
      * Contains the allowedFileExtensions applet parameter.
      */
     private boolean allowHttpPersistent = UploadPolicy.DEFAULT_ALLOW_HTTP_PERSISTENT;
 
     /**
      * Contains the allowedFileExtensions applet parameter.
      */
     private String allowedFileExtensions = UploadPolicy.DEFAULT_ALLOWED_FILE_EXTENSIONS;
 
     /**
      * Indicate whether the log window is shown or not to the user. In all cases
      * it remains in memory, and stores all debug information. This allows a log
      * information, in case of an error occurs.
      * 
      * @see #urlToSendErrorTo
      */
     private String showLogWindow = UploadPolicy.DEFAULT_SHOW_LOGWINDOW;
 
     private boolean showStatusbar = UploadPolicy.DEFAULT_SHOW_STATUSBAR;
 
     private String specificHeaders = null;
 
     /** Indicates the directory in which the file chooser is to be opened */
     private File currentBrowsingDirectory = null;
 
     /**
      * This parameter controls whether the applet generates a debug file or not.
      * If true, this file contains the full debug output, whatever the current
      * debugLevel is.
      */
     private boolean debugGenerateFile = false;
 
     /**
      * The current debug level. This control the details of information that is
      * written in the log part of the applet.
      */
     private int debugLevel = UploadPolicy.DEFAULT_DEBUG_LEVEL;
 
     /**
      * Stored value for the fileChooserIconFromFileContent applet property.
      * 
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT
      */
     private int fileChooserIconFromFileContent = UploadPolicy.DEFAULT_FILE_CHOOSER_ICON_FROM_FILE_CONTENT;
 
     /**
      * Stored value for the fileChooserIconSize applet property.
      * 
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_SIZE
      */
     private int fileChooserIconSize = UploadPolicy.DEFAULT_FILE_CHOOSER_ICON_SIZE;
 
     /**
      * This String contains the filenameEncoding parameter. All details about
      * the available applet parameters are displayed in the <a
      * href="UploadPolicy.html@parameters">Upload Policy javadoc page</a>.
      */
     private String filenameEncoding = UploadPolicy.DEFAULT_FILENAME_ENCODING;
 
     /** Default value for the ftpCreateDirectoryStructure applet parameter */
     private boolean ftpCreateDirectoryStructure = UploadPolicy.DEFAULT_FTP_CREATE_DIRECTORY_STRUCTURE;
 
     /** Default value for the ftpCreateDirectoryStructure applet parameter */
     private boolean ftpTransfertBinary = UploadPolicy.DEFAULT_FTP_TRANSFERT_BINARY;
 
     /** Default value for the ftpCreateDirectoryStructure applet parameter */
     private boolean ftpTransfertPassive = UploadPolicy.DEFAULT_FTP_TRANSFERT_PASSIVE;
 
     /**
      * The lang parameter, given to the applet.
      */
     private String lang = UploadPolicy.DEFAULT_LANG;
 
     /**
      * Contains the last exception that occurs in the applet.
      * 
      * @see #displayErr(String, Exception)
      */
     private JUploadException lastException = null;
 
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
      * Current value (or default value) of the maxChunkSize applet parameter. <BR>
      * Default : Long.MAX_VALUE
      */
     private long maxChunkSize = UploadPolicy.DEFAULT_MAX_CHUNK_SIZE;
 
     /**
      * Current value (or default value) of the maxFileSize applet parameter. <BR>
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
      * @see UploadPolicy#getStringUploadError()
      */
     private String stringUploadError = UploadPolicy.DEFAULT_STRING_UPLOAD_ERROR;
 
     /**
      * @see UploadPolicy#getStringUploadSuccess()
      */
     private String stringUploadSuccess = UploadPolicy.DEFAULT_STRING_UPLOAD_SUCCESS;
 
     /**
      * @see UploadPolicy#getStringUploadWarning()
      */
     private String stringUploadWarning = UploadPolicy.DEFAULT_STRING_UPLOAD_WARNING;
 
     /**
      * If an error occurs during upload, and this attribute is not null, the
      * applet asks the user if wants to send the debug ouput to the
      * administrator. If yes, the full debug information is POSTed to this URL.
      * It's a little development on the server side to send a mail to the
      * webmaster, or just log this error into a log file.
      * 
      * @see UploadPolicy#sendDebugInformation(String, Exception)
      */
     private String urlToSendErrorTo = UploadPolicy.DEFAULT_URL_TO_SEND_ERROR_TO;
 
     /**
      * Optional name of a form (in the same document like the applet) which is
      * used to populate POST parameters.
      */
     private String formData = UploadPolicy.DEFAULT_FORMDATA;
 
     private String afterUploadTarget = UploadPolicy.DEFAULT_AFTER_UPLOAD_TARGET;
 
     private String lastResponseBody = null;
 
     private String lastResponseMessage = null;
 
     private int sslVerifyCert = InteractiveTrustManager.NONE;
 
     private final String CRLF = System.getProperty("line.separator");
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// INTERNAL ATTRIBUTE
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This Vector contains headers that will be added for each upload. It may
      * contains specific cookies, for instance.
      * 
      * @see #onAppendHeader(ByteArrayEncoder)
      */
     private Vector<String> headers = new Vector<String>();
 
     /**
      * The text area, where message are to be displayed.
      * 
      * @see #displayMsg(String, String)
      */
     private JUploadTextArea logWindow = null;
 
     /**
      * The resourceBundle contains all localized String (and others ??)
      */
     private ResourceBundle resourceBundle = null;
 
     /**
      * This stream is used to store all information that could be useful, in
      * case a problem occurs. Is content can then be sent to the webmaster.
      */
     protected PrintStream debugOut = null;
 
     /**
      * The actual file, used for the debug log.
      * 
      * @see #debugGenerateFile
      */
     protected File debugFile = null;
 
     /**
      * This flag prevents endless repeats of opening the debug log, if that
      * failed for some reason.
      */
     protected boolean debugOk = true;
 
     /** cookie is the value of the javascript <I>document.cookie</I> property. */
     protected String cookie = null;
 
     /**
      * userAgent is the value of the javascript <I>navigator.userAgent</I>
      * property. Protected as there is no setter for it, and no other way to
      * update it.
      */
     protected String userAgent = null;
 
     /**
      * This constant defines the upper limit of lines, kept in the log window.
      */
     private final static int MAX_DEBUG_LINES = 10000;
 
     /**
      * Same as {@link #patternSuccess}, but for the error message. If found,
      * then the upload was accepted by the remote HTTP server, but rejected by
      * the remote application. This pattern should also find the error message
      * in the first matching string.
      */
     protected Pattern patternError = Pattern
             .compile(UploadPolicy.DEFAULT_STRING_UPLOAD_ERROR);
 
     /**
      * The regexp pattern that is used to find the success string in the HTTP
      * response. If found, the upload is considered to be a success: it has been
      * accepted by the remote server and the remote application.
      */
     protected Pattern patternSuccess = Pattern
             .compile(UploadPolicy.DEFAULT_STRING_UPLOAD_SUCCESS);
 
     /**
      * Same as {@link #patternSuccess}, but for the warning message. Each time
      * it is found, a message is displayed to the user.
      */
     protected Pattern patternWarning = Pattern
             .compile(UploadPolicy.DEFAULT_STRING_UPLOAD_WARNING);
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// CONSTRUCTORS
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The main constructor : use default values, and the given postURL.
      * 
      * @param theApplet The current applet. As the reference to the current
      *            upload policy exists almost everywhere, this parameter allows
      *            any access to anyone on the applet... including reading the
      *            applet parameters.
      * @throws JUploadException If an applet parameter is invalid
      */
     public DefaultUploadPolicy(JUploadApplet theApplet) throws JUploadException {
         // Call default constructor for all default initialization;.
         this.applet = theApplet;
         this.logWindow = theApplet.getLogWindow();
 
         // get the debug level. This control the level of debug messages that
         // are written in the log window (see displayDebugMessage). In all
         // cases, the full output is written in the debugBufferString (see also
         // urlToSendErrorTo)
         setDebugLevel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL, this), false);
 
         // Get resource file. This must be the very first parameter to be set,
         // because during initialization, translations may be needed.
         setLang(UploadPolicyFactory.getParameter(theApplet, PROP_LANG,
                 DEFAULT_LANG, this));
 
         // Force the look and feel of the current system. This must be the
         // second
         // first parameter to be set, because during initialization, dialogs can
         // appear.
         setLookAndFeel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL, this));
 
         // This must be set before any URL's because these might trigger an
         // connection attempt.
         setSslVerifyCert(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SSL_VERIFY_CERT, DEFAULT_SSL_VERIFY_CERT, this));
 
         // get the afterUploadURL applet parameter.
         setAfterUploadURL(UploadPolicyFactory.getParameter(theApplet,
                 PROP_AFTER_UPLOAD_URL, DEFAULT_AFTER_UPLOAD_URL, this));
 
         // Whether or not to create subfolders on the server side.
         setFtpCreateDirectoryStructure(UploadPolicyFactory.getParameter(
                 theApplet, PROP_FTP_CREATE_DIRECTORY_STRUCTURE,
                 DEFAULT_FTP_CREATE_DIRECTORY_STRUCTURE, this));
         // Whether or not to create subfolders on the server side.
         setFtpTransfertBinary(UploadPolicyFactory.getParameter(theApplet,
                 PROP_FTP_TRANSFERT_BINARY, DEFAULT_FTP_TRANSFERT_BINARY, this));
         // Whether or not to create subfolders on the server side.
         setFtpTransfertPassive(UploadPolicyFactory
                 .getParameter(theApplet, PROP_FTP_TRANSFERT_PASSIVE,
                         DEFAULT_FTP_TRANSFERT_PASSIVE, this));
 
         // get the allowedFileExtensions applet parameter
         setAllowedFileExtensions(UploadPolicyFactory.getParameter(theApplet,
                 PROP_ALLOWED_FILE_EXTENSIONS, DEFAULT_ALLOWED_FILE_EXTENSIONS,
                 this));
 
         setAllowHttpPersistent(UploadPolicyFactory
                 .getParameter(theApplet, PROP_ALLOW_HTTP_PERSISTENT,
                         DEFAULT_ALLOW_HTTP_PERSISTENT, this));
 
         setShowStatusbar(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SHOW_STATUSBAR, DEFAULT_SHOW_STATUSBAR, this));
 
         setShowLogWindow(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SHOW_LOGWINDOW, DEFAULT_SHOW_LOGWINDOW, this));
 
         // set the fileChooser relative stuff.
         setFileChooserIconFromFileContent(UploadPolicyFactory.getParameter(
                 theApplet, PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT,
                 DEFAULT_FILE_CHOOSER_ICON_FROM_FILE_CONTENT, this));
         setFileChooserIconSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_FILE_CHOOSER_ICON_SIZE, DEFAULT_FILE_CHOOSER_ICON_SIZE,
                 this));
         setCurrentBrowsingDirectory(UploadPolicyFactory.getParameter(theApplet,
                 PROP_BROWSING_DIRECTORY, DEFAULT_BROWSING_DIRECTORY, this));
         // get the filenameEncoding. If not null, it should be a valid argument
         // for the URLEncoder.encode method.
         // DEPRECATED.
         setFilenameEncoding(UploadPolicyFactory.getParameter(theApplet,
                 PROP_FILENAME_ENCODING, DEFAULT_FILENAME_ENCODING, this));
 
         // get the maximum number of files to upload in one HTTP request.
         setNbFilesPerRequest(UploadPolicyFactory.getParameter(theApplet,
                 PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST, this));
 
         // get the maximum size of a file on one HTTP request (indicates if the
         // file must be splitted before upload, see UploadPolicy comment).
         setMaxChunkSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_CHUNK_SIZE, DEFAULT_MAX_CHUNK_SIZE, this));
 
         // get the maximum size of an uploaded file.
         setMaxFileSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_FILE_SIZE, DEFAULT_MAX_FILE_SIZE, this));
 
         // get the URL where files must be posted.
         setPostURL(UploadPolicyFactory.getParameter(theApplet, PROP_POST_URL,
                 DEFAULT_POST_URL, this));
 
         // get any additional headers.
         setSpecificHeaders(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SPECIFIC_HEADERS, DEFAULT_SPECIFIC_HEADERS, this));
         setStringUploadError(UploadPolicyFactory.getParameter(theApplet,
                 PROP_STRING_UPLOAD_ERROR, DEFAULT_STRING_UPLOAD_ERROR, this));
         setStringUploadSuccess(UploadPolicyFactory
                 .getParameter(theApplet, PROP_STRING_UPLOAD_SUCCESS,
                         DEFAULT_STRING_UPLOAD_SUCCESS, this));
         setStringUploadWarning(UploadPolicyFactory
                 .getParameter(theApplet, PROP_STRING_UPLOAD_WARNING,
                         DEFAULT_STRING_UPLOAD_WARNING, this));
 
         // get the URL where the full debug output can be sent when an error
         // occurs.
         setUrlToSendErrorTo(UploadPolicyFactory.getParameter(theApplet,
                 PROP_URL_TO_SEND_ERROR_TO, DEFAULT_URL_TO_SEND_ERROR_TO, this));
         this.formData = UploadPolicyFactory.getParameter(theApplet,
                 PROP_FORMDATA, DEFAULT_FORMDATA, this);
         this.afterUploadTarget = UploadPolicyFactory.getParameter(theApplet,
                 PROP_AFTER_UPLOAD_TARGET, DEFAULT_AFTER_UPLOAD_TARGET, this);
 
         // /////////////////////////////////////////////////////////////////////////////
         // Load session data read from the navigator:
         // - cookies.
         // - User-Agent : useful, as the server will then see a post request
         // coming from the same navigator.
         //
         try {
             // Patch given by Stani: corrects the use of the applet on Firefox
             // on Mac.
             this.cookie = (String) JSObject.getWindow(getApplet()).eval(
                     "document.cookie");
             this.userAgent = (String) JSObject.getWindow(getApplet()).eval(
                     "navigator.userAgent");
 
             displayDebug("cookie: " + this.cookie, 30);
             displayDebug("userAgent: " + this.userAgent, 30);
         } catch (JSException e) {
             displayWarn("JSException (" + e.getClass() + ": " + e.getMessage()
                     + ") in DefaultUploadPolicy, trying default values.");
 
             // If we can't have access to the JS objects, we're in development :
             // Let's put some 'hard value', to test the applet from the
             // development tool (mine is eclipse).
 
             // felfert: I need different values so let's make that
             // configurable...
             this.cookie = System.getProperty("debug_cookie");
             this.userAgent = System.getProperty("debug_agent");
 
             displayDebug(
                     "  no navigator found, reading 'debug_cookie' from system properties ("
                             + this.cookie + ")", 30);
             displayDebug(
                     "  no navigator found, reading 'debug_agent' from system properties ("
                             + this.userAgent + ")", 30);
             /*
              * Exemple of parameter when calling the JVM:
              * -Ddebug_cookie="Cookie:cpg146_data=
              * YTo0OntzOjI6IklEIjtzOjMyOiJhZGU3MWIxZmU4OTZjNThhZjQ5N2FiY2ZiNmFlZTUzOCI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO3M6MzoibGl2IjthOjI6e2k6MDtOO2k6MTtzOjQ6IjE0ODgiO319
              * ;cpg143_data=
              * YTozOntzOjI6IklEIjtzOjMyOiI4NjhhNmQ4ZmNlY2IwMTc5YTJiNmZlMGY3YWQzNThkNSI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO30
              * %3D;
              * 8387c97d1f683b758a67a0473b586126=5ed998846fec70d6d2f73971b9cbbf0b;
              * b1d7468cf1b317c97c7c284f6bb14ff8
              * =587b82a7abb3d2aca134742b1df9acf7" -Ddebug_agent="userAgent:
              * Mozilla/5.0 (Windows; U; Windows NT 5.0; fr; rv:1.8.1.3)
              * Gecko/20070309 Firefox/2.0.0.3"
              */
         }
         // The cookies and user-agent will be added to the header sent by the
         // applet:
         if (this.cookie != null)
             addHeader("Cookie: " + this.cookie);
         if (this.userAgent != null)
             addHeader("User-Agent: " + this.userAgent);
 
         // Let's touch the server, to test that everything is Ok. Take care,
         // this is the only place where we override the default value, by null:
         // the default value will be used by the HttpConnect.getProtocol()
         // method.
         // Also, in FTP mode, there can be no default value.
         setServerProtocol(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SERVER_PROTOCOL, null, this));
 
         // We let the UploadPolicyFactory call the displayParameterStatus
         // method, so that the initialization is finished, including for classes
         // which inherit from DefaultUploadPolicy.
         displayDebug(
                 "[DefaultUploadPolicy] end of constructor (serverProtocol has been set)",
                 30);
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// UploadPolicy methods
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
      * the server. The return is tested, in the order below: <DIR> <LI>False, if
      * the stringUploadError is found. An error message is then displayed. <LI>
      * True, if the stringUploadSuccess is null or empty (no test at all). <LI>
      * True, if the stringUploadSuccess string is present in the
      * serverOutputBody. <LI>True, If previous condition is not filled, but the
      * HTTP header "HTTP(.*)200OK$" is present: the test is currently non
      * blocking, because I can not test all possible HTTP configurations.<BR>
      * <LI>False if the previous conditions are not fullfilled. </DIR>
      * 
      * <BR>
      * This method also looks for the stringUploadWarning regular expression.
      * Each time it is matched, the found message is displayed to the user.
      * 
      * @param status The HTTP response code
      * @param msg The status message from the first line of the response (e.g.
      *            "200 OK").
      * @param body The body of the HTTP answer.
      * @return True or False, indicating if the upload is a success or not.
      * @see UploadPolicy#checkUploadSuccess(int, String, String)
      */
     public boolean checkUploadSuccess(int status, String msg, String body)
             throws JUploadException {
         boolean bReturn = false;
 
         if (getDebugLevel() > 100) {
             // Let's have a little time to check the upload messages written on
             // the progress bar.
             try {
                 Thread.sleep(300);
             } catch (InterruptedException e) {
             }
         }
 
         this.lastResponseBody = body;
         this.lastResponseMessage = msg;
         displayDebug("HTTP status: " + msg, 30);
         // HTTP-100 correction, thanks to Marc Reidy
         if ((status != 200) && (status != 100))
             throw new JUploadExceptionUploadFailed("Received HTTP status "
                     + msg);
 
         // Let's analyze the body returned, line by line. The end of line
         // character may be CR, LF, or CRLF. We navigate through the body, and
         // replace any end of line character by a uniform CRLF.
         Matcher matcherError, matcherWarning;
         String line;
         Pattern p = Pattern.compile("[\\r\\n]", Pattern.MULTILINE);
         String[] lines = p.split(body);
         StringBuffer sbBodyWithUniformCRLF = new StringBuffer(body.length());
         for (int i = 0; i < lines.length; i += 1) {
             line = lines[i];
             sbBodyWithUniformCRLF.append(line).append("\r\n");
 
             if (line == null || line.equals("")) {
                 // An empty line. Let's go the next line.
                 continue;
             }
 
             // Check if this is a success
             // The success string should be in the http body
             if (getStringUploadSuccess() != null
                     && !getStringUploadSuccess().equals("")) {
                 if (this.patternSuccess.matcher(line).matches()) {
                     // We go on. There may be some WARNING message, hereafter.
                     bReturn = true;
                 }
             }
 
             // Check if this is an error
             if (getStringUploadError() != null
                     && !getStringUploadError().equals("")) {
                 matcherError = this.patternError.matcher(line);
                 if (matcherError.matches()) {
                     String errmsg = "An error occurs during upload (but the applet couldn't find the error message)";
                     if (matcherError.groupCount() > 0) {
                         if (!matcherError.group(1).equals("")) {
                             // Let's do a (very simple) formatting: one line to
                             // 100 characters
                             errmsg = matcherError.group(1);
                             int maxLineLength = 80;
                             StringBuffer sbErrMsg = new StringBuffer();
                             for (int j = 0, remaining = errmsg.length(); remaining > 0; remaining -= maxLineLength, j += 1) {
                                 if (remaining <= maxLineLength) {
                                     // It's the last loop.
                                     sbErrMsg.append(errmsg.substring(j
                                             * maxLineLength));
                                 } else {
                                     sbErrMsg.append(errmsg.substring(j
                                             * maxLineLength, (j + 1)
                                             * maxLineLength));
                                 }
                                 sbErrMsg.append("\n");
                             }
                             errmsg = sbErrMsg.toString();
                         }
                     }
                     this.lastResponseMessage = errmsg;
                     throw new JUploadExceptionUploadFailed(errmsg);
                 }
             }// getStringUploadError
 
             // Check if this is an warning
             if (getStringUploadWarning() != null
                     && !getStringUploadWarning().equals("")) {
                 matcherWarning = this.patternWarning.matcher(line);
                 if (matcherWarning.matches()) {
                     String warnmsg = "A warning occurs during upload (but the applet couldn't find the warning message)";
                     if (matcherWarning.groupCount() > 0) {
                         if (!matcherWarning.group(1).equals("")) {
                             warnmsg = matcherWarning.group(1);
                         }
                     }
                     this.lastResponseMessage = warnmsg;
                     displayWarn(warnmsg);
                     alertStr(warnmsg);
                 }
             }// getStringUploadWarning
 
         }// while(st.hasMoreTokens())
 
         if (bReturn) {
             return true;
         }
 
         // We found no stringUploadSuccess nor stringUploadError
         if (getStringUploadSuccess() == null
                 || getStringUploadSuccess().equals("")) {
             // No chance to check the correctness of this upload. -> Assume Ok
             return true;
         }
 
         // stringUploadSuccess was defined but we did not find it.
         // This is most certainly an error as http-status 200 does *not* refer
         // to the correctness of the content. It merely means that the protocol
         // handling was ok. -> throw an exception
         throw new JUploadExceptionUploadFailed(getClass().getName()
                 + ".checkUploadSuccess(): The string \""
                 + this.stringUploadSuccess
                 + "\" was not found in the response body");
     } // checkUploadSuccess
 
     /**
      * Generate a js String, that can be written in a javascript expression.
      * It's up to the caller to put the starting and ending quotes. The double
      * quotes are replaced by simple quotes (to let simple quotes unchanged, as
      * it may be used in common language). Thus, start and end of JS string
      * should be with double quotes, when using the return of this function.
      * 
      * @param s
      * @return The transformed string, that can be written in the output, into a
      *         javascript string. It doesn't contain the starting and ending
      *         double quotes.
      */
     private String jsString(String s) {
         String dollarReplacement = Matcher.quoteReplacement("\\$");
         String singleQuoteReplacement = Matcher.quoteReplacement("\\'");
         String linefeedReplacement = Matcher.quoteReplacement("\\n");
 
         if (s == null || s.equals("")) {
             displayDebug("jsString (nothing to do, s is an empty string)", 100);
             return "";
         } else {
             displayDebug("jsString:", 100);
             displayDebug("  original string:" + s, 100);
             s = s.replaceAll("\\$", dollarReplacement);
             displayDebug("  after dollar changed:" + s, 100);
             s = s.replaceAll("\"", "'");
             displayDebug("  after double quote changed:" + s, 100);
             s = s.replaceAll("'", singleQuoteReplacement);
             displayDebug("  after single quote changed:" + s, 100);
             s = s.replaceAll("\n", linefeedReplacement);
             displayDebug("  after LF changed:" + s, 100);
             s = s.replaceAll("\r", "");
             displayDebug("  after CR changed:" + s, 100);
             return s;
         }
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
      */
     public void afterUpload(Exception e, String serverOutput)
             throws JUploadException {
         // If there was no error, and afterUploadURL is defined, let's try to go
         // to this URL.
         String url = getAfterUploadURL();
         if (url != null) {
             try {
                 if (url.toLowerCase().startsWith("javascript:")) {
                     // A JavaScript expression was specified. Execute it.
                     String expr = url.substring(11);
 
                     // Replacement of %msg%. Will do something only if the %msg%
                     // string exists in expr.
                     expr = expr
                             .replaceAll(
                                     "%msg%",
                                     Matcher
                                             .quoteReplacement(jsString(getLastResponseMessage())));
 
                     // Replacement of %body%. Will do something only if the
                     // %body% string exists in expr.
                     expr = expr.replaceAll("%body%", Matcher
                             .quoteReplacement(jsString(getLastResponseBody())));
 
                     // Replacement of %success%. Will do something only if the
                     // %success% string exists in expr.
                     expr = expr.replaceAll("%success%", Matcher
                             .quoteReplacement((null == e) ? "true" : "false"));
 
                     displayDebug("Calling javascript expression: " + expr, 80);
                     JSObject.getWindow(getApplet()).eval(expr);
                 } else if (null == e) {
                     // This is not a javascript URL: we change the current page
                     // only if no error occured.
                     String target = getAfterUploadTarget();
                     if (getDebugLevel() >= 100) {
                         alertStr("No switch to getAfterUploadURL, because debug level is "
                                 + getDebugLevel() + " (>=100)");
                     } else {
                         // Let's change the current URL to edit names and
                         // comments, for the selected album. Ok, let's go and
                         // add names and comments to the newly updated pictures.
                         getApplet().getAppletContext().showDocument(
                                 new URL(url),
                                 (null == target) ? "_self" : target);
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
 
     /**
      * The DefaultUpload accepts all file types: we just return an instance of
      * FileData, without any test.
      * 
      * @see UploadPolicy#createFileData(File, File)
      */
     public FileData createFileData(File file, File root)
             throws JUploadExceptionStopAddingFiles {
         if (!fileFilterAccept(file)) {
             String msg = file.getName() + " : "
                     + getString("errForbiddenExtension");
             displayWarn(msg);
             if (JOptionPane.showConfirmDialog(null, msg, "alert",
                     JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                 // The user want to stop to add files to the list. For instance,
                 // when he/she added a whole directory, and it contains a lot of
                 // files that don't match the allowed file extension.
                 throw new JUploadExceptionStopAddingFiles("Stopped by the user");
             }
             return null;
         } else if (!file.canRead()) {
             displayInfo("Can't read file " + file.getName()
                     + ". No DefaultFileData creation.");
             return null;
         } else {
             return new DefaultFileData(file, root, this);
         }
     }
 
     /**
      * Default implementation of
      * {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JUploadPanel)}
      * . IT creates a JPanel, containing the three given JButton. It creates the
      * same panel as the original JUpload.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton,
      *      JButton, JUploadPanel)
      */
     public JPanel createTopPanel(JButton browse, JButton remove,
             JButton removeAll, JUploadPanel jUploadPanel) {
         JPanel jPanel = new JPanel();
 
         jPanel.setLayout(new GridLayout(1, 3, 10, 5));
         jPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
         jPanel.add(browse);
         jPanel.add(removeAll);
         jPanel.add(remove);
 
         jUploadPanel.setBorder(BorderFactory
                 .createLineBorder(SystemColor.controlDkShadow));
 
         return jPanel;
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#createProgressPanel(JProgressBar,
      *      JProgressBar, JButton, JButton, JPanel)
      */
     public JPanel createProgressPanel(JProgressBar preparationProgressBar,
             JProgressBar uploadProgressBar, JButton uploadButton,
             JButton stopButton, JPanel mainPanel) {
 
         // There may be two progress bar: one for preparation progress of files
         // (preparation before upload) and one to follow the actual upload.
         JPanel jProgressBarPanel = new JPanel();
         jProgressBarPanel.setLayout(new BorderLayout(10, 1));
         jProgressBarPanel.add(preparationProgressBar, BorderLayout.NORTH);
         jProgressBarPanel.add(uploadProgressBar, BorderLayout.SOUTH);
 
         JPanel jProgressPanel = new JPanel();
         jProgressPanel.setLayout(new BorderLayout(10, 0));
         jProgressPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
         jProgressPanel.add(uploadButton, BorderLayout.LINE_START);
         jProgressPanel.add(jProgressBarPanel, BorderLayout.CENTER);
         jProgressPanel.add(stopButton, BorderLayout.LINE_END);
         return jProgressPanel;
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#createStatusBar(javax.swing.JLabel,
      *      javax.swing.JPanel)
      */
     public JPanel createStatusBar(JLabel content, JPanel mainPanel) {
         if (this.showStatusbar) {
             JPanel pstatus = new JPanel();
             pstatus.setLayout(new BorderLayout());
             pstatus.add(content, BorderLayout.CENTER);
             pstatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
             return pstatus;
         }
         return null;
     }
 
     /**
      * This methods allow the upload policy to override the default disposition
      * of the components on the applet.
      * 
      * @see UploadPolicy#addComponentsToJUploadPanel(JUploadPanel)
      */
     public void addComponentsToJUploadPanel(JUploadPanel jUploadPanel) {
         // Set the global layout of the panel.
         jUploadPanel.setLayout(new BoxLayout(jUploadPanel, BoxLayout.Y_AXIS));
 
         // The top panel is the upper part of the applet: above the file list.
         // JPanel topPanel = new JPanel();
         JPanel topPanel = createTopPanel(jUploadPanel.getBrowseButton(),
                 jUploadPanel.getRemoveButton(), jUploadPanel
                         .getRemoveAllButton(), jUploadPanel);
         if (topPanel != null) {
             jUploadPanel.add(topPanel);
             topPanel.addMouseListener(jUploadPanel);
         }
 
         // Then, we add the file list.
         jUploadPanel.add(jUploadPanel.getFilePanel().getDropComponent());
 
         // The progress panel contains the progress bar, and the upload and stop
         // buttons.
         JPanel progressPanel = createProgressPanel(jUploadPanel
                 .getPreparationProgressBar(), jUploadPanel
                 .getUploadProgressBar(), jUploadPanel.getUploadButton(),
                 jUploadPanel.getStopButton(), jUploadPanel);
         jUploadPanel.add(progressPanel);
         jUploadPanel.addMouseListener(jUploadPanel);
 
         // Now, we add the log window.
         jUploadPanel.showOrHideLogWindow();
         jUploadPanel.add(jUploadPanel.getJLogWindowPane());
 
         // And, to finish with: the status bar.
         JPanel p = createStatusBar(jUploadPanel.getStatusLabel(), jUploadPanel);
         if (null != p) {
             jUploadPanel.add(p);
             p.addMouseListener(jUploadPanel);
         }
     }
 
     /** @see UploadPolicy#displayErr(Exception) */
     public void displayErr(Exception e) {
         displayErr(e.getMessage(), e);
     }
 
     /** @see UploadPolicy#displayErr(String) */
     public void displayErr(String err) {
         displayErr(err, null);
     }
 
     /**
      * Logs a stack trace for the given exception.
      * 
      * @param throwable
      */
     private void displayStackTrace(Throwable throwable) {
         if (throwable != null) {
             ByteArrayOutputStream bs = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(bs);
             throwable.printStackTrace(ps);
             ps.close();
             displayMsg("", bs.toString());
 
             // If there is a cause, let's be sure its stack trace is displayed.
             if (throwable.getCause() != null) {
                 displayMsg("", "Caused by:");
                 displayStackTrace(throwable.getCause());
             }
         }
     }
 
     /**
      * If debug is off, the log window may not be visible. We switch the debug
      * to on, to be sure that some information will be displayed to the user. <BR>
      * If debug is -1, the log window remains hidden.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#displayErr(java.lang.String,
      *      java.lang.Exception)
      */
     public void displayErr(String errorText, Exception exception) {
 
         if (exception == null) {
             setLastException(new JUploadException("errorText"));
         } else if (exception instanceof JUploadException) {
             setLastException((JUploadException) exception);
         } else {
             setLastException(new JUploadException(exception));
         }
 
         // Default behavior: if debugLevel is 0, and an error occurs, we force
         // the debug level to 1: this makes the log window become visible, if it
         // was hidden.
         if (getDebugLevel() == 0)
             setDebugLevel(1);
 
         String exceptionMsg = null;
         String exceptionClassName = null;
         String alertMsg = errorText;
         String logMsg = errorText;
 
         // First, we construct the exception class name.
         if (exception == null) {
             exceptionClassName = "";
         } else if (exception instanceof JUploadException) {
             exceptionClassName = "["
                     + ((JUploadException) exception).getClassNameAndClause()
                     + "] ";
         } else {
             exceptionClassName = "[" + exception.getClass().getName() + "] ";
         }
 
         // Then, the message body can be completed by the exception message.
         if (exception != null) {
             // Ok, we have an exception.
             if (exception.getCause() != null) {
                 exceptionMsg = exception.getCause().getMessage();
             } else {
                 exceptionMsg = exception.getMessage();
             }
             if (errorText == null || errorText.equals("")) {
                 alertMsg = "Unknown error (" + exceptionMsg + ")";
             }
             logMsg = exceptionMsg + " (" + errorText + ")";
         }
 
         // Add the message to the log window
         displayMsg("[ERROR] ", exceptionClassName + logMsg);
         // Let's display the stack trace, if relevant.
         displayStackTrace(exception);
 
         // Display the message to the user.
         if (getDebugLevel() >= 100) {
             // Debug has been put on (by the user or by applet configuration).
             alertStr(exceptionClassName + logMsg);
         } else {
             // Debug level may be set to 1, when an error occurs, even if debug
             // was not put on by the user.
             alertStr(alertMsg);
         }
 
         // Then we copy the debug output to the clipboard, and say it to the
         // current user.
         if (getApplet().getUploadPanel() != null && getDebugLevel() == 100) {
             // Ok, the applet has been fully built.
             getApplet().getUploadPanel().copyLogWindow();
             alert("messageLogWindowCopiedToClipboard");
         }
 
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
         } else if (this.debugGenerateFile) {
             // We have to write the message to the debug file, whatever the
             // debugLevel is.
             addMsgToDebugLog(tag + debug);
         }
     }
 
     /** @see UploadPolicy#getString(String) */
     public String getString(String key) {
         String ret = this.resourceBundle.getString(key);
         return ret;
     }
 
     /**
      * @see UploadPolicy#getUploadFilename(FileData, int)
      */
     public String getUploadFilename(FileData fileData, int index)
             throws JUploadException {
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
     public String getUploadName(FileData fileData, int index) {
         // This is the original way of working of JUpload.
         // It can easily be modified, by using another UploadPolicy.
         return "File" + index;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#isUploadReady() */
     public boolean isUploadReady() {
         // Default : nothing to do before upload, so we're ready.
         return true;
     }
 
     /** @see UploadPolicy#onAppendHeader(ByteArrayEncoder) */
     public ByteArrayEncoder onAppendHeader(ByteArrayEncoder bae)
             throws JUploadIOException {
         Iterator<String> it = this.headers.iterator();
         String header;
         while (it.hasNext()) {
             header = it.next();
             displayDebug(header, 70);
             bae.append(header).append("\r\n");
         }
         return bae;
     }// appendHeader
 
     /**
      * Default implementation of the
      * {@link wjhk.jupload2.policies.UploadPolicy#onFileSelected(wjhk.jupload2.filedata.FileData)}
      * . Nothing's done.
      */
     public void onFileSelected(FileData fileData) {
         // Default implementation : no action
     }
 
     /**
      * Default implementation of the
      * {@link wjhk.jupload2.policies.UploadPolicy#onFileDoubleClicked(FileData)}
      * . Nothing's done.
      */
     public void onFileDoubleClicked(FileData fileData) {
         // Default implementation : no action
     }
 
     /** @see UploadPolicy#sendDebugInformation(String, Exception) */
     public void sendDebugInformation(String description, Exception exception) {
         try {
            displayInfo("Sending debug information to " + getUrlToSendErrorTo());
             if (null != getUrlToSendErrorTo()) {
                 if (JOptionPane.showConfirmDialog(null,
                         getString("questionSendMailOnError"),
                         getString("Confirm"), JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                     displayDebug(
                             "[sendDebugInformation] Within response == true",
                             30);
 
                     String action = null;
                     String line;
                     HTTPConnectionHelper connectionHelper = null;
                     boolean localDebugOk = this.debugOk;
 
                     try {
                         URL url = new URL(this.urlToSendErrorTo);
                         connectionHelper = new HTTPConnectionHelper(this);
                         connectionHelper.initRequest(url, "POST", false, true);
 
                         ByteArrayEncoder baeContent = new ByteArrayEncoderHTTP(
                                 this, connectionHelper.getByteArrayEncoder()
                                         .getBoundary(), connectionHelper
                                         .getByteArrayEncoder().getEncoding());
                         // The message is written in English, as it is not sure
                         // that
                         // the webmaster speaks the same language as the current
                         // user.
                         baeContent.appendTextProperty("description",
                                 "An error occured during upload, in JUpload.");
                         String exceptionClass = null;
                         String exceptionCause = null;
                         String exceptionStackTrace = null;
                         if (exception != null) {
                             exceptionClass = exception.getClass().getName();
                             if (exception.getCause() != null) {
                                 exceptionCause = exception.getCause()
                                         .getClass().getName();
                             }
                             StackTraceElement[] elements = exception
                                     .getStackTrace();
                             ByteArrayEncoderHTTP baeStackTrace = new ByteArrayEncoderHTTP(
                                     this, connectionHelper
                                             .getByteArrayEncoder()
                                             .getBoundary(), connectionHelper
                                             .getByteArrayEncoder()
                                             .getEncoding());
                             for (int i = 0; i < elements.length; i += 1) {
                                 baeStackTrace.append(" at ");
                                 baeStackTrace
                                         .append(elements[i].getClassName());
                                 baeStackTrace.append(".");
                                 baeStackTrace.append(elements[i]
                                         .getMethodName());
                                 baeStackTrace.append("() [line ");
                                 baeStackTrace.append(Integer
                                         .toString(elements[i].getLineNumber()));
                                 baeStackTrace.append("]\r\n");
                             }
                             baeStackTrace.close();
                             exceptionStackTrace = baeStackTrace.getString();
                         }
                         baeContent.appendTextProperty("exceptionClass",
                                 exceptionClass);
                         baeContent.appendTextProperty("exceptionCause",
                                 exceptionCause);
                         baeContent.appendTextProperty("exceptionStackTrace",
                                 exceptionStackTrace);
 
                         String baeBound = connectionHelper
                                 .getByteArrayEncoder().getBoundary();
                         String baeEncoding = connectionHelper
                                 .getByteArrayEncoder().getEncoding();
                         ByteArrayEncoder baeDebug = new ByteArrayEncoderHTTP(
                                 this, baeBound, baeEncoding);
                         if (this.debugGenerateFile) {
                             // During debug output, we need to make sure that
                             // the debug log is not changed, so we set debugOk
                             // to false temporarily. -> Everything goes to
                             // stdout.
                             action = "flush (debugGenerateFile=true)";
                             this.debugOut.flush();
                             this.debugOk = false;
                             // First, calculate the size of the strings we will
                             // send.
                             action = "read debug file (debugGenerateFile=true)";
                             BufferedReader debugIn = new BufferedReader(
                                     new FileReader(this.debugFile));
                             while ((line = debugIn.readLine()) != null) {
                                 baeDebug.append(line).append("\r\n");
                             }
                             debugIn.close();
 
                             // We are done with the debug log, so re-enable it.
                             this.debugOk = localDebugOk;
                         }// if (this.debugGenerateFile)
                         else {
                             action = "read debug file (debugGenerateFile=false)";
                             baeDebug.append(this.applet.getLogWindow()
                                     .getText());
                         }
                         action = "baeDebug.close()";
                         baeDebug.close();                        
 
                         baeContent.appendTextProperty("debugOutput", baeDebug
                                 .getString());
                         baeContent.appendEndPropertyList();
                         // The content has been built.
                         baeContent.close();
 
                         // byteArrayEncoder
                         // .append("Content-type:
                         // application/x-www-form-urlencoded\r\n");
                         action = "send request";
                         connectionHelper.append(
                                 "Content-Type: multipart/form-data; boundary=")
                                 .append(
                                         connectionHelper.getBoundary()
                                                 .substring(2)).append("\r\n");
                         connectionHelper.append("Content-length: ").append(
                                 String.valueOf(baeContent.getEncodedLength()))
                                 .append("\r\n");
 
                         // Let's send the headers (without baeDescription) ...
                         connectionHelper.sendRequest();
                         // Blank line (end of header)
                         connectionHelper.append("\r\n");
                         connectionHelper.append(baeContent);
 
                         // .. then the debug information
                         /*
                          * debugIn = new BufferedReader(new FileReader(
                          * this.debugFile)); while ((line = debugIn.readLine())
                          * != null) { baeDebug = new ByteArrayEncoderHTTP(this,
                          * baeBound, baeEncoding);
                          * baeDebug.append(line).append("\r\n");
                          * baeDebug.close(); connectionHelper.append(baeDebug);
                          * } debugIn.close();
                          */
 
                         action = "connectionHelper.readHttpResponse()";
                         int status = connectionHelper.readHttpResponse();
 
                         displayDebug(
                                 "========================================================================================",
                                 90);
                         displayDebug(
                                 "==================      sendDebugInformation [start]   =================================",
                                 90);
                         displayDebug(
                                 "========================================================================================",
                                 90);
                         displayDebug(
                                 "[sendDebugInformation] Sent to server: \r\n"
                                         + connectionHelper
                                                 .getByteArrayEncoder()
                                                 .getString(), 90);
                         displayDebug(
                                 "========================================================================================",
                                 90);
                         displayDebug(
                                 "[sendDebugInformation] Body received: \r\n"
                                         + connectionHelper.getResponseBody(),
                                 90);
                         displayDebug(
                                 "========================================================================================",
                                 90);
                         displayDebug(
                                 "==================      sendDebugInformation [end]     =================================",
                                 90);
                         displayDebug(
                                 "========================================================================================",
                                 90);
 
                         // Is our upload a success ?
                         if (!checkUploadSuccess(status, connectionHelper
                                 .getResponseMsg(), connectionHelper
                                 .getResponseBody())) {
                             throw new JUploadExceptionUploadFailed(
                                     getString("errHttpResponse"));
                         }
 
                        displayInfo("debug information sent correctly");
                     } catch (MalformedURLException e) {
                         throw new JUploadIOException(
                                 "Malformed URL Exception for "
                                         + this.urlToSendErrorTo, e);
                     } catch (Exception e) {
                         this.debugOk = localDebugOk;
                         displayErr(getString("errDuringLogManagement") + " ("
                                 + action + ")", e);
                     } finally {
                         this.debugOk = localDebugOk;
                     }
                 }
             }
         } catch (JUploadIOException e) {
             displayErr("Could not send debug information", e);
         }
     }// sendDebugInformation
 
     /**
      * This method manages all applet parameters. It allows javascript to update
      * their value, for instance after the user chooses a value in a list ...
      * 
      * @throws JUploadException
      * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
      *      java.lang.String)
      */
     public void setProperty(String prop, String value) throws JUploadException {
 
         displayDebug("[DefaultUploadPolicy] Call of setProperty: " + prop
                 + " => " + value, 30);
 
         if (prop.equals(PROP_AFTER_UPLOAD_URL)) {
             setAfterUploadURL(value);
         } else if (prop.equals(PROP_ALLOW_HTTP_PERSISTENT)) {
             setAllowHttpPersistent(Boolean.parseBoolean(value));
         } else if (prop.equals(PROP_ALLOWED_FILE_EXTENSIONS)) {
             setAllowedFileExtensions(value);
         } else if (prop.equals(PROP_DEBUG_LEVEL)) {
             setDebugLevel(UploadPolicyFactory.parseInt(value, this.debugLevel,
                     this));
         } else if (prop.equals(PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT)) {
             setFileChooserIconFromFileContent(UploadPolicyFactory.parseInt(
                     value, getFileChooserIconFromFileContent(), this));
         } else if (prop.equals(PROP_FILE_CHOOSER_ICON_SIZE)) {
             setFileChooserIconSize(UploadPolicyFactory.parseInt(value,
                     getFileChooserIconSize(), this));
         } else if (prop.equals(PROP_LANG)) {
             setLang(value);
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
         } else if (prop.equals(PROP_SSL_VERIFY_CERT)) {
             setSslVerifyCert(value);
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
         displayDebug(
                 "=======================================================================",
                 30);
         displayDebug("======= Parameters managed by DefaultUploadPolicy", 30);
         // /////////////////////////////////////////////////////////////////////////////
         // Let's display some information to the user, about the received
         // parameters.
         displayInfo("JUpload applet, version " + getApplet().VERSION
                 + " (compiled: " + getApplet().BUILD_DATE
                 + "), available at http://jupload.sourceforge.net/");
         displayDebug("Java version: " + System.getProperty("java.version"), 30);
         displayDebug("Cookie: " + this.cookie, 30);
         displayDebug("userAgent: " + this.userAgent, 30);
 
         displayDebug("List of all applet parameters:", 30);
         displayDebug("  language: "
                 + this.resourceBundle.getLocale().getLanguage(), 30);
         displayDebug("  country: "
                 + this.resourceBundle.getLocale().getCountry(), 30);
 
         displayDebug(PROP_AFTER_UPLOAD_URL + ": " + getAfterUploadURL(), 30);
         displayDebug(PROP_ALLOW_HTTP_PERSISTENT + ": "
                 + getAllowHttpPersistent(), 30);
         displayDebug(PROP_ALLOWED_FILE_EXTENSIONS + ": "
                 + getAllowedFileExtensions(), 30);
         displayDebug(PROP_BROWSING_DIRECTORY + " (current value): "
                 + getCurrentBrowsingDirectory(), 30);
         displayDebug(PROP_DEBUG_LEVEL + ": " + this.debugLevel, 1);
         if (this.debugGenerateFile) {
             displayDebug("  (debugfile: " + this.debugFile.getAbsolutePath()
                     + ")", 1);
         }
         displayDebug(PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT + ": "
                 + getFileChooserIconFromFileContent(), 30);
         displayDebug(PROP_FILE_CHOOSER_ICON_SIZE + ": "
                 + getFileChooserIconSize(), 30);
         displayDebug(PROP_FILENAME_ENCODING + ": " + getFilenameEncoding(), 30);
         displayDebug(PROP_FTP_CREATE_DIRECTORY_STRUCTURE + ": "
                 + getFtpCreateDirectoryStructure(), 30);
         displayDebug(
                 PROP_FTP_TRANSFERT_BINARY + ": " + getFtpTransfertBinary(), 30);
         displayDebug(PROP_FTP_TRANSFERT_PASSIVE + ": "
                 + getFtpTransfertPassive(), 30);
         displayDebug("lang: " + this.lang, 30);
         displayDebug(PROP_MAX_CHUNK_SIZE + ": " + getMaxChunkSize(), 30);
         if (this.maxFileSize == Long.MAX_VALUE) {
             // If the maxFileSize was not given, we display its value only
             // in debug mode.
             displayDebug(PROP_MAX_FILE_SIZE + ": " + getMaxFileSize(), 30);
         } else {
             // If the maxFileSize was given, we always inform the user.
             displayInfo(PROP_MAX_FILE_SIZE + ": " + getMaxFileSize());
         }
         displayDebug(PROP_NB_FILES_PER_REQUEST + ": " + getNbFilesPerRequest(),
                 30);
         displayDebug(PROP_POST_URL + ": " + this.postURL, 30);
         displayDebug(PROP_SERVER_PROTOCOL + ": " + getServerProtocol(), 30);
         displayDebug(PROP_SHOW_LOGWINDOW + ": " + getShowLogWindow(), 30);
         displayDebug(PROP_SHOW_STATUSBAR + ": " + this.showStatusbar, 30);
         displayDebug(PROP_SPECIFIC_HEADERS + ": " + getSpecificHeaders(), 30);
         displayDebug(PROP_STRING_UPLOAD_ERROR + ": " + getStringUploadError(),
                 30);
         displayDebug(PROP_STRING_UPLOAD_SUCCESS + ": "
                 + getStringUploadSuccess(), 30);
         displayDebug(PROP_STRING_UPLOAD_WARNING + ": "
                 + getStringUploadWarning(), 30);
         displayDebug(PROP_URL_TO_SEND_ERROR_TO + ": " + getUrlToSendErrorTo(),
                 30);
         displayDebug("", 30);
     }
 
     private final String normalizeURL(String url) throws JUploadException {
         if (null == url || url.length() == 0)
             return getApplet().getDocumentBase().toString();
         URI uri = null;
         try {
             uri = new URI(url);
             if (null == uri.getScheme())
                 uri = getApplet().getDocumentBase().toURI().resolve(url);
             if (!uri.getScheme().equals("http")
                     && !uri.getScheme().equals("https")
                     && !uri.getScheme().equals("ftp")) {
                 throw new JUploadException("URI scheme " + uri.getScheme()
                         + " not supported.");
             }
         } catch (URISyntaxException e) {
             throw new JUploadException(e);
         }
         return uri.toString();
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// getters / setters
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /** @see UploadPolicy#getAfterUploadURL() */
     public String getAfterUploadURL() {
         return this.afterUploadURL;
     }
 
     /**
      * Set the {@link #afterUploadURL}
      * 
      * @param afterUploadURL The URL to use.
      * @throws JUploadException
      */
     protected void setAfterUploadURL(String afterUploadURL)
             throws JUploadException {
         if (null == afterUploadURL)
             return;
         if (afterUploadURL.toLowerCase().startsWith("javascript:")) {
             this.afterUploadURL = afterUploadURL;
         } else
             this.afterUploadURL = normalizeURL(afterUploadURL);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#getAllowHttpPersistent()
      */
     public boolean getAllowHttpPersistent() {
         return this.allowHttpPersistent;
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
 
     protected void setAllowHttpPersistent(boolean value) {
         this.allowHttpPersistent = value;
     }
 
     /** @see UploadPolicy#getApplet() */
     public JUploadApplet getApplet() {
         return this.applet;
     }
 
     /** @see UploadPolicy#setCurrentBrowsingDirectory(File) */
     public void setCurrentBrowsingDirectory(File currentBrowsingDirectoryParam) {
         try {
             if (currentBrowsingDirectoryParam.isDirectory()) {
                 this.currentBrowsingDirectory = currentBrowsingDirectoryParam;
             } else {
                 displayWarn("DefaultUploadPolicy.setCurrentBrowsingDirectory(): "
                         + currentBrowsingDirectoryParam + " doesn't exist.");
             }
         } catch (SecurityException se) {
             displayWarn(se.getClass().getName()
                     + " in DefaultUploadPolicy.setCurrentBrowsingDirectory(): "
                     + currentBrowsingDirectoryParam + " is ignored.");
         }
     }
 
     /** @see UploadPolicy#setCurrentBrowsingDirectory(File) */
     private void setCurrentBrowsingDirectory(
             String currentBrowsingDirectoryParam) {
         if (currentBrowsingDirectoryParam == null) {
             this.currentBrowsingDirectory = null;
         } else {
             setCurrentBrowsingDirectory(new File(currentBrowsingDirectoryParam));
         }
     }
 
     /** @see UploadPolicy#getCurrentBrowsingDirectory() */
     public File getCurrentBrowsingDirectory() {
         return this.currentBrowsingDirectory;
     }
 
     /** @see UploadPolicy#getDateFormat() */
     public String getDateFormat() {
         return UploadPolicy.DEFAULT_DATE_FORMAT;
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
             if (this.debugGenerateFile) {
                 displayInfo("Current debug output file: "
                         + this.debugFile.getAbsolutePath());
             }
         }
         this.debugLevel = debugLevel;
 
         // Let's display the current applet parameters.
         if (displayAppletParameterList) {
             displayParameterStatus();
         }
     }
 
     /**
      * Getter for fileChooserIconFromFileContent.
      * 
      * @return Current value for fileChooserIconFromFileContent
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT
      */
     public int getFileChooserIconFromFileContent() {
         return this.fileChooserIconFromFileContent;
     }
 
     /**
      * Setter for fileChooserIconFromFileContent. Current allowed values are:
      * -1, 0, 1. Default value is 0.
      * 
      * @param fileChooserIconFromFileContent Value to be set. If the value is
      *            not allowed (not -1, 0 or 1), the current value is unchangeed.
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_FROM_FILE_CONTENT
      */
     public void setFileChooserIconFromFileContent(
             int fileChooserIconFromFileContent) {
         this.fileChooserIconFromFileContent = fileChooserIconFromFileContent;
     }
 
     /**
      * Getter for fileChooserIconSize.
      * 
      * @return Current value for fileChooserIconSize
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_SIZE
      */
     public int getFileChooserIconSize() {
         return this.fileChooserIconSize;
     }
 
     /**
      * Setter for fileChooserIconSize.
      * 
      * @param fileChooserIconSize Value to be set.
      * @see UploadPolicy#PROP_FILE_CHOOSER_ICON_SIZE
      */
     public void setFileChooserIconSize(int fileChooserIconSize) {
         this.fileChooserIconSize = fileChooserIconSize;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#setLang(String) */
     public void setLang(String lang) {
         Locale locale;
         this.lang = lang;
         if (lang == null) {
             displayInfo("lang = null, taking default language");
             locale = Locale.getDefault();
         } else {
             // If we have a 5 characters lang string, then it should look like
             // ll_CC, where ll is the language code
             // and CC is the Country code.
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
                 displayDebug("setLang - language read (no country): " + lang,
                         50);
             }
         }
 
         /*
          * Patch given by Patrick
          * 
          * Use of a specific classloader. The standard ResourceBundle checks
          * first for a class that has the name of the resource bundle. Since
          * there is no such class in the jar file, the AppletClassLoader makes a
          * http request to the server, which will end with a 404 since there is
          * no such class either. To avoid this unneccessary lookup we use a
          * clasloader that throws directly a ClassNotFoundException. After
          * looking for a class (which is unsuccessful) ResourceBundle looks
          * finally for a properties file. Herefore we delegate that lookup to
          * the original classloader since this is in the jar file.
          */
         this.resourceBundle = ResourceBundle.getBundle(
                 "wjhk.jupload2.lang.lang", locale,
                 // Special classloader, see description above
                 new ClassLoader(this.getClass().getClassLoader()) {
                     /** {@inheritDoc} */
                     @Override
                     public Class<?> loadClass(String name)
                             throws ClassNotFoundException {
                         throw new ClassNotFoundException();
                     }
 
                     /** {@inheritDoc} */
                     @Override
                     public InputStream getResourceAsStream(String name) {
                         return this.getClass().getClassLoader()
                                 .getResourceAsStream(name);
                     }
                 });
     }
 
     /**
      */
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
         if (maxChunkSize <= 0) {
             displayDebug(
                     "maxChunkSize<=0 which is invalid. Switched to the default value (Long.MAX_VALUE)",
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
 
     /** @see UploadPolicy#getFtpCreateDirectoryStructure() */
     public boolean getFtpCreateDirectoryStructure() {
         return this.ftpCreateDirectoryStructure;
     }
 
     /** @param ftpCreateDirectoryStructure the ftpCreateDirectoryStructure to set */
     protected void setFtpCreateDirectoryStructure(
             boolean ftpCreateDirectoryStructure) {
         this.ftpCreateDirectoryStructure = ftpCreateDirectoryStructure;
     }
 
     /** @see UploadPolicy#getFtpTransfertBinary() */
     public boolean getFtpTransfertBinary() {
         return this.ftpTransfertBinary;
     }
 
     /** @param ftpTransfertBinary the ftpTransfertBinary to set */
     protected void setFtpTransfertBinary(boolean ftpTransfertBinary) {
         this.ftpTransfertBinary = ftpTransfertBinary;
         ;
     }
 
     /** @see UploadPolicy#getFtpTransfertPassive() */
     public boolean getFtpTransfertPassive() {
         return this.ftpTransfertPassive;
     }
 
     /** @param ftpTransfertPassive the ftpTransfertPassive to set */
     protected void setFtpTransfertPassive(boolean ftpTransfertPassive) {
         this.ftpTransfertPassive = ftpTransfertPassive;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getPostURL() */
     public String getPostURL() {
         return this.postURL;
     }
 
     /**
      * @throws JUploadException
      * @see wjhk.jupload2.policies.UploadPolicy#setPostURL(String)
      */
     public void setPostURL(String postURL) throws JUploadException {
         // Be more forgiving about postURL:
         // - If none is specified, use the original DocumentBase of the applet.
         // - If a non-absolute URI (an URI without protocol and server) is
         // specified,
         // prefix it with "http://servername"
         // - If a relative URI is specified, prefix it with the DocumentBase's
         // parent
         this.postURL = normalizeURL(postURL);
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
     public String getServerProtocol() {
         if (this.serverProtocol == null) {
             return DEFAULT_SERVER_PROTOCOL;
         } else {
             return this.serverProtocol;
         }
     }
 
     /**
      * @param value the serverProtocol to set
      * @throws JUploadException
      */
     protected void setServerProtocol(String value) throws JUploadException {
         if (null == value || value.equals("")) {
             if (null == this.postURL || this.postURL.equals("")) {
                 displayErr("postURL not set");
                 value = UploadPolicy.DEFAULT_SERVER_PROTOCOL;
             } else if (this.postURL.substring(0, 3).equals("ftp")) {
                 value = "ftp";
             } else {
                 try {
                     value = new HttpConnect(this).getProtocol();
                 } catch (Exception e) {
                     // If we throw an error here, we prevent the applet to
                     // start. So we just log it, and try the default protocol
                     displayErr("Unable to access to the postURL: '"
                             + getPostURL() + "'", e);
                     // Let's try with default value.
                     value = UploadPolicy.DEFAULT_SERVER_PROTOCOL;
                 }
             }
         } else if (value.startsWith("HTTP")) {
             // In HTTP mode, we always give a try to HTTPConnect, to check if
             // the page has moved, and other stuff.
             // But we keep the given parameter.
             try {
                 new HttpConnect(this).getProtocol();
             } catch (Exception e) {
                 // If we throw an error here, we prevent the applet to
                 // start. So we just log it, and try the default protocol
                 displayErr("Unable to access to the postURL: '" + getPostURL()
                         + "'", e);
             }
         }
         this.serverProtocol = value;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getShowLogWindow() */
     public String getShowLogWindow() {
         return this.showLogWindow;
     }
 
     /** {@inheritDoc} */
     public void setShowLogWindow(String showLogWindow) {
         if (showLogWindow.equals(SHOWLOGWINDOW_TRUE)
                 || showLogWindow.equals(SHOWLOGWINDOW_FALSE)
                 || showLogWindow.equals(SHOWLOGWINDOW_ONERROR)) {
             this.showLogWindow = showLogWindow;
             // The log window may become visible or hidden, depending on this
             // parameter.
             if (getApplet().getUploadPanel() != null) {
                 getApplet().getUploadPanel().showOrHideLogWindow();
             }
         } else {
             displayWarn("[setShowLogWindow] Unallowed value: " + showLogWindow
                     + " (showLogWindow is left unchanged)");
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getSpecificHeaders() */
     public String getSpecificHeaders() {
         return this.specificHeaders;
     }
 
     /**
      * Set all specific headers defined in the specificHeaders applet parameter.
      * This string is splitted, so that each header is added to the headers
      * Vector. These headers are added to the headers list during applet
      * initialization. There is currently no automatic way to remove the headers
      * coming from specificHeaders, after initialization.
      * 
      * @param specificHeaders
      */
     protected void setSpecificHeaders(String specificHeaders) {
         this.specificHeaders = specificHeaders;
         if (specificHeaders != null) {
             // Let's add each header in specificHeaders to the headers list. In
             // specificHeaders, each header is separated by the \n string (two
             // characters: \ then n, not the \n character).
             // The regexp to find the \n string (not the \n character) is: \\n
             // We then double each \ character:
             String[] headerArray = specificHeaders.split("\\\\n");
             for (int x = 0; x < headerArray.length; x++) {
                 addHeader(headerArray[x]);
             }
         }
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#getSslVerifyCert()
      */
     public int getSslVerifyCert() {
         return this.sslVerifyCert;
     }
 
     protected void setSslVerifyCert(String mode) throws JUploadException {
         int val = -1;
         if (mode.toLowerCase().equals("none"))
             val = InteractiveTrustManager.NONE;
         if (mode.toLowerCase().equals("server"))
             val = InteractiveTrustManager.SERVER;
         if (mode.toLowerCase().equals("client"))
             val = InteractiveTrustManager.CLIENT;
         if (mode.toLowerCase().equals("strict"))
             val = InteractiveTrustManager.STRICT;
         if (val == -1)
             throw new JUploadException("Invalid parameter sslVerifyCert ("
                     + mode + ")");
         this.sslVerifyCert = val;
     }
 
     /** @param show the new showStatusbar value */
     protected void setShowStatusbar(boolean show) {
         this.showStatusbar = show;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadError() */
     public String getStringUploadError() {
         return this.stringUploadError;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadSuccess() */
     public String getStringUploadSuccess() {
         return this.stringUploadSuccess;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadWarning() */
     public String getStringUploadWarning() {
         return this.stringUploadWarning;
     }
 
     /**
      * @param stringUploadError the stringUploadError to set
      * @throws JUploadException
      */
     protected void setStringUploadError(String stringUploadError)
             throws JUploadException {
         this.stringUploadError = stringUploadError;
         if (stringUploadError != null) {
             try {
                 this.patternError = Pattern.compile(stringUploadError);
             } catch (PatternSyntaxException e) {
                 throw new JUploadException(
                         "Invalid regex in parameter stringUploadError");
             }
         }
     }
 
     /**
      * @param stringUploadSuccess the stringUploadSuccess to set
      * @throws JUploadException
      */
     protected void setStringUploadSuccess(String stringUploadSuccess)
             throws JUploadException {
         this.stringUploadSuccess = stringUploadSuccess;
         if (stringUploadSuccess != null) {
             try {
                 this.patternSuccess = Pattern.compile(stringUploadSuccess);
             } catch (PatternSyntaxException e) {
                 throw new JUploadException(
                         "Invalid regex in parameter stringUploadSuccess");
             }
         }
     }
 
     /**
      * @param stringUploadWarning the stringUploadWarning to set
      * @throws JUploadException
      */
     protected void setStringUploadWarning(String stringUploadWarning)
             throws JUploadException {
         this.stringUploadWarning = stringUploadWarning;
         if (stringUploadWarning != null) {
             try {
                 this.patternWarning = Pattern.compile(stringUploadWarning);
             } catch (PatternSyntaxException e) {
                 throw new JUploadException(
                         "Invalid regex in parameter stringUploadWarning");
             }
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getUrlToSendErrorTo() */
     public String getUrlToSendErrorTo() {
         return this.urlToSendErrorTo;
     }
 
     /** {@inheritDoc} */
     public void setUrlToSendErrorTo(String urlToSendErrorTo)
             throws JUploadException {
         if (null == urlToSendErrorTo)
             return;
         String tmp = normalizeURL(urlToSendErrorTo);
         if (tmp.startsWith("ftp://")) {
             throw new JUploadException(
                     "urlToSendErrorTo: ftp scheme not supported.");
         }
         this.urlToSendErrorTo = tmp;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getFormdata() */
     public String getFormdata() {
         return this.formData;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getAfterUploadTarget() */
     public String getAfterUploadTarget() {
         return this.afterUploadTarget;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// Internal methods
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * @see UploadPolicy#setWaitCursor()
      */
     public Cursor setWaitCursor() {
         Cursor previousCursor = getApplet().getCursor();
         getApplet().setCursor(new Cursor(Cursor.WAIT_CURSOR));
         return previousCursor;
     }
 
     /**
      * @see UploadPolicy#setCursor(Cursor)
      */
     public void setCursor(Cursor cursor) {
         getApplet().setCursor(cursor);
     }
 
     /**
      * Delete the current log. (called upon applet termination)
      */
     public void deleteLog() {
         if (this.debugGenerateFile) {
             try {
                 if (null != this.debugOut) {
                     this.debugOut.close();
                     this.debugOut = null;
                 }
                 if (null != this.debugFile) {
                     this.debugFile.delete();
                     this.debugFile = null;
                 }
             } catch (Exception e) {
                 // nothing to do
             }
         }
     }
 
     /**
      * This methods allows the applet to store all messages (debug, warning,
      * info, errors...) into a StringBuffer. If any problem occurs, the whole
      * output (displayed or not by the displayDebug, for instance) can be stored
      * in a file, or sent to the webmaster. This can help to identify and
      * correct problems that can occurs on the various computer configurations.
      * 
      * @param msg
      */
     protected synchronized void addMsgToDebugLog(String msg) {
         // If uploading lots of chunks, the buffer gets too large, resulting in
         // a OutOfMemoryError on the heap so we now use a temporary file for the
         // debug log.
         if (this.debugGenerateFile && this.debugOk) {
             try {
                 if (null == this.debugOut) {
                     this.getApplet().registerUnload(this, "deleteLog");
                     this.debugFile = File
                             .createTempFile("jupload_", "_log.txt");
                     this.debugOut = new PrintStream(new FileOutputStream(
                             this.debugFile));
                 }
                 boolean endsLF = msg.endsWith("\n");
                 msg = msg.replaceAll("\n", this.CRLF);
                 if (endsLF) {
                     this.debugOut.print(msg);
                 } else {
                     this.debugOut.println(msg);
                 }
             } catch (IOException e) {
                 this.debugOk = false;
                 System.err.println("IO error on debuglog "
                         + this.debugFile.getPath()
                         + "\nFallback to standard output.");
                 System.out.println(msg);
             }
         } else {
             System.out.println(msg);
         }
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
      * Displays a message. If the logWindow panel is set, the message is
      * displayed on it. If not, the System.out.println function is used.
      * 
      * @param msg The message to display.
      */
     private synchronized void displayMsg(String tag, String msg) {
         msg = timestamp(tag, msg);
         if (this.logWindow == null) {
             System.out.println(msg);
         } else {
             this.logWindow.append(msg);
             if (!msg.endsWith("\n"))
                 this.logWindow.append("\n");
             int lc = this.logWindow.getLineCount();
             if (lc > MAX_DEBUG_LINES) {
                 int end;
                 try {
                     end = this.logWindow.getLineEndOffset(lc - MAX_DEBUG_LINES);
                     this.logWindow.replaceRange("", 0, end);
                 } catch (BadLocationException e) {
                     e.printStackTrace();
                 }
             }
         }
         // Let's store all text in the debug logfile
         if (this.debugGenerateFile) {
             addMsgToDebugLog(msg);
         }
     }
 
     /**
      * Default reaction after a successful drop operation: no action.
      * 
      * @see UploadPolicy#afterFileDropped(DropTargetDropEvent)
      */
     public void afterFileDropped(DropTargetDropEvent dropEvent) {
         // Default: no action.
     }
 
     /**
      * Default implementation for {@link UploadPolicy#createFileChooser()}: just
      * a creation of a {@link JUploadFileChooser}.
      * 
      * @see UploadPolicy#createFileChooser()
      */
     public JUploadFileChooser createFileChooser() {
         return new JUploadFileChooser(this);
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
             String extension = DefaultFileData.getExtension(file).toLowerCase();
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
     public Icon fileViewGetIcon(File file) {
         return null;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getLastException() */
     public JUploadException getLastException() {
         return this.lastException;
     }
 
     /**
      * Set the last exception.
      * 
      * @param exception The last exception that occurs into the applet.
      */
     public void setLastException(JUploadException exception) {
         this.lastException = exception;
 
         // The log window may become visible.
         if (getApplet().getUploadPanel() != null) {
             getApplet().getUploadPanel().showOrHideLogWindow();
         }
 
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getLastResponseBody() */
     public String getLastResponseBody() {
         return this.lastResponseBody;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getLastResponseMessage() */
     public String getLastResponseMessage() {
         return (null != this.lastResponseMessage) ? this.lastResponseMessage
                 : "";
     }
 
 }
