 package control.listeners;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import model.AbstractAddress;
import model.AddressList;
 import view.AbstractAddressView;
 import view.AddressListView;
 
 
 public class SpeicherButtonActionListener implements ActionListener {
 	
 	protected AddressListView alv;
 	protected AbstractAddressView av;
 	protected AbstractAddress address;
 	
 	
 	public SpeicherButtonActionListener(AddressListView alv, AbstractAddressView av){
 		this.alv = alv;
 		this.av = av;
 		this.address = av.getAddress();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		System.out.println("SpeicherButtonListener: saving Address to AddressList");
 		// get the input from the AddressView and
 		// add the Address to the AddressList
 		av.retrieveFields();
		
		/*
 		if(address == null)
 			System.out.println("a ist null");
		*/
 		System.out.println(address.toString());
		AddressList al = AddressList.getInstance();
		al.add(address);
 
 		// close the Window
 		Container container = (Container) e.getSource();
 		while (!(container instanceof JFrame)) {
 			container = container.getParent();
 		}
 		((JFrame) container).setVisible(false);
 		((JFrame) container).dispose();
 	}
 
 }
