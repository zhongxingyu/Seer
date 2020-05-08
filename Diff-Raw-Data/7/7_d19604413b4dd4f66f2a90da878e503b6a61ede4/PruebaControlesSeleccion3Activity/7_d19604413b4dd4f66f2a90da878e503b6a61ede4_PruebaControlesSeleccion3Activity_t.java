 package com.pruebas.controlesseleccion2;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PruebaControlesSeleccion3Activity extends Activity {
     /** Called when the activity is first created. */
 	
 	private Titular[] datos = new Titular[25];
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         for(int i = 0;i<25;i++){
         	datos[i] = new Titular("Ttulo " + (i+1), "Subttulo largo " + (i+1));
         }
         	 
         AdaptadorTitulares adaptador = new AdaptadorTitulares(this);
          
         ListView lstOpciones = (ListView)findViewById(R.id.LstOpciones);
          
         lstOpciones.setAdapter(adaptador);
     	
     	lstOpciones.setOnItemClickListener(new OnItemClickListener() {
     	    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
     	        //Acciones necesarias al hacer click
     	    }
     	});
     	
     }
 
 
 	
 	class AdaptadorTitulares extends ArrayAdapter {
 		 
 		Activity context;
  
         AdaptadorTitulares(Activity context) {
             super(context, R.layout.listitem_titular, datos);
             this.context = context;
         }
  
         public View getView(int position, View convertView, ViewGroup parent) {
 	        
         	View item = convertView;
         	ViewHolder holder;
         	
         	if(item==null){
         		LayoutInflater inflater = context.getLayoutInflater();
     	        item = inflater.inflate(R.layout.listitem_titular, null);
     	        holder = new ViewHolder();
     	        holder.titulo = (TextView)item.findViewById(R.id.LblTitulo);
     	        holder.subtitulo = (TextView)item.findViewById(R.id.LblSubTitulo);
     	        
     	        item.setTag(holder);
         	}else{
         		holder = (ViewHolder)item.getTag();
     		}
 			
 			holder.titulo.setText(datos[position].getTitulo());
 			holder.subtitulo.setText(datos[position].getSubtitulo());
 			
 			return(item);
 		}
     }
 	
 	static class ViewHolder{
 		TextView titulo;
 		TextView subtitulo;
 	}
 }
