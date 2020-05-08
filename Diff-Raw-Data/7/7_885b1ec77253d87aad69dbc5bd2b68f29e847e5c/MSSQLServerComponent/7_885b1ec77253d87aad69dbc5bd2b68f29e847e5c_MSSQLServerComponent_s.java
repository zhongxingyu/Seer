 /*
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation version 2 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package org.rhq.plugins.sqlserver;
 
 import java.sql.*;
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.rhq.core.domain.configuration.Configuration;
 import org.rhq.core.domain.measurement.*;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceComponent;
 import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.pluginapi.measurement.MeasurementFacet;
 import org.rhq.core.util.jdbc.JDBCUtil;
 import org.rhq.plugins.database.DatabaseComponent;
 import org.rhq.plugins.database.DatabaseQueryUtility;
 
 public class MSSQLServerComponent<T extends ResourceComponent<?>> implements DatabaseComponent<T>, MeasurementFacet {
     private static final Log LOG = LogFactory.getLog(MSSQLServerComponent.class);
 
     private Connection connection;
 
     private ResourceContext resourceContext;
 
     private static final String PROPERTY_QUERY = "SELECT CONVERT(varchar(100), SERVERPROPERTY('productversion')) AS productversion, CONVERT(varchar(100), SERVERPROPERTY('productlevel')) AS productlevel, CONVERT(varchar(100), SERVERPROPERTY('edition')) AS edition";
    private static final String CONFIG_QUERY = "SELECT configuration_id, CONVERT(varchar(100), value) AS value FROM sys.configurations";
 
 
     private boolean started;
 
     public void start(ResourceContext resourceContext) throws InvalidPluginConfigurationException, SQLException {
         this.resourceContext = resourceContext;
         this.connection = buildConnection(resourceContext.getPluginConfiguration());
         this.started = true;
     }
 
     public void stop() {
         removeConnection();
         this.started = false;
     }
 
     public AvailabilityType getAvailability() {
         if (started && getConnection() != null) {
             return AvailabilityType.UP;
         } else {
             return AvailabilityType.DOWN;
         }
     }
 
     private Map<String, String> fillTraitData() throws SQLException {
         Map<String, String> traitsMap = new HashMap<String, String>();
 
         // This is form of configuration_id, value
         List<Map<String,Object>> gridValues = DatabaseQueryUtility.getGridValues(this, CONFIG_QUERY);
 
         // Now transform to String, String and add config_id to it
         for(Map<String, Object> data : gridValues) {
            for(Map.Entry<String, Object> entry : data.entrySet()) {
                traitsMap.put("config_id_" + entry.getKey(), (String) entry.getValue());
            }
         }
 
         // Rest, just transform to String, String
         gridValues = DatabaseQueryUtility.getGridValues(this, PROPERTY_QUERY);
 
         for(Map<String, Object> data : gridValues) {
             for(Map.Entry<String, Object> entry : data.entrySet()) {
                 traitsMap.put(entry.getKey(), (String) entry.getValue());
             }
         }
 
         return traitsMap;
     }
 
     /*
      * (non-Javadoc)
      * @see org.rhq.core.pluginapi.measurement.MeasurementFacet#getValues(org.rhq.core.domain.measurement.MeasurementReport, java.util.Set)
      */
     public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) {
         Map<String, String> queryResults = null;
         try {
             queryResults = fillTraitData();
             for (MeasurementScheduleRequest request : metrics) {
 
                 String property = request.getName();
                 report.addData(new MeasurementDataTrait(request, queryResults.get(property)));
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public Connection getConnection() {
         try {
             if (this.connection == null || connection.isClosed()) {
                 this.connection = buildConnection(this.resourceContext.getPluginConfiguration());
             }
             this.connection.setCatalog("master"); // Default for server component requests
         } catch (SQLException e) {
             LOG.info("Unable to create SQL Server connection", e);
         }
         return this.connection;
     }
 
     public void removeConnection() {
         JDBCUtil.safeClose(connection);
         this.connection = null;
     }
 
     public static Connection buildConnection(Configuration configuration) throws SQLException {
         String driverClass = configuration.getSimple("driverClass").getStringValue();
         try {
             Class.forName(driverClass);
         } catch (ClassNotFoundException e) {
             throw new InvalidPluginConfigurationException("Specified JDBC driver class (" + driverClass
                 + ") not found.");
         }
 
         String url = buildUrl(configuration);
         LOG.debug("Attempting JDBC connection to [" + url + "]");
 
         String principal = configuration.getSimple("principal").getStringValue();
         String credentials = configuration.getSimple("credentials").getStringValue();
         String instance = configuration.getSimple("instanceName").getStringValue();
 
         Properties props = new Properties();
         props.put("user", principal);
         props.put("password", credentials);
         if(instance != null && !instance.equals("MSSQLSERVER")) {
         	props.put("instanceName", instance);
         }
 
         return DriverManager.getConnection(url, props);
     }
 
     private static String buildUrl(Configuration configuration) {
     	return "jdbc:sqlserver://" + configuration.getSimpleValue("host", "localhost") + ":"
     			+ configuration.getSimpleValue("port", "1433");
     }
 }
