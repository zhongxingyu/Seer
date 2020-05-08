 package com.escoand.android.wceu;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.webkit.WebView;
 
 public class ArticleActivity extends Activity {
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.article);
 
 		/* action bar */
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		Cursor cursor = null;
 		WebView view = (WebView) findViewById(R.id.articleText);
 		String html = "";
 
 		/* get article */
 		// TODO: double used database - close previous cursor
 		cursor = new NewsDatabase(getBaseContext()).getDate(getIntent()
 				.getExtras().getString("date"));
 
 		/* show article */
 		if (cursor != null && view != null) {
 			html = String
 					.format("<html><head>"
 							+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"
 							+ "</head><body>"
 							+ "<h2>%s</h2><h3>%s</h3><div>%s</div>"
 							+ "</body></html>",
 							cursor.getString(cursor
 									.getColumnIndex(NewsDatabase.COLUMN_TITLE)),
 							cursor.getString(cursor
 									.getColumnIndex(NewsDatabase.COLUMN_AUTHOR)),
 							cursor.getString(
 									cursor.getColumnIndex(NewsDatabase.COLUMN_TEXT))
 									.replace("href=\"../",
 											"href=\"http://www.worldsceunion.org/"));
 
			view.loadData(html, "text/html", "UTF-8");
 
 			cursor.close();
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		/* up button clicked */
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 }
