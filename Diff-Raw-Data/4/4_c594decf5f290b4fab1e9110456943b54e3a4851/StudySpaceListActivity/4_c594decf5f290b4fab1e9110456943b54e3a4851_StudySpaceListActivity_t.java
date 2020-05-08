 package edu.upenn.studyspaces;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class StudySpaceListActivity extends ListActivity {
 
     private ProgressDialog ss_ProgressDialog = null; // Dialog when loading
     private ArrayList<StudySpace> ss_list = null; // List containing available
                                                   // rooms
     private StudySpaceListAdapter ss_adapter; // Adapter to format list items
     private Runnable viewAvailableSpaces; // runnable to get available spaces
     public static final int ACTIVITY_ViewSpaceDetails = 1;
     public static final int ACTIVITY_SearchActivity = 2;
     private SearchOptions searchOptions; // create a default searchoption later
     private boolean favSelected = false;
     private Preferences preferences;
 
     private SharedPreferences favorites;
     static final String FAV_PREFERENCES = "favoritePreferences";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sslist);
 
        this.searchOptions = (SearchOptions) getIntent().getSerializableExtra(
                "SEARCH_OPTIONS");
         favorites = getSharedPreferences(FAV_PREFERENCES, 0);
 
         ss_list = new ArrayList<StudySpace>(); // List to store StudySpaces
         this.ss_adapter = new StudySpaceListAdapter(this, R.layout.sslistitem,
                 ss_list);
        ss_adapter.filterSpaces();
         this.setListAdapter(this.ss_adapter); // Adapter to read list and
                                               // display
 
         Map<String, ?> items = favorites.getAll();
         preferences = new Preferences(); // Change this when bundle is
                                          // implemented.
         for (String s : items.keySet()) {
             // boolean fav = favorites.getBoolean(s, false);
             if (Boolean.parseBoolean(items.get(s).toString())) {
                 preferences.addFavorites(s);
             }
         }
 
         viewAvailableSpaces = new Runnable() {
             public void run() {
                 getSpaces(); // retrieves list of study spaces
             }
         };
         Thread thread = new Thread(null, viewAvailableSpaces, "ThreadName"); // change
                                                                              // name?
         thread.start();
         ss_ProgressDialog = ProgressDialog.show(StudySpaceListActivity.this,
                 "Please wait...", "Retrieving data ...", true);
 
         /*
          * engiBox.setChecked(true); engiBox.setOnCheckedChangeListener(new
          * CompoundButton.OnCheckedChangeListener() { public void
          * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          * ss_adapter.filterSpaces(); } }); whartonBox.setChecked(true);
          * whartonBox .setOnCheckedChangeListener(new
          * CompoundButton.OnCheckedChangeListener() { public void
          * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          * ss_adapter.filterSpaces(); } }); libBox.setChecked(true);
          * libBox.setOnCheckedChangeListener(new
          * CompoundButton.OnCheckedChangeListener() { public void
          * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          * ss_adapter.filterSpaces(); } }); otherBox.setChecked(true);
          * otherBox.setOnCheckedChangeListener(new
          * CompoundButton.OnCheckedChangeListener() { public void
          * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          * ss_adapter.filterSpaces(); } });
          */
         final TextView search = (EditText) findViewById(R.id.search);
         search.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(Editable s) {
                 String query = search.getText().toString();
                 ss_adapter.searchNames(query);
             }
 
             public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {
             }
 
             public void onTextChanged(CharSequence s, int start, int before,
                     int count) {
             }
         });
 
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) { // click
         // String item = ((StudySpace)
         // getListAdapter().getItem(position)).getSpaceName();
         // Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
         Intent i = new Intent(this, StudySpaceDetails.class);
         i.putExtra("STUDYSPACE", (StudySpace) getListAdapter()
                 .getItem(position));
         i.putExtra("PREFERENCES", preferences);
         startActivityForResult(i,
                 StudySpaceListActivity.ACTIVITY_ViewSpaceDetails);
     }
 
     protected void onActivityResult(int requestCode, int resultCode,
             Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         switch (requestCode) {
         case ACTIVITY_SearchActivity:
             searchOptions = (SearchOptions) intent
                     .getSerializableExtra("SEARCH_OPTIONS");
             favSelected = false;
             ImageView image = (ImageView) this
                     .findViewById(R.id.favorite_button);
             image.setImageResource(R.color.yellow);
             ss_adapter.filterSpaces();
             ss_adapter.updateFavorites(preferences);
             break;
         case ACTIVITY_ViewSpaceDetails:
             preferences = (Preferences) intent
                     .getSerializableExtra("PREFERENCES");
             ss_adapter.updateFavorites(preferences);
             break;
         }
     }
 
     private Runnable returnRes = new Runnable() {
         public void run() {
             ss_ProgressDialog.dismiss();
             ss_adapter.notifyDataSetChanged();
             if (searchOptions != null)
                 ss_adapter.filterSpaces();
         }
     };
 
     private void getSpaces() {
         try {
             ss_list.addAll(APIAccessor.getStudySpaces()); // uncomment this
             ss_adapter.updateFavorites(preferences);
             Thread.sleep(2000); // appears to load for 2 seconds
             Log.i("ARRAY", "" + ss_list.size());
         } catch (Exception e) {
             Log.e("BACKGROUND_PROC", "Something went wrong!");
         }
         runOnUiThread(returnRes);
     }
 
     public void onFavClick(View v) {
         ImageView image = (ImageView) this.findViewById(R.id.favorite_button);
         if (favSelected) {
             favSelected = false;
             image.setImageResource(R.color.yellow);
             ss_adapter.favToAll();
         } else {
             favSelected = true;
             image.setImageResource(R.color.lightblue);
             ss_adapter.allToFav();
         }
 
     }
 
     public void onFilterClick(View view) {
         // Start up the search options screen
         finish();
     }
 
     private class StudySpaceListAdapter extends ArrayAdapter<StudySpace> {
 
         private ArrayList<StudySpace> list_items;
         private ArrayList<StudySpace> orig_items;
         private ArrayList<StudySpace> before_search;
         private ArrayList<StudySpace> fav_orig_items;
         private ArrayList<StudySpace> temp; // Store list items for when
                                             // favorites is displayed
 
         public StudySpaceListAdapter(Context context, int textViewResourceId,
                 ArrayList<StudySpace> items) {
             super(context, textViewResourceId, items);
             this.list_items = items;
             this.orig_items = items;
             this.fav_orig_items = new ArrayList<StudySpace>();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater vi = (LayoutInflater) getContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(R.layout.sslistitem, null);
             }
             // int index = getRealPosition(position);
 
             StudySpace o = list_items.get(position);
             if (o != null) {
 
                 TextView tt = (TextView) v.findViewById(R.id.nametext);
                 TextView bt = (TextView) v.findViewById(R.id.roomtext);
                 if (tt != null) {
                     tt.setText(o.getBuildingName() + " - " + o.getSpaceName());
                 }
                 if (bt != null) {
                     if (o.getNumberOfRooms() == 1) {
                         bt.setText(o.getRooms()[0].getRoomName());
                     } else {
                         bt.setText(o.getRooms()[0].getRoomName() + " (and "
                                 + String.valueOf(o.getNumberOfRooms() - 1)
                                 + " others)");
                     }
                 }
                 ImageView image = (ImageView) v.findViewById(R.id.icon);
                 int resID;
                 if (image != null) {
                     Resources resource = getResources();
                     if (o.getBuildingType().equals(StudySpace.ENGINEERING))
                         resID = resource.getIdentifier("engiicon", "drawable",
                                 getPackageName());
                     else if (o.getBuildingType().equals(StudySpace.WHARTON))
                         resID = resource.getIdentifier("whartonicon",
                                 "drawable", getPackageName());
                     else if (o.getBuildingType().equals(StudySpace.LIBRARIES))
                         resID = resource.getIdentifier("libicon", "drawable",
                                 getPackageName());
                     else
                         resID = resource.getIdentifier("othericon", "drawable",
                                 getPackageName());
                     image.setImageResource(resID);
                 }
                 ImageView priv = (ImageView) v.findViewById(R.id.priv);
                 ImageView wb = (ImageView) v.findViewById(R.id.wb);
                 ImageView comp = (ImageView) v.findViewById(R.id.comp);
                 ImageView proj = (ImageView) v.findViewById(R.id.proj);
                 if (priv != null && o.getPrivacy().equals("S"))
                     priv.setVisibility(View.INVISIBLE);
                 else
                     priv.setVisibility(View.VISIBLE);
                 if (wb != null && !o.hasWhiteboard())
                     wb.setVisibility(View.INVISIBLE);
                 else
                     wb.setVisibility(View.VISIBLE);
                 if (comp != null && !o.hasComputer())
                     comp.setVisibility(View.INVISIBLE);
                 else
                     comp.setVisibility(View.VISIBLE);
                 if (proj != null && !o.has_big_screen())
                     proj.setVisibility(View.INVISIBLE);
                 else
                     proj.setVisibility(View.VISIBLE);
             }
             return v;
         }
 
         @Override
         public int getCount() {
             return list_items.size();
         }
 
         @Override
         public StudySpace getItem(int position) {
             return list_items.get(position);
         }
 
         public void filterSpaces() {
 
             ArrayList<StudySpace> filtered = (ArrayList<StudySpace>) orig_items
                     .clone();
 
             int i = 0;
             while (i < filtered.size()) {
                 if (!searchOptions.getEngi()
                         && filtered.get(i).getBuildingType()
                                 .equals(StudySpace.ENGINEERING)) {
                     filtered.remove(i);
                     continue;
                 }
                 if (!searchOptions.getWhar()
                         && filtered.get(i).getBuildingType()
                                 .equals(StudySpace.WHARTON)) {
                     filtered.remove(i);
                     continue;
                 }
                 if (!searchOptions.getLib()
                         && filtered.get(i).getBuildingType()
                                 .equals(StudySpace.LIBRARIES)) {
                     filtered.remove(i);
                     continue;
                 }
                 if (!searchOptions.getOth()
                         && filtered.get(i).getBuildingType()
                                 .equals(StudySpace.OTHER)) {
                     filtered.remove(i);
                     continue;
                 }
                 if (searchOptions.getPrivate()
                         && filtered.get(i).getPrivacy().equals("S")) {
                     filtered.remove(i);
                     continue;
                 }
                 if (searchOptions.getWhiteboard()
                         && !filtered.get(i).hasWhiteboard()) {
                     filtered.remove(i);
                     continue;
                 }
                 if (searchOptions.getComputer()
                         && !filtered.get(i).hasComputer()) {
                     filtered.remove(i);
                     continue;
                 }
                 if (searchOptions.getProjector()
                         && !filtered.get(i).has_big_screen()) {
                     filtered.remove(i);
                     continue;
                 }
                 i++;
             }
 
             // this.list_items = filtered;
 
             this.list_items = SpaceInfo.sortByRank(filtered);
             this.list_items = filterByPeople(list_items);
             this.list_items = filterByDate(list_items);
             this.before_search = (ArrayList<StudySpace>) this.list_items
                     .clone();
 
             notifyDataSetChanged();
         }
 
         public void searchNames(String query) {
             query = query.toLowerCase();
             this.list_items = (ArrayList<StudySpace>) this.before_search
                     .clone();
             if (!query.equals("")) {
                 for (int i = list_items.size() - 1; i >= 0; i--) {
                     StudySpace s = list_items.get(i);
                     if (s.getBuildingName().toLowerCase().indexOf(query) >= 0
                             || s.getSpaceName().toLowerCase().indexOf(query) >= 0
                             || s.getRoomNames().toLowerCase().indexOf(query) >= 0) {
 
                     } else {
                         list_items.remove(i);
                     }
                 }
             }
             notifyDataSetChanged();
         }
 
         // switch to favorites
         public void allToFav() {
             this.temp = this.list_items; // remember list items
             this.list_items = fav_orig_items;
             notifyDataSetChanged();
         }
 
         public void favToAll() {
             this.list_items = this.temp; // restore list items
             notifyDataSetChanged();
         }
 
         public void updateFavorites(Preferences p) {
             this.fav_orig_items = SpaceInfo.sortByRank(this.orig_items);
             for (int i = fav_orig_items.size() - 1; i >= 0; i--) {
                 if (!p.isFavorite(fav_orig_items.get(i).getBuildingName()
                         + fav_orig_items.get(i).getSpaceName()))
                     fav_orig_items.remove(i);
             }
         }
     }
 
     public ArrayList<StudySpace> filterByDate(ArrayList<StudySpace> arr) {
         Date d1 = searchOptions.getStartDate();
         Date d2 = searchOptions.getEndDate();
         // d1 = new Date(112, 3, 7, 15, 0);
         // d2 = new Date(112, 3, 9, 23, 0);
         // d2.setHours(d2.getHours()+1);
         // Log.e("date1", d1.toString());
         // Log.e("date2", d2.toString());
         for (int i = arr.size() - 1; i >= 0; i--) {
             boolean flag = false;
             for (Room r : arr.get(i).getRooms()) {
                 try {
                     if (r.searchAvailability(d1, d2)) {
                         flag = true;
                     }
                 } catch (Exception e) {
                     // Log.e("exception","here");
                     // arr.remove(i); shouldn't be here
                 }
             }
 
             if (!flag)
                 arr.remove(i);
         }
         return arr;
     }
 
     public ArrayList<StudySpace> filterByPeople(ArrayList<StudySpace> arr) {
         for (int i = arr.size() - 1; i >= 0; i--) {
             if (arr.get(i).getMaximumOccupancy() < searchOptions
                     .getNumberOfPeople())
                 arr.remove(i);
         }
         return arr;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.meme:
             startActivity(new Intent(this, Meme.class));
             break;
         case R.id.about:
             startActivity(new Intent(this, About.class));
             break;
         case R.id.help:
             startActivity(new Intent(this, Help.class));
             break;
         }
         return true;
     }
 }
