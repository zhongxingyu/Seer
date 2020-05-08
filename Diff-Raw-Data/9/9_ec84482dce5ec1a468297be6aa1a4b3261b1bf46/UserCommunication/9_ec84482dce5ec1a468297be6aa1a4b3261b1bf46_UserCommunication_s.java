 package com.example.multimodal2;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import multimodal.Constraint;
 import multimodal.schedule.Booking;
 import multimodal.schedule.Room;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Vibrator;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.util.Log;
 import android.widget.Toast;
 
 public class UserCommunication {
 	
 	private static final String OUTPUT_TYPE_QUESTION = "http://imi.org/Question";
 	@SuppressWarnings("unused")
 	private static final String OUTPUT_TYPE_STATEMENT = "http://imi.org/Statement";
 	private static final String OUTPUT_TYPE_YES_NO_QUESTION = "http://imi.org/YesNoQuestion";
 	private static final String OUTPUT_TYPE_REMINDER = "http://imi.org/Reminder";
 	
 	private static final String MODALITY_SPEECH = "http://imi.org/Speech";	
 	private static final String MODALITY_TACTILE = "http://imi.org/Tactile";
 	private static final String MODALITY_MUSIC = "http://imi.org/Music";
 	private static final String MODALITY_LIGHT = "http://imi.org/Light";
 	private static final String MODALITY_SCREEN = "http://imi.org/Screen";
 	
 	private MainActivity ma;
 	
 	private LinkedList<Room> roomList;
 	public String locationContext;
 	private boolean confirm;
 	private Booking currentBooking;
 	private UserInputInterpreter currentCommand;
 
 	public UserCommunication(MainActivity ma) {
 		
 		this.ma = ma;
 		
 			
 		roomList= this.ma.rdfModel.createRoomsFromRDF();
 	}
 	
 	/**
 	 * interprets the input from the user speech input
 	 * @param text the text from the speech recognizer
 	 */
 	public void InputFromUser(String text) {
 		if(this.currentCommand == null) {
 			this.currentCommand = new UserInputInterpreter(text, roomList);
 		}
 		if(currentCommand.command == UserInputInterpreter.CommandType.REMINDER) {
 			if(this.currentCommand.time != null){
 				final Date reminderTime = this.currentCommand.time.getExactStartTime();
 				final MainActivity mainActivity = this.ma;
 				mainActivity.runOnUiThread(new Runnable() {
 				    Date remindAtTime = reminderTime;
 					MainActivity activity = mainActivity;
 					@Override
 					public void run() {
 						while(remindAtTime.getTime()<new Date().getTime()){
 							synchronized (this) {
 								try {
 									this.wait(1000, 0);
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}	
 							}
 						}
 						outputToUser("WAKE UP! THIS IS A REMINDER!", OUTPUT_TYPE_REMINDER);
 					}
 				});
 			}
 		} else
 		if(currentCommand.command == UserInputInterpreter.CommandType.WHEN) {
 			// not implemented
 			// possibly query for a booked room
 		} else 
 		if(currentCommand.command == UserInputInterpreter.CommandType.BOOK) {
 			if(this.confirm) {
 				if(text.contains("yes")) {
 					currentBooking.book();
 					Toast.makeText(this.ma, "Booked!", Toast.LENGTH_LONG).show();
 				}
 				this.confirm = false;
 				this.currentCommand = null;
 				this.currentBooking = null;
 			} else {
 				
 				Room associatedRoom = null;
 				if(currentCommand.associatedRoom != null){
 					associatedRoom = currentCommand.associatedRoom;
 				} else {
 					for(Room room : roomList ) {
 						//TODO constrain rooms based on RDF
 						associatedRoom = room;
 					}
 				}
 
 				Constraint constr = new Constraint();
 				if(currentCommand.time != null){
 					constr.fuzzyTimeConstrain(currentCommand.time);
 				}
 				LinkedList<Booking> possibleBookings = associatedRoom.getPossibleBookings(constr);
 				currentBooking = possibleBookings.getFirst();
 				
 		
 				outputToUser("Do you want to book a meeting in the "+associatedRoom.getSpeechName()+currentBooking.getSpeechStartTime()+"?", OUTPUT_TYPE_YES_NO_QUESTION);	
 			}
 		}
 	}
 
 
 	/**
 	 * gets the best modality for the response to the user based on
 	 * the current locatio (room) and the kind of message that
 	 * should be passed to the user (question, statement, reminder etc)
 	 * 
 	 * @param type the kind of message
 	 * @return a RDF URI describing the best modality to use
 	 */
 	public String getModalitiesForRoom(String type) {
 
 		HashMap<String, Integer> modalities = null;	
 		
 		for(Room room : this.roomList ) {
 			if(room.getName().equals(locationContext)) {
 				modalities = this.ma.rdfModel.getModalityForRoom(room, OUTPUT_TYPE_QUESTION);				
 				break;
 			}
 		}	
 		String modality = null;
 		Integer modalityVal = -1;
 		for(Map.Entry<String, Integer>mod : modalities.entrySet()) {						
 			if(mod.getValue() > modalityVal && !mod.getKey().equals(MODALITY_MUSIC) && !mod.getKey().equals(MODALITY_LIGHT)) {
 				modality = mod.getKey();
 				modalityVal = mod.getValue();
 			}
 		}
 		Log.d(this.getClass().getName(), "chosen modality: " + modality);
 		return modality;
 	}
 	
 	/**
 	 * Sets the context in which the messages should be evaluated.
 	 * @param room the location (room name)
 	 */
 	public void setLocationContext(String room) {
 		this.locationContext = room;
 	}
 	
 	/**
 	 * this method outputs the message to the user using the best available modality
 	 * @param msg the message to output
 	 * @param type the type of message (question, reminder, statement, etc.)
 	 */
 	public void outputToUser(final String msg, String type) {
 		String preferredModality = getModalitiesForRoom(type);
 		if(preferredModality.equals(MODALITY_SPEECH)) {
 			if(type == OUTPUT_TYPE_YES_NO_QUESTION) {
 				this.ma.tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
 			        @Override
 			        public void onUtteranceCompleted(String utteranceId) {
 			        	askForUserSpeechInput(msg);
 			            
 			        }
 			    });
 				this.confirm = true;
 			}	
 			outputToUserByVoice(msg);
 		} else if(preferredModality.equals(MODALITY_SCREEN)) {
 			if(type == OUTPUT_TYPE_YES_NO_QUESTION) {
 				Intent i = new Intent(this.ma, MeetingConfirmation.class);		
 				i.putExtra("booking", currentBooking);
 				this.ma.startActivityForResult(i,3); 
 				this.confirm = true;
 			}			
 		} else if(preferredModality.equals(MODALITY_TACTILE)){
 			Vibrator v = (Vibrator) this.ma.getSystemService(Context.VIBRATOR_SERVICE);
 			v.vibrate(1000);
 		}
 	}
 	
 	/**
 	 * Speech output
 	 * @param msg the message
 	 */
 	private void outputToUserByVoice(String msg) {		
 		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
 		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
 		this.ma.tts.speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
 		Toast.makeText(this.ma, msg, Toast.LENGTH_LONG).show();
 	}
 	
 	/**
 	 * speech input request
 	 */
 	public void askForUserSpeechInput(String msg) {
 		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
 		listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, msg);
 		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
 		this.ma.startActivityForResult(listenIntent, MainActivity.VR_REQUEST);
 	}
 
 }
