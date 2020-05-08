 package org.ngrinder.network;
 
 import java.util.Set;
 
 import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
 import net.grinder.engine.controller.AgentControllerIdentityImplementation;
 import net.grinder.util.UnitUtil;
 
 import org.apache.commons.lang.StringUtils;
 import org.ngrinder.extension.OnPeriodicWorkingAgentCheckRunnable;
 import org.ngrinder.model.PerfTest;
 import org.ngrinder.model.Status;
 import org.ngrinder.service.IConfig;
 import org.ngrinder.service.IPerfTestService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Physical Network overflow blocking plugin. This plugin blocks tests which cause very large amount
  * of physical traffic.
  * 
  * @author JunHo Yoon
  * @since 3.1.2
  */
 public class PhysicalNetworkOverFlow implements OnPeriodicWorkingAgentCheckRunnable {
 	private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalNetworkOverFlow.class);
 	private final IConfig config;
 	private final IPerfTestService perfTestService;
 	public static final String NGRINDER_PROP_BANDWIDTH_LIMIT_MEGABYTE = "ngrinder.bandwidth.limit.megabyte";
 	public static final int NGRINDER_PROP_BANDWIDTH_LIMIT_MEGABYTE_DEFAULT_VALUE = 128;
 
 	public PhysicalNetworkOverFlow(IConfig config, IPerfTestService perfTestService) {
 		this.config = config;
 		this.perfTestService = perfTestService;
 	}
 
 	@Override
 	public void checkWorkingAgent(Set<AgentStatus> workingAgents) {
 		int totalRecieved = 0;
 		int totalSent = 0;
 		if (workingAgents.isEmpty()) {
 			return;
 		}
 
 		for (AgentStatus each : workingAgents) {
 			if (!StringUtils.contains(((AgentControllerIdentityImplementation) each.getAgentIdentity()).getRegion(),
 							"owned_")) {
 				totalRecieved += each.getSystemDataModel().getRecievedPerSec();
 				totalSent += each.getSystemDataModel().getSentPerSec();
 			}
 		}
 
 		int limit = config.getSystemProperties().getPropertyInt(NGRINDER_PROP_BANDWIDTH_LIMIT_MEGABYTE,
 						NGRINDER_PROP_BANDWIDTH_LIMIT_MEGABYTE_DEFAULT_VALUE) * 1024 * 1024;
 		if (totalRecieved > limit || totalSent > limit) {
 			LOGGER.debug("LIMIT : {}, RX : {}, TX : {}", new Object[] { limit, totalRecieved, totalSent });
 			for (PerfTest perfTest : perfTestService.getTestingPerfTest()) {
 				if (perfTest.getStatus() != Status.ABNORMAL_TESTING) {
 					perfTestService.markStatusAndProgress(
 									perfTest,
 									Status.ABNORMAL_TESTING,
									String.format("TOO MUCH TRAFFIC on this region. STOP IN FORCE.\n"
 													+ "- LIMIT/s: %s\n" + "- RX/s: %s / TX/s: %s",
 													UnitUtil.byteCountToDisplaySize(limit),
 													UnitUtil.byteCountToDisplaySize(totalRecieved),
 													UnitUtil.byteCountToDisplaySize(totalSent)));
 				}
 			}
 		}
 	}
 }
