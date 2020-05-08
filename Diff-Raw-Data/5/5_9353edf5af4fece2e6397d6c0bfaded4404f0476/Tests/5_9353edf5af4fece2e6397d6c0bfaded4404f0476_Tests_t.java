 package nl.finalist.datomic.intro;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import datomic.Connection;
 import datomic.Entity;
 import datomic.Peer;
 
 public class Tests
 {
     private static final Logger LOGGER = LoggerFactory.getLogger( Tests.class );
     
     @Test
     public void parseDatomicFileAndRunTransaction()
     {
         final String uri = "datomic:mem://players";
         
         LOGGER.info( "Creating and connecting to database at {}", uri );
         Connection conn = Main.createAndConnect( uri );
         
         // Adding schema and data (1)
         
         LOGGER.info( "Adding schema and data with attrs: name, country, person/born, person/height, player/position" );
         Main.parseDatomicFileAndRunTransaction( "data/schema-1.dtm", conn );
         Main.parseDatomicFileAndRunTransaction( "data/data-1.dtm", conn );
         
         // Exercise 1
 
         LOGGER.info( "Find all entities" );
         // Task: define the query
        String query = "[:find ?p :in $ :where [?p :name]]";
         Collection<List<Object>> results = Peer.q( query, conn.db() );
         assertEquals( 153, results.size() );
         List<Entity> entities = Helper.entities( results, conn.db() );
         Helper.printEntities( entities );
         
         // Exercise 2
         
         LOGGER.info( "Find all persons" );
         // Task: define the query
        query = "[:find ?p :in $ :where [?p :person/height _]]";
         results = Peer.q( query, conn.db() );
         assertEquals( 85, results.size() );
         Helper.printEntities( Helper.entities( results, conn.db() ) );
 
         // Adding schema and data (2)
         
         LOGGER.info( "Adding attributes to schema: player/team, player/salary + data" );
         Main.parseDatomicFileAndRunTransaction( "data/schema-2.dtm", conn );
         Main.loadPlayerTeamAndSalary( "data/data-2-2011.csv", conn );
 
         // Exercise 3
         
         LOGGER.info( "Find team and salary for Zlatan Ibrahimovic" );
         // Task: define the query
         query = "";
         results = Peer.q( query, conn.db(), "Zlatan Ibrahimovic" );
         List<Object> tuple = results.iterator().next();        
         assertEquals( 2, tuple.size() );
         assertEquals( "AC Milan", tuple.get( 0 ) );
         assertEquals( 9.0, tuple.get( 1 ) );
         LOGGER.info( "Added data for Zlatan Ibrahimovic, team: {}, salary: {}", tuple.toArray() );
         
         // Find instant for Zlatans salary addition
 
         LOGGER.info( "Find instant when Zlatans salary was recorded" );
         query = "[:find ?instant :in $ ?n :where [?p :name ?n] [?tx :db/txInstant ?instant]]";
         results = Peer.q( query, conn.db(), "Zlatan Ibrahimovic" );
         Date year2011 = (Date)results.iterator().next().get( 0 );
         LOGGER.info( "Salary data for Zlatan added on {}", year2011 );
         
         // Add data for 2012
         
         LOGGER.info( "Loading player team and salary data for 2012" );
         Main.loadPlayerTeamAndSalary( "data/data-2-2012.csv", conn );
 
         // Exercise 4
         
         LOGGER.info( "List name, team and salary, ordered by salary (desc) for 2011" );
         query = "";
         // Task: change the argument
         results = Peer.q( query, conn.db() );
         List<List<Object>> values = Helper.sort( Helper.list( results ), 1, "DESC" ); 
         Helper.printValues( values );
         assertEquals( "Cristiano Ronaldo", values.get( 0 ).get( 0 ) );
         
         // Add schema and data (3)
 
         LOGGER.info( "Adding Twitter user attributes to schema + data" );
         Main.parseDatomicFileAndRunTransaction( "data/schema-3.dtm", conn );
         Main.parseDatomicFileAndRunTransaction( "data/data-3.dtm", conn );
 
         // Exercise 5
 
         LOGGER.info( "Find Twitter screenName and followersCount where followersCount > a million" );
         // Task: define the query
         query = "";
         results = Peer.q( query, conn.db() );
         assertEquals( 21, results.size() );
         values = Helper.sort( Helper.list( results ), 1, "DESC" ); 
         Helper.printValues( values );
 
         // Add schema and data (4)
         
         LOGGER.info( "Adding attributes to schema: player/twitter.screenName + data" );
         Main.parseDatomicFileAndRunTransaction( "data/schema-4.dtm", conn );
         Main.loadPlayerTwitterScreenName( "data/data-4.csv", conn );
 
         // Exercise 6
         
         LOGGER.info( "Find names of players who are following Robin van Persie on Twitter" );
         // Task: define the query
         query = "";
         results = Peer.q( query, conn.db(), "Robin van Persie" );
         assertEquals( 23, results.size() );
         Helper.printValues( Helper.list( results ) );
     }    
 }
