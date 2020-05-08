 package org.pillarone.riskanalytics.core.output.batch.results;
 
 import groovy.sql.Sql;
 import org.pillarone.riskanalytics.core.output.SimulationRun;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 class MysqlBulkInsert extends AbstractResultsBulkInsert {
 
     protected void writeResult(List values) {
         try {
             for (Object value : values) {
                 String toWrite = value != null ? value.toString() : "null";
                 getWriter().append(toWrite);
             }
            getWriter().append(";");
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public synchronized void setSimulationRun(SimulationRun simulationRun) {
         super.setSimulationRun(simulationRun);
         Sql sql = new Sql(simulationRun.getDataSource());
         try {
             sql.execute("ALTER TABLE single_value_result ADD PARTITION (PARTITION P" + getSimulationRunId() + " VALUES IN (" + getSimulationRunId() + "))");
         } catch (Exception ex) {
             deletePartitionIfExist(sql, getSimulationRunId());
             try {
                 sql.execute("ALTER TABLE single_value_result ADD PARTITION (PARTITION P" + getSimulationRunId() + " VALUES IN (" + getSimulationRunId() + "))");
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
 
     public void save() {
         long time = System.currentTimeMillis();
         Sql sql = new Sql(getSimulationRun().getDataSource());
         String query = "LOAD DATA LOCAL INFILE '" + getTempFile().getAbsolutePath() + "' INTO TABLE single_value_result FIELDS TERMINATED BY ',' LINES TERMINATED BY ';' (simulation_run_id, period, iteration, path_id, field_id, collector_id, value, value_index, date)";
         try {
             int numberOfResults = sql.executeUpdate(query.replaceAll("\\\\", "/"));
             time = System.currentTimeMillis() - time;
             LOG.info(numberOfResults + " results saved in " + time + " ms");
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         sql.close();
     }
 
     /**
      * if the partition exists before, it causes an exception
      *
      * @param sql
      * @param partitionName
      */
     private void deletePartitionIfExist(Sql sql, long partitionName) {
         try {
             sql.execute("ALTER TABLE single_value_result DROP PARTITION P" + partitionName);
         } catch (Exception e) {//the partition was not created yet
         }
     }
 }
