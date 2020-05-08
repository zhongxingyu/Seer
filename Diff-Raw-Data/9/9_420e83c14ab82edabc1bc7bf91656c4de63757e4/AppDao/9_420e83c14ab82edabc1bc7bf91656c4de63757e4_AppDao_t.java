 package models;
 /*
  * class for AppDao with some methods
  */
 
 import java.util.*;
 import me.prettyprint.hector.api.beans.*; 
 import static me.prettyprint.hector.api.factory.HFactory.*;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.*;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.mutation.MutationResult;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.*;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AppDao {
 	final static String KEYSPACE = "InternetRuntime";
 	final static String CF = "App";
 	final static StringSerializer se = new StringSerializer();
 	
	Cluster cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9160");
 	Keyspace keyspace = createKeyspace(KEYSPACE, cluster);	
 	Mutator<String> m = createMutator(keyspace, se);
 	
 	public void save(App instance){
 		System.out.println(instance.getId());
 		m.insert(instance.getId(), CF, createStringColumn("name", instance.getName()));
 		m.insert(instance.getId(), CF, createStringColumn("information", instance.getInformation()));
 		m.insert(instance.getId(), CF, createStringColumn("installUrl", instance.getInstallUrl()));		
 		m.insert(instance.getId(), CF, createStringColumn("updated", instance.getUpdated()));
 		System.out.println( "^&*^*%^(&%*&((((((((((((((("+instance.getUpdateUrl());
 		m.insert(instance.getId(), CF, createStringColumn("updateUrl", instance.getUpdateUrl()));
 		System.out.println( instance.getSecret());
 		m.insert(instance.getId(), CF, createStringColumn("secret", instance.getSecret()));		
 	}
 	
 
 	public void delete(App instance){	
 		
 	//	m = createMutator(keyspace, se);
 	//	m.addDeletion(instance.getId(), CF, null, se);
 		instance.getId().equals("");
 		m.delete(instance.getId(), CF, null, se);
 	//	m.execute();
 	}
 	
 	public List<App> getAllApps()
 	{
 		RangeSlicesQuery<String, String, String> rangeSlicesQuery = createRangeSlicesQuery(keyspace, se, se, se);
 		rangeSlicesQuery.setColumnFamily(CF);
 		rangeSlicesQuery.setRange("", "", false, 100);
 		rangeSlicesQuery.setRowCount(100);
 		QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
 		OrderedRows<String, String, String> orderedRows = result.get();
 		List<Row<String, String, String>> rows = orderedRows.getList();
 		
 		List<App> appList = new ArrayList<App>();
 		ColumnQuery<String, String, String> columnQuery = createStringColumnQuery(keyspace);	
 		for(Row<String, String, String> row: rows)
 		{
 			ColumnQuery<String, String, String> rowQuery = columnQuery.setColumnFamily(CF).setKey(row.getKey());
 			if (rowQuery.setName("name").execute().get() != null){
 				App temp = new App(row.getKey(),
 					rowQuery.setName("name").execute().get().getValue(),
 					rowQuery.setName("information").execute().get().getValue(),
 					rowQuery.setName("installUrl").execute().get().getValue(),
 					rowQuery.setName("updated").execute().get().getValue(),
 					rowQuery.setName("updateUrl").execute().get().getValue(),
 					rowQuery.setName("secret").execute().get().getValue()
 				);
 				appList.add(temp);
 			}
 		}
 		return appList;		
 	}	
 	public App findById(String id)
 	{
 		System.out.println("Find APP by ID:"+id);
 		ColumnQuery<String, String, String> columnQuery = createStringColumnQuery(keyspace);
 		ColumnQuery<String, String, String> rowQuery = columnQuery.setColumnFamily(CF).setKey(id);
 		
 		if (rowQuery.setName("name").execute().get()!=null){
 			System.out.println(rowQuery.setName("information").execute().get().getValue());
 		App app = new App(id,
 			rowQuery.setName("name").execute().get().getValue(),
 			rowQuery.setName("information").execute().get().getValue(),
 			rowQuery.setName("installUrl").execute().get().getValue(),
 			rowQuery.setName("updated").execute().get().getValue(),
 			rowQuery.setName("updateUrl").execute().get().getValue(),
 			rowQuery.setName("secret").execute().get().getValue()
 		);
 		return app;
 		} else
 			return null;
 	}
 }
 
