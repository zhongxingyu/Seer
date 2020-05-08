 package M2;
 
 import java.util.ArrayList;
 
 public class Connector extends Element {
 
 	private ArrayList<ConnectorRole> fromRoles;
 	private ArrayList<ConnectorRole> toRoles;
 	private Configuration subConfig;
 	
 	public Connector(String name, int level) {
 		super(name,level);
 		subConfig = null;
 	}
 	
 	public void addFromRole(ConnectorRole i) {
 		fromRoles.add(i);
 	}
 	
 	public void addToRole(ConnectorRole i) {
 		toRoles.add(i);
 	}
 	
 	public void setSubConfig(Configuration config) {
 		subConfig = config;
 	}
 	
	public Collection<Connector> getFromRoles() {
 		return fromRoles;
 	}
 	
	public Collection<Connector> getToRoles() {
 		return toRoles;
 	}
 	
 	public Configuration getSubConfig() {
 		return subConfig;
 	}
 	
 }
