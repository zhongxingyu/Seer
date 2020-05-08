 package com.digitalnatura.dialogmanager;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.commonsware.cwac.tlv.TouchListView;
 import com.digitalnatura.filemanager.MyScreenplaysActivity;
 import com.digitalnatura.R;
 import com.digitalnatura.helpers.ClaseSecuencia;
 import com.digitalnatura.helpers.DomEngine;
 import com.digitalnatura.helpers.guardarCambios;
 import com.digitalnatura.helpers.servGenerarPrevParcial;
 import com.digitalnatura.helpers.servGenerarPrevTot;
 
 public class Dialogos extends ListFragment {
 	static String LOGTAG;
 	boolean mDualPane;
 	int mCurCheckPosition = 0;
 	private ArrayList<ClaseSecuencia> aSecuencias;
 	private int elementoelexido;
 	private LayoutInflater layoutinflater;
 	private IconicAdapter adapter;
 	int scrollpos;
 
 	private int pos;
 
 	public static Dialogos newInstance() {
 		Dialogos fragment = new Dialogos();
 
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		layoutinflater = inflater;
 
 		// TouchListView tlv=(TouchListView)getListView();
 		// adapter=new IconicAdapter();
 		// setListAdapter(adapter);
 		//
 
 		return inflater.inflate(R.layout.main3, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		iniciamosListaDeElementos();
 
 	}
 
 	private void iniciamosListaDeElementos() {
 
 		// TODO Auto-generated method stub
 		try {
 			obterListaDeSecuencias();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// setContentView(R..main);
 
 		ListView lv = getListView();
 
 		boolean paraofondo = false;
		SharedPreferences settings = DialogosActivity.getContext()
 				.getSharedPreferences("paraofondo", 0);
 		paraofondo = settings.getBoolean("paraofondo", false);
 		if (paraofondo) {
 
 			lv.setSelection(lv.getCount() - 1);
 			// SharedPreferences.Editor editor = settings.edit();
 			// editor.putBoolean("paraofondo", false);
 			// editor.commit();
 		} else {
 			lv.setSelectionFromTop(scrollpos, pos);
 
 		}
 
 	}
 
 	private void obterListaDeSecuencias() throws XmlPullParserException,
 			FileNotFoundException {
 
 		// TODO Auto-generated method stub
 
 		// Este array e o que temos que sacar do sample.xml ou no seu defecto do
 		// correspondente guion
 
 		if (cargaListaDeSecuencias()) {
 
 			// Populate list with our static array of titles.
 			// setListAdapter(new ArrayAdapter<String>(getActivity(),
 			// com.digitalnatura.filemanager.R.layout.maineditor,
 			// obtenerElementosDeSecuencia()));
 			// registerForContextMenu(getListView());
 			// setHasOptionsMenu(true);
 
 			TouchListView tlv = (TouchListView) getListView();
 			adapter = new IconicAdapter();
 			setListAdapter(adapter);
 			registerForContextMenu(tlv);
 			setHasOptionsMenu(true);
 
 			tlv.setDropListener(onDrop);
 			tlv.setRemoveListener(onRemove);
 
 		}
 	}
 
 	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
 		@Override
 		public void drop(int from, int to) {
 			String item = adapter.getItem(from);
 
 			adapter.remove(item);
 			adapter.insert(item, to);
 
 			Log.i(CopyOfTestFragment.LOGTAG, "algo " + from + " " + to);
 			moverElemento(from, to);
 
 		}
 	};
 
 	private TouchListView.RemoveListener onRemove = new TouchListView.RemoveListener() {
 		@Override
 		public void remove(int which) {
 			adapter.remove(adapter.getItem(which));
 		}
 	};
 
 	protected void moverElemento(int from, int to) {
 		SharedPreferences settings = DialogosActivity.getContext()
 				.getSharedPreferences("titulo", 0);
 		String tituloGuion = settings.getString("titulo", "oquesexa");
 
 		SharedPreferences asdf = DialogosActivity.getContext()
 				.getSharedPreferences("no_sec", 0);
 		String no_sec = asdf.getString("no_sec", "oquesexa");
 
 		try {
 			File destinationFile = new File(
 					Environment.getExternalStorageDirectory()
 							+ "/myScreenplays/scripts/" + tituloGuion);
 			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = fact.newDocumentBuilder();
 			Document doc = builder.parse(destinationFile);
 			
 			NodeList listula = doc.getElementsByTagName("sec");
 			int numDeSec = Integer.parseInt(no_sec) - 1;
 			int numDeEle = from * 2 + 1;
 			int numEleDest = to * 2 + 1;
 
 			Node nodulo = listula.item(numDeSec);
 			NodeList childlist = nodulo.getChildNodes();
 			Node child = childlist.item(numDeEle);
 			Node dest = childlist.item(numEleDest);
 
 			((Element) child).getParentNode().insertBefore(child, dest);
 			
 //			doc.getDocumentElement().normalize();
 			
 //			Node Novonodulo = listula.item(numDeSec);
 //			
 //			Node Novonodulo = listula.item(numDeSec);
 			
 //			NodeList Novochildlist = Novonodulo.getChildNodes();
 			
 			Node no = childlist.item(0);
 			
 			int a = 0;
 			int fucka = childlist.getLength();
 			
 			for (int i = 0; i < fucka; i++) {  	
 
 				
 				
 				if (no.hasAttributes()){
 					((Element) no).setAttribute("n", a+"");
 					a= a+1;
 					
 				}
 				
 				 no = no.getNextSibling();
 				 
 			
 				
 	    	 }
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 			
 
 			guardarCambios.guardarCambios(doc, tituloGuion);
 
 			Intent serviceDom = new Intent(DialogosActivity.getContext(),
 					servGenerarPrevParcial.class);
 			DialogosActivity.getContext().startService(serviceDom);
 
 			Preview.recargar();
 
 			iniciamosListaDeElementos();
 
 		} catch (Exception e) {
 			System.out.println("XML Pasing Excpetion = " + e);
 		}
 
 	}
 
 	private boolean cargaListaDeSecuencias() throws XmlPullParserException,
 			FileNotFoundException {
 		aSecuencias = new ArrayList<ClaseSecuencia>();
 
 		boolean bReturn = true; // Inicializamos valor de retorno
 		// XmlPullParser parser = getResources().getXml(R.xml.entradas); //
 		// Obtenemos recurso xml
 		// XmlPullParser parser = getResources().getXml(R.xml.sample); //
 		// Obtenemos recurso xml
 
 		// collemos os datos da activity secuencias
 
 		// Bundle extras = getIntent().getExtras();
 		// tituloPulsado = extras.getString(Secuencias.nombramosGuion);
 		// no_sec = extras.getString(Secuencias.no_sec);
 		SharedPreferences settings = DialogosActivity.getContext()
 				.getSharedPreferences("titulo", 0);
 		String tituloPulsado = settings.getString("titulo", "oquesexa");
 
 		SharedPreferences asdf = DialogosActivity.getContext()
 				.getSharedPreferences("no_sec", 0);
 		String no_sec = asdf.getString("no_sec", "oquesexa");
 
 		// Toast.makeText(EditorDeSecuencias.this, "Creado Nuevo Guion \n" +
 		// no_sec, Toast.LENGTH_SHORT).show();
 
 		File destinationFile = new File(
 				Environment.getExternalStorageDirectory()
 						+ "/myScreenplays/scripts/" + tituloPulsado);
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		XmlPullParser parser = factory.newPullParser();
 		parser.setInput(new FileReader(destinationFile));
 		// List<String> estructuraSecuencia = new ArrayList<String> ();
 
 		try { // Parseamos XML
 			while (parser.next() != XmlPullParser.END_DOCUMENT) {
 				String name = parser.getName();
 				String numero = null;
 				String lugar = null;
 
 				int eventType = parser.getEventType();
 				int numerodeAcciones = 0;
 
 				if (eventType == XmlPullParser.START_TAG && name.equals("sec")
 						&& parser.getAttributeName(0).equals("numero")
 						&& parser.getAttributeValue(0).equals(no_sec)) {
 
 					// ArrayList<HashMap<String,String>> estructuraSecuencia =
 					// new ArrayList<HashMap<String,String>>();
 					// List<HashMap<String, String>> estructuraSecuencia = new
 					// ArrayList<HashMap<String, String>>();
 					// HashMap<String,String> item = new
 					// HashMap<String,String>();
 
 					while (parser.nextTag() != XmlPullParser.END_TAG) {
 
 						name = parser.getName();
 
 						int size = 0;
 						if (eventType == XmlPullParser.START_TAG) {
 							size++;
 						}
 
 						for (int i = 0; i < size; i++) {
 
 							if (name.equals("acc")) {
 
 								String attrName = parser.getAttributeName(i);
 								String attrValue = parser.getAttributeValue(i);
 								numero = attrValue;
 							} else {
 
 								int size1 = 3;
 								String contenedorDialogo[];
 								contenedorDialogo = new String[size1];
 								StringBuilder arTostr = new StringBuilder();
 								for (int i1 = 0; i1 < size1; i1++) {
 
 									String attrName = parser
 											.getAttributeName(i1);
 									String attrValue = parser
 											.getAttributeValue(i1);
 
 									contenedorDialogo[i1] = attrValue;
 
 									arTostr.append(contenedorDialogo[i1]);
 
 									numero = arTostr.toString();
 
 									// // String attrDialogo = attrValue;
 									// numero = attrDialogo;
 									// numero = ""+contenedorDialogo;
 
 								}
 
 							}
 
 							aSecuencias
 									.add(new ClaseSecuencia(numero, lugar,
 											numero, lugar, numero, lugar,
 											numero, lugar));
 
 							// Log.i(LOGTAG, "index=" + aSecuencias );
 
 						}
 
 						parser.nextTag();
 
 						// item.put( "d", nome );
 						// estructuraSecuencia.add(item);
 
 					}
 				}
 
 				//
 			}
 
 		} catch (Exception e) { // Error
 			Log.e(Dialogos.LOGTAG, e.getMessage(), e);
 			bReturn = false;
 		}
 
 		return bReturn;
 	}
 
 	private ArrayList<String> obtenerElementosDeSecuencia() {
 		ArrayList<String> aListaSecuencias = new ArrayList<String>();
 
 		for (int i = 0; i < aSecuencias.size(); i++) {
 			aListaSecuencias.add(aSecuencias.get(i).getLugar());
 			//
 		}
 		// Collections.sort(aListaSecuencias );
 		return aListaSecuencias;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
 		outState.putInt("curChoice", mCurCheckPosition);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int pos, long id) {
 		// showDetails(pos);
 
 		// String no_sec = settings.getString("no_sec","oquesexa");
 		// SharedPreferences asdf =
 		// DialogosActivity.getContext().getSharedPreferences("no_sec", 0);
 		// String no_sec = asdf.getString("no_sec","oquesexa");
 
 		SharedPreferences settings = DialogosActivity.getContext()
 				.getSharedPreferences("elemento", 0);
 		SharedPreferences.Editor edit = settings.edit();
 		edit.putString("elemento", pos + "");
 		edit.commit();
 
 		SharedPreferences paraofondo = DialogosActivity.getContext()
 				.getSharedPreferences("paraofondo", 0);
 		SharedPreferences.Editor editor = paraofondo.edit();
 		editor.putBoolean("paraofondo", false);
 		editor.commit();
 
 		Intent serviceDom = new Intent(DialogosActivity.getContext(),
 				DomEngine.class);
 
 		// serviceDom.putExtra("tituloPulsado", tituloPulsado);
 		// serviceDom.putExtra("no_sec", no_sec);
 		// serviceDom.putExtra("elemento", position+"");
 		serviceDom.putExtra("headereditor", "0");
 
 		DialogosActivity.getContext().startService(serviceDom);
 
 		// Toast.makeText(DialogosActivity.getContext(), "You selected: "+ pos ,
 		// Toast.LENGTH_LONG).show();
 
 	}
 
 	private void showDetails(int pos) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 
 		MenuInflater inflater = ((Activity) MyScreenplaysActivity.getContext())
 				.getMenuInflater();
 		inflater.inflate(R.menu.context_menu_seq_editor, menu);
 		super.onCreateContextMenu(menu, v, menuInfo);
 		// menu.getItem(mCurCheckPosition);
 		// cambiar despois esto debe ser dinamico
 		menu.setHeaderTitle(R.string.selected_item);
 	}
 
 	// @Override
 	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 	// inflater.inflate(R.menu.menu_lista_secuencias, menu);
 	//
 	// super.onCreateOptionsMenu(menu, inflater);
 	// }
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		switch (item.getItemId()) {
 		// case R.id.guion_propiedades:
 		// mostrarPropiedadesDeGuion();
 		// return true;
 		// case R.id.guion_exportar:
 		// mostrarExportarEnviar();
 		// return true;
 		case R.id.elemento_eliminar:
 
 			ListView lv = getListView();
 			scrollpos = lv.getFirstVisiblePosition();
 			View vfirst = lv.getChildAt(0);
 			if (vfirst != null)
 				pos = vfirst.getTop();
 
 			SharedPreferences paraofondo = DialogosActivity.getContext()
 					.getSharedPreferences("paraofondo", 0);
 			SharedPreferences.Editor editor = paraofondo.edit();
 			editor.putBoolean("paraofondo", false);
 			editor.commit();
 
 			elementoelexido = info.position;
 			// Log.i(Secuencias.LOGTAG, ""+numerodesecelexida);
 			EliminarElemento();
 
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	private void EliminarElemento() {
 		// TODO Auto-generated method stub
 
 		SharedPreferences settings = DialogosActivity.getContext()
 				.getSharedPreferences("titulo", 0);
 		String tituloGuion = settings.getString("titulo", "oquesexa");
 
 		SharedPreferences asdf = DialogosActivity.getContext()
 				.getSharedPreferences("no_sec", 0);
 		String no_sec = asdf.getString("no_sec", "oquesexa");
 
 		try {
 
 			File destinationFile = new File(
 					Environment.getExternalStorageDirectory()
 							+ "/myScreenplays/scripts/" + tituloGuion);
 			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = fact.newDocumentBuilder();
 			Document doc = builder.parse(destinationFile);
 			doc.getDocumentElement().normalize();
 			NodeList listula = doc.getElementsByTagName("sec");
 			int numDeSec = Integer.parseInt(no_sec) - 1;
 			int numDeEle = elementoelexido * 2 + 1;
 			// recollemos os valores do cadro de dialogo
 			Node nodulo = listula.item(numDeSec);
 			NodeList childlist = nodulo.getChildNodes();
 			
 //			int cantidade = childlist.getLength();
 //			Log.i(Dialogos.LOGTAG, "antes"+cantidade);
 //			
 			
 			Node child = childlist.item(numDeEle);
 
 			((Element) child).getParentNode().removeChild(child);
 			
 			
 			Node Novonodulo = listula.item(numDeSec);
 			
 			NodeList Novochildlist = Novonodulo.getChildNodes();
 			
 			Node no = Novochildlist.item(0);
 			
 			
 			int fucka = Novochildlist.getLength();
 			int a = 0;
 			for (int i = 0; i < fucka; i++) {  	
 
 				
 				
 				if (no.hasAttributes()){
 					((Element) no).setAttribute("n", a+"");
 					a= a+1;
 					
 				}
 				
 				 no = no.getNextSibling();
 				 
 				
 				
 	    	 }
 			
 			
 			
 			
 			
 			
 //			Log.i(Dialogos.LOGTAG, "dempoix"+fucka);
 			
 			
 			
 			
 			
 			
 			
 			
 
 			guardarCambios.guardarCambios(doc, tituloGuion);
 
 			Intent serviceDom = new Intent(DialogosActivity.getContext(),
 					servGenerarPrevParcial.class);
 			DialogosActivity.getContext().startService(serviceDom);
 
 			Preview.recargar();
 
 			iniciamosListaDeElementos();
 
 		} catch (Exception e) {
 			System.out.println("XML Pasing Excpetion = " + e);
 		}
 
 	}
 
 	//
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// MenuInflater inflater = ((Activity)
 		// MyScreenplaysActivity.getContext()).getMenuInflater();
 		inflater.inflate(R.menu.menu_editor_secuencias, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 
 		// return true;
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.novaaccion:
 
 			iraEditorDeDescripcion();
 			return true;
 
 		case R.id.novodialogo:
 
 			iraEditorDeDialogo();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void iraEditorDeDialogo() {
 		// TODO Auto-generated method stub
 		Intent aboutActivity = new Intent(DialogosActivity.getContext(),
 				nuevoDialogo.class);
 		startActivity(aboutActivity);
 
 	}
 
 	private void iraEditorDeDescripcion() {
 		// TODO Auto-generated method stub
 		Intent aboutActivity = new Intent(DialogosActivity.getContext(),
 				nuevaAccion.class);
 		startActivity(aboutActivity);
 
 	}
 
 	//
 
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		Intent serviceDom = new Intent(DialogosActivity.getContext(),
 				servGenerarPrevParcial.class);
 		DialogosActivity.getContext().startService(serviceDom);
 
 		iniciamosListaDeElementos();
 
 	}
 
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		SharedPreferences paraofondo = DialogosActivity.getContext()
 				.getSharedPreferences("paraofondo", 0);
 		SharedPreferences.Editor editor = paraofondo.edit();
 		editor.putBoolean("paraofondo", false);
 		editor.commit();
 		ListView lv = getListView();
 		scrollpos = lv.getFirstVisiblePosition();
 		View vfirst = lv.getChildAt(0);
 		if (vfirst != null)
 			pos = vfirst.getTop();
 
 		Intent serviceDom = new Intent(DialogosActivity.getContext(),
 				servGenerarPrevTot.class);
 		DialogosActivity.getContext().startService(serviceDom);
 
 		// iniciamosListaDeElementos();
 
 	}
 
 	class IconicAdapter extends ArrayAdapter<String> {
 		IconicAdapter() {
 			super(DialogosActivity.getContext(), R.layout.row3,
 					obtenerElementosDeSecuencia());
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 
 				row = inflater.inflate(R.layout.row3, parent, false);
 			}
 
 			TextView label = (TextView) row.findViewById(R.id.label);
 
 			label.setText(obtenerElementosDeSecuencia().get(position));
 
 			return (row);
 		}
 
 		private LayoutInflater getLayoutInflater() {
 			// TODO Auto-generated method stub
 			return layoutinflater;
 		}
 	}
 
 }
