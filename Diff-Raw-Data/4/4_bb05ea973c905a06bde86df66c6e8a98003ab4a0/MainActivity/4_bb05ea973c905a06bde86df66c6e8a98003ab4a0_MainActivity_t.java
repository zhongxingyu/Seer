 package com.telnor.curso.tadk;
 
 import com.androidquery.AQuery;
 import com.telnor.curso.tadk.contentProvider.UsuarioContentProvider;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import com.telnor.curso.tadk.camara.CameraActivity;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
 	Button btnCamera;
 	AQuery aq;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
		btnCamera = (Button) findViewById(R.id.btn_camera);
 		btnCamera.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent cameraIntent = new Intent(getApplicationContext(),
 						CameraActivity.class);
 				startActivity(cameraIntent);
 			}
 		});
 		
 		aq = new AQuery(this);
 
 		aq.id(R.id.btn_mainActivity_contentProvider).clicked(this,
 				"abrirContentProvider");
 	}
 
 	public void abrirContentProvider() {
 		Intent intent = new Intent(this, UsuarioContentProvider.class);
 		startActivity(intent);
 
 	}
 
 }
