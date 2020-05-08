 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.reporting.dbwriter;
 
 import com.sun.grid.logging.SGELog;
 import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.*;
 import com.sun.grid.reporting.dbwriter.db.*;
 import com.sun.grid.reporting.dbwriter.file.*;
 import com.sun.grid.reporting.dbwriter.model.StatisticRuleType;
 import java.math.BigDecimal;
 import java.sql.Connection;
 
 public class StatisticManager extends StoredRecordManager {
    static String primaryKeyFields[] = {
       "s_name",
    };
    
    protected Map statisticsMap;
    
    /** Creates a new instance of StatisticManager */
    public StatisticManager(Database p_database, Controller controller, ValueRecordManager p_valueManager)
    throws ReportingException {
       
       super(p_database, "sge_statistic", "s_", false, primaryKeyFields, null, controller);
       
       statisticsMap = new HashMap();
       statisticsMap.put("s_name", "s_name");
       
       valueManager = p_valueManager;
    }
    
    public void initRecordFromEvent(Record statistic, RecordDataEvent e) {
       initRecordFromEventData(statistic, e.data, statisticsMap);
    }
    
    public void initSubRecordsFromEvent(Record obj, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
       if(e.reportingSource == ReportingSource.DBWRITER_STATISTIC ||
             e.reportingSource == ReportingSource.DATABASE_STATISTIC ) {
          valueManager.handleNewSubRecord(obj, e, connection);
       }
    }
    
    public Record findRecord(RecordDataEvent e, java.sql.Connection connection ) throws ReportingException {
       return findRecordFromEventData(e.data, statisticsMap, connection);
    }
    
    
    /**
     * Create a event object which can be process be the <code>processRecord</code>
     * method of this class
     *
     *
     * @param sourceObj   the source obj
     * @param source      The reporting source
     * @param ts          timestamp for the event object
     * @param name        category of the statistic
     * @param variable    variable name of the statistic
     * @param value       numerical values of the statistic
     * @return the event object
     */
    public static RecordDataEvent createStatisticEvent(Object sourceObj, ReportingSource source, long ts, String name, String variable, double value) {
       Map fieldMap = new HashMap(4);
       
       StringField typeField = new StringField("type");
       typeField.setValue("statistic");
       
       DateField timeField = new DateField("time");
       timeField.setValue(new Timestamp(ts));
       StringField nameField = new StringField("s_name");
       nameField.setValue(name);
       
       DoubleField valueField = new DoubleField(variable);
       valueField.setValue(value);
       
       fieldMap.put(typeField.getName(), typeField);
       fieldMap.put(timeField.getName(), timeField);
       fieldMap.put(nameField.getName(), nameField);
       fieldMap.put(valueField.getName(), valueField);
       
       return new RecordDataEvent(sourceObj, source, fieldMap, null);
    }
    
    
    /**
     * Get the timestamp of the next calculation of the statistic rule.
     *
     * @param rule   the statistic rule
     * @param connection  connection to the database
     * @throws com.sun.grid.reporting.dbwriter.ReportingException
     * @return timestamp of the next calculation
     */
    public Timestamp getNextCalculation(StatisticRuleType rule, java.sql.Connection connection) throws ReportingException {
       // Determine if this rules has to be executed
       // We search the last entry in the database for the calculated variable
       Timestamp lastEntryTimestamp = valueManager.getLastEntryTime(new BigDecimal("-1"), rule.getVariable(), connection);
       
       Calendar cal = Calendar.getInstance();
       cal.setTime(lastEntryTimestamp);
       
       if("hour".equals(rule.getInterval())) {
          cal.add(Calendar.HOUR_OF_DAY, 1);
       } else if ("day".equals(rule.getInterval())) {
          cal.add(Calendar.DAY_OF_MONTH, 1);
       } else if ("month".equals(rule.getInterval())) {
          cal.add(Calendar.MONTH, 1);
       } else if ("year".equals(rule.getInterval())) {
          cal.add(Calendar.YEAR, 1);
       }
       
       return new Timestamp(cal.getTimeInMillis());
    }
    
    public boolean validateStatisticRules(StatisticRuleType rule) {
       boolean executeIt = true;
       if (!rule.isSetVariable()) {
          SGELog.warning("StatisticManager.missingAttribute", "variable");
          executeIt = false;
       }
       if (!rule.isSetInterval()) {
          SGELog.warning("StatisticManager.missingAttribute", "interval");
          executeIt = false;
       }
       if (!rule.isSetSql()) {
          SGELog.warning("StatisticManager.missingAttribute", "sql");
          executeIt = false;
       }
       
       return executeIt;
    }
    
    /**
     * Calculate the statistic for a rule and store in the the database
     * @param rule the statistic rule
     * @param connection the connection to the database
     * @throws com.sun.grid.reporting.dbwriter.ReportingException
     */
    public void calculateStatistic(StatisticRuleType rule, Connection connection) throws ReportingException {
       boolean seriesFromRows = false;
      if("seriesFromRows".equals(rule.getType().value())) {
          seriesFromRows = true;
      } else if ("seriesFromColumns".equals((rule.getType().value()))) {
          seriesFromRows = false;
       } else {
          throw new ReportingException("StatisticManager.invalidStatisticType", rule.getType());
       }

       long timestamp = System.currentTimeMillis();
       
       SGELog.config("StatisticManager.executeRule", rule.getVariable());
       
       Statement stmt = database.executeQuery(rule.getSql(), connection);
       
       try {
          ResultSet rs = stmt.getResultSet();
          try {
             if(seriesFromRows) {
                String nameColumn = rule.getNameColumn();
                if(nameColumn == null) {
                   throw new ReportingException("StatisticManager.nameColumnRequired");
                }
                
                String valueColumn = rule.getValueColumn();
                if(valueColumn == null) {
                   throw new ReportingException("StatisticManager.valueColumnRequired");
                }
                while(rs.next()) {
                   
                   String name = rs.getString(nameColumn);
                   double value = rs.getDouble(valueColumn);
                   RecordDataEvent evt = createStatisticEvent(this, ReportingSource.DATABASE_STATISTIC,
                         timestamp, name, rule.getVariable(), value);
                   controller.processStatisticData(evt, connection);
                }
             } else {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                if(rs.next()) {
                   for(int i = 1; i <= colCount; i++) {
                      String name = meta.getColumnName(i);
                      double value = rs.getDouble(i);
                      RecordDataEvent evt = createStatisticEvent(this, ReportingSource.DATABASE_STATISTIC,
                            timestamp, name, rule.getVariable(), value);
                      controller.processStatisticData(evt, connection);
                   }
                }
             }
          } finally {
             try {
                rs.close();
                stmt.close();
             } catch(SQLException e) {
                // Ignore
             }
          }
       } catch(SQLException e) {
          SGELog.severe(e, "StatisticManager.ruleSQLError", rule.getVariable());
       }
    }
    
    public Record newDBRecord() {
       return new Statistic(this);
    }
    
 }
