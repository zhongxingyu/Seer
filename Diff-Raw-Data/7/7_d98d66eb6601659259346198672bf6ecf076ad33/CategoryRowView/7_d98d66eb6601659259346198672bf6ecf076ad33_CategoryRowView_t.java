 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.redgeek.android.eventrend.category;
 
 import net.redgeek.android.eventrend.InputActivity;
 import net.redgeek.android.eventrend.Preferences;
 import net.redgeek.android.eventrend.R;
 import net.redgeek.android.eventrend.backgroundtasks.AddEntryTask;
 import net.redgeek.android.eventrend.db.CategoryDbTable;
 import net.redgeek.android.eventrend.db.EntryDbTable;
 import net.redgeek.android.eventrend.db.EvenTrendDbAdapter;
 import net.redgeek.android.eventrend.primitives.TimeSeriesCollector;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.GUITask;
 import net.redgeek.android.eventrend.util.GUITaskQueue;
 import net.redgeek.android.eventrend.util.Number;
 import net.redgeek.android.eventrend.util.ProgressIndicator;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.LayoutAnimationController;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class CategoryRowView extends LinearLayout implements GUITask {
   // UI elements
   private CategoryRowView mRowView;
   private CategoryDbTable.Row mDbRow;
   private EditText mDefaultValue;
   private Button mPlusButton;
   private Button mMinusButton;
   private Button mAddButton;
   private Drawable mTrendIconDrawable;
   private ImageView mTrendIconImage;
   private TextView mCategoryNameView;
   private TextView mCategoryUpdateView;
   private TextView mTrendValueView;
   private ProgressIndicator.Titlebar mProgress;
 
   // Listeners
   private View.OnClickListener mPlusButtonListener;
   private View.OnClickListener mMinusButtonListener;
   private View.OnClickListener mAddListener;
 
   // Private data
   private TimeSeriesCollector mTSC;
   private int mColorInt;
   private long mTimestamp;
   private float mAddValue;
   private boolean mKeepCounting = false;
   
   // Prefs
   private int mDecimals;
   private int mHistory;
 
   private Context mCtx;
   private EvenTrendDbAdapter mDbh;
   private AddEntryTask mAddEntryTask;
   private boolean mSelectable = true;
 
   public CategoryRowView(Context context, CategoryRow viewRow,
       TimeSeriesCollector tsc) {
     super(context);
     mTSC = tsc;
     mCtx = context;
     mRowView = this;
     mDbh = ((InputActivity) mCtx).getDb();
     mDbRow = viewRow.getDbRow();
 
     mTimestamp = viewRow.getTimestamp();
 
     setupPrefs();
     setupTasks();
     setupUI();
     populateFields();
   }
 
   public boolean isSelectable() {
     return mSelectable;
   }
 
   public void setSelectable(boolean selectable) {
     mSelectable = selectable;
   }
 
   public CategoryDbTable.Row getDbRow() {
     return mDbRow;
   }
 
   public void executeNonGuiTask() throws Exception {
     mAddEntryTask.AddEntry(mDbRow, mTimestamp, mAddValue);
   }
 
   public void afterExecute() {
     String status;
     String toast;
 
     float newValue = mAddEntryTask.mLastAddValue;
     float oldValue = mAddEntryTask.mLastAddOldValue;
     long timestamp = mAddEntryTask.mLastAddTimestamp;
 
     if (mAddEntryTask.mLastAddUpdate == true) {
       ((InputActivity) mCtx).setLastAdd(mAddEntryTask.mLastAddId, oldValue,
           timestamp, mCategoryUpdateView, mRowView);
       status = "Update @ " + DateUtil.toShortTimestamp(timestamp) + ": "
           + oldValue + " -> " + newValue;
       mCategoryUpdateView.setText(status);
 //      toast = "Update @ " + DateUtil.toTimestamp(timestamp) + ": " + oldValue
 //          + " -> " + newValue;
 //      Toast.makeText(mCtx, mDbRow.getCategoryName() + ": " + toast,
 //          Toast.LENGTH_SHORT).show();
     } else {
       ((InputActivity) mCtx).setLastAdd(mAddEntryTask.mLastAddId, newValue,
           timestamp, mCategoryUpdateView, mRowView);
 
       status = "Add @ "
           + DateUtil.toShortTimestamp(mAddEntryTask.mLastAddTimestamp) + ": "
           + newValue;
       mCategoryUpdateView.setText(status);
 //      toast = "Add @ " + DateUtil.toTimestamp(mAddEntryTask.mLastAddTimestamp)
 //          + ": " + newValue;
 //      Toast.makeText(mCtx, mDbRow.getCategoryName() + ": " + toast,
 //          Toast.LENGTH_SHORT).show();
     }
 
     CategoryDbTable.Row cat = mDbh.fetchCategory(mDbRow.getId());
     updateTrendIcon(cat.getTrendState());
     float trendValue = Number.Round(cat.getLastTrend(), Preferences
         .getDecimalPlaces(mCtx));
     mTrendValueView.setText(Float.valueOf(trendValue).toString());
 
     mAddButton.setClickable(true);
     mAddButton.setTextColor(Color.BLACK);
 
     ((InputActivity) mCtx).redrawSyntheticViews();
   }
 
   public void onFailure(Throwable t) {
   }
 
   private void setupPrefs() {
     mDecimals = Preferences.getDecimalPlaces(mCtx);
     mHistory = Preferences.getHistory(mCtx);
   }
 
   private void setupTasks() {
     mAddEntryTask = new AddEntryTask(mTSC, mDecimals, mHistory);
   }
 
   private void setupUI() {
     this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
     this.setOrientation(VERTICAL);
     this.setLongClickable(true);
 
     setupListeners();
 
     LayoutInflater inflater = (LayoutInflater) mCtx
         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     inflater.inflate(R.layout.category_row, this);
 
     mCategoryNameView = (TextView) findViewById(R.id.category_name);
     mCategoryUpdateView = (TextView) findViewById(R.id.category_status);
 
     mMinusButton = (Button) findViewById(R.id.category_input_minus);
     mPlusButton = (Button) findViewById(R.id.category_input_plus);
     mDefaultValue = (EditText) findViewById(R.id.category_input_value);
     mAddButton = (Button) findViewById(R.id.category_input_add);
     mTrendIconImage = (ImageView) findViewById(R.id.category_input_trend_icon);
     mTrendValueView = (TextView) findViewById(R.id.category_input_trend_value);
 
     mPlusButton.setOnClickListener(mPlusButtonListener);
     mMinusButton.setOnClickListener(mMinusButtonListener);
     mAddButton.setOnClickListener(mAddListener);
 
     mProgress = new ProgressIndicator.Titlebar(mCtx);
 
   }
 
   private void setupListeners() {
     mPlusButtonListener = new OnClickListener() {
       public void onClick(View v) {
         float value = Float.valueOf(mDefaultValue.getText().toString())
             .floatValue();
         value += mDbRow.getIncrement();
         value = Number.Round(value, Preferences.getDecimalPlaces(mCtx));
         mDbRow.setDefaultValue(value);
         mDefaultValue.setText(Float.toString(value));
         mDbh.updateCategoryLastValue(mDbRow.getId(), value);
       }
     };
     
     mMinusButtonListener = new OnClickListener() {
       public void onClick(View v) {
         float value = Float.valueOf(mDefaultValue.getText().toString())
             .floatValue();
         value -= mDbRow.getIncrement();
         value = Number.Round(value, Preferences.getDecimalPlaces(mCtx));
         mDbRow.setDefaultValue(value);
         mDefaultValue.setText(Float.toString(value));
         mDbh.updateCategoryLastValue(mDbRow.getId(), value);
       }
     };
     
     mAddListener = new OnClickListener() {
       public void onClick(View v) {
         addEntry();
        float value;
        try {
          value = Float.valueOf(mDefaultValue.getText().toString()).floatValue();
        } catch(Exception e) {
          value = mDbRow.getDefaultValue();
        }
         mDbRow.setLastValue(value);
         mDbRow.setDefaultValue(value);
         mDbh.updateCategoryLastValue(mDbRow.getId(), value);
         mDefaultValue.setText(Float.valueOf(value).toString());
       }
     };
   }
 
   public void populateFields() {
     try {
       mColorInt = Color.parseColor(mDbRow.getColor());
     } catch (IllegalArgumentException e) {
       mColorInt = Color.WHITE;
     }
 
     mCategoryNameView.setText(mDbRow.getCategoryName());
     mCategoryNameView.setTextColor(mColorInt);
 
     if (mDbRow.getSynthetic() == false) {
       EntryDbTable.Row row = mDbh.fetchLastCategoryEntry(mDbRow.getId());
       if (row != null) {
         String str = DateUtil.toTimestamp(row.getTimestamp()) + ": "
             + Float.valueOf(row.getValue()).toString();
         mCategoryUpdateView.setText(str);
       }
 
       float inputValue = mDbRow.getLastValue();
       mDefaultValue.setText(Float.valueOf(inputValue).toString());
       float trendValue = Number.Round(mDbRow.getLastTrend(), Preferences
           .getDecimalPlaces(mCtx));
       mTrendValueView.setText(Float.valueOf(trendValue).toString());
 
       String trendState = mDbRow.getTrendState();
       updateTrendIcon(trendState);
     } else {
       mDbRow = mDbh.fetchCategory(mDbRow.getId());
 
       mMinusButton.setVisibility(View.INVISIBLE);
       mPlusButton.setVisibility(View.INVISIBLE);
       mDefaultValue.setVisibility(View.INVISIBLE);
       mAddButton.setVisibility(View.INVISIBLE);
 
       float trendValue = Number.Round(mDbRow.getLastTrend(), Preferences
           .getDecimalPlaces(mCtx));
       mTrendValueView.setText(Float.valueOf(trendValue).toString());
 
       String trendState = mDbRow.getTrendState();
       updateTrendIcon(trendState);
     }
   }
 
   public static void setLayoutAnimationSlideOutLeftIn(ViewGroup panel,
       Context ctx) {
     AnimationSet set = new AnimationSet(true);
     Animation animation;
 
     animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
         Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
         Animation.RELATIVE_TO_SELF, 0.0f);
     animation.setStartOffset(0);
     animation.setDuration(500);
     animation.setRepeatCount(1);
     animation.setRepeatMode(Animation.REVERSE);
     set.addAnimation(animation);
     set.setDuration(500);
 
     LayoutAnimationController controller = new LayoutAnimationController(set,
         0.25f);
     controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
     panel.setLayoutAnimation(controller);
   }
 
   public void updateTrendIcon(String trendState) {
     if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_45_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_45_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_45_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_45_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_30_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_30_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_30_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_30_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_15_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_15_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_15_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_15_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_45_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_45_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_45_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_45_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_30_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_30_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_30_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_30_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_15_BAD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_15_bad);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_15_GOOD))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_15_good);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_DOWN_15))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_down_15);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_UP_15))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_up_15);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_FLAT))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_flat);
     else if (trendState.equals(CategoryDbTable.KEY_TREND_FLAT_GOAL))
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_flat_goal_glow);    
     else
       mTrendIconDrawable = getResources().getDrawable(R.drawable.trend_unknown);
 
     mTrendIconImage.setImageDrawable(mTrendIconDrawable);
   }
 
   public void addEntry() {
     mTimestamp = ((InputActivity) mCtx).getTimestampMs();
     mAddValue = Float.valueOf(mDefaultValue.getText().toString()).floatValue();
     GUITaskQueue.getInstance().addTask(mProgress, this);
 
     mAddButton.setClickable(false);
     mAddButton.setTextColor(Color.LTGRAY);
 
     CategoryDbTable.Row cat = mDbh.fetchCategory(mDbRow.getId());
     updateTrendIcon(cat.getTrendState());
     setLayoutAnimationSlideOutLeftIn(mRowView, mCtx);
   }
 }
