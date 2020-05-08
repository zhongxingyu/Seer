 package com.digitalnatura.dialogmanager;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 //import javax.xml.transform.OutputKeys;
 //import javax.xml.transform.Result;
 //import javax.xml.transform.Source;
 //import javax.xml.transform.Transformer;
 //import javax.xml.transform.TransformerFactory;
 //import javax.xml.transform.dom.DOMSource;
 //import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlSerializer;
 
 import com.digitalnatura.R;
 import com.digitalnatura.dialogmanager.EditorDeDialogo.InitTask;
 import com.digitalnatura.escaletamanager.BbddHelper;
 import com.digitalnatura.helpers.guardarCambios;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.util.Xml;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class nuevoDialogo extends Activity implements View.OnClickListener {
 	private static String LOGTAG;
 	private String tituloGuion;
 	private String no_sec;
 	private Button speakButton;
 	protected String ocabia;
 	private String idGuion;
 	private String idSecu;
 	private BbddHelper helper;
 	private CursorFactory factory;
 	private AutoCompleteTextView editper;
 	private String perindex="";
 	private AutoCompleteTextView editaco;
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dialog_editor);
 		SharedPreferences settings = getSharedPreferences("titulo",
 				MODE_PRIVATE);
 		tituloGuion = settings.getString("titulo", "oquesexa");
 		SharedPreferences getidGuion = getApplicationContext()
 				.getSharedPreferences("_id", 0);
 		idGuion = getidGuion.getString("_id", "001");
 		SharedPreferences getIdSecu = getApplicationContext()
 				.getSharedPreferences("idsecu", 0);
 		idSecu = getIdSecu.getString("idsecu", "oquesexa");
 		helper = new BbddHelper(this, BbddHelper.DATABASE_NAME, factory, 1);
 		if (personaxesIndexados()) {
 			InitTask initTask = new InitTask();
 			initTask.execute(nuevoDialogo.this);
 
 		}
 
 		this.setTitle(tituloGuion.replace("_", " ").toUpperCase());
 		 editper = (AutoCompleteTextView) findViewById(R.id.editPer);
 		 
 		 ArrayList<String> mArrayList = new ArrayList<String>();
 			String[] countries = getResources().getStringArray(
 					R.array.countries_array);
 
 			SQLiteDatabase rowid = helper.getReadableDatabase();
 			Cursor mCursor = rowid
 					.rawQuery(
 							"SELECT DISTINCT personajes.personaje FROM personajes,apariciones WHERE personajes.id=apariciones.idpersonaje and idguion="
 									+ idGuion+" ORDER BY apariciones.rowid", null);
 			mCursor.moveToFirst();
 			while (!mCursor.isAfterLast()) {
 				mArrayList.add(mCursor.getString(mCursor
 						.getColumnIndex("personaje")));
 				mCursor.moveToNext();
 			}
 			
 			 Log.i(nuevoDialogo.LOGTAG,"algo "+ mArrayList);
 
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_dropdown_item_1line, mArrayList);
 			editper.setThreshold(0);
 
 			

			editper.setAdapter(adapter);
 			if(mArrayList.size()>1){
 				editper.setText(mArrayList.get(mArrayList.size()-2));
 			}
 		
 			
 			mCursor.close();
 			
 			
 			
 			 editaco = (AutoCompleteTextView) findViewById(R.id.editAco);
 			
 			
 			ArrayList<String> arrayacos = new ArrayList<String>();
 			SQLiteDatabase acos = helper.getReadableDatabase();
 			Cursor curAcos = acos
 					.rawQuery(
 							"SELECT * from acotaciones"
 									, null);
 			curAcos.moveToFirst();
 			while (!curAcos.isAfterLast()) {
 				arrayacos.add(curAcos.getString(curAcos
 						.getColumnIndex("acotaciones")));
 				curAcos.moveToNext();
 			}
 			ArrayAdapter<String> adaptor = new ArrayAdapter<String>(this,
 					android.R.layout.simple_dropdown_item_1line, arrayacos);
 			editaco.setThreshold(2);
 			
 			editaco.setAdapter(adaptor);
 			
 			
 			curAcos.close();
 			acos.close();
 			
 		
 
 		 
 		 
 		 
 		 
 		SharedPreferences asdf = getSharedPreferences("no_sec", MODE_PRIVATE);
 		no_sec = asdf.getString("no_sec", "oquesexa");
 
 		TextView mResultado = (TextView) findViewById(R.id.textView4);
 		mResultado.setText(getText(R.string.secuencia_abrev) + " " + no_sec
 				+ " " + getText(R.string.dial_new_dial));
 
 		speakButton = (Button) findViewById(R.id.listen);
 		// Drawable drawable =
 		// Resources.getSystem().getDrawable(android.R.drawable.ic_btn_speak_now);
 		// speakButton.setBackgroundDrawable(drawable);
 
 		// mList = (ListView) findViewById(R.id.list);
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() != 0) {
 			speakButton.setOnClickListener(this);
 			speakButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// if (v.getId() == R.id.speak) {
 					EditText editacc = (EditText) findViewById(R.id.editDia);
 					ocabia = editacc.getText().toString();
 					startVoiceRecognitionActivity();
 
 				}
 			});
 
 		} else {
 			// speakButton.setEnabled(false);
 			speakButton.setText(R.string.sinvoz);
 			// speakButton.setEms(R.string.sinvoz);
 			speakButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					Intent browse = new Intent(
 							Intent.ACTION_VIEW,
 							Uri.parse("https://market.android.com/details?id=com.google.android.voicesearch.x&feature=search_result"));
 
 					startActivity(browse);
 
 					// finish();
 				}
 			});
 
 		}
 
 		final Button button = (Button) findViewById(R.id.ok);
 
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				try {
 
 					int nosec = Integer.parseInt(no_sec) - 1;
 
 					File destinationFile = new File(Environment
 							.getExternalStorageDirectory()
 							+ "/myScreenplays/scripts/" + tituloGuion);
 					DocumentBuilderFactory fact = DocumentBuilderFactory
 							.newInstance();
 					DocumentBuilder builder = fact.newDocumentBuilder();
 					Document doc = builder.parse(destinationFile);
 
 					// doc.getDocumentElement().normalize();
 					NodeList listula = doc.getElementsByTagName("sec");
 					Node nodulo = listula.item(nosec);
 
 					String acotacion = null;
 					String oquescribiru = editaco.getText().toString();
 
 					
 					String nomePersonaxe = editper.getText().toString().toUpperCase();
 					String[] personajes={nomePersonaxe};
 					helper.insertarPersoeiros(personajes, null, idGuion);
 					
 					
 					
 					if (oquescribiru.equals("")) {
 						acotacion = "";
 					} else {
 						acotacion = "(" + oquescribiru + ")\n";
 						helper.insertarAcotacion(oquescribiru);
 						
 					}
 					EditText editdia = (EditText) findViewById(R.id.editDia);
 					String dialogo = editdia.getText().toString();
 
 					// Element nameNode = doc.createElement("dia");
 					// ((Element) nodulo).appendChild(nameNode);
 
 					boolean gun = nodulo.getLastChild().getPreviousSibling() != null;
 
 					if (gun) {
 						Node numeralos = nodulo.getLastChild()
 								.getPreviousSibling();
 						int numeracion = Integer.parseInt(((Element) numeralos)
 								.getAttribute("n")) + 1;
 
 						Element child = doc.createElement("dia");
 						child.setAttribute("personaje", nomePersonaxe+"\n");
 						child.setAttribute("acotacion", acotacion);
 						child.setAttribute("dialogo", dialogo);
 						child.setAttribute("n", numeracion + "");
 
 						((Element) nodulo).appendChild(child);
 
 					} else {
 
 						Element child = doc.createElement("dia");
 						child.setAttribute("personaje", nomePersonaxe+"\n");
 						child.setAttribute("acotacion", acotacion);
 						child.setAttribute("dialogo", dialogo);
 						child.setAttribute("n", "0");
 
 						((Element) nodulo).appendChild(child);
 
 					}
 
 					SharedPreferences paraofondo = getSharedPreferences(
 							"paraofondo", MODE_PRIVATE);
 					SharedPreferences.Editor editor = paraofondo.edit();
 					editor.putBoolean("paraofondo", true);
 					editor.commit();
 
 					guardarCambios.guardarCambios(doc, tituloGuion);
 
 				} catch (Exception e) {
 					System.out.println("XML Pasing Excpetion = " + e);
 				}
 
 				finish();
 			}
 		});
 
 		final Button boton = (Button) findViewById(R.id.del);
 		boton.setText(R.string.cancel);
 		boton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				finish();
 			}
 		});
 
 		// speakButton.setOnClickListener(new OnClickListener() {
 		//
 		// @Override
 		// public void onClick(View v) {
 		// // TODO Auto-generated method stub
 		// // if (v.getId() == R.id.speak) {
 		// EditText editacc = (EditText) findViewById(R.id.editDia);
 		// ocabia = editacc.getText().toString();
 		// startVoiceRecognitionActivity();
 		// // }
 		//
 		// }
 		//
 		// private void startVoiceRecognitionActivity() {
 		// // TODO Auto-generated method stub
 		// Intent intent = new Intent(
 		// RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 		// RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
 		// "myScreenplay Voice input");
 		// startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 		//
 		// }
 		// });
 
 	}
 	
 	private boolean personaxesIndexados() {
 
 		SQLiteDatabase rowid = helper.getReadableDatabase();
 
 		try {
 			 perindex = DatabaseUtils.stringForQuery(rowid,
 					"select defecto from objetivo where idguion=" + idGuion,
 					null);
 			if (perindex.contains("2")) {
 				rowid.close();
 				return false;
 			} else {
 				rowid.close();
 				return true;
 			}
 
 		} catch (Exception e) {
 			rowid.close();
 			return true;
 		}
 	}
 
 	protected void startVoiceRecognitionActivity() {
 		// TODO Auto-generated method stub
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
 				"myScreenplay Voice input");
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
 				&& resultCode == RESULT_OK) {
 			// Fill the list view with the strings the recognizer thought it
 			// could have heard
 			ArrayList<String> matches = data
 					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			// mList.setAdapter(new ArrayAdapter<String>(this,
 			// android.R.layout.simple_list_item_1,matches));
 			EditText editacc = (EditText) findViewById(R.id.editDia);
 
 			String accion = matches.get(0);
 			editacc.setText(ocabia + " " + accion);
 			int position = editacc.length();
 			editacc.setSelection(position);
 
 			// editacc.setFreezesText(true);
 			// editacc.setText(accion);
 
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		// saveState();
 		// if (nullsec==null){
 		// com.digitalnatura.sequencemanager.Preview.recargar();}
 		// else{
 		Preview.recargar();
 		// }
 	}
 	
 	
 	protected class InitTask extends AsyncTask<Context, Integer, String> {
 
 		@Override
 		protected String doInBackground(Context... arg0) {
 
 			RecollerPersonaxes getper = new RecollerPersonaxes();
 
 			String[] personajes = null;
 			try {
 				personajes = getper.parsearPersonaxes(tituloGuion);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (XmlPullParserException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			helper.insertarOuActualizarTitulo(idGuion, tituloGuion, "");
 			// helper.insertarSecuencia("1", 1, "", "", "", "", "", "1");
 
 			helper.insertarPersoeiros(personajes, null, idGuion);
 
 			SQLiteDatabase rowid = helper.getWritableDatabase();
 			ContentValues values = new ContentValues();
 
 			// editobx.getText();
 			values.put("titulo", tituloGuion);
 			values.put("idguion", idGuion);
 			values.put("objetivo", 120);
 			values.put("defecto", perindex+2);
 
 			try {
 				rowid.insertOrThrow("objetivo", null, values);
 				rowid.close();
 			} catch (SQLiteConstraintException e) {
 
 				values.put("defecto", perindex+2);
 				String[] whereArgs = { idGuion };
 				rowid.update("objetivo", values, "idguion=?", whereArgs);
 				rowid.close();
 
 			}
 
 			Log.i(EditorDeDialogo.LOGTAG, "non ta indexado ");
 
 			return null;
 		}
 		
 	}
 }
