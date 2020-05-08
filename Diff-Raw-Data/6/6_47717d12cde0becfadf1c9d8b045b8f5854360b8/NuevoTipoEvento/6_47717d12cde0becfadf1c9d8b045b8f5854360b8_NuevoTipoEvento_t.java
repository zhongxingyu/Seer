 package com.researchmobile.coretel.view;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.researchmobile.coretel.entity.RespuestaWS;
 import com.researchmobile.coretel.utility.ConnectState;
 import com.researchmobile.coretel.utility.ImageAdapter;
 import com.researchmobile.coretel.utility.Mensaje;
 import com.researchmobile.coretel.utility.TokenizerUtility;
 import com.researchmobile.coretel.ws.RequestWS;
 
 public class NuevoTipoEvento extends Activity implements OnClickListener, OnKeyListener{
 	private EditText nombreEditText;
 	private EditText descripcionEditText;
 	private Button guardarButton;
 	private Button iconoButton;
 	private Button cancelarButton;
 	private ProgressDialog pd = null;
 	private RespuestaWS respuesta;
 	private Mensaje mensaje;
 	private String idComunidad;
 	private String urlSeleccionado = "";
 	int seleccionado = 0;
 	private ImageView iconoEvento;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.nuevotipoevento);
 		Bundle bundle = (Bundle)getIntent().getExtras();
 		setIdComunidad((String)bundle.getString("idComunidad"));
 		setMensaje(new Mensaje());
 		setNombreEditText((EditText)findViewById(R.id.nuevotipoevento_nombre_edittext));
 		setDescripcionEditText((EditText)findViewById(R.id.nuevotipoevento_descripcion_edittext));
 		setGuardarButton((Button)findViewById(R.id.nuevotipoevento_guardar_button));
 		setIconoButton((Button)findViewById(R.id.nuevotipoevento_icono_button));
 		setCancelarButton((Button)findViewById(R.id.nuevotipoevento_cancelar_button));
 		setIconoEvento((ImageView)findViewById(R.id.nuevotipoevento_icono_imageview));
 		getIconoButton().setOnClickListener(this);
 		getCancelarButton().setOnClickListener(this);
 		getGuardarButton().setOnClickListener(this);
 		getNombreEditText().setOnKeyListener(this);
 		getDescripcionEditText().setOnKeyListener(this);
 	}
 	
 	public void mDialog(){
 		LayoutInflater factory = LayoutInflater.from(NuevoTipoEvento.this);
         
         final View textEntryView = factory.inflate(R.layout.items_icon , null);
         final GridView gv = (GridView)textEntryView.findViewById(R.id.grid);
         gv.setAdapter(new ImageAdapter(this, 100));
         final AlertDialog.Builder alert = new AlertDialog.Builder(NuevoTipoEvento.this );
         alert.setTitle( "ICONO");
         alert.setView(textEntryView);
         alert.setCancelable(true);
        
         gv.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
             	ImageAdapter img = (ImageAdapter)parent.getAdapter();
             	urlSeleccionado = img.nombre(position);
             	seleccionado = (int)parent.getAdapter().getItemId(position);
             	Log.e("pio", "icono = " + urlSeleccionado + " " + seleccionado);
                 gv.setAdapter(new ImageAdapter(NuevoTipoEvento.this, position));
             }
         });
         alert.setPositiveButton( "   OK   " ,
                 new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                    	  if (urlSeleccionado != null && !urlSeleccionado.equalsIgnoreCase("")){
                    		  verSeleccion(urlSeleccionado);  
                    	  }
                      }
                });
 
         alert.show();
 
 	}
 	
 	public void verSeleccion(String imagen){
 		TokenizerUtility tokenizer = new TokenizerUtility();
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 0;
 //		Bitmap bm = BitmapFactory.decodeFile(tokenizer.iconoResource(NuevoTipoEvento.this, "0=+=1=+=2=+=3=+=4=+=" + imagen), options);
 //        imageView.setImageBitmap(bm);
 		getIconoEvento().setImageDrawable(tokenizer.iconoResource(NuevoTipoEvento.this, "0=+=1=+=2=+=3=+=4=+=" + imagen));
 //		getIconoEvento().setImageBitmap(bm);
 	}
 
 	// Clase para ejecutar en Background
 	class NuevoEventoAsync extends AsyncTask<String, Integer, Integer> {
 
 		// Metodo que prepara lo que usara en background, Prepara el progress
 		@Override
 		protected void onPreExecute() {
 			pd = ProgressDialog.show(NuevoTipoEvento.this, "GUARDANDO DATOS", "ESPERE UN MOMENTO");
 			pd.setCancelable(false);
 		}
 
 		// Metodo con las instrucciones que se realizan en background
 		@Override
 		protected Integer doInBackground(String... urlString) {
 			try {
 				EnviarDatos();
 			} catch (Exception exception) {
 
 			}
 			return null;
 		}
 
 		private void EnviarDatos() {
 			ConnectState connect = new ConnectState();
 			String nombre = getNombreEditText().getText().toString();
 			String descripcion = getDescripcionEditText().getText().toString();
 			String incidente = "1";
 			
 			if (connect.isConnectedToInternet(NuevoTipoEvento.this)){
 				RequestWS request = new RequestWS();
 				setRespuesta(request.NuevoTipoEvento(getIdComunidad(), nombre, descripcion, incidente, urlSeleccionado));
 				Log.e("pio", "resultado = " + getRespuesta().getMensaje());
 				Log.e("pio", "resultado 1 = " + getRespuesta().isResultado());
 			}else{
 				getRespuesta().setMensaje("No cuenta con internet");
 				getRespuesta().setResultado(false);
 			}
 		}
 
 		// Metodo con las instrucciones al finalizar lo ejectuado en background
 		protected void onPostExecute(Integer resultado) {
 			pd.dismiss();
 			Log.e("pio", "fin dialog");
 			if (getRespuesta() != null){
 				Log.e("pio", "respuesta != null");
 				getMensaje().VerMensaje(NuevoTipoEvento.this, getRespuesta().getMensaje());
 				Log.e("pio", "mensaje = " + getRespuesta().getMensaje());
 				Log.e("pio", "resultado = " + getRespuesta().isResultado());
 				if (getRespuesta().isResultado()){
 					Log.e("pio", "resultado = " + getRespuesta().isResultado());
 					finish();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v == getGuardarButton()){
 			new NuevoEventoAsync().execute("");
 		}else if (v == getIconoButton()){
 			mDialog();
 		}else if (v == getCancelarButton()){
 			finish();
 		}
 	}
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public EditText getNombreEditText() {
 		return nombreEditText;
 	}
 
 	public void setNombreEditText(EditText nombreEditText) {
 		this.nombreEditText = nombreEditText;
 	}
 
 	public EditText getDescripcionEditText() {
 		return descripcionEditText;
 	}
 
 	public void setDescripcionEditText(EditText descripcionEditText) {
 		this.descripcionEditText = descripcionEditText;
 	}
 
 	public Button getGuardarButton() {
 		return guardarButton;
 	}
 
 	public void setGuardarButton(Button guardarButton) {
 		this.guardarButton = guardarButton;
 	}
 
 	public RespuestaWS getRespuesta() {
 		return respuesta;
 	}
 
 	public void setRespuesta(RespuestaWS respuesta) {
 		this.respuesta = respuesta;
 	}
 
 	public Mensaje getMensaje() {
 		return mensaje;
 	}
 
 	public void setMensaje(Mensaje mensaje) {
 		this.mensaje = mensaje;
 	}
 
 	public String getIdComunidad() {
 		return idComunidad;
 	}
 
 	public void setIdComunidad(String idComunidad) {
 		this.idComunidad = idComunidad;
 	}
 
 	public ImageView getIconoEvento() {
 		return iconoEvento;
 	}
 
 	public void setIconoEvento(ImageView iconoEvento) {
 		this.iconoEvento = iconoEvento;
 	}
 
 	public Button getIconoButton() {
 		return iconoButton;
 	}
 
 	public void setIconoButton(Button iconoButton) {
 		this.iconoButton = iconoButton;
 	}
 
 	public Button getCancelarButton() {
 		return cancelarButton;
 	}
 
 	public void setCancelarButton(Button cancelarButton) {
 		this.cancelarButton = cancelarButton;
 	}
 
 	
 	
 }
