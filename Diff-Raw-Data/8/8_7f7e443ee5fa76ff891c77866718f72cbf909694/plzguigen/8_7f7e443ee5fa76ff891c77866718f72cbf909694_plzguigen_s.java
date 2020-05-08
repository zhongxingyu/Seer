 package PLZ;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.text.ParseException;
 import javax.swing.border.*;
 import javax.swing.text.*;
 
 public class plzguigen extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6932703849032028687L;
 	private JPanel contentPane;
 	private JFormattedTextField textField;
 	JTextPane txtpnOutput;
 	private JFileChooser fc = new JFileChooser();
 	private PLZ p = new PLZ();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					plzguigen frame = new plzguigen();
 					
 					frame.pack();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public plzguigen() {
 		p.parse("blatt10/plz/plz.txt");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setTitle("some PLZ stuff");
 		this.setIconImage(Toolkit.getDefaultToolkit().getImage("blatt10/PLZ/smile.png"));
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		contentPane.add(panel, BorderLayout.NORTH);
 		
		Label label = new Label("Enter the PLZ here:");
		label.setAlignment(Label.CENTER);
		label.setEnabled(false);
		panel.add(label);
 		
 		textField = new JFormattedTextField(textmask());
 		textField.setToolTipText("Enter the PLZ here");
 		textField.setHorizontalAlignment(SwingConstants.CENTER);
 		panel.add(textField);
 		textField.setColumns(10);
 		txtpnOutput = new JTextPane();
 		txtpnOutput.setText("Output:\n\n\n\n\n\n\n\n");
 		contentPane.add(txtpnOutput, BorderLayout.CENTER);
 		
 		textField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyChar()==10){
 					handleinput();
 				}
 				if(arg0.getKeyChar()=='c'){
 					textField.setText("");
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 			@Override
 			public void keyTyped(KeyEvent arg0) {}
 		});
 
 
 		
 		JButton btnNewButton = new JButton("Find!");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				handleinput();				
 			}
 		});
 		panel.add(btnNewButton);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		mnFile.setMnemonic('f');
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmNewEntry = new JMenuItem("New Entry");
 		mntmNewEntry.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) 
 			{
 				opendialog();
 				//open the new menu
 			}
 		});
 		mntmNewEntry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
 		mntmNewEntry.setMnemonic(KeyEvent.VK_N);
 		mnFile.add(mntmNewEntry);
 		
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mntmSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) 
 			{
 				
 				save();
 				//save it				
 			}
 		});
 		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
 		mntmSave.setMnemonic(KeyEvent.VK_S);
 		mnFile.add(mntmSave);
 		
 		
 	}
 	
 	private MaskFormatter textmask()
 	{
 		MaskFormatter f = null;
 		try
 		{
 			f = new MaskFormatter("####");
 		}
 		catch(ParseException e)
 		{
 			
 		}
 		return f;
 	}
 	
 	private void handleinput()
 	{
 		cAddy[] a = p.HandleInput(this.textField.getText());
 		if(a != null && a.length > 0)
 			this.txtpnOutput.setText("Output:\n"+p.makeString(a));
 		else
 			this.txtpnOutput.setText("Output:\nThis PLZ can't be found inside switzerland");
 	}
 	
 	private void save()
 	{
 		if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 			p.save(fc.getSelectedFile()+".txt");		
 		}
 	}
 	
 	private void opendialog()
 	{
 		
 		try {
 			NewEntry dialog = new NewEntry(p);
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.pack();
 			dialog.setVisible(true);
 ;		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
