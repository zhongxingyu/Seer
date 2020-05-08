 package jieqoo.android.KASS.integration.test;
 
 import static jieqoo.android.KASS.test.Factory.createUser;
 import static jieqoo.android.KASS.test.Factory.signoutUser;
 import jieqoo.android.KASS.R;
import jieqoo.android.KASS.SettingsActivity;
 import jieqoo.android.KASS.SiginByWeiboActivity;
 import jieqoo.android.KASS.models.Account;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class UserSignsOutTests extends IntegrationBaseTests {
 
 	public UserSignsOutTests(String name) {
 		super(name);
 	}
 	
 	public UserSignsOutTests() {
 		this("UserSignsOutTests");
 	}
 	
 	public final void testSignsOut() throws JSONException {
 		JSONObject userJSON = createUser();
 		signoutUser(); // This is factory signout. Not what we are testing.
 
 		String email = userJSON.getString("email");
 		String password = userJSON.getString("password");
 
 		clickOnProfileMainTab();
 		clickOnSigninButton();
 		
 		solo.clearEditText(0);
 		solo.clearEditText(1);
 		solo.enterText(0, email);
 		solo.enterText(1, password);
 		
 		clickOnSigninButton();
 		
		solo.assertCurrentActivity("Settings activity", SettingsActivity.class);
 		assertTrue(Account.getInstance().isAuthenticated());
 
 		clickOnSignoutButton();
 		
 		solo.assertCurrentActivity("Back to sign in", SiginByWeiboActivity.class);
 		
 		assertFalse(Account.getInstance().isAuthenticated());
 	}
 }
