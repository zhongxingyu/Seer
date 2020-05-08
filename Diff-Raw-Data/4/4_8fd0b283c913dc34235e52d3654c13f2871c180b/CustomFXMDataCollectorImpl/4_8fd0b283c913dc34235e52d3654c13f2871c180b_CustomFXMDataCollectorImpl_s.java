 /*
   Copyright 2012 Quest Software, Inc.
   ALL RIGHTS RESERVED.
 
   This software is the confidential and proprietary information of
   Quest Software Inc. ("Confidential Information").  You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered
   into with Quest Software Inc.
 
   QUEST SOFTWARE INC. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
   THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
   INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
   NON-INFRINGEMENT. QUEST SOFTWARE SHALL NOT BE LIABLE FOR ANY
   DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  */
 
 package de.quest.pso.fxm.agent.customCollector;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import de.quest.pso.fxm.agent.customCollector.samples.*;
 
 import com.quest.glue.api.services.*;
 import com.quest.glue.api.services.TopologyDataSubmissionService3.TopologySubmitter3;
 
 /**
  * The core implementation class of the CustomFXMDataCollector agent.
  */
 public class CustomFXMDataCollectorImpl implements
 		com.quest.glue.api.agent.Agent, CustomFXMDataCollectorCollectors,
 
 		ASPPropertyListener {
 
 	public static final String FXM_CUSTOM_MODEL_NAME = "FXMCustomModel";
 	private final LogService.Logger mLogger;
 	private final RegistrationService mRegistrationService;
 	private final CustomFXMDataCollectorSupportBundle mBundle;
 
 	private final CustomFXMDataCollectorDataProvider mDataProvider;
 	private CustomFXMDataCollectorPropertyWrapper mProperties;
 	private TopologySubmitter3 mSubmitter;
 	private UnitService mUnitService;
 	private TimestampService mTimestampService;
 
 	/**
 	 * Called by FglAM to create a new instance of this agent. This constructor
 	 * is required by FglAM and must be present.
 	 * 
 	 * @param serviceFactory
 	 *            Factory used to create services for this agent
 	 */
 	public CustomFXMDataCollectorImpl(ServiceFactory serviceFactory)
 			throws ServiceFactory.ServiceNotAvailableException {
 		this(serviceFactory, new CustomFXMDataCollectorDataProviderImpl(
 				serviceFactory));
 	}
 
 	/**
 	 * Creates a new instance of this agent using to provided class to collect
 	 * all necessary data for submission. This allows the data provided to be
 	 * swapped out or mocked up during unit tests.
 	 * <p/>
 	 * This is an example of one possible way to structure the agent, but it is
 	 * not the only way. You are free to change or remove this constructor as
 	 * you see fit.
 	 * 
 	 * @param serviceFactory
 	 *            Factory used to create services for this agent
 	 * @param dataProvider
 	 *            The class to use to obtain all data for submission.
 	 */
 	/* pkg */CustomFXMDataCollectorImpl(ServiceFactory serviceFactory,
 			CustomFXMDataCollectorDataProvider dataProvider)
 			throws ServiceFactory.ServiceNotAvailableException {
 		LogService logService = serviceFactory.getService(LogService.class);
 		mLogger = logService.getLogger(CustomFXMDataCollectorImpl.class);
 
 		mDataProvider = dataProvider;
 
 		mRegistrationService = serviceFactory
 				.getService(RegistrationService.class);
 		// This will automatically register all the service-related listeners
 		// implemented by this class.
 		mRegistrationService.registerAllListeners(this);
 
 		// This hooks the CustomFXMDataCollector up to the support bundle
 		// framework and
 		// and allows it to contribute information to the support bundle.
 		mBundle = new CustomFXMDataCollectorSupportBundle(this);
 		mRegistrationService.registerAllListeners(mBundle);
 		mProperties = new CustomFXMDataCollectorPropertyWrapper(
 				serviceFactory.getService(ASPService3.class));
 		
 		mSubmitter = serviceFactory.getService(TopologyDataSubmissionService3.class).getTopologySubmitter();
 		mUnitService = serviceFactory.getService(UnitService.class);
 
 		mTimestampService = serviceFactory.getService(TimestampService.class);
 		
 		// Log some basic info to indicate that the agent has been created
 		mLogger.log("agentVersion", "CustomFXMDataCollector", "1.0.0");
 	}
 
 	/**
 	 * Called by FglAM at the end of the agent's life
 	 */
 	@Override
 	public void destroy() {
 		mRegistrationService.unregisterAllListeners(this);
 		mRegistrationService.unregisterAllListeners(mBundle);
 	}
 
 	/**
 	 * Called by FglAM to begin data collection.
 	 * <p/>
 	 * Since there are data collector(s) defined for this agent, taking action
 	 * as a result of this method call is optional. Each data collector method
 	 * will be called by FglAM when it is scheduled.
 	 */
 	@Override
 	public void startDataCollection() {
 		mLogger.debug("Data collection started");
 
 	}
 
 	/**
 	 * Called by FglAM when data collection should be stopped.
 	 * <p/>
 	 * Since there are data collector(s) defined for this agent, taking action
 	 * as a result of this method call is optional.
 	 */
 	@Override
 	public void stopDataCollection() {
 		mLogger.debug("Stopping data collection");
 	}
 
 	/**
 	 * Respond to property changes.
 	 * <p/>
 	 * This method is part of the ASPPropertyListener interface and is not
 	 * required by agents that do not implement it.
 	 * <p/>
 	 * Agents can receive property changes while they are running, and it is up
 	 * to the agent developer to ensure that modifications do not break the
 	 * agent.
 	 */
 	public void propertiesChanged() {
 		mLogger.debug("Property change notification received");
 	}
 
 	/**
 	 * Collect Frequent Downloads Data Collector
 	 * 
 	 * @param collectionFreqInMs
 	 *            the collection frequency for this collector, in ms.
 	 */
 	@Override
 	public void collectFrequentDownloads(long collectionFreqInMs) {
 		mLogger.debug("Collect Frequent Downloads collector invoked");
 		// This will load the MySQL driver, each DB has its own driver
 		mLogger.debug("SampleFreq:" + collectionFreqInMs);
 		Connection connect = null;
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			// Setup the connection with the DB
 			connect = DriverManager.getConnection("jdbc:mysql://"
 					+ mProperties.getFxmHostname() + ":"
 					+ mProperties.getFxmPort() + "/UserSession?" + "user="
 					+ mProperties.getUserName() + "&password="
 					+ mProperties.getPassword());
 
 			mLogger.debug("Connection established !");
 
 			// Statements allow to issue SQL queries to the database
 			Statement statement = connect.createStatement();
 			// Result set get the result of the SQL query
 			// .executeQuery("select s.ClientIP saugerip, u.Count anzahl from Session s, UserSessionValueCount u where s.ResourceID = u.ResourceID and s.UserAgent not like '%bot%' and u.Count > 1000 order by u.Count desc limit 20;");
 
 			PreparedStatement preparedStatement = connect
 					.prepareStatement("select s.ClientIP saugerip, u.Count anzahl "
 							+ "from Session s, UserSessionValueCount u "
 							+ "where s.ResourceID = u.ResourceID and s.UserAgent not like '%bot%' and u.Count > ? "
 							+ "order by u.Count desc limit ?;");
 
 			preparedStatement.setInt(1, mProperties.getLowLimit());
 			preparedStatement.setInt(2, mProperties.getNumSessions());
 			ResultSet resultSet = preparedStatement.executeQuery();
 
 			mLogger.debug("Result :" + resultSet.toString());
 
			CustomFXMCollection collection = new CustomFXMCollection(mProperties.getFxmHostname(), new CustomFXMCollectionRoot(FXM_CUSTOM_MODEL_NAME));
 
 			CustomFXMFrequentHitSessions frequentHits = new CustomFXMFrequentHitSessions(collection);
 			collection.setFrequentHits(frequentHits );
 			
 		 List<TopHitSessionsEntry> sessions = frequentHits.getTopHitSessions();
 			
 			resultSet.beforeFirst();
 			int i = 1;
 			while (resultSet.next()) {
 				System.out.println(i + " : " + resultSet.getString(1)
 						+ "   ,   " + resultSet.getString(2));
 				i++;
 			
 				TopHitSessionsEntry entry = new TopHitSessionsEntry(resultSet.getString(1));
 				entry.setHits(resultSet.getLong(2));
 				sessions.add(entry);
 			}
 			
 				collection.submit(mSubmitter, mUnitService, collectionFreqInMs, mTimestampService.getCorrectedTimestamp().getTimeInMillis());
 			connect.close();
 		} catch (ClassNotFoundException e) {
 			mLogger.errorUnexpected("noDriver", e);
 		} catch (SQLException e) {
 			mLogger.errorUnexpected("sqlException", e);
 
 		} catch (TopologyException e) {
 			// TODO Auto-generated catch block
 			mLogger.errorUnexpected("TopologyException", e);
 
 		} finally {
 			if (connect != null)
 				try {
 					connect.close();
 				} catch (SQLException e) {
 
 				}
 		}
 	}
 
 }
