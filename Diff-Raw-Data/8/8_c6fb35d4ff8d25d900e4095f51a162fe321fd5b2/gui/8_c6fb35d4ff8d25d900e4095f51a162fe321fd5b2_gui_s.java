 package de.runinho.maneger;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextPane;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import java.awt.Font;
 
 public class gui {
 
 	private JFrame frame;
 	private JTextField Name;
 	private JTextField ort;
 	private JTextField plz;
 	private JTextField strasse;
 	private JTextField nr;
 	private JTextPane textPane;
 	private JButton btnKorrektur;
 	private Font buttonFont = new Font("Lucida Grande", Font.PLAIN, 40);
 	
 	private boolean korrektur = false;
 	private int herrenOrden = 0;
 	private int damenOrden = 0;
 	private int herrenPreis = 50 ;
 	private int damenPreis = 30;
 	private int Sonstige = 0;
 	private String massage = "";
 	private String nl = System.lineSeparator();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					gui window = new gui();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public gui() {
 		initialize();
 	}
 	public void clearInfo(){
 		Sonstige = 0;
 		herrenOrden = 0;
 		damenOrden = 0;
 		textPane.setText("");
 		korrektur = false;
 		Name.setText("");
 		ort.setText("");
 		plz.setText("");
 		strasse.setText("");
 		nr.setText("");
 	}
 	public void setInfo(){
 		massage = "";
 		if(herrenOrden != 0 )
 			massage += herrenOrden+"x Herrenorden a "+herrenPreis+" fr "+(herrenOrden*herrenPreis)+""+nl;
 		if(damenOrden != 0 )
 			massage += damenOrden+"x Damenorden a "+damenPreis+" fr "+(damenOrden*damenPreis)+""+nl;
 		if(Sonstige != 0)
 			massage += Sonstige+"x Sonstiges "+nl;
 		textPane.setText(massage);
 	}
 	public void  plusHerren(){
 		if(!korrektur){
 			herrenOrden++;
 		}else
 		{
 			herrenOrden--;
 		}
 		setInfo();
 	}
 	public void plusDamen(){
 		if(!korrektur){
 			damenOrden++;
 		}else{
 			damenOrden--;
 		}
 		setInfo();
 	}
 	public void bestatigen(){
 		if(Name.getText().length() != 0){
 			String namestr = Name.getText();
 			String ortstr;
 			String plzstr;
 			String strassestr;
 			String nrstr;
 			if(massage.length() != 0){
 				if(ort.getText().length() != 0){
 					ortstr = ort.getText();
 				}
 				else{
 					ortstr = "Kln";
 				}
 				if(plz.getText().length() != 0){
 					plzstr = plz.getText();
 					
 					if(strasse.getText().length() !=0 ){
 						strassestr = strasse.getText();
 						
 						if(nr.getText().length() !=0){
 							nrstr = nr.getText();
 									writer.writeBestellung(massage,namestr, ortstr, plzstr, strassestr, nrstr);
 									clearInfo();
 						}
 					}
 				}else{
 					return;
 				}
 			}
 		}
 		else{
 			if(massage.length() != 0){
 				writer.writeBestellung(massage);
 				clearInfo();
 			}
 		}
 	}
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 1171, 537);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(6, 6, 1159, 503);
 		frame.getContentPane().add(tabbedPane);
 		
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("Eingabe", null, panel, null);
 		panel.setLayout(null);
 		
 		JButton btnTest = new JButton("Best\u00E4tigen");
 		btnTest.setFont(buttonFont);
 		btnTest.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//Besttigen
 				bestatigen();
 				//writer.writeBestellung("Herren Orden", (float)50.5, 2, "Herr Frohn", "Kln", 50935, "Guldenbachstr", "14", true);
 			}
 		});
 		btnTest.setBounds(395, 341, 245, 115);
 		panel.add(btnTest);
 		
 		JButton btnNewButton = new JButton("Herren Orden");
 		btnNewButton.setFont(buttonFont);
 		btnNewButton.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Herrenorden
 				plusHerren();
 				
 			}
 		});
		btnNewButton.setBounds(298, 15, 342, 99);
 		panel.add(btnNewButton);
 		
 		JButton btnDamenOrden = new JButton("Damen Orden");
 		btnDamenOrden.setFont(buttonFont);
 		btnDamenOrden.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Dameorden
 				plusDamen();
 			}
 		});
		btnDamenOrden.setBounds(6, 15, 280, 99);
 		panel.add(btnDamenOrden);
 		
 		JButton btnSonstieges = new JButton("Sonstieges\n");
 		btnSonstieges.setFont(buttonFont);
 		btnSonstieges.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Sonstieges
 			}
 		});
 		btnSonstieges.setBounds(652, 10, 480, 108);
 		panel.add(btnSonstieges);
 		
 		btnKorrektur = new JButton("Korrektur");
 		btnKorrektur.setFont(buttonFont);
 		btnKorrektur.setForeground(new Color(0, 100, 0));
 		btnKorrektur.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Korrektur
 				if(korrektur){
 					korrektur = false;
 					btnKorrektur.setForeground(new Color(0, 100, 0));
 				}else{
 					korrektur = true;
 					btnKorrektur.setForeground(new Color(220, 20, 60));
 				}
 			}
 		});
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(652, 147, 480, 303);
 		panel.add(scrollPane);
 		
 		textPane = new JTextPane();
 		scrollPane.setViewportView(textPane);
 		textPane.setFont(buttonFont);
 		textPane.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
 		btnKorrektur.setBounds(6, 341, 255, 115);
 		panel.add(btnKorrektur);
 		
 		Name = new JTextField();
 		Name.setFont(buttonFont);
 		Name.setBounds(142, 140, 477, 48);
 		panel.add(Name);
 		Name.setColumns(10);
 		
 		JLabel lblName = new JLabel("Name:");
 		lblName.setFont(buttonFont);
 		lblName.setBounds(16, 126, 150, 76);
 		panel.add(lblName);
 		
 		JLabel lblOrt = new JLabel("Ort");
 		lblOrt.setFont(buttonFont);
 		lblOrt.setBounds(16, 202, 82, 68);
 		panel.add(lblOrt);
 		
 		ort = new JTextField();
 		ort.setFont(buttonFont);
 		ort.setBounds(90, 212, 293, 48);
 		panel.add(ort);
 		ort.setColumns(10);
 		
 		JLabel lblPlz = new JLabel("PLZ:");
 		lblPlz.setFont(buttonFont);
 		lblPlz.setBounds(395, 215, 89, 42);
 		panel.add(lblPlz);
 		
 		plz = new JTextField();
 		plz.setFont(buttonFont);
 		plz.setBounds(496, 211, 123, 50);
 		panel.add(plz);
 		plz.setColumns(10);
 		
 		strasse = new JTextField();
 		strasse.setFont(buttonFont);
 		strasse.setBounds(202, 281, 323, 48);
 		panel.add(strasse);
 		strasse.setColumns(10);
 		
 		JLabel lblStrasse = new JLabel("Stra\u00DFe Nr");
 		lblStrasse.setFont(buttonFont);
 		lblStrasse.setBounds(16, 279, 180, 53);
 		panel.add(lblStrasse);
 		
 		nr = new JTextField();
 		nr.setBounds(537, 281, 82, 48);
 		nr.setFont(buttonFont);
 		panel.add(nr);
 		nr.setColumns(10);
 		
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab("Info", null, panel_1, null);
 		
 		JPanel panel_2 = new JPanel();
 		tabbedPane.addTab("Einstellungen", null, panel_2, null);
 	}
 }
