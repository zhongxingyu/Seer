 package gui.Ver;
 
 import java.util.Date;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.DateField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.container.PopupScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 public class VerCitaPopUp extends PopupScreen {
 
 	private BasicEditField _txtDescripcion;
 	private CheckboxField _cbAlarma;
 	private BasicEditField _txtTiempo;
 	private ObjectChoiceField _nfTiempo;
 	private SeparatorField _s;
 	private ButtonField _btnAceptar;
 	private ButtonField _btnCancelar;
 	private DateField _dfFecha;
 
 	public final int GUARDAR = 1;
 	public final int CERRAR = 3;
 
 	public VerCitaPopUp() {
		super(new VerticalFieldManager());
 
 		LabelField labelField = new LabelField("Ver cita", Field.FIELD_HCENTER);
 		add(labelField);
 
 		_txtDescripcion = new BasicEditField("Descripcin: ", "");
 		add(_txtDescripcion);
 
 		_dfFecha = new DateField("Fecha: ", 0, DateField.DATE_TIME);
 		add(_dfFecha);
 
 		_nfTiempo = new ObjectChoiceField(null, null, 0, FIELD_LEFT
 				| USE_ALL_WIDTH);
 
 		_txtTiempo = new BasicEditField("Anticipacin: ", null, 3,
 				BasicEditField.FILTER_INTEGER);
 
 		_cbAlarma = new CheckboxField("Alarma", false);
 		_cbAlarma.setChangeListener(listenerTiempo);
 		add(_cbAlarma);
 
 		_s = new SeparatorField();
 		add(_s);
 
 		_btnAceptar = new ButtonField("Aceptar", ButtonField.CONSUME_CLICK
 				| Field.FIELD_HCENTER);
 		_btnAceptar.setMinimalWidth(100);
 		_btnAceptar.setChangeListener(listenerAceptar);
 		add(_btnAceptar);
 
 		_btnCancelar = new ButtonField("Cancelar", ButtonField.CONSUME_CLICK
 				| Field.FIELD_HCENTER);
 		_btnCancelar.setChangeListener(listenerCancelar);
 		add(_btnCancelar);
 	}
 
 	private FieldChangeListener listenerTiempo = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			if (_cbAlarma.getChecked()) {
 				delete(_s);
 				delete(_btnAceptar);
 				delete(_btnCancelar);
 				add(_txtTiempo);
 				add(_nfTiempo);
 				add(_s);
 				add(_btnAceptar);
 				add(_btnCancelar);
 			} else {
 				delete(_txtTiempo);
 				delete(_nfTiempo);
 			}
 		}
 	};
 
 	private FieldChangeListener listenerAceptar = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			fieldChangeNotify(GUARDAR);
 		}
 	};
 
 	private FieldChangeListener listenerCancelar = new FieldChangeListener() {
 
 		public void fieldChanged(Field field, int context) {
 			fieldChangeNotify(CERRAR);
 		}
 	};
 
 	public void setChecked(boolean alarma) {
 		_cbAlarma.setChecked(alarma);
 	}
 
 	public void setDescripcion(String text) {
 		_txtDescripcion.setText(text);
 	}
 
 	public void setFecha(Date date) {
 		_dfFecha.setDate(date);
 	}
 
 	public void setAlarma(Object[] alarma) {
 		_txtTiempo.setText(((Integer) alarma[0]).toString());
 		_nfTiempo.setSelectedIndex(alarma[1]);
 	}
 
 	public void setChoices(Object[] choices) {
 		_nfTiempo.setChoices(choices);
 	}
 
 	public String getDescripcion() {
 		return _txtDescripcion.getText();
 	}
 
 	public Date getFecha() {
 		return new Date(_dfFecha.getDate());
 	}
 
 	public Object[] getAlarma() {
 		Object[] alarma = new Object[2];
 		alarma[0] = new Integer(Integer.parseInt(_txtTiempo.getText()));
 		alarma[1] = _nfTiempo.getChoice(_nfTiempo.getSelectedIndex());
 		return alarma;
 	}
 
 	public boolean hasAlarma() {
 		return _cbAlarma.getChecked();
 	}
 
 	public boolean onClose() {
 		fieldChangeNotify(CERRAR);
 		return false;
 	}
 }
