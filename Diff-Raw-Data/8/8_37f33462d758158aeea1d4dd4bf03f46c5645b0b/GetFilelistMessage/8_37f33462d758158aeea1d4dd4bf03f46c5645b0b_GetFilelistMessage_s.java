 package de.tr0llhoehle.buschtrommel.models;
 
 public class GetFilelistMessage extends Message {
 
 	public GetFilelistMessage() {
		type = "GETFILELIST";
 	}
 
 	@Override
 	public String Serialize() {
 		return type + MESSAGE_SPERATOR;
 	}
 
 }
