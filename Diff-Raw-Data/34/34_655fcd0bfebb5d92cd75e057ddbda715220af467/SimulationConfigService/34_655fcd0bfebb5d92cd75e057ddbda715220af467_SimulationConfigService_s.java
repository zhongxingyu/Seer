 package conf.service;
 
 import gui.pluginRegistry.SimPropRegistry;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import annotations.SimProp;
 
 public class SimulationConfigService {
 
 	private SimPropRegistry simPropRegistry;
 
 	public SimulationConfigService(SimPropRegistry gcr) {
 		this.setSimPropRegistry(gcr);
 	}
 
 	public SimPropRegistry getSimPropRegistry() {
 		return this.simPropRegistry;
 	}
 
 	public void loadConfig(File file) {
		// TODO Auto-generated method stub

 	}
 
 	public void setSimPropRegistry(SimPropRegistry simPropRegistry) {
 		this.simPropRegistry = simPropRegistry;
 	}
 
 	public void writeConfig(File outputFile) {
 		Properties props = new Properties();
 		for (Entry<String, SimProp> s : this.simPropRegistry.getAllSimProps()) {
 			props.setProperty(s.getKey(), s.getValue().getValue().toString());
 		}
 		try {
 			props.store(new FileOutputStream(outputFile), null);
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "File not found.");
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Could not write to file.");
 		}
 	}
 }
