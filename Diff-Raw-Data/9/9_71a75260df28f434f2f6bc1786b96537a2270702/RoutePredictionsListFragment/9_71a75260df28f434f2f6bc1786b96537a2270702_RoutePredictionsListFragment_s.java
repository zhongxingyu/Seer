 package com.kentph.ttcnextbus;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.kentph.ttcnextbus.NextBusPredictionsXmlParser.Prediction;
 import static com.kentph.ttcnextbus.NextBusPredictionsXmlParser.RoutePredictions;
 
 /**
  * Created by Kent on 9/6/13.
  */
 public class RoutePredictionsListFragment extends ListFragment {
     private PredictionsTransferInterface mPredictionsTransferInterface;
     private List<List<RoutePredictions>> predictions = null;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         // TODO move to onCreateView?
         try {
             mPredictionsTransferInterface = (PredictionsTransferInterface) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement mPredictionsTransferInterface");
         }
         predictions = mPredictionsTransferInterface.transferPredictions();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 //        View rootView = inflater.inflate(R.layout.listfragment_route_predictions, container, false);
 
 //        List<String> words = populateList();
 //        List<String> l = removeDoubles(words);
 //        Collections.sort(l);
 //
 //        ListView lv = (ListView)findViewById(R.id.listview);
 //
 //        RoutePredictionsListAdapter adapter = new RoutePredictionsListAdapter(getApplicationContext(), l);
 //
 //        lv.setAdapter(adapter);
        ListView mListView = (ListView) this.getActivity().findViewById(android.R.id.list);
 
         // get data from the table by the ListAdapter
         RoutePredictionsListAdapter mRoutePredictionsListAdapter =
                 new RoutePredictionsListAdapter(this.getActivity(),
                         R.layout.list_row_stop_prediction, predictions);
 
         mListView.setAdapter(mRoutePredictionsListAdapter);
 
         return mListView;
     }
 
     /**
      * Adapter for each stop. May contain more than 1 route.
      */
     public static class RoutePredictionsListAdapter
             extends ArrayAdapter<List<RoutePredictions>> {
 
 //        public RoutePredictionsListAdapter(Context context, int textViewResourceId) {
 //            super(context, textViewResourceId);
 //        }
 
         private List<List<RoutePredictions>> listOfRoutePredictionsByStop;
 
         public RoutePredictionsListAdapter(Context context, int textViewResourceId,
                 List<List<RoutePredictions>> listOfRoutePredictionsByStop) {
             super(context, textViewResourceId, listOfRoutePredictionsByStop);
             this.listOfRoutePredictionsByStop = listOfRoutePredictionsByStop;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
 
             View view = convertView;
 
             if (view == null) {
                 LayoutInflater vi;
                 vi = LayoutInflater.from(getContext());
                 view = vi.inflate(R.layout.list_row_stop_prediction, null);
             }
 
             List<RoutePredictions> stopRoutePredictions = listOfRoutePredictionsByStop.get(position);
 
             if (stopRoutePredictions != null) {
                 // add each prediction as a table row here
                 TableLayout tl = (TableLayout) view.findViewById(R.layout.list_row_stop_prediction);  // TODO change to PredictionActivity view?
 
                 for (RoutePredictions routePredictions : stopRoutePredictions) {
                     // Create and add new row in the format laid out by prediction_table_row for each prediction
                     TableRow newTr = new TableRow(this.getContext());
 
                     // Create cell layout of prediction times
                     RelativeLayout predLayout = new RelativeLayout(this.getContext());
 
                     TextView prediction1Tv = new TextView(this.getContext());
                     prediction1Tv.setText(routePredictions.listOfPredictions.get(0).minutes + " min");
                     RelativeLayout.LayoutParams l1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     l1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                     l1.addRule(RelativeLayout.CENTER_HORIZONTAL);
                     prediction1Tv.setLayoutParams(l1);
                     // TODO prediction1Tv.setTextSize();
 
                     TextView prediction2Tv = new TextView(this.getContext());
                     prediction2Tv.setText(routePredictions.listOfPredictions.get(1).minutes + " min");
                     RelativeLayout.LayoutParams l2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     l2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                     l2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                     prediction2Tv.setLayoutParams(l2);
                     // TODO prediction2Tv.setTextSize();
 
                     TextView prediction3Tv = new TextView(this.getContext());
                     prediction3Tv.setText(routePredictions.listOfPredictions.get(2).minutes + " min");
                     RelativeLayout.LayoutParams l3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     l3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                     l3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                     prediction3Tv.setLayoutParams(l3);
                     // TODO prediction3Tv.setTextSize();
 
                     // Add cell to table row
                     predLayout.addView(prediction1Tv);
                     predLayout.addView(prediction2Tv);
                     predLayout.addView(prediction3Tv);
                     newTr.addView(predLayout);
 
 
                     // Create cell layout of route info
                     RelativeLayout infoLayout = new RelativeLayout(this.getContext());
                     infoLayout.setBackgroundResource(R.drawable.card_background);
 
                     // set route info
                     TextView routeNumTv = new TextView(this.getContext());
                     routeNumTv.setId(1);
                     routeNumTv.setText(routePredictions.routeNumber);
                     RelativeLayout.LayoutParams lNum = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     lNum.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                     lNum.addRule(RelativeLayout.CENTER_VERTICAL);
                     routeNumTv.setLayoutParams(lNum);
 
                     TextView directionTv = new TextView(this.getContext());
                     directionTv.setId(2);
                     directionTv.setText(routePredictions.direction);
                     RelativeLayout.LayoutParams lDir = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     lDir.addRule(RelativeLayout.CENTER_VERTICAL);
                     lDir.addRule(RelativeLayout.RIGHT_OF, routeNumTv.getId());
                     directionTv.setLayoutParams(lDir);
 
                     TextView routeNameTv = new TextView(this.getContext());
                     routeNameTv.setId(3);
                     routeNameTv.setText(routePredictions.routeName);
                     RelativeLayout.LayoutParams lName = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     lName.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                     lName.addRule(RelativeLayout.RIGHT_OF, directionTv.getId());
                     routeNameTv.setLayoutParams(lName);
 
                     TextView terminalTv = new TextView(this.getContext());
                     terminalTv.setId(4);
                     terminalTv.setText("Dest: " + routePredictions.terminal);
                     RelativeLayout.LayoutParams lTerm = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
                     lTerm.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                     lTerm.addRule(RelativeLayout.RIGHT_OF, directionTv.getId());
                     terminalTv.setLayoutParams(lTerm);
 
                     // Add cell to table row
                     infoLayout.addView(routeNumTv);
                     infoLayout.addView(directionTv);
                     infoLayout.addView(routeNameTv);
                     infoLayout.addView(terminalTv);
                     newTr.addView(infoLayout);
 
                 /* Add row to TableLayout. */
                 //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                 //tl.addView(newTr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                     tl.addView(newTr);
                 }
             }
 
             return view;
 
         }
 //        public RoutePredictionsListAdapter(
 //                Context context,
 //                List<List<NextBusPredictionsXmlParser.RoutePredictions>> predictions) {
 //            // Caches the LayoutInflater for quicker use
 //            this.inflater = LayoutInflater.from(context);
 //            // Sets the events data
 //            this.predictions = predictions;
 //        }
 //
 //        @Override
 //        public int getCount() {
 //            return this.predictions.size();
 //        }
 //
 //        @Override
 //        public List<NextBusPredictionsXmlParser.RoutePredictions> getItem(int position)
 //                throws IndexOutOfBoundsException {
 //            return this.predictions.get(position);
 //        }
 //
 //        @Override
 //        public long getItemId(int position) throws IndexOutOfBoundsException {
 //            if(position < getCount() && position >= 0 ){
 //                return position;
 //            }
 //        }
 //
 //        @Override
 //        public int getViewTypeCount() {
 //            return 1;
 //        }
 //
 //        @Override
 //        public View getView(int position, View convertView, ViewGroup parent) {
 //            String myText = getItem(position);
 //
 //            if(convertView == null){ // If the View is not cached
 //                // Inflates the Common View from XML file
 //                convertView = this.inflater.inflate(R.id.list_row, null);
 //            }
 //
 //            // Select your color and apply it to your textview
 //            int myColor;
 //            if(myText.substring(0, 1) == "a"){
 //                myColor = Color.BLACK;
 //            }else{
 //                ....
 //            }
 //
 //            convertView.findViewById(R.id.myTextViewId).setBackground(myColor);
 //            // Of course you will need to set the same ID in your item list XML layout.
 //
 //            return convertView;
 //        }
     }
 }
