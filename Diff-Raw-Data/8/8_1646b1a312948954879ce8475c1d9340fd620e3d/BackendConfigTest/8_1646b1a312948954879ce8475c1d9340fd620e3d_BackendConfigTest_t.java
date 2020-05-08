 package cz.cuni.mff.odcleanstore.configuration;
 
 import static org.junit.Assert.assertEquals;
 
 import cz.cuni.mff.odcleanstore.configuration.exceptions.ConfigurationException;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Properties;
 
 /**
  *
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 public class BackendConfigTest {
     private static final String GROUP_NAME = "backend";
 
     @Test
     public void testCorrectConfiguration() throws ConfigurationException, URISyntaxException, MalformedURLException {
         Properties properties = Mockito.mock(Properties.class);
 
         mockDirtyJDBCConnectionCredentials(properties);
         mockCleanJDBCConnectionCredentials(properties);
         mockSparqlEndpointsConnectionCredentials(properties);
         mockQueryTimeout(properties);
         mockGraphUriPrefixes(properties);
 
         BackendConfig backendConfig = BackendConfig.load(properties);
 
         checkDirtyJDBCConnectionCredentials(backendConfig);
         checkCleanJDBCConnectionCredentials(backendConfig);
         checkSparqlEndpointsConnectionCredentials(backendConfig);
         checkQueryTimeout(backendConfig);
         checkGraphUriPrefixes(backendConfig);
     }
 
     private void mockQueryTimeout(Properties properties) {
         Mockito.when(properties.getProperty(GROUP_NAME + ".query_timeout")).thenReturn("30");
     }
 
     private void checkQueryTimeout(BackendConfig config) {
         assertEquals(new Integer(30), config.getQueryTimeout());
     }
 
     private void mockGraphUriPrefixes(Properties properties) {
         Mockito.when(properties.getProperty(GROUP_NAME + ".data_graph_uri_prefix")).thenReturn(
                 "http://opendata.cz/infrastructure/odcleanstore/");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".metadata_graph_uri_prefix")).thenReturn(
                 "http://opendata.cz/infrastructure/odcleanstore/metadata/");
     }
 
     private void checkGraphUriPrefixes(BackendConfig config) throws URISyntaxException {
         assertEquals(new URI("http://opendata.cz/infrastructure/odcleanstore/"), config.getDataGraphURIPrefix());
 
         assertEquals(new URI("http://opendata.cz/infrastructure/odcleanstore/metadata/"),
                 config.getMetadataGraphURIPrefix());
     }
 
     private void mockSparqlEndpointsConnectionCredentials(Properties properties) {
         Mockito.when(properties.getProperty(GROUP_NAME + ".dirty_sparql_endpoint_url")).thenReturn(
                 "http://www.sparql.cz/dirty");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".clean_sparql_endpoint_url")).thenReturn(
                 "http://www.sparql.cz/clean");
     }
 
     private void checkSparqlEndpointsConnectionCredentials(BackendConfig config) throws MalformedURLException {
         assertEquals(new URL("http://www.sparql.cz/dirty"), config.getDirtyDBSparqlConnectionCredentials().getUrl());
 
         assertEquals(new URL("http://www.sparql.cz/clean"), config.getCleanDBSparqlConnectionCredentials().getUrl());
     }
 
     private void mockDirtyJDBCConnectionCredentials(Properties properties) {
        Mockito.when(properties.getProperty(GROUP_NAME + ".dirty_jdbc_connection_string")).thenReturn("jdbc:virtuoso://localhost:1112");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".dirty_jdbc_username")).thenReturn("dba");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".dirty_jdbc_password")).thenReturn("dba");
     }
 
     private void checkDirtyJDBCConnectionCredentials(BackendConfig config) throws MalformedURLException {
         JDBCConnectionCredentials dirtyJDBCConnectionCredentials = config.getDirtyDBJDBCConnectionCredentials();
 
        assertEquals(new URL("jdbc:virtuoso://localhost:1112"), dirtyJDBCConnectionCredentials.getConnectionString());
 
         assertEquals("dba", dirtyJDBCConnectionCredentials.getUsername());
 
         assertEquals("dba", dirtyJDBCConnectionCredentials.getPassword());
     }
 
     private void mockCleanJDBCConnectionCredentials(Properties properties) {
         Mockito.when(properties.getProperty(GROUP_NAME + ".clean_jdbc_connection_string")).thenReturn("jdbc:virtuoso://localhost:1111");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".clean_jdbc_username")).thenReturn("dba");
 
         Mockito.when(properties.getProperty(GROUP_NAME + ".clean_jdbc_password")).thenReturn("dba");
     }
 
     private void checkCleanJDBCConnectionCredentials(BackendConfig config) throws MalformedURLException {
         JDBCConnectionCredentials cleanJDBCConnectionCredentials = config.getCleanDBJDBCConnectionCredentials();
 
        assertEquals(new URL("jdbc:virtuoso://localhost:1111"), cleanJDBCConnectionCredentials.getConnectionString());
 
         assertEquals("dba", cleanJDBCConnectionCredentials.getUsername());
 
         assertEquals("dba", cleanJDBCConnectionCredentials.getPassword());
     }
 }
