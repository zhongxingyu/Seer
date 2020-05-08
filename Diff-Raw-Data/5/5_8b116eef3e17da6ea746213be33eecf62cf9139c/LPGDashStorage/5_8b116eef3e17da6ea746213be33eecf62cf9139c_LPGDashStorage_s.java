 package uk.ac.imperial.lpgdash.db;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import uk.ac.imperial.lpgdash.LPGService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.db.sql.Agent;
 import uk.ac.imperial.presage2.db.sql.Environment;
 import uk.ac.imperial.presage2.db.sql.SqlStorage;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 public class LPGDashStorage extends SqlStorage {
 
 	int maxRound = -1;
 
 	boolean shutdown = false;
 
 	private LPGService game = null;
 
 	@Inject
 	public LPGDashStorage(@Named(value = "sql.info") Properties jdbcInfo) {
 		super(jdbcInfo);
 	}
 
 	@Inject(optional = true)
 	public void setGame(EnvironmentServiceProvider serviceProvider) {
 		try {
 			this.game = serviceProvider.getEnvironmentService(LPGService.class);
 		} catch (UnavailableServiceException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	protected void initTables() {
 		super.initTables();
 		Statement createTables = null;
 		try {
 			createTables = conn.createStatement();
 			createTables.execute("CREATE TABLE IF NOT EXISTS `playerScore` ("
					+ "`simID` int(11) NOT NULL,"
 					+ "`player` varchar(10) NOT NULL,"
 					+ "`round` int(11) NOT NULL," + "`g` double NOT NULL,"
 					+ "`q` double NOT NULL," + "`d` double NOT NULL,"
 					+ "`p` double NOT NULL," + "`r` double NOT NULL,"
 					+ "`rP` double NOT NULL," + "`rTotal` double NOT NULL,"
 					+ "`satisfaction` double NOT NULL,"
 					+ "`U` double NOT NULL,"
 					+ "PRIMARY KEY (`simID`,`player`,`round`),"
 					+ "KEY `simID` (`simID`)," + "KEY `player` (`player`),"
 					+ "KEY `round` (`round`)" + ")");
 
 			createTables.execute("CREATE TABLE IF NOT EXISTS `roundGlobals` ("
					+ "`simID` int(11) NOT NULL," + "`round` int(11) NOT NULL,"
 					+ "`fairness` double NOT NULL,"
 					+ "`w_f1` double DEFAULT NULL,"
 					+ "`w_f1a` double DEFAULT NULL,"
 					+ "`w_f2` double DEFAULT NULL,"
 					+ "`w_f3` double DEFAULT NULL,"
 					+ "`w_f4` double DEFAULT NULL,"
 					+ "`w_f5` double DEFAULT NULL,"
 					+ "`w_f6` double DEFAULT NULL,"
 					+ "`w_f7` double DEFAULT NULL,"
 					+ "PRIMARY KEY (`simID`,`round`)" + ")");
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 			throw new RuntimeException(e);
 		} finally {
 			if (createTables != null) {
 				try {
 					createTables.close();
 				} catch (SQLException e) {
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void updateTransientEnvironment() {
 		PreparedStatement insertRound = null;
 
 		try {
 			insertRound = conn
 					.prepareStatement("INSERT INTO roundGlobals "
 							+ "(simID, round, fairness, w_f1, w_f1a, w_f2, w_f3, w_f4, w_f5, w_f6, w_f7)"
 							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
 		} catch (SQLException e) {
 			logger.warn(e);
 			throw new RuntimeException(e);
 		}
 
 		try {
 			Set<Environment> notfullyProcessed = new HashSet<Environment>();
 			for (Environment e : environmentTransientQ) {
 				List<Integer> forRemoval = new LinkedList<Integer>();
 				for (Map.Entry<Integer, Map<String, String>> round : e.transientProperties
 						.entrySet()) {
 					if (!shutdown && game != null
 							&& round.getKey() >= game.getRoundNumber() - 1) {
 						notfullyProcessed.add(e);
 						continue;
 					}
 
 					Map<String, String> props = round.getValue();
 
 					insertRound.setLong(1, e.simId);
 					insertRound.setInt(2, round.getKey() - 1);
 					insertRound.setDouble(3,
 							getProperty(props, "c0-fairness", 0));
 
 					String[] canons = { "w_F1", "w_F1a", "w_F2", "w_F3",
 							"w_F4", "w_F5", "w_F6", "w_F7" };
 					for (int i = 0; i < canons.length; i++) {
 						if (props.containsKey(canons[i]))
 							insertRound.setDouble(4 + i,
 									Double.parseDouble(props.get(canons[i])));
 						else
 							insertRound.setNull(4 + i, Types.DOUBLE);
 					}
 					insertRound.addBatch();
 
 					forRemoval.add(round.getKey());
 				}
 				for (Integer round : forRemoval) {
 					e.transientProperties.remove(round);
 				}
 			}
 			environmentTransientQ.clear();
 			environmentTransientQ.addAll(notfullyProcessed);
 			batchQueryQ.put(insertRound);
 		} catch (SQLException e) {
 			logger.warn(e);
 			throw new RuntimeException(e);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private double getProperty(Map<String, String> properties, String key,
 			double defaultValue) {
 		if (properties.containsKey(key))
 			return Double.parseDouble(properties.get(key));
 		else
 			return defaultValue;
 	}
 
 	@Override
 	protected void updateTransientAgents() {
 		PreparedStatement insertPlayer = null;
 		try {
 			insertPlayer = conn
 					.prepareStatement("INSERT INTO playerScore "
 							+ "(simID, player, round, g, q, d, p, r, rP, rTotal, satisfaction, U)  "
 							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
 		} catch (SQLException e) {
 			logger.warn(e);
 			throw new RuntimeException(e);
 		}
 
 		try {
 			Set<Agent> notfullyProcessed = new HashSet<Agent>();
 			for (Agent a : agentTransientQ) {
 				List<Integer> forRemoval = new LinkedList<Integer>();
 				for (Map.Entry<Integer, Map<String, String>> round : a.transientProperties
 						.entrySet()) {
 					if (!shutdown && game != null
 							&& round.getKey() >= game.getRoundNumber() - 2) {
 						notfullyProcessed.add(a);
 						continue;
 					}
 
 					Map<String, String> props = round.getValue();
 
 					if (!props.containsKey("g"))
 						continue;
 
 					insertPlayer.setLong(1, a.simId);
 					insertPlayer.setString(2, a.getName());
 					insertPlayer.setInt(3, round.getKey() - 1);
 
 					insertPlayer.setDouble(4, getProperty(props, "g", 0.0));
 					insertPlayer.setDouble(5, getProperty(props, "q", 0.0));
 					insertPlayer.setDouble(6, getProperty(props, "d", 0.0));
 					insertPlayer.setDouble(7, getProperty(props, "p", 0.0));
 					insertPlayer.setDouble(8, getProperty(props, "r", 0.0));
 					insertPlayer.setDouble(9, getProperty(props, "r'", 0.0));
 					insertPlayer.setDouble(10,
 							getProperty(props, "RTotal", 0.0));
 					insertPlayer.setDouble(11, getProperty(props, "o", 0.0));
 					insertPlayer.setDouble(12, getProperty(props, "U", 0.0));
 
 					insertPlayer.addBatch();
 
 					forRemoval.add(round.getKey());
 				}
 				for (Integer round : forRemoval) {
 					a.transientProperties.remove(round);
 				}
 			}
 			batchQueryQ.put(insertPlayer);
 			agentTransientQ.clear();
 			agentTransientQ.addAll(notfullyProcessed);
 		} catch (SQLException e) {
 			logger.warn(e);
 			throw new RuntimeException(e);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void stop() {
 		this.shutdown = true;
 		super.stop();
 	}
 
 }
