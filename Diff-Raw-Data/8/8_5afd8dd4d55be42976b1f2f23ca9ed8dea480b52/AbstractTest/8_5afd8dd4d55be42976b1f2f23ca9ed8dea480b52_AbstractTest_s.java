 package fr.xebia.xke.cassandra;
 
 import static com.datastax.driver.core.Cluster.builder;
 import static java.lang.String.format;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.List;
 import java.util.Scanner;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import com.datastax.driver.core.Cluster;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.Session;
 import com.google.common.io.Resources;
 
 public class AbstractTest {
 
     public static final String WORKSHOP_KEYSPACE = "workshop";
 
     protected static Session session;
 
     @BeforeClass
     public static void globalSetUp() throws Exception {
        Cluster cluster = builder()
                 .addContactPoints(InetAddress.getLoopbackAddress())
                 .build();
 
         try (Session session = cluster.connect()) {
             runScript(session, Resources.getResource("scripts/create-keyspace.cql"));
         }
 
         session = cluster.connect(WORKSHOP_KEYSPACE);
 
         runScript(session, Resources.getResource("scripts/create-tables.cql"));
     }
 
     @AfterClass
     public static void globalCleanUp() throws Exception {
         if (session != null) {
             session.close();
         }
     }
 
     @After
     public void cleanUp() {
         List<Row> rows = session.execute("SELECT columnfamily_name FROM system.schema_columnfamilies WHERE keyspace_name=?", WORKSHOP_KEYSPACE).all();
         for (Row row : rows) {
             session.execute(format("TRUNCATE %s", row.getString("columnfamily_name")));
         }
     }
 
     private static void runScript(Session session, URL url) throws Exception {
         try (Scanner scanner = new Scanner(new File(url.toURI()))) {
             while (scanner.hasNextLine()) {
                 session.execute(scanner.nextLine());
             }
         }
     }
 }
