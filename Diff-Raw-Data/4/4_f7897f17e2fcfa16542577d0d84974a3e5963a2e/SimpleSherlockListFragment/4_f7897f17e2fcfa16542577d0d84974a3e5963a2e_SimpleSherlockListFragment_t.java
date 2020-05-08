 package com.github.sgdesmet.android.utils.list;
 
 import android.view.View;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 
 /**
  * TODO description
  * <p/>
  * Date: 18/06/13
  * Time: 16:56
  *
  * @author: sgdesmet
  */
 public class SimpleSherlockListFragment extends SherlockListFragment {
 
     public void setSimpleListAdapter(SimpleListAdapter adapter){
         setListAdapter( adapter );
     }
 
     public SimpleListAdapter getSimpleListAdapter(){
         if (getListAdapter() != null && getListAdapter() instanceof SimpleListAdapter)
             return (SimpleListAdapter) getListAdapter();
         return null;
     }
 
     @Override
     public void onListItemClick(final ListView l, final View v, final int position, final long id) {
 
         super.onListItemClick( l, v, position, id );
        if (getSimpleListAdapter() != null && getSimpleListAdapter().getItem(position) != null)
            getSimpleListAdapter().getItem(position).onClick();
     }
 }
