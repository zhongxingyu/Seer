 /**
  * 
  */
 package ecologylab.oodss.authentication.db;
 
 /**
  * @author Zachary O. Toups (zach@ecologylab.net)
  * 
  */
 public interface AuthenticationDBStrings
 {
 	/** End of an SQL statement that concludes with a string (includes single quotation mark). */
 	static final String	STATEMENT_END_STRING									= "';";
 
 	static final String	END_STRING														= ";";
 
 	/** End of an SQL statement that concludes with a string inside a parenthesis. */
 	static final String	STATEMENT_END_STRING_PAREN						= "');";
 
 	/**
 	 * 
 	 */
 	static final String	LIST_SEPARATOR_STRING_TYPE						= "', '";
 
 	static final String	DB_TABLE_USER													= "study_user";
 
	static final String	DB_COL_USER_ID												= "user_id";
 
 	static final String	COL_USER_KEY													= "user_key";
 
 	static final String	COL_PASSWORD													= "password";
 
 	// static final String COL_EMAIL = "email";
 
 	static final String	COL_AUX_USER_DATA											= "aux_user_data";
 
 	static final String	COL_LEVEL															= "level";
 
 	static final String	COL_ONLINE														= "online";
 
 	static final String	COL_LAST_ONLINE												= "last_online";
 
 	static final String	COL_SESSION_ID												= "session_id";
 
 	static final String	PREPARED_INSERT_USER									= "INSERT INTO "
 																																+ DB_TABLE_USER
 																																+ " ("
 																																+ COL_USER_KEY
 																																+ ", "
 																																+ COL_PASSWORD
 																																+ ", "
 																																+ COL_AUX_USER_DATA
 																																+ ") VALUES (?, ?, ?);";
 
 	static final String	PREPARED_SELECT_USER_BY_USER_KEY			= "SELECT * FROM "
 																																+ DB_TABLE_USER
 																																+ " WHERE "
 																																+ COL_USER_KEY
 																																+ " = ?;";
 
 	static final String	PREPARED_SELECT_USER_BY_SESSION_ID		= "SELECT * FROM "
 																																+ DB_TABLE_USER
 																																+ " WHERE "
 																																+ COL_SESSION_ID
 																																+ " = ?;";
 
 	static final String	SELECT_USER_LEVEL_BY_USER_KEY_PREFIX	= "SELECT "
 																																+ COL_LEVEL
 																																+ " FROM "
 																																+ DB_TABLE_USER
 																																+ " WHERE "
 																																+ COL_USER_KEY
 																																+ " = '";
 
 	static final String	PREPARED_DELETE_USER_BY_USER_KEY			= "DELETE FROM "
 																																+ DB_TABLE_USER
 																																+ " WHERE "
 																																+ COL_USER_KEY
 																																+ " = ?;";
 
 	static final String	PREPARED_LOGIN_USER										= "UPDATE "
 																																+ DB_TABLE_USER
 																																+ " SET "
 																																+ COL_ONLINE
 																																+ "=TRUE, "
 																																+ COL_LAST_ONLINE
 																																+ "=now(), "
 																																+ COL_SESSION_ID
 																																+ " = ? WHERE "
 																																+ COL_USER_KEY
 																																+ " = ?;";
 
	static final String	PREPARED_LOGOUT_USER_BY_USER_KEY			= "UPDATE "
 																																+ DB_TABLE_USER
 																																+ " SET "
 																																+ COL_ONLINE
 																																+ "=FALSE WHERE "
 																																+ COL_USER_KEY
 																																+ "= ?;";
 
 	static final String	PREPARED_LOGOUT_USER_BY_SESSION_ID		= "UPDATE "
 																																+ DB_TABLE_USER
 																																+ " SET "
 																																+ COL_ONLINE
 																																+ "=FALSE, "
 																																+ COL_SESSION_ID
 																																+ "=NULL WHERE "
 																																+ COL_SESSION_ID
 																																+ "=?;";
 
 	static final String	SELECT_ALL_ONLINE_USERS								= "SELECT * FROM "
 																																+ DB_TABLE_USER
 																																+ " WHERE "
 																																+ COL_ONLINE
 																																+ "=TRUE;";
 }
