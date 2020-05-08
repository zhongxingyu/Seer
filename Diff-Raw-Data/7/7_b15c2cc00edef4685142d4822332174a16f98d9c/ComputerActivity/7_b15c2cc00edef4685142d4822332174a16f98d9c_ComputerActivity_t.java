 package spang.mobile;
 
 import keyboard.KeyboardNetworkedActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 public class ComputerActivity extends NetworkedActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_computer);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_computer, menu);
         return true;
     }
 
 
 	@Override
 	protected void onNetworkServiceConnected() {				
 		//TODO remove testing code!.
 		SpangTouchView view = new SpangTouchView(this, this.getNetworkService());
 		setContentView(view);
 		view.setFocusableInTouchMode(true);
 		view.requestFocus();	
 	}
 
 	@Override
 	protected void onNetworkSerivceDissconnected() {
 		Toast.makeText(this,"Dissconnected!", Toast.LENGTH_SHORT).show();
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.Keyboard:
             	this.onShowKeyboard(item);
            	break;
             case R.id.shortcuts:
             	this.goToShortcuts(item);
            	break;
         }
        return super.onOptionsItemSelected(item);
     }
 
 	public void goToShortcuts(MenuItem item){
 		Intent intent = new Intent(this, ShortcutActivity.class);
 		startActivity(intent);
 	}
 	
 	public void onShowKeyboard(MenuItem item) {
 		Intent intent = new Intent(this, KeyboardNetworkedActivity.class);
 		this.startActivity(intent);
 	}
 
 	@Override
 	protected void onMessageRecived(Object message) {
 		//Don't rly care tbh :O
 	}
 }
