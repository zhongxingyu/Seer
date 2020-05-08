 package controlador;
 
 import instanciadores.InstanciadorMuniciones;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 
 import tablero.TableroComunicable;
 
 import municiones.Municion;
 
 import juego.BatallaNavalgo;
 
 public class Controlador {
 
 	private BatallaNavalgo modelo;
 	private String municionSeleccionada = "Disparo Convencional" ; //seteado por defecto
 	
 	
 	public Controlador(BatallaNavalgo batalla)
 	{
		this.modelo = modelo;
 		
 	}
 	
 	
 	
 	public class ListenerBotonesTablero implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{	
 			JButton boton = (JButton) e.getSource();
 			int [] id = (int[]) boton.getClientProperty("id");
 			
 			Municion municion = InstanciadorMuniciones.instanciar(municionSeleccionada);
 			
 			//Pasarle al tablero el id del casillero y la municionSeleccionada
			modelo.obtenerTablero().ponerMuncion(municion, id);
 		}
 	}
 	
 	public ActionListener obtenerListenerBotonesTablero(){
 		return new ListenerBotonesTablero();
 	}
 	
 	
 	
 	public class ListenerListadoMuniciones implements MouseListener {
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			JList<String> listaMuniciones = (JList<String>) e.getSource();
 
 			municionSeleccionada = (String) listaMuniciones.getSelectedValue();
 			
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 	
 	public MouseListener obtenerListenerListadoMuniciones(){
 		return new ListenerListadoMuniciones();
 	}
 }
