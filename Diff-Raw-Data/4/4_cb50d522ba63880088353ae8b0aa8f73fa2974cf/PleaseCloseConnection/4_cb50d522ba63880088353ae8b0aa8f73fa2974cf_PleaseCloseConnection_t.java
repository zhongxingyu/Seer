 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets;
 
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ClientFN;
 
 
 /**
  * Signals that the peer had closed the corresponding socket.
  */
 public class PleaseCloseConnection extends SignallingRequest
 {
 	private static final long serialVersionUID = -5909151809075802012L;
 	
 	@Override
 	public boolean execute(ForwardingNode fn, Packet packet, Identity requester)
 	{
 		if(fn instanceof ClientFN) {
 			ClientFN tClientFN = (ClientFN)fn;
 			try {
 				fn.getEntity().getLogger().log(this, "Sending close connection message back to sender"); 
 				//TODO: check if the following can be improved by a better signaling approach
				if(tClientFN.getConnectionEndPoint() != null){
					tClientFN.getConnectionEndPoint().write(new PleaseCloseConnection());
				}
 			}
 			catch(NetworkException exc) {
 				fn.getEntity().getLogger().err(this, "Can not send close connection message to the sender. Closing without it.", exc);
 			}			
 
 			fn.getEntity().getLogger().log(this, "execute close socket request on " +fn + " @ " + fn.getEntity());
 			((ClientFN) fn).closed();
 			return true;
 		}
 		
 		fn.getEntity().getLogger().log(this, "ignore close socket request on " +fn + " @ " + fn.getEntity());
 		return false;
 	}
 }
