 package com.lofibucket.yotris.ui;
 
 
 import com.lofibucket.yotris.ui.UserInterface;
 import com.lofibucket.yotris.ui.UserInterface;
 import com.lofibucket.yotris.util.commands.Command;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 
 
 public class UserInterfaceMock implements UserInterface {
 	private ArrayList<Command> commands;
 	public int updated;
 
 	public UserInterfaceMock() {
 		commands = new ArrayList<Command>();		
 		updated = 0;
 	}
 
 	@Override
 	public void start() {
 	}
 
 	@Override
 	public void stop() {
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		updated++;
 	}
 
 	@Override
 	public void addNewCommand(Command c) {
 		commands.add(c);
 	}
 
 	@Override
 	public List<Command> pollCommands() {
 		return commands;
 	}
 
 	public void clearCommands() {
 		commands.clear();
 	}
 
 	@Override
 	public void reset() {
 	}
 
 }
