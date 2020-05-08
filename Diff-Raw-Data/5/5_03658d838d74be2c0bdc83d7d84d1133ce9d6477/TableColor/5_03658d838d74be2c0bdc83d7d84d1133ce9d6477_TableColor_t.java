 package edu.brown.cs32.atian.crassus.gui.mainwindow.table;
 
 import java.awt.Color;
 
 import edu.brown.cs32.atian.crassus.backend.StockEventType;
 
 public class TableColor {
 
 	public final static Color BUY = new Color(170,230,170);
 	public final static Color BUY_INACTIVE = new Color(200,240,200);
 	public final static Color SELL = new Color(255,150,150);
 	public final static Color SELL_INACTIVE = new Color(255,210,210);
 	public final static Color CONFLICT = new Color(255,255,0);
 	public final static Color NONE = Color.white;
 	
 	public static Color getColor(StockEventType state){
		if(state==null)
			return NONE;
 		switch(state){
 		case BUY:
 			return BUY;
 		case SELL:
 			return SELL;
 		case CONFLICT:
 			return CONFLICT;
 		case NONE:
 		default:
 			return NONE;
 		}
 	}
 	
 	public static Color getColor(StockEventType state, boolean isActive){
		if(state==null)
			return NONE;
 		if(isActive)
 			return getColor(state);
 		
 		switch(state){
 		case BUY:
 			return BUY_INACTIVE;
 		case SELL:
 			return SELL_INACTIVE;
 		case NONE:
 		default:
 			return NONE;
 		}
 	}
 	
 	
 }
