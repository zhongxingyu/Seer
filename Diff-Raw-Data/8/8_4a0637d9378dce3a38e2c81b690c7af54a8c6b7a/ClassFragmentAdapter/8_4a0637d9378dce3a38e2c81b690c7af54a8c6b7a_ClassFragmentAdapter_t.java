 package com.pate.diablo;
 
 import java.util.List;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.view.View.OnClickListener;
 
 import com.pate.diablo.model.Class;
 import com.pate.diablo.model.D3Application;
 import com.viewpagerindicator.TitleProvider;
 
 public class ClassFragmentAdapter extends FragmentPagerAdapter implements TitleProvider
 {
     private List<Class> classes;
     private Context context;
     private OnClickListener listener;
     private String selectedClass;
     private int requiredLevel;
 
     public ClassFragmentAdapter(FragmentManager fm, Context context, String selectedClass, int requiredLevel) {
         super(fm);
         this.context = context;
         this.selectedClass = selectedClass;
         this.requiredLevel = requiredLevel;
         this.classes = D3Application.dataModel.getClasses();
     }
 
     @Override
     public String getTitle(int position) {
         return classes.get(position).getName();
     }
 
     @Override
     public Fragment getItem(int position) {
        ClassListFragment classList = ClassListFragment.newInstance(classes.get(position).getName(), requiredLevel, context);
         classList.setOnListItemClickListener(listener);
         return classList;
         
     }
 
     @Override
     public int getCount() {
         return classes.size();
     }
 
     public void setOnListItemClickListener(OnClickListener listener)
     {
         this.listener = listener;
     }
     
 }
