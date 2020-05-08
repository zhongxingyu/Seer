 package com.orange.groupbuy.web.server.handler;
 
import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import net.customware.gwt.dispatch.server.ActionHandler;
 import net.customware.gwt.dispatch.server.ExecutionContext;
 import net.customware.gwt.dispatch.shared.DispatchException;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.orange.groupbuy.web.client.dispatch.GetGroupBuyCategory;
 import com.orange.groupbuy.web.client.model.Item;
 import com.orange.groupbuy.web.client.model.ItemList;
 import com.orange.groupbuy.web.server.util.ProxyUtil;
 import com.orange.groupbuy.web.shared.ServiceConstant;
 
 public class GetGroupBuyCategoryListHandler implements
 		ActionHandler<GetGroupBuyCategory, ItemList> {
 
 	private Logger log = Logger.getLogger(GetGroupBuyCategoryListHandler.class
 			.getName());
 
 	@Override
 	public ItemList execute(GetGroupBuyCategory action, ExecutionContext context)
 			throws DispatchException {
 		String apiServerUrl = ProxyUtil.getSearchGroupBuyUrl();
 		String requestUrl = apiServerUrl + "m=gac&app=GROUPBUYWEB";
 
 		ArrayList<Item> category = new ArrayList<Item>();
 		try {
 			InputStream inputStream = ProxyUtil.getResponse(requestUrl);
 
 			StringWriter sw = new StringWriter();
 			copyLarge(inputStream, sw);
 
 			JSONObject dataObject = JSONObject.fromObject(sw.toString());
 			JSONArray resultList = dataObject.getJSONArray("dat");
 
 			if (resultList != null) {
 				category = jsonToResultList(resultList);
 			}
 
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 
 		ItemList names = new ItemList();
 		names.setItems(category);
 		return names;
 	}
 
 	private ArrayList<Item> jsonToResultList(JSONArray productList) {
 		ArrayList<Item> searchResultList = new ArrayList<Item>();
 		if (productList != null) {
 			for (int i = 0; i < productList.size(); i++) {
 				JSONObject object = (JSONObject) productList.get(i);
 				if (object != null) {
 					Item result = jsonToResult(object);
 					searchResultList.add(result);
 				}
 			}
 		}
 		return searchResultList;
 	}
 
 	private Item jsonToResult(JSONObject object) {
 		Item result = new Item();
 		// set value
 		result.setValue(getString(object, ServiceConstant.PARA_CATEGORY_ID));
 		result.setDisplayName(getString(object,
 				ServiceConstant.PARA_CATEGORY_NAME)
 				+ "("
 				+ getString(object, ServiceConstant.PARA_CATEGORY_PRODUCTS_NUM)
 				+ ")");
 		return result;
 	}
 
 	private String getString(JSONObject object, String paraCategoryName) {
 		String returnValue = "";
 		Object value = object.get(paraCategoryName);
 
 		if (value != null) {
 			returnValue = String.valueOf(value);
 		}
 		return returnValue;
 	}
 
 	public static long copyLarge(InputStream input, StringWriter output)
 			throws IOException {
		char[] buffer = new char[4096];
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 		long count = 0;
 		int n = 0;
		while (-1 != (n = reader.read(buffer))) {
 			output.write(new String(buffer));
 			count += n;
 		}
 		return count;
 	}
 
 	@Override
 	public Class<GetGroupBuyCategory> getActionType() {
 		return GetGroupBuyCategory.class;
 	}
 
 	@Override
	public void rollback(GetGroupBuyCategory category, ItemList itemList,
			ExecutionContext context) throws DispatchException {
 	}
 
 }
