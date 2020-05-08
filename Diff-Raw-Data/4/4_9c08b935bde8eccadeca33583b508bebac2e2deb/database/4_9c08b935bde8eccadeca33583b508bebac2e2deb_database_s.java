 package ibms;
 
 import java.sql.*;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * This class contains the code which actually does the database access.
  * You should NOT use the methods in this class - use the methods in the
  * Info classes instead.
  */
 public class database
 {
     
     public static database busDatabase = null;
     
     protected String     connection_url = "";
     protected String     driver_name    = "";
     protected String     name           = "";
     protected String     user           = "";
     protected String     password       = "";
     protected Class      driver_class   = null;
     protected Connection connection     = null;
     protected ResultSet  results        = null;
     protected String     current_table  = "";
     protected Boolean    error          = false;
     
     public database(String name, String user, String password)
     {
         this(name, user, password, "jdbc:mysql://localhost:3306", "com.mysql.jdbc.Driver");
     }
     
     public database(String name, String user, String password, String connection_url, String driver_name)
     {
         this.name           = name;
         this.user           = user;
         this.password       = password;
         this.connection_url = connection_url;
         this.driver_name    = driver_name;
     }
     
     public static void openBusDatabase()
     {
         try
         {
             busDatabase = new database("12_COMP23420_H14", "12_COMP23420_H14",
                                        "bgD8PYelpadF8GMv", "jdbc:mysql://potnoodle.cs.man.ac.uk:3306",
                                        "com.mysql.jdbc.Driver");
             busDatabase.open();
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Unable to open database " + ex.getMessage());
         }
     }
     
     public static java.util.Date today()
     {
         Calendar c = new GregorianCalendar();
         c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
         return c.getTime();
     }
     
     public Boolean open()
     {
         if (find_driver())
             if (connect())
                 return true;
             else
                 return false;
             else
                 return false;
     }
     
     private Boolean execute(String sql_command)
     {
         try
         {
             Statement s = connection.createStatement();
             s.execute(sql_command);
             if (results != null) results.close();
             results = s.getResultSet();
             return true;
         }
         catch (Exception ex)
         {
             return false;
         }
     }
     
     public int record_count(String target, String source, String criteria)
     {
         try
         {
             Statement s = connection.createStatement();
             if (criteria.isEmpty())
                 s.execute("Select count(" + target + ") From " + source);
             else
                 s.execute("Select count(" + target + ") From " + source + " Where " + criteria);
             if (results != null) results.close();
             results = s.getResultSet();
             if (results.first())
                 return results.getInt(1);
             else
                 return -1;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database count failed");
         }
     }
     
     public int record_count(String target, String source, String field, Object value)
     {
         return record_count(target, source, field + " = " + value_string(value));
     }
     
     public int record_count(String target, String source, String field1, Object value1, String field2, Object value2)
     {
         return record_count(target, source, field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2));
     }
     
     public void select(String target, String source, String criteria, String order)
     {
         try
         {
             Statement s = connection.createStatement();
             String ordering = "";
             if (!order.isEmpty()) ordering = " Order By " + order;
             if (criteria.isEmpty())
                 s.execute("Select " + target + " From " + source + ordering);
             else
                 s.execute("Select " + target + " From " + source + " Where " + criteria + ordering);
             if (results != null) results.close();
             results = s.getResultSet();
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database access failed");
         }
     }
     
     public Boolean select_record(String target, String source, String field, Object value)
     {
         select("*", source, field + " = " + value_string(value), "");
         return move_first();
     }
     
     public Boolean select_record(String source, String field1, Object value1, String field2, Object value2)
     {
         select("*", source, field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2), "");
         return move_first();
     }
     
     public int find_id(String table, String field, Object value)
     {
         select(table + "_id", table, field + " = " + value_string(value), "");
         if (move_first()) return (Integer)get_field(table + "_id");
         else return 0;
     }
     
     public int find_id(String id_field, String source, String field1, Object value1, String field2, Object value2)
     {
         select(id_field, source, field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2), "");
         if (move_first()) return (Integer)get_field(id_field);
         else return 0;
     }
     
     public int[] select_ids(String id_field, String source, String order)
     {
         int    count    = record_count(id_field, source, "");
         int[]  results  = new int[count];
         select(id_field, source, "", order);
         for (int i = 0; i < count && move_next(); i = i + 1)
             results[i] = (Integer)get_field(id_field.replaceFirst("Distinct ", ""));
         return results;
     }
     
     public int[] select_ids(String id_field, String source, String field, Object value, String order)
     {
         int    count    = record_count(id_field, source, field + " = " + value_string(value));
         int[]  results  = new int[count];
         select(id_field, source, field + " = " + value_string(value), order);
         for (int i = 0; i < count && move_next(); i = i + 1)
             results[i] = (Integer)get_field(id_field.replaceFirst("Distinct ", ""));
         return results;
     }
     
     public int[] select_ids(String id_field, String source, String field1, Object value1, String field2, Object value2, String order)
     {
         int    count    = record_count(id_field, source, field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2));
         int[]  results  = new int[count];
         select(id_field, source, field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2), order);
         for (int i = 0; i < count && move_next(); i = i + 1)
             results[i] = (Integer)get_field(id_field.replaceFirst("Distinct ", ""));
         return results;
     }
     
     public int get_int(String table, int id, String field_name)
     {
         select(field_name, table, table + "_id = " + id, "");
         if (move_first())
             return (Integer)get_field(field_name);
         else return 0;
     }
     
     public String get_string(String table, int id, String field_name)
     {
         select(field_name, table, table + "_id = " + id, "");
         if (move_first())
             return (String)get_field(field_name);
         else return "";
     }
     
     public void set_value(String table, int id, String field_name, Object value)
     {
         begin_update(table, id);
         set_field(field_name, value);
         end_update();
     }
     
     public java.util.Date get_date(String table, int id, String field_name)
     {
         select(field_name, table, table + "_id = " + id, "");
         if (move_first())
             return (Date)get_field(field_name);
         else return database.today();
     }
     
     public static String join(String referring_table, String referenced_table, String referring_field)
     {
         return referring_table + " Inner Join " + referenced_table + " On (" + referenced_table + "." + referenced_table + "_id = " + referring_table + "." + referring_field + ")";
     }
     
     public Boolean move_first()
     {
         try
         {
             return results.first();
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database access failed");
         }
     }
     
     public Boolean move_next()
     {
         try
         {
             return results.next();
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database access failed");
         }
     }
     
     public Object get_field(String field_name)
     {
         try
         {
             return results.getObject(field_name);
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database access failed");
         }
     }
     
     public Boolean set_field(String field_name, Object value)
     {
         try
         {
             results.updateObject(field_name, value);
             return true;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database update failed");
         }
     }
     
     public Boolean begin_update(String table, Integer id)
     {
         try
         {
             Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             s.execute("Select * From " + table + " Where " + table + "_id = " + id.toString());
             if (results != null) results.close();
             results = s.getResultSet();
             move_first();
             error = false;
             return true;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database update failed");
         }
     }
     
     public Boolean end_update()
     {
         if (error)
             return false;
         else
             return commit();
     }
     
     public void update_record(String table, int id, Object[][] fields)
     {
         begin_update(table, id);
         for (Object field: fields)
         {
             String name  = (String)((Object[])field)[0];
             Object value = ((Object[])field)[1];
             set_field(name, value);
         }
         end_update();
     }
     
     public Boolean begin_new_record(String table)
     {
         try
         {
             Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
             s.execute("Select * From " + table + " Where " + table + "_id = 0");
             if (results != null) results.close();
             results = s.getResultSet();
             results.moveToInsertRow();
             current_table = table;
             error = false;
             return true;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database update failed");
         }
     }
     
     public Integer end_new_record()
     {
         if (error)
             return 0;
         else
             try
         {
             results.insertRow();
             results.last();
             return results.getInt(current_table + "_id");
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database update failed");
         }
     }
     
     public void new_record(String table, Object[][] fields)
     {
         begin_new_record(table);
         for (Object field: fields)
         {
             String name  = (String)((Object[])field)[0];
             Object value = ((Object[])field)[1];
             set_field(name, value);
         }
         end_new_record();
     }
     
     public Boolean select_record(String table_name, String criteria)
     {
         if (criteria.isEmpty())
             return execute("Select * From " + table_name);
         else
             return execute("Select * From " + table_name + " Where " + criteria);
     }
     
     public Boolean delete_record(String table, Integer id)
     {
         return execute("Delete From " + table + " Where " + table + "_id = " + id.toString());
     }
     
     public Boolean delete_record(String table, String criteria)
     {
         return execute("Delete From " + table + " Where " + criteria);
     }
     
     public Boolean delete_record(String table, String field, Object value)
     {
         return execute("Delete From " + table + " Where field = " + value_string(value));
     }
     
     public Boolean delete_record(String table, String field1, Object value1, String field2, Object value2)
     {
         return execute("Delete From " + table + " Where " + field1 + " = " + value_string(value1) + " And " + field2 + " = " + value_string(value2));
     }
     
     public Boolean close()
     {
         try
         {
             if (results != null) results.close();
             results      = null;
             driver_class = null;
             return disconnect();
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Closing database failed");
         }
     }
     
     public String value_string(Object x)
     {
         if (x == null                      ) return "''";
         else if(x instanceof String        ) return "'" + x.toString() + "'";
         else if(x instanceof java.util.Date) return sql_date((java.util.Date)x);
         else                                 return x.toString();
     }
     
     public static String sql_date(java.util.Date d)
     {
         Calendar c = new GregorianCalendar();
         c.setTime(d);
         return "'" + c.get(Calendar.YEAR)  + "," + (c.get(Calendar.MONTH) + 1)  + "," + c.get(Calendar.DAY_OF_MONTH)  + "'";
     }
     
     private Boolean commit()
     {
         try
         {
             results.updateRow();
             return true;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Database update failed");
         }
     }
     
     private Boolean connect()
     {
         try
         {
             connection = DriverManager.getConnection(connection_url + "/" + name, user, password);
             return true;
         }
         catch (SQLException e)
         {
             throw new InvalidQueryException("Unable to connect to database");
         }
     }
     
     private Boolean disconnect()
     {
         try
         {
             connection.close();
             connection = null;
             return true;
         }
         catch (SQLException e)
         {
             throw new InvalidQueryException("Unable to disconnect to database");
         }
     }
     
     private Boolean find_driver()
     {
         try
         {
             if (driver_class == null)
                 driver_class = Class.forName(driver_name);
             if (driver_class == null)
                 driver_class = Class.forName(driver_name, true, ClassLoader.getSystemClassLoader());
             return true;
         }
         catch (Exception ex)
         {
             throw new InvalidQueryException("Unable to find database driver");
         }
     }
     
     public int getResult(String source, String conditions) {
         try {
             String query = "select COUNT(*) from " + source + " ";
             if (conditions != null) {
                 query += "where " + conditions;
             }
             
             int values = 0;
             if (execute(query)) {
                 results.absolute(1);
                 values = results.getInt(1);
             }
             return values;
         } catch (Exception e) {
             System.out.println("Something has gone wrong.");
         }
         
         return 0;
         
     }
     
     public <T> ArrayList<T> getResult(String target, String source, String c) {
         try {
             String query = "select " + target + " from " + source + " ";
            if (conditions != null) {
                 query += "where " + c;
             }
             
             ArrayList<T> values = new ArrayList<T>();
             
             if (execute(query)) {
                 results.absolute(1);
                 do {
                     values.add((T)results.getObject(1));
                 } while (results.next());
             }
             return values;
         } catch (Exception e) {
             return null;
         }
     }
     
 }
