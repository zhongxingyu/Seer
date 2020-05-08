 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.Robot;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.KeyEvent;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JLabel;
 
 import java.awt.Font;
  
 import javax.swing.SwingConstants;
 
 import java.awt.Color;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 import javax.swing.JCheckBox;
 
 public class MainFrame extends JFrame {
     Thread d;
     
 	private JPanel contentPane;
     
     private JTextField text;
     private JTextField terxt;
     private JTextField d1terxt;
 	protected JFrame frame;
 	static JButton btnNewButton;
 	private JPanel panel;
 	private static JLabel lblAutotyper;
 	private JLabel lblText;
 	private JTextField txtTextToSpam;
 	private JLabel lblDelayBetweenSpam;
 	private JLabel lblInitialDelay;
 	private JLabel lblNumberToSpam;
 	private JLabel lblForInfinite;
     
 	public static void main(String[] args) throws Exception{
 		
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "AutoTyper (zst123)");
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				
 					MainFrame frame = new MainFrame();
					// TRANSPARENCY VALUE IN A FLOAT // CURRENTLY 80%
					frame.getRootPane().putClientProperty("Window.alpha", new Float(0.80f)); 
 					frame.setAlwaysOnTop(true);
 					frame.setVisible(true);
 			} });
 	}
 
 	
 	public MainFrame() {
 		setResizable(false);
 	    setTitle("Universal Auto Typer by zst123");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setAlwaysOnTop(true);
 		setBounds(100, 100, 310, 281);
 		contentPane = new JPanel();
 		contentPane.setBackground(new Color(0, 0, 0));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		setContentPane(contentPane);
 		
 		panel = new JPanel();
 		panel.setBackground(Color.BLACK);
 		contentPane.add(panel, BorderLayout.CENTER);
 		panel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("134px:grow"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("70px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("62px"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.PREF_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("28px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),}));
 		
 		lblText = new JLabel("Text to Spam");
 		lblText.setHorizontalAlignment(SwingConstants.RIGHT);
 		panel.add(lblText, "2, 2");
 		lblText.setFont(new Font("Tahoma", Font.PLAIN, 14));   
 		lblText.setForeground(Color.WHITE);
 		
 		txtTextToSpam = new JTextField();
 		txtTextToSpam.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		txtTextToSpam.setText("spam");
 		panel.add(txtTextToSpam, "4, 2");
 		txtTextToSpam.setColumns(10);
 		
 		lblDelayBetweenSpam = new JLabel("Delay Per Spam (ms)");
 		lblDelayBetweenSpam.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblDelayBetweenSpam.setForeground(Color.WHITE);
 		lblDelayBetweenSpam.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(lblDelayBetweenSpam, "2, 4, right, default");
 		
 		terxt = new JTextField();
 		terxt.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(terxt, "4, 4, left, top");
 		terxt.setColumns(10);
 		terxt.setText("100");
 		
 		lblInitialDelay = new JLabel("Initial Delay (ms)");
 		lblInitialDelay.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblInitialDelay.setForeground(Color.WHITE);
 		lblInitialDelay.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(lblInitialDelay, "2, 6, right, default");
 		
 		text = new JTextField();
 		text.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(text, "4, 6, left, top");
 		text.setColumns(10);
 		text.setText("3000");
 		
 		lblNumberToSpam = new JLabel("Number to Spam");
 		lblNumberToSpam.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNumberToSpam.setForeground(Color.WHITE);
 		lblNumberToSpam.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(lblNumberToSpam, "2, 8, right, default");
 		
 		d1terxt = new JTextField();
 		d1terxt.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(d1terxt, "4, 8");
 		d1terxt.setColumns(3);
 		d1terxt.setText("100");
 		
 		lblForInfinite = new JLabel("(0 for infinite until stopped)");
 		panel.add(lblForInfinite, "2, 10, 3, 1");
 		lblForInfinite.setHorizontalAlignment(SwingConstants.CENTER);
 		lblForInfinite.setForeground(Color.WHITE);
 		lblForInfinite.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		
 		lblUse = new JLabel("Alternate Spamming");
 		lblUse.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblUse.setForeground(Color.WHITE);
 		lblUse.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(lblUse, "2, 12");
 		
 		altbox = new JCheckBox("");
 		altbox.setVerticalAlignment(SwingConstants.TOP);
 		altbox.setHorizontalAlignment(SwingConstants.LEFT);
 		altbox.setForeground(Color.WHITE);
 		altbox.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		panel.add(altbox, "4, 12");
 		
 		 btnNewButton = new JButton("Start");
 		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		contentPane.add(btnNewButton, BorderLayout.SOUTH);
 		
 		lblAutotyper = new JLabel("Universal AutoTyper");
 		lblAutotyper.setForeground(new Color(255, 0, 0));
 		lblAutotyper.setHorizontalAlignment(SwingConstants.CENTER);
 		lblAutotyper.setFont(new Font("Arial", Font.BOLD, 20));
 		contentPane.add(lblAutotyper, BorderLayout.NORTH);
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				if (start == false){
 				initSpam();
 				updateVariable(true);
 				}else{//force stop
 					//d.stop();
 					start = false;
 					updateVariable(false);
 				}
 				
 			}
 		});
 	}
 	
 	public static void updateVariable(boolean on){
 		if (on == true){
 			lblAutotyper.setForeground(new Color(0, 255, 0));
 			btnNewButton.setText("Stop");
 		}else{
 			lblAutotyper.setForeground(new Color(255, 0, 0));
 			btnNewButton.setText("Start");
 		}
 	}
 	public void initSpam(){
 		int mKey = ModifierKey.get();
 
 		String text_spam = txtTextToSpam.getText();
 		int delay = Integer.parseInt(terxt.getText());
 		int initial = Integer.parseInt(text.getText());
 		int loops = Integer.parseInt(d1terxt.getText());
 		boolean unlimited = false;
 		if (delay<1){ delay = 1;}
 		if (loops == 0){ loops = 100; unlimited = true;}
 		
 		if (altbox.isSelected()){
 		CopyPaster.type(text_spam, loops, initial, delay, mKey,unlimited);
 			
 		}else{
 			
 		try{
 		ManualTyper.mtype(text_spam, loops, initial, delay, mKey,unlimited);
 		}catch(Exception e){ }
 		
 		}
 	}
 	
 	static boolean start = false;
 	private JCheckBox altbox;
 	private JLabel lblUse;
 	
 	
 
 	
 	
 
 	
 
 
 	
 	
 
 	/* "nix" "nux" "aix" */
 	
 	
 }
