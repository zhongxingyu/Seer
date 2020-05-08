 package ru.ododo.activities;
 
 import nc_project_team.nc_prototypeinterface.R;
 import nc_project_team.nc_prototypeinterface.R.id;
 import ru.ododo.logic.Variables;
 import ru.ododo.logic.SocNetAbstractFactory.AbstractFactory;
 import ru.ododo.logic.SocNetAbstractFactory.FB;
 import ru.ododo.logic.systemstate.SysSinglton;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.model.GraphUser;
 
 public class EnterToSystem extends Activity implements OnClickListener {
 
 
 
 	Button entryByVk;
 	Button entryByFb;
 	Button entryWithoutInt;
 	Intent intent;
 	AbstractFactory userFactory=null;
 	private static EnterToSystem activ;
 
 	public static EnterToSystem getActiv() {
 		return activ;
 	}
 
 
 	public static void setActiv(EnterToSystem activ) {
 		EnterToSystem.activ = activ;
 	}
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.enter_system);
 		setTitle(R.string.name_reg_activity);

 		activ=this;
 		

		if(SysSinglton.getInstance().isServiceRun()){
			startActivity(new Intent(this, MainMenu.class));
		}
		
 		entryByFb=(Button)findViewById(id.entryByFb);
 		entryByVk=(Button)findViewById(id.entryByVk);
 		entryWithoutInt=(Button)findViewById(id.entryWithoutInt);
 		
 		entryByFb.setOnClickListener(this);
 		entryByVk.setOnClickListener(this);	
 		entryWithoutInt.setOnClickListener(this);
 		
 		
 	}
 
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 		int id = v.getId();
 		if (id == R.id.entryByFb) {
 		Session.openActiveSession(this, 
 					true, new Session.StatusCallback() {
 						
 						@Override
 						public void call(Session session, SessionState state, Exception exception) {
 							// TODO Auto-generated method stub
 							
 							if(session.isOpened()){
 								
 								Request.newMeRequest(session, new Request.GraphUserCallback() {
 									
 									@Override
 									public void onCompleted(GraphUser user, Response response) {
 										// TODO Auto-generated method stub
 										if(user!=null){
 											FB.setFullName(user.getName());
 											FB.setUserId(user.getId());
 											userFactory=new FB();
 											SysSinglton.getInstance().createUser(userFactory);
 											startNewActivety();
 										}														
 									}
 								}).executeAsync();
 								Session.getActiveSession().closeAndClearTokenInformation();
 							}
 							
 						}
 					});
 		} else if (id == R.id.entryByVk){
 				intent=new Intent(this, EnterByVk.class);
 				intent.putExtra(Variables.NAME_OF_SOCIAL_NETWORK, "_VK");
 				startActivity(intent);
 		}
 		 else if(id==R.id.entryWithoutInt){
 				FB.setUserId("100500");
 				FB.setFullName("Andrey Krasnov");
 				userFactory=new FB();
 				SysSinglton.getInstance().createUser(userFactory);
 				intent=new Intent(this, MainMenu.class);
 				startActivity(intent);
 		}
 		
 
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		//Log.d(Settings.MY_TAG, "socNet Name:"+socNet);
 		if(Session.getActiveSession()!=null){
 			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 		}
 	}
 	public void startNewActivety(){
 		this.setVisible(false);
 		Intent intent=new Intent(this, MainMenu.class);
 		startActivity(intent);
 	}
 }
 
