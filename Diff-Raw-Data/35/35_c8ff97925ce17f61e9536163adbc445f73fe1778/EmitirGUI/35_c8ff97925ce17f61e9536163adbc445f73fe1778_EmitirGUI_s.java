 package ar.edu.utn.frsf.licenciator.gui.windows;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import ar.edu.utn.frsf.licenciator.entidades.ClaseLicencia;
 import ar.edu.utn.frsf.licenciator.entidades.Licencia;
 import ar.edu.utn.frsf.licenciator.entidades.TipoDoc;
 import ar.edu.utn.frsf.licenciator.entidades.Titular;
 import ar.edu.utn.frsf.licenciator.logica.GestorLicencias;
 
 public class EmitirGUI extends JDialog {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private Titular titular;
 	private Licencia licencia;
 	private JTextField textApellido;
 	private JTextField textNombre;
 	private JTextField textDomicilio;
 	private JTextField textLocalidad;
 	private JFormattedTextField textNac;
 	private JTextField textDonante;
 	private JTextField textFactor;
 	private JComboBox<ClaseLicencia> textClase;
 	private JTextField textNroLic;
 	private JFormattedTextField textFE;
 	private JFormattedTextField textFV;
 	private JTextField textNroDoc;
 	private JTextPane textObs;
 	private JComboBox<TipoDoc> textTipoDoc;
 	private JButton btnCrearLic;
 	private JButton btnEmitirLic;
 	private JButton btnBuscar;
 	
 	
 	/**
 	 * Lanza una nueva instancia de la ventana de Alta Usuario.
 	 * 
 	 * @param superusuario Si es true, nos habilita la opcion de crear un superusuario.
 	 *                     Si es false, no aparece dicha opcin.
 	 */
 	public static void lanzarGUI(Frame owner) {
 		try {
 			EmitirGUI dialog = new EmitirGUI(owner, "Emitir Licencia", true);
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setLocationRelativeTo( null );
 			dialog.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected EmitirGUI() { super(); inicializar(); }
 	
 	public EmitirGUI(Frame owner, String string, boolean b) {
 		super(owner, string, b);
 		inicializar();
 	}
 	
 	/**
 	 * Create the panel.
 	 */
 	private void inicializar() {
 		
 		setMinimumSize(new Dimension(430, 550));
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setTitle("Emitir Licencia");
 		setResizable(false);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] {10, 0, 10};
 		gridBagLayout.rowHeights = new int[] {30, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0};
 		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0};
 		getContentPane().setLayout(gridBagLayout);
 		
 		JPanel panel_2 = new JPanel();
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_2.gridx = 1;
 		gbc_panel_2.gridy = 0;
 		getContentPane().add(panel_2, gbc_panel_2);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[] {0, 100, 0};
 		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
 		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0};
 		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		panel_2.setLayout(gbl_panel_2);
 		
 		JLabel label_14 = new JLabel("Tipo Documento:");
 		GridBagConstraints gbc_label_14 = new GridBagConstraints();
 		gbc_label_14.anchor = GridBagConstraints.EAST;
 		gbc_label_14.insets = new Insets(0, 0, 5, 5);
 		gbc_label_14.gridx = 0;
 		gbc_label_14.gridy = 0;
 		panel_2.add(label_14, gbc_label_14);
 		
 		textTipoDoc = new JComboBox<TipoDoc>();
 		// Completo el comboBox con los tipos de documento
 		for(TipoDoc a: GestorLicencias.obtenerTiposDocumento()) {
 			textTipoDoc.addItem(a);
 		}
 		GridBagConstraints gbc_textTipoDoc = new GridBagConstraints();
 		gbc_textTipoDoc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textTipoDoc.insets = new Insets(0, 0, 5, 5);
 		gbc_textTipoDoc.gridx = 1;
 		gbc_textTipoDoc.gridy = 0;
 		panel_2.add(textTipoDoc, gbc_textTipoDoc);
 		
 		
 		
 		JLabel label_15 = new JLabel("Nro Documento:");
 		GridBagConstraints gbc_label_15 = new GridBagConstraints();
 		gbc_label_15.anchor = GridBagConstraints.EAST;
 		gbc_label_15.insets = new Insets(0, 0, 0, 5);
 		gbc_label_15.gridx = 0;
 		gbc_label_15.gridy = 1;
 		panel_2.add(label_15, gbc_label_15);
 		
 		textNroDoc = new JTextField();
 		textNroDoc.setColumns(10);
 		GridBagConstraints gbc_textNroDoc = new GridBagConstraints();
 		gbc_textNroDoc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textNroDoc.insets = new Insets(0, 0, 0, 5);
 		gbc_textNroDoc.gridx = 1;
 		gbc_textNroDoc.gridy = 1;
 		panel_2.add(textNroDoc, gbc_textNroDoc);
 		
 		btnBuscar = new JButton("Buscar");
 		btnBuscar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (validar_numero(textNroDoc.getText())) {
					titular = GestorLicencias.buscarTitular(textTipoDoc.getSelectedItem().toString(), Long.parseLong(textNroDoc.getText()));
 					if (titular != null){
 						setTitular();
 					}
 					else{
 						JOptionPane.showMessageDialog(null, "El titular ingresado no existe.\nRevise los datos ingresados o vea la opcin Dar de alta -> Alta Titular del men principal", "Error", JOptionPane.ERROR_MESSAGE);
 						clearTitular();
 					}
 				}
 				else
 				{ // Si no existe el titular, borro todos los campos.
 					JOptionPane.showMessageDialog(null, "El nmero de documento que ha ingresado no es vlido", "Error", JOptionPane.ERROR_MESSAGE);
 					clearTitular();
 				}
 			}
 
 			private boolean validar_numero(String text) {
 				for(char a: text.toCharArray()) {
 					if(a < '0' || a > '9')
 						return false;
 				}
 				return true;
 			}
 		});
 		GridBagConstraints gbc_button = new GridBagConstraints();
 		gbc_button.insets = new Insets(0, 0, 0, 5);
 		gbc_button.gridx = 2;
 		gbc_button.gridy = 1;
 		panel_2.add(btnBuscar, gbc_button);
 		
 		JSeparator separator_1 = new JSeparator();
 		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
 		gbc_separator_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separator_1.insets = new Insets(0, 0, 5, 5);
 		gbc_separator_1.gridx = 1;
 		gbc_separator_1.gridy = 1;
 		getContentPane().add(separator_1, gbc_separator_1);
 		
 		JLabel label = new JLabel("Datos del titular");
 		label.setFont(new Font("Tahoma", Font.PLAIN, 17));
 		GridBagConstraints gbc_label = new GridBagConstraints();
 		gbc_label.anchor = GridBagConstraints.WEST;
 		gbc_label.insets = new Insets(0, 0, 5, 5);
 		gbc_label.gridx = 1;
 		gbc_label.gridy = 2;
 		getContentPane().add(label, gbc_label);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.fill = GridBagConstraints.VERTICAL;
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 3;
 		getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] {150, 200, 30};
 		gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 		panel.setLayout(gbl_panel);
 		
 		JLabel label_1 = new JLabel("Apellidos:");
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.anchor = GridBagConstraints.EAST;
 		gbc_label_1.insets = new Insets(0, 0, 5, 5);
 		gbc_label_1.gridx = 0;
 		gbc_label_1.gridy = 0;
 		panel.add(label_1, gbc_label_1);
 		
 		textApellido = new JTextField();
 		textApellido.setEnabled(false);
 		textApellido.setColumns(10);
 		GridBagConstraints gbc_textApellido = new GridBagConstraints();
 		gbc_textApellido.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textApellido.insets = new Insets(0, 0, 5, 0);
 		gbc_textApellido.gridx = 1;
 		gbc_textApellido.gridy = 0;
 		panel.add(textApellido, gbc_textApellido);
 		
 		JLabel label_2 = new JLabel("Nombre:");
 		GridBagConstraints gbc_label_2 = new GridBagConstraints();
 		gbc_label_2.anchor = GridBagConstraints.EAST;
 		gbc_label_2.insets = new Insets(0, 0, 5, 5);
 		gbc_label_2.gridx = 0;
 		gbc_label_2.gridy = 1;
 		panel.add(label_2, gbc_label_2);
 		
 		textNombre = new JTextField();
 		textNombre.setEnabled(false);
 		textNombre.setColumns(10);
 		GridBagConstraints gbc_textNombre = new GridBagConstraints();
 		gbc_textNombre.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textNombre.insets = new Insets(0, 0, 5, 0);
 		gbc_textNombre.gridx = 1;
 		gbc_textNombre.gridy = 1;
 		panel.add(textNombre, gbc_textNombre);
 		
 		JLabel label_3 = new JLabel("Domicilio:");
 		GridBagConstraints gbc_label_3 = new GridBagConstraints();
 		gbc_label_3.anchor = GridBagConstraints.EAST;
 		gbc_label_3.insets = new Insets(0, 0, 5, 5);
 		gbc_label_3.gridx = 0;
 		gbc_label_3.gridy = 2;
 		panel.add(label_3, gbc_label_3);
 		
 		textDomicilio = new JTextField();
 		textDomicilio.setEnabled(false);
 		textDomicilio.setColumns(10);
 		GridBagConstraints gbc_textDomicilio = new GridBagConstraints();
 		gbc_textDomicilio.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textDomicilio.insets = new Insets(0, 0, 5, 0);
 		gbc_textDomicilio.gridx = 1;
 		gbc_textDomicilio.gridy = 2;
 		panel.add(textDomicilio, gbc_textDomicilio);
 		
 		JLabel label_4 = new JLabel("Localidad:");
 		GridBagConstraints gbc_label_4 = new GridBagConstraints();
 		gbc_label_4.anchor = GridBagConstraints.EAST;
 		gbc_label_4.insets = new Insets(0, 0, 5, 5);
 		gbc_label_4.gridx = 0;
 		gbc_label_4.gridy = 3;
 		panel.add(label_4, gbc_label_4);
 		
 		textLocalidad = new JTextField();
 		textLocalidad.setEnabled(false);
 		textLocalidad.setColumns(10);
 		GridBagConstraints gbc_textLocalidad = new GridBagConstraints();
 		gbc_textLocalidad.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textLocalidad.insets = new Insets(0, 0, 5, 0);
 		gbc_textLocalidad.gridx = 1;
 		gbc_textLocalidad.gridy = 3;
 		panel.add(textLocalidad, gbc_textLocalidad);
 		
 		JLabel label_5 = new JLabel("Fecha de Nacimiento:");
 		GridBagConstraints gbc_label_5 = new GridBagConstraints();
 		gbc_label_5.anchor = GridBagConstraints.EAST;
 		gbc_label_5.insets = new Insets(0, 0, 5, 5);
 		gbc_label_5.gridx = 0;
 		gbc_label_5.gridy = 4;
 		panel.add(label_5, gbc_label_5);
 		
 		textNac = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));   
 		textNac.setEnabled(false);
 		textNac.setColumns(10);
 		GridBagConstraints gbc_textNac = new GridBagConstraints();
 		gbc_textNac.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textNac.insets = new Insets(0, 0, 5, 0);
 		gbc_textNac.gridx = 1;
 		gbc_textNac.gridy = 4;
 		panel.add(textNac, gbc_textNac);
 		
 		JLabel label_6 = new JLabel("Donante:");
 		GridBagConstraints gbc_label_6 = new GridBagConstraints();
 		gbc_label_6.anchor = GridBagConstraints.EAST;
 		gbc_label_6.insets = new Insets(0, 0, 5, 5);
 		gbc_label_6.gridx = 0;
 		gbc_label_6.gridy = 5;
 		panel.add(label_6, gbc_label_6);
 		
 		textDonante = new JTextField();
 		textDonante.setEnabled(false);
 		textDonante.setColumns(10);
 		GridBagConstraints gbc_textDonante = new GridBagConstraints();
 		gbc_textDonante.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textDonante.insets = new Insets(0, 0, 5, 0);
 		gbc_textDonante.gridx = 1;
 		gbc_textDonante.gridy = 5;
 		panel.add(textDonante, gbc_textDonante);
 		
 		JLabel label_7 = new JLabel("Grupo y factor sanguineo:");
 		GridBagConstraints gbc_label_7 = new GridBagConstraints();
 		gbc_label_7.anchor = GridBagConstraints.EAST;
 		gbc_label_7.insets = new Insets(0, 0, 0, 5);
 		gbc_label_7.gridx = 0;
 		gbc_label_7.gridy = 6;
 		panel.add(label_7, gbc_label_7);
 		
 		textFactor = new JTextField();
 		textFactor.setEnabled(false);
 		textFactor.setColumns(10);
 		GridBagConstraints gbc_textFactor = new GridBagConstraints();
 		gbc_textFactor.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFactor.gridx = 1;
 		gbc_textFactor.gridy = 6;
 		panel.add(textFactor, gbc_textFactor);
 		
 		JSeparator separator = new JSeparator();
 		GridBagConstraints gbc_separator = new GridBagConstraints();
 		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separator.insets = new Insets(0, 0, 5, 5);
 		gbc_separator.gridx = 1;
 		gbc_separator.gridy = 4;
 		getContentPane().add(separator, gbc_separator);
 		
 		JLabel label_8 = new JLabel("Datos de la licencia");
 		label_8.setFont(new Font("Tahoma", Font.PLAIN, 17));
 		GridBagConstraints gbc_label_8 = new GridBagConstraints();
 		gbc_label_8.insets = new Insets(0, 0, 5, 5);
 		gbc_label_8.anchor = GridBagConstraints.WEST;
 		gbc_label_8.gridx = 1;
 		gbc_label_8.gridy = 5;
 		getContentPane().add(label_8, gbc_label_8);
 		
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
 		gbc_panel_1.gridx = 1;
 		gbc_panel_1.gridy = 6;
 		getContentPane().add(panel_1, gbc_panel_1);
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[] {150, 200, 30};
 		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0};
 		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		panel_1.setLayout(gbl_panel_1);
 		
 		JLabel label_9 = new JLabel("Clase:");
 		GridBagConstraints gbc_label_9 = new GridBagConstraints();
 		gbc_label_9.anchor = GridBagConstraints.EAST;
 		gbc_label_9.insets = new Insets(0, 0, 5, 5);
 		gbc_label_9.gridx = 0;
 		gbc_label_9.gridy = 0;
 		panel_1.add(label_9, gbc_label_9);
 		
 		textClase = new JComboBox<ClaseLicencia>();
 		// Completo el comboBox con las distintas clase de licencia
 		for(ClaseLicencia a: GestorLicencias.obtenerTiposDeLicencia()) {
 			textClase.addItem(a);
 		}
 		textClase.setEnabled(false);
 		GridBagConstraints gbc_textClase = new GridBagConstraints();
 		gbc_textClase.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textClase.insets = new Insets(0, 0, 5, 5);
 		gbc_textClase.gridx = 1;
 		gbc_textClase.gridy = 0;
 		panel_1.add(textClase, gbc_textClase);
 		
 		btnCrearLic = new JButton(">");
 		btnCrearLic.setEnabled(false);
 		
 		/* Cuando se pulsa el boton > */
 		btnCrearLic.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 					licencia = GestorLicencias.emitirLicencia(titular, (ClaseLicencia)textClase.getSelectedItem(), textObs.getText());
 					if (licencia != null)
 					{
 						setLicencia();
 					} else {
 						JOptionPane.showMessageDialog(null, "No es posible emitir la licencia solicitada para este titular.", "Error", JOptionPane.ERROR_MESSAGE);
 					}
 
 			}
 		});
 		GridBagConstraints gbc_btnEmitir = new GridBagConstraints();
 		gbc_btnEmitir.insets = new Insets(0, 0, 5, 0);
 		gbc_btnEmitir.gridx = 2;
 		gbc_btnEmitir.gridy = 0;
 		panel_1.add(btnCrearLic, gbc_btnEmitir);
 		
 		JLabel label_10 = new JLabel("N\u00FAmero:");
 		GridBagConstraints gbc_label_10 = new GridBagConstraints();
 		gbc_label_10.anchor = GridBagConstraints.EAST;
 		gbc_label_10.insets = new Insets(0, 0, 5, 5);
 		gbc_label_10.gridx = 0;
 		gbc_label_10.gridy = 1;
 		panel_1.add(label_10, gbc_label_10);
 		
 		textNroLic = new JTextField();
 		textNroLic.setEnabled(false);
 		textNroLic.setColumns(10);
 		GridBagConstraints gbc_textNroLic = new GridBagConstraints();
 		gbc_textNroLic.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textNroLic.insets = new Insets(0, 0, 5, 5);
 		gbc_textNroLic.gridx = 1;
 		gbc_textNroLic.gridy = 1;
 		panel_1.add(textNroLic, gbc_textNroLic);
 		
 		JLabel label_11 = new JLabel("Fecha de emisi\u00F3n:");
 		GridBagConstraints gbc_label_11 = new GridBagConstraints();
 		gbc_label_11.anchor = GridBagConstraints.EAST;
 		gbc_label_11.insets = new Insets(0, 0, 5, 5);
 		gbc_label_11.gridx = 0;
 		gbc_label_11.gridy = 2;
 		panel_1.add(label_11, gbc_label_11);
 		
 		textFE = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy")); 
 		textFE.setEnabled(false);
 		textFE.setColumns(10);
 		GridBagConstraints gbc_textFE = new GridBagConstraints();
 		gbc_textFE.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFE.insets = new Insets(0, 0, 5, 5);
 		gbc_textFE.gridx = 1;
 		gbc_textFE.gridy = 2;
 		panel_1.add(textFE, gbc_textFE);
 		
 		JLabel label_12 = new JLabel("Fecha de vencimiento:");
 		GridBagConstraints gbc_label_12 = new GridBagConstraints();
 		gbc_label_12.anchor = GridBagConstraints.EAST;
 		gbc_label_12.insets = new Insets(0, 0, 5, 5);
 		gbc_label_12.gridx = 0;
 		gbc_label_12.gridy = 3;
 		panel_1.add(label_12, gbc_label_12);
 		
 		textFV =  new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy")); 
 		textFV.setEnabled(false);
 		textFV.setColumns(10);
 		GridBagConstraints gbc_textFV = new GridBagConstraints();
 		gbc_textFV.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textFV.insets = new Insets(0, 0, 5, 5);
 		gbc_textFV.gridx = 1;
 		gbc_textFV.gridy = 3;
 		panel_1.add(textFV, gbc_textFV);
 		
 		JLabel label_13 = new JLabel("Observaciones:");
 		GridBagConstraints gbc_label_13 = new GridBagConstraints();
 		gbc_label_13.anchor = GridBagConstraints.NORTHEAST;
 		gbc_label_13.insets = new Insets(0, 0, 0, 5);
 		gbc_label_13.gridx = 0;
 		gbc_label_13.gridy = 4;
 		panel_1.add(label_13, gbc_label_13);
 		
 		textObs = new JTextPane();
 		textObs.setEnabled(false);
 		textObs.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		GridBagConstraints gbc_textObs = new GridBagConstraints();
 		gbc_textObs.insets = new Insets(0, 0, 0, 5);
 		gbc_textObs.fill = GridBagConstraints.BOTH;
 		gbc_textObs.gridx = 1;
 		gbc_textObs.gridy = 4;
 		panel_1.add(textObs, gbc_textObs);
 		
 		JSeparator separator_2 = new JSeparator();
 		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
 		gbc_separator_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separator_2.insets = new Insets(0, 0, 5, 5);
 		gbc_separator_2.gridx = 1;
 		gbc_separator_2.gridy = 7;
 		getContentPane().add(separator_2, gbc_separator_2);
 		
 		JPanel panel_4 = new JPanel();
 		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
 		gbc_panel_4.anchor = GridBagConstraints.EAST;
 		gbc_panel_4.insets = new Insets(0, 0, 0, 5);
 		gbc_panel_4.gridx = 1;
 		gbc_panel_4.gridy = 8;
 		getContentPane().add(panel_4, gbc_panel_4);
 		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
 		
 		btnEmitirLic = new JButton("Emitir");
 		btnEmitirLic.setEnabled(false);
 		btnEmitirLic.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (licencia !=null)
 				{
 					licencia.setObservaciones(textObs.getText());
 					GestorLicencias.guardarLicencia(MenuPrincipal.getInstancia().getUsuario(), licencia);
 					//TODO: Como sabe si la persistio bien?
 					dispose();
 					ImprimirGUI.LanzarGUI(MenuPrincipal.getInstancia(), licencia);
 				}
 			}
 		});
 		panel_4.add(btnEmitirLic);
 		
 		JButton btnCancelar = new JButton("Cancelar");
 		btnCancelar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		});
 		panel_4.add(btnCancelar);
 
 	}
 
 	private void setTitular() {
 		textApellido.setText(titular.getApellido());
 		textNombre.setText(titular.getNombre());
 		textDomicilio.setText(titular.getDomicilio());
 		textLocalidad.setText(titular.getLocalidad());
 		textNac.setValue(titular.getFechaNac().getTime());
 		if (titular.getDonante()){
 			textDonante.setText("Si");
 		} else {
 			textDonante.setText("No");
 		}
 		textFactor.setText(titular.getTipoSanguineo().getGrupo() + titular.getTipoSanguineo().getFactor());
 		
 		// Habilito los campos correspondientes de la seccion "Datos de la licencia"
 		textClase.setEnabled(true);
 		textObs.setEnabled(true);
 		btnCrearLic.setEnabled(true);
 	}
 	
 	private void clearTitular() {
 		textApellido.setText("");
 		textNombre.setText("");
 		textDomicilio.setText("");
 		textLocalidad.setText("");
 		textNac.setText("");
 		textDonante.setText("");
 		textFactor.setText("");
 		textClase.setSelectedIndex(0);
 		textObs.setText("");
 		
 		// Deshabilito los campos correspondientes de la seccion "Datos de la licencia"
 		textClase.setEnabled(false);
 		textObs.setEnabled(false);
 		btnCrearLic.setEnabled(false);
 	}
 	
 	private void setLicencia() {
 		textNroLic.setText(licencia.getNrolicencia());
 		textFE.setValue(licencia.getFechaEmision().getTime());
 		textFV.setValue(licencia.getFechaVencimiento().getTime());
 
 		/*Deshabilitamos los campos editables*/
 		textTipoDoc.setEnabled(false);
 		textNroDoc.setEnabled(false);
 		textClase.setEnabled(false);
 		
 		/* Deshabilito el boton de buscar titular */
 		btnBuscar.setEnabled(false);
 		
 		/* Deshabilito el boton de crear licencia ( > )*/
 		btnCrearLic.setEnabled(false);
 		
 		/* Habilitamos el boton de Emitir Licencia */
 		btnEmitirLic.setEnabled(true);
 	}
 	
 	
 }
