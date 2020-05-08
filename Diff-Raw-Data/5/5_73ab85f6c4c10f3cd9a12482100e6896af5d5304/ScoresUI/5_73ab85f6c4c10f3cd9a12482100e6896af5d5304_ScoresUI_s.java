 package com.turlutu;
 
 import android.app.Dialog;
 import android.database.Cursor;
 import android.os.Looper;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.angle.AngleActivity;
 import com.android.angle.AngleObject;
 import com.android.angle.AngleString;
 import com.android.angle.AngleUI;
 
 // TODO rajouter un bouton rejouer
 public class ScoresUI   extends AngleUI
 {
 	private AngleObject ogMenuTexts;
 	private AngleString strExit, strScores, strNames, strNewScore;
 	
 	public ScoresUI(AngleActivity activity, Background mBackGround)
 	{
 		super(activity);
 		if(mBackGround != null)
 			addObject(mBackGround);
 		ogMenuTexts = new AngleObject();
 		
 		addObject(ogMenuTexts);
 
 		strNewScore = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "", 160, 30, AngleString.aCenter));
 		strScores = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "", 30, 100, AngleString.aLeft));
 		strNames = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "", 170, 100, AngleString.aLeft));
 		strExit = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Back home", 160, 390, AngleString.aCenter));
 
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			float eX = event.getX();
 			float eY = event.getY();
 
 			if (strExit.test(eX, eY))
 				((MainActivity) mActivity).setUI(((MainActivity) mActivity).mMenu);
 
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onActivate()
 	{
 		Log.i("ScoresUI", "ScoresUI onActivate debut "+((MainActivity) mActivity).mGame.mScore);
 		
 		showScores();
 		
 		if( ((MainActivity) mActivity).mGame.mScore != 0) 
 		{
 			Log.i("ScoresUI", "Dialog show");
 			strNewScore.set("Last Score : " + ((MainActivity)mActivity).mGame.mScore);
 			DBScores db = new DBScores(mActivity);
 			db.open();
 			int worstscore = db.getWorstScore();
 			int nbScore = db.nbScore();
 			db.close();
			if( ((MainActivity) mActivity).mGame.mScore > worstscore || nbScore <= 8) {
 				askName();
 			}
 		}
 		Log.i("ScoresUI", "ScoresUI onActivate fin");
 	}
 	
 	public void showScores() {
     	String scores = "";
     	String names = "";
 		DBScores db = new DBScores(mActivity);
 		db.open();
         Cursor c = db.getAllScores();
         if (c.moveToFirst())
         {
             do {          
                 //DisplayScore(c);
             	scores +=  c.getString(1) + "\n";
             	names += c.getString(2) + "\n";
             } while (c.moveToNext());
         }
         db.close();
         strScores.set(scores);
         strNames.set(names);
 	}
 	
 	public void askName() {
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				Looper.prepare();
 				Dialog dialog = new Dialog(mActivity);
 		        dialog.setContentView(R.layout.name_activity);
 		        //dialog.setTitle("Entrer votre nom :");
 		        TextView inputText = (TextView) dialog.findViewById(R.id.entry);
 		        inputText.setText(((MainActivity)mActivity).mOptions.mName);
 		        Button buttonOK = (Button) dialog.findViewById(R.id.ok);        
 		        buttonOK.setOnClickListener(new OKListener(dialog));
 		        Button buttonCancel = (Button) dialog.findViewById(R.id.cancel);        
 		        buttonCancel.setOnClickListener(new CancelListener(dialog));
 		        dialog.show();
 				Looper.loop();
 			}
 		}.start();
 	}
 	protected class OKListener implements OnClickListener {	 
         private Dialog dialog;
         public OKListener(Dialog dialog) {
                 this.dialog = dialog;
         }
 
         public void onClick(View v) {
         		TextView input = (TextView) dialog.findViewById(R.id.entry);
         		String name = ""+input.getText();
         		if (name == "")
         			name = "anonyme";
         		((MainActivity)mActivity).mOptions.mName = name;
         		dialog.dismiss(); 
         		DBScores db = new DBScores(mActivity);
         		db.open();
         		long i = db.insertScore( ((MainActivity) mActivity).mGame.mScore, name);
         		Log.i("ScoresUI", "ScoresUI on click on ok insert : " + i);
         		db.close();
         		((MainActivity) mActivity).mGame.mScore = 0;
         		showScores();
         }
 	}
 	
 	protected class CancelListener implements OnClickListener {	 
         private Dialog dialog;
         public CancelListener(Dialog dialog) {
                 this.dialog = dialog;
         }
 
         public void onClick(View v) {
                 dialog.dismiss();    
         		((MainActivity) mActivity).mGame.mScore = 0;
         }
 	}
 
 	public void DisplayScore(Cursor c)
     {
         Toast.makeText(mActivity, 
                 "score : " + c.getString(0) + "\n" +
                 "name: " + c.getString(1) ,
                 Toast.LENGTH_LONG).show();
     } 
 	
 	@Override
 	public void onDeactivate()
 	{
 		
 	}
 }
