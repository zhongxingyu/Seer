 package code.google.com.opengis.gestionVISUAL;
 
 import javax.swing.JPanel;
 
 import javax.swing.JLabel;
 import java.awt.Rectangle;
 import javax.swing.JButton;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JComboBox;
 
 import code.google.com.opengis.gestion.Parcela;
 import code.google.com.opengis.gestion.Usuarios;
 import code.google.com.opengis.gestionDAO.ConectarDBA;
 import code.google.com.opengis.gestionDAO.Idioma;
 
 import java.awt.Font;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class ParcelasPanelNuevo extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JLabel lblAliasp = null;
 	private JLabel lblProvinciap = null;
 	private JLabel lblIdParcela = null;
 	private JLabel lblPoligonop = null;
 	private JLabel lblPartidap = null;
 	private JLabel lblNumerop = null;
 	private JLabel lblDnip = null;
 	private JLabel lblPoblacionp = null;
 	private JLabel lblCamposObligatorios;
 	
 	private JButton bGuardar = null;
 	private JButton bRestablecer = null;
 	
 	private JTextField txtIdParcelap = null;
 	private JTextField txtAliasp = null;
 	private JTextField txtProvinciap = null;
 	private JTextField txtPoblacion = null;
 	private JTextField txtPoligonop = null;
 	private JTextField txtPartidap = null;
 	JTextField txtDniPropietario = null;
 	private JTextField txtNumerop = null;
 	private String accion;
 	private ConectarDBA dba =null;
 	private boolean encontrado=true;
 	
 	private String idparcela; //identificador de la parcela
 	private String alias; //nombre que el usuario asignara a la parcela para relacionarlo
 	private String provincia;  //provincia de la parcela
 	private String poblacion; //poblacion de la parcela
 	private String poligono; //poligono de la parcela
 	private String numero; //numero de la parcela
 	private String activo; //nos avisara si la parcela esta activa o no, a los ojos del usuario
 	private String partida; //identificador de parcela a nivel localidad
 	private String dniPropietario; //dni del propietario de la parcela
 	
 	
 	
 	
 	/**
 	 * Constructor del Panel de gestin de Usuarios. En caso de que la accin sea "modificar" el panel 
 	 * se utilizar para modificar. En caso de que la accin sea "alta" el panel se utilizar como altas.
 	 */
 	public ParcelasPanelNuevo(String accion,String idparcela,String alias, String provincia, String poblacion,String poligono,String numero, String partida, String dnipropietario){
 		super();
 		this.accion = accion;
 		this.idparcela = idparcela;
 		this.alias = alias;
 		this.provincia = provincia;
 		this.poblacion = poblacion;
 		this.poligono = poligono;
 		this.numero = numero;
 		this.partida = partida;
 		this.dniPropietario = dnipropietario;
 		initialize();
 	}
 	
 	public ParcelasPanelNuevo(String accion){
 		super();
 		this.accion = accion;
 		initialize();
 		try {
 			String snt = "SELECT MAX(idparcela) FROM `parcela`"; //$NON-NLS-1$
 			dba.acceder();
 			ResultSet rs2 = dba.consulta(snt);
 			while (rs2.next()) {
 				txtIdParcelap.setText((rs2.getInt(1) + 1) + ""); //$NON-NLS-1$
 			}
 			txtIdParcelap.setEnabled(false);
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 	}
 
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		lblPoblacionp = new JLabel();
 		lblPoblacionp.setBounds(new Rectangle(506, 83, 138, 30));
 		lblPoblacionp.setText(Idioma.getString("etCityNum")); //$NON-NLS-1$
 
 		lblDnip = new JLabel();
 		lblDnip.setBounds(new Rectangle(44, 192, 88, 30));
 		lblDnip.setText(Idioma.getString("etOwnerID")); //$NON-NLS-1$
 		lblNumerop = new JLabel();
 		lblNumerop.setBounds(new Rectangle(506, 136, 88, 30));
 		lblNumerop.setText(Idioma.getString("etLotNum")); //$NON-NLS-1$
 		lblPartidap = new JLabel();
 		lblPartidap.setBounds(new Rectangle(274, 137, 88, 30));
 		lblPartidap.setText(Idioma.getString("etEntry")); //$NON-NLS-1$
 		lblPoligonop = new JLabel();
 		lblPoligonop.setBounds(new Rectangle(42, 137, 88, 30));
 		lblPoligonop.setText(Idioma.getString("etAreaNum")); //$NON-NLS-1$
 		lblIdParcela = new JLabel();
 		lblIdParcela.setBounds(new Rectangle(42, 31, 88, 30));
 		lblIdParcela.setText(Idioma.getString("etLotID")); //$NON-NLS-1$
 		lblProvinciap = new JLabel();
 		lblProvinciap.setBounds(new Rectangle(274, 84, 88, 30));
 		lblProvinciap.setText(Idioma.getString("etProvinceNum")); //$NON-NLS-1$
 		lblAliasp = new JLabel();
 		lblAliasp.setBounds(new Rectangle(42, 84, 88, 30));
 		lblAliasp.setText(Idioma.getString("etAlias")); //$NON-NLS-1$
 		
 		lblCamposObligatorios = new JLabel();
 		lblCamposObligatorios.setBounds(new Rectangle(434, 334, 238, 25));
 		lblCamposObligatorios.setFont(new Font(
 		Idioma.getString("Dialog"), Font.ITALIC, 12)); //$NON-NLS-1$
 		lblCamposObligatorios.setText(Idioma.getString("etAllFields")); //$NON-NLS-1$
 		this.setSize(782, 388);
 		this.setLayout(null);
 		this.add(lblAliasp, null);
 		this.add(lblProvinciap, null);
 		this.add(lblIdParcela, null);
 		this.add(lblPoligonop, null);
 		this.add(lblPartidap, null);
 		this.add(lblNumerop, null);
 		this.add(lblDnip, null);
 		this.add(lblCamposObligatorios,null);
 
 		this.add(lblPoblacionp, null);
 		this.add(getBGuardar(), null);
 		this.add(getBRestablecer(), null);
 		this.add(getTxtIdParcela(), null);
 		this.add(getTxtAliasp(), null);
 		this.add(getTxtProvinciap(), null);
 		this.add(getTextPoblacionp(), null);
 		this.add(getTxtPoligono(), null);
 		this.add(getTxtPartidap(), null);
 		this.add(getTxtNumerop(),null);
 		this.add(getTxtDniPropietario(), null);
 
 	}
 
 	/**
 	 * This method initializes bGuardar	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getBGuardar() {
 		if (bGuardar == null) {
 			bGuardar = new JButton();
 			bGuardar.setBounds(new Rectangle(46, 314, 53, 45));
 			bGuardar.setIcon(new ImageIcon(getClass().getResource("/recursosVisuales/Guardar.png"))); //$NON-NLS-1$
 			bGuardar.setToolTipText(Idioma.getString("etSaveNewUser")); //$NON-NLS-1$
 			bGuardar.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					ConectarDBA dba=null;
 					
 					if(accion.equals("alta")){  //$NON-NLS-1$
 						
 						Parcela p1=new Parcela(0,txtAliasp
 								.getText(), txtProvinciap.getText(),txtPoblacion.getText(),
 								txtPoligonop.getText(),txtNumerop.getText(),1,txtPartidap.getText(),txtDniPropietario.getText());
 						
 						if(Parcela.isValido()==true && encontrado==true){
 							try {
 								p1.altaParcela();
 								
 								
 								
 								ConectarDBA.acceder();
 								
 								String sql = "SELECT MAX(idparcela) FROM `parcela`";
 								
 								ResultSet resul = ConectarDBA.consulta(sql);
 								
 								resul.next();
 								
 								int masId = resul.getInt(1) + 1;
 								
 								ConectarDBA.cerrarCon();
 								
 								
 								txtIdParcelap.setText(masId+"");
 								txtAliasp.setText(""); //$NON-NLS-1$
 								txtProvinciap.setText(""); //$NON-NLS-1$
 								txtPoblacion.setText(""); //$NON-NLS-1$
 								txtPoligonop.setText(""); //$NON-NLS-1$
 								txtPartidap.setText(""); //$NON-NLS-1$
 								txtNumerop.setText(""); //$NON-NLS-1$
 								txtDniPropietario.setText(""); //$NON-NLS-1$
 								
 							} catch (SQLException e1) {
 								JOptionPane.showMessageDialog(null, Idioma.getString("msgErrorInsert")); //$NON-NLS-1$
 								 //TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 											
 	
 						}
 						
 						
 						
 					}else{
 						
 						
 						// Aqu entraremos cuando la accin declarada sea modificar
 
 		        		Parcela p1 = new Parcela(Integer.parseInt(txtIdParcelap.getText()),txtAliasp.getText(),txtProvinciap.getText(),txtPoblacion.getText(),
 								txtPoligonop.getText(),txtNumerop.getText(),1,txtPartidap.getText(),txtDniPropietario.getText());
 		        		
 						int x=p1.getIdParcela();
 						if(Parcela.isValido()==true && encontrado==true){
 							try {
 
 								String sentencia = "UPDATE `dai2opengis`.`parcela` SET  `alias` = '"+p1.getAlias()+"', `provincia`= '"+p1.getProvincia()+"', `poblacion`= '"+p1.getPoblacion()+"', `poligono`= '"+p1.getPoligono()+"', `numero`= '"+p1.getNumero()+"', `partida`= '"+p1.getPartida()+"',`dni_propietario`= '"+p1.getDniPropietario()+"'  WHERE `idparcela` = "+x; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
 								
 								dba.modificar(sentencia);
 								
 								JOptionPane.showMessageDialog(null, Idioma.getString("msgLotModified")); //$NON-NLS-1$
 							} catch (SQLException e1) {
 								
 								JOptionPane.showMessageDialog(null, Idioma.getString("msgErrorLotModify")); //$NON-NLS-1$
 								 //TODO Auto-generated catch block
 								//e1.printStackTrace();
 							}
 						}
 					}
 				}
 			});
 		}
 		return bGuardar;
 	}
 
 	/**
 	 * This method initializes bRestablecer	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getBRestablecer() {
 		if (bRestablecer == null) {
 			bRestablecer = new JButton();
 			bRestablecer.setBounds(new Rectangle(122, 314, 53, 45));
 			bRestablecer.setIcon(new ImageIcon(getClass().getResource("/recursosVisuales/Limpiar.png"))); //$NON-NLS-1$
 			bRestablecer.setToolTipText(Idioma.getString("etCleanFields")); //$NON-NLS-1$
 			bRestablecer.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					
 					
 					txtAliasp.setText(""); //$NON-NLS-1$
 					txtProvinciap.setText(""); //$NON-NLS-1$
 					txtPoblacion.setText(""); //$NON-NLS-1$
 					txtPoligonop.setText(""); //$NON-NLS-1$
 					txtPartidap.setText(""); //$NON-NLS-1$
					txtNumerop.setText("");
					
 					
 					
 				}
 			});
 		}
 		return bRestablecer;
 	}
 
 	/**
 	 * This method initializes txtIdParcela
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtIdParcela() {
 		if (txtIdParcelap == null) {
 			txtIdParcelap = new JTextField(idparcela);
 			txtIdParcelap.setBounds(new Rectangle(123, 33, 143, 27));
 			
 			if(accion=="modificar"){ //$NON-NLS-1$
 				
 				txtIdParcelap.setEnabled(false);
 			}
 			
 		}
 		return txtIdParcelap;
 	}
 
 	/**
 	 * This method initializes txtAlias	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtAliasp() {
 		if (txtAliasp == null) {
 			txtAliasp = new JTextField(alias);
 			txtAliasp.setBounds(new Rectangle(123, 86, 143, 27));
 		}
 		return txtAliasp;
 	}
 
 	/**
 	 * This method initializes txtProvincia	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtProvinciap() {
 		if (txtProvinciap == null) {
 			txtProvinciap = new JTextField(provincia);
 			txtProvinciap.setBounds(new Rectangle(358, 86, 115, 27));
 		}
 		return txtProvinciap;
 	}
 
 	/**
 	 * This method initializes txtPoblacion	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTextPoblacionp() {
 		if (txtPoblacion == null) {
 			txtPoblacion = new JTextField(poblacion);
 			txtPoblacion.setBounds(new Rectangle(634, 86, 116, 27));
 		}
 		return txtPoblacion;
 	}
 
 	/**
 	 * This method initializes txtPoligono	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtPoligono() {
 		if (txtPoligonop == null) {
 			txtPoligonop = new JTextField(poligono);
 			txtPoligonop.setBounds(new Rectangle(123, 139, 143, 27));
 		}
 		return txtPoligonop;
 	}
 
 	/**
 	 * This method initializes txtPartida	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtPartidap() {
 		if (txtPartidap == null) {
 			txtPartidap = new JTextField(partida);
 			txtPartidap.setBounds(new Rectangle(358, 139, 115, 27));
 		}
 		return txtPartidap;
 	}
 
 	/**
 	 * This method initializes txtDniPropietario	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtDniPropietario() {
 		if (txtDniPropietario == null) {
 			txtDniPropietario = new JTextField(dniPropietario);
 			txtDniPropietario.setBounds(new Rectangle(123, 194, 143, 27));
 		}
 		if(accion=="modificar"){ //$NON-NLS-1$
 			
 			txtDniPropietario.setText(dniPropietario);
 			
 		}
 		
 		txtDniPropietario.addFocusListener(new java.awt.event.FocusAdapter() {
 			public void focusLost(java.awt.event.FocusEvent e) {
 				encontrado=false;
 				
 				ConectarDBA.acceder();
 				
 				String consulta = "SELECT dni from usuario where dni = '"+ txtDniPropietario.getText() +"'"; //$NON-NLS-1$ //$NON-NLS-2$
 				
 				try {
 					ResultSet rs = ConectarDBA.consulta(consulta);
 					
 					
 					while(rs.next()){
 						encontrado = true;
 						
 					}
 					
 
 					
 					if(encontrado == false){
 						
 						JOptionPane.showMessageDialog(null,"El DNI no corresponde a ningn usuario"); //$NON-NLS-1$
 						txtDniPropietario.setText(""); //$NON-NLS-1$
 						
 					}
 					
 					
 				} catch (SQLException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		
 		
 	
 	return txtDniPropietario;
 }
 
 	
 	
 
 
 	/**
 	 * This method initializes txtNumero	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getTxtNumerop() {
 		if (txtNumerop == null) {
 			txtNumerop = new JTextField(numero);
 			txtNumerop.setBounds(new Rectangle(634, 142, 116, 27));
 		}
 		return txtNumerop;
 	}
 
 
 }  //  @jve:decl-index=0:visual-constraint="26,16"
