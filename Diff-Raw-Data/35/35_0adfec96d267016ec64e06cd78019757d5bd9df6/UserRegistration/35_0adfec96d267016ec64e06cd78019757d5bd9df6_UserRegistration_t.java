 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.ac;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
 
 /**
  * Dialog for user registration request
  * @author m.chini
  *
  */
 public class UserRegistration {
 	static AlertDialog.Builder builder;
	public static void upgradeuser(final Activity activity) {
			builder = new AlertDialog.Builder(activity);
 			final AMSCAccessProvider accessprovider = new AMSCAccessProvider();
 			//
 				// dialogbox for registration
 				DialogInterface.OnClickListener updateDialogClickListener;
 
 				updateDialogClickListener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 
 
 							switch (which) {
 							case DialogInterface.BUTTON_POSITIVE:
 								//upgrade the user
								accessprovider.promote(activity, null, accessprovider.readToken(activity, null));
 								break;
 
 							case DialogInterface.BUTTON_NEGATIVE:
 								//CLOSE
 								
 								break;
 							
 							}
 
 					}
 				};
 				
				builder.setCancelable(false).setMessage(activity.getString(R.string.auth_question))
 						.setPositiveButton(android.R.string.yes, updateDialogClickListener)
 						.setNegativeButton(android.R.string.no, updateDialogClickListener).show();
 			
 		
 }
 }
