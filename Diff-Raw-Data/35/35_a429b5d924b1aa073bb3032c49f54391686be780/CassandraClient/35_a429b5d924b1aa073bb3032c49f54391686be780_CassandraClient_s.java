 package com.dev.cassandratomongo;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import com.dev.cassandratomongo.reader.Reader;
 import com.dev.cassandratomongo.writer.Writer;
 
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.service.StringKeyIterator;
 import me.prettyprint.cassandra.service.ThriftKsDef;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.ColumnQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 
 public class CassandraClient implements Writer, Reader {
 
     private static final String COLUMNFAMILY = "columnfamily";
     private static final String REPLICATIONFACTOR = "replicationfactor";
     private static final String KEYSPACENAME = "keyspacename";
     private static final String CASSANDRAHOSTANDPORT = "cassandrahostandport";
     private static final String CASSANDRACLUSTER = "cassandracluster";
     private Keyspace ksp;
     private Properties props;
     private Cluster myCluster;
 
     private String keyspaceName = "DevdattaKeyspace";
     private String columnFamily = "DevdattaStandard";
     private String cassandraClusterName = "Test Cluster";
     private String cassandraHostAndPort = "localhost:9160";
     private String replicationFactor;
 
     private List<String> columnNamesList;
 
     public CassandraClient(Properties properties) {
         props = properties;
         columnNamesList = new ArrayList<String>();
         cassandraClusterName = props.getProperty(CASSANDRACLUSTER);
         cassandraHostAndPort = props.getProperty(CASSANDRAHOSTANDPORT);
         keyspaceName = props.getProperty(KEYSPACENAME);
         columnFamily = props.getProperty(COLUMNFAMILY);
         replicationFactor = props.getProperty(REPLICATIONFACTOR);
 
         myCluster = HFactory.getOrCreateCluster(cassandraClusterName,
             cassandraHostAndPort);
 
         ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(
             keyspaceName, columnFamily, ComparatorType.BYTESTYPE);
 
         KeyspaceDefinition newKeyspaceDef = HFactory.createKeyspaceDefinition(
             keyspaceName, ThriftKsDef.DEF_STRATEGY_CLASS, Integer
                 .parseInt(replicationFactor), Arrays.asList(cfDef));
 
         myCluster.dropKeyspace(keyspaceName);
         ksp = HFactory.createKeyspace(keyspaceName, myCluster);
         myCluster.addKeyspace(newKeyspaceDef);
     }
 
     public void write(List<String> lines) throws Exception {
 
         Mutator<String> mutator = HFactory.createMutator(ksp, StringSerializer
             .get());
 
         for (int i = 0; i < lines.size(); i++) {
             String line = lines.get(i);
             StringTokenizer tokenizer = new StringTokenizer(line, ",");
             boolean firstPair = true;
             String key = null;
             String columnName = null;
             String columnValue = null;
             while (tokenizer.hasMoreTokens()) {
                 String columnValuePair = tokenizer.nextToken();
                 String[] columnAndValue = columnValuePair.split(":");
                 if (firstPair) {
                     firstPair = false;
                     key = columnAndValue[1];
                     columnName = key;
                     columnNamesList.add(columnName);
                 } else {
                     columnName = columnAndValue[0];
                     if (!columnNamesList.contains(columnName)) {
                         columnNamesList.add(columnName);
                     }
                     columnValue = columnAndValue[1];
                     // System.out.println("Key:" + key + " Column Name:"
                     // + columnName + " Column Value:" + columnValue);
 
                     mutator.insert(key, columnFamily, HFactory
                         .createStringColumn(columnName, columnValue));
                 }
             }
         }
 
     }
 
     public List<String> read() throws Exception {
         List<String> data = new ArrayList<String>();
         StringKeyIterator keyIterator = new StringKeyIterator(ksp, columnFamily);
 
         Iterator<String> keys = keyIterator.iterator();
 
         // System.out.println("Column Names:" + columnList.toString());
         while (keys.hasNext()) {
             String key = keys.next();
             // System.out.println("Key:" + key);
 
             ColumnQuery<String, String, String> columnQuery = HFactory
                 .createStringColumnQuery(ksp);
 
             for (int i = 0; i < columnNamesList.size(); i++) {
                 columnQuery.setColumnFamily(columnFamily).setKey(key).setName(
                     columnNamesList.get(i));
 
                 QueryResult<HColumn<String, String>> result = columnQuery
                     .execute();
 
                 HColumn<String, String> cols = result.get();
                 if (cols != null) {
                     System.out.print(cols.getName() + ":" + cols.getValue());
                    data.add(cols.getName() + ":" + cols.getValue());
                 }
             }
            System.out.println();
         }
         return data;
     }
 }
