 package mobisocial.payments;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.X509EncodedKeySpec;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.DbObj;
 import mobisocial.socialkit.musubi.Musubi;
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class VerifyPaymentActivity extends Activity {
     public static final String TAG = "VerifyPaymentActivity";
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.accept_bill);
         
         if (getIntent() == null || getIntent().getData() == null) {
             finish();
             return;
         }
         
         // Clear any pending notifications
         NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancelAll();
         
         // Send the token if yes is clicked
         findViewById(R.id.yesbutton).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(VerifyPaymentActivity.this, PaymentsActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 finishActivity();
             }
         });
         findViewById(R.id.nobutton).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(VerifyPaymentActivity.this, PaymentsActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 finishActivity();
             }
         });
         
         Musubi musubi = Musubi.getInstance(this);
         DbObj obj = musubi.objForUri(getIntent().getData());
         DbFeed feed = obj.getContainingFeed();
         JSONObject json = obj.getJson();
         int myIndex = (feed.getMembers().get(0).getName()
                 .equals(feed.getLocalUser().getName())) ? 1 : 0;
         try {
            if (!parseAndVerify(json)) {
                 ((TextView)findViewById(R.id.verifyText))
                     .setText("Payment could not be verified!" +
                             "\nPayer: " + feed.getMembers().get(myIndex).getName() +
                             "\nAmount: $" + json.getString("amount") +
                             "\nTransaction ID: " + json.getString("tid"));
             } else {
                 ((TextView)findViewById(R.id.verifyText))
                     .setText("Success!" +
                              "\nPayer: " + feed.getMembers().get(myIndex).getName() +
                              "\nAmount: $" + json.getString("amount") +
                              "\nTransaction ID: " + json.getString("tid"));
             }
             ((Button)findViewById(R.id.yesbutton)).setText("OK");
             ((Button)findViewById(R.id.nobutton)).setVisibility(Button.INVISIBLE);
             return;
         } catch (JSONException e1) {
             finish();
             return;
         }
         
         //getBankNames(getIntent().getData());
     }
     
     private boolean parseAndVerify(JSONObject toVerify) {
         try {
             JSONObject token = toVerify.getJSONObject("token");
            JSONObject sig = toVerify.getJSONObject("sig");
            return verify(token.toString(), sig.toString());
         } catch (JSONException e) {
             Log.w(TAG, "JSON parse error", e);
             return false;
         } catch (Exception e) {
             Log.e(TAG, "could not run signature verification");
             return false;
         }
     }
     
     private boolean verify(String reported, String signed)
             throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
             InvalidKeyException, SignatureException {
         InputStream instream = new BufferedInputStream(getAssets().open("public_key.der"));
         byte[] encodedKey = new byte[instream.available()];
         instream.read(encodedKey);
         X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
         KeyFactory kf = KeyFactory.getInstance("RSA");
         PublicKey pkPublic = kf.generatePublic(publicKeySpec);
         Signature sig = Signature.getInstance("SHA256withRSA");
         sig.initVerify(pkPublic);
         sig.update(reported.getBytes());
         return sig.verify(Base64.decode(signed, Base64.DEFAULT));
     }
     
     private void finishActivity() {
         finish();
     }
 }
