 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.io.FileWatcher;
 import org.xins.common.text.ParseException;
 
 /**
  * A collection of access rules.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public class AccessRuleFile implements AccessRuleContainer {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns the next token in the descriptor.
     *
     * @param descriptor
     *   the original descriptor, for use in the {@link ParseException}, if
     *   necessary.
     *
     * @param tokenizer
     *   the {@link StringTokenizer} to retrieve the next token from.
     *
     * @return
     *   the next token, never <code>null</code>.
     *
     * @throws ParseException
     *   if <code>tokenizer.{@link StringTokenizer#hasMoreTokens() hasMoreTokens}() == false</code>.
     */
    private static String nextToken(String          descriptor,
                                    StringTokenizer tokenizer)
    throws ParseException {
 
       if (tokenizer.hasMoreTokens()) {
          return tokenizer.nextToken();
       } else {
          throw new ParseException("The string \""
                                 + descriptor
                                 + "\" is invalid as an access rule"
                                 + " descriptor. Too few tokens retrieved"
                                 + " from the descriptor.");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AccessRuleFile</code>.
     *
     * <p>If the specified interval is <code>0</code>, then no watching will be
     * performed.
     *
     * @param descriptor
     *    the access rule descriptor, the character string to parse, cannot be <code>null</code>.
     *    It also cannot be empty <code>(" ")</code>.
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
 
       StringTokenizer tokenizer = new StringTokenizer(descriptor," \t\n\r");
 
       String token = nextToken(descriptor, tokenizer);
       if (! "file".equals(token)) {
          throw new ParseException("First token of descriptor is \""
                                 + token
                                 + "\", instead of 'file'.");
       }
 
       String file = nextToken(descriptor, tokenizer);
 
       // First try parsing the file as it is
       IOException exception;
       try {
          parseAccessRuleFile(file, interval);
 
       // File not found
       } catch (FileNotFoundException fnfe) {
          String message = "File \""
                         + file
                        + "\" cannot be opened for reading."
          ParseException pe = new ParseException(message);
          ExceptionUtils.setCause(pe, ioe);
          throw pe;
 
       // I/O error reading from the file not found
       } catch (IOException ioe) {
          String message = "Cannot parse the file \""
                         + file
                        + "\" due to an I/O error."
          ParseException pe = new ParseException(message);
          ExceptionUtils.setCause(pe, ioe);
          throw pe;
       }
 
       // Create and start a file watch thread, if the interval is not zero
       if (interval > 0) {
          ACLFileListener aclFileListener = new ACLFileListener();
          _aclFileWatcher = new FileWatcher(file, interval, aclFileListener);
          _aclFileWatcher.start();
       }
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
     * File watcher for this ACL file.
     */
    private FileWatcher _aclFileWatcher;
 
    /**
     * The list of rules. Cannot be <code>null</code>.
     */
    private AccessRuleContainer[] _rules;
 
    /**
     * String representation of this object. Cannot be <code>null</code>.
     * XXX TODO
     */
    //private final String _asString;
 
 
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
     *    access or <code>null</code> if no match is found.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     *
     * @throws ParseException
     *    if the specified IP address is malformed.
     */
    public Boolean isAllowed(String ip, String functionName)
    throws IllegalArgumentException, ParseException {
 
       // TODO: If disposed, then throw a ProgrammingError
 
       // Check preconditions
       MandatoryArgumentChecker.check("ip",           ip,
                                      "functionName", functionName);
 
       for (int i = 0; i < _rules.length; i++) {
          Boolean allowed = _rules[i].isAllowed(ip, functionName);
          if (allowed != null) {
             return allowed;
          }
       }
 
       // Not found
       return null;
    }
 
    /**
     * Disposes this access rule. All claimed resources are freed as much as
     * possible.
     *
     * <p>Once disposed, the {@link #isAllowed} method should no longer be
     * called.
     */
    public void dispose() {
 
       // Close all the children
       if (_rules != null) {
          for (int i = 0; i < _rules.length; i++) {
             _rules[i].dispose();
          }
       }
       _aclFileWatcher.end();
       _aclFileWatcher = null;
    }
 
    /**
     * Parses an ACL file.
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
    private void parseAccessRuleFile(String file, int interval)
    throws IllegalArgumentException, ParseException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("file", file);
       if (interval < 0) {
          throw new IllegalArgumentException("interval < 0");
       }
 
       BufferedReader reader = new BufferedReader(new FileReader(file));
 
       List rules = new ArrayList(25);
       int lineNumber = 0;
       String nextLine = "";
       while(reader.ready() && nextLine != null) {
          nextLine = reader.readLine();
          lineNumber++;
          if (nextLine == null || nextLine.trim().equals("") || nextLine.startsWith("#")) {
 
             // Ignore comments and empty lines
          } else if (nextLine.startsWith("allow") || nextLine.startsWith("deny")) {
             rules.add(AccessRule.parseAccessRule(nextLine));
          } else if (nextLine.startsWith("file")) {
             if (nextLine.substring(5).equals(file)) {
                throw new ParseException("The access rule file  \"" + file + "\" cannot include itself.");
             }
             rules.add(new AccessRuleFile(nextLine, interval));
          } else {
 
             // Incorrect line
             // TODO: Logdoc
             throw new ParseException("Incorrect line \"" + nextLine + "\" in the file " + file + " at line " + lineNumber + ".");
          }
       }
       _rules = (AccessRuleContainer[])rules.toArray(new AccessRuleContainer[0]);
 
    }
 
    /**
     * Listener that reloads the ACL file if it changes.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     *
     * @since XINS 1.1.0
     */
    private final class ACLFileListener
    extends Object
    implements FileWatcher.Listener {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>ACLFileListener</code> object.
        */
       ACLFileListener() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Re-initializes the ACL rules for this file.
        */
       private void reinit() {
 
          // Close the children
          if (_rules != null) {
             for (int i = 0; i < _rules.length; i++) {
                _rules[i].dispose();
             }
          }
          try {
             parseAccessRuleFile(_file, _interval);
          } catch (Exception ioe) {
 
             // XXX log error
             _rules = new AccessRuleContainer[0];
          }
       }
 
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
