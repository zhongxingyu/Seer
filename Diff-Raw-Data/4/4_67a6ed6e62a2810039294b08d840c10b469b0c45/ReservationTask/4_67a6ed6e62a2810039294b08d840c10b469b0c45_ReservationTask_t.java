 package se.pellbysandberg.util;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import se.pellbysandberg.models.Reservation;
 import android.os.AsyncTask;
 
 import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 
 public class ReservationTask extends AsyncTask<Reservation, Integer, String> {
 
 	@Override
 	protected String doInBackground(Reservation... params) {
 		if(params.length == 0 )
 			return null;
 		Reservation reservation = params[0];
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(Constants.WEBSERVER_BASE_URL + "/reservations?action=reserve");
 		post.setHeader("Content-Type", "application/json");
 		
		Gson g = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SS").create();
 		try {
 			post.setEntity(new ByteArrayEntity(g.toJson(reservation).getBytes(Constants.TEXT_ENCODING)));
 		} catch (UnsupportedEncodingException e) {
 			post.setEntity(new ByteArrayEntity(g.toJson(reservation).getBytes()));
 		}
 		try {
 			client.execute(post);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 
 }
