 package no.steria.swhrs;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 public class MSSQLHourRegDao implements HourRegDao {
 	
 	
 	private final DataSource datasource;
 	private Connection connection = null;
 	private static final String SELECT_USERS = "SELECT No_, \"WEB Password\" FROM \"Norge$Resource\" where No_ = ? and \"WEB Password\" = ?";
 	private static final String SELECT_REGISTRATIONS = "SELECT * FROM \"Norge$Time Entry\" WHERE Ressourcekode = ? AND Dato = ? AND Projektnr_ NOT LIKE 'FLEX'";
 	private static final String SELECT_FAVOURITES = "SELECT \"Norge$Favourite Task\".Projektnr_, \"Norge$Favourite Task\".Aktivitetskode, \"Norge$Tasklist\".Beskrivelse, \"Norge$Tasklist\".\"Ressource fakturerbar\", \"Norge$Job\".Description, \"Norge$Job\".Name,  \"Norge$Job\".\"Internt projekt\"  FROM \"Norge$Favourite Task\"  INNER JOIN \"Norge$Tasklist\" ON \"Norge$Favourite Task\".Projektnr_= \"Norge$Tasklist\".Projektnr_ AND \"Norge$Favourite Task\".Aktivitetskode = \"Norge$Tasklist\".Kode INNER JOIN \"Norge$Job\" ON \"Norge$Favourite Task\".Projektnr_= \"Norge$Job\".No_ WHERE \"Norge$Favourite Task\".Resourcekode = ? AND \"Norge$Tasklist\".Sprret NOT LIKE '1'";
 	//private static final String SELECT_PROJECTS = "select \"Norge$Tasklist\".Projektnr_, \"Norge$Tasklist\".Kode, \"Norge$Tasklist\".Beskrivelse FROM \"Norge$Tasklist\" WHERE Type=0 and Afsluttet=0 and Sprret=0 and Vis=1 and Status=2";
 	private static final String SELECT_WEEKREGISTRATIONS = "SELECT Dato, sum(Antal), Godkendt FROM \"Norge$Time Entry\" where Ressourcekode = ? AND Projektnr_ NOT LIKE 'FLEX' AND Dato Between ? AND ? GROUP BY Dato, Godkendt";
 	private static final String SELECT_SEARCHPROJECTS = "SELECT TOP 50 \"Norge$Tasklist\".Projektnr_, \"Norge$Tasklist\".Kode, \"Norge$Tasklist\".Beskrivelse FROM \"Norge$Tasklist\" WHERE Beskrivelse like ? OR Projektnr_ like ? ORDER BY Beskrivelse";
 	private static final String SELECT_PERIODS = "SELECT Startdato, Slutdato, Beskrivelse, Bogfrt FROM \"Norge$Time Periods\" WHERE Ressource = ? AND Startdato <= ? AND Slutdato >= ?";
 	private static final String DELETE_REGISTRATION = "DELETE FROM \"Norge$Time Entry\" where Lbenr_ = ? AND Godkendt = 0 AND Bogfrt = 0";
 	private static final String INSERT_FAVOURITE = "INSERT into \"Norge$Favourite Task\" (Resourcekode, Projektnr_, Aktivitetskode) VALUES(?, ?, ?)";
 //	private static final String INSERT_REGISTRATION = "INSERT into \"Norge$Time Entry\" (Projektnr_, Aktivitetskode, Ressourcekode, Arbejdstype, Dato, Antal, Beskrivelse) Values(?, ?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_REGISTRATION = "INSERT into \"Norge$Time Entry\" (Projektnr_, Aktivitetskode, Ressourcekode, Arbejdstype, Dato, Antal, Beskrivelse, Godkendt, Bogfrt, Fakturerbart, Linienr_, \"Internt projekt\", \"Lg til norm tid\", Afdelingsleder, \"Shortcut Dimension 1 Code\", \"Shortcut Dimension 2 Code\", \"Ressource Gruppe Nr_\", \"Exportert Tieto\", \"Ikke godkjent\", \"Ikke godkjent Beskrivelse\", \"Ikke godkjent av\", \"Endret dato\", \"Endret av\", \"Transferdate Tieto\", \"Approved By LM_PM\", \"Adjust flex limit\") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 	private static final String UPDATE_REGISTRATION = "UPDATE \"Norge$Time Entry\" SET Godkendt=? WHERE Ressourcekode = ? AND Dato BETWEEN ? AND ?";
 	private static final String DELETE_FAVOURITE = "DELETE FROM \"Norge$Favourite Task\" WHERE Resourcekode = ? AND Projectnr_ = ? AND Aktivitetskode = ?";
 	private static final String UPDATE_REGISTRATIONHOURS = "UPDATE \"Norge$Time Entry\" SET Antal = ?, Beskrivelse = ? WHERE Lbenr_ = ?";
 	
 	public MSSQLHourRegDao(DataSource datasource) {
 		this.datasource = datasource;
 		
 	}
 
 	@Override
 	public void beginTransaction() {
 		try {
 			connection = datasource.getConnection();
 			connection.setAutoCommit(false);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void endTransaction(boolean b) {
 		try {
 			connection.commit();
 			//connection.rollback();
 			connection.close();
 			connection = null;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public List<HourRegistration> getAllHoursForDate(String userid,
 			String date) {
 		List<HourRegistration> result = new ArrayList<HourRegistration>();
 		
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(SELECT_REGISTRATIONS);
 			statement.setString(1, userid);
 			statement.setString(2, date);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
 				int taskNumber = res.getInt(2);
 				String projectNumber = res.getString(3);
 				String activityCode = res.getString(4);
 				double hours = res.getDouble(8);
 				String description = res.getString(9);
 				boolean submitted = res.getBoolean(10);
 				boolean approved = res.getBoolean(11);
 				
 				HourRegistration hourReg = new HourRegistration(date, taskNumber, projectNumber, activityCode, hours, description, submitted, approved);
 				result.add(hourReg);	
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public boolean validateUser(String userid, String password) {
 		PreparedStatement statement = null;
 		Users users = new Users();
 		String user = null;
 		try {
 			statement = connection.prepareStatement(SELECT_USERS);
 			statement.setString(1, userid);
 			statement.setString(2, password);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
 				System.out.println(res.getString(1)+":"+res.getString(2));
 				users.setUsername(res.getString(1));
 				user = res.getString(1);
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		if(user != null)return true;
 		return false;	
 	}
 
 
 	@Override
 	public boolean deleteHourRegistration(String taskNumber) {
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(DELETE_REGISTRATION);
 			statement.setString(1, taskNumber);
 			int delResult = statement.executeUpdate();
 			System.out.println(delResult);
 			if (delResult == 0) {
 				return false;
 			} else {
 				return true;
 			}
 			
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 
 	@Override
 	public List<UserFavourites> getUserFavourites(String userid) {
 		List<UserFavourites> result = new ArrayList<UserFavourites>();
 		PreparedStatement statement = null;
 		int counter = 0;
 		try {
 			statement = connection.prepareStatement(SELECT_FAVOURITES);
 			statement.setString(1, userid);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
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
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		System.out.println(counter);
 		return result;
 	}
 	
 	@Override
 	public List<Projects> searchProjects(String projectName) {
 		List<Projects> result = new ArrayList<Projects>();
 		System.out.println(projectName);
 		PreparedStatement statement = null;
 		int counter = 0;
 		String searchString = "%"+projectName+"%";
 		System.out.println(searchString);
 		try {
 			statement = connection.prepareStatement(SELECT_SEARCHPROJECTS);
 			statement.setString(1, searchString);
 			statement.setString(2, projectName);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
 				String projectNumber = res.getString(1);
 				String activityCode = res.getString(2);
 				String description = res.getString(3);
 				Projects project = new Projects(projectNumber, activityCode, description);
 				result.add(project);
 				counter++;
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		System.out.println(counter);
 		return result;
 	}
 	
 	@Override
 	public List<WeekRegistration> getWeekList(String userid, String dateFrom,
 			String dateTo) {
 		List<WeekRegistration> result = new ArrayList<WeekRegistration>();
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(SELECT_WEEKREGISTRATIONS);
 			statement.setString(1, userid);
 			statement.setString(2, dateFrom);
 			statement.setString(3, dateTo);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
 				String date = res.getString(1);
 				double hours = res.getDouble(2);
 				int approved = res.getInt(3);
 				WeekRegistration weekReg = new WeekRegistration(date, hours, approved);
 				result.add(weekReg);
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return result;
 		
 	}
 	
 	@Override
 	public DatePeriod getPeriod(String userid, String date) {
 		DatePeriod datePeriod = new DatePeriod();
 		PreparedStatement statement = null;
 		int counter = 0;
 		try {
 			statement = connection.prepareStatement(SELECT_PERIODS);
 			statement.setString(1, userid);
 			statement.setString(2, date);
 			statement.setString(3, date);
 			ResultSet res = statement.executeQuery();
 			while(res.next()){
 				datePeriod.setFromDate(res.getString(1));
 				datePeriod.setToDate(res.getString(2));
 				datePeriod.setDescription(res.getString(3));
 				counter++;
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		System.out.println(counter);
 		return datePeriod;
 	}
 	
 	@Override
 	public boolean addFavourites(String userid, String project_id, String activityCode) {
 		
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(INSERT_FAVOURITE);
 			statement.setString(1, userid);
 			statement.setString(2, project_id);
 			statement.setString(3, activityCode);
 			statement.executeUpdate();
 			return true;
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	@Override
 	public boolean addHourRegistrations(String projectNumber, String activityCode,
 			String userid, String workType, String date, double hours, String description, 
 			int submitted, int approved , int billable, int linenumber, int internalProject, 
 			int addNormTime, String departmentManager, String shortcutDimensionOneCode, 
 			String shortcutDimensionTwoCode, String resourceGroupNumber, int exportTieto, int notApproved, 
 			String notApprovedDescription, String notApprovedBy, String changedDate, String changedBy, 
 			String transferedTieto, int approvedByLMPM, int adjustFlexLimit) {
 		
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(INSERT_REGISTRATION );
 			statement.setString(1, projectNumber);
 			statement.setString(2, activityCode);
 			statement.setString(3, userid);
 			statement.setString(4, workType);
 			statement.setString(5, date);
 			statement.setDouble(6, hours);
 			statement.setString(7, description);
 			statement.setInt(8, submitted);
 			statement.setInt(9, approved);
 			statement.setInt(10, billable);
 			statement.setInt(11, linenumber);
 			statement.setInt(12, internalProject);
 			statement.setInt(13, addNormTime);
 			statement.setString(14, departmentManager);
 			statement.setString(15, shortcutDimensionOneCode);
 			statement.setString(16, shortcutDimensionTwoCode);
 			statement.setString(17, resourceGroupNumber);
 			statement.setInt(18, exportTieto);
 			statement.setInt(19, notApproved);
 			statement.setString(20, notApprovedDescription);
 			statement.setString(21, notApprovedBy);
 			statement.setString(22, changedDate);
 			statement.setString(23, changedBy);
 			statement.setString(24, transferedTieto);
 			statement.setInt(25, approvedByLMPM);
 			statement.setInt(26, adjustFlexLimit);
 			statement.executeUpdate();
 			return true;
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@Override
 	//remove this method later
 	public boolean addHourRegistrations(String projectNumber,
 			String activityCode, String workType, String date, String username,
 			double hours, String description) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public void deleteFavourite(String userid, String projectNumber,
 			String activityCode) {
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(DELETE_FAVOURITE);
 			statement.setString(1, userid);
 			statement.setString(2, projectNumber);
 			statement.setString(3, activityCode);
 			statement.executeUpdate();
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@Override
 	public void updateRegistration(int taskNumber, double hours,
 			String description) {
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(UPDATE_REGISTRATIONHOURS);
 			statement.setDouble(1, hours);
 			statement.setString(2, description);
 			statement.setInt(3, taskNumber);
 			statement.executeUpdate();
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 
 	@Override
 	public void updatePeriod(String userid, int option, String fromDate,
 			String toDate) {
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(UPDATE_REGISTRATION);
 			statement.setInt(1, option);
 			statement.setString(2, userid);
 			statement.setString(3, fromDate);
 			statement.setString(4, toDate);
 			statement.executeUpdate();
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		finally{
 			try {
 				if(statement != null)statement.close();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 	}
 
 }
