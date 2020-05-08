 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package edu.upenn.mkse212.server;
 
 import edu.upenn.mkse212.client.Database;
 
 import java.sql.Timestamp;
 import java.util.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.amazonaws.services.simpledb.*;
 import com.amazonaws.services.simpledb.model.*;
 import com.amazonaws.auth.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class DatabaseImpl extends RemoteServiceServlet implements Database {
 	
 	private AmazonSimpleDBClient db;
 	
 	public DatabaseImpl() {
 		String userID = "AKIAJQV2RJ5OZMECVPQA";
 		String authKey = "PAlrjITfdQ+RmNxGpqfV8MXOqxSEf8ZLSJoNwA4w";
 		db = new AmazonSimpleDBClient(new BasicAWSCredentials(userID, authKey));
 	} 
 	
 	public boolean addUser(String username, String password, String firstName, 
 			String lastName, String email, String network, String interests,
 			String birthday) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users", username));
 		List<Attribute> attributesList = result.getAttributes();
		if (!attributesList.isEmpty()) {
			return false;
		} else {
			List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
			list.add(new ReplaceableAttribute("password", ""+password,false));
			list.add(new ReplaceableAttribute("firstName", ""+firstName,false));
			list.add(new ReplaceableAttribute("lastName", ""+lastName,false));
			list.add(new ReplaceableAttribute("email", ""+email,false));
			list.add(new ReplaceableAttribute("network", ""+network,false));
			list.add(new ReplaceableAttribute("interests", ""+interests, false));
			list.add(new ReplaceableAttribute("birthday", ""+birthday,false));
			String friends = "";
			list.add(new ReplaceableAttribute("friends", ""+friends, true));
			db.putAttributes(new PutAttributesRequest("users", username, list, 
					new UpdateCondition()));
			return true;
 		}
 	}
 	
 	public boolean addFriend(String username, String otherUsername, Timestamp time) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users", username));
 		List<Attribute> attributeList = result.getAttributes();
 		String friend = otherUsername;
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("friends")) {
 				friend+="~"+a.getValue();
 				a.setValue(friend);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean validateLogin(String username, String password) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest("users", username));
 		List<Attribute> attributeList = result.getAttributes();
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("password")) {
 				return new Boolean(a.getValue().equals(password));
 			}
 		}
 		return new Boolean(false);
 	}
 	
 	public Integer incrementLogins(String username) {
 		GetAttributesResult result = db.getAttributes(
 			new GetAttributesRequest("users", username));
 		List<Attribute> attributeList = result.getAttributes();
 		int loginsSoFar = 0;
 		for (Attribute a : attributeList) {
 			if (a.getName().equals("loginsSoFar")) {
 				loginsSoFar = Integer.valueOf(a.getValue()).intValue();
 			}
 		}
 		loginsSoFar ++;
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute("loginsSoFar", ""+loginsSoFar, false));
 		db.putAttributes(new PutAttributesRequest("users", username, list, 
 				new UpdateCondition()));
 		return new Integer(loginsSoFar);
 	}
 	
 	public void addUser(String username, String password, String firstName,
 					String lastName, String email, String network, String birthday) {
 		
 	}
 
 	
 	
 
 
 }
