 package fr.ybo.ybotv.android.activity;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.lasylist.ImageLoader;
 import fr.ybo.ybotv.android.modele.ChannelWithProgramme;
 import fr.ybo.ybotv.android.modele.Programme;
 
 import java.util.List;
 
 public class ProgrammeActivity extends SherlockActivity {
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.programme_activity);
         Programme programme = getIntent().getParcelableExtra("programme");
         ImageLoader imageLoader=new ImageLoader(getApplicationContext());
 
         ImageView icon = (ImageView) findViewById(R.id.programme_activity_icon);
         TextView title = (TextView) findViewById(R.id.programme_activity_title);
         TextView description = (TextView) findViewById(R.id.programme_activity_description);
 
         if (programme.getIcon() != null && programme.getIcon().length() > 0) {
             imageLoader.DisplayImage(programme.getIcon(), icon);
             icon.setVisibility(View.VISIBLE);
         } else {
             icon.setVisibility(View.GONE);
         }
         title.setText(programme.getTitle());
        description.setText(programme.getDesc());
 
     }
 
 }
 
