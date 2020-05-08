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
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AbsoluteLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.SimpleAdapter;
 
 public class Task2Id0Album1Activity extends Activity {
 
 	public boolean bInMoving = false;
 	public int movingX = 0, movingY = 0;
 	public int movingDectedThreshold = 5;
 	public int movingPhotoId = -1;  //index in raw list
 	public int imageIndex;  //index in current list
 	
 	ImageView photoMove;
 	int[] photoIds = new int[]
 	{
 			//Todo: add photo in album1;
 	};
 	int rawPhotoIds[];
 	
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
 			if(command.startsWith("touch#0"))
 			{
 				//move specific photo
 				String tokens[] = command.split("\\#");
 				String infos[] = tokens[1].split("\\:");
 				
 				
 		    	int x = Integer.parseInt(infos[1]);//(int)event.getX();
 		    	int y = Integer.parseInt(infos[2]);//(int)event.getY();
 				
 				if(!bInMoving)
 				{
 					imageIndex = getImageIndexByTouchCoordinate(x, y);
 					if( imageIndex != -1)
 					{
 						movingPhotoId = 0;
 						//movingX = x;
 						//movingY = y;
 						
 						//find raw image index using imageIndex
 						for(int i = 0; i < rawPhotoIds.length; ++i)
 						{
 							if(rawPhotoIds[i] == photoIds[imageIndex])
 							{
 								movingPhotoId = i;
 								break;
 							}
 						}
 
 						bInMoving = true;
 						Task2Service.bStartMoving = true;
 						photoMove.setImageResource(photoIds[imageIndex]);
 						photoMove.setVisibility(View.VISIBLE);
 					}	
 				}
 				else
 				{
 					if(Math.abs(x - movingX) > movingDectedThreshold && Math.abs(y - movingY) > movingDectedThreshold)
 					{
 						movingX = x;
 						movingY = y;
 						photoMove.setLayoutParams(
 								new AbsoluteLayout.LayoutParams(320, 320, (int)movingX-160, (int)movingY-160));
 					}
 					
 					if(movingX < 100/*out from left*/ || movingX > 850/*out from right*/)
 					{
 							
 						photoMove.setImageResource(0);
 						photoMove.setVisibility(View.INVISIBLE);
 						photoMove.setLayoutParams(
 								new AbsoluteLayout.LayoutParams(320, 320, 480, 240));
 						movingX = 480;
 						movingY = 480;
 						
 						//Create command to slave display
 						String command = "";
 						
 						command = "move#" + Task2Service.iSelectedAlbum + ":" + movingPhotoId +  ":" + movingX + ":" + movingY;
         				MainActivity.clientCommandChanged[1] = true;
 						MainActivity.clientCommand[1] = command +"\n";
 						
 						removeMovingPhotoFromList();
 						updateAlbum();
 						
 						bInMoving = false;
 					}
 				}
 				
 			}
 			else if(command.startsWith("taskChooser"))
 			{
 				Intent intentTaskChooser = new Intent();
 				intentTaskChooser.setClass(Task2Id0Album1Activity.this, TaskChooserActivity.class);
 				startActivity(intentTaskChooser);
 				
 				Task2Id0Album1Activity.this.finish();
 			}
 			else if(command.startsWith("zone#0:hot") && !Task2Service.bStartMoving)
 			{
 				Task2Service.iCurrentZone = Task2Service.Zone.Hot; //hot zone
 				
 				//start the book activity
 				Intent intentBook = new Intent();
 				intentBook.setClass(Task2Id0Album1Activity.this, Task2Id0PhotoDetailActivity.class);
 				
 				startActivity(intentBook);
 				Task2Id0Album1Activity.this.finish();
 			}
 		}
 	}
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //set full screen
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         setContentView(R.layout.activity_task2_id0_album1);
         
         photoMove = (ImageView)findViewById(R.id.imageViewTask2MovePhoto);
         photoMove.setVisibility(View.INVISIBLE);
         
         fillAlbum();
         updateAlbum();  //set adapter
             
         //receive command
         registerBroadcastReceiver();
     }
 
     	
 //    public boolean dispatchTouchEvent(MotionEvent event)
 //    {
 //    	int x = (int)event.getX();
 //    	int y = (int)event.getY();
 //    	
 //    	try {
 //    		
 //    		switch(event.getAction())
 //    		{
 //	    		case MotionEvent.ACTION_DOWN:
 //	    		case MotionEvent.ACTION_MOVE:
 //					if(!bInMoving)
 //					{
 //						imageIndex = getImageIndexByTouchCoordinate(x, y);
 //						if( imageIndex != -1)
 //						{
 //							movingPhotoId = 0;
 //							//movingX = x;
 //							//movingY = y;
 //							
 //							//find raw image index using imageIndex
 //							for(int i = 0; i < rawPhotoIds.length; ++i)
 //							{
 //								if(rawPhotoIds[i] == photoIds[imageIndex])
 //								{
 //									movingPhotoId = i;
 //									break;
 //								}
 //							}
 //
 //							bInMoving = true;
 //							Task2Service.bStartMoving = true;
 //							photoMove.setImageResource(photoIds[imageIndex]);
 //							photoMove.setVisibility(View.VISIBLE);
 //						}	
 //					}
 //					else
 //					{
 //						if(Math.abs(x - movingX) > movingDectedThreshold && Math.abs(y - movingY) > movingDectedThreshold)
 //						{
 //							movingX = x;
 //							movingY = y;
 //							photoMove.setLayoutParams(
 //									new AbsoluteLayout.LayoutParams(320, 320, (int)movingX-160, (int)movingY-160));
 //						}
 //						
 //						if(movingX < 60/*out from left*/ || movingX > 900/*out from right*/)
 //						{
 //							bInMoving = false;
 //							
 //							photoMove.setImageResource(0);
 //							photoMove.setVisibility(View.INVISIBLE);
 //							
 //							//Create command to slave display
 //							String command = "";
 //							
 //							command = "move#" + Task2Service.iSelectedAlbum + ":" + movingPhotoId +  ":" + movingX + ":" + movingY;
 //            				MainActivity.clientCommandChanged[1] = true;
 //    						MainActivity.clientCommand[1] = command +"\n";
 //							
 //							removeMovingPhotoFromList();
 //							updateAlbum();
 //						}
 //					}
 //    		}
 //    		
 //    		
 //		} catch (Exception e) {
 //			// TODO: handle exception
 //		}
 //    	
 //    	return false;
 //    }
 
     
     public void removeMovingPhotoFromList() //After moving photo, we need to remove that one from album
     {
     	int[] tempPhotoIds = new int[photoIds.length - 1];
     	
     	for(int i = 0; i < imageIndex; ++i)
     	{
     		tempPhotoIds[i] = photoIds[i];
     	}
     	
     	for(int i = imageIndex; i < photoIds.length - 1; ++i)
     	{
     		tempPhotoIds[i] = photoIds[i+1];
     	}
     	
     	photoIds = tempPhotoIds;
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
         GridView grid = (GridView)findViewById(R.id.photoGridTask2Id0);
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
     	
     	rawPhotoIds = photoIds;
     }
     
     public int getImageIndexByTouchCoordinate(int x, int y)  //x, y are all by pixel, return -1 if no photo in touch place
     {
     	if(y >= 0 && y < 320)  //first line
     	{
     		if(x >= 0 && x < 320)
     		{
     			return 0;
     		}
     		else if(x >= 320 && x < 640)
     		{
     			return 1;
     		}
     		else if(x > 640 && x < 960)
     		{
     			return 2;
     		}
     	}
     	else if(y >= 320 && y < 640)
     	{
     		if(x >= 0 && x < 320)
     		{
     			return 3;
     		}
     		else if(x >= 320 && x < 640)
     		{
     			return 4;
     		}
     		else if(x > 640 && x < 960)
     		{
     			return 5;
     		}
     	}
     	else
     	{
     		return -1;
     	}
     	
     	return -1;
     }
     
     
     public void registerBroadcastReceiver()
     {
     	receiver = new MyReceiver();
     	IntentFilter filter = new IntentFilter();
     	filter.addAction(KeySimulationService.receiverAction);
     	this.registerReceiver(receiver, filter);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_task2_id0_album1, menu);
         return true;
     }
 }
