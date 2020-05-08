 package com.dobby.client.ui;
 
import java.awt.Font;
 import java.awt.Rectangle;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.text.Document;
 
 import jsyntaxpane.DefaultSyntaxKit;
 
 public class TestDisplayPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JEditorPane jEditorPane = null;
 
 	/**
 	 * This is the default constructor
 	 */
 	public TestDisplayPanel() {
 		super();
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setSize(500, 700);
 		this.setLayout(null);
        DefaultSyntaxKit.initKit();
 		JScrollPane scrPane = new JScrollPane(getJEditorPane());
 		scrPane.setSize(500, 700);
 		this.add(scrPane);
 		jEditorPane.setContentType("text/java");
 
 	}
 
 	/**
 	 * This method initializes jEditorPane	
 	 * 	
 	 * @return javax.swing.JEditorPane	
 	 */
 	private JEditorPane getJEditorPane() {
 		if (jEditorPane == null) {
 			jEditorPane = new JEditorPane();
 			jEditorPane.setBounds(new Rectangle(5, 5, 490, 690));
 		}
 		return jEditorPane;
 	}
 
 	public Document getDocument() {
 		return jEditorPane.getDocument();
 	}
 
 	public JEditorPane getjEditorPane() {
 		return jEditorPane;
 	}
 
 	
 	
 	
 }
