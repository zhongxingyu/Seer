 package gui;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import core.CalendarManager;
 import core.Juzgado;
 
 public class NuevaActuacionScreen extends FondoNormal {
 
 	private ButtonField _btnJuzgado;
 	private DateField _dfFecha;
 	private DateField _dfFechaProxima;
 	private BasicEditField _txtDescripcion;
 	private CheckboxField _cbCita;
 
 	private Juzgado _juzgado;
 	private String _uid;
 
 	private boolean _guardar;
 
 	/**
 	 * Crea una NuevaActuacionScreen inicializando los componentes y
 	 * agregandolos a la pantalla
 	 */
 	public NuevaActuacionScreen() {
 		setTitle("Nueva actuacin");
 
 		HorizontalFieldManager fldJuzgado = new HorizontalFieldManager();
 		VerticalFieldManager fldLeftJuzgado = new VerticalFieldManager();
 		VerticalFieldManager fldRightJuzgado = new VerticalFieldManager(
 				USE_ALL_WIDTH);
 
 		_btnJuzgado = new ButtonField("Seleccionar", FIELD_RIGHT);
 		_btnJuzgado.setChangeListener(listenerJuzgado);
 		fldLeftJuzgado.add(new LabelField("Juzgado: "));
 		fldRightJuzgado.add(_btnJuzgado);
 		fldJuzgado.add(fldLeftJuzgado);
 		fldJuzgado.add(fldRightJuzgado);
 		add(fldJuzgado);
 
 		_dfFecha = new DateField("Fecha: ", System.currentTimeMillis(),
 				DateField.DATE_TIME);
 		add(_dfFecha);
 
 		_dfFechaProxima = new DateField("Fecha prxima: ",
 				System.currentTimeMillis(), DateField.DATE_TIME);
 		add(_dfFechaProxima);
 
 		_txtDescripcion = new BasicEditField();
 		_txtDescripcion.setLabel("Descripcin: ");
 		add(_txtDescripcion);
 		
 		_cbCita = new CheckboxField("Crear cita", false);
 		_cbCita.setChangeListener(listenerCita);
 		add(_cbCita);
 
 		addMenuItem(menuGuardar);
 	}
 	
 	private FieldChangeListener listenerCita = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
 			if (_cbCita.getChecked()) {
 				NuevaCita n = new NuevaCita(getDescripcion(), new Date(
 						_dfFechaProxima.getDate()));
 				UiApplication.getUiApplication().pushModalScreen(n.getScreen());
 				n.guardarCita();
 				_uid = n.getUid();
 				if (!n.isGuardado()) {
 					_cbCita.setChecked(false);
 				}
				n = null;
 			} else {
 				try {
					CalendarManager.borrarCita(_uid);
 				} catch (Exception e) {
 					Dialog.alert(e.toString());
 				}
 			}
 		}
 	};
 
 	private FieldChangeListener listenerJuzgado = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
			ListadoJuzgados juzgados = new ListadoJuzgados();
 			UiApplication.getUiApplication().pushModalScreen(
 					juzgados.getScreen());
 			setJuzgado(juzgados.getSelected());
 		}
 	};
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			if (_txtDescripcion.getTextLength() == 0) {
 				Dialog.alert("Debe agregar una descripcin");
 			} else if (_juzgado == null) {
 				Object[] ask = { "Guardar", "Cancelar" };
 				int sel = Dialog.ask("Juzgado se considera importante", ask, 1);
 				if (sel == 0) {
 					if (_txtDescripcion.getTextLength() == 0) {
 						Dialog.alert("Debe agregar una descripcin");
 					} else {
 						_guardar = true;
 						UiApplication.getUiApplication().popScreen(getScreen());
 					}
 				}
 			} else {
 				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 			}
 		}
 	};
 
 	/**
 	 * @return el Juzgado asociado al objeto, en caso de no existir se retorna
 	 *         null
 	 */
 	public Juzgado getJuzgado() {
 		return _juzgado;
 	}
 
 	/**
 	 * @param juzgado
 	 *            Se asigna al Objeto un Juzgado
 	 */
 	public void setJuzgado(Juzgado juzgado) {
 		_btnJuzgado.setLabel(juzgado.getNombre());
 		_juzgado = juzgado;
 	}
 
 	/**
 	 * @return La fecha ingresada en la pantalla, en caso de no exixtir se
 	 *         retorna null
 	 */
 	public Calendar getFecha() {
 		Calendar fecha = Calendar.getInstance();
 		Date date = new Date(_dfFecha.getDate());
 		fecha.setTime(date);
 		return fecha;
 	}
 
 	/**
 	 * @return La fecha proxima ingresada en la pantalla, en caso de no existir
 	 *         se retorna null
 	 */
 	public Calendar getFechaProxima() {
 		Calendar fecha = Calendar.getInstance();
 		Date date = new Date(_dfFechaProxima.getDate());
 		fecha.setTime(date);
 		return fecha;
 	}
 
 	/**
 	 * @return La descripcion ingresada en la pantalla, en caso de no existir se
 	 *         retorna null
 	 */
 	public String getDescripcion() {
 		String descripcion = null;
 		try {
 			descripcion = _txtDescripcion.getText();
 		} catch (NullPointerException e) {
 			return descripcion;
 		}
 		return descripcion;
 	}
 	
 	public String getUid() {
 		return _uid;
 	}
 
 	public boolean isGuardado() {
 		return _guardar;
 	}
 
 	public boolean onClose() {
 		if (_txtDescripcion.getTextLength() == 0 && _juzgado == null) {
 			UiApplication.getUiApplication().popScreen(getScreen());
 			return true;
 		} else {
 			Object[] ask = { "Guardar", "Descartar", "Cancelar" };
 			int sel = Dialog.ask("Se han detectado cambios", ask, 2);
 			if (sel == 0) {
 				_guardar = true;
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			}
 			if (sel == 1) {
 				UiApplication.getUiApplication().popScreen(getScreen());
 				return true;
 			}
 			if (sel == 2) {
 				return false;
 			} else
 				return false;
 		}
 	}
 }
