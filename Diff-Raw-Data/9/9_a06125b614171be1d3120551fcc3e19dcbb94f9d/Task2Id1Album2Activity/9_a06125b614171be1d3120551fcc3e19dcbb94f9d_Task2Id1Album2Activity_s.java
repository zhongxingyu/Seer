 package hml.paperdeskandroid;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.R.integer;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 
 public class Task2Id1Album2Activity extends Activity {
 
 	int[] photoIds = new int[]
 	{
 			//Todo: add photo in album2;
 	};
 	
 	//this activity has inner class intends broadcast receiver to recive msg from service that comm with pc
 	MyReceiver receiver;
 	
 	
 	//command
 	private String command = "";
 	public static boolean bCommandChanged = false;
 
 	public class MyReceiver extends BroadcastReceiver
 	{
 		public MyReceiver()
 		{
 			
 		}
 		
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			Bundle bundle = intent.getExtras();
 			command = bundle.getString("command");
 			bCommandChanged = true;
 			if(command.startsWith("touch#"))
 			{
 				//move specific photo
 			}
 			else if(command.startsWith("move#"))  //there comes a new photo moved from another album
 			{
 				String tokens[] = command.split("\\#");
 				String infos[] = tokens[1].split("\\:");
 				int sourceAlbumId = Integer.parseInt(infos[0]);
 				int sourcePhotoId = Integer.parseInt(infos[1]);
 				
 				addMovingPhotoToList(sourceAlbumId, sourcePhotoId);
 				updateAlbum();
 				
 			}
 			else if(command.startsWith("taskChooser"))
 			{
 				Intent intentTaskChooser = new Intent();
 				intentTaskChooser.setClass(Task2Id1Album2Activity.this, TaskChooserActivity.class);
 				startActivity(intentTaskChooser);
 				
 				Task2Id1Album2Activity.this.finish();
 			}
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //set full screen
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
         setContentView(R.layout.activity_task2_id1_album2);
         
         fillAlbum();
         updateAlbum();
         
         //receive command
         registerBroadcastReceiver();
     }
 
     public void addMovingPhotoToList(int sourceAlbumId, int sourcePhotoId)
     {
     	int sourcePhotoIds[];
     	if(sourceAlbumId == 0)
     	{
     		sourcePhotoIds = Task2Service.album1;
     	}
     	else
     	{
     		sourcePhotoIds = Task2Service.album2;
 		}
     	
     	int targetPhotoIds[] = new int[photoIds.length + 1];
     	for(int i = 0; i < photoIds.length; ++i)
     	{
     		targetPhotoIds[i] = photoIds[i];
     	}
     	targetPhotoIds[photoIds.length] = sourcePhotoIds[sourcePhotoId];
     	
     	photoIds = targetPhotoIds;
     	
     }
     
     public void updateAlbum()
     {
         List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
         for(int i=0; i < photoIds.length; ++i)
         {
         	Map<String, Object> listItem = new HashMap<String, Object>();
         	listItem.put("photo", photoIds[i]);
         	listItems.add(listItem);
         }
         
         SimpleAdapter simpleAdapter = new SimpleAdapter(this,
         		listItems,
         		R.layout.photo_grid_cell,
         		new String[]{"photo"},
         		new int[]{R.id.image1});
         GridView grid = (GridView)findViewById(R.id.photoGridTask2Id1);
         grid.setAdapter(simpleAdapter);  
     }
     
     public void fillAlbum()
     {
     	if(Task2Service.iSelectedAlbum == 0)
     	{
     		photoIds = Task2Service.album1;
     	}
     	else
     	{
     		photoIds = Task2Service.album2;
 		}
     }
     
     public void registerBroadcastReceiver()
     {
     	receiver = new MyReceiver();
     	IntentFilter filter = new IntentFilter();
     	filter.addAction(KeySimulationSlaveService.receiverSlaveAction);
     	this.registerReceiver(receiver, filter);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_task2_id1_album2, menu);
         return true;
     }
 }
