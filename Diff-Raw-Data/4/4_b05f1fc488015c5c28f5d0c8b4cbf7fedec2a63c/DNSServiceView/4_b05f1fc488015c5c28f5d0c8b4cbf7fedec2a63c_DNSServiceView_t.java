 package org.bitducks.spoofing.gui.serviceView;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.gui.View;
 import org.bitducks.spoofing.services.DNSService;
 
 public class DNSServiceView extends View implements ActionListener{
 	
 	public static final String ADD_REGEX = "Add";
 	public static final String REMOVE_REGEX = "Remove";
 	public static final String CHANGE_FALSE_IP = "Default DNS IP";
 	
 	public static final String WARNING_NO_ENTRY = "Invalid operation: ':' is prohibited.";
 	public static final String ENTER_DNS_NAME = "Enter the DNS name you want to add rule.";
 	public static final String ENTER_IP_ADDR = "Enter the IP address you want to redirect the user to.";
 	public static final String ERROR_BAD_IP = "Invalid operation: The Ip address you have provided is not valid.";
 	public static final String ERROR_BAD_CHARACTER = "Invalid operation: ':' is prohibited.";
 	public static final String TITLE = "DNS Poisoning";
 	
 	private JList<String> jlist;
 	private DefaultListModel<String> modelList = new DefaultListModel<String>();
 	private Map<String, InetAddress> dnsPacketFilter = new HashMap<String, InetAddress>();
 	private InetAddress defaultFalseIp = null;
 	
 	private JButton addRegex = new JButton();
 	private JButton removeRegex = new JButton();
 	//private JButton changeFalseIp = new JButton();
 	
 	
 
 	public DNSServiceView() {
 		super( DNSServiceView.TITLE );
 		
 		setUpServicePanel();
 		
 		this.addDefaultFilter(Server.getInstance().getInfo().getDeviceAddress().address);
 	}
 	
 	private void addDefaultFilter(InetAddress addr) {
 		dnsPacketFilter.put(".*", addr);
 		this.refreshList();
 	}
 	
 	public void setUpServicePanel() {
 		
 		this.servicePanel.setLayout(new BorderLayout());
 		
 		
 		jlist = new JList<String>(modelList);
 		this.servicePanel.add(jlist, BorderLayout.CENTER);
 		
 		JScrollPane scroll = new JScrollPane(this.jlist);
 		this.servicePanel.add(scroll, BorderLayout.CENTER);
 		
 		this.setUpButton();
 	}
 	
 	public void setUpButton() {
 		JPanel pan = new JPanel();
 		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
 		
 		this.addRegex.setText(DNSServiceView.ADD_REGEX);
 		this.addRegex.addActionListener(this);
 		this.addRegex.setActionCommand(DNSServiceView.ADD_REGEX);
 		pan.add(this.addRegex);
 		
 		pan.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		this.removeRegex.setText(DNSServiceView.REMOVE_REGEX);
 		this.removeRegex.addActionListener(this);
 		this.removeRegex.setActionCommand(DNSServiceView.REMOVE_REGEX);
 		pan.add(this.removeRegex);
 		
 		/*pan.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		this.changeFalseIp.setText(DNSServiceView.CHANGE_FALSE_IP);
 		this.changeFalseIp.addActionListener(this);
 		this.changeFalseIp.setActionCommand(DNSServiceView.CHANGE_FALSE_IP);
 		pan.add(this.changeFalseIp);*/
 		
 		this.servicePanel.add(pan, BorderLayout.LINE_END);
 		
 	}
 	
 	private void addToList(String regex, String ipAddr) throws UnknownHostException {
 		if (regex.contains(":")) {
 			// We do not accept the : character
 			JOptionPane.showMessageDialog(null, DNSServiceView.ERROR_BAD_CHARACTER);
 			return ;
 		}
 		
 		InetAddress addr = InetAddress.getByName(ipAddr);
 		
 		this.dnsPacketFilter.put(regex, addr);
 	}
 	
 	private void removeFromList(String line) throws UnknownHostException {
 		if (this.modelList.getSize() > 1 ) {
 			String splitResult[] = line.split(":");
 			this.dnsPacketFilter.remove(splitResult[0].trim());
 			
 		} else if (JOptionPane.showConfirmDialog(null, DNSServiceView.WARNING_NO_ENTRY) == 0) {
 			String splitResult[] = line.split(":");
 			this.dnsPacketFilter.remove(splitResult[0].trim());
 			
 			InetAddress addr = InetAddress.getByName(JOptionPane.showInputDialog(DNSServiceView.ENTER_IP_ADDR, Server.getInstance().getInfo().getDeviceAddress().address.getHostAddress()));
 			this.addDefaultFilter(addr);
 		}
 		
 
 	}
 	
 	private void refreshList() {
 	
 		this.modelList.clear();
 		
 		Iterator<String> it = this.dnsPacketFilter.keySet().iterator();
 		// Checking all the filter
 		while (it.hasNext()) {
 			String next = it.next();
 			this.modelList.addElement(next + "   :   " + this.dnsPacketFilter.get(next).getHostAddress());
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		super.actionPerformed(e);
 		
 		String regex = "";
 		String ipAddr = "";
 		
 		switch (e.getActionCommand()) {
 		
 		case DNSServiceView.ADD_REGEX:
 			if ((regex = JOptionPane.showInputDialog(DNSServiceView.ENTER_DNS_NAME))
 					== null) {
 				break;
 			}
 			if ((ipAddr = JOptionPane.showInputDialog(DNSServiceView.ENTER_IP_ADDR,
 					Server.getInstance().getInfo().getDeviceAddress().address.getHostAddress()))
 					== null) {
 				break;
 			}
 			
 			try {
 				this.addToList(regex, ipAddr);
 				if (this.service != null && this.service instanceof DNSService) {
 					((DNSService)service).addDnsPacketFilter(regex, InetAddress.getByName(ipAddr));
 				}
 			} catch (UnknownHostException e1) {
 				JOptionPane.showMessageDialog(null, DNSServiceView.ERROR_BAD_IP);
 			}
 			
 			this.refreshList();
 			
 			break;
 			
 		case DNSServiceView.REMOVE_REGEX:
 			regex = this.jlist.getSelectedValue();
 			if (regex != null) {
 				try {
 					this.removeFromList(regex);
 					if (this.service != null && this.service instanceof DNSService) {
 						((DNSService)service).removeDnsPacketFilter(regex);
 					}
 				} catch (UnknownHostException e1) {
 					JOptionPane.showMessageDialog(null, DNSServiceView.ERROR_BAD_IP);
 				}
 			}
 			this.refreshList();
 			break;
 			
 		/*case DNSServiceView.CHANGE_FALSE_IP:
 			
 			if (this.defaultFalseIp != null) {
 				ipAddr = JOptionPane.showInputDialog(DNSServiceView.ENTER_IP_ADDR, this.defaultFalseIp.getHostAddress());
 			} else {
 				ipAddr = JOptionPane.showInputDialog(DNSServiceView.ENTER_IP_ADDR, Server.getInstance().getInfo().getDeviceAddress().address.getHostAddress());
 			}
 			
 			try {
 				this.removeFromList("*: ");
 				this.addToList("*", ipAddr);
 				this.defaultFalseIp = InetAddress.getByName(ipAddr);
 			} catch (UnknownHostException e1) {
 				JOptionPane.showMessageDialog(null, DNSServiceView.ERROR_BAD_IP);
 			}
 			
 			this.refreshList();
 			break;*/
 		}
 		
 	}
 
 	@Override
 	protected Service createService() {
 		DNSService service = new DNSService();
 		
 		Iterator<String> it = this.dnsPacketFilter.keySet().iterator();
 		// Checking all the filter
 		while (it.hasNext()) {
 			String next = it.next();
 			service.addDnsPacketFilter(next, this.dnsPacketFilter.get(next));
 		}
 		
 		/*if (this.defaultFalseIp != null) {
 			service.setDNSFalseIp(this.defaultFalseIp);
 		}*/
 
 		return service;
 	}
 }
