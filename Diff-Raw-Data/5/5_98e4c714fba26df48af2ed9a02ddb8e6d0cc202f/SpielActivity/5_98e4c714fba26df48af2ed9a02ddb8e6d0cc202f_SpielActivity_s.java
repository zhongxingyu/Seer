 package com.dhbw.dvst.activities;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.dhbw.dvst.R;
 import com.dhbw.dvst.adapters.SpielbrettAdapter;
 import com.dhbw.dvst.models.Spiel;
 import com.dhbw.dvst.models.Spielplatte;
 import com.dhbw.dvst.utilities.ActivityInteraction;
 import com.dhbw.dvst.utilities.SpielDialog;
 import com.dhbw.dvst.views.SpielView;
 
 public class SpielActivity extends Activity{
 	private ActivityInteraction kommunikation = new ActivityInteraction();
 	private SpielView view;
 	private SpielbrettAdapter brettAdapter;
 
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
 		ArrayList<Spielplatte> spielfeldArray = new ArrayList<Spielplatte>();
 		spielfeldArray.addAll(Spiel.getInstance().getSpielbrett().getAlleSpielplatten());
 		brettAdapter = new SpielbrettAdapter(this, R.layout.zeilenansicht, R.id.tv_gewaehlter_name, spielfeldArray);
         grid_spielbrett.setAdapter(brettAdapter);
 	}
 	
 	protected void setBildAktivePlatte(){
 		final ImageView motiv = (ImageView)findViewById(R.id.img_aktive_platte);		
 		int resID = getResources().getIdentifier(Spiel.getInstance().getSpielbrett().getAktivePlatte().getMotivURL(), "drawable", "com.dhbw.dvst");
 		motiv.setImageResource(resID);
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
			Spielplatte aktiv = Spiel.getInstance().getSpielbrett().getAlleSpielplatten().get(49);
 			aktiv.dreheSpielplatteNachLinks();
 			img_aktive_platte.setRotation(aktiv.getAusrichtung().getRotation());
 		}
 
 		@Override
 		public void onNachRechtsDrehen(ImageView img_aktive_platte) {
			Spielplatte aktiv = Spiel.getInstance().getSpielbrett().getAlleSpielplatten().get(49);
 			aktiv.dreheSpielplatteNachRechts();
 			img_aktive_platte.setRotation(aktiv.getAusrichtung().getRotation());
 		}
 
 		@Override
 		public void onSpielplatteAnklicken(int position) {
 			Spielplatte angeklicktePlatte = Spiel.getInstance().getSpielbrett().getAlleSpielplatten().get(position);
 			Spiel.getInstance().getSpielbrett().spielplatteEinschieben(angeklicktePlatte);
 			
 			//TODO: set clickable im Layout
 			brettAdapter.notifyDataSetChanged();
 		}
 	};
 }
