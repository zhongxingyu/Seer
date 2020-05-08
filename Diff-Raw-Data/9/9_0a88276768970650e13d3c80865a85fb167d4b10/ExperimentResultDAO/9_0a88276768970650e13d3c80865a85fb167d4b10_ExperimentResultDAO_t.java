 package edacc.model;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
import java.sql.Time;
 import java.util.Vector;
 
 public class ExperimentResultDAO {
 
     protected static PreparedStatement curSt = null;
     protected static final String table = "ExperimentResults";
     protected static final String insertQuery = "INSERT INTO " + table + " (run, status, seed, resultFileName, time, statusCode, " +
             "SolverConfig_idSolverConfig, Experiment_idExperiment, Instances_idInstance) VALUES (?,?,?,?,?,?,?,?,?)";
     protected static final String deleteQuery = "DELETE FROM " + table + " WHERE idJob=?";
 
     public static ExperimentResult createExperimentResult(int run, int status, int seed, float time, int statusCode, int SolverConfigId, int ExperimentId, int InstanceId) throws SQLException {
         ExperimentResult r = new ExperimentResult(run, status, seed, time, statusCode, SolverConfigId, ExperimentId, InstanceId);
         r.setNew();
         return r;
     }
 
     public static void batchSave(Vector<ExperimentResult> v) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery);
             curSt = st;
             for (ExperimentResult r : v) {
                 st.setInt(1, r.getRun());
                 st.setInt(2, r.getStatus());
                 st.setInt(3, r.getSeed());
                 st.setString(4, r.getResultFileName());
                 st.setFloat(5, r.getTime());
                 st.setInt(6, r.getStatusCode());
                 st.setInt(7, r.getSolverConfigId());
                 st.setInt(8, r.getExperimentId());
                 st.setInt(9, r.getInstanceId());
                 st.addBatch();
                 r.setSaved();
                 /* this should only be done if the batch save actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
 
     }
 
     /**
      * Updates the run property of the ExperimentResults at once (batch).
      * @param v vector of ExperimentResults to be updated
      * @throws SQLException
      */
     public static void batchUpdateRun(Vector<ExperimentResult> v) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             final String query = "UPDATE " + table + " SET run=? WHERE idJob=?";
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(query);
             curSt = st;
             for (ExperimentResult r : v) {
                 st.setInt(1, r.getRun());
                 st.setInt(2, r.getId());
                 st.addBatch();
                 r.setSaved();
                 /* this should only be done if the batch update actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
     }
 
     /**
      * Deletes all experiment results at once (batch).
      * @param experimentResults the experiment results to be deleted
      * @throws SQLException
      */
     public static void deleteExperimentResults(Vector<ExperimentResult> experimentResults) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(deleteQuery);
             curSt = st;
             for (ExperimentResult r : experimentResults) {
                 st.setInt(1, r.getId());
                 st.addBatch();
                 r.setDeleted();
                 /* this should only be done if the batch delete actually
                  * gets commited, right now this might not be the case
                  * if there's an DB exception or the executeBatch() is
                  * cancelled (see cancelStatement()).
                  * Without caching this might not be a problem.
                  */
             }
             st.executeBatch();
             st.close();
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             curSt = null;
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
     }
 
     private static ExperimentResult getExperimentResultFromResultSet(ResultSet rs) throws SQLException {
         ExperimentResult r = new ExperimentResult();
         r.setId(rs.getInt("idJob"));
         r.setRun(rs.getInt("run"));
         r.setStatus(rs.getInt("status"));
         r.setSeed(rs.getInt("seed"));
         r.setResultFileName(rs.getString("resultFileName"));
         r.setTime(rs.getFloat("time"));
         r.setStatusCode(rs.getInt("statusCode"));
         r.setSolverConfigId(rs.getInt("SolverConfig_idSolverConfig"));
         r.setInstanceId(rs.getInt("Instances_idInstance"));
         r.setExperimentId(rs.getInt("Experiment_idExperiment"));
         if (r.getStatus() == 0) {
            try {
                r.setMaxTimeLeft(rs.getTime("maxTimeLeft"));
            } catch (Exception e) {
                // happens if the maxTimeLeft field is negative
                r.setMaxTimeLeft(new Time(0));
            }
         } else {
             r.setMaxTimeLeft(null);
         }
         return r;
     }
 
     /**
      * returns the number of jobs in the database for the given experiment
      * @param id experiment id
      * @return
      * @throws SQLException
      */
     public static int getCountByExperimentId(int id) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT COUNT(*) as count FROM " + table + " WHERE Experiment_idExperiment=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         rs.next(); // there will always be a count
         int count = rs.getInt("count");
         rs.close();
         return count;
     }
 
     /**
      * checks the database if a job with the given parameters already exists
      * @param run
      * @param solverConfigId
      * @param InstanceId
      * @param ExperimentId
      * @return bool
      * @throws SQLException
      */
     public static boolean jobExists(int run, int solverConfigId, int InstanceId, int ExperimentId) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT COUNT(*) as count FROM " + table + " " +
                 "WHERE run=? AND SolverConfig_idSolverConfig=? AND Instances_idInstance=? AND Experiment_idExperiment=? ;");
         st.setInt(1, run);
         st.setInt(2, solverConfigId);
         st.setInt(3, InstanceId);
         st.setInt(4, ExperimentId);
         ResultSet rs = st.executeQuery();
         rs.next();
         int count = rs.getInt("count");
         rs.close();
         return count > 0;
     }
 
     /**
      * returns the seed value of the job specified by the given parameters
      * @param run
      * @param solverConfigId
      * @param InstanceId
      * @param ExperimentId
      * @return bool
      * @throws SQLException
      */
     public static int getSeedValue(int run, int solverConfigId, int InstanceId, int ExperimentId) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT seed FROM " + table + " " +
                 "WHERE run=? AND SolverConfig_idSolverConfig=? AND Instances_idInstance=? AND Experiment_idExperiment=? ;");
         st.setInt(1, run);
         st.setInt(2, solverConfigId);
         st.setInt(3, InstanceId);
         st.setInt(4, ExperimentId);
         ResultSet rs = st.executeQuery();
         rs.next();
         int seed = rs.getInt("seed");
         rs.close();
         return seed;
     }
 
     /**
      * returns all jobs of the given Experiment
      * @param id
      * @return ExperimentResults vector
      * @throws SQLException
      */
     public static Vector<ExperimentResult> getAllByExperimentId(int id) throws SQLException {
         Vector<ExperimentResult> v = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=?;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             v.add(er);
             er.setSaved();
         }
         rs.close();
         st.close();
         return v;
     }
 
     /**
      * Returns all runs for an experiment specified by id
      * @param id the experiment id
      * @return vector of run numbers
      * @throws SQLException
      */
     public static Vector<Integer> getAllRunsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT run " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY run ORDER BY run;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     /**
      * Returns all instance ids which have jobs in the database and are associated
      * with the experiment specified by id
      * @param id the experiment id
      * @return vector of solver config ids
      * @throws SQLException
      */
     public static Vector<Integer> getAllInstanceIdsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT Instances_idInstance " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY Instances_idInstance ORDER BY Instances_idInstance;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     /**
      * Returns all solver config ids which have jobs in the database and are associated
      * with the experiment specified by id.
      * @param id the experiment id
      * @return vector of solver config ids
      * @throws SQLException
      */
     public static Vector<Integer> getAllSolverConfigIdsByExperimentId(int id) throws SQLException {
         Vector<Integer> res = new Vector<Integer>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT SolverConfig_idSolverConfig " +
                 "FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? GROUP BY SolverConfig_idSolverConfig ORDER BY SolverConfig_idSolverConfig;");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             res.add(rs.getInt(1));
         }
 
         return res;
     }
 
     public static Vector<ExperimentResult> getAllByExperimentIdAndRun(int eid, int run) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? AND run=?;");
         st.setInt(1, eid);
         st.setInt(2, run);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static int getInstanceCountByExperimentId(int id) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT COUNT(*) FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? " +
                 "GROUP BY Instance_idInstance");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             return rs.getInt(1);
         }
         return 0;
     }
 
     public static int getSolverConfigCountByExperimentId(int id) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT COUNT(*) FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? " +
                 "GROUP BY SolverConfig_idSolverConfig");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             return rs.getInt(1);
         }
         return 0;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfiguration(SolverConfiguration sc) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=?;");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfigurationAndStatus(SolverConfiguration sc, int status) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=? AND status=?;");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         st.setInt(3, status);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
 
     public static double getMaxCalculationTimeForSolverConfiguration(SolverConfiguration sc, int status, int run) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT MAX(time) " +
                 "FROM " + table + " " +
                 "WHERE SolverConfig_idSolverConfig=? AND status=? AND run=?;");
         st.setInt(1, sc.getId());
         st.setInt(2, status);
         st.setInt(3, run);
         ResultSet rs = st.executeQuery();
         if (rs.next()) {
             return rs.getDouble(1);
         } else {
             return 0.;
         }
     }
 
     public static Vector<ExperimentResult> getAllByExperimentHasInstance(ExperimentHasInstance ehi) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? AND Instances_idInstance=?;");
         st.setInt(1, ehi.getExperiment_id());
         st.setInt(2, ehi.getInstances_id());
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static Vector<ExperimentResult> getAllBySolverConfigurationAndRunAndStatusOrderByTime(SolverConfiguration sc, int run, int status) throws SQLException {
         Vector<ExperimentResult> res = new Vector<ExperimentResult>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(
                 "SELECT idJob, run, status, seed, resultFileName, time, statusCode, SolverConfig_idSolverConfig, " +
                 "Experiment_idExperiment, Instances_idInstance, TIMEDIFF(curTime(), startTime) AS maxTimeLeft FROM " + table + " " +
                 "WHERE Experiment_idExperiment=? AND SolverConfig_idSolverConfig=? AND status=? AND run=? " +
                 "ORDER BY time");
         st.setInt(1, sc.getExperiment_id());
         st.setInt(2, sc.getId());
         st.setInt(3, status);
         st.setInt(4, run);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             ExperimentResult er = getExperimentResultFromResultSet(rs);
             res.add(er);
             er.setSaved();
         }
         return res;
     }
 
     public static void setAutoCommit(boolean commit) throws SQLException {
         DatabaseConnector.getInstance().getConn().setAutoCommit(commit);
     }
 
     public static void cancelStatement() throws SQLException {
         if (curSt != null) {
             try {
                 curSt.cancel();
             } catch (Exception _) {
             }
         }
     }
 }
