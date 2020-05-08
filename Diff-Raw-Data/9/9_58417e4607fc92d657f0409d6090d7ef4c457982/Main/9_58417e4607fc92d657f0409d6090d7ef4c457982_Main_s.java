 package soofw.trk;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.Transformation;
 import android.view.HapticFeedbackConstants;
 import android.view.inputmethod.EditorInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.Calendar;
 import soofw.util.FlowLayout;
 import soofw.util.SpaceTokenizer;
 
 public class Main extends FragmentActivity {
 	Trk app = null;
 
	DrawerLayout drawerLayout = null;
 	LayoutInflater inflater = null;
 	FlowLayout filterLayout = null;
 	ListView drawer = null;
 	ListView taskView = null;
 	MultiAutoCompleteTextView omnibar = null;
 
 	ArrayAdapter<String> autoCompleteAdapter = null;
 	TagAdapter tagAdapter = null;
 	TaskAdapter taskAdapter = null;
 	TaskList list = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		app = (Trk)getApplicationContext();
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
 		drawer = (ListView)findViewById(R.id.drawer);
 		filterLayout = (FlowLayout)findViewById(R.id.filter_layout);
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		omnibar = (MultiAutoCompleteTextView)findViewById(R.id.omnibar);
 		taskView = (ListView)findViewById(R.id.task_view);
 
 		filterLayout.setVisibility(View.GONE);
 
 		this.list = new TaskList(this.app.listFile);
 		this.list.read();
 
 		taskAdapter = new TaskAdapter(this, this.list.filterList);
 		taskView.setAdapter(taskAdapter);
 		taskView.setItemsCanFocus(false);
 		taskView.setLongClickable(false);
 
 		tagAdapter = new TagAdapter(this, this.list);
 		drawer.setAdapter(tagAdapter);
 		drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				boolean checked = ((ListView)parent).isItemChecked(position);
 				if(checked) {
 					Main.this.addFilter(view);
 				} else {
 					Main.this.removeFilter(view);
 				}
 			}
 		});
 
 
 		autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.list.complexTagList);
 		omnibar.setAdapter(autoCompleteAdapter);
 		omnibar.setTokenizer(new SpaceTokenizer());
 		omnibar.setThreshold(1);
 		omnibar.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				notifyAdapters();
 			}
 			@Override public void afterTextChanged(Editable s) {}
 			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 		});
 		omnibar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_SEND) {
 					addItem();
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	void updateFilters() {
 		if(this.list.tagFilters.size() == 0) {
 			filterLayout.setVisibility(View.GONE);
 			return;
 		}
 		filterLayout.setVisibility(View.VISIBLE);
 
 		Calendar now = Calendar.getInstance();
 		Calendar tomorrow = Calendar.getInstance();
 		tomorrow.add(Calendar.DATE, 1);
 
 		for(int i = 0; i < this.list.tagFilters.size() || i < filterLayout.getChildCount(); i++) {
 			if(i < this.list.tagFilters.size()) {
 				TextView tag;
 				if(i < filterLayout.getChildCount()) {
 					tag = (TextView)(filterLayout.getChildAt(i));
 					tag.setVisibility(View.VISIBLE);
 				} else {
 					tag = (TextView)this.inflater.inflate(R.layout.filter_item, filterLayout, false);
 					filterLayout.addView(tag);
 				}
 
 				tag.setText(this.list.tagFilters.get(i));
 
 				int bg_id = 0;
 				switch(this.list.tagFilters.get(i).charAt(0)) {
 					case '+':
 						bg_id = R.color.plus_bg;
 						break;
 					case '@':
 						bg_id = R.color.at_bg;
 						break;
 					case '#':
 						bg_id = R.color.hash_bg;
 						break;
 					case '!':
 						if(this.list.tagFilters.get(i).equals("!0")) {
 							bg_id = R.color.lowpriority_bg;
 						} else {
 							bg_id = R.color.priority_bg;
 						}
 						break;
 					default:
 						Calendar c = Task.matcherToCalendar(Task.re_date.matcher(this.list.tagFilters.get(i)));
 						if(c.before(now)) {
 							bg_id = R.color.date_overdue_bg;
 						} else if(c.before(tomorrow)) {
 							bg_id = R.color.date_soon_bg;
 						} else {
 							bg_id = R.color.date_bg;
 						}
 				}
 
 				tag.setBackgroundColor(this.getResources().getColor(bg_id));
 			} else {
 				filterLayout.getChildAt(i).setVisibility(View.GONE);
 			}
 		}
 	}
 	void notifyAdapters() {
 		if(omnibar.getText().toString().isEmpty()) {
 			this.findViewById(R.id.omnibar_clear).setVisibility(View.GONE);
 		} else {
 			this.findViewById(R.id.omnibar_clear).setVisibility(View.VISIBLE);
 		}
 		this.list.filter(omnibar.getText().toString());
 		this.taskAdapter.notifyDataSetChanged();
 		this.tagAdapter.notifyDataSetChanged();
 
 		// apparently autoCompleteAdapter.notifyDataSetChanged()
 		// won't update a MultiAutoCompleteTextView list
 		this.autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.list.complexTagList);
 		this.omnibar.setAdapter(autoCompleteAdapter);
 	}
 
 	void addFilter(String filter) {
 		this.list.addTagFilter(filter);
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	public void addFilter(View view) {
 		this.list.addTagFilter(((TextView)view).getText().toString());
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	void removeFilter(String filter) {
 		this.list.removeTagFilter(filter);
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	public void removeFilter(View view) {
 		this.list.removeTagFilter(((TextView)view).getText().toString());
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 
 
 	public void addItem(View view) {
 		addItem();
 	}
 	void addItem() {
 		String source = omnibar.getText().toString();
 		if(!source.isEmpty()) {
 			this.list.add(source);
 			this.omnibar.setText("");
 			this.notifyAdapters();
 			this.list.write();
 		}
 	}
 
 	public void clearSearch(View view) {
 		this.omnibar.setText("");
 		this.notifyAdapters();
 	}
 
 	void editItem(Task source, String newSource) {
 		if(!newSource.isEmpty()) {
 			this.list.set(list.indexOf(source), newSource);
 			this.notifyAdapters();
 			this.list.write();
 		}
 	}
 
 	// Many thanks to http://stackoverflow.com/a/14306588
 	// and https://github.com/paraches/ListViewCellDeleteAnimation
 	int deletions = 0;
 	ArrayList<Task> deleteQueue = new ArrayList<Task>();
 	void deleteItem(final View view, final Task item) {
 		final int initialHeight = view.getMeasuredHeight();
 
 		AnimationListener al = new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation arg) {
 				deletions--;
 				if(deletions == 0) {
 					for(int i = 0; i < deleteQueue.size(); i++) {
 						Main.this.list.remove(deleteQueue.get(i));
 					}
 					deleteQueue.clear();
 					Main.this.list.write();
 					Main.this.notifyAdapters();
 					Main.this.taskView.setEnabled(true);
 				}
 			}
 			@Override public void onAnimationRepeat(Animation anim) {}
 			@Override public void onAnimationStart(Animation anim) {}
 		};
 
 		Animation anim = new Animation() {
 			@Override
 			protected void applyTransformation(float time, Transformation t) {
 				if(time == 1) {
 					if(view.getLayoutParams().height >= 0) {
 						view.setVisibility(View.GONE);
 						view.getLayoutParams().height = 1; // setting this to 0 breaks it...
 						view.requestLayout();
 					}
 				} else {
 					view.getLayoutParams().height = initialHeight - (int)(initialHeight * time);
 					view.requestLayout();
 				}
 			}
 			@Override
 			public boolean willChangeBounds() {
 				return true;
 			}
 		};
 
 		deleteQueue.add(item);
 		anim.setAnimationListener(al);
 		anim.setDuration(200);
 		view.startAnimation(anim);
 	}
 }
