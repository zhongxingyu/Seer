 ///////////////////////////////////////////////////////////////////////////
 //
 // This program is part of Zenoss Core, an open source monitoring platform.
 // Copyright (C) 2008, Zenoss Inc.
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License version 2 as published by
 // the Free Software Foundation.
 //
 // For complete information please visit: http://www.zenoss.com/oss/
 //
 ///////////////////////////////////////////////////////////////////////////
 package com.zenoss.zenpacks.zenjmx;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.zenoss.jmx.JmxClient;
 import com.zenoss.jmx.JmxException;
 import com.zenoss.zenpacks.zenjmx.call.CallFactory;
 import com.zenoss.zenpacks.zenjmx.call.ConfigurationException;
 import com.zenoss.zenpacks.zenjmx.call.JmxCall;
 import com.zenoss.zenpacks.zenjmx.call.Summary;
 import com.zenoss.zenpacks.zenjmx.call.Utility;
 
 public class ZenJmxService {
   private static final Log _logger = LogFactory.getLog(ZenJmxService.class);
 
   public Object collect(List<Map<?, ?>> dsConfigs) throws Exception {
 
     long start = System.currentTimeMillis();
     if (_logger.isDebugEnabled()) {
       _logger.debug("processing " + dsConfigs.size() + " datasources");
     }
     boolean concurrentCalls = Configuration.instance().propertyExists(
         OptionsFactory.CONCURRENT_JMX_CALLS);
     JMXCollector collector = new JMXCollector(dsConfigs, concurrentCalls);
     List<Map<String, String>> result = collector.collect();
     String msg = "finished processing %1$s datasources for device %2$s in %3$s ms";
     _logger.info(String.format(msg, dsConfigs.size(), collector._deviceId,
         (System.currentTimeMillis() - start)));
     return result;
   }
 
   public static class JMXCollector {
     private static final String SUMMARY = "summary";
     // logger
     private static final Log _logger = LogFactory.getLog(JMXCollector.class);
     boolean _concurrentServerCalls = false;
     boolean _authenticate;
     String _username;
     String _password;
     String _deviceId;
     ConfigAdapter _config;
     List<ConfigAdapter> _configs = new ArrayList<ConfigAdapter>();
 
     public JMXCollector(List<Map<?, ?>> dataSourceConfigs, boolean concurrent) {
       _concurrentServerCalls = concurrent;
       ConfigAdapter config = null;
       for (Map<?, ?> configMap : dataSourceConfigs) {
         config = new ConfigAdapter(configMap);
         _configs.add(config);
       }
       // we assume all configs are to the same device
       // and to the same jmx server with same credentials
       if (config != null) {
         _authenticate = config.authenticate();
         _username = config.getUsername();
         _password = config.getPassword();
         _config = config;
         _deviceId = config.getDevice();
       }
 
     }
 
     /**
      * collects jmx values and returns a list of results.
      * 
      * @param dsConfigs
      * @return
      */
     public List<Map<String, String>> collect() {
       List<Map<String, String>> result = new LinkedList<Map<String, String>>();
       JmxClient client = null;
 
       try {
         client = createJmxClient();
         client.connect();
         result.addAll(doCollect(client));
      } catch (Throwable e) {
         Map<String, String> error = createConnectionError(_deviceId,
             "error connecting to server", e);
         result.add(error);
 
       } finally {
         if (client != null) {
           try {
             client.close();
           } catch (JmxException e) {
             Map<String, String> error = createConnectionError(_config
                 .getDevice(), "error closing connection to server", e);
             result.add(error);
           }
         }
       }
       return result;
     }
 
     private List<Map<String, String>> doCollect(final JmxClient client) {
 
       // all calls should be to same server with same credentials
       int size = _configs.size();
       final List<Map<String, String>> results = Collections
           .synchronizedList(new ArrayList<Map<String, String>>(size));
       // used to keep track of running calls
       final Map<Summary, ConfigAdapter> summaries = Collections
           .synchronizedMap(new HashMap<Summary, ConfigAdapter>());
       ExecutorService es = null;
       // create an single or multi-threaded executor
       if (_concurrentServerCalls) {
         es = Executors.newCachedThreadPool();
       } else {
         es = Executors.newSingleThreadExecutor();
       }
       for (final ConfigAdapter config : _configs) {
         try {
 
           final JmxCall call = CallFactory.createCall(config);
           // create job to query and create result
           Runnable job = new Runnable() {
             public void run() {
               Summary summary = call.getSummary();
               // keep track of unfinished summaries
               summaries.put(summary, config);
               try {
                 call.call(client);
                 results.addAll(createResult(summary));
               } catch (JmxException e) {
                 results.add(createError(summary, config, e));
               } finally {
                 summaries.remove(summary);
               }
             }
           };
           // submit job to be run
           es.execute(job);
         } catch (ConfigurationException e) {
           Map<String, String> err = createError(config, e);
           results.add(err);
         }
 
       }
       // shutdown the executor service and wait for all pending jobs to
       // finish
       es.shutdown();
       try {
         es.awaitTermination(5 * 60 * 1000, TimeUnit.MILLISECONDS);
       } catch (InterruptedException e) {
         for (Entry<Summary, ConfigAdapter> entry : summaries.entrySet()) {
           results.add(createTimeOutError(entry.getKey(), entry.getValue()));
         }
       }
       return results;
     }
 
     private JmxClient createJmxClient() throws ConfigurationException {
       JmxClient jmxClient = null;
       String url = Utility.getUrl(_config);
       jmxClient = new JmxClient(url);
       if (_authenticate) {
         String[] creds = new String[] { _username, _password };
         jmxClient.setCredentials(creds);
       }
 
       return jmxClient;
     }
 
     private List<Map<String, String>> createResult(Summary summary) {
       if (_logger.isDebugEnabled()) {
         _logger.debug(summary.toString());
       }
       List<Map<String, String>> results = new ArrayList<Map<String, String>>();
 
       Map<String, Object> values = summary.getResults();
 
       for (String key : values.keySet()) {
         Object value = values.get(key);
         if (value == null) {
           _logger.warn("(" + summary.getCallId()
               + "): null value for data point: " + key);
           continue;
         }
         HashMap<String, String> result = new HashMap<String, String>();
         results.add(result);
         result.put(ConfigAdapter.DEVICE, summary.getDeviceId());
         result.put(ConfigAdapter.DATASOURCE_ID, summary.getDataSourceId());
         result.put("value", value.toString());
         result.put("dpId", key);
       }
       return results;
     }
 
     private Map<String, String> createError(Summary summary,
         ConfigAdapter config, Exception e) {
       String msg = "DataSource %1$s; Error calling mbean %2$s: Exception: %3$s";
       msg = String.format(msg, config.getDatasourceId(), summary
           .getObjectName(), e.getMessage());
       Map<String, String> error = createError(config, e);
       error.put(SUMMARY, msg);
       return error;
     }
 
     private Map<String, String> createTimeOutError(Summary summary,
         ConfigAdapter config) {
       String msg = "DataSource %1$s; Timed out %2$s on mbean %3$s ";
       msg = String.format(msg, config.getDatasourceId(), summary
           .getCallSummary(), summary.getObjectName());
       Map<String, String> error = createError(config, null);
       error.put(SUMMARY, msg);
       return error;
     }
 
     private HashMap<String, String> createError(String deviceId, String msg) {
       HashMap<String, String> error = new HashMap<String, String>();
 
       error.put(ConfigAdapter.DEVICE, deviceId);
 
       error.put(SUMMARY, msg);
 
       return error;
     }
 
     private Map<String, String> createConnectionError(String deviceId,
        String msg, Throwable e) {
       HashMap<String, String> error = createError(deviceId, msg + ":"
           + e.getMessage());
       error.put(ConfigAdapter.EVENT_CLASS, "/Status/JMX/Connection");
       return error;
     }
 
     private Map<String, String> createError(ConfigAdapter config, Exception e) {
       String msg = "";
       if (e != null)
         msg = e.getMessage();
       HashMap<String, String> error = createError(config.getDevice(), msg);
       error.put(ConfigAdapter.DATASOURCE_ID, config.getDatasourceId());
       error.put(ConfigAdapter.EVENT_CLASS, config.getEventClass());
       error.put(ConfigAdapter.EVENT_KEY, config.getEventKey());
       error.put(ConfigAdapter.COMPONENT_KEY, config.getComponent());
       return error;
     }
   }
 
 }
