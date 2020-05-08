 package de.freiburg.uni.iig.sisi.log;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.TreeMap;
 
 import de.freiburg.uni.iig.sisi.simulation.SimulationEngine;
 
 public class LogGenerator implements PropertyChangeListener {
 
 	public enum FileMode {
 		CSV, MXML
 	}
 	
 	private final FileMode fileMode;
 	private TreeMap<String, EventLog> eventLogs = new TreeMap<String, EventLog>();
 	private TreeMap<String, MutationEvent> mutationLog = new TreeMap<String, MutationEvent>();
 	private TreeMap<String, ProcessInstanceInformation> modelMap = new TreeMap<String, ProcessInstanceInformation>();
 
 	private String currentSimulationID = null;
 
 	public LogGenerator(SimulationEngine se) {
 		se.addChangeListener(this);
 		this.fileMode = FileMode.CSV;
 	}
 
 	public LogGenerator(SimulationEngine se, FileMode fileMode) {
 		se.addChangeListener(this);
 		this.fileMode = fileMode;
 	}
 	
 	public FileMode getFileMode() {
 		return fileMode;
 	}
 
 	public TreeMap<String, EventLog> getEventLogs() {
 		return eventLogs;
 	}	
 	
 	public TreeMap<String, MutationEvent> getMutationLog() {
 		return mutationLog;
 	}
 
 	public void addMutationEvent(MutationEvent mutationEvent) {
 		this.mutationLog.put(mutationEvent.getSimulationID(), mutationEvent);
 	}
 
 	public TreeMap<String, ProcessInstanceInformation> getModelMap() {
 		return modelMap;
 	}
 
 	protected String getCurrentSimulationID() {
 		return currentSimulationID;
 	}
 
 	protected void setCurrentSimulationID(String currentSimulation) {
 		this.currentSimulationID = currentSimulation;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName() == SimulationEngine.PORPERTY_SIMULATION_START) {
 			setCurrentSimulationID((String) evt.getNewValue());
 			eventLogs.put(getCurrentSimulationID(), new EventLog());
 		}
 		if (evt.getPropertyName() == SimulationEngine.PROPERTY_TRANSITION_FIRED) {
 			eventLogs.get(getCurrentSimulationID()).addEvent((SimulationEvent) evt.getNewValue());
 		}
 		if (evt.getPropertyName() == SimulationEngine.PORPERTY_SIMULATION_COMPLETE) {
 			modelMap.put(getCurrentSimulationID(), (ProcessInstanceInformation) evt.getNewValue());
 		}
 		if ( evt.getPropertyName() == SimulationEngine.PROPERTY_MUTATION_EXECUTED ) {
 			MutationEvent mutationEvent = ((MutationEvent) evt.getNewValue());
 			mutationLog.put(mutationEvent.getSimulationID(), mutationEvent);
 		}
 	}
 
 	public String generateLog(String path, boolean createFile) throws IOException {
 		// parse log
 		String log = "";
 		if ( fileMode == FileMode.MXML ) {
 			log = MXMLLog.createMXML(eventLogs);
 		} else {
 			log = logsToCSV();
 		}
 		
 		// create file
 		if ( createFile ) {
 			File file = new File(path + getFileType());
 			FileOutputStream fop = new FileOutputStream(file);
 			if ( !file.exists() )
 				file.createNewFile();
 			fop.write(log.getBytes("UTF-8"));
 			fop.flush();
 			fop.close();	
 		}
 		generateViolationLog(path, true);
 		generateModelLog(path, true);
 		return log;
 	}
 	
 	public String generateLogFromID(String id, String path, boolean createFile) throws IOException {
 		// parse log
 		String log = "";
 		if ( fileMode == FileMode.MXML ) {
 			log = MXMLLog.createMXML(eventLogs);
 		} else {
 			log = logToCSV(id);
 			// create file
 			if ( createFile ) {
 				File file = new File(path + getFileType());
 				FileOutputStream fop = new FileOutputStream(file);
 				if ( !file.exists() )
 					file.createNewFile();
 				fop.write(log.getBytes("UTF-8"));
 				fop.flush();
 				fop.close();	
 			}			
 		}
 
 		if ( !mutationLog.isEmpty() ) {
 			generateLogFromID(id, path + "_violationData.log", true);
 		}		
 		generateViolationLogFromID(id, path, true);
 		generateModelLogFromID(id, path, true);
 		return log;		
 	}
 	
 	private String generateViolationLog(String path, boolean createFile) throws IOException {
 		if ( mutationLog.isEmpty() )
 			return null;
 		
 		String log = "";
 		for (MutationEvent event : mutationLog.values()) {
 			log += event.toString() + System.getProperty("line.separator");
 		}
 		
 		// create file
 		if ( createFile ) {
 			File file = new File(path + "_violationData.log");
 			if ( !file.exists() )
 				file.createNewFile();
 			FileOutputStream fop = new FileOutputStream(file);
 			if ( !file.exists() )
 				file.createNewFile();
 			fop.write(log.getBytes("UTF-8"));
 			fop.flush();
 			fop.close();		
 		}	
 		return log;
 	}
 	
 	private String generateViolationLogFromID(String id, String path, boolean createFile) throws IOException {
 		if ( !mutationLog.containsKey(id) )
 			return null;
	
 		// parse log
 		String log = mutationLog.get(id).toString() + System.getProperty("line.separator");
 		
 		// create file
 		if ( createFile ) {
 			File file = new File(path + "_violationData.log");
 			FileOutputStream fop = new FileOutputStream(file);
 			if ( !file.exists() )
 				file.createNewFile();
 			fop.write(log.getBytes("UTF-8"));
 			fop.flush();
 			fop.close();	
 		}
 		return null;
 	}	
 
 	private String generateModelLog(String path, boolean createFile) throws IOException {
 		if ( modelMap.isEmpty() )
 			return null;
 		
 		String log = "";
 		for (ProcessInstanceInformation info : modelMap.values()) {
 			log += info.toString() + System.getProperty("line.separator");
 		}
 		
 		// create file
 		if ( createFile ) {
 			File file = new File(path + "_modelData.log");
 			FileOutputStream fop = new FileOutputStream(file);
 			if ( !file.exists() )
 				file.createNewFile();
 			fop.write(log.getBytes("UTF-8"));
 			fop.flush();
 			fop.close();	
 		}	
 		return log;
 	}	
 
 	private String generateModelLogFromID(String id, String path, boolean createFile) throws IOException {
 		if ( !modelMap.containsKey(id) )
 			return null;
 
 		// parse log
 		String log = modelMap.get(id).toString() + System.getProperty("line.separator");
 		
 		// create file
 		if ( createFile ) {
 			File file = new File(path + "_modelData.log");
 			FileOutputStream fop = new FileOutputStream(file);
 			if ( !file.exists() )
 				file.createNewFile();
 			fop.write(log.getBytes("UTF-8"));
 			fop.flush();
 			fop.close();		
 		}
 		return null;
 	}	
 	
 	public String logToCSV(String id) {
 		String log = "Case ID,Task,Subjekt,Obejcts" + System.getProperty("line.separator");
 		for (SimulationEvent event : eventLogs.get(id).getEvents()) {
 			log += event.toCSV() + System.getProperty("line.separator");
 		}
 		return log;
 	}
 	
 	public String logsToCSV(){
 		String log = "Case ID,Task,Subjekt,Obejcts" + System.getProperty("line.separator");
 		for (EventLog eventLog : eventLogs.values()) {
 			for (SimulationEvent event : eventLog.getEvents()) {
 				log += event.toCSV() + System.getProperty("line.separator");
 			}
 		}		
 		return log;
 	}
 	
 	public String logToMXML(String id){
 		TreeMap<String, EventLog> tree = new TreeMap<>();
 		tree.put(id, eventLogs.get(id));
 		return MXMLLog.createMXML(tree);
 	}
 	
 	public String logsToMXML(){
 		return MXMLLog.createMXML(eventLogs);
 	}
 	
 	private String getFileType() {
 		if ( fileMode == FileMode.CSV ) return ".csv";
 		return ".mxml";
 	}
 	
 }
