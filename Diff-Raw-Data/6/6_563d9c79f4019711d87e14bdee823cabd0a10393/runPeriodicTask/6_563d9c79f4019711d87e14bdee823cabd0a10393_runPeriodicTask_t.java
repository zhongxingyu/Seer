 import java.sql.Timestamp;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 
 /**
  * Periodically fetch stock data.
  */
 
 public class runPeriodicTask {
 
 	private static String emailAddress;
 	private static String emailPassword;
 	
     public static boolean canDrawChart=false;
     public static boolean canEmail=false;
         
 	public static drawChart stockChart;
 
 	public static String pathName="";
 	public static boolean isRunning=false;
 	
 	public static Gui gui;
 	public static StockLinkedList stockList = new StockLinkedList();
 	public static int instanceNum=0;
 	
 	static protected boolean quit=false;
 	//Toolkit toolkit;
 	static boolean debug=false;
 
 	static final String myOS = getOS.getOsName();
 	static Timer timer;
 	static FetchData fetcher = new FetchData();
 	static FileIO fio = new FileIO();
     
     public static void main(String[] arg) {
     	
     	// check libraries
     	checkLibraries();
         
     	gui = new Gui();
     	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     	gui.setSize(350, 210); // (x,y)
     	gui.setVisible(true);    	
     }
 		
 	public runPeriodicTask(int refreshRate, String tickerSymbol, double hi, double lo, String emailAddr, String pwd) {
 		
 		//set email address and password
 		emailAddress = emailAddr;
 		emailPassword = pwd;
 		
 		instanceNum = getInstanceNum(tickerSymbol);		
 		stockList.addStock(tickerSymbol, lo, hi, instanceNum);
 		
 	    // toolkit = Toolkit.getDefaultToolkit();
 		if (!isRunning) {
 			
 			timer = new Timer();
 			isRunning = true;
 			
 			if (myOS.equals("windows")) {
 				pathName = "C:\\PSU\\cs510_OpenSource\\";	        		        	
 	        } else if (myOS.equals("linux")) {
 	        	pathName = "//u//agospodn//cs510_OpenSource//";
 	        } else if (myOS.equals("mac")){
 				pathName = "//users//matthewwolfe//Documents//";	        					
 	        }	else {
 	        	System.out.println(myOS + " operating system not supported.");
 	        }
 			timer.schedule(new periodicMethod(), 0, //initial delay
     		        refreshRate * 1000); //subsequent rate
 			
 		// if it's running, check if any stock ticker symbols are active, if not then quit
 		} else if (isRunning) {
 			if (!stockList.HasActiveInstanceNum()) {
 				System.out.println("No more active ticker symbols. Quitting...");
 				quit=true;
 			}
 		}
 	}
 
 	public runPeriodicTask(String tickerSymbol, int buttonNum) {
 		
 		/* This is called when the runPeriodicTask has not already been instantiated (only by first clicking a button other than "Start"). 
 		 * 	The buttonNum is the code for the following buttons:
 		 *		2 - Remove
 		 *		5 - Draw Chart
 		 *  These methods check to see if the ticker symbol is valid, and since runPeriodicTask is not yet running, 
 		 *  the tickerSymbol is guaranteed not to be in and so a message is written to the console.	
  		 */
 		
 		if (buttonNum == 2) {
 			disableTickerSymbol(tickerSymbol);
 		} else if (buttonNum == 5) {
 				if (canDrawChart) {
 					drawChart(tickerSymbol);
 				} else {System.out.println("can't find chart library.\nDownload the library at:\n\thttp://www.java2s.com/Code/JavaDownload/jfreechart-1.0.0-rc1.zip");} 
 		}	
 	}
 	
 	class periodicMethod extends TimerTask {
 	    
 		int cnt = 0;
 		
 		public void run() {						
 
 			java.util.Date date = new java.util.Date();
 
 			StockNode tempNode;
 			String tempLastTrade="";
 
 			if (debug) {System.out.print("quit = " + quit);}			
 			if (quit) {System.out.print("Stopped.");	System.exit(0);}
 			
 			for (int i=0; i<stockList.getNodeCount(); i++) {
 			
 				tempNode = stockList.getNodeAt(i);
 				
 				if (tempNode.getInstanceNum()>0) {
 				
 					//System.out.print(cnt + " ");
 					
 					tempLastTrade = fetcher.getLastTrade(tempNode.getTickerSymbol());
 					tempNode.setLastTrade(tempLastTrade);
 					  
 					  
 					gui.setTickerandPrice(tempNode.getTickerSymbol(), tempLastTrade);			
 					System.out.print("\n" + new Timestamp(date.getTime()) + ", " + 
 											tempNode.getTickerSymbol()+": " + tempLastTrade + 
 											" [" + tempNode.getInstanceNum() + "]");		// instance number
 					
 					fio.writeToFile(
 							pathName + tempNode.getTickerSymbol() + ".txt", 
 							tempLastTrade);				
 					
 					testThresholds(tempNode);
 				}
 			}
 			cnt++;
 		}
 	}
 	
 	public void testThresholds(StockNode _stockNode) {
 
 		if (Double.parseDouble(_stockNode.getLastTrade()) < _stockNode.getThreshLow()) {
 			System.out.println("*** " + _stockNode.getTickerSymbol() + " is lower than low threshold. ***");
 
 			// send email
 			if (!_stockNode.getEmailed()) {		// if it hasn't been emailed
 				System.out.println("send email...");
 
 				if (canEmail) {					
 					String tickerSymb=_stockNode.getTickerSymbol(), lastTrade=_stockNode.getLastTrade();
 					new Email(emailAddress, emailPassword.toString(),
 							"Stocker Alert: " + tickerSymb + " is LOW (" + lastTrade + ")",
 		                    "Last trade (low): " + lastTrade + 
 		                    "\nlow threshold:    " + _stockNode.getThreshLow() + 
 		                    "\n\n*** Automatically generated message from Stocker. ***");
 				} else {
 					System.out.println("JavaMail library not installed. Unable to send email.");
 				}
 	               
 				_stockNode.setEmailed(true);	// set emailed in node to true
 			}
 		
 		} else if (Double.parseDouble(_stockNode.getLastTrade()) > _stockNode.getThreshHigh()) {
 			System.out.println("*** " + _stockNode.getTickerSymbol() + " is higher than high threshold. ***");
 
 			// send email
 			if (!_stockNode.getEmailed()) {		// if it hasn't been emailed
 				System.out.println("send email...");
 				
 				if (canEmail) {
 					String tickerSymb=_stockNode.getTickerSymbol(), lastTrade=_stockNode.getLastTrade();
 					new Email(emailAddress, emailPassword.toString(),
 							"Stocker Alert: " + tickerSymb + " is HIGH (" + lastTrade + ")",
 		            		"Last trade (high): " + lastTrade + 
		                    "\nhigh threshold:    " + _stockNode.getThreshHigh() + 
 		                    "\n\n*** Automatically generated message from Stocker. ***");				
 				} else {
 					System.out.println("JavaMail library not installed. Unable to send email.");
 				}
 				
 				_stockNode.setEmailed(true);	// set emailed in node to true
 			}
 
 		} else {
 			_stockNode.setEmailed(false);	// set emailed in node to false
 		}
 
 	}
 
 	public void disableTickerSymbol(String tickerSymb) {
 		
 		// find the ticker symbol's node
 		StockNode disableNode = stockList.findTickerSymb(tickerSymb);
 
 		// if the ticker symbol is found, multiply its instanceNum by -1 // 0 (inactive)
 		if (disableNode != null) {
 			
     		StockNode currNode = stockList.findTickerSymb(tickerSymb); 
 			
     		if (currNode.getInstanceNum() < 0) {
     			System.out.println(tickerSymb + " is already inactive.");
     		} else {    			
     			// set to -1*instance number (deactivate)
     			currNode.setInstanceNum(-1*Math.abs(currNode.getInstanceNum()));				
     			System.out.println("Removed ticker symbol " + tickerSymb);
 			}			
 			
 			// if no nodes are active, quit
 			if (!stockList.HasActiveInstanceNum()) {
 				System.out.println("No more active ticker symbols. Quitting...");
 				quit=true;
 			}
 				
 		} else {
 			System.out.println(tickerSymb + " is not a ticker symbol being stocked.");
 		}
 	}
 
 	
 	/**
 	 * getInstanceNum takes the ticker symbol as an argument and returns
 	 * 		the static variable instanceNum incremented by 1.
 	 * 	    OR
 	 * 		the instanceNum if the ticker symbol has already been added
 	 * @param tickerSymb
 	 * @return instanceNum
 	 */
 	public int getInstanceNum(String tickerSymb) {
 		
 		if (stockList.findTickerSymb(tickerSymb)==null) {
 			// System.out.println("instance num = " + instanceNum);
 			return instanceNum+1;
 		} else {
 			// System.out.println("instance num = " + instanceNum);			
 			return stockList.findTickerSymb(tickerSymb).getInstanceNum();		
 		}
 	}
 	
 	public void drawChart(String tickerSymbol) {
 		
 		StockNode tempNode = stockList.findTickerSymb(tickerSymbol);
 		// if ticker symbol is in list, then draw chart 
 		if (tempNode != null) {		
 			System.out.println("tickerSymbol = " + tickerSymbol);
 			System.out.println("low = " + tempNode.getThreshLow());
 			System.out.println("high = " + tempNode.getThreshHigh());
 			
 			stockChart = new drawChart(tickerSymbol, pathName, tempNode.getThreshLow(), tempNode.getThreshHigh());
 		} else {		
 			System.out.println(tickerSymbol + " not found.");
 		}
 	}
 	
 	public static void checkLibraries() {
 		
 	    if (System.getProperty("java.class.path",null).contains("jcommon") &&
 	       (System.getProperty("java.class.path",null).contains("jfreechart"))) {
 	            System.out.println("JCommon and JFreeChart are installed");
 	            canDrawChart=true;
 	    } else if (!System.getProperty("java.class.path",null).contains("jcommon")) {
 	        System.out.println("JCommon is not installed.  Refer to the documentation for information on how to add the JCommon library.");
 	    } else if (!System.getProperty("java.class.path",null).contains("jfreechart")) {
 	        System.out.println("JFreeChart is not installed.  Refer to the documentation for information on how to add the JFreeChart library.");
 	    }
 	    
 	    if (System.getProperty("java.class.path",null).contains("javamail")) {
 	        System.out.println("JavaMail is installed");
 	        canEmail=true;
 	    } else {
 	        System.out.println("JavaMail is not installed.  Refer to the documentation for information on how to add the JavaMail library.");
 	    }
 	}
}
