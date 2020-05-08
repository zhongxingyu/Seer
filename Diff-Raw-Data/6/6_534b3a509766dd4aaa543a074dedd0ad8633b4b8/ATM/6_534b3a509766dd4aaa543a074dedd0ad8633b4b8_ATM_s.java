 package com.kaimbe.gimmedadough.atm;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 
 import org.joda.money.Money;
 
 import com.kaimbe.gimmedadough.atm.io.*;
 import com.kaimbe.gimmedadough.atm.physical.*;
 
 public class ATM implements Runnable{
 	private int id;
 	private int branch;
 	private int institution;
 	private InetAddress bankAddress;
 	private ATMController controller;
 	
 	private Logger log;
 	private CardReader cardReader;
 	private CashDispenser cashDispenser;
 	private CustomerConsole customerConsole;
 	private EnvelopeReceiver envelopeReceiver;
 	private BankNetworkManager bankNetworkManager;
 	private ManagementPanel managementPanel;
 	private ReceiptPrinter receiptPrinter;
 	
     private int state;
     private boolean switchOn;
     private boolean cardInserted; 
 
     private static final int OFF_STATE = 0;
     private static final int IDLE_STATE = 1;
     private static final int SERVING_CUSTOMER_STATE = 2;
     
 	public ATM(int id, int branch, int institution, InetAddress bankAddress, ATMController controller) {
 		this.id = id;
 		this.branch = branch;
 		this.institution = institution;
 		this.bankAddress = bankAddress;
 		this.controller = controller;
 		
 		// Create the ATM Logger
 		try {
			Handler handler = new FileHandler("test.log");
 			log = Logger.getLogger("ATM Logging");
			log.addHandler(handler);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		// Create objects corresponding to component parts
 
         bankNetworkManager = new BankNetworkManager(log, bankAddress, controller);
         
         cardReader = new CardReader(this, controller);
         cashDispenser = new CashDispenser(log, controller);
         customerConsole = new CustomerConsole(controller);
         envelopeReceiver = new EnvelopeReceiver(log, controller);
         managementPanel = new ManagementPanel(this, controller);
         receiptPrinter = new ReceiptPrinter(controller);  
     
         // Set up initial conditions when ATM first created
         
         state = OFF_STATE;
         switchOn = false;
         cardInserted = false;  
         
         log.info("ATM Created");
 	}
 
 	@Override
 	public void run() {
 		Session currentSession = null;
         
         while (true)
         {
             switch(state)
             {
                 case OFF_STATE:
                 
                     customerConsole.display("Not currently available");
 
                     synchronized(this)
                     {
                         try
                         { 
                             wait();
                         }
                         catch(InterruptedException e)
                         { }
                     }
                     
                     if (switchOn)
                     {
                         performStartup();
                         state = IDLE_STATE;
                     }
                                             
                     break;
                     
                 case IDLE_STATE:
                 
                     customerConsole.display("Please insert your card");
                     cardInserted = false;
                                         
                     synchronized(this)
                     {
                         try
                         { 
                             wait();
                         }
                         catch(InterruptedException e)
                         { }
                     }       
                     
                     if (cardInserted)
                     {
                         currentSession = new Session(this);
                         state = SERVING_CUSTOMER_STATE;
                     }
                     else if (! switchOn)
                     {
                         performShutdown();
                         state = OFF_STATE;
                     }
                     
                     break;
             
                 case SERVING_CUSTOMER_STATE:
                                     
                     // The following will not return until the session has
                     // completed
                     
                     currentSession.performSession();
                     
                     state = IDLE_STATE;
                     
                     break;
                 
             }
         }
 		
 	}
 	
     public synchronized void switchOn()
     {
         switchOn = true;
         notify();
     }
    
     public synchronized void switchOff()
     {
         switchOn = false;
         notify();
     }
    
     public synchronized void cardInserted()
     {
         cardInserted = true;
         notify();
     }
 
 	private void performStartup() {
 		Money initialCash = managementPanel.getInitialCash();
         cashDispenser.setInitialCash(initialCash);
         bankNetworkManager.openConnection();    
 	}
 	
 	private void performShutdown() {
 		bankNetworkManager.closeConnection();
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @return the branch
 	 */
 	public int getBranch() {
 		return branch;
 	}
 
 	/**
 	 * @return the institution
 	 */
 	public int getInstitution() {
 		return institution;
 	}
 
 	/**
 	 * @return the controller
 	 */
 	public ATMController getController() {
 		return controller;
 	}
 
 	/**
 	 * @return the log
 	 */
 	public Logger getLog() {
 		return log;
 	}
 
 	/**
 	 * @return the cardReader
 	 */
 	public CardReader getCardReader() {
 		return cardReader;
 	}
 
 	/**
 	 * @return the cashDispenser
 	 */
 	public CashDispenser getCashDispenser() {
 		return cashDispenser;
 	}
 
 	/**
 	 * @return the customerConsole
 	 */
 	public CustomerConsole getCustomerConsole() {
 		return customerConsole;
 	}
 
 	/**
 	 * @return the envelopeReceiver
 	 */
 	public EnvelopeReceiver getEnvelopeReceiver() {
 		return envelopeReceiver;
 	}
 
 	/**
 	 * @return the bankNetworkManager
 	 */
 	public BankNetworkManager getBankNetworkManager() {
 		return bankNetworkManager;
 	}
 
 	/**
 	 * @return the bankAddress
 	 */
 	public InetAddress getBankAddress() {
 		return bankAddress;
 	}
 
 	/**
 	 * @return the controlPanel
 	 */
 	public ManagementPanel getControlPanel() {
 		return managementPanel;
 	}
 
 	/**
 	 * @return the receiptPrinter
 	 */
 	public ReceiptPrinter getReceiptPrinter() {
 		return receiptPrinter;
 	}
 }
