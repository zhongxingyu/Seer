 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/15/12
  * Time: 3:26 PM
  */
 public class CommandExecutor implements Runnable {
 
     @Override
     public void run() {
         DataSource dataSource = ConnectionPool.getDataSource();
         Connection connection = null;
         PreparedStatement selectStatement = null;
         PreparedStatement updateStatement = null;
         Command command = null;
         try {
             try {
                 connection = dataSource.getConnection();
                 connection.setAutoCommit(false);
                selectStatement = connection.prepareStatement("select * from commands where status='NEW' limit 3 for update");
                 updateStatement = connection.prepareStatement("update commands set status=? WHERE id=?");

                 ResultSet resultSet = null;
                 while(true) {
                     resultSet =  selectStatement.executeQuery();
                     if (!resultSet.first()) {
                         break;
                     }
                     while (resultSet.next()) {
                         command = new Command(resultSet.getInt("id"),
                                resultSet.getString("name"),
                                Command.Status.valueOf(resultSet.getString("status")));

                         updateStatement.setString(1, Command.Status.IN_PROGRESS.toString());
                         updateStatement.setInt(2, command.getId());
                         updateStatement.execute();

                        command.execute();
 
                         updateStatement.setString(1, Command.Status.DONE.toString());
                         updateStatement.setInt(2, command.getId());
                         updateStatement.execute();
                     }
                    connection.commit();
                 }
             } finally {
                 if (connection != null) {
                     connection.close();
                 }
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         }
     }
 }
