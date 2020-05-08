 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 
 import javax.swing.WindowConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class Proyecto extends javax.swing.JFrame {
 	private JLabel lblID;
 	private JButton btnAdminEvento;
 	private JTextField txtJefe;
 	private JLabel jLabel1;
 	private JButton btnAdminGrupo;
 	private JList lstProyectos;
 	private JComboBox cbxEstado;
 	private JButton btnAdminTarea;
 	private JButton btnEliminar;
 	private JLabel lblProyectos;
 	private JButton btnOk;
 	private JButton btnCancel;
 	private JComboBox cbxFinAo;
 	private JComboBox cbxFinMes;
 	private JComboBox cbxFinDia;
 	private JEditorPane edpDescripcion;
 	private JComboBox cbxInicioAo;
 	private JComboBox cbxInicioMes;
 	private JComboBox cbxInicioDia;
 	private JLabel lblEstado;
 	private JLabel lblFechaFin;
 	private JLabel lblFechaDeInicio;
 	private JLabel lblDescripcion;
 	private JTextField txtNombre;
 	private JLabel lblNombre;
 	private JTextField txtID;
 	private Conexion conexionDB;
 	private Main frmPrincipal;
 
 	/**
 	* Auto-generated main method to display this JFrame
 	*/
 	public static void main(String[] args) {/*
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Proyecto inst = new Proyecto(null, null);
 				inst.setLocationRelativeTo(null);
 				inst.setVisible(true);
 			}
 		});*/
 		Main.main(null);
 	}
 	
 	
 	public Proyecto(Main parent, Conexion dbConnection) {
 		super();
 		initGUI();
 		
 		conexionDB = dbConnection;
 		frmPrincipal = parent;
 		populateList();
 	}
 	
 	public Main getMain(){
 		return frmPrincipal;
 	}
 	private void initGUI() {
 		try {
 setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			this.setTitle("Project Manager: Proyecto");
 			getContentPane().setLayout(null);
 			this.setPreferredSize(new java.awt.Dimension(674, 304));
 			this.addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent evt) {
 					thisWindowClosing(evt);
 				}
 			});
 			{
 				lblID = new JLabel();
 				getContentPane().add(getCbxEstado());
 				getContentPane().add(lblID);
 				lblID.setText("ID");
 				lblID.setBounds(18, 15, 34, 14);
 				lblID.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				txtID = new JTextField();
 				getContentPane().add(txtID);
 				txtID.setBounds(98, 12, 132, 21);
 				txtID.setFont(new java.awt.Font("Arial",0,10));
 				txtID.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 				txtID.setEditable(false);
 			}
 			{
 				lblNombre = new JLabel();
 				getContentPane().add(lblNombre);
 				lblNombre.setText("Nombre");
 				lblNombre.setBounds(18, 74, 46, 14);
 				lblNombre.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				txtNombre = new JTextField();
 				getContentPane().add(txtNombre);
 				txtNombre.setBounds(98, 71, 132, 21);
 				txtNombre.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			}
 			{
 				lblDescripcion = new JLabel();
 				getContentPane().add(lblDescripcion);
 				lblDescripcion.setText("Descripcin");
 				lblDescripcion.setBounds(18, 100, 54, 14);
 				lblDescripcion.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				edpDescripcion = new JEditorPane();
 				getContentPane().add(edpDescripcion);
 				edpDescripcion.setBounds(98, 98, 186, 85);
 				edpDescripcion.setFont(new java.awt.Font("Tahoma",0,10));
 				edpDescripcion.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			}
 			{
 				lblFechaDeInicio = new JLabel();
 				getContentPane().add(lblFechaDeInicio);
 				lblFechaDeInicio.setText("Fecha de Inicio");
 				lblFechaDeInicio.setBounds(18, 192, 72, 14);
 				lblFechaDeInicio.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				lblFechaFin = new JLabel();
 				getContentPane().add(lblFechaFin);
 				lblFechaFin.setText("Fecha de Fin");
 				lblFechaFin.setBounds(18, 218, 61, 14);
 				lblFechaFin.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				lblEstado = new JLabel();
 				getContentPane().add(lblEstado);
 				lblEstado.setText("Estado");
 				lblEstado.setBounds(18, 244, 43, 14);
 				lblEstado.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				ComboBoxModel cbxInicioDiaModel = 
 					new DefaultComboBoxModel();
 				cbxInicioDia = new JComboBox();
 				getContentPane().add(cbxInicioDia);
 				cbxInicioDia.setModel(cbxInicioDiaModel);
 				cbxInicioDia.setBounds(96, 189, 49, 21);
 				cbxInicioDia.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				ComboBoxModel cbxInicioMesModel = 
 					new DefaultComboBoxModel(
 							new String[] { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" });
 				cbxInicioMes = new JComboBox();
 				getContentPane().add(cbxInicioMes);
 				cbxInicioMes.setModel(cbxInicioMesModel);
 				cbxInicioMes.setBounds(157, 189, 61, 21);
 				cbxInicioMes.setFont(new java.awt.Font("Tahoma",0,10));
 				cbxInicioMes.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						cbxInicioMesActionPerformed(evt);
 					}
 				});
 				cbxInicioMes.setSelectedIndex(0);
 			}
 			{
 				String[] anios = new String[90];
 				for(int i=0;i<90;i++){
 					int aux = i+2010;
 					anios[i] = ""+ aux;
 				}
 				ComboBoxModel cbxInicioAoModel = 
 					new DefaultComboBoxModel(anios);
 				cbxInicioAo = new JComboBox();
 				getContentPane().add(cbxInicioAo);
 				cbxInicioAo.setModel(cbxInicioAoModel);
 				cbxInicioAo.setBounds(230, 189, 54, 21);
 				cbxInicioAo.setFont(new java.awt.Font("Tahoma",0,10));
 				cbxInicioAo.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						cbxInicioAoActionPerformed(evt);
 					}
 				});
 			}
 			{
 				ComboBoxModel jComboBox2Model = 
 					new DefaultComboBoxModel();
 				cbxFinDia = new JComboBox();
 				getContentPane().add(cbxFinDia);
 				cbxFinDia.setModel(jComboBox2Model);
 				cbxFinDia.setBounds(96, 215, 49, 21);
 				cbxFinDia.setFont(new java.awt.Font("Tahoma",0,10));
 			}
 			{
 				ComboBoxModel jComboBox3Model = 
 					new DefaultComboBoxModel(
 							new String[] { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" });
 				cbxFinMes = new JComboBox();
 				getContentPane().add(cbxFinMes);
 				cbxFinMes.setModel(jComboBox3Model);
 				cbxFinMes.setBounds(157, 213, 61, 21);
 				cbxFinMes.setFont(new java.awt.Font("Tahoma",0,10));
 				cbxFinMes.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						cbxFinMesActionPerformed(evt);
 					}
 				});
 				cbxFinMes.setSelectedIndex(0);
 			}
 			{
 				String[] anios = new String[90];
 				for(int i=0;i<90;i++){
 					int aux = i+2010;
 					anios[i] = ""+ aux;
 				}
 				ComboBoxModel jComboBox4Model = 
 					new DefaultComboBoxModel(anios);
 				cbxFinAo = new JComboBox();
 				getContentPane().add(cbxFinAo);
 				cbxFinAo.setModel(jComboBox4Model);
 				cbxFinAo.setBounds(230, 215, 54, 21);
 				cbxFinAo.setFont(new java.awt.Font("Tahoma",0,10));
 				cbxFinAo.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						cbxFinAoActionPerformed(evt);
 					}
 				});
 			}
 			{
 				btnCancel = new JButton();
 				getContentPane().add(btnCancel);
 				btnCancel.setText("Cancelar");
 				btnCancel.setBounds(562, 241, 92, 21);
 				btnCancel.setFont(new java.awt.Font("Tahoma",0,10));
 				btnCancel.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						btnCancelActionPerformed(evt);
 					}
 				});
 			}
 			{
 				btnOk = new JButton();
 				getContentPane().add(btnOk);
 				btnOk.setText("Guardar Cambios");
 				btnOk.setBounds(426, 241, 124, 21);
 				btnOk.setFont(new java.awt.Font("Tahoma",0,10));
 				btnOk.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						btnOkActionPerformed(evt);
 					}
 				});
 			}
 			{
 				lblProyectos = new JLabel();
 				getContentPane().add(lblProyectos);
 				lblProyectos.setText("Proyectos");
 				lblProyectos.setBounds(296, 15, 122, 14);
 				lblProyectos.setFont(new java.awt.Font("Arial",0,10));
 			}
 			{
 				btnEliminar = new JButton();
 				getContentPane().add(btnEliminar);
 				btnEliminar.setText("Eliminar Proyecto");
 				btnEliminar.setFont(new java.awt.Font("Tahoma",0,10));
 				btnEliminar.setBounds(296, 241, 115, 21);
 				btnEliminar.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						btnEliminarActionPerformed(evt);
 					}
 				});
 			}
 			{
 				btnAdminEvento = new JButton();
 				getContentPane().add(btnAdminEvento);
 				btnAdminEvento.setText("Administrar Evento");
 				btnAdminEvento.setFont(new java.awt.Font("Tahoma",0,10));
 				btnAdminEvento.setBounds(500, 38, 154, 21);
 				btnAdminEvento.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent evt) {
 						btnAdminEventoActionPerformed(evt);
 					}
 				});
 			}
 			{
 				ListModel lstProyectosModel = new DefaultComboBoxModel();
 				lstProyectos = new JList();
 				lstProyectos.setModel(lstProyectosModel);
 				lstProyectos.setBounds(295, 37, 192, 189);
 				getContentPane().add(lstProyectos);
 				lstProyectos.setFont(new java.awt.Font("Tahoma",0,10));
 				lstProyectos.addListSelectionListener(new ListSelectionListener() {
 					public void valueChanged(ListSelectionEvent evt) {
 						lstProyectosValueChanged(evt);
 					}
 				});
 			}
 			{
 				btnAdminTarea = new JButton();
 				getContentPane().add(btnAdminTarea);
 				getContentPane().add(getBtnAdminGrupo());
 				getContentPane().add(getJLabel1());
 				getContentPane().add(getJTextField1());
 				btnAdminTarea.setText("Administrar Tarea");
 				btnAdminTarea.setFont(new java.awt.Font("Tahoma",0,10));
 				btnAdminTarea.setBounds(499, 64, 154, 21);
 			}
 			{
 				
 			}
 			pack();
 			this.setSize(674, 304);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void populateList(){
 		try {
 			conexionDB.conectarBD();			
 			Statement stmt = conexionDB.statement();			
 			String query = "select id_proyecto, nombre from proyectos";
 			
 			ResultSet rs = stmt.executeQuery(query);
 			
 			rs.last();
 			int cantResults = rs.getRow();
 			rs.beforeFirst();
 			int i=0;
 			DefaultComboBoxModel model = new DefaultComboBoxModel();
 			for(i=0;i<cantResults;i++){
 				rs.next();
 				model.addElement(rs.getString("id_proyecto") + "-"+ rs.getString("nombre"));
 				//System.out.println(stringArr[i]);
 			}
 			
 			model.addElement("0-CREAR NUEVO PROYECTO"); 
 			
 			stmt.close();
 			conexionDB.desconectarBD();
 			
 			lstProyectos.setModel(model);
			
 			lstProyectos.setSelectedIndex(0);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private void thisWindowClosing(WindowEvent evt) {
 		frmPrincipal.setVisible(true);
 		this.dispose();
 	}
 	
 	
 	
 	private void setDate(String yyyyMMdd, JComboBox cbxDia, JComboBox cbxMes, JComboBox cbxAnio){
 		//formato de fecha de entrada: yyyy-MM-dd
 		cbxAnio.setSelectedItem(yyyyMMdd.substring(0, 4));
 		cbxMes.setSelectedIndex(Integer.parseInt(yyyyMMdd.substring(5,7))-1);
 		cbxDia.setSelectedIndex(Integer.parseInt(yyyyMMdd.substring(8))-1);
 	}
 	
 	public boolean esJefe(String userName, String projectID) {
 		String queryJefe = "select jefe from proyectos where id_proyecto = " + projectID;
 		boolean ret = false;
 		try {
 			conexionDB.conectarBD();
 			Statement stmtJefe = conexionDB.statement();			
 			ResultSet rsJefe = stmtJefe.executeQuery(queryJefe);
 			if(rsJefe.next()) ret = userName.equals(rsJefe.getString("jefe"));
 			stmtJefe.close();
 			conexionDB.desconectarBD();
 			
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return ret;
 	}
 	
 	private void setUserControls(boolean enable, boolean editable) {
 		txtJefe.setEditable(enable && editable);
 		txtNombre.setEditable(enable && editable);
 		edpDescripcion.setEditable(enable && editable);
 		cbxInicioDia.setEnabled(enable && editable);
 		cbxInicioMes.setEnabled(enable && editable);
 		cbxInicioAo.setEnabled(enable && editable);
 		cbxFinDia.setEnabled(enable && editable);
 		cbxFinMes.setEnabled(enable && editable);
 		cbxFinAo.setEnabled(enable && editable);
 		cbxEstado.setEnabled(enable && editable);
 		btnAdminEvento.setEnabled(enable && editable);
 		btnAdminTarea.setEnabled(enable && editable);
 		btnAdminGrupo.setEnabled(enable && editable);
 		btnEliminar.setEnabled(enable && editable);
 		btnOk.setEnabled(enable && editable);
 	}
 	
 	private boolean editable(String projectID){
 		try {
 			conexionDB.conectarBD();
 			Statement stmt = conexionDB.statement();
 			
 			ResultSet rs = stmt.executeQuery("select estado from proyectos where id_proyecto = " + projectID);
 			if(rs.next()){
 				String status = rs.getString("estado");
 				System.out.println(status);
 				return ((status.equals("Activo")) || (status.equals("Pendiente")));
 			}else return false;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private void lstProyectosValueChanged(ListSelectionEvent evt) {
 		String projName;
 		String projID;
 		
 		String currentUser = frmPrincipal.getCurrentUserID();
 		
 		boolean permitido;
 		boolean editable;
 		if (lstProyectos.getSelectedIndices().length>0) {
 			
 			projName = getCurrentProjectName();
 			projID = getCurrentProjectID();
 			permitido = esJefe(currentUser, projID);
 			editable = editable(projID);
 			setUserControls(permitido, editable);
 			System.out.println("Proyecto " + projName + ", " + permitido );
 			if (projID.equals("0")){
 				//si seleccion para crear un nuevo proyecto
 				btnOk.setEnabled(true);
 				txtID.setText("");
 				cbxEstado.setSelectedItem("Pendiente");
 				txtNombre.setText("Nuevo proyecto");
 				edpDescripcion.setText("");
 				cbxEstado.setEnabled(false);
 				Date date = Calendar.getInstance().getTime();
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 				String currentDate = sdf.format(date);
 				setDate(currentDate, cbxInicioDia, cbxInicioMes, cbxInicioAo);
 				setDate(currentDate, cbxFinDia, cbxFinMes, cbxFinAo);
 			} else {
 				//seleccion un proyecto ya existente
 				try {
 					conexionDB.conectarBD();
 					Statement stmt = conexionDB.statement();
 					
 					String query = "select jefe, descripcion, fecha_inicio, fecha_fin, estado from proyectos where id_proyecto = " + projID;
 					ResultSet rs = stmt.executeQuery(query);
 					
 					if(rs.next()){
 						txtID.setText(projID);
 						txtJefe.setText(rs.getString("jefe"));
 						txtNombre.setText(projName);
 						edpDescripcion.setText(rs.getString("descripcion"));
 						cbxEstado.setSelectedItem(rs.getString("estado"));
 						setDate(rs.getString("fecha_inicio"), cbxInicioDia, cbxInicioMes, cbxInicioAo);
 						setDate(rs.getString("fecha_fin"), cbxFinDia, cbxFinMes, cbxFinAo);
 					}else{
 						System.out.println("This should NOT happen...");
 					}
 					rs.close();
 					conexionDB.desconectarBD();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public String getCurrentProjectName(){
 		String selectedProj = lstProyectos.getModel().getElementAt(lstProyectos.getSelectedIndex()).toString();
 		String projName = selectedProj.substring(selectedProj.indexOf('-')+1);
 		return projName;
 	}
 	
 	public String getCurrentProjectID(){
 		String selectedProj = lstProyectos.getModel().getElementAt(lstProyectos.getSelectedIndex()).toString();
 		String projID = selectedProj.substring(0, selectedProj.indexOf('-'));
 		return projID;
 	}
 	
 	private void btnEliminarActionPerformed(ActionEvent evt) {
 		String projID = getCurrentProjectID();
 		if (projID != "0"){
 			if ((cbxEstado.getSelectedItem().toString().equals("Cancelado")) || (cbxEstado.getSelectedItem().toString().equals("Finalizado"))){
 				try {
 					conexionDB.conectarBD();
 					Statement stmt = conexionDB.statement();
 					
 					String query = "update proyectos set estado = 'Cancelado' where id_proyecto = " + projID;
 					
 					System.out.println(query);
 					
 					stmt.executeUpdate(query);
 					cbxEstado.setSelectedItem("Cancelado");
					populateList();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}else{
 				int result = JOptionPane.showConfirmDialog(this, "El proyecto se encuentra "+cbxEstado.getSelectedItem().toString()+", est seguro de que desea cancelarlo?", "Advertencia", JOptionPane.WARNING_MESSAGE);
 				
 				if (result == JOptionPane.YES_OPTION){
 					try {
 						conexionDB.conectarBD();
 						Statement stmt = conexionDB.statement();
 						
 						String query = "update proyectos set estado = 'Cancelado' where id_proyecto = " + projID;
 						
 						System.out.println(query);
 						
 						stmt.executeUpdate(query);
 						cbxEstado.setSelectedItem("Cancelado");
						populateList();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 			}
 		}
 	}
 	
 	private void cbxInicioMesActionPerformed(ActionEvent evt) {
 		Utils.updateDias(cbxInicioDia, cbxInicioMes, cbxInicioAo);
 	}
 	
 	private void cbxInicioAoActionPerformed(ActionEvent evt) {
 		Utils.updateDias(cbxInicioDia, cbxInicioMes, cbxInicioAo);
 	}
 	
 	private void cbxFinMesActionPerformed(ActionEvent evt) {
 		Utils.updateDias(cbxFinDia, cbxFinMes, cbxFinAo);
 	}
 	
 	private void cbxFinAoActionPerformed(ActionEvent evt) {
 		Utils.updateDias(cbxFinDia, cbxFinMes, cbxFinAo);
 	}
 	
 	private boolean validarFecha(){
 		int diaInicio = cbxInicioDia.getSelectedIndex();
 		int diaFin = cbxFinDia.getSelectedIndex();
 		int mesInicio = cbxInicioMes.getSelectedIndex();
 		int mesFin = cbxFinMes.getSelectedIndex();
 		int anioInicio = cbxInicioAo.getSelectedIndex();
 		int anioFin = cbxFinAo.getSelectedIndex();
 		
 		if (anioInicio < anioFin)
 			return true;			
 		else
 			if ((anioInicio == anioFin) && (mesInicio < mesFin))
 				return true;
 			else
 				if ((anioInicio == anioFin) && (mesInicio == mesFin) && (diaInicio < diaFin))
 					return true;
 				else return false;
 	}
 	
 	private void btnOkActionPerformed(ActionEvent evt) {
 		if (validarFecha()){
 			if (getCurrentProjectID().equals("0")){
 				//Caso de uso CREAR PROYECTO
 				try {					
 					Date date = Calendar.getInstance().getTime();
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 					String currentDate = sdf.format(date);
 					if (currentDate.compareTo(Utils.makeDate(cbxInicioDia,cbxInicioMes,cbxInicioAo)) <= 0){
 						int opt = JOptionPane.showConfirmDialog(this, "Est seguro de que desea crear un proyecto nuevo?", "Project Manager - Crear Proyecto", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 						if (opt == JOptionPane.YES_OPTION){
 							String userID = frmPrincipal.getCurrentUserID();
 							conexionDB.conectarBD();
 							Statement stmt = conexionDB.statement();
 							String query = "insert into proyectos (jefe, nombre, descripcion, fecha_inicio, fecha_fin, estado) values ("+
 								userID + ",'" + 
 								txtNombre.getText() + "','" +
 								edpDescripcion.getText() + "','" +
 								Utils.makeDate(cbxInicioDia, cbxInicioMes, cbxInicioAo) + "','" +
 								Utils.makeDate(cbxFinDia, cbxFinMes, cbxFinAo) + "','Pendiente')";
 							System.out.println(query);
 							stmt.executeUpdate(query);
 							stmt.close();
 							conexionDB.desconectarBD();
 							populateList();
 						}
 					}else{
 						JOptionPane.showMessageDialog(this, "La fecha no puede ser anterior a la actual.", "Error: fecha invlida", JOptionPane.ERROR_MESSAGE);
 					}
 					
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				
 			}else{
 				//estamos modificando un proyecto existente
 				try {
 					int opt = JOptionPane.showConfirmDialog(this, "Est seguro de que desea modificar el proyecto?", "Project Manager - Crear Proyecto", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (opt == JOptionPane.YES_OPTION){
 						conexionDB.conectarBD();
 						Statement stmt = conexionDB.statement();
 						String query = "update proyectos set "+ 
 							"nombre = '" + txtNombre.getText() + "', " +
 							"descripcion = '" + edpDescripcion.getText() + "', " +
 							"fecha_inicio = '" + Utils.makeDate(cbxInicioDia, cbxInicioMes, cbxInicioAo) + "', " +
 							"fecha_fin = '" + Utils.makeDate(cbxFinDia, cbxFinMes, cbxFinAo) + "', " + 
 							"estado = '" + cbxEstado.getSelectedItem().toString() +
 							"' where id_proyecto = " + getCurrentProjectID();
 						System.out.println(query);
 						stmt.execute(query);
 						stmt.close();
 						conexionDB.desconectarBD();
 						populateList();
 					}
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}else{
 			JOptionPane.showMessageDialog(this, "La fecha de finalizacin debe ser posterior a la de comienzo.", "Error: fecha invlida", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	private JComboBox getCbxEstado() {
 		if(cbxEstado == null) {
 			ComboBoxModel cbxEstadoModel = 
 				new DefaultComboBoxModel(
 						new String[] { "Pendiente", "Activo", "Finalizado", "Cancelado" });
 			cbxEstado = new JComboBox();
 			cbxEstado.setModel(cbxEstadoModel);
 			cbxEstado.setBounds(96, 241, 188, 21);
 		}
 		return cbxEstado;
 	}
 	
 	private void btnCancelActionPerformed(ActionEvent evt) {
 		System.out.println("btnCancel.actionPerformed, event="+evt);
 		frmPrincipal.setVisible(true);
 		this.dispose();
 	}
 	
 	private void btnAdminEventoActionPerformed(ActionEvent evt) {
 		//System.out.println("btnAdminEvento.actionPerformed, event="+evt);
 		String id = getCurrentProjectID();
 		if (!id.equals("0")){
 			this.setVisible(false);
 			
 			Evento evento = new Evento(this,conexionDB,getCurrentProjectName(),id);
 			evento.setVisible(true);
 		}
 		else {
 			JOptionPane.showMessageDialog(this, "No ha seleccionado ningn proyecto.", 
 											"Cuidado!", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 	
 	private JButton getBtnAdminGrupo() {
 		if(btnAdminGrupo == null) {
 			btnAdminGrupo = new JButton();
 			btnAdminGrupo.setText("Administrar Grupos");
 			btnAdminGrupo.setBounds(500, 90, 155, 21);
 			btnAdminGrupo.setFont(new java.awt.Font("Tahoma",0,10));
 		}
 		return btnAdminGrupo;
 	}
 	
 	private JLabel getJLabel1() {
 		if(jLabel1 == null) {
 			jLabel1 = new JLabel();
 			jLabel1.setText("Jefe");
 			jLabel1.setFont(new java.awt.Font("Tahoma",0,10));
 			jLabel1.setBounds(18, 46, 34, 14);
 		}
 		return jLabel1;
 	}
 	
 	private JTextField getJTextField1() {
 		if(txtJefe == null) {
 			txtJefe = new JTextField();
 			txtJefe.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			txtJefe.setBounds(98, 41, 132, 21);
 		}
 		return txtJefe;
 	}
 
 }
