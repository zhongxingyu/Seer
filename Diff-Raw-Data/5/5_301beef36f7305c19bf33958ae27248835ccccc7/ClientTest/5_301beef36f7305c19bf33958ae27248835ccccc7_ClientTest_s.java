 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.event.EventListenerList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.jubula.client.core.businessprocess.AbstractXMLReportGenerator;
 import org.eclipse.jubula.client.core.businessprocess.CompleteXMLReportGenerator;
 import org.eclipse.jubula.client.core.businessprocess.ErrorsOnlyXMLReportGenerator;
 import org.eclipse.jubula.client.core.businessprocess.FileXMLReportWriter;
 import org.eclipse.jubula.client.core.businessprocess.ITestExecutionEventListener;
 import org.eclipse.jubula.client.core.businessprocess.ITestresultSummaryEventListener;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IXMLReportWriter;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution.PauseMode;
 import org.eclipse.jubula.client.core.businessprocess.TestExecutionEvent;
 import org.eclipse.jubula.client.core.businessprocess.TestResultBP;
 import org.eclipse.jubula.client.core.businessprocess.TestresultSummaryBP;
 import org.eclipse.jubula.client.core.commands.AUTStartedCommand;
 import org.eclipse.jubula.client.core.commands.CAPRecordedCommand;
 import org.eclipse.jubula.client.core.commands.DisconnectFromAutAgentResponseCommand;
 import org.eclipse.jubula.client.core.commands.GetAutConfigMapResponseCommand;
 import org.eclipse.jubula.client.core.communication.AUTConnection;
 import org.eclipse.jubula.client.core.communication.BaseConnection;
 import org.eclipse.jubula.client.core.communication.BaseConnection.NotConnectedException;
 import org.eclipse.jubula.client.core.communication.ConnectionException;
 import org.eclipse.jubula.client.core.communication.ServerConnection;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.model.TestResult;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.TestResultPM;
 import org.eclipse.jubula.client.core.persistence.TestResultSummaryPM;
 import org.eclipse.jubula.communication.ICommand;
 import org.eclipse.jubula.communication.message.AUTStateMessage;
 import org.eclipse.jubula.communication.message.BuildMonitoringReportMessage;
 import org.eclipse.jubula.communication.message.ChangeAUTModeMessage;
 import org.eclipse.jubula.communication.message.DisconnectFromAutAgentMessage;
 import org.eclipse.jubula.communication.message.GetAutConfigMapMessage;
 import org.eclipse.jubula.communication.message.GetMonitoringDataMessage;
 import org.eclipse.jubula.communication.message.Message;
 import org.eclipse.jubula.communication.message.SendAUTListOfSupportedComponentsMessage;
 import org.eclipse.jubula.communication.message.StartAUTServerMessage;
 import org.eclipse.jubula.communication.message.StopAUTServerMessage;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.constants.MonitoringConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.ToolkitConstants;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBVersionException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.IMonitoringValue;
 import org.eclipse.jubula.tools.objects.MonitoringValue;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessprocess.ProfileBuilder;
 
 
 /**
  * This class contains methods for starting and stopping a test. It's also holds
  * the listener for AutStarterEvent, AUTServerEvent and AUTEvent.
  * 
  * @author BREDEX GmbH
  * @created 16.07.2004
  */
 public class ClientTest implements IClientTest {
     /** the logger */
     private static Log log = LogFactory.getLog(ClientTest.class);
 
     /** file extension for XML */
     private static final String XML_FILE_EXT = ".xml"; //$NON-NLS-1$
     
     /** used in filenames for reports for succeful tests */
     private static final String TEST_SUCCESSFUL = "ok"; //$NON-NLS-1$
     
     /** used in filenames for reports for failed tests */
     private static final String TEST_FAILED = "failed"; //$NON-NLS-1$
     /** timeout for report job, after this time the job will be canceled 
      * 1200000ms = 20min */
     private static final int BUILD_REPORT_TIMEOUT = 1200000;
     /** timeout for requesting AutConfigMap from Agent */
     private static final int REQUEST_CONFIG_MAP_TIMEOUT = 10000;
     /**
      * Timeout for connect to servicecomponent
      */
     //    private static final int TIMEOUT = 60;
     /** A list of listeners for all events*/
     private static EventListenerList eventListenerList = 
         new EventListenerList();
 
     
     /**
      * Time of test suite start
      */
     private Date m_testsuiteStartTime = null;
     
     /**
      * Time of test job start
      */
     private Date m_testjobStartTime = null;
     
     /**
      * Time of TestEnd
      */
     private Date m_endTime = null;
 
     /** log style (for example, Complete or Errors only) */
     private String m_logStyle = null;
 
     /**
      * log path for results
      */
     private String m_logPath = null;
 
     /** The test result summary */
     private ITestResultSummaryPO m_summary;   
     
     /**
      * <code>m_relevant</code> the relevant flag
      */
     private boolean m_relevant = true;
     
     /**
      * <code>m_pauseOnError</code>
      */
     private boolean m_pauseOnError = false;
 
     /**
      * empty default constructor
      */
     public ClientTest() {
         super();
         ComponentBuilder.getInstance().getCompSystem();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void connectToServer(String serverName, 
         String port) {
         
         try {
             if (!initServerConnection(serverName, port)) {
                 // *ServerEvnetListener are already notfied from 
                 // initConnections() 
                 fireAutStarterStateChanged(new AutStarterEvent(
                         AutStarterEvent.SERVER_CANNOT_CONNECTED));
                 return;
             }
         } catch (JBVersionException e) {
             fireAutStarterStateChanged(new AutStarterEvent(
                 AutStarterEvent.VERSION_ERROR));
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void disconnectFromServer() {
         // Send request to aut starter and wait for response
         ICommand command = new DisconnectFromAutAgentResponseCommand();
         Message message = new DisconnectFromAutAgentMessage();
         try {
             ServerConnection.getInstance().request(message, command, 10000);
         } catch (NotConnectedException e) {
             // Exceptions thrown from getInstance(): no connections are
             // established, just log
             log.info(Messages.ClosingTheConnectionsFailed, e);
         } catch (CommunicationException e) {
             // Exceptions thrown from getInstance(): no connections are
             // established, just log
             log.info(Messages.ClosingTheConnectionsFailed, e);
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void startAut(IAUTMainPO aut, IAUTConfigPO conf, Locale locale) 
         throws ToolkitPluginException {
 
         final String autToolkit = aut.getToolkit();
         if (!ComponentBuilder.getInstance().getLevelToolkitIds().contains(
                 autToolkit)
             && ToolkitConstants.LEVEL_TOOLKIT.equals(
                 ToolkitSupportBP.getToolkitLevel(autToolkit))) {
 
             throw new ToolkitPluginException(
                     Messages.ErrorMessageAUT_TOOLKIT_NOT_AVAILABLE);
         }
         
         try {
             // start the AUTServer
             Map<String, String> autConfigMap = createAutConfigMap(conf);
             StartAUTServerMessage startAUTServerMessage = 
                 new StartAUTServerMessage(
                     InetAddress.getLocalHost().getCanonicalHostName(), 
                     AUTConnection.getInstance().getCommunicator()
                         .getLocalPort(), autConfigMap, autToolkit, 
                         aut.isGenerateNames());
             startAUTServerMessage.setLocale(locale);
             ServerConnection.getInstance().send(startAUTServerMessage);
             if (log.isDebugEnabled()) {
                 log.debug(Messages.StartAUTServerMessageSend);
             }
         } catch (NotConnectedException nce) {
             // The ServerConnection was closed. This Exception occurs after 
             // initialising the server, so there must be a shutdown(). The 
             // listeners are already notified from the ConnectionListener of
             // the ServerConnection, -> just log.
             log.info(nce);
         } catch (ConnectionException ce) {
             // This exception is thrown from ServerConnection.getInstance(). See comment above.
             log.info(ce);
         } catch (CommunicationException cce) {
             log.fatal(cce);
             // message could not send for any reason, close the connections
             try {
                 closeConnections();
             } catch (ConnectionException ce) {
                 log.fatal(Messages.ClosingTheConnectionsFailed, ce);
             }
         } catch (UnknownHostException uhe) {
             log.fatal(uhe);
             try {
                 // from InetAdress.getLocalHost().getName(), should not occur 
                 // -> no communication possible -> close the connections as a 
                 // precaution
                 AUTConnection.getInstance().close();
                 ServerConnection.getInstance().close();
             } catch (ConnectionException ce) {
                 log.fatal(Messages.ClosingTheConnectionsFailed, ce);
             }
         }
     }
     
     /**
      * Creates the Map with the autConfig which will be send to the server.
      * @param autConfig the {@link IAUTConfigPO}
      * @return the Map which will be send to the server.
      */
     private Map<String, String> createAutConfigMap(IAUTConfigPO autConfig) {
         final Set<String> autConfigKeys = autConfig.getAutConfigKeys();
         final Map<String, String> mapToSend = 
             new HashMap<String, String>(autConfigKeys.size());
         for (String key : autConfigKeys) {
             String value = autConfig.getValue(key, null);
             mapToSend.put(key, value);
         }
         try {
             mapToSend.put(AutConfigConstants.AUT_AGENT_PORT, 
                 String.valueOf(ServerConnection.getInstance()
                         .getCommunicator().getPort()));
             mapToSend.put(AutConfigConstants.AUT_AGENT_HOST, 
                     ServerConnection.getInstance()
                         .getCommunicator().getHostName());
             mapToSend.put(AutConfigConstants.AUT_NAME, 
                     mapToSend.get(AutConfigConstants.AUT_ID));
         } catch (ConnectionException e) {
             log.error(Messages.UnableToAppendAUTAgent);
         }
         return mapToSend;
     }
 
     /**
      * closes the connections
      * @throws ConnectionException in case of error.
      */
     private void closeConnections() throws ConnectionException {
         ServerConnection.getInstance().close();
         AUTConnection.getInstance().close();
     }
 
     /**
      * {@inheritDoc}
      */
     public void stopAut(AutIdentifier autId) {
         if (log.isInfoEnabled()) {
             log.info(Messages.StoppingTest);            
         }
         try {
             ServerConnection.getInstance().getCommunicator().send(
                     new StopAUTServerMessage(autId));
         } catch (ConnectionException ce) {
             // Exceptions thrown from getInstance(): no connections are
             // established, just log
             if (log.isInfoEnabled()) {
                 log.info(Messages.ClosingTheConnectionsFailed, ce);
             }
         } catch (CommunicationException e) {
             log.error(Messages.ErrorOccurredWhileTryingToStopAUT, e);
         }
         TestExecution te = TestExecution.getInstance();
         ITestSuitePO startedTestSuite = te.getStartedTestSuite();
         if (startedTestSuite != null && startedTestSuite.isStarted()) {
             startedTestSuite.setStarted(false);
         }
         te.setStartedTestSuite(null);
         if (te.getStartedTestSuite() != null
                 && te.getStartedTestSuite().isStarted()) {
             fireEndTestExecution();
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void startObjectMapping(AutIdentifier autId, int mod, 
             int inputCode, int inputType) throws ConnectionException, 
             NotConnectedException, CommunicationException {
         
         log.info(Messages.StartingObjectMapping);
         // put the AUTServer into the mode OBJECT_MAPPING via sending a
         // ChangeAUTModeMessage(OBJECT_MAPPING).
 
         if (AUTConnection.getInstance().connectToAut(autId, 
                 new NullProgressMonitor())) {
 
             ChangeAUTModeMessage message = new ChangeAUTModeMessage();
             message.setMode(ChangeAUTModeMessage.OBJECT_MAPPING);
             message.setKeyModifier(mod);
             switch (inputType) {
                 case InputConstants.TYPE_MOUSE_CLICK:
                     message.setMouseButton(inputCode);
                     message.setKey(InputConstants.NO_INPUT);
                     break;
                 case InputConstants.TYPE_KEY_PRESS:
                     // fall through
                 default:
                     message.setKey(inputCode);
                     message.setMouseButton(InputConstants.NO_INPUT);
                     break;
             }
             AUTConnection.getInstance().send(message);
         }
             
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void startRecordTestCase(ISpecTestCasePO spec, 
             IWritableComponentNameMapper compNamesMapper, 
             int recordCompMod, int recordCompKey, 
             int recordApplMod, int recordApplKey,
             int checkModeKeyMod, int checkModeKey, 
             int checkCompKeyMod, int checkCompKey,
             boolean dialogOpen,
             SortedSet<String> singleLineTrigger,
             SortedSet<String> multiLineTrigger, Locale locale) {
         log.info(Messages.StartingRecordModus);
         // put the AUTServer into the mode RECORD_MODE via sending a
         // ChangeAUTModeMessage(RECORD_MODE).
         try {
             ChangeAUTModeMessage message = new ChangeAUTModeMessage();
             message.setMode(ChangeAUTModeMessage.RECORD_MODE);
             message.setKeyModifier(recordCompMod);
             message.setKey(recordCompKey);
             message.setKey2Modifier(recordApplMod);
             message.setKey2(recordApplKey);
             message.setCheckModeKeyModifier(checkModeKeyMod);
             message.setCheckModeKey(checkModeKey);
             message.setCheckCompKeyModifier(checkCompKeyMod);
             message.setCheckCompKey(checkCompKey);
             message.setRecordDialogOpen(dialogOpen);
             message.setSingleLineTrigger(singleLineTrigger);
             message.setMultiLineTrigger(multiLineTrigger);
             message.setToolkit(GeneralStorage.getInstance().getProject()
                     .getToolkit());
             ObjectMappingEventDispatcher.setCategoryToCreateIn(null);
                             
             AUTConnection.getInstance().send(message);
             CAPRecordedCommand.setRecSpecTestCase(spec);
             CAPRecordedCommand.setCompNamesMapper(compNamesMapper);
             CAPRecordedCommand.setRecordLocale(locale);
         } catch (UnknownMessageException ume) {
             fireAUTServerStateChanged(new AUTServerEvent(ume.getErrorId()));
         } catch (NotConnectedException nce) {
             log.error(nce);
             // HERE: notify the listeners about unsuccessfull mode change
         } catch (CommunicationException ce) {
             log.error(ce);
             // HERE: notify the listeners about unsuccessfull mode change
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void resetToTesting() {
         log.info("setting mode to test"); //$NON-NLS-1$
         // stops the object mapping modew by sending a
         // ChangeAUTModeMessage(TESTING) to the AUTServer and then closing the
         // connection to the AUT.
         try {
             ChangeAUTModeMessage message = new ChangeAUTModeMessage();
             message.setMode(ChangeAUTModeMessage.TESTING);
             CAPRecordedCommand.setRecordListener(null);
             AUTConnection.getInstance().send(message);
         } catch (UnknownMessageException ume) {
             fireAUTServerStateChanged(new AUTServerEvent(ume.getErrorId()));
         } catch (NotConnectedException nce) {
             log.error(nce);
             // HERE: notify the listeners about unsuccessfull mode change
         } catch (CommunicationException ce) {
             log.error(ce);
             // HERE: notify the listeners about unsuccessfull mode change
         }
 
         try {
             AUTConnection.getInstance().close();
         } catch (ConnectionException e) {
             log.error("Error occurred while closing connection to AUT.", e); //$NON-NLS-1$
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void startTestSuite(ITestSuitePO execTestSuite, Locale locale,
             AutIdentifier autId, boolean autoScreenshot) {
         startTestSuite(execTestSuite, locale, autId, autoScreenshot, null);
     }
     
     /**
      * {@inheritDoc}
      */
     public void startTestSuite(ITestSuitePO execTestSuite,
             Locale locale, AutIdentifier autId, boolean autoScreenshot,
             Map<String, String> externalVars) {
         TestExecution.getInstance().setStartedTestSuite(execTestSuite);
         execTestSuite.setStarted(true);
         m_testsuiteStartTime = new Date();
         setTestresultSummary(PoMaker.createTestResultSummaryPO());
         TestExecution.getInstance().executeTestSuite(execTestSuite, locale,
                 autId, autoScreenshot, externalVars, getTestresultSummary());
     }
 
     /** {@inheritDoc} */
     public void startTestJob(ITestJobPO testJob, Locale locale,
             boolean autoScreenshot) {
         TestExecution.getInstance().setStartedTestJob(testJob);
         m_testjobStartTime = new Date();
         try {
             final AtomicBoolean isTestExecutionFailed = 
                 new AtomicBoolean(false);
             final AtomicInteger testExecutionMessageId = 
                 new AtomicInteger(0);
             final AtomicInteger testExecutionState = 
                 new AtomicInteger(0);
             final AtomicBoolean isTestExecutionFinished = 
                 new AtomicBoolean(false);
             ITestExecutionEventListener executionListener = 
                 new ITestExecutionEventListener() {
                     /** {@inheritDoc} */
                     public void stateChanged(TestExecutionEvent event) {
                         testExecutionState.set(event.getState());
                         if (event.getState() 
                                 == TestExecutionEvent.TEST_EXEC_FAILED) {
                             if (event.getException() instanceof JBException) {
                                 JBException e = (JBException)
                                     event.getException();
                                 testExecutionMessageId.set(e.getErrorId());
                             }
                             isTestExecutionFailed.set(true);
                             testExecutionFinished();
                         }
                     }
                     /** {@inheritDoc} */
                     public void endTestExecution() {
                         testExecutionFinished();
                     }
                     
                     private void testExecutionFinished() {
                         isTestExecutionFinished.set(true);
                         removeTestExecutionEventListener(this);
                     }
                 };
             List<IRefTestSuitePO> refTestSuiteList = 
                 testJob.getUnmodifiableNodeList();
             for (IRefTestSuitePO refTestSuite : refTestSuiteList) {
                 isTestExecutionFailed.set(false);
                 isTestExecutionFinished.set(false);
                 addTestExecutionEventListener(executionListener);
                 AutIdentifier autId = new AutIdentifier(refTestSuite
                         .getTestSuiteAutID());
                 String testSuiteGuid = refTestSuite.getTestSuiteGuid();
                 ITestSuitePO testSuite = NodePM.getTestSuite(testSuiteGuid);
                 startTestSuite(testSuite, locale, autId, autoScreenshot);
                 while (!isTestExecutionFinished.get()) {
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException e) {
                         // Do nothing. The condition will be checked on the next
                         // loop iteration
                     }
                 }
                 if (!continueTestJobExecution(testExecutionState, 
                         testExecutionMessageId)) {
                     break;
                 }
             }
         } finally {
             TestExecution.getInstance().setStartedTestJob(null);
         }
     }
 
 
     /**
      * @param testExecutionState the test execution state 
      * @param testExecutionMessageId the test execution message id
      * @return wether the test job execution should be stopped or not
      */
     private boolean continueTestJobExecution(AtomicInteger testExecutionState,
             AtomicInteger testExecutionMessageId) {
         if (testExecutionMessageId.get() 
                 == MessageIDs.E_NO_AUT_CONNECTION_ERROR.intValue()) {
             return false;
         }
         if (testExecutionState.get() 
                 == TestExecutionEvent.TEST_EXEC_STOP) {
             return false;
         }
         return true;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void stopTestExecution() {
         fireTestExecutionChanged(new TestExecutionEvent(
                 TestExecutionEvent.TEST_EXEC_STOP));
         TestExecution.getInstance().stopExecution();
     }
 
     /**
      * {@inheritDoc}
      */
     public void pauseTestExecution(PauseMode pm) {
         TestExecution.getInstance().pauseExecution(pm);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void getAllComponentsFromAUT(IAUTInfoListener listener, 
             int timeout) {
         
         log.info(Messages.GettingAllComponentsFromAUT);
 
         if (listener == null) {
             log.warn(Messages.ListenerIsNull);
             return;
         }
 
         AUTStartedCommand response = new AUTStartedCommand(listener);
         response.setStateMessage(new AUTStateMessage(AUTStateMessage.RUNNING));
         try {
             SendAUTListOfSupportedComponentsMessage message = 
                 MessageFactory.getSendAUTListOfSupportedComponentsMessage();
             int timeoutToUse = 0;
             if (timeout > 0) {
                 timeoutToUse = timeout;
             }
             // Send the supported components and their implementation classes
             // to the AUT server to get registered.
             CompSystem compSystem = ComponentBuilder.getInstance()
                 .getCompSystem();
             
             List components = compSystem.getComponents(
                 TestExecution.getInstance().getConnectedAut().getToolkit(),
                 true);
             message.setComponents(components);
             message.setProfile(ProfileBuilder.getActiveProfile());
 
             AUTConnection.getInstance()
                     .request(message, response, timeoutToUse);
 
             long startTime = System.currentTimeMillis();
             while (System.currentTimeMillis() <= startTime + timeoutToUse
                     && !response.wasExecuted() 
                     && AUTConnection.getInstance().isConnected()) {
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     // Do nothing. The exit conditions will be checked
                     // again on the next loop iteration.
                 }
             }
             if (ObjectMappingEventDispatcher.getObjMapTransient()
                     .getMappings().isEmpty()) {
                 
                 // FIXME zeb Logging as error assumes that every AUT Server has
                 //           default mappings to contribute. So far this is true,
                 //           but might not be true for future toolkits.
                 log.warn(Messages.NoDefaultObjectMappingsCouldBeFoundForTheAUT);
             }
         } catch (UnknownMessageException ume) {
             fireAUTServerStateChanged(new AUTServerEvent(ume.getErrorId()));
         } catch (CommunicationException bce) {
             log.error(Messages.CouldNotRequestComponentsFromAUT, bce); 
             listener.error(IAUTInfoListener.ERROR_COMMUNICATION);
         } 
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void addTestEventListener(IAUTEventListener listener) {
         if (log.isInfoEnabled()) {
             log.info(Messages.AddingIAUTEventListener + StringConstants.SPACE
                     + listener.getClass().getName() + StringConstants.COLON
                     + listener.toString());
         }
         eventListenerList.add(IAUTEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void removeTestEventListener(IAUTEventListener listener) {
         if (log.isInfoEnabled()) {
             log.info(Messages.RemovingIAUTEventListener + StringConstants.SPACE
                     + listener.getClass().getName() + StringConstants.COLON
                     + listener.toString());
         }
         eventListenerList.remove(IAUTEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void addAutStarterEventListener(
             IServerEventListener listener) {
         eventListenerList.add(IServerEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void removeAutStarterEventListener(
             IServerEventListener listener) {
 
         eventListenerList.remove(IServerEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void addAUTServerEventListener(
             IAUTServerEventListener listener) {
         eventListenerList.add(IAUTServerEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void removeAUTServerEventListener(
             IAUTServerEventListener listener) {
         eventListenerList.remove(IAUTServerEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void addTestExecutionEventListener(
             ITestExecutionEventListener listener) {
         eventListenerList.add(ITestExecutionEventListener.class, listener);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void removeTestExecutionEventListener(
             ITestExecutionEventListener listener) {
         eventListenerList.remove(ITestExecutionEventListener.class, listener);
 
     }
     
     /**
      * {@inheritDoc}
      */
     public void addTestresultSummaryEventListener(
             ITestresultSummaryEventListener listener) {
         eventListenerList.add(ITestresultSummaryEventListener.class, listener);
         
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeTestresultSummaryEventListener(
             ITestresultSummaryEventListener listener) {
         eventListenerList.remove(ITestresultSummaryEventListener.class,
                 listener);
         
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void fireAUTStateChanged(AUTEvent event) {
         if (log.isInfoEnabled()) {
             log.info(Messages.FiringAUTStateChanged + StringConstants.COLON 
                     + String.valueOf(event.getState()));
         }
         // Guaranteed to return a non-null array
         Object[] listeners = eventListenerList.getListenerList();
 
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == IAUTEventListener.class) {
                 ((IAUTEventListener)listeners[i + 1]).stateChanged(event);
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void fireAutStarterStateChanged(AutStarterEvent event) {
         if (log.isInfoEnabled()) {
             log.info(Messages.FiringAUTStateChanged + StringConstants.COLON 
                     + String.valueOf(event.getState()));
         }
         // Guaranteed to return a non-null array
         Object[] listeners = eventListenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == IServerEventListener.class) {
                 ((IServerEventListener)listeners[i + 1]).stateChanged(event);
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void fireAUTServerStateChanged(AUTServerEvent event) {
         if (log.isInfoEnabled()) {
             log.info(Messages.FiringAUTStateChanged + StringConstants.COLON 
                     + String.valueOf(event.getState()));
         }
         // Guaranteed to return a non-null array
         Object[] listeners = eventListenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == IAUTServerEventListener.class) {
                 ((IAUTServerEventListener)listeners[i + 1]).stateChanged(event);
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void fireTestExecutionChanged(TestExecutionEvent event) {
         Object[] listeners = eventListenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ITestExecutionEventListener.class) {
                 ((ITestExecutionEventListener)listeners[i + 1])
                         .stateChanged(event);
             }
         }
     }
     
     /**
      * if the execution of a test ends normally the execution data from
      * the profiling agent can be collected. For that, a GetMonitoringDataMessage
      * is send to the AutAgent to collect the data.
      */
     public void getMonitoringData() {
         GetMonitoringDataMessage message = new GetMonitoringDataMessage(
                 TestExecution.getInstance().getConnectedAutId()
                 .getExecutableName());
         try {
             ServerConnection.getInstance().send(message);
         } catch (NotConnectedException nce) {
             log.error(nce);
         } catch (CommunicationException ce) {
             log.error(ce);
         }
          
     }
     /**
      * Many Agents are supporting a mechansim to generate reports 
      * from the execution data, 
      */
     public void buildMonitoringReport() {
         BuildMonitoringReportMessage message = 
             new BuildMonitoringReportMessage(
                 TestExecution.getInstance().getConnectedAutId()
                         .getExecutableName());
        
         try {
             ServerConnection.getInstance().send(message);
         } catch (NotConnectedException nce) {
             log.error(nce);
         } catch (CommunicationException ce) {
             log.error(ce);
         }
          
     } 
 
     /**
      * {@inheritDoc}
      */
     public Map<String, String> requestAutConfigMapFromAgent(String autId) {
         Map<String, String> autConfigMap = null;
         GetAutConfigMapMessage message = new GetAutConfigMapMessage(autId);
         GetAutConfigMapResponseCommand response = 
             new GetAutConfigMapResponseCommand();
         try {
             ServerConnection.getInstance().request(message, response,
                     REQUEST_CONFIG_MAP_TIMEOUT);
             final AtomicBoolean timeoutFlag = new AtomicBoolean(true);
             final Timer timerTimeout = new Timer();
             timerTimeout.schedule(new TimerTask() {
                 public void run() {
                     timeoutFlag.set(false);
                     timerTimeout.cancel();
                 }
             }, REQUEST_CONFIG_MAP_TIMEOUT);
             while (response.getAutConfigMap() == null && timeoutFlag.get()) {
                 TimeUtil.delay(200);
                 log.info(Messages.WaitingForAutConfigMapFromAgent);
             }
             autConfigMap = response.getAutConfigMap();
         } catch (NotConnectedException nce) {
             log.error(nce);
         } catch (CommunicationException ce) {
             log.error(ce);
         }
         return autConfigMap;
     }
     
     /** 
      * {@inheritDoc}
      */
     public void fireEndTestExecution() {
         m_endTime = new Date();
         TestResult result = TestResultBP.getInstance().getResultTestModel(); 
         if (result != null) {            
             createReportJob(result);
         }          
         
         Object[] listeners = eventListenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ITestExecutionEventListener.class) {
                 ((ITestExecutionEventListener)listeners[i + 1])
                         .endTestExecution();
             }
         } 
     }
     
     /**
      * creating the job that is building and writing test data to DB. 
      * @param results The test results  
      */
     private void createReportJob(TestResult results) {
         final TestResult result = results;         
         final AtomicBoolean ab = new AtomicBoolean(false);
         final Job job = new Job (Messages.ClientCollectingInformation) {
             private String m_jobFamily = this.getName();
             public boolean belongsTo(Object family) {
                 return m_jobFamily.equals(family);
             } 
             protected IStatus run(IProgressMonitor monitor) {            
                 monitor.beginTask(Messages.ClientWritingReport,
                         IProgressMonitor.UNKNOWN); 
                 writeReport();
                 monitor.beginTask(Messages.ClientWritingReportToDB,
                         IProgressMonitor.UNKNOWN); 
                 writeTestresultToDB();                
                 if (isRunningWithMonitoring()) {
                     monitor.setTaskName(Messages.ClientCalculating);
                     getMonitoringData();   
                     while (result.getMonitoringValues() == null 
                             || result.getMonitoringValues().isEmpty()) {
                         TimeUtil.delay(500);
                         Map monitoringValue = result.getMonitoringValues();
                         if (monitoringValue != null) {
                             break;
                         }                        
                         if (monitor.isCanceled()) {
                             return Status.CANCEL_STATUS;
                         }
                     }
                     monitor.setTaskName(Messages.ClientBuildingReport);
                     buildMonitoringReport();      
                     while (result.getReportData() == null) {
                         TimeUtil.delay(500);
                         if (result.getReportData() == (
                                 MonitoringConstants.EMPTY_REPORT)) {
                             break;
                         }
                         if (monitor.isCanceled()) {
                             return Status.CANCEL_STATUS;
                         }
                     }
                     writeMonitoringResults(result);              
                }          
                 monitor.done();
                 return Status.OK_STATUS;
             }
         }; 
         job.addJobChangeListener(new JobChangeAdapter() {
             public void done(IJobChangeEvent event) {
                 ab.set(true);                
             }
         });        
         final Timer timerTimeout = new Timer();
         timerTimeout.schedule(new TimerTask() {
             public void run() {
                 job.cancel();
                 timerTimeout.cancel();
             }
         }, BUILD_REPORT_TIMEOUT);
         job.setPriority(Job.LONG);        
         job.schedule();             
         while (!ab.get()) {
             TimeUtil.delay(200);            
         }            
     }     
     /**
      * writes monitoring results
      * @param result The monitoring results
      */    
     public void writeMonitoringResults(TestResult result) {
         
         ITestResultSummaryPO currentSummary = getTestresultSummary();        
         currentSummary.setMonitoringValues(result.getMonitoringValues()); 
         
         MonitoringValue significantValue = 
             findSignificantValue(result.getMonitoringValues());
         if (significantValue == null) {
             currentSummary.setMonitoringValue(null);            
         } else {
             currentSummary.setMonitoringValue(significantValue.getValue());
             currentSummary.setMonitoringValueType(significantValue.getType());
         }
         currentSummary.setInternalMonitoringId(result.getMonitoringId()); 
         if (result.getReportData() == MonitoringConstants.EMPTY_REPORT) {
             currentSummary.setReportWritten(false);
         } else {
             currentSummary.setReport(result.getReportData());
             currentSummary.setReportWritten(true);            
         }
         TestResultSummaryPM
                 .mergeTestResultSummaryInDB(
                         currentSummary);        
        fireTestresultSummaryChanged();        
     }
     /**
      * find the significant monitoring value
      * @param map The map contaning monitoring values
      * @return The MonitoringValue or null if no significant value was set
      */
     public MonitoringValue findSignificantValue(
             Map<String, IMonitoringValue> map) {
         
         Iterator<?> it = map.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry)it.next();           
             MonitoringValue tmp = (MonitoringValue)pairs.getValue();         
             if (tmp.isSignificant()) {
                 return tmp; 
             }
         }        
         
         return null;
     }
     
     /**
      * checks if last connected AUT was running with monitoring agent.
      * 
      * @return true if last connected AUT was running with monitoring else false
      */
     private boolean isRunningWithMonitoring() {
         Map<String, String> m = requestAutConfigMapFromAgent(
                 TestExecution.getInstance().getConnectedAutId()
                 .getExecutableName());
         if (m != null) {
             String monitoringID = m.get(AutConfigConstants.MONITORING_AGENT_ID);
             if (monitoringID != null
                     && !monitoringID.equals(StringConstants.EMPTY)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public void fireTestresultSummaryChanged() {
         Object[] listeners = eventListenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ITestresultSummaryEventListener.class) {
                 ((ITestresultSummaryEventListener)listeners[i + 1])
                         .testresultSummaryChanged();
             }
         }
     }    
     /**
      * {@inheritDoc}
      */
     public void writeTestresultToDB() {
         TestResult result = TestResultBP.getInstance().getResultTestModel();
         if (result != null) {
             TestresultSummaryBP.getInstance()
                 .populateTestResultSummary(result, getTestresultSummary());
 
             if (getTestresultSummary() != null) {
                 TestResultSummaryPM.storeTestResultSummaryInDB(
                         getTestresultSummary());                
                
                 TestResultPM.storeTestResult(TestresultSummaryBP.getInstance()
                         .createTestResultDetailsSession(result,
                                 getTestresultSummary().getId()));
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void writeReport() {
         if (TestResultBP.getInstance().getResultTestModel() != null 
                 && m_logPath != null) {
             
             AbstractXMLReportGenerator generator = null;
             // Use the appropriate report generator
             // Default is currently Complete
             if (Messages.TestResultViewPreferencePageStyleErrorsOnly
                     .equalsIgnoreCase(m_logStyle)) { 
 
                 generator = new ErrorsOnlyXMLReportGenerator(TestResultBP.
                         getInstance().getResultTestModel());
             } else {
                 generator = new CompleteXMLReportGenerator(TestResultBP.
                         getInstance().getResultTestModel());
             }
             writeReport(generator);
         }
     }
 
     /**
      * Writes a report to disk using the given ReportGenerator.
      * @param generator generates the XML that will be written to disk.
      */
     private void writeReport(AbstractXMLReportGenerator generator) {
         Document document = generator.generateXmlReport();
         
         String fileName = createFilename(
                 TestResultBP.getInstance().getResultTestModel());
         
         IXMLReportWriter writer =  new FileXMLReportWriter(fileName);
         writer.write(document);
     }
 
     /**
      * @param result The Test Result.
      * @return a suitable filename for the given test result model.
      */
     private String createFilename(TestResult result) {
         StringBuilder sb = new StringBuilder(m_logPath);
         sb.append(StringConstants.SLASH);
         sb.append(Messages.ExecutionLog);
         sb.append(StringConstants.MINUS);
         sb.append(result.getProjectName());
         sb.append(StringConstants.MINUS);
         TestResultNode testSuiteNode = result.getRootResultNode();
         sb.append(testSuiteNode.getName());
         sb.append(StringConstants.MINUS);
 
         // Add result of test
         if (testSuiteNode.getStatus() == TestResultNode.SUCCESS) {
             sb.append(TEST_SUCCESSFUL);
         } else {
             sb.append(TEST_FAILED);
         }
 
         if (new File(sb.toString() + XML_FILE_EXT).exists()) {
             int postfix = 1;
             sb.append(StringConstants.MINUS);
             while (new File(sb.toString() + postfix + XML_FILE_EXT).exists()) {
                 postfix++;
             }
             sb.append(postfix);
         }
 
         return sb.toString();
     }
 
     /**
      * Initiliazes the ServerConnection and the AUTConnection, in case of an
      * error, the listeners are notified with appopriate ServerEvent.
      * @param serverName The name of the server.
      * @param port The port number.
      * @throws JBVersionException in case of a version error between Client
      * and AutStarter
      * @return false if an error occurs, true otherwise
      */
     private boolean initServerConnection(String serverName, String port) 
         throws JBVersionException {
         
         try {
             ServerConnection.createInstance(serverName, port);
             ServerConnection.getInstance().run();
             if (log.isDebugEnabled()) {
                 log.debug(Messages.ConnectedToTheServer);
             }
         } catch (ConnectionException ce) {
             log.error(ce.getLocalizedMessage(), ce);
             return false;
         } catch (BaseConnection.AlreadyConnectedException ae) {
             // The connection is already established.
             if (log.isDebugEnabled()) {
                 log.debug(ae);
             }
             return false;
         }
         return true;
     }
 
     /**
      * Initiliazes the ServerConnection and the AUTConnection, in case of an
      * error, the listeners are notified with appopriate ServerEvent.
 
      * @return false if an error occurs, true otherwise
      */
     private boolean initAutServerConnection() {
         try {
             if (log.isDebugEnabled()) {
                 log.debug(Messages.AUTConnectionStarting);
             }
             AUTConnection.getInstance().run();
         } catch (ConnectionException ce) {
             log.fatal(ce);
             fireAUTServerStateChanged(new AUTServerEvent(
                     AUTServerEvent.COULD_NOT_ACCEPTING));
             return false;
         } catch (BaseConnection.AlreadyConnectedException ae) {
             // The connection is already established.
             log.error(ae);
             return false;
         } catch (JBVersionException e) {
             log.error(e);
             fireAUTServerStateChanged(new AUTServerEvent(
                 AUTServerEvent.COULD_NOT_ACCEPTING));
             return false;
         }
         return true;
     }
    
     /**
      * 
      * {@inheritDoc}
      */
     public Date getEndTime() {
         return m_endTime;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Date getTestsuiteStartTime() {
         return m_testsuiteStartTime;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public Date getTestjobStartTime() {
         return m_testjobStartTime;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setLogPath(String logPath) {
         m_logPath = logPath;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setLogStyle(String logStyle) {
         m_logStyle = logStyle;
     }
 
     /**
      * {@inheritDoc}
      */
     public void stateChanged(TestExecutionEvent event) {
         // do nothing
     }
     
     /**
      * @return the Test Result Summary for the current test execution, or for
      *         the previous test execution if no test is currently running.
      */
     private ITestResultSummaryPO getTestresultSummary() {
         return m_summary;
     }
     
     /**
      * @param summary The Test Result Summary to set.
      */
     private void setTestresultSummary(ITestResultSummaryPO summary) {
         m_summary = summary;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setRelevantFlag(boolean relevant) {
         m_relevant = relevant;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isRelevant() {
         return m_relevant;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isPauseTestExecutionOnError() {
         return m_pauseOnError;
     }
 
     /**
      * {@inheritDoc}
      */
     public void pauseTestExecutionOnError(boolean pauseOnError) {
         m_pauseOnError = pauseOnError;
     }
 }
