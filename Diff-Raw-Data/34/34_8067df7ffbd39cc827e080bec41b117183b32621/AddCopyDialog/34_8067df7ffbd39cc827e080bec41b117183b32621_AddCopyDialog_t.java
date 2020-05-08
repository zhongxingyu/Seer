 package UI;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import Transactions.Transactions;
 
 public class AddCopyDialog extends JFrame implements ActionListener {
 
 
 	JTextField callNumber = new JTextField();
 	JTextField copyNo = new JTextField();
	
 	static String returnToLibrarianDialogCommand = "Return to Librarian Dialog";
 	static String add = "Add";
 	
 	public static final int VALIDATIONERROR = 2;
 	
 	public AddCopyDialog(String name)
 	{
 		super (name);
 
 		
 	}
 	
 	private void addComponentsToPane(final Container pane)
 	{
 		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 2));
 		
 		panel.add(new Label("Add a copy of a pre-existing book"));
 		panel.add(new Label(""));
 		
 		panel.add(new Label("Call Number"));
 		panel.add(callNumber);
 		
 		panel.add(new Label("Copy Number"));
 		panel.add(copyNo);
 	
 		
 		JButton returnToUserDialog = new JButton(returnToLibrarianDialogCommand);
 		returnToUserDialog.setActionCommand(returnToLibrarianDialogCommand);
 		returnToUserDialog.addActionListener(this);
 		
 		JButton addButton = new JButton(add);
 		addButton.setActionCommand(add);
 		addButton.addActionListener(this);
 		
 		panel.add(returnToUserDialog);
 		panel.add(addButton);
 		
 		pane.add(panel);
 	}
     public static void createAndShowGUI() {
         //Create and set up the window.
         AddCopyDialog frame = new AddCopyDialog("Add Copy Dialog");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         frame.addComponentsToPane(frame.getContentPane());
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (returnToLibrarianDialogCommand.equals(arg0.getActionCommand()))
 		{
 			this.dispose();
 		}else if(arg0.getActionCommand().equals("Add"))
 		{
 			
 			
 			// need to check that status is "on-hold", "in" or "out"
 			
			if (addCopy() == 1)
			{
				GiveMeTitleAndMessageDialog.createAndShowGUI("Success", "Copy addded Successfully");
				callNumber.setText("");
				copyNo.setText("");
			}
 		}
 		
 	}
 	
 	public int addCopy() {
 		
 		int callNo;
 		int copynum;
 		String stat;
 		
 		if (callNumber.getText().trim().length() != 0) {
 			callNo = Integer.parseInt(callNumber.getText().trim());
 		}
 		else {
 			return VALIDATIONERROR;
 		}
 		
 		if (copyNo.getText().trim().length() != 0) {
 			copynum = Integer.parseInt(copyNo.getText());
 		}
 		else {
 			return VALIDATIONERROR;
 		}
 		
 		// need to check that callNumber is currently in system before we can add a copy
 		// maybe a sequence to increment copy No
 		
 		Transactions trans = new Transactions();
		if(trans.insertBookCopy(callNo, copynum, Constants.IN))
		{
			return 1;
		}else return VALIDATIONERROR;
 	}
 
 }
