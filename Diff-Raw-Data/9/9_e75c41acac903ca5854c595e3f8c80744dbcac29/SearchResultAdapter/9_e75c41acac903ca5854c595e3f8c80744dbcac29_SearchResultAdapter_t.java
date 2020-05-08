 package cs169.project.thepantry;
 
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.loopj.android.image.SmartImageView;
 
 import cs169.project.thepantry.ThePantryContract.Inventory;
 
 public class SearchResultAdapter extends ArrayAdapter<SearchMatch> {
 	
 	  private final Context context;
 	  public List<SearchMatch> values;
 	  DatabaseModel dm;
 	  private static final String DATABASE_NAME = "thepantry";
 
 	  public SearchResultAdapter(Context context, List<SearchMatch> values) {
 		  
 	  //call the super class constructor and provide the ID of the resource to use instead of the default list view item
 	    super(context, R.layout.list_result, values);
 	    this.context = context;
 	    this.values = values;
 	  }
 	  
 	  //this method is called once for each item in the list
 	  @Override
 	  public View getView(int position, View convertView, ViewGroup parent) {
 		String youHave = "";
 		String youNeed = "";
 		String source = "Unknown";
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    View listItem = inflater.inflate(R.layout.list_result, parent, false);
 	
 	    if (values.size() > 0) {
 	    	// check which ingredients are in database
 	    	// add them to you have or you need accordingly
 	    	dm = new DatabaseModel(context, DATABASE_NAME);
 			Cursor invItems = dm.findAllItems(Inventory.TABLE_NAME);
 			Boolean found;
 	    	for (int i=0; i < values.get(position).ingredients.size(); i++) {
 	    		found = false;
 	    		if (invItems != null && invItems.moveToFirst()) {
 	    			do {
	    				if (invItems.getString(Inventory.ITEMIND).toLowerCase().equals(values.get(position).ingredients.get(i).toLowerCase())) {
 	    					found = true;
 	    					break;
 	    				}
 	    			} while (invItems.moveToNext());
 	    		}
 	    		if (found) {
 	    			youHave += values.get(position).ingredients.get(i) + ", ";
 	    		}
 	    		else {
 	    			youNeed += values.get(position).ingredients.get(i) + ", ";
 	    		}
 	    	}
 	    	
 	    	//take off trailing commas
 	    	if (youHave.length() > 0) {
 	    		youHave = youHave.substring(0, youHave.length()-2);
 	    	}
 	    	if (youNeed.length() > 0) {
 	    		youNeed = youNeed.substring(0, youNeed.length()-2);
 	    	}
 	    
 	    	listItem.setTag(values.get(position).id);
 	    
 		    // set the title of the result item
 		    TextView titleView = (TextView) listItem.findViewById(R.id.title);
 		    titleView.setText(values.get(position).name);
 		    
 		    //set you have and you need
 		    TextView youHaveView = (TextView) listItem.findViewById(R.id.you_have);
 		    youHaveView.setText("You have: " + youHave);
 		    
 		    TextView youNeedView = (TextView) listItem.findViewById(R.id.you_need);
 		    youNeedView.setText("You need: " + youNeed);
 		    
 		    // display the source
 		    TextView timeView = (TextView) listItem.findViewById(R.id.source);
 		    timeView.setText("Source: " + values.get(position).sourceDisplayName);
 		    
 		    //load the image
 		    SmartImageView imageView = (SmartImageView) listItem.findViewById(R.id.image);
 		    if (values.get(position).smallImageUrl != null) { //might need an online check
 		    	imageView.setImageUrl(values.get(position).smallImageUrl);
 		    }
 	    }   
 	    return listItem;
 	  }
 }
