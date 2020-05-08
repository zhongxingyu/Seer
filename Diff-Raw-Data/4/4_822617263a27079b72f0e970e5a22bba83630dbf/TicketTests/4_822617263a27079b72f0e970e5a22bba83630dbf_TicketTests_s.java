 package com.vaadin.addon.sqlcontainer;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
 import com.vaadin.addon.sqlcontainer.query.FreeformQuery;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Window;
 
 public class TicketTests {
 
     @Test
     public void ticket5867_throwsIllegalState_transactionAlreadyActive()
             throws SQLException {
         SimpleJDBCConnectionPool connectionPool = new SimpleJDBCConnectionPool(
                "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/sqlcontainer",
                "sqlcontainer", "sqlcontainer", 2, 2);
         SQLContainer container = new SQLContainer(new FreeformQuery(
                 "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
         Table table = new Table();
         Window w = new Window();
         w.addComponent(table);
         table.setContainerDataSource(container);
     }
 }
