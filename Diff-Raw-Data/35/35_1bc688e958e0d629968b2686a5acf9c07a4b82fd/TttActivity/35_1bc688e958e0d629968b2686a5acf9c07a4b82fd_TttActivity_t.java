 package ceemos.ttt;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TabHost;
 
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import static ceemos.ttt.DatabaseHelper.*;
 
 public class TttActivity extends TabActivity {
 
     TabHost mTabHost;
     ListView tasklist;
     ListView timelist;
     ListView todolist;
     EditText editNewTask;
     
     private TaskDbAdapter taskHelper;
     private TodoDbAdapter todoHelper;
     private TimeDbAdapter timeHelper;
     
     private void fillData() {
         
         resetTimerButton();
         
         Cursor cursor;
         cursor = taskHelper.fetchAllTasks();
         startManagingCursor(cursor);
 
         String[] from = new String[]{KEY_LABEL,     KEY_COLOR,            KEY_ROWID};
         int[] to =         new int[]{R.id.tasktext, R.id.taskentrylayout, R.id.taskentrylayout};
 
         SimpleCursorAdapter tasks = 
                 new SimpleCursorAdapter(this, R.layout.taskentry, cursor, from, to);
         tasks.setViewBinder(new Binder());
         tasklist.setAdapter(tasks);
         
         cursor = todoHelper.fetchAllTodos();
         from = new String[]{KEY_LABEL,      KEY_NOTES,      KEY_ROWID};
         to =      new int[]{R.id.todolabel, R.id.todoextra, R.id.tododone};
         SimpleCursorAdapter todos = 
                 new SimpleCursorAdapter(this, R.layout.todoentry, cursor, from, to);
         todos.setViewBinder(new Binder());
         todolist.setAdapter(todos);
         
         cursor = timeHelper.fetchAllTimes();
         from = new String[]{KEY_LABEL,      KEY_COLOR,       KEY_TIME_SUM,  KEY_ROWID};
         to =      new int[]{R.id.timelabel, R.id.timelayout, R.id.timetime, R.id.timelayout};
         SimpleCursorAdapter times = 
                 new SimpleCursorAdapter(this, R.layout.timeentry, cursor, from, to);
         times.setViewBinder(new Binder());
         timelist.setAdapter(times);
         
     }
     
     class Binder implements SimpleCursorAdapter.ViewBinder {
 
         @Override
         public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (cursor.getColumnName(columnIndex).equals("_id")) {
                 int tid = cursor.getInt(columnIndex);
                 view.setTag(tid);
                if (view instanceof LinearLayout) {
                     LinearLayout ll = (LinearLayout) view;
                     for (View v : ll.getTouchables()) {
                         v.setTag(tid);
                     }
                    if (timerData != null) {
                         View autobutton = ll.findViewById(R.id.buttonauto);
                         if (autobutton != null) {
                            if (timerData.id == tid) {
                                setupTimerButton(timerData, (Button) autobutton);
                            } else {
                                resetButton((Button) autobutton);
                            }
                        } 
                     }
                 }
                 return true;
             } else if (cursor.getColumnName(columnIndex).equals(KEY_COLOR)) {
                 String color = cursor.getString(columnIndex);
                 int c = (Integer.parseInt(color) | 0xFF000000) & 0xFF7F7F7F;
                view.setBackgroundColor(c);
                 return true;
             }
             return false;
         }

     }
     
     private TabHost.OnTabChangeListener tablistener = new TabHost.OnTabChangeListener() {
 
         @Override
         public void onTabChanged(String tabId) {
             if("tab_time".equals(tabId)) {
                 fillData(); // Time-Tab heir nachladen, weil es nicht immer automatisch geschieht
             }
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         mTabHost = getTabHost();
 
         mTabHost.addTab(mTabHost.newTabSpec("tab_task").setIndicator("Tasks").setContent(R.id.listViewTasks));
         mTabHost.addTab(mTabHost.newTabSpec("tab_time").setIndicator("Time").setContent(R.id.listViewTime));
         mTabHost.addTab(mTabHost.newTabSpec("tab_todo").setIndicator("ToDo").setContent(R.id.listViewTodo));
         mTabHost.setCurrentTab(0);
         
         mTabHost.setOnTabChangedListener(tablistener);
 
         tasklist = (ListView) findViewById(R.id.listViewTasks);
         View footer = inflater.inflate(R.layout.taskadd, null);
         tasklist.addFooterView(footer);
         EditText taskeditnew = (EditText) footer.findViewById(R.id.taskeditnew);
         taskeditnew.setImeActionLabel("New", EditorInfo.IME_ACTION_DONE);
         taskeditnew.setOnEditorActionListener(new OnEditorActionListener() {
 
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_DONE) {
                     onNewTask(null);
                     
                     return true;
                 }
                 return false;
             }
         });
         
         registerForContextMenu(tasklist);
         tasklist.setItemsCanFocus(true);
 
         editNewTask = (EditText) findViewById(R.id.taskeditnew);
 
         taskHelper = new TaskDbAdapter(this);
         taskHelper.open();
 
         todolist = (ListView) findViewById(R.id.listViewTodo);
        todolist.setItemsCanFocus(true);
         todoHelper = new TodoDbAdapter(this);
         todoHelper.open();
         
         timeHelper = new TimeDbAdapter(this);
         timeHelper.open();
         timeHelper.setTimeBase(0);
         
         timelist = (ListView) findViewById(R.id.listViewTime);
         
         fillData();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.timebasemenu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.tbDay:
 
                 break;
             case R.id.tb7Days:
 
                 break;
             case R.id.tbWeek:
 
                 break;
             case R.id.tbEver:
 
                 break;
         }
         return true;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         if (v.getId() == R.id.listViewTasks) {
             AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
             MenuItem m = menu.add("Remove");
         }
         super.onCreateContextMenu(menu, v, menuInfo);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         if (item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
             AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
             if (acmi.targetView.getId() == R.id.taskentrylayout) {
                 int id = (Integer) acmi.targetView.getTag();
                 taskHelper.deleteTask(id);
                 fillData();
                 return true;
             }
         }
         return super.onContextItemSelected(item);
     }
     
   
     public void onTodoDone(View v) {
         final Button b = (Button) v;
         int id = (Integer) b.getTag();
         todoHelper.deleteTodo(id);
         fillData();
 
     }
 
     public void onNewTask(View v) {
         String label = editNewTask.getText().toString();
         long id = taskHelper.createTask(label, "" + label.hashCode());
         fillData();
     }
     
     public void onTaskTodo(View v) {
         Button b = (Button) v;
         final int id = (Integer) b.getTag();
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
         alert.setTitle("Extra");
         alert.setMessage("Enter Notes");
 
         // Set an EditText view to get user input 
         final EditText input = new EditText(this);
         alert.setView(input);
 
         alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 String value = input.getText().toString();
                 todoHelper.createTodo(value, id);
                 taskHelper.incPriority(id);
                 fillData();
                 mTabHost.setCurrentTabByTag("tab_todo");
             }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 // Canceled.
             }
         });
 
         alert.show();
     }
     
     Handler handler = new Handler();
     Runnable timerTask = new Runnable() {
 
         @Override
         public void run() {
             long diff = System.currentTimeMillis() - timerData.t_0;
             int value = Math.round(diff / 60000.0f);
             timerData.button.setText("" + value);
             if (value < 0) {
                 timerData.button.setTextColor(0xFFFF0000);
             } else {
                 timerData.button.setTextColor(0xFF00FF00);
             }
             handler.postDelayed(this, 60000);
         }
     };
 
     private class TimerData {
 
         long t_0;
         int id;
         Button button;
         
     }
     
     TimerData timerData = null;
     
     private void resetTimerButton() {
         if(timerData != null) {
             handler.removeCallbacks(timerTask);
            resetButton(timerData.button);
         }
     }
     
    private void resetButton(Button b) {
        timerData.button.setText("Start");
        timerData.button.setTextColor(0xFF000000);
    }

     private void startTimerButton(float min, View v){
         resetTimerButton();
         TimerData td = new TimerData();
         td.t_0 = System.currentTimeMillis() + ((int) min * 60000);
         View parent = (View) v.getParent();
         setupTimerButton(td, (Button) parent.findViewById(R.id.buttonauto)); 
         taskHelper.incPriority(td.id);
     }
     
     private void setupTimerButton(TimerData td, Button b) {
         td.button = b;
         td.id = (Integer) td.button.getTag();
         timerData = td;
         handler.postDelayed(timerTask, 100);
     }
     
     private void commitTime(float min, int id){
         System.out.println("Commiting Time: " + min + "min, tid: " + id);
         timeHelper.createTime(System.currentTimeMillis(), Math.round(min), id);
     }
 
     public void onTask5min(View v) {
         commitTime(5.0f, (Integer) v.getTag());
         startTimerButton(5.0f, v);
         
     }
 
     public void onTask10min(View v) {
         commitTime(10.0f, (Integer) v.getTag());
         startTimerButton(10.0f, v);
     }
 
     public void onTask30min(View v) {
         commitTime(30.0f, (Integer) v.getTag());
         startTimerButton(30.0f, v);
     }
 
     public void onTask60min(View v) {
         commitTime(60.0f, (Integer) v.getTag());
         startTimerButton(60.0f, v);
     }
 
     public void onTaskStart(View v) {
         if(timerData != null && timerData.id == v.getTag()) {
             long diff = System.currentTimeMillis() - timerData.t_0;
             int value = Math.round(diff / 60000.0f);
             timerData = null;
             commitTime(value, timerData.id);
             resetTimerButton();
         } else {
             startTimerButton(0.0f, v);
         }
     }
 }
