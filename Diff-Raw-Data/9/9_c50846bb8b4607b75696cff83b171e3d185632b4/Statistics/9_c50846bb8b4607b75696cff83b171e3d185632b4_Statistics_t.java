 package com.theminequest.bukkit.statistic;
 
 import java.io.File;
 import java.util.List;
 
 import com.alta189.simplesave.Database;
 import com.alta189.simplesave.DatabaseFactory;
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.alta189.simplesave.exceptions.TableRegistrationException;
 import com.alta189.simplesave.h2.H2Configuration;
 import com.alta189.simplesave.mysql.MySQLConfiguration;
 import com.alta189.simplesave.query.QueryResult;
 import com.alta189.simplesave.sqlite.SQLiteConfiguration;
 import com.theminequest.api.Managers;
 import com.theminequest.api.statistic.QuestStatistic;
 import com.theminequest.api.statistic.QuestStatisticManager;
 import com.theminequest.api.statistic.Statistic;
 import com.theminequest.api.util.PropertiesFile;
 
 public class Statistics implements QuestStatisticManager {
 	
 	private enum Mode {
 		MySQL, SQlite, H2;
 	}
 	
 	private Mode databasetype;
 	private Database backend;
 	
 	public Statistics() throws ConnectionException {
 		Managers.log("[SQL] Loading and connecting to SQL...");
 		
 		PropertiesFile config = Managers.getPlatform().getConfigurationFile();
 		
 		String dbtype = config.getString("statistics.type", "h2");
 		if (dbtype.equalsIgnoreCase("mysql"))
 			databasetype = Mode.MySQL;
 		else if (dbtype.equalsIgnoreCase("sqlite"))
 			databasetype = Mode.SQlite;
 		else
 			databasetype = Mode.H2;
 		Managers.log("[SQL] Using " + databasetype.name() + " as database.");
 		String hostname = config.getString("statistics.host", "localhost");
 		String port = config.getString("statistics.port", "3306");
 		String databasename = config.getString("statistics.name", "minequest");
 		String username = config.getString("statistics.user", "root");
 		String password = config.getString("statistics.pass", "toor");
 		if (databasetype == Mode.MySQL)
 			backend = DatabaseFactory.createNewDatabase(new MySQLConfiguration().setHost(hostname).setDatabase(databasename).setPort(Integer.parseInt(port)).setUser(username).setPassword(password));
 		else if (databasetype == Mode.SQlite) {
 			SQLiteConfiguration s = new SQLiteConfiguration();
 			s.setPath(Managers.getPlatform().getResourceDirectory().getAbsolutePath() + File.separator + "minequest_sqlite");
 			backend = DatabaseFactory.createNewDatabase(s);
 		} else
 			backend = DatabaseFactory.createNewDatabase(new H2Configuration().setDatabase(Managers.getPlatform().getResourceDirectory().getAbsolutePath() + File.separator + "minequest_h2"));
		
		backend.setCheckTableOnRegistration(true);
 	}
 	
 	@Override
 	public Database getStorageBackend() {
 		return backend;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Statistic> List<T> getAllStatistics(String playerName, Class<? extends Statistic> tableClazz) {
 		List<? extends Statistic> result = backend.select(tableClazz).where().equal("playerName", playerName.toLowerCase()).execute().find();
 		for (Statistic s : result)
 			s.setup();
 		return (List<T>) result;
 	}
 	
 	@Override
 	public <T extends Statistic> void saveStatistic(T statistic, Class<? extends Statistic> tableClazz) {
 		backend.save(tableClazz, statistic);
 	}
 	
 	@Override
 	public void registerStatistic(Class<? extends Statistic> tableClazz) {
 		try {
 			backend.registerTable(tableClazz);
 		} catch (TableRegistrationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public void connect(boolean connect) throws ConnectionException {
 		if (connect)
 			backend.connect();
 		else
 			backend.close();
 	}
 	
 	@Override
 	public <T extends Statistic> List<T> getStatisticList(Class<? extends Statistic> tableClazz) {
 		QueryResult<? extends Statistic> r = backend.select(tableClazz).execute();
 		@SuppressWarnings("unchecked")
 		List<T> list = (List<T>) r.find();
 		return list;
 	}
 	
 	@Override
 	public <T extends Statistic> T createStatistic(String playerName, Class<? extends Statistic> tableClazz) {
 		try {
 			@SuppressWarnings("unchecked")
 			T s = (T) tableClazz.newInstance();
 			s.setPlayerName(playerName);
 			s.setup();
 			return s;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public <T extends Statistic> void removeStatistic(T statistic, Class<? extends Statistic> tableClazz) {
 		backend.remove(tableClazz, statistic);
 	}
 	
 	@Override
 	public <T extends QuestStatistic> T getQuestStatistic(String playerName, String questName, Class<? extends QuestStatistic> tableClazz) {
 		@SuppressWarnings("unchecked")
 		T s = (T) backend.select(tableClazz).where().equal("playerName", playerName.toLowerCase()).and().equal("questName", questName).execute().findOne();
 		if (s != null)
 			s.setup();
 		return s;
 	}
 	
 	@Override
 	public <T extends QuestStatistic> T createStatistic(String playerName, String questName, Class<? extends QuestStatistic> tableClazz) {
 		try {
 			@SuppressWarnings("unchecked")
 			T s = (T) tableClazz.newInstance();
 			s.setPlayerName(playerName);
 			s.setQuestName(questName);
 			s.setup();
 			return s;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 }
