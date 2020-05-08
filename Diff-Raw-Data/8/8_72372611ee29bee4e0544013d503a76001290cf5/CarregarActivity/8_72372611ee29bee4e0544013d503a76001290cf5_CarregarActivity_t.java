 package br.com.activity;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import br.com.logica.FileAdapter;
 import br.com.logica.ListMusic;
 import com.beaglebuddy.mp3.MP3;
 import com.example.tagedition.R;
 
 public class CarregarActivity extends Activity {
 	
 	private File sdcard;
 	private List<File> list;
 	private ListView listView;
 	private FileAdapter adapter;
 	
 	private void updateList(File[] files){	
 		if (list == null){
 			list = new ArrayList<File>();
 		}else{
 			list.removeAll(list);
 		}
 		
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				list.add(0, files[i]);
 			}else{
 				if (files[i].getName().contains(".mp3")){
 					list.add(files[i]);
 				}
 			}	
 		}
 	}
 	
 	TextView textViewAlbum = null;
 	TextView textViewAutor = null;
 	TextView textViewGenero = null;
 	File file = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_carregar);
 		textViewAutor = (TextView) findViewById(R.id.textAutor);
 		textViewAlbum = (TextView) findViewById(R.id.textAlbum);
 		textViewGenero = (TextView) findViewById(R.id.textGenero);
 		
 		textViewAutor.setText("Autor: "+"Campo Vázio");
 		textViewAlbum.setText("Album: "+"Campo Vázio");
 		textViewGenero.setText("Genero: "+"Campo Vázio");
 		
 		ImageButton botaoAdd = (ImageButton) findViewById(R.id.botaoAddMusica);
 		
 		botaoAdd.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(file != null){
 					if (!(ListMusic.getInstance().getListMusic().contains(file))){
 						ListMusic.getInstance().addMusica(file);
 						MainActivity.fileAdapter.notifyDataSetChanged();
 					}
 				}
 				
 			}
 		});
 		
 		sdcard = Environment.getExternalStorageDirectory();
 		
 		final File[] files = sdcard.listFiles();
 		
 		listView = (ListView) findViewById(R.id.files);
 		updateList(files);
 		adapter = new FileAdapter(getApplicationContext(), list);
 		//ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
 		listView.setAdapter(adapter);
 		
 		listView.setOnItemClickListener(new OnItemClickListener() {
 	        @Override
 			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
 	        	if (list.get(position).isDirectory()){
 	        		File[] filesAux = list.get(position).listFiles();
 	        		File auxParent = list.get(position).getParentFile();
 	        		updateList(filesAux);
 	        		list.add(0, auxParent);
 	        		adapter.notifyDataSetChanged();
 	        	}else{
 	        		try {
 	        			System.out.println("entrou");
 						MP3 m = new MP3(list.get(position));
						if(m.getAlbum()!=null && m.getAlbum().length()>20){
 							String album = String.valueOf(m.getAlbum()).length()>12 ? String.valueOf(m.getAlbum()).substring(0,20)+"...":String.valueOf(m.getAlbum());
 							textViewAlbum.setText("Album: "+album);
 						}
						if(m.getMusicBy() !=null && m.getMusicBy().length()> 20){
 							String musicBy = String.valueOf(m.getMusicBy()).length()>12 ? String.valueOf(m.getMusicBy()).substring(0,20)+"...":String.valueOf(m.getMusicBy());
 							textViewAutor.setText("Autor: "+musicBy);
 						}
						if(m.getMusicType() != null && m.getMusicType().length()>20){
 							String genero = String.valueOf(m.getMusicType()).length()>12 ? String.valueOf(m.getMusicType()).substring(0,20)+"...":String.valueOf(m.getMusicType());
 							textViewGenero.setText("Genero: "+genero);
 						}
 						file = list.get(position);
 						
 					} catch (IOException e) {
 						System.out.println("Não foi possivel adicionar musica!");
 					}
 	        		
 	        	}
 	        }
 	    });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.carregar, menu);
 		return true;
 	}
 
 }
