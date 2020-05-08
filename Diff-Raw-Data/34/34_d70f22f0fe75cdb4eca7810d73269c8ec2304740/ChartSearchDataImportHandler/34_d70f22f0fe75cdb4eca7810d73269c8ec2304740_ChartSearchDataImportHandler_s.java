 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *PATIENT_INFO_TIMEOUT
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.apache.solr.handler.dataimport;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.common.params.CommonParams;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.apache.solr.common.params.SolrParams;
 import org.apache.solr.common.util.ContentStream;
 import org.apache.solr.common.util.ContentStreamBase;
 import org.apache.solr.common.util.NamedList;
 import org.apache.solr.core.CloseHook;
 import org.apache.solr.core.SolrCore;
 import org.apache.solr.handler.RequestHandlerBase;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategies;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategy;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategyBasicImpl;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategyNoActionImpl;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategyNonUsageTimeImpl;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategyWithIdImpl;
 import org.apache.solr.handler.dataimport.custom.IndexSizeManager;
 import org.apache.solr.handler.dataimport.custom.PatientInfoCache;
 import org.apache.solr.handler.dataimport.custom.PatientInfoHolder;
 import org.apache.solr.handler.dataimport.custom.PatientInfoProvider;
 import org.apache.solr.handler.dataimport.custom.PatientInfoProviderCSVImpl;
 import org.apache.solr.handler.dataimport.custom.SolrConfigParams;
 import org.apache.solr.handler.dataimport.custom.SolrQueryInfo;
 import org.apache.solr.request.SolrQueryRequest;
 import org.apache.solr.request.SolrRequestHandler;
 import org.apache.solr.response.RawResponseWriter;
 import org.apache.solr.response.SolrQueryResponse;
 import org.apache.solr.util.plugin.SolrCoreAware;
 import org.openmrs.module.chartsearch.server.ConfigCommands;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 /**
  * TODO refactor! too many responsibilities
  */
 public class ChartSearchDataImportHandler extends RequestHandlerBase implements SolrCoreAware {
 	
 	private static final Logger log = LoggerFactory.getLogger(ChartSearchDataImportHandler.class);
 	
 	private final BlockingQueue<SolrQueryInfo> queue = new LinkedBlockingQueue<SolrQueryInfo>();
 	
 	private PatientInfoCache cache;
 	
 	private PatientInfoHolder patientInfoHolder;
 	
 	private ExecutorService executorService;
 	
 	private SolrConfigParams configParams;
 	
 	private String myName = "csdataimport";
 	
 	private ScheduledExecutorService patientInfoScheduledExecutorService;
 	
 	private ScheduledExecutorService indexSizeManagerScheduledExecutorService;
 	
 	private List<DataImportDaemon> dataImportDaemons = new ArrayList<DataImportDaemon>();
 	
 	private int daemonsCount;
 	
 	private IndexClearStrategy indexClearStrategy;
 	
 	private IndexSizeManager indexSizeManager;
 	
 	private int indexSizemanagerTimeout;
 	
 	private int patientInfoTimeout;
 	
 	private SolrCore core;
 	
 	@Override
 	public void init(NamedList args) {
 		super.init(args);
 		configParams = new SolrConfigParams(defaults);
 		
 		daemonsCount = configParams.getDaemonsCount();
 		indexClearStrategy = configParams.getIndexClearStrategy();
 		indexSizemanagerTimeout = configParams.getIndexSizeManagerTimeout();
 		patientInfoTimeout = configParams.getPatientInfoTimeout();
 	}
 	
 	@Override
 	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
 		
 		rsp.setHttpCaching(false);
 		SolrParams params = req.getParams();
 		
 		ContentStream contentStream = null;
 		Iterable<ContentStream> streams = req.getContentStreams();
 		if (streams != null) {
 			for (ContentStream stream : streams) {
 				contentStream = stream;
 				break;
 			}
 		}
 		RequestInfo requestParams = new RequestInfo(getParamsMap(params), contentStream);
 		String command = requestParams.getCommand();
 		NamedList defaultParams = (NamedList) initArgs.get("defaults");
 		
 		Integer personId = params.getInt("personId");
 		if (personId != null) {
 			rsp.add("personId", personId);
 		}
 		
 		if (DataImporter.IMPORT_CMD.equals(command) || DataImporter.FULL_IMPORT_CMD.equals(command)
 		        || DataImporter.DELTA_IMPORT_CMD.equals(command)) {
 			queue.put(new SolrQueryInfo(req, rsp));
 			return;
 		}
 		if (DataImporter.SHOW_CONF_CMD.equals(command)) {
 			String dataConfigFile = params.get("config");
 			String dataConfig = params.get("dataConfig");
 			if (dataConfigFile != null) {
 				dataConfig = SolrWriter.getResourceAsString(req.getCore().getResourceLoader().openResource(dataConfigFile));
 			}
 			if (dataConfig == null) {
 				rsp.add("status", DataImporter.MSG.NO_CONFIG_FOUND);
 			} else {
 				// Modify incoming request params to add wt=raw
 				ModifiableSolrParams rawParams = new ModifiableSolrParams(req.getParams());
 				rawParams.set(CommonParams.WT, "raw");
 				req.setParams(rawParams);
 				ContentStreamBase content = new ContentStreamBase.StringStream(dataConfig);
 				rsp.add(RawResponseWriter.CONTENT, content);
 			}
 			return;
 		}
 		
 		if (DataImporter.RELOAD_CONF_CMD.equals(command)) {
 			List<String> messages = new ArrayList<String>();
 			for (DataImportDaemon daemon : dataImportDaemons) {
 				DataImporter importer = daemon.getIndexUpdater().getImporter();
 				String message;
 				if (importer.maybeReloadConfiguration(requestParams, defaultParams)) {
 					message = String.format("Daemon %d: %s", daemon.getId(), DataImporter.MSG.CONFIG_RELOADED);
 				} else {
 					message = String.format("Daemon %d: %s", daemon.getId(), DataImporter.MSG.CONFIG_NOT_RELOADED);
 				}
 				messages.add(message);
 			}
 			rsp.add("importResponse", messages);
 			return;
 		}
 		
 		if (ConfigCommands.PATIENT_STATE.equals(command)) {
 			handlePatientStateCommand(rsp, personId);
 		} else if (ConfigCommands.STATS.equals(command)) {
 			handleStatsCommand(rsp);
 		} else if (ConfigCommands.PRUNE.equals(command)) {
 			String strategy = params.get(ConfigCommands.PRUNE_CLEAR_STRATEGY);
 			String idsByComma = params.get(ConfigCommands.PRUNE_IDS);
 			Integer maxPatients = params.getInt(ConfigCommands.PRUNE_MAX_PATIENTS);
 			Integer ago = params.getInt(ConfigCommands.PRUNE_AGO);
 			handlePruneCommand(rsp, strategy, idsByComma, maxPatients, ago);
 		} else if (ConfigCommands.SHANGE_DAEMONS_COUNT.equals(command)) {
 			Integer count = params.getInt(ConfigCommands.DAEMONS_COUNT);
 			if (core != null && count != null) {				
 				handleChangeDaemonsCountCommand(rsp, count);
 			}
 		}
 	}
 	
 	private void handleChangeDaemonsCountCommand(SolrQueryResponse rsp, Integer count) {
 		executorService.shutdownNow();
 		dataImportDaemons.clear();
 		runDataImportDaemons(core, count);
 		this.daemonsCount = count;
		rsp.add("status", "Daemon successfully started. Daemons count: " + daemonsCount);
 	}
 	
 	private void handlePruneCommand(SolrQueryResponse rsp, String strategyName, String idsByComma, Integer maxPatients,
 	                                Integer ago) {
 		int pruneCount = 0;
 		
 		//TODO Remove duplications
 		IndexClearStrategy strategy = null;
 		if (strategyName.equals(IndexClearStrategies.IDS.toString())) {
 			if (!StringUtils.isBlank(idsByComma)) {
 				idsByComma = idsByComma.replaceAll("\\s+","");
 				String[] idStrings = idsByComma.split(",");
 				List<Integer> ids = new ArrayList<Integer>();
 				for (String idString : idStrings) {
 					try {
 						int id = Integer.parseInt(idString);
 						ids.add(id);
 					}
 					catch (NumberFormatException e) {
 						String errorText = "Wrong id in request";
 						rsp.add(ConfigCommands.Labels.ERROR, errorText);
 						log.error(errorText);
 					}
 				}
 				if (ids.size() != 0) {
 					strategy = new IndexClearStrategyWithIdImpl(ids);
 				}
 			}
 		} else if (strategyName.toUpperCase().equals(IndexClearStrategies.BASIC.toString().toUpperCase())) {
 			if (maxPatients != null)
 				strategy = new IndexClearStrategyBasicImpl(maxPatients);
 		} else if (strategyName.toUpperCase().equals(IndexClearStrategies.NO_ACTION.toString().toUpperCase())) {
 			strategy = new IndexClearStrategyNoActionImpl();
 		} else if (strategyName.toUpperCase().equals(IndexClearStrategies.NON_USAGE_TIME.toString().toUpperCase())) {
 			if (ago != null)
 				strategy = new IndexClearStrategyNonUsageTimeImpl(ago);
 		}
 		
 		if (strategy == null) {
 			String errorText = "Couldn't create IndexClearStrategy";
 			rsp.add(ConfigCommands.Labels.ERROR, errorText);
 			log.error(errorText);
 		}
 		
 		if (strategy != null)
 			pruneCount = indexSizeManager.clearIndex(strategy);
 		
 		rsp.add(ConfigCommands.Labels.CLEARED_PATIENTS_COUNT, pruneCount);
 	}
 	
 	private void handleStatsCommand(SolrQueryResponse rsp) {
 		List<Object> list = new ArrayList<Object>();
 		for (DataImportDaemon daemon : dataImportDaemons) {
 			HashMap<String, Object> item = new HashMap<String, Object>();
 			
 			int id = daemon.getId();
 			String status = daemon.getIndexUpdater().getStatus();
 			int successCount = daemon.getIndexUpdater().getSuccessCount();
 			int failCount = daemon.getIndexUpdater().getFailCount();
 			
 			item.put(ConfigCommands.Labels.DAEMON_ID, id);
 			item.put(ConfigCommands.Labels.DAEMON_STATUS, status);
 			item.put(ConfigCommands.Labels.DAEMON_SUCCESS_COUNT, successCount);
 			item.put(ConfigCommands.Labels.DAEMON_FAIL_COUNT, failCount);
 			
 			list.add(item);
 		}
 		String clearStrategy = indexClearStrategy.toString();
 		int clearedPatientsCount = indexSizeManager.getClearedPatientsCount();
 		
 		rsp.add(ConfigCommands.Labels.DAEMON_STATES, list);
 		rsp.add(ConfigCommands.Labels.CLEAR_STRATEGY, clearStrategy);
 		rsp.add(ConfigCommands.Labels.CLEARED_PATIENTS_COUNT, clearedPatientsCount);
 	}
 	
 	private void handlePatientStateCommand(SolrQueryResponse rsp, Integer personId) {
 		if (personId != null) {
 			//TODO Add patient state 
 			rsp.add(ConfigCommands.Labels.PATIENT_LAST_INDEX_TIME, patientInfoHolder.getLastIndexTime(personId));
 		}
 	}
 	
 	@Override
 	public String getDescription() {
 		// TODO Auto-generated method stub
 		return "";
 	}
 	
 	@Override
 	public String getSource() {
 		// TODO Auto-generated method stub
 		return "";
 	}
 	
 	@Override
 	public void inform(SolrCore core) {
 		
 		this.core = core;
 		
 		// hack to get the name of this handler
 		for (Map.Entry<String, SolrRequestHandler> e : core.getRequestHandlers().entrySet()) {
 			SolrRequestHandler handler = e.getValue();
 			// this will not work if startup=lazy is set
 			if (this == handler) {
 				String name = e.getKey();
 				if (name.startsWith("/")) {
 					myName = name.substring(1);
 				}
 				// some users may have '/' in the handler name. replace with
 				// '_'
 				myName = myName.replaceAll("/", "_");
 			}
 		}
 		
 		String fileName = core.getResourceLoader().getDataDir() + File.separatorChar + "Patient information.data";
 		File patientInfoFile = new File(fileName);
 		if (!patientInfoFile.exists()) {
 			try {
 				patientInfoFile.createNewFile();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				log.error("Error creating patient information file", e);
 			}
 		}
 		PatientInfoProvider provider = new PatientInfoProviderCSVImpl(fileName);
 		cache = new PatientInfoCache(provider);
 		patientInfoHolder = new PatientInfoHolder(cache);
 		
 		runDataImportDaemons(core, daemonsCount);
 		
 		runScheduledIndexSizeManager(core, indexClearStrategy, indexSizemanagerTimeout);
 		
 		runScheduledPatientInfoUpdates(patientInfoTimeout);
 		
 		core.addCloseHook(new CloseHook() {
 			
 			@Override
 			public void preClose(SolrCore core) {
 				executorService.shutdownNow();
 				indexSizeManagerScheduledExecutorService.shutdownNow();
 				patientInfoScheduledExecutorService.shutdownNow();
 				log.info("ExecutorServices were shutdown");
 			}
 			
 			@Override
 			public void postClose(SolrCore core) {
 				// TODO Auto-generated method stub
 			}
 		});
 	}
 	
 	private Map<String, Object> getParamsMap(SolrParams params) {
 		Iterator<String> names = params.getParameterNamesIterator();
 		Map<String, Object> result = new HashMap<String, Object>();
 		while (names.hasNext()) {
 			String s = names.next();
 			String[] val = params.getParams(s);
 			if (val == null || val.length < 1)
 				continue;
 			if (val.length == 1)
 				result.put(s, val[0]);
 			else
 				result.put(s, Arrays.asList(val));
 		}
 		return result;
 	}
 	
 	private void runDataImportDaemons(SolrCore core, int daemonsCount) {
 		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("ChartSearchDataImport Daemon #%d").build();
 		executorService = Executors.newFixedThreadPool(daemonsCount, factory);
 		for (int i = 0; i < daemonsCount; i++) {
 			try {
 				startImportDaemon(i, core);
 			}
 			catch (Exception e) {
 				log.error("Error in DataImporter instantiating", e);
 			}
 			
 		}
 	}
 	
 	private void startImportDaemon(int id, SolrCore core) {
 		DataImportDaemon daemon = createDateImportDaemon(id, core);
 		dataImportDaemons.add(daemon);
 		executorService.execute(daemon);
 		log.info("Executed daemon #{}", id);
 	}
 	
 	private DataImportDaemon createDateImportDaemon(int id, SolrCore core) {
 		String importerName = myName;
 		DataImporter importer = new DataImporter(core, importerName);
 		DataImportDaemon daemon = new DataImportDaemon(id, queue, new ChartSearchIndexUpdater(importer, initArgs,
 		        patientInfoHolder));
 		return daemon;
 	}
 	
 	private void runScheduledIndexSizeManager(SolrCore core, IndexClearStrategy clearStrategy, int timeout) {
 		
 		indexSizeManager = new IndexSizeManager(core, cache, clearStrategy);
 		
 		indexSizeManagerScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
 		indexSizeManagerScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
 			
 			@Override
 			public void run() {
 				indexSizeManager.clearIndex();
 				
 			}
 		}, 10, timeout, TimeUnit.SECONDS);
 		
 	}
 	
 	private void runScheduledPatientInfoUpdates(int timeout) {
 		patientInfoScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
 		patientInfoScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
 			
 			@Override
 			public void run() {
 				cache.save();
 				
 			}
 		}, 10, timeout, TimeUnit.SECONDS);
 		
 	}
 	
 }
