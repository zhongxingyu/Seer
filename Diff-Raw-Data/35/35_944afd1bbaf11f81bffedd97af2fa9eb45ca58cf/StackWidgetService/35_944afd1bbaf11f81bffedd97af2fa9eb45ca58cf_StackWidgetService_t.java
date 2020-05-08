 /*
  * Copyright (C) 2013 Geometer Plus <contact@geometerplus.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  */
 
 package org.geometerplus.fbreader.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import android.annotation.TargetApi;

 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService;
 
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
 import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.IBookCollection;
 import org.geometerplus.fbreader.book.XMLSerializer;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class StackWidgetService extends RemoteViewsService {
 	@Override
 	public RemoteViewsFactory onGetViewFactory(Intent intent) {
 		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
 	}
 }
 
 class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {	
 	public static final int FIRST_COVER_VIEW_TYPE = 0;
 	public static final int NOT_FIRST_COVER_VIEW_TYPE = 1;
 	public static final int VIEW_TYPES_COUNT = 2;
 	
 	private final Context myContext;
 	private final int myAppWidgetId;
 	private final List<Book> myBooks = new ArrayList<Book>();
 	private Boolean myWithBar;
 	private Boolean myWithPercent;
 
 	public StackRemoteViewsFactory(Context context, Intent intent) {
 		myContext = context;
 		myAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 		updateData();
 	}
 
 	@Override
 	public void onCreate() {
 	}
 
 	@Override
 	public void onDestroy() {
 		myBooks.clear();
 	}
 
 	@Override
 	public int getCount() {
 		return myBooks.size();
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public RemoteViews getLoadingView() {
 		return null;
 	}
 
 	@Override
 	public RemoteViews getViewAt(int position) {
 		final SingleWidget singleWidget;
 		if (getItemViewType(position) == FIRST_COVER_VIEW_TYPE) {
 			singleWidget = SingleWidget.create(myAppWidgetId, myWithBar, myWithPercent, myBooks.get(position), SingleWidget.CURRENT_BOOK);
 		} else {
 			singleWidget = SingleWidget.create(myAppWidgetId, false, false, myBooks.get(position), SingleWidget.CURRENT_BOOK);
 		}
 		return new WidgetRemoteViewsHolder().setRemoteViews(myContext, singleWidget, position);
 	}
 
 	private int getItemViewType(int position) {
 		if (position == 0) {
 			return FIRST_COVER_VIEW_TYPE;
 		}
 		return NOT_FIRST_COVER_VIEW_TYPE;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return VIEW_TYPES_COUNT;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public void onDataSetChanged() {
 		updateData();
 	}
 
 	private void updateData() {
 		myBooks.clear();
 		final Set<String> books = SharedPreferencesManager.getBooksSet(myContext, myAppWidgetId);
 		for (String book : books) {
 			myBooks.add(new XMLSerializer().deserializeBook(book));
 		}
 		myWithBar = SharedPreferencesManager.getBoolean(myContext, PreferenceKey.WITH_BAR, myAppWidgetId);
 		myWithPercent = SharedPreferencesManager.getBoolean(myContext, PreferenceKey.WITH_PERCENT, myAppWidgetId);
 	}
 }
