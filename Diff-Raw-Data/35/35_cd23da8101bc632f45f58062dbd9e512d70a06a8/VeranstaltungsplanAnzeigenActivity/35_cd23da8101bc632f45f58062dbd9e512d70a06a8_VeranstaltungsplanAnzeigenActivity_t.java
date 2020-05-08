 package com.example.haw_app;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.example.haw_app.veranstaltungsplan.implementations.Termin;
 import com.example.haw_app.veranstaltungsplan.implementations.Veranstaltungsplan;
 
 public class VeranstaltungsplanAnzeigenActivity extends Activity {
 	Veranstaltungsplan vp = VeranstaltungsplanActivity.getVp();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		vpAnzeigenLayout();
 		if (vp.termine.size() == 0) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 			builder.setMessage(
 					"Veranstaltungsplan.txt wurde nicht gefunden. Probiere bitte erst zu aktualisieren!")
 					.setNeutralButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 								}
 							});
 			AlertDialog alertDialog = builder.create();
 			alertDialog.show();
 		} else {
 		List<String> termine = new ArrayList<String>();
 		for (Termin termin : vp.termine) {
 			if (!termine.contains(termin.semesterGruppe())) {
 				termine.add(termin.semesterGruppe());
 			}
 		}
 		setSemesterGrpListView(termine);
 		}
 	}
 
 	private void vpAnzeigenLayout() {
 		setContentView(R.layout.vp_anzeigen);
 	}
 
 	private void setSemesterGrpListView(List<String> arrayAdapterList) {
 
 		final ListView lv = (ListView) findViewById(R.id.veranstaltungsplan_listview);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.vp_simple_list_item, arrayAdapterList);
 
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				String semestergrp = (String) lv.getItemAtPosition(position);
 				setFachListView(semestergrp);
 			}
 
 		});
 	}
 	
 	private void setFachListView(String semestergrp) {
 		List<Termin> termine = new ArrayList<Termin>();
 		for (Termin termin : vp.termine) {
 			if (termin.semesterGruppe().equals(semestergrp)) {
 				Boolean containsName = false;
 				for (Termin termin2 : termine) {
 					if (termin2.name().equals(termin.name())) {
 						containsName = true;
 					}
 				}
 				if (!containsName) {
 					termine.add(termin);
 				}
 				
 			}
 		}
 		final ListView lv = (ListView) findViewById(R.id.veranstaltungsplan_listview);
 		TerminAdapter adapter = new TerminAdapter(this,
 				R.layout.vp_simple_list_item, termine);
 
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Intent intent = new Intent();
 				intent.setClassName(getPackageName(), getPackageName()
 						+ ".VeranstaltungsplanTerminActivity");
 				Termin termin = (Termin) lv.getItemAtPosition(position);
 				String terminString = "Name: " + termin.name() + "\nDozent: "
 						+ termin.prof() + "\nRaum: " + termin.raum()
 						+ "\nSemester: " + termin.semesterGruppe();
 				List<String> zeiten = new ArrayList<String>();
 				for (Termin termin2 : vp.termine) {
 					if (termin2.name().equals(termin.name())) {
						zeiten.add("\n\nAm: "+ termin2.von().toString("dd.MM.yyyy") + "\nVon: "
								+ termin2.von().toString("HH:mm:ss") + "\nBis: "
								+ termin2.bis().toString("HH:mm:ss"));		
 					}
 				}
 				Collections.sort(zeiten);
 				for (String zeit : zeiten){
 					terminString = terminString + zeit;
 				}
 				if (termin != null) {
 					intent.putExtra("selected", terminString);
 					startActivity(intent);
 				}
 
 			}
 		});
 
 	}
 
 	public class TerminAdapter extends ArrayAdapter<Termin> {
 
 		public TerminAdapter(Context context, int textViewResourceId) {
 			super(context, textViewResourceId);
 		}
 
 		private List<Termin> termine;
 
 		public TerminAdapter(Context context, int resource, List<Termin> termine) {
 
 			super(context, resource, termine);
 
 			this.termine = termine;
 
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			View v = convertView;
 
 			if (v == null) {
 
 				LayoutInflater vi;
 				vi = LayoutInflater.from(getContext());
 				v = vi.inflate(R.layout.vp_listitem, null);
 
 			}
 
 			Termin termin = termine.get(position);
 
 			if (termin != null) {
 
 				TextView tt = (TextView) v.findViewById(R.id.item);
 
 				if (tt != null) {
 					tt.setText(termin.name());
 				}
 			}
 
 			return v;
 
 		}
 	}
 
 }
