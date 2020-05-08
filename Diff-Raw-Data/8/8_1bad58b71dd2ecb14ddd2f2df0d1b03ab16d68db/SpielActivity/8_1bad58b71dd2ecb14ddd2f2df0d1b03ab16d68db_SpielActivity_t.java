 package com.dhbw.dvst.activities;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.dhbw.dvst.R;
 import com.dhbw.dvst.adapters.SpielbrettAdapter;
 import com.dhbw.dvst.models.Sehenswuerdigkeit;
 import com.dhbw.dvst.models.Spiel;
 import com.dhbw.dvst.models.Spielplatte;
 import com.dhbw.dvst.utilities.ActivityInteraction;
 import com.dhbw.dvst.utilities.SpielDialog;
 import com.dhbw.dvst.views.SpielView;
 
 public class SpielActivity extends Activity{
 	private ActivityInteraction kommunikation = new ActivityInteraction();
 	private SpielView view;
 	private SpielbrettAdapter brettAdapter;
 	private Spiel spiel = Spiel.getInstance();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		view = (SpielView)View.inflate(this, R.layout.spielbildschirm, null);
 		view.setViewListener(viewListener);
 		setContentView(view);
 		setSpielbrettAdapter();
 		setBildAktivePlatte();
 	}
 
 	protected void setSpielbrettAdapter() {
 		final GridView grid_spielbrett = (GridView)findViewById(R.id.grid_spielbrett);
 		brettAdapter = new SpielbrettAdapter(this, R.layout.zeilenansicht, R.id.tv_gewaehlter_name, 
 				spiel.getSpielbrett().getAlleSpielplatten());
         grid_spielbrett.setAdapter(brettAdapter);
 	}
 	
 	protected void setBildAktivePlatte(){
 		final ImageView platte = (ImageView)findViewById(R.id.img_aktive_platte);		
 		int resID = getResources().getIdentifier(spiel.getSpielbrett().getAktivePlatte().getMotivURL(), "drawable", "com.dhbw.dvst");
 		platte.setImageResource(resID);
 		platte.setRotation(spiel.getSpielbrett().getAktivePlatte().getAusrichtung().getRotation());
 		
 		Sehenswuerdigkeit ziel = spiel.getSpielbrett().getAktivePlatte().getZiel();
		final ImageView sehenswuerdigkeit = (ImageView)findViewById(R.id.img_sehenswuerdigkeit);
		if(ziel != null){			
 			resID = getResources().getIdentifier(ziel.getMotivURL(), "drawable", "com.dhbw.dvst");
 			sehenswuerdigkeit.setImageResource(resID);
 		}
		else{
			sehenswuerdigkeit.setImageResource(android.R.color.transparent);
		}
 		
 	}
 	
 	private SpielView.ViewListener viewListener = new SpielView.ViewListener() {
 		
 		@Override
 		public void onBeenden() {
 			OnClickListener positiv_listener = new OnClickListener() {				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Spiel.resetInstance();
 					kommunikation.navigieren(SpielActivity.this, ModusActivity.class);					
 				}
 			};
 			new SpielDialog(SpielActivity.this, getString(R.string.err_beenden), positiv_listener);			
 		}
 
 		@Override
 		public void onReadManual() {
 			kommunikation.navigieren(SpielActivity.this, AnleitungActivity.class, true);
 		}
 
 		@Override
 		public void onNachLinksDrehen(ImageView img_aktive_platte) {
 			Spielplatte aktiv = spiel.getSpielbrett().getAktivePlatte();
 			aktiv.dreheSpielplatteNachLinks();
 			img_aktive_platte.setRotation(aktiv.getAusrichtung().getRotation());
 		}
 
 		@Override
 		public void onNachRechtsDrehen(ImageView img_aktive_platte) {
 			Spielplatte aktiv = spiel.getSpielbrett().getAktivePlatte();
 			aktiv.dreheSpielplatteNachRechts();
 			img_aktive_platte.setRotation(aktiv.getAusrichtung().getRotation());
 		}
 
 		@Override
 		public void onSpielplatteAnklicken(int position, GridView spielbrett) {
 			Spielplatte angeklicktePlatte = spiel.getSpielbrett().getAlleSpielplatten().get(position);
 			spiel.getSpielbrett().spielplatteEinschieben(angeklicktePlatte);
 			
 			//TODO: set clickable im Layout
 			brettAdapter.notifyDataSetChanged();
 			spielbrett.invalidateViews();
 			setBildAktivePlatte();
 		}
 	};
 }
