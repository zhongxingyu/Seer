 package at.edu.uas.fm.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.mysql.jdbc.StringUtils;
 
 public class DBHelper {
 
 	private static final String USER_TYPE_WORKER = "Worker";
 	private static final String USER_TYPE_EMPLOYEE = "Employee";
 	private static final String USER_TYPE_CONTACT_PERSON = "ContactPerson";
 
 	private static final String STATUS_TO_DO = "ToDo";
 	private static final String STATUS_IN_PROGRESS = "InProgress";
 	private static final String STATUS_DONE = "Done";
 	private static final String STATUS_NOT_POSSIBLE = "NotPossible";
 
 	private static final String FREQUENCY_ONCE = "Once";
 	private static final String FREQUENCY_DAILY = "Daily";
 	private static final String FREQUENCY_WEEKLY = "Weekly";
 	private static final String FREQUENCY_MONTHLY = "Monthly";
 
 	public static Boolean authenticate(String username, String password) {
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Boolean result = false;
 
 		if (!StringUtils.isNullOrEmpty(username)
 				&& !StringUtils.isNullOrEmpty(password)) {
 
 			try {
 				String query = "SELECT * FROM user WHERE UserName = ?";
 				statement = getConnection().prepareStatement(query);
 				statement.setString(1, username);
 
 				queryResult = statement.executeQuery();
 				// we've got results - and we only check the first one
 				if (queryResult.first()
 						&& (password.equals(queryResult
 								.getString("UserPassword")))) {
 					// at least one entry existed AND the password does match
 					result = true;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (statement != null) {
 						statement.close();
 					}
 					if (queryResult != null) {
 						queryResult.close();
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 		return result;
 	}
 
 	public static Object[] getAllWorkers() {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 			String query = "SELECT * FROM user WHERE UserType = ?";
 			statement = getConnection().prepareStatement(query);
 			statement.setString(1, USER_TYPE_WORKER);
 			queryResult = statement.executeQuery();
 
 			List<Object> workerList = new ArrayList<Object>();
 
 			// we've got results
 			while (queryResult.next()) {
 				Object[] worker = new Object[10];
 
 				worker[0] = queryResult.getLong("UserId");
 				worker[1] = queryResult.getString("UserName");
 				worker[2] = queryResult.getString("FirstName");
 				worker[3] = queryResult.getString("LastName");
 				worker[4] = queryResult.getDate("DateOfBirth");
 				worker[5] = queryResult.getString("MobilePhoneNumber");
 				worker[6] = queryResult.getString("TelephoneNumber");
 				worker[7] = queryResult.getString("EMailAddress");
 				worker[8] = queryResult.getLong("Latitude");
 				worker[9] = queryResult.getLong("Longitude");
 
 				workerList.add(worker);
 			}
 
 			if (workerList.size() > 0) {
 				result = workerList.toArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static Object[] getTasks() {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 			String query = "SELECT * FROM task";
 			statement = getConnection().prepareStatement(query);
 			queryResult = statement.executeQuery();
 
 			List<Object> taskList = new ArrayList<Object>();
 
 			// we've got results
 			while (queryResult.next()) {
 				Object[] task = new Object[3];
 
 				task[0] = queryResult.getLong("TaskID");
 				task[1] = queryResult.getString("Name");
 				task[2] = queryResult.getString("Description");
 
 				taskList.add(task);
 			}
 
 			if (taskList.size() > 0) {
 				result = taskList.toArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static Object[] getWorkObjectsForUser(Long userId) {
 		if (userId == null) {
 			return new Object[0];
 		}
 		return getWorkObjects(userId);
 	}
 
 	public static Object[] getAllWorkObjects() {
 		return getWorkObjects(null);
 	}
 
 	private static Object[] getWorkObjects(Long userId) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 			String query = "";
 			if (userId != null) {
 				query = "SELECT DISTINCT wob.*, adr.*, usr.* FROM workobject wob INNER JOIN taskassignment tass ON tass.WorkObject_FK = wob.WorkObjectID "
 						+ "LEFT JOIN address adr ON wob.Address_FK = adr.AddressID LEFT JOIN user usr ON wob.User_FK = usr.UserID WHERE tass.User_FK = ?";
 			} else {
 				query = "SELECT DISTINCT wob.*, adr.*, usr.* FROM workobject wob LEFT JOIN address adr ON wob.Address_FK = adr.AddressID"
 						+ " LEFT JOIN user usr ON wob.User_FK = usr.UserID";
 
 			}
 
 			statement = getConnection().prepareStatement(query);
 			if (userId != null) {
 				statement.setLong(1, userId);
 			}
 
 			queryResult = statement.executeQuery();
 
 			List<Object> workObjectList = new ArrayList<Object>();
 
 			// we've got results
 			while (queryResult.next()) {
 				Object[] workObject = new Object[5];
 
 				workObject[0] = queryResult.getLong("WorkObjectID");
 				workObject[1] = queryResult.getString("WorkObjectNumber");
 				workObject[2] = queryResult.getString("Description");
 
 				Object[] address = new Object[8];
 				address[0] = queryResult.getLong("AddressID");
 				address[1] = queryResult.getString("Country");
 				address[2] = queryResult.getString("City");
 				address[3] = queryResult.getString("ZIPCode");
 				address[4] = queryResult.getString("Street");
 				address[5] = queryResult.getString("Number");
 				address[6] = queryResult.getLong("Latitude");
 				address[7] = queryResult.getLong("Longitude");
 
 				Object[] contactPerson = new Object[7];
 				contactPerson[0] = queryResult.getLong("UserId");
 				contactPerson[1] = queryResult.getString("FirstName");
 				contactPerson[2] = queryResult.getString("LastName");
 				contactPerson[3] = queryResult.getDate("DateOfBirth");
 				contactPerson[4] = queryResult.getString("MobilePhoneNumber");
 				contactPerson[5] = queryResult.getString("TelephoneNumber");
 				contactPerson[6] = queryResult.getString("EMailAddress");
 
 				workObject[3] = address;
 				workObject[4] = contactPerson;
 
 				workObjectList.add(workObject);
 			}
 
 			if (workObjectList.size() > 0) {
 				result = workObjectList.toArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static Object[] getAllAssignments() {
 		return getAssignments(null, false);
 	}
 
 	public static Object[] getAssignmentsForUser(Long userId) {
 		return getAssignments(userId, true);
 	}
 
 	private static Object[] getAssignments(Long userId, boolean forUser) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 			String query = "SELECT * FROM taskassignment";
 			if (forUser) {
 				query += " WHERE User_FK = ?";
 			}
 			statement = getConnection().prepareStatement(query);
 			if (forUser) {
 				statement.setLong(1, userId);
 			}
 			queryResult = statement.executeQuery();
 
 			List<Object> taskAssignmentList = new ArrayList<Object>();
 
 			// we've got results
 			while (queryResult.next()) {
 				Object[] taskAssignment = new Object[6];
 
 				taskAssignment[0] = queryResult.getLong("TaskAssignmentID");
 				taskAssignment[1] = queryResult.getString("Frequency");
 				taskAssignment[2] = queryResult
 						.getString("FrequencyInformation");
 				taskAssignment[3] = queryResult.getLong("Task_FK");
 				taskAssignment[4] = queryResult.getLong("WorkObject_FK");
 				taskAssignment[5] = queryResult.getLong("User_FK");
 
 				taskAssignmentList.add(taskAssignment);
 			}
 
 			if (taskAssignmentList.size() > 0) {
 				result = taskAssignmentList.toArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static String[] getFrequencies() {
 		String[] frequencies = new String[] { FREQUENCY_ONCE, FREQUENCY_DAILY,
 				FREQUENCY_WEEKLY, FREQUENCY_MONTHLY };
 		return frequencies;
 	}
 
 	public static String[] getUserTypes() {
 		String[] userTypes = new String[] { USER_TYPE_WORKER,
 				USER_TYPE_EMPLOYEE, USER_TYPE_CONTACT_PERSON };
 		return userTypes;
 	}
 
 	public static String[] getStatus() {
 		String[] status = new String[] { STATUS_TO_DO, STATUS_IN_PROGRESS,
 				STATUS_DONE, STATUS_NOT_POSSIBLE };
 		return status;
 	}
 
 	public static boolean deleteAssignment(Long assignmentId) {
 
 		PreparedStatement statement = null;
 		boolean result = false;
 
 		try {
 
 			String query = "DELETE FROM taskassignment WHERE TaskAssignmentID = ?";
 			statement = getConnection().prepareStatement(query);
 			statement.setLong(1, assignmentId);
 			int rowCount = statement.executeUpdate();
 
 			if (rowCount > 0) {
 				result = true;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static boolean insertAssignment(Long workObjectId, Long taskId,
 			String frequency, String frequencyInformation, Long workerId) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		boolean result = false;
 
 		try {
 
 			String query = "INSERT INTO taskassignment (Frequency, FrequencyInformation, Task_FK, WorkObject_FK, User_FK) VALUES (?, ?, ?, ?, ?)";
 			statement = getConnection().prepareStatement(query);
 
 			statement.setString(1, frequency);
 			statement.setString(2, frequencyInformation);
 			statement.setLong(3, taskId);
 			statement.setLong(4, workObjectId);
 			statement.setLong(5, workerId);
 
 			int rowCount = statement.executeUpdate();
 
 			if (rowCount > 0) {
 				result = true;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static Object[] getWorkItemListForAssignments(
 			List<Long> assignmentIds) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 			String assignmentIdsString = "";
 			int size = assignmentIds.size();
 			for (int i = 0; i < size; i++) {
 				if (i == size - 1) {
 					assignmentIdsString += "?";
 				} else {
 					assignmentIdsString += "?, ";
 				}
 			}
 
 			String query = "SELECT wi.*, tass.Frequency FROM workitem wi INNER JOIN taskassignment tass ON wi.TaskAssignment_FK = tass.TaskAssignmentID "
 					+ "WHERE wi.TaskAssignment_FK IN ("
 					+ assignmentIdsString
 					+ ") ORDER BY wi.TaskAssignment_FK";
 			statement = getConnection().prepareStatement(query);
 
 			int i = 1;
 			for (Long assignmentId : assignmentIds) {
 				statement.setLong(i, assignmentId);
 				i++;
 			}
 
 			queryResult = statement.executeQuery();
 
 			Map<Long, List<Object>> assignmentWorkItemMap = new HashMap<Long, List<Object>>();
 			Map<Long, String> assignmentFrequencyMap = new HashMap<Long, String>();
 
 			// we've got results, build helper map
 			while (queryResult.next()) {
 
 				Long assignmentId = queryResult.getLong("TaskAssignment_FK");
 
 				// create work item data
 				Object[] workItem = new Object[4];
 				workItem[0] = queryResult.getLong("WorkItemID");
 				workItem[1] = assignmentId;
 				workItem[2] = queryResult.getString("Status");
 				workItem[3] = queryResult.getDate("Date");
 
 				// fill assignment <-> work item map
 				List<Object> workItemList = assignmentWorkItemMap
 						.get(assignmentId);
 				if (workItemList == null) {
 					workItemList = new ArrayList<Object>();
 					assignmentWorkItemMap.put(assignmentId, workItemList);
 				}
 				workItemList.add(workItem);
 
 				// fill assignment <-> frequency map
 				String frequency = queryResult.getString("Frequency");
 				String frequencyFromMap = assignmentFrequencyMap
 						.get(assignmentId);
 				if (frequencyFromMap == null) {
 					assignmentFrequencyMap.put(assignmentId, frequency);
 				} else if (!frequencyFromMap.equals(frequency)) {
 					throw new Exception("Error occured");
 				}
 			}
 
 			// filter by frequency and build result work item list
 			List<Object> workItemResultList = new ArrayList<Object>();
 			Calendar today = Calendar.getInstance();
 
 			for (Map.Entry<Long, List<Object>> assignmentWorkItemMapEntry : assignmentWorkItemMap
 					.entrySet()) {
 				List<Object> workItemList = assignmentWorkItemMapEntry
 						.getValue();
 				String frequency = assignmentFrequencyMap
 						.get(assignmentWorkItemMapEntry.getKey());
 
 				for (Object workItem : workItemList) {
 					Date workItemDate = (Date) ((Object[]) workItem)[3];
 					Calendar workItemCalendar = Calendar.getInstance();
 					workItemCalendar.setTime(workItemDate);
 
 					if (FREQUENCY_DAILY.equals(frequency)) {
 
 						boolean sameDay = today.get(Calendar.YEAR) == workItemCalendar
 								.get(Calendar.YEAR)
 								&& today.get(Calendar.DAY_OF_YEAR) == workItemCalendar
 										.get(Calendar.DAY_OF_YEAR);
 						if (!sameDay) {
 							continue;
 						}
 
 					} else if (FREQUENCY_WEEKLY.equals(frequency)) {
 
 						boolean sameWeek = today.get(Calendar.YEAR) == workItemCalendar
 								.get(Calendar.YEAR)
 								&& today.get(Calendar.WEEK_OF_YEAR) == workItemCalendar
 										.get(Calendar.WEEK_OF_YEAR);
 						if (!sameWeek) {
 							continue;
 						}
 
 					} else if (FREQUENCY_MONTHLY.equals(frequency)) {
 
 						boolean sameMonth = today.get(Calendar.YEAR) == workItemCalendar
 								.get(Calendar.YEAR)
 								&& today.get(Calendar.MONTH) == workItemCalendar
 										.get(Calendar.MONTH);
 						if (!sameMonth) {
 							continue;
 						}
 
 					} else {
 						continue;
 					}
 
 					workItemResultList.add(workItem);
 				}
 
 			}
 
 			if (workItemResultList.size() > 0) {
 				result = workItemResultList.toArray();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static Object[] insertOrUpdateWorkItem(Long workItemId,
 			Long taskAssignmentId, String status, Date date) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		Object[] result = new Object[0];
 
 		try {
 
 			boolean isUpdate = workItemId != null;
 			String query = "";
 			if (!isUpdate) {
 				query = "INSERT INTO workitem (TaskAssignment_FK, Status, Date) VALUES (?, ?, ?)";
 			} else {
 				query = "UPDATE workitem SET TaskAssignment_FK = ?, Status = ?, Date = ? WHERE WorkItemID = ?";
 			}
 			statement = getConnection().prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			statement.setLong(1, taskAssignmentId);
 			statement.setString(2, status);
 			statement.setDate(3, new java.sql.Date(date.getTime()));
 			if (isUpdate) {
 				statement.setLong(4, workItemId);
 			}
 
 			int rowCount = statement.executeUpdate();
 
 			if (rowCount > 0) {
 				Long returnWorkItemId = workItemId;
 
 				ResultSet resultSet = statement.getGeneratedKeys();
 				if (!isUpdate && resultSet != null && resultSet.first()) {
 					returnWorkItemId = resultSet.getLong(1);
 				}
 
 				result = new Object[] { returnWorkItemId, taskAssignmentId,
 						status, date };
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static boolean insertAdditionalWorkItem(String taskName,
 			String taskDescription, String frequency,
 			String frequencyInformation, String status, Long userId,
 			Long workObjectId) {
 
 		PreparedStatement statement = null;
 		ResultSet queryResult = null;
 		boolean result = false;
 
 		try {
 
 			String query = "INSERT INTO additionalworkitem (TaskName, TaskDescription, Frequency, FrequencyInformation, Status, User_FK, WorkObject_FK) VALUES (?, ?, ?, ?, ?, ?, ?)";
 			statement = getConnection().prepareStatement(query);
 
 			statement.setString(1, taskName);
 			statement.setString(2, taskDescription);
 			statement.setString(3, frequency);
 			statement.setString(4, frequencyInformation);
 			statement.setString(5, status);
 			statement.setLong(6, userId);
 			statement.setLong(7, workObjectId);
 
 			int rowCount = statement.executeUpdate();
 
 			if (rowCount > 0) {
 				result = true;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 				if (queryResult != null) {
 					queryResult.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	private static Connection getConnection() {
 		return MySQLConnector.getConnection();
 	}
 
 }
