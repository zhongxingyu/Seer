 package com.example.boox;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import listasLibros.Libro;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHttpResponse;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import crossings.GestorCrossings;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 import apiGoogle.InterfazAPI;
 
 public class TabCrossingsFragment extends ListFragment implements OnItemClickListener {
 
     boolean mDualPane;
     int mCurCheckPosition = 1;
 
 
 
 	public ArrayList<String> list = new ArrayList<String>(); 
 
     final int mode = Activity.MODE_PRIVATE;
 	public static final String myPrefs = "prefs";
 	String uname = new String();
 	public ListView crossingListView;
 	public ArrayList<CrossingListRow> crossingList;
     AdapterView.AdapterContextMenuInfo info;
 	
 	
 	@Override
     public void onActivityCreated(Bundle savedState) {
         super.onActivityCreated(savedState);
 
         SharedPreferences mySharedPreferences = this.getActivity().getSharedPreferences(myPrefs, mode);
 		uname = mySharedPreferences.getString("username", "");
         // Populate list with our static array of titles.
 
 
         // Check to see if we have a frame in which to embed the details
         // fragment directly in the containing UI.
         /*View detallesLibro = getActivity().findViewById(R.id.details);
         mDualPane = detallesLibro != null && detallesLibro.getVisibility() == View.VISIBLE;
     	Log.d("BooksTab", "onActivityCreated -> mDualPane = " + (mDualPane ? "true" : "false"));
 
         if(savedState != null){
         	Log.d("BooksTab", "savedState = null");
             // Restore last state for checked position.
             mCurCheckPosition = savedState.getInt("curChoice", 0);
         }
         if(mDualPane){
         	Log.d("BooksTab", "mDualPane = true");
             // In dual-pane mode, list view highlights selected item.
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             // Make sure our UI is in the correct state.
             showDetails(mCurCheckPosition);
         }*/
         
         
   /*      
         crossingList = new ArrayList<CrossingListRow>();
         CrossingListRow row;
         
         row = new CrossingListRow();
         row.setThumb1(R.drawable.got_thumbnail_small);
         row.setTitle1("Juego de Tronos");
         row.setAuthor1("G.R.R. Martin");
         row.setThumb2(R.drawable.got_thumbnail_small);
         row.setTitle2("Choque de Reyes");
         row.setAuthor2("G.R.R. Martin");
         row.setState("Rejected");
         crossingList.add(row);
 */
 		//crossingListView = (ListView) getView().findViewById(R.id.crossingList);
 		//CrossingListAdapter adapter = new CrossingListAdapter(getActivity(), crossingList);
         //crossingListView.setAdapter(adapter);
         //crossingListView.setOnItemClickListener(this);
 
         AsyncGetCross gc = new AsyncGetCross();
         try{
         	gc.execute(uname, null, null);
         }catch(Exception e){
         	// PONER LAYOUT SI PETAAAA
     		//setListAdapter(adapter);
         }
         
     }
 
 	@Override
 	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
 		
         //Intent intent = new Intent();
         //intent.setClass(getActivity(), CrossingActivity.class);
         //intent.putExtra("index", pos);
         //startActivity(intent);
 	}
 
 	public class AsyncGetCross extends AsyncTask<String, Void, Void> {
 
 		StringBuilder sb = new StringBuilder();
 		String responseString;
 		boolean flag = true;
 		
 		@Override
 		protected Void doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			
 			///////////////////////////////////////////////
 			HttpParams httpParameters = new BasicHttpParams();
 
 			int timeoutConnection = 1500;
 			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 
 			int timeoutSocket = 1500;
 			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 
 			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
 			HttpGet request = new HttpGet(
 					"http://boox.eu01.aws.af.cm/users/"+uname+"/getCrossings");
 
 			request.setHeader("Accept", "application/json");
 			BasicHttpResponse response;
 		
 			try {
 				response = (BasicHttpResponse) client.execute(request);
 				HttpEntity entity = response.getEntity();
 				if (entity != null) {
 					InputStream stream = entity.getContent();
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(stream));
 					String line = null;
 					while ((line = reader.readLine()) != null) { 
 					    sb.append(line + "\n"); 
 					}
 					stream.close();
 					responseString = sb.toString();
 				}
 			////////////////////////////////////////////////////////////////////////////////////////////////////
 			} catch (ConnectTimeoutException e) {
 				flag = false;
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				flag = false;
 				e.printStackTrace();
 			} catch (IOException e) {
 				flag = false;
 				e.printStackTrace();
 			}
 			     
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if(flag){
 				GsonBuilder builder = new GsonBuilder();
 				Gson gson = builder.create();
 				GestorCrossings cross = new GestorCrossings();
 				JSONObject json;
 				
 				try {
 					json = new JSONObject(responseString);
 
 				    cross = gson.fromJson(json.toString(),
 						GestorCrossings.class);
 				    
 				    if(list.isEmpty()){
 						InterfazAPI api = new InterfazAPI(); 
 						crossingList = new ArrayList<CrossingListRow>();
 						CrossingListRow row;
						Libro lib1;
 						Libro lib2;
 						for(int i=0; i<cross.getNumeroCrossingsUsuarioActual(); i++){
							lib1 = api.ObtenerLibroPorId(cross.getCrossings().get(i).getbook1());
 							lib2 = api.ObtenerLibroPorId(cross.getCrossings().get(i).getbook2());
 
 							row = new CrossingListRow();
 							row.setThumb1(R.drawable.got_thumbnail_small);
							row.setTitle1(lib1.getTitulo());
 							row.setAuthor1(cross.getCrossings().get(0).user1);
 							row.setThumb2(R.drawable.got_thumbnail_small);
 							row.setTitle2(lib2.getTitulo());
 							row.setAuthor2(cross.getCrossings().get(0).user2);
 							row.setState("Rejected");
 							crossingList.add(row);
 						}
 						CrossingListAdapter adapter = new CrossingListAdapter(getActivity(), crossingList);
 						setListAdapter(adapter);
 				    }
 				}catch (JSONException e) {
 			    	   Toast toast = Toast.makeText(
 								getActivity(), 
 								"error", 
 								Toast.LENGTH_LONG);
 						toast.show();
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}	
 			}
 			else {
 				//Fallo de conexion, cargar layout vacio
 		    	   Toast toast = Toast.makeText(
 							getActivity(), 
 							getResources().getString(R.string.login_internet_error), 
 							Toast.LENGTH_SHORT);
 					toast.show();
 			}
 
 		}
 	
 	}
 	
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("curChoice", mCurCheckPosition);
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int pos, long id) {
     	Log.d("CrossingsTabFragment", "onListItemClick(" + pos + ")");
     	
     	/*Intent intent = new Intent();
         intent.setClass(getActivity(), CrossingListActivity.class);
         intent.putExtra("index", pos);
         startActivity(intent);*/
     	
         //showDetails(pos);
     }
 
 }
