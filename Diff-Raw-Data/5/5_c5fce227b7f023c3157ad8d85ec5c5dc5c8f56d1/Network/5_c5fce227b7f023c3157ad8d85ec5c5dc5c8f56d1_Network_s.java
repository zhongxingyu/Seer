 package com.titanium.ebaybottom.controller;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Cookie;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import com.google.gson.Gson;
 import com.titanium.ebaybottom.model.EbaySearchResult;
 import com.titanium.ebaybottom.model.FindItemsAdvancedResponse;
 import com.titanium.ebaybottom.model.Group;
 import com.titanium.ebaybottom.model.Item;
 import com.titanium.ebaybottom.model.Pair;
 import com.titanium.ebaybottom.selenium.WebWindow;
 import com.titanium.ebaybottom.selenium.WebdriverHelper;
 import com.titanium.ebaybottom.ui.UI;
 import com.titanium.ebaybottom.util.ApiKey;
 
 public class Network {
 
 	public static WebDriver logIn(Pair<String, String> selectedUserAccount) {
 
 		WebDriver driver = new FirefoxDriver();
 		driver.get("https://signin.ebay.com/ws/eBayISAPI.dll?SignIn");
 
 		UI.printUI("Log in with: " + selectedUserAccount);
 
 		WebdriverHelper.setInstantKeys(driver, "userid",
 				selectedUserAccount.getKey());
 		WebdriverHelper.setInstantKeys(driver, "pass",
 				selectedUserAccount.getValue());
 
 		// remember me
 		if (!driver.findElement(By.id("signed_in")).isSelected())
 			driver.findElement(By.id("signed_in")).click();
 
 		WebElement element = driver.findElement(By.id("userid"));
 		element.submit();
 
 		return driver;
 	}
 
 	public static List<Item> loadFromEbay(Group group) {
 
 		List<Item> result = new ArrayList<>();
 
 		for (Pair<String, Pair<Integer, Integer>> item : group.keywordsWithPrices) {
 			result.addAll(ebayServerRequest(buildUri(item.getKey(),
 					group.categories, item.getValue().getKey(), item.getValue()
 							.getValue())));
 		}
 
 		return result;
 	}
 
 	public static List<Item> ebayServerRequest(String uri) {
 
 		UI.printUI("Request to eBay server...");
 		UI.printDebug(uri);
 		String returnJson = "";
 		try {
 			HttpResponse response = new DefaultHttpClient()
 					.execute(new HttpGet(uri));
 
 			String json = EntityUtils.toString(response.getEntity());
 
 			// have to replace @ signs because eBay returns faulty JSON
 			returnJson = json.replace("\"@", "");
 
 			UI.printDebug("@ chars removed count:"
 					+ (json.length() - returnJson.length()));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			UI.printError(e.toString());
 		}
 
 		return parseItems(returnJson);
 	}
 
 	/**
 	 * http://developer.ebay.com/DevZone/finding/CallRef/findItemsAdvanced.html
 	 */
 	private static String buildUri(String keyword, List<Integer> categories,
 			int minPrice, int maxPrice) {
 
 		String uri = "";
 		try {
 			URIBuilder uBuilder = new URIBuilder(
 					"http://svcs.ebay.com/services/search/FindingService/v1");
 
 			// compulsory
 			uBuilder.addParameter("OPERATION-NAME", "findItemsAdvanced");
 			uBuilder.addParameter("SECURITY-APPNAME", ApiKey.KEY);
 			uBuilder.addParameter("RESPONSE-DATA-FORMAT", "JSON");
 
 			// ### OPTIONAL
 			uBuilder.addParameter("keywords", keyword);
 			uBuilder.addParameter("GLOBAL-ID", Config.locale);
 			uBuilder.addParameter("outputSelector", "SellerInfo");
 			uBuilder.addParameter("paginationInput.entriesPerPage",
 					String.valueOf(Config.resultCount));
 			uBuilder.addParameter("SERVICE-VERSION", "1.12.0");
 
 			// v4 - sort by date
 			uBuilder.addParameter("sortOrder", "EndTimeSoonest");
 
 			// Specifies the category from which you want to retrieve item
 			// listings.
 			// This field can be repeated to include multiple categories. Up to
 			// three (3) categories can be specified.
 			// http://developer.researchadvanced.com/tools/categoryBrowser.php
 			for (int i = 0; i <= categories.size() && i < 3; i++)
 				uBuilder.addParameter("categoryId", categories.get(i)
 						.toString());
 
 			// custom filters, see
 			// more:http://developer.ebay.com/devzone/finding/callref/types/ItemFilterType.html
 			uBuilder.addParameter("itemFilter(0).name", "Condition");
 			uBuilder.addParameter("itemFilter(0).value(0)", "1000");
 			uBuilder.addParameter("itemFilter(0).value(1)", "1500");
 			uBuilder.addParameter("itemFilter(0).value(2)", "2000");
 			uBuilder.addParameter("itemFilter(0).value(3)", "2500");
 			uBuilder.addParameter("itemFilter(0).value(4)", "3000");
 
 			// defaults to USD.
 			uBuilder.addParameter("itemFilter(1).name", "MaxPrice");
 			uBuilder.addParameter("itemFilter(1).value",
 					Integer.toString(maxPrice));
 			uBuilder.addParameter("itemFilter(2).name", "MinPrice");
 			uBuilder.addParameter("itemFilter(2).value",
 					Integer.toString(minPrice));
 
 			// Limits the results to items ending on or after the specified
 			// time.
 			// 24h is user request
			// ex: YYYY-MM-DDTHH:MM:SS.SSSZ (e.g., 2004-08-04T19:09:02.768Z)
 			DateTime date = new DateTime().plusHours(24);
 			DateTimeFormatter fmt = DateTimeFormat
					.forPattern("YYYY-MM-DD'T'HH:mm:ss.SSS'Z'");
 
 			uBuilder.addParameter("itemFilter(3).name", "EndTimeFrom");
 			uBuilder.addParameter("itemFilter(3).value", fmt.print(date));
 
 			uri = uBuilder.build().toString();
 			UI.printDebug("REQUEST URL:" + uri);
 
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return uri;
 	}
 
 	private static List<Item> parseItems(String jsonValidVariableNames) {
 
 		List<Item> result = new ArrayList<Item>();
 		EbaySearchResult resultSet = new Gson().fromJson(
 				jsonValidVariableNames, EbaySearchResult.class);
 
 		if (resultSet.getErrorMessage() != null) {
 			// we have a error
 			UI.printError(resultSet.getErrorMessage().get(0).getError().get(0));
 			return result;
 		}
 
 		if (resultSet.getFindItemsAdvancedResponse().get(0).getErrorMessage() != null) {
 			// we have a error
 			UI.printError(resultSet.getFindItemsAdvancedResponse().get(0)
 					.getErrorMessage().get(0).getError().get(0));
 			return result;
 		}
 
 		// response header
 		FindItemsAdvancedResponse advancedResponse = resultSet
 				.getFindItemsAdvancedResponse().get(0);
 
 		if (advancedResponse.getSearchResult().get(0).getItem() == null) {
 			UI.printError("0 items returned");
 			return result;
 		}
 
 		UI.printUI(advancedResponse);
 
 		result = advancedResponse.getSearchResult().get(0).getItem();
 
 		return result;
 	}
 
 }
