 package info.ohgita.bincalc_android;
 
 /**
  * Bin.Calc - MainActivity
  * @author Masanori Ohgita
  */
 
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	final static int MENU_ID_LOG = 100;
 	final static int MENU_ID_ABOUT = 200;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setTheme(R.style.Theme_Sherlock_Light);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		/* SubMenu button */
 		SubMenu sub_menu = menu.addSubMenu("Overflow Item");
 		sub_menu.getItem().setIcon(R.drawable.ic_action_overflow);
 		sub_menu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		
 		sub_menu.add("Log");
 		
 		sub_menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE , R.string.menu_about);
 		
 		/* All clear button*/
 		menu.add("AllClear")
 			.setIcon(R.drawable.button_allclear)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		boolean ret = true;
 		switch (item.getItemId()) {
 		default:
 			ret = super.onOptionsItemSelected(item);
 			break;
 		case MENU_ID_LOG:
 		    ret = true;
 		    break;
 		case MENU_ID_ABOUT:
 		    ret = false;
 		    Intent intent = new Intent(MainActivity.this,Activity_appInfo.class);
 		    intent.setAction(Intent.ACTION_VIEW);
 		    startActivity(intent);
 		    break;
 		}
 		return ret;
 	}
 }
