 package ku.mobilepos.activity;
 
 import ku.mobilepos.controller.CustomerController;
 import ku.mobilepos.domain.Customer;
 import ku.mobilepos.domain.CustomerList;
 
 import com.example.mobilepos.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class CustomerAddNewCustomerActivity extends Activity {
 	/** name of customer */
 	private EditText cusName;
 	/** customer's phone number */
 	private EditText cusPhoneNo;
 	/** button to confirm for add product */
 	private Button confirmButton;
 	/** button to cancel product */
 	private Button cancelButton;
 
 	/** new customer **/
 	private Customer customerList;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.customer_addnewcus);
 
 		customerList = CustomerList.getInstance();
		setupAddNewCustomerActivity();
 		
 		confirmButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				CustomerController newCustomer = new CustomerController();
 				newCustomer.setCusName(cusName.getText().toString());
 				newCustomer.setCusPhoneNo(cusPhoneNo.getText().toString());
 				newCustomer.setCusId(customerList.getCustomerList().size());
 				customerList.addCustomer(newCustomer);
 				Intent goCusMain = new Intent(getApplicationContext(),
 						CustomerMainActivity.class);
 
 						startActivity(goCusMain);
 				
 			}
 		});
 
 		/**
 		 * when select cancel button it will go back to inventory page
 		 */
 		cancelButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent goCusMain = new Intent(getApplicationContext(),
 				CustomerMainActivity.class);
 
 				startActivity(goCusMain);
 			}
 		});
 	}
 	
 	public void setupAddNewCustomerActivity(){
 		cusName = (EditText) findViewById(R.id.customer_f_name);
 		cusPhoneNo = (EditText) findViewById(R.id.customer_addnew_f_phone);
		confirmButton = (Button) findViewById(R.id.customer_addnew_b_confirm);
		cancelButton = (Button) findViewById(R.id.customer_addnew_f_cancel);
 	}
 
 }
