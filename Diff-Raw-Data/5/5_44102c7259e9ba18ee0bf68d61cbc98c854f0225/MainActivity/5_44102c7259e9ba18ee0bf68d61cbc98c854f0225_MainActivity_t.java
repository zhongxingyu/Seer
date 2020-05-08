 package by.fksis.schedule.app;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.widget.TextView;
 import by.fksis.schedule.API;
 import by.fksis.schedule.Preferences;
 import by.fksis.schedule.R;
 import by.fksis.schedule.Util;
 import by.fksis.schedule.adapters.WeekPagerAdapter;
 import by.fksis.schedule.async.SynchronizationTask;
 import by.fksis.schedule.dal.ScheduleClass;
 import com.WazaBe.HoloEverywhere.sherlock.SFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.danikula.aibolit.Aibolit;
 import com.danikula.aibolit.annotation.InjectView;
 import com.viewpagerindicator.TitlePageIndicator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 public class MainActivity extends SFragmentActivity {
     private DateFormat sdf_all = new SimpleDateFormat("yyyy-MM-dd hh:mm");
     private DateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Aibolit.doInjections(this);
 
         getSupportActionBar().setTitle(R.string.schedule);
         getSupportActionBar().setIcon(R.drawable.logo);
 
         API.loadCredentials(this);
 
         if (!API.credentialsPresent()) {
             startActivity(new Intent(this, LoginActivity.class));
             finish();
         }
 
         viewPager.setAdapter(new WeekPagerAdapter(getSupportFragmentManager(), this));
         indicator.setViewPager(viewPager);
         indicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Underline);
         indicator.setTextColor(0xff888888);
         indicator.setTextSize(20);
         indicator.setSelectedColor(0xff444444);
         indicator.setSelectedBold(false);
 
         viewPager.setCurrentItem(WeekPagerAdapter.NOW_PAGE);
 
         if (new Preferences(this).getAutoReload())
             new SynchronizationTask(this, false).execute();
 
         currentClass.setText(getString(R.string.no_classes));
         nextClass.setText(getString(R.string.no_classes));
         handler.sendEmptyMessage(0);
 
     }
     private Handler handler = new Handler() {
         @Override
         public void dispatchMessage(Message msg) {
 
             Calendar time = Calendar.getInstance();
             time.setTimeInMillis(System.currentTimeMillis());
             time.set(Calendar.HOUR, 0);
             time.set(Calendar.MINUTE, 00);
             time.set(Calendar.AM_PM, Calendar.AM);
             time.add(Calendar.DATE, 1);
 
             List<ScheduleClass> classes = ScheduleClass.get(ScheduleClass.class)
                     .filter("weeks%", "%" + Util.getScheduleWeek(Calendar.getInstance().getTime()) + "%")
                     .filter("day", Util.getDayOfWeekIndex(Calendar.getInstance()))
                     .filter("studentGroup", new Preferences(MainActivity.this).getGroup())
                     .filter("subgroups%", "%" + new Preferences(MainActivity.this).getSubgroupString() + "%")
                     .list();
 
             boolean found = false;
             for (ScheduleClass clazz : classes) {
                 Date dateStart = null ,dateEnd = null;
                 try {
                     dateStart = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + getResources().getStringArray(R.array.timeSlotStart)[clazz.timeSlot]);
                     dateEnd = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + getResources().getStringArray(R.array.timeSlotEnd)[clazz.timeSlot]);
                 } catch (Exception e){
                     Log.e(DayScheduleFragment.class.getSimpleName(), e.getMessage());
                 }
                 if ((dateStart.getTime() <= Calendar.getInstance().getTime().getTime())
                         && (dateEnd.getTime() >= Calendar.getInstance().getTime().getTime())) {
                     currentClass.setText(clazz.name + " " + clazz.room + "\n" +
                             Util.defaultValue(clazz.type, "") + " " + Util.defaultValue(clazz.teacher, ""));
                     time.setTime(dateEnd);
                     if ((classes.size() - 1)  > classes.lastIndexOf(clazz))  {
                         ScheduleClass l_next = classes.get(classes.lastIndexOf(clazz) + 1);
                         nextClass.setText( l_next.name + " " + l_next.room  + "\n" +
                                 Util.defaultValue(l_next.type, "") + " " + Util.defaultValue(l_next.teacher, ""));
                     } else {
                         nextClass.setText(getString(R.string.no_classes));
                     }
                     found = true;
                 }
             }
             if (!found) {
                 found = false;
                 for (Iterator<ScheduleClass> i = classes.iterator(); i.hasNext() && !found; ) {
                     ScheduleClass l = i.next();
                     Date dateStart = null, dateEnd;
                     try {
                         dateStart = sdf_all.parse(sdf_date.format(Calendar.getInstance().getTime()) + " " + getResources().getStringArray(R.array.timeSlotStart)[l.timeSlot]);
                     } catch (Exception e){
                         Log.e(DayScheduleFragment.class.getSimpleName(),e.getMessage());
                     }
                     if (dateStart.getTime() > Calendar.getInstance().getTime().getTime()) {
                         found = true;
                         currentClass.setText(getString(R.string.no_classes));
                         nextClass.setText(l.name + " " + l.room + "\n" + Util.defaultValue(l.type, "") + " " + Util.defaultValue(l.teacher, ""));
                         time.setTime(dateStart);
                     }
                 }
             }
             handler.sendMessageAtTime(new Message(),time.getTimeInMillis());
         }
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         {
             String[] specialRoles = getResources().getStringArray(R.array.specialRoles);
             String userRole = new Preferences(this).getUserRole();
             for (String role : specialRoles) {
                 if (role.equals(userRole)) {
                     MenuItem menuItem = menu.add(R.string.group_message);
                     menuItem.setIcon(R.drawable.ic_message);
                     menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                     menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                         @Override
                         public boolean onMenuItemClick(MenuItem item) {
                             startActivity(new Intent(MainActivity.this, MessageActivity.class));
                             return true;
                         }
                     });
                     break;
                 }
             }
         }
 
         {
             MenuItem menuItem = menu.add(R.string.refresh);
             menuItem.setIcon(R.drawable.ic_reload);
             menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
             menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                 @Override
                 public boolean onMenuItemClick(MenuItem item) {
                     new SynchronizationTask(MainActivity.this, true).execute();
                     return true;
                 }
             });
         }
 
         {
             MenuItem menuItem = menu.add(R.string.preferences);
             menuItem.setIcon(R.drawable.ic_settings);
             menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
             menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                 @Override
                 public boolean onMenuItemClick(MenuItem item) {
                     startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                     return true;
                 }
             });
         }
         return true;
     }
 
     @InjectView(R.id.indicator)
     private TitlePageIndicator indicator;
 
     @InjectView(R.id.pager)
     private ViewPager viewPager;
 
    @InjectView(R.id.current_class)
     private TextView currentClass;
 
    @InjectView(R.id.next_class)
     private TextView nextClass;
 }
