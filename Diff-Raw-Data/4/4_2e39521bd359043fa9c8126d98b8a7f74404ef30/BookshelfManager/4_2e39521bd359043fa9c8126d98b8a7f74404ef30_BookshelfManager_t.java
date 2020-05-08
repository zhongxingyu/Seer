 package org.csie.mpp.buku.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.R;
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class BookshelfManager extends ViewManager {
 	public static interface ViewListener {
 		public void onListViewCreated(ListView view);
 	}
 	
 	private static class BookEntryAdapter extends ArrayAdapter<BookEntry> {
 		private LayoutInflater inflater;
 		private int resourceId;
 		private List<BookEntry> entries;
 		
 		public BookEntryAdapter(Activity activity, int resource, List<BookEntry> list) {
 			super(activity, resource, list);
 			
 			inflater = activity.getLayoutInflater();
 			resourceId = resource;
 			entries = list;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			BookEntry entry = entries.get(position);
 			View view = inflater.inflate(resourceId, parent, false);
 			// TODO: using ImageLoader instead
 			if(entry.cover!=null)
 				((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(entry.cover);
 			else 
 				((ImageView)view.findViewById(R.id.list_image)).setImageResource(R.drawable.book);
 			((TextView)view.findViewById(R.id.list_title)).setText(entry.title);
 			((TextView)view.findViewById(R.id.list_author)).setText(entry.author);
 			return view;
 		}
 	}
 	
 	private interface ViewManager {
 		public void initView(View view);
 		public int length();
 		public void set(BookEntry[] entries);
 		public BookEntry get(int position);
 		public void add(BookEntry entry);
 		public void remove(BookEntry entry);
 	}
 	
 	private class ListViewManager implements ViewManager {
 		private List<BookEntry> entries; 
 		private ListView booklist;
 		private BookEntryAdapter booklistAdapter;
 		
 		@Override
 		public void initView(View view) {
 			entries = new ArrayList<BookEntry>();
 			booklist = (ListView)view.findViewById(R.id.inner_list);
 			booklistAdapter = new BookEntryAdapter(activity, R.layout.list_item_book, entries);
 			
 			booklist.setAdapter(booklistAdapter);
 			
 			if(callback != null)
 				callback.onListViewCreated(booklist);
 		}
 		
 		@Override
 		public int length() {
 			return entries == null? -1 : entries.size();
 		}
 		
 		@Override
 		public void set(BookEntry[] es) {
 			entries.clear();
 			for(BookEntry entry: es)
 				_addBook_(entry);
 			booklistAdapter.notifyDataSetChanged();
 		}
 		
 		@Override
 		public BookEntry get(int position) {
 			return entries.get(position);
 		}
 
 		@Override
 		public void add(BookEntry entry) {
 			_addBook_(entry);
 			booklistAdapter.notifyDataSetChanged();
 		}
 
 		@Override
 		public void remove(BookEntry entry) {
 			_removeBook_(entry);
 			booklistAdapter.notifyDataSetChanged();
 		}
 		
 		private void _addBook_(BookEntry entry) {
 			entries.add(entry);
 		}
 		
 		private void _removeBook_(BookEntry entry) {
 			int position = entries.indexOf(entry);
 			entries.remove(position);
 		}
 	}
 	
 	private ListViewManager vm;
 	
 	public BookshelfManager(Activity activity, DBHelper helper) {
 		super(activity, helper);
 		
 		vm = new ListViewManager();
 	}
 	
 	private ViewListener callback;
 	
 	public BookshelfManager(Activity activity, DBHelper helper, ViewListener callback) {
 		this(activity, helper);
 		
 		this.callback = callback;
 	}
 	
 	public void add(String isbn) {
 		BookEntry entry = get(isbn);
 		add(entry);
 	}
 	
 	public void add(BookEntry entry) {
		if(vm.length() < 1)
 			createBookView();
 		vm.add(entry);
 	}
 	
 	public BookEntry get(String isbn) {
 		return BookEntry.get(rdb, isbn);
 	}
 	
 	public BookEntry get(int position) {
 		return vm.get(position);
 	}
 	
 	public void remove(String isbn) {
 		BookEntry entry = get(isbn);
 		remove(entry);
 	}
 	
 	public void remove(BookEntry entry) {
 		vm.remove(entry);
 		if(vm.length() < 1)
 			createNoBookView();
 	}
 	
 	private void updateBooklist() {
 		BookEntry[] entries = BookEntry.queryAll(rdb);
 		vm.set(entries);
 	}
 
 	@Override
 	protected int getFrameId() {
 		return R.id.bookshelf_frame;
 	}
 
 	@Override
 	protected void updateView() {
 		if(BookEntry.count(rdb) == 0)
 			createNoBookView();
 		else {
 			createBookView();
 			updateBooklist();
 		}
 	}
 	
 	private void createBookView() {
 		FrameLayout frame = getFrame();
 		
 		if(frame.getChildCount() > 0)
 			frame.removeAllViews();
 		
 		View view = activity.getLayoutInflater().inflate(R.layout.list, null);
 		frame.addView(view);
 		vm.initView(view);
 	}
 	
 	private void createNoBookView() {
 		FrameLayout frame = getFrame();
 		
 		if(frame.getChildCount() > 0)
 			frame.removeAllViews();
 		
 		View view = activity.getLayoutInflater().inflate(R.layout.none, null);
 		frame.addView(view);
 		TextView text = (TextView)view.findViewById(R.id.inner_text);
 		text.setText(R.string.add_book_to_start);
 	}
 }
