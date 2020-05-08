 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.Request;
 import edu.wpi.cs.wpisuitetng.network.models.HttpMethod;
 
 /**
  * This controller coordinates retrieving all of the requirements
  * from the server.
  *
  */
 public class GetRequirementsController implements ActionListener {
 
 	private GetRequirementsRequestObserver observer;
 	private static GetRequirementsController instance;
 
 	/**
 	 * Constructs the controller given a RequirementModel
 	 */
	private GetRequirementsController() {
 		
 		observer = new GetRequirementsRequestObserver(this);
 	}
 	
 	/**
 	 * Returns the instance of the GetRequirementController or creates one if it does not
 	 * exist.
 	 */
 	public static GetRequirementsController getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new GetRequirementsController();
 		}
 		
 		return instance;
 	}
 
 	/**
 	 * Sends an HTTP request to store a requirement when the
 	 * update button is pressed
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// Send a request to the core to save this requirement
 		final Request request = Network.getInstance().makeRequest("requirementmanager/requirement", HttpMethod.GET); // GET == read
 		request.addObserver(observer); // add an observer to process the response
 		request.send(); // send the request
 	}
 	
 	/**
 	 * Sends an HTTP request to retrieve all requirements
 	 */
 	public void retrieveRequirements() {
 		final Request request = Network.getInstance().makeRequest("requirementmanager/requirement", HttpMethod.GET); // GET == read
 		request.addObserver(observer); // add an observer to process the response
 		request.send(); // send the request
 	}
 
 	/**
 	 * Add the given requirements to the local model (they were received from the core).
 	 * This method is called by the GetRequirementsRequestObserver
 	 * 
 	 * @param an array of requirements received from the server
 	 */
 	public void receivedRequirements(Requirement[] requirements) {
 		// Empty the local model to eliminate duplications
 		RequirementModel.getInstance().emptyModel();
 		
 		// Make sure the response was not null
 		if (requirements != null) {
 			
 			// add the requirements to the local model
 			RequirementModel.getInstance().addRequirements(requirements);
 		}
 	}
 }
