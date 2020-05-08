 package com.example.shiftjistest;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.CharConversionException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.io.IOUtils;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         Executors.newSingleThreadExecutor().execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     read();
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     private void read() throws IOException, UnsupportedEncodingException {
         InputStream raw = getAssets().open("shift_jis.txt");
         
         Charset shiftJisCharset = Charset.forName("Shift_JIS");
         CharsetDecoder decoder = shiftJisCharset.newDecoder();
        Reader is = new BufferedReader(new InputStreamReader(raw, decoder));
         
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         IOUtils.copy(is, output);
         
         
        //CharBuffer buffer = decoder.decode(ByteBuffer.wrap(output.toByteArray()));
        
        //final String text = buffer.toString();
         final String text = new String(output.toByteArray());
         Log.v("encode-test", "out: " + text.length());
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 TextView textView = (TextView) findViewById(R.id.textview);
                 textView.setText(text);
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 }
