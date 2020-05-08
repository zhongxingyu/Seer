 package org.server.panels;
 
 import java.awt.BorderLayout;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.JButton;
 import javax.swing.ListSelectionModel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 
 import org.common.accounts.Account;
 import org.common.accounts.Accounts;
 import org.common.atms.Atm;
 import org.common.atms.Atms;
 import org.server.Server;
 import org.server.dialogs.AccountAdd;
 import org.server.dialogs.AtmAdd;
 import org.server.dialogs.AtmBalance;
 
 public class MainForm {
 
 	public JFrame frmBankServer;
 	
 	private JButton accountsBtnAdd, accountsBtnEdit, accountsBtnRemove;
 	private JButton atmsBtnAdd, atmsBtnEdit, atmsBtnRemove;
 	
 	private DefaultListModel<String> accountsListModel = new DefaultListModel<String>();
 	private DefaultListModel<String> atmsListModel = new DefaultListModel<String>();
 	private JList<String> accountsList, atmsList;
 
 	public MainForm() {
 		initialize();
	}	
 
 	private void initialize() {
 		frmBankServer = new JFrame();
 		frmBankServer.setTitle("Bank - Server side");
 		frmBankServer.setResizable(false);
 		frmBankServer.setBounds(100, 100, 640, 480);
 		frmBankServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmBankServer.getContentPane().setLayout(null);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(10, 11, 614, 431);
 		frmBankServer.getContentPane().add(tabbedPane);
 		
 		
 		/** ACCOUNTS **/
 		JPanel accountsPanel = new JPanel();
 		tabbedPane.addTab("Accounts", null, accountsPanel, null);
 		tabbedPane.setEnabledAt(0, true);
 		accountsPanel.setLayout(null);
 		
 		JToolBar accountsToolBar = new JToolBar();
 		accountsToolBar.setBounds(0, 0, 609, 23);
 		accountsToolBar.setFloatable(false);
 		accountsPanel.add(accountsToolBar);
 		
 		accountsBtnAdd = new JButton("Add");
 		accountsBtnAdd.addMouseListener(addAccount());
 		accountsToolBar.add(accountsBtnAdd);
 		
 		accountsBtnEdit = new JButton("Edit");
 		accountsBtnEdit.addMouseListener(editAccount());
 		accountsToolBar.add(accountsBtnEdit);
 		
 		accountsBtnRemove = new JButton("Remove");
 		accountsBtnRemove.addMouseListener(removeAccount());
 		accountsToolBar.add(accountsBtnRemove);
 		
 		JPanel accountsListPanel = new JPanel();
 		accountsListPanel.setBounds(10, 34, 190, 358);
 		accountsPanel.add(accountsListPanel);
 		accountsListPanel.setLayout(new BorderLayout(0, 0));
 
 		loadAccounts(false);		
 		
 		accountsList = new JList<String>(accountsListModel);
 		accountsList.addListSelectionListener(accountsListSelectionChanged());
 		accountsList.setValueIsAdjusting(true);
 		accountsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		accountsList.setSelectedIndex(0);
 		accountsList.setVisibleRowCount(22);		
 		
 		JScrollPane accountsScrollPane = new JScrollPane(accountsList);
 		accountsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		accountsListPanel.add(accountsScrollPane);
 		
 		JPanel accountDetailsPanel = new JPanel();
 		accountDetailsPanel.setBounds(210, 34, 389, 358);
 		accountsPanel.add(accountDetailsPanel);
 
 		
 		
 		/** ATMS **/
 		JPanel atmsPanel = new JPanel();
 		tabbedPane.addTab("Atms", null, atmsPanel, null);
 		atmsPanel.setLayout(null);
 		
 		JToolBar atmsToolBar = new JToolBar();
 		atmsToolBar.setFloatable(false);
 		atmsToolBar.setBounds(0, 0, 609, 23);
 		atmsPanel.add(atmsToolBar);
 		
 		atmsBtnAdd = new JButton("Add");
 		atmsBtnAdd.addMouseListener(addAtm());
 		atmsToolBar.add(atmsBtnAdd);
 		
 		atmsBtnEdit = new JButton("Edit");
 		atmsBtnEdit.addMouseListener(editAtm());
 		atmsToolBar.add(atmsBtnEdit);
 		
 		atmsBtnRemove = new JButton("Remove");
 		atmsBtnRemove.addMouseListener(removeAtm());
 		atmsToolBar.add(atmsBtnRemove);
 		
 		JButton atmsBtnBalance = new JButton("Balance");
 		atmsBtnBalance.addMouseListener(balanceAtm());
 		atmsToolBar.add(atmsBtnBalance);
 		
 		JPanel atmsListPanel = new JPanel();
 		atmsListPanel.setBounds(10, 34, 190, 358);
 		atmsPanel.add(atmsListPanel);
 		atmsListPanel.setLayout(new BorderLayout(0, 0));
 		
 		loadAtms(false);
 		
 		atmsList = new JList<String>(atmsListModel);
 		atmsList.addListSelectionListener(atmsListSelectionChanged());
 		atmsList.setValueIsAdjusting(true);
 		atmsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		atmsList.setSelectedIndex(0);
 		atmsList.setVisibleRowCount(22);		
 		
 		JScrollPane atmsScrollPane = new JScrollPane(atmsList);
 		atmsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		atmsListPanel.add(atmsScrollPane);
 		
 		JPanel atmsDetailsPanel = new JPanel();
 		atmsDetailsPanel.setBounds(210, 34, 389, 358);
 		atmsPanel.add(atmsDetailsPanel);
 		
 		Server.startUpdater();
 	}
 
 	private MouseAdapter balanceAtm() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				try {
 					if (atmsList.getSelectedIndex() != -1) {
 						Server.getAccounts().setBlocked(true);
 						Atm atm = Server.getAtms().getAtm(atmsListModel.getElementAt( atmsList.getSelectedIndex()).toString());
 						AtmBalance dialog = new AtmBalance(atm);
 						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 						dialog.setVisible(true);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}				
 			}
 		};
 	}
 
 	private ListSelectionListener accountsListSelectionChanged() {
 		return new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 		        if (e.getValueIsAdjusting() == false) {
 		        	 
 		            if (accountsList.getSelectedIndex() != -1) {
		            	//System.out.println(accountsListModel.getElementAt( accountsList.getSelectedIndex()).toString() );
 		            }
 		        }
 			}
 		};
 	}
 	
 	private ListSelectionListener atmsListSelectionChanged() {
 		return new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				if (e.getValueIsAdjusting() == false) {
 					
 					if (atmsList.getSelectedIndex() != -1) {
						//System.out.println(atmsListModel.getElementAt( atmsList.getSelectedIndex()).toString() );
 					}
 				}
 			}
 		};
 	}
 
 	private MouseAdapter editAccount() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					if (Server.getAccounts().isBlocked()) return;
 					if (accountsList.getSelectedIndex() != -1) {
 						Server.getAccounts().setBlocked(true);
 						Account account = Server.getAccounts().getAccount(accountsListModel.getElementAt( accountsList.getSelectedIndex()).toString());
 						AccountAdd dialog = new AccountAdd(true, account);
 						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 						dialog.setVisible(true);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}				
 			}
 		};
 	}
 
 	private MouseAdapter removeAccount() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (accountsList.getSelectedIndex() != -1) {
 					if (Server.getAccounts().isBlocked()) return;
 					Server.getAccounts().setBlocked(true);
 					
 					Server.getAccounts().removeAccount(accountsListModel.getElementAt( accountsList.getSelectedIndex()).toString());
 					loadAccounts(false);
 					if ( accountsListModel.size() > 0 ) {
 						accountsList.setSelectedIndex(0);
 					}
 					Server.getAccounts().setBlocked(false);
 				}
 			}
 		};
 	}
 
 	private MouseAdapter removeAtm() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (atmsList.getSelectedIndex() != -1) {
 					if (Server.getAtms().isBlocked()) return;
 					Server.getAtms().setBlocked(true);
 					
 					Server.getAtms().removeAtm(atmsListModel.getElementAt( atmsList.getSelectedIndex()).toString());
 					loadAtms(false);
 					if ( atmsListModel.size() > 0 ) {
 						atmsList.setSelectedIndex(0);
 					}
 					Server.getAtms().setBlocked(false);
 				}				
 			}
 		};
 	}
 
 	private MouseAdapter editAtm() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					if (Server.getAtms().isBlocked()) return;
 					if (atmsList.getSelectedIndex() != -1) {
 						Server.getAtms().setBlocked(true);
 						Atm atm = Server.getAtms().getAtm(atmsListModel.getElementAt( atmsList.getSelectedIndex()).toString());
 						AtmAdd dialog = new AtmAdd(true, atm);
 						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 						dialog.setVisible(true);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}				
 			}
 		};
 	}
 
 	public void loadAtms(boolean checkBlocked) {
 		if (checkBlocked == true && Server.getAtms().isBlocked()) return;
 		atmsListModel.clear();
 		Atms atms = Server.getAtms();
 		if (atms == null || atms.count() == 0) return;
 		for(int i = 0, l = atms.count(); i <= l; i++) {
 			Atm atm = atms.getAtm(i);
 			if (atm == null) continue;
 			atmsListModel.addElement(atm.getId());			
 		}
 	}
 
 	public void loadAccounts(boolean checkBlocked) {
 		if (checkBlocked == true && Server.getAccounts().isBlocked()) return;
 		accountsListModel.clear();
 		Accounts accounts = Server.getAccounts();
 		if (accounts == null || accounts.count() == 0) return;
 		for(int i = 0, l = accounts.count(); i <= l; i++) {
 			Account account = accounts.getAccount(i);
 			if (account == null) continue;
 			accountsListModel.addElement(account.getCardNumber());			
 		}
 	}
 
 	private MouseAdapter addAtm() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					if (Server.getAtms().isBlocked()) return;
 					Server.getAtms().setBlocked(true);
 					AtmAdd dialog = new AtmAdd();
 					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 					dialog.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}				
 			}
 		};
 	}
 	
 	private MouseAdapter addAccount() {
 		return new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				try {
 					if (Server.getAccounts().isBlocked()) return;
 					Server.getAccounts().setBlocked(true);
 					AccountAdd dialog = new AccountAdd();
 					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 					dialog.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 	}
 
 	public void updateAccountDetailsPanel() {
 		
 	}
 
 	public void updateAtmDetailsPanel() {
 		
 	}	
 }
