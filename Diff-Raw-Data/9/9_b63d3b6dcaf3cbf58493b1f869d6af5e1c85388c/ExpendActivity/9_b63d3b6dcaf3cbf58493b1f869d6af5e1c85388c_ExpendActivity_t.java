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
  * Class represents the Expenses option of the Main Menu. Responsible for allowing the user to make a "purchase"
  * Very similar to EarnActivity, hence, less comments, because implementation is mostly the same.
  */
 
 public class ExpendActivity extends Activity {
 	
     CategoryListener categorylistener;
     MoneyListener moneylistener;
     WalletListener walletlistener;
     DataStorage GlobalData = new DataStorage();
     
     Wallet[] wallets;
     Category[] categories;
     Currency[] currencies;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         wallets = new Wallet[DataStorage.listOfWallets.size()];
         for(int i = 0; i < wallets.length; i++)
         	wallets[i] = DataStorage.listOfWallets.get(i);
         
         categories = new Category[DataStorage.listOfCategories.size()];
         for(int i = 0; i < categories.length; i++)
         	categories[i] = DataStorage.listOfCategories.get(i);
         
         currencies = new Currency[DataStorage.typesOfCurrency.size()];
        for(int i = 0; i < currencies.length; i++)
         	currencies[i] = DataStorage.typesOfCurrency.get(i);
         
         setContentView(R.layout.activity_expend);
         
         TextView changable = (TextView) findViewById(R.id.textViewEx3);
         PositionContainer poscon = new PositionContainer();
                         
         Spinner spinner = (Spinner) findViewById(R.id.spinnerEx1);
         ArrayAdapter<Wallet> adapter = new ArrayAdapter<Wallet>(this, R.layout.spinneritem, R.id.spinneritem, wallets);
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(adapter);
 	    if(DataStorage.listOfWallets.size() > 0){
 	    	walletlistener = new WalletListener(changable, poscon);
 	    	spinner.setOnItemSelectedListener(walletlistener);
 	    }
 	    
 	    Spinner spinner2 = (Spinner) findViewById(R.id.spinnerEx2);
 	    ArrayAdapter<Category> adapter2 = new ArrayAdapter<Category>(this, R.layout.spinneritem, R.id.spinneritem, categories);
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner2.setAdapter(adapter2);
 	    if(DataStorage.listOfCategories.size() > 0){
 		    categorylistener = new CategoryListener();
 		    spinner2.setOnItemSelectedListener(categorylistener);
 	    }
 	    
 	    Spinner spinner3 = (Spinner) findViewById(R.id.spinnerEx3);
 	    ArrayAdapter<Category> adapter3 = new ArrayAdapter<Category>(this, R.layout.spinneritem, R.id.spinneritem, categories);
 	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner3.setAdapter(adapter3);
 	    if(DataStorage.typesOfCurrency.size() > 0){
 	    	moneylistener = new MoneyListener(changable, poscon);
 	    	spinner3.setOnItemSelectedListener(moneylistener);
 	    }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_expend, menu);
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
