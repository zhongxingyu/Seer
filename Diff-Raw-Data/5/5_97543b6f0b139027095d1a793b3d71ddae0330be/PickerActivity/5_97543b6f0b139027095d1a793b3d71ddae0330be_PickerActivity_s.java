 package com.example.speechhelper.facebook;
 
 import com.example.speechhelper.R;
 import com.facebook.widget.FriendPickerFragment;
 import com.facebook.widget.PickerFragment;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 
 public class PickerActivity extends FragmentActivity {
 	public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");
 	private FriendPickerFragment friendPickerFragment;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.pickers);
 
 	    Bundle args = getIntent().getExtras();
 	    FragmentManager manager = getSupportFragmentManager();
 	    Fragment fragmentToShow = null;
 	    Uri intentUri = getIntent().getData();
 
 	    if (FRIEND_PICKER.equals(intentUri)) {
 	        if (savedInstanceState == null) {
 	            friendPickerFragment = new FriendPickerFragment(args);
 	        } else {
 	            friendPickerFragment = 
 	                (FriendPickerFragment) manager.findFragmentById(R.id.picker_fragment);
 	        }
 	        // Set the listener to handle errors
	        friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
 	            @Override
 	            public void onError(PickerFragment<?> fragment,
 	                                FacebookException error) {
 	                PickerActivity.this.onError(error);
 	            }
	        });
 	        // Set the listener to handle button clicks
 	        friendPickerFragment.setOnDoneButtonClickedListener(
 	                new PickerFragment.OnDoneButtonClickedListener() {
 	            @Override
 	            public void onDoneButtonClicked(PickerFragment<?> fragment) {
 	                finishActivity();
 	            }
 	        });
 	        fragmentToShow = friendPickerFragment;
 
 	    } else {
 	        // Nothing to do, finish
 	        setResult(RESULT_CANCELED);
 	        finish();
 	        return;
 	    }
 
 	    manager.beginTransaction()
 	           .replace(R.id.picker_fragment, fragmentToShow)
 	           .commit();
 	}
 
 	private void onError(Exception error) {
 	    onError(error.getLocalizedMessage(), false);
 	}
 
 	private void onError(String error, final boolean finishActivity) {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setTitle(R.string.error_dialog_title).
 	            setMessage(error).
 	            setPositiveButton(R.string.error_dialog_button_text, 
 	               new DialogInterface.OnClickListener() {
 	                @Override
 	                public void onClick(DialogInterface dialogInterface, int i) {
 	                    if (finishActivity) {
 	                        finishActivity();
 	                    }
 	                }
 	            });
 	    builder.show();
 	}
 
 	private void finishActivity() {
 	    setResult(RESULT_OK, null);
 	    finish();
 	}
 }
