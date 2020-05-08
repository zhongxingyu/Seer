 package com.isawabird;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SearchActivity extends Activity {
 
 	private ArrayList<Species> species = null;
 	private ArrayList<Species> speciesSubset = new ArrayList<Species>();
 
 	// listview section
 	private static StandardArrayAdapter arrayAdapter;
 	private SectionListAdapter sectionAdapter;
 	private SectionListView listView;
 
 	EditText search;
 
 	Typeface openSansLight;
 	Typeface openSansBold;
 	Typeface arvo;
 
 	// sideIndex
 	LinearLayout sideIndex;
 	// height of side index
 	private int sideIndexHeight;
 
 	// How many alphabets are there in the English language?
 	private int sideIndexSize = 26;
 
 	// list with items for side index
 	private ArrayList<Object[]> sideIndexList = new ArrayList<Object[]>();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search);
 		getActionBar().hide();
 
 		openSansLight = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
 		openSansBold = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Bold.ttf");
 		arvo = Typeface.createFromAsset(getAssets(), "fonts/Arvo-Regular.ttf");
 
 		search = (EditText) findViewById(R.id.search_query);
 		search.addTextChangedListener(filterTextWatcher);
 		listView = (SectionListView) findViewById(R.id.section_list_view);
 		sideIndex = (LinearLayout) findViewById(R.id.list_index);
 		sideIndex.setOnTouchListener(new Indextouch());
 
 		search.setTypeface(arvo);
 
 		species = Utils.getAllSpecies();
 		arrayAdapter = new StandardArrayAdapter(species);
 		sectionAdapter = new SectionListAdapter(this.getLayoutInflater(), arrayAdapter);
 		listView.setAdapter(sectionAdapter);
 		//PoplulateSideview();
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Species species = (Species) parent.getItemAtPosition(position);
 
 				// Jerry: Return to main intend after adding a bird
 				//Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
 				Intent mainIntent = getIntent();
 				mainIntent.putExtra(Consts.SPECIES_NAME, species.fullName);
 				setResult(14, mainIntent);
 				finish();
 			}
 		});
 	}
 
 	private class Indextouch implements OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 
 			if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
 				// now you know coordinates of touch
 				float sideIndexX = event.getX();
 				float sideIndexY = event.getY();
 
 				sideIndexHeight = sideIndex.getHeight();
 
 				if (sideIndexX > 0 && sideIndexY > 0) {
 					// and can display a proper item it country list
 					displayListItem(sideIndexY);
 				}
 			}
 			return true;
 		}
 	};
 
 	private class StandardArrayAdapter extends BaseAdapter implements Filterable {
 
 		private ArrayList<Species> items;
 		private SpeciesFilter speciesFilter;
 
 		public StandardArrayAdapter(ArrayList<Species> args) {
 			this.items = args;
 		}
 
 		@Override
 		public View getView(final int position, final View convertView, final ViewGroup parent) {
 			View view = convertView;
 			if (view == null) {
 				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = vi.inflate(R.layout.search_row, null);
 			}
 			TextView textView = (TextView) view.findViewById(R.id.row_title);
 			if (textView != null) {
 				textView.setText(items.get(position).fullName);
 				textView.setTypeface(arvo);
 			}
 			return view;
 		}
 
 		public int getCount() {
 			if(items == null) return 0;
 			return items.size();
 		}
 
 		public Filter getFilter() {
 			if(speciesFilter == null) {
 				speciesFilter = new SpeciesFilter();
 			}
 			return speciesFilter;
 		}
 
 		public Object getItem(int position) {
 			if(items == null) return null;
 			return items.get(position);
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 	}
 
 	public class SpeciesFilter extends Filter {
 
 		private FilterResults result = new FilterResults();
 
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 			// NOTE: this function is *always* called from a background thread,
 			// and not the UI thread.
 			constraint = search.getText().toString();
 
 			if (constraint != null && constraint.toString().length() > 0) {
 				// do not show side index while filter results
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						((LinearLayout) findViewById(R.id.list_index)).setVisibility(View.INVISIBLE);
 					}
 				});
 
 				ArrayList<Species> searchResult = Utils.search(constraint.toString(), speciesSubset);
 
 				result.count = searchResult.size();
 				result.values = searchResult;
 			} else {
 				speciesSubset.clear();
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						((LinearLayout) findViewById(R.id.list_index)).setVisibility(View.VISIBLE);
 					}
 				});
 				synchronized (this) {
 					result.count = species.size();
 					result.values = species;
 				}
 			}
 			return result;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint, FilterResults results) {
 			arrayAdapter.items = (ArrayList<Species>) results.values;
 			sectionAdapter = new SectionListAdapter(getLayoutInflater(), arrayAdapter);
 			listView.setAdapter(sectionAdapter);
 		}
 	}
 
 	private void displayListItem(float sideIndexY) {
 
 		// compute number of pixels for every side index item
 		double pixelPerIndexItem = (double) sideIndexHeight / sideIndexSize;
 
 		// compute the item index for given event position belongs to
 		int itemPosition = (int) (sideIndexY / pixelPerIndexItem);
 
 		if (itemPosition < sideIndexList.size()) {
 			// get the item (we can do it since we know item index)
 			Object[] indexItem = sideIndexList.get(itemPosition);
 			listView.setSelectionFromTop((Integer) indexItem[1], 0);
 		}
 	}
 
 	@SuppressLint("DefaultLocale")
 	private void PoplulateSideview() {
 
 		String latter_temp, latter = "";
 		int index = 0;
 		sideIndex.removeAllViews();
 		sideIndexList.clear();
 
 		for (int i = 0; i < species.size(); i++) {
 			Object[] temp = new Object[2];
 			latter_temp = (species.get(i).fullName).substring(0, 1).toUpperCase();
 			if (!latter_temp.equals(latter)) {
 				// latter with its array index
 				latter = latter_temp;
 				temp[0] = latter;
 				temp[1] = i + index;
 				index++;
 				sideIndexList.add(temp);
 
 				TextView latter_txt = new TextView(this);
 				latter_txt.setText(latter);
 
 				latter_txt.setSingleLine(true);
 				latter_txt.setHorizontallyScrolling(false);
 				latter_txt.setTypeface(null, Typeface.BOLD);
 				latter_txt.setTextSize(12);
 				latter_txt.setTextColor(getResources().getColor(R.color.color_50_transparent_black));
 				// latter_txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP,getResources().getDimension(R.dimen.index_list_font));
 				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
 				params.gravity = Gravity.CENTER_HORIZONTAL;
 
 				latter_txt.setLayoutParams(params);
 				latter_txt.setPadding(10, 0, 10, 0);
 				latter_txt.setTypeface(openSansLight);
 
 				sideIndex.addView(latter_txt);
 			}
 		}
 	}
 
 	private TextWatcher filterTextWatcher = new TextWatcher() {
 
 		public void afterTextChanged(Editable searchText) {
 			if(searchText.toString().length() < 2) return;
 			SearchActivity.arrayAdapter.getFilter().filter(searchText.toString());
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			// your search logic here
 		}
 	};
 }
