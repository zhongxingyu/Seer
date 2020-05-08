 package br.ufsm.brunodea.tcc.map;
 
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import br.ufsm.brunodea.tcc.App;
 import br.ufsm.brunodea.tcc.R;
 import br.ufsm.brunodea.tcc.context.ContextData;
 import br.ufsm.brunodea.tcc.context.ContextSupplier;
 import br.ufsm.brunodea.tcc.context.ContextSupplier.ContextAction;
 import br.ufsm.brunodea.tcc.context.supplier.InfoCityAlohar;
 import br.ufsm.brunodea.tcc.context.supplier.InfoCityQrCode;
 import br.ufsm.brunodea.tcc.internet.InfoCityServer;
 import br.ufsm.brunodea.tcc.internet.Internet;
 import br.ufsm.brunodea.tcc.map.InfoCityLocationListener.LocationAction;
 import br.ufsm.brunodea.tcc.model.EventItem;
 import br.ufsm.brunodea.tcc.model.EventTypeManager;
 import br.ufsm.brunodea.tcc.util.DialogHelper;
 import br.ufsm.brunodea.tcc.util.InfoCityPreferenceActivity;
 import br.ufsm.brunodea.tcc.util.InfoCityPreferences;
 import br.ufsm.brunodea.tcc.util.Util;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 /**
  * Activity principal da aplicação que tem um mapa onde é possível
  * visualizar e interagir com os eventos, além de adicionar novos e buscar
  * por outros, etc.
  * 
  * @author bruno
  *
  */
 public class InfoCityMap extends MapActivity implements OnClickListener {
 	private MapView mMapView;
 	private MapController mMapController;
 	private List<Overlay> mMapOverlays;
 	
 	private final int DEFAULT_MAP_ZOOM = 20;
 	
 	private MyLocationOverlay mMyLocationOverlay;
 	
 	private LocationManager mLocationManager;
 	private InfoCityLocationListener mLocationListener;
 	
 	private InfoCityAlohar mAloharContextSupplier;
 	private InfoCityQrCode mQrCodeContextSupplier;
 	
 	private ContextSupplier mCurrContextSupplier;
 	
 	private Location mLastKnownLocation;
 	
 	private ImageButton mWindowTitleButtonRefresh;
 	private ImageButton mWindowTitleButtonCenterOn;
 	private ImageButton mWindowTitleButtonAddEvent;
 	private ProgressBar mWindowTitleProgressBar;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceBundle) {
 		super.onCreate(savedInstanceBundle);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.map);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.windowtitle);
 
 		initGUIElements();
 		init();
 		
 		Internet.hasConnection(this, true);
 		//onClick(mWindowTitleButtonRefresh);
 	}
 	
 	private void init() {
 		mMapView = (MapView) findViewById(R.id.mapview);
 		mMapView.getController().setZoom(DEFAULT_MAP_ZOOM);
 		mMapView.setSatellite(true);
 		
 		mMapController = mMapView.getController();
 		
 		mMapOverlays = mMapView.getOverlays();
 		
 		App.instance().initEventOverlayManager(this, mMapView, mMapOverlays);
 		
 		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		mLocationListener = new InfoCityLocationListener(this);
 
 		mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(mLastKnownLocation == null) {
 			mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 	
 		mAloharContextSupplier = new InfoCityAlohar(getApplication());
 		mAloharContextSupplier.init();
 		
 		mCurrContextSupplier = mAloharContextSupplier;
 		mQrCodeContextSupplier = new InfoCityQrCode(this);
 		mQrCodeContextSupplier.setHandler(mQrCodeHandler);
 		
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 		adjustMyLocationStuff();
 		mMapOverlays.add(mMyLocationOverlay);
 		
 		centerMapInLastKnownLocation();
 		
 		mMapView.invalidate();
 		
 		//para desenhar o marker de adição sempre na frente dos outros.
 		App.instance().getEventOverlayManager().getEventOverlay(
 				EventTypeManager.instance(this).type_add(), this);
 	}
 	
 	private void initGUIElements() {
 		mWindowTitleButtonAddEvent = (ImageButton)findViewById(R.id.button_add_event);
 		mWindowTitleButtonAddEvent.setOnClickListener(this);
 		
 		mWindowTitleProgressBar = (ProgressBar)findViewById(R.id.progressbar_window_title);
 		mWindowTitleProgressBar.setVisibility(View.GONE);
 		
 		mWindowTitleButtonCenterOn = (ImageButton)findViewById(R.id.button_centeron_marker);
 		mWindowTitleButtonCenterOn.setOnClickListener(this);
 		
 		mWindowTitleButtonRefresh = (ImageButton)findViewById(R.id.button_refresh_events);
 		mWindowTitleButtonRefresh.setOnClickListener(this);
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.map_menu, menu);
 	    return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.menu_map_settings:
 	    	Intent prefs = new Intent(this, InfoCityPreferenceActivity.class);
 	    	startActivityForResult(prefs, InfoCityPreferenceActivity.REQUEST_CODE);
 	    	break;
 	    default:
 	    	return super.onOptionsItemSelected(item);
 	    }
 	    return true;
 	}
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if(requestCode == InfoCityPreferenceActivity.REQUEST_CODE) {
 			//alterou a preferência de distância máxima que um evento pode estar.
 			if(resultCode == InfoCityPreferenceActivity.RESULT_CODE_EVENT_RADIUS) {
 				aloharFetchEvents();
 			} else if(resultCode == InfoCityPreferenceActivity.RESULT_CODE_MYLOCATION) {
 				adjustMyLocationStuff();
 			}
 		} else if(requestCode == IntentIntegrator.REQUEST_CODE) {
 			IntentResult intent_result = IntentIntegrator.parseActivityResult(
 					requestCode, resultCode, intent);
 			mQrCodeContextSupplier.finishedScan(intent_result);
 		}
 	}
 	
 	private void adjustMyLocationStuff() {
 		if(InfoCityPreferences.shouldEnableCompass(this)) {
 			mMyLocationOverlay.enableCompass();
 		} else {
 			mMyLocationOverlay.disableCompass();
 		}
 
 		if(InfoCityPreferences.shouldEnableMyLocation(this)) {
 			mMyLocationOverlay.enableMyLocation();
 		} else {
 			mMyLocationOverlay.disableMyLocation();
 		}
 	}
 	/**
 	 * Inicializa a requisição de localização do usuário, através de
 	 * InfoCityLocationListener utilizando GPS e a Rede. 
 	 */
 	public void startRequestLocationUpdates() {
 		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
 				0, 0, mLocationListener);
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				0, 0, mLocationListener);
 	}
 	
 	/**
 	 * Termina com a requisição de atualização da localização atual do usuário.
 	 */
 	public void stopRequestLocationUpdates() {
 		mLocationManager.removeUpdates(mLocationListener);
 	}
 	
 	/**
 	 * Centraliza o mapa na última localização conhecida (conseguida pelo
 	 * listener).
 	 */
 	public void centerMapInLastKnownLocation() {
 		if(mLastKnownLocation != null) {
 			mMapController.setCenter(
 					new GeoPoint(
 						(int)(mLastKnownLocation.getLatitude()*1E6), 
 						(int)(mLastKnownLocation.getLongitude()*1E6)
 				    ));
 		}
 	}
 
 	/**
 	 * Se tem algum balão aberto, ele é fechado.
 	 * Se não, fecha a aplicação.
 	 */
 	@Override
 	public void onBackPressed() {
 		boolean closed_balloons = false;
 		for(EventsItemizedOverlay e : App.instance().getEventOverlayManager().getItemizedOverlays()) {
 			if(e.getFocus() != null) {
 				e.setFocus(null);
 				closed_balloons = true;
 			}
 		}
 		
 		if(!closed_balloons) {
 			super.onBackPressed();
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v == mWindowTitleButtonAddEvent) {
 			Handler handler = new Handler() {
 				@Override
 				public void handleMessage(Message msg) {
 					if(msg.what == 0) {
 						aloharAddEvent();
 					} else if(msg.what == 1) {
 						mQrCodeContextSupplier.beginScan(ContextAction.ADD_EVENT);
 					}
 				}
 			};
 			
 			showSelectContextSupplierDialog(handler);
 		} else if(v == mWindowTitleButtonCenterOn) {
 			GeoPoint p = App.instance().getEventOverlayManager().
 					getEventOverlay(EventTypeManager.instance().type_add(), this)
 					.getItem(0).getPoint();
 			mMapController.animateTo(p);
 			mMapView.getController().setZoom(DEFAULT_MAP_ZOOM);
 		} else if(v == mWindowTitleButtonRefresh) {
 			Handler handler = new Handler() {
 				@Override
 				public void handleMessage(Message msg) {
 					if(msg.what == 0) {
 						aloharFetchEvents();
 					} else if(msg.what == 1) {
 						mQrCodeContextSupplier.beginScan(ContextAction.FETCH_EVENTS);
 					}
 				}
 			};
 			showSelectContextSupplierDialog(handler);
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		mAloharContextSupplier.stop();
 		super.onDestroy();
 	}
 	
 	private void showSelectContextSupplierDialog(Handler handler) {
 		ContextSupplier[] suppliers = 
 			{mAloharContextSupplier, mQrCodeContextSupplier};
 		
 		DialogHelper.selectContextProviderDialog(this, suppliers, handler);
 	}
 	
 	public void setLastKnownLocation(Location location) {
 		mLastKnownLocation = location;
 	}	
 	
 	public void toggleWindowTitleAddEventProgressBar() {
 		int pb_vis = mWindowTitleProgressBar.getVisibility();
 		mWindowTitleProgressBar.setVisibility(
 				pb_vis == View.VISIBLE ? View.GONE : View.VISIBLE);
 		TextView tv = (TextView)findViewById(R.id.textview_gettingpos_window_title);
 		int tv_vis = tv.getVisibility();
 		tv.setVisibility(tv_vis == View.VISIBLE ? View.GONE : View.VISIBLE);
 		
 		mWindowTitleButtonAddEvent.setEnabled(!mWindowTitleButtonAddEvent.isEnabled());
 	}
 	
 	public void toggleWindowTitleAddEventCenterOn() {
 		int add_vis = mWindowTitleButtonAddEvent.getVisibility();
 		mWindowTitleButtonAddEvent.setVisibility(
 				add_vis == View.VISIBLE ? View.GONE : View.VISIBLE);
 		mWindowTitleButtonCenterOn.setVisibility(add_vis);
 	}
 	
 	public void addAddEventItemMarker() {
 		EventItem new_event = Util.createEventItem(mLastKnownLocation.getLatitude(),
 				mLastKnownLocation.getLongitude(), "New Event", "New Event", EventTypeManager.instance().type_add());
 		App.instance().getEventOverlayManager().addEventItem(new_event, this);
 
 		toggleWindowTitleAddEventCenterOn();
 	}
 	
 	public void addEventItem(EventItem event_item) {
 		App.instance().getEventOverlayManager().addEventItem(event_item, this);
 	}
 
 	private void toggleRefreshAnimation() {
 		boolean clickable = mWindowTitleButtonRefresh.isClickable();
 		mWindowTitleButtonRefresh.setClickable(!clickable);
 		if(mWindowTitleButtonRefresh.isClickable()) {
 			mWindowTitleButtonRefresh.getAnimation().cancel();
 		} else {
 			mWindowTitleButtonRefresh.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
 		}
 	}
 	
 	public void removeAddEventItem() {
 		App.instance().getEventOverlayManager()
 			.getEventOverlay(EventTypeManager.instance().type_add(), this).setFocus(null);
 		App.instance().getEventOverlayManager()
 			.clearItemizedOverlaysOfType(EventTypeManager.instance().type_add());
 
 		toggleWindowTitleAddEventCenterOn();
 	}
 
 	public void aloharFetchEvents() {
 		mCurrContextSupplier = mAloharContextSupplier;
 		toggleRefreshAnimation();
 		mLocationListener.setCurrAction(LocationAction.GET_EVENTS);
 		Location l = mMyLocationOverlay.getLastFix();
 		if(l != null) {
 			mLocationListener.onLocationChanged(l);
 		} else {
 			startRequestLocationUpdates();
 		}
 	}
 	
 	/**
 	 * Cria a thread que vai buscar no servidor os eventos dentro de um raio
 	 * pré-determinado (no caso, 50km) e os mostra na tela, mas apenas aquelas
 	 * que ainda não estão carregados no aplicativo.
 	 */
 	public void fetchEvents() {
 		final Handler done_handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				toggleRefreshAnimation();
 				if(msg.what == 0) {
 					//good
 					mMapView.invalidate(); //força o mapa se redesenhar para mostrar os markers.
 				} else if(msg.what == 1) {
 					Toast.makeText(InfoCityMap.this, getResources()
 							.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
 				}
 			}
 		};
 		
 		final Handler event_handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				EventItem event = (EventItem) msg.obj;
 				//só adiciona evento que ainda não exista no aplicativo.
 				if(!App.instance().getEventOverlayManager()
 					.getEventOverlay(event.getType(), InfoCityMap.this)
 					.containsEventItem(event)) {
 					
 					addEventItem(event);
 				}
 			}
 		};
 		
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				boolean ok = false;
 				//faz a busca no servidor pelos eventos.
 				JSONObject res = InfoCityServer.getEvents(InfoCityMap.this, mLastKnownLocation, 
 						InfoCityPreferences.getEventMaxRadius(InfoCityMap.this), 
 						mCurrContextSupplier.getContextData());
 				if(res != null && res.has("size")) {
 					try {
						App.instance().getEventOverlayManager()
							.clearItemizedOverlaysExcept(EventTypeManager.instance().type_add());
 						int size = res.getInt("size");
 						for(int i = 0; i < size; i++) {
 							if(res.has("event_"+i)) {
 								JSONObject event = res.getJSONObject("event_"+i);
 								try {
 									Message msg = event_handler.obtainMessage();
 									msg.obj = Util.eventItemFromJSON(event);
 									event_handler.sendMessage(msg);
 								} catch (Exception e) {
 									e.printStackTrace();
 								}
 							}
 						}
 						ok = true;
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				if(ok) {
 					done_handler.sendEmptyMessage(0);
 				} else {
 					done_handler.sendEmptyMessage(1);
 				}
 			}
 		};
 		
 		t.start();
 	}
 	
 	//TODO: adicionar contextData ao evento.
 	private void aloharAddEvent() {
 		mCurrContextSupplier = mAloharContextSupplier;
 		toggleWindowTitleAddEventProgressBar();
 		mLocationListener.setCurrAction(LocationAction.ADD_EVENT);
 		Location l = mMyLocationOverlay.getLastFix();
 		if(l != null) {
 			mLocationListener.onLocationChanged(l);
 		} else {
 			startRequestLocationUpdates();
 		}
 	}
 	
 	private Handler mQrCodeHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if(msg.what == ContextAction.NONE.getValue()) {
 				Toast.makeText(InfoCityMap.this, InfoCityMap.this.getResources()
 						.getString(R.string.qrcode_error), Toast.LENGTH_SHORT).show();
 			} else if(msg.what == ContextAction.ADD_EVENT.getValue()) {
 				mLocationListener.setCurrAction(LocationAction.ADD_EVENT);
 				ContextData cd = mQrCodeContextSupplier.getContextData();
 
 				Location l = new Location("QrCode");
 				l.setLatitude(cd.getLatitude());
 				l.setLongitude(cd.getLongitude());
 
 				mLocationListener.onLocationChanged(l);
 				onClick(mWindowTitleButtonCenterOn);
 			} else if(msg.what == ContextAction.FETCH_EVENTS.getValue()) {
 				mCurrContextSupplier = mQrCodeContextSupplier;
 				onClick(mWindowTitleButtonRefresh);
 				mCurrContextSupplier = mAloharContextSupplier;
 			}
 		}
 	};
 }
