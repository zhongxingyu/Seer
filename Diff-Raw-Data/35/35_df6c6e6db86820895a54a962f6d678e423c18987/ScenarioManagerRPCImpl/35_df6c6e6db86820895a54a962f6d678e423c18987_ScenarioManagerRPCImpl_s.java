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
 package org.sopeco.webui.server.rpc.scenario;
 
 import javax.validation.constraints.Null;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
 import org.sopeco.persistence.entities.definition.ScenarioDefinition;
 import org.sopeco.service.configuration.ServiceConfiguration;
 import org.sopeco.webui.server.rest.ClientFactory;
 import org.sopeco.webui.server.rpc.servlet.SPCRemoteServlet;
 import org.sopeco.webui.shared.builder.ScenarioDefinitionBuilder;
 import org.sopeco.webui.shared.rpc.ScenarioManagerRPC;
 
 /**
  * Implementation of the ScenarioManagerRPC. The Frontend is getting all
  * scenario inforamtion from these calls.
  * 
  * @author Marius Oehler
  * 
  */
 public class ScenarioManagerRPCImpl extends SPCRemoteServlet implements ScenarioManagerRPC {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioManagerRPCImpl.class);
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String[] getScenarioNames() {
 		requiredLoggedIn();
 
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 				     									     ServiceConfiguration.SVC_SCENARIO_LIST);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).post(Entity.entity(Null.class, MediaType.APPLICATION_JSON));
 		
 		return r.readEntity(String[].class);
 	}
 
 	/**
 	 * Adds and switches the scenario immediatly.
 	 */
 	@Override
 	public boolean addScenario(String scenarioName, String specificationName, ExperimentSeriesDefinition experiment) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 					     									 ServiceConfiguration.SVC_SCENARIO_ADD,
 					     									 scenarioName);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_SPECNAME, specificationName);
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).post(Entity.entity(experiment, MediaType.APPLICATION_JSON));
 		
 		if (r.getStatus() != Status.OK.getStatusCode()) {
 			return false;
 		}
 		
 		switchScenario(scenarioName);
 
 		return r.getStatus() == Status.OK.getStatusCode();
 	}
 
 	/**
 	 * Adds and switches the scenario immediatly.
 	 */
 	@Override
 	public boolean addScenario(ScenarioDefinition scenario) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 															 ServiceConfiguration.SVC_SCENARIO_ADD);
 											
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).post(Entity.entity(scenario, MediaType.APPLICATION_JSON));
 		
 		if (r.getStatus() != Status.OK.getStatusCode()) {
 			return false;
 		}
 		
 		switchScenario(scenario.getScenarioName());
 		
 		return r.getStatus() == Status.OK.getStatusCode();
 	}
 
 	@Override
 	public boolean removeScenario(String name) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 															 name);
 
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).delete();
 		
 		return r.getStatus() == Status.OK.getStatusCode();
 	}
 
 	@Override
 	public boolean switchScenario(String name) {
 		requiredLoggedIn();
 		
 		ScenarioDefinition definition = loadScenarioDefinition(name);
 		
 		if (definition == null) {
 			return false;
 		}
 		
 		ScenarioDefinitionBuilder builder = ScenarioDefinitionBuilder.load(definition);
 		getUser().setCurrentScenarioDefinitionBuilder(builder);
 
 		return true;
 	}
 	
 	@Override
 	public ScenarioDefinition getCurrentScenarioDefinition() {
 		requiredLoggedIn();

		System.out.println("+++++++++++++++++++++++++++++++");
		System.out.println(getAccountDetails());
		System.out.println(getAccountDetails().getAccountName());
 		
 		return loadScenarioDefinition(getAccountDetails().getSelectedScenario());
 	}
 
 	/**
 	 * Requests the {@link ScenarioDefinition} with the given name in the Service Layer.
 	 * 
 	 * @param sceName	the scenario name
 	 * @return			the {@link ScenarioDefinition}, null possible
 	 */
 	private ScenarioDefinition loadScenarioDefinition(String sceName) {
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 															 sceName,
 														     ServiceConfiguration.SVC_SCENARIO_DEFINITON);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 
 		return r.readEntity(ScenarioDefinition.class);
 	}
 	
 	/**
 	 * The scenario is updated in the SPC SL.
 	 */
 	@Override
 	public boolean storeScenarioDefinition(ScenarioDefinition definition) {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
					     									 ServiceConfiguration.SVC_SCENARIO_UPDATE);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
		Response r = wt.request(MediaType.APPLICATION_JSON).post(Entity.entity(definition, MediaType.APPLICATION_JSON));
 		
 		if (r.getStatus() != Status.OK.getStatusCode()) {
 			LOGGER.debug("Failed to store the scenario definiton.");
 			return false;
 		}
 		
		// TODO correct switch?
		Boolean b = switchScenario(definition.getScenarioName());
		
		if (!b) {
			LOGGER.debug("Could not switch scenario to the given one.");
			return false;
		}
		
		return b;
 	}
 
 	@Override
 	public String getScenarioAsXML() {
 		requiredLoggedIn();
 		
 		WebTarget wt = ClientFactory.getInstance().getClient(ServiceConfiguration.SVC_SCENARIO,
 															 getAccountDetails().getSelectedScenario(),
 														     ServiceConfiguration.SVC_SCENARIO_XML);
 		
 		wt = wt.queryParam(ServiceConfiguration.SVCP_SCENARIO_TOKEN, getToken());
 		
 		Response r = wt.request(MediaType.APPLICATION_JSON).get();
 		
 		String xml = r.readEntity(String.class);
 		
 		if (xml == null) {
 			return "";
 		}
 		
 		return xml;
 	}
 }
