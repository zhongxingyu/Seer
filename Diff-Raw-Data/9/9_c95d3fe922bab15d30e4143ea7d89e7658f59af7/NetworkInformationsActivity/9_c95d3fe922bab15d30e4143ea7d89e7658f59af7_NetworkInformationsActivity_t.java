 package ch.bfh.evoting.votinglib;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewTreeObserver;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 
 public class NetworkInformationsActivity extends Activity {
 
 	private boolean paramsAvailable = false;
 	private String ssid;
 	private String password;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_network_informations);
 		setupActionBar();
 
 		ssid = AndroidApplication.getInstance().getNetworkInterface()
 				.getNetworkName();
 		password = AndroidApplication.getInstance().getNetworkInterface()
 				.getConversationPassword();
 		if (password == null) {
 			ssid = getString(R.string.not_connected);
 			password = getString(R.string.not_connected);
 			paramsAvailable = false;
 		} else {
 			paramsAvailable = true;
 		}
 
 		TextView tv_network_name = (TextView) findViewById(R.id.textview_network_name);
 		tv_network_name.setText(ssid);
 
 		TextView tv_network_password = (TextView) findViewById(R.id.textview_network_password);
 		tv_network_password.setText(password);
 
 		// TODO QR-code and NFC
 		if (paramsAvailable) {
 
 			final ImageView ivQrCode = (ImageView) findViewById(R.id.imageview_qrcode);
 
 			ivQrCode.getViewTreeObserver().addOnPreDrawListener(
 					new ViewTreeObserver.OnPreDrawListener() {
 						public boolean onPreDraw() {
 							int width = ivQrCode.getMeasuredHeight();
 							int height = ivQrCode.getMeasuredWidth();
 							Log.d(this.getClass().getSimpleName(), "Width: "
 									+ width);
 							Log.d(this.getClass().getSimpleName(), "Height: "
 									+ height);
 
 							int size;
 
 							if (height > width) {
 								size = width;
 							} else {
 								size = height;
 							}
 
 							try {
 								QRCodeWriter writer = new QRCodeWriter();
 								BitMatrix qrcode = writer.encode(ssid + "||"
 										+ password, BarcodeFormat.QR_CODE,
 										size, size);
 								ivQrCode.setImageBitmap(qrCode2Bitmap(qrcode));
 
 							} catch (WriterException e) {
 								Log.d(this.getClass().getSimpleName(),
 										e.getMessage());
 							}
 							return true;
 						}
 
 					});
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		// do nothing because we don't want that people access to an anterior
 		// activity
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.network_informations, menu);
 		return true;
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			if (this.getIntent().getBooleanExtra("goToMain", false)) {
 				NavUtils.navigateUpFromSameTask(this);
 			} else {
 				super.onBackPressed();
 			}
 			return true;
 		} else if (item.getItemId() == R.id.help) {
 			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
 					getString(R.string.help_title_network_info),
 					getString(R.string.help_text_network_info));
 			hdf.show(getFragmentManager(), "help");
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private Bitmap qrCode2Bitmap(BitMatrix qrcode) {
		
		final int WHITE = 0x00EAEAEA;
		final int BLACK = 0xFF000000;
		
 		int width = qrcode.getWidth();
 		int height = qrcode.getHeight();
 		int[] pixels = new int[width * height];
 		for (int y = 0; y < height; y++) {
 			int offset = y * width;
 			for (int x = 0; x < width; x++) {
 				pixels[offset + x] = qrcode.get(x, y) ? BLACK : WHITE;
 			}
 		}
 
 		Bitmap bitmap = Bitmap.createBitmap(width, height,
 				Bitmap.Config.ARGB_8888);
 		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
 		return bitmap;
 	}
 
 }
