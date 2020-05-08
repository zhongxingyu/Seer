 package gui;
 
 import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import facturacion.Operador;
 
 import terminal.Datos;
 
 public class MenuSuperior extends JFrame implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private Operador op;
 	private JMenuBar mb;
     private JMenu menu1;
     private JMenuItem mi1,mi2,mi3;
     private JMenu menu2;
     private JMenuItem mi1_2;
     private JMenu menu3;
     private JMenuItem mi1_3, mi2_2;
     
     public MenuSuperior(Operador op, int ancho) {
         this.op = op;
         mb=new JMenuBar();
         setJMenuBar(mb);
         //ARCHIVO
         menu1=new JMenu("Archivo");
         mb.add(menu1);
         mi1=new JMenuItem("Cargar últimos datos");
         mi1.addActionListener(this);
         menu1.add(mi1);
         mi2=new JMenuItem("Almacenar datos actuales");
         mi2.addActionListener(this);
         menu1.add(mi2);
         mi3=new JMenuItem("Salir");
         mi3.addActionListener(this);
         menu1.add(mi3);
         //EDICIÓN
         menu2=new JMenu("Clientes");
         mb.add(menu2);
         mi1_2=new JMenuItem("Añadir cliente");
         mi1_2.addActionListener(this);
         menu2.add(mi1_2);
         mi2_2=new JMenuItem("Eliminar cliente");
         mi2_2.addActionListener(this);
         menu2.add(mi2_2);
         //AYUDA
         menu3=new JMenu("Ayuda");
         mb.add(menu3);
         mi1_3=new JMenuItem("Acerca de");
         mi1_3.addActionListener(this);
         menu3.add(mi1_3);
         mb.setSize(ancho, 50);
         mb.setVisible(true);
     }
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
     	Datos dato = new Datos();
         if (e.getSource()==mi1) {
         	op = dato.recuperarDatos();
         }
         if (e.getSource()==mi2) {
     		dato.almacenarDatos(op);
         }
         if (e.getSource()==mi3) {
         	System.exit(0); 
         } 
         if (e.getSource()==mi1_2) {
         	JFrame ventana = new JFrame("Añadir cliente");
        	
        	//ventana.setLayout(new FlowLayout());
        	
         	String html = "<html>" +
                     "<b>Paso 1: </b><br/>" +
                     " <i>Seleccionar tipo de cliente.</i><br/>" +
                     "</html>";
         	JLabel etiqueta = new JLabel(html);
         	ventana.getContentPane().add(etiqueta, BorderLayout.NORTH);
         	ventana.setAlwaysOnTop(true);
         	
         	
         	JButton boton_particular = new JButton("Particular");
 
         	//boton_particular.addActionListener(new EscuchadoraBoton());//Registro escuchador
 
         	ventana.getContentPane().add(boton_particular, BorderLayout.EAST);
         	
         	JButton boton_empresa = new JButton("Empresa");
 
         	//boton_empresa.addActionListener(new EscuchadoraBoton());//Registro escuchador
 
         	ventana.getContentPane().add(boton_empresa, BorderLayout.WEST);
         	
         	ventana.setSize(500, 500);
         	ventana.setResizable(false);
         	ventana.setLocationRelativeTo(null);
         	ventana.pack();
         	ventana.setVisible(true);
 
         } 
         if (e.getSource()==mi2_2) {
         	System.out.println("Eliminar cliente.");
         } 
         if (e.getSource()==mi1_3) {
         	JOptionPane.showMessageDialog(this, "© Ángel Carlos del Pozo Muela, 2013.", "Acerca de", JOptionPane.WARNING_MESSAGE);
         } 
 	}
 
 	public JMenuBar getMenu(){
 		return this.mb;
 	}
 }
