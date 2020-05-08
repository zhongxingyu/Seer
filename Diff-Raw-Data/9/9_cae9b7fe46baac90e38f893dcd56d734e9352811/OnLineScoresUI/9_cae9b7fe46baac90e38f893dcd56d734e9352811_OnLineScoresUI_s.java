 package com.turlutu;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Dialog;
 import android.database.Cursor;
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
 public class OnLineScoresUI   extends AngleUI
 {
 	private AngleObject ogMenuTexts;
 	private AngleString strExit, strScores, strNames, strPushScore;
 	
 	public OnLineScoresUI(AngleActivity activity, Background mBackGround)
 	{
 		super(activity);
 		if(mBackGround != null)
 			addObject(mBackGround);
 		ogMenuTexts = new AngleObject();
 		
 		addObject(ogMenuTexts);
 
 		strPushScore = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Send my score", 160, 30, AngleString.aCenter));
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
 			else if (strPushScore.test(eX, eY))
 				UploadMyScore();
 
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onActivate()
 	{
 		Log.i("ScoresUI", "OnlineScoresUI onActivate debut "+((MainActivity) mActivity).mGame.mScore);
 
 		getScores();
 		Log.i("ScoresUI", "OnlineScoresUI onActivate fin");
 	}
 	
 	public void UploadMyScore() {
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				DBScores db = new DBScores(mActivity);
 				db.open();
 		        Cursor c = db.getAllScores();
 		        if (c.moveToFirst())
 		        {
 		            do {          
 						postData(""+c.getString(2), ""+c.getString(1));
 		            } while (c.moveToNext());
 		        }
 		        db.close();
 		        getScores();
 			}
 		}.start();
 	}
 	
 	
 	public static String md5(String key) throws NoSuchAlgorithmException {
 		byte[] uniqueKey = key.getBytes();
 		byte[] hash = null;
 		hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
 		StringBuffer hashString = new StringBuffer();
 		for ( int i = 0; i < hash.length; ++i ) {
 			String hex = Integer.toHexString(hash[i]);
 			if ( hex.length() == 1 ) {
 				hashString.append('0');
 				hashString.append(hex.charAt(hex.length()-1));
 			} else {
 				hashString.append(hex.substring(hex.length()-2));
 			}
 		}
 		return hashString.toString();
 	}
 	
 	
 	public void postData(String name, String score) {
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost("http://wwwetu.utc.fr/~mguffroy/Score.php?page=add");
 		
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair("name", name));
 		pairs.add(new BasicNameValuePair("score", score));
 		try {
 			pairs.add(new BasicNameValuePair("sign", md5(name+score+"I am the best")));
 		} catch (NoSuchAlgorithmException e1) {
 			Log.e("POST", "Error");
 		}
 		try {
 			post.setEntity(new UrlEncodedFormEntity(pairs));
 		} catch (UnsupportedEncodingException e) {
 			Log.e("POST", "UnsupportedEncodingException");
 		}
 		try {
 			ResponseHandler<String> responseHandler=new BasicResponseHandler();
 			String responseBody=client.execute(post, responseHandler);
 			
 			//HttpResponse response = client.execute(post);
 			Log.e("http",responseBody );
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
     }
 	
 	
 	public String getData(String info) {
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpGet getMethod=new HttpGet("http://wwwetu.utc.fr/~mguffroy/Score.php?page=get&info="+info);
 
 		try {
 			ResponseHandler<String> responseHandler=new BasicResponseHandler();
 			String responseBody=client.execute(getMethod, responseHandler);
 			return responseBody;
 		}
 		catch (Throwable t) {
 			Log.e("Exitjump","Exception in getData()", t);
 			return "error";
 		}
 	}
 	
 	public void getScores() {
     	String scores = getData("scores");
     	String names = getData("names");
         strScores.set(scores);
         strNames.set(names);
 	}
 	
 	public void askName() {
 		Dialog dialog = new Dialog(mActivity);
         dialog.setContentView(R.layout.name_activity);
         dialog.setTitle("Entrer votre nom :");
         Button buttonOK = (Button) dialog.findViewById(R.id.ok);        
         buttonOK.setOnClickListener(new OKListener(dialog));
         Button buttonCancel = (Button) dialog.findViewById(R.id.cancel);        
         buttonCancel.setOnClickListener(new CancelListener(dialog));
         dialog.show();
 	}
 	protected class OKListener implements OnClickListener {	 
         private Dialog dialog;
         public OKListener(Dialog dialog) {
                 this.dialog = dialog;
         }
 
         public void onClick(View v) {
         		TextView input = (TextView) dialog.findViewById(R.id.entry);
         		CharSequence name = input.getText();
         		dialog.dismiss(); 
         		DBScores db = new DBScores(mActivity);
         		db.open();
         		long i = db.insertScore( ((MainActivity) mActivity).mGame.mScore, ""+name);
         		postData(""+name, ""+((MainActivity) mActivity).mGame.mScore);
         		Log.i("ScoresUI", "ScoresUI on click on ok insert : " + i);
         		db.close();
         		((MainActivity) mActivity).mGame.mScore = 0;
         		getScores();
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
         		getScores();
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
