 package pro.trousev.cleer.android.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pro.trousev.cleer.Database;
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Player;
 import pro.trousev.cleer.Player.PlayerChangeEvent;
 import pro.trousev.cleer.Player.Status;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Queue.EnqueueMode;
 import pro.trousev.cleer.android.AndroidMessages;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRequestMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRespondMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceTaskMessage;
 import pro.trousev.cleer.android.Constants;
 import pro.trousev.cleer.android.service.MediaScanner.MediaScannerException;
 import pro.trousev.cleer.sys.QueueImpl;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.util.Log;
 
 //TODO Make notification and foreground job
 public class AndroidCleerService extends Service {
 	private static ServiceRespondMessage respondMessage = new ServiceRespondMessage();
 
 	private Queue queue = null;
 	private Player player;
 	//FIXME delete that kostil
 	List<Item> itemList;
 	private Database database = null;
 
 	// Binder allow us get Service.this from the Activity
 	public class CleerBinder extends Binder {
 		public AndroidCleerService getService() {
 			return AndroidCleerService.this;
 		}
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		player = new PlayerAndroid();
 		queue = new QueueImpl(player);
 		database = new DatabaseImpl(Constants.DATABASE_PATH, getApplicationContext());
 		Messaging.subscribe(AndroidMessages.ServiceRequestMessage.class,
 				new Messaging.Event() {
 
 					@Override
 					public void messageReceived(Message message) {
 						// TODO end implementation of that event
 						ServiceRequestMessage mes = (ServiceRequestMessage) message;
 						respondMessage.list = new ArrayList<Item>();
 						switch (mes.type) {
 						case Compositions:
 							respondMessage.list.addAll(itemList);
 							break;
 						case Queue:
 							//FIXME it doesn't work
 							respondMessage.list.addAll(queue.queue());
 							break;
 						case Albums:
 							break;
 						case Genres:
 							break;
 						case Artists:
 							break;
 						case Playlists:
 							break;
 						case Playlist:
 							break;
 						case PlaylistsInDialog:
 							break;
 						}
 						respondMessage.typeOfContent = mes.type;
 						Messaging.fire(respondMessage);
 					}
 				});
 		Messaging.subscribe(AndroidMessages.ServiceTaskMessage.class,
 				new Messaging.Event() {
 
 					@Override
 					public void messageReceived(Message message) {
 						// TODO end implementation of that event
 						ServiceTaskMessage mes = (ServiceTaskMessage) message;
 						switch (mes.action) {
 						case Play:
 							queue.play();
 							break;
 						case Resume:
 							queue.resume();
 							break;
 						case Pause:
 							queue.pause();
 							break;
 						case Next:
 							queue.next();
 							break;
 						case Previous:
 							queue.prev();
 							break;
 						case addToQueue:
 							queue.enqueue(mes.list, EnqueueMode.AfterAll);
 							break;
 						case setToQueue:
 							queue.enqueue(mes.list, EnqueueMode.ReplaceAll);
 							// FIXME write one method which would work correctly
 							SystemClock.sleep(150);
 							queue.seek(mes.position);
 							break;
 						case scanSystem:
 							MediaScanner mediaScanner = new MediaScanner(
 									getApplication());
 							try {
 								//TODO set this to database
 								itemList = mediaScanner.scanner();
 							} catch (MediaScannerException e) {
 								Log.e(Constants.LOG_TAG,
 										"Can't scan for mediafiles");
 								e.printStackTrace();
 							}
 							
 							break;
 						default:
 							break;
 						// TODO add others...
 						}
 					}
 				});
 
 		Log.d(Constants.LOG_TAG, "Service.onCreate()");
 	}
 
 	// This method is called every time UI starts
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(Constants.LOG_TAG, "Service.onStartCommand()");
 		Status status = player.getStatus();
 		if((status == Status.Playing)||(status==Status.Processing)||(status==Status.Paused)){
 			PlayerChangeEvent message = new PlayerChangeEvent();
 			message.status = status;
 			message.track = player.now_playing();
 			Messaging.fire(message);
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	public void onDestroy() {
 		Log.d(Constants.LOG_TAG, "Service.onDestroy()");
 		queue.clear();
 		super.onDestroy();
 	}
 
 	public IBinder onBind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onBind()");
 		return new CleerBinder();
 	}
 
 	public boolean onUnbind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onUnbind()");
 		return true; // for onServiceConnected
 	}
 
 	public void onRebind(Intent intent) {
 		Log.d(Constants.LOG_TAG, "Service.onRebind()");
 	}
 }
