 package presentacion;
 
 import logica.Gestor;
 import logica.Mecenas;
 import logica.Mecenazgo;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.SystemColor;
 import java.sql.SQLException;
 import java.util.TreeMap;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.JOptionPane;
 
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 public class PantallaActualizarMecenas extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtId;
 	private JTextField txtNombre;
 	private JTextField txtPaisNacimiento;
 	private JTextField txtCiudadNacimiento;
 	private JTextField txtFechaMuerte;
 	private JButton btnBorrar;
 	private JButton btnActualizar;
 	private Mecenas mecenas;
 	private JButton btnCrearMecenazgo;
 	private JButton btnEditarMecenazgo;
 	private List listMecenazgos;
 
 	public PantallaActualizarMecenas( Mecenas pMecenas ) {
 		
 		mecenas = pMecenas;
 		setTitle("Actualizar Mecenas");
 		setResizable(false);
 		setBounds(100, 100, 407, 358);
 		contentPane = new JPanel();
 		contentPane.setBackground(SystemColor.inactiveCaptionBorder);
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblId = new JLabel("Id");
 		lblId.setDisplayedMnemonic('I');
 		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblId.setBounds(72, 10, 121, 14);
 		contentPane.add(lblId);
 		
 		txtId = new JTextField();
 		lblId.setLabelFor(txtId);
 		txtId.setEditable( false );
 		txtId.setBounds(203, 5, 86, 20);
 		txtId.setText( String.valueOf( mecenas.getId() ) );
 		contentPane.add(txtId);
 		
 		JLabel lblNombre = new JLabel("Nombre");
 		lblNombre.setDisplayedMnemonic('N');
 		lblNombre.setLabelFor(lblNombre);
 		lblNombre.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNombre.setBounds(147, 34, 46, 14);
 		contentPane.add(lblNombre);
 		
 		txtNombre = new JTextField();
 		txtNombre.setBounds(203, 30, 86, 20);
 		txtNombre.setText( mecenas.getNombre() );
 		contentPane.add(txtNombre);
 		
 		JLabel lblPaisNacimiento = new JLabel("Pa\u00EDs de nacimiento");
 		lblPaisNacimiento.setDisplayedMnemonic('P');
 		lblPaisNacimiento.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPaisNacimiento.setBounds(72, 58, 121, 14);
 		contentPane.add(lblPaisNacimiento);
 		
 		txtPaisNacimiento = new JTextField();
 		lblPaisNacimiento.setLabelFor(txtPaisNacimiento);
 		txtPaisNacimiento.setBounds(203, 55, 86, 20);
 		txtPaisNacimiento.setText( mecenas.getNacionalidad() );
 		contentPane.add(txtPaisNacimiento);
 		
 		JLabel lblCiudadNacimiento = new JLabel("Ciudad de nacimiento");
 		lblCiudadNacimiento.setDisplayedMnemonic('P');
 		lblCiudadNacimiento.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCiudadNacimiento.setBounds(72, 82, 121, 14);
 		contentPane.add(lblCiudadNacimiento);
 		
 		txtCiudadNacimiento = new JTextField();
 		lblCiudadNacimiento.setLabelFor(txtCiudadNacimiento);
 		txtCiudadNacimiento.setBounds(203, 80, 86, 20);
 		txtCiudadNacimiento.setText( mecenas.getCiudadNacimiento() );
 		contentPane.add(txtCiudadNacimiento);
 		
 		JLabel lblFechaMuerte = new JLabel("Fecha de muerte");
 		lblFechaMuerte.setDisplayedMnemonic('F');
 		lblFechaMuerte.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblFechaMuerte.setBounds(96, 106, 97, 14);
 		contentPane.add(lblFechaMuerte);
 		
 		txtFechaMuerte = new JTextField();
 		lblFechaMuerte.setLabelFor(txtFechaMuerte);
 		txtFechaMuerte.setBounds(203, 105, 86, 20);
 		txtFechaMuerte.setText( mecenas.getFechaMuerte() );
 		contentPane.add(txtFechaMuerte);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnCancel.setBounds(104, 295, 89, 23);
 		contentPane.add(btnCancel);
 		
 		btnBorrar = new JButton("Borrar");
 		btnBorrar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Gestor.borrarMecenas(mecenas.getId());
 					JOptionPane.showMessageDialog( null, "El mecenas ha sido eliminado exitosamente.");
 					setVisible(false);
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error");
 				}
 			}
 		});
 		btnBorrar.setBounds(203, 295, 89, 23);
 		contentPane.add(btnBorrar);
 		
 		btnActualizar = new JButton("Actualizar");
 		btnActualizar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Gestor.actualizarMecenas(mecenas.getId(), txtNombre.getText(), txtPaisNacimiento.getText(), txtCiudadNacimiento.getText(), txtFechaMuerte.getText());
 					JOptionPane.showMessageDialog( null, "El mecenas ha sido actualizo exitosamente!" );
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog( null, "Hubo un error\nPor favor revise los datos ingresados");
 				}
 			}
 		});
 		btnActualizar.setBounds(302, 295, 89, 23);
 		contentPane.add(btnActualizar);
 		
 		btnCrearMecenazgo = new JButton("Crear Mecenazgo");
 		btnCrearMecenazgo.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				btnCrearMecenazgo_mouseClicked(e);
 			}
 		});
 		btnCrearMecenazgo.setBounds(10, 168, 121, 23);
 		contentPane.add(btnCrearMecenazgo);
 		
 		btnEditarMecenazgo = new JButton("Editar Mecenazgo");
 		btnEditarMecenazgo.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				btnEditarMecenazgo_mouseClicked(e);
 			}
 		});
 		btnEditarMecenazgo.setBounds(10, 229, 121, 23);
 		contentPane.add(btnEditarMecenazgo);
 		
 		listMecenazgos = new List();
 		listMecenazgos.setBounds(141, 131, 250, 146);
 		contentPane.add(listMecenazgos);
 		
 		JLabel lblListaDeMecenazgos = new JLabel("Lista de Mecenazgos");
 		lblListaDeMecenazgos.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblListaDeMecenazgos.setBounds(10, 131, 121, 14);
 		contentPane.add(lblListaDeMecenazgos);
 		
 		if( pMecenas.getMecenazgos().size() > 0 ) {
 			for ( Mecenazgo m : pMecenas.getMecenazgos() ) {
 				listMecenazgos.add( "Pintor: " + m.getPintor().getId() + ", " + m.getFechaInicio() + " - " + m.getFechaFin() );
 			}
 		}
 		
 	}
 	
 	
 	public void btnCrearMecenazgo_mouseClicked(MouseEvent e) {
 		try {
 			PantallaCrearMecenazgo p;
 			p = new PantallaCrearMecenazgo(txtId.getText());
 			p.setVisible(true);
 		}
 		catch (Exception ex) {
 			JOptionPane.showMessageDialog(this,"Error","Error",JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	public void btnEditarMecenazgo_mouseClicked(MouseEvent e) {
 		if( listMecenazgos.getSelectedIndex() == -1 ){
 			JOptionPane.showMessageDialog(null,"Debe seleccionar un item de la lista para editarlo." );
 		}
 		else {
 			try {
 				Mecenazgo mecenazgo = Gestor.consultarMecenazgo( mecenas.getMecenazgos(), listMecenazgos.getSelectedIndex() );
 				PantallaActualizarMecenazgo pantallaMecenazgo = new PantallaActualizarMecenazgo( mecenazgo );
 				pantallaMecenazgo.setVisible(true);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog( null, "No se ha podido encontrar por el id indicado");
 			}
 		}
 	}
 }
