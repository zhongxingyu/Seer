 package com.imaginea.android.sugarcrm.restapi;
 
 import android.test.suitebuilder.annotation.SmallTest;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.ModuleFields;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 import com.imaginea.android.sugarcrm.util.SugarBean;
 
import java.util.HashMap;
import java.util.List;

 /**
  * ContactsApiTest, tests the rest api calls
  * 
  * @author chander
  * 
  */
 public class ContactDetailsApiTest extends RestAPITest {
 
 	String moduleName = "Contacts";
 
 	String[] fields = new String[] {};
 
 	String[] customFields = new String[] { "a", "b" };
 
 	private String[] mSelectFields = { ModuleFields.FIRST_NAME,
 			ModuleFields.LAST_NAME, ModuleFields.ACCOUNT_NAME,
 			ModuleFields.PHONE_MOBILE, ModuleFields.PHONE_WORK,
 			ModuleFields.EMAIL1 };
 
	HashMap<String, List<String>> mLinkNameToFieldsArray = new HashMap<String, List<String>>();
 
 	public final static String LOG_TAG = "ContactDetailsTest";
 
 	@SmallTest
 	public void testContactDetail() throws Exception {
 
 		String id = "c2dc2bf6-2845-f460-1a25-4c1f47c05b87";
 		SugarBean sBean = getSugarBean(id);
 		assertNotNull(sBean);
 		for (int i = 0; i < mSelectFields.length; i++) {
 			String fieldValue = sBean.getFieldValue(mSelectFields[i]);
 			Log.i(LOG_TAG, "FieldName:|Field value " + mSelectFields[i] + ":"
 					+ fieldValue);
 			assertNotNull(fieldValue);
 		}
 	}
 
 	/**
 	 * demonstrates the usage of RestUtil for contact detail. ModuleFields.NAME
 	 * or FULL_NAME is not returned by Sugar CRM. The fields that are not
 	 * returned by SugarCRM can be automated, but not yet generated
 	 * 
 	 * @param beanId
 	 * @return
 	 * @throws Exception
 	 */
 	private SugarBean getSugarBean(String beanId) throws Exception {
 		SugarBean sBean = RestUtil.getEntry(url, mSessionId, moduleName,
 				beanId, mSelectFields, mLinkNameToFieldsArray);
 		return sBean;
 	}
 
 }
