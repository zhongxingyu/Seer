 /*
  * Copyright 2009 Eugene Prokopiev <eugene.prokopiev@gmail.com>
  * 
  * This file is part of TXMLib (Telephone eXchange Management Library).
  *
  * TXMLib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TXMLib is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with TXMLib. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package txm.lib.common.core;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Eugene Prokopiev <eugene.prokopiev@gmail.com>
  *
  */
 public class OperationManager {
 	
 	@SuppressWarnings("unused")
 	private long id;
 	
 	private String name;
 	private String description;	
 	private String dumpPath;
 	private List<Attribute> attributes;
 	private CommandManager commandManager;
 
 	public void setName(String name) {
 		this.name = name;
 		setDumpPath(null);
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 	
 	public String getDumpPath() {
 		if (dumpPath != null) {
 			new File(dumpPath).mkdir();
 			return (new File(dumpPath).isDirectory())?dumpPath:null;
 		} else {
 			return null;
 		}
 	}
 
 	public void setDumpPath(String dumpPath) {
 		if (dumpPath == null)
 			this.dumpPath = "dump/"+name;
 		else
 			this.dumpPath = dumpPath;
 	}
 
 	public void setAttributes(List<Attribute> attributes) {
 		this.attributes = attributes;
 	}
 	
 	public void setAttributes(Properties params) {
 		attributes = new ArrayList<Attribute>();
 		for(String name : params.stringPropertyNames())
 			attributes.add(new Attribute(name, params.getProperty(name)));
 	}
 	
 	public void addAttribute(Attribute attribute) {
 		attributes.add(attribute);
 	}
 	
 	public void addAttribute(String name, String value) {
 		attributes.add(new Attribute(name, value));
 	}
 
 	public List<Attribute> getAttributes() {
 		return attributes;
 	}
 	
 	public void connect(CommandDump dump) throws Error {
 		String commandManagerClass = this.getClass().getCanonicalName().replace("Operation", "Command");
 		try {
 			commandManager = (CommandManager)Class.forName(commandManagerClass).getConstructor(new Class[] {}).newInstance(new Object[] {});
 			commandManager.setDump(dump);
 			commandManager.setAttributes(attributes);
 			commandManager.connect();
 		} catch (Exception e) {
 			throw new Error(e);
 		}
 		
 	}
 
 	public void execute(Operation operation) {
 		for (Method method : this.getClass().getDeclaredMethods()) {
 			if (method.getName().equals(operation.getAction())) {
 				try {
 					operation.setBeginTime(new Date());
 					connect(operation.getDump());
 					method.invoke(this, operation);
 				} catch (Exception e) {
					if (e.getCause() instanceof Error)
						operation.setError((Error)e.getCause());
					else
						operation.setError(new Error(e));
 				} finally {
 					disconnect();
 					operation.setEndTime(new Date());
 				}
 				return;
 			}
 		}
 		operation.setError(new Error(
 			"Method ["+this.getClass().getCanonicalName()+"."+operation.getAction()+"] is not found"));
 	}
 	
 	protected void executeCommand(Operation operation, Command command) throws Error {
 		operation.addCommand(command);
 		commandManager.execute(command);
 	}
 
 	protected void executeCommand(Operation operation, Command command, Map<String,CommandResultReader> resultMatch) throws Error {
 		operation.addCommand(command);
 		commandManager.execute(command, resultMatch);
 	}
 
 	protected void executeCommand(Operation operation, Command command, String pattern, CommandResultReader execution) throws Error {
 		operation.addCommand(command);
 		commandManager.execute(command, pattern, execution);
 	}
 	
 	public void pullCommand(Command command, Map<String, CommandResultReader> resultMatch) throws Error {
 		commandManager.pull(command, resultMatch);
 	}
 	
 	public void disconnect() {
 		try {
 			commandManager.disconnect();
 		} catch (Exception e) {
 			// do nothing
 		}
 	}
 }
