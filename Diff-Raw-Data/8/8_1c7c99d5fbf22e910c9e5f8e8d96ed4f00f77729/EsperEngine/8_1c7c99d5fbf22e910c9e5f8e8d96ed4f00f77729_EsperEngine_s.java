 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.service;
 
 import com.espertech.esper.client.Configuration;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 import com.espertech.esper.client.EPStatement;
 import net.zyclonite.gw2live.util.AppConfig;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author zyclonite
  */
 public class EsperEngine {
 
     private static final Log LOG = LogFactory.getLog(EsperEngine.class);
     private static final EsperEngine instance;
     private static AppConfig config;
     private final EPServiceProvider epService;
 
     static {
         instance = new EsperEngine();
     }
 
     private EsperEngine() {
         config = AppConfig.getInstance();
         final int inbound = config.getInt("statistics.threadpools.inbound", 0);
         final int outbound = config.getInt("statistics.threadpools.outbound", 0);
         final int route = config.getInt("statistics.threadpools.route", 0);
         final int timer = config.getInt("statistics.threadpools.timer", 0);
         final Configuration espconfig = new Configuration();
         espconfig.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);
         if (inbound > 0) {
             espconfig.getEngineDefaults().getThreading().setThreadPoolInbound(true);
             espconfig.getEngineDefaults().getThreading().setThreadPoolInboundNumThreads(inbound);
         }
         if (outbound > 0) {
             espconfig.getEngineDefaults().getThreading().setThreadPoolOutbound(true);
             espconfig.getEngineDefaults().getThreading().setThreadPoolOutboundNumThreads(outbound);
         }
         if (route > 0) {
             espconfig.getEngineDefaults().getThreading().setThreadPoolRouteExec(true);
             espconfig.getEngineDefaults().getThreading().setThreadPoolRouteExecNumThreads(route);
         }
         if (timer > 0) {
             espconfig.getEngineDefaults().getThreading().setThreadPoolTimerExec(true);
             espconfig.getEngineDefaults().getThreading().setThreadPoolTimerExecNumThreads(timer);
         }
         //change preserve order for insert into statement (could end up in 100ms latch delay if true)
         espconfig.getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(false);
         espconfig.addEventTypeAutoName("net.zyclonite.gw2live.model");
 
         epService = EPServiceProviderManager.getProvider("stats", espconfig);
         LOG.debug("EsperEngine initialized");
     }
 
     public void killCache() {
         epService.destroy();
     }
 
     public void sendEvent(final Object event) {
         epService.getEPRuntime().sendEvent(event);
     }
 
     public EPStatement createEPL(final String stmt) {
         return epService.getEPAdministrator().createEPL(stmt);
     }
 
     public static EsperEngine getInstance() {
         return instance;
     }
 }
