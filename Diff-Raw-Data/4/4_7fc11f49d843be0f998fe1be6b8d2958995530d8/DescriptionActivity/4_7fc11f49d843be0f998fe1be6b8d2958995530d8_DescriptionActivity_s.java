 package com.github.alexesprit.lostfilm.activity;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.github.alexesprit.lostfilm.R;
 import com.github.alexesprit.lostfilm.adapter.EpisodeItemAdapter;
 import com.github.alexesprit.lostfilm.item.SerialDescription;
 import com.github.alexesprit.lostfilm.loader.DescripionLoader;
 
 
 public class DescriptionActivity extends SherlockActivity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.desc_view);
 
         Intent intent = getIntent();
         if (null != intent) {
             String url = intent.getStringExtra("url");
             String name = intent.getStringExtra("name");
             setTitle(name);
             loadDescription(url);
         }
     }
 
     private void loadDescription(String url) {
         new DescLoadTask().execute(url);
     }
 
     private void updateView(SerialDescription desc) {
         View headerView = getLayoutInflater().inflate(R.layout.desc_header, null);
 
         TextView descView = (TextView)headerView.findViewById(R.id.serial_description);
         descView.setText(desc.desc);
 
         ImageView posterView = (ImageView)headerView.findViewById(R.id.serial_poster);
         posterView.setImageBitmap(desc.poster);
 
         ListView episodesList = (ListView)findViewById(R.id.episodes_list);
         episodesList.addHeaderView(headerView);
         episodesList.setAdapter(new EpisodeItemAdapter(this, desc.episodes));
     }
 
     private class DescLoadTask extends AsyncTask<String, Void, SerialDescription> {
         private DescripionLoader loader = new DescripionLoader();
 
         @Override
         protected void onPreExecute() {
 
         }
 
         @Override
         protected SerialDescription doInBackground(String... url) {
             return loader.getDescription(url[0]);
         }
 
         @Override
         protected void onPostExecute(SerialDescription desc) {
             if (null != desc) {
                 updateView(desc);
             }
             findViewById(R.id.loading_layout).setVisibility(View.GONE);
         }
     }
 }
