 /**
  * 
  */
 package com.steelthorn.android.watch.smartsquare;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.util.Log;
 import br.com.condesales.models.Venue;
 
 import com.sonyericsson.extras.liveware.aef.control.Control;
 import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
 
 /**
  * @author Jeff Mixon
  * 
  */
 public class SmartSquareControlExtension extends BaseControlExtensionView implements ISmartSquareControlView
 {
 	private static final String TAG = "SmartSquareControlExtension";
 	
 
 	private List<Venue> _venues;
 	private int _venueIndex = -1;
 	private final ISmartSquarePresenter _presenter;
 
 	private VenuImage _currentVenueImage;
 
 	public SmartSquareControlExtension(Context context, String hostAppPackageName)
 	{
 		super(context, hostAppPackageName);
 
 		//_ctx = context;
 		_presenter = new SmartSquarePresenter(this);
 	}
 
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 
 		showBitmap(new GenericTextImage(_ctx, "Searching..."));
 		_presenter.requestNearbyVenues();
 		setScreenState(Control.Intents.SCREEN_STATE_ON);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if (_venues != null && _venues.size() > 0)
 			showVenueFromList(_venueIndex);
 	}
 
 	@Override
 	public Context getContext()
 	{
 		return _ctx;
 	}
 
 	@Override
 	public void onNearbyVenuesReceived(List<Venue> venues)
 	{
 		_venues = venues;
 
 		if (_venues != null && _venues.size() > 0)
 			showVenueFromList(0);
 
 		setScreenState(Control.Intents.SCREEN_STATE_AUTO);
 	}
 
 	private void showVenueFromList(int index)
 	{
 		// TODO: Don't reload the image each time when swiping
 		//if (index == _venueIndex)
 		//	return;
 
 		Venue v = _venues.get(index);
 		_currentVenueImage = new VenuImage(_ctx, v);
 		showBitmap(_currentVenueImage);
 		_venueIndex = index;
 
 		_presenter.fetchVenueCategoryBitmap(v);
 	}
 
 	@Override
 	public void onSwipe(int direction)
 	{
 		if (_venues == null)
 		{
 			Log.d(TAG, "Venue list is empty.");
 			return;
 		}
 
 		if (direction == Control.Intents.SWIPE_DIRECTION_LEFT)
 			showVenueFromList(++_venueIndex > (_venues.size() - 1) ? _venues.size() - 1 : _venueIndex);
 		else if (direction == Control.Intents.SWIPE_DIRECTION_RIGHT)
 			showVenueFromList(--_venueIndex < 0 ? 0 : _venueIndex);
 
 	}
 
 	private long _lastLongPress;
 
 	@Override
 	public void onTouch(ControlTouchEvent event)
 	{
 		super.onTouch(event);
 
 		if (Control.Intents.TOUCH_ACTION_LONGPRESS == event.getAction())
 		{
 			Log.d(TAG, "LONG PRESS!");
 			if ((event.getTimeStamp() - _lastLongPress) > 2500)
 			{
 				Log.d(TAG, "Initial long press. Sending event on timestamp " + event.getTimeStamp());
 				_lastLongPress = event.getTimeStamp();
 				showBitmap(new GenericTextImage(_ctx, "Checking in..."));
 
 				setScreenState(Control.Intents.SCREEN_STATE_ON);
 				_presenter.requestCheckin(_venues.get(_venueIndex));
 			}
 		}
 	}
 
 	@Override
 	public void onVenueCategoryIconRetrieved(Venue v, Bitmap b)
 	{
 		//Bitmap q = scaleCenterCrop(b, 200, 200);
 		//		
 		//		Bitmap q = Bitmap.createBitmap(b, 28, 28, 200, 200);
 		//		
 		//		Bitmap x = invertBitmap(q);
 		//		
 		//		x = makeBrightnessBitmap(x, -90);
 
 		if (_venues.get(_venueIndex).equals(v))
 		{
 			//_currentVenueImage = new VenuImage(_ctx, v);
 			_currentVenueImage.setBackgroundImage(b);
 			showBitmap(_currentVenueImage);
 		}
 	}
 
 	@Override
 	public void onCheckinResponse(Boolean success)
 	{
 		Log.d(TAG, "onCheckinResponse" + success);
 		setScreenState(Control.Intents.SCREEN_STATE_AUTO);
 
 		if (success)
 		{
 			showBitmap(new VenueDetailImage(_ctx, _venues.get(_venueIndex), _currentVenueImage.getBitmap()));
 		}
 		else
 			showBitmap(new GenericTextImage(_ctx, "Check in failed."));

		new CheckInDelayTask().execute((Void) null);
 
 	}
 
 	private class CheckInDelayTask extends AsyncTask<Void, Void, Void>
 	{
 
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			try
 			{
 				Thread.sleep(3000);
 			}
 			catch (InterruptedException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return (Void) null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result)
 		{
 			super.onPostExecute(result);
 
 			showVenueFromList(_venueIndex);
 		}
 
 	}
 
 	@Override
 	public void onNotFoursquareAuthenticated()
 	{
 		showBitmap(new GenericTextImage(_ctx, "Please log in first."));
 		
 	}
 }
