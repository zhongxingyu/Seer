 package game.interaction;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import android.widget.SimpleAdapter;
 import game.BackgroundHandler;
 import game.BracDataToServer;
 import ioio.examples.hello.GameActivity;
 import ioio.examples.hello.R;
 
 public class InteractiveGameHandler {
 	private GameActivity ga;
 	private Gallery gGallery;
 	private ArrayList<HashMap<String,Object>> partner_list = new ArrayList<HashMap<String,Object>>();
 	private InteractiveGamePopupWindowHandler pop;
 	private InteractiveGameDB igDB;
 	private SimpleAdapter adapter;
 	private int cur_pos = -1;
 	
 	
 	public InteractiveGameHandler(GameActivity ga){
 		this.ga = ga;
 		init();
 	}
 	
 	private void init(){
 		gGallery = (Gallery) ga.findViewById(R.id.interactive_game_bar);
 		igDB = new InteractiveGameDB(ga);
 		gGallery.setOnItemClickListener(new InteractiveOnItemClickListener());
 		gGallery.setOnItemSelectedListener(new InteractiveOnItemSelectedListener());
 		pop = new InteractiveGamePopupWindowHandler(ga,this);
 	}
 	
 	private final static String code_names = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	
 	private void setAdapter(){
 		partner_list.clear();
 		InteractiveGameState[] stateList = igDB.getStates();
 		if (stateList.length == 0){
 				adapter = null;
 				return;
 		}
 		for (int i=0;i<stateList.length;++i){
 			HashMap<String,Object> item = new HashMap<String,Object>();
 			int bg_pic =  BackgroundHandler.getBackgroundDrawableId(stateList[i].state, stateList[i].coin);
 			int tree_pic = BackgroundHandler.getTreeDrawableId(stateList[i].state);
 			
 			item.put("pic",bg_pic);
 			item.put("tree",tree_pic );
 			item.put("pid", stateList[i].PID);
 			item.put("code_name",code_names.substring(i, i+1) );
 			partner_list.add(item);
 		}
 		
 		adapter = new SimpleAdapter(
 					ga, 
 					partner_list,
 					R.layout.interactive_item,
 					new String[] { "pic","tree","code_name"},
 					new int[] {R.id.interactive_state,R.id.interactive_tree,R.id.interactive_code});
 	}
 	
 	private void fake_update(){
 		InteractiveGameState states[] = new 	InteractiveGameState[5];
 		states[0] = new InteractiveGameState(3,4,"Abcde");
 		states[1] = new InteractiveGameState(4,2,"Bcdef");
 		states[2] = new InteractiveGameState(5,3,"Cdefg");
 		states[3] = new InteractiveGameState(6,1,"Defgh");
 		states[4] = new InteractiveGameState(0,2,"Ehijk");
 		igDB.updateState(states);
 		update_adapter();
 	}
 	
 	private static final String SERVER_URL = "http://140.112.30.165/drunk_detection/userStates.php";
 	
 	public void update(){
 		try {
 			 DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(SERVER_URL);
 			httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 			MultipartEntity mpEntity = new MultipartEntity();
 			String devId = Secure.getString(ga.getContentResolver(), Secure.ANDROID_ID);
 			mpEntity.addPart("userData[]", new StringBody(devId));
 			httpPost.setEntity(mpEntity);
 			GetStateHandler handler = new GetStateHandler(httpClient,httpPost);
 			Thread thread = new Thread(handler);
 			thread.start();
 			thread.join();
 			if (handler .result==-1)
 				return;
			//update_adapter();
 		} catch (Exception e) {
 			e.printStackTrace();	
 			return;
 		} 
 		//fake_update();
 		
 	}
 	
 	
 	private class GetStateHandler implements Runnable{
 		private HttpPost httpPost;
 		private DefaultHttpClient httpClient;
 		public int result;
 		private ResponseHandler< String> responseHandler;
 		
 		public GetStateHandler(DefaultHttpClient httpClient, HttpPost httpPost){
 			this.httpClient = httpClient;
 			this.httpPost = httpPost;
 			responseHandler=new BasicResponseHandler();
 			result = -1;
 		}
 		@Override
 		public void run() {
 			HttpResponse httpResponse;
 			try {
 				httpResponse = httpClient.execute(httpPost);
 				String responseString = responseHandler.handleResponse(httpResponse);
 				int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
 				if (httpStatusCode == HttpStatus.SC_OK)
 					result = 1;
 				else
 					result = -1;
 				Log.d("UPDATE STATE",responseString);
 				InteractiveGameState[] states = parseResponse(responseString);
 				if (states != null){
 					igDB.updateState(states);
					update_adapter();
 				}
 				//Log.d("UPDATE STATE",responseString);
 			} catch (Exception e) {	e.printStackTrace();}
 		}
 		
 	}
 	
 	private InteractiveGameState[] parseResponse(String response){
 		response = response.substring(2, response.length()-2);
 		//Log.d("UPDATE STATE",response);
 		String[] tmp = response.split("]," );
 		if (tmp.length==0)
 			return null;
 					
 		InteractiveGameState[] states = new InteractiveGameState[tmp.length];
 		for (int i=0;i<tmp.length;++i){
 			if (tmp[i].charAt(0)=='[')
 				tmp[i]=tmp[i].substring(1,tmp[i].length());
 			String[] items = tmp[i].split(",");
 			//Log.d("UPDATE STATE",items[0]+" / " +items[1]+" / "+items[2]);
 			String pid = items[0].substring(1, items[0].length()-1);
 			int state;
 			if (items[1].equals("null"))
 				state = 0;
 			else
 				state= Integer.valueOf(items[1].substring(1,items[1].length()-1));
 			int coin;
 			if (items[2].equals("null"))
 				coin = 0;
 			else
 				coin = Integer.valueOf(items[2].substring(1,items[2].length()-1));
 			//Log.d("UPDATE STATE",pid+" / " +state+" / "+coin);
 			states[i] = new InteractiveGameState(state,coin,pid);
 		}
 		return states;
 	}
 	
 	
 	public void clear(){
 		if (partner_list != null)
 			partner_list.clear();
 		if (adapter != null)
 			adapter.notifyDataSetInvalidated();
 	}
 	
 	private void update_adapter(){
 		setAdapter();
 		if (adapter == null)
 			gGallery.setVisibility(View.INVISIBLE);
 		else{
 			gGallery.setVisibility(View.VISIBLE);
 			gGallery.setAdapter(adapter);
 			if (cur_pos == -1)
 				cur_pos= (adapter.getCount())/2;
 			gGallery.setSelection(cur_pos);
 			gGallery.refreshDrawableState();
 		}
 	}
 	
 	private class InteractiveOnItemClickListener implements AdapterView.OnItemClickListener{
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
 			int idx = (int)arg3;
 			HashMap<String,Object> item =partner_list.get(idx);
 			String pid = (String) item.get("pid");
 			String code_name = (String) item.get("code_name");
 			pop.showPopWindow(code_name, pid);
 		}
 		
 	}
 	
 	private class InteractiveOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
 
 		@Override
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			cur_pos = arg2;
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 		
 	}
 	
 	public void send_cheers(String pid){
 		
 	}
 }
