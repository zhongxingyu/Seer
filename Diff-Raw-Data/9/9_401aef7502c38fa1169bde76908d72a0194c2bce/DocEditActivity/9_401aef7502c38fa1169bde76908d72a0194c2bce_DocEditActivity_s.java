 package com.example.collabtext;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.content.Intent;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import edu.umich.imlc.android.common.Utils;
 import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
 import edu.umich.imlc.collabrify.client.CollabrifyClient;
 import edu.umich.imlc.collabrify.client.CollabrifyListener;
 import edu.umich.imlc.collabrify.client.CollabrifySession;
 import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
 
 import com.example.collabtext.CollabTextProto.globalMove;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 
 public class DocEditActivity extends Activity {
 	
 	private int idCount = 0;
	private int clock = -1;
 	
 	private User notepad;
 	
 	private static final Level LOGGING_LEVEL = Level.ALL;
 	private static String TAG = "collabText";
 	private CollabrifyClient myClient;
 	//private CheckBox withBaseFile;
 	private CollabrifyListener collabrifyListener;
 	private long sessionId;
 	private String sessionName;
 	private ByteArrayInputStream baseFileBuffer;
 	private ByteArrayOutputStream baseFileReceiveBuffer;
 	private ArrayList<String> tags = new ArrayList<String>();
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState){
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_doc_edit);
         EditText ref = (EditText) findViewById(R.id.editText1);
         notepad = new User(ref, this);
         
         // enable logging
 	    Logger.getLogger("edu.umich.imlc.collabrify.client").setLevel(LOGGING_LEVEL);
 	    
 	    tags.add("default");
 	    
 	    collabrifyListener = new CollabrifyAdapter(){
 		    @Override
 		    public void onReceiveSessionList(final List<CollabrifySession> sessionList)
 		    {
 		      boolean exist = false;
 		      int clock = 0;
 		      for(CollabrifySession s : sessionList)
 		      {
 		        if(s.name().equals("C0llabT3xt23191")){
 		        	exist = true;
 		        	break;
 		        }
 		        clock++;
 		      }
 		      
               try
               {
             	  if(!exist){
             		  try
             	        {
             	          sessionName = "C0llabT3xt23191"; //name hardcoded for now            	         
             	          myClient.createSession(sessionName, tags, null, 0);
             	          Log.i(TAG, "Session name is " + sessionName);
             	        }
             	        catch( CollabrifyException e ){
             	        		Log.e(TAG, "error", e);
             	        }
             	  }
             	  else{
             		  sessionId = sessionList.get(clock).id();
             		  sessionName = sessionList.get(clock).name();
             		  myClient.joinSession(sessionId, null);
             	  }
               }
               catch( CollabrifyException e ){Log.e(TAG, "error", e);}
 		    }
 		    
 		    @Override
 		      public void onDisconnect()
 		      {
 		        Log.i(TAG, "disconnected");
 		        runOnUiThread(new Runnable()
 		        {
 		        	
 		          @Override
 		          public void run(){}
 		        });
 		      }
 		    
 		    @Override
 		      public void onReceiveEvent(final long orderId, int subId, String eventType, final byte[] data)
 		      {
 		        Utils.printMethodName(TAG);
 		        Log.d(TAG, "RECEIVED SUB ID:" + subId);
 		        runOnUiThread(new Runnable()
 		        {
 		          @Override
 		          public void run()
 		          {
 		            Utils.printMethodName(TAG);
 
 		            globalMove message;
 					try {
 						message = globalMove.parseFrom(data);
						if(message.getId() == (clock + 1))
 						{
 							notepad.updateLocal(message.getLocation(), message.getLength(), message.getText(), message.getDelete());
							clock++;
 						}
 					} catch (InvalidProtocolBufferException e) {
 						Log.e(TAG, "error", e);
 					}
 
 		          }
 		        });
 		      }
 		    @Override
 		      public void onSessionCreated(long id)
 		      {
 		        Log.i(TAG, "Session created, id: " + id);
 		        sessionId = id;
 		        runOnUiThread(new Runnable()
 		        {
 		          @Override
 		          public void run(){}
 		        });
 		      }
 		        
 		    @Override
 		      public void onError(CollabrifyException e){Log.e(TAG, "error", e);}
 		    
 		    @Override
 		      public void onSessionJoined(long maxOrderId, long baseFileSize)
 		      {
 		        Log.i(TAG, "Session Joined");
 		        if(baseFileSize > 0)
 		        {
 		          //initialize buffer to receive base file
 		          baseFileReceiveBuffer = new ByteArrayOutputStream((int) baseFileSize);
 		        }
 		        runOnUiThread(new Runnable()
 		        {
 
 		          @Override
 		          public void run(){}
 		        });
 		      }
 		    
 		    @Override
 		      public byte[] onBaseFileChunkRequested(long currentBaseFileSize)
 		      {
 		        // read up to max chunk size at a time
 		        byte[] temp = new byte[CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE];
 		        int read = 0;
 		        try
 		        {
 		          read = baseFileBuffer.read(temp);
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		        if( read == -1 )
 		        {
 		          return null;
 		        }
 		        if( read < CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE )
 		        {
 		          // Trim garbage data
 		          ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		          bos.write(temp, 0, read);
 		          temp = bos.toByteArray();
 		        }
 		        return temp;
 		      }
 		    
 		    @Override
 		      public void onBaseFileChunkReceived(byte[] baseFileChunk)
 		      {
 		        try
 		        {
 		          if( baseFileChunk != null )
 		          {
 		            baseFileReceiveBuffer.write(baseFileChunk);
 		          }
 		          else
 		          {
 		            runOnUiThread(new Runnable()
 		            {
 		              @Override
 		              public void run()
 		              {
 		            	  //textViewer.setText(baseFileReceiveBuffer.toString());
 		              }
 		            });
 		            baseFileReceiveBuffer.close();
 		          }
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		      }
 		    
 		    @Override
 		      public void onBaseFileUploadComplete(long baseFileSize)
 		      {
 		        runOnUiThread(new Runnable()
 		        {
 
 		          @Override
 		          public void run()
 		          {
 		        	  //textViewer.setText(sessionName);
 		          }
 		        });
 		        try
 		        {
 		          baseFileBuffer.close();
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		      }
 	    };
 	    
 	    //create or join session
 	   
 	    boolean getLatestEvent = false; 
 	    //true == only get latest update (used for models where whole file is sent at once.
 	    //false allows it to grab all updates.
 
 	    // Instantiate client object
 	    try
 	    {
 	      myClient = new CollabrifyClient(this, "user email", "user display name",
 	          "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
 	          collabrifyListener);
 	    
 	      Log.i(TAG, "myclient Created");
 	      
 	    }
 	    catch( CollabrifyException e )
 	    {
 	      e.printStackTrace();
 	    } 
 	    
 		try
 	    {
 	      myClient.requestSessionList(tags); //grabs list of available sessions & opens C0llabT3xt2319
 	    }
 	    catch( Exception e ){Log.e(TAG, "error", e);}
     
         
 	}
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.doc_edit, menu);
         return true;
     }
     
     public void undo(MenuItem menu){
     	notepad.undo();
     }
     
     public void redo(MenuItem menu){
     	notepad.redo();
     }
     
     public void backToMain(MenuItem menu){
     	Intent i = new Intent(this, MainActivity.class);
     	leaveSession();
 		startActivity(i);
     }
     
     public void broadcastMessage(Boolean delete, int location, String text){
     
     	if( myClient != null && myClient.inSession() ) //if in session
         {
           try
           {
         	globalMove message = 
         			globalMove.newBuilder()
         			.setDelete(false)
         			.setId(idCount)
         			.setLocation(0)
         			.setLength(text.length())
         			.setText(text)
         			.build();
         	
         	idCount++;
         	
             myClient.broadcast(message.toByteArray(), "lol");
           }
           catch( CollabrifyException e ){Log.e(TAG, "error", e);}
         }
     	
     }
     
     public void leaveSession(){
     	try
         {
           if(myClient.inSession())
             myClient.leaveSession(false); //if true, deletes session when owner leaves
         }
         catch( CollabrifyException e ){Log.e(TAG, "error", e);}
     }
     
     
     
 }
 
