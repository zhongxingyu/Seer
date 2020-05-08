 package gui;
 
 import icm.dao.UserMail;
 import javax.swing.*;
 import javax.swing.event.ListSelectionListener;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.EventObject;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 
 
 public class MailEntryComponent extends JComponent
 {
 	public static Vector<UserMail> vc;
 	
 	public MailEntryComponent(UserMail mail)
 	{
 		
 		
 	}
 	
 	public static void main(String args[]) 
 	{
 		UserMail u=new UserMail();
 		u.setText("this is a text");
 		u.setTitle("title 1");
 		u.setSentAt(System.currentTimeMillis());
 		
 		UserMail u1=new UserMail();
 		u1.setText("some text");
 		u1.setTitle("title 2");
 		u1.setSentAt(System.currentTimeMillis());
 		
 		vc=new Vector<UserMail>();
 		//loop
 		vc.add(0,u);
 		vc.add(1,u1);
 		
 		//loop
 	    String labels[] = { vc.get(0).getTitle(),vc.get(1).getTitle()};
 
 	    JFrame frame = new JFrame("UserMailBox");
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    Container contentPane = frame.getContentPane();
 	    frame.getContentPane().setLayout(null);
 
 	    JList jlist = new JList(labels);
 	    JScrollPane scrollPane1 = new JScrollPane(jlist);
 	    scrollPane1.setBounds(0, 0, 54, 161);
 	    contentPane.add(scrollPane1);
 	    
 	    final JTextPane textPane = new JTextPane();
 	    textPane.setEditable(false);
 	    textPane.setBounds(55, 0, 279, 161);
 	    
 	    frame.getContentPane().add(textPane);
 
 	    ListSelectionListener listSelectionListener = new ListSelectionListener() 
 	    {
 	    	
 	      public void valueChanged(ListSelectionEvent listSelectionEvent) 
 	      {
	    	  JList model = (JList) listSelectionEvent.getSource();
 	    	  textPane.removeAll();
 			//ListSelectionModel lsm = (ListSelectionModel)listSelectionEvent.getSource();
 			
 	    	  StyleContext context = new StyleContext();
 	    	  DefaultStyledDocument doc = new DefaultStyledDocument(context);
 
 	    	    Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
 	    	    StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
 	    	    
 	    	    try {
					doc.insertString(doc.getLength(), vc.get(model.getLeadSelectionIndex()).getTitle()+"\n", style);
					doc.insertString(doc.getLength(), vc.get(model.getLeadSelectionIndex()).getText(), style);
 
 				} catch (BadLocationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	    	    
 	    	  
 	    		  textPane.setDocument(doc);
 	    	
 	    	  //textPane.setText(vc.get(listSelectionEvent.getLastIndex()).getText());
 	    	  //textPane.setText(Long.toString(vc.get(listSelectionEvent.getFirstIndex()).getSentAt()));
 		    	  textPane.removeAll();
 	      }
 	    };
 	    jlist.addListSelectionListener(listSelectionListener);
 	    jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//only one at time
 
 	    frame.setSize(350, 200);
 	    frame.setVisible(true);
 	  }
 }
