 package nl.q42.hue2.views;
 
 import android.content.Context;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.widget.CompoundButton;
 import android.widget.Switch;
 
 /**
  * The normal switch fires callbacks for setChecked calls, but it also uses this built-in for user events.
  * There is no way to differentiate between those. It also calls setChecked in onRestoreInstanceState
  * that overwrites the restored checked state from the lights array. It aims to solve those problems
  * in a semi-non-hacky way.
  */
 public class FeedbackSwitch extends Switch {
	private boolean ignoreCallback = false;
 	
 	public FeedbackSwitch(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	// Don't fire callbacks for programmatic setting
 	public void setCheckedCode(boolean checked) {
 		this.ignoreCallback = true;
 		setChecked(checked);
 		this.ignoreCallback = false;
 	}
 	
 	// State is incorrectly restored by built-in code, so ignore
 	@Override
 	public void onRestoreInstanceState(Parcelable state) {
 		this.ignoreCallback = true;
 		boolean checked = isChecked();
 		super.onRestoreInstanceState(state);
 		setChecked(checked);
 		this.ignoreCallback = false;
 	}
 	
 	// Marked as deprecated here to prevent code from using it accidentally
 	@Override
 	@Deprecated
 	public void setChecked(boolean checked) {
 		super.setChecked(checked);
 	}
 	
 	@Override
 	public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
 		super.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (!ignoreCallback) {
 					listener.onCheckedChanged(buttonView, isChecked);
 				}
 			}
 		});
 	}
 }
