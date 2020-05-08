 package com.mimascota.mimascota;
 
 import java.io.BufferedReader;
 import java.io.File;
import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class SubirFotoYFormulario extends Fragment {
 	private JSONObject datosPerro;
 	private Uri outputFileUri;
 	private int userId = -1;
 	private boolean llenoFormulario = false;
 	private boolean llenoUbicacion = false;
 	private boolean sacoFoto = false;
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	private static final int TAKE_PICTURE = 5;
 	private static final int LLENAR_FORMULARIO = 6;
 	private static final int LLENAR_UBICACION = 7;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.fragment_subir_foto, container,
 				false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle state) {
 		super.onActivityCreated(state);
 		
 		getActivity().setRequestedOrientation(
 				ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
 
 
 		final Button buttonSFoto = (Button) getView().findViewById(R.id.bSacarFoto);
 		final Button buttonSubir = (Button) getView().findViewById(R.id.bSubir);
 		final Button buttonFormulario = (Button) getView().findViewById(R.id.bFormulario);
 		final Button buttonUbicacion = (Button) getView().findViewById(R.id.bUbicacion);
 
 		buttonUbicacion.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onClickUbicacion(v);
 			}
 
 		});
 
 		buttonSFoto.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onClickSacarFoto(v);
 			}
 		});
 
 		buttonSubir.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onClickSubir(v);
 			}
 		});
 
 		buttonFormulario.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onClickFormulario(v);
 			}
 
 		});		
 	}
 
 	private void onClickUbicacion(View v) {
 		Intent intent3 = new Intent(super.getActivity(), MapaActivity.class);
 		startActivityForResult(intent3, LLENAR_UBICACION);
 	}
 
 	private void onClickFormulario(View v) {
 		Intent intent2 = new Intent(super.getActivity(),
 				FormularioActivity.class);
 		startActivityForResult(intent2, LLENAR_FORMULARIO);
 	}
 
 	public void onClickSacarFoto(View Boton) {
 		// Create an output file.
 		File file = new File(Environment.getExternalStorageDirectory(),
 				"test.jpg");
 		outputFileUri = Uri.fromFile(file);
 		// Generate the Intent.
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
 		// Launch the camera app.
 		startActivityForResult(intent, TAKE_PICTURE);
 		// startActivityForResult(
 		// new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE);
 	}
 
 	public void onClickSubir(View Boton) {
 		if (llenoUbicacion && llenoFormulario && sacoFoto || Constantes.debug) {
 			Log.d("InputStream", "onClick");
 			String jsonString = "";
 			File ruta_sd = Environment.getExternalStorageDirectory();
 			String miFoto = ruta_sd.getAbsolutePath() + "/test.jpg";
 			try {
 				JSONObject jsonObject;
 				if (this.datosPerro == null) {
 					// harcodeo si no se lleno el formulario
 					jsonObject = new JSONObject();
 					jsonObject.put("age", "20");
					jsonObject.put("breed", "Beagle");
 					jsonObject.put("user_id", this.userId);
 					jsonObject.put("color", "agagaegfag");
 					jsonObject.put("description", "agagaegfag");
 					jsonObject.put("name", "Juan2");
 					jsonObject.put("latitude", "10.4198");
 					jsonObject.put("longitude", "10.3012");
 					jsonObject.put("gmaps", "true");
 					jsonString = jsonObject.toString();
 				} else {
 					datosPerro.put("user_id", this.userId);
 					jsonString = datosPerro.toString();
 				}
 
 				Log.d("InputStream", "StringOnClickSubir" + jsonString);
 				
 				// 1. create HttpClient
 				HttpClient httpclient = new DefaultHttpClient();
 				httpclient.getParams().setParameter(
 						CoreProtocolPNames.PROTOCOL_VERSION,
 						HttpVersion.HTTP_1_1);
 				HttpPost httppost = new HttpPost("http://"
 						+ Constantes.IPSERVER
 						+ ":3000/cargador/subirPerroBuscado");
 				File file = new File(miFoto);
 				MultipartEntity mpEntity = new MultipartEntity();
 				ContentBody foto = new FileBody(file, "image/jpeg");
 				mpEntity.addPart("fotoUp", foto);
 				mpEntity.addPart("jsonString", new StringBody(jsonString));
 				httppost.setEntity(mpEntity);
 				httpclient.execute(httppost);
 				Toast.makeText(this.getActivity(), "Gracias por enviar!!!",
 						Toast.LENGTH_SHORT).show();
 			} catch (Exception e) {
 				Log.d("InputStream", e.getLocalizedMessage());
 				Toast.makeText(this.getActivity(), "Error de conexion con servidor",
 						Toast.LENGTH_SHORT).show();				
 			}
 		} else {
 			String msg = "";
 			int cant = 0;
 			if(!llenoFormulario) {
 				msg = "Falta llenar el formulario";
 				cant++;
 			}
 			if(!sacoFoto) {
 				if(cant > 0)
 					msg += "\n";
 				msg += "Falta sacar foto";
 			}
 			if(!llenoUbicacion) {
 				if(cant > 0)
 					msg += "\n";
 				msg += "Falta llenar ubicacion";
 			}				
 			Toast.makeText(this.getActivity(), msg,
 					Toast.LENGTH_SHORT).show();			
 		}
 
 	}
 
 	private static String convertInputStreamToString(InputStream inputStream)
 			throws IOException {
 		BufferedReader bufferedReader = new BufferedReader(
 				new InputStreamReader(inputStream));
 		String line = "";
 		String result = "";
 		while ((line = bufferedReader.readLine()) != null)
 			result += line;
 
 		inputStream.close();
 		return result;
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
 			sacoFoto = true;
 			ImageView imageView = (ImageView) getView()
 					.findViewById(R.id.vFoto);
 			// Check if the result includes a thumbnail Bitmap
 			if (data != null) { // no me acuerdo por que esta este if
 				if (data.hasExtra("data")) {
 					Bitmap thumbnail = data.getParcelableExtra("data");
 					imageView.setImageBitmap(thumbnail);
 				}
 				Log.d("MiMascota", "data distinto null");
 			} else {
 				// If there is no thumbnail image data, the image
 				// will have been stored in the target output URI.
 				// Resize the full image to fit in out image view.
 				BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
 				factoryOptions.inJustDecodeBounds = true;
 				BitmapFactory.decodeFile(outputFileUri.getPath(),
 						factoryOptions);
 				int imageWidth = factoryOptions.outWidth;
 				int imageHeight = factoryOptions.outHeight;
 				// Determine how much to scale down the image
 				int scaleFactor = Math.min(imageWidth / Constantes.viewerWidth, imageHeight
 						/ Constantes.viewerHeight);
 				// Decode the image file into a Bitmap sized to fill the View
 				factoryOptions.inJustDecodeBounds = false;
 				factoryOptions.inSampleSize = scaleFactor;
 				factoryOptions.inPurgeable = true;
 				Bitmap bitmap = BitmapFactory.decodeFile(
 						outputFileUri.getPath(), factoryOptions);
				///////////////////////////
				String filename = "pippo.jpg";
//				File sd = Environment.getExternalStorageDirectory();
				File dest = new File(outputFileUri.getPath());

//				Bitmap bitmap = (Bitmap)data.getExtras().get("data");
				try {
				     FileOutputStream out = new FileOutputStream(dest);
				     bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				     out.flush();
				     out.close();
				} catch (Exception e) {
				     e.printStackTrace();
				}
				//////////////////////////
 				imageView.setImageBitmap(bitmap);
 				Log.d("MiMascota", "imagen entera cargada");				
 			}
 		}
 		if (requestCode == LLENAR_FORMULARIO
 				&& resultCode == Activity.RESULT_OK) {
 			llenoFormulario = true;
 			try {
 				if (this.datosPerro == null) {
 					this.datosPerro = new JSONObject();
 				}
 
 				String jsonString = data.getExtras().getString("json");
 				JSONObject jsonAux = new JSONObject(jsonString);
 				Iterator<String> it = jsonAux.keys();
 
 				while (it.hasNext()) {
 					String clave = it.next();
 					this.datosPerro.put(clave, jsonAux.get(clave));
 				}
 
 				// FIXME harcodeo un par de datos que no estan en el formulario
 				this.datosPerro.put("age", "20");
 				this.datosPerro.put("gmaps", "true");
 
 				Log.d("MiMascota", jsonString);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		if (requestCode == LLENAR_UBICACION && resultCode == Activity.RESULT_OK) {
 			llenoUbicacion = true;
 			double latitude = data.getExtras().getDouble("lat");
 			double longitude = data.getExtras().getDouble("lng");
 
 			if (this.datosPerro == null) {
 				this.datosPerro = new JSONObject();
 			}
 
 			try {
 				this.datosPerro.put("latitude", latitude);
 				this.datosPerro.put("longitude", longitude);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			Toast.makeText(this.getActivity(),
 					"Lat: " + latitude + "\n" + "Lng: " + longitude,
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 }
