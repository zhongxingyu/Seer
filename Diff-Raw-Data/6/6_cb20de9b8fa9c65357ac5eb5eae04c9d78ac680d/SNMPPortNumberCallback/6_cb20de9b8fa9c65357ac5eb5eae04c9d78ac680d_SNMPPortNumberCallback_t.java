 package plugins.SNMP.snmplib;
 
 import freenet.config.InvalidConfigValueException;
 import freenet.support.api.IntCallback;
 
 public class SNMPPortNumberCallback extends IntCallback {
 	
 	public SNMPPortNumberCallback() {
 	}
 	
 	public Integer get() {
 		return SNMPAgent.getSNMPAgent().getSNMPort();
 	}
 
 	public void set(Integer val) throws InvalidConfigValueException {
		if (!val.equals(get())) {
 			SNMPAgent.setSNMPPort(val);
 		}
 	}
}
