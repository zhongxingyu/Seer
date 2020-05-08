 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s):
  *  Copywrite in the anon-variable support:
  *  The Australian Commonwealth Government
  *  Department of Defense
  *  Developed by Netymon Pty Ltd
  *  under contract 4500507038
  *  contributed to the Mulgara Project under the 
  *    Mozilla Public License version 1.1
  *  per clause 4.1.3 and 4.1.4 of the above contract.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.itql;
 
 // Java 2 standard packages
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.rmi.server.UnicastRemoteObject;
 import java.rmi.RemoteException;
 import java.rmi.NoSuchObjectException;
 
 // Third party packages
 import org.apache.log4j.Logger; // Apache Log4J
 import org.jrdf.graph.*; // JRDF
 
 // Locally written packages
 
 // Automatically generated packages (SableCC)
 import org.mulgara.itql.analysis.*;
 import org.mulgara.itql.lexer.*;
 import org.mulgara.itql.node.*;
 import org.mulgara.itql.parser.*;
 import org.mulgara.query.*;
 import org.mulgara.query.rdf.*;
 import org.mulgara.rules.*;
 import org.mulgara.server.NonRemoteSessionException;
 import org.mulgara.server.Session;
 import org.mulgara.server.SessionFactory;
 import org.mulgara.server.driver.SessionFactoryFinder;
 import org.mulgara.server.driver.SessionFactoryFinderException;
 
 // emory util package
 import edu.emory.mathcs.util.remote.io.*;
 import edu.emory.mathcs.util.remote.io.server.impl.*;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipInputStream;
 
 /**
  * Interactive TQL (ITQL) command interpreter.
  * <p>
  * Responsible for the following :
  * - maintains connectivty between client and server;
  * - parsing and converting TQL requests to query objects for execution;
  * - Abstract layer for the {@link ItqlInterpreterBean} and {@link org.mulgara.connection.MulgaraConnection}s.
  * </p>
  *
  * @created 2001-08-21
  * @author Simon Raboczi
  * @author Tom Adams
  * @author Paul Gearon
  * @version $Revision: 1.12 $
  * @modified $Date: 2005/07/03 12:52:07 $ by $Author: pgearon $
  * @maintenanceAuthor $Author: pgearon $
  * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
  * @copyright &copy;2001-2004 <a href="http://www.tucanatech.com/">Tucana Technologies, Inc.</a>
  * @copyright &copy;2005 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 public class ItqlInterpreter extends DepthFirstAdapter {
 
   //
   // Constants
   //
   /** The rdf namespace prefix. */
   public static final String RDF = "rdf";
     
   /** The rdfs namespace prefix. */
   public static final String RDFS = "rdfs";
     
   /** The owl namespace prefix. */
   public static final String OWL = "owl";
     
   /** The mulgara namespace prefix. */
   public static final String MULGARA = "mulgara";
     
   /** The krule namespace prefix. */
   public static final String KRULE = "krule";
     
   /** The URI of the rdf namespace. */
   public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     
   /** The URI of the rdfs namespace. */
   public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     
   /** The URI of the owl namespace. */
   public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
     
   /** The URI of the mulgara namespace. */
   public static final String MULGARA_NS = "http://mulgara.org/mulgara#";
     
   /** The URI of the krule namespace. */
   public static final String KRULE_NS = "http://mulgara.org/owl/krule/#";
 
   /**
    * Get line separator.
    */
   private static final String EOL = System.getProperty("line.separator");
 
   /**
    * the logger
    */
   private static final Logger logger =
       Logger.getLogger(ItqlInterpreter.class.getName());
 
   /**
    * the message to display to the user if no results message was specififed by
    * a command
    */
   private final static String DEFAULT_RESULT_MESSAGE = "No results";
 
   /**
    * A constraint expression builder.
    */
   private ConstraintExpressionBuilder builder = new ConstraintExpressionBuilder(this);
 
   /**
    * Variable factory for this interpreter.
    */
   private VariableFactory variableFactory = new VariableFactoryImpl();
 
   /**
    * Lexer...
    */
   Lexer2 lexer = new Lexer2();
 
   //
   // Members
   //
 
   /**
    * the driver used to communicate with the database
    */
   private Session session = null;
 
   /**
    * A session provided by an external source. Also acts as a flag to indicate that
    * a session has been provided.
    */
   private Session providedSession = null;
 
   /**
    * the map from targets to aliases
    */
   private Map aliasMap = null;
 
   /**
    * a flag indicating the user has entered the quit command
    */
   private boolean quitRequested = false;
 
   /**
    * the last query parsed
    */
   private Query lastQuery = null;
 
   /**
    * the query should be executed when it has been parsed
    */
   private boolean executeQuery = true;
 
   /**
    * the results of the last command execution
    */
   private Answer lastAnswer = null;
 
   /**
    * the results of the last command execution
    */
   private String lastMessage = null;
 
   /**
    * the results of the last error
    */
   private ItqlInterpreterException lastError = null;
 
   /**
    * the time a command started
    */
   private long commandStartTime = -1L;
 
   /**
    * the log file to record all iTQL requests *
    */
   private PrintWriter itqlLog = null;
 
   /**
    * the location of the log iTQL file
    */
   private String itqlLogFile = null;
 
   /**
    * Map from security domain URIs to {@link Login} records.
    */
   private final Map loginMap = new HashMap();
 
   /**
    * The URI of the server of the current {@link #session}.
    */
   private URI serverURI = null;
 
   /**
    * The security domain for the session factory that produced
    * {@link #session}.
    */
   private URI securityDomainURI = null;
 
   /**
    * This will be <code>true</code> if {@link #autoCommit} is
    * <code>false</code> and the {@link #session} has been {@link #update}d.
    */
   private boolean transactionUpdated = false;
 
   /**
    * True if we are using a local session.
    */
   public boolean isLocal;
 
   //
   // Interpreter options
   //
 
   /**
    * the option to automatically make each method call transactional
    */
   private boolean autoCommit = true;
 
   /**
    * the option to enable the echoing of command output
    */
   private boolean echoOption = true;
 
   /**
    * the option to enable the display of command statistics
    */
   private boolean statisticsOption = false;
 
   /**
    * the option to enable the stopping of the interpreter on error in scripts
    */
   private boolean stoponerrorOption = false;
 
   /**
    * the option to enable the timing of commands
    */
   private boolean timeOption = false;
 
   /**
    * The next anonymous variable suffix
    */
   private int anonSuffix = 0;
 
   //
   // Constructors
   //
 
   /**
    * Creates a new ITQL command interpreter.
    *
    * @param aliasMap the map from targets to aliases, never <code>null</code>
    */
   public ItqlInterpreter(Map aliasMap) {
 
     // validate aliasMap parameter
     if (aliasMap == null) {
       throw new IllegalArgumentException("Null \"alias\" parameter");
     }
 
     // set members
     this.setAliasMap(aliasMap);
     this.setQuitRequested(false);
 
     // log the creation of this interpreter
     if (logger.isDebugEnabled()) {
       logger.debug("Itql interpreter created");
     }
 
     // is this session configured for logging.
     if (System.getProperty("itql.command.log") != null) {
       itqlLogFile = System.getProperty("itql.command.log");
       logger.info("iTQL command logging has been enabled.  Logging to " + System.getProperty("itql.command.log"));
     }
   }
 
   /**
    * Constructor internal to Mulgara that accepts a provided session.
    *
    * @param providedSession The session to use.
    * @param aliasMap the map from targets to aliases, never <code>null</code>
    */
   public ItqlInterpreter(Session providedSession, Map aliasMap) {
     this(aliasMap);
     // validate providedSession parameter
     if (providedSession == null) {
       throw new IllegalArgumentException("Null session parameter");
     }
 
     this.providedSession = providedSession;
     this.session = providedSession;
   }
 
 
   /**   
    * Set up default aliases.
    *
    * @return A map of aliases to their fully qualified names
    */        
   public static Map getDefaultAliases() {
     Map aliases = new HashMap();
     try {     
       aliases.put(RDF, new URI(RDF_NS));
       aliases.put(RDFS, new URI(RDFS_NS));
       aliases.put(OWL, new URI(OWL_NS));
       aliases.put(MULGARA, new URI(MULGARA_NS));
       aliases.put(KRULE, new URI(KRULE_NS));
     } catch (URISyntaxException e) {
       /* get those aliases which we could */
       logger.error("Error defining internal aliases: ", e);
     }
     return aliases;
   }  
 
 
   /**
    * @return whether there are unparsed tokens from an unterminated command
    */
   public boolean isComplete() {
 
     return lexer.leftoverTokenList.size() == 0;
   }
 
   /**
    * Returns true if a quit command has been entered. <p>
    *
    * Note. This method will return true after a quit command has been issued. It
    * is up to the user of this class to check for a quit after each command has
    * been issued, eg. </p> <pre>
    * while (!interpreter.isQuitRequested()) {
    *   String result = interpreter.executeCommand(command);
    * }
    * </pre>
    *
    * @return true if a quit command has been entered
    */
   public boolean isQuitRequested() {
 
     return this.quitRequested;
   }
 
   // toNode()
 
   /**
    * Returns the Exception Chain in a pretty fashion.
    *
    * @param e the throwable exception
    * @param preferredDepth the preferred depth to go into the exception to
    *   retrieve the root cause.  A depth of zero will chain all exceptions
    *   together.
    * @return String the Exception Chain in a pretty fashion
    */
   public String getCause(Throwable e, int preferredDepth) {
 
     // Keep getting the cause of the message until we reach preferred depth or
     // null cause.
     Throwable preferredException = e;
     int index = 0;
     while ( (preferredException != null) && (index != preferredDepth)) {
 
       // Pre-check next exception and increment index if it's not null.
       if (preferredException.getCause() != null) {
 
         index++;
       }
 
       // Get next exception
       preferredException = preferredException.getCause();
     }
 
     // If the preferredException is not null.
     if (preferredException != null) {
 
       e = preferredException;
     }
 
     // get the exception's message
     String message = e.getMessage();
 
     // we don't want nulls
     if (message == null) {
 
       message = "";
     }
 
     // end if
     // get the cause of the exception
     Throwable cause = e.getCause();
 
     // decend into it if we can
     if (cause != null) {
 
       // get the cause's message
       String causeMsg = this.getCause(cause, 0);
 
       // only add the cause's message if there was one
       if (causeMsg != null) {
 
         // format the class name
         String exceptionClassName = cause.getClass().getName();
         exceptionClassName =
             exceptionClassName.substring(exceptionClassName.lastIndexOf('.') +
                                          1);
         message += (EOL + "Caused by: (" + exceptionClassName + ") " +
                     causeMsg);
       }
 
       // end if
     }
 
     // end if
     // return the message
     return message;
   }
 
   /**
    * Returns the error of the last query. Methods overriding {@link
    * org.mulgara.itql.analysis.DepthFirstAdapter} are expected to set a
    * results message, even if that message is null.  The is for APIs, logging
    * and programmers not for users.
    *
    * @return the error of the last query, <code>null</code> if the query did not
    *      fail
    * @see #setLastError(Exception)
    */
   public ItqlInterpreterException getLastError() {
 
     return this.lastError;
   }
 
   /**
    * Returns the results of the last query. Methods overriding {@link
    * org.mulgara.itql.analysis.DepthFirstAdapter} are expected to set a
    * results message, even if that message is null.
    *
    * @return the results of the last query, <code>null</code> if the query
    *      failed
    * @see #setLastAnswer(Answer)
    */
   public Answer getLastAnswer() {
     return this.lastAnswer;
   }
 
   /**
    * Returns the results of the last command execution. Methods overriding
    * {@link org.mulgara.itql.analysis.DepthFirstAdapter} are expected to set
    * a results message, even if that message is null.  This is the user
    * understandable message.
    *
    * @return the results of the last command execution, null if the command did
    *      not set any message
    * @see #setLastMessage(String)
    */
   public String getLastMessage() {
 
     return this.lastMessage;
   }
 
   // ItqlInterpreter()
   //
   // Public API
   //
 
   /**
    * Executes the given ITQL command.
    *
    * @param command the command to execute in ITQL syntax
    * @throws ParserException if the syntax of the command is incorrect
    * @throws LexerException if the syntax of the command is incorrect
    * @throws IOException if the <var>command</var> cannot be paersed
    * @throws IllegalArgumentException if the <var>command</var> is
    *   <code>null</code>
    */
   public void executeCommand(String command) throws ParserException,
       LexerException, Exception {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // validate command parameter
     if ((command == null) || command.equals("")) {
       throw new IllegalArgumentException("Null \"command\" parameter");
     } // end if
 
     // log that we're going to execute the command
     if (logger.isDebugEnabled()) {
       logger.debug("Executing command " + command);
     }
 
     // log the iTQL command - system property itql.command.log must be set
     this.logItql(command);
 
     // Reset the variable incrementer in the query.
     variableFactory.reset();
 
     // push the command into the lexer
     lexer.add(command);
 
     // if the lexer saw terminators, parse the associated commands
     while (lexer.nextCommand()) {
 
       Start commandTree = null;
 
       // parse the command
       try {
         Parser parser = new Parser(lexer);
         commandTree = parser.parse();
       }
       catch (ParserException pe) {
 
         // let the user know the problem
         this.setLastError(pe);
         this.setLastAnswer(null);
         this.setLastMessage("Syntax error " + EOL + this.getCause(pe, 2));
         flush();
         throw pe;
       }
       catch (LexerException le) {
 
         // let the user know the problem
         this.setLastError(le);
         this.setLastAnswer(null);
         this.setLastMessage("Lexer exception " + EOL + this.getCause(le, 2));
         flush();
         throw le;
       }
 
       // execute the command
       try {
         commandTree.apply(this);
       }
       catch (Exception e) {
         flush();
         throw e;
       }
       catch (Error e) {
 
         flush();
         throw e;
       }
 
       if (logger.isDebugEnabled()) {
         logger.debug("Successfully executed command " + command);
       }
     }
   }
 
   /**
    * Discard any unparsed tokens.
    *
    */
   public void flush() {
 
     lexer.leftoverTokenList.clear();
   }
 
   // isQuitRequested()
 
   /**
    * Parse a string into a {@link Query}.
    *
    * @param queryString a string containing an ITQL query
    * @return the corresponding {@link Query} instance
    * @throws IOException if <var>queryString</var> can't be buffered.
    * @throws LexerException if <var>queryString</var> can't be tokenized.
    * @throws ParserException if <var>queryString</var> is not syntactic.
    */
   public Query parseQuery(String queryString) throws IOException,
       LexerException, ParserException {
 
     if (queryString == null) {
       throw new IllegalArgumentException("Null \"queryString\" parameter");
     }
 
     // clean up query
     queryString = queryString.trim();
     while (queryString.endsWith(";")) {
       queryString = queryString.substring(0, queryString.length() - 1);
     }
 
     // log that we're going to execute the command
     if (logger.isDebugEnabled()) {
       logger.debug("Parsing query \"" + queryString + "\"");
     }
 
     // execute the command
     Parser parser = new Parser(new Lexer(new PushbackReader(new StringReader(queryString), 256)));
     lastQuery = null;
     try {
       executeQuery = false;
       parser.parse().apply(this);
     }
     finally {
       executeQuery = true;
     }
 
     if (lastQuery == null) {
       throw new ParserException(null, "Parameter was not a query");
     }
 
     // return the results of the command
     return lastQuery;
   }
 
 
   //
   // Methods overridden from DepthFirstAdapter
   //
 
   /**
    * Hijacks the start of every command to perform actions on interpreter
    * options.
    *
    * @param node a command to be executed by the interpreter
    */
   public void inACommandStart(ACommandStart node) {
 
     // set the time of the start of the command
     this.setCommandStartTime(System.currentTimeMillis());
   }
 
   // inACommandStart()
 
   /**
    * Load the contents of an InputStream into a database/model.  The method assumes
    * the source to be RDF/XML.
    *
    * @param inputStream a locally supplied inputstream.
    * @param destinationURI destination model for the source data
    * @return number of rows inserted into the destination model
    * @throws QueryException if the data fails to load
    */
   public long load(InputStream inputStream, URI destinationURI)
                 throws QueryException {
 
      if ( inputStream == null ) {
        throw new IllegalArgumentException("Null InputStream supplied");
      }
      if ( destinationURI == null ) {
        throw new IllegalArgumentException("Null destination URI supplied");
      }
 
      URI dummySourceURI = null;
      try {
 
        // create dummy URI to force the server to assume RDF/XML inputStream
        dummySourceURI = new URI(Mulgara.NAMESPACE+"locally-sourced-inputStream.rdf");
 
       } catch ( URISyntaxException ex ) {};
 
       return this.load(inputStream, dummySourceURI, destinationURI);
 
    }
 
   /**
    * Load the contents of an InputStream or a URI into a database/model.
    *
    * @param inputStream a locally supplied inputstream.  Null assumes the
    * server will obtain the stream from the sourceURI.
    * @param sourceURI an idenifier for the source or inputstream.  The extension
    * will determine the type of parser to be used. ie. .rdf or .n3  When an inputStream
    * is supplied the server will not attempt to read the contents of the sourceURI
    * @param destinationURI destination model for the source data
    * @return number of rows inserted into the destination model
    * @throws QueryException if the data fails to load
    */
   public long load(InputStream inputStream, URI sourceURI, URI destinationURI)
                 throws QueryException {
 
     RemoteInputStreamSrvImpl srv = null;
 
     if ( sourceURI == null ) {
       throw new IllegalArgumentException("Null source URI supplied");
     }
     if ( destinationURI == null ) {
       throw new IllegalArgumentException("Null destination URI supplied");
     }
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Loading "+sourceURI+" into " +destinationURI );
     }
 
     long stmtCount = 0;
 
     // update the session
     this.updateSession(new ModelResource(destinationURI));
 
     if ( inputStream != null ) {
 
       //is the file/stream compressed?
       try {
         String path = (sourceURI == null) ? "" : sourceURI.toString();
         inputStream = adjustForCompression(path, inputStream);
       } catch (IOException ioException) {
 
         throw new QueryException("Could not get InputStream for compressed file.",
             ioException);
       }
 
       // open and wrap the inputstream
       srv = new RemoteInputStreamSrvImpl(inputStream);
 
       try {
 
         // prepare it for exporting
         UnicastRemoteObject.exportObject(srv);
 
       } catch ( RemoteException rex ) {
         throw new QueryException("Unable to create a remote InputStream to "+
                                  "load statements into "+ destinationURI, rex);
       }
 
       inputStream = new RemoteInputStream(srv);
     }
 
     try {
       stmtCount =
         this.getSession().setModel( inputStream,
                                     destinationURI, // model to redefine
                                     new ModelResource(sourceURI));
     } finally {
       if ( srv != null ) {
         try {
           UnicastRemoteObject.unexportObject(srv, false);
         } catch ( NoSuchObjectException ex ) {};
       }
     }
     update();
 
     // log that we've loaded the contents of the file
     if (logger.isDebugEnabled()) {
 
       logger.debug("Loaded " + stmtCount + " statements from " + sourceURI +
                    " into " + destinationURI);
     }
 
     // tell the user
     if (stmtCount > 0L) {
 
       this.setLastMessage("Successfully loaded " + stmtCount +
                           " statements from " + sourceURI + " into " +
                           destinationURI);
     }
     else {
 
       this.setLastMessage("WARNING: No valid RDF statements found in " +
                           sourceURI);
     } // end if
 
     return stmtCount;
 
   }
 
   /**
    * Backup all the data on the specified server or model to a local file.
    * The database is not changed by this method.
    *
    * @param sourceURI The URI of the server or model to backup.
    * @param destinationFile an non-existent file on the local file system to
    * receive the backup contents.
    * @throws QueryException if the backup cannot be completed.
    */
   public void backup(URI sourceURI, File destinationFile )
     throws QueryException {
 
     FileOutputStream fileOutputStream = null;
     try {
 
       fileOutputStream = new FileOutputStream(destinationFile);
     }
     catch (FileNotFoundException ex) {
       throw new QueryException("File "+destinationFile+" cannot be created "+
                            "for backup. ", ex);
     }
 
     this.backup( sourceURI, fileOutputStream  );
 
     // assume the server may not have closed the file.
     if ( fileOutputStream != null) {
       try {
         fileOutputStream .close();
       } catch (IOException ioe ) {};
     }
   }
 
   /**
    * Backup all the data on the specified server to an output stream.
    * The database is not changed by this method.
    *
    * @param sourceURI The URI of the server or model to backup.
    * @param outputStream The stream to receive the contents
    * @throws QueryException if the backup cannot be completed.
    */
   public void backup(URI sourceURI, OutputStream outputStream)
     throws QueryException {
 
      // open and wrap the outputstream
      RemoteOutputStreamSrvImpl srv = new RemoteOutputStreamSrvImpl(outputStream);
 
      // prepare it for exporting
      try {
        UnicastRemoteObject.exportObject(srv);
      }
      catch (RemoteException rex) {
        throw new QueryException("Unable to backup "+serverURI + " to "+
                                 "an output stream", rex);
      }
 
      outputStream = new RemoteOutputStream(srv);
 
      // set the server URI given a possible model
      this.setBackupServer( sourceURI );
 
      try {
        // perform the backup
        this.getSession().backup( sourceURI, outputStream );
 
      } finally {
        if ( outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException ioe ) {};
        }
        if ( srv != null ) {
          try {
            UnicastRemoteObject.unexportObject(srv, false);
          } catch ( NoSuchObjectException ex ) {};
        }
      }
      if ( outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException ioe ) {};
      }
   }
 
   /**
    * Restore all the data on the specified server. If the database is not
    * currently empty then the database will contain the union of its current
    * content and the content of the backup file when this method returns.
    *
    * @param inputStream A stream to obtain the restore from.
    * @param serverURI The URI of the server to restore.
    * @throws QueryException if the restore cannot be completed.
    */
   public void restore(InputStream inputStream, URI serverURI) throws QueryException {
 
     if ( inputStream == null ) {
       throw new IllegalArgumentException("Null input stream supplied");
     }
     if ( serverURI == null ) {
       throw new IllegalArgumentException("Null server URI supplied");
     }
 
     URI dummySourceURI = null;
     try {
 
       // create dummy URI for server identification messages
       dummySourceURI = new URI(Mulgara.NAMESPACE+"locally-sourced-inputStream.gz");
 
      } catch ( URISyntaxException ex ) {};
 
      this.restore(inputStream, dummySourceURI, serverURI);
 
   }
 
   /**
    * Restore all the data on the specified server. If the database is not
    * currently empty then the database will contain the union of its current
    * content and the content of the backup file when this method returns.
    *
    * @param inputStream a client supplied inputStream to obtain the restore
    *        content from. If null assume the sourceURI has been supplied.
    * @param serverURI The URI of the server to restore.
    * @param sourceURI The URI of the backup file to restore from.
    * @throws QueryException if the restore cannot be completed.
    */
   public void restore(InputStream inputStream, URI serverURI, URI sourceURI)
       throws QueryException {
 
     RemoteInputStreamSrvImpl srv = null;
 
     if ( sourceURI == null ) {
       throw new IllegalArgumentException("Null source URI supplied");
     }
     if ( serverURI == null ) {
       throw new IllegalArgumentException("Null server URI supplied");
     }
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Restoring "+sourceURI+" into " +serverURI );
     }
 
     // update the session
     this.updateSession(new ModelResource(serverURI));
 
     if ( inputStream != null ) {
 
       // open and wrap the inputstream
       srv = new RemoteInputStreamSrvImpl(inputStream);
 
       try {
 
         // prepare it for exporting
         UnicastRemoteObject.exportObject(srv);
 
       } catch ( RemoteException rex ) {
         throw new QueryException("Unable to restore an remote InputStream "+
                                  "into "+ serverURI, rex);
       }
 
       inputStream = new RemoteInputStream(srv);
     }
 
     try  {
 
       // perform the restore
       this.getSession().restore( inputStream, serverURI, sourceURI);
 
     } finally {
       if ( srv != null ) {
         try {
           UnicastRemoteObject.unexportObject(srv, false);
         } catch ( NoSuchObjectException ex ) {};
       }
     }
 
     update();
 
     // log that we've loaded the contents of the file
     if (logger.isDebugEnabled()) {
 
       logger.debug("Restored statements from " + sourceURI +
                    " into " + serverURI);
     }
   }
 
 
   /**
    * Hijacks the completion of every command to perform actions on interpreter
    * options.
    *
    * @param node a command to be executed by the interpreter
    */
   public void outACommandStart(ACommandStart node) {
 
     // hijack the last message to return the command time
     if (this.getTimeOption()) {
 
       // get the time the command took to execute
       long commandTimeMillis =
           System.currentTimeMillis() - this.getCommandStartTime();
 
       // convert the time to seconds
       double commandTimeSeconds =
           ( (double) commandTimeMillis) / ( (double) 1000L);
 
       // set a new message
       String commandTimeMsg = EOL + "Command execution time - " +
           commandTimeSeconds + " seconds";
       this.setLastMessage(this.getLastMessage() + commandTimeMsg);
     }
 
     // end if
   }
 
   // outACommandStart()
 
   /**
    * Displays help information to the user.
    *
    * @param node the help command
    */
   public void outAHelpCommand(AHelpCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing help command " + node);
     }
 
     // let the user know the help for the selected command
     this.setLastMessage(HelpPrinter.getHelp(node.getCommandPrefix()));
   }
 
   // outAHelpCommand()
 
   /**
    * Quits a session.
    *
    * @param node the quit command
    */
   public void outAQuitCommand(AQuitCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
       logger.debug("Processing quit command " + node);
     }
 
     // idicate that a quit command was received
     this.setQuitRequested(true);
 
     // let the user know that we're closing down
     this.setLastMessage("Quitting ITQL session");
   }
 
   // outAQuitCommand()
 
   /**
    * Commits a transaction.
    *
    * @param node the commit command
    */
   public void outACommitCommand(ACommitCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing commit command " + node);
     }
 
     try {
 
       // commit this transaction
       this.getSession().commit();
 
       // log that we've executed the query
       if (logger.isDebugEnabled()) {
 
         logger.debug("Successfully committed transaction");
       }
 
       // inform the user of the result
       this.setLastAnswer(null);
       this.setLastMessage("Successfully committed transaction");
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Unable to commit transaction." + EOL +
                           this.getCause(qe, 2));
       logger.warn("Unable to commit transaction", qe);
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to commit transaction:" + this.getCause(re, 0));
       logger.fatal("Failed to commit transaction", re);
     }
 
     // try-catch
   }
 
   // outAQuitCommand()
 
   /**
    * Rolls back a transaction.
    *
    * @param node the rollback command
    */
   public void outARollbackCommand(ARollbackCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing rollback command " + node);
     }
 
     try {
 
       // rollback any changes made
       this.getSession().rollback();
 
       // log that we've executed the query
       if (logger.isDebugEnabled()) {
 
         logger.debug("Successfully rolled back changes");
       }
 
       // inform the user of the result
       this.setLastAnswer(null);
       this.setLastMessage("Successfully rolled back changes");
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Unable to roll back changes." + EOL +
                           this.getCause(qe, 2));
       logger.warn("Unable to roll back changes", qe);
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Unable to roll back changes:" + this.getCause(re, 0));
       logger.fatal("Failed to roll back changes", re);
     }
   }
 
   /**
    * Executes a query.
    *
    * @param node the query command
    */
   public void outASelectCommand(ASelectCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
       logger.debug("Processing select command " + node);
     }
 
     try {
 
       // build the query
       Query query = this.buildQuery(node.getQuery());
 
       if (executeQuery) {
 
         // log that we've created the query and will execute it
         if (logger.isDebugEnabled()) {
           logger.debug("Executing query " + query);
           logger.debug("Executing query " + query.getVariableList());
         }
 
         updateSession(query.getModelExpression());
 
         // build the query
         Answer answer = this.getSession().query(query);
 
         // make sure we got an answer
         if (answer == null) {
           throw new QueryException("Invalid answer received");
         } // end if
 
         // log that we've executed the query
         if (logger.isDebugEnabled()) {
           logger.debug("Successfully executed query " + node);
         }
 
         // move to the first row
         answer.beforeFirst();
 
         // inform the user of the answer
         this.setLastAnswer(answer);
       }
     }
     catch (TuplesException te) {
 
       // let the user know the problem
       this.setLastError(te);
       this.setLastAnswer(null);
       this.setLastMessage("Couldn't answer select query." + EOL + this.getCause(te, 2) + EOL + te);
       logger.warn("Couldn't answer query", te);
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Couldn't answer select query." + EOL + this.getCause(qe, 2) + EOL + qe);
       logger.warn("Couldn't answer query", qe);
     }
     catch (URISyntaxException use) {
 
       // let the user know the problem
       this.setLastError(use);
       this.setLastAnswer(null);
       this.setLastMessage("Couldn't answer query: Invalid resource URI.");
       logger.warn("Invalid resource URI." + EOL + this.getCause(use, 0));
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Couldn't answer query :" + this.getCause(re, 0));
       logger.fatal("Failed to select statements", re);
     }
 
     // try-catch
   }
 
   // outASelectCommand()
 
   /**
    * Notify that the current session has been updated.
    */
   private void update() {
     transactionUpdated = !autoCommit;
   }
 
   /**
    * If the <code>FROM</code> clause of query refers to models in a different
    * server than the one we're pointed at, change the {@link #session}.
    *
    * @param modelExpression a <code>FROM</code> clause, never <code>null</code>
    * @throws QueryException if the <var>modelExpression</var> can't be resolved
    *   by any single server
    */
   private void updateSession(ModelExpression modelExpression) throws
       QueryException {
     assert modelExpression != null;
 
     if (providedSession != null) {
       session = providedSession;
       return;
     }
 
     URI databaseURI = null;
 
     // Check to see that we're aimed at the right server
     Set databaseURISet = modelExpression.getDatabaseURIs();
     assert databaseURISet != null;
     if (logger.isDebugEnabled()) {
       logger.debug(
           "Current server is " + serverURI + ", updating for " + databaseURISet
           );
     }
     switch (databaseURISet.size()) {
       case 0:
 
         // Query presumably contains only URLs -- any server can deal with
         // this, including the current one
         if (session == null) {
           try {
             serverURI = SessionFactoryFinder.findServerURI();
 
             SessionFactory sessionFactory =
                 SessionFactoryFinder.newSessionFactory(serverURI, !isLocal);
 
             // Switch the underlying session to point at the required server
             setSession(sessionFactory.newSession(),
                        sessionFactory.getSecurityDomain());
           }
           catch (SessionFactoryFinderException e) {
             throw new QueryException("Unable to connect to a server", e);
           }
           catch (NonRemoteSessionException e) {
             throw new QueryException("Error connecting to the local server", e);
           }
         }
         assert session != null;
         break;
 
       case 1:
 
         // Query must go to a particular server
         databaseURI = (URI) databaseURISet.iterator().next();
         assert databaseURI != null;
         if (!databaseURI.equals(serverURI)) {
           setServerURI(databaseURI);
         }
         assert databaseURI.equals(serverURI);
         break;
 
       default:
 
         // Query must be distributed between multiple servers
         assert databaseURISet.size() > 1;
 
         if (!databaseURISet.contains(serverURI)) {
           // We're not even aimed at one of the servers involved in this query,
           // so choose one arbitrarily and switch to it
           databaseURI = (URI) databaseURISet.iterator().next();
           assert databaseURI != null;
           setServerURI(databaseURI);
           assert databaseURI.equals(serverURI);
         }
 
         // We're now connected to one of the servers involved in this query.
         assert databaseURISet.contains(serverURI);
         break;
     }
   }
 
   public void setServerURI(URI databaseURI) throws QueryException {
 
     // Short-circuit evaluation if the new value equals the old value
     if (serverURI == null) {
       if (databaseURI == null) {
         return;
       }
     }
     else {
       if (serverURI.equals(databaseURI)) {
         return;
       }
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Changing server URI from " + serverURI + " to " +
                    databaseURI);
     }
 
     // connect to local server via EmbeddedMulgaraServer Database which is an instance of SessionFactory
     // - get a session factory from that singleton database
     // Switch the underlying session to point at the required server
     if (databaseURI == null) {
       setSession(null, null);
     }
     else {
       if (providedSession != null) {
         if (session != providedSession) {
           session = providedSession;
         }
       } else {
         try {
           if (logger.isDebugEnabled()) {
             logger.debug("Finding session factory for " + databaseURI);
           }
 
           SessionFactory sessionFactory =
               SessionFactoryFinder.newSessionFactory(databaseURI, !isLocal);
 
           if (logger.isDebugEnabled()) {
             logger.debug("Found " + sessionFactory.getClass() +
                          " session factory, obtaining session with " +
                          databaseURI);
           }
 
           Session session = sessionFactory.newSession();
 
           if (logger.isDebugEnabled()) {
             logger.debug("Obtained session with " + databaseURI);
           }
 
           setSession(session, sessionFactory.getSecurityDomain());
         }
         catch (SessionFactoryFinderException e) {
           throw new QueryException("Unable to reconnect to " + databaseURI, e);
         }
         catch (NonRemoteSessionException e) {
           throw new QueryException("Error connecting to the local server", e);
         }
       }
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Changed server URI from " + serverURI + " to " +
                    databaseURI);
     }
 
     serverURI = databaseURI;
   }
 
   /**
    * Returns the session to use to communicate with the specified Mulgara server.
    *
    * @param serverURI URI Server to get a Session for.
    * @return the session to use to communicate with the specified Mulgara server
    */
   public Session getSession(URI serverURI) throws QueryException {
 
     //set up session to the server
     this.updateSession(new ModelResource(serverURI));
     return this.getSession();
   }
 
   /**
    * Substitutes the user associated with this session.
    *
    * @param node the su command
    */
   public void outASuCommand(ASuCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing su command " + node);
     }
 
     URI newSecurityDomainURI = toURI(node.getResource());
 
     loginMap.put(newSecurityDomainURI,
                  new Login(node.getUser().getText(),
                            node.getPassword().getText().toCharArray()));
 
     if (newSecurityDomainURI.equals(securityDomainURI)) {
       try {
         this.getSession().login(securityDomainURI,
                                 node.getUser().getText(),
                                 node.getPassword().getText().toCharArray());
       }
       catch (QueryException e) {
         this.setLastError(e);
         this.setLastMessage("Could not present credential to " +
                             securityDomainURI + ": " + e.getMessage());
       }
     }
 
     this.setLastMessage("Credential presented");
 
   } // outASuCommand()
 
   /**
    * Associates an alias prefix with a target.
    *
    * @param node the alias command
    */
   public void outAAliasCommand(AAliasCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing alias command " + node);
     }
 
     // get the prefix and target
     String aliasPrefix = node.getPrefix().getText();
     String aliasTarget = node.getTarget().getText();
 
     try {
 
       // convert the target to a URI
       URI aliasTargetURI = new URI(aliasTarget);
 
       // log the conversion
       if (logger.isDebugEnabled()) {
 
         logger.debug("Converted " + aliasTarget + " to URI " + aliasTargetURI);
       }
 
       // add the alias pair to the map
       this.addAliasPair(aliasPrefix, aliasTargetURI);
 
       // log that we've added the pair to the map
       if (logger.isDebugEnabled()) {
 
         logger.debug("Aliased " + aliasTarget + " as " + aliasPrefix);
       }
 
       // tell the user
       this.setLastMessage("Successfully aliased " + aliasTarget + " as " +
                           aliasPrefix);
     }
     catch (URISyntaxException use) {
 
       // log the failed URI creation
       logger.warn("Unable to create URI from alias target " + aliasTarget);
       this.setLastMessage(aliasTarget + " is not a valid URI");
       this.setLastError(use);
       this.setLastAnswer(null);
     }
 
     // try-catch
   }
 
   // outAAliasCommand()
 
   /**
    * Applies a set of rules in a model to data in another model.
    *
    * @param node the alias command
    */
   public void outAApplyCommand(AApplyCommand node) {
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
       logger.debug("Processing apply command " + node);
     }
 
     // get the rule model and target model
     URI ruleModel = toURI(node.getRules());
     URI baseModel = toURI(node.getBase());
     Token dest = node.getDestination();
     URI destModel = (dest == null) ? baseModel : toURI(dest);
 
     try {
       // update the session
       this.updateSession(new ModelResource(ruleModel));
 
       // get the structure from the rule model
       RulesRef rules = this.getSession().buildRules(ruleModel, baseModel, destModel);
 
       // move the session on to the target
       this.updateSession(new ModelResource(destModel));
 
       // create the model
       this.getSession().applyRules(rules);
       update();
 
       this.setLastMessage("Successfully applied " + ruleModel +
                           " to " + baseModel + (dest == null ? "" : " => " + destModel));
 
     } catch (Exception e) {
 
       // let the user know the problem
       this.setLastError(e);
       this.setLastAnswer(null);
       int depth = (e instanceof QueryException) ? 2 : 0;
       this.setLastMessage("Could not run " + ruleModel + " on " + baseModel + EOL +
                           this.getCause(e, depth));
       if (e instanceof QueryException) {
         logger.warn("Failed to run " + ruleModel + " on " + baseModel, e);
       } else {
         logger.fatal("Failed to run rules:", e);
       }
     }
 
   }
 
   /**
    * Creates a new database/model.
    *
    * @param node the create command
    */
   public void outACreateCommand(ACreateCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing create command " + node);
     }
 
     // get the name of the model to create
     URI modelURI = toURI(node.getModel());
 
     // get the type of model to create; default to mulgara:Model is unspecified
     URI modelTypeURI = (node.getModelType() == null)
         ? Session.MULGARA_MODEL_URI
         : toURI(node.getModelType());
 
     try {
 
       // log that we're asking the driver to create the resource
       if (logger.isDebugEnabled()) {
 
         logger.debug("Creating new model " + modelURI);
       }
 
       modelURI = getCanonicalUriAlias(modelURI);
 
       if (logger.isDebugEnabled()) {
         logger.debug("Model is alias for " + modelURI);
       }
 
       // ensure that we're aimed at the server this model will reside upon
       this.updateSession(new ModelResource(modelURI));
 
       // create the model
       this.getSession().createModel(modelURI, modelTypeURI);
       update();
 
       // log that we've created the database
       if (logger.isDebugEnabled()) {
 
         logger.debug("Created new model " + modelURI + " of type " +
                      modelTypeURI);
       }
 
       // tell the user
       this.setLastMessage("Successfully created model " + modelURI);
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not create " + modelURI + EOL +
                           this.getCause(qe, 2));
       logger.warn("Failed to create " + modelURI + " as type " + modelTypeURI,
                   qe);
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to create model:" + this.getCause(re, 0));
       logger.fatal("Failed to create model", re);
     }
 
     // try-catch
   }
 
   // outACreateCommand()
 
   /**
    * Drop (delete) a database/model.
    *
    * @param node the drop command
    */
   public void outADropCommand(ADropCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing drop command " + node);
     }
 
     // get the name of the database/model to drop
     URI resourceURI = toURI(node.getResource());
 
     try {
       // ensure we have a session to the server where the model resides
       this.updateSession(new ModelResource(resourceURI));
 
       // drop the resource
       this.getSession().removeModel(resourceURI);
       update();
 
       // log that we've dropped the database
       if (logger.isDebugEnabled()) {
 
         logger.debug("Dropped model " + resourceURI);
       }
 
       // tell the user
       this.setLastMessage("Successfully dropped model " + resourceURI);
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not remove " + resourceURI + EOL +
                           this.getCause(qe, 2));
       logger.warn("Failed to remove " + resourceURI, qe);
     }
     catch (RuntimeException re) {
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to remove model:" + this.getCause(re, 0));
       logger.fatal("Failed to remove model", re);
     }
 
     // try-catch
   }
 
   // outADropCommand()
 
   /**
    * Load the contents of a file into a database/model.
    *
    * @param node the load command
    */
   public void outALoadCommand(ALoadCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     RemoteInputStream remoteInputStream = null;
     RemoteInputStreamSrvImpl srv = null;
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing load command " + node);
     }
 
     // get constituents of the load command
     URI sourceURI = toURI(node.getSource());
     URI destinationURI = toURI(node.getDestination());
 
     try {
 
       long stmtCount = 0;
 
       // update the session
       this.updateSession(new ModelResource(destinationURI));
 
 
       // are we loading the file locally from the client?
       if ( node.getLocality() != null &&
            (node.getLocality() instanceof ALocalLocality) ) {
 
         if ( logger.isInfoEnabled() ) {
           logger.info("loading local resource : " + sourceURI );
         }
 
         try {
 
           //open an InputStream
           InputStream inputStream = sourceURI.toURL().openStream();
 
           //is the file/stream compressed?
           String path = (sourceURI == null) ? "" : sourceURI.toString();
           inputStream = adjustForCompression(path, inputStream);
 
           // open and wrap the inputstream
           srv = new RemoteInputStreamSrvImpl(inputStream);
 
           // prepare it for exporting
           UnicastRemoteObject.exportObject(srv);
 
           remoteInputStream = new RemoteInputStream(srv);
 
           // modify the database
           stmtCount =
               this.getSession().setModel(remoteInputStream,
                                          destinationURI, // model to redefine
                                          new ModelResource(sourceURI));
         }
         catch (IOException ex) {
           logger.error("Error attempting to load : " + sourceURI, ex);
           throw new QueryException("Error attempting to load : " + sourceURI, ex);
         } finally {
           if ( srv != null ) {
             try {
               UnicastRemoteObject.unexportObject(srv, false);
             } catch ( NoSuchObjectException ex ) {};
           }
         }
       }
       else {
 
         if ( logger.isInfoEnabled() ) {
           logger.info("loading remote resource : " + sourceURI );
         }
 
         // modify the database using a remote file located on the server
         // default behaviour
 
         stmtCount =
             this.getSession().setModel(destinationURI, // model to redefine
                                        new ModelResource(sourceURI));
       }
       update();
 
       // log that we've loaded the contents of the file
       if (logger.isDebugEnabled()) {
 
         logger.debug("Loaded " + stmtCount + " statements from " + sourceURI +
                      " into " + destinationURI);
       }
 
       // tell the user
       if (stmtCount > 0L) {
 
         this.setLastMessage("Successfully loaded " + stmtCount +
                             " statements from " + sourceURI + " into " +
                             destinationURI);
       }
       else {
 
         this.setLastMessage("WARNING: No valid RDF statements found in " +
                             sourceURI);
       } // end if
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not load " + sourceURI + " into " +
                           destinationURI + EOL + this.getCause(qe, 2));
       logger.warn("Failed to load " + sourceURI + " into " + destinationURI,
                   qe);
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to load statements:" + this.getCause(re, 0));
       logger.fatal("Failed to load statements", re);
     }
     finally {
 
       // close the inputstream in-case the server was not able to
       // complete the task.
       if ( remoteInputStream != null ) {
         try {
           remoteInputStream.close();
         } catch ( Exception ex ) {};
       }
 
     }
     // try-catch
 
   }
 
   // outALoadCommand()
 
   /**
    * Executes an iTQL script.
    *
    * @param node the execute command
    */
   public void outAExecuteCommand(AExecuteCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing execute command " + node);
     }
 
     // get the name of the script to execute
     String resource = node.getResource().getText();
 
     // keep a record of the line number
     int line = 0;
 
     try {
 
       // convert the script to a URL
       URL scriptURL = new URL(resource);
 
       // log that we're executing a script
       if (logger.isDebugEnabled()) {
 
         logger.debug("Executing  script " + scriptURL);
       }
 
       // create a buffer to hold the results in
       StringBuffer resultsMsg = new StringBuffer();
 
       // create a reader to read the contents of the script
       BufferedReader scriptIn =
           new BufferedReader(new InputStreamReader(scriptURL.openStream()));
 
       // execute the script!
       String command = scriptIn.readLine();
 
       while (command != null) {
 
         // increment the line number
         line++;
 
         if (!command.equals("")) {
 
           // execute the command
           executeCommand(command);
         }
 
         // end if
         // get the next command
         command = scriptIn.readLine();
       }
 
       // end if
       // tell the user
       resultsMsg.append("Completed execution of script " + resource);
       this.setLastMessage(resultsMsg.toString());
     }
     catch (ParserException pe) {
 
       // let the user know the problem
       this.setLastError(pe);
       this.setLastAnswer(null);
       this.setLastMessage("Syntax error in script (line " + line + "): " +
                           pe.getMessage());
       logger.warn("Unable to execute script - " + resource + EOL +
                   this.getCause(pe, 0));
     }
     catch (LexerException le) {
 
       // let the user know the problem
       this.setLastError(le);
       this.setLastAnswer(null);
       this.setLastMessage("Syntax error in script (line " + line + "): " +
                           le.getMessage());
       logger.warn("Unable to execute script - " + resource + EOL +
                   this.getCause(le, 0));
     }
     catch (MalformedURLException mue) {
 
       // let the user know the problem
       this.setLastError(mue);
       this.setLastAnswer(null);
       this.setLastMessage("Could not execute script: Invalid script URL.");
       logger.warn("Invalid script source URL." + EOL + this.getCause(mue, 0));
     }
     catch (Exception e) {
 
       // let the user know the problem
       this.setLastError(e);
       this.setLastAnswer(null);
       this.setLastMessage("Could not execute script." + EOL +
                           this.getCause(e, 0));
       logger.error("Unable to execute script - " + resource + EOL +
                    this.getCause(e, 0));
     }
     // try-catch
   }
 
   // outAExecuteCommand()
 
   /**
    * Inserts a triple, model, database or the results of a query into a model or
    * database.
    *
    * @param node the insert command
    */
   public void outAInsertCommand(AInsertCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing insert command " + node);
     }
 
     // get the resource we're inserting data into
     URI resourceURI = toURI(node.getResource());
 
     try {
 
       // log that we're inserting the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Inserting statements into " + resourceURI);
       }
 
       // update the session
       this.updateSession(new ModelResource(resourceURI));
 
       // insert the statements into the model
       insertStatements(resourceURI, node.getTripleFactor());
       update();
 
       // log that we've inserted the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Completed inserting statements into " + resourceURI);
       }
 
       // tell the user
       this.setLastMessage(
           "Successfully inserted statements into " + resourceURI
           );
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not insert statements into " + resourceURI +
                           EOL + this.getCause(qe, 2));
       logger.warn("Failed to insert statements into " + resourceURI, qe);
     }
     catch (URISyntaxException use) {
 
       // let the user know the problem
       this.setLastError(use);
       this.setLastAnswer(null);
       this.setLastMessage("Could not insert into resource: Invalid resource " +
                           "URI.");
       logger.warn("Invalid resource URI." + EOL + this.getCause(use, 0));
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to insert statements:" + this.getCause(re, 0));
       logger.fatal("Failed to insert statements", re);
     }
   }
 
   // outAInsertCommand()
 
   /**
    * Deletes a triple, model, database or the results of a query from a model or
    * database.
    *
    * @param node the delete command
    */
   public void outADeleteCommand(ADeleteCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing delete command " + node);
     }
 
     // get the resource we're deleting data from
     URI resourceURI = toURI(node.getResource());
 
     try {
 
       // log that we're deleting the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Deleting statements from " + resourceURI);
       }
 
       // update the session
       this.updateSession(new ModelResource(resourceURI));
 
       // delete the statements from the model
       deleteStatements(resourceURI, node.getTripleFactor());
       update();
 
       // log that we've inserted the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Completed deleting statements from " + resourceURI);
       }
 
       // tell the user
       this.setLastMessage(
           "Successfully deleted statements from " + resourceURI
           );
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not delete statements from " + resourceURI +
                           EOL + this.getCause(qe, 2));
       logger.warn("Failed to delete statements from " + resourceURI, qe);
     }
     catch (URISyntaxException use) {
 
       // let the user know the problem
       this.setLastError(use);
       this.setLastAnswer(null);
       this.setLastMessage("Could not delete from resource: Invalid resource " +
                           "URI.");
       logger.warn("Invalid resource URI." + EOL + this.getCause(use, 0));
     }
     catch (RuntimeException re) {
 
       // let the user know the problem
       this.setLastError(re);
       this.setLastAnswer(null);
       this.setLastMessage("Failed to delete statements:" + this.getCause(re, 0));
       logger.fatal("Failed to delete statements", re);
     }
 
     // try-catch
   }
 
   // outADeleteCommand()
 
   /**
    * Sets an interpreter property.
    *
    * @param node the set command
    */
   public void outASetCommand(ASetCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing set command " + node);
     }
 
     // get the option to set
     PSetOption option = node.getSetOption();
 
     // log that we've got the option
     if (logger.isDebugEnabled()) {
 
       logger.debug("Found option " + option);
     }
 
     // get the value
     PSetOptionMode optionMode = node.getSetOptionMode();
     boolean optionSet = false;
 
     if (optionMode instanceof AOffSetOptionMode) {
 
       optionSet = false;
     }
     else {
 
       optionSet = true;
     } // end if
 
     // set the option
     if (option instanceof ATimeSetOption) {
 
       // set the time option
       this.setTimeOption(optionSet);
 
       // return the user a message
       this.setLastMessage("Command timing is " + (optionSet ? "on" : "off"));
     }
     else if (option instanceof AAutocommitSetOption) {
 
       /* Do not assume this.  As the server may have
          encountered an error.
       if (optionSet == autoCommit) {
         if (logger.isDebugEnabled()) {
           logger.debug("Autocommit option is already " + autoCommit);
         } // end if
         return;
       }
       */
 
       try {
 
         // log that we got a autocommit option
         if (logger.isDebugEnabled()) {
 
           logger.debug("Found autocommit option, setting to " +
                        (optionSet ? "on" : "off"));
         } // end if
 
         // set the auto commit status
         if (session != null) {
           this.getSession().setAutoCommit(optionSet);
          update();
         }
         autoCommit = optionSet;
 
         if (logger.isDebugEnabled()) {
           logger.debug("Set autocommit to " + (optionSet ? "on" : "off"));
         }
 
         // return the user a message
         this.setLastMessage("Auto commit is " + (optionSet ? "on" : "off"));
       }
       catch (QueryException qe) {
 
         // let the user know the problem
         this.setLastError(qe);
         this.setLastAnswer(null);
         this.setLastMessage("Unable to set interpreter option" + EOL +
                             this.getCause(qe, 2));
         logger.warn("Unable to set interpreter property", qe);
       }
 
       // try-catch
     }
     else {
 
       // this should never get through the parser, if it does it probably
       // means the grammar has been updated
       this.setLastMessage("Unknown interpreter option");
     }
 
     // end if
     // log the option setting
     if (logger.isDebugEnabled()) {
 
       logger.debug(option + "has been set to " + optionSet);
     }
   }
 
   // outASetCommand()
 
   /**
    * Backs up the contents of a server to a local or remote file.
    *
    * @param node the backup command
    */
   public void outABackupCommand(ABackupCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     RemoteOutputStream outputStream = null;
     RemoteOutputStreamSrvImpl srv = null;
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing backup command " + node);
     }
 
     // get the server we'll be backing up
     URI sourceURI = toURI(node.getSource());
 
     // get the resource we're writing to
     URI destinationURI = toURI(node.getDestination());
 
     try {
 
       // set the server uri given a possible model
       this.setBackupServer(sourceURI);
 
       // are we backing up a file locally to the client?
       if ( node.getLocality() != null &&
            (node.getLocality() instanceof ALocalLocality) ) {
 
         if ( logger.isInfoEnabled() ) {
           logger.info("backing up local resource : " + sourceURI );
         }
 
         try {
 
           // open and wrap the outputstream
           srv = new RemoteOutputStreamSrvImpl(
                   new FileOutputStream(destinationURI.getPath()));
 
           // prepare it for exporting
           UnicastRemoteObject.exportObject(srv);
 
           outputStream = new RemoteOutputStream(srv);
 
         }
         catch (IOException ex) {
           logger.error("Error attempting to backing up : " + sourceURI, ex);
         }
 
         try {
 
           // back it up to the local file system
           this.getSession().backup(sourceURI, outputStream);
 
         } finally {
 
           // ensure the stream is closed in case the server was not able
           // to close it.
           if ( outputStream != null ) {
             try {
               outputStream.close();
             } catch ( IOException ioex ) {};
             if ( srv != null ) {
               try {
                 UnicastRemoteObject.unexportObject(srv, false);
               } catch ( NoSuchObjectException ex ) {};
             }
           }
         }
 
       }
       else {
 
         // back it up via the server
         this.getSession().backup(sourceURI, destinationURI);
       }
 
       // log that we've inserted the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Completed backing up " + sourceURI +
                      " to " + destinationURI);
       }
 
       // tell the user
       this.setLastMessage("Successfully backed up " + sourceURI +
                           " to " + destinationURI + ".");
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not backup " + sourceURI + " to " +
                           destinationURI + EOL + this.getCause(qe, 2) + ".");
       logger.warn("Failed to backup server " + sourceURI + " to " +
                   destinationURI,
                   qe);
     }
 
     // try-catch
   }
 
   // outABackupCommand()
 
   /**
    * Restores the contents of a server from a file.
    *
    * @param node the restore command
    */
   public void outARestoreCommand(ARestoreCommand node) {
 
     this.setLastError(null);
     this.setLastAnswer(null);
     this.setLastMessage("");
 
     RemoteInputStream inputStream = null;
     RemoteInputStreamSrvImpl srv = null;
 
     // log the command
     if (logger.isDebugEnabled()) {
 
       logger.debug("Processing restore command " + node);
     }
 
     // get the server we'll be restoring to
     URI destinationURI = toURI(node.getDestination());
 
     // get the resource we're reading from
     URI sourceURI = toURI(node.getSource());
 
     try {
 
       // log that we're backing up a server
       if (logger.isDebugEnabled()) {
 
         logger.debug("Restoring server " + destinationURI + " from " +
                      sourceURI);
       }
 
       setServerURI(destinationURI);
 
       // are we loading the file locally from the client?
       if ( node.getLocality() != null &&
            (node.getLocality() instanceof ALocalLocality) ) {
 
         if ( logger.isInfoEnabled() ) {
           logger.info("restoring local resource : " + sourceURI );
         }
 
         try {
 
           // open and wrap the inputstream
           srv = new RemoteInputStreamSrvImpl(sourceURI.toURL().openStream());
 
           // prepare it for exporting
           UnicastRemoteObject.exportObject(srv);
 
           inputStream = new RemoteInputStream(srv);
 
           // modify the database
           this.getSession().restore( inputStream, destinationURI, sourceURI);
         }
         catch (IOException ex) {
           logger.error("Error attempting to restore : " + sourceURI, ex);
           throw new QueryException("Error attempting to restore : " + sourceURI, ex);
         }
         finally {
           if ( srv != null ) {
             try {
               UnicastRemoteObject.unexportObject(srv, false);
             } catch ( NoSuchObjectException ex ) {};
           }
         }
       }
       else {
 
         // restore it from the server
         this.getSession().restore(destinationURI, sourceURI);
 
       }
 
       update();
 
       // log that we've inserted the statments
       if (logger.isDebugEnabled()) {
 
         logger.debug("Completed restoring " + destinationURI + " from " +
                      sourceURI);
       }
 
       // tell the user
       this.setLastMessage("Successfully restored " + destinationURI + " from " +
                           sourceURI);
     }
     catch (QueryException qe) {
 
       // let the user know the problem
       this.setLastError(qe);
       this.setLastAnswer(null);
       this.setLastMessage("Could not restore " + destinationURI + " from " +
                           sourceURI + EOL + this.getCause(qe, 2));
       logger.warn("Failed to restore server " + destinationURI + " from " +
                   sourceURI, qe);
     }
     finally {
 
       // close the inputstream in-case the server was not able to
       // complete the task.
       if ( inputStream != null ) {
         try {
          inputStream.close();
         }
         catch ( Exception ex ) {};
       }
     }
   }
 
   /**
    * Sets the error of the last query. Methods overriding <code>DepthFirstAdapter</code>
    * are expected to set a result for successful queries, and <code>null</code>
    * for failed ones.
    *
    * @param lastError the exception of the last command execution
    */
   void setLastError(Exception lastError) {
 
     // carefully set the answer by value rather than reference
     this.lastError =
         (lastError == null) ? null : new ItqlInterpreterException(lastError);
   }
 
   /**
    * Sets the results of the last command execution. Methods overriding <code>DepthFirstAdapter</code>
    * are expected to set a results message, even if that message is null.
    *
    * @param lastMessage the results of the last command execution
    */
   void setLastMessage(String lastMessage) {
 
     this.lastMessage = lastMessage;
   }
 
   // setLastMessage()
 
   /**
    * Returns the driver used to communicate with the database.
    *
    * @return the driver used to communicate with the database
    */
   Session getSession() throws QueryException {
 
     if (providedSession != null) {
       if (session != providedSession) {
         session = providedSession;
       }
     }
 
     if (this.session == null) {
       throw new QueryException("Null session");
     }
     return this.session;
   }
 
   // getCause()
 
   /**
    * Sets the option to enable the timing of commands.
    *
    * @param timeOption the option to enable the timing of commands
    */
   private void setTimeOption(boolean timeOption) {
 
     this.timeOption = timeOption;
   }
 
   // getTimeOption()
 
   /**
    * Sets the time a command started.
    *
    * @param commandStartTime the time a command started
    */
   private void setCommandStartTime(long commandStartTime) {
 
     this.commandStartTime = commandStartTime;
   }
 
   // getCommandStartTime()
 
   /**
    * Sets the driver used to communicate with the database.
    *
    * @param session  the driver used to communicate with the database
    * @param securityDomainURI  the security domain of the <var>session</var>
    * @throws QueryException if the state of the original session couldn't be
    *   propagated to the new <var>session</var>.
    */
   void setSession(Session session, URI securityDomainURI) throws QueryException {
     // override all sessions with the provided one.
     if (providedSession != null) {
       session = providedSession;
       return;
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug(
           "Setting session to " + session + ", security domain " +
           securityDomainURI
           );
     }
 
     // Short-circuit reassignment of the same session
     if (this.session == session) {
       if (logger.isDebugEnabled()) {
         logger.debug("Didn't need to set session");
       }
       return;
     }
 
     // We can't change sessions if we're in the midst of a transaction which
     // has already updated the server
     if (transactionUpdated) {
       throw new QueryException(
           "Can't access more than one server in a transaction"
           );
     }
 
     // Propagate the autoCommit property to the new session
     if (!autoCommit) {
       if (logger.isDebugEnabled()) {
         logger.debug("Setting autocommit to " + autoCommit + " on " + serverURI);
       }
       if (session != null) {
         session.setAutoCommit(autoCommit);
       }
       if (this.session != null) {
         this.session.setAutoCommit(true); // TODO: this requires a compensating
         //       transaction in case of
         //       failure
       }
     }
 
     // Propagate the login to the new session
     Login login = (Login) loginMap.get(securityDomainURI);
     if (login != null) {
       if (logger.isDebugEnabled()) {
         logger.debug("Logging " + login.username + " into " + serverURI);
       }
       session.login(securityDomainURI, login.username, login.password);
     }
     else {
       if (logger.isDebugEnabled()) {
         logger.debug("No credentials to propagate for " + securityDomainURI);
       }
     }
 
     if (this.session != null) {
       // Close existing session
       if (logger.isDebugEnabled()) {
         logger.debug("Closing session");
       }
 
       try {
         this.session.close();
       }
       catch (QueryException e) {
         logger.warn("Couldn't close session (abandoning)", e);
       }
 
       if (logger.isDebugEnabled()) {
         logger.debug("Closed session");
       }
     }
 
     // Reassign session
     if (session != null) {
       isLocal = session.isLocal();
     }
     this.session = session;
     this.securityDomainURI = securityDomainURI;
   }
 
   // getSession()
 
   /**
    * Sets the alias map associated with this session.
    *
    * @param aliasMap the alias map associated with this session
    */
   void setAliasMap(Map aliasMap) {
 
     this.aliasMap = aliasMap;
   }
 
   // addAliasPair()
 
   /**
    * Sets the flag indicating whether the user has entered the quit command
    *
    * @param quitRequested The new QuitRequested value
    */
   private void setQuitRequested(boolean quitRequested) {
 
     this.quitRequested = quitRequested;
   }
 
   // setQuitRequested()
 
   /**
    * Sets the results of the last query. Methods overriding <code>DepthFirstAdapter</code>
    * are expected to set a result for successful queries, and <code>null</code>
    * for failed ones.
    *
    * @param lastAnswer the results of the last command execution
    */
   private void setLastAnswer(Answer lastAnswer) {
 
     this.lastAnswer = lastAnswer;
   }
 
   /**
    * Insert a set of triples.  Calls the server if it can be done only on the
    * server.
    *
    * @param tripleFactor a triple, model or query from the parser
    * @throws QueryException if <code>tripleFactor</code> contains a query that
    *      cannot be executed, or after evaluation contains no statements
    * @throws URISyntaxException if <code>tripleFactor</code> contains a query or
    *      a resource that that violates <a
    *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC\uFFFD2396</a>
    */
   private void insertStatements(URI modelURI, PTripleFactor tripleFactor) throws
       QueryException, URISyntaxException {
 
     // validate tripleFactor parameter
     if (tripleFactor == null) {
 
       throw new IllegalArgumentException("Null \"tripleFactor\" parameter");
     }
 
     // log that we're finding statements
     if (logger.isDebugEnabled()) {
 
       logger.debug("Finding statements in expression " + tripleFactor);
     }
 
     // get the set of triples out of the factor
     PSetOfTriples setOfTriples = null;
 
     if (tripleFactor instanceof ABracedTripleFactor) {
 
       setOfTriples = ( (ABracedTripleFactor) tripleFactor).getSetOfTriples();
     }
     else if (tripleFactor instanceof AUnbracedTripleFactor) {
 
       setOfTriples = ( (AUnbracedTripleFactor) tripleFactor).getSetOfTriples();
     }
 
     // drill down into the set of triples
     if (setOfTriples instanceof AResourceSetOfTriples) {
 
       // log that we've found a resource
       if (logger.isDebugEnabled()) {
 
         URI resourceURI =
             toURI( ( (AResourceSetOfTriples) setOfTriples).getResource());
         logger.debug("Found resource " + resourceURI + "in triple factor");
       }
 
       // TODO: implement this once we can select all triples from a model
       // we cannot currently handle inserting or deleting a complete model
       // into/from another
       throw new UnsupportedOperationException("Models cannot be inserted " +
                                               "into or deleted from other models");
     }
     else if (setOfTriples instanceof ASelectSetOfTriples) {
 
       // build the query
       Query query = this.buildQuery( ( (ASelectSetOfTriples) setOfTriples));
 
       // log that we've created the query and will execute it
       if (logger.isDebugEnabled()) {
 
         logger.debug("Executing query " + query);
       }
 
       getSession().insert(modelURI, query);
     }
     else if (setOfTriples instanceof ATripleSetOfTriples) {
 
       Map variableMap = new HashMap();
       Set statements = getStatements( (ATripleSetOfTriples) setOfTriples,
           variableMap);
       getSession().insert(modelURI, statements);
     }
   }
 
   /**
    * Delete a set of triples.  Calls the server if it can be done only on the
    * server.
    *
    * @param tripleFactor a triple, model or query from the parser
    * @throws QueryException if <code>tripleFactor</code> contains a query that
    *      cannot be executed, or after evaluation contains no statements
    * @throws URISyntaxException if <code>tripleFactor</code> contains a query or
    *      a resource that that violates <a
    *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC\uFFFD2396</a>
    */
   private void deleteStatements(URI modelURI, PTripleFactor tripleFactor) throws
       QueryException, URISyntaxException {
 
     // validate tripleFactor parameter
     if (tripleFactor == null) {
 
       throw new IllegalArgumentException("Null \"tripleFactor\" parameter");
     }
 
     // log that we're finding statements
     if (logger.isDebugEnabled()) {
 
       logger.debug("Finding statements in expression " + tripleFactor);
     }
 
     // get the set of triples out of the factor
     PSetOfTriples setOfTriples = null;
 
     if (tripleFactor instanceof ABracedTripleFactor) {
 
       setOfTriples = ( (ABracedTripleFactor) tripleFactor).getSetOfTriples();
     }
     else if (tripleFactor instanceof AUnbracedTripleFactor) {
 
       setOfTriples = ( (AUnbracedTripleFactor) tripleFactor).getSetOfTriples();
     }
 
     // drill down into the set of triples
     if (setOfTriples instanceof AResourceSetOfTriples) {
 
       // log that we've found a resource
       if (logger.isDebugEnabled()) {
 
         URI resourceURI =
             toURI( ( (AResourceSetOfTriples) setOfTriples).getResource());
         logger.debug("Found resource " + resourceURI + "in triple factor");
       }
 
       // TODO: implement this once we can select all triples from a model
       // we cannot currently handle inserting or deleting a complete model
       // into/from another
       throw new UnsupportedOperationException("Models cannot be inserted " +
                                               "into or deleted from other models");
     }
     else if (setOfTriples instanceof ASelectSetOfTriples) {
 
       // build the query
       Query query = this.buildQuery( ( (ASelectSetOfTriples) setOfTriples));
 
       // log that we've created the query and will execute it
       if (logger.isDebugEnabled()) {
 
         logger.debug("Executing query " + query);
       }
 
       getSession().delete(modelURI, query);
     }
     else if (setOfTriples instanceof ATripleSetOfTriples) {
 
       Map variableMap = new HashMap();
       Set statements = getStatements((ATripleSetOfTriples) setOfTriples,
           variableMap);
       if (variableMap.size() > 0) {
         throw new QueryException("Cannot use variables when deleting " +
             "statements");
       }
       getSession().delete(modelURI, statements);
     }
   }
 
   /**
    * Returns a set of statements from the iTQL query object.
    *
    * @param setOfTriples the set of statements defined in the query.
    * @param variableMap the variable map to store the value of the variable
    *   against the variable object.
    * @throws URISyntaxException if <code>tripleFactor</code> contains a query or
    *      a resource that that violates <a
    *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC\uFFFD2396</a>
    * @throws QueryException if an invalid node is used in the set of triples.
    * @return a set of statements from the iTQL query.
    */
   public Set getStatements(ATripleSetOfTriples setOfTriples, Map variableMap)
       throws QueryException, URISyntaxException {
 
     List tripleList = setOfTriples.getTriple();
     HashSet statements = new HashSet();
 
     // Check that each set of triples has the predicate bound.
     for (Iterator i = tripleList.iterator(); i.hasNext(); ) {
 
       // get the triple
       ATriple triple = (ATriple) i.next();
 
       // Convert the Subject, Predicate and Object.
       org.jrdf.graph.Node subject = toNode(triple.getSubject(), variableMap);
       org.jrdf.graph.Node predicate = toNode(triple.getPredicate(), variableMap);
       org.jrdf.graph.Node object = toNode(triple.getObject(), variableMap);
 
       // Predicate cannot be a blank node.
       if (predicate instanceof BlankNode) {
         throw new QueryException("Predicate must be a valid URI");
       }
 
       // Check that the subject or predicate node is not a literal.
       if (subject instanceof LiteralImpl ||
           predicate instanceof LiteralImpl) {
 
         // throw an exception indicating we have a bad triple
         throw new QueryException(
             "Subject or Predicate cannot be a literal");
       }
 
       // Create a new statement using the triple elements
       org.jrdf.graph.Triple jrdfTriple = new TripleImpl(
           (SubjectNode) subject, (PredicateNode) predicate,
           (ObjectNode) object);
 
       // add the statement to the statement set
       statements.add(jrdfTriple);
     }
 
     return statements;
   }
 
   /**
    * Builds a set of {@link org.jrdf.graph.Triple}s from a
    * {@link org.mulgara.query.Answer}.
    *
    * @param answer the results of a query
    * @return a set of {@link org.jrdf.graph.Triple}s, suitable for use in
    *      inserting or deleting data to or from a model
    * @throws QueryException EXCEPTION TO DO
    */
   private Set getStatements(Answer answer) throws QueryException {
 
     // validate answer parameter
     if (answer == null) {
 
       throw new IllegalArgumentException("Null \"answer\" parameter");
     } // end if
 
     // log that we're parsing an answer into a set of statements
     if (logger.isDebugEnabled()) {
 
       logger.debug("Parsing answer into set of statements");
     }
 
     try {
       Set statements = new HashSet();
       answer.beforeFirst();
 
       while (answer.next()) {
 
         if (! (answer.getObject(0) instanceof ObjectNode)) {
 
           throw new QueryException("Subject is not a resource for [" +
                                    answer.getObject(0) + " " +
                                    answer.getObject(1) +
                                    " " +
                                    answer.getObject(2) + "]");
         }
 
         if (! (answer.getObject(1) instanceof URIReference)) {
 
           throw new QueryException("Predicate is not a resource for [" +
                                    answer.getObject(0) + " " +
                                    answer.getObject(1) +
                                    " " +
                                    answer.getObject(2) + "]");
         }
 
         statements.add(new TripleImpl( (SubjectNode) answer.getObject(0),
                                       (PredicateNode) answer.getObject(1),
                                       (ObjectNode) answer.getObject(2)));
       }
 
       logger.debug("Parsed answer into " + statements.size());
 
       return statements;
     }
     catch (TuplesException e) {
       throw new QueryException("Couldn't parse answer into statements", e);
     }
   } // setStatements
 
   /**
    * Returns the option to enable the timing of commands.
    *
    * @return the option to enable the timing of commands
    */
   private boolean getTimeOption() {
 
     return timeOption;
   }
 
   /**
    * Return the time a command started.
    *
    * @return the time a command started
    */
   private long getCommandStartTime() {
 
     return commandStartTime;
   }
 
   /**
    * Returns the alias map associated with this session.
    *
    * @return the alias namespace map associated with this session
    */
   Map getAliasMap() {
 
     return this.aliasMap;
   }
 
   //
   // Internal methods
   //
 
   /**
    * Executes a query and returns its results.
    *
    * @param rawQuery a select query, represented as either a {@link
    *      org.mulgara.itql.node.ASelectCommand} or a {@link
    *      org.mulgara.itql.node.ASelectSetOfTriples}
    * @return the answer to the query
    * @throws QueryException if the query cannot be executed
    * @throws URISyntaxException if the <code>query</code> contains a resource
    *      whose text violates <a href="http://www.isi.edu/in-notes/rfc2396.txt">
    *      RFC\uFFFD2396</a>
    */
   public Query buildQuery(org.mulgara.itql.node.Node rawQuery) throws
       QueryException, URISyntaxException {
 
     // validate query parameter
     if (rawQuery == null) {
 
       throw new IllegalArgumentException("Null \"rawQuery\" parameter");
     }
 
     // end if
     // create the variables needed...
     LinkedList variables = null;
     AFromClause fromClause;
     AWhereClause whereClause;
     AOrderClause orderClause;
     AHavingClause havingClause;
     ALimitClause limitClause;
     AOffsetClause offsetClause;
 
     // cast the correct way (we don't have a common superclass, event though we
     // have methods with the same names)
     if (rawQuery instanceof AQuery) {
 
       AQuery query = (AQuery) rawQuery;
       PSelectClause selectClause = query.getSelectClause();
       if (selectClause instanceof ANormalSelectSelectClause) {
         variables = ( (ANormalSelectSelectClause) selectClause).getElement();
       }
       fromClause = ( (AFromClause) query.getFromClause());
       whereClause = ( (AWhereClause) query.getWhereClause());
       orderClause = ( (AOrderClause) query.getOrderClause());
       havingClause = ( (AHavingClause) query.getHavingClause());
       limitClause = ( (ALimitClause) query.getLimitClause());
       offsetClause = ( (AOffsetClause) query.getOffsetClause());
     }
     else if (rawQuery instanceof ASelectSetOfTriples) {
 
       ASelectSetOfTriples query = (ASelectSetOfTriples) rawQuery;
       variables = new LinkedList();
       variables.add(query.getSubject());
       variables.add(query.getPredicate());
       variables.add(query.getObject());
       fromClause = ( (AFromClause) query.getFromClause());
       whereClause = ( (AWhereClause) query.getWhereClause());
       orderClause = ( (AOrderClause) query.getOrderClause());
       havingClause = ( (AHavingClause) query.getHavingClause());
       limitClause = ( (ALimitClause) query.getLimitClause());
       offsetClause = ( (AOffsetClause) query.getOffsetClause());
     }
     else {
 
       // we only handle AQuery and ASelectSetOfTriples
       throw new IllegalArgumentException("Invalid type for \"rawQuery\" " +
                                          "parameter");
     }
 
     if (fromClause == null) {
 
       throw new QueryException("FROM clause missing.");
     }
 
     if (whereClause == null) {
 
       throw new QueryException("WHERE clause missing.");
     }
 
     // end if
     // log that we're about to build the variable list
     if (logger.isDebugEnabled()) {
       logger.debug("Building query variable list from " + variables);
     }
 
     // build the variable list
     List variableList = this.buildVariableList(variables);
 
     // log that we've built the variable list
     if (logger.isDebugEnabled()) {
       logger.debug("Built variable list " + variableList);
     }
 
     // get the model expression from the parser
     PModelExpression rawModelExpression = fromClause.getModelExpression();
 
     // log that we're about to build the model expression
     if (logger.isDebugEnabled()) {
       logger.debug("Building model expression from " + rawModelExpression);
     }
 
     // parse the text into a model expression
     ModelExpression modelExpression =
         ModelExpressionBuilder.build(this.getAliasMap(), rawModelExpression);
 
     // log that we've built the model expression
     if (logger.isDebugEnabled()) {
       logger.debug("Built model expression " + modelExpression);
     }
 
     // get the constraint expression from the parser
     PConstraintExpression rawConstraintExpression =
         whereClause.getConstraintExpression();
 
     // log that we're about to build the constraint expression
     if (logger.isDebugEnabled()) {
       logger.debug("Building constraint expression from " +
                    rawConstraintExpression);
     }
 
     // parse the text into a constraint expression
     ConstraintExpression constraintExpression = build(rawConstraintExpression);
 
     // log that we've build the constraint expression
     if (logger.isDebugEnabled()) {
       logger.debug("Built constraint expression " + constraintExpression);
     }
 
     // build the order list
     List orderList = Collections.EMPTY_LIST;
 
     if (orderClause != null) {
 
       orderList = buildOrderList(orderClause.getOrderElement());
     }
 
     // build the having clause
     ConstraintHaving havingExpression = null;
 
     if (havingClause != null) {
       // get the constraint expression from the parser
       PConstraintExpression rawHavingExpression =
           havingClause.getConstraintExpression();
 
       // log that we're about to build the constraint expression
       if (logger.isDebugEnabled()) {
         logger.debug("Building constraint expression from " +
                      rawHavingExpression);
       }
 
       // parse the text into a constraint expression
       havingExpression = buildHaving(rawHavingExpression);
     }
 
     // build the limit
     Integer limit = null;
 
     if (limitClause != null) {
 
       try {
 
         limit = new Integer(limitClause.getNumber().getText());
       }
       catch (NumberFormatException e) {
 
         throw new Error("Sable parser permitted non-integer limit", e);
       }
     }
 
     // build the offset
     int offset = 0;
 
     if (offsetClause != null) {
 
       try {
 
         offset = Integer.parseInt(offsetClause.getNumber().getText());
       }
       catch (NumberFormatException e) {
 
         throw new Error("Sable parser permitted non-integer offset", e);
       }
     }
 
     // build a query using the information we've obtained from the parser
     // (all answers are acceptable)
     lastQuery = new Query(variableList, modelExpression, constraintExpression,
         havingExpression, orderList, limit, offset, new UnconstrainedAnswer());
 
     return lastQuery;
   }
 
   /**
    * Builds a list of {@link org.mulgara.query.Variable}s from a list of
    * {@link org.mulgara.itql.node.PVariable}s. Note. Variables in both the
    * <code>rawVariableList</code> and the returned list will <strong>not
    * </strong> contain the variable prefix <code>$</code> in their name.
    *
    * @param rawVariableList a list of {@link
    *      org.mulgara.itql.node.PVariable}s from the parser
    * @return a list of {@link org.mulgara.query.Variable}s, suitable for use
    *      in creating a {@link org.mulgara.query.Query}
    * @throws QueryException if the <code>rawVariableList</code> cannot be parsed
    *      into a list of {@link org.mulgara.query.Variable}s
    */
   private List buildVariableList(LinkedList rawVariableList) throws
       QueryException, URISyntaxException {
 
     // Empty variable list.
     if (rawVariableList == null) {
       return Collections.EMPTY_LIST;
     }
 
     // validate rawVariableList parameter
     if (rawVariableList.size() == 0) {
       throw new IllegalArgumentException("Empty \"rawVariableList\" parameter");
     }
 
     // Construct the required builder
     VariableBuilder variableBuilder = new VariableBuilder(this,
         variableFactory);
 
     // end if
     // log that we're building the variable list
     if (logger.isDebugEnabled()) {
       logger.debug("Building variable list from " + rawVariableList);
     }
 
     // copy each variable from the query into the list
     for (Iterator i = rawVariableList.iterator(); i.hasNext(); ) {
       PElement element = (PElement) i.next();
       element.apply( (Switch) variableBuilder);
     }
 
     // Get the variable list
     List variableList = variableBuilder.getVariableList();
 
     // make sure that we return a list with something in it
     if (variableList.size() == 0) {
       throw new QueryException("No variables parseable from query");
     }
 
     // log that we've successfully built the variable list
     if (logger.isDebugEnabled()) {
       logger.debug("Built variable list " + variableList);
     }
 
     // return the list
     return variableList;
   }
 
   /**
    * Builds a list of {@link org.mulgara.query.Variable}s from a list of
    * {@link org.mulgara.itql.node.POrderElement}s. Note. Variables in both
    * the <code>rawVariableList</code> and the returned list will <strong>not
    * </strong> contain the variable prefix <code>$</code> in their name.
    *
    * @param rawOrderList PARAMETER TO DO
    * @return a list of {@link org.mulgara.query.Variable}s, suitable for use
    *      in creating a {@link org.mulgara.query.Query}
    * @throws QueryException if the <code>rawOrderElementList</code> cannot be
    *      parsed into a list of {@link org.mulgara.query.Variable}s
    */
   private List buildOrderList(LinkedList rawOrderList) throws QueryException {
 
     // validate rawOrderElementList parameter
     if ( (rawOrderList == null) || (rawOrderList.size() == 0)) {
 
       throw new IllegalArgumentException("Null \"rawOrderList\" parameter");
     }
 
     // end if
     // log that we're building the variable list
     if (logger.isDebugEnabled()) {
       logger.debug("Building order list from " + rawOrderList);
     }
 
     // create a list for the parsed variables
     List orderList = new ArrayList(rawOrderList.size());
 
     // copy each variable from the query into the list
     for (Iterator i = rawOrderList.iterator(); i.hasNext(); ) {
 
       AOrderElement order = (AOrderElement) i.next();
 
       // get the name of this variable
       String variableName =
           ( (AVariable) order.getVariable()).getIdentifier().getText();
 
       // log that we've found a variable
       if (logger.isDebugEnabled()) {
         logger.debug("Found variable $" + variableName);
       }
 
       // Figure out which way to order, ascending or descending
       boolean ascending;
       PDirection direction = order.getDirection();
 
       if (direction == null) {
         ascending = true;
       }
       else if (direction instanceof AAscendingDirection) {
         ascending = true;
       }
       else if (direction instanceof ADescendingDirection) {
         ascending = false;
       }
       else {
         throw new Error("Unknown direction field in order");
       }
 
       // add a new variable to the list
       orderList.add(new Order(new Variable(variableName), ascending));
     }
 
     // end for
     // make sure that we return a list with something in it
     if (orderList.size() == 0) {
 
       throw new QueryException("No variables parseable from query");
     }
 
     // log that we've successfully built the order list
     if (logger.isDebugEnabled()) {
 
       logger.debug("Built order list " + orderList);
     }
 
     // return the list
     return orderList;
   }
 
   // getStatements()
 
   /**
    * Construct a {@link LiteralImpl} from a {@link PLiteral}.
    *
    * @param p  the instance to convert
    */
   public LiteralImpl toLiteralImpl(PLiteral p) {
 
     ALiteral aLiteral = (ALiteral) p;
 
     // Determine the datatype URI, if present
     URI datatypeURI = null;
     if (aLiteral.getDatatype() != null) {
       datatypeURI = toURI( ( (ADatatype) aLiteral.getDatatype()).getResource());
     }
 
     // Determine the language code
     String language = (datatypeURI == null) ? "" : null;
 
     if (datatypeURI == null) {
       return new LiteralImpl(getLiteralText(aLiteral), language);
     }
     else {
       return new LiteralImpl(getLiteralText(aLiteral), datatypeURI);
     }
   }
 
   /**
    * Constructs a {@link org.jrdf.graph.Node} from a {@link
    * org.mulgara.itql.node.PTripleElement}.
    *
    * @param element dd
    * @param variableMap a {@link Map} of variable names (as string) to
    *   {@link VariableNodeImpl} that are used to contain all variables.
    * @return dd
    * @throws QueryException if <code>element</code> is a {@link
    *   org.mulgara.itql.node.AResourceTripleElement} whose text contains a
    *   <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified
    *   name</a> with a prefix not defined in the <code>aliasMap</code>
    * @throws URISyntaxException if <code>element</code> is a {@link
    *   org.mulgara.itql.node.AResourceTripleElement} whose text doesn't
    *   conform to <a href="http://www.isi.edu/in-notes/rfc2396.txt">
    *   RFC\uFFFD2396</a>
    */
   private org.jrdf.graph.Node toNode(PTripleElement element, Map variableMap) throws
       QueryException, URISyntaxException {
 
     // validate the element parameter
     if (element == null) {
 
       throw new IllegalArgumentException("Null \"element\" parameter");
     }
 
     // end if
     // log what we're doing
     if (logger.isDebugEnabled()) {
 
       logger.debug("Resolving " + element + "to a RDF node");
     }
 
     // create the node
     org.jrdf.graph.Node node = null;
 
     // get the node
     if (element instanceof ALiteralTripleElement) {
 
       // create a new literal with the given text
       node = toLiteralImpl( ( (ALiteralTripleElement) element).getLiteral());
     }
     else if (element instanceof AResourceTripleElement) {
 
       // create a new resource
       node = new URIReferenceImpl(toURI(
           ( (AResourceTripleElement) element).getResource()
           ));
     }
     else if (element instanceof AVariableTripleElement) {
 
       // get the variable
       String variableName =
           ( (AVariable) ( (AVariableTripleElement) element).getVariable()).
           getIdentifier().getText();
 
       // log what we're doing
       if (logger.isDebugEnabled()) {
 
         logger.debug("Resolved " + element + " to variable " + variableName);
       }
 
       if (variableMap.containsKey(variableName)) {
 
         node = (VariableNodeImpl) variableMap.get(variableName);
       }
       else {
 
         VariableNodeImpl variable = new VariableNodeImpl(variableName);
         variableMap.put(variableName, variable);
         node = variable;
       }
     } // end if
 
     // return the node
     return node;
   }
 
   /**
    * Convert SableCC-generated {@link TResource} tokens into {@link URI}s.
    *
    * Resolution will treat the token as an XML
    * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified
    * names</a> if the {@link #aliasMap} <code>aliasMap</code> contains a key
    * for the URI scheme part that can be treated as an XML namespace prefix.
    * For example, <kbd>dc:title</kbd> is treated as a qname and mapped to the
    * to the URI <kbd>http://purl.org/dc/elements/1.1/title</kbd>, assuming the
    * {@link #aliasMap} had an entry mapping <code>"dc"</code> to the Dublin
    * Core namespace.
    *
    * @param token  the token to be converted, which should actually be a
    *   {@link TResource}
    * @throws Error if the <var>token</var> text isn't syntactically
    *   a {@link URI}; this shouldn't ever occur, assuming the <var>token</var>
    *   is a {@link TResource}
    */
   public URI toURI(Token token) {
 
     assert token instanceof TResource;
 
     try {
       // get the URI of the resource -- this may actually be an XML qname
       URI resourceURI = new URI(token.getText());
 
       if (resourceURI.isOpaque()) {
         // Attempt qname-to-URI substitution for aliased namespace prefixes
         URI mapping = (URI) aliasMap.get(resourceURI.getScheme());
         if (mapping != null) {
           resourceURI = new URI(mapping + resourceURI.getSchemeSpecificPart());
         }
       }
 
       return resourceURI;
     }
     catch (URISyntaxException e) {
       throw new Error("Bad URI syntax in resource: " + e);
     }
   }
 
   /**
    * Determines if the stream is compressed by inspecting the fileName extension.
    *
    * Returns a new "Compressed" stream if the file is compressed or the original
    * InputStream if the file is not compressed.
    *
    * @param fileName String
    * @param inputStream InputStream
    * @throws IOException
    * @return InputStream
    */
   private InputStream adjustForCompression(String fileName,
       InputStream inputStream) throws IOException {
 
     //validate
     if (fileName == null) {
       throw new IllegalArgumentException("File name is null");
     }
     if (inputStream == null) {
       throw new IllegalArgumentException("InputStream is null");
     }
 
     InputStream stream = inputStream;
 
     // Guess at transfer encoding (compression scheme) based on file extension
     if (fileName.toLowerCase().endsWith(".gz")) {
       // The file name ends with ".gz", so assume it's a gzip'ed file
       stream = new GZIPInputStream(inputStream);
     }
     else if (fileName.toLowerCase().endsWith(".zip")) {
       // The file name ends with ".zip", so assume it's a zip'ed file
       stream = new ZipInputStream(inputStream);
     }
 
     assert stream != null;
     return stream;
   }
 
   /**
    * Adds a name/value pair to the alias map. This method will add associate a
    * prefix for a target for subsequent commands, making commands like the
    * following possible: <PRE>
    * alias http://purl.org/dc/elements/1.1 as dc;
    * select $title where $uri dc:title $title ;
    * </PRE>
    *
    * @param aliasPrefix the alias that denotes the target
    * @param aliasTarget the target associated with the prefix
    */
   private void addAliasPair(String aliasPrefix, URI aliasTarget) {
 
     // validate the aliasPrefix parameter
     if (aliasPrefix == null) {
 
       throw new IllegalArgumentException("Null \"aliasPrefix\" " + "parameter");
     }
 
     // end if
     // validate the aliasTarget parameter
     if (aliasTarget == null) {
 
       throw new IllegalArgumentException("Null \"aliasTarget\" " + "parameter");
     }
 
     // end if
     // add the pair to the map
     this.getAliasMap().put(aliasPrefix, aliasTarget);
   }
 
   /**
    * Log the iTQL command to a specified file
    *
    * @param command PARAMETER TO DO
    */
   private void logItql(String command) {
 
     // Has this session been constructed for logging.
     // the constructor initialise this if
     // system property itql.command.log is set
     if (itqlLogFile == null) {
 
       return;
     }
 
     try {
       // has the log file been opened?
       if (itqlLog == null) {
         itqlLog = new PrintWriter(new FileWriter(itqlLogFile, true), true);
       }
 
       // append the command to the file
       itqlLog.println(command);
     }
     catch (Exception ex) {
       logger.error("Unable to log itql commands", ex);
     }
   }
 
   private class Lexer2 extends Lexer {
 
     int commandCount = 0;
     final LinkedList leftoverTokenList = new LinkedList();
 
     public Lexer2() {
       super(null);
     }
 
     public int getCommandCount() {
       return commandCount;
     }
 
     public void add(String command) throws LexerException, IOException {
       Lexer lexer = new Lexer(new PushbackReader(new StringReader(command), 256));
       Token t;
       while (! ( (t = lexer.next()) instanceof EOF)) {
         if (t instanceof TTerminator) {
           t = new EOF();
           commandCount++;
         }
         leftoverTokenList.add(t);
       }
     }
 
     public Token next() throws LexerException, IOException {
       return leftoverTokenList.isEmpty() ? new EOF() : (Token) leftoverTokenList.removeFirst();
     }
 
     public Token peek() throws LexerException, IOException {
       return leftoverTokenList.isEmpty() ? new EOF() : (Token) leftoverTokenList.getFirst();
     }
 
     public boolean nextCommand() {
       if (commandCount == 0) {
         return false;
       }
       else {
         //assert commandCount > 0;
         commandCount--;
         return true;
       }
     }
   }
 
   //
   // Static methods brought in from the late ConstraintExpressionBuilder
   //
 
   /**
    * Returns the text of the given <code>literal</code>.
    *
    * @param literal the literal to retrieve the text from
    * @return The LiteralText value
    */
   public static String getLiteralText(ALiteral literal) {
 
     // validate the literal parameter
     if (literal == null) {
 
       throw new IllegalArgumentException("Null \"literal\" " + "parameter");
     }
 
     // end if
     // the text of the literal
     StringBuffer literalText = new StringBuffer();
 
     // get all the strands in this literal
     List strands = literal.getStrand();
 
     // add each strand together to make the literal text
     for (Iterator i = strands.iterator(); i.hasNext(); ) {
 
       // get the strand
       PStrand strand = (PStrand) i.next();
 
       // add the strand to the literal text
       if (strand instanceof AUnescapedStrand) {
 
         literalText.append( ( (AUnescapedStrand) strand).getText().getText());
       }
       else if (strand instanceof AEscapedStrand) {
 
         literalText.append( ( (AEscapedStrand) strand).getEscapedtext().getText());
       } // end if
 
     } // end for
 
     // return the text
     return literalText.toString();
   }
 
   //
   // Public API
   //
 
   /**
    * Builds a {@link org.mulgara.query.ConstraintExpression} object from a
    * {@link org.mulgara.itql.node.PConstraintExpression}, using an <code>aliasMap</code>
    * to resolve aliases.
    *
    * @param expression a constraint expression from the parser
    * @return RETURNED VALUE TO DO
    * @throws QueryException if <code>rawConstraintExpression</code> does not
    *      represent a valid query
    * @throws URISyntaxException if the <code>rawConstraintExpression</code>
    *      contains a resource whose text violates <a
    *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
    */
   public ConstraintExpression build(PConstraintExpression expression) throws
       QueryException, URISyntaxException {
 
     // validate aliasMap parameter
     if (aliasMap == null) {
       throw new IllegalArgumentException("Null \"aliasMap\" parameter");
     }
 
     // validate expression parameter
     if (expression == null) {
       throw new IllegalArgumentException("Null \"expression\" parameter");
     }
 
     // log that we're building a constraint expression
     if (logger.isDebugEnabled()) {
       logger.debug("Building constraint expression from " + expression);
     }
 
     // build the contraint expression from the parser input
     expression.apply( (Switch) builder);
     ConstraintExpression constraintExpression = builder.
         getConstraintExpression();
 
     // log that we've building successfully built a constraint expression
     if (logger.isDebugEnabled()) {
       logger.debug("Successfully built constraint expression from " +
                    expression);
     }
 
     // return the contraint expression
     return constraintExpression;
   }
 
   /**
    * Returns an anonymous variable unique for this interpreter.
    * Note: We really should introduce a new subclass of Variable
    * that is explicitly anonymous, but for now this will do.
    */
   public Variable nextAnonVariable() {
     return new Variable("av__" + this.anonSuffix++);
   }
 
   /**
    * Builds a HAVING compliant {@link org.mulgara.query.ConstraintExpression} object from a
    * {@link org.mulgara.itql.node.PConstraintExpression}, using an <code>aliasMap</code>
    * to resolve aliases.  To comply with a HAVING clause the predicate must be one of:
    * mulgara:occurs mulgara:occursLessThan mulgara:occursMoreThan.
    *
    * @param expression a constraint expression from the parser
    * @return RETURNED VALUE TO DO
    * @throws QueryException if <code>rawConstraintExpression</code> does not
    *      represent a valid query
    * @throws URISyntaxException if the <code>rawConstraintExpression</code>
    *      contains a resource whose text violates <a
    *      href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
    */
   private ConstraintHaving buildHaving(PConstraintExpression expression)
       throws QueryException, URISyntaxException {
     ConstraintExpression hExpr = build(expression);
 
     if (hExpr instanceof ConstraintOperation) {
       throw new QueryException("Having currently supports only one constraint");
     }
 
     if (!checkHavingPredicates(hExpr)) {
       throw new QueryException("Only \"occurs\" predicates can be used in a Having clause");
     }
     return (ConstraintHaving) hExpr;
   }
 
 
   /**
    * Checks that all predicates in a constraint expression are valid Having predicates
    * from {@link SpecialPredicates}.
    *
    * @param e The constraint expression to check.
    * @return true if all constraints have special predicates.
    */
   private boolean checkHavingPredicates(ConstraintExpression e) {
     if (e instanceof Constraint) {
       return e instanceof ConstraintHaving;
     } else if (e instanceof ConstraintOperation) {
       // check all sub expressions
       Iterator i = ((ConstraintOperation)e).getElements().iterator();
       while (i.hasNext()) {
         if (checkHavingPredicates((ConstraintExpression)i.next())) {
           return false;
         }
       }
       // all sub expressions returned true
       return true;
     } else {
       // An unexpected type
       return false;
     }
   }
 
 
   /**
    * Set the active server for a backup given either a server URI or
    * a model URI.
    *
    * @param sourceURI URI
    */
 
   private void setBackupServer( URI sourceURI ) throws QueryException {
 
     //Determine if a model or a server is being backed up.
     if ( (sourceURI != null)
       && (sourceURI.getFragment() != null)) {
 
       // make a note that a model is being backed up
       if (logger.isDebugEnabled()) {
 
        logger.debug("Model Found. Backing up model " + sourceURI );
       }
 
       //remove fragment from the model URI
       String fragment = sourceURI.getFragment();
       String serverURI = sourceURI.toString().replaceAll("#" + fragment, "");
 
       //create URI for the model's server
        try {
 
          URI uri = new URI(serverURI);
 
          //get session for the model's server.
          setServerURI(uri);
 
          //update the session (required if this is the first query)
          this.updateSession(new ModelResource(uri));
 
        }
        catch (URISyntaxException uriException) {
 
         //this will be caught below
         throw new QueryException("Could not connect to model's Server.",
                                   uriException);
        }
     }
     else {
 
       // log that we're backing up a server
       if (logger.isDebugEnabled()) {
 
         logger.debug("Backing up server " + sourceURI );
       }
 
      //source is a server.
      setServerURI(sourceURI);
     }
   }
 
 
   /**
    * Try to recognise a uri alias, and return the canonical form instead.
    *
    * @param uri The URI being checked.
    * @return The updated URI.  May be the same as the uri parameter.
    */
   private URI getCanonicalUriAlias(URI uri) {
     // only do this for remote protocols
     if (!"rmi".equals(uri.getScheme()) && !"soap".equals(uri.getScheme())) {
       return uri;
     }
     logger.debug("Checking for an alias on: " + uri);
     // extract the host name
     String host = uri.getHost();
     if (host == null) {
       return uri;
     }
     Set hostnames = getHostnameAliases();
     // Check with a DNS server to see if this host is recognised
     InetAddress addr = null;
     try {
       addr = InetAddress.getByName(host);
     } catch (UnknownHostException uhe) {
       // The host was unknown, so allow resolution to continue as before
       return uri;
     }
     // check the various names against known aliases and the given name
     if (
         hostnames.contains(host) ||
         hostnames.contains(addr.getHostName()) ||
         hostnames.contains(addr.getCanonicalHostName()) ||
         hostnames.contains(addr.getHostAddress())
     ) {
       // change the host name to one that is recognised
       // use the system uri to find the local host name
       URI serverURI = getServerURI();
       if (serverURI == null) {
         return uri;
       }
       String newHost = serverURI.getHost();
       try {
         return new URI(uri.getScheme(), newHost, uri.getPath(), uri.getFragment());
       } catch (URISyntaxException e) { /* fall through */ }
     }
 
     // not found, so return nothing
     return uri;
   }
 
 
   /**
    * Method to ask the ServerInfo for the local server aliases.
    * This will return an empty set if ServerInfo is not available -
    * ie. being run on a host which has no local database, such an an iTQL client.
    *
    * @return The set of server aliases as strings
    */
   private static Set getHostnameAliases() {
     Set names = (Set)getServerInfoProperty("HostnameAliases");
     return (names == null) ? java.util.Collections.EMPTY_SET : names;
   }
 
 
   /**
    * Method to ask the ServerInfo for the local server URI.
    * This will return null if ServerInfo is not available -
    * ie. being run on a host which has no local database, such an an iTQL client.
    *
    * @return The URI of the local server, or null if this is not a server.
    */
   private static URI getServerURI() {
     return (URI)getServerInfoProperty("ServerURI");
   }
 
 
   /**
    * Method to get the value of a property from the ServerInfo for the local database session.
    * This will return null if ServerInfo is not available -
    * ie. being run on a host which has no local database, such an an iTQL client.
    *
    * @param property The property to return, with the correct case.
    * @return The object returned from the accessor method named, or null if ServerInfo is not available.
    */
   private static Object getServerInfoProperty(String property) {
     Object o = null;
     try {
       Class rsf = Class.forName("org.mulgara.server.ServerInfo");
       java.lang.reflect.Method getter = rsf.getMethod("get" + property, null);
       o = getter.invoke(null, null);
     } catch (Exception e) { /* no op */ }
     return o;
   }
 
 }
