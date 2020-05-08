 package com.newsrss.Feed;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.animation.Animation;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import android.view.animation.TranslateAnimation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class NewsRssActivity extends SherlockActivity implements Animation.AnimationListener{
     /**
      * Called when the activity is first created.
      */
     boolean cellStatusPosition = false;
 
     int animationID;
     View slideLayar1;
 
     View menu;
     View app;
     boolean menuOut = false;
     AnimParams animParams = new AnimParams();
     int idArticlList;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.main);
 
 
         menu = findViewById(R.id.menu);
         app = findViewById(R.id.app);
 
 
         app.findViewById(R.id.BtnSlide).setOnClickListener(new ClickListener());
 
 
         menu.findViewById(R.id.show_podcast).setOnClickListener(new ShowPodcastOnclickListener());
         ListView rssListView = (ListView) findViewById(R.id.rssListView);
 
         ShowAriclList();
         rssListView.setOnItemClickListener(new LaunchDetalActiviti());
 
     }
 
     class  LaunchDetalActiviti implements AdapterView.OnItemClickListener{
 
 
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             //To change body of implemented methods use File | Settings | File Templates.
             //TODO: Detale Activity Launcher
             System.out.println("item click" + id);
 
             switch (idArticlList)  {
                 case 1:
 
                     Intent startDetailArticl = new Intent(NewsRssActivity.this, DetailsArticle.class);
                     startDetailArticl.putExtra("position", position);
                     startActivity(startDetailArticl);
                     break;
                 case 2:
                     Intent startDetailPodcast = new Intent(NewsRssActivity.this,DetailsPodcast.class );
                     startDetailPodcast.putExtra("position", position);
                     startActivity(startDetailPodcast);
                     break;
             }
 
 
 
         }
     }
 
 
     private List<Map<String, ?>> createArticleList() {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
 
         try
         {
            DataStorage.updateArticleList();
            ArrayList<Article> articleList = DataStorage.getArticleList();
 
             for (Article article : articleList)
             {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", article.getTitle());
                 map.put("rssnewsdate", article.getPubDate());
                 items.add(map);
             }
         }
         catch(Exception e) {
             e.printStackTrace();
         }
 
         return items;
     }
 
     private List<Map<String, ?>> createPodcastList()   {
         List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
 
         try {
            DataStorage.updatePodcastList();
            ArrayList<Podcast> podcastList = DataStorage.getPodcastList();
 
             for(Podcast podcast : podcastList) {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("rssnewstitle", podcast.getTitle());
                 map.put("rssnewsdate", podcast.getPubDate());
                 items.add(map);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return  items;
     }
 
     void layoutApp(boolean menuOut) {
         System.out.println("layout [" + animParams.left + "," + animParams.top + "," + animParams.right + ","
                 + animParams.bottom + "]");
         app.layout(animParams.left, animParams.top, animParams.right, animParams.bottom);
         //Now that we've set the app.layout property we can clear the animation, flicker avoided :)
         app.clearAnimation();
     }
 
     public void onAnimationEnd(Animation arg0) {
         // TODO Auto-generated method stub
         System.out.println("onAnimationEnd");
         switch (animationID){
             case (1):  {
                 menuOut = !menuOut;
                 if (!menuOut) {
                     menu.setVisibility(View.INVISIBLE);
                 }
                 layoutApp(menuOut);
             }
             break;
 
         }
 
 
     }
 
     public void onAnimationRepeat(Animation animation) {
 
         // TODO Auto-generated method stub
 
     }
 
     public void onAnimationStart(Animation animation) {
         // TODO Auto-generated method stub
         System.out.println("onAnimationRepeat");
 
     }
 
     static class AnimParams{
         int left, right, top, bottom;
         void init (int left, int top, int right, int bottom){
             this.left = left;
             this.top = top;
             this.right = right;
             this.bottom = bottom;
 
         }
     }
 
     public void ShowAriclList() {
         ListView rssListView = (ListView) findViewById(R.id.rssListView);
 
         DataStorage.updateArticleList();
         SimpleAdapter adapter = new SimpleAdapter(
                 this,
                 createArticleList(),
                 R.layout.rss_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate"},
                 new int [] { R.id.rss_news_title, R.id.rss_news_date});
         idArticlList = 1;
         rssListView.setAdapter(adapter);
     }
 
     public void ShowPodcastList(){
         ListView rssListView = (ListView) findViewById(R.id.rssListView);
         SimpleAdapter adapter = new SimpleAdapter(
                 this, createPodcastList()/*list*/, R.layout.podcast_item_layout,
                 new String[] { "rssnewstitle", "rssnewsdate"},
                 new int [] { R.id.rss_podcast_title, R.id.rss_podcast_date});
         idArticlList = 2;
         rssListView.setAdapter(adapter);
 
     }
 
     class ShowPodcastOnclickListener implements View.OnClickListener{
 
         @Override
         public void onClick(View v) {
             //To change body of implemented methods use File | Settings | File Templates.
 
             NewsRssActivity me = NewsRssActivity.this;
             Context context = me;
             Animation anim;
 
             int w = app.getMeasuredWidth();
             int h = app.getMeasuredHeight();
             int left = (int) (app.getMeasuredWidth() * 0.8);
 
 
 
                 // anim = AnimationUtils.loadAnimation(context, R.anim.push_left_in_80);
                 anim = new TranslateAnimation(left, 0, 0, 0);
                 animParams.init(0, 0, w, h);
 
 
             animationID =1;
             anim.setDuration(500);
 
             anim.setAnimationListener(me);
 
             //Tell the animation to stay as it ended (we are going to set the app.layout first than remove this property)
             anim.setFillAfter(true);
 
 
             // Only use fillEnabled and fillAfter if we don't call layout ourselves.
             // We need to do the layout ourselves and not use fillEnabled and fillAfter because when the anim is finished
             // although the View appears to have moved, it is actually just a drawing effect and the View hasn't moved.
             // Therefore clicking on the screen where the button appears does not work, but clicking where the View *was* does
             // work.
             // anim.setFillEnabled(true);
             // anim.setFillAfter(true);
 
             app.startAnimation(anim);
             ShowPodcastList();
         }
     }
 
     class ClickListener implements View.OnClickListener {
         @Override
         public void onClick(View v) {
 
             NewsRssActivity me = NewsRssActivity.this;
             Context context = me;
             Animation anim;
 
             int w = app.getMeasuredWidth();
             int h = app.getMeasuredHeight();
             int left = (int) (app.getMeasuredWidth() * 0.8);
 
             if (!menuOut) {
                 // anim = AnimationUtils.loadAnimation(context, R.anim.push_right_out_80);
                 anim = new TranslateAnimation(0, left, 0, 0);
                 menu.setVisibility(View.VISIBLE);
                 animParams.init(left, 0, left + w, h);
             } else {
                 // anim = AnimationUtils.loadAnimation(context, R.anim.push_left_in_80);
                 anim = new TranslateAnimation(0, -left, 0, 0);
                 animParams.init(0, 0, w, h);
             }
             animationID =1;
             anim.setDuration(500);
 
             anim.setAnimationListener(me);
 
             //Tell the animation to stay as it ended (we are going to set the app.layout first than remove this property)
             anim.setFillAfter(true);
 
 
             // Only use fillEnabled and fillAfter if we don't call layout ourselves.
             // We need to do the layout ourselves and not use fillEnabled and fillAfter because when the anim is finished
             // although the View appears to have moved, it is actually just a drawing effect and the View hasn't moved.
             // Therefore clicking on the screen where the button appears does not work, but clicking where the View *was* does
             // work.
             // anim.setFillEnabled(true);
             // anim.setFillAfter(true);
 
             app.startAnimation(anim);
 
         }
     }
 
     /*
     class MyCAdapter extends SimpleAdapter {
 
         private Context context;
         private ListView mListView;
         private List<? extends Map<String, ?>>   data;
         private int resurs;
         private  String[] from;
         private int[] to;
 
 
         public MyCAdapter(Context context,
                           List<? extends Map<String, ?>> data,
                           int _resource,
                           String[] from,
                           int[] to){
             super(context, data, _resource, from, to);
 
             MyCAdapter.this.context = context;
             MyCAdapter.this.data = data;
             MyCAdapter.this.resurs = _resource;
             MyCAdapter.this.from = from;
             MyCAdapter.this.to = to;
 
         }
         protected class RowViewHolder {
             public TextView mTitle;
             public TextView mDate;
             public ImageView mImageView;
         }
 
         public View getView(int position, View convertView, ViewGroup parent){
             View v = convertView;
             System.out.println("Btn 1");
 
             if (v == null) {
 
                 LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = inflater.inflate(R.layout.rss_item_layout, null);
             }
 
             final View  slideLayar= v.findViewById(R.id.mini_func_lay);
             RowViewHolder holder  = new RowViewHolder();
             holder.mImageView = (ImageView)v.findViewById(R.id.rss_img_news_pass);
             holder.mTitle = (TextView)v.findViewById(R.id.rss_news_title);
             holder.mDate = (TextView)v.findViewById(R.id.rss_news_date);
 
             //Взаимодействие с элементами Списка Картинка Татйтл и Дата
 
             holder.mImageView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     //To change body of implemented methods use File | Settings | File Templates.
                     System.out.println("Btn на картинку ");
                 }
             });
             v.setTag(holder);
             /*
             v.findViewById(R.id.mini_slide).setOnClickListener(new View.OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
 
                     System.out.println("Btn 123"+" "+ v.getId());
                     //TODO Auto-generated method stub
                     NewsRssActivity me1 = NewsRssActivity.this;
                     Context context1 = me1;
                     Animation anim1;
                     if (cellStatusPosition == false){
                         anim1 = new TranslateAnimation(0, 50, 0, 0);
                         cellStatusPosition = true;
                     }else {
                         anim1 = new TranslateAnimation(50, 0, 0, 0);
                         cellStatusPosition = false;
                     }
 
 
 
                     animationID = 2;
                     anim1.setDuration(800);
                     anim1.setAnimationListener(me1);
                     anim1.setFillAfter(true);
                     slideLayar.startAnimation(anim1);
 
 
 
                 }
             });
 
 
 
 
             return v;
 
 
         }
 
 
 
 }        */
 
     class CustomViewBinder implements SimpleAdapter.ViewBinder {
         @Override
         public boolean setViewValue(View view, Object data,String textRepresentation) {
             if((view instanceof ImageView) & (data instanceof Drawable))
             {
                 ImageView iv = (ImageView) view;
                 Drawable image = (Drawable) data;
                 iv.setImageDrawable(image);
                 return true;
             }
             return false;
         }
 
     }
 }
