 package com.domomtica.JarviseRemote;
 import java.util.concurrent.Semaphore;
 
 import com.domotica.JarviseRemote.R;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.ScaleAnimation;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class Status extends Fragment implements OnClickListener{
 	private static String nameFile = "jarvise.txt";
 	TextView def1;
 	TextView def2;
 	TextView def3;
 	TextView def4;
 	String default1;
 	String default2;
 	String default3;
 	String default4;
 	Button getStatus;
 	ToggleButton personalizzato1;
 	ToggleButton personalizzato2;
 	ToggleButton personalizzato3;
 	ToggleButton personalizzato4;
 	ToggleButton personalizzato5;
 	ToggleButton personalizzato6;
 	ToggleButton personalizzato7;
 	ToggleButton personalizzato8;
 	ToggleButton personalizzato9;
 	ToggleButton personalizzato10;
 	ToggleButton personalizzato11;
 	TextView errore;
 	static Activity thisActivity = null;
 	Configuration readFile;
 	int [] status;
 	View view;
 	Semaphore sem = new Semaphore(0, true);
 	MultiThread thread;
 	@SuppressWarnings("static-access")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.e("Controlli", "Tutti");
 		this.thisActivity = this.getActivity();
 		this.readFile = new Configuration(nameFile);
 		this.default1 = readFile.getDefault0(); 
 		this.default2 = readFile.getDefault1();
 		this.default3 = readFile.getDefault2();
 		this.default4 = readFile.getDefault3();
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		view = inflater.inflate(R.layout.status0, container, false);
 		//		TextView textView = (TextView) view.findViewById(R.id.infoStatus);
 		//		textView.setText("STATUS   GPIO");
 		getStatus = (Button) view.findViewById(R.id.getStatus);
 		getStatus.setOnClickListener(this);
 		return view;
 	}
 
 	public static void log(String log){
 		Toast.makeText(thisActivity, log, Toast.LENGTH_LONG).show();
 
 	}
 
 
 
 	public void checkButton(ToggleButton personale){
 		if(personale.isChecked())
 		{
 			personale.setBackgroundResource(R.layout.acceso);
 			personale.setText("Acceso");
 		}
 		else
 		{
 			personale.setBackgroundResource(R.layout.spento);
 			personale.setText("Spento");
 		}
 	}
 
 	public void checkButtonAlarm(ToggleButton personale, String log){
 		if(personale.isChecked())
 		{
 			personale.setBackgroundResource(R.layout.rosso);
 			personale.setText(log);
 		}
 		else
 		{
 			personale.setBackgroundResource(R.layout.acceso);
 			personale.setText(log);
 		}
 	}
 
 	public void refresh(){
 		this.readFile = new Configuration(nameFile);
 		this.default1 = readFile.getDefault0(); 
 		this.default2 = readFile.getDefault1();
 		this.default3 = readFile.getDefault2();
 		this.default4 = readFile.getDefault3();
 
 	}
 
 
 
 	@SuppressWarnings("static-access")
 	@Override
 	public void onClick(View button) {
 
 		if(button == personalizzato1){
 			if (!personalizzato1.isChecked()){
 				//				segnale di spegnimento
 				checkButton(personalizzato1);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(0, 0));
 
 			}
 			else if (personalizzato1.isChecked()){
 				//				segnale d'accensione
 				checkButton(personalizzato1);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(0, 1));
 			}
 		}
 		if(button == personalizzato2){
 			if (!personalizzato2.isChecked()){
 				checkButton(personalizzato2);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(1, 0));
 			}
 			else if (personalizzato2.isChecked()){
 				checkButton(personalizzato2);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(1, 1));
 			}
 		}
 		if(button == personalizzato3){
 			if (!personalizzato3.isChecked()){
 				checkButton(personalizzato3);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(2, 0));
 			}
 			else if (personalizzato3.isChecked()){
 				checkButton(personalizzato3);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(2, 1));
 			}
 		}
 		if(button == personalizzato4){
 			if (!personalizzato4.isChecked()){
 				checkButton(personalizzato4);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(3, 0));
 			}
 			else if (personalizzato4.isChecked()){
 				checkButton(personalizzato4);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(3, 1));
 			}
 		}
 		if(button == personalizzato5){
 			if (!personalizzato5.isChecked()){
 				checkButton(personalizzato5);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(5, 0));
 			}
 			else if (personalizzato5.isChecked()){
 				checkButton(personalizzato5);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(4, 1));
 			}
 		}
 		if(button == personalizzato6){
 			if (!personalizzato6.isChecked()){
 				checkButton(personalizzato6);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(6, 0));
 			}
 			else if (personalizzato6.isChecked()){
 				checkButton(personalizzato6);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(6, 1));
 			}
 		}
 		if(button == personalizzato7){
 			if (!personalizzato7.isChecked()){
 				checkButton(personalizzato7);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(7, 0));
 			}
 			else if (personalizzato7.isChecked()){
 				checkButton(personalizzato7);
 				new MultiThread("127.0.0.1", 9001,new PaccoGpio(7, 1));
 			}
 		}
 		if(button == personalizzato8){
 			checkButtonAlarm(personalizzato8,"info");
 			//			new MultiThread("127.0.0.1", 9001,new PaccoGpio(8, 2)); 
 			new MultiThread("127.0.0.1", 9001,new PaccoGpio(8, 0));
 		}
 		if(button == personalizzato9){
 			checkButtonAlarm(personalizzato9,"info");
 			//				new MultiThread("127.0.0.1", 9001,new PaccoGpio(9, 2)); 
 			new MultiThread("127.0.0.1", 9001,new PaccoGpio(9, 0));
 		}
 		if(button == personalizzato10){
 			checkButtonAlarm(personalizzato10,"info");
 			//			new MultiThread("127.0.0.1", 9001,new PaccoGpio(10, 2));  
 			new MultiThread("127.0.0.1", 9001,new PaccoGpio(10, 0));
 		}
 
 
 
 		if(button == getStatus){
 			//			log("Waiting refresh....");
 			errore =(TextView) view.findViewById(R.id.erroreConnessione);
 
			new ProgressMessageTask().execute("Recupero Stato gpio....");
 			
 			try {
 
 				thread = new MultiThread("127.0.0.1", 9001,new PaccoStatus(1), sem);
 				sem.acquire();
 			}
 			catch (InterruptedException e) {
 				errore.setText("Non riesco a raggiungere il Server di Casa");
 				System.out.println("Non riesco a raggiungere il Server di Casa");
 				e.printStackTrace();
 				return;
 			}
 			if(thread.errore){
 				errore.setText("Non riesco a raggiungere il Server di Casa");
 				System.out.println("Non riesco a raggiungere il Server di Casa");
 				return;
 			} else {
 
 				((ViewGroup) view).removeAllViews();
 
 				view = getView().inflate(getView().getContext(), R.layout.status,((ViewGroup) view) );
 				/*ScaleAnimation scale = new ScaleAnimation((float)1, (float)1, (float)0.1, (float)1,(float)3,(float)3);
 			scale.setFillAfter(true);
 			scale.setDuration(500);
 			view.startAnimation(scale); 
 				 */
 				Animation scale = new ScaleAnimation(1, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
 				// 1 second duration
 				//			scale.setDuration(1000);
 				// Moving up
 				//			Animation slideUp = new TranslateAnimation(1200, 0, 0, 0);
 				// 1 second duration
 				DisplayMetrics displaymetrics = new DisplayMetrics();
 				getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 				int height = displaymetrics.heightPixels;
 				int width = displaymetrics.widthPixels;
 				Animation slideUp = new TranslateAnimation(0, 0, -height,0);
 				slideUp.setDuration(800);
 				Animation slideDown = new TranslateAnimation(0, 0,0,height/4);
 				slideDown.setDuration(1200);
 				Animation slideNormal = new TranslateAnimation(0, 0,height/4,-height/4);
 				slideNormal.setDuration(1900);
 				AnimationSet animSet = new AnimationSet(true);
 
 				//			animSet.addAnimation(scale);
 				animSet.addAnimation(slideUp);
 				animSet.addAnimation(slideDown);
 
 				animSet.addAnimation(slideNormal);
 				view.startAnimation(animSet);
 				getStatus = (Button) view.findViewById(R.id.getStatus);
 				getStatus.setOnClickListener(this);
 				personalizzato1 = (ToggleButton) view.findViewById(R.id.pesonalizzato1);
 				personalizzato2 = (ToggleButton) view.findViewById(R.id.pesonalizzato2);
 				personalizzato3 = (ToggleButton) view.findViewById(R.id.pesonalizzato3);
 				personalizzato4 = (ToggleButton) view.findViewById(R.id.pesonalizzato4);
 				personalizzato5 = (ToggleButton) view.findViewById(R.id.ControlloGarage);
 				personalizzato6 = (ToggleButton) view.findViewById(R.id.ControlloAllarmeGarage);
 				personalizzato7 = (ToggleButton) view.findViewById(R.id.ControlloAllarmeCasa);
 				personalizzato8 = (ToggleButton) view.findViewById(R.id.ControlloPerditaAcquarioButton);
 				personalizzato9 = (ToggleButton) view.findViewById(R.id.ControlloPerditaCasaButton);
 				personalizzato10 = (ToggleButton) view.findViewById(R.id.ControlloMovimentoCasaButton);
 				personalizzato1.setClickable(true);
 				personalizzato2.setClickable(true);
 				personalizzato3.setClickable(true);
 				personalizzato4.setClickable(true);
 				personalizzato5.setClickable(true);
 				personalizzato6.setClickable(true);
 				personalizzato7.setClickable(true);
 				personalizzato8.setClickable(true);
 				personalizzato9.setClickable(true);
 				personalizzato10.setClickable(true);
 				personalizzato1.setOnClickListener(this);
 				personalizzato2.setOnClickListener(this);
 				personalizzato3.setOnClickListener(this);
 				personalizzato4.setOnClickListener(this);
 				personalizzato5.setOnClickListener(this);
 				personalizzato6.setOnClickListener(this);
 				personalizzato7.setOnClickListener(this);
 				personalizzato8.setOnClickListener(this);
 				personalizzato9.setOnClickListener(this);
 				personalizzato10.setOnClickListener(this);
 				def1 = (TextView) view.findViewById(R.id.personalizzatoText1);
 				def2 = (TextView) view.findViewById(R.id.personalizzatoText2);
 				def3 = (TextView) view.findViewById(R.id.personalizzatoText3);
 				def4 = (TextView) view.findViewById(R.id.personalizzatoText4);
 				refresh();
 				if(default1 != null)def1.setText(default1);
 				if(default2 != null)def2.setText(default2);
 				if(default3 != null)def3.setText(default3);
 				if(default4 != null)def4.setText(default4);
 				//			int [] prova = new int[]{0,0,0,0,0,0,0,0,0,0,0,0};
 				//			status = prova;
 				status = thread.settings;
 				System.out.println("ricevuto array: "+status[0]+status[1]+status[2]+status[3]+status[4]+status[5]+status[6]+status[7]+status[8]+status[9]);
 				if(status[0] == 0){
 					personalizzato1.setChecked(false);
 					checkButton(personalizzato1);
 				}
 				else if(status[0] == 1){
 					personalizzato1.setChecked(true);
 					checkButton(personalizzato1);
 				}
 
 				if(status[1] == 0){
 					personalizzato2.setChecked(false);
 					checkButton(personalizzato2);
 				}
 				else if(status[1]==1){
 					personalizzato2.setChecked(true);
 					checkButton(personalizzato2);
 				}
 
 				if(status[2] == 0){
 					personalizzato3.setChecked(false);
 					checkButton(personalizzato3);
 				}
 				else if(status[2]==1){
 					personalizzato3.setChecked(true);
 					checkButton(personalizzato3);
 				}
 
 				if(status[3] == 0){
 					personalizzato4.setChecked(false);
 					checkButton(personalizzato4);
 				}
 				else if(status[3]==1){
 					personalizzato4.setChecked(true);
 					checkButton(personalizzato4);
 				}
 
 				if(status[4] == 0){
 					personalizzato5.setChecked(false);
 					checkButton(personalizzato5);
 				}
 				else if(status[4]==1){
 					personalizzato5.setChecked(true);
 					checkButton(personalizzato5);
 				}
 
 				if(status[5] == 0){
 					personalizzato6.setChecked(false);
 					checkButton(personalizzato6);
 				}
 				else if(status[5]==1){
 					personalizzato6.setChecked(true);
 					checkButton(personalizzato6);
 				}
 
 				if(status[6] == 0){
 					personalizzato7.setChecked(false);
 					checkButton(personalizzato7);
 				}
 				else if(status[6]==1){
 					personalizzato7.setChecked(true);
 					checkButton(personalizzato7);
 				}
 
 				if(status[7] == 0){
 					personalizzato8.setChecked(false);
 					checkButtonAlarm(personalizzato8,"OK");
 				}
 				else if(status[7]==1){
 					personalizzato8.setChecked(true);
 					checkButtonAlarm(personalizzato8,"allarme");
 				}
 
 				if(status[8] == 0){
 					personalizzato9.setChecked(false);
 					checkButtonAlarm(personalizzato9,"OK");
 				}
 				else if(status[8]==1){
 					personalizzato9.setChecked(true);
 					checkButtonAlarm(personalizzato9,"allarme");
 				}
 
 				if(status[9] == 0){
 					personalizzato10.setChecked(false);
 					checkButtonAlarm(personalizzato10,"OK");
 				}
 				else if(status[9]==1){
 					personalizzato10.setChecked(true);
 					checkButtonAlarm(personalizzato10,"allarme");
 				}
 			}
 
 
 
 		}
 
 
 
 
 
 
 
 
 	}
 	// TODO Auto-generated method stub
 
 }
