 package com.keyboardr.xdiexplorer;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MainActivity extends Activity implements LoaderCallbacks<Cursor> {
 
 	private static final Uri CONTENT_URI = Uri.parse("content://com.google.android.music.xdi/browse/1");
 
 	// Allowable paths:
 	//
 	// launcher
 	// launcher/items/
 	// launcher/items/#
 	// browse/headers
 	// browse/#
 	// mymusic/headers
 	// mymusic/#
 	// mygenres/headers
 	// mygenres/#
 	// details/albums
 	// details/albums/*/sections
 	// details/albums/*/tracks
 	// details/albums/*/actions
 	// details/playlists/#
 	// details/playlists/#/sections
 	// details/playlists/#/tracks
 	// details/playlists/#/actions
 	// details/artists/*
 	// details/artists/*/sections
 	// details/artists/*/albums
 	// details/artists/*/actions
 	// details/artists/*/topsongs
 	// search
 	// search/headers/#
 	// explore/headers
 	// explore/#
 	// explore/featured/headers
 	// explore/featured/*/*
 	// explore/recommendations/headers
 	// explore/recommendations/*/*
 	// explore/newreleases/headers
 	// explore/newreleases/*/*
 	// explore/genres/*/*
 	// explore/genres/*/*/headers
 	// explore/genre/featured/items/*/*/*
 	// explore/genre/newreleases/items/*/*/*
 	// explore/genre/featured/*/headers
 	// explore/genre/newreleases/*/headers
 	// explore/genre/items/*
 	// explore/genre/*/headers
 	// meta/#
 	// metatitle/*﻿
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		boolean useLoader = true;
 		if (useLoader) {
 			getLoaderManager().initLoader(0, null, this);
 		} else {
 			Intent intent = new Intent("com.google.android.music.xdi.intent.PLAY");
 			intent.putExtra("container", 1); // int
 			// intent.putExtra("name", name); //String
 			intent.putExtra("id", 2612592938l); // long
 			intent.putExtra("id_string", "2612592938"); // String
			// intent.putExtra("offset", offset); //int
			// intent.putExtra("artUri", artUri); //String
 			startActivity(intent);
 			finish();
 		}
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		return new CursorLoader(this, CONTENT_URI, null, null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
 		boolean playFoundSong = true;
 
 		if (cursor.moveToFirst()) {
 			Log.v("Cursor Count", "" + cursor.getCount());
 			List<String> columns = new ArrayList<String>();
 			for (int i = 0; i < cursor.getColumnCount(); i++) {
 				columns.add(cursor.getColumnName(i));
 			}
 			Log.v("Column Names", buildConcatString(", ", columns.toArray(new String[columns.size()])));
 
 			do {
 				List<String> row = new ArrayList<String>();
 				for (int i = 0; i < cursor.getColumnCount(); i++) {
 					row.add(cursor.getString(i));
 				}
 
 				Log.v("Row " + cursor.getPosition(), buildConcatString(", ", row.toArray(new String[row.size()])));
 				if (playFoundSong) {
 					Intent playIntent;
 					try {
 						playIntent = Intent.parseUri(cursor.getString(cursor.getColumnIndex("intent_uri")), 0);
 
 						if ("com.google.android.music.xdi.intent.PLAY".equals(playIntent.getAction())) {
 							Log.v("Row " + cursor.getPosition(), "Found playable");
 							startActivity(playIntent);
 							break;
 						}
 					} catch (URISyntaxException e) {
 						e.printStackTrace();
 					}
 				}
 			} while (cursor.moveToNext());
 
 		}
 		finish();
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 	}
 
 	public static String buildConcatString(String delimeter, String... strings) {
 		StringBuilder builder = new StringBuilder();
 		boolean isFirst = true;
 		for (String string : strings) {
 			if (string == null) {
 				string = "NULL";
 			}
 			if (!isFirst) {
 				builder.append(delimeter);
 			}
 			isFirst = false;
 			builder.append(string);
 		}
 		return builder.toString();
 	}
 
 }
