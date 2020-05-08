 package jag.kumamoto.apps.gotochi.stamprally;
 
 import jag.kumamoto.apps.gotochi.stamprally.Data.StampPin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.content.Context;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.Projection;
 
 /**
  * 
  * スタンプラリーのピンをマップ上に表示するオーバーレイ
  * 
  * @author aharisu
  *
  */
 public class StampPinOverlay extends ItemizedOverlay<StampPinOverlay.StampRallyMarker> 
 	implements OnGestureListener, OnDoubleTapListener{
 	
 	public static interface OnClickListener {
 		public void onClick(StampPin pin);
 	}
 	
 	public static interface Filter {
 		public boolean filter(StampPin pin);
 	}
 	
 	public static class StampRallyMarker extends OverlayItem {
 		public final StampPin stampPin;
 		
 		public StampRallyMarker(StampPin stamp, Context context) {
 			super(new GeoPoint(stamp.latitude, stamp.longitude),
 					stamp.name, null);
 			
 			stampPin = stamp;
 			
 			Drawable drawable;
 			if(stamp.type ==  StampPin.STAMP_TYPE_NONE) {
 				drawable = context.getResources().getDrawable(stamp.isArrive ? 
 								R.drawable.marker_none_arrived :
 								R.drawable.marker_none);
 			} else {
 				drawable = context.getResources().getDrawable(stamp.isArrive ? 
 								R.drawable.marker_quiz_arrived :
 								R.drawable.marker_quiz);
 			}
 			
 			setMarker(boundCenterBottom(drawable));
 		}
 	}
 	
 	
 	public static final int ShowTypeAllMarker = 0;
 	public static final int ShowTypeArrivedMarker = 1;
 	public static final int ShowTypeNoArriveMarker = 2;
 	
 	
 	private final Context mContext;
 	
 	private PinInfoOverlay mInfoOverlay;
 	private final ArrayList<StampPin> mStampPinList = new ArrayList<StampPin>();
 	private final MapView mMapView;
 	
 	private final ArrayList<Filter> mFilterList = new ArrayList<Filter>();
 	
 	private final ArrayList<StampPin> mShowStampPin = new ArrayList<StampPin>();
 	
 	private int mTappedItemIndex = -1;
 	private boolean mIsTouched = false;
 	private final GestureDetector mGestureDetector;
 	
 	private OnClickListener mListener;
 	
 	public StampPinOverlay(Context context, Drawable defaultMarker,  MapView map,
 			OnClickListener listener) {
 		super(boundCenterBottom(defaultMarker));
 		
 		this.mContext = context;
 		this.mMapView = map;
 		
 		mGestureDetector = new GestureDetector(context, this);
 		mGestureDetector.setOnDoubleTapListener(this);
 		
 		mListener = listener;
 		
 		applyFilter();
 	}
 	
 	public void addStampPins(StampPin... pins) {
 		if(pins == null || pins.length == 0)
 			return;
 		
 		mStampPinList.addAll(Arrays.asList(pins));
 		
 		applyFilter();
 	}
 	
 	public void removeStampPins(StampPin... pins) {
 		if(pins == null || pins.length == 0)
 			return ;
 		
 		for(StampPin pin : pins) {
 			int size = mStampPinList.size();
 			for(int i = 0;i < size;++i) {
 				if(pin.id == mStampPinList.get(i).id) {
 					mStampPinList.remove(i);
 					break;
 				}
 			}
 		}
 		
 		applyFilter();
 	}
 	
 	public void setStampPins(StampPin... pins) {
 		mShowStampPin.clear();
 		
 		if(pins == null || pins.length == 0)
 			return;
 		
 		mStampPinList.addAll(Arrays.asList(pins));
 		
 		applyFilter();
 	}
 	
 	public void setInfoOverlay(PinInfoOverlay overlay) {
 		mInfoOverlay = overlay;
 	}
 	
 	public void addShowPinFilter(Filter filter, boolean update) {
 		mFilterList.add(filter);
 		
 		if(update) {
 			applyFilter();
 		}
 	}
 	
 	public void removeShowPinFilter(Filter filter, boolean update) {
 		mFilterList.remove(filter);
 		
 		if(update) {
 			applyFilter();
 		}
 	}
 	
 	private void applyFilter() {
 		mShowStampPin.clear();
 		
 		if(mFilterList.size() == 0) {
 			mShowStampPin.addAll(mStampPinList);
 		} else {
 			for(StampPin pin : mStampPinList) {
 				boolean show = true;
 				for(Filter filter : mFilterList) {
 					if(!filter.filter(pin)) {
 						show = false;
 						break;
 					}
 				}
 				
 				if(show) {
 					mShowStampPin.add(pin);
 				}
 			}
 		}
 		
 		this.populate();
 		mMapView.invalidate();
 	}
 	
 	@Override protected StampRallyMarker createItem(int i) {
 		return new StampRallyMarker(mShowStampPin.get(i), mContext);
 	}
 	
 	@Override public int size() {
 		return mShowStampPin.size();
 	}
 	
 	@Override public boolean onTouchEvent(MotionEvent event, MapView mapView) {
 		
 		boolean handle = false;
 		int action = event.getAction();
 		
 		if(action == MotionEvent.ACTION_DOWN) {
 			if(mTappedItemIndex >= 0) {
 				if(hitTest(event, mTappedItemIndex)) {
 					mIsTouched = true;
 				} else {
 					mTappedItemIndex = -1;
 				}
 			}
 			
 			if(mTappedItemIndex < 0) {
 				mTappedItemIndex = hitTest(event);
 				if(mTappedItemIndex >= 0) {
 					mIsTouched = true;
 				}
 			}
 		}
 		
 		if(mIsTouched && mGestureDetector.onTouchEvent(event)) {
 			handle = true;
 		}
 		
 		if(action == MotionEvent.ACTION_UP ||
 				action == MotionEvent.ACTION_CANCEL) {
 			mIsTouched = false;
 		}
 		
 		return handle;
 	}
 	
 	private boolean hitTest(MotionEvent event, int index) {
 		Point pt = new Point();
 		Projection projection = mMapView.getProjection();
 		
 		int x = (int)event.getX();
 		int y = (int)event.getY();
 		StampRallyMarker marker = getItem(index);
 		if(marker == null)
 			return false;
 		
 		projection.toPixels(marker.getPoint(), pt);
 		
 		return hitTest(marker, marker.getMarker(0), x - pt.x, y - pt.y);
 	}
 	
 	private int hitTest(MotionEvent event) {
 		int size = size();
 		Point pt = new Point();
 		Projection projection = mMapView.getProjection();
 		
 		int x = (int)event.getX();
 		int y = (int)event.getY();
 		
 		for(int i = 0;i < size;++i) {
 			StampRallyMarker marker = getItem(i);
 			if(marker == null)
 				continue;
 			
 			projection.toPixels(marker.getPoint(), pt);
 			
 			if(hitTest(marker, marker.getMarker(0), x - pt.x, y - pt.y)) {
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 	
 	@Override public boolean onDoubleTap(MotionEvent e) {
 		if(mTappedItemIndex >= 0) {
 			if(mListener != null) {
 				mListener.onClick(getItem(mTappedItemIndex).stampPin);
 			}
 			
 			mTappedItemIndex = -1;
 		}
 		
 		return true;
 	}
 	
 	@Override public boolean onDoubleTapEvent(MotionEvent e) {
 		return false;
 	}
 	
 	@Override public boolean onDown(MotionEvent e) {
		return true;
 	}
 	
 	@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		mTappedItemIndex = -1;
 		return false;
 	}
 	
 	@Override public void onLongPress(MotionEvent e) {
 	}
 	
 	@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		mTappedItemIndex = -1;
 		
 		return false;
 	}
 	
 	@Override public void onShowPress(MotionEvent e) {
 	}
 	
 	@Override public boolean onSingleTapConfirmed(MotionEvent e) {
 		if(mTappedItemIndex >= 0) {
 			StampRallyMarker marker = getItem(mTappedItemIndex);
 			
 			mMapView.getController().animateTo(marker.getPoint());
 			if(mInfoOverlay != null) {
 				mInfoOverlay.setMarkerInfo(marker);
 			}
 			
 			mTappedItemIndex = -1;
 			
 		}
 		return true;
 	}
 	
 	@Override public boolean onSingleTapUp(MotionEvent e) {
 		return false;
 	}
 	
 }
