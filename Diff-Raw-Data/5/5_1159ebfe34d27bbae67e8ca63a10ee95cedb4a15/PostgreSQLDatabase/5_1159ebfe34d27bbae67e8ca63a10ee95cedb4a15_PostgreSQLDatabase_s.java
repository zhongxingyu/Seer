 package org.monitoring.queryapisql.preaggregation;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import org.monitoring.queryapi.Event;
 import org.monitoring.queryapi.Manager;
 
 /**
  *
  * @author Michal
  */
 public class PostgreSQLDatabase {
 
     Connection conn = null;
     List<Event> events = new LinkedList<Event>();
     PostgreSQLDatabaseMapper mapper = new PostgreSQLDatabaseMapper();
 
     public PostgreSQLDatabase() {
         try {
             Class.forName("org.postgresql.Driver");
             conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/preaggregate", "postgres", "root");
             mapper = new PostgreSQLDatabaseMapper();
         } catch (SQLException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
     }
 
     public List<Event> getAllEvents() {
         try {
             String query = "SELECT * "
                     + "FROM MeterEvent event "
                     + "ORDER BY event.time";
             PreparedStatement st = conn.prepareStatement(query);
             ResultSet result = st.executeQuery();
             return mapper.getResult(result);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public void save(Event event) {
         try {
             String query = "INSERT INTO event (source,date,value) VALUES (?,?,?)";
             PreparedStatement st = conn.prepareStatement(query);
             mapper.set(st, event);
             int result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public List<Event> getAggregate() {
         try {
             String query = "SELECT esource , sum(evalue) as sum "
                     + "FROM MeterEvent event"
                     + "GROUP BY event.edate";
             PreparedStatement st = conn.prepareStatement(query);
             ResultSet result = st.executeQuery();
             while (result.next()) {
                 //TODO
                 result.getDate("date");
                 result.getDouble("sum");
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
     public void updateAggregateWithAggFunc(String table, Date start,Date end,Date middle,String fieldTimeString, Event event) {
         try {
             String query = "select upsert('"+table+"',?,'"+fieldTimeString+"',?,?)";
         
         PreparedStatement st = conn.prepareStatement(query);
             st.setTimestamp(1, new java.sql.Timestamp(middle.getTime()));
             st.setTimestamp(2, new java.sql.Timestamp(start.getTime()));
             st.setTimestamp(3, new java.sql.Timestamp(end.getTime()));
             st.execute();
         }catch (SQLException ex) {
             ex.printStackTrace();
         }        
     }
 
     public void updateAggregate(String table, Date date, String fieldTimeString, Event event) {
         try {
             String query = "select upsertclassic('"+table+"',?,'"+fieldTimeString+"',?)";
             PreparedStatement st = conn.prepareStatement(query);
              st.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
             st.setDouble(2, event.getValue());
             st.execute();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void execute(String query) {
         try {
             PreparedStatement st = conn.prepareStatement(query);
             int result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void dropTable() {
         try {
             String query = "DROP TABLE aggregate60;";
             PreparedStatement st = conn.prepareStatement(query);
             int result = st.executeUpdate();
             query = "DROP TABLE aggregate1440";
             st = conn.prepareStatement(query);
             result = st.executeUpdate();
             query = "DROP TABLE aggregate43200";
             st = conn.prepareStatement(query);
             result = st.executeUpdate();
             query = "DROP TABLE event";
             st = conn.prepareStatement(query);
             result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void createTable(String table,  int timeActual, int timeNext, String[] fields) {
         StringBuilder builder = new StringBuilder();
         builder.append("CREATE TABLE ");
         builder.append(table);
         builder.append("(");
         builder.append("id SERIAL, ");
         builder.append("date TIMESTAMP, ");
         builder.append("source VARCHAR(100), ");
         for (Integer j = 0; j < timeNext / timeActual; j++) {
             for (String field : fields) {
                 builder.append(field).append(j).append(" DOUBLE precision DEFAULT 0,");
             }
         }
         builder.append(" CONSTRAINT pk_agg"+timeActual+"_id PRIMARY KEY (id) ");
         builder.append(")");
         execute(builder.toString());
     }
     
     public void createEventTable() {
         
         execute("CREATE TABLE event(id serial NOT NULL, source character(100),date timestamp without time zone, value integer, CONSTRAINT pk_id PRIMARY KEY (id))");
         execute("CREATE INDEX date_agg ON aggregate60 (date);");
         execute("CREATE INDEX date_event ON event (date);");
     }
     
     public void createUpsertFunctions(){
         
         try {
            String query = Manager.readFile("src/main/properties/upsert_agg_func.sql");
             PreparedStatement st = conn.prepareStatement(query);
             st.execute();
            query = Manager.readFile("src/main/properties/upsert_classic.sql");
             st = conn.prepareStatement(query);
             st.execute();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     
 }
