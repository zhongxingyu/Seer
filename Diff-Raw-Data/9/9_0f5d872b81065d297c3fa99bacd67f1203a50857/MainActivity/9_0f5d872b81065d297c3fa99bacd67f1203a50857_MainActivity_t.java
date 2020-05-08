 package z.hol.loadingstate.sample;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import z.hol.loadingstate.LoadingStateLayout.ReloadingListener;
 import z.hol.loadingstate.view.ListViewWithLoadingState;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 
 public class MainActivity extends Activity implements OnItemClickListener, ReloadingListener{
     
     private ListViewWithLoadingState mListView;
     private ArrayAdapter<String> mAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ls__test_layout_main);
         mListView = (ListViewWithLoadingState) findViewById(android.R.id.list);
         mListView.getDataView().setOnItemClickListener(this);
         mListView.setReloadingListener(this);
         loading();
     }
     
     @Override
     protected void onDestroy() {
         // TODO Auto-generated method stub
         super.onDestroy();
         if (mLoadingTask != null && mLoadingTask.getStatus() == Status.RUNNING){
             mLoadingTask.cancel(true);
         }
     }
     
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position,
             long id) {
         // TODO Auto-generated method stub
         if (position == 3){
             mAdapter.clear();
         }else if (position == 4){
             mAdapter.clear();
             mListView.error();
         }else if (position == 5){
             mAdapter.clear();
             loading();
         }else if (position == 6){
             Intent i = new Intent(this, SimpleLayoutActivity.class);
             startActivity(i);
         }
     }
 
     @Override
     public void onErrorReloading() {
         // TODO Auto-generated method stub
         loading();
     }
 
     @Override
     public void onEmptyReloading() {
         // TODO Auto-generated method stub
         loading();
     }
 
 
 
     private LoadingTask mLoadingTask;
     private void loading(){
         mLoadingTask = new LoadingTask();
         mLoadingTask.execute();
     }
 
     private class LoadingTask extends AsyncTask<Void, Void, List<String>>{
         
         @Override
         protected void onPreExecute() {
             // TODO Auto-generated method stub
             super.onPreExecute();
             mListView.startLoading();
         }
 
         @Override
         protected List<String> doInBackground(Void... params) {
             // TODO Auto-generated method stub
             List<String> result = new ArrayList<String>();
             SystemClock.sleep(3500);
             for (int i = 0; i < 35; i ++){
                 String item;
                 if (i == 3){
                     item = "Empty list";
                 }else if (i == 4){
                     item = "Error list";
                 }else if (i == 5){
                     item = "Reload list";
                 } else if (i== 6){
                     item = "Set data from xml";
                 }
                 else{
                     item = "Item " + i;
                 }
                 result.add(item);
             }
             return result;
         }
         
         @Override
         protected void onPostExecute(List<String> result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             mListView.stopLoading();
             if (result != null){
                 if (mAdapter == null){
                     ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.ls_test_item_text, result);
                     mListView.setAdapter(adapter);
                     mAdapter = adapter;
                 }else{
                     mAdapter.addAll(result);
                 }
             }
         }
     }
 }
