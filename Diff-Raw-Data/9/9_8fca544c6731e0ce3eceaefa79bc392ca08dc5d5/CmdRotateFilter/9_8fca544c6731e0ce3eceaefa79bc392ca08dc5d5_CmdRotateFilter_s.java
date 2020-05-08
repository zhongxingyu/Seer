 package org.scifair2015.commands;
 
 import org.scifair2015.client.SciFair2015Client;
 
 
 public class CmdRotateFilter extends Command {
 	private int position;
 	private int motorNumber;
 	public CmdRotateFilter(int motorNumber, int position) {
		cmd = 20;
 		this.motorNumber = motorNumber;
 		this.position = position;
 	}
 	
 	@Override
 	public void sendCommand() {
 		
 		super.sendCommand();
 		SciFair2015Client.writeInt(motorNumber);
 		SciFair2015Client.writeInt(position);
 	}
 }
