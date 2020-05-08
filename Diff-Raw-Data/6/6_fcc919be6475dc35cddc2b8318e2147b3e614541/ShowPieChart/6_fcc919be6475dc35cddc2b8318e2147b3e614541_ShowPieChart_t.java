 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.SeriesSelection;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class ShowPieChart extends Activity {
 	  public static final String TYPE = "type";
 
 	  private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.RED, Color.YELLOW};
 
 	  private CategorySeries mSeries = new CategorySeries("");
 
 	  private DefaultRenderer mRenderer = new DefaultRenderer();
 
 	  private String mDateFormat;
 
 	  private GraphicalView mChartView;
 	  
 	  private DataBaseWrapper m_database;
 	  
 	  String[] piechart_category_strings = {"Automotive", "Grocery", "Alcohol", "Restaurants", "Entertainment"};
 	  double[] piechart_amounts = {25.5, 17.6, 42.69, 15.36, 10.33};
 
 	  @Override
 	  public boolean onCreateOptionsMenu(Menu menu)
 	  {
 		  MenuInflater inflater = getMenuInflater();
 		  inflater.inflate(R.menu.menu, menu);
 		  return true;
 	  }
 		
 	  public boolean onOptionsItemSelected(MenuItem item)
 	  {
 		  Intent i;
 		  switch(item.getItemId())
 		  {
 		  		case R.id.home:    			
 		  			i = new Intent(this, MoneySaverActivity.class);
 		  			startActivity(i);
 		  			finish();
 	    			return true;
 	    		case R.id.tran:
 	    			i = new Intent(this, MoneySaverActivity.class);
 	    			i.putExtra(MoneySaverActivity.TRANS_NEW, true);
 	    			startActivity(i);
 	    			finish();
 	    			return true;
 	    		case R.id.pie:
 	    			return true;
 	    		case R.id.settings:
 	    			i = new Intent(this, Settings.class);
 	    			startActivity(i);
 	    			finish();
 	    			return true;
 	    		case R.id.help:
 	    			i = new Intent(this, Help.class);
 	    			startActivity(i);
 	    			finish();
 	    			return true;    			
 	    		default:
 	    			return true;	
 	    	}
 		}
 
 	  @Override
 	  protected void onRestoreInstanceState(Bundle savedState) {
 	    super.onRestoreInstanceState(savedState);
 	    mSeries = (CategorySeries) savedState.getSerializable("current_series");
 	    mRenderer = (DefaultRenderer) savedState.getSerializable("current_renderer");
 	    mDateFormat = savedState.getString("date_format");
 	  }
 
 	  @Override
 	  protected void onSaveInstanceState(Bundle outState) {
 	    super.onSaveInstanceState(outState);
 	    outState.putSerializable("current_series", mSeries);
 	    outState.putSerializable("current_renderer", mRenderer);
 	    outState.putString("date_format", mDateFormat);
 	  }
 
 	  @Override
 	  protected void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.graph_layout);
 
 	    ActionBar actionbar = getActionBar();
         actionbar.setDisplayShowHomeEnabled(false);
         actionbar.setDisplayShowTitleEnabled(false);
 	    
 	    mRenderer.setApplyBackgroundColor(true);
 	    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
 	    mRenderer.setChartTitleTextSize(20);
 	    mRenderer.setLabelsTextSize(15);
 	    mRenderer.setLegendTextSize(15);
 	    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
 	    mRenderer.setZoomButtonsVisible(true);
 	    mRenderer.setStartAngle(90);
 
         //connect to database
         m_database = new DataBaseWrapper(getApplicationContext());
         List<Category> category_list = m_database.getAllCategories();
         List<Transaction> trans_list = m_database.getAllTransactions();
         
         int num_categories = category_list.size();
         int list_size = trans_list.size();
 
         String[] categories = new String[num_categories-1];
         
        int cat_index = 0;
        for(int index = 0; index < num_categories-1;index++) {
         	if(category_list.get(index).getCategory().equals("Deposit") == false)
        		categories[cat_index++] = category_list.get(index).getCategory();
         }
         
         int totals_for_categories[] = new int[num_categories-1];
         
         for(int i = 0; i<list_size;i++) {
         	Transaction current_trans = trans_list.get(i);
         	String current_cat = current_trans.getCategory();
         	float current_amount = current_trans.getValue();
         	for(int j = 0; j<num_categories-1; j++) {
         		if(categories[j].equals(current_cat) == true)
         		{
         			totals_for_categories[j] += Math.abs(current_amount);
         			break;
         		}
         	}
         }
 	    
 	    for(int i = 0; i<categories.length;i++)
 	    {
 	    	if(totals_for_categories[i] != 0) {
 	    		mSeries.add(categories[i], totals_for_categories[i]);
 	    		SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
 	    		renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
 	    		mRenderer.addSeriesRenderer(renderer);
 	    		if (mChartView != null) {
 	    			mChartView.repaint();
 	    		}
 	    	}
 	    }
 	  }
 
 	  @Override
 	  protected void onResume() {
 	    super.onResume();
 	    if (mChartView == null) {
 	      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
 	      mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
 	      mRenderer.setSelectableBuffer(10);
 	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
 	          LayoutParams.FILL_PARENT));
 	    } else {
 	      mChartView.repaint();
 	    }
 	  }
 }
