 /*
  * Copyright 2012 Matthew Stock - http://www.bexkat.com/
  * 
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.bexkat.feedlib;
 
 import java.net.URISyntaxException;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
import android.util.Log;
 import android.view.View;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.bexkat.feedlib.db.Item;
 import com.bexkat.feedlib.db.ItemTable;
 
 public class ItemDetailActivity extends SherlockFragmentActivity {
	private static final String TAG = "ItemDetailActivity";
 	ItemTable mItemTable;
 	long mItemId;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.Theme_Sherlock);
 		super.onCreate(savedInstanceState);
 
		if (savedInstanceState == null) {
			Fragment f = new ItemDetailFragment();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(android.R.id.content, f).commit();
		}
 
 		// Pull up the item to display
 		Intent intent = getIntent();
 		mItemId = intent.getLongExtra(ItemTable._ID, 0);
 
 		mItemTable = new ItemTable(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Bundle b = new Bundle();
 		Item item = mItemTable.getItem(mItemId);
 
 		Fragment f = getSupportFragmentManager().findFragmentById(
 				android.R.id.content);
 		b.putString("title", item.getTitle());
 		b.putString("pubDate", item.getPubdate().toString());
 		b.putString("content", item.getContent());
 		try {
 			b.putString("URI", item.getLink().toURI().toString());
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 		((ItemDetailFragment) f).updateState(b);
 	}
 
	@Override
	public void onRestoreInstanceState(Bundle inState) {
		Log.d(TAG, "Loading item state");
		super.onRestoreInstanceState(inState);
		mItemId = inState.getLong("id");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "Saving item state");
		outState.putLong("id", mItemId);
		super.onSaveInstanceState(outState);
	}

 	public void onClick(View v) {
 		Item item = mItemTable.getItem(mItemId);
 
 		if (v.getId() == R.id.web_view) {
 			try {
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item
 						.getLink().toURI().toString()));
 				startActivity(intent);
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 			finish();
 		}
 	}
 }
