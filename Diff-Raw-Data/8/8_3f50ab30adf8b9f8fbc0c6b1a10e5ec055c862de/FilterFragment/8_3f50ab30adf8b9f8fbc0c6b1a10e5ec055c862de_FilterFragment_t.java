 package eu.fiveminutes.cipele46.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.fiveminutes.cipele46.R;
 import eu.fiveminutes.cipele46.adapter.AdTypeAdapter;
 import eu.fiveminutes.cipele46.adapter.CategoryAdapter;
 import eu.fiveminutes.cipele46.adapter.DistrictAdapter;
 import eu.fiveminutes.cipele46.api.CategoriesListener;
 import eu.fiveminutes.cipele46.api.CipeleAPI;
 import eu.fiveminutes.cipele46.api.DistrictWithCitiesListener;
 import eu.fiveminutes.cipele46.app.Filters;
 import eu.fiveminutes.cipele46.model.AdType;
 import eu.fiveminutes.cipele46.model.Category;
 import eu.fiveminutes.cipele46.model.District;
 
 public class FilterFragment extends SherlockFragment implements OnItemSelectedListener {
 	
 	private Spinner typeSpinner;
 	private Spinner categorySpinner;
 	private Spinner districtSpinner;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.filter, container, false);
 		return v;
 	}
 	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		categorySpinner = (Spinner)view.findViewById(R.id.filter_category);
 		districtSpinner = (Spinner)view.findViewById(R.id.filter_county);
 		typeSpinner = (Spinner)view.findViewById(R.id.filter_type);
 		categorySpinner.setOnItemSelectedListener(this);
 		districtSpinner.setOnItemSelectedListener(this);
 		typeSpinner.setOnItemSelectedListener(this);
 		getData();
 	}
 	
 	private void getData() {
 		
 		CipeleAPI.get().getCategories(new CategoriesListener() {
 			
 			@Override
 			public void onSuccess(List<Category> categories) {
 				CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), categories);
 				categorySpinner.setAdapter(categoryAdapter);
 			}
 			
 			@Override
 			public void onFailure(Throwable t) {
				Toast.makeText(getActivity(), R.string.error_get_cat, Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		CipeleAPI.get().getDistrictWithCities(new DistrictWithCitiesListener() {
 			
 			@Override
 			public void onSuccess(List<District> districts) {
 				DistrictAdapter districtAdapter = new DistrictAdapter(getActivity(), districts);
 				districtSpinner.setAdapter(districtAdapter);
 			}
 			
 			@Override
 			public void onFailure(Throwable t) {
				Toast.makeText(getActivity(), R.string.error_get_dist, Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		List<String> adTypes = new ArrayList<String>();
 		adTypes.add(getString(R.string.filter_supply));
 		adTypes.add(getString(R.string.filter_demand));
 		AdTypeAdapter adTypeAdapter = new AdTypeAdapter(getActivity(), adTypes);
 		typeSpinner.setAdapter(adTypeAdapter);
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 		
 		if (arg0.getAdapter() instanceof CategoryAdapter) {
 			Filters.setCategoryFilter(getActivity(), arg0.getAdapter().getItemId(arg2));
 			Log.d(this.getClass().getSimpleName(), "Category adapter");
 		} else if (arg0.getAdapter() instanceof DistrictAdapter) {
 			Filters.setDistrictFilter(getActivity(), arg0.getAdapter().getItemId(arg2));
 			Log.d(this.getClass().getSimpleName(), "DistrictAdapter adapter");
 		} else if (arg0.getAdapter() instanceof AdTypeAdapter) {
 			Filters.setAdTypeFilter(getActivity(), AdType.values()[(int)arg0.getAdapter().getItemId(arg2)]);
 			Log.d(this.getClass().getSimpleName(), "AdTypeAdapter adapter");
 		} else {
 			Log.d(this.getClass().getSimpleName(), "Not recognized adapter");
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 	}
 	
 }
