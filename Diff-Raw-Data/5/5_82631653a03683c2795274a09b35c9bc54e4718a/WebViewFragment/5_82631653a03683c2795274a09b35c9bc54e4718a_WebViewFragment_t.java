 package com.brianantonelli.babymon;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.webkit.HttpAuthHandler;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.Toast;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.PasswordAuthentication;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javazoom.jl.decoder.Bitstream;
 import javazoom.jl.decoder.BitstreamException;
 import javazoom.jl.decoder.Decoder;
 import javazoom.jl.decoder.DecoderException;
 import javazoom.jl.decoder.Header;
 import javazoom.jl.decoder.SampleBuffer;
 
 /**
  * Created by monkeymojo on 9/14/13.
  */
 public class WebViewFragment extends Fragment {
     private static final String LOCAL_HTML_FILE = "file:///android_asset/stream_android.html?endpoint=";
     private AudioTrack audioTrack;
     private final String TAG = "WebVideoFragment";
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         final MainActivity activity = (MainActivity)getActivity();
         Authenticator.setDefault(new Authenticator() {
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(activity.getServerUsername(), activity.getServerPassword().toCharArray());
             }
         });
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.web_layout, container, false);
 
         final WebView wv = (WebView) v.findViewById(R.id.webPage);
         WebSettings webSettings = wv.getSettings();
         webSettings.setJavaScriptEnabled(true);
 
         webSettings.setLoadWithOverviewMode(true);
         webSettings.setUseWideViewPort(true);
         webSettings.setAllowFileAccess(true);
 
         wv.setBackgroundColor(Color.BLACK);
         wv.setVerticalScrollBarEnabled(false);
         wv.setHorizontalScrollBarEnabled(false);
 
         wv.setWebViewClient(new WebViewClient(){
             @Override
             public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                 MainActivity activity = (MainActivity)getActivity();
                 handler.proceed(activity.getServerUsername(), activity.getServerPassword());
             }
         });
 
         // get server endpoint
         final String endpoint = ((MainActivity) getActivity()).getServerAddress();
 
         wv.loadUrl(LOCAL_HTML_FILE + endpoint);
 
         final MainActivity activity = (MainActivity)getActivity();
         Button saveButton = (Button) v.findViewById(R.id.saveWebButton);
         saveButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 new NetworkTask(activity).execute("http://" + endpoint + ":8080/?action=snapshot");
             }
         });
 
         final Button audioButton = (Button) v.findViewById(R.id.audioButton);
         audioButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(audioTrack == null){
                     startAudio();
                    audioButton.setText("Mute Sound");
                 }
                 else{
                     stopAudio();
                    audioButton.setText("Play Sound");
                 }
             }
         });
 
         // start streaming audio
         startAudio();
 
         // keep the screen on
         activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         return v;
     }
 
     public void onStop(){
         Log.d(TAG, "onStop");
         stopAudio();
         super.onStop();
     }
 
     public void onDestroy(){
         Log.d(TAG, "onDestroy");
         stopAudio();
         super.onDestroy();
     }
 
     public void startAudio(){
         Log.d(TAG, "Starting audio");
 
         new AudioTask().execute("http://" + ((MainActivity) getActivity()).getServerAddress() + ":8000/stream.mp3");
     }
 
     public void stopAudio(){
         Log.d(TAG, "Stopping audio, audioTrack = " + audioTrack);
         if(audioTrack != null){
 
             audioTrack.stop();
             audioTrack.release();
             audioTrack = null;
         }
     }
 
     private class NetworkTask extends AsyncTask<String, Void, Integer> {
         MainActivity activity;
 
         public NetworkTask(MainActivity activity){
             this.activity = activity;
         }
 
         @Override
         protected Integer doInBackground(String... params) {
             HttpURLConnection httpCon;
             try{
                 URL apiUrl = new URL(params[0]);
                 httpCon = (HttpURLConnection) apiUrl.openConnection();
 
                 InputStream input = httpCon.getInputStream();
                 Bitmap myBitmap = BitmapFactory.decodeStream(input);
                 SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH:mm");
                 String fileName = String.format("%s/DCIM/babycam%s.jpg",
                         Environment.getExternalStorageDirectory().getAbsolutePath(),
                         sdf.format(new Date()));
                 String data1 = String.valueOf(fileName);
 
                 Log.d(TAG, "Saved to: " + fileName);
 
                 FileOutputStream stream = new FileOutputStream(data1);
 
                 ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                 myBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outstream);
                 byte[] byteArray = outstream.toByteArray();
 
                 stream.write(byteArray);
                 stream.close();
 
                 return httpCon.getResponseCode();
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             return HttpURLConnection.HTTP_BAD_REQUEST;
         }
 
         @Override
         protected void onPostExecute(Integer responseCode) {
         }
     }
 
     private class AudioTask extends AsyncTask<String, Void, Void> {
         protected Void doInBackground(String... url) {
             boolean retry = true;
             while (retry) {
                 try {
                     int shortSizeInBytes = Short.SIZE/Byte.SIZE;
                     // define the buffer size for audio track
                     Decoder decoder = new Decoder();
                     int bufferSize = (int) decoder.getOutputBlockSize() * shortSizeInBytes;
                     // todo: validate settings with darkice
                     audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                     audioTrack.play();
 
                     HttpClient client = new DefaultHttpClient();
                     HttpGet request = new HttpGet();
                     request.setURI(new URI(url[0]));
                     HttpResponse response = client.execute(request);
                     HttpEntity entity = response.getEntity();
                     InputStream inputStream = new BufferedInputStream(entity.getContent(), 8 * decoder.getOutputBlockSize());
                     Bitstream bitstream = new Bitstream(inputStream);
                     boolean done = false;
                     while (!done) {
                         try {
                             Header frameHeader = bitstream.readFrame();
                             SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                             short[] pcm = output.getBuffer();
                             audioTrack.write(pcm, 0, output.getBufferLength());
                             bitstream.closeFrame();
                         } catch (ArrayIndexOutOfBoundsException e) {
                             Log.d(TAG, "Index out of bounds: " + e.getMessage());
                         }
                     }
                     audioTrack.stop();
                     audioTrack.release();
                     inputStream.close();
                 } catch (IOException e) {
                     Log.e(TAG, e.getMessage());
                 } catch (DecoderException e) {
                     Log.e(TAG, e.getMessage());
                 } catch (BitstreamException e) {
                     Log.e(TAG, e.getMessage());
                 } catch (URISyntaxException e) {
                     Log.e(TAG, e.getMessage());
                     retry = false;
                 } catch (NullPointerException e) {
                     return null;
                 } catch(Exception e){
                     return null;
                 }
             }
             return null;
         }
     }
 
 }
 
