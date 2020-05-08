 package budgetapp.main;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import budgetapp.graph.GraphActivity;
 import budgetapp.util.BudgetDataSource;
 import budgetapp.util.BudgetEntry;
 import budgetapp.util.CategoryEntry;
 import budgetapp.util.DayEntry;
 import budgetapp.util.TransactionCommand;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.content.Context;
 import android.content.Intent;
 public class MainActivity extends Activity implements OnItemSelectedListener{
 
 	
 	TransactionCommand tempCom;
 	public static BudgetDataSource datasource;
 	int currentBudget = 0;
 	private String currentBudgetFileName = "current_budget"; // Internal file for current budget
 	private boolean logData = true; // If transactions should be logged
 	private int dailyBudget = 200; // The daily plus
 	public ArrayList<String> allCategories = new ArrayList<String>();
 	int min(int a,int b) 
 	{
 		if(a<b)
 			return a;
 		return b;
 	}
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         try
         {
         	 DataInputStream in = new DataInputStream(openFileInput(currentBudgetFileName));
              try
              {
             	 String strLine = in.readUTF();
             	 currentBudget = Integer.parseInt(strLine);
            	
            	  //Close the input stream
            	  in.close();
            	  
              	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
              	//EditText resultText = (EditText)findViewById(R.id.editTextSubtract);
              	//	resultText.requestFocus();
              	newBudget.setText(""+currentBudget);
              	updateColor();
             	 
              }
              catch(IOException e)
              {
             	 currentBudget=0;
              }
         }
         catch(NumberFormatException e)
         {
         	currentBudget=0;
         }
         catch(FileNotFoundException e)
         {
         	currentBudget=0;
         }
       
         	  
         datasource = new BudgetDataSource(this);
         datasource.open();
         
         updateSpinner();
         // Add daily budget for all days since last run
         addToBudget();
         //List all budget entries
         updateLog();
        
     }
     
     public void updateColor()
     {
     	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
     	if(currentBudget<0)
     		newBudget.setTextColor(Color.rgb(255,255-min(255,Math.abs(currentBudget/5)),255-min(255,Math.abs(currentBudget/5))));
     	else
     		newBudget.setTextColor(Color.rgb(255-min(255,Math.abs(currentBudget/5)),255,255-min(255,Math.abs(currentBudget/5))));
 	  
     }
     public void updateSpinner()
     {
     	// Get the categories for the Spinner
         List<CategoryEntry> categories = datasource.getAllCategories();
         // Put the category names in an ArrayList to get them into the spinner
         for(int i=0;i<categories.size();i++)
         {
         	allCategories.add(categories.get(i).getCategory());
         }
         
         Spinner spinner = (Spinner) findViewById(R.id.categories_spinner);
 		 // Create an ArrayAdapter using the string array and a default spinner layout
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, allCategories);
 	   
 	     // Specify the layout to use when the list of choices appears
 		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		 // Apply the adapter to the spinner
 		 spinner.setAdapter(adapter);
 		 spinner.setOnItemSelectedListener(this);
 			
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void updateLog()
     {
     	List<BudgetEntry> entries = datasource.getSomeTransactions(5);
         TextView temp = (TextView)findViewById(R.id.textViewLog);
         temp.setText("");
         for(int i=0;i<entries.size();i++)
         {	
         	if(i>=0)
         		temp.append(entries.get(i).getDate() + ":    " + entries.get(i).getValue() + "\t\t\t" + entries.get(i).getCategory() +  "\n");
         }
        
         List<CategoryEntry> categories = datasource.getAllCategories();
         temp.append("\n\n");
         for(int i=1;i<categories.size();i++) // Don't print out "Choose category"
         {	
         		temp.append(categories.get(i)+ ": "+ categories.get(i).getNum() + "\t\t\t\t");
         		temp.append("Sum: "+categories.get(i).getTotal()+"\n");
         }
         List<DayEntry> days = datasource.getAllDays();
         temp.append("\n\n");
         for(int i=0;i<days.size();i++) 
         {	
         		temp.append(days.get(i).getDate()+ ": ");
         		temp.append(days.get(i).getTotal()+"\n");
         }
     }
     
     
     public void subtractFromBudget(View view,String theCategory) {
        
     	EditText resultText = (EditText)findViewById(R.id.editTextSubtract);
     	String result = resultText.getText().toString();
     	
     	try
     	{
     		 
     		int resultInt = Integer.parseInt(result);
         	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
         	currentBudget-=resultInt;
         	newBudget.setText(""+currentBudget);
         	
         	// Add to database if logging is set
         	if(logData)
         	{
 	        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 	        	Calendar cal = Calendar.getInstance();
 	        	
 	        	BudgetEntry entry = new BudgetEntry(resultInt*-1, dateFormat.format(cal.getTime()),theCategory);
 	        	tempCom = new TransactionCommand(datasource,entry);
 	        	tempCom.execute();
 	        	
         	}
         	//Set color
         	updateColor();
         	resultText.setText("");
         	updateLog();
     		saveToFile(); // Save budget to file
     		
     	}
     	catch(NumberFormatException e)
     	{
     		System.out.println("Error: "+e);
     	}
     	
     		
     }
     
     // Adds the daily plus sum for all days missing since the last time the program was run
     public void addToBudget()
     {
     	List<DayEntry> lastDay = datasource.getSomeDays(1);
     //	datasource.database.delete(BudgetDatabase.TABLE_CASHFLOW, BudgetDatabase.COLUMN_ID + " = 79", null);
    
     	
    	if(lastDay!=null)
     	{
     		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 	    	
 	    	String lastDayString = lastDay.get(0).getDate();
 	    	Calendar lastDayCalendar = Calendar.getInstance();
 	    	// Convert the string to a Calendar time. Subtract 1 from month because month 0 = January
 	    	lastDayCalendar.set(Integer.parseInt(lastDayString.substring(0, 4)),Integer.parseInt(lastDayString.substring(5, 7))-1,Integer.parseInt(lastDayString.substring(8, 10)));
 	    	System.out.println("Last day: " + dateFormat.format(lastDayCalendar.getTime()));
 	    	lastDayCalendar.add(Calendar.DAY_OF_MONTH, 1); // We want to start counting from the first day without transactions
 
 	    	// Step up to the day before tomorrow
 	    	Calendar nextDay = Calendar.getInstance();
 	    	nextDay.roll(Calendar.DAY_OF_MONTH,1);
 	    	
 	    	System.out.println("Next day: " + dateFormat.format(nextDay.getTime()));
 	    	Calendar tempDate = (Calendar)lastDayCalendar.clone();
 	    	while(tempDate.before(nextDay))
 	    	{
 	    		if(!dateFormat.format(tempDate.getTime()).equalsIgnoreCase(dateFormat.format(nextDay.getTime())))
 	    		{
 	    			System.out.println("Day to add: " + dateFormat.format(tempDate.getTime()));
 	    			BudgetEntry entry = new BudgetEntry(dailyBudget, dateFormat.format(tempDate.getTime()),"Income");
 		        	tempCom = new TransactionCommand(datasource,entry);
 		        	tempCom.execute();
 		        	currentBudget+=dailyBudget;
 		        	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
 		        	newBudget.setText(""+currentBudget);
 	    		}
 	    		
 	    		tempDate.roll(Calendar.DAY_OF_MONTH,1);	
 	    	}
 	    	saveToFile();
     		
     	}
     	
     	
     }
     
     // Saves the current budget to file
     public void saveToFile()
     {
     	DataOutputStream out;
 		try {
 			out = new DataOutputStream(openFileOutput(currentBudgetFileName,Context.MODE_PRIVATE));
 			out.writeUTF(""+currentBudget);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
 
     	DialogFragment newFragment;
         switch (item.getItemId()) {
 	        case R.id.menu_undo:
 	        	if(tempCom!=null && tempCom.unexecute()==true)
 	        	{
 		        	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
 		        	currentBudget-=tempCom.getEntry().getValue();
 		        	newBudget.setText(""+currentBudget);
 		        	try
 		        	{
 			        	DataOutputStream out = new DataOutputStream(openFileOutput(currentBudgetFileName,Context.MODE_PRIVATE));
 			    		out.writeUTF(""+currentBudget);
 		        	}
 		        	catch(IOException e)
 		        	{
 		        		System.out.println("Error: "+e);
 		        	}
 		        	updateLog();
 		        	updateColor();
 	        	}
 	        	return true;
 	        	
             case R.id.menu_logdata: // Change logging data status
                 logData=!logData;
                 item.setChecked(logData);
                 return true;
             case R.id.menu_addcategory:
             	newFragment = new AddCategoryDialogFragment();
                 newFragment.show(getFragmentManager(), "add_category");
                 return true;
             case R.id.menu_removecategory:
             	newFragment = new RemoveCategoryDialogFragment();
             	newFragment.show(getFragmentManager(), "remove_category");
             	return true;
             case R.id.menu_showgraph:
             	Intent intent = new Intent(this,GraphActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 	{
 		//Toast.makeText(parent.getContext(), "The planet is " +parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
 		String theCategory = parent.getItemAtPosition(pos).toString();
 		if(pos!=0)
 		{
 			subtractFromBudget(parent,theCategory);
 		}
 		parent.setSelection(0); // Reset to "Choose category"
 		
 	}
 
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		
 		
 	
 	}
     
 }
