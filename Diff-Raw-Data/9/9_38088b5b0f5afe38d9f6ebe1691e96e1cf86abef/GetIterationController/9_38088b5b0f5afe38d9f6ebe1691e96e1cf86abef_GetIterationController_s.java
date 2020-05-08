 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.iterationcontroller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.Request;
 import edu.wpi.cs.wpisuitetng.network.models.HttpMethod;
 
 /**
  * This controller coordinates retrieving all of the Iterations
  * from the server.
  *
  */
 public class GetIterationController implements ActionListener {
 
 	private GetIterationRequestObserver observer;
 	private static GetIterationController instance;
 
 	/**
 	 * Constructs the controller given a IterationModel
 	 */
	public GetIterationController() {
 		
 		observer = new GetIterationRequestObserver(this);
 	}
 	
 	/**
 	 * Returns the instance of the GetIterationController or creates one if it does not
 	 * exist.
 	 */
 	public static GetIterationController getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new GetIterationController();
 		}
 		
 		return instance;
 	}
 
 	/**
 	 * Sends an HTTP request to store a Iteration when the
 	 * update button is pressed
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// Send a request to the core to save this Iteration
 		final Request request = Network.getInstance().makeRequest("requirementmanager/iteration", HttpMethod.GET); // GET == read
 		request.addObserver(observer); // add an observer to process the response
 		request.send(); // send the request
 	}
 	
 	/**
 	 * Sends an HTTP request to retrieve all Iterations
 	 */
 	public void retrieveIterations() {
 		final Request request = Network.getInstance().makeRequest("requirementmanager/iteration", HttpMethod.GET); // GET == read
 		request.addObserver(observer); // add an observer to process the response
 		request.send(); // send the request
 	}
 
 	/**
 	 * Add the given Iterations to the local model (they were received from the core).
 	 * This method is called by the GetIterationsRequestObserver
 	 * 
 	 * @param an array of Iterations received from the server
 	 */
 	public void receivedIterations(Iteration[] Iterations) {
 		// Make sure the response was not null
 		if (Iterations != null) 
 		{	
 			// add the Iterations to the local model
 			IterationModel.getInstance().addIterations(Iterations);
 		}
 	}
 }
