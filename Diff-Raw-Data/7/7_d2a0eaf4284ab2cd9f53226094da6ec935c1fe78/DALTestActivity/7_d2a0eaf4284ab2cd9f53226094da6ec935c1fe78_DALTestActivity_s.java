 package il.ac.huji.chores.dal;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.parse.Parse;
 import com.parse.ParseACL;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 
 import il.ac.huji.chores.ApartmentChore;
 import il.ac.huji.chores.Chore;
 import il.ac.huji.chores.ChoreInfo;
 import il.ac.huji.chores.Chore.CHORE_STATUS;
 import il.ac.huji.chores.ChoreInfo.CHORE_INFO_PERIOD;
 import il.ac.huji.chores.ChoreInfoInstance;
 import il.ac.huji.chores.R;
 import il.ac.huji.chores.R.layout;
 import il.ac.huji.chores.R.menu;
 import il.ac.huji.chores.RoommatesApartment;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import il.ac.huji.chores.RoommatesApartment;
 import il.ac.huji.chores.exceptions.ApartmentAlreadyExistsException;
 import il.ac.huji.chores.exceptions.DataNotFoundException;
 import il.ac.huji.chores.exceptions.FailedToAddChoreInfoException;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 
 public class DALTestActivity extends Activity {
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		setContentView(R.layout.activity_daltest);
 		Parse.initialize(this,
 				this.getResources().getString(R.string.parse_app_id), this
 						.getResources().getString(R.string.parse_client_key));
 		ParseACL defaultACL = new ParseACL();
 		defaultACL.setPublicReadAccess(true);
 		ParseACL.setDefaultACL(defaultACL, true);
 		// String roommateID =RoommateDAL.createRoommateUser("anna",
 		// "anna123","anna@gmail.com");
 		RoommateDAL.Login("anna", "anna123");
 		Chore chore = new ApartmentChore(null, "anna's chore", ParseUser.getCurrentUser().getObjectId(), new Date(), new Date(), CHORE_STATUS.STATUS_FUTURE, null, null, null, 3);
 		String choreID;
 		try {
 			choreID = ChoreDAL.addChore(chore);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		Chore choreResult  = ChoreDAL.getChore(choreID);
		List<Chore> allChores = ChoreDAL.getAllChores();
		
 		/*
 		 * String apartmentID; try { apartmentID = RoommateDAL.getApartmentID();
 		 * } catch (UserNotLoggedInException e1) { apartmentID = null; }
 		 */
 	/*	ChoreInfoInstance chore = new ChoreInfoInstance();
 		chore.setChoreInfoName("Anna's chore");
 		chore.setCoins(3);
 		chore.setIsEveryone(true);
 		chore.setHowMany(2);
 		chore.setPeriod(ChoreInfo.CHORE_INFO_PERIOD.CHORE_INFO_MONTH);
 		String choreId;*/
 		/*try {
 			// choreId = ChoreDAL.addChoreInfo(chore);
 			/*
 			 * ChoreDAL.updateChoreInfoName("jQY3c2QQ8o", "YOAV'S CHORE");
 			 * ChoreDAL.updateCoins("jQY3c2QQ8o", 4);
 			 * ChoreDAL.updateFrequency("jQY3c2QQ8o",1);
 			 * ChoreDAL.updatePeriod("jQY3c2QQ8o",
 			 * CHORE_INFO_PERIOD.CHORE_INFO_WEEK);
 			 *
 			List<ChoreInfo> list = ChoreDAL.getAllChoreInfo();
 
 		} catch (UserNotLoggedInException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} */
 		/*
 		 * RoommatesApartment apt = new RoommatesApartment();
 		 * apt.setName("apt");
 		 */
 		/*
 		 * List<String> roommates = new ArrayList<String>(); //try {
 		 * //apartmentID = ApartmentDAL.createApartment(apt); try {
 		 * ApartmentDAL.addRoommateToApartment(apartmentID); } catch
 		 * (UserNotLoggedInException e) { // TODO Auto-generated catch block
 		 * e.printStackTrace(); } //roommates =
 		 * ApartmentDAL.getApartmentRoommates(apartmentID); //}// catch
 		 * (ApartmentAlreadyExistsException e) { //
 		 * Log.e("ApartmentAlreadyExistsException", e.toString()); //} catch
 		 * (UserNotLoggedInException e) { // TODO Auto-generated catch block
 		 * //e.printStackTrace(); //}
 		 *catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.daltest, menu);
 		return true;
 	}
 
 }
