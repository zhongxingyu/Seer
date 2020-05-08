 /**
  * datos a buscar en TukenizerUtility
 
  * getTitulo
  * titulo - idAnotacion - idComunidad - usuarioAnoto - tipoAnotacion - icono
  * 
  * getDescripcion
  * descripcion - fechaRegistro - nombreUsuario - nombreComunidad - imagen
  * 
  * 8va calle 7-63 zona 12, UNI   Tel 23765333
  */
 
 package alvarado.wuil;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.researchmobile.coretel.entity.CatalogoComunidad;
 import com.researchmobile.coretel.entity.CatalogoTipoAnotacion;
 import com.researchmobile.coretel.entity.RespuestaWS;
 import com.researchmobile.coretel.entity.User;
 import com.researchmobile.coretel.utility.ConnectState;
 import com.researchmobile.coretel.utility.Fecha;
 import com.researchmobile.coretel.utility.Mensaje;
 import com.researchmobile.coretel.utility.TokenizerUtility;
 import com.researchmobile.coretel.ws.RequestWS;
 
 public class Evento extends Activity implements OnClickListener, OnKeyListener{
 	final static int CAMERA_RESULT = 0;
 	private static int TAKE_PICTURE = 1;
 	private static int SELECT_PICTURE = 2;
 	
 	private Button cameraButton;
 	private Button borrarButton;
 	private Button guardarButton;
 	private Button descripcionButton;
 	private LinearLayout imagenLayout;
 	private ImageView fotoEvento;
 	private String pathFoto;
 	private String latitud;
 	private String longitud;
 	private String titulo;
 	private String descripcion;
 	private Mensaje mensaje;
 	private Fecha fecha;
 	
 	private TextView fechaTextView;
 	private TextView latitudTextView;
 	private TextView longitudTextView;
 	private TextView comunidadTextView;
 	private TextView tipoTextView;
 	private TextView descripcionTextView;
 	private Spinner tipoEventoSpinnet;
 	private Spinner comunidadSpinner;
 	private CatalogoComunidad catalogoComunidad;
 	private CatalogoTipoAnotacion catalogoTipoAnotacion;
 	private TokenizerUtility tokenizer = new TokenizerUtility();
 	private RespuestaWS respuestaWS = new RespuestaWS();
 	
 	private ProgressDialog pd = null;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.evento);
 		new ComunidadesAsync().execute("");
 		Bundle bundle = (Bundle)getIntent().getExtras();
 		String lat = bundle.getString("latitud");
 		String lon = bundle.getString("longitud");
 		setTitulo(bundle.getString("titulo"));
 		setDescripcion(bundle.getString("descripcion"));
 		
 		setLatitud(String.valueOf(Integer.parseInt(lat)/1E6));
 		setLongitud(String.valueOf(Integer.parseInt(lon)/1E6));
 		setMensaje(new Mensaje());
 		getMensaje().VerMensaje(this, getLatitud());
 		setFecha(new Fecha());
 		
 		setCameraButton((Button)findViewById(R.id.evento_capturar_button));
 		setBorrarButton((Button)findViewById(R.id.evento_borrar_button));
 		setGuardarButton((Button)findViewById(R.id.evento_save_button));
 		setDescripcionButton((Button)findViewById(R.id.evento_descripcion_button));
 		setFotoEvento((ImageView)findViewById(R.id.evento_foto_imageview));
 		getCameraButton().setOnClickListener(this);
 		getBorrarButton().setOnClickListener(this);
 		getGuardarButton().setOnClickListener(this);
 		getDescripcionButton().setOnClickListener(this);
 		
 		setImagenLayout((LinearLayout)findViewById(R.id.evento_imagen_layout));
 		getImagenLayout().setVisibility(View.INVISIBLE);
 		setFechaTextView((TextView)findViewById(R.id.evento_fecha_textview));
 		setLatitudTextView((TextView)findViewById(R.id.evento_latitud_textview));
 		setLongitudTextView((TextView)findViewById(R.id.evento_longitud_textview));
 		setDescripcionTextView((TextView)findViewById(R.id.evento_descripcion_textview));
 		setTipoEventoSpinnet((Spinner)findViewById(R.id.evento_tipo_spinner));
 		setComunidadSpinner((Spinner)findViewById(R.id.evento_comunidad_spinner));
 		
 		
 		if(getTitulo().equalsIgnoreCase("nuevo")){
 			iniciaNuevo();
 		}else{
 			verEvento();
 		}
 		
 		
 		new ComunidadesAsync().execute("");
 		
 	}
 	
 	private void iniciaNuevo(){
 		getFechaTextView().setText(getFecha().FechaHoy());
 		getLatitudTextView().setText(getLatitud());
 		getLongitudTextView().setText(getLongitud());
 	}
 	
 	private void verEvento(){
 		if (!User.getUserId().equalsIgnoreCase(tokenizer.usuarioAnoto(getTitulo()))){
 			getBorrarButton().setEnabled(false);
 		}
 		getComunidadSpinner().setEnabled(false);
 		getTipoEventoSpinnet().setEnabled(false);
 		getCameraButton().setEnabled(false);
 		getGuardarButton().setEnabled(false);
 		getFechaTextView().setText(tokenizer.fechaRegistro(getDescripcion()));
 		getComunidadTextView().setText(tokenizer.nombreComunidad(getDescripcion()));
 		getTipoTextView().setText(tokenizer.tipoAnotacion(getTitulo()));
 		getLatitudTextView().setText(getLatitud());
 		getLongitudTextView().setText(getLongitud());
 	}
 	
 	private void CargarDatosSpinner() {
 		Comunidades();
 		getComunidadSpinner().setOnItemSelectedListener(
 				new AdapterView.OnItemSelectedListener() {
 					public void onItemSelected(AdapterView<?> parent,
 							android.view.View v, int position, long id) {
 						TipoAnotacion();
 
 					}
 
 					public void onNothingSelected(AdapterView<?> parent) {
 						// textView.setText("");
 					}
 				});
 		
 	}
 
 	private void Comunidades() {
 		ArrayAdapter<String> adaptador =  new ArrayAdapter<String>(this, R.layout.item_spinner, R.id.item_spinner_textview, misComunidades());
 		getComunidadSpinner().setAdapter(adaptador);
 	}
 
 	private String[] misComunidades() {
 		String[] error = {" "};
 		try{
 			int tamano = getCatalogoComunidad().getComunidad().length;
 			String[] comunidades = new String[tamano];
 			for (int i = 0; i < tamano; i++){
 				comunidades[i] = getCatalogoComunidad().getComunidad()[i].getNombre();
 			}
 			if (tamano > 0){
 				return comunidades;
 			}else{
 				return error;
 			}
 		}catch(Exception exception){
 			return error;
 		}
 	}
 
 	private void TipoAnotacion() {
 		try{
 			String selectComunidad = getComunidadSpinner().getSelectedItem().toString();
 			System.out.println("comunidad seleccionada = " + selectComunidad);
 			String idComunidad = "";
 			int tamano = getCatalogoComunidad().getComunidad().length;
 			for (int i = 0; i < tamano; i++){
 				if (getCatalogoComunidad().getComunidad()[i].getNombre().equalsIgnoreCase(selectComunidad)){
 					idComunidad = getCatalogoComunidad().getComunidad()[i].getId();
 				}
 			}
 			
 			RequestWS request = new RequestWS();
 			
 			setCatalogoTipoAnotacion((CatalogoTipoAnotacion)request.BuscarTiposEventos(idComunidad));
 			
 			ArrayAdapter<String> adaptador =  new ArrayAdapter<String>(this, R.layout.item_spinner, R.id.item_spinner_textview, tipoAnotacion());
 			getTipoEventoSpinnet().setAdapter(adaptador);
 		}catch(Exception exception){
 			
 		}
 		
 	}
 	
 	 private String[] tipoAnotacion() {
 		 try{
 			 int tamano = getCatalogoTipoAnotacion().getTipoAnotacion().length;
 				String[] tipos = new String[tamano];
 				for (int i = 0; i < tamano; i++){
 					tipos[i] = getCatalogoTipoAnotacion().getTipoAnotacion()[i].getNombre();
 				}
 				return tipos;
 		 }catch(Exception exception){
 			 return null;
 		 }
 		
 	}
 
 	// Clase para ejecutar en Background
     class ComunidadesAsync extends AsyncTask<String, Integer, Integer> {
 
           // Metodo que prepara lo que usara en background, Prepara el progress
           @Override
           protected void onPreExecute() {
                 
          }
 
           // Metodo con las instrucciones que se realizan en background
           @Override
           protected Integer doInBackground(String... urlString) {
                 try {
                 	CargarDatos();
                 
                } catch (Exception exception) {
 
                }
                 return null ;
          }
 
           private void CargarTipoAnotacion() {
 			RequestWS request = new RequestWS();
 			String selectComunidad = getComunidadSpinner().getSelectedItem().toString();
 			String idComunidad = "";
 			int tamano = getCatalogoComunidad().getComunidad().length;
 			for (int i = 0; i < tamano; i++){
 				if (getCatalogoComunidad().getComunidad()[i].getNombre().equalsIgnoreCase(selectComunidad)){
 					idComunidad = getCatalogoComunidad().getComunidad()[i].getId();
 				}
 			}
 			setCatalogoTipoAnotacion(request.BuscarTiposEventos(idComunidad));
 		}
 
 		private void CargarDatos() {
         	  RequestWS request = new RequestWS();
       		  setCatalogoComunidad(request.CargarComunidades(User.getUserId()));
       		  CargarTipoAnotacion();
 			
 		}
 
 		// Metodo con las instrucciones al finalizar lo ejectuado en background
          protected void onPostExecute(Integer resultado) {
         	 CargarDatosSpinner();
          }
     }
     
 	// Clase para ejecutar en Background
 	class enviarAsync extends AsyncTask<String, Integer, Integer> {
 
 		// Metodo que prepara lo que usara en background, Prepara el progress
 		@Override
 		protected void onPreExecute() {
			pd = ProgressDialog.show(Evento.this, "ENVIAR", "ESPERE UN MOMENTO");
 			pd.setCancelable(false);
 		}
 
 		// Metodo con las instrucciones que se realizan en background
 		@Override
 		protected Integer doInBackground(String... urlString) {
 			try {
 				EnviarEvento();
 			} catch (Exception exception) {
 
 			}
 			return null;
 		}
 
 		// Metodo con las instrucciones al finalizar lo ejectuado en background
 		protected void onPostExecute(Integer resultado) {
 			pd.dismiss();
 			finish();
 
 		}
 	}
 
 	@Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
 	@Override
 	public void onClick(View view) {
 		if (view == getCameraButton()){
 			fotografiaDialog();
 		}else if (view == getBorrarButton()){
 			borrarEvento();
 		}else if (view == getGuardarButton()){
 			dialogEnviar();
 		}else if (view == getDescripcionButton()){
 			dialogDescripcion();
 		}
 //			mostrarGaleria();
 	}
 	
 	private void mostrarGaleria(){
 		try{
 			int code = TAKE_PICTURE;
 			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
 			code = SELECT_PICTURE;
 			startActivityForResult(intent, code);
 		}catch(Exception exception){
 			
 		}
 	}
 	
 	private void dialogEnviar(){
 		new AlertDialog.Builder(this)
         .setTitle("Enviar Evento")
         .setMessage("Esta seguro de enviar el evento?")
         .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	new enviarAsync().execute(""); 
                 }
         })
         .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
 //                	new enviarAsync().execute(""); 
                 }
         })
         .show();
 
 	}
 	private void borrarEvento(){
 		new AlertDialog.Builder(this)
         .setTitle("Borrar Evento")
         .setMessage("Esta seguro que desea eliminar el evento")
         .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                      new eliminaEventoAsync().execute("");
                 }
         })
         .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	Toast.makeText(getBaseContext(), "Operacion cancelada", Toast.LENGTH_SHORT).show();
                 }
         })
         .show();
 
 	}
 	
 	private void dialogDescripcion() {
         LayoutInflater factory = LayoutInflater.from(Evento.this);
        
         final View textEntryView = factory.inflate(R.layout.dialog_descripcion , null);
        
         final EditText descripcionEditText = (EditText) textEntryView.findViewById(R.id.dialog_descripcion_edittext);
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
         
         alert.setPositiveButton( "OK" ,
                 new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                     	  String descripcion = descripcionEditText.getText().toString();
                     	  getDescripcionTextView().setText(descripcion);
                      }
          });
        alert.setTitle( "Descripcin");
        alert.setView(textEntryView);
        
        alert.show();
 	}
 	
 	private void fotografiaDialog() {
         LayoutInflater factory = LayoutInflater.from(Evento.this);
        
         final View textEntryView = factory.inflate(R.layout.dialog_fotografia , null);
        
         final Button capturarButton = (Button) textEntryView.findViewById(R.id.dialog_fotografia_camara);
         final Button albumButton = (Button) textEntryView.findViewById(R.id.dialog_fotografia_album);
         
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
         
         capturarButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				ActivarCamara();
 				
 			}
 		});
         
         alert.setPositiveButton( "CERRAR" ,
                 new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface arg0, int arg1) {
                     	  MostrarImagen();
                      }
          });
        alert.setTitle( "Agregar Fotografa");
        alert.setView(textEntryView);
        
        alert.show();
 	}
 
 
 	private void MostrarImagen() {
 		if(getTitulo().equalsIgnoreCase("nuevo")){
 			verImagen();
 		}else{
 			String url = "http://23.23.1.2/WS/" + tokenizer.imagen(getDescripcion());
 			Log.e("TT", "url imagen = " + url);
 			new descargaImagenes().execute(url);
 		}
 		getImagenLayout().setVisibility(View.VISIBLE);
 	}
 
 	private void EnviarEvento() {
 		
 		try{
 			ConnectState connect = new ConnectState();
 			RequestWS request = new RequestWS();
 			if (connect.isConnectedToInternet(this)){
 				String idUsuario = User.getUserId();
 			    String comunidad = ComunidadSeleccionada();
 			    String tipoAnotacion = TipoSeleccionado();
			    String descripcion = getDescripcionTextView().getText().toString();
 			    String imagen = fotoReducida();
 				RespuestaWS respuesta = new RespuestaWS();
 				request.MandarEvento(titulo, getLatitud(), getLongitud(), idUsuario, comunidad, tipoAnotacion, descripcion, imagen);
 				if (respuesta.isResultado()){
 					getMensaje().VerMensaje(this, respuesta.getMensaje());
 					finish();
 				}
 			}
 		}catch(Exception exception){
 			
 		}
 	}
 	
 	private String fotoReducida(){
 		String fotonueva = "/captura.jpg";
 		BitmapFactory.Options options = new BitmapFactory.Options();
         options.inSampleSize = 0;
         Bitmap bm = BitmapFactory.decodeFile("sdcard" + getPathFoto());
 
         File file = new File("sdcard" + fotonueva);
         try {
         file.createNewFile();
         FileOutputStream out = new FileOutputStream(file);
         bm.compress(Bitmap.CompressFormat.JPEG, 20, out);//Convertimos la imagen a JPEG
         return fotonueva;
         } catch (Exception e) {
         	e.printStackTrace();
         	return null;
         }
 	}
 
 	private String TipoSeleccionado() {
 		String anotacion = "";
 		String tipo = getTipoEventoSpinnet().getSelectedItem().toString();
 		int tamano = getCatalogoTipoAnotacion().getTipoAnotacion().length;
 		for (int i = 0; i < tamano; i++){
 			if (getCatalogoTipoAnotacion().getTipoAnotacion()[i].getNombre().equalsIgnoreCase(tipo)){
 				anotacion = getCatalogoTipoAnotacion().getTipoAnotacion()[i].getId();
 			}
 		}
 		return anotacion;
 	}
 
 	private String ComunidadSeleccionada() {
 		String comunidad = "";
 		String com = getComunidadSpinner().getSelectedItem().toString();
 		int tamano = getCatalogoComunidad().getComunidad().length;
 		for (int i = 0; i < tamano; i++){
 			if (getCatalogoComunidad().getComunidad()[i].getNombre().equalsIgnoreCase(com)){
 				comunidad = getCatalogoComunidad().getComunidad()[i].getId();
 			}
 		}
 		return comunidad;
 	}
 
 	private void ActivarCamara() {
 		//Activar la camara
 	  	Intent cIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 		//startActivityForResult(cIntent, CAMERA_RESULT); 
 		//asignar nombre y direccion a la imagen
 		setPathFoto("/foto1-even.jpg");
 		String path = "/mifoto.jpg";
 		//crear nuevo archivo (imagen)
 		File f = new File(Environment.getExternalStorageDirectory() + getPathFoto());
 		Uri uri = Uri.fromFile(f);
 		cIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
 		startActivityForResult(cIntent, CAMERA_RESULT);
 	}
 
 	private void verImagen() {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 0;
 		Log.e("Log", "ver foto = sdcard" + getPathFoto());
         Bitmap bm = BitmapFactory.decodeFile("sdcard" + getPathFoto(), options);
         getFotoEvento().setImageBitmap(bm);
 	}
 	
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		 
              if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
              {
                  
                  return true;
              }
              if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
              {
                  //TODO: When the enter key is pressed
                  return true;
              }
              return false;
          
 	}
 	
 	private void eliminarEvento(){
 		RequestWS request = new RequestWS();
 		respuestaWS = request.eliminaEvento(tokenizer.idAnotacion(getTitulo()), User.getUserId());
 	}
 	
 	// Clase para ejecutar en Background
     class eliminaEventoAsync extends AsyncTask<String, Integer, Integer> {
 
           // Metodo que prepara lo que usara en background, Prepara el progress
           @Override
           protected void onPreExecute() {
                 pd = ProgressDialog. show(Evento.this, "Eliminar Evento", "ESPERE UN MOMENTO");
                 pd.setCancelable( false);
          }
 
           // Metodo con las instrucciones que se realizan en background
           @Override
           protected Integer doInBackground(String... urlString) {
                 try {
                 	eliminarEvento();
                } catch (Exception exception) {
 
                }
                 return null ;
          }
           // Metodo con las instrucciones al finalizar lo ejectuado en background
           protected void onPostExecute(Integer resultado) {
                 pd.dismiss();
                 if (respuestaWS != null){
                 	if (respuestaWS.isResultado()){
                 		Intent intent = new Intent(Evento.this, MapWuil.class);
                 		startActivity(intent);
                 	}else{
                 		Toast.makeText(getBaseContext(), respuestaWS.getMensaje(), Toast.LENGTH_SHORT).show();
                 	}
                 }
          }
    }
 
 	
 	class descargaImagenes extends AsyncTask<String, Integer, Integer>{
 
 		ProgressBar miBarra;
 		
 		@Override
 		protected void onPreExecute(){
 			miBarra = (ProgressBar)findViewById(R.id.barraProgreso);
 			
 		}
 		@Override
 		protected Integer doInBackground(String... urlString) {
 			try {
 				URL url = new URL(urlString[0]);
 				URLConnection conexion = url.openConnection();
 				conexion.connect();
 				
 				int tamano = conexion.getContentLength();
 				Log.e("TT", "resultado busqueda imagen = " + tamano);
 				InputStream input = new BufferedInputStream(url.openStream());
 				OutputStream output = new FileOutputStream("/sdcard/temp.jpg");
 				
 				byte datos[] = new byte[1024];
 				int cuenta;
 				long total = 0;
 				
 				while ((cuenta = input.read(datos)) != -1){
 					total += cuenta;
 					output.write(datos, 0, cuenta);
 					
 					this.publishProgress((int)total*100/tamano);
 				}
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer...progreso){
 			int porcentaje = progreso[0];
 			miBarra.setProgress(porcentaje);
 		}
 		
 		protected void onPostExecute(Integer resultado){
 			getFotoEvento().setImageBitmap(BitmapFactory.decodeFile("/sdcard/temp.jpg"));
 		}
 	}
 	
 	/**
      * Funcin que se ejecuta cuando concluye el intent en el que se solicita una imagen
      * ya sea de la cmara o de la galera
      */
     @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	/**
     	 * Se revisa si la imagen viene de la cmara (TAKE_PICTURE) o de la galera (SELECT_PICTURE)
     	 */
     	if (requestCode == SELECT_PICTURE){
     		if (data != null){
     			setPathFoto(data.getDataString());
     		}
     	}
     }
 
 	public Button getCameraButton() {
 		return cameraButton;
 	}
 
 	public void setCameraButton(Button cameraButton) {
 		this.cameraButton = cameraButton;
 	}
 
 	public Button getBorrarButton() {
 		return borrarButton;
 	}
 
 	public void setBorrarButton(Button borrarButton) {
 		this.borrarButton = borrarButton;
 	}
 
 	public ImageView getFotoEvento() {
 		return fotoEvento;
 	}
 
 	public void setFotoEvento(ImageView fotoEvento) {
 		this.fotoEvento = fotoEvento;
 	}
 
 	public String getPathFoto() {
 		return pathFoto;
 	}
 
 	public void setPathFoto(String pathFoto) {
 		this.pathFoto = pathFoto;
 	}
 
 	public String getLatitud() {
 		return latitud;
 	}
 
 	public void setLatitud(String latitud) {
 		this.latitud = latitud;
 	}
 
 	public String getLongitud() {
 		return longitud;
 	}
 
 	public void setLongitud(String longitud) {
 		this.longitud = longitud;
 	}
 
 	public String getTitulo() {
 		return titulo;
 	}
 
 	public void setTitulo(String titulo) {
 		this.titulo = titulo;
 	}
 
 	public String getDescripcion() {
 		return descripcion;
 	}
 
 	public void setDescripcion(String descripcion) {
 		this.descripcion = descripcion;
 	}
 
 	public Mensaje getMensaje() {
 		return mensaje;
 	}
 
 	public void setMensaje(Mensaje mensaje) {
 		this.mensaje = mensaje;
 	}
 
 	public TextView getFechaTextView() {
 		return fechaTextView;
 	}
 
 	public void setFechaTextView(TextView fechaTextView) {
 		this.fechaTextView = fechaTextView;
 	}
 
 	public TextView getLatitudTextView() {
 		return latitudTextView;
 	}
 
 	public void setLatitudTextView(TextView latitudTextView) {
 		this.latitudTextView = latitudTextView;
 	}
 
 	public TextView getLongitudTextView() {
 		return longitudTextView;
 	}
 
 	public void setLongitudTextView(TextView longitudTextView) {
 		this.longitudTextView = longitudTextView;
 	}
 
 	public Spinner getTipoEventoSpinnet() {
 		return tipoEventoSpinnet;
 	}
 
 	public void setTipoEventoSpinnet(Spinner tipoEventoSpinnet) {
 		this.tipoEventoSpinnet = tipoEventoSpinnet;
 	}
 
 	public Spinner getComunidadSpinner() {
 		return comunidadSpinner;
 	}
 
 	public void setComunidadSpinner(Spinner comunidadSpinner) {
 		this.comunidadSpinner = comunidadSpinner;
 	}
 
 	public Button getGuardarButton() {
 		return guardarButton;
 	}
 
 	public void setGuardarButton(Button guardarButton) {
 		this.guardarButton = guardarButton;
 	}
 
 	public Fecha getFecha() {
 		return fecha;
 	}
 
 	public void setFecha(Fecha fecha) {
 		this.fecha = fecha;
 	}
 
 	public CatalogoComunidad getCatalogoComunidad() {
 		return catalogoComunidad;
 	}
 
 	public void setCatalogoComunidad(CatalogoComunidad catalogoComunidad) {
 		this.catalogoComunidad = catalogoComunidad;
 	}
 
 	public CatalogoTipoAnotacion getCatalogoTipoAnotacion() {
 		return catalogoTipoAnotacion;
 	}
 
 	public void setCatalogoTipoAnotacion(CatalogoTipoAnotacion catalogoTipoAnotacion) {
 		this.catalogoTipoAnotacion = catalogoTipoAnotacion;
 	}
 
 	public LinearLayout getImagenLayout() {
 		return imagenLayout;
 	}
 
 	public void setImagenLayout(LinearLayout imagenLayout) {
 		this.imagenLayout = imagenLayout;
 	}
 
 	public TextView getComunidadTextView() {
 		return comunidadTextView;
 	}
 
 	public void setComunidadTextView(TextView comunidadTextView) {
 		this.comunidadTextView = comunidadTextView;
 	}
 
 	public TextView getTipoTextView() {
 		return tipoTextView;
 	}
 
 	public void setTipoTextView(TextView tipoTextView) {
 		this.tipoTextView = tipoTextView;
 	}
 
 	public TextView getDescripcionTextView() {
 		return descripcionTextView;
 	}
 
 	public void setDescripcionTextView(TextView descripcionTextView) {
 		this.descripcionTextView = descripcionTextView;
 	}
 
 	public Button getDescripcionButton() {
 		return descripcionButton;
 	}
 
 	public void setDescripcionButton(Button descripcionButton) {
 		this.descripcionButton = descripcionButton;
 	}
 	
 }
