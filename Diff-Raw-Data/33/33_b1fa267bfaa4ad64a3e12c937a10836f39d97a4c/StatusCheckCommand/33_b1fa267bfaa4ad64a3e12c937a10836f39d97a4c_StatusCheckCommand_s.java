 /*
  * Hackerspace Bremen Backend - An Open-Space-Notifier
  * 
  * Copyright (C) 2012 Steve Liedtke <sliedtke57@gmail.com>
  * 
  * This program is free software; you can redistribute it and/or modify it under the terms of the 
  * GNU General Public License as published by the Free Software Foundation; either version 3 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
  * the GNU General Public License for more details.
  * 
  * You can find a copy of the GNU General Public License on http://www.gnu.org/licenses/gpl.html.
  * 
  * Contributors:
  *     Steve Liedtke <sliedtke57@gmail.com>
  */
 package de.hackerspacebremen.commands;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 
 import com.google.inject.Inject;
 
 import de.hackerspacebremen.data.entities.SpaceStatus;
 import de.hackerspacebremen.domain.api.SpaceStatusService;
 import de.hackerspacebremen.domain.val.ValidationException;
 import de.hackerspacebremen.modules.binding.annotations.Proxy;
 
 
 /**
  * @author Steve
  *
  */
 public class StatusCheckCommand extends WebCommand{
 
 	/**
      * static attribute used for logging.
      */
     private static final Logger logger = Logger.getLogger(StatusCheckCommand.class.getName());
 	
     @Inject
     @Proxy
 	private SpaceStatusService statusService;
     
 	/* (non-Javadoc)
 	 * @see de.hackerspacebremen.commands.WebCommand#process()
 	 */
 	@Override
 	public void process() throws ServletException, IOException {
 		
 		try{
 			final SpaceStatus currentStatus = statusService.currentCopyStatus();
 			if(currentStatus.getStatus()!=null && currentStatus.getStatus().equals("OPEN")){
 				logger.info("The space wasn't closed - START closing space!");
				statusService.closeSpace("ADMIN - AUTOMATIC", "");
 				this.handleSuccess("The space is now closed"/* - An email was sent to the email " + keeper.getEmail()*/, null);
 			}else{
 				logger.info("The space was correctly closed");
 			}
 		}catch(ValidationException ve){
 			this.handleError(ve);
 		}
 		super.process();
 	}
 }
