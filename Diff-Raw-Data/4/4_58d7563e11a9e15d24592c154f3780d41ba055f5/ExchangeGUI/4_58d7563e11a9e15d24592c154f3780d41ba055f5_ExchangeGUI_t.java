 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.JLabel;
 import domain.DomainController;
 
 /**
  * This class extends JFrame and allows the user to exchange his SymbolCards.
  * @author Thijs van der Burgt
  * @author Arno Schutijzer
  * @see domain.baseclass.User
  */
 
 @SuppressWarnings("serial")
 public class ExchangeGUI extends JFrame implements ActionListener{
 	
 	private DomainController domainController;
 	private Messages messages;
 	
 	private int currentUser;
 	private List<ImageIcon> images= new ArrayList<>();
 	private List<JLabel> lblImages= new ArrayList<>();
 	private List<JToggleButton> btnSelection= new ArrayList<>();
 	private JButton btnConfirm;
 	/**
	 * Constructor of this class and creates a new JFrame.
 	 * @param domainController
 	 * @param messages
 	 * @param currentUser
 	 */
 	public ExchangeGUI(DomainController domainController, Messages messages, int currentUser){
 		this.domainController= domainController;
 		this.messages= messages;
 		
 		//get active user
 		this.currentUser= currentUser;
 		
 		//extract imageIcons from user
 		for(int i= 0; i < domainController.getUser(currentUser).getCards().size(); i++)
 			images.add(new ImageIcon("lib/images/"+domainController.getUser(currentUser).getCards().get(i).getType()+".png"));
 				
 		//use imageIcons to construct JLabels with the image
 		for(ImageIcon i: images)
 			lblImages.add(new JLabel(i));
 		
 		//add buttons to select card
 		for(JLabel lbl: lblImages){
 			btnSelection.add(new JToggleButton(messages.getString("select")));
 		}
 				
 		btnConfirm= new JButton(messages.getString("confirm"));
 		btnConfirm.setActionCommand("confirm");
 		btnConfirm.addActionListener(this);
 		
 		//initialize GroupLayout
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setAutoCreateContainerGaps(true);
 		layout.setAutoCreateGaps(true);
 				
 		//create different Groups
 		SequentialGroup sGroup= layout.createSequentialGroup();
 		ParallelGroup pGroup= layout.createParallelGroup();
 		
 		SequentialGroup sBtnGroup= layout.createSequentialGroup();
 		ParallelGroup pBtnGroup= layout.createParallelGroup();
 		
 		SequentialGroup sConfirmGroup= layout.createSequentialGroup();
 		ParallelGroup pConfirmGroup= layout.createParallelGroup();
 		
 		//fill Groups
 		for(JLabel lbl: lblImages){
 			sGroup.addComponent(lbl);
 			pGroup.addComponent(lbl);
 		}
 		
 		for(JToggleButton btn: btnSelection){
 			sBtnGroup.addComponent(btn);
 			pBtnGroup.addComponent(btn);
 		}
 				
 		sConfirmGroup.addComponent(btnConfirm);
 		pConfirmGroup.addComponent(btnConfirm);
 		
 		//add Groups to Frame
 		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 					.addGroup(sGroup)
 						.addGap(20)
 					.addGroup(sBtnGroup)
 						.addGap(20)
 					.addGroup(sConfirmGroup));
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 					.addGroup(pGroup)
 						.addGap(10)
 					.addGroup(pBtnGroup)
 						.addGap(10)
 					.addGroup(pConfirmGroup));
 		
 		//settings
 		setVisible(true);
 		pack();
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 
 	/**
 	 * This method performs a series of actions based on the user's actions.
	 * @param e
 	 */
 	public void actionPerformed(ActionEvent e) {
 
 		try{
 			int[] indices= new int[3];
 			int index= 0, exchangedValue= 0;
 			List<String> cards= new ArrayList<>();
 
 			int check= 0;
 			for(int i = 0; i< btnSelection.size(); i++)
 				if(btnSelection.get(i).isSelected())
 					check++;
 
 			if(check== 0)
 				this.dispose();
 
 			if( check > 0 && check < 3)
 				throw new IllegalStateException("noCorrectCombinationException");
 
 			for(int i= 0; i< btnSelection.size(); i++)
 				if(btnSelection.get(i).isSelected() && index < 3){
 					indices[index]= i;
 					index++;
 				}
 
 			for(int i=0; i< indices.length; i++)
 				cards.add(domainController.getUser(currentUser).getCards().get(indices[i]).getType());
 
 			exchangedValue= domainController.getGame().exchangeCards(cards);
 
 			int count= 0;
 			if(exchangedValue != 0){
 				while(count != 3){
 					for(int i= 0; i< domainController.getUser(currentUser).getCards().size(); i++)
 						if(domainController.getUser(currentUser).getCards().get(i).getType().equals(cards.get(count))){
 							domainController.getUser(currentUser).getCards().remove(i);
 							count++;
 						}
 
 				}
 				
 				System.out.print(exchangedValue);
 				domainController.getUser(currentUser).addUnassignedArmy(exchangedValue);
 			}
 			else
 				throw new IllegalArgumentException("noCorrectCombinationException");
 
 			System.out.print(domainController.getUser(currentUser).getUnassignedArmy());
 			
 			this.dispose();
 		}
 		catch(IllegalStateException e1){
 			JOptionPane.showMessageDialog(null, messages.getString(e1.getMessage()) );
 		}
 		catch(IllegalArgumentException e1){
 			JOptionPane.showMessageDialog(null, messages.getString(e1.getMessage()) );
 		}
 	}
 }
