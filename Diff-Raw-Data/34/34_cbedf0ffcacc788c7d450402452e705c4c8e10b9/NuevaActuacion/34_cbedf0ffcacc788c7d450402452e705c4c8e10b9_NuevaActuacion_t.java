 package gui;
 
 import net.rim.device.api.ui.component.Dialog;
 import persistence.Persistence;
 import core.Actuacion;
 import core.Proceso;
 
 public class NuevaActuacion {
 	private Actuacion _actuacion;
 	private NuevaActuacionScreen _screen;
 	private Proceso _proceso;
 
 	/**
 	 * Se crea una NuevaActuacion sin proceso asociado, este debe ser asignado
 	 * con setProceso(); o si no se generaria excepcion en el guardado
 	 */
 	public NuevaActuacion() {
 		_screen = new NuevaActuacionScreen();
 	}
 
 	/**
 	 * @param proceso
 	 *            Se crea una NuevaActuacion con proceso asociado
 	 */
 	public NuevaActuacion(Proceso proceso) {
 		_proceso = proceso;
 		_screen = new NuevaActuacionScreen();
 	}
 
 	/**
 	 * @param proceso
 	 *            se asocia al objeto NuevaActuacion, despues de esta haber sido
 	 *            creada
 	 */
 	public void setProceso(Proceso proceso) {
 		_proceso = proceso;
 	}
 
 	/**
 	 * @return El Proceso asociado al objeto NuevaActuacion
 	 */
 	public Proceso getProceso() {
 		return _proceso;
 	}
 
 	/**
 	 * @return La pantalla asociada al objeto NuevaActuacion
 	 */
 	public NuevaActuacionScreen getScreen() {
 		return _screen;
 	}
 
 	/**
 	 * @return La nueva actuacion creada, s esta no ha sido guardada
 	 *         previamente con guardarActuacion(); se invoca dicho metodo
 	 */
 	public Actuacion getActuacion() throws Exception {
 		if (_actuacion == null) {
 			guardarActuacion();
 		}
 		return _actuacion;
 	}
 
 	public Actuacion getActuacion(boolean guardado) throws Exception {
 		if (!guardado) {
 			_actuacion = new Actuacion(_screen.getJuzgado(),
 					_screen.getFecha(), _screen.getFechaProxima(),
 					_screen.getDescripcion());
 			return _actuacion;
 		} else {
 			return getActuacion();
 		}
 	}
 
 	/**
 	 * Crea el nuevo objeto Actuacion y la guarda en la base de datos usando la
 	 * informacion capturada desde la pantalla
 	 */
 	public void guardarActuacion() throws Exception {
 		if (_screen.isGuardado()) {
 			Persistence guardado = null;
 			_actuacion = new Actuacion(_screen.getJuzgado(),
 					_screen.getFecha(), _screen.getFechaProxima(),
					_screen.getDescripcion(), null,_screen.getUid());
 			try {
 				guardado = new Persistence();
 			} catch (Exception e) {
 				Dialog.alert("New -> " + e.toString());
 			}
 			try {
 				guardado.guardarActuacion(_actuacion, _proceso.getId_proceso());
 			} catch (NullPointerException e) {
 				throw e;
 			} catch (Exception e) {
 				Dialog.alert("Guardar -> " + e.toString());
 			}
 		} else {
 			throw new Exception("No se esta guardando el elemento");
 		}
 	}
 }
