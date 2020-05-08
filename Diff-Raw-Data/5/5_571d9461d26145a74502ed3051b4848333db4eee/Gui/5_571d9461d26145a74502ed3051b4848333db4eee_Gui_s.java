 package simumatch.gui;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JPanel;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.TitledBorder;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.UIManager;
 import javax.swing.JSpinner;
 import java.awt.Font;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import simumatch.common.Action;
 import simumatch.common.Effect;
 import simumatch.datamanager.AbilitiesData;
 import simumatch.match.Partido;
 import simumatch.match.Turno;
 import simumatch.team.Equipo;
 
 
 import java.awt.Toolkit;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JScrollPane;
 
 public class Gui {
 
 	private JFrame frmJugadorNumero;
 	private JTextField textField;
 	private JTextArea textArea;
 	private JPanel panel;
 	private JPanel panel_1;
 	private JPanel panel_4;
 	private JPanel panel_5;
 	private JButton btnSiguiente;
 	private JPanel panel_2;
 	private JPanel panel_3;
 	private JSpinner spinner_30;
 	private JSpinner spinner_31;
 	private JSpinner spinner_32;
 	private JSpinner spinner_33;
 	private JSpinner spinner_34;
 	private JSpinner spinner_35;
 	private JSpinner spinner_36;
 	private JSpinner spinner_37;
 	private JSpinner spinner_38;
 	private JSpinner spinner_39;
 	private JSpinner spinner_40;
 	private JSpinner spinner_41;
 	private JSpinner spinner_42;
 	private JSpinner spinner_43;
 	private JSpinner spinner_44;
 	private JSpinner spinner_45;
 	private JSpinner spinner_46;
 	private JSpinner spinner_47;
 	private JSpinner spinner_48;
 	private JSpinner spinner_49;
 	private JButton button;
 	private JSpinner spinner;
 	private JSpinner spinner_1;
 	private JSpinner spinner_2;
 	private JSpinner spinner_3;
 	private JSpinner spinner_4;
 	private JSpinner spinner_5;
 	private JSpinner spinner_6;
 	private JSpinner spinner_7;
 	private JSpinner spinner_8;
 	private JSpinner spinner_9;
 	private JSpinner spinner_10;
 	private JSpinner spinner_11;
 	private JSpinner spinner_12;
 	private JSpinner spinner_13;
 	private JSpinner spinner_14;
 	private JSpinner spinner_15;
 	private JSpinner spinner_16;
 	private JSpinner spinner_17;
 	private JSpinner spinner_18;
 	private JSpinner spinner_19;
 	private JSpinner spinner_20;
 	private JSpinner spinner_21;
 	private JSpinner spinner_22;
 	private JSpinner spinner_23;
 	private JSpinner spinner_24;
 	private JSpinner spinner_25;
 	private JSpinner spinner_26;
 	private JSpinner spinner_27;
 	private JSpinner spinner_28;
 	private JSpinner spinner_29;
 	private JButton btnCargar;
 	private List<Effect> listEffectsP1,listEffectsP2,listEffectsM1,listEffectsM2;
 	private AbilitiesData data;
 	private Equipo e1;
 	private Equipo e2;
 	private Partido match;
     private Turno turn;
     
     private HashMap<Action,Number> prepHashL,prepHashV,matchHashL,matchHashV;
 	private JSpinner[] arraySpinsP;
 	private JSpinner[] arraySpinsM;
 	private JScrollPane scrollPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		/*try {
 			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}*/
 		try {
 		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 		        if ("Nimbus".equals(info.getName())) {
 		            UIManager.setLookAndFeel(info.getClassName());
 		            break;
 		        }
 		    }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Gui window = new Gui();
 					window.frmJugadorNumero.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Gui() {
 		listEffectsP1 = new LinkedList<Effect>();
         listEffectsP2 = new LinkedList<Effect>();
         listEffectsM1 = new LinkedList<Effect>();
         listEffectsM2 = new LinkedList<Effect>();
         data = new AbilitiesData();
         try {
 			data.loadFile(new File("./animadora.txt"));
 			data.loadFile(new File("./empresario.txt"));
 			data.loadFile(new File("./ultra.txt"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         e1 = Equipo.equipo_de_prueba1();
         e2 = Equipo.equipo_de_prueba2();
         
 		initialize();
 		arraySpinsP = new JSpinner[] {spinner,spinner_1,spinner_2,spinner_3,spinner_4,spinner_5,spinner_6,spinner_7,spinner_8,spinner_9,
 								spinner_10,spinner_11,spinner_12,spinner_13,spinner_14,spinner_15,spinner_16,spinner_17,spinner_18,
 								spinner_19,spinner_20,spinner_21,spinner_22,spinner_23,spinner_24,spinner_25,spinner_26,spinner_27,spinner_28,
 								spinner_29};
 		arraySpinsM = new JSpinner[] {spinner_30,spinner_31,spinner_32,spinner_33,spinner_34,spinner_35,spinner_36,spinner_37,spinner_38,
 								spinner_39,spinner_40,spinner_41,spinner_42,spinner_43,spinner_44,spinner_45,spinner_46,spinner_47,
 								spinner_48,spinner_49};
 		prepHashL = new HashMap<Action,Number>();
 		prepHashV = new HashMap<Action,Number>();
 		matchHashL = new HashMap<Action,Number>();
 		matchHashV = new HashMap<Action,Number>();
 	}
 	
 	//Load button from preparatory actions
 	protected void bntCargarActionPerformed(ActionEvent arg0) {
 		int i;
 		//Local
 		for(i=0;i<15;i++){
 			prepHashL.put(Action.get(arraySpinsP[i].getName()) , (Number) arraySpinsP[i].getValue());
 		}
 		//Visitor
 		for(i=15;i<30;i++){
 			prepHashV.put(Action.get(arraySpinsP[i].getName()) , (Number) arraySpinsP[i].getValue());
 		}
 		listEffectsP1 = data.getEffects(prepHashL, true);
 		listEffectsP2 = data.getEffects(prepHashV, true);
 		e1.setPerparatorias(listEffectsP1);
 		e2.setPerparatorias(listEffectsP2);
 		match = new Partido(e1,e2);
 		btnCargar.setEnabled(false);
 	}
 	
 	//Load button from match actions
 	protected void buttonActionPerformed(ActionEvent arg0) {
 		int i;
 		//Local
 		for(i=0;i<10;i++){
 			matchHashL.put(Action.get(arraySpinsM[i].getName()) , (Number) arraySpinsM[i].getValue());
 		}
 		//Visitor
 		for(i=10;i<20;i++){
 			matchHashV.put(Action.get(arraySpinsM[i].getName()) , (Number) arraySpinsM[i].getValue());
 		}
 		listEffectsM1 = data.getEffects(matchHashL, true);
 		listEffectsM2 = data.getEffects(matchHashV, true);
 		turn = match.turno(listEffectsM1, listEffectsM2);
 		
 		//Paint
 		textField.setText(String.valueOf(turn.getEstado()));
 		textArea.setText(textArea.getText()+turn);
 	}
 
 	//Next button for next match turn
 	protected void btnSiguienteActionPerformed(ActionEvent e) {
 		textField.setText("");
 		for(int i=0;i<20;i++){
 			arraySpinsM[i].setValue(0);
 		}
		if(turn.terminado())btnSiguiente.setEnabled(false);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmJugadorNumero = new JFrame();
 		frmJugadorNumero.setIconImage(Toolkit.getDefaultToolkit().getImage(Gui.class.getResource("/simumatch/gui/numero12.png")));
 		frmJugadorNumero.setTitle("Jugador Numero 12");
 		frmJugadorNumero.setBounds(100, 100, 1280, 700);
 		frmJugadorNumero.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		panel = new JPanel();
 		panel.setBorder(new TitledBorder(null, "Variables de entrada", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		
 		panel_1 = new JPanel();
 		panel_1.setBorder(new TitledBorder(null, "Salida generada", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout groupLayout = new GroupLayout(frmJugadorNumero.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 861, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 397, GroupLayout.PREFERRED_SIZE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		);
 		
 		panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "Registro de valores generados", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		
 		panel_5 = new JPanel();
 		panel_5.setBorder(new TitledBorder(null, "Resultado actual", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
 						.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
 						.addGroup(gl_panel_1.createSequentialGroup()
 							.addGap(6)
 							.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)))
 					.addContainerGap())
 		);
 		gl_panel_1.setVerticalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 405, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		
 		textField = new JTextField();
 		textField.setEditable(false);
 		textField.setColumns(10);
 		
 		btnSiguiente = new JButton("Siguiente");
 		btnSiguiente.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				btnSiguienteActionPerformed(e);
 			}
 		});
 		btnSiguiente.setFont(new Font("Tahoma", Font.BOLD, 12));
 		GroupLayout gl_panel_5 = new GroupLayout(panel_5);
 		gl_panel_5.setHorizontalGroup(
 			gl_panel_5.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_5.createSequentialGroup()
 					.addGap(31)
 					.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(176, Short.MAX_VALUE))
 				.addGroup(Alignment.TRAILING, gl_panel_5.createSequentialGroup()
 					.addContainerGap(191, Short.MAX_VALUE)
 					.addComponent(btnSiguiente, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
 					.addGap(31))
 		);
 		gl_panel_5.setVerticalGroup(
 			gl_panel_5.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_5.createSequentialGroup()
 					.addGap(48)
 					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(btnSiguiente, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(19, Short.MAX_VALUE))
 		);
 		panel_5.setLayout(gl_panel_5);
 		
 		scrollPane = new JScrollPane();
 		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
 		gl_panel_4.setHorizontalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
 		);
 		gl_panel_4.setVerticalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
 		);
 		
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		scrollPane.setViewportView(textArea);
 		panel_4.setLayout(gl_panel_4);
 		panel_1.setLayout(gl_panel_1);
 		
 		panel_2 = new JPanel();
 		panel_2.setBorder(new TitledBorder(null, "Modificadores de preparativos", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		
 		panel_3 = new JPanel();
 		panel_3.setBorder(new TitledBorder(null, "Acciones inmediatas", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
 				.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 757, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
 					.addGap(12)
 					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 310, GroupLayout.PREFERRED_SIZE))
 		);
 		
 		spinner_30 = new JSpinner();
 		spinner_30.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_30.setName("SALTO_ESPONTANEO");
 		
 		spinner_31 = new JSpinner();
 		spinner_31.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_31.setName("INICIAR_OLA");
 		
 		spinner_32 = new JSpinner();
 		spinner_32.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_32.setName("PUNTERO_LASER");
 		
 		spinner_33 = new JSpinner();
 		spinner_33.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_33.setName("TIRAR_BENGALA");
 		
 		spinner_34 = new JSpinner();
 		spinner_34.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_34.setName("BEBER_CERVEZA");
 		
 		JLabel lblNewLabel = new JLabel("Salto espont.");
 		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		spinner_35 = new JSpinner();
 		spinner_35.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_35.setName("ENTREVISTA_INTERMEDIO");
 		
 		spinner_36 = new JSpinner();
 		spinner_36.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_36.setName("RETRANSMITIR_PARTIDO");
 		
 		spinner_37 = new JSpinner();
 		spinner_37.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_37.setName("HABLAR_SPEAKER");
 		
 		spinner_38 = new JSpinner();
 		spinner_38.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_38.setName("ACTIVAR_SOBORNO");
 		
 		spinner_39 = new JSpinner();
 		spinner_39.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_39.setName("DOBLAR_APUESTA");
 		
 		JLabel lblEntrevistaDesc = new JLabel("Entrevista desc.");
 		lblEntrevistaDesc.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblIniciarOla = new JLabel("Iniciar ola");
 		lblIniciarOla.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblPunteroLaser = new JLabel("Puntero laser");
 		lblPunteroLaser.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblTirarVengala = new JLabel("Tirar bengala");
 		lblTirarVengala.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblBeberCerveza = new JLabel("Beber cerveza");
 		lblBeberCerveza.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblRetransmision = new JLabel("Retransmision");
 		lblRetransmision.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblHablarSpeaker = new JLabel("Hablar speaker");
 		lblHablarSpeaker.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblActivarSoborno = new JLabel("Activar soborno");
 		lblActivarSoborno.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblDoblarApuesta = new JLabel("Doblar apuesta");
 		lblDoblarApuesta.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_8 = new JLabel("Equipo Local");
 		label_8.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		spinner_40 = new JSpinner();
 		spinner_40.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_40.setName("SALTO_ESPONTANEO");
 		
 		spinner_41 = new JSpinner();
 		spinner_41.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_41.setName("INICIAR_OLA");
 		
 		spinner_42 = new JSpinner();
 		spinner_42.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_42.setName("PUNTERO_LASER");
 		
 		spinner_43 = new JSpinner();
 		spinner_43.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_43.setName("TIRAR_BENGALA");
 		
 		spinner_44 = new JSpinner();
 		spinner_44.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_44.setName("BEBER_CERVEZA");
 		
 		JLabel label_9 = new JLabel("Salto espont.");
 		label_9.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		spinner_45 = new JSpinner();
 		spinner_45.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_45.setName("ENTREVISTA_INTERMEDIO");
 		
 		spinner_46 = new JSpinner();
 		spinner_46.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_46.setName("RETRANSMITIR_PARTIDO");
 		
 		spinner_47 = new JSpinner();
 		spinner_47.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_47.setName("HABLAR_SPEAKER");
 		
 		spinner_48 = new JSpinner();
 		spinner_48.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_48.setName("ACTIVAR_SOBORNO");
 		
 		spinner_49 = new JSpinner();
 		spinner_49.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_49.setName("DOBLAR_APUESTA");
 		
 		JLabel label_10 = new JLabel("Entrevista desc.");
 		label_10.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_11 = new JLabel("Iniciar ola");
 		label_11.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_12 = new JLabel("Puntero laser");
 		label_12.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_13 = new JLabel("Tirar bengala");
 		label_13.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_14 = new JLabel("Beber cerveza");
 		label_14.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_15 = new JLabel("Retransmision");
 		label_15.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_16 = new JLabel("Hablar speaker");
 		label_16.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_17 = new JLabel("Activar soborno");
 		label_17.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_18 = new JLabel("Doblar apuesta");
 		label_18.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblEquipoVisitante_1 = new JLabel("Equipo Visitante");
 		lblEquipoVisitante_1.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		button = new JButton("Cargar");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				buttonActionPerformed(arg0);
 			}
 		});
 		button.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
 		gl_panel_3.setHorizontalGroup(
 			gl_panel_3.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_30, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblNewLabel))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_31, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblIniciarOla, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_32, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblPunteroLaser, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_33, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblTirarVengala, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_34, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblBeberCerveza, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)))
 					.addGap(22)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_38, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblActivarSoborno, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_37, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblHablarSpeaker, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_36, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblRetransmision, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_35, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblEntrevistaDesc, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 							.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
 							.addGroup(gl_panel_3.createSequentialGroup()
 								.addComponent(spinner_39, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addComponent(lblDoblarApuesta, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))))
 					.addPreferredGap(ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_40, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
 							.addGap(49)
 							.addComponent(spinner_45, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_41, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
 							.addGap(22)
 							.addComponent(spinner_46, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_15, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_42, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
 							.addGap(22)
 							.addComponent(spinner_47, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_16, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_43, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
 							.addGap(22)
 							.addComponent(spinner_48, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_17, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(spinner_44, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
 							.addGap(22)
 							.addComponent(spinner_49, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(label_18, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGap(167)
 							.addComponent(lblEquipoVisitante_1)
 							.addGap(48)
 							.addComponent(button, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)))
 					.addGap(18))
 		);
 		gl_panel_3.setVerticalGroup(
 			gl_panel_3.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addGap(33)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_40, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addComponent(spinner_45, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGap(6)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_41, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addComponent(spinner_46, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_15, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGap(6)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_42, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addComponent(spinner_47, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_16, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGap(6)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_43, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addComponent(spinner_48, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_17, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGap(6)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_44, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addComponent(spinner_49, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_3.createSequentialGroup()
 									.addGap(6)
 									.addComponent(label_18, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGap(31)
 							.addComponent(lblEquipoVisitante_1, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_30, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblNewLabel)
 								.addComponent(spinner_35, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblEntrevistaDesc, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_31, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_36, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblIniciarOla, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblRetransmision, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_32, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_37, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblPunteroLaser, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblHablarSpeaker, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_33, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_38, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblTirarVengala, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblActivarSoborno, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_34, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblBeberCerveza, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_39, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblDoblarApuesta, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addGap(31)
 							.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap(45, Short.MAX_VALUE))
 				.addGroup(Alignment.TRAILING, gl_panel_3.createSequentialGroup()
 					.addContainerGap(229, Short.MAX_VALUE)
 					.addComponent(button, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 		panel_3.setLayout(gl_panel_3);
 		
 		spinner = new JSpinner();
 		spinner.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner.setName("MOTIVARSE");
 		
 		spinner_1 = new JSpinner();
 		spinner_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_1.setName("PINTARSE");
 		
 		spinner_2 = new JSpinner();
 		spinner_2.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_2.setName("PELEA_AFICIONES");
 		
 		spinner_3 = new JSpinner();
 		spinner_3.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_3.setName("CREAR_PANCARTA");
 		
 		spinner_4 = new JSpinner();
 		spinner_4.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_4.setName("PROMOCIONAR_EQUIPO");
 		
 		spinner_5 = new JSpinner();
 		spinner_5.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_5.setName("HACKEAR_PAGINA");
 		
 		spinner_6 = new JSpinner();
 		spinner_6.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_6.setName("ORGANIZAR_CENA");
 		
 		spinner_7 = new JSpinner();
 		spinner_7.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_7.setName("ORGANIZAR_HOMENAJE");
 		
 		spinner_8 = new JSpinner();
 		spinner_8.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_8.setName("CONTRATAR_RRPP");
 		
 		spinner_9 = new JSpinner();
 		spinner_9.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_9.setName("FINANCIAR_EVENTO");
 		
 		spinner_10 = new JSpinner();
 		spinner_10.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_10.setName("MEJORAR_GRADAS");
 		
 		spinner_11 = new JSpinner();
 		spinner_11.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_11.setName("SOBORNAR_LINIER");
 		
 		spinner_12 = new JSpinner();
 		spinner_12.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_12.setName("INCENTIVO_ECONOMICO");
 		
 		spinner_13 = new JSpinner();
 		spinner_13.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_13.setName("ASCENDER_TRABAJO");
 		
 		spinner_14 = new JSpinner();
 		spinner_14.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_14.setName("APOSTAR");
 		
 		spinner_15 = new JSpinner();
 		spinner_15.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_15.setName("MOTIVARSE");
 		
 		spinner_16 = new JSpinner();
 		spinner_16.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_16.setName("PINTARSE");
 		
 		spinner_17 = new JSpinner();
 		spinner_17.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_17.setName("PELEA_AFICIONES");
 		
 		spinner_18 = new JSpinner();
 		spinner_18.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_18.setName("CREAR_PANCARTA");
 		
 		spinner_19 = new JSpinner();
 		spinner_19.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_19.setName("PROMOCIONAR_EQUIPO");
 		
 		spinner_20 = new JSpinner();
 		spinner_20.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_20.setName("HACKEAR_PAGINA");
 		
 		spinner_21 = new JSpinner();
 		spinner_21.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_21.setName("ORGANIZAR_CENA");
 		
 		spinner_22 = new JSpinner();
 		spinner_22.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_22.setName("ORGANIZAR_HOMENAJE");
 		
 		spinner_23 = new JSpinner();
 		spinner_23.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_23.setName("CONTRATAR_RRPP");
 		
 		spinner_24 = new JSpinner();
 		spinner_24.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_24.setName("FINANCIAR_EVENTO");
 		
 		spinner_25 = new JSpinner();
 		spinner_25.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_25.setName("MEJORAR_GRADAS");
 		
 		spinner_26 = new JSpinner();
 		spinner_26.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_26.setName("SOBORNAR_LINIER");
 		
 		spinner_27 = new JSpinner();
 		spinner_27.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_27.setName("INCENTIVO_ECONOMICO");
 		
 		spinner_28 = new JSpinner();
 		spinner_28.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_28.setName("ASCENDER_TRABAJO");
 		
 		spinner_29 = new JSpinner();
 		spinner_29.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		spinner_29.setName("APOSTAR");
 		
 		JLabel lblMotivarse = new JLabel("Motivarse");
 		lblMotivarse.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblHackearpagina = new JLabel("Hackear pagina");
 		lblHackearpagina.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblMejorargradas = new JLabel("Mejorar gradas");
 		lblMejorargradas.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label = new JLabel("Motivarse");
 		label.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_1 = new JLabel("Hackear pagina");
 		label_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_2 = new JLabel("Mejorar gradas");
 		label_2.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		btnCargar = new JButton("Cargar");
 		btnCargar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				bntCargarActionPerformed(arg0);
 			}
 		});
 		btnCargar.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblPintarse = new JLabel("Pintarse");
 		lblPintarse.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_3 = new JLabel("Pintarse");
 		label_3.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblPelea = new JLabel("Pelea");
 		lblPelea.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_4 = new JLabel("Pelea");
 		label_4.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblPancarta = new JLabel("Pancarta");
 		lblPancarta.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_5 = new JLabel("Pancarta");
 		label_5.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblPromocionarEquipo = new JLabel("Promocionar\r\nequipo");
 		lblPromocionarEquipo.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_6 = new JLabel("Promocionar\r\nequipo");
 		label_6.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblOrgCena = new JLabel("Org. Cena");
 		lblOrgCena.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel label_7 = new JLabel("Org. Cena");
 		label_7.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblOrgHomenaje = new JLabel("Org. Homenaje");
 		lblOrgHomenaje.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblRrppNuevo = new JLabel("RRPP nuevo");
 		lblRrppNuevo.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblFinanciacin = new JLabel("Financiacion");
 		lblFinanciacin.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblSoborno = new JLabel("Soborno");
 		lblSoborno.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblIncentivo = new JLabel("Incentivo");
 		lblIncentivo.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblAscenderTr = new JLabel("Ascender Trb.");
 		lblAscenderTr.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblApostar = new JLabel("Apostar");
 		lblApostar.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblFinanciacin_1 = new JLabel("Financiacion");
 		lblFinanciacin_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblRrppNuevo_1 = new JLabel("RRPP nuevo");
 		lblRrppNuevo_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblOrgHomenaje_1 = new JLabel("Org. Homenaje");
 		lblOrgHomenaje_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblSoborno_1 = new JLabel("Soborno");
 		lblSoborno_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblIncentivo_1 = new JLabel("Incentivo");
 		lblIncentivo_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblAscenderTrb = new JLabel("Ascender Trb.");
 		lblAscenderTrb.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblApostar_1 = new JLabel("Apostar");
 		lblApostar_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		
 		JLabel lblEquipoLocal = new JLabel("Equipo Local");
 		lblEquipoLocal.setFont(new Font("Tahoma", Font.BOLD, 12));
 		
 		JLabel lblEquipoVisitante = new JLabel("Equipo Visitante");
 		lblEquipoVisitante.setFont(new Font("Tahoma", Font.BOLD, 12));
 		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
 		gl_panel_2.setHorizontalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblMotivarse)
 								.addComponent(lblPintarse, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblPelea, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblPancarta, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblPromocionarEquipo, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 							.addGap(18)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblHackearpagina)
 								.addComponent(lblOrgCena, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING, false)
 									.addComponent(lblFinanciacin, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(lblRrppNuevo, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(lblOrgHomenaje, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 							.addGap(18)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
 								.addComponent(spinner_10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 									.addComponent(spinner_11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(spinner_12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(spinner_13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(spinner_14, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING, false)
 								.addComponent(lblMejorargradas, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addComponent(lblSoborno, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblIncentivo, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblApostar, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblAscenderTr, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGap(155)
 							.addComponent(lblEquipoLocal)))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING, false)
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(spinner_15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(label, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(spinner_16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(spinner_17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(spinner_18, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addComponent(spinner_19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)))
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(spinner_20, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_21, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_22, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_23, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(spinner_24, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
 								.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
 								.addGroup(gl_panel_2.createSequentialGroup()
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING, false)
 										.addComponent(lblFinanciacin_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addComponent(lblRrppNuevo_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addComponent(lblOrgHomenaje_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
 							.addPreferredGap(ComponentPlacement.RELATED))
 						.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
 							.addComponent(lblEquipoVisitante, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
 							.addGap(39)))
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(btnCargar, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(spinner_29, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblApostar_1, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(spinner_28, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblAscenderTrb, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
 							.addPreferredGap(ComponentPlacement.RELATED))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(spinner_27, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblIncentivo_1, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(spinner_26, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblSoborno_1, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(spinner_25, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)))
 					.addGap(5))
 		);
 		gl_panel_2.setVerticalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
 							.addGroup(gl_panel_2.createSequentialGroup()
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblMotivarse))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblPintarse, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblPelea, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblPancarta, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblPromocionarEquipo, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 							.addGroup(gl_panel_2.createSequentialGroup()
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 									.addComponent(spinner_10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(lblMejorargradas, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
 									.addComponent(spinner_15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(label, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 									.addGroup(gl_panel_2.createSequentialGroup()
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(lblSoborno, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(lblIncentivo, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(lblAscenderTr, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_14, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(lblApostar, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 									.addGroup(gl_panel_2.createSequentialGroup()
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_18, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 											.addComponent(spinner_19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 											.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))))))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblHackearpagina))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblOrgCena, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblOrgHomenaje, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblRrppNuevo, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblFinanciacin, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_20, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_21, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_22, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblOrgHomenaje_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_23, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblRrppNuevo_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_24, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblFinanciacin_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_25, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_26, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblSoborno_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_27, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblIncentivo_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_28, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblAscenderTrb, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(spinner_29, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblApostar_1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))))
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGap(31)
 							.addComponent(lblEquipoLocal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addGap(11)
 							.addComponent(btnCargar, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap(15, Short.MAX_VALUE))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGap(18)
 							.addComponent(lblEquipoVisitante, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
 							.addContainerGap())))
 		);
 		panel_2.setLayout(gl_panel_2);
 		panel.setLayout(gl_panel);
 		frmJugadorNumero.getContentPane().setLayout(groupLayout);
 	}
 }
