 package com.sosobro.enchroma;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 // TODO: Optimize layout
 //       http://stackoverflow.com/questions/5254562/is-there-a-simpler-better-way-to-put-a-border-outline-around-my-textview
 
 public class MainActivity extends Activity {
 
 	public static final int REQ_CAPTURESUBJECTPHOTO = 1;
 	public static final int REQ_SELECTBACKGROUNDPHOTO = 2;
 	
 	@Override
 	protected void onCreate( Bundle savedInstanceState ) {
 		super.onCreate( savedInstanceState );
 		setContentView( R.layout.activity_main );
 
 		{ // Init subjectImg
 			PhotoView v = (PhotoView) findViewById( R.id.subjectImg );
 			v.init( this, REQ_CAPTURESUBJECTPHOTO );
 		}
 		{ // Init backgroundImg
 			PhotoView v = (PhotoView) findViewById( R.id.backgroundImg );
 			v.init( this, REQ_SELECTBACKGROUNDPHOTO );
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu ) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate( R.menu.main, menu );
 		return true;
 	}
 
 	/*@Override
 	protected void onRestoreInstanceState( Bundle state ) {
 		super.onRestoreInstanceState( state );
 	}*/
 
 	@Override
 	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
 		super.onActivityResult( requestCode, resultCode, data );
 
 		System.out.printf("onActivityResult req=%d res=%d\n", requestCode, resultCode);
 
 		switch (requestCode)
 		{
 		case REQ_CAPTURESUBJECTPHOTO: {
 			PhotoView v = (PhotoView) findViewById( R.id.subjectImg );
 			v.onActivityResult( resultCode, data );
 		} break;
 		case REQ_SELECTBACKGROUNDPHOTO: {
			PhotoView v = (PhotoView) findViewById( R.id.backgroundImg );
 			v.onActivityResult( resultCode, data );
 		} break;
 		}
 	}
 
 	/*@Override
 	protected void onResume( ) {
 		super.onResume();
 	}*/
 
 	/*@Override
 	protected void onSaveInstanceState( Bundle state ) {
 		super.onSaveInstanceState( state );
 	}*/
 	
 	public void engageBtn_onClick( View v ) {
 		// TODO: Enage
 	}
 }
