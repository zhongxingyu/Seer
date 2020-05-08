 package com.aconex.scrutineer.jdbc;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyNoMoreInteractions;
 import static org.mockito.Mockito.when;
 
 public class JdbcIdAndVersionStreamTest {
 
     private static final String SQL = "select id, version from tablename order by id";
     @Mock
     private DataSource dataSource;
     @Mock
     private Connection connection;
     @Mock
     private Statement statement;
     @Mock
     private ResultSet resultSet;
 
     @Before
     public void setup() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void shouldOpenNewJdbcConnection() throws SQLException {
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.open();
         verify(dataSource).getConnection();
     }
 
     @Test
     public void shouldCloseConnection() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.open();
         jdbcIdAndVersionStream.close();
         verify(connection).close();
     }
 
     @Test
     public void closeShouldDoNothingIfNotOpen() throws SQLException {
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.close();
         verifyNoMoreInteractions(dataSource);
     }
 
     @Test
     public void shouldExecuteSQLQuery() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         when(connection.createStatement()).thenReturn(statement);
         //TODO: Handle scrolling properly
         when(statement.executeQuery(SQL)).thenReturn(resultSet);
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.open();
         IdAndVersionResultSetIterator iterator = (IdAndVersionResultSetIterator) jdbcIdAndVersionStream.iterator();
         assertThat(iterator.getResultSet(), is(resultSet));
         verify(statement).executeQuery(SQL);
     }
 
     @Test
     public void shouldTearDownStatement() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         when(connection.createStatement()).thenReturn(statement);
         when(statement.executeQuery(SQL)).thenReturn(resultSet);
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.open();
         jdbcIdAndVersionStream.iterator();
         jdbcIdAndVersionStream.close();
         verify(resultSet).close();
         verify(statement).close();
     }
 
     @Test(expected = RuntimeException.class)
     public void shouldThrowExceptionIfResultSetFailsToClose() throws SQLException {
         try {
             when(dataSource.getConnection()).thenReturn(connection);
             when(connection.createStatement()).thenReturn(statement);
             when(statement.executeQuery(SQL)).thenReturn(resultSet);
         } catch (SQLException e) {
             e.printStackTrace();
             fail("Unexpected exception");
         }
         doThrow(new SQLException()).when(resultSet).close();
         JdbcIdAndVersionStream jdbcIdAndVersionStream = new JdbcIdAndVersionStream(dataSource, SQL);
         jdbcIdAndVersionStream.open();
         jdbcIdAndVersionStream.iterator();
         jdbcIdAndVersionStream.close();
     }
 }
