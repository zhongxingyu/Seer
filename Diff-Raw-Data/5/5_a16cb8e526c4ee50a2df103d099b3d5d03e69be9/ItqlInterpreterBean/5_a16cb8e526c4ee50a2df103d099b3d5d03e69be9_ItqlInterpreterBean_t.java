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
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.itql;
 
 // Java APIs
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.*;
 import javax.xml.soap.SOAPException;
 import org.w3c.dom.*;
 
 // Third party packages
 import org.apache.log4j.*;              // Log4J
 import org.apache.axis.utils.XMLUtils;  // Apache Axis
 import org.apache.axis.utils.DOM2Writer;
 import org.jrdf.graph.URIReference;     // JRDF
 import org.jrdf.graph.BlankNode;
 
 // Mulgara packages
 import org.mulgara.itql.lexer.LexerException;
 import org.mulgara.itql.parser.ParserException;
 import org.mulgara.query.Answer;
 import org.mulgara.query.Query;
 import org.mulgara.query.QueryException;
 import org.mulgara.query.TuplesException;
 import org.mulgara.query.rdf.LiteralImpl;
 import org.mulgara.server.Session;
 
 /**
  * iTQL Interpreter Bean.
  * <p>
  * This class provides a simple interface for the execution of iTQL queries.
  * </p>
  * <p>
  * Note. This class will be deprecated and is going away in favour of {@link org.mulgara.connection.MulgaraConnection}
  * based interfaces.
  * </p>
  *
  * @created 2001-Aug-30
  * @author Tate Jones
  * @author Ben Warren
  * @author Tom Adams
  * @version $Revision: 1.11 $
  * @modified $Date: 2005/04/04 11:30:11 $ by $Author: tomadams $
  * @copyright &copy;2001-2004 <a href="http://www.tucanatech.com/">Tucana Technology, Inc</a>
  * @copyright &copy;2005 <a href="mailto:tomjadams@gmail.com">Tom Adams</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 public class ItqlInterpreterBean {
 
   /**
    * the logging category to log to
    */
   private final static Logger log =
       Logger.getLogger(ItqlInterpreterBean.class.getName());
 
   /**
    * Get line separator.
    */
   private static final String EOL = System.getProperty("line.separator");
 
   /**
    * The TQL namespace, used when creating an XML response.
    *
    * TODO: Bring this into line with the Mulgara.NAMESPACE, which may break existing client.
    */
   private final static String TQL_NS = "http://mulgara.org/tql#";
 
   /**
    * The ITQL interpreter Bean.
    */
   private final ItqlInterpreter interpreter =
     new ItqlInterpreter(new HashMap());
 
   /**
    * Create the ITQL interpreter.
    *
    * The session will be obtained using a
    *   {@link org.mulgara.server.driver.SessionFactoryFinder}.
    */
   public ItqlInterpreterBean() {
 
     if (log.isInfoEnabled()) {
 
       log.info("Created an ItqlInterpreterBean");
     }
 
   }
 
   /**
    * Create the ITQL interpreter using the given <code>session</code>.
    *
    * @param session the session to use to communicate with the Mulgara server
    */
   public ItqlInterpreterBean(Session session, URI securityDomain) {
 
     if (log.isInfoEnabled()) {
       log.info("Created an ItqlInterpreterBean with a supplied session and security domain");
     }
 
     try {
       interpreter.setSession(session, securityDomain);
     }
     catch (QueryException e) {
 
       interpreter.setLastError(e);
       interpreter.setLastMessage("Couldn't set interpreter session: " +
           e.getMessage());
     }
   }
 
   // executeQueryToMap()
 
   /**
    * Splits a query containing multiple queries into an array of single queries.
    *
    * @param multiQuery PARAMETER TO DO
    * @return An array of query strings which include the ending ';' charater.
    */
   public static String[] splitQuery(String multiQuery) {
 
     List singleQueryList = new ArrayList();
 
     // Inside a URI?
     boolean INSIDE_URI = false;
 
     // Inside a text literal?
     boolean INSIDE_TEXT = false;
 
     // Start index for next single query
     int startIndex = 0;
 
     if (log.isDebugEnabled()) {
 
       log.debug("About to break up query: " + multiQuery);
     }
 
     multiQuery = multiQuery.trim();
 
     // Iterate along the multi query and strip out the single queries.
     for (int lineIndex = 0; lineIndex < multiQuery.length(); lineIndex++) {
 
       char currentChar = multiQuery.charAt(lineIndex);
 
       switch (currentChar) {
 
         // Quote - end or start of a literal if not in a URI
         // (OK so maybe it won't appear in a legal URI but let things further
         // down handle this)
         case '\'':
 
           if (!INSIDE_URI) {
 
             if (INSIDE_TEXT) {
 
               // Check for an \' inside a literal
               if ( (lineIndex > 1) && (multiQuery.charAt(lineIndex - 1) != '\\')) {
 
                 INSIDE_TEXT = false;
               }
             }
             else {
 
               INSIDE_TEXT = true;
             }
           }
 
           break;
 
           // URI start - if not in a literal
         case '<':
 
           if (!INSIDE_TEXT) {
 
             INSIDE_URI = true;
           }
 
           break;
 
           // URI end - if not in a literal
         case '>':
 
           if (!INSIDE_TEXT) {
 
             INSIDE_URI = false;
           }
 
           break;
 
         case ';':
 
           if (!INSIDE_TEXT && !INSIDE_URI) {
 
             String singleQuery =
                 multiQuery.substring(startIndex, lineIndex + 1).trim();
             startIndex = lineIndex + 1;
             singleQueryList.add(singleQuery);
 
             if (log.isDebugEnabled()) {
 
               log.debug("Found single query: " + singleQuery);
             }
           }
 
           break;
 
         default:
       }
     }
 
     // Lasy query is not terminated with a ';'
     if (startIndex < multiQuery.length()) {
 
       singleQueryList.add(multiQuery.substring(startIndex, multiQuery.length()));
     }
 
     return (String[]) singleQueryList.toArray(new String[singleQueryList.size()]);
   }
 
   /**
    * Returns the session to use to communicate with the Mulgara server.
    *
    * @return the session to use to communicate with the Mulgara server
    */
   public Session getSession() {
 
     try {
       return this.interpreter.getSession();
     }
     catch (QueryException e) {
       return null;
     }
   }
 
   /**
    * Returns the session to use to communicate with the specified Mulgara server.
    *
    * @param serverURI URI Server to get a Session for.
    * @return the session to use to communicate with the specified Mulgara server
    */
   public Session getSession(URI serverURI) {
 
     try {
       return this.interpreter.getSession(serverURI);
     }
     catch (QueryException e) {
       return null;
     }
   }
 
 
   /**
    * Returns the aliases associated with this bean.
    *
    * @return the alias namespace map associated with this bean.
    */
   public Map getAliasMap() {
 
     return this.interpreter.getAliasMap();
   }
 
   /**
    * Closes the session underlying this bean.
    */
   public void close() {
     try {
       Session session = interpreter.getSession();
       if (session != null) {
         session.close();
       }
       session = null;
     } catch (QueryException e) {
       if (log.isInfoEnabled()) {
         log.info("Couldn't close interpreter session", e);
       }
     }
   }
 
   //
   // Public API
   //
 
   /**
    * Begin a new transaction by setting the autocommit off
    *
    * @param name the name of the transaction ( debug purposes only )
    * @throws QueryException EXCEPTION TO DO
    */
   public void beginTransaction(String name) throws QueryException {
 
     if (log.isDebugEnabled()) {
 
       log.debug("Begin transaction for :" + name);
     }
 
     this.getSession().setAutoCommit(false);
   }
 
   /**
    * Commit a new transaction by setting the autocommit on
    *
    * @param name the name of the transaction ( debug purposes only ) *
    * @throws QueryException EXCEPTION TO DO
    */
   public void commit(String name) throws QueryException {
 
     if (log.isDebugEnabled()) {
 
       log.debug("Commit transaction for :" + name);
     }
 
     // this is the same as a commit call
     this.getSession().setAutoCommit(true);
   }
 
   /**
    * Rollback an existing transaction
    *
    * @param name the name of the transaction ( debug purposes only ) *
    * @throws QueryException EXCEPTION TO DO
    */
   public void rollback(String name) throws QueryException {
 
     log.warn("Rollback transaction for :" + name);
 
     this.getSession().rollback();
     this.getSession().setAutoCommit(true);
   }
 
   /**
    * Answer TQL queries.
    *
    * @param queryString PARAMETER TO DO
    * @return the answer DOM to the TQL query
    * @throws Exception EXCEPTION TO DO
    */
   public Element execute(String queryString) throws Exception {
     try {
       //DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
       //Document doc = xdb.newDocument();
       Document doc = XMLUtils.newDocument();
 
       Element answerDom = doc.createElementNS(TQL_NS, "answer");
       answerDom.setAttribute("xmlns", TQL_NS);
 
       if (log.isDebugEnabled()) {
         log.debug("Received a TQL query : " + queryString);
       }
 
       String[] queries = splitQuery(queryString);
 
       for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
         String singleQuery = queries[queryIndex];
 
         // Attach the answer
        Element query = (Element) answerDom.appendChild(doc.createElementNS(TQL_NS, "query"));
 
         // Resolve the query
         if (log.isDebugEnabled()) {
           log.debug("Executing query : " + singleQuery);
         }
 
         interpreter.executeCommand(singleQuery);
 
         Answer answer = null;
         try {
           answer = this.interpreter.getLastAnswer();
 
           if ((answer == null) || answer.isUnconstrained()) {
             if (this.interpreter.getLastError() == null) {
               //Not an error, but a message does exist
               Element message =
                   (Element)query.appendChild(doc.createElementNS(TQL_NS, "message"));
               message.appendChild(doc.createTextNode(interpreter.getLastMessage()));
 
               if (log.isDebugEnabled()) {
                 log.debug("Attached last message: " + interpreter.getLastMessage());
               }
             } else {
               //Error has occurred at the interpreter
               //Generate a SOAP fault
               // TODO REMOVE Constants.FAULT_CODE_SERVER,
               log.error("Execute query failed.  Returning error", interpreter.getLastError());
               throw new SOAPException("ItqlInterpreter error - " +
                   interpreter.getCause(interpreter.getLastError(), 0));
             }
             // Ensure answer is null.
             answer = null;
           } else {
             log.debug("Building XML result set");
             appendSolution(answer, query);
             log.debug("Attached answer text");
           }
         } finally {
           if (answer != null) {
             answer.close();
           }
         }
       }
 
       // Send the answer back to the caller
       return answerDom;
     } catch (Exception e) {
       log.error("Failed to execute query", e);
       throw e;
     }
   }
 
   /**
    * Answer TQL queries.
    *
    * @param queryString PARAMETER TO DO
    * @return the answer String to the TQL query
    * @throws Exception EXCEPTION TO DO
    */
   public String executeQueryToString(String queryString) throws Exception {
 
     String result = DOM2Writer.nodeToString(this.execute(queryString), false);
 
     if (log.isDebugEnabled()) {
       log.debug("Sending result to caller : " + result);
     }
 
     return result;
   }
 
   /**
    * Executes a semicolon delimited string of queries. <p>
    *
    * This method allows you to execute mulitple queries at once by specifying a
    * string of the following form: </p> <pre>
    * String queryString = "select $s $p $o from <rmi://machine/server1#model> " +
    *     "where $s $p $o;";
    * queryString += "select $s $p $o from <rmi://machine2/server1#model> " +
    *     "where $s $p $o;";
    * </pre> <p>
    *
    * The ordering of the result list will correspond to the ordering of the
    * queries in the input string. </p> <p>
    *
    * Note. Two different return types will be contained in the returned list. An
    * {@link org.mulgara.query.Answer} or a {@link java.lang.String} (error)
    * message. </p>
    *
    * @param queryString semi-colon delimited string containing the queries to be
    *      executed
    * @return a list of answers, messages and errors, answers are of type {@link
    *      org.mulgara.query.Answer}, the messages are {@link
    *      java.lang.String}s
    */
   public List executeQueryToList(String queryString) {
 
     return executeQueryToList(queryString, false);
   }
 
   /**
    * Executes a semicolon delimited string of queries. <p>
    *
    * This method allows you to execute mulitple queries at once by specifying a
    * string of the following form: </p> <pre>
    * String queryString = "select $s $p $o from <rmi://machine/server1#model> " +
    *     "where $s $p $o;";
    * queryString += "select $s $p $o from <rmi://machine2/server1#model> " +
    *     "where $s $p $o;";
    * </pre> <p>
    *
    * The ordering of the result list will correspond to the ordering of the
    * queries in the input string. </p> <p>
    *
    * Note. Two different return types will be contained in the returned list. An
    * {@link org.mulgara.query.Answer} or a {@link java.lang.String} (error)
    * message. </p>
    *
    * @param queryString semi-colon delimited string containing the queries to be
    *      executed
    * @param keepExceptions return exceptions, don't convert them to a string.
    * @return a list of answers, messages and errors, answers are of type {@link
    *      org.mulgara.query.Answer}, the messages are {@link
    *      java.lang.String}s
    */
   public List executeQueryToList(String queryString, boolean keepExceptions) {
 
     List answers = new ArrayList();
 
     if (log.isDebugEnabled()) {
 
       log.debug("Received a TQL query : " + queryString);
     }
 
     String[] queries = splitQuery(queryString);
 
     for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
 
       String singleQuery = queries[queryIndex];
 
       // Resolve the query
       if (log.isDebugEnabled()) {
 
         log.debug("Executing query : " + singleQuery);
       }
 
       // end if
       // execute it
       answers.add(this.executeQueryToNiceResult(singleQuery, keepExceptions));
     }
 
     // end for
     // Send the answers back to the caller
     return answers;
   }
 
   /**
    * Executes a {@link java.util.Map} of queries, returning the results of those
    * queries in a map keyed with the same keys as the input map. <p>
    *
    * The <var>queries</var> is a map of keys ({@link java.lang.Object}s) to
    * queries ({@link java.lang.String}s). Each query will be excuted (in the
    * order in which <var>queries</var> 's map implementation {@link
    * java.util.Map#keySet()}'s {@link java.util.Set#iterator()} returns keys)
    * and the results added to the returned map, keyed on the same key as the
    * original query. </p> <p>
    *
    * For example: </p> <pre>
    * // create the queries
    * URI title = new URI("http://www.foo.com/title");
    * String titleQuery = "select $s $p $o from <rmi://machine/server1#model> " +
    *     "where $s $p $o;";
    * URI date = new URI("http://www.foo.com/date");
    * String dateQuery = "select $s $p $o from <rmi://machine2/server1#model> " +
    *     "where $s $p $o;";
    *
    * // add them to the map
    * HashMap queries = new HashMap();
    * queries.put(title, titleQuery);
    * queries.put(date, dateQuery);
    *
    * // execute them
    * ItqlInterpreterBean itb = new ItqlInterpreterBean();
    * HashMap answers = itb.executeQueryToMap(queries);
    *
    * // get the answers
    * Answer titleAnswer = answers.get(title);
    * Answer dateAnswer = answers.get(date);
    * </pre> <p>
    *
    * Note. Each answer will be either a {@link org.mulgara.query.Answer} or a
    * {@link java.lang.String} (error) message. </p>
    *
    * @param queries a map of keys to queries to be executed
    * @return a map of answers and error messages
    */
   public Map executeQueryToMap(Map queries) {
 
     // create the answer map
     HashMap answers = new HashMap();
 
     // iterate over the queries
     for (Iterator keyIter = queries.keySet().iterator(); keyIter.hasNext(); ) {
 
       // get the key and the query
       Object key = keyIter.next();
       String query = (String) queries.get(key);
 
       // log the query we're executing
       if (log.isDebugEnabled()) {
 
         log.debug("Executing " + key + " -> " + query);
       }
 
       // end if
       // execute the query
       answers.put(key, this.executeQueryToNiceResult(query, false));
     }
 
     // end for
     // return the answers
     return answers;
   }
 
   // getSession()
 
   /**
    * Builds a {@link org.mulgara.query.Query} from the given <var>query</var>.
    *
    * @param query PARAMETER TO DO
    * @return a {@link org.mulgara.query.Query} constructed from the given
    *      <var>query</var>
    * @throws IOException if the <var>query</var> can't be buffered
    * @throws LexerException if <var>query</var> can't be tokenized
    * @throws ParserException if the <var>query</var> is not syntactically
    *      correct
    */
   public Query buildQuery(String query) throws IOException, LexerException,
       ParserException {
 
     // defer to the interpreter
     return this.interpreter.parseQuery(query);
 
   } // buildQuery()
 
   /**
    * Execute an iTQL &quot;update&quot; statement that returns no results.
    * <p>
    *   This method should be used only for commands that return no results
    *   such as <code>INSERT</code> and <code>ALIAS</code>.
    * </p>
    *
    * @param itql The statement to execute.
    *
    * @throws ItqlInterpreterException if the statement fails or the command
    *   executed returned results.
    */
   public void executeUpdate(String itql) throws ItqlInterpreterException {
 
     try {
 
       interpreter.executeCommand(itql);
     }
     catch (Exception e) {
 
       throw new ItqlInterpreterException(e);
     }
 
     ItqlInterpreterException exception = interpreter.getLastError();
     Answer answer = interpreter.getLastAnswer();
 
     if (answer != null) {
 
       try {
         answer.close();
       } catch (TuplesException te) { /* use the following exception */ }
       throw new IllegalStateException("The execute update method should not " +
           "return an Answer object!");
     }
 
     if (exception != null) {
 
       throw exception;
     }
   }
 
   /**
    * Returns true if a quit command has been entered, simply calls the
    * interpreter.isQuitRequested.
    *
    * @return true if a quit command has been entered
    *
    * @see ItqlInterpreter#isQuitRequested()
    */
   public boolean isQuitRequested() {
     return interpreter.isQuitRequested();
   }
 
 
   /**
    * Returns the {@linkplain ItqlInterpreter#getLastMessage last message} of
    * the interpreter.
    *
    * @return the results of the last command execution, null if the command did
    *      not set any message
    *
    * @see ItqlInterpreter#getLastMessage()
    */
   public String getLastMessage() {
 
     return interpreter.getLastMessage();
   }
 
 
   /**
    * Returns the {@linkplain ItqlInterpreter#getLastError last error} of the
    * interpreter.
    *
    * @return the results of the last command execution, null if the command did
    *      not set any message
    *
    * @see ItqlInterpreter#getLastError()
    */
   public ItqlInterpreterException getLastError() {
 
     return interpreter.getLastError();
   }
 
 
   /**
    * Execute an ITQL query and return an answer.
    *
    * @param itql The query to execute.
    * @return The answer to the query.
    * @throws ItqlInterpreterException if the query fails.
    */
   public Answer executeQuery(String itql) throws ItqlInterpreterException {
 
     try {
       interpreter.executeCommand(itql);
     }
     catch (Exception e) {
       throw new ItqlInterpreterException(e);
     }
 
     ItqlInterpreterException exception = interpreter.getLastError();
 
     if (exception != null) {
 
       throw exception;
     }
 
     Answer answer = interpreter.getLastAnswer();
 
     return answer;
   }
 
 
   /**
    * Load the contents of a local file into a database/model.
    * <p>
    *   The method assumes the source to be RDF/XML if an .rdf extension can
    *   not be found.  A .n3 extension assume n3 triples.
    * </p>
    * <p>
    *   Note. <var>destinationURI</var> must be a valid URI, and does not include
    *   the angle brackets (&lt; and &gt;) used to delimit URLs in iTQL.
    * </p>
    *
    * @param sourceFile  a local file containing the source data
    * @param destinationURI destination model for the source data
    * @return number of rows inserted into the destination model
    * @throws QueryException if the data fails to load or the file does not exist
    * on the local file system.
    */
   public long load(File sourceFile, URI destinationURI)
                 throws QueryException {
 
     long NumberOfStatements = 0;
     InputStream inputStream = null;
 
     // check for the local file
     if ( ! sourceFile.exists() ) {
       throw new QueryException(sourceFile+" does not exist on the local file "+
                                "system");
     }
     try {
       inputStream = sourceFile.toURL().openStream();
       NumberOfStatements =
           interpreter.load(inputStream, sourceFile.toURI(), destinationURI);
     }
     catch ( IOException ex ) {
       throw new QueryException("Unable to read the contents of "+sourceFile,
                               ex );
     }
     finally {
       if ( inputStream != null ) {
         try {
           inputStream.close();
         }
         catch (IOException ex) {}
       }
     }
     return NumberOfStatements;
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
 
     interpreter.backup(sourceURI, destinationFile);
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
 
     interpreter.backup(sourceURI, outputStream);
   }
 
   /**
    * Load the contents of an InputStream into a database/model.  The method assumes
    * the source to be RDF/XML.
    * <p>
    *   Note. <var>destinationURI</var> must be a valid URI, and does not include
    *   the angle brackets (&lt; and &gt;) used to delimit URLs in iTQL.
    * </p>
    *
    * @param inputStream a locally supplied inputstream.
    * @param destinationURI destination model for the source data
    * @return number of rows inserted into the destination model
    */
   public long load(InputStream inputStream, URI destinationURI)
                 throws QueryException {
 
     return interpreter.load(inputStream, destinationURI);
 
    }
 
   /**
    * Load the contents of an InputStream or a URI into a database/model.
    * <p>
    *   Note. <var>destinationURI</var> must be a valid URI, and does not include
    *   the angle brackets (&lt; and &gt;) used to delimit URLs in iTQL.
    * </p>
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
 
      return interpreter.load(inputStream, sourceURI, destinationURI);
   }
 
   /**
    * Restore all the data on the specified server. If the database is not
    * currently empty then the database will contain the union of its current
    * content and the content of the backup file when this method returns.
    *
    * @param inputStream An input stream to obtain the restore from.
    * @param serverURI The URI of the server to restore.
    * @throws QueryException if the restore cannot be completed.
    */
   public void restore(InputStream inputStream, URI serverURI) throws QueryException {
      interpreter.restore(inputStream, serverURI);
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
      interpreter.restore(inputStream, serverURI, sourceURI);
   }
 
 
   /**
    * @param answer the answer to convert into XML
    * @param parent the XML element to add the query solutions to
    * @throws QueryException if the solutions can't be added
    */
   private void appendSolution(Answer answer, Element parent)
     throws QueryException {
 
     try {
       Document doc = parent.getOwnerDocument();
 
       Element VARIABLES = doc.createElementNS(TQL_NS, "variables");
 
       // Add the variable list
       Element variables = (Element) parent.appendChild(VARIABLES);
 
       for (int column = 0; column < answer.getVariables().length; column++) {
 
         Element variable =
             (Element) variables.appendChild(doc.createElement(
             answer.getVariables()[column].getName()));
       }
 
       // Add any solutions
       answer.beforeFirst();
 
       while (answer.next()) {
 
         Element solution = doc.createElementNS(TQL_NS, "solution");
 
         for (int column = 0; column < answer.getVariables().length; column++) {
 
           Object object = answer.getObject(column);
 
           // If the node is null, don't add a tag at all
           if (object == null) {
 
             continue;
           }
 
           // Otherwise, add a tag for the node
           Element variable =
               (Element) solution.appendChild(doc.createElementNS(TQL_NS,
               answer.getVariables()[column].getName()));
 
           if (object instanceof Answer) {
 
             Answer tmpAnswer = null;
             try {
 
               tmpAnswer = (Answer) object;
               appendSolution(tmpAnswer, variable);
             }
             finally {
 
               if (tmpAnswer == null) {
 
                 tmpAnswer.close();
               }
             }
 
             continue;
           }
           else if (object instanceof LiteralImpl) {
 
             variable.appendChild(doc.createTextNode(
                 ( (LiteralImpl) object).getLexicalForm()));
           }
           else if (object instanceof URIReference) {
 
             variable.setAttribute("resource",
                 ((URIReference) object).getURI().toString());
           }
           else if (object instanceof BlankNode) {
 
             variable.setAttribute("blank-node", object.toString());
           }
           else {
             throw new AssertionError("Unknown RDFNode type: " +
                 object.getClass());
           }
         }
 
         parent.appendChild(solution);
       }
     }
     catch (TuplesException e) {
       throw new QueryException("Couldn't build query", e);
     }
   } // appendSolutions
 
   //
   // Internal methods
   //
 
   /**
    * Executes the <var>query</var> , returning a &quot;nice&quot; result. <p>
    *
    * The result is either a {@link java.lang.String} or a {@link
    * org.mulgara.query.Answer}. Any exceptions are logged, gobbled and return
    * as a {@link java.lang.String}. </p>
    *
    * @param query the query to execute
    * @param keepExceptions keep exceptions, don't convert to a message.
    * @return the result of the query in a &quot;nice&quot; format
    */
   private Object executeQueryToNiceResult(String query, boolean keepExceptions) {
 
     // create the result
     Object result = null;
 
     try {
 
       // get the answer to the query
       interpreter.executeCommand(query);
       Answer answer = this.interpreter.getLastAnswer();
 
       // log the query response
       if (log.isDebugEnabled()) {
 
         log.debug("Query response message = " + interpreter.getLastMessage());
       }
 
       // end if
       // turn the answer into a form we can handle
       if (answer != null) {
 
         // set this as the answer
         result = answer;
       }
       else {
 
         // get the error in an appropriate form
         if (this.interpreter.getLastError() != null) {
 
           // error has occurred at the interpreter
           if (log.isDebugEnabled()) {
 
             log.debug("Adding query error to map - " +
                 this.interpreter.getLastError());
           }
 
           // end if
           // set this as the answer
           if (keepExceptions) {
 
             result = this.interpreter.getLastError();
           }
           else {
 
             result = this.interpreter.getLastError().getMessage();
           }
         }
         else {
 
           // log that we're adding the response message
           if (log.isDebugEnabled()) {
 
             log.debug("Adding response message to map - " +
                 interpreter.getLastMessage());
           }
 
           // end if
           // set this as the answer
           result = interpreter.getLastMessage();
         }
 
         // end if
       }
 
       // end if
     }
     catch (Exception e) {
 
       if (keepExceptions) {
 
         result = e;
       }
       else {
 
         // get root cause exception
         Throwable cause = e.getCause();
         Throwable lastCause = e;
 
         while (cause != null) {
 
           lastCause = cause;
           cause = cause.getCause();
         }
 
         // end while
         // format the exception message
         String exceptionMessage = lastCause.getMessage();
 
         if (exceptionMessage == null) {
 
           exceptionMessage = lastCause.toString();
         }
 
         // end if
         // turn it into a pretty string
         exceptionMessage = "Query Error: " + exceptionMessage;
 
         // log the message
         if (log.isDebugEnabled()) {
 
           log.debug(exceptionMessage);
         }
 
         // end if
         // add the exception message to the answers
         result = exceptionMessage;
 
         // log full stack trace
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter(stringWriter);
         e.printStackTrace(printWriter);
         printWriter.flush();
         stringWriter.flush();
 
         log.error("Error while processing query: '" + query + "' " + EOL +
             stringWriter.getBuffer().toString());
       }
     }
 
     // try-catch
     // return the result
     return result;
   }
 
   /**
     * Sets the serverURI of the interpreter.
     * @param serverURI The new URI of the server for the interpreter
     */
   public void setServerURI(String serverURI) {
 
     try {
 
       // Set the server URI of the interpreter
       interpreter.setServerURI((serverURI == null) ? null : new URI(serverURI));
     } catch (URISyntaxException uriSyntaxException) {
 
       log.error("Could not change server due to bad uri syntax.",
                 uriSyntaxException);
 
       // Set the last error to be the syntax exception
       interpreter.setLastError(uriSyntaxException);
     } catch (QueryException queryException) {
 
       log.error("Could not change the server due to a query exception.",
                 queryException);
 
       // Set the last error to be a query exception
       interpreter.setLastError(queryException);
     }
   }
 
   /**
    * Sets the aliases associated with this bean.
    *
    * @param aliasMap the alias map associated with this bean
    */
   public void setAliasMap(HashMap aliasMap) {
 
     this.interpreter.setAliasMap(aliasMap);
   }
 
   /**
     * Clears the last error of the interpreter.
     */
   public void clearLastError() {
 
     // Set the last error to be null
     interpreter.setLastError(null);
   }
 
  /**
   * Ensures all resources are closed at GC.
   * Especially important if this object is in a servlet
   * container or the user has not called close().
   */
   protected void finalize() throws Throwable {
 
     try {
 
       // close the interpreter session
       this.close();
 
     } finally {
 
       super.finalize();
     }
   }
 }
