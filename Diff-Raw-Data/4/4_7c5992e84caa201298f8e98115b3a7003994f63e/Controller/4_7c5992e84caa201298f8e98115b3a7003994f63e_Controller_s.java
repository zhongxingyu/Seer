 package ru.slavabulgakov.busesspb.controller;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.animation.TranslateAnimation;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.flurry.android.FlurryAgent;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.Marker;
 
 import java.util.ArrayList;
 import java.util.Timer;
 
 import ru.slavabulgakov.busesspb.AboutActivity;
 import ru.slavabulgakov.busesspb.Animations;
 import ru.slavabulgakov.busesspb.BaseActivity;
 import ru.slavabulgakov.busesspb.FlurryConstants;
 import ru.slavabulgakov.busesspb.MainActivity;
 import ru.slavabulgakov.busesspb.Network.Loader;
 import ru.slavabulgakov.busesspb.R;
 import ru.slavabulgakov.busesspb.controls.MapController.Listener;
 import ru.slavabulgakov.busesspb.controls.RootView.OnActionListener;
 import ru.slavabulgakov.busesspb.controls.TicketsTray;
 import ru.slavabulgakov.busesspb.model.Model;
 import ru.slavabulgakov.busesspb.model.Model.MenuKind;
 import ru.slavabulgakov.busesspb.model.Model.OnLoadCompleteListener;
 import ru.slavabulgakov.busesspb.model.Route;
 import ru.slavabulgakov.busesspb.model.Transport;
 import ru.slavabulgakov.busesspb.model.TransportKind;
 import ru.slavabulgakov.busesspb.paths.ModelPaths.OnPathLoaded;
 import ru.slavabulgakov.busesspb.paths.Path;
 import ru.slavabulgakov.busesspb.paths.Station;
 
 public class Controller implements OnClickListener, OnLoadCompleteListener, OnActionListener, OnKeyListener, OnPathLoaded, Listener, TicketsTray.Listener, Loader.Listener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
 	
 	private static volatile Controller _instance;
 	private Model _model;
 	private BaseActivity _currentActivity;
 	private Handler _handler;
 	private Timer _timer;
 	private State _state;
 
     public Timer getTimer() {
 		if (_timer == null) {
 			_timer = new Timer();
 		}
 		return _timer;
 	}
 
     public void cancelTimer() {
         if (_timer != null) {
             _timer.cancel();
             _timer = null;
         }
     }
 
     private State _lastState;
     public  void  switchToLastState() {
         switchToState(_lastState);
     }
 
 	public void switchToState(final State state) {
         getHandler().post(new Runnable() {
             @Override
             public void run() {
                 Log.d("switchToState", state.toString());
                 if (_state != null) {
                     if (state.getClass() == _state.getClass()) {
                         return;
                     }
                 }
                 if (_state != null) {
                     _state.removeState();
                 }
                 _lastState = _state;
                 _state = state;
                 _state.setController(Controller.this);
                 _state.start();
             }
         });
 	}
 	
 	public State getState() {
 		return _state;
 	}
 	
 	public Handler getHandler() {
 		if (_handler == null) {
 			_handler = new Handler(Looper.getMainLooper());
 		}
 		return _handler;
 	}
 	
 	public static Controller getInstance() {
     	Controller localInstance = _instance;
     	if (localInstance == null) {
     		synchronized (Controller.class) {
     			localInstance = _instance;
     			if (localInstance == null) {
     				_instance = localInstance = new Controller();
     			}
     		}
     	}
     	return localInstance;
     }
 	
 	public void setActivity(BaseActivity activity) {
 		_currentActivity = activity;
 		_model = (Model)_currentActivity.getApplicationContext();
 	}
 	
 	public BaseActivity getActivity() {
 		return _currentActivity;
 	}
 	
 	public MainActivity getMainActivity() {
 		if (getActivity().getClass() == MainActivity.class) {
 			return (MainActivity)getActivity();
 		}
 		return null;
 	}
 	
 	public Model getModel() {
 		return _model;
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void onClick(View v) {
 		ListView listView = (ListView)_currentActivity.findViewById(R.id.selectRouteListView);
 		switch (v.getId()) {
 		case R.id.closelessTicketsTray:
 			_mainActivity().toggleMenu(MenuKind.Left);
 			break;
 
         case R.id.rightMenuButton:
             if (_model.menuIsOpened(MenuKind.Right)) {
                 _mainActivity().toggleMenu(MenuKind.Right);
             } else {
                 switchToState(new RightMenuStationsState(getMainActivity().getMapController().getCameraLocation()));
             }
             break;
 			
 		case R.id.busFilter:
 			_model.setFilter(TransportKind.Bus);
 			_mainActivity().updateTransportOffline();
 			_mainActivity().updateFilterButtons();
 			FlurryAgent.logEvent(FlurryConstants.busFilterBtnPressed);
 			break;
 			
 		case R.id.trolleyFilter:
 			_model.setFilter(TransportKind.Trolley);
 			_mainActivity().updateTransportOffline();
 			_mainActivity().updateFilterButtons();
 			FlurryAgent.logEvent(FlurryConstants.trolleyFilterBtnPressed);
 			break;
 			
 		case R.id.tramFilter:
 			_model.setFilter(TransportKind.Tram);
 			_mainActivity().updateTransportOffline();
 			_mainActivity().updateFilterButtons();
 			FlurryAgent.logEvent(FlurryConstants.tramFilterBtnPressed);
 			break;
 			
 		case R.id.shipFilter:
 			_model.setFilter(TransportKind.Ship);
 			_mainActivity().updateTransportOffline();
 			_mainActivity().updateFilterButtons();
 			FlurryAgent.logEvent(FlurryConstants.shipFilterBtnPressed);
 			break;
 			
 		case R.id.location:
 			_mainActivity().getMapController().moveCameraToMyLocation(getMainActivity().getLocation());
 			break;
 			
 		case R.id.plus:
 			_mainActivity().getMapController().zoomCameraTo(1);
 			break;
 			
 		case R.id.minus:
 			_mainActivity().getMapController().zoomCameraTo(-1);
 			break;
 			
 		case R.id.about:
 			_currentActivity.startActivity(new Intent(_currentActivity, AboutActivity.class));
 			FlurryAgent.logEvent(FlurryConstants.aboutBtnPressed);
 			break;
 			
 		case R.id.clearRouteText:
 			EditText editText = (EditText)_currentActivity.findViewById(R.id.selectRouteText);
 			editText.setText("");
 			break;
 			
 		case R.id.back_btn:
 			_currentActivity.finish();
 			break;
 			
 		case R.id.paths:
 			_model.getModelPaths().setPathsOn(_mainActivity().pathsButton().checked());
 			FlurryAgent.logEvent(FlurryConstants.pathBtnPressed);
 			break;
 
 		default:
 			break;
 		}
 		
 		if (listView != null) {
 			if (listView.getAdapter() != null) {
 				TransportKind kind = TransportKind.None;
 				switch (v.getId()) {
 				case R.id.menuBusFilter:
 					kind = TransportKind.Bus;
 					FlurryAgent.logEvent(FlurryConstants.menuBusFilterBtnPressed);
 					break;
 					
 				case R.id.menuTrolleyFilter:
 					kind = TransportKind.Trolley;
 					FlurryAgent.logEvent(FlurryConstants.menuTrolleyFilterBtnPressed);
 					break;
 					
 				case R.id.menuTramFilter:
 					kind = TransportKind.Tram;
 					FlurryAgent.logEvent(FlurryConstants.menuTramFilterBtnPressed);
 					break;
 					
 				case R.id.menuShipFilter:
 					kind = TransportKind.Ship;
 					FlurryAgent.logEvent(FlurryConstants.menuShipFilterBtnPressed);
 					break;
 
 				default:
 					break;
 				}
 				if (kind != TransportKind.None) {
 					_model.setFilterMenu(kind);
 					_mainActivity().getLeftMenu().updateListView();
 					_mainActivity().updateFilterButtons();
 				}
 			}
 		}
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void onCameraChange(CameraPosition cameraPosition) {
 		if (_isMainActivity()) {
 			if (Math.abs(_model.getZoom() - cameraPosition.zoom) > .1 || _model.isFirstCameraChange()) {
 				_mainActivity().updateTransportOffline();
 				_model.getModelPaths().updateStationsAndPaths();
 			}
 			_model.setZoom(cameraPosition.zoom);
 			_model.setLocation(cameraPosition.target);
 		}
 	}
 
 	@Override
 	public void onTransportListOfRouteLoadComplete(final ArrayList<Transport> array) {
         getHandler().post(new Runnable() {
             @Override
             public void run() {
                 if (_isMainActivity()) {
                     _mainActivity().getMapController().showTransportListOnMap(array);
                 }
             }
         });
 	}
 
 	@Override
 	public void onRouteKindsLoadComplete(final ArrayList<Route> array) {
 		getHandler().post(new Runnable() {
 
 			@Override
 			public void run() {
 				if (_isMainActivity()) {
 					if(array == null) {
 						Toast.makeText(_currentActivity, R.string.server_access_deny, Toast.LENGTH_LONG).show();
 					}
 					_mainActivity().getLeftMenu().showMenuContent();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onImgLoadComplete(Bitmap img) {
 		if (_isMainActivity()) {
 			_mainActivity().getMapController().showTransportImgOnMap(img);
 		}
 		
 	}
 
 	@Override
 	public void onMenuChangeState(boolean isOpen, MenuKind kind) {
        _mainActivity().updateControls();
 
         if (!isOpen) {
             _mainActivity().keyboardTurnOff();
         }
 
         _mainActivity().getMapController().toggleRotateMap(isOpen);
 
 		if (kind == MenuKind.Left) {
 			if (isOpen) {
 	    		switchToState(new LeftMenuState());
 			}
 			
 			_mainActivity().updateFilterButtons();
 			
 			
 			if (isOpen) {
 				FlurryAgent.logEvent(FlurryConstants.menuIsOpen);
 			} else {
 				if (_model.getFavorite().size() > 0) {
 					FlurryAgent.logEvent(FlurryConstants.selectedTransportModeIsOn);
 				} else {
 					FlurryAgent.logEvent(FlurryConstants.selectedTransportModeIsOff);
 				}
 			}
 		} else if (kind == MenuKind.Right) {
             if (isOpen) {
                 FlurryAgent.logEvent(FlurryConstants.rightMenuIsOpen);
                 switchToState(new ForecastsState());
             }
         }
 		if (!isOpen) {
 			switchToState(new MapState());
 		}
 	}
 
 	@Override
 	public void onHold(Boolean hold) {
 		_mainActivity().getMapController().enableMapGestures(!hold);
 	}
 	
 	@Override
 	public void onMove(double percent) {
 		if (_currentActivity != null) {
 			if (_isMainActivity()) {
 				_mainActivity().getLeftMenu().move(percent);
 				_mainActivity().getRightMenu().move(percent);
 			}
 		}
 	}
 
 	@Override
 	public void onInternetAccessDeny() {
 		if (_isMainActivity()) {
 			_mainActivity().getInternetDenyButtonController().showInternetDenyIcon();
 		}
 	}
 
 	@Override
 	public void onInternetAccessSuccess() {
 		if (_isMainActivity()) {
 			_mainActivity().getInternetDenyButtonController().hideInternetDenyIcon();
 			_mainActivity().getMapController().clearMap();
 		}
 	}
 	
 	private boolean _isMainActivity() {
 		return _currentActivity.getClass() == MainActivity.class;
 	}
 	
 	private MainActivity _mainActivity() {
 		if (_isMainActivity()) {
 			return (MainActivity)_currentActivity;
 		}
 		return null;
 	}
 
 	@Override
 	public boolean onKey(View view, int keyCode, KeyEvent arg2) {
 		if (view.getClass() == EditText.class && _isMainActivity()) {
 			if (keyCode == KeyEvent.KEYCODE_ENTER) {
 				_mainActivity().keyboardTurnOff();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onPathLoaded(final Path path) {
         getHandler().post(new Runnable() {
             @Override
             public void run() {
                 if (_mainActivity() != null) {
                     if (_mainActivity().getMapController() != null) {
                         _mainActivity().getMapController().showPath(path);
                     }
                 }
             }
         });
 	}
 
 	@Override
 	public void onStationsLoaded(final ArrayList<?> stations) {
         getHandler().post(new Runnable() {
             @Override
             public void run() {
                 if (_mainActivity() != null) {
                     if (_mainActivity().getMapController() != null) {
                         _mainActivity().getMapController().showStations(stations);
                     }
                 }
             }
         });
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		Station station = _model.getModelPaths().getStationByMarker(marker);
         if (station != null) {
             FlurryAgent.logEvent(FlurryConstants.onInfoWindowClick);
             _model.setData("stationId", station.id);
             _model.setData("stationTitle", station.name);
             ((MainActivity)_currentActivity).toggleMenu(MenuKind.Right);
         }
     }
 	
 	@Override
 	public void onMapImgUpdated() {
 		_mainActivity().runReopenAnimation();
 	}
 
 	@Override
 	public void willRemoveTicket() {
 		_mainActivity().getLeftMenu().updateListView();
 	}
 
 	@Override
 	public void didRemoveTicket() {
 		if (_model.getFavorite().size() == 0) {
 			LinearLayout listViewAndProgressBarLinearLayout = (LinearLayout)_currentActivity.findViewById(R.id.listViewAndProgressBarLinearLayout);
 			TranslateAnimation animation = new TranslateAnimation(0, 0, _model.dpToPx(60), 0);
 			animation.setDuration(Animations.ANIMATION_DURATION);
 			listViewAndProgressBarLinearLayout.startAnimation(animation);
 		}
 	}
 
 	@Override
 	public void staticLoaded(Loader loader) {
 		if (_state.getClass() == ForecastsState.class) {
 			ForecastsState state = (ForecastsState)_state;
 			state.staticLoaded(loader);
         } else if (_state.getClass() == RightMenuStationsState.class) {
             RightMenuStationsState state = (RightMenuStationsState)_state;
             state.staticLoaded(loader);
         }
 	}
 
 	@Override
 	public void netLoaded(Loader loader) {
         if (_state.getClass() == ForecastsState.class) {
             ForecastsState state = (ForecastsState)_state;
             state.staticLoaded(loader);
         }
 	}
 
 	@Override
 	public void netError(Loader loader) {
 		if (_state.getClass() != NetCheckState.class) {
             switchToState(new NetCheckState());
         }
 	}
 
     @Override
     public void onConnected(Bundle bundle) {
 
     }
 
     @Override
     public void onDisconnected() {
 
     }
 
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
 
     }
 }
