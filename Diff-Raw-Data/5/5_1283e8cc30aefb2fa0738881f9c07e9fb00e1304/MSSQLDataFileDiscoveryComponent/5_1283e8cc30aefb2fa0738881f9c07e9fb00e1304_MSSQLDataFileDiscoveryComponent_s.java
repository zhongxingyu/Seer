 package org.rhq.plugins.sqlserver;
 
 import org.rhq.core.domain.configuration.PropertySimple;
 import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
 import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
 import org.rhq.plugins.database.DatabaseQueryUtility;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Discover the datafiles associated to this database
  */
 public class MSSQLDataFileDiscoveryComponent implements ResourceDiscoveryComponent<MSSQLDatabaseComponent<?>>  {
 
     private static String DISCOVER_DATAFILES = "SELECT m.name, m.file_guid FROM sys.master_files AS m WHERE m.database_id = ?";
 
     @Override
     public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MSSQLDatabaseComponent<?>> mssqlDatabaseComponentResourceDiscoveryContext) throws InvalidPluginConfigurationException, Exception {
         Set<DiscoveredResourceDetails> discoveredFiles = new HashSet<DiscoveredResourceDetails>();
 
         List<Map<String, Object>> gridValues = DatabaseQueryUtility.getGridValues(mssqlDatabaseComponentResourceDiscoveryContext.getParentResourceComponent(), DISCOVER_DATAFILES, mssqlDatabaseComponentResourceDiscoveryContext.getParentResourceContext().getResourceKey());
         for(Map<String, Object> dataFileRow : gridValues) {
             String datafileName = (String) dataFileRow.get("name");
             String datafileKey = (String) dataFileRow.get("file_guid");
 
             // Create resource
             DiscoveredResourceDetails service = new DiscoveredResourceDetails(mssqlDatabaseComponentResourceDiscoveryContext.getResourceType(), datafileKey,
                     datafileName, null, null, null, null);
             service.getPluginConfiguration().put(new PropertySimple("logicalName", datafileName));
             discoveredFiles.add(service);
 
         }
 
         return discoveredFiles;
     }
 }
