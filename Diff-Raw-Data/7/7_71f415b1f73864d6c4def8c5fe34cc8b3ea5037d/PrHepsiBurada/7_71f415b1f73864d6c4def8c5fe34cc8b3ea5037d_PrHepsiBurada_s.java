 package parsers;
 
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import models.Product;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class PrHepsiBurada{
 	
 	public static Product getContentPrice(String receivedURL){
 		Product parsedProduct = new Product();
 		
 		parsedProduct.site = "HepsiBurada";
 		parsedProduct.source = receivedURL;
 		parsedProduct.picture = null;
 		
 		Document doc;
 		String prodPrice = "NOTFOUND";
 		String prodName = "NOTFOUND";
 		
 		try {
 			// need http protocol
 			doc = Jsoup.connect(receivedURL).get();
 		 
 			Elements spanIds = doc.select("span[id]");
 				
 			for (Element spanId : spanIds) {
 
 				if(spanId.attr("id").equals("ctl00_ContentPlaceHolder1_ProductControl1_MainControl1_ProductMain1_lblPriceWithTax")){
 //					System.out.println("Price is: " + spanId.text()));
 					prodPrice = spanId.text();
 				}
 				if(spanId.attr("id").equals("ctl00_ContentPlaceHolder1_ProductControl1_MainControl1_ProductMain1_lblProductName")){
 //					System.out.println("Price is: " + spanId.text());
 					prodName = spanId.text();
 				}
 			}
 			
 			if( prodPrice != null && !"".equals(prodPrice))	prodPrice = trimPrice(prodPrice);
 			
 			parsedProduct.title = prodName;
 			
 			parsedProduct.attr1 = "INITALPRICE";
 			parsedProduct.attr1value = prodPrice;
 			parsedProduct.attr2 = "CURRENTPRICE";
 			parsedProduct.attr2value = prodPrice;
 			
			parsedProduct.pid=1;
 			
 			
 		} catch (MalformedURLException e) {
 			System.out.println("ERROR: MalformedURLException type at HepsiBurada parser!");
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			System.out.println("ERROR: IOException type at HepsiBurada parser!");
 			e.printStackTrace();
 			return null;
 		}	
 		
 		return parsedProduct;
 	}
 	
 	// HepsiBurada parsed price is at '69,89 TL' format
 //	private static double trimPrice(String priceStr){
 //		double price = -1;
 //		try{
 //			String priceStrUpd = (String) priceStr.subSequence(0, priceStr.length()-3);
 //			priceStrUpd = priceStrUpd.replace(",", ".");
 //		//	System.out.println(priceStrUpd);
 //			price = Double.parseDouble(priceStrUpd);
 //		}catch(Exception e){
 //			System.out.println("ERROR: Exception type at HepsiBurada parser!");
 //			e.printStackTrace();
 //		}
 //		return price;
 //	}
 	
 	private static String trimPrice(String priceStr){
 		String price = "NOTFOUND";
 		try{
 			price = (String) priceStr.subSequence(0, priceStr.length()-3);
 			price = price.replace(",", ".");
 
 		}catch(Exception e){
 			System.out.println("ERROR: Exception type at HepsiBurada parser!");
 			e.printStackTrace();
 		}
 		return price;
 	}
 	
 	public static void main(String[] args) {
 		System.out.println("Price is:[" + getContentPrice("http://www.hepsiburada.com/liste/life-zigon-sehpa-beyaz-/productDetails.aspx?productId=mbmdllifeb&categoryId=18021617&navq=categoryid%3d18021617%26fh_view_size%3d12%26fh_sort_by%3d-order_stock_attribute_pl%252c-ranking_cocktail_bestseller%26fh_maxdisplaynrvalues_brand%3d-1%26fh_secondid%3dmbmdllifeb%26fh_lister_pos%3d1%26fh_location%3d%252f%252fcatalog01%252ftr_TR%252fbrand%253d%257bmobetto%257d%252fcategories%253c%257bcatalog01_60002028%257d%252fcategories%253c%257bcatalog01_60002028_2147483614%257d%252fcategories%253c%257bcatalog01_60002028_2147483614_18021299%257d%252fcategories%253c%257bcatalog01_60002028_2147483614_18021299_18021303%257d%252fcategories%253c%257bcatalog01_60002028_2147483614_18021299_18021303_18021332%257d%252fcategories%253c%257bcatalog01_60002028_2147483614_18021299_18021303_18021332_18021617%257d%26fh_refview%3dlister") + "]");
 	}
 
 }
