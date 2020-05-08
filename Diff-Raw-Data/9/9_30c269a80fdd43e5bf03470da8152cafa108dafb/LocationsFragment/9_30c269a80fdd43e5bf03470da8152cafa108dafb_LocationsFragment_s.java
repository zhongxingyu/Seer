 package net.greybeardedgeek.fragments;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.LoaderManager;
 import android.content.ActivityNotFoundException;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import net.greybeardedgeek.R;
 import net.greybeardedgeek.database.LocationProvider.Locations;
 
 import java.util.Date;
 
 public class LocationsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
 
     private static final String TAG = "LocationsFragment";
 
     private ListView locationList;
     private CursorAdapter locationAdapter;
 
     private Filter filter;
 
     public enum Filter {favorites, recent, all}
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         //return super.onCreateView(inflater, container, savedInstanceState);
         View view = inflater.inflate(R.layout.fragment_locations, null);
         return view;
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
         locationList = (ListView) view.findViewById(R.id.location_list);
         locationAdapter = new LocationAdapter(getActivity());
         locationList.setAdapter(locationAdapter);
 
         locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 handleItemClick(view);
             }
         });
 
         registerForContextMenu(locationList);
 
         Bundle bundle = getArguments();
         if(bundle != null && bundle.containsKey("filter")) {
             filter = Filter.valueOf(bundle.getString("filter"));
         }
 
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         getLoaderManager().initLoader(0, null, this);
     }
 
     @Override
     public void onPause() {
         getLoaderManager().destroyLoader(0);
         super.onPause();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.location_context_menu, menu);
     }
 
     private void handleItemClick(View view) {
         ViewHolder viewHolder = (ViewHolder) view.getTag();
         navigate(viewHolder);
     }
 
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 
         View targetView = info.targetView;
         ViewHolder viewHolder = (ViewHolder) targetView.getTag();
 
         switch (item.getItemId()) {
             case R.id.action_nav:
                 navigate(viewHolder);
                 return true;
 
             case R.id.action_edit:
                 edit(info.id);
                 return true;
 
             case R.id.action_delete:
                 deleteOnConfirm(info.id, viewHolder.nameView.getText().toString());
                 return true;
 
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     private void edit(long itemId) {
         AddEditLocationDialogFragment.newInstance(itemId).show(getFragmentManager(), "addDialog");
     }
 
     private void navigate(ViewHolder viewHolder){
         double latitude = 0.0;
         double longitude = 0.0;
 
         try {
             if(viewHolder.latString != null) {
                 latitude = Double.parseDouble(viewHolder.latString);
             }
 
             if(viewHolder.longString != null) {
                 longitude = Double.parseDouble(viewHolder.longString);
             }
 
         } catch (NumberFormatException ex) {
         //
         }
 
         if(latitude != 0.0 && longitude != 0.0) {
             navigate(latitude, longitude);
         } else {
             navigate(viewHolder.address);
         }
     }
 
     private void navigate(String address) {
         try {
             Uri uri = Uri.parse("google.navigation:q=" + address.replaceAll(" ", "+"));
             Log.d("nav", "navigating using uri: " + uri);
             Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
             startActivity(intent);
         } catch(ActivityNotFoundException ex) {
             Toast.makeText(getActivity(), "Google Navigation Application Not Found", Toast.LENGTH_LONG).show();
         }
     }
 
     private void navigate(double latitude, double longitude) {
         try {
             Uri uri = Uri.parse("google.navigation:ll=" + latitude + "," + longitude);
             Log.d("nav", "navigating using uri: " + uri);
             Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
             startActivity(intent);
         } catch(ActivityNotFoundException ex) {
             Toast.makeText(getActivity(), "Google Navigation Application Not Found", Toast.LENGTH_LONG).show();
         }
     }
 
     private void deleteLocation(long locationId) {
         getActivity().getContentResolver().delete(ContentUris.withAppendedId(Locations.CONTENT_URI, locationId), null, null);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 
         Log.d(TAG, "onCreateLoader");
 
         String[] projection = null; // all columns
         String selectionCriteria = null;
         String[] selectionArgs = null;
         String sort = null;
 
         if(filter != null) {
             switch(filter) {
                 case favorites:
                     selectionCriteria = Locations.IS_FAVORITE + " > 0";
                     sort = Locations.NAME + " asc ";
                     break;
 
                 case recent:
                     selectionCriteria = Locations.LAST_USED + " >= ?";
                     selectionArgs = new String[1];
                     selectionArgs[0] = getRecentTimeThresholdAsString();
                     sort = Locations.LAST_USED + " desc";
                     break;
 
                 case all:
                     sort = Locations.NAME + " asc ";
                     break;
             }
         }
 
         CursorLoader loader =  new CursorLoader(
                 getActivity(),
                 Locations.CONTENT_URI,
                 projection,
                 selectionCriteria,
                 selectionArgs,
                 sort
         );
 
         return loader;
     }
 
     private String getRecentTimeThresholdAsString() {
         long DAY_IN_MS = 1000 * 60 * 60 * 24;
         long now = new Date().getTime();
 
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         String pref = preferences.getString("recent_threshold", "7");
 
         int daysAgo = 7; // default value
 
         try {
             daysAgo = Integer.parseInt(pref);
         } catch (NumberFormatException ex) {
             //
         }
 
         long threshold = now - (daysAgo * DAY_IN_MS);
 
         return Long.toString(threshold);
     }
 
     public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
         locationAdapter.swapCursor(cursor);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         locationAdapter.swapCursor(null);
     }
 
     private static class LocationAdapter extends CursorAdapter {
 
         public LocationAdapter(Context context) {
             super(context, null, 0);
         }
 
         public LocationAdapter(Context context, Cursor cursor) {
             super(context, cursor, 0);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
             View view = LayoutInflater.from(context).inflate(R.layout.list_item_location, null);
             ViewHolder viewHolder = new ViewHolder();
             viewHolder.nameView = (TextView) view.findViewById(R.id.location_name);
             viewHolder.addressView = (TextView) view.findViewById(R.id.address);
             viewHolder.latlongView = (TextView) view.findViewById(R.id.latlong);
             viewHolder.favoriteView = (CheckBox) view.findViewById(R.id.star);
             view.setTag(viewHolder);
             return view;
         }
 
         @Override
         public void bindView(View view, final Context context, Cursor cursor) {
             ViewHolder viewHolder = (ViewHolder) view.getTag();
             viewHolder.id = cursor.getInt(cursor.getColumnIndex(Locations.ID));
             viewHolder.nameView.setText(cursor.getString(cursor.getColumnIndex(Locations.NAME)));
 
             viewHolder.address = cursor.getString(cursor.getColumnIndex(Locations.ADDRESS));
             viewHolder.latString = cursor.getString(cursor.getColumnIndex(Locations.LATITUDE));
             viewHolder.longString = cursor.getString(cursor.getColumnIndex(Locations.LONGITUDE));
 
             if(viewHolder.address != null && !viewHolder.address.isEmpty()) {
                 viewHolder.addressView.setText(viewHolder.address);
                 viewHolder.addressView.setVisibility(View.VISIBLE);
             } else {
                 viewHolder.addressView.setText("");
                 viewHolder.addressView.setVisibility(View.GONE);
             }
 
             if(viewHolder.latString != null && !viewHolder.latString.isEmpty() && viewHolder.longString != null && !viewHolder.longString.isEmpty()) {
                 viewHolder.latlongView.setText("(" + viewHolder.latString + ", " + viewHolder.longString + ")");
                 viewHolder.latlongView.setVisibility(View.VISIBLE);
             } else {
                 viewHolder.latlongView.setText("");
                 viewHolder.latlongView.setVisibility(View.GONE);
             }
 
             viewHolder.favoriteView.setChecked(cursor.getInt(cursor.getColumnIndex(Locations.IS_FAVORITE)) != 0);
             viewHolder.favoriteView.setTag(new Integer(viewHolder.id));
             viewHolder.favoriteView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     CheckBox checkBox = (CheckBox) view;
                     int id = ((Integer) checkBox.getTag()).intValue();
                     ContentValues values = new ContentValues();
                     values.put(Locations.IS_FAVORITE, checkBox.isChecked());
                     context.getContentResolver().update(ContentUris.withAppendedId(Locations.CONTENT_URI, id), values, null, null);
                 }
             });
         }
     }
 
     private static class ViewHolder {
         public int id;
         public TextView nameView;
         public TextView addressView;
         public TextView latlongView;
         public CheckBox favoriteView;
 
         public String address;
         public String latString;
         public String longString;
     }
 
     private void deleteOnConfirm(final long locationId, final String locationName) {
 
         AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
         dialog.setTitle("Delete Location");
         dialog.setMessage("Delete " + locationName + "?");
         dialog.setCancelable(false);
         dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int buttonId) {
                 deleteLocation(locationId);
                 dialog.dismiss();
             }
         });
         dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int buttonId) {
                 dialog.dismiss();
             }
         });
         dialog.setIcon(android.R.drawable.ic_dialog_alert);
         dialog.show();
     }
 }
 
