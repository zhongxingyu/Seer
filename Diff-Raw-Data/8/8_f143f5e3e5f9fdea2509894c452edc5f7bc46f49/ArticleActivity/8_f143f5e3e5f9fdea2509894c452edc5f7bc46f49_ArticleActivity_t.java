 package com.escoand.android.wceu;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MenuItem;
 
 public class ArticleActivity extends Activity {
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		ArticleFragment article = new ArticleFragment();
 		article.setArguments(getIntent().getExtras());
 		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, article).commit();
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
