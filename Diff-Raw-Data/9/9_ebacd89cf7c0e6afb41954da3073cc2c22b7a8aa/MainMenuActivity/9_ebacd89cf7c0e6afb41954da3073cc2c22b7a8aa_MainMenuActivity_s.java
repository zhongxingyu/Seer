 package com.jpizarro.th.activity;
 
 import com.jpizarro.th.R;
 import com.jpizarro.th.client.common.actions.CommonActions;
 import com.jpizarro.th.client.common.dialogs.CommonDialogs;
 import com.jpizarro.th.entity.User;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class MainMenuActivity extends Activity {
     private static final int CHANGE_PASSWORD_ID = Menu.FIRST;
     private static final int UPDATE_ID = Menu.FIRST + 1;
     private static final int VIEW_HISTORY_ID = Menu.FIRST + 2;
     private static final int FIND_GAMES_ID = Menu.FIRST + 3;
     private static final int LOGOUT_ID = Menu.FIRST + 4;
 	
 	private TextView usernameView;
 	
 	 private User user = new User();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_info_page);
 		
 		usernameView = (TextView)findViewById(R.id.username);
 	}
 	@Override
 	protected void onResume() {
 		showDialog(CommonDialogs.CONNECTING_TO_SERVER_DIALOG_ID);
 		super.onResume();
 		user = (User)getIntent().getExtras().getSerializable("user");
 		fillPersonalInfo();
 		
 		dismissDialog(CommonDialogs.CONNECTING_TO_SERVER_DIALOG_ID);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		// TODO Auto-generated method stub
 		return CommonDialogs.createDialog(id, this);
 	}
 	private void fillPersonalInfo() {
 		usernameView.setText(user.getUserName());
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, LOGOUT_ID,0, R.string.pi_mo_logout)
 //    	.setIcon(R.drawable.logout)
     	;
 		return true;
 	}
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
         case LOGOUT_ID:
         	doLogout();
         	break;
 //        case UPDATE_ID:
 //        	doUpdateInfo();
 //        	break;
 //        case CHANGE_PASSWORD_ID:
 //        	doChangePassword();
 //        	break;
 //        case VIEW_HISTORY_ID:
 //	    	doViewHistory();
 //	    	break;
 //	    case FIND_GAMES_ID:
 //	    	doFindGames();
 //	    	break;
 	    }
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	private void doLogout() {
 		CommonActions.launchLogoutThread(user.getUserName(), this);		
 	}
 
 }
