 package com.cedarsoft.couchdb.test.utils;
 
 import com.cedarsoft.couchdb.ActionFailedException;
 import com.cedarsoft.couchdb.CouchDatabase;
 import com.cedarsoft.couchdb.CouchUtils;
 import com.cedarsoft.couchdb.DocId;
import org.fest.reflect.core.Reflection;
 import org.jcouchdb.db.Database;
 import org.jcouchdb.document.DesignDocument;
 import org.junit.*;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class CouchUtilsTest extends CouchTest {
 
   public static final String DB_NAME = "couchutilstest";
 
   @Test
   public void testBasic() throws Exception {
     CouchDatabase database = couchDbRule.getCouchDatabaseObject( DB_NAME );
 
     CouchUtils utils = new CouchUtils( database );
     assertThat( utils.exists() ).isFalse();
 
     try {
       database.get( new DocId( "dasdf" ) );
       fail( "Where is the Exception" );
     } catch ( ActionFailedException e ) {
       assertThat( e.getStatus() ).isEqualTo( 404 );
       assertThat( e.getReason() ).isEqualTo( "no_db_file" );
       assertThat( e.getMessage() ).isEqualTo( "404 not_found: no_db_file" );
     }
 
     assertThat( utils.exists() ).isFalse();
     try {
       utils.create();
       assertThat( utils.exists() ).isTrue();
     } finally {
       couchDbRule.deleteDb( database.getDbName() );
     }
   }
 
   @Test
   public void testUrl() throws Exception {
     CouchDatabase database = couchDbRule.getCouchDatabaseObject( DB_NAME );
     Database internalDb = new CouchUtils( database ).createInternalDb();
 
     assertThat( internalDb.getName() ).isEqualTo( DB_NAME );
 
    String serverURI = Reflection.field( "serverURI" ).ofType( String.class ).in( internalDb.getServer() ).get();
    assertThat( serverURI ).startsWith( "http://" );
   }
 
   @Test
   public void testViews() throws Exception {
     CouchDatabase db = createDb( DB_NAME );
 
     CouchUtils utils = new CouchUtils( db );
     utils.uploadViews( getClass().getResource( "sampleView.map.js" ) );
 
     DesignDocument designDocument = utils.createInternalDb().getDesignDocument( "utils" );
     assertThat( designDocument.getViews() ).hasSize( 1 );
   }
 }
