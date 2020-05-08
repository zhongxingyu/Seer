 package com.thinksoft.login;
 
 import static com.thinksoft.businesslayer.utils.constants.Constants.WRONG_DATA;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.thinksoft.businesslayer.bussinessmanagers.BusinessManager;
 import com.thinksoft.businesslayer.bussinessmanagers.impl.BusinessManagerImpl;
 import com.thinksoft.models.databases.PolAppHelper;
<<<<<<< HEAD
=======
<<<<<<< Updated upstream
<<<<<<< HEAD
=======
=======
import com.thinksoft.models.dtos.Brand;
import com.thinksoft.models.dtos.Client;
import com.thinksoft.models.dtos.Product;
>>>>>>> Stashed changes
import com.thinksoft.models.dtos.User;
import com.thinksoft.models.dtos.Vehicle;
import com.thinksoft.models.dtos.impl.BrandImpl;
import com.thinksoft.models.dtos.impl.ClientImpl;
import com.thinksoft.models.dtos.impl.ProductImpl;
import com.thinksoft.models.dtos.impl.UserImpl;
<<<<<<< Updated upstream
>>>>>>> a56e7822a73b1fc5d3a23b832e122fcc1ac947e7
=======
import com.thinksoft.models.dtos.impl.VehicleImpl;
>>>>>>> Stashed changes
>>>>>>> 4532bdaeee6120d32875e09fcbf6ede2aedba166
 import com.thinksoft.polapp.HomeScreenActivity;
 import com.thinksoft.polapp.R;
 public class SignIn extends OrmLiteBaseActivity<PolAppHelper> {
 
 	private BusinessManager bussinnessLayer;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sign_in);
 		bussinnessLayer = new BusinessManagerImpl(getHelper() .getConnectionSource());
 		/*
 		java.util.Date d = new Date();
 		java.text.DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
 		
 		try {
 			d = df.parse("07/15/2010");
 		} catch (java.text.ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 
 		
 		User user = new UserImpl("admin", "123", "1", "Administrator", "Polaco", "Application");
 		bussinnessLayer.addUser(user);
 		Client client = new ClientImpl("Jonathan","Ruiz","Fallas",true);
 		bussinnessLayer.addClient(client);
<<<<<<< Updated upstream
 		pro= new ProductImpl("TELEV", "Samsung LED TV 32'", 14000000, 5);
=======
		Product pro = new ProductImpl("TELEV", "Samsung LED TV 32'", 14000000, 5);
>>>>>>> Stashed changes
 		bussinnessLayer.addProduct(pro);
 		Brand brand= new BrandImpl("Honda");
 		bussinnessLayer.addBrand(brand);
 		Vehicle veh= new VehicleImpl("5616423", true, d, 5161, "YVR", (BrandImpl) brand);
 		bussinnessLayer.addVechicle(veh);
<<<<<<< Updated upstream
 		*/
=======

>>>>>>> Stashed changes
 		final EditText username = (EditText) findViewById(R.id.txtUserNameSignIn);
 		final EditText password = (EditText) findViewById(R.id.txtPasswordSignIn);
 		TextView signUp = (TextView) findViewById(R.id.lblSignUp);
 		Button btnEntrar = (Button) findViewById(R.id.btnSignIn);
 
 		signUp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(SignIn.this, SignUp.class);
 				startActivity(intent);
 			}
 		});
 
 		btnEntrar.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String usernameString = username.getText().toString();
 				String passString = password.getText().toString();
 					if (bussinnessLayer.checkUserCredentials(usernameString,passString)) {
 						Intent intent = new Intent(SignIn.this, HomeScreenActivity.class);
 						startActivity(intent);
 					} else {
 						Toast error = Toast.makeText(SignIn.this,WRONG_DATA,Toast.LENGTH_LONG);
 						error.show();
 					}
 				}
 			});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.sign_in, menu);
 		return true;
 	}
 	
 }
