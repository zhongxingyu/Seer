 package main.pfmandroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /*
  * Class represents the Income option from the Main Menu. Class responsible for allowing the user to make
  * transaction that increases the wallets current amount of money from some sort of income source.
  */
 
 public class EarnActivity extends Activity{
     CategoryListener categorylistener;
     MoneyListener moneylistener;
     WalletListener walletlistener;
     
     Wallet[] wallets;
     Source[] sources;
     Currency[] currencies;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         wallets = new Wallet[DataStorage.listOfWallets.size()];
         for(int i = 0; i < wallets.length; i++)
         	wallets[i] = DataStorage.listOfWallets.get(i);
         
         sources = new Source[DataStorage.listOfSources.size()];
         for(int i = 0; i < sources.length; i++)
         	sources[i] = DataStorage.listOfSources.get(i);
         
         currencies = new Currency[DataStorage.typesOfCurrency.size()];
        for(int i = 0; i < currencies.length; i++)
         	currencies[i] = DataStorage.typesOfCurrency.get(i);
         
         setContentView(R.layout.activity_earn);
         
         TextView changable = (TextView) findViewById(R.id.textViewEa3);
         //Create a class in which two integers are incapsulated, so that the listeners can share it and change it together.
         PositionContainer poscon = new PositionContainer();
         
         //
         Spinner spinner = (Spinner) findViewById(R.id.spinnerEa1);
         // Create an ArrayAdapter using the Wallet array and a custom spinneritem layout (res/layout)
         ArrayAdapter<Wallet> adapter = new ArrayAdapter<Wallet>(this, R.layout.spinneritem, R.id.spinneritem, wallets);
 	    // Specify the layout to use when the list of choices appears
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    // Apply the adapter to the spinner and add a listener so that we can track changes
 	    spinner.setAdapter(adapter);
 	    if(DataStorage.listOfWallets.size() > 0){
 		    walletlistener = new WalletListener(changable, poscon);
 		    spinner.setOnItemSelectedListener(walletlistener);
 	    }
 	    
 	    //Same as previous, only different spinner and different listener (category this time)
 	    Spinner spinner2 = (Spinner) findViewById(R.id.spinnerEa2);
         ArrayAdapter<Source> adapter2 = new ArrayAdapter<Source>(this, R.layout.spinneritem, R.id.spinneritem, sources);
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner2.setAdapter(adapter2);
 	    if(DataStorage.listOfSources.size() > 0){
 		    SourceListener sourcelistener = new SourceListener();
 		    spinner2.setOnItemSelectedListener(sourcelistener);
 	    }
 	    
 	    //Same thing with currency type spinner, assign a money listener.
 	    Spinner spinner3 = (Spinner) findViewById(R.id.spinnerEa3);
         ArrayAdapter<Currency> adapter3 = new ArrayAdapter<Currency>(this, R.layout.spinneritem, R.id.spinneritem, currencies);
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner3.setAdapter(adapter3);
 	    if(DataStorage.typesOfCurrency.size() > 0){
 	    	moneylistener = new MoneyListener(changable, poscon);
 	    	spinner3.setOnItemSelectedListener(moneylistener);
 	    }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_earn, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     protected void onStop(){
     	super.onStop();
     }
     
     public void returnBack(View view){
     	this.finish();
     }
 }
