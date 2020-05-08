 package com.example.bookstoremb;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.example.bookstoremb.adapter.BookstoreAdapter;
 import com.example.bookstoremb.models.Book;
 import com.example.bookstoremb.utils.Constants;
 import com.example.bookstoremb.utils.RestClient;
 import com.example.bookstoremb.utils.Utils;
 import com.example.bookstoremb.wrapper.SearchWrapper;
 
 /**
  * Created by The eXo Platform SAS
  * Author : BinhNV
  *          binhnv@exoplatform.com
  * Jul 5, 2012  
  */
@SuppressLint("NewApi")
 public class MainActivity extends ListActivity {
 
     //List of book which will be show to screen
     private List<Book> books;
     //The button allow user back to search all book screen
     private Button btnReturn;
     //The text view will be show when no data retrieve from web service
     private TextView nocontent;
     //The Main Activity
     private LinearLayout main;
     //search condition for search
     private String searchCondition;
     //The name of book
     private String bookName;
     
     /**
      * create main screen for app
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         //Check version of android sdk
         //if android sdk large then 9, will be add StrictMode for make sure long thread can execute
         if (Build.VERSION.SDK_INT > 9) {
           StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
           StrictMode.setThreadPolicy(policy);
         }
         
         //init all view for main activity
         initView();
         
         //get extract from intent
         Bundle extras = getIntent().getExtras();
         if (extras == null) {
           //case once time access to app
           main.removeView(btnReturn);
           searchCondition = Constants.SEARCH_ALL_BOOK_URL;
           renderList(Constants.SEARCH_ALL_BOOK_URL);
         } else {
           bookName = extras.getString(Constants.BOOK_NAME);
           searchCondition = extras.getString(Constants.SEARCH_CONDITION);
           //don't have book name => not search or search all
           if (bookName == null || "".equals(bookName)) {
             renderList(searchCondition);
           } else {
             //search with condition
             searchCondition = Constants.SEARCH_BOOK_BY_NAME + bookName.replaceAll(" ", "");
             renderList(Constants.SEARCH_BOOK_BY_NAME + bookName.replaceAll(" ", ""));
           }
         }
         
     }
 
     /**
      * init all view for main screen
      */
     private void initView() {
       bookName = "";
       searchCondition = "";
       //init main layout
       main = (LinearLayout) findViewById(R.id.main);
       //init text view
       nocontent = new TextView(this);
       nocontent.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
       nocontent.setText(R.string.nocontent);
       //init book list
       books = new ArrayList<Book>();
       //init button and add listener for it
       btnReturn = (Button) findViewById(R.id.btnReturn);
       btnReturn.setOnClickListener(new OnClickListener() {
         
         @Override
         public void onClick(View v) {
           main.removeView(btnReturn);
           main.removeView(nocontent);
           renderList(Constants.SEARCH_ALL_BOOK_URL);
         }
         
       });
     }
     
     /**
      * retrieve data from web service and render to screen 
      * 
      * @param url
      */
     private void renderList(String url) {
       try {
         //create adapter
         BookstoreAdapter adapter;
         //define rest client object
         RestClient rest = new RestClient(url);
         //add get method for rest client object
         rest.execute(RestClient.RequestMethod.GET);
         books = new ArrayList<Book>();
         //check response code from web service
         if (rest.getResponseCode() == 200) {
           //get data to json array
           JSONArray jsons = (JSONArray) new JSONArray(rest.getResponseStr());
           for (int i = 0; i < jsons.length(); i++) {
             //parse to json object and create book object depend on json
             JSONObject json = jsons.getJSONObject(i);
             Book book = Utils.createBookFromJSON(json);
             books.add(book);
           }
           //set data to adapter
           adapter = new BookstoreAdapter(this, books);
           this.setListAdapter(adapter);
         } else {
           //case response code return error
           adapter = new BookstoreAdapter(this, books);
           this.setListAdapter(adapter);
           prepareViewResponseError();
           main.addView(btnReturn, 1);
         }
       } catch (JSONException e) {
         e.printStackTrace();
         prepareViewResponseError();
       } catch (ClientProtocolException e) {
         e.printStackTrace();
         prepareViewResponseError();
       } catch (IOException e) {
         e.printStackTrace();
         prepareViewResponseError();
       }
     }
     
     /**
      * prepare view when response return error
      */
     private void prepareViewResponseError() {
       main.removeView(btnReturn);
       main.removeView(nocontent);
       main.addView(nocontent, 0);
     }
     
     /* (non-Javadoc)
      * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
      */
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
       super.onListItemClick(l, v, position, id);
       //create book object
       Book book = new Book();
       book = books.get(position);
       //define intent and put extra to intent
       Intent intent = new Intent(this, ContentActivity.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       intent.putExtra(Constants.BOOK_ID, book.getBookId());
       intent.putExtra(Constants.BOOK_NAME, book.getName());
       intent.putExtra(Constants.BOOK_CATEGORY, Utils.bookCategoryEnumToString(book.getCategory()));
       intent.putExtra(Constants.BOOK_CONTENT, book.getContent());
       intent.putExtra(Constants.SEARCH_CONDITION, searchCondition);
       startActivity(intent);
     }
     
     /* (non-Javadoc)
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
       //define menu with 2 option: close and search
       menu.add(menu.NONE, Constants.MENU_CLOSE, menu.NONE, R.string.menu_close);
       menu.add(menu.NONE, Constants.MENU_SEARCH, menu.NONE, R.string.menu_search);
       return super.onCreateOptionsMenu(menu);
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
      */
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
       //add action for menu
       switch (item.getItemId()) {
       //assign action for close menu
       case Constants.MENU_CLOSE:
         finish();
         System.exit(0);
         break;
       //assign action for search menu
       case Constants.MENU_SEARCH:
         //create layout inflater
         LayoutInflater inflater = LayoutInflater.from(this);
         View searchView = inflater.inflate(R.layout.activity_search, null);
         //create search wrapper which allow show popup search to screen
         final SearchWrapper wrapper = new SearchWrapper(searchView, this);
         final Context nestContext = this;
         //add alert dialog for search show to screen
         new AlertDialog.Builder(this).setTitle(R.string.tltsearch)
                                    .setView(searchView)
                                    //add positive button
                                    .setPositiveButton(R.string.btnsearch, new DialogInterface.OnClickListener() {
 
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                        String bookName = wrapper.getBookNameStr();
                                        Intent intent = new Intent(nestContext, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra(Constants.BOOK_NAME, bookName);
                                        startActivity(intent);
                                      }
                                    })
                                    //add negative button
                                    .setNegativeButton(R.string.btncancel, new DialogInterface.OnClickListener() {
 
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                        
                                      }
                                    })
                                    .show();
         break;
       }
       return super.onMenuItemSelected(featureId, item);
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
      */
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
       //check key code and add event
       if (keyCode == KeyEvent.KEYCODE_BACK) {
         if (bookName != null || "".equals(bookName)) {
           main.removeView(btnReturn);
           main.removeView(nocontent);
           bookName = "";
           searchCondition = Constants.SEARCH_ALL_BOOK_URL;
           renderList(Constants.SEARCH_ALL_BOOK_URL);
           return true;
         }
       }
       return super.onKeyDown(keyCode, event);
     }
     
 }
