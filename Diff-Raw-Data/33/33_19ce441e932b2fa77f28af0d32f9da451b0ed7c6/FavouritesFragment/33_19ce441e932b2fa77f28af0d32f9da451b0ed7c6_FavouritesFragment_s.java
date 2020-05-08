 package ru.noisefm.orders.fragment;
 
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import ru.noisefm.orders.R;
 import ru.noisefm.orders.activity.MainActivity;
 import ru.noisefm.orders.adapter.FavouritesAdapter;
 import ru.noisefm.orders.database.FavouritesDatabase;
 import ru.noisefm.orders.helper.Util;
 import ru.noisefm.orders.order.Track;
 
 public class FavouritesFragment extends BaseFragment {
     private FavouritesDatabase favDatabase;
     private FavouritesAdapter favAdapter;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fav_list_view, null);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         final MainActivity activity = getMainActivity();
         favDatabase = getMainActivity().getFavouriteDatabase();
         favAdapter = new FavouritesAdapter(activity, favDatabase.getCursor());
         ListView favListView = (ListView)activity.findViewById(R.id.fav_list);
         favListView.setAdapter(favAdapter);
         favListView.setEmptyView(activity.findViewById(R.id.fav_list_empty_view));
         favListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                 Track track = favDatabase.getTrack(id);
                 activity.orderTrack(track);
             }
         });
         registerForContextMenu(favListView);
     }
 
     @Override
    public void onResume() {
        super.onResume();
        // Update title after getting fragment from navigation stack
        updateActivityTitle();
    }

    @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.fav_list_context_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         long trackItemId = info.id;
         switch (item.getItemId()) {
             case R.id.fav_list_menu_delete:
                 favDatabase.delTrack(trackItemId);
                 favAdapter.changeCursor(favDatabase.getCursor());
                 return true;
             case R.id.fav_list_menu_copy_track_data:
                 Util.copyTrackData(getActivity(), favDatabase.getTrack(trackItemId));
                 return true;
             case R.id.fav_list_menu_search_by_artist:
                 getMainActivity().searchQuery(favDatabase.getTrack(trackItemId).getArtist());
                 return true;
         }
         return false;
     }
 
     @Override
     public void updateActivityTitle() {
         getSherlockActivity().getSupportActionBar().setTitle(R.string.favourites);
         getSherlockActivity().getSupportActionBar().setSubtitle(null);
     }
 }
