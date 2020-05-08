 package com.example.bacsafe;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.ExecutionException;
 
 import android.os.AsyncTask;
 
 import com.appspot.bacsafeserver.bacsafeAPI.BacsafeAPI;
 import com.appspot.bacsafeserver.bacsafeAPI.model.BacsafeAPIGroupUser;
 import com.appspot.bacsafeserver.bacsafeAPI.model.BuddyRequestsProtoSenderUserNameRequestedUserName;
 import com.appspot.bacsafeserver.bacsafeAPI.model.Groups;
 import com.appspot.bacsafeserver.bacsafeAPI.model.GroupsProtoGroupName;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfo;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserName;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserNameBuddies;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserNameCurBAC;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserNameDrinkCount;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserNameFirstNameLastName;
 import com.appspot.bacsafeserver.bacsafeAPI.model.UserInfoProtoUserNameGroups;
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.json.gson.GsonFactory;
 
 public class ServerAPI {
 	private String logResult;
 	private String[] buddies;
 	private Groups groupInfo;
 	
 	public String userAccountSetup(String userName, String firstName, String lastName) throws InterruptedException, ExecutionException {
 		UserAccountSetup request = new UserAccountSetup();
 		return request.execute(userName, firstName, lastName).get();
 	}
 	public String updateUserBAC(String userName, Double curBAC) throws InterruptedException, ExecutionException {
 		UpdateUserBAC request = new UpdateUserBAC();
 		return request.execute(userName, curBAC.toString()).get();
 	}
 	public String updateUserDrinkCount(String userName, Integer drinkCount) throws InterruptedException, ExecutionException {
 		UpdateUserDrinkCount request = new UpdateUserDrinkCount();
 		return request.execute(userName, drinkCount.toString()).get();
 	}
 	public double getUserBAC(String userName) throws InterruptedException, ExecutionException {
 		GetUserBuddiesInfo request = new GetUserBuddiesInfo();
 		return request.execute(userName).get().getCurBAC();
 	}
 	public String[] getUserBuddies(String userName) throws InterruptedException, ExecutionException {
 		GetUserBuddiesInfo request = new GetUserBuddiesInfo();
 		List<String> buddies = request.execute(userName).get().getBuddies();
 		String[] results = null;
 		if(buddies != null) {
 			results = new String[buddies.size()];
 			
 			for(int i = 0; i < buddies.size(); i++) {
 				results[i] = buddies.get(i);
 			}
 		}
 		return results;
 	}
 	public String[] getUserGroups(String userName) throws InterruptedException, ExecutionException {
 		GetUserBuddiesInfo request = new GetUserBuddiesInfo();
 		List<String> groups = request.execute(userName).get().getGroups();
 		String[] results = null;
 		if(groups != null) {
 			results = new String[groups.size()];
 		
 			for(int i = 0; i < groups.size(); i++) {
 				results[i] = groups.get(i);
 			}
 		}
 		return results;
 	}
 	public long getUserDrinkCount(String userName) throws InterruptedException, ExecutionException {
 		GetUserBuddiesInfo request = new GetUserBuddiesInfo();
 		return request.execute(userName).get().getDrinkCount();
 	}
 	public String createBuddies(String userName, LinkedList<Buddy> buddies) throws InterruptedException, ExecutionException {
 		CreateBuddies request = new CreateBuddies();	
 		List<String> buddiesList = new LinkedList<String>();
 		for(int i = 0; i < buddies.size(); i++) {
 			buddiesList.add(buddies.get(i).m_sBuddyUsername);
 		}
 		UserInfoProtoUserNameBuddies buddyInfo = new UserInfoProtoUserNameBuddies();
 		buddyInfo.setUserName(userName);
 		buddyInfo.setBuddies(buddiesList);
 		return request.execute(buddyInfo).get();
 	}
 	public String setGroups(String userName, LinkedList<Group> groups) throws InterruptedException, ExecutionException {
 		SetGroups request = new SetGroups();	
 		List<String> groupsList = new LinkedList<String>();
 		for(int i = 0; i < groups.size(); i++) {
 			groupsList.add(groups.get(i).getGroupName());
 		}
 		UserInfoProtoUserNameGroups groupInfo = new UserInfoProtoUserNameGroups();
 		groupInfo.setUserName(userName);
 		groupInfo.setGroups(groupsList);
 		return request.execute(groupInfo).get();
 	}
 	public String[] getUserBuddiesInfo(String userName) throws InterruptedException, ExecutionException {
 		GetUserBuddiesInfo request = new GetUserBuddiesInfo();
 		UserInfo user = request.execute(userName).get();
 		String[] results = new String[4];
 		results[0] = user.getFirstName();
 		results[1] = user.getLastName();
 		results[2] = user.getCurBAC().toString();
 		results[3] = user.getDrinkCount().toString();
 		
 		return results;
 	}
 	public String sendBuddyRequest(String senderName, String requestedName) throws InterruptedException, ExecutionException {
 		SendBuddyRequest request = new SendBuddyRequest();
 		return request.execute(senderName, requestedName).get();
 	}
 	public String[] getBuddyRequests(String userName) throws InterruptedException, ExecutionException {
 		GetBuddyRequests request = new GetBuddyRequests();
 		return request.execute(userName).get();
 	}
 	public String acceptBuddyRequest(String userName, String newBuddyUserName) throws InterruptedException, ExecutionException {
 		SendBuddyRequest request = new SendBuddyRequest();
 		return request.execute(newBuddyUserName, userName).get();
 	}
 	public void createGroup(Group group) throws InterruptedException, ExecutionException {
 		CreateGroup request = new CreateGroup();
		request.execute(group.m_sGroupName);
 		for(int i = 0; i < group.m_listGroupBuddies.size(); i++) {
			addDrinker(group.m_sGroupName, group.m_listGroupBuddies.get(i).m_sBuddyUsername);
 		}
 		for(int i = 0; i < group.m_listGroupBuddies.size(); i++) {
 			LinkedList<Group> groups = new LinkedList<Group>();
 			groups.add(group);
 			setGroups(group.m_listGroupBuddies.get(i).m_sBuddyUsername, groups);
 		}
 	}
 	public String addDD(String groupName, String drinker) throws InterruptedException, ExecutionException {
 		GroupAddDD request = new GroupAddDD();
 		return request.execute(groupName, drinker).get();
 	}
 	public String addDrinker(String groupName, String dd) throws InterruptedException, ExecutionException {
 		GroupAddDrinker request = new GroupAddDrinker();
 		return request.execute(groupName, dd).get();
 	}
 	public LinkedList<String> getGroupDrinkers(String groupName) throws InterruptedException, ExecutionException {
 		GetGroupInfo request = new GetGroupInfo();
 		Groups group = request.execute(groupName).get();
 		List<String> drinkers = group.getDrinkers();
 		String[] results = new String[drinkers.size()];
 		for(int i = 0; i < drinkers.size(); i++) {
 			results[i] = drinkers.get(i);
 		}
 		LinkedList<String> groupList = new LinkedList<String>(Arrays.asList(results));
 		return groupList;
 	}
 	public String[] getGroupDDs(String groupName) throws InterruptedException, ExecutionException {
 		GetGroupInfo request = new GetGroupInfo();
 		Groups group = request.execute(groupName).get();
 		List<String> dds = group.getDd();
 		String[] results = new String[dds.size()];
 		for(int i = 0; i < dds.size(); i++) {
 			results[i] = dds.get(i);
 		}
 		return results;
 	}
 	
 	public class UserAccountSetup extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				UserInfoProtoUserNameFirstNameLastName user = new UserInfoProtoUserNameFirstNameLastName();
 				user.setUserName(info[0]);
 				user.setFirstName(info[1]);
 				user.setLastName(info[2]);
 				log = service.userinfo().userAccountSetup(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class UpdateUserBAC extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				UserInfoProtoUserNameCurBAC user = new UserInfoProtoUserNameCurBAC();
 				user.setUserName(info[0]);
 				user.setCurBAC(Double.parseDouble(info[1]));
 				log = service.userinfo().updateUserBAC(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class UpdateUserDrinkCount extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				UserInfoProtoUserNameDrinkCount user = new UserInfoProtoUserNameDrinkCount();
 				user.setUserName(info[0]);
 				user.setDrinkCount(Long.parseLong(info[1]));
 				log = service.userinfo().updateUserDrinkCount(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class CreateBuddies extends AsyncTask<UserInfoProtoUserNameBuddies, Void, String> {
 		protected String doInBackground(UserInfoProtoUserNameBuddies... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				log = service.userinfo().createBuddies(info[0]).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class SetGroups extends AsyncTask<UserInfoProtoUserNameGroups, Void, String> {
 		protected String doInBackground(UserInfoProtoUserNameGroups... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				log = service.userinfo().setGroups(info[0]).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class GetUserBuddiesInfo extends AsyncTask<String, Void, UserInfo> {
 		protected UserInfo doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			UserInfo userInfo = new UserInfo();
 			try {
 				UserInfoProtoUserName user = new UserInfoProtoUserName();
 				user.setUserName(info[0]);
 				userInfo = service.userinfo().getUserBuddiesInfo(user).execute();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return userInfo;
 		}
 	}
 	public class SendBuddyRequest extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				BuddyRequestsProtoSenderUserNameRequestedUserName user = new BuddyRequestsProtoSenderUserNameRequestedUserName();
 				user.setSenderUserName(info[0]);
 				user.setRequestedUserName(info[1]);
 				log = service.buddyrequest().sendBuddyRequest(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class GetBuddyRequests extends AsyncTask<String, Void, String[]> {
 		protected String[] doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			Vector<String> names = new Vector<String>();
 			String log = "";
 			try {
 				BuddyRequestsProtoSenderUserNameRequestedUserName user = new BuddyRequestsProtoSenderUserNameRequestedUserName();
 				user.setRequestedUserName(info[0]);
 				log = service.buddyrequest().sendBuddyRequest(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			int last = 0;
 			for(int i = 0; i < log.length(); i++) {
 				if (log.charAt(i) == ' ') {
 					names.add(log.substring(last, i));
 					last = i + 1;
 				}
 			}
 			String[] result = new String[names.size()];
 			for(int i = 0; i < result.length; i++) {
 				result[i] = names.elementAt(i);
 			}
 			
 			return result;
 		}
 		protected void onPostExecute(String[] result) {
 			buddies = result;
 		}
 	}
 	public class AcceptBuddyRequest extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				BuddyRequestsProtoSenderUserNameRequestedUserName user = new BuddyRequestsProtoSenderUserNameRequestedUserName();
 				user.setSenderUserName(info[0]);
 				user.setRequestedUserName(info[1]);
 				log = service.buddyrequest().acceptBuddyRequests(user).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class CreateGroup extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				GroupsProtoGroupName group = new GroupsProtoGroupName();
 				group.setGroupName(info[0]);
 				log = service.groups().createGroup(group).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class GroupAddDD extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				BacsafeAPIGroupUser group = new BacsafeAPIGroupUser();
 				group.setUserName(info[0]);
 				group.setGroupName(info[1]);
 				log = service.groups().addDD(group).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class GroupAddDrinker extends AsyncTask<String, Void, String> {
 		protected String doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			String log = "";
 			try {
 				BacsafeAPIGroupUser group = new BacsafeAPIGroupUser();
 				group.setUserName(info[0]);
 				group.setGroupName(info[1]);
 				log = service.groups().addDrinker(group).execute().getMessage();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(String result) {
 			logResult = result;
 		}
 	}
 	public class GetGroupInfo extends AsyncTask<String, Void, Groups> {
 		protected Groups doInBackground(String... info) {
 			BacsafeAPI.Builder builder = new BacsafeAPI.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 			BacsafeAPI service = builder.build();
 			Groups log = new Groups();
 			log.setGroupName(info[0]);
 			try {
 				GroupsProtoGroupName group = new GroupsProtoGroupName();
 				group.setGroupName(info[0]);
 				log = service.groups().getGroupInfo(group).execute();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 			
 			return log;
 		}
 		protected void onPostExecute(Groups result) {
 			groupInfo = result;
 		}
 	}
 }
