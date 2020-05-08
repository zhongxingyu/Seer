 package casarural;
 
 import java.sql.SQLException;
 import java.util.List;
 
 
 public class GestorServicio {
 
 	private static GestorServicio gestorServicio;
 	private GestorBD gbd;
 	
 	/**Devuelve una instancia de la clase GestorServicio
 	 * @param ninguno
 	 * @return Una instancia del Gestor de Servicios
 	 */
 	public static GestorServicio getInstance() {
 		if (gestorServicio == null) {
 			gestorServicio = new GestorServicio();
 		}
 		return gestorServicio;
 	}
 		
 	private GestorServicio() {
 		gbd = GestorBD.getInstance();
 	}
 	
 	/**Obtiene una lista de las recogidas actuales
 	 * @param ninguno
 	 * @return Una lista de recogidas
 	 */
 	public List<Recogida> getRecogidas() {
 		return gbd.getRecogidas();
 	}
 	
 	/**Crea un Servicio asignandolo a un recorrido
 	 * 
 	 * @param servicio Toma como entrada la clase Servicio
 	 * @return confirmación true/false
 	 */
 	public boolean crearServicio(Servicio servicio) {
 		try {
 			return gbd.crearServicio(servicio);
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**Obtiene una lista de Servicios asignados a una casa rural
 	 * 
 	 * @param numCasa Código de la casa rural de la cual se requieren los servicios
 	 * @return listaDeServicios asociados a una casa rural
 	 */
 	public List<Servicio> mostrarServicios(int numCasa){
 		try {
 			java.util.Date fechaActual = new java.util.Date();
 			java.sql.Date fecha = new java.sql.Date(fechaActual.getTime());
 			return gbd.obtenerServicios(numCasa, fecha);
 		}catch(SQLException ex){
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**Crea una reserva y asigna un servicio a la misma
 	 *
 	 * @param 
 	 * @param idServicio Identificador del servicio que se asignara a la reserva
 	 * @param numPlazas Numero de plazas que se quiere reservar
 	 * @return
 	 */
	public boolean transaccionDeReserva(List<Reserva> reservasTotales, int idReserva, String numTfno, int idServicio, int numPlazas){
 		try {
			float precioTotal = 0;
			for(Reserva reserva: reservasTotales){
				precioTotal = precioTotal + reserva.getPrecioTotal();
			}
 			gbd.transaccionDeReserva(reservasTotales, idReserva, numTfno, precioTotal, idServicio, numPlazas);
 			return true;
 		}catch(SQLException ex){
 			ex.printStackTrace();
 			return false;
 		}
 	}
 }
