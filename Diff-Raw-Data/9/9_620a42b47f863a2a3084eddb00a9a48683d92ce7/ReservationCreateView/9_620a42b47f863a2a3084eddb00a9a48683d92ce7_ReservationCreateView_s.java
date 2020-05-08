 package be.uclouvain.sinf1225.gourmet;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.models.Reservation;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.models.User;
 
 public class ReservationCreateView extends Activity
 {
 	/* date and time */
 	protected static Calendar dateTime = Calendar.getInstance();
 	// TODO check if we should use SimpleDateFormat.getDate/TimeInstance() or not.
 	protected static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
 	protected static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
 	protected static Button datePicker;
 	protected static Button timePicker;
 
 	/* input Controls */
 	private ListView list;
 	private TextView restaurant;
 	private EditText nbrReservation;
 	private TextView price_view;
 
 	/* adapters */
 	private ArrayAdapter<String> adapter; // list of dishes
 
 	/* intent keys */
 	private static final String RESTAURANT = "restoId"; // intent's key - restoID
 	private static final String DISH = "dishId"; 		// intent's key - dishID
 
 	/* list of dishes from resto */
 	private ArrayList<Integer> dish_id_list;
 	private List<String> dish_name_list;
 	private static int resto_id;
 	
 	/* Price */
 	private double price;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reservation_edit);
 
 		/* if the date is incorrect, set 08:00:00 */
 		if (dateTime.get(Calendar.HOUR_OF_DAY) < 8) 
 			{
 				dateTime.set(Calendar.HOUR_OF_DAY, 8);
 				dateTime.set(Calendar.MINUTE,0);
 				dateTime.set(Calendar.SECOND,0);
 			}
 		
 		/* date picker - choose the date of the reservation */
 		datePicker = (Button) findViewById(R.id.datepicker);
 		datePicker.setText(dateFormatter.format(dateTime.getTime()));
 
 		/* date picker - choose the hour of the reservation */
 		timePicker = (Button) findViewById(R.id.timepicker);
 		timePicker.setText(timeFormatter.format(dateTime.getTime()));
 
 		/* input items */
 		list = (ListView) findViewById(R.id.listDish);
 		restaurant = (TextView) findViewById(R.id.restaurant);
 		nbrReservation = (EditText) findViewById(R.id.nbrReservation);
 		price_view = (TextView) findViewById(R.id.price);
 
 		dish_name_list = new ArrayList<String>();
 		dish_id_list = new ArrayList<Integer>();
 
 		/* adapter for the list of dishes */
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, dish_name_list);
 		list.setAdapter(adapter);
 		
 		/* Receive the data */
 		getDataTransfer(getIntent());
 		
 		nbrReservation.setHint("Nbr max ("+ Restaurant.getRestaurant(resto_id).getSeats() + ")");
 		
 		list.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				int ID = dish_id_list.remove(position);		/* remove the dish into the list of dish_id */
 				dish_name_list.remove(position); 			/* remove the dish into the listView */
 				adapter.notifyDataSetChanged(); 			/* update the adapter */
 
 				Dish dish = Dish.getDish(ID);
 				
 				/* set the price */
 				price -= dish.getPrice();
 				price_view.setText("" + Double.toString( Math.round(price*100)/100.0) + " \u20ac");
 
 				/* increment the values of available */
 				dish.setAvailable(dish.getAvailable() + 1);
 				dish.updateDish();
 			}
 		});
 		
 	}
 
 	public void showTimePickerDialog(View v)
 	{
 		DialogFragment newFragment = new TimePickerFragment();
 		newFragment.show(getFragmentManager(), "timePicker");
 	}
 
 	public void showDatePickerDialog(View v)
 	{
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 	/**
 	 * OnClick Behavior - button ADD
 	 * @param v
 	 */
 	public void addItems(View v)
 	{
 		/* launch the activity with the list of dishes*/
 		Intent intent=new Intent(this,DishListView.class);
 		intent.putExtra("restoId", resto_id);
 		intent.putExtra("key","ComeBack");
 	    startActivityForResult(intent, 1);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		Context context = getApplicationContext();
 		Toast toast;
 
 		/* add the id into the list of dishes */
 		int dish_id = data.getIntExtra("dishId",0);
 
 		if (dish_id == 0) toast = Toast.makeText(context, "aucun plat ajout", Toast.LENGTH_SHORT);
 		else
 		{
 			/* add the dish into the list of dish_id */
 			dish_id_list.add(dish_id);
 
 			Dish dish = Dish.getDish(dish_id);
 			
 			/* add the dish into the listView */
 			dish_name_list.add(dish.getName());
 			
 			/* set price */
 			price += dish.getPrice();
 			price_view.setText("" + Double.toString( Math.round(price*100)/100.0) + " \u20ac");
 			
 			/* update the adapter*/
 			adapter.notifyDataSetChanged();
 
 			toast = Toast.makeText(context, "plat ajout", Toast.LENGTH_SHORT);
 		}
 		toast.show();
 	}
 
 	/**
 	 * onClick Behavior - button SEND
 	 * @param view
 	 */
 	public void sendReservation(View view)
 	{
 		System.out.println("SENDING RESERVATION");
 		
 		/* variables for the toast */
 		Context context = getApplicationContext();
 		String text = "";
 		boolean exception = false;
 		int nbrResv = 0;
 
 		/* Variables for the reservation */
 		String email = User.getUserConnected().getEmail();
 		Date date = dateTime.getTime();
 
 		/* Manage Exception */
 		try{nbrResv = Integer.parseInt(nbrReservation.getText().toString());}
 		catch (Exception e) {exception = true;	text = "Nbr non rempli";}
 		
 		if(!exception && nbrResv > Restaurant.getRestaurant(resto_id).getSeats())
 		{exception = true; text = "Nbr trop grand";}
 		if(!exception && nbrResv <= 0)
 		{exception = true; text = "Nbr incorrect";}
 
 		/* Check if the reservation does not exists yet */
 		if ( !exception && Reservation.getReservation(email, resto_id, date) != null)
 		{
 			exception = true; text = "Vous avez dj une rservation  cette date";
 		}
 
 		/* Create reservation */
 		if (!exception)
 		{
 			Reservation resv = new Reservation(email, Restaurant.getRestaurant(resto_id), nbrResv, date);			
 			for(int id : dish_id_list){resv.addDish(id);}
 			
 			/* add the reservation into the database */
			exception = ( Reservation.addReservation(resv)  == -1);
 			
 			/* check if the reservation passed */
 			if (exception) text = "Reservation choue";
 			else 
 			{
 				/* remove the places booked */
 				Restaurant resto = Restaurant.getRestaurant(resto_id);
 				resto.setSeats(resto.getSeats() - nbrResv);
 				resto.updateRestaurant();
 				
 				/* all the insertion into the dataBase passed */
 				text = "Reservation envoye";
 				
 				/* REFRESH ACTIVITY */
 				finish(); startActivity(getIntent());
 			}
 		}
 
 		/* display the toast */
 		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	/**
 	 * Get the data from the intent
 	 * @param intent
 	 */
 	public void getDataTransfer(Intent intent)
 	{
 		/* get intent values */
 		resto_id = intent.getIntExtra(RESTAURANT, 0);
 		int dish_id = intent.getIntExtra(DISH, 0);
 		price = 0;
 
 		if ((dish_id == 0 && resto_id == 0) || (dish_id != 0 && resto_id != 0)) {finish();}
 		else if (dish_id != 0)
 		{
 			/* add item to the dish_name_list */
 			dish_name_list.add(Dish.getDish(dish_id).getName());
 			adapter.notifyDataSetChanged();
 			
 			/* information for the user */
 			Context context = getApplicationContext();
 			Toast toast = Toast.makeText(context, "plat ajout", Toast.LENGTH_SHORT);
 			toast.show();
 			
 			/* add to the list of dishId */
 			dish_id_list.add(dish_id);
 
 			/* get restoID */
 			resto_id = Dish.getDish(dish_id).getRestoId();
 			
 			/* set the price */
 			price = Dish.getDish(dish_id).getPrice();
 		}
 
 		/* display the restaurant's name */
 		restaurant.setText(Restaurant.getRestaurant(resto_id).getName());
 		price_view.setText("" + Double.toString( Math.round(price*100)/100.0) + " \u20ac");
 	}
 }
