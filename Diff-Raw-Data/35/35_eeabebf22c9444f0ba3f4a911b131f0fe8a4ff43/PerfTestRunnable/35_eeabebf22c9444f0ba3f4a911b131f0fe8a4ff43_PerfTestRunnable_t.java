 /* 
  * Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.ngrinder.perftest.service;
 
 import static org.apache.commons.lang.ObjectUtils.defaultIfNull;
 import static org.ngrinder.model.Status.CANCELED;
 import static org.ngrinder.model.Status.DISTRIBUTE_FILES;
 import static org.ngrinder.model.Status.DISTRIBUTE_FILES_FINISHED;
 import static org.ngrinder.model.Status.START_AGENTS;
 import static org.ngrinder.model.Status.START_AGENTS_FINISHED;
 import static org.ngrinder.model.Status.START_CONSOLE;
 import static org.ngrinder.model.Status.START_TESTING;
 import static org.ngrinder.model.Status.TESTING;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 
 import net.grinder.SingleConsole;
 import net.grinder.SingleConsole.ConsoleShutdownListener;
 import net.grinder.StopReason;
 import net.grinder.common.GrinderProperties;
 import net.grinder.console.model.ConsoleProperties;
 import net.grinder.util.ListenerHelper;
 import net.grinder.util.ListenerSupport;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.ngrinder.common.constant.NGrinderConstants;
 import org.ngrinder.extension.OnTestLifeCycleRunnable;
 import org.ngrinder.extension.OnTestSamplingRunnable;
 import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
 import org.ngrinder.infra.config.Config;
 import org.ngrinder.infra.plugin.PluginManager;
 import org.ngrinder.model.AgentInfo;
 import org.ngrinder.model.PerfTest;
 import org.ngrinder.model.Status;
 import org.ngrinder.perftest.model.NullSingleConsole;
 import org.ngrinder.perftest.service.samplinglistener.AgentDieHardListener;
 import org.ngrinder.perftest.service.samplinglistener.AgentLostDetectionListener;
 import org.ngrinder.perftest.service.samplinglistener.MonitorCollectorListener;
 import org.ngrinder.perftest.service.samplinglistener.PerfTestSamplingCollectorListener;
 import org.ngrinder.perftest.service.samplinglistener.PluginRunListener;
 import org.python.google.common.collect.Sets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import com.atlassian.plugin.event.PluginEventListener;
 import com.atlassian.plugin.event.events.PluginDisabledEvent;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 
 /**
  * {@link PerfTest} run scheduler.
  * 
  * This class is responsible to execute/finish the performance test. The job is started from
  * {@link #startTest()} and {@link #finishTest()} method. These methods are scheduled by Spring
  * Task.
  * 
  * @author JunHo Yoon
  * @since 3.0
  */
 @RuntimeOnlyComponent
 public class PerfTestRunnable implements NGrinderConstants {
 
 	private static final Logger LOG = LoggerFactory.getLogger(PerfTestRunnable.class);
 
 	@Autowired
 	private PerfTestService perfTestService;
 
 	@Autowired
 	private ConsoleManager consoleManager;
 
 	@Autowired
 	private AgentManager agentManager;
 
 	@Autowired
 	private PluginManager pluginManager;
 
 	@Autowired
 	private Config config;
 
 	private List<OnTestSamplingRunnable> testSamplingRunnables;
 
 	@Autowired
 	private ApplicationContext applicationContext;
 
 	/**
 	 * Initialize plugin manager to register plugin update event.
 	 */
 	@PostConstruct
 	public void init() {
 		pluginManager.addPluginUpdateEvent(this);
 		pluginInit();
 	}
 
 	private void pluginInit() {
 		this.testSamplingRunnables = pluginManager.getEnabledModulesByClass(OnTestSamplingRunnable.class);
 	}
 
 	/**
 	 * Event handler for plugin enable.
 	 * 
 	 * @param event
 	 *            event
 	 */
 	@PluginEventListener
 	public void onPluginEnabled(PluginEnabledEvent event) {
 		pluginInit();
 	}
 
 	/**
 	 * Event handler for plugin disable.
 	 * 
 	 * @param event
 	 *            event
 	 */
 	@PluginEventListener
 	public void onPluginDisabled(PluginDisabledEvent event) {
 		pluginInit();
 	}
 
 	/**
 	 * Scheduled method for test execution. This method dispatches the test candidates and run one
 	 * of them. This method is responsible until a test is executed.
 	 */
 	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
 	public void startTest() {
 		if (config.hasNoMoreTestLock()) {
 			return;
 		}
 		// Block if the count of testing exceed the limit
 		if (!perfTestService.canExecuteTestMore()) {
 			// LOG MORE
 			List<PerfTest> currentlyRunningTests = perfTestService.getCurrentlyRunningTest();
 			LOG.debug("Currently Running test is {}. so no tests start to run", currentlyRunningTests.size());
 			return;
 		}
 		// Find out next ready perftest
 		PerfTest runCandidate = perfTestService.getPerfTestCandiate();
 		if (runCandidate == null) {
 			return;
 		}
 
 		if (!isScheduledNow(runCandidate)) {
 			// this test project is reserved,but it isn't yet going to run test
 			// right now.
 			return;
 		}
 
 		if (exceedMoreAgent(runCandidate)) {
 			return;
 		}
 
 		if (!hasEnoughFreeAgents(runCandidate)) {
 			return;
 		}
 
 		doTest(runCandidate);
 	}
 
 	private boolean isScheduledNow(PerfTest test) {
 		Date current = new Date();
 		Date scheduledDate = DateUtils
 						.truncate((Date) defaultIfNull(test.getScheduledTime(), current), Calendar.MINUTE);
 		return current.after(scheduledDate);
 	}
 
 	/**
 	 * Check the approved agent availability for the given {@link PerfTest}.
 	 * 
 	 * @param test
 	 *            {@link PerfTest}
 	 * @return true if enough agents
 	 */
 	protected boolean exceedMoreAgent(PerfTest test) {
 		int size = agentManager.getAllApprovedAgents(test.getCreatedUser()).size();
 		if (test.getAgentCount() > size) {
 			perfTestService.markAbromalTermination(test,
 							"The test is tried to execute but this test requires more agents "
 											+ "than count of approved agents." + "\n- Current all agent size : " + size
 											+ "  / Requested : " + test.getAgentCount() + "\n");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Check the free agent availability for the given {@link PerfTest}.
 	 * 
 	 * @param test
 	 *            {@link PerfTest}
 	 * @return true if enough agents
 	 */
 	protected boolean hasEnoughFreeAgents(PerfTest test) {
 		int size = agentManager.getAllFreeApprovedAgentsForUser(test.getCreatedUser()).size();
 		if (test.getAgentCount() > size) {
 			perfTestService.markProgress(test, "The test is tried to execute but there is not enough free agents."
 							+ "\n- Current free agent size : " + size + "  / Requested : " + test.getAgentCount()
 							+ "\n");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Run the given test.
 	 * 
	 * If fails, it marks STOP_BY_ERROR in the given {@link PerfTest} status
 	 * 
 	 * @param perfTest
 	 *            perftest instance;
 	 */
 	public void doTest(final PerfTest perfTest) {
 		SingleConsole singleConsole = null;
 		try {
 			singleConsole = startConsole(perfTest);
 			perfTestService.prepareDistribution(perfTest);
 			GrinderProperties grinderProperties = perfTestService.getGrinderProperties(perfTest);
 			startAgentsOn(perfTest, grinderProperties, checkCancellation(singleConsole));
 			distributeFileOn(perfTest, grinderProperties, checkCancellation(singleConsole));
 			singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest));
 			runTestOn(perfTest, grinderProperties, checkCancellation(singleConsole));
 		} catch (SinlgeConsolCancellationException ex) {
 			// In case of error, mark the occurs error on perftest.
 			doCancel(perfTest, singleConsole);
 			notifyFinsish(perfTest, StopReason.CANCEL_BY_USER);
 		} catch (Exception e) {
 			// In case of error, mark the occurs error on perftest.
 			LOG.error("Error while excuting test: {} - {} ", perfTest.getTestIdentifier(), e.getMessage());
 			LOG.debug("Stack Trace is : ", e);
 			doTerminate(perfTest, singleConsole);
 			notifyFinsish(perfTest, StopReason.ERROR_WHILE_PREPARE);
 		}
 	}
 
 	/**
 	 * Check the cancellation status on console.
 	 * 
 	 * @param singleConsole
 	 *            console
 	 * @return true if cancellation is requested.
 	 */
 	SingleConsole checkCancellation(SingleConsole singleConsole) {
 		if (singleConsole.isCanceled()) {
 			throw new SinlgeConsolCancellationException("Single Console " + singleConsole.getConsolePort()
 							+ " is canceled");
 		}
 		return singleConsole;
 	}
 
 	/**
 	 * Start a console for given {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            perftest
 	 * @return started console
 	 */
 	SingleConsole startConsole(PerfTest perfTest) {
 		perfTestService.markStatusAndProgress(perfTest, START_CONSOLE, "Console is being prepared.");
 		// get available consoles.
 		ConsoleProperties consoleProperty = perfTestService.createConsoleProperties(perfTest);
 		SingleConsole singleConsole = consoleManager.getAvailableConsole(perfTest.getTestIdentifier(), consoleProperty);
 		singleConsole.start();
 		perfTestService.markPerfTestConsoleStart(perfTest, singleConsole.getConsolePort());
 		return singleConsole;
 	}
 
 	/**
 	 * Distribute files to agents.
 	 * 
 	 * @param perfTest
 	 *            perftest
 	 * @param grinderProperties
 	 *            grinder properties
 	 * @param singleConsole
 	 *            console to be used.
 	 */
 	void distributeFileOn(final PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
 		// Distribute files
		perfTestService.markStatusAndProgress(perfTest, DISTRIBUTE_FILES, "All necessary files are being distributed.");
 		ListenerSupport<SingleConsole.FileDistributionListener> listener = ListenerHelper.create();
 		final int safeThreadHold = getSafeTransitionThreadHold();
 
 		listener.add(new SingleConsole.FileDistributionListener() {
 			@Override
 			public void distributed(String fileName) {
 				perfTestService.markProgress(perfTest, " - " + fileName);
 			}
 
 			@Override
 			public boolean start(File dir, boolean safe) {
 				if (safe) {
 					perfTestService.markProgress(perfTest, "Safe file distribution mode is enabled.");
 					return safe;
 				}
 				long sizeOfDirectory = FileUtils.sizeOfDirectory(dir);
 				if (sizeOfDirectory > safeThreadHold) {
 					perfTestService.markProgress(perfTest, "The total distributed file size is over " + safeThreadHold
 									+ "B.\n -Safe file distribution mode is enabled by force.");
 					return true;
 				}
 				return safe;
 			}
 
 		});
 
 		// the files have prepared before
 		singleConsole.distributeFiles(perfTestService.getPerfTestDistributionPath(perfTest), listener,
 						isSafeDistPerfTest(perfTest));
 		perfTestService.markStatusAndProgress(perfTest, DISTRIBUTE_FILES_FINISHED,
 						"All necessary files are distributed.");
 	}
 
 	private int getSafeTransitionThreadHold() {
 		// For backward compatibility
 		int safeThreadHold = config.getSystemProperties().getPropertyInt(NGRINDER_PROP_DIST_SAFE_THRESHHOLD_OLD, 0);
 		if (safeThreadHold == 0) {
 			safeThreadHold = config.getSystemProperties().getPropertyInt(NGRINDER_PROP_DIST_SAFE_THRESHHOLD,
 							1 * 1024 * 1024);
 		}
 		return safeThreadHold;
 	}
 
 	private boolean isSafeDistPerfTest(final PerfTest perfTest) {
 		boolean safeDist = perfTest.getSafeDistribution();
 		if (config.isCluster()) {
 			String distSafeRegion = config.getSystemProperties().getProperty(NGRINDER_PROP_DIST_SAFE_REGION,
 							StringUtils.EMPTY);
 			for (String each : StringUtils.split(distSafeRegion, ",")) {
 				if (StringUtils.equals(perfTest.getRegion(), StringUtils.trim(each))) {
 					safeDist = true;
 					break;
 				}
 			}
 		}
 		return safeDist;
 	}
 
 	/**
 	 * Start agents for the given {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            perftest
 	 * @param grinderProperties
 	 *            grinder properties
 	 * @param singleConsole
 	 *            console to be used.
 	 */
 	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
 		perfTestService.markStatusAndProgress(perfTest, START_AGENTS, perfTest.getAgentCount()
 						+ " agents are starting.");
 		agentManager.runAgent(perfTest.getCreatedUser(), singleConsole, grinderProperties, perfTest.getAgentCount());
 		singleConsole.waitUntilAgentConnected(perfTest.getAgentCount());
 		perfTestService.markStatusAndProgress(perfTest, START_AGENTS_FINISHED, perfTest.getAgentCount()
 						+ " agents are started.");
 	}
 
 	/**
 	 * Run a given {@link PerfTest} with the given {@link GrinderProperties} and the
 	 * {@link SingleConsole} .
 	 * 
 	 * @param perfTest
 	 *            perftest
 	 * @param grinderProperties
 	 *            grinder properties
 	 * @param singleConsole
 	 *            console to be used.
 	 */
 	void runTestOn(final PerfTest perfTest, GrinderProperties grinderProperties, final SingleConsole singleConsole) {
 		// start target monitor
 		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class)) {
 			run.start(perfTest, perfTestService, config.getVesion());
 		}
 
 		addSamplingListeners(perfTest, singleConsole);
 
 		// Run test
 		perfTestService.markStatusAndProgress(perfTest, START_TESTING, "The test is ready to start.");
 		// Add listener to detect abnormal condition and mark the perfTest
 		singleConsole.addListener(new ConsoleShutdownListener() {
 			@Override
 			public void readyToStop(StopReason stopReason) {
 				perfTestService.markAbromalTermination(perfTest, stopReason);
 				LOG.error("Abnormal test {} by {}", perfTest.getId(), stopReason.name());
 			}
 		});
 		long startTime = singleConsole.startTest(grinderProperties);
 		perfTest.setStartTime(new Date(startTime));
 		perfTestService.markStatusAndProgress(perfTest, TESTING, "The test is started.");
 		singleConsole.startSampling(grinderProperties.getInt(GRINDER_PROP_IGNORE_SAMPLE_COUNT, 0));
 
 	}
 
 	private void addSamplingListeners(final PerfTest perfTest, final SingleConsole singleConsole) {
 		// Add the SamplingLifeCycleFollowUpListener
 		singleConsole.addSamplingLifeCycleFollowUpCycleListener(new MonitorCollectorListener(this.applicationContext,
 						perfTest.getId(), createMonitorTargets(perfTest), singleConsole.getReportPath()));
 
 		// Add SamplingLifeCycleListener
 		singleConsole.addSamplingLifeCyleListener(new PerfTestSamplingCollectorListener(singleConsole,
 						perfTest.getId(), perfTestService));
 		singleConsole.addSamplingLifeCyleListener(new AgentLostDetectionListener(singleConsole, perfTest,
 						perfTestService));
 		singleConsole.addSamplingLifeCyleListener(new PluginRunListener(this.testSamplingRunnables, singleConsole,
 						perfTest, perfTestService));
 		singleConsole.addSamplingLifeCyleListener(new AgentDieHardListener(singleConsole, perfTest, perfTestService,
 						agentManager));
 	}
 
 	private Set<AgentInfo> createMonitorTargets(final PerfTest perfTest) {
 		final Set<AgentInfo> agents = Sets.newHashSet();
 		Set<String> ipSet = Sets.newHashSet();
 		List<String> targetIPList = perfTest.getTargetHostIP();
 		for (String targetIP : targetIPList) {
 			if (ipSet.contains(targetIP)) {
 				continue;
 			}
 			AgentInfo targetServer = new AgentInfo();
 			targetServer.setIp(targetIP);
 			targetServer.setPort(config.getMonitorPort());
 			agents.add(targetServer);
 			ipSet.add(targetIP);
 		}
 		return agents;
 	}
 
 	/**
 	 * Notify test finish to plugins.
 	 * 
 	 * @param perfTest
 	 *            PerfTest
 	 * @param reason
 	 *            the reason of test finish..
 	 * @see OnTestLifeCycleRunnable
 	 */
 	public void notifyFinsish(PerfTest perfTest, StopReason reason) {
 		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class)) {
 			run.finish(perfTest, reason.name(), perfTestService, config.getVesion());
 		}
 	}
 
 	/**
 	 * Finish the tests.(Scheduled by SpringTask)<br/>
 	 * 
 	 * There are three types of finish. <br/>
 	 * 
 	 * <ul>
 	 * <li>Abnormal test finish : when TPS is too low or too many errors occurs</li>
 	 * <li>User requested test finish : when user requested to finish test from the UI</li>
 	 * <li>Normal test finish : when test goes over the planned duration and run count.</li>
 	 * </ul>
 	 */
 	@Scheduled(fixedDelay = PERFTEST_TERMINATION_FREQUENCY_MILLISECONDS)
 	public void finishTest() {
 		for (PerfTest each : perfTestService.getAbnoramlTestingPerfTest()) {
 			LOG.error("Terminate {}", each.getId());
 			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
 			doTerminate(each, consoleUsingPort);
 			cleanUp(each);
 			notifyFinsish(each, StopReason.TOO_MANY_ERRORS);
 		}
 
 		for (PerfTest each : perfTestService.getStopRequestedPerfTest()) {
 			LOG.error("Stop test {}", each.getId());
 			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
 			doCancel(each, consoleUsingPort);
 			cleanUp(each);
 			notifyFinsish(each, StopReason.CANCEL_BY_USER);
 		}
 
 		for (PerfTest each : perfTestService.getTestingPerfTest()) {
 			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
 			if (isTestFinishCandidate(each, consoleUsingPort)) {
 				doFinish(each, consoleUsingPort);
 				cleanUp(each);
 				notifyFinsish(each, StopReason.NORMAL);
 			}
 		}
 
 	}
 
 	/**
 	 * Clean up distribution directory for the given perfTest.
 	 * 
 	 * @param perfTest
 	 *            perfTest
 	 */
 	private void cleanUp(PerfTest perfTest) {
 		perfTestService.cleanUpDistFolder(perfTest);
 		perfTestService.cleanUpRuntimeOnlyData(perfTest);
 	}
 
 	/**
 	 * Check if the given {@link PerfTest} is ready to finish.
 	 * 
 	 * @param perfTest
 	 *            perf test
 	 * @param singleConsoleInUse
 	 *            singleConsole
 	 * @return true if it's a finish candidate.
 	 */
 	private boolean isTestFinishCandidate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
 		// Give 5 seconds to be finished
 		if (perfTest.isThreshholdDuration()
 						&& singleConsoleInUse.isCurrentRunningTimeOverDuration(perfTest.getDuration())) {
 			LOG.debug("Test {} is ready to Finish. Current : {}, Planned : {}",
 							new Object[] { perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentRunningTime(),
 									perfTest.getDuration() });
 			return true;
 		} else if (perfTest.isThreshholdRunCount()
 						&& singleConsoleInUse.getCurrentExecutionCount() >= perfTest.getTotalRunCount()) {
 			LOG.debug("Test {} is ready to Finish. Current : {}, Planned : {}",
 							new Object[] { perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentExecutionCount(),
 									perfTest.getTotalRunCount() });
 			return true;
 		} else if (singleConsoleInUse instanceof NullSingleConsole) {
 			LOG.debug("Test {} is ready to Finish. Current : {}, Planned : {}",
 							new Object[] { perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentExecutionCount(),
 									perfTest.getTotalRunCount() });
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Cancel the given {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} to be canceled.
 	 * @param singleConsoleInUse
 	 *            {@link SingleConsole} which is being used for the given {@link PerfTest}
 	 */
 	public void doCancel(PerfTest perfTest, SingleConsole singleConsoleInUse) {
 		LOG.error("Cancel {} by user request.", perfTest.getTestIdentifier());
 		singleConsoleInUse.unregisterSampling();
 		try {
 			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, CANCELED,
 							"Stop requested by user");
 		} catch (Exception e) {
 			LOG.error("Error while canceling {}", perfTest.getTestIdentifier());
 			LOG.error("Details : ", e);
 		}
 		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
 	}
 
 	/**
 	 * Terminate the given {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} to be finished
 	 * @param singleConsoleInUse
 	 *            {@link SingleConsole} which is being used for the given {@link PerfTest}
 	 */
 	public void doTerminate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
 		singleConsoleInUse.unregisterSampling();
 		try {
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
 							"Stoped by error");
 		} catch (Exception e) {
 			LOG.error("Error while terminating {}", perfTest.getTestIdentifier());
 			LOG.error("Details : ", e);
 		}
 		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
 	}
 
 	/**
 	 * Finish the given {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} to be finished
 	 * @param singleConsoleInUse
 	 *            {@link SingleConsole} which is being used for the given {@link PerfTest}
 	 */
 	public void doFinish(PerfTest perfTest, SingleConsole singleConsoleInUse) {
 		// FIXME... it should found abnormal test status..
 		LOG.debug("PerfTest {} status - currentRunningTime {} ", perfTest.getId(),
 						singleConsoleInUse.getCurrentRunningTime());
 		singleConsoleInUse.unregisterSampling();
 		try {
 			// stop target host monitor
 			if (perfTestService.hasTooManError(perfTest)) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
 								"The test is finished. but contains a lot of errors over 20% of total runs");
 			} else if (singleConsoleInUse.hasNoPerformedTest()) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
 								"The test is finished. but has no TPS");
 			} else {
 				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.FINISHED,
 								"The test is finished successfully");
 			}
 		} catch (Exception e) {
			perfTestService.markStatusAndProgress(perfTest, Status.STOP_BY_ERROR, e.getMessage());
 			LOG.error("Error while finishing {}", perfTest.getTestIdentifier());
 			LOG.error("Details : ", e);
 		}
 		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
 	}
 
 	public PerfTestService getPerfTestService() {
 		return perfTestService;
 	}
 
 	public AgentManager getAgentManager() {
 		return agentManager;
 	}
 
 }
