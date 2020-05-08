 package pe.edu.pucp.proyectorh.reclutamiento;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pe.edu.pucp.proyectorh.R;
 import pe.edu.pucp.proyectorh.connection.ConnectionManager;
 import pe.edu.pucp.proyectorh.model.Evaluacion;
 import pe.edu.pucp.proyectorh.model.Funcion;
 import pe.edu.pucp.proyectorh.model.OfertaLaboral;
 import pe.edu.pucp.proyectorh.model.Postulante;
 import pe.edu.pucp.proyectorh.model.Respuesta;
 import pe.edu.pucp.proyectorh.services.AsyncCall;
 import pe.edu.pucp.proyectorh.services.ConstanteServicio;
 import pe.edu.pucp.proyectorh.services.ErrorServicio;
 import pe.edu.pucp.proyectorh.services.Servicio;
 import pe.edu.pucp.proyectorh.utils.Constante;
 import pe.edu.pucp.proyectorh.utils.EstiloApp;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @SuppressLint({ "ValidFragment", "ValidFragment" })
 public class EvaluacionPostulanteTerceraFase extends Fragment {
 
 	private View rootView;
 	private Postulante postulante;
 	private OfertaLaboral oferta;
 	private ArrayList<Funcion> funciones;
 	private ArrayList<Respuesta> respuestas;
 	private Evaluacion evaluacion;
 	private int numPagina;
 	private int totalPaginas;
 	private final int PREGUNTAS_X_PAGINA = 4;
 
 	public EvaluacionPostulanteTerceraFase(OfertaLaboral oferta,
 			Postulante postulante) {
 		this.oferta = oferta;
 		this.postulante = postulante;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		rootView = inflater.inflate(R.layout.rendir_evaluaciones, container,
 				false);
 		llamarServicioObtenerEvaluacion();
 		activarBotonAtras();
 		activarBotonFinalizar();
 		activarBotonSiguiente();
 		customizarEstilos(getActivity(), rootView);
 		return rootView;
 	}
 
 	private void customizarEstilos(Context context, View view) {
 		try {
 			if (view instanceof ViewGroup) {
 				ViewGroup vg = (ViewGroup) view;
 				for (int i = 0; i < vg.getChildCount(); i++) {
 					View child = vg.getChildAt(i);
 					customizarEstilos(context, child);
 				}
 			} else if (view instanceof TextView) {
 				((TextView) view).setTypeface(Typeface.createFromAsset(
 						context.getAssets(), EstiloApp.FORMATO_LETRA_APP));
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	private void activarBotonSiguiente() {
 		Button botonSiguiente = (Button) rootView
 				.findViewById(R.id.siguienteEvaluacion);
 		botonSiguiente.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (numPagina < totalPaginas - 1) {
 					guardarRespuestas();
 					numPagina++;
 					refreshLayout();
 				} else {
 					Toast.makeText(getActivity(),
 							"Estas son las ltimas preguntas de la evaluacin",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private void activarBotonAtras() {
 		Button botonAtras = (Button) rootView
 				.findViewById(R.id.atrasEvaluacion);
 		botonAtras.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (numPagina > 0) {
 					guardarRespuestas();
 					numPagina--;
 					refreshLayout();
 				} else {
 					Toast.makeText(
 							getActivity(),
 							"Estas son las primeras preguntas de la evaluacin",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private void activarBotonFinalizar() {
 		Button botonFinalizar = (Button) rootView.findViewById(R.id.finalizar);
 		botonFinalizar.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				guardarRespuestas();
 				if (seCompletoEvaluacion()) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(
 							getActivity());
 					builder.setTitle("Finalizar evaluacin");
 					builder.setMessage("Desea finalizar la evaluacin con los resultados registrados?");
 					builder.setCancelable(false);
 					builder.setNegativeButton("Cancelar",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 					builder.setPositiveButton("Aceptar",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									Date actual = new Date(System
 											.currentTimeMillis());
 									SimpleDateFormat formatoFecha = new SimpleDateFormat();
 									formatoFecha
											.applyPattern("dd/M/yyyy HH:mm:ss");
 									evaluacion.setFechaFin(formatoFecha
 											.format(actual));
 									FragmentTransaction ft = getActivity()
 											.getSupportFragmentManager()
 											.beginTransaction();
 									ConfirmacionEvaluacionTerceraFase fragment = new ConfirmacionEvaluacionTerceraFase(
 											oferta, postulante, funciones,
 											respuestas, evaluacion);
 									ft.setCustomAnimations(
 											android.R.anim.slide_in_left,
 											android.R.anim.slide_out_right);
 									ft.replace(R.id.opcion_detail_container,
 											fragment, "detailFragment")
 											.commit();
 								}
 							});
 					builder.create();
 					builder.show();
 				} else {
 					Toast.makeText(
 							getActivity(),
 							"Debe completar todas las preguntas de la evaluacin para finalizar",
 							Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 
 	protected void refreshLayout() {
 		TextView pregunta1Text = (TextView) rootView
 				.findViewById(R.id.pregunta1);
 		TextView pregunta2Text = (TextView) rootView
 				.findViewById(R.id.pregunta2);
 		TextView pregunta3Text = (TextView) rootView
 				.findViewById(R.id.pregunta3);
 		TextView pregunta4Text = (TextView) rootView
 				.findViewById(R.id.pregunta4);
 		RatingBar ratingPregunta1 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta1);
 		RatingBar ratingPregunta2 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta2);
 		RatingBar ratingPregunta3 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta3);
 		RatingBar ratingPregunta4 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta4);
 
 		if (numPagina * PREGUNTAS_X_PAGINA < funciones.size()) {
 			pregunta1Text.setText(numPagina
 					* PREGUNTAS_X_PAGINA
 					+ 1
 					+ ") "
 					+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 0)
 							.getDescripcion());
 
 			ratingPregunta1.setRating(respuestas.get(
 					numPagina * PREGUNTAS_X_PAGINA + 0).getPuntaje());
 			ratingPregunta1.setVisibility(View.VISIBLE);
 		}
 
 		if (numPagina * PREGUNTAS_X_PAGINA + 1 < funciones.size()) {
 			pregunta2Text.setText(numPagina
 					* PREGUNTAS_X_PAGINA
 					+ 2
 					+ ") "
 					+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 1)
 							.getDescripcion());
 
 			ratingPregunta2.setRating(respuestas.get(
 					numPagina * PREGUNTAS_X_PAGINA + 1).getPuntaje());
 			ratingPregunta2.setVisibility(View.VISIBLE);
 		} else {
 			pregunta2Text.setText(Constante.CADENA_VACIA);
 			ratingPregunta2.setVisibility(View.INVISIBLE);
 			return;
 		}
 
 		if (numPagina * PREGUNTAS_X_PAGINA + 2 < funciones.size()) {
 			pregunta3Text.setText(numPagina
 					* PREGUNTAS_X_PAGINA
 					+ 3
 					+ ") "
 					+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 2)
 							.getDescripcion());
 			ratingPregunta3.setRating(respuestas.get(
 					numPagina * PREGUNTAS_X_PAGINA + 2).getPuntaje());
 			ratingPregunta3.setVisibility(View.VISIBLE);
 		} else {
 			pregunta3Text.setText(Constante.CADENA_VACIA);
 			ratingPregunta3.setVisibility(View.INVISIBLE);
 			return;
 		}
 
 		if (numPagina * PREGUNTAS_X_PAGINA + 3 < funciones.size()) {
 			pregunta4Text.setText(numPagina
 					* PREGUNTAS_X_PAGINA
 					+ 4
 					+ ") "
 					+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 3)
 							.getDescripcion());
 			ratingPregunta4.setRating(respuestas.get(
 					numPagina * PREGUNTAS_X_PAGINA + 3).getPuntaje());
 			ratingPregunta4.setVisibility(View.VISIBLE);
 		} else {
 			pregunta4Text.setText(Constante.CADENA_VACIA);
 			ratingPregunta4.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	protected void guardarRespuestas() {
 		if (numPagina * PREGUNTAS_X_PAGINA < funciones.size()) {
 			RatingBar ratingPregunta1 = (RatingBar) rootView
 					.findViewById(R.id.ratingPregunta1);
 			respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 0).setPuntaje(
 					(int) ratingPregunta1.getRating());
 		}
 
 		if (numPagina * PREGUNTAS_X_PAGINA + 1 < funciones.size()) {
 			RatingBar ratingPregunta2 = (RatingBar) rootView
 					.findViewById(R.id.ratingPregunta2);
 			respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 1).setPuntaje(
 					(int) ratingPregunta2.getRating());
 		}
 
 		if (numPagina * PREGUNTAS_X_PAGINA + 2 < funciones.size()) {
 			RatingBar ratingPregunta3 = (RatingBar) rootView
 					.findViewById(R.id.ratingPregunta3);
 			respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 2).setPuntaje(
 					(int) ratingPregunta3.getRating());
 		}
 		if (numPagina * PREGUNTAS_X_PAGINA + 3 < funciones.size()) {
 			RatingBar ratingPregunta4 = (RatingBar) rootView
 					.findViewById(R.id.ratingPregunta4);
 			respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 3).setPuntaje(
 					(int) ratingPregunta4.getRating());
 		}
 	}
 
 	private void llamarServicioObtenerEvaluacion() {
 		obtenerEvaluacionPostulante();
 	}
 
 	private void mostrarEvaluacion() {
 		TextView tituloPuestoText = (TextView) rootView
 				.findViewById(R.id.puesto_title);
 		tituloPuestoText.setText("Puesto: " + oferta.getPuesto().getNombre());
 		TextView tituloPostulanteText = (TextView) rootView
 				.findViewById(R.id.postulante_title);
 		tituloPostulanteText.setText("Postulante: " + postulante.toString());
 
 		numPagina = 0;
 		refreshLayout();
 	}
 
 	private boolean seCompletoEvaluacion() {
 		for (Respuesta respuesta : respuestas) {
 			if (!seEvaluo(respuesta.getPuntaje())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean seEvaluo(int puntaje) {
 		return puntaje > 0 ? true : false;
 	}
 
 	private void obtenerEvaluacionPostulante() {
 		if (ConnectionManager.connect(getActivity())) {
 			String request = Servicio.ObtenerEvaluacionTerceraFase
 					+ "?idOfertaLaboral=" + oferta.getID();
 			new ObtencionEvaluacion(this.getActivity()).execute(request);
 		} else {
 			ErrorServicio.mostrarErrorConexion(getActivity());
 		}
 	}
 
 	public class ObtencionEvaluacion extends AsyncCall {
 
 		public ObtencionEvaluacion(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			System.out.println("Recibido: " + result.toString());
 			try {
 				JSONObject jsonObject = new JSONObject(result);
 				String respuesta = jsonObject.getString("success");
 				if (procesaRespuesta(respuesta)) {
 					JSONObject datosObject = (JSONObject) jsonObject
 							.get("data");
 					JSONArray funcionesListObject = (JSONArray) datosObject
 							.get("funciones");
 					funciones = new ArrayList<Funcion>();
 					for (int i = 0; i < funcionesListObject.length(); ++i) {
 						JSONObject funcionObject = funcionesListObject
 								.getJSONObject(i);
 						Funcion funcion = new Funcion();
 						funcion.setID(funcionObject.getInt("ID"));
 						funcion.setDescripcion(funcionObject
 								.getString("Nombre"));
 						funcion.setPuestoID(funcionObject.getString("PuestoID"));
 						funciones.add(funcion);
 					}
 					prepararRespuestasYEvaluacion();
 					mostrarEvaluacion();
 					ocultarMensajeProgreso();
 				}
 			} catch (JSONException e) {
 				ocultarMensajeProgreso();
 				ErrorServicio.mostrarErrorComunicacion(e.toString(),
 						getActivity());
 			} catch (NullPointerException ex) {
 				ocultarMensajeProgreso();
 				ErrorServicio.mostrarErrorComunicacion(ex.toString(),
 						getActivity());
 			}
 		}
 	}
 
 	private void agregarFuncionesMock() {
 		String puestoID = funciones.get(0).getPuestoID();
 		Funcion funcion1 = new Funcion(28,
 				"Desarrollar proyecciones de variables por campaa y anual.",
 				puestoID);
 		Funcion funcion2 = new Funcion(
 				29,
 				"Desarrollar informes y presentaciones requeridos por la corporacin, para talleres y eventos.",
 				puestoID);
 		Funcion funcion3 = new Funcion(30,
 				"Desarrollar proyecciones de variables por campaa y anual.",
 				puestoID);
 		Funcion funcion4 = new Funcion(
 				31,
 				"Desarrollar el informe de cobertura / rentabilidad de zonas por campaa.",
 				puestoID);
 		// Funcion funcion5 = new Funcion(
 		// 32,
 		// "Realizar anlisis que soporte el diagnstico de desempeo de tcticas comerciales del pas.",
 		// puestoID);
 		funciones.add(funcion1);
 		funciones.add(funcion2);
 		funciones.add(funcion3);
 		funciones.add(funcion4);
 		// funciones.add(funcion5);
 	}
 
 	public void prepararRespuestasYEvaluacion() {
 		respuestas = new ArrayList<Respuesta>();
 		for (Funcion funcion : funciones) {
 			Respuesta respuesta = new Respuesta();
 			respuesta.setFuncionID(funcion.getID());
 			respuesta.setPuntaje(0);
 			respuestas.add(respuesta);
 		}
 
 		evaluacion = new Evaluacion();
 		Date actual = new Date(System.currentTimeMillis());
 		SimpleDateFormat formatoFecha = new SimpleDateFormat();
		formatoFecha.applyPattern("dd/M/yyyy HH:mm:ss");
 		evaluacion.setFechaInicio(formatoFecha.format(actual));
 
 		totalPaginas = (funciones.size() + PREGUNTAS_X_PAGINA - 1)
 				/ PREGUNTAS_X_PAGINA;
 	}
 
 	public boolean procesaRespuesta(String respuestaServidor) {
 		if (ConstanteServicio.SERVICIO_OK.equals(respuestaServidor)) {
 			return true;
 		} else if (ConstanteServicio.SERVICIO_ERROR.equals(respuestaServidor)) {
 			// Se muestra mensaje de servicio no disponible
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Servicio no disponible");
 			builder.setMessage("No se pudo obtener la evaluacin. Intente nuevamente");
 			builder.setCancelable(false);
 			builder.setPositiveButton("Ok", null);
 			builder.create();
 			builder.show();
 			return false;
 		} else {
 			// Se muestra mensaje de la respuesta invalida del servidor
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Problema en el servidor");
 			builder.setMessage("Hay un problema en el servidor.");
 			builder.setCancelable(false);
 			builder.setPositiveButton("Ok", null);
 			builder.create();
 			builder.show();
 			return false;
 		}
 	}
 }
