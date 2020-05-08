 package com.mymed.controller.core.manager;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Logger;
 
 /**
 * Simple class to hold generic strings that can be used with all the managers.
 * We hold the names of the columns families and of the super columns.
 * <p>
 * It contains also the log facilities for all the managers.
  * 
  * @author Milo Casagrande
  * 
  */
 public class ManagerValues {
 	// Column families
 	/**
 	 * The 'Application' column family
 	 */
 	protected static final String CF_APPLICATION = "Application";
 
 	/**
 	 * The 'ApplicationView' column family
 	 */
 	protected static final String CF_APPLICATION_VIEW = "ApplicationView";
 
 	/**
 	 * The 'Authentication' column family
 	 */
 	protected static final String CF_AUTHENTICATION = "Authentication";
 
 	/**
 	 * The 'Interaction' column family
 	 */
 	protected static final String CF_INTERACTION = "Interaction";
 
 	/**
 	 * The 'Ontology' column family
 	 */
 	protected static final String CF_ONTOLOGY = "Ontology";
 
 	/**
 	 * The 'OntologyType' column family
 	 */
 	protected static final String CF_ONTOLOGY_TYPE = "OntologyType";
 
 	/**
 	 * The 'Reputation' column family
 	 */
 	protected static final String CF_REPUTATION = "Reputation";
 
 	/**
 	 * The 'Session' column family
 	 */
 	protected static final String CF_SESSION = "Session";
 
 	/**
 	 * The 'User' column family
 	 */
 	protected static final String CF_USER = "User";
 
 	// Super columns
 	/**
 	 * The 'ApplicationController' super column
 	 */
 	protected static final String SC_APPLICATION_CONTROLLER = "ApplicationController";
 
 	/**
 	 * The 'ApplicationList' super column
 	 */
 	protected static final String SC_APPLICATION_LIST = "ApplicationList";
 
 	/**
 	 * The 'ApplicationModel' super column
 	 */
 	protected static final String SC_APPLICATION_MODEL = "ApplicationModel";
 
 	/**
 	 * The 'UserList' super column
 	 */
 	protected static final String SC_USER_LIST = "UserList";
 
 	/**
 	 * The 'InteractionList' super column
 	 */
 	protected static final String SC_INTERACTION_LIST = "InteractionList";
 
 	/**
 	 * The 'RaterList' super column
 	 */
 	protected static final String SC_RATER_LIST = "RaterList";
 
 	// Logger facilities
 	/**
 	 * The name of the default info logger as defined in the file logback.xml
 	 */
 	private static final String INFO_LOG_NAME = "mymed.info.logger";
 
 	/**
 	 * The name of the default debug logger as defined in the file logback.xml
 	 */
 	private static final String DEBUG_LOG_NAME = "mymed.debug.logger";
 
 	/**
 	 * The default info logger to be used throughout all the managers
 	 */
 	protected static final Logger INFO_LOGGER = (Logger) LoggerFactory.getLogger(INFO_LOG_NAME);
 
 	/**
 	 * The default debug logger to be used throughout all the managers
 	 */
 	protected static final Logger DEBUG_LOGGER = (Logger) LoggerFactory.getLogger(DEBUG_LOG_NAME);
 }
