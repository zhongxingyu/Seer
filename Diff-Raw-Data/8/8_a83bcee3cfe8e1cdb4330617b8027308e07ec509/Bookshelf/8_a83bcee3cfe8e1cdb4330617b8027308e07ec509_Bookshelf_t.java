 // Copyright 2009 Shun'ichi Shinohara
 
 // This file is part of ShotQuote.
 //
 // ShotQuote is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ShotQuote is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ShotQuote.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.tumblr.shino.shotquote;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Keep information about books
  * @author shino
  */
 public class Bookshelf extends Activity {
 	public static final String DATA_BOOK_INFORMATION = "BOOK_INFORMATION";
 	
 	private static final int MENU_ITEM_ADD = 0;
 	
 	private static final int INTENT_BARCODE_SCAN = 0;
 
 	private BookDatabase bookDatabase;
 	private ListView bookListView;
 	private ArrayAdapter<Book> bookListAdapter;
 	private Book selectedBook;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 
 		setTitle(R.string.bookshelf_title);
 		setContentView(R.layout.bookshelf);
 
 		bookListView = (ListView) findViewById(R.id.bookshelf_list);
         registerForContextMenu(bookListView);
     			bookDatabase = new BookDatabase(this);
 		List<Book> books = bookDatabase.allBooks();
 		bookListAdapter = new BookArrayAdapeter(this, R.layout.book_item, books);
 		bookListView.setAdapter(bookListAdapter);
 
 		bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		        ListView listView = (ListView) parent;
 		        Book book = (Book) listView.getItemAtPosition(position);
 		        View bookView = listView.getChildAt(position);
 		        U.debugLog(this, "Clicked item is the book", book);
 		        U.debugLog(this, "Clicked item's view", bookView);
 		        selectedBook = book;
 		        bookView.showContextMenu();
 		        registerForContextMenu(bookListView);
 		    }
 		});
 		
 		// TODO: What does "selected" mean? Look into it later. 
 		bookListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 		    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		        ListView listView = (ListView) parent;
 		        Book book = (Book) listView.getSelectedItem();
 		        U.debugLog(this, "Selected item is the book", book);
 		    }
 		    public void onNothingSelected(AdapterView<?> parent) {
 		        U.debugLog(this, "Nothing is selected from the bookshelf", this);
 		    }
 		});
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuItem item0 = menu.add(0, MENU_ITEM_ADD, 0, R.string.label_add);
 		item0.setIcon(android.R.drawable.ic_menu_add);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()) {
 		case MENU_ITEM_ADD:
 			scanBookBarcode();
 			return true;
 		default:
 			U.showDialog(this, "Menu button " + item.getTitle() + " is pressed.",
 					"Not yet implemented");
 			return true;
 		}
 	}
 
 	final static int CONTEXT_ITEM_BOOK_SELECT = 1000;
 	final static int CONTEXT_ITEM_BOOK_DELETE = 1001;
 	final static int CONTEXT_ITEM_BOOK_SHARE = 1002;
     //コンテキストメニューが生成される時に起動される
     public void onCreateContextMenu(ContextMenu menu, final View view,
     		final ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, view, menuInfo);
     	unregisterForContextMenu(view);
     	menu.add(0, CONTEXT_ITEM_BOOK_SELECT, 0, R.string.label_select);
     	menu.add(0, CONTEXT_ITEM_BOOK_DELETE, 0, R.string.label_delete);
     	menu.add(0, CONTEXT_ITEM_BOOK_SHARE, 0, R.string.label_share);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     		case CONTEXT_ITEM_BOOK_SELECT:
 				U.debugLog(this, "Menu \"Select\" is selected", selectedBook);
 				Intent selectResult = new Intent();
 				selectResult.putExtra(DATA_BOOK_INFORMATION, selectedBook);
 				setResult(RESULT_OK, selectResult);
 				finish();
 				return true;
     		case CONTEXT_ITEM_BOOK_DELETE:
 				U.debugLog(this, "Menu \"Delete\" is selected", selectedBook);
 				bookDatabase.delete(selectedBook);
 				bookListAdapter.remove(selectedBook);
 				bookListView.setAdapter(bookListAdapter);
 				U.showToast(this, "Deleted: " + selectedBook.getTitle());
 				return true;
     		case CONTEXT_ITEM_BOOK_SHARE:
 				U.debugLog(this, "Menu \"Share\" is selected", selectedBook);
 				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 				shareIntent.setType("text/plain");
 				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, selectedBook.getTitle());
 				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, selectedBook.getUrl() + " " + selectedBook.getAuthors());
 				startActivity(shareIntent);
 				return true;
     		default:
     			return false;
     	}
     }
       	
 	private void scanBookBarcode(){
         Intent barcodeReaderIntent = new Intent("com.google.zxing.client.android.SCAN");
         barcodeReaderIntent.putExtra("SCAN_MODE", "ONE_D_MODE");
         // TODO: Confirm Barcode Scanner is installed here.
        try {
        	startActivityForResult(barcodeReaderIntent, INTENT_BARCODE_SCAN);
		} catch (ActivityNotFoundException notfound) {
			U.showDialog(this, "ERROR", "Barcode scanner is needed. Install some app.");
		}        	
 	}
 	
 	public class BookSearchTask extends AsyncTask<Intent, Integer, Book>{
 
 		private Bookshelf activity;
 		private ProgressDialog progressDialog;
 		
 		public BookSearchTask(Bookshelf activity){
 			this.activity = activity;
 		}
 
 		@Override
 		protected void onPreExecute() {
 		    progressDialog = new ProgressDialog(Bookshelf.this);
 		    progressDialog.setTitle(R.string.message_processing);
 		    progressDialog.setMessage(getString(R.string.message_loading));
 		    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		    progressDialog.setIndeterminate(false);
 		    progressDialog.setMax(100);
 		    progressDialog.show();
 		    this.publishProgressAndSecondary(10, 10);
 		}
 
 		@Override
 		protected Book doInBackground(Intent... intents) {
 			Intent scannedBarcodeIntent = intents[0];
         	Book book = null;
 			try {
 				book = Book.searchAndCreate(scannedBarcodeIntent, Bookshelf.this, this);
 				if(book == null || book.getTitle() == null) {
 					return book;
 				}
 				bookDatabase.add(book);
 				return book;
 			} catch (Exception e) {
 	        	U.errorLog(this, "Error occured in accessing Amazon", e);
 			}
         	return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Book book) {
 		    progressDialog.dismiss();
 		    
 		    if (book != null && book.getTitle() != null) {
 	        	activity.bookListAdapter.insert(book, 0);
 	        	activity.bookListView.setAdapter(activity.bookListAdapter);
 	        	activity.bookListView.invalidate();
 		    } else if(book != null && book.getTitle() == null) {
 		        Toast toast = Toast.makeText(activity,
 		        		getString(R.string.message_book_not_found_for_ISBN) + book.getIsbn(),
 		        		Toast.LENGTH_LONG);
 		        toast.show();
 		    } else {
 		        Toast toast = Toast.makeText(activity, getString(R.string.message_book_not_found),
 		        		Toast.LENGTH_LONG);
 		        toast.show();
 		    }
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progresses) {
 			progressDialog.setProgress(progresses[0]);
 			progressDialog.setSecondaryProgress(progresses[1]);
 		}
 		
 		public void publishProgressAndSecondary(Integer progress, Integer secondary){
 			super.publishProgress(progress, secondary);
 		}
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, final Intent result) {
 	    if (requestCode == INTENT_BARCODE_SCAN) {
 	        if (resultCode == RESULT_OK) {
 	            U.debugLog(this, "Barcode scan result", result);
 	            BookSearchTask task = new BookSearchTask(this);
 	            task.execute(result);
 	        } else if (resultCode == RESULT_CANCELED) {
 	            U.debugLog(this, "Barcode scan is canceled.", Integer.toString(resultCode));
 	        } else {
 	            U.debugLog(this, "Barcode scan result.", result.toString());
 	        }
 	    } else {
 	    	U.showDialog(this, "Unknow activity request code", Integer.toString(requestCode));
 	    }
 	}
 	
 	@Override
 	protected void onRestart(){
 		super.onRestart();
 		bookDatabase = new BookDatabase(this);
 	}
 	@Override
 	protected void onStop() {
 		super.onStop();
 		bookDatabase.close();
 		bookDatabase = null;
 	}
 	
 	public void testSearchBook(){
 		Intent intentForDebug = new Intent();
 		intentForDebug.putExtra("SCAN_RESULT", "9784883376636");
 		intentForDebug.putExtra("SCAN_RESULT_FORMAT", "EAN_13");
 		onActivityResult(INTENT_BARCODE_SCAN, RESULT_OK, intentForDebug);
 	}
 
 }
