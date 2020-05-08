 package net.roguedraco.infobutton;
 
 public enum ActionType {
 
	PLAYER_COMMAND("PLAYER_COMMAND"),
	CONSOLE_COMMAND("CONSOLE_COMMAND"),
	FILE_READ("FILE_READ");
	
	private String type;
	
	private ActionType(String type) {
		this.type = type;
	}
 	
 	public static ActionType getType(String key) {
 		if(key == "PLAYER_COMMAND")
 			return ActionType.PLAYER_COMMAND;
 		if(key == "CONSOLE_COMMAND")
 			return ActionType.CONSOLE_COMMAND;
 		if(key == "FILE_READ")
 			return ActionType.FILE_READ;
 		return ActionType.PLAYER_COMMAND;
 	}

	public String getType() {
		return type;
	}
 }
