 package gui.Nuevos;
 
 import gui.FondoNormal;
 import gui.Util;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.BitmapField;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 
 public class NuevaActuacionScreen extends FondoNormal {
 
 	private LabelField _lblJuzgado;
 	private DateField _dfFecha;
 	private DateField _dfFechaProxima;
 	private BasicEditField _txtDescripcion;
 	private BitmapField _cita;
 	private BitmapField _alarm;
 	private Bitmap _bell = Bitmap.getBitmapResource("bell.png");
 	private Bitmap _clock = Bitmap.getBitmapResource("clock.png");
 	private boolean _hasAlarm = false;
 
 	/**
 	 * Crea una NuevaActuacionScreen inicializando los componentes y
 	 * agregandolos a la pantalla
 	 */
 	public NuevaActuacionScreen() {
 		HorizontalFieldManager titleContainer = new HorizontalFieldManager();
 		titleContainer.add(new LabelField("Nueva actuacion"));
 
 		_cita = new BitmapField(null, Field.FIELD_VCENTER);
 		_alarm = new BitmapField(null, Field.FIELD_VCENTER);
 
 		titleContainer.add(_cita);
 		titleContainer.add(_alarm);
 
 		setTitle(titleContainer);
 
 		HorizontalFieldManager fldJuzgado = new HorizontalFieldManager();
 
 		_lblJuzgado = new LabelField("*Ninguno*", Field.FOCUSABLE);
 		fldJuzgado.add(new LabelField("Juzgado: "));
 		fldJuzgado.add(_lblJuzgado);
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
 	}
 
 	public void setClock() {
 		_cita.setBitmap(_clock);
 		_hasAlarm = true;
 	}
 
 	public void setBell() {
 		_alarm.setBitmap(_bell);
 	}
 
 	public void removeClock() {
 		_cita.setBitmap(null);
 		removeBell();
 		_hasAlarm = false;
 	}
 
 	public void removeBell() {
 		_alarm.setBitmap(null);
 	}
 
 	protected void makeMenu(Menu menu, int instance) {
		if(getFieldWithFocus().equals(_lblJuzgado)) {
			menu.add(menuCambiar);
			menu.addSeparator();
		}
 		if (_hasAlarm) {
 			menu.add(menuVerCita);
 			menu.add(menuEliminarCita);
 			menu.addSeparator();
 		} else {
 			menu.add(menuAddCita);
 			menu.addSeparator();
 		}
 		menu.addSeparator();
 		menu.add(menuGuardar);
 	}
 
 	private final MenuItem menuVerCita = new MenuItem("Ver cita", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.VER_CITA);
 		}
 	};
 
 	private final MenuItem menuAddCita = new MenuItem("Agregar cita", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.ADD_CITA);
 		}
 	};
 
 	private final MenuItem menuEliminarCita = new MenuItem("Eliminar cita", 0,
 			0) {
 
 		public void run() {
 			fieldChangeNotify(Util.ELIMINAR_CITA);
 		}
 	};
 
 	private final MenuItem menuCambiar = new MenuItem("Cambiar", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.ADD_JUZGADO);
 		}
 	};
 
 	private final MenuItem menuGuardar = new MenuItem("Guardar", 0, 0) {
 
 		public void run() {
 			fieldChangeNotify(Util.GUARDAR);
 		}
 	};
 
 	public void alert(String alert) {
 		Dialog.alert(alert);
 	}
 
 	public int ask(Object[] options, String string, int index) {
 		return Dialog.ask(string, options, index);
 	}
 
 	public boolean hasAlarma() {
 		return _hasAlarm;
 	}
 
 	public void setJuzgado(String text) {
 		_lblJuzgado.setText(text);
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
 		return _txtDescripcion.getText();
 	}
 
 	public boolean onClose() {
 		fieldChangeNotify(Util.CERRAR);
 		return false;
 	}
 }
