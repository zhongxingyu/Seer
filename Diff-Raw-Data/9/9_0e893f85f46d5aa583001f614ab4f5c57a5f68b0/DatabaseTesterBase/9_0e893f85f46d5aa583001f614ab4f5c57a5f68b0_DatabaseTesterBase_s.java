 package com.seitenbau.testing.dbunit.tester;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbcp.DelegatingConnection;
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.database.DatabaseConfig;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.DataSetException;
 import org.dbunit.dataset.DefaultDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.SortedTable;
 import org.dbunit.dataset.datatype.IDataTypeFactory;
 import org.dbunit.operation.DatabaseOperation;
 
 import com.seitenbau.testing.config.StoredProperty;
 import com.seitenbau.testing.config.TestConfiguration;
 import com.seitenbau.testing.dbunit.DatabaseTester;
 import com.seitenbau.testing.dbunit.SortConfig;
 import com.seitenbau.testing.dbunit.TestConfigDatabase;
 import com.seitenbau.testing.dbunit.dsl.DataSetIdentificator;
 import com.seitenbau.testing.dbunit.dsl.DataSetRegistry;
 import com.seitenbau.testing.dbunit.extend.DatabaseOperationFactory;
 import com.seitenbau.testing.dbunit.extend.DatabaseTesterCleanAction;
 import com.seitenbau.testing.dbunit.extend.DbUnitDatasetFactory;
 import com.seitenbau.testing.dbunit.extend.impl.DefaultDbUnitDatabaseOperationFactory;
 import com.seitenbau.testing.dbunit.modifier.IDataSetModifier;
 import com.seitenbau.testing.util.Future;
 
 public class DatabaseTesterBase<MY_TYPE>
 {
 
   /**
    * Class der 'Umgebung' Bentigt zum finden des korrekten packages
    * beim Laden von XML Dateien.
    */
   private Class<?> fClazz;
 
   protected IDatabaseConnection fDatabaseConnection;
 
   protected List<IDataSetModifier> fDefaultModifierList = new ArrayList<IDataSetModifier>();
 
   protected String fDriverName;
 
   protected IDataSet fLastInsertedDataSet;
 
   protected boolean fKeepLastInsertedDataSet = true;
 
   protected String fPassword;
 
   protected String fPrefixClasspath = "";
 
   protected String fUrl;
 
   protected String fUsername;
 
   protected String fSchema;
 
   protected IDataTypeFactory _registerTypeFactory;
 
   protected Future<DataSource> _lazySource;
 
   protected List<DatabaseTesterCleanAction> _cleanActions = new ArrayList<DatabaseTesterCleanAction>();
 
   protected Map<String, Boolean> _dbUnitFeatures;
 
   protected Map<String, Object> _dbUnitProperty;
 
   protected DatabaseOperationFactory _databaseOperationFactory;
 
   /**
    * Konstruktor welcher gleich die Verbindungsdaten zur Datenbank
    * setzt.
    *
    * <code><pre>
    *     dbTester = new DatabaseTester(
    *        "org.gjt.mm.mysql.Driver",
    *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
    *        "user",
    *        "password"
    *       );
    * </pre></code>
    *
    * @param driverName Klassen - Name fr den Datenbank Treiber.
    *
    * @param url URL der Datenbank
    *
    * @param username Datenbank Benutzer
    *
    * @param password Datenbank Benutzer Passwort
    */
   public DatabaseTesterBase(String driverName, String url, String username, String password,
       IDataSetModifier... defaultModifiers)
   {
     fDriverName = driverName;
     fUrl = url;
     fUsername = username;
     fPassword = password;
     init(defaultModifiers, null);
   }
 
   /**
    * Konstruktor welcher gleich die Verbindungsdaten zur Datenbank
    * setzt.
    *
    * <code><pre>
    *     dbTester = new DatabaseTester(
    *        "org.gjt.mm.mysql.Driver",
    *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
    *        "user",
    *        "password",
    *        getClass()
    *       );
    * </pre></code>
    *
    * @param driverName Klassen - Name fr den Datenbank Treiber.
    *
    * @param url URL der Datenbank
    *
    * @param username Datenbank Benutzer
    *
    * @param clazz Class Objekt dessen package dazu genutzt wird um bei
    *        Laden von XML-DataSet-Dateien das korrekte
    *        Unterverzeichnis zu nutzen.
    *
    * @param password Datenbank Benutzer Passwort
    */
   public DatabaseTesterBase(String driverName, String url, String username, String password, Class<?> clazz,
       IDataSetModifier... defaultModifiers)
   {
     fDriverName = driverName;
     fUrl = url;
     fUsername = username;
     fPassword = password;
     init(defaultModifiers, clazz);
   }
 
   /**
    * Konstruktor welcher gleich die Verbindungsdaten zur Datenbank
    * setzt.
    *
    * <code><pre>
    *     dbTester = new DatabaseTester(
    *        "org.gjt.mm.mysql.Driver",
    *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
    *        "user",
    *        "password",
    *        getClass()
    *       );
    * </pre></code>
    *
    * @param driverName Klassen - Name fr den Datenbank Treiber.
    *
    * @param url URL der Datenbank
    *
    * @param username Datenbank Benutzer
    *
    * @param password Datenbank Benutzer Passwort
    *
    * @param fSchema Datenbank Schema
    */
   public DatabaseTesterBase(String driverName, String url, String username, String password, String schemema,
       IDataSetModifier... defaultModifiers)
   {
     fDriverName = driverName;
     fUrl = url;
     fUsername = username;
     fPassword = password;
     fSchema = schemema;
     init(defaultModifiers, null);
   }
 
   public DatabaseTesterBase(Class<? extends TestConfigDatabase> configClass, IDataSetModifier... defaultModifiers)
   {
     this(TestConfiguration.getString("db.driver"), TestConfiguration.getString("db.url"), TestConfiguration
         .getString("db.username"), TestConfiguration.getString("db.password"), defaultModifiers);
     String schema = TestConfiguration.getString("db.schema");
     if (schema != null && schema != StoredProperty.NOT_SET_VALUE && !schema.isEmpty())
     {
       setSchema(schema);
     }
   }
 
   public DatabaseTesterBase(DataSource ds, String schema, IDataSetModifier... defaultModifiers)
   {
     init(defaultModifiers, null);
     setSchema(schema);
     setConnection(ds);
   }
 
   public DatabaseTesterBase(Future<DataSource> lazySource, IDataSetModifier... defaultModifiers)
   {
     addDefaultModifier(defaultModifiers);
     init(defaultModifiers, null);
     _lazySource = lazySource;
   }
 
   public DatabaseTesterBase(BasicDataSource dataSource, IDataSetModifier... defaultModifiers)
   {
     init(defaultModifiers, null);
     setConnection(dataSource);
   }
 
   /**
    * Setzt eine Type Factory die registiert auf einer neuen Connection
    * Registiert wird. Wurde die Connection schon erstell.
    *
    * @param registerTypeFactory
    */
   public MY_TYPE setTypeFactoryToRegister(IDataTypeFactory registerTypeFactory)
   {
     this._registerTypeFactory = registerTypeFactory;
     registerTypeFactory(fDatabaseConnection);
     return myself();
   }
 
   /**
    * Setzt das Schema fr die Datenbank Connection
    * @param schema
    * @return
    */
   public MY_TYPE setSchema(String schema)
   {
     this.fSchema = schema;
     return myself();
   }
 
   public void init(IDataSetModifier[] defaultModifiers, Class<?> clazz)
   {
     setDbUnitFeature("http://www.dbunit.org/features/caseSensitiveTableNames", false);
     addDefaultModifier(defaultModifiers);
     if (clazz != null)
     {
       fClazz = clazz;
     }
     else
     {
       doMagicClazzFind();
     }
   }
 
   /**
    * Fgt einen Modifier zur Liste der Default modifier hinzu.
    * <p>
    * Default Modifier werden immer beim Laden von Datenstzen( bspw.
    * ber {@link #getDataSet(String, IDataSetModifier...)} )
    * ausgefhrt.
    * </p>
    *
    * @param aModifier Der hinzuzufgende Modifier.
    */
   public MY_TYPE addDefaultModifier(IDataSetModifier aModifier)
   {
     if (fDefaultModifierList == null)
     {
       fDefaultModifierList = new ArrayList<IDataSetModifier>();
     }
     fDefaultModifierList.add(aModifier);
     return myself();
   }
 
   /**
    * Fgt einen Modifier zur Liste der Default modifier hinzu.
    * <p>
    * Default Modifier werden immer beim Laden von Datenstzen( bspw.
    * ber {@link #getDataSet(String, IDataSetModifier...)} )
    * ausgefhrt.
    * </p>
    *
    * @param aModifier Der hinzuzufgende Modifier.
    */
   public MY_TYPE addDefaultModifier(IDataSetModifier... modifiers)
   {
     if (modifiers != null)
     {
       for (IDataSetModifier aModifier : modifiers)
       {
         fDefaultModifierList.add(aModifier);
       }
     }
     return myself();
   }
 
   /**
    * Methode zum vergleichen eines Datasets mit der aktuellen
    * Datenbank
    *
    * @param expectedDataSet
    * @param modifiers
    * @throws Exception
    */
   public void assertDataBase(IDataSet expectedDataSet, IDataSetModifier... modifiers) throws Exception
   {
     DBAssertion.assertDataSet(createDatabaseSnapshot(), expectedDataSet, getModifiers(modifiers));
   }
 
   /**
    * Methode zum vergleichen eines Datasets mit der aktuellen
    * Datenbank
    *
    * @param expectedDataSet
    * @param modifiers
    * @throws Exception
    */
   public void assertDataBase(DbUnitDatasetFactory factory, IDataSetModifier... modifiers) throws Exception
   {
     DBAssertion.assertDataSet(createDatabaseSnapshot(), factory.createDBUnitDataSet(), getModifiers(modifiers));
   }
 
   public void assertDataBaseSorted(DbUnitDatasetFactory factory, SortConfig[] config, IDataSetModifier... modifiers)
       throws Exception
   {
     assertDataBaseSorted(factory.createDBUnitDataSet(), config, modifiers);
   }
 
   public void assertDataBaseSorted(IDataSet expectedDataSet, SortConfig[] config, IDataSetModifier... modifiers)
       throws Exception
   {
     DBAssertion.assertDataSet(sortTables(createDatabaseSnapshot(), config), sortTables(expectedDataSet, config),
         getModifiers(modifiers));
   }
 
   public void assertDataBaseSorted(DbUnitDatasetFactory factory, IDataSetModifier... modifiers) throws Exception
   {
     assertDataBaseSorted(factory.createDBUnitDataSet(), modifiers);
   }
 
   public void assertDataBaseSorted(IDataSet expectedDataSet, IDataSetModifier... modifiers) throws Exception
   {
     DBAssertion.assertDataSet(true, createDatabaseSnapshot(), expectedDataSet, getModifiers(modifiers));
   }
 
   public IDataSet sortTables(IDataSet ds, SortConfig[] config)
   {
     if (config == null)
     {
       return ds;
     }
     DefaultDataSet dataSet = new DefaultDataSet();
     try
     {
       for (String tableName : ds.getTableNames())
       {
         SortConfig sortConfig = null;
         for (SortConfig cfg : config)
         {
           if (cfg.getTablename().equalsIgnoreCase(tableName))
           {
             sortConfig = cfg;
           }
         }
         ITable table = ds.getTable(tableName);
         ITable wrapped;
         if (sortConfig == null)
         {
           wrapped = createSortTable(table, null);
         }
         else
         {
           wrapped = createSortTable(table, sortConfig.getColumnOrder());
         }
         dataSet.addTable(wrapped);
       }
     }
     catch (DataSetException e)
     {
       throw new RuntimeException(e);
     }
 
     return dataSet;
   }
 
   protected ITable createSortTable(ITable table, String[] columnSortOrder) throws DataSetException
   {
     SortedTable table2;
     if (columnSortOrder == null)
     {
       table2 = new SortedTable(table);
     }
     else
     {
       table2 = new SortedTable(table, columnSortOrder);
     }
     table2.setUseComparable(true);
     return table2;
   }
 
   /**
    * Vergleich den Aktuellen Zustand der Datenbank mit der gegebenen
    * XML Datei. Hauptschlich eine convenient Methode. Siehe fr
    * weitere Datenbank assert Methoden {@link DBAssertion}.
    *
    * @param xmlFileRelativToClass XML Datei relativ zum Package. Wenn
    *        ein Prefix ber {@link #setClassPathPrefix(String)}
    *        gesetzt wurde, dann wird dies vorrausgestellt.
    *
    * @param modifiers Optimale Liste and {@link IDataSetModifier},
    *        welche vor dem Assert den Inhalt des geladenen DataSets
    *        manipulieren knnen.
    *
    * @throws Exception Fehler beim laden oder Vergleichen
    */
   public void assertDataBase(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     IDataSet expectedDS = getDataSet(xmlFileRelativToClass, getModifiers(modifiers));
     DBAssertion.assertDataSet(createDatabaseSnapshot(), expectedDS, modifiers);
   }
 
   public void assertDataBaseSorted(String xmlFileRelativToClass, SortConfig[] config, IDataSetModifier... modifiers)
       throws Exception
   {
     IDataSet expectedDS = getDataSet(xmlFileRelativToClass, getModifiers(modifiers));
     DBAssertion.assertDataSet(sortTables(createDatabaseSnapshot(), config), sortTables(expectedDS, config), modifiers);
   }
 
   /**
    * Vergleicht die Datenbank mit dem DataSet welches als letztes ber
    * die cleanInsert Methode eingefgt wurde. Abgeleitete Klassen
    * knnten den letzten Datensatz aber evlt. auch gendert haben
    * durch einen Aufruf von {@link #setLastInsertedDataSet(IDataSet)}
    *
    * @throws Exception Fehler im Vergleich
    */
   public void assertDataBaseStillTheSame() throws Exception
   {
     assertDataBaseStillTheSame(null);
   }
 
   /**
    * Vergleicht die Datenbank mit dem DataSet welches als letztes ber
    * die cleanInsert Methode eingefgt wurde. Abgeleitete Klassen
    * knnten den letzten Datensatz aber evlt. auch gendert haben
    * durch einen Aufruf von {@link #setLastInsertedDataSet(IDataSet)}
    *
    * @throws Exception Fehler im Vergleich
    */
   public void assertDataBaseStillTheSame(SortConfig[] sortConfig) throws Exception
   {
     if (!fKeepLastInsertedDataSet)
     {
       throw new AssertionError("Didn't keep the old dataset. Unable to compare!");
     }
     IDataSet lastDS = getLastInsertedDataSet();
     if (lastDS == null)
     {
       throw new AssertionError("Last dataset was null. Did you call a insert first?");
     }
     if (sortConfig == null)
     {
       assertDataBaseSorted(lastDS);
     }
     else
     {
       assertDataBaseSorted(lastDS, sortConfig);
     }
   }
 
   /**
    * Hilfsmethode welche das Laden des DataSets und ein CLEAN_INSERT
    * wrappt.
    *
    * @param datasetFactry This creates the actual dataset on the fly
    *
    * @param modifiers Zustzlich werden die Default-modifier zu
    * @throws Exception
    */
   public void prepare(DbUnitDatasetFactory datasetFactory, DatabaseOperation operation, IDataSetModifier... modifiers)
       throws Exception
   {
     prepare(datasetFactory.createDBUnitDataSet(), operation, modifiers);
   }
 
   /**
    * Hilfsmethode welche das Laden des DataSets und ein CLEAN_INSERT
    * wrappt.
    *
    * @param dataset
    *
    * @param modifiers Zustzlich werden die Default-modifier zu
    * @throws Exception
    */
   public void prepare(IDataSet dataset, DatabaseOperation operation, IDataSetModifier... modifiers) throws Exception
   {
     IDataSet loadedDS = DataSetUtil.modifyDataSet(dataset, getModifiers(modifiers));
 
     setLastInsertedDataSet(loadedDS);
 
     operation.execute(getConnection(), loadedDS);
   }
 
   /**
    * Hilfsmethode welche das Laden des DataSets und ein CLEAN_INSERT
    * wrappt.
    *
    * @param dataset
    *
    * @param modifiers Zustzlich werden die Default-modifier zu
    * @throws Exception
    */
   public void cleanInsert(IDataSet dataset, IDataSetModifier... modifiers) throws Exception
   {
     doCleanAction(dataset);
     prepare(dataset, getOperationFactory().cleanInsertOperation(), modifiers);
     doPrepareAction(dataset);
     trySetAnnotatedField(dataset);
   }
 
   /**
    * Hilfsmethode welche das Laden des DataSets und ein CLEAN_INSERT
    * wrappt.
    *
    * @param datasetFactory Factory welche das eigentliche Dataset
    *        zurckliefert
    *
    * @param modifiers Zustzlich werden die Default-modifier zu
    * @throws Exception
    */
   public void cleanInsert(DbUnitDatasetFactory datasetFactory, IDataSetModifier... modifiers) throws Exception
   {
     if (datasetFactory instanceof DataSetIdentificator)
     {
       DataSetIdentificator scope = (DataSetIdentificator) datasetFactory;
       DataSetRegistry.use(scope);
     }
     cleanInsert(datasetFactory.createDBUnitDataSet(), modifiers);
     trySetAnnotatedField(datasetFactory);
   }
 
   /**
    * Util method for truncate all data set tables.
    * @param dataset1 the dataset Factory which create the actual
    *        Dataset for the truncate.
    * @throws Exception
    */
   public void truncate(DbUnitDatasetFactory datasetFactory) throws Exception
   {
     truncate(datasetFactory.createDBUnitDataSet());
     trySetAnnotatedField(datasetFactory);
   }
 
   /**
    * Util method for truncate all data set tables.
    * @param dataset the dataset for the truncate.
    * @throws Exception
    */
   public void truncate(IDataSet dataset) throws Exception
   {
     doCleanAction(dataset);
     prepare(dataset, getOperationFactory().truncateOperation());
     doPrepareAction(dataset);
     trySetAnnotatedField(dataset);
   }
 
   public void cleanInsert(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     IDataSet loadedDS = getDataSet(xmlFileRelativToClass, getModifiers(modifiers));
     doCleanAction(loadedDS);
     setLastInsertedDataSet(loadedDS);
     prepare(loadedDS, getOperationFactory().cleanInsertOperation(), modifiers);
     trySetAnnotatedField(loadedDS);
   }
 
   public void insert(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     IDataSet loadedDS = getDataSet(xmlFileRelativToClass, getModifiers(modifiers));
     doCleanAction(loadedDS);
     setLastInsertedDataSet(loadedDS);
     prepare(loadedDS, getOperationFactory().insertOperation(), modifiers);
     trySetAnnotatedField(loadedDS);
   }
 
   public void truncate(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     IDataSet loadedDS = getDataSet(xmlFileRelativToClass, getModifiers(modifiers));
     doCleanAction(loadedDS);
     setLastInsertedDataSet(loadedDS);
     prepare(loadedDS, getOperationFactory().truncateOperation(), modifiers);
     trySetAnnotatedField(loadedDS);
   }
 
   public void truncateAndInsert(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     truncate(xmlFileRelativToClass, modifiers);
     insert(xmlFileRelativToClass, modifiers);
   }
 
   /**
    * Schliet die Verbindung zur Datenbank
    *
    * @throws Exception Fehler die beim Schlieen aufgetreten sind.
    */
   public void close() throws Exception
   {
     if (fDatabaseConnection != null)
     {
       fDatabaseConnection.close();
     }
     fDatabaseConnection = null;
   }
 
   /**
    * Erzeugt ein DataSet mit einem live Abzug der Kompletten Datenbank
    *
    * @return Der Datenbank-dump
    */
   public IDataSet createDatabaseSnapshot() throws Exception
   {
     return getConnection().createDataSet();
   }
 
   /**
    * Liefert die Verbindung zur Datenbank oder eine Exception
    *
    * @return Die Verbindung zur Datenbank
    *
    * @throws ClassNotFoundException Datenbank-Treiber konnte nicht
    *         geladen werden.
    *
    * @throws SQLException Fehler beim Verbinden zur Datenbank
    * @throws DatabaseUnitException
    */
   public IDatabaseConnection getConnection() throws ClassNotFoundException, SQLException, DatabaseUnitException
   {
     if (fDatabaseConnection != null)
     {
       return fDatabaseConnection;
     }
     if (fDriverName != null)
     {
       Class.forName(fDriverName);
     }
     if (_lazySource == null)
     {
       Connection dConn = DriverManager.getConnection(fUrl, fUsername, fPassword);
       return setConnection(dConn, fSchema);
     }
     return setConnection(getLazyDatasource());
   }
 
   protected DataSource getLazyDatasource()
   {
     return _lazySource.getFuture();
   }
 
   public IDatabaseConnection setConnection(DataSource dataSource)
   {
     try
     {
       Connection dconn = null;
       if (dataSource instanceof BasicDataSource)
       {
         ((BasicDataSource) dataSource).setAccessToUnderlyingConnectionAllowed(true);
         dconn = ((DelegatingConnection) dataSource.getConnection()).getInnermostDelegate();
         if (null == dconn)
         {
           dconn = ((DelegatingConnection) dataSource.getConnection()).getDelegate();
         }
       }
       if (null == dconn)
       {
         dconn = dataSource.getConnection();
       }
       if (null == dconn)
       {
         throw new RuntimeException("Unable to get Connection from datasource");
       }
       String schema = dconn.getCatalog();
       if (schema == null)
       {
         schema = fSchema;
       }
       return setConnection(dconn, schema);
     }
     catch (SQLException e)
     {
       throw new RuntimeException(e);
     }
     catch (DatabaseUnitException e)
     {
       throw new RuntimeException(e);
     }
   }
 
   protected IDatabaseConnection setConnection(Connection conn)
   {
     closeSilent();
     try
     {
       fDatabaseConnection = new DatabaseConnection(conn);
     }
     catch (DatabaseUnitException e)
     {
       throw new RuntimeException(e);
     }
     registerTypeFactory(fDatabaseConnection);
     return fDatabaseConnection;
   }
 
   public IDatabaseConnection setConnection(Connection dconn, String schema) throws DatabaseUnitException
   {
     closeSilent();
     fDatabaseConnection = new DatabaseConnection(dconn, schema);
     registerTypeFactory(fDatabaseConnection);
     registerFeatures(fDatabaseConnection);
     registerProperties(fDatabaseConnection);
     return fDatabaseConnection;
   }
 
   protected void closeSilent()
   {
     try
     {
       close();
     }
     catch (Exception e)
     {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * Ldt das DataSet aus der gegebenen XML Datei relativ zum Package.
    *
    * @param xmlFileRelativToClass Dateiname. Zustzlich wird das
    *        optimale {@link #setClassPathPrefix(String)} davor
    *        geschrieben.
    *
    * @param modifiers Optimale Liste an Modifiern die das geladene
    *        Dataset manipulieren. Zustzlich werden optimale durch
    *        {@link #addDefaultModifier(IDataSetModifier)} hinzugefgte
    *        'globale' Modifier ausgefhrt.
    *
    * @return Das DataSet aus der XML Datei. Evtl. verndert durch die
    *         modifiers.
    *
    * @throws Exception Fehler beim Laden oder Ausfhren der Modifier.
    */
   public IDataSet getDataSet(String xmlFileRelativToClass, IDataSetModifier... modifiers) throws Exception
   {
     return DataSetUtil.getDataSetFromClasspath(getClazz(), fPrefixClasspath + xmlFileRelativToClass,
         getModifiers(modifiers));
   }
 
   /**
    * Hilfs-Methode fr das als letztes eingespiele DataSet
    *
    * @return Liefert das als letztes durch
    *         {@link #cleanInsert(IDataSet, IDataSetModifier...)}
    *         eingespielte DataSet. Oder {@code null} falls noch kein
    *         DataSet eingespielt wurde.
    */
   public IDataSet getLastInsertedDataSet()
   {
     return fLastInsertedDataSet;
   }
 
   /**
    * Methoden zum Zugriff auf die default-Modifier.
    *
    * @param modifiers Optimale zustzliche Liste and Modifiern
    *
    * @return Liefert die optimal bergebenen Modifier sowie die evtl.
    *         gesetzen Default Modifier als Array zurck.
    */
   public IDataSetModifier[] getModifiers(IDataSetModifier... modifiers)
   {
     if (fDefaultModifierList == null)
     {
       return modifiers;
     }
     List<IDataSetModifier> list = new ArrayList<IDataSetModifier>();
     list.addAll(fDefaultModifierList);
     if (modifiers != null)
     {
       list.addAll(Arrays.asList(modifiers));
     }
     return list.toArray(new IDataSetModifier[] {});
   }
 
   /**
    * Setzt ein Prefix vor alle Datei-Ladeoperationen
    *
    * @param prefixForClasspath Das Prefix. Wird vor jeden Dateinaben
    *        gehngt. Kann aber auch aus einem Pfad
    *        ("Test-02-resources/") bestehen. Wird {@code null}
    *        bergeben wird das Prefix ignoriert.
    */
   public MY_TYPE setClassPathPrefix(String prefixForClasspath)
   {
     fPrefixClasspath = prefixForClasspath;
     if (fPrefixClasspath == null)
     {
       fPrefixClasspath = "";
     }
     return myself();
   }
 
   protected Class<?> getClazz()
   {
     if (fClazz == null)
     {
       throw new IllegalStateException("Das Feld fClass ist null. "
           + "Vermutlich wurde dem Konstrkutor keine Class Instanz bergeben.");
     }
     return fClazz;
   }
 
   /**
    * Versucht die Class-Instanz zu finden von der Aufrufenden Klasse.
    * <p>
    * Hierzu wird der Stack durchlaufen bis eine nicht von
    * DatabaseTester abgeleitete Klasse gefunden wird. Daher schlgt
    * dieser Code fehl wenn die Test-Klasse direkt oder indirekt von
    * DatabaseTester abgeleitet ist!
    * </p>
    */
   protected void doMagicClazzFind()
   {
     Class<?> clazz = getCallerClassViaMagic();
     setClazz(clazz);
   }
 
   protected static boolean isSubclassFrom(Class<?> thisClazz, Class<?> isOfSubclass)
   {
     try
     {
       thisClazz.asSubclass(isOfSubclass);
       return true;
     }
     catch (ClassCastException cl)
     {
       return false;
     }
   }
 
   /**
    * Setzen der Clazz Instanz fr das Detektieren das Ziel-Package
    * Verzeichnisses
    *
    * @param clazz Die Class Instanz
    */
   protected void setClazz(Class<?> clazz)
   {
     fClazz = clazz;
   }
 
   /**
    * Setter
    *
    * @param loadedDS die DataSet Instanz des als letzten
    *        eingepspielten DataSets
    */
   protected void setLastInsertedDataSet(IDataSet loadedDS) throws Exception
   {
     if (fKeepLastInsertedDataSet)
     {
       fLastInsertedDataSet = loadedDS;
     }
     else
     {
       fLastInsertedDataSet = null;
     }
   }
 
   /**
    * Clear, in Case Dataset is SOO big
    */
   protected void clearLastInsertedDataSet()
   {
     fLastInsertedDataSet = null;
   }
 
   public void dumpDatabase(String fileName) throws Exception
   {
     DataSetUtil.dumpDatabase(this, fileName);
   }
 
   /**
    * Vergleich den Aktuellen Zustand der Datenbank Tablle mit der
    * gegebenen XML Datei. Hauptschlich eine convenient Methode. Siehe
    * fr weitere Datenbank assert Methoden {@link DBAssertion}.
    *
    * @param xmlFileRelativToClass
    * @param tableName
    * @param modifiers
    * @throws Exception
    */
   public void assertTable(String xmlFileRelativToClass, String tableName, IDataSetModifier... modifiers)
       throws Exception
   {
     IDataSet dataset = getDataSet(xmlFileRelativToClass, modifiers);
     DBAssertion.assertTable(this.getConnection(), tableName, dataset);
   }
 
   protected void registerTypeFactory(IDatabaseConnection databaseConnection)
   {
     if (_registerTypeFactory != null && databaseConnection != null)
     {
       DatabaseConfig config = databaseConnection.getConfig();
       config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, _registerTypeFactory);
     }
   }
 
   protected void registerFeatures(IDatabaseConnection databaseConnection)
   {
     for (Entry<String, Boolean> item : getDBUnitFeatures().entrySet())
     {
       String feature = item.getKey();
       boolean value = item.getValue();
       databaseConnection.getConfig().setFeature(feature, value);
     }
   }
 
   protected void registerProperties(IDatabaseConnection databaseConnection)
   {
     for (Entry<String, Object> item : getDBUnitProperties().entrySet())
     {
       String name = item.getKey();
       Object value = item.getValue();
       databaseConnection.getConfig().setProperty(name, value);
     }
   }
 
   public boolean isKeepLastInsertedDataSet()
   {
     return fKeepLastInsertedDataSet;
   }
 
   public MY_TYPE setKeepLastInsertedDataSet(boolean keepLastInsertedDataSet)
   {
     fKeepLastInsertedDataSet = keepLastInsertedDataSet;
     return myself();
   }
 
   public MY_TYPE setUseCaseSensitiveNames(boolean useCaseSensitiveNames)
   {
     setDbUnitFeature("http://www.dbunit.org/features/caseSensitiveTableNames", useCaseSensitiveNames);
     return myself();
   }
 
   /**
    * Wait until database table has at least n- new rows.
    * @param tableName
    * @param minNewRowCount
    * @return the row count at the time the inner loop stopped.
    * @throws Exception
    */
   public int waitForNewRows(String tableName, int minNewRowCount) throws Exception
   {
     return waitForNewRows(tableName, minNewRowCount, 70);
   }
 
   /**
    * Wait until database table has at least n- new rows.
    * @param tableName
    * @param minNewRowCount
    * @return the row count at the time the inner loop stopped.
    * @throws Exception
    */
   public int waitForNewRows(String tableName, int minNewRowCount, int timeoutSeconds) throws Exception
   {
     long time = 0;
     long maxTime = timeoutSeconds * 1000;
     int progress = 0;
     while (true)
     {
       int count = getRowCount(tableName);
       if (count > minNewRowCount)
       {
         return count;
       }
       if (time > maxTime)
       {
         throw new AssertionError("timeout while waiting for new rows to appear in Table " + tableName);
       }
       Thread.sleep(250);
       time += 250;
       if (++progress % (4 * 15) == 0)
       {
         System.out.print(".");
       }
     }
   }
 
   /**
    * Get the actual row count of a table.
    *
    * @param tableName the table name
    * @return the count 0 or greater not null
    *
    * @throws Exception
    */
   public int getRowCount(String tableName) throws Exception
   {
     String table = tableName;
     if (fSchema != null && !tableName.contains("."))
     {
       table = fSchema + "." + tableName;
     }
     String sql = "select count(*) from " + table;
 
     return executeSQLandReturnInt(sql);
   }
 
   public int executeSQLandReturnInt(String sql) throws Exception
   {
     Connection connection = getConnection().getConnection();
     connection.setAutoCommit(false);
     try
     {
       final Statement statement = connection.createStatement();
       try
       {
         statement.execute(sql);
         final ResultSet set = statement.getResultSet();
         try
         {
           if (statement.getResultSet().next())
           {
             int count = set.getInt(1);
             return count;
           }
         }
         finally
         {
           if (set != null)
           {
             set.close();
           }
         }
       }
       finally
       {
         if (statement != null)
         {
           statement.close();
         }
       }
     }
     finally
     {
       connection.commit();
       connection.setAutoCommit(true);
     }
     throw new RuntimeException("got no result");
   }
 
   public boolean executeSQL(String sql) throws Exception
   {
     Connection connection = getConnection().getConnection();
     connection.setAutoCommit(false);
     boolean result = false;
     try
     {
       final Statement statement = connection.createStatement();
       try
       {
         result = statement.execute(sql);
       }
       finally
       {
         if (statement != null)
         {
           statement.close();
         }
       }
     }
     finally
     {
       connection.commit();
       connection.setAutoCommit(true);
     }
     return result;
   }
 
   public static synchronized Class<?> getCallerClassViaMagic()
   {
     Class<?> potentialClazz = null;
     try
     {
       StackTraceElement[] stackTrace = new Throwable().getStackTrace();
       ClassLoader cl = DatabaseTester.class.getClassLoader();
       for (StackTraceElement item : stackTrace)
       {
        Class<?> clazz = cl.loadClass(item.getClassName());
         if (!isSubclassFrom(clazz, DatabaseTesterBase.class))
         {
           if (potentialClazz == null)
           {
             potentialClazz = clazz;
           }
           else
           {
             if (isSubclassFrom(clazz, potentialClazz))
             {
               potentialClazz = clazz;
             }
             else
             {
               break;
             }
           }
         }
       }
     }
     catch (Throwable t)
     {
       // verschlucken
     }
     return potentialClazz;
   }
 
   public MY_TYPE setClassPathClass(Class<?> clazz)
   {
     fClazz = clazz;
     return myself();
   }
 
   public MY_TYPE addCleanAction(DatabaseTesterCleanAction action)
   {
     _cleanActions.add(action);
     return myself();
   }
 
   /**
    * Set a config feature on the underlying DBUnit. See
    * {@link DatabaseConfig} for possile features.
    *
    * @param feature feature id
    * @param value activate deactivate feature
    * @return this
    */
   public MY_TYPE setDbUnitFeature(String feature, boolean value)
   {
     getDBUnitFeatures().put(feature, value);
     return myself();
   }
 
   protected Map<String, Boolean> getDBUnitFeatures()
   {
     if (_dbUnitFeatures == null)
     {
       _dbUnitFeatures = new HashMap<String, Boolean>();
     }
     return _dbUnitFeatures;
   }
 
   /**
    * Set a config property on the underlying DBUnit. See
    * {@link DatabaseConfig} for possile features.
    *
    * @param name of the property
    * @param value activate deactivate feature
    * @return this
    */
   public MY_TYPE setDbUnitProperty(String name, Object value)
   {
     getDBUnitProperties().put(name, value);
     return myself();
   }
 
   protected Map<String, Object> getDBUnitProperties()
   {
     if (_dbUnitProperty == null)
     {
       _dbUnitProperty = new HashMap<String, Object>();
     }
     return _dbUnitProperty;
   }
 
   protected void doCleanAction(IDataSet dataset) throws Exception
   {
     if (_cleanActions == null)
     {
       return;
     }
     for (DatabaseTesterCleanAction action : _cleanActions)
     {
       action.doCleanDatabase(this, dataset);
     }
   }
 
   protected void doPrepareAction(IDataSet dataset) throws Exception
   {
     if (_cleanActions == null)
     {
       return;
     }
     for (DatabaseTesterCleanAction action : _cleanActions)
     {
       action.doPrepareDatabase(this, dataset);
     }
   }
 
   protected void trySetAnnotatedField(Object datasetFactory) throws Exception
   {
     // 2 implement
   }
 
   /**
    * Internal getter of the OperationFactory, used to create
    * @return the actual Factory or lazy initialize the factory as
    *         {@linkplain DefaultDbUnitDatabaseOperationFactory}.
    */
   protected DatabaseOperationFactory getOperationFactory()
   {
     if (_databaseOperationFactory == null)
     {
       _databaseOperationFactory = new DefaultDbUnitDatabaseOperationFactory();
     }
     return _databaseOperationFactory;
   }
 
   /** replace the DatabaseOperation Factory */
   public MY_TYPE setDatabaseOperationFactory(DatabaseOperationFactory databaseOperationFactory)
   {
     _databaseOperationFactory = databaseOperationFactory;
     return myself();
   }
 
   @SuppressWarnings("unchecked")
   protected MY_TYPE myself()
   {
     return (MY_TYPE) this;
   }
 
 }
