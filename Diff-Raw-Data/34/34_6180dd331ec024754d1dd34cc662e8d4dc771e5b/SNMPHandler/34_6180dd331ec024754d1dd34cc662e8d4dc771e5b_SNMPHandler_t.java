 package com.basho.proserv.riakmdcmon.Alarm;
 
 import java.io.IOException;
 
 import org.snmp4j.CommunityTarget;
 import org.snmp4j.PDU;
 import org.snmp4j.PDUv1;
 import org.snmp4j.Snmp;
 import org.snmp4j.TransportMapping;
 import org.snmp4j.mp.SnmpConstants;
 import org.snmp4j.smi.Address;
 import org.snmp4j.smi.Gauge32;
 import org.snmp4j.smi.GenericAddress;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.VariableBinding;
 import org.snmp4j.transport.DefaultUdpTransportMapping;
 
 import com.basho.proserv.riakmdcmon.Configuration;
 
 public class SNMPHandler implements IAlarmHandler {
 	public static enum SNMP_VERSION { V1, V2C };
 	private final static String DEFAULT_SNMP_TEXT_FMT = "Riak Cluster %s is out of tolerance by %d milliseconds";
 	private final Configuration config;
 	
 	public static String ENTERPRISE_OID = "1.3.6.1.4.1.31130.900";
 	public static String DATA_CENTER_ID_OID = "1.3.6.1.4.1.31130.200.27";
 	public static String DATA_CENTER_LATENCY_OID = "1.3.6.1.4.1.31130.200.28";
 	public static String DATA_CENTER_LATENCY_TRESHOLD_OID = "1.3.6.1.4.1.31130.1.25";
 	public static int LATENCY_RISING_TRAP = 27;
 	public static int LATENCY_FALLING_TRAP = 28;
 	
 	public SNMPHandler(Configuration config) {
 		this.config = config;
 	}
 	
 	public void handleAlarm(Alarm alarm) {
 		try {
 			sendSNMPMessage(alarm, SNMP_VERSION.V1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void sendSNMPMessage(Alarm alarm, SNMP_VERSION snmpVersion) throws IOException {
 		if (snmpVersion == SNMP_VERSION.V1) {
 			sendV1SNMPMessage(alarm);
 		}
 	}
 	
 	public void sendV1SNMPMessage(Alarm alarm) throws IOException {
 		long dt = Math.abs(alarm.localTimestamp() - alarm.remoteDataCenterTimestamp());
		if (dt > 4294967296l) {
			dt =  999999999l;
		}
 //		int dtSeconds = (int)(dt/1000);
 //		System.out.println(dt);
 //		String alarmText = String.format(DEFAULT_SNMP_TEXT_FMT, alarm.remoteDataCenterId(), 
 //				alarm.localTimestamp()-alarm.remoteDataCenterTimestamp());
 		int trapId = LATENCY_FALLING_TRAP;
 		if (dt > config.getErrorThreshold()) {
 			trapId = LATENCY_RISING_TRAP;
 		}
 		
 		TransportMapping transport = new DefaultUdpTransportMapping();
 		Snmp snmp = new Snmp(transport);
 		String host = String.format("%s/%d",config.getSnmpHostname(), config.getSnmpPort());
 		Address targetHost = GenericAddress.parse(host);
 		
 		CommunityTarget target = new CommunityTarget();
 		target.setCommunity(new OctetString(config.getSnmpCommunity()));
 		target.setAddress(targetHost);
 		target.setVersion(SnmpConstants.version1);
 		
 		PDUv1 pdu = new PDUv1(	);
 		pdu.setType(PDU.V1TRAP);
 
 		pdu.setEnterprise(new OID(ENTERPRISE_OID));
 		pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
 		pdu.setSpecificTrap(trapId);
 		pdu.add(new VariableBinding(new OID(DATA_CENTER_ID_OID), new OctetString(alarm.remoteDataCenterId())));
 		pdu.add(new VariableBinding(new OID(DATA_CENTER_LATENCY_OID), new Gauge32(dt)));
 		pdu.add(new VariableBinding(new OID(DATA_CENTER_LATENCY_TRESHOLD_OID), new Gauge32(config.getErrorThreshold())));
 		
 //		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.31130.1.200.2"), new OctetString(alarmText)));
 		
 		snmp.trap(pdu, target);
 		
 		transport.close();
 	}
 
 //	public void sendV2SNMPMessage(Alarm alarm) throws IOException {
 //		int dt = (int)((alarm.localTimestamp()/1000)-(alarm.remoteDataCenterTimestamp()/1000));
 ////		System.out.println(dt);
 //		String alarmText = String.format(DEFAULT_SNMP_TEXT_FMT, alarm.remoteDataCenterId(), 
 //				alarm.localTimestamp()-alarm.remoteDataCenterTimestamp());
 //		
 //		TransportMapping transport = new DefaultUdpTransportMapping();
 //		Snmp snmp = new Snmp(transport);
 //		String host = String.format("%s/%d",config.getSnmpHostname(), config.getSnmpPort());
 //		Address targetHost = GenericAddress.parse(host);
 //		
 //		CommunityTarget target = new CommunityTarget();
 //		target.setCommunity(new OctetString(config.getSnmpCommunity()));
 //		target.setAddress(targetHost);
 //		
 //		PDU pdu = new PDU();
 //		
 //		pdu.add(new VariableBinding(new OID("1.3.6.1.6.3.1.1.4.1.0"), new OID(".1.3.6.1.4.1.31130.1.99.3"))); // v2c trap id?
 //		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.31130.1.200.1"), new OctetString(alarm.remoteDataCenterId())));
 //		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.31130.1.200.2"), new OctetString(alarmText)));
 //		
 //		snmp.inform(pdu, target);
 //		
 //		transport.close();
 //	}
 	
 }
