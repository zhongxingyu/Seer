 package com.emn.fil.automaticdiscover.snmp;
 
 import java.util.EnumMap;
 import com.emn.fil.automaticdiscover.Main;
 import com.emn.fil.automaticdiscover.ihm.MachineDetail;
 import com.emn.fil.automaticdiscover.ihm.ShowDialog;
 import com.emn.fil.automaticdiscover.snmp.SNMPWalkDialer.Keys;
 
 public class RunSNMP implements Runnable {
 	
 	private SNMPWalkDialer snmp;
 	private String ip;
 	private String community;
 	private String version;
 	private int port;
 	private MachineDetail detail;
 
 	public RunSNMP(MachineDetail detail, String ip, String community, String version, int port) {
 		this.detail = detail;
 		this.ip = ip;
 		this.community = community;
 		this.version = version;
 		this.port = port;
 	}
 	
 	public void run() {
 		try {
 			this.snmp = new SNMPWalkDialer(ip, community, version, port);
 			EnumMap<Keys, Integer> map = snmp.collect();
 			if(detail.isActive()) {
 				detail.getRam().setText(String.valueOf(map.get(Keys.ramCap)) + " MO");
				detail.getProgressBarRAM().setValue(map.get(Keys.ram));
 				detail.getCpu().setText(String.valueOf(map.get(Keys.cpuCor)) + " coeurs");
 				detail.getProgressBarCPU().setValue(map.get(Keys.cpu));
				detail.getPourcentageRAM().setText(map.get(Keys.ram)+ " %");
				detail.getPourcentageCPU().setText(map.get(Keys.cpu) + " %");
 			}
 		} catch (Exception e) {
 			Main.log.trace(e.getMessage());
 			if(detail.isActive()) {
 				detail.getRam().setText("Erreur...");
 				detail.getCpu().setText("Erreur...");
 				ShowDialog dialog = new ShowDialog("Problème lors de la récupération des données RAM" +
 							" et CPU de la mchine distante !\n" + e.getMessage());
 				dialog.setVisible(true);
 			}	
 		}
 	}
 
 }
