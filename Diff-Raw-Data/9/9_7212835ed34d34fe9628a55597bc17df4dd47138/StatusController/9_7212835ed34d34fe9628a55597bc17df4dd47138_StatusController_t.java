 /**
  * Copyright (c) 2000-2009, Jasig, Inc.
  * See license distributed with this file and available online at
  * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
  */
 
 package edu.wisc.mum.status.web;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.wisc.mum.status.dao.ConfigDao;
 import edu.wisc.mum.status.dao.ServerResponseDao;
 import edu.wisc.mum.status.om.MonitorLog;
 import edu.wisc.mum.status.om.MonitorStatus;
 import edu.wisc.mum.status.om.ServerResponse;
 import edu.wisc.mum.status.om.Status;
 import edu.wisc.mum.status.xom.config.Config;
 
 /**
  * @author Eric Dalquist
  * @version $Revision: 321 $
  */
 @Controller
 public class StatusController {
     private ConfigDao configDao;
     private ServerResponseDao serverResponseDao;
 
     @Autowired
     public void setConfigDao(ConfigDao configDao) {
         this.configDao = configDao;
     }
     
     @Autowired
     public void setServerResponseDao(ServerResponseDao serverResponseDao) {
         this.serverResponseDao = serverResponseDao;
     }
 
     @RequestMapping("/index.jsp")
     public ModelAndView statusIndex(
            @RequestParam(value = "refresh", required = false) Integer refresh,
            @RequestParam(value = "zoom", required = false) Double zoom) {
         final long start = System.currentTimeMillis();
         
         final Map<String, Object> model = new LinkedHashMap<String, Object>();
         
         final Config config = this.configDao.getConfig();
         model.put("config", config);
 
         final Map<String, ServerResponse> serverResponses = this.getServerResponseMap();
         model.put("serverResponses", serverResponses);
         
         final Map<String, MonitorStatus> monitorStatuses = this.getMonitorStatusMap();
         model.put("monitorStatuses", monitorStatuses);
         
         final Map<String, Collection<MonitorLog>> monitorLogs = this.getMonitorLogMap();
         model.put("monitorLogs", monitorLogs);
         
         final Map<String, Status> monitorLogsStatus = this.getMonitorLogStatusMap(monitorLogs);
         model.put("monitorLogsStatus", monitorLogsStatus);
         
         model.put("renderTime", System.currentTimeMillis() - start);
         
         if (refresh != null && refresh < 5) {
             refresh = 5;
         }
        if (zoom == null) {
            zoom = 1.0;
        }
         model.put("refresh", refresh);
        model.put("zoom", zoom);
         
         return new ModelAndView("statusIndex", model);
     }
 
     protected Map<String, Status> getMonitorLogStatusMap(final Map<String, Collection<MonitorLog>> monitorLogs) {
         final Map<String, Status> monitorLogsStatus = new LinkedHashMap<String, Status>();
         for (final Map.Entry<String, Collection<MonitorLog>> monitorLogEntry : monitorLogs.entrySet()) {
             Boolean ok = null;
             Boolean warn = null;
             for (final MonitorLog monitorLog : monitorLogEntry.getValue()) {
                 final boolean success = monitorLog.isSuccess();
                 if (ok == null || warn == null) {
                     ok = success;
                     warn = success;
                 }
                 else {
                     ok = ok && success;
                     warn = ok || success;
                 }
             }
             
             final Status status;
             if (warn == null || ok == null || (warn && !ok)) {
                 status = Status.WARN;
             }
             else if (ok) {
                 status = Status.OK;
             }
             else {
                 status = Status.FAIL;
             }
             
             final String key = monitorLogEntry.getKey();
             monitorLogsStatus.put(key, status);
         }
         return monitorLogsStatus;
     }
 
     protected Map<String, Collection<MonitorLog>> getMonitorLogMap() {
 //        final Map<String, ServerInfoKey> monitorServers = this.configDao.getMonitorServers();
 //        final Map<String, Collection<MonitorLog>> monitorLogByServer = this.serverMonitorDao.getMonitorLog(monitorServers.keySet());
 //        final Map<String, Collection<MonitorLog>> monitorLogs = new LinkedHashMap<String, Collection<MonitorLog>>();
 //        for (final Map.Entry<String, Collection<MonitorLog>> statusEntry : monitorLogByServer.entrySet()) {
 //            final ServerInfoKey serverInfoKey = monitorServers.get(statusEntry.getKey());
 //            monitorLogs.put(serverInfoKey.toString(), statusEntry.getValue());
 //        }
 //        return monitorLogs;
         return Collections.emptyMap();
     }
 
     protected Map<String, MonitorStatus> getMonitorStatusMap() {
 //        final Map<String, ServerInfoKey> monitorServers = this.configDao.getMonitorServers();
 //        final Map<String, MonitorStatus> monitorStatusByServer = this.serverMonitorDao.getMonitorStatus(monitorServers.keySet());
 //        final Map<String, MonitorStatus> monitorStatuses = new LinkedHashMap<String, MonitorStatus>();
 //        for (final Map.Entry<String, MonitorStatus> statusEntry : monitorStatusByServer.entrySet()) {
 //            final ServerInfoKey serverInfoKey = monitorServers.get(statusEntry.getKey());
 //            monitorStatuses.put(serverInfoKey.toString(), statusEntry.getValue());
 //        }
 //        return monitorStatuses;
         return Collections.emptyMap();
     }
 
     protected Map<String, ServerResponse> getServerResponseMap() {
         return this.serverResponseDao.getResults();
     }
 }
