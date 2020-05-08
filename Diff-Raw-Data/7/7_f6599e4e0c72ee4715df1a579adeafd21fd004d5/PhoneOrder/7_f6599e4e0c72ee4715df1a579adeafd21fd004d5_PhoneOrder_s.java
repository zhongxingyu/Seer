 package com.htb.cnk.data;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.htb.cnk.lib.ErrorPHP;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 public class PhoneOrder extends MyOrder {
 
 	public PhoneOrder(Context context) {
 		super(context);
 		// TODO Auto-generated constructor stub
 	}
 
 	public int getPhoneQuantity(int position) {
 		return mOrder.get(position).phoneQuantity;
 	}
 
 	public void phoneClear() {
 		for (int i = 0; i < mOrder.size(); i++) {
 			OrderedDish item = (OrderedDish) mOrder.get(i);
 			if (item.padQuantity == 0) {
 				mOrder.remove(item);
 				i--;
 			} else {
 				item.phoneQuantity = 0;
 			}
 		}
 	}
 
 	public int getPhoneOrderFromServer(int tableId) {
 		talbeClear();
 		String response = Http.get(Server.GET_GETPHONEORDER, "TID=" + tableId);
 		if (response == null) {
 			Log.e(TAG, "getPhoneOrderFromServer.timeOut");
 			return TIME_OUT;
 		} else if ("null".equals(response)) {
 			return RET_NULL_PHONE_ORDER;
 		}
 	
 		try {
 			JSONArray tableList = new JSONArray(response);
 			int length = tableList.length();
 			phoneClear();
 			for (int i = 0; i < length; i++) {
 				JSONObject item = tableList.getJSONObject(i);
 				int quantity = item.getInt("quantity");
 				int dishId = item.getInt("dish_id");
 				// int status = item.getInt("status");
 	
 				Cursor cur = getDishNameAndPriceFromDB(dishId);
 				String name = cur.getString(0);
 				float dishPrice = cur.getFloat(1);
 				Dish mDish = new Dish(dishId, name, dishPrice, null);
 				addOrder(mDish, quantity, tableId, 0, MODE_PHONE);
 			}
 			return 0;
 		} catch (Exception e) {
 			Log.e(TAG, response);
 			e.printStackTrace();
 		}
 	
 		return -1;
 	}
 
 	public int cleanServerPhoneOrder(int tableId) {
 		String phoneOrderPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
 				+ tableId);
 		if (!ErrorPHP.isSucc(phoneOrderPkg, TAG)) {
 			return -1;
 		}
 		return 0;
 	}
 
 	private int delPhoneOrderedDish(int tableId, int dishId) {
 		showServerDelProgress();
 		String phoneOrderedPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
 				+ tableId + "&DID=" + dishId);
 		if (!ErrorPHP.isSucc(phoneOrderedPkg, TAG)) {
 			return -1;
 		}
 		remove(dishId);
 		return 0;
 	
 	}
 
 	private int minusPhoneOrderOnServer(int tableId, float quantity, int dishId) {
 		if (quantity != 0) {
 			showServerDelProgress();
 			String phoneOrderPkg = Http.get(Server.UPDATE_PHONE_ORDER, "DID="
 					+ dishId + "&DNUM=" + quantity + "&TID=" + tableId);
 
 			if (!ErrorPHP.isSucc(phoneOrderPkg, TAG)) {
 				return -1;
 			}
 			return 0;
 		} else {
 			return delPhoneOrderedDish(tableId, dishId);
 		}
 	}
 
 	public int minus(Dish dish, int quantity) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				return minus(item, quantity);
 			}
 		}
 		return 0;
 	}
 
 	public int minus(int position, float quantity) {
 		OrderedDish item = mOrder.get(position);
 		return minus(item, quantity);
 	}
 
 	private int minus(OrderedDish item, float quantity) {
 		if ((item.padQuantity + item.phoneQuantity) > quantity) {
 			if (item.padQuantity > quantity) {
 				item.padQuantity -= quantity;
 				return 0;
 			} else {
 				quantity -= item.padQuantity;
 
 				if (minusPhoneOrderOnServer(Info.getTableId(),
 						item.phoneQuantity - quantity, item.getDishId()) < 0) {
 					return -1;
 				} else {
 					item.phoneQuantity -= quantity;
 					item.padQuantity = 0;
 					return 0;
 				}
 	
 			}
 		} else {
 			if (item.phoneQuantity > 0) {
 				if (minusPhoneOrderOnServer(Info.getTableId(), 0,
 						item.getDishId()) < 0) {
 					return -1;
 				}
 			}
 			mOrder.remove(item);
 			return 0;
 		}
 	}
 
 }
