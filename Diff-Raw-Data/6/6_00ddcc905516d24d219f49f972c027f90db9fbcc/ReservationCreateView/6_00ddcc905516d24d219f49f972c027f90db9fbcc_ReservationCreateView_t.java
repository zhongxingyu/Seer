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
 	protected static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm a", Locale.getDefault());
 	protected static Button datePicker;
 	protected static Button timePicker;
 
 	/* input Controls */
 	private ListView list;
 	private TextView restaurant;
 	private EditText nbrReservation;
 
 	/* adapters */
 	private ArrayAdapter<String> adapter; // list of dishes
 
 	/* intent keys */
 	private static final String RESTAURANT = "restoId"; // intent's key - restoID
 	private static final String DISH = "dishId"; 		// intent's key - dishID
 
 	/* list of dishes from resto */
 	private ArrayList<Integer> dish_id_list;
 	private List<String> dish_name_list;
 	private static int resto_id;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reservation_edit);
 
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
 
 		/* Receive the data */
 		getDataTransfer(getIntent());
 
 		dish_name_list = new ArrayList<String>();
 		
 		/* adapter for the list of dishes */
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, dish_name_list);
 		list.setAdapter(adapter);
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
 	    startActivityForResult(intent, 1);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		/* add the id into the list of dishes */
 		dish_id_list.add(resultCode);
 
 		/* update the adapter*/
 		String name = Dish.getDish(resultCode).getName();
 		dish_name_list.add(name);
 		adapter.notifyDataSetChanged();
 
 		/* Display a toast */
 		Context context = getApplicationContext();
 		Toast toast = Toast.makeText(context, "plat ajout", Toast.LENGTH_SHORT);
 		toast.show();
 	}
 	
 	/**
 	 * onClick Behavior - button SEND
 	 * @param view
 	 */
 	public void sendReservation(View view)
 	{
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
 		catch (Exception e) {exception = true;	text = "Nbr inrempli";}
 
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
 		}
 
 		/* if all the insertion into the dataBase passed */
 		if (!exception) {text = "Reservation envoye";}
 
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
 
 		if ((dish_id == 0 && resto_id == 0) || (dish_id != 0 && resto_id != 0)) {finish();}
 		else if (dish_id != 0)
 		{
			/* add item to the dish_name_list */
			dish_name_list.add(Dish.getDish(dish_id).getName());
 			adapter.notifyDataSetChanged();
 			
 			/* add to the list of dishId */
 			dish_id_list.add(dish_id);
 
 			/* get restoID */
 			resto_id = Dish.getDish(dish_id).getRestoId();
 		}
 
 		/* display the restaurant's name */
 		restaurant.setText(Restaurant.getRestaurant(resto_id).getName());
 	}
 }
