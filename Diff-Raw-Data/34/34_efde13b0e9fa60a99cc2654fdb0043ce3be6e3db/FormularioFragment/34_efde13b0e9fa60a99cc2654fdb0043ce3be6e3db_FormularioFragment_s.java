 package com.mimascota.mimascota;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class FormularioFragment extends Fragment {
 	private String json = "";
 	private EditText eNombre;
 	private EditText eColor;
 	private EditText eDescripcion;
 	private Spinner eRaza;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.fragment_formulario, container,
 				false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle state) {
 		super.onActivityCreated(state);
 		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		final Button buttonOk = (Button) getView().findViewById(R.id.bOkFormulario);
 		eNombre = (EditText) getView().findViewById(R.id.tNombre);
 		eColor = (EditText) getView().findViewById(R.id.tColor);
 		eDescripcion = (EditText) getView().findViewById(R.id.tDescripcion);
 		eRaza = (Spinner) getView().findViewById(R.id.tBreed);
 		
 		ArrayAdapter<CharSequence> adapterRaza = ArrayAdapter.createFromResource(
 				getActivity(), R.array.valores_razas,
 				android.R.layout.simple_spinner_item);
 		
 		adapterRaza.setDropDownViewResource(
 		        android.R.layout.simple_spinner_dropdown_item);
 		 
 		eRaza.setAdapter(adapterRaza);
 
 		buttonOk.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onClickOk(v);
 			}
 		});
 		
 	}
 
 	private void onClickOk(View v) {
 		String sNombre = eNombre.getText().toString();
 		String sColor = eColor.getText().toString();
 		String sDescripcion = eDescripcion.getText().toString();
		String sRaza = eRaza.toString();
 
 		JSONObject jsonObject = new JSONObject();
 		try {
 			jsonObject.put("name", sNombre);
 			jsonObject.put("color", sColor);
 			jsonObject.put("description", sDescripcion);
 			jsonObject.put("breed", sRaza);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		json = jsonObject.toString();
 
 		Intent i = new Intent();
 		i.putExtra("json", json);
 		super.getActivity().setResult(Activity.RESULT_OK, i);
 		super.getActivity().finish();
 	}
 
 }
