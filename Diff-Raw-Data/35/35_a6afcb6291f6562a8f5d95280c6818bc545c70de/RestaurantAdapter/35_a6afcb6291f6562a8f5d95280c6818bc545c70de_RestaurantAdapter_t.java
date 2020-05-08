 package edu.vanderbilt.vuphone.android.objects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import edu.vanderbilt.vuphone.android.dining.R;
 import edu.vanderbilt.vuphone.android.storage.Restaurant;
 
 /**
  * @author austin
  *
  */
 /**
  * @author austin
  *
  */
 public class RestaurantAdapter extends BaseAdapter {
 
 	// unsorted, for when constructor is passed initial sort state
 	public static final int UNSORTED = 0;
 	
 	// alphabetical sort
 	public static final int ALPHABETICAL = 1;
 	
 	// secondary sorts, all include alphabetical
 	public static final int FAVORITE = 2;
 	public static final int OPEN_CLOSED = 3;
 	public static final int NEAR_FAR = 4;
 	
 	// tertiary sorts, all include alphabetical
 	public static final int FAVORITE_OPEN_CLOSED = 5;
 	
 	public static final int DEFAULT = FAVORITE_OPEN_CLOSED;
 	
 	// non restaurant item Ids, must be negative
 	public static final long FAVORITE_PARTITION = -1;
 	public static final long OPEN_PARTITION = -2;
 	public static final long CLOSED_PARTITION = -3;
 	public static final long OTHER_PARTITION = -4;
 	
 		
 	private Context _context;
 	
 	private ArrayList<Long> _order;
 	private boolean displayFav;
 	private boolean grayClosed; // currently always true
 	
 	private int currentSortType;
 	
 	// TODO: Implement partition 
 	public RestaurantAdapter(Context context) {
 		this(context, DEFAULT);
 	}
 	public RestaurantAdapter(Context context, int sortType) {
 		_context = context;
 		_order = Restaurant.getIDs();
 		setSort(sortType);
 	} 
 	public RestaurantAdapter(Context context, ArrayList<Long> sortOrder, boolean showFavIcon) {
 		_context = context;
 		_order = sortOrder;
 		displayFav = showFavIcon;
 	}
 	
 	public int getCount() {
		return _order.size();
 	}
 	
 	public Object getItem(int i) {
 		if (getItemId(i)>0)
 			return Restaurant.get(getItemId(i));
 		else return getItemId(i);
 	}
 	
 	public long getItemId(int i) {
 		return _order.get(i);
 	}
 	
 	public View getView(int i, View convertView, ViewGroup parent) {
 		long rID = getItemId(i);
 		if (rID>0) {
 			
 			ViewWrapper wrapper;
 			// checks if convertView not initialized, or initialized to partition
 			if (convertView == null || convertView.getTag()==null) {
 				LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflater.inflate(R.layout.main_list_item, parent, false);
 				wrapper = new ViewWrapper(convertView);
 				convertView.setTag(wrapper);
 			} else {
 				wrapper = (ViewWrapper)convertView.getTag();
 			}
 			//Restaurant r = (Restaurant)current;
 			if (displayFav) {
 				wrapper.getFavoriteView().setImageResource(Restaurant.favorite(rID)?
 				R.drawable.dining:		// favorite icon 
 				R.drawable.icon);		// nonfavorite icon
 			// TODO add proper favorite/nonfavorite icons
 			} else {
 				wrapper.getFavoriteView().setVisibility(ImageView.GONE);
 			}
 			wrapper.getNameView().setText(Restaurant.getName(rID));
 			wrapper.getSpecialView().setText(getSpecialText(Restaurant.getHours(rID)));
 
 			if (grayClosed) {
 				boolean enabled = true;
 				if (!Restaurant.getHours(rID).isOpen()) 
 					enabled = false;
 				wrapper.getNameView().setEnabled(enabled);
 				wrapper.getSpecialView().setEnabled(enabled);
 				wrapper.getFavoriteView().setEnabled(enabled);
 			}
 			
 			 return convertView;
 		} else {
 			TextView partition = new TextView(_context);
 			partition.setGravity(Gravity.CENTER);
 			partition.setFocusable(false);
 			partition.setClickable(false);
 			partition.setTextSize((float) 22.0);
 			switch ((int)rID) { 
 			case (int)FAVORITE_PARTITION:
 				partition.setText("Favorites");
 				break;
 			case (int)OPEN_PARTITION:
 				partition.setText("Open");
 				break;
 			case (int)CLOSED_PARTITION:
 				partition.setText("Closed");
 				break;				
 			case (int)OTHER_PARTITION:
 				partition.setText("Other");
 			}
 			return partition;
 			
 		}
 		
 	}
 	
 	/**
 	 * Sets sort method for list and sorts
 	 * @param sortType 
 	 * 		a class constant
 	 */
 	public void setSort(int sortType) {
 		grayClosed = true;
 		switch (sortType) {
 		case UNSORTED:
 		case ALPHABETICAL:
 		case OPEN_CLOSED:
 		case NEAR_FAR:
 			displayFav = true;
 			break;
 		case FAVORITE_OPEN_CLOSED:
 		case FAVORITE:
 			displayFav = false;
 			break;
 		}
 		
 		if (sortType == currentSortType || sortType == UNSORTED)
 			return; // only set the above if unsorted
 		currentSortType = sortType;
 		
 		// first remove partitions if this is a later sort
		for (int i = 0; i<_order.size(); i++)
			if (_order.get(i)<0)
				_order.remove(i);

 		sort(_order, ALPHABETICAL);
 		switch (sortType) {
 		case FAVORITE_OPEN_CLOSED:
 		{
 			sort(_order, OPEN_CLOSED);
 			sort(_order, FAVORITE);

 			int nonFav = firstNonFavorite();
 			int closed;
 			if (nonFav != -1) {
 				// if some are non favorites
 				//sort(_order.subList(nonFav, _order.size()), OPEN_CLOSED);
 				closed = firstClosed(nonFav);
 			} else {
 				closed = -1;
 			}
 			// add partitions bottom to top, so indices remain valid
 			
 			if (closed != -1 && nonFav!=-1) 
 				// if some restaurants are closed which are not favorites
 				_order.add(closed, (long)CLOSED_PARTITION);
 			if (nonFav != -1 && nonFav!=closed) 
 				// if some restaurants are open which are not favorites
 				_order.add(nonFav, (long)OPEN_PARTITION);
 			if (nonFav != 0) 
 				// if there are some favorites
 				_order.add(0, (long)FAVORITE_PARTITION);
 

 			break;
 		}
 		case FAVORITE:
 		{
 			sort(_order, FAVORITE);
 			int nonFav = firstNonFavorite();
 			if (nonFav != -1) 
 				// if some are non favorites
 				_order.add(firstNonFavorite(), (long)OTHER_PARTITION);
 			if (nonFav != 0) 
 				// if there are some favorites
 				_order.add(0, (long)FAVORITE_PARTITION);
 			break;
 		}
 		case OPEN_CLOSED:
 		{
 			sort(_order, OPEN_CLOSED);
 			int closed = firstClosed();
 			if (closed != -1)
 				// if some are closed
 				_order.add(closed, (long)CLOSED_PARTITION);
 			if (closed != 0) 
 				// if some are open
 				_order.add(0, (long)OPEN_PARTITION);
 			break;
 		}
 		case NEAR_FAR:
 		default:
 		}
 	}
 	
 	
 	/**
 	 *  Use this instead of setSort() if the underlying Restaurant data
 	 *  has changed.
 	 * @param sortType
 	 * 		class constant representing a sort method
 	 */
 	public void forceSetSort(int sortType) {
 		currentSortType = UNSORTED;
 		setSort(sortType);
 	}
 	
 	public void setShowFavIcon(boolean show) {
 		displayFav = show;
 	}
 	
 	public void setGrayClosed(boolean gray) {
 		grayClosed = gray;
 	}
 	
 	
 	/**	
 	 * @return 
 	 * the underlying array of Restaurant row ids and partitions,
 	 * modifying this will change the actual sort of the RestaurantAdapter
 	 */
 	public ArrayList<Long> getSortOrder() {
 		return _order;
 	}
 	
 	
 	/**
 	 * @param order
 	 * the new order of display elements, add a partition using the 
 	 * class constants as row ids
 	 */
 	public void setSortOrder(ArrayList<Long> order) {
 		_order = order;
 	}
 	
 	private String getSpecialText(RestaurantHours rh) {
 		int toOpen = rh.minutesToOpen();
 		if (toOpen==0) {
 			int min = rh.minutesToClose();
 			if (min<=60)
 				return "closes in " + min + " minutes ";
 			else return "closes at " + rh.getNextCloseTime().toString() + " ";
 		} else if (toOpen>0) {
 			if (toOpen<=60)
 				return "opens in " + toOpen + " minutes ";
 			else return "opens at " + rh.getNextOpenTime().toString() + " ";
 		} else return "closed "; // closed for the day
 	}
 	
 	
 	
 	// returns the first instance of a non favorite element in the sort, -1 if all are favorites
 	// useful in in lists sorted by favorites
 	private int firstNonFavorite() {
 		for (int i = 0; i<_order.size(); i++)
 			if (!Restaurant.favorite(_order.get(i)))
 				return i;
 		return -1;
 	}
 	private int firstClosed(int start) {
 		for (int i = start; i<_order.size(); i++)
 			if (!Restaurant.getHours(_order.get(i)).isOpen())
 				return i;
 		return -1;			
 	}
 	private int firstClosed() {
 		return firstClosed(0);
 	}
 	//implementation of merge sort
 	private void sort(List<Long> toSort, int sortType) {
 		if (toSort.size()<=1)
 			return;
 		int center = toSort.size()/2;
 		ArrayList<Long> left = new ArrayList<Long>();
 		left.addAll(toSort.subList(0, center));
 		ArrayList<Long> right = new ArrayList<Long>();
 		right.addAll(toSort.subList(center, toSort.size()));
 		sort(left, sortType);
 		sort(right, sortType);
 		int li = 0, ri = 0, i=0;
 		while (li<left.size() && ri<right.size()) {
 			if (compare(left.get(li), right.get(ri), sortType))
 				toSort.set(i++, left.get(li++));
 			else toSort.set(i++, right.get(ri++));
 		}
 		for (;li < left.size();)
 			toSort.set(i++, left.get(li++));
 		for (;ri < right.size();)
 			toSort.set(i++, right.get(ri++));
 	}
 	// compare method for merge, 'less or equal'
 	private boolean compare(long first, long second, int sortType) {
 		switch (sortType) {
 		case FAVORITE:
 			return !(!Restaurant.favorite(first) && Restaurant.favorite(second));
 		case ALPHABETICAL:
 			return Restaurant.getName(first).compareToIgnoreCase(Restaurant.getName(second))<=0; 
 		case OPEN_CLOSED:
 			return !(!Restaurant.getHours(first).isOpen() && Restaurant.getHours(second).isOpen());
 		case NEAR_FAR:
 			return true;
 		default:
 			return true;
 		}
 	}
 	
 }
