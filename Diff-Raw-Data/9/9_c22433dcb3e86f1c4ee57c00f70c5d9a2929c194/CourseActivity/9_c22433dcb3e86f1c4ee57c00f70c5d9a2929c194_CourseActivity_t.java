 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Course;
 import no.hials.muldvarp.v2.domain.Date;
 import no.hials.muldvarp.v2.domain.Document;
 import no.hials.muldvarp.v2.domain.Help;
 import no.hials.muldvarp.v2.domain.Info;
 import no.hials.muldvarp.v2.domain.News;
 import no.hials.muldvarp.v2.domain.Programme;
 import no.hials.muldvarp.v2.domain.Requirement;
 import no.hials.muldvarp.v2.domain.Video;
 import no.hials.muldvarp.v2.fragments.FrontPageFragment;
 import no.hials.muldvarp.v2.fragments.ListFragment;
 import no.hials.muldvarp.v2.fragments.QuizFragment;
 import no.hials.muldvarp.v2.fragments.TextFragment;
 import no.hials.muldvarp.v2.utility.utils;
 
 /**
  *
  * @author kristoffer
  */
 public class CourseActivity extends MuldvarpActivity {
     public List<Fragment> fragmentList = new ArrayList<Fragment>();
     private Programme selectedProgramme;
     private Activity activity = this;
     public ActionBar actionBar;
     Info info;
     Course selectedCourse;
     Requirement req;
     Help help;
     Date date;
     public List<News> newsList = new ArrayList<News>();
     public List<Video> videoList = new ArrayList<Video>();
     public List<Document> documentList = new ArrayList<Document>();
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         info = new Info("Informasjon", "Blablablabl");
         req = new Requirement("Opptakskrav", "blablabla");
         help = new Help("Hjelpeside", "Dette gjør du blablabla");
         date = new Date("Datoer", "<h2>Test</h2><p>babvavasvas</p>");
         newsList.add(new News("Tittel", "Tekst"));
         videoList.add(new Video("Videotittel", "Beskrivelse"));
         documentList.add(new Document("Dokumenttittel", "Beskrivelse"));
         
         icons = new int[] {
             R.drawable.stolen_contacts,
             R.drawable.stolen_tikl,
             R.drawable.stolen_smsalt,
             R.drawable.stolen_youtube,
             R.drawable.stolen_calculator,
             R.drawable.stolen_dictonary,
             R.drawable.stolen_notes,
             R.drawable.stolen_calender,
             R.drawable.stolen_help
         };
         
         selectedCourse = new Course("Programmering");
         
         if(fragmentList.isEmpty()) {
             fragmentList.add(new FrontPageFragment(FrontPageFragment.Type.COURSE));
             fragmentList.add(new TextFragment(TextFragment.Type.INFO));
             fragmentList.add(new ListFragment(ListFragment.Type.NEWS));
 //            fragmentList.add(new ListFragment(ListFragment.Type.COURSES));
             fragmentList.add(new ListFragment(ListFragment.Type.VIDEO));
             fragmentList.add(new QuizFragment());
             fragmentList.add(new ListFragment(ListFragment.Type.DOCUMENTS));
             fragmentList.add(new TextFragment(TextFragment.Type.REQUIREMENT));
             fragmentList.add(new TextFragment(TextFragment.Type.DATE));
             fragmentList.add(new TextFragment(TextFragment.Type.HELP));
         }
         
         actionBar = getActionBar();
         actionBar.setHomeButtonEnabled(true);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         actionBar.setDisplayShowTitleEnabled(false);
         
         SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.programme_list,
           android.R.layout.simple_spinner_dropdown_item);
         
         ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
             String[] strings = getResources().getStringArray(R.array.programme_list);
             
             @Override
             public boolean onNavigationItemSelected(int position, long itemId) {
                 return utils.changeFragment(activity, fragmentList, position);
             }
         };
         
         actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
     }
     
     @Override
     public List<Course> getCourseList() {
         return selectedProgramme.getCourses();
     }
 
     public Course getSelectedCourse() {
         return selectedCourse;
     }
     
    @Override
     public Info getInfo(){
         return info;
     }
 
     @Override
     public List<News> getNewsList() {
         return super.getNewsList();
     }
 
     @Override
     public List<Video> getVideoList() {
         return super.getVideoList();
     }
 
     @Override
     public List<Document> getDocumentList() {
         return super.getDocumentList();
     }
     
     
 }
