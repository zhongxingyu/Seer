 package org.gitmad.wreckroll;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import org.gitmad.wreckroll.canvas.Circle;
 import org.gitmad.wreckroll.canvas.ControllerBoard;
 import org.gitmad.wreckroll.canvas.Image;
 import org.gitmad.wreckroll.canvas.OnDrawListener;
 import org.gitmad.wreckroll.canvas.OnTouchPointListener;
 import org.gitmad.wreckroll.canvas.TouchPoint;
 import org.gitmad.wreckroll.client.RelayClient;
 import org.gitmad.wreckroll.client.WreckClient;
 import org.gitmad.wreckroll.util.CountdownTimer;
 import org.gitmad.wreckroll.video.CameraCaptureAsyncTask;
 import org.gitmad.wreckroll.video.SpyHardProcessor;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 
 
 public class WreckRollActivity extends Activity {
     
     private CameraCaptureAsyncTask cameraCaptureTask;
     private WreckClient client;
 
     private static final int FREEZE_FRAME_TIME_MS = 1000;
     
     private final String REGISTRAR_ADDRESS = "192.168.1.150";
 
     protected static final int MAX_DETECTED_FACES = 1;
 
     private CountdownTimer freezeFrameTimer = new CountdownTimer();
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         final ControllerBoard board = ((ControllerBoard)findViewById(R.id.panel));
 
 //        Rect rectgle= new Rect();
 //        Window window= getWindow();
 //        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
 //        int StatusBarHeight= rectgle.top;
 //        int contentViewTop= 
 //            window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
 //        int TitleBarHeight= contentViewTop - StatusBarHeight;
 //
 
         board.setOnDrawListener(new OnDrawListener() {
 
             public void onPreDraw() {
                 if (!WreckRollActivity.this.freezeFrameTimer.poll()) {
                     Bitmap bmp = cameraCaptureTask.getCurrentBitmap();
                     //set the latest bitmap captured from the camera
                     board.setBackgroundImage(bmp);
                 }
             }
 
             public void onPostDraw() {
                 //nothing to do
             }
         });
         
         //draw the buttons as touchpoint controls
         int titleBarHeight = 50;
         DisplayMetrics metrics = new DisplayMetrics();
         this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int statusBarHeight = (int) Math.ceil(titleBarHeight * metrics.density);
 
         int usableWidth  = metrics.widthPixels;
         int usableHeight = metrics.heightPixels - statusBarHeight;
         int dPadRadius = (int)(usableHeight * 0.6/2);
         //NOTE: currently assumes landscape mode
         Circle circle = new Circle((int)(dPadRadius * 1.3), usableHeight / 2, dPadRadius, Color.LTGRAY);
         board.addTouchPoint(circle);
         
         circle.setOnTouchListener(new OnTouchPointListener() {
             public boolean isSupportedAction(int action) {
                 return action != MotionEvent.ACTION_UP;
             }
 
             public void touchPerformed(TouchPoint point, float x, float y) {
                 processMovement((Circle) point, x, y);
             }
         });
         
         int radius = usableHeight / 8;
 
         Circle smokeButton = new Circle(metrics.widthPixels - 200, 1 * usableHeight / 2 - radius / 2, radius, Color.RED);
         board.addTouchPoint(smokeButton);
         smokeButton.setOnTouchListener(new OnTouchPointListener() {
             public boolean isSupportedAction(int action) {
                 return action == MotionEvent.ACTION_DOWN;
             }
 
             public void touchPerformed(TouchPoint point, float x, float y) {
                 client.toggleSmoke();
             }
         });
         
         Circle gunButton       = new Circle(metrics.widthPixels - 75, 1 * usableHeight / 4, radius, Color.RED);
         board.addTouchPoint(gunButton);
         gunButton.setOnTouchListener(new OnTouchPointListener() {
             public boolean isSupportedAction(int action) {
                 return action == MotionEvent.ACTION_DOWN;
             }
             
             public void touchPerformed(TouchPoint point, float x, float y) {
                 client.toggleGun();
             }
         });
 
         Circle canopyButton    = new Circle(metrics.widthPixels - 200, 3 * usableHeight / 4, radius, Color.RED);
         board.addTouchPoint(canopyButton);
         canopyButton.setOnTouchListener(new OnTouchPointListener() {
             
             public boolean isSupportedAction(int action) {
                 return action == MotionEvent.ACTION_DOWN;
             }
 
             public void touchPerformed(TouchPoint point, float x, float y) {
                 client.toggleCanopy();
             }
         });
         Circle startStopButton = new Circle(metrics.widthPixels - 75, 1 * usableHeight / 2 + radius / 2, radius, Color.RED);
         board.addTouchPoint(startStopButton);
 
         Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.camera);
         bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false);
         Image snapShotButton = new Image(bitmap, usableWidth / 2, 100);
         board.addTouchPoint(snapShotButton);
         snapShotButton.setOnTouchListener(new OnTouchPointListener() {
 
             public boolean isSupportedAction(int action) {
                 return action == MotionEvent.ACTION_DOWN;
             }
 
             public void touchPerformed(TouchPoint point, float x, float y) {
                 saveImage(board.getBackgroundImage());
                 WreckRollActivity.this.freezeFrameTimer.start(FREEZE_FRAME_TIME_MS);
             }
         });
         
     }
     
     protected void saveImage(Bitmap backgroundImage) {
         File imagesFolder = new File(Environment.getExternalStorageDirectory(), "WreckRoll");
         imagesFolder.mkdirs(); 
         String fileName = "image_" + new Date().getTime() + ".jpg";
         File output = new File(imagesFolder, fileName);
 //        while (output.exists()){
 //            fileName = "image_" + String.valueOf(imageNum) + ".jpg";
 //            output = new File(imagesFolder, fileName);
 //        }
 //        Uri uriSavedImage = Uri.fromFile(output);
 //        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
         try {
             FileOutputStream out = new FileOutputStream(output);
             backgroundImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
     }
 
     protected void processMovement(Circle point, float touchX, float touchY) {
         boolean moving = false;
         boolean forward = false;
         boolean turning = false;
         boolean right = false;
         
         int stopBarPX = 30;
         if (Math.abs(touchY - point.getY()) > stopBarPX) {
             if (touchY < point.getY()) {
                 this.client.forward();
             } else {
                 this.client.reverse();
             }
         } else {
             this.client.stop();
         }
         
         if (Math.abs(touchX - point.getX()) > stopBarPX) {
             turning = true;
             if (touchX > point.getX()) {
                 this.client.right();
             } else {
                 this.client.left();
             }
         } else {
             System.out.println("NOT TURNING");
         }        
     }
     
     private String queryRegistrar(String path){
     	HttpURLConnection connection = null;
     	try {
     		URL url = new URL("http://" + REGISTRAR_ADDRESS + ":8001" + path);
     		connection = (HttpURLConnection)url.openConnection();
     		connection.setDoOutput(false);
     		connection.setRequestMethod("GET");
     		connection.connect();
     		return connection.getResponseMessage();
     	} catch(IOException ex){
     		return null;
     	}
     }
     private String getRelayIP(){
     	String queryResult = queryRegistrar("/relay");
     	if (queryResult != null){
     		return queryResult;
     	}
     	return "192.168.1.50";
     }
     
     private String getCameraIP(){
     	String queryResult = queryRegistrar("/camera");
     	if (queryResult != null){
     		return queryResult;
     	}
     	return "192.168.1.20";
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         
         String ipCamera = getCameraIP();
 //        int color = 224, 170, 15 or 183, 135, 39
         this.cameraCaptureTask = new CameraCaptureAsyncTask(this, ipCamera, new SpyHardProcessor(MAX_DETECTED_FACES, Color.YELLOW, 50));
         this.cameraCaptureTask.execute();
         
         try {
 //            this.client = new DebugClient(); // DirectArduinoClient();
             ///TODO: connect to registrar
             String ipRelay   = getRelayIP();
             short  relayPort = 6696;
             this.client = new RelayClient(ipRelay, relayPort);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         this.cameraCaptureTask.cancel(true);
     }
 
     void setPanelBackground(Bitmap bitmap) {
         ((ControllerBoard)findViewById(R.id.panel)).setBackgroundImage(bitmap);
     }
 }
