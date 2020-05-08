 package instanciadores;
 
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 
 import naves.SeccionDeNave;
 import naves.Sentido;
 
 public class InstanciadorImagenes {
 	//Instancia las imagenes segun el tipo
 	
 	static public Image nave(SeccionDeNave seccion){
 		//Se fija que el tipo de seccion, en donde esta ubicada(proa, popa, etc), el sentido y el estado
 		//Va formando el nombre de la imagen segun su estado
 		String tipo;
 		String estado;
 		String posicion = "BORRAME";
 		String sentido;
 		Sentido sentidoPreProcesado;
 		
 		String rutaImagen;
 		
 		
 		//Obtengo el tipo:
 		tipo = seccion.obtenerTipo();
 		
 		//Obtengo la ubicacion de la seccion
 			//TODO: implementar en tablero y otros.
 		
 		//obtengo el sentido, solo me importa si esta en dianogan o vertical/horizontal.
 		sentidoPreProcesado = seccion.sentido();
 		if (sentidoPreProcesado == Sentido.ESTE || sentidoPreProcesado == Sentido.OESTE){
 			sentido = "horizontal";
 		}
		else if (sentidoPreProcesado == Sentido.NORESTE || sentidoPreProcesado == Sentido.SUDESTE){
			sentido = "diagonalEste";
 		}
		else if (sentidoPreProcesado == Sentido.NOROESTE || sentidoPreProcesado == Sentido.SUDOESTE){
			sentido = "diagonalOeste";
 		}
 		else{
 			// norte o sur
 			sentido = "vertical";
 		}
 		sentido = seccion.sentido().toString();
 		
 		//obtengo el estado:
 		estado = seccion.estado().toString();
 		
 		rutaImagen = "estaticos/"+tipo+"_"+estado+"_"+sentido+"_"+posicion;
 		//instancio la imagen
 		
 		return  new ImageIcon(rutaImagen).getImage();		
 	}
 //	
 //	static public Image municion(Municion municion){
 //		
 //	}
 
 }
