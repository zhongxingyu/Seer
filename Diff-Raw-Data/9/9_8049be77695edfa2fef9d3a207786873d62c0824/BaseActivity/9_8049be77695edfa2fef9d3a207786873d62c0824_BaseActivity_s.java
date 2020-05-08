 package com.gmail.va034600.nreader.ui.activity;
 
 import javax.inject.Inject;
 
 import com.gmail.va034600.nreader.business.domain.service.RssService;
 import com.gmail.va034600.nreader.ui.NanairoApplication;
 
import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity {
 	@Inject
 	RssService rssService;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		((NanairoApplication) getApplication()).inject(this);
 	}
 }
