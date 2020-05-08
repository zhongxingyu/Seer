 import javax.swing.*;
 
 import org.xbill.DNS.ResolverConfig;
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.net.URLEncoder;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Semaphore;
 
 /**
  * 
  */
 
 /**
  * @author Bob Novas
  *
  */
 public class DRC_App  extends javax.swing.JFrame 
 		implements ActionListener {
 	
 	protected JTextField messageField = null;
 	protected JTextField addressField = null;
 	protected static final String identifying_string = "id_string";
 	protected static final String ip_address_string = "ip_address";
 	//protected DefaultListModel<Behavior> resultsListModel = null;
 	protected DefaultListModel resultsListModel = null;
 	//protected JList<Behavior> resultsList = null;
 	protected JList resultsList = null;
 	protected final Semaphore available = new Semaphore(1, true);
 	protected String help_link = null;
 
 	protected SwingWorker<String, Void> getWorker(final int theModelIndex, final String the_ip_address, final String the_message) {
 
 		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
 
 			protected int inx = theModelIndex;
 			protected String ip_address = the_ip_address;
 			protected String message = the_message + " DRC_App";
 			
 			@Override
 			public String doInBackground() throws InterruptedException {
 				available.acquire();
 				String g = "";
 				String tr = "";
 		        DNSSEC_resolver_check check = new DNSSEC_resolver_check();
 				try {
 				    g = check.evaluate_resolver(ip_address, URLEncoder.encode(message, "UTF-8")); 
 				}
 				catch (Exception exc) {
 				    System.err.println("Exception: " + exc);
 				    g = "Failure: " + exc.getMessage();
 				}
 				available.release();
 				if (g != null) {
 				    tr = new Translator().translate(g);
 				    return tr + "  (" + g + ")";
 				} else {
 					return "Failed";				
 				}
 			}
 			
 			@Override
 			public void done() {
 				try {
 					String results = get();
 					Behavior b = ((Behavior) resultsListModel.elementAt(inx));
 					b.setBehavior(results);
 					resultsListModel.set(inx, b);
 					resultsList.ensureIndexIsVisible(inx);
 					addressField.selectAll();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 		return worker;
 	}
 	
 	protected class Behavior {
 		private Boolean intro = false;
 		private Boolean isLocal = null;
 		private String ipAddress = null;
 		private String behavior = null;
 		
 		public Behavior() {
 			this.intro = true;
 		}
 		public Behavior(Boolean local, String ipAddress) {
 			this.intro = false;
 			this.isLocal = local;
 			this.ipAddress = ipAddress;
 		}
 		public Boolean getIsLocal() {
 			return this.isLocal;
 		}
 		public String getIpAddress() {
 			return this.ipAddress;
 		}
 		public String getBehavior() {
 			return this.behavior;
 		}
 		public void setBehavior(String behavior) {
 			this.behavior = behavior;
 		}
 		public String toString() {
 			
 			if (intro == true) {
 				return getAppletInfo().split("\n")[0];
 			} 
 
 			String resolverType = isLocal? "Local " : "";
 			if (behavior == null) {
 				return "Checking " + resolverType + " resolver at " + ipAddress;
 			} else {
 				return "" + resolverType + "Resolver at " + ipAddress + ":  " + behavior;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6070152429809976637L;
 
 	public static void main (String[] args) {
 		final DRC_App theApp = new DRC_App();
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				theApp.InitComponents();
 			}
 		});
 	}
 	
 	private void InitComponents() {
 		JFrame frame = new JFrame("DNSSEC Resolver Check");
 		frame.setLayout(new GridBagLayout());
 		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		JLabel commentLabel = new JLabel("Enter an identifying message:");
 		GridBagConstraints c0 = new GridBagConstraints();
 		c0.anchor = GridBagConstraints.FIRST_LINE_START;
 		c0.fill = GridBagConstraints.HORIZONTAL;
 		c0.insets = new Insets(2,10,0,2);
 		c0.gridx = 0;
 		c0.gridy = 0;
 		c0.weightx = 0.1;
 		c0.weighty = 0.1;
 		frame.add(commentLabel, c0);
 		
 		// Create a text field to enter an identifying message
 		messageField = new JTextField(20);
 		messageField.setActionCommand(identifying_string);
 		messageField.addActionListener(this);
 		messageField.setText("<Type a message and hit enter to check local resolvers>");
 		messageField.selectAll();
 		GridBagConstraints c1 = new GridBagConstraints();
 		c1.anchor = GridBagConstraints.FIRST_LINE_START;
 		c1.fill = GridBagConstraints.HORIZONTAL;
 		c1.insets = new Insets(2,2,0,10);
 		c1.gridx = 1;
 		c1.gridy = 0;
 		c1.weightx = 0.9;
 		c1.weighty = 0.1;
 		frame.add(messageField, c1);
 		
 		JLabel labelField = new JLabel("Type IP Address and hit <Enter>:");
 		GridBagConstraints c2 = new GridBagConstraints();
 		c2.anchor = GridBagConstraints.PAGE_START;
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.insets = new Insets(2,10,0,2);
 		c2.gridx = 0;
 		c2.gridy = 1;
 		c2.weightx = 0.1;
 		c2.weighty = 0.1;
 		frame.add(labelField, c2);
 		
 		// Create a text field to enter an IP Address to check
 		addressField = new JTextField(20);
 		addressField.setActionCommand(ip_address_string);
 		addressField.addActionListener(this);
 		GridBagConstraints c3 = new GridBagConstraints();
 		c3.anchor = GridBagConstraints.PAGE_START;
 		c3.fill = GridBagConstraints.HORIZONTAL;
 		c3.insets = new Insets(2,2,0,10);
 		c3.gridx = 1;
 		c3.gridy = 1;
 		c3.weightx = 0.9;
 		c3.weighty = 0.1;
 		frame.add(addressField, c3);
 		
 		//Create a results list to display the results
 		//resultsListModel = new DefaultListModel<Behavior>();
 		resultsListModel = new DefaultListModel();
 		//resultsList = new JList<Behavior>(resultsListModel);
 		resultsList = new JList(resultsListModel);
 		resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane resultsScrollPane = new JScrollPane(this.resultsList);
 		resultsScrollPane.setVerticalScrollBarPolicy(
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		resultsScrollPane.setPreferredSize(new Dimension(500, 145));
 		resultsScrollPane.setMinimumSize(new Dimension(10,10));
 		GridBagConstraints c4 = new GridBagConstraints();
 		c4.anchor = GridBagConstraints.LINE_START;
 		c4.fill = GridBagConstraints.BOTH;
 		c4.gridx = 0;
 		c4.gridwidth = 2;
 		c4.gridy = 2;
 		c4.weightx = 1.0;
 		c4.weighty = 1.0;
 		c4.ipady = 150;
 		c4.insets = new Insets(0,10,2,10);
 		frame.add(resultsScrollPane, c4);
 
 		// put the version info in the model
 		resultsListModel.add(0, new Behavior());
 		
 		frame.pack();
 		frame.setVisible(true);
 	}
 
     public String getAppletInfo() {
         return "DRC App v1.0.0, 19 March 2013.\n"
               + "  Authors: lafur Gumundsson, Bob Novas.\n"
                + "  Checks the DNSSEC Features of DNS Resolvers.";
     }
 
 	public void local() {
 		ResolverConfig.refresh();
 		String list [] = ResolverConfig.getCurrentConfig().servers(); 
 		for( int num = 0; num < list.length; num++) {
 			int i = resultsListModel.getSize();
 			resultsListModel.add(i, new Behavior(true, list[num]));
 			resultsList.ensureIndexIsVisible(i);
 			SwingWorker<String, Void> w = getWorker(i, list[num], this.messageField.getText());
 			w.execute();
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (ip_address_string.equals(e.getActionCommand())) {
 			JTextField source = (JTextField)e.getSource();
 			String ip_address = source.getText();
 			int i = resultsListModel.getSize();
 			resultsListModel.add(i, new Behavior(false, ip_address));
 			resultsList.ensureIndexIsVisible(i);
 			SwingWorker<String, Void> w = getWorker(i, ip_address, messageField.getText());
 			w.execute();
 		} else if (identifying_string.equals(e.getActionCommand())) {
 			// check the local resolvers
 			local();
 		}
 	}
 }
