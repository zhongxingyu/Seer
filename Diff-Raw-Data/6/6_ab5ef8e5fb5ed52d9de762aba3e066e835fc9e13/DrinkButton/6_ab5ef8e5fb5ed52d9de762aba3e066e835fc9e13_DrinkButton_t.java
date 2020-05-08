 package Hanes.testProj;
 
 /*
  * CSH Drink App by Andrew Hanes
  */
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class DrinkButton extends Button {
 	/*
 	 * drinkButton supports all of the Button methods, but is automatically set up
 	 * to provide the needed information
 	 */
 	String drink; //drink name
 	int price; //drink price
 	int count; //the number of this drink available
 	int slot;
 	Connector drinkServ;
 	Context soopaContext;
 	Drink_Main drinkMain;
 	public DrinkButton(Context context, String drink, int price, int count, int slot, Connector drinkServ, Drink_Main drinkMain)
 	{
 		super(context); 
 		this.soopaContext = context;
 		this.drink = drink;
 		this.price = price;
 		this.drinkMain = drinkMain;
 		this.count = count;
 		this.slot = slot;
 		this.drinkServ = drinkServ;
 		if( this.count > 0 )
 		{
 			this.setText(this.drink.substring(1,this.drink.length()-1)+" | " + this.price+" Credits | "+ this.count+ " left");
 		}
 		else
 		{
 			this.setEnabled(false);
 			this.setText("OUT OF STOCK - "+this.drink.substring(1,this.drink.length()-1)+" | " + this.price+" Credits");
 			this.setTextColor(Color.RED);
 		}
 		this.setOnClickListener(new OnClickListener(){
 			public void onClick(View v)
 			/*
 			 * Starts a new thread when order is created so that the phone
 			 * doesn't lag as much
 			 */
 			{
 				setEnabled(false);
 				order();
 				setEnabled(true);
 			}
 		});
 	}
 	public String getDrink()
 	/*
 	 * Returns the drink name
 	 */
 	{
 		return this.drink;
 	}
 	public int getPrice()
 	/*
 	 * Returns the drink price
 	 */
 	{
 		return this.price;
 	}
 	public int getCount()
 	/*
 	 * Returns the number of this drink
 	 */
 	{
 		return count;
 	}
 	public void order()
 	/*
 	 * This method handles dropping drinks
 	 */
 	{
 		final AlertDialog.Builder alert2 = new AlertDialog.Builder(soopaContext);
 		alert2.setTitle("Drop");
 		alert2.setMessage("Confirm drop of "+this.drink +"\nPrice = "+this.price);
 		alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				final AlertDialog.Builder alert3 = new AlertDialog.Builder(soopaContext);
 				alert3.setTitle("Drop");
 				alert3.setMessage("Dropping...");
 				alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 
 						ArrayList<String> back = drinkServ.command("DROP "+slot+" "+drinkMain.sp.getInt("delay", 0));	
 						if (back.get(0).indexOf("ERR") > -1)
 						{
							
 						}
 						else
 						{
 							drinkMain.displayAlert("Successful Drop");	
 							drinkMain.updateButtons();
 
 						}
 
 					}
 				});
 				ArrayList<String> back = drinkServ.command("DROP "+slot+" "+drinkMain.sp.getInt("delay", 0));	
 				if (back.get(0).indexOf("ERR") > -1)
 				{
					String s = back.get(0);
					drinkMain.displayAlert(s);
 				}
 				else
 				{
 					drinkMain.displayAlert("Successful Drop");	
 					drinkMain.updateButtons();
 
 				}
 
 			}
 		});
 		alert2.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 			}
 		});
 		alert2.show();
 
 	}
 }
