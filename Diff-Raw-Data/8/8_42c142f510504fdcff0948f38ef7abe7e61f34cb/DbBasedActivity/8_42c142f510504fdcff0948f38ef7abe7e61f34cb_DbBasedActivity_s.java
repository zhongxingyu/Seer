 package com.group5.android.fd.activity;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 import com.group5.android.fd.DbAdapter;
 import com.group5.android.fd.adapter.FdCursorAdapter;
 
 abstract public class DbBasedActivity extends ListActivity implements
 		OnItemClickListener, OnItemLongClickListener {
 	protected Cursor m_cursor;
 	protected FdCursorAdapter m_cursorAdapter;
 	protected DbAdapter m_dbAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		initLayout();
 		initListeners();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
		initDb();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
		closeDb();
 	}
 
 	protected void initDb() {
 		m_dbAdapter = new DbAdapter(this);
 		m_dbAdapter.open();
 
 		m_cursor = initCursor();
 		startManagingCursor(m_cursor);
 
 		m_cursorAdapter = initAdapter();
 		setListAdapter(m_cursorAdapter);
 	}
 
 	protected void closeDb() {
 		m_dbAdapter.close();
 	}
 
 	abstract protected Cursor initCursor();
 
 	abstract protected FdCursorAdapter initAdapter();
 
 	protected void initLayout() {
 		// TODO
 	}
 
 	protected void initListeners() {
 		ListView listView = getListView();
 
 		listView.setOnItemClickListener(this);
 		listView.setOnItemLongClickListener(this);
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
