 package com.masterofcode.android.kyivhack.coolstorybro;
 
 import android.content.Intent;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import com.masterofcode.android.kyivhack.coolstorybro.base.BaseActivity;
 import com.masterofcode.android.kyivhack.coolstorybro.base.BaseFragment;
 import com.masterofcode.android.kyivhack.coolstorybro.fragments.ImageFragment;
 import com.masterofcode.android.kyivhack.coolstorybro.utils.PicasaConnector;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: boss1088
  * Date: 7/14/12
  * Time: 4:20 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CreateStoryActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
 
     private static String OUTPUT_FILE= "/sdcard/";
     private MediaRecorder recorder;
     String timeStemp = new String();
     ViewPager pager;
 
     String fullName;
 
     @Override
     protected void onCreate(Bundle arg0) {
         super.onCreate(arg0);
 
         setContentView(R.layout.activity_view_pager);
 
         updateUi();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.create_story_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_stop:
                 stopAllProcces();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
        private void updateUi() {
         List<BaseFragment> fragments = new Vector();
 
         for (String item : PicasaConnector.getInstance().getURLPhotosFromAlbum()) {
             ImageFragment fragment = new ImageFragment();
             fragment.setInstance(item);
             fragments.add(fragment);
         }
 
         PagerAdapter pagerAdapter = new FragmentAdapter(this.getSupportFragmentManager(), fragments);
 
         pager = (ViewPager) findViewById(R.id.pager);
         pager.setAdapter(pagerAdapter);
         pager.setOnPageChangeListener(this);
 
         beginRecording();
         timeStemp="[[0,"+System.currentTimeMillis()+"],";
     }
 
     @Override
     public void onPageScrolled(int i, float v, int i1) {
     }
 
     @Override
     public void onPageSelected(int i) {
         timeStemp+="["+String.valueOf(i)+","+System.currentTimeMillis()+"],";
     }
 
     @Override
     public void onPageScrollStateChanged(int i) {
     }
 
     public class FragmentAdapter extends FragmentPagerAdapter {
 
         private final List<BaseFragment> fragments;
 
         public FragmentAdapter(FragmentManager fm, List<BaseFragment> fragments) {
             super(fm);
             this.fragments = fragments;
         }
 
         @Override
         public Fragment getItem(int position) {
             return this.fragments.get(position);
         }
 
         @Override
         public int getCount() {
             return this.fragments.size();
         }
     }
 
     private void beginRecording() {
         killMediaRecorder();
 
         createDirectory();
 
         final String curAlbum = PicasaConnector.getInstance().getCurrentAlbumName();
         fullName = OUTPUT_FILE+".coolstorybro/"+curAlbum+"/"+curAlbum+".3gp";
 
         File outFile = new File(fullName);
         if(outFile.exists()){
             outFile.delete();
         }
 
         recorder = new MediaRecorder();
         recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         recorder.setOutputFile(fullName);
         try {
             recorder.prepare();
            recorder.start();
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }

     }
 
     private void createDirectory(){
         File outDirectory = new File(OUTPUT_FILE+".coolstorybro");
         if((!outDirectory.exists())||(outDirectory.exists()&&!outDirectory.isDirectory())){
             outDirectory.mkdir();
         }
 
         final String curAlbum = PicasaConnector.getInstance().getCurrentAlbumName();
         final String file_name = OUTPUT_FILE+".coolstorybro/"+curAlbum;
         outDirectory = new File(file_name);
         if((!outDirectory.exists())||(outDirectory.exists()&&!outDirectory.isDirectory())){
             outDirectory.mkdir();
         }
     }
 
     private void stopRecording() {
         if (recorder != null) {
             recorder.stop();
         }
     }
     private void killMediaRecorder() {
         if (recorder != null) {
             recorder.release();
         }
     }
 
     private void stopAllProcces() {
         timeStemp+="[0,"+System.currentTimeMillis()+"]]";
         stopRecording();
 
         Intent intent = new Intent(this, UploadActivity.class);
         intent.putExtra("timestemp",timeStemp);
         intent.putExtra("filename",fullName);
 
         startActivity(intent);
         finish();
     }
 
     @Override
     protected BaseFragment addListFragment() {
         return null;
     }
 
     @Override
     protected BaseFragment addBaseFragment() {
         return null;
     }
 }
