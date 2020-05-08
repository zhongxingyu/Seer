 package com.bubbletastic.prayercards;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.bubbletastic.prayercards.model.Card;
 
 
 public class CardListFragment extends ListFragment {
 
     private static final String STATE_ACTIVATED_POSITION = "activated_position";
 
     private CardListFragmentAccess mCallbacks = sDummyCallbacks;
     private int mActivatedPosition = ListView.INVALID_POSITION;
 
     private static CardListFragmentAccess sDummyCallbacks = new CardListFragmentAccess() {
         @Override
         public void onItemSelected(int id) {
         }
 
     };
 
     public CardListFragment() { }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setListAdapter(new ArrayAdapter<Card>(getActivity(),
                 R.layout.card_list_row_layout,
                 android.R.id.text1,
                 PrayerCards.cardTitles.CARDS));
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
        setActivateOnItemClick(true);
         if (savedInstanceState != null && savedInstanceState
                 .containsKey(STATE_ACTIVATED_POSITION)) {
             setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         if (!(activity instanceof CardListFragmentAccess)) {
             throw new IllegalStateException("Activity must implement fragment's callbacks.");
         }
 
         mCallbacks = (CardListFragmentAccess) activity;
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         mCallbacks = sDummyCallbacks;
     }
 
     @Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
         super.onListItemClick(listView, view, position, id);
         mCallbacks.onItemSelected(position);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (mActivatedPosition != ListView.INVALID_POSITION) {
             outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
         }
     }
 
     public void setActivateOnItemClick(boolean activateOnItemClick) {
         getListView().setChoiceMode(activateOnItemClick
                 ? ListView.CHOICE_MODE_SINGLE
                 : ListView.CHOICE_MODE_NONE);
     }
 
     public void setActivatedPosition(int position) {
         if (position == ListView.INVALID_POSITION) {
             getListView().setItemChecked(mActivatedPosition, false);
         } else {
             getListView().setItemChecked(position, true);
         }
 
         mActivatedPosition = position;
     }
 }
