 package org.startsmall.openalarm;
 
 import android.content.Context;
 import android.net.Uri;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.text.TextUtils;
 
 interface IRingtoneChangedListener {
     public void onRingtoneChanged(Uri uri);
 }
 
 class RingtonePreference extends android.preference.RingtonePreference {
     IRingtoneChangedListener mRingtoneChangedListener;
 
     public RingtonePreference(Context context) {
         super(context);
 
         setShowDefault(true);
         setShowSilent(true);
     }
 
     @SuppressWarnings("unused")
     public void setRingtoneChangedListener(IRingtoneChangedListener listener) {
         mRingtoneChangedListener = listener;
     }
 
     public Uri getRingtoneUri() {
         String uriString = getPersistedString("");
         return TextUtils.isEmpty(uriString) ? null : Uri.parse(uriString);
     }
 
     public void setRingtoneUri(Uri ringtoneUri) {
         super.onSaveRingtone(ringtoneUri);
         if (ringtoneUri != null) {
             Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
            setSummary(ringtone.getTitle(getContext()));
         }
     }
 
     @Override
     protected void onSaveRingtone(Uri ringtoneUri) {
         setRingtoneUri(ringtoneUri);
         if(mRingtoneChangedListener != null) {
             mRingtoneChangedListener.onRingtoneChanged(ringtoneUri);
         }
     }
 
     @Override
     protected Uri onRestoreRingtone() {
         return getRingtoneUri();
     }
 }
