 package org.sourceforge.uptodater;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.*;
 import java.util.regex.Pattern;
 
 public abstract class ChangeExecutor {
 
     private static Log logger = LogFactory.getLog(ChangeExecutor.class);
 
     public static interface DeferredStatement {
         void doIt() throws SQLException;
         Statement getStatement();
     }
 
     protected String sqlText;
     protected Connection connection;
     protected boolean optional;
 
     public static final Pattern NEXT_STATEMENT_IGNORE_HINT =  Pattern.compile("^--\\s*optional\\s*$", Pattern.MULTILINE);
 
     public boolean isOptional() {
         return optional;
     }
 
     public void setOptional(boolean optional) {
         this.optional = optional;
     }
 
     private ChangeExecutor(String sqlText) {
         this.sqlText = sqlText;
     }
 
     public final void execute(Connection con) throws SQLException {
         this.connection = con;
         DeferredStatement deferredStatement = null;
         try {
             deferredStatement = createDeferredStatement();
             deferredStatement.doIt();
         } finally {
             DBUtil.close(deferredStatement.getStatement());
         }
     }
 
 
     abstract DeferredStatement createDeferredStatement() throws SQLException;
 
 
     public static ChangeExecutor createChangeExecutor(final String sqlText) {
         String lowerCaseSqlText = sqlText.trim().toLowerCase();
         ChangeExecutor changeExecutor = null;
         if(lowerCaseSqlText.startsWith("call")) {
             changeExecutor = new CallableChangeExecutor(sqlText);
         } else {
             changeExecutor = new StatementChangeExecutor(sqlText);
         }
         // check flags
         if (NEXT_STATEMENT_IGNORE_HINT.matcher(sqlText).lookingAt()) {
             changeExecutor.setOptional(true);
         }
         return changeExecutor;
     }
 
     public static class CallableChangeExecutor extends ChangeExecutor {
         CallableStatement stmt;
 
         private CallableChangeExecutor(String sqlText) {
             super(sqlText);
         }
         public DeferredStatement createDeferredStatement() throws SQLException {
             stmt = connection.prepareCall(sqlText);
             return new DeferredStatement() {
 
                 public void doIt() throws SQLException {
                     try {
                         stmt.execute();
                     } catch (SQLException e) {
                         if (isOptional()) {
                            logger.warn("Optional statement failed to run: " + sqlText, e);
                         } else {
                             throw e;
                         }
                     }
                 }
 
                 public Statement getStatement() {
                     return stmt;
                 }
             };
         }
     }
 
     public static class StatementChangeExecutor extends ChangeExecutor {
         Statement stmt;
 
         private StatementChangeExecutor (String sqlText) {
             super(sqlText);
         }
 
         public DeferredStatement createDeferredStatement() throws SQLException {
             stmt = connection.createStatement();
             return new DeferredStatement() {
 
                 public void doIt() throws SQLException {
                     try {
                         stmt.executeUpdate(sqlText);
                     } catch (SQLException e) {
                         if (isOptional()) {
                             logger.warn("Optional statement failed to run: " + sqlText, e);
                         } else {
                             throw e;
                         }
                     }
                 }
 
                 public Statement getStatement() {
                     return stmt;
                 }
             };
         }
     }
 }
