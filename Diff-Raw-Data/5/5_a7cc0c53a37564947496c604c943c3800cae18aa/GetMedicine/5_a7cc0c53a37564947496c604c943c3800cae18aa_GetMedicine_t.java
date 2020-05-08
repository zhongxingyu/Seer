 package com.possebom.mypharmacy;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.possebom.mypharmacy.model.Medicine;
 
 public class GetMedicine extends AsyncTask<Void, Void, Void> {
 	private static final String	TAG	= "MEDICINE";
 	private String barcode;
 	private ProgressDialog progressDialog;
 	private JSONObject json;
 	private String country;
 	private GetMedicineListener listener;
 
 	public GetMedicine(ProgressDialog progressDialog,GetMedicineListener listener, String barcode, String country) {
 		this.barcode = barcode;
 		this.country = country;
 		this.listener = listener;
 		this.progressDialog = progressDialog;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		progressDialog.show();
 	}
 
 	@Override
 	protected void onPostExecute(Void result) {
 		super.onPostExecute(result);
 		Medicine medicine = new Medicine();
 		
 		try {
 			medicine.setBrandName(json.getString("brandName"));
 			medicine.setDrug(json.getString("drug"));
 			medicine.setConcentration(json.getString("concentration"));
 			medicine.setForm(json.getString("form"));
 			medicine.setLaboratory(json.getString("laboratory"));
		} catch (Exception e) {
 			Log.e(TAG, "Error on json : " + e.toString());
 		}
 
 		listener.onRemoteCallComplete(medicine);
 		
		if (progressDialog != null && progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 
 	}
 
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		HttpClient httpclient = new DefaultHttpClient();
 		ResponseHandler<String> handler = new BasicResponseHandler();
 		HttpGet request = new HttpGet("http://possebom.com/android/mypharmacy/getMedicine.php?country="+country+"&barcode="+barcode);
 		try {
 			String result = new String(httpclient.execute(request, handler).getBytes("ISO-8859-1"),"UTF-8");
 			JSONArray jsonArray = new JSONArray(result);
 			json = jsonArray.getJSONObject(0);
 			httpclient.getConnectionManager().shutdown();
 		} catch (Exception e) {
 			json = null;
 			Log.e(TAG, "Error converting result " + e.toString());
 		}
 		return null;
 	}
 	
 	public interface GetMedicineListener {
         public void onRemoteCallComplete(Medicine medicine);
     }
 
 }
