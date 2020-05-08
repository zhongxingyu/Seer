 package gui;
 
 import java.util.Date;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.container.GridFieldManager;
 import net.rim.device.api.ui.container.PopupScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 public class NuevaCitaPopUp extends PopupScreen {
 
	/**
	 * 
	 */

 	private BasicEditField _txtDescripcion;
 	private CheckboxField _cbAlarma;
 	private BasicEditField _txtTiempo;
 	private ObjectChoiceField _nfTiempo;
 	private GridFieldManager _g;
 	private SeparatorField _s;
 	private ButtonField _btnAceptar;
 	private ButtonField _btnCancelar;
 	private LabelField _lblFecha;
 
 	private Date _fecha;
 
 	private boolean _guardar = false;
 
 	public NuevaCitaPopUp(String descripcion, Date fecha) {
 		super(new VerticalFieldManager());
 
 		_fecha = fecha;
 
 		LabelField labelField = new LabelField("Crear cita",
 				Field.FIELD_HCENTER);
 		add(labelField);
 		add(new SeparatorField());
 
 		_g = new GridFieldManager(1, 2, 8);
 		_g.setColumnProperty(0, GridFieldManager.FIXED_SIZE, 200);
 		_g.setColumnProperty(1, GridFieldManager.PREFERRED_SIZE, 20);
 
 		_txtDescripcion = new BasicEditField("Descripcin: ", descripcion);
 		add(_txtDescripcion);
 
 		_lblFecha = new LabelField(fecha.toString());
 		add(_lblFecha);
 
 		_cbAlarma = new CheckboxField("Alarma", false);
 		_cbAlarma.setChangeListener(listenerTiempo);
 		add(_cbAlarma);
 
 		_txtTiempo = new BasicEditField("Anticipacin: ", null, 3,
 				BasicEditField.FILTER_INTEGER);
 
 		Object[] choices = { "Minutos", "Horas", "Das" };
 		_nfTiempo = new ObjectChoiceField(null, choices, 0, FIELD_LEFT);
 
 		_g.add(_txtTiempo);
 		_g.add(_nfTiempo);
 
 		_s = new SeparatorField();
 		add(_s);
 		_btnAceptar = new ButtonField("Aceptar", ButtonField.CONSUME_CLICK
 				| Field.FIELD_HCENTER);
 		_btnAceptar.setChangeListener(listenerAceptar);
 		add(_btnAceptar);
 		_btnCancelar = new ButtonField("Cancelar",
 				ButtonField.CONSUME_CLICK | Field.FIELD_HCENTER);
 		_btnCancelar.setChangeListener(listenerCancelar);
 		add(_btnCancelar);
 	}
 
 	private FieldChangeListener listenerAceptar = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			_guardar = true;
 			UiApplication.getUiApplication().popScreen(getScreen());
 		}
 	};
 
 	private FieldChangeListener listenerCancelar = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			UiApplication.getUiApplication().popScreen(getScreen());
 		}
 	};
 
 	private FieldChangeListener listenerTiempo = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (_cbAlarma.getChecked()) {
 				delete(_s);
 				delete(_btnAceptar);
 				delete(_btnCancelar);
 				add(_g);
 				add(_s);
 				add(_btnAceptar);
 				add(_btnCancelar);
 			} else {
 				delete(_g);
 			}
 		}
 	};
 
 	public String getDescripcion() {
 		return _txtDescripcion.getText();
 	}
 
 	public Date getFecha() {
 		return _fecha;
 	}
 
 	public boolean isAlarma() {
 		return _cbAlarma.getChecked();
 	}
 
 	public boolean isGuardado() {
 		return _guardar;
 	}
 
 	public int getDuracion() {
		if (_txtTiempo.getTextLength() == 0) {
			return 3600;
		} else {
			return Integer.parseInt(_txtTiempo.getText());
		}
 	}
 
 	public String getIntervalo() {
 		return (String) _nfTiempo.getChoice(_nfTiempo.getSelectedIndex());
 	}
 }
