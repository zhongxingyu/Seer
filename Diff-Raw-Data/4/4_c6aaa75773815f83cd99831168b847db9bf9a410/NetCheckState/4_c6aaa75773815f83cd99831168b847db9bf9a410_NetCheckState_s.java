 package ru.slavabulgakov.busesspb.controller;
 
 import android.util.Log;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.StringRequest;
 import com.android.volley.toolbox.Volley;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class NetCheckState extends State {
 
     RequestQueue _queue;
     Timer _timer;
 
 	@Override
 	public void start() {
 		super.start();
         _queue = Volley.newRequestQueue(_controller.getModel());
         _timer = new Timer();
         _timer.schedule(new CheckInternetConnectionTimerTask(), 0);
 	}
 
 	class CheckInternetConnectionTimerTask extends TimerTask {
 		
 		@Override
 		public void run() {
             StringRequest request = new StringRequest("http://futbix.ru/busesspb/v1_0/list/version/", new Response.Listener<String>() {
                 @Override
                 public void onResponse(String response) {
                     Log.d("internet", "success");
                     _controller.switchToLastState();
                     _controller.getMainActivity().getInternetDenyButtonController().hideInternetDenyIcon();
                 }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError error) {
                     Log.d("internet", "retry");
                     _timer.schedule(new CheckInternetConnectionTimerTask(), 3000);
                    _controller.getMainActivity().getInternetDenyButtonController().showInternetDenyIcon();
                 }
             });
             _queue.add(request);
 		}
 	}
 }
