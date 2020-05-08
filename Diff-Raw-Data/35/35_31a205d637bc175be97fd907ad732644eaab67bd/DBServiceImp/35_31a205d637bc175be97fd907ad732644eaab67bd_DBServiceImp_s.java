 package Global.Imps;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sidorov Vadim
  * Date: 20.11.13
  * Time: 13:39
  */
 
 
 import Global.Address;
 import Global.DBService;
 import Global.MessageSystem;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.sql.*;
 import java.util.HashMap;
 import java.util.Map;
 
 import static Global.Utilities.dataToKey;
 import static java.lang.Thread.sleep;
 
 public class DBServiceImp implements DBService {
 
     private final Address address;
     private final MessageSystem ms;
 
     //параметры подключения к базе
     private static final String serverName = "localhost";
    private static final String database = "PlaneDB";
     private static final String dbUser = "PlaneDB";
     private static final String dbPassword = "PlaneDB";
    private static final String dbUrl = "jdbc:mysql://" + serverName + "/" + database;
 
     private Connection connect;
     private static final String SQL_DIR = "sql";
     private static final Configuration CFG = new Configuration();
 
     private static class QueryWrapper {
         private ResultSet rs;
         private String queryString;
         private Connection connect;
 
         public QueryWrapper(String queryString, Connection c) throws SQLException {
             super();
             this.queryString = queryString;
             this.connect = c;
         }
 
         public void query() throws SQLException {
             Statement statement = this.connect.createStatement();
             this.rs = statement.executeQuery(this.queryString);
         }
 
         public int update() throws SQLException {
             Statement statement = this.connect.createStatement();
             return statement.executeUpdate(this.queryString);
         }
 
         public int rowCounts() throws SQLException {
             this.rs.last();
             final int rowCounts = this.rs.getRow();
             this.rs.beforeFirst();
             return rowCounts;
         }
 
         public ResultSet getResultSet() {
             return this.rs;
         }
     }
 
     private static int rowCounts(ResultSet rs) throws SQLException {
         rs.last();
         final int rowCounts = rs.getRow();
         rs.beforeFirst();
         return rowCounts;
     }
 
     private static int execUpdate(Connection connect, String update) throws SQLException {
         Statement statement = connect.createStatement();
         statement.execute(update);
         int updated = statement.getUpdateCount();
         statement.close();
         return updated;
     }
 
     public interface TResultHandler<T> {
         T handler(ResultSet result) throws SQLException;
     }
 
     private static <T>  T execQuery(Connection connect, String query, TResultHandler<T> handler) throws SQLException {
         Statement statement = connect.createStatement();
         statement.execute(query);
         ResultSet resultSet = statement.getResultSet();
         T result = handler.handler(resultSet);
         resultSet.close();
         statement.close();
         return result;
     }
 
    public DBServiceImp(MessageSystem ms) throws SQLException {
         super();
         this.ms = ms;
         this.address = new Address();
         //регистрируем DBServiceImp в MsgSystem
         ms.addService(this);
         //регестрируем DBServiceImp в AddressService, чтобы каждый мог обратиться к DBService
         ms.getAddressService().setAccountService(this.address);
 
        this.connect = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
     }
 
     @Override
     public Long getUserIdByUserName(String login, String password) {
         //заполнение sql скрипта
         Map<String, Object> pageVariables = dataToKey(new String [] { "login", "password" },
                                                         login,   password);
         //формирование sql скрипта
         String queryString = generateSQL("userid_by_name.sql", pageVariables);
 
         try {
             return execQuery(this.connect, queryString, new TResultHandler<Long>() {
                 @Override
                 public Long handler(ResultSet result) throws SQLException {
                     if (!result.isLast())  {
                         result.next();
                         return result.getLong("idUser");
                     }
                     else
                         return USER_NOT_EXIST;
                 }
             });
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return USER_NOT_EXIST;
     }
 
     @Override
     public boolean createUser(String login, String password) {
         //заполнение sql скрипта
         Map<String, Object> pageVariables = dataToKey(new String [] { "login", "password" },
                 login,   password);
 
         try {
             if (!checkIsUserExist(login))
             {
                 if (execUpdate(this.connect, generateSQL("create_user.sql", pageVariables)) == 1)
                     return true;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     @Override
     public boolean checkIsUserExist(String login) {
         //заполнение sql скрипта
         Map<String, Object> pageVariables = dataToKey(new String [] { "login" }, login);
         //формирование sql скрипта для проверки существования пользоваталя
         String queryString = generateSQL("check_user.sql", pageVariables);
 
         try {
             //проверяем, что пользователей нет
             return execQuery(this.connect, queryString, new TResultHandler<Boolean>() {
                 @Override
                 public Boolean handler(ResultSet result) throws SQLException {
                     if (rowCounts(result) == 1)
                         return true;
                     else
                         return false;
                 }
             });
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     @Override
     public boolean deleteUser(Long userId) {
         //заполнение sql скрипта
         Map<String, Object> pageVariables = dataToKey(new String [] { "userId" }, userId);
         //формирование sql скрипта для проверки существования пользоваталя
         String queryString = generateSQL("delete_user.sql", pageVariables);
 
         try {
             int result = execUpdate(this.connect, queryString);
             if (result == 1)
                 return true;
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     private static String generateSQL(String templateName, Map<String, Object> context) {
         Writer stream = new StringWriter();
         try {
             Template template = CFG.getTemplate(SQL_DIR + File.separator + templateName);
             template.process(context, stream);
         } catch (IOException | TemplateException e) {
             e.printStackTrace();
             return null;    //FIXME: Щито?
         }
         return stream.toString();
     }
 
     @Override
     public void run() {
         while(true){
             this.ms.execForAbonent(this);
 
             try {
                 sleep(10);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public Address getAddress() {
         return this.address;
     }
 
     @Override
     public MessageSystem getMessageSystem() {
         return this.ms;
     }
 }
