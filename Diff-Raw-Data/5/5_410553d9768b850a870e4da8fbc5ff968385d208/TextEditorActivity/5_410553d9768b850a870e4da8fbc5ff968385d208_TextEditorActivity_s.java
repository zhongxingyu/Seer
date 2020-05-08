 package umich.eecs441.project;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import umich.eecs441.project.proto.CursorCommandBuf.CursorCommandBufObj;
 import umich.eecs441.project.proto.InsertCommandBuf.InsertCommandBufObj;
 import umich.eecs441.project.proto.RedoCommandBuf.RedoCommandBufObj;
 import umich.eecs441.project.proto.RemoveCommandBuf.RemoveCommandBufObj;
 import umich.eecs441.project.proto.UndoCommandBuf.UndoCommandBufObj;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
 import edu.umich.imlc.collabrify.client.exceptions.ConnectException;
 public class TextEditorActivity extends Activity
 							    implements EventAccessible {
 	
 	
 	// waitingDialog
 	private ProgressDialog waitingDialog;
 		
 	
 	private boolean guard;
 
 	private Button undoButton;
 	private Button redoButton;
 	private Button leaveButton;
 	private Button terminateButton;
 	private static CursorWatcher editText;
 	
 	// for the timer
 	// string buffer, recording the char
 	private static String buffer;
 	// keep track of the last command
 	private static String lastCommand;
 	// the time
 	private static long startTime;
 	
 	
 	private HashMap <Long, Integer> recoverMap;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		recoverMap = new HashMap<Long, Integer> ();
 		
 		
 		// initialize
 		buffer = "";
 		lastCommand = "";
 		
 		setContentView(R.layout.text_editor_screen);
 		editText = (CursorWatcher) this.findViewById(R.id.txtMessage);
 		undoButton = (Button) this.findViewById(R.id.button2);
 		redoButton = (Button) this.findViewById(R.id.button1);
 		leaveButton = (Button)this.findViewById(R.id.button3);
 		terminateButton = (Button)this.findViewById(R.id.button4);
 		
 		// set editorActivity
 		OnlineClient.getInstance().setEditorActivity(TextEditorActivity.this);
 		
 		
 		if (MainActivity.createNewSession){
 			// create new session
 			Log.i("TextEditorActivity onCreate", "get sessionName  " + MainActivity.getSessionName());
 			Log.i("TextEditorActivity onCreate", "get Maximum people  " + String.valueOf(MainActivity.getUserUpperLimit()));
 			Log.i("TextEditorActivity onCreate", "get base file" + MainActivity.getBaseFileStr());
 		
 			if (MainActivity.getBaseFileStr().equals("") || MainActivity.getBaseFileStr() == null) {
 				// TODO: getUserUpperLimit
 				List<String> temp = new ArrayList<String>();
 				temp.add("pass");
 				try {
 					Log.i("createSession", "no base file");
 					OnlineClient.getInstance().getClient().createSession(MainActivity.getSessionName(), temp, null, MainActivity.getUserUpperLimit());
 					Log.i("createSession clientID", String.valueOf(OnlineClient.getInstance().getClientID()));
 				} catch (ConnectException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (CollabrifyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				waitingDialog = ProgressDialog.show(TextEditorActivity.this, "Waiting...", "Creating Session");
 			} else {
 				String baseFile = MainActivity.getBaseFileStr();
 				editText.setText(baseFile);
 				List<String> temp = new ArrayList<String>();
 				temp.add("pass");
 				Log.i("createSession", "with base file");
 				Log.i("base file", baseFile);
 				OnlineClient.getInstance().iniInputBuffer(baseFile);
 				try {
 					OnlineClient.getInstance().getClient().createSessionWithBase(MainActivity.getSessionName(), temp, null, MainActivity.getUserUpperLimit());
 				} catch (ConnectException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (CollabrifyException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				// start uploading
 				waitingDialog = ProgressDialog.show(TextEditorActivity.this, "Waiting...", "Creating Session", true);
 			}
 		} else {
 			try {
 				OnlineClient.getInstance().getClient().joinSession(MainActivity.getSessionId(), null);
 			} catch (ConnectException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (CollabrifyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			waitingDialog = ProgressDialog.show(TextEditorActivity.this, "Waiting...", "Initialize file", true);
 			
 		}
 		
 		
 		
 		
 		// timer thread
 		new Thread () {
 			public void run() {
 				startTime = System.currentTimeMillis();
 				while(true) {
 					if (System.currentTimeMillis() - startTime >= 800) {
 						startTime = System.currentTimeMillis();
 						timeUp();
 					}
 				}
 				
 			}
 		}.start();
 		
 		undoButton.setOnClickListener(new Button.OnClickListener () {
 			public void onClick (View arg0) {
 				// if there is action (not undo) in the stack
 				if (OnlineClient.getInstance().getCommandStackContains() != 0){
 					
 					Log.i("undoButton onClick", "getCommandStackContains: " + String.valueOf(OnlineClient.getInstance().getCommandStackContains()));
 					
 					OnlineClient.getInstance().setCommandStackContains(
 							OnlineClient.getInstance().getCommandStackContains() - 1);
 					OnlineClient.getInstance().setRedoListContains(
 							OnlineClient.getInstance().getRedoListContains() + 1);
 					
 					Log.i("undoButton onClick", "getCommandStackContains after: " + String.valueOf(OnlineClient.getInstance().getCommandStackContains()));
 					
 					// TODO: Should check if the command is confirmed or not, change undo function in CommandManager
 					changeCommand("Undo", "");
 					AbstractCommand cmd = new UndoCommand ();
 					cmd.execute();
 					
 			/*		if (cmd.getClient() == Client.getInstance().getClient()) {
 						Log.i("undo button clicked", "  ");*/
 //						editText.setSelection(CursorTrack.getInstance().getCursor(Client.getInstance().getClient()));
 					/*}*/
 				}
 			}
 
 		});
 		
 		redoButton.setOnClickListener(new Button.OnClickListener () {
 
 			@Override
 			public void onClick(View arg0) {
 				// if there is action in the list
 				if (OnlineClient.getInstance().getRedoListContains() != 0) {
 					OnlineClient.getInstance().setCommandStackContains(
 							OnlineClient.getInstance().getCommandStackContains() + 1);
 					OnlineClient.getInstance().setRedoListContains(
 							OnlineClient.getInstance().getRedoListContains() - 1);
 					
 					changeCommand("redo", "");
 					AbstractCommand cmd = new RedoCommand ();
 					cmd.execute();
 					
 					// It should run when the client receive the redo request
 					// move it into receive client
 //					CommandManager.getInstance().redo(cmd);
 //					if (cmd.getClient() == Client.getInstance().getClient()) {
 //						editText.setSelection(CursorTrack.getInstance().getCursor(Client.getInstance().getClient()));
 //					}
 				}
 			}
 			
 		});
 		
 		
 		
 		editText.setOnClickListener(new EditText.OnClickListener () {
 
 			@Override
 			public void onClick(View arg0) {
 				Log.i("EditText", "Trigger");
 				
 				// might need to send request for the last command
 				changeCommand("Move", "");
 				startTime = System.currentTimeMillis();
 				Log.i("editText setOnClickListener", String.valueOf(CursorTrack.getInstance().getCursorMap().size()));
 				Log.i("editText setOnClickListener", String.valueOf(OnlineClient.getInstance().getClient().currentSessionParticipantId()));
 				int movement = editText.getSelectionEnd() 
 						- CursorTrack.getInstance().getCursor(OnlineClient.getInstance().getClientID());
 				// unconfirmed operation
 				if (movement != 0) {
 					// TODO constructor with client argument might be expected
 					// add commandStackContains by 1 to indicate the client has one action
 					AbstractCommand cmd = new CursorCommand(movement);
 					
 					Log.i("setOnClickListener CommandStackContains", String.valueOf(OnlineClient.getInstance().getCommandStackContains()));
 					
 					//!!!! execute should be ahead of the store since it will find storecommand
 					cmd.execute();
 					CommandManager.getInstance().storeCommand(cmd);
 					// when there is a new command executed, we have to tell that the client
 					// can undo no matter if it is confirmed, so 
 					OnlineClient.getInstance().setCommandStackContains(
 							OnlineClient.getInstance().getCommandStackContains() + 1);
 					OnlineClient.getInstance().setRedoListContains(0);
 					//!!!! two number in Client is just for track the very instant operation of the client
 					// doesnt influence the real stack, where the change occurs when the command come back
 					
 					// Since it is not collabrify, just do as if we get the response
 					// clear
 					
 					// when received an event
 					// just pass the client ID
 //					CommandManager.getInstance().newCommandHandling(Client.getInstance().getClient());
 					// TODO when receive a command, put the command into the stack, also check if it is confirmed or not
 					
 				}
 			}
 			
 		});
 
 		
 		
 		
 		
 		leaveButton.setOnClickListener(new Button.OnClickListener () {
 			public void onClick (View v) {
 				LinearLayout layout = new LinearLayout(TextEditorActivity.this);
 				layout.setOrientation(LinearLayout.VERTICAL);
 				
 				final AlertDialog.Builder alertDialogbuilder = new AlertDialog.Builder(TextEditorActivity.this);
 				alertDialogbuilder.setTitle("Leave Session");
 				alertDialogbuilder.setMessage("Are you sure to leave the session?");
 				
 				alertDialogbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			            // TODO: leave the session, back to the main menu. !!how about the session's creator?
 			        	try {
 			        		if (OnlineClient.getInstance().getClient().inSession()) {
 			        			OnlineClient.getInstance().getClient().leaveSession(false);
 			        			Intent intent = new Intent();
 			    				intent.setClass(TextEditorActivity.this, MainActivity.class);
 			    				startActivity(intent);
 			    				TextEditorActivity.this.finish();
 			        		}
 			        	} catch (CollabrifyException e) {
 			        		e.printStackTrace();
 			        	}
 			        	
 			        }
 			     });
 				
 			    alertDialogbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			            // TODO: back to the texteditor screen
 			        }
 			     });
 			    alertDialogbuilder.setView(layout);
 			    final AlertDialog alertDialog = alertDialogbuilder.create();
 			    alertDialog.show();
 			}
 		
 		});
 		
 		terminateButton.setOnClickListener(new Button.OnClickListener () {
 			public void onClick (View v) {
 				LinearLayout layout = new LinearLayout(TextEditorActivity.this);
 				layout.setOrientation(LinearLayout.VERTICAL);
 				
 				final AlertDialog.Builder alertDialogbuilder = new AlertDialog.Builder(TextEditorActivity.this);
 				alertDialogbuilder.setTitle("Leave Session");
 				alertDialogbuilder.setMessage("Are you sure to leave the session?");
 				
 				alertDialogbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			            // TODO: should first check whether the current user is the creator. if not should pop up
 			        	// 		warning window and back to the session. otherwise, should terminate the session and
 			        	//		back to the main menu.
 			        	try {
 			        		if (OnlineClient.getInstance().getClient().inSession() && 
 			        				OnlineClient.getInstance().getClient().currentSessionOwner().getId() == OnlineClient.getInstance().getClientID()) {
 			        			OnlineClient.getInstance().getClient().leaveSession(true);
 			        			Intent intent = new Intent();
 			    				intent.setClass(TextEditorActivity.this, MainActivity.class);
 			    				startActivity(intent);
 			    				TextEditorActivity.this.finish();
 			        		} else {
 			        			 Toast toast = Toast.makeText(TextEditorActivity.this, "You are not allowed to terminate this session", Toast.LENGTH_LONG);
 								 toast.show();
 			        		}
 			        	} catch (CollabrifyException e) {
 			        		e.printStackTrace();
 			        	}		        	
 			        	
 			        }
 			     });
 				
 			    alertDialogbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			        	// TODO:should first check whether the current user is the creator. if not should pop up
 			        	// 		warning window and back to the session. otherwise, should back to the texteditor
 			        	// 		screen.
 			        }
 			     });
 			    alertDialogbuilder.setView(layout);
 			    final AlertDialog alertDialog = alertDialogbuilder.create();
 			    alertDialog.show();
 			}
 		
 		});
 		
 		
 				
 		TextWatcher textWatcher = new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				
 			}
 			
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				if (count > after) {
 					String change = s.toString().substring(start, start + count - after);
 					
 					
 					Log.i("TextEditorActivity beforeTextChanged recoverMap", "Map size: " + String.valueOf(recoverMap.size()));
 					
 					for (Map.Entry<Long, Integer> entry : CursorTrack.getInstance().getCursorMap().entrySet()) {
 						 if (entry.getValue() == CursorTrack.getInstance().getCursorMap().get(OnlineClient.getInstance().getClientID())) {
 							 if (!recoverMap.containsKey(entry.getKey()))
 								 recoverMap.put(entry.getKey(), entry.getValue());
 						 }
 					}
 					
 					Log.i("TextEditorActivity beforeTextChanged recoverMap", "Map size: " + String.valueOf(recoverMap.size()));
 					
 					Log.i("@ TextEditor afterTextChanged is calling CursorTrack's moveLeft", "Client: "+ OnlineClient.getInstance().getClientID() 
 							+ "move left: " + String.valueOf(1));
 					
 					CursorTrack.getInstance().moveLeft(OnlineClient.getInstance().getClientID(), 1);
 					Log.i("TextEditor onTextChange beforeTextChanged", change);
 					changeCommand("Delete", change);
 					startTime = System.currentTimeMillis();	
 //					CommandManager.getInstance().newCommandHandling(Client.getInstance().getClient());
 				}
 				
 				
 				
 			}
 			
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 					
 				if (count > before) {
 					
 					/*Client.getInstance().setCommandStackContains(
 							Client.getInstance().getCommandStackContains() + 1);
 					Log.i("onTextChanged triggered, CharSequence", s.toString());
 					Log.i("onTextChanged triggered, Current Edit Text", editText.getText().toString());
 					Log.i("onTextChanged triggered, start", String.valueOf(start));
 					Log.i("onTextChanged triggered, count", String.valueOf(count));
 					Log.i("onTextChanged triggered, before", String.valueOf(before));
 					String change = s.toString().substring(start, start + count - before);
 					Log.i("onTextChanged triggered, string change", change);
 					AbstractCommand cmd = new InsertCommand(change, editText);
 					CommandManager.getInstance().storeCommand(cmd);
 					cmd.execute();
 					
 					// when there is a new command executed, we have to tell that the client
 					// can undo no matter if it is confirmed, so 
 					Client.getInstance().setCommandStackContains(
 							Client.getInstance().getCommandStackContains() + 1);
 					Client.getInstance().setRedoListContains(0);
 					//!!!! two number in Client is just for track the very instant operation of the client
 					// doesnt influence the real stack, where the change occurs when the command come back
 					*/
 					// Since it is not collabrify, just do as if we get the response
 					// clear
 					
 					
 					String change = s.toString().substring(start, start + count - before);
 					
 					Log.i("@ TextEditor onTextChanged is calling CursorTrack's moveRight", "Client: "+ OnlineClient.getInstance().getClientID() 
 							+ "move right: " + String.valueOf(1));
 					CursorTrack.getInstance().moveRight(OnlineClient.getInstance().getClientID(), 1);
 					changeCommand("Insert", change);
 					startTime = System.currentTimeMillis();
 //					CommandManager.getInstance().newCommandHandling(Client.getInstance().getClient());
 				}
 			}
 		};
 		
 		editText.setTextWatcher(textWatcher);
 		editText.addTextChangedListener(editText.getTextWatcher());
 		
 	}
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * if time up, then according to the lastcommand, flush the buffer and send the request
 	 */
 	private synchronized void timeUp() {
 		if (buffer != "") {
 			
 			Log.i("TextEditorActivity timeUp", "entered!");
 			if (lastCommand.equals("Insert")) {
 				Log.i("TextEditorActivity timeUp lastCommand is insert", buffer);
 				AbstractCommand cmd = new InsertCommand(buffer);
 				// notice that the execute should be ahead of the storecommand to set the command id.
 				cmd.execute();
 				CommandManager.getInstance().storeCommand(cmd);
 				OnlineClient.getInstance().setCommandStackContains(
 						OnlineClient.getInstance().getCommandStackContains() + 1);
 				OnlineClient.getInstance().setRedoListContains(0);
 				// since the local client run out of time, pass the command and say there is a new command, empty redolist
 				CommandManager.getInstance().newCommandHandling((int)OnlineClient.getInstance().getClientID());
 
 			} else {
 				Log.i("TextEditorActivity timeUp lastCommand is remove", buffer);
 				// reverse the buffer string
 				/*String temp ="";
 				for (int i = temp.length() - 1; i >= 0; i++) {
 					temp.concat(buffer.substring(i, i+1));
 				}*/
 				String temp = new StringBuilder(buffer).reverse().toString();
 				Log.i("TextEditorActivity timeUp", "temp: " + temp);
 				AbstractCommand cmd = new RemoveCommand(temp, recoverMap);
 				recoverMap.clear();
 				
 				Log.i("@ TextEditorActivity timeUp", "TrackMap size: " + String.valueOf(cmd.getTrackMapSize()));
 				
 				cmd.execute();
 				CommandManager.getInstance().storeCommand(cmd);
 				OnlineClient.getInstance().setCommandStackContains(
 						OnlineClient.getInstance().getCommandStackContains() + 1);
 				OnlineClient.getInstance().setRedoListContains(0);
 				CommandManager.getInstance().newCommandHandling((int)OnlineClient.getInstance().getClientID());
 
 			}
 		}
 		buffer = "";
 	}
 	// TODO: command change from insert to move cursor
 	// I am considering to pass a "" as newChar
 	private synchronized void changeCommand(String command, String newChar) {
 		if (buffer != "" && !lastCommand.equals(command)) {
 			Log.i("changeCommand command changed", command);
 			if (lastCommand.equals("Insert")) {
 				AbstractCommand cmd = new InsertCommand(buffer);
 				cmd.execute();	
 				CommandManager.getInstance().storeCommand(cmd);			
 				OnlineClient.getInstance().setCommandStackContains(
 						OnlineClient.getInstance().getCommandStackContains() + 1);
 				OnlineClient.getInstance().setRedoListContains(0);
 				CommandManager.getInstance().newCommandHandling((int)OnlineClient.getInstance().getClientID());
 			} else {
 				// reverse the buffer string
				String temp = "";
				for (int i = temp.length() - 1; i >= 0; i++) {
					temp += buffer.charAt(i);
				}
 				AbstractCommand cmd = new RemoveCommand(temp, recoverMap);
 				recoverMap.clear();
 				
 				Log.i("@ TextEditorActivity changeCommand", "TrackMap size: " + String.valueOf(cmd.getTrackMapSize()));
 				
 				cmd.execute();
 				CommandManager.getInstance().storeCommand(cmd);				
 				OnlineClient.getInstance().setCommandStackContains(
 						OnlineClient.getInstance().getCommandStackContains() + 1);
 				OnlineClient.getInstance().setRedoListContains(0);
 				CommandManager.getInstance().newCommandHandling((int)OnlineClient.getInstance().getClientID());
 
 			}
 			buffer = "";
 		}
 		lastCommand = command;
 		buffer = buffer + newChar;
 		Log.i("changeCommand append", buffer);
 	}
 	
 	
 	public void dismissWaitingDialog () {
 		waitingDialog.dismiss();
 		// when new client come addClient
 		CursorTrack.getInstance().addClient(OnlineClient.getInstance().getClientID());
 		RedoTrack.getInstance().addClient(OnlineClient.getInstance().getClientID());
 		Log.i("TextEditorActivity warmup", String.valueOf(CursorTrack.getInstance().getCursorMap().size()));
 		Log.i("TextEditorActivity warmup local cursor", String.valueOf(CursorTrack.getInstance().getCursor(OnlineClient.getInstance().getClientID())));
 		Log.i("TextEditorActivity client ID", String.valueOf(OnlineClient.getInstance().getClientID()));
 		
 	}
 	
 	public void setEditorText (final String text) {
 		Log.i("setEditorText", "entered!");
 		TextEditorActivity.this.runOnUiThread(new Runnable () {
 			@Override
 			public void run() {
 				editText.removeTextChangedListener(editText.getTextWatcher());
 				// TODO Auto-generated method stub
 				editText.setText(text);
 				editText.addTextChangedListener(editText.getTextWatcher());
 			}
 		});
 		dismissWaitingDialog();
 	}
 	
 	
 	public void eventReceived(final AbstractCommand cmd)  {
 				
 		// commmand is constructed
 		// first receive command
 		// flush redoList
 		//
 		TextEditorActivity.this.runOnUiThread(new Runnable(){
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				try {
 					CommandManager.getInstance().receiveCommand(cmd);
 					// if it is not redo and undo
 					if (!(cmd instanceof UndoCommand) && !(cmd instanceof RedoCommand)) {
 						CommandManager.getInstance().newCommandHandling(cmd.getClient());
 					}
 						
 				} catch (NullPointerException e) {
 					e.printStackTrace();
 				}
 				editText.setSelection(CursorTrack.getInstance().getCursor(OnlineClient.getInstance().getClientID()));	
 			
 			}
 			
 		});
 	}
 	public static CursorWatcher getCursorWatcher() {
 		return editText;
 	}
 	
 	public boolean getGuard() {
 		return guard;
 	}
 	public void setGuard(boolean val) {
 		guard = val;
 	}
 }
 
 
 
 
 
 
