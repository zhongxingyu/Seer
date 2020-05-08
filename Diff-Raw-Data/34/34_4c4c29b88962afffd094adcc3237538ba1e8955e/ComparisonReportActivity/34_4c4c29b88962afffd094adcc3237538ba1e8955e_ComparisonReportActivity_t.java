 package com.timetracker.ui.activities;
 
 import android.graphics.*;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.timetracker.R;
 import com.timetracker.domain.Task;
 import com.timetracker.domain.persistance.DatabaseHelper;
 import com.timetracker.util.EmptyListAdapter;
 import java.util.*;
 
 /**
  * Created by Anton Chernetskij
  */
 public class ComparisonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 
     private static final int BAR_HEIGHT = 90;
     private final ReportGenerator reportGenerator = new ReportGenerator();
 
     private List<AggregationPeriod> aggregationPeriods;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.comparison_report);
 
         initSlider();
         updatePlot(aggregationPeriods.get(0));
     }
 
     private void initSlider() {
         aggregationPeriods = new ArrayList<>();
         String[] labels = getResources().getStringArray(R.array.intervalChooserLabels);
         aggregationPeriods.add(new AggregationPeriod(labels[0], 1));
         aggregationPeriods.add(new AggregationPeriod(labels[1], 2));
         aggregationPeriods.add(new AggregationPeriod(labels[2], 3));
         aggregationPeriods.add(new AggregationPeriod(labels[3], 7));
         aggregationPeriods.add(new AggregationPeriod(labels[4], 30));
         aggregationPeriods.add(new AggregationPeriod(labels[5], 365));
 
         SeekBar intervalChooser = (SeekBar) findViewById(R.id.reportIntervalChooser);
         intervalChooser.setMax(aggregationPeriods.size() - 1);
         int aggregationIndex = intervalChooser.getProgress();
         updateAggregateLabel(aggregationIndex);
         intervalChooser.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 updateAggregateLabel(progress);
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
 
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
 
             }
         });
 
         intervalChooser.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 switch (event.getAction()) {
                     case MotionEvent.ACTION_DOWN:
                         ListView tasksList = (ListView) findViewById(R.id.tasks);
                         tasksList.setAdapter(new EmptyListAdapter());
                         //todo add some kind of progressbar
                         break;
                     case MotionEvent.ACTION_UP:
                         SeekBar intervalChooser = (SeekBar) findViewById(R.id.reportIntervalChooser);
                         int aggregationIndex = intervalChooser.getProgress();
                         updatePlot(aggregationPeriods.get(aggregationIndex));
                         break;
                 }
                 return false;
             }
         });
     }
 
     public void updateAggregateLabel(int n) {
         TextView chooserLabel = (TextView) findViewById(R.id.intervalChooserLabel);
         chooserLabel.setText(aggregationPeriods.get(n).label);
     }
 
     private class AggregationPeriod {
 
         int days;
         String label;
 
         private AggregationPeriod(String label, int days) {
             this.label = label;
             this.days = days;
         }
 
     }
 
     private void updatePlot(AggregationPeriod period) {
        Calendar calendar = Calendar.getInstance();
        Date to = calendar.getTime();
        calendar.add(Calendar.DATE, -(period.days - 1));
        Date from = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        from = calendar.getTime();

        List<ReportGenerator.AggregatedTaskItem> currentPeriodReport = reportGenerator.generateReport(from, to, this);

        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date prevPeriodTo = calendar.getTime();
        calendar.add(Calendar.DATE, -(period.days - 1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date prevPeriodFrom = calendar.getTime();
        List<ReportGenerator.AggregatedTaskItem> previousPeriodReport = reportGenerator.generateReport(prevPeriodFrom, prevPeriodTo, this);
 
         long[] previousPeriodTime = getCorrespondingTime(currentPeriodReport, previousPeriodReport);
         displayChart(currentPeriodReport, previousPeriodTime, period.label);
     }
 
     private long[] getCorrespondingTime(List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                                         List<ReportGenerator.AggregatedTaskItem> previousPeriodReport) {
         long[] result = new long[currentPeriodReport.size()];
         for (int i = 0; i < currentPeriodReport.size(); i++) {
             ReportGenerator.AggregatedTaskItem item = currentPeriodReport.get(i);
             for (ReportGenerator.AggregatedTaskItem correspondingItem : previousPeriodReport) {
                 if (item.task.id == correspondingItem.task.id) {
                     result[i] = correspondingItem.duration;
                     break;
                 }
             }
         }
         return result;
     }
 
     private void displayChart(final List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                               final long[] previousPeriodDurations, String periodLabel) {
         ListView tasksList = (ListView) findViewById(R.id.tasks);
         final long maxDuration = Math.max(currentPeriodReport.get(0).duration, previousPeriodDurations[0]);
 
         Display display = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
 
         final float width = size.x;
 
         tasksList.setAdapter(new BaseAdapter() {
             @Override
             public int getCount() {
                 return currentPeriodReport.size();
             }
 
             @Override
             public Object getItem(int position) {
                 return currentPeriodReport.get(position);
             }
 
             @Override
             public long getItemId(int position) {
                 return 0;
             }
 
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 Button view = new Button(ComparisonReportActivity.this);
                 view.setGravity(Gravity.LEFT);
                 view.setHeight(BAR_HEIGHT);
                 view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, BAR_HEIGHT));
 
                 ReportGenerator.AggregatedTaskItem currentPeriodItem = currentPeriodReport.get(position);
                 view.setText(currentPeriodItem.task.name + " " +
                         buildTime(currentPeriodItem.duration) + "/" +
                         buildTime(previousPeriodDurations[position]));
                 BitmapDrawable drawable = getBackgroundBar((int) width, BAR_HEIGHT, (double) currentPeriodItem.duration / maxDuration,
                         (double) previousPeriodDurations[position] / maxDuration, currentPeriodItem.task.color);
                 view.setBackground(drawable);
 
                 return view;
             }
         });
     }
 
     private BitmapDrawable getBackgroundBar(int width, int height, double currentAmount, double previousAmount, int color) {
         Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap);
         Paint p = new Paint();
         p.setColor(color);
         p.setAlpha(Task.DEFAULT_COLOR_ALPHA);
         canvas.drawRect(0, 0, (float) (currentAmount * width), height, p);
 
         float x = (float) (previousAmount * width);
         Paint p2 = new Paint();
         p2.setColor(Color.WHITE);
         p2.setAlpha(Task.DEFAULT_COLOR_ALPHA);
         p2.setStrokeWidth(4);
 
         canvas.drawLine(x, 0, x, height, p2);
 
         return new BitmapDrawable(getResources(), bitmap);
     }
 
     private String buildTime(long time) {
         time /= 1000;
         long s = time % 60;
         time /= 60;
         long m = time % 60;
         time /= 60;
         long h = time;
         return String.format("%d:%02d", h, m);
     }
 }
