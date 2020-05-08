 package info.ohgita.bincalc_android;
 
 import info.ohgita.bincalc_android.calculator.HistoryItem;
 
 import com.actionbarsherlock.R;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.os.Build;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 
 public class Adapter_BaseinputsViewPager extends PagerAdapter {
 	public LayoutInflater inflater;
 	public Context context;
 	public Fragment_main mainFragment;
 	private TableLayout tv;
 	private LinearLayout ll;
 	private static int currentPage;
 	
 	final static int ID_BASETYPE_BIN =	100;
 	final static int ID_BASETYPE_DEC =	200;
 	final static int ID_BASETYPE_HEX =	300;
 	
 	public Adapter_BaseinputsViewPager(Context c, Fragment_main fragment) {
 		super();
 		context = c;
 		mainFragment = fragment;
 		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		currentPage = 0;
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public Object instantiateItem(ViewGroup container, int position) {
 		Log.d("binCalc","instantiateItem(container, "+position+")");
 		
 		if(position == 0 && currentPage !=0 ){
 			currentPage--;
 		}else if(position == 0 && currentPage == 0){
 			currentPage = 0;
 		}else{
 			currentPage++;
 		}
 		Log.d("binCalc","currentPage = "+currentPage);
 		
 		/* LinearLayout inflating */
 		ll = (LinearLayout)inflater.inflate(R.layout.page_baseinputs, null);
 		
 		/* LinearLayout into container */
 		container.addView(ll);
 		
 		tv = (TableLayout) ll.findViewById(R.id.tableLayout_baseinputs);
 		
 		/* Set event-handler to Base-input EditText */
 		EditText et_bin = (EditText) tv.findViewById(R.id.editText_baseinput_bin);
 		EditText et_dec = (EditText) tv.findViewById(R.id.editText_baseinput_dec);
 		EditText et_hex = (EditText) tv.findViewById(R.id.editText_baseinput_hex);
 		et_bin.setOnFocusChangeListener(new OnFocusChangeListener(){
 			@Override
 		    public void onFocusChange(View v, boolean isFocus) {
 		        if(isFocus)
 		        	mainFragment.switchBasetype(Fragment_main.ID_BASETYPE_BIN);
 		    }
 		});
 		et_dec.setOnFocusChangeListener(new OnFocusChangeListener(){
 			@Override
 		    public void onFocusChange(View v, boolean isFocus) {
 		        if(isFocus)
 		        	mainFragment.switchBasetype(Fragment_main.ID_BASETYPE_DEC);
 		    }
 		});
 		et_hex.setOnFocusChangeListener(new OnFocusChangeListener(){
 			@Override
 		    public void onFocusChange(View v, boolean isFocus) {
 		        if(isFocus)
 		        	mainFragment.switchBasetype(Fragment_main.ID_BASETYPE_HEX);
 		    }
 		});
 		
 		/* Hide a on-screen keyboard on Base-input EditText */
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 	    	et_bin.setTextIsSelectable(true);
 	    	et_dec.setTextIsSelectable(true);
 	    	et_hex.setTextIsSelectable(true);
 		} else {
 		    et_bin.setInputType(0);
 		    et_dec.setInputType(0);
 		    et_hex.setInputType(0);
 		}
 		
 		/* Load history */
 		Log.d("binCalc","History num = "+ mainFragment.calc.histories.size());
 		if(currentPage < mainFragment.calc.histories.size()){
 			Log.d("binCalc","Load History..."+ currentPage);
 			HistoryItem history = (HistoryItem) mainFragment.calc.histories.get(currentPage);
 			Log.d("binCalc","  history.value = " + history.value);
 			Log.d("binCalc","  history.basetype = " + history.basetype);
 			mainFragment.selectedBasetypeId = history.basetype;
 			switch (history.basetype){
 				case Adapter_BaseinputsViewPager.ID_BASETYPE_BIN:
 					et_bin.setText(history.value);
 					break;
 				case Adapter_BaseinputsViewPager.ID_BASETYPE_DEC:
 					et_dec.setText(history.value);
 					break;
 				case Adapter_BaseinputsViewPager.ID_BASETYPE_HEX:
 					et_hex.setText(history.value);
 					break;
 			};
 			mainFragment.switchBasetype(history.basetype);
 			mainFragment.baseConvert();
 			Log.d("binCalc","Load History done."+ currentPage);
 		}else{
			mainFragment.baseinputsViewPager_LinearLayout = ll;
 			mainFragment.init();
 		}
 		
 		Log.i("binCalc", "ViewPager instantiateItem complete.");
 		return ll;
     }
 	
 	@Override
 	public int getItemPosition(Object object) {
 		return POSITION_NONE;
 	}
 	
 	@Override
     public void destroyItem(ViewGroup container, int position, Object object) {
         ((ViewPager)container).removeView((View)object);
     }
 	
 	@Override
 	public int getCount() {
 		return mainFragment.calc.histories.size() + 1;
 	}
 
 	@Override
 	public boolean isViewFromObject(View view, Object obj) {
 		return view.equals(obj);
 	}
 }
