 /**
  * 
  */
 package org.promasi.client_swing.gui.desktop.application.EMail;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import org.promasi.client_swing.components.JEditorPane.ExtendedJEditorPane;
 
 /**
  * @author alekstheod
  *
  */
 public class EmailJPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * 
 	 */
 	private JTextField _senderField;
 	
 	/**
 	 * 
 	 */
 	private JTextField _themeField;
 	
 	/**
 	 * 
 	 */
 	private JEditorPane _bodyPane;
 	
 	/**
 	 * 
 	 */
 	public EmailJPanel(){
 		setLayout(new BorderLayout());
 		JPanel topPanel = new JPanel();
 		topPanel.setLayout(new BorderLayout());
 		
 		JPanel senderPanel = new JPanel();
 		senderPanel.setLayout(new BorderLayout());
		JLabel fromLabel = new JLabel("From :");
 		fromLabel.setPreferredSize(new Dimension(50,20));
 		senderPanel.add(fromLabel, BorderLayout.WEST);
 		_senderField = new JTextField();
 		senderPanel.add(_senderField, BorderLayout.CENTER);
 		topPanel.add(senderPanel, BorderLayout.NORTH);
 		
 		JPanel themePanel = new JPanel();
 		themePanel.setLayout(new BorderLayout());
		JLabel themeLabel = new JLabel("Theme :");
 		themeLabel.setPreferredSize(new Dimension(50,20));
 		themePanel.add(themeLabel, BorderLayout.WEST);
 		_themeField = new JTextField();
 		themePanel.add(_themeField, BorderLayout.CENTER);
 		topPanel.add(themePanel, BorderLayout.SOUTH);
 		
 		_bodyPane = new ExtendedJEditorPane();
 		_bodyPane.setEditable(false);
 		_bodyPane.setContentType("text/html");
 		JScrollPane scrollPane = new JScrollPane(_bodyPane);
 		
 		add(topPanel, BorderLayout.NORTH);
 		add(scrollPane, BorderLayout.CENTER);
 	}
 	
 	/**
 	 * 
 	 * @param message
 	 * @return
 	 */
 	public boolean showMessage( Message message ){
 		boolean result = false;
 		
 		if( message != null ){
 			_senderField.setText(message.getSender());
 			_themeField.setText(message.getTheme());
 			_bodyPane.setText(message.open());
 			result = true;
 		}
 		
 		return result;
 	}
 
 }
