 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 
 
 public class DialogInterface implements ActionListener {
 	
 	private JTextField textField = new JTextField(60);
 	private JTextArea textArea = new JTextArea(20,60);
 	private JTextField textField2 = new JTextField(20);
 	private JTextArea textArea2 = new JTextArea(20,60);
 	private final static String newline = "\n";
 	private JFrame frame;
 	private JPanel jPanel = new JPanel();
 	private JPanel jPanelManual = new JPanel();
 	private String userName;
 	private JPanel jPanelSouth;
 
 	public DialogInterface() {
 		frame = new JFrame("Suhtlustreener");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(600,400);
 		frame.setLocation(300,300);
 		frame.add(textPanelManual());
 		frame.pack();
 		Dimension dim = new Dimension(800,420);
 		frame.setMinimumSize(dim);
 		frame.setVisible(true);
 		jPanel.setVisible(false);
 	}
 
 	private Component textPanel() {
 		jPanel = new JPanel();
 		jPanel.setLayout(new BorderLayout());
  
         textArea.setEditable(false);
         JScrollPane scrollPane = new JScrollPane(textArea);
 		JMenuBar menu = new MenuBar();
 		menu.setVisible(true);
 		jPanel.add(menu, BorderLayout.NORTH);
         jPanel.add(scrollPane, BorderLayout.CENTER);
         JPanel jPanelTextAreaSouth = new JPanel();
         JButton sendButton = new JButton("Saada");
         
         textField.addActionListener(this);
         sendButton.addActionListener(this);
         
         jPanelTextAreaSouth.add(textField);
         jPanelTextAreaSouth.add(sendButton);
         
         
         jPanel.add(jPanelTextAreaSouth, BorderLayout.SOUTH);
         jPanel.setVisible(true);
         return jPanel;
 	}
 	
 	private Component textPanelManual() {
 		jPanelManual = new JPanel();
 		jPanelManual.setLayout(new BorderLayout());
  
         textArea2.setEditable(false);
         textArea2.setBackground(frame.getBackground());
         writeManual(textArea2);
         JScrollPane scrollPane = new JScrollPane(textArea2);
 		JMenuBar menu = new MenuBar();
 		menu.setVisible(true);
 		jPanelManual.add(menu, BorderLayout.NORTH);
         jPanelManual.add(scrollPane, BorderLayout.CENTER);
         jPanelSouth = new JPanel();
         JButton sendButton = new JButton("Edasi");
         JLabel label = new JLabel("Sisesta oma kasutajanimi");
         
         textField2.addActionListener(this);
         sendButton.addActionListener(this);
         
         jPanelSouth.add(label);
         jPanelSouth.add(textField2);
         jPanelSouth.add(sendButton);
         
         
         jPanelManual.add(jPanelSouth, BorderLayout.SOUTH);
         jPanelManual.setVisible(true);
         return jPanelManual;
 	}
 
 	private void writeManual(JTextArea textArea22) {
 		textArea22.setText("  Juhend:");
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		if(jPanelManual.isVisible() && !StringUtils.isEmpty(textField2.getText())){
 			frame.add(textPanel(), 1);
 			jPanelManual.setVisible(false);
 			userName = textField2.getText();
 			frame.addNotify();
 		}
 		else{
 	        String text = textField.getText();
 	        textArea.append(userName+" : "+text + newline);
 	        new ConversationAgent().scales(text);
 	        textField.setText("");
 	        textArea.setCaretPosition(textArea.getDocument().getLength());
 		}
 	}
 	
 	public void openManual(){
 		jPanel.setVisible(false);
 		jPanelManual.setVisible(true);
 	}
 	
 	public void openTextArea(){
 		jPanel.setVisible(true);
 		jPanelManual.setVisible(false);
 	}
 	
 	public boolean isTextPanelVisible(){
 		return jPanel.isVisible();
 	}
 	
 	public JPanel getJPanelSouth(){
 		return jPanelSouth;
 	}
 	
 	public JPanel getJPanelManual(){
 		return jPanelManual;
 	}
 	
 	public void addTextToTextArea(String enteredText){
 		textArea.append("Agent : " + enteredText + newline);
 	}
 
 	public void clearTextArea(){
 		textArea.setText("");
 	}
 	
 	public String getTextAreaText(){
 		return textArea.getText();
 	}
 }
