 package ku.mobilepos.activity;
 
 import java.util.List;
 
 import ku.mobilepos.controller.CartController;
 import ku.mobilepos.controller.InventoryController;
 import ku.mobilepos.domain.Cart;
 import ku.mobilepos.domain.Inventory;
 
 
 import com.example.mobilepos.R;
 
 import android.R.integer;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SaleSelectItemActivity extends Activity {
 	private Button cancelButton;
 	private Button searchButton;
 	private EditText search;
 	private ListView itemInventory;
 	//private ListView itemInInventory;
 	private Inventory inventory;
 	private String[] inventoryListStringArr;
 	private String[] inventoryString;
 	private Cart cart;
 	private int itemPosition;
 	
 	private List<CartController> itemInCart;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sale_item_in_inventory);
 		cart = Cart.getCartInstance();
 		itemInCart = cart.getItemListInCart();
 		
 		// Button
 		cancelButton = (Button)findViewById(R.id.sale_iii_b_cancel);	
 		searchButton = (Button) findViewById(R.id.sale_iii_search);
 		
 		// Text view
 		search = (EditText) findViewById(R.id.sale_iii_searchText);
 		
 		inventory = InventoryController.getInstance();  
 		//createItemListStringArr();
 		
 		cancelButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				Intent goSaleMain = new Intent(getApplicationContext(),SaleMainActivity.class);
     			startActivity(goSaleMain);
 			}
 		});
 	
 	
 		searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(search.getText().toString().equals(""))
 				{
 					Toast.makeText(getApplicationContext(),"Please enter product ID.", Toast.LENGTH_LONG)
 	  		        .show();
 				}
 				else
 				{
 					if (inventory.getItemList().size()!=0){
 			    		inventoryListStringArr = new String[inventory.getItemList().size()];
 			    		inventoryString = new String[1];
 			    		boolean haveProduct = false;
 			    		for (int i = 0; i < inventoryListStringArr.length; i++) {
 			    			if(search.getText().toString().equals(inventory.getItemList().get(i).getItemId()))
 			    			{
 			    				itemPosition = i;
 			    				inventoryString[0] = "Product name: " + inventory.getItemList().get(i).getItemName()+"\nQuantity: "+inventory.getItemList().get(i).getItemQnty();
 			    				haveProduct = true;
 			    				break;
 			    			}
 			    		}
 			    					    		
 			    		if(haveProduct == false)
 			    		{
 			    			Toast.makeText(getApplicationContext(),"Inventory don't have this product.", Toast.LENGTH_LONG)
 			  		        .show();
 			    		}
 			    		
 			    		createItemString(inventoryString);
 					}
 				}
 			}
 		});
 	}
 	
 	public void createItemString(String[] item){
         	
     	itemInventory = (ListView)findViewById(R.id.sale_iii_item);
     	ArrayAdapter<String> itemListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, inventoryString);
     	itemInventory.setAdapter(itemListAdapter); 
     	itemInventory.setOnItemClickListener(new OnItemClickListener() {
 
     		//check the item on inventory and add to cart
     		@Override
     		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			
    			cart.addToCart(inventory.getItemByPostion(itemPosition));
     			int updateQuantity = inventory.getItemList().get(itemPosition).getItemQnty() - 1;
     			if(updateQuantity >= 0)
     			{
     				inventory.getItemList().get(itemPosition).setItemQnty(Integer.toString(updateQuantity));
     				// Show Alert 
     				Toast.makeText(getApplicationContext(), "Add "+ inventory.getItemList().get(itemPosition).getItemName() +" to Sale list\n" + updateQuantity, Toast.LENGTH_LONG)
     				.show();			
     			}
     			else
     			{
     				updateQuantity++;
     				Toast.makeText(getApplicationContext(), "Your inventory don't have this product", Toast.LENGTH_LONG)
     				.show();	
     			}
     		}
     	});
     }
 }
