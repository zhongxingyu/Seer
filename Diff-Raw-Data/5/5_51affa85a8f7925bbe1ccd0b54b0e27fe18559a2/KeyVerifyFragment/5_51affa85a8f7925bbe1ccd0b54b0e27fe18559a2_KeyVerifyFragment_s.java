 package com.lookout.keymaster.fragments;
 
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import com.lookout.keymaster.gpg.GPGCli;
 import com.lookout.keymaster.gpg.GPGFactory;
 import com.lookout.keymaster.R;
 import com.lookout.keymaster.gpg.GPGKey;
 
 public class KeyVerifyFragment extends Fragment {
     SimpleAdapter adapter;
 
     View rootView;
 
     public KeyVerifyFragment() {
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         rootView = inflater.inflate(R.layout.fragment_key_verify, container, false);
 
         new SetReceivingKeyTask(
                 GPGFactory.getReceivedKey(),
                 GPGFactory.getReceivedKeyId(),
                 GPGFactory.getPublicKey(),
                 GPGFactory.getPublicKeyId()
         ).execute();
 
 
         Button verifyButton = (Button)rootView.findViewById(R.id.verify_key_button);
         verifyButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Fragment fragment = new KeyTrustLevelFragment();
                 FragmentManager fragmentManager = getFragmentManager();
                 fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "key_trust_level").commit();
             }
         });
 
         getActivity().setTitle("Key Exchange");
         return rootView;
     }
 
     private void setTextForId(int id, String txt) {
         TextView tv = (TextView)rootView.findViewById(id);
         tv.setText(txt);
     }
 
 
     private class SetReceivingKeyTask extends AsyncTask<Void, Void, GPGKey[]> {
         String theirKey, theirkeyId, yourkeyId, yourKey;
 
         public SetReceivingKeyTask(String theirKey, String theirKeyId, String yourKey, String yourKeyId)  {
             this.theirKey = theirKey;
             this.theirkeyId = theirKeyId;
 
             this.yourKey = yourKey;
             this.yourkeyId = yourKeyId;
         }
 
         protected GPGKey[] doInBackground(Void... voids) {
             return new GPGKey[] {
                     GPGCli.getInstance().getPublicKey(theirkeyId),
                     GPGCli.getInstance().getPublicKey(yourkeyId)
             };
         }
 
         protected void onPostExecute(GPGKey[] result) {
            setTextForId(R.id.your_email, result[1].getPrimaryKeyId().getEmail());
             setTextForId(R.id.your_full_name, result[1].getPrimaryKeyId().getPersonalName());
             setTextForId(R.id.your_fingerprint, result[1].getFormattedFingerprint());
 
            setTextForId(R.id.their_email, result[0].getPrimaryKeyId().getEmail());
             setTextForId(R.id.their_full_name, result[0].getPrimaryKeyId().getPersonalName());
             setTextForId(R.id.their_fingerprint, result[0].getFormattedFingerprint());
         }
     }
 }
