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
 
 package eu.trentorise.smartcampus.ac.account;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.util.Log;
 import eu.trentorise.smartcampus.ac.AACException;
 import eu.trentorise.smartcampus.ac.AuthActivity;
 import eu.trentorise.smartcampus.ac.Constants;
 import eu.trentorise.smartcampus.ac.SCAccessProvider;
 import eu.trentorise.smartcampus.ac.embedded.EmbeddedSCAccessProvider;
 import eu.trentorise.smartcampus.ac.model.TokenData;
 import eu.trentorise.smartcampus.ac.network.RemoteConnector;
 
 /**
  * @author raman
  *
  */
 public class AccountSCAccessProvider extends SCAccessProvider {
 
 	@Override
 	public boolean login(Activity activity, Bundle extras) throws AACException {
 
 		String clientId = Constants.getClientId(activity);
 		String clientSecret = Constants.getClientSecret(activity);
 		
 		String aTokenType = Constants.getAccountTokenType(activity);
 		String accountType;
 		try {
 			accountType = Constants.getAccountType(activity);
 		} catch (NameNotFoundException e) {
 			throw new AACException(e.getMessage());
 		}
 		final AccountManager am = AccountManager.get(activity);
 		Account[] accounts = am.getAccountsByType(accountType);
 		if (accounts == null || accounts.length == 0) {
 			am.addAccount(accountType, aTokenType, null, null, null, new Callback(activity, clientId, clientSecret, extras), null);
 			return true;
 		} else {
 			Account a = accounts[0];
			String token = am.peekAuthToken(a, null);
 			if (token == null) {
 				am.getAuthToken(a, aTokenType, null, null, new Callback(activity, clientId, clientSecret, extras), null);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String readToken(Context ctx) throws AACException {
 
 		String aTokenType = Constants.getAccountTokenType(ctx);
 		String accountType;
 		try {
 			accountType = Constants.getAccountType(ctx);
 		} catch (NameNotFoundException e) {
 			throw new AACException(e.getMessage());
 		}
 		final AccountManager am = AccountManager.get(ctx);
 		Account[] accounts = am.getAccountsByType(accountType);
 		if (accounts != null && accounts.length > 0) {
 			Account a = accounts[0];
			String token = am.peekAuthToken(a, null);
 			if (token != null) {
 				String expires = am.getUserData(a, Constants.KEY_EXPIRES_IN+aTokenType);
 				long expTime = 0;
 				if (expires != null) {
 					expTime = Long.parseLong(expires);
 				}
 				// have a margine of 1 min
 				if (expTime > System.currentTimeMillis() + 60*1000) {
 					return token;
 				} else {
 					String refresh = am.getUserData(a, Constants.KEY_REFRESH_TOKEN+aTokenType);
 					TokenData data;
 					try {
 						String clientId = Constants.getClientId(ctx);
 						String clientSecret = Constants.getClientSecret(ctx);
 						data = RemoteConnector.refreshToken(Constants.getAuthUrl(ctx), refresh, clientId, clientSecret);
 					} catch (NameNotFoundException e) {
 						throw new AACException(e.getMessage());
 					}
 					am.setAuthToken(a, aTokenType, data.getAccess_token());
 					if (data.getRefresh_token() != null && data.getRefresh_token().length() > 0) {
 						am.setUserData(a, Constants.KEY_REFRESH_TOKEN+aTokenType, data.getRefresh_token());
 					}
 					am.setUserData(a, Constants.KEY_EXPIRES_IN+aTokenType, ""+(System.currentTimeMillis()+1000*data.getExpires_in()));
 					return data.getAccess_token();
 				}
 
 			}
 		}
 		throw new AACException("No token data.");
 	}
 
 	@Override
 	public boolean logout(Context ctx) throws AACException {
 		String aTokenType = Constants.getAccountTokenType(ctx);
 		String accountType;
 		try {
 			accountType = Constants.getAccountType(ctx);
 		} catch (NameNotFoundException e) {
 			return false;
 		}
 		final AccountManager am = AccountManager.get(ctx);
 		Account[] accounts = am.getAccountsByType(accountType);
 		if (accounts != null && accounts.length != 0) {
 			Account a = accounts[0];
			String token = am.peekAuthToken(a, null);
 			if (token == null) {
 				try {
 					RemoteConnector.revokeToken(Constants.getAuthUrl(ctx), token);
 					am.invalidateAuthToken(accountType, token);
 					am.setUserData(a, Constants.KEY_REFRESH_TOKEN+aTokenType, null);
 					am.setUserData(a, Constants.KEY_EXPIRES_IN+aTokenType, null);
 					return true;
 				} catch (NameNotFoundException e) {
 					Log.e(EmbeddedSCAccessProvider.class.getName(), ""+e.getMessage());
 				} catch (AACException e) {
 					Log.e(EmbeddedSCAccessProvider.class.getName(), ""+e.getMessage());
 				}
 			}
 		}
 		return false;
 	}
 
 	private class Callback implements AccountManagerCallback<Bundle> {
 		
 		private Bundle extras;
 		private Activity activity;
 		private String clientSecret;
 		private String clientId;
 
 		public Callback(Activity activity, String clientId, String clientSecret, Bundle extras) {
 			super();
 			this.activity = activity;
 			this.clientSecret = clientSecret;
 			this.clientId = clientId;
 			this.extras = extras;
 		}
 
 		@Override
 		public void run(AccountManagerFuture<Bundle> result) {
 			Bundle bundle = null;
 			try {
 				bundle = result.getResult();
 				Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
 				if (launch != null) {
 					if (extras != null) {
 						launch.putExtras(extras);
 					}
 					launch.putExtra(AuthActivity.CLIENT_ID, clientId);
 					launch.putExtra(AuthActivity.CLIENT_SECRET, clientSecret);
 						activity.startActivityForResult(launch, SC_AUTH_ACTIVITY_REQUEST_CODE);
 				}
 			} catch (Exception e) {
 				return;
 			}
 		}
 	}	
 
 }
