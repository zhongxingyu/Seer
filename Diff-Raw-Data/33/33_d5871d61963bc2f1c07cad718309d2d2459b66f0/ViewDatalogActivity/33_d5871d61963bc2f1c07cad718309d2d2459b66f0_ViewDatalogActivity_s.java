 package uk.org.smithfamily.mslogger.activity;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.regex.Pattern;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.chart.ChartFactory;
 import uk.org.smithfamily.mslogger.chart.GraphicalView;
 import uk.org.smithfamily.mslogger.chart.chart.PointStyle;
 import uk.org.smithfamily.mslogger.chart.model.TimeSeries;
 import uk.org.smithfamily.mslogger.chart.model.XYMultipleSeriesDataset;
 import uk.org.smithfamily.mslogger.chart.renderer.XYMultipleSeriesRenderer;
 import uk.org.smithfamily.mslogger.chart.renderer.XYSeriesRenderer;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import uk.org.smithfamily.mslogger.widgets.ScatterPlotZAxisGradient;
 import uk.org.smithfamily.mslogger.widgets.ZAxisGradient;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 /**
  * Activity used to display a datalog file in a graph format to the user
  */
 public class ViewDatalogActivity extends Activity
 {
     private GraphicalView mChartView;
     private GraphicalView mChartScatterPlotView;
     private readLogFileInBackground mReadlogAsync;
 
     private String[] headers;
     private String[] completeHeaders;
     private List<List<Double>> data;
     private List<List<Double>> dataScatterPlot;
     
     private TabHost tabHost;
     private Spinner xAxisSpinner;
     private Spinner yAxisSpinner;
     private Spinner zAxisSpinner;
     private Button btGenerate;
     
     private ScatterPlotZAxisGradient scatterPlotZAxisGradient;
     
     private Button selectDatalogFields;
     
     public static final int BACK_FROM_DATALOG_FIELDS = 1;
     
     /**
      * On creation of the activity, we bind click events and launch the datalog reading function in a different thread
      */
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.viewdatalog);
          
         setTitle(R.string.datalog_viewer_title);
         
         tabHost = (TabHost) findViewById(R.id.tabhost);
         tabHost.setup();
 
         Resources res = getResources(); 
         
         TabSpec tabSpecLogViewer = tabHost.newTabSpec(getString(R.string.log_viewer));
         tabSpecLogViewer.setContent(R.id.regular_datalog);
         tabSpecLogViewer.setIndicator(getString(R.string.log_viewer),res.getDrawable(R.drawable.logviewer));
 
         TabSpec tabSpecScatterPlot = tabHost.newTabSpec(getString(R.string.scatter_plot));
         tabSpecScatterPlot.setIndicator(getString(R.string.scatter_plot),res.getDrawable(R.drawable.scatterplot));
         tabSpecScatterPlot.setContent(R.id.scatter_plot);
         
         tabHost.addTab(tabSpecLogViewer);
         tabHost.addTab(tabSpecScatterPlot);
         
         tabHost.setOnTabChangedListener(new OnTabChangeListener()
         {
             @Override
             public void onTabChanged(String tabId)
             {
                 populateAxisSpinners();
             }
         });
         
         xAxisSpinner = (Spinner) findViewById(R.id.xAxisSpinner);
         yAxisSpinner = (Spinner) findViewById(R.id.yAxisSpinner);
         zAxisSpinner = (Spinner) findViewById(R.id.zAxisSpinner);
         
         scatterPlotZAxisGradient = (ScatterPlotZAxisGradient) findViewById(R.id.scatterPlotZAxisGradient);
         
         btGenerate = (Button) findViewById(R.id.btGenerate);
         btGenerate.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 mReadlogAsync = (readLogFileInBackground) new readLogFileInBackground().execute("scatterplot");                
             }            
         });
         
         selectDatalogFields = (Button) findViewById(R.id.select_datalog_fields);
         selectDatalogFields.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 Intent launchDatalogFields = new Intent(ViewDatalogActivity.this, DatalogFieldsActivity.class);
                 
                 Bundle b = new Bundle();
                 b.putStringArray("datalog_fields",completeHeaders);
                 launchDatalogFields.putExtras(b);
                 
                 startActivityForResult(launchDatalogFields,BACK_FROM_DATALOG_FIELDS);
             }
         });
         
         mReadlogAsync = (readLogFileInBackground) new readLogFileInBackground().execute("regular");
     }
     
     private void populateAxisSpinners()
     {        
         ArrayAdapter<String> datalogFieldsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, completeHeaders);
        
         // Specify the layout to use when the list of choices appears
         datalogFieldsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         xAxisSpinner.setAdapter(datalogFieldsArrayAdapter);
         yAxisSpinner.setAdapter(datalogFieldsArrayAdapter);
         zAxisSpinner.setAdapter(datalogFieldsArrayAdapter);
         
         int i = 0;
         
         while (i < xAxisSpinner.getAdapter().getCount())
         {
             if (datalogFieldsArrayAdapter.getItem(i).toString().equals("MAP"))
             {
                 xAxisSpinner.setSelection(i);
             }
             
             if (datalogFieldsArrayAdapter.getItem(i).toString().equals("RPM"))
             {
                 yAxisSpinner.setSelection(i);
             }
             
             if (datalogFieldsArrayAdapter.getItem(i).toString().equals("AFR"))
             {
                 zAxisSpinner.setSelection(i);
             }
             
             i++;
         }
     }
     
     /**
      * Method called when the datalog fields have changed to refresh the graph
      */
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if (resultCode == BACK_FROM_DATALOG_FIELDS)
         {
             LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
             layout.removeAllViews();
             
             mChartView = null;
 
             mReadlogAsync = (readLogFileInBackground) new readLogFileInBackground().execute("regular");
         }
     }
     
     /**
      * Read the datalog and fill up all the necessary variables to generate a scatter plot
      * 
      * @param datalog The datalog file name to read
      */
     private void readScatterPlotDatalog(String datalog)
     {
         int[] indexOfFieldsToKeep = new int[3];                
         
         indexOfFieldsToKeep[0] = xAxisSpinner.getSelectedItemPosition();
         indexOfFieldsToKeep[1] = yAxisSpinner.getSelectedItemPosition();
         indexOfFieldsToKeep[2] = zAxisSpinner.getSelectedItemPosition();
         
         dataScatterPlot = new ArrayList<List<Double>>();
         
         for (int i = 0; i < 3; i++)
         {                                            
             dataScatterPlot.add(new ArrayList<Double>());
         }
                         
         try
         {            
             InputStream instream = new FileInputStream(datalog);
 
             if (instream != null)
             {
                 try
                 {
                     // Prepare the file for reading
                     InputStreamReader inputreader = new InputStreamReader(instream);
                     BufferedReader buffreader = new BufferedReader(inputreader);
     
                     int nbLine = 0;
                     headers = new String[] {};
                     
                     String line;
                     String[] lineSplit;
                     
                     long timeStart = System.currentTimeMillis();
                     
                     File datalogFile = new File(datalog);
                     
                     double currentLength = 0;
                     double totalLength = datalogFile.length();
                     
                     // Read every line of the file into the line-variable, on line at the time
                     while ((line = buffreader.readLine()) != null)
                     {
                         if (nbLine > 1)
                         {                    
                             lineSplit = line.split("\t");    
 
                             // Skip MARK and empty line
                             if ((lineSplit[0].length() > 3 && lineSplit[0].substring(0,4).equals("MARK")) || lineSplit[0].equals(""))
                             {
                                 
                             }
                             else
                             {
                                 for (int i = 0; i < indexOfFieldsToKeep.length; i++)
                                 {
                                     double currentValue = 0;
                                     if (lineSplit.length > indexOfFieldsToKeep[i])
                                     {                                    
                                         currentValue = Double.parseDouble(lineSplit[indexOfFieldsToKeep[i]]);
                                     }
 
                                     dataScatterPlot.get(i).add(currentValue);
                                 }
                             }
                         }
                         
                         nbLine++;
                         
                         currentLength += line.length();
      
                         mReadlogAsync.doProgress((int) (currentLength * 100 / totalLength));
                     }
                     
                     buffreader.close();
                     
                     long timeEnd = System.currentTimeMillis();
                     
                     DebugLogManager.INSTANCE.log("Read datalog file in " + (timeEnd - timeStart) + " milliseconds",Log.DEBUG);
                 }
                 finally
                 {
                     instream.close();
                 }
             }
         }
         catch (FileNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         } 
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }                               
     }
     
     /**
      * Read the datalog and fill up all the necessary variables to generate a line chart
      * 
      * @param datalog The datalog file name to read
      */
     private void readRegularDatalog(String datalog)
     {        
         String fieldsToKeep[] = ApplicationSettings.INSTANCE.getDatalogFields();
         int indexOfFieldsToKeep[] = new int[fieldsToKeep.length];
         
         try
         {            
             InputStream instream = new FileInputStream(datalog);
 
             if (instream != null)
             {
                 try {
                     // Prepare the file for reading
                     InputStreamReader inputreader = new InputStreamReader(instream);
                     BufferedReader buffreader = new BufferedReader(inputreader);
     
                     int nbLine = 0;
                     headers = new String[] {};
                     data = new ArrayList<List<Double>>();
                     
                     // Initialise list
                     for (int i = 0; i < fieldsToKeep.length; i++)
                     {
                         data.add(new ArrayList<Double>());
                     }
                     
                     String line;
                     String[] lineSplit;
                     
                     long timeStart = System.currentTimeMillis();
                     
                     File datalogFile = new File(datalog);
                     
                     double currentLength = 0;
                     double totalLength = datalogFile.length();
                     
                     Pattern pattern = Pattern.compile("\t");
                     
                     // Read every line of the file into the line-variable, on line at the time
                     while ((line = buffreader.readLine()) != null)
                     {
                         if (nbLine > 0)
                         {                    
                             lineSplit = pattern.split(line);    
                             
                             if (nbLine == 1) 
                             {
                                 headers = lineSplit;
                                 int k = 0;
                                 
                                 for (int i = 0; i < headers.length; i++)
                                 {
                                     for (int j = 0; j < fieldsToKeep.length; j++)
                                     {
                                         if (headers[i].equals(fieldsToKeep[j])) 
                                         {
                                             indexOfFieldsToKeep[k++] = i;
                                         }
                                     }
                                 }
                                 
                                 completeHeaders = headers;
                                 headers = fieldsToKeep;
                             }
                             else
                             {     
                                 // Skip MARK and empty line
                                 if ((lineSplit[0].length() > 3 && lineSplit[0].substring(0,4).equals("MARK")) || lineSplit[0].equals(""))
                                 {
                                     
                                 }
                                 else
                                 {
                                     for (int i = 0; i < indexOfFieldsToKeep.length; i++)
                                     {
                                         double currentValue = 0;
                                         if (lineSplit.length > indexOfFieldsToKeep[i])
                                         {                                    
                                             currentValue = Double.parseDouble(lineSplit[indexOfFieldsToKeep[i]]);
                                         }
 
                                         data.get(i).add(currentValue);
                                     }
                                 }
                             }
                         }
                         
                         nbLine++;
                         
                         currentLength += line.length();
      
                         mReadlogAsync.doProgress((int) (currentLength * 100 / totalLength));
                     }
                     
                     buffreader.close();
                     
                     long timeEnd = System.currentTimeMillis();
                     
                     DebugLogManager.INSTANCE.log("Read datalog file in " + (timeEnd - timeStart) + " milliseconds",Log.DEBUG);
                 }
                 finally {
                     instream.close();
                 }
             }
         }
         catch (FileNotFoundException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         } 
         catch (IOException e)
         {
             DebugLogManager.INSTANCE.logException(e);
         }      
     }
     
     /**
      * Generate graph for the selected regular datalog
      */
     private void generateRegularGraph()
     {        
         double minXaxis = 0;
         double maxXaxis = data.get(0).size();
         
         long timeStart = System.currentTimeMillis();
         
         // Assuming first column of datalog is time for X axis
         double[] xValues = new double[(int)maxXaxis];        
         for (int i = 0; i < maxXaxis; i++)
         {          
             xValues[i] = i;
         } 
         
         // Rebuild the headers array for title, we use them all but the first one (Time)
         String[] titles = new String[headers.length - 1];
         int[] colors = new int[headers.length - 1];
 
         Random rand = new Random();
                 
         for (int i = 1; i < headers.length; i++)
         {
             titles[i - 1] = headers[i];
             colors[i - 1] = Color.rgb(rand.nextInt(156) + 100, rand.nextInt(156) + 100, rand.nextInt(156) + 100);
         }
 
         List<double[]> x = new ArrayList<double[]>();
         List<double[]> values = new ArrayList<double[]>();
              
         // Add X values for all titles
         for (int i = 0; i < titles.length; i++)
         { 
             x.add(xValues);
         }
         
         List<Double> minColumns = new ArrayList<Double>();
         List<Double> maxColumns = new ArrayList<Double>();
         
         // Find min and max value for each columns
         for (int i = 1; i < data.size(); i++) 
         {
             List<Double> row = data.get(i);
             
             double min = row.get(0); 
             double max = min;
             for (int j = 0; j < row.size(); j++)
             {
                 double value = row.get(j);
                 
                 if (min > value)
                 {
                     min = value;
                 } 
                 
                 if (max < value)
                 {
                     max = value;
                 }
             }
             
             minColumns.add(min);
             maxColumns.add(max);
         }
         
         long timeEnd = System.currentTimeMillis();
         
         DebugLogManager.INSTANCE.log("Prepared value and found min/max value of each columns in " + (timeEnd - timeStart) + " milliseconds",Log.DEBUG);
         
         for (int i = 1; i < data.size(); i++)
         {
             List<Double> row = data.get(i);
             double[] rowDouble = new double[row.size()];
             for (int j = 0; j < row.size(); j++)
             {
                 rowDouble[j] = row.get(j);
                 
                 // Find percent between min and max
                 rowDouble[j] = (rowDouble[j] - minColumns.get(i - 1)) / (maxColumns.get(i - 1) - minColumns.get(i - 1)) * 100;
             }           
             
             values.add(rowDouble);
         }
                 
         XYMultipleSeriesRenderer renderer = buildRenderer(titles.length, colors);
         setChartSettings(renderer, "", "", "", minXaxis, Math.min(100,maxXaxis), 0, 100, Color.GRAY, Color.LTGRAY);
         
         renderer.setPanLimits(new double[] { minXaxis,maxXaxis,0,100 });
         renderer.setShowLabels(false);
         renderer.setClickEnabled(false);
         renderer.setShowGrid(true);
         renderer.setZoomEnabled(true);
                
         TextView currentlyViewing = (TextView) findViewById(R.id.currentlyViewing);
         
         Bundle b = getIntent().getExtras();
         String datalog = b.getString("datalog");
         
         currentlyViewing.setText("Currently viewing " + new File(datalog).getName());
         
         LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
 
         if (mChartView == null)
         {
             mChartView = ChartFactory.getLineChartView(ViewDatalogActivity.this, buildDateDataset(titles, x, values), renderer);     
             /*mChartView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                     if (seriesSelection == null)
                     {
                         System.out.println("Nothing was clicked");
                     }
                     else
                     {
                         System.out.println("Chart element data point index " + seriesSelection.getPointIndex() + " was clicked" + " point value=" + seriesSelection.getValue());
                     }
                 }
             });*/
             
             layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
         }
         else
         {
             mChartView.repaint();
         }
     }
 
     /**
      * Generate scatter plot for the current datalog
      */
     public void generateScatterPlot()
     {
         String[] titles = new String[dataScatterPlot.get(0).size()];
         int[] colors = new int[dataScatterPlot.get(0).size()];
 
         List<double[]> x = new ArrayList<double[]>();
         List<double[]> values = new ArrayList<double[]>();
 
         List<Double> xAxis = dataScatterPlot.get(0);
         List<Double> yAxis = dataScatterPlot.get(1);
         //List<Double> zAxis = dataScatterPlot.get(2);
         
         for (int i = 0; i < xAxis.size(); i++)
         {
             x.add(new double[] { xAxis.get(i) });
             values.add(new double[] { yAxis.get(i) });
         }
 
         List<Double> minColumns = new ArrayList<Double>();
         List<Double> maxColumns = new ArrayList<Double>();
         
         // Find min and max value for each columns
         for (int i = 0; i < dataScatterPlot.size(); i++) 
         {
             List<Double> row = dataScatterPlot.get(i);
             
             double min = row.get(0); 
             double max = min;
             for (int j = 0; j < row.size(); j++)
             {
                 double value = row.get(j);
                 
                 if (min > value)
                 {
                     min = value;
                 } 
                 
                 if (max < value)
                 {
                     max = value;
                 }
             }
             
             minColumns.add(min);
             maxColumns.add(max);
         }
         
         scatterPlotZAxisGradient.initWithMinMax(minColumns.get(2), maxColumns.get(2));
 
         ZAxisGradient color = new ZAxisGradient(minColumns.get(2), maxColumns.get(2));
         
         for (int i = 0; i < dataScatterPlot.get(0).size(); i++)
         {
             titles[i] = "";
             colors[i] = color.getColorForValue(dataScatterPlot.get(2).get(i));
         }
         
         String xAxisField = xAxisSpinner.getSelectedItem().toString();
         String yAxisField = yAxisSpinner.getSelectedItem().toString();
         
         String title = xAxisSpinner.getSelectedItem().toString() + " vs " + yAxisField;
 
         XYMultipleSeriesRenderer renderer = buildRenderer(titles.length, colors);
         setChartSettings(renderer, title, xAxisField, yAxisField, minColumns.get(0) - 10, maxColumns.get(0) + 10, minColumns.get(1) - 10, maxColumns.get(1) + 10, Color.GRAY, Color.LTGRAY);
         
         renderer.setXLabels(10);
         renderer.setYLabels(10);
         renderer.setShowLegend(false);
         
         int length = renderer.getSeriesRendererCount();
         for (int i = 0; i < length; i++) {
           ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
         }
 
         TextView chooseAxisAndGenerateText = (TextView) findViewById(R.id.chooseAxisAndGenerateText);
         chooseAxisAndGenerateText.setVisibility(View.GONE);
         
         LinearLayout scatterPlotBottom = (LinearLayout) findViewById(R.id.scatterPlotBottom);
         scatterPlotBottom.setVisibility(View.VISIBLE);
         
         LinearLayout layout = (LinearLayout) findViewById(R.id.chart_scatter_plot);
 
         if (mChartScatterPlotView == null)
         {
             mChartScatterPlotView = ChartFactory.getScatterChartView(ViewDatalogActivity.this, buildDateDataset(titles, x, values), renderer);                                 
             layout.addView(mChartScatterPlotView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
         }
         else
         {
             mChartScatterPlotView.repaint();
         }
     }
     
     /**
      * Builds an XY multiple time dataset using the provided values.
      * 
      * @param titles
      *            the series titles
      * @param xValues
      *            the values for the X axis
      * @param yValues
      *            the values for the Y axis
      * @return the XY multiple time dataset
      */
     protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<double[]> xValues, List<double[]> yValues)
     {
         XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
         
         int length = titles.length;
         for (int i = 0; i < length; i++)
         {
             TimeSeries series = new TimeSeries(titles[i]);
             double[] xV = xValues.get(i);
             double[] yV = yValues.get(i);
             int seriesLength = xV.length;
             for (int k = 0; k < seriesLength; k++)
             {
                 series.add(xV[k], yV[k]);
             }
             dataset.addSeries(series);
         }
         
         return dataset;
     }
 
     /**
      * Builds an XY multiple series renderer.
      * 
      * @param nbLines Number of lines
      * @param colors Array of colors for each point
      * @return The XY multiple series renderers
      */
     protected XYMultipleSeriesRenderer buildRenderer(int nbLines, int[] colors)
     {
         XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
         setRenderer(renderer, nbLines, colors);
         return renderer;
     }
 
     protected void setRenderer(XYMultipleSeriesRenderer renderer, int nbLines, int[] colors)
     {
         renderer.setAxisTitleTextSize(16);
         renderer.setChartTitleTextSize(20);
         renderer.setLabelsTextSize(15);
         renderer.setLegendTextSize(15);
         renderer.setPointSize(5f);
         renderer.setMargins(new int[] { 20, 30, 15, 20 });
 
         for (int i = 0; i < nbLines; i++)
         {
             XYSeriesRenderer r = new XYSeriesRenderer();
             r.setColor(colors[i]);
             r.setPointStyle(PointStyle.POINT);
             renderer.addSeriesRenderer(r);
         }
     }
 
     /**
      * Sets a few of the series renderer settings.
      * 
      * @param renderer
      *            the renderer to set the properties to
      * @param title
      *            the chart title
      * @param xTitle
      *            the title for the X axis
      * @param yTitle
      *            the title for the Y axis
      * @param xMin
      *            the minimum value on the X axis
      * @param xMax
      *            the maximum value on the X axis
      * @param yMin
      *            the minimum value on the Y axis
      * @param yMax
      *            the maximum value on the Y axis
      * @param axesColor
      *            the axes color
      * @param labelsColor
      *            the labels color
      */
     protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor)
     {
         renderer.setChartTitle(title);
         renderer.setXTitle(xTitle);
         renderer.setYTitle(yTitle);
         renderer.setXAxisMin(xMin);
         renderer.setXAxisMax(xMax);
         renderer.setYAxisMin(yMin);
         renderer.setYAxisMax(yMax);
         renderer.setAxesColor(axesColor);
         renderer.setLabelsColor(labelsColor);
     }
     
     /**
      * AsyncTask that is used to read datalog in a background task while the UI can keep updating
      */
     private class readLogFileInBackground extends AsyncTask<String, Integer, Void>
     {
         private ProgressDialog dialog = new ProgressDialog(ViewDatalogActivity.this);
         
         private long taskStartTime;
         private long lastRemainingUpdate;
         
         private String graphType = "";
         
         /**
          * This is executed before doInBackground
          */
         protected void onPreExecute()
         {
             taskStartTime = System.currentTimeMillis();
             
             dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             dialog.setProgress(0);
             dialog.setMessage("Reading datalog...");
             dialog.show();
             
             // Finish "View datalog" activity when canceling
             dialog.setOnCancelListener(new OnCancelListener()
             {
                 @Override
                 public void onCancel(DialogInterface dialog)
                 {
                    finish();
                 }
             });
         }
         
         /**
          * @param result This is executed after doInBackground and the result is returned in result
          */
         @Override
         protected void onPostExecute(Void result)
         {
             super.onPostExecute(result);
             
             if (graphType.equals("regular"))
             {
                 generateRegularGraph();
             }
             else if (graphType.equals("scatterplot"))
             {
                 generateScatterPlot();
             }
             
             dialog.dismiss();
         }
         
         /**
          * Called by the UI thread to update progress
          * @param value The new value of the progress bar 
          */
         public void doProgress(int value) 
         {
             publishProgress(value);
         }
         
         /**
          * @param value The new value of the progress bar
          */
         protected void onProgressUpdate(Integer...  value)
         {
            super.onProgressUpdate(value);
 
            long currentTime = System.currentTimeMillis();
            long elapsedMillis = (currentTime - taskStartTime);
            
            int percentValue = value[0];
 
            long totalMillis =  (long) (elapsedMillis / (((double) percentValue) / 100.0));
            long remainingMillis = totalMillis - elapsedMillis;
            int remainingSeconds = (int) remainingMillis / 1000;
            
            /*
                Update the status string. If task is less than 5% complete or started less then 2 seconds ago, 
                assume that the estimate is inaccurate
                
                Also, don't update more often then every second
            */
            if (percentValue >= 5 && elapsedMillis > 2000 && currentTime - lastRemainingUpdate > 1000)
            {
                dialog.setMessage("Reading datalog (About " + remainingSeconds + " second(s) remaining)...");
                
                lastRemainingUpdate = System.currentTimeMillis();
            }
            
            dialog.setProgress(percentValue);
         }
         
         /**
          * This is the main function that is executed in another thread 
          * 
          * @param params Parameters of the task
          */
         @Override
         protected Void doInBackground(String... params)
         {
             graphType = params[0];
             
             Bundle b = getIntent().getExtras();
             String datalog = b.getString("datalog");
            
             if (graphType.equals("regular"))
             {
                 readRegularDatalog(datalog);
             }
             else if (graphType.equals("scatterplot"))
             {
                 readScatterPlotDatalog(datalog);
             }
             
             return null;
         }
     }
 }
