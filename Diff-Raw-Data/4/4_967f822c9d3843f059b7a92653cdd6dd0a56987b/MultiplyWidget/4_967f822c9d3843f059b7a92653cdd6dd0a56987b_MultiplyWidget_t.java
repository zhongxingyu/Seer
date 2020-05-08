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
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.annotation.TargetApi;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.widget.RemoteViews;
 
 import org.geometerplus.fbreader.book.Book;
 import org.geometerplus.fbreader.book.XMLSerializer;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class MultiplyWidget extends Widget {
 	public static final int STACK_VIEW_SIZE = 4;
 	private final List<Book> myBooks;
 	
 	private MultiplyWidget(int id, boolean withBar, boolean withPercent, List<Book> books) {
 		super(id, withBar, withPercent, Widget.MULTIPLY);
 		myBooks = books;
 	}
 	
 	public static MultiplyWidget create(int id, boolean withBar, boolean withPercent, List<Book> books) {
 		if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
 			return null;
 		}
 		return new MultiplyWidget(id, withBar, withPercent, books);
 	}
 	
 	public static MultiplyWidget create(Context context, int id) {
 		if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
 			return null;
 		}
 		return readDataFromFile(context, id);
 	}
 
 	public static MultiplyWidget readDataFromFile(Context context, int id) {
 		if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
 			return null;
 		}
 		final String sharedPreferencesName = PreferenceKey.SHARED_PREFERENCES + id;
 		final SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
 		final Set<String> xmlBooks = sharedPreferences.getStringSet(PreferenceKey.BOOKS, null);
 		if (xmlBooks == null) {
 			return null;
 		}
 		List<Book> books = new ArrayList<Book>();
 		for (String xmlBook : xmlBooks) {
 			books.add(Book.deserializeBook(xmlBook));
 		}
 		final boolean withBar = sharedPreferences.getBoolean(PreferenceKey.WITH_BAR, false);
 		final boolean withPercent = sharedPreferences.getBoolean(PreferenceKey.WITH_PERCENT, false);
 		return create(id, withBar, withPercent, books);
 	}
 	
 	public List<Book> getBooks() {
 		return myBooks;
 	}
 
 	@Override
 	protected void writeSpecific(Editor editor) {
 		final Set<String> xmlBooks = new LinkedHashSet<String>();
 		for (Book book : myBooks) {
 			xmlBooks.add(new XMLSerializer().serialize(book));
 		}
 		editor.putStringSet(PreferenceKey.BOOKS, xmlBooks);
 	}
 	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
 	@Override
 	protected RemoteViews getRemoteViews(Context context) {
 		final RemoteViews rm = new WidgetRemoteViewsHolder().setRemoteViews(context, this);
		rm.setDisplayedChild(R.id.stack_widget_view, 0);
 		return rm;
 	}
 
 	public boolean updateBook(Book myBookFromEvent) {
 		if (myBooks.contains(myBookFromEvent)) {
 			myBooks.remove(myBookFromEvent);
 			myBooks.add(0, myBookFromEvent);
 		} else {
 			addNewBook(myBookFromEvent);
 		}
 		return true;
 	}
 
 	@Override
 	protected boolean isProgressNull() {
 		return myBooks.get(0).getProgress() == null;
 	}
 
 	public void addNewBook(Book myBookFromEvent) {
 		if (myBooks.size() > STACK_VIEW_SIZE) {
 			myBooks.remove(myBooks.size() - 1);
 		}
 		myBooks.add(0, myBookFromEvent);
 	}
 	
 	public boolean deleteOneOfBooks(Book book) {
 		if (!myBooks.contains(book)) {
 			return false;
 		}
 		myBooks.remove(book);
 		return true;
 	}
 }
