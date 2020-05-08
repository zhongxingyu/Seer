 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.server.rpc;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.validation.constraints.Null;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.GenericType;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.sopeco.service.configuration.ServiceConfiguration;
 import org.sopeco.service.execute.MECLogEntry;
 import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;
 import org.sopeco.service.persistence.entities.MECLog;
 import org.sopeco.service.persistence.entities.ScheduledExperiment;
 import org.sopeco.service.rest.exchange.ExperimentStatus;
 import org.sopeco.webui.server.persistence.UiPersistenceProvider;
 import org.sopeco.webui.server.rest.ClientFactory;
 import org.sopeco.webui.server.rpc.servlet.SPCRemoteServlet;
 import org.sopeco.webui.shared.entities.FrontendScheduledExperiment;
 import org.sopeco.webui.shared.entities.RunningControllerStatus;
 import org.sopeco.webui.shared.rpc.ExecuteRPC;
 
 import com.google.gwt.core.shared.GWT;
 
 /**
  * 
  * @author Marius Oehler
  * @author Peter Merkert
  */
 public class ExecuteRPCImpl extends SPCRemoteServlet implements ExecuteRPC {
 
 	/** */
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger LOGGER = Logger.getLogger(ExecuteRPCImpl.class.getName());
 
 	@Override
 	public void scheduleExperiment(FrontendScheduledExperiment rawScheduledExperiment) {
 		requiredLoggedIn();
 
 		System.out.println("SCHEDULE");
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 															 ServiceConfiguration.SVC_EXECUTE_SCHEDULE);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		
 		ScheduledExperiment scheduledExperiment = ServiceConverter.convertFrontendScheduledExperiment(rawScheduledExperiment);
 		
 		/*
 		 * IMPORTANT: The experiment is always directly set to true in the WebUI.
 		 */
 		scheduledExperiment.setActive(true);
 
 		System.out.println("Trying to scheduled the experiment on SL.");
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).post(Entity.entity(scheduledExperiment, MediaType.APPLICATION_JSON));
 		
 		if (r.getStatus() != Status.OK.getStatusCode()) {
 			System.out.println("Failed to scheduled experiment on SL.");
 			LOGGER.info("The experiment could not be set to execution state. Most likely the "
 						+ "ScheduledExperiment has the status 'active'. Please first insert the experiment as inactive.");
 			return;
 		}
 		
 		// As the experiment status is active, the experiment key is returned from the REST Service Layer
 		// store the experiment key
 		Long experimentKey = r.readEntity(Long.class);
 		
 		System.out.println("Experiment key for ScheduledExperiment: " + experimentKey);
 		
 		getUser().setExperimentKey(experimentKey);
 		
 		System.out.println("Stored experiment key is: " + getUser().getExperimentKey());
 	}
 
 	@Override
 	public List<FrontendScheduledExperiment> getScheduledExperiments() {
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 						 									 ServiceConfiguration.SVC_EXECUTE_SCHEDULE);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 		
 		List<ScheduledExperiment> listSE = r.readEntity(new GenericType<List<ScheduledExperiment>>() { });
 		List<FrontendScheduledExperiment> listFSE = new ArrayList<FrontendScheduledExperiment>();
 		
 		for (ScheduledExperiment se : listSE) {
 			listFSE.add(ServiceConverter.convertScheduledExperiment(se));
 		}
 		
 		return listFSE;
 	}
 
 	@Override
 	public boolean removeScheduledExperiment(long id) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 															 String.valueOf(id));
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).delete();
 
 		return r.getStatus() == Status.OK.getStatusCode();
 	}
 
 	@Override
 	public boolean setScheduledExperimentEnabled(long id, boolean enabled) {
 		requiredLoggedIn();
 		
 		WebTarget wt = null;
 		
 		if (enabled) {
 			wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_ACCOUNT,
 													   String.valueOf(id),
 													   ServiceConfiguration.SVC_EXECUTE_ENABLE);
 		} else {
 			wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_ACCOUNT,
 					 								   String.valueOf(id),
 					 								   ServiceConfiguration.SVC_EXECUTE_DISABLE);
 		}
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 
 		Response r = wt.request(MediaType.APPLICATION_JSON).put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
 		
 		return r.getStatus() == Status.OK.getStatusCode();
 	}
 
 	@Override
 	public List<ExecutedExperimentDetails> getExecutedExperimentDetails() {
 		requiredLoggedIn();
 
 		GWT.log("Fetching list of ExecutedExperimentDetails.");
 		
 		// now request the executedExperimentDetails corresponding to the current selected scenario
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 			     								   			 ServiceConfiguration.SVC_EXECUTE_DETAILS);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_EXECUTE_TOKEN, getToken());
 		wt = wt.queryParam(ServiceConfiguration.SVCP_EXECUTE_SCENARIONAME, getAccountDetails().getSelectedScenario());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 		
 		// ridiculous converting for GWT must be done
 		List<org.sopeco.service.persistence.entities.ExecutedExperimentDetails> list =
 				r.readEntity(new GenericType<List<org.sopeco.service.persistence.entities.ExecutedExperimentDetails>>() { });
 		
 		GWT.log("List of ExecutedExperimentDetails size: " + list.size());
 		
 		if (r.getStatus() == Status.OK.getStatusCode()) {
 			
 			List<ExecutedExperimentDetails> eedlist = new ArrayList<ExecutedExperimentDetails>();
 			
 			if (list != null) {
 				
 				for (org.sopeco.service.persistence.entities.ExecutedExperimentDetails eed : list) {
 					eedlist.add(eed);
 				}
 				
 			}
 
 			return eedlist;
 		}
 
 		GWT.log("Returning null, when fetching ExecutedExperimentDetails.");
 		
 		return null;
 	}
 
 	@Override
 	public MECLog getMECLog(long id) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 						 									 ServiceConfiguration.SVC_EXECUTE_MECLOG);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		wt = wt.queryParam(ServiceConfiguration.SVCP_EXECUTE_ID, id);
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 		
 		return r.readEntity(org.sopeco.service.persistence.entities.MECLog.class);
 	}
 
 	@Override
 	public RunningControllerStatus getControllerLog() {
 		requiredLoggedIn();
 		
 		System.out.println("GET LOG");
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 															 ServiceConfiguration.SVC_EXECUTE_STATUS);
 		
 		long experimentKey = getUser().getExperimentKey();
 		
 		System.out.println("Experiment key for current selected experiment: " + experimentKey);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		wt = wt.queryParam(ServiceConfiguration.SVCP_EXECUTE_KEY, experimentKey);
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 
 		if (r.getStatus() == Status.OK.getStatusCode()) {
			
			ExperimentStatus es = r.readEntity(ExperimentStatus.class);
			
 			// now convert the ExperimentStatus object into a RunningControllerStatus object
 			RunningControllerStatus rcs = new RunningControllerStatus();
 			rcs.setAccount(es.getAccountId());
 			rcs.setHasFinished(es.isFinished());
 			rcs.setLabel(es.getLabel());
 			rcs.setProgress(es.getProgress());
 			rcs.setScenario(es.getScenarioName());
 			rcs.setTimeRemaining(es.getTimeRemaining());
 			rcs.setTimeStart(es.getTimeStart());
 			
 			rcs.setEventLogList(new ArrayList<MECLogEntry>());
 			for (org.sopeco.service.execute.MECLogEntry meclogentry : es.getEventLogList()) {
 				rcs.getEventLogList().add(meclogentry);
 			}
 			
 			return rcs;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void abortCurrentExperiment() {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_EXECUTE,
 												   			 ServiceConfiguration.SVC_EXECUTE_ABORT);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_ACCOUNT_TOKEN, getToken());
 		wt = wt.queryParam(ServiceConfiguration.SVCP_EXECUTE_KEY, getUser().getExperimentKey());
 		
 		wt.request(MediaType.APPLICATION_JSON).put(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
 	}
 }
