 package org.adaptlab.chpir.android.survey;
 
 import java.util.List;
 
 import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
 import org.adaptlab.chpir.android.activerecordcloudsync.PollService;
 import org.adaptlab.chpir.android.survey.Models.AdminSettings;
 import org.adaptlab.chpir.android.survey.Models.Instrument;
 import org.adaptlab.chpir.android.survey.Models.Option;
 import org.adaptlab.chpir.android.survey.Models.Question;
 import org.adaptlab.chpir.android.survey.Models.Response;
 import org.adaptlab.chpir.android.survey.Models.Survey;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class InstrumentFragment extends ListFragment {
     private final static String TAG = "InstrumentFragment";
 
     private List<Instrument> mInstrumentList;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 
         appInit();
         mInstrumentList = Instrument.getAll();
 
         InstrumentAdapter adapter = new InstrumentAdapter(mInstrumentList);
         setListAdapter(adapter);
         Log.d(TAG, "Instrument list is: " + mInstrumentList);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.fragment_instrument, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_item_admin:
             Intent i = new Intent(getActivity(), AdminActivity.class);
             startActivity(i);
         case R.id.menu_item_refresh:
             mInstrumentList = Instrument.getAll();
             InstrumentAdapter adapter = new InstrumentAdapter(mInstrumentList);
             setListAdapter(adapter);
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         ((InstrumentAdapter) getListAdapter()).notifyDataSetChanged();
     }
 
     private class InstrumentAdapter extends ArrayAdapter<Instrument> {
         public InstrumentAdapter(List<Instrument> instruments) {
             super(getActivity(), 0, instruments);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = getActivity().getLayoutInflater().inflate(
                         R.layout.list_item_instrument, null);
             }
 
             Instrument instrument = getItem(position);
 
             TextView titleTextView = (TextView) convertView
                     .findViewById(R.id.instrument_list_item_titleTextView);
             titleTextView.setText(instrument.getTitle());
 
             TextView surveysCompleteTextView = (TextView) convertView
                     .findViewById(R.id.instrument_list_item_surveysCompleteTextView);
            surveysCompleteTextView.setText(instrument.surveys().size() + " "
                    + getString(R.string.surveys));
 
             return convertView;
         }
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         Instrument instrument = ((InstrumentAdapter) getListAdapter())
                 .getItem(position);
         if (instrument == null || instrument.questions().size() == 0) {
             return;
         }
 
        long instrumentId = instrument.getId();
         Intent i = new Intent(getActivity(), SurveyActivity.class);
         i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, instrumentId);
         startActivity(i);
     }
 
     private final void appInit() {
         Log.i(TAG, "Initializing application...");
         DatabaseSeed.seed(getActivity());
 
         AdminSettings.getInstance().setDeviceIdentifier("TestDevice1");
         AdminSettings.getInstance().setSyncInterval(2);
         AdminSettings.getInstance().save();
 
         PollService.setPollInterval(AdminSettings.getInstance()
                 .getSyncInterval());
 
         ActiveRecordCloudSync.setEndPoint(AdminSettings.getInstance()
                 .getApiUrl());
         ActiveRecordCloudSync.addReceiveTable("instruments", Instrument.class);
         ActiveRecordCloudSync.addReceiveTable("questions", Question.class);
         ActiveRecordCloudSync.addReceiveTable("options", Option.class);
 
         ActiveRecordCloudSync.addSendTable("surveys", Survey.class);
         ActiveRecordCloudSync.addSendTable("responses", Response.class);
 
         PollService.setServiceAlarm(getActivity(), true);
     }
 }
