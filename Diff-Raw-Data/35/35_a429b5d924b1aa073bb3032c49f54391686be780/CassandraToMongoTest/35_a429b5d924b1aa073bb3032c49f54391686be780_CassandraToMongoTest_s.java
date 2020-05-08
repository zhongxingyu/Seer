 package com.dev.cassandratomongo;
 
 import java.util.List;
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.dev.cassandratomongo.reader.FileReaderImpl;
 import com.dev.cassandratomongo.reader.Reader;
 
 public class CassandraToMongoTest {
 
     private Reader fileReader;
     private CassandraClient cassandra;
     private MongoDBClient mongo;
     private Properties props;
 
     @Before
     public void setup() throws Exception {
         props = new Properties();
     }
 
     @Test
     public void testEndToEnd() throws Exception {
         props.setProperty("cassandracluster", "Test Cluster");
         props.setProperty("cassandrahostandport", "localhost:9160");
         props.setProperty("keyspacename", "DevdattaKeyspace");
         props.setProperty("columnfamily", "DevdattaStandard");
         props.setProperty("replicationfactor", "1");
         props.setProperty("host", "localhost");
         props.setProperty("port", "27017");
         props.setProperty("db", "mydb");
        props.setProperty("collection", "DevdattaStandard3");
 
         cassandra = new CassandraClient(props);
         mongo = new MongoDBClient(props);
 
         props.setProperty("filename", "input1.txt");
         fileReader = new FileReaderImpl(props);
         List<String> lines = fileReader.read();
         cassandra.write(lines);
         List<String> data = cassandra.read();
         System.out.println("====== Data got back from Cassandra ======");
         System.out.println(data);
         mongo.write(data);
         List<String> output = mongo.read();
 
         System.out.println("===== Data got back from Mongo ======");
        System.out.println(output);
     }
 }
