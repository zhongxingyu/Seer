 package com.escoand.android.wceu;
 
 import android.app.Fragment;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 
 public class ArticleFragment extends Fragment {
 	private String date;
 
 	public ArticleFragment() {
 		super();
 	}
 
 	@Override
 	public void setArguments(Bundle args) {
 		if (args != null)
 			date = args.getString("date");
 		super.setArguments(args);
 	}
 
 	/* create fragment */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		setHasOptionsMenu(true);
 		super.onCreate(savedInstanceState);
 	}
 
 	/* create fragment view */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.article, container, false);
 	}
 
 	/* fragment created */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Cursor cursor = null;
 		WebView view = (WebView) getActivity().findViewById(R.id.articleText);
 		String html = "";
 
 		if (savedInstanceState != null)
 			date = savedInstanceState.getString("date");
 
 		if (view == null || date == null)
 			return;
 
 		/* get article */
 		// TODO double used database - close previous cursor
 		cursor = new NewsDatabase(getActivity()).getDate(date);
 		if (cursor == null || cursor.getCount() < 1)
 			return;
 
 		/* show article */
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
 		view.loadData(html, "text/html; charset=utf-8", "UTF-8");
 		cursor.close();
 	}
 
 	/* fragment stopped */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putString("date", date);
 		super.onSaveInstanceState(outState);
 	}
 }
