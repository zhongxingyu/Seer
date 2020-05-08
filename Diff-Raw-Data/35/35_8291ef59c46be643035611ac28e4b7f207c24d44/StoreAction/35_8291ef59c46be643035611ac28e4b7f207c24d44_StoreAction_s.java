 /*
  * Author: David Rivera
  * Course: CNT 4714 – Fall 2012
  * Assignment title: Program 1 – Event-driven Programming
  * Date: September 9, 2012
  */
 package ucf.cdstore;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 /**
  * Performs the back-end work for the StoreWindow class.
  */
 
 public class StoreAction{
 	
 	private ArrayList<CD> mCDs;
 	
 	private Invoice mInvoice; 
 	
 	private boolean mRead = false;
 	
 	private final float TAX = 0.06f;
 	
 	private final String FILE = "inventory.txt";
 	private final String FILE_OUT = "transaction.txt";
 	private final String DISPLAY_TAX_RATE = "6%";
 	
 	/**
 	 * Constructor, initializes the Invoice and on first run reads in the inventory.
 	 */
 	public StoreAction(){
 		this.mInvoice = new Invoice();
 		
 		if(!mRead){
 			ReadInventory();
 			mRead = true;
 		}
 	}
 	
 	/**
 	 * Adds the current order to the invoices.
 	 * @param mCD, The current CD object.
 	 * @param mQuantity, The amount of that CD the customer wants to buy.
 	 * @param mNumItemsInOrder, The total amount of expected orders for the invoice. 
 	 */
 	public void ProcessOrder(CD mCD,int mQuantity, int mNumItemsInOrder){
 		Order mOrder = new Order();
 		mOrder.setCD(mCD);
 		mOrder.setQuantityForItem(mQuantity);
 		mOrder.setDiscount(Float.valueOf(getDisplayDiscount(mQuantity)));
 		mOrder.setSubtotal(CalculateDiscount(mCD,mQuantity));
 		
 		this.mInvoice.addOrder(mOrder);
 		this.mInvoice.setSubtotal(CalculateDiscount(mCD,mQuantity));
 		this.mInvoice.setGrandtotal(this.mInvoice.getSubtotal() + (this.mInvoice.getSubtotal() * TAX));
 		this.mInvoice.setNumItemsInOrder(mNumItemsInOrder);
 		
 		
 	}
 	
 	/**
 	 * Finds a CD based on the ID.
 	 * @param mID, The ID of the CD.
 	 * @return The CD object found, if not a blank CD object.
 	 */
 	public CD FindCD(int mID){
 		for(CD cd:mCDs){
 			if(cd.getID() == mID){
 				return cd;
 			}
 		}
 		return new CD();
 	}
 	
 	/**
 	 * Sets the CD Info field using a decorator pattern.
 	 * @param mCD, The Current CD object.
 	 * @param mQuantity, The quantity of that CD.
 	 */
     public void SetCDInfo(CD mCD, int mQuantity){
     	mCD.setInfo(String.valueOf(mCD.getID()) +" "+mCD.getName()+" "+String.valueOf(mCD.getPrice() + 
 				" " + String.valueOf(mQuantity) + " " + String.valueOf(getDisplayDiscount(mQuantity))+ "% "+String.valueOf(CalculateDiscount(mCD,mQuantity))));
     }
     
     /**
      * Calculates a subtotal solely for display.
      * @param mCD, The Current CD object.
      * @param mQuantity, The quantity of that CD.
      * @return Subtotal
      */
     public float getDisplaySubtotal(CD mCD, int mQuantity){
     	return mInvoice.getSubtotal() + CalculateDiscount(mCD,mQuantity);
     }
     
     /**
      * Creates a string containing information of all the processed orders.
      * @return Processed orders
      */
     public String getDisplayViewOrder(){
     	String mViewOrders = "";
     	int index = 1;
     	for(Order mOrder: mInvoice.getOrders()){
     		mViewOrders = mViewOrders + String.valueOf(index)+". "+mOrder.getCDItems().getInfo()+"\n";
     		index++;
     	}
     	return mViewOrders;
     }
 	
     /**
      * Writes all of the orders in the object Invoice to a file.
      */
     public void WriteInvoice(){
 		FileWriter mFW = null;
 		BufferedWriter mBW = null;
 		try {
 			mFW = new FileWriter(FILE_OUT,true);
 		} catch (IOException e) {
 			System.out.println("Sorry could not open file for writing!");
 			e.printStackTrace();
 		}
 		mBW = new BufferedWriter(mFW);
 		
 		for(Order mOrder: this.mInvoice.getOrders()){
 			try {
 				mBW.write(this.mInvoice.getTransactionStamp()+", "+mOrder.getCDItems().getID()+", "
 						+mOrder.getCDItems().getName()+", "+mOrder.getCDItems().getPrice()+", "
 						+mOrder.getQuantityForItem()+", "+mOrder.getDiscount()+", "+mOrder.getSubtotal()+", "
 						+this.mInvoice.getTimeStamp());
 				mBW.newLine();
 			} catch (IOException e) {
 				System.out.println("Sorry could not write to file!");
 				e.printStackTrace();
 			}
 		}
 		try {
 			mBW.close();
 			mFW.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}    
     
     /**
      * Creates a string of all the Invoice information to be displayed in a message dialog.
      * @return Invoice information
      */
     public String DisplayInvoice(){
     	this.mInvoice.setDate();
     	String mMessage = "Date: ";
     	
     	mMessage = mMessage + this.mInvoice.getTimeStamp() + "\n\n";
     	mMessage = mMessage + "Number of line items: " + this.mInvoice.getNumItemsInOrder() + "\n\n";
     	mMessage = mMessage + "Item# / ID / Title / Price / Qty / Disc % / Subtotal:\n\n";
    	mMessage = mMessage + getDisplayViewOrder() + "\n\n\n\n";
     	mMessage = mMessage + "Order subtotal: " + this.mInvoice.getSubtotal() + "\n\n";
     	mMessage = mMessage + "Tax rate:    " + DISPLAY_TAX_RATE + "\n\n";
     	mMessage = mMessage + "Tax amount:    $" + (this.mInvoice.getSubtotal() * TAX) + "\n\n";
     	mMessage = mMessage + "Order total:    " + this.mInvoice.getGrandtotal() + "\n\n";
     	mMessage = mMessage + "Thank you for shopping at the CD store! \n\n";
     	
     	return mMessage;
     }
 	
     /**
      * Determines the discount amount based on the quantity of the item ordered; solely for display. 
      * @param mQuantity
      * @return Discount amount
      */
     private int getDisplayDiscount(int mQuantity){
     	if(mQuantity < 5){
 			return 0;
 		}else if(mQuantity < 10){
 			return 10;
 		}else if(mQuantity < 15){
 			return 15;
 		}else if(mQuantity >= 15){
 			return 20;
 		}else return 0;
     }
 	
     /**
      * Determines the discounted subtotal. 
      * @param mCD
      * @param mQuantity
      * @return Discounted subtotal
      */
     private float CalculateDiscount(CD mCD, int mQuantity){
 		float mSubtotal = mCD.getPrice() * (float) mQuantity;
 		
 		if(mQuantity < 5){
 			return mSubtotal;
 		}else if(mQuantity < 10){
 			return mSubtotal - (mSubtotal * 0.1f);
 		}else if(mQuantity < 15){
 			return mSubtotal - (mSubtotal * 0.15f);
 		}else if(mQuantity >= 15){
 			return mSubtotal - (mSubtotal * 0.2f);
 		}else return 0f;
 	}
 	
     /**
      * Reads in the inventory from a file into CD objects and stores them in an ArrayList.
      */
     private void ReadInventory(){
 		//Read in the inventory to the cd object
 		FileReader mFR = null;
 		BufferedReader mBR = null;
 		mCDs = new ArrayList<CD>();
 		try {
 			mFR = new FileReader(FILE);
 			
 		} catch (FileNotFoundException e) {
 			JOptionPane mBox = new JOptionPane("Waning could not open file for reading!",JOptionPane.ERROR_MESSAGE);
 			mBox.setVisible(true);
 			e.printStackTrace();
 		}	
 		
 		try {
 			mBR = new BufferedReader(mFR);
 			
 			String mLine;
 			while((mLine = mBR.readLine()) != null){
 				String[] mCurrentCD = new String[3];
 				CD mCD = new CD();
 				
 				mCurrentCD = mLine.split(",", 3);
 				mCD.setID(Integer.valueOf(mCurrentCD[0]));
 				mCD.setName(mCurrentCD[1]);
 				mCD.setPrice(Float.valueOf(mCurrentCD[2]));
 				
 				mCDs.add(mCD);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
