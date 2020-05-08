 package com.orange.groupbuy.parser;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jdom.Element;
 
 import com.orange.common.utils.DateUtil;
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.addressparser.CommonAddressParser;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.dao.Gps;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class Tuan800Parser extends CommonGroupBuyParser {
 
 	@Override
 	public int convertCategory(String category) {
 		return DBConstants.C_CATEGORY_UNKNOWN;
 	}
 
 	@Override
 	public String generateWapLoc(String webURL, String imageURL) {
 		return null;
 	}
 
 	@Override
 	public boolean parseElement(Element root, CommonAddressParser addressParser) {
 		List<?> productList = getFieldBlock(root, "url");
 		if (productList == null)
 			return false;
 				
 		Iterator<?> it = productList.iterator();
 		while (it.hasNext()){
 			Element productElement = (Element)it.next();
 			if (productElement == null)
 				continue;
 			
 			String  loc = getFieldValue(productElement, "loc");
 			loc = loc.replaceAll("\\?source=tuan800", "");
 			
 			Element data = getFieldElement(productElement, "data", "display");
 			if (data == null)
 				continue;
 			
 			String website = getFieldValue(data, "website");
 			String siteurl = getFieldValue(data, "siteurl");
 			if (siteurl == null){
 				siteurl = getDefaultSiteURL();
 			}
 			
 			String city = convertCity(getFieldValue(data, "city"));
 			//more consideration
 			String title = getFieldValue(data, "title");
 			String image = getFieldValue(data, "image");
 			String startTimeString = getFieldValue(data, "startTime");
 			String endTimeString = getFieldValue(data, "endTime");
 			String merchantEndTimeString = getFieldValue(data, "merchantEndTime");
 			double value = StringUtil.doubleFromString(getFieldValue(data, "value"));
 			double price = StringUtil.doubleFromString(getFieldValue(data, "price"));
 			String description = getFieldValue(data, "tip");
 			int bought = StringUtil.intFromString(getFieldValue(data, "bought"));
 			String detail = getFieldValue(data, "detail");
 			int major = StringUtil.intFromString(getFieldValue(data, "major"));
 			int category = convertCategory(getFieldValue(data, "cate"));
 			List<String> range = StringUtil.stringToList(getFieldValue(data, "range"));
 			
 			String isPostString = getFieldValue(data, "post");
 			String soldOutString = getFieldValue(data, "soldOut");
 			int maxQuota = StringUtil.intFromString(getFieldValue(data, "maxQuota"));
 			int minQuota = StringUtil.intFromString(getFieldValue(data, "minQuota"));
 			int priority = StringUtil.intFromString(getFieldValue(data, "priortity"));
 			if (priority == 0)
 				priority = StringUtil.intFromString(getFieldValue(data, "priority"));
 			
 			String allTags = getFieldValue(data, "tag");
 			List<String> tag = StringUtil.stringToList(allTags);
 			if (category == DBConstants.C_CATEGORY_UNKNOWN)
 				category = setCategoryByTag(allTags, city);
 			
 			Date startDate = null;
 			Date endDate = null;
 			Date merchantEndDate = null;
 			if (startTimeString.contains("*") || startTimeString.contains(":")){
 				final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; 
 				final String BEIJING_TIMEZONE = "GMT+0800";
 				startDate = DateUtil.dateFromStringByFormat(startTimeString, DATE_FORMAT, BEIJING_TIMEZONE);
 				endDate = DateUtil.dateFromStringByFormat(endTimeString, DATE_FORMAT, BEIJING_TIMEZONE);
 				merchantEndDate = DateUtil.dateFromStringByFormat(merchantEndTimeString, DATE_FORMAT, BEIJING_TIMEZONE);
 			}
 			else{
 				startDate = StringUtil.dateFromIntString(startTimeString);
 				endDate = StringUtil.dateFromIntString(endTimeString);
 				merchantEndDate = StringUtil.dateFromIntString(merchantEndTimeString);				
 			}
 			
 			
 			List<String> telList = new LinkedList<String>();
 			List<String> shopNameList = new LinkedList<String>();
 			List<List<Double>> gpsList = new LinkedList<List<Double>>();
 			List<String> address = new LinkedList<String>(); 
 			
 			List<?> shopList = getShopListBlock(productElement, data);
 			if (shopList != null){
 				Iterator<?> shopListIter = shopList.iterator();
 				while (shopListIter.hasNext()){
 					Element shop = (Element)shopListIter.next();
 					String shopName = getFieldValue(shop, "name");
 					String shopTel = getFieldValue(shop, "tel");
 					
 					
 					String shopAddress = "";
 					if (isPostString == null || isPostString.equalsIgnoreCase("no")) {
 						String addressString = getFieldValue(shop, "addr");
 						if (addressString == null) {
 							addressString = getFieldValue(shop, "address");	// adapt to Jingdong/Manzuo
 						}
 						// parse the address
 						shopAddress = parseAddress(addressString);
					}										
 
 					String longitude = getFieldValue(shop, "longitude");
 					String latitude = getFieldValue(shop, "latitude");					
 					
 					// TODO add into address list
 //					System.out.println("shop="+shopName+","+shopTel+","+shopAddress+","+latitude+","+longitude);
 					
 					if (shopAddress != null && shopAddress.length() > 0)
 						address.add(shopAddress);
 					
 					if (shopTel != null && shopTel.length() > 0)
 						telList.add(shopTel);
 					
 					if (shopName != null && shopName.length() > 0)
 						shopNameList.add(shopName);
 
 					if (longitude != null && longitude.length() > 0 &&
 						latitude != null && latitude.length() > 0){
 						Gps gps = new Gps(latitude, longitude);
 						gpsList.add(gps.toDoubleList());
 					}
 					
 				}
 			}
 
 			Product product = saveProduct(mongoClient, city, loc, image, title, startDate, endDate, 
 					price, value, bought, siteId, website, siteurl, major, address, addressParser, gpsList);
 			
 			if (product != null){							
 				// save extra fields
 				product.setMerchantEndDate(merchantEndDate);
 				if (isPostString != null)
 					product.setPost(StringUtil.booleanFromString(isPostString));
 				if (soldOutString != null)
 					product.setSoldOut(StringUtil.booleanFromString(soldOutString));
 				product.setQuota(maxQuota, minQuota);
 				product.setTag(tag);
 				product.setPriority(priority);
 				product.setDescription(deleteXmlTag(description));
 				product.setDetail(deleteXmlTag(detail));
 				product.setRange(range);
 				product.setCategory(category);
 				product.setWapLoc(generateWapLoc(loc, image));
 				product.setTel(telList);
 				product.setShopList(shopNameList);
 				product.setGPS(gpsList);
 
 				ProductManager.save(mongoClient, product);
 				ProductManager.createSolrIndex(product, false);
 //				log.info("save final product="+product.toString());
 			}					
 			
 			commitSolrIndex();
 			
 		}		
 		
 		return true;
 	}
 
 	
 
 	private int setCategoryByTag(String allTags, String city) {
 		
 		int category = DBConstants.C_CATEGORY_UNKNOWN;
 		if (allTags == null)
 			return category;
 		
 		if (allTags.matches(".*(美食|食品|菜|餐|吃).*") && !city.equalsIgnoreCase("全国")){
 				category = DBConstants.C_CATEGORY_EAT;
 		}
 		else if (allTags.matches(".*(美容|化妆).*")){
 			category = DBConstants.C_CATEGORY_FACE;				
 		}
 		else if (allTags.matches(".*(娱乐|玩|休闲|电影|KTV).*") && !city.equalsIgnoreCase("全国")){
 			category = DBConstants.C_CATEGORY_FUN;				
 		}
 		else if (allTags.matches(".*(运动|健身|球).*")){
 			category = DBConstants.C_CATEGORY_KEEPFIT;
 		}
 		else if (allTags.matches(".*(生活|酒店|旅).*")){
 			category = DBConstants.C_CATEGORY_LIFE;				
 		}
 		else if (allTags.matches(".*购.*")){
 			category = DBConstants.C_CATEGORY_SHOPPING;								
 		}
 		
 //		if (allTags.contains("美食") || allTags.contains("食品") || allTags.contains("菜") || 
 //			allTags.contains("餐") || allTags.contains("吃")){
 //			category = DBConstants.C_CATEGORY_EAT;
 //		}
 //		else if (allTags.contains("美容") || allTags.contains("化妆")){
 //			category = DBConstants.C_CATEGORY_FACE;				
 //		}
 //		else if (allTags.contains("娱乐") || allTags.contains("玩") || allTags.contains("休闲") || 
 //				allTags.contains("电影") || allTags.contains("KTV")){
 //			category = DBConstants.C_CATEGORY_FUN;				
 //		}
 //		else if (allTags.contains("运动") || allTags.contains("健身") || allTags.contains("球")){
 //			category = DBConstants.C_CATEGORY_KEEPFIT;
 //		}
 //		else if (allTags.contains("生活") || allTags.contains("酒店") || allTags.contains("旅")){
 //			category = DBConstants.C_CATEGORY_LIFE;				
 //		}
 //		else if (allTags.contains("购")){
 //			category = DBConstants.C_CATEGORY_SHOPPING;								
 //		}
 		
 		return category;
 	}
 
 	private String getDefaultSiteURL() {
 		return "";
 	}
 	
 	public List<?> getShopListBlock(Element productElement, Element dataElement){
 		return getFieldBlock(productElement, "data", "shops", "shop");		
 	}
 
 	@Override
 	public boolean disableAddressParsing() {
 		return false;
 	}
 
 }
