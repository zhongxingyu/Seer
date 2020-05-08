 package uk.co.brotherlogic.mdb;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Properties;
 
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 /**
  * Class to deal with database connection
  * 
  * @author Simon Tucker
  */
 public final class Connect
 {
    /** enum of modes */
    private enum mode
    {
       DEVELOPMENT, PRODUCTION
    }
 
   private static boolean forced = false;
    /** Current mode of operation */
    private static mode operationMode = mode.DEVELOPMENT;
    private static Connect singleton;
 
    /**
     * Static constructor
     * 
     * @return A suitable db connection
     * @throws SQLException
     *            if a db connection cannot be established
     */
    public static Connect getConnection() throws SQLException
    {
       if (singleton == null)
       {
          singleton = new Connect(operationMode);
 
          // Upgrade the database ready for use
          DBUpgrade.upgradeDB();
       }
       return singleton;
    }
 
    public static String getSource()
    {
       if (operationMode == mode.DEVELOPMENT)
          return "Dev";
       else
          return "";
    }
 
    public static void main(final String[] args) throws Exception
    {
       System.out.println(Connect.getConnection().getVersionString());
       Collection<Record> records = GetRecords.create().getRecords(GetRecords.UNSHELVED, "12");
       for (Record rec : records)
          System.out.println(rec.getAuthor() + " - " + rec.getTitle());
       System.exit(1);
    }
 
    public static void setForDevMode()
    {
       operationMode = mode.DEVELOPMENT;
      forced = true;
    }
 
    public static void setForProdMode()
    {
       operationMode = mode.PRODUCTION;
      forced = true;
    }
 
    /** The connection to the local DB */
    private Connection locDB;
 
    String longestQuery = "";
 
    long longestQueryTime = 0;
    int sCount = 0;
 
    long totalDBTime = 0;
 
    private Connect(mode operationMode) throws SQLException
    {
       makeConnection(operationMode);
    }
 
    /**
     * Cancels all impending transactions
     * 
     * @throws SQLException
     *            if the cancel fails
     */
    public void cancelTrans() throws SQLException
    {
       locDB.rollback();
    }
 
    /**
     * Commits the impending transactions
     * 
     * @throws SQLException
     *            If the commit fails
     */
    public void commitTrans() throws SQLException
    {
       locDB.commit();
    }
 
    public ResultSet executeQuery(PreparedStatement ps) throws SQLException
    {
 
       sCount++;
 
       long sTime = System.currentTimeMillis();
       ResultSet rs = ps.executeQuery();
       long eTime = System.currentTimeMillis() - sTime;
       totalDBTime += eTime;
       if (eTime > longestQueryTime)
       {
          longestQueryTime = eTime;
          longestQuery = ps.toString();
       }
       return rs;
    }
 
    public void executeStatement(PreparedStatement ps) throws SQLException
    {
 
       sCount++;
 
       long sTime = System.currentTimeMillis();
       ps.execute();
       long eTime = System.currentTimeMillis() - sTime;
       totalDBTime += eTime;
       if (eTime > longestQueryTime)
       {
          longestQueryTime = eTime;
          longestQuery = ps.toString();
       }
    }
 
    public long getLQueryTime()
    {
       return longestQueryTime;
    }
 
    /**
     * Builds a prepared statements from the data store
     * 
     * @param sql
     *           The statement to build
     * @return a {@link PreparedStatement}
     * @throws SQLException
     *            If the construction fails
     */
    public PreparedStatement getPreparedStatement(final String sql) throws SQLException
    {
       if (operationMode == mode.DEVELOPMENT)
          System.err.println("Qu: " + sql);
 
       // Create the statement
       PreparedStatement ps = locDB.prepareStatement(sql);
       return ps;
    }
 
    public int getSCount()
    {
       return sCount;
    }
 
    public long getTQueryTime()
    {
       return totalDBTime;
    }
 
    public String getVersionString()
    {
       // Read from the properties file
       Properties props = new Properties();
       try
       {
          props.load(this.getClass().getResourceAsStream("/properties"));
          return props.getProperty("mdbcore.version").trim();
       }
       catch (IOException e)
       {
          return "DEV";
       }
    }
 
    /**
     * Makes the connection to the DB
     * 
     * @throws SQLException
     *            if something fails
     */
    private void makeConnection(mode operationMode) throws SQLException
    {
       try
       {
          // Load all the drivers and initialise the database connection
          Class.forName("org.postgresql.Driver");
 
          // Check on the operation mode
         if (getVersionString().contains("SNAPSHOT") && !forced)
             operationMode = mode.DEVELOPMENT;
          else
             operationMode = mode.PRODUCTION;
 
          if (operationMode == mode.PRODUCTION)
             locDB = DriverManager.getConnection("jdbc:postgresql://192.168.1.100/music?user=music");
          else
          {
             System.err.println("Connection to development database");
             locDB = DriverManager
                   .getConnection("jdbc:postgresql://localhost/musicdev?user=musicdev");
          }
 
          // Switch off auto commit
          locDB.setAutoCommit(false);
       }
       catch (ClassNotFoundException e)
       {
          throw new SQLException(e);
       }
    }
 
    public void printStats()
    {
       System.out.println("SQL: " + longestQueryTime + " => " + longestQuery);
    }
 }
