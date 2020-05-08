 package com.dev.cassandratomongo;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CassandraClientTest {
 
     private CassandraClient cassandraClient;
     private Properties props;
 
     @Before
     public void setup() throws Exception {
     }
 
     @Test
     public void testWriteToAndReadFromCassandra() throws Exception {
         props = new Properties();
         props.setProperty("cassandracluster", "Test Cluster");
         props.setProperty("cassandrahostandport", "localhost:9160");
         props.setProperty("keyspacename", "DevdattaKeyspace");
         props.setProperty("columnfamily", "DevdattaStandard");
         props.setProperty("replicationfactor", "1");
         cassandraClient = new CassandraClient(props);
 
         List<String> input = new ArrayList<String>();
        input.add("key:India, Country:India, Currency:Rupee");
        input.add("key:USA, Country:USA, Currency:Dollar");
         cassandraClient.write(input);
         List<String> output = cassandraClient.read();
         System.out.println("--- Data Read Back ---");
        for (int i = 0; i < output.size(); i++) {
            System.out.println(output.get(i));
        }
     }
 }
