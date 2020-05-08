 package se.purplescout.purplemow.onboard.ui.remote.activity;
 
 import com.google.inject.Inject;
 
 import roboguice.activity.RoboActivity;
 import se.purplescout.purplemow.core.bus.CoreBus;
 import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEvent;
 import se.purplescout.purplemow.onboard.backend.service.remote.RemoteService;
 import se.purplescout.purplemow.onboard.backend.service.remote.RemoteService.Direction;
 import se.purplescout.purplemow.onboard.ui.remote.view.RemoteView;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class RemoteActivity extends RoboActivity {
 
 	public interface ViewDisplay {
 
 		Button getGoHomeBtn();
 
 		Button getClockInBtn();
 
 		Button getForwardBtn();
 
 		Button getLeftBtn();
 
 		Button getRightBtn();
 
 		Button getReverseBtn();
 
 		Button getIncrementCutterBtn();
 
 		Button getDecrementCutterBtn();
 
 		View getStopBtn();
 	}
 
 	@Inject RemoteService remoteService;
 
 	ViewDisplay display;
 	CoreBus coreBus = CoreBus.getInstance();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		display = new RemoteView(this);
 		bind();
 	}
 
 	private void bind() {
 		display.getGoHomeBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				coreBus.fireEvent(new BatterySensorReceiveEvent(0));
 			}
 		});
 		display.getClockInBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				coreBus.fireEvent(new BatterySensorReceiveEvent(1023));
 			}
 		});
 		display.getForwardBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.incrementMovmentSpeed(Direction.FORWARD);
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getLeftBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.incrementMovmentSpeed(Direction.LEFT);
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getRightBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.incrementMovmentSpeed(Direction.RIGHT);
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getReverseBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.incrementMovmentSpeed(Direction.REVERSE);
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getStopBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.stop();
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getIncrementCutterBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.incrementCutterSpeed();
 						return null;
 					}
 				}.execute();
 			}
 		});
 		display.getDecrementCutterBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AsyncTask<Void, Void, Void>() {
 
 					@Override
 					protected Void doInBackground(Void... params) {
 						remoteService.decrementCutterSpeed();
 						return null;
 					}
 				}.execute();
 			}
 		});
 	}
}
