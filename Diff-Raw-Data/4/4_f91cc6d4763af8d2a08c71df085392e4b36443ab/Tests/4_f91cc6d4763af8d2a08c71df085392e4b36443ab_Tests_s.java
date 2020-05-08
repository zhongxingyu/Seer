 package tests;
 
 import h2.DataBlock;
 import h2.QueryDataSource;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.List;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import com.google.common.collect.ImmutableList;
 import dml.CommandBuilder;
 import dml.Metadata;
 import dml.TableColumn;
 
 public class Tests {
 
     private static Connection _connection;
 
     public Connection getConnection() {
         return _connection;
     }
 
     @BeforeClass
     public static void initConnection() throws ClassNotFoundException, SQLException {
         if (_connection == null)
             Class.forName("oracle.jdbc.OracleDriver");
         System.setProperty("oracle.net.tns_admin",
                            "c:\\Oracle\\Dev10g\\NETWORK\\ADMIN");
         _connection = DriverManager.getConnection("jdbc:oracle:thin:@ece", "ec_calc", "calc");
     }
 
     @AfterClass
     public static void closeConnection() throws SQLException {
         _connection.close();
     }
 
     @Ignore
     @Test
     public void BuilderTest() {
 
         // Insert without returning
         Metadata metadata = getDMLMetadata(false);
         out(CommandBuilder.insertCommand(metadata));
 
         // Insert with returning
         metadata = getDMLMetadata(true);
         out(CommandBuilder.insertCommand(metadata));
 
         // Update statement
         out(CommandBuilder.updateCommand(metadata));
 
         out(CommandBuilder.deleteCommand(metadata));
         // out(DMLBuilder.lockCommand("berechnungen", ImmutableList.of("name",
         // "ber_id", "bezeichnung")));
 
     }
 
     @Ignore
     @Test
     public void test() throws SQLException {
         QueryDataSource queryDataSource = new QueryDataSource("TTEST");
         Metadata metadata = new Metadata(_connection, queryDataSource);
         List<String> pkCols = metadata.getPrimaryKeyColumns();
 
     }
 
 
     @Test
     public void dataBlockTest() throws SQLException {
 
         DataBlock dataBlock = DataBlock.createDataBlock(_connection, "TTEST");
         dataBlock.createRecord();
         dataBlock.setItems(null, "Test", 1, "kuna", 25);
         dataBlock.post();
         dataBlock.setItem(1, "Test-Update");
         dataBlock.post();
 
     }
 
     private void nextTest() {
 
 
     }
 
     private Metadata getDMLMetadata(boolean returningCols) {
         if (returningCols)
             return new Metadata("ttest",
                                 ImmutableList.of("id", "name", "tag"),
                                 ImmutableList.of(new TableColumn("id",
                                                                  Types.NUMERIC)),
                                 ImmutableList.of("tag"));
         return new Metadata("ttest", ImmutableList.of("id", "name", "tag"));
     }
 
 
 
     private void out(String text) {
         System.out.println(text);
 
     }
 
 }
