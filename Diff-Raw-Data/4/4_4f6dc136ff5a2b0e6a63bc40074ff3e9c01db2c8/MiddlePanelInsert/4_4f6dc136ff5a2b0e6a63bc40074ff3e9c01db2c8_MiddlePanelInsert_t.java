 package ar.proyecto.gui;
 
 import java.awt.FlowLayout;
 import java.text.NumberFormat;
 import java.text.ParseException;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.text.MaskFormatter;
 
 import ar.proyecto.controller.ActionInsert;
 
 public class MiddlePanelInsert extends MiddlePanel {
 	
 	//Crear la ventana
 	private JLabel into;
 	
 	private JLabel nroPatente;
 	private JFormattedTextField txtNroPatente;
 	private JLabel typo;
 	private JTextField txtTypo;	
 	private JLabel modelo;
 	private JTextField txtModelo;	
 	private JLabel ano;
 	private JFormattedTextField txtAno;	
 
 	@SuppressWarnings("rawtypes")
 	private JComboBox cboxMarca;
 	
 	private JButton ok;
 		
 	//constructor
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public MiddlePanelInsert(MainWindow gui) {
 		super(gui);
 		//Inicializar los Labeles
 		into = new JLabel("INTO Vehiculo :");
 		nroPatente = new JLabel("nro_patente =");
 		typo = new JLabel("typo =");
 		modelo = new JLabel("modelo =");
 		ano = new JLabel("anos =");
 		
 		//Inicializar los textFields
 		try {
 			txtNroPatente = new JFormattedTextField(new MaskFormatter("******"));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			txtNroPatente = new JFormattedTextField();
 		}
 		txtNroPatente.setColumns(5);
 		txtTypo = new JTextField();
 		txtTypo.setColumns(4);
 		txtModelo = new JTextField();
 		txtModelo.setColumns(10);
		NumberFormat AnoFormatter = NumberFormat.getIntegerInstance();
		AnoFormatter.setGroupingUsed(false);
		txtAno = new JFormattedTextField(AnoFormatter);
 		txtAno.setColumns(4);
 		
 		//Inicializar el comboBox
 		String[] marcas = {"FIAT","FORD","RENAULT"};
 		cboxMarca = new JComboBox(marcas);
 		cboxMarca.setSelectedItem(2);
 		
 		//Inicializar el Button
 		ok = new JButton(new ActionInsert(this,"OK"));
 		
 		//Agregar todo al panel
 		this.setLayout(new FlowLayout(FlowLayout.LEFT));
 		this.add(into);
 		this.add(nroPatente);
 		this.add(txtNroPatente);
 		this.add(typo);
 		this.add(txtTypo);
 		this.add(cboxMarca);
 		this.add(modelo);
 		this.add(txtModelo);
 		this.add(ano);
 		this.add(txtAno);
 		this.add(ok);
 	}
 
 	public JFormattedTextField getTxtNroPatente() {
 		return txtNroPatente;
 	}
 
 	public JTextField getTxtTypo() {
 		return txtTypo;
 	}
 	
 	public JTextField getTxtModelo() {
 		return txtModelo;
 	}
 
 	public JFormattedTextField getTxtAno() {
 		return txtAno;
 	}
 
 	public JComboBox getCboxMarca() {
 		return cboxMarca;
 	}
 
 }
