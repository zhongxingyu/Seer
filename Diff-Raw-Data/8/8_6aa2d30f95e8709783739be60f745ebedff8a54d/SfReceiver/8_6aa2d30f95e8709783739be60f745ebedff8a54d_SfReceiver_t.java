 package com.sforce.intf.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 
 import com.sforce.domain.Execution;
 import com.sforce.domain.ExecutionStatus;
 import com.sforce.domain.Job;
 import com.sforce.domain.JobState;
 import com.sforce.intf.Receiver;
 import com.sforce.parser.Parser;
 import com.sforce.service.ExecutionManager;
 import com.sforce.service.JobManager;
 import com.sforce.to.InitConfig;
 import com.sforce.to.SfSqlConfig;
 import com.sforce.util.DateUtils;
 
 public abstract class SfReceiver extends SfConnector implements Receiver {
 	private static final Logger logger = LoggerFactory.getLogger(SfReceiver.class);
 	protected List<Parser<?>> parsers;
 	@Autowired
 	protected ExecutionManager executionManager;
 	@Autowired
 	protected JobManager jobManager;
 	
 	@Value("${file.parent.path}")
 	protected String parentPath;
 	
 	@Override
 	public boolean receive() {
 		this.connect(this.account, this.password);
 		
 		if (!this.connected) {
 			logger.warn("Doesn't connecto to SalesForce, Please run connect first.");
 			return false;
 		}
 		Execution execution = executionManager.findByComponent(component);
 		Date lastDate = execution.getLastSuccessDate();
 		SfSqlConfig config = new SfSqlConfig();
 		config.setLasySyncDate(lastDate);
 		Date now = new Date();
 		Job job = this.initNewJob();
 		//@TODO if file is exist
 		try {
 			this.doReceive(config, job);
 			execution.setLastSuccessDate(now);
 			execution.setStatus(ExecutionStatus.Success);
 			
 			logger.debug("Reading data from salesforce finished. Try to create [{}] for component[{}].", job, this.component);
 			File f = new File(job.getAbsolutePath());
 			if (f.exists()) {
 				jobManager.create(job);
 			} else {
 				logger.info("Skip empty file Job : {} - {}",job.getComponent(), job.getMqId());
 			}
 		} catch (Exception e) {
 			logger.error("Get Data From Salesforce Failed", e);
 			execution.setStatus(ExecutionStatus.Fail);
 		}
 		
 		executionManager.saveOrUpdate(execution);
 		
 		this.disconnect();
 		return false;
 	}
 
 	protected Job initNewJob() {
 		Job job = new Job();
 		job.setComponent(component);
 		job.setState(JobState.Created);
 		job.setMqId(this.component+DateUtils.formatMessageId(new Date()));
 		String folderPath = this.parentPath + "/"+this.component+"/"+DateUtils.formatPath(new Date())+"/";
 		String fileName = job.getMqId();
 		
 		File file = new File(folderPath, fileName);
 		if (file.exists()) {
 			logger.error("Can't override exist file[{}]", file.getAbsolutePath());
 			//TODO
 			return null;
 		}
 		job.setAbsolutePath(file.getAbsolutePath());
 		return job;
 	}
 	public abstract void doReceive(SfSqlConfig config, Job job);
 	public abstract void postInit();
 	@Override
 	public List<File> getResult() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void init(InitConfig config) {
 		this.component = config.getName();
 		this.debugMode = config.getDebugMode();
 		if (null != this.parsers) {
 			for (Parser<?> parser : this.parsers) {
 				parser.init();
 			}
 		}
 		this.postInit();
 	}
 
 	public ExecutionManager getExecutionManager() {
 		return executionManager;
 	}
 
 	public void setExecutionManager(ExecutionManager executionManager) {
 		this.executionManager = executionManager;
 	}
 
 	public JobManager getJobManager() {
 		return jobManager;
 	}
 
 	public void setJobManager(JobManager jobManager) {
 		this.jobManager = jobManager;
 	}
 
 	public String getParentPath() {
 		return parentPath;
 	}
 
 	public void setParentPath(String parentPath) {
 		this.parentPath = parentPath;
 	}
 	
 	public void write(File target, String source) {
 		try {
//			String es = new String(source.getBytes(DEFAULT_ENCODING), encoding);
//			logger.debug("Source [{}]", source);
//			logger.debug("[{}] Encoding [{}]", this.encoding, es);
			FileUtils.write(target, source, encoding, true);
 		} catch (IOException e) {
 			logger.error("Write data to file failed!", e);
 		}
 	}
 
 	public String getEncoding() {
 		return encoding;
 	}
 
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 	
 }
