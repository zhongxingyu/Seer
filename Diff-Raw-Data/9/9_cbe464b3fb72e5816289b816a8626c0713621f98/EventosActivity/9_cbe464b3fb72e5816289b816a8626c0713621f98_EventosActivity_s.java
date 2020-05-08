 package br.com.einsteinlimeira.beyond.mobile;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 import br.com.einsteinlimeira.beyond.mobile.util.EntidadeUtils;
 import br.com.einsteinlimeira.beyond.model.Evento;
 
 public class EventosActivity extends GlobalActivity {
 
 	private ListView listViewListaEventos;
 	private Adaptador adaptador;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_eventos);
 		
 		@SuppressWarnings("unchecked")
 		List<Evento> eventos = (List<Evento>) getIntent().getExtras().getSerializable("eventos");
 		
 		listViewListaEventos = (ListView) findViewById(R.id.lista_eventos);
 		adaptador = new Adaptador(eventos, this);
 		listViewListaEventos.setAdapter(adaptador);
 
 		listViewListaEventos.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				Evento eventoSelecionado = (Evento) listViewListaEventos.getAdapter().getItem(position);
 				
 				Log.i(Constantes.TAG, "Evento selecionado: " + EntidadeUtils.bandasToString(eventoSelecionado.getBandas()));
 				
 				Intent intent = new Intent(EventosActivity.this, EventoDetalheActivity.class);
 				intent.putExtra("evento", eventoSelecionado);
 				
 				startActivity(intent);
 			}
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	  super.onCreateOptionsMenu(menu);
 	  
 	  MenuItem menuItem = menu.add(R.string.eventos_atualizar);
 	  menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
       
       @Override
       public boolean onMenuItemClick(MenuItem item) {
         atualizarEventos();
         return true;
       }
     });
 	  
 	  return true;
 	}
 	
 	private void atualizarEventos(){
 	  new ObtemEventosAsyncTask(this) {
       private ProgressDialog progressDialog;
 
       @Override
       protected void onPreExecute() {
         progressDialog = new ProgressDialog(EventosActivity.this);
         progressDialog.setMessage(getResources().getString(
             R.string.global_aguarde));
         progressDialog.show();
       }
 
       @Override
       protected void onPostExecute(ArrayList<Evento> result) {
        progressDialog.hide();
 
         if (problema) {
           Toast.makeText(EventosActivity.this,
               getResources().getString(R.string.global_erro_requisicao), Toast.LENGTH_LONG)
               .show();
         }
         else {
           adaptador.setEventos(result);
           adaptador.notifyDataSetChanged();
         }
       }
     }.execute();
 	}
 }
