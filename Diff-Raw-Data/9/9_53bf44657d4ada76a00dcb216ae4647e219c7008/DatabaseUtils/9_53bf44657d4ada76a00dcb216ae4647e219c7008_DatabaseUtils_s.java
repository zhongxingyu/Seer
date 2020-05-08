 /**
  *
  * Copyright 2010, 2011, 2012 Altruix Software Development e. U.
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.impl.util;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.slf4j.Logger;
 
 /**
  * @author DP118M
  * 
  */
 public class DatabaseUtils {
     public static void closeStatement(final Statement aStatement,
             final Logger aLogger) {
         if (aStatement != null) {
             try {
                 aStatement.close();
             } catch (final SQLException exception) {
                 aLogger.error("", exception);
             }
         }
     }
 
     public static ResultSet executeQuery(final String aQuery,
             final Logger aLogger, final Statement aStatement) {
         ResultSet resultSet = null;
         try {
             resultSet = aStatement
                     .executeQuery(aQuery);
         } catch (final SQLException exception) {
             aLogger.error("", exception);
             throw new RuntimeException(exception);
         }
         return resultSet;
     }
     public static int executeQueryWithSingleIntResult(final String aQuery,
             final Statement aStatement, final Logger aLogger) {
         ResultSet resultSet = null;
         try {
             resultSet =
                     DatabaseUtils.executeQuery(aQuery, aLogger, aStatement);
             resultSet.next();
             return resultSet.getInt(1);
         } catch (final SQLException exception) {
             aLogger.error("", exception);
             throw new RuntimeException(exception);
         } finally {
             closeResultSet(resultSet, aLogger);
         }
     }
     public static void closeResultSet(final ResultSet aResultSet, final Logger aLogger) {
         if (aResultSet != null) {
             try {
                 aResultSet.close();
             } catch (final SQLException exception) {
                 aLogger.error("", exception);
             }
         }
     }
     public static long executeQueryWithSingleLongResult(final String aQuery,
             final Statement aStatement, final Logger aLogger) {
         ResultSet resultSet = null;
         try {
             resultSet = DatabaseUtils.
                     executeQuery(aQuery, aLogger, aStatement);
             if (resultSet.next()) {
                 return resultSet.getLong(1);
             } else {
                 return -1;
             }
         } catch (final SQLException exception) {
             aLogger.error("", exception);
             throw new RuntimeException(exception);
         } finally {
             DatabaseUtils.closeResultSet(resultSet, aLogger);
         }
     }
     public static String substituteUserId(final long aUserId, final String aTemplate) {
         return aTemplate
                 .replace("@{userId}", Long.toString(aUserId));
     }
     
 }
