 package com.mrpinghe.android.holonote.activities;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.Menu;
 
 import com.mrpinghe.android.holonote.fragments.EditChecklistFragment;
 import com.mrpinghe.android.holonote.fragments.EditTextFragment;
 import com.mrpinghe.android.holonote.helpers.Const;
 import com.mrpinghe.android.holonote.helpers.DatabaseAdapter;
 import com.mrpinghe.android.holonote.helpers.Util;
 
 public class EditNoteActivity extends Activity {
 
 	private int mNoteType = Const.INVALID_INT;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		Util.setPrefTheme(this);
 		super.onCreate(savedInstanceState);
 		
 		Bundle req = this.getIntent().getExtras();
 		long noteId = req.getLong(DatabaseAdapter.ID_COL, Const.INVALID_LONG);
 		mNoteType = req.getInt(DatabaseAdapter.TYPE_COL, Const.INVALID_INT);
 
 		if (savedInstanceState != null && mNoteType == Const.INVALID_INT) {
 			// create new note
 			Serializable savedType = savedInstanceState.getSerializable(DatabaseAdapter.TYPE_COL);
 			mNoteType = (savedType != null && savedType instanceof Integer) ? (Integer) savedType : Const.INVALID_INT;
 		}
 		
 		Fragment frag = null;
 		switch (mNoteType) {
 		case Const.TYPE_CHECKLIST:
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.put(DatabaseAdapter.ID_COL, noteId);
 			frag = EditChecklistFragment.newInstance(params);
 			break;
 		case Const.TYPE_TEXT:
 			params = new HashMap<String, Object>();
 			params.put(DatabaseAdapter.ID_COL, noteId);
 			frag = EditTextFragment.newInstance(params);
 			break;
 		default:
 			this.onBackPressed();
 			Util.alert(this, "Not supported");
 			return;
 		}
 		
 		this.getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.getActionBar().setDisplayHomeAsUpEnabled(true);
 		this.getActionBar().setDisplayShowTitleEnabled(false);
 		return true;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putSerializable(DatabaseAdapter.TYPE_COL, mNoteType);
 	}
 }
