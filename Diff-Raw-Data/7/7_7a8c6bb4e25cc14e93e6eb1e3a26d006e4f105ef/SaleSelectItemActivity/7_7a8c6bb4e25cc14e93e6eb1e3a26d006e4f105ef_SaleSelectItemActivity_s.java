 package ku.mobilepos.activity;
 
 import java.util.List;
 
 import ku.mobilepos.domain.Cart;
 import ku.mobilepos.domain.Inventory;
 import ku.mobilepos.domain.Item;
 import ku.mobilepos.domain.MockupInventory;
 
 
 import com.example.mobilepos.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SaleSelectItemActivity extends Activity {
 	private Button cancelButton;
 	private ListView itemInInventory;
 	private Inventory inventory;
 	private String[] inventoryListStringArr;
 	private Cart cart;
 	private List<Item> itemInCart;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sale_item_in_inventory);
 		cart = Cart.getCartInstance();
 		itemInCart = cart.getItemListInCart();
 		cancelButton = (Button)findViewById(R.id.sale_iii_b_cancel);	
 		inventory = MockupInventory.getInstance();  
 		createItemListStringArr();
 		
 		cancelButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				Intent goSaleMain = new Intent(getApplicationContext(),SaleMainActivity.class);
     			startActivity(goSaleMain);
 			}
 		});
 	}
 	//create the item list
 	public void createItemListStringArr(){
     	if (inventory.getItemList().size()!=0){
     		inventoryListStringArr = new String[inventory.getItemList().size()];
     		for (int i = 0; i < inventoryListStringArr.length; i++) {
    			inventoryListStringArr[i] =  inventory.getItemList().get(i).getItemName();
     		}
         	
     		itemInInventory = (ListView)findViewById(R.id.sale_iii_itemlist);
     		ArrayAdapter<String> itemListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, inventoryListStringArr);
     		itemInInventory.setAdapter(itemListAdapter); 
     		itemInInventory.setOnItemClickListener(new OnItemClickListener() {
 
     			//check the item on inventory and add to cart
     			@Override
     			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     				cart.addToCart(inventory.getItemList().get(position));
    				String  itemValue = (String)itemInInventory.getItemAtPosition(position);
     				// Show Alert 
    				Toast.makeText(getApplicationContext(), "Add: "+itemValue+" to Cart", Toast.LENGTH_LONG)
     				.show();			
     			}
     		});
     	}
     }
 }
