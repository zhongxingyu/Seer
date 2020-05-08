 package com.gmail.va034600.nreader.ui.activity;
 
 import javax.inject.Inject;
 
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

 import com.gmail.va034600.nreader.business.domain.service.RssService;
 import com.gmail.va034600.nreader.ui.NanairoApplication;
 
public abstract class BaseActivity extends ActionBarActivity {
 	@Inject
 	RssService rssService;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		((NanairoApplication) getApplication()).inject(this);
 	}
 }
