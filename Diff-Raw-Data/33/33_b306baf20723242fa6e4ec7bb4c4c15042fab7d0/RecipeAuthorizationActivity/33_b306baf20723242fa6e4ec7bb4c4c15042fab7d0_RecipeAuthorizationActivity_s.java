 // 
 //  RecipeAuthorizationActivity.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-03-22.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveui;
 
 import edu.berkeley.androidwave.R;
 import edu.berkeley.androidwave.waveexception.WaveRecipeNotCachedException;
 import edu.berkeley.androidwave.waverecipe.WaveRecipe;
 import edu.berkeley.androidwave.waverecipe.WaveRecipeAuthorization;
 import edu.berkeley.androidwave.waveservice.RecipeRetrievalResponder;
 import edu.berkeley.androidwave.waveservice.WaveService;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.*;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.io.File;
 
 /**
  * RecipeAuthorizationActivity
  * 
  * UI displayed when a client application requests authorization for a given
  * recipe
  * 
  * TODO: Handle download progress, failure and success updates from the service
  * TODO: More clearly define the recipe portion of the UI
  * TODO: Use dialogs instead of Toast where necessary file:///usr/local/android-sdk-mac_86/docs/guide/topics/ui/dialogs.html
  * TODO: This activity should not run if the app is already authorized
  */
 public class RecipeAuthorizationActivity extends Activity implements RecipeRetrievalResponder {
     
     public static final String ACTION_DID_AUTHORIZE = "edu.berkeley.androidwave.intent.action.DID_AUTHORIZE";
     public static final String ACTION_DID_DENY = "edu.berkeley.androidwave.intent.action.DID_DENY";
     
     protected WaveRecipe theRecipe;
     protected WaveService mService;
     protected boolean mBound = false;
     
     protected String clientKey;
     protected String recipeClientName;
     protected Signature[] recipeClientSignatures;
     protected WaveRecipeAuthorization recipeAuthorization;
     
     // view refs
     TextView appName;
     TextView appSig;
     TextView recipeName;
     TextView recipeDescription;
     TextView recipeSig;
     Button authButton;
     Button denyButton;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitleColor(Color.RED);
         setContentView(R.layout.recipe_auth);
         
         // connect to views
         appName = (TextView) findViewById(R.id.app_name);
         appSig = (TextView) findViewById(R.id.app_sig);
         recipeName = (TextView) findViewById(R.id.recipe_name);
         recipeDescription = (TextView) findViewById(R.id.recipe_description);
         recipeSig = (TextView) findViewById(R.id.recipe_sig);
         authButton = (Button) findViewById(R.id.auth_button);
         denyButton = (Button) findViewById(R.id.deny_button);
         
         // more UI setup
         authButton.setEnabled(false);
         authButton.setOnClickListener(mAuthListener);
         denyButton.setEnabled(false);
         denyButton.setOnClickListener(mDenyListener);
         
         // connect to WaveService
         Intent sIntent = new Intent(Intent.ACTION_MAIN);
         sIntent.setClass(this, WaveService.class);
         try {
             bindService(sIntent, mConnection, Context.BIND_AUTO_CREATE);
             // we have to wait for onCreate to finish before the binding happens
        } catch (Exception e) {
            Toast.makeText(RecipeAuthorizationActivity.this, "Exception encountered, see log.", Toast.LENGTH_SHORT).show();
            System.out.println("Encountered excepting during bindService in RecipeAuthorizationActivity with "+sIntent);
            e.printStackTrace();
        }
        
        try {
             // get some name information about the requesting app
             PackageManager pm = getPackageManager();
             String callingActivityName = "From: Unknown application";
             try {
                 ActivityInfo aInfo = pm.getActivityInfo(getCallingActivity(), 0);   // may need flag PackageManager.GET_META_DATA
                 callingActivityName = "From: "+aInfo.loadLabel(pm);
             } catch (PackageManager.NameNotFoundException nnfe) {
                 Log.d(getClass().getSimpleName(), "NameNotFoundException while getting info for calling activity", nnfe);
             }
             appName.setText(callingActivityName);
            
             // store the client's name and signature info for the authorization
             recipeClientName = getCallingPackage();
         
             // get some signature information about the requesting app
             String callingActivitySigString = "Signed: Unknown";
             try {
                 PackageInfo pInfo = pm.getPackageInfo(getCallingPackage(), PackageManager.GET_SIGNATURES);
                 recipeClientSignatures = pInfo.signatures;
                 if (recipeClientSignatures != null && recipeClientSignatures.length > 0) {
                     String sigAscii = recipeClientSignatures[0].toCharsString();
                     callingActivitySigString = "Signed: "+sigAscii.substring(0,20)+"…";
                 }
             } catch (PackageManager.NameNotFoundException e) {
                 Log.d(getClass().getSimpleName(), "Exception while getting signature info for calling package", e);
             }
             appSig.setText(callingActivitySigString);
         } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "Exeption while getting info for calling activity", e);
         }
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     private void afterBind() {
         // check in with the service about the status of this recipe
        if (mBound) {
             Log.d(getClass().getSimpleName(), "RecipeAuthorizationActivity has bound to "+mService);
             
             Intent i = getIntent();
             String recipeId = i.getStringExtra(WaveService.RECIPE_ID_EXTRA);
             
             // verify that authenticity of the requesting app
             clientKey = i.getStringExtra(WaveService.CLIENT_KEY_EXTRA);
             if (mService.permitClientNameKeyPair(recipeClientName, clientKey)) {
                 theRecipe = null;
                 try {
                     File recipeCacheFile = mService.recipeCacheFileForId(recipeId);
                     theRecipe = WaveRecipe.createFromDisk(this, recipeCacheFile.getPath());
                     if (theRecipe == null) {
                         setResult(RESULT_CANCELED);
                         finish();
                     } else {
                         // Create an authorization object
                         recipeAuthorization = new WaveRecipeAuthorization(theRecipe);
                         recipeAuthorization.setRecipeClientName(recipeClientName);
                         recipeAuthorization.setRecipeClientSignatures(recipeClientSignatures);
                         // TODO: assign granularity data with UI
                         
                         // update the UI
                         recipeName.setText("Recipe: "+theRecipe.getName());
                         recipeDescription.setText(theRecipe.getDescription());
                         String recipeSigner = theRecipe.getCertificate().getSubjectDN().toString();
                         recipeSig.setText("Signed by: "+recipeSigner);
                         authButton.setEnabled(true);
                         denyButton.setEnabled(true);
                     }
                 } catch (WaveRecipeNotCachedException nce) {
                     Toast.makeText(RecipeAuthorizationActivity.this, "Attempting to retrieve this recipe…", Toast.LENGTH_SHORT).show();
                     mService.beginRetrieveRecipeForID(recipeId, this);
                 } catch (Exception e) {
                     Toast.makeText(RecipeAuthorizationActivity.this, "Exception encountered, see log.", Toast.LENGTH_SHORT).show();
                     e.printStackTrace();
                 }
             } else {
                 AlertDialog.Builder builder = new AlertDialog.Builder(RecipeAuthorizationActivity.this);
                 String[] pParts = recipeClientName.split(".");
                 String shortName = (pParts.length > 0 ? pParts[pParts.length-1] : recipeClientName);
                 builder.setMessage("Authentication failed for requesting package "+recipeClientName+".\n\n"+shortName+" has either been re-installed, or is attempting to use another app's key.")
                        .setCancelable(false)
                        .setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                setResult(RESULT_CANCELED);
                                RecipeAuthorizationActivity.this.finish();
                            }
                        });
                 AlertDialog alert = builder.create();
             }
         } else {
             setResult(RESULT_CANCELED);
             finish();
         }
     }
     
     private OnClickListener mAuthListener = new OnClickListener() {
         public void onClick(View v) {
             if (mService.saveAuthorization(clientKey, recipeAuthorization)) {
                 setResult(RESULT_OK, (new Intent()).setAction(ACTION_DID_AUTHORIZE));
                 finish();
             } else {
                 AlertDialog.Builder builder = new AlertDialog.Builder(RecipeAuthorizationActivity.this);
                 builder.setMessage("AndroidWave: internal error.")
                        .setCancelable(false)
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                 setResult(RESULT_CANCELED);
                                RecipeAuthorizationActivity.this.finish();
                            }
                        });
                 AlertDialog alert = builder.create();
             }
         }
     };
     
     private OnClickListener mDenyListener = new OnClickListener() {
         public void onClick(View v) {
             setResult(RESULT_OK, (new Intent()).setAction(ACTION_DID_DENY));
             finish();
         }
     };
     
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             Log.d("RecipeAuthorizationActivity.ServiceConnection", "onServiceConnected("+className+", "+service+")");
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             mService = ((WaveService.LocalBinder)service).getService();
             mBound = true;
             afterBind();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
             Log.d("RecipeAuthorizationActivity.ServiceConnection", "onServiceDisconnected("+className+")");
             mBound = false;
         }
     };
     
     /**
      * RecipeRetrievalResponder implementation
      */
      public void handleRetrievalFailed(String recipeId, String message) {
          Toast.makeText(RecipeAuthorizationActivity.this, "Recipe retrieval failed.", Toast.LENGTH_SHORT).show();
          setResult(RESULT_CANCELED);
          finish();
      }
 
      public void handleRetrievalFinished(String recipeId, File f) {
          
      }
 
 }
