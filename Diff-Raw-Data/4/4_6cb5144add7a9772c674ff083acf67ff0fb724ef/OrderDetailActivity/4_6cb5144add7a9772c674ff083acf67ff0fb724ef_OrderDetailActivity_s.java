 package ilmarse.mobile.activities;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class OrderDetailActivity extends Activity {
 
 	private String TAG = getClass().getSimpleName();
 	Bitmap bmImg;
 	ImageView imView;
 
 	// String orderid = ((OrderImpl)o).getId()+"";
 	// String username = ((OrderImpl)o).getUsername();
 	// String token = ((OrderImpl)o).getToken();
 	// String location = ((OrderImpl)o).getLocation();
 	// String created_date = ((OrderImpl)o).getCreated_date();
 
 	@Override
 	public void onCreate(Bundle b) {
 		super.onCreate(b);
 
 		setContentView(R.layout.order_detail);
 
 		Log.d(TAG, "inside onCreate");
 
		String orderid = this.getIntent().getExtras().getString("catid");
 		String username = this.getIntent().getExtras().getString("username");
 		String token = this.getIntent().getExtras().getString("token");
 		String location = this.getIntent().getExtras().getString("location");
 		String created_date = this.getIntent().getExtras()
 				.getString("created_date");
 		String status = this.getIntent().getExtras().getString("status");
 
 		System.out.println(" " + orderid + " " + status + " " + created_date
 				+ " " + location);
 
 		TextView orderidView;
 		TextView statusView;
 		TextView created_dateView;
 		String phoneLanguage = getResources().getConfiguration().locale
 				.getLanguage();
 		if (phoneLanguage.equals("en"))
 			setTitle("Order " + orderid);
 		else
 			setTitle("Orden " + orderid);
 
 		orderidView = (TextView) findViewById(R.id.detail_orderid);
 		statusView = (TextView) findViewById(R.id.detail_orderstatus);
 		created_dateView = (TextView) findViewById(R.id.detail_created_date);
 		imView = (ImageView) findViewById(R.id.ordermap);
 		orderidView.setText(orderid);
 		statusView.setText(status);
 		created_dateView.setText(created_date);
 		downloadFile(location);
 
 	}
 
 	void downloadFile(String fileUrl) {
 		URL myFileUrl = null;
 		Log.d(TAG, fileUrl);
 		try {
 			myFileUrl = new URL(fileUrl);
 			Log.d(TAG, fileUrl);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			HttpURLConnection conn = (HttpURLConnection) myFileUrl
 					.openConnection();
 			conn.setDoInput(true);
 			conn.connect();
 			InputStream is = conn.getInputStream();
 
 			bmImg = BitmapFactory.decodeStream(is);
 			imView.setImageBitmap(bmImg);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
