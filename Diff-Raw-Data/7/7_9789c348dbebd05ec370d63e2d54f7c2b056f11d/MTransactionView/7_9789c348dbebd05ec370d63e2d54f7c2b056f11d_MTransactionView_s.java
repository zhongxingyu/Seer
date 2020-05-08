 package com.cs174.starrus.view;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.Vector;
 
 import javax.swing.JDialog;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 
 public class MTransactionView extends JDialog implements IView{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static MTransactionView mtsView = null;
 	private JScrollPane scrollPane;
 	private JTable table;
 	
 	private MTransactionView(){
 		this.setLocation(new Point(350, 200));
 		this.setSize(new Dimension(400, 400));
 		this.setPreferredSize(new Dimension(400, 400));
 	    getContentPane().setLayout(null);
 	    
 	   
 	    setView();
 	}
 
 	public static MTransactionView getView() {
 		if(mtsView  == null){
 			mtsView  = new MTransactionView();
 			mtsView .setView();
 		}
 		return mtsView;
 	}
 	public void setView(){
 	    Vector<String> col = new Vector<String>();
 	    col.add("ID");
 	    col.add("Date");
 	    col.add("Amount");
 	    
 	    
 	    /*this is testting date, need to be get rid of later, instead using date from DB*/
 	    Vector<String> first = new Vector<String>();
 	    first.add("1");
 	    first.add("2013/03/09");
 	    first.add("$100");
 	    Vector<Vector<String>> row = new Vector<Vector<String>>();
 	    row.add(first);
 	    //---------------------------------------------------------------------------------
 	    
 	    this.scrollPane = new JScrollPane();
 	    this.scrollPane.setBounds(6, 6, 388, 366);
 	    getContentPane().add(this.scrollPane);
 	    
 	    this.table = new JTable(row, col);
 	    this.table.setPreferredSize(new Dimension(400, 400));
 	    this.scrollPane.setViewportView(this.table);
 	    //this.setVisible(true);
 	}
 	@Override
 	public void present(String model) {
 		// TODO Auto-generated method stub
 		
 	}
 }
