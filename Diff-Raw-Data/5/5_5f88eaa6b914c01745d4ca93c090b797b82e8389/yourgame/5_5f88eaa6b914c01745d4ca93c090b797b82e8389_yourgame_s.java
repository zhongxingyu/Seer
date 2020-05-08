 //----------------------------------------------------------------------
 package com.yourgame.ouya;
 //----------------------------------------------------------------------
 // @@BEGIN_ACTIVITY_IMPORTS@@
 //----------------------------------------------------------------------
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.FileDescriptor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import tv.ouya.console.api.CancelIgnoringOuyaResponseListener;
 import tv.ouya.console.api.OuyaEncryptionHelper;
 import tv.ouya.console.api.OuyaFacade;
 import tv.ouya.console.api.OuyaResponseListener;
 import tv.ouya.console.api.Product;
 import tv.ouya.console.api.Purchasable;
 import tv.ouya.console.api.Receipt;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.KeyguardManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.LayoutInflater;
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.util.Log;
 import android.widget.VideoView;
 import android.widget.RelativeLayout;
 import android.net.Uri;
 import android.media.MediaPlayer;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.widget.MediaController;
 import android.os.Build;
 import android.os.Message;
 import android.os.Handler;
 import android.os.PowerManager;
 
 // @@END_ACTIVITY_IMPORTS@@
 //----------------------------------------------------------------------
 
 class Globals
 {
     //------------------------------------------------------------------
 
     // @@BEGIN_ACTIVITY_GLOBALS@@
     //------------------------------------------------------------------
 
     public static String sPackageName = "com.yourgame.ouya";
     public static String sApplicationName = "yourgame";
     public static boolean bUseGLES2 = true;
     public static boolean bForceDefaultOrientation = false;
     
     public static final boolean bInAppPurchasingEnabled = false;
     public static final String DEVELOPER_ID = "GET FROM OUYA PORTAL";
     
     //------------------------------------------------------------------
 
     // @@END_ACTIVITY_GLOBALS@@
     //------------------------------------------------------------------
 
 }
 //----------------------------------------------------------------------
 
 public class yourgame extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener
 {
     private static final String TAG = "yourgame";
     
     private static final List<Purchasable> PRODUCT_ID_LIST = Arrays.asList(new Purchasable("YOUR_PRODUCT_LIST"));
     private static ArrayList<Product> mProducts;
     
     //------------------------------------------------------------------
 	// @@BEGIN_ACTIVITY_MESSAGES_LIST@@
     //------------------------------------------------------------------
 	public static final int MSG_START_ENGINE 		    = 0 ;
 	public static final int MSG_RESUME_ENGINE 		    = 1 ;
 	public static final int MSG_PAUSE_ENGINE 		    = 2 ;
 	public static final int MSG_HIDE_SPLASH 		    = 3 ;
 	public static final int MSG_PLAY_OVERLAY_MOVIE 	    = 4 ;
 	public static final int MSG_STOP_OVERLAY_MOVIE 	    = 5 ;
     //------------------------------------------------------------------
 	// @@END_ACTIVITY_MESSAGES_LIST@@
     //------------------------------------------------------------------
 	
     //------------------------------------------------------------------
     @Override
     protected void onCreate ( Bundle savedInstanceState )
     {
         // Enable OUYA IAP if desired
         if(Globals.bInAppPurchasingEnabled)
         {
             OuyaFacade.getInstance().init(this, Globals.DEVELOPER_ID);
 //            requestOUYAProductList();
 //            requestOUYAReceipts();
         }
         
         // Call parent constructor
         //
         super.onCreate  ( savedInstanceState ) ;
 
 //        requestOuyaGamerID();
         
         // Get singleton
         //
         oThis = this ;
 
         // Print some infos about the device :
         //
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Create activity " + Globals.sApplicationName ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Device infos :" ) ;
         Log.d ( Globals.sApplicationName, "    BOARD:         " + Build.BOARD ) ;
         Log.d ( Globals.sApplicationName, "    BRAND:         " + Build.BRAND ) ;
         Log.d ( Globals.sApplicationName, "    CPU_ABI:       " + Build.CPU_ABI ) ;
         Log.d ( Globals.sApplicationName, "    DEVICE:        " + Build.DEVICE ) ;
         Log.d ( Globals.sApplicationName, "    DISPLAY:       " + Build.DISPLAY ) ;
         Log.d ( Globals.sApplicationName, "    MANUFACTURER:  " + Build.MANUFACTURER ) ;
         Log.d ( Globals.sApplicationName, "    MODEL:         " + Build.MODEL ) ;
         Log.d ( Globals.sApplicationName, "    PRODUCT:       " + Build.PRODUCT ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
 
         // Get APK file path
         //
         PackageManager  oPackageManager = getPackageManager ( ) ;
         try
         {
             ApplicationInfo oApplicationInfo = oPackageManager.getApplicationInfo ( Globals.sPackageName, 0 );
             mAPKFilePath  = oApplicationInfo . sourceDir ;
         }
         catch ( NameNotFoundException e ) { e.printStackTrace ( ) ; }
 
 		// Create the main view group and inflate startup screen (but do not add it right now, to avoid a "black flash")
 		//
 		//oSplashView 		= View.inflate ( this, R.layout.main, null ) ;
 		oViewGroup 			= new RelativeLayout ( this ) ;
         setContentView  	( oViewGroup ) ;
 
         // Fix from: http://www.stonetrip.com/developer/forum/viewtopic.php?f=75&t=26449&p=49411#p49411
         // The reason that the splashview doesn't have the correct dimensions is because the inflation doesn't have any
         // parent views to know what fill_parent means.  This uses the RelativeLayout created above as the parent
         // for inflation without adding the view to the parent
         //
 //        LayoutInflater inflater = (LayoutInflater)this.getSystemService ( Context.LAYOUT_INFLATER_SERVICE ) ;
 //        oSplashView             = inflater.inflate ( R.layout.main, oViewGroup, false ) ;        
 
     	//--------------------------------------------------------------
 		// @@ON_ACTIVITY_CREATED@@
     	//--------------------------------------------------------------
 		
         // Asynchronously initialize engine and other stuff 
         //
         createAsync ( ) ;     
 
 		// Register lock-screen intent handler
 		//
 //		registerLockScreenHandlers ( ) ;
     }
 
     //------------------------------------------------------------------
     protected void createAsync ( )
     {
         Thread t = new Thread ( ) 
         {
 		    public void run ( ) 
 		    {
 	            // Create useful directories, extract packs and create 3DView
                 //
                 if ( ! createCacheDirectory 	( ) ||
                      ! createHomeDirectory  	( ) ||
                      ! extractMainPack         	( ) ||
                      ! extractAdditionalFiles   ( ) )
                 {
 			        try { runOnUiThread ( new Runnable ( ) { public void run ( ) { onStorageError ( ) ; } } ) ; } 
 			        catch ( Exception e ) { }
                 }
                 else
                 {
 			        try { runOnUiThread ( new Runnable ( ) { public void run ( ) { onStartEngine ( ) ; } } ) ; } 
 			        catch ( Exception e ) { }
                 }		        		        
             }
 	    } ;
 	    t.start ( ) ;
     }
 
     //------------------------------------------------------------------
     protected void onStartEngine ( )
     {
 //        oPowerManager       = (PowerManager)    getSystemService ( Context.POWER_SERVICE ) ;
 
         // Create the 3D view
         //
         o3DView             = new S3DSurfaceView ( (Context)this, mCacheDirPath, mHomeDirPath, mPackDirPath, mPackFileDescriptor, mPackFileOffset, mPackFileLength, Globals.bUseGLES2, Globals.bForceDefaultOrientation ) ;
 
         if ( o3DView != null )
         {
 			//o3DView.setZOrderOnTop ( true ) ; // Uncomment to make transparent background to work
 			oViewGroup.addView    ( o3DView ) ;
 
             // Add the splash view on top of the 3D view
             //
 //            oViewGroup.removeView ( oSplashView ) ;
 //    	    oViewGroup.addView    ( oSplashView ) ;
 			
             // Enable wake lock
             //
 //            onEnableWakeLock ( true ) ;
 
     		// Inform the system we want the volume buttons to control the multimedia stream
     		//
     		setVolumeControlStream ( AudioManager.STREAM_MUSIC ) ;
 
             // Send a delayed event to actually start engine and show the 3D View
             //
             Message msg     = new Message ( )  ;
             msg.what        = MSG_START_ENGINE ;
             msg.obj         = this ;
     		oUIHandler  	.sendMessage ( msg ) ;
 		}
     }
 
     //------------------------------------------------------------------
     protected void onStorageError ( )
     {
         String    sLocale = Locale.getDefault  ( ).getLanguage ( ) ;
         if      ( sLocale.contentEquals ( "fr" ) ) showError   ( "L'espace de stockage disponible est insuffisant pour lancer cette application. Veuillez en liberer et relancer l'application." ) ; // OK
         else if ( sLocale.contentEquals ( "it" ) ) showError   ( "Spazio libero in memoria insufficiente per lanciare l'applicazione. Liberare pi\371 spazio e ripetere l'operazione." ) ; // OK
         else if ( sLocale.contentEquals ( "es" ) ) showError   ( "Esta aplicaci\363n no puede comenzar debido al espacio de almacenamiento libre escaso. Libere por favor para arriba un cierto espacio y vuelva a efectuar la aplicaci\363n." ) ;
         else if ( sLocale.contentEquals ( "de" ) ) showError   ( "Diese Anwendung kann auf Grund von unzureichend freiem Speicherplatz nicht starten. Geben Sie bitte etwas Speicherplatz frei und starten Sie die Anwendung erneut." ) ;
         else                                       showError   ( "This application cannot start due to insufficient free storage space. Please free up some space and rerun the application." ) ; // OK
     }
     
     //------------------------------------------------------------------
     public Handler oUIHandler = new Handler ( ) 
     {
         @Override
         public void handleMessage ( Message msg ) 
         {
             switch ( msg.what ) 
             {
     		//----------------------------------------------------------
 			// @@BEGIN_ACTIVITY_MESSAGES_HANDLING@@
     		//----------------------------------------------------------
             case MSG_START_ENGINE :
                 {
                     if ( o3DView != null )
                     {
                         // At this point, we can actually initialize the engine
                         //
                         o3DView.requestFocus();
                         o3DView.allowInit ( ) ;
 
     					//----------------------------------------------
 						// @@ON_ACTIVITY_ENGINE_STARTED@@
     					//----------------------------------------------
                     }
                 }
                 break ;
                 
             case MSG_RESUME_ENGINE :
                 {
                     // Handle the case when the user locks/unlocks screen rapidly 
                     // Not clean, but no choice as events are not sent in the right order, ie. onResume is sent *before* onScreenLocked... just great.
                     //
                     if ( o3DView != null )
                     {
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                         Log.d ( Globals.sApplicationName, "Resume activity " + Globals.sApplicationName ) ;
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
 
                         // Resume view
                         //
                         o3DView.onResume ( ) ;
 
 //                        if ( bWakeLockWasEnabledBeforePause              ) onEnableWakeLock             ( true ) ;
                     }
                 }
                 break ;
 
             case MSG_PAUSE_ENGINE :
                 {
 			        if ( o3DView != null )
 			        {
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                         Log.d ( Globals.sApplicationName, "Pause activity " + Globals.sApplicationName ) ;
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
 			            
 				        onStopOverlayMovie           ( ) ;
 				
 						// Pause view
 						//
 			            o3DView.onPause ( ) ;
 			        }
 				}
 				break ;
                 
             case MSG_HIDE_SPLASH :
                 {
 					if ( o3DView != null )
                 	{
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                         Log.d ( Globals.sApplicationName, "Hide splash view" ) ;
                         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                 	    
                         // Remove splash view
                         //
 //						oViewGroup.removeView ( oSplashView ) ;
 					
 						// Force focus to 3D view
 						//
 						o3DView.requestFocus ( ) ;
 					}
                 }
                 break ;
 
 			case MSG_PLAY_OVERLAY_MOVIE :
 				{
 					onPlayOverlayMovie ( (String)msg.obj ) ;
 				}
 				break ;
 
 			case MSG_STOP_OVERLAY_MOVIE :
 				{
 					onStopOverlayMovie ( ) ;
 				}
 				break ;
 
     		//----------------------------------------------------------
 			// @@END_ACTIVITY_MESSAGES_HANDLING@@
     		//----------------------------------------------------------
             }
             super.handleMessage ( msg ) ;
         }
     } ;
 
 	
     //------------------------------------------------------------------
 	// @@BEGIN_ACTIVITY_METHODS@@	
     //------------------------------------------------------------------
     @Override
     protected void onStart ( )
     {
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Start activity " + Globals.sApplicationName ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         super.onStart ( ) ;
     }
         
     //------------------------------------------------------------------
     @Override
     protected void onRestart ( )
     {
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Restart activity " + Globals.sApplicationName ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         super.onRestart ( ) ;
     }
                 
     //------------------------------------------------------------------
     @Override
     protected void onResume ( )
     {
         super.onResume ( ) ;
 
         Message msg     = new Message ( )  ;
         msg.what        = MSG_RESUME_ENGINE ;
         msg.obj         = this ;
         oUIHandler      .sendMessage ( msg ) ;
         
         // If screen is locked, just wait for unlock
         //
 //        if ( bScreenLocked )
 //        {
 //            bWantToResume = true ;
 //        }
 //        else
 //        {
 //            onResumeActually ( ) ;
 //		}
     }
     
     //------------------------------------------------------------------
     @Override
     protected void onPause ( ) 
     {
         super.onPause ( ) ;
 
         // Send a delayed event to actually pause engine and hide the 3D View
         //
         Message msg     = new Message ( )  ;
         msg.what        = MSG_PAUSE_ENGINE ;
         msg.obj         = this ;
         //oUIHandler  	.sendMessageDelayed ( msg, 500 ) ;
 		oUIHandler  	.sendMessage ( msg ) ;		
     }
     
     //------------------------------------------------------------------
     protected void onStop ( ) 
     {
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Stop activity " + Globals.sApplicationName ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         super.onStop ( ) ;
     }
     
     //------------------------------------------------------------------
     protected void onDestroy ( ) 
     {
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         Log.d ( Globals.sApplicationName, "Destroy activity " + Globals.sApplicationName ) ;
         Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
         super.onDestroy ( ) ;
 
         if(Globals.bInAppPurchasingEnabled)
         {
             OuyaFacade.getInstance().shutdown();
         }
         
     	//--------------------------------------------------------------
 		// @@ON_ACTIVITY_DESTROYED@@
     	//--------------------------------------------------------------
 		
 		// Unregister lock-screen intent handler
 		//
 
         // Destroy 3D view
         //
         if ( o3DView != null )
         {
         	o3DView.onTerminate ( ) ;
		}
     }
     
     public native void receiveOuyaProductList(int count);
     public static void requestOuyaProductList()
     {
         Log.d(TAG, "Requesting OUYA product list");
         OuyaFacade.getInstance().requestProductList(PRODUCT_ID_LIST, new CancelIgnoringOuyaResponseListener<ArrayList<Product>>() {
             
             @Override
             public void onSuccess(ArrayList<Product> products) {
                 mProducts = products;
                 Log.i(TAG, "Got products!");
                 for(Product p : products) {
                     Log.i(TAG, p.getName() + " costs " + p.getPriceInCents());
                 }
                 oThis.receiveOuyaProductList(products.size());
             }
             
             @Override
             public void onFailure(int errorCode, String message, Bundle arg2) {
                 Log.e(TAG, "Failed to get products");
                 oThis.receiveOuyaProductList(0);
             }
         });
     }
     
     public native void receiveOuyaPurchase(boolean success);
     public static void requestOuyaPurchase(int index)
     {
         Log.d(TAG, "Requesting OUYA Purchase");
         Purchasable productToBuy = PRODUCT_ID_LIST.get(index);
         OuyaFacade.getInstance().requestPurchase(productToBuy, new CancelIgnoringOuyaResponseListener<Product>() {
             @Override
             public void onSuccess(Product product) {
                 Log.d(TAG, "You purchased: " + product.getName());
                 oThis.receiveOuyaPurchase(true);
             }
             
             public void onFailure(int errorCode, String errorMessage, Bundle errorBundle) {
                 Log.e(TAG, "Failed to purchase item");
                 oThis.receiveOuyaPurchase(false);
             };
         });
     }
     
     public native void receiveOuyaReceipts();
     public static void requestOuyaReceipts()
     {
         Log.d(TAG, "Requesting OUYA receipts");
         OuyaFacade.getInstance().requestReceipts(new CancelIgnoringOuyaResponseListener<String>() 
         {
             @Override
             public void onSuccess(String receiptResponse) {
                 OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
                 List<Receipt> receipts = null;
                 try {
                     receipts = helper.decryptReceiptResponse(receiptResponse);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
                 for (Receipt r : receipts) {
                     Log.d(TAG, "You have previously purchased: " + r.getIdentifier());
                 }
             }
             
             @Override
             public void onFailure(int arg0, String arg1, Bundle arg2) {
                 Log.e(TAG, "Failed to get receipts");
             }
         });
     }
     
     public native void receiveOuyaGamerID(String uuid);
     public static void requestOuyaGamerID(){
         Log.d(TAG, "Requesting OUYA gamer ID");
         OuyaFacade.getInstance().requestGamerUuid(new CancelIgnoringOuyaResponseListener<String>() {
             @Override
             public void onSuccess(String result) {
                 Log.i(TAG, "Player UUID is: " + result);
                 oThis.receiveOuyaGamerID(result);
             }
             
             @Override
             public void onFailure(int arg0, String arg1, Bundle arg2) {
                 Log.e(TAG, "Failed to get player UUID");
                 oThis.receiveOuyaGamerID(null);
             }
         });
     }
     
     //------------------------------------------------------------------
     // OpenURL callback.
     //
 	public static void onOpenURL ( String sURL, String sTarget )
 	{
 	    if ( oThis != null )
 	    {
             Intent i            = new Intent ( Intent.ACTION_VIEW ) ;
 		    i.setData		    ( Uri.parse  ( sURL ) ) ;
 		    oThis.startActivity ( i ) ;
 	    }
 	}
 
     //------------------------------------------------------------------
     // Sound functions.
     //
     public static boolean onInitSound ( )
     {
         if ( oSoundPool == null )
         {
             oSoundPool = new SoundPool ( 15, AudioManager.STREAM_MUSIC, 0 ) ;
         }
         return ( oSoundPool != null ) ;
     }
     
     //------------------------------------------------------------------
     public static void onShutdownSound ( )
     {
         if ( oSoundPool != null )
         {
             oSoundPool.release ( ) ;
             oSoundPool = null ;
         }
     }
     
     //------------------------------------------------------------------
 //    public static void onSuspendSound ( boolean bSuspend )
 //    {
 //		/* Only available starting from 2.2... so let the engine do it
 //        if ( oSoundPool != null )
 //        {
 //			if ( bSuspend ) oSoundPool.autoPause  ( ) ;
 //			else 			oSoundPool.autoResume ( ) ;
 //		}*/
 //    }
     
     //------------------------------------------------------------------
     public static int onLoadSound ( String sURI )
     {
 		Log.d ( Globals.sApplicationName, "### onLoadSound: " + sURI ) ;
         return oSoundPool.load ( sURI, 1 ) ;
         /*
         try
         {
             FileInputStream fis = new FileInputStream ( sURI ) ;
             return oSoundPool.load ( fis.getFD ( ), 0, fis.available ( ), 1 ) ;
         }
         catch ( IOException e ) { e.printStackTrace ( ) ; }
         return 0 ;
         */
     }
     
     //------------------------------------------------------------------
     public static void onUnloadSound ( int iSound )
     {
         oSoundPool.unload ( iSound ) ;
     }
     
     //------------------------------------------------------------------
     public static int onPlaySound ( int iSound, float fVolume, boolean bLoop, float fPriority )
     {
 		//Log.d ( Globals.sApplicationName, "### onPlaySound: " + String.format ( "%d, %f, %s, %f", iSound, fVolume, bLoop ? "true" : "false", fPriority ) ) ;
 		int iStream = oSoundPool.play ( iSound, fVolume, fVolume, (int)(fPriority * 255.0f), bLoop ? -1 : 0, 1.0f )  ;		
 		//Log.d ( Globals.sApplicationName, "### onPlaySound: " + String.format ( "%d", iStream ) ) ;		
 		return ( iStream > 0 ) ? iStream : -1 ;
     }
 
     //------------------------------------------------------------------
     public static void onPauseSound ( int iStream )
     {
 		if ( iStream > 0 )
 		{
         	//Log.d ( Globals.sApplicationName, "### onPauseSound: " + String.format ( "%d", iStream ) ) ;
         	oSoundPool.pause ( iStream ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onResumeSound ( int iStream )
     {
 		if ( iStream > 0 )
 		{
         	oSoundPool.resume ( iStream ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onStopSound ( int iStream )
     {
 		if ( iStream > 0 )
 		{
 			//Log.d ( Globals.sApplicationName, "### onStopSound: " + String.format ( "%d", iStream ) ) ;
         	oSoundPool.setVolume ( iStream, 0.0f, 0.0f ) ;
         	oSoundPool.setLoop   ( iStream, 0 ) ;
         	oSoundPool.stop	     ( iStream ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onSetSoundPitch ( int iStream, float fPitch )
     {
 		if ( iStream > 0 )
 		{
          	oSoundPool.setRate ( iStream, fPitch ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onSetSoundLooping ( int iStream, boolean bLoop )
     {
 		if ( iStream > 0 )
 		{
         	oSoundPool.setLoop ( iStream, bLoop ? -1 : 0 ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onSetSoundVolume ( int iStream, float fVolume )
     {
 		if ( iStream > 0 )
 		{
         	oSoundPool.setVolume ( iStream, fVolume, fVolume ) ;
 		}
     }
     
     //------------------------------------------------------------------
     // Music functions
     //
     public static int onLoadMusic ( String sURI )
     {
 		//Log.d ( Globals.sApplicationName, "### onLoadMusic: " + sURI ) ;
 		
 		for ( int i = 1 ; i < 64 ; i++ )
 		{
 			if ( aMusicsList[i] == null )
 			{
 				aMusicsList[i] = sURI ;
 				return i ;
 			}
 		}
 		return 0 ; // Means "failed"
     }
     
     //------------------------------------------------------------------
     public static void onUnloadMusic ( int iMusic )
     {
 		//Log.d ( Globals.sApplicationName, "### onUnloadMusic: " + String.format ( "%d", iMusic ) ) ;
 		
 		if ( iMusic < 64 )
 		{
 			aMusicsList[ iMusic ] = null ;
 		}
     }
 
     //------------------------------------------------------------------
     public static int onPlayMusic ( int iMusic, float fVolume, boolean bLoop, float fPriority )
     {
 		//Log.d ( Globals.sApplicationName, "### onPlayMusic: " + String.format ( "%d, %f, %s, %f", iMusic, fVolume, bLoop ? "true" : "false", fPriority ) ) ;
 
 		if ( ( iMusic < 64 ) && ( aMusicsList[ iMusic ] != null ) )
 		{
 			if ( oMediaPlayer != null )
 			{
 				oMediaPlayer.stop ( ) ;
 				try
 				{
 					oMediaPlayer.setDataSource 	( aMusicsList[ iMusic ] ) ;
 				}
 				catch ( Exception e ) { e.printStackTrace ( ) ; return -1 ; }
 			}
 			else
 			{
 				oMediaPlayer = MediaPlayer.create ( oThis, Uri.parse ( aMusicsList[ iMusic ] ) ) ;
 			}
 
 			if ( oMediaPlayer != null )
 			{
 				oMediaPlayer.setAudioStreamType	( AudioManager.STREAM_MUSIC ) ;
 				oMediaPlayer.setLooping			( bLoop ) ;
 				oMediaPlayer.setVolume 			( fVolume, fVolume ) ;
 				oMediaPlayer.start	 			( ) ;
 				return 0 ; // Stream 0 is reserved for music
 			}
 		}
         return -1 ;
     }
 
     //------------------------------------------------------------------
     public static void onPauseMusic ( int iStream )
     {
 		Log.d ( Globals.sApplicationName, "### onPauseMusic: " + String.format ( "%d", iStream ) ) ;        
 
 		if ( oMediaPlayer != null )
 		{
 			oMediaPlayer.pause ( ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onResumeMusic ( int iStream )
     {
 		//Log.d ( Globals.sApplicationName, "### onResumeMusic: " + String.format ( "%d", iStream ) ) ;                
 
 		if ( oMediaPlayer != null )
 		{
 			oMediaPlayer.start ( ) ;
 		}
     }
     
     //------------------------------------------------------------------
     public static void onStopMusic ( int iStream )
     {
 		//Log.d ( Globals.sApplicationName, "### onStopMusic: " + String.format ( "%d", iStream ) ) ;              
 		
 		if ( oMediaPlayer != null )
 		{
 			oMediaPlayer.stop    ( ) ;
 			oMediaPlayer.release ( ) ;
 			oMediaPlayer	  = null ;
 		}          
     }
     
     //------------------------------------------------------------------
     public static void onSetMusicVolume ( int iStream, float fVolume )
     {
 		//Log.d ( Globals.sApplicationName, "### onSetMusicVolume: " + String.format ( "%d, %f", iStream, fVolume ) ) ;        
 		
 		if ( oMediaPlayer != null )
 		{
 			oMediaPlayer.setVolume 	( fVolume, fVolume ) ;
 		}
     }
 
     //------------------------------------------------------------------
     // Movie playback related methods
     //
     private static boolean onPlayOverlayMovie ( String sURI )
     {
         Log.d ( Globals.sApplicationName, "#### onPlayOverlayMovie: " + sURI ) ;
 
 		try 
 		{
 	        if ( oVideoView == null )        
 	        {
 	            oVideoView = new VideoView ( oThis ) ;    
                 
 	            if ( oVideoView != null )
 	            {
 					oVideoView.setOnPreparedListener	( oThis ) ;
 					oVideoView.setOnErrorListener		( oThis ) ;
 	                oVideoView.setOnCompletionListener 	( oThis ) ;
 	
 					RelativeLayout.LayoutParams oVideoViewLayoutParams = new RelativeLayout.LayoutParams ( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT ) ;
 					oVideoViewLayoutParams.addRule 	( RelativeLayout.CENTER_IN_PARENT ) ;
 					oViewGroup.addView				( oVideoView, oVideoViewLayoutParams ) ;
 					//o3DView.setVisibility 			( View.INVISIBLE ) ; // Kills the rendering context, play with ZOrder instead
 		            oVideoView.setVideoURI  		( Uri.parse ( sURI ) ) ;
 					oVideoView.setMediaController 	( new MediaController ( oThis ) ) ;
 				   	oVideoView.requestFocus 		( ) ;
 		            oVideoView.start 		        ( ) ;
 					oVideoView.setZOrderMediaOverlay( true ) ;
 		            return oVideoView.isPlaying 	( ) ;
 				}
 	        }
 		}
 		catch ( Exception e )
 		{
 			Log.d ( Globals.sApplicationName, "onPlayOverlayMovie: " + e.getMessage ( ), e ) ;
 			
 			onStopOverlayMovie ( ) ;
 		}
 
         return false ;
     }   
 
     //------------------------------------------------------------------
     private static void onStopOverlayMovie ( )
     {
         Log.d ( Globals.sApplicationName, "#### onStopOverlayMovie" ) ;
         
         if ( oVideoView != null )
         {
             oVideoView.stopPlayback 		( ) ;
             oVideoView.setVisibility        ( View.INVISIBLE ) ;
 			oViewGroup.removeView   		( oVideoView ) ;
 			oVideoView 						= null ;
 			//o3DView.setVisibility 			( View.VISIBLE ) ;
 			o3DView.onOverlayMovieStopped	( ) ;
         }
     }   
 
     //------------------------------------------------------------------
     public void onPrepared ( MediaPlayer mp )
     {
         
     } 
     
     //------------------------------------------------------------------
     public void onCompletion ( MediaPlayer mp )
     {
         onStopOverlayMovie ( ) ;
     } 
     
     //------------------------------------------------------------------
     public boolean onError ( MediaPlayer mp, int what, int extra )
     {
 		return false ;
     } 
     
     //------------------------------------------------------------------
     // View options methods (must be called before SetContentView).
     //
     public void setFullscreen ( )
     {
         requestWindowFeature   ( Window.FEATURE_NO_TITLE ) ;
         getWindow ( ).setFlags ( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN ) ;
     }
     public void setNoTitle ( )
     {
         requestWindowFeature ( Window.FEATURE_NO_TITLE ) ;
     }
 
     //------------------------------------------------------------------
     // Utility function to create a writable directory
     //
     protected boolean createWritableDirectory ( String sDir, boolean bDeleteOnExit )
     { 
         // Can we create to the output directory ?
         //
         try 
         { 
             File dir = new File ( sDir ) ;
             
             if ( ! dir.exists ( ) )
             {
                 if ( ! dir.mkdirs ( ) ) 
                 {
                     Log.d ( Globals.sApplicationName, "Could not create directory: " + sDir ) ;
                     return false ;
                 }
             }
 
             if ( bDeleteOnExit ) dir.deleteOnExit ( ) ; // We want the directory to delete itself when the activity is finished
         } 
         catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
         
         // Can we write to the output directory ?
         //
         try 
         { 
             if ( System.getSecurityManager ( ) != null )
             {
                 System.getSecurityManager ( ).checkWrite ( sDir ) ;
             }
         } 
         catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
     
         // Seems ok :)
         //
         return true ;
     }
     
     //------------------------------------------------------------------
     // Utility function to extract and dump a STK file from the APK.
     //
     protected static final int EXTRACT_ASSET_BUFFER_SIZE = 524288 ; // 512kb
 
     protected boolean extractAssetFromAPK ( String sAssetName, String sOutputDirPath, String sOutputName )
     { 
         if ( ! createWritableDirectory ( sOutputDirPath, true ) )
         {
 			Log.d ( Globals.sApplicationName, "Could not create folder " + sOutputDirPath ) ;
             return false ;
         }
 
         // Extract data
         //
         try
         {
             InputStream oIn  = getAssets ( ).open ( sAssetName ) ;
             if ( oIn != null )
             {
                 FileOutputStream oOut = new FileOutputStream ( sOutputDirPath + "/" + sOutputName ) ;
                 byte aBuffer [ ] = new byte [ EXTRACT_ASSET_BUFFER_SIZE ] ; 
                 while ( oIn.available ( ) > 0 )
                 {
                     int iLen = ( oIn.available ( ) > EXTRACT_ASSET_BUFFER_SIZE ) ? EXTRACT_ASSET_BUFFER_SIZE : (int)oIn.available ( ) ;
                     oIn .read  ( aBuffer, 0, iLen ) ;
                     oOut.write ( aBuffer, 0, iLen ) ;
                 }
                 oIn .close ( ) ;
                 oOut.close ( ) ;
 
                 Log.d ( Globals.sApplicationName, "Extracted asset " + sOutputName + " to folder" + sOutputDirPath ) ;
                 return true ;               
             }
         }
         catch ( IOException e ) { e.printStackTrace ( ) ; }
 		Log.d ( Globals.sApplicationName, "Could not extract asset " + sOutputName + " to folder" + sOutputDirPath ) ;
         return false ;               
     }    
     
     //------------------------------------------------------------------
     // Utility function to extract main STK file to a temporary directory
     //    
     private boolean extractMainPack ( )
     {
         // 20120614: try to get a file descriptor inside the APK directly, in order to avoid the copy
         //
         try
         {
             mPackFileAFD = getAssets ( ).openFd ( "S3DMain.smf" ) ; // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
             
             if ( mPackFileAFD != null )
             {
                 mPackFileDescriptor = mPackFileAFD.getFileDescriptor ( ) ;
                 mPackFileOffset     = mPackFileAFD.getStartOffset    ( ) ;
                 mPackFileLength     = mPackFileAFD.getLength         ( ) ;
 
                 if ( ( mPackFileDescriptor != null ) && ( mPackFileLength != AssetFileDescriptor.UNKNOWN_LENGTH ) )
                 {
                     Log.d ( Globals.sApplicationName, "Successfully opened file descriptor for main pack" ) ;
 
                     // Ok, we still need to fill the mPackDirPath variable, for other files.
                     // Try SD card first:
                     //
                     mPackDirPath = "/sdcard/Android/data/" + Globals.sPackageName ;
 
                     if ( ! createWritableDirectory ( mPackDirPath, true ) )
                     {
             			Log.d ( Globals.sApplicationName, "Could not create folder " + mPackDirPath ) ;
             			
                         // If something went wrong try on the phone internal filesystem 
                         //
                         mPackDirPath = getCacheDir ( ).getAbsolutePath ( ) ;
                     
                         if ( ! createWritableDirectory ( mPackDirPath, true ) )
                         {
                 			Log.d ( Globals.sApplicationName, "Could not create folder " + mPackDirPath ) ;
 
                 			return false ; // No choice...
                         }                                			
                     }
                     
                     return true ;
                 }
             }
         }
         catch ( IOException e ) { e.printStackTrace ( ) ; }
     
         // Then try to extract on the SD card
         //
         mPackDirPath = "/sdcard/Android/data/" + Globals.sPackageName ;
                 
         // Extract STK files from the APK and dump them to the packs directory
         //
         if ( extractAssetFromAPK ( "S3DMain.smf", mPackDirPath, "S3DMain.stk" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
         {
             return true ;
         }
 
         // If something went wrong try on the phone internal filesystem 
         //
         mPackDirPath = getCacheDir ( ).getAbsolutePath ( ) ;
                 
         // Extract STK files from the APK and dump them to the packs directory
         //
         if ( extractAssetFromAPK ( "S3DMain.smf", mPackDirPath, "S3DMain.stk" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
         {
             return true ;
         }
 
         // No more alternatives :(
         //
         mPackDirPath = "" ;
         return false ;
     }
 
     //------------------------------------------------------------------
     // Utility function to extract STK files to a temporary directory
     //    
     private boolean extractAdditionalFiles ( )
     {
         if ( mPackDirPath != "" )
         {
             try 
             {
                 // List assets
                 //
                 String aAssets [] = getAssets ( ).list ( "" ) ;
             
                 for ( int i = 0 ; i < aAssets.length ; i++ )
                 {
                     //if ( ! aAssets[i].endsWith ( ".stk" ) )
 					if ( ! aAssets[i].endsWith ( "S3DMain.smf" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
                     {
                         // Extract file
                         //
                         if ( ! extractAssetFromAPK ( aAssets[i], mPackDirPath, aAssets[i] ) )
                         {
                             //return false ;
                         }
                     }
                 }
                 
                 // OK
                 //
                 return true ;
             }
             catch ( IOException e ) { e.printStackTrace ( ) ; return false ; }
         }
         return false ;
     }    
     
     //------------------------------------------------------------------
     // Utility function to create the cache directory
     //    
     private boolean createCacheDirectory ( )
     {
         // First try on the SD card
         //
         mCacheDirPath = "/sdcard/Android/data/" + Globals.sPackageName + "/cache" ;
           
         if ( createWritableDirectory ( mCacheDirPath, false ) )
         {
             Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
             return true ;
         }
 
         // If something went wrong try on the phone internal filesystem 
         //
         File dir  = getCacheDir ( ) ;
         if ( dir != null )
         {
             mCacheDirPath = dir.getAbsolutePath ( ) ;
             Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
             return true ;
         }
         
         // No more alternatives :(
         //
         mCacheDirPath = "" ;
         return false ;
     }
 
     //------------------------------------------------------------------
     // Utility function to create the home directory
     //    
     private boolean createHomeDirectory ( )
     {
         // Get home directory path (persistent scratch pad)
         //
         File dir  = getDir ( "home", 0 ) ;
         if ( dir != null )
         {
             mHomeDirPath = getDir ( "home", 0 ).getAbsolutePath ( ) ;
             Log.d ( Globals.sApplicationName, "Using home directory: " + mHomeDirPath ) ;
             return true ;
         }
         return false ;
     }
     
     //------------------------------------------------------------------
     // Utility function to display a fatal error message.
     //    
     private void showError ( String s )
     {
         AlertDialog.Builder builder = new AlertDialog.Builder ( this ) ;
         builder.setMessage ( s ) ;
         builder.setTitle   ( Globals.sApplicationName ) ;
         //???builder.setIcon    ( R.drawable.app_icon ) ;
         builder.setPositiveButton ( "OK", new DialogInterface.OnClickListener ( ) 
                                               {
                                                   public void onClick ( DialogInterface dialog, int id) 
                                                   {
                                                      finish ( ) ;
                                                   }
                                               }
                                    ) ;
         AlertDialog dialog = builder.create ( ) ;
         dialog.show ( ) ;
     }    
 
     //------------------------------------------------------------------
 	// @@END_ACTIVITY_METHODS@@	
     //------------------------------------------------------------------
 
     //------------------------------------------------------------------
 	// @@BEGIN_ACTIVITY_VARIABLES@@	
     //------------------------------------------------------------------
     // Software keyboard view.
     //
     //private static KeyboardView     oKeyboardView   ;
     //private static EditText         oEditText       ;
 
     //------------------------------------------------------------------
     // Main view group.
     //
     private static RelativeLayout   oViewGroup      ;
     
     //------------------------------------------------------------------
     // Splash screen view.
     //
 //    private static View 			oSplashView		;
 
     //------------------------------------------------------------------
     // Video surface view.
     //
     private static VideoView        oVideoView      ;
     
     //------------------------------------------------------------------
     // 3D surface view.
     //
     private static S3DSurfaceView   o3DView         ;
 
     //------------------------------------------------------------------
     // Sound pool object to play sounds from Java.
     //
     private static SoundPool        oSoundPool      ;
 
     //------------------------------------------------------------------
     // Media player object to play musics from Java.
     //
     private static MediaPlayer      oMediaPlayer    ;
 	private static String [ ]       aMusicsList     = new String [64] ;
 
     //------------------------------------------------------------------
     // Power manager & wake lock object.
     //
 //    private static PowerManager             oPowerManager   ;
 //    private static PowerManager.WakeLock    oWakeLock       ;
 //    private static BroadcastReceiver        oIntentReceiver ;
           
     //------------------------------------------------------------------
     // Singleton object.
     //
     private static yourgame   oThis         ;        
           
     //------------------------------------------------------------------
     // Various files access infos.
     //
     private String              mCacheDirPath       ;
     private String              mHomeDirPath        ;
     private String              mAPKFilePath        ;
     private String              mPackDirPath        ;
     private AssetFileDescriptor mPackFileAFD        ;
     private FileDescriptor      mPackFileDescriptor ;   
     private long                mPackFileOffset     ;
     private long                mPackFileLength     ;
 
     //------------------------------------------------------------------
     // State variables
     //
                            
     //------------------------------------------------------------------
 	// @@END_ACTIVITY_VARIABLES@@	
     //------------------------------------------------------------------
 
     //------------------------------------------------------------------
     // Engine native library loading.
     //
     static 
     {
     	//--------------------------------------------------------------
 		// @@BEGIN_ACTIVITY_NATIVE_LIBRARIES@@	
     	//--------------------------------------------------------------
 		System.loadLibrary ( "crypto" ) ;
 		System.loadLibrary ( "ssl" ) ;
         System.loadLibrary ( "openal" ) ;
         System.loadLibrary ( "S3DClient" ) ;
     	//--------------------------------------------------------------
 		// @@END_ACTIVITY_NATIVE_LIBRARIES@@	
     	//--------------------------------------------------------------
     }    
 }
