 package org.routy.task;
 
 import java.io.IOException;
 import java.util.Timer;
 
 import org.routy.exception.NoInternetConnectionException;
 import org.routy.exception.RoutyException;
 import org.routy.log.Log;
 import org.routy.model.AppConfig;
 import org.routy.model.Route;
 import org.routy.model.RouteRequest;
 import org.routy.service.RouteService;
 import org.routy.timer.Timeout;
 import org.routy.timer.TimeoutCallback;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 
 public abstract class CalculateRouteTask extends AsyncTask<RouteRequest, Void, Route> {
 
 	private final String TAG = "CalculateRouteTask";
 	
 	public abstract void onRouteCalculated(Route route);
 	public abstract void onRouteCalculateTimeout();
 	public abstract void onNoInternetConnectionException();
 	public abstract void onRouteCalculateFailed(Throwable t);
 
 	private Context context;
 	private ProgressDialog progressDialog;
 	private Timer timer;
 
 	public CalculateRouteTask(Context context) {
 		super();
 		
 		this.context = context;
 	}
 	
 	
 	@Override
 	protected void onPreExecute() {
 		progressDialog = new ProgressDialog(context);
 		progressDialog.setTitle("Hang Tight!");
 		progressDialog.setMessage("Generating your route...");
 		progressDialog.setCancelable(false);
 		progressDialog.setCanceledOnTouchOutside(false);
 		progressDialog.setIndeterminate(true);
 		progressDialog.setCancelable(false);
 		progressDialog.show();
 	}
 	
 	
 	@Override
 	protected Route doInBackground(RouteRequest...requests) {
 		timer = new Timer();
 		try {
 			if (requests.length == 0) {
 				CalculateRouteTask.this.cancel(true);
 			} else {
 				//Timer to timeout this task
 				timer.schedule(new Timeout(this, new TimeoutCallback() {
 					
 					@Override
 					public void onTimeout() {
 						onRouteCalculateTimeout();
 					}
 				}), AppConfig.CALCULATE_ROUTE_TIMEOUT_MS);
 				
 				RouteRequest request = requests[0];
 				RouteService routeService = new RouteService(request.getOrigin(), request.getDestinations(), request.getPreference(), false);
 				Route bestRoute = routeService.getBestRoute();
 				
 				return bestRoute;
 			}
 			
 			
 		} catch (NoInternetConnectionException e) {
 			onNoInternetConnectionException();
 			CalculateRouteTask.this.cancel(true);
 		} catch (Exception e) {
 			onRouteCalculateFailed(e);
 			CalculateRouteTask.this.cancel(true);
		} finally {
			Log.v(TAG, "route calculate timer cancelled");
			if (timer != null) {
				timer.cancel();
			}
 		}
 		return null;
 	}
 	
 	
 	@Override
 	protected void onPostExecute(Route result) {
 		if (progressDialog.isShowing()) {
 			progressDialog.cancel();
 		}
 		
 		if (result != null) {
 			onRouteCalculated(result);
 		} else {
 			Log.e(TAG, "Best route returned was null.");
 		}
 	}
 	
 	
 	@Override
 	protected void onCancelled(Route result) {
 		if (progressDialog.isShowing()) {
 			progressDialog.cancel();
 		}
 		
 		if (timer != null) {
 			timer.cancel();
 		}
 	}
 }
