 package com.example.clasificados3;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v7.app.ActionBarActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 import com.example.clasificados3.Clases.Usuario;
 import com.example.clasificados3.Controladores.Metodos;
 
 public class MainActivity extends ActionBarActivity
 {
     public static String ip = "10.0.0.3";
     public static String pathImagenesServidor = "http://" + MainActivity.ip + "/prueba/uploads/";
     public static Usuario usuario = new Usuario();
     Metodos metodos = new Metodos(ip);
     EditText et_usuario;
     EditText et_password;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         if (savedInstanceState == null) {
             getSupportFragmentManager().beginTransaction()
                     .add(R.id.container, new PlaceholderFragment())
                     .commit();
         }
 
         //----- Solucion Error HttpPost
 //        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
 //        StrictMode.setThreadPolicy(policy);
         //-------------------------------
 
         et_usuario = (EditText)findViewById(R.id.et_usuario);
         et_password = (EditText)findViewById(R.id.et_password);


        Intent i = new Intent(this, DetalleClasificado.class );
        startActivity(i);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();
         if (id == R.id.action_settings) {
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * A placeholder fragment containing a simple view.
      */
     public static class PlaceholderFragment extends Fragment {
 
         public PlaceholderFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_main, container, false);
             return rootView;
         }
     }
 
 
     public void registrarse(View view)
     {
         Intent i = new Intent(this, RegistrarUsuario.class );
         startActivity(i);
     }
 
     public void iniciarSesion(View view)
     {
         int x = metodos.validarUsuario(et_usuario.getText().toString(), et_password.getText().toString());
 
         if(x == 1)
         {
             usuario = metodos.getUsuario(et_usuario.getText().toString());
 
             Intent i = new Intent(this, Home.class );
             startActivity(i);
 
         }
         else
         {
         new AlertDialog.Builder(this)
                 .setTitle("El usuario o la contraseña no son correctas")
                 .setPositiveButton("Aceptar",
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {}
                         }).show();
         }
     }
 
 
     public void prueba(View view)
     {
         Intent i = new Intent(this, DetalleClasificado.class );
         startActivity(i);
     }
 
 }
