 package M2;
 
 import java.util.ArrayList;
import java.util.Collection;
 
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
 	
	public Collection<ConnectorRole> getFromRoles() {
 		return fromRoles;
 	}
 	
	public Collection<ConnectorRole> getToRoles() {
 		return toRoles;
 	}
 	
 	public Configuration getSubConfig() {
 		return subConfig;
 	}
 	
 }
