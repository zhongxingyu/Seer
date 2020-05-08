 package no.steria.swhrs;
 
 import org.joda.time.DateTime;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class MSSQLHourRegDao implements HourRegDao {
 
     private static final String COUNTRY = "NO";
     private final DataSource datasource;
     private static final String SELECT_CURRENT_PERIOD = "select TP.[Startdato],TP.[Slutdato], TP.[Beskrivelse] FROM [Norge$Time Periods] TP WHERE Ressource=? AND TP.[Startdato] <= ? AND TP.[Slutdato] >= ? ORDER BY [Startdato] DESC";
     private static final String SELECT_USERS = "SELECT No_, \"WEB Password\" FROM \"Norge$Resource\" where No_ = ?";
     private static final String SELECT_REGISTRATIONS = "SELECT * FROM \"Norge$Time Entry\" WHERE Ressourcekode = ? AND Dato = ? AND Projektnr_ NOT LIKE 'FLEX'";
     private static final String SELECT_NORMTIME = "SELECT \"Norge$Norm Time Data\".Kode, \"Norge$Norm Time Data\".Mandag, \"Norge$Norm Time Data\".Tirsdag, \"Norge$Norm Time Data\".Onsdag, \"Norge$Norm Time Data\".Torsdag, \"Norge$Norm Time Data\".Fredag, \"Norge$Norm Time Data\".Lørdag, \"Norge$Norm Time Data\".Søndag from \"Norge$Norm Time Data\" INNER JOIN \"Norge$Resource\" ON \"Norge$Norm Time Data\".Kode = \"Norge$Resource\".\"Norm Tid\" WHERE \"Norge$Resource\".No_ = ?";
     private static final String SELECT_SEARCHPROJECTS = "SELECT TOP 150 \"Norge$Tasklist\".Projektnr_, \"Norge$Tasklist\".Kode, \"Norge$Tasklist\".Beskrivelse FROM \"Norge$Tasklist\" WHERE Beskrivelse like ? OR Projektnr_ like ? ORDER BY Beskrivelse";
     private static final String INSERT_FAVOURITE = "INSERT into \"Norge$Favourite Task\" (Resourcekode, Projektnr_, Aktivitetskode) VALUES(?, ?, ?)";
     private static final String DELETE_FAVOURITE = "DELETE FROM \"Norge$Favourite Task\" WHERE Resourcekode = ? AND Projektnr_ = ? AND Aktivitetskode = ?";
     private static final String SELECT_FAVOURITES = "SELECT \"Norge$Favourite Task\".Projektnr_, \"Norge$Favourite Task\".Aktivitetskode, \"Norge$Tasklist\".Beskrivelse, \"Norge$Tasklist\".\"Ressource fakturerbar\", \"Norge$Job\".Description, \"Norge$Job\".Name,  \"Norge$Job\".\"Internt projekt\"  FROM \"Norge$Favourite Task\"  INNER JOIN \"Norge$Tasklist\" ON \"Norge$Favourite Task\".Projektnr_= \"Norge$Tasklist\".Projektnr_ AND \"Norge$Favourite Task\".Aktivitetskode = \"Norge$Tasklist\".Kode INNER JOIN \"Norge$Job\" ON \"Norge$Favourite Task\".Projektnr_= \"Norge$Job\".No_ WHERE \"Norge$Favourite Task\".Resourcekode = ? AND \"Norge$Tasklist\".Spærret NOT LIKE '1'";
     private static final String STORE_PROCEDURE_INSERT_HOURS = "{? = call dbo.uspSTE_InsertTimeEntry(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
     private static final String STORE_PROCEDURE_DELETE_HOURS = "{call dbo.uspSTE_DeleteTimeEntry(?, ?, ?)}";
     private static final String STORE_PROCEDURE_UPDATE_HOURS = "{call dbo.uspSTE_UpdateTimeEntry(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
     private static final String STORE_PROCEDURE_SUBMIT_HOURS = "{call dbo.uspSTE_EMPApprovePeriod(?, ?, ?, ?)}";
     private static final String STORE_PROCEDURE_REOPEN_HOURS = "{call dbo.uspSTE_ReopenPeriod(?, ?, ?, ?)}";
     private static final String STORE_PROCEDURE_GET_PERIOD_INFORMATION = "{? = call dbo.uspSTE_GetPeriodRegTime(?, ?, ?, ?, ?)}";
 
     private Map<String, Password> userCache = new ConcurrentHashMap<String, Password>();
 
     public MSSQLHourRegDao(DataSource datasource) {
         this.datasource = datasource;
     }
 
     public static MSSQLHourRegDao createInstance() throws NamingException {
         DataSource dataSource = (DataSource) new InitialContext().lookup(JettyServer.DB_JNDI);
         if (dataSource == null) {
             throw new RuntimeException("Could not find datasource");
         }
         return new MSSQLHourRegDao(dataSource);
     }
 
     private Connection getConnection() throws SQLException {
         return datasource.getConnection();
     }
 
     private static void close(PreparedStatement statement, ResultSet res, Connection connection) {
         if (res != null) try {
             res.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         if (statement != null) try {
             statement.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         if (connection != null) try {
             connection.close();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     public PeriodDetails getPeriodDetails(String user, DateTime date) {
         PeriodDetails periodDetails = new PeriodDetails();
         Date sqlDate = new Date(date.getMillis());
 
         PreparedStatement statement = null;
         ResultSet res = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(SELECT_CURRENT_PERIOD);
             statement.setString(1, user.toUpperCase());
             statement.setDate(2, sqlDate);
             statement.setDate(3, sqlDate);
 
             res = statement.executeQuery();
             if (res != null && res.next()) {
                 periodDetails.setStartDate(new DateTime(res.getDate(1).getTime()));
                 periodDetails.setEndDate(new DateTime(res.getDate(2).getTime()));
                 periodDetails.setPeriodDescription(res.getString(3));
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, res, connection);
         }
         return periodDetails;
     }
 
     @Override
     public List<HourRegistration> getAllHoursForDate(String userId, DateTime date) {
         List<HourRegistration> result = new ArrayList<HourRegistration>();
 
         PreparedStatement statement = null;
         ResultSet res = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(SELECT_REGISTRATIONS);
             statement.setString(1, userId.toUpperCase());
             statement.setDate(2, new Date(date.getMillis()));
             res = statement.executeQuery();
             while (res.next()) {
                 Integer taskNumber = res.getInt(2);
                 String projectNumber = res.getString(3);
                 String activityCode = res.getString(4);
                 double hours = res.getDouble(8);
                 String description = res.getString(9);
                 boolean submitted = res.getBoolean(10);
                 boolean approved = res.getBoolean(11);
                 boolean rejected = res.getBoolean(12);
 
                 HourRegistration hourReg = new HourRegistration(taskNumber, projectNumber, activityCode, date, description,
                         hours, "", submitted, approved, rejected, null, null, null);
 
                 result.add(hourReg);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, res, connection);
         }
         return result;
     }
 
     @Override
     public Integer addHourRegistrations(String loggedInUser, String registeringForUser, String projectNumber,
                                         String activity, DateTime date, double hours, boolean isChargedHours,
                                         String workType, String description, boolean bypassChecks) {
 
         CallableStatement statement = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareCall(STORE_PROCEDURE_INSERT_HOURS);
             statement.registerOutParameter(1, Types.INTEGER);
             statement.setString(2, COUNTRY);
             statement.setString(3, loggedInUser.toUpperCase());
             statement.setString(4, registeringForUser.toUpperCase());
             statement.setString(5, projectNumber);
             statement.setString(6, activity);
             statement.setDate(7, new Date(date.getMillis()));
             statement.setDouble(8, hours);
             statement.setBoolean(9, isChargedHours);
             statement.setString(10, workType == null ? "" : workType);
             statement.setString(11, description);
             statement.setBoolean(12, bypassChecks);
 
             statement.execute();
             return statement.getInt(1);
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public void updateHourRegistration(String userId, String taskNumber, String projectNumber, String activity,
                                        DateTime date, double hours, boolean isBillable, String workType,
                                        String description) {
 
         CallableStatement statement = null;
         Connection connection = null;
         try {
             connection = getConnection();
             connection.setAutoCommit(false);
 
             statement = connection.prepareCall(STORE_PROCEDURE_UPDATE_HOURS);
             statement.setString(1, COUNTRY);
             statement.setString(2, userId.toUpperCase());
             statement.setString(3, taskNumber);
             statement.setString(4, projectNumber);
             statement.setString(5, activity);
             statement.setDate(6, new Date(date.getMillis()));
             statement.setDouble(7, hours);
             statement.setBoolean(8, isBillable);
             statement.setString(9, workType == null ? "" : workType);
             statement.setString(10, description);
 
             statement.executeUpdate();
             connection.commit();
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public void deleteHourRegistration(String userId, String taskNumber) {
         CallableStatement statement = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareCall(STORE_PROCEDURE_DELETE_HOURS);
             statement.setString(1, COUNTRY);
             statement.setString(2, userId.toUpperCase());
             statement.setString(3, taskNumber);
             statement.executeUpdate();
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public boolean addFavourites(String userId, String project_id, String activityCode) {
         PreparedStatement statement = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(INSERT_FAVOURITE);
             statement.setString(1, userId.toUpperCase());
             statement.setString(2, project_id);
             statement.setString(3, activityCode);
             statement.executeUpdate();
             return true;
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public List<UserFavourites> getUserFavourites(String userId) {
         List<UserFavourites> result = new ArrayList<UserFavourites>();
         PreparedStatement statement = null;
         ResultSet res = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(SELECT_FAVOURITES);
             statement.setString(1, userId.toUpperCase());
             res = statement.executeQuery();
             while (res.next()) {
                 String projectNumber = res.getString(1);
                 String activityCode = res.getString(2);
                 String description = res.getString(3);
                 int billable = res.getInt(4);
                 String projectName = res.getString(5);
                 String customer = res.getString(6);
                 int internalProject = res.getInt(7);
                 UserFavourites userFav = new UserFavourites(projectNumber, activityCode, description, billable, projectName, customer, internalProject);
                 result.add(userFav);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, res, connection);
         }
         return result;
     }
 
     @Override
     public void deleteFavourite(String userId, String projectNumber, String activityCode) {
         PreparedStatement statement = null;
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(DELETE_FAVOURITE);
             statement.setString(1, userId.toUpperCase());
             statement.setString(2, projectNumber);
             statement.setString(3, activityCode);
             statement.executeUpdate();
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     public List<ProjectDetail> searchProjects(String projectName) {
         List<ProjectDetail> result = new ArrayList<ProjectDetail>();
         PreparedStatement statement = null;
         String searchString = "%" + projectName + "%";
         Connection connection = null;
         ResultSet res = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(SELECT_SEARCHPROJECTS);
             statement.setString(1, searchString);
             statement.setString(2, projectName);
             res = statement.executeQuery();
             while (res.next()) {
                 String projectNumber = res.getString(1);
                 String activityCode = res.getString(2);
                 String description = res.getString(3);
                 ProjectDetail project = new ProjectDetail(projectNumber, activityCode, null, null, description);
                 result.add(project);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, res, connection);
         }
         return result;
     }
 
     @Override
     public WeekDetails getWeekList(String loggedInUser, String registeringForUser, String viewer, DateTime startDate) {
         CallableStatement statement = null;
         WeekDetails weekDetails = new WeekDetails();
 
         Connection connection = null;
         ResultSet resultSet = null;
         try {
             connection = getConnection();
             statement = connection.prepareCall(STORE_PROCEDURE_GET_PERIOD_INFORMATION);
             statement.registerOutParameter(1, Types.INTEGER);
             statement.setString(2, COUNTRY);
             statement.setString(3, loggedInUser.toUpperCase());
             statement.setString(4, viewer);
             statement.setString(5, registeringForUser.toUpperCase());
             statement.setDate(6, new Date(startDate.getMillis()));
 
             resultSet = statement.executeQuery();
             while (resultSet.next()) {
                 Integer recordId = resultSet.getInt(1);
                 String projectNumber = resultSet.getString(2);
                 String activityCode = resultSet.getString(3);
                 Date date = resultSet.getDate(4);
                 String entryDescription = resultSet.getString(5); // The users description for an hour
                 double hours = resultSet.getDouble(6);
                 String workType = resultSet.getString(7);
                 boolean submitted = resultSet.getBoolean(9);
                 boolean approved = resultSet.getBoolean(10);
                 boolean rejected = resultSet.getBoolean(11);
 
                 String projectName = resultSet.getString(15);
                 String customerName = resultSet.getString(16);
                 String activityDescription = resultSet.getString(17);
 
                 DateTime dateTime = date != null ? new DateTime(date.getTime()) : null;
                 if (!"FLEX".equalsIgnoreCase(projectNumber)) {
                     weekDetails.addEntry(recordId, projectNumber, activityCode, dateTime, entryDescription, hours, workType, submitted,
                             approved, rejected, projectName, customerName, activityDescription);
                 }
             }
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, resultSet, connection);
         }
 
         return weekDetails;
     }
 
     @Override
     public void submitHours(String loggedInUser, String registeringForUser, DateTime dayInPeriod) {
         CallableStatement statement = null;
 
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareCall(STORE_PROCEDURE_SUBMIT_HOURS);
             statement.setString(1, COUNTRY);
             statement.setString(2, loggedInUser.toUpperCase());
             statement.setString(3, registeringForUser.toUpperCase());
             statement.setDate(4, new Date(dayInPeriod.getMillis()));
 
             statement.executeUpdate();
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public void reopenHours(String loggedInUser, String registeringForUser, DateTime dayInPeriod) {
         CallableStatement statement = null;
 
         Connection connection = null;
         try {
             connection = getConnection();
             statement = connection.prepareCall(STORE_PROCEDURE_REOPEN_HOURS);
             statement.setString(1, COUNTRY);
             statement.setString(2, loggedInUser.toUpperCase());
             statement.setString(3, registeringForUser.toUpperCase());
             statement.setDate(4, new Date(dayInPeriod.getMillis()));
             statement.executeUpdate();
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, null, connection);
         }
     }
 
     @Override
     public List<NormTime> getNormTime(String username) {
         List<NormTime> result = new ArrayList<NormTime>();
         PreparedStatement statement = null;
         Connection connection = null;
         ResultSet res = null;
         try {
             connection = getConnection();
             statement = connection.prepareStatement(SELECT_NORMTIME);
             statement.setString(1, username.toUpperCase());
             res = statement.executeQuery();
             while (res.next()) {
                 String normcode = res.getString(1);
                 int monday = res.getInt(2);
                 int tuesday = res.getInt(3);
                 int wednesday = res.getInt(4);
                 int thursday = res.getInt(5);
                 int friday = res.getInt(6);
                 int saturday = res.getInt(7);
                 int sunday = res.getInt(8);
 
                 NormTime norm = new NormTime(normcode, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
                 result.add(norm);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             close(statement, res, connection);
         }
         return result;
     }
 
     @Override
     public User findUser(String userId, Password providedPassword) {
         PreparedStatement statement = null;
         ResultSet res = null;
         Connection connection = null;
        if (!userCache.containsKey(userId)) {
             try {
                 connection = getConnection();
                 statement = connection.prepareStatement(SELECT_USERS);
                statement.setString(1, userId.toUpperCase());
                 res = statement.executeQuery();
                 if (res.next()) {
                     userCache.put(userId, Password.fromPlaintext(providedPassword.getSalt(), res.getString(2)));
                 }
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 close(statement, res, connection);
             }
         }
         Password correctPassword = userCache.get(userId);
         if (providedPassword.equals(correctPassword)) {
             User user = new User();
             user.setUsername(userId);
             user.setPassword(correctPassword);
             return user;
         }
         return null;
     }
 }
