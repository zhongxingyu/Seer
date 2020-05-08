 /*
  * Mark Logic Interface to Relational Databases
  *
  * Copyright 2006 Jason Hunter and Ryan Grimm
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @author Jason Hunter
  * @version 1.0
  *
  */
 package com.xqdev.sql;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.text.ParseException;
 import javax.servlet.http.*;
 import javax.servlet.*;
 import org.jdom.input.SAXBuilder;
 import org.jdom.*;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Main class for supporting the sql.xqy client.
  *
  * Improvement idea: Let multiple executes happen in the same transaction.
  *
  * Improvement idea: Construct response XML document using strings rather
  * than JDOM for a performance improvement, but at the cost of sanity checking
  * and simplicity.
  */
 public class MLSQL extends HttpServlet {
 
   private ConnectionPool pool = null;
 
   static String TRY_DATABASE_CONNECTION = "select 1";
 
   String initProblemDriverUnavailable = null;
   String initProblemMissingCredential = null;
 
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
 
     String driver = null, url, user, password;
     try {
       driver = getInitParameter("driver");
       url = getInitParameter("url");
       user = getInitParameter("user");
       password = getInitParameter("password");
       if (driver == null) {
         initProblemMissingCredential = "Error: web.xml file is missing the 'driver' init parameter";
         Log.log(initProblemMissingCredential);
       }
       if (url == null) {
         initProblemMissingCredential = "Error: web.xml file is missing the 'url' init parameter";
         Log.log(initProblemMissingCredential);
       }
       if (user == null) {
         Log.log("Warning: web.xml file is missing the 'user' init parameter");
       }
       if (password == null) {
         Log.log("Warning: web.xml file is missing the 'password' init parameter");
       }
       if (initProblemMissingCredential == null) {  // good to try
         pool = new ConnectionPool(driver, url, user, password);
       }
     }
     catch (ClassNotFoundException e) {  // db driver couldn't be found
       initProblemDriverUnavailable = "Could not load driver class '" + driver + "', unable to contact database";
       Log.log(initProblemDriverUnavailable);
     }
   }
 
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
     doPost(req, res);
   }
 
   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
     res.setContentType("text/xml");
 
     Namespace sql = Namespace.getNamespace("sql", "http://xqdev.com/sql");
     Document responseDoc = new Document();
     Element root = new Element("result", sql);
     Element meta = new Element("meta", sql);
     responseDoc.setRootElement(root);
     root.addContent(meta);
 
     Document requestDoc = null;
     try {
       // Normally the request comes via the post body,
       // but we let you bookmark w/ a query string
       String postbody = req.getParameter("postbody");
       if (postbody != null) {
         SAXBuilder builder = new SAXBuilder();
         requestDoc = builder.build(new StringReader(postbody));
       }
       else {
         InputStream in = req.getInputStream();
         SAXBuilder builder = new SAXBuilder();
         requestDoc = builder.build(in);
       }
     }
     catch (Exception e) {
       addExceptions(meta, e);
       // Now write the error and return
       OutputStream out = res.getOutputStream();
       new XMLOutputter().output(responseDoc, out);
       out.flush();
       return;
     }
 
     Connection con = null;
     try {
       Namespace[] namespaces = new Namespace[]{ sql };
       XPathHelper xpath = new XPathHelper(requestDoc, namespaces);
 
       String type = xpath.getString("/sql:request/sql:type");
       String query = xpath.getString("/sql:request/sql:query");
       int maxRows = xpath.getInt("/sql:request/sql:execute-options/sql:max-rows", -1);
       int queryTimeout = xpath.getInt("/sql:request/sql:execute-options/sql:query-timeout", -1);
       int maxFieldSize = xpath.getInt("/sql:request/sql:execute-options/sql:max-field-size", -1);
       List<Element> params = xpath.getElements("/sql:request/sql:execute-options/sql:parameters/sql:parameter");
 
       con = pool.getConnection();
 
       PreparedStatement stmt = null;
 
       if (type.equalsIgnoreCase("procedure")) {
         stmt = con.prepareCall(query);
       }
       else {
         // Note this call depends on JDBC 3.0 (accompanying Java 1.4).
         // The call without the 2nd argument would work on earlier JVMs,
         // you just won't catch any generated keys.
         stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
       }
       configureStatement(stmt, maxRows, queryTimeout, maxFieldSize);
       parameterizeStatement(stmt, params);
 
       if (type.equalsIgnoreCase("select")) {
         try {
           ResultSet rs = stmt.executeQuery();
           addWarnings(meta, stmt.getWarnings());
           addResultSet(root, rs);
         }
         catch (SQLException e) {
           addExceptions(meta, e);
         }
       }
       else if (type.equalsIgnoreCase("update")) {
         try {
           int count = stmt.executeUpdate();
           addWarnings(meta, stmt.getWarnings());
           addUpdateCount(meta, count);
          try {
            addGeneratedKeys(meta, stmt.getGeneratedKeys());
          }
          catch (SQLException e) {
            // Generated keys are available on INSERT calls but not UPDATE calls
            // So catch and eat the exception that Oracle (and maybe others) will throw
          }
         }
         catch (SQLException e) {
           addExceptions(meta, e);
         }
       }
       else if (type.equalsIgnoreCase("procedure")) {
         boolean isResultSet = stmt.execute();
         if (isResultSet) {
           addResultSet(root, stmt.getResultSet());
           addOutParam(root, stmt, params);
         }
         else {
           addOutParam(root, stmt, params);
         }
       }
       else {
         try {
           boolean isResultSet = stmt.execute();
           addWarnings(meta, stmt.getWarnings());
           if (isResultSet) {
             addResultSet(root, stmt.getResultSet());
           }
           else {
             addUpdateCount(meta, stmt.getUpdateCount());
             addGeneratedKeys(meta, stmt.getGeneratedKeys());
           }
         }
         catch (SQLException e) {
           addExceptions(meta, e);
         }
       }
     }
     catch (Exception e) {
       addExceptions(meta, e);
     }
     finally {
       if (con != null) pool.returnConnection(con);
     }
 
     OutputStream out = res.getOutputStream();
     new XMLOutputter().output(responseDoc, out);
     out.flush();
   }
 
   private static void addOutParam(Element root, PreparedStatement stmt, List<Element> params) throws SQLException {
     Namespace sql = root.getNamespace();
     CallableStatement callableStmt = (CallableStatement) stmt;
 
     for (int i = 1; i < params.size() + 1; i++) {
       Element element = params.get(i - 1);
       String out = element.getAttributeValue("out");
       String paramType = element.getAttributeValue("type");
 
       if ("true".equalsIgnoreCase(out)) {
         Element parameter = new Element("parameter", sql);
         parameter.setAttribute("index", String.valueOf(i));
         parameter.setText(TypeMapping.getStringValue(callableStmt, paramType, i));
         root.addContent(parameter);
       }
     }
   }
 
   private static void addUpdateCount(Element meta, int count) {
     Namespace sql = meta.getNamespace();
     meta.addContent(
             new Element("rows-affected", sql).setText("" + count)
     );
   }
 
   private static void addGeneratedKeys(Element meta, ResultSet keys) throws SQLException {
     Namespace sql = meta.getNamespace();
     while (keys.next()) {  // should only be one
       meta.addContent(
               new Element("generated-key", sql).setText("" + keys.getString(1))
       );
     }
   }
 
   private static void addResultSet(Element root, ResultSet rs) throws SQLException {
     Namespace sql = root.getNamespace();
 
     ResultSetMetaData rsmd = rs.getMetaData();
     int columnCount = rsmd.getColumnCount();
     while (rs.next()) {
       Element tuple = new Element("tuple", sql);
       for (int i = 1; i <= columnCount; i++) {
         String colName = rsmd.getColumnName(i);  // names aren't guaranteed OK in xml
         String colTypeName = rsmd.getColumnTypeName(i);
         String colValue = rs.getString(i);
         boolean wasNull = rs.wasNull();
         Element elt = new Element(colName);
         if (wasNull) {
           elt.setAttribute("null", "true");
         }
         if ("UNKNOWN".equalsIgnoreCase(colTypeName)) {
           tuple.addContent(elt.setText("UNKNOWN TYPE"));  // XXX ugly
         }
         else {
           tuple.addContent(elt.setText(colValue));
         }
       }
       root.addContent(tuple);
     }
   }
 
   private static void addExceptions(Element meta, Throwable t) {
     if (t == null) return;
 
     Namespace sql = meta.getNamespace();
     Element exceptions = new Element("exceptions", sql);
     meta.addContent(exceptions);
     do {
       exceptions.addContent(
               new Element("exception", sql)
                       .setAttribute("type", t.getClass().getName())
                       .addContent(new Element("reason", sql).setText(t.getMessage()))
       );
       Log.log(t);
       t = t.getCause();
     } while (t != null);
   }
 
   private static void addExceptions(Element meta, SQLException e) {
     if (e == null) return;
 
     Namespace sql = meta.getNamespace();
     Element exceptions = new Element("exceptions", sql);
     meta.addContent(exceptions);
     do {
       exceptions.addContent(
               new Element("exception", sql)
                       .setAttribute("type", e.getClass().getName())
                       .addContent(new Element("reason", sql).setText(e.getMessage()))
                       .addContent(new Element("sql-state", sql).setText(e.getSQLState()))
                       .addContent(new Element("vendor-code", sql).setText("" + e.getErrorCode()))
       );
       e = e.getNextException();
     } while (e != null);
   }
 
   private static void addWarnings(Element meta, SQLWarning w) {
     if (w == null) return;
 
     Namespace sql = meta.getNamespace();
     Element warnings = new Element("warnings", sql);
     meta.addContent(warnings);
     do {
       warnings.addContent(
               new Element("warning", sql)
                       .setAttribute("type", w.getClass().getName())
                       .addContent(new Element("reason", sql).setText(w.getMessage()))
                       .addContent(new Element("sql-state", sql).setText(w.getSQLState()))
                       .addContent(new Element("vendor-code", sql).setText("" + w.getErrorCode()))
       );
       w = w.getNextWarning();
     } while (w != null);
   }
 
   private static void configureStatement(PreparedStatement stmt, int maxRows, int queryTimeout, int maxFieldSize)
         throws SQLException {
     if (maxRows != -1) {
       stmt.setMaxRows(maxRows);
     }
     if (queryTimeout != -1) {
       stmt.setQueryTimeout(queryTimeout);
     }
     if (maxFieldSize != -1) {
       stmt.setMaxFieldSize(maxFieldSize);
     }
   }
 
   private static void parameterizeStatement(PreparedStatement stmt, List<Element> params)
           throws SQLException, NumberFormatException, ParseException {
     // Presently we accept these types:
     // boolean, date, double, float, int,
     // long, short, string, time, timestamp.
     // We also accept a null flag.
     // XXX Might be nice to support blobs and clobs, BigDecimal
     int paramPosition = 0;
     for (Element param : params) {
       paramPosition++;
       String paramType = param.getAttributeValue("type");
       String outType = param.getAttributeValue("out");
 
       if ("true".equalsIgnoreCase(outType)) {
         if (!(stmt instanceof CallableStatement)) {
             String s = "Out parameters only allowed on stored procedures";
             Log.log(s);
             throw new RuntimeException(s);
         }
 
         ((CallableStatement)stmt).registerOutParameter(paramPosition, TypeMapping.getSqlDataType(paramType));
       }
       else {
         String paramValue = param.getText();
         boolean paramNull = "true".equalsIgnoreCase(param.getAttributeValue("null"));
 
         if (paramType == null) {
           String s = "No parameter type received: " + paramType + " with value: " + paramValue;
           Log.log(s);
           throw new RuntimeException(s);
         }
 
         TypeMapping.parameterize(paramType, paramNull, stmt, paramPosition, paramValue);
       }
     }
   }
 }
