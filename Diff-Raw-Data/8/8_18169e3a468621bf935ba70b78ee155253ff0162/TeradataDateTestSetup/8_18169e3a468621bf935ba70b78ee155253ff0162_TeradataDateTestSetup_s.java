 package org.geotools.data.teradata;
 
 import org.geotools.jdbc.JDBCDateTestSetup;
 import org.geotools.jdbc.JDBCTestSetup;
 
 public class TeradataDateTestSetup extends JDBCDateTestSetup {
 
 
     public TeradataDateTestSetup(JDBCTestSetup delegate) {
         super(delegate);
     }
 
 
     protected void createDateTable() throws Exception {
         run("CREATE TABLE dates (d DATE, dt TIMESTAMP, t TIME)");
 
         //_date('1998/05/31:12:00:00AM', 'yyyy/mm/dd:hh:mi:ssam'));

        run("INSERT INTO DATES VALUES (DATE '1969-12-23' (format 'yyyy-MM-dd'), '2009-06-28 15:12:41' (TIMESTAMP, format 'YYYY-MM-ddBHH:mi:ss'), '15:12:41' (TIMESTAMP, format 'HH:mi:ss')  );");
        run("INSERT INTO DATES VALUES (DATE '2009-01-15' (format 'yyyy-MM-dd'), '2009-01-15 13:10:12' (TIMESTAMP, format 'YYYY-MM-ddBHH:mi:ss'), '13:10:12' (TIMESTAMP, format 'HH:mi:ss')  );");
        run("INSERT INTO DATES VALUES (DATE '2009-09-29' (format 'yyyy-MM-dd'), '2009-09-29 17:54:23' (TIMESTAMP, format 'YYYY-MM-ddBHH:mi:ss'), '17:54:23' (TIMESTAMP, format 'HH:mi:ss')  );");
 
     }
 
 
     protected void dropDateTable() throws Exception {
         runSafe("DROP TABLE DATES");
     }
 
 }
