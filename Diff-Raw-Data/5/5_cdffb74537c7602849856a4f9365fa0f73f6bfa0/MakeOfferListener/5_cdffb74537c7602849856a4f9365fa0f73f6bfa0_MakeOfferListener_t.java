 package gui;
 
 import java.awt.Container;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JOptionPane;
 
 
 import app.Command;
 
 public class MakeOfferListener extends MouseAdapter {
 
 	private Command comm;
 	private int row, index;
 	private Container parent;
 
 
 	public MakeOfferListener(Command comm, int row, int index, Container parent) {
 		super();
 		this.comm = comm;
 		this.row = row;
 		this.index = index;
 	}
 
 	private void errorDialog() {
 		JOptionPane.showMessageDialog(parent, "Incorrect price! Aborting.", 
 			    "Make offer error",
 			    JOptionPane.PLAIN_MESSAGE);	
 	}
 	
 	public void mousePressed(MouseEvent e) {
 		String price = JOptionPane.showInputDialog(parent,"Enter the price:", -1);
 		if (price == null)
 			return;
 		if (!price.equals("")) {
 			try {
 				int offerredPrice = Integer.parseInt(price);
				if (offerredPrice < 0)
					errorDialog();
				else
					comm.execute(row, offerredPrice, index);
 			} catch(java.lang.NumberFormatException exc) {
 				exc.printStackTrace();
 				errorDialog();
 			}
 		}
 		else
 			errorDialog();
 			
 	}
 }
