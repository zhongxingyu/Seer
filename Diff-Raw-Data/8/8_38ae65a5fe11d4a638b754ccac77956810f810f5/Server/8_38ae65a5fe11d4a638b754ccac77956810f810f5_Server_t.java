 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 public class Server {
 	public String SERVER;
 	public Server(String server) {
 		this.SERVER = server;
 	}
 	public void createAccount(Map p, final SignUpActivity a) {
 		class CreateAcct extends JsonTask {
 			public CreateAcct(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 				a.onUserCreation();
 			}
 		}
 		CreateAcct c = new CreateAcct("/users");
 		c.execute(p);
 	}
 	
 	public void login(Map p, final MainActivity a) {
 		class AcctLogin extends JsonTask {
 			public AcctLogin(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					//Log.d("Json data", data.toString());
 					String token = null;
 					int id = -1;
 					String name = null;
 					String message = null;
 					try {
 						message = data.getString("message");
 					} catch (JSONException e1) {
 						e1.printStackTrace();
 					}
 					if (message.compareTo("success") == 0) {
 						try {
 							token = (String) data.get("token");
 							id = data.getInt("id");
 							name = data.getString("name");
 						} catch (JSONException e) {
 							// The login request failed so we can do something here if needed
 							e.printStackTrace();
 						}
 						
 					    SharedPreferences.Editor editor = MainActivity.settings.edit();
 					    editor.putString("token", token);
 					    editor.putInt("id", id);
 					    editor.commit();
 					    MainActivity.authToken = token;
 					    MainActivity.userId = id;
 					    MainActivity.userName = name;
 					    
 					    a.afterLoginAttempt(true);
 					    return;
 						
 					}
 				}
 				a.afterLoginAttempt(false);
 				Log.d("Json data", "Request failed");					
 			}
 		}
 		AcctLogin c = new AcctLogin("/tokens/create");
 		c.execute(p);
 	}
 
 	public void logout(Map p) {
 		class AcctLogout extends JsonTask {
 			public AcctLogout(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 			}
 		}
 		AcctLogout c = new AcctLogout("/tokens/delete");
 		c.execute(p);
 	}
 	
 	public void getItems(Map p) {
 		class gItems extends JsonTask {
 			public gItems(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					//Log.d("Json data getItems", data.toString());
 					JSONArray items = null;
 					if (MainActivity.firstUpdate)
 						ShoppingList.hm = new HashMap<String, ShoppingList>();
 					ArrayList<ShoppingList> lists = new ArrayList<ShoppingList>();
 					ArrayList<Item> itemsFriendsWillSplit = new ArrayList<Item>();
 					try {
 						items = (JSONArray) data.get("items");
 						for (int i = 0; i < items.length(); i++) {
 							JSONObject item = (JSONObject) items.get(i);
 							Item sItem = new Item(item);
 							sItem._list.addItem(sItem);
 							if (!lists.contains(sItem._list))
 								lists.add(sItem._list);
 							if (sItem._shareAccepted) {
 								itemsFriendsWillSplit.add(sItem);
 							}
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 	
 					synchronized (FeedAdapter.itemsIWillSplit) {
 						FeedAdapter.itemsFriendsWillSplit = itemsFriendsWillSplit;
 					}
 					if (MainActivity.firstUpdate && ListActivity.mainListActivity != null) {
 						ListActivity.mainListActivity.updateListNames();
 						ListActivity.mainListActivity.updateItemsList();
 						MainActivity.firstUpdate = false;
 					}
 				}
 			}
 		}
 		gItems c = new gItems("/item/getItems");
 		c.execute(p);
 		
 	}
 	
 	public void getSharedItems(Map p) {
 		class gSharedItems extends JsonTask {
 			public gSharedItems(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					//Log.d("Json data getSharedItems", data.toString());
 					JSONArray items = null;
 					ArrayList<Item> itemsIWillSplit = new ArrayList<Item>();
 					ArrayList<Item> itemsFriendsWantToSplit = new ArrayList<Item>();
 					try {
 						items = (JSONArray) data.get("items");
 						for (int i = 0; i < items.length(); i++) {
 							JSONObject item = (JSONObject) items.get(i);
 							Item sItem = new Item(item);
 							sItem._list.addItem(sItem);
 							if (sItem._shareAccepted) {
 								itemsIWillSplit.add(sItem);
 							} else {
 								itemsFriendsWantToSplit.add(sItem);
 							}
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 					
 					synchronized (FeedAdapter.itemsIWillSplit) {
 						FeedAdapter.itemsIWillSplit = itemsIWillSplit;
 					}
 					synchronized (FeedAdapter.itemsFriendsWantToSplit) {
 						FeedAdapter.itemsFriendsWantToSplit = itemsFriendsWantToSplit;
 					}
 					if (CalcBillActivity.mainCalcBillAct != null) {
 						CalcBillActivity.mainCalcBillAct.updateListNames();
 					}
 				}
 			}
 		}
 		gSharedItems c = new gSharedItems("/item/getSharedItems");
 		c.execute(p);
 		
 	}
 	
 	public void getFriends(Map p) {
 		class gFriend extends JsonTask {
 			public gFriend(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 					JSONArray friends = null;
 					try {
 						friends = (JSONArray) data.get("friends");
 					} catch (JSONException e) {
 						//something happened!
 						e.printStackTrace();
 					}
 					Friend.allFriends = new ArrayList<Friend>();
 					ArrayList<Friend> friendsRequest = new ArrayList<Friend>();
 					ArrayList<Friend> myfriends = new ArrayList<Friend>();
 					for (int i = 0; i < friends.length(); i++) {
 						JSONObject f = null;
 						try {
 							f = friends.getJSONObject(i);
 						} catch (JSONException e) {
 							//Oh noes!
 							e.printStackTrace();
 						}
 						if (f != null) {
 							try {
 								Friend fr = new Friend( f.getString("name"), 
 													    f.getString("email"), 
 													    f.getInt("id"), 
 													    f.getBoolean("accepted"),
 													    f.getString("asker"));
 								if (fr.haveAcceptedRequest) {
 									myfriends.add(fr);
 								} else {
 									if (fr.asker.compareTo("them") == 0){
 										friendsRequest.add(fr);
 									}
 								}	
 							} catch (NumberFormatException e) {
 								//It done go wrong
 								e.printStackTrace();
 							} catch (JSONException e) {
 								//It be even more wrong
 								e.printStackTrace();
 							}							
 						}	
 					}
 					FriendAdapter.friends = myfriends;
 					FriendAdapter.friendsRequest = friendsRequest;					
 					FriendsActivity.refreshEXV();
 				}
 			}
 		}
 		gFriend c = new gFriend("/user/getFriends");
 		c.execute(p);
 		
 	}
 		
 	public void requestFriend(Map p, final View v) {
 		class rFriend extends JsonTask {
 			public rFriend(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString()); 
 					try {
 						String message = data.getString("status");
 						
 						if (message.contains("success")) {
 							Toast.makeText(v.getContext(),"Your friend request has been sent sucessfully!",
 									Toast.LENGTH_SHORT).show();
 						} else {
 							Toast.makeText(v.getContext(), "Friend Request Failed. " + message + ".", Toast.LENGTH_SHORT).show();
 						}
 					} catch (JSONException e) {
 						
 					}
 				} else {
 					Toast.makeText(v.getContext(),"Not Connected to the Interwebz",Toast.LENGTH_SHORT).show();
 				}
 			}
 		}
 		rFriend c = new rFriend("/user/makeFriendReq");
 		c.execute(p);
 				
 	}
 	
 	public void acceptFriend(Map p) {
 		class aFriend extends JsonTask {
 			public aFriend(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null)
 					Log.d("Json data", data.toString()); 
 				//a.onUserCreation();
 			}
 		}
 		aFriend c = new aFriend("/user/acceptFriendReq");
 		c.execute(p);
 				
 	}
 	
 	public void addItem(Map p) {
 		class aItem extends JsonTask {
 			public aItem(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 					int id = -1;
 					String list = null;
 					String name = null;
 					String price = null;
 					boolean shared = false;
 					try {
 						id = data.getInt("id");
 						name = data.getString("name");
 						price = data.getString("price");
 						list = data.getString("list");
 						shared = data.getBoolean("shared");
 						for (Item i : ShoppingList.getShoppingList(list)._items) {
 							if (i._shared == shared && i._name == name &&
 									i._price.toString() == price && i._id == -1) {
 								i._id = id;
 								break;
 							}
 						}
 					} catch (JSONException e) {
 						Log.d("Json data parsing", "Failed...");
 					}
 				}
 			}
 		}
 		aItem c = new aItem("/item/add");
 		c.execute(p);
 	}
 	
 	public void editItem(Map p) {
 		class eItem extends JsonTask {
 			public eItem(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 			}
 		}
 		eItem c = new eItem("/item/edit");
 		c.execute(p);
 		
 	}
 	
 	public void deleteItem(Map p) {
 		class dItem extends JsonTask {
 			public dItem(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 			}
 		}
 		dItem c = new dItem("/item/delete");
 		c.execute(p);
		//
 	}
 	
 	public void removeFriend(Map p) {
 		class dItem extends JsonTask {
 			public dItem(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 			}
 		}
 		dItem c = new dItem("/user/removeFriend");
 		c.execute(p);
 	}
 	
 	public void acceptShare(Map p) {
 		class dItem extends JsonTask {
 			public dItem(String _path) {
 				super(SERVER + _path);
 			}
 			@Override
 			public void onPostExecute(JSONObject data) {
 				if (data != null) {
 					Log.d("Json data", data.toString());
 				}
 			}
 		}
 		dItem c = new dItem("/item/acceptShare");
 		c.execute(p);
 	}
 }
 
 
 
 
 
 
