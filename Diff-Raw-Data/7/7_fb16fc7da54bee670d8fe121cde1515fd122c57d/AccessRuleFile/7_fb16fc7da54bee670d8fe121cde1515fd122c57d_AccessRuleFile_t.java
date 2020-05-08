 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.io.FileWatcher;
import org.xins.common.text.TextUtils;
 import org.xins.common.text.ParseException;
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * A set of access rules in a separate file.
  *
  * <p>An <code>AccessRuleFile</code> instance is constructed using a
  * descriptor and a file watch interval. The descriptor is a character string
  * that is parsed to determine which file should be parsed and monitored for
  * changes. Such a descriptor must match the following pattern:
  *
  * <blockquote><code>file&nbsp;<em>filename</em></code></blockquote>
  *
  * where <em>filename</em> is the name of the file to parse and watch.
  *
  * <p>The file watch interval is specified in seconds. At the specified
  * interval, the file will be checked for modifications. If there are any
  * modifications, then the file is reloaded and the access rules are
  * re-applied.
  *
  * <p>If the file watch interval is set to <code>0</code>, then the watching
  * is disabled, and no automatic reloading will be performed.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class AccessRuleFile
 extends Object
 implements AccessRuleContainer {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * An empty array of type <code>AccessRuleContainer[]</code>. Size is 0.
     * This object is used to pass to {@link List#toArray(Object[])}.
     */
    private static final AccessRuleContainer[] ARC_ARRAY =
       new AccessRuleContainer[0];
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns the next token in the descriptor.
     *
     * @param descriptor
     *    the original descriptor, useful when constructing the message for a
     *    {@link ParseException}, when appropriate, should not be
     *    <code>null</code>.
     *
     * @param tokenizer
     *    the {@link StringTokenizer} to retrieve the next token from, cannot be
     *    <code>null</code>.
     *
     * @return
     *    the next token, never <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>tokenizer == null</code>
     *
     * @throws ParseException
     *    if <code>tokenizer.{@link StringTokenizer#hasMoreTokens()
     *    hasMoreTokens}() == false</code>.
     */
    private static String nextToken(String          descriptor,
                                    StringTokenizer tokenizer)
    throws ParseException {
 
       if (! tokenizer.hasMoreTokens()) {
          String message = "The string \""
                         + descriptor
                         + "\" is invalid as an access rule file descriptor. "
                         + "More tokens expected.";
          throw new ParseException(message);
       } else {
          return tokenizer.nextToken();
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AccessRuleFile</code> based on a descriptor and
     * a file watch interval.
     *
     * <p>If the specified interval is <code>0</code>, then no watching will be
     * performed.
     *
     * @param descriptor
     *    the access rule file descriptor, the character string to parse,
     *    cannot be <code>null</code>.
     *
     * @param interval
     *    the interval to check the ACL file for modifications, in seconds,
     *    must be &gt;= 0.
     *
     * @throws ParseException
     *    If the token is incorrectly formatted.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null || interval &lt; 0</code>.
     */
    public AccessRuleFile(String descriptor, int interval)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
       if (interval < 0) {
          throw new IllegalArgumentException("interval ("
                                           + interval
                                           + ") < 0");
       }
 
       // First token must be 'file'
       StringTokenizer tokenizer = new StringTokenizer(descriptor, " \t\n\r");
       String token = nextToken(descriptor, tokenizer);
       if (! "file".equals(token)) {
          throw new ParseException("First token of descriptor is \""
                                 + token
                                 + "\", instead of \"file\".");
       }
 
       // First try parsing the file as it is
       _file = nextToken(descriptor, tokenizer);
       IOException exception;
       try {
          parseAndApply(_file, interval);
 
       // File not found
       } catch (FileNotFoundException fnfe) {
          String message = "File \""
                         + _file
                         + "\" cannot be opened for reading.";
          ParseException pe = new ParseException(message, fnfe, null);
          throw pe;
 
       // I/O error reading from the file not found
       } catch (IOException ioe) {
          String message = "Cannot parse the file \""
                         + _file
                         + "\" due to an I/O error.";
          ParseException pe = new ParseException(message, ioe, null);
          throw pe;
       }
 
       // Create and start a file watch thread, if the interval is not zero
       if (interval > 0) {
          FileListener fileListener = new FileListener();
          _fileWatcher = new FileWatcher(_file, interval, fileListener);
          _fileWatcher.start();
       }
 
       // Generate the string representation
       _asString = "file " + _file;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The ACL file.
     */
    private String _file;
 
    /**
     * The interval used to check the ACL file for modification.
     */
    private int _interval;
 
    /**
     * Watcher for the ACL file.
     */
    private FileWatcher _fileWatcher;
 
    /**
     * The list of rules. Cannot be <code>null</code>.
     */
    private AccessRuleContainer[] _rules;
 
    /**
     * String representation of this object. Cannot be <code>null</code>.
     */
    private final String _asString;
 
    /**
     * Flag that indicates whether this object is disposed.
     */
    private boolean _disposed;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Determines if the specified IP address is allowed to access the
     * specified function, returning a <code>Boolean</code> object or
     * <code>null</code>.
     *
     * <p>This method finds the first matching rule and then returns the
     * <em>allow</em> property of that rule (see
     * {@link AccessRule#isAllowRule()}). If there is no matching rule, then
     * <code>null</code> is returned.
     *
     * @param ip
     *    the IP address, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @return
     *    {@link Boolean#TRUE} if the specified IP address is allowed to access
     *    the specified function, {@link Boolean#FALSE} if it is disallowed
     *    access or <code>null</code> if there is no match.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     *
     * @throws ParseException
     *    if the specified IP address is malformed.
     */
    public Boolean isAllowed(String ip, String functionName)
    throws IllegalArgumentException, ParseException {
 
       // Check state
       if (_disposed) {
          String detail = "This AccessRuleFile is disposed.";
          Utils.logProgrammingError(detail);
          throw new IllegalStateException(detail);
       }
 
       // Check arguments
       MandatoryArgumentChecker.check("ip",           ip,
                                      "functionName", functionName);
 
       // Find a matching rule and see if the call is allowed
       int count = _rules == null ? 0 : _rules.length;
       Boolean allowed = null;
       for (int i = 0; i < count && allowed == null; i++) {
          allowed = _rules[i].isAllowed(ip, functionName);
       }
 
       return allowed;
    }
 
    /**
     * Disposes this access rule. All claimed resources are freed as much as
     * possible.
     *
     * <p>Once disposed, the {@link #isAllowed} method should no longer be
     * called.
     *
     * @throws IllegalStateException
     *    if {@link #dispose()} has been called previously
     *    (<em>since XINS 1.3.0</em>).
     */
    public void dispose() throws IllegalStateException {
 
       // Check state
       if (_disposed) {
          String detail = "This AccessRuleFile is already disposed.";
          Utils.logProgrammingError(detail);
          throw new IllegalStateException(detail);
       }
 
       // Dispose all children
       int count = _rules == null ? 0 : _rules.length;
       for (int i = 0; i < count; i++) {
          AccessRuleContainer rule = _rules[i];
          if (rule != null) {
             try {
                rule.dispose();
             } catch (Throwable exception) {
                Utils.logIgnoredException(AccessRuleFile.class.getName(),
                                          "dispose()",
                                          rule.getClass().getName(),
                                          "dispose()",
                                          exception);
             }
          }
       }
       _rules = null;
 
       // Stop the file watcher
       if (_fileWatcher != null) {
          try {
             _fileWatcher.end();
          } catch (Throwable exception) {
             Utils.logIgnoredException(AccessRuleFile.class.getName(),
                                       "dispose()",
                                       _fileWatcher.getClass().getName(),
                                       "end()",
                                       exception);
          }
          _fileWatcher = null;
       }
    }
 
    /**
     * Parses the specified ACL file and then applies it to this
     * <code>AccessRuleFile</code> instance.
     *
     * @param file
     *    the file to parse, cannot be <code>null</code>.
     *
     * @param interval
     *    the interval for checking the ACL file for modifications, in
     *    milliseconds.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || interval &lt; 0</code>.
     *
     * @throws ParseException
     *    if the file could not be parsed successfully.
     *
     * @throws IOException
     *    if there was an I/O error while reading from the file.
     */
    private void parseAndApply(String file, int interval)
    throws IllegalArgumentException, ParseException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("file", file);
       if (interval < 0) {
          throw new IllegalArgumentException("interval < 0");
       }
 
       // Buffer the input from the file
       BufferedReader reader = new BufferedReader(new FileReader(file));
 
       // Loop through the file, line by line
       List rules = new ArrayList(25);
       int lineNumber = 0;
       String nextLine = "";
       while(reader.ready() && nextLine != null) {
 
          // Read the next line and remove leading/trailing whitespace
         nextLine = TextUtils.trim(reader.readLine(), null);
 
          // Increase the line number (so it's 1-based)
          lineNumber++;
 
          // Skip comments and empty lines
         if (nextLine == null || nextLine.startsWith("#")) {
             // ignore
 
          // Plain access rule
          } else if (nextLine.startsWith("allow") || nextLine.startsWith("deny")) {
             rules.add(AccessRule.parseAccessRule(nextLine));
 
          // File reference
          } else if (nextLine.startsWith("file")) {
 
             // Make sure the file does not include itself
             if (nextLine.substring(5).equals(file)) {
                String detail = "The access rule file \""
                              + file
                              + "\" includes itself.";
                throw new ParseException(detail);
             }
             rules.add(new AccessRuleFile(nextLine, interval));
 
          // Otherwise: Incorrect line
          } else {
             String detail = "Failed to parse \""
                           + file
                           + "\", line #"
                           + lineNumber
                           + ": \""
                           + nextLine
                           + "\". Expected line to start with \"#\", "
                           + "\"allow\", \"deny\" or \"file\".";
             throw new ParseException(detail);
             // XXX: Log parsing problem?
          }
       }
 
       // Copy to the instance field
       _rules = (AccessRuleContainer[]) rules.toArray(ARC_ARRAY);
    }
 
    /**
     * Re-initializes the ACL rules for this file.
     */
    private void reinit() {
 
       // Dispose the current rules
       int count = _rules == null ? 0 : _rules.length;
       for (int i = 0; i < count; i++) {
          _rules[i].dispose();
       }
       _rules = null;
 
       // Parse the file and apply the rules
       try {
          parseAndApply(_file, _interval);
 
       // If the parsing fails, then log the exception
       } catch (Throwable exception) {
          Utils.logIgnoredException(AccessRuleFile.class.getName(),
                                    "reinit()",
                                    AccessRuleFile.class.getName(),
                                    "parseAndApply",
                                    exception);
          _rules = new AccessRuleContainer[0];
          // TODO: The framework re-initialization should fail
       }
    }
 
    public String toString() {
       return _asString;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Listener that reloads the ACL file if it changes.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     *
     * @since XINS 1.1.0
     */
    private final class FileListener
    extends Object
    implements FileWatcher.Listener {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>FileListener</code> object.
        */
       FileListener() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Callback method called when the configuration file is found while it
        * was previously not found.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileFound() {
          reinit();
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * found.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotFound() {
          Log.log_3400(_file);
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * modified.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotModified() {
          // empty
       }
 
       /**
        * Callback method called when the configuration file could not be
        * examined due to a <code>SecurityException</code>.
        * modified.
        *
        * <p>The implementation of this method does not perform any actions.
        *
        * @param exception
        *    the caught security exception, should not be <code>null</code>
        *    (although this is not checked).
        */
       public void securityException(SecurityException exception) {
          Log.log_3401(exception, _file);
       }
 
       /**
        * Callback method called when the configuration file is modified since
        * the last time it was checked.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileModified() {
          reinit();
       }
    }
 }
