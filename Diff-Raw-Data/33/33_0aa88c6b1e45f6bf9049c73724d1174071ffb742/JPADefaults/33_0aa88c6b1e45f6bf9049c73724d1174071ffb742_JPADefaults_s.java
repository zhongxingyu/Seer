 package jordner.jpa.preferences;
 
 public interface JPADefaults {
 
	public static final String JPA_UNIT = "development";
	public static final String H2_URL_EMBEDDED = "jdbc:h2:database/h2jpa;AUTO_SERVER=TRUE;ACCESS_MODE_DATA=rw;TRACE_LEVEL_FILE=4";
	public static final String H2_URL_SERVER = "jdbc:h2:tcp://localhost/~/database";
	public static final String H2_URL_EMBEDDED_NOTWORKED = "jdbc:h2:~/test";
	public static final String H2_URL_NOTWORKED = "jdbc:h2:tcp://localhost/~/test";
	public static final String H2_URL_EMBEDDED_HOME_DIR = "jdbc:h2:test";
 }
