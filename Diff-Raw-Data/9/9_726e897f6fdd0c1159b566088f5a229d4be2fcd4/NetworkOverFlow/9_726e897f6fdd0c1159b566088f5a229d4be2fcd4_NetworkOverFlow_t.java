 package org.ngrinder.network;
 
 import java.util.List;
 
 import net.grinder.common.processidentity.AgentIdentity;
 import net.grinder.statistics.ImmutableStatisticsSet;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.StatisticsIndexMap.LongIndex;
 import net.grinder.util.UnitUtil;
 
 import org.apache.commons.lang.StringUtils;
 import org.ngrinder.extension.OnTestSamplingRunnable;
 import org.ngrinder.model.AgentInfo;
 import org.ngrinder.model.PerfTest;
 import org.ngrinder.model.Status;
 import org.ngrinder.service.IAgentManagerService;
 import org.ngrinder.service.IConfig;
 import org.ngrinder.service.IPerfTestService;
 import org.ngrinder.service.ISingleConsole;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Network overflow plugin. This plugin blocks test running which causes the network overflow by the
  * large test.
  * 
  * @author JunHo Yoon
  * @since 3.1.2
  * 
  */
 public class NetworkOverFlow implements OnTestSamplingRunnable {
 	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkOverFlow.class);
 	private final IConfig config;
 	private IAgentManagerService agentManagerService;
 	private ThreadLocal<Long> limit = new ThreadLocal<Long>();
 
 	public NetworkOverFlow(IConfig config, IAgentManagerService agentManagerService) {
 		this.config = config;
 		this.agentManagerService = agentManagerService;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ngrinder.extension.OnTestSamplingRunnable#startSampling(org.ngrinder.service.ISingleConsole
 	 * , org.ngrinder.model.PerfTest, org.ngrinder.service.IPerfTestService)
 	 */
 	@Override
 	public void startSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService) {
 		List<AgentIdentity> allAttachedAgents = singleConsole.getAllAttachedAgents();
 		int consolePort = singleConsole.getConsolePort();
 		int userSpecificAgentCount = 0;
 		for (AgentInfo each : getLocalAgents()) {
 			if (each.getPort() == consolePort && StringUtils.contains(each.getRegion(), "owned")) {
 				userSpecificAgentCount++;
 			}
 		}
 		long configuredLimit = getLimit();
 		int totalAgentSize = allAttachedAgents.size();
 		int sharedAgent = (totalAgentSize - userSpecificAgentCount);
 		limit.set((sharedAgent == 0 ? Long.MAX_VALUE
 						: (long) (configuredLimit / (((float) sharedAgent) / totalAgentSize))));
 
 	}
 
 	protected int getLimit() {
 		return this.config.getSystemProperties().getPropertyInt("ngrinder.pertest.bandwidth.limit.megabyte", 128) * 1024 * 1024;
 	}
 
 	protected List<AgentInfo> getLocalAgents() {
 		return agentManagerService.getLocalAgents();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ngrinder.extension.OnTestSamplingRunnable#sampling(org.ngrinder.service.ISingleConsole,
 	 * org.ngrinder.model.PerfTest, org.ngrinder.service.IPerfTestService,
 	 * net.grinder.statistics.ImmutableStatisticsSet, net.grinder.statistics.ImmutableStatisticsSet)
 	 */
 	@Override
 	public void sampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService,
 					ImmutableStatisticsSet intervalStatistics, ImmutableStatisticsSet cumulativeStatistics) {
 		LongIndex longIndex = singleConsole.getStatisticsIndexMap().getLongIndex(
 						StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY);
 		Long byteSize = intervalStatistics.getValue(longIndex);
 		if (byteSize != null) {
 			if (byteSize > limit.get()) {
 				if (perfTest.getStatus() != Status.ABNORMAL_TESTING) {
					String message = String.format("TOO MUCH TRAFFIC by this test. STOP BY FORCE.\n"
 									+ "- LIMIT : %s - SENT :%s", UnitUtil.byteCountToDisplaySize(limit.get()),
 									UnitUtil.byteCountToDisplaySize(byteSize));
 					LOGGER.info(message);
 					LOGGER.info("Forcely Stop the test {}", perfTest.getTestIdentifier());
 					perfTestService.markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, message);
 				}
 				return;
 			}
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ngrinder.extension.OnTestSamplingRunnable#endSampling(org.ngrinder.service.ISingleConsole
 	 * , org.ngrinder.model.PerfTest, org.ngrinder.service.IPerfTestService)
 	 */
 	@Override
 	public void endSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService) {
 		limit.remove();
 	}
 
 }
