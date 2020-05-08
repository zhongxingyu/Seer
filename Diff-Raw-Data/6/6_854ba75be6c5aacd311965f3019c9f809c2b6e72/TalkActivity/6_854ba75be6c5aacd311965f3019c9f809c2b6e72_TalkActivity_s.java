 package com.ehret.mixit.ui.activity;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.ehret.mixit.R;
 import com.ehret.mixit.domain.Salle;
 import com.ehret.mixit.domain.TypeFile;
 import com.ehret.mixit.domain.talk.Conference;
 import com.ehret.mixit.domain.talk.Talk;
 import com.ehret.mixit.model.ConferenceFacade;
 import com.ehret.mixit.ui.utils.UIUtils;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class TalkActivity extends AbstractActivity {
 
     private ImageView image;
     private TextView title;
     private TextView horaire;
     private TextView level;
     private TextView name;
     private TextView summary;
     private TextView descriptif;
     private TextView salle;
     private Long id;
     private String type;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_talk);
 
         this.title = (TextView) findViewById(R.id.talk_title);
         this.image = (ImageView) findViewById(R.id.talk_image);
         this.horaire = (TextView) findViewById(R.id.talk_horaire);
         this.level = (TextView) findViewById(R.id.talk_level);
         this.name = (TextView) findViewById(R.id.talk_name);
         this.summary = (TextView) findViewById(R.id.talk_summary);
         this.descriptif = (TextView) findViewById(R.id.talk_desciptif);
         this.salle = (TextView) findViewById(R.id.talk_salle);
 
         if (getIntent().getExtras() != null) {
             long id = getIntent().getExtras().getLong(UIUtils.MESSAGE);
             type = getIntent().getExtras().getString(UIUtils.TYPE);
 
             Conference conference = null;
             if(TypeFile.lightningtalks.name().equals(type)){
                 conference = ConferenceFacade.getInstance().getLightningtalk(getBaseContext(), id);
                 title.setText(getText(R.string.calendrier_ligthning) + " \n");
                 title.setBackgroundResource(R.color.red1);
                 image.setImageDrawable(getResources().getDrawable(R.drawable.lightning));
             }
             else if(TypeFile.workshops.name().equals(type)){
                 conference = ConferenceFacade.getInstance().getTalk(getBaseContext(), id);
                 title.setText(getText(R.string.calendrier_atelier)+ " \n");
                 title.setBackgroundResource(R.color.yellow2);
                 image.setImageDrawable(getResources().getDrawable(R.drawable.workshop));
             }
             else {
                 conference = ConferenceFacade.getInstance().getTalk(getBaseContext(), id);
                 title.setText(getText(R.string.calendrier_conf)+ " \n");
                 title.setBackgroundResource(R.color.yellow1);
                 image.setImageDrawable(getResources().getDrawable(R.drawable.talk));
             }
             title.setLines(2);
 
             SimpleDateFormat sdf = new SimpleDateFormat("EEE");
             if(conference.getStart()!=null && conference.getEnd()!=null){
                 horaire.setText(String.format(getResources().getString(R.string.periode),
                         sdf.format(conference.getStart()),
                         DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getStart()),
                         DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getEnd())
                 ));
             }
             else{
                 horaire.setText(getResources().getString(R.string.pasdate));
 
             }
             if(conference instanceof Talk){
                 level.setText(((Talk) conference).getLevel());
             }
             name.setText(conference.getTitle());
             summary.setText(conference.getSummary());
             descriptif.setText(conference.getDescription());
             Salle room = Salle.INCONNU;
             if(conference instanceof Talk){
                 room = Salle.getSalle(((Talk) conference).getRoom());
             }
             if(Salle.INCONNU != room){
                 salle.setText(String.format(getString(R.string.Salle),room.getNom()));
                 salle.setBackgroundColor(getBaseContext().getResources().getColor(room.getColor()));
                 salle.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                        UIUtils.startActivity(SalleActivity.class, getParent());
                     }
                 });
             }
         }
     }
 
     /**
      * Recuperation des marques de la partie en cours
      */
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if(id!=null){
             outState.putLong("ID_TALK", id);
             outState.putString("TYPE_TALK", type);
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         Long myId = savedInstanceState.getLong("ID_TALK");
         if(myId!=null){
             id= myId;
         }
         String myType = savedInstanceState.getString("TYPE_TALK");
         if(myType!=null){
             type= myType;
         }
     }
 }
