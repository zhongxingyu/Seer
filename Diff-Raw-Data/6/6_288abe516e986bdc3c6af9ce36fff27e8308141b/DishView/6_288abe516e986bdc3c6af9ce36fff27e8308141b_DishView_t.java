 package be.uclouvain.sinf1225.gourmet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.TextView;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.models.Image;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.models.Restaurator;
 import be.uclouvain.sinf1225.gourmet.models.User;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetFiles;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 
 /**
  * RestaurantView activity
  * 
  * @author Olivia Bleeckx
  */
 
 public class DishView extends Activity
 {
 	private Dish dish = null;
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 	    GourmetUtils.createMenu(menu, this, R.id.search);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return GourmetUtils.onMenuItemSelected(item, this);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dish_view);
 		
 		dish = Dish.getDish(getIntent().getExtras().getInt("dishId"));
 		Restaurant restaurant = dish.getRestaurant();
 
 		((TextView) findViewById(R.id.DishViewName)).setText(dish.getName());
 		((TextView) findViewById(R.id.DishViewDescription)).setText(dish.getDescription());
 		((TextView) findViewById(R.id.DishViewPrice)).setText("" + dish.getPrice());
 		
 		
 		// ((TextView)findViewById(R.id.RestaurantListDistance)).setText(new
 		// DecimalFormat("#.##").format(city.getLocation().distanceTo(locationListener.getLastLocation())/1000));
 		
 		final CheckBox ViewSpicy = (CheckBox) findViewById(R.id.DishViewSpicy);
 		final CheckBox ViewVegan = (CheckBox) findViewById(R.id.DishViewVegan);
 		final CheckBox ViewAvailable = (CheckBox) findViewById(R.id.DishViewAvailable);
 		
 		if (dish.getSpicy() == 1)
 			ViewSpicy.setChecked(true); // to be checked, true equals 1 or 0
 		else
 			ViewSpicy.setChecked(false);
 
 		if (dish.getVegan() == 1)
 			ViewVegan.setChecked(true);
 		else
 			ViewVegan.setChecked(false);
 
 		if (dish.getAllergen() == 1)
 			ViewAvailable.setChecked(true);
 		else
 			ViewAvailable.setChecked(false);
 		
 		Button menu = (Button) findViewById(R.id.DishViewReservation);
 		menu.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View arg0)
 			{
 				Intent intent = new Intent(DishView.this, ReservationCreateView.class);
 			    intent.putExtra("dishId", 1);
 			    startActivity(intent);
 			}
 		});
 		
 		Button editdish = (Button) findViewById(R.id.DishViewEdit);
 		User user = User.getUserConnected();
 		if(user instanceof Restaurator && ((Restaurator)user).hasRightsForRestaurant(restaurant))
 			editdish.setVisibility(View.VISIBLE);	
 		else
 			editdish.setVisibility(View.INVISIBLE);
 		
 		editdish.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View arg0)
 			{
 				Intent intent = new Intent(DishView.this, DishEditView.class);
 			    intent.putExtra("dishId", 1);
 			    startActivity(intent);
 			}
 		});
 		
 		Image image = dish.getImg();
 		ImageView imageView = (ImageView) findViewById(R.id.DishViewImage);
		if(image != null)
			imageView.setImageBitmap(BitmapFactory.decodeFile(GourmetFiles.getRealPath(image.getPath())));
		else
			imageView.setVisibility(ImageView.GONE);
 	}
 }
