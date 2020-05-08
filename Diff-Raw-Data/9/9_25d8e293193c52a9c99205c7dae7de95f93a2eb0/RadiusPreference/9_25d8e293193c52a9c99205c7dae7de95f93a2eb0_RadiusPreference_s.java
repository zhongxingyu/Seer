 package com.thoughtworks.blipit.preferences;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.preference.EditTextPreference;
 import android.util.AttributeSet;
 
import static com.thoughtworks.blipit.utils.BlipItUtils.*;
 
 public class RadiusPreference extends EditTextPreference {
     public RadiusPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     // TODO: Currently does nothing. But this may not be the right thing to do here !
     @Override
     protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        // do nothing
     }
 
     @Override
     protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
         super.onPrepareDialogBuilder(builder);
         float radius = getRadius(getSharedPreferences(), RADIUS_PREF_KEY);
         getEditText().setText(String.valueOf(radius));
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         if (positiveResult) {
             String radiusStr = this.getEditText().getText().toString();
             float radius;
             try {
                 radius = Float.valueOf(radiusStr);
             } catch (NumberFormatException e) {
                 radius = 2f;
             }
             getSharedPreferences().edit().putFloat(RADIUS_PREF_KEY, radius).commit();
         }
     }
 }
