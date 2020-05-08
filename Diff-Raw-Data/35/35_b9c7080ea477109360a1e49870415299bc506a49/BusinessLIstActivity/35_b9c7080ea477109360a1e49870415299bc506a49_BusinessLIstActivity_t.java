 package com.mimo.app;
 
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 import com.mimo.app.component.COrderAdapter;
 import com.mimo.app.component.IComponentFactory;
 import com.mimo.app.interfaces.IMenuInstance;
 import com.mimo.app.model.pojo.Business;
 import com.mimo.app.model.pojo.Icons;
 import com.mimo.app.model.pojo.Items;
 import com.mimo.app.worker.BusinessWorker;
 import com.mimo.app.worker.IBusinessWorker;
 
 public class BusinessLIstActivity extends BaseListActivity implements
 		IMenuInstance, OnClickListener {
 	private IComponentFactory componentFactory;
 	private ProgressDialog progressDialog = null;
 	private ArrayList<Items> items = null;
 	private COrderAdapter adapter;
 	private IBusinessWorker businessWorker;
 	private Intent intent;
 	private ImageButton homeButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.layout_biz);
 
 		// initialize component ui
 		homeButton = (ImageButton) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 
 		items = new ArrayList<Items>();
 		this.componentFactory = getComponentFactory();
 		this.adapter = componentFactory.createOrderList(this, R.layout.row,
 				items);
 		this.setListAdapter(this.adapter);
 		adapter.runThread();
 		progressDialog = showDialogProgress();
 	}
 
 	public void onClick(View v) {
 		if (v == homeButton) {
			intent = new Intent();
			intent.setClass(this, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}
 
 		return;
 	}
 
 	// handle menu ui
 	public boolean createOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.biz_options_menu, menu);
 		return true;
 	}
 
 	public boolean createOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.reload:
 			BusinessWorker.setBusinessWorker();
 			adapter.clear();
 			break;
 
 		}
 		return true;
 	}
 
 	private Business[] readApiData() {
 		businessWorker = BusinessWorker.getInstance();
 		Business[] dataBusiness = businessWorker.createApiCall();
 		return dataBusiness;
 	}
  
 	@Override
 	protected void getData() {
 		// TODO Auto-generated method stub
 
 		try {
 			items = new ArrayList<Items>();
 
 			buildDataComponent(readApiData());
 
 			Log.i("ARRAY", "" + items.size());
 		} catch (Exception e) {
 			Log.e("BACKGROUND_PROC", e.getMessage());
 		}
 
 		adapter.setItems(items);
 		runOnUiThread(adapter.resultThread());
 
 		progressDialog.dismiss();
 	}
 
 	@Override
 	protected void buildDataComponent(Object[] data) {
 		Business[] dataBusiness = (Business[]) data;
 		StringBuffer sb;
 		Items o;
 		for (int j = 0; j < dataBusiness.length; j++) {
 			Business biz = dataBusiness[j];
 
 			sb = new StringBuffer();
 			o = new Items();
 			o.setItemId("" + j);
 			o.setItemName(biz.getBizname());
 
 			sb.append(biz.getDescription());
 			sb.append("\n" + biz.getEvent());
 			o.setItemStatus(sb.toString());
 			o.setData("lat:" + biz.getLat() + ",lng:" + biz.getLng());
 			o.setDrawableImage(Icons.getInstances().getIconFromBizLabel(
 					biz.getBizname()));
 			items.add(o);
 		}
 	}
 
 }
