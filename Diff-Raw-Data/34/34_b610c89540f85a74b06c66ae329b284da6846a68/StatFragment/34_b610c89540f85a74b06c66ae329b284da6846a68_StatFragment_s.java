 package com.nurun.activemtl.ui.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.location.LocationClient;
 import com.nurun.activemtl.ActiveMtlApplication;
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.callback.GetEventStatCallback;
 import com.nurun.activemtl.controller.EventController;
 import com.nurun.activemtl.ui.fragment.ProfileFragment.Area;
 
 public class StatFragment extends Fragment {
 
     private static final String EXTRA_AREA = "EXTRA_AREA";
 
     private EventController eventController;
     private TextView ideaCounter;
     private TextView challengeCounter;
     private TextView alertCounter;
 
     private LocationClient locationClient;
 
     protected int alertValue = -1;
     protected int challengeValue = -1;
     protected int ideaValue = -1;
 
     public static Fragment newInstance(Area area) {
         StatFragment statFragment = new StatFragment();
         Bundle bundle = new Bundle();
         bundle.putSerializable(EXTRA_AREA, area);
         statFragment.setArguments(bundle);
         return statFragment;
     }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         eventController = (EventController) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.EVENT_CONTROLLER);
         locationClient = (LocationClient) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.LOCATION_CLIENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_fragment, null);
        ideaCounter = (TextView) view.findViewById(R.id.idea_counter);
        challengeCounter = (TextView) view.findViewById(R.id.challenge_counter);
        alertCounter = (TextView) view.findViewById(R.id.alert_counter);
         if (alertValue == -1 || challengeValue == -1 || ideaValue == -1) {
             Area area = (Area) getArguments().getSerializable(EXTRA_AREA);
             switch (area) {
             case ME:
                 eventController.getAlertForMe(alertCountCallback);
                 eventController.getChallengeForMe(challengeCountCallback);
                 eventController.getIdeaForMe(ideaCountCallback);
                 break;
             case DISTRICT:
                 eventController.getAlertForDistrict(alertCountCallback, locationClient.getLastLocation());
                 eventController.getChallengeForDistrict(challengeCountCallback, locationClient.getLastLocation());
                 eventController.getIdeaForDistrict(ideaCountCallback, locationClient.getLastLocation());
                 break;
             case MONTREAL:
                 eventController.getAlertForCity(alertCountCallback);
                 eventController.getChallengeForCity(challengeCountCallback);
                 eventController.getIdeaForCity(ideaCountCallback);
                 break;
             default:
                 throw new IllegalStateException("Wrong area : " + area);
             }
         } else {
             ideaCounter.setText("" + ideaValue);
             challengeCounter.setText("" + challengeValue);
             alertCounter.setText("" + alertValue);
         }
         return view;
     }
 
     private GetEventStatCallback alertCountCallback = new GetEventStatCallback() {
 
         @Override
         public void onEventStatFailed(RuntimeException runtimeException) {
             Toast.makeText(getActivity(), R.string.error_retrieving_alerts, Toast.LENGTH_LONG).show();
         }
 
         @Override
         public void onEventStatComplete(int count) {
             alertValue = count;
             alertCounter.setText("" + alertValue);
         }
     };
 
     private GetEventStatCallback challengeCountCallback = new GetEventStatCallback() {
 
         @Override
         public void onEventStatFailed(RuntimeException runtimeException) {
             Toast.makeText(getActivity(), R.string.error_retrieving_challenges, Toast.LENGTH_LONG).show();
         }
 
         @Override
         public void onEventStatComplete(int count) {
             challengeValue = count;
             challengeCounter.setText("" + challengeValue);
         }
     };
 
     private GetEventStatCallback ideaCountCallback = new GetEventStatCallback() {
 
         @Override
         public void onEventStatFailed(RuntimeException runtimeException) {
             Toast.makeText(getActivity(), R.string.error_retrieving_ideas, Toast.LENGTH_LONG).show();
         }
 
         @Override
         public void onEventStatComplete(int count) {
             ideaValue = count;
             ideaCounter.setText("" + ideaValue);
         }
     };
 }
