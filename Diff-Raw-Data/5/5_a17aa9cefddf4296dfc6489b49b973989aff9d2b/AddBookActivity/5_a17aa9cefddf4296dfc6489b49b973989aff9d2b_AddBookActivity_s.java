 package cs1635.group.booksharing;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.xml.sax.InputSource;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class AddBookActivity extends FragmentActivity {
 	final String URL_START = "http://isbndb.com/api/books.xml?access_key=JBWC5O6E&results=subjects&index1=isbn&value1=";
 	String isbn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_book);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_add_book, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	    	Intent intent = new Intent();
 			intent.putExtra("action", "None");
 			setResult(0, intent);
 			finish();
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	// Called when "Home" button is pressed
 	public void goHome(View view) {
 		Intent intent = new Intent();
 		intent.putExtra("action", "Home");
 		setResult(0, intent);
 		finish();
 	}
 	
 	// Called when "Post" button is pressed
 	public void post(View view) {
 		DialogFragment dialog = new ConfirmPostDialogFragment();
 		dialog.show(getSupportFragmentManager(), "ConfirmPostDialogFragment");
 	}
 	
 	// Called when "Add photo" button is pressed
 	public void addPhoto(View view) {
 		// TODO: Add code here.
 	}
 	
 	// Called when "Scan barcode" button is clicked
 	public void scan(View view) {
 		// Display error dialog and return if not connected to Internet
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
 			DialogFragment dialog = new NoNetworkConnectionDialogFragment();
 			dialog.show(getSupportFragmentManager(), "NoNetworkConnectionDialogFragment");
 		}
 		
 		// Scan barcode
 		IntentIntegrator integrator = new IntentIntegrator(this);
 		integrator.initiateScan();
 	}
 	
 	// Handles result of barcode scan
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if (scanResult != null) {
 			String format = scanResult.getFormatName();
			System.out.println("Format recorded.");
 			// Make sure barcode is proper format. (Does this cover all ISBN barcodes?)
 			if (format.equals("EAN_13")) {
 				try {
 					// Download data and fill out fields
 					isbn = scanResult.getContents();
 					URL url = new URL(URL_START + isbn);
 					new BookDataTask().execute(url);
 				} catch (Exception ex) {
 					System.out.println("Exception caught while attempting to start BookInfoTask: " + ex);
 				}
 			}
 			else {
 				// TODO: Fix crash here.
 				/*
				System.out.println("About to display WrongFormatDialogFragment.");
 				// Display error message
 				DialogFragment dialog = new WrongFormatDialogFragment();
 				dialog.show(getSupportFragmentManager(), "WrongFormatDialogFragment");
 				*/
 			}
 		} else {
 			// TODO: See if this causes a crash.
 			/*
 			// Display error message
 			DialogFragment dialog = new ScanErrorDialogFragment();
 			dialog.show(getSupportFragmentManager(), "ScanErrorDialogFragment");
 			*/
 		}
 	}
 	
 	// Used to retrieve book information from ISBNDB
 	private class BookDataTask extends AsyncTask<URL, Void, cs1635.group.booksharing.BookDataXmlParser.BookData> {
 		@Override
 		protected cs1635.group.booksharing.BookDataXmlParser.BookData doInBackground(URL... url) {
 			InputSource bookData;
 			try {
 				return downloadBookData(url[0]);
 			} catch (Exception ex) {
 				System.out.println("Exception caught while attempting to access book data: " + ex);
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(cs1635.group.booksharing.BookDataXmlParser.BookData result) {
 			// Populate fields with book data
 			((EditText) findViewById(R.id.editText_addBook_isbn)).setText(isbn);
 			((EditText) findViewById(R.id.editText_addBook_title)).setText(result.title);
 			((EditText) findViewById(R.id.editText_addBook_author)).setText(result.author);
 			((EditText) findViewById(R.id.editText_addBook_subject)).setText(result.subject);
 		}
 	}
 	
 	// Downloads book data from ISBNDB.com, parses XML, and returns a BookData object containing information about the book.
 	private cs1635.group.booksharing.BookDataXmlParser.BookData downloadBookData(URL url) throws XmlPullParserException, IOException{
 		InputStream stream = null;
 		BookDataXmlParser parser = new BookDataXmlParser();
 		cs1635.group.booksharing.BookDataXmlParser.BookData bookData = null;
 		
 		// Download data
 		try {
 			stream = downloadUrl(url);
 			bookData = parser.parse(stream);
 		} finally {
 			if (stream != null) {
 				stream.close();
 			}
 		}
 
 		return bookData;
 	}
 	
 	// Downloads book data from ISBNDB.com
 	private InputStream downloadUrl(URL url) throws IOException {
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 		conn.setReadTimeout(10000);
 		conn.setConnectTimeout(15000);
 		conn.setRequestMethod("GET");
 		conn.setDoInput(true);
 		
 		conn.connect();
 		return conn.getInputStream();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent();
 			intent.putExtra("action", "None");
 			setResult(0, intent);
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
