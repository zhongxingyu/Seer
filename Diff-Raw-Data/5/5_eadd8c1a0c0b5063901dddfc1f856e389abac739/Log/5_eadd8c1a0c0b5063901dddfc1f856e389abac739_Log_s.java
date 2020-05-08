 package tylerhayes.tools;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * The <b>Log</b> Class defines an object that logs messages to a log file.
  * The user of the Log object can either specify their own file name and path,
  * or use the default file name given by the default Constructor
  * {@link #Log() Log()}.
  * <p>
  * In addition, the user can switch over to a new file during the use of the
  * Log object, if, for example, the file is growing too large by using the
  * {@link #setFile(String) setFile(String)} method, or the {@link
  * #setFileDefault() setFileDefault()} method.  The latter is if you want the
  * Log object to create the default file name for you (using the current
  * directory and current time).
  * </p>
  * By default, <b>Log</b> only outputs to the log file, but the user can have
  * it also output to <tt>stdout</tt>--either all messages (excluding the header
  * and footer) or only error messages.  This can be done using the
  * {@link #enableStdoutForAll() enableStdoutForAll()} and
  * {@link #enableStdoutForErrorsOnly() enableStdoutForErrorsOnly()} methods.
  * 
  * <p>
  * <b>INDICATORS</b>
  * </p>
  * <p>
  * The Log object writes its messages with <i>indicators</i>.  These indicators
  * are two-character strings that prepend the logged messages to make it easier
  * to locate certain kinds of messages when reading the log file.<br>
  * Here is an example of a log file with the message indicators:
  * </p>
  * <p>
  * <tt>
  * CURRENT URL ID: 1347<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;__ Parsing page contents...<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;~~ WARNING: server was redirected.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;## link found: "http://link"<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;** ERROR: could not save link in database.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;!! FATAL ERROR: IOException caught
  * </tt>
  * </p>
  * The Constructors set the indicators to the following default values:
  * <p>
  * General messages: <tt>"__"</tt><br>
  * Error messages: <tt>"**"</tt><br>
  * Fatal error messages: <tt>"!!"</tt><br>
  * Warning messages: <tt>"~~"</tt><br>
  * Data value messages: <tt>"##"</tt><br>
  * Kill message: <tt>"XX"</tt>
  * </p>
  * <p>
  * The user can, however, set their own indicator values by calling any of the
  * <tt>set[indicator]</tt> methods, such as {@link #setDataIndicator(String)
  * setDataIndicator(String)}.
  * </p>
  * <p>
  * The user also has the option of not using indicators at all, in which case
  * one would call {@link #disableIndicators()}.  If the user wanted to turn the
  * use of indicators back on, a call to {@link #enableIndicators()
  * enableIndicators()} would set the indicators back to whatever they were
  * before the call to <tt>disableIndicators()</tt>.  There is also the option
  * of resetting the indicators to their default values with a call to {@link
  * #resetIndicators() resetIndicators()}.
  * </p>
  * <p>
  * To use custom indicators, one would call the {@link
  * #logGeneralMessageWithoutIndicator(String, int, boolean)} method and simply
  * include the indicator within the message being passed in.
  * For example, if one was logging a web crawler and wanted an indicator for
  * logging every link extracted from each web page, the above method could be
  * called like so: <code>logObj.logGeneralMessageWithoutIndicator("-> Link
  * extracted: " + link, 2, true);</code>, where "<tt>-> </tt>" is the indicator.
  * Unfortunately, you will have to include that indicator every time you want
  * to use it.
  * </p>
  * 
  * @author Tyler Hayes - Portland State University &copy; 2010.
  */
 public class Log {
   
   private BufferedWriter bw;
   private Calendar time;
   private String generalIndicator;
   private String generalIndicatorReserve;
   private String errorIndicator;
   private String errorIndicatorReserve;
   private String fatalErrorIndicator;
   private String fatalErrorIndicatorReserve;
   private String warningIndicator;
   private String warningIndicatorReserve;
   private String dataIndicator;
   private String dataIndicatorReserve;
   private String killIndicator;
   private String killIndicatorReserve;
   private int tabSize;
   private boolean stdoutAll;
   private boolean stdoutErrors;
   
   
   /**
    * Creates a Log object that writes to a default log file.  The default
    * file is placed in the current directory and named
    * "[current timestamp].log".  The timestamp is of the format:
    * MM-DD-YYYY_[time in milliseconds].
    * <p>
    * Example: <tt>8-20-2010_1282332840418.log</tt> for a log file created on
    * August 20th, 2010, at around 12:34 pm.
    * </p>
    * <p>
    * By default, output will only be directed to the log file.  If you would
    * like to also output the log messages to stdout (either all messages, or
    * only error messages), use the {@link #enableStdoutForAll()
    * enableStdoutForAll()} or {@link #enableStdoutForErrorsOnly()
    * enableStdoutForErrorsOnly()} methods.
    * </p>
    */
   public Log() {
     //instantiate Calendar object
     time = new GregorianCalendar();
     //set up the default log file
     initDefaultLogFile();
     //initialize the message indicators
     initIndicators();
     //set default tab size
     tabSize = 4;
   }
   
   
   /**
    * Creates a Log object that writes to the file given as the parameter,
    * <tt>fileName</tt>.  If the file already exists, the original file will be
    * overridden, so be careful.
    * <p>
    * If you wish to append to the given file, use the following Constructor,
    * {@link #Log(String, boolean) Log(String, boolean)}.
    * </p>
    * <p>
    * By default, output will only be directed to the log file.  If you would
    * like to also output the log messages to stdout (either all messages, or
    * only error messages), use the {@link #enableStdoutForAll()
    * enableStdoutForAll()} or {@link #enableStdoutForErrorsOnly()
    * enableStdoutForErrorsOnly()} methods.
    * </p>
    * 
    * @param fileName A <tt>String</tt> for the path and filename of the log
    * file.
    * <p>
    * <b>Windows example</b> (forward slashes need escaping):
    * <tt>"C:\\dir\\program.log"</tt><br>
    * <b>Unix/Linux example</b> ('~' means home directory):
    * <tt>"~/logfiles/program.log"</tt><br>
    * </p>
    * <p>
    * To create the log file in the current directory, just give a name without
    * the system-specific slashes, e.g. <tt>"filename.log"</tt>.
    * </p>
    */
   public Log(String fileName) {
     //instantiate Calendar object
     time = new GregorianCalendar();
     //initialize the message indicators
     initIndicators();
     //set default tab size
     tabSize = 4;
     
     try {
       bw = new BufferedWriter(new FileWriter(fileName));
     }
     catch (IOException ioe) {
       exitFromIoError("while creating Log object.", ioe);
     }
   }
   
   
   /**
    * Creates a Log object that appends to the file given as the parameter,
    * <tt>fileName</tt>.
    * <p>
    * By default, output will only be directed to the log file.  If you would
    * like to also output the log messages to stdout (either all messages, or
    * only error messages), use the {@link #enableStdoutForAll()
    * enableStdoutForAll()} or {@link #enableStdoutForErrorsOnly()
    * enableStdoutForErrorsOnly()} methods.
    * </p>
    * @param fileName A <tt>String</tt> for the path and filename of the log
    * file.
    * @param append A <tt>boolean</tt> specifying whether or not to append to
    * the file given as <tt>fileName</tt>.  This should always be <tt>true</tt>,
    * because if you do not want to append, then just use the previous
    * Constructor, {@link #Log(String) Log(String)}.
    */
   public Log(String fileName, boolean append) {
     //instantiate Calendar object
     time = new GregorianCalendar();
     //initialize the message indicators
     initIndicators();
     //set default tab size
     tabSize = 4;
     
     try {
       bw = new BufferedWriter(new FileWriter(fileName, append));
     }
     catch (IOException ioe) {
       exitFromIoError("while creating Log object.", ioe);
     }
   }
   
   
   /**
    * Properly closes the resources used with the Log object--in this case just
    * a private <tt>BufferedWriter</tt> object used to write to the specified
    * file.
    */
   public void close() {
     try { bw.close(); }
     catch (IOException ioe) {
       exitFromIoError("while initializing new log file.", ioe);
     }
   }
   
   
   /**
    * Sets the Log object to write to the given file.  Since all constructors
    * instantiate a <tt>BufferedWriter</tt> object to a file, these <tt>setFile
    * </tt> methods first close that <tt>BufferedWriter</tt> object before
    * instantiating a new one to the file given.
    * <p>
    * The purpose of these <tt>setFile</tt> methods is to give the caller the
    * option of switching to a new file, if, for example, the file it was
    * previously writing to was getting substantially large.
    * </p>
    * 
    * @param fileName A <tt>String</tt> representing the file to write the log
    * entries to.
    * <p>
    * <b>NOTE:</b> if the file already exists, the original file will be written
    * over and lost, so be careful.
    * </p>
    * <p>
    * If you would like to append to a file use the {@link #setFileAppend(String)
    * setFileAppend(String)} method.  There is also a Constructor to set up the
    * given file for appending: {@link #Log(String, boolean)
    * Log(String, boolean)}.
    * </p>
    * @see #Log()
    * @see #Log(String)
    * @see #Log(String, boolean)
    * @see #setFileAppend(String) setFileAppend
    * @see #setFileDefault() setFileDefault
    */
   public void setFile(String fileName) {
     try {
       //close current file appropriately
       bw.close();
       //create a new file specified by the 'fileName' parameter
       bw = new BufferedWriter(new FileWriter(fileName));
     }
     catch (IOException ioe) {
       exitFromIoError("while trying to create a new log file.", ioe);
     }
   }
   
   
   /**
    * Sets the Log object to write to the given file for appending.  The only
    * difference between this method and {@link #setFile(String)} is that this
    * method sets the BufferedWriter for appending rather than implicitly
    * writing over the file if it already exists.  If you don't care about
    * writing over the file, you should just call <code>setFile(String)</code>.
    * <p>
    * The purpose of these <tt>setFile</tt> methods is to give the caller the
    * option of switching to a new file, if, for example, the file it was
    * previously writing to was getting substantially large.
    * </p>
    * 
    * @param fileName A <tt>String</tt> representing the file to append the log
    * entries to.
    * <p>
    * <b>NOTE:</b> there is also a Constructor, {@link #Log(String, boolean)},
    * that initializes the BufferedWriter for appending.
    * </p>
    * @see #Log()
    * @see #Log(String)
    * @see #Log(String, boolean)
    * @see #setFile(String) setFile
    * @see #setFileDefault() setFileDefault
    */
   public void setFileAppend(String fileName) {
     try {
       //close current file appropriately
       bw.close();
       //create a new file specified by the 'fileName' parameter
       bw = new BufferedWriter(new FileWriter(fileName, true));
     }
     catch (IOException ioe) {
       exitFromIoError("while trying to create a new log file.", ioe);
     }
   }
   
   
   /**
    * Sets the Log object to write to a log file with a default file name.  The
    * default file name is of the form
    * <tt>MM-DD-YYYY_[current time in milliseconds].log</tt> and is placed in the current
    * directory (where the program <tt>.class</tt> file resides).
    * <p>
    * Example: <tt>8-20-2010_1282332840418.log</tt> for a log file created on
    * August 20th, 2010, at around 12:34 pm.
    * </p>
    * <p>
    * This is usually only used when the Log object was first constructed with
    * the default Constructor, {@link #Log()}.
    * </p>
    * <p>
    * The purpose of these <tt>setFile</tt> methods is to give the caller the
    * option of switching to a new file, if, for example, the file it was
    * previously writing to was getting substantially large.
    * </p>
    * @see #Log()
    * @see #Log(String)
    * @see #Log(String, boolean)
    * @see #setFile(String) setFile
    * @see #setFileAppend(String) setFileAppend
    */
   public void setFileDefault() {
     try {
       //close current file appropriately
       bw.close();
       //create a new file using the default file name format
       initDefaultLogFile();
     }
     catch (IOException ioe) {
       exitFromIoError("while closing current log file.", ioe);
     }
   }
   
   
   /**
    * Sets the tab size (number of spaces) for message indentation.  Indenting
    * certain messages can really make the log file more organized and more
    * readable--especially when logging activities within nested loops.
    * <p>
    * <b>NOTE: </b>the constructors set the tab size to 4 as the default.
    * </p>
    * @param size The number of spaces to use for indenting.
    * <p>
    * <b>NOTE: </b>The tab size will be set to zero for any integer passed in
    * that is less than or equal to zero.  On the flip side, 25 is the maximum
    * (although I can't imagine wanting a tab size bigger than 8 or possibly 12),
    * so the tab size will be set to 25 for any integer passed in that is greater
    * than or equal to 25.
    * </p>
    */
   public void setTabSize (int size) {
     if (size < 0)
       tabSize = 0;
     if (size > 25)
       tabSize = 25;
     else
       tabSize = size;
   }
   
   
   /**
    * Sets the general message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setGeneralIndicator(String indicator) {
     generalIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Sets the data message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setDataIndicator(String indicator) {
     dataIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Sets the warning message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setWarningIndicator(String indicator) {
     warningIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Sets the error message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setErrorIndicator(String indicator) {
     errorIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Sets the fatal error message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setFatalIndicator(String indicator) {
     fatalErrorIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Sets the kill message indicator.
    * @param indicator A <tt>String</tt> representing the message indicator.
    */
   public void setKillIndicator(String indicator) {
     killIndicator = String.valueOf(indicator);
   }
   
   
   /**
    * Outputs a formatted header marking the beginning of the log session.  This
    * is especially useful when the Log object is appending to a file, so that
    * one can easily spot the beginnings of each logging session.  When the Log
    * is not appending this will always just be at the top of the file.
    * <p>
    * The header looks like this:
    * <p><tt>
    * ================================================================================<br>
    * ==&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;{headerText centered}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;==<br>
    * ================================================================================
    * </tt></p>
    * It is 80 characters wide, and 3 lines, followed by an empty line.
    * </p>
    * @param headerText Text to be placed inside the header block.
    * @see #logFooter(String) logFooter
    */
   public void logHeader(String headerText) {
     try {
       //write 80 '=' characters
       bw.write("==============================================================="
                 + "=================");
       bw.newLine();
       
       bw.write("==");
       
       //determine the number of spaces to place before the header text
       int spacesBefore = (76 - headerText.length()) / 2;
       //determine the number of spaces after the header text (either the same
       // as, or 1 more than before)
       int spacesAfter = spacesBefore + (76 - headerText.length()) % 2;
       //output spaces before header text
       for (int i=0; i < spacesBefore; ++i)
         bw.write(" ");
       //output header text
       bw.write(headerText);
       //output spaces after header text
       for (int i=0; i < spacesAfter; ++i)
         bw.write(" ");
       
       bw.write("==");
       bw.newLine();
       bw.write("==============================================================="
           + "=================");
       bw.newLine();
       bw.newLine();
     }
     catch (IOException ioe) {
       exitFromIoError("while writing header message (or footer, since"
                       + " logFooter() calls logHeader())", ioe);
     }
   }
   
   
   /**
    * Outputs a formatted footer marking the end of a log session.  This is
    * especially useful when the Log object is appending to a file, so that
    * one can easily spot the beginnings and endings of each logging session.
    * When the Log is not appending this will always just be at the bottom of the
    * file, unless the program terminated unexpectedly and was not able to output
    * a footer.
    * <p>
    * This is a wrapper for {@link #logHeader(String) logHeader}.
    * </p>
    * <p>
    * The footer looks like this:
    * <p><tt>
    * ================================================================================<br>
    * ==&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;{footerText centered}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;==<br>
    * ================================================================================
    * </tt></p>
    * It follows an empty line, and is 80 characters wide, and 3 lines long.
    * </p>
    * @param footerText Text to be placed inside the header block.
    * @see #logHeader(String) logHeader
    */
   public void logFooter(String footerText) {
     try {
       bw.newLine();
       logHeader(footerText);
     }
     catch (IOException ioe) {
       exitFromIoError("while writing newline before footer.", ioe);
     }
   }
   
   
   /**
    * Logs a general message to the log file.  A "general" message is basically
    * one that is not data, an error, nor a warning.
    * <p>
    * The given message will be prepended by the general message <i>indicator</i>
    * (<i>indicators</i> are described in the opening description of the <b>Log
    * </b> Class).  If you do not want the indicator to be logged with the
    * message, you can set it to the empty string (<tt>""</tt>) by calling
    * {@link #setGeneralIndicator(String)}, disabling all indicators by calling
    * {@link #disableIndicators()}, or, in this case, calling {@link
    * #logGeneralMessageWithoutIndicator(String, int, boolean)}.
    * </p>
    * <p>
    * By default, the indicator for this type of message is "<tt>__</tt>".
    * </p>
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log (following the indicator).
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logGeneralMessage(String message, int tabs, boolean usingTime) {
    printMessage(generalIndicator + " ", message, tabs, usingTime);
   }
   
   
   /**
    * Logs a general message to the log file without an indicator.  This gives
    * you the option of using your own indicators.  Simply include them at the
    * beginning of the messages you pass in.
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log.
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logGeneralMessageWithoutIndicator(String message, int tabs,
                                                             boolean usingTime) {
     printMessage("", message, tabs, usingTime);
   }
   
   
   /**
    * Logs a data message to the log file.  A "data" message is primarily used
    * to output values of one or more variables.  For example, if you want to
    * know the value of the <tt>i</tt> variable in a <tt>for</tt> loop, you
    * should log it with this method to distinguish the log entry from another
    * type of message.
    * <p>
    * The given message will be prepended by the data message <i>indicator</i>
    * (<i>indicators</i> are described in the opening description of the <b>Log
    * </b> Class).  If you do not want the indicator to be logged with the
    * message, you can set it to the empty string (<tt>""</tt>) by calling
    * {@link #setDataIndicator(String)}, or disabling all indicators by calling
    * {@link #disableIndicators()}.
    * </p>
    * <p>
    * By default, the indicator for this type of message is "<tt>##</tt>".
    * </p>
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log (following the indicator).
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logData(String message, int tabs, boolean usingTime) {
    printMessage(dataIndicator + " ", message, tabs, usingTime);
   }
   
   
   /**
    * Logs a warning message to the log file.  A "warning" message is something
    * worthy of attention--could be bad, could be irrelevant.
    * <p>
    * The given message will be prepended by the warning message <i>indicator</i>
    * (<i>indicators</i> are described in the opening description of the <b>Log
    * </b> Class).  If you do not want the indicator to be logged with the
    * message, you can set it to the empty string (<tt>""</tt>) by calling
    * {@link #setWarningIndicator(String)}, or disabling all indicators by
    * calling {@link #disableIndicators()}.
    * </p>
    * <p>
    * By default, the indicator for this type of message is "<tt>~~</tt>".
    * </p>
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log (following the indicator).
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logWarning(String message, int tabs, boolean usingTime) {
     printMessage(warningIndicator + " WARNING: ", message, tabs, usingTime);
   }
   
   
   /**
    * Logs an error message to the log file.  An "error" message indicates that
    * something wrong has occurred, but not too bad that the program should
    * terminate as a result.  you could call this a "soft" error.
    * <p>
    * The given message will be prepended by the error message <i>indicator</i>
    * (<i>indicators</i> are described in the opening description of the <b>Log
    * </b> Class).  If you do not want the indicator to be logged with the
    * message, you can set it to the empty string (<tt>""</tt>) by calling
    * {@link #setErrorIndicator(String)}, or disabling all indicators by calling
    * {@link #disableIndicators()}.
    * </p>
    * <p>
    * By default, the indicator for this type of message is "<tt>**</tt>".
    * </p>
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log (following the indicator).
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logError(String message, int tabs, boolean usingTime) {
     printErrorMessage(errorIndicator + " ERROR: ", message, tabs, usingTime);
   }
   
   
   /**
    * Logs a fatal error message to the log file.  A "fatal error" message shows
    * that a critical error has occurred causing the program to terminate.  Of
    * course, this method does not actually terminate the program--that is up to
    * the caller, but this method should not be called if the caller does not
    * intend to terminate the program shortly after (it wouldn't be a fatal
    * error, otherwise).
    * <p>
    * The given message will be prepended by the fatal error message <i>indicator
    * </i> (<i>indicators</i> are described in the opening description of the <b>
    * Log</b> Class).  If you do not want the indicator to be logged with the
    * message, you can set it to the empty string (<tt>""</tt>) by calling
    * {@link #setFatalIndicator(String)}, or disabling all indicators by calling
    * {@link #disableIndicators()}.
    * </p>
    * <p>
    * By default, the indicator for this type of message is "<tt>!!</tt>".
    * </p>
    * <p>
    * All of these <tt>log</tt> methods require the number of tabs to indent and
    * whether or not to prepend the entry with a timestamp (in addition to the
    * message itself).
    * </p>
    * @param message The message to log (following the indicator).
    * @param tabs The number of tabs to indent the message.  The indentation
    * begins after the timestamp if a timestamp is to be included.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logFatalError(String message, int tabs, boolean usingTime) {
     printErrorMessage(fatalErrorIndicator + " FATAL ERROR: ", message, tabs,
                                                                      usingTime);
   }
   
   
   /**
    * Logs the current timestamp to the log file.  When logging messages with
    * timestamps, the timestamp always comes first--at the start of a new line.
    * This method, however, can be used to simply log a timestamp without an
    * indicator or message, with any amount of indentation.
    * @param tabs The number of tabs to indent before logging the timestamp.
    */
   public void logTimestamp(int tabs) {
     time.setTime(new Date());
     printMessage("", time.toString(), tabs, false);
   }
   
   
   /**
    * Logs that the program has been killed.  It does not distinguish what killed
    * the program, however, just that it has been killed.  Of course, in order to
    * use this practically, your program needs some sort of signal handling
    * mechanism to intercept the kill signal.  As of the time of this writing
    * (Aug, 2010), this proved helpful:
    * <a href="http://twit88.com/blog/2007/09/27/do-a-graceful-shutdown-of-your-java-application-when-ctr-c-kill/">
    * Do a Graceful Shutdown of Your Java-Application</a>.  <b>NOTE: </b>the
    * approach given in that article does not catch the signal caused when
    * clicking the "Terminate" button in Eclipse.  It will catch CTRL-C when
    * running through a command-line.
    * <p>
    * Unlike most of the other <tt>log</tt> methods, this one does not take a
    * <tt>String</tt> as an argument for a caller-defined message.  The kill
    * message is always:
    * <p>
    * <tt>Program has been killed.</tt>
    * </p>
    * <p>
    * and the default indicator is "<tt>XX</tt>".
    * </p>
    * </p>
    * @param tabs The number of tabs to indent before logging the kill message.
    * @param usingTime <tt>true</tt> will cause a timestamp to be output before
    * the message, and <tt>false</tt> will not.
    */
   public void logKill(int tabs, boolean usingTime) {
     printMessage(killIndicator, "Program has been killed.", tabs, usingTime);
   }
   
   
   /**
    * Turns off the use of message indicators.  What this actually does is
    * simply change all of the private indicators to the empty string (after
    * saving the current values in case the caller wants to turn them back on).
    * 
    * @see #enableIndicators() enableIndicators
    */
   public void disableIndicators() {
     generalIndicatorReserve = String.valueOf(generalIndicator);
     generalIndicator = "";
     errorIndicatorReserve = String.valueOf(errorIndicator);
     errorIndicator = "";
     fatalErrorIndicatorReserve = String.valueOf(fatalErrorIndicator);
     fatalErrorIndicator = "";
     warningIndicatorReserve = String.valueOf(warningIndicator);
     warningIndicator = "";
     dataIndicatorReserve = String.valueOf(dataIndicator);
     dataIndicator = "";
     killIndicatorReserve = String.valueOf(killIndicator);
     killIndicator = "";
   }
   
   
   /**
    * Turns the use of indicators back on (they are on by default).  This returns
    * the indicator values back to waht they were before the call to {@link
    * #disableIndicators()}.  A call to this before without ever calling <tt>
    * disableIndicators()</tt> does nothing (but waste cycles).
    */
   public void enableIndicators() {
     generalIndicator = String.valueOf(generalIndicatorReserve);
     errorIndicator = String.valueOf(errorIndicatorReserve);
     fatalErrorIndicator = String.valueOf(fatalErrorIndicatorReserve);
     warningIndicator = String.valueOf(warningIndicatorReserve);
     dataIndicator = String.valueOf(dataIndicatorReserve);
     killIndicator = String.valueOf(killIndicatorReserve);
   }
   
   
   /**
    * Resets the indicator values to the default values:
    * <p>
    * General messages: <tt>"__"</tt><br>
    * Error messages: <tt>"**"</tt><br>
    * Fatal error messages: <tt>"!!"</tt><br>
    * Warning messages: <tt>"~~"</tt><br>
    * Data value messages: <tt>"##"</tt><br>
    * Kill message: <tt>"XX"</tt>
    * </p>
    */
   public void resetIndicators() {
     initIndicators();
   }
   
   
   /**
    * Enables the output of all logged messages (excluding header and footer)
    * to be directed to stdout as well as the log file.
    * <p>
    * <b>NOTE:</b> a client may accidentally enable stdout for both all messages
    * <i>and</i> error messages by calling both this method and {@link
    * #enableStdoutForErrorsOnly() enableStdoutForErrorsOnly()}.  <b>Log</b>
    * does not prevent this from happening.  However, doing so does not output
    * error messages twice--it only has the same effect of this method.
    */
   public void enableStdoutForAll() {
 	  stdoutAll = true;
   }
   
   
   /**
    * Enables the output of error messages to be directed to stdout as well as
    * the log file.
    * <p>
    * <b>NOTE:</b> a client may accidentally enable stdout for both all messages
    * <i>and</i> error messages by calling both this method and {@link
    * #enableStdoutForAll() enableStdoutForAll()}.  <b>Log</b> does not prevent
    * this from happening.  However, doing so does not output error messages
    * twice--it only has the same effect of {@link #enableStdoutForAll()
    * enableStdoutForAll()}.
    */
   public void enableStdoutForErrorsOnly() {
 	  stdoutErrors = true;
   }
   
   
   /**
    * Disables the directing of messages to stdout.  If output to stdout was
    * never enabled, then this call has no effect.  If all messages were
    * enabled to be output to stdout, but it is now desired to only direct
    * errors to stdout, then a call to this method followed by a call to
    * {@link #enableStdoutForErrorsOnly() enableStdoutForErrorsOnly()} will do
    * the job.
    */
   public void disableStdoutForAll() {
 	  stdoutAll = false;
 	  stdoutErrors = false;
   }
   
   
   /*
    * Instantiates the BufferedWriter object to a default file in the current
    * directory with the current date and current time in milliseconds as the
    * file name.
    */
   private void initDefaultLogFile() {
     try {
       //reset the time to the current time
       time.setTime(new Date());
       int month = time.get(Calendar.MONTH) + 1;
       String fileName = month + "-" + time.get(Calendar.DATE) + "-"
               + time.get(Calendar.YEAR) + "_" + time.getTimeInMillis() + ".log";
       
       //get the current directory path
       File curDir = new File(".");
       String currentDirectory = curDir.getCanonicalPath();
       
       //unix-based file system syntax
       if (currentDirectory.contains("/"))
         //create a generic, timestamped log file in the current directory
         bw = new BufferedWriter(new FileWriter(currentDirectory + "/"
                                                + fileName));
       
       //Windows file system syntax
       else
         //create a generic, timestamped log file in the current directory
         bw = new BufferedWriter(new FileWriter(currentDirectory + "\\"
                                                + fileName));
     }
     catch (IOException ioe) {
       exitFromIoError("while creating default log file.", ioe);
     }
   }
   
   
   /*
    * Sets the indicators to their default values.
    */
   private void initIndicators() {
     generalIndicator = "__";
     generalIndicatorReserve = "__";
     errorIndicator = "**";
     errorIndicatorReserve = "**";
     fatalErrorIndicator = "!!";
     fatalErrorIndicatorReserve = "!!";
     warningIndicator = "~~";
     warningIndicatorReserve = "~~";
     dataIndicator = "##";
     dataIndicatorReserve = "##";
     killIndicator = "XX";
     killIndicatorReserve = "XX";
   }
   
   
   /*
    * This is what happens whenever an IOException is caught.
    */
   private void exitFromIoError(String message, IOException ioe) {
     System.out.println("**ERROR: " + message);
     ioe.printStackTrace();
     System.exit(1);
   }
   
   
   /*
    * This is the method that does the actual output to the file (and maybe
    * stdout if it's enabled) for writing non-error messages (error messages
    * are passed to printErrorMessage()).  It outputs a timestamp if it's
    * desired, any indentation, the appropriate indicator and space,
    * followed by the message.
    */
   private void printMessage(String indicator, String message, int tabs,
                                                             boolean usingTime) {
     try {
       //output timestamp if caller desired it
       if (usingTime) {
         //reset the calendar to the current time
         time.setTime(new Date());
         //output the timestamp
         bw.write(time.getTime().toString() + "> ");
         if (stdoutAll)
         	System.out.print(time.getTime().toString() + "> ");
       }
       
       //indent the message
       StringBuilder sb = new StringBuilder("");
       for (int i=0; i < tabSize*tabs; ++i) {
         sb.append(" ");
       }
       bw.write(sb.toString());
       if (stdoutAll)
     	  System.out.print(sb.toString());
       
       //output the indicator followed by the message
       bw.write(indicator + " " + message);
       bw.newLine();
       if (stdoutAll)
     	  System.out.println(indicator + " " + message);
     }
     catch (IOException ioe) {
       exitFromIoError("while writing message to log file.", ioe);
     }
   }
   
   /*
    * This is the method that does the actual output to the file (and maybe
    * stdout if it's enabled) for writing error messages (regular messages
    * are passed to printMessage()).  It outputs a timestamp if it's
    * desired, any indentation, the appropriate indicator and space,
    * followed by the message.
    */
   private void printErrorMessage(String indicator, String message, int tabs,
                                                             boolean usingTime) {
     try {
       //output timestamp if caller desired it
       if (usingTime) {
         //reset the calendar to the current time
         time.setTime(new Date());
         //output the timestamp
         bw.write(time.getTime().toString() + "> ");
         if (stdoutAll || stdoutErrors)
         	System.out.print(time.getTime().toString() + "> ");
       }
       
       //indent the message
       StringBuilder sb = new StringBuilder("");
       for (int i=0; i < tabSize*tabs; ++i) {
         sb.append(" ");
       }
       bw.write(sb.toString());
       if (stdoutAll || stdoutErrors)
     	  System.out.print(sb.toString());
       
       //output the indicator followed by the message
       bw.write(indicator + " " + message);
       bw.newLine();
       if (stdoutAll || stdoutErrors)
     	  System.out.println(indicator + " " + message);
     }
     catch (IOException ioe) {
       exitFromIoError("while writing message to log file.", ioe);
     }
   }
 }
