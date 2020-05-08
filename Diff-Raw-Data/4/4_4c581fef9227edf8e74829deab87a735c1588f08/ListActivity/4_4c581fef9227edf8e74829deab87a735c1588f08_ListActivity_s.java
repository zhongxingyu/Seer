 package whyq.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import whyq.WhyqApplication;
 import whyq.WhyqMain;
 import whyq.adapter.WhyqAdapter;
 import whyq.adapter.WhyqAdapter.ViewHolder;
 import whyq.controller.WhyqListController;
 import whyq.interfaces.Login_delegate;
 import whyq.model.User;
 import whyq.model.Whyq;
 import whyq.utils.API;
 import whyq.utils.RSA;
 import whyq.utils.UrlImageViewHelper;
 import whyq.utils.WhyqUtils;
 import whyq.utils.XMLParser;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.whyq.R;
 
 public class ListActivity extends FragmentActivity implements Login_delegate, OnClickListener{
 
 	
 	public static final String DOWNLOAD_COMPLETED = "DOWNLOAD_COMPLETED";
 	public static final String COFFE = "";
 	public String url = "";
 	public Boolean header = true;
 
 	/**
 	 * MSA
 	 */
 	private ArrayList<Whyq> permListMain = new ArrayList<Whyq>();
 
 	public static int screenWidth;
 	public static int screenHeight;
 	public static boolean isLogin = false;
 	public static int loginType = 0;
 	public static boolean isRefesh = true;
 	public static boolean isCalendar = false;
 	int nextItem = -1;
 //	FragmentManager t = ggetSupportFragmentManager();
 	private ProgressDialog dialog;
 	
 	ListView whyqListView;
 //	Button btnRefesh;
 	ProgressBar progressBar;
 //	ImageView imageViewBeforRefesh;
 //	RelativeLayout headerLayout;
 	WhyqAdapter permListAdapter;
 	View headerView = null;
 	private boolean isFirst = true;
 	public static LoadPermList loadPermList;
 	public boolean isAddHeader = false;
 	
 	/*
 	 * Whyq elements
 	 */
 	
 	ImageButton lnCutlery;
 	ImageButton lnWine;
 	private ImageButton lnCoffe;
 	
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			if (intent.getAction().equals(DOWNLOAD_COMPLETED)) {
 				exeListActivity();
 			} 
 		}
 	};
 	
 	
 	private OnItemClickListener onStoreItemListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> adapter, View view,
 				int position, long id) {
 			try {
 				ViewHolder store = ((ViewHolder) view.getTag());
 				if(store !=null){
 					String storeId = store.id;
 					if(storeId !=null)
 						gotoStoreDetail(storeId);
 				}
 				
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 	};
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		createUI();
 		IntentFilter intentFilter = new IntentFilter(DOWNLOAD_COMPLETED);
 		registerReceiver(receiver, intentFilter);
 		WhyqUtils.clearViewHistory();
 		WhyqUtils utils= new WhyqUtils();
 		utils.writeLogFile(ListActivity.this.getIntent());
     	dialog = ProgressDialog.show(getParent(), "", "progressing...",
     			true);
     	showProgress();
    	Intent intent = new Intent(ListActivity.this, WhyqLogout.class);
    	startActivity(intent);
 	}
 	
 	protected void gotoStoreDetail(String storeId) {
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(ListActivity.this, StoreDetailActivity.class);
 		startActivity(intent);
 	}
 
 	public void createUI() {
 		setContentView(R.layout.list_screen);//
 		
 		whyqListView = (ListView) findViewById(R.id.lvWhyqList);
 		lnCutlery = (ImageButton) findViewById(R.id.lnCutleryTab);
 		lnWine = (ImageButton) findViewById(R.id.lnWineTab);
 		lnCoffe = (ImageButton) findViewById(R.id.lnCoffeTab);
 		loadPermList = new LoadPermList();
 		progressBar = new ProgressBar(ListActivity.this);
 		isAddHeader = true;
 	}
 	
 	@Override
 	protected void onDestroy(){
 		super.onDestroy();
 		isCalendar =false;
 		isRefesh = true;
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(isLogin && WhyqMain.getCurrentTab() == 3){
 			User user2 = WhyqUtils.isAuthenticated(getApplicationContext());
 			if(user2 != null){
 				String id = user2.getId();
 				if(id != null)
 					WhyqMain.gotoDiaryTab(id);
 			}
 			isLogin = false;
 		}else if(WhyqMain.getCurrentTab() == 0 && isRefesh){
 			// Get the screen's size.
 			exeListActivity();
 		}else if(WhyqMain.getCurrentTab() == 1 || WhyqMain.getCurrentTab() == 4){
 			if(isRefesh)
 				exeListActivity();
 		}else if(WhyqMain.getCurrentTab() == 3) { 
 			isCalendar = true;
 			exeListActivity();
 		}else if(!isRefesh){
 			isRefesh = true;
 		}
 	}
 	
 	protected void onPause () {
     	super.onPause();
     	isFirst = true;
     	nextItem = -1;
     	showProgress();
     	
     }
 
 
 
 	public void exeListActivity() {
 		// TODO Auto-generated method stub
 		if(isFirst){
 	    	clearData();
 	    	if(permListMain !=null)
 	    		permListMain.clear();
 	    	isFirst = false;
 		}
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		
 		
 		//Set to application
 		WhyqApplication state = (WhyqApplication) this.getApplication();
 		if (state != null) {
 			state.setDisplayMetrics(metrics);
 		}
 		
 		
 		screenHeight = metrics.heightPixels;
 		screenWidth = metrics.widthPixels;
 
 		User user = WhyqUtils.isAuthenticated(getApplicationContext());
 		Bundle extras = getIntent().getExtras();
 		if(extras != null && extras.containsKey("allcategory")){
 			this.url = API.getNewPerm;
 			this.header = false;
 		}else if( extras != null && extras.containsKey("permByDate")){
 			this.url = (String)extras.getString("permByDate");
 			this.header = false;
 		}else if (extras != null) {
 			this.url = (String) extras.get("categoryURL");
 			this.header = false;
 		} else if (user != null) {
 //			this.url = API.followingPerm + String.valueOf(user.getId());
 			this.url = API.popularBusinessListURL;
 			this.header = false;
 		}
 		clearData();
 //		btnRefesh.setVisibility(View.GONE);
 //		progressBar.setVisibility(View.VISIBLE);
 		showProgress();
 
 		loadPermList = new LoadPermList();
 		loadPermList.execute();
 	}
 	
 	private void timeoutDialog() {
 		// TODO Auto-generated method stub
 
 		hideProgress();
 	}
 	public void loadPreviousItems() {
 		if(nextItem > -1) {
 			nextItem = nextItem - 1;
 			clearData();
 
 //			btnRefesh.setVisibility(View.GONE);
 //			progressBar.setVisibility(View.VISIBLE);
 			showProgress();
 			
 			loadPermList = new LoadPermList();
 			loadPermList.execute();
 		}
 		
 	}
 
 	public void loadNextItems() {
 		if(permListAdapter != null) {
 			nextItem = permListAdapter.getNextItems();
 			//clearData();
 
 //			btnRefesh.setVisibility(View.GONE);
 //			progressBar.setVisibility(View.VISIBLE);
 			showProgress();
 	    	loadPermList = new LoadPermList();
 			loadPermList.execute();
 		}		
 	}
 	
 	public void cancelLoadPermList() {
 		if(loadPermList != null) {
 			loadPermList.cancel(true);
 		}
 	}
 	
 	private void loadPerms() {
 		User user = WhyqUtils.isAuthenticated(getApplicationContext());		
 		if(permListMain != null && !permListMain.isEmpty()){
 			//clearData();
 			//createUI();
 			if(this.permListAdapter == null) {
 				this.permListAdapter = new WhyqAdapter(ListActivityGroup.context,
 					getSupportFragmentManager(),R.layout.whyq_item_1, permListMain, this, screenWidth, screenHeight, header, user);
 			} else {
 				for(int i = 0; i < permListMain.size(); i++) {
 					Whyq whyq = permListMain.get(i);
 					if(!permListAdapter.isPermDuplicate(whyq)) {
 						permListAdapter.add(whyq);
 					}
 				}
 			}
 
 			
 			whyqListView.setAdapter(permListAdapter);
 
 			int selected = permListAdapter.getCount() - permListMain.size() - 2;
 			if(selected >= 0) {
 				whyqListView.setSelection(selected);
 			} else {
 				whyqListView.setSelection(0);
 			}
 
 		}else{
 			
 			
 		}
 
 	}
 	
 	public void clearData() {
 		if(permListAdapter != null && !permListAdapter.isEmpty()) {
 			//permListAdapter.clear();
 			UrlImageViewHelper.clearAllImageView();				
 		}
 		if(whyqListView != null && headerView != null) {
 			whyqListView.removeHeaderView(headerView);
 		}
 	}
 	
 	public void removeAllData() {
 		if(permListAdapter != null && !permListAdapter.isEmpty()) {
 			permListAdapter.clear();						
 		}
 		clearData();
 	}
 
 	
 	// AsyncTask task for upload file
 
 	public class LoadPermList extends AsyncTask<ArrayList<Whyq>, Void, ArrayList<Whyq>> {
 
 		@Override
 		protected ArrayList<Whyq> doInBackground(ArrayList<Whyq>... params) {
 			// TODO Auto-generated method stub
 			WhyqListController whyqListController = new WhyqListController();
 			ArrayList<Whyq> permList = null;
 			try {				
 				if (nextItem != -1) {
 					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					nameValuePairs.add(new BasicNameValuePair("nextItem", String.valueOf(nextItem)));
 					if(isCalendar){
 						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 //						isCalendar =false;
 					}
 
 					permList = whyqListController.getBusinessList(url, nameValuePairs);
 				} else {
 					if(isCalendar){
 						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 						nameValuePairs.add(new BasicNameValuePair("uid", WhyqMain.UID));
 						permList = whyqListController.getBusinessList(url,nameValuePairs);
 						
 					}else{
 						RSA rsa = new RSA();
 						String enToken = rsa.RSAEncrypt(XMLParser.getToken(WhyqApplication.Instance().getApplicationContext()));
 						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 						nameValuePairs.add(new BasicNameValuePair("token",enToken));
 						permList = whyqListController.getBusinessList(url, nameValuePairs);	
 					}
 					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				// TODO: handle exception
 //				if (dialog != null && dialog.isShowing()) {
 //					dialog.dismiss();
 //				}
 				/*if(progressBar.getVisibility()==View.VISIBLE){
 					progressBar.setVisibility(View.GONE);
 					btnRefesh.setVisibility(View.VISIBLE);
 				}*/
 				hideProgress();
 			}
 			/*if(permList != null){
 				for(int i = 0; i < permList.size(); i++) {
 					if(!permListMain.contains(permList.get(i))) {
 						permListMain.add(permList.get(i));
 					}
 				}
 				
 			}*/
 			permListMain = permList;
 			/**
 			 * MSA
 			 */
 			//permListMain.addAll(permList);
 			//permListMain = permList;
 						
 			return permListMain;
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... unsued) {
 
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList< Whyq> sResponse) {
 			/**
 			 * MSA
 			 */
 			loadPerms();
 			WhyqListController.isLoading = false;
 			
 			//permListMain.size();
 //			if (dialog != null && dialog.isShowing()) {
 //				dialog.dismiss();
 //			}
 			hideProgress();
 			if(permListAdapter != null) {
 				permListAdapter.notifyDataSetChanged();
 			}
 		}
 
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 	    if ((keyCode == KeyEvent.KEYCODE_BACK))
 	    {
 	        WhyqMain.back();
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void on_success() {
 		// TODO Auto-generated method stub
 		WhyqMain.refeshFollowerActivity();
 	}
 
 	@Override
 	public void on_error() {
 		// TODO Auto-generated method stub
 		//Logger.appendLog("test log", "loginerror");
 		Intent intent = new Intent(getApplicationContext(), JoinWhyqActivity.class);
 		this.startActivity(intent);		
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		int id = v.getId();
 		switch (id) {
 		case R.id.btnRefesh:
 			isFirst = true;
 	    	nextItem = -1;
 			exeListActivity();
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	public void resetTabBarFocus(int index){
 		
 		switch (index) {
 		case 1:
 			
 			lnWine.setBackgroundResource(R.drawable.bg_tab_normal);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_normal);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_active);
 //			lnCoffe.setBackgroundResource(R.drawable.icon_cat_coffee);
 //			lnCutlery.setBackgroundResource(R.drawable.bg_tab_active);
 			break;
 		case 2:
 			lnWine.setBackgroundResource(R.drawable.bg_tab_active);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_normal);
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_normal);
 //			lnCoffe.setBackgroundResource(R.drawable.icon_cat_coffee);
 //			lnCutlery.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			break;
 		case 3:
 			lnCoffe.setBackgroundResource(R.drawable.bg_tab_active);
 			lnWine.setBackgroundResource(R.drawable.bg_tab_normal);
 			lnCutlery.setBackgroundResource(R.drawable.bg_tab_normal);
 //			lnWine.setBackgroundResource(R.drawable.icon_cat_wine);
 //			lnCutlery.setBackgroundResource(R.drawable.icon_cat_cutlery);
 			break;
 
 		default:
 			break;
 		}
 		
 	}
 /*
  * Clicked Listener
  * 
  */
 	public void onCutleryTabCliked(View v){
 		resetTabBarFocus(1);
 	}
 	
 	public void onWineTabCliked(View v){
 		resetTabBarFocus(2);
 	}
 	
 	public void onCoffeTabClicked(View v){
 		resetTabBarFocus(3);
 	}
 	
 	
 	private void hideProgress() {
 		// TODO Auto-generated method stub
 //    	if(progressBar.getVisibility() == View.VISIBLE){
 //    		progressBar.setVisibility(View.GONE);
 //    	}
 		if (dialog != null && dialog.isShowing()) {
 			dialog.dismiss();
 		}
 	}
 	private void showProgress() {
 		// TODO Auto-generated method stub
 //    	if(progressBar.getVisibility() != View.VISIBLE){
 //    		progressBar.setVisibility(View.VISIBLE);
 //    	}
 		if (dialog != null && dialog.isShowing()) {
 			dialog.show();
 		}
 	}
 }
