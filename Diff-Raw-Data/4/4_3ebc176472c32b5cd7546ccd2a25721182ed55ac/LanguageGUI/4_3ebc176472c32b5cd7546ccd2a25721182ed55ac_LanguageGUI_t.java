 package gui;
 
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import domain.DomainController;
 
 import java.awt.event.*;
 import java.sql.SQLException;
 /**
  * This class extends JFrame and allows the user to select a language.
  * @author Thijs van der Burgt
  * @author Arno Schutijzer
  * @see gui.Messages
  */
 @SuppressWarnings("serial")
 public class LanguageGUI extends JFrame implements ActionListener{
 
 	final ImageIcon ENGLISHFLAG = new ImageIcon("lib/images/english_flag.gif");
 	final ImageIcon DUTCHFLAG = new ImageIcon("lib/images/dutch_flag.gif");
 	final ImageIcon FRENCHFLAG = new ImageIcon("lib/images/french_flag.gif");
 
 	private JButton btnDutch = new JButton(DUTCHFLAG);
 	private JButton btnEnglish = new JButton(ENGLISHFLAG);
 	private JButton btnFrench = new JButton(FRENCHFLAG);
 
 	private Messages messages= new Messages();
 	
 	private DomainController domainController;
 	/**
	 * Constructor of this class.
 	 * This method creates a new JFrame
 	 * @param domainController
 	 */
 	public LanguageGUI(DomainController domainController) {
 		this.domainController= domainController;
 		
 		btnDutch.setBorder(null);
 		btnEnglish.setBorder(null);
 		btnFrench.setBorder(null);
 
 		btnDutch.addActionListener(this);
 		btnDutch.setActionCommand("selectDutch");
 
 		btnEnglish.addActionListener(this);
 		btnEnglish.setActionCommand("selectEnglish");
 
 		btnFrench.addActionListener(this);
 		btnFrench.setActionCommand("selectFrench");
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGap(0,0,Short.MAX_VALUE)
         		.addGroup(layout.createSequentialGroup()
         				.addComponent(btnDutch)
         				.addComponent(btnEnglish)
         				.addComponent(btnFrench))
 				);
 
         layout.setVerticalGroup(layout.createSequentialGroup()
         		.addGroup(layout.createParallelGroup()
         				.addComponent(btnDutch)
         				.addComponent(btnEnglish)
         				.addComponent(btnFrench))
         		);
 
         setVisible(true);
 		setTitle("Language");
 		setResizable(false);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 	}
 	/**
	 * This method performs an action based on an event caused by a component activating.
 	 * In this case the event is performed by a JButton on the JFrame.
 	 */
 	public void actionPerformed(ActionEvent event) {
 		try{
 		switch(event.getActionCommand()){
 		case "selectDutch":
 			messages.setResourceBundle("Dutch");
 			new InitGUI(messages, domainController);
 			this.dispose();
 			break;
 		
 		case "selectFrench":
 			messages.setResourceBundle("French");			
 			new InitGUI(messages, domainController);
 			this.dispose();
 			break;
 			
 		case "selectEnglish":
 			messages.setResourceBundle("English");
 			new InitGUI(messages, domainController);
 			this.dispose();
 			break;
 		}
 		}
 		catch( ClassNotFoundException e){
 			JOptionPane.showMessageDialog(null, "Error!", e.getMessage(), JOptionPane.ERROR_MESSAGE);
 		} catch (SQLException e) {
 			JOptionPane.showMessageDialog(null, "Error!", e.getMessage(), JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	public Messages getMessages(){
 		return messages;
 	}
 }
