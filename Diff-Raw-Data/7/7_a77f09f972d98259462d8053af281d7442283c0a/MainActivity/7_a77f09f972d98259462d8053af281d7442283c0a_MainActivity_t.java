 package eu.urgas.mparkimine.activities;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import eu.urgas.mparkimine.CarRegistrationNumbersManager;
 import eu.urgas.mparkimine.R;
 import eu.urgas.mparkimine.adapters.CitiesListAdapter;
 import eu.urgas.mparkimine.dialogs.RegistrationNumbersDialog;
 import eu.urgas.mparkimine.items.CarRegistrationNumber;
 
 public class MainActivity extends Activity {
     private TextView carNumbersSelectView;
     private CarRegistrationNumbersManager carRegistrationNumberManager;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         setContentView(R.layout.main);
 
         //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         // tm.getNetworkOperatorName() => EE Tele2
         // tm.getNetworkOperator() => 24803
         // tm.getNetworkCountryIso() => ee
 
         carNumbersSelectView = (TextView) findViewById(R.id.choose_car_registration_number);
         carNumbersSelectView.setOnClickListener(new SelectCarNumberListener(this));
 
        carRegistrationNumberManager = new CarRegistrationNumbersManager(this);
        selectDefaultCarRegistrationNumber();

         initCitiesList();
 
         super.onCreate(savedInstanceState);
     }
 
     public void selectDefaultCarRegistrationNumber() {
         CarRegistrationNumber number = carRegistrationNumberManager.getDefault();
         if (number != null) {
             selectCarRegistrationNumber(number);
         }
     }
 
     public void selectCarRegistrationNumber(CarRegistrationNumber number) {
         carNumbersSelectView.setText(number.toString());
         carRegistrationNumberManager.setDefault(number);
     }
 
     private void initCitiesList() {
         CitiesListAdapter citiesListAdapter = new CitiesListAdapter(this);
         ExpandableListView citiesList = (ExpandableListView) findViewById(R.id.cities_list);
         citiesList.setAdapter(citiesListAdapter);
     }
 
     private class SelectCarNumberListener implements OnClickListener {
         private MainActivity activity;
 
         public SelectCarNumberListener(MainActivity activity) {
             this.activity = activity;
         }
 
         @Override
         public void onClick(View view) {
             Dialog dialog = new RegistrationNumbersDialog(activity);
             dialog.show();
         }
     }
 }
