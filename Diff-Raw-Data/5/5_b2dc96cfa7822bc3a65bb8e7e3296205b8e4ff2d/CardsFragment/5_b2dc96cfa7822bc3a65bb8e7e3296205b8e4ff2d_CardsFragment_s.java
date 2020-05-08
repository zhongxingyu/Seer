 package com.innutrac.poly.innutrac;
 
 /*
  * ******************************************************************************
  *   Copyright (c) 2013 Gabriele Mariotti.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *  *****************************************************************************
  */
 
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 
 import com.innutrac.poly.innutrac.database.Food;
 import com.innutrac.poly.innutrac.database.FoodDatabase;
 
 import it.gmariotti.cardslib.library.internal.Card;
 import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
 import it.gmariotti.cardslib.library.internal.CardHeader;
 import it.gmariotti.cardslib.library.view.CardListView;
 
 public class CardsFragment extends Fragment {
 	FoodDatabase fdb;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_cards, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         fdb = new FoodDatabase(getActivity());
         fdb.open("FoodRecord");
         initCard();
     }
 
     private void initCard() {
         ArrayList<Food> foods =fdb.getAllEatenFood();
     	
     	//Initialization an array of Cards
         ArrayList<Card> cards = new ArrayList<Card>();
         for (int i = 0; i < foods.size(); i++) {
             Food tempFood = foods.get(i);
         	TestCard card = new TestCard(getActivity());
             CardHeader header = new CardHeader(getActivity());
             card.setTitle(tempFood.getName());
             card.setSecondaryTitle("Calories: " + tempFood.getCalories());
             card.addCardHeader(header);
             card.setId("" + i);
             card.setShadow(true);
             card.init();
             cards.add(card);
         }
 
         CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
         
         //Enable undo controller
         mCardArrayAdapter.setEnableUndo(true);
         CardListView listView = (CardListView) getActivity().findViewById(R.id.myList);
         if (listView != null) {
             listView.setAdapter(mCardArrayAdapter);
         }
 
     }
     public class TestCard extends Card {
 
         protected TextView mTitle;
         protected TextView mSecondaryTitle;
         protected int count;
 
         protected String title;
         protected String secondaryTitle;
 
 
         public TestCard(Context context) {
             this(context, R.layout.card_inner_content);
         }
 
         public TestCard(Context context, int innerLayout) {
             super(context, innerLayout);
         }
 
         private void init() {
 
             setSwipeable(true);
 
             setOnSwipeListener(new OnSwipeListener() {
                     @Override
                     public void onSwipe(Card card) {
                        Toast.makeText(getContext(), "Removed food" + title, Toast.LENGTH_SHORT).show();
                     }
             });
 
 
             setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
                 @Override
                 public void onUndoSwipe(Card card) {
                    Toast.makeText(getContext(), "Undo removal of" + title + "?", Toast.LENGTH_SHORT).show();
                 }
             });
 
         }
 
         @Override
         public void setupInnerViewElements(ViewGroup parent, View view) {
 
             //Retrieve elements
             mTitle = (TextView) parent.findViewById(R.id.myapps_main_inner_title);
             mSecondaryTitle = (TextView) parent.findViewById(R.id.myapps_main_inner_secondaryTitle);
 
             if (mTitle != null)
                 mTitle.setText(title);
 
             if (mSecondaryTitle != null)
                 mSecondaryTitle.setText(secondaryTitle);
         }
 
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         public String getSecondaryTitle() {
             return secondaryTitle;
         }
 
         public void setSecondaryTitle(String secondaryTitle) {
             this.secondaryTitle = secondaryTitle;
         }
     }
 }
