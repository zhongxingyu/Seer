 package org.kalibro.core.persistence;
 
 import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.kalibro.DatabaseSettings;
 import org.kalibro.KalibroSettings;
 import org.kalibro.dao.DaoFactory;
 import org.kalibro.dao.ProcessingNotificationDao;
 
 /**
  * Factory of data access objects that read and write on the database.
  * 
  * @author Carlos Morais
  */
 public class DatabaseDaoFactory extends DaoFactory {
 
 	private static DatabaseSettings currentSettings;
 	private static EntityManagerFactory entityManagerFactory;
 
 	static RecordManager createRecordManager() {
		if (entityManagerFactory == null || !entityManagerFactory.isOpen())
 			updateSettings(currentSettings);
 		return new RecordManager(entityManagerFactory.createEntityManager());
 	}
 
 	private static void updateSettings(DatabaseSettings settings) {
 		currentSettings = settings;
 		Map<String, String> properties = new HashMap<String, String>();
 		properties.put(DDL_GENERATION, NONE);
 		properties.put(JDBC_DRIVER, settings.getDatabaseType().getDriverClassName());
 		properties.put(JDBC_URL, settings.getJdbcUrl());
 		properties.put(JDBC_USER, settings.getUsername());
 		properties.put(JDBC_PASSWORD, settings.getPassword());
 		properties.put(LOGGING_LOGGER, PersistenceLogger.class.getName());
 		properties.put(SESSION_CUSTOMIZER, DatabaseImport.class.getName());
 		if (entityManagerFactory != null && entityManagerFactory.isOpen())
 			entityManagerFactory.close();
 		entityManagerFactory = Persistence.createEntityManagerFactory("Kalibro", properties);
 	}
 
 	public DatabaseDaoFactory() {
 		this(KalibroSettings.load().getServerSettings().getDatabaseSettings());
 	}
 
 	public DatabaseDaoFactory(DatabaseSettings settings) {
		if (!settings.deepEquals(currentSettings))
 			updateSettings(settings);
 	}
 
 	@Override
 	protected BaseToolDatabaseDao createBaseToolDao() {
 		return new BaseToolDatabaseDao();
 	}
 
 	@Override
 	public ConfigurationDatabaseDao createConfigurationDao() {
 		return new ConfigurationDatabaseDao();
 	}
 
 	@Override
 	protected MetricConfigurationDatabaseDao createMetricConfigurationDao() {
 		return new MetricConfigurationDatabaseDao();
 	}
 
 	@Override
 	public MetricResultDatabaseDao createMetricResultDao() {
 		return new MetricResultDatabaseDao();
 	}
 
 	@Override
 	public ModuleResultDatabaseDao createModuleResultDao() {
 		return new ModuleResultDatabaseDao();
 	}
 
 	@Override
 	public ProcessingDatabaseDao createProcessingDao() {
 		return new ProcessingDatabaseDao();
 	}
 
 	@Override
 	protected ProjectDatabaseDao createProjectDao() {
 		return new ProjectDatabaseDao();
 	}
 
 	@Override
 	protected RangeDatabaseDao createRangeDao() {
 		return new RangeDatabaseDao();
 	}
 
 	@Override
 	protected ReadingDatabaseDao createReadingDao() {
 		return new ReadingDatabaseDao();
 	}
 
 	@Override
 	protected ReadingGroupDatabaseDao createReadingGroupDao() {
 		return new ReadingGroupDatabaseDao();
 	}
 
 	@Override
 	protected RepositoryDatabaseDao createRepositoryDao() {
 		return new RepositoryDatabaseDao();
 	}
 
 	@Override
 	protected ProcessingNotificationDao createProcessingNotificationDao() {
		return ProcessingNotificationDatabaseDao();
 	}
 }
