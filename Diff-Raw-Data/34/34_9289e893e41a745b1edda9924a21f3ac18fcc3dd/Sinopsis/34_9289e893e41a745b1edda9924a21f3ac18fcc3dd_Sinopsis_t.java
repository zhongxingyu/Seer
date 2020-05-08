 package com.digitalnatura.dialogmanager;
 
 //import org.taptwo.android.widget.viewflow.example.R;
 
 import com.digitalnatura.escaletamanager.BbddHelper;
 import com.digitalnatura.escaletamanager.EditorEscaleta;
 import com.digitalnatura.escaletamanager.ProvedorContidos;
 import com.digitalnatura.escaletamanager.ProvedorContidos.Secuencias;
 import com.digitalnatura.R;
 import com.digitalnatura.sequencemanager.TargetSelect;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.graphics.drawable.GradientDrawable;
 import android.graphics.drawable.GradientDrawable.Orientation;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public final class Sinopsis extends Fragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	private static String LOGTAG;
 
 	// ¿?¿ esta ID ou outro loader diftinto???
 	private static final int TUTORIAL_LIST_LOADER = 0x01;
 
 	private View containder;
 	private boolean existenaBbdd;
 	// private String nombramosGuion;
 	private String _id;
 	private SQLiteDatabase cagondios;
 	private CursorFactory factory;
 
 	// private BbddHelper database = new BbddHelper(Secuencias.getContext(),
 	// "escaleta.db", factory, 1);
 
 	// private static String LOGTAG;
 
 	private BbddHelper helper;
 	private ListView lv;
 	private SimpleCursorAdapter adapter;
 
 	public static Sinopsis newInstance() {
 		Sinopsis fragment = new Sinopsis();
 
 		return fragment;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		getLoaderManager().restartLoader(TUTORIAL_LIST_LOADER, null, this);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		helper = new BbddHelper(getActivity(), BbddHelper.DATABASE_NAME,
 				factory, 1);
 		SharedPreferences settings = getActivity().getApplicationContext()
 				.getSharedPreferences("_id", 0);
 		_id = settings.getString("_id", "oquesexa");
 
 		// getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);
 
 		existenaBbdd = existenaBbdd();
 
 		if (existenaBbdd) {
 
 			return lv;
 
 		} else {
 
 			containder = container;
 			return inflater.inflate(R.layout.helpfragment, null);
 		}
 
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		if (existenaBbdd) {
 
 			String[] columnas = new String[] { Secuencias.COL_ORDEN,
 					Secuencias.COL_ACCION, Secuencias.COL_INT_EXT,
 					Secuencias.COL_DIA_NOCHE, Secuencias.COL_LOCAL,
 					Secuencias.COL_PER };
 
 			int[] textonolayout = new int[] { R.id.orden, R.id.accion,
 					R.id.intext, R.id.dianoite, R.id.localizacion,
 					R.id.personaxes };
 
 			adapter = new SimpleCursorAdapter(getActivity()
 					.getApplicationContext(), R.layout.contentlayout, null,
 					columnas, textonolayout,
 					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 
 			int[] colors = { 0, 0xF3863000, 0 }; // red for the example
 			lv.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
 			lv.setDividerHeight(5);
 			// lv.setClickable(false);
 
 			lv.setAdapter(adapter);
 			lv.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					// TODO Auto-generated method stub
 
 					Intent i = new Intent(DialogosActivity.getContext(),
 							EditorEscaleta.class);
 
 					// Esta información se recuperará en el objeto Bundle de
 					// onCreate
 					// i.putExtra("urlnova", keyword);
 					// i.putExtra("titulo", titulo);
 					// startActivityForResult(i, RESULTADO_ESCALETAS);
 					//
 
 					startActivity(i);
 
 				}
 
 			});
 
 		} else {
 
 			final TextView aboutText = (TextView) containder
 					.findViewById(R.id.about);
 			aboutText.setMovementMethod(LinkMovementMethod.getInstance());
 
 			SharedPreferences settings = getActivity().getApplicationContext()
 					.getSharedPreferences("titulo", 0);
 			String tituloGuion = settings.getString("titulo", "oquesexa")
 					.replace("_", " ");
 
 			CharSequence styledText = Html.fromHtml(getString(
 					R.string.about_content2, getString(R.string.folla),
 					getString(R.string.formulario), tituloGuion));
 			aboutText.setText(styledText);
 		}
 
 	}
 
 	private boolean existenaBbdd() {
 
 		SQLiteDatabase rowid = helper.getReadableDatabase();
 
 		try {
 			lv = new ListView(getActivity().getApplicationContext());
 
 			if (DatabaseUtils.stringForQuery(rowid,
 					"select URL from escaleta where _id=" + _id, null).length() == 0) {
 				rowid.close();
 				return false;
 			} else {
 				rowid.close();
 				return true;
 			}
 
 		} catch (Exception e) {
 			rowid.close();
 			return false;
 		}
 
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		// TODO Auto-generated method stub
 		SharedPreferences settings = getActivity().getApplicationContext()
 				.getSharedPreferences("idsecu", 0);
 		String idsecu = settings.getString("idsecu", "oquesexa");
 
 		Uri secuenciasUri = Uri
 				.parse("content://com.digitalnatura.myscreenplays/secuencias/"
 						+ idsecu);
 
 		String[] projection = new String[] { Secuencias._ID,
 				Secuencias.COL_ORDEN, Secuencias.COL_INT_EXT,
 				Secuencias.COL_DIA_NOCHE, Secuencias.COL_ACCION,
 				Secuencias.COL_ESCALETA, Secuencias.COL_LOCAL,
 				Secuencias.COL_PER
 
 		};
 
 		CursorLoader cursorLoader = new CursorLoader(getActivity(),
 				secuenciasUri, projection, "escaleta=" + _id, null,
 				Secuencias.COL_ORDEN);
 
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		if (existenaBbdd) {
 			adapter.swapCursor(cursor);
 
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		// TODO Auto-generated method stub
 		if (existenaBbdd) {
 			adapter.swapCursor(null);
 		} else {
 			getLoaderManager().destroyLoader(TUTORIAL_LIST_LOADER);
 		}
 	}
 }
