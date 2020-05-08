 package de.cstimming.konphidroid;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.ColorStateList;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class KonPhiActivity extends Activity implements LocationListener {
 
 	private ToggleButton m_togglebuttonGps;
 	private ToggleButton m_togglebuttonSender;
 
 	private SimpleDateFormat m_dateFormatter;
 	private SimpleDateFormat m_dateFormatSender;
 
 	private Location m_lastLocation;
 	private boolean m_lastLocationValid;
 
 	private long m_senderIntervalSecs;
 	private String m_server = "http://carcomm.cstimming.de/";
 
 	//private WebView m_webview;
 	private TextView m_labelSender;
 
 	private static final int DIALOG_GPSWARNING = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		m_dateFormatSender = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 		m_dateFormatSender.setTimeZone(TimeZone.getTimeZone("GMT00"));
 		m_dateFormatter = new SimpleDateFormat("HH:mm:ss");
 		m_senderIntervalSecs = 30;
 		m_togglebuttonGps = (ToggleButton) findViewById(R.id.togglebuttonGps);
 		m_togglebuttonSender = (ToggleButton) findViewById(R.id.togglebuttonSender);
 		m_labelSender = (TextView) findViewById(R.id.TextLabelSender);
 
 		//final LinearLayout ell = (LinearLayout) findViewById(R.id.LayoutWebview);
 		//m_webview = new WebView(this);
 		//ell.addView(m_webview);
 
 		final LocationManager locationManager = (LocationManager) getApplicationContext()
 		.getSystemService(Context.LOCATION_SERVICE);
 
 		m_togglebuttonGps.setChecked(locationManager
 				.isProviderEnabled(LocationManager.GPS_PROVIDER));
 
 		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 			showDialog(DIALOG_GPSWARNING);
 		}
 
 		m_togglebuttonGps.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on clicks
 				final Intent i = new Intent(
 						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				startActivity(i);
 				m_togglebuttonGps.setChecked(locationManager
 						.isProviderEnabled(LocationManager.GPS_PROVIDER));
 			}
 		});
 		m_togglebuttonSender.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final TextView resultview = (TextView) findViewById(R.id.TextViewSender);
 				if (m_togglebuttonSender.isChecked()) {
 					m_labelSender.setText(R.string.labelsender_on);
 					final TextView viewSecs = (TextView) findViewById(R.id.TextViewSecs);
 					m_labelSender.setTextColor(viewSecs.getTextColors());
 					if (locationManager
 							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
 						// Show the toast only if GPS is already on, but not if
 						// we open the dialog as this would interfere visually
 						// with the toast.
 						Toast.makeText(KonPhiActivity.this,
 								R.string.transmission_on_toast,
 								Toast.LENGTH_SHORT).show();
 					} else {
 						showDialog(DIALOG_GPSWARNING);
 					}
 				} else {
 					Toast.makeText(KonPhiActivity.this,
 							R.string.transmission_off_toast, Toast.LENGTH_SHORT)
 							.show();
 					m_labelSender.setText(R.string.labelsender_off);
 					m_labelSender.setTextColor(ColorStateList.valueOf(Color.RED));
 					resultview.setTextColor(ColorStateList.valueOf(Color.GRAY));
 				}
 			}
 		});
 
 		long minTime = 1000 * m_senderIntervalSecs; // [milliseconds]
 		float minDistance = 0; // 10; // [meters]
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				minTime, minDistance, this);
 	}
 
 	protected Dialog onCreateDialog(int d) {
 		Dialog result;
 		switch (d) {
 		case DIALOG_GPSWARNING:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.ask_activate_gps_settings)
 			.setCancelable(false)
 			.setPositiveButton(R.string.yes,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					final Intent i = new Intent(
 							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 					startActivity(i);
 				}
 			})
 			.setNegativeButton(R.string.no,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 						int id) {
 					dialog.cancel();
 				}
 			});
 			result = builder.create();
 			break;
 		default:
 			result = null;
 		}
 		return result;
 	}
 
 	@Override
 	public void onLocationChanged(Location loc) {
 		// Toast.makeText(KonPhiActivity.this, "Received location from " +
 		// loc.getProvider(), Toast.LENGTH_SHORT).show();
 
 		printLocInfo(loc);
 
 		if (m_lastLocationValid) {
 			long secdiff = (loc.getTime() - m_lastLocation.getTime()) / 1000;
 			if (secdiff >= m_senderIntervalSecs) {
				if (secdiff <= 4 * m_senderIntervalSecs) {
					sendPairNow(m_lastLocation, loc);
				}
 				m_lastLocation = loc;
 			}
 			// Note: We don't set the m_lastLocation if we are smaller than
 			// m_senderIntervalSecs.
 		} else {
 			m_lastLocationValid = true;
 			m_lastLocation = loc;
 		}
 	}
 
 	private void printLocInfo(Location loc) {
 		final TextView viewgpsage = (TextView) findViewById(R.id.TextViewGPSAge);
 		final TextView viewLat = (TextView) findViewById(R.id.TextViewLat);
 		final TextView viewLon = (TextView) findViewById(R.id.TextViewLon);
 		final TextView viewAcc = (TextView) findViewById(R.id.TextViewAcc);
 		final TextView viewDist = (TextView) findViewById(R.id.TextViewDist);
 		final TextView viewSecs = (TextView) findViewById(R.id.TextViewSecs);
 
 		viewgpsage.setText(m_dateFormatter.format(new Date(loc.getTime())));
 		viewLat.setText(String.valueOf(loc.getLatitude()));
 		viewLon.setText(String.valueOf(loc.getLongitude()));
 		viewAcc.setText(loc.hasAccuracy() ? String.valueOf(loc.getAccuracy())
 				: "...");
 
 		if (m_lastLocationValid) {
 			long secdiff = (loc.getTime() - m_lastLocation.getTime()) / 1000;
 			String secs = String.valueOf(secdiff);
 			String dist = String.valueOf(m_lastLocation.distanceTo(loc));
 			viewDist.setText(dist);
 			viewSecs.setText(secs);
 		} else {
 			viewDist.setText("...");
 			viewSecs.setText("...");
 		}
 	}
 
 	private String degToString(double deg) {
 		return String.valueOf(deg);
 	}
 
 	private void sendPairNow(Location startLoc, Location endLoc) {
 		// Only send if the togglebutton is active
 		if (!m_togglebuttonSender.isChecked())
 			return;
 
 		String startLat = degToString(startLoc.getLatitude());
 		String startLon = degToString(startLoc.getLongitude());
 		String endLat = degToString(endLoc.getLatitude());
 		String endLon = degToString(endLoc.getLongitude());
 		String duration = String
 		.valueOf((endLoc.getTime() - startLoc.getTime()) / 1000);
 		String endTime = m_dateFormatSender.format(new Date(endLoc.getTime()));
 		// FIXME: Need to transfer speed and accuracy at both points, and also
 		// group selection
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(m_server + "slices");
 
 		final TextView resultview = (TextView) findViewById(R.id.TextViewSender);
 		resultview.setText("Sending to " + httppost.getURI().toString());
 		String displaytime = m_dateFormatter.format(new Date(endLoc.getTime()))
 		+ ": ";
 		try {
 			// Add your data
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
 			nameValuePairs.add(new BasicNameValuePair("slice[startlat]",
 					startLat));
 			nameValuePairs.add(new BasicNameValuePair("slice[startlon]",
 					startLon));
 			nameValuePairs.add(new BasicNameValuePair("slice[lat]", endLat));
 			nameValuePairs.add(new BasicNameValuePair("slice[lon]", endLon));
 			nameValuePairs.add(new BasicNameValuePair("slice[duration]",
 					duration));
 			nameValuePairs.add(new BasicNameValuePair("slice[time]", endTime));
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			// httppost.getEntity().writeTo(baos);
 			// m_webview.loadData(baos.toString(), "text/html", "utf-8");
 
 			// Execute HTTP Post Request
 			HttpResponse response = httpclient.execute(httppost);
 			// baos.reset();
 			// response.getEntity().writeTo(baos);
 			// m_webview.loadData(baos.toString(), "text/html", "utf-8");
 			StatusLine rstatus = response.getStatusLine();
 			int rcode = rstatus.getStatusCode();
 			if (rcode >= 200 && rcode < 300) {
 				resultview.setText(displaytime + String.valueOf(rcode) + " "
 						+ rstatus.getReasonPhrase());
 				resultview.setTextColor(ColorStateList.valueOf(Color.GREEN));
 			} else {
 				resultview.setText(displaytime + String.valueOf(rcode) + " "
 						+ rstatus.getReasonPhrase());
 				resultview.setTextColor(ColorStateList.valueOf(Color.RED));
 			}
 		} catch (ClientProtocolException e) {
 			resultview.setText(displaytime + "Sending failed (Protocol): "
 					+ e.getLocalizedMessage());
 			resultview.setTextColor(ColorStateList.valueOf(Color.RED));
 		} catch (IOException e) {
 			resultview.setText(displaytime + "Sending failed (IO): "
 					+ e.getLocalizedMessage());
 			resultview.setTextColor(ColorStateList.valueOf(Color.RED));
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 		// " disabled", Toast.LENGTH_SHORT).show();
 		m_togglebuttonGps.setChecked(false);
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// Toast.makeText(KonPhiActivity.this, "Good: Provider " + provider +
 		// " enabled", Toast.LENGTH_SHORT).show();
 		m_togglebuttonGps.setChecked(true);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		switch (status) {
 		case LocationProvider.OUT_OF_SERVICE:
 			// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 			// " out of service", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(false);
 			break;
 		case LocationProvider.TEMPORARILY_UNAVAILABLE:
 			// Toast.makeText(KonPhiActivity.this, "Oh: Provider " + provider +
 			// " tmp. unavailable", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(false);
 			break;
 		case LocationProvider.AVAILABLE:
 			// Toast.makeText(KonPhiActivity.this, "Good: Provider " + provider
 			// + " available", Toast.LENGTH_SHORT).show();
 			m_togglebuttonGps.setChecked(true);
 			break;
 		}
 	}
 }
