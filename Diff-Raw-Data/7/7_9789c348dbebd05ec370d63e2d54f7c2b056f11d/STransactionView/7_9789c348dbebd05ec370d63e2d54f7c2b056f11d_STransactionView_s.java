 package com.cs174.starrus.view;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.Vector;
 import javax.swing.JDialog;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 
 
 public class STransactionView extends JDialog implements IView{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static STransactionView stsView = null;
 	private JScrollPane scrollPane;
 	private JTable table;
 	Vector<Vector<String>> row;
 	
 	private STransactionView(){
 		this.setLocation(new Point(350, 200));
 		this.setSize(new Dimension(400, 400));
 		this.setPreferredSize(new Dimension(400, 400));
 	    getContentPane().setLayout(null);
 	    
 	   
 	    setView();
 	}
 
 	public static STransactionView getView() {
 		if(stsView  == null){
 			stsView  = new STransactionView();
 			stsView .setView();
 		}
 		return stsView;
 	}
 	public void setView(){
 	    Vector<String> col = new Vector<String>();
 	    col.add("ID");
 	    col.add("Symbo");
 	    col.add("Date");
 	    col.add("Quantity");
 	    col.add("Price");
 	    
 	    
 	    /*this is testting date, need to be get rid of later, instead using date from DB*/
 	    Vector<String> first = new Vector<String>();
 	    first.add("1");
 	    first.add("GOO");
 	    first.add("2013/03/09");
 	    first.add("100");
 	    first.add("$2.13");
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
 	
 	
 	
 	public Vector<Vector<String>> getRow() {
 		return row;
 	}
 
 	public void setRow(Vector<Vector<String>> row) {
 		this.row = row;
 	}
 
 	@Override
 	public void present(String model) {
 		// TODO Auto-generated method stub
 	}
 	
 }
