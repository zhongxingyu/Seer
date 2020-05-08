 package kanbannow.health;
 
 import com.yammer.dropwizard.config.Environment;
 import com.yammer.dropwizard.db.DatabaseConfiguration;
 import com.yammer.dropwizard.jdbi.DBIFactory;
 import com.yammer.metrics.core.HealthCheck;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 
 public class CardServiceHealthCheck extends HealthCheck {
 
 
     private DatabaseConfiguration databaseConfiguration;
     private Environment environment;
     private Handle databaseHandle;
 
 
     public static final String CARD_1_TEXT = "zzzTest card text1zzz";
     public static final String CARD_2_TEXT = "zzzTest card text2zzz";
     public static final String CARD_3_TEXT = "zzzTest card text3zzz";
     public static final String CARD_4_TEXT = "zzzTest card text4zzz";
 
 
 
    public CardServiceHealthCheck(Environment anEnvironment, DatabaseConfiguration aDatabaseConfiguration) {
         super("cardService");
        this.environment = anEnvironment;
         this.databaseConfiguration = aDatabaseConfiguration;
     }
 
     @Override
     protected Result check() throws Exception {
         final DBIFactory factory = new DBIFactory();
         final DBI dbi = factory.build(environment, databaseConfiguration, "oracle");
         databaseHandle = dbi.open();
 
         cleanupDbData(dbi);
 
         return Result.healthy();
     }
 
 
     // CHECKSTYLE:OFF
     private void cleanupDbData(DBI dbi) {
 
         databaseHandle.execute("delete from card where text ='" + CARD_1_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_2_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_3_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_4_TEXT + "'");
     }
     // CHECKSTYLE:ON
 
 
 }
